package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

public class TypeDeclarationPattern extends SearchPattern {

	private char[] qualification;
	private char[][] enclosingTypeNames;
	private char[] simpleName;

	// set to CLASS_SUFFIX for only matching classes 
	// set to INTERFACE_SUFFIX for only matching interfaces
	// set to TYPE_SUFFIX for matching both classes and interfaces
	private char classOrInterface; 

	private char[] decodedQualification;
	private char[] decodedSimpleName;
	private char[][] decodedEnclosingTypeNames;
	private char decodedClassOrInterface;
public TypeDeclarationPattern(
	char[] qualification,
	char[][] enclosingTypeNames,
	char[] simpleName,
	char classOrInterface,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	if (isCaseSensitive || enclosingTypeNames == null) {
		this.enclosingTypeNames = enclosingTypeNames;
	} else {
		int length = enclosingTypeNames.length;
		this.enclosingTypeNames = new char[length][];
		for (int i = 0; i < length; i++){
			this.enclosingTypeNames[i] = CharOperation.toLowerCase(enclosingTypeNames[i]);
		}
	}
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;
	
	this.needsResolve = qualification != null;
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;

	decodedClassOrInterface = word[TYPE_DECL_LENGTH];
	int oldSlash = TYPE_DECL_LENGTH+1;
	int slash = CharOperation.indexOf(SEPARATOR, word, oldSlash+1);
	if (slash == oldSlash+1){ 
		decodedQualification = NO_CHAR;
	} else {
		decodedQualification = CharOperation.subarray(word, oldSlash+1, slash);
	}
	decodedSimpleName = CharOperation.subarray(word, slash+1, slash = CharOperation.indexOf(SEPARATOR, word, slash+1));

	if (slash+1 < size){
		decodedEnclosingTypeNames = CharOperation.splitOn('/', CharOperation.subarray(word, slash+1, size-1));
	} else {
		decodedEnclosingTypeNames = NO_CHAR_CHAR;
	}
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	boolean isClass = decodedClassOrInterface == CLASS_SUFFIX;
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path =IndexedFile.convertPath(file.getPath()))) {
			if (isClass) {
				requestor.acceptClassDeclaration(path, decodedSimpleName, decodedEnclosingTypeNames, decodedQualification);
			} else {
				requestor.acceptInterfaceDeclaration(path, decodedSimpleName, decodedEnclosingTypeNames, decodedQualification);
			}
		}
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
public char[] indexEntryPrefix(){

	return AbstractIndexer.bestTypeDeclarationPrefix(
			qualification,
			simpleName,
			classOrInterface,
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS;
}
/**
 * @see SearchPattern#matches(AstNode, boolean)
 */
protected boolean matches(AstNode node, boolean resolve) {
	if (!(node instanceof TypeDeclaration)) return false;

	TypeDeclaration type = (TypeDeclaration)node;

	// type name
	if (this.simpleName != null && !this.matchesName(this.simpleName, type.name))
		return false;

	if (resolve) {
		// fully qualified name
		TypeBinding binding = type.binding;
		if (binding != null && !this.matches(binding)) {
			return false;
		}
	}
	
	return true;
}
/**
 * @see SearchPattern#matches(Binding)
 */
public boolean matches(Binding binding) {
	if (!(binding instanceof TypeBinding)) return false;

	TypeBinding type = (TypeBinding)binding;

	// fully qualified name
	char[] enclosingTypeName = this.enclosingTypeNames == null ? null : CharOperation.concatWith(this.enclosingTypeNames, '.');
	if (!this.matchesType(this.simpleName, this.qualification, enclosingTypeName, type)) {
		return false;
	}

	// class or interface
	switch (this.classOrInterface) {
		case CLASS_SUFFIX:
			if (type.isInterface())
				return false;
			break;
		case INTERFACE_SUFFIX:
			if (!type.isInterface())
				return false;
			break;
	}
	
	return true;
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;

	IBinaryType type = (IBinaryType)binaryInfo;

	// fully qualified name
	char[] typeName = (char[])type.getName().clone();
	CharOperation.replace(typeName, '/', '.');
	char[] enclosingTypeName = this.enclosingTypeNames == null ? null : CharOperation.concatWith(this.enclosingTypeNames, '.');
	if (!this.matchesType(this.simpleName, this.qualification, enclosingTypeName, typeName)) {
		return false;
	}

	// class or interface
	switch (this.classOrInterface) {
		case CLASS_SUFFIX:
			if (type.isInterface())
				return false;
			break;
		case INTERFACE_SUFFIX:
			if (!type.isInterface())
				return false;
			break;
	}
	
	return true;
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * qualification pattern and enclosing name pattern.
 */
protected boolean matchesType(char[] simpleNamePattern, char[] qualificationPattern, char[] enclosingNamePattern, char[] fullyQualifiedTypeName) {
	if (enclosingNamePattern == null) {
		return this.matchesType(simpleNamePattern, qualificationPattern, fullyQualifiedTypeName);
	} else {
		char[] pattern;
		if (qualificationPattern == null) {
			pattern = enclosingNamePattern;
		} else {
			pattern = CharOperation.concat(qualificationPattern, enclosingNamePattern, '.');
		}
		return this.matchesType(simpleNamePattern, pattern, fullyQualifiedTypeName);
	}
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * qualification pattern and enclosing type name pattern.
 */
protected boolean matchesType(char[] simpleNamePattern, char[] qualificationPattern, char[] enclosingNamePattern, TypeBinding type) {
	if (enclosingNamePattern == null) {
		return this.matchesType(simpleNamePattern, qualificationPattern, type);
	} else {
		char[] pattern;
		if (qualificationPattern == null) {
			return matchesType(simpleNamePattern, enclosingNamePattern, type);
		} else {
			// pattern was created from a Java element: qualification is the package name.
			char[] fullQualificationPattern = CharOperation.concat(qualificationPattern, enclosingNamePattern, '.');
			return 
				this.matchesType(simpleNamePattern, fullQualificationPattern, type)
				&& CharOperation.equals(qualification, CharOperation.concatWith(type.getPackage().compoundName, '.'));
		}
	}
}
/**
 * see SearchPattern.matchIndexEntry
 */
protected boolean matchIndexEntry(){

	/* check class/interface nature */
	switch(classOrInterface){
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (classOrInterface != decodedClassOrInterface) return false;
		default :
	}
	/* check qualification - exact match only */
	if (qualification != null && !CharOperation.equals(qualification, decodedQualification, isCaseSensitive))
		return false;
	/* check enclosingTypeName - exact match only */
	if (enclosingTypeNames != null){
		// empty char[][] means no enclosing type, i.e. the decoded one is the empty char array
		if (enclosingTypeNames.length == 0){
			if (decodedEnclosingTypeNames != NO_CHAR_CHAR) return false;
		} else {
			if (!CharOperation.equals(enclosingTypeNames, decodedEnclosingTypeNames, isCaseSensitive)) return false;
		}
	}

	/* check simple name matches */
	if (simpleName != null){
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
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	switch (classOrInterface){
		case CLASS_SUFFIX :
			buffer.append("ClassDeclarationPattern: pkg<");
			break;
		case INTERFACE_SUFFIX :
			buffer.append("InterfaceDeclarationPattern: pkg<");
			break;
		default :
			buffer.append("TypeDeclarationPattern: pkg<");
			break;
	}
	if (qualification != null) buffer.append(qualification);
	buffer.append(">, enclosing<");
	if (enclosingTypeNames != null) {
		for (int i = 0; i < enclosingTypeNames.length; i++){
			buffer.append(enclosingTypeNames[i]);
			if (i < enclosingTypeNames.length - 1)
				buffer.append('.');
		}
	}
	buffer.append(">, type<");
	if (simpleName != null) buffer.append(simpleName);
	buffer.append(">, ");
	switch(matchMode){
		case EXACT_MATCH : 
			buffer.append("exact match, ");
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, ");
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, ");
			break;
	}
	if (isCaseSensitive)
		buffer.append("case sensitive");
	else
		buffer.append("case insensitive");
	return buffer.toString();
}
}
