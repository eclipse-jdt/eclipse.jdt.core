package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;

import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.*;

import java.io.IOException;

public class PackageReferencePattern extends AndPattern {
	private static char[][] TAGS = { REF };
	private char[] pkgName;

	private char[][] segments;
	private int currentSegment;
	private char[] decodedSegment;
public PackageReferencePattern(char[] pkgName, int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
	this.pkgName = pkgName;
	char[][] splittedName = CharOperation.splitOn('.', pkgName);
	this.segments = splittedName == TypeConstants.NoCharChar ? new char[][]{ pkgName } : splittedName;
	this.needsResolve = pkgName != null;
}
/**
 * ref/name (where name is the last segment of the package name)
 * @see SearchPattern#decodeIndexEntry
 */ 
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int tagLength = REF.length;
	int nameLength = CharOperation.indexOf(SEPARATOR, word, tagLength);
	if (nameLength < 0) nameLength = size;
	this.decodedSegment = CharOperation.subarray(word, tagLength, nameLength);
}
/**
 * @see SearchPattern#feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		int reference = references[i];
		if (reference != -1) { // if the reference has not been eliminated
			IndexedFile file = input.getIndexedFile(reference);
			String path;
			if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
				requestor.acceptPackageReference(path, this.pkgName);
			}
		}
	}
}
protected char[][] getPossibleTags() {
	return TAGS;
}
/**
 * @see AndPattern#hasNextQuery
 */
