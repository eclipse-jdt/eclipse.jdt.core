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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * @see IField
 */

/* package */ class SourceField extends Member implements IField {

/**
 * Constructs a handle to the field with the given name in the specified type. 
 */
protected SourceField(JavaElement parent, String name) {
	super(parent, name);
}
public boolean equals(Object o) {
	if (!(o instanceof SourceField)) return false;
	return super.equals(o);
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) {
	return (node.getNodeType() == IDOMNode.FIELD) && super.equalsDOMNode(node);
}
/**
 * @see IField
 */
public Object getConstant() throws JavaModelException {
	Object constant = null;	
	SourceFieldElementInfo info = (SourceFieldElementInfo) getElementInfo();
	if (info.initializationSource == null) {
		return null;
	}
			
	String constantSource = new String(info.initializationSource);
	String signature = info.getTypeSignature();
	if (signature.equals(Signature.SIG_INT)) {
		constant = new Integer(constantSource);
	} else if (signature.equals(Signature.SIG_SHORT)) {
		constant = new Short(constantSource);
	} else if (signature.equals(Signature.SIG_BYTE)) {
		constant = new Byte(constantSource);
	} else if (signature.equals(Signature.SIG_BOOLEAN)) {
		constant = new Boolean(constantSource);
	} else if (signature.equals(Signature.SIG_CHAR)) {
		constant = new Character(constantSource.charAt(0));
	} else if (signature.equals(Signature.SIG_DOUBLE)) {
		constant = new Double(constantSource);
	} else if (signature.equals(Signature.SIG_FLOAT)) {
		constant = new Float(constantSource);
	} else if (signature.equals(Signature.SIG_LONG)) { 
		constant = new Long(constantSource);
	} else if (signature.equals("QString;")) {//$NON-NLS-1$
		constant = constantSource;
	}
	return constant;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return FIELD;
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_FIELD;
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner) {
		CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
		if (cu.isPrimary()) return this;
	}
	IJavaElement parent =fParent.getPrimaryElement(false);
	return ((IType)parent).getField(fName);
}
/**
 * @see IField
 */
public String getTypeSignature() throws JavaModelException {
	SourceFieldElementInfo info = (SourceFieldElementInfo) getElementInfo();
	return info.getTypeSignature();
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	if (info == null) {
		buffer.append(getElementName());
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append(getElementName());
	} else {
		try {
			buffer.append(Signature.toString(this.getTypeSignature()));
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(this.getElementName());
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
