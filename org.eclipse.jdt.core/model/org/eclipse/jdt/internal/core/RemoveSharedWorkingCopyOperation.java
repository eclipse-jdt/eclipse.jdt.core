/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Removes a shared working copy from its cache and signal its removal through a delta.
 */
public class RemoveSharedWorkingCopyOperation extends JavaModelOperation {
	
	Map perFactoryWorkingCopies;
	
	public RemoveSharedWorkingCopyOperation(IJavaElement originalElement, Map perFactoryWorkingCopies) {
		super(new IJavaElement[] {originalElement});
		this.perFactoryWorkingCopies = perFactoryWorkingCopies;
	}
	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		WorkingCopy workingCopy;
		if ((workingCopy = (WorkingCopy)this.perFactoryWorkingCopies.remove(getCompilationUnit())) != null) {
			if (CompilationUnit.SHARED_WC_VERBOSE) {
				System.out.println("Destroying shared working copy " + workingCopy.toStringWithAncestors());//$NON-NLS-1$
			}

			// report removed java delta
			JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
			delta.removed(workingCopy);
			addDelta(delta);
			removeReconcileDelta(workingCopy);
		}
	}
	/**
	 * Returns the compilation unit this operation is working on.
	 */
	protected ICompilationUnit getCompilationUnit() {
		return (ICompilationUnit)getElementToProcess();
	}

}
