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

public class ConditionalExpression extends OperatorExpression {
	public Expression condition, valueIfTrue, valueIfFalse;
	private int returnTypeSlotSize = 1;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;
public ConditionalExpression(Expression condition, Expression valueIfTrue, Expression valueIfFalse) {
	this.condition = condition;
	this.valueIfTrue = valueIfTrue;
	this.valueIfFalse = valueIfFalse;
	sourceStart = condition.sourceStart ;
	sourceEnd = valueIfFalse.sourceEnd;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	Constant inlinedCondition;
	if ((inlinedCondition = condition.constant) != NotAConstant) {
		if (inlinedCondition.booleanValue()) {
			FlowInfo resultInfo = valueIfTrue.analyseCode(currentScope, flowContext, flowInfo);
			// analyse valueIfFalse, but do not take into account any of its infos
			valueIfFalse.analyseCode(currentScope, flowContext, flowInfo.copy());
			mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(resultInfo);
			return resultInfo;
		} else {
			// analyse valueIfTrue, but do not take into account any of its infos			
			valueIfTrue.analyseCode(currentScope, flowContext, flowInfo.copy());
			FlowInfo mergeInfo = valueIfFalse.analyseCode(currentScope, flowContext, flowInfo);
			mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergeInfo);
			return mergeInfo;
		}
	}
	// notice that the receiver investigation is not performed in the previous case, since there is
	// not a chance it is worth trying to check anything on a constant expression.

	flowInfo = condition.analyseCode(currentScope, flowContext, flowInfo);

	// store a copy of the merged info, so as to compute the local variable attributes afterwards
	thenInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo.initsWhenTrue());
	elseInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo.initsWhenFalse());
	FlowInfo mergedInfo = valueIfTrue.analyseCode(
		currentScope,
		flowContext,
		flowInfo.initsWhenTrue().copy()).
			unconditionalInits().
				mergedWith(
					valueIfFalse.analyseCode(
						currentScope,
						flowContext,
						flowInfo.initsWhenFalse().copy()).
							unconditionalInits());
	mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}
