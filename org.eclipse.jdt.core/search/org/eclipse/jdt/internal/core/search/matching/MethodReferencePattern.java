package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

public class MethodReferencePattern extends MethodPattern {
	public char[][][] allSuperDeclaringTypeNames;

public MethodReferencePattern(
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
	decodedSelector = CharOperation.subarray(word, METHOD_REF.length, lastSeparatorIndex);
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		String path;
		if (file != null && scope.encloses(path = IndexedFile.convertPath(file.getPath()))) {
			requestor.acceptMethodReference(path, decodedSelector, decodedParameterCount);
		}
	}
}
public String getPatternName(){
	return "MethodReferencePattern: "; //$NON-NLS-1$
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	return AbstractIndexer.bestMethodReferencePrefix(
			selector, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return METHOD | FIELD;
}

public boolean initializeFromLookupEnvironment(LookupEnvironment env) {
	this.allSuperDeclaringTypeNames = this.collectSuperTypeNames(this.declaringQualification, this.declaringSimpleName, this.matchMode, env);
	return this.allSuperDeclaringTypeNames == null || this.allSuperDeclaringTypeNames != NOT_FOUND_DECLARING_TYPE; 
}

/**
 * Returns whether the code gen will use an invoke virtual for 
 * this message send or not.
 */
private boolean isVirtualInvoke(MethodBinding method, MessageSend messageSend) {
	return !method.isStatic() && !messageSend.isSuperAccess() && !method.isPrivate();
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof MessageSend)) return IMPOSSIBLE_MATCH;
	MessageSend messageSend = (MessageSend)node;

	if (resolve) {
		return this.matchLevel(messageSend.binding, messageSend);
	} else {
		// selector
		if (this.selector != null && !this.matchesName(this.selector, messageSend.selector))
			return IMPOSSIBLE_MATCH;
			
		// argument types
		int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
		if (argumentCount > -1) {
			int parameterCount = messageSend.arguments == null ? 0 : messageSend.arguments.length;
			if (parameterCount != argumentCount)
				return IMPOSSIBLE_MATCH;
		}

		return POSSIBLE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding, MessageSend messageSend) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;
	int level;

	MethodBinding method = (MethodBinding)binding;

	// selector
	if (this.selector != null && !this.matchesName(this.selector, method.selector))
		return IMPOSSIBLE_MATCH;

	// receiver type
	ReferenceBinding receiverType = 
		binding == null ? 
			null : 
			(!isVirtualInvoke(method, messageSend) ? method.declaringClass : (ReferenceBinding)messageSend.receiverType);
	if (this.isVirtualInvoke(method, messageSend)) {
		level = this.matchLevelAsSubtype(receiverType, this.declaringSimpleName, this.declaringQualification);
		if (level == IMPOSSIBLE_MATCH) {
			level = this.matchLevelForType(this.allSuperDeclaringTypeNames, receiverType);
			if (level == IMPOSSIBLE_MATCH) {
				return IMPOSSIBLE_MATCH;
			}
		}
	} else {
		level = this.matchLevelForType(this.declaringSimpleName, this.declaringQualification, receiverType);
		if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
	}

	// return type
	int newLevel = this.matchLevelForType(this.returnSimpleName, this.returnQualification, method.returnType);
	switch (newLevel) {
		case IMPOSSIBLE_MATCH:
			return IMPOSSIBLE_MATCH;
		case ACCURATE_MATCH: // keep previous level
			break;
		default: // ie. INACCURATE_MATCH
			level = newLevel;
			break;
	}
		
	// argument types
	int argumentCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (argumentCount > -1) {
		if (method.parameters == null) {
			level = INACCURATE_MATCH;
		} else {
			int parameterCount = method.parameters.length;
			if (parameterCount != argumentCount) return IMPOSSIBLE_MATCH;
			for (int i = 0; i < parameterCount; i++) {
				char[] qualification = this.parameterQualifications[i];
				char[] type = this.parameterSimpleNames[i];
				newLevel = this.matchLevelForType(type, qualification, method.parameters[i]);
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
	}

	return level;
}
}
