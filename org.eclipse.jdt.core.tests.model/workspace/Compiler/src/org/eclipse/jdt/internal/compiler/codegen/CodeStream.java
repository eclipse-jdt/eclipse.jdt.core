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
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.*;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CodeStream implements OperatorIds, ClassFileConstants, Opcodes, BaseTypes, TypeConstants, TypeIds {

	public static final boolean DEBUG = false;
	
	// It will be responsible for the following items.
	// -> Tracking Max Stack.

	public int stackMax; // Use Ints to keep from using extra bc when adding
	public int stackDepth; // Use Ints to keep from using extra bc when adding
	public int maxLocals;
	public static final int LABELS_INCREMENT = 5;
	public byte[] bCodeStream;
	public int pcToSourceMapSize;
	public int[] pcToSourceMap = new int[24];
	public int lastEntryPC; // last entry recorded
	public int[] lineSeparatorPositions;
	public int position; // So when first set can be incremented
	public int classFileOffset;
	public int startingClassFileOffset; // I need to keep the starting point inside the byte array
	public ConstantPool constantPool; // The constant pool used to generate bytecodes that need to store information into the constant pool
	public ClassFile classFile; // The current classfile it is associated to.
	// local variable attributes output
	public static final int LOCALS_INCREMENT = 10;
	public LocalVariableBinding[] locals = new LocalVariableBinding[LOCALS_INCREMENT];
	static LocalVariableBinding[] noLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	public LocalVariableBinding[] visibleLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	static LocalVariableBinding[] noVisibleLocals = new LocalVariableBinding[LOCALS_INCREMENT];
	int visibleLocalsCount;
	public AbstractMethodDeclaration methodDeclaration;
	public ExceptionLabel[] exceptionHandlers = new ExceptionLabel[LABELS_INCREMENT];
	static ExceptionLabel[] noExceptionHandlers = new ExceptionLabel[LABELS_INCREMENT];
	public int exceptionHandlersIndex;
	public int exceptionHandlersCounter;
	
	public static FieldBinding[] ImplicitThis = new FieldBinding[] {};
	public boolean generateLineNumberAttributes;
	public boolean generateLocalVariableTableAttributes;
	public boolean preserveUnusedLocals;
	// store all the labels placed at the current position to be able to optimize
	// a jump to the next bytecode.
	public Label[] labels = new Label[LABELS_INCREMENT];
	static Label[] noLabels = new Label[LABELS_INCREMENT];
	public int countLabels;
	public int allLocalsCounter;
	public int maxFieldCount;
	// to handle goto_w
	public boolean wideMode = false;
	public static final CompilationResult RESTART_IN_WIDE_MODE = new CompilationResult((char[])null, 0, 0, 0);
	
	// target level to manage different code generation between different target levels
	private long targetLevel;
	
public CodeStream(ClassFile classFile, long targetLevel) {
	this.targetLevel = targetLevel;
	this.generateLineNumberAttributes = (classFile.produceDebugAttributes & CompilerOptions.Lines) != 0;
	this.generateLocalVariableTableAttributes = (classFile.produceDebugAttributes & CompilerOptions.Vars) != 0;
	if (this.generateLineNumberAttributes) {
		this.lineSeparatorPositions = classFile.referenceBinding.scope.referenceCompilationUnit().compilationResult.lineSeparatorPositions;
	}
}
final public void aaload() {
	if (DEBUG) System.out.println(position + "\t\taaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aaload;
}
final public void aastore() {
	if (DEBUG) System.out.println(position + "\t\taastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aastore;
}
final public void aconst_null() {
	if (DEBUG) System.out.println(position + "\t\taconst_null"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aconst_null;
}
public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
	if (!generateLocalVariableTableAttributes)
		return;
/*	if (initStateIndex == lastInitStateIndexWhenAddingInits)
		return;
	lastInitStateIndexWhenAddingInits = initStateIndex;
	if (lastInitStateIndexWhenRemovingInits != initStateIndex){
		lastInitStateIndexWhenRemovingInits = -2; // reinitialize remove index 
		// remove(1)-add(1)-remove(1) -> ignore second remove
		// remove(1)-add(2)-remove(1) -> perform second remove
	}
	
*/	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			// Check if the local is definitely assigned
			if ((initStateIndex != -1) && isDefinitelyAssigned(scope, initStateIndex, localBinding)) {
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
public void addLabel(Label aLabel) {
	if (countLabels == labels.length)
		System.arraycopy(labels, 0, labels = new Label[countLabels + LABELS_INCREMENT], 0, countLabels);
	labels[countLabels++] = aLabel;
}
public void addVisibleLocalVariable(LocalVariableBinding localBinding) {
	if (!generateLocalVariableTableAttributes)
		return;

	if (visibleLocalsCount >= visibleLocals.length)
		System.arraycopy(visibleLocals, 0, visibleLocals = new LocalVariableBinding[visibleLocalsCount * 2], 0, visibleLocalsCount);
	visibleLocals[visibleLocalsCount++] = localBinding;
}
final public void aload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\taload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_aload;
		writeUnsignedShort(iArg);
	} else {
		// Don't need to use the wide bytecode
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_aload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void aload_0() {
	if (DEBUG) System.out.println(position + "\t\taload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aload_0;
}
final public void aload_1() {
	if (DEBUG) System.out.println(position + "\t\taload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aload_1;
}
final public void aload_2() {
	if (DEBUG) System.out.println(position + "\t\taload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aload_2;
}
final public void aload_3() {
	if (DEBUG) System.out.println(position + "\t\taload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_aload_3;
}
public final void anewarray(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tanewarray: " + typeBinding); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_anewarray;
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
final public void areturn() {
	if (DEBUG) System.out.println(position + "\t\tareturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_areturn;
}
public void arrayAt(int typeBindingID) {
	switch (typeBindingID) {
		case T_int :
			this.iaload();
			break;
		case T_byte :
		case T_boolean :
			this.baload();
			break;
		case T_short :
			this.saload();
			break;
		case T_char :
			this.caload();
			break;
		case T_long :
			this.laload();
			break;
		case T_float :
			this.faload();
			break;
		case T_double :
			this.daload();
			break;
		default :
			this.aaload();
	}
}
public void arrayAtPut(int elementTypeID, boolean valueRequired) {
	switch (elementTypeID) {
		case T_int :
			if (valueRequired)
				dup_x2();
			iastore();
			break;
		case T_byte :
		case T_boolean :
			if (valueRequired)
				dup_x2();
			bastore();
			break;
		case T_short :
			if (valueRequired)
				dup_x2();
			sastore();
			break;
		case T_char :
			if (valueRequired)
				dup_x2();
			castore();
			break;
		case T_long :
			if (valueRequired)
				dup2_x2();
			lastore();
			break;
		case T_float :
			if (valueRequired)
				dup_x2();
			fastore();
			break;
		case T_double :
			if (valueRequired)
				dup2_x2();
			dastore();
			break;
		default :
			if (valueRequired)
				dup_x2();
			aastore();
	}
}
final public void arraylength() {
	if (DEBUG) System.out.println(position + "\t\tarraylength"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_arraylength;
}
final public void astore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tastore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position+=2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_astore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position+=2;
		bCodeStream[classFileOffset++] = OPC_astore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void astore_0() {
	if (DEBUG) System.out.println(position + "\t\tastore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_astore_0;
}
final public void astore_1() {
	if (DEBUG) System.out.println(position + "\t\tastore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_astore_1;
}
final public void astore_2() {
	if (DEBUG) System.out.println(position + "\t\tastore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_astore_2;
}
final public void astore_3() {
	if (DEBUG) System.out.println(position + "\t\tastore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_astore_3;
}
final public void athrow() {
	if (DEBUG) System.out.println(position + "\t\tathrow"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_athrow;
}
final public void baload() {
	if (DEBUG) System.out.println(position + "\t\tbaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_baload;
}
final public void bastore() {
	if (DEBUG) System.out.println(position + "\t\tbastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_bastore;
}
final public void bipush(byte b) {
	if (DEBUG) System.out.println(position + "\t\tbipush "+b); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = OPC_bipush;
	bCodeStream[classFileOffset++] = b;
}
final public void caload() {
	if (DEBUG) System.out.println(position + "\t\tcaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_caload;
}
final public void castore() {
	if (DEBUG) System.out.println(position + "\t\tcastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_castore;
}
public final void checkcast(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tcheckcast:"+typeBinding); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_checkcast;
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
final public void d2f() {
	if (DEBUG) System.out.println(position + "\t\td2f"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_d2f;
}
final public void d2i() {
	if (DEBUG) System.out.println(position + "\t\td2i"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_d2i;
}
final public void d2l() {
	if (DEBUG) System.out.println(position + "\t\td2l"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_d2l;
}
final public void dadd() {
	if (DEBUG) System.out.println(position + "\t\tdadd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dadd;
}
final public void daload() {
	if (DEBUG) System.out.println(position + "\t\tdaload"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_daload;
}
final public void dastore() {
	if (DEBUG) System.out.println(position + "\t\tdastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 4;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dastore;
}
final public void dcmpg() {
	if (DEBUG) System.out.println(position + "\t\tdcmpg"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dcmpg;
}
final public void dcmpl() {
	if (DEBUG) System.out.println(position + "\t\tdcmpl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dcmpl;
}
final public void dconst_0() {
	if (DEBUG) System.out.println(position + "\t\tdconst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dconst_0;
}
final public void dconst_1() {
	if (DEBUG) System.out.println(position + "\t\tdconst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dconst_1;
}
final public void ddiv() {
	if (DEBUG) System.out.println(position + "\t\tddiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ddiv;
}
public void decrStackSize(int offset) {
	stackDepth -= offset;
}
final public void dload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tdload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < iArg + 2) {
		maxLocals = iArg + 2; // + 2 because it is a double
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_dload;
		writeUnsignedShort(iArg);
	} else {
		// Don't need to use the wide bytecode
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_dload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void dload_0() {
	if (DEBUG) System.out.println(position + "\t\tdload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dload_0;
}
final public void dload_1() {
	if (DEBUG) System.out.println(position + "\t\tdload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dload_1;
}
final public void dload_2() {
	if (DEBUG) System.out.println(position + "\t\tdload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dload_2;
}
final public void dload_3() {
	if (DEBUG) System.out.println(position + "\t\tdload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dload_3;
}
final public void dmul() {
	if (DEBUG) System.out.println(position + "\t\tdmul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dmul;
}
final public void dneg() {
	if (DEBUG) System.out.println(position + "\t\tdneg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dneg;
}
final public void drem() {
	if (DEBUG) System.out.println(position + "\t\tdrem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_drem;
}
final public void dreturn() {
	if (DEBUG) System.out.println(position + "\t\tdreturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dreturn;
}
final public void dstore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tdstore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_dstore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_dstore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void dstore_0() {
	if (DEBUG) System.out.println(position + "\t\tdstore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dstore_0;
}
final public void dstore_1() {
	if (DEBUG) System.out.println(position + "\t\tdstore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dstore_1;
}
final public void dstore_2() {
	if (DEBUG) System.out.println(position + "\t\tdstore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dstore_2;
}
final public void dstore_3() {
	if (DEBUG) System.out.println(position + "\t\tdstore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dstore_3;
}
final public void dsub() {
	if (DEBUG) System.out.println(position + "\t\tdsub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dsub;
}
final public void dup() {
	if (DEBUG) System.out.println(position + "\t\tdup"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup;
}
final public void dup_x1() {
	if (DEBUG) System.out.println(position + "\t\tdup_x1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup_x1;
}
final public void dup_x2() {
	if (DEBUG) System.out.println(position + "\t\tdup_x2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup_x2;
}
final public void dup2() {
	if (DEBUG) System.out.println(position + "\t\tdup2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup2;
}
final public void dup2_x1() {
	if (DEBUG) System.out.println(position + "\t\tdup2_x1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup2_x1;
}
final public void dup2_x2() {
	if (DEBUG) System.out.println(position + "\t\tdup2_x2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup2_x2;
}
public void exitUserScope(BlockScope blockScope) {
	// mark all the scope's locals as loosing their definite assignment

	if (!generateLocalVariableTableAttributes)
		return;
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding visibleLocal = visibleLocals[i];
		if ((visibleLocal != null) && (visibleLocal.declaringScope == blockScope)) { 
			// there maybe some some preserved locals never initialized
			if (visibleLocal.initializationCount > 0){
				visibleLocals[i].recordInitializationEndPC(position);
			}
			visibleLocals[i] = null; // this variable is no longer visible afterwards
		}
	}
}
final public void f2d() {
	if (DEBUG) System.out.println(position + "\t\tf2d"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_f2d;
}
final public void f2i() {
	if (DEBUG) System.out.println(position + "\t\tf2i"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_f2i;
}
final public void f2l() {
	if (DEBUG) System.out.println(position + "\t\tf2l"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_f2l;
}
final public void fadd() {
	if (DEBUG) System.out.println(position + "\t\tfadd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fadd;
}
final public void faload() {
	if (DEBUG) System.out.println(position + "\t\tfaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_faload;
}
final public void fastore() {
	if (DEBUG) System.out.println(position + "\t\tfaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fastore;
}
final public void fcmpg() {
	if (DEBUG) System.out.println(position + "\t\tfcmpg"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fcmpg;
}
final public void fcmpl() {
	if (DEBUG) System.out.println(position + "\t\tfcmpl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fcmpl;
}
final public void fconst_0() {
	if (DEBUG) System.out.println(position + "\t\tfconst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fconst_0;
}
final public void fconst_1() {
	if (DEBUG) System.out.println(position + "\t\tfconst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fconst_1;
}
final public void fconst_2() {
	if (DEBUG) System.out.println(position + "\t\tfconst_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fconst_2;
}
final public void fdiv() {
	if (DEBUG) System.out.println(position + "\t\tfdiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fdiv;
}
final public void fload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tfload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_fload;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_fload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void fload_0() {
	if (DEBUG) System.out.println(position + "\t\tfload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fload_0;
}
final public void fload_1() {
	if (DEBUG) System.out.println(position + "\t\tfload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fload_1;
}
final public void fload_2() {
	if (DEBUG) System.out.println(position + "\t\tfload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fload_2;
}
final public void fload_3() {
	if (DEBUG) System.out.println(position + "\t\tfload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fload_3;
}
final public void fmul() {
	if (DEBUG) System.out.println(position + "\t\tfmul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fmul;
}
final public void fneg() {
	if (DEBUG) System.out.println(position + "\t\tfneg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fneg;
}
final public void frem() {
	if (DEBUG) System.out.println(position + "\t\tfrem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_frem;
}
final public void freturn() {
	if (DEBUG) System.out.println(position + "\t\tfreturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_freturn;
}
final public void fstore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tfstore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_fstore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_fstore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void fstore_0() {
	if (DEBUG) System.out.println(position + "\t\tfstore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fstore_0;
}
final public void fstore_1() {
	if (DEBUG) System.out.println(position + "\t\tfstore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fstore_1;
}
final public void fstore_2() {
	if (DEBUG) System.out.println(position + "\t\tfstore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fstore_2;
}
final public void fstore_3() {
	if (DEBUG) System.out.println(position + "\t\tfstore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fstore_3;
}
final public void fsub() {
	if (DEBUG) System.out.println(position + "\t\tfsub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_fsub;
}
/**
 * Macro for building a class descriptor object
 */
public void generateClassLiteralAccessForType(TypeBinding accessedType, FieldBinding syntheticFieldBinding) {
	Label endLabel;
	ExceptionLabel anyExceptionHandler;
	int saveStackSize;
	if (accessedType.isBaseType() && accessedType != NullBinding) {
		this.getTYPE(accessedType.id);
		return;
	}

	if (this.targetLevel >= ClassFileConstants.JDK1_5) {
		// generation using the new ldc_w bytecode
		this.ldc(accessedType);
	} else {
		endLabel = new Label(this);
		if (syntheticFieldBinding != null) { // non interface case
			this.getstatic(syntheticFieldBinding);
			this.dup();
			this.ifnonnull(endLabel);
			this.pop();
		}

		/* Macro for building a class descriptor object... using or not a field cache to store it into...
		this sequence is responsible for building the actual class descriptor.
		
		If the fieldCache is set, then it is supposed to be the body of a synthetic access method
		factoring the actual descriptor creation out of the invocation site (saving space).
		If the fieldCache is nil, then we are dumping the bytecode on the invocation site, since
		we have no way to get a hand on the field cache to do better. */
	
	
		// Wrap the code in an exception handler to convert a ClassNotFoundException into a NoClassDefError
	
		anyExceptionHandler = new ExceptionLabel(this, BaseTypes.NullBinding /* represents ClassNotFoundException*/);
		this.ldc(accessedType == BaseTypes.NullBinding ? "java.lang.Object" : String.valueOf(accessedType.constantPoolName()).replace('/', '.')); //$NON-NLS-1$
		this.invokeClassForName();
	
		/* See https://bugs.eclipse.org/bugs/show_bug.cgi?id=37565
		if (accessedType == BaseTypes.NullBinding) {
			this.ldc("java.lang.Object"); //$NON-NLS-1$
		} else if (accessedType.isArrayType()) {
			this.ldc(String.valueOf(accessedType.constantPoolName()).replace('/', '.'));
		} else {
			// we make it an array type (to avoid class initialization)
			this.ldc("[L" + String.valueOf(accessedType.constantPoolName()).replace('/', '.') + ";"); //$NON-NLS-1$//$NON-NLS-2$
		}
		this.invokeClassForName();
		if (!accessedType.isArrayType()) { // extract the component type, which doesn't initialize the class
			this.invokeJavaLangClassGetComponentType();
		}	
		*/
		/* We need to protect the runtime code from binary inconsistencies
		in case the accessedType is missing, the ClassNotFoundException has to be converted
		into a NoClassDefError(old ex message), we thus need to build an exception handler for this one. */
		anyExceptionHandler.placeEnd();
	
		if (syntheticFieldBinding != null) { // non interface case
			this.dup();
			this.putstatic(syntheticFieldBinding);
		}
		this.goto_(endLabel);
	
	
		// Generate the body of the exception handler
		saveStackSize = stackDepth;
		stackDepth = 1;
		/* ClassNotFoundException on stack -- the class literal could be doing more things
		on the stack, which means that the stack may not be empty at this point in the
		above code gen. So we save its state and restart it from 1. */
	
		anyExceptionHandler.place();
	
		// Transform the current exception, and repush and throw a 
		// NoClassDefFoundError(ClassNotFound.getMessage())
	
		this.newNoClassDefFoundError();
		this.dup_x1();
		this.swap();
	
		// Retrieve the message from the old exception
		this.invokeThrowableGetMessage();
	
		// Send the constructor taking a message string as an argument
		this.invokeNoClassDefFoundErrorStringConstructor();
		this.athrow();
		stackDepth = saveStackSize;
		endLabel.place();
	}
}
/**
 * This method generates the code attribute bytecode
 */
final public void generateCodeAttributeForProblemMethod(String problemMessage) {
	newJavaLangError();
	dup();
	ldc(problemMessage);
	invokeJavaLangErrorConstructor();
	athrow();
}
public void generateConstant(Constant constant, int implicitConversionCode) {
	int targetTypeID = implicitConversionCode >> 4;
	switch (targetTypeID) {
		case T_boolean :
			generateInlinedValue(constant.booleanValue());
			break;
		case T_char :
			generateInlinedValue(constant.charValue());
			break;
		case T_byte :
			generateInlinedValue(constant.byteValue());
			break;
		case T_short :
			generateInlinedValue(constant.shortValue());
			break;
		case T_int :
			generateInlinedValue(constant.intValue());
			break;
		case T_long :
			generateInlinedValue(constant.longValue());
			break;
		case T_float :
			generateInlinedValue(constant.floatValue());
			break;
		case T_double :
			generateInlinedValue(constant.doubleValue());
			break;
		default : //String or Object
			ldc(constant.stringValue());
	}
}
/**
 * Generates the sequence of instructions which will perform the conversion of the expression
 * on the stack into a different type (e.g. long l = someInt; --> i2l must be inserted).
 * @param implicitConversionCode int
 */
public void generateImplicitConversion(int implicitConversionCode) {
	switch (implicitConversionCode) {
		case Float2Char :
			this.f2i();
			this.i2c();
			break;
		case Double2Char :
			this.d2i();
			this.i2c();
			break;
		case Int2Char :
		case Short2Char :
		case Byte2Char :
			this.i2c();
			break;
		case Long2Char :
			this.l2i();
			this.i2c();
			break;
		case Char2Float :
		case Short2Float :
		case Int2Float :
		case Byte2Float :
			this.i2f();
			break;
		case Double2Float :
			this.d2f();
			break;
		case Long2Float :
			this.l2f();
			break;
		case Float2Byte :
			this.f2i();
			this.i2b();
			break;
		case Double2Byte :
			this.d2i();
			this.i2b();
			break;
		case Int2Byte :
		case Short2Byte :
		case Char2Byte :
			this.i2b();
			break;
		case Long2Byte :
			this.l2i();
			this.i2b();
			break;
		case Byte2Double :
		case Char2Double :
		case Short2Double :
		case Int2Double :
			this.i2d();
			break;
		case Float2Double :
			this.f2d();
			break;
		case Long2Double :
			this.l2d();
			break;
		case Byte2Short :
		case Char2Short :
		case Int2Short :
			this.i2s();
			break;
		case Double2Short :
			this.d2i();
			this.i2s();
			break;
		case Long2Short :
			this.l2i();
			this.i2s();
			break;
		case Float2Short :
			this.f2i();
			this.i2s();
			break;
		case Double2Int :
			this.d2i();
			break;
		case Float2Int :
			this.f2i();
			break;
		case Long2Int :
			this.l2i();
			break;
		case Int2Long :
		case Char2Long :
		case Byte2Long :
		case Short2Long :
			this.i2l();
			break;
		case Double2Long :
			this.d2l();
			break;
		case Float2Long :
			this.f2l();
	}
}
public void generateInlinedValue(byte inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush(inlinedValue);
				return;
			}
	}
}
public void generateInlinedValue(char inlinedValue) {
	switch (inlinedValue) {
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((6 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			if ((128 <= inlinedValue) && (inlinedValue <= 32767)) {
				this.sipush(inlinedValue);
				return;
			}
			this.ldc(inlinedValue);
	}
}
public void generateInlinedValue(double inlinedValue) {
	if (inlinedValue == 0.0) {
		if (Double.doubleToLongBits(inlinedValue) != 0L)
			this.ldc2_w(inlinedValue);
		else
			this.dconst_0();
		return;
	}
	if (inlinedValue == 1.0) {
		this.dconst_1();
		return;
	}
	this.ldc2_w(inlinedValue);
}
public void generateInlinedValue(float inlinedValue) {
	if (inlinedValue == 0.0f) {
		if (Float.floatToIntBits(inlinedValue) != 0)
			this.ldc(inlinedValue);
		else
			this.fconst_0();
		return;
	}
	if (inlinedValue == 1.0f) {
		this.fconst_1();
		return;
	}
	if (inlinedValue == 2.0f) {
		this.fconst_2();
		return;
	}
	this.ldc(inlinedValue);
}
public void generateInlinedValue(int inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			if ((-32768 <= inlinedValue) && (inlinedValue <= 32767)) {
				this.sipush(inlinedValue);
				return;
			}
			this.ldc(inlinedValue);
	}
}
public void generateInlinedValue(long inlinedValue) {
	if (inlinedValue == 0) {
		this.lconst_0();
		return;
	}
	if (inlinedValue == 1) {
		this.lconst_1();
		return;
	}
	this.ldc2_w(inlinedValue);
}
public void generateInlinedValue(short inlinedValue) {
	switch (inlinedValue) {
		case -1 :
			this.iconst_m1();
			break;
		case 0 :
			this.iconst_0();
			break;
		case 1 :
			this.iconst_1();
			break;
		case 2 :
			this.iconst_2();
			break;
		case 3 :
			this.iconst_3();
			break;
		case 4 :
			this.iconst_4();
			break;
		case 5 :
			this.iconst_5();
			break;
		default :
			if ((-128 <= inlinedValue) && (inlinedValue <= 127)) {
				this.bipush((byte) inlinedValue);
				return;
			}
			this.sipush(inlinedValue);
	}
}
public void generateInlinedValue(boolean inlinedValue) {
	if (inlinedValue)
		this.iconst_1();
	else
		this.iconst_0();
}
public void generateOuterAccess(Object[] mappingSequence, ASTNode invocationSite, Binding target, Scope scope) {
	if (mappingSequence == null) {
		if (target instanceof LocalVariableBinding) {
			scope.problemReporter().needImplementation(); //TODO (philippe) should improve local emulation failure reporting
		} else {
			scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, false);
		}
		return;
	}
	if (mappingSequence == BlockScope.NoEnclosingInstanceInConstructorCall) {
		scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, true);
		return;
	} else if (mappingSequence == BlockScope.NoEnclosingInstanceInStaticContext) {
		scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, false);
		return;
	}
	
	if (mappingSequence == BlockScope.EmulationPathToImplicitThis) {
		this.aload_0();
		return;
	} else if (mappingSequence[0] instanceof FieldBinding) {
		FieldBinding fieldBinding = (FieldBinding) mappingSequence[0];
		this.aload_0();
		this.getfield(fieldBinding);
	} else {
		load((LocalVariableBinding) mappingSequence[0]);
	}
	for (int i = 1, length = mappingSequence.length; i < length; i++) {
		if (mappingSequence[i] instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding) mappingSequence[i];
			this.getfield(fieldBinding);
		} else {
			this.invokestatic((MethodBinding) mappingSequence[i]);
		}
	}
}

/**
 * The equivalent code performs a string conversion:
 *
 * @param blockScope the given blockScope
 * @param oper1 the first expression
 * @param oper2 the second expression
 */
public void generateStringConcatenationAppend(BlockScope blockScope, Expression oper1, Expression oper2) {
	int pc;
	if (oper1 == null) {
		/* Operand is already on the stack, and maybe nil:
		note type1 is always to  java.lang.String here.*/
		this.newStringContatenation();
		this.dup_x1();
		this.swap();
		// If argument is reference type, need to transform it 
		// into a string (handles null case)
		this.invokeStringValueOf(T_Object);
		this.invokeStringConcatenationStringConstructor();
	} else {
		pc = position;
		oper1.generateOptimizedStringConcatenationCreation(blockScope, this, oper1.implicitConversion & 0xF);
		this.recordPositionsFrom(pc, oper1.sourceStart);
	}
	pc = position;
	oper2.generateOptimizedStringConcatenation(blockScope, this, oper2.implicitConversion & 0xF);
	this.recordPositionsFrom(pc, oper2.sourceStart);
	this.invokeStringConcatenationToString();
}
/**
 * Code responsible to generate the suitable code to supply values for the synthetic enclosing
 * instance arguments of a constructor invocation of a nested type.
 */
public void generateSyntheticEnclosingInstanceValues(
		BlockScope currentScope, 
		ReferenceBinding targetType, 
		Expression enclosingInstance, 
		ASTNode invocationSite) {

	// supplying enclosing instance for the anonymous type's superclass
	ReferenceBinding checkedTargetType = targetType.isAnonymousType() ? targetType.superclass() : targetType;
	boolean hasExtraEnclosingInstance = enclosingInstance != null;
	if (hasExtraEnclosingInstance 
			&& (!checkedTargetType.isNestedType() || checkedTargetType.isStatic())) {
		currentScope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, checkedTargetType);
		return;
	}

	// perform some emulation work in case there is some and we are inside a local type only
	ReferenceBinding[] syntheticArgumentTypes;
	if ((syntheticArgumentTypes = targetType.syntheticEnclosingInstanceTypes()) != null) {

		ReferenceBinding targetEnclosingType = checkedTargetType.enclosingType();
		boolean complyTo14 = currentScope.environment().options.complianceLevel >= ClassFileConstants.JDK1_4;
		// deny access to enclosing instance argument for allocation and super constructor call (if 1.4)
		boolean ignoreEnclosingArgInConstructorCall = invocationSite instanceof AllocationExpression
					|| (complyTo14 && ((invocationSite instanceof ExplicitConstructorCall && ((ExplicitConstructorCall)invocationSite).isSuperAccess())));
						
		for (int i = 0, max = syntheticArgumentTypes.length; i < max; i++) {
			ReferenceBinding syntheticArgType = syntheticArgumentTypes[i];
			if (hasExtraEnclosingInstance && syntheticArgType == targetEnclosingType) {
				hasExtraEnclosingInstance = false;
				enclosingInstance.generateCode(currentScope, this, true);
				if (complyTo14){
					dup();
					invokeObjectGetClass(); // will perform null check
					pop();
				}
			} else {
				Object[] emulationPath = currentScope.getEmulationPath(
						syntheticArgType, 
						false /*not only exact match (that is, allow compatible)*/,
						ignoreEnclosingArgInConstructorCall);
				this.generateOuterAccess(emulationPath, invocationSite, syntheticArgType, currentScope);
			}
		}
		if (hasExtraEnclosingInstance){
			currentScope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, checkedTargetType);
		}
	}
}

/**
 * Code responsible to generate the suitable code to supply values for the synthetic outer local
 * variable arguments of a constructor invocation of a nested type.
 * (bug 26122) - synthetic values for outer locals must be passed after user arguments, e.g. new X(i = 1){}
 */
public void generateSyntheticOuterArgumentValues(BlockScope currentScope, ReferenceBinding targetType, ASTNode invocationSite) {

	// generate the synthetic outer arguments then
	SyntheticArgumentBinding syntheticArguments[];
	if ((syntheticArguments = targetType.syntheticOuterLocalVariables()) != null) {
		for (int i = 0, max = syntheticArguments.length; i < max; i++) {
			LocalVariableBinding targetVariable = syntheticArguments[i].actualOuterLocalVariable;
			VariableBinding[] emulationPath = currentScope.getEmulationPath(targetVariable);
			this.generateOuterAccess(emulationPath, invocationSite, targetVariable, currentScope);
		}
	}
}

/**
 * @param accessBinding the access method binding to generate
 */
public void generateSyntheticBodyForConstructorAccess(SyntheticAccessMethodBinding accessBinding) {

	initializeMaxLocals(accessBinding);

	MethodBinding constructorBinding = accessBinding.targetMethod;
	TypeBinding[] parameters = constructorBinding.parameters;
	int length = parameters.length;
	int resolvedPosition = 1;
	this.aload_0();
	if (constructorBinding.declaringClass.isNestedType()) {
		NestedTypeBinding nestedType = (NestedTypeBinding) constructorBinding.declaringClass;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticEnclosingInstances();
		for (int i = 0; i < (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
			TypeBinding type;
			load((type = syntheticArguments[i].type), resolvedPosition);
			if ((type == DoubleBinding) || (type == LongBinding))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
	}
	for (int i = 0; i < length; i++) {
		load(parameters[i], resolvedPosition);
		if ((parameters[i] == DoubleBinding) || (parameters[i] == LongBinding))
			resolvedPosition += 2;
		else
			resolvedPosition++;
	}
	
	if (constructorBinding.declaringClass.isNestedType()) {
		NestedTypeBinding nestedType = (NestedTypeBinding) constructorBinding.declaringClass;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
		for (int i = 0; i < (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
			TypeBinding type;
			load((type = syntheticArguments[i].type), resolvedPosition);
			if ((type == DoubleBinding) || (type == LongBinding))
				resolvedPosition += 2;
			else
				resolvedPosition++;
		}
	}
	this.invokespecial(constructorBinding);
	this.return_();
}
public void generateSyntheticBodyForFieldReadAccess(SyntheticAccessMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	FieldBinding fieldBinding = accessBinding.targetReadField;
	TypeBinding type;
	if (fieldBinding.isStatic())
		this.getstatic(fieldBinding);
	else {
		this.aload_0();
		this.getfield(fieldBinding);
	}
	if ((type = fieldBinding.type).isBaseType()) {
		if (type == IntBinding)
			this.ireturn();
		else
			if (type == FloatBinding)
				this.freturn();
			else
				if (type == LongBinding)
					this.lreturn();
				else
					if (type == DoubleBinding)
						this.dreturn();
					else
						this.ireturn();
	} else
		this.areturn();
}
public void generateSyntheticBodyForFieldWriteAccess(SyntheticAccessMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	FieldBinding fieldBinding = accessBinding.targetWriteField;
	if (fieldBinding.isStatic()) {
		load(fieldBinding.type, 0);
		this.putstatic(fieldBinding);
	} else {
		this.aload_0();
		load(fieldBinding.type, 1);
		this.putfield(fieldBinding);
	}
	this.return_();
}
public void generateSyntheticBodyForMethodAccess(SyntheticAccessMethodBinding accessBinding) {

	initializeMaxLocals(accessBinding);
	MethodBinding methodBinding = accessBinding.targetMethod;
	TypeBinding[] parameters = methodBinding.parameters;
	int length = parameters.length;
	TypeBinding[] arguments = accessBinding.accessType == SyntheticAccessMethodBinding.BridgeMethodAccess 
													? accessBinding.parameters
													: null;
	int resolvedPosition;
	if (methodBinding.isStatic())
		resolvedPosition = 0;
	else {
		this.aload_0();
		resolvedPosition = 1;
	}
	for (int i = 0; i < length; i++) {
	    TypeBinding parameter = parameters[i];
	    if (arguments != null) { // for bridge methods
		    TypeBinding argument = arguments[i];
			load(argument, resolvedPosition);
			if (argument != parameter) 
			    checkcast(parameter);
	    } else {
			load(parameter, resolvedPosition);
		}
		if ((parameter == DoubleBinding) || (parameter == LongBinding))
			resolvedPosition += 2;
		else
			resolvedPosition++;
	}
	TypeBinding type;
	if (methodBinding.isStatic())
		this.invokestatic(methodBinding);
	else {
		if (methodBinding.isConstructor()
			|| methodBinding.isPrivate()
			// qualified super "X.super.foo()" targets methods from superclass
			|| accessBinding.accessType == SyntheticAccessMethodBinding.SuperMethodAccess){
			this.invokespecial(methodBinding);
		} else {
			if (methodBinding.declaringClass.isInterface()){
				this.invokeinterface(methodBinding);
			} else {
				this.invokevirtual(methodBinding);
			}
		}
	}
	if ((type = methodBinding.returnType).isBaseType())
		if (type == VoidBinding)
			this.return_();
		else
			if (type == IntBinding)
				this.ireturn();
			else
				if (type == FloatBinding)
					this.freturn();
				else
					if (type == LongBinding)
						this.lreturn();
					else
						if (type == DoubleBinding)
							this.dreturn();
						else
							this.ireturn();
	else
		this.areturn();
}
final public byte[] getContents() {
	byte[] contents;
	System.arraycopy(bCodeStream, 0, contents = new byte[position], 0, position);
	return contents;
}
final public void getfield(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tgetfield:"+fieldBinding); //$NON-NLS-1$
	countLabels = 0;
	if ((fieldBinding.type.id == T_double) || (fieldBinding.type.id == T_long)) {
		if (++stackDepth > stackMax)
			stackMax = stackDepth;
	}
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_getfield;
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
final public void getstatic(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tgetstatic:"+fieldBinding); //$NON-NLS-1$
	countLabels = 0;
	if ((fieldBinding.type.id == T_double) || (fieldBinding.type.id == T_long))
		stackDepth += 2;
	else
		stackDepth += 1;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_getstatic;
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
public void getTYPE(int baseTypeID) {
	countLabels = 0;
	if (++stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_getstatic;
	switch (baseTypeID) {
		case T_byte :
			// getstatic: java.lang.Byte.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Byte.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangByteTYPE());
			break;
		case T_short :
			// getstatic: java.lang.Short.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Short.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangShortTYPE());
			break;
		case T_char :
			// getstatic: java.lang.Character.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Character.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangCharacterTYPE());
			break;
		case T_int :
			// getstatic: java.lang.Integer.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Integer.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangIntegerTYPE());
			break;
		case T_long :
			// getstatic: java.lang.Long.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Long.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangLongTYPE());
			break;
		case T_float :
			// getstatic: java.lang.Float.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Float.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangFloatTYPE());
			break;
		case T_double :
			// getstatic: java.lang.Double.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Double.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangDoubleTYPE());
			break;
		case T_boolean :
			// getstatic: java.lang.Boolean.TYPE			
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Boolean.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangBooleanTYPE());
			break;
		case T_void :
			// getstatic: java.lang.Void.TYPE
			if (DEBUG) System.out.println(position + "\t\tgetstatic: java.lang.Void.TYPE"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangVoidTYPE());
			break;
	}
}
/**
 * We didn't call it goto, because there is a conflit with the goto keyword
 */
final public void goto_(Label label) {
	if (this.wideMode) {
		this.goto_w(label);
		return;
	}
	if (DEBUG) System.out.println(position + "\t\tgoto:"+label); //$NON-NLS-1$
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	label.inlineForwardReferencesFromLabelsTargeting(position);
	/*
	 Possible optimization for code such as:
	 public Object foo() {
		boolean b = true;
		if (b) {
			if (b)
				return null;
		} else {
			if (b) {
				return null;
			}
		}
		return null;
	}
	The goto around the else block for the first if will
	be unreachable, because the thenClause of the second if
	returns.
	See inlineForwardReferencesFromLabelsTargeting defined
	on the Label class for the remaining part of this
	optimization.
	 if (!lbl.isBranchTarget(position)) {
		switch(bCodeStream[classFileOffset-1]) {
			case OPC_return :
			case OPC_areturn:
				return;
		}
	}*/
	position++;
	bCodeStream[classFileOffset++] = OPC_goto;
	label.branch();
}

final public void goto_w(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tgotow:"+lbl); //$NON-NLS-1$
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_goto_w;
	lbl.branchWide();
}
final public void i2b() {
	if (DEBUG) System.out.println(position + "\t\ti2b"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_i2b;
}
final public void i2c() {
	if (DEBUG) System.out.println(position + "\t\ti2c"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_i2c;
}
final public void i2d() {
	if (DEBUG) System.out.println(position + "\t\ti2d"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_i2d;
}
final public void i2f() {
	if (DEBUG) System.out.println(position + "\t\ti2f"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_i2f;
}
final public void i2l() {
	if (DEBUG) System.out.println(position + "\t\ti2l"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_i2l;
}
final public void i2s() {
	if (DEBUG) System.out.println(position + "\t\ti2s"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_i2s;
}
final public void iadd() {
	if (DEBUG) System.out.println(position + "\t\tiadd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iadd;
}
final public void iaload() {
	if (DEBUG) System.out.println(position + "\t\tiaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iaload;
}
final public void iand() {
	if (DEBUG) System.out.println(position + "\t\tiand"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iand;
}
final public void iastore() {
	if (DEBUG) System.out.println(position + "\t\tiastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iastore;
}
final public void iconst_0() {
	if (DEBUG) System.out.println(position + "\t\ticonst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_0;
}
final public void iconst_1() {
	if (DEBUG) System.out.println(position + "\t\ticonst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_1;
}
final public void iconst_2() {
	if (DEBUG) System.out.println(position + "\t\ticonst_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_2;
}
final public void iconst_3() {
	if (DEBUG) System.out.println(position + "\t\ticonst_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_3;
}
final public void iconst_4() {
	if (DEBUG) System.out.println(position + "\t\ticonst_4"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_4;
}
final public void iconst_5() {
	if (DEBUG) System.out.println(position + "\t\ticonst_5"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_5;
}
final public void iconst_m1() {
	if (DEBUG) System.out.println(position + "\t\ticonst_m1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iconst_m1;
}
final public void idiv() {
	if (DEBUG) System.out.println(position + "\t\tidiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_idiv;
}
final public void if_acmpeq(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_acmpeq:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth-=2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_acmpne, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_acmpeq;
		lbl.branch();
	}
}
final public void if_acmpne(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_acmpne:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth-=2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_acmpeq, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_acmpne;
		lbl.branch();
	}
}
final public void if_icmpeq(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_cmpeq:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_icmpne, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_icmpeq;
		lbl.branch();
	}
}
final public void if_icmpge(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_iacmpge:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_icmplt, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_icmpge;
		lbl.branch();
	}
}
final public void if_icmpgt(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_iacmpgt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_icmple, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_icmpgt;
		lbl.branch();
	}
}
final public void if_icmple(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_iacmple:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_icmpgt, lbl);
	} else {	
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_icmple;
		lbl.branch();
	}
}
final public void if_icmplt(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_iacmplt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_icmpge, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_icmplt;
		lbl.branch();
	}
}
final public void if_icmpne(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tif_iacmpne:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_if_icmpeq, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_if_icmpne;
		lbl.branch();
	}
}
final public void ifeq(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifeq:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifne, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifeq;
		lbl.branch();
	}
}
final public void ifge(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifge:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_iflt, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifge;
		lbl.branch();
	}
}
final public void ifgt(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifgt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifle, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifgt;
		lbl.branch();
	}
}
final public void ifle(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifle:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifgt, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifle;
		lbl.branch();
	}
}
final public void iflt(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tiflt:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifge, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_iflt;
		lbl.branch();
	}
}
final public void ifne(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifne:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifeq, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifne;
		lbl.branch();
	}
}
final public void ifnonnull(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifnonnull:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifnull, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifnonnull;
		lbl.branch();
	}
}
final public void ifnull(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tifnull:"+lbl); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (this.wideMode) {
		generateWideRevertedConditionalBranch(OPC_ifnonnull, lbl);
	} else {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ifnull;
		lbl.branch();
	}
}
final public void iinc(int index, int value) {
	if (DEBUG) System.out.println(position + "\t\tiinc:"+index+","+value); //$NON-NLS-1$ //$NON-NLS-2$
	countLabels = 0;
	if ((index > 255) || (value < -128 || value > 127)) { // have to widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_iinc;
		writeUnsignedShort(index);
		writeSignedShort(value);
	} else {
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 3;
		bCodeStream[classFileOffset++] = OPC_iinc;
		bCodeStream[classFileOffset++] = (byte) index;
		bCodeStream[classFileOffset++] = (byte) value;
	}
}
final public void iload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tiload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_iload;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_iload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void iload_0() {
	if (DEBUG) System.out.println(position + "\t\tiload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 0) {
		maxLocals = 1;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iload_0;
}
final public void iload_1() {
	if (DEBUG) System.out.println(position + "\t\tiload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iload_1;
}
final public void iload_2() {
	if (DEBUG) System.out.println(position + "\t\tiload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iload_2;
}
final public void iload_3() {
	if (DEBUG) System.out.println(position + "\t\tiload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iload_3;
}
final public void imul() {
	if (DEBUG) System.out.println(position + "\t\timul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_imul;
}
public void incrementTemp(LocalVariableBinding localBinding, int value) {
	if (value == (short) value) {
		this.iinc(localBinding.resolvedPosition, value);
		return;
	}
	load(localBinding);
	this.ldc(value);
	this.iadd();
	store(localBinding, false);
}
public void incrStackSize(int offset) {
	if ((stackDepth += offset) > stackMax)
		stackMax = stackDepth;
}
public int indexOfSameLineEntrySincePC(int pc, int line) {
	for (int index = pc, max = pcToSourceMapSize; index < max; index+=2) {
		if (pcToSourceMap[index+1] == line)
			return index;
	}
	return -1;
}
final public void ineg() {
	if (DEBUG) System.out.println(position + "\t\tineg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ineg;
}
public void init(ClassFile targetClassFile) {
	this.classFile = targetClassFile;
	this.constantPool = targetClassFile.constantPool;
	this.bCodeStream = targetClassFile.contents;
	this.classFileOffset = targetClassFile.contentsOffset;
	this.startingClassFileOffset = this.classFileOffset;
	pcToSourceMapSize = 0;
	lastEntryPC = 0;
	int length = visibleLocals.length;
	if (noVisibleLocals.length < length) {
		noVisibleLocals = new LocalVariableBinding[length];
	}
	System.arraycopy(noVisibleLocals, 0, visibleLocals, 0, length);
	visibleLocalsCount = 0;
	
	length = locals.length;
	if (noLocals.length < length) {
		noLocals = new LocalVariableBinding[length];
	}
	System.arraycopy(noLocals, 0, locals, 0, length);
	allLocalsCounter = 0;

	length = exceptionHandlers.length;
	if (noExceptionHandlers.length < length) {
		noExceptionHandlers = new ExceptionLabel[length];
	}
	System.arraycopy(noExceptionHandlers, 0, exceptionHandlers, 0, length);
	exceptionHandlersIndex = 0;
	exceptionHandlersCounter = 0;
	
	length = labels.length;
	if (noLabels.length < length) {
		noLabels = new Label[length];
	}
	System.arraycopy(noLabels, 0, labels, 0, length);
	countLabels = 0;

	stackMax = 0;
	stackDepth = 0;
	maxLocals = 0;
	position = 0;
}
/**
 * @param methodBinding the given method binding to initialize the max locals
 */
public void initializeMaxLocals(MethodBinding methodBinding) {

	maxLocals = (methodBinding == null || methodBinding.isStatic()) ? 0 : 1;
	// take into account the synthetic parameters
	if (methodBinding != null) {
		if (methodBinding.isConstructor() && methodBinding.declaringClass.isNestedType()) {
			ReferenceBinding enclosingInstanceTypes[];
			if ((enclosingInstanceTypes = methodBinding.declaringClass.syntheticEnclosingInstanceTypes()) != null) {
				for (int i = 0, max = enclosingInstanceTypes.length; i < max; i++) {
					maxLocals++; // an enclosingInstanceType can only be a reference binding. It cannot be
					// LongBinding or DoubleBinding
				}
			}
			SyntheticArgumentBinding syntheticArguments[];
			if ((syntheticArguments = methodBinding.declaringClass.syntheticOuterLocalVariables()) != null) {
				for (int i = 0, max = syntheticArguments.length; i < max; i++) {
					TypeBinding argType;
					if (((argType = syntheticArguments[i].type) == LongBinding) || (argType == DoubleBinding)) {
						maxLocals += 2;
					} else {
						maxLocals++;
					}
				}
			}
		}
		TypeBinding[] arguments;
		if ((arguments = methodBinding.parameters) != null) {
			for (int i = 0, max = arguments.length; i < max; i++) {
				TypeBinding argType;
				if (((argType = arguments[i]) == LongBinding) || (argType == DoubleBinding)) {
					maxLocals += 2;
				} else {
					maxLocals++;
				}
			}
		}
	}
}
/**
 * This methods searches for an existing entry inside the pcToSourceMap table with a pc equals to @pc.
 * If there is an existing entry it returns -1 (no insertion required).
 * Otherwise it returns the index where the entry for the pc has to be inserted.
 * This is based on the fact that the pcToSourceMap table is sorted according to the pc.
 *
 * @param pcToSourceMap the given pcToSourceMap array
 * @param length the given length
 * @param pc the given pc
 * @return int
 */
public static int insertionIndex(int[] pcToSourceMap, int length, int pc) {
	int g = 0;
	int d = length - 2;
	int m = 0;
	while (g <= d) {
		m = (g + d) / 2;
		// we search only on even indexes
		if ((m % 2) != 0)
			m--;
		int currentPC = pcToSourceMap[m];
		if (pc < currentPC) {
			d = m - 2;
		} else
			if (pc > currentPC) {
				g = m + 2;
			} else {
				return -1;
			}
	}
	if (pc < pcToSourceMap[m])
		return m;
	return m + 2;
}
/**
 * We didn't call it instanceof because there is a conflit with the
 * instanceof keyword
 */
final public void instance_of(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tinstance_of:"+typeBinding); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_instanceof;
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
public void invokeClassForName() {
	// invokestatic: java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class;
	if (DEBUG) System.out.println(position + "\t\tinvokestatic: java.lang.Class.forName(Ljava.lang.String;)Ljava.lang.Class;"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokestatic;
	writeUnsignedShort(constantPool.literalIndexForJavaLangClassForName());
}
public void invokeJavaLangClassDesiredAssertionStatus() {
	// invokevirtual: java.lang.Class.desiredAssertionStatus()Z;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Class.desiredAssertionStatus()Z;"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(constantPool.literalIndexForJavaLangClassDesiredAssertionStatus());
}

public void invokeJavaLangClassGetComponentType() {
	// invokevirtual: java.lang.Class.getComponentType()java.lang.Class;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Class.getComponentType()java.lang.Class;"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(constantPool.literalIndexForJavaLangClassGetComponentType());
}

final public void invokeinterface(MethodBinding methodBinding) {
	// initialized to 1 to take into account this  immediately
	if (DEBUG) System.out.println(position + "\t\tinvokeinterface: " + methodBinding); //$NON-NLS-1$
	countLabels = 0;
	int argCount = 1;
	int id;
	if (classFileOffset + 4 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 3;
	bCodeStream[classFileOffset++] = OPC_invokeinterface;
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount += 1;
	bCodeStream[classFileOffset++] = (byte) argCount;
	// Generate a  0 into the byte array. Like the array is already fill with 0, we just need to increment
	// the number of bytes.
	bCodeStream[classFileOffset++] = 0;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long)) {
		stackDepth += (2 - argCount);
	} else {
		if (id == T_void) {
			stackDepth -= argCount;
		} else {
			stackDepth += (1 - argCount);
		}
	}
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
}
public void invokeJavaLangErrorConstructor() {
	// invokespecial: java.lang.Error<init>(Ljava.lang.String;)V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.Error<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	stackDepth -= 2;
	writeUnsignedShort(constantPool.literalIndexForJavaLangErrorConstructor());
}
public void invokeNoClassDefFoundErrorStringConstructor() {
	// invokespecial: java.lang.NoClassDefFoundError.<init>(Ljava.lang.String;)V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.NoClassDefFoundError.<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	stackDepth -= 2;
	writeUnsignedShort(constantPool.literalIndexForJavaLangNoClassDefFoundErrorStringConstructor());
}
public void invokeObjectGetClass() {
	// invokevirtual: java.lang.Object.getClass()Ljava.lang.Class;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Object.getClass()Ljava.lang.Class;"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(constantPool.literalIndexForJavaLangObjectGetClass());
}

final public void invokespecial(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokespecial:"+methodBinding); //$NON-NLS-1$
	// initialized to 1 to take into account this  immediately
	countLabels = 0;
	int argCount = 1;
	int id;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	if (methodBinding.isConstructor() && methodBinding.declaringClass.isNestedType()) {
		// enclosing instances
		TypeBinding[] syntheticArgumentTypes = methodBinding.declaringClass.syntheticEnclosingInstanceTypes();
		if (syntheticArgumentTypes != null) {
			for (int i = 0, max = syntheticArgumentTypes.length; i < max; i++) {
				if (((id = syntheticArgumentTypes[i].id) == T_double) || (id == T_long)) {
					argCount += 2;
				} else {
					argCount++;
				}
			}
		}
		// outer local variables
		SyntheticArgumentBinding[] syntheticArguments = methodBinding.declaringClass.syntheticOuterLocalVariables();
		if (syntheticArguments != null) {
			for (int i = 0, max = syntheticArguments.length; i < max; i++) {
				if (((id = syntheticArguments[i].type.id) == T_double) || (id == T_long)) {
					argCount += 2;
				} else {
					argCount++;
				}
			}
		}
	}
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount++;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
final public void invokestatic(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokestatic:"+methodBinding); //$NON-NLS-1$
	// initialized to 0 to take into account that there is no this for
	// a static method
	countLabels = 0;
	int argCount = 0;
	int id;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokestatic;
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount += 1;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
/**
 * The equivalent code performs a string conversion of the TOS
 * @param typeID <CODE>int</CODE>
 */
public void invokeStringConcatenationAppendForType(int typeID) {
	if (DEBUG) {
		if (this.targetLevel >= JDK1_5) {
			System.out.println(position + "\t\tinvokevirtual: java.lang.StringBuilder.append(...)"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tinvokevirtual: java.lang.StringBuffer.append(...)"); //$NON-NLS-1$
		}
	}
	countLabels = 0;
	int usedTypeID;
	if (typeID == T_null) {
		usedTypeID = T_String;
	} else {
		usedTypeID = typeID;
	}
	// invokevirtual
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	if (this.targetLevel >= JDK1_5) {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilderAppend(typeID));
	} else {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferAppend(typeID));
	}
	if ((usedTypeID == T_long) || (usedTypeID == T_double)) {
		stackDepth -= 2;
	} else {
		stackDepth--;
	}
}

public void invokeJavaLangAssertionErrorConstructor(int typeBindingID) {
	// invokespecial: java.lang.AssertionError.<init>(typeBindingID)V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.AssertionError.<init>(typeBindingID)V"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	writeUnsignedShort(constantPool.literalIndexForJavaLangAssertionErrorConstructor(typeBindingID));
	stackDepth -= 2;
}

public void invokeJavaLangAssertionErrorDefaultConstructor() {
	// invokespecial: java.lang.AssertionError.<init>()V
	if (DEBUG) System.out.println(position + "\t\tinvokespecial: java.lang.AssertionError.<init>()V"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	writeUnsignedShort(constantPool.literalIndexForJavaLangAssertionErrorDefaultConstructor());
	stackDepth --;
}
public void invokeJavaUtilIteratorHasNext() {
	// invokeinterface java.util.Iterator.hasNext()Z
	if (DEBUG) System.out.println(position + "\t\tinvokeinterface: java.util.Iterator.hasNext()Z"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 4 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 3;
	bCodeStream[classFileOffset++] = OPC_invokeinterface;
	writeUnsignedShort(constantPool.literalIndexForJavaUtilIteratorHasNext());
	bCodeStream[classFileOffset++] = 1;
	// Generate a  0 into the byte array. Like the array is already fill with 0, we just need to increment
	// the number of bytes.
	bCodeStream[classFileOffset++] = 0;
}
public void invokeJavaUtilIteratorNext() {
	// invokeinterface java.util.Iterator.next()java.lang.Object
	if (DEBUG) System.out.println(position + "\t\tinvokeinterface: java.util.Iterator.next()java.lang.Object"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 4 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 3;
	bCodeStream[classFileOffset++] = OPC_invokeinterface;
	writeUnsignedShort(constantPool.literalIndexForJavaUtilIteratorNext());
	bCodeStream[classFileOffset++] = 1;
	// Generate a  0 into the byte array. Like the array is already fill with 0, we just need to increment
	// the number of bytes.
	bCodeStream[classFileOffset++] = 0;
}
public void invokeStringConcatenationDefaultConstructor() {
	// invokespecial: java.lang.StringBuffer.<init>()V
	if (DEBUG) {
		if (this.targetLevel >= JDK1_5) {
			System.out.println(position + "\t\tinvokespecial: java.lang.StringBuilder.<init>()V"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tinvokespecial: java.lang.StringBuffer.<init>()V"); //$NON-NLS-1$
		}
	}
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	if (this.targetLevel >= JDK1_5) {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilderDefaultConstructor());
	} else {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferDefaultConstructor());
	}
	stackDepth--;
}
public void invokeStringConcatenationStringConstructor() {
	if (DEBUG) {
		if (this.targetLevel >= JDK1_5) {
			System.out.println(position + "\t\tjava.lang.StringBuilder.<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tjava.lang.StringBuffer.<init>(Ljava.lang.String;)V"); //$NON-NLS-1$
		}
	}
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	if (this.targetLevel >= JDK1_5) {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilderConstructor());
	} else {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferConstructor());
	}
	stackDepth -= 2;
}

public void invokeStringConcatenationToString() {
	if (DEBUG) {
		if (this.targetLevel >= JDK1_5) {
			System.out.println(position + "\t\tinvokevirtual: StringBuilder.toString()Ljava.lang.String;"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tinvokevirtual: StringBuffer.toString()Ljava.lang.String;"); //$NON-NLS-1$
		}
	}
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	if (this.targetLevel >= JDK1_5) {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilderToString());
	} else {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferToString());
	}
}
public void invokeStringIntern() {
	// invokevirtual: java.lang.String.intern()
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.String.intern()"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringIntern());
}
public void invokeStringValueOf(int typeID) {
	// invokestatic: java.lang.String.valueOf(argumentType)
	if (DEBUG) System.out.println(position + "\t\tinvokestatic: java.lang.String.valueOf(...)"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokestatic;
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringValueOf(typeID));
}
public void invokeThrowableGetMessage() {
	// invokevirtual: java.lang.Throwable.getMessage()Ljava.lang.String;
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual: java.lang.Throwable.getMessage()Ljava.lang.String;"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(constantPool.literalIndexForJavaLangThrowableGetMessage());
}
final public void invokevirtual(MethodBinding methodBinding) {
	if (DEBUG) System.out.println(position + "\t\tinvokevirtual:"+methodBinding); //$NON-NLS-1$
	// initialized to 1 to take into account this  immediately
	countLabels = 0;
	int argCount = 1;
	int id;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	writeUnsignedShort(constantPool.literalIndex(methodBinding));
	for (int i = methodBinding.parameters.length - 1; i >= 0; i--)
		if (((id = methodBinding.parameters[i].id) == T_double) || (id == T_long))
			argCount += 2;
		else
			argCount++;
	if (((id = methodBinding.returnType.id) == T_double) || (id == T_long))
		stackDepth += (2 - argCount);
	else
		if (id == T_void)
			stackDepth -= argCount;
		else
			stackDepth += (1 - argCount);
	if (stackDepth > stackMax)
		stackMax = stackDepth;
}
final public void ior() {
	if (DEBUG) System.out.println(position + "\t\tior"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ior;
}
final public void irem() {
	if (DEBUG) System.out.println(position + "\t\tirem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_irem;
}
final public void ireturn() {
	if (DEBUG) System.out.println(position + "\t\tireturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ireturn;
}
public boolean isDefinitelyAssigned(Scope scope, int initStateIndex, LocalVariableBinding local) {
	// Dependant of UnconditionalFlowInfo.isDefinitelyAssigned(..)
	if (initStateIndex == -1)
		return false;
	if (local.isArgument) {
		return true;
	}
	int localPosition = local.id + maxFieldCount;
	MethodScope methodScope = scope.methodScope();
	// id is zero-based
	if (localPosition < UnconditionalFlowInfo.BitCacheSize) {
		return (methodScope.definiteInits[initStateIndex] & (1L << localPosition)) != 0; // use bits
	}
	// use extra vector
	long[] extraInits = methodScope.extraDefiniteInits[initStateIndex];
	if (extraInits == null)
		return false; // if vector not yet allocated, then not initialized
	int vectorIndex;
	if ((vectorIndex = (localPosition / UnconditionalFlowInfo.BitCacheSize) - 1) >= extraInits.length)
		return false; // if not enough room in vector, then not initialized 
	return ((extraInits[vectorIndex]) & (1L << (localPosition % UnconditionalFlowInfo.BitCacheSize))) != 0;
}
final public void ishl() {
	if (DEBUG) System.out.println(position + "\t\tishl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ishl;
}
final public void ishr() {
	if (DEBUG) System.out.println(position + "\t\tishr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ishr;
}
final public void istore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tistore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= iArg) {
		maxLocals = iArg + 1;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_istore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_istore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void istore_0() {
	if (DEBUG) System.out.println(position + "\t\tistore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals == 0) {
		maxLocals = 1;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_istore_0;
}
final public void istore_1() {
	if (DEBUG) System.out.println(position + "\t\tistore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 1) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_istore_1;
}
final public void istore_2() {
	if (DEBUG) System.out.println(position + "\t\tistore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 2) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_istore_2;
}
final public void istore_3() {
	if (DEBUG) System.out.println(position + "\t\tistore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (maxLocals <= 3) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_istore_3;
}
final public void isub() {
	if (DEBUG) System.out.println(position + "\t\tisub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_isub;
}
final public void iushr() {
	if (DEBUG) System.out.println(position + "\t\tiushr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_iushr;
}
final public void ixor() {
	if (DEBUG) System.out.println(position + "\t\tixor"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ixor;
}
final public void jsr(Label lbl) {
	if (this.wideMode) {
		this.jsr_w(lbl);
		return;
	}
	if (DEBUG) System.out.println(position + "\t\tjsr"+lbl); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_jsr;
	lbl.branch();
}
final public void jsr_w(Label lbl) {
	if (DEBUG) System.out.println(position + "\t\tjsr_w"+lbl); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_jsr_w;
	lbl.branchWide();
}
final public void l2d() {
	if (DEBUG) System.out.println(position + "\t\tl2d"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_l2d;
}
final public void l2f() {
	if (DEBUG) System.out.println(position + "\t\tl2f"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_l2f;
}
final public void l2i() {
	if (DEBUG) System.out.println(position + "\t\tl2i"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_l2i;
}
final public void ladd() {
	if (DEBUG) System.out.println(position + "\t\tladd"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ladd;
}
final public void laload() {
	if (DEBUG) System.out.println(position + "\t\tlaload"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_laload;
}
final public void land() {
	if (DEBUG) System.out.println(position + "\t\tland"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_land;
}
final public void lastore() {
	if (DEBUG) System.out.println(position + "\t\tlastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 4;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lastore;
}
final public void lcmp() {
	if (DEBUG) System.out.println(position + "\t\tlcmp"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lcmp;
}
final public void lconst_0() {
	if (DEBUG) System.out.println(position + "\t\tlconst_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lconst_0;
}
final public void lconst_1() {
	if (DEBUG) System.out.println(position + "\t\tlconst_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lconst_1;
}
final public void ldc(float constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+constant); //$NON-NLS-1$
		// Generate a ldc_w
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		if (DEBUG) System.out.println(position + "\t\tldc:"+constant); //$NON-NLS-1$
		// Generate a ldc
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
final public void ldc(int constant) {
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+constant); //$NON-NLS-1$
		// Generate a ldc_w
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		if (DEBUG) System.out.println(position + "\t\tldc:"+constant); //$NON-NLS-1$
		// Generate a ldc
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
final public void ldc(String constant) {
	countLabels = 0;
	int currentConstantPoolIndex = constantPool.currentIndex;
	int currentConstantPoolOffset = constantPool.currentOffset;
	int currentCodeStreamPosition = position;
	int index = constantPool.literalIndexForLdc(constant.toCharArray());
	if (index > 0) {
		// the string already exists inside the constant pool
		// we reuse the same index
		stackDepth++;
		if (stackDepth > stackMax)
			stackMax = stackDepth;
		if (index > 255) {
			if (DEBUG) System.out.println(position + "\t\tldc_w:"+constant); //$NON-NLS-1$
			// Generate a ldc_w
			if (classFileOffset + 2 >= bCodeStream.length) {
				resizeByteArray();
			}
			position++;
			bCodeStream[classFileOffset++] = OPC_ldc_w;
			writeUnsignedShort(index);
		} else {
			if (DEBUG) System.out.println(position + "\t\tldc:"+constant); //$NON-NLS-1$
			// Generate a ldc
			if (classFileOffset + 1 >= bCodeStream.length) {
				resizeByteArray();
			}
			position += 2;
			bCodeStream[classFileOffset++] = OPC_ldc;
			bCodeStream[classFileOffset++] = (byte) index;
		}
	} else {
		// the string is too big to be utf8-encoded in one pass.
		// we have to split it into different pieces.
		// first we clean all side-effects due to the code above
		// this case is very rare, so we can afford to lose time to handle it
		char[] constantChars = constant.toCharArray();
		position = currentCodeStreamPosition;
		constantPool.currentIndex = currentConstantPoolIndex;
		constantPool.currentOffset = currentConstantPoolOffset;
		constantPool.stringCache.remove(constantChars);
		constantPool.UTF8Cache.remove(constantChars);
		int i = 0;
		int length = 0;
		int constantLength = constant.length();
		byte[] utf8encoding = new byte[Math.min(constantLength + 100, 65535)];
		int utf8encodingLength = 0;
		while ((length < 65532) && (i < constantLength)) {
			char current = constantChars[i];
			// we resize the byte array immediately if necessary
			if (length + 3 > (utf8encodingLength = utf8encoding.length)) {
				System.arraycopy(utf8encoding, 0, utf8encoding = new byte[Math.min(utf8encodingLength + 100, 65535)], 0, length);
			}
			if ((current >= 0x0001) && (current <= 0x007F)) {
				// we only need one byte: ASCII table
				utf8encoding[length++] = (byte) current;
			} else {
				if (current > 0x07FF) {
					// we need 3 bytes
					utf8encoding[length++] = (byte) (0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
					utf8encoding[length++] = (byte) (0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
					utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				} else {
					// we can be 0 or between 0x0080 and 0x07FF
					// In that case we only need 2 bytes
					utf8encoding[length++] = (byte) (0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
					utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
				}
			}
			i++;
		}
		// check if all the string is encoded (PR 1PR2DWJ)
		// the string is too big to be encoded in one pass
		newStringContatenation();
		dup();
		// write the first part
		char[] subChars = new char[i];
		System.arraycopy(constantChars, 0, subChars, 0, i);
		System.arraycopy(utf8encoding, 0, utf8encoding = new byte[length], 0, length);
		index = constantPool.literalIndex(subChars, utf8encoding);
		stackDepth++;
		if (stackDepth > stackMax)
			stackMax = stackDepth;
		if (index > 255) {
			// Generate a ldc_w
			if (classFileOffset + 2 >= bCodeStream.length) {
				resizeByteArray();
			}
			position++;
			bCodeStream[classFileOffset++] = OPC_ldc_w;
			writeUnsignedShort(index);
		} else {
			// Generate a ldc
			if (classFileOffset + 1 >= bCodeStream.length) {
				resizeByteArray();
			}
			position += 2;
			bCodeStream[classFileOffset++] = OPC_ldc;
			bCodeStream[classFileOffset++] = (byte) index;
		}
		// write the remaining part
		invokeStringConcatenationStringConstructor();
		while (i < constantLength) {
			length = 0;
			utf8encoding = new byte[Math.min(constantLength - i + 100, 65535)];
			int startIndex = i;
			while ((length < 65532) && (i < constantLength)) {
				char current = constantChars[i];
				// we resize the byte array immediately if necessary
				if (constantLength + 2 > (utf8encodingLength = utf8encoding.length)) {
					System.arraycopy(utf8encoding, 0, utf8encoding = new byte[Math.min(utf8encodingLength + 100, 65535)], 0, length);
				}
				if ((current >= 0x0001) && (current <= 0x007F)) {
					// we only need one byte: ASCII table
					utf8encoding[length++] = (byte) current;
				} else {
					if (current > 0x07FF) {
						// we need 3 bytes
						utf8encoding[length++] = (byte) (0xE0 | ((current >> 12) & 0x0F)); // 0xE0 = 1110 0000
						utf8encoding[length++] = (byte) (0x80 | ((current >> 6) & 0x3F)); // 0x80 = 1000 0000
						utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					} else {
						// we can be 0 or between 0x0080 and 0x07FF
						// In that case we only need 2 bytes
						utf8encoding[length++] = (byte) (0xC0 | ((current >> 6) & 0x1F)); // 0xC0 = 1100 0000
						utf8encoding[length++] = (byte) (0x80 | (current & 0x3F)); // 0x80 = 1000 0000
					}
				}
				i++;
			}
			// the next part is done
			subChars = new char[i - startIndex];
			System.arraycopy(constantChars, startIndex, subChars, 0, i - startIndex);
			System.arraycopy(utf8encoding, 0, utf8encoding = new byte[length], 0, length);
			index = constantPool.literalIndex(subChars, utf8encoding);
			stackDepth++;
			if (stackDepth > stackMax)
				stackMax = stackDepth;
			if (index > 255) {
				// Generate a ldc_w
				if (classFileOffset + 2 >= bCodeStream.length) {
					resizeByteArray();
				}
				position++;
				bCodeStream[classFileOffset++] = OPC_ldc_w;
				writeUnsignedShort(index);
			} else {
				// Generate a ldc
				if (classFileOffset + 1 >= bCodeStream.length) {
					resizeByteArray();
				}
				position += 2;
				bCodeStream[classFileOffset++] = OPC_ldc;
				bCodeStream[classFileOffset++] = (byte) index;
			}
			// now on the stack it should be a StringBuffer and a string.
			invokeStringConcatenationAppendForType(T_String);
		}
		invokeStringConcatenationToString();
		invokeStringIntern();
	}
}
final public void ldc(TypeBinding typeBinding) {
	countLabels = 0;
	int index = constantPool.literalIndex(typeBinding);
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (index > 255) {
		if (DEBUG) System.out.println(position + "\t\tldc_w:"+ typeBinding); //$NON-NLS-1$
		// Generate a ldc_w
		if (classFileOffset + 2 >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = OPC_ldc_w;
		writeUnsignedShort(index);
	} else {
		if (DEBUG) System.out.println(position + "\t\tldw:"+ typeBinding); //$NON-NLS-1$
		// Generate a ldc
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_ldc;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
final public void ldc2_w(double constant) {
	if (DEBUG) System.out.println(position + "\t\tldc2_w:"+constant); //$NON-NLS-1$
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	// Generate a ldc2_w
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ldc2_w;
	writeUnsignedShort(index);
}
final public void ldc2_w(long constant) {
	if (DEBUG) System.out.println(position + "\t\tldc2_w:"+constant); //$NON-NLS-1$
	countLabels = 0;
	int index = constantPool.literalIndex(constant);
	stackDepth += 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	// Generate a ldc2_w
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ldc2_w;
	writeUnsignedShort(index);
}
final public void ldiv() {
	if (DEBUG) System.out.println(position + "\t\tldiv"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_ldiv;
}
final public void lload(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tlload:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_lload;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_lload;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void lload_0() {
	if (DEBUG) System.out.println(position + "\t\tlload_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lload_0;
}
final public void lload_1() {
	if (DEBUG) System.out.println(position + "\t\tlload_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lload_1;
}
final public void lload_2() {
	if (DEBUG) System.out.println(position + "\t\tlload_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lload_2;
}
final public void lload_3() {
	if (DEBUG) System.out.println(position + "\t\tlload_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth += 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lload_3;
}
final public void lmul() {
	if (DEBUG) System.out.println(position + "\t\tlmul"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lmul;
}
final public void lneg() {
	if (DEBUG) System.out.println(position + "\t\tlneg"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lneg;
}
public final void load(LocalVariableBinding localBinding) {
	countLabels = 0;
	TypeBinding typeBinding = localBinding.type;
	int resolvedPosition = localBinding.resolvedPosition;
	// Using dedicated int bytecode
	if (typeBinding == IntBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			//case -1 :
			// internal failure: trying to load variable not supposed to be generated
			//	break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}
	// Using dedicated float bytecode
	if (typeBinding == FloatBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.fload_0();
				break;
			case 1 :
				this.fload_1();
				break;
			case 2 :
				this.fload_2();
				break;
			case 3 :
				this.fload_3();
				break;
			default :
				this.fload(resolvedPosition);
		}
		return;
	}
	// Using dedicated long bytecode
	if (typeBinding == LongBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.lload_0();
				break;
			case 1 :
				this.lload_1();
				break;
			case 2 :
				this.lload_2();
				break;
			case 3 :
				this.lload_3();
				break;
			default :
				this.lload(resolvedPosition);
		}
		return;
	}
	// Using dedicated double bytecode
	if (typeBinding == DoubleBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.dload_0();
				break;
			case 1 :
				this.dload_1();
				break;
			case 2 :
				this.dload_2();
				break;
			case 3 :
				this.dload_3();
				break;
			default :
				this.dload(resolvedPosition);
		}
		return;
	}
	// boolean, byte, char and short are handled as int
	if ((typeBinding == ByteBinding) || (typeBinding == CharBinding) || (typeBinding == BooleanBinding) || (typeBinding == ShortBinding)) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}

	// Reference object
	switch (resolvedPosition) {
		case 0 :
			this.aload_0();
			break;
		case 1 :
			this.aload_1();
			break;
		case 2 :
			this.aload_2();
			break;
		case 3 :
			this.aload_3();
			break;
		default :
			this.aload(resolvedPosition);
	}
}
public final void load(TypeBinding typeBinding, int resolvedPosition) {
	countLabels = 0;
	// Using dedicated int bytecode
	if (typeBinding == IntBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}
	// Using dedicated float bytecode
	if (typeBinding == FloatBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.fload_0();
				break;
			case 1 :
				this.fload_1();
				break;
			case 2 :
				this.fload_2();
				break;
			case 3 :
				this.fload_3();
				break;
			default :
				this.fload(resolvedPosition);
		}
		return;
	}
	// Using dedicated long bytecode
	if (typeBinding == LongBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.lload_0();
				break;
			case 1 :
				this.lload_1();
				break;
			case 2 :
				this.lload_2();
				break;
			case 3 :
				this.lload_3();
				break;
			default :
				this.lload(resolvedPosition);
		}
		return;
	}
	// Using dedicated double bytecode
	if (typeBinding == DoubleBinding) {
		switch (resolvedPosition) {
			case 0 :
				this.dload_0();
				break;
			case 1 :
				this.dload_1();
				break;
			case 2 :
				this.dload_2();
				break;
			case 3 :
				this.dload_3();
				break;
			default :
				this.dload(resolvedPosition);
		}
		return;
	}
	// boolean, byte, char and short are handled as int
	if ((typeBinding == ByteBinding) || (typeBinding == CharBinding) || (typeBinding == BooleanBinding) || (typeBinding == ShortBinding)) {
		switch (resolvedPosition) {
			case 0 :
				this.iload_0();
				break;
			case 1 :
				this.iload_1();
				break;
			case 2 :
				this.iload_2();
				break;
			case 3 :
				this.iload_3();
				break;
			default :
				this.iload(resolvedPosition);
		}
		return;
	}

	// Reference object
	switch (resolvedPosition) {
		case 0 :
			this.aload_0();
			break;
		case 1 :
			this.aload_1();
			break;
		case 2 :
			this.aload_2();
			break;
		case 3 :
			this.aload_3();
			break;
		default :
			this.aload(resolvedPosition);
	}
}
public final void loadInt(int resolvedPosition) {
	// Using dedicated int bytecode
	switch (resolvedPosition) {
		case 0 :
			this.iload_0();
			break;
		case 1 :
			this.iload_1();
			break;
		case 2 :
			this.iload_2();
			break;
		case 3 :
			this.iload_3();
			break;
		default :
			this.iload(resolvedPosition);
	}
}
public final void loadObject(int resolvedPosition) {
	switch (resolvedPosition) {
		case 0 :
			this.aload_0();
			break;
		case 1 :
			this.aload_1();
			break;
		case 2 :
			this.aload_2();
			break;
		case 3 :
			this.aload_3();
			break;
		default :
			this.aload(resolvedPosition);
	}
}
final public void lookupswitch(CaseLabel defaultLabel, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	if (DEBUG) System.out.println(position + "\t\tlookupswitch"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	int length = keys.length;
	int pos = position;
	defaultLabel.placeInstruction();
	for (int i = 0; i < length; i++) {
		casesLabel[i].placeInstruction();
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lookupswitch;
	for (int i = (3 - (pos % 4)); i > 0; i--) {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = 0;
	}
	defaultLabel.branch();
	writeSignedWord(length);
	for (int i = 0; i < length; i++) {
		writeSignedWord(keys[sortedIndexes[i]]);
		casesLabel[sortedIndexes[i]].branch();
	}
}
final public void lor() {
	if (DEBUG) System.out.println(position + "\t\tlor"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lor;
}
final public void lrem() {
	if (DEBUG) System.out.println(position + "\t\tlrem"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lrem;
}
final public void lreturn() {
	if (DEBUG) System.out.println(position + "\t\tlreturn"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lreturn;
}
final public void lshl() {
	if (DEBUG) System.out.println(position + "\t\tlshl"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lshl;
}
final public void lshr() {
	if (DEBUG) System.out.println(position + "\t\tlshr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lshr;
}
final public void lstore(int iArg) {
	if (DEBUG) System.out.println(position + "\t\tlstore:"+iArg); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals <= iArg + 1) {
		maxLocals = iArg + 2;
	}
	if (iArg > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_lstore;
		writeUnsignedShort(iArg);
	} else {
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_lstore;
		bCodeStream[classFileOffset++] = (byte) iArg;
	}
}
final public void lstore_0() {
	if (DEBUG) System.out.println(position + "\t\tlstore_0"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 2) {
		maxLocals = 2;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lstore_0;
}
final public void lstore_1() {
	if (DEBUG) System.out.println(position + "\t\tlstore_1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 3) {
		maxLocals = 3;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lstore_1;
}
final public void lstore_2() {
	if (DEBUG) System.out.println(position + "\t\tlstore_2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 4) {
		maxLocals = 4;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lstore_2;
}
final public void lstore_3() {
	if (DEBUG) System.out.println(position + "\t\tlstore_3"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (maxLocals < 5) {
		maxLocals = 5;
	}
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lstore_3;
}
final public void lsub() {
	if (DEBUG) System.out.println(position + "\t\tlsub"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lsub;
}
final public void lushr() {
	if (DEBUG) System.out.println(position + "\t\tlushr"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lushr;
}
final public void lxor() {
	if (DEBUG) System.out.println(position + "\t\tlxor"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_lxor;
}
final public void monitorenter() {
	if (DEBUG) System.out.println(position + "\t\tmonitorenter"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_monitorenter;
}
final public void monitorexit() {
	if (DEBUG) System.out.println(position + "\t\tmonitorexit"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_monitorexit;
}
final public void multianewarray(TypeBinding typeBinding, int dimensions) {
	if (DEBUG) System.out.println(position + "\t\tmultinewarray:"+typeBinding+","+dimensions); //$NON-NLS-1$ //$NON-NLS-2$
	countLabels = 0;
	stackDepth += (1 - dimensions);
	if (classFileOffset + 3 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = OPC_multianewarray;
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
	bCodeStream[classFileOffset++] = (byte) dimensions;
}
/**
 * We didn't call it new, because there is a conflit with the new keyword
 */
final public void new_(TypeBinding typeBinding) {
	if (DEBUG) System.out.println(position + "\t\tnew:"+typeBinding); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	writeUnsignedShort(constantPool.literalIndex(typeBinding));
}
final public void newarray(int array_Type) {
	if (DEBUG) System.out.println(position + "\t\tnewarray:"+array_Type); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = OPC_newarray;
	bCodeStream[classFileOffset++] = (byte) array_Type;
}
public void newArray(Scope scope, ArrayBinding arrayBinding) {
	TypeBinding component = arrayBinding.elementsType();
	switch (component.id) {
		case T_int :
			this.newarray(INT_ARRAY);
			break;
		case T_byte :
			this.newarray(BYTE_ARRAY);
			break;
		case T_boolean :
			this.newarray(BOOLEAN_ARRAY);
			break;
		case T_short :
			this.newarray(SHORT_ARRAY);
			break;
		case T_char :
			this.newarray(CHAR_ARRAY);
			break;
		case T_long :
			this.newarray(LONG_ARRAY);
			break;
		case T_float :
			this.newarray(FLOAT_ARRAY);
			break;
		case T_double :
			this.newarray(DOUBLE_ARRAY);
			break;
		default :
			this.anewarray(component);
	}
}
public void newJavaLangError() {
	// new: java.lang.Error
	if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Error"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	writeUnsignedShort(constantPool.literalIndexForJavaLangError());
}

public void newJavaLangAssertionError() {
	// new: java.lang.AssertionError
	if (DEBUG) System.out.println(position + "\t\tnew: java.lang.AssertionError"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	writeUnsignedShort(constantPool.literalIndexForJavaLangAssertionError());
}

public void newNoClassDefFoundError() {
	// new: java.lang.NoClassDefFoundError
	if (DEBUG) System.out.println(position + "\t\tnew: java.lang.NoClassDefFoundError"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	writeUnsignedShort(constantPool.literalIndexForJavaLangNoClassDefFoundError());
}
public void newStringContatenation() {
	// new: java.lang.StringBuffer
	// new: java.lang.StringBuilder
	if (DEBUG) {
		if (this.targetLevel >= JDK1_5) {
			System.out.println(position + "\t\tnew: java.lang.StringBuilder"); //$NON-NLS-1$
		} else {
			System.out.println(position + "\t\tnew: java.lang.StringBuffer"); //$NON-NLS-1$
		}
	}
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax) {
		stackMax = stackDepth;
	}
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	if (this.targetLevel >= JDK1_5) {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilder());
	} else {
		writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuffer());
	}
}
public void newWrapperFor(int typeID) {
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	switch (typeID) {
		case T_int : // new: java.lang.Integer
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Integer"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangInteger());
			break;
		case T_boolean : // new: java.lang.Boolean
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Boolean"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangBoolean());
			break;
		case T_byte : // new: java.lang.Byte
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Byte"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangByte());
			break;
		case T_char : // new: java.lang.Character
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Character"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangCharacter());
			break;
		case T_float : // new: java.lang.Float
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Float"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangFloat());
			break;
		case T_double : // new: java.lang.Double
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Double"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangDouble());
			break;
		case T_short : // new: java.lang.Short
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Short"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangShort());
			break;
		case T_long : // new: java.lang.Long
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Long"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangLong());
			break;
		case T_void : // new: java.lang.Void
			if (DEBUG) System.out.println(position + "\t\tnew: java.lang.Void"); //$NON-NLS-1$
			writeUnsignedShort(constantPool.literalIndexForJavaLangVoid());
	}
}
final public void nop() {
	if (DEBUG) System.out.println(position + "\t\tnop"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_nop;
}
final public void pop() {
	if (DEBUG) System.out.println(position + "\t\tpop"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_pop;
}
final public void pop2() {
	if (DEBUG) System.out.println(position + "\t\tpop2"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 2;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_pop2;
}
final public void putfield(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tputfield:"+fieldBinding); //$NON-NLS-1$
	countLabels = 0;
	int id;
	if (((id = fieldBinding.type.id) == T_double) || (id == T_long))
		stackDepth -= 3;
	else
		stackDepth -= 2;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_putfield;
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
final public void putstatic(FieldBinding fieldBinding) {
	if (DEBUG) System.out.println(position + "\t\tputstatic:"+fieldBinding); //$NON-NLS-1$
	countLabels = 0;
	int id;
	if (((id = fieldBinding.type.id) == T_double) || (id == T_long))
		stackDepth -= 2;
	else
		stackDepth -= 1;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset + 2 >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_putstatic;
	writeUnsignedShort(constantPool.literalIndex(fieldBinding));
}
public void record(LocalVariableBinding local) {
	if (!generateLocalVariableTableAttributes)
		return;
	if (allLocalsCounter == locals.length) {
		// resize the collection
		System.arraycopy(locals, 0, locals = new LocalVariableBinding[allLocalsCounter + LOCALS_INCREMENT], 0, allLocalsCounter);
	}
	locals[allLocalsCounter++] = local;
	local.initializationPCs = new int[4];
	local.initializationCount = 0;
}
public void recordPositionsFrom(int startPC, int sourcePos) {

	/* Record positions in the table, only if nothing has 
	 * already been recorded. Since we output them on the way 
	 * up (children first for more specific info)
	 * The pcToSourceMap table is always sorted.
	 */

	if (!generateLineNumberAttributes)
		return;
	if (sourcePos == 0)
		return;

	// no code generated for this node. e.g. field without any initialization
	if (position == startPC)
		return;

	// Widening an existing entry that already has the same source positions
	if (pcToSourceMapSize + 4 > pcToSourceMap.length) {
		// resize the array pcToSourceMap
		System.arraycopy(pcToSourceMap, 0, pcToSourceMap = new int[pcToSourceMapSize << 1], 0, pcToSourceMapSize);
	}
	int newLine = ClassFile.searchLineNumber(lineSeparatorPositions, sourcePos);
	// lastEntryPC represents the endPC of the lastEntry.
	if (pcToSourceMapSize > 0) {
		// in this case there is already an entry in the table
		if (pcToSourceMap[pcToSourceMapSize - 1] != newLine) {
			if (startPC < lastEntryPC) {
				// we forgot to add an entry.
				// search if an existing entry exists for startPC
				int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
				if (insertionIndex != -1) {
					// there is no existing entry starting with startPC.
					int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, newLine); // index for PC
					/* the existingEntryIndex corresponds to en entry with the same line and a PC >= startPC.
						in this case it is relevant to widen this entry instead of creating a new one.
						line1: this(a,
						  b,
						  c);
						with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
						aload0 bytecode. The first entry is the one for the argument a.
						But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
						So we widen the existing entry (if there is one) or we create a new entry with the startPC.
					*/
					if (existingEntryIndex != -1) {
						// widen existing entry
						pcToSourceMap[existingEntryIndex] = startPC;
					} else if (insertionIndex < 1 || pcToSourceMap[insertionIndex - 1] != newLine) {
						// we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
						System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - insertionIndex);
						pcToSourceMap[insertionIndex++] = startPC;
						pcToSourceMap[insertionIndex] = newLine;
						pcToSourceMapSize += 2;
					}
				} else if (position != lastEntryPC) { // no bytecode since last entry pc
					pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
					pcToSourceMap[pcToSourceMapSize++] = newLine;
				}
			} else {
				// we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
				pcToSourceMap[pcToSourceMapSize++] = startPC;
				pcToSourceMap[pcToSourceMapSize++] = newLine;
			}
		} else {
			/* the last recorded entry is on the same line. But it could be relevant to widen this entry.
			   we want to extend this entry forward in case we generated some bytecode before the last entry that are not related to any statement
			*/	
			if (startPC < pcToSourceMap[pcToSourceMapSize - 2]) {
				int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
				if (insertionIndex != -1) {
					// widen the existing entry
					// we have to figure out if we need to move the last entry at another location to keep a sorted table
					/* First we need to check if at the insertion position there is not an existing entry
					 * that includes the one we want to insert. This is the case if pcToSourceMap[insertionIndex - 1] == newLine.
					 * In this case we don't want to change the table. If not, we want to insert a new entry. Prior to insertion
					 * we want to check if it is worth doing an arraycopy. If not we simply update the recorded pc.
					 */
					if (!((insertionIndex > 1) && (pcToSourceMap[insertionIndex - 1] == newLine))) {
						if ((pcToSourceMapSize > 4) && (pcToSourceMap[pcToSourceMapSize - 4] > startPC)) {
							System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2, pcToSourceMapSize - 2 - insertionIndex);
							pcToSourceMap[insertionIndex++] = startPC;
							pcToSourceMap[insertionIndex] = newLine;						
						} else {
							pcToSourceMap[pcToSourceMapSize - 2] = startPC;
						}
					}
				}
			}
		}
		lastEntryPC = position;
	} else {
		// record the first entry
		pcToSourceMap[pcToSourceMapSize++] = startPC;
		pcToSourceMap[pcToSourceMapSize++] = newLine;
		lastEntryPC = position;
	}
}
/**
 * @param anExceptionLabel org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel
 */
public void registerExceptionHandler(ExceptionLabel anExceptionLabel) {
	int length;
	if (exceptionHandlersIndex >= (length = exceptionHandlers.length)) {
		// resize the exception handlers table
		System.arraycopy(exceptionHandlers, 0, exceptionHandlers = new ExceptionLabel[length + LABELS_INCREMENT], 0, length);
	}
	// no need to resize. So just add the new exception label
	exceptionHandlers[exceptionHandlersIndex++] = anExceptionLabel;
	exceptionHandlersCounter++;
}
public void removeExceptionHandler(ExceptionLabel exceptionLabel) {
	for (int i = 0; i < exceptionHandlersIndex; i++) {
		if (exceptionHandlers[i] == exceptionLabel) {
			exceptionHandlers[i] = null;
			exceptionHandlersCounter--;
			return;
		}
	}
}
public final void removeNotDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// given some flow info, make sure we did not loose some variables initialization
	// if this happens, then we must update their pc entries to reflect it in debug attributes
	if (!generateLocalVariableTableAttributes)
		return;
/*	if (initStateIndex == lastInitStateIndexWhenRemovingInits)
		return;
		
	lastInitStateIndexWhenRemovingInits = initStateIndex;
	if (lastInitStateIndexWhenAddingInits != initStateIndex){
		lastInitStateIndexWhenAddingInits = -2;// reinitialize add index 
		// add(1)-remove(1)-add(1) -> ignore second add
		// add(1)-remove(2)-add(1) -> perform second add
	}*/
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			if (initStateIndex == -1 || !isDefinitelyAssigned(scope, initStateIndex, localBinding)) {
				if (localBinding.initializationCount > 0) {
					localBinding.recordInitializationEndPC(position);
				}
			}
		}
	}
}
/**
 * @param referenceMethod org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration
 * @param targetClassFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
 */
public void reset(AbstractMethodDeclaration referenceMethod, ClassFile targetClassFile) {
	init(targetClassFile);
	this.methodDeclaration = referenceMethod;
	preserveUnusedLocals = referenceMethod.scope.problemReporter().options.preserveAllLocalVariables;
	initializeMaxLocals(referenceMethod.binding);
}
/**
 * @param targetClassFile The given classfile to reset the code stream
 */
public void resetForProblemClinit(ClassFile targetClassFile) {
	init(targetClassFile);
	maxLocals = 0;
}
private final void resizeByteArray() {
	int length = bCodeStream.length;
	int requiredSize = length + length;
	if (classFileOffset > requiredSize) {
		// must be sure to grow by enough
		requiredSize = classFileOffset + length;
	}
	System.arraycopy(bCodeStream, 0, bCodeStream = new byte[requiredSize], 0, length);
}
final public void ret(int index) {
	if (DEBUG) System.out.println(position + "\t\tret:"+index); //$NON-NLS-1$
	countLabels = 0;
	if (index > 255) { // Widen
		if (classFileOffset + 3 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_wide;
		bCodeStream[classFileOffset++] = OPC_ret;
		writeUnsignedShort(index);
	} else { // Don't Widen
		if (classFileOffset + 1 >= bCodeStream.length) {
			resizeByteArray();
		}
		position += 2;
		bCodeStream[classFileOffset++] = OPC_ret;
		bCodeStream[classFileOffset++] = (byte) index;
	}
}
final public void return_() {
	if (DEBUG) System.out.println(position + "\t\treturn"); //$NON-NLS-1$
	countLabels = 0;
	// the stackDepth should be equal to 0 
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_return;
}
final public void saload() {
	if (DEBUG) System.out.println(position + "\t\tsaload"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_saload;
}
final public void sastore() {
	if (DEBUG) System.out.println(position + "\t\tsastore"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth -= 3;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_sastore;
}
/**
 * @param operatorConstant int
 * @param type_ID int
 */
public void sendOperator(int operatorConstant, int type_ID) {
	switch (type_ID) {
		case T_int :
		case T_boolean :
		case T_char :
		case T_byte :
		case T_short :
			switch (operatorConstant) {
				case PLUS :
					this.iadd();
					break;
				case MINUS :
					this.isub();
					break;
				case MULTIPLY :
					this.imul();
					break;
				case DIVIDE :
					this.idiv();
					break;
				case REMAINDER :
					this.irem();
					break;
				case LEFT_SHIFT :
					this.ishl();
					break;
				case RIGHT_SHIFT :
					this.ishr();
					break;
				case UNSIGNED_RIGHT_SHIFT :
					this.iushr();
					break;
				case AND :
					this.iand();
					break;
				case OR :
					this.ior();
					break;
				case XOR :
					this.ixor();
					break;
			}
			break;
		case T_long :
			switch (operatorConstant) {
				case PLUS :
					this.ladd();
					break;
				case MINUS :
					this.lsub();
					break;
				case MULTIPLY :
					this.lmul();
					break;
				case DIVIDE :
					this.ldiv();
					break;
				case REMAINDER :
					this.lrem();
					break;
				case LEFT_SHIFT :
					this.lshl();
					break;
				case RIGHT_SHIFT :
					this.lshr();
					break;
				case UNSIGNED_RIGHT_SHIFT :
					this.lushr();
					break;
				case AND :
					this.land();
					break;
				case OR :
					this.lor();
					break;
				case XOR :
					this.lxor();
					break;
			}
			break;
		case T_float :
			switch (operatorConstant) {
				case PLUS :
					this.fadd();
					break;
				case MINUS :
					this.fsub();
					break;
				case MULTIPLY :
					this.fmul();
					break;
				case DIVIDE :
					this.fdiv();
					break;
				case REMAINDER :
					this.frem();
			}
			break;
		case T_double :
			switch (operatorConstant) {
				case PLUS :
					this.dadd();
					break;
				case MINUS :
					this.dsub();
					break;
				case MULTIPLY :
					this.dmul();
					break;
				case DIVIDE :
					this.ddiv();
					break;
				case REMAINDER :
					this.drem();
			}
	}
}
final public void sipush(int s) {
	if (DEBUG) System.out.println(position + "\t\tsipush:"+s); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth > stackMax)
		stackMax = stackDepth;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_sipush;
	writeSignedShort(s);
}
public static final void sort(int[] tab, int lo0, int hi0, int[] result) {
	int lo = lo0;
	int hi = hi0;
	int mid;
	if (hi0 > lo0) {
		/* Arbitrarily establishing partition element as the midpoint of
		  * the array.
		  */
		mid = tab[ (lo0 + hi0) / 2];
		// loop through the array until indices cross
		while (lo <= hi) {
			/* find the first element that is greater than or equal to 
			 * the partition element starting from the left Index.
			 */
			while ((lo < hi0) && (tab[lo] < mid))
				++lo;
			/* find an element that is smaller than or equal to 
			 * the partition element starting from the right Index.
			 */
			while ((hi > lo0) && (tab[hi] > mid))
				--hi;
			// if the indexes have not crossed, swap
			if (lo <= hi) {
				swap(tab, lo, hi, result);
				++lo;
				--hi;
			}
		}
		/* If the right index has not reached the left side of array
		  * must now sort the left partition.
		  */
		if (lo0 < hi)
			sort(tab, lo0, hi, result);
		/* If the left index has not reached the right side of array
		  * must now sort the right partition.
		  */
		if (lo < hi0)
			sort(tab, lo, hi0, result);
	}
}

public final void store(LocalVariableBinding localBinding, boolean valueRequired) {
	int localPosition = localBinding.resolvedPosition;
	// Using dedicated int bytecode
	switch(localBinding.type.id) {
		case TypeIds.T_int :
		case TypeIds.T_char :
		case TypeIds.T_byte :
		case TypeIds.T_short :
		case TypeIds.T_boolean :
			if (valueRequired)
				this.dup();
			switch (localPosition) {
				case 0 :
					this.istore_0();
					break;
				case 1 :
					this.istore_1();
					break;
				case 2 :
					this.istore_2();
					break;
				case 3 :
					this.istore_3();
					break;
				//case -1 :
				// internal failure: trying to store into variable not supposed to be generated
				//	break;
				default :
					this.istore(localPosition);
			}
			break;
		case TypeIds.T_float :
			if (valueRequired)
				this.dup();
			switch (localPosition) {
				case 0 :
					this.fstore_0();
					break;
				case 1 :
					this.fstore_1();
					break;
				case 2 :
					this.fstore_2();
					break;
				case 3 :
					this.fstore_3();
					break;
				default :
					this.fstore(localPosition);
			}
			break;
		case TypeIds.T_double :
			if (valueRequired)
				this.dup2();
			switch (localPosition) {
				case 0 :
					this.dstore_0();
					break;
				case 1 :
					this.dstore_1();
					break;
				case 2 :
					this.dstore_2();
					break;
				case 3 :
					this.dstore_3();
					break;
				default :
					this.dstore(localPosition);
			}
			break;
		case TypeIds.T_long :
			if (valueRequired)
				this.dup2();
			switch (localPosition) {
				case 0 :
					this.lstore_0();
					break;
				case 1 :
					this.lstore_1();
					break;
				case 2 :
					this.lstore_2();
					break;
				case 3 :
					this.lstore_3();
					break;
				default :
					this.lstore(localPosition);
			}
			break;
		default:
			// Reference object
			if (valueRequired)
				this.dup();
			switch (localPosition) {
				case 0 :
					this.astore_0();
					break;
				case 1 :
					this.astore_1();
					break;
				case 2 :
					this.astore_2();
					break;
				case 3 :
					this.astore_3();
					break;
				default :
					this.astore(localPosition);
			}
	}
}
public final void store(TypeBinding type, int localPosition) {
	// Using dedicated int bytecode
	if ((type == IntBinding) || (type == CharBinding) || (type == ByteBinding) || (type == ShortBinding) || (type == BooleanBinding)) {
		switch (localPosition) {
			case 0 :
				this.istore_0();
				break;
			case 1 :
				this.istore_1();
				break;
			case 2 :
				this.istore_2();
				break;
			case 3 :
				this.istore_3();
				break;
			default :
				this.istore(localPosition);
		}
		return;
	}
	// Using dedicated float bytecode
	if (type == FloatBinding) {
		switch (localPosition) {
			case 0 :
				this.fstore_0();
				break;
			case 1 :
				this.fstore_1();
				break;
			case 2 :
				this.fstore_2();
				break;
			case 3 :
				this.fstore_3();
				break;
			default :
				this.fstore(localPosition);
		}
		return;
	}
	// Using dedicated long bytecode
	if (type == LongBinding) {
		switch (localPosition) {
			case 0 :
				this.lstore_0();
				break;
			case 1 :
				this.lstore_1();
				break;
			case 2 :
				this.lstore_2();
				break;
			case 3 :
				this.lstore_3();
				break;
			default :
				this.lstore(localPosition);
		}
		return;
	}
	// Using dedicated double bytecode
	if (type == DoubleBinding) {
		switch (localPosition) {
			case 0 :
				this.dstore_0();
				break;
			case 1 :
				this.dstore_1();
				break;
			case 2 :
				this.dstore_2();
				break;
			case 3 :
				this.dstore_3();
				break;
			default :
				this.dstore(localPosition);
		}
		return;
	}
	// Reference object
	switch (localPosition) {
		case 0 :
			this.astore_0();
			break;
		case 1 :
			this.astore_1();
			break;
		case 2 :
			this.astore_2();
			break;
		case 3 :
			this.astore_3();
			break;
		default :
			this.astore(localPosition);
	}
}
public final void storeInt(int localPosition) {
	switch (localPosition) {
		case 0 :
			this.istore_0();
			break;
		case 1 :
			this.istore_1();
			break;
		case 2 :
			this.istore_2();
			break;
		case 3 :
			this.istore_3();
			break;
		default :
			this.istore(localPosition);
	}
}
public final void storeObject(int localPosition) {
	switch (localPosition) {
		case 0 :
			this.astore_0();
			break;
		case 1 :
			this.astore_1();
			break;
		case 2 :
			this.astore_2();
			break;
		case 3 :
			this.astore_3();
			break;
		default :
			this.astore(localPosition);
	}
}
final public void swap() {
	if (DEBUG) System.out.println(position + "\t\tswap"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_swap;
}
private static final void swap(int a[], int i, int j, int result[]) {
	int T;
	T = a[i];
	a[i] = a[j];
	a[j] = T;
	T = result[j];
	result[j] = result[i];
	result[i] = T;
}
final public void tableswitch(CaseLabel defaultLabel, int low, int high, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	if (DEBUG) System.out.println(position + "\t\ttableswitch"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth--;
	int length = casesLabel.length;
	int pos = position;
	defaultLabel.placeInstruction();
	for (int i = 0; i < length; i++)
		casesLabel[i].placeInstruction();
	if (classFileOffset >= bCodeStream.length) {
		resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_tableswitch;
	for (int i = (3 - (pos % 4)); i > 0; i--) {
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = 0;
	}
	defaultLabel.branch();
	writeSignedWord(low);
	writeSignedWord(high);
	int i = low, j = low;
	// the index j is used to know if the index i is one of the missing entries in case of an 
	// optimized tableswitch
	while (true) {
		int index;
		int key = keys[index = sortedIndexes[j - low]];
		if (key == i) {
			casesLabel[index].branch();
			j++;
			if (i == high) break; // if high is maxint, then avoids wrapping to minint.
		} else {
			defaultLabel.branch();
		}
		i++;
	}
}
public String toString() {
	StringBuffer buffer = new StringBuffer("( position:"); //$NON-NLS-1$
	buffer.append(position);
	buffer.append(",\nstackDepth:"); //$NON-NLS-1$
	buffer.append(stackDepth);
	buffer.append(",\nmaxStack:"); //$NON-NLS-1$
	buffer.append(stackMax);
	buffer.append(",\nmaxLocals:"); //$NON-NLS-1$
	buffer.append(maxLocals);
	buffer.append(")"); //$NON-NLS-1$
	return buffer.toString();
}
public void updateLastRecordedEndPC(int pos) {

	/* Tune positions in the table, this is due to some 
	 * extra bytecodes being
	 * added to some user code (jumps). */
	/** OLD CODE
		if (!generateLineNumberAttributes)
			return;
		pcToSourceMap[pcToSourceMapSize - 1][1] = position;
		// need to update the initialization endPC in case of generation of local variable attributes.
		updateLocalVariablesAttribute(pos);	
	*/	

	if (!generateLineNumberAttributes)
		return;
	this.lastEntryPC = pos;
	// need to update the initialization endPC in case of generation of local variable attributes.
	updateLocalVariablesAttribute(pos);
}
public void updateLocalVariablesAttribute(int pos) {
	// need to update the initialization endPC in case of generation of local variable attributes.
	if (generateLocalVariableTableAttributes) {
		for (int i = 0, max = locals.length; i < max; i++) {
			LocalVariableBinding local = locals[i];
			if ((local != null) && (local.initializationCount > 0)) {
				if (local.initializationPCs[((local.initializationCount - 1) << 1) + 1] == pos) {
					local.initializationPCs[((local.initializationCount - 1) << 1) + 1] = position;
				}
			}
		}
	}
}
/**
 * Write a signed 16 bits value into the byte array
 * @param value the signed short
 */
public final void writeSignedShort(int value) {
	// we keep the resize in here because it is used outside the code stream
	if (classFileOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 2;
	bCodeStream[classFileOffset++] = (byte) (value >> 8);
	bCodeStream[classFileOffset++] = (byte) value;
}
public final void writeSignedShort(int pos, int value) {
	int currentOffset = startingClassFileOffset + pos;
	if (currentOffset + 1 >= bCodeStream.length) {
		resizeByteArray();
	}
	bCodeStream[currentOffset] = (byte) (value >> 8);
	bCodeStream[currentOffset + 1] = (byte) value;
}
public final void writeSignedWord(int value) {
	// we keep the resize in here because it is used outside the code stream
	if (classFileOffset + 3 >= bCodeStream.length) {
		resizeByteArray();
	}
	position += 4;
	bCodeStream[classFileOffset++] = (byte) ((value & 0xFF000000) >> 24);
	bCodeStream[classFileOffset++] = (byte) ((value & 0xFF0000) >> 16);
	bCodeStream[classFileOffset++] = (byte) ((value & 0xFF00) >> 8);
	bCodeStream[classFileOffset++] = (byte) (value & 0xFF);
}
public final void writeSignedWord(int pos, int value) {
	int currentOffset = startingClassFileOffset + pos;
	if (currentOffset + 4 >= bCodeStream.length) {
		resizeByteArray();
	}
	bCodeStream[currentOffset++] = (byte) ((value & 0xFF000000) >> 24);
	bCodeStream[currentOffset++] = (byte) ((value & 0xFF0000) >> 16);
	bCodeStream[currentOffset++] = (byte) ((value & 0xFF00) >> 8);
	bCodeStream[currentOffset++] = (byte) (value & 0xFF);
}
/**
 * Write a unsigned 16 bits value into the byte array
 * @param value the unsigned short
 */
protected final void writeUnsignedShort(int value) {
	position += 2;
	bCodeStream[classFileOffset++] = (byte) (value >>> 8);
	bCodeStream[classFileOffset++] = (byte) value;
}
/*
 * Wide conditional branch compare, improved by swapping comparison opcode
 *   ifeq WideTarget
 * becomes
 *    ifne Intermediate
 *    gotow WideTarget
 *    Intermediate:
 */
public void generateWideRevertedConditionalBranch(byte revertedOpcode, Label wideTarget) {
		Label intermediate = new Label(this);
		if (classFileOffset >= bCodeStream.length) {
			resizeByteArray();
		}
		position++;
		bCodeStream[classFileOffset++] = revertedOpcode;
		intermediate.branch();
		this.goto_w(wideTarget);
		intermediate.place();
}
}
