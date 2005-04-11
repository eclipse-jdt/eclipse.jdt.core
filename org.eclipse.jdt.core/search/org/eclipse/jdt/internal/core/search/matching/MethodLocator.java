/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class MethodLocator extends PatternLocator {

protected MethodPattern pattern;
protected boolean isDeclarationOfReferencedMethodsPattern;

//extra reference info
public char[][][] allSuperDeclaringTypeNames;

public MethodLocator(MethodPattern pattern) {
	super(pattern);

	this.pattern = pattern;
	this.isDeclarationOfReferencedMethodsPattern = this.pattern instanceof DeclarationOfReferencedMethodsPattern;
}
public void initializePolymorphicSearch(MatchLocator locator) {
	try {
		this.allSuperDeclaringTypeNames =
			new SuperTypeNamesCollector(
				this.pattern,
				this.pattern.declaringSimpleName,
				this.pattern.declaringQualification,
				locator,
				this.pattern.declaringType,
				locator.progressMonitor).collect();
	} catch (JavaModelException e) {
		// inaccurate matches will be found
	}
}
/**
 * Returns whether the code gen will use an invoke virtual for 
 * this message send or not.
 */
protected boolean isVirtualInvoke(MethodBinding method, MessageSend messageSend) {
	return !method.isStatic() && !method.isPrivate() && !messageSend.isSuperAccess();
}
public int match(ASTNode node, MatchingNodeSet nodeSet) {
	int declarationsLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findReferences) {
		if (node instanceof ImportReference) {
			// With static import, we can have static method reference in import reference
			ImportReference importRef = (ImportReference) node;
			int length = importRef.tokens.length-1;
			if (importRef.isStatic() && !importRef.onDemand && matchesName(this.pattern.selector, importRef.tokens[length])) {
				char[][] compoundName = new char[length][];
				System.arraycopy(importRef.tokens, 0, compoundName, 0, length);
				char[] declaringType = CharOperation.concat(pattern.declaringQualification, pattern.declaringSimpleName, '.');
				if (matchesName(declaringType, CharOperation.concatWith(compoundName, '.'))) {
					declarationsLevel = ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
				}
			}
		}
	}
	return nodeSet.addMatch(node, declarationsLevel);
}
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(MethodDeclaration node, MatchingNodeSet nodeSet) {
	if (!this.pattern.findDeclarations) return IMPOSSIBLE_MATCH;

	// Verify method name
	if (!matchesName(this.pattern.selector, node.selector)) return IMPOSSIBLE_MATCH;
	
	// Verify parameters types
	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		ASTNode[] args = node.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
		for (int i = 0; i < argsLength; i++) {
			if (!matchesTypeReference(this.pattern.parameterSimpleNames[i], ((Argument) args[i]).type)) return IMPOSSIBLE_MATCH;
		}
	}

	// Verify type arguments (do not reject if pattern has no argument as it can be an erasure match)
	if (this.pattern.hasMethodArguments()) {
		if (node.typeParameters == null || node.typeParameters.length != this.pattern.methodArguments.length) return IMPOSSIBLE_MATCH;
	}

	// Method declaration may match pattern
	return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
