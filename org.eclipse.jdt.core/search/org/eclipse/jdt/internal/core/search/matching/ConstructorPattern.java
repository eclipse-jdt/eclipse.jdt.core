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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class ConstructorPattern extends SearchPattern {

protected boolean findDeclarations;
protected boolean findReferences;

protected char[] declaringQualification;
protected char[] declaringSimpleName;

protected char[][] parameterQualifications;
protected char[][] parameterSimpleNames;

protected char[] decodedTypeName;
protected int decodedParameterCount;

// extra reference info
protected IType declaringType;

protected char[] currentTag;

public ConstructorPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] declaringSimpleName,
	int matchMode,
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[][] parameterQualifications,
	char[][] parameterSimpleNames,
	IType declaringType) {

	super(matchMode, isCaseSensitive);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
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
	if (this.currentTag ==  CONSTRUCTOR_REF)
		requestor.acceptConstructorReference(path, this.decodedTypeName, this.decodedParameterCount);
	else
		requestor.acceptConstructorDeclaration(path, this.decodedTypeName, this.decodedParameterCount);
}
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	this.decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.decodedTypeName = CharOperation.subarray(word, currentTag.length, lastSeparatorIndex);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	// in the new story this will be a single call with a mask
	if (this.findReferences) {
		this.currentTag = CONSTRUCTOR_REF;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
	if (this.findDeclarations) {
		this.currentTag = CONSTRUCTOR_DECL;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
protected char[] indexEntryPrefix() {
	// will have a common pattern in the new story
	if (currentTag ==  CONSTRUCTOR_REF)
		return AbstractIndexer.bestConstructorReferencePrefix(
			declaringSimpleName, 
			parameterSimpleNames == null ? -1 : parameterSimpleNames.length, 
			matchMode, 
			isCaseSensitive);
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
	if (!this.findDeclarations) return false; // only relevant for declarations
	if (!(binaryInfo instanceof IBinaryMethod)) return false;

	IBinaryMethod method = (IBinaryMethod) binaryInfo;
	if (!method.isConstructor()) return false;

	// declaring type
	if (enclosingBinaryInfo != null) {
		IBinaryType declaringType = (IBinaryType) enclosingBinaryInfo;
		char[] declaringTypeName = (char[]) declaringType.getName().clone();
		CharOperation.replace(declaringTypeName, '/', '.');
		if (!matchesType(this.declaringSimpleName, this.declaringQualification, declaringTypeName))
			return false;
	}

	// parameter types
	int parameterCount = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
	if (parameterCount > -1) {
		String methodDescriptor = new String(method.getMethodDescriptor()).replace('/', '.');
		String[] arguments = Signature.getParameterTypes(methodDescriptor);
		if (parameterCount != arguments.length) return false;
		for (int i = 0; i < parameterCount; i++)
			if (!matchesType(this.parameterSimpleNames[i], this.parameterQualifications[i],  Signature.toString(arguments[i]).toCharArray()))
				return false;
	}
	return true;
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	if (this.findReferences) // handles both declarations + references & just references
		return 
			COMPILATION_UNIT // implicit constructor call: case of Y extends X and Y doesn't define any constructor
			| CLASS // implicit constructor call: case of constructor declaration with no explicit super call
			| METHOD // reference in another constructor
			| FIELD; // anonymous in a field initializer

	// declarations are only found in Class
	return CLASS;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (parameterSimpleNames != null && parameterSimpleNames.length != decodedParameterCount) return false;

	if (declaringSimpleName != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(declaringSimpleName, decodedTypeName, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(declaringSimpleName, decodedTypeName, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(declaringSimpleName, decodedTypeName, isCaseSensitive);
		}
	}
	return true;
}
/**
 * Returns whether this constructor pattern  matches the given allocation expression.
 * Look at resolved information only if specified.
 */
protected int matchLevel(AllocationExpression allocation, boolean resolve) {
	// constructor name is simple type name
	char[][] typeName = allocation.type.getTypeName();
	if (this.declaringSimpleName != null && !matchesName(this.declaringSimpleName, typeName[typeName.length-1]))
		return IMPOSSIBLE_MATCH;

	if (resolve)
		return matchLevel(allocation.binding);

	// argument types
	if (this.parameterSimpleNames != null && allocation.arguments != null)
		if (this.parameterSimpleNames.length != allocation.arguments.length) return IMPOSSIBLE_MATCH;
	return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (this.findReferences) {
		if (node instanceof AllocationExpression)
			return matchLevel((AllocationExpression) node, resolve);
		if (node instanceof ExplicitConstructorCall)
			return matchLevel((ExplicitConstructorCall) node, resolve);
		if (node instanceof TypeDeclaration)
			return matchLevel((TypeDeclaration) node, resolve);
	}
	if (node instanceof ConstructorDeclaration)
		return matchLevel((ConstructorDeclaration) node, resolve);
	return IMPOSSIBLE_MATCH;
}
/**
 * @see SearchPattern#matchLevel(Binding binding).
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;

	MethodBinding method = (MethodBinding) binding;
	if (!method.isConstructor()) return IMPOSSIBLE_MATCH;

	// declaring type, simple name has already been matched by matchIndexEntry()
	int level = matchLevelForType(this.declaringSimpleName, this.declaringQualification, method.declaringClass);
	if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

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
 * Returns whether the given constructor declaration has an implicit constructor reference that matches
 * this constructor pattern.
 * Look at resolved information only if specified.
 */
protected int matchLevel(ConstructorDeclaration constructor, boolean resolve) {
	int referencesLevel = IMPOSSIBLE_MATCH;
	if (this.findReferences) {
		ExplicitConstructorCall constructorCall = constructor.constructorCall;
		if (constructorCall != null && constructorCall.accessMode == ExplicitConstructorCall.ImplicitSuper) {
			// eliminate explicit super call as it will be treated with matchLevel(ExplicitConstructorCall, boolean)
			referencesLevel = matchLevel(constructorCall, resolve);
			if (referencesLevel == ACCURATE_MATCH) return ACCURATE_MATCH; // cannot get better
		}
	}

	int declarationsLevel = IMPOSSIBLE_MATCH;
	if (this.findDeclarations) {
		if (resolve) {
			declarationsLevel = matchLevel(constructor.binding);
		} else {
			// constructor name is stored in selector field
			if (this.declaringSimpleName != null && !matchesName(this.declaringSimpleName, constructor.selector))
				return referencesLevel; // answer referencesLevel since this is an IMPOSSIBLE_MATCH

			// parameter types
			if (this.parameterSimpleNames != null && constructor.arguments != null)
				if (this.parameterSimpleNames.length != constructor.arguments.length)
					return referencesLevel; // answer referencesLevel since this is an IMPOSSIBLE_MATCH

			if (!this.mustResolve) return ACCURATE_MATCH; // cannot get better
			declarationsLevel = POTENTIAL_MATCH;
		}
	}
	return referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel; // answer the stronger match
}
/**
 * Returns whether this constructor pattern  matches the given explicit constructor call.
 * Look at resolved information only if specified.
 */
protected int matchLevel(ExplicitConstructorCall call, boolean resolve) {
	if (resolve)
		return matchLevel(call.binding);

	// argument types
	if (this.parameterSimpleNames != null && call.arguments != null)
		if (this.parameterSimpleNames.length != call.arguments.length) return IMPOSSIBLE_MATCH;
	return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
}
/**
 * Returns whether the given type declaration has an implicit constructor reference that matches
 * this constructor pattern.
 * Look at resolved information only if specified.
 */
protected int matchLevel(TypeDeclaration type, boolean resolve) {
	if (resolve) {
		// find default constructor
		AbstractMethodDeclaration[] methods = type.methods;
		if (methods != null) {
			for (int i = 0, length = methods.length; i < length; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.isDefaultConstructor() && method.sourceStart < type.bodyStart) // if synthetic
					return matchLevel((ConstructorDeclaration) method, true);
			}
		}
		return IMPOSSIBLE_MATCH;
	}

	// Need to wait for all the constructor bodies to have been parsed
	return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
}
protected boolean mustResolve() {
	if (declaringQualification != null) return true;

	// parameter types
	if (parameterSimpleNames != null)
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++)
			if (parameterQualifications[i] != null) return true;
	return false;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "ConstructorCombinedPattern: " //$NON-NLS-1$
			: "ConstructorDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("ConstructorReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null)
		buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName);
	else if (declaringQualification != null)
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