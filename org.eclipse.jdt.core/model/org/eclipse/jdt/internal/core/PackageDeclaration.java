package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * @see IPackageDeclaration
 */

/* package */ class PackageDeclaration extends SourceRefElement implements IPackageDeclaration {
protected PackageDeclaration(ICompilationUnit parent, String name) {
	super(PACKAGE_DECLARATION, parent, name);
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	return (node.getNodeType() == IDOMNode.PACKAGE) && getElementName().equals(node.getName());
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEDECLARATION;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append("package "); //$NON-NLS-1$
	buffer.append(getElementName());
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
}
