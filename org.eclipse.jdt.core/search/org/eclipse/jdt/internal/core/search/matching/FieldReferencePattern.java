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
 * @see SearchPattern#matches(AstNode, boolean)
 */
protected boolean matches(AstNode node, boolean resolve) {
	if (node instanceof FieldReference) {
		return this.matches((FieldReference)node, resolve);
	} else if (node instanceof NameReference) {
		return this.matches((NameReference)node, resolve);
	}
	return false;
}
/**
 * Returns whether this field reference pattern matches the given field reference.
 * Look at resolved information only if specified.
 */
private boolean matches(FieldReference fieldRef, boolean resolve) {	
	// field name
	if (!this.matchesName(this.name, fieldRef.token))
		return false;

	if (resolve) {
		// receiver type and field type
		FieldBinding binding = fieldRef.binding;
		if (binding != null && !this.matches(fieldRef.receiverType, fieldRef.isSuperAccess(), binding))
			return false;
	}

	return true;
}
/**
 * Returns whether this field reference pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
private boolean matches(NameReference nameRef, boolean resolve) {	
	// field name
	boolean nameMatch = true;
	if (this.name != null) {
		if (nameRef instanceof SingleNameReference) {
			nameMatch = matchesName(this.name, ((SingleNameReference)nameRef).token);
		} else { // QualifiedNameReference
			nameMatch = false;
			QualifiedNameReference qNameRef = (QualifiedNameReference)nameRef;
			char[][] tokens = qNameRef.tokens;
			for (int i = qNameRef.indexOfFirstFieldBinding-1, max = tokens.length; i < max && !nameMatch; i++){
				if (i >= 0) nameMatch = matchesName(this.name, tokens[i]);
			}
		}				
	} 
	if (!nameMatch) return false;

	if (resolve){	
		Binding binding = nameRef.binding;
		if (binding != null){
			if (nameRef instanceof SingleNameReference){
				if (binding instanceof FieldBinding){
					if (!this.matches(nameRef.receiverType, false, (FieldBinding) binding)){
						return false;
					}
				} else {
					return false; // must be a field binding
				}
			} else { // QualifiedNameReference
				QualifiedNameReference qNameRef = (QualifiedNameReference)nameRef;
				TypeBinding receiverType = qNameRef.receiverType;
				FieldBinding fieldBinding = null;
				if (!(binding instanceof FieldBinding 
						&& matchesName(this.name, (fieldBinding = (FieldBinding)binding).name) 
						&& matches(receiverType, false, fieldBinding))){
					if (binding instanceof VariableBinding){
						receiverType = ((VariableBinding) binding).type;
					}
					boolean otherMatch = false;
					int otherMax = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;
					for (int i = 0; i < otherMax && !otherMatch; i++){
						FieldBinding otherBinding = qNameRef.otherBindings[i];
						if (otherBinding == null) break;
						otherMatch = matchesName(this.name, otherBinding.name) && matches(receiverType, false, otherBinding);
						receiverType = otherBinding.type;
					}
					if (!otherMatch) return false;
				}
			}
		}
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
 * Finds out whether the given ast node matches this search pattern.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 * Returns TRUSTED_MATCH if it matches exactly this search pattern (ie. 
 * it doesn't need to be resolved or it has already been resolved.)
 * Returns POSSIBLE_MATCH if it potentially matches 
 * this search pattern and it needs to be resolved to get more information.
 */
public int matchLevel(AstNode node) {
	if (this.matches(node, false)) {
		if (this.needsResolve 
			|| node instanceof NameReference) { // ensure it is a field
			return POSSIBLE_MATCH;
		} else {
			return TRUSTED_MATCH;
		}
	}
	return IMPOSSIBLE_MATCH;
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
	buffer.append("FieldReferencePattern: "/*nonNLS*/);
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."/*nonNLS*/);
	if (name != null) {
		buffer.append(name);
	} else {
		buffer.append("*"/*nonNLS*/);
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

public boolean initializeFromLookupEnvironment(LookupEnvironment env) {
	this.allSuperDeclaringTypeNames = this.collectSuperTypeNames(this.declaringQualification, this.declaringSimpleName, this.matchMode, env);
	return this.allSuperDeclaringTypeNames == null || this.allSuperDeclaringTypeNames != NOT_FOUND_DECLARING_TYPE; 
}

/**
 * Returns whether this field reference pattern matches the given field binding and receiver type.
 */
private boolean matches(TypeBinding receiverType, boolean isSuperAccess, FieldBinding binding) {
	// receiver type
	ReferenceBinding receiverBinding = 
		isSuperAccess || binding.isStatic() ? 
			binding.declaringClass : 
			(ReferenceBinding)receiverType;
	if (receiverBinding != null 
		&& !this.matchesAsSubtype(receiverBinding, this.declaringSimpleName, this.declaringQualification)
		&& !this.matchesType(this.allSuperDeclaringTypeNames, receiverBinding)) {
			return false;
	}

	// field type
	if (!this.matchesType(this.typeSimpleName, this.typeQualification, binding.type))
		return false;

	return true;
}
}
