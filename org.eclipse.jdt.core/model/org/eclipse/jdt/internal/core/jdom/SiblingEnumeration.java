package org.eclipse.jdt.internal.core.jdom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.jdom.IDOMNode;

import java.util.Enumeration;

/**
 * SiblingEnumeration provides an enumeration on a linked list
 * of sibling DOM nodes.
 *
 * @see java.util.Enumeration
 */

/* package */ class SiblingEnumeration implements Enumeration {

	/**
	 * The current location in the linked list
	 * of DOM nodes.
	 */
	protected IDOMNode fCurrentElement;
/**
 * Creates an enumeration of silbings starting at the given node.
 * If the given node is <code>null</code> the enumeration is empty.
 */
SiblingEnumeration(IDOMNode child) {
	fCurrentElement= child;
}
/**
 * @see java.util.Enumeration#hasMoreElements()
 */
public boolean hasMoreElements() {
	return fCurrentElement != null;
}
/**
 * @see java.util.Enumeration#nextElement()
 */
public Object nextElement() {
	IDOMNode curr=  fCurrentElement;
	if (curr != null) {
		fCurrentElement= fCurrentElement.getNextNode();
	}
	return curr;
}
}
