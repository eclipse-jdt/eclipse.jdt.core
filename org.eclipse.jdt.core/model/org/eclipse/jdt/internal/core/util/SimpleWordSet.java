/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.compiler.CharOperation;

public final class SimpleWordSet {

// to avoid using Enumerations, walk the individual values skipping nulls
public char[][] words;
public int elementSize; // number of elements in the table
public int threshold;

public SimpleWordSet(int size) {
	if (size < 3) size = 3;
	this.elementSize = 0;
	this.threshold = size + 1; // size is the expected number of elements
	this.words = new char[2 * size + 1][];
}

public char[] add(char[] word) {
	int length = this.words.length;
	int index = CharOperation.hashCode(word) % length;
	char[] current;
	while ((current = words[index]) != null) {
		if (CharOperation.equals(current, word)) return current;
		if (++index == length) index = 0;
	}
	words[index] = word;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) rehash();
	return word;
}

public boolean includes(char[] word) {
	int length = this.words.length;
	int index = CharOperation.hashCode(word) % length;
	char[] current;
	while ((current = words[index]) != null) {
		if (CharOperation.equals(current, word)) return true;
		if (++index == length) index = 0;
	}
	return false;
}

private void rehash() {
	SimpleWordSet newSet = new SimpleWordSet(elementSize * 2); // double the number of expected elements
	char[] current;
	for (int i = words.length; --i >= 0;)
		if ((current = words[i]) != null)
			newSet.add(current);

	this.words = newSet.words;
	this.elementSize = newSet.elementSize;
	this.threshold = newSet.threshold;
}
}