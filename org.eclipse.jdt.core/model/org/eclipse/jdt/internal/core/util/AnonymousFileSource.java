package org.eclipse.jdt.internal.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.*;


/** An anonymous file source creates files in the given directory.
 */
public class AnonymousFileSource {
	File fDirectory;
/**
 * Creates an anonymous file source which creates files in the given directory.
 */
public AnonymousFileSource(File directory) {
	if (!directory.exists()) {
		directory.mkdirs();
	} else if (!directory.isDirectory()) {
		throw new IllegalArgumentException("Directory arguments should be a directory."/*nonNLS*/);
	}
	fDirectory = directory;	
}
/**
 * Allocates and returns a RandomAccessFile in R/W mode on a new anonymous file.
 * Guaranteed to be unallocated.
 */
synchronized public RandomAccessFile allocateAnonymousFile() throws IOException {
	
	File file = getAnonymousFile();
	return new RandomAccessFile(file, "rw"/*nonNLS*/);
}
/**
 * Returns a URL on a newly allocated file with the given initial content.
 * Guaranteed to be unallocated.
 */
synchronized public URL allocateAnonymousURL(byte[] bytes) throws IOException {
	try {
		byte hasharray[] = java.security.MessageDigest.getInstance("SHA"/*nonNLS*/).digest(bytes);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hasharray.length; i++) {
			sb.append(Character.forDigit((int)((hasharray[i] >> 4) & 0x0F), 16));
			sb.append(Character.forDigit((int)(hasharray[i] & 0x0F), 16));
		}
		sb.append(".jnk"/*nonNLS*/);
		String fileName = sb.toString();
		File file = fileForName(fileName);
		if (!file.exists()) {
			RandomAccessFile raf = new RandomAccessFile(file, "rw"/*nonNLS*/);
			raf.write(bytes);
			raf.close();
		}
		return convertFileToURL(file);
	} 
	catch (java.security.NoSuchAlgorithmException e) {
		throw new IOException(e.getMessage());
	}
}
/**
 * Returns a URL using the "file" protocol corresponding to the given File.
 */
static public URL convertFileToURL(File file) {
	try {
		String path = file.getCanonicalPath().replace(java.io.File.separatorChar, '/');
		return new URL("file"/*nonNLS*/, ""/*nonNLS*/, "/"/*nonNLS*/ + path);
	}
	catch (IOException ioe) {
		throw new Error();
	}
}
/**
 * Answer a File to use for the given simple file name.
 */
File fileForName(String fileName) {
	File dir;
	if (fileName.length() >= 1) {
		String dirName = Integer.toHexString((fileName.hashCode() % 255) & 255);
		dir = new File(fDirectory, dirName);
		dir.mkdirs();
	} else {
		dir = fDirectory;
	}
	return new File(dir, fileName);	
}
/**
 * Returns a new anonymous file, but does not allocate it.  
 * Not guaranteed to be free when used since it is unallocated.
 */
synchronized public File getAnonymousFile() {
	File file;
	file = fileForName(getAnonymousFileName());
	while (file.exists()) {
		try {
			Thread.sleep(1);
		} 
		catch (InterruptedException e) {
		}
		file = fileForName(getAnonymousFileName());
	}
	return file;
}
/**
 * Returns a new anonymous file name.  
 * Not guaranteed to be free since its directory is unknown.
 */
synchronized public String getAnonymousFileName() {
	return getAnonymousFileName(System.currentTimeMillis());
}
/**
 * Returns a new anonymous file name based on the given long.  
 * Not guaranteed to be free since its directory is unknown.
 */
synchronized public String getAnonymousFileName(long l) {
	if (l < 0) l = -l;
	StringBuffer sb = new StringBuffer();
	sb.append(Character.forDigit((int)(l % 26 + 10), 36));
	l /= 26;
	while (l != 0) {
		sb.append(Character.forDigit((int)(l % 36), 36));
		l /= 36;
	}
	sb.append(".jnk"/*nonNLS*/);
	return sb.toString();
}
}
