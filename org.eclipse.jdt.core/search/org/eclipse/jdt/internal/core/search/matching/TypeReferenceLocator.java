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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class TypeReferenceLocator extends PatternLocator {

protected TypeReferencePattern pattern;
protected boolean isDeclarationOfReferencedTypesPattern;

public TypeReferenceLocator(TypeReferencePattern pattern) {
	super(pattern);

	this.pattern = pattern;
	this.isDeclarationOfReferencedTypesPattern = this.pattern instanceof DeclarationOfReferencedTypesPattern;
}
protected IJavaElement findElement(IJavaElement element, int accuracy) {
	// need exact match to be able to open on type ref
	if (accuracy != SearchMatch.A_ACCURATE) return null;

	// element that references the type must be included in the enclosing element
	DeclarationOfReferencedTypesPattern declPattern = (DeclarationOfReferencedTypesPattern) this.pattern; 
	while (element != null && !declPattern.enclosingElement.equals(element))
		element = element.getParent();
	return element;
}
public int match(ASTNode node, MatchingNodeSet nodeSet) { // interested in ImportReference
	if (!(node instanceof ImportReference)) return IMPOSSIBLE_MATCH;

	return nodeSet.addMatch(node, matchLevel((ImportReference) node));
}
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
public int match(Reference node, MatchingNodeSet nodeSet) { // interested in NameReference & its subtypes
	if (!(node instanceof NameReference)) return IMPOSSIBLE_MATCH;

	if (this.pattern.simpleName == null)
		return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	if (node instanceof SingleNameReference) {
		if (matchesName(this.pattern.simpleName, ((SingleNameReference) node).token))
			return nodeSet.addMatch(node, POSSIBLE_MATCH); // resolution is needed to find out if it is a type ref 
	} else {
		char[][] tokens = ((QualifiedNameReference) node).tokens;
		for (int i = 0, max = tokens.length; i < max; i++)
			if (matchesName(this.pattern.simpleName, tokens[i]))
				return nodeSet.addMatch(node, POSSIBLE_MATCH); // resolution is needed to find out if it is a type ref
	}

	return IMPOSSIBLE_MATCH;
}
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(TypeReference node, MatchingNodeSet nodeSet) {
	if (this.pattern.simpleName == null)
		return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	if (node instanceof SingleTypeReference) {
		if (matchesName(this.pattern.simpleName, ((SingleTypeReference) node).token))
			return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
	} else {
		char[][] tokens = ((QualifiedTypeReference) node).tokens;
		for (int i = 0, max = tokens.length; i < max; i++)
			if (matchesName(this.pattern.simpleName, tokens[i]))
				return nodeSet.addMatch(node, POSSIBLE_MATCH); // resolution is needed to find out if it is a type ref
	}

	return IMPOSSIBLE_MATCH;
}

protected int matchLevel(ImportReference importRef) {
	if (this.pattern.qualification == null) {
		if (this.pattern.simpleName == null) return ACCURATE_MATCH;
		char[][] tokens = importRef.tokens;
		for (int i = 0, length = tokens.length; i < length; i++)
			if (matchesName(this.pattern.simpleName, tokens[i])) return ACCURATE_MATCH;
	} else {
		char[][] tokens = importRef.tokens;
		char[] qualifiedPattern = this.pattern.simpleName == null
			? this.pattern.qualification
			: CharOperation.concat(this.pattern.qualification, this.pattern.simpleName, '.');
		char[] qualifiedTypeName = CharOperation.concatWith(tokens, '.');
		switch (this.matchMode) {
			case SearchPattern.R_EXACT_MATCH :
			case SearchPattern.R_PREFIX_MATCH :
				if (CharOperation.prefixEquals(qualifiedPattern, qualifiedTypeName, this.isCaseSensitive)) return POSSIBLE_MATCH;
				break;
			case SearchPattern.R_PATTERN_MATCH:
				if (CharOperation.match(qualifiedPattern, qualifiedTypeName, this.isCaseSensitive)) return POSSIBLE_MATCH;
				break;
		}
	}
	return IMPOSSIBLE_MATCH;
}
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedTypesPattern) {
		if ((element = findElement(element, accuracy)) != null) {
			SimpleSet knownTypes = ((DeclarationOfReferencedTypesPattern) this.pattern).knownTypes; 
			while (binding instanceof ReferenceBinding) {
				ReferenceBinding typeBinding = (ReferenceBinding) binding;
				reportDeclaration(typeBinding, 1, locator, knownTypes);
				binding = typeBinding.enclosingType();
			}
		}
		return;
	}

	if (binding instanceof ReferenceBinding) {
		int lastIndex = importRef.tokens.length - 1;
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		if (typeBinding instanceof ProblemReferenceBinding) {
			ProblemReferenceBinding pbBinding = (ProblemReferenceBinding) typeBinding;
			typeBinding = pbBinding.original;
			lastIndex = pbBinding.compoundName.length - 1;
		}
		// try to match all enclosing types for which the token matches as well.
		while (typeBinding != null && lastIndex >= 0) {
			if (resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding) == ACCURATE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = importRef.sourcePositions;
					int start = (int) ((positions[this.pattern.qualification == null ? lastIndex : 0]) >>> 32);
					int end = (int) positions[lastIndex];
					SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, start, end-start+1, importRef);
					locator.report(match);
				}
				return;
			}
			lastIndex--;
			typeBinding = typeBinding.enclosingType();
		}
	}
	locator.reportAccurateTypeReference(importRef, this.pattern.simpleName, element, accuracy);
}
protected void matchReportReference(ArrayTypeReference arrayRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.pattern.simpleName == null) {
		if (locator.encloses(element)) {
			int offset = arrayRef.sourceStart;
			SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, offset, arrayRef.sourceEnd-offset+1, arrayRef);
			locator.report(match);
		}
	} else
		locator.reportAccurateTypeReference(arrayRef, this.pattern.simpleName, element, accuracy);
}
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedTypesPattern) {
		if ((element = findElement(element, accuracy)) != null)
			reportDeclaration(reference, element, locator, ((DeclarationOfReferencedTypesPattern) this.pattern).knownTypes);
		return;
	}

	if (reference instanceof QualifiedNameReference)
		matchReportReference((QualifiedNameReference) reference, element, accuracy, locator);
	else if (reference instanceof QualifiedTypeReference)
		matchReportReference((QualifiedTypeReference) reference, element, accuracy, locator);
	else if (reference instanceof ArrayTypeReference)
		matchReportReference((ArrayTypeReference) reference, element, accuracy, locator);
	else {
		int offset = reference.sourceStart;
		SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
		locator.report(match);
	}
}
protected void matchReportReference(QualifiedNameReference qNameRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	Binding binding = qNameRef.binding;
	TypeBinding typeBinding = null;
	int lastIndex = qNameRef.tokens.length - 1;
	switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			lastIndex -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
			break;
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof TypeBinding)
				typeBinding = (TypeBinding) binding;
			break;
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :
			if (binding instanceof ProblemReferenceBinding) {
				typeBinding = (TypeBinding) binding;
			} else if (binding instanceof ProblemFieldBinding) {
				typeBinding = qNameRef.actualReceiverType;
				lastIndex -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
			} else if (binding instanceof ProblemBinding) {
				typeBinding = ((ProblemBinding) binding).searchType;
			}
			break;					
	}
	if (typeBinding instanceof ProblemReferenceBinding) {
		ProblemReferenceBinding pbBinding = (ProblemReferenceBinding) typeBinding;
		typeBinding = pbBinding.original;
		lastIndex = pbBinding.compoundName.length - 1;
	}
	// try to match all enclosing types for which the token matches as well.
	if (typeBinding instanceof ReferenceBinding) {
		ReferenceBinding refBinding = (ReferenceBinding) typeBinding; 
		while (refBinding != null && lastIndex >= 0) {
			if (resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, refBinding) == ACCURATE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = qNameRef.sourcePositions;
					int start = (int) ((positions[this.pattern.qualification == null ? lastIndex : 0]) >>> 32);
					int end = (int) positions[lastIndex];
					SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, start, end-start+1, qNameRef);
					locator.report(match);
				}
				return;
			}
			lastIndex--;
			refBinding = refBinding.enclosingType();
		}
	}
	locator.reportAccurateTypeReference(qNameRef, this.pattern.simpleName, element, accuracy);
}
protected void matchReportReference(QualifiedTypeReference qTypeRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	TypeBinding typeBinding = qTypeRef.resolvedType;
	int lastIndex = qTypeRef.tokens.length - 1;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding) {
		ProblemReferenceBinding pbBinding = (ProblemReferenceBinding) typeBinding;
		typeBinding = pbBinding.original;
		lastIndex = pbBinding.compoundName.length - 1;
	}
	// try to match all enclosing types for which the token matches as well
	if (typeBinding instanceof ReferenceBinding) {
		ReferenceBinding refBinding = (ReferenceBinding) typeBinding; 
		while (refBinding != null && lastIndex >= 0) {
			if (resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, refBinding) == ACCURATE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = qTypeRef.sourcePositions;
					int start = (int) ((positions[this.pattern.qualification == null ? lastIndex : 0]) >>> 32);
					int end = (int) positions[lastIndex];
					SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, start, end-start+1, qTypeRef);
					locator.report(match);
				}
				return;
			}
			lastIndex--;
			refBinding = refBinding.enclosingType();
		}
	}
	locator.reportAccurateTypeReference(qTypeRef, this.pattern.simpleName, element, accuracy);
}
protected int referenceType() {
	return IJavaElement.TYPE;
}
protected void reportDeclaration(ASTNode reference, IJavaElement element, MatchLocator locator, SimpleSet knownTypes) throws CoreException {
	int maxType = -1;
	TypeBinding typeBinding = null;
	if (reference instanceof TypeReference) {
		typeBinding = ((TypeReference) reference).resolvedType;
		maxType = Integer.MAX_VALUE;
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		Binding binding = qNameRef.binding;
		maxType = qNameRef.tokens.length - 1;
		switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
			case BindingIds.FIELD : // reading a field
				typeBinding = qNameRef.actualReceiverType;
				maxType -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
				break;
			case BindingIds.TYPE : //=============only type ==============
				if (binding instanceof TypeBinding)
					typeBinding = (TypeBinding) binding;
				break;
			case BindingIds.VARIABLE : //============unbound cases===========
			case BindingIds.TYPE | BindingIds.VARIABLE :
				if (binding instanceof ProblemFieldBinding) {
					typeBinding = qNameRef.actualReceiverType;
					maxType -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
				} else if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
					char[] partialQualifiedName = pbBinding.name;
					maxType = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
					if (typeBinding == null || maxType < 0) return;
				}
				break;
		}
	} else if (reference instanceof SingleNameReference) {
		typeBinding = (TypeBinding) ((SingleNameReference) reference).binding;
		maxType = 1;
	}
	
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding == null || typeBinding instanceof BaseTypeBinding) return;
	if (typeBinding instanceof ProblemReferenceBinding)
		typeBinding = ((ProblemReferenceBinding) typeBinding).original;
	reportDeclaration((ReferenceBinding) typeBinding, maxType, locator, knownTypes);
}
protected void reportDeclaration(ReferenceBinding typeBinding, int maxType, MatchLocator locator, SimpleSet knownTypes) throws CoreException {
	IType type = locator.lookupType(typeBinding);
	if (type == null) return; // case of a secondary type

	IResource resource = type.getResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null)
			resource = type.getJavaProject().getProject();
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile) type.getClassFile(), resource);
	}
	while (maxType >= 0 && type != null) {
		if (!knownTypes.includes(type)) {
			if (isBinary) {
				locator.reportBinaryMemberDeclaration(resource, type, info, SearchMatch.A_ACCURATE);
			} else {
				ClassScope scope = ((SourceTypeBinding) typeBinding).scope;
				if (scope != null) {
					TypeDeclaration typeDecl = scope.referenceContext;
					int offset = typeDecl.sourceStart;
					SearchMatch match = new TypeDeclarationMatch(type, SearchMatch.A_ACCURATE, offset, typeDecl.sourceEnd-offset+1, locator.getParticipant(), resource);
					locator.report(match);
				}
			}
			knownTypes.add(type);
		}
		typeBinding = typeBinding.enclosingType();
		IJavaElement parent = type.getParent();
		if (parent instanceof IType) {
			type = (IType)parent;
		} else {
			type = null;
		}
		maxType--;
	}
}
public int resolveLevel(ASTNode node) {
	if (node instanceof TypeReference)
		return resolveLevel((TypeReference) node);
	if (node instanceof NameReference)
		return resolveLevel((NameReference) node);
//	if (node instanceof ImportReference) - Not called when resolve is true, see MatchingNodeSet.reportMatching(unit)
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;

	TypeBinding typeBinding = (TypeBinding) binding;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding)
		typeBinding = ((ProblemReferenceBinding) typeBinding).original;

	if (this.pattern.focus instanceof IType && typeBinding instanceof ReferenceBinding) {
		IPackageFragment pkg = ((IType) this.pattern.focus).getPackageFragment();
		// check that type is located inside this instance of a package fragment
		if (!PackageReferenceLocator.isDeclaringPackageFragment(pkg, (ReferenceBinding) typeBinding))
			return IMPOSSIBLE_MATCH;
	}

	return resolveLevelForTypeOrEnclosingTypes(this.pattern.simpleName, this.pattern.qualification, typeBinding);
}
protected int resolveLevel(NameReference nameRef) {
	Binding binding = nameRef.binding;

	if (nameRef instanceof SingleNameReference) {
		if (binding instanceof ProblemReferenceBinding)
			binding = ((ProblemReferenceBinding) binding).original;
		if (binding instanceof ReferenceBinding)
			return resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, (ReferenceBinding) binding);
		return binding == null || binding instanceof ProblemBinding ? INACCURATE_MATCH : IMPOSSIBLE_MATCH;
	}

	TypeBinding typeBinding = null;
	QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
	switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			if (qNameRef.tokens.length < (qNameRef.otherBindings == null ? 2 : qNameRef.otherBindings.length + 2))
				return IMPOSSIBLE_MATCH; // must be at least A.x
			typeBinding = nameRef.actualReceiverType;
			break;
		case BindingIds.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no type match in it
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof TypeBinding)
				typeBinding = (TypeBinding) binding;
			break;
		/*
		 * Handling of unbound qualified name references. The match may reside in the resolved fragment,
		 * which is recorded inside the problem binding, along with the portion of the name until it became a problem.
		 */
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :
			if (binding instanceof ProblemReferenceBinding) {
				typeBinding = (TypeBinding) binding;
			} else if (binding instanceof ProblemFieldBinding) {
				if (qNameRef.tokens.length < (qNameRef.otherBindings == null ? 2 : qNameRef.otherBindings.length + 2))
					return IMPOSSIBLE_MATCH; // must be at least A.x
				typeBinding = nameRef.actualReceiverType;
			} else if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				if (CharOperation.occurencesOf('.', pbBinding.name) <= 0) // index of last bound token is one before the pb token
					return INACCURATE_MATCH;
				typeBinding = pbBinding.searchType;
			}
			break;
	}
	return resolveLevel(typeBinding);
}
protected int resolveLevel(TypeReference typeRef) {
	TypeBinding typeBinding = typeRef.resolvedType;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding)
		typeBinding = ((ProblemReferenceBinding) typeBinding).original;

	if (typeRef instanceof SingleTypeReference)
		return resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
	else
		return resolveLevelForTypeOrEnclosingTypes(this.pattern.simpleName, this.pattern.qualification, typeBinding);
}
/**
 * Returns whether the given type binding or one of its enclosing types
 * matches the given simple name pattern and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int resolveLevelForTypeOrEnclosingTypes(char[] simpleNamePattern, char[] qualificationPattern, TypeBinding binding) {
	if (binding == null) return INACCURATE_MATCH;

	if (binding instanceof ReferenceBinding) {
		ReferenceBinding type = (ReferenceBinding) binding;
		char[] qualifiedPattern = qualifiedPattern(simpleNamePattern, qualificationPattern);
		while (type != null) {
			if (resolveLevelForType(qualifiedPattern, type) == ACCURATE_MATCH) return ACCURATE_MATCH;
	
			type = type.enclosingType();
		}
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
