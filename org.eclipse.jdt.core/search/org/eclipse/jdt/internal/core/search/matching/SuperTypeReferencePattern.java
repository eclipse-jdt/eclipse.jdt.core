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

import java.io.*;
import java.util.HashMap;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.*;

public class SuperTypeReferencePattern extends SearchPattern {

public char[] superQualification;
public char[] superSimpleName;

protected char[] decodedSuperQualification;
protected char[] decodedSuperSimpleName;
protected char decodedSuperClassOrInterface;
protected char[] decodedQualification;
protected char[] decodedSimpleName;
protected char[] decodedEnclosingTypeName;
protected char decodedClassOrInterface;
protected int decodedModifiers;

protected boolean checkOnlySuperinterfaces; // used for IMPLEMENTORS

/**
 * A map from IndexInputs to IEntryResult[]
 */
public HashMap entryResults;

private static final IEntryResult[] NO_ENTRY_RESULT = new IEntryResult[0];

public SuperTypeReferencePattern(char[] superQualification, char[] superSimpleName, int matchMode, boolean isCaseSensitive) {
	this(superQualification, superSimpleName, matchMode, isCaseSensitive, false);
}
public SuperTypeReferencePattern(
	char[] superQualification,
	char[] superSimpleName,
	int matchMode,
	boolean isCaseSensitive,
	boolean checkOnlySuperinterfaces) {

	super(matchMode, isCaseSensitive);

	this.superQualification = isCaseSensitive ? superQualification : CharOperation.toLowerCase(superQualification);
	this.superSimpleName = isCaseSensitive ? superSimpleName : CharOperation.toLowerCase(superSimpleName);
	this.mustResolve = superQualification != null;
	this.checkOnlySuperinterfaces = checkOnlySuperinterfaces; // ie. skip the superclass
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptSuperTypeReference(path, decodedQualification, decodedSimpleName, decodedEnclosingTypeName, decodedClassOrInterface, decodedSuperQualification, decodedSuperSimpleName, decodedSuperClassOrInterface, decodedModifiers);
}
/*
 * "superRef/Object/java.lang/X/p" represents "class p.X extends java.lang.Object"
 * "superRef/Exception//X/p" represents "class p.X extends Exception"
 */
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int slash = SUPER_REF.length - 1;
	this.decodedSuperSimpleName = CharOperation.subarray(word, slash + 1, slash = CharOperation.indexOf(SEPARATOR, word, slash + 1));
	int oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash + 1);
	this.decodedSuperQualification = (slash == oldSlash + 1)
		? null // could not have been known at index time
		: CharOperation.subarray(word, oldSlash + 1, slash);
	this.decodedSuperClassOrInterface = word[slash + 1];
	slash += 2;
	this.decodedSimpleName = CharOperation.subarray(word, slash + 1, slash = CharOperation.indexOf(SEPARATOR, word, slash + 1));
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash + 1);
	if (slash == oldSlash + 1) { // could not have been known at index time
		this.decodedEnclosingTypeName = null;
	} else {
		this.decodedEnclosingTypeName = (slash == oldSlash + 2 && word[oldSlash + 1] == ONE_ZERO[0])
			? ONE_ZERO
			: CharOperation.subarray(word, oldSlash + 1, slash);
	}
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash + 1);
	this.decodedQualification = (slash == oldSlash + 1)
		? null // could not have been known at index time
		: CharOperation.subarray(word, oldSlash + 1, slash);
	this.decodedClassOrInterface = word[slash + 1];
	this.decodedModifiers = word[slash + 2]; // implicit cast to int type
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (this.entryResults == null) {
		// non-optimized case
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);	
		return;
	}

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	/* narrow down a set of entries using prefix criteria */
	IEntryResult[] entries = (IEntryResult[]) this.entryResults.get(input);
	if (entries == null) {
		entries = input.queryEntriesPrefixedBy(SUPER_REF);
		if (entries == null)
			entries = NO_ENTRY_RESULT;
		this.entryResults.put(input, entries);
	}
	if (entries == NO_ENTRY_RESULT) return;

	/* only select entries which actually match the entire search pattern */
	int slash = SUPER_REF.length;
	char[] simpleName = this.superSimpleName;
	int length = simpleName == null ? 0 : simpleName.length;
	nextEntry: for (int i = 0, max = entries.length; i < max; i++) {
		/* check that the entry is a super ref to the super simple name */
		IEntryResult entry = entries[i];
		if (simpleName != null) {
			char[] word = entry.getWord();
			if (slash + length >= word.length) continue;
			
			// ensure it is the end of the ref (a simple name is not a prefix of ref)
			if (word[length + slash] != '/') continue; 
			
			// compare ref to simple name
			for (int j = 0; j < length; j++)
				if (word[j + slash] != simpleName[j]) continue nextEntry;
		}

		/* retrieve and decode entry */	
		decodeIndexEntry(entry);
		feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
protected char[] indexEntryPrefix(){
	return AbstractIndexer.bestReferencePrefix(SUPER_REF, superSimpleName, matchMode, isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return CLASS;
}
/**
 * @see SearchPattern#matchesBinary
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;
	IBinaryType type = (IBinaryType) binaryInfo;
	if (!this.checkOnlySuperinterfaces) {
		char[] vmName = type.getSuperclassName();
		if (vmName != null) {
			char[] superclassName = (char[]) vmName.clone();
			CharOperation.replace(vmName, '/', '.');
			if (matchesType(this.superSimpleName, this.superQualification, superclassName))
				return true;
		}
	}

	char[][] superInterfaces = type.getInterfaceNames();
	if (superInterfaces != null) {
		for (int i = 0, max = superInterfaces.length; i < max; i++) {
			char[] superInterfaceName = (char[]) superInterfaces[i].clone();
			CharOperation.replace(superInterfaceName, '/', '.');
			if (matchesType(this.superSimpleName, this.superQualification, superInterfaceName))
				return true;
		}
	}
	return false;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (this.checkOnlySuperinterfaces)
		if (this.decodedSuperClassOrInterface != IIndexConstants.INTERFACE_SUFFIX) return false;

	if (this.superSimpleName != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(this.superSimpleName, this.decodedSuperSimpleName, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(this.superSimpleName, this.decodedSuperSimpleName, this.isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(this.superSimpleName, this.decodedSuperSimpleName, this.isCaseSensitive);
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof TypeReference)) return IMPOSSIBLE_MATCH;

	TypeReference typeRef = (TypeReference) node;
	if (resolve) {
		TypeBinding binding = typeRef.resolvedType;
		if (binding == null) return INACCURATE_MATCH;
		return matchLevelForType(this.superSimpleName, this.superQualification, binding);
	}

	if (this.superSimpleName == null)
		return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;

	char[] typeRefSimpleName = null;
	if (typeRef instanceof SingleTypeReference) {
		typeRefSimpleName = ((SingleTypeReference) typeRef).token;
	} else { // QualifiedTypeReference
		char[][] tokens = ((QualifiedTypeReference) typeRef).tokens;
		typeRefSimpleName = tokens[tokens.length-1];
	}				

	if (matchesName(this.superSimpleName, typeRefSimpleName))
		return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
	return IMPOSSIBLE_MATCH;
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof ReferenceBinding)) return IMPOSSIBLE_MATCH;

	ReferenceBinding type = (ReferenceBinding) binding;
	int level = IMPOSSIBLE_MATCH;
	if (!this.checkOnlySuperinterfaces) {
		level = matchLevelForType(this.superSimpleName, this.superQualification, type.superclass());
		if (level == ACCURATE_MATCH) return ACCURATE_MATCH;
	}

	ReferenceBinding[] superInterfaces = type.superInterfaces();
	for (int i = 0, max = superInterfaces.length; i < max; i++) {
		int newLevel = matchLevelForType(this.superSimpleName, this.superQualification, superInterfaces[i]);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel;
		}
	}
	return level;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append(
		this.checkOnlySuperinterfaces
			? "SuperInterfaceReferencePattern: <" //$NON-NLS-1$
			: "SuperTypeReferencePattern: <"); //$NON-NLS-1$
	if (superSimpleName != null) buffer.append(superSimpleName);
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
