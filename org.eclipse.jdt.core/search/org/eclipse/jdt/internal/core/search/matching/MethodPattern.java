/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class MethodPattern extends SearchPattern {

protected boolean findDeclarations;
protected boolean findReferences;

protected char[] selector;

protected char[] declaringQualification;
protected char[] declaringSimpleName;

protected char[] returnQualification;
protected char[] returnSimpleName;

protected char[][] parameterQualifications;
protected char[][] parameterSimpleNames;

protected char[] decodedSelector;
protected int decodedParameterCount;

// extra reference info
public char[][][] allSuperDeclaringTypeNames;
protected IType declaringType;

protected char[] currentTag;

public MethodPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] selector, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] returnQualification, 
	char[] returnSimpleName,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	IType declaringType) {

	super(matchMode, isCaseSensitive);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	this.selector = isCaseSensitive ? selector : CharOperation.toLowerCase(selector);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.returnQualification = isCaseSensitive ? returnQualification : CharOperation.toLowerCase(returnQualification);
	this.returnSimpleName = isCaseSensitive ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);
	if (parameterSimpleNames != null) {
		this.parameterQualifications = new char[parameterSimpleNames.length][];
		this.parameterSimpleNames = new char[parameterSimpleNames.length][];
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
			this.parameterQualifications[i] = isCaseSensitive ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	}

	this.declaringType = declaringType;
	this.mustResolve = mustResolve();
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	if (this.currentTag ==  METHOD_REF)
		requestor.acceptMethodReference(path, this.decodedSelector, this.decodedParameterCount);
	else
		requestor.acceptMethodDeclaration(path, this.decodedSelector, this.decodedParameterCount);
}
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	this.decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.decodedSelector = CharOperation.subarray(word, this.currentTag.length, lastSeparatorIndex);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	// in the new story this will be a single call with a mask
	if (this.findReferences) {
		this.currentTag = METHOD_REF;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
	if (this.findDeclarations) {
		this.currentTag = METHOD_DECL;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
protected char[] indexEntryPrefix() {
	// will have a common pattern in the new story
	if (this.currentTag ==  METHOD_REF)
		return AbstractIndexer.bestMethodReferencePrefix(
			selector, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
	return AbstractIndexer.bestMethodDeclarationPrefix(
		selector, 
		parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
		matchMode, 
		isCaseSensitive);
}
public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {
	try {
		this.allSuperDeclaringTypeNames = 
			new SuperTypeNamesCollector(
				this, 
				this.declaringSimpleName,
				this.declaringQualification,
				locator,
				this.declaringType, 
				progressMonitor).collect();
	} catch (JavaModelException e) {
		// inaccurate matches will be found
	}
}
public boolean isPolymorphicSearch() {
	return this.findReferences;
}
/**
 * Returns whether the code gen will use an invoke virtual for 
 * this message send or not.
 */
protected boolean isVirtualInvoke(MethodBinding method, MessageSend messageSend) {
	return !method.isStatic() && !method.isPrivate() && !messageSend.isSuperAccess();
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	if (this.findReferences) {
		if (this.findDeclarations)
			return CLASS | METHOD | FIELD;
		return METHOD | FIELD;
	}
	return CLASS;
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!this.findDeclarations) return false; // only relevant when finding declarations
	if (!(binaryInfo instanceof IBinaryMethod)) return false;

	IBinaryMethod method = (IBinaryMethod) binaryInfo;
	if (!matchesName(this.selector, method.getSelector())) return false;

	// declaring type
	if (enclosingBinaryInfo != null && (this.declaringSimpleName != null || this.declaringQualification != null)) {
		IBinaryType declaring = (IBinaryType) enclosingBinaryInfo;
		char[] declaringTypeName = (char[]) declaring.getName().clone();
		CharOperation.replace(declaringTypeName, '/', '.');
		if (!matchesType(this.declaringSimpleName, this.declaringQualification, declaringTypeName))
			return false;
	}

	// parameter types
	boolean checkReturnType = this.declaringSimpleName == null && (this.returnSimpleName != null || this.returnQualification != null);
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (checkReturnType || parameterCount > -1) {
		String methodDescriptor = new String(method.getMethodDescriptor()).replace('/', '.');

		// look at return type only if declaring type is not specified
		if (checkReturnType) {
			String returnTypeSignature = Signature.toString(Signature.getReturnType(methodDescriptor));
			if (!matchesType(this.returnSimpleName, this.returnQualification, returnTypeSignature.toCharArray()))
				return false;
		}

		if (parameterCount > -1) {
			String[] arguments = Signature.getParameterTypes(methodDescriptor);
			if (parameterCount != arguments.length) return false;
			for (int i = 0; i < parameterCount; i++)
				if (!matchesType(this.parameterSimpleNames[i], this.parameterQualifications[i], Signature.toString(arguments[i]).toCharArray()))
					return false;
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (parameterSimpleNames != null && parameterSimpleNames.length != decodedParameterCount) return false;

	if (selector != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(selector, decodedSelector, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(selector, decodedSelector, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(selector, decodedSelector, isCaseSensitive);
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (resolve) {
		if (this.findReferences && node instanceof MessageSend)
			return matchLevel((MessageSend) node);
		if (this.findDeclarations && node instanceof MethodDeclaration)
			return matchLevel(((MethodDeclaration) node).binding);
		return IMPOSSIBLE_MATCH;
	}

	char[] itsSelector = null;
	AstNode[] args = null;
	TypeReference methodReturnType = null;
	if (this.findDeclarations && node instanceof MethodDeclaration) {
		MethodDeclaration method = (MethodDeclaration) node;
		itsSelector = method.selector;
		args = method.arguments;
		methodReturnType = method.returnType;
	} else if (this.findReferences && node instanceof MessageSend) {
		MessageSend messageSend = (MessageSend) node;
		itsSelector = messageSend.selector;
		args = messageSend.arguments;
	}

	if (!matchesName(this.selector, itsSelector))
		return IMPOSSIBLE_MATCH;
	if (this.parameterSimpleNames != null && args != null)
		if (this.parameterSimpleNames.length != args.length)
			return IMPOSSIBLE_MATCH;
	if (methodReturnType != null) {
		char[][] methodReturnTypeName = methodReturnType.getTypeName();
		char[] sourceName = toArrayName(
			methodReturnTypeName[methodReturnTypeName.length-1], 
			methodReturnType.dimensions());
		if (!matchesName(this.returnSimpleName, sourceName))
			return IMPOSSIBLE_MATCH;
	}
	return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;

	MethodBinding method = (MethodBinding) binding;
	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// declaring type
	int declaringLevel = !method.isStatic() && !method.isPrivate()
		? matchLevelAsSubtype(this.declaringSimpleName, this.declaringQualification, method.declaringClass)
		: matchLevelForType(this.declaringSimpleName, this.declaringQualification, method.declaringClass);
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
protected int matchLevel(MessageSend messageSend) {
	MethodBinding method = messageSend.binding;
	if (method == null) return INACCURATE_MATCH;

	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// receiver type
	int declaringLevel;
	if (isVirtualInvoke(method, messageSend) && !(messageSend.receiverType instanceof ArrayBinding)) {
		declaringLevel = matchLevelAsSubtype(this.declaringSimpleName, this.declaringQualification, method.declaringClass);
		if (declaringLevel == IMPOSSIBLE_MATCH) {
			if (method.declaringClass == null || this.allSuperDeclaringTypeNames == null) {
				declaringLevel = INACCURATE_MATCH;
			} else {
				char[][] compoundName = method.declaringClass.compoundName;
				for (int i = 0, max = this.allSuperDeclaringTypeNames.length; i < max; i++)
					if (CharOperation.equals(this.allSuperDeclaringTypeNames[i], compoundName))
						return methodLevel; // since this is an ACCURATE_MATCH so return the possibly weaker match
			}
		}
	} else {
		declaringLevel = matchLevelForType(this.declaringSimpleName, this.declaringQualification, method.declaringClass);
	}
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
/**
 * Returns whether the given reference type binding matches or is a subtype of a type
 * that matches the given simple name pattern and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve fails
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int matchLevelAsSubtype(char[] simpleNamePattern, char[] qualificationPattern, ReferenceBinding type) {
	if (type == null) return INACCURATE_MATCH;
	
	int level = this.matchLevelForType(simpleNamePattern, qualificationPattern, type);
	if (level != IMPOSSIBLE_MATCH) return level;
	
	// matches superclass
	if (!type.isInterface() && !CharOperation.equals(type.compoundName, TypeConstants.JAVA_LANG_OBJECT)) {
		level = this.matchLevelAsSubtype(simpleNamePattern, qualificationPattern, type.superclass());
		if (level != IMPOSSIBLE_MATCH) return level;
	}

	// matches interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces == null) return INACCURATE_MATCH;
	for (int i = 0; i < interfaces.length; i++) {
		level = this.matchLevelAsSubtype(simpleNamePattern, qualificationPattern, interfaces[i]);
		if (level != IMPOSSIBLE_MATCH) return level;
	}
	return IMPOSSIBLE_MATCH;
}
protected int matchMethod(MethodBinding method) {
	if (!matchesName(this.selector, method.selector)) return IMPOSSIBLE_MATCH;

	int level = ACCURATE_MATCH;
	// look at return type only if declaring type is not specified
	if (this.declaringSimpleName == null) {
		int newLevel = matchLevelForType(this.returnSimpleName, this.returnQualification, method.returnType);
		if (level > newLevel) {
			if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
			level = newLevel; // can only be downgraded
		}
	}

	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		if (method.parameters == null) return INACCURATE_MATCH;
		if (parameterCount != method.parameters.length) return IMPOSSIBLE_MATCH;
		for (int i = 0; i < parameterCount; i++) {
			int newLevel = matchLevelForType(this.parameterSimpleNames[i], this.parameterQualifications[i], method.parameters[i]);
			if (level > newLevel) {
				if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
				level = newLevel; // can only be downgraded
			}
		}
	}
	return level;
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.findReferences && reference instanceof MessageSend) {
		// message ref are starting at the selector start
		locator.report(
			(int) (((MessageSend) reference).nameSourcePosition >> 32),
			reference.sourceEnd,
			element,
			accuracy);
	} else {
		super.matchReportReference(reference, element, accuracy, locator);
	}
}
/**
 * Returns whether a method declaration or message send must be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean mustResolve() {
	// declaring type 
	// If declaring type is specified - even with simple name - always resolves 
	// (see MethodPattern.matchLevel)
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	// If return type is specified - even with simple name - always resolves 
	// (see MethodPattern.matchLevel)
	if (returnSimpleName != null || returnQualification != null) return true;

	// parameter types
	if (parameterSimpleNames != null)
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++)
			if (parameterQualifications[i] != null) return true;
	return false;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "MethodCombinedPattern: " //$NON-NLS-1$
			: "MethodDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("MethodReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null)
		buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null)
		buffer.append("*."); //$NON-NLS-1$

	if (selector != null)
		buffer.append(selector);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append('(');
	if (parameterSimpleNames == null) {
		buffer.append("..."); //$NON-NLS-1$
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
			if (i > 0) buffer.append(", "); //$NON-NLS-1$
			if (parameterQualifications[i] != null) buffer.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) buffer.append('*'); else buffer.append(parameterSimpleNames[i]);
		}
	}
	buffer.append(')');
	if (returnQualification != null) 
		buffer.append(" --> ").append(returnQualification).append('.'); //$NON-NLS-1$
	else if (returnSimpleName != null)
		buffer.append(" --> "); //$NON-NLS-1$
	if (returnSimpleName != null) 
		buffer.append(returnSimpleName);
	else if (returnQualification != null)
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(", "); //$NON-NLS-1$
	switch(matchMode) {
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
	buffer.append(isCaseSensitive ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}
