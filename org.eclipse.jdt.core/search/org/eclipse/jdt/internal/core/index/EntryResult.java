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
private int[] documentIds;
private String[] documentNames;

public EntryResult(char[] word, int[] ids) {
	this.word = word;
	this.documentIds = ids;
}
public void addDocumentId(int id) {
	if (this.documentIds == null) {
		this.documentIds = new int[]{id};
		return;
	}

	int length = this.documentIds.length;
	for (int i = 0; i < length; i++)
		if (id == this.documentIds[i]) return;

	System.arraycopy(this.documentIds, 0, this.documentIds = new int[length + 1], 0, length);
	this.documentIds[length] = id;
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
	if (this.documentIds != null)
		for (int i = 0, l = this.documentIds.length; i < l; i++)
			addDocumentName(index.diskIndex.readDocumentName(this.documentIds[i]));
		
	return this.documentNames != null ? this.documentNames : new String[0];
}
public boolean isEmpty() {
	return this.documentIds == null && this.documentNames == null;
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