/**
 * Code generation for the conditional operator ?:
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
*/
public void generateCode(
	BlockScope currentScope, 
	CodeStream codeStream, 
	boolean valueRequired) {

	/* Reset the selector of the message pattern to use the optimized selectors,
	 * when compiling full Java messages, if compiling macroexpanded controls, then
	 * the selector is supposed correctly positionned.
	 */

	int pc = codeStream.position, divergePC;
	Label endifLabel, falseLabel;
	if (constant != NotAConstant) {
		if (valueRequired)
			codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this);
		return;
	}

	Constant cst = condition.constant;
	Constant condCst = condition.conditionalConstant();
	boolean needTruePart = 
		!(((cst != NotAConstant) && (cst.booleanValue() == false))
			|| ((condCst != NotAConstant) && (condCst.booleanValue() == false))); 
	boolean needFalsePart = 
		!(((cst != NotAConstant) && (cst.booleanValue() == true))
			|| ((condCst != NotAConstant) && (condCst.booleanValue() == true))); 

	endifLabel = new Label(codeStream);

	// Generate code for the condition
	boolean needConditionValue = (cst == NotAConstant) && (condCst == NotAConstant); 
	condition.generateOptimizedBoolean(
		currentScope, 
		codeStream, 
		null, 
		(falseLabel = new Label(codeStream)), 
		needConditionValue); 

	if (thenInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(
			currentScope, 
			thenInitStateIndex); 
		codeStream.addDefinitelyAssignedVariables(currentScope, thenInitStateIndex);
	}

	// Then code generation
	if (needTruePart) {
		valueIfTrue.generateCode(currentScope, codeStream, valueRequired);

		if (needFalsePart) {
			// Jump over the else part
			int position = codeStream.position;
			codeStream.goto_(endifLabel);
			codeStream.updateLastRecordedEndPC(position);
			// Tune codestream stack size
			if (valueRequired) {
				codeStream.decrStackSize(returnTypeSlotSize);
			}
		}

	}
	if (needFalsePart) {
		falseLabel.place();
		if (elseInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope, 
				elseInitStateIndex); 
			codeStream.addDefinitelyAssignedVariables(currentScope, elseInitStateIndex);
		}
		valueIfFalse.generateCode(currentScope, codeStream, valueRequired);

		// End of if statement
		endifLabel.place();
	}
	// May loose some local variable initializations : affecting the local variable attributes
	if (mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(
			currentScope, 
			mergedInitStateIndex); 
	}
	// implicit conversion
	if (valueRequired)
		codeStream.generateImplicitConversion(implicitConversion);
	codeStream.recordPositionsFrom(pc, this);
}
public TypeBinding resolveType(BlockScope scope) {
	// specs p.368
	constant = NotAConstant;
	TypeBinding condTb = condition.resolveTypeExpecting(scope, BooleanBinding);
	TypeBinding trueTb = valueIfTrue.resolveType(scope);
	TypeBinding falseTb = valueIfFalse.resolveType(scope);
	if (condTb == null || trueTb == null || falseTb == null)
		return null;

	// Propagate the constant value from the valueIfTrue and valueIFFalse expression if it is possible
	if (condition.constant != NotAConstant && valueIfTrue.constant != NotAConstant && valueIfFalse.constant != NotAConstant) {
		// all terms are constant expression so we can propagate the constant
		// from valueIFTrue or valueIfFalse to teh receiver constant
		constant = (condition.constant.booleanValue()) ? valueIfTrue.constant : valueIfFalse.constant;
	}
	if (trueTb == falseTb) { // harmed the implicit conversion 
		valueIfTrue.implicitWidening(trueTb, trueTb);
		valueIfFalse.implicitConversion = valueIfTrue.implicitConversion;
		if (trueTb == LongBinding || trueTb == DoubleBinding) {
			returnTypeSlotSize = 2;
		}
		return trueTb;
	}

	// Determine the return type depending on argument types
	// Numeric types
	if (trueTb.isNumericType() && falseTb.isNumericType()) {
		// (Short x Byte) or (Byte x Short)"
		if ((trueTb == ByteBinding && falseTb == ShortBinding) || (trueTb == ShortBinding && falseTb == ByteBinding)) {
			valueIfTrue.implicitWidening(ShortBinding, trueTb);
			valueIfFalse.implicitWidening(ShortBinding, falseTb);
			return ShortBinding;
		}

		// <Byte|Short|Char> x constant(Int)  ---> <Byte|Short|Char>   and reciprocally
		if ((trueTb == ByteBinding || trueTb == ShortBinding || trueTb == CharBinding) &&
			(falseTb == IntBinding && valueIfFalse.isConstantValueOfTypeAssignableToType(falseTb, trueTb))) {
				valueIfTrue.implicitWidening(trueTb, trueTb);
				valueIfFalse.implicitWidening(trueTb, falseTb);
				return trueTb;
		}
		if ((falseTb == ByteBinding || falseTb == ShortBinding || falseTb == CharBinding) &&
			(trueTb == IntBinding && valueIfTrue.isConstantValueOfTypeAssignableToType(trueTb, falseTb))) {
				valueIfTrue.implicitWidening(falseTb, trueTb);
				valueIfFalse.implicitWidening(falseTb, falseTb);
				return falseTb;
		}

		// Manual binary numeric promotion
		// int
		if (BaseTypeBinding.isNarrowing(trueTb.id, T_int) && BaseTypeBinding.isNarrowing(falseTb.id, T_int)) {
			valueIfTrue.implicitWidening(IntBinding, trueTb);
			valueIfFalse.implicitWidening(IntBinding, falseTb);
			return IntBinding;
		}
		// long
		if (BaseTypeBinding.isNarrowing(trueTb.id, T_long) && BaseTypeBinding.isNarrowing(falseTb.id, T_long)) {
			valueIfTrue.implicitWidening(LongBinding, trueTb);
			valueIfFalse.implicitWidening(LongBinding, falseTb);
			returnTypeSlotSize = 2;
			return LongBinding;
		}
		// float
		if (BaseTypeBinding.isNarrowing(trueTb.id, T_float) && BaseTypeBinding.isNarrowing(falseTb.id, T_float)) {
			valueIfTrue.implicitWidening(FloatBinding, trueTb);
			valueIfFalse.implicitWidening(FloatBinding, falseTb);
			return FloatBinding;
		}
		// double
		valueIfTrue.implicitWidening(DoubleBinding, trueTb);
		valueIfFalse.implicitWidening(DoubleBinding, falseTb);
		returnTypeSlotSize = 2;
		return DoubleBinding;
	}

	// Type references (null null is already tested)
	if ((trueTb.isBaseType() && trueTb != NullBinding) || (falseTb.isBaseType() && falseTb != NullBinding)) {
		scope.problemReporter().conditionalArgumentsIncompatibleTypes(this, trueTb, falseTb);
		return null;
	}
	if (scope.areTypesCompatible(falseTb, trueTb)) {
		valueIfTrue.implicitWidening(trueTb, trueTb);
		valueIfFalse.implicitWidening(trueTb, falseTb);
		return trueTb;
	}
	if (scope.areTypesCompatible(trueTb, falseTb)) {
		valueIfTrue.implicitWidening(falseTb, trueTb);
		valueIfFalse.implicitWidening(falseTb, falseTb);
		return falseTb;
	}
	scope.problemReporter().conditionalArgumentsIncompatibleTypes(this, trueTb, falseTb);
	return null;
}
public String toStringExpressionNoParenthesis(){
	/* slow code*/

	return	condition.toStringExpression() + " ? "/*nonNLS*/ +
			valueIfTrue.toStringExpression() + " : "/*nonNLS*/ +
			valueIfFalse.toStringExpression() ; }
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		condition.traverse(visitor, scope);
		valueIfTrue.traverse(visitor, scope);
		valueIfFalse.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
