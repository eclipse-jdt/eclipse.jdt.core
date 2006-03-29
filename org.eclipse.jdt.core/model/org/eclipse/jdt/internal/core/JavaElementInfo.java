/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Holds cached structure and properties for a Java element.
 * Subclassed to carry properties for specific kinds of elements.
 */
/* package */ class JavaElementInfo {

	/**
	 * Collection of handles of immediate children of this
	 * object. This is an empty array if this element has
	 * no children.
	 */
	protected IJavaElement[] children;

	/**
	 * Shared empty collection used for efficiency.
	 */
	static Object[] NO_NON_JAVA_RESOURCES = new Object[] {};	
	
	protected JavaElementInfo() {
		this.children = JavaElement.NO_ELEMENTS;
	}
	public void addChild(IJavaElement child) {
		if (this.children == JavaElement.NO_ELEMENTS) {
			setChildren(new IJavaElement[] {child});
		} else {
			if (!includesChild(child)) {
				setChildren(growAndAddToArray(this.children, child));
			}
		}
	}
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}
	public IJavaElement[] getChildren() {
		return this.children;
	}
	/**
	 * Adds the new element to a new array that contains all of the elements of the old array.
	 * Returns the new array.
	 */
	protected IJavaElement[] growAndAddToArray(IJavaElement[] array, IJavaElement addition) {
		IJavaElement[] old = array;
		array = new IJavaElement[old.length + 1];
		System.arraycopy(old, 0, array, 0, old.length);
		array[old.length] = addition;
		return array;
	}
	/**
	 * Returns <code>true</code> if this child is in my children collection
	 */
	protected boolean includesChild(IJavaElement child) {
		
		for (int i= 0; i < this.children.length; i++) {
			if (this.children[i].equals(child)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns an array with all the same elements as the specified array except for
	 * the element to remove. Assumes that the deletion is contained in the array.
	 */
	protected IJavaElement[] removeAndShrinkArray(IJavaElement[] array, IJavaElement deletion) {
		IJavaElement[] old = array;
		array = new IJavaElement[old.length - 1];
		int j = 0;
		for (int i = 0; i < old.length; i++) {
			if (!old[i].equals(deletion)) {
				array[j] = old[i];
			} else {
				System.arraycopy(old, i + 1, array, j, old.length - (i + 1));
				return array;
			}
			j++;
		}
		return array;
	}
	public void removeChild(IJavaElement child) {
		if (includesChild(child)) {
			setChildren(removeAndShrinkArray(this.children, child));
		}
	}
	public void setChildren(IJavaElement[] children) {
		this.children = children;
	}
}
