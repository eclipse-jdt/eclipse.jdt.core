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

public class MethodDeclarationPattern extends MethodPattern {
public MethodDeclarationPattern(
	char[] selector, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] returnQualification, 
	char[] returnSimpleName,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames) {

	super(matchMode, isCaseSensitive);

	this.selector = isCaseSensitive ? selector : CharOperation.toLowerCase(selector);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.returnQualification = isCaseSensitive ? returnQualification : CharOperation.toLowerCase(returnQualification);
	this.returnSimpleName = isCaseSensitive ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);

	if (parameterSimpleNames != null){
		this.parameterQualifications = new char[parameterSimpleNames.length][];
		this.parameterSimpleNames = new char[parameterSimpleNames.length][];
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++){
			this.parameterQualifications[i] = isCaseSensitive ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	}	
	this.needsResolve = this.needsResolve();
}
public void decodeIndexEntry(IEntryResult entryResult){

	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	decodedSelector = CharOperation.subarray(word, METHOD_DECL.length, lastSeparatorIndex);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptMethodDeclaration(path, decodedSelector, decodedParameterCount);
		}
	}
}
public String getPatternName(){
	return "MethodDeclarationPattern: "/*nonNLS*/;
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestMethodDeclarationPrefix(
			selector, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
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
 * @see SearchPattern#matches(AstNode, boolean)
 */
protected boolean matches(AstNode node, boolean resolve) {
	if (!(node instanceof MethodDeclaration)) return false;

	MethodDeclaration method = (MethodDeclaration)node;
	
	// selector
	if (!this.matchesName(this.selector, method.selector))
		return false;

	// declaring type
	MethodBinding binding = method.binding;
	if (resolve && binding != null) {
		ReferenceBinding declaringType = binding.declaringClass;
		if (declaringType != null) {
			if (!binding.isStatic() && !binding.isPrivate()) {
				if (!this.matchesAsSubtype(declaringType, this.declaringSimpleName, this.declaringQualification))
					return false;
			} else {
				if (!this.matchesType(this.declaringSimpleName, this.declaringQualification, declaringType))
					return false;
			}
		}
	}

	// return type
	if (this.returnQualification == null) {
		if (this.returnSimpleName != null) {
			TypeReference methodReturnType = method.returnType;
			if (methodReturnType != null) {
				char[][] methodReturnTypeName = methodReturnType.getTypeName();
				char[] sourceName = this.toArrayName(
					methodReturnTypeName[methodReturnTypeName.length-1], 
					methodReturnType.dimensions());
				if (!this.matchesName(this.returnSimpleName, sourceName))
					return false;
			}
		}
	} else {
		if (resolve 
				&& binding != null 
				&& !this.matchesType(this.returnSimpleName, this.returnQualification, binding.returnType))
			return false;
	}
		
	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		int argumentCount = method.arguments == null ? 0 : method.arguments.length;
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
	
	// selector
	if (!this.matchesName(this.selector, method.selector))
		return false;

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

	// return type
	if (!this.matchesType(this.returnSimpleName, this.returnQualification, method.returnType)) {
		return false;
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
	
	// selector
	if (!this.matchesName(this.selector, method.getSelector()))
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

	// return type
	String methodDescriptor = new String(method.getMethodDescriptor()).replace('/', '.');
	String returnTypeSignature = Signature.toString(Signature.getReturnType(methodDescriptor));
	if (!this.matchesType(this.returnSimpleName, this.returnQualification, returnTypeSignature.toCharArray())) {
		return false;
	}
		
	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		String[] arguments = Signature.getParameterTypes(methodDescriptor);
		int argumentCount = arguments.length;
		if (parameterCount != argumentCount)
			return false;
		for (int i = 0; i < parameterCount; i++) {
			char[] qualification = this.parameterQualifications[i];
			char[] type = this.parameterSimpleNames[i];
			if (!this.matchesType(type, qualification, Signature.toString(arguments[i]).toCharArray()))
				return false;
		}
	}

	return true;
}
}
