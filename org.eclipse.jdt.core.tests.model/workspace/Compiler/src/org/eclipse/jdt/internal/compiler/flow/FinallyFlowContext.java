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
	
	Reference finalAssignments[];
	VariableBinding finalVariables[];
	int assignCount;
	
	public FinallyFlowContext(FlowContext parent, ASTNode associatedNode) {
		super(parent, associatedNode);
	}

	/**
	 * Given some contextual initialization info (derived from a try block or a catch block), this 
	 * code will check that the subroutine context does not also initialize a final variable potentially set
	 * redundantly.
	 */
	public void complainOnRedundantFinalAssignments(
		FlowInfo flowInfo,
		BlockScope scope) {
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
	}

	public String individualToString() {
		
		StringBuffer buffer = new StringBuffer("Finally flow context"); //$NON-NLS-1$
		buffer.append("[finalAssignments count -").append(assignCount).append(']'); //$NON-NLS-1$
		return buffer.toString();
	}
	
	public boolean isSubRoutine() {
		return true;
	}

	boolean recordFinalAssignment(
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
}
