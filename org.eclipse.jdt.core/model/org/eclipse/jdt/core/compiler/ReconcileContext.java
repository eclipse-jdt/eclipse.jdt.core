/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    IBM - renamed from PreReconcileCompilationResult to ReconcileContext
 *    IBM - rewrote spec
 *    
 *******************************************************************************/

package org.eclipse.jdt.core.compiler;

import java.util.HashMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.ReconcileWorkingCopyOperation;

/**
 * The context of a reconcile event that is notified to interested compilation 
 * participants while a reconcile operation is running.
 * <p>
 * A reconcile participant can get the AST for the reconcile-operation using
 * {@link #getAST(int, boolean)}. If the participant modifies in any way the AST 
 * (either by modifying the source of the working copy, or modifying another entity 
 * that would result in different bindings for the AST), it is expected to reset the 
 * AST in the context using {@link #resetAST()}.
 * </p><p>
 * A reconcile participant can also create and return problems using 
 * {@link #putProblems(String, CategorizedProblem[])}. These problems are then reported 
 * to the problem requestor of the reconcile operation.
 * </p><p>
 * This class is not intended to be instanciated or subclassed by clients.
 * </p>
 * 
 * @see CompilationParticipant#reconcile(ReconcileContext)
 * @since 3.2
 */
public class ReconcileContext {
	
	private ReconcileWorkingCopyOperation operation;
	private CompilationUnit workingCopy;

/**
 * Creates a reconcile context for the given reconcile operation.
 * <p>
 * This constructor is not intended to be called by clients.
 * </p>
 * 
 * @param operation the reconcile operation
 */
public ReconcileContext(ReconcileWorkingCopyOperation operation, CompilationUnit workingCopy) {
	this.operation = operation;
	this.workingCopy = workingCopy;
}

/**
 * Returns the AST created from the current state of the working copy.
 * Creates one if none exists yet.
 * Returns <code>null</code> if the current state of the working copy
 * doesn't allow the AST to be created (e.g. if the working copy's content 
 * cannot be parsed).
 * 
 * @param astLevel the level of AST requested
 * @param resolveBindings whether the bindings in the returned AST should be resolved
 * @return the AST created from the current state of the working copy,
 *   or <code>null</code> if none could be created
 * @exception JavaModelException  if the contents of the working copy
 *		cannot be accessed. Reasons include:
 * <ul>
 * <li> The working copy does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * </ul>
 */
// TODO do we really want individual astLevel for participants ? or should they implicitly get the one from ongoing reconcile operation ?
public org.eclipse.jdt.core.dom.CompilationUnit getAST(int astLevel, boolean resolveBindings) throws JavaModelException {
	if (this.operation.astLevel < astLevel) {
		this.operation.astLevel = astLevel;
		this.operation.ast = null; // force the AST to be re-created since the ast level was insufficient the first time
	}
	if (!this.operation.resolveBindings & resolveBindings) {
		this.operation.resolveBindings = true;
		this.operation.ast = null; // force the AST to be re-created since bindings were not computed the first time
	}
	org.eclipse.jdt.core.dom.CompilationUnit result = this.operation.makeConsistent(this.workingCopy, null/*don't report problems to the working copy's problem requestor*/);
	if (result == null)
		result = this.operation.createAST(this.workingCopy);
	return result;
}

/**
 * Returns the delta describing the change to the working copy being reconciled.
 * Returns <code>null</code> if there is no change.
 *
 * @return the delta describing the change, or <code>null</code> if none
 */
public IJavaElementDelta getDelta() {
	return this.operation.deltaBuilder.delta;
}

/**
 * Returns the problems to be reported to the problem requestor of the reconcile operation
 * for the given marker type.
 * Returns <code>null</code> if no problems need to be reported for this marker type.
 * 
 * @param markerType the given marker type
 * @return problems to be reported to the problem requesto
 */
public CategorizedProblem[] getProblems(String markerType) {
	if (this.operation.problems == null) return null;
	return (CategorizedProblem[]) this.operation.problems.get(markerType);
}

/**
 * Returns the working copy this context refers to.
 * 
 * @return the working copy this context refers to
 */
public ICompilationUnit getWorkingCopy() {
	return this.workingCopy;
}

/**
 * Resets the AST carried by this context.
 * A compilation participant that modifies the buffer of the working copy,
 * or that modifies another entity that would result in different bindings for the AST
 * is expected to reset the AST on this context, so that other participants
 * don't get a stale AST.
 */
public void resetAST() {
	this.operation.ast = null;
	putProblems(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, null);
	putProblems(IJavaModelMarker.TASK_MARKER, null);
}

/**
 * Sets the problems to be reported to the problem requestor of the reconcile operation
 * for the given marker type.
 * <code>null</code> indicates that no problems need to be reported.
 * 
 * @param markerType the marker type of the given problems
 * @param problems  the problems to be reported to the problem requestor of the reconcile operation,
 *   or <code>null</code> if none
 */
public void putProblems(String markerType, CategorizedProblem[] problems) {
	if (this.operation.problems == null)
		this.operation.problems = new HashMap();
	this.operation.problems.put(markerType, problems);
}

}
