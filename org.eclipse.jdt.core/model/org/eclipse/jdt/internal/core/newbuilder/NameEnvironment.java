package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.util.*;

import java.io.*;

public class NameEnvironment implements INameEnvironment {

ClasspathLocation[] classpathLocations;
State state;
String[] initialTypeNames; // assumed that each name is of the form "a/b/ClassName"
String[] additionalSourceFilenames; // assumed that each name is of the form "d:/eclipse/Test/a/b/ClassName.java"

public NameEnvironment(ClasspathLocation[] classpathLocations, State state) {
	this.classpathLocations = classpathLocations;
	this.state = state;
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