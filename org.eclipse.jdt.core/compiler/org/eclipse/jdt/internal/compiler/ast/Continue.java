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
		FlowContext targetContext = (label == null)
				? flowContext.getTargetContextForDefaultContinue()
				: flowContext.getTargetContextForContinueLabel(label);

		if (targetContext == null) {
			if (label == null) {
				currentScope.problemReporter().invalidContinue(this);
			} else {
				currentScope.problemReporter().undefinedLabel(this); 
			}
			return flowInfo; // pretend it did not continue since no actual target			
		} 

		if (targetContext == FlowContext.NotContinuableContext) {
			currentScope.problemReporter().invalidContinue(this);
			return flowInfo; // pretend it did not continue since no actual target
		}
		targetLabel = targetContext.continueLabel();
		FlowContext traversedContext = flowContext;
		int subIndex = 0, maxSub = 5;
		subroutines = new AstNode[maxSub];

		do {
			AstNode sub;
			if ((sub = traversedContext.subRoutine()) != null) {
				if (subIndex == maxSub) {
					System.arraycopy(subroutines, 0, (subroutines = new AstNode[maxSub*=2]), 0, subIndex); // grow
				}
				subroutines[subIndex++] = sub;
				if (sub.cannotReturn()) {
					break;
				}
			}
			traversedContext.recordReturnFrom(flowInfo.unconditionalInits());

			AstNode node;
			if ((node = traversedContext.associatedNode) instanceof TryStatement) {
				TryStatement tryStatement = (TryStatement) node;
				flowInfo.addInitializationsFrom(tryStatement.subRoutineInits); // collect inits			
			} else if (traversedContext == targetContext) {
				// only record continue info once accumulated through subroutines, and only against target context
				targetContext.recordContinueFrom(flowInfo);
				break;
			}
		} while ((traversedContext = traversedContext.parent) != null);
		
		// resize subroutines
		if (subIndex != maxSub) {
			System.arraycopy(subroutines, 0, (subroutines = new AstNode[subIndex]), 0, subIndex);
		}
		return FlowInfo.DEAD_END;
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s += "continue "; //$NON-NLS-1$
		if (label != null)
			s += new String(label);
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}