package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;

/**
 * An indexOutput is used to write an index into a different object (a File, ...). 
 */
public abstract class IndexOutput {
	/**
	 * Adds a File to the destination.
	 */
	public abstract void addFile(IndexedFile file) throws IOException;
	/**
	 * Adds a word to the destination.
	 */
	public abstract void addWord(WordEntry word) throws IOException;
	/**
	 * Closes the output, releasing the resources it was using.
	 */
	public abstract void close() throws IOException;
	/**
	 * Flushes the output.
	 */
	public abstract void flush() throws IOException;
	/**
	 * Returns the Object the output is writing to. It can be a file, another type of index, ... 
	 */
	public abstract Object getDestination();
	/**
	 * Opens the output, before writing any information.
	 */
	public abstract void open() throws IOException;
}
