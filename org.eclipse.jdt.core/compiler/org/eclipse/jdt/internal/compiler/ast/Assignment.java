package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Assignment extends Expression {
	public Reference lhs;
	public Expression expression;
	public Assignment(Expression lhs, Expression expression) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		this.lhs = (Reference) lhs;
		this.expression = expression;

		sourceStart = lhs.sourceStart;
		sourceEnd = expression.sourceEnd;

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

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		// various scenarii are possible, setting an array reference, 
		// a field reference, a blank final field reference, a field of an enclosing instance or 
		// just a local variable.

		int pc = codeStream.position;
		lhs.generateAssignment(currentScope, codeStream, this, valueRequired);
		// variable may have been optimized out
		// the lhs is responsible to perform the implicitConversion generation for the assignment since optimized for unused local assignment.
		codeStream.recordPositionsFrom(pc, this);
	}

	public TypeBinding resolveType(BlockScope scope) {
		// due to syntax lhs may be only a NameReference, a FieldReference or an ArrayReference

		constant = NotAConstant;
		TypeBinding lhsTb = lhs.resolveType(scope);
		TypeBinding expressionTb = expression.resolveType(scope);
		if (lhsTb == null || expressionTb == null)
			return null;

		// Compile-time conversion of base-types : implicit narrowing integer into byte/short/character
		// may require to widen the rhs expression at runtime
		if ((expression.isConstantValueOfTypeAssignableToType(expressionTb, lhsTb)
			|| (lhsTb.isBaseType() && BaseTypeBinding.isWidening(lhsTb.id, expressionTb.id)))
			|| (scope.areTypesCompatible(expressionTb, lhsTb))) {
			expression.implicitWidening(lhsTb, expressionTb);
			return lhsTb;
		}
		scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
			expression,
			expressionTb,
			lhsTb);
		return null;

		/*------------code deported to the flow analysis-------------------
		if (lhs.isFieldReference()) {
			// cover also the case of a nameReference that refers to a field...(of course !...)
			if (lhsTb.isFinal()) {
				// if the field is final, then the assignment may be done only in constructors/initializers
				// (this does not insure that the assignment is valid....the flow analysis will tell so)
				if (scope.enclosingType() == lhs.fieldBinding().declaringClass) {
					scope.problemReporter().cannotAssignToFinalField(this, lhs.fieldBinding());
					return null;
				}
				if (!scope.enclosingMethod().isConstructorOrInintilizer()) {
					scope.problemReporter().cannotAssignToFinalField(this, lhs.fieldBinding());
					return null;
				}
			}
		}
		-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- * /
		
		/*----------------------------------------------------------------
		the code that test if the local is an outer local (it is by definition final)
		and thus cannot be assigned , has been moved to flow analysis		
		------------------------------------------------------------------*/

		/*-----------------------------------------------------------------
		The code that detect the a.b = a.b + 1 ; is done by the flow analysis too
		-------------------------------------------------------------------*/
	}

	public String toString(int tab) {
		/* slow code*/

		//no () when used as a statement 

		return tabString(tab) + toStringExpressionNoParenthesis();
	}

	public String toStringExpression() {
		/* slow code*/

		//subclass redefine toStringExpressionNoParenthesis()

		return "(" + toStringExpressionNoParenthesis() + ")";
	}

	public String toStringExpressionNoParenthesis() {

		return lhs.toStringExpression()
			+ " "
			+ "="
			+ ((expression.constant != null)
				&& (expression.constant != NotAConstant)
					? " /*cst:" + expression.constant.toString() + "*/ "
					: " ")
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
