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

public interface IConstantPool {

	/**
	 * Answer back the number of entries in the constant pool.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getConstantPoolCount();

	/**
	 * Answer back the type of the entry at the index @index
	 * in the constant pool.
	 * 
	 * @param index the index of the entry in the constant pool
	 * @return <CODE>int</CODE>
	 */
	int getEntryKind(int index);

	/**
	 * Answer back the entry at the index @index
	 * in the constant pool.
	 * 
	 * @param index the index of the entry in the constant pool
	 * @return org.eclipse.jdt.core.util.IConstantPoolEntry
	 */
	IConstantPoolEntry decodeEntry(int index);
}
