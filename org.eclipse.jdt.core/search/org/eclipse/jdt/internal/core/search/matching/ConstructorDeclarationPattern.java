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

/**
 * The selector is unused, the constructor name is specified by the type simple name.
 */ 
public class ConstructorDeclarationPattern extends MethodDeclarationPattern {

	private char[] decodedTypeName;	
public ConstructorDeclarationPattern(char[] declaringSimpleName, int matchMode, boolean isCaseSensitive, char[] declaringQualification, char[][] parameterQualifications, char[][] parameterSimpleNames) {
	super(null, matchMode, isCaseSensitive, declaringQualification, declaringSimpleName, null, null, parameterQualifications, parameterSimpleNames);
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	decodedTypeName = CharOperation.subarray(word, CONSTRUCTOR_DECL.length, lastSeparatorIndex);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptConstructorDeclaration(path, decodedTypeName, decodedParameterCount);
		}
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestConstructorDeclarationPrefix(
			declaringSimpleName, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryMethod)) return false;

	IBinaryMethod method = (IBinaryMethod)binaryInfo;
	
	// must be a constructor
	if (!method.isConstructor()) return false;

	// declaring type
	IBinaryType declaringType = (IBinaryType)enclosingBinaryInfo;
	if (declaringType != null) {
		char[] declaringTypeName = (char[])declaringType.getName().clone();
		CharOperation.replace(declaringTypeName, '/', '.');
		if (!this.matchesType(this.declaringSimpleName, this.declaringQualification, declaringTypeName)) {
			return false;
		}
	}

	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		String methodDescriptor = new String(method.getMethodDescriptor()).replace('/', '.');
		String[] arguments = Signature.getParameterTypes(methodDescriptor);
		int argumentCount = arguments.length;
		if (parameterCount != argumentCount)
			return false;
		for (int i = 0; i < parameterCount; i++) {
			char[] qualification = this.parameterQualifications[i];
			char[] type = this.parameterSimpleNames[i];
			if (!this.matchesType(type, qualification,  Signature.toString(arguments[i]).toCharArray()))
				return false;
		}
	}

	return true;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	/* check selector matches */
	if (declaringSimpleName != null){
		switch(matchMode){
			case EXACT_MATCH :
				if (!CharOperation.equals(declaringSimpleName, decodedTypeName, isCaseSensitive)){
					return false;
				}
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(declaringSimpleName, decodedTypeName, isCaseSensitive)){
					return false;
				}
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(declaringSimpleName, decodedTypeName, isCaseSensitive)){
					return false;
				}
		}
	}
	if (parameterSimpleNames != null){
		if (parameterSimpleNames.length != decodedParameterCount) return false;
	}
	return true;
}
public String toString(){

	StringBuffer buffer = new StringBuffer(20);
	buffer.append("ConstructorDeclarationPattern: "/*nonNLS*/);
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName);
	else if (declaringQualification != null) buffer.append("*"/*nonNLS*/);

	buffer.append('(');
	if (parameterSimpleNames == null) {
		buffer.append("..."/*nonNLS*/);
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			if (i > 0) buffer.append(", "/*nonNLS*/);
			if (parameterQualifications[i] != null) buffer.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) buffer.append('*'); else buffer.append(parameterSimpleNames[i]);
		}
	}
	buffer.append(')');
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
	if (!(node instanceof ConstructorDeclaration)) return IMPOSSIBLE_MATCH;

	ConstructorDeclaration constructor = (ConstructorDeclaration)node;

	if (resolve) {
		return this.matchLevel(constructor.binding);
	} else {
		// constructor name is stored in selector field
		if (this.declaringSimpleName != null 
				&& !this.matchesName(this.declaringSimpleName, constructor.selector))
			return IMPOSSIBLE_MATCH;
			
		// parameter types
		int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (parameterCount > -1) {
			int argumentCount = constructor.arguments == null ? 0 : constructor.arguments.length;
			if (parameterCount != argumentCount)
				return IMPOSSIBLE_MATCH;
		}

		return POSSIBLE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;
	int level;

	MethodBinding constructor = (MethodBinding)binding;
	
	// must be a constructor
	if (!constructor.isConstructor()) return IMPOSSIBLE_MATCH;

	// declaring type
	ReferenceBinding declaringType = constructor.declaringClass;
	if (!constructor.isStatic() && !constructor.isPrivate()) {
		level = this.matchLevelAsSubtype(declaringType, this.declaringSimpleName, this.declaringQualification);
	} else {
		level = this.matchLevelForType(this.declaringSimpleName, this.declaringQualification, declaringType);
	}
	if (level == IMPOSSIBLE_MATCH) {
		return IMPOSSIBLE_MATCH;
	}
		
	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		int argumentCount = constructor.parameters == null ? 0 : constructor.parameters.length;
		if (parameterCount != argumentCount)
			return IMPOSSIBLE_MATCH;
		for (int i = 0; i < parameterCount; i++) {
			char[] qualification = this.parameterQualifications[i];
			char[] type = this.parameterSimpleNames[i];
			int newLevel = this.matchLevelForType(type, qualification, constructor.parameters[i]);
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
	}

	return level;
}
}
