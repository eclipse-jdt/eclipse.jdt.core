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

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class BranchStatement extends Statement {
	public char[] label;
	public Label targetLabel;
	public SubRoutineStatement[] subroutines;
/**
 * BranchStatement constructor comment.
 */
public BranchStatement(char[] l, int s,int e) {
	label = l ;
	sourceStart = s;
	sourceEnd = e;
}
/**
 * Branch code generation
 *
 *   generate the finallyInvocationSequence.
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;

	// generation of code responsible for invoking the finally 
	// blocks in sequence
	if (subroutines != null){
		for (int i = 0, max = subroutines.length; i < max; i++){
			SubRoutineStatement sub = subroutines[i];
			sub.generateSubRoutineInvocation(currentScope, codeStream);
			if (sub.isSubRoutineEscaping()) {
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					SubRoutineStatement.reenterExceptionHandlers(subroutines, i, codeStream);
					return;
			}
			sub.exitAnyExceptionHandler();
		}
	}
	codeStream.goto_(targetLabel);
	codeStream.recordPositionsFrom(pc, this.sourceStart);
	SubRoutineStatement.reenterExceptionHandlers(subroutines, -1, codeStream);
}
public void resolve(BlockScope scope) {
	// nothing to do during name resolution
}

}
