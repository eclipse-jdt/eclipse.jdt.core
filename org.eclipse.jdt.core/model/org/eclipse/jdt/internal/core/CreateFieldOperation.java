package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.*;

/**
 * <p>This operation creates a field declaration in a type.
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing Type
 *  <li>The source code for the declaration. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateFieldOperation extends CreateTypeMemberOperation {
	/**
	 * When executed, this operation will create a field with the given name
	 * in the given type with the specified source.
	 *
	 * <p>By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	public CreateFieldOperation(
		IType parentElement,
		String source,
		boolean force) {
		super(parentElement, source, force);
	}

	/**
	 * @see CreateTypeMemberOperation#generateElementDOM
	 */
	protected IDOMNode generateElementDOM() throws JavaModelException {
		if (fDOMNode == null) {
			fDOMNode = (new DOMFactory()).createField(fSource);
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
		return getType().getField(fDOMNode.getName());
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName() {
		return "Creating a field...";
	}

	/**
	 * By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	protected void initializeDefaultPosition() {
		IType parentElement = getType();
		try {
			IJavaElement[] elements = parentElement.getFields();
			if (elements != null && elements.length > 0) {
				createAfter(elements[elements.length - 1]);
			} else {
				elements = parentElement.getChildren();
				if (elements != null && elements.length > 0) {
					createBefore(elements[0]);
				}
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * @see CreateTypeMemberOperation#verifyNameCollision
	 */
	protected IJavaModelStatus verifyNameCollision() {
		IType type = getType();
		if (type.getField(fDOMNode.getName()).exists()) {
			return new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION);
		}
		return JavaModelStatus.VERIFIED_OK;
	}

}
