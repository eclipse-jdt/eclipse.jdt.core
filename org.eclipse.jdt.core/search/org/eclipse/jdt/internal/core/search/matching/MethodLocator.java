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
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
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
//public int match(ASTNode node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(MethodDeclaration node, MatchingNodeSet nodeSet) {
	if (!this.pattern.findDeclarations) return IMPOSSIBLE_MATCH;

	if (!matchesName(this.pattern.selector, node.selector)) return IMPOSSIBLE_MATCH;
	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		ASTNode[] args = node.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;

		for (int i = 0; i < argsLength; i++)
			if (!matchesTypeReference(this.pattern.parameterSimpleNames[i], ((Argument) args[i]).type)) return IMPOSSIBLE_MATCH;
	}
	if (!matchesTypeReference(this.pattern.returnSimpleName, node.returnType)) return IMPOSSIBLE_MATCH;

	return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
public int match(MessageSend node, MatchingNodeSet nodeSet) {
	if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;

	if (!matchesName(this.pattern.selector, node.selector)) return IMPOSSIBLE_MATCH;
	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		ASTNode[] args = node.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
	}

	return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
//public int match(Reference node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

protected int matchContainer() {
	if (this.pattern.findReferences) {
		// need to look almost everywhere to find in javadocs
		return CLASS_CONTAINER | METHOD_CONTAINER | FIELD_CONTAINER;
	}
	return CLASS_CONTAINER;
}
protected int matchMethod(MethodBinding method) {
	if (!matchesName(this.pattern.selector, method.selector)) return IMPOSSIBLE_MATCH;

	int level = ACCURATE_MATCH;
	// look at return type only if declaring type is not specified
	if (this.pattern.declaringSimpleName == null) {
		int newLevel = resolveLevelForType(this.pattern.returnSimpleName, this.pattern.returnQualification, method.returnType);
		if (level > newLevel) {
			if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
			level = newLevel; // can only be downgraded
		}
	}

	// parameter types
	int parameterCount = this.pattern.parameterSimpleNames == null ? -1 : this.pattern.parameterSimpleNames.length;
	if (parameterCount > -1) {
		if (method.parameters == null) return INACCURATE_MATCH;
		if (parameterCount != method.parameters.length) return IMPOSSIBLE_MATCH;
		for (int i = 0; i < parameterCount; i++) {
			int newLevel = resolveLevelForType(this.pattern.parameterSimpleNames[i], this.pattern.parameterQualifications[i], method.parameters[i]);
			if (level > newLevel) {
				if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
				level = newLevel; // can only be downgraded
			}
		}
	}
	return level;
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedMethodsPattern) {
		// need exact match to be able to open on type ref
		if (accuracy != SearchMatch.A_ACCURATE) return;

		// element that references the method must be included in the enclosing element
		DeclarationOfReferencedMethodsPattern declPattern = (DeclarationOfReferencedMethodsPattern) this.pattern; 
		while (element != null && !declPattern.enclosingElement.equals(element))
			element = element.getParent();
		if (element != null)
			reportDeclaration(((MessageSend) reference).binding, locator, declPattern.knownMethods);
	} else if (this.pattern.findReferences && reference instanceof MessageSend) {
		int sourceStart = (int) (((MessageSend) reference).nameSourcePosition >>> 32);
		SearchMatch match = locator.newMethodReferenceMatch(element, accuracy, sourceStart, reference.sourceEnd+1, reference);
		locator.report(match);
	} else {
		SearchMatch match = locator.newMethodReferenceMatch(element, accuracy, reference.sourceStart, reference.sourceEnd+1, reference);
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
		locator.reportBinaryMemberDeclaration(resource, method, info, SearchMatch.A_ACCURATE);
	} else {
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
				SearchMatch match = new MethodDeclarationMatch(method, SearchMatch.A_ACCURATE, methodDecl.sourceStart, methodDecl.sourceEnd+1, locator.getParticipant(), resource);
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
	if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// declaring type
	char[] qualifiedPattern = qualifiedPattern(this.pattern.declaringSimpleName, this.pattern.declaringQualification);
	if (qualifiedPattern == null) return methodLevel; // since any declaring class will do

	int declaringLevel = !method.isStatic() && !method.isPrivate()
		? resolveLevelAsSubtype(qualifiedPattern, method.declaringClass)
		: resolveLevelForType(qualifiedPattern, method.declaringClass);
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
protected int resolveLevel(MessageSend messageSend) {
	MethodBinding method = messageSend.binding;
	if (method == null) return INACCURATE_MATCH;

	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// receiver type
	char[] qualifiedPattern = qualifiedPattern(this.pattern.declaringSimpleName, this.pattern.declaringQualification);
	if (qualifiedPattern == null) return methodLevel; // since any declaring class will do

	int declaringLevel;
	if (isVirtualInvoke(method, messageSend) && !(messageSend.receiverType instanceof ArrayBinding)) {
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
