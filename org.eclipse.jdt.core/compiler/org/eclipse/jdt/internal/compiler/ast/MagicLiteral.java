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

public abstract class  MagicLiteral extends Literal {
public MagicLiteral(int s , int e) {
	super(s,e);
}
public boolean isValidJavaStatement(){
	//should never be reach, but with a bug in the ast tree....
	//see comment on the Statement class
	
	return false ;}
/**
 * source method comment.
 */
public char[] source() {
	return null;
}
public String toStringExpression(){

	return  new String(source()) ; }
}
