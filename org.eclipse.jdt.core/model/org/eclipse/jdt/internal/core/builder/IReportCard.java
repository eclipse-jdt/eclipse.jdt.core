package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

/**
 * Represents a workspace-based report card.
 */
public interface IReportCard {

/**
 * Calculates the paths for all resources that have a changed problem state between
 * this report card and previous.
 */
IPath[] changedPaths(IReportCard previous);
/**
 * Returns this report card's image context.
 */
IImageContext getImageContext();
/**
 * Returns the problem details pertaining to built objects having the
 * given path, or having the path as a prefix of their path.
 * That is, this returns all problems with the specified element and all its children.
 * Only objects within the scope of the report card's image context are considered.
 * If null is passed, all problems within the image context are returned,
 * as well as problems reported against the image itself (e.g. missing required classes).
 */
IProblemDetail[] getLeafProblemsFor(IPath path);
/**
 * Returns the paths of resources whose built objects have problems.
 * Only objects having the given path, or having the given path 
 * as a prefix of their path, are considered.
 * Only objects within the scope of the report card's image context are considered.
 * If null is passed, all objects within the image context are considered.
 * Note that problems which have no path, such as problems reported against
 * the image itself, are not considered.  
 * To get all problems, use <code>getLeafProblemsFor(IPath path)</code> instead.
 *
 * @see #getLeafProblemsFor
 */
IPath[] getProblemPaths(IPath path);
/**
 * Returns this report card's state.
 */
IState getState();
/**
 * Returns whether there are problems pertaining to built objects having the
 * given path, or having the given path as a prefix of their path.
 * See <code>getLeafProblemsFor(IPath path)</code> for more details.
 * 
 * @see #getLeafProblemsFor
 */
boolean hasLeafProblems(IPath path);
}
