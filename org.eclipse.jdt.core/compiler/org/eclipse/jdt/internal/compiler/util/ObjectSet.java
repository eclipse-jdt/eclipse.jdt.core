package org.eclipse.jdt.internal.compiler.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Enumeration;
import org.eclipse.jdt.internal.compiler.*;

/**
 * Set of Objects
 */
public final class ObjectSet {
	
	private Object[] elementTable;
	private int elementSize; // number of elements in the table
	private int threshold;
	
	public ObjectSet() {
		this(13);
	}

	public ObjectSet(int size) {

		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.elementTable = new Object[extraRoom];
	}

	public boolean contains(Object element) {

		int index = element.hashCode() % elementTable.length;
		Object currentElement;
		while ((currentElement = elementTable[index]) != null) {
			if (currentElement.equals(element)) return true;
			index = (index + 1) % elementTable.length;
		}
		return false;
	}

	public boolean add(Object element) {

		int length = this.elementTable.length;
		int index = element.hashCode() % length;
		Object currentElement;
		while ((currentElement = this.elementTable[index]) != null) {
			if (currentElement.equals(element)) return false;
			index = (index + 1) % length;
		}
		this.elementTable[index] = element;

		// assumes the threshold is never equal to the size of the table
		if (++elementSize > threshold)
			rehash();
		return true;
	}

	public void addAll(Object[] elements) {
		for (int i = 0, length = elements.length; i < length; i++){
			add(elements[i]);
		}
	}

	public void copyInto(Object[] targetArray){
		
		int index = 0;
		for (int i = 0, length = this.elementTable.length; i < length; i++){
			if (elementTable[i] != null){
				targetArray[index++] = this.elementTable[i];
			}
		}
	}

	public Enumeration elements(){
		
		return new Enumeration(){
			int index = 0;
			int count = 0;
			public boolean hasMoreElements(){
				return this.count < ObjectSet.this.elementSize;
			}
			public Object nextElement(){
				do {
					Object current = ObjectSet.this.elementTable[index];
					if (current != null){
						count++;
						return current;
					}
				} while(++this.index < ObjectSet.this.elementTable.length);
				return null;
			}	
		};
	}
	
	public boolean isEmpty(){
		return this.elementSize == 0;
	}

	public boolean remove(Object element) {

		int hash = element.hashCode();
		int length = this.elementTable.length;
		int index = hash % length;
		Object currentElement;
		while ((currentElement = elementTable[index]) != null) {
			if (currentElement.equals(element)){
				this.elementTable[index] = null;
				this.elementSize--;
				
				// local rehash - find the last matching element with same hashcode
				// and move it in place of the current one if any
				int next = index, lastMatching = -1;
				while (this.elementTable[next = (next + 1) % length] != null && next != index) {
					if (this.elementTable[next].hashCode() == hash){
						lastMatching = next;
					}
				}
				if (lastMatching > 0){
					this.elementTable[index] = this.elementTable[lastMatching];
					this.elementTable[lastMatching] = null;
				}
				return true;
			}
			index = (index + 1) % length;
		}
		return false;
	}

	public void removeAll() {
		
		for (int i = this.elementTable.length; --i >= 0;)
			this.elementTable[i] = null;
		this.elementSize = 0;
	}
	
	private void rehash() {

		ObjectSet newSet = new ObjectSet(elementSize * 2);
		// double the number of expected elements
		Object currentElement;
		for (int i = elementTable.length; --i >= 0;)
			if ((currentElement = elementTable[i]) != null)
				newSet.add(currentElement);

		this.elementTable = newSet.elementTable;
		this.threshold = newSet.threshold;
	}

	public int size() {
		return this.elementSize;
	}
	
	public String toString() {
		String s = "["; //$NON-NLS-1$
		Object object;
		int count = 0;
		for (int i = 0, length = elementTable.length; i < length; i++)
			if ((object = elementTable[i]) != null){
				if (count++ > 0) s += ", "; //$NON-NLS-1$
				s += elementTable[i]; 	
			}
		return s + "]";//$NON-NLS-1$
	}
	public String toDebugString() {
		String s = "#[\n"; //$NON-NLS-1$
		Object object;
		int count = 0;
		for (int i = 0, length = elementTable.length; i < length; i++){
			s += "\t"+i+"\t";//$NON-NLS-1$//$NON-NLS-2$
			object = elementTable[i];
			if (object == null){
				s+= "-\n";//$NON-NLS-1$
			} else {
				s+= object.toString()+ "\t#"+object.hashCode() +"\n";//$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return s + "]";//$NON-NLS-1$
	}
}