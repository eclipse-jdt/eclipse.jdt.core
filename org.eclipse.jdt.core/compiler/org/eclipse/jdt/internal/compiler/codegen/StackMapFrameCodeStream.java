/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class StackMapFrameCodeStream extends CodeStream {
	public static class ExceptionMarker implements Comparable {
		public char[] constantPoolName;
		public int pc;

		public ExceptionMarker(int pc, char[] constantPoolName) {
			this.pc = pc;
			this.constantPoolName = constantPoolName;
		}
		public int compareTo(Object o) {
			if (o instanceof ExceptionMarker) {
				return this.pc - ((ExceptionMarker) o).pc;
			}
			return 0;
		}
		public boolean equals(Object obj) {
			if (obj instanceof ExceptionMarker) {
				ExceptionMarker marker = (ExceptionMarker) obj;
				return this.pc == marker.pc && CharOperation.equals(this.constantPoolName, marker.constantPoolName);
			}
			return false;
		}
		public int hashCode() {
			return this.pc + this.constantPoolName.hashCode();
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append('(').append(this.pc).append(',').append(this.constantPoolName).append(')');
			return String.valueOf(buffer);
		}
	}

	public static class StackMarker {
		public int pc;
		public int delta;
		public TypeBinding typeBinding;

		public StackMarker(int pc, int delta, TypeBinding typeBinding) {
			this.pc = pc;
			this.typeBinding = typeBinding;
			this.delta = delta;
		}

		public StackMarker(int pc, int delta) {
			this.pc = pc;
			this.delta = delta;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append('(').append(this.pc).append(',').append(this.delta);
			if (this.typeBinding != null) {
				buffer
					.append(',')
					.append(this.typeBinding.qualifiedPackageName())
					.append(this.typeBinding.qualifiedSourceName());
			}
			buffer.append(')');
			return String.valueOf(buffer);
		}
	}

	static class FramePosition {
		int counter;
	}

	public int[] stateIndexes;
	public int stateIndexesCounter;
	private HashMap framePositions;
	public Set exceptionMarkers;
	public ArrayList stackMarkers;

public StackMapFrameCodeStream(ClassFile givenClassFile) {
	super(givenClassFile);
	this.generateAttributes |= ClassFileConstants.ATTR_STACK_MAP;
}
public void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
	loop: for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			// Check if the local is definitely assigned
			boolean isDefinitelyAssigned = isDefinitelyAssigned(scope, initStateIndex, localBinding);
			if (!isDefinitelyAssigned) {
				if (this.stateIndexes != null) {
					for (int j = 0, max = this.stateIndexesCounter; j < max; j++) {
						if (isDefinitelyAssigned(scope, this.stateIndexes[j], localBinding)) {
							if ((localBinding.initializationCount == 0) || (localBinding.initializationPCs[((localBinding.initializationCount - 1) << 1) + 1] != -1)) {
								/* There are two cases:
								 * 1) there is no initialization interval opened ==> add an opened interval
								 * 2) there is already some initialization intervals but the last one is closed ==> add an opened interval
								 * An opened interval means that the value at localBinding.initializationPCs[localBinding.initializationCount - 1][1]
								 * is equals to -1.
								 * initializationPCs is a collection of pairs of int:
								 * 	first value is the startPC and second value is the endPC. -1 one for the last value means that the interval
								 * 	is not closed yet.
								 */
								localBinding.recordInitializationStartPC(position);
							}
							continue loop;
						}
					}
				}
			} else {
				if ((localBinding.initializationCount == 0) || (localBinding.initializationPCs[((localBinding.initializationCount - 1) << 1) + 1] != -1)) {
					/* There are two cases:
					 * 1) there is no initialization interval opened ==> add an opened interval
					 * 2) there is already some initialization intervals but the last one is closed ==> add an opened interval
					 * An opened interval means that the value at localBinding.initializationPCs[localBinding.initializationCount - 1][1]
					 * is equals to -1.
					 * initializationPCs is a collection of pairs of int:
					 * 	first value is the startPC and second value is the endPC. -1 one for the last value means that the interval
					 * 	is not closed yet.
					 */
					localBinding.recordInitializationStartPC(position);
				}
			}
		}
	}
}
public void addExceptionMarker(int pc, TypeBinding typeBinding) {
	if (this.exceptionMarkers == null) {
		this.exceptionMarkers = new HashSet();
	}
	if (typeBinding == null) {
		this.exceptionMarkers.add(new ExceptionMarker(pc, ConstantPool.JavaLangThrowableConstantPoolName));
	} else {
		switch(typeBinding.id) {
			case TypeIds.T_null :
				this.exceptionMarkers.add(new ExceptionMarker(pc, ConstantPool.JavaLangClassNotFoundExceptionConstantPoolName));
				break;
			case TypeIds.T_long :
				this.exceptionMarkers.add(new ExceptionMarker(pc, ConstantPool.JavaLangNoSuchFieldErrorConstantPoolName));
				break;
			default:
				this.exceptionMarkers.add(new ExceptionMarker(pc, typeBinding.constantPoolName()));
		}
	}
}
public void addFramePosition(int pc) {
	Integer newEntry = new Integer(pc);
	FramePosition value;
	if ((value = (FramePosition) this.framePositions.get(newEntry)) != null) {
		value.counter++;
	} else {
		this.framePositions.put(newEntry, new FramePosition());
	}
}
public void optimizeBranch(int oldPosition, BranchLabel lbl) {
	super.optimizeBranch(oldPosition, lbl);
	this.removeFramePosition(oldPosition);
}
public void removeFramePosition(int pc) {
	Integer entry = new Integer(pc);
	FramePosition value;
	if ((value = (FramePosition) this.framePositions.get(entry)) != null) {
		value.counter--;
		if (value.counter <= 0) {
			this.framePositions.remove(entry);
		}
	}
}
public void addVariable(LocalVariableBinding localBinding) {
	if (localBinding.initializationPCs == null) {
		record(localBinding);
	}
	localBinding.recordInitializationStartPC(position);
}
public void decrStackSize(int offset) {
	super.decrStackSize(offset);
	if (this.stackMarkers == null) {
		this.stackMarkers = new ArrayList();
		this.stackMarkers.add(new StackMarker(this.position, -1));
	} else {
		int size = this.stackMarkers.size();
		if (size == 0 || ((StackMarker) this.stackMarkers.get(size - 1)).pc != this.position) {
			this.stackMarkers.add(new StackMarker(this.position, -1));
		}
	}
}
public void recordExpressionType(TypeBinding typeBinding) {
	if (this.stackMarkers == null) {
		this.stackMarkers = new ArrayList();
		this.stackMarkers.add(new StackMarker(this.position, 0, typeBinding));
	} else {
		int size = this.stackMarkers.size();
		if (size == 0 || ((StackMarker) this.stackMarkers.get(size - 1)).pc != this.position) {
			this.stackMarkers.add(new StackMarker(this.position, 0, typeBinding));
		}
	}
}
public ExceptionMarker[] getExceptionMarkers() {
	Set exceptionMarkerSet = this.exceptionMarkers;
	if (this.exceptionMarkers == null) return null;
	int size = exceptionMarkerSet.size();
	ExceptionMarker[] markers = new ExceptionMarker[size];
	int n = 0;
	for (Iterator iterator = exceptionMarkerSet.iterator(); iterator.hasNext(); ) {
		markers[n++] = (ExceptionMarker) iterator.next();
	}
	Arrays.sort(markers);
//  System.out.print('[');
//  for (int n = 0; n < size; n++) {
//  	if (n != 0) System.out.print(',');
//  	System.out.print(positions[n]);
//  }
//  System.out.println(']');
	return markers;
}
public int[] getFramePositions() {
	Set set = this.framePositions.keySet();
	int size = set.size();
	int[] positions = new int[size];
	int n = 0;
	for (Iterator iterator = set.iterator(); iterator.hasNext(); ) {
		positions[n++] = ((Integer) iterator.next()).intValue();
	}
	Arrays.sort(positions);
//  System.out.print('[');
//  for (int n = 0; n < size; n++) {
//  	if (n != 0) System.out.print(',');
//  	System.out.print(positions[n]);
//  }
//  System.out.println(']');
	return positions;
}
public boolean hasFramePositions() {
	return this.framePositions.size() != 0;
}
public void init(ClassFile targetClassFile) {
	super.init(targetClassFile);
	this.stateIndexesCounter = 0;
	if (this.framePositions != null) {
		this.framePositions.clear();
	}
	if (this.exceptionMarkers != null) {
		this.exceptionMarkers.clear();
	}
	if (this.stackMarkers != null) {
		this.stackMarkers.clear();
	}
}

