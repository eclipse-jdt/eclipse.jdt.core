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
package org.eclipse.jdt.core.search;

import org.eclipse.core.runtime.CoreException;

/**
 * TODO add spec
 */
/**
 * A <code>SearchRequestor</code> collects search results from a <code>search</code>
 * query to a <code>SearchEngine</code>. Clients must implement this interface and pass
 * an instance to the <code>search(...)</code> methods. When a search starts, the <code>aboutToStart()</code>
 * method is called, then 0 or more call to <code>accept(...)</code> are done, finally the
 * <code>done()</code> method is called.
 * <p>
 * Results provided to this collector may be accurate - in this case they have an <code>EXACT_MATCH</code> accuracy -
 * or they might be potential matches only - they have a <code>POTENTIAL_MATCH</code> accuracy. This last
 * case can occur when a problem prevented the <code>SearchEngine</code> from resolving the match.
 * </p>
 * <p>
 * The order of the results is unspecified. Clients must not rely on this order to display results, 
 * but they should sort these results (for example, in syntactical order).
 * <p>
 * The <code>SearchRequestor</code> is also used to provide a progress monitor to the 
 * <code>SearchEngine</code>.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see SearchEngine#search
 * @since 3.0
 */
public abstract class SearchRequestor {

	/**expected detail level */
	public static final int D_LOCATION = 8;
	public static final int D_NAME = 1;
	public static final int D_PATH = 2;
	public static final int D_POSITION = 4;

	// answer false if requesting to cancel
	public abstract boolean acceptSearchMatch(SearchMatch match) throws CoreException;

	/**
	 * Notification sent before starting the search action.
	 * Typically, this would tell a search requestor to clear previously recorded search results.
	 */
	public abstract void beginReporting();

	/**
	 * Notification sent after having completed the search action.
	 * Typically, this would tell a search requestor collector that no more results  should be expected in this
	 * iteration.
	 */
	public abstract void endReporting();

	/**
	 * Intermediate notification sent when a given participant is starting to contribute.
	 */
	public abstract void enterParticipant(SearchParticipant participant);

	/**
	 * Intermediate notification sent when a given participant is finished contributing.
	 */
	public abstract void exitParticipant(SearchParticipant participant);

//	/**
//	 * Client can indicate how much detail is expected
//	 */
//	public int getRequestedDetailLevel() {
//		// by default, request all details
//		return D_NAME | D_PATH | D_POSITION | D_LOCATION;
//	}
}
