package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class EntryResult implements IEntryResult {
	private char[] word;
	private int[]  fileRefs;
	
public EntryResult(char[] word, int[] refs) {
	this.word = word;
	this.fileRefs = refs;
}
public boolean equals(Object anObject){
	
	if (this == anObject) {
	    return true;
	}
	if ((anObject != null) && (anObject instanceof EntryResult)) {
		EntryResult anEntryResult = (EntryResult) anObject;
		if (!CharOperation.equals(this.word, anEntryResult.word)) return false;

		int length;
		int[] refs, otherRefs;
		if ((length = (refs = this.fileRefs).length) != (otherRefs = anEntryResult.fileRefs).length) return false;
		for (int i =  0; i < length; i++){
			if (refs[i] != otherRefs[i]) return false;
		}
		return true;
	}
	return false;
	
}
public int[] getFileReferences() {
	return fileRefs;
}
public char[] getWord() {
	return word;
}
public int hashCode(){
	return CharOperation.hashCode(word);
}
public String toString(){
	StringBuffer buffer = new StringBuffer(word.length * 2);
	buffer.append("EntryResult: word=");
	buffer.append(word);
	buffer.append(", refs={");
	for (int i = 0; i < fileRefs.length; i++){
		if (i > 0) buffer.append(',');
		buffer.append(' ');
		buffer.append(fileRefs[i]);
	}
	buffer.append(" }");
	return buffer.toString();
}
}
