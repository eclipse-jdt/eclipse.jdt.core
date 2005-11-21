/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API as ICompilationParticipant
 *    IBM - changed from interface ICompilationParticipant to abstract class CompilationParticipant
 *    IBM - rewrote spec
 *    
 *******************************************************************************/

package org.eclipse.jdt.core.compiler;

import org.eclipse.jdt.core.IJavaProject;

/**
 * A compilation participant is notified of events occuring during the compilation process.
 * The compilation process not only involves generating .class files (i.e. building), it also involve
 * cleaning the output dierectory, reconciling a working copy, etc.
 * So the notified events are the result of a build action, a clean action, a reconcile operation 
 * (for a working copy), etc.
 * <p>
 * Clients wishing to participate in the compilation process must suclass this class, and implement
 * {@link #isActive(IJavaProject)}, {@link #buildStarting(IJavaProject)}, 
 * {@link #reconcile(ReconcileContext)}, etc.
* </p><p>
 * This class is intended to be subclassed by clients.
 * </p>
 * @since 3.2
 */
public abstract class CompilationParticipant {

public static int READY_FOR_BUILD = 1;
public static int NEEDS_FULL_BUILD = 2;

/**
 * Notifies this participant that a build is about to start and provides
 * the opportunity for the participant to create missing source folders
 * for generated source files.
 * Only sent to participants interested in the project.
 * <p>
 * Default is to return <code>READY_FOR_BUILD</code>.
 * </p>
 * @param project the project about to build
 * @return READY_FOR_BUILD or NEEDS_FULL_BUILD
 */
public int buildStarting(IJavaProject project) {
	return READY_FOR_BUILD;
}

/**
 * Notifies this participant that a clean is about to start and provides
 * the opportunity for the participant to delete generated source files.
 * Only sent to participants interested in the project.
 * @param project the project about to be cleaned
 */
public void cleanStarting(IJavaProject project) {
	// do nothing by default
}

/**
 * Returns whether this participant is active for a given project.
 * <p>
 * Default is to return <code>false</code>.
 * </p><p>
 * For efficiency, participants that are not interested in the 
 * given project should return <code>false</code> for that project.
 * </p>
 * @param project the project to participate in
 * @return whether this participant is active for a given project
 */
public boolean isActive(IJavaProject project) {
	return false;
}
	
/**
 * Notifies this participant that a reconcile operation is happening. The participant can act on this reconcile
 * operation by using the given context. Other participant can then see the result of this participation
 * on this context.
 * <p>
 * Default is to do nothing.
 * </p>
 * @param context the reconcile context to act on
  */
// TODO specify whether clients can call this method
public void reconcile(ReconcileContext context) {
	// do nothing by default
}

}
