/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
//import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 *
 * @author milan
 *
 */
public class IF_Statement extends Statement {

	//this class represents the case of only one statement in
	//either else and/or then branches.

	public Expression condition;
	public Statement thenStatement;
	public Statement elseStatement;
	public TypeBinding resolvedType;
	public ThisReference thisReference;

	public MethodBinding ifAppropriateMethodForOverload = null;
	public MethodBinding elseAppropriateMethodForOverload = null;
	public MethodBinding endifAppropriateMethodForOverload = null;

	private MethodBinding ifSyntheticAccessor = null;
	private MethodBinding elseSyntheticAccessor = null;
	private MethodBinding endifSyntheticAccessor = null;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;


public IF_Statement(Expression condition, Statement thenStatement, 	int sourceStart, int sourceEnd, ThisReference thisReference) {
	this.condition = condition;
	this.thenStatement = thenStatement;
	// remember useful empty statement
	if (thenStatement instanceof EmptyStatement) thenStatement.bits |= IsUsefulEmptyStatement;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
	this.thisReference = thisReference;
}

public IF_Statement(Expression condition, Statement thenStatement, Statement elseStatement, int sourceStart, int sourceEnd, ThisReference thisReference) {
	this.condition = condition;
	this.thenStatement = thenStatement;
	// remember useful empty statement
	if (thenStatement instanceof EmptyStatement) thenStatement.bits |= IsUsefulEmptyStatement;
	this.elseStatement = elseStatement;
	if (elseStatement instanceof IfStatement) elseStatement.bits |= IsElseIfStatement;
	if (elseStatement instanceof EmptyStatement) elseStatement.bits |= IsUsefulEmptyStatement;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
	this.thisReference = thisReference;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// process the condition
	FlowInfo conditionFlowInfo = this.condition.analyseCode(currentScope, flowContext, flowInfo);
	int initialComplaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;

	if(this.ifAppropriateMethodForOverload != null){
		MethodBinding original = this.ifAppropriateMethodForOverload.original();
		if(original.isPrivate()){
			this.ifSyntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}

	if(this.elseAppropriateMethodForOverload != null){
		MethodBinding original = this.elseAppropriateMethodForOverload.original();
		if(original.isPrivate()){
			this.elseSyntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}

	if(this.endifAppropriateMethodForOverload != null){
		MethodBinding original = this.endifAppropriateMethodForOverload.original();
		if(original.isPrivate()){
			this.endifSyntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
			currentScope.problemReporter().needToEmulateMethodAccess(original, this);
		}
	}

	//Constant cst = this.condition.optimizedBooleanConstant();

	boolean isConditionOptimizedTrue = false;
	boolean isConditionOptimizedFalse = false;

	// process the THEN part
	FlowInfo thenFlowInfo = conditionFlowInfo.safeInitsWhenTrue();
	if (isConditionOptimizedFalse) {
		thenFlowInfo.setReachMode(FlowInfo.UNREACHABLE);
	}
	FlowInfo elseFlowInfo = conditionFlowInfo.initsWhenFalse();
	if (isConditionOptimizedTrue) {
		elseFlowInfo.setReachMode(FlowInfo.UNREACHABLE);
	}
	if (this.thenStatement != null) {
		// Save info for code gen
		this.thenInitStateIndex = currentScope.methodScope().recordInitializationStates(thenFlowInfo);
		if (isConditionOptimizedFalse) {
			if (!isKnowDeadCodePattern(this.condition) || currentScope.compilerOptions().reportDeadCodeInTrivialIfStatement) {
				this.thenStatement.complainIfUnreachable(thenFlowInfo, currentScope, initialComplaintLevel, false);
			}
		}
		thenFlowInfo = this.thenStatement.analyseCode(currentScope, flowContext, thenFlowInfo);
	}
	// code gen: optimizing the jump around the ELSE part
	if ((thenFlowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) {
		this.bits |= ASTNode.ThenExit;
	}

	// process the ELSE part
	if (this.elseStatement != null) {
	    // signal else clause unnecessarily nested, tolerate else-if code pattern
	    if (thenFlowInfo == FlowInfo.DEAD_END
	            && (this.bits & IsElseIfStatement) == 0 	// else of an else-if
	            && !(this.elseStatement instanceof IfStatement)) {
	        currentScope.problemReporter().unnecessaryElse(this.elseStatement);
	    }
		// Save info for code gen
		this.elseInitStateIndex = currentScope.methodScope().recordInitializationStates(elseFlowInfo);
		if (isConditionOptimizedTrue) {
			if (!isKnowDeadCodePattern(this.condition) || currentScope.compilerOptions().reportDeadCodeInTrivialIfStatement) {
				this.elseStatement.complainIfUnreachable(elseFlowInfo, currentScope, initialComplaintLevel, false);
			}
		}
		elseFlowInfo = this.elseStatement.analyseCode(currentScope, flowContext, elseFlowInfo);
	}
	// merge THEN & ELSE initializations
	FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
		thenFlowInfo,
		isConditionOptimizedTrue,
		elseFlowInfo,
		isConditionOptimizedFalse,
		true /*if(true){ return; }  fake-reachable(); */);
	this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
	return mergedInfo;
}

/**
 * If code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	/**
	 * new method binding for IF
	 */
	if(this.ifAppropriateMethodForOverload != null)
		this.generateOperatorOverloadCodeForIF(this.ifAppropriateMethodForOverload, currentScope, codeStream, true);
	/**
	 * end
	 */
	if ((this.bits & IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	BranchLabel endifLabel = new BranchLabel(codeStream);

	boolean hasThenPart =
		!(this.thenStatement == null
				|| this.thenStatement.isEmptyBlock());
	boolean hasElsePart =
		!( this.elseStatement == null
				|| this.elseStatement.isEmptyBlock());

	if (hasThenPart) {
		if (this.thenInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex);
		}
		// generate then statement
		this.thenStatement.generateCode(currentScope, codeStream);
	}
	if (hasElsePart) {
		/**
		 * new method binding for ELSE
		 */
		if(this.elseAppropriateMethodForOverload != null)
			this.generateOperatorOverloadCodeForELSE(this.elseAppropriateMethodForOverload, currentScope, codeStream, true);
		/**
		 * end
		 */
		if (this.elseInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					this.elseInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.elseInitStateIndex);
		}
		this.elseStatement.generateCode(currentScope, codeStream);

	} else {
		// generate condition side-effects
//		this.condition.generateCode(currentScope, codeStream, false);
//		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	// May loose some local variable initializations : affecting the local variable attributes
	/**
	 * new method binding for ENDIF
	 */
	if(this.endifAppropriateMethodForOverload != null)
		this.generateOperatorOverloadCodeForENDIF(this.endifAppropriateMethodForOverload, currentScope, codeStream, true);

	/**
	 * end
	 */
	if (this.mergedInitStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				this.mergedInitStateIndex);
		codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
	}
	endifLabel.place();
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * Milan new
 */
public String getMethodNameForIF() {
	return "_IF"; //$NON-NLS-1$
}

public String getMethodNameForELSE() {
	return "_ELSE"; //$NON-NLS-1$
}

public String getMethodNameForENDIF() {
	return "_END_IF"; //$NON-NLS-1$
}

/**
 * method overloading IF
 */

public MethodBinding getMethodBindingForOverloadForIF(BlockScope scope) {
	TypeBinding [] tb_right = new TypeBinding[] {this.condition.resolvedType};
	final TypeBinding tb = scope.parent.classScope().referenceContext.binding;
	final Expression[] args = new Expression[] {this.condition};
	InvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite() {
		@Override
		public TypeBinding invocationTargetType() {
			return tb;
		}
		@Override
		public Expression[] arguments() {
			return args;
		}
	};

	String ms = getMethodNameForIF();

	MethodBinding mb2 = scope.parent.getMethod(tb, ms.toCharArray(), tb_right, fakeInvocationSite);
	return mb2;
}

public void generateOperatorOverloadCodeForIF(MethodBinding mb2, BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	this.thisReference.generateCode(currentScope, codeStream, true);
	this.condition.generateCode(currentScope, codeStream, true);
	if (mb2.hasSubstitutedParameters() || mb2.hasSubstitutedReturnType()) {
		TypeBinding tbo = mb2.returnType;
		MethodBinding mb3 = mb2.original();
		MethodBinding final_mb = mb3;
		codeStream.checkcast(final_mb.declaringClass);
		codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());
		if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
			codeStream.checkcast(tbo);
		}
	} else {
		MethodBinding original = mb2.original();
		if(original.isPrivate()){
			codeStream.invoke(Opcodes.OPC_invokestatic, this.ifSyntheticAccessor, null /* default declaringClass */);
		}
		else{
			codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
		}
		if (!mb2.returnType.isBaseType()) codeStream.checkcast(mb2.returnType);
	}
}

/**
 * method overloading ELSE
 */
public MethodBinding getMethodBindingForOverloadForELSE(BlockScope scope) {
	TypeBinding [] tb_right = new TypeBinding[] {};

	final TypeBinding tb = scope.parent.classScope().referenceContext.binding;
	InvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite() {
		@Override
		public TypeBinding invocationTargetType() {
			return tb;
		}
		@Override
		public Expression[] arguments() {
			return null;
		}
	};

	String ms = getMethodNameForELSE();

	MethodBinding mb2 = scope.parent.getMethod(tb, ms.toCharArray(), tb_right, fakeInvocationSite);
	return mb2;
}

public void generateOperatorOverloadCodeForELSE(MethodBinding mb2, BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	this.thisReference.generateCode(currentScope, codeStream, true);
	if (mb2.hasSubstitutedParameters() || mb2.hasSubstitutedReturnType()) {
		TypeBinding tbo = mb2.returnType;
		MethodBinding mb3 = mb2.original();
		MethodBinding final_mb = mb3;
		codeStream.checkcast(final_mb.declaringClass);
		codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());
		if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
			codeStream.checkcast(tbo);
		}
	} else {
		MethodBinding original = mb2.original();
		if(original.isPrivate()){
			codeStream.invoke(Opcodes.OPC_invokestatic, this.elseSyntheticAccessor, null /* default declaringClass */);
		}
		else{
			codeStream.invoke((mb2.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, mb2, mb2.declaringClass);
		}
	}
}

