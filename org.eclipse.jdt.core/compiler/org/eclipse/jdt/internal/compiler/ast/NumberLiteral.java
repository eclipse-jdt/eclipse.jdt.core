package org.eclipse.jdt.internal.compiler.ast;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public abstract class NumberLiteral extends Literal {
	char[] source;
public NumberLiteral(char[] token, int s, int e) {
	this(s,e) ;
	source = token ;
}
public NumberLiteral(int s, int e) {
	super (s,e) ;
}
public boolean isValidJavaStatement(){
	//should never be reach, but with a bug in the ast tree....
	//see comment on the Statement class
	
	return false ;}
public char[] source(){
	return source;}
public String toStringExpression(){

	return new String(source);}
}
