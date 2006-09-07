/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.util.Util;

	public class TypeReferencePattern extends AndPattern {
	
	protected char[] qualification;
	protected char[] simpleName;
		
	protected char[] currentCategory;
	
	/* Optimization: case where simpleName == null */
	public int segmentsSize;
	protected char[][] segments;
	protected int currentSegment;

	protected static char[][] CATEGORIES = { REF };
	
	public TypeReferencePattern(char[] qualification, char[] simpleName, int matchRule) {
		this(matchRule);
	
		this.qualification = isCaseSensitive() ? qualification : CharOperation.toLowerCase(qualification);
		this.simpleName = (isCaseSensitive() || isCamelCase())  ? simpleName : CharOperation.toLowerCase(simpleName);
	
		if (simpleName == null)
			this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
		else
			this.segments = null;
		
		if (this.segments == null)
			if (this.qualification == null)
				this.segmentsSize =  0;
			else
				this.segmentsSize =  CharOperation.occurencesOf('.', this.qualification) + 1;
		else
			this.segmentsSize = this.segments.length;
	
		((InternalSearchPattern)this).mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
	}
	/*
	 * Instanciate a type reference pattern with additional information for generics search
	 */
	public TypeReferencePattern(char[] qualification, char[] simpleName, String typeSignature, int matchRule) {
		this(qualification, simpleName,matchRule);
		if (typeSignature != null) {
			// store type signatures and arguments
			this.typeSignatures = Util.splitTypeLevelsSignature(typeSignature);
			setTypeArguments(Util.getAllTypeArguments(this.typeSignatures));
			if (hasTypeArguments()) {
				this.segmentsSize = getTypeArguments().length + CharOperation.occurencesOf('/', this.typeSignatures[0]) - 1;
			}
		}
	}
	/*
	 * Instanciate a type reference pattern with additional information for generics search
	 */
	public TypeReferencePattern(char[] qualification, char[] simpleName, IType type, int matchRule) {
		this(qualification, simpleName,matchRule);
		storeTypeSignaturesAndArguments(type);
	}
	TypeReferencePattern(int matchRule) {
		super(TYPE_REF_PATTERN, matchRule);
	}
	public void decodeIndexKey(char[] key) {
		this.simpleName = key;
	}
	public SearchPattern getBlankPattern() {
		return new TypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
	public char[] getIndexKey() {
		if (this.simpleName != null)
			return this.simpleName;
	
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		if (this.currentSegment >= 0) 
			return this.segments[this.currentSegment];
		return null;
	}
	public char[][] getIndexCategories() {
		return CATEGORIES;
	}
	protected boolean hasNextQuery() {
		if (this.segments == null) return false;
	
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		// if package has at least 4 segments, don't look at the first 2 since they are mostly
		// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
		return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
	}

	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
		return true; // index key is not encoded so query results all match
	}

	protected void resetQuery() {
		/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
		if (this.segments != null)
			this.currentSegment = this.segments.length - 1;
	}
	protected StringBuffer print(StringBuffer output) {
		output.append("TypeReferencePattern: qualification<"); //$NON-NLS-1$
		if (qualification != null) 
			output.append(qualification);
		else
			output.append("*"); //$NON-NLS-1$
		output.append(">, type<"); //$NON-NLS-1$
		if (simpleName != null) 
			output.append(simpleName);
		else
			output.append("*"); //$NON-NLS-1$
		output.append(">"); //$NON-NLS-1$
		return super.print(output);
	}
}
