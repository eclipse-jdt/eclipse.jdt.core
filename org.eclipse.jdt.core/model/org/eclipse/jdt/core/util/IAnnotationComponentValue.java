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
 * Description of an annotation component value as described in the JVM specifications.
 * 
 * This interface may be implemented by clients. 
 *  
 * @since 3.0
 */
public interface IAnnotationComponentValue {
	/**
	 * Answer back the tag as described in the JVM specifications.
	 * 
	 * @return the tag
	 */
	int getTag();

	/**
	 * Answer back the constant value index as described in the JVM specifications.
	 * 
	 * @return the constant value index
	 */
	int getConstantValueIndex();

	/**
	 * Answer back the constant value as described in the JVM specifications.
	 * 
	 * @return the constant value
	 */
	IConstantPoolEntry getConstantValue();
	
	/**
	 * Answer back the enum constant index as described in the JVM specifications.
	 * 
	 * @return the enum constant index
	 */
	int getEnumConstantIndex();

	/**
	 * Answer back the enum constant as described in the JVM specifications.
	 * 
	 * @return the enum constant
	 */
	IConstantPoolEntry getEnumConstant();	

	/**
	 * Answer back the class info index as described in the JVM specifications.
	 * 
	 * @return the class info index
	 */
	int getClassInfoIndex();

	/**
	 * Answer back the class info as described in the JVM specifications.
	 * 
	 * @return the class info
	 */
	IConstantPoolEntry getClassInfo();	
}
