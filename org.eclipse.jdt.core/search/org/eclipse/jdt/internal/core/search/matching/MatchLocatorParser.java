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

import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * A parser that locates ast nodes that match a given search pattern.
 */
public class MatchLocatorParser extends Parser {

private MatchingNodeSet matchSet;
private PatternLocator patternLocator;
private AbstractSyntaxTreeVisitorAdapter localDeclarationVisitor;
private int matchContainer;

/**
 * An ast visitor that visits local type declarations.
 */
public class NoClassNoMethodDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		return (constructorDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return (fieldDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type;
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		return (initializer.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		return (methodDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
}
public class MethodButNoClassDeclarationVisitor extends NoClassNoMethodDeclarationVisitor {
	public boolean visit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration, BlockScope scope) {
		patternLocator.match(anonymousTypeDeclaration, matchSet);
		return true; 
	}
	public boolean visit(LocalTypeDeclaration localTypeDeclaration, BlockScope scope) {
		patternLocator.match(localTypeDeclaration, matchSet);
		return true;
	}
}
public class ClassButNoMethodDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		patternLocator.match(constructorDeclaration, matchSet);
		return (constructorDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		patternLocator.match(fieldDeclaration, matchSet);
		return (fieldDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type;
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		patternLocator.match(initializer, matchSet);
		return (initializer.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
	public boolean visit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope) {
		patternLocator.match(memberTypeDeclaration, matchSet);
		return true;
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		patternLocator.match(methodDeclaration, matchSet);
		return (methodDeclaration.bits & AstNode.HasLocalTypeMASK) != 0; // continue only if it has local type
	}
}
public class ClassAndMethodDeclarationVisitor extends ClassButNoMethodDeclarationVisitor {
	public boolean visit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration, BlockScope scope) {
		patternLocator.match(anonymousTypeDeclaration, matchSet);
		return true; 
	}
	public boolean visit(LocalTypeDeclaration localTypeDeclaration, BlockScope scope) {
		patternLocator.match(localTypeDeclaration, matchSet);
		return true;
	}
}

public MatchLocatorParser(ProblemReporter problemReporter) {
	super(problemReporter, true);
}
protected void classInstanceCreation(boolean alwaysQualified) {
	super.classInstanceCreation(alwaysQualified);
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.matchSet);
}
protected void consumeAssignment() {
	super.consumeAssignment();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.matchSet);
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
	super.consumeExplicitConstructorInvocation(flag, recFlag);
	this.patternLocator.match(this.astStack[this.astPtr], this.matchSet);
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	super.consumeFieldAccess(isSuperAccess);
	Expression node = this.expressionStack[this.expressionPtr];
	if (node instanceof Reference) // should always be a FieldReference, but play it safe
		this.patternLocator.match((Reference) node, this.matchSet);
	else
		this.patternLocator.match(node, this.matchSet);
}
protected void consumeMethodInvocationName() {
	super.consumeMethodInvocationName();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.matchSet);
}
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.matchSet);
}
protected void consumeMethodInvocationSuper() {
	super.consumeMethodInvocationSuper();
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.matchSet);
}
protected void consumePrimaryNoNewArray() {
	// pop parenthesis positions (and don't update expression positions
	// (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=23329)
	intPtr--;
	intPtr--;
}
protected void consumeSingleTypeImportDeclarationName() {
	super.consumeSingleTypeImportDeclarationName();
	this.patternLocator.match(this.astStack[this.astPtr], this.matchSet);
}
protected void consumeTypeImportOnDemandDeclarationName() {
	super.consumeTypeImportOnDemandDeclarationName();
	this.patternLocator.match(this.astStack[this.astPtr], this.matchSet);
}
protected void consumeUnaryExpression(int op, boolean post) {
	super.consumeUnaryExpression(op, post);
	this.patternLocator.match(this.expressionStack[this.expressionPtr], this.matchSet);
}
protected TypeReference copyDims(TypeReference typeRef, int dim) {
	TypeReference result = super.copyDims(typeRef, dim);
	 if (this.matchSet.removePossibleMatch(typeRef) != null)
		this.matchSet.addPossibleMatch(result);
	 else if (this.matchSet.removeTrustedMatch(typeRef) != null)
		this.matchSet.addTrustedMatch(result);
	return result;
}
protected TypeReference getTypeReference(int dim) {
	TypeReference typeRef = super.getTypeReference(dim);
	this.patternLocator.match(typeRef, this.matchSet); // NB: Don't check container since type reference can happen anywhere
	return typeRef;
}
protected NameReference getUnspecifiedReference() {
	NameReference nameRef = super.getUnspecifiedReference();
	this.patternLocator.match(nameRef, this.matchSet); // NB: Don't check container since unspecified reference can happen anywhere
	return nameRef;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	NameReference nameRef = super.getUnspecifiedReferenceOptimized();
	this.patternLocator.match(nameRef, this.matchSet); // NB: Don't check container since unspecified reference can happen anywhere
	return nameRef;
}
/**
 * Parses the method bodies in the given compilation unit
 */
