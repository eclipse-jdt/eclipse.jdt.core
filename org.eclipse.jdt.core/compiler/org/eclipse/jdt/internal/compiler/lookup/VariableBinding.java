package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;

public abstract class VariableBinding extends Binding {
	public int modifiers;
	public TypeBinding type;
	public char[] name;
	public Constant constant;
	public int id; // for flow-analysis (position in flowInfo bit vector)
	public boolean isConstantValue() {
		return constant != Constant.NotAConstant;
	}

	/* Answer true if the receiver is final and cannot be changed
	*/

	public final boolean isFinal() {
		return (modifiers & AccFinal) != 0;
	}

	public char[] readableName() {
		return name;
	}

	public String toString() {
		String s = (type != null) ? type.debugName() : "UNDEFINED TYPE";
		s += " ";
		s += (name != null) ? new String(name) : "UNNAMED FIELD";
		return s;
	}

}
