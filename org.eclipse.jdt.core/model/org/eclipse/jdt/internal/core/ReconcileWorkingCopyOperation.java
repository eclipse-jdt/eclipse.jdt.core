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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

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
		if (fMonitor != null){
			if (fMonitor.isCanceled()) return;
			fMonitor.beginTask(Util.bind("element.reconciling"), 10); //$NON-NLS-1$
		}
	
		WorkingCopy workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		JavaElementDeltaBuilder deltaBuilder = null;
	
		try {
			// create the delta builder (this remembers the current content of the cu)
			if (!wasConsistent){
				deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
				
				// update the element infos with the content of the working copy
				workingCopy.makeConsistent(fMonitor);
				deltaBuilder.buildDeltas();
		
			}
	
			if (fMonitor != null) fMonitor.worked(2);
			
			// force problem detection? - if structure was consistent
			if (forceProblemDetection && wasConsistent){
				if (fMonitor != null && fMonitor.isCanceled()) return;
		
				IProblemRequestor problemRequestor = workingCopy.getProblemRequestor();
				if (problemRequestor != null && problemRequestor.isActive()){
					problemRequestor.beginReporting();
					CompilationUnitProblemFinder.process(workingCopy, problemRequestor, fMonitor);
					problemRequestor.endReporting();
				}
			}
			
			// register the deltas
			if (deltaBuilder != null){
				if ((deltaBuilder.delta != null) && (deltaBuilder.delta.getAffectedChildren().length > 0)) {
					addReconcileDelta(workingCopy, deltaBuilder.delta);
				}
			}
		} finally {
			if (fMonitor != null) fMonitor.done();
		}
	}
	/**
	 * Returns the working copy this operation is working on.
	 */
	protected WorkingCopy getWorkingCopy() {
		return (WorkingCopy)getElementToProcess();
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
		WorkingCopy workingCopy = getWorkingCopy();
		if (workingCopy.useCount == 0) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, workingCopy); //was destroyed
		}
		return status;
	}


}
