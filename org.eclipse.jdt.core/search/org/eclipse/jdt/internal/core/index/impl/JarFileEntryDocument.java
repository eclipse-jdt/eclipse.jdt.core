package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Path;

import java.io.*;
import java.util.zip.*;

/**
 * An <code>JarFileEntryDocument</code> represents an jar file.
 */

public class JarFileEntryDocument extends PropertyDocument {
	protected ZipEntry zipEntry;
	protected byte[] byteContents;
	protected Path zipFilePath;
	public static final String JAR_FILE_ENTRY_SEPARATOR = "|";
	/**
	 * JarFileEntryDocument constructor comment.
	 */
	public JarFileEntryDocument(
		ZipEntry entry,
		byte[] contents,
		Path zipFilePath) {
		this.zipEntry = entry;
		this.byteContents = contents;
		this.zipFilePath = zipFilePath;
	}

	/**
	 * This API always return null for a JarFileDocument
	 * @see IDocument#getByteContent
	 */
	public byte[] getByteContent() throws IOException {
		return this.byteContents;
	}

	/**
	 * This API always return null for a JarFileDocument
	 * @see IDocument#getByteContent
	 */
	public char[] getCharContent() throws IOException {
		return null;
	}

	/**
	 * @see IDocument#getName
	 */
	public String getName() {
		return zipFilePath + JAR_FILE_ENTRY_SEPARATOR + zipEntry.getName();
	}

	/**
	 * This API always return null for a JarFileDocument
	 * @see IDocument#getByteContent
	 */
	public String getStringContent() throws java.io.IOException {
		return null;
	}

	/**
	 * @see IDocument#getType
	 */
	public String getType() {
		return "class";
	}

	public void setBytes(byte[] byteContents) {
		this.byteContents = byteContents;
	}

}
