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

//dedicated treatment for the &&
public class AND_AND_Expression extends BinaryExpression {

	int rightInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public AND_AND_Expression(Expression left, Expression right, int operator) {
		super(left, right, operator);
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

		Constant cst = this.left.optimizedBooleanConstant();
		boolean isLeftOptimizedTrue = cst != NotAConstant && cst.booleanValue() == true;
		boolean isLeftOptimizedFalse = cst != NotAConstant && cst.booleanValue() == false;

		if (isLeftOptimizedTrue) {
			// TRUE && anything
			// need to be careful of scenario:
			//  (x && y) && !z, if passing the left info to the right, it would
			// be swapped by the !
			FlowInfo mergedInfo = left.analyseCode(currentScope, flowContext, flowInfo)
					.unconditionalInits();
			mergedInfo = right.analyseCode(currentScope, flowContext, mergedInfo);
			mergedInitStateIndex = currentScope.methodScope()
					.recordInitializationStates(mergedInfo);
			return mergedInfo;
		}

		FlowInfo leftInfo = left.analyseCode(currentScope, flowContext, flowInfo);
		// need to be careful of scenario:
		//  (x && y) && !z, if passing the left info to the right, it would be
		// swapped by the !
		FlowInfo rightInfo = leftInfo.initsWhenTrue().unconditionalInits().copy();
		rightInitStateIndex = currentScope.methodScope().recordInitializationStates(rightInfo);

		int previousMode = rightInfo.reachMode();
		if (isLeftOptimizedFalse) {
			rightInfo.setReachMode(FlowInfo.UNREACHABLE);
		}
		rightInfo = right.analyseCode(currentScope, flowContext, rightInfo);
		FlowInfo trueMergedInfo = rightInfo.initsWhenTrue().copy();
		rightInfo.setReachMode(previousMode); // reset after trueMergedInfo got extracted
		FlowInfo mergedInfo = FlowInfo.conditional(
				trueMergedInfo, 
				leftInfo.initsWhenFalse().copy().unconditionalInits().mergedWith(
						rightInfo.initsWhenFalse().copy().unconditionalInits()));
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * Code generation for a binary operation
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		int pc = codeStream.position;
		if (constant != Constant.NotAConstant) {
			// inlined value
			if (valueRequired)
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		Constant cst = right.constant;
		if (cst != NotAConstant) {
			// <expr> && true --> <expr>
			if (cst.booleanValue() == true) {
				this.left.generateCode(currentScope, codeStream, valueRequired);
			} else {
				// <expr> && false --> false
				this.left.generateCode(currentScope, codeStream, false);
				if (valueRequired) codeStream.iconst_0();
			}
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			}			
			codeStream.generateImplicitConversion(implicitConversion);
			codeStream.updateLastRecordedEndPC(codeStream.position);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		
		Label falseLabel = new Label(codeStream), endLabel;
		cst = left.optimizedBooleanConstant();
		boolean leftIsConst = cst != NotAConstant;
		boolean leftIsTrue = leftIsConst && cst.booleanValue() == true;

		cst = right.optimizedBooleanConstant();
		boolean rightIsConst = cst != NotAConstant;
		boolean rightIsTrue = rightIsConst && cst.booleanValue() == true;

		generateOperands : {
			if (leftIsConst) {
				left.generateCode(currentScope, codeStream, false);
				if (!leftIsTrue) {
					break generateOperands; // no need to generate right operand
				}
			} else {
				left.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, true); 
				// need value, e.g. if (a == 1 && ((b = 2) > 0)) {} -> shouldn't initialize 'b' if a!=1 
			}
			if (rightInitStateIndex != -1) {
				codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
			}
			if (rightIsConst) {
				right.generateCode(currentScope, codeStream, false);
			} else {
				right.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, valueRequired);
			}
		}
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		/*
		 * improving code gen for such a case: boolean b = i < 0 && false since
		 * the label has never been used, we have the inlined value on the
		 * stack.
		 */
		if (valueRequired) {
			if (leftIsConst && !leftIsTrue) {
				codeStream.iconst_0();
				codeStream.updateLastRecordedEndPC(codeStream.position);
			} else {
				if (rightIsConst && !rightIsTrue) {
					codeStream.iconst_0();
					codeStream.updateLastRecordedEndPC(codeStream.position);
				} else {
					codeStream.iconst_1();
				}
				if (falseLabel.hasForwardReferences()) {
					if ((bits & ValueForReturnMASK) != 0) {
						codeStream.ireturn();
						falseLabel.place();
						codeStream.iconst_0();
					} else {
						codeStream.goto_(endLabel = new Label(codeStream));
						codeStream.decrStackSize(1);
						falseLabel.place();
						codeStream.iconst_0();
						endLabel.place();
					}
				} else {
					falseLabel.place();
				}
			}
			codeStream.generateImplicitConversion(implicitConversion);
			codeStream.updateLastRecordedEndPC(codeStream.position);
		} else {
			falseLabel.place();
		}
	}

	/**
	 * Boolean operator code generation Optimized operations are: &&
	 */
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream,
			Label trueLabel, Label falseLabel, boolean valueRequired) {

		if (constant != Constant.NotAConstant) {
			super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel,
					valueRequired);
			return;
		}

		// <expr> && true --> <expr>
		Constant cst = right.constant;
		if (cst != NotAConstant && cst.booleanValue() == true) {
			int pc = codeStream.position;
			this.left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			}			
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		cst = left.optimizedBooleanConstant();
		boolean leftIsConst = cst != NotAConstant;
		boolean leftIsTrue = leftIsConst && cst.booleanValue() == true;

		cst = right.optimizedBooleanConstant();
		boolean rightIsConst = cst != NotAConstant;
		boolean rightIsTrue = rightIsConst && cst.booleanValue() == true;

		// default case
		generateOperands : {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					Label internalFalseLabel = new Label(codeStream);
					left.generateOptimizedBoolean(currentScope, codeStream, null,
							internalFalseLabel, !leftIsConst); 
					// need value, e.g. if (a == 1 && ((b = 2) > 0)) {} -> shouldn't initialize 'b' if a!=1
					if (leftIsConst && !leftIsTrue) {
						internalFalseLabel.place();
						break generateOperands; // no need to generate right operand
					}
					if (rightInitStateIndex != -1) {
						codeStream
								.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
					}
					right.generateOptimizedBoolean(currentScope, codeStream, trueLabel, null,
							valueRequired && !rightIsConst);
					if (valueRequired && rightIsConst && rightIsTrue) {
						codeStream.goto_(trueLabel);
						codeStream.updateLastRecordedEndPC(codeStream.position);
					}
					internalFalseLabel.place();
				}
			} else {
				// implicit falling through the TRUE case
				if (trueLabel == null) {
					left.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, !leftIsConst); 
					// need value, e.g. if (a == 1 && ((b = 2) > 0)) {} -> shouldn't initialize 'b' if a!=1
					if (leftIsConst && !leftIsTrue) {
						codeStream.goto_(falseLabel);
						codeStream.updateLastRecordedEndPC(codeStream.position);
						break generateOperands; // no need to generate right operand
					}
					if (rightInitStateIndex != -1) {
						codeStream
								.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
					}
					right.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel,
							valueRequired && !rightIsConst);
					if (valueRequired && rightIsConst && !rightIsTrue) {
						codeStream.goto_(falseLabel);
						codeStream.updateLastRecordedEndPC(codeStream.position);
					}
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
	}

	public boolean isCompactableOperation() {
		return false;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			left.traverse(visitor, scope);
			right.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}