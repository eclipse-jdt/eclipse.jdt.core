package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

public class TypeReferencePattern extends MultipleSearchPattern {

	private char[] qualification;
	private char[] simpleName;

	private char[] decodedQualification;
	private char[] decodedSimpleName;

	private static char[][] TAGS = { TYPE_REF, SUPER_REF, REF, CONSTRUCTOR_REF };
	private static char[][] REF_TAGS = { REF };

	/* Optimization: case where simpleName == null */
	private char[][] segments;
	private int currentSegment;
	private char[] decodedSegment;
public TypeReferencePattern(
	char[] qualification,
	char[] simpleName,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null) {
		this.segments = CharOperation.splitOn('.', qualification);
	}
	
	this.needsResolve = qualification != null;
}
/**
 * Either decode ref/name, typeRef/name or superRef/superName/name
 */ 
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int tagLength = currentTag.length;
	int nameLength = CharOperation.indexOf(SEPARATOR, word, tagLength);
	if (nameLength < 0) nameLength = size;
	if (this.simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		this.decodedSegment = CharOperation.subarray(word, tagLength, nameLength);
	} else {
		this.decodedSimpleName = CharOperation.subarray(word, tagLength, nameLength);
	}
}
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
				requestor.acceptTypeReference(path, decodedSimpleName);
			}
		}
	}
}
protected char[][] getPossibleTags(){
	if (this.simpleName == null) {
		return REF_TAGS;
	} else {
		return TAGS;
	}
}
/**
 * @see AndPattern#hasNextQuery
 */
