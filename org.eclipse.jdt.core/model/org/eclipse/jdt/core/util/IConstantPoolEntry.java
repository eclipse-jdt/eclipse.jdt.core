/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.util;

public interface IConstantPoolEntry {

	/**
	 * Answer back the type of this entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getKind();

	/**
	 * Answer back the name index for a CONSTANT_Class type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getClassInfoNameIndex();

	/**
	 * Answer back the class index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getClassIndex();

	/**
	 * Answer back the nameAndType index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNameAndTypeIndex();
	
	/**
	 * Answer back the string index for a CONSTANT_String type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getStringIndex();

	/**
	 * Answer back the string value for a CONSTANT_String type entry.
	 * 
	 * @return String
	 */
	String getStringValue();
	
	/**
	 * Answer back the integer value for a CONSTANT_Integer type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getIntegerValue();

	/**
	 * Answer back the float value for a CONSTANT_Float type entry.
	 * 
	 * @return <CODE>float</CODE>
	 */
	float getFloatValue();

	/**
	 * Answer back the double value for a CONSTANT_Double type entry.
	 * 
	 * @return <CODE>double</CODE>
	 */
	double getDoubleValue();

	/**
	 * Answer back the long value for a CONSTANT_Long type entry.
	 * 
	 * @return <CODE>long</CODE>
	 */
	long getLongValue();
	
	/**
	 * Answer back the descriptor index for a CONSTANT_NameAndType type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNameAndTypeInfoDescriptorIndex();

	/**
	 * Answer back the name index for a CONSTANT_NameAndType type entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNameAndTypeInfoNameIndex();

	/**
	 * Answer back the class name for a CONSTANT_Class type entry.
	 * 
	 * @return char[]
	 */
	char[] getClassInfoName();

	/**
	 * Answer back the class name for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * 
	 * @return char[]
	 */
	char[] getClassName();

	/**
	 * Answer back the field name for a CONSTANT_Fieldref type entry.
	 * 
	 * @return char[]
	 */
	char[] getFieldName();
	
	/**
	 * Answer back the field name for a CONSTANT_Methodref or CONSTANT_InterfaceMethodred
	 * type entry.
	 * 
	 * @return char[]
	 */
	char[] getMethodName();

	/**
	 * Answer back the field descriptor value for a CONSTANT_Fieldref type entry. This value
	 * is set only when decoding the CONSTANT_Fieldref entry. 
	 * 
	 * @return char[]
	 */
	char[] getFieldDescriptor();

	/**
	 * Answer back the method descriptor value for a CONSTANT_Methodref or
	 * CONSTANT_InterfaceMethodref type entry. This value is set only when decoding the 
	 * CONSTANT_Methodref or CONSTANT_InterfaceMethodref entry. 
	 * 
	 * @return char[]
	 */
	char[] getMethodDescriptor();
	
	/**
	 * Answer back the utf8 value for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry.
	 * 
	 * @return char[]
	 */
	char[] getUtf8Value();
	
	/**
	 * Answer back the utf8 length for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getUtf8Length();
}
