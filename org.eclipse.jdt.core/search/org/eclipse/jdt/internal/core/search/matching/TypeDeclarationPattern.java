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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class TypeDeclarationPattern extends SearchPattern {

protected char[] simpleName;
protected char[] pkg;
protected char[][] enclosingTypeNames;

// set to CLASS_SUFFIX for only matching classes 
// set to INTERFACE_SUFFIX for only matching interfaces
// set to TYPE_SUFFIX for matching both classes and interfaces
protected char classOrInterface; 

private char[] decodedPackage;
private char[][] decodedEnclosingTypeNames;
protected char[] decodedSimpleName;
protected char decodedClassOrInterface;
	
public TypeDeclarationPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
public TypeDeclarationPattern(
	char[] pkg,
	char[][] enclosingTypeNames,
	char[] simpleName,
	char classOrInterface,
	int matchMode, 
	boolean isCaseSensitive) {

	super(matchMode, isCaseSensitive);

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
protected void decodeIndexEntry(IEntryResult entryResult) {
	char[] word = entryResult.getWord();
	int size = word.length;

	this.decodedClassOrInterface = word[TYPE_DECL_LENGTH];
	int oldSlash = TYPE_DECL_LENGTH + 1;
	int slash = CharOperation.indexOf(SEPARATOR, word, oldSlash + 1);
	this.decodedPackage = (slash == oldSlash + 1)
		? CharOperation.NO_CHAR
		: CharOperation.subarray(word, oldSlash + 1, slash);
	this.decodedSimpleName = CharOperation.subarray(word, slash + 1, slash = CharOperation.indexOf(SEPARATOR, word, slash + 1));

	if (slash+1 < size) {
		this.decodedEnclosingTypeNames = (slash + 3 == size && word[slash + 1] == ONE_ZERO[0])
			? ONE_ZERO_CHAR
			: CharOperation.splitOn('/', CharOperation.subarray(word, slash+1, size-1));
	} else {
		this.decodedEnclosingTypeNames = CharOperation.NO_CHAR_CHAR;
	}
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	boolean isClass = decodedClassOrInterface == CLASS_SUFFIX;
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		if (file != null) {
			String path = IndexedFile.convertPath(file.getPath());
			if (scope.encloses(path)) {
				if (isClass)
					requestor.acceptClassDeclaration(path, decodedSimpleName, decodedEnclosingTypeNames, decodedPackage);
				else
					requestor.acceptInterfaceDeclaration(path, decodedSimpleName, decodedEnclosingTypeNames, decodedPackage);
			}
		}
	}
}
/**
 * see SearchPattern.indexEntryPrefix()
 */
protected char[] indexEntryPrefix(){
	return AbstractIndexer.bestTypeDeclarationPrefix(pkg, simpleName, classOrInterface, matchMode, isCaseSensitive);
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return COMPILATION_UNIT | CLASS | METHOD | FIELD;
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!(binaryInfo instanceof IBinaryType)) return false;

	IBinaryType type = (IBinaryType) binaryInfo;
	char[] fullyQualifiedTypeName = (char[]) type.getName().clone();
	CharOperation.replace(fullyQualifiedTypeName, '/', '.');

	if (this.enclosingTypeNames == null) {
		if (!matchesType(this.simpleName, this.pkg, fullyQualifiedTypeName)) return false;
	} else {
		char[] enclosingTypeName = CharOperation.concatWith(this.enclosingTypeNames, '.');
		char[] pattern = this.pkg == null
			? enclosingTypeName
			: CharOperation.concat(this.pkg, enclosingTypeName, '.');
		if (!matchesType(this.simpleName, pattern, fullyQualifiedTypeName)) return false;
	}

	switch (this.classOrInterface) {
		case CLASS_SUFFIX:
			return !type.isInterface();
		case INTERFACE_SUFFIX:
			return type.isInterface();
		case TYPE_SUFFIX: // nothing
	}
	return true;
}
/**
 * see SearchPattern.matchIndexEntry
 */
protected boolean matchIndexEntry() {
	switch(this.classOrInterface) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
			if (this.classOrInterface != this.decodedClassOrInterface) return false;
		case TYPE_SUFFIX : // nothing
	}

	/* check qualification - exact match only */
	if (this.pkg != null && !CharOperation.equals(this.pkg, this.decodedPackage, this.isCaseSensitive))
		return false;
	/* check enclosingTypeName - exact match only */
	if (this.enclosingTypeNames != null) {
		// empty char[][] means no enclosing type (in which case, the decoded one is the empty char array)
		if (this.enclosingTypeNames.length == 0) {
			if (this.decodedEnclosingTypeNames != CharOperation.NO_CHAR_CHAR) return false;
		} else {
			if (!CharOperation.equals(this.enclosingTypeNames, this.decodedEnclosingTypeNames, this.isCaseSensitive))
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
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (!(node instanceof TypeDeclaration)) return IMPOSSIBLE_MATCH;

	TypeDeclaration type = (TypeDeclaration) node;
	if (resolve)
		return matchLevel(type.binding);

	if (this.simpleName != null && !matchesName(this.simpleName, type.name))
		return IMPOSSIBLE_MATCH;
	return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
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
		case TYPE_SUFFIX : // nothing
	}

	// fully qualified name
	char[] enclosingTypeName = this.enclosingTypeNames == null ? null : CharOperation.concatWith(this.enclosingTypeNames, '.');
	return matchLevelForType(this.simpleName, this.pkg, enclosingTypeName, type);
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * qualification pattern and enclosing type name pattern.
 */
protected int matchLevelForType(char[] simpleNamePattern, char[] qualificationPattern, char[] enclosingNamePattern, TypeBinding type) {
	if (enclosingNamePattern == null)
		return matchLevelForType(simpleNamePattern, qualificationPattern, type);
	if (qualificationPattern == null)
		return matchLevelForType(simpleNamePattern, enclosingNamePattern, type);

	// case of an import reference while searching for ALL_OCCURENCES of a type (see bug 37166)
	if (type instanceof ProblemReferenceBinding) return IMPOSSIBLE_MATCH;

	// pattern was created from a Java element: qualification is the package name.
	char[] fullQualificationPattern = CharOperation.concat(qualificationPattern, enclosingNamePattern, '.');
	if (CharOperation.equals(this.pkg, CharOperation.concatWith(type.getPackage().compoundName, '.')))
		return matchLevelForType(simpleNamePattern, fullQualificationPattern, type);
	return IMPOSSIBLE_MATCH;
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
	if (pkg != null) buffer.append(pkg);
	buffer.append(">, enclosing<"); //$NON-NLS-1$
	if (enclosingTypeNames != null) {
		for (int i = 0; i < enclosingTypeNames.length; i++){
			buffer.append(enclosingTypeNames[i]);
			if (i < enclosingTypeNames.length - 1)
				buffer.append('.');
		}
	}
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
