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

/**
 * SingleMemberAnnotation node
 */
public class SingleMemberAnnotation extends Annotation {
	public Expression memberValue;
	
	public SingleMemberAnnotation(char[][] tokens, long[] sourcePositions, int sourceStart) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceStart = sourceStart;
		this.sourceEnd = (int) sourcePositions[sourcePositions.length - 1];
	}
	
	public SingleMemberAnnotation(char[] token, long sourcePosition, int sourceStart) {
		this.tokens = new char[][] { token };
		this.sourcePositions = new long[] { sourcePosition };
		this.sourceStart = sourceStart;
		this.sourceEnd = (int) sourcePosition;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		super.printExpression(indent, output);
		output.append('(');
		this.memberValue.printExpression(indent, output);
		return output.append(')');
	}	
}
