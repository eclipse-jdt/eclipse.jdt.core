/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/
package org.eclipse.jdt.core.util;

import java.io.IOException;
import java.io.Writer;

/**
 * Description of a code attribute as described in the JVM specifications.
 *  
 * @since 2.0
 */
public interface ICodeAttribute extends IClassFileAttribute {
	/**
	 * Answer back the max locals value of the code attribute.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getMaxLocals();

	/**
	 * Answer back the max stack value of the code attribute.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getMaxStack();

	/**
	 * Answer back the line number attribute, if it exists, null otherwise.
	 * 
	 * @return org.eclipse.jdt.core.util.ILineNumberAttribute
	 */
	ILineNumberAttribute getLineNumberAttribute();

	/**
	 * Answer back the local variable attribute, if it exists, null otherwise.
	 * 
	 * @return org.eclipse.jdt.core.util.ILocalVariableAttribute
	 */
	ILocalVariableAttribute getLocalVariableAttribute();

	/**
	 * Answer back the array of exception entries, if they are present.
	 * An empty array otherwise.
	 * 
	 * @return org.eclipse.jdt.core.util.IExceptionTableEntry
	 */
	IExceptionTableEntry[] getExceptionTable();
	
	/**
	 * Answer back the array of bytes, which represents all the opcodes as described
	 * in the JVM specifications.
	 * 
	 * @return byte[]
	 */
	byte[] getBytecodes();

	/**
	 * Answer back the length of the bytecode contents.
	 * 
	 * @return <CODE>long</CODE>
	 */
	long getCodeLength();
	
	/**
	 * Answer back the attribute number of the code attribute.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAttributesCount();

	/**
	 * Answer back the collection of all attributes of the field info. It 
	 * includes the LineNumberAttribute and the LocalVariableTableAttribute.
	 * Returns an empty collection if none.
	 * 
	 * @return IClassFileAttribute[]
	 */
	IClassFileAttribute[] getAttributes();

	/**
	 * Answer back the exception table length of the code attribute.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getExceptionTableLength();
	
	/**
	 * Define a Java opcodes walker. All actions are defined in the visitor.
	 * @param writer The writer used to generate the disassemble output
	 * @param lineSeparator The line separator used to put each opcode on its own line
	 * @param tabNumber the number of indentation (SPACE or TAB)
	 * @param visitor The visitor to use to walk the opcodes.
	 * 
	 * @exception ClassFormatException Exception thrown if the opcodes contain invalid bytes
	 */
	void traverse(IBytecodeVisitor visitor) throws ClassFormatException;
}