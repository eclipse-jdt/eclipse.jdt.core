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
 * MemberValueArrayInitializer node
 */
public class MemberValueArrayInitializer extends Expression {
	
	public Expression[] memberValues;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append('{');
		if (this.memberValues != null) { 	
			int j = 2 ; 
			for (int i = 0 ; i < this.memberValues.length ; i++) {	
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.memberValues[i].printExpression(0, output);
				j -- ;
				if (j == 0) {
					output.append('\n');
					printIndent(indent+1, output);
					j = 2;
				}
			}
		}
		return output.append('}');
	}
}
