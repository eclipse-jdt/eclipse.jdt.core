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

import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public abstract class AbstractVariableDeclaration extends Statement {
	public int modifiers;

	public TypeReference type;
	public Expression initialization;

	public char[] name;
	public int declarationEnd;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiersSourceStart;
	
	public AbstractVariableDeclaration() {}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}

	public abstract String name();

	public void resolve(BlockScope scope) {}
		
	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		s += type.toString(0) + " " + new String(name()); //$NON-NLS-1$
		if (initialization != null)
			s += " = " + initialization.toStringExpression(tab); //$NON-NLS-1$
		return s;
	}
}
