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
 * (added in J2SE 1.5).
 * 
 * This interface may be implemented by clients. 
 *  
 * @since 3.1
 */
public interface IAnnotationComponentValue {
	
	/**
	 * Answer back the annotation component values as described in the JVM specifications.
	 * This is initialized only of the tag item is '['.
	 * 
	 * @return the annotation component values
	 */
	IAnnotationComponentValue[] getAnnotationComponentValues();
	
	/**
	 * Answer back the attribute value as described in the JVM specifications.
	 * This is initialized only of the tag item is '@'.
	 * 
	 * @return the attribute value
	 */
	IAnnotation getAttributeValue();

	/**
	 * Answer back the class info as described in the JVM specifications.
	 * This is initialized only of the tag item is 'c'.
	 * 
	 * @return the class info
	 */
	IConstantPoolEntry getClassInfo();

	/**
	 * Answer back the class info index as described in the JVM specifications.
	 * This is initialized only of the tag item is 'c'.
	 * 
	 * @return the class info index
	 */
	int getClassInfoIndex();

	/**
	 * Answer back the constant value as described in the JVM specifications.
	 * This is initialized only of the tag item is one of 'B', 'C', 'D', 'F',
	 * 'I', 'J', 'S', 'Z', or 's'.
	 * 
	 * @return the constant value
	 */
	IConstantPoolEntry getConstantValue();

	/**
	 * Answer back the constant value index as described in the JVM specifications.
	 * This is initialized only of the tag item is one of 'B', 'C', 'D', 'F',
	 * 'I', 'J', 'S', 'Z', or 's'.
	 * 
	 * @return the constant value index
	 */
	int getConstantValueIndex();

	/**
	 * Answer back the enum constant as described in the JVM specifications.
	 * This is initialized only of the tag item is 'e'.
	 * 
	 * @return the enum constant
	 */
	IConstantPoolEntry getEnumConstant();	
	
	/**
	 * Answer back the enum constant index as described in the JVM specifications.
	 * This is initialized only of the tag item is 'e'.
	 * 
	 * @return the enum constant index
	 */
	int getEnumConstantIndex();
	/**
	 * Answer back the tag as described in the JVM specifications.
	 * 
	 * @return the tag
	 */
	int getTag();
	
	/**
	 * Answer back the number of values as described in the JVM specifications.
	 * This is initialized only of the tag item is '['.
	 * 
	 * @return the number of values
	 */
	int getValuesNumber();
}
