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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;

public class TypeDeclarationPattern extends SearchPattern {

private static ThreadLocal indexRecord = new ThreadLocal() {
	protected Object initialValue() {
		return new TypeDeclarationPattern(null, null, null, ' ', R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
};

public char[] simpleName;
public char[] pkg;
public char[][] enclosingTypeNames;

// set to CLASS_SUFFIX for only matching classes 
// set to INTERFACE_SUFFIX for only matching interfaces
// set to TYPE_SUFFIX for matching both classes and interfaces
public char classOrInterface; 

public static char[] createIndexKey(char[] packageName, char[][] enclosingTypeNames, char[] typeName, boolean isClass) {
	TypeDeclarationPattern record = getTypeDeclarationRecord();
	record.pkg = packageName;
	record.enclosingTypeNames = enclosingTypeNames;
	record.simpleName = typeName;
	record.classOrInterface = isClass ? CLASS_SUFFIX : INTERFACE_SUFFIX;
	return record.encodeIndexKey();
}
public static TypeDeclarationPattern getTypeDeclarationRecord() {
	return (TypeDeclarationPattern)indexRecord.get();
}
public TypeDeclarationPattern(int matchRule) {
	super(TYPE_DECL_PATTERN, matchRule);
}
public TypeDeclarationPattern(
	char[] pkg,
	char[][] enclosingTypeNames,
	char[] simpleName,
	char classOrInterface,
	int matchRule) {

	super(TYPE_DECL_PATTERN, matchRule);

	boolean isCaseSensitive = isCaseSensitive();
	this.pkg = isCaseSensitive ? pkg : CharOperation.toLowerCase(pkg);
	if (isCaseSensitive || enclosingTypeNames == null) {
		this.enclosingTypeNames = enclosingTypeNames;
	} else {
		int length = enclosingTypeNames.length;
		this.enclosingTypeNames = new char[length][];
		for (int i = 0; i < length; i++)
			this.enclosingTypeNames[i] = CharOperation.toLowerCase(enclosingTypeNames[i]);
	}
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;
	
	this.mustResolve = pkg != null && enclosingTypeNames != null;
}
/*
 * Type entries are encoded as 'typeDecl/' ('C' | 'I') '/' PackageName '/' TypeName '/' EnclosingTypeName
 * e.g. typeDecl/C/java.lang/Object/
 * e.g. typeDecl/I/java.lang/Cloneable/
 * e.g. typeDecl/C/javax.swing/LazyValue/UIDefaults
 * 
 * Current encoding is optimized for queries: all classes/interfaces
 */
public void decodeIndexKey(char[] key) {
	int size = key.length;

	this.classOrInterface = key[0];
	int oldSlash = 1;
	int slash = CharOperation.indexOf(SEPARATOR, key, oldSlash + 1);
	this.pkg = (slash == oldSlash + 1)
		? CharOperation.NO_CHAR
		: CharOperation.subarray(key, oldSlash + 1, slash);
	this.simpleName = CharOperation.subarray(key, slash + 1, slash = CharOperation.indexOf(SEPARATOR, key, slash + 1));

	if (slash+1 < size) {
		this.enclosingTypeNames = (slash + 3 == size && key[slash + 1] == ONE_ZERO[0])
			? ONE_ZERO_CHAR
			: CharOperation.splitOn('/', CharOperation.subarray(key, slash+1, size-1));
	} else {
		this.enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
	}
}
/*
 * classOrInterface / package / simpleName / enclosingTypeNames
 */
public char[] encodeIndexKey() {
	char[] packageName = isCaseSensitive() ? pkg : null;
	switch(this.classOrInterface) {
		case CLASS_SUFFIX :
			if (packageName == null) 
				return new char[] {CLASS_SUFFIX, SEPARATOR};
			break;
		case INTERFACE_SUFFIX :
			if (packageName == null) 
				return new char[] {INTERFACE_SUFFIX, SEPARATOR};
			break;
		default :
			return CharOperation.NO_CHAR; // cannot do better given encoding
	}

	char[] typeName = isCaseSensitive() ? this.simpleName : null;
	if (typeName != null && matchMode() == PATTERN_MATCH) {
		int starPos = CharOperation.indexOf('*', typeName);
		switch(starPos) {
			case -1 :
				break;
			case 0 :
				typeName = null;
				break;
			default : 
				typeName = CharOperation.subarray(typeName, 0, starPos);
		}
	}

	int packageLength = packageName.length;
	int enclosingTypeNamesLength = 0;
	if (this.enclosingTypeNames != null)
		for (int i = 0, length = this.enclosingTypeNames.length; i < length; i++)
			enclosingTypeNamesLength += this.enclosingTypeNames[i].length + 1;
	int pos = 0;
	int typeNameLength = typeName == null ? 0 : typeName.length;
	int resultLength = pos + packageLength + typeNameLength + enclosingTypeNamesLength + 4;
	char[] result = new char[resultLength];
	result[pos++] = this.classOrInterface;
	result[pos++] = SEPARATOR;
	if (packageLength > 0) {
		System.arraycopy(packageName, 0, result, pos, packageLength);
		pos += packageLength;
	}
	result[pos++] = SEPARATOR;
	if (typeName != null) {
		System.arraycopy(typeName, 0, result, pos, typeNameLength);
		pos += typeNameLength;

		result[pos++] = SEPARATOR;
		if (enclosingTypeNames != null) {
			for (int i = 0, length = this.enclosingTypeNames.length; i < length; i++) {
				int enclosingTypeNameLength = this.enclosingTypeNames[i].length;
				System.arraycopy(this.enclosingTypeNames[i], 0, result, pos, enclosingTypeNameLength);
				pos += enclosingTypeNameLength;
				result[pos++] = SEPARATOR;
			}
		}
	}
	if (pos != resultLength) {
		System.arraycopy(result, 0, result = new char[pos], 0, pos);
	}
	return result;
}
public SearchPattern getIndexRecord() {
	return getTypeDeclarationRecord();
}
public char[][] getMatchCategories() {
	return new char[][] {TYPE_DECL};
}
public boolean isMatchingIndexRecord() {
	TypeDeclarationPattern record = getTypeDeclarationRecord();
	switch(this.classOrInterface) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (this.classOrInterface != record.classOrInterface) return false;
		case TYPE_SUFFIX : // nothing
	}

	/* check qualification - exact match only */
	if (this.pkg != null && !CharOperation.equals(this.pkg, record.pkg, isCaseSensitive()))
		return false;
	/* check enclosingTypeName - exact match only */
	if (this.enclosingTypeNames != null) {
		// empty char[][] means no enclosing type (in which case, the decoded one is the empty char array)
		if (this.enclosingTypeNames.length == 0) {
			if (record.enclosingTypeNames != CharOperation.NO_CHAR_CHAR) return false;
		} else {
			if (!CharOperation.equals(this.enclosingTypeNames, record.enclosingTypeNames, isCaseSensitive()))
				if (!CharOperation.equals(record.enclosingTypeNames, ONE_ZERO_CHAR)) // if not a local or anonymous type
					return false;
		}
	}

	return matchesName(this.simpleName, record.simpleName);
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
	switch(matchMode()){
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
	if (isCaseSensitive())
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