protected boolean hasNextQuery() {
	if (this.segments.length > 2) {
		// if package has more than 2 segments, don't look at the first 2 since they are mostly
		// redundant (eg. in 'org.eclipse.jdt.core.*', 'com.ibm' is used all the time)
		return --this.currentSegment >= 2;
	} else {
		return --this.currentSegment >= 0;
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {
	return AbstractIndexer.bestReferencePrefix(
		REF,
		this.segments[this.currentSegment],
		matchMode,
		isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS | METHOD | FIELD;
}
/**
 * Returns whether this package reference pattern matches the given tokens.
 */
private boolean matches(char[][] tokens) {
	char[] name = CharOperation.concatWith(tokens, '.');
	return this.matchesName(this.pkgName, name);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	switch(matchMode){
		case EXACT_MATCH :
			if (!CharOperation.equals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive)){
				return false;
			}
			break;
		case PREFIX_MATCH :
			if (!CharOperation.prefixEquals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive)){
				return false;
			}
			break;
		case PATTERN_MATCH :
			if (!CharOperation.match(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive)){
				return false;
			}
	}
	return true;
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] splitName = CharOperation.splitOn('.', 
		this.pkgName == null ? 
			new char[0] :
			this.pkgName);
	locator.reportQualifiedReference(reference.sourceStart, reference.sourceEnd, splitName, element, accuracy);
}
/**
 * @see AndPattern#resetQuery
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

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof QualifiedTypeReference) {
		return this.matchLevel((QualifiedTypeReference)node, resolve);
	} else if (node instanceof ImportReference) {
		return this.matchLevel((ImportReference)node, resolve);
	} else if (node instanceof QualifiedNameReference) {
		return this.matchLevel((QualifiedNameReference)node, resolve);
	}
	return IMPOSSIBLE_MATCH;
}

/**
 * Returns whether this package reference pattern matches the given import reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(ImportReference importRef, boolean resolve) {
	if (importRef.onDemand) {
		if (this.matches(importRef.tokens)) {
			return ACCURATE_MATCH;
		} else {
			return IMPOSSIBLE_MATCH;
		}
	} else {
		int length = importRef.tokens.length - 1;
		char[][] tokens = new char[length][];
		System.arraycopy(importRef.tokens, 0, tokens, 0, length);
		if (this.matches(tokens)) {
			return ACCURATE_MATCH;
		} else {
			return IMPOSSIBLE_MATCH;
		}
	}
}

/**
 * Returns whether this package reference pattern matches the given qualified name reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(QualifiedNameReference qNameRef, boolean resolve) {
	if (!resolve) {
		if (this.pkgName == null) {
			return POSSIBLE_MATCH;
		} else {
			switch (this.matchMode) {
				case EXACT_MATCH:
				case PREFIX_MATCH:
					if (CharOperation.prefixEquals(this.pkgName, CharOperation.concatWith(qNameRef.tokens, '.'), this.isCaseSensitive)) {
						return POSSIBLE_MATCH;
					} else {
						return IMPOSSIBLE_MATCH;
					}
				case PATTERN_MATCH:
					char[] pattern = this.pkgName[this.pkgName.length-1] == '*' ? this.pkgName : CharOperation.concat(this.pkgName, ".*".toCharArray()); //$NON-NLS-1$
					if (CharOperation.match(pattern, CharOperation.concatWith(qNameRef.tokens, '.'), this.isCaseSensitive)) {
						return POSSIBLE_MATCH;
					} else {
						return IMPOSSIBLE_MATCH;
					}
				default:
					return IMPOSSIBLE_MATCH;
			}
		}
	} else {
		Binding binding = qNameRef.binding;
		if (binding == null) {
			return INACCURATE_MATCH;
		} else {
			TypeBinding typeBinding = null;
			char[][] tokens = qNameRef.tokens;
			int lastIndex = tokens.length-1;
			switch (qNameRef.bits & Statement.RestrictiveFlagMASK) {
				case BindingIds.FIELD : // reading a field
					typeBinding = ((FieldBinding)binding).declaringClass;
					// no valid match amongst fields
					int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
					lastIndex -= otherBindingsCount + 1;
					if (lastIndex < 0) return IMPOSSIBLE_MATCH;
					break;
				case BindingIds.LOCAL : // reading a local variable
					return IMPOSSIBLE_MATCH; // no package match in it
				case BindingIds.TYPE : //=============only type ==============
					typeBinding = (TypeBinding)binding;
			}
			if (typeBinding instanceof ArrayBinding) {
				typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
			}
			if (typeBinding == null) {
				return INACCURATE_MATCH;
			} else {
				if (typeBinding instanceof ReferenceBinding) {
					PackageBinding pkgBinding = ((ReferenceBinding)typeBinding).fPackage;
					if (pkgBinding == null) {
						return INACCURATE_MATCH;
					} else if (this.matches(pkgBinding.compoundName)) {
						return ACCURATE_MATCH;
					} else {
						return IMPOSSIBLE_MATCH;
					}
				} else {
					return IMPOSSIBLE_MATCH;
				}
			}
		}
	}
}

/**
 * Returns whether this package reference pattern matches the given type reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(QualifiedTypeReference typeRef, boolean resolve) {
	if (!resolve) {
		if (this.pkgName == null) {
			return POSSIBLE_MATCH;
		} else {
			switch (this.matchMode) {
				case EXACT_MATCH:
				case PREFIX_MATCH:
					if (CharOperation.prefixEquals(this.pkgName, CharOperation.concatWith(typeRef.tokens, '.'), this.isCaseSensitive)) {
						return POSSIBLE_MATCH;
					} else {
						return IMPOSSIBLE_MATCH;
					}
				case PATTERN_MATCH:
					char[] pattern = this.pkgName[this.pkgName.length-1] == '*' ? this.pkgName : CharOperation.concat(this.pkgName, ".*".toCharArray()); //$NON-NLS-1$
					if (CharOperation.match(pattern, CharOperation.concatWith(typeRef.tokens, '.'), this.isCaseSensitive)) {
						return POSSIBLE_MATCH;
					} else {
						return IMPOSSIBLE_MATCH;
					}
				default:
					return IMPOSSIBLE_MATCH;
			}
		}
	} else {
		TypeBinding typeBinding = typeRef.binding;
		if (typeBinding == null) {
			return INACCURATE_MATCH;
		} else {
			if (typeBinding instanceof ArrayBinding) {
				typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
			}
			if (typeBinding == null) {
				return INACCURATE_MATCH;
			} else if (typeBinding instanceof ReferenceBinding) {
				PackageBinding pkgBinding = ((ReferenceBinding)typeBinding).fPackage;
				if (this.matches(pkgBinding.compoundName)) {
					return ACCURATE_MATCH;
				} else {
					return IMPOSSIBLE_MATCH;
				}
			} else {
				return IMPOSSIBLE_MATCH;
			}
		}
			
	}
}
}
