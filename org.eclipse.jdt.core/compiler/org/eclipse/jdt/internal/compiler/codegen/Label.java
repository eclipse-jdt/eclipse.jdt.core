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
package org.eclipse.jdt.internal.compiler.codegen;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

/**
 * This type is a port of smalltalks JavaLabel
 */
public class Label {
	
	public CodeStream codeStream;
	public final static int POS_NOT_SET = -1;
	public int position = POS_NOT_SET; // position=POS_NOT_SET Then it's pos is not set.
	public int[] forwardReferences = new int[10]; // Add an overflow check here.
	public int forwardReferenceCount = 0;
	
	// Label tagbits
	public int tagBits;
	public final static int WIDE = 1;
	public final static int USED = 2;
	
public Label() {
	// for creating labels ahead of code generation
}

/**
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public Label(CodeStream codeStream) {
	this.codeStream = codeStream;
}

/**
 * Add a forward refrence for the array.
 */
void addForwardReference(int pos) {
	final int count = this.forwardReferenceCount;
	if (count >= 1) {
		int previousValue = this.forwardReferences[count - 1];
		if (previousValue < pos) {
			int length;
			if (count >= (length = this.forwardReferences.length))
				System.arraycopy(this.forwardReferences, 0, (this.forwardReferences = new int[2*length]), 0, length);
			this.forwardReferences[this.forwardReferenceCount++] = pos;			
		} else if (previousValue > pos) {
			int[] refs = this.forwardReferences;
			// check for duplicates
			for (int i = 0, max = this.forwardReferenceCount; i < max; i++) {
				if (refs[i] == pos) return; // already recorded
			}
			int length;
			if (count >= (length = refs.length))
				System.arraycopy(refs, 0, (this.forwardReferences = new int[2*length]), 0, length);
			this.forwardReferences[this.forwardReferenceCount++] = pos;
			Arrays.sort(this.forwardReferences, 0, this.forwardReferenceCount);
		}
	} else {
		int length;
		if (count >= (length = this.forwardReferences.length))
			System.arraycopy(this.forwardReferences, 0, (this.forwardReferences = new int[2*length]), 0, length);
		this.forwardReferences[this.forwardReferenceCount++] = pos;
	}
}

/**
 * Add a forward reference for the array.
 */
public void appendForwardReferencesFrom(Label otherLabel) {
	final int otherCount = otherLabel.forwardReferenceCount;
	if (otherCount == 0) return;
	// need to merge the two sorted arrays of forward references
	int[] mergedForwardReferences = new int[this.forwardReferenceCount + otherCount];
	int indexInMerge = 0;
	int j = 0;
	int i = 0;
	int max = this.forwardReferenceCount;
	int max2 = otherLabel.forwardReferenceCount;
	loop1 : for (; i < max; i++) {
		final int value1 = this.forwardReferences[i];
		for (; j < max2; j++) {
			final int value2 = otherLabel.forwardReferences[j];
			if (value1 < value2) {
				mergedForwardReferences[indexInMerge++] = value1;
				continue loop1;
			} else if (value1 == value2) {
				mergedForwardReferences[indexInMerge++] = value1;
				j++;
				continue loop1;
			} else {
				mergedForwardReferences[indexInMerge++] = value2;
			}
		}
		mergedForwardReferences[indexInMerge++] = value1;
	}
	for (; j < max2; j++) {
		mergedForwardReferences[indexInMerge++] = otherLabel.forwardReferences[j];
	}
	this.forwardReferences = mergedForwardReferences;
	this.forwardReferenceCount = indexInMerge;
}

/*
* Put down  a reference to the array at the location in the codestream.
*/
void branch() {
	this.tagBits |= USED;
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave two bytes free to generate the jump afterwards
		codeStream.position += 2;
		codeStream.classFileOffset += 2;
	} else {
		/*
		 * Position is set. Write it if it is not a wide branch.
		 */
		codeStream.writePosition(this);
	}
}

/*
* No support for wide branches yet
*/
void branchWide() {
	this.tagBits |= USED;
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		this.tagBits |= WIDE;
		codeStream.position += 4;
		codeStream.classFileOffset += 4;
	} else { //Position is set. Write it!
		codeStream.writeWidePosition(position - codeStream.position + 1);
	}
}

