/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Vector;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;

public class CompletionTestsRequestor extends CompletionRequestor {
	private Vector fElements = new Vector();
	private Vector fCompletions = new Vector();
	private Vector fRelevances = new Vector();
	private Vector fCompletionStart = new Vector();
	private Vector fCompletionEnd = new Vector();
	
	public boolean fDebug = false;

	private void acceptCommon(CompletionProposal proposal) {
		fCompletions.addElement(new String(proposal.getCompletion()));
		fRelevances.addElement(String.valueOf(proposal.getRelevance()));
		fCompletionStart.addElement(String.valueOf(proposal.getReplaceStart()));
		fCompletionEnd.addElement(String.valueOf(proposal.getReplaceEnd()));
	}
	public void accept(CompletionProposal proposal) {
		char[] typeName = null;
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				typeName = Signature.getSignatureSimpleName(proposal.getDeclarationSignature());
				fElements.addElement(new String(typeName));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("anonymous type " + new String(typeName));
				break;
				
			case CompletionProposal.TYPE_REF :
				if((proposal.getFlags() & Flags.AccEnum) != 0) {
					
				} else if((proposal.getFlags() & Flags.AccInterface) != 0) {
					typeName = Signature.getSignatureSimpleName(proposal.getSignature());
					fElements.addElement(new String(typeName));
					this.acceptCommon(proposal);
					if (fDebug)
						System.out.println("Interface " + new String(typeName));
				} else {
					typeName = Signature.getSignatureSimpleName(proposal.getSignature());
					fElements.addElement(new String(typeName));
					this.acceptCommon(proposal);
					if (fDebug) {
						if(Signature.getTypeSignatureKind(proposal.getSignature()) == Signature.TYPE_VARIABLE_SIGNATURE) {
							System.out.println("type parameter " + new String(typeName));
						} else {
							System.out.println("Class " + new String(typeName));
						}
					}
				}
				break;
				
			case CompletionProposal.FIELD_REF :
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("Field " + new String(proposal.getName()));
				break;
				
			case CompletionProposal.KEYWORD:
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("Keyword " + new String(proposal.getName()));
				break;
				
			case CompletionProposal.LABEL_REF:
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("Label " + new String(proposal.getName()));
				break;
				
			case CompletionProposal.LOCAL_VARIABLE_REF:
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("Local variable " + new String(proposal.getName()));
				break;
				
			case CompletionProposal.METHOD_REF:
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("method " + new String(proposal.getName()));
				break;
				
			case CompletionProposal.METHOD_DECLARATION:
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("method declaration " + new String(proposal.getName()));
				break;
				
			case CompletionProposal.PACKAGE_REF:
				fElements.addElement(new String(proposal.getDeclarationSignature()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("package " + new String(proposal.getDeclarationSignature()));
				break;
				
			case CompletionProposal.VARIABLE_DECLARATION:
				fElements.addElement(new String(proposal.getName()));
				this.acceptCommon(proposal);
				if (fDebug)
					System.out.println("variable name " + new String(proposal.getName()));
				break;
		}

	}

	public String getResults() {
		return getResults(true, false);
	}

	public String getResultsWithPosition(){
		return getResults(true, true);
	}

	public String getResults(boolean relevance, boolean position) {
		StringBuffer result = new StringBuffer();
		int size = fElements.size();
		
		if (size == 1) {
			result.append(getResult(0, relevance, position));
		} else if (size > 1) {
			String[] sortedBucket = new String[size];
			for (int i = 0; i < size; i++) {
				sortedBucket[i] = getResult(i, relevance, position);
			}
			quickSort(sortedBucket, 0, size - 1);
			for (int j = 0; j < sortedBucket.length; j++) {
				if (result.length() > 0) result.append("\n");
				result.append(sortedBucket[j]);
			}
		}

		return result.toString();
	}

	private String getResult(int i, boolean relevance, boolean position) {
		if(i < 0 || i >= fElements.size())
			return "";
		
		StringBuffer buffer =  new StringBuffer();
		buffer.append("element:");
		buffer.append(fElements.elementAt(i));
		buffer.append("    completion:");
		buffer.append(fCompletions.elementAt(i));
		if(position) {
			buffer.append("    position:[");
			buffer.append(fCompletionStart.elementAt(i));
			buffer.append(",");
			buffer.append(fCompletionEnd.elementAt(i));
			buffer.append("]");
		}
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
