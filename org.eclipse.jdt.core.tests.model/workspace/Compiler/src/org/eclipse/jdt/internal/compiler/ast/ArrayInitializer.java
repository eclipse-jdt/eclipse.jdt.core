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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ArrayInitializer extends Expression {
		
	public Expression[] expressions;
	public ArrayBinding binding; //the type of the { , , , }
	
	/**
	 * ArrayInitializer constructor comment.
	 */
	public ArrayInitializer() {

		super();
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

		if (expressions != null) {
			for (int i = 0, max = expressions.length; i < max; i++) {
				flowInfo = expressions[i].analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
			}
		}
		return flowInfo;
	}

	/**
	 * Code generation for a array initializer
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		// Flatten the values and compute the dimensions, by iterating in depth into nested array initializers
		int pc = codeStream.position;
		int expressionLength = (expressions == null) ? 0: expressions.length;
		codeStream.generateInlinedValue(expressionLength);
		codeStream.newArray(currentScope, binding);
		if (expressions != null) {
			// binding is an ArrayType, so I can just deal with the dimension
			int elementsTypeID = binding.dimensions > 1 ? -1 : binding.leafComponentType.id;
			for (int i = 0; i < expressionLength; i++) {
				Expression expr;
				if ((expr = expressions[i]).constant != NotAConstant) {
					switch (elementsTypeID) { // filter out initializations to default values
						case T_int :
						case T_short :
						case T_byte :
						case T_char :
						case T_long :
							if (expr.constant.longValue() != 0) {
								codeStream.dup();
								codeStream.generateInlinedValue(i);
								expr.generateCode(currentScope, codeStream, true);
								codeStream.arrayAtPut(elementsTypeID, false);
							}
							break;
						case T_float :
						case T_double :
							double constantValue = expr.constant.doubleValue();
							if (constantValue == -0.0 || constantValue != 0) {
								codeStream.dup();
								codeStream.generateInlinedValue(i);
								expr.generateCode(currentScope, codeStream, true);
								codeStream.arrayAtPut(elementsTypeID, false);
							}
							break;
						case T_boolean :
							if (expr.constant.booleanValue() != false) {
								codeStream.dup();
								codeStream.generateInlinedValue(i);
								expr.generateCode(currentScope, codeStream, true);
								codeStream.arrayAtPut(elementsTypeID, false);
							}
							break;
						default :
							if (!(expr instanceof NullLiteral)) {
								codeStream.dup();
								codeStream.generateInlinedValue(i);
								expr.generateCode(currentScope, codeStream, true);
								codeStream.arrayAtPut(elementsTypeID, false);
							}
					}
				} else if (!(expr instanceof NullLiteral)) {
					codeStream.dup();
					codeStream.generateInlinedValue(i);
					expr.generateCode(currentScope, codeStream, true);
					codeStream.arrayAtPut(elementsTypeID, false);
				}
			}
		}
		if (!valueRequired) {
			codeStream.pop();
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
	
		output.append('{');
		if (expressions != null) { 	
			int j = 20 ; 
			for (int i = 0 ; i < expressions.length ; i++) {	
				if (i > 0) output.append(", "); //$NON-NLS-1$
				expressions[i].printExpression(0, output);
				j -- ;
				if (j == 0) {
					output.append('\n');
					printIndent(indent+1, output);
					j = 20;
				}
			}
		}
		return output.append('}');
	}

	public TypeBinding resolveTypeExpecting(BlockScope scope, TypeBinding expectedTb) {
		// Array initializers can only occur on the right hand side of an assignment
		// expression, therefore the expected type contains the valid information
		// concerning the type that must be enforced by the elements of the array initializer.
	
		// this method is recursive... (the test on isArrayType is the stop case)
	
		constant = NotAConstant;
		if (expectedTb.isArrayType()) {
			binding = (ArrayBinding) expectedTb;
			if (expressions == null)
				return binding;
			TypeBinding expectedElementsTb = binding.elementsType();
			if (expectedElementsTb.isBaseType()) {
				for (int i = 0, length = expressions.length; i < length; i++) {
					Expression expression = expressions[i];
					TypeBinding expressionTb =
						(expression instanceof ArrayInitializer)
							? expression.resolveTypeExpecting(scope, expectedElementsTb)
							: expression.resolveType(scope);
					if (expressionTb == null)
						return null;
	
					// Compile-time conversion required?
					if (expression.isConstantValueOfTypeAssignableToType(expressionTb, expectedElementsTb)) {
						expression.computeConversion(scope, expectedElementsTb, expressionTb);
					} else if (BaseTypeBinding.isWidening(expectedElementsTb.id, expressionTb.id)) {
						expression.computeConversion(scope, expectedElementsTb, expressionTb);
					} else {
						scope.problemReporter().typeMismatchError(expressionTb, expectedElementsTb, expression);
						return null;
					}
				}
			} else {
				for (int i = 0, length = expressions.length; i < length; i++)
					if (expressions[i].resolveTypeExpecting(scope, expectedElementsTb) == null)
						return null;
			}
			return binding;
		}
		
		// infer initializer type for error reporting based on first element
		TypeBinding leafElementType = null;
		int dim = 1;
		if (expressions == null) {
			leafElementType = scope.getJavaLangObject();
		} else {
			Expression currentExpression = expressions[0];
			while(currentExpression != null && currentExpression instanceof ArrayInitializer) {
				dim++;
				Expression[] subExprs = ((ArrayInitializer) currentExpression).expressions;
				if (subExprs == null){
					leafElementType = scope.getJavaLangObject();
					currentExpression = null;
					break;
				}
				currentExpression = ((ArrayInitializer) currentExpression).expressions[0];
			}
			if (currentExpression != null) {
				leafElementType = currentExpression.resolveType(scope);
			}
		}
		if (leafElementType != null) {
			TypeBinding probableTb = scope.createArrayType(leafElementType, dim);
			scope.problemReporter().typeMismatchError(probableTb, expectedTb, this);
		}
		return null;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (expressions != null) {
				int expressionsLength = expressions.length;
				for (int i = 0; i < expressionsLength; i++)
					expressions[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