public int match(MessageSend node, MatchingNodeSet nodeSet) {
	if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;

	if (!matchesName(this.pattern.selector, node.selector)) return IMPOSSIBLE_MATCH;
	if (this.pattern.parameterSimpleNames != null && (this.pattern.shouldCountParameter() || ((node.bits & ASTNode.InsideJavadoc) != 0))) {
		int length = this.pattern.parameterSimpleNames.length;
		ASTNode[] args = node.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
	}

	return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
//public int match(Reference node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

protected int matchContainer() {
	if (this.pattern.findReferences) {
		// need to look almost everywhere to find in javadocs and static import
		return ALL_CONTAINER;
	}
	return CLASS_CONTAINER;
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.matching.PatternLocator#matchLevelAndReportImportRef(org.eclipse.jdt.internal.compiler.ast.ImportReference, org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.core.search.matching.MatchLocator)
 * Accept to report match of static field on static import
 */
protected void matchLevelAndReportImportRef(ImportReference importRef, Binding binding, MatchLocator locator) throws CoreException {
	if (importRef.isStatic() && binding instanceof MethodBinding) {
		super.matchLevelAndReportImportRef(importRef, binding, locator);
	}
}
protected int matchMethod(MethodBinding method) {
	if (!matchesName(this.pattern.selector, method.selector)) return IMPOSSIBLE_MATCH;

	int level = ACCURATE_MATCH;
	// look at return type only if declaring type is not specified
	if (this.pattern.declaringSimpleName == null) {
		// TODO (frederic) use this call to refine accuracy on return type
		// int newLevel = resolveLevelForType(this.pattern.returnSimpleName, this.pattern.returnQualification, this.pattern.returnTypeArguments, 0, method.returnType);
		int newLevel = resolveLevelForType(this.pattern.returnSimpleName, this.pattern.returnQualification, method.returnType);
		if (level > newLevel) {
			if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
			level = newLevel; // can only be downgraded
		}
	}

	// parameter types
	int parameterCount = this.pattern.parameterSimpleNames == null ? -1 : this.pattern.parameterSimpleNames.length;
	if (parameterCount > -1) {
		// global verification
		if (method.parameters == null) return INACCURATE_MATCH;
		if (parameterCount != method.parameters.length) return IMPOSSIBLE_MATCH;
		if (!method.isValidBinding() && ((ProblemMethodBinding)method).problemId() == ProblemReasons.Ambiguous) {
			// return inaccurate match for ambiguous call (bug 80890)
			return INACCURATE_MATCH;
		}

		// verify each parameter
		for (int i = 0; i < parameterCount; i++) {
			TypeBinding argType = method.parameters[i];
			int newLevel = IMPOSSIBLE_MATCH;
			if (argType.isMemberType()) {
				// only compare source name for member type (bug 41018)
				newLevel = CharOperation.match(this.pattern.parameterSimpleNames[i], argType.sourceName(), this.isCaseSensitive)
					? ACCURATE_MATCH
					: IMPOSSIBLE_MATCH;
			} else {
				// TODO (frederic) use this call to refine accuracy on parameter types
//				 newLevel = resolveLevelForType(this.pattern.parameterSimpleNames[i], this.pattern.parameterQualifications[i], this.pattern.parametersTypeArguments[i], 0, argType);
				newLevel = resolveLevelForType(this.pattern.parameterSimpleNames[i], this.pattern.parameterQualifications[i], argType);
			}
			if (level > newLevel) {
				if (newLevel == IMPOSSIBLE_MATCH) {
//					if (isErasureMatch) {
//						return ERASURE_MATCH;
//					}
					return IMPOSSIBLE_MATCH;
				}
				level = newLevel; // can only be downgraded
			}
		}
	}

	return level;
}
/**
 * @see org.eclipse.jdt.internal.core.search.matching.PatternLocator#matchReportReference(org.eclipse.jdt.internal.compiler.ast.ASTNode, org.eclipse.jdt.core.IJavaElement, Binding, int, org.eclipse.jdt.internal.core.search.matching.MatchLocator)
 */
protected void matchReportReference(ASTNode reference, IJavaElement element, Binding elementBinding, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedMethodsPattern) {
		// need exact match to be able to open on type ref
		if (accuracy != SearchMatch.A_ACCURATE) return;

		// element that references the method must be included in the enclosing element
		DeclarationOfReferencedMethodsPattern declPattern = (DeclarationOfReferencedMethodsPattern) this.pattern; 
		while (element != null && !declPattern.enclosingElement.equals(element))
			element = element.getParent();
		if (element != null) {
			reportDeclaration(((MessageSend) reference).binding, locator, declPattern.knownMethods);
		}
	} else {
		match = locator.newMethodReferenceMatch(element, elementBinding, accuracy, -1, -1, false /*not constructor*/, false/*not synthetic*/, reference);
		if (this.pattern.findReferences && reference instanceof MessageSend) {
			IJavaElement focus = ((InternalSearchPattern) this.pattern).focus;
			// verify closest match if pattern was bound
			// (see bug 70827)
			if (focus != null && focus.getElementType() == IJavaElement.METHOD) {
				MethodBinding method = ((MessageSend)reference).binding;
				boolean isPrivate = Flags.isPrivate(((IMethod) focus).getFlags());
				if (isPrivate && !CharOperation.equals(method.declaringClass.sourceName, focus.getParent().getElementName().toCharArray())) {
					return; // finally the match was not possible
				}
			}
			matchReportReference((MessageSend)reference, locator, ((MessageSend)reference).binding);
		} else {
			int offset = reference.sourceStart;
			match.setOffset(offset);
			match.setLength(reference.sourceEnd-offset+1);
			locator.report(match);
		}
	}
}
void matchReportReference(MessageSend messageSend, MatchLocator locator, MethodBinding methodBinding) throws CoreException {

	// Look if there's a need to special report for parameterized type
	boolean isParameterized = false;
	if (methodBinding instanceof ParameterizedGenericMethodBinding) { // parameterized generic method
		isParameterized = true;

		// Update match regarding method type arguments
		ParameterizedGenericMethodBinding parameterizedMethodBinding = (ParameterizedGenericMethodBinding) methodBinding;
		match.setRaw(parameterizedMethodBinding.isRaw);
		TypeBinding[] typeArguments = /*parameterizedMethodBinding.isRaw ? null :*/ parameterizedMethodBinding.typeArguments;
		updateMatch(typeArguments, locator, this.pattern.methodArguments, this.pattern.hasMethodParameters());

		// Update match regarding declaring class type arguments
		if (methodBinding.declaringClass.isParameterizedType() || methodBinding.declaringClass.isRawType()) {
			ParameterizedTypeBinding parameterizedBinding = (ParameterizedTypeBinding)methodBinding.declaringClass;
			if (!this.pattern.hasTypeArguments() && this.pattern.hasMethodArguments()) {
				// special case for pattern which defines method arguments but not its declaring type
				// in this case, we do not refine accuracy using declaring type arguments...!
			} else {
				updateMatch(parameterizedBinding, this.pattern.getTypeArguments(), this.pattern.hasTypeParameters(), 0, locator);
			}
		} else if (this.pattern.hasTypeArguments()) {
			match.setRule(SearchPattern.R_ERASURE_MATCH);
		}

		// Update match regarding method parameters
		// TODO ? (frederic)

		// Update match regarding method return type
		// TODO ? (frederic)
	} else if (methodBinding instanceof ParameterizedMethodBinding) {
		isParameterized = true;
		if (methodBinding.declaringClass.isParameterizedType() || methodBinding.declaringClass.isRawType()) {
			ParameterizedTypeBinding parameterizedBinding = (ParameterizedTypeBinding)methodBinding.declaringClass;
			updateMatch(parameterizedBinding, this.pattern.getTypeArguments(), this.pattern.hasTypeParameters(), 0, locator);
		} else if (this.pattern.hasTypeArguments()) {
			match.setRule(SearchPattern.R_ERASURE_MATCH);
		}

		// Update match regarding method parameters
		// TODO ? (frederic)

		// Update match regarding method return type
		// TODO ? (frederic)
	} else if (this.pattern.hasMethodArguments()) { // binding has no type params, compatible erasure if pattern does
		match.setRule(SearchPattern.R_ERASURE_MATCH);
	}

	// See whether it is necessary to report or not
	if (match.getRule() == 0) return; // impossible match
	boolean report = (this.isErasureMatch && match.isErasure()) || (this.isEquivalentMatch && match.isEquivalent()) || match.isExact();
	if (!report) return;

	// Report match
	int offset = (int) (messageSend.nameSourcePosition >>> 32);
	match.setOffset(offset);
	match.setLength(messageSend.sourceEnd - offset + 1);
	 if (isParameterized && this.pattern.hasMethodArguments())  {
		locator.reportAccurateParameterizedMethodReference(match, messageSend, messageSend.typeArguments);
	} else {
		locator.report(match);
	}
}
protected int referenceType() {
	return IJavaElement.METHOD;
}
protected void reportDeclaration(MethodBinding methodBinding, MatchLocator locator, SimpleSet knownMethods) throws CoreException {
	ReferenceBinding declaringClass = methodBinding.declaringClass;
	IType type = locator.lookupType(declaringClass);
	if (type == null) return; // case of a secondary type

	char[] bindingSelector = methodBinding.selector;
	TypeBinding[] parameters = methodBinding.parameters;
	int parameterLength = parameters.length;
	String[] parameterTypes = new String[parameterLength];
	for (int i = 0; i  < parameterLength; i++)
		parameterTypes[i] = Signature.createTypeSignature(parameters[i].sourceName(), false);
	IMethod method = type.getMethod(new String(bindingSelector), parameterTypes);
	if (knownMethods.includes(method)) return;

	knownMethods.add(method);
	IResource resource = type.getResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null)
			resource = type.getJavaProject().getProject();
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile)type.getClassFile(), resource);
		locator.reportBinaryMemberDeclaration(resource, method, methodBinding, info, SearchMatch.A_ACCURATE);
	} else {
		if (declaringClass instanceof ParameterizedTypeBinding)
			declaringClass = ((ParameterizedTypeBinding) declaringClass).type;
		ClassScope scope = ((SourceTypeBinding) declaringClass).scope;
		if (scope != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			AbstractMethodDeclaration methodDecl = null;
			AbstractMethodDeclaration[] methodDecls = typeDecl.methods;
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				if (CharOperation.equals(bindingSelector, methodDecls[i].selector)) {
					methodDecl = methodDecls[i];
					break;
				}
			} 
			if (methodDecl != null) {
				int offset = methodDecl.sourceStart;
				Binding binding = methodDecl.binding;
				if (binding != null)
					method = (IMethod) ((JavaElement) method).resolved(binding);
				match = new MethodDeclarationMatch(method, SearchMatch.A_ACCURATE, offset, methodDecl.sourceEnd-offset+1, locator.getParticipant(), resource);
				locator.report(match);
			}
		}
	}
}
public int resolveLevel(ASTNode possibleMatchingNode) {
	if (this.pattern.findReferences && possibleMatchingNode instanceof MessageSend)
		return resolveLevel((MessageSend) possibleMatchingNode);
	if (this.pattern.findDeclarations && possibleMatchingNode instanceof MethodDeclaration)
		return resolveLevel(((MethodDeclaration) possibleMatchingNode).binding);
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;

	MethodBinding method = (MethodBinding) binding;
	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) {
		if (method != method.original()) methodLevel = matchMethod(method.original());
		if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
		method = method.original();
	}

	// declaring type
	char[] qualifiedPattern = qualifiedPattern(this.pattern.declaringSimpleName, this.pattern.declaringQualification);
	if (qualifiedPattern == null) return methodLevel; // since any declaring class will do

	boolean subType = !method.isStatic() && !method.isPrivate();
	if (subType && this.pattern.declaringQualification != null && method.declaringClass != null && method.declaringClass.fPackage != null) {
		subType = CharOperation.compareWith(this.pattern.declaringQualification, method.declaringClass.fPackage.shortReadableName()) == 0;
	}
	int declaringLevel = subType
		? resolveLevelAsSubtype(qualifiedPattern, method.declaringClass)
		: resolveLevelForType(qualifiedPattern, method.declaringClass);
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
protected int resolveLevel(MessageSend messageSend) {
	MethodBinding method = messageSend.binding;
	if (method == null || messageSend.resolvedType == null) return INACCURATE_MATCH;
	
	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) {
		if (method != method.original()) methodLevel = matchMethod(method.original());
		if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
		method = method.original();
	}

	// receiver type
	char[] qualifiedPattern = qualifiedPattern(this.pattern.declaringSimpleName, this.pattern.declaringQualification);
	if (qualifiedPattern == null) return methodLevel; // since any declaring class will do

	int declaringLevel;
	if (isVirtualInvoke(method, messageSend) && !(messageSend.actualReceiverType instanceof ArrayBinding)) {
		declaringLevel = resolveLevelAsSubtype(qualifiedPattern, method.declaringClass);
		if (declaringLevel == IMPOSSIBLE_MATCH) {
			if (method.declaringClass == null || this.allSuperDeclaringTypeNames == null) {
				declaringLevel = INACCURATE_MATCH;
			} else {
				char[][] compoundName = method.declaringClass.compoundName;
				for (int i = 0, max = this.allSuperDeclaringTypeNames.length; i < max; i++)
					if (CharOperation.equals(this.allSuperDeclaringTypeNames[i], compoundName))
						return methodLevel; // since this is an ACCURATE_MATCH so return the possibly weaker match
			}
		}
	} else {
		declaringLevel = resolveLevelForType(qualifiedPattern, method.declaringClass);
	}
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
/**
 * Returns whether the given reference type binding matches or is a subtype of a type
 * that matches the given qualified pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve fails
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int resolveLevelAsSubtype(char[] qualifiedPattern, ReferenceBinding type) {
	if (type == null) return INACCURATE_MATCH;

	int level = resolveLevelForType(qualifiedPattern, type);
	if (level != IMPOSSIBLE_MATCH) return level;

	// matches superclass
	if (!type.isInterface() && !CharOperation.equals(type.compoundName, TypeConstants.JAVA_LANG_OBJECT)) {
		level = resolveLevelAsSubtype(qualifiedPattern, type.superclass());
		if (level != IMPOSSIBLE_MATCH) return level;
	}

	// matches interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces == null) return INACCURATE_MATCH;
	for (int i = 0; i < interfaces.length; i++) {
		level = resolveLevelAsSubtype(qualifiedPattern, interfaces[i]);
		if (level != IMPOSSIBLE_MATCH) return level;
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
