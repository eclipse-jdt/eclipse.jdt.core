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
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class SelectionOnLocalName extends LocalDeclaration{
	public SelectionOnLocalName(Expression expr, char[] name,	int sourceStart, int sourceEnd) {
		super(expr, name, sourceStart, sourceEnd);
	}
	
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		throw new SelectionNodeFound(binding);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<SelectionOnLocalName:"; //$NON-NLS-1$
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		s += type.toString(0) + " " + new String(name()); //$NON-NLS-1$
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s+= ">";//$NON-NLS-1$
		return s;	
	}
}
