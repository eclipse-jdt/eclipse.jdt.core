/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * Handle for an import declaration. Info object is a ImportDeclarationElementInfo.
 * @see IImportDeclaration
 */

/* package */ class ImportDeclaration extends SourceRefElement implements IImportDeclaration {


/**
 * Constructs an ImportDeclaration in the given import container
 * with the given name.
 */
protected ImportDeclaration(IImportContainer parent, String name) {
	super(IMPORT_DECLARATION, parent, name);
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	return (node.getNodeType() == IDOMNode.IMPORT) && getElementName().equals(node.getName());
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.IImportDeclaration#getFlags()
 */
public int getFlags() throws JavaModelException {
	ImportDeclarationElementInfo info = (ImportDeclarationElementInfo)getElementInfo();
	return info.getModifiers();
}

/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_IMPORTDECLARATION;
}
/**
 * Returns true if the import is on-demand (ends with ".*")
 */
public boolean isOnDemand() {
	return fName.endsWith(".*"); //$NON-NLS-1$
}
/**
 */
public String readableName() {

	return null;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	buffer.append("import "); //$NON-NLS-1$
	buffer.append(getElementName());
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
}
