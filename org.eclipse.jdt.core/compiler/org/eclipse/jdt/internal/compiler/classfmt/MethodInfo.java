package org.eclipse.jdt.internal.compiler.classfmt;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class MethodInfo
	extends ClassFileStruct
	implements IBinaryMethod, AttributeNamesConstants {
	private char[][] exceptionNames;
	private int[] constantPoolOffsets;
	private boolean isDeprecated;
	private int accessFlags;
	private char[] name;
	private char[] signature;
	private int attributesCount;
	private int attributeBytes;
	static private final char[][] noException = new char[0][0];
	private int decodeIndex;
	/**
	 * @param classFileBytes byte[]
	 * @param offsets int[]
	 * @param offset int
	 */
	public MethodInfo(byte classFileBytes[], int offsets[], int offset)
		throws ClassFormatException {
		super(classFileBytes, offset);
		constantPoolOffsets = offsets;
		accessFlags = -1;
		int attributesCount = u2At(6);
		int readOffset = 8;
		for (int i = 0; i < attributesCount; i++) {
			readOffset += (6 + u4At(readOffset + 2));
		}
		attributeBytes = readOffset;
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
		if (signature == null) {
			// read the name
			int utf8Offset = constantPoolOffsets[u2At(4)] - structOffset;
			signature = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		}
		return signature;
	}

	/**
	 * Answer an int whose bits are set according the access constants
	 * defined by the VM spec.
	 * Set the AccDeprecated and AccSynthetic bits if necessary
	 * @return int
	 */
	public int getModifiers() {
		if (accessFlags == -1) {
			// compute the accessflag. Don't forget the deprecated attribute
			accessFlags = u2At(0);
			readDeprecatedAttributes();
			if (isDeprecated) {
				accessFlags |= AccDeprecated;
			}
			if (isSynthetic()) {
				accessFlags |= AccSynthetic;
			}
		}
		return accessFlags;
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
	private boolean isSynthetic() {
		int attributesCount = u2At(6);
		int readOffset = 8;
		boolean isSynthetic = false;
		for (int i = 0; i < attributesCount; i++) {
			int utf8Offset = constantPoolOffsets[u2At(8)] - structOffset;
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (CharOperation.equals(attributeName, SyntheticName)) {
				isSynthetic = true;
			}
			readOffset += (6 + u4At(readOffset + 2));
		}
		return isSynthetic;
	}

	private void readDeprecatedAttributes() {
		int attributesCount = u2At(6);
		int readOffset = 8;
		for (int i = 0; i < attributesCount; i++) {
			int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (CharOperation.equals(attributeName, DeprecatedName)) {
				isDeprecated = true;
			}
			readOffset += (6 + u4At(readOffset + 2));
		}
	}

	private void readExceptionAttributes() {
		int attributesCount = u2At(6);
		int readOffset = 8;
		for (int i = 0; i < attributesCount; i++) {
			int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
			char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
			if (CharOperation.equals(attributeName, ExceptionsName)) {
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
		StringBuffer buffer = new StringBuffer(this.getClass().getName());
		return buffer
			.append("{")
			.append(
				((modifiers & AccDeprecated) != 0 ? "deprecated " : "")
					+ ((modifiers & 0x0001) == 1 ? "public " : "")
					+ ((modifiers & 0x0002) == 0x0002 ? "private " : "")
					+ ((modifiers & 0x0004) == 0x0004 ? "protected " : "")
					+ ((modifiers & 0x0008) == 0x000008 ? "static " : "")
					+ ((modifiers & 0x0010) == 0x0010 ? "final " : "")
					+ ((modifiers & 0x0040) == 0x0040 ? "volatile " : "")
					+ ((modifiers & 0x0080) == 0x0080 ? "transient " : ""))
			.append(getSelector())
			.append(getMethodDescriptor())
			.append("}")
			.toString();
	}

}
