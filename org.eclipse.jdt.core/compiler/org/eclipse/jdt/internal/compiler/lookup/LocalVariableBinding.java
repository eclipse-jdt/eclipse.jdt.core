package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;

public class LocalVariableBinding extends VariableBinding {
	public boolean isArgument;

	public int resolvedPosition; // for code generation (position in method context)
	public boolean used; // for flow analysis
	public BlockScope declaringScope; // back-pointer to its declaring scope
	public LocalDeclaration declaration; // for source-positions

	public int[] initializationPCs;
	public int initializationCount = 0;
public LocalVariableBinding(char[] name, TypeBinding type, int modifiers) {
	this(name, type, modifiers, false);
}
public LocalVariableBinding(char[] name, TypeBinding type, int modifiers, boolean isArgument) {
	this.name = name;
	this.type = type;
	this.modifiers = modifiers;
	if (this.isArgument = isArgument)
		this.constant = Constant.NotAConstant;
}
public LocalVariableBinding(LocalDeclaration declaration, TypeBinding type, int modifiers) {
	this(declaration.name, type, modifiers, false);
	this.declaration = declaration;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return LOCAL;
}
// Answer whether the variable binding is a secret variable added for code gen purposes

public boolean isSecret() {
	return declaration == null && !isArgument;
}
public void recordInitializationEndPC(int pc) {
	if (initializationPCs[((initializationCount - 1) << 1) + 1] == -1)
		initializationPCs[((initializationCount - 1) << 1) + 1] = pc;
}
public void recordInitializationStartPC(int pc) {
	if (initializationPCs == null)
		return;
	// optimize cases where reopening a contiguous interval
	if ((initializationCount > 0) && (initializationPCs[ ((initializationCount - 1) << 1) + 1] == pc)) {
		initializationPCs[ ((initializationCount - 1) << 1) + 1] = -1; // reuse previous interval (its range will be augmented)
	} else {
		int index = initializationCount << 1;
		if (index == initializationPCs.length) {
			System.arraycopy(initializationPCs, 0, (initializationPCs = new int[initializationCount << 2]), 0, index);
		}
		initializationPCs[index] = pc;
		initializationPCs[index + 1] = -1;
		initializationCount++;
	}
}
public String toString() {
	String s = super.toString();
	if (!used)
		s += "[pos: unused]"/*nonNLS*/;
	else
		s += "[pos: "/*nonNLS*/ + String.valueOf(resolvedPosition) + "]"/*nonNLS*/;
	s += "[id:"/*nonNLS*/ + String.valueOf(id) + "]"/*nonNLS*/;
	if (initializationCount > 0) {
		s += "[pc: "/*nonNLS*/;
		for (int i = 0; i < initializationCount; i++) {
			if (i > 0)
				s += ", "/*nonNLS*/;
			s += String.valueOf(initializationPCs[i << 1]) + "-"/*nonNLS*/ + ((initializationPCs[(i << 1) + 1] == -1) ? "?"/*nonNLS*/ : String.valueOf(initializationPCs[(i<< 1) + 1]));
		}
		s += "]"/*nonNLS*/;
	}
	return s;
}
}
