package org.eclipse.jdt.internal.compiler.util;

public final class HashtableOfInt {
	// to avoid using Enumerations, walk the individual tables skipping nulls
	public int[] keyTable;
	public Object[] valueTable;

	int elementSize; // number of elements in the table
	int threshold;
public HashtableOfInt() {
	this(13);
}
public HashtableOfInt(int size) {
	this.elementSize = 0;
	this.threshold = size; // size represents the expected number of elements
	int extraRoom = (int) (size * 1.75f);
	if (this.threshold == extraRoom)
		extraRoom++;
	this.keyTable = new int[extraRoom];
	this.valueTable = new Object[extraRoom];
}
public boolean containsKey(int key) {
	int index = key % valueTable.length;
	int currentKey;
	while ((currentKey = keyTable[index]) != 0) {
		if (currentKey == key)
			return true;
		index = (index + 1) % keyTable.length;
	}
	return false;
}
public Object get(int key) {
	int index = key % valueTable.length;
	int currentKey;
	while ((currentKey = keyTable[index]) != 0) {
		if (currentKey == key)	return valueTable[index];
		index = (index + 1) % keyTable.length;
	}
	return null;
}
public Object put(int key, Object value) {
	int index = key % valueTable.length;
	int currentKey;
	while ((currentKey = keyTable[index]) != 0) {
		if (currentKey == key)	return valueTable[index] = value;
		index = (index + 1) % keyTable.length;
	}
	keyTable[index] = key;
	valueTable[index] = value;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold)
		rehash();
	return value;
}
private void rehash() {
	HashtableOfInt newHashtable = new HashtableOfInt(elementSize * 2); // double the number of expected elements
	int currentKey;
	for (int i = keyTable.length; --i >= 0;)
		if ((currentKey = keyTable[i]) != 0)
			newHashtable.put(currentKey, valueTable[i]);

	this.keyTable = newHashtable.keyTable;
	this.valueTable = newHashtable.valueTable;
	this.threshold = newHashtable.threshold;
}
public int size() {
	return elementSize;
}
public String toString() {
	String s = ""/*nonNLS*/;
	Object object;
	for (int i = 0, length = valueTable.length; i < length; i++)
		if ((object = valueTable[i]) != null)
			s += keyTable[i] + " -> "/*nonNLS*/ + object.toString() + "\n"/*nonNLS*/;
	return s;
}
}
