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
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ArrayReference extends Reference {
	
	public Expression receiver;
	public Expression position;

	public ArrayReference(Expression rec, Expression pos) {
		this.receiver = rec;
		this.position = pos;
		sourceStart = rec.sourceStart;
	}

	public FlowInfo analyseAssignment(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Assignment assignment,
		boolean compoundAssignment) {

		if (assignment.expression == null) {
			return analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
		return assignment
			.expression
			.analyseCode(
				currentScope,
				flowContext,
				analyseCode(currentScope, flowContext, flowInfo).unconditionalInits())
			.unconditionalInits();
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return position.analyseCode(
			currentScope,
			flowContext,
			receiver.analyseCode(currentScope, flowContext, flowInfo));
	}

	public void generateAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Assignment assignment,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == NullBinding){
			codeStream.checkcast(receiver.resolvedType); 
		}	
		position.generateCode(currentScope, codeStream, true);
		assignment.expression.generateCode(currentScope, codeStream, true);
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
		if (valueRequired) {
			codeStream.generateImplicitConversion(assignment.implicitConversion);
		}
	}

	/**
	 * Code generation for a array reference
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == NullBinding){
			codeStream.checkcast(receiver.resolvedType); 
		}			
		position.generateCode(currentScope, codeStream, true);
		codeStream.arrayAt(this.resolvedType.id);
		// Generating code for the potential runtime type checking
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		} else {
			if (this.resolvedType == LongBinding
				|| this.resolvedType == DoubleBinding) {
				codeStream.pop2();
			} else {
				codeStream.pop();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void generateCompoundAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Expression expression,
		int operator,
		int assignmentImplicitConversion,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == NullBinding){
			codeStream.checkcast(receiver.resolvedType); 
		}	
		position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(this.resolvedType.id);
		int operationTypeID;
		if ((operationTypeID = implicitConversion >> 4) == T_String) {
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
		} else {
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One) { // prefix operation
				codeStream.generateConstant(expression.constant, implicitConversion);
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
	}

	public void generatePostIncrement(
		BlockScope currentScope,
		CodeStream codeStream,
		CompoundAssignment postIncrement,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == NullBinding){
			codeStream.checkcast(receiver.resolvedType); 
		}	
		position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(this.resolvedType.id);
		if (valueRequired) {
			if ((this.resolvedType == LongBinding)
				|| (this.resolvedType == DoubleBinding)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		codeStream.generateConstant(
			postIncrement.expression.constant,
			implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.resolvedType.id);
		codeStream.generateImplicitConversion(
			postIncrement.assignmentImplicitConversion);
		codeStream.arrayAtPut(this.resolvedType.id, false);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		receiver.printExpression(0, output).append('[');
		return position.printExpression(0, output).append(']');
	} 

	public TypeBinding resolveType(BlockScope scope) {

		constant = Constant.NotAConstant;
		if (receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression() instanceof NullLiteral) {
			this.receiver.bits |= IgnoreNeedForCastCheckMASK; // will check later on
		}		
		TypeBinding arrayType = receiver.resolveType(scope);
		if (arrayType != null) {
			receiver.computeConversion(scope, arrayType, arrayType);
			if (arrayType.isArrayType()) {
				this.resolvedType = ((ArrayBinding) arrayType).elementsType();
			} else {
				scope.problemReporter().referenceMustBeArrayTypeAt(arrayType, this);
			}
		}
		TypeBinding positionType = position.resolveTypeExpecting(scope, IntBinding);
		if (positionType != null) {
			position.computeConversion(scope, IntBinding, positionType);
		}
		return this.resolvedType;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			receiver.traverse(visitor, scope);
			position.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
