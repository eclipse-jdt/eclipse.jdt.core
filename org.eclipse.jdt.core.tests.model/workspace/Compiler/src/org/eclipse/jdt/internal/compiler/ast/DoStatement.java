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

public class DoStatement extends Statement {

	public Expression condition;
	public Statement action;

	private Label breakLabel, continueLabel;

	// for local variables table attributes
	int mergedInitStateIndex = -1;

	public DoStatement(Expression condition, Statement action, int s, int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
		this.condition = condition;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= IsUsefulEmptyStatementMASK;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		breakLabel = new Label();
		continueLabel = new Label();
		LoopingFlowContext loopingContext =
			new LoopingFlowContext(
				flowContext,
				this,
				breakLabel,
				continueLabel,
				currentScope);

		Constant cst = condition.constant;
		boolean isConditionTrue = cst != NotAConstant && cst.booleanValue() == true;
		cst = condition.optimizedBooleanConstant();
		boolean isConditionOptimizedTrue = cst != NotAConstant && cst.booleanValue() == true;
		boolean isConditionOptimizedFalse = cst != NotAConstant && cst.booleanValue() == false;

		int previousMode = flowInfo.reachMode();
				
		if ((action != null) && !action.isEmptyBlock()) {
			flowInfo = action.analyseCode(currentScope, loopingContext, flowInfo);

			// code generation can be optimized when no need to continue in the loop
			if (!flowInfo.isReachable() && !loopingContext.initsOnContinue.isReachable()) {
				continueLabel = null;
			}
		}
		/* Reset reach mode, to address following scenario.
		 *   final blank;
		 *   do { if (true) break; else blank = 0; } while(false);
		 *   blank = 1; // may be initialized already 
		 */
		flowInfo.setReachMode(previousMode);
		
		flowInfo =
			condition.analyseCode(
				currentScope,
				loopingContext,
				(action == null
					? flowInfo
					: (flowInfo.mergedWith(loopingContext.initsOnContinue))));
		if (!isConditionOptimizedFalse && continueLabel != null) {
			loopingContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);
		}

		// end of loop
		FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
				loopingContext.initsOnBreak, 
				isConditionOptimizedTrue, 
				flowInfo.initsWhenFalse(), 
				false, // never consider opt false case for DO loop, since break can always occur (47776)
				!isConditionTrue /*do{}while(true); unreachable(); */);
		mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * Do statement code generation
	 *
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;

		// labels management
		Label actionLabel = new Label(codeStream);
		actionLabel.place();
		breakLabel.initialize(codeStream);
		if (continueLabel != null) {
			continueLabel.initialize(codeStream);
		}

		// generate action
		if (action != null) {
			action.generateCode(currentScope, codeStream);
		}
		// generate condition
		if (continueLabel != null) {
			continueLabel.place();
			condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				actionLabel,
				null,
				true);
		}
		breakLabel.place();

		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);

	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output).append("do"); //$NON-NLS-1$
		if (action == null)
			output.append(" ;\n"); //$NON-NLS-1$
		else {
			output.append('\n');
			action.printStatement(indent + 1, output).append('\n');
		}
		output.append("while ("); //$NON-NLS-1$
		return condition.printExpression(0, output).append(");"); //$NON-NLS-1$
	}
	public void resolve(BlockScope scope) {

		TypeBinding type = condition.resolveTypeExpecting(scope, BooleanBinding);
		condition.computeConversion(scope, type, type);
		if (action != null)
			action.resolve(scope);
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (action != null) {
				action.traverse(visitor, scope);
			}
			condition.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
