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
	// need accurate match to be able to open on type ref
	if (accuracy == IJavaSearchResultCollector.POTENTIAL_MATCH) return null;

	// element that references the type must be included in the enclosing element
	DeclarationOfReferencedTypesPattern declPattern = (DeclarationOfReferencedTypesPattern) this.pattern; 
	while (element != null && !declPattern.enclosingElement.equals(element))
		element = element.getParent();
	return element;
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
public void match(Reference node, MatchingNodeSet nodeSet) { // interested in NameReference & its subtypes
	if (!(node instanceof NameReference)) return;

	if (this.pattern.simpleName == null) {
		nodeSet.addMatch(node, this.pattern.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH);
	} else if (node instanceof SingleNameReference) {
		if (matchesName(this.pattern.simpleName, ((SingleNameReference) node).token))
			nodeSet.addMatch(node, POTENTIAL_MATCH); // resolution is needed to find out if it is a type ref 
	} else {
		char[][] tokens = ((QualifiedNameReference) node).tokens;
		for (int i = 0, max = tokens.length; i < max; i++) {
			if (matchesName(this.pattern.simpleName, tokens[i])) {
				nodeSet.addMatch(node, POTENTIAL_MATCH); // resolution is needed to find out if it is a type ref
				return;
			}
		}
	}
}
//public void match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public void match(TypeReference node, MatchingNodeSet nodeSet) {
	if (this.pattern.simpleName == null)
		nodeSet.addMatch(node, this.pattern.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH);

	if (node instanceof SingleTypeReference) {
		if (matchesName(this.pattern.simpleName, ((SingleTypeReference) node).token))
			nodeSet.addMatch(node, this.pattern.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH);
	} else {
		char[][] tokens = ((QualifiedTypeReference) node).tokens;
		for (int i = 0, max = tokens.length; i < max; i++) {
			if (matchesName(this.pattern.simpleName, tokens[i])) {
				nodeSet.addMatch(node, POTENTIAL_MATCH); // resolution is needed to find out if it is a type ref
				return;
			}
		}
	}
}

