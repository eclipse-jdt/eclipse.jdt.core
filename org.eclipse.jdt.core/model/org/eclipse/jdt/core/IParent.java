package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;
 
/**
 * Common protocol for Java elements that contain other Java elements.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IParent {
/**
 * Returns the immediate children of this element.
 * Unless otherwise specified by the implementing element,
 * the children are in no particular order.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
IJavaElement[] getChildren() throws JavaModelException;
/**
 * Returns whether this element has one or more immediate children.
 * This is a convenience method, and may be more efficient than
 * testing whether <code>getChildren</code> is an empty array.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
boolean hasChildren() throws JavaModelException;
}
