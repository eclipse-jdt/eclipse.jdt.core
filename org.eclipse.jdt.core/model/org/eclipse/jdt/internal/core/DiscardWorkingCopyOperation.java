/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A working copy was discarded: signal its removal through a delta.
 */
public class DiscardWorkingCopyOperation extends JavaModelOperation {
	
	public DiscardWorkingCopyOperation(IJavaElement workingCopy) {
		super(new IJavaElement[] {workingCopy});
	}
	protected void executeOperation() throws JavaModelException {
		CompilationUnit workingCopy = getWorkingCopy();
		
		// report removed java delta
		JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
		delta.removed(workingCopy);
		addDelta(delta);
		removeReconcileDelta(workingCopy);
	}
	/**
	 * Returns the working copy this operation is working on.
	 */
	protected CompilationUnit getWorkingCopy() {
		return (CompilationUnit)getElementToProcess();
	}
	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}
}
