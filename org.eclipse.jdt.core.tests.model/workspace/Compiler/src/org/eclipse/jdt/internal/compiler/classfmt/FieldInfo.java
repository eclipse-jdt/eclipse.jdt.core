/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.Util;

public class FieldInfo extends ClassFileStruct implements AttributeNamesConstants, IBinaryField, Comparable, TypeIds {
	private int accessFlags;
	private int attributeBytes;
	private Constant constant;
	private int[] constantPoolOffsets;
	private char[] descriptor;
	private char[] name;
	private Object wrappedConstantValue;
	private char[] signature;
	private int signatureUtf8Offset;
/**
 * @param classFileBytes byte[]
 * @param offsets int[]
 * @param offset int
 */
public FieldInfo (byte classFileBytes[], int offsets[], int offset) {
	super(classFileBytes, offset);
	constantPoolOffsets = offsets;
	accessFlags = -1;
	int attributesCount = u2At(6);
	int readOffset = 8;
	this.signatureUtf8Offset = -1;
	for (int i = 0; i < attributesCount; i++) {
		// check the name of each attribute
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation.equals(AttributeNamesConstants.SignatureName, attributeName)) {
			this.signatureUtf8Offset = constantPoolOffsets[u2At(readOffset + 6)] - structOffset;
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
	attributeBytes = readOffset;
}

public int compareTo(Object o) {
	if (!(o instanceof FieldInfo)) {
		throw new ClassCastException();
	}
	return new String(this.getName()).compareTo(new String(((FieldInfo) o).getName()));
}
/**
 * Return the constant of the field.
 * Return org.eclipse.jdt.internal.compiler.impl.Constant.NotAConstant if there is none.
 * @return org.eclipse.jdt.internal.compiler.impl.Constant
 */
public Constant getConstant() {
	if (constant == null) {
		// read constant
		readConstantAttribute();
	}
	return constant;
}
public char[] getGenericSignature() {
	if (this.signatureUtf8Offset != -1) {
		if (this.signature == null) {
			// decode the signature
			this.signature = utf8At(this.signatureUtf8Offset + 3, u2At(this.signatureUtf8Offset + 1));
		}
		return this.signature;
	}
	return null;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Set the AccDeprecated and AccSynthetic bits if necessary
 * @return int
 */
public int getModifiers() {
	if (this.accessFlags == -1) {
		// compute the accessflag. Don't forget the deprecated attribute
		this.accessFlags = u2At(0);
		readDeprecatedAndSyntheticAttributes();
	}
	return this.accessFlags;
}
/**
 * Answer the name of the field.
 * @return char[]
 */
public char[] getName() {
	if (name == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(2)] - structOffset;
		name = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return name;
}
/**
 * Answer the resolved name of the receiver's type in the
 * class file format as specified in section 4.3.2 of the Java 2 VM spec.
 *
 * For example:
 *   - java.lang.String is Ljava/lang/String;
 *   - an int is I
 *   - a 2 dimensional array of strings is [[Ljava/lang/String;
 *   - an array of floats is [F
 * @return char[]
 */
public char[] getTypeName() {
	if (descriptor == null) {
		// read the signature
		int utf8Offset = constantPoolOffsets[u2At(4)] - structOffset;
		descriptor = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return descriptor;
}
/**
 * Return a wrapper that contains the constant of the field.
 * @return java.lang.Object
 */
public Object getWrappedConstantValue() {

	if (this.wrappedConstantValue == null) {
		if (hasConstant()) {
			Constant fieldConstant = getConstant();
			switch (fieldConstant.typeID()) {
				case T_int :
					this.wrappedConstantValue = Integer.valueOf(fieldConstant.intValue());
					break;
				case T_byte :
					this.wrappedConstantValue = Byte.valueOf(fieldConstant.byteValue());
					break;
				case T_short :
					this.wrappedConstantValue = Short.valueOf(fieldConstant.shortValue());
					break;
				case T_char :
					this.wrappedConstantValue = Character.valueOf(fieldConstant.charValue());
					break;
				case T_float :
					this.wrappedConstantValue = new Float(fieldConstant.floatValue());
					break;
				case T_double :
					this.wrappedConstantValue = new Double(fieldConstant.doubleValue());
					break;
				case T_boolean :
					this.wrappedConstantValue = Util.toBoolean(fieldConstant.booleanValue());
					break;
				case T_long :
					this.wrappedConstantValue = Long.valueOf(fieldConstant.longValue());
					break;
				case T_String :
					this.wrappedConstantValue = fieldConstant.stringValue();
			}
		}
	}
	return this.wrappedConstantValue;
}
/**
 * Return true if the field has a constant value attribute, false otherwise.
 * @return boolean
 */
public boolean hasConstant() {
	return getConstant() != Constant.NotAConstant;
}
/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
void initialize() {
	getModifiers();
	getName();
	getConstant();
	getTypeName();
	getGenericSignature();
	reset();
}
/**
 * Return true if the field is a synthetic field, false otherwise.
 * @return boolean
 */
public boolean isSynthetic() {
	return (getModifiers() & AccSynthetic) != 0;
}

private void readConstantAttribute() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	boolean isConstant = false;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation
			.equals(attributeName, ConstantValueName)) {
			isConstant = true;
			// read the right constant
			int relativeOffset = constantPoolOffsets[u2At(readOffset + 6)] - structOffset;
			switch (u1At(relativeOffset)) {
				case IntegerTag :
					char[] sign = getTypeName();
					if (sign.length == 1) {
						switch (sign[0]) {
							case 'Z' : // boolean constant
								constant = new BooleanConstant(i4At(relativeOffset + 1) == 1);
								break;
							case 'I' : // integer constant
								constant = new IntConstant(i4At(relativeOffset + 1));
								break;
							case 'C' : // char constant
								constant = new CharConstant((char) i4At(relativeOffset + 1));
								break;
							case 'B' : // byte constant
								constant = new ByteConstant((byte) i4At(relativeOffset + 1));
								break;
							case 'S' : // short constant
								constant = new ShortConstant((short) i4At(relativeOffset + 1));
								break;
							default:
								constant = Constant.NotAConstant;                   
						}
					} else {
						constant = Constant.NotAConstant;
					}
					break;
				case FloatTag :
					constant = new FloatConstant(floatAt(relativeOffset + 1));
					break;
				case DoubleTag :
					constant = new DoubleConstant(doubleAt(relativeOffset + 1));
					break;
				case LongTag :
					constant = new LongConstant(i8At(relativeOffset + 1));
					break;
				case StringTag :
					utf8Offset = constantPoolOffsets[u2At(relativeOffset + 1)] - structOffset;
					constant = 
						new StringConstant(
							String.valueOf(utf8At(utf8Offset + 3, u2At(utf8Offset + 1)))); 
					break;
			}
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
	if (!isConstant) {
		constant = Constant.NotAConstant;
	}
}
private void readDeprecatedAndSyntheticAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation.equals(attributeName, DeprecatedName)) {
			this.accessFlags |= AccDeprecated;
		} else if (CharOperation.equals(attributeName, SyntheticName)) {
			this.accessFlags |= AccSynthetic;
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
}
protected void reset() {
	this.constantPoolOffsets = null;
	super.reset();
}
/**
 * Answer the size of the receiver in bytes.
 * 
 * @return int
 */
public int sizeInBytes() {
	return attributeBytes;
}
public void throwFormatException() throws ClassFormatException {
	throw new ClassFormatException(ClassFormatException.ErrBadFieldInfo);
}
public String toString() {
	StringBuffer buffer = new StringBuffer(this.getClass().getName());
	int modifiers = getModifiers();
	return buffer
		.append("{") //$NON-NLS-1$
		.append(
			((modifiers & AccDeprecated) != 0 ? "deprecated " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0001) == 1 ? "public " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0002) == 0x0002 ? "private " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0004) == 0x0004 ? "protected " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0008) == 0x000008 ? "static " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0010) == 0x0010 ? "final " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0040) == 0x0040 ? "volatile " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0080) == 0x0080 ? "transient " : "")) //$NON-NLS-1$ //$NON-NLS-2$
		.append(getTypeName())
		.append(" ") //$NON-NLS-1$
		.append(getName())
		.append(" ") //$NON-NLS-1$
		.append(getConstant())
		.append("}") //$NON-NLS-1$
		.toString(); 
}

}
