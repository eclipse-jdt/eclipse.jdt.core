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
import org.eclipse.jdt.core.util.IAnnotationComponent;
import org.eclipse.jdt.core.util.IConstantPool;

/**
 * Default implementation of IAnnotation
 */
public class Annotation extends ClassFileStruct implements IAnnotation {

	private static final IAnnotationComponent[] NO_ENTRIES = new IAnnotationComponent[0];
	
	private int typeIndex;
	private int componentsNumber;
	private IAnnotationComponent[] components;
	private int readOffset;
	
	/**
	 * Constructor for Annotation.
	 * 
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public Annotation(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		
		this.typeIndex = u2At(classFileBytes, 0, offset);
		final int length = u2At(classFileBytes, 2, offset);
		this.componentsNumber = length;
		if (length != 0) {
			this.readOffset = 4;
			this.components = new IAnnotationComponent[length];
			for (int i = 0; i < length; i++) {
				AnnotationComponent component = new AnnotationComponent(classFileBytes, constantPool, offset + readOffset);
				this.components[i++] = component;
				this.readOffset += component.sizeInBytes();
			}
		} else {
			this.components = NO_ENTRIES;
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
}
