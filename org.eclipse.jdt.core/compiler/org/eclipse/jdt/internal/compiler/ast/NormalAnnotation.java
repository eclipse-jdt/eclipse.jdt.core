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
 * Normal annotation node
 */
public class NormalAnnotation extends Annotation {
	
	public MemberValuePair[] memberValuePairs;
	
	public NormalAnnotation(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
	}

	public NormalAnnotation(char[] token, long sourcePosition) {
		this.tokens = new char[][] { token };
		this.sourcePositions = new long[] { sourcePosition };
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
}
