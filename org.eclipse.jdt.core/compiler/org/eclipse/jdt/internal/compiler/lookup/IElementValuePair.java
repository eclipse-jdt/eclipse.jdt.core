/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public interface IElementValuePair 
{	
	/**
	 * @return the name of the member.
	 */
	
	char[] getMemberName();
	/**
	 * @return the method binding that defined this member value pair or null
	 *  	   if no such binding exists.
	 */
	MethodBinding getMethodBinding();	
	
	/**
	 * Convinence method. 
	 * The type will determine the type of objects returned from {@link #getValue()}
	 * @return the type of the member value or null if it cannot be determined
	 * @see #getMethodBinding()
	 * @see #getValue()
	 * 
	 */
	TypeBinding getType();

	/**
	 * <br><li>Return {@link TypeBinding} for member value of type {@link java.lang.Class}</li></br>
	 * <br><li>Return {@link org.eclipse.jdt.internal.compiler.impl.Constant} 
	 * for member of primitive type or String</li></br>
	 * <br><li>Return {@link FieldBinding} for enum constant</li></br>
	 * <br><li>Return {@link IAnnotationInstance} for annotation instance</li></br>
	 * <br><li>Return <code>Object[]</code> for member value of array type.
	 * @return the value of this member value pair or null if the value is missing or
	 *         is not a compile-time constant.
	 */
	Object getValue();
	
}
