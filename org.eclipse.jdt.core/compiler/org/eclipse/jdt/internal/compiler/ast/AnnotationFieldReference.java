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

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (receiver != null) {
			receiver.printExpression(0, output);
		}
		output.append('#').append(token);
		return output;
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;

		if (this.receiver == null) {
			this.receiverType = scope.enclosingSourceType();
		}
		else {
			this.receiverType = receiver.resolveType(scope);
		}
		if (this.receiverType == null) {
			return null;
		}
		// the case receiverType.isArrayType and token = 'length' is handled by the scope API
		this.codegenBinding = this.binding = scope.getField(this.receiverType, this.token, this);
		if (!this.binding.isValidBinding()) {
			constant = NotAConstant;
			scope.problemReporter().invalidField(this, this.receiverType);
			return null;
		}

		if (isFieldUseDeprecated(binding, scope, (this.bits & IsStrictlyAssignedMASK) !=0)) {
			scope.problemReporter().deprecatedField(this.binding, this);
		}
		return this.resolvedType = binding.type;
	}

	public TypeBinding resolveType(ClassScope scope) {

		constant = NotAConstant;

		if (this.receiver == null) {
			this.receiverType = scope.enclosingSourceType();
		}
		else {
			this.receiverType = receiver.resolveType(scope);
		}
		if (this.receiverType == null) {
			return null;
		}
		// the case receiverType.isArrayType and token = 'length' is handled by the scope API
		this.binding = scope.findField(this.receiverType, token, this);
		if (this.binding == null) {
			Binding fieldBinding = scope.getBinding(token, BindingIds.FIELD, this);
			if (fieldBinding != null && fieldBinding instanceof FieldBinding) {
				this.binding = (FieldBinding) fieldBinding;
			} else {
				this.binding = new ProblemFieldBinding((ReferenceBinding)this.receiverType,token,fieldBinding.problemId());
			}
		}
		if (!this.binding.isValidBinding()) {
			constant = NotAConstant;
			scope.problemReporter().invalidField(this, this.receiverType);
			return null;
		}

		if (isFieldUseDeprecated(binding, scope, (this.bits & IsStrictlyAssignedMASK) !=0)) {
			scope.problemReporter().deprecatedField(this.binding, this);
		}
		return this.resolvedType = binding.type;
	}

	/**
	 * Redefine to capture annotation specific signatures
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