protected boolean hasNextQuery() {
	if (this.simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		if (this.segments.length > 2) {
			// if package has more than 2 segments, don't look at the first 2 since they are mostly
			// redundant (eg. in 'org.eclipse.jdt.core.*', 'com.ibm' is used all the time)
			return --this.currentSegment >= 2;
		} else {
			return --this.currentSegment >= 0;
		}
	} else {
		return false;
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
public char[] indexEntryPrefix(){

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
 * @see SearchPattern#matches(AstNode, boolean)
 */
protected boolean matches(AstNode node, boolean resolve) {
	if (node instanceof TypeReference) {
		return this.matches((TypeReference)node, resolve);
	} else if (node instanceof NameReference) {
		return this.matches((NameReference)node, resolve);
	} else if (node instanceof ImportReference) {
		return this.matches((ImportReference)node, resolve);
	}
	return false;
}
/**
 * Returns whether this type pattern matches the given import reference.
 * Look at resolved information only if specified.
 */
private boolean matches(ImportReference importRef, boolean resolve) {

	if (importRef.onDemand) return false;

	char[][] tokens = importRef.tokens;
	int importLength = tokens.length;
	
	if (this.qualification != null){
		char[][] qualificationTokens = CharOperation.splitOn('.', this.qualification);
		int qualificationLength = qualificationTokens.length;
		if (qualificationLength+1 > importLength) return false;
		for (int i = 0; i < qualificationLength; i++){
			if (!this.matchesName(qualificationTokens[i], tokens[i])) {
				return false;
			}
		}
		if (this.simpleName != null 
			&& !this.matchesName(this.simpleName, tokens[qualificationLength])) {
			return false;
		}
	} else {
		if (this.simpleName != null) {
			for (int i = 0; i < importLength; i++){
				if (this.matchesName(this.simpleName, tokens[i])){
					return true;
				}
			}
			return false;
		}
	}
	return true;
}
/**
 * Returns whether this type pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
private boolean matches(NameReference nameRef, boolean resolve) {
	Binding binding = nameRef.binding;
	if (!resolve || binding == null || !binding.isValidBinding()) {
		if (this.simpleName != null) {
			if (nameRef instanceof SingleNameReference) {
				return this.matchesName(this.simpleName, ((SingleNameReference)nameRef).token);
			} else { // QualifiedNameReference
				char[][] tokens = ((QualifiedNameReference)nameRef).tokens;
				for (int i = 0, max = tokens.length; i < max; i++){
					if (this.matchesName(this.simpleName, tokens[i])) return true;
				}
				return false;
			}				
		}
	} else {
		if (nameRef instanceof SingleNameReference){
			if (binding instanceof TypeBinding){
				if (!this.matchesType(this.simpleName, this.qualification, (TypeBinding) binding)){
					return false;
				}
			} else {
				return false; // must be a type binding
			}
		} else { // QualifiedNameReference
			TypeBinding typeBinding = null;
			QualifiedNameReference qNameRef = (QualifiedNameReference)nameRef;
			char[][] tokens = qNameRef.tokens;
			int lastIndex = tokens.length-1;
			switch (qNameRef.bits & Statement.RestrictiveFlagMASK) {
				case BindingIds.FIELD : // reading a field
					typeBinding = ((FieldBinding)binding).declaringClass;
					// no valid match amongst fields
					int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
					lastIndex -= otherBindingsCount + 1;
					if (lastIndex < 0) return false;
					break;
				case BindingIds.LOCAL : // reading a local variable
					return false; // no type match in it
				case BindingIds.TYPE : //=============only type ==============
					typeBinding = (TypeBinding)binding;
			}
			// try to match all enclosing types for which the token matches as well.
			while (typeBinding != null && lastIndex >= 0){
				if (matchesName(this.simpleName, tokens[lastIndex--])
					&& matchesType(this.simpleName, this.qualification, typeBinding)) return true;
					//&& matchesAsSubtype(this.simpleName, this.qualification, typeBinding)) return true;
				if (typeBinding instanceof ReferenceBinding){
					typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
				} else {
					typeBinding = null;
				}
			}
			return false;
		} 
	}
	return true;
}
/**
 * Returns whether this type pattern matches the given type reference.
 * Look at resolved information only if specified.
 */
private boolean matches(TypeReference typeRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName != null) {
			if (typeRef instanceof SingleTypeReference) {
				return this.matchesName(this.simpleName, ((SingleTypeReference)typeRef).token);
			} else { // QualifiedTypeReference
				char[][] tokens = ((QualifiedTypeReference)typeRef).tokens;
				for (int i = 0, max = tokens.length; i < max; i++){
					if (this.matchesName(this.simpleName, tokens[i])) return true;
				}
				return false;
			}				
		}
	} else {

		TypeBinding typeBinding = typeRef.binding;
		if (typeBinding != null){
			if (typeBinding instanceof ArrayBinding) typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
			if (typeRef instanceof SingleTypeReference){
				if (!this.matchesType(this.simpleName, this.qualification, typeBinding)){
					return false;
				}
			} else { // QualifiedTypeReference
				QualifiedTypeReference qNameRef = (QualifiedTypeReference)typeRef;
				char[][] tokens = qNameRef.tokens;
				int lastIndex = tokens.length-1;
				// try to match all enclosing types for which the token matches as well.
				while (typeBinding != null && lastIndex >= 0){
					if (matchesName(this.simpleName, tokens[lastIndex--])
						&& matchesType(this.simpleName, this.qualification, typeBinding)) return true;
						//&& matchesAsSubtype(this.simpleName, this.qualification, typeBinding)) return true;
					if (typeBinding instanceof ReferenceBinding){
						typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
					} else {
						typeBinding = null;
					}
				}
				return false;
			} 
		}
			
	}
	return true;
}
/**
 * @see SearchPattern#matches(Binding)
 */
public boolean matches(Binding binding) {
	if (!(binding instanceof ReferenceBinding)) return false;

	ReferenceBinding type = (ReferenceBinding) binding;
	if (this.matchesType(this.simpleName, this.qualification, type.superclass())){
		return true;
	}
	
	ReferenceBinding[] superInterfaces = type.superInterfaces();
	for (int i = 0, max = superInterfaces.length; i < max; i++){
		if (this.matchesType(this.simpleName, this.qualification, superInterfaces[i])){
			return true;
		}
	}
	return false;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check type name matches */
	if (simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
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
	} else {
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(simpleName, decodedSimpleName, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(simpleName, decodedSimpleName, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(simpleName, decodedSimpleName, isCaseSensitive)){
					return false;
				}
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchLevel
 */
public int matchLevel(AstNode node) {
	if (node instanceof NameReference) {
		if (this.matches((NameReference)node, false)) {
			return POSSIBLE_MATCH; // always need to resolve name reference
		} else {
			return IMPOSSIBLE_MATCH;
		}
	} else if (node instanceof ImportReference) {
		if (this.matches((ImportReference)node, false)) {
			return POSSIBLE_MATCH;
		} else {
			return IMPOSSIBLE_MATCH;
		}
	} else {
		return super.matchLevel(node);
	}
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (reference instanceof QualifiedNameReference) {
		this.matchReportReference((QualifiedNameReference)reference, element, accuracy, locator);
	} else if (reference instanceof QualifiedTypeReference) {
		this.matchReportReference((QualifiedTypeReference)reference, element, accuracy, locator);
	} else {
		super.matchReportReference(reference, element, accuracy, locator);
	}
}
/**
 * Reports the match of the given qualified name reference.
 */
private void matchReportReference(QualifiedNameReference nameRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] qualifiedName = CharOperation.splitOn('.', 
		this.qualification == null ? 
			this.simpleName :
			CharOperation.concat(this.qualification, this.simpleName, '.'));
	locator.reportQualifiedReference(nameRef.sourceStart, nameRef.sourceEnd, qualifiedName, element, accuracy);
}
/**
 * Reports the match of the given qualified type reference.
 */
private void matchReportReference(QualifiedTypeReference typeRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] qualifiedName = CharOperation.splitOn('.', CharOperation.concat(this.qualification, this.simpleName, '.'));
	locator.reportQualifiedReference(typeRef.sourceStart, typeRef.sourceEnd, qualifiedName, element, accuracy);
}
/**
 * @see AndPattern#resetQuery
 */
protected void resetQuery() {
	if (this.simpleName == null) {
		/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
		this.currentSegment = this.segments.length - 1;
	}
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("TypeReferencePattern: pkg<"/*nonNLS*/);
	if (qualification != null) buffer.append(qualification);
	buffer.append(">, type<"/*nonNLS*/);
	if (simpleName != null) buffer.append(simpleName);
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
}
