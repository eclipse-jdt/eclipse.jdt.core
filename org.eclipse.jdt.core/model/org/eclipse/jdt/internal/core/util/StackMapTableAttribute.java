/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IConstantPool;
/**
 * Default implementation of ILineNumberAttribute.
 */
public class StackMapTableAttribute
	extends ClassFileAttribute {

	private static final byte[] NO_ENTRIES = new byte[0];
	

	private byte[] bytes;
	
	/**
	 * Constructor for LineNumberAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public StackMapTableAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		
		final int length = (int) u4At(classFileBytes, 2, offset);
		
		if (length != 0) {
			System.arraycopy(classFileBytes, offset + 6, this.bytes = new byte[length], 0, length);
		} else {
			this.bytes = NO_ENTRIES;
		}
	}
	/**
	 */
	public byte[] getBytes() {
		return this.bytes;
	}
}
