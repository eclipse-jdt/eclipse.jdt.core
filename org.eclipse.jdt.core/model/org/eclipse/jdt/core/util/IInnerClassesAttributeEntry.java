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
 * Description of a inner class info as described in the the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface IInnerClassesAttributeEntry {
	
	/**
	 * Answer back the access flag of this inner classes attribute as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getAccessFlags();

	/**
	 * Answer back the inner name index of this inner classes attribute as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getInnerNameIndex();

	/**
	 * Answer back the outer class name index of this inner classes attribute as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getOuterClassNameIndex();

	/**
	 * Answer back the inner class name index of this inner classes attribute as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getInnerClassNameIndex();

	/**
	 * Answer back the inner name of this inner classes attribute as specified in
	 * the JVM specifications, null if inner name index is equals to zero.
	 * 
	 * @return char[]
	 */
	char[] getInnerName();

	/**
	 * Answer back the outer class name of this inner classes attribute as specified in
	 * the JVM specifications, null if outer class name index is equals to zero.
	 * 
	 * @return char[]
	 */
	char[] getOuterClassName();

	/**
	 * Answer back the inner class name of this inner classes attribute as specified in
	 * the JVM specifications, null if inner class name index is equals to zero.
	 * 
	 * @return char[]
	 */
	char[] getInnerClassName();

}