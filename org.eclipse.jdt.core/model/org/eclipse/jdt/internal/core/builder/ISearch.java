package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * An <code>ISearch</code> represents an image search and its results.
 * <code>ISearch</code>es are created by <code>ISearchFactory</code> objects. 
 * They can be queried for results and report progress as the search runs. A 
 * search can be stopped before it finishes and can be run again. 
 * <code>ISearch</code> is implemented in a thread safe manner.
 * <p>
 * The search is not precise. Instead it provides a best-effort search. 
 * No occurrences of what is being searched for will be missed, and no duplicate 
 * results will be returned.  However, the search may occasionally return additional 
 * matches which don't actually relate to what was being searched for.
 * This will only be an issue when searching for source references. An 
 * example of this would be if a search for references to a field called <code>io</code> was
 * performed. Any methods which contained a reference to <code>java.io.</code><i>something</i> 
 * would be returned as matches, because in the source, it is unclear whether 
 * <code>io</code> is a field in some class called <code>java</code> or not.
 * <p>
 * ISearchFactory shows examples of using ISearch.
 *
 * @see ISearchFactory
 */
public interface ISearch extends Runnable
{
/**
 * Adds the specified action listener to receive progress and result events 
 * from this search. Progress events occur as a search examines the types
 * and packages in the search scope. Result events occur as a search finds
 * matches to the search criteria.
 * @param       listener the search listener
 * @see         ISearchListener 
 * @see         SearchProgressEvent
 * @see         SearchResultEvent
 * @see         ISearch#removeSearchListener
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
 *      (i.e., type named 'Foo') return type, number and type of parameters 
 *      (even if not used -- as in the case of search for type), and search 
 *      context (i.e., ISearchFactory.SEARCH_FOR_DECLS)
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
 * @param       listener the search listener
 * @see         ISearchListener 
 * @see         SearchProgressEvent
 * @see         SearchResultEvent
 * @see         ISearch#addSearchListener
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
