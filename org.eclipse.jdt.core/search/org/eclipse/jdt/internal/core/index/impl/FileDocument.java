package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import org.eclipse.jdt.internal.core.index.*;

/**
 * A <code>FileDocument</code> represents a java.io.File.
 */

public class FileDocument extends PropertyDocument {
	File file;

	public FileDocument(File file) {
		super();
		this.file = file;
	}

	/**
	 * @see IDocument#getByteContent
	 */
	public byte[] getByteContent() throws IOException {
		return Util.getFileByteContent(file);
	}

	/**
	 * @see IDocument#getCharContent
	 */
	public char[] getCharContent() throws IOException {
		return Util.getFileCharContent(file);
	}

	/**
	 * @see IDocument#getName
	 */
	public String getName() {
		return file.getAbsolutePath().replace(
			File.separatorChar,
			IIndexConstants.FILE_SEPARATOR);
	}

	/**
	 * @see IDocument#getStringContent
	 */
	public String getStringContent() throws IOException {
		return new String(getCharContent());
	}

	/**
	 * @see IDocument#getType
	 */
	public String getType() {
		int lastDot = file.getPath().lastIndexOf('.');
		if (lastDot == -1)
			return "";
		return file.getPath().substring(lastDot + 1);
	}

}
