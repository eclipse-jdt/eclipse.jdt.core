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
 * Description of a local variable table entry as specified in the JVM specifications.
 * 
 * @since 2.0
 */
public interface ILocalVariableTableEntry {
	
	/**
	 * Answer back the start pc of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getStartPC();

	/**
	 * Answer back the length of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getLength();

	/**
	 * Answer back the name index in the constant pool of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNameIndex();

	/**
	 * Answer back the descriptor index in the constant pool of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getDescriptorIndex();

	/**
	 * Answer back the index of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getIndex();
	
	/**
	 * Answer back the name of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return char[]
	 */
	char[] getName();

	/**
	 * Answer back the descriptor of this entry as specified in
	 * the JVM specifications.
	 * 
	 * @return char[]
	 */
	char[] getDescriptor();
}
