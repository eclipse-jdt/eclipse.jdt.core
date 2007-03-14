/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Reconcile a working copy and signal the changes through a delta.
 * <p>
 * High level summmary of what a reconcile does:
 * <ul>
 * <li>populates the model with the new working copy contents</li>
 * <li>fires a fine grained delta (flag F_FINE_GRAINED) describing the difference between the previous content
 *      and the new content (which method was added/removed, which field was changed, etc.)</li>
 * <li>computes problems and reports them to the IProblemRequestor (begingReporting(), n x acceptProblem(...), endReporting()) iff
 *     	(working copy is not consistent with its buffer || forceProblemDetection is set)
 * 		&& problem requestor is active
 * </li>
 * <li>produces a DOM AST (either JLS_2, JLS_3 or NO_AST) that is resolved if flag is set</li>
 * <li>notifies compilation participants of the reconcile allowing them to participate in this operation and report problems</li>
 * </ul>
 */
public class ReconcileWorkingCopyOperation extends JavaModelOperation {
	public static boolean PERF = false;

	public int astLevel;
	public boolean resolveBindings;
	public HashMap problems;
	boolean forceProblemDetection;
	boolean enableStatementsRecovery;
	WorkingCopyOwner workingCopyOwner;
	public org.eclipse.jdt.core.dom.CompilationUnit ast;
	public JavaElementDeltaBuilder deltaBuilder;

	public ReconcileWorkingCopyOperation(IJavaElement workingCopy, int astLevel, boolean forceProblemDetection, boolean enableStatementsRecovery, WorkingCopyOwner workingCopyOwner) {
		super(new IJavaElement[] {workingCopy});
		this.astLevel = astLevel;
		this.forceProblemDetection = forceProblemDetection;
		this.enableStatementsRecovery = enableStatementsRecovery;
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		checkCanceled();
		try {
			beginTask(Messages.element_reconciling, 2);

			CompilationUnit workingCopy = getWorkingCopy();
			boolean wasConsistent = workingCopy.isConsistent();
			IProblemRequestor problemRequestor = workingCopy.getPerWorkingCopyInfo();
			if (problemRequestor != null) {
				problemRequestor =  ((JavaModelManager.PerWorkingCopyInfo)problemRequestor).getProblemRequestor();
			}
			IProblemRequestor ownerProblemRequestor = this.workingCopyOwner.getProblemRequestor(workingCopy);
			this.resolveBindings |= problemRequestor != null && problemRequestor.isActive();

			// create the delta builder (this remembers the current content of the cu)
			this.deltaBuilder = new JavaElementDeltaBuilder(workingCopy);

			// make working copy consistent if needed and compute AST if needed
			makeConsistent(workingCopy, problemRequestor);

			// notify reconcile participants
			notifyParticipants(workingCopy);

			// recreate ast if needed
			if (this.ast == null && (this.astLevel > ICompilationUnit.NO_AST || this.resolveBindings))
				makeConsistent(workingCopy, problemRequestor);

			// report problems
			if (this.problems != null && (this.forceProblemDetection || !wasConsistent)) {
				if (problemRequestor != null) {
					reportProblems(workingCopy, problemRequestor);
				}
				if (ownerProblemRequestor != null && ownerProblemRequestor != problemRequestor) {
					reportProblems(workingCopy, ownerProblemRequestor);
				}
			}

			// report delta
			JavaElementDelta delta = this.deltaBuilder.delta;
			if (delta != null) {
				addReconcileDelta(workingCopy, delta);
			}
		} finally {
			done();
		}
	}

	/**
	 * Report working copy problems to a given requestor.
	 *
	 * @param workingCopy
	 * @param problemRequestor
	 */
	private void reportProblems(CompilationUnit workingCopy, IProblemRequestor problemRequestor) {
		try {
			problemRequestor.beginReporting();
			for (Iterator iteraror = this.problems.values().iterator(); iteraror.hasNext();) {
				CategorizedProblem[] categorizedProblems = (CategorizedProblem[]) iteraror.next();
				if (categorizedProblems == null) continue;
				for (int i = 0, length = categorizedProblems.length; i < length; i++) {
					CategorizedProblem problem = categorizedProblems[i];
					if (JavaModelManager.VERBOSE){
						System.out.println("PROBLEM FOUND while reconciling : " + problem.getMessage());//$NON-NLS-1$
					}
					if (this.progressMonitor != null && this.progressMonitor.isCanceled()) break;
					problemRequestor.acceptProblem(problem);
				}
			}
		} finally {
			problemRequestor.endReporting();
		}
	}

