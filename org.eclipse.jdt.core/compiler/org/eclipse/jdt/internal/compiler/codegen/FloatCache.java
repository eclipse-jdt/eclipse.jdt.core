package org.eclipse.jdt.internal.compiler.codegen;

public class FloatCache {
	private float keyTable[];
	private int valueTable[];
	private int elementSize;
/**
 * Constructs a new, empty hashtable. A default capacity and
 * load factor is used. Note that the hashtable will automatically 
 * grow when it gets full.
 */
public FloatCache() {
	this(13);
}
/**
 * Constructs a new, empty hashtable with the specified initial
 * capacity.
 * @param initialCapacity int
 *	the initial number of buckets
 */
public FloatCache(int initialCapacity) {
	elementSize = 0;
	keyTable = new float[initialCapacity];
	valueTable = new int[initialCapacity];
}
/**
 * Clears the hash table so that it has no more elements in it.
 */
public void clear() {
	for (int i = keyTable.length; --i >= 0;) {
		keyTable[i] = 0.0f;
		valueTable[i] = 0;
	}
	elementSize = 0;
}
/** Returns true if the collection contains an element for the key.
 *
 * @param key <CODE>float</CODE> the key that we are looking for
 * @return boolean
 * @see ConstantPoolCache#contains
 */
public boolean containsKey(float key) {
	if (key == 0.0f) {
		for (int i = 0, max = elementSize; i < max; i++) {
			if (keyTable[i] == 0.0f) {
				int value1 = Float.floatToIntBits(key);
				int value2 = Float.floatToIntBits(keyTable[i]);
				if (value1 == -2147483648 && value2 == -2147483648)
					return true;
				if (value1 == 0 && value2 == 0)
					return true;
			}
		}
	} else {
		for (int i = 0, max = elementSize; i < max; i++) {
			if (keyTable[i] == key) {
				return true;
			}
		}
	}
	return false;
}
/** Gets the object associated with the specified key in the
 * hashtable.
 * @param key <CODE>float</CODE> the specified key
 * @return int the element for the key or -1 if the key is not
 *	defined in the hash table.
 * @see ConstantPoolCache#put
 */
public int get(float key) {
	if (key == 0.0f) {
		for (int i = 0, max = elementSize; i < max; i++) {
			if (keyTable[i] == 0.0f) {
				int value1 = Float.floatToIntBits(key);
				int value2 = Float.floatToIntBits(keyTable[i]);
				if (value1 == -2147483648 && value2 == -2147483648)
					return valueTable[i];
				if (value1 == 0 && value2 == 0)
					return valueTable[i];
			}
		}
	} else {
		for (int i = 0, max = elementSize; i < max; i++) {
			if (keyTable[i] == key) {
				return valueTable[i];
			}
		}
	}
	return -1;
}
/**
 * Puts the specified element into the hashtable, using the specified
 * key.  The element may be retrieved by doing a get() with the same key.
 * 
 * @param key <CODE>float</CODE> the specified key in the hashtable
 * @param value <CODE>int</CODE> the specified element
 * @return int value
 */
public int put(float key, int value) {
	if (elementSize == keyTable.length) {
		// resize
		System.arraycopy(keyTable, 0, (keyTable = new float[elementSize * 2]), 0, elementSize);
		System.arraycopy(valueTable, 0, (valueTable = new int[elementSize * 2]), 0, elementSize);
	}
	keyTable[elementSize] = key;
	valueTable[elementSize] = value;
	elementSize++;
	return value;
}
/**
 * Converts to a rather lengthy String.
 *
 * return String the ascii representation of the receiver
 */
public String toString() {
	int max = elementSize;
	StringBuffer buf = new StringBuffer();
	buf.append("{");
	for (int i = 0; i < max; ++i) {
		if ((keyTable[i] != 0) || ((keyTable[i] == 0) && (valueTable[i] != 0))) {
			buf.append(keyTable[i]).append("->").append(valueTable[i]);
		}
		if (i < max) {
			buf.append(", ");
		}
	}
	buf.append("}");
	return buf.toString();
}
}
