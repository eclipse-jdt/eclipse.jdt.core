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

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		Constant cst = this.left.optimizedBooleanConstant();
		boolean isLeftOptimizedTrue = cst != NotAConstant && cst.booleanValue() == true;
		boolean isLeftOptimizedFalse = cst != NotAConstant && cst.booleanValue() == false;

		if (isLeftOptimizedTrue) {
			// TRUE && anything
			 // need to be careful of scenario:
			//		(x && y) && !z, if passing the left info to the right, it would be swapped by the !
			FlowInfo mergedInfo = left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits(); 
			mergedInfo = right.analyseCode(currentScope, flowContext, mergedInfo);
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		}
		
		FlowInfo leftInfo = left.analyseCode(currentScope, flowContext, flowInfo);
		 // need to be careful of scenario:
		//		(x && y) && !z, if passing the left info to the right, it would be swapped by the !
		FlowInfo rightInfo = leftInfo.initsWhenTrue().unconditionalInits().copy();
		rightInitStateIndex =
			currentScope.methodScope().recordInitializationStates(rightInfo);

		int previousMode = rightInfo.reachMode();
		if (isLeftOptimizedFalse){
			rightInfo.setReachMode(FlowInfo.UNREACHABLE); 
		}
		rightInfo = right.analyseCode(currentScope, flowContext, rightInfo);
		FlowInfo trueMergedInfo = rightInfo.initsWhenTrue().copy();
		rightInfo.setReachMode(previousMode); // reset after trueMergedInfo got extracted
		
		FlowInfo mergedInfo = 
			FlowInfo.conditional(
				trueMergedInfo,
				leftInfo.initsWhenFalse().copy().unconditionalInits().mergedWith(
						rightInfo.initsWhenFalse().copy().unconditionalInits()));
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * Code generation for a binary operation
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		Label falseLabel, endLabel;
		if (constant != Constant.NotAConstant) {
			// inlined value
			if (valueRequired)
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		bits |= OnlyValueRequiredMASK;
		generateOptimizedBoolean(
			currentScope,
			codeStream,
			null,
			(falseLabel = new Label(codeStream)),
			valueRequired);
		/* improving code gen for such a case: boolean b = i < 0 && false
		 * since the label has never been used, we have the inlined value on the stack. */
		if (falseLabel.hasForwardReferences()) {
			if (valueRequired) {
				codeStream.iconst_1();
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
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		}
		// reposition the endPC
		codeStream.updateLastRecordedEndPC(codeStream.position);		
	}

	/**
	 * Boolean operator code generation
	 *	Optimized operations are: &&
	 */
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {
			
		if (constant != Constant.NotAConstant) {
			super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			return;
		}
		Constant condConst;
		if ((condConst = left.optimizedBooleanConstant()) != NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> && x
				left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				if (rightInitStateIndex != -1) {
					codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
				}
				if ((bits & OnlyValueRequiredMASK) != 0) {
					right.generateCode(currentScope, codeStream, valueRequired);
				} else {
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						valueRequired);
				}
			} else {
				// <something equivalent to false> && x
				left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				if (valueRequired) {
					if ((bits & OnlyValueRequiredMASK) != 0) {
						codeStream.iconst_0();
					} else {
						if (falseLabel != null) {
							// implicit falling through the TRUE case
							codeStream.goto_(falseLabel);
						}
					}
				}
				// reposition the endPC
				codeStream.updateLastRecordedEndPC(codeStream.position);
			}
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					mergedInitStateIndex);
			}
			return;
		}
		if ((condConst = right.optimizedBooleanConstant()) != NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x && <something equivalent to true>
				if ((bits & OnlyValueRequiredMASK) != 0) {
					left.generateCode(currentScope, codeStream, valueRequired);
				} else {
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						valueRequired);
				}
				if (rightInitStateIndex != -1) {
					codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
				}
				right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
			} else {
				// x && <something equivalent to false>
				Label internalTrueLabel = new Label(codeStream);
				left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					internalTrueLabel, // will be false in the end
					null,
					false);
				if (rightInitStateIndex != -1) {
					codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
				}
				internalTrueLabel.place();
				right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				if (valueRequired) {
					if ((bits & OnlyValueRequiredMASK) != 0) {
						codeStream.iconst_0();
					} else {
						if (falseLabel != null) {
							// implicit falling through the TRUE case
							codeStream.goto_(falseLabel);
						}
					}
				}
				// reposition the endPC
				codeStream.updateLastRecordedEndPC(codeStream.position);
			}
			if (mergedInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					mergedInitStateIndex);
			}
			return;
		}
		// default case
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				Label internalFalseLabel = new Label(codeStream);
				left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					null,
					internalFalseLabel,
					true);
				if (rightInitStateIndex != -1) {
					codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
				}
				right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					null,
					valueRequired);
				internalFalseLabel.place();
			}
		} else {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				left.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, true);
				if (rightInitStateIndex != -1) {
					codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);
				}
				right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					null,
					falseLabel,
					valueRequired);
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
	}

	public boolean isCompactableOperation() {
		return false;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			left.traverse(visitor, scope);
			right.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}