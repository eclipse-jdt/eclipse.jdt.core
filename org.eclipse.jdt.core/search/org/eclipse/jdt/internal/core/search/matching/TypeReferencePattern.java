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

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (node instanceof TypeReference) {
		return this.matchLevel((TypeReference)node, resolve);
	} else if (node instanceof NameReference) {
		return this.matchLevel((NameReference)node, resolve);
	} else if (node instanceof ImportReference) {
		return this.matchLevel((ImportReference)node, resolve);
	}
	return IMPOSSIBLE_MATCH;
}

/**
 * Returns whether this type pattern matches the given import reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(ImportReference importRef, boolean resolve) {

	if (importRef.onDemand) return IMPOSSIBLE_MATCH;

	char[][] tokens = importRef.tokens;
	int importLength = tokens.length;
	
	if (this.qualification != null){
		char[][] qualificationTokens = CharOperation.splitOn('.', this.qualification);
		int qualificationLength = qualificationTokens.length;
		if (qualificationLength+1 > importLength) return IMPOSSIBLE_MATCH;
		for (int i = 0; i < qualificationLength; i++){
			if (!this.matchesName(qualificationTokens[i], tokens[i])) {
				return IMPOSSIBLE_MATCH;
			}
		}
		if (this.simpleName == null || this.matchesName(this.simpleName, tokens[qualificationLength])) {
			return ACCURATE_MATCH;
		} else {
			return IMPOSSIBLE_MATCH;
		}
	} else {
		if (this.simpleName == null) {
			return ACCURATE_MATCH;
		} else {
			for (int i = 0; i < importLength; i++){
				if (this.matchesName(this.simpleName, tokens[i])){
					return ACCURATE_MATCH;
				}
			}
			return IMPOSSIBLE_MATCH;
		}
	}
}

/**
 * Returns whether this type pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(NameReference nameRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName == null) {
			return POSSIBLE_MATCH;
		} else {
			if (nameRef instanceof SingleNameReference) {
				if (this.matchesName(this.simpleName, ((SingleNameReference)nameRef).token)) {
					return POSSIBLE_MATCH;
				} else {
					return IMPOSSIBLE_MATCH;
				}
			} else { // QualifiedNameReference
				char[][] tokens = ((QualifiedNameReference)nameRef).tokens;
				for (int i = 0, max = tokens.length; i < max; i++){
					if (this.matchesName(this.simpleName, tokens[i])) 
						return POSSIBLE_MATCH;
				}
				return IMPOSSIBLE_MATCH;
			}				
		}
	} else {
		Binding binding = nameRef.binding;
		if (binding == null) {
			return INACCURATE_MATCH;
		} else {
			if (nameRef instanceof SingleNameReference) {
				if (binding instanceof TypeBinding) {
					return this.matchLevelForType(this.simpleName, this.qualification, (TypeBinding) binding);
				} else {
					return IMPOSSIBLE_MATCH; // must be a type binding
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
						if (lastIndex < 0) return IMPOSSIBLE_MATCH;
						break;
					case BindingIds.LOCAL : // reading a local variable
						return IMPOSSIBLE_MATCH; // no type match in it
					case BindingIds.TYPE : //=============only type ==============
						typeBinding = (TypeBinding)binding;
				}
				// try to match all enclosing types for which the token matches as well.
				while (typeBinding != null && lastIndex >= 0){
					if (this.matchesName(this.simpleName, tokens[lastIndex--])) {
						int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
						if (level != IMPOSSIBLE_MATCH) {
							return level;
						}
					}
					if (typeBinding instanceof ReferenceBinding){
						typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
					} else {
						typeBinding = null;
					}
				}
				return IMPOSSIBLE_MATCH;
			}
		}
	}
}

/**
 * Returns whether this type pattern matches the given type reference.
 * Look at resolved information only if specified.
 */
private int matchLevel(TypeReference typeRef, boolean resolve) {
	if (!resolve) {
		if (this.simpleName == null) {
			return POSSIBLE_MATCH;
		} else {
			if (typeRef instanceof SingleTypeReference) {
				if (this.matchesName(this.simpleName, ((SingleTypeReference)typeRef).token)) {
					return POSSIBLE_MATCH;
				} else {
					return IMPOSSIBLE_MATCH;
				}
			} else { // QualifiedTypeReference
				char[][] tokens = ((QualifiedTypeReference)typeRef).tokens;
				for (int i = 0, max = tokens.length; i < max; i++){
					if (this.matchesName(this.simpleName, tokens[i])) {
						return POSSIBLE_MATCH;
					}
				}
				return IMPOSSIBLE_MATCH;
			}				
		} 
	} else {
		TypeBinding typeBinding = typeRef.binding;
		if (typeBinding == null) {
			return INACCURATE_MATCH;
		} else {
			if (typeBinding instanceof ArrayBinding) typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
			if (typeRef instanceof SingleTypeReference){
				return this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
			} else { // QualifiedTypeReference
				QualifiedTypeReference qNameRef = (QualifiedTypeReference)typeRef;
				char[][] tokens = qNameRef.tokens;
				int lastIndex = tokens.length-1;
				// try to match all enclosing types for which the token matches as well.
				while (typeBinding != null && lastIndex >= 0){
					if (matchesName(this.simpleName, tokens[lastIndex--])) {
						int level = this.matchLevelForType(this.simpleName, this.qualification, typeBinding);
						if (level != IMPOSSIBLE_MATCH) {
							return level;
						}
					}
					if (typeBinding instanceof ReferenceBinding){
						typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
					} else {
						typeBinding = null;
					}
				}
				return IMPOSSIBLE_MATCH;
			} 
		}
			
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof ReferenceBinding)) return IMPOSSIBLE_MATCH;

	ReferenceBinding type = (ReferenceBinding) binding;
	int level = this.matchLevelForType(this.simpleName, this.qualification, type.superclass());
	if (level != IMPOSSIBLE_MATCH) {
		return level;
	}
	
	ReferenceBinding[] superInterfaces = type.superInterfaces();
	for (int i = 0, max = superInterfaces.length; i < max; i++){
		int newLevel = this.matchLevelForType(this.simpleName, this.qualification, superInterfaces[i]);
		switch (newLevel) {
			case IMPOSSIBLE_MATCH:
				return IMPOSSIBLE_MATCH;
			case ACCURATE_MATCH: // keep previous level
				break;
			default: // ie. INACCURATE_MATCH
				level = newLevel;
				break;
		}
	}
	return level;
}
}
