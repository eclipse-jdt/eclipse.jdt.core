package org.eclipse.jdt.internal.compiler.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public final class ObjectVector {
	
	static int INITIAL_SIZE = 10;

	public int size;
	int maxSize;
	Object[] elements;
	
	public ObjectVector() {

		this.maxSize = INITIAL_SIZE;
		this.size = 0;
		this.elements = new Object[this.maxSize];
	}

	public void add(Object newElement) {

		if (this.size == this.maxSize) // knows that size starts <= maxSize
			System.arraycopy(this.elements, 0, (this.elements = new Object[this.maxSize *= 2]), 0, this.size);
		this.elements[this.size++] = newElement;
	}

	public void addAll(Object[] newElements) {

		if (this.size + newElements.length >= this.maxSize) {
			maxSize = this.size + newElements.length; // assume no more elements will be added
			System.arraycopy(this.elements, 0, (this.elements = new Object[this.maxSize]), 0, this.size);
		}
		System.arraycopy(newElements, 0, this.elements, size, newElements.length);
		this.size += newElements.length;
	}

	/**
	 * Identity check
	 */
	public boolean containsIdentical(Object element) {

		for (int i = this.size; --i >= 0;)
			if (element == this.elements[i])
				return true;
		return false;
	}

	/**
	 * Equality check
	 */
	public boolean contains(Object element) {

		for (int i = this.size; --i >= 0;)
			if (element.equals(this.elements[i]))
				return true;
		return false;
	}

	public void copyInto(Object[] targetArray){
		
		this.copyInto(targetArray, 0);
	}
	
	public void copyInto(Object[] targetArray, int index){
		
		System.arraycopy(this.elements, 0, targetArray, index, this.size);
	}	
	
	public Object elementAt(int index) {

		return this.elements[index];
	}

	public Object find(Object element) {

		for (int i = this.size; --i >= 0;)
			if (element.equals(this.elements[i]))
				return element;
		return null;
	}

	public Object remove(Object element) {

		// assumes only one occurrence of the element exists
		for (int i = this.size; --i >= 0;)
			if (element.equals(this.elements[i])) {
				// shift the remaining elements down one spot
				System.arraycopy(this.elements, i + 1, this.elements, i, --this.size - i);
				this.elements[this.size] = null;
				return element;
			}
		return null;
	}

	public void removeAll() {
		
		for (int i = this.size; --i >= 0;)
			this.elements[i] = null;
		this.size = 0;
	}

	public int size(){
		
		return this.size;
	}
	
	public String toString() {
		
		String s = ""; //$NON-NLS-1$
		for (int i = 0; i < this.size; i++)
			s += this.elements[i].toString() + "\n"; //$NON-NLS-1$
		return s;
	}
}