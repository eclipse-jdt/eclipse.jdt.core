package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Continue extends BranchStatement {

	public Continue(char[] l, int s, int e) {
		super(l, s, e);
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// here requires to generate a sequence of finally blocks invocations depending corresponding
		// to each of the traversed try statements, so that execution will terminate properly.

		// lookup the label, this should answer the returnContext
		FlowContext targetContext;
		if (label == null) {
			targetContext = flowContext.getTargetContextForDefaultContinue();
		} else {
			targetContext = flowContext.getTargetContextForContinueLabel(label);
		}
		if (targetContext == null) {
			if (label == null) {
				currentScope.problemReporter().invalidContinue(this);
			} else {
				currentScope.problemReporter().undefinedLabel(this); // need to improve
			}
		} else {
			if (targetContext == FlowContext.NotContinuableContext) {
				currentScope.problemReporter().invalidContinue(this);
				return FlowInfo.DeadEnd;
			}
			targetLabel = targetContext.continueLabel();
			targetContext.recordContinueFrom(flowInfo);
			FlowContext traversedContext = flowContext;
			int subIndex = 0, maxSub = 5;
			subroutines = new AstNode[maxSub];
			while (true) {
				AstNode sub;
				if ((sub = traversedContext.subRoutine()) != null) {
					if (subIndex == maxSub) {
						System.arraycopy(
							subroutines,
							0,
							(subroutines = new AstNode[maxSub *= 2]),
							0,
							subIndex);
						// grow
					}
					subroutines[subIndex++] = sub;
					if (sub.cannotReturn()) {
						break;
					}
				}
				if (traversedContext == targetContext) {
					break;
				} else {
					traversedContext = traversedContext.parent;
				}
			}
			// resize subroutines
			if (subIndex != maxSub) {
				System.arraycopy(
					subroutines,
					0,
					(subroutines = new AstNode[subIndex]),
					0,
					subIndex);
			}
		}
		return FlowInfo.DeadEnd;
	}

	public String toString(int tab) {
		/* slow code */

		String s = tabString(tab);
		s = s + "continue ";
		if (label != null)
			s = s + new String(label);
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}

}
