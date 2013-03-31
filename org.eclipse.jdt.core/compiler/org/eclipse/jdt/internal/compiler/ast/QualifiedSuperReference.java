/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 382350 - [1.8][compiler] Unable to invoke inherited default method via I.super.m() syntax
 *								bug 404649 - [1.8][compiler] detect illegal reference to indirect or redundant super
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedSuperReference extends QualifiedThisReference {

public QualifiedSuperReference(TypeReference name, int pos, int sourceEnd) {
	super(name, pos, sourceEnd);
}

public boolean isSuper() {
	return true;
}

public boolean isThis() {
	return false;
}

public StringBuffer printExpression(int indent, StringBuffer output) {
	return this.qualification.print(0, output).append(".super"); //$NON-NLS-1$
}

public TypeBinding resolveType(BlockScope scope) {
	if ((this.bits & ParenthesizedMASK) != 0) {
		scope.problemReporter().invalidParenthesizedExpression(this);
		return null;
	}
	super.resolveType(scope);
	if (!this.resolvedType.isValidBinding()) {
		scope.problemReporter().illegalSuperAccess(this.qualification.resolvedType, this.resolvedType, this);
		return null;
	}
	if (this.currentCompatibleType == null)
		return null; // error case

	if (this.currentCompatibleType.id == T_JavaLangObject) {
		scope.problemReporter().cannotUseSuperInJavaLangObject(this);
		return null;
	}
	return this.resolvedType = (this.currentCompatibleType.isInterface()
			? this.currentCompatibleType
			: this.currentCompatibleType.superclass());
}

int findCompatibleEnclosing(ReferenceBinding enclosingType, TypeBinding type) {
	if (type.isInterface()) {
		// super call to an overridden default method? (not considering outer enclosings)
		ReferenceBinding[] supers = enclosingType.superInterfaces();
		int length = supers.length;
		for (int i = 0; i < length; i++) {
			if (supers[i].erasure() == type) {
				this.currentCompatibleType = supers[i];
			} else if (supers[i].erasure().isCompatibleWith(type)) {
				this.currentCompatibleType = null;
				this.resolvedType = new ProblemReferenceBinding(supers[i].compoundName, supers[i], ProblemReasons.AttemptToBypassDirectSuper);
				return 0;
			}
		}
		return 0;
	}
	return super.findCompatibleEnclosing(enclosingType, type);
}

public void traverse(
	ASTVisitor visitor,
	BlockScope blockScope) {

	if (visitor.visit(this, blockScope)) {
		this.qualification.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
public void traverse(
		ASTVisitor visitor,
		ClassScope blockScope) {

	if (visitor.visit(this, blockScope)) {
		this.qualification.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}
}
