package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.*;

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
 * @see CreateTypeMemberOperation#generateElementDOM
 */
protected IDOMNode generateElementDOM() throws JavaModelException {
	if (fDOMNode == null) {
		fDOMNode = (new DOMFactory()).createType(fSource);
		if (fDOMNode == null) {
			fDOMNode = generateSyntaxIncorrectDOM();
		}
		if (fAlteredName != null && fDOMNode != null) {
			fDOMNode.setName(fAlteredName);
		}
	}
	return fDOMNode;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle
 */
protected IJavaElement generateResultHandle() {
	IJavaElement parent= getParentElement();
	int type= parent.getElementType();
	if (type == IJavaElement.TYPE) {
		return ((IType)parent).getType(fDOMNode.getName());
	} else if (type == IJavaElement.COMPILATION_UNIT) {
		return ((ICompilationUnit)parent).getType(fDOMNode.getName());
	} 
	return null;
}
/**
 * @see CreateElementInCUOperation#getMainTaskName
 */
public String getMainTaskName(){
	return "Creating a type...";
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
	int type = parent.getElementType();
	if (type == IJavaElement.TYPE) {
		if (((IType) parent).getType(fDOMNode.getName()).exists()) {
			return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION);
		}
	} else
		if (type == IJavaElement.COMPILATION_UNIT) {
			if (((ICompilationUnit) parent).getType(fDOMNode.getName()).exists()) {
				return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION);
			}
		}
	return JavaModelStatus.VERIFIED_OK;
}
}