public void parseBodies(CompilationUnitDeclaration unit) {
	TypeDeclaration[] types = unit.types;
	if (types == null) return;

	if (this.matchContainer != this.matchSet.matchContainer || this.localDeclarationVisitor == null) {
		this.matchContainer = this.matchSet.matchContainer;
		if ((this.matchContainer & PatternLocator.CLASS_CONTAINER) != 0) {
			this.localDeclarationVisitor = (this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0
				? new ClassAndMethodDeclarationVisitor()
				: new ClassButNoMethodDeclarationVisitor();
		} else {
			this.localDeclarationVisitor = (this.matchContainer & PatternLocator.METHOD_CONTAINER) != 0
				? new MethodButNoClassDeclarationVisitor()
				: new NoClassNoMethodDeclarationVisitor();
		}
	}

	for (int i = 0; i < types.length; i++) {
		TypeDeclaration type = types[i];
		this.patternLocator.match(type, this.matchSet);
		this.parseBodies(type, unit);
	}
}
/**
 * Parses the member bodies in the given type.
 */
protected void parseBodies(TypeDeclaration type, CompilationUnitDeclaration unit) {
	FieldDeclaration[] fields = type.fields;
	if (fields != null) {
		for (int i = 0; i < fields.length; i++) {
			FieldDeclaration field = fields[i];
			if (field instanceof Initializer)
				this.parse((Initializer) field, type, unit);
			field.traverse(localDeclarationVisitor, null);
		}
	}

	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		for (int i = 0; i < methods.length; i++) {
			AbstractMethodDeclaration method = methods[i];
			if (method.sourceStart >= type.bodyStart) { // if not synthetic
				if (method instanceof MethodDeclaration) {
					MethodDeclaration methodDeclaration = (MethodDeclaration) method;
					this.parse(methodDeclaration, unit);
					methodDeclaration.traverse(localDeclarationVisitor, (ClassScope) null);
				} else if (method instanceof ConstructorDeclaration) {
					ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) method;
					this.parse(constructorDeclaration, unit);
					constructorDeclaration.traverse(localDeclarationVisitor, (ClassScope) null);
				}
			} else if (method.isDefaultConstructor()) {
				method.parseStatements(this, unit);
			}
		}
	}

	MemberTypeDeclaration[] memberTypes = type.memberTypes;
	if (memberTypes != null) {
		for (int i = 0; i < memberTypes.length; i++) {
			MemberTypeDeclaration memberType = memberTypes[i];
			this.parseBodies(memberType, unit);
			memberType.traverse(localDeclarationVisitor, (ClassScope) null);
		}
	}
}
public void setMatchSet(MatchingNodeSet nodeSet) {
	if (nodeSet == null) {
		this.matchSet = null;
		this.patternLocator = null;
	} else {
		this.matchSet = nodeSet;
		this.patternLocator = this.matchSet.locator.patternLocator;
	}
}
}
