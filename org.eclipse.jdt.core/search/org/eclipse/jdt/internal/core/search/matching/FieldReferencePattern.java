package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

public class FieldReferencePattern extends MultipleSearchPattern {

	// selector	
	protected char[] name;
	
	// declaring type
	protected char[] declaringQualification;
	protected char[] declaringSimpleName;

	// type
	protected char[] typeQualification;
	protected char[] typeSimpleName;

	protected char[] decodedName;

	private static char[][] TAGS = { FIELD_REF, REF };
	public char[][][] allSuperDeclaringTypeNames;

public FieldReferencePattern(
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
/**
 * Either decode ref/name, fieldRef/name 
 */ 
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int tagLength = currentTag.length;
	int nameLength = CharOperation.indexOf(SEPARATOR, word, tagLength);
	if (nameLength < 0) nameLength = size;
	decodedName = CharOperation.subarray(word, tagLength, nameLength);}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	if (currentTag == REF) {
		foundAmbiguousIndexMatches = true;
	}
	for (int i = 0, max = references.length; i < max; i++) {
		int reference = references[i];
		if (reference != -1) { // if the reference has not been eliminated
			IndexedFile file = input.getIndexedFile(reference);
			String path;
			if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
				requestor.acceptFieldReference(path, decodedName);
			}
		}
	}
}
protected char[][] getPossibleTags(){
	return TAGS;
}
/**
 * @see AndPattern#hasNextQuery
 */
protected boolean hasNextQuery() {
	return false;
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
public char[] indexEntryPrefix(){

	return AbstractIndexer.bestReferencePrefix(
			currentTag,
			name,
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return METHOD | FIELD;
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
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[] declaringTypeName = CharOperation.concat(this.declaringQualification, this.declaringSimpleName, '.');
	char[][] qualifiedName = CharOperation.splitOn('.', CharOperation.concat(declaringTypeName, this.name, '.'));
	locator.reportQualifiedReference(reference.sourceStart, reference.sourceEnd, qualifiedName, element, accuracy);
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
/**
 * @see AndPattern#resetQuery
 */
protected void resetQuery() {
}
public String toString(){

	StringBuffer buffer = new StringBuffer(20);
	buffer.append("FieldReferencePattern: "); //$NON-NLS-1$
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."); //$NON-NLS-1$
	if (name != null) {
		buffer.append(name);
	} else {
		buffer.append("*"); //$NON-NLS-1$
	}
	if (typeQualification != null) 
		buffer.append(" --> ").append(typeQualification).append('.'); //$NON-NLS-1$
	else if (typeSimpleName != null) buffer.append(" --> "); //$NON-NLS-1$
	if (typeSimpleName != null) 
		buffer.append(typeSimpleName);
	else if (typeQualification != null) buffer.append("*"); //$NON-NLS-1$
	buffer.append(", "); //$NON-NLS-1$
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

public boolean initializeFromLookupEnvironment(LookupEnvironment env) {
	this.allSuperDeclaringTypeNames = this.collectSuperTypeNames(this.declaringQualification, this.declaringSimpleName, this.matchMode, env);
	return this.allSuperDeclaringTypeNames == null || this.allSuperDeclaringTypeNames != NOT_FOUND_DECLARING_TYPE; 
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof FieldReference) {
		return this.matchLevel((FieldReference)node, resolve);
	} else if (node instanceof NameReference) {
		return this.matchLevel((NameReference)node, resolve);
	}
	return IMPOSSIBLE_MATCH;
}

/**
 * Returns whether this field reference pattern matches the given field reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(FieldReference fieldRef, boolean resolve) {	
	// field name
	if (!this.matchesName(this.name, fieldRef.token))
		return IMPOSSIBLE_MATCH;

	if (resolve) {
		// receiver type and field type
		return this.matchLevel(fieldRef.receiverType, fieldRef.isSuperAccess(), fieldRef.binding);
	} else {
		return POSSIBLE_MATCH;
	}
}

/**
 * Returns whether this field reference pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(NameReference nameRef, boolean resolve) {	
	// field name
	boolean nameMatch = true;
	if (this.name != null) {
		if (nameRef instanceof SingleNameReference) {
			nameMatch = this.matchesName(this.name, ((SingleNameReference)nameRef).token);
		} else { // QualifiedNameReference
			nameMatch = false;
			QualifiedNameReference qNameRef = (QualifiedNameReference)nameRef;
			char[][] tokens = qNameRef.tokens;
			for (int i = qNameRef.indexOfFirstFieldBinding-1, max = tokens.length; i < max && !nameMatch; i++){
				if (i >= 0) nameMatch = this.matchesName(this.name, tokens[i]);
			}
		}				
	} 
	if (!nameMatch) return IMPOSSIBLE_MATCH;

	if (resolve) {	
		Binding binding = nameRef.binding;
		if (binding == null) {
			return INACCURATE_MATCH;
		} else {
			if (nameRef instanceof SingleNameReference){
				if (binding instanceof FieldBinding){
					return this.matchLevel(nameRef.receiverType, false, (FieldBinding) binding);
				} else {
					return IMPOSSIBLE_MATCH; // must be a field binding
				}
			} else { // QualifiedNameReference
				QualifiedNameReference qNameRef = (QualifiedNameReference)nameRef;
				TypeBinding receiverType = qNameRef.receiverType;
				FieldBinding fieldBinding = null;
				if (binding instanceof FieldBinding && this.matchesName(this.name, (fieldBinding = (FieldBinding)binding).name)) {
					return this.matchLevel(receiverType, false, fieldBinding);
				} else {
					if (binding instanceof VariableBinding){
						receiverType = ((VariableBinding) binding).type;
					}
					int otherLevel = IMPOSSIBLE_MATCH;
					int otherMax = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;
					for (int i = 0; i < otherMax && (otherLevel == IMPOSSIBLE_MATCH); i++){
						FieldBinding otherBinding = qNameRef.otherBindings[i];
						if (this.matchesName(this.name, otherBinding.name)) {
							otherLevel = this.matchLevel(receiverType, false, otherBinding);
						}
						receiverType = otherBinding.type;
					}
					return otherLevel;
				}
			}
		}
	} else {
		return POSSIBLE_MATCH;
	}
}

/**
 * Returns whether this field reference pattern matches the given field binding and receiver type.
 */
private int matchLevel(TypeBinding receiverType, boolean isSuperAccess, FieldBinding binding) {
	if (receiverType == null || binding == null) return INACCURATE_MATCH;
	int level;
	
	// receiver type
	ReferenceBinding receiverBinding = 
		isSuperAccess || binding.isStatic() ? 
			binding.declaringClass : 
			(ReferenceBinding)receiverType;
	if (receiverBinding == null) {
		return INACCURATE_MATCH;
	} else {
		level = this.matchLevelAsSubtype(receiverBinding, this.declaringSimpleName, this.declaringQualification);
		if (level == IMPOSSIBLE_MATCH) {
			level = this.matchLevelForType(this.allSuperDeclaringTypeNames, receiverBinding);
			if (level == IMPOSSIBLE_MATCH) {
				return IMPOSSIBLE_MATCH;
			}
		}
	}

	// field type
	int newLevel = this.matchLevelForType(this.typeSimpleName, this.typeQualification, binding.type);
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
