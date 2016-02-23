/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ModuleReference;

public class CompletionOnModuleReference extends ModuleReference implements CompletionOnKeyword {

	public CompletionOnModuleReference(char[] ident, long pos) {
		this(new char[][]{ident}, new long[]{pos});
	}
	public CompletionOnModuleReference(char[][] tokens, long[] sourcePositions) {
		super(tokens, sourcePositions);
	}
	@Override
	public char[] getToken() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public char[][] getPossibleKeywords() {
		// TODO Auto-generated method stub
		return null;
	}
	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output).append("<CompleteOnModuleReference:"); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output.append('>');
	}

}
