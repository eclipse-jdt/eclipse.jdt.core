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
import org.eclipse.jdt.internal.compiler.lookup.*;

public class JavadocFieldReference extends FieldReference {

	public int tagSourceStart, tagSourceEnd;

	public JavadocFieldReference(char[] source, long pos) {
		super(source, pos);
		this.bits |= InsideJavadoc;
	}

	/*
	 * Resolves type on a Block or Class scope.
	 */
	private TypeBinding internalResolveType(Scope scope) {

		this.constant = NotAConstant;
		if (this.receiver == null) {
			this.receiverType = scope.enclosingSourceType();
		} else if (scope.kind == Scope.CLASS_SCOPE) {
			this.receiverType = this.receiver.resolveType((ClassScope) scope);
		} else {
			this.receiverType = this.receiver.resolveType((BlockScope)scope);
		}
		if (this.receiverType == null) {
			return null;
		}

		this.binding = scope.getField(this.receiverType, this.token, this);
		if (!this.binding.isValidBinding()) {
			if (this.receiverType instanceof ReferenceBinding) {
				ReferenceBinding refBinding = (ReferenceBinding) this.receiverType;
				MethodBinding[] bindings = refBinding.getMethods(this.token);
				if (bindings == null) {
					scope.problemReporter().javadocInvalidField(this, this.receiverType, scope.getDeclarationModifiers());
					return null;
				} 
				switch (bindings.length) {
					case 0:
						scope.problemReporter().javadocInvalidField(this, this.receiverType, scope.getDeclarationModifiers());
						return null;
					case 1:
						this.binding = null;
						return null;
					default:
						scope.problemReporter().javadocAmbiguousMethodReference(this, scope.getDeclarationModifiers());
						return null;
				}
			}
		}

		if (isFieldUseDeprecated(this.binding, scope, (this.bits & IsStrictlyAssignedMASK) != 0)) {
			scope.problemReporter().javadocDeprecatedField(this.binding, this, scope.getDeclarationModifiers());
		}
		return this.resolvedType = this.binding.type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return false;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (this.receiver != null) {
			this.receiver.printExpression(0, output);
		}
		output.append('#').append(this.token);
		return output;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding resolveType(BlockScope scope) {
		return internalResolveType(scope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding resolveType(ClassScope scope) {
		return internalResolveType(scope);
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.receiver != null) {
				this.receiver.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
