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
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class MethodLocator extends PatternLocator {

protected MethodPattern pattern;
protected boolean isDeclarationOfReferencedMethodsPattern;

public MethodLocator(MethodPattern pattern) {
	super(pattern);

	this.pattern = pattern;
	this.isDeclarationOfReferencedMethodsPattern = this.pattern instanceof DeclarationOfReferencedMethodsPattern;
}
/**
 * Returns whether the code gen will use an invoke virtual for 
 * this message send or not.
 */
protected boolean isVirtualInvoke(MethodBinding method, MessageSend messageSend) {
	return !method.isStatic() && !method.isPrivate() && !messageSend.isSuperAccess();
}
//public void match(AstNode node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public void match(Expression node, MatchingNodeSet nodeSet) { // interested in MessageSend
	if (!this.pattern.findReferences) return;
	if (!(node instanceof MessageSend)) return;

	MessageSend messageSend = (MessageSend) node;
	if (!matchesName(this.pattern.selector, messageSend.selector)) return;
	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		AstNode[] args = messageSend.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return;
	}

	nodeSet.addMatch(node, this.pattern.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH);
}
//public void match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public void match(MethodDeclaration node, MatchingNodeSet nodeSet) {
	if (!this.pattern.findDeclarations) return;

	if (!matchesName(this.pattern.selector, node.selector)) return;
	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		AstNode[] args = node.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return;

		for (int i = 0; i < argsLength; i++)
			if (!matchesTypeReference(this.pattern.parameterSimpleNames[i], ((Argument) args[i]).type)) return;
	}
	if (!matchesTypeReference(this.pattern.returnSimpleName, node.returnType)) return;

	nodeSet.addMatch(node, this.pattern.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH);
}
//public void match(Reference node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public void match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

protected int matchContainer() {
	if (this.pattern.findReferences) {
		if (this.pattern.findDeclarations)
			return CLASS_CONTAINER | METHOD_CONTAINER | FIELD_CONTAINER;
		return METHOD_CONTAINER | FIELD_CONTAINER;
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
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedMethodsPattern) {
		// need accurate match to be able to open on type ref
		if (accuracy == IJavaSearchResultCollector.POTENTIAL_MATCH) return;

		// element that references the method must be included in the enclosing element
		DeclarationOfReferencedMethodsPattern declPattern = (DeclarationOfReferencedMethodsPattern) this.pattern; 
		while (element != null && !declPattern.enclosingElement.equals(element))
			element = element.getParent();
		if (element != null)
			reportDeclaration(((MessageSend) reference).binding, locator, declPattern.knownMethods);
	} else if (this.pattern.findReferences && reference instanceof MessageSend) {
		// message ref are starting at the selector start
		locator.report(
			(int) (((MessageSend) reference).nameSourcePosition >> 32),
			reference.sourceEnd,
			element,
			accuracy);
	} else {
		super.matchReportReference(reference, element, accuracy, locator);
	}
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
		locator.reportBinaryMatch(resource, method, info, IJavaSearchResultCollector.EXACT_MATCH);
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
			if (methodDecl != null)
				locator.report(resource, methodDecl.sourceStart, methodDecl.sourceEnd, method, IJavaSearchResultCollector.EXACT_MATCH);
		}
	}
}
public int resolveLevel(AstNode potentialMatchingNode) {
	if (this.pattern.findReferences && potentialMatchingNode instanceof MessageSend)
		return resolveLevel((MessageSend) potentialMatchingNode);
	if (this.pattern.findDeclarations && potentialMatchingNode instanceof MethodDeclaration)
		return resolveLevel(((MethodDeclaration) potentialMatchingNode).binding);
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;

	MethodBinding method = (MethodBinding) binding;
	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// declaring type
	int declaringLevel = !method.isStatic() && !method.isPrivate()
		? resolveLevelAsSubtype(this.pattern.declaringSimpleName, this.pattern.declaringQualification, method.declaringClass)
		: resolveLevelForType(this.pattern.declaringSimpleName, this.pattern.declaringQualification, method.declaringClass);
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
protected int resolveLevel(MessageSend messageSend) {
	MethodBinding method = messageSend.binding;
	if (method == null) return INACCURATE_MATCH;

	int methodLevel = matchMethod(method);
	if (methodLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// receiver type
	int declaringLevel;
	if (isVirtualInvoke(method, messageSend) && !(messageSend.receiverType instanceof ArrayBinding)) {
		declaringLevel = resolveLevelAsSubtype(this.pattern.declaringSimpleName, this.pattern.declaringQualification, method.declaringClass);
		if (declaringLevel == IMPOSSIBLE_MATCH) {
			if (method.declaringClass == null || this.pattern.allSuperDeclaringTypeNames == null) {
				declaringLevel = INACCURATE_MATCH;
			} else {
				char[][] compoundName = method.declaringClass.compoundName;
				for (int i = 0, max = this.pattern.allSuperDeclaringTypeNames.length; i < max; i++)
					if (CharOperation.equals(this.pattern.allSuperDeclaringTypeNames[i], compoundName))
						return methodLevel; // since this is an ACCURATE_MATCH so return the possibly weaker match
			}
		}
	} else {
		declaringLevel = resolveLevelForType(this.pattern.declaringSimpleName, this.pattern.declaringQualification, method.declaringClass);
	}
	return methodLevel > declaringLevel ? declaringLevel : methodLevel; // return the weaker match
}
/**
 * Returns whether the given reference type binding matches or is a subtype of a type
 * that matches the given simple name pattern and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve fails
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int resolveLevelAsSubtype(char[] simpleNamePattern, char[] qualificationPattern, ReferenceBinding type) {
	if (type == null) return INACCURATE_MATCH;

	int level = resolveLevelForType(simpleNamePattern, qualificationPattern, type);
	if (level != IMPOSSIBLE_MATCH) return level;

	// matches superclass
	if (!type.isInterface() && !CharOperation.equals(type.compoundName, TypeConstants.JAVA_LANG_OBJECT)) {
		level = resolveLevelAsSubtype(simpleNamePattern, qualificationPattern, type.superclass());
		if (level != IMPOSSIBLE_MATCH) return level;
	}

	// matches interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces == null) return INACCURATE_MATCH;
	for (int i = 0; i < interfaces.length; i++) {
		level = resolveLevelAsSubtype(simpleNamePattern, qualificationPattern, interfaces[i]);
		if (level != IMPOSSIBLE_MATCH) return level;
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