protected int matchLevel(ImportReference importRef) {
	if (this.pattern.qualification != null) {
		char[][] tokens = importRef.tokens;
		char[] pattern = this.pattern.simpleName == null
			? this.pattern.qualification
			: CharOperation.concat(this.pattern.qualification, this.pattern.simpleName, '.');
		char[] qualifiedTypeName = CharOperation.concatWith(tokens, '.');
		switch (this.matchMode) {
			case IJavaSearchConstants.EXACT_MATCH :
			case IJavaSearchConstants.PREFIX_MATCH :
				if (CharOperation.prefixEquals(pattern, qualifiedTypeName, this.isCaseSensitive)) return POTENTIAL_MATCH;
				break;
			case IJavaSearchConstants.PATTERN_MATCH:
				if (CharOperation.match(pattern, qualifiedTypeName, this.isCaseSensitive)) return POTENTIAL_MATCH;
				break;
		}
	} else {
		if (this.pattern.simpleName == null) return ACCURATE_MATCH;
		char[][] tokens = importRef.tokens;
		for (int i = 0, length = tokens.length; i < length; i++)
			if (matchesName(this.pattern.simpleName, tokens[i])) return ACCURATE_MATCH;
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

	ReferenceBinding typeBinding = null;
	if (binding instanceof ReferenceBinding)
		typeBinding = (ReferenceBinding) binding;

	char[][] typeTokens = importRef.tokens;
	int lastIndex = typeTokens.length-1;
	char[][] tokens = null;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.pattern.simpleName, typeTokens[lastIndex--])) {
			int level = resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(typeTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		typeBinding = typeBinding.enclosingType();
	}
	if (tokens == null) {
		tokens = typeBinding == null || typeBinding instanceof ProblemReferenceBinding
			? new char[][] {this.pattern.simpleName}
			: importRef.tokens;
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++)
				lowerCaseTokens[i] = CharOperation.toLowerCase(tokens[i]);
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(importRef.sourceStart, importRef.sourceEnd, tokens, element, accuracy);
}
protected void matchReportReference(ArrayTypeReference arrayRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = this.pattern.simpleName == null ? CharOperation.NO_CHAR_CHAR : new char[][] {this.pattern.simpleName};
	locator.reportAccurateReference(arrayRef.sourceStart, arrayRef.sourceEnd, tokens, element, accuracy);
}
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
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
	else
		super.matchReportReference(reference, element, accuracy, locator);
}
protected void matchReportReference(QualifiedNameReference qNameRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	Binding binding = qNameRef.binding;
	TypeBinding typeBinding = null;
	char[][] nameTokens = qNameRef.tokens;
	int lastIndex = nameTokens.length-1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			break;
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
			} else {
				typeBinding = (TypeBinding)binding;
			}
			break;
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :						
			if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				typeBinding = pbBinding.searchType; // second chance with recorded type so far
				char[] partialQualifiedName = pbBinding.name;
				lastIndex = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
			}
			break;
	}
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.pattern.simpleName, nameTokens[lastIndex--])) {
			int level = resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(nameTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		typeBinding = typeBinding instanceof ReferenceBinding
			? ((ReferenceBinding)typeBinding).enclosingType()
			: null;
	} 
	if (tokens == null) {
		tokens = binding == null || binding instanceof ProblemBinding
			? new char[][] {this.pattern.simpleName}
			: qNameRef.tokens;
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(qNameRef.sourceStart, qNameRef.sourceEnd, tokens, element, accuracy);
}
protected void matchReportReference(QualifiedTypeReference qTypeRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	char[][] tokens = null;
	TypeBinding typeBinding = qTypeRef.resolvedType;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	char[][] typeTokens = qTypeRef.tokens;
	int lastIndex = typeTokens.length-1;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.pattern.simpleName, typeTokens[lastIndex--])) {
			int level = resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) {
				tokens = new char[lastIndex+2][];
				System.arraycopy(typeTokens, 0, tokens, 0, lastIndex+2);
				break;
			}
		}
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		else
			typeBinding = null;
	}
	if (tokens == null) {
		if (typeBinding == null || typeBinding instanceof ProblemReferenceBinding)
			tokens = new char[][] {this.pattern.simpleName};
		else
			tokens = qTypeRef.tokens;
		if (!this.isCaseSensitive) {
			int length = tokens.length;
			char[][] lowerCaseTokens = new char[length][];
			for (int i = 0; i < length; i++) {
				char[] token = tokens[i];
				lowerCaseTokens[i] = CharOperation.toLowerCase(token);
			}
			tokens = lowerCaseTokens;
		}
	}
	locator.reportAccurateReference(qTypeRef.sourceStart, qTypeRef.sourceEnd, tokens, element, accuracy);
}
protected void reportDeclaration(AstNode reference, IJavaElement element, MatchLocator locator, SimpleSet knownTypes) throws CoreException {
	int maxType = -1;
	TypeBinding typeBinding = null;
	if (reference instanceof TypeReference) {
		typeBinding = ((TypeReference) reference).resolvedType;
		maxType = Integer.MAX_VALUE;
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		Binding binding = qNameRef.binding;
		maxType = qNameRef.tokens.length - 1;
		switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
			case BindingIds.FIELD : // reading a field
				typeBinding = qNameRef.actualReceiverType;
				int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
				maxType -= otherBindingsCount + 1;
				break;
			case BindingIds.TYPE : //=============only type ==============
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
					char[] partialQualifiedName = pbBinding.name;
					maxType = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
					if (typeBinding == null || maxType < 0) return;
				} else {
					typeBinding = (TypeBinding)binding;
				}
				break;
			case BindingIds.VARIABLE : //============unbound cases===========
			case BindingIds.TYPE | BindingIds.VARIABLE :						
				if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
					char[] partialQualifiedName = pbBinding.name;
					maxType = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
					if (typeBinding == null || maxType < 0) return;
				}
				break;
		}
	} else if (reference instanceof SingleNameReference) {
		typeBinding = (TypeBinding)((SingleNameReference) reference).binding;
		maxType = 1;
	}
	
	if (typeBinding == null || typeBinding instanceof BaseTypeBinding) return;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	this.reportDeclaration(typeBinding, maxType, locator, knownTypes);
}
protected void reportDeclaration(TypeBinding typeBinding, int maxType, MatchLocator locator, SimpleSet knownTypes) throws CoreException {
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
				locator.reportBinaryMatch(resource, type, info, IJavaSearchResultCollector.EXACT_MATCH);
			} else {
				ClassScope scope = ((SourceTypeBinding) typeBinding).scope;
				if (scope != null) {
					TypeDeclaration typeDecl = scope.referenceContext;
					locator.report(resource, typeDecl.sourceStart, typeDecl.sourceEnd, type, IJavaSearchResultCollector.EXACT_MATCH);
				}
			}
			knownTypes.add(type);
		}
		typeBinding = ((ReferenceBinding) typeBinding).enclosingType();
		IJavaElement parent = type.getParent();
		if (parent instanceof IType) {
			type = (IType)parent;
		} else {
			type = null;
		}
		maxType--;
	}
}
public int resolveLevel(AstNode node) {
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
	if (typeBinding instanceof ProblemReferenceBinding) return INACCURATE_MATCH;

	while (typeBinding != null) {
		int level = resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
		if (level != IMPOSSIBLE_MATCH) return level;
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding) typeBinding).enclosingType();
		else return IMPOSSIBLE_MATCH;
	}
	return IMPOSSIBLE_MATCH;
}
protected int resolveLevel(NameReference nameRef) {
	Binding binding = nameRef.binding;

	if (nameRef instanceof SingleNameReference) {
		if (binding instanceof ProblemReferenceBinding)
			binding = ((ProblemReferenceBinding) binding).original;
		if (binding instanceof VariableBinding) return IMPOSSIBLE_MATCH;
		if (!(binding instanceof TypeBinding)) return INACCURATE_MATCH;
		return resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, (TypeBinding) binding);
	}

	TypeBinding typeBinding = null;
	QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
	char[][] tokens = qNameRef.tokens;
	int lastIndex = tokens.length-1;
	switch (qNameRef.bits & AstNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			typeBinding = nameRef.actualReceiverType;
			// no valid match amongst fields
			int otherBindingsCount = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;			
			lastIndex -= otherBindingsCount + 1;
			if (lastIndex < 0) return IMPOSSIBLE_MATCH;
			break;
		case BindingIds.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no type match in it
		case BindingIds.TYPE : //=============only type ==============
			if (binding instanceof ProblemReferenceBinding)
				binding = ((ProblemReferenceBinding) binding).original;
			if (!(binding instanceof TypeBinding)) return INACCURATE_MATCH;
			typeBinding = (TypeBinding) binding;
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
			} else if (binding instanceof ProblemReferenceBinding) {
				ProblemReferenceBinding pbBinding = (ProblemReferenceBinding)binding;
				binding = pbBinding.original;
				if (!(binding instanceof TypeBinding)) return INACCURATE_MATCH;
				typeBinding = (TypeBinding) binding;
				char[][] partialQualifiedName = pbBinding.compoundName;
				lastIndex = partialQualifiedName == null ? -1 : partialQualifiedName.length - 1; // index of last bound token is one before the pb token
				if (typeBinding == null || lastIndex < 0) return INACCURATE_MATCH;
			}
			break;
	}
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.pattern.simpleName, tokens[lastIndex--])) {
			int level = resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding) typeBinding).enclosingType();
		else
			typeBinding = null;
	}
	return IMPOSSIBLE_MATCH;
}
protected int resolveLevel(TypeReference typeRef) {
	TypeBinding typeBinding = typeRef.resolvedType;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding) {
		Binding binding = ((ProblemReferenceBinding) typeBinding).original;
		if (binding instanceof TypeBinding)
			typeBinding = (TypeBinding) binding;
		else if (binding == null)
			typeBinding = null;
	}
	if (typeBinding == null) return INACCURATE_MATCH;

	if (typeRef instanceof SingleTypeReference)
		return resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);

	QualifiedTypeReference qTypeRef = (QualifiedTypeReference) typeRef;
	char[][] tokens = qTypeRef.tokens;
	int lastIndex = tokens.length-1;
	// try to match all enclosing types for which the token matches as well.
	while (typeBinding != null && lastIndex >= 0) {
		if (matchesName(this.pattern.simpleName, tokens[lastIndex--])) {
			int level = resolveLevelForType(this.pattern.simpleName, this.pattern.qualification, typeBinding);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
		if (typeBinding instanceof ReferenceBinding)
			typeBinding = ((ReferenceBinding)typeBinding).enclosingType();
		else return IMPOSSIBLE_MATCH;
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString();
}
}
