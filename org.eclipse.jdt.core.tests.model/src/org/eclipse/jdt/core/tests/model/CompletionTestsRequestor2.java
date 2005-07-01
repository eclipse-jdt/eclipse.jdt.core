/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;

public class CompletionTestsRequestor2 extends CompletionRequestor {
	private final char[] NULL_LITERAL = "null".toCharArray();//$NON-NLS-1$
	
	private CompletionContext context;
	private Vector proposals = new Vector();
	private IProblem problem;
	
	private boolean showParamterNames;
	private boolean showUniqueKeys;
	private boolean showPositions;
	
	public boolean fDebug = false;

	public CompletionTestsRequestor2() {
		this(false, false);
	}
	public CompletionTestsRequestor2(boolean showParamterNames) {
		this(showParamterNames, false, false);
	}
	public CompletionTestsRequestor2(boolean showParamterNames, boolean showUniqueKeys) {
		this(showParamterNames, showUniqueKeys, false);
	}
	public CompletionTestsRequestor2(boolean showParamterNames, boolean showUniqueKeys, boolean showPositions) {
		this.showParamterNames = showParamterNames;
		this.showUniqueKeys = showUniqueKeys;
		this.showPositions = showPositions;
	}
	public void acceptContext(CompletionContext cc) {
		this.context = cc;
	}
	public void accept(CompletionProposal proposal) {
		proposals.add(proposal);
	}

	public void completionFailure(IProblem p) {
		this.problem = p;
	}
	
	public String getContext() {
		if(this.context == null) return "";
		
		StringBuffer buffer = new StringBuffer();
		
		char[][] expectedTypesSignatures = this.context.getExpectedTypesSignatures();
		buffer.append("expectedTypesSignatures=");
		if(expectedTypesSignatures == null) {
			buffer.append(NULL_LITERAL);
		} else {
			buffer.append('{');
			for (int i = 0; i < expectedTypesSignatures.length; i++) {
				if(i > 0) buffer.append(',');
				buffer.append(expectedTypesSignatures[i]);
				
			}
			buffer.append('}');
		}
		buffer.append('\n');
		
		char[][] expectedTypesKeys = this.context.getExpectedTypesKeys();
		buffer.append("expectedTypesKeys=");
		if(expectedTypesSignatures == null) {
			buffer.append(NULL_LITERAL);
		} else {
			buffer.append('{');
			for (int i = 0; i < expectedTypesKeys.length; i++) {
				if(i > 0) buffer.append(',');
				buffer.append(expectedTypesKeys[i]);
				
			}
			buffer.append('}');
		}
		//buffer.append('\n');
		
		
		return buffer.toString();
	}
	public String getProblem() {
		return this.problem == null ? "" : this.problem.getMessage();
	}
	public String getResults() {
		if(proposals.size() == 0)
			return "";
		
		StringBuffer buffer = new StringBuffer();
		if(proposals.size() == 1) {
			appendProposal((CompletionProposal)proposals.elementAt(0), buffer);
		} else {
			CompletionProposal[] sortedProposals = (CompletionProposal[])proposals.toArray(new CompletionProposal[proposals.size()]);
			
			sortedProposals = quickSort(sortedProposals, 0, sortedProposals.length - 1);
			
			for(int i = 0; i < sortedProposals.length; i++) {
				if(i > 0) 
					buffer.append('\n');
				appendProposal(sortedProposals[i], buffer);
			}
		}
		return buffer.toString();
	}

