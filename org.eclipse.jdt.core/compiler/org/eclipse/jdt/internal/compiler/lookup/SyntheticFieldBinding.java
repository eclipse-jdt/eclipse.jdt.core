package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;

public class SyntheticFieldBinding extends FieldBinding {
	public int index;
public SyntheticFieldBinding(char[] name, TypeBinding type, int modifiers, ReferenceBinding declaringClass, Constant constant, int index) {
	super(name, type, modifiers, declaringClass, constant);
	this.index = index;
}
}
