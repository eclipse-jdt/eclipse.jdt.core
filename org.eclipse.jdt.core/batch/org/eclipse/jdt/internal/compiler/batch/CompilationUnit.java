package org.eclipse.jdt.internal.compiler.batch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

import org.eclipse.jdt.internal.compiler.env.*;

public class CompilationUnit implements ICompilationUnit {
	public char[] contents;
	public char[] fileName;
	public char[] mainTypeName;
public CompilationUnit(char[] contents, String fileName) {
	this.contents = contents;
	this.fileName = fileName.toCharArray();

	int start = fileName.lastIndexOf("/"/*nonNLS*/) + 1;
	if (start == 0 || start < fileName.lastIndexOf("\\"/*nonNLS*/))
		start = fileName.lastIndexOf("\\"/*nonNLS*/) + 1;

	int end = fileName.lastIndexOf("."/*nonNLS*/);
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
public String toString() {
	return "CompilationUnit["/*nonNLS*/ + new String(fileName) + "]"/*nonNLS*/; 
}
}
