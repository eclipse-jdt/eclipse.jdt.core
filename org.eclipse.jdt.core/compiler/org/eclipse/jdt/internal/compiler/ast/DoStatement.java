/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class DoStatement extends Statement {

	public Expression condition;
	public Statement action;

	private BranchLabel breakLabel, continueLabel;

	// for local variables table attributes
	int mergedInitStateIndex = -1;

	public DoStatement(Expression condition, Statement action, int s, int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
		this.condition = condition;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= IsUsefulEmptyStatement;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		breakLabel = new BranchLabel();
		continueLabel = new BranchLabel();
		LoopingFlowContext loopingContext =
			new LoopingFlowContext(
				flowContext,
				flowInfo,
				this,
				breakLabel,
				continueLabel,
				currentScope);

		Constant cst = condition.constant;
		boolean isConditionTrue = cst != Constant.NotAConstant && cst.booleanValue() == true;
		cst = condition.optimizedBooleanConstant();
		boolean isConditionOptimizedTrue = cst != Constant.NotAConstant && cst.booleanValue() == true;
		boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;

		int previousMode = flowInfo.reachMode();
				
		UnconditionalFlowInfo actionInfo = flowInfo.nullInfoLessUnconditionalCopy();
		// we need to collect the contribution to nulls of the coming paths through the
		// loop, be they falling through normally or branched to break, continue labels
		// or catch blocks
		if ((action != null) && !action.isEmptyBlock()) {
			actionInfo = action.
				analyseCode(currentScope, loopingContext, actionInfo).
				unconditionalInits();

			// code generation can be optimized when no need to continue in the loop
			if ((actionInfo.tagBits & 
					loopingContext.initsOnContinue.tagBits & 
					FlowInfo.UNREACHABLE) != 0) {
				continueLabel = null;
			}
		}
		/* Reset reach mode, to address following scenario.
		 *   final blank;
		 *   do { if (true) break; else blank = 0; } while(false);
		 *   blank = 1; // may be initialized already 
		 */
		actionInfo.setReachMode(previousMode);
		
		LoopingFlowContext condLoopContext;
		FlowInfo condInfo =
			condition.analyseCode(
				currentScope,
				(condLoopContext =
					new LoopingFlowContext(flowContext,	flowInfo, this, null, 
						null, currentScope)),
				(action == null
					? actionInfo
					: (actionInfo.mergedWith(loopingContext.initsOnContinue))).copy());
		if (!isConditionOptimizedFalse && continueLabel != null) {
			loopingContext.complainOnDeferredFinalChecks(currentScope, condInfo);
			condLoopContext.complainOnDeferredFinalChecks(currentScope, condInfo);
			UnconditionalFlowInfo checkFlowInfo;
			loopingContext.complainOnDeferredNullChecks(currentScope, 
					checkFlowInfo = actionInfo.
						addPotentialNullInfoFrom(
						  condInfo.initsWhenTrue().unconditionalInits()));
			condLoopContext.complainOnDeferredNullChecks(currentScope, 
					checkFlowInfo);
		}

		// end of loop
		FlowInfo mergedInfo = FlowInfo.mergedOptimizedBranches(
				(loopingContext.initsOnBreak.tagBits &
					FlowInfo.UNREACHABLE) != 0 ?
					loopingContext.initsOnBreak :
					flowInfo.unconditionalCopy().addInitializationsFrom(loopingContext.initsOnBreak), 
						// recover upstream null info
				isConditionOptimizedTrue,
				(condInfo.tagBits & FlowInfo.UNREACHABLE) == 0 ?
						flowInfo.addInitializationsFrom(condInfo.initsWhenFalse()) : condInfo, 
					// recover null inits from before condition analysis
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

		if ((bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;

		// labels management
		BranchLabel actionLabel = new BranchLabel(codeStream);
		if (action != null) actionLabel.tagBits |= BranchLabel.USED;
		actionLabel.place();
		breakLabel.initialize(codeStream);
		if (continueLabel != null) {
			continueLabel.initialize(codeStream);
		}

		// generate action
		if (action != null) {
			action.generateCode(currentScope, codeStream);
		}
		Constant cst = condition.optimizedBooleanConstant();
		boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;		
		if (isConditionOptimizedFalse){
			condition.generateCode(currentScope, codeStream, false);
		} else {
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
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		if (breakLabel.hasForwardReferences()) {
			breakLabel.place();
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

		TypeBinding type = condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
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
