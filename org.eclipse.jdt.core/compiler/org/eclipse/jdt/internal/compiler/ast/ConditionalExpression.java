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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ConditionalExpression extends OperatorExpression {

	public Expression condition, valueIfTrue, valueIfFalse;
	public Constant optimizedBooleanConstant;
	public Constant optimizedIfTrueConstant;
	public Constant optimizedIfFalseConstant;
	
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
					codeStream.decrStackSize(this.resolvedType == LongBinding || this.resolvedType == DoubleBinding ? 2 : 1);
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
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
			}
			valueIfFalse.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);

			// End of if statement
			endifLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		// no implicit conversion for boolean values
		codeStream.updateLastRecordedEndPC(codeStream.position);
	}

	public Constant optimizedBooleanConstant() {

		return this.optimizedBooleanConstant == null ? this.constant : this.optimizedBooleanConstant;
	}
	
	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {
		
		condition.printExpression(indent, output).append(" ? "); //$NON-NLS-1$
		valueIfTrue.printExpression(0, output).append(" : "); //$NON-NLS-1$
		return valueIfFalse.printExpression(0, output);
	}

	public TypeBinding resolveType(BlockScope scope) {
		// specs p.368
		constant = NotAConstant;
		TypeBinding conditionType = condition.resolveTypeExpecting(scope, BooleanBinding);
		
		if (valueIfTrue instanceof CastExpression) valueIfTrue.bits |= IgnoreNeedForCastCheckMASK; // will check later on
		TypeBinding valueIfTrueType = valueIfTrue.resolveType(scope);

		if (valueIfFalse instanceof CastExpression) valueIfFalse.bits |= IgnoreNeedForCastCheckMASK; // will check later on
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
			valueIfTrue.computeConversion(scope, valueIfTrueType, valueIfTrueType);
			valueIfFalse.implicitConversion = valueIfTrue.implicitConversion;
			if (valueIfTrueType == BooleanBinding) {
				this.optimizedIfTrueConstant = valueIfTrue.optimizedBooleanConstant();
				this.optimizedIfFalseConstant = valueIfFalse.optimizedBooleanConstant();
				if (this.optimizedIfTrueConstant != NotAConstant 
						&& this.optimizedIfFalseConstant != NotAConstant
						&& this.optimizedIfTrueConstant.booleanValue() == this.optimizedIfFalseConstant.booleanValue()) {
					// a ? true : true  /   a ? false : false
					this.optimizedBooleanConstant = optimizedIfTrueConstant;
				} else if ((condConstant = condition.optimizedBooleanConstant()) != NotAConstant) { // Propagate the optimized boolean constant if possible
					this.optimizedBooleanConstant = condConstant.booleanValue()
						? this.optimizedIfTrueConstant
						: this.optimizedIfFalseConstant;
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
				valueIfTrue.computeConversion(scope, ShortBinding, valueIfTrueType);
				valueIfFalse.computeConversion(scope, ShortBinding, valueIfFalseType);
				return this.resolvedType = ShortBinding;
			}
			// <Byte|Short|Char> x constant(Int)  ---> <Byte|Short|Char>   and reciprocally
			if ((valueIfTrueType == ByteBinding || valueIfTrueType == ShortBinding || valueIfTrueType == CharBinding)
					&& (valueIfFalseType == IntBinding
						&& valueIfFalse.isConstantValueOfTypeAssignableToType(valueIfFalseType, valueIfTrueType))) {
				valueIfTrue.computeConversion(scope, valueIfTrueType, valueIfTrueType);
				valueIfFalse.computeConversion(scope, valueIfTrueType, valueIfFalseType);
				return this.resolvedType = valueIfTrueType;
			}
			if ((valueIfFalseType == ByteBinding
					|| valueIfFalseType == ShortBinding
					|| valueIfFalseType == CharBinding)
					&& (valueIfTrueType == IntBinding
						&& valueIfTrue.isConstantValueOfTypeAssignableToType(valueIfTrueType, valueIfFalseType))) {
				valueIfTrue.computeConversion(scope, valueIfFalseType, valueIfTrueType);
				valueIfFalse.computeConversion(scope, valueIfFalseType, valueIfFalseType);
				return this.resolvedType = valueIfFalseType;
			}
			// Manual binary numeric promotion
			// int
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_int)
					&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_int)) {
				valueIfTrue.computeConversion(scope, IntBinding, valueIfTrueType);
				valueIfFalse.computeConversion(scope, IntBinding, valueIfFalseType);
				return this.resolvedType = IntBinding;
			}
			// long
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_long)
					&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_long)) {
				valueIfTrue.computeConversion(scope, LongBinding, valueIfTrueType);
				valueIfFalse.computeConversion(scope, LongBinding, valueIfFalseType);
				return this.resolvedType = LongBinding;
			}
			// float
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_float)
					&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_float)) {
				valueIfTrue.computeConversion(scope, FloatBinding, valueIfTrueType);
				valueIfFalse.computeConversion(scope, FloatBinding, valueIfFalseType);
				return this.resolvedType = FloatBinding;
			}
			// double
			valueIfTrue.computeConversion(scope, DoubleBinding, valueIfTrueType);
			valueIfFalse.computeConversion(scope, DoubleBinding, valueIfFalseType);
			return this.resolvedType = DoubleBinding;
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
			valueIfTrue.computeConversion(scope, valueIfTrueType, valueIfTrueType);
			valueIfFalse.computeConversion(scope, valueIfTrueType, valueIfFalseType);
			return this.resolvedType = valueIfTrueType;
		}
		if (valueIfTrueType.isCompatibleWith(valueIfFalseType)) {
			valueIfTrue.computeConversion(scope, valueIfFalseType, valueIfTrueType);
			valueIfFalse.computeConversion(scope, valueIfFalseType, valueIfFalseType);
			return this.resolvedType = valueIfFalseType;
		}
		// 1.5 addition: allow most common type 
		if (scope.environment().options.sourceLevel >= ClassFileConstants.JDK1_5) {
			TypeBinding commonType = scope.lowerUpperBound(new TypeBinding[] { valueIfTrueType, valueIfFalseType });
//			TypeBinding commonType = scope.mostSpecificCommonType(new TypeBinding[] { valueIfTrueType, valueIfFalseType });
			if (commonType != null) {
				return this.resolvedType = commonType;
			}
		}
		scope.problemReporter().conditionalArgumentsIncompatibleTypes(
			this,
			valueIfTrueType,
			valueIfFalseType);
		return null;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			condition.traverse(visitor, scope);
			valueIfTrue.traverse(visitor, scope);
			valueIfFalse.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
