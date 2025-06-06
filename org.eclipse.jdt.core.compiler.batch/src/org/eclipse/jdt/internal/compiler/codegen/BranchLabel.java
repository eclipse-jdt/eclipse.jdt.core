/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import java.util.Arrays;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.OperandStack.NullStack;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class BranchLabel extends Label {

	private int[] forwardReferences = new int[10]; // Add an overflow check here.
	private int forwardReferenceCount = 0;
	BranchLabel delegate; //

	// Label tagbits
	public int tagBits;
	protected int targetStackDepth = -1;
	public final static int WIDE = 1;
	public final static int VALIDATE = 2;
	private OperandStack operandStack;


public BranchLabel() {
	// for creating labels ahead of code generation
}

/**
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public BranchLabel(CodeStream codeStream) {
	super(codeStream);
	if (this.codeStream.classFile.referenceBinding.scope.compilerOptions().validateOperandStack)
		this.tagBits |= VALIDATE;
}

/**
 * Add a forward refrence for the array.
 */
void addForwardReference(int pos) {
	if (this.delegate != null) {
		this.delegate.addForwardReference(pos);
		return;
	}
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
 * Makes the current label inline all references to the other label
 */
public void becomeDelegateFor(BranchLabel otherLabel) {
	// other label is delegating to receiver from now on
	otherLabel.delegate = this;

	trackStackDepth(true);

	// all existing forward refs to other label are inlined into current label
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

// If branch target can be reached in more ways than one, verify that operand stack for every edge/arc is mutually compatible
private boolean compatibleOperandStacks(OperandStack stackNow, OperandStack stackEarlier) {
	if ((this.tagBits & VALIDATE) == 0)
		return true;
	if (stackNow.size() != stackEarlier.size())
		return false;
	int depth = 0;
	for (int i = 0, size = stackNow.size(); i < size; i++) {
		TypeBinding tn = stackNow.get(i);
		TypeBinding te = stackEarlier.get(i);
		if (TypeBinding.notEquals(tn, te)) {
			if (!tn.isCompatibleWith(te) && !te.isCompatibleWith(tn)) {
				if (this.codeStream.classFile.referenceBinding.scope.lowerUpperBound(new TypeBinding[] {te, tn}) == null)
					return false;
			}
		}
		depth += TypeIds.getCategory(tn.id);
	}
	return stackNow instanceof NullStack || depth == this.targetStackDepth;
}

protected void trackStackDepth(boolean branch) {
	/* Control can reach an instruction with a label in two ways: (1) via a branch using that label or (2) by falling through from the previous instruction.
	   In both cases, we KNOW the stack depth at the instruction from which control flows to the instruction with the label.
	   Control cannot reach the instruction with a label by falling through if the previous instruction completes abruptly via a goto/return/throw etc
	   this.codeStream.lastAbruptCompletion == this.position ==> instruction prior to the one with the label is a goto/return/throw
	*/
	boolean sourceDepthKnown = branch || this.codeStream.lastAbruptCompletion != this.position;
	if (this.targetStackDepth == -1) {
		// First branch to this label || placement of this label
		if (sourceDepthKnown) {
			if (this.codeStream.stackDepth < 0) {
				this.codeStream.classFile.referenceBinding.scope.problemReporter()
						.operandStackSizeInappropriate(this.codeStream.classFile.referenceBinding.scope.referenceContext);
				this.codeStream.stackDepth = 0; // FWIW
				this.codeStream.operandStack.clear();
			}
			this.targetStackDepth = this.codeStream.stackDepth;
			this.operandStack = this.codeStream.operandStack.copy();

			if (!this.operandStack.depthEquals(this.targetStackDepth)) {
				this.codeStream.classFile.referenceBinding.scope.problemReporter().operandStackSizeInappropriate(
						this.codeStream.classFile.referenceBinding.scope.referenceContext);
			}
		} // else: previous instruction completes abruptly via goto/return/throw: Wait for a backward branch to be emitted.
	} else {
		// Stack depth known at label having encountered a previous branch and/or having fallen through to label
		if (sourceDepthKnown) {
			if (this.targetStackDepth != this.codeStream.stackDepth || !compatibleOperandStacks(this.codeStream.operandStack, this.operandStack)) {
				this.codeStream.classFile.referenceBinding.scope.problemReporter().operandStackSizeInappropriate(
						this.codeStream.classFile.referenceBinding.scope.referenceContext);
				if (this.targetStackDepth < this.codeStream.stackDepth) {
					this.targetStackDepth = this.codeStream.stackDepth; // FWIW, pick the higher water mark.
					this.operandStack = this.codeStream.operandStack.copy();
				}
			}
		} else {
			this.codeStream.stackDepth = this.targetStackDepth;
			this.codeStream.operandStack = this.operandStack.copy();
		}
	}
}

/*
* Put down  a reference to the array at the location in the codestream.
*/
void branch() {
	if (this.delegate != null) {
		this.delegate.branch();
		return;
	}
	if (this.position == Label.POS_NOT_SET) {
		addForwardReference(this.codeStream.position);
		// Leave two bytes free to generate the jump afterwards
		this.codeStream.position += 2;
		this.codeStream.classFileOffset += 2;
	} else {
		/*
		 * Position is set. Write it if it is not a wide branch.
		 */
		this.codeStream.writePosition(this);
	}
	trackStackDepth(true);
}

void branchWide() {
	if (this.delegate != null) {
		this.delegate.branchWide();
		return;
	}
	if (this.position == Label.POS_NOT_SET) {
		addForwardReference(this.codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		this.tagBits |= BranchLabel.WIDE;
		this.codeStream.position += 4;
		this.codeStream.classFileOffset += 4;
	} else { //Position is set. Write it!
		this.codeStream.writeWidePosition(this);
	}
	trackStackDepth(true);
}

public int forwardReferenceCount() {
	if (this.delegate != null) return this.delegate.forwardReferenceCount();
	return this.forwardReferenceCount;
}
public int[] forwardReferences() {
	if (this.delegate != null) return this.delegate.forwardReferences();
	return this.forwardReferences;
}
public void initialize(CodeStream stream) {
    this.codeStream = stream;
   	this.position = Label.POS_NOT_SET;
	this.forwardReferenceCount = 0;
	this.delegate = null;
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
@Override
public void place() { // Currently lacking wide support.
//	if ((this.tagBits & USED) == 0 && this.forwardReferenceCount == 0) {
//		return;
//	}

	//TODO how can position be set already ? cannot place more than once
	if (this.position == Label.POS_NOT_SET) {
		this.position = this.codeStream.position;
		this.codeStream.addLabel(this);
		int oldPosition = this.position;
		boolean isOptimizedBranch = false;
		if (this.forwardReferenceCount != 0) {
			isOptimizedBranch = (this.forwardReferences[this.forwardReferenceCount - 1] + 2 == this.position) && (this.codeStream.bCodeStream[this.codeStream.classFileOffset - 3] == Opcodes.OPC_goto);
			if (isOptimizedBranch) {
				if (this.codeStream.lastAbruptCompletion == this.position) {
					this.codeStream.lastAbruptCompletion = -1;
				}
				this.codeStream.position = (this.position -= 3);
				this.codeStream.classFileOffset -= 3;
				this.forwardReferenceCount--;
				if (this.codeStream.lastEntryPC == oldPosition) {
					this.codeStream.lastEntryPC = this.position;
				}
				// end of new code

				LocalVariableBinding locals[] = this.codeStream.locals;
				for (LocalVariableBinding local : locals) {
					if ((local != null) && (local.initializationCount > 0)) {
						if (local.initializationPCs[((local.initializationCount - 1) << 1) + 1] == oldPosition) {
							// we want to prevent interval of size 0 to have a negative size.
							// see PR 1GIRQLA: ITPJCORE:ALL - ClassFormatError for local variable attribute
							local.initializationPCs[((local.initializationCount - 1) << 1) + 1] = this.position;
						}
						if (local.initializationPCs[(local.initializationCount - 1) << 1] == oldPosition) {
							local.initializationPCs[(local.initializationCount - 1) << 1] = this.position;
						}
					}
				}

				if ((this.codeStream.generateAttributes & ClassFileConstants.ATTR_LINES) != 0) {
					// we need to remove all entries that is beyond this.position inside the pcToSourcerMap table
					this.codeStream.removeUnusedPcToSourceMapEntries();
				}
			}
		}
		for (int i = 0; i < this.forwardReferenceCount; i++) {
			this.codeStream.writePosition(this, this.forwardReferences[i]);
		}
		// For all labels placed at that position we check if we need to rewrite the jump
		// offset. It is the case each time a label had a forward reference to the current position.
		// Like we change the current position, we have to change the jump offset. See 1F4IRD9 for more details.
		if (isOptimizedBranch) {
			this.codeStream.optimizeBranch(oldPosition, this);
		}
	}
	trackStackDepth(false);
}

/**
 * Print out the receiver
 */
@Override
public String toString() {
	String basic = getClass().getName();
	basic = basic.substring(basic.lastIndexOf('.')+1);
	StringBuilder buffer = new StringBuilder(basic);
	buffer.append('@').append(Integer.toHexString(hashCode()));
	buffer.append("(position=").append(this.position); //$NON-NLS-1$
	if (this.delegate != null) buffer.append("delegate=").append(this.delegate); //$NON-NLS-1$
	buffer.append(", forwards = ["); //$NON-NLS-1$
	for (int i = 0; i < this.forwardReferenceCount - 1; i++)
		buffer.append(this.forwardReferences[i] + ", "); //$NON-NLS-1$
	if (this.forwardReferenceCount >= 1)
		buffer.append(this.forwardReferences[this.forwardReferenceCount-1]);
	buffer.append("] )"); //$NON-NLS-1$
	return buffer.toString();
}
}
