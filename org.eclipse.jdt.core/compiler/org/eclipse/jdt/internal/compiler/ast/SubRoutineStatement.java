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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * Extra behavior for statements which are generating subroutines
 */
public abstract class SubRoutineStatement extends Statement {
	
	ExceptionLabel anyExceptionLabel = null;
	
	public abstract boolean isSubRoutineEscaping();

	public abstract void generateSubRoutineInvocation(BlockScope currentScope, CodeStream codeStream);
	
	public ExceptionLabel enterAnyExceptionHandler(CodeStream codeStream) {
		
		if (this.anyExceptionLabel == null) {
			this.anyExceptionLabel = new ExceptionLabel(codeStream, null /*any exception*/);
		}
		this.anyExceptionLabel.placeStart();
		return this.anyExceptionLabel;
	}

	public void exitAnyExceptionHandler() {
		if (this.anyExceptionLabel != null) {
			this.anyExceptionLabel.placeEnd();
		}
	}
	
	public void placeAllAnyExceptionHandler() {
		this.anyExceptionLabel.place();
	}
	
	public static void reenterAnyExceptionHandlers(SubRoutineStatement[] subroutines, int max, CodeStream codeStream) {
		if (subroutines == null) return;
		if (max < 0) max = subroutines.length;
		for (int i = 0; i < max; i++) {
			subroutines[i].enterAnyExceptionHandler(codeStream); 
		}	
	}
}
