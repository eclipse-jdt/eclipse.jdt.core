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
	 * This value should be used to read completely each part of a .class file.
	 */
	int ALL 					= 0xFFFF;
	
	/**
	 * This value should be used to read only the constant pool entries of a .class file.
	 */
	int CONSTANT_POOL 			= 0x0001;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the method infos of a .class file.
	 */
	int METHOD_INFOS 			= 0x0003;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the field infos of a .class file.
	 */
	int FIELD_INFOS 			= 0x0005;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the super interface names of a .class file.
	 */
	int SUPER_INTERFACES 		= 0x0009;

	/**
	 * This value should be used to read the constant pool entries and 
	 * the attributes of a .class file.
	 */
	int CLASSFILE_ATTRIBUTES 	= 0x0011;

	/**
	 * Answers back the disassembled string of the IClassFileReader.
	 * This is an output quite similar to the javap tool.
	 * 
	 * @param classFileReader The classFileReader to be disassembled
	 * @param lineSeparator the line separator to use.
	 */
	String disassemble(IClassFileReader classFileReader, String lineSeparator);
}
