package org.eclipse.jdt.internal.core.index.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * This interface provides constants used by the search engine.
 */
public interface IIndexConstants {
	/**
	 * The signature of the index file.
	 */
	public static final String SIGNATURE= "INDEX FILE 0.001"; //$NON-NLS-1$
	/**
	 * The signature of the index file.
	 */
	public static final char FILE_SEPARATOR= '/';
	/**
	 * The size of a block for a <code>Block</code>.
	 */
	public static final int BLOCK_SIZE= 8192;
}
