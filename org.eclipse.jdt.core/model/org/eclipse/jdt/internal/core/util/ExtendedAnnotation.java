/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IAnnotationComponent;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IExtendedAnnotation;
import org.eclipse.jdt.core.util.IExtendedAnnotationConstants;
import org.eclipse.jdt.core.util.ILocalVariableReferenceInfo;

/**
 * Default implementation of IAnnotation
 */
public class ExtendedAnnotation extends ClassFileStruct implements IExtendedAnnotation {

	private static final IAnnotationComponent[] NO_ENTRIES = new IAnnotationComponent[0];

	private int typeIndex;
	private char[] typeName;
	private int componentsNumber;
	private IAnnotationComponent[] components;
	private int readOffset;
	private int targetType;
	private int annotationTypeIndex;
	private int offset;
	private int typeParameterIndex;
	private int typeParameterBoundIndex;
	private int parameterIndex;
	private int wildcardLocationType;
	private ILocalVariableReferenceInfo[] localVariableTable;
	private int[] locations;
	private int[] wildcardLocations;
	/**
	 * Constructor for Annotation.
	 *
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public ExtendedAnnotation(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {

		int index = u2At(classFileBytes, 0, offset);
		this.typeIndex = index;
		if (index != 0) {
			IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			this.typeName = constantPoolEntry.getUtf8Value();
		} else {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		final int length = u2At(classFileBytes, 2, offset);
		this.componentsNumber = length;
		this.readOffset = 4;
		if (length != 0) {
			this.components = new IAnnotationComponent[length];
			for (int i = 0; i < length; i++) {
				AnnotationComponent component = new AnnotationComponent(classFileBytes, constantPool, offset + this.readOffset);
				this.components[i] = component;
				this.readOffset += component.sizeInBytes();
			}
		} else {
			this.components = NO_ENTRIES;
		}
		index = u1At(classFileBytes, this.readOffset, offset);
		this.readOffset++;
		this.targetType = index;
		switch(index) {
			case IExtendedAnnotationConstants.WILDCARD_BOUND :
				this.wildcardLocationType = u1At(classFileBytes, this.readOffset, offset);
				this.readOffset++;
				internalDecoding(this.wildcardLocationType, classFileBytes, constantPool, offset);
				// copy the location back into the wildcard location
				int size = this.locations.length;
				System.arraycopy(this.locations, 0, (this.wildcardLocations = new int[size]), 0, size);
				this.locations = null;
				break;
			case IExtendedAnnotationConstants.WILDCARD_BOUND_GENERIC_OR_ARRAY :
				this.wildcardLocationType = u1At(classFileBytes, this.readOffset, offset);
				this.readOffset++;
				internalDecoding(this.wildcardLocationType, classFileBytes, constantPool, offset);
				size = this.locations.length;
				System.arraycopy(this.locations, 0, (this.wildcardLocations = new int[size]), 0, size);
				int locationLength = u2At(classFileBytes, this.readOffset, offset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, offset);
					this.readOffset++;
				}
				break;
			default:
				internalDecoding(index, classFileBytes, constantPool, offset);
		}
		if (this.annotationTypeIndex == 0xFFFF) {
			this.annotationTypeIndex = -1;
		}
	}
	
	private void internalDecoding(
			int localTargetType,
			byte[] classFileBytes,
			IConstantPool constantPool,
			int localOffset) throws ClassFormatException {
		switch(localTargetType) {
			case IExtendedAnnotationConstants.CLASS_EXTENDS_IMPLEMENTS :
				this.annotationTypeIndex = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset+=2;
				break;
			case IExtendedAnnotationConstants.CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY :
				this.annotationTypeIndex = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset+=2;
				int locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.TYPE_CAST :
			case IExtendedAnnotationConstants.TYPE_INSTANCEOF :
			case IExtendedAnnotationConstants.OBJECT_CREATION :
			case IExtendedAnnotationConstants.CLASS_LITERAL :
				this.offset = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				break;
			case IExtendedAnnotationConstants.TYPE_CAST_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.TYPE_INSTANCEOF_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.OBJECT_CREATION_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.CLASS_LITERAL_GENERIC_OR_ARRAY :
				this.offset = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.CLASS_TYPE_PARAMETER :
			case IExtendedAnnotationConstants.METHOD_TYPE_PARAMETER :
				this.typeParameterIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				break;
			case IExtendedAnnotationConstants.CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY :
				this.typeParameterIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.METHOD_TYPE_PARAMETER_BOUND :
			case IExtendedAnnotationConstants.CLASS_TYPE_PARAMETER_BOUND :
				this.typeParameterIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				this.typeParameterBoundIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				break;
			case IExtendedAnnotationConstants.METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY :
				this.typeParameterIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				this.typeParameterBoundIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.LOCAL_VARIABLE :
				int tableLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.localVariableTable = new LocalVariableReferenceInfo[tableLength];
				for (int i = 0; i < tableLength; i++) {
					this.localVariableTable[i] = new LocalVariableReferenceInfo(classFileBytes, constantPool, this.readOffset + localOffset);
					this.readOffset += 6;
				}
				break;
			case IExtendedAnnotationConstants.LOCAL_VARIABLE_GENERIC_OR_ARRAY :
				tableLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.localVariableTable = new LocalVariableReferenceInfo[tableLength];
				for (int i = 0; i < tableLength; i++) {
					this.localVariableTable[i] = new LocalVariableReferenceInfo(classFileBytes, constantPool, this.readOffset + localOffset);
					this.readOffset += 6;
				}
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.METHOD_PARAMETER :
				this.parameterIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				break;
			case IExtendedAnnotationConstants.METHOD_PARAMETER_GENERIC_OR_ARRAY :
				this.parameterIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.METHOD_RECEIVER_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.METHOD_RETURN_TYPE_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.FIELD_GENERIC_OR_ARRAY :
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.TYPE_ARGUMENT_CONSTRUCTOR_CALL :
			case IExtendedAnnotationConstants.TYPE_ARGUMENT_METHOD_CALL :
				this.offset = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.annotationTypeIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				break;
			case IExtendedAnnotationConstants.TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY :
			case IExtendedAnnotationConstants.TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY :
				this.offset = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.annotationTypeIndex = u1At(classFileBytes, this.readOffset, localOffset);
				this.readOffset++;
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
			case IExtendedAnnotationConstants.THROWS :
				this.annotationTypeIndex = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset+=2;
				break;
			case IExtendedAnnotationConstants.THROWS_GENERIC_OR_ARRAY :
				this.annotationTypeIndex = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset+=2;
				locationLength = u2At(classFileBytes, this.readOffset, localOffset);
				this.readOffset += 2;
				this.locations = new int[locationLength];
				for (int i = 0; i < locationLength; i++) {
					this.locations[i] = u1At(classFileBytes, this.readOffset, localOffset);
					this.readOffset++;
				}
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getTypeIndex()
	 */
	public int getTypeIndex() {
		return this.typeIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getComponentsNumber()
	 */
	public int getComponentsNumber() {
		return this.componentsNumber;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getComponents()
	 */
	public IAnnotationComponent[] getComponents() {
		return this.components;
	}

	int sizeInBytes() {
		return this.readOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getTypeName()
	 */
	public char[] getTypeName() {
		return this.typeName;
	}

	public int getTargetType() {
		return this.targetType;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getLocalVariableRefenceInfoLength() {
		return this.localVariableTable != null ? this.localVariableTable.length : 0;
	}

	public ILocalVariableReferenceInfo[] getLocalVariableTable() {
		return this.localVariableTable;
	}

	public int getParameterIndex() {
		return this.parameterIndex;
	}

	public int getTypeParameterIndex() {
		return this.typeParameterIndex;
	}

	public int getTypeParameterBoundIndex() {
		return this.typeParameterBoundIndex;
	}

	public int getWildcardLocationType() {
		return this.wildcardLocationType;
	}

	public int[] getWildcardLocations() {
		return this.wildcardLocations;
	}

	public int[] getLocations() {
		return this.locations;
	}

	public int getAnnotationTypeIndex() {
		return this.annotationTypeIndex;
	}
}
