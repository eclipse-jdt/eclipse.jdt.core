package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import java.io.*;

/**
 * An <code>JarFileDocument</code> represents an jar file.
 */

public class JarFileDocument extends PropertyDocument {
	protected IFile file;
	/**
	 * JarFileDocument constructor comment.
	 */
	public JarFileDocument(IFile file) {
		this.file = file;
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
	 */
	public byte[] getByteContent() throws IOException {
		return null;
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getCharContent()
	 */
	public char[] getCharContent() throws IOException {
		return null;
	}
	public File getFile() {
		return file.getLocation().toFile();
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getName()
	 */
	public String getName() {
		return file.getFullPath().toString();
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
	 */
	public String getStringContent() throws java.io.IOException {
		return null;
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getType()
	 */
	public String getType() {
		String extension= file.getFileExtension();
		if (extension == null)
			return ""; //$NON-NLS-1$
		return extension;
	}
}
