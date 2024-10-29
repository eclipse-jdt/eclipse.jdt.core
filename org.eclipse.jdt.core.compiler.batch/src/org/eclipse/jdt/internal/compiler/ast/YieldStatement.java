/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.INVOCATION_CONTEXT;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InsideSubRoutineFlowContext;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class YieldStatement extends BranchStatement {

	public Expression expression;
	public SwitchExpression swich; // innermost enclosing switch **expression**

	public boolean isImplicit;
	static final char[] SECRET_YIELD_RESULT_VALUE_NAME = " secretYieldValue".toCharArray(); //$NON-NLS-1$
	private LocalVariableBinding secretYieldResultValue = null;

public YieldStatement(Expression expression, int sourceStart, int sourceEnd) {
	super(null, sourceStart, sourceEnd);
	this.expression = expression;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// this.switchExpression != null true here.

	// here requires to generate a sequence of finally blocks invocations depending corresponding
	// to each of the traversed try statements, so that execution will terminate properly.


	// lookup the null label, this should answer the returnContext - for implicit yields, the nesting
	// doesn't occur since it immediately follow '->' and hence identical to default break - ie the
	// immediate breakable context is guaranteed to be the one intended;
	// while explicit yield should move up the parent to the switch expression.
	FlowContext targetContext = flowContext.getTargetContextForYield(!this.isImplicit);

	flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
	this.expression.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);

	targetContext.recordAbruptExit();
	targetContext.expireNullCheckedFieldInfo();

	this.initStateIndex =
		currentScope.methodScope().recordInitializationStates(flowInfo);

	this.targetLabel = targetContext.breakLabel();
	FlowContext traversedContext = flowContext;
	int subCount = 0;
	this.subroutines = new SubRoutineStatement[5];

	do {
		SubRoutineStatement sub;
		if ((sub = traversedContext.subroutine()) != null) {
			if (subCount == this.subroutines.length) {
				System.arraycopy(this.subroutines, 0, (this.subroutines = new SubRoutineStatement[subCount*2]), 0, subCount); // grow
			}
			this.subroutines[subCount++] = sub;
			if (sub.isSubRoutineEscaping()) {
				break;
			}
		}
		traversedContext.recordReturnFrom(flowInfo.unconditionalInits());
		traversedContext.recordBreakTo(targetContext);

		if (traversedContext instanceof InsideSubRoutineFlowContext) {
			ASTNode node = traversedContext.associatedNode;
			if (node instanceof TryStatement) {
				flowInfo.addInitializationsFrom(((TryStatement) node).subRoutineInits); // collect inits
			}
		} else if (traversedContext == targetContext) {
			// only record break info once accumulated through subroutines, and only against target context
			targetContext.recordBreakFrom(flowInfo);
			break;
		}
	} while ((traversedContext = traversedContext.getLocalParent()) != null);

	// resize subroutines
	if (subCount != this.subroutines.length) {
		System.arraycopy(this.subroutines, 0, (this.subroutines = new SubRoutineStatement[subCount]), 0, subCount);
	}
	return FlowInfo.DEAD_END;
}

@Override
protected void setSubroutineSwitchExpression(SubRoutineStatement sub) {
	sub.setSwitchExpression(this.swich);
}

protected void addSecretYieldResultValue(BlockScope scope) {
	SwitchExpression se = this.swich;
	if (se == null || !se.jvmStackVolatile)
		return;
	LocalVariableBinding local = new LocalVariableBinding(
			YieldStatement.SECRET_YIELD_RESULT_VALUE_NAME,
			se.resolvedType,
			ClassFileConstants.AccDefault,
			false);
	local.setConstant(Constant.NotAConstant);
	local.useFlag = LocalVariableBinding.USED;
	local.declaration = new LocalDeclaration(YieldStatement.SECRET_YIELD_RESULT_VALUE_NAME, 0, 0);
	assert se.yieldResolvedPosition >= 0;
	local.resolvedPosition = se.yieldResolvedPosition;
	assert local.resolvedPosition < scope.maxOffset;
	scope.addLocalVariable(local);
	this.secretYieldResultValue = local;
}

@Override
protected void restartExceptionLabels(CodeStream codeStream) {
	SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, -1, codeStream);
}

