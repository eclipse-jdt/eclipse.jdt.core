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
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class UnaryExpression extends OperatorExpression {
	
	public Expression expression;
	public Constant optimizedBooleanConstant;

	public UnaryExpression(Expression expression, int operator) {
		this.expression = expression;
		this.bits |= operator << OperatorSHIFT; // encode operator
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
			
		if (((bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
			return this.expression
				.analyseCode(currentScope, flowContext, flowInfo)
				.asNegatedCondition();
		} else {
			return this.expression.analyseCode(currentScope, flowContext, flowInfo);
		}
	}

	public Constant optimizedBooleanConstant() {
		
		return this.optimizedBooleanConstant == null 
				? this.constant 
				: this.optimizedBooleanConstant;
	}

	/**
	 * Code generation for an unary operation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		Label falseLabel, endifLabel;
		if (this.constant != Constant.NotAConstant) {
			// inlined value
			if (valueRequired) {
				codeStream.generateConstant(this.constant, this.implicitConversion);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		switch ((bits & OperatorMASK) >> OperatorSHIFT) {
			case NOT :
				switch (this.expression.implicitConversion >> 4) /* runtime type */ {
					case T_boolean :
						// ! <boolean>
						// Generate code for the condition
						this.expression.generateOptimizedBoolean(
							currentScope,
							codeStream,
							null,
							(falseLabel = new Label(codeStream)),
							valueRequired);
						if (valueRequired) {
							codeStream.iconst_0();
							if (falseLabel.hasForwardReferences()) {
								codeStream.goto_(endifLabel = new Label(codeStream));
								codeStream.decrStackSize(1);
								falseLabel.place();
								codeStream.iconst_1();
								endifLabel.place();
							}
						} else { // 6596: if (!(a && b)){} - must still place falseLabel
							falseLabel.place();
						}						
						break;
				}
				break;
			case TWIDDLE :
				switch (this.expression.implicitConversion >> 4 /* runtime */
					) {
					case T_int :
						// ~int
						this.expression.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired) {
							codeStream.iconst_m1();
							codeStream.ixor();
						}
						break;
					case T_long :
						this.expression.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired) {
							codeStream.ldc2_w(-1L);
							codeStream.lxor();
						}
				}
				break;
			case MINUS :
				// - <num>
				if (this.constant != NotAConstant) {
					if (valueRequired) {
						switch (this.expression.implicitConversion >> 4){ /* runtime */
							case T_int :
								codeStream.generateInlinedValue(this.constant.intValue() * -1);
								break;
							case T_float :
								codeStream.generateInlinedValue(this.constant.floatValue() * -1.0f);
								break;
							case T_long :
								codeStream.generateInlinedValue(this.constant.longValue() * -1L);
								break;
							case T_double :
								codeStream.generateInlinedValue(this.constant.doubleValue() * -1.0);
						}
					}
				} else {
					this.expression.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired) {
						switch (expression.implicitConversion >> 4){ /* runtime type */
							case T_int :
								codeStream.ineg();
								break;
							case T_float :
								codeStream.fneg();
								break;
							case T_long :
								codeStream.lneg();
								break;
							case T_double :
								codeStream.dneg();
						}
					}
				}
				break;
			case PLUS :
				this.expression.generateCode(currentScope, codeStream, valueRequired);
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Boolean operator code generation
	 *	Optimized operations are: &&, ||, <, <=, >, >=, &, |, ^
	 */
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		if ((this.constant != Constant.NotAConstant) && (this.constant.typeID() == T_boolean)) {
			super.generateOptimizedBoolean(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		}
		if (((this.bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
			this.expression.generateOptimizedBoolean(
				currentScope,
				codeStream,
				falseLabel,
				trueLabel,
				valueRequired);
		} else {
			super.generateOptimizedBoolean(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
		}
	}

	public TypeBinding resolveType(BlockScope scope) {
		
		TypeBinding expressionType = this.expression.resolveType(scope);
		if (expressionType == null) {
			this.constant = NotAConstant;
			return null;
		}
		int expressionId = expressionType.id;
		if (expressionId > 15) {
			this.constant = NotAConstant;
			scope.problemReporter().invalidOperator(this, expressionType);
			return null;
		}

		int tableId;
		switch ((bits & OperatorMASK) >> OperatorSHIFT) {
			case NOT :
				tableId = AND_AND;
				break;
			case TWIDDLE :
				tableId = LEFT_SHIFT;
				break;
			default :
				tableId = MINUS;
		} //+ and - cases

		// the code is an int
		// (cast)  left   Op (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8    <<4       <<0
		int result = ResolveTypeTables[tableId][(expressionId << 4) + expressionId];
		this.expression.implicitConversion = result >>> 12;
		this.bits |= result & 0xF;
		switch (result & 0xF) { // only switch on possible result type.....
			case T_boolean :
				this.resolvedType = BooleanBinding;
				break;
			case T_byte :
				this.resolvedType = ByteBinding;
				break;
			case T_char :
				this.resolvedType = CharBinding;
				break;
			case T_double :
				this.resolvedType = DoubleBinding;
				break;
			case T_float :
				this.resolvedType = FloatBinding;
				break;
			case T_int :
				this.resolvedType = IntBinding;
				break;
			case T_long :
				this.resolvedType = LongBinding;
				break;
			default : //error........
				this.constant = Constant.NotAConstant;
				if (expressionId != T_undefined)
					scope.problemReporter().invalidOperator(this, expressionType);
				return null;
		}
		// compute the constant when valid
		if (this.expression.constant != Constant.NotAConstant) {
			this.constant =
				Constant.computeConstantOperation(
					this.expression.constant,
					expressionId,
					(bits & OperatorMASK) >> OperatorSHIFT);
		} else {
			this.constant = Constant.NotAConstant;
			if (((bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
				Constant cst = expression.optimizedBooleanConstant();
				if (cst != Constant.NotAConstant) 
					this.optimizedBooleanConstant = Constant.fromValue(!cst.booleanValue());
			}
		}
		return this.resolvedType;
	}

	public String toStringExpressionNoParenthesis() {
		
		return operatorToString() + " " + this.expression.toStringExpression(); //$NON-NLS-1$
	} 
	
	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
			
		if (visitor.visit(this, blockScope)) {
			this.expression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
