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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;

/**
 * Enum constant node
 */
public class EnumConstant extends TypeDeclaration {
	
	public Expression[] arguments ;

	public EnumConstant(CompilationResult compilationResult){
		super(compilationResult);
		this.compilationResult = compilationResult;
	}

	public StringBuffer print(int indent, StringBuffer output) {
		output.append(name);
		if (arguments != null) {
			output.append('(');
			int length = arguments.length;
			for (int i = 0; i < length - 1; i++) {
				arguments[i].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			arguments[length - 1].print(0, output);
			output.append(')');
		}
		printBody(indent, output);
		return output;
	}
}
