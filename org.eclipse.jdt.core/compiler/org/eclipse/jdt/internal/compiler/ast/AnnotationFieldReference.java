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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class AnnotationFieldReference extends FieldReference {

	public int tagSourceStart, tagSourceEnd;

	public AnnotationFieldReference(char[] source, long pos) {
		super(source, pos);
		this.bits |= InsideAnnotation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return false;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (receiver != null) {
			receiver.printExpression(0, output);
		}
		output.append('#').append(token);
		return output;
	}

	/* (non-Javadoc)
	 * Redefine to remove unnecessary tests while resolving in annotation
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;

		if (this.receiver == null) {
			SourceTypeBinding sourceTypeBinding = scope.enclosingSourceType();
			this.receiverType = sourceTypeBinding;
			this.receiver = new AnnotationQualifiedTypeReference(sourceTypeBinding.compoundName, new long[sourceTypeBinding.compoundName.length], 0, 0);
		} else {
			this.receiverType = receiver.resolveType(scope);
		}
		if (this.receiverType == null) {
			return null;
		}

		this.binding = scope.getField(this.receiverType, this.token, this);
		if (!this.binding.isValidBinding()) {
			constant = NotAConstant;
			scope.problemReporter().invalidField(this, this.receiverType);
			return null;
		}

		if (isFieldUseDeprecated(binding, scope, (this.bits & IsStrictlyAssignedMASK) != 0)) {
			scope.problemReporter().deprecatedField(this.binding, this);
		}
		return this.resolvedType = binding.type;
	}

	/* (non-Javadoc)
	 * Redefine to remove unnecessary tests while resolving in annotation
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public TypeBinding resolveType(ClassScope scope) {

		constant = NotAConstant;

		if (this.receiver == null) {
			SourceTypeBinding sourceTypeBinding = scope.enclosingSourceType();
			this.receiverType = sourceTypeBinding;
			this.receiver = new AnnotationQualifiedTypeReference(sourceTypeBinding.compoundName, new long[sourceTypeBinding.compoundName.length], 0, 0);
		} else {
			this.receiverType = receiver.resolveType(scope);
		}
		if (this.receiverType == null) {
			return null;
		}

		this.binding = scope.getField(this.receiverType, this.token, this);
		if (!this.binding.isValidBinding()) {
			constant = NotAConstant;
			scope.problemReporter().invalidField(this, this.receiverType);
			return null;
		}

		if (isFieldUseDeprecated(binding, scope, (this.bits & IsStrictlyAssignedMASK) != 0)) {
			scope.problemReporter().deprecatedField(this.binding, this);
		}
		return this.resolvedType = binding.type;
	}

	/* (non-Javadoc)
	 * Redefine to capture annotation specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#traverse(org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (receiver != null) {
				receiver.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}