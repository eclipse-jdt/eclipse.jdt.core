package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class AbstractVariableDeclaration extends Statement {
	public int modifiers;
	
	public TypeReference type;
	public Expression initialization;

	public char[] name;

	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiersSourceStart;
public AbstractVariableDeclaration(){}
public String name(){

	return "anonymous storage box"; }
public String toString(int tab) {
	/*slow code*/

	String s = tabString(tab);
	if (modifiers != AccDefault) {
		s += modifiersString(modifiers);
	}
	s += type.toString(0) + " " + new String(name());
	if (initialization != null)
		s += " = " + initialization.toStringExpression(tab);
	return s;
}
}
