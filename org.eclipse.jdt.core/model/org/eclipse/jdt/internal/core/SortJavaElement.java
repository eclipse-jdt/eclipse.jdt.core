/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Arrays;
import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.SortElementBuilder.SortElement;

/**
 * 
 * @since 2.1
 */
public abstract class SortJavaElement implements Comparable {

	public static final int COMPILATION_UNIT = 1;
	public static final int TYPE = 2;
	public static final int CLASS = 4;
	public static final int INTERFACE = 8;
	public static final int FIELD = 16;
	public static final int INITIALIZER = 32;
	public static final int METHOD = 64;	
	public static final int CONSTRUCTOR = 128;
	public static final int MULTIPLE_FIELD = 256;
	
	SortElementBuilder builder;

	protected static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	public static final String CORRESPONDING_ELEMENT = "corresponding_element";  //$NON-NLS-1$

	Hashtable options;
	
	protected int id;
	protected int sourceStart;
	protected int modifiers;
	protected String superclass;
	protected String[] superInterfaces;
	
	protected String[] parametersNames;
	protected String[] parametersTypes;
	protected String[] thrownExceptions;
	protected String returnType;
	protected String name;
	protected String type;
	protected int fieldCounter;
	protected SortElementBuilder.SortFieldDeclaration[] innerFields;
	protected ASTNode[] astNodes;
	
	protected int sourceEnd;
	protected int nameSourceStart;
	protected SortElement[] children;
	protected int children_count;
	protected SortElement firstChildBeforeSorting;
	protected SortElement lastChildBeforeSorting;
	protected int declarationStart;
	protected int declarationSourceEnd;
	
	SortJavaElement(SortElementBuilder builder) {
		this.builder = builder;
		this.options = JavaCore.getOptions();
	} 
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return this.builder.comparator.compare(this, o);
	}
	
	protected void addChild(SortElement sortElement) {
		if (this.children_count == 0) {
			this.children = new SortElement[3];
		} else if (this.children_count == this.children.length) {
			System.arraycopy(this.children, 0, this.children = new SortElement[this.children_count * 2], 0, this.children_count);
		}
		this.children[this.children_count++] = sortElement;
	}

	protected void closeCollections() {
		int length = this.children_count;
		if (length != 0 && length != this.children.length) {
			System.arraycopy(this.children, 0, this.children= new SortElement[length], 0, length);
		}			
	}

	abstract void display(StringBuffer buffer, int tab);
		
	abstract void generateSource(StringBuffer buffer, String lineSeparator);

	public String toString(int tab) {
		StringBuffer buffer = new StringBuffer();
		display(buffer, tab);
		if (this.children != null) {
			buffer
				.append(tab(tab))
				.append("CHILDREN ------------------------------" + LINE_SEPARATOR); //$NON-NLS-1$
			for (int i = 0; i < this.children_count; i++) {
				buffer.append(this.children[i].toString(tab + 1));
				buffer.append(LINE_SEPARATOR);
			}
		}
		return buffer.toString();
	}

	protected char[] tab(int tab) {
		char[] tabs = new char[tab];
		Arrays.fill(tabs, '\t');
		return tabs; 
	}

	public String toString() {
		return toString(0);
	}		

	protected void sort() {
		if (this.children != null) {
			this.firstChildBeforeSorting = children[0];
			this.lastChildBeforeSorting = children[this.children_count - 1];
			switch(this.id) {
				case CLASS | TYPE :
				case INTERFACE | TYPE :
				case COMPILATION_UNIT :		
					this.astNodes = convertChildren();
					Arrays.sort(astNodes, this.builder.comparator);
			}
			for (int i = 0, max = this.children_count; i < max; i++) {
				children[i].sort();
			} 
		}
	}
	
	private ASTNode[] convertChildren() {
		ASTNode[] astNodes = new ASTNode[this.children_count];
		for (int i = 0, max = this.children_count; i < max; i++) {
			SortElementBuilder.SortElement currentElement = this.children[i];
			ASTNode newNode = currentElement.convert();
			newNode.setProperty(CORRESPONDING_ELEMENT, currentElement);
			astNodes[i] = newNode;
		}
		return astNodes;
	}
}
