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

/**
 * Annotation method that came from binary.
 * @author tyeung
 *
 */
public class AnnotationMethodBinding extends BinaryMethodBinding 
{
	private Object defaultValue;
	public AnnotationMethodBinding(int modifiers,
								   char[] selector, 
								   TypeBinding returnType, 
								   ReferenceBinding declaringClass,
								   IAnnotationInstance[] methodAnnotation,
								   IAnnotationInstance[][] parameterAnnotations,
								   Object defaultValue)
	{
		super(modifiers, selector, 
			 returnType, NoParameters, 
			 NoExceptions, declaringClass,
			 methodAnnotation, parameterAnnotations );
		
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue()
	{
		return this.defaultValue;
	}	
}
