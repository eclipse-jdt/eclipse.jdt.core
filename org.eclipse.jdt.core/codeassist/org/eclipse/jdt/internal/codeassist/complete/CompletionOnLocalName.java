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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;


public class CompletionOnLocalName extends LocalDeclaration {
	private static final char[] FAKENAMESUFFIX = " ".toCharArray(); //$NON-NLS-1$
	public char[] realName;
	public CompletionOnLocalName(Expression expr,char[] name, int sourceStart, int sourceEnd){
		super(expr, CharOperation.concat(name, FAKENAMESUFFIX), sourceStart, sourceEnd);
		this.realName = name;
	}
	
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		
		throw new CompletionNodeFound(this, scope);
	}
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<CompleteOnLocalName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(realName);
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s += ">"; //$NON-NLS-1$
		return s;
	}	
}