	protected void appendProposal(CompletionProposal proposal, StringBuffer buffer) {
		buffer.append(getElementName(proposal));
		buffer.append('[');
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				buffer.append("ANONYMOUS_CLASS_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.FIELD_REF :
				buffer.append("FIELD_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.KEYWORD :
				buffer.append("KEYWORD"); //$NON-NLS-1$
				break;
			case CompletionProposal.LABEL_REF :
				buffer.append("LABEL_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.LOCAL_VARIABLE_REF :
				buffer.append("LOCAL_VARIABLE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_DECLARATION :
				buffer.append("METHOD_DECLARATION"); //$NON-NLS-1$
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$
				}
				break;
			case CompletionProposal.METHOD_REF :
				buffer.append("METHOD_REF"); //$NON-NLS-1$
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$
				}
				break;
			case CompletionProposal.PACKAGE_REF :
				buffer.append("PACKAGE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.TYPE_REF :
				buffer.append("TYPE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.VARIABLE_DECLARATION :
				buffer.append("VARIABLE_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION :
				buffer.append("POTENTIAL_METHOD_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_NAME_REFERENCE :
				buffer.append("METHOD_IMPORT"); //$NON-NLS-1$
				break;
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF :
				buffer.append("ANNOTATION_ATTRIBUTE_REF"); //$NON-NLS-1$
				break;
			default :
				buffer.append("PROPOSAL"); //$NON-NLS-1$
				break;
				
		}
		buffer.append("]{");
		buffer.append(proposal.getCompletion() == null ? NULL_LITERAL : proposal.getCompletion());
		buffer.append(", ");
		buffer.append(proposal.getDeclarationSignature() == null ? NULL_LITERAL : proposal.getDeclarationSignature());  
		buffer.append(", ");
		buffer.append(proposal.getSignature() == null ? NULL_LITERAL : proposal.getSignature());
		if(this.showUniqueKeys) {
			buffer.append(", ");
			buffer.append(proposal.getDeclarationKey() == null ? NULL_LITERAL : proposal.getDeclarationKey());
			buffer.append(", ");
			buffer.append(proposal.getKey() == null ? NULL_LITERAL : proposal.getKey());
		}
		buffer.append(", ");
		buffer.append(proposal.getName() == null ? NULL_LITERAL : proposal.getName());
		if(this.showParamterNames) {
			char[][] parameterNames = proposal.findParameterNames(null);
			buffer.append(", ");
			if(parameterNames == null || parameterNames.length <= 0) {
				buffer.append(NULL_LITERAL);
			} else {
				buffer.append("(");
				for (int i = 0; i < parameterNames.length; i++) {
					if(i > 0) buffer.append(", ");
					buffer.append(parameterNames[i]);
				}
				buffer.append(")");
			}
		}
		if(this.showPositions) {
			buffer.append(", [");
			buffer.append(proposal.getReplaceStart());
			buffer.append(", ");
			buffer.append(proposal.getReplaceEnd());
			buffer.append("]");
		}
		buffer.append(", ");
		buffer.append(proposal.getRelevance());
		buffer.append('}');
	}

	protected CompletionProposal[] quickSort(CompletionProposal[] collection, int left, int right) {
		int original_left = left;
		int original_right = right;
		CompletionProposal mid = collection[ (left + right) / 2];
		do {
			while (compare(mid, collection[left]) > 0)
				// s[left] >= mid
				left++;
			while (compare(mid, collection[right]) < 0)
				// s[right] <= mid
				right--;
			if (left <= right) {
				CompletionProposal tmp = collection[left];
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
	
	protected int compare(CompletionProposal proposal1, CompletionProposal proposal2) {
		int relDif = proposal1.getRelevance() - proposal2.getRelevance();
		if(relDif != 0) {
			return relDif;
		} else {
			String name1 = getElementName(proposal1);
			String name2 = getElementName(proposal2);
			return name1.compareTo(name2);
		}
	}
	
	protected String getElementName(CompletionProposal proposal) {
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				return new String(Signature.getSignatureSimpleName(proposal.getDeclarationSignature()));
			case CompletionProposal.TYPE_REF :
				return new String(Signature.getSignatureSimpleName(proposal.getSignature()));
			case CompletionProposal.FIELD_REF :
			case CompletionProposal.KEYWORD:
			case CompletionProposal.LABEL_REF:
			case CompletionProposal.LOCAL_VARIABLE_REF:
			case CompletionProposal.METHOD_REF:
			case CompletionProposal.METHOD_DECLARATION:
			case CompletionProposal.VARIABLE_DECLARATION:
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
			case CompletionProposal.METHOD_NAME_REFERENCE:
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
				return new String(proposal.getName());
			case CompletionProposal.PACKAGE_REF:
				return new String(proposal.getDeclarationSignature());	
		}
		return "";
	}
	public String toString() {
		return getResults();
	}
}
