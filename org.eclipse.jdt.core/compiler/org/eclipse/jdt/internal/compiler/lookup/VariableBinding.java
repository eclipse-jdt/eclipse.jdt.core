/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.impl.Constant;

public abstract class VariableBinding extends Binding {
	public int modifiers;
	public TypeBinding type;
	public char[] name;
	public Constant constant;
	public int id; // for flow-analysis (position in flowInfo bit vector)

public boolean isConstantValue() {
	return constant != Constant.NotAConstant;
}

public final boolean isBlankFinal(){
	return (modifiers & AccBlankFinal) != 0;
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
	String s = (type != null) ? type.debugName() : "UNDEFINED TYPE"; //$NON-NLS-1$
	s += " "; //$NON-NLS-1$
	s += (name != null) ? new String(name) : "UNNAMED FIELD"; //$NON-NLS-1$
	return s;
}
}
