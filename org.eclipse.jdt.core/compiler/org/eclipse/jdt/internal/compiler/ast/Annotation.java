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
 * Annotation
 */
public abstract class Annotation extends Expression {
	
	public char[][] tokens;
	public long[] sourcePositions;
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append('@');
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(tokens[i]);
		}
		return output;
	}
}
