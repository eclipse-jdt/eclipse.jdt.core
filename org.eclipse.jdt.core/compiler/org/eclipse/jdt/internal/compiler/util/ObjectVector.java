package org.eclipse.jdt.internal.compiler.util;

public final class ObjectVector {
	static int INITIAL_SIZE = 10;
	
	public int size;
	int maxSize;
	Object[] elements;
public ObjectVector() {
	maxSize = INITIAL_SIZE;
	size = 0;
	elements = new Object[maxSize];
}
public void add(Object newElement) {
	if (size == maxSize)	// knows that size starts <= maxSize
		System.arraycopy(elements, 0, (elements = new Object[maxSize *= 2]), 0, size);
	elements[size++] = newElement;
}
public void addAll(Object[] newElements) {
	if (size + newElements.length >= maxSize) {
		maxSize = size + newElements.length;	// assume no more elements will be added
		System.arraycopy(elements, 0, (elements = new Object[maxSize]), 0, size);
	}
	System.arraycopy(newElements, 0, elements, size, newElements.length);
	size += newElements.length;
}
public boolean contains(Object element) {
	for (int i = size; --i >= 0;)
		if (element == elements[i])
			return true;
	return false;
}
public Object elementAt(int index) {
	return elements[index];
}
public Object find(Object element) {
	for (int i = size; --i >= 0;)
		if (element == elements[i])
			return elements[i];
	return null;
}
public Object remove(Object element) {
	// assumes only one occurrence of the element exists
	for (int i = size; --i >= 0;)
		if (element == elements[i]) {
			// shift the remaining elements down one spot
			System.arraycopy(elements, i + 1, elements, i, --size - i);
			elements[size] = null;
			return element;
		}
	return null;
}
public void removeAll() {
	for (int i = size; --i >= 0;)
		elements[i] = null;
	size = 0;
}
public String toString() {
	String s = "";
	for (int i = 0; i < size; i++)
		s += elements[i].toString() + "\n";
	return s;
}
}
