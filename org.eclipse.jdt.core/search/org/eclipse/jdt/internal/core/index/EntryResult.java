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

public class EntryResult {

private char[] word;
private int[] documentNumbers;
private String[] documentNames;

public EntryResult(char[] word, int[] docNumbers) {
	this.word = word;
	this.documentNumbers = docNumbers;
}
public void addDocumentNumber(int docNumber) {
	if (this.documentNumbers == null) {
		this.documentNumbers = new int[]{docNumber};
		return;
	}

	int length = this.documentNumbers.length;
	for (int i = 0; i < length; i++)
		if (docNumber == this.documentNumbers[i]) return;

	System.arraycopy(this.documentNumbers, 0, this.documentNumbers = new int[length + 1], 0, length);
	this.documentNumbers[length] = docNumber;
}
public void addDocumentName(String documentName) {
	if (this.documentNames == null) {
		this.documentNames = new String[]{documentName};
		return;
	}

	int length = this.documentNames.length;
	for (int i = 0; i < length; i++)
		if (documentName.equals(this.documentNames[i])) return;

	System.arraycopy(this.documentNames, 0, this.documentNames = new String[length + 1], 0, length);
	this.documentNames[length] = documentName;
}
public char[] getWord() {
	return this.word;
}
public String[] getDocumentNames(Index index) throws java.io.IOException {
	if (this.documentNumbers != null)
		for (int i = 0, l = this.documentNumbers.length; i < l; i++)
			addDocumentName(index.diskIndex.readDocumentName(this.documentNumbers[i]));
		
	return this.documentNames != null ? this.documentNames : new String[0];
}
public boolean isEmpty() {
	return this.documentNumbers == null && this.documentNames == null;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(word.length * 2);
	buffer.append("EntryResult: word = "); //$NON-NLS-1$
	buffer.append(this.word);
	buffer.append(", document names = {"); //$NON-NLS-1$
	for (int i = 0; i < this.documentNames.length; i++){
		if (i > 0) buffer.append(',');
		buffer.append(' ');
		buffer.append(this.documentNames[i]);
	}
	buffer.append(" }"); //$NON-NLS-1$
	return buffer.toString();
}
}