	/**
	 * Returns the working copy this operation is working on.
	 */
	protected CompilationUnit getWorkingCopy() {
		return (CompilationUnit)getElementToProcess();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.JavaModelOperation#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/*
	 * Makes the given working copy consistent, computes the delta and computes an AST if needed.
	 * Returns the AST.
	 */
	public org.eclipse.jdt.core.dom.CompilationUnit makeConsistent(CompilationUnit workingCopy, IProblemRequestor problemRequestor) throws JavaModelException {
		if (!workingCopy.isConsistent()) {
			// make working copy consistent
			if (this.problems == null) this.problems = new HashMap();
			this.ast = workingCopy.makeConsistent(this.astLevel, this.resolveBindings, this.enableStatementsRecovery, this.problems, this.progressMonitor);
			this.deltaBuilder.buildDeltas();
			if (this.ast != null && this.deltaBuilder.delta != null)
				this.deltaBuilder.delta.changedAST(this.ast);
			return this.ast;
		}
		if (this.ast != null) return this.ast; // no need to recompute AST if known already
		if (this.forceProblemDetection || this.resolveBindings) {
			if (JavaProject.hasJavaNature(workingCopy.getJavaProject().getProject())) {
				HashMap problemMap;
				if (this.problems == null) {
					problemMap = new HashMap();
					if (this.forceProblemDetection)
						this.problems = problemMap;
				} else
					problemMap = this.problems;
			    CompilationUnitDeclaration unit = null;
			    try {
			    	// find problems
					char[] contents = workingCopy.getContents();
					unit =
						CompilationUnitProblemFinder.process(
							workingCopy,
							contents,
							this.workingCopyOwner,
							problemMap,
							this.astLevel != ICompilationUnit.NO_AST/*creating AST if level is not NO_AST */,
							this.enableStatementsRecovery,
							this.progressMonitor);
					if (this.progressMonitor != null) this.progressMonitor.worked(1);

					// create AST if needed
					if (this.astLevel != ICompilationUnit.NO_AST && unit != null) {
						Map options = workingCopy.getJavaProject().getOptions(true);
						this.ast =
							AST.convertCompilationUnit(
								this.astLevel,
								unit,
								contents,
								options,
								true/*isResolved*/,
								workingCopy,
								this.progressMonitor);
						if (this.ast != null) {
							this.deltaBuilder.delta = new JavaElementDelta(workingCopy);
							this.deltaBuilder.delta.changedAST(this.ast);
						}
						if (this.progressMonitor != null) this.progressMonitor.worked(1);
					}
			    } catch (JavaModelException e) {
			    	if (JavaProject.hasJavaNature(workingCopy.getJavaProject().getProject()))
			    		throw e;
			    	// else JavaProject has lost its nature (or most likely was closed/deleted) while reconciling -> ignore
			    	// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=100919)
			    } finally {
			        if (unit != null) {
			            unit.cleanUp();
			        }
			    }
			} // else working copy not in a Java project
			return this.ast;
		}
		return null;
	}
	private void notifyParticipants(final CompilationUnit workingCopy) {
		IJavaProject javaProject = getWorkingCopy().getJavaProject();
		CompilationParticipant[] participants = JavaModelManager.getJavaModelManager().compilationParticipants.getCompilationParticipants(javaProject);
		if (participants == null) return;

		final ReconcileContext context = new ReconcileContext(this, workingCopy);
		for (int i = 0, length = participants.length; i < length; i++) {
			final CompilationParticipant participant = participants[i];
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					if (exception instanceof Error) {
						throw (Error) exception; // errors are not supposed to be caught
					} else if (exception instanceof OperationCanceledException)
						throw (OperationCanceledException) exception;
					else if (exception instanceof UnsupportedOperationException) {
						// might want to disable participant as it tried to modify the buffer of the working copy being reconciled
						Util.log(exception, "Reconcile participant attempted to modify the buffer of the working copy being reconciled"); //$NON-NLS-1$
					} else
						Util.log(exception, "Exception occurred in reconcile participant"); //$NON-NLS-1$
				}
				public void run() throws Exception {
					participant.reconcile(context);
				}
			});
		}
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
