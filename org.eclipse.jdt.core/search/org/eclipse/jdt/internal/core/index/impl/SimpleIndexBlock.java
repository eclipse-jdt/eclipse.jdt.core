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

import java.io.UTFDataFormatException;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Does no compression of words, and uses 4-byte ints for file numbers and number of files.
 */
public class SimpleIndexBlock extends IndexBlock {
	protected int offset= 0;

	public SimpleIndexBlock(int blockSize) {
		super(blockSize);
	}
	/**
	 * @see IndexBlock#addEntry
	 */
	public boolean addEntry(WordEntry entry) {
		char[] word= entry.getWord();
		int n= entry.getNumRefs();
		int sizeEstimate= 2 + word.length * 3 + 4 + n * 4;
		int off= this.offset;
		if (off + sizeEstimate > this.blockSize - 2)
			return false;
		off += field.putUTF(off, word);
		field.putInt4(off, n);
		off += 4;
		for (int i= 0; i < n; ++i) {
			field.putInt4(off, entry.getRef(i));
			off += 4;
		}
		this.offset= off;
		return true;
	}
	public WordEntry findEntry(char[] word) {
		try {
			int off= 0;
			int byteLen;
			while ((byteLen= field.getUInt2(off)) != 0) {
				char[] tempWord= field.getUTF(off);
				off += byteLen + 2;
				if (CharOperation.equals(tempWord, word)) {
					WordEntry entry= new WordEntry(word);
					int n= field.getInt4(off);
					off += 4;
					for (int i= 0; i < n; ++i) {
						int ref= field.getInt4(off);
						off += 4;
						entry.addRef(ref);
					}
					return entry;
				} else {
					int n= field.getInt4(off);
					off += 4 + 4 * n;
				}
			}
			return null;
		} catch (UTFDataFormatException e) {
			return null;
		}
	}
	/**
	 * @see IndexBlock#flush
	 */
	public void flush() {
		if (offset > 0) {
			field.putInt2(offset, 0);
			offset= 0;
		}
	}
	/**
	 * @see IndexBlock#isEmpty
	 */
	public boolean isEmpty() {
		return offset == 0;
	}
	/**
	 * @see IndexBlock#nextEntry
	 */
	public boolean nextEntry(WordEntry entry) {
		try {
			int off= this.offset;
			int byteLen= field.getUInt2(off);
			if (byteLen == 0)
				return false;
			char[] word= field.getUTF(off);
			off += byteLen + 2;
			entry.reset(word);
			int n= field.getInt4(off);
			off += 4;
			for (int i= 0; i < n; ++i) {
				int ref= field.getInt4(off);
				off += 4;
				entry.addRef(ref);
			}
			this.offset= off;
			return true;
		} catch (UTFDataFormatException e) {
			return false;
		}
	}
	/**
	 * @see IndexBlock#reset
	 */
	public void reset() {
		super.reset();
		this.offset= 0;
	}
}
