package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import java.io.*;

/**
 * A basic implementation of <code>ICompilationUnit</code>
 * for use in the <code>SourceMapper</code>./
 * @see ICompilationUnit
 */
 
public class BasicCompilationUnit implements ICompilationUnit {
	protected char[] contents;
	protected char[] fileName;
	protected char[] mainTypeName;
public BasicCompilationUnit(char[] contents, String fileName) {
	this.contents = contents;
	this.fileName = fileName.toCharArray();

	int start = fileName.lastIndexOf("/") + 1;
	if (start == 0 || start < fileName.lastIndexOf("\\"))
		start = fileName.lastIndexOf("\\") + 1;

	int end = fileName.lastIndexOf(".");
	if (end == -1)
		end = fileName.length();

	this.mainTypeName = fileName.substring(start, end).toCharArray();
}
public char[] getContents() {
	if (contents != null)
		return contents;   // answer the cached source

	// otherwise retrieve it
	BufferedReader reader = null;
	try {
		File file = new File(new String(fileName));
		reader = new BufferedReader(new FileReader(file));
		int length;
		char[] contents = new char[length = (int) file.length()];
		int len = 0;
		int readSize = 0;
		while ((readSize != -1) && (len != length)) {
			// See PR 1FMS89U
			// We record first the read size. In this case len is the actual read size.
			len += readSize;
			readSize = reader.read(contents, len, length - len);
		}
		reader.close();
		// See PR 1FMS89U
		// Now we need to resize in case the default encoding used more than one byte for each
		// character
		if (len != length)
			System.arraycopy(contents, 0, (contents = new char[len]), 0, len);		
		return contents;
	} catch (FileNotFoundException e) {
	} catch (IOException e) {
		if (reader != null) {
			try {
				reader.close();
			} catch(IOException ioe) {
			}
		}
	};
	return new char[0];
}
public char[] getFileName() {
	return fileName;
}
public char[] getMainTypeName() {
	return mainTypeName;
}
public String toString(){
	return "CompilationUnit: "+new String(fileName);
}
}
