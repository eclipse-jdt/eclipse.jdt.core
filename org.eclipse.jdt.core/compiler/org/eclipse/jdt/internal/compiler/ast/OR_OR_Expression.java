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

public class OR_OR_Expression extends BinaryExpression {
	//dedicated treatment for the &&
	int rightInitStateIndex = -1;
	int mergedInitStateIndex = -1;

public OR_OR_Expression(Expression left, Expression right,int operator) {
	super(left,right,operator);
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	if (left.constant != NotAConstant) {
		if (!left.constant.booleanValue()) { // false || anything
			// in this case only, we are sure that local inits performed in 
			// the argument <anything> will be executed even though it was 
			// a conditional operation
			return right.analyseCode(currentScope, flowContext, flowInfo);
		} else { // true || anything
			// in this case only, we are sure that local inits performed in 
			// the argument <anything> will *not* be executed even though it 
			// was a conditional operation
			return flowInfo;
		}
	}
	if (right.constant != NotAConstant) {
		if (right.constant.booleanValue()) { // anything || true
			// whatever is on the left, we will succeed, so the result must merge the left inits when answering
			// initsWhenTrue.
			// the initsWhenFalse are undetermined, since this path will be fake reachable...
			FlowInfo mergedInfo = left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
			mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;			
		} else { // anything || false
			// ignore the right part
			FlowInfo mergedInfo = left.analyseCode(currentScope, flowContext, flowInfo);
			mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;			
		}
	}
	
	if (right.constant != NotAConstant) {
		// anything && true/false
		// in this case only, we are sure that local inits performed in 
		// the argument <anything> will be executed even though it was 
		// a conditional operation
		FlowInfo mergedInfo = left.analyseCode(currentScope, flowContext, flowInfo);
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	FlowInfo leftInfo, rightInfo;	leftInfo = left.analyseCode(currentScope, flowContext, flowInfo);
	rightInfo = leftInfo.initsWhenFalse().copy();
	rightInitStateIndex = currentScope.methodScope().recordInitializationStates(rightInfo);
	rightInfo = right.analyseCode(currentScope, flowContext, rightInfo);
	FlowInfo mergedInfo = FlowInfo.conditional(
		// merging two true initInfos for such a negative case: if ((t && (b = t)) || f) r = b; // b may not have been initialized
		leftInfo.initsWhenTrue().unconditionalInits().mergedWith(rightInfo.initsWhenTrue().copy().unconditionalInits()), 
		rightInfo.initsWhenFalse());
	mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}
public void computeConstant(BlockScope scope, int leftId, int rightId) {
	//the TC has been done so leftId and rightId are both equal to T_boolean

	Constant cst;
	if ((cst = left.constant) != NotAConstant) {
		if (cst.booleanValue() == true) { // true || x --> true
			constant = cst; // inlined to constant(false)
		} else { // false || x --> x
			if ((constant = right.constant) == NotAConstant) {
				// compute conditionalConstant
				optimizedBooleanConstant(leftId, (bits & OperatorMASK) >> OperatorSHIFT, rightId);
			}
		}
	} else {
		constant = NotAConstant;
		// compute conditionalConstant
		optimizedBooleanConstant(leftId, (bits & OperatorMASK) >> OperatorSHIFT, rightId);
	}
}
/**
 * Code generation for a binary operation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	Label falseLabel, endLabel;
	if (constant != Constant.NotAConstant) {
		if (valueRequired)
			codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this);
		return;
	}
	bits |= OnlyValueRequiredMASK;
	generateOptimizedConditionalOr(currentScope, codeStream, null, (falseLabel = new Label(codeStream)), valueRequired);
	/*  improving code gen for such a case:		boolean b = i < 0 || true; 
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
	codeStream.recordPositionsFrom(pc, this);
}
/**
 * Boolean operator code generation
 *	Optimized operations are: ||
 */
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean)) {
		int pc = codeStream.position;
		if (constant.booleanValue() == true) {
			// constant == true
			if (valueRequired) {
				if (falseLabel == null) {
					// implicit falling through the FALSE case
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
			}
		} else {
			if (valueRequired) {
				if (falseLabel != null) {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.goto_(falseLabel);
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this);
		return;
	}
	generateOptimizedConditionalOr(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
}
/**
 * Boolean generation for ||
 */
public void generateOptimizedConditionalOr(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	int pc = codeStream.position;
	Constant condConst;
	if ((condConst = left.conditionalConstant()) != NotAConstant) {
		if (condConst.booleanValue() == true) {
			// <something equivalent to true> || x
			left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, false);
			if (valueRequired) {
				if ((bits & OnlyValueRequiredMASK) != 0) {
					codeStream.iconst_1();
				} else {
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
			}
		} else {
			// <something equivalent to false> || x
			left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, false);
			if (rightInitStateIndex != -1){
				codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);				
			}
			if ((bits & OnlyValueRequiredMASK) != 0){
				right.generateCode(currentScope, codeStream, valueRequired);
			} else {
				right.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			}
		}
		codeStream.recordPositionsFrom(pc, this);
		if (mergedInitStateIndex != -1){
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		return;
	}
	if ((condConst = right.conditionalConstant()) != NotAConstant) {
		if (condConst.booleanValue() == true) {
			// x || <something equivalent to true>
			left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, false);
			if (rightInitStateIndex != -1){
				codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);				
			}
			right.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, false);
			if (valueRequired) {
				if ((bits & OnlyValueRequiredMASK) != 0) {
					codeStream.iconst_1();
				} else {
					if (trueLabel != null) {
						codeStream.goto_(trueLabel);
					}
				}
			}
		} else {
			// x || <something equivalent to false>
			if ((bits & OnlyValueRequiredMASK) != 0){
				left.generateCode(currentScope, codeStream, valueRequired);
			} else {
				left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			}
			if (rightInitStateIndex != -1){
				codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);				
			}
			right.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, false);
		}
		codeStream.recordPositionsFrom(pc, this);
		if (mergedInitStateIndex != -1){
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		return;
	}
	// default case
	if (falseLabel == null) {
		if (trueLabel != null) {
			// implicit falling through the FALSE case
			left.generateOptimizedBoolean(currentScope, codeStream, trueLabel, null, true);
			right.generateOptimizedBoolean(currentScope, codeStream, trueLabel, null, valueRequired);
		}
	} else {
		// implicit falling through the TRUE case
		if (trueLabel == null) {
			Label internalTrueLabel = new Label(codeStream);
			left.generateOptimizedBoolean(currentScope, codeStream, internalTrueLabel, null, true);
			if (rightInitStateIndex != -1){
				codeStream.addDefinitelyAssignedVariables(currentScope, rightInitStateIndex);				
			}
			right.generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, valueRequired);
			internalTrueLabel.place();
		} else {
			// no implicit fall through TRUE/FALSE --> should never occur
		}
	}
	codeStream.recordPositionsFrom(pc, this);
	if (mergedInitStateIndex != -1){
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
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
