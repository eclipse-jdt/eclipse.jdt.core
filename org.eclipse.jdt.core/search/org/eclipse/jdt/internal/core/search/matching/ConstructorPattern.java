/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.EntryResult;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.util.Util;

public class ConstructorPattern extends JavaSearchPattern implements IIndexConstants {

protected boolean findDeclarations;
protected boolean findReferences;

public char[] declaringQualification;
public char[] declaringSimpleName;

public char[][] parameterQualifications;
public char[][] parameterSimpleNames;
public int parameterCount;
public int flags = 0;

// Signatures and arguments for generic search
char[][][] parametersTypeSignatures;
char[][][][] parametersTypeArguments;
boolean constructorParameters = false;
char[][] constructorArguments;

protected static char[][] REF_CATEGORIES = { CONSTRUCTOR_REF };
protected static char[][] REF_AND_DECL_CATEGORIES = { CONSTRUCTOR_REF, CONSTRUCTOR_DECL };
protected static char[][] DECL_CATEGORIES = { CONSTRUCTOR_DECL };

/**
 * Constructor entries are encoded as TypeName '/' Arity:
 * e.g. 'X/0'
 */
public static char[] createIndexKey(char[] typeName, int argCount) {
	char[] countChars = argCount < 10
		? COUNTS[argCount]
		: ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
	return CharOperation.concat(typeName, countChars);
}

ConstructorPattern(int matchRule) {
	super(CONSTRUCTOR_PATTERN, matchRule);
}
public ConstructorPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] declaringSimpleName,
	char[] declaringQualification,
	char[][] parameterQualifications,
	char[][] parameterSimpleNames,
	int matchRule) {

	this(matchRule);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	this.declaringQualification = isCaseSensitive() ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = (isCaseSensitive() || isCamelCase()) ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	if (parameterSimpleNames != null) {
		this.parameterCount = parameterSimpleNames.length;
		this.parameterQualifications = new char[this.parameterCount][];
		this.parameterSimpleNames = new char[this.parameterCount][];
		for (int i = 0; i < this.parameterCount; i++) {
			this.parameterQualifications[i] = isCaseSensitive() ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive() ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	} else {
		this.parameterCount = -1;
	}
	((InternalSearchPattern)this).mustResolve = mustResolve();
}
/*
 * Instanciate a method pattern with signatures for generics search
 */
public ConstructorPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] declaringSimpleName,	
	char[] declaringQualification,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	String[] parameterSignatures,
	IMethod method,
//	boolean varargs,
	int matchRule) {

	this(findDeclarations,
		findReferences,
		declaringSimpleName,	
		declaringQualification,
		parameterQualifications, 
		parameterSimpleNames,
		matchRule);

	// Set flags
	try {
		this.flags = method.getFlags();
	} catch (JavaModelException e) {
		// do nothing
	}

	// Get unique key for parameterized constructors
	String genericDeclaringTypeSignature = null;
	String key;
	if (method.isResolved() && new BindingKey(key = method.getKey()).isParameterizedType()) {
		genericDeclaringTypeSignature = Util.getDeclaringTypeSignature(key);
	} else {
		constructorParameters = true;
	}

	// Store type signature and arguments for declaring type
	if (genericDeclaringTypeSignature != null) {
		this.typeSignatures = Util.splitTypeLevelsSignature(genericDeclaringTypeSignature);
		setTypeArguments(Util.getAllTypeArguments(this.typeSignatures));
	} else {
		storeTypeSignaturesAndArguments(method.getDeclaringType());
	}

	// store type signatures and arguments for method parameters type
	if (parameterSignatures != null) {
		int length = parameterSignatures.length;
		if (length > 0) {
			parametersTypeSignatures = new char[length][][];
			parametersTypeArguments = new char[length][][][];
			for (int i=0; i<length; i++) {
				parametersTypeSignatures[i] = Util.splitTypeLevelsSignature(parameterSignatures[i]);
				parametersTypeArguments[i] = Util.getAllTypeArguments(parametersTypeSignatures[i]);
			}
		}
	}

	// Store type signatures and arguments for method
	constructorArguments = extractMethodArguments(method);
	if (hasConstructorArguments())  ((InternalSearchPattern)this).mustResolve = true;
}
/*
 * Instanciate a method pattern with signatures for generics search
 */
public ConstructorPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] declaringSimpleName,	
	char[] declaringQualification,
	String declaringSignature,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	String[] parameterSignatures,
	char[][] arguments,
	int matchRule) {

	this(findDeclarations,
		findReferences,
		declaringSimpleName,	
		declaringQualification,
		parameterQualifications, 
		parameterSimpleNames,
		matchRule);

	// Store type signature and arguments for declaring type
	if (declaringSignature != null) {
		typeSignatures = Util.splitTypeLevelsSignature(declaringSignature);
		setTypeArguments(Util.getAllTypeArguments(typeSignatures));
	}

	// Store type signatures and arguments for method parameters type
	if (parameterSignatures != null) {
		int length = parameterSignatures.length;
		if (length > 0) {
			parametersTypeSignatures = new char[length][][];
			parametersTypeArguments = new char[length][][][];
			for (int i=0; i<length; i++) {
				parametersTypeSignatures[i] = Util.splitTypeLevelsSignature(parameterSignatures[i]);
				parametersTypeArguments[i] = Util.getAllTypeArguments(parametersTypeSignatures[i]);
			}
		}
	}

	// Store type signatures and arguments for method
	constructorArguments = arguments;
	if (arguments  == null || arguments.length == 0) {
		if (getTypeArguments() != null && getTypeArguments().length > 0) {
			constructorArguments = getTypeArguments()[0];
		}
	}
	if (hasConstructorArguments())  ((InternalSearchPattern)this).mustResolve = true;
}
public void decodeIndexKey(char[] key) {
	int size = key.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, key);	

	this.parameterCount = Integer.parseInt(new String(key, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.declaringSimpleName = CharOperation.subarray(key, 0, lastSeparatorIndex);
}
public SearchPattern getBlankPattern() {
	return new ConstructorPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getIndexCategories() {
	if (this.findReferences)
		return this.findDeclarations ? REF_AND_DECL_CATEGORIES : REF_CATEGORIES;
	if (this.findDeclarations)
		return DECL_CATEGORIES;
	return CharOperation.NO_CHAR_CHAR;
}
boolean hasConstructorArguments() {
	return constructorArguments != null && constructorArguments.length > 0;
}
boolean hasConstructorParameters() {
	return constructorParameters;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	ConstructorPattern pattern = (ConstructorPattern) decodedPattern;

	return (this.parameterCount == pattern.parameterCount || this.parameterCount == -1 || !shouldCountParameter())
		&& matchesName(this.declaringSimpleName, pattern.declaringSimpleName);
}
protected boolean mustResolve() {
	if (this.declaringQualification != null) return true;

	// parameter types
	if (this.parameterSimpleNames != null)
		for (int i = 0, max = this.parameterSimpleNames.length; i < max; i++)
			if (this.parameterQualifications[i] != null) return true;
	return this.findReferences; // need to check resolved default constructors and explicit constructor calls
}
EntryResult[] queryIn(Index index) throws IOException {
	char[] key = this.declaringSimpleName; // can be null
	int matchRule = getMatchRule();

	switch(getMatchMode()) {
		case R_EXACT_MATCH :
			if (this.isCamelCase) break;
			if (shouldCountParameter() && this.declaringSimpleName != null && this.parameterCount >= 0)
				key = createIndexKey(this.declaringSimpleName, this.parameterCount);
			else { // do a prefix query with the declaringSimpleName
				matchRule &= ~R_EXACT_MATCH;
				matchRule |= R_PREFIX_MATCH;
			}
			break;
		case R_PREFIX_MATCH :
			// do a prefix query with the declaringSimpleName
			break;
		case R_PATTERN_MATCH :
			if (shouldCountParameter() && this.parameterCount >= 0)
				key = createIndexKey(this.declaringSimpleName == null ? ONE_STAR : this.declaringSimpleName, this.parameterCount);
			else if (this.declaringSimpleName != null && this.declaringSimpleName[this.declaringSimpleName.length - 1] != '*')
				key = CharOperation.concat(this.declaringSimpleName, ONE_STAR, SEPARATOR);
			// else do a pattern query with just the declaringSimpleName
			break;
		case R_REGEXP_MATCH :
			// TODO (frederic) implement regular expression match
			break;
	}

	return index.query(getIndexCategories(), key, matchRule); // match rule is irrelevant when the key is null
}
protected StringBuffer print(StringBuffer output) {
	if (this.findDeclarations) {
		output.append(this.findReferences
			? "ConstructorCombinedPattern: " //$NON-NLS-1$
			: "ConstructorDeclarationPattern: "); //$NON-NLS-1$
	} else {
		output.append("ConstructorReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null)
		output.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		output.append(declaringSimpleName);
	else if (declaringQualification != null)
		output.append("*"); //$NON-NLS-1$

	output.append('(');
	if (parameterSimpleNames == null) {
		output.append("..."); //$NON-NLS-1$
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
			if (i > 0) output.append(", "); //$NON-NLS-1$
			if (parameterQualifications[i] != null) output.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) output.append('*'); else output.append(parameterSimpleNames[i]);
		}
	}
	output.append(')');
	return super.print(output);
}
boolean shouldCountParameter() {
	return (this.flags & Flags.AccStatic) == 0 && (this.flags & Flags.AccVarargs) == 0;
}
}
