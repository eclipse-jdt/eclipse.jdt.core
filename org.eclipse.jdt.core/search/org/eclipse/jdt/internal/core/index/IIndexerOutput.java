package org.eclipse.jdt.internal.core.index;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.Vector;

/**
 * This class represents the output from an indexer to an index 
 * for a single document.
 */

public interface IIndexerOutput {
	public void addDocument(IDocument document);
	public void addRef(char[] word);
	public void addRef(String word);
}
