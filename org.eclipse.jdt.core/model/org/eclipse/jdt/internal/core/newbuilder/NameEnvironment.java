package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

import java.io.*;
import java.util.*;

public class NameEnvironment implements INameEnvironment {

ClasspathLocation[] classpathLocations;
String[] initialTypeNames; // assumed that each name is of the form "a/b/ClassName"
String[] additionalSourceFilenames; // assumed that each name is of the form "d:/eclipse/Test/a/b/ClassName.java"

public NameEnvironment(ClasspathLocation[] classpathLocations) {
	this.classpathLocations = classpathLocations;
}

public NameEnvironment(IJavaProject javaProject) {
	try {
		IWorkspaceRoot workspaceRoot = javaProject.getProject().getWorkspace().getRoot();
		IResource outputFolder = workspaceRoot.findMember(javaProject.getOutputLocation());
		String outputFolderLocation = null;
		if (outputFolder != null && outputFolder.exists())
			outputFolderLocation = outputFolder.getLocation().toString();
		this.classpathLocations = computeLocations(workspaceRoot, javaProject, outputFolderLocation, null, null);
	} catch(JavaModelException e) {
		this.classpathLocations = new ClasspathLocation[0];
	}
}

/* Some examples of resolved class path entries.
* Remember to search class path in the order that it was defined.
*
* 1a. typical project with no source folders:
*   /Test[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test
* 1b. project with source folders:
*   /Test/src1[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test/src1
*   /Test/src2[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test/src2
*  NOTE: These can be in any order & separated by prereq projects or libraries
* 1c. project external to workspace (only detectable using getLocation()):
*   /Test/src[CPE_SOURCE][K_SOURCE] -> d:/eclipse.zzz/src
*  Need to search source folder & output folder TOGETHER
*  Use .java file if its more recent than .class file
*
* 2. zip files:
*   D:/j9/lib/jclMax/classes.zip[CPE_LIBRARY][K_BINARY][sourcePath:d:/j9/lib/jclMax/source/source.zip]
*      -> D:/j9/lib/jclMax/classes.zip
*  ALWAYS want to take the library path as is
*
* 3a. prereq project (regardless of whether it has a source or output folder):
*   /Test[CPE_PROJECT][K_SOURCE] -> D:/eclipse.test/Test
*  ALWAYS want to append the output folder & ONLY search for .class files
*/
public static ClasspathLocation[] computeLocations(
	IWorkspaceRoot workspaceRoot,
	IJavaProject javaProject,
	String outputFolderLocation,
	ArrayList sourceFolders,
	SimpleLookupTable prereqOutputFolders) throws JavaModelException {

	IClasspathEntry[] classpathEntries = ((JavaProject) javaProject).getExpandedClasspath(true);
	int cpCount = 0;
	int max = classpathEntries.length;
	ClasspathLocation[] classpathLocations = new ClasspathLocation[max];

	boolean firstSourceFolder = true;
	nextEntry : for (int i = 0; i < max; i++) {
		IClasspathEntry entry = classpathEntries[i];
		Object target = JavaModel.getTarget(workspaceRoot, entry.getPath(), true);
		if (target == null) continue nextEntry;

		if (target instanceof IResource) {
			IResource resource = (IResource) target;
			switch(entry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE :
					if (outputFolderLocation == null || !(resource instanceof IContainer)) continue nextEntry;
					if (sourceFolders != null) { // normal builder mode
						sourceFolders.add(resource);
						classpathLocations[cpCount++] =
							ClasspathLocation.forSourceFolder(resource.getLocation().toString(), outputFolderLocation);
					} else if (firstSourceFolder) { // add the output folder only once
						firstSourceFolder = false;
						classpathLocations[cpCount++] = ClasspathLocation.forBinaryFolder(outputFolderLocation);
					}
					continue nextEntry;

				case IClasspathEntry.CPE_PROJECT :
					if (!(resource instanceof IProject)) continue nextEntry;
					IProject prereqProject = (IProject) resource;
					IPath outputLocation = JavaCore.create(prereqProject).getOutputLocation();
					IResource prereqOutputFolder;
					if (prereqProject.getFullPath().equals(outputLocation)) {
						prereqOutputFolder = prereqProject;
					} else {
						prereqOutputFolder = workspaceRoot.findMember(outputLocation);
						if (prereqOutputFolder == null || !prereqOutputFolder.exists() || !(prereqOutputFolder instanceof IFolder))
							continue nextEntry;
					}
					if (prereqOutputFolders != null)
						prereqOutputFolders.put(prereqProject, prereqOutputFolder);
					classpathLocations[cpCount++] = ClasspathLocation.forBinaryFolder(prereqOutputFolder.getLocation().toString());
					continue nextEntry;

				case IClasspathEntry.CPE_LIBRARY :
					if (resource instanceof IFile) {
						String extension = entry.getPath().getFileExtension();
						if (!(JavaBuilder.JAR_EXTENSION.equalsIgnoreCase(extension) || JavaBuilder.ZIP_EXTENSION.equalsIgnoreCase(extension)))
							continue nextEntry;
						classpathLocations[cpCount++] = ClasspathLocation.forLibrary(resource.getLocation().toString());
					} else if (resource instanceof IFolder) {
						classpathLocations[cpCount++] = ClasspathLocation.forBinaryFolder(resource.getLocation().toString());
					}
					continue nextEntry;
			}
		} else if (target instanceof File) {
			String extension = entry.getPath().getFileExtension();
			if (!(JavaBuilder.JAR_EXTENSION.equalsIgnoreCase(extension) || JavaBuilder.ZIP_EXTENSION.equalsIgnoreCase(extension)))
				continue nextEntry;
			classpathLocations[cpCount++] = ClasspathLocation.forLibrary(entry.getPath().toString());
		}
	}
	if (cpCount < max)
		System.arraycopy(classpathLocations, 0, (classpathLocations = new ClasspathLocation[cpCount]), 0, cpCount);
	return classpathLocations;
}

static String assembleName(char[] fileName, char[][] packageName, char separator) {
	return new String(CharOperation.concatWith(packageName, fileName, separator));
}

static String assembleName(String fileName, char[][] packageName, char separator) {
	return new String(
		CharOperation.concatWith(
			packageName,
			fileName == null ? null : fileName.toCharArray(),
			separator));
}

private NameEnvironmentAnswer findClass(char[] name, char[][] packageName) {
	String fullName = assembleName(name, packageName, '/');
	if (initialTypeNames != null)
		for (int i = 0, length = initialTypeNames.length; i < length; i++)
			if (fullName.equals(initialTypeNames[i]))
				return null; // looking for a file which we know was provided at the beginning of the compilation
	for (int i = 0, length = classpathLocations.length; i < length; i++) {
		NameEnvironmentAnswer answer = classpathLocations[i].findClass(name, packageName);
		if (answer != null) return answer;
	}
	return null; 
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName == null) return null;
	return findClass(
		compoundName[compoundName.length - 1],
		CharOperation.subarray(compoundName, 0, compoundName.length - 1));
}

public NameEnvironmentAnswer findType(char[] name, char[][] compoundName) {
	if (name == null) return null;
	return findClass(name, compoundName);
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	if (compoundName == null)
		compoundName = new char[0][];

	for (int i = 0, length = classpathLocations.length; i < length; i++)
		if (classpathLocations[i].isPackage(compoundName, packageName))
			return true;
	return false;
}

public void reset() {
}

void setNames(String[] initialTypeNames, String[] additionalSourceFilenames) {
	this.initialTypeNames = initialTypeNames;
	this.additionalSourceFilenames = additionalSourceFilenames;
	for (int i = 0, length = classpathLocations.length; i < length; i++) {
		ClasspathLocation classpath = classpathLocations[i];
		classpath.reset();
		if (classpath instanceof ClasspathMultiDirectory)
			((ClasspathMultiDirectory) classpath).nameEnvironment = this;
	}
}
}