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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Creates a new shared working copy and signal its addition through a delta.
 */
public class CreateSharedWorkingCopyOperation extends JavaModelOperation {
	
	Map perFactoryWorkingCopies;
	IBufferFactory factory;
	IProblemRequestor problemRequestor;
	
	public CreateSharedWorkingCopyOperation(ICompilationUnit originalElement, Map perFactoryWorkingCopies, IBufferFactory factory, IProblemRequestor problemRequestor) {
		super(new IJavaElement[] {originalElement});
		this.perFactoryWorkingCopies = perFactoryWorkingCopies;
		this.factory = factory;
		this.problemRequestor = problemRequestor;
	}
	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		ICompilationUnit cu = getCompilationUnit();
		WorkingCopy workingCopy = (WorkingCopy)cu.getWorkingCopy(fMonitor, this.factory, this.problemRequestor);
		this.perFactoryWorkingCopies.put(cu, workingCopy);

		if (CompilationUnit.SHARED_WC_VERBOSE) {
			System.out.println("Creating shared working copy " + workingCopy.toStringWithAncestors()); //$NON-NLS-1$
		}

		// report added java delta
		JavaElementDelta delta = new JavaElementDelta(this.getJavaModel());
		delta.added(workingCopy);
		addDelta(delta);

		fResultElements = new IJavaElement[] {workingCopy};
	}
	/**
	 * Returns the compilation unit this operation is working on.
	 */
	protected ICompilationUnit getCompilationUnit() {
		return (ICompilationUnit)getElementToProcess();
	}

}
