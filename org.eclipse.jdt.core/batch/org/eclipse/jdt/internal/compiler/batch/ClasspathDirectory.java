package org.eclipse.jdt.internal.compiler.batch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;

class ClasspathDirectory implements FileSystem.Classpath {
	String path;
	Hashtable missingPackages;
	Hashtable directoryCache;
ClasspathDirectory(File directory) {
	this.path = directory.getAbsolutePath();
	if (!path.endsWith(File.separator))
		this.path += File.separator;
	this.missingPackages = new Hashtable(11);
	this.directoryCache = new Hashtable(11);
}
private String[] directoryList(char[][] compoundName, char[] packageName) {
	String partialPath = FileSystem.assembleName(packageName, compoundName, File.separatorChar);
	String[] dirList = (String[])directoryCache.get(partialPath);
	if (dirList != null)
		return dirList;
	if (missingPackages.containsKey(partialPath))
		return null;

	File dir = new File(path + partialPath);
	if (dir != null && dir.isDirectory()) {
		boolean matchesName = packageName == null;
		if (!matchesName) {
			int index = packageName.length;
			while (--index >= 0 && !Character.isUpperCase(packageName[index])) {}
			matchesName = index < 0 || exists(new String(packageName), compoundName); // verify that the case sensitive packageName really does exist
		}
		if (matchesName) {
			if ((dirList = dir.list()) == null)
				dirList = new String[0];
			directoryCache.put(partialPath, dirList);
			return dirList;
		}
	}
	missingPackages.put(partialPath, partialPath); // value is not used
	return null;
}
public boolean exists(String filename, char[][] packageName) {
	String[] dirList = directoryList(packageName, null);
	if (dirList != null)
		for (int i = dirList.length; --i >= 0;)
			if (filename.equals(dirList[i]))
				return true;
	return false;
}
public boolean isPackage(char[][] compoundName, char[] packageName) {
	return directoryList(compoundName, packageName) != null;
}
public long lastModified(String filename, char[][] packageName) {
	File file = new File(path + FileSystem.assembleName(filename, packageName, File.separatorChar));
	return file.lastModified();
}
public NameEnvironmentAnswer readClassFile(String filename, char[][] packageName) {
	try {
		return new NameEnvironmentAnswer(
			ClassFileReader.read(path + FileSystem.assembleName(filename, packageName, File.separatorChar)));
	} catch (Exception e) {
		return null; // treat as if class file is missing
	}
}
public NameEnvironmentAnswer readJavaFile(String fileName, char[][] packageName) {
	String fullName = path + FileSystem.assembleName(fileName, packageName, File.separatorChar);
	return new NameEnvironmentAnswer(new CompilationUnit(null, fullName));
}
public String toString() {
	return "ClasspathDirectory " + path;
}
}
