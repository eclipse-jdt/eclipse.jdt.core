package org.eclipse.jdt.internal.core.builder;

public interface ISearchListener extends java.util.EventListener {
	/**
	 * Reports that the search has been cancelled and is no longer running.
	 */
	public void cancelled();
	/**
	 * Reports that the search has finished.
	 */
	public void done();
	/**
	 * Reports that the search has moved to the type or package named
	 * in the event.
	 *
	 * @see SearchProgressEvent
	 */
	public void progressUpdated(SearchProgressEvent e);
	/**
	 * Reports that the search has found the result given
	 * in the event.
	 *
	 * @see SearchResultEvent
	 */
	public void resultsUpdated(SearchResultEvent e);
}
