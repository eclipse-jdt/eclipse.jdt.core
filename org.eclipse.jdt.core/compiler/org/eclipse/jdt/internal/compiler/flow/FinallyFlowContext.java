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
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class FinallyFlowContext extends FlowContext {
	
	Reference[] finalAssignments;
	VariableBinding[] finalVariables;
	int assignCount;

	Expression[] nullReferences;
	int[] nullStatus;
	int nullCount;
	
	public FinallyFlowContext(FlowContext parent, ASTNode associatedNode) {
		super(parent, associatedNode);
	}

	/**
	 * Given some contextual initialization info (derived from a try block or a catch block), this 
	 * code will check that the subroutine context does not also initialize a final variable potentially set
	 * redundantly.
	 */
	public void complainOnDeferredChecks(FlowInfo flowInfo, BlockScope scope) {
		
		// check redundant final assignments
		for (int i = 0; i < assignCount; i++) {
			VariableBinding variable = finalVariables[i];
			if (variable == null) continue;
			
			boolean complained = false; // remember if have complained on this final assignment
			if (variable instanceof FieldBinding) {
				// final field
				if (flowInfo.isPotentiallyAssigned((FieldBinding)variable)) {
					complained = true;
					scope.problemReporter().duplicateInitializationOfBlankFinalField((FieldBinding)variable, finalAssignments[i]);
				}
			} else {
				// final local variable
				if (flowInfo.isPotentiallyAssigned((LocalVariableBinding) variable)) {
					complained = true;
					scope.problemReporter().duplicateInitializationOfFinalLocal(
						(LocalVariableBinding) variable,
						finalAssignments[i]);
				}
			}
			// any reference reported at this level is removed from the parent context 
			// where it could also be reported again
			if (complained) {
				FlowContext currentContext = parent;
				while (currentContext != null) {
					//if (currentContext.isSubRoutine()) {
					currentContext.removeFinalAssignmentIfAny(finalAssignments[i]);
					//}
					currentContext = currentContext.parent;
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
	
	public String individualToString() {
		
		StringBuffer buffer = new StringBuffer("Finally flow context"); //$NON-NLS-1$
		buffer.append("[finalAssignments count - ").append(assignCount).append(']'); //$NON-NLS-1$
		buffer.append("[nullReferences count - ").append(nullCount).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}
	
	public boolean isSubRoutine() {
		return true;
	}
	
	protected boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {
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

	void removeFinalAssignmentIfAny(Reference reference) {
		for (int i = 0; i < assignCount; i++) {
			if (finalAssignments[i] == reference) {
				finalAssignments[i] = null;
				finalVariables[i] = null;
				return;
			}
		}
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
}
