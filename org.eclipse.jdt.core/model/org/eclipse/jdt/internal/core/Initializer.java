/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IInitializer
 */

/* package */ class Initializer extends Member implements IInitializer {

protected Initializer(JavaElement parent, int count) {
	super(parent, ""); //$NON-NLS-1$
	// 0 is not valid: this first occurrence is occurrence 1.
	if (count <= 0)
		throw new IllegalArgumentException();
	this.occurrenceCount = count;
}
public boolean equals(Object o) {
	if (!(o instanceof Initializer)) return false;
	return super.equals(o);
}
/**
 * @see JavaElement#equalsDOMNode
 * @deprecated JDOM is obsolete
 */
// TODO - JDOM - remove once model ported off of JDOM
protected boolean equalsDOMNode(IDOMNode node) {
	if (node.getNodeType() == IDOMNode.INITIALIZER) {
		try {
			return node.getContents().trim().equals(getSource());
		} catch (JavaModelException e) {
			return false;
		}
	} else {
		return false;
	}
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return INITIALIZER;
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	StringBuffer buff= new StringBuffer(((JavaElement)getParent()).getHandleMemento());
	buff.append(getHandleMementoDelimiter());
	buff.append(this.occurrenceCount);
	return buff.toString();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_INITIALIZER;
}
public int hashCode() {
	return Util.combineHashCodes(this.parent.hashCode(), this.occurrenceCount);
}
/**
 */
public String readableName() {

	return ((JavaElement)getDeclaringType()).readableName();
}
/**
 * @see ISourceManipulation
 */
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
}
/**
 * @see IMember
 */
public ISourceRange getNameRange() {
	return null;
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner) {
		CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
		if (cu == null || cu.isPrimary()) return this;
	}
	IJavaElement primaryParent = this.parent.getPrimaryElement(false);
	return ((IType)primaryParent).getInitializer(this.occurrenceCount);
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		buffer.append("<initializer #"); //$NON-NLS-1$
		buffer.append(this.occurrenceCount);
		buffer.append("> (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append("<initializer #"); //$NON-NLS-1$
		buffer.append(this.occurrenceCount);
		buffer.append(">"); //$NON-NLS-1$
	} else {
		try {
			buffer.append("<"); //$NON-NLS-1$
			if (Flags.isStatic(this.getFlags())) {
				buffer.append("static "); //$NON-NLS-1$
			}
		buffer.append("initializer #"); //$NON-NLS-1$
		buffer.append(this.occurrenceCount);
		buffer.append(">"); //$NON-NLS-1$
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