@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & ASTNode.IsReachable) == 0) {
		return;
	}
	boolean generateExpressionResultCodeExpanded = false;
	if (this.swich != null && this.swich.jvmStackVolatile && this.swich.resolvedType != null ) {
		generateExpressionResultCodeExpanded = true;
		addSecretYieldResultValue(currentScope);
		assert this.secretYieldResultValue != null;
		codeStream.record(this.secretYieldResultValue);
		SingleNameReference lhs = new SingleNameReference(this.secretYieldResultValue.name, 0);
		lhs.binding = this.secretYieldResultValue;
		lhs.bits &= ~ASTNode.RestrictiveFlagMASK; // clear bits
		lhs.bits |= Binding.LOCAL;
		lhs.bits |= ASTNode.IsSecretYieldValueUsage;
		((LocalVariableBinding) lhs.binding).markReferenced(); // TODO : Can be skipped?
		Assignment assignment = new Assignment(lhs, this.expression, 0);
		assignment.generateCode(currentScope, codeStream);
	} else {
		this.expression.generateCode(currentScope, codeStream, this.swich != null);
		if (this.expression.resolvedType == TypeBinding.NULL) {
			if (!this.swich.resolvedType.isBaseType()) {
				// no opcode called for to align the types, but we need to adjust the notion of type of TOS.
				codeStream.operandStack.pop(TypeBinding.NULL);
				codeStream.operandStack.push(this.swich.resolvedType);
			}
		}
	}
	int pc = codeStream.position;
	// generation of code responsible for invoking the finally
	// blocks in sequence
	if (this.subroutines != null){
		for (int i = 0, max = this.subroutines.length; i < max; i++){
			SubRoutineStatement sub = this.subroutines[i];
			SwitchExpression se = sub.getSwitchExpression();
			setSubroutineSwitchExpression(sub);
			boolean didEscape = sub.generateSubRoutineInvocation(currentScope, codeStream, this.targetLabel, this.initStateIndex, null);
			sub.setSwitchExpression(se);
			if (didEscape) {
					if (generateExpressionResultCodeExpanded) {
						codeStream.removeVariable(this.secretYieldResultValue);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, i, codeStream);
					if (this.initStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
						codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
					}
					restartExceptionLabels(codeStream);
					return;
			}
		}
	}
	if (generateExpressionResultCodeExpanded) {
		this.swich.refillOperandStack(codeStream);
		codeStream.load(this.secretYieldResultValue);
		codeStream.removeVariable(this.secretYieldResultValue);
	}
	codeStream.goto_(this.targetLabel);
	codeStream.recordPositionsFrom(pc, this.sourceStart);
	SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, -1, codeStream);
	if (this.initStateIndex != -1) {
		codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
		codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
	}
}

@Override
public void resolve(BlockScope scope) {

	if (this.swich == null) {
		this.swich = scope.enclosingSwitchExpression();
		if (this.swich != null) {
			this.swich.resultExpressions.add(this.expression);
			if (this.swich.expressionContext == ASSIGNMENT_CONTEXT || this.swich.expressionContext == INVOCATION_CONTEXT) { // poly switch expression
				this.expression.setExpressionContext(this.swich.expressionContext); // result expressions feature in same context ...
				this.expression.setExpectedType(this.swich.expectedType);           // ... with the same target type
			}
		}
	}

	if (this.swich != null || this.isImplicit) {
		if (this.swich == null && this.isImplicit && !this.expression.statementExpression()) {
			if (scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK14) {
				/* JLS 13 14.11.2
				Switch labeled rules in switch statements differ from those in switch expressions (15.28).
				In switch statements they must be switch labeled statement expressions, ... */
				scope.problemReporter().invalidExpressionAsStatement(this.expression);
				return;
			}
		}
	} else {
		if (scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK14) {
			scope.problemReporter().switchExpressionsYieldOutsideSwitchExpression(this);
		}
	}
	TypeBinding type = this.expression.resolveType(scope);
	if (this.swich != null && type != null)
		this.swich.originalTypeMap.put(this.expression, type);
}

@Override
public StringBuilder printStatement(int tab, StringBuilder output) {
	if (!this.isImplicit)
		printIndent(tab, output).append("yield"); //$NON-NLS-1$
	if (this.isImplicit) {
		this.expression.print(tab, output);
	} else {
		output.append(' ');
		this.expression.printExpression(tab, output);
	}
	return output.append(';');
}

@Override
public void traverse(ASTVisitor visitor, BlockScope blockscope) {
	if (visitor.visit(this, blockscope)) {
		this.expression.traverse(visitor, blockscope);
	}
	visitor.endVisit(this, blockscope);
}

@Override
public boolean doesNotCompleteNormally() {
	return true;
}

@Override
public boolean canCompleteNormally() {
	return false;
}
}