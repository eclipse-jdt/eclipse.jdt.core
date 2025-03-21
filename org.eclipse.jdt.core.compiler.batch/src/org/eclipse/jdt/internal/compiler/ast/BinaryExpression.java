/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class BinaryExpression extends OperatorExpression {

/* Tracking helpers
 * The following are used to elaborate realistic statistics about binary
 * expressions. This must be neutralized in the released code.
 * Search the keyword BE_INSTRUMENTATION to reenable.
 * An external device must install a suitable probe so as to monitor the
 * emission of events and publish the results.
	public interface Probe {
		public void ping(int depth);
	}
	public int depthTracker;
	public static Probe probe;
 */

	public Expression left, right;
	public Constant optimizedBooleanConstant;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding syntheticAccessor = null;
	public int overloadedExpresionSide = -1;
	protected static final int overloadedLeftSide = 0;
	protected static final int overloadedRightSide = 1;
	public TypeBinding expectedType = null;//Operator overload, for generic function call

	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

public BinaryExpression(Expression left, Expression right, int operator) {
	this.left = left;
	this.right = right;
	this.bits |= operator << ASTNode.OperatorSHIFT; // encode operator
	this.sourceStart = left.sourceStart;
	this.sourceEnd = right.sourceEnd;
	// BE_INSTRUMENTATION: neutralized in the released code
//	if (left instanceof BinaryExpression &&
//			((left.bits & OperatorMASK) ^ (this.bits & OperatorMASK)) == 0) {
//		this.depthTracker = ((BinaryExpression)left).depthTracker + 1;
//	} else {
//		this.depthTracker = 1;
//	}
}
public BinaryExpression(BinaryExpression expression) {
	this.left = expression.left;
	this.right = expression.right;
	this.bits = expression.bits;
	this.sourceStart = expression.sourceStart;
	this.sourceEnd = expression.sourceEnd;
}
@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// keep implementation in sync with CombinedBinaryExpression#analyseCode
	try {
		if (this.appropriateMethodForOverload != null) {
			MethodBinding original = this.appropriateMethodForOverload.original();
			if (original.isPrivate()) {
				this.syntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}
		if (this.resolvedType.id == TypeIds.T_JavaLangString) {
			return this.right.analyseCode(
								currentScope, flowContext,
								this.left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits())
							.unconditionalInits();
		} else {
			flowInfo = this.left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
			this.left.checkNPE(currentScope, flowContext, flowInfo);
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) != AND) {
				flowContext.expireNullCheckedFieldInfo();
			}
			flowInfo = this.right.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
			this.right.checkNPE(currentScope, flowContext, flowInfo);
			if (((this.bits & OperatorMASK) >> OperatorSHIFT) != AND) {
				flowContext.expireNullCheckedFieldInfo();
			}
			return flowInfo;
		}
	} finally {
		// account for exception possibly thrown by arithmetics
		flowContext.recordAbruptExit();
	}
}

@Override
protected void updateFlowOnBooleanResult(FlowInfo flowInfo, boolean result) {
	int operator = (this.bits & OperatorMASK) >> OperatorSHIFT;
	if (result ? operator == AND_AND : operator == OR_OR) {
		this.left.updateFlowOnBooleanResult(flowInfo, result);
		this.right.updateFlowOnBooleanResult(flowInfo, result);
	}
}

