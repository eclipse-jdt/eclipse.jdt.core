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

public class QualifiedTypeDeclarationPattern extends TypeDeclarationPattern {
	
private static ThreadLocal indexRecord = new ThreadLocal() {
	protected Object initialValue() {
		return new QualifiedTypeDeclarationPattern(null, null, ' ', R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
};

protected char[] qualification;

public static QualifiedTypeDeclarationPattern getQualifiedTypeDeclarationRecord() {
	return (QualifiedTypeDeclarationPattern)indexRecord.get();
}
public QualifiedTypeDeclarationPattern(
	char[] qualification,
	char[] simpleName,
	char classOrInterface,
	int matchRule) {

	super(matchRule);

	boolean isCaseSensitive = isCaseSensitive();
	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;

	this.mustResolve = qualification != null;
}
public void decodeIndexKey(char[] key) {
	int size = key.length;

	this.classOrInterface = key[0];
	int oldSlash = 1;
	int slash = CharOperation.indexOf(SEPARATOR, key, oldSlash + 1);
	char[] pkgName = slash == oldSlash + 1
		? CharOperation.NO_CHAR
		: CharOperation.subarray(key, oldSlash+1, slash);
	this.simpleName = CharOperation.subarray(key, slash + 1, slash = CharOperation.indexOf(SEPARATOR, key, slash + 1));

	char[][] decodedEnclosingTypeNames;
	if (slash + 1 < size) {
		decodedEnclosingTypeNames = (slash + 3 == size && key[slash + 1] == ONE_ZERO[0])
			? ONE_ZERO_CHAR
			: CharOperation.splitOn('/', CharOperation.subarray(key, slash + 1, size - 1));
	} else {
		decodedEnclosingTypeNames = CharOperation.NO_CHAR_CHAR;
	}
	this.qualification = CharOperation.concatWith(pkgName, decodedEnclosingTypeNames, '.');
}
public SearchPattern getIndexRecord() {
	return getQualifiedTypeDeclarationRecord();
}
public boolean isMatchingIndexRecord() {
	QualifiedTypeDeclarationPattern record = getQualifiedTypeDeclarationRecord();
	switch(this.classOrInterface) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (this.classOrInterface != record.classOrInterface) return false;
		case TYPE_SUFFIX : // nothing
	}

	if (!matchesName(this.pkg, record.qualification))
		return false;
	
	return matchesName(this.simpleName, record.simpleName);
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	switch (classOrInterface){
		case CLASS_SUFFIX :
			buffer.append("ClassDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			buffer.append("InterfaceDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		default :
			buffer.append("TypeDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
	}
	if (this.qualification != null) 
		buffer.append(this.qualification);
	else
		buffer.append("*"); //$NON-NLS-1$
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
