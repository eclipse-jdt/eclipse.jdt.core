/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

import java.util.Vector;

public class CompletionTestsRequestor implements ICompletionRequestor {
	private Vector fElements = new Vector();
	private Vector fCompletions = new Vector();
	private Vector fRelevances = new Vector();
	
	public boolean fDebug = false;
/**
 * CompletionTestsRequestor constructor comment.
 */
public CompletionTestsRequestor() {
	super();
}

public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd, int relevance){
	fElements.addElement(new String(superTypeName));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("anonymous type " + new String(superTypeName));
}
/**
 * @see ICompletionRequestor
 */
public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(className));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("Class " + new String(className));
}
/**
 * @see ICompletionRequestor
 */
public void acceptError(IProblem error) {
}
/**
 * @see ICompletionRequestor
 */
public void acceptField(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] name,
	char[] typePackageName,
	char[] typeName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance) {
		
	fElements.addElement(new String(name));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("Field " + new String(name));
}
/**
 * @see ICompletionRequestor
 */
public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(interfaceName));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("Interface " + new String(interfaceName));

}
/**
 * @see ICompletionRequestor
 */
public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(keywordName));
	fCompletions.addElement(new String(keywordName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("Keyword " + new String(keywordName));
}
/**
 * @see ICompletionRequestor
 */
public void acceptLabel(char[] labelName, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(labelName));
	fCompletions.addElement(new String(labelName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("Label " + new String(labelName));
}
/**
 * @see ICompletionRequestor
 */
public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(name));
	fCompletions.addElement(new String(name));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("Local variable " + new String(name));
}
/**
 * @see ICompletionRequestor
 */
public void acceptMethod(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] selector,
	char[][] parameterPackageName,
	char[][] parameterTypeName,
	char[][] parameterNames,
	char[] returnTypePackageName,
	char[] returnTypeName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance) {
		
	fElements.addElement(new String(selector));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("method " + new String(selector));
}
/**
 * @see ICompletionRequestor
 */
public void acceptMethodDeclaration(
	char[] declaringTypePackageName,
	char[] declaringTypeName,
	char[] selector,
	char[][] parameterPackageNames,
	char[][] parameterTypeNames,
	char[][] parameterNames,
	char[] returnTypePackageName,
	char[] returnTypeName,
	char[] completionName,
	int modifiers,
	int completionStart,
	int completionEnd,
	int relevance) {
		
	fElements.addElement(new String(selector));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("method declaration " + new String(selector));
}
/**
 * @see ICompletionRequestor
 */
public void acceptModifier(char[] modifierName, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(modifierName));
	fCompletions.addElement(new String(modifierName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("modifier " + new String(modifierName));
}
/**
 * @see ICompletionRequestor
 */
public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(packageName));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("package " + new String(packageName));
}
/**
 * @see ICompletionRequestor
 */
public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd, int relevance) {
	fElements.addElement(new String(typeName));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("type " + new String(typeName));
}

/**
 * @see ICompletionRequestor
 */
public void acceptVariableName(char[] typePackageName, char[] typeName, char[] name, char[] completionName, int completionStart, int completionEnd, int relevance){
	fElements.addElement(new String(name));
	fCompletions.addElement(new String(completionName));
	fRelevances.addElement(String.valueOf(relevance));
	if (fDebug)
		System.out.println("variable name " + new String(name));
}

public String getResults() {
	return getResults(true);
}

public String getResults(boolean relevance) {
	StringBuffer result = new StringBuffer();
	int size = fElements.size();
	
	if (size == 1) {
		result.append(getResult(0, relevance));
	} else if (size > 1) {
		String[] sortedBucket = new String[size];
		for (int i = 0; i < size; i++) {
			sortedBucket[i] = getResult(i, relevance);
		}
		quickSort(sortedBucket, 0, size - 1);
		for (int j = 0; j < sortedBucket.length; j++) {
			if (result.length() > 0) result.append("\n");
			result.append(sortedBucket[j]);
		}
	}

	return result.toString();
}

private String getResult(int i, boolean relevance) {
	if(i < 0 || i >= fElements.size())
		return "";
	
	StringBuffer buffer =  new StringBuffer();
	buffer.append("element:");
	buffer.append(fElements.elementAt(i));
	buffer.append("    completion:");
	buffer.append(fCompletions.elementAt(i));
	if(relevance) {
		buffer.append("    relevance:");
		buffer.append(fRelevances.elementAt(i));
	}
	return buffer.toString();
}

protected String[] quickSort(String[] collection, int left, int right) {
	int original_left = left;
	int original_right = right;
	String mid = collection[ (left + right) / 2];
	do {
		while (mid.compareTo(collection[left]) > 0)
			// s[left] >= mid
			left++;
		while (mid.compareTo(collection[right]) < 0)
			// s[right] <= mid
			right--;
		if (left <= right) {
			String tmp = collection[left];
			collection[left] = collection[right];
			collection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right)
		collection = quickSort(collection, original_left, right);
	if (left < original_right)
		collection = quickSort(collection, left, original_right);
	return collection;
}
public String toString() {
	return getResults();
}
}
