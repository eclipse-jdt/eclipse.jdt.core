/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * A <code>SearchRequestor</code> collects search results from a <code>search</code>
 * query to a <code>SearchEngine</code>. Clients must implement this interface and pass
 * an instance to the <code>search(...)</code> methods. When a search starts, the <code>beginReporting()</code>
 * method is called, then 0 or more call to <code>acceptSearchMatch(...)</code> are done, finally the
 * <code>endReporting()</code> method is called.
 * <p>
 * Results provided to this collector may be accurate - in this case they have an <code>A_ACCURATE</code> accuracy -
 * or they might be potential matches only - they have a <code>A_INACCURATE</code> accuracy. This last
 * case can occur when a problem prevented the <code>SearchEngine</code> from resolving the match.
 * </p>
 * <p>
 * The order of the results is unspecified. Clients must not rely on this order to display results, 
 * but they should sort these results (for example, in syntactical order).
 * <p>
 * Clients may subclass this class.
 * </p>
 *
 * @see SearchEngine
 * @since 3.0
 */
public abstract class SearchRequestor {

	/**
	 * Accepts the given search match.
	 *
	 * @param match the found match
	 * @exception CoreException if this collector had a problem accepting the search result
	 */
	public abstract void acceptSearchMatch(SearchMatch match) throws CoreException;

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
	 * 
	 * @param participant the participant that is starting to contribute
	 */
	public abstract void enterParticipant(SearchParticipant participant);

	/**
	 * Intermediate notification sent when a given participant is finished contributing.
	 * 
	 * @param participant the participant that finished contributing
	 */
	public abstract void exitParticipant(SearchParticipant participant);

}
