/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.io.*;

import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfIntValues;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public class DiskIndex {

String fileName;

private int headerInfoOffset;
private int numberOfChunks;
private int sizeOfLastChunk;
private int[] chunkOffsets;
private int documentReferenceSize; // 1, 2 or more bytes... depends on # of document names
private int startOfCategoryTables;
private HashtableOfIntValues categoryOffsets;

private int cacheUserCount;
private String[][] cachedChunks; // decompressed chunks of document names
private HashtableOfObject categoryTables; // category name -> HashtableOfObject(words -> int[] of document #'s) or offset if not read yet
private char[] cachedCategoryName;

public static final String SIGNATURE= "INDEX VERSION 1.106"; //$NON-NLS-1$
public static boolean DEBUG = false;

private static final int RE_INDEXED = -1;
private static final int DELETED = -2;

private static final int CHUNK_SIZE = 100;

class IntList {

int size;
int[] elements;

IntList(int[] elements) {
	this.elements = elements;
	this.size = elements.length;
}
void add(int newElement) {
	if (this.size == this.elements.length) {
		int newSize = this.size * 3;
		if (newSize < 7) newSize = 7;
		System.arraycopy(this.elements, 0, this.elements = new int[newSize], 0, this.size);
	}
	this.elements[this.size++] = newElement;
}
int[] asArray() {
	int[] result = new int[this.size];
	System.arraycopy(this.elements, 0, result, 0, this.size);
	return result;
}	
}


DiskIndex(String fileName) {
	this.fileName = fileName;

	// clear cached items
	this.headerInfoOffset = -1;
	this.numberOfChunks = -1;
	this.sizeOfLastChunk = -1;
	this.chunkOffsets = null;
	this.documentReferenceSize = -1;
	this.cacheUserCount = -1;
	this.cachedChunks = null;
	this.categoryTables = null;
	this.cachedCategoryName = null;
	this.categoryOffsets = null;
}
SimpleSet addDocumentNames(String substring, MemoryIndex memoryIndex) throws IOException {
	// must skip over documents which have been added/changed/deleted in the memory index
	String[] docNames = readAllDocumentNames();
	SimpleSet results = new SimpleSet(docNames.length);
	if (substring == null) {
		if (memoryIndex == null) {
			for (int i = 0, l = docNames.length; i < l; i++)
				results.add(docNames[i]);
		} else {
			SimpleLookupTable docsToRefs = memoryIndex.docsToReferences;
			for (int i = 0, l = docNames.length; i < l; i++) {
				String docName = docNames[i];
				if (!docsToRefs.containsKey(docName))
					results.add(docName);
			}
		}
	} else {
		if (memoryIndex == null) {
			for (int i = 0, l = docNames.length; i < l; i++)
				if (docNames[i].startsWith(substring, 0))
					results.add(docNames[i]);
		} else {
			SimpleLookupTable docsToRefs = memoryIndex.docsToReferences;
			for (int i = 0, l = docNames.length; i < l; i++) {
				String docName = docNames[i];
				if (docName.startsWith(substring, 0) && !docsToRefs.containsKey(docName))
					results.add(docName);
			}
		}
	}
	return results;
}
private HashtableOfObject addQueryResult(HashtableOfObject results, char[] word, HashtableOfObject wordsToDocNumbers, MemoryIndex memoryIndex) throws IOException {
	// must skip over documents which have been added/changed/deleted in the memory index
	if (results == null)
		results = new HashtableOfObject(13);
	EntryResult result = (EntryResult) results.get(word);
	if (memoryIndex == null) {
		if (result == null)
			results.put(word, new EntryResult(word, wordsToDocNumbers));
		else
			result.addDocumentTable(wordsToDocNumbers);
	} else {
		SimpleLookupTable docsToRefs = memoryIndex.docsToReferences;
		if (result == null)
			result = new EntryResult(word, null);
		int[] docNumbers = readDocumentNumbers(wordsToDocNumbers.get(word));
		for (int i = 0, l = docNumbers.length; i < l; i++) {
			String docName = readDocumentName(docNumbers[i]);
			if (!docsToRefs.containsKey(docName))
				result.addDocumentName(docName);
		}
		if (!result.isEmpty())
			results.put(word, result);
	}
	return results;
}
HashtableOfObject addQueryResults(char[][] categories, char[] key, int matchRule, MemoryIndex memoryIndex) throws IOException {
	// assumes sender has called startQuery() & will call stopQuery() when finished
	if (this.categoryOffsets == null) return null; // file is empty

	HashtableOfObject results = null; // initialized if needed
	if (key == null) {
		for (int i = 0, l = categories.length; i < l; i++) {
			HashtableOfObject wordsToDocNumbers = readCategoryTable(categories[i], true); // cache if key is null since its a definite match
			if (wordsToDocNumbers != null) {
				char[][] words = wordsToDocNumbers.keyTable;
				if (results == null)
					results = new HashtableOfObject(wordsToDocNumbers.elementSize);
				for (int j = 0, m = words.length; j < m; j++)
					if (words[j] != null)
						results = addQueryResult(results, words[j], wordsToDocNumbers, memoryIndex);
			}
		}
		if (results != null && this.cachedChunks == null)
			cacheDocumentNames();
	} else if (matchRule == SearchPattern.R_EXACT_MATCH + SearchPattern.R_CASE_SENSITIVE) {
		for (int i = 0, l = categories.length; i < l; i++) {
			HashtableOfObject wordsToDocNumbers = readCategoryTable(categories[i], false);
			if (wordsToDocNumbers != null && wordsToDocNumbers.containsKey(key))
				results = addQueryResult(results, key, wordsToDocNumbers, memoryIndex);
		}
	} else {
		for (int i = 0, l = categories.length; i < l; i++) {
			HashtableOfObject wordsToDocNumbers = readCategoryTable(categories[i], false);
			if (wordsToDocNumbers != null) {
				char[][] words = wordsToDocNumbers.keyTable;
				for (int j = 0, m = words.length; j < m; j++) {
					char[] word = words[j];
					if (word != null && Index.isMatch(key, word, matchRule))
						results = addQueryResult(results, word, wordsToDocNumbers, memoryIndex);
				}
			}
		}
	}

	if (results == null) return null;
	return results;
}
private void cacheDocumentNames() throws IOException {
	// will need all document names so get them now
	this.cachedChunks = new String[this.numberOfChunks][];
	DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(getIndexFile()), this.numberOfChunks > 5 ? 4096 : 2048));
	try {
		stream.skip(this.chunkOffsets[0]);
		for (int i = 0; i < this.numberOfChunks; i++) {
			int size = i == this.numberOfChunks - 1 ? this.sizeOfLastChunk : CHUNK_SIZE;
			readChunk(this.cachedChunks[i] = new String[size], stream, 0, size);
		}
	} finally {
		stream.close();
	}
}
private String[] computeDocumentNames(String[] onDiskNames, int[] positions, SimpleLookupTable indexedDocuments, MemoryIndex memoryIndex) {
	int onDiskLength = onDiskNames.length;
	Object[] docNames = memoryIndex.docsToReferences.keyTable;
	Object[] referenceTables = memoryIndex.docsToReferences.valueTable;
	if (onDiskLength == 0) {
		// disk index was empty, so add every indexed document
		for (int i = 0, l = referenceTables.length; i < l; i++)
			if (referenceTables[i] != null)
				indexedDocuments.put(docNames[i], null); // remember each new document

		String[] newDocNames = new String[indexedDocuments.elementSize];
		int count = 0;
		Object[] added = indexedDocuments.keyTable;
		for (int i = 0, l = added.length; i < l; i++)
			if (added[i] != null)
				newDocNames[count++] = (String) added[i];
		Util.sort(newDocNames);
		for (int i = 0, l = newDocNames.length; i < l; i++)
			indexedDocuments.put(newDocNames[i], new Integer(i));
		return newDocNames;
	}

	// initialize positions as if each document will remain in the same position
	for (int i = 0; i < onDiskLength; i++)
		positions[i] = i;

	// find out if the memory index has any new or deleted documents, if not then the names & positions are the same
	int numDeletedDocNames = 0;
	int numReindexedDocNames = 0;
	nextPath : for (int i = 0, l = docNames.length; i < l; i++) {
		String docName = (String) docNames[i];
		if (docName != null) {
			for (int j = 0; j < onDiskLength; j++) {
				if (docName.equals(onDiskNames[j])) {
					if (referenceTables[i] == null) {
						positions[j] = DELETED;
						numDeletedDocNames++;
					} else {
						positions[j] = RE_INDEXED;
						numReindexedDocNames++;
					}
					continue nextPath;
				}
			}
			if (referenceTables[i] != null)
				indexedDocuments.put(docName, null); // remember each new document, skip deleted documents which were never saved
		}
	}

	String[] newDocNames = onDiskNames;
	if (numDeletedDocNames > 0 || indexedDocuments.elementSize > 0) {
		// some new documents have been added or some old ones deleted
		newDocNames = new String[onDiskLength + indexedDocuments.elementSize - numDeletedDocNames];
		int count = 0;
		for (int i = 0; i < onDiskLength; i++)
			if (positions[i] >= RE_INDEXED)
				newDocNames[count++] = onDiskNames[i]; // keep each unchanged document
		Object[] added = indexedDocuments.keyTable;
		for (int i = 0, l = added.length; i < l; i++)
			if (added[i] != null)
				newDocNames[count++] = (String) added[i]; // add each new document
		Util.sort(newDocNames);
		for (int i = 0, l = newDocNames.length; i < l; i++)
			if (indexedDocuments.containsKey(newDocNames[i]))
				indexedDocuments.put(newDocNames[i], new Integer(i)); // remember the position for each new document
	}

	// need to be able to look up an old position (ref# from a ref[]) and map it to its new position
	// if its old position == DELETED then its forgotton
	// if its old position == ReINDEXED then its also forgotten but its new position is needed to map references
	int count = -1;
	for (int i = 0; i < onDiskLength;) {
		switch(positions[i]) {
			case DELETED :
				i++; // skip over deleted... references are forgotten
				break;
			case RE_INDEXED :
				String newName = newDocNames[++count];
				if (newName.equals(onDiskNames[i])) {
					indexedDocuments.put(newName, new Integer(count)); // the reindexed docName that was at position i is now at position count
					i++;
				}
				break;
			default :
				if (newDocNames[++count].equals(onDiskNames[i]))
					positions[i++] = count; // the unchanged docName that was at position i is now at position count
		}
	}
	return newDocNames;
}
private void copyQueryResults(HashtableOfObject categoryToWords, int newPosition) {
	char[][] categoryNames = categoryToWords.keyTable;
	Object[] wordSets = categoryToWords.valueTable;
	for (int i = 0, l = categoryNames.length; i < l; i++) {
		char[] categoryName = categoryNames[i];
		if (categoryName != null) {
			SimpleWordSet wordSet = (SimpleWordSet) wordSets[i];
			HashtableOfObject wordsToDocs = (HashtableOfObject) this.categoryTables.get(categoryName);
			if (wordsToDocs == null)
				this.categoryTables.put(categoryName, wordsToDocs = new HashtableOfObject(wordSet.elementSize));

			char[][] words = wordSet.words;
			for (int j = 0, m = words.length; j < m; j++) {
				char[] word = words[j];
				if (word != null) {
					Object o = wordsToDocs.get(word);
					if (o == null) {
						wordsToDocs.put(word, new int[] {newPosition});
					} else if (o instanceof IntList) {
						((IntList) o).add(newPosition);
					} else {
						IntList list = new IntList((int[]) o);
						list.add(newPosition);
						wordsToDocs.put(word, list);
					}
				}
			}
		}
	}
}
File getIndexFile() {
	if (this.fileName == null) return null;

	return new File(this.fileName);
}
void initialize(boolean reuseExistingFile) throws IOException {
	File indexFile = getIndexFile();
	if (indexFile.exists()) {
		if (reuseExistingFile) {
			RandomAccessFile file = new RandomAccessFile(this.fileName, "r"); //$NON-NLS-1$
			try {
				String signature = file.readUTF();
				if (!signature.equals(SIGNATURE))
					throw new IOException(Messages.exception_wrongFormat); 

				this.headerInfoOffset = file.readInt();
				if (this.headerInfoOffset > 0) // file is empty if its not set
					readHeaderInfo(file);
			} finally {
				file.close();
			}
			return;
		}
		if (!indexFile.delete()) {
			if (DEBUG)
				System.out.println("initialize - Failed to delete index " + this.fileName); //$NON-NLS-1$
			throw new IOException("Failed to delete index " + this.fileName); //$NON-NLS-1$
		}
	}
	if (indexFile.createNewFile()) {
		RandomAccessFile file = new RandomAccessFile(this.fileName, "rw"); //$NON-NLS-1$
		try {
			file.writeUTF(SIGNATURE);
			file.writeInt(-1); // file is empty
		} finally {
			file.close();
		}
	} else {
		if (DEBUG)
			System.out.println("initialize - Failed to create new index " + this.fileName); //$NON-NLS-1$
		throw new IOException("Failed to create new index " + this.fileName); //$NON-NLS-1$
	}
}
private void initializeFrom(DiskIndex diskIndex, File newIndexFile) throws IOException {
	if (newIndexFile.exists() && !newIndexFile.delete()) { // delete the temporary index file
		if (DEBUG)
			System.out.println("initializeFrom - Failed to delete temp index " + this.fileName); //$NON-NLS-1$
	} else if (!newIndexFile.createNewFile()) {
		if (DEBUG)
			System.out.println("initializeFrom - Failed to create temp index " + this.fileName); //$NON-NLS-1$
		throw new IOException("Failed to create temp index " + this.fileName); //$NON-NLS-1$
	}

	int size = diskIndex.categoryOffsets == null ? 8 : diskIndex.categoryOffsets.elementSize;
	this.categoryOffsets = new HashtableOfIntValues(size);
	this.categoryTables = new HashtableOfObject(size);
}
private void mergeCategories(DiskIndex onDisk, int[] positions, DataOutputStream stream) throws IOException {
	// at this point, this.categoryTables contains the names -> wordsToDocs added in copyQueryResults()
	char[][] oldNames = onDisk.categoryOffsets.keyTable;
	for (int i = 0, l = oldNames.length; i < l; i++) {
		char[] oldName = oldNames[i];
		if (oldName != null && !this.categoryTables.containsKey(oldName))
			this.categoryTables.put(oldName, null);
	}

	char[][] categoryNames = this.categoryTables.keyTable;
	for (int i = 0, l = categoryNames.length; i < l; i++)
		if (categoryNames[i] != null)
			mergeCategory(categoryNames[i], onDisk, positions, stream);
	this.categoryTables = null;
}
private void mergeCategory(char[] categoryName, DiskIndex onDisk, int[] positions, DataOutputStream stream) throws IOException {
	HashtableOfObject wordsToDocs = (HashtableOfObject) this.categoryTables.get(categoryName);
	if (wordsToDocs == null)
		wordsToDocs = new HashtableOfObject(3);

	HashtableOfObject oldWordsToDocs = onDisk.readCategoryTable(categoryName, true);
	if (oldWordsToDocs != null) {
		char[][] oldWords = oldWordsToDocs.keyTable;
		Object[] oldArrayOffsets = oldWordsToDocs.valueTable;
		nextWord: for (int i = 0, l = oldWords.length; i < l; i++) {
			char[] oldWord = oldWords[i];
			if (oldWord != null) {
				int[] oldDocNumbers = (int[]) oldArrayOffsets[i];
				int length = oldDocNumbers.length;
				int[] mappedNumbers = new int[length];
				int count = 0;
				for (int j = 0; j < length; j++) {
					int pos = positions[oldDocNumbers[j]];
					if (pos > RE_INDEXED) // forget any reference to a document which was deleted or re_indexed
						mappedNumbers[count++] = pos;
				}
				if (count < length) {
					if (count == 0) continue nextWord; // skip words which no longer have any references
					System.arraycopy(mappedNumbers, 0, mappedNumbers = new int[count], 0, count);
				}

				Object o = wordsToDocs.get(oldWord);
				if (o == null) {
					wordsToDocs.put(oldWord, mappedNumbers);
				} else {
					IntList list = null;
					if (o instanceof IntList) {
						list = (IntList) o;
					} else {
						list = new IntList((int[]) o);
						wordsToDocs.put(oldWord, list);
					}
					for (int j = 0; j < count; j++)
						list.add(mappedNumbers[j]);
				}
			}
		}
		onDisk.categoryTables.put(categoryName, null); // flush cached table
	}
	writeCategoryTable(categoryName, wordsToDocs, stream);
}
DiskIndex mergeWith(MemoryIndex memoryIndex) throws IOException {
 	// assume write lock is held
	// compute & write out new docNames
	String[] docNames = readAllDocumentNames();
	int previousLength = docNames.length;
	int[] positions = new int[previousLength]; // keeps track of the position of each document in the new sorted docNames
	SimpleLookupTable indexedDocuments = new SimpleLookupTable(3); // for each new/changed document in the memoryIndex
	docNames = computeDocumentNames(docNames, positions, indexedDocuments, memoryIndex);
	if (docNames.length == 0) {
		if (previousLength == 0) return this; // nothing to do... memory index contained deleted documents that had never been saved

		// index is now empty since all the saved documents were removed
		DiskIndex newDiskIndex = new DiskIndex(this.fileName);
		newDiskIndex.initialize(false);
		return newDiskIndex;
	}

	DiskIndex newDiskIndex = new DiskIndex(this.fileName + ".tmp"); //$NON-NLS-1$
	File newIndexFile = newDiskIndex.getIndexFile();
	try {
		newDiskIndex.initializeFrom(this, newIndexFile);
		DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newIndexFile, false), 2048));
		int offsetToHeader = -1;
		try {
			newDiskIndex.writeAllDocumentNames(docNames, stream);
			docNames = null; // free up the space

			// add each new/changed doc to empty category tables using its new position #
			if (indexedDocuments.elementSize > 0) {
				Object[] names = indexedDocuments.keyTable;
				Object[] integerPositions = indexedDocuments.valueTable;
				for (int i = 0, l = names.length; i < l; i++)
					if (names[i] != null)
						newDiskIndex.copyQueryResults(
							(HashtableOfObject) memoryIndex.docsToReferences.get(names[i]),
							((Integer) integerPositions[i]).intValue());
			}
			indexedDocuments = null; // free up the space

			// merge each category table with the new ones & write them out
			if (previousLength == 0)
				newDiskIndex.writeCategories(stream);
			else
				newDiskIndex.mergeCategories(this, positions, stream);
			offsetToHeader = stream.size();
			newDiskIndex.writeHeaderInfo(stream);
			positions = null; // free up the space
		} finally {
			stream.close();
		}
		newDiskIndex.writeOffsetToHeader(offsetToHeader);

		// rename file by deleting previous index file & renaming temp one
		File old = getIndexFile();
		if (!old.delete()) {
			if (DEBUG)
				System.out.println("mergeWith - Failed to delete " + this.fileName); //$NON-NLS-1$
			throw new IOException("Failed to delete index file " + this.fileName); //$NON-NLS-1$
		}
		if (!newIndexFile.renameTo(old)) {
			if (DEBUG)
				System.out.println("mergeWith - Failed to rename " + this.fileName); //$NON-NLS-1$
			throw new IOException("Failed to rename index file " + this.fileName); //$NON-NLS-1$
		}
	} catch (IOException e) {
		if (newIndexFile.exists() && !newIndexFile.delete())
			if (DEBUG)
				System.out.println("mergeWith - Failed to delete temp index " + newDiskIndex.fileName); //$NON-NLS-1$
		throw e;
	}

	newDiskIndex.fileName = this.fileName;
	return newDiskIndex;
}
private synchronized String[] readAllDocumentNames() throws IOException {
	if (this.numberOfChunks <= 0)
		return new String[0];

	DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(getIndexFile()), this.numberOfChunks > 5 ? 4096 : 2048));
	try {
		stream.skip(this.chunkOffsets[0]);
		int lastIndex = this.numberOfChunks - 1;
		String[] docNames = new String[lastIndex * CHUNK_SIZE + sizeOfLastChunk];
		for (int i = 0; i < this.numberOfChunks; i++)
			readChunk(docNames, stream, i * CHUNK_SIZE, i < lastIndex ? CHUNK_SIZE : sizeOfLastChunk);
		return docNames;
	} finally {
		stream.close();
	}
}
private synchronized HashtableOfObject readCategoryTable(char[] categoryName, boolean readDocNumbers) throws IOException {
	// result will be null if categoryName is unknown
	int offset = this.categoryOffsets.get(categoryName);
	if (offset == HashtableOfIntValues.NO_VALUE)
		return null;

	if (this.categoryTables == null) {
		this.categoryTables = new HashtableOfObject(3);
	} else {
		HashtableOfObject cachedTable = (HashtableOfObject) this.categoryTables.get(categoryName);
		if (cachedTable != null) {
			if (readDocNumbers) { // must cache remaining document number arrays
				Object[] arrayOffsets = cachedTable.valueTable;
				for (int i = 0, l = arrayOffsets.length; i < l; i++)
					if (arrayOffsets[i] instanceof Integer)
						arrayOffsets[i] = readDocumentNumbers(arrayOffsets[i]);
			}
			return cachedTable;
		}
	}

	DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(getIndexFile()), 2048));
	HashtableOfObject categoryTable = null;
	char[][] matchingWords = null;
	int count = 0;
	int firstOffset = -1;
	try {
		stream.skip(offset);
		int size = stream.readInt();
		categoryTable = new HashtableOfObject(size);
		int largeArraySize = 256;
		for (int i = 0; i < size; i++) {
			char[] word = Util.readUTF(stream);
			int arrayOffset = stream.readInt();
			// if arrayOffset is:
			//		<= 0 then the array size == 1 with the value -> -arrayOffset
			//		> 1 & < 256 then the size of the array is > 1 & < 256, the document array follows immediately
			//		256 if the array size >= 256 followed by another int which is the offset to the array (written prior to the table)
			if (arrayOffset <= 0) {
				categoryTable.put(word, new int[] {-arrayOffset}); // store 1 element array by negating documentNumber
			} else if (arrayOffset < largeArraySize) {
				categoryTable.put(word, readDocumentArray(stream, arrayOffset)); // read in-lined array providing size
			} else {
				arrayOffset = stream.readInt(); // read actual offset
				if (readDocNumbers) {
					if (matchingWords == null)
						matchingWords = new char[size][];
					if (count == 0)
						firstOffset = arrayOffset;
					matchingWords[count++] = word;
				}
				categoryTable.put(word, new Integer(arrayOffset)); // offset to array in the file
			}
		}
		this.categoryTables.put(categoryName, categoryTable);
		// cache the table as long as its not too big
		// in practise, some tables can be greater than 500K when the contain more than 10K elements
		this.cachedCategoryName = categoryTable.elementSize < 10000 ? categoryName : null;
	} finally {
		stream.close();
	}

	if (count > 0) {
		stream = new DataInputStream(new BufferedInputStream(new FileInputStream(getIndexFile()), 2048));
		try {
			stream.skip(firstOffset);
			for (int i = 0; i < count; i++) // each array follows the previous one
				categoryTable.put(matchingWords[i], readDocumentArray(stream, stream.readInt()));
		} finally {
			stream.close();
		}
	}
	return categoryTable;
}
private void readChunk(String[] docNames, DataInputStream stream, int index, int size) throws IOException {
	String current = stream.readUTF();
	docNames[index++] = current;
	for (int i = 1; i < size; i++) {
		int start = stream.readUnsignedByte(); // number of identical characters at the beginning
		int end = stream.readUnsignedByte(); // number of identical characters at the end
		String next = stream.readUTF();
		if (start > 0) {
			if (end > 0) {
				int length = current.length();
				next = current.substring(0, start) + next + current.substring(length - end, length);
			} else {
				next = current.substring(0, start) + next;
			}
		} else if (end > 0) {
			int length = current.length();
			next = next + current.substring(length - end, length);
		}
		docNames[index++] = next;
		current = next;
	}
}
private int[] readDocumentArray(DataInputStream stream, int arraySize) throws IOException {
	int[] result = new int[arraySize];
	switch (this.documentReferenceSize) {
		case 1 :
			for (int i = 0; i < arraySize; i++)
				result[i] = stream.readUnsignedByte();
			break;
		case 2 :
			for (int i = 0; i < arraySize; i++)
				result[i] = stream.readUnsignedShort();
			break;
		default :
			for (int i = 0; i < arraySize; i++)
				result[i] = stream.readInt();
			break;
	}
	return result;
}
synchronized String readDocumentName(int docNumber) throws IOException {
	if (this.cachedChunks == null)
		this.cachedChunks = new String[this.numberOfChunks][];

	int chunkNumber = docNumber / CHUNK_SIZE;
	String[] chunk = this.cachedChunks[chunkNumber];
	if (chunk == null) {
		boolean isLastChunk = chunkNumber == this.numberOfChunks - 1;
		int start = this.chunkOffsets[chunkNumber];
		int numberOfBytes = (isLastChunk ? this.startOfCategoryTables : this.chunkOffsets[chunkNumber + 1]) - start;
		if (numberOfBytes < 0)
			throw new IllegalArgumentException();
		byte[] bytes = new byte[numberOfBytes];
		FileInputStream file = new FileInputStream(getIndexFile());
		try {
			file.skip(start);
			if (file.read(bytes, 0, numberOfBytes) != numberOfBytes)
				throw new IOException();
		} finally {
			file.close();
		}
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
		int numberOfNames = isLastChunk ? this.sizeOfLastChunk : CHUNK_SIZE;
		chunk = this.cachedChunks[chunkNumber] = new String[numberOfNames];
		readChunk(chunk, stream, 0, numberOfNames);
	}
	return chunk[docNumber - (chunkNumber * CHUNK_SIZE)];
}
synchronized int[] readDocumentNumbers(Object arrayOffset) throws IOException {
	// arrayOffset is either a cached array of docNumbers or an Integer offset in the file
	if (arrayOffset instanceof int[])
		return (int[]) arrayOffset;

	DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(getIndexFile()), 2048));
	try {
		stream.skip(((Integer) arrayOffset).intValue());
		return readDocumentArray(stream, stream.readInt());
	} finally {
		stream.close();
	}
}
private void readHeaderInfo(RandomAccessFile file) throws IOException {
	file.seek(this.headerInfoOffset);

	// must be same order as writeHeaderInfo()
	this.numberOfChunks = file.readInt();
	this.sizeOfLastChunk = file.readUnsignedByte();
	this.documentReferenceSize = file.readUnsignedByte();

	this.chunkOffsets = new int[this.numberOfChunks];
	for (int i = 0; i < this.numberOfChunks; i++)
		this.chunkOffsets[i] = file.readInt();

	this.startOfCategoryTables = file.readInt();

	int size = file.readInt();
	this.categoryOffsets = new HashtableOfIntValues(size);
	for (int i = 0; i < size; i++)
		this.categoryOffsets.put(Util.readUTF(file), file.readInt()); // cache offset to category table
	this.categoryTables = new HashtableOfObject(3);
}
synchronized void startQuery() {
	this.cacheUserCount++;
}
synchronized void stopQuery() {
	if (--this.cacheUserCount < 0) {
		// clear cached items
		this.cacheUserCount = -1;
		this.cachedChunks = null;
		if (this.categoryTables != null) {
			if (this.cachedCategoryName == null) {
				this.categoryTables = null;
			} else if (this.categoryTables.elementSize > 1) {
				HashtableOfObject newTables = new HashtableOfObject(3);
				newTables.put(this.cachedCategoryName, this.categoryTables.get(this.cachedCategoryName));
				this.categoryTables = newTables;
			}
		}
	}
}
private void writeAllDocumentNames(String[] sortedDocNames, DataOutputStream stream) throws IOException {
	if (sortedDocNames.length == 0)
		throw new IllegalArgumentException();

	// assume the file was just created by initializeFrom()
	// in order, write: SIGNATURE & headerInfoOffset place holder, then each compressed chunk of document names
	stream.writeUTF(SIGNATURE);
	this.headerInfoOffset = stream.size();
	stream.writeInt(-1); // will overwrite with correct value later

	int size = sortedDocNames.length;
	this.numberOfChunks = (size / CHUNK_SIZE) + 1;
	this.sizeOfLastChunk = size % CHUNK_SIZE;
	if (this.sizeOfLastChunk == 0) {
		this.numberOfChunks--;
		this.sizeOfLastChunk = CHUNK_SIZE;
	}
	this.documentReferenceSize = size <= 0x7F ? 1 : (size <= 0x7FFF ? 2 : 4); // number of bytes used to encode a reference

	this.chunkOffsets = new int[this.numberOfChunks];
	int lastIndex = this.numberOfChunks - 1;
	for (int i = 0; i < this.numberOfChunks; i++) {
		this.chunkOffsets[i] = stream.size();

		int chunkSize = i == lastIndex ? this.sizeOfLastChunk : CHUNK_SIZE;
		int chunkIndex = i * CHUNK_SIZE;
		String current = sortedDocNames[chunkIndex];
		stream.writeUTF(current);
		for (int j = 1; j < chunkSize; j++) {
			String next = sortedDocNames[chunkIndex + j];
			int len1 = current.length();
			int len2 = next.length();
			int max = len1 < len2 ? len1 : len2;
			int start = 0; // number of identical characters at the beginning (also the index of first character that is different)
			while (current.charAt(start) == next.charAt(start)) {
				start++;
				if (max == start) break; // current is 'abba', next is 'abbab'
			}
			if (start > 255) start = 255;

			int end = 0; // number of identical characters at the end
			while (current.charAt(--len1) == next.charAt(--len2)) {
				end++;
				if (len2 == start) break; // current is 'abbba', next is 'abba'
				if (len1 == 0) break; // current is 'xabc', next is 'xyabc'
			}
			if (end > 255) end = 255;
			stream.writeByte(start);
			stream.writeByte(end);

			int last = next.length() - end;
			stream.writeUTF(start < last ? next.substring(start, last) : ""); //$NON-NLS-1$
			current = next;
		}
	}
	this.startOfCategoryTables = stream.size() + 1;
}
private void writeCategories(DataOutputStream stream) throws IOException {
	char[][] categoryNames = this.categoryTables.keyTable;
	Object[] tables = this.categoryTables.valueTable;
	for (int i = 0, l = categoryNames.length; i < l; i++)
		if (categoryNames[i] != null)
			writeCategoryTable(categoryNames[i], (HashtableOfObject) tables[i], stream);
	this.categoryTables = null;
}
private void writeCategoryTable(char[] categoryName, HashtableOfObject wordsToDocs, DataOutputStream stream) throws IOException {
	// the format of a category table is as follows:
	// any document number arrays with >= 256 elements are written before the table (the offset to each array is remembered)
	// then the number of word->int[] pairs in the table is written
	// for each word -> int[] pair, the word is written followed by:
	//		an int <= 0 if the array size == 1
	//		an int > 1 & < 256 for the size of the array if its > 1 & < 256, the document array follows immediately
	//		256 if the array size >= 256 followed by another int which is the offset to the array (written prior to the table)

	int largeArraySize = 256;
	Object[] values = wordsToDocs.valueTable;
	for (int i = 0, l = values.length; i < l; i++) {
		Object o = values[i];
		if (o != null) {
			if (o instanceof IntList)
				o = values[i] = ((IntList) values[i]).asArray();
			int[] documentNumbers = (int[]) o;
			if (documentNumbers.length >= largeArraySize) {
				values[i] = new Integer(stream.size());
				writeDocumentNumbers(documentNumbers, stream);
			}
		}
	}

	this.categoryOffsets.put(categoryName, stream.size()); // remember the offset to the start of the table
	this.categoryTables.put(categoryName, null); // flush cached table
	stream.writeInt(wordsToDocs.elementSize);
	char[][] words = wordsToDocs.keyTable;
	for (int i = 0, l = words.length; i < l; i++) {
		Object o = values[i];
		if (o != null) {
			Util.writeUTF(stream, words[i]);
			if (o instanceof int[]) {
				int[] documentNumbers = (int[]) o;
				if (documentNumbers.length == 1)
					stream.writeInt(-documentNumbers[0]); // store an array of 1 element by negating the documentNumber (can be zero)
				else
					writeDocumentNumbers(documentNumbers, stream);
			} else {
				stream.writeInt(largeArraySize); // mark to identify that an offset follows
				stream.writeInt(((Integer) o).intValue()); // offset in the file of the array of document numbers
			}
		}
	}
}
private void writeDocumentNumbers(int[] documentNumbers, DataOutputStream stream) throws IOException {
	// must store length as a positive int to detect in-lined array of 1 element
	int length = documentNumbers.length;
	stream.writeInt(length);
	Util.sort(documentNumbers);
	switch (this.documentReferenceSize) {
		case 1 :
			for (int i = 0; i < length; i++)
				stream.writeByte(documentNumbers[i]);
			break;
		case 2 :
			for (int i = 0; i < length; i++)
				stream.writeShort(documentNumbers[i]);
			break;
		default :
			for (int i = 0; i < length; i++)
				stream.writeInt(documentNumbers[i]);
			break;
	}
}
private void writeHeaderInfo(DataOutputStream stream) throws IOException {
	stream.writeInt(this.numberOfChunks);
	stream.writeByte(this.sizeOfLastChunk);
	stream.writeByte(this.documentReferenceSize);

	// apend the file with chunk offsets
	for (int i = 0; i < this.numberOfChunks; i++)
		stream.writeInt(this.chunkOffsets[i]);

	stream.writeInt(this.startOfCategoryTables);

	// append the file with the category offsets... # of name -> offset pairs, followed by each name & an offset to its word->doc# table
	stream.writeInt(this.categoryOffsets.elementSize);
	char[][] categoryNames = this.categoryOffsets.keyTable;
	int[] offsets = this.categoryOffsets.valueTable;
	for (int i = 0, l = categoryNames.length; i < l; i++) {
		if (categoryNames[i] != null) {
			Util.writeUTF(stream, categoryNames[i]);
			stream.writeInt(offsets[i]);
		}
	}
}
private void writeOffsetToHeader(int offsetToHeader) throws IOException {
	if (offsetToHeader > 0) {
		RandomAccessFile file = new RandomAccessFile(this.fileName, "rw"); //$NON-NLS-1$
		try {
			file.seek(this.headerInfoOffset); // offset to position in header
			file.writeInt(offsetToHeader);
			this.headerInfoOffset = offsetToHeader; // update to reflect the correct offset
		} finally {
			file.close();
		}
	}
}
}
