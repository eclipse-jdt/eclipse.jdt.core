/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.*;

import org.eclipse.jdt.core.ICorrectionRequestor;

public class CodeCorrectionTestsRequestor implements ICorrectionRequestor {
	private class Suggestion {
		public String text;
		public int start;
		public int end;
		public Suggestion(char[] text, int start, int end){
			this.text = new String(text);
			this.start = start;
			this.end = end;
		}
	}

	class SuggestionComparator implements Comparator {
		public int compare(Object o1,Object o2) {
			Suggestion s1 = (Suggestion)o1;
			Suggestion s2 = (Suggestion)o2;

			int result = s1.text.compareTo(s2.text);
			if(result == 0) {
				result = s1.start - s2.start;
				if(result == 0) {
					result = s1.end - s2.end;
				}
			}
			return result;
		}
	}


	private Vector suggestions = new Vector(5);

	public void acceptClass(char[] packageName,char[] className,char[] correctionName,int modifiers,int correctionStart,int correctionEnd){
		this.suggestions.addElement(new Suggestion(correctionName, correctionStart, correctionEnd));
	}

	public void acceptField(char[] declaringTypePackageName,char[] declaringTypeName,char[] name,char[] typePackageName,char[] typeName,char[] correctionName,int modifiers,int correctionStart,int correctionEnd){
		this.suggestions.addElement(new Suggestion(correctionName, correctionStart, correctionEnd));
	}

	public void acceptInterface(char[] packageName,char[] interfaceName,char[] correctionName,int modifiers,int correctionStart,int correctionEnd){
		this.suggestions.addElement(new Suggestion(correctionName, correctionStart, correctionEnd));
	}

	public void acceptLocalVariable(char[] name,char[] typePackageName,char[] typeName,int modifiers,int correctionStart,int correctionEnd){
		this.suggestions.addElement(new Suggestion(name, correctionStart, correctionEnd));
	}

	public void acceptMethod(char[] declaringTypePackageName,char[] declaringTypeName,char[] selector,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] returnTypePackageName,char[] returnTypeName,char[] correctionName,int modifiers,int correctionStart,int correctionEnd){
		this.suggestions.addElement(new Suggestion(correctionName, correctionStart, correctionEnd));
	}

	public void acceptPackage(char[] packageName,char[] correctionName,int correctionStart,int correctionEnd){
		this.suggestions.addElement(new Suggestion(correctionName, correctionStart, correctionEnd));
	}

	public String getSuggestions(){
		Suggestion[] sortedSuggestions = getSortedSuggestions();

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < sortedSuggestions.length; i++) {
			if(i != 0)
				result.append('\n');

			result.append(sortedSuggestions[i].text);
		}
		return result.toString();
	}

	public String getStarts(){
		Suggestion[] sortedSuggestions = getSortedSuggestions();

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < sortedSuggestions.length; i++) {
			if(i != 0)
				result.append('\n');

			result.append(sortedSuggestions[i].start);
		}
		return result.toString();
	}

	public String getEnds(){
		Suggestion[] sortedSuggestions = getSortedSuggestions();

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < sortedSuggestions.length; i++) {
			if(i != 0)
				result.append('\n');

			result.append(sortedSuggestions[i].end);
		}
		return result.toString();
	}

	private Suggestion[] getSortedSuggestions(){
		Object[] unsorted = this.suggestions.toArray();
		Suggestion[] sorted = new Suggestion[unsorted.length];
		System.arraycopy(unsorted, 0, sorted, 0, unsorted.length);
		Arrays.sort(sorted, new SuggestionComparator());
		return sorted;
	}
}
