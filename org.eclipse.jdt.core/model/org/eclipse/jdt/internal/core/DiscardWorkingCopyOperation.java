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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Discards a working copy (decrement its use count and remove its working copy info if the use count is 0)
 * and signal its removal through a delta.
 */
public class DiscardWorkingCopyOperation extends JavaModelOperation {
	
	public DiscardWorkingCopyOperation(IJavaElement workingCopy) {
		super(new IJavaElement[] {workingCopy});
	}
	protected void executeOperation() throws JavaModelException {
		CompilationUnit workingCopy = getWorkingCopy();
		
		int useCount = JavaModelManager.getJavaModelManager().discardPerWorkingCopyInfo(workingCopy);
		if (useCount == 0) {
			if (!workingCopy.isPrimary()) {
				// report removed java delta for a non-primary working copy
				JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
				delta.removed(workingCopy);
				addDelta(delta);
				removeReconcileDelta(workingCopy);
			} else {
				if (workingCopy.getResource().isAccessible()) {
					// report a F_PRIMARY_WORKING_COPY change delta for a primary working copy
					JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
					delta.changed(workingCopy, IJavaElementDelta.F_PRIMARY_WORKING_COPY);
					addDelta(delta);
				} else {
					// report a REMOVED delta
					JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
					delta.removed(workingCopy, IJavaElementDelta.F_PRIMARY_WORKING_COPY);
					addDelta(delta);
				}
			}
		}
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