public void computeConstant(BlockScope scope, int leftId, int rightId) {
	//compute the constant when valid
	if ((this.left.constant != Constant.NotAConstant)
		&& (this.right.constant != Constant.NotAConstant)) {
		try {
			this.constant =
				Constant.computeConstantOperation(
					this.left.constant,
					leftId,
					(this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT,
					this.right.constant,
					rightId);
		} catch (ArithmeticException e) {
			this.constant = Constant.NotAConstant;
			// 1.2 no longer throws an exception at compile-time
			//scope.problemReporter().compileTimeConstantThrowsArithmeticException(this);
		}
	} else {
		this.constant = Constant.NotAConstant;
		//add some work for the boolean operators & |
		this.optimizedBooleanConstant(
			leftId,
			(this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT,
			rightId);
	}
}

@Override
public Constant optimizedBooleanConstant() {
	return this.optimizedBooleanConstant == null ? this.constant : this.optimizedBooleanConstant;
}

/**
 * Code generation for a binary operation
 */
// given the current focus of CombinedBinaryExpression on strings concatenation,
// we do not provide a general, non-recursive implementation of generateCode,
// but rely upon generateOptimizedStringConcatenationCreation instead
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (this.constant != Constant.NotAConstant) {
		if (valueRequired)
			codeStream.generateConstant(this.constant, this.implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	}
	if (this.appropriateMethodForOverload != null){
		this.generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	}
	switch ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
		case PLUS :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_JavaLangString :
					// BE_INSTRUMENTATION: neutralized in the released code
//					if (probe != null) {
//						probe.ping(this.depthTracker);
//					}
					codeStream.generateStringConcatenationAppend(currentScope, this.left, this.right);
					if (!valueRequired)
						codeStream.pop();
					break;
				case T_int :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.iadd();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.ladd();
					break;
				case T_double :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.dadd();
					break;
				case T_float :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.fadd();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case MINUS :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.isub();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.lsub();
					break;
				case T_double :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.dsub();
					break;
				case T_float :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.fsub();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case MULTIPLY :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.imul();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.lmul();
					break;
				case T_double :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.dmul();
					break;
				case T_float :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.fmul();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case DIVIDE :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, true);
					this.right.generateCode(currentScope, codeStream, true);
					codeStream.idiv();
					if (!valueRequired)
						codeStream.pop();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, true);
					this.right.generateCode(currentScope, codeStream, true);
					codeStream.ldiv();
					if (!valueRequired)
						codeStream.pop2();
					break;
				case T_double :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.ddiv();
					break;
				case T_float :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.fdiv();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case CAT :
			generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
			break;
		case EQUAL_EQUAL_EQUAL :
			generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
			break;
		case NOT_EQUAL_EQUAL :
			generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
			break;
		case REMAINDER :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, true);
					this.right.generateCode(currentScope, codeStream, true);
					codeStream.irem();
					if (!valueRequired)
						codeStream.pop();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, true);
					this.right.generateCode(currentScope, codeStream, true);
					codeStream.lrem();
					if (!valueRequired)
						codeStream.pop2();
					break;
				case T_double :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.drem();
					break;
				case T_float :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.frem();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case AND :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					// 0 & x
					if ((this.left.constant != Constant.NotAConstant)
						&& (this.left.constant.typeID() == TypeIds.T_int)
						&& (this.left.constant.intValue() == 0)) {
						this.right.generateCode(currentScope, codeStream, false);
						if (valueRequired)
							codeStream.iconst_0();
					} else {
						// x & 0
						if ((this.right.constant != Constant.NotAConstant)
							&& (this.right.constant.typeID() == TypeIds.T_int)
							&& (this.right.constant.intValue() == 0)) {
							this.left.generateCode(currentScope, codeStream, false);
							if (valueRequired)
								codeStream.iconst_0();
						} else {
							this.left.generateCode(currentScope, codeStream, valueRequired);
							this.right.generateCode(currentScope, codeStream, valueRequired);
							if (valueRequired)
								codeStream.iand();
						}
					}
					break;
				case T_long :
					// 0 & x
					if ((this.left.constant != Constant.NotAConstant)
						&& (this.left.constant.typeID() == TypeIds.T_long)
						&& (this.left.constant.longValue() == 0L)) {
						this.right.generateCode(currentScope, codeStream, false);
						if (valueRequired)
							codeStream.lconst_0();
					} else {
						// x & 0
						if ((this.right.constant != Constant.NotAConstant)
							&& (this.right.constant.typeID() == TypeIds.T_long)
							&& (this.right.constant.longValue() == 0L)) {
							this.left.generateCode(currentScope, codeStream, false);
							if (valueRequired)
								codeStream.lconst_0();
						} else {
							this.left.generateCode(currentScope, codeStream, valueRequired);
							this.right.generateCode(currentScope, codeStream, valueRequired);
							if (valueRequired)
								codeStream.land();
						}
					}
					break;
				case T_boolean : // logical and
					generateLogicalAnd(currentScope, codeStream, valueRequired);
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case OR :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					// 0 | x
					if ((this.left.constant != Constant.NotAConstant)
						&& (this.left.constant.typeID() == TypeIds.T_int)
						&& (this.left.constant.intValue() == 0)) {
						this.right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						// x | 0
						if ((this.right.constant != Constant.NotAConstant)
							&& (this.right.constant.typeID() == TypeIds.T_int)
							&& (this.right.constant.intValue() == 0)) {
							this.left.generateCode(currentScope, codeStream, valueRequired);
						} else {
							this.left.generateCode(currentScope, codeStream, valueRequired);
							this.right.generateCode(currentScope, codeStream, valueRequired);
							if (valueRequired)
								codeStream.ior();
						}
					}
					break;
				case T_long :
					// 0 | x
					if ((this.left.constant != Constant.NotAConstant)
						&& (this.left.constant.typeID() == TypeIds.T_long)
						&& (this.left.constant.longValue() == 0L)) {
						this.right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						// x | 0
						if ((this.right.constant != Constant.NotAConstant)
							&& (this.right.constant.typeID() == TypeIds.T_long)
							&& (this.right.constant.longValue() == 0L)) {
							this.left.generateCode(currentScope, codeStream, valueRequired);
						} else {
							this.left.generateCode(currentScope, codeStream, valueRequired);
							this.right.generateCode(currentScope, codeStream, valueRequired);
							if (valueRequired)
								codeStream.lor();
						}
					}
					break;
				case T_boolean : // logical or
					generateLogicalOr(currentScope, codeStream, valueRequired);
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case XOR :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					// 0 ^ x
					if ((this.left.constant != Constant.NotAConstant)
						&& (this.left.constant.typeID() == TypeIds.T_int)
						&& (this.left.constant.intValue() == 0)) {
						this.right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						// x ^ 0
						if ((this.right.constant != Constant.NotAConstant)
							&& (this.right.constant.typeID() == TypeIds.T_int)
							&& (this.right.constant.intValue() == 0)) {
							this.left.generateCode(currentScope, codeStream, valueRequired);
						} else {
							this.left.generateCode(currentScope, codeStream, valueRequired);
							this.right.generateCode(currentScope, codeStream, valueRequired);
							if (valueRequired)
								codeStream.ixor();
						}
					}
					break;
				case T_long :
					// 0 ^ x
					if ((this.left.constant != Constant.NotAConstant)
						&& (this.left.constant.typeID() == TypeIds.T_long)
						&& (this.left.constant.longValue() == 0L)) {
						this.right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						// x ^ 0
						if ((this.right.constant != Constant.NotAConstant)
							&& (this.right.constant.typeID() == TypeIds.T_long)
							&& (this.right.constant.longValue() == 0L)) {
							this.left.generateCode(currentScope, codeStream, valueRequired);
						} else {
							this.left.generateCode(currentScope, codeStream, valueRequired);
							this.right.generateCode(currentScope, codeStream, valueRequired);
							if (valueRequired)
								codeStream.lxor();
						}
					}
					break;
				case T_boolean :
					generateLogicalXor(currentScope, 	codeStream, valueRequired);
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case LEFT_SHIFT :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.ishl();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.lshl();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case RIGHT_SHIFT :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.ishr();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.lshr();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case UNSIGNED_RIGHT_SHIFT :
			switch (this.bits & ASTNode.ReturnTypeIDMASK) {
				case T_int :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.iushr();
					break;
				case T_long :
					this.left.generateCode(currentScope, codeStream, valueRequired);
					this.right.generateCode(currentScope, codeStream, valueRequired);
					if (valueRequired)
						codeStream.lushr();
					break;
				default:
					generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
					break;
			}
			break;
		case GREATER :
			if ((this.bits & ASTNode.ReturnTypeIDMASK) != T_boolean) {
				generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
				break;
			}
			BranchLabel falseLabel, endLabel;
			generateOptimizedGreaterThan(
				currentScope,
				codeStream,
				null,
				(falseLabel = new BranchLabel(codeStream)),
				valueRequired);
			if (valueRequired) {
				codeStream.iconst_1();
				if ((this.bits & ASTNode.IsReturnedValue) != 0) {
					codeStream.generateImplicitConversion(this.implicitConversion);
					codeStream.generateReturnBytecode(this);
					falseLabel.place();
					codeStream.iconst_0();
				} else {
					codeStream.goto_(endLabel = new BranchLabel(codeStream));
					falseLabel.place();
					codeStream.iconst_0();
					endLabel.place();
				}
			}
			break;
		case GREATER_EQUAL :
			if ((this.bits & ASTNode.ReturnTypeIDMASK) != T_boolean) {
				generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
				break;
			}
			generateOptimizedGreaterThanOrEqual(
				currentScope,
				codeStream,
				null,
				(falseLabel = new BranchLabel(codeStream)),
				valueRequired);
			if (valueRequired) {
				codeStream.iconst_1();
				if ((this.bits & ASTNode.IsReturnedValue) != 0) {
					codeStream.generateImplicitConversion(this.implicitConversion);
					codeStream.generateReturnBytecode(this);
					falseLabel.place();
					codeStream.iconst_0();
				} else {
					codeStream.goto_(endLabel = new BranchLabel(codeStream));
					falseLabel.place();
					codeStream.iconst_0();
					endLabel.place();
				}
			}
			break;
		case LESS :
			if ((this.bits & ASTNode.ReturnTypeIDMASK) != T_boolean) {
				generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
				break;
			}
			generateOptimizedLessThan(
				currentScope,
				codeStream,
				null,
				(falseLabel = new BranchLabel(codeStream)),
				valueRequired);
			if (valueRequired) {
				codeStream.iconst_1();
				if ((this.bits & ASTNode.IsReturnedValue) != 0) {
					codeStream.generateImplicitConversion(this.implicitConversion);
					codeStream.generateReturnBytecode(this);
					falseLabel.place();
					codeStream.iconst_0();
				} else {
					codeStream.goto_(endLabel = new BranchLabel(codeStream));
					falseLabel.place();
					codeStream.iconst_0();
					endLabel.place();
				}
			}
			break;
		case LESS_EQUAL :
			if ((this.bits & ASTNode.ReturnTypeIDMASK) != T_boolean) {
				generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
				break;
			}
			generateOptimizedLessThanOrEqual(
				currentScope,
				codeStream,
				null,
				(falseLabel = new BranchLabel(codeStream)),
				valueRequired);
			if (valueRequired) {
				codeStream.iconst_1();
				if ((this.bits & ASTNode.IsReturnedValue) != 0) {
					codeStream.generateImplicitConversion(this.implicitConversion);
					codeStream.generateReturnBytecode(this);
					falseLabel.place();
					codeStream.iconst_0();
				} else {
					codeStream.goto_(endLabel = new BranchLabel(codeStream));
					falseLabel.place();
					codeStream.iconst_0();
					endLabel.place();
				}
			}
			break;
		default:
			generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
			break;
	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * Boolean operator code generation. Optimized operations are: {@code <, <=, >, >=, &, |, ^}
 */
