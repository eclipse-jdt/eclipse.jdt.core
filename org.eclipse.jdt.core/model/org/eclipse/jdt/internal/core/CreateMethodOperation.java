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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.jdom.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * <p>This operation creates an instance method. 
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing type
 *  <li>The source code for the method. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateMethodOperation extends CreateTypeMemberOperation {
	protected String[] fParameterTypes;
/**
 * When executed, this operation will create a method
 * in the given type with the specified source.
 */
public CreateMethodOperation(IType parentElement, String source, boolean force) {
	super(parentElement, source, force);
}
/**
 * Returns the type signatures of the parameter types of the
 * current <code>DOMMethod</code>
 * @deprecated JDOM is obsolete
 */
// TODO - JDOM - remove once model ported off of JDOM
protected String[] convertDOMMethodTypesToSignatures() {
	if (fParameterTypes == null) {
		if (isDOMNodeNull()) {
			String[] domParameterTypes = ((IDOMMethod)fDOMNode).getParameterTypes();
			if (domParameterTypes != null) {
				fParameterTypes = new String[domParameterTypes.length];
				// convert the DOM types to signatures
				int i;
				for (i = 0; i < fParameterTypes.length; i++) {
					fParameterTypes[i] = Signature.createTypeSignature(domParameterTypes[i].toCharArray(), false);
				}
			}
		}
	}
	return fParameterTypes;
}
/**
 * @see CreateTypeMemberOperation#generateElementDOM
 * @deprecated JDOM is obsolete
 */
// TODO - JDOM - remove once model ported off of JDOM
protected IDOMNode generateElementDOM() throws JavaModelException {
	if (fDOMNode == null) {
		fDOMNode = (new DOMFactory()).createMethod(fSource);
		if (fDOMNode == null) {
			//syntactically incorrect source
			fDOMNode = generateSyntaxIncorrectDOM();
			if (fDOMNode == null) {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
			}
		}
		if (fAlteredName != null && isDOMNodeNull()) {
			fDOMNode.setName(fAlteredName);
		}
	}
	if (!(fDOMNode instanceof IDOMMethod)) {
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
	}
	return fDOMNode;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle
 */
protected IJavaElement generateResultHandle() {
	String[] types = convertDOMMethodTypesToSignatures();
	String name = computeName();
	return getType().getMethod(name, types);
}
/**
 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
 */
// TODO - JDOM - remove once model ported off of JDOM
private String computeName() {
	String name;
	if (((IDOMMethod) fDOMNode).isConstructor()) {
		name = fDOMNode.getParent().getName();
	} else {
		name = getDOMNodeName();
	}
	return name;
}
/**
 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
 */
// TODO - JDOM - remove once model ported off of JDOM
private String getDOMNodeName() {
	return fDOMNode.getName();
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
public String getMainTaskName(){
	return Util.bind("operation.createMethodProgress"); //$NON-NLS-1$
}
/**
 * @see CreateTypeMemberOperation#verifyNameCollision
 */
protected IJavaModelStatus verifyNameCollision() {
	if (isDOMNodeNull()) {
		IType type = getType();
		String name = getDOMNodeName();
		if (name == null) { //constructor
			name = type.getElementName();
		}
		String[] types = convertDOMMethodTypesToSignatures();
		if (type.getMethod(name, types).exists()) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.NAME_COLLISION, 
				Util.bind("status.nameCollision", name)); //$NON-NLS-1$
		}
	}
	return JavaModelStatus.VERIFIED_OK;
}
/**
 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
 */
// TODO - JDOM - remove once model ported off of JDOM
private boolean isDOMNodeNull() {
	return fDOMNode != null;
}
}
