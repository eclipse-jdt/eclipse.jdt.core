package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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
		s+= ">";
		return s;	
	}
}
