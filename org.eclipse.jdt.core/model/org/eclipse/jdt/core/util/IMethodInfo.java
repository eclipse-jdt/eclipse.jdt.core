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

/**
 * Description of a method info as described in the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface IMethodInfo {

	/**
	 * Answer back the method descriptor of this method info as specified
	 * in the JVM specifications.
	 * 
	 * @return char[]
	 */
	char[] getDescriptor();

	/**
	 * Answer back the descriptor index of this method info.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getDescriptorIndex();

	/**
	 * Answer back the access flags of this method info as specified
	 * in the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAccessFlags();

	/**
	 * Answer back the name of this method info as specified
	 * in the JVM specifications.
	 * 
	 * @return char[]
	 */
	char[] getName();

	/**
	 * Answer back the name index of this method info.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNameIndex();
	
	/**
	 * Answer true if this method info represents a &lt;clinit&gt; method,
	 * false otherwise.
	 * 
	 * @return boolean
	 */
	boolean isClinit();

	/**
	 * Answer true if this method info represents a constructor,
	 * false otherwise.
	 * 
	 * @return boolean
	 */
	boolean isConstructor();

	/**
	 * Answer true if this method info has a synthetic attribute,
	 * false otherwise.
	 * 
	 * @return boolean
	 */
	boolean isSynthetic();

	/**
	 * Answer true if this method info has a deprecated attribute,
	 * false otherwise.
	 * 
	 * @return boolean
	 */
	boolean isDeprecated();

	/**
	 * Answer the code attribute of this method info, null if none or if the decoding
	 * flag doesn't include METHOD_BODIES.
	 * 
	 * @return org.eclipse.jdt.core.util.ICodeAttribute
	 */
	ICodeAttribute getCodeAttribute();
	
	/**
	 * Answer the exception attribute of this method info, null is none.
	 * 
	 * 	@return org.eclipse.jdt.core.util.IExceptionAttribute
	 */
	IExceptionAttribute getExceptionAttribute();
	
	/**
	 * Answer back the attribute number of the method info. It includes the CodeAttribute
	 * if any even if the decoding flags doesn't include METHOD_BODIES.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAttributeCount();
	
	/**
	 * Answer back the collection of all attributes of the method info. It 
	 * includes SyntheticAttribute, CodeAttributes, etc. It doesn't include the
	 * CodeAttribute if the decoding flags doesn't include METHOD_BODIES.
	 * Returns an empty collection if none.
	 * 
	 * @return IClassFileAttribute[]
	 */
	IClassFileAttribute[] getAttributes();
}