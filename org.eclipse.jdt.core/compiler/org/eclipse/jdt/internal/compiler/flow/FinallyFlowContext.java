package org.eclipse.jdt.internal.compiler.flow;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class FinallyFlowContext extends FlowContext {
	Reference finalAssignments[];
	int assignCount;
	public FinallyFlowContext(FlowContext parent, AstNode associatedNode) {
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
			Reference ref;
			if (((ref = finalAssignments[i]).bits & BindingIds.FIELD) != 0) {
				// final field
				if (flowInfo
					.isPotentiallyAssigned((FieldBinding) ((NameReference) ref).binding)) {
					scope.problemReporter().duplicateInitializationOfBlankFinalField(
						(FieldBinding) ((NameReference) ref).binding,
						(NameReference) ref);
				}
			} else {
				// final local variable
				if (flowInfo
					.isPotentiallyAssigned((LocalVariableBinding) ((NameReference) ref).binding)) {
					scope.problemReporter().duplicateInitializationOfFinalLocal(
						(LocalVariableBinding) ((NameReference) ref).binding,
						(NameReference) ref);
				}
			}
			// any reference reported at this level is removed from the parent context 
			// where it could also be reported again
			FlowContext currentContext = parent;
			while (currentContext != null) {
				if (currentContext.isSubRoutine()) {
					currentContext.removeFinalAssignmentIfAny(ref);
				}
				currentContext = currentContext.parent;
			}
		}
	}

	public boolean isSubRoutine() {
		return true;
	}

	boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {
		if (assignCount == 0) {
			finalAssignments = new Reference[5];
		} else {
			if (assignCount == finalAssignments.length)
				System.arraycopy(
					finalAssignments,
					0,
					(finalAssignments = new Reference[assignCount * 2]),
					0,
					assignCount);
		};
		finalAssignments[assignCount++] = finalAssignment;
		return true;
	}

}
