package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

/**
 * A safe subclass of RandomAccessFile, which ensure that it's closed
 * on finalize.
 */
public class SafeRandomAccessFile extends RandomAccessFile {
	public SafeRandomAccessFile(java.io.File file, String mode) throws java.io.IOException {
		super(file, mode);
	}
	public SafeRandomAccessFile(String name, String mode) throws java.io.IOException {
		super(name, mode);
	}
	protected void finalize() throws IOException {
		close();
	}
}
