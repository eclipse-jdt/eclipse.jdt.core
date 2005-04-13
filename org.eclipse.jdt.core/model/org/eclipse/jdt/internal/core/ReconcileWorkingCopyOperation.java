/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Map;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * Reconcile a working copy and signal the changes through a delta.
 */
public class ReconcileWorkingCopyOperation extends JavaModelOperation {
	public static boolean PERF = false;
	
	boolean createAST;
	int astLevel;
	boolean forceProblemDetection;
	WorkingCopyOwner workingCopyOwner;
	org.eclipse.jdt.core.dom.CompilationUnit ast;
	
	public ReconcileWorkingCopyOperation(IJavaElement workingCopy, boolean creatAST, int astLevel, boolean forceProblemDetection, WorkingCopyOwner workingCopyOwner) {
		super(new IJavaElement[] {workingCopy});
		this.createAST = creatAST;
		this.astLevel = astLevel;
		this.forceProblemDetection = forceProblemDetection;
		this.workingCopyOwner = workingCopyOwner;
	}
	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		if (this.progressMonitor != null){
			if (this.progressMonitor.isCanceled()) 
				throw new OperationCanceledException();
			this.progressMonitor.beginTask(Messages.element_reconciling, 2); 
		}
	
		CompilationUnit workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		try {
			if (!wasConsistent) {
				// create the delta builder (this remembers the current content of the cu)
				JavaElementDeltaBuilder deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
				
				// update the element infos with the content of the working copy
				this.ast = workingCopy.makeConsistent(this.createAST, this.astLevel, this.progressMonitor);
				deltaBuilder.buildDeltas();

				if (progressMonitor != null) progressMonitor.worked(2);
			
				// register the deltas
				if (deltaBuilder.delta != null) {
					addReconcileDelta(workingCopy, deltaBuilder.delta);
				}
			} else {
				// force problem detection? - if structure was consistent
				if (forceProblemDetection) {
					IProblemRequestor problemRequestor = workingCopy.getPerWorkingCopyInfo();
					if (problemRequestor != null && problemRequestor.isActive()) {
					    CompilationUnitDeclaration unit = null;
					    try {
							problemRequestor.beginReporting();
							char[] contents = workingCopy.getContents();
							unit = CompilationUnitProblemFinder.process(workingCopy, contents, this.workingCopyOwner, problemRequestor, !this.createAST/*reset env if not creating AST*/, this.progressMonitor);
							problemRequestor.endReporting();
							if (progressMonitor != null) progressMonitor.worked(1);
							if (this.createAST && unit != null) {
								Map options = workingCopy.getJavaProject().getOptions(true);
								this.ast = AST.convertCompilationUnit(this.astLevel, unit, contents, options, true/*isResolved*/, workingCopy, this.progressMonitor);
								if (progressMonitor != null) progressMonitor.worked(1);
							}
					    } finally {
					        if (unit != null) {
					            unit.cleanUp();
					        }
					    }
					}
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
