package org.eclipse.jdt.core;

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
