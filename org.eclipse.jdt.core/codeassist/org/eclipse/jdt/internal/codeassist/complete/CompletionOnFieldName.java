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
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public class CompletionOnFieldName extends FieldDeclaration {
	private static final char[] FAKENAMESUFFIX = " ".toCharArray(); //$NON-NLS-1$
	public char[] realName;
	public CompletionOnFieldName(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
		super(initialization, CharOperation.concat(name, FAKENAMESUFFIX), sourceStart, sourceEnd); //$NON-NLS-1$
		this.realName = name;
	}
	
	public void resolve(MethodScope initializationScope) {
		super.resolve(initializationScope);
		
		throw new CompletionNodeFound(this, initializationScope);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<CompleteOnFieldName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(realName);
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s += ">"; //$NON-NLS-1$
		return s;
	}	
}

