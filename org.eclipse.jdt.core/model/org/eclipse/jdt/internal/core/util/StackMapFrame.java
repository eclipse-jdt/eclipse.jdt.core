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
import org.eclipse.jdt.core.util.IStackMapFrame;
import org.eclipse.jdt.core.util.IVerificationTypeInfo;

/**
 * @since 3.0
 */
public class StackMapFrame extends ClassFileStruct implements IStackMapFrame {
	
	private static final IVerificationTypeInfo[] NO_ENTRIES = new IVerificationTypeInfo[0];
	private int stackFrameOffset;
	private int numberOfLocals;
	private int numberOfStackItems;
	private IVerificationTypeInfo[] locals;
	private IVerificationTypeInfo[] stackItems;
	private int size;
	
	StackMapFrame(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset,
		boolean extendedOffset,
		boolean extendedLocals,
		boolean extendedStack) throws ClassFormatException {
		
		int readOffset = 0;
		if (extendedOffset) {
			this.stackFrameOffset = (int) u4At(classFileBytes, readOffset, offset);
			readOffset+= 4;
		} else {
			this.stackFrameOffset = u2At(classFileBytes, readOffset, offset);
			readOffset += 2;
		}
		int localsCounter;
		if (extendedLocals) {
			localsCounter = (int) u4At(classFileBytes, readOffset, offset);
			readOffset += 4;
		} else {
			localsCounter = u2At(classFileBytes, readOffset, offset);
			readOffset += 2;
		}
		this.numberOfLocals = localsCounter;
		if (localsCounter > 0) {
			this.locals = new IVerificationTypeInfo[localsCounter];
			for (int i = 0; i < localsCounter; i++) {
				VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(classFileBytes, constantPool, offset + readOffset);
				this.locals[i] = verificationTypeInfo;
				readOffset += verificationTypeInfo.getSize();
			}
		} else {
			this.locals = NO_ENTRIES;
		}
		int stackCounter;
		if (extendedStack) {
			stackCounter = (int) u4At(classFileBytes, readOffset, offset);
			readOffset += 4;
		} else {
			stackCounter = u2At(classFileBytes, readOffset, offset);
			readOffset += 2;
		}
		this.numberOfStackItems = stackCounter;
		if (stackCounter > 0) {
			this.stackItems = new IVerificationTypeInfo[stackCounter];
			for (int i = 0; i < stackCounter; i++) {
				VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(classFileBytes, constantPool, offset + readOffset);
				this.stackItems[i] = verificationTypeInfo;
				readOffset += verificationTypeInfo.getSize();
			}
		} else {
			this.stackItems = NO_ENTRIES;
		}
		this.size = readOffset;
	}
	
	int getSize() {
		return this.size;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapFrame#getOffset()
	 */
	public int getOffset() {
		return this.stackFrameOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapFrame#getNumberOfLocals()
	 */
	public int getNumberOfLocals() {
		return this.numberOfLocals;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapFrame#getNumberOfStackItems()
	 */
	public int getNumberOfStackItems() {
		return this.numberOfStackItems;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapFrame#getLocals()
	 */
	public IVerificationTypeInfo[] getLocals() {
		return this.locals;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IStackMapFrame#getStackItems()
	 */
	public IVerificationTypeInfo[] getStackItems() {
		return this.stackItems;
	}
}
