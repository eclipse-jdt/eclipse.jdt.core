/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.*;

public class TypeDeclarationPattern extends SearchPattern {

public char[] simpleName;
public char[] pkg;
public char[][] enclosingTypeNames;

// set to CLASS_SUFFIX for only matching classes 
// set to INTERFACE_SUFFIX for only matching interfaces
// set to TYPE_SUFFIX for matching both classes and interfaces
public char classOrInterface; 

protected static char[][] CATEGORIES = { TYPE_DECL };

public static char[] createIndexKey(char[] typeName, char[] packageName, char[][] enclosingTypeNames, char classOrInterface) {
	int typeNameLength = typeName == null ? 0 : typeName.length;
	int packageLength = packageName == null ? 0 : packageName.length;
	int enclosingNamesLength = 0;
	if (enclosingTypeNames != null) {
		for (int i = 0, length = enclosingTypeNames.length; i < length;) {
			enclosingNamesLength += enclosingTypeNames[i].length;
			if (++i < length)
				enclosingNamesLength++; // for the '.' separator
		}
	}

	char[] result = new char[typeNameLength + packageLength + enclosingNamesLength + 4];
	int pos = 0;
	if (typeNameLength > 0) {
		System.arraycopy(typeName, 0, result, pos, typeNameLength);
		pos += typeNameLength;
	}
	result[pos++] = SEPARATOR;
	if (packageLength > 0) {
		System.arraycopy(packageName, 0, result, pos, packageLength);
		pos += packageLength;
	}
	result[pos++] = SEPARATOR;
	if (enclosingNamesLength > 0) {
		for (int i = 0, length = enclosingTypeNames.length; i < length;) {
			char[] enclosingName = enclosingTypeNames[i];
			int itsLength = enclosingName.length;
			System.arraycopy(enclosingName, 0, result, pos, itsLength);
			pos += itsLength;
			if (++i < length)
				result[pos++] = '.';
		}
	}
	result[pos++] = SEPARATOR;
	result[pos] = classOrInterface;
	return result;
}

public TypeDeclarationPattern(
	char[] pkg,
	char[][] enclosingTypeNames,
	char[] simpleName,
	char classOrInterface,
	int matchRule) {

	this(matchRule);

	this.pkg = this.isCaseSensitive ? pkg : CharOperation.toLowerCase(pkg);
	if (this.isCaseSensitive || enclosingTypeNames == null) {
		this.enclosingTypeNames = enclosingTypeNames;
	} else {
		int length = enclosingTypeNames.length;
		this.enclosingTypeNames = new char[length][];
		for (int i = 0; i < length; i++)
			this.enclosingTypeNames[i] = CharOperation.toLowerCase(enclosingTypeNames[i]);
	}
	this.simpleName = this.isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;

	((InternalSearchPattern)this).mustResolve = this.pkg != null && this.enclosingTypeNames != null;
}
TypeDeclarationPattern(int matchRule) {
	super(TYPE_DECL_PATTERN, matchRule);
}
/*
 * Type entries are encoded as simpleTypeName / packageName / enclosingTypeName / 'C' or 'I'
 * e.g. Object/java.lang//C
 * e.g. Cloneable/java.lang//I
 * e.g. LazyValue/javax.swing/UIDefaults/C
 */
public void decodeIndexKey(char[] key) {
	int slash = CharOperation.indexOf(SEPARATOR, key, 0);
	this.simpleName = CharOperation.subarray(key, 0, slash);

	int start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.pkg = slash == start ? CharOperation.NO_CHAR : CharOperation.subarray(key, start, slash);

	slash = CharOperation.indexOf(SEPARATOR, key, start = slash + 1);
	if (slash == start) {
		this.enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
	} else {
		char[] names = CharOperation.subarray(key, start, slash);
		this.enclosingTypeNames = CharOperation.equals(ONE_ZERO, names) ? ONE_ZERO_CHAR : CharOperation.splitOn('.', names);
	}

	this.classOrInterface = key[key.length - 1];
}
public SearchPattern getBlankPattern() {
	return new TypeDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getMatchCategories() {
	return CATEGORIES;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	TypeDeclarationPattern pattern = (TypeDeclarationPattern) decodedPattern;
	switch(this.classOrInterface) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (this.classOrInterface != pattern.classOrInterface) return false;
	}

	if (!matchesName(this.simpleName, pattern.simpleName))
		return false;

	// check package - exact match only
	if (this.pkg != null && !CharOperation.equals(this.pkg, pattern.pkg, this.isCaseSensitive))
		return false;

	// check enclosingTypeNames - exact match only
	if (this.enclosingTypeNames != null) {
		if (this.enclosingTypeNames.length == 0)
			return pattern.enclosingTypeNames.length == 0;
		if (this.enclosingTypeNames.length == 1 && pattern.enclosingTypeNames.length == 1)
			return CharOperation.equals(this.enclosingTypeNames[0], pattern.enclosingTypeNames[0], this.isCaseSensitive);
		if (pattern.enclosingTypeNames == ONE_ZERO_CHAR)
			return true; // is a local or anonymous type
		return CharOperation.equals(this.enclosingTypeNames, pattern.enclosingTypeNames, this.isCaseSensitive);
	}
	return true;
}
EntryResult[] queryIn(Index index) throws IOException {
	char[] key = this.simpleName; // can be null
	int matchRule = getMatchRule();

	switch(this.matchMode) {
		case R_PREFIX_MATCH :
			// do a prefix query with the simpleName
			break;
		case R_EXACT_MATCH :
			if (this.simpleName != null) {
				matchRule = matchRule - R_EXACT_MATCH + R_PREFIX_MATCH;
				key = this.pkg == null
					? CharOperation.append(this.simpleName, SEPARATOR)
					: CharOperation.concat(this.simpleName, SEPARATOR, this.pkg, SEPARATOR, CharOperation.NO_CHAR);
				break; // do a prefix query with the simpleName and possibly the pkg
			}
			matchRule = matchRule - R_EXACT_MATCH + R_PATTERN_MATCH;
			// fall thru to encode the key and do a pattern query
		case R_PATTERN_MATCH :
			if (this.pkg == null) {
				if (this.simpleName == null) {
					if (this.classOrInterface == CLASS_SUFFIX || this.classOrInterface == INTERFACE_SUFFIX)
						key = new char[] {ONE_STAR[0], SEPARATOR, this.classOrInterface}; // find all classes or all interfaces
				} else if (this.simpleName[this.simpleName.length - 1] != '*') {
					key = CharOperation.concat(this.simpleName, ONE_STAR, SEPARATOR);
				}
				break; // do a pattern query with the current encoded key
			}
			// must decode to check enclosingTypeNames due to the encoding of local types
			key = CharOperation.concat(
				this.simpleName == null ? ONE_STAR : this.simpleName, SEPARATOR, this.pkg, SEPARATOR, ONE_STAR);
			break;
	}

	return index.query(getMatchCategories(), key, matchRule); // match rule is irrelevant when the key is null
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	switch (classOrInterface){
		case CLASS_SUFFIX :
			buffer.append("ClassDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			buffer.append("InterfaceDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		default :
			buffer.append("TypeDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
	}
	if (pkg != null) 
		buffer.append(pkg);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, enclosing<"); //$NON-NLS-1$
	if (enclosingTypeNames != null) {
		for (int i = 0; i < enclosingTypeNames.length; i++){
			buffer.append(enclosingTypeNames[i]);
			if (i < enclosingTypeNames.length - 1)
				buffer.append('.');
		}
	} else {
		buffer.append("*"); //$NON-NLS-1$
	}
	buffer.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) 
		buffer.append(simpleName);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, "); //$NON-NLS-1$
	switch(this.matchMode){
		case R_EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case R_PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case R_PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	if (this.isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
