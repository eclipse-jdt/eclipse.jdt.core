package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class IntLiteralMinValue extends IntLiteral {

	final static char[] CharValue = new char[]{'-','2','1','4','7','4','8','3','6','4','8'};
	final static Constant MIN_VALUE = Constant.fromValue(Integer.MIN_VALUE) ; 

public IntLiteralMinValue() {
	super(CharValue,0,0,Integer.MIN_VALUE);
	constant = MIN_VALUE;
}
public void computeConstant(){
	
	/*precomputed at creation time*/ }
}
