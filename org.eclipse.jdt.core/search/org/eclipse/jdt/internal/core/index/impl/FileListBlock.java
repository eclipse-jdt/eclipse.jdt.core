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

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.internal.core.util.Util;

public class FileListBlock extends Block {

	protected int offset= 0;
	protected String prevPath= null;
	protected String[] paths= null;

	public FileListBlock(int blockSize) {
		super(blockSize);
	}
	/**
	 * add the name of the indexedfile to the buffr of the field. 
	 * The name is not the entire name of the indexedfile, but the 
	 * difference between its name and the name of the previous indexedfile ...	
	 */
	public boolean addFile(IndexedFile indexedFile) {
		int currentOffset= this.offset;
		if (isEmpty()) {
			field.putInt4(currentOffset, indexedFile.getFileNumber());
			currentOffset += 4;
		}
		String path= indexedFile.getPath();
		int prefixLen= prevPath == null ? 0 : Util.prefixLength(prevPath, path);
		int sizeEstimate= 2 + 2 + (path.length() - prefixLen) * 3;
		if (currentOffset + sizeEstimate > blockSize - 2)
			return false;
		field.putInt2(currentOffset, prefixLen);
		currentOffset += 2;
		char[] chars= new char[path.length() - prefixLen];
		path.getChars(prefixLen, path.length(), chars, 0);
		currentOffset += field.putUTF(currentOffset, chars);
		this.offset= currentOffset;
		prevPath= path;
		return true;
	}
	public void clear() {
		reset();
		super.clear();
	}
	public void flush() {
		if (offset > 0) {
			field.putInt2(offset, 0);
			field.putInt2(offset + 2, 0);
			offset= 0;
		}
	}
	public IndexedFile getFile(int fileNum) {
		IndexedFile resp= null;
		try {
			String[] currentPaths = getPaths();
			int i= fileNum - field.getInt4(0);
			resp= new IndexedFile(currentPaths[i], fileNum);
		} catch (Exception e) {
			//fileNum too big
		}
		return resp;
	}
	/**
	 * Creates a vector of paths reading the buffer of the field.
	 */
	protected String[] getPaths() throws IOException {
		if (paths == null) {
			ArrayList v= new ArrayList();
			int currentOffset = 4;
			char[] previousPath = null;
			for (;;) {
				int prefixLen= field.getUInt2(currentOffset);
				currentOffset += 2;
				int utfLen= field.getUInt2(currentOffset);
				char[] path= field.getUTF(currentOffset);
				currentOffset += 2 + utfLen;
				if (prefixLen != 0) {
					char[] temp= new char[prefixLen + path.length];
					System.arraycopy(previousPath, 0, temp, 0, prefixLen);
					System.arraycopy(path, 0, temp, prefixLen, path.length);
					path= temp;
				}
				if (path.length == 0)
					break;
				v.add(new String(path));
				previousPath= path;
			}
			paths= new String[v.size()];
			v.toArray(paths);
		}
		return paths;
	}
	public boolean isEmpty() {
		return offset == 0;
	}
	public void reset() {
		offset= 0;
		prevPath= null;
	}
}
