/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.jdt.core.util.IAnnotation;
import org.eclipse.jdt.core.util.IAnnotationComponentValue;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IAnnotationComponent
 */
public class AnnotationComponentValue extends ClassFileStruct implements IAnnotationComponentValue {
	
	private int readOffset;
	private int constantValueIndex;
	private IConstantPoolEntry constantValue;
	private int classFileInfoIndex;
	private IConstantPoolEntry classFileInfo;
	private int enumConstantIndex;
	private IConstantPoolEntry enumConstant;
	private IAnnotation attributeValue;
	private int valuesNumber;
	private IAnnotationComponentValue[] annotationComponentValues;
	private int tag;
	
	public AnnotationComponentValue(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		final int t = u1At(classFileBytes, 0, offset);
		this.tag = t;
		this.readOffset = 1;
		switch(t) {
			case 'B' :
			case 'C' :
			case 'D' :
			case 'F' :
			case 'I' :
			case 'J' :
			case 'S' :
			case 'Z' :
			case 's' :
				final int constantIndex = this.u2At(classFileBytes, this.readOffset, offset);
				this.constantValueIndex = constantIndex;
				if (constantIndex != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(constantIndex);
					switch(constantPoolEntry.getKind()) {
						case IConstantPoolConstant.CONSTANT_Long :
						case IConstantPoolConstant.CONSTANT_Float :
						case IConstantPoolConstant.CONSTANT_Double :
						case IConstantPoolConstant.CONSTANT_Integer :
						case IConstantPoolConstant.CONSTANT_Utf8 :
							break;
						default :
							throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.constantValue = constantPoolEntry;
				}
				this.readOffset += 2;
				break;
			case 'e' :
				final int enumIndex = this.u2At(classFileBytes, this.readOffset, offset);
				this.enumConstantIndex = enumIndex;
				if (enumIndex != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(enumIndex);
					if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Fieldref) {
						throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.enumConstant = constantPoolEntry;
				}
				this.readOffset += 2;
				break;
			case 'c' :
				final int classFileIndex = this.u2At(classFileBytes, this.readOffset, offset);
				this.classFileInfoIndex = classFileIndex;
				if (classFileIndex != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(classFileIndex);
					if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Class) {
						throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.classFileInfo = constantPoolEntry;
				}
				this.readOffset += 2;
				break;
			case '@' :
				Annotation annotation = new Annotation(classFileBytes, constantPool, this.readOffset + offset);
				this.attributeValue = annotation;
				this.readOffset += annotation.sizeInBytes();
				break;
			case '[' :
				final int numberOfValues = this.u2At(classFileBytes, this.readOffset, offset);
				this.valuesNumber = numberOfValues;
				if (numberOfValues != 0) {
					this.readOffset += 2;
					this.annotationComponentValues = new IAnnotationComponentValue[numberOfValues];
					for (int i = 0; i < numberOfValues; i++) {
						AnnotationComponentValue value = new AnnotationComponentValue(classFileBytes, constantPool, offset + readOffset);
						this.annotationComponentValues[i] = value;
						this.readOffset += value.sizeInBytes();
					}
				}
				break;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getAnnotationComponentValues()
	 */
	public IAnnotationComponentValue[] getAnnotationComponentValues() {
		return this.annotationComponentValues;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getAttributeValue()
	 */
	public IAnnotation getAttributeValue() {
		return this.attributeValue;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getClassInfo()
	 */
	public IConstantPoolEntry getClassInfo() {
		return this.classFileInfo;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getClassInfoIndex()
	 */
	public int getClassInfoIndex() {
		return this.classFileInfoIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getConstantValue()
	 */
	public IConstantPoolEntry getConstantValue() {
		return this.constantValue;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getConstantValueIndex()
	 */
	public int getConstantValueIndex() {
		return this.constantValueIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getEnumConstant()
	 */
	public IConstantPoolEntry getEnumConstant() {
		return this.enumConstant;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getEnumConstantIndex()
	 */
	public int getEnumConstantIndex() {
		return this.enumConstantIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getTag()
	 */
	public int getTag() {
		return this.tag;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getValuesNumber()
	 */
	public int getValuesNumber() {
		return this.valuesNumber;
	}
	
	int sizeInBytes() {
		return this.readOffset;
	}
}
