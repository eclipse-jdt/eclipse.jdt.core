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
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Specific local variable location used to:
 * - either provide emulation for outer local variables used from within innerclass constructs,
 * - or provide emulation to enclosing instances. 
 * When it is mapping to an outer local variable, this actual outer local is accessible through 
 * the public field #actualOuterLocalVariable.
 *
 * Such a synthetic argument binding will be inserted in all constructors of local innertypes before
 * the user arguments.
 */

import org.eclipse.jdt.core.compiler.CharOperation;

public class SyntheticArgumentBinding extends LocalVariableBinding {

	{	
		this.isArgument = true;
		this.useFlag = USED;
	}
	
	// if the argument is mapping to an outer local variable, this denotes the outer actual variable
	public LocalVariableBinding actualOuterLocalVariable;
	// if the argument has a matching synthetic field
	public FieldBinding matchingField;

	final static char[] OuterLocalPrefix = { 'v', 'a', 'l', '$' };
	final static char[] EnclosingInstancePrefix = { 't', 'h', 'i', 's', '$' };
	
	public SyntheticArgumentBinding(LocalVariableBinding actualOuterLocalVariable) {

		super(
			CharOperation.concat(OuterLocalPrefix, actualOuterLocalVariable.name), 
			actualOuterLocalVariable.type, 
			AccFinal,
			true);
		this.actualOuterLocalVariable = actualOuterLocalVariable;
	}

	public SyntheticArgumentBinding(ReferenceBinding enclosingType) {

		super(
			CharOperation.concat(
				SyntheticArgumentBinding.EnclosingInstancePrefix,
				String.valueOf(enclosingType.depth()).toCharArray()),
			enclosingType, 
			AccFinal,
			true);
	}
}
