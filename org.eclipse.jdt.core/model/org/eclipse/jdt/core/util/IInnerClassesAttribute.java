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
 * Description of a inner class attribute as described in the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface IInnerClassesAttribute extends IClassFileAttribute {
	
	/**
	 * Answer back the number of inner classes infos as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getNumberOfClasses();

	/**
	 * Answer back the array of inner attribute entries as specified in
	 * the JVM specifications, or an empty array if none.
	 * 
	 * @return org.eclipse.jdt.core.util.IInnerClassesAttributeEntry[]
	 */
	IInnerClassesAttributeEntry[] getInnerClassAttributesEntries();
}
