package org.eclipse.jdt.internal.compiler.batch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJar implements FileSystem.Classpath {
	
ZipFile zipFile;
Hashtable packageCache;
boolean closeZipFileAtEnd;

public ClasspathJar(File file) throws IOException {
	this(new ZipFile(file), true);
}
public ClasspathJar(ZipFile zipFile, boolean closeZipFileAtEnd) throws IOException {
	this.zipFile = zipFile;
	this.packageCache = null;
	this.closeZipFileAtEnd = closeZipFileAtEnd;
}	
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.read(zipFile, qualifiedBinaryFileName);
		if (reader != null) return new NameEnvironmentAnswer(reader);
	} catch (Exception e) {} // treat as if class file is missing
	return null;
}
public boolean isPackage(String qualifiedPackageName) {
	if (packageCache != null)
		return packageCache.containsKey(qualifiedPackageName);

	this.packageCache = new Hashtable(41);
	packageCache.put("", ""); //$NON-NLS-1$ //$NON-NLS-2$

	nextEntry : for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// add the package name & all of its parent packages
		int last = fileName.lastIndexOf('/');
		while (last > 0) {
			// extract the package name
			String packageName = fileName.substring(0, last);
			if (packageCache.containsKey(packageName))
				continue nextEntry;
			packageCache.put(packageName, packageName);
			last = packageName.lastIndexOf('/');
		}
	}
	return packageCache.containsKey(qualifiedPackageName);
}
public void reset() {
	if (zipFile != null && closeZipFileAtEnd) {
		try { zipFile.close(); } catch(IOException e) {}
	}
	this.packageCache = new Hashtable(41);
}
public String toString() {
	return "Classpath for jar file " + zipFile; //$NON-NLS-1$
}
}