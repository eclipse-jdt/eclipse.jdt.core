package org.eclipse.jdt.internal.core.builder;

public interface ISearch extends Runnable {
	/**
	 * Adds the specified action listener to receive progress and result events 
	 * from this search. Progress events occur as a search examines the types
	 * and packages in the search scope. Result events occur as a search finds
	 * matches to the search criteria.
	 * @param		listener the search listener
	 * @see			ISearchListener 
	 * @see			SearchProgressEvent
	 * @see			SearchResultEvent
	 * @see			ISearch#removeSearchListener
	 */
	void addSearchListener(ISearchListener listener);
	/** 
	 * Cancels the search. If the search is not running, this operation has no 
	 * effect.
	 */
	void cancelSearch();
	/**
	 * Returns whether two ISearch objects are equal or not.
	 * Two ISearch objects are considered to be equal if
	 * they are the same object OR:
	 * <ul>
	 * <li> neither is equal to null AND
	 * <li> both searches have the same kind (i.e.. search for type), name 
	 * 		(i.e., type named 'Foo') return type, number and type of parameters 
	 * 		(even if not used -- as in the case of search for type), and search 
	 * 		context (i.e., ISearchFactory.SEARCH_FOR_DECLS)
	 * <li> their scopes are equal (contain the same IPackage and IType handles, in any order)
	 * </ul>
	 */

	boolean equals(Object obj);
	/**
	 * Returns the item <i>n</i>th item in the search result. This may be a field,
	 * method, constructor, package, or type object.
	 */
	IHandle getItem(int n);
	/**
	 * Returns the number of items in the search scope which matched the 
	 * search goal so far. Note that the result may not include all matches 
	 * within the search space if the search is still running or was cancelled.
	 */
	int getItemCount();
	/**
	 * Returns the positions for the <i>n</i>th item in the search result.
	 * If no positions exist, null is returned.
	 */
	int[] getPositions(int n);
	/**
	 * Returns whether a search is currently in progress.
	 */
	boolean isSearching();
	/**
	 * Removes the specified action listener so that it no longer
	 * receives progress and result events from the search.
	 * @param		listener the search listener
	 * @see			ISearchListener 
	 * @see			SearchProgressEvent
	 * @see			SearchResultEvent
	 * @see			ISearch#addSearchListener
	 */
	void removeSearchListener(ISearchListener listener);
	/**
	 * Performs the search. Upon return the results can be obtained via
	 * <code>getItem</code>. During the search, the results are made available
	 * as they are found, and progress and results are reported to registered
	 * <code>ISearchListener</code>s.
	 */
	void run();
}
