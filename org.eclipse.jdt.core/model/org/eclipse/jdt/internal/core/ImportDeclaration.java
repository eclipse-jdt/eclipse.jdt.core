package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * @see IImportDeclaration
 */

/* package */ class ImportDeclaration extends SourceRefElement implements IImportDeclaration {


/**
 * Constructs an ImportDeclartaion in the given import container
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
	return fName.endsWith(".*"/*nonNLS*/);
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
	buffer.append("import "/*nonNLS*/);
	buffer.append(getElementName());
	if (info == null) {
		buffer.append(" (not open)"/*nonNLS*/);
	}
}
}
