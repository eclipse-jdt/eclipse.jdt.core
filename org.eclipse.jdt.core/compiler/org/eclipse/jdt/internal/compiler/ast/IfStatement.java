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

public class IfStatement extends Statement {
	
	//this class represents the case of only one statement in 
	//either else and/or then branches.

	public Expression condition;
	public Statement thenStatement;
	public Statement elseStatement;

	boolean thenExit;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public IfStatement(
		Expression condition,
		Statement thenStatement,
		int s,
		int e) {

		this.condition = condition;
		this.thenStatement = thenStatement;
		sourceStart = s;
		sourceEnd = e;
	}

	public IfStatement(
		Expression condition,
		Statement thenStatement,
		Statement elseStatement,
		int s,
		int e) {

		this.condition = condition;
		this.thenStatement = thenStatement;
		this.elseStatement = elseStatement;
		sourceEnd = e;
		sourceStart = s;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		FlowInfo thenFlowInfo, elseFlowInfo;

		// process the condition
		flowInfo = condition.analyseCode(currentScope, flowContext, flowInfo);

		Constant cst = this.condition.optimizedBooleanConstant();
		boolean isConditionOptimizedTrue = cst != NotAConstant && cst.booleanValue() == true;
		boolean isConditionOptimizedFalse = cst != NotAConstant && cst.booleanValue() == false;
		
		// process the THEN part
		if (isConditionOptimizedFalse) {
			//thenFlowInfo = conditionInfo.initsWhenTrue().copy().setReachMode(FlowInfo.CHECK_POT_INIT_FAKE_REACHABLE);
			thenFlowInfo = flowInfo.initsWhenTrue().copy().setReachMode(FlowInfo.UNREACHABLE); // revert
			
		} else {
			thenFlowInfo = flowInfo.initsWhenTrue().copy();
		}
		if (this.thenStatement != null) {
			// Save info for code gen
			thenInitStateIndex =
				currentScope.methodScope().recordInitializationStates(thenFlowInfo);
			if (!thenFlowInfo.complainIfUnreachable(thenStatement, currentScope, false)) {
				thenFlowInfo =
					thenStatement.analyseCode(currentScope, flowContext, thenFlowInfo);
			}
		};
		// optimizing the jump around the ELSE part
		this.thenExit = !thenFlowInfo.isReachable();

		// process the ELSE part
		if (isConditionOptimizedTrue) {
			//elseFlowInfo = conditionInfo.initsWhenFalse().copy().setReachMode(FlowInfo.CHECK_POT_INIT_FAKE_REACHABLE);
			elseFlowInfo = flowInfo.initsWhenFalse().copy().setReachMode(FlowInfo.UNREACHABLE); // revert
		} else {
			elseFlowInfo = flowInfo.initsWhenFalse().copy();
		}
		if (this.elseStatement != null) {
			// Save info for code gen
			elseInitStateIndex =
				currentScope.methodScope().recordInitializationStates(elseFlowInfo);
			if (!elseFlowInfo.complainIfUnreachable(elseStatement, currentScope, false)) {
				elseFlowInfo =
					elseStatement.analyseCode(currentScope, flowContext, elseFlowInfo);
			}
		}

		boolean elseExit = !elseFlowInfo.isReachable();
		
		// merge THEN & ELSE initializations
		FlowInfo mergedInfo;
		if (isConditionOptimizedTrue){
			if (!this.thenExit) {
				mergedInfo = thenFlowInfo;
			} else {
				mergedInfo = elseFlowInfo.setReachMode(FlowInfo.UNREACHABLE);
			}

		} else if (isConditionOptimizedFalse) {
			if (!elseExit) {
				mergedInfo = elseFlowInfo;
			} else {
				mergedInfo = thenFlowInfo.setReachMode(FlowInfo.UNREACHABLE);
			}

		} else {
			mergedInfo = thenFlowInfo.mergedWith(elseFlowInfo.unconditionalInits());
		}
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * If code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((this.bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		Label endifLabel = new Label(codeStream);

		// optimizing the then/else part code gen
		Constant cst;
		boolean hasThenPart = 
			!(((cst = this.condition.optimizedBooleanConstant()) != NotAConstant
					&& cst.booleanValue() == false)
				|| this.thenStatement == null
				|| this.thenStatement.isEmptyBlock());
		boolean hasElsePart =
			!((cst != NotAConstant && cst.booleanValue() == true)
				|| this.elseStatement == null
				|| this.elseStatement.isEmptyBlock());

		if (hasThenPart) {
			Label falseLabel;
			// generate boolean condition
			this.condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				null,
				(falseLabel = new Label(codeStream)),
				true);
			// May loose some local variable initializations : affecting the local variable attributes
			if (thenInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					thenInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, thenInitStateIndex);
			}
			// generate then statement
			this.thenStatement.generateCode(currentScope, codeStream);
			// jump around the else statement
			if (hasElsePart && !thenExit) {
				this.thenStatement.branchChainTo(endifLabel);
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				codeStream.updateLastRecordedEndPC(position);
				//goto is tagged as part of the thenAction block
			}
			falseLabel.place();
		} else {
			if (hasElsePart) {
				// generate boolean condition
				this.condition.generateOptimizedBoolean(
					currentScope,
					codeStream,
					endifLabel,
					null,
					true);
			} else {
				// generate condition side-effects
				this.condition.generateCode(currentScope, codeStream, false);
				codeStream.recordPositionsFrom(pc, this.sourceStart);
			}
		}
		// generate else statement
		if (hasElsePart) {
			// May loose some local variable initializations : affecting the local variable attributes
			if (elseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					elseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, elseInitStateIndex);
			}
			this.elseStatement.generateCode(currentScope, codeStream);
		}
		endifLabel.place();
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope scope) {

		TypeBinding type = condition.resolveTypeExpecting(scope, BooleanBinding);
		condition.implicitWidening(type, type);
		if (thenStatement != null)
			thenStatement.resolve(scope);
		if (elseStatement != null)
			elseStatement.resolve(scope);
	}

	public String toString(int tab) {

		String inFront, s = tabString(tab);
		inFront = s;
		s = s + "if (" + condition.toStringExpression() + ") \n";	//$NON-NLS-1$ //$NON-NLS-2$
		s = s + thenStatement.toString(tab + 2) + ";"; //$NON-NLS-1$
		if (elseStatement != null)
			s = s + "\n" + inFront + "else\n" + elseStatement.toString(tab + 2) + ";"; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			condition.traverse(visitor, blockScope);
			if (thenStatement != null)
				thenStatement.traverse(visitor, blockScope);
			if (elseStatement != null)
				elseStatement.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}