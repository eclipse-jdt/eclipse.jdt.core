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
package org.eclipse.jdt.core;

public abstract class CodeFormatter {

	/**
	 * Kind used to format an expression
	 */
	public static final int K_EXPRESSION = 0x01;
	
	/**
	 * Kind used to format a set of statements
	 */
	public static final int K_STATEMENTS = 0x02;
	
	/**
	 * Kind used to format a set of class body declarations
	 */
	public static final int K_CLASS_BODY_DECLARATIONS = 0x04;
	
	/**
	 * Kind used to format a compilation unit
	 */
	public static final int K_COMPILATION_UNIT = 0x08;

	/** 
	 * Formats the String <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * 
	 * @param string the string to format
	 * @param indentationLevel the initial indentation level, used 
	 *      to shift left/right the entire source fragment. An initial indentation
	 *      level of zero has no effect.
	 * @param positions an array of positions to map. These are
	 *      character-based source positions inside the original source,
	 * 		arranged in non-decreasing order, for which corresponding positions in 
	 *     the formatted source will be computed (so as to relocate elements associated 
	 *     with the original source). It updates the positions array with updated 
	 *     positions. If set to <code>null</code>, then no positions are mapped.
	 * @param lineSeparator the line separator to use in formatted source,
	 *     if set to <code>null</code>, then the platform default one will be used.
	 * @param kind Use to specify the kind of the code snippet to format. It can be any of these:
	 * 		  K_EXPRESSION, K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_COMPILATION_UNIT
	 * @return the formatted output string
	 */
	public abstract String format(String string, int indentationLevel, int[] positions, String lineSeparator, int kind);
}
