/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.codegen.Label;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class LoopingFlowContext extends SwitchFlowContext {
	
	public Label continueLabel;
	public UnconditionalFlowInfo initsOnContinue = FlowInfo.DEAD_END;
	Reference finalAssignments[];
	VariableBinding finalVariables[];
	int assignCount = 0;
	
	Expression[] nullReferences;
	int[] nullStatus;
	int nullCount;
	
	Scope associatedScope;
	
	public LoopingFlowContext(
		FlowContext parent,
		ASTNode associatedNode,
		Label breakLabel,
		Label continueLabel,
		Scope associatedScope) {
		super(parent, associatedNode, breakLabel);
		this.continueLabel = continueLabel;
		this.associatedScope = associatedScope;
	}
	
	public void complainOnDeferredChecks(BlockScope scope, FlowInfo flowInfo) {
		
		// complain on final assignments in loops
		for (int i = 0; i < assignCount; i++) {
			VariableBinding variable = finalVariables[i];
			if (variable == null) continue;
			boolean complained = false; // remember if have complained on this final assignment
			if (variable instanceof FieldBinding) {
				if (flowInfo.isPotentiallyAssigned((FieldBinding) variable)) {
					complained = true;
					scope.problemReporter().duplicateInitializationOfBlankFinalField(
						(FieldBinding) variable,
						finalAssignments[i]);
				}
			} else {
				if (flowInfo.isPotentiallyAssigned((LocalVariableBinding) variable)) {
					complained = true;
					scope.problemReporter().duplicateInitializationOfFinalLocal(
						(LocalVariableBinding) variable,
						finalAssignments[i]);
				}
			}
			// any reference reported at this level is removed from the parent context where it 
			// could also be reported again
			if (complained) {
				FlowContext context = parent;
				while (context != null) {
					context.removeFinalAssignmentIfAny(finalAssignments[i]);
					context = context.parent;
				}
			}
		}
		// check inconsistent null checks
		for (int i = 0; i < nullCount; i++) {
			Expression expression = nullReferences[i];
			if (expression == null) continue;
			// final local variable
			LocalVariableBinding local = expression.localVariableBinding();
			switch (nullStatus[i]) {
				case FlowInfo.NULL :
					if (flowInfo.isDefinitelyNull(local)) {
						nullReferences[i] = null;
						this.parent.recordUsingNullReference(scope, local, expression, nullStatus[i], flowInfo);
					}
					break;
				case FlowInfo.NON_NULL :
					if (flowInfo.isDefinitelyNonNull(local)) {
						nullReferences[i] = null;
						this.parent.recordUsingNullReference(scope, local, expression, nullStatus[i], flowInfo);
					}
					break;
			}
		}		
	}

	public Label continueLabel() {
		return continueLabel;
	}

	public String individualToString() {
		StringBuffer buffer = new StringBuffer("Looping flow context"); //$NON-NLS-1$
		buffer.append("[initsOnBreak - ").append(initsOnBreak.toString()).append(']'); //$NON-NLS-1$
		buffer.append("[initsOnContinue - ").append(initsOnContinue.toString()).append(']'); //$NON-NLS-1$
		buffer.append("[finalAssignments count - ").append(assignCount).append(']'); //$NON-NLS-1$
		buffer.append("[nullReferences count - ").append(nullCount).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}

	public boolean isContinuable() {
		return true;
	}

	public boolean isContinuedTo() {
		return initsOnContinue != FlowInfo.DEAD_END;
	}

	public void recordContinueFrom(FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return;
		if (initsOnContinue == FlowInfo.DEAD_END) {
			initsOnContinue = flowInfo.copy().unconditionalInits();
		} else {
			initsOnContinue = initsOnContinue.mergedWith(flowInfo.copy().unconditionalInits());
		}
	}

	protected boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {

		// do not consider variables which are defined inside this loop
		if (binding instanceof LocalVariableBinding) {
			Scope scope = ((LocalVariableBinding) binding).declaringScope;
			while ((scope = scope.parent) != null) {
				if (scope == associatedScope)
					return false;
			}
		}
		if (assignCount == 0) {
			finalAssignments = new Reference[5];
			finalVariables = new VariableBinding[5];
		} else {
			if (assignCount == finalAssignments.length)
				System.arraycopy(
					finalAssignments,
					0,
					(finalAssignments = new Reference[assignCount * 2]),
					0,
					assignCount);
			System.arraycopy(
				finalVariables,
				0,
				(finalVariables = new VariableBinding[assignCount * 2]),
				0,
				assignCount);
		}
		finalAssignments[assignCount] = finalAssignment;
		finalVariables[assignCount++] = binding;
		return true;
	}

	protected boolean recordNullReference(Expression expression, int status) {
		if (nullCount == 0) {
			nullReferences = new Expression[5];
			nullStatus = new int[5];
		} else {
			if (nullCount == nullReferences.length) {
				System.arraycopy(nullReferences, 0, nullReferences = new Expression[nullCount * 2], 0, nullCount);
				System.arraycopy(nullStatus, 0, nullStatus = new int[nullCount * 2], 0, nullCount);
			}
		}
		nullReferences[nullCount] = expression;
		nullStatus[nullCount++] = status;
		return true;
	}	
	
	void removeFinalAssignmentIfAny(Reference reference) {
		for (int i = 0; i < assignCount; i++) {
			if (finalAssignments[i] == reference) {
				finalAssignments[i] = null;
				finalVariables[i] = null;
				return;
			}
		}
	}
}
