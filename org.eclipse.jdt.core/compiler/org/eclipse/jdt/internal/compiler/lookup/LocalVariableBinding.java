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

import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class LocalVariableBinding extends VariableBinding {

	public boolean isArgument;
	public int resolvedPosition; // for code generation (position in method context)
	
	public static final int UNUSED = 0;
	public static final int USED = 1;
	public static final int FAKE_USED = 2;
	public int useFlag; // for flow analysis (default is UNUSED)
	
	public BlockScope declaringScope; // back-pointer to its declaring scope
	public LocalDeclaration declaration; // for source-positions

	public int[] initializationPCs;
	public int initializationCount = 0;

	// for synthetic local variables	
	// if declaration slot is not positionned, the variable will not be listed in attribute
	// note that the name of a variable should be chosen so as not to conflict with user ones (usually starting with a space char is all needed)
	public LocalVariableBinding(char[] name, TypeBinding type, int modifiers, boolean isArgument) {
		super(name, type, modifiers, isArgument ? Constant.NotAConstant : null);
		this.isArgument = isArgument;
	}
	
	// regular local variable or argument
	public LocalVariableBinding(LocalDeclaration declaration, TypeBinding type, int modifiers, boolean isArgument) {

		this(declaration.name, type, modifiers, isArgument);
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

		if (initializationPCs == null) 	return;
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
		switch (useFlag){
			case USED:
				s += "[pos: " + String.valueOf(resolvedPosition) + "]"; //$NON-NLS-2$ //$NON-NLS-1$
				break;
			case UNUSED:
				s += "[pos: unused]"; //$NON-NLS-1$
				break;
			case FAKE_USED:
				s += "[pos: fake_used]"; //$NON-NLS-1$
				break;
		}
		s += "[id:" + String.valueOf(id) + "]"; //$NON-NLS-2$ //$NON-NLS-1$
		if (initializationCount > 0) {
			s += "[pc: "; //$NON-NLS-1$
			for (int i = 0; i < initializationCount; i++) {
				if (i > 0)
					s += ", "; //$NON-NLS-1$
				s += String.valueOf(initializationPCs[i << 1]) + "-" + ((initializationPCs[(i << 1) + 1] == -1) ? "?" : String.valueOf(initializationPCs[(i<< 1) + 1])); //$NON-NLS-2$ //$NON-NLS-1$
			}
			s += "]"; //$NON-NLS-1$
		}
		return s;
	}
}
