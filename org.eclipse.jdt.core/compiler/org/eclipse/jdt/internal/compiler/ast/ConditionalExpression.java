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

public class ConditionalExpression extends OperatorExpression {

	public Expression condition, valueIfTrue, valueIfFalse;
	public Constant optimizedBooleanConstant;
	public Constant optimizedIfTrueConstant;
	public Constant optimizedIfFalseConstant;
	
	private int returnTypeSlotSize = 1;

	// for local variables table attributes
	int trueInitStateIndex = -1;
	int falseInitStateIndex = -1;
	int mergedInitStateIndex = -1;
	
	public ConditionalExpression(
		Expression condition,
		Expression valueIfTrue,
		Expression valueIfFalse) {
		this.condition = condition;
		this.valueIfTrue = valueIfTrue;
		this.valueIfFalse = valueIfFalse;
		sourceStart = condition.sourceStart;
		sourceEnd = valueIfFalse.sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		Constant cst = this.condition.optimizedBooleanConstant();
		boolean isConditionOptimizedTrue = cst != NotAConstant && cst.booleanValue() == true;
		boolean isConditionOptimizedFalse = cst != NotAConstant && cst.booleanValue() == false;

		int mode = flowInfo.reachMode();
		flowInfo = condition.analyseCode(currentScope, flowContext, flowInfo, cst == NotAConstant);
		
		// process the if-true part
		FlowInfo trueFlowInfo = flowInfo.initsWhenTrue().copy();
		if (isConditionOptimizedFalse) {
			trueFlowInfo.setReachMode(FlowInfo.UNREACHABLE); 
		}
		trueInitStateIndex = currentScope.methodScope().recordInitializationStates(trueFlowInfo);
		trueFlowInfo = valueIfTrue.analyseCode(currentScope, flowContext, trueFlowInfo);

		// process the if-false part
		FlowInfo falseFlowInfo = flowInfo.initsWhenFalse().copy();
		if (isConditionOptimizedTrue) {
			falseFlowInfo.setReachMode(FlowInfo.UNREACHABLE); 
		}
		falseInitStateIndex = currentScope.methodScope().recordInitializationStates(falseFlowInfo);
		falseFlowInfo = valueIfFalse.analyseCode(currentScope, flowContext, falseFlowInfo);

		// merge if-true & if-false initializations
		FlowInfo mergedInfo;
		if (isConditionOptimizedTrue){
			mergedInfo = trueFlowInfo.addPotentialInitializationsFrom(falseFlowInfo);
		} else if (isConditionOptimizedFalse) {
			mergedInfo = falseFlowInfo.addPotentialInitializationsFrom(trueFlowInfo);
		} else {
			// merge using a conditional info -  1GK2BLM
			// if ((t && (v = t)) ? t : t && (v = f)) r = v;  -- ok
			cst = this.optimizedIfTrueConstant;
			boolean isValueIfTrueOptimizedTrue = cst != null && cst != NotAConstant && cst.booleanValue() == true;
			boolean isValueIfTrueOptimizedFalse = cst != null && cst != NotAConstant && cst.booleanValue() == false;
			
			cst = this.optimizedIfFalseConstant;
			boolean isValueIfFalseOptimizedTrue = cst != null && cst != NotAConstant && cst.booleanValue() == true;
			boolean isValueIfFalseOptimizedFalse = cst != null && cst != NotAConstant && cst.booleanValue() == false;

			UnconditionalFlowInfo trueInfoWhenTrue = trueFlowInfo.initsWhenTrue().copy().unconditionalInits();
			if (isValueIfTrueOptimizedFalse) trueInfoWhenTrue.setReachMode(FlowInfo.UNREACHABLE); 

			UnconditionalFlowInfo falseInfoWhenTrue = falseFlowInfo.initsWhenTrue().copy().unconditionalInits();
			if (isValueIfFalseOptimizedFalse) falseInfoWhenTrue.setReachMode(FlowInfo.UNREACHABLE); 
			
			UnconditionalFlowInfo trueInfoWhenFalse = trueFlowInfo.initsWhenFalse().copy().unconditionalInits();
			if (isValueIfTrueOptimizedTrue) trueInfoWhenFalse.setReachMode(FlowInfo.UNREACHABLE); 

			UnconditionalFlowInfo falseInfoWhenFalse = falseFlowInfo.initsWhenFalse().copy().unconditionalInits();
			if (isValueIfFalseOptimizedTrue) falseInfoWhenFalse.setReachMode(FlowInfo.UNREACHABLE); 

			mergedInfo =
				FlowInfo.conditional(
					trueInfoWhenTrue.mergedWith(falseInfoWhenTrue),
					trueInfoWhenFalse.mergedWith(falseInfoWhenFalse));
		}
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		mergedInfo.setReachMode(mode);
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

		int pc = codeStream.position;
		Label endifLabel, falseLabel;
		if (constant != NotAConstant) {
			if (valueRequired)
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		Constant cst = condition.constant;
		Constant condCst = condition.optimizedBooleanConstant();
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

		if (trueInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				trueInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, trueInitStateIndex);
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
			if (falseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					falseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
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
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Optimized boolean code generation for the conditional operator ?:
	*/
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean) // constant
			|| (valueIfTrue.implicitConversion >> 4) != T_boolean) { // non boolean values
			super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			return;
		}
		Constant cst = condition.constant;
		Constant condCst = condition.optimizedBooleanConstant();
		boolean needTruePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == false))
				|| ((condCst != NotAConstant) && (condCst.booleanValue() == false)));
		boolean needFalsePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == true))
				|| ((condCst != NotAConstant) && (condCst.booleanValue() == true)));

		Label internalFalseLabel, endifLabel = new Label(codeStream);

		// Generate code for the condition
		boolean needConditionValue = (cst == NotAConstant) && (condCst == NotAConstant);
		condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				null,
				internalFalseLabel = new Label(codeStream),
				needConditionValue);

		if (trueInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				trueInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, trueInitStateIndex);
		}
		// Then code generation
		if (needTruePart) {
			valueIfTrue.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			
			if (needFalsePart) {
				// Jump over the else part
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				codeStream.updateLastRecordedEndPC(position);
				// No need to decrement codestream stack size
				// since valueIfTrue was already consumed by branch bytecode
			}
		}
		if (needFalsePart) {
			internalFalseLabel.place();
			if (falseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					falseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
			}
			valueIfFalse.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);

			// End of if statement
			endifLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		// no implicit conversion for boolean values
		codeStream.updateLastRecordedEndPC(codeStream.position);
	}

	public Constant optimizedBooleanConstant() {

		return this.optimizedBooleanConstant == null ? this.constant : this.optimizedBooleanConstant;
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		// specs p.368
		constant = NotAConstant;
		TypeBinding conditionType = condition.resolveTypeExpecting(scope, BooleanBinding);
		TypeBinding valueIfTrueType = valueIfTrue.resolveType(scope);
		TypeBinding valueIfFalseType = valueIfFalse.resolveType(scope);
		if (conditionType == null || valueIfTrueType == null || valueIfFalseType == null)
			return null;

		// Propagate the constant value from the valueIfTrue and valueIFFalse expression if it is possible
		Constant condConstant, trueConstant, falseConstant;
		if ((condConstant = condition.constant) != NotAConstant
			&& (trueConstant = valueIfTrue.constant) != NotAConstant
			&& (falseConstant = valueIfFalse.constant) != NotAConstant) {
			// all terms are constant expression so we can propagate the constant
			// from valueIFTrue or valueIfFalse to teh receiver constant
			constant = condConstant.booleanValue() ? trueConstant : falseConstant;
		}
		if (valueIfTrueType == valueIfFalseType) { // harmed the implicit conversion 
			valueIfTrue.implicitWidening(valueIfTrueType, valueIfTrueType);
			valueIfFalse.implicitConversion = valueIfTrue.implicitConversion;
			if (valueIfTrueType == LongBinding || valueIfTrueType == DoubleBinding) {
				returnTypeSlotSize = 2;
			}

			if (valueIfTrueType == BooleanBinding) {
				this.optimizedIfTrueConstant = valueIfTrue.optimizedBooleanConstant();
				this.optimizedIfFalseConstant = valueIfFalse.optimizedBooleanConstant();
			
				// Propagate the optimized boolean constant if possible
				if ((condConstant = condition.optimizedBooleanConstant()) != NotAConstant) {
					
					this.optimizedBooleanConstant = condConstant.booleanValue()
						? optimizedIfTrueConstant
						: optimizedIfFalseConstant;
				}
			}
			return this.resolvedType = valueIfTrueType;
		}
		// Determine the return type depending on argument types
		// Numeric types
		if (valueIfTrueType.isNumericType() && valueIfFalseType.isNumericType()) {
			// (Short x Byte) or (Byte x Short)"
			if ((valueIfTrueType == ByteBinding && valueIfFalseType == ShortBinding)
				|| (valueIfTrueType == ShortBinding && valueIfFalseType == ByteBinding)) {
				valueIfTrue.implicitWidening(ShortBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(ShortBinding, valueIfFalseType);
				this.resolvedType = ShortBinding;
				return ShortBinding;
			}
			// <Byte|Short|Char> x constant(Int)  ---> <Byte|Short|Char>   and reciprocally
			if ((valueIfTrueType == ByteBinding || valueIfTrueType == ShortBinding || valueIfTrueType == CharBinding)
				&& (valueIfFalseType == IntBinding
					&& valueIfFalse.isConstantValueOfTypeAssignableToType(valueIfFalseType, valueIfTrueType))) {
				valueIfTrue.implicitWidening(valueIfTrueType, valueIfTrueType);
				valueIfFalse.implicitWidening(valueIfTrueType, valueIfFalseType);
				this.resolvedType = valueIfTrueType;
				return valueIfTrueType;
			}
			if ((valueIfFalseType == ByteBinding
				|| valueIfFalseType == ShortBinding
				|| valueIfFalseType == CharBinding)
				&& (valueIfTrueType == IntBinding
					&& valueIfTrue.isConstantValueOfTypeAssignableToType(valueIfTrueType, valueIfFalseType))) {
				valueIfTrue.implicitWidening(valueIfFalseType, valueIfTrueType);
				valueIfFalse.implicitWidening(valueIfFalseType, valueIfFalseType);
				this.resolvedType = valueIfFalseType;
				return valueIfFalseType;
			}
			// Manual binary numeric promotion
			// int
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_int)
				&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_int)) {
				valueIfTrue.implicitWidening(IntBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(IntBinding, valueIfFalseType);
				this.resolvedType = IntBinding;
				return IntBinding;
			}
			// long
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_long)
				&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_long)) {
				valueIfTrue.implicitWidening(LongBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(LongBinding, valueIfFalseType);
				returnTypeSlotSize = 2;
				this.resolvedType = LongBinding;
				return LongBinding;
			}
			// float
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_float)
				&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_float)) {
				valueIfTrue.implicitWidening(FloatBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(FloatBinding, valueIfFalseType);
				this.resolvedType = FloatBinding;
				return FloatBinding;
			}
			// double
			valueIfTrue.implicitWidening(DoubleBinding, valueIfTrueType);
			valueIfFalse.implicitWidening(DoubleBinding, valueIfFalseType);
			returnTypeSlotSize = 2;
			this.resolvedType = DoubleBinding;
			return DoubleBinding;
		}
		// Type references (null null is already tested)
		if ((valueIfTrueType.isBaseType() && valueIfTrueType != NullBinding)
			|| (valueIfFalseType.isBaseType() && valueIfFalseType != NullBinding)) {
			scope.problemReporter().conditionalArgumentsIncompatibleTypes(
				this,
				valueIfTrueType,
				valueIfFalseType);
			return null;
		}
		if (valueIfFalseType.isCompatibleWith(valueIfTrueType)) {
			valueIfTrue.implicitWidening(valueIfTrueType, valueIfTrueType);
			valueIfFalse.implicitWidening(valueIfTrueType, valueIfFalseType);
			this.resolvedType = valueIfTrueType;
			return valueIfTrueType;
		}
		if (valueIfTrueType.isCompatibleWith(valueIfFalseType)) {
			valueIfTrue.implicitWidening(valueIfFalseType, valueIfTrueType);
			valueIfFalse.implicitWidening(valueIfFalseType, valueIfFalseType);
			this.resolvedType = valueIfFalseType;
			return valueIfFalseType;
		}
		scope.problemReporter().conditionalArgumentsIncompatibleTypes(
			this,
			valueIfTrueType,
			valueIfFalseType);
		return null;
	}
	
	public String toStringExpressionNoParenthesis() {
		return condition.toStringExpression() + " ? " + //$NON-NLS-1$
		valueIfTrue.toStringExpression() + " : " + //$NON-NLS-1$
		valueIfFalse.toStringExpression();
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			condition.traverse(visitor, scope);
			valueIfTrue.traverse(visitor, scope);
			valueIfFalse.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
