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

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

/**
 * A compilation participant is notified of events occuring during the compilation process.
 * The compilation process not only involves generating .class files (i.e. building), it also involve
 * cleaning the output directory, reconciling a working copy, etc.
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
 * Configures this participant, optionally changing the order of participation in the
 * given list.
 * <ul>
 * <li>A participant that modifies code is expected to move itself in the first position:
 * <pre>
 * participants.remove(this);
 * participants.add(0, this);
 * </pre>
 *  </li>
 * <li>A participant that wants to ensure that all participants that modify code
 * have run before itself should move itself in the last position. 
 * <pre>
 * participants.remove(this);
 * participants.add(this);
 * </pre>
 * For example, a participant that creates problems and that wants to ensure 
 * that its view of the world is not going to be modified should move itself in the
 * last position.</li>
 * <li>A participant that wants to run after a given
 * participant should move itself after the given participant.
 * <pre>
 * CompilationParticipant otherParticipant = ...
 * participants.remove(this);
 * participants.add(participants.indexOf(otherParticipant)+1, this);
 * </pre> 
 * </li>
 * </ul>
 * <p>
 * This method is called exactly once.
 * </p>
 * <p>
 * Default is to do nothing.
 * </p>
 * <p>
 * This method is not expected to be called by clients.
 * </p>
 * 
 * @param participants the list of all <code>CompilationParticipant</code>s.
 */
public void configure(List participants) {
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
public void reconcile(ReconcileContext context) {
	// do nothing by default
}

}
