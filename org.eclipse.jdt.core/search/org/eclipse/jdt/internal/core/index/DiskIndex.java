/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.io.*;

import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class DiskIndex {

class SafeRandomAccessFile extends RandomAccessFile {
	public SafeRandomAccessFile(File file, String mode) throws IOException {
		super(file, mode);
	}
	public SafeRandomAccessFile(String name, String mode) throws IOException {
		super(name, mode);
	}
	protected void finalize() throws IOException {
		close();
	}
}

String fileName;
SafeRandomAccessFile file;

private int categoryOffset; // offset to the category name table
private int numberOfChunks;
private int sizeOfLastChunk;
private int[] chunkOffsets;
private int documentReferenceSize; // 1, 2 or more bytes... depends on # of document names

private String[][] cachedChunks;
private HashtableOfObject categoryTables; // category name -> HashtableOfObject(words -> int[] of document #'s)

public static final String SIGNATURE= "INDEX FILE 0.002"; //$NON-NLS-1$
public static boolean DEBUG = false;

private static final int RE_INDEXED = -1;
private static final int DELETED = -2;

private static final int CHUNK_SIZE = 100;

DiskIndex(String fileName) {
	this.fileName = fileName;
	this.file = null;

	// initialized when file is opened
	this.categoryOffset = -1;
	this.numberOfChunks = -1;
	this.sizeOfLastChunk = -1;
	this.chunkOffsets = null;
	this.documentReferenceSize = -1;

	// clear cached items
	this.cachedChunks = null;
	this.categoryTables = null;
}
SimpleSet addDocumentNames(String substring, MemoryIndex memoryIndex) throws IOException {
	// must skip over documents which have been added/changed/deleted in the memory index
	try {
		open();
		SimpleSet results = new SimpleSet();
		String[] docNames = readAllDocumentNames();
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
	} finally {
		close();
	}
}
private void addQueryResult(char[] word, int[] refs, HashtableOfObject results, MemoryIndex memoryIndex) throws IOException {
	// must skip over documents which have been added/changed/deleted in the memory index
	EntryResult result = (EntryResult) results.get(word);
	if (memoryIndex == null) {
		if (result == null) {
			results.put(word, new EntryResult(word, refs));
		} else {
			for (int i = 0, l = refs.length; i < l; i++)
				result.addDocumentId(refs[i]);
		}
	} else {
		SimpleLookupTable docsToRefs = memoryIndex.docsToReferences;
		if (result == null)
			result = new EntryResult(word, null);
		for (int i = 0, l = refs.length; i < l; i++) {
			String docName = readDocumentName(refs[i]);
			if (!docsToRefs.containsKey(docName))
				result.addDocumentName(docName);
		}
		if (!result.isEmpty())
			results.put(word, result);
	}
}
HashtableOfObject addQueryResults(char[][] categories, char[] key, int matchRule, MemoryIndex memoryIndex) throws IOException {
	// assumes sender has opened the index & will close when finished
	HashtableOfObject results = new HashtableOfObject(3);
	if (matchRule == SearchPattern.R_EXACT_MATCH + SearchPattern.R_CASE_SENSITIVE) {
		for (int i = 0, l = categories.length; i < l; i++) {
			HashtableOfObject wordsToDocs = readCategoryTable(categories[i]);
			if (wordsToDocs != null) {
				int[] docNumbers = (int[]) wordsToDocs.get(key);
				if (docNumbers != null)
					addQueryResult(key, docNumbers, results, memoryIndex);
			}
		}
	} else {
		for (int i = 0, l = categories.length; i < l; i++) {
			HashtableOfObject wordsToDocs = readCategoryTable(categories[i]);
			if (wordsToDocs != null) {
				char[][] words = wordsToDocs.keyTable;
				Object[] docNumbers = wordsToDocs.valueTable;
				for (int j = 0, m = words.length; j < m; j++) {
					char[] word = words[j];
					if (word != null && Index.isMatch(key, word, matchRule))
						addQueryResult(word, (int[]) docNumbers[j], results, memoryIndex);
				}
			}
		}
	}
	return results;
}
void checkSignature() throws IOException {
	SafeRandomAccessFile temp = new SafeRandomAccessFile(this.fileName, "r"); //$NON-NLS-1$
	String signature = temp.readUTF();
	temp.close();

	if (!signature.equals(SIGNATURE))
		throw new IOException(Util.bind("exception.wrongFormat")); //$NON-NLS-1$
}
void close() throws IOException {
	if (this.file == null) return;

	// clear cached items
	this.cachedChunks = null;
	this.categoryTables = null;

	SafeRandomAccessFile temp = this.file;
	this.file = null;
	temp.close();
}
private String[] computeDocumentNames(String[] onDiskNames, int[] positions, SimpleLookupTable indexedDocuments, MemoryIndex memoryIndex) {
	int onDiskLength = onDiskNames.length;
	Object[] docNames = memoryIndex.docsToReferences.keyTable;
	Object[] categories = memoryIndex.docsToReferences.valueTable;
	if (onDiskLength == 0) {
		// disk index was empty, so add every indexed document
		for (int i = 0, l = categories.length; i < l; i++)
			if (categories[i] != null)
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
					if (categories[i] == null) {
						positions[j] = DELETED;
						numDeletedDocNames++;
					} else {
						positions[j] = RE_INDEXED;
						numReindexedDocNames++;
					}
					continue nextPath;
				}
			}
			if (categories[i] != null)
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
private void copyQueryResults(HashtableOfObject categoryToWords, int newPosition) throws IOException {
	char[][] categoryNames = categoryToWords.keyTable;
	Object[] wordSets = categoryToWords.valueTable;
	for (int i = 0, l = categoryNames.length; i < l; i++) {
		char[] categoryName = categoryNames[i];
		if (categoryName != null) {
			HashtableOfObject wordsToDocs = (HashtableOfObject) this.categoryTables.get(categoryName);
			if (wordsToDocs == null)
				this.categoryTables.put(categoryName, wordsToDocs = new HashtableOfObject(3));

			SimpleWordSet wordSet = (SimpleWordSet) wordSets[i];
			char[][] words = wordSet.words;
			for (int j = 0, m = words.length; j < m; j++) {
				char[] word = words[j];
				if (word != null) {
					int[] docNumbers = (int[]) wordsToDocs.get(word);
					if (docNumbers == null) {
						docNumbers = new int[] {newPosition};
					} else {
						int itsLength = docNumbers.length;
						System.arraycopy(docNumbers, 0, docNumbers = new int[itsLength + 1], 0, itsLength);
						docNumbers[itsLength] = newPosition;
					}
					wordsToDocs.put(word, docNumbers);
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
			checkSignature();
			return;
		}
		if (!indexFile.delete()) {
			if (DEBUG)
				System.out.println("initialize - Failed to delete index " + this.fileName); //$NON-NLS-1$
			throw new IOException("Failed to delete index " + this.fileName); //$NON-NLS-1$
		}
	}
	if (indexFile.createNewFile()) {
		this.file = new SafeRandomAccessFile(this.fileName, "rw"); //$NON-NLS-1$ $NON-NLS-2$
		this.file.writeUTF(SIGNATURE);
		this.file.writeInt(this.categoryOffset);
		this.file.writeInt(this.numberOfChunks);
		this.file.writeByte(this.sizeOfLastChunk);
		this.file.writeByte(this.documentReferenceSize);
		this.file.close();
	} else {
		if (DEBUG)
			System.out.println("initialize - Failed to create new index " + this.fileName); //$NON-NLS-1$
		throw new IOException("Failed to create new index " + this.fileName); //$NON-NLS-1$
	}
}
private void initializeFrom(DiskIndex diskIndex) throws IOException {
	File temp = getIndexFile();
	if (temp.exists() && !temp.delete()) { // delete the temporary index file
		if (DEBUG)
			System.out.println("initializeFrom - Failed to delete temp index " + this.fileName); //$NON-NLS-1$
	} else if (!temp.createNewFile()) {
		if (DEBUG)
			System.out.println("initializeFrom - Failed to create temp index " + this.fileName); //$NON-NLS-1$
		throw new IOException("Failed to create temp index " + this.fileName); //$NON-NLS-1$
	}

	this.file = new SafeRandomAccessFile(this.fileName, "rw"); //$NON-NLS-1$
	this.file.writeUTF(SIGNATURE);
	this.categoryOffset = (int) this.file.length();
	this.file.writeInt(0); // will replace with correct category table offset

	this.numberOfChunks = -1;
	this.sizeOfLastChunk = -1;
	this.chunkOffsets = null;
	this.documentReferenceSize = -1;

	int size = diskIndex.categoryTables == null ? 7 : diskIndex.categoryTables.elementSize;
	this.categoryTables = new HashtableOfObject(size);
}
private void mergeCategories(DiskIndex onDisk, int[] positions) throws IOException {
	char[][] oldNames = onDisk.readCategoryNames();
	char[][] newNames = this.categoryTables.keyTable; // the names added in copyQueryResults()

	// arrays may contain null's
	SimpleWordSet combined = new SimpleWordSet(this.categoryTables.elementSize);
	for (int i = 0, l = oldNames.length; i < l; i++)
		if (oldNames[i] != null)
			combined.add(oldNames[i]);
	for (int i = 0, l = newNames.length; i < l; i++)
		if (newNames[i] != null)
			combined.add(newNames[i]);

	int count = 0;
	char[][] categoryNames = new char[combined.elementSize][];
	char[][] words = combined.words;
	for (int i = 0, l = words.length; i < l; i++)
		if (words[i] != null)
			categoryNames[count++] = words[i];
	Util.sort(categoryNames);

	int[] tablePositions = writeCategoryNames(categoryNames);
	for (int i = 0, l = categoryNames.length; i < l; i++) {
		char[] categoryName = categoryNames[i];
		mergeCategory(categoryName, onDisk, tablePositions[i], positions);
	}
}
private void mergeCategory(char[] categoryName, DiskIndex onDisk, int tableOffset, int[] positions) throws IOException {
	HashtableOfObject wordsToDocs = (HashtableOfObject) this.categoryTables.get(categoryName);
	if (wordsToDocs == null)
		wordsToDocs = new HashtableOfObject(3);

	HashtableOfObject oldWordsToDocs = onDisk.readCategoryTable(categoryName);
	if (oldWordsToDocs != null) {
		char[][] oldWords = oldWordsToDocs.keyTable;
		Object[] oldRefArrays = oldWordsToDocs.valueTable;
		for (int i = 0, l = oldWords.length; i < l; i++) {
			char[] oldWord = oldWords[i];
			if (oldWord != null) {
				int[] oldRefs = (int[]) oldRefArrays[i];
				int length = oldRefs.length;
				int[] mappedNumbers = new int[length];
				int count = 0;
				for (int j = 0; j < length; j++) {
					int pos = positions[oldRefs[j]];
					if (pos > RE_INDEXED) // forget any reference to a document which was deleted or re_indexed
						mappedNumbers[count++] = pos;
				}
				if (count < length)
					System.arraycopy(mappedNumbers, 0, mappedNumbers = new int[count], 0, count);

				int[] docNumbers = (int[]) wordsToDocs.get(oldWord);
				if (docNumbers == null) {
					wordsToDocs.put(oldWord, mappedNumbers);
				} else {
					int[] merged = new int[count + docNumbers.length];
					System.arraycopy(mappedNumbers, 0, merged, 0, count);
					System.arraycopy(docNumbers, 0, merged, count, docNumbers.length);
					wordsToDocs.put(oldWord, merged);
				}
			}
		}
	}
	writeCategoryTable(categoryName, wordsToDocs, tableOffset);
}
DiskIndex mergeWith(MemoryIndex memoryIndex) throws IOException {
	open();

	// compute & write out new docNames
	String[] docNames = readAllDocumentNames();
	int[] positions = new int[docNames.length]; // keeps track of the position of each document in the new sorted docNames
	SimpleLookupTable indexedDocuments = new SimpleLookupTable(3); // for each new/changed document in the memoryIndex
	docNames = computeDocumentNames(docNames, positions, indexedDocuments, memoryIndex);
	if (docNames.length == 0)
		return this; // memory index contained some deleted documents that had never been written to disk

	DiskIndex newDiskIndex = new DiskIndex(this.fileName + ".tmp"); //$NON-NLS-1$
	try {
		newDiskIndex.initializeFrom(this);
		newDiskIndex.writeAllDocumentNames(docNames);
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
		newDiskIndex.mergeCategories(this, positions);
		positions = null; // free up the space

		// rename file by deleting previous index file & renaming temp one
		close();
		newDiskIndex.close();

		File old = getIndexFile();
		if (!old.delete()) {
			if (DEBUG)
				System.out.println("mergeWith - Failed to delete " + this.fileName); //$NON-NLS-1$
			throw new IOException("Failed to delete index file " + this.fileName); //$NON-NLS-1$
		}
		File temp = newDiskIndex.getIndexFile();
		if (!temp.renameTo(old)) {
			if (DEBUG)
				System.out.println("mergeWith - Failed to rename " + this.fileName); //$NON-NLS-1$
			throw new IOException("Failed to rename index file " + this.fileName); //$NON-NLS-1$
		}
	} catch (IOException e) {
		try {
			close();
			newDiskIndex.close();
		} catch (IOException ignore) {
			// ignore
		}

		File temp = newDiskIndex.getIndexFile();
		if (temp.exists() && !temp.delete())
			if (DEBUG)
				System.out.println("mergeWith - Failed to delete temp index " + newDiskIndex.fileName); //$NON-NLS-1$
		throw e;
	}

	newDiskIndex.fileName = this.fileName;
	return newDiskIndex;
}
void open() throws IOException {
	if (this.file != null) return;

	SafeRandomAccessFile temp = new SafeRandomAccessFile(this.fileName, "r"); //$NON-NLS-1$
	String signature = temp.readUTF();
	if (!signature.equals(SIGNATURE))
		throw new IOException(Util.bind("exception.wrongFormat")); //$NON-NLS-1$

	// must be same as saveEmpty()
	this.categoryOffset = temp.readInt();
	this.numberOfChunks = temp.readInt();
	this.sizeOfLastChunk = temp.readUnsignedByte();
	this.documentReferenceSize = temp.readUnsignedByte();

	if (this.numberOfChunks >= 1) {
		this.chunkOffsets = new int[this.numberOfChunks];
		if (this.numberOfChunks == 1) {
			this.chunkOffsets[0] = (int) temp.getFilePointer();
		} else {
			for (int i = 0; i < this.numberOfChunks; i++)
				this.chunkOffsets[i] = temp.readInt();
		}
	}

	this.file = temp;
}
private String[] readAllDocumentNames() throws IOException {
	if (this.numberOfChunks <= 0)
		return new String[0];

	int lastIndex = this.numberOfChunks - 1;
	String[] docNames = new String[lastIndex * CHUNK_SIZE + sizeOfLastChunk];
	for (int i = 0; i < this.numberOfChunks; i++) {
		this.file.seek(this.chunkOffsets[i]);
		readChunk(docNames, i * CHUNK_SIZE, i < lastIndex ? CHUNK_SIZE : sizeOfLastChunk);
	}
	return docNames;
}
private char[][] readCategoryNames() throws IOException {
	// result may include null's
	if (this.categoryTables == null) { // retrieve from disk & cache them
		if (this.categoryOffset == -1) {
			this.categoryTables = new HashtableOfObject(7);
		} else {
			this.file.seek(this.categoryOffset);
			int size = this.file.readInt();
			this.categoryTables = new HashtableOfObject(size);
			for (int i = 0; i < size; i++)
				this.categoryTables.put(this.file.readUTF().toCharArray(), new Integer(this.file.readInt())); // cache offset to category table
		}
	}
	return this.categoryTables.keyTable;
}
private HashtableOfObject readCategoryTable(char[] categoryName) throws IOException {
	// result will be null if categoryName is unknown
	if (this.categoryTables == null)
		readCategoryNames();

	Object o = this.categoryTables.get(categoryName);
	if (o == null)
		return null;
	if (o instanceof HashtableOfObject)
		return (HashtableOfObject) o; // table was cached

	int offset = ((Integer) o).intValue();
	this.file.seek(offset);

	byte[] byteArray = new byte[this.file.readInt()];
	this.file.read(byteArray);
	ByteArrayInputStream bytes = new ByteArrayInputStream(byteArray);
	DataInputStream stream = new DataInputStream(bytes);

	int size = stream.readInt();
	HashtableOfObject categoryTable = new HashtableOfObject(size);
	for (int i = 0; i < size; i++)
		categoryTable.put(stream.readUTF().toCharArray(), readDocumentNumbers(stream));
	this.categoryTables.put(categoryName, categoryTable);
	return categoryTable;
}
private void readChunk(String[] docNames, int index, int size) throws IOException {
	byte[] byteArray = new byte[this.file.readInt()];
	this.file.read(byteArray);
	ByteArrayInputStream bytes = new ByteArrayInputStream(byteArray);
	DataInputStream stream = new DataInputStream(bytes);

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
String readDocumentName(int docNumber) throws IOException {
	if (this.cachedChunks == null)
		this.cachedChunks = new String[this.numberOfChunks][];

	int chunkNumber = docNumber / CHUNK_SIZE;
	if (this.cachedChunks[chunkNumber] == null) {
		this.file.seek(this.chunkOffsets[chunkNumber]);
		int size = chunkNumber == this.numberOfChunks - 1 ? this.sizeOfLastChunk : CHUNK_SIZE;
		this.cachedChunks[chunkNumber] = new String[size];
		readChunk(this.cachedChunks[chunkNumber], 0, size);
	}

	docNumber = docNumber - (chunkNumber * CHUNK_SIZE);
	return this.cachedChunks[chunkNumber][docNumber];
}
private int[] readDocumentNumbers(DataInputStream stream) throws IOException {
	int arraySize = stream.readShort();
	if (arraySize < 0)
		return new int[] {-arraySize}; // used a negative offset to represent an array of 1 element

	if (arraySize == 0x7FFF)
		arraySize = stream.readInt();
	int[] result = new int[arraySize];
	for (int i = 0; i < arraySize; i++) {
		switch (this.documentReferenceSize) {
			case 1 :
				result[i] = stream.readUnsignedByte();
				break;
			case 2 :
				result[i] = stream.readUnsignedShort();
				break;
			default :
				result[i] = stream.readInt();
				break;
		}
	}
	return result;
}
private void writeAllDocumentNames(String[] sortedDocNames) throws IOException {
	// assume file is positioned at the end
	// write the offset array first... ie. how many chunks are there & an offset to each one
	// append the chunks to the end of the file
	// need to seek back to the offset array to write the offset

	int size = sortedDocNames.length;
	this.numberOfChunks = (size / CHUNK_SIZE) + 1;
	this.sizeOfLastChunk = size % CHUNK_SIZE;
	if (this.sizeOfLastChunk == 0) {
		this.numberOfChunks--;
		this.sizeOfLastChunk = CHUNK_SIZE;
	}
	this.file.writeInt(this.numberOfChunks);
	this.file.writeByte(this.sizeOfLastChunk);

	this.documentReferenceSize = size <= 0x7F ? 1 : (size <= 0x7FFF ? 2 : 4); // number of bytes used to encode a reference
	this.file.writeByte(this.documentReferenceSize);

	this.chunkOffsets = new int[this.numberOfChunks];
	long tableOffset = 0;
	if (this.numberOfChunks > 1) {
		tableOffset = this.file.length();
		for (int i = 0; i < this.numberOfChunks; i++)
			this.file.writeInt(0); // will replace with the actual position
	}

	int lastIndex = this.numberOfChunks - 1;
	for (int i = 0; i < this.numberOfChunks; i++) {
		int chunkSize = i == lastIndex ? this.sizeOfLastChunk : CHUNK_SIZE;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(chunkSize * 50);
		DataOutputStream stream = new DataOutputStream(bytes);

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
			}
			if (end > 255) end = 255;
			stream.writeByte(start);
			stream.writeByte(end);

			int last = next.length() - end;
			stream.writeUTF(start < last ? next.substring(start, last) : ""); //$NON-NLS-1$
			current = next;
		}

		this.chunkOffsets[i] = (int) this.file.length();
		byte[] byteArray = bytes.toByteArray();
		this.file.writeInt(byteArray.length);
		this.file.write(byteArray);
	}

	if (this.numberOfChunks > 1) {
		this.file.seek(tableOffset);
		for (int i = 0; i < this.numberOfChunks; i++)
			this.file.writeInt(this.chunkOffsets[i]);
		this.file.seek(this.file.length());
	}
}
private int[] writeCategoryNames(char[][] categoryNames) throws IOException {
	// assume file is positioned at the end
	// write the category table... # of name -> offset pairs, followed by each name & an offset to its word->doc# table
	// return a lookup table that maps each category name to the position of its offset
	// seek back to this position to write the offset of the table

	int[] result = new int[categoryNames.length];
	int startOfTable = (int) this.file.length();
	this.file.seek(this.categoryOffset); // offset to position in header
	this.file.writeInt(startOfTable);
	this.categoryOffset = startOfTable; // update to reflect the correct offset
	this.file.seek(startOfTable);

	this.file.writeInt(categoryNames.length);
	for (int i = 0, l = categoryNames.length; i < l; i++) {
		char[] categoryName = categoryNames[i];
		this.file.writeUTF(new String(categoryName));
		result[i] = (int) this.file.length(); // will replace with the actual position in writeCategoryTable()
		this.file.writeInt(0);
	}
	return result;
}
private void writeCategoryTable(char[] categoryName, HashtableOfObject wordsToDocs, int tableOffset) throws IOException {
	int offset = (int) this.file.length(); // offset to this category table
	this.categoryTables.put(categoryName, new Integer(offset)); // flush cached result & remember its offset

	this.file.seek(tableOffset);
	this.file.writeInt(offset);
	this.file.seek(offset);

	ByteArrayOutputStream bytes = new ByteArrayOutputStream(wordsToDocs.elementSize * 50);
	DataOutputStream stream = new DataOutputStream(bytes);
	stream.writeInt(wordsToDocs.elementSize);
	char[][] words = wordsToDocs.keyTable;
	Object[] refArrays = wordsToDocs.valueTable;
	for (int i = 0, l = words.length; i < l; i++) {
		if (words[i] != null) {
			stream.writeUTF(new String(words[i]));
			writeDocumentNumbers((int[]) refArrays[i], stream);
		}
	}
	byte[] byteArray = bytes.toByteArray();
	this.file.writeInt(byteArray.length);
	this.file.write(byteArray);
}
private void writeDocumentNumbers(int[] documentNumbers, DataOutputStream stream) throws IOException {
	int length = documentNumbers.length;
	if (length == 1 && documentNumbers[0] > 0) {
			stream.writeShort(-documentNumbers[0]); // save writing out an array of size 1
	} else {
		if (length < 0x7FFF) {
			stream.writeShort(length);
		} else {
			stream.writeShort(0x7FFF); // use 0x7FFF as a marker for big lengths
			stream.writeInt(length);
		}
		Util.sort(documentNumbers);
		for (int i = 0; i < length; i++) {
			switch (this.documentReferenceSize) {
				case 1 :
					stream.writeByte(documentNumbers[i]);
					break;
				case 2 :
					stream.writeShort(documentNumbers[i]);
					break;
				default :
					stream.writeInt(documentNumbers[i]);
					break;
			}
		}
	}
}
}