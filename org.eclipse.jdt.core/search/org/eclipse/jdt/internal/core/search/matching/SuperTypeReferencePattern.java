package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

import org.eclipse.jdt.internal.compiler.env.IBinaryType;

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
public SuperTypeReferencePattern(
	char[] superQualification,
	char[] superSimpleName,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

	this.superQualification = isCaseSensitive ? superQualification : CharOperation.toLowerCase(superQualification);
	this.superSimpleName = isCaseSensitive ? superSimpleName : CharOperation.toLowerCase(superSimpleName);
	
	this.needsResolve = superQualification != null;
}
/*
 * superRef/Object/java.lang/X/p (i.e. class p.X extends java.lang.Object)
 * superRef/Exception//X/p (i.e. class p.X extends Exception)
 */
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int slash = SUPER_REF.length - 1;
	int size = word.length;
	decodedSuperSimpleName = CharOperation.subarray(word, slash+1, slash = CharOperation.indexOf(SEPARATOR, word, slash+1));
	int oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash+1);
	if (slash == oldSlash+1){ // could not have been known at index time
		decodedSuperQualification = null;
	} else {
		decodedSuperQualification = CharOperation.subarray(word, oldSlash+1, slash);
	}
	decodedSuperClassOrInterface = word[slash+1];
	slash += 2;
	decodedSimpleName = CharOperation.subarray(word, slash+1, slash = CharOperation.indexOf(SEPARATOR, word, slash+1));
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash+1);
	if (slash == oldSlash+1){ // could not have been known at index time
		decodedEnclosingTypeName = null;
	} else {
		decodedEnclosingTypeName = CharOperation.subarray(word, oldSlash+1, slash);
	}
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash+1);
	if (slash == oldSlash+1){ // could not have been known at index time
		decodedQualification = null;
	} else {
		decodedQualification = CharOperation.subarray(word, oldSlash+1, slash);
	}
	
	decodedClassOrInterface = word[slash+1];
	decodedModifiers = (int)word[slash+2];
}
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptSuperTypeReference(path, decodedQualification, decodedSimpleName, decodedEnclosingTypeName, decodedClassOrInterface, decodedSuperQualification, decodedSuperSimpleName, decodedSuperClassOrInterface, decodedModifiers);
		}
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
public char[] indexEntryPrefix(){

	return AbstractIndexer.bestReferencePrefix(
			SUPER_REF,
			superSimpleName, 
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return CLASS;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check type name matches */
	if (superSimpleName != null){
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(superSimpleName, decodedSuperSimpleName, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(superSimpleName, decodedSuperSimpleName, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(superSimpleName, decodedSuperSimpleName, isCaseSensitive)){
					return false;
				}
		}
	}
	return true;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("SuperTypeReferencePattern: <"/*nonNLS*/);
	if (superSimpleName != null) buffer.append(superSimpleName);
	buffer.append(">, "/*nonNLS*/);
	switch(matchMode){
		case EXACT_MATCH : 
			buffer.append("exact match, "/*nonNLS*/);
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "/*nonNLS*/);
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "/*nonNLS*/);
			break;
	}
	if (isCaseSensitive)
		buffer.append("case sensitive"/*nonNLS*/);
	else
		buffer.append("case insensitive"/*nonNLS*/);
	return buffer.toString();
}

/**
 * @see SearchPattern#matchesBinary
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;
	IBinaryType type = (IBinaryType)binaryInfo;

	char[] vmName = type.getSuperclassName();
	if (vmName != null) {
		char[] superclassName = (char[])vmName.clone();
		CharOperation.replace(vmName, '/', '.');
		if (this.matchesType(this.superSimpleName, this.superQualification, superclassName)){
			return true;
		}
	}
	
	char[][] superInterfaces = type.getInterfaceNames();
	if (superInterfaces != null) {
		for (int i = 0, max = superInterfaces.length; i < max; i++) {
			char[] superInterfaceName = (char[])superInterfaces[i].clone();
			CharOperation.replace(superInterfaceName, '/', '.');
			if (this.matchesType(this.superSimpleName, this.superQualification, superInterfaceName)){
				return true;
			}
		}
	}
	return false;
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof TypeReference)) return IMPOSSIBLE_MATCH;

	TypeReference typeRef = (TypeReference)node;
	if (resolve) {
		TypeBinding binding = typeRef.binding;
		if (binding == null) {
			return INACCURATE_MATCH;
		} else {
			return this.matchLevelForType(this.superSimpleName, this.superQualification, binding);
		}
	} else {
		if (this.superSimpleName == null) {
			return POSSIBLE_MATCH;
		} else {
			char[] typeRefSimpleName = null;
			if (typeRef instanceof SingleTypeReference) {
				typeRefSimpleName = ((SingleTypeReference)typeRef).token;
			} else { // QualifiedTypeReference
				char[][] tokens = ((QualifiedTypeReference)typeRef).tokens;
				typeRefSimpleName = tokens[tokens.length-1];
			}				
			if (this.matchesName(this.superSimpleName, typeRefSimpleName))
				return POSSIBLE_MATCH;
			else
				return IMPOSSIBLE_MATCH;
		}
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof ReferenceBinding)) return IMPOSSIBLE_MATCH;

	// super class
	ReferenceBinding type = (ReferenceBinding) binding;
	int level = this.matchLevelForType(this.superSimpleName, this.superQualification, type.superclass());
	switch (level) {
		case IMPOSSIBLE_MATCH:
			break; // try to find match in super interfaces
		case ACCURATE_MATCH:
			return ACCURATE_MATCH;
		default: // ie. INACCURATE_MATCH
			break; // try to find accurate match in super interfaces
	}

	// super interfaces
	ReferenceBinding[] superInterfaces = type.superInterfaces();
	for (int i = 0, max = superInterfaces.length; i < max; i++){
		int newLevel = this.matchLevelForType(this.superSimpleName, this.superQualification, superInterfaces[i]);
		switch (newLevel) {
			case IMPOSSIBLE_MATCH:
				break;
			case ACCURATE_MATCH:
				return ACCURATE_MATCH;
			default: // ie. INACCURATE_MATCH
				level = newLevel;
				break;
		}
	}
	return level;
}
}
