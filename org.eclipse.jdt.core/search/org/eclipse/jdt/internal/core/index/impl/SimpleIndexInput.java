package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import org.eclipse.jdt.internal.core.index.*;
import java.util.Vector;

/**
 * A simpleIndexInput is an input on an in memory Index. 
 */

public class SimpleIndexInput extends IndexInput {
	protected char[][] sortedWords;
	protected IndexedFile currentFile;
	protected IndexedFile[] sortedFiles;
	protected InMemoryIndex index;

	public SimpleIndexInput(InMemoryIndex index) {
		super();
		this.index = index;
	}

	/**
	 * @see IndexInput#clearCache
	 */
	public void clearCache() {
	}

	/**
	 * @see IndexInput#close
	 */
	public void close() throws IOException {
		sortedFiles = null;
	}

	/**
	 * @see IndexInput#getCurrentFile
	 */
	public IndexedFile getCurrentFile() throws IOException {
		if (!hasMoreFiles())
			return null;
		return currentFile;
	}

	/**
	 * @see IndexInput#getIndexedFile
	 */
	public IndexedFile getIndexedFile(int fileNum) throws IOException {
		for (int i = 0; i < sortedFiles.length; i++)
			if (sortedFiles[i].getFileNumber() == fileNum)
				return sortedFiles[i];
		return null;
	}

	/**
	 * @see IndexInput#getIndexedFile
	 */
	public IndexedFile getIndexedFile(IDocument document) throws IOException {
		for (int i = index.getNumFiles(); i >= 1; i--) {
			IndexedFile file = getIndexedFile(i);
			String path = file.getPath();
			String name = document.getName();
			if (name.equals(path))
				return file;
		}
		return null;
	}

	/**
	 * @see IndexInput#getNumFiles
	 */
	public int getNumFiles() {
		return index.getNumFiles();
	}

	/**
	 * @see IndexInput#getNumWords
	 */
	public int getNumWords() {
		return sortedWords.length;
	}

	/**
	 * @see IndexInput#getSource
	 */
	public Object getSource() {
		return index;
	}

	/**
	 * @see IndexInput#init
	 */
	public void init() {
		index.init();

	}

	/**
	 * @see IndexInput#moveToNextFile
	 */
	public void moveToNextFile() throws IOException {
		filePosition++;
		if (!hasMoreFiles()) {
			return;
		}
		currentFile = sortedFiles[filePosition - 1];
	}

	/**
	 * @see IndexInput#moveToNextWordEntry
	 */
	public void moveToNextWordEntry() throws IOException {
		wordPosition++;
		if (!hasMoreWords()) {
			return;
		}
		char[] word = sortedWords[wordPosition - 1];
		currentWordEntry = (WordEntry) index.words.get(word);
	}

	/**
	 * @see IndexInput#open
	 */
	public void open() throws IOException {
		sortedWords = index.getSortedWords();
		sortedFiles = index.getSortedFiles();
		filePosition = 1;
		wordPosition = 1;
		setFirstFile();
		setFirstWord();
	}

	/**
	 * @see IndexInput#query
	 */
	public IQueryResult[] query(String word) throws IOException {
		char[] wordChar = word.toCharArray();
		WordEntry wordEntry = index.getWordEntry(wordChar);
		int[] fileNums = wordEntry.getRefs();
		IQueryResult[] files = new IQueryResult[fileNums.length];
		for (int i = 0; i < files.length; i++)
			files[i] = getIndexedFile(fileNums[i]);
		return files;
	}

	public IEntryResult[] queryEntriesPrefixedBy(char[] prefix)
		throws IOException {
		return null;
	}

	public IQueryResult[] queryFilesReferringToPrefix(char[] prefix)
		throws IOException {
		return null;
	}

	/**
	 * @see IndexInput#query
	 */
	public IQueryResult[] queryInDocumentNames(String word) throws IOException {
		setFirstFile();
		Vector matches = new Vector();
		while (hasMoreFiles()) {
			IndexedFile file = getCurrentFile();
			if (file.getPath().indexOf(word) != -1)
				matches.addElement(file.getPath());
			moveToNextFile();
		}
		IQueryResult[] match = new IQueryResult[matches.size()];
		matches.copyInto(match);
		return match;
	}

	/**
	 * @see IndexInput#setFirstFile
	 */
	protected void setFirstFile() throws IOException {
		filePosition = 1;
		if (sortedFiles.length > 0) {
			currentFile = sortedFiles[0];
		}
	}

	/**
	 * @see IndexInput#setFirstWord
	 */
	protected void setFirstWord() throws IOException {
		wordPosition = 1;
		if (sortedWords.length > 0) {
			char[] word = sortedWords[0];
			currentWordEntry = (WordEntry) index.words.get(word);
		}
	}

}
