package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * The listener event which is sent to all <code>ISearchListener</code>s
 * when an <code>ISearch</code> has a result to report.
 *
 * @see ISearch
 * @see ISearchListener
 */
public class SearchResultEvent extends java.util.EventObject {
	IHandle result;
	int[] fMatchingPositions;
/**
 * Creates a new <code>SearchResultEvent</code> with the handle
 * of the result that has been found.
 */
public SearchResultEvent(IHandle result) {
	this(result, null);
}
/**
 * Creates a new <code>SearchResultEvent</code> with the handle
 * of the result that has been found.
 */
public SearchResultEvent(IHandle result, int[] positions) {
	super(result);
	this.result = result;
	this.fMatchingPositions = positions;
}
/**
 * Returns the matching char offset positions within the workspace element.
 * Returns null if matching positions are not known.
 */
public int[] getMatchingPositions() {
	return fMatchingPositions;
}
/**
 * Returns the result that this progress event represents.
 */
public IHandle getResult() {
	return result;
}
public String toString() {
	return result.toString();
}
}
