package org.eclipse.jdt.internal.core.jdom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;

import java.io.*;

/**
 * Implements a very simple version of the ICompilationUnit
 *
 * <p>Please do not use outside of jdom.
 */
public class CompilationUnit implements ICompilationUnit {
	protected char[] fContents;
	protected char[] fFileName;
	protected char[] fMainTypeName;
	public CompilationUnit(char[] contents, char[] filename) {
		fContents = contents;
		fFileName = filename;

		String file = new String(filename);
		int start = file.lastIndexOf("/") + 1;
		if (start == 0 || start < file.lastIndexOf("\\"))
			start = file.lastIndexOf("\\") + 1;

		int end = file.lastIndexOf(".");
		if (end == -1)
			end = file.length();

		fMainTypeName = file.substring(start, end).toCharArray();
	}

	public char[] getContents() {
		return fContents;
	}

	public char[] getFileName() {
		return fFileName;
	}

	public char[] getMainTypeName() {
		return fMainTypeName;
	}

	public String toString() {
		return "CompilationUnit[" + new String(fFileName) + "]";
	}

}
