/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beriozkin - added support for reporting assignment with no effect
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Assignment extends Expression {

	public Expression lhs;
	public Expression expression;
		
	public Assignment(Expression lhs, Expression expression, int sourceEnd) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		this.lhs = lhs;
		lhs.bits |= IsStrictlyAssignedMASK; // tag lhs as assigned
		
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

		return ((Reference) lhs)
			.analyseAssignment(currentScope, flowContext, flowInfo, this, false)
			.unconditionalInits();
	}

	void checkAssignmentEffect(BlockScope scope) {
		
		Binding left = getDirectBinding(this.lhs);
		if (left != null && left == getDirectBinding(this.expression)) {
			scope.problemReporter().assignmentHasNoEffect(this, left.shortReadableName());
			this.bits |= IsAssignmentWithNoEffectMASK; // record assignment has no effect
		}
	}

	void checkAssignment(BlockScope scope, TypeBinding lhsType, TypeBinding rhsType) {
		
		FieldBinding leftField = getLastField(this.lhs);
		if (leftField != null &&  rhsType != NullBinding && lhsType.isWildcard() && ((WildcardBinding)lhsType).kind != Wildcard.SUPER) {
		    scope.problemReporter().wildcardAssignment(lhsType, rhsType, this.expression);
		} else if (leftField != null && leftField.declaringClass != null /*length pseudo field*/&& leftField.declaringClass.isRawType() 
		        && (rhsType.isParameterizedType() || rhsType.isGenericType())) {
		    scope.problemReporter().unsafeRawFieldAssignment(leftField, rhsType, this.lhs);
		} else if (rhsType.isRawType() && (lhsType.isBoundParameterizedType() || lhsType.isGenericType())) {
		    scope.problemReporter().unsafeRawConversion(this.expression, rhsType, lhsType);
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
		if ((this.bits & IsAssignmentWithNoEffectMASK) != 0) {
			if (valueRequired) {
				this.expression.generateCode(currentScope, codeStream, true);
			}
		} else {
			 ((Reference) lhs).generateAssignment(currentScope, codeStream, this, valueRequired);
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
			if (fieldRef.receiver.isThis() && !(fieldRef.receiver instanceof QualifiedThisReference)) {
				return fieldRef.binding;
			}			
		}
		return null;
	}
	FieldBinding getLastField(Expression someExpression) {
	    if (someExpression instanceof SingleNameReference) {
	        if ((someExpression.bits & RestrictiveFlagMASK) == BindingIds.FIELD) {
	            return (FieldBinding) ((SingleNameReference)someExpression).binding;
	        }
	    } else if (someExpression instanceof FieldReference) {
	        return ((FieldReference)someExpression).binding;
	    } else if (someExpression instanceof QualifiedNameReference) {
	        QualifiedNameReference qName = (QualifiedNameReference) someExpression;
	        if (qName.otherBindings == null && ((someExpression.bits & RestrictiveFlagMASK) == BindingIds.FIELD)) {
	            return (FieldBinding)qName.binding;
	        } else {
	            return qName.otherBindings[qName.otherBindings.length - 1];
	        }
	    }
	    return null;
	}	
	public StringBuffer print(int indent, StringBuffer output) {

		//no () when used as a statement 
		printIndent(indent, output);
		return printExpressionNoParenthesis(indent, output);
	}
	public StringBuffer printExpression(int indent, StringBuffer output) {

		//subclass redefine printExpressionNoParenthesis()
		output.append('(');
		return printExpressionNoParenthesis(0, output).append(')');
	} 

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		lhs.printExpression(indent, output).append(" = "); //$NON-NLS-1$
		return expression.printExpression(0, output);
	}
	
	public StringBuffer printStatement(int indent, StringBuffer output) {

		//no () when used as a statement 
		return print(indent, output).append(';');
	}

	public TypeBinding resolveType(BlockScope scope) {

		// due to syntax lhs may be only a NameReference, a FieldReference or an ArrayReference
		constant = NotAConstant;
		if (!(this.lhs instanceof Reference) || this.lhs.isThis()) {
			scope.problemReporter().expressionShouldBeAVariable(this.lhs);
			return null;
		}
		TypeBinding lhsType = this.resolvedType = lhs.resolveType(scope);
		expression.setExpectedType(lhsType); // needed in case of generic method invocation
		TypeBinding rhsType = expression.resolveType(scope);
		if (lhsType == null || rhsType == null) {
			return null;
		}
		checkAssignmentEffect(scope);

		// Compile-time conversion of base-types : implicit narrowing integer into byte/short/character
		// may require to widen the rhs expression at runtime
		if ((expression.isConstantValueOfTypeAssignableToType(rhsType, lhsType)
				|| (lhsType.isBaseType() && BaseTypeBinding.isWidening(lhsType.id, rhsType.id)))
				|| rhsType.isCompatibleWith(lhsType)) {
			expression.computeConversion(scope, lhsType, rhsType);
			checkAssignment(scope, lhsType, rhsType);
			return this.resolvedType;
		}
		scope.problemReporter().typeMismatchError(rhsType, lhsType, expression);
		return lhsType;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveTypeExpecting(org.eclipse.jdt.internal.compiler.lookup.BlockScope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
	 */
	public TypeBinding resolveTypeExpecting(
			BlockScope scope,
			TypeBinding expectedType) {

		TypeBinding type = super.resolveTypeExpecting(scope, expectedType);
		if (type == null) return null;
		TypeBinding lhsType = this.resolvedType; 
		TypeBinding rhsType = this.expression.resolvedType;
		// signal possible accidental boolean assignment (instead of using '==' operator)
		if (expectedType == BooleanBinding 
				&& lhsType == BooleanBinding 
				&& (this.lhs.bits & IsStrictlyAssignedMASK) != 0) {
			scope.problemReporter().possibleAccidentalBooleanAssignment(this);
		}
		checkAssignment(scope, lhsType, rhsType);
		return type;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
			expression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}