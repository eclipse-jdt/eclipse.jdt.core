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
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IVerificationTypeInfo;

/**
 * @since 3.0
 */
public class VerificationTypeInfo extends ClassFileStruct implements IVerificationTypeInfo {
	
	private static final int ITEM_Top = 0;
	private static final int ITEM_Integer = 1;
	private static final int ITEM_Float = 2;
	private static final int ITEM_Double = 3;
	private static final int ITEM_Long = 4;
	private static final int ITEM_Null = 5;
	private static final int ITEM_UninitializedThis = 6;
	private static final int ITEM_Object = 7;
	private static final int ITEM_Uninitialized = 8;
	
	private int tag;
	private int variableOffset;
	private int constantPoolIndex;
	private int size;
	private char[] classTypeName;
	
	VerificationTypeInfo(byte[] classFileBytes,	IConstantPool constantPool,	int offset) throws ClassFormatException {
		final int tagValue = u1At(classFileBytes, 0, offset);
		switch(tagValue) {
			case ITEM_Top :
			case ITEM_Integer :
			case ITEM_Float :
			case ITEM_Double :
			case ITEM_Long :
			case ITEM_Null :
			case ITEM_UninitializedThis :
			case ITEM_Object :
			case ITEM_Uninitialized :
				break;
			default:
				throw new ClassFormatException(ClassFormatException.INVALID_TAG_CONSTANT);
		}
		this.tag = tagValue;
		int currentSize = 1;
		switch(tagValue) {
			case ITEM_Uninitialized :
				this.variableOffset = u2At(classFileBytes, 1, offset);
				currentSize += 2;
				break;
			case ITEM_Object :
				final int index = u2At(classFileBytes, 1, offset);
				this.constantPoolIndex = index;
				IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
				if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Class) {
					throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
				}
				this.classTypeName = constantPoolEntry.getClassName();
				currentSize += 2;
				break;
		}
		this.size = currentSize;
	}

	int getSize() {
		return this.size;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IVerificationTypeInfo#getTag()
	 */
	public int getTag() {
		return this.tag;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IVerificationTypeInfo#getOffset()
	 */
	public int getOffset() {
		return this.variableOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IVerificationTypeInfo#getConstantPoolIndex()
	 */
	public int getConstantPoolIndex() {
		return this.constantPoolIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IVerificationTypeInfo#getClassTypeName()
	 */
	public char[] getClassTypeName() {
		return this.classTypeName;
	}
}
