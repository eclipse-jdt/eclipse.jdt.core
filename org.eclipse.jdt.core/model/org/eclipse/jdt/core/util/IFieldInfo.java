/**********************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *********************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a field info as described in the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface IFieldInfo {

	/**
	 * Answer back the constant value attribute of this field info if specified, 
	 * null otherwise.
	 * 
	 * @return org.eclipse.jdt.core.util.IConstantValueAttribute
	 */
	IConstantValueAttribute getConstantValueAttribute();

	/**
	 * Answer back the access flag of this field info.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAccessFlags();
	
	/**
	 * Answer back the name of this field info. The name is returned as
	 * specified in the JVM specifications.
	 * 
	 * @return char[]
	 */
	char[] getName();

	/**
	 * Answer back the name index of this field info.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNameIndex();
	
	/**
	 * Answer back the descriptor of this field info. The descriptor is returned as
	 * specified in the JVM specifications.
	 * 
	 * @return char[]
	 */
	char[] getDescriptor();

	/**
	 * Answer back the descriptor index of this field info.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getDescriptorIndex();

	/**
	 * Return true if the field info has a constant value attribute, false otherwise.
	 * 
	 * @return boolean
	 */
	boolean hasConstantValueAttribute();

	/**
	 * Return true if the field info has a synthetic attribute, false otherwise.
	 * 
	 * @return boolean
	 */
	boolean isSynthetic();

	/**
	 * Return true if the field info has a deprecated attribute, false otherwise.
	 * 
	 * @return boolean
	 */
	boolean isDeprecated();
	
	/**
	 * Answer back the attribute number of the field info.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAttributeCount();

	
	/**
	 * Answer back the collection of all attributes of the field info. It 
	 * includes SyntheticAttribute, ConstantValueAttributes, etc.
	 * Returns an empty collection if none.
	 * 
	 * @return IClassFileAttribute[]
	 */
	IClassFileAttribute[] getAttributes();
}