/**
 * method overloading ENDIF
 */
public MethodBinding getMethodBindingForOverloadForENDIF(BlockScope scope) {
	TypeBinding [] tb_right = new TypeBinding[] {};

	final TypeBinding tb = scope.parent.classScope().referenceContext.binding;
	InvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite() {
		@Override
		public TypeBinding invocationTargetType() {
			return tb;
		}
		@Override
		public Expression[] arguments() {
			return null;
		}
	};

	String ms = getMethodNameForENDIF();

	MethodBinding mb2 = scope.parent.getMethod(tb, ms.toCharArray(), tb_right,  fakeInvocationSite);
	return mb2;
}

public void generateOperatorOverloadCodeForENDIF(MethodBinding mb2, BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	this.thisReference.generateCode(currentScope, codeStream, true);
	if (mb2.hasSubstitutedParameters() || mb2.hasSubstitutedReturnType()) {
		TypeBinding tbo = mb2.returnType;
		MethodBinding mb3 = mb2.original();
		MethodBinding final_mb = mb3;
		codeStream.checkcast(final_mb.declaringClass);
		codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());
		if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
			codeStream.checkcast(tbo);
		}
	} else {
		MethodBinding original = mb2.original();
		if(original.isPrivate()){
			codeStream.invoke(Opcodes.OPC_invokestatic, this.endifSyntheticAccessor, null /* default declaringClass */);
		}
		else{
			codeStream.invoke((mb2.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, mb2, mb2.declaringClass);
		}
	}
}
/**
 * milan: end overloading block
 */


