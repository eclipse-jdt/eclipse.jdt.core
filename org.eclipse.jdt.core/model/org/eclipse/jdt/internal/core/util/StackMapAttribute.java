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
import org.eclipse.jdt.core.util.IStackMapAttribute;
import org.eclipse.jdt.core.util.IStackMapFrame;

/**
 * @since 3.0
 */
public class StackMapAttribute extends ClassFileAttribute implements IStackMapAttribute {
	
	private static final IStackMapFrame[] NO_ENTRIES = new IStackMapFrame[0];
	private int numberOfStackFrames;
	private IStackMapFrame[] stackFrames;
	
	StackMapAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset,
		boolean extendedOffset,
		boolean extendedLocals,
		boolean extendedStack) throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		final int counter = u2At(classFileBytes, 6, offset);
		this.numberOfStackFrames = counter;
		if (counter != 0) {
			int readOffset = 8;
			this.stackFrames = new StackMapFrame[counter];
			for (int i = 0; i < counter; i++) {
				StackMapFrame currentStackFrame = new StackMapFrame(classFileBytes, constantPool, offset + readOffset, extendedOffset, extendedLocals, extendedStack);
				this.stackFrames[i] = currentStackFrame;
				readOffset += currentStackFrame.getSize();
			}
		} else {
			this.stackFrames = NO_ENTRIES;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapAttribute#getNumberOfStackMapFrames()
	 */
	public int getNumberOfStackMapFrames() {
		return this.numberOfStackFrames;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapAttribute#getStackMapFrames()
	 */
	public IStackMapFrame[] getStackMapFrames() {
		return this.stackFrames;
	}
}
