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
 * Description of an annotation component value as described in the JVM specifications
 * (added in J2SE 1.5).
 * 
 * This interface may be implemented by clients. 
 *  
 * @since 3.0
 */
public interface IAnnotationComponentValue {
	
	/**
	 * Tag value for a constant of type <code>byte</code>
	 * @since 3.1
	 */
	int BYTE_TAG = 'B';
	/**
	 * Tag value for a constant of type <code>char</code>
	 * @since 3.1
	 */
	int CHAR_TAG = 'C';
	/**
	 * Tag value for a constant of type <code>double</code>
	 * @since 3.1
	 */
	int DOUBLE_TAG = 'D';
	/**
	 * Tag value for a constant of type <code>float</code>
	 * @since 3.1
	 */
	int FLOAT_TAG = 'F';
	/**
	 * Tag value for a constant of type <code>int</code>
	 * @since 3.1
	 */
	int INTEGER_TAG = 'I';
	/**
	 * Tag value for a constant of type <code>long</code>
	 * @since 3.1
	 */
	int LONG_TAG = 'J';
	/**
	 * Tag value for a constant of type <code>short</code>
	 * @since 3.1
	 */
	int SHORT_TAG = 'S';
	/**
	 * Tag value for a constant of type <code>boolean</code>
	 * @since 3.1
	 */
	int BOOLEAN_TAG = 'Z';
	/**
	 * Tag value for a constant of type <code>java.lang.String</code>
	 * @since 3.1
	 */
	int STRING_TAG = 's';
	/**
	 * Tag value for a value that represents an enum constant
	 * @since 3.1
	 */
	int ENUM_TAG = 'e';
	/**
	 * Tag value for a value that represents a class
	 * @since 3.1
	 */
	int CLASS_TAG = 'c';
	/**
	 * Tag value for a value that represents an annotation
	 * @since 3.1
	 */
	int ANNOTATION_TAG = '@';
	/**
	 * Tag value for a value that represents an array
	 * @since 3.1
	 */
	int ARRAY_TAG = '[';
	
	/**
	 * Answer back the annotation component values as described in the JVM specifications.
	 * This is initialized only if the tag item is '['.
	 * 
	 * @return the annotation component values
	 */
	IAnnotationComponentValue[] getAnnotationComponentValues();
	
	/**
	 * Answer back the annotation value as described in the JVM specifications.
	 * This is initialized only if the tag item is '&#064;'.
	 * 
	 * @return the attribute value
	 * @since 3.1
	 */
	IAnnotation getAnnotationValue();

	/**
	 * Answer back the annotation value as described in the JVM specifications.
	 * This is initialized only if the tag item is '&#064;'.
	 * 
	 * @return the attribute value
	 * TODO (olivier) remove after 3.1M4
	 * @deprecated Use getAnnotationValue() instead
	 */
	IAnnotation getAttributeValue();

	/**
	 * Answer back the class info as described in the JVM specifications.
	 * This is initialized only if the tag item is 'c'.
	 * 
	 * @return the class info
	 */
	IConstantPoolEntry getClassInfo();

	/**
	 * Answer back the class info index as described in the JVM specifications.
	 * This is initialized only if the tag item is 'c'.
	 * 
	 * @return the class info index
	 */
	int getClassInfoIndex();

	/**
	 * Answer back the constant value as described in the JVM specifications.
	 * This is initialized only if the tag item is one of 'B', 'C', 'D', 'F',
	 * 'I', 'J', 'S', 'Z', or 's'.
	 * 
	 * @return the constant value
	 */
	IConstantPoolEntry getConstantValue();

	/**
	 * Answer back the constant value index as described in the JVM specifications.
	 * This is initialized only if the tag item is one of 'B', 'C', 'D', 'F',
	 * 'I', 'J', 'S', 'Z', or 's'.
	 * 
	 * @return the constant value index
	 */
	int getConstantValueIndex();

	/**
	 * Answer back the simple name of the enum constant represented
	 * by this annotation component value as described in the JVM specifications.
	 * This is initialized only if the tag item is 'e'.
	 * 
	 * @return the enum constant
	 * @since 3.1
	 */
	char[] getEnumConstantName();	
	
	/**
	 * Answer back the utf8 constant index as described in the JVM specifications.
	 * This is initialized only if the tag item is 'e'.
	 * 
	 * @return the enum constant index
	 * @since 3.1
	 */
	int getEnumConstantNameIndex();

	/**
	 * Answer back the binary name of the type of the enum constant represented
	 * by this annotation component value as described in the JVM specifications.
	 * This is initialized only if the tag item is 'e'.
	 * 
	 * @return the enum constant
	 * @since 3.1
	 */
	char[] getEnumConstantTypeName();	
	
	/**
	 * Answer back the utf8 constant index as described in the JVM specifications.
	 * This is initialized only if the tag item is 'e'.
	 * 
	 * @return the enum constant index
	 * @since 3.1
	 */
	int getEnumConstantTypeNameIndex();

	/**
	 * Answer back the tag as described in the JVM specifications.
	 * 
	 * @return the tag
	 */
	int getTag();
	
	/**
	 * Answer back the number of values as described in the JVM specifications.
	 * This is initialized only if the tag item is '['.
	 * 
	 * @return the number of values
	 */
	int getValuesNumber();
}
