/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index.impl;

import java.util.ArrayList;

import org.eclipse.jdt.core.search.SearchDocument;

/**
 * A simpleIndexInput is an input on an in memory Index. 
 */

public class SimpleIndexInput extends IndexInput {
	protected WordEntry[] sortedWordEntries;
	protected IndexedFile currentFile;
	protected IndexedFile[] sortedFiles;
	protected InMemoryIndex index;

	public SimpleIndexInput(InMemoryIndex index) {
		super();
		this.index= index;
	}
	/**
	 * @see IndexInput#clearCache()
	 */
	public void clearCache() {
		// implements abstract method
	}
	/**
	 * @see IndexInput#close()
	 */
	public void close() {
		sortedFiles= null;
	}
	/**
	 * @see IndexInput#getCurrentFile()
	 */
	public IndexedFile getCurrentFile() {
		if (!hasMoreFiles())
			return null;
		return currentFile;
	}
	/**
	 * @see IndexInput#getIndexedFile(int)
	 */
	public IndexedFile getIndexedFile(int fileNum) {
		for (int i= 0; i < sortedFiles.length; i++)
			if (sortedFiles[i].getFileNumber() == fileNum)
				return sortedFiles[i];
		return null;
	}
	/**
	 * @see IndexInput#getIndexedFile(IDocument)
	 */
	public IndexedFile getIndexedFile(SearchDocument document) {
		String name= document.getPath();
		for (int i= index.getNumFiles(); i >= 1; i--) {
			IndexedFile file= getIndexedFile(i);
			if (name.equals(file.getPath()))
				return file;
		}
		return null;
	}
	/**
	 * @see IndexInput#getNumFiles()
	 */
	public int getNumFiles() {
		return index.getNumFiles();
	}
	/**
	 * @see IndexInput#getNumWords()
	 */
	public int getNumWords() {
		return sortedWordEntries.length;
	}
	/**
	 * @see IndexInput#getSource()
	 */
	public Object getSource() {
		return index;
	}
	public void init() {
		index.init();

	}
	/**
	 * @see IndexInput#moveToNextFile()
	 */
	public void moveToNextFile() {
		filePosition++;
		if (!hasMoreFiles()) {
			return;
		}
		currentFile= sortedFiles[filePosition - 1];
	}
	/**
	 * @see IndexInput#moveToNextWordEntry()
	 */
	public void moveToNextWordEntry() /* throws IOException */ {
		wordPosition++;
		if (hasMoreWords())
			currentWordEntry= sortedWordEntries[wordPosition - 1];
	}
	/**
	 * @see IndexInput#open()
	 */
	public void open() {
		sortedWordEntries= index.getSortedWordEntries();
		sortedFiles= index.getSortedFiles();
		filePosition= 1;
		wordPosition= 1;
		setFirstFile();
		setFirstWord();
	}
	/**
	 * @see IndexInput#query(String)
	 */
	public String[] query(String word) {
		char[] wordChar= word.toCharArray();
		WordEntry wordEntry= index.getWordEntry(wordChar);
		int[] fileNums= wordEntry.getRefs();
		String[] paths= new String[fileNums.length];
		for (int i= 0; i < paths.length; i++)
			paths[i]= getIndexedFile(fileNums[i]).getPath();
		return paths;
	}
	public EntryResult[] queryEntries(char[] pattern, int matchRule) {
		return null;
	}
	public EntryResult[] queryEntriesPrefixedBy(char[] prefix) {
		return null;
	}
	public String[] queryFilesReferringToPrefix(char[] prefix) {
			return null;
	}
	/**
	 * @see IndexInput#queryInDocumentNames(String)
	 */
	public String[] queryInDocumentNames(String word) {
		setFirstFile();
		ArrayList matches= new ArrayList();
		while (hasMoreFiles()) {
			IndexedFile file= getCurrentFile();
			if (file.getPath().indexOf(word) != -1)
				matches.add(file.getPath());
			moveToNextFile();
		}
		String[] match= new String[matches.size()];
		matches.toArray(match);
		return match;
	}
	/**
	 * @see IndexInput#setFirstFile()
	 */
	protected void setFirstFile() {
		filePosition= 1;
		if (sortedFiles.length > 0) {
			currentFile= sortedFiles[0];
		}
	}
	/**
	 * @see IndexInput#setFirstWord()
	 */
	protected void setFirstWord() {
		wordPosition= 1;
		if (sortedWordEntries.length > 0)
			currentWordEntry= sortedWordEntries[0];
	}
}
