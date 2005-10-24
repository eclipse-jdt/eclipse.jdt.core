/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;

public class MethodInfo extends ClassFileStruct implements IBinaryMethod, Comparable {
	static private final char[][] noException = CharOperation.NO_CHAR_CHAR;
	private int accessFlags;
	private int attributeBytes;
	private int[] constantPoolOffsets;
	private char[] descriptor;
	private char[][] exceptionNames;
	private char[] name;
	private char[] signature;
	private int signatureUtf8Offset;
	private long tagBits;	
	
/**
 * @param classFileBytes byte[]
 * @param offsets int[]
 * @param offset int
 */
public MethodInfo (byte classFileBytes[], int offsets[], int offset) {
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
		if (attributeName.length > 0) {
			switch(attributeName[0]) {
				case 'S' :
					if (CharOperation.equals(AttributeNamesConstants.SignatureName, attributeName)) {
						this.signatureUtf8Offset = constantPoolOffsets[u2At(readOffset + 6)] - structOffset;
					}
					break;
				case 'R' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleAnnotationsName)) {
						decodeStandardAnnotations(readOffset);
					}
			}
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
	attributeBytes = readOffset;
}
public int compareTo(Object o) {
	if (!(o instanceof MethodInfo)) {
		throw new ClassCastException();
	}

	MethodInfo otherMethod = (MethodInfo) o;
	int result = new String(this.getSelector()).compareTo(new String(otherMethod.getSelector()));
	if (result != 0) return result;
	return new String(this.getMethodDescriptor()).compareTo(new String(otherMethod.getMethodDescriptor()));
}
private int decodeAnnotation(int offset) {
	int readOffset = offset;
	int utf8Offset = this.constantPoolOffsets[u2At(offset)] - structOffset;
	char[] typeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	int numberOfPairs = u2At(offset + 2);
	readOffset += 4;
	if (typeName.length == 22 && CharOperation.equals(typeName, ConstantPool.JAVA_LANG_DEPRECATED)) {
		this.tagBits |= TagBits.AnnotationDeprecated;
		return readOffset;		
	}
	for (int i = 0; i < numberOfPairs; i++) {
		readOffset += 2;
		readOffset = decodeElementValue(readOffset);
	}
	return readOffset;
}
private int decodeElementValue(int offset) {
	int readOffset = offset;
	int tag = u1At(readOffset);
	readOffset++;
	switch(tag) {
		case 'B' :
		case 'C' :
		case 'D' :
		case 'F' :
		case 'I' :
		case 'J' :
		case 'S' :
		case 'Z' :
		case 's' :
			readOffset += 2;
			break;
		case 'e' :
			readOffset += 4;
			break;
		case 'c' :
			readOffset += 2;
			break;
		case '@' :
			readOffset = decodeAnnotation(readOffset);
			break;
		case '[' :
			int numberOfValues = u2At(readOffset);
			readOffset += 2;
			for (int i = 0; i < numberOfValues; i++) {
				readOffset = decodeElementValue(readOffset);
			}
			break;
	}
	return readOffset;
}
/**
 * @param offset the offset is located at the beginning of the runtime visible 
 * annotation attribute.
 */
private void decodeStandardAnnotations(int offset) {
	int numberOfAnnotations = u2At(offset + 6);
	int readOffset = offset + 8;
	for (int i = 0; i < numberOfAnnotations; i++) {
		readOffset = decodeAnnotation(readOffset);
	}
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IGenericMethod#getArgumentNames()
 */
public char[][] getArgumentNames() {
	return null;
}
/**
 * Answer the resolved names of the exception types in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[][]
 */
public char[][] getExceptionTypeNames() {
	if (exceptionNames == null) {
		readExceptionAttributes();
	}
	return exceptionNames;
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
 * Answer the receiver's method descriptor which describes the parameter &
 * return types as specified in section 4.3.3 of the Java 2 VM spec.
 *
 * For example:
 *   - int foo(String) is (Ljava/lang/String;)I
 *   - void foo(Object[]) is (I)[Ljava/lang/Object;
 * @return char[]
 */
public char[] getMethodDescriptor() {
	if (descriptor == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(4)] - structOffset;
		descriptor = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return descriptor;
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
		readModifierRelatedAttributes();
	}
	return this.accessFlags;
}
/**
 * Answer the name of the method.
 *
 * For a constructor, answer <init> & <clinit> for a clinit method.
 * @return char[]
 */
public char[] getSelector() {
	if (name == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(2)] - structOffset;
		name = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return name;
}
public long getTagBits() {
	return this.tagBits;
}
/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
void initialize() {
	getModifiers();
	getSelector();
	getMethodDescriptor();
	getExceptionTypeNames();
	getGenericSignature();
	reset();
}
/**
 * Answer true if the method is a class initializer, false otherwise.
 * @return boolean
 */
public boolean isClinit() {
	char[] selector = getSelector();
	return selector[0] == '<' && selector.length == 8; // Can only match <clinit>
}
/**
 * Answer true if the method is a constructor, false otherwise.
 * @return boolean
 */
public boolean isConstructor() {
	char[] selector = getSelector();
	return selector[0] == '<' && selector.length == 6; // Can only match <init>
}
/**
 * Return true if the field is a synthetic method, false otherwise.
 * @return boolean
 */
public boolean isSynthetic() {
	return (getModifiers() & AccSynthetic) != 0;
}
private void readExceptionAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation.equals(attributeName, AttributeNamesConstants.ExceptionsName)) {
			// read the number of exception entries
			int entriesNumber = u2At(readOffset + 6);
			// place the readOffset at the beginning of the exceptions table
			readOffset += 8;
			if (entriesNumber == 0) {
				exceptionNames = noException;
			} else {
				exceptionNames = new char[entriesNumber][];
				for (int j = 0; j < entriesNumber; j++) {
					utf8Offset = 
						constantPoolOffsets[u2At(
							constantPoolOffsets[u2At(readOffset)] - structOffset + 1)]
							- structOffset; 
					exceptionNames[j] = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
					readOffset += 2;
				}
			}
		} else {
			readOffset += (6 + u4At(readOffset + 2));
		}
	}
	if (exceptionNames == null) {
		exceptionNames = noException;
	}
}
private void readModifierRelatedAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		// test added for obfuscated .class file. See 79772
		if (attributeName.length != 0) {
			switch(attributeName[0]) {
				case 'D' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.DeprecatedName))
						this.accessFlags |= AccDeprecated;
					break;
				case 'S' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.SyntheticName))
						this.accessFlags |= AccSynthetic;
					break;
				case 'A' :
					if (CharOperation.equals(attributeName, AttributeNamesConstants.AnnotationDefaultName))
						this.accessFlags |= AccAnnotationDefault;
					break;
			}
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
public String toString() {
	int modifiers = getModifiers();
	char[] desc = getGenericSignature();
	if (desc == null)
		desc = getMethodDescriptor();
	StringBuffer buffer = new StringBuffer(this.getClass().getName());
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
				+ ((modifiers & 0x0080) == 0x0080 ? "varargs " : "")) //$NON-NLS-1$ //$NON-NLS-2$
		.append(getSelector())
		.append(desc)
		.append("}") //$NON-NLS-1$
		.toString(); 
}
}
