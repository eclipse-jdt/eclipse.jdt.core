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
import org.eclipse.jdt.internal.compiler.lookup.*;


public class JavadocQualifiedTypeReference extends QualifiedTypeReference {

	public int tagSourceStart, tagSourceEnd;
	public PackageBinding packageBinding;

	public JavadocQualifiedTypeReference(char[][] sources, long[] pos, int tagStart, int tagEnd) {
		super(sources, pos);
		this.tagSourceStart = tagStart;
		this.tagSourceEnd = tagEnd;
		this.bits |= InsideJavadoc;
	}

	protected void reportInvalidType(Scope scope) {
		scope.problemReporter().javadocInvalidType(this, resolvedType, scope.getDeclarationModifiers());
	}
	protected void reportDeprecatedType(Scope scope) {
		scope.problemReporter().javadocDeprecatedType(resolvedType, this, scope.getDeclarationModifiers());
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	/*
	 * We need to modify resolving behavior to handle package references
	 */
	private TypeBinding internalResolveType(Scope scope, boolean checkBounds) {
		// handle the error here
		constant = NotAConstant;
		if (resolvedType != null) // is a shared type reference which was already resolved
			return resolvedType.isValidBinding() ? resolvedType : null; // already reported error

		resolvedType = getTypeBinding(scope);
		if (!resolvedType.isValidBinding()) {
			Binding binding = scope.getTypeOrPackage(tokens);
			if (binding instanceof PackageBinding) {
				packageBinding = (PackageBinding) binding;
			} else {
				reportInvalidType(scope);
			}
			return null;
		}
		if (isTypeUseDeprecated(resolvedType, scope))
			reportDeprecatedType(scope);
		if (resolvedType instanceof ParameterizedTypeBinding) {
			resolvedType = ((ParameterizedTypeBinding)resolvedType).type;
		}
		return resolvedType;
	}

	public TypeBinding resolveType(BlockScope blockScope, boolean checkBounds) {
		return internalResolveType(blockScope, checkBounds);
	}

	public TypeBinding resolveType(ClassScope classScope) {
		return internalResolveType(classScope, false);
	}
}
