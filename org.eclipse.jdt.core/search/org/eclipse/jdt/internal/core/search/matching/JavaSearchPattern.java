/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;


public class JavaSearchPattern extends SearchPattern {
	
	/*
	 * Whether this pattern is case sensitive.
	 */
	boolean isCaseSensitive;

	/*
	 * One of R_EXACT_MATCH, R_PREFIX_MATCH, R_PATTERN_MATCH, R_REGEXP_MATCH.
	 */
	int matchMode;
	
	// Signatures and arguments for parameterized types search
	char[][] typeSignatures;
	char[][][] typeArguments;

	protected JavaSearchPattern(int patternKind, int matchRule) {
		super(matchRule);
		((InternalSearchPattern)this).kind = patternKind;
		this.isCaseSensitive = (matchRule & R_CASE_SENSITIVE) != 0;
		this.matchMode = matchRule - (this.isCaseSensitive ? R_CASE_SENSITIVE : 0);
	}
	
	public SearchPattern getBlankPattern() {
		return null;
	}

	int getMatchMode() {
		return this.matchMode;
	}

	boolean isCaseSensitive () {
		return this.isCaseSensitive;
	}
	
	/**
	 * Returns whether the pattern includess type arguments information or not.
	 * @return true if pattern has signature *and* type arguments
	 */
	public boolean isParameterized() {
		return this.typeSignatures != null && this.typeArguments != null;
	}

	/* (non-Javadoc)
	 * Compute a IJavaElement signature or a string pattern signature to store
	 * its type arguments. Recurse when signature is qualified to store signatures and
	 * type arguments also for of all enclosing types.
	 */
	void computeSignature(String signature) {
		// In case of IJavaElement signature, replace '/' by '.'
		char[] source = signature.replace('/','.').replace('$','.').toCharArray();

		// Init counters and arrays
		char[][] signatures = new char[10][];
		int signaturesCount = 0;
		int[] lengthes = new int [10];
		int typeArgsCount = 0;
		int paramOpening = 0;
		boolean parameterized = false;
		
		// Scan each signature character
		for (int idx=0, ln = source.length; idx < ln; idx++) {
			switch (source[idx]) {
				case '>':
					paramOpening--;
					if (paramOpening == 0)  {
						if (signaturesCount == lengthes.length) {
							System.arraycopy(signatures, 0, signatures = new char[signaturesCount+10][], 0, signaturesCount);
							System.arraycopy(lengthes, 0, lengthes = new int[signaturesCount+10], 0, signaturesCount);
						}
						lengthes[signaturesCount] = typeArgsCount;
						typeArgsCount = 0;
					}
					break;
				case '<':
					paramOpening++;
					if (paramOpening == 1) {
						typeArgsCount = 0;
						parameterized = true;
					}
					break;
				case '*':
				case ';':
					if (paramOpening == 1) typeArgsCount++;
					break;
				case '.':
					if (paramOpening == 0)  {
						if (signaturesCount == lengthes.length) {
							System.arraycopy(signatures, 0, signatures = new char[signaturesCount+10][], 0, signaturesCount);
							System.arraycopy(lengthes, 0, lengthes = new int[signaturesCount+10], 0, signaturesCount);
						}
						signatures[signaturesCount] = new char[idx+1];
						System.arraycopy(source, 0, signatures[signaturesCount], 0, idx);
						signatures[signaturesCount][idx] = Signature.C_SEMICOLON;
						signaturesCount++;
					}
					break;
			}
		}
		
		// Store signatures and type arguments
		this.typeSignatures = new char[signaturesCount+1][];
		if (parameterized)
			this.typeArguments = new char[signaturesCount+1][][];
		this.typeSignatures[0] = source;
		if (parameterized) {
			this.typeArguments[0] = Signature.getTypeArguments(source);
			if (lengthes[signaturesCount] != this.typeArguments[0].length) {
				// TODO (frederic) abnormal signature => should raise an error
			}
		}
		for (int i=1, j=signaturesCount-1; i<=signaturesCount; i++, j--){
			this.typeSignatures[i] = signatures[j];
			if (parameterized) {
				this.typeArguments[i] = Signature.getTypeArguments(signatures[j]);
				if (lengthes[j] != this.typeArguments[i].length) {
					// TODO (frederic) abnormal signature => should raise an error
				}
			}
		}
	}

	/*
	 * Optimization of implementation above (uses cached matchMode and isCaseSenistive)
	 */
	public boolean matchesName(char[] pattern, char[] name) {
		if (pattern == null) return true; // null is as if it was "*"
		if (name != null) {
			switch (this.matchMode) {
				case R_EXACT_MATCH :
					return CharOperation.equals(pattern, name, this.isCaseSensitive);
				case R_PREFIX_MATCH :
					return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
				case R_PATTERN_MATCH :
					if (!this.isCaseSensitive)
						pattern = CharOperation.toLowerCase(pattern);
					return CharOperation.match(pattern, name, this.isCaseSensitive);
				case R_REGEXP_MATCH :
					// TODO (frederic) implement regular expression match
					return true;
			}
		}
		return false;
	}
	protected StringBuffer print(StringBuffer output) {
		output.append(", "); //$NON-NLS-1$
		if (this.typeSignatures != null && this.typeSignatures.length > 0) {
			output.append("signature:\""); //$NON-NLS-1$
			output.append(this.typeSignatures[0]);
			output.append("\", "); //$NON-NLS-1$
		}
		switch(getMatchMode()) {
			case R_EXACT_MATCH : 
				output.append("exact match, "); //$NON-NLS-1$
				break;
			case R_PREFIX_MATCH :
				output.append("prefix match, "); //$NON-NLS-1$
				break;
			case R_PATTERN_MATCH :
				output.append("pattern match, "); //$NON-NLS-1$
				break;
		}
		if (isCaseSensitive())
			output.append("case sensitive"); //$NON-NLS-1$
		else
			output.append("case insensitive"); //$NON-NLS-1$
		return output;
	}
	public final String toString() {
		return print(new StringBuffer(30)).toString();
	}
}
