package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;

import java.util.Vector;
import java.util.Enumeration;

/**
 * An <code>IProblemReporter</code> keeps track of problems reported
 * by the image builder.
 */
public interface IProblemReporter {
/**
 * Creates a copy of this problem reporter.
 */
public IProblemReporter copy();
/**
 * Returns an enumeration of all problems with elements of the image,
 * but not with the image itself.
 * Returns an Enumeration of IProblemDetail.
 */
public Enumeration getAllProblems();
/**
 * Returns the problems with the image itself.  Returns null if no
 * problems exist for the image.  An example of such a problem is
 * duplicate types in a package.
 * Returns an Enumeration of IProblemDetail.
 */
public Enumeration getImageProblems();
/**
 * Returns an enumeration of problem table keys (sourceID objects)
 */
public Enumeration getProblemKeys();
/**
 * Returns an enumeration of problems for a given source. 
 */
public Enumeration getProblems(Object sourceID);
/**
 * Returns a vector of problems for a given source,
 * or null if no problems.
 */
public Vector getProblemVector(Object sourceID);
/**
 * Returns whether the given source has any problems.
 */
public boolean hasProblems(Object sourceID);
/**
 * Adds a problem to this problem reporter for a source with the given ID. 
 * If the problem is a duplicate, it is not added and an error message is 
 * printed to the console.
 */
public void putProblem(Object sourceID, IProblemDetail problem);
/**
 * Removes all problems except syntax errors for the given source.
 */
public void removeNonSyntaxErrors(Object sourceID);
/**
 * Removes all problems for the given source.
 */
public void removeProblems(Object sourceID);
/**
 * Removes all syntax errors for the given source.
 */
public void removeSyntaxErrors(Object sourceID);

/**
 * Initializes this problem reporter for the given project.
 * This is used only when deserializing a state.
 */
public void initialize(IProject project, IDevelopmentContext devContext);
}