/**
 * Answers true if the if is identified as a known coding pattern which
 * should be tolerated by dead code analysis.
 * e.g. if (DEBUG) print(); // no complaint
 * Only invoked when overall condition is known to be optimizeable into false.
 */
public static boolean isKnowDeadCodePattern(Expression expression) {
	// if (!DEBUG) print(); - tolerated
	if (expression instanceof UnaryExpression) {
		expression = ((UnaryExpression) expression).expression;
	}
	// if (DEBUG) print(); - tolerated
	if (expression instanceof Reference) return true;
	return false;
}

@Override
public StringBuilder printStatement(int indent, StringBuilder output) {
	printIndent(indent, output).append("IF ("); //$NON-NLS-1$
	this.condition.printExpression(0, output).append(")\n");	//$NON-NLS-1$
	this.thenStatement.printStatement(indent + 2, output);
	if (this.elseStatement != null) {
		output.append('\n');
		printIndent(indent, output);
		output.append("ELSE\n"); //$NON-NLS-1$
		this.elseStatement.printStatement(indent + 2, output);
	}
	return output;
}


@Override
public void resolve(BlockScope scope) {
	if (this.condition.resolvedType == null){
		this.condition.resolveType(scope);
		if(this.condition.resolvedType == null)
			return;
	}
	MethodBinding mb2 = this.getMethodBindingForOverloadForIF(scope);
	if (mb2 != null && mb2.isValidBinding()) {
		if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
			scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodNameForIF());
			return;
		}
		if(mb2.returnType != TypeBinding.VOID){
			scope.problemReporter().invalidReturnTypeForOverloadedOperator(this, getMethodNameForIF(), TypeBinding.VOID, mb2.returnType);
			return;
		}
		this.ifAppropriateMethodForOverload = mb2;
		if (isMethodUseDeprecated(this.ifAppropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
			scope.problemReporter().deprecatedMethod(this.ifAppropriateMethodForOverload, this);

		if(this.thisReference.resolvedType == null)
			this.thisReference.resolveType(scope);
		this.thisReference.computeConversion(scope, this.thisReference.resolvedType, this.thisReference.resolvedType);
		this.condition.computeConversion(scope, this.ifAppropriateMethodForOverload.parameters[0], this.condition.resolvedType);
		if ((this.bits & IsReachable) == 0) {
			return;
		}
		boolean hasThenPart =
			!(this.thenStatement == null
					|| this.thenStatement.isEmptyBlock());
		boolean hasElsePart =
			!( this.elseStatement == null
					|| this.elseStatement.isEmptyBlock());
		if(hasThenPart)
			this.thenStatement.resolve(scope);
		if(hasElsePart){
			MethodBinding mb3 = this.getMethodBindingForOverloadForELSE(scope);
			if (mb3.isValidBinding()) {
				if((mb3.modifiers & ClassFileConstants.AccStatic) != 0) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodNameForELSE());
					return;
				}
				if(mb3.returnType != TypeBinding.VOID){
					scope.problemReporter().invalidReturnTypeForOverloadedOperator(this, getMethodNameForELSE(), TypeBinding.VOID, mb3.returnType);
					return;
				}
				this.elseAppropriateMethodForOverload = mb3;
				if (isMethodUseDeprecated(this.elseAppropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
					scope.problemReporter().deprecatedMethod(this.elseAppropriateMethodForOverload, this);
			}else{
				scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodNameForELSE(), TypeBinding.VOID);
				return;
			}
			this.elseStatement.resolve(scope);
		}
		MethodBinding mb4 = this.getMethodBindingForOverloadForENDIF(scope);
		if (mb4.isValidBinding()) {
			if((mb4.modifiers & ClassFileConstants.AccStatic) != 0) {
				scope.problemReporter().overloadedOperatorMethodNotStatic(this, getMethodNameForENDIF());
				return;
			}if(mb4.returnType != TypeBinding.VOID){
				scope.problemReporter().invalidReturnTypeForOverloadedOperator(this, getMethodNameForENDIF(), TypeBinding.VOID, mb4.returnType);
				return;
			}
			this.endifAppropriateMethodForOverload = mb4;
			if (isMethodUseDeprecated(this.endifAppropriateMethodForOverload, scope, true, new InvocationSite.EmptyWithAstNode(this)))
				scope.problemReporter().deprecatedMethod(this.endifAppropriateMethodForOverload, this);
		}else{
			scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodNameForENDIF(), TypeBinding.VOID);
			return;
		}
	}else{
		if(this.condition == null)
			scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodNameForIF(), TypeBinding.VOID);
		else
			scope.problemReporter().invalidOrMissingOverloadedOperator(this, getMethodNameForIF(), this.condition.resolvedType);
		return;
	}
}

@Override
public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		this.condition.traverse(visitor, blockScope);
		if (this.thenStatement != null)
			this.thenStatement.traverse(visitor, blockScope);
		if (this.elseStatement != null)
			this.elseStatement.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}

}
