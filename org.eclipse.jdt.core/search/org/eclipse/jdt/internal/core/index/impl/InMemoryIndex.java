package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import org.eclipse.jdt.internal.core.index.*;

/**
 * This index stores the document names in an <code>ObjectVector</code>, and the words in
 * an <code>HashtableOfObjects</code>.
 */

public class InMemoryIndex {

	/**
	 * hashtable of WordEntrys = words+numbers of the files they appear in.
	 */
	protected HashtableOfObject words = new HashtableOfObject(1023);

	/**
	 * Vector of IndexedFiles = file name + a unique number.
	 */
	protected ObjectVector files = new ObjectVector();

	/**
	 * Number of references in the index (not the number of actual words).
	 */
	protected int wordCount = 0;

	/**
	 * Size of the index.
	 */
	protected long footprint = 0;

	protected long indexFileSize;
	private char[][] sortedWords;
	private IndexedFile[] sortedFiles;
	public InMemoryIndex() {
		super();
	}

	/**
	 * @see IIndex#addFile
	 */
	public IndexedFile addDocument(IDocument document) {
		IndexedFile indexedFile = new IndexedFile(document, this.files.size + 1);
		this.files.add(indexedFile);
		this.footprint += indexedFile.footprint() + 4;
		this.sortedFiles = null;
		return indexedFile;
	}

	/**
	 * Adds the references of the word to the index (reference = number of the file the word belongs to).
	 */
	protected void addRef(char[] word, int[] references) {
		int size = references.length;
		int i = 0;
		while (i < size) {
			if (references[i] != 0)
				addRef(word, references[i]);
			i++;
		}
	}

	/**
	 * Looks if the word already exists in the index and add the fileNum to this word.
	 * If the word does not exist, it adds it in the index.
	 */
	protected void addRef(char[] word, int fileNum) {
		word = preprocessWord(word);
		WordEntry entry = (WordEntry) this.words.get(word);
		if (entry == null) {
			entry = new WordEntry(word);
			entry.addRef(fileNum);
			this.words.put(word, entry);
			this.sortedWords = null;
			this.footprint += entry.footprint();
		} else {
			this.footprint += entry.addRef(fileNum);
		}
		++this.wordCount;
	}

	/**
	 * @see IIndex#addRef
	 */
	public void addRef(IndexedFile indexedFile, char[] word) {
		addRef(word, indexedFile.getFileNumber());
	}

	/**
	 * @see IIndex#addRef
	 */
	public void addRef(IndexedFile indexedFile, String word) {
		addRef(word.toCharArray(), indexedFile.getFileNumber());
	}

	/**
	 * Returns the footprint of the index.
	 */

	public long getFootprint() {
		return this.footprint;
	}

	/**
	 * Returns the indexed file with the given path, or null if such file does not exist.
	 */
	public IndexedFile getIndexedFile(String path) {
		for (int i = files.size; i > 0; i--) {
			IndexedFile file = (IndexedFile) files.elementAt(i - 1);
			if (file.getPath().equals(path))
				return file;
		}
		return null;
	}

	/**
	 * @see IIndex#getNumFiles
	 */
	public int getNumFiles() {
		return files.size;
	}

	/**
	 * @see IIndex#getNumWords
	 */
	public int getNumWords() {
		return words.elementSize;
	}

	/**
	 * Returns the words contained in the hashtable of words, sorted by alphabetical order.
	 */
	protected IndexedFile[] getSortedFiles() {
		if (this.sortedFiles == null) {
			IndexedFile[] indexedfiles = new IndexedFile[files.size];
			for (int i = 0; i < indexedfiles.length; i++)
				indexedfiles[i] = (IndexedFile) files.elementAt(i);
			Util.sort(indexedfiles);
			this.sortedFiles = indexedfiles;
		}
		return this.sortedFiles;
	}

	/**
	 * Returns the words contained in the hashtable of words, sorted by alphabetical order.
	 */
	protected char[][] getSortedWords() {
		if (this.sortedWords == null) {
			char[][] words = new char[this.words.size()][];
			int numWords = 0;
			char[][] keys = this.words.keyTable;
			for (int i = keys.length; i-- > 0;) {
				if (keys[i] != null) {
					words[numWords++] = keys[i];
				}
			}
			Util.sort(words);
			this.sortedWords = words;
		}
		return this.sortedWords;
	}

	/**
	 * Returns the word entry corresponding to the given word.
	 */
	protected WordEntry getWordEntry(char[] word) {
		return (WordEntry) words.get(word);
	}

	/**
	 * Initialises the fields of the index
	 */
	public void init() {
		words = new HashtableOfObject(1023);
		files = new ObjectVector();
		wordCount = 0;
		footprint = 0;
		sortedWords = null;
		sortedFiles = null;
	}

	protected char[] preprocessWord(char[] word) {
		if (word.length > 255) {
			System.arraycopy(word, 0, word = new char[255], 0, 255);
		}
		return word;
	}

	/**
	 * Saves the index in the given file.
	 * Structure of the saved Index :
	 *   - IndexedFiles in sorted order.
	 *		+ example: 
	 *			"c:/com/Test.java 1"
	 *			"c:/com/UI.java 2"
	 *   - References with the words in sorted order
	 *		+ example: 
	 *			"classDecl/Test 1"
	 *			"classDecl/UI 2"
	 *			"ref/String 1 2"
	 */

	public void save(File file) throws IOException {
		BlocksIndexOutput output = new BlocksIndexOutput(file);
		save(output);
	}

	/**
	 * Saves the index in the given IndexOutput.
	 * Structure of the saved Index :
	 *   - IndexedFiles in sorted order.
	 *		+ example: 
	 *			"c:/com/Test.java 1"
	 *			"c:/com/UI.java 2"
	 *   - References with the words in sorted order
	 *		+ example: 
	 *			"classDecl/Test 1"
	 *			"classDecl/UI 2"
	 *			"ref/String 1 2"
	 */

	protected void save(IndexOutput output) throws IOException {
		boolean ok = false;
		char[][] sortedWords = getSortedWords();
		try {
			output.open();
			int numFiles = this.files.size;
			for (int i = 0; i < numFiles; ++i) {
				IndexedFile indexedFile = (IndexedFile) this.files.elementAt(i);
				output.addFile(indexedFile);
			}
			int numWords = sortedWords.length;
			for (int i = 0; i < numWords; ++i) {
				char[] word = sortedWords[i];
				WordEntry entry = (WordEntry) this.words.get(word);
				output.addWord(entry);
			}
			output.flush();
			output.close();
			ok = true;
		} finally {
			if (!ok)
				if (output != null)
					output.close();
		}
	}

}
