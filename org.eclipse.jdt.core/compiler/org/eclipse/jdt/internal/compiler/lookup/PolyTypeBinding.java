/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Expression;

public class PolyTypeBinding extends TypeBinding {

	Expression expression;
	
	public PolyTypeBinding(Expression expression) {
		this.expression = expression;
	}
	
	public char[] constantPoolName() {
		throw new UnsupportedOperationException();  // should never reach code generation
	}

	public PackageBinding getPackage() {
		throw new UnsupportedOperationException();  // nobody should be asking this question.
	}

	public boolean isCompatibleWith(TypeBinding left, Scope scope) {
		return this.expression.isCompatibleWith(left, scope);
	}

	public char[] qualifiedSourceName() {
		return readableName();
	}

	public char[] sourceName() {
		return readableName();
	}

	public char[] readableName() {
		return this.expression.printExpression(0,  new StringBuffer()).toString().toCharArray();
	}
	
	public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t) {
		return this.expression.sIsMoreSpecific(s, t);
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer("PolyTypeBinding for: "); //$NON-NLS-1$
		return this.expression.printExpression(0,  buffer).toString();
	}
	
	public int kind() {
		return Binding.POLY_TYPE;
	}
}