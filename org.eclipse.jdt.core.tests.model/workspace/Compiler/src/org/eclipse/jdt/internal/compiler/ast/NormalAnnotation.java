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
 * Normal annotation node
 */
public class NormalAnnotation extends Annotation {
	
	public MemberValuePair[] memberValuePairs;
	
	public NormalAnnotation(char[][] tokens, long[] sourcePositions, int sourceStart) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceStart = sourceStart;
		this.sourceEnd = (int) sourcePositions[sourcePositions.length - 1];
	}

	public NormalAnnotation(char[] token, long sourcePosition, int sourceStart) {
		this.tokens = new char[][] { token };
		this.sourcePositions = new long[] { sourcePosition };
		this.sourceStart = sourceStart;
		this.sourceEnd = (int) sourcePosition;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		super.printExpression(indent, output);
		output.append('(');
		if (this.memberValuePairs != null) {
			for (int i = 0, max = this.memberValuePairs.length; i < max; i++) {
				if (i > 0) {
					output.append(',');
				}
				this.memberValuePairs[i].print(indent, output);
			}
		}
		output.append(')');
		return output;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.memberValuePairs != null) {
				int memberValuePairsLength = this.memberValuePairs.length;
				for (int i = 0; i < memberValuePairsLength; i++)
					this.memberValuePairs[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.memberValuePairs != null) {
				int memberValuePairsLength = this.memberValuePairs.length;
				for (int i = 0; i < memberValuePairsLength; i++)
					this.memberValuePairs[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.memberValuePairs != null) {
				int memberValuePairsLength = this.memberValuePairs.length;
				for (int i = 0; i < memberValuePairsLength; i++)
					this.memberValuePairs[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
