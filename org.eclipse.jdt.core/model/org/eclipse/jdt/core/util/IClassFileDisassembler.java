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

/**
 * This interface is intented to be implemented to disassemble
 * IClassFileReader onto a String using the proper line separator.
 * 
 * @since 2.0
 */
public interface IClassFileDisassembler {

	/**
	 * Answers back the disassembled string of the IClassFileReader.
	 * This is an output quite similar to the javap tool.
	 * 
	 * @param classFileReader The classFileReader to be disassembled
	 * @param lineSeparator the line separator to use.
	 */
	String disassemble(IClassFileReader classFileReader, String lineSeparator);
}
