package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

public class OrderedSet {
	protected int elementCount;
	protected Object [] elementKeys;
	protected int [] elementIndexes;
	protected Object [] orderedList;

	protected float loadFactor;

	private static final int DEFAULT_SIZE = 101;

/**
 * Constructs a new OrderedSet using the default capacity
 * and load factor.
 */
public OrderedSet() {
	this (DEFAULT_SIZE);
}
/**
 * Constructs a new OrderedSet using the specified capacity
 * and the default load factor.
 *
 * @param       capacity    the initial capacity
 */
public OrderedSet (int capacity) {
	this(capacity, 0.75f);
}
/**
 * Constructs a new OrderedSet using the specified capacity
 * and load factor.
 *
 * @param       loadFactor  the initial load factor
 */
public OrderedSet (int capacity, float loadFactor) {
	if (capacity <= 0) throw new IllegalArgumentException();
	elementCount = 0;
	elementKeys = new Object [capacity];
	int[] indexes = new int [capacity];
	for (int i = capacity; --i >= 0; ) {
		indexes[i] = -1;
	}
	elementIndexes = indexes;
	orderedList = new Object[capacity];
	this.loadFactor = loadFactor;
}
private int findIndex (Object object, Object [] array) {
	Object key;
	int length = array.length;
	int index = (object.hashCode() & 0x7FFFFFFF) % length;
	for (int i=index; i<length; i++) {
		if (((key = array [i]) == null) || (key == object)) return i;
		if (key.equals (object)) return i;
	}
	for (int i=0; i<index; i++) {
		if (((key = array [i]) == null) || (key == object)) return i;
		if (key.equals (object)) return i;
	}
	return -1;  // unreacheable
}
/**
 * Answers the object associated with the specified index in
 * this OrderedSet. 
 *
 * @param       index the index to use
 * @return      the object associated with the specified index
 * @throws      ArrayIndexOutOfBoundsException if the index is out of range
 *
 * @see         #put
 */
public Object get(int index) {
	if (index >= elementCount) {
		throw new ArrayIndexOutOfBoundsException();
	}
	return orderedList[index];
}
/**
 * Answers whether the specified object is in
 * this OrderedSet.
 *
 * @param       obj the object, which must not be null
 * @return      true if the object is in the set, false otherwise
 *
 * @see         #put
 */
public boolean includes(Object obj) {
	if (obj == null) {
		throw new NullPointerException();
	}
	int index = elementIndexes[findIndex (obj, elementKeys)];
	return index != -1;
}
/**
 * Answers the index associated with the specified object in
 * this OrderedSet.
 *
 * @param       obj the object, which must not be null
 * @return      the index associated with the object
 * @throws      IllegalArgumentException if the key is not in the pool
 *
 * @see         #put
 */
public int index(Object obj) {
	if (obj == null) {
		throw new NullPointerException();
	}
	int findIndex = findIndex (obj, elementKeys);
	int index = elementIndexes[findIndex];
	if (index == -1) {
		throw new IllegalArgumentException();
	}
	return index;
}
/**
 * Associate the given object with the given index in this OrderedSet.
 * The object's index is not retrievable using index(Object).
 * But the object is retrievable using get(int index).
 * The index must not yet have been assigned.
 *
 * @param       index the index of the object
 * @param       obj the object to add
 *
 * @see         #get
 * @see         java.lang.Object#equals
 */
public void put(int index, Object obj) {
	if (obj == null) throw new NullPointerException ();
	if (orderedList.length <= index) {
		int[] newOrderedList = new int[orderedList.length*2];
		System.arraycopy(orderedList, 0, newOrderedList, 0, elementCount);
	}
	if (orderedList[index] != null) {
		throw new IllegalArgumentException();
	}
	orderedList[index] = obj;
	if (index >= elementCount) {
		elementCount = index + 1;
	}
}
/**
 * Associate the given object with the next index in this OrderedSet.
 * If the object is already present, its index is unchanged. The object cannot be null.
 *
 * @param       obj the object to add
 * @return      the index associated with the specified obj
 *
 * @see         #index
 * @see         java.lang.Object#equals
 */
public int put (Object obj) {
	if (obj == null) throw new NullPointerException ();
	int index = findIndex (obj, elementKeys);
	int resultIndex = elementIndexes [index];
	if (resultIndex == -1) {
		int size = (int)(elementKeys.length * loadFactor + 1);
		if ((elementCount + 1) >= size) {
			rehash ();
			index = findIndex (obj, elementKeys);
		}
		orderedList [elementCount] = elementKeys [index] = obj;
		return elementIndexes [index] = elementCount++;
	}
	else {
		return resultIndex;
	}
}
/**
 * Increases the capacity of this OrderedSet. This method is sent when
 * the size of this OrderedSet exceeds the load factor.
 */
protected void rehash () {
	Object key;
	int index, length = elementKeys.length<<1;
	Object [] newKeys = new Object [length];
	int [] newIndexes = new int [length];
	for (int i = length; --i >= 0; ) {
		newIndexes[i] = -1;
	}
	for (int i=0; i<elementKeys.length; i++) {
		if ((key = elementKeys [i]) != null) {
			index = findIndex (key, newKeys);
			newKeys [index] = key;
			newIndexes [index] = elementIndexes [i];
		}
	}
	Object [] newOrdered = new Object[length];
	System.arraycopy(orderedList, 0, newOrdered, 0, elementCount);
	elementKeys = newKeys;
	elementIndexes = newIndexes;
	orderedList = newOrdered;
}
/**
 * Answers the number of objects in this OrderedSet.
 *
 * @return      the number of objects in this OrderedSet
 */
public int size () {
	return elementCount;
}
/**
 * Answers the string representation of this OrderedSet.
 *
 * @return      the string representation of this OrderedSet
 */
public String toString () {
	Object key;
	StringBuffer buffer = new StringBuffer ();
	buffer.append ('{');
	for (int i=0; i<elementCount; i++) {
		if (i != 0) buffer.append (',');
		if (buffer.length() > 1000) {
			buffer.append("..."/*nonNLS*/);
			break;
		}
		buffer.append (orderedList[i]);
		buffer.append ('=');
		buffer.append (i);
	}
	buffer.append ('}');
	return buffer.toString ();
}
}
