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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;



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
		scope.problemReporter().javadocInvalidType(this, this.resolvedType, scope.getDeclarationModifiers());
	}
	protected void reportDeprecatedType(Scope scope) {
		scope.problemReporter().javadocDeprecatedType(this.resolvedType, this, scope.getDeclarationModifiers());
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
	 *
	 */
	private TypeBinding internalResolveType(Scope scope) {
		// handle the error here
		this.constant = NotAConstant;
		if (this.resolvedType != null) // is a shared type reference which was already resolved
			return this.resolvedType.isValidBinding() ? this.resolvedType : null; // already reported error

		this.resolvedType = getTypeBinding(scope);
		if (!this.resolvedType.isValidBinding()) {
			Binding binding = scope.getTypeOrPackage(this.tokens);
			if (binding instanceof PackageBinding) {
				this.packageBinding = (PackageBinding) binding;
			} else {
				reportInvalidType(scope);
			}
			return null;
		}
		if (isTypeUseDeprecated(this.resolvedType, scope))
			reportDeprecatedType(scope);
		return this.resolvedType = scope.convertToRawType(this.resolvedType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 * We need to override to handle package references
	 */
	public TypeBinding resolveType(BlockScope blockScope) {
		return internalResolveType(blockScope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 * We need to override to handle package references
	 */
	public TypeBinding resolveType(ClassScope classScope) {
		return internalResolveType(classScope);
	}
}
