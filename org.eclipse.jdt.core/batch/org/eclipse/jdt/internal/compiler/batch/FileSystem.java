package org.eclipse.jdt.internal.compiler.batch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class FileSystem implements INameEnvironment  {
	Classpath[] classpaths;
	String[] knownFileNames;

	interface Classpath {
		boolean exists(String filename, char[][] packageName);
		long lastModified(String filename, char[][] packageName);
		NameEnvironmentAnswer readClassFile(String filename, char[][] packageName);
		NameEnvironmentAnswer readJavaFile(String filename, char[][] packageName);
		boolean isPackage(char[][] compoundName, char[] packageName); 
	}
/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/

public FileSystem(String[] classpathNames, String[] initialFileNames) {
	int classpathSize = classpathNames.length;
	classpaths = new Classpath[classpathSize];
	String[] pathNames = new String[classpathSize];
	int problemsOccured = 0;
	for (int i = 0; i < classpathSize; i++) {
		try {
			File file = new File(convertPathSeparators(classpathNames[i]));
			if (file.exists()) {
				if (file.isDirectory()) {
					classpaths[i] = new ClasspathDirectory(file);
					pathNames[i] = ((ClasspathDirectory) classpaths[i]).path;
				} else if (classpathNames[i].endsWith(".jar") | (classpathNames[i].endsWith(".zip"))) {
					classpaths[i] = new ClasspathJar(file);
					pathNames[i] = classpathNames[i].substring(0, classpathNames[i].lastIndexOf('.'));
				}
			}
		} catch (IOException e) {
			classpaths[i] = null;
		}
		if (classpaths[i] == null)
			problemsOccured++;
	}
	if (problemsOccured > 0) {
		Classpath[] newPaths = new Classpath[classpathSize - problemsOccured];
		String[] newNames = new String[classpathSize - problemsOccured];
		for (int i = 0, current = 0; i < classpathSize; i++)
			if (classpaths[i] != null) {
				newPaths[current] = classpaths[i];
				newNames[current++] = pathNames[i];
			}
		classpathSize = newPaths.length;
		classpaths = newPaths;
		pathNames = newNames;
	}

	knownFileNames = new String[initialFileNames.length];
	for (int i = initialFileNames.length; --i >= 0;) {
		String fileName = initialFileNames[i];
		String matchingPathName = null;
		if (fileName.lastIndexOf(".") != -1)
			fileName = fileName.substring(0, fileName.lastIndexOf('.')); // remove trailing ".java"

		fileName = convertPathSeparators(fileName);
		for (int j = 0; j < classpathSize; j++)
			if (fileName.startsWith(pathNames[j]))
				matchingPathName = pathNames[j];
		if (matchingPathName == null)
			knownFileNames[i] = fileName; // leave as is...
		else
			knownFileNames[i] = fileName.substring(matchingPathName.length());
	}
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
private String convertPathSeparators(String path) {
	if (File.separatorChar == '/')
		return path.replace('\\', '/');
	else
		return path.replace('/', '\\');
}
private NameEnvironmentAnswer findClass(char[] name, char[][] packageName) {
	String fullName = assembleName(name, packageName, File.separatorChar);
	for (int i = 0, length = knownFileNames.length; i < length; i++)
		if (fullName.equals(knownFileNames[i]))
			return null; // looking for a file which we know was provided at the beginning of the compilation

	String filename = new String(name);
	String binaryFilename = filename + ".class";
	String sourceFilename = filename + ".java";
	for (int i = 0, length = classpaths.length; i < length; i++) {
		Classpath classpath = classpaths[i];
		boolean binaryExists = classpath.exists(binaryFilename, packageName);
		boolean sourceExists = classpath.exists(sourceFilename, packageName);
		if (binaryExists == sourceExists) {
			if (binaryExists) { // so both are true
				long binaryModified = classpath.lastModified(binaryFilename, packageName);
				long sourceModified = classpath.lastModified(sourceFilename, packageName);
				if (binaryModified > sourceModified)
					return classpath.readClassFile(binaryFilename, packageName);
				if (sourceModified > 0)
					return classpath.readJavaFile(sourceFilename, packageName);
			}
		} else {
			if (binaryExists)
				return classpath.readClassFile(binaryFilename, packageName);
			else
				return classpath.readJavaFile(sourceFilename, packageName);
		}
	}
	return null; 
}
public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName == null)
		return null;
	else
		return findClass(
			compoundName[compoundName.length - 1],
			CharOperation.subarray(compoundName, 0, compoundName.length - 1));
}
public NameEnvironmentAnswer findType(char[] name, char[][] compoundName) {
	if (name == null)
		return null;
	else
		return findClass(name, compoundName);
}
public boolean isPackage(char[][] compoundName, char[] packageName) {
	if (compoundName == null)
		compoundName = new char[0][];

	for (int i = 0, length = classpaths.length; i < length; i++)
		if (classpaths[i].isPackage(compoundName, packageName))
			return true;
	return false;
}
}
