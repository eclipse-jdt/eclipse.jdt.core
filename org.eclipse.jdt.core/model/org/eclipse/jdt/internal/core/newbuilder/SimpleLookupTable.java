package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A simple lookup table is a non-synchronized Hashtable, whose keys
 * and values are Objects. It also uses linear probing to resolve collisions
 * rather than a linked list of hash table entries.
 */
public final class SimpleLookupTable implements Cloneable {

// to avoid using Enumerations, walk the individual tables skipping nulls
public Object keyTable[];
public Object valueTable[];

int elementSize; // number of elements in the table
int threshold;

public SimpleLookupTable() {
	this(13);
}

public SimpleLookupTable(int size) {
	this.elementSize = 0;
	this.threshold = size; // size represents the expected number of elements
	int extraRoom = (int) (size * 1.75f);
	if (this.threshold == extraRoom)
		extraRoom++;
	this.keyTable = new Object[extraRoom];
	this.valueTable = new Object[extraRoom];
}

public Object clone() throws CloneNotSupportedException {
	SimpleLookupTable result = (SimpleLookupTable) super.clone();
	result.elementSize = this.elementSize;
	result.threshold = this.threshold;

	int length = this.keyTable.length;
	result.keyTable = new Object[length];
	System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);

	length = this.valueTable.length;
	result.valueTable = new Object[length];
	System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
	return result;
}

public boolean containsKey(Object key) {
	int index = (key.hashCode() & 0x7FFFFFFF) % keyTable.length;
	Object currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key)) return true;
		index = (index + 1) % keyTable.length;
	}
	return false;
}

public Object get(Object key) {
	int index = (key.hashCode() & 0x7FFFFFFF) % keyTable.length;
	Object currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key)) return valueTable[index];
		index = (index + 1) % keyTable.length;
	}
	return null;
}

public Object put(Object key, Object value) {
	int index = (key.hashCode() & 0x7FFFFFFF) % keyTable.length;
	Object currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key)) return valueTable[index] = value;
		index = (index + 1) % keyTable.length;
	}
	keyTable[index] = key;
	valueTable[index] = value;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold)
		rehash();
	return value;
}

public Object removeKey(Object key) {
	int index = (key.hashCode() & 0x7FFFFFFF) % keyTable.length;
	Object currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key)) {
				Object value = valueTable[index];
				elementSize--;
				keyTable[index] = null;
				valueTable[index] = null;
				rehash();
				return value;
			}
		index = (index + 1) % keyTable.length;
	}
	return null;
}

private void rehash() {
	SimpleLookupTable newLookupTable = new SimpleLookupTable(elementSize * 2);
	// double the number of expected elements
	Object currentKey;
	for (int i = keyTable.length; --i >= 0;)
		if ((currentKey = keyTable[i]) != null)
			newLookupTable.put(currentKey, valueTable[i]);

	this.keyTable = newLookupTable.keyTable;
	this.valueTable = newLookupTable.valueTable;
	this.threshold = newLookupTable.threshold;
}

public int size() {
	return elementSize;
}

public String toString() {
	String s = ""; //$NON-NLS-1$
	Object object;
	for (int i = 0, length = valueTable.length; i < length; i++)
		if ((object = valueTable[i]) != null)
			s += keyTable[i].toString() + " -> " + object.toString() + "\n"; 	//$NON-NLS-2$ //$NON-NLS-1$
	return s;
}
}