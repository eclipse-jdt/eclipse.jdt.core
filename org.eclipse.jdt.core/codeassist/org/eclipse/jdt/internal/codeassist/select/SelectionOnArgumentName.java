package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnArgumentName extends Argument {
	public SelectionOnArgumentName(char[] name , long posNom , TypeReference tr , int modifiers){
		super(name, posNom, tr, modifiers);
	}
	
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		throw new SelectionNodeFound(binding);
	}
	
	public void bind(MethodScope scope, TypeBinding typeBinding, boolean used) {
		super.bind(scope, typeBinding, used);
		
		throw new SelectionNodeFound(binding);
	}
	
	public String toString(int tab) {
		String s = tabString(tab);
		s += "<SelectionOnArgumentName:"; //$NON-NLS-1$
		if (type != null) s += type.toString() + " "; //$NON-NLS-1$
		s += new String(name());
		if (initialization != null) s += " = " + initialization.toStringExpression(); //$NON-NLS-1$
		s += ">"; //$NON-NLS-1$
		return s;
	}
}
