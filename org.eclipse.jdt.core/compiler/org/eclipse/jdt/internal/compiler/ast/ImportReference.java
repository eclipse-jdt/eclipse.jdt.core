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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ImportReference extends AstNode {

	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public boolean onDemand = true; //most of the time
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public boolean used;
	public int modifiers; // 1.5 addition for static imports

	public ImportReference(
			char[][] tokens,
			long[] sourcePositions,
			boolean onDemand,
			int modifiers) {

		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.onDemand = onDemand;
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length-1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
		this.modifiers = modifiers;
	}

	/**
	 * @return char[][]
	 */
	public char[][] getImportName() {

		return tokens;
	}

	public String toString(int tab) {

		return toString(tab, true);
	}

	public String toString(int tab, boolean withOnDemand) {

		/* when withOnDemand is false, only the name is printed */
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			buffer.append(tokens[i]);
			if (i < (tokens.length - 1)) {
				buffer.append("."); //$NON-NLS-1$
			}
		}
		if (withOnDemand && onDemand) {
			buffer.append(".*"); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	public void traverse(
			IAbstractSyntaxTreeVisitor visitor,
			CompilationUnitScope scope) {

		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
