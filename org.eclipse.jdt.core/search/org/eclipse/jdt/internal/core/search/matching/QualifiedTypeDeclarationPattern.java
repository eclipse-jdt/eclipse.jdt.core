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
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.core.index.IEntryResult;

public class QualifiedTypeDeclarationPattern extends TypeDeclarationPattern {
	
protected char[] qualification;
protected char[] decodedQualification;
	
public QualifiedTypeDeclarationPattern(
	char[] qualification,
	char[] simpleName,
	char classOrInterface,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);
	this.classOrInterface = classOrInterface;

	this.mustResolve = qualification != null;
}
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int size = word.length;

	this.decodedClassOrInterface = word[TYPE_DECL_LENGTH];
	int oldSlash = TYPE_DECL_LENGTH + 1;
	int slash = CharOperation.indexOf(SEPARATOR, word, oldSlash + 1);
	char[] pkgName = slash == oldSlash + 1
		? CharOperation.NO_CHAR
		: CharOperation.subarray(word, oldSlash+1, slash);
	this.decodedSimpleName = CharOperation.subarray(word, slash + 1, slash = CharOperation.indexOf(SEPARATOR, word, slash + 1));

	char[][] decodedEnclosingTypeNames;
	if (slash + 1 < size) {
		decodedEnclosingTypeNames = (slash + 3 == size && word[slash + 1] == ONE_ZERO[0])
			? ONE_ZERO_CHAR
			: CharOperation.splitOn('/', CharOperation.subarray(word, slash + 1, size - 1));
	} else {
		decodedEnclosingTypeNames = CharOperation.NO_CHAR_CHAR;
	}
	this.decodedQualification = CharOperation.concatWith(pkgName, decodedEnclosingTypeNames, '.');
}
/**
 * see SearchPattern.matchIndexEntry
 */
protected boolean matchIndexEntry() {
	switch(this.classOrInterface) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (this.classOrInterface != decodedClassOrInterface) return false;
		case TYPE_SUFFIX : // nothing
	}

	if (this.qualification != null) {
		switch(this.matchMode) {
			case EXACT_MATCH :
				if (!CharOperation.equals(this.qualification, this.decodedQualification, this.isCaseSensitive))
					return false;
				break;
			case PREFIX_MATCH :
				if (!CharOperation.prefixEquals(this.qualification, this.decodedQualification, this.isCaseSensitive))
					return false;
				break;
			case PATTERN_MATCH :
				if (!CharOperation.match(this.qualification, this.decodedQualification, this.isCaseSensitive))
					return false;
		}
	}

	if (this.simpleName != null) {
		switch(this.matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(this.simpleName, this.decodedSimpleName, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(this.simpleName, this.decodedSimpleName, this.isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(this.simpleName, this.decodedSimpleName, this.isCaseSensitive);
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;

	TypeBinding type = (TypeBinding) binding;

	switch (this.classOrInterface) {
		case CLASS_SUFFIX:
			if (type.isInterface()) return IMPOSSIBLE_MATCH;
			break;
		case INTERFACE_SUFFIX:
			if (!type.isInterface()) return IMPOSSIBLE_MATCH;
			break;
		case TYPE_SUFFIX: // nothing
	}

	// fully qualified name
	return matchLevelForType(this.simpleName, this.qualification, type);
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
	if (this.qualification != null) buffer.append(this.qualification);
	buffer.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) buffer.append(simpleName);
	buffer.append(">, "); //$NON-NLS-1$
	switch(matchMode){
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
	if (isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
