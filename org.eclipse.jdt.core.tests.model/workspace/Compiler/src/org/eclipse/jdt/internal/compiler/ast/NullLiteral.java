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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class NullLiteral extends MagicLiteral {

	static final char[] source = {'n' , 'u' , 'l' , 'l'};

	public NullLiteral(int s , int e) {

		super(s,e);
	}

	public void computeConstant() {
	
		constant = NotAConstant; 
	}

	/**
	 * Code generation for the null literal
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */ 
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			codeStream.aconst_null();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	public TypeBinding literalType(BlockScope scope) {
		return NullBinding;
	}

	/**
	 * 
	 */
	public char[] source() {
		return source;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
