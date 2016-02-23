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

import org.eclipse.jdt.internal.compiler.ast.ExportReference;
/*
 * Completion node build by the parser in any case it was intending to
 * reduce an exports reference containing the cursor location.
 * e.g.
 *
 *	module myModule {
 *  exports packageo[cursor];
 *  }
 *
 *	module myModule {
 *	---> <CompleteOnExport:packageo>
 *  }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

public class CompletionOnExportReference extends ExportReference {

	public CompletionOnExportReference(char[] ident, long pos) {
		this(new char[][]{ident}, new long[]{pos});
	}
	public CompletionOnExportReference(char[][]  tokens, long[] positions) {
		super(tokens, positions);
	}
	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output).append("<CompleteOnExport:"); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output.append('>');
	}

}
