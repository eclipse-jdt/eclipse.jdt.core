package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import org.eclipse.core.resources.*;

/**
 * An <code>IFileDocument</code> represents an IFile.
 */

public class IFileDocument extends PropertyDocument {
	protected IFile file;

	// cached contents if needed - only one of them is used at a time
	protected char[] charContents;
	protected byte[] byteContents;
	/**
	 * IFileDocument constructor comment.
	 */
	public IFileDocument(IFile file) {
		this(file, (char[]) null);
	}

	/**
	 * IFileDocument constructor comment.
	 */
	public IFileDocument(IFile file, byte[] byteContents) {
		this.file = file;
		this.byteContents = byteContents;
	}

	/**
	 * IFileDocument constructor comment.
	 */
	public IFileDocument(IFile file, char[] charContents) {
		this.file = file;
		this.charContents = charContents;
	}

	/**
	 * @see IDocument#getByteContent
	 */
	public byte[] getByteContent() throws IOException {
		if (byteContents != null)
			return byteContents;
		return byteContents = Util.getFileByteContent(file.getLocation().toFile());
	}

	/**
	 * @see IDocument#getCharContent
	 */
	public char[] getCharContent() throws IOException {
		if (charContents != null)
			return charContents;
		return charContents = Util.getFileCharContent(file.getLocation().toFile());
	}

	/**
	 * @see IDocument#getName
	 */
	public String getName() {
		return file.getFullPath().toString();
	}

	/**
	 * @see IDocument#getStringContent
	 */
	public String getStringContent() throws java.io.IOException {
		return new String(getCharContent());
	}

	/**
	 * @see IDocument#getType
	 */
	public String getType() {
		String extension = file.getFileExtension();
		if (extension == null)
			return "";
		return extension;
	}

}