@Override
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	if ((this.constant != Constant.NotAConstant) && (this.constant.typeID() == TypeIds.T_boolean)) {
		super.generateOptimizedBoolean(
			currentScope,
			codeStream,
			trueLabel,
			falseLabel,
			valueRequired);
		return;
	}
	switch ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
		case LESS :
			generateOptimizedLessThan(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		case LESS_EQUAL :
			generateOptimizedLessThanOrEqual(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		case GREATER :
			generateOptimizedGreaterThan(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		case GREATER_EQUAL :
			generateOptimizedGreaterThanOrEqual(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		case AND :
			generateOptimizedLogicalAnd(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		case OR :
			generateOptimizedLogicalOr(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		case XOR :
			generateOptimizedLogicalXor(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
	}
	super.generateOptimizedBoolean(
		currentScope,
		codeStream,
		trueLabel,
		falseLabel,
		valueRequired);
}

/**
 * Boolean generation for >
 */
public void generateOptimizedGreaterThan(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	int promotedTypeID = (this.left.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	// both sides got promoted in the same way
	if (promotedTypeID == TypeIds.T_int) {
		// 0 > x
		if ((this.left.constant != Constant.NotAConstant) && (this.left.constant.intValue() == 0)) {
			this.right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.iflt(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.ifge(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			// reposition the endPC
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
		// x > 0
		if ((this.right.constant != Constant.NotAConstant) && (this.right.constant.intValue() == 0)) {
			this.left.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.ifgt(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.ifle(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			// reposition the endPC
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
	}
	// default comparison
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmpgt(trueLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpl();
						codeStream.ifgt(trueLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.ifgt(trueLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpl();
						codeStream.ifgt(trueLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpeq(trueLabel);
						break;
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			}
		} else {
			if (trueLabel == null) {
				// implicit falling through the TRUE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmple(falseLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpl();
						codeStream.ifle(falseLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.ifle(falseLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpl();
						codeStream.ifle(falseLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpne(falseLabel);
						break;
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
}

/**
 * Boolean generation for >=
 */
public void generateOptimizedGreaterThanOrEqual(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	int promotedTypeID = (this.left.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	// both sides got promoted in the same way
	if (promotedTypeID == TypeIds.T_int) {
		// 0 >= x
		if ((this.left.constant != Constant.NotAConstant) && (this.left.constant.intValue() == 0)) {
			this.right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.ifle(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.ifgt(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			// reposition the endPC
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
		// x >= 0
		if ((this.right.constant != Constant.NotAConstant) && (this.right.constant.intValue() == 0)) {
			this.left.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.ifge(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.iflt(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			// reposition the endPC
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
	}
	// default comparison
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmpge(trueLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpl();
						codeStream.ifge(trueLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.ifge(trueLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpl();
						codeStream.ifge(trueLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpeq(trueLabel);
						break;
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			}
		} else {
			if (trueLabel == null) {
				// implicit falling through the TRUE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmplt(falseLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpl();
						codeStream.iflt(falseLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.iflt(falseLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpl();
						codeStream.iflt(falseLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpne(falseLabel);
						break;
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
}

/**
 * Boolean generation for {@code <}
 */
public void generateOptimizedLessThan(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	int promotedTypeID = (this.left.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	// both sides got promoted in the same way
	if (promotedTypeID == TypeIds.T_int) {
		// 0 < x
		if ((this.left.constant != Constant.NotAConstant) && (this.left.constant.intValue() == 0)) {
			this.right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.ifgt(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.ifle(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
		// x < 0
		if ((this.right.constant != Constant.NotAConstant) && (this.right.constant.intValue() == 0)) {
			this.left.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.iflt(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.ifge(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
	}
	// default comparison
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmplt(trueLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpg();
						codeStream.iflt(trueLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.iflt(trueLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpg();
						codeStream.iflt(trueLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpeq(trueLabel);
						break;
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			}
		} else {
			if (trueLabel == null) {
				// implicit falling through the TRUE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmpge(falseLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpg();
						codeStream.ifge(falseLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.ifge(falseLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpg();
						codeStream.ifge(falseLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpne(falseLabel);
						break;
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
}

/**
 * Boolean generation for {@code <=}
 */
public void generateOptimizedLessThanOrEqual(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	int promotedTypeID = (this.left.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	// both sides got promoted in the same way
	if (promotedTypeID == TypeIds.T_int) {
		// 0 <= x
		if ((this.left.constant != Constant.NotAConstant) && (this.left.constant.intValue() == 0)) {
			this.right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.ifge(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.iflt(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			// reposition the endPC
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
		// x <= 0
		if ((this.right.constant != Constant.NotAConstant) && (this.right.constant.intValue() == 0)) {
			this.left.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicitly falling through the FALSE case
						codeStream.ifle(trueLabel);
					}
				} else {
					if (trueLabel == null) {
						// implicitly falling through the TRUE case
						codeStream.ifgt(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			// reposition the endPC
			codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			return;
		}
	}
	// default comparison
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmple(trueLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpg();
						codeStream.ifle(trueLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.ifle(trueLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpg();
						codeStream.ifle(trueLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpeq(trueLabel);
						break;
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			}
		} else {
			if (trueLabel == null) {
				// implicit falling through the TRUE case
				switch (promotedTypeID) {
					case T_int :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.if_icmpgt(falseLabel);
						break;
					case T_float :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.fcmpg();
						codeStream.ifgt(falseLabel);
						break;
					case T_long :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.lcmp();
						codeStream.ifgt(falseLabel);
						break;
					case T_double :
						this.left.generateCode(currentScope, codeStream, valueRequired);
						this.right.generateCode(currentScope, codeStream, valueRequired);
						codeStream.dcmpg();
						codeStream.ifgt(falseLabel);
						break;
					default:
						generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
						codeStream.generateInlinedValue(true);
						codeStream.if_icmpne(falseLabel);
						break;
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				return;
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
}

/**
 * Boolean generation for {@code &}
 */
public void generateLogicalAnd(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	Constant condConst;
	if ((this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK) == TypeIds.T_boolean) {
		if ((condConst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> & x
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, valueRequired);
			} else {
				// <something equivalent to false> & x
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, false);
				if (valueRequired) {
					codeStream.iconst_0();
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			}
			return;
		}
		if ((condConst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x & <something equivalent to true>
				this.left.generateCode(currentScope, codeStream, valueRequired);
				this.right.generateCode(currentScope, codeStream, false);
			} else {
				// x & <something equivalent to false>
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, false);
				if (valueRequired) {
					codeStream.iconst_0();
				}
				// reposition the endPC
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			}
			return;
		}
	}
	// default case
	boolean custom = (TypeBinding.notEquals(this.right.resolvedType, TypeBinding.BOOLEAN) && this.right.resolvedType.id != TypeIds.T_JavaLangBoolean)
		|| (TypeBinding.notEquals(this.left.resolvedType, TypeBinding.BOOLEAN) && this.left.resolvedType.id != TypeIds.T_JavaLangBoolean);
	if (custom) {
		generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
	} else {
		this.left.generateCode(currentScope, codeStream, valueRequired);
		this.right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			codeStream.iand();
		}
	}
	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}

/**
 * Boolean generation for |
 */
public void generateLogicalOr(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	Constant condConst;
	if ((this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK) == TypeIds.T_boolean) {
		if ((condConst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> | x
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, false);
				if (valueRequired) {
					codeStream.iconst_1();
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			} else {
				// <something equivalent to false> | x
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, valueRequired);
			}
			return;
		}
		if ((condConst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x | <something equivalent to true>
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, false);
				if (valueRequired) {
					codeStream.iconst_1();
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			} else {
				// x | <something equivalent to false>
				this.left.generateCode(currentScope, codeStream, valueRequired);
				this.right.generateCode(currentScope, codeStream, false);
			}
			return;
		}
	}
	// default case
	boolean custom = (TypeBinding.notEquals(this.right.resolvedType, TypeBinding.BOOLEAN) && this.right.resolvedType.id != TypeIds.T_JavaLangBoolean)
		|| (TypeBinding.notEquals(this.left.resolvedType, TypeBinding.BOOLEAN) && this.left.resolvedType.id != TypeIds.T_JavaLangBoolean);
	if (custom) {
		generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
	} else {
		this.left.generateCode(currentScope, codeStream, valueRequired);
		this.right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			codeStream.ior();
		}
	}
	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}

/**
 * Boolean generation for ^
 */
public void generateLogicalXor(BlockScope currentScope,	CodeStream codeStream, boolean valueRequired) {
	Constant condConst;
	if ((this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK) == TypeIds.T_boolean) {
		if ((condConst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> ^ x
				this.left.generateCode(currentScope, codeStream, false);
				if (valueRequired) {
					codeStream.iconst_1();
				}
				this.right.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					codeStream.ixor(); // negate
					codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				}
			} else {
				// <something equivalent to false> ^ x
				this.left.generateCode(currentScope, codeStream, false);
				this.right.generateCode(currentScope, codeStream, valueRequired);
			}
			return;
		}
		if ((condConst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x ^ <something equivalent to true>
				this.left.generateCode(currentScope, codeStream, valueRequired);
				this.right.generateCode(currentScope, codeStream, false);
				if (valueRequired) {
					codeStream.iconst_1();
					codeStream.ixor(); // negate
					codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
				}
			} else {
				// x ^ <something equivalent to false>
				this.left.generateCode(currentScope, codeStream, valueRequired);
				this.right.generateCode(currentScope, codeStream, false);
			}
			return;
		}
	}
	// default case
	boolean custom = (TypeBinding.notEquals(this.right.resolvedType, TypeBinding.BOOLEAN) && this.right.resolvedType.id != TypeIds.T_JavaLangBoolean)
		|| (TypeBinding.notEquals(this.left.resolvedType, TypeBinding.BOOLEAN) && this.left.resolvedType.id != TypeIds.T_JavaLangBoolean);
	if (custom) {
		generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
	} else{
		this.left.generateCode(currentScope, codeStream, valueRequired);
		this.right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			codeStream.ixor();
		}
	}
	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}

/**
 * Boolean generation for {@code &}
 */
public void generateOptimizedLogicalAnd(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	Constant condConst;
	if ((this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK) == TypeIds.T_boolean) {
		if ((condConst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> & x
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
			} else {
				// <something equivalent to false> & x
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				if (valueRequired) {
					if (falseLabel != null) {
						// implicit falling through the TRUE case
						codeStream.goto_(falseLabel);
					}
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			}
			return;
		}
		if ((condConst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x & <something equivalent to true>
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
			} else {
				// x & <something equivalent to false>
				BranchLabel internalTrueLabel = new BranchLabel(codeStream);
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					internalTrueLabel,
					falseLabel,
					false);
				internalTrueLabel.place();
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				if (valueRequired) {
					if (falseLabel != null) {
						// implicit falling through the TRUE case
						codeStream.goto_(falseLabel);
					}
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			}
			return;
		}
	}
	// default case
	boolean custom = (TypeBinding.notEquals(this.right.resolvedType, TypeBinding.BOOLEAN) && this.right.resolvedType.id != TypeIds.T_JavaLangBoolean)
		|| (TypeBinding.notEquals(this.left.resolvedType, TypeBinding.BOOLEAN) && this.left.resolvedType.id != TypeIds.T_JavaLangBoolean);
	if (custom) {
		generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
	} else {
		this.left.generateCode(currentScope, codeStream, valueRequired);
		this.right.generateCode(currentScope, codeStream, valueRequired);
	}
	if (valueRequired) {
		if (custom) {
			codeStream.ineg();
		} else {
			codeStream.iand();
		}
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				codeStream.ifne(trueLabel);
			}
		} else {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				codeStream.ifeq(falseLabel);
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}

/**
 * Boolean generation for |
 */
public void generateOptimizedLogicalOr(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	Constant condConst;
	if ((this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK) == TypeIds.T_boolean) {
		if ((condConst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> | x
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				BranchLabel internalFalseLabel = new BranchLabel(codeStream);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					internalFalseLabel,
					false);
				internalFalseLabel.place();
				if (valueRequired) {
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			} else {
				// <something equivalent to false> | x
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
			}
			return;
		}
		if ((condConst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x | <something equivalent to true>
				BranchLabel internalFalseLabel = new BranchLabel(codeStream);
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					internalFalseLabel,
					false);
				internalFalseLabel.place();
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				if (valueRequired) {
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
				codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
			} else {
				// x | <something equivalent to false>
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
			}
			return;
		}
	}
	// default case
	boolean custom = (TypeBinding.notEquals(this.right.resolvedType, TypeBinding.BOOLEAN) && this.right.resolvedType.id != TypeIds.T_JavaLangBoolean)
		|| (TypeBinding.notEquals(this.left.resolvedType, TypeBinding.BOOLEAN) && this.left.resolvedType.id != TypeIds.T_JavaLangBoolean);
	if (custom) {
		generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
	} else {
		this.left.generateCode(currentScope, codeStream, valueRequired);
		this.right.generateCode(currentScope, codeStream, valueRequired);
	}
	if (valueRequired) {
		if (custom) {
			codeStream.ineg();
		} else {
			codeStream.ior();
		}
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				codeStream.ifne(trueLabel);
			}
		} else {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				codeStream.ifeq(falseLabel);
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}

/**
 * Boolean generation for ^
 */
public void generateOptimizedLogicalXor(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
	Constant condConst;
	if ((this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK) == TypeIds.T_boolean) {
		if ((condConst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// <something equivalent to true> ^ x
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					falseLabel, // negating
					trueLabel,
					valueRequired);
			} else {
				// <something equivalent to false> ^ x
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
			}
			return;
		}
		if ((condConst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
			if (condConst.booleanValue() == true) {
				// x ^ <something equivalent to true>
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					falseLabel, // negating
					trueLabel,
					valueRequired);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
			} else {
				// x ^ <something equivalent to false>
				this.left.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				this.right.generateOptimizedBoolean(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					false);
			}
			return;
		}
	}
	// default case
	boolean custom = (TypeBinding.notEquals(this.right.resolvedType, TypeBinding.BOOLEAN) && this.right.resolvedType.id != TypeIds.T_JavaLangBoolean)
		|| (TypeBinding.notEquals(this.left.resolvedType, TypeBinding.BOOLEAN) && this.left.resolvedType.id != TypeIds.T_JavaLangBoolean);
	if (custom) {
		generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
	} else {
		this.left.generateCode(currentScope, codeStream, valueRequired);
		this.right.generateCode(currentScope, codeStream, valueRequired);
	}
	if (valueRequired) {
		if (custom) {
			codeStream.ineg();
		} else {
			codeStream.ixor();
		}
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				codeStream.ifne(trueLabel);
			}
		} else {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				codeStream.ifeq(falseLabel);
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}
@Override
public void buildStringForConcatation(BlockScope blockScope, CodeStream codeStream, int typeID, StringBuilder recipe, List<TypeBinding> argTypes) {
	if ((((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.PLUS)
			&& ((this.bits & ASTNode.ReturnTypeIDMASK) == TypeIds.T_JavaLangString)) {
			if (this.constant != Constant.NotAConstant) {
				super.buildStringForConcatation(blockScope, codeStream, typeID, recipe, argTypes);
			} else {
				this.left.buildStringForConcatation(blockScope, codeStream, this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK, recipe, argTypes);
				this.right.buildStringForConcatation(blockScope, codeStream, this.right.implicitConversion & TypeIds.COMPILE_TYPE_MASK, recipe, argTypes);
			}
		} else {
			super.buildStringForConcatation(blockScope, codeStream, typeID, recipe, argTypes);
		}
}
@Override
public void generateOptimizedStringConcatenation(BlockScope blockScope, CodeStream codeStream, int typeID) {
	// keep implementation in sync with CombinedBinaryExpression
	// #generateOptimizedStringConcatenation
	/* In the case trying to make a string concatenation, there is no need to create a new
	 * string buffer, thus use a lower-level API for code generation involving only the
	 * appending of arguments to the existing StringBuffer
	 */

	if ((((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.PLUS)
		&& ((this.bits & ASTNode.ReturnTypeIDMASK) == TypeIds.T_JavaLangString)) {
		if (this.constant != Constant.NotAConstant) {
			codeStream.generateConstant(this.constant, this.implicitConversion);
			codeStream.invokeStringConcatenationAppendForType(this.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
		} else {
			int pc = codeStream.position;
			this.left.generateOptimizedStringConcatenation(
				blockScope,
				codeStream,
				this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
			codeStream.recordPositionsFrom(pc, this.left.sourceStart);
			pc = codeStream.position;
			this.right.generateOptimizedStringConcatenation(
				blockScope,
				codeStream,
				this.right.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
			codeStream.recordPositionsFrom(pc, this.right.sourceStart);
		}
	} else {
		super.generateOptimizedStringConcatenation(blockScope, codeStream, typeID);
	}
}

@Override
public void generateOptimizedStringConcatenationCreation(BlockScope blockScope, CodeStream codeStream, int typeID) {
	// keep implementation in sync with CombinedBinaryExpression
	// #generateOptimizedStringConcatenationCreation
	/* In the case trying to make a string concatenation, there is no need to create a new
	 * string buffer, thus use a lower-level API for code generation involving only the
	 * appending of arguments to the existing StringBuffer
	 */
	if ((((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.PLUS)
		&& ((this.bits & ASTNode.ReturnTypeIDMASK) == TypeIds.T_JavaLangString)) {
		if (this.constant != Constant.NotAConstant) {
			codeStream.newStringContatenation(); // new: java.lang.StringBuffer
			codeStream.dup();
			codeStream.ldc(this.constant.stringValue());
			codeStream.invokeStringConcatenationStringConstructor();
			// invokespecial: java.lang.StringBuffer.<init>(Ljava.lang.String;)V
		} else {
			int pc = codeStream.position;
			this.left.generateOptimizedStringConcatenationCreation(
				blockScope,
				codeStream,
				this.left.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
			codeStream.recordPositionsFrom(pc, this.left.sourceStart);
			pc = codeStream.position;
			this.right.generateOptimizedStringConcatenation(
				blockScope,
				codeStream,
				this.right.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
			codeStream.recordPositionsFrom(pc, this.right.sourceStart);
		}
	} else {
		super.generateOptimizedStringConcatenationCreation(blockScope, codeStream, typeID);
	}
}

boolean isCompactable = true;

@Override
public boolean isCompactableOperation() {
	return this.isCompactable;
}

/**
 * Separates into a reusable method the subpart of {@link
 * #resolveType(BlockScope)} that needs to be executed while climbing up the
 * chain of expressions of this' leftmost branch. For use by {@link
 * CombinedBinaryExpression#resolveType(BlockScope)}.
 * @param scope the scope within which the resolution occurs
 */
void nonRecursiveResolveTypeUpwards(BlockScope scope) {
	// keep implementation in sync with BinaryExpression#resolveType
	boolean leftIsCast, rightIsCast;
	TypeBinding leftType = this.left.resolvedType;

	if ((rightIsCast = this.right instanceof CastExpression) == true) {
		this.right.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
	}
	TypeBinding rightType = this.right.resolveType(scope);

	// use the id of the type to navigate into the table
	if (leftType == null || rightType == null) {
		this.constant = Constant.NotAConstant;
		return;
	}

	int leftTypeID = leftType.id;
	int rightTypeID = rightType.id;

	// autoboxing support
	boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
	if (use15specifics) {
		if (!leftType.isBaseType() && rightTypeID != TypeIds.T_JavaLangString && rightTypeID != TypeIds.T_null) {
			leftTypeID = scope.environment().computeBoxingType(leftType).id;
		}
		if (!rightType.isBaseType() && leftTypeID != TypeIds.T_JavaLangString && leftTypeID != TypeIds.T_null) {
			rightTypeID = scope.environment().computeBoxingType(rightType).id;
		}
	}
	if (leftTypeID > 15
		|| rightTypeID > 15) { // must convert String + Object || Object + String
		if (leftTypeID == TypeIds.T_JavaLangString) {
			rightTypeID = TypeIds.T_JavaLangObject;
		} else if (rightTypeID == TypeIds.T_JavaLangString) {
			leftTypeID = TypeIds.T_JavaLangObject;
		} else {
			this.constant = Constant.NotAConstant;
			scope.problemReporter().invalidOperator(this, leftType, rightType);
			return;
		}
	}
	if (((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.PLUS) {
		if (leftTypeID == TypeIds.T_JavaLangString) {
			this.left.computeConversion(scope, leftType, leftType);
			if (rightType.isArrayType() && TypeBinding.equalsEquals(((ArrayBinding) rightType).elementsType(), TypeBinding.CHAR)) {
				scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(this.right);
			}
		}
		if (rightTypeID == TypeIds.T_JavaLangString) {
			this.right.computeConversion(scope, rightType, rightType);
			if (leftType.isArrayType() && TypeBinding.equalsEquals(((ArrayBinding) leftType).elementsType(), TypeBinding.CHAR)) {
				scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(this.left);
			}
		}
	}

	// the code is an int
	// (cast)  left   Op (cast)  right --> result
	//  0000   0000       0000   0000      0000
	//  <<16   <<12       <<8    <<4       <<0

	// Don't test for result = 0. If it is zero, some more work is done.
	// On the one hand when it is not zero (correct code) we avoid doing the test
	int operator = (this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
	int operatorSignature = OperatorExpression.OperatorSignatures[operator][(leftTypeID << 4) + rightTypeID];

	this.left.computeConversion(scope, 	TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), leftType);
	this.right.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 8) & 0x0000F), rightType);
	this.bits |= operatorSignature & 0xF;
	switch (operatorSignature & 0xF) { // record the current ReturnTypeID
		// only switch on possible result type.....
		case T_boolean :
			this.resolvedType = TypeBinding.BOOLEAN;
			break;
		case T_byte :
			this.resolvedType = TypeBinding.BYTE;
			break;
		case T_char :
			this.resolvedType = TypeBinding.CHAR;
			break;
		case T_double :
			this.resolvedType = TypeBinding.DOUBLE;
			break;
		case T_float :
			this.resolvedType = TypeBinding.FLOAT;
			break;
		case T_int :
			this.resolvedType = TypeBinding.INT;
			break;
		case T_long :
			this.resolvedType = TypeBinding.LONG;
			break;
		case T_JavaLangString :
			this.resolvedType = scope.getJavaLangString();
			break;
		default : //error........
			this.constant = Constant.NotAConstant;
			scope.problemReporter().invalidOperator(this, leftType, rightType);
			return;
	}

	// check need for operand cast
	if ((leftIsCast = (this.left instanceof CastExpression)) == true ||
			rightIsCast) {
		CastExpression.checkNeedForArgumentCasts(scope, operator, operatorSignature, this.left, leftTypeID, leftIsCast, this.right, rightTypeID, rightIsCast);
	}
	// compute the constant when valid
	computeConstant(scope, leftTypeID, rightTypeID);
}

public void optimizedBooleanConstant(int leftId, int operator, int rightId) {
	switch (operator) {
		case AND :
			if ((leftId != TypeIds.T_boolean) || (rightId != TypeIds.T_boolean))
				return;
			//$FALL-THROUGH$
		case AND_AND :
			Constant cst;
			if ((cst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
				if (cst.booleanValue() == false) { // left is equivalent to false
					this.optimizedBooleanConstant = cst; // constant(false)
					return;
				} else { //left is equivalent to true
					if ((cst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
						this.optimizedBooleanConstant = cst;
						// the conditional result is equivalent to the right conditional value
					}
					return;
				}
			}
			if ((cst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
				if (cst.booleanValue() == false) { // right is equivalent to false
					this.optimizedBooleanConstant = cst; // constant(false)
				}
			}
			return;
		case OR :
			if ((leftId != TypeIds.T_boolean) || (rightId != TypeIds.T_boolean))
				return;
			//$FALL-THROUGH$
		case OR_OR :
			if ((cst = this.left.optimizedBooleanConstant()) != Constant.NotAConstant) {
				if (cst.booleanValue() == true) { // left is equivalent to true
					this.optimizedBooleanConstant = cst; // constant(true)
					return;
				} else { //left is equivalent to false
					if ((cst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
						this.optimizedBooleanConstant = cst;
					}
					return;
				}
			}
			if ((cst = this.right.optimizedBooleanConstant()) != Constant.NotAConstant) {
				if (cst.booleanValue() == true) { // right is equivalent to true
					this.optimizedBooleanConstant = cst; // constant(true)
				}
			}
	}
}

@Override
public StringBuilder printExpressionNoParenthesis(int indent, StringBuilder output) {
	// keep implementation in sync with
	// CombinedBinaryExpression#printExpressionNoParenthesis
	this.left.printExpression(indent, output).append(' ').append(operatorToString()).append(' ');
	return this.right.printExpression(0, output);
}

public String getMethodName() {
	switch ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
		case PLUS:
			return "add"; //$NON-NLS-1$
		case MINUS :
			return "sub"; //$NON-NLS-1$
		case MULTIPLY :
			return "mul"; //$NON-NLS-1$
		case DIVIDE :
			return "div"; //$NON-NLS-1$
		case CAT :
			return "cat"; //$NON-NLS-1$
		case REMAINDER :
			return "mod"; //$NON-NLS-1$
		case AND :
			return "and"; //$NON-NLS-1$
		case OR :
			return "or"; //$NON-NLS-1$
		case XOR :
			return "xor"; //$NON-NLS-1$
		case EQUAL_EQUAL_EQUAL :
			return "eq"; //$NON-NLS-1$
		case NOT_EQUAL_EQUAL :
			return "neq"; //$NON-NLS-1$
		case LEFT_SHIFT:
			return "shiftLeft"; //$NON-NLS-1$
		case RIGHT_SHIFT:
			return "shiftRight"; //$NON-NLS-1$
		case UNSIGNED_RIGHT_SHIFT:
			return "unsignedShiftRight"; //$NON-NLS-1$
		case GREATER:
			return "gt"; //$NON-NLS-1$
		case GREATER_EQUAL:
			return "gte"; //$NON-NLS-1$
		case LESS:
			return "lt"; //$NON-NLS-1$
		case LESS_EQUAL:
			return "lte"; //$NON-NLS-1$
		case EQUAL_EQUAL:
			return "CECINESTPASUNOPERATEUR_eq"; //$NON-NLS-1$
		case NOT_EQUAL:
			return "CECINESTPASUNOPERATEUR_neq";  //$NON-NLS-1$
	}
	return ""; //$NON-NLS-1$
}

class BinaryExpressionSite extends OperatorOverloadInvocationSite {
	protected Expression [] siteArguments = new Expression[1];

	public BinaryExpressionSite(Expression rhs) {
		this.siteArguments[0] = rhs;
	}
	@Override
	public TypeBinding invocationTargetType() {
		return BinaryExpression.this.expectedType();
	}
	@Override
	public Expression[] arguments() {
		return this.siteArguments;
	}
}

public MethodBinding getMethodBindingForOverload(BlockScope scope) {
	TypeBinding tb_right = null;
	TypeBinding tb_left = null;

	if(this.left.resolvedType == null)
		tb_left = this.left.resolveType(scope);
	else
		tb_left = this.left.resolvedType;

	if(this.right.resolvedType == null)
		tb_right = this.right.resolveType(scope);
	else
		tb_right = this.right.resolvedType;

	String ms = getMethodName();

	//Object <op> Object
	if (!tb_left.isBoxedPrimitiveType() && !tb_left.isBaseType() && !tb_right.isBoxedPrimitiveType() && !tb_right.isBaseType()){
		MethodBinding mbLeft = getLeftMethod(scope, ms, tb_left, tb_right);
		MethodBinding mbRight = getRightMethod(scope, ms, tb_left, tb_right);
		if(mbLeft.isValidBinding() && mbRight.isValidBinding()){
			if(mbLeft.isStatic() && mbRight.isStatic()) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
				return null;
			}
			return new ProblemMethodBinding(ms.toCharArray(), new TypeBinding[]{tb_right}, ProblemReasons.Ambiguous);
		}
		if(mbLeft.isValidBinding()){
			if(mbLeft.isStatic()) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
				return null;
			}
			this.overloadedExpresionSide = overloadedLeftSide;
			return mbLeft;
		}
		if(mbRight.isValidBinding()){
			if(mbRight.isStatic()) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
				return null;
			}
			this.overloadedExpresionSide = overloadedRightSide;
			return mbRight;
		}
		return null;
	}


	//Object <op> type or type <op> Object
	if(!tb_left.isBoxedPrimitiveType() && !tb_left.isBaseType() && (tb_right.isBoxedPrimitiveType() || tb_right.isBaseType())){
		MethodBinding mbLeft = getLeftMethod(scope, ms, tb_left, tb_right);
		if(mbLeft.isValidBinding() && isAnnotationSet(mbLeft)){
			if(mbLeft.isStatic()) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
				return null;
			}
			this.overloadedExpresionSide = overloadedLeftSide;
			return mbLeft;
		}
		return null;
	}
	if(!tb_right.isBoxedPrimitiveType() && !tb_right.isBaseType() && (tb_left.isBoxedPrimitiveType() || tb_left.isBaseType())){
		MethodBinding mbRight = getRightMethod(scope, ms, tb_left, tb_right);
		if(mbRight.isValidBinding()){
			if(mbRight.isStatic()) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodName());
				return null;
			}
			this.overloadedExpresionSide = overloadedRightSide;
			return mbRight;
		}
		return null;
	}
	return null;
}

	public boolean isAnnotationSet(MethodBinding method){
/*		if(!method.isValidBinding())
			return false;
		AnnotationBinding[] annotations = method.declaringClass.getAnnotations();
		if( annotations == null)
			return false;
		for(int i = 0; i < annotations.length; i++){
			AnnotationBinding annotation = annotations[i];
			if(Arrays.equals(annotation.getAnnotationType().sourceName, "EnableOperatorOverloading".toCharArray())) //$NON-NLS-1$
				return true;
		}

		return false;*/
		return true;
	}

public void boxConvert(Expression e, BlockScope currentScope, CodeStream codeStream) {
	if (e.resolvedType.isBaseType()) {
		e.computeConversion(currentScope, currentScope.boxing(e.resolvedType), e.resolvedType);
		codeStream.generateBoxingConversion(e.resolvedType.id);
	}
}

public void generateOperatorOverloadCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	if(this.overloadedExpresionSide == overloadedLeftSide){
		this.left.generateCode(currentScope, codeStream,true);
		this.right.generateCode(currentScope, codeStream, true);
	}else{
		this.right.generateCode(currentScope, codeStream, true);
		this.left.generateCode(currentScope, codeStream,true);
	}
	if (this.appropriateMethodForOverload.hasSubstitutedParameters() || this.appropriateMethodForOverload.hasSubstitutedReturnType()) {
		TypeBinding tbo = this.appropriateMethodForOverload.returnType;
		MethodBinding mb3 = this.appropriateMethodForOverload.original();
		MethodBinding final_mb = mb3;
		// TODO remove for real?
		//final_mb.returnType = final_mb.returnType.erasure();
		codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());

		if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
			codeStream.checkcast(tbo);
		}
	} else {
		MethodBinding original = this.appropriateMethodForOverload.original();
		if(original.isPrivate()){
			codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticAccessor, null /* default declaringClass */);
		}
		else{
			codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
		}
		if (!this.appropriateMethodForOverload.returnType.isBaseType()) codeStream.checkcast(this.appropriateMethodForOverload.returnType);
	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	}
}

@Override
public TypeBinding resolveType(BlockScope scope) {
	// keep implementation in sync with CombinedBinaryExpression#resolveType
	// and nonRecursiveResolveTypeUpwards
	boolean leftIsCast, rightIsCast;
	if ((leftIsCast = this.left instanceof CastExpression) == true) this.left.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
	TypeBinding leftType = this.left.resolveType(scope);

	if ((rightIsCast = this.right instanceof CastExpression) == true) this.right.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on

	LocalVariableBinding [] patternVars = switch ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
		case AND_AND -> this.left.bindingsWhenTrue();
		case OR_OR   -> this.left.bindingsWhenFalse();
		default      -> NO_VARIABLES;
	};

	TypeBinding rightType = this.right.resolveTypeWithBindings(patternVars, scope);

	/*
	 * 6.3.1 Scope for Pattern Variables in Expressions
	 * 6.3.1.1 Conditional-And Operator &&
	 *
	 * It is a compile-time error if any of the following conditions hold:
		• A pattern variable is both (i) introduced by a when true and (ii) introduced by
		b when true.
		• A pattern variable is both (i) introduced by a when false and (ii) introduced by
		b when false.

	    ...

	 * 6.3.1.2 Conditional-Or Operator ||
	 *
	 * It is a compile-time error if any of the following conditions hold:
		• A pattern variable is both (i) introduced by a when true and (ii) introduced by
		b when true.
		• A pattern variable is both (i) introduced by a when false and (ii) introduced by
		b when false.
	 */

	// We handle only the cases NOT already diagnosed in due course to avoid double jeopardy
	switch ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
		case AND_AND -> {
			scope.reportClashingDeclarations(this.left.bindingsWhenFalse(), this.right.bindingsWhenFalse());
		}
		case OR_OR -> {
			scope.reportClashingDeclarations(this.left.bindingsWhenTrue(), this.right.bindingsWhenTrue());
		}
	}

	// use the id of the type to navigate into the table
	if (leftType == null || rightType == null) {
		this.constant = Constant.NotAConstant;
		return null;
	}

	//if L is object or R is object
	if((!leftType.isBoxedPrimitiveType() && !leftType.isBaseType() && leftType.id != T_JavaLangString) || (!rightType.isBoxedPrimitiveType() && !rightType.isBaseType() && rightType.id != T_JavaLangString)){
		MethodBinding overloadMethod = this.getMethodBindingForOverload(scope);
		//if overloaded method is OK continue
		if(overloadMethod != null && overloadMethod.isValidBinding()){
			this.appropriateMethodForOverload = overloadMethod;
			if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
				scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
			// Object <op> Object just return returnType
			if((!leftType.isBoxedPrimitiveType() && !leftType.isBaseType() && leftType.id != T_JavaLangString) && (!rightType.isBoxedPrimitiveType() && !rightType.isBaseType() && rightType.id != T_JavaLangString)){
				this.constant = Constant.NotAConstant;
				this.isCompactable = false;
				this.left.computeConversion(scope, leftType, leftType);
				this.right.computeConversion(scope, rightType, rightType);
				return this.resolvedType = overloadMethod.returnType;
			}
			if(this.overloadedExpresionSide == overloadedLeftSide){
				leftType = overloadMethod.parameters[0];
			}else if(this.overloadedExpresionSide == overloadedRightSide){
				rightType = overloadMethod.parameters[0];
			}else{
				this.constant = Constant.NotAConstant;
				scope.problemReporter().invalidOperator(this, this.left.resolvedType, this.right.resolvedType);
				return null;
			}

		}else{
			//Left is object and right isn't string or Right is object and left isn't string
			if((!leftType.isBoxedPrimitiveType() && !leftType.isBaseType() && leftType.id != T_JavaLangString && rightType.id != T_JavaLangString) ||
					(!rightType.isBoxedPrimitiveType() && !rightType.isBaseType() && rightType.id != T_JavaLangString && leftType.id != T_JavaLangString)){
				//Error, return appropriate error text
				if(overloadMethod instanceof ProblemMethodBinding && overloadMethod.problemId() == ProblemReasons.Ambiguous){
						scope.problemReporter().ambiguousOperator(this, leftType, rightType);
						this.constant = Constant.NotAConstant;
						return null;
				}else{
					this.constant = Constant.NotAConstant;
					scope.problemReporter().invalidOperator(this, this.left.resolvedType, this.right.resolvedType);
					return null;
				}
			}
		}
	}

	if(((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.CAT && this.appropriateMethodForOverload == null) {
		this.constant = Constant.NotAConstant;
		scope.problemReporter().invalidOperator(this, this.left.resolvedType, this.right.resolvedType);
		return null;
	}

	if((((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.EQUAL_EQUAL_EQUAL
			|| ((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.NOT_EQUAL_EQUAL)
			&& this.appropriateMethodForOverload == null){
		this.constant = Constant.NotAConstant;
		scope.problemReporter().invalidOperator(this, this.left.resolvedType, this.right.resolvedType);
		return null;
	}


	int leftTypeID = leftType.id;
	int rightTypeID = rightType.id;

	// autoboxing support
	boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
	if (use15specifics) {
		if (!leftType.isBaseType() && rightTypeID != TypeIds.T_JavaLangString && rightTypeID != TypeIds.T_null) {
			leftTypeID = scope.environment().computeBoxingType(leftType).id;
		}
		if (!rightType.isBaseType() && leftTypeID != TypeIds.T_JavaLangString && leftTypeID != TypeIds.T_null) {
			rightTypeID = scope.environment().computeBoxingType(rightType).id;
		}
	}
	if (leftTypeID > 15
		|| rightTypeID > 15) { // must convert String + Object || Object + String
		if (leftTypeID == TypeIds.T_JavaLangString) {
			rightTypeID = TypeIds.T_JavaLangObject;
		} else if (rightTypeID == TypeIds.T_JavaLangString) {
			leftTypeID = TypeIds.T_JavaLangObject;
		} else {
			rightTypeID = TypeIds.T_JavaLangObject;
			leftTypeID = TypeIds.T_JavaLangObject;
			/*
			this.constant = Constant.NotAConstant;
			scope.problemReporter().invalidOperator(this, leftType, rightType);
			return null;*/
		}
	}
	if (((this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.PLUS) {
		if (leftTypeID == TypeIds.T_JavaLangString) {
			this.left.computeConversion(scope, leftType, leftType);
			if (rightType.isArrayType() && TypeBinding.equalsEquals(((ArrayBinding) rightType).elementsType(), TypeBinding.CHAR)) {
				scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(this.right);
			}
		}
		if (rightTypeID == TypeIds.T_JavaLangString) {
			this.right.computeConversion(scope, rightType, rightType);
			if (leftType.isArrayType() && TypeBinding.equalsEquals(((ArrayBinding) leftType).elementsType(), TypeBinding.CHAR)) {
				scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(this.left);
			}
		}
	}

	// the code is an int
	// (cast)  left   Op (cast)  right --> result
	//  0000   0000       0000   0000      0000
	//  <<16   <<12       <<8    <<4       <<0

	// Don't test for result = 0. If it is zero, some more work is done.
	// On the one hand when it is not zero (correct code) we avoid doing the test
	int operator = (this.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
	int operatorSignature = OperatorExpression.OperatorSignatures[operator][(leftTypeID << 4) + rightTypeID];

	if (this.appropriateMethodForOverload == null) {
		this.left.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), leftType);
		this.right.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 8) & 0x0000F), rightType);
		this.bits |= operatorSignature & 0xF;
		switch (operatorSignature & 0xF) { // record the current ReturnTypeID
			// only switch on possible result type.....
			case T_boolean :
				this.resolvedType = TypeBinding.BOOLEAN;
				break;
			case T_byte :
				this.resolvedType = TypeBinding.BYTE;
				break;
			case T_char :
				this.resolvedType = TypeBinding.CHAR;
				break;
			case T_double :
				this.resolvedType = TypeBinding.DOUBLE;
				break;
			case T_float :
				this.resolvedType = TypeBinding.FLOAT;
				break;
			case T_int :
				this.resolvedType = TypeBinding.INT;
				break;
			case T_long :
				this.resolvedType = TypeBinding.LONG;
				break;
			case T_JavaLangString :
				this.resolvedType = scope.getJavaLangString();
				break;
			default : //error........
				boolean allow = this.appropriateMethodForOverload != null &&  this.appropriateMethodForOverload.isValidBinding();

				if (!allow) {
					this.constant = Constant.NotAConstant;
					scope.problemReporter().invalidOperator(this, leftType, rightType);
					return null;
				}
				this.isCompactable = false;
				this.resolvedType =  this.appropriateMethodForOverload.returnType;
				break;
		}
	} else {
		if (this.overloadedExpresionSide == overloadedLeftSide) {
			this.left.implicitConversion = 0;
			this.left.computeConversion(scope, this.left.resolvedType, this.left.resolvedType);
			this.right.computeConversion(scope, this.appropriateMethodForOverload.parameters[0], this.right.resolvedType);
			this.isCompactable = false;
			this.resolvedType = this.appropriateMethodForOverload.returnType;
		} else if (this.overloadedExpresionSide == overloadedRightSide) {
			this.right.implicitConversion = 0;
			this.right.computeConversion(scope, this.right.resolvedType, this.right.resolvedType);
			this.left.computeConversion(scope, this.appropriateMethodForOverload.parameters[0], this.left.resolvedType);
			this.isCompactable = false;
			this.resolvedType = this.appropriateMethodForOverload.returnType;
		} else {
			this.constant = Constant.NotAConstant;
			scope.problemReporter().invalidOperator(this, leftType, rightType);
			return null;
		}
	}

	// check need for operand cast
	if (leftIsCast || rightIsCast) {
		CastExpression.checkNeedForArgumentCasts(scope, operator, operatorSignature, this.left, leftTypeID, leftIsCast, this.right, rightTypeID, rightIsCast);
	}
	// compute the constant when valid
	computeConstant(scope, leftTypeID, rightTypeID);
	return this.resolvedType;
}

@Override
public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		this.left.traverse(visitor, scope);
		this.right.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}

protected MethodBinding getLeftMethod(BlockScope scope, String ms, TypeBinding leftType, TypeBinding rightType) {
	return scope.getMethod(leftType, ms.toCharArray(), new TypeBinding[]{rightType}, new BinaryExpressionSite(this.right));
}

protected MethodBinding getRightMethod(BlockScope scope, String ms, TypeBinding leftType, TypeBinding rightType) {
	return scope.getMethod(rightType, (ms + "AsRHS").toCharArray(), new TypeBinding[]{leftType}, new BinaryExpressionSite(this.left)); //$NON-NLS-1$
}
}
