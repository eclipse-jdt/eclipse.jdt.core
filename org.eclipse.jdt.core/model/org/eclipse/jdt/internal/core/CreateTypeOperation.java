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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * <p>This operation creates a class or interface.
 *
 * <p>Required Attributes:<ul>
 *  <li>Parent element - must be a compilation unit, or type.
 *  <li>The source code for the type. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateTypeOperation extends CreateTypeMemberOperation {
/**
 * When executed, this operation will create a type unit
 * in the given parent element (a compilation unit, type)
 */
public CreateTypeOperation(IJavaElement parentElement, String source, boolean force) {
	super(parentElement, source, force);
}
/**
 * @see CreateElementInCUOperation#generateElementDOM()
 * @deprecated JDOM is obsolete
 */
// TODO - JDOM - remove once model ported off of JDOM
protected IDOMNode generateElementDOM() throws JavaModelException {
	if (fDOMNode == null) {
		fDOMNode = (new DOMFactory()).createType(fSource);
		if (fDOMNode == null) {
			//syntactically incorrect source
			fDOMNode = generateSyntaxIncorrectDOM();
			if (fDOMNode == null) {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
			}
		}
		if (fAlteredName != null && fDOMNode != null) {
			fDOMNode.setName(fAlteredName);
		}
	}
	if (!(fDOMNode instanceof IDOMType)) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
	}
	return fDOMNode;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle()
 */
protected IJavaElement generateResultHandle() {
	IJavaElement parent= getParentElement();
	switch (parent.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			return ((ICompilationUnit)parent).getType(getDOMNodeName());
		case IJavaElement.TYPE:
			return ((IType)parent).getType(getDOMNodeName());
		// Note: creating local/anonymous type is not supported 
	}
	return null;
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
public String getMainTaskName(){
	return Util.bind("operation.createTypeProgress"); //$NON-NLS-1$
}
/**
 * Returns the <code>IType</code> the member is to be created in.
 */
protected IType getType() {
	IJavaElement parent = getParentElement();
	if (parent.getElementType() == IJavaElement.TYPE) {
		return (IType) parent;
	}
	return null;
}
/**
 * @see CreateTypeMemberOperation#verifyNameCollision
 */
protected IJavaModelStatus verifyNameCollision() {
	IJavaElement parent = getParentElement();
	switch (parent.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			if (((ICompilationUnit) parent).getType(getDOMNodeName()).exists()) {
				return new JavaModelStatus(
					IJavaModelStatusConstants.NAME_COLLISION, 
					Util.bind("status.nameCollision", getDOMNodeName())); //$NON-NLS-1$
			}
			break;
		case IJavaElement.TYPE:
			if (((IType) parent).getType(getDOMNodeName()).exists()) {
				return new JavaModelStatus(
					IJavaModelStatusConstants.NAME_COLLISION, 
					Util.bind("status.nameCollision", getDOMNodeName())); //$NON-NLS-1$
			}
			break;
		// Note: creating local/anonymous type is not supported 
	}
	return JavaModelStatus.VERIFIED_OK;
}
/**
 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
 */
// TODO - JDOM - remove once model ported off of JDOM
private String getDOMNodeName() {
	return fDOMNode.getName();
}
}
