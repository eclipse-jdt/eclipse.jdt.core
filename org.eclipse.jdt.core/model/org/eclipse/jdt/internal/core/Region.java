package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @see IRegion
 */

public class Region implements IRegion {

	/**
	 * A collection of the top level elements
	 * that have been added to the region
	 */
	protected Vector fRootElements;
	/**
	 * Creates an empty region.
	 *
	 * @see IRegion
	 */
	public Region() {
		fRootElements = new Vector(1);
	}

	/**
	 * @see IRegion#add(IJavaElement)
	 */
	public void add(IJavaElement element) {
		if (!contains(element)) {
			//"new" element added to region
			removeAllChildren(element);
			fRootElements.addElement(element);
			fRootElements.trimToSize();
		}
	}

	/**
	 * @see IRegion
	 */
	public boolean contains(IJavaElement element) {

		int size = fRootElements.size();
		Vector parents = getAncestors(element);

		for (int i = 0; i < size; i++) {
			IJavaElement aTop = (IJavaElement) fRootElements.elementAt(i);
			if (aTop.equals(element)) {
				return true;
			}
			for (int j = 0, pSize = parents.size(); j < pSize; j++) {
				if (aTop.equals(parents.elementAt(j))) {
					//an ancestor is already included
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a collection of all the parents of this element
	 * in bottom-up order.
	 *
	 */
	private Vector getAncestors(IJavaElement element) {
		Vector parents = new Vector();
		IJavaElement parent = element.getParent();
		while (parent != null) {
			parents.addElement(parent);
			parent = parent.getParent();
		}
		parents.trimToSize();
		return parents;
	}

	/**
	 * @see IRegion
	 */
	public IJavaElement[] getElements() {
		int size = fRootElements.size();
		IJavaElement[] roots = new IJavaElement[size];
		for (int i = 0; i < size; i++) {
			roots[i] = (IJavaElement) fRootElements.elementAt(i);
		}

		return roots;
	}

	/**
	 * @see IRegion#remove(IJavaElement)
	 */
	public boolean remove(IJavaElement element) {

		removeAllChildren(element);
		return fRootElements.removeElement(element);
	}

	/**
	 * Removes any children of this element that are contained within this
	 * region as this parent is about to be added to the region.
	 *
	 * <p>Children are all children, not just direct children.
	 */
	private void removeAllChildren(IJavaElement element) {
		if (element instanceof IParent) {
			Vector newRootElements = new Vector();
			for (int i = 0, size = fRootElements.size(); i < size; i++) {
				IJavaElement currentRoot = (IJavaElement) fRootElements.elementAt(i);
				//walk the current root hierarchy
				IJavaElement parent = currentRoot.getParent();
				boolean isChild = false;
				while (parent != null) {
					if (parent.equals(element)) {
						isChild = true;
						break;
					}
					parent = parent.getParent();
				}
				if (!isChild) {
					newRootElements.addElement(currentRoot);
				}
			}
			fRootElements = newRootElements;
		}
	}

	/**
	 * Returns a printable representation of this region.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		IJavaElement[] roots = getElements();
		buffer.append('[');
		for (int i = 0; i < roots.length; i++) {
			buffer.append(roots[i].getElementName());
			if (i < (roots.length - 1)) {
				buffer.append(", ");
			}
		}
		buffer.append(']');
		return buffer.toString();
	}

}
