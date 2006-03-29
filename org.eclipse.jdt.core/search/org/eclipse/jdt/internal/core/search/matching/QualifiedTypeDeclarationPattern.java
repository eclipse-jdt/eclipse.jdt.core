/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class QualifiedTypeDeclarationPattern extends TypeDeclarationPattern implements IIndexConstants {

public char[] qualification;
public int packageIndex;

public QualifiedTypeDeclarationPattern(char[] qualification, char[] simpleName, char typeSuffix, int matchRule) {
	this(matchRule);

	this.qualification = isCaseSensitive() ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = (isCaseSensitive() || isCamelCase())  ? simpleName : CharOperation.toLowerCase(simpleName);
	this.typeSuffix = typeSuffix;

	((InternalSearchPattern)this).mustResolve = this.qualification != null || typeSuffix != TYPE_SUFFIX;
}
QualifiedTypeDeclarationPattern(int matchRule) {
	super(matchRule);
}
public void decodeIndexKey(char[] key) {
	int slash = CharOperation.indexOf(SEPARATOR, key, 0);
	this.simpleName = CharOperation.subarray(key, 0, slash);

	int start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	int secondSlash = CharOperation.indexOf(SEPARATOR, key, slash + 1);
	this.packageIndex = -1; // used to compute package vs. enclosingTypeNames in MultiTypeDeclarationPattern
	if (start + 1 == secondSlash) {
		this.qualification = CharOperation.NO_CHAR; // no package name or enclosingTypeNames
	} else if (slash + 1 == secondSlash) {
		this.qualification = CharOperation.subarray(key, start, slash); // only a package name
	} else if (slash == start) {
		this.qualification = CharOperation.subarray(key, slash + 1, secondSlash); // no package name
		this.packageIndex = 0;
	} else {
		this.qualification = CharOperation.subarray(key, start, secondSlash);
		this.packageIndex = slash - start;
		this.qualification[this.packageIndex] = '.';
	}

	// Continue key read by the end to decode modifiers
	int last = key.length-1;
	this.secondary = key[last] == 'S';
	if (this.secondary) {
		last -= 2;
	}
	this.modifiers = key[last-1] + (key[last]<<16);
	decodeModifiers();
}
public SearchPattern getBlankPattern() {
	return new QualifiedTypeDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[] getPackageName() {
	if (this.packageIndex == -1)
		return this.qualification;
	return internedPackageNames.add(CharOperation.subarray(this.qualification, 0, this.packageIndex));
}
public char[][] getEnclosingTypeNames() {
	if (this.packageIndex == -1)
		return CharOperation.NO_CHAR_CHAR;
	if (this.packageIndex == 0)
		return CharOperation.splitOn('.', this.qualification);

	char[] names = CharOperation.subarray(this.qualification, this.packageIndex + 1, this.qualification.length);
	return CharOperation.splitOn('.', names);
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	QualifiedTypeDeclarationPattern pattern = (QualifiedTypeDeclarationPattern) decodedPattern;
	switch(this.typeSuffix) {
		case CLASS_SUFFIX :
			switch (pattern.typeSuffix) {
				case CLASS_SUFFIX :
				case CLASS_AND_INTERFACE_SUFFIX :
				case CLASS_AND_ENUM_SUFFIX :
					break;
				default:
					return false;
			}
			break;
		case INTERFACE_SUFFIX :
			switch (pattern.typeSuffix) {
				case INTERFACE_SUFFIX :
				case CLASS_AND_INTERFACE_SUFFIX :
					break;
				default:
					return false;
			}
			break;
		case ENUM_SUFFIX :
			switch (pattern.typeSuffix) {
				case ENUM_SUFFIX :
				case CLASS_AND_ENUM_SUFFIX :
					break;
				default:
					return false;
			}
			break;
		case ANNOTATION_TYPE_SUFFIX :
			if (this.typeSuffix != pattern.typeSuffix) return false;
			break;
		case CLASS_AND_INTERFACE_SUFFIX :
			switch (pattern.typeSuffix) {
				case CLASS_SUFFIX :
				case INTERFACE_SUFFIX :
				case CLASS_AND_INTERFACE_SUFFIX :
					break;
				default:
					return false;
			}
			break;
		case CLASS_AND_ENUM_SUFFIX :
			switch (pattern.typeSuffix) {
				case CLASS_SUFFIX :
				case ENUM_SUFFIX :
				case CLASS_AND_ENUM_SUFFIX :
					break;
				default:
					return false;
			}
			break;
	}

	return matchesName(this.simpleName, pattern.simpleName) && matchesName(this.qualification, pattern.qualification);
}
protected StringBuffer print(StringBuffer output) {
	switch (this.typeSuffix){
		case CLASS_SUFFIX :
			output.append("ClassDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case CLASS_AND_INTERFACE_SUFFIX:
			output.append("ClassAndInterfaceDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case CLASS_AND_ENUM_SUFFIX :
			output.append("ClassAndEnumDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			output.append("InterfaceDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case ENUM_SUFFIX :
			output.append("EnumDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case ANNOTATION_TYPE_SUFFIX :
			output.append("AnnotationTypeDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		default :
			output.append("TypeDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
	}
	if (this.qualification != null) 
		output.append(this.qualification);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) 
		output.append(simpleName);
	else
		output.append("*"); //$NON-NLS-1$
	output.append("> "); //$NON-NLS-1$
	return super.print(output);
}
}
