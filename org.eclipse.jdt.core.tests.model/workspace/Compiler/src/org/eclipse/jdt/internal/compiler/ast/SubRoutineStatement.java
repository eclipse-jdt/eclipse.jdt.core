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

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * Extra behavior for statements which are generating subroutines
 */
public abstract class SubRoutineStatement extends Statement {
	
	public static final ExceptionLabel[] NO_EXCEPTION_HANDLER = new ExceptionLabel[0];
	ExceptionLabel[] anyExceptionLabels = NO_EXCEPTION_HANDLER;
	int anyExceptionLabelsCount = 0;
	
	public abstract boolean isSubRoutineEscaping();

	public abstract void generateSubRoutineInvocation(BlockScope currentScope, CodeStream codeStream);
	
	public ExceptionLabel enterAnyExceptionHandler(CodeStream codeStream) {
		
		int length;
		if ((length = this.anyExceptionLabelsCount) == this.anyExceptionLabels.length) {
			System.arraycopy(this.anyExceptionLabels, 0 , this.anyExceptionLabels=new ExceptionLabel[length*2 + 1], 0, length);
		}
		ExceptionLabel exceptionLabel = new ExceptionLabel(codeStream, null);
		this.anyExceptionLabels[this.anyExceptionLabelsCount++] = exceptionLabel;
		return exceptionLabel;
	}

	public void exitAnyExceptionHandler() {
		if (this.anyExceptionLabelsCount == 0) return;
		ExceptionLabel currentLabel = this.anyExceptionLabels[this.anyExceptionLabelsCount-1];
		if (currentLabel.start == currentLabel.codeStream.position) {
			// discard empty exception handler
			this.anyExceptionLabels[--this.anyExceptionLabelsCount] = null;
			currentLabel.codeStream.removeExceptionHandler(currentLabel);
		} else {
			currentLabel.placeEnd();
		}
	}
	
	public void placeAllAnyExceptionHandlers() {
		
		for (int i = 0; i < this.anyExceptionLabelsCount; i++) {
			this.anyExceptionLabels[i].place();
		}
	}
	
	public static void reenterExceptionHandlers(SubRoutineStatement[] subroutines, int max, CodeStream codeStream) {
		if (subroutines == null) return;
		if (max < 0) max = subroutines.length;
		for (int i = 0; i < max; i++) {
			subroutines[i].enterAnyExceptionHandler(codeStream); 
		}	
	}
}
