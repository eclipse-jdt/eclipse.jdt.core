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
/* SEARCH_15 
 * Modify PatternLocator.qualifiedPattern behavior:
 * do not add star before simple name pattern when qualification pattern is null.
 * This avoid to match p.X when pattern is X...
 */
public static char[] qualifiedPattern(char[] simpleNamePattern, char[] qualificationPattern) {
	// NOTE: if case insensitive search then simpleNamePattern & qualificationPattern are assumed to be lowercase
	if (simpleNamePattern == null) {
		if (qualificationPattern == null) return null;
		return CharOperation.concat(qualificationPattern, ONE_STAR, '.');
	} else if (qualificationPattern == null) {
		return simpleNamePattern;
	} else {
		return CharOperation.concat(qualificationPattern, simpleNamePattern, '.');
	}
}
/* SEARCH_15 
 * Modify PatternLocator.qualifiedSourceName behavior:
 * concat enclosing type when type is a only a member type.
 */
public static char[] qualifiedSourceName(TypeBinding binding) {
	if (binding instanceof ReferenceBinding) {
		ReferenceBinding type = (ReferenceBinding) binding;
		if (type.isLocalType()) {
			return CharOperation.concat(qualifiedSourceName(type.enclosingType()), new char[] {'.', '1', '.'}, type.sourceName());
		} else if (type.isMemberType()) {
			return CharOperation.concat(qualifiedSourceName(type.enclosingType()), type.sourceName(), '.');
		}
	}
	return binding != null ? binding.qualifiedSourceName() : null;
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
		return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

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
		return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	if (node instanceof SingleTypeReference) {
		if (matchesName(this.pattern.simpleName, ((SingleTypeReference) node).token))
			return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
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
	if (this.pattern.shouldExtendSelection()) {
		// SEARCH_15 do not report import ref for generic patterns...
		return;
	}
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
			if (resolveLevelForType(typeBinding) == ACCURATE_MATCH) {
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
	} else if (this.pattern.shouldExtendSelection() && arrayRef.resolvedType.isParameterizedType() && ((ParameterizedTypeBinding)arrayRef.resolvedType).arguments != null) {
		// SEARCH_15 specific report accurate match for parameterized types
		locator.reportAccurateParameterizedTypeReference(arrayRef, this.pattern.simpleName, element, accuracy);
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
			if (resolveLevelForType(refBinding) == ACCURATE_MATCH) {
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
			if (resolveLevelForType(refBinding) != IMPOSSIBLE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = qTypeRef.sourcePositions;
					int start = (int) ((positions[this.pattern.qualification == null ? lastIndex : 0]) >>> 32);
					int end = (int) positions[lastIndex];
					if (this.pattern.shouldExtendSelection() && refBinding.isParameterizedType() && ((ParameterizedTypeBinding)refBinding).arguments != null) {
						// SEARCH_15 specific report accurate match for parameterized types
						locator.reportAccurateParameterizedTypeReference(qTypeRef, this.pattern.simpleName, element, accuracy);
					} else {
						SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, start, end-start+1, qTypeRef);
						locator.report(match);
					}
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
	if (typeBinding instanceof ProblemReferenceBinding) {
		ReferenceBinding original = ((ProblemReferenceBinding) typeBinding).original;
		if (original == null) return; // original may be not set (example in bug 71279)
		typeBinding = original;
	}
	typeBinding = typeBinding.erasure();
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
				if (typeBinding instanceof ParameterizedTypeBinding)
					typeBinding = ((ParameterizedTypeBinding) typeBinding).type;
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

	if (((InternalSearchPattern) this.pattern).focus instanceof IType && typeBinding instanceof ReferenceBinding) {
		IPackageFragment pkg = ((IType) ((InternalSearchPattern) this.pattern).focus).getPackageFragment();
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
			return resolveLevelForType((ReferenceBinding) binding);
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

	if (typeRef instanceof SingleTypeReference) {
		return resolveLevelForType(typeBinding);
	} else
		return resolveLevelForTypeOrEnclosingTypes(this.pattern.simpleName, this.pattern.qualification, typeBinding);
}
/* (non-Javadoc)
 * SEARCH_15
 * Resolve level for type with a given binding.
 * This is just an helper to avoid call of method with all parameters...
 */
protected int resolveLevelForType(TypeBinding typeBinding) {
	return resolveLevelForType(
			this.pattern.simpleName,
			this.pattern.qualification,
			this.pattern.typeNames,
			((InternalSearchPattern)this.pattern).mustResolve,
			this.pattern.declaration,
			typeBinding);
}
/* (non-Javadoc)
 * SEARCH_15
 * Overrides PatternLocator method behavior in order to accept member pattern as X.Member
 * @see org.eclipse.jdt.internal.core.search.matching.PatternLocator#resolveLevelForType(char[], char[], org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
protected int resolveLevelForType (char[] simpleNamePattern, char[] qualificationPattern, TypeBinding type) {
	char[] qualifiedPattern = qualifiedPattern(simpleNamePattern, qualificationPattern);
	int level = resolveLevelForType(qualifiedPattern, type);
	if (level == ACCURATE_MATCH || type == null) return level;
	boolean match = false;
	if (type.isMemberType() || type.isLocalType()) {
		if (qualificationPattern != null) {
			match = CharOperation.equals(qualifiedPattern, qualifiedSourceName(type), this.isCaseSensitive);
		} else {
			match = CharOperation.equals(qualifiedPattern, type.sourceName(), this.isCaseSensitive);
		}
	} else if (qualificationPattern == null) {
		match = CharOperation.equals(qualifiedPattern, qualifiedSourceName(type), this.isCaseSensitive);
	}
	return match ? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
}
/* (non-Javadoc)
 * SEARCH_15
 * Resolve level for type with a given binding with all pattern information.
 */
protected int resolveLevelForType (char[] simpleNamePattern,
									char[] qualificationPattern,
									char[][] typeNames,
									boolean mustResolve,
									boolean declaration,
									TypeBinding type) {
	int level = resolveLevelForType(simpleNamePattern, qualificationPattern, type);
	if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
	if (type == null) return level;

	// pattern has no type parameter
	if (typeNames == null || typeNames.length == 0) {
		return level;
	}
	
	// pattern has type parameter(s) or type argument(s)
	boolean isRawType = type.isRawType();
	if (type.isGenericType()) {
		// Binding is generic, get its type variable(s)
		TypeVariableBinding[] typeVariables = null;
		if (type instanceof SourceTypeBinding) {
			SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) type;
			typeVariables = sourceTypeBinding.typeVariables;
		} else if (type instanceof BinaryTypeBinding) {
			BinaryTypeBinding binaryTypeBinding = (BinaryTypeBinding) type;
			if (mustResolve)
				typeVariables = binaryTypeBinding.typeVariables(); // TODO (frederic) do we really want to resolve?
		}
		// type variables length must match at least specified type names length
		if (typeVariables == null || typeVariables.length == 0) {
			return IMPOSSIBLE_MATCH;
		}
		int length = typeNames.length;
		if (typeVariables.length != length) return IMPOSSIBLE_MATCH;
		// verify each parameters
		return level; // we can't do better
		// TODO (frederic) need to do more verifications here?
	} else if (!type.isParameterizedType() && !isRawType) {
		// Standard types (ie. neither generic nor parameterized nor raw types)
		// cannot match pattern when it has type parameters or arguments
		return IMPOSSIBLE_MATCH;
	} else {
		// Binding is parameterized type
		ParameterizedTypeBinding paramTypeBinding = (ParameterizedTypeBinding) type;
		if (paramTypeBinding.arguments == null) {
			// binding has no type parameters => ok for raw types
			if (isRawType) return level;
			// need to verify hierarchy for member types
			if (type.isMemberType() && qualificationPattern != null) {
				int lastDot = CharOperation.lastIndexOf('.', qualificationPattern);
				char[] enclosingQualificationPattern = lastDot==-1 ? null : CharOperation.subarray(qualificationPattern, 0, lastDot);
				char[] enclosingSimpleNamePattern = lastDot==-1 ? qualificationPattern : CharOperation.subarray(qualificationPattern, lastDot+1, qualificationPattern.length);
				if (resolveLevelForType(enclosingSimpleNamePattern, enclosingQualificationPattern, typeNames, mustResolve, declaration, paramTypeBinding.enclosingType()) == IMPOSSIBLE_MATCH) {
					return IMPOSSIBLE_MATCH;
				}
				return level;
			}
			return IMPOSSIBLE_MATCH;
		}
		// type parameters length must match at least specified type names length
		int length = typeNames.length;
		if (paramTypeBinding.arguments.length != length) return IMPOSSIBLE_MATCH;
		// verify each type parameter
		if (declaration) {
			// TODO (frederic) more verification to do here with type parameter bounds?
			return level;
		}
		nextTypeArgument: for (int i= 0; i<length; i++) {
			char[] argType = typeNames[i];
			TypeBinding argTypeBinding = paramTypeBinding.arguments[i];
			// get pattern wildcard
			int patternWildcard = this.pattern.wildcards[i];
			if (patternWildcard == Wildcard.UNBOUND) continue;
			// try to resolve pattern
			TypeBinding patternBinding = this.pattern.getTypeNameBinding(this.unitScope, i);
			if (patternBinding != null) {
				// We can bind pattern type name => verify that types are compatible
				if (argTypeBinding == patternBinding) continue;
				if (argTypeBinding.isWildcard()) {
					TypeBinding bound = ((WildcardBinding) argTypeBinding).bound;
					if (this.pattern.wildcards != null) {
						switch (this.pattern.wildcards[i]) {
							case Wildcard.SUPER:
								if (bound == null || patternBinding.isCompatibleWith(bound))
									// argument type is in bound hierarchy => valid
									continue;
								break;
							case Wildcard.EXTENDS:
								if (bound == null || bound.isCompatibleWith(patternBinding))
									// argument type is a subclass of bound => valid
									continue;
								break;
							default: //UNBOUND
								// there's no bound name to match => valid
								continue;
						}
					}
				}
				return IMPOSSIBLE_MATCH;
			}
			
			// pattern hasn't be solved, try to see if names match in hierarchy
			// First if type argument is a wildcard
			if (argTypeBinding.isWildcard()) {
				WildcardBinding wildcardBinding = (WildcardBinding) argTypeBinding;
				switch (wildcardBinding.kind) {
					case Wildcard.EXTENDS:
						// We cannot know in this case...
						level = INACCURATE_MATCH;
					case Wildcard.UNBOUND:
						// there's no bound name to match => valid
						continue;
				}
				// try to match name in hierarchy
				ReferenceBinding boundBinding = (ReferenceBinding) wildcardBinding.bound;
				while (boundBinding != null) {
					if (CharOperation.equals(argType, boundBinding.shortReadableName(), this.isCaseSensitive) ||
						CharOperation.equals(argType, boundBinding.readableName(), this.isCaseSensitive)) {
						continue nextTypeArgument;
					}
					boundBinding = boundBinding.superclass();
				}
				return IMPOSSIBLE_MATCH;
			}
			
			// try to match names when there's no wildcard
			ReferenceBinding refBinding = null;
			if (argTypeBinding.isArrayType()) {
				TypeBinding leafBinding = ((ArrayBinding) argTypeBinding).leafComponentType;
				if (!leafBinding.isBaseType()) {
					refBinding = (ReferenceBinding) leafBinding;
				}
			} else if (!argTypeBinding.isBaseType()) {
				refBinding = (ReferenceBinding) argTypeBinding;
			}
			if (refBinding == null) {
				// Based type
				if (!CharOperation.equals(argType, argTypeBinding.shortReadableName(), this.isCaseSensitive) &&
					!CharOperation.equals(argType, argTypeBinding.readableName(), this.isCaseSensitive)) {
					return IMPOSSIBLE_MATCH;
				}
			} else {
				while (refBinding != null) {
					if (CharOperation.equals(argType, refBinding.shortReadableName(), this.isCaseSensitive) ||
						CharOperation.equals(argType, refBinding.readableName(), this.isCaseSensitive)) {
						continue nextTypeArgument;
					}
					refBinding = refBinding.superclass();
				}
				return IMPOSSIBLE_MATCH;
			}
		}
		return level;
	}
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
		while (type != null) {
			int level = resolveLevelForType(type);
			if (level != IMPOSSIBLE_MATCH) return level;
	
			type = type.enclosingType();
		}
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
