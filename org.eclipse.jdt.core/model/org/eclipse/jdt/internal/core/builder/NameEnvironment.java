/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

import java.io.*;
import java.util.*;

public class NameEnvironment implements INameEnvironment {

boolean isIncrementalBuild;
ClasspathMultiDirectory[] sourceLocations;
ClasspathLocation[] binaryLocations;

String[] initialTypeNames; // assumed that each name is of the form "a/b/ClassName"
SourceFile[] additionalUnits;

NameEnvironment(IWorkspaceRoot root, JavaProject javaProject, SimpleLookupTable binaryLocationsPerProject) throws CoreException {
	this.isIncrementalBuild = false;
	computeClasspathLocations(root, javaProject, binaryLocationsPerProject);
	setNames(null, null);
}

public NameEnvironment(IJavaProject javaProject) {
	this.isIncrementalBuild = false;
	try {
		computeClasspathLocations(javaProject.getProject().getWorkspace().getRoot(), (JavaProject) javaProject, null);
	} catch(CoreException e) {
		this.sourceLocations = new ClasspathMultiDirectory[0];
		this.binaryLocations = new ClasspathLocation[0];
	}
	setNames(null, null);
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
*  Need to search source folder & output folder
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
private void computeClasspathLocations(
	IWorkspaceRoot root,
	JavaProject javaProject,
	SimpleLookupTable binaryLocationsPerProject) throws CoreException {

	/* Update incomplete classpath marker */
	IClasspathEntry[] classpathEntries = javaProject.getExpandedClasspath(true, true);

	/* Update cycle marker */
	IMarker cycleMarker = javaProject.getCycleMarker();
	if (cycleMarker != null) {
		int severity = JavaCore.ERROR.equals(javaProject.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true))
			? IMarker.SEVERITY_ERROR
			: IMarker.SEVERITY_WARNING;
		if (severity != ((Integer) cycleMarker.getAttribute(IMarker.SEVERITY)).intValue())
			cycleMarker.setAttribute(IMarker.SEVERITY, severity);
	}

	ArrayList sLocations = new ArrayList(classpathEntries.length);
	ArrayList bLocations = new ArrayList(classpathEntries.length);
	nextEntry : for (int i = 0, l = classpathEntries.length; i < l; i++) {
		ClasspathEntry entry = (ClasspathEntry) classpathEntries[i];
		IPath path = entry.getPath();
		Object target = JavaModel.getTarget(root, path, true);
		if (target == null) continue nextEntry;

		switch(entry.getEntryKind()) {
			case IClasspathEntry.CPE_SOURCE :
				if (!(target instanceof IContainer)) continue nextEntry;
				IPath outputPath = entry.getOutputLocation() != null 
					? entry.getOutputLocation() 
					: javaProject.getOutputLocation();
				IContainer outputFolder;
				if (outputPath.segmentCount() == 1) {
					outputFolder = javaProject.getProject();
				} else {
					outputFolder = root.getFolder(outputPath);
					if (!outputFolder.exists())
						createFolder(outputFolder);
				}
				sLocations.add(
					ClasspathLocation.forSourceFolder((IContainer) target, outputFolder, entry.fullExclusionPatternChars()));
				continue nextEntry;

			case IClasspathEntry.CPE_PROJECT :
				if (!(target instanceof IProject)) continue nextEntry;
				IProject prereqProject = (IProject) target;
				if (!JavaProject.hasJavaNature(prereqProject)) continue nextEntry; // if project doesn't have java nature or is not accessible

				JavaProject prereqJavaProject = (JavaProject) JavaCore.create(prereqProject);
				IClasspathEntry[] prereqClasspathEntries = prereqJavaProject.getExpandedClasspath(true, true);
				ArrayList seen = new ArrayList();
				for (int j = 0, m = prereqClasspathEntries.length; j < m; j++) {
					ClasspathEntry e = (ClasspathEntry) prereqClasspathEntries[j];
					Object t = JavaModel.getTarget(root, e.getPath(), true);
					if (t instanceof IContainer && e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						IPath prereqOutputPath = e.getOutputLocation() != null 
							? e.getOutputLocation() 
							: prereqJavaProject.getOutputLocation();
						IContainer binaryFolder = prereqOutputPath.segmentCount() == 1
							? (IContainer) prereqProject
							: (IContainer) root.getFolder(prereqOutputPath);
						if (binaryFolder.exists() && !seen.contains(binaryFolder)) {
							seen.add(binaryFolder);
							ClasspathLocation bLocation = ClasspathLocation.forBinaryFolder(binaryFolder, true);
							bLocations.add(bLocation);
							if (binaryLocationsPerProject != null) { // normal builder mode
								ClasspathLocation[] existingLocations = (ClasspathLocation[]) binaryLocationsPerProject.get(prereqProject);
								if (existingLocations == null) {
									existingLocations = new ClasspathLocation[] {bLocation};
								} else {
									int size = existingLocations.length;
									System.arraycopy(existingLocations, 0, existingLocations = new ClasspathLocation[size + 1], 0, size);
									existingLocations[size] = bLocation;
								}
								binaryLocationsPerProject.put(prereqProject, existingLocations);
							}
						}
					}
				}
				continue nextEntry;

			case IClasspathEntry.CPE_LIBRARY :
				if (target instanceof IResource) {
					IResource resource = (IResource) target;
					ClasspathLocation bLocation = null;
					if (resource instanceof IFile) {
						if (!(Util.isArchiveFileName(path.lastSegment())))
							continue nextEntry;
						bLocation = ClasspathLocation.forLibrary((IFile) resource);
					} else if (resource instanceof IContainer) {
						bLocation = ClasspathLocation.forBinaryFolder((IContainer) target, false); // is library folder not output folder
					}
					bLocations.add(bLocation);
					if (binaryLocationsPerProject != null) { // normal builder mode
						IProject p = resource.getProject(); // can be the project being built
						ClasspathLocation[] existingLocations = (ClasspathLocation[]) binaryLocationsPerProject.get(p);
						if (existingLocations == null) {
							existingLocations = new ClasspathLocation[] {bLocation};
						} else {
							int size = existingLocations.length;
							System.arraycopy(existingLocations, 0, existingLocations = new ClasspathLocation[size + 1], 0, size);
							existingLocations[size] = bLocation;
						}
						binaryLocationsPerProject.put(p, existingLocations);
					}
				} else if (target instanceof File) {
					if (!(Util.isArchiveFileName(path.lastSegment())))
						continue nextEntry;
					bLocations.add(ClasspathLocation.forLibrary(path.toString()));
				}
				continue nextEntry;
		}
	}

	// now split the classpath locations... place the output folders ahead of the other .class file folders & jars
	ArrayList outputFolders = new ArrayList(1);
	this.sourceLocations = new ClasspathMultiDirectory[sLocations.size()];
	if (!sLocations.isEmpty()) {
		sLocations.toArray(this.sourceLocations);

		// collect the output folders, skipping duplicates
		next : for (int i = 0, l = sourceLocations.length; i < l; i++) {
			ClasspathMultiDirectory md = sourceLocations[i];
			IPath outputPath = md.binaryFolder.getFullPath();
			for (int j = 0; j < i; j++) { // compare against previously walked source folders
				if (outputPath.equals(sourceLocations[j].binaryFolder.getFullPath())) {
					md.hasIndependentOutputFolder = sourceLocations[j].hasIndependentOutputFolder;
					continue next;
				}
			}
			outputFolders.add(md);

			// also tag each source folder whose output folder is an independent folder & is not also a source folder
			for (int j = 0, m = sourceLocations.length; j < m; j++)
				if (outputPath.equals(sourceLocations[j].sourceFolder.getFullPath()))
					continue next;
			md.hasIndependentOutputFolder = true;
		}
	}

	// combine the output folders with the binary folders & jars... place the output folders before other .class file folders & jars
	this.binaryLocations = new ClasspathLocation[outputFolders.size() + bLocations.size()];
	int index = 0;
	for (int i = 0, l = outputFolders.size(); i < l; i++)
		this.binaryLocations[index++] = (ClasspathLocation) outputFolders.get(i);
	for (int i = 0, l = bLocations.size(); i < l; i++)
		this.binaryLocations[index++] = (ClasspathLocation) bLocations.get(i);
}

public void cleanup() {
	this.initialTypeNames = null;
	this.additionalUnits = null;
	for (int i = 0, l = sourceLocations.length; i < l; i++)
		sourceLocations[i].cleanup();
	for (int i = 0, l = binaryLocations.length; i < l; i++)
		binaryLocations[i].cleanup();
}

private void createFolder(IContainer folder) throws CoreException {
	if (!folder.exists()) {
		createFolder(folder.getParent());
		((IFolder) folder).create(true, true, null);
	}
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName) {
	if (initialTypeNames != null) {
		for (int i = 0, l = initialTypeNames.length; i < l; i++) {
			if (qualifiedTypeName.equals(initialTypeNames[i])) {
				if (isIncrementalBuild)
					// catch the case that a type inside a source file has been renamed but other class files are looking for it
					throw new AbortCompilation(true, new AbortIncrementalBuildException(qualifiedTypeName));
				return null; // looking for a file which we know was provided at the beginning of the compilation
			}
		}
	}

	if (additionalUnits != null && sourceLocations.length > 0) {
		// if an additional source file is waiting to be compiled, answer it BUT not if this is a secondary type search
		// if we answer X.java & it no longer defines Y then the binary type looking for Y will think the class path is wrong
		// let the recompile loop fix up dependents when the secondary type Y has been deleted from X.java
		IPath qSourceFilePath = new Path(qualifiedTypeName + ".java"); //$NON-NLS-1$
		int qSegmentCount = qSourceFilePath.segmentCount();
		next : for (int i = 0, l = additionalUnits.length; i < l; i++) {
			SourceFile additionalUnit = additionalUnits[i];
			IPath fullPath = additionalUnit.resource.getFullPath();
			int prefixCount = additionalUnit.sourceLocation.sourceFolder.getFullPath().segmentCount();
			if (qSegmentCount == fullPath.segmentCount() - prefixCount) {
				for (int j = 0; j < qSegmentCount; j++)
					if (!qSourceFilePath.segment(j).equals(fullPath.segment(j + prefixCount)))
						continue next;
				return new NameEnvironmentAnswer(additionalUnit);
			}
		}
	}

	String qBinaryFileName = qualifiedTypeName + ".class"; //$NON-NLS-1$
	String binaryFileName = qBinaryFileName;
	String qPackageName =  ""; //$NON-NLS-1$
	if (qualifiedTypeName.length() > typeName.length) {
		int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
		qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
		binaryFileName = qBinaryFileName.substring(typeNameStart);
	}

	// NOTE: the output folders are added at the beginning of the binaryLocations
	for (int i = 0, l = binaryLocations.length; i < l; i++) {
		NameEnvironmentAnswer answer = binaryLocations[i].findClass(binaryFileName, qPackageName, qBinaryFileName);
		if (answer != null) return answer;
	}
	return null;
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1]);
	return null;
}

public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName);
	return null;
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return isPackage(new String(CharOperation.concatWith(compoundName, packageName, '/')));
}

public boolean isPackage(String qualifiedPackageName) {
	// NOTE: the output folders are added at the beginning of the binaryLocations
	for (int i = 0, l = binaryLocations.length; i < l; i++)
		if (binaryLocations[i].isPackage(qualifiedPackageName))
			return true;
	return false;
}

void setNames(String[] initialTypeNames, SourceFile[] additionalUnits) {
	this.initialTypeNames = initialTypeNames;
	this.additionalUnits = additionalUnits;
	for (int i = 0, l = sourceLocations.length; i < l; i++)
		sourceLocations[i].reset();
	for (int i = 0, l = binaryLocations.length; i < l; i++)
		binaryLocations[i].reset();
}
}