package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

public class FieldDeclarationPattern extends SearchPattern {

	// selector	
	protected char[] name;
	
	// declaring type
	protected char[] declaringQualification;
	protected char[] declaringSimpleName;

	// type
	protected char[] typeQualification;
	protected char[] typeSimpleName;

	protected char[] decodedName;
public FieldDeclarationPattern(
	char[] name, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] typeQualification, 
	char[] typeSimpleName) {

	super(matchMode, isCaseSensitive);

	this.name = isCaseSensitive ? name : CharOperation.toLowerCase(name);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.typeQualification = isCaseSensitive ? typeQualification : CharOperation.toLowerCase(typeQualification);
	this.typeSimpleName = isCaseSensitive ? typeSimpleName : CharOperation.toLowerCase(typeSimpleName);

	this.needsResolve = this.needsResolve();
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	decodedName = CharOperation.subarray(word, FIELD_DECL.length, word.length);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptFieldDeclaration(path, decodedName);
		}
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestFieldDeclarationPrefix(
			name, 
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
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryField)) return false;

	IBinaryField field = (IBinaryField)binaryInfo;
	
	// field name
	if (!this.matchesName(this.name, field.getName()))
		return false;

	// declaring type
	IBinaryType declaringType = (IBinaryType)enclosingBinaryInfo;
	if (declaringType != null) {
		char[] declaringTypeName = (char[])declaringType.getName().clone();
		CharOperation.replace(declaringTypeName, '/', '.');
		if (!this.matchesType(this.declaringSimpleName, this.declaringQualification, declaringTypeName)) {
			return false;
		}
	}

	// field type
	String fieldTypeSignature = new String(field.getTypeName()).replace('/', '.');
	if(!this.matchesType(this.typeSimpleName, this.typeQualification, Signature.toString(fieldTypeSignature).toCharArray())) {
		return false;
	}
	
	return true;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check name matches */
	if (name != null){
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(name, decodedName, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(name, decodedName, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(name, decodedName, isCaseSensitive)){
					return false;
				}
		}
	}
	return true;
}
/**
 * Returns whether a method declaration or message send will need to be resolved to 
 * find out if this method pattern matches it.
 */
private boolean needsResolve() {

	// declaring type
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	if (typeSimpleName != null || typeQualification != null) return true;

	return false;
}
public String toString(){

	StringBuffer buffer = new StringBuffer(20);
	buffer.append("FieldDeclarationPattern: "/*nonNLS*/);
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."/*nonNLS*/);
	if (name == null) {
		buffer.append("*"/*nonNLS*/);
	} else {
		buffer.append(name);
	}
	if (typeQualification != null) 
		buffer.append(" --> "/*nonNLS*/).append(typeQualification).append('.');
	else if (typeSimpleName != null) buffer.append(" --> "/*nonNLS*/);
	if (typeSimpleName != null) 
		buffer.append(typeSimpleName);
	else if (typeQualification != null) buffer.append("*"/*nonNLS*/);
	buffer.append(", "/*nonNLS*/);
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
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof FieldDeclaration)) return IMPOSSIBLE_MATCH;

	FieldDeclaration field = (FieldDeclaration)node;

	if (resolve) {
		return this.matchLevel(field.binding);
	} else {
		if (!field.isField()) return IMPOSSIBLE_MATCH; // ignore field initializers
		
		// field name
		if (!this.matchesName(this.name, field.name))
			return IMPOSSIBLE_MATCH;

		// field type
		TypeReference fieldType = field.type;
		char[][] fieldTypeName = fieldType.getTypeName();
		char[] sourceName = this.toArrayName(
			fieldTypeName[fieldTypeName.length-1], 
			fieldType.dimensions());
		if (!this.matchesName(this.typeSimpleName, sourceName))
			return IMPOSSIBLE_MATCH;

		return POSSIBLE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof FieldBinding)) return IMPOSSIBLE_MATCH;
	int level;

	FieldBinding field = (FieldBinding)binding;
	
	// field name
	if (!this.matchesName(this.name, field.readableName()))
		return IMPOSSIBLE_MATCH;

	// declaring type
	ReferenceBinding declaringBinding = field.declaringClass;
	if (declaringBinding == null ) {
		return INACCURATE_MATCH;
	} else {
		level = this.matchLevelForType(this.declaringSimpleName, this.declaringQualification, declaringBinding);
		if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
	}

	// field type
	int newLevel = this.matchLevelForType(this.typeSimpleName, this.typeQualification, field.type);
	switch (newLevel) {
		case IMPOSSIBLE_MATCH:
			return IMPOSSIBLE_MATCH;
		case ACCURATE_MATCH: // keep previous level
			break;
		default: // ie. INACCURATE_MATCH
			level = newLevel;
			break;
	}
	
	return level;
}
}
