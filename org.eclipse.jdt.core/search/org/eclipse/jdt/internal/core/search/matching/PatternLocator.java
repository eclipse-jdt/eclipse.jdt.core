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

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public abstract class PatternLocator implements IIndexConstants {

protected int matchMode;
protected boolean isCaseSensitive;

/* match levels */
public static final int IMPOSSIBLE_MATCH = 0;
public static final int INACCURATE_MATCH = 1;
public static final int POSSIBLE_MATCH = 2;
public static final int ACCURATE_MATCH = 3;

/* match container */
public static final int COMPILATION_UNIT_CONTAINER = 1;
public static final int CLASS_CONTAINER = 2;
public static final int METHOD_CONTAINER = 4;
public static final int FIELD_CONTAINER = 8;
public static final int ALL_CONTAINER =
	COMPILATION_UNIT_CONTAINER | CLASS_CONTAINER | METHOD_CONTAINER | FIELD_CONTAINER;

public static PatternLocator patternLocator(SearchPattern pattern) {
	switch (pattern.kind) {
		case IIndexConstants.PKG_REF_PATTERN :
			return new PackageReferenceLocator((PackageReferencePattern) pattern);
		case IIndexConstants.PKG_DECL_PATTERN :
			return new PackageDeclarationLocator((PackageDeclarationPattern) pattern);
		case IIndexConstants.TYPE_REF_PATTERN :
			return new TypeReferenceLocator((TypeReferencePattern) pattern);
		case IIndexConstants.TYPE_DECL_PATTERN :
			return new TypeDeclarationLocator((TypeDeclarationPattern) pattern);
		case IIndexConstants.SUPER_REF_PATTERN :
			return new SuperTypeReferenceLocator((SuperTypeReferencePattern) pattern);
		case IIndexConstants.CONSTRUCTOR_PATTERN :
			return new ConstructorLocator((ConstructorPattern) pattern);
		case IIndexConstants.FIELD_PATTERN :
			return new FieldLocator((FieldPattern) pattern);
		case IIndexConstants.METHOD_PATTERN :
			return new MethodLocator((MethodPattern) pattern);
		case IIndexConstants.OR_PATTERN :
			return new OrLocator((OrPattern) pattern);
		case IIndexConstants.LOCAL_VAR_PATTERN :
			return new LocalVariableLocator((LocalVariablePattern) pattern);
	}
	return null;
}
public static char[] qualifiedPattern(char[] simpleNamePattern, char[] qualificationPattern) {
	// NOTE: if case insensitive search then simpleNamePattern & qualificationPattern are assumed to be lowercase
	if (simpleNamePattern == null) {
		if (qualificationPattern == null) return null;
		return CharOperation.concat(qualificationPattern, ONE_STAR, '.');
	} else {
		return qualificationPattern == null
			? CharOperation.concat(ONE_STAR, simpleNamePattern)
			: CharOperation.concat(qualificationPattern, simpleNamePattern, '.');
	}
}
public static char[] qualifiedSourceName(TypeBinding binding) {
	if (binding instanceof ReferenceBinding) {
		ReferenceBinding type = (ReferenceBinding) binding;
		if (type.isLocalType())
			return type.isMemberType()
				? CharOperation.concat(qualifiedSourceName(type.enclosingType()), type.sourceName(), '.')
				: CharOperation.concat(qualifiedSourceName(type.enclosingType()), new char[] {'.', '1', '.'}, type.sourceName());
	}
	return binding != null ? binding.qualifiedSourceName() : null;
}


public PatternLocator(SearchPattern pattern) {
	this.matchMode = pattern.matchMode;
	this.isCaseSensitive = pattern.isCaseSensitive;
}
/**
 * Initializes this search pattern so that polymorphic search can be performed.
 */ 
public void initializePolymorphicSearch(MatchLocator locator) {
	// default is to do nothing
}
/**
 * Check if the given ast node syntactically matches this pattern.
 * If it does, add it to the match set.
 * Returns the match level.
 */
public int match(ASTNode node, MatchingNodeSet nodeSet) { // needed for some generic nodes
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(Expression node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(FieldDeclaration node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(LocalDeclaration node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(MethodDeclaration node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(MessageSend node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(Reference node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(TypeDeclaration node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
public int match(TypeReference node, MatchingNodeSet nodeSet) {
	// each subtype should override if needed
	return IMPOSSIBLE_MATCH;
}
/**
 * Returns the type(s) of container for this pattern.
 * It is a bit combination of types, denoting compilation unit, class declarations, field declarations or method declarations.
 */
protected int matchContainer() {
	// override if the pattern can be more specific
	return ALL_CONTAINER;
}
/**
 * Returns whether the given name matches the given pattern.
 */
protected boolean matchesName(char[] pattern, char[] name) {
	if (pattern == null) return true; // null is as if it was "*"
	if (name != null) {
		switch (this.matchMode) {
			case SearchPattern.R_EXACT_MATCH :
				return CharOperation.equals(pattern, name, this.isCaseSensitive);
			case SearchPattern.R_PREFIX_MATCH :
				return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
			case SearchPattern.R_PATTERN_MATCH :
				if (!this.isCaseSensitive)
					pattern = CharOperation.toLowerCase(pattern);
				return CharOperation.match(pattern, name, this.isCaseSensitive);
		}
	}
	return false;
}
/**
 * Returns whether the given type reference matches the given pattern.
 */
protected boolean matchesTypeReference(char[] pattern, TypeReference type) {
	if (pattern == null) return true; // null is as if it was "*"
	if (type == null) return true; // treat as an inexact match

	char[][] compoundName = type.getTypeName();
	char[] simpleName = compoundName[compoundName.length - 1];
	int dimensions = type.dimensions() * 2;
	if (dimensions > 0) {
		int length = simpleName.length;
		char[] result = new char[length + dimensions];
		System.arraycopy(simpleName, 0, result, 0, length);
		for (int i = length, l = result.length; i < l;) {
			result[i++] = '[';
			result[i++] = ']';
		}
		simpleName = result;
	}

	return matchesName(pattern, simpleName);
}
/**
 * Returns the match level for the given importRef.
 */
protected int matchLevel(ImportReference importRef) {
	// override if interested in import references which are caught by the generic version of match(ASTNode, MatchingNodeSet)
	return IMPOSSIBLE_MATCH;
}
/**
 * Reports the match of the given import reference if the resolveLevel is high enough.
 */
protected void matchLevelAndReportImportRef(ImportReference importRef, Binding binding, MatchLocator locator) throws CoreException {
	int level = resolveLevel(binding);
	if (level >= INACCURATE_MATCH) {
		matchReportImportRef(
			importRef, 
			binding, 
			locator.createImportHandle(importRef), 
			level == ACCURATE_MATCH
				? SearchMatch.A_ACCURATE
				: SearchMatch.A_INACCURATE,
			locator);
	}
}
/**
 * Reports the match of the given import reference.
 */
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (locator.encloses(element)) {
		// default is to report a match as a regular ref.
		this.matchReportReference(importRef, element, accuracy, locator);
	}
}
/**
 * Reports the match of the given reference.
 */
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	SearchMatch match = null;
	int referenceType = referenceType();
	int offset = reference.sourceStart;
	switch (referenceType) {
		case IJavaElement.PACKAGE_FRAGMENT:
			match = locator.newPackageReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
			break;
		case IJavaElement.TYPE:
			match = locator.newTypeReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
			break;
		case IJavaElement.FIELD:
			match = locator.newFieldReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
			break;
		case IJavaElement.METHOD:
			match = locator.newMethodReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
			break;
		case IJavaElement.LOCAL_VARIABLE:
			match = locator.newLocalVariableReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
			break;
	}
	if (match != null) {
		locator.report(match);
	}
}
protected int referenceType() {
	return 0; // defaults to unknown (a generic JavaSearchMatch will be created)
}
/**
 * Finds out whether the given ast node matches this search pattern.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 * Returns INACCURATE_MATCH if it potentially matches this search pattern (ie. 
 * it has already been resolved but resolving failed.)
 * Returns ACCURATE_MATCH if it matches exactly this search pattern (ie. 
 * it doesn't need to be resolved or it has already been resolved.)
 */
public int resolveLevel(ASTNode possibleMatchingNode) {
	// only called with nodes which were possible matches to the call to matchLevel
	// need to do instance of checks to find out exact type of ASTNode
	return IMPOSSIBLE_MATCH;
}
/**
 * Finds out whether the given binding matches this search pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed but match is still possible.
 * Returns IMPOSSIBLE_MATCH otherwise.
 * Default is to return INACCURATE_MATCH.
 */
public int resolveLevel(Binding binding) {
	// override if the pattern can match the binding
	return INACCURATE_MATCH;
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int resolveLevelForType(char[] simpleNamePattern, char[] qualificationPattern, TypeBinding type) {
	return resolveLevelForType(qualifiedPattern(simpleNamePattern, qualificationPattern), type);
}
/**
 * Returns whether the given type binding matches the given qualified pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int resolveLevelForType(char[] qualifiedPattern, TypeBinding type) {
	if (qualifiedPattern == null) return ACCURATE_MATCH;
	if (type == null) return INACCURATE_MATCH;

	// NOTE: if case insensitive search then qualifiedPattern is assumed to be lowercase

	char[] qualifiedPackageName = type.qualifiedPackageName();
	char[] qualifiedSourceName = qualifiedSourceName(type);
	char[] fullyQualifiedTypeName = qualifiedPackageName.length == 0
		? qualifiedSourceName
		: CharOperation.concat(qualifiedPackageName, qualifiedSourceName, '.');
	return CharOperation.match(qualifiedPattern, fullyQualifiedTypeName, this.isCaseSensitive)
		? ACCURATE_MATCH
		: IMPOSSIBLE_MATCH;
}
public String toString(){
	return "SearchPattern"; //$NON-NLS-1$
}
}
