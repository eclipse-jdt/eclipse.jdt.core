package org.eclipse.jdt.internal.core.jdom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.util.*;

/**
 * DOMImport provides an implementation of IDOMImport.
 *
 * @see IDOMImport
 * @see DOMNode
 */
class DOMImport extends DOMNode implements IDOMImport {
	/**
	 * Indicates if this import is an on demand type import
	 */
	protected boolean fOnDemand;
	/**
	 * Creates a new empty IMPORT node.
	 */
	DOMImport() {
		fName = "java.lang.*";
		setMask(MASK_DETAILED_SOURCE_INDEXES, true);
	}

	/**
	 * Creates a new detailed IMPORT document fragment on the given range of the document.
	 *
	 * @param document - the document containing this node's original contents
	 * @param sourceRange - a two element array of integers describing the
	 *		entire inclusive source range of this node within its document.
	 * 		Contents start on and include the character at the first position.
	 *		Contents end on and include the character at the last position.
	 *		An array of -1's indicates this node's contents do not exist
	 *		in the document.
	 * @param name - the identifier portion of the name of this node, or
	 *		<code>null</code> if this node does not have a name
	 * @param nameRange - a two element array of integers describing the
	 *		entire inclusive source range of this node's name within its document,
	 *		including any array qualifiers that might immediately follow the name
	 *		or -1's if this node does not have a name.
	 * @param onDemand - indicates if this import is an on demand style import
	 */
	DOMImport(
		char[] document,
		int[] sourceRange,
		String name,
		int[] nameRange,
		boolean onDemand) {
		super(document, sourceRange, name, nameRange);
		fOnDemand = onDemand;
		setMask(MASK_DETAILED_SOURCE_INDEXES, true);
	}

	/**
	 * Creates a new simple IMPORT document fragment on the given range of the document.
	 *
	 * @param document - the document containing this node's original contents
	 * @param sourceRange - a two element array of integers describing the
	 *		entire inclusive source range of this node within its document.
	 * 		Contents start on and include the character at the first position.
	 *		Contents end on and include the character at the last position.
	 *		An array of -1's indicates this node's contents do not exist
	 *		in the document.
	 * @param name - the identifier portion of the name of this node, or
	 *		<code>null</code> if this node does not have a name
	 * @param onDemand - indicates if this import is an on demand style import
	 */
	DOMImport(char[] document, int[] sourceRange, String name, boolean onDemand) {
		this(document, sourceRange, name, new int[] { -1, -1 }, onDemand);
		fOnDemand = onDemand;
		setMask(MASK_DETAILED_SOURCE_INDEXES, false);
	}

	/**
	 * @see DOMNode#appendFragmentedContents(CharArrayBuffer)
	 */
	protected void appendFragmentedContents(CharArrayBuffer buffer) {
		if (fNameRange[0] < 0) {
			buffer.append("import ").append(fName).append(';').append(
				JavaModelManager.LINE_SEPARATOR);
		} else {
			buffer.append(fDocument, fSourceRange[0], fNameRange[0] - fSourceRange[0]);
			//buffer.append(fDocument, fNameRange[0], fNameRange[1] - fNameRange[0] + 1);
			buffer.append(fName);
			buffer.append(fDocument, fNameRange[1] + 1, fSourceRange[1] - fNameRange[1]);
		}
	}

	/** 
	 * @see IDOMNode#getContents()
	 */
	public String getContents() {
		if (fName == null) {
			return null;
		} else {
			return super.getContents();
		}
	}

	/**
	 * @see DOMNode#getDetailedNode()
	 */
	protected DOMNode getDetailedNode() {
		return (DOMNode) getFactory().createImport(getContents());
	}

	/**
	 * @see IDOMNode#getJavaElement
	 */
	public IJavaElement getJavaElement(IJavaElement parent)
		throws IllegalArgumentException {
		if (parent.getElementType() == IJavaElement.COMPILATION_UNIT) {
			return ((ICompilationUnit) parent).getImport(getName());
		} else {
			throw new IllegalArgumentException("Illegal parent argument");
		}
	}

	/**
	 * @see IDOMNode#getNodeType()
	 */
	public int getNodeType() {
		return IDOMNode.IMPORT;
	}

	/**
	 * @see IDOMImport#isOnDemand()
	 */
	public boolean isOnDemand() {
		return fOnDemand;
	}

	/**
	 * @see DOMNode
	 */
	protected DOMNode newDOMNode() {
		return new DOMImport();
	}

	/**
	 * @see IDOMNode#setName(char[])
	 */
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("illegal to set name to null");
		}
		becomeDetailed();
		super.setName(name);
		fOnDemand = name.endsWith(".*");
	}

	/**
	 * @see IDOMNode#toString()
	 */
	public String toString() {
		return "IMPORT: " + getName();
	}

}