/**
 * @return boolean
 */
public boolean hasForwardReferences() {
	return forwardReferenceCount != 0;
}
public void initialize(CodeStream stream) {
    this.codeStream = stream;
   	this.position = POS_NOT_SET;
	this.forwardReferenceCount = 0; 
}
public boolean isCaseLabel() {
	return false;
}
public boolean isStandardLabel(){
	return true;
}
/*
* Place the label. If we have forward references resolve them.
*/
public void place() { // Currently lacking wide support.
	if (CodeStream.DEBUG) System.out.println("\t\t\t\t<place at: "+codeStream.position+" - "+ this); //$NON-NLS-1$ //$NON-NLS-2$
//	if ((this.tagBits & USED) == 0 && this.forwardReferenceCount == 0) {
//		return;
//	}

	if (position == POS_NOT_SET) {
		if ((this.tagBits & USED) != 0 || this.forwardReferenceCount != 0) {
			this.position = codeStream.getPosition();
		} else {
			this.position = codeStream.position;
		}
		codeStream.addLabel(this);
		int oldPosition = position;
		boolean isOptimizedBranch = false;
		if (forwardReferenceCount != 0) {
			isOptimizedBranch = (forwardReferences[forwardReferenceCount - 1] + 2 == position) && (codeStream.bCodeStream[codeStream.classFileOffset - 3] == Opcodes.OPC_goto);
			if (isOptimizedBranch) {
				codeStream.position = (position -= 3);
				codeStream.classFileOffset -= 3;
				forwardReferenceCount--;
				// also update the PCs in the related debug attributes
				/* OLD CODE
					int index = codeStream.pcToSourceMapSize - 1;
						while ((index >= 0) && (codeStream.pcToSourceMap[index][1] == oldPosition)) {
							codeStream.pcToSourceMap[index--][1] = position;
						}
				*/
				// Beginning of new code
				int index = codeStream.pcToSourceMapSize - 2;
				if (codeStream.lastEntryPC == oldPosition) {
					codeStream.lastEntryPC = position;
				}
				if ((index >= 0) && (codeStream.pcToSourceMap[index] == position)) {
					codeStream.pcToSourceMapSize-=2;
				}
				// end of new code
				if ((codeStream.generateAttributes & ClassFileConstants.ATTR_VARS) != 0) {
					LocalVariableBinding locals[] = codeStream.locals;
					for (int i = 0, max = locals.length; i < max; i++) {
						LocalVariableBinding local = locals[i];
						if ((local != null) && (local.initializationCount > 0)) {
							if (local.initializationPCs[((local.initializationCount - 1) << 1) + 1] == oldPosition) {
								// we want to prevent interval of size 0 to have a negative size.
								// see PR 1GIRQLA: ITPJCORE:ALL - ClassFormatError for local variable attribute
								local.initializationPCs[((local.initializationCount - 1) << 1) + 1] = position;
							}
							if (local.initializationPCs[(local.initializationCount - 1) << 1] == oldPosition) {
								local.initializationPCs[(local.initializationCount - 1) << 1] = position;
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < forwardReferenceCount; i++) {
			codeStream.writePosition(this, forwardReferences[i]);
		}
		// For all labels placed at that position we check if we need to rewrite the jump
		// offset. It is the case each time a label had a forward reference to the current position.
		// Like we change the current position, we have to change the jump offset. See 1F4IRD9 for more details.
		if (isOptimizedBranch) {
			this.codeStream.optimizeBranch(oldPosition, this);
		}
	}
}
/**
 * Print out the receiver
 */
public String toString() {
	String basic = getClass().getName();
	basic = basic.substring(basic.lastIndexOf('.')+1);
	StringBuffer buffer = new StringBuffer(basic); 
	buffer.append('@').append(Integer.toHexString(hashCode()));
	buffer.append("(position=").append(position); //$NON-NLS-1$
	buffer.append(", forwards = ["); //$NON-NLS-1$
	for (int i = 0; i < forwardReferenceCount - 1; i++)
		buffer.append(forwardReferences[i] + ", "); //$NON-NLS-1$
	if (forwardReferenceCount >= 1)
		buffer.append(forwardReferences[forwardReferenceCount-1]);
	buffer.append("] )"); //$NON-NLS-1$
	return buffer.toString();
}
}
