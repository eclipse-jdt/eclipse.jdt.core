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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ImportReference extends ASTNode {

	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public boolean onDemand = true; //most of the time
	public int declarationEnd; // doesn't include an potential trailing comment
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public boolean used;
	public int modifiers; // 1.5 addition for static imports
	public Annotation[] annotations;

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
	
	public boolean isStatic() {
		return (this.modifiers & AccStatic) != 0;
	}

	/**
	 * @return char[][]
	 */
	public char[][] getImportName() {

		return tokens;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		return print(indent, output, true);
	}

	public StringBuffer print(int tab, StringBuffer output, boolean withOnDemand) {

		/* when withOnDemand is false, only the name is printed */
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(tokens[i]);
		}
		if (withOnDemand && onDemand) {
			output.append(".*"); //$NON-NLS-1$
		}
		return output;
	}

	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {

		visitor.visit(this, scope);
		if (this.annotations != null) {
			int annotationsLength = this.annotations.length;
			for (int i = 0; i < annotationsLength; i++)
				this.annotations[i].traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
