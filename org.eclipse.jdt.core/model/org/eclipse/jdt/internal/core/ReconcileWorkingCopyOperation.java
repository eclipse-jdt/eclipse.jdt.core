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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Reconcile a working copy and signal the changes through a delta.
 */
public class ReconcileWorkingCopyOperation extends JavaModelOperation {
		
	boolean forceProblemDetection;
	
	public ReconcileWorkingCopyOperation(IJavaElement workingCopy, boolean forceProblemDetection) {
		super(new IJavaElement[] {workingCopy});
		this.forceProblemDetection = forceProblemDetection;
	}
	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		if (progressMonitor != null){
			if (progressMonitor.isCanceled()) return;
			progressMonitor.beginTask(Util.bind("element.reconciling"), 10); //$NON-NLS-1$
		}
	
		CompilationUnit workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		JavaElementDeltaBuilder deltaBuilder = null;
	
		try {
			// create the delta builder (this remembers the current content of the cu)
			if (!wasConsistent){
				deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
				
				// update the element infos with the content of the working copy
				workingCopy.makeConsistent(progressMonitor);
				deltaBuilder.buildDeltas();
		
			}
	
			if (progressMonitor != null) progressMonitor.worked(2);
			
			// force problem detection? - if structure was consistent
			if (forceProblemDetection && wasConsistent){
				if (progressMonitor != null && progressMonitor.isCanceled()) return;
		
				IProblemRequestor problemRequestor = workingCopy.getPerWorkingCopyInfo();
				if (problemRequestor != null && problemRequestor.isActive()){
					problemRequestor.beginReporting();
					CompilationUnitProblemFinder.process(workingCopy, problemRequestor, progressMonitor);
					problemRequestor.endReporting();
				}
			}
			
			// register the deltas
			if (deltaBuilder != null){
				if (deltaBuilder.delta != null) {
					addReconcileDelta(workingCopy, deltaBuilder.delta);
				}
			}
		} finally {
			if (progressMonitor != null) progressMonitor.done();
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
	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		CompilationUnit workingCopy = getWorkingCopy();
		if (!workingCopy.isWorkingCopy()) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, workingCopy); //was destroyed
		}
		return status;
	}


}
