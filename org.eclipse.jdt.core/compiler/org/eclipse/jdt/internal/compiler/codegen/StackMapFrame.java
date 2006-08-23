/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import java.text.MessageFormat;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class StackMapFrame implements Cloneable {
	public static final int USED = 1;
	public static final int SAME_FRAME = 0;
	public static final int CHOP_FRAME = 1;
	public static final int APPEND_FRAME = 2;
	public static final int SAME_FRAME_EXTENDED = 3;
	public static final int FULL_FRAME = 4;
	public static final int SAME_LOCALS_1_STACK_ITEMS = 5;
	public static final int SAME_LOCALS_1_STACK_ITEMS_EXTENDED = 6;

	public int pc;
	public int numberOfStackItems;
	private int numberOfLocals;	
	public int localIndex;
	public VerificationTypeInfo[] locals;
	public VerificationTypeInfo[] stackItems;
	private int numberOfDifferentLocals = -1;
	public int tagBits;

public StackMapFrame() {
	this.numberOfLocals = -1;
	this.numberOfDifferentLocals = -1;
}
public int getFrameType(StackMapFrame prevFrame) {
	final int offsetDelta = this.getOffsetDelta(prevFrame);
	switch(this.numberOfStackItems) {
		case 0 :
			switch(this.numberOfDifferentLocals(prevFrame)) {
				case 0 :
					return offsetDelta <= 63 ? SAME_FRAME : SAME_FRAME_EXTENDED;
				case 1 :
				case 2 :
				case 3 :
					return APPEND_FRAME;
				case -1 :
				case -2 :
				case -3 :
					return CHOP_FRAME;
			}
			break;
		case 1 :
			switch(this.numberOfDifferentLocals(prevFrame)) {
				case 0 :
					return offsetDelta <= 63 ? SAME_LOCALS_1_STACK_ITEMS : SAME_LOCALS_1_STACK_ITEMS_EXTENDED;
			}
	}
	return FULL_FRAME;
}
public void addLocal(int resolvedPosition, VerificationTypeInfo info) {
	if (this.locals == null) {
		this.locals = new VerificationTypeInfo[resolvedPosition + 1];
		this.locals[resolvedPosition] = info;
	} else {
		final int length = this.locals.length;
		if (resolvedPosition >= length) {
			System.arraycopy(this.locals, 0, this.locals = new VerificationTypeInfo[resolvedPosition + 1], 0, length);
		}
		this.locals[resolvedPosition] = info;
	}
}
public void addStackItem(VerificationTypeInfo info) {
	if (this.stackItems == null) {
		this.stackItems = new VerificationTypeInfo[1];
		this.stackItems[0] = info;
		this.numberOfStackItems = 1;
	} else {
		final int length = this.stackItems.length;
		if (this.numberOfStackItems == length) {
			System.arraycopy(this.stackItems, 0, this.stackItems = new VerificationTypeInfo[length + 1], 0, length);
		}
		this.stackItems[this.numberOfStackItems++] = info;
	}
}
public void addStackItem(TypeBinding binding) {
	this.addStackItem(new VerificationTypeInfo(binding));
}
public Object clone() throws CloneNotSupportedException {
	StackMapFrame result = (StackMapFrame) super.clone();
	result.numberOfLocals = -1;
	result.numberOfDifferentLocals = -1;
	result.pc = this.pc;
	result.numberOfStackItems = this.numberOfStackItems;
	
	int length = this.locals == null ? 0 : this.locals.length;
	if (length != 0) {
		result.locals = new VerificationTypeInfo[length];
		for (int i = 0; i < length; i++) {
			final VerificationTypeInfo verificationTypeInfo = this.locals[i];
			if (verificationTypeInfo != null) {
				result.locals[i] = (VerificationTypeInfo) verificationTypeInfo.clone();
			}
		}
	}
	length = this.numberOfStackItems;
	if (length != 0) {
		result.stackItems = new VerificationTypeInfo[length];
		for (int i = 0; i < length; i++) {
			result.stackItems[i] = (VerificationTypeInfo) this.stackItems[i].clone();
		}
	}
	return result;
}
public int numberOfDiffentStackItems(StackMapFrame prevFrame) {
	if (prevFrame == null) {
		return this.numberOfStackItems;
	}
	return this.numberOfStackItems - prevFrame.numberOfStackItems;
}
public int numberOfDifferentLocals(StackMapFrame prevFrame) {
	if (this.numberOfDifferentLocals != -1) return this.numberOfDifferentLocals;
	if (prevFrame == null) {
		this.numberOfDifferentLocals = 0;
		return 0;
	}
	VerificationTypeInfo[] prevLocals = prevFrame.locals;
	VerificationTypeInfo[] currentLocals = this.locals;
	int prevLocalsLength = prevLocals == null ? 0 : prevLocals.length;
	int currentLocalsLength = currentLocals == null ? 0 : currentLocals.length;
	int prevNumberOfLocals = prevFrame.getNumberOfLocals();
	int currentNumberOfLocals = this.getNumberOfLocals();

	int result = 0;
	if (prevNumberOfLocals == 0) {
		if (currentNumberOfLocals != 0) {
			// need to check if there is a hole in the locals
			result = currentNumberOfLocals; // append if no hole and currentNumberOfLocals <= 3
			int counter = 0;
			for(int i = 0; i < currentLocalsLength && counter < currentNumberOfLocals; i++) {
				if (currentLocals[i] != null) {
					switch(currentLocals[i].id()) {
						case TypeIds.T_double :
						case TypeIds.T_long :
							i++;
					}
					counter++;
				} else {
					result = Integer.MAX_VALUE;
					this.numberOfDifferentLocals = result;
					return result;
				}
			}
		}
	} else if (currentNumberOfLocals == 0) {
		// need to check if there is a hole in the prev locals
		int counter = 0;
		result = -prevNumberOfLocals; // chop frame if no hole and prevNumberOfLocals <= 3
		for(int i = 0; i < prevLocalsLength && counter < prevNumberOfLocals; i++) {
			if (prevLocals[i] != null) {
				switch(prevLocals[i].id()) {
					case TypeIds.T_double :
					case TypeIds.T_long :
						i++;
				}
				counter++;
			} else {
				result = Integer.MAX_VALUE;
				this.numberOfDifferentLocals = result;
				return result;
			}
		}
	} else {
		// need to see if prevLocals matches with currentLocals
		int indexInPrevLocals = 0;
		int indexInCurrentLocals = 0;
		int currentLocalsCounter = 0;
		int prevLocalsCounter = 0;
		currentLocalsLoop: for (;indexInCurrentLocals < currentLocalsLength && currentLocalsCounter < currentNumberOfLocals; indexInCurrentLocals++) {
			VerificationTypeInfo currentLocal = currentLocals[indexInCurrentLocals];
			if (currentLocal != null) {
				currentLocalsCounter++;
				switch(currentLocal.id()) {
					case TypeIds.T_double :
					case TypeIds.T_long :
						indexInCurrentLocals++; // next entry  is null
				}
			}
			for (;indexInPrevLocals < prevLocalsLength && prevLocalsCounter < prevNumberOfLocals; indexInPrevLocals++) {
				VerificationTypeInfo prevLocal = prevLocals[indexInPrevLocals];
				if (prevLocal != null) {
					prevLocalsCounter++;
					switch(prevLocal.id()) {
						case TypeIds.T_double :
						case TypeIds.T_long :
							indexInPrevLocals++; // next entry  is null
					}
				}
				// now we need to check if prevLocal matches with currentLocal
				// the index must be the same
				if (equals(prevLocal, currentLocal) && indexInPrevLocals == indexInCurrentLocals) {
					if (result != 0) {
						result = Integer.MAX_VALUE;
						this.numberOfDifferentLocals = result;
						return result;
					}
				} else {
					// locals at the same location are not equals - this has to be a full frame
					result = Integer.MAX_VALUE;
					this.numberOfDifferentLocals = result;
					return result;						
				}
				indexInPrevLocals++;
				continue currentLocalsLoop;
			}
			// process remaining current locals
			if (currentLocal != null) {
				result++;
			} else {
				result = Integer.MAX_VALUE;
				this.numberOfDifferentLocals = result;
				return result;
			}
			indexInCurrentLocals++;
			break currentLocalsLoop;
		}
		if (currentLocalsCounter < currentNumberOfLocals) {
			for(;indexInCurrentLocals < currentLocalsLength && currentLocalsCounter < currentNumberOfLocals; indexInCurrentLocals++) {
				VerificationTypeInfo currentLocal = currentLocals[indexInCurrentLocals];
				if (currentLocal == null) {
					result = Integer.MAX_VALUE;
					this.numberOfDifferentLocals = result;
					return result;
				}
				result++;
				currentLocalsCounter++;
				switch(currentLocal.id()) {
					case TypeIds.T_double :
					case TypeIds.T_long :
						indexInCurrentLocals++; // next entry  is null
				}
			}
		} else if (prevLocalsCounter < prevNumberOfLocals) {
			result = -result;
			// process possible remaining prev locals
			for(; indexInPrevLocals < prevLocalsLength && prevLocalsCounter < prevNumberOfLocals; indexInPrevLocals++) {
				VerificationTypeInfo prevLocal = prevLocals[indexInPrevLocals];
				if (prevLocal == null) {
					result = Integer.MAX_VALUE;
					this.numberOfDifferentLocals = result;
					return result;
				}
				result--;
				prevLocalsCounter++;
				switch(prevLocal.id()) {
					case TypeIds.T_double :
					case TypeIds.T_long :
						indexInPrevLocals++; // next entry  is null
				}
			}
		}
	}
	this.numberOfDifferentLocals = result;
	return result;
}
public int getNumberOfLocals() {
	if (this.numberOfLocals != -1) {
		return this.numberOfLocals;
	}
	int result = 0;
	final int length = this.locals == null ? 0 : this.locals.length;
	for(int i = 0; i < length; i++) {
		if (this.locals[i] != null) {
			switch(this.locals[i].id()) {
				case TypeIds.T_double :
				case TypeIds.T_long :
					i++;
			}
			result++;
		}
	}
	this.numberOfLocals = result;
	return result;
}
public int getOffsetDelta(StackMapFrame prevFrame) {
	if (prevFrame == null) return this.pc;
	return prevFrame.pc == -1 ? this.pc : this.pc - prevFrame.pc - 1;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	printFrame(buffer, this);
	return String.valueOf(buffer);
}
private void printFrame(StringBuffer buffer, StackMapFrame frame) {
	String pattern = "[pc : {0} locals: {1} stack items: {2}\n{3}\n{4}\n]"; //$NON-NLS-1$
	int localsLength = frame.locals == null ? 0 : frame.locals.length;
	buffer.append(MessageFormat.format(
		pattern,
		new String[] {
			Integer.toString(frame.pc),
			Integer.toString(frame.getNumberOfLocals()),
			Integer.toString(frame.numberOfStackItems),
			print(frame.locals, localsLength),
			print(frame.stackItems, frame.numberOfStackItems)
		}
	));
}
private String print(VerificationTypeInfo[] infos, int length) {
	StringBuffer buffer = new StringBuffer();
	buffer.append('[');
	if (infos != null) {
		for (int i = 0; i < length; i++) {
			if (i != 0) buffer.append(',');
			if (infos[i] == null) {
				buffer.append("top"); //$NON-NLS-1$
				continue;
			}
			switch(infos[i].tag) {
				case VerificationTypeInfo.ITEM_NULL :
					buffer.append("null"); //$NON-NLS-1$
					break;
				case VerificationTypeInfo.ITEM_UNINITIALIZED_THIS :
					buffer.append("uninitialized_this"); //$NON-NLS-1$
					break;
				case VerificationTypeInfo.ITEM_TOP :
					buffer.append("top"); //$NON-NLS-1$
					break;
				case VerificationTypeInfo.ITEM_UNINITIALIZED :
					buffer.append("uninitialized(").append(infos[i].readableName()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				default:
					buffer.append(infos[i].readableName());
			}
		}
	}
	buffer.append(']');
	return String.valueOf(buffer);
}
public void setTopOfStack(TypeBinding typeBinding) {
	this.stackItems[this.numberOfStackItems - 1].setBinding(typeBinding);
}
public void initializeReceiver() {
	if (this.numberOfStackItems > 0) {
		this.stackItems[this.numberOfStackItems - 1].tag = VerificationTypeInfo.ITEM_OBJECT;
	}
}
public void removeLocals(int resolvedPosition) {
	if (this.locals == null || resolvedPosition < 0) return;
	if (resolvedPosition < this.locals.length) {
		this.locals[resolvedPosition] = null;
	}
}
public void putLocal(int resolvedPosition, VerificationTypeInfo info) {
	if (this.locals == null) {
		this.locals = new VerificationTypeInfo[resolvedPosition + 1];
		this.locals[resolvedPosition] = info;
	} else {
		final int length = this.locals.length;
		if (resolvedPosition >= length) {
			System.arraycopy(this.locals, 0, this.locals = new VerificationTypeInfo[resolvedPosition + 1], 0, length);
		}
		this.locals[resolvedPosition] = info;
	}
}
public void replaceWithElementType() {
	VerificationTypeInfo info = this.stackItems[this.numberOfStackItems - 1];
	try {
		VerificationTypeInfo info2 = (VerificationTypeInfo) info.clone();
		info2.replaceWithElementType();
		this.stackItems[this.numberOfStackItems - 1] = info2;
	} catch (CloneNotSupportedException e) {
		// ignore
	}
}
public int getIndexOfDifferentLocals(int differentLocalsCount) {
	for (int i = this.locals.length - 1; i >= 0; i--) {
		VerificationTypeInfo currentLocal = this.locals[i];
		if (currentLocal == null) {
			// check the previous slot
			continue;
		} else {
			differentLocalsCount--;
		}
		if (differentLocalsCount == 0) {
			return i;
		}
	}
	return 0;
}
private boolean equals(VerificationTypeInfo info, VerificationTypeInfo info2) {
	if (info == null) {
		return info2 == null;
	}
	if (info2 == null) return false;
	return info.equals(info2);
}
public void mergeLocals(StackMapFrame currentFrame) {
	int currentFrameLocalsLength = currentFrame.locals == null ? 0 : currentFrame.locals.length;
	int localsLength = this.locals == null ? 0 : this.locals.length;
	for (int i = 0, max = Math.min(currentFrameLocalsLength, localsLength); i < max; i++) {
		VerificationTypeInfo info = this.locals[i];
		VerificationTypeInfo info2 = currentFrame.locals[i];
		if (info == null) {
			if (info2 != null) {
				this.locals[i] = info2;
			}
		} else if (info2 == null) {
			this.locals[i] = null;
		} else {
			int tag1 = info.tag;
			int tag2 = info2.tag;
			if (tag1 != tag2) {
				this.locals[i] = null;
			}
		}
	}
}
}