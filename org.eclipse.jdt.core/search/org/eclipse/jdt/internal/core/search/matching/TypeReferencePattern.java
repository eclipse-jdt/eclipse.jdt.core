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
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class TypeReferencePattern extends AndPattern {

protected char[] qualification;
protected char[] simpleName;

protected char[] decodedSimpleName;
protected char[] currentTag;

/* Optimization: case where simpleName == null */
protected char[][] segments;
protected int currentSegment;
protected char[] decodedSegment;

protected static char[][] TAGS = { TYPE_REF, SUPER_REF, REF, CONSTRUCTOR_REF };
protected static char[][] REF_TAGS = { REF };

public TypeReferencePattern(char[] qualification, char[] simpleName, int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null)
		this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
	
	this.mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptTypeReference(path, decodedSimpleName);
}
/**
 * Either decode ref/name, typeRef/name or superRef/superName/name
 */ 
protected void decodeIndexEntry(IEntryResult entryResult) {
	char[] word = entryResult.getWord();
	int tagLength = currentTag.length;
	int nameLength = CharOperation.indexOf(SEPARATOR, word, tagLength);
	if (this.simpleName == null)
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		this.decodedSegment = CharOperation.subarray(word, tagLength, nameLength);
	else
		this.decodedSimpleName = CharOperation.subarray(word, tagLength, nameLength);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	char[][] possibleTags = this.simpleName == null ? REF_TAGS : TAGS;
	for (int i = 0, max = possibleTags.length; i < max; i++) {
		currentTag = possibleTags[i];
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * @see AndPattern#hasNextQuery
 */
protected boolean hasNextQuery() {
	if (this.simpleName != null) return false;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
protected char[] indexEntryPrefix() {

	if (this.simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		return AbstractIndexer.bestReferencePrefix(
			REF,
			this.segments[this.currentSegment],
			matchMode, 
			isCaseSensitive);
	} else {
		return AbstractIndexer.bestReferencePrefix(
			currentTag,
			simpleName,
			matchMode, 
			isCaseSensitive);
	}
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS | METHOD | FIELD;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
		}
	} else {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(simpleName, decodedSimpleName, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(simpleName, decodedSimpleName, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(simpleName, decodedSimpleName, isCaseSensitive);
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof TypeReference)
		return matchLevel((TypeReference)node, resolve);
	if (node instanceof NameReference)
		return matchLevel((NameReference)node, resolve);
	if (node instanceof ImportReference)
		return matchLevel((ImportReference)node, resolve);
	return IMPOSSIBLE_MATCH;
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;

	TypeBinding typeBinding = (TypeBinding) binding;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding) return INACCURATE_MATCH;

	while (typeBinding != null) {
		int level = matchLevelForType(this.simpleName, this.qualification, typeBinding);
		if (level != IMPOSSIBLE_MATCH) return level;
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding) typeBinding).enclosingType();
		else return IMPOSSIBLE_MATCH;
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * Returns whether this type pattern matches the given import reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(ImportReference importRef, boolean resolve) {
	// NOTE: Not called when resolve is true, see MatchingNodeSet.reportMatching(unit)
	if (this.qualification != null) {
		char[][] tokens = importRef.tokens;
		char[] pattern = this.simpleName == null
			? this.qualification
			: CharOperation.concat(this.qualification, this.simpleName, '.');
		char[] qualifiedTypeName = CharOperation.concatWith(tokens, '.');
		switch (this.matchMode) {
			case EXACT_MATCH :
			case PREFIX_MATCH :
				if (CharOperation.prefixEquals(pattern, qualifiedTypeName, this.isCaseSensitive)) return POTENTIAL_MATCH;
				break;
			case PATTERN_MATCH:
				if (CharOperation.match(pattern, qualifiedTypeName, this.isCaseSensitive)) return POTENTIAL_MATCH;
				break;
		}
	} else {
		if (this.simpleName == null) return ACCURATE_MATCH;
		char[][] tokens = importRef.tokens;
		for (int i = 0, length = tokens.length; i < length; i++)
			if (matchesName(this.simpleName, tokens[i])) return ACCURATE_MATCH;
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * Returns whether this type pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(NameReference nameRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName == null)
			return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
		if (nameRef instanceof SingleNameReference)
			return matchesName(this.simpleName, ((SingleNameReference)nameRef).token)
				? POTENTIAL_MATCH // can only be a possible match since resolution is needed to find out if it is a type ref
				: IMPOSSIBLE_MATCH;

		char[][] tokens = ((QualifiedNameReference) nameRef).tokens;
		for (int i = 0, max = tokens.length; i < max; i++)
			if (matchesName(this.simpleName, tokens[i]))
				// can only be a possible match since resolution is needed to find out if it is a type ref
				return POTENTIAL_MATCH;
		return IMPOSSIBLE_MATCH;
	}

	Binding binding = nameRef.binding;

	if (nameRef instanceof SingleNameReference) {
		if (binding instanceof ProblemReferenceBinding)
			binding = ((ProblemReferenceBinding) binding).original;
		if (binding instanceof VariableBinding) return IMPOSSIBLE_MATCH;
		if (!(binding instanceof TypeBinding)) return INACCURATE_MATCH;
		return matchLevelForType(this.simpleName, this.qualification, (TypeBinding) binding);
	}

	TypeBinding typeBinding = null;
	QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
	char[][] tokens = qNameRef.tokens;
	int lastIndex = tokens.length-1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = nameRef.actualReceiverType;
			// no valid match amongst fields
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			if (lastIndex < 0) return IMPOSSIBLE_MATCH;
			break;
		case BindingIds.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no type match in it
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof ProblemReferenceBinding)
				binding = ((ProblemReferenceBinding) binding).original;
			if (!(binding instanceof TypeBinding)) return INACCURATE_MATCH;
			typeBinding = (TypeBinding) binding;
			break;
		/*
		 * Handling of unbound qualified name references. The match may reside in the resolved fragment,
		 * which is recorded inside the problem binding, along with the portion of the name until it became a problem.
		 */
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :						
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
				if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
			} else if (binding instanceof ProblemReferenceBinding) {
				ProblemReferenceBinding pbBinding = (ProblemReferenceBinding)binding;
				binding = pbBinding.original;
				if (!(binding instanceof TypeBinding)) return INACCURATE_MATCH;
				typeBinding = (TypeBinding) binding;
				char[][] partialQualifiedName = pbBinding.compoundName;
				lastIndex = partialQualifiedName == null ? -1 : partialQualifiedName.length - 1; // index of last bound token is one before the pb token
				if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
			}
			break;
	}
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.simpleName, tokens[lastIndex--])) {
			int level = matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding) typeBinding).enclosingType();
		else
			typeBinding = null;
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * Returns whether this type pattern matches the given type reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(TypeReference typeRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName == null)
			return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;

		if (typeRef instanceof SingleTypeReference) {
			if (matchesName(this.simpleName, ((SingleTypeReference) typeRef).token))
				return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
		} else { // QualifiedTypeReference
			char[][] tokens = ((QualifiedTypeReference) typeRef).tokens;
			// can only be a possible match since resolution is needed to find out if it is a type ref
			for (int i = 0, max = tokens.length; i < max; i++)
				if (matchesName(this.simpleName, tokens[i])) return POTENTIAL_MATCH;
		}
		return IMPOSSIBLE_MATCH;
	}

	TypeBinding typeBinding = typeRef.resolvedType;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding) {
		Binding binding = ((ProblemReferenceBinding) typeBinding).original;
		if (binding instanceof TypeBinding)
			typeBinding = (TypeBinding) binding;
		else if (binding == null)
			typeBinding = null;
	}
	if (typeBinding == null) return INACCURATE_MATCH;

	if (typeRef instanceof SingleTypeReference)
		return matchLevelForType(this.simpleName, this.qualification, typeBinding);

	QualifiedTypeReference qTypeRef = (QualifiedTypeReference) typeRef;
	char[][] tokens = qTypeRef.tokens;
	int lastIndex = tokens.length-1;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.simpleName, tokens[lastIndex--])) {
			int level = matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		else return IMPOSSIBLE_MATCH;
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * @see SearchPattern#matchReportImportRef(ImportReference, Binding, IJavaElement, int, MatchLocator)
 */
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	ReferenceBinding typeBinding = null;
	if (binding instanceof ReferenceBinding)
		typeBinding = (ReferenceBinding) binding;

	char[][] typeTokens = importRef.tokens;
	int lastIndex = typeTokens.length-1;
	char[][] tokens = null;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.simpleName, typeTokens[lastIndex--])) {
			int level = matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(typeTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		typeBinding = typeBinding.enclosingType();
	}
	if (tokens == null) {
		tokens = typeBinding == null || typeBinding instanceof ProblemReferenceBinding
			? new char[][] {this.simpleName}
			: importRef.tokens;
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++)
				lowerCaseTokens[i] = CharOperation.toLowerCase(tokens[i]);
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, tokens, element, accuracy);
}
/**
 * Reports the match of the given array type reference.
 */
