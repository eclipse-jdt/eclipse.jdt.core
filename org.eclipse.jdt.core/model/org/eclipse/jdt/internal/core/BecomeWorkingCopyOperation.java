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

import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Switch and ICompilationUnit to working copy mode
 * and signal the working copy addition through a delta.
 */
public class BecomeWorkingCopyOperation extends JavaModelOperation {
	
	Map perFactoryWorkingCopies;
	
	/*
	 * Creates a BecomeWorkingCopyOperation for the given working copy.
	 * perFactoryWorkingCopies map is not null if the working copy is a shared working copy.
	 */
	public BecomeWorkingCopyOperation(ICompilationUnit workingCopy, Map perFactoryWorkingCopies) {
		super(new IJavaElement[] {workingCopy});
		this.perFactoryWorkingCopies = perFactoryWorkingCopies;
	}
	protected void executeOperation() throws JavaModelException {

		// open the working copy now to ensure contents are that of the current state of this element
		CompilationUnit workingCopy = getWorkingCopy();
		workingCopy.openWhenClosed(new WorkingCopyElementInfo(), fMonitor);

		if (this.perFactoryWorkingCopies != null) {
			this.perFactoryWorkingCopies.put(workingCopy.getOriginalElement(), workingCopy);
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
