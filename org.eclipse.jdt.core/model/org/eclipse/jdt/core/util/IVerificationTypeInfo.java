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
 * Description of a verification type info as described in the JVM specifications.
 * 
 * This interface may be implemented by clients. 
 * 
 * @since 3.0
 */
public interface IVerificationTypeInfo {
	/**
	 * Answer back the tag of this verification type info as described in the JVM specifications.
	 * <ul>
	 * <li>0 for the top type</li>
	 * <li>1 for the int type</li>
	 * <li>2 for the float type</li>
	 * <li>3 for the double type</li>
	 * <li>4 for the long type</li>
	 * <li>5 for the null type</li>
	 * <li>6 for the uninitialized this type</li>
	 * <li>7 for the object type</li>
	 * <li>8 for the uninitialized offset type</li>
	 * </ul>
	 * 
	 * @return the tag of this verification type info as described in the JVM specifications
	 * @since 3.0
	 */
	int getTag();
	
	/**
	 * Answer back the offset of this verification type info as described in the JVM specifications.
	 * This makes sense only if the tag is 8.
	 * 
	 * @return the offset of this verification type info as described in the JVM specifications
	 * @since 3.0
	 */
	int getOffset();
	
	/**
	 * Answer back the constant pool index of this verification type info as described in the JVM specifications.
	 * This makes sense only if the tag is 7.
	 * 
	 * @return the constant pool index of this verification type info as described in the JVM specifications
	 * @since 3.0
	 */
	int getConstantPoolIndex();
	
	/**
	 * Answer back the name of the class type referenced by the index in the constant pool
	 * as described in the JVM specifications.
	 * This makes sense only if the tag is 7.
	 * 
	 * @return the name of the class type referenced by the index in the constant pool
	 * as described in the JVM specifications
	 * @since 3.0
	 */
	char[] getClassTypeName();
}
