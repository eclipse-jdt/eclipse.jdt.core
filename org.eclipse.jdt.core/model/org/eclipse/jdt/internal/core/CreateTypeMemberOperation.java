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

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Implements functionality common to
 * operations that create type members.
 */
public abstract class CreateTypeMemberOperation extends CreateElementInCUOperation {
	/**
	 * The source code for the new member.
	 */
	protected String fSource = null;
	/**
	 * The name of the <code>DOMNode</code> that may be used to
	 * create this new element.
	 * Used by the <code>CopyElementsOperation</code> for renaming
	 */
	protected String fAlteredName;
	/**
	 * The JDOM document fragment representing the element that
	 * this operation created. 
	 */
	 protected IDOMNode fDOMNode;
/**
 * When executed, this operation will create a type member
 * in the given parent element with the specified source.
 */
public CreateTypeMemberOperation(IJavaElement parentElement, String source, boolean force) {
	super(parentElement);
	fSource= source;
	this.force= force;
}
/**
 * @see CreateElementInCUOperation#generateNewCompilationUnitDOM
 */
protected void generateNewCompilationUnitDOM(ICompilationUnit cu) throws JavaModelException {
	IBuffer buffer = cu.getBuffer();
	if (buffer == null) return;
	char[] prevSource = buffer.getCharacters();
	if (prevSource == null) return;

	// create a JDOM for the compilation unit
	fCUDOM = (new DOMFactory()).createCompilationUnit(prevSource, cu.getElementName());
	IDOMNode parent = ((JavaElement) getParentElement()).findNode(fCUDOM);
	if (parent == null) {
		//#findNode does not work for autogenerated CUs as the contents are empty
		parent = fCUDOM;
	}
	IDOMNode child = generateElementDOM();
	if (child != null) {
		insertDOMNode(parent, child);
	}
	worked(1);
}
/**
 * Generates a <code>IDOMNode</code> based on the source of this operation
 * when there is likely a syntax error in the source.
 */
protected IDOMNode generateSyntaxIncorrectDOM() {
	//create some dummy source to generate a dom node
	StringBuffer buff = new StringBuffer();
	buff.append(Util.LINE_SEPARATOR + " public class A {" + Util.LINE_SEPARATOR); //$NON-NLS-1$
	buff.append(fSource);
	buff.append(Util.LINE_SEPARATOR).append('}');
	IDOMCompilationUnit domCU = (new DOMFactory()).createCompilationUnit(buff.toString(), "A.java"); //$NON-NLS-1$
	IDOMNode node = (IDOMNode) domCU.getChild("A").getChildren().nextElement(); //$NON-NLS-1$
	if (node != null) {
		node.remove();
	}
	return node;
}
/**
 * Returns the IType the member is to be created in.
 */
protected IType getType() {
	return (IType)getParentElement();
}
/**
 * Sets the name of the <code>DOMNode</code> that will be used to
 * create this new element.
 * Used by the <code>CopyElementsOperation</code> for renaming
 */
protected void setAlteredName(String newName) {
	fAlteredName = newName;
}
/**
 * Possible failures: <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - the parent element supplied to the operation is
 * 		<code>null</code>.
 *	<li>INVALID_CONTENTS - The source is <code>null</code> or has serious syntax errors.
  *	<li>NAME_COLLISION - A name collision occurred in the destination
 * </ul>
 */
public IJavaModelStatus verify() {
	IJavaModelStatus status = super.verify();
	if (!status.isOK()) {
		return status;
	}
	IJavaElement parent = getParentElement(); // non-null since check was done in supper
	Member localContext;
	if (parent instanceof Member && (localContext = ((Member)parent).getOuterMostLocalContext()) != null && localContext != parent) {
		// JDOM doesn't support source manipulation in local/anonymous types
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, parent);
	}
	if (fSource == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS);
	}
	if (!force) {
		//check for name collisions
		try {
			generateElementDOM();
		} catch (JavaModelException jme) {
			return jme.getJavaModelStatus();
		}
		return verifyNameCollision();
	}
	
	return JavaModelStatus.VERIFIED_OK;
}
/**
 * Verify for a name collision in the destination container.
 */
protected IJavaModelStatus verifyNameCollision() {
	return JavaModelStatus.VERIFIED_OK;
}
}
