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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class JavadocImplicitTypeReference extends TypeReference {
	
	public char[] token;

	public JavadocImplicitTypeReference(char[] name, int pos) {
		super();
		this.token = name;
		this.sourceStart = pos;
		this.sourceEnd = pos;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#copyDims(int)
	 */
	public TypeReference copyDims(int dim) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
	 */
	protected TypeBinding getTypeBinding(Scope scope) {
		this.constant = Constant.NotAConstant;
		return this.resolvedType = scope.enclosingSourceType();
	}

	public char[] getLastToken() {
		return this.token;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getTypeName()
	 */
	public char[][] getTypeName() {
		if (this.token != null) {
			char[][] tokens = { this.token };
			return tokens;
		}
		return null;
	}
	public boolean isThis() {
		return true;
	}

	/*
	 * Resolves type on a Block, Class or CompilationUnit scope.
	 * We need to modify resoling behavior to avoid raw type creation.
	 */
	private TypeBinding internalResolveType(Scope scope) {
		// handle the error here
		this.constant = Constant.NotAConstant;
		if (this.resolvedType != null) // is a shared type reference which was already resolved
			return this.resolvedType.isValidBinding() ? this.resolvedType : null; // already reported error

		this.resolvedType = scope.enclosingSourceType();
		if (this.resolvedType == null)
			return null; // detected cycle while resolving hierarchy	
		if (!this.resolvedType.isValidBinding()) {
			reportInvalidType(scope);
			return null;
		}
		if (isTypeUseDeprecated(this.resolvedType, scope))
			reportDeprecatedType(scope);
		return this.resolvedType;
	}

	protected void reportInvalidType(Scope scope) {
		scope.problemReporter().javadocInvalidType(this, this.resolvedType, scope.getDeclarationModifiers());
	}
	protected void reportDeprecatedType(Scope scope) {
		scope.problemReporter().javadocDeprecatedType(this.resolvedType, this, scope.getDeclarationModifiers());
	}

	public TypeBinding resolveType(BlockScope blockScope, boolean checkBounds) {
		return internalResolveType(blockScope);
	}

	public TypeBinding resolveType(ClassScope classScope) {
		return internalResolveType(classScope);
	}

	public void traverse(ASTVisitor visitor, BlockScope classScope) {
		// Do nothing
	}

	public void traverse(ASTVisitor visitor, ClassScope classScope) {
		// Do nothing
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		return new StringBuffer();
	}
}