public void initializeMaxLocals(MethodBinding methodBinding) {
	super.initializeMaxLocals(methodBinding);
	if (this.framePositions == null) {
		this.framePositions = new HashMap();
	} else {
		this.framePositions.clear();
	}
}
public void popStateIndex() {
	this.stateIndexesCounter--;
}
public void pushStateIndex(int naturalExitMergeInitStateIndex) {
	if (this.stateIndexes == null) {
		this.stateIndexes = new int[3];
	}
	int length = this.stateIndexes.length;
	if (length == this.stateIndexesCounter) {
		// resize
		System.arraycopy(this.stateIndexes, 0, (this.stateIndexes = new int[length * 2]), 0, length);
	}
	this.stateIndexes[this.stateIndexesCounter++] = naturalExitMergeInitStateIndex;
}
public void removeNotDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	int index = this.visibleLocalsCount;
	loop : for (int i = 0; i < index; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null && localBinding.initializationCount > 0) {
			boolean isDefinitelyAssigned = isDefinitelyAssigned(scope, initStateIndex, localBinding);
			if (!isDefinitelyAssigned) {
				if (this.stateIndexes != null) {
					for (int j = 0, max = this.stateIndexesCounter; j < max; j++) {
						if (isDefinitelyAssigned(scope, this.stateIndexes[j], localBinding)) {
							continue loop;
						}
					}
				}
				localBinding.recordInitializationEndPC(position);
			}
		}
	}
}
public void reset(ClassFile givenClassFile) {
	super.reset(givenClassFile);
	this.stateIndexesCounter = 0;
	if (this.framePositions != null) {
		this.framePositions.clear();
	}
	if (this.exceptionMarkers != null) {
		this.exceptionMarkers.clear();
	}
	if (this.stackMarkers != null) {
		this.stackMarkers.clear();
	}
}
protected void writePosition(BranchLabel label) {
	super.writePosition(label);
	addFramePosition(label.position);
}
protected void writePosition(BranchLabel label, int forwardReference) {
	super.writePosition(label, forwardReference);
	addFramePosition(label.position);
}
protected void writeSignedWord(int pos, int value) {
	super.writeSignedWord(pos, value);
	addFramePosition(this.position);
}
protected void writeWidePosition(BranchLabel label) {
	super.writeWidePosition(label);
	addFramePosition(label.position);
}
public void areturn() {
	super.areturn();
	this.addFramePosition(this.position);
}
public void ireturn() {
	super.ireturn();
	this.addFramePosition(this.position);
}
public void lreturn() {
	super.lreturn();
	this.addFramePosition(this.position);
}
public void freturn() {
	super.freturn();
	this.addFramePosition(this.position);
}
public void dreturn() {
	super.dreturn();
	this.addFramePosition(this.position);
}
public void return_() {
	super.return_();
	this.addFramePosition(this.position);
}
public void athrow() {
	super.athrow();
	this.addFramePosition(this.position);
}
public void pushOnStack(TypeBinding binding) {
	super.pushOnStack(binding);
	if (this.stackMarkers == null) {
		this.stackMarkers = new ArrayList();
		this.stackMarkers.add(new StackMarker(this.position, 1, binding));
	} else {
		int size = this.stackMarkers.size();
		if (size == 0 || ((StackMarker) this.stackMarkers.get(size - 1)).pc != this.position) {
			this.stackMarkers.add(new StackMarker(this.position, 1, binding));
		}
	}
}
public void pushExceptionOnStack(TypeBinding binding) {
	super.pushExceptionOnStack(binding);
	this.addExceptionMarker(this.position, binding);
}
public void goto_(BranchLabel label) {
	super.goto_(label);
	this.addFramePosition(this.position);
}
public void goto_w(BranchLabel label) {
	super.goto_w(label);
	this.addFramePosition(this.position);
}
}
