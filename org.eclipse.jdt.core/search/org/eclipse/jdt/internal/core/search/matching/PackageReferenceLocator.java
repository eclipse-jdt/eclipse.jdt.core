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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class PackageReferenceLocator extends PatternLocator {

protected PackageReferencePattern pattern;
	
public PackageReferenceLocator(PackageReferencePattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
public void match(AstNode node, MatchingNodeSet nodeSet) { // interested in ImportReference
	if (!(node instanceof ImportReference)) return;

	int level = matchLevel((ImportReference) node);
	if (level >= POTENTIAL_MATCH)
		nodeSet.addMatch(node, level);
}
//public void match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
public void match(Reference node, MatchingNodeSet nodeSet) { // interested in QualifiedNameReference
	if (!(node instanceof QualifiedNameReference)) return;

	int level = matchLevelForTokens(((QualifiedNameReference) node).tokens);
	if (level >= POTENTIAL_MATCH)
		nodeSet.addMatch(node, level);
}
//public void match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public void match(TypeReference node, MatchingNodeSet nodeSet) { // interested in QualifiedTypeReference only
	if (!(node instanceof QualifiedTypeReference)) return;

	int level = matchLevelForTokens(((QualifiedTypeReference) node).tokens);
	if (level >= POTENTIAL_MATCH)
		nodeSet.addMatch(node, level);
}

protected int matchLevel(ImportReference importRef) {
	if (!importRef.onDemand)
		return matchLevelForTokens(importRef.tokens);

	return matchesName(this.pattern.pkgName, CharOperation.concatWith(importRef.tokens, '.'))
		? ACCURATE_MATCH
		: IMPOSSIBLE_MATCH;
}
protected int matchLevelForTokens(char[][] tokens) {
	if (this.pattern.pkgName == null) return ACCURATE_MATCH;

	switch (this.matchMode) {
		case IJavaSearchConstants.EXACT_MATCH:
		case IJavaSearchConstants.PREFIX_MATCH:
			if (CharOperation.prefixEquals(this.pattern.pkgName, CharOperation.concatWith(tokens, '.'), this.isCaseSensitive))
				return POTENTIAL_MATCH;
			break;
		case IJavaSearchConstants.PATTERN_MATCH:
			char[] patternName = this.pattern.pkgName[this.pattern.pkgName.length-1] == '*'
				? this.pattern.pkgName
				: CharOperation.concat(this.pattern.pkgName, ".*".toCharArray()); //$NON-NLS-1$
			if (CharOperation.match(patternName, CharOperation.concatWith(tokens, '.'), this.isCaseSensitive))
				return POTENTIAL_MATCH;
			break;
	}
	return IMPOSSIBLE_MATCH;
}
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (binding == null) {
		this.matchReportReference(importRef, element, accuracy, locator);
	} else {
		if (binding instanceof ImportBinding) {
			locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, importRef.tokens, element, accuracy);
		} else if (binding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) binding).fPackage;
			if (pkgBinding != null)
				locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, pkgBinding.compoundName, element, accuracy);
			else
				locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, importRef.tokens, element, accuracy);
		} 
	}
}
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	if (reference instanceof ImportReference) {
		ImportReference importRef = (ImportReference) reference;
		if (importRef.onDemand) {
			tokens = importRef.tokens;
		} else {
			int length = importRef.tokens.length - 1;
			tokens = new char[length][];
			System.arraycopy(importRef.tokens, 0, tokens, 0, length);
		}
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		Binding binding = qNameRef.binding;
		TypeBinding typeBinding = null;
		switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
			case BindingIds.FIELD : // reading a field
				typeBinding = qNameRef.actualReceiverType;
				break;
			case BindingIds.TYPE : //=============only type ==============
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
				} else {
					typeBinding = (TypeBinding) binding;
				}
				break;
			case BindingIds.VARIABLE : //============unbound cases===========
			case BindingIds.TYPE | BindingIds.VARIABLE :						
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
				}
				break;
		}
		if (typeBinding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) typeBinding).fPackage;
			if (pkgBinding != null)
				tokens = pkgBinding.compoundName;
		} 
		if (tokens == null)
			tokens = qNameRef.tokens;
	} else if (reference instanceof QualifiedTypeReference) {
		QualifiedTypeReference qTypeRef = (QualifiedTypeReference) reference;
		TypeBinding typeBinding = qTypeRef.resolvedType;
		if (typeBinding instanceof ArrayBinding)
			typeBinding = ((ArrayBinding)typeBinding).leafComponentType;
		if (typeBinding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) typeBinding).fPackage;
			if (pkgBinding != null)
				tokens = pkgBinding.compoundName;
		} 
		if (tokens == null)
			tokens = qTypeRef.tokens;
	}
	if (tokens == null)
		tokens = CharOperation.NO_CHAR_CHAR;
	locator.reportAccurateReference(reference.sourceStart, reference.sourceEnd, tokens, element, accuracy);
}
public int resolveLevel(AstNode node) {
	if (node instanceof QualifiedTypeReference)
		return resolveLevel(((QualifiedTypeReference) node).resolvedType);
	if (node instanceof QualifiedNameReference)
		return this.resolveLevel((QualifiedNameReference) node);
//	if (node instanceof ImportReference) - Not called when resolve is true, see MatchingNodeSet.reportMatching(unit)
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;

	char[][] compoundName = null;
	if (binding instanceof ImportBinding) {
		compoundName = ((ImportBinding) binding).compoundName;
	} else {
		if (binding instanceof ProblemReferenceBinding) return INACCURATE_MATCH;
		if (binding instanceof ArrayBinding) {
			binding = ((ArrayBinding) binding).leafComponentType;
			if (binding == null) return INACCURATE_MATCH;
		}
		if (binding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) binding).fPackage;
			if (pkgBinding == null) return INACCURATE_MATCH;
			compoundName = pkgBinding.compoundName;
		}
	}
	return compoundName != null && matchesName(this.pattern.pkgName, CharOperation.concatWith(compoundName, '.'))
		? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
}
protected int resolveLevel(QualifiedNameReference qNameRef) {
	Binding binding = qNameRef.binding;
	if (binding == null) return INACCURATE_MATCH;

	TypeBinding typeBinding = null;
	char[][] tokens = qNameRef.tokens;
	int lastIndex = tokens.length - 1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			// no valid match amongst fields
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			if (lastIndex < 0) return IMPOSSIBLE_MATCH;
			break;
		case BindingIds.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no package match in it
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
				if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
			} else {
				typeBinding = (TypeBinding)binding;
			}
			break;
		/*
		 * Handling of unbound qualified name references. The match may reside in the resolved fragment,
		 * which is recorded inside the problem binding, along with the portion of the name until it became a problem.
		 */
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :						
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
				if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
			}
			break;					
	}
	return resolveLevel(typeBinding);
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
