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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class PackageReferencePattern extends AndPattern {

protected char[] pkgName;

protected char[][] segments;
protected int currentSegment;
protected char[] decodedSegment;
	
public PackageReferencePattern(char[] pkgName, int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);

	this.pkgName = isCaseSensitive ? pkgName : CharOperation.toLowerCase(pkgName);
	this.segments = CharOperation.splitOn('.', this.pkgName);
	this.mustResolve = this.pkgName != null;
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptPackageReference(path, this.pkgName);
}
/**
 * ref/name (where name is the last segment of the package name)
 */ 
protected void decodeIndexEntry(IEntryResult entryResult) {
	this.decodedSegment = CharOperation.subarray(entryResult.getWord(), REF.length, -1);
}
/**
 * @see AndPattern#hasNextQuery()
 */
protected boolean hasNextQuery() {
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
/**
 * @see SearchPattern#indexEntryPrefix()
 */
protected char[] indexEntryPrefix() {
	return AbstractIndexer.bestReferencePrefix(
		REF,
		this.segments[this.currentSegment],
		matchMode,
		isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS | METHOD | FIELD;
}
/**
 * @see SearchPattern#matchIndexEntry()
 */
protected boolean matchIndexEntry() {
	switch(matchMode) {
		case EXACT_MATCH :
			return CharOperation.equals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
		case PREFIX_MATCH :
			return CharOperation.prefixEquals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
		case PATTERN_MATCH :
			return CharOperation.match(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
	}
	return false;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof QualifiedTypeReference)
		return this.matchLevel((QualifiedTypeReference) node, resolve);
	if (node instanceof QualifiedNameReference)
		return this.matchLevel((QualifiedNameReference) node, resolve);
	if (node instanceof ImportReference)
		return this.matchLevel((ImportReference) node, resolve);
	return IMPOSSIBLE_MATCH;
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;

	char[][] compoundName = null;
	if (binding instanceof ImportBinding) {
		compoundName = ((ImportBinding) binding).compoundName;
	} else {
		if (binding instanceof ProblemReferenceBinding) return INACCURATE_MATCH;
		if (binding instanceof ArrayBinding) {
			binding = ((ArrayBinding) binding).leafComponentType;
			if (binding == null) return INACCURATE_MATCH;
		}
		if (binding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) binding).fPackage;
			if (pkgBinding == null) return INACCURATE_MATCH;
			compoundName = pkgBinding.compoundName;
		}
	}
	return compoundName != null && matchesName(this.pkgName, CharOperation.concatWith(compoundName, '.'))
		? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
}
/**
 * Returns whether this package reference pattern matches the given import reference.
 */
protected int matchLevel(ImportReference importRef, boolean resolve) {
	// NOTE: Not called when resolve is true, see MatchingNodeSet.reportMatching(unit)
	if (!importRef.onDemand)
		return matchTokens(importRef.tokens); 

	return matchesName(this.pkgName, CharOperation.concatWith(importRef.tokens, '.'))
		? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
}
/**
 * Returns whether this package reference pattern matches the given qualified name reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(QualifiedNameReference qNameRef, boolean resolve) {
	if (!resolve)
		return matchTokens(qNameRef.tokens);

	Binding binding = qNameRef.binding;
	if (binding == null) return INACCURATE_MATCH;

	TypeBinding typeBinding = null;
	char[][] tokens = qNameRef.tokens;
	int lastIndex = tokens.length - 1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			// no valid match amongst fields
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			if (lastIndex < 0) return IMPOSSIBLE_MATCH;
			break;
		case BindingIds.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no package match in it
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
				if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
			} else {
				typeBinding = (TypeBinding)binding;
			}
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
			}
			break;					
	}
	return matchLevel(typeBinding);
}

/**
 * Returns whether this package reference pattern matches the given type reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(QualifiedTypeReference typeRef, boolean resolve) {
	return resolve
		? matchLevel(typeRef.resolvedType)
		: matchTokens(typeRef.tokens);
}
/**
 * @see SearchPattern#matchReportImportRef(ImportReference, Binding, IJavaElement, int, MatchLocator)
 */
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (binding == null) {
		this.matchReportReference(importRef, element, accuracy, locator);
	} else {
		if (binding instanceof ImportBinding) {
			locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, importRef.tokens, element, accuracy);
		} else if (binding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding)binding).fPackage;
			if (pkgBinding != null)
				locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, pkgBinding.compoundName, element, accuracy);
			else
				locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, importRef.tokens, element, accuracy);
		} 
	}
}
/**
 * @see SearchPattern#matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator)
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	if (reference instanceof ImportReference) {
		ImportReference importRef = (ImportReference)reference;
		if (importRef.onDemand) {
			tokens = importRef.tokens;
		} else {
			int length = importRef.tokens.length - 1;
			tokens = new char[length][];
			System.arraycopy(importRef.tokens, 0, tokens, 0, length);
		}
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference)reference;
		Binding binding = qNameRef.binding;
		TypeBinding typeBinding = null;
		switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
			case BindingIds.FIELD : // reading a field
				typeBinding = qNameRef.actualReceiverType;
				break;
			case BindingIds.TYPE : //=============only type ==============
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
				} else {
					typeBinding = (TypeBinding)binding;
				}
				break;
			case BindingIds.VARIABLE : //============unbound cases===========
			case BindingIds.TYPE | BindingIds.VARIABLE :						
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
				}
				break;
		}
		if (typeBinding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding)typeBinding).fPackage;
			if (pkgBinding != null)
				tokens = pkgBinding.compoundName;
		} 
		if (tokens == null)
			tokens = qNameRef.tokens;
	} else if (reference instanceof QualifiedTypeReference) {
		QualifiedTypeReference qTypeRef = (QualifiedTypeReference)reference;
		TypeBinding typeBinding = qTypeRef.resolvedType;
		if (typeBinding instanceof ArrayBinding)
			typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
		if (typeBinding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding)typeBinding).fPackage;
			if (pkgBinding != null)
				tokens = pkgBinding.compoundName;
		} 
		if (tokens == null)
			tokens = qTypeRef.tokens;
	}
	if (tokens == null)
		tokens = CharOperation.NO_CHAR_CHAR;
	locator.reportAccurateReference(reference.sourceStart, reference.sourceEnd, tokens, element, accuracy);
}
protected int matchTokens(char[][] tokens) {
	if (this.pkgName == null) return ACCURATE_MATCH;

	switch (this.matchMode) {
		case EXACT_MATCH:
		case PREFIX_MATCH:
			if (CharOperation.prefixEquals(this.pkgName, CharOperation.concatWith(tokens, '.'), this.isCaseSensitive))
				return POTENTIAL_MATCH;
			break;
		case PATTERN_MATCH:
			char[] pattern = this.pkgName[this.pkgName.length-1] == '*'
				? this.pkgName
				: CharOperation.concat(this.pkgName, ".*".toCharArray()); //$NON-NLS-1$
			if (CharOperation.match(pattern, CharOperation.concatWith(tokens, '.'), this.isCaseSensitive))
				return POTENTIAL_MATCH;
			break;
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * @see AndPattern#resetQuery()
 */
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	this.currentSegment = this.segments.length - 1;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("PackageReferencePattern: <"); //$NON-NLS-1$
	if (this.pkgName != null) buffer.append(this.pkgName);
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
