package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.*;

import java.io.*;

/**
 * An indexBlock stores wordEntries.
 */

public abstract class IndexBlock extends Block {

	public IndexBlock(int blockSize) {
		super(blockSize);
	}

	/**
	 * Adds the given wordEntry to the indexBlock.
	 */

	public abstract boolean addEntry(WordEntry entry);
	/**
	 * @see Block#clear
	 */
	public void clear() {
		reset();
		super.clear();
	}

	/**
	 * @see Block#findEntry
	 */
	public WordEntry findEntryMatching(char[] pattern, boolean isCaseSensitive) {
		reset();
		WordEntry entry = new WordEntry();
		while (nextEntry(entry)) {
			if (CharOperation.match(pattern, entry.getWord(), isCaseSensitive)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * @see Block#findEntry
	 */
	public WordEntry findEntryPrefixedBy(char[] word, boolean isCaseSensitive) {
		reset();
		WordEntry entry = new WordEntry();
		while (nextEntry(entry)) {
			if (CharOperation.prefixEquals(entry.getWord(), word, isCaseSensitive)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * @see Block#findEntry
	 */
	public WordEntry findExactEntry(char[] word) {
		reset();
		WordEntry entry = new WordEntry();
		while (nextEntry(entry)) {
			if (CharOperation.equals(entry.getWord(), word)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Returns whether the block is empty or not (if it doesn't contain any wordEntry).
	 */
	public abstract boolean isEmpty();
	/**
	 * Finds the next wordEntry and stores it in the given entry.
	 */

	public abstract boolean nextEntry(WordEntry entry);
	/**
	 * @see Block#findEntry
	 */
	public void reset() {
	}

}
