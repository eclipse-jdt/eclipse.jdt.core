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
 * Description of a line number attribute as described in the JVM specifications.
 *  
 * @since 2.0
 */
public interface ILineNumberAttribute extends IClassFileAttribute {

	/**
	 * Answer back the line number table length as specified in
	 * the JVM specifications.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getLineNumberTableLength();

	/**
	 * Answer back the array of pairs (start pc, line number) as specified in the 
	 * JVM specifications.
	 * 
	 * @return int[][]
	 */
	int[][] getLineNumberTable();

}
