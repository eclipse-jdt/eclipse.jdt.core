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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

/**
 * MemberValuePair node
 */
public class MemberValuePair extends ASTNode {
	
	public char[] token;
	public Expression value;
	
	public MemberValuePair(char[] token, int sourceStart, int sourceEnd, Expression value) {
		this.token = token;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		output
			.append(token)
			.append(" = "); //$NON-NLS-1$
		value.print(indent, output);
		return output;
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.value != null) {
				this.value.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.value != null) {
				this.value.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.value != null) {
				this.value.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
