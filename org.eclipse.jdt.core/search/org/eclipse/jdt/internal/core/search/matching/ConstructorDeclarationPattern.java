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
 * @see SearchPattern#matches(AstNode, boolean)
 */
protected boolean matches(AstNode node, boolean resolve) {
	if (!(node instanceof ConstructorDeclaration)) return false;

	ConstructorDeclaration constructor = (ConstructorDeclaration)node;

	// constructor name is stored in selector field
	if (this.declaringSimpleName != null 
			&& !this.matchesName(this.declaringSimpleName, constructor.selector))
		return false;

	// declaring type
	MethodBinding binding = constructor.binding;
	if (resolve && binding != null) {
		ReferenceBinding declaringBinding = binding.declaringClass;
		if (!this.matchesType(this.declaringSimpleName, this.declaringQualification, declaringBinding))
			return false;
	}
		
	// argument types
	int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (argumentCount > -1) {
		int parameterCount = constructor.arguments == null ? 0 : constructor.arguments.length;
		if (parameterCount != argumentCount)
			return false;

		if (resolve && binding != null) {
			for (int i = 0; i < parameterCount; i++) {
				char[] qualification = this.parameterQualifications[i];
				char[] type = this.parameterSimpleNames[i];
				if (!this.matchesType(type, qualification, binding.parameters[i]))
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
	if (!(binding instanceof MethodBinding)) return false;

	MethodBinding method = (MethodBinding)binding;

	// must be a constructor
	if (!method.isConstructor()) return false;
	
	// declaring type
	ReferenceBinding declaringType = method.declaringClass;
	if (declaringType != null) {
		if (!method.isStatic() && !method.isPrivate()) {
			if (!this.matchesAsSubtype(declaringType, this.declaringSimpleName, this.declaringQualification))
				return false;
		} else {
			if (!this.matchesType(this.declaringSimpleName, this.declaringQualification, declaringType))
				return false;
		}
	}

	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		int argumentCount = method.parameters == null ? 0 : method.parameters.length;
		if (parameterCount != argumentCount)
			return false;
		for (int i = 0; i < parameterCount; i++) {
			char[] qualification = this.parameterQualifications[i];
			char[] type = this.parameterSimpleNames[i];
			if (!this.matchesType(type, qualification, method.parameters[i]))
				return false;
		}
	}

	return true;
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
	buffer.append("ConstructorDeclarationPattern: ");
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName);
	else if (declaringQualification != null) buffer.append("*");

	buffer.append('(');
	if (parameterSimpleNames == null) {
		buffer.append("...");
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			if (i > 0) buffer.append(", ");
			if (parameterQualifications[i] != null) buffer.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) buffer.append('*'); else buffer.append(parameterSimpleNames[i]);
		}
	}
	buffer.append(')');
	buffer.append(", ");
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
