package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.core.*;

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
	IBinaryField info = (IBinaryField) getRawInfo();
	return convertConstant(info.getConstant());
}
/**
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryField info = (IBinaryField) getRawInfo();
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
	IBinaryField info = (IBinaryField) getRawInfo();
	return new String(ClassFile.translatedName(info.getTypeName()));
}
}
