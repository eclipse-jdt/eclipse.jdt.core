/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * Description of a stack map frame entry as described in the JVM specifications.
 * 
 * 
 * This interface may be implemented by clients. 
 * 
 * @since 3.0
 */
public interface IStackMapFrame {
	/**
	 * Answer back the offset as specified in
	 * the JVM specifications.
	 * 
	 * @return the offset
	 */
	int getOffset();
	
	/**
	 * Answer back the number of locals as specified in
	 * the JVM specifications.
	 * 
	 * @return the number of locals
	 */
	int getNumberOfLocals();
	
	/**
	 * Answer back the number of stack items as specified in
	 * the JVM specifications.
	 * 
	 * @return the number of stack items
	 */
	int getNumberOfStackItems();
	
	/**
	 * Answer back the verification type infos for the locals as specified in
	 * the JVM specifications.
	 * Answer back an empty array if none.
	 * 
	 * @return the verification type infos for the locals.
	 */
	IVerificationTypeInfo[] getLocals();
	
	/**
	 * Answer back the verification type infos for the stack items as specified in
	 * the JVM specifications.
	 * Answer back an empty array if none.
	 * 
	 * @return the verification type infos for the stack items.
	 */
	IVerificationTypeInfo[] getStackItems();
}
