/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Assignment extends Expression {

	public Reference lhs;
	public Expression expression;
	public boolean hasNoEffect; // TODO: should collapse into a bit
		
	public Assignment(Expression lhs, Expression expression, int sourceEnd) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		this.lhs = (Reference) lhs;
		this.expression = expression;

		this.sourceStart = lhs.sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
		// record setting a variable: various scenarii are possible, setting an array reference, 
		// a field reference, a blank final field reference, a field of an enclosing instance or 
		// just a local variable.

		return lhs
			.analyseAssignment(currentScope, flowContext, flowInfo, this, false)
			.unconditionalInits();
	}

	void checkAssignmentEffect(BlockScope scope) {
		
		Binding left = getDirectBinding(this.lhs);
		if (left != null && left == getDirectBinding(this.expression)) {
			scope.problemReporter().assignmentHasNoEffect(this, left.shortReadableName());
			this.hasNoEffect = true;
		}
	}

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		// various scenarii are possible, setting an array reference, 
		// a field reference, a blank final field reference, a field of an enclosing instance or 
		// just a local variable.

		int pc = codeStream.position;
		if (this.hasNoEffect) {
			if (valueRequired) {
				this.expression.generateCode(currentScope, codeStream, true);
			}
		} else {
			lhs.generateAssignment(currentScope, codeStream, this, valueRequired);
			// variable may have been optimized out
			// the lhs is responsible to perform the implicitConversion generation for the assignment since optimized for unused local assignment.
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	Binding getDirectBinding(Expression someExpression) {
		if (someExpression instanceof SingleNameReference) {
			return ((SingleNameReference)someExpression).binding;
		} else if (someExpression instanceof FieldReference) {
			FieldReference fieldRef = (FieldReference)someExpression;
			if (fieldRef.receiver.isThis()) {
				return fieldRef.binding;
			}			
		}
		return null;
	}

	public TypeBinding resolveType(BlockScope scope) {

		// due to syntax lhs may be only a NameReference, a FieldReference or an ArrayReference
		constant = NotAConstant;
		this.resolvedType = lhs.resolveType(scope); // expressionType contains the assignment type (lhs Type)
		TypeBinding rhsType = expression.resolveType(scope);
		if (this.resolvedType == null || rhsType == null)
			return null;

		checkAssignmentEffect(scope);
				
		// Compile-time conversion of base-types : implicit narrowing integer into byte/short/character
		// may require to widen the rhs expression at runtime
		if ((expression.isConstantValueOfTypeAssignableToType(rhsType, this.resolvedType)
			|| (this.resolvedType.isBaseType() && BaseTypeBinding.isWidening(this.resolvedType.id, rhsType.id)))
			|| (Scope.areTypesCompatible(rhsType, this.resolvedType))) {
			expression.implicitWidening(this.resolvedType, rhsType);
			return this.resolvedType;
		}
		scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
			expression,
			rhsType,
			this.resolvedType);
		return null;
	}

	public String toString(int tab) {

		//no () when used as a statement 
		return tabString(tab) + toStringExpressionNoParenthesis();
	}

	public String toStringExpression() {

		//subclass redefine toStringExpressionNoParenthesis()
		return "(" + toStringExpressionNoParenthesis() + ")"; //$NON-NLS-2$ //$NON-NLS-1$
	} 
	
	public String toStringExpressionNoParenthesis() {

		return lhs.toStringExpression() + " " //$NON-NLS-1$
			+ "=" //$NON-NLS-1$
			+ ((expression.constant != null) && (expression.constant != NotAConstant)
				? " /*cst:" + expression.constant.toString() + "*/ " //$NON-NLS-1$ //$NON-NLS-2$
				: " ")  //$NON-NLS-1$
			+ expression.toStringExpression();
	}
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
			expression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}