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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;

/**
 * @see IField
 */

/* package */ class BinaryField extends BinaryMember implements IField {

/**
 * Constructs a handle to the field with the given name in the specified type. 
 */
protected BinaryField(IType parent, String name) {
	super(FIELD, parent, name);
}
/**
 * @see IField
 */
public Object getConstant() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return convertConstant(info.getConstant());
}
/**
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return info.getModifiers();
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_FIELD;
}
/**
 * @see IField
 */
public String getTypeSignature() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return new String(ClassFile.translatedName(info.getTypeName()));
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
