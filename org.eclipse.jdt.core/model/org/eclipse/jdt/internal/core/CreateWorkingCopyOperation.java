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
 * Creates a new working copy and signal its addition through a delta.
 */
public class CreateWorkingCopyOperation extends JavaModelOperation {
	
	Map perFactoryWorkingCopies;
	IBufferFactory factory;
	IProblemRequestor problemRequestor;
	
	/*
	 * Creates a working copy from the given original cu and the given buffer factory.
	 * perFactoryWorkingCopies map is not null if the working copy is a shared working copy.
	 */
	public CreateWorkingCopyOperation(ICompilationUnit originalElement, Map perFactoryWorkingCopies, IBufferFactory factory, IProblemRequestor problemRequestor) {
		super(new IJavaElement[] {originalElement});
		this.perFactoryWorkingCopies = perFactoryWorkingCopies;
		this.factory = factory;
		this.problemRequestor = problemRequestor;
	}
	protected void executeOperation() throws JavaModelException {
		ICompilationUnit cu = getCompilationUnit();

		WorkingCopy workingCopy = new WorkingCopy((IPackageFragment)cu.getParent(), cu.getElementName(), this.factory, this.problemRequestor);
		// open the working copy now to ensure contents are that of the current state of this element
		workingCopy.open(fMonitor);

		if (this.perFactoryWorkingCopies != null) {
			this.perFactoryWorkingCopies.put(cu, workingCopy);
			if (CompilationUnit.SHARED_WC_VERBOSE) {
				System.out.println("Creating shared working copy " + workingCopy.toStringWithAncestors()); //$NON-NLS-1$
			}
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
	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}

}
