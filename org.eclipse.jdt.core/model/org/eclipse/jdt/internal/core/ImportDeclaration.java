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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.*;

/**
 * Handle for an import declaration. Info object is a ImportDeclarationElementInfo.
 * @see IImportDeclaration
 */

/* package */ class ImportDeclaration extends SourceRefElement implements IImportDeclaration {


/**
 * Constructs an ImportDeclaration in the given import container
 * with the given name.
 */
protected ImportDeclaration(ImportContainer parent, String name) {
	super(parent, name);
}
public boolean equals(Object o) {
	if (!(o instanceof ImportDeclaration)) return false;
	return super.equals(o);
}
/**
 * @see JavaElement#equalsDOMNode
 * @deprecated JDOM is obsolete
 */
// TODO - JDOM - remove once model ported off of JDOM
protected boolean equalsDOMNode(IDOMNode node) {
	return (node.getNodeType() == IDOMNode.IMPORT) && getElementName().equals(node.getName());
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return IMPORT_DECLARATION;
}
/**
 * @see org.eclipse.jdt.core.IImportDeclaration#getFlags()
 */
public int getFlags() throws JavaModelException {
	ImportDeclarationElementInfo info = (ImportDeclarationElementInfo)getElementInfo();
	return info.getModifiers();
}
/**
 * @see JavaElement#getHandleMemento(StringBuffer)
 * For import declarations, the handle delimiter is associated to the import container already
 */
protected void getHandleMemento(StringBuffer buff) {
	((JavaElement)getParent()).getHandleMemento(buff);
	escapeMementoName(buff, getElementName());
	if (this.occurrenceCount > 1) {
		buff.append(JEM_COUNT);
		buff.append(this.occurrenceCount);
	}
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	// For import declarations, the handle delimiter is associated to the import container already
	Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
	return 0;
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	CompilationUnit cu = (CompilationUnit)this.parent.getParent();
	if (checkOwner && cu.isPrimary()) return this;
	return cu.getImport(this.name);
}
/**
 * Returns true if the import is on-demand (ends with ".*")
 */
public boolean isOnDemand() {
	return this.name.endsWith(".*"); //$NON-NLS-1$
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
	toStringName(buffer);
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
}
