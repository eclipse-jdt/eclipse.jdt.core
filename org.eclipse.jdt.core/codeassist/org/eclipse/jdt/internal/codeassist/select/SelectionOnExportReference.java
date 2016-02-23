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
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.ExportReference;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce an export reference containing the assist identifier.
 * e.g.
 *
 *	module myModule {
 *  exports packageo[cursor];
 *  }
 *
 *	module myModule {
 *	---> <SelectionOnExport:packageo>
 *  }
 *
 */
public class SelectionOnExportReference extends ExportReference {

	public SelectionOnExportReference(char[][] tokens, long[] sourcePositions) {
		super(tokens, sourcePositions);
		// TODO Auto-generated constructor stub
	}
	public StringBuffer print(int indent, StringBuffer output, boolean withOnDemand) {

		printIndent(indent, output).append("<SelectOnExport:"); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output.append('>');
	}
}
