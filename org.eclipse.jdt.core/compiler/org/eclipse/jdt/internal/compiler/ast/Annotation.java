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

/**
 * Node representing a structured Javadoc annotation comment
 */
public class Annotation extends AstNode {

	public Argument[] parameters; 						// @param
	public TypeReference[] thrownExceptions; 	// @throws, @exception
	public TypeReference returnType;					// @return
	public Reference[] references; 						// @see
	
	public Annotation(int sourceStart, int sourceEnd) {
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
		
	/* 
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output).append("/**\n"); //$NON-NLS-1$
		if (this.parameters != null) {
			for (int i = 0, length = this.parameters.length; i < length; i++) {
				printIndent(indent+1, output).append(" * @param "); //$NON-NLS-1$		
				this.parameters[i].print(indent, output).append('\n');
			}
		}
		if (this.returnType != null) {
			printIndent(indent+1, output).append(" * @return "); //$NON-NLS-1$		
			this.returnType.print(indent, output).append('\n');
		}
		if (this.thrownExceptions != null) {
			for (int i = 0, length = this.thrownExceptions.length; i < length; i++) {
				printIndent(indent+1, output).append(" * @throws "); //$NON-NLS-1$		
				this.thrownExceptions[i].print(indent, output).append('\n');
			}
		}
		if (this.references != null) {
			for (int i = 0, length = this.references.length; i < length; i++) {
				printIndent(indent+1, output).append(" * @see"); //$NON-NLS-1$		
				this.references[i].print(indent, output).append('\n');
			}
		}
		printIndent(indent, output).append(" */\n"); //$NON-NLS-1$
		return output;
	}
}
