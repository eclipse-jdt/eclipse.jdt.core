package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * A Java model region describes a hierarchical set of elements.
 * Regions are often used to describe a set of elements to be considered
 * when performing operations; for example, the set of elements to be
 * considered during a search. A region may include elements from different
 * projects.
 * <p>
 * When an element is included in a region, all of its children
 * are considered to be included. Children of an included element 
 * <b>cannot</b> be selectively excluded.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * Instances can be created via the <code>JavaCore.newRegion</code>.
 * </p>
 *
 * @see JavaCore#newRegion
 */
public interface IRegion {
/**
 * Adds the given element and all of its descendents to this region.
 * If the specified element is already included, or one of its
 * ancestors is already included, this has no effect. If the element
 * being added is an ancestor of an element already contained in this
 * region, the ancestor subsumes the descendent.
 */
void add(IJavaElement element);
/**
 * Returns whether the given element is contained in this region.
 */
boolean contains(IJavaElement element);
/**
 * Returns the top level elements in this region.
 * All descendents of these elements are also included in this region.
 */
IJavaElement[] getElements();
/**
 * Removes the specified element from the region and returns
 * <code>true</code> if successful, <code>false</code> if the remove
 * fails. If an ancestor of the given element is included, the
 * remove fails (i.e. not possible to selectively
 * exclude descendants of included ancestors).
 */
boolean remove(IJavaElement element);
}