protected void matchReportReference(ArrayTypeReference arrayRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = this.simpleName == null ? CharOperation.NO_CHAR_CHAR : new char[][] {this.simpleName};
	locator.reportAccurateReference(arrayRef.sourceStart, arrayRef.sourceEnd, tokens, element, accuracy);
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (reference instanceof QualifiedNameReference)
		matchReportReference((QualifiedNameReference) reference, element, accuracy, locator);
	else if (reference instanceof QualifiedTypeReference)
		matchReportReference((QualifiedTypeReference) reference, element, accuracy, locator);
	else if (reference instanceof ArrayTypeReference)
		matchReportReference((ArrayTypeReference) reference, element, accuracy, locator);
	else
		super.matchReportReference(reference, element, accuracy, locator);
}
/**
 * Reports the match of the given qualified name reference.
 */
protected void matchReportReference(QualifiedNameReference qNameRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	Binding binding = qNameRef.binding;
	TypeBinding typeBinding = null;
	char[][] nameTokens = qNameRef.tokens;
	int lastIndex = nameTokens.length-1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			break;
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
			} else {
				typeBinding = (TypeBinding)binding;
			}
			break;
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :						
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
			}
			break;
	}
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.simpleName, nameTokens[lastIndex--])) {
			int level = matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(nameTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		typeBinding = typeBinding instanceof ReferenceBinding
			? ((ReferenceBinding)typeBinding).enclosingType()
			: null;
	} 
	if (tokens == null) {
		tokens = binding == null || binding instanceof ProblemBinding
			? new char[][] {this.simpleName}
			: qNameRef.tokens;
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(qNameRef.sourceStart, qNameRef.sourceEnd, tokens, element, accuracy);
}
/**
 * Reports the match of the given qualified type reference.
 */
protected void matchReportReference(QualifiedTypeReference qTypeRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	TypeBinding typeBinding = qTypeRef.resolvedType;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	char[][] typeTokens = qTypeRef.tokens;
	int lastIndex = typeTokens.length-1;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.simpleName, typeTokens[lastIndex--])) {
			int level = matchLevelForType(this.simpleName, this.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(typeTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		else
			typeBinding = null;
	}
	if (tokens == null) {
		if (typeBinding == null || typeBinding instanceof ProblemReferenceBinding)
			tokens = new char[][] {this.simpleName};
		else
			tokens = qTypeRef.tokens;
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(qTypeRef.sourceStart, qTypeRef.sourceEnd, tokens, element, accuracy);
}
/**
 * @see AndPattern#resetQuery
 */
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	if (this.simpleName == null)
		this.currentSegment = this.segments.length - 1;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("TypeReferencePattern: pkg<"); //$NON-NLS-1$
	if (qualification != null) buffer.append(qualification);
	buffer.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) buffer.append(simpleName);
	buffer.append(">, "); //$NON-NLS-1$
	switch(matchMode){
		case EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	if (isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
