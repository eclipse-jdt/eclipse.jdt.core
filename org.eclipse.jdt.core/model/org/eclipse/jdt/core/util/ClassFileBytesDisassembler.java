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
package org.eclipse.jdt.core.util;

/**
 * This class is intended to be subclassed to disassemble
 * classfile bytes onto a String using the proper line separator.
 * 
 * @since 2.1
 */
public abstract class ClassFileBytesDisassembler {
	
	/**
	 * The mode is the detailed mode to disassemble IClassFileReader. It returns the magic
	 * numbers, the version numbers and field and method descriptors.
	 */
	public final static int DETAILED = 1;
	
	/**
	 * The mode is the default mode to disassemble IClassFileReader.
	 */
	public final static  int DEFAULT  = 2;

	/**
	 * Answers back the disassembled string of the classfile bytes using the default
	 * mode.
	 * This is an output quite similar to the javap tool, using DEFAULT mode.
	 * 
	 * @param classFileBytes The bytes of the classfile
	 * @param lineSeparator the line separator to use.
	 * 
	 * @return the disassembled string of the IClassFileReader using the default mode.
	 * @exception ClassFormatException if the classfile bytes are ill-formed
	 */
	public abstract String disassemble(byte[] classFileBytes, String lineSeparator) throws ClassFormatException;

	/**
	 * Answers back the disassembled string of the classfile bytes according to the
	 * mode.
	 * This is an output quite similar to the javap tool.
	 * 
	 * @param classFileBytes The bytes of the classfile
	 * @param lineSeparator the line separator to use.
	 * @param mode the mode used to disassemble the IClassFileReader
	 * 
	 * @return the disassembled string of the IClassFileReader according to the mode
	 * @exception ClassFormatException if the classfile bytes are ill-formed
	 */
	public abstract String disassemble(byte[] classFileBytes, String lineSeparator, int mode)  throws ClassFormatException;

	/**
	 * Answers a readable short description of this disassembler
	 * 
	 * @return String - a string description of the disassembler
	 */	
    public abstract String getDescription();
}
