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

import org.eclipse.jdt.internal.compiler.impl.Constant;

public class BinaryFieldBinding extends FieldBinding
{
	private IAnnotationInstance[] annotations = NoAnnotations;
	public BinaryFieldBinding(char[] name, 
							  TypeBinding type, 
							  int modifiers, 
							  ReferenceBinding declaringClass,
							  Constant constant,
							  IAnnotationInstance[] annos) {
		super(name, type, modifiers, declaringClass, constant);		
		if( annos != null )
			this.annotations = annos;
	}
	
	public BinaryFieldBinding(BinaryFieldBinding initialFieldBinding, ReferenceBinding declaringClass)
	{
		super(initialFieldBinding, declaringClass);
		this.annotations = initialFieldBinding.annotations;
	}
	
	public IAnnotationInstance[] getAnnotations()
	{
		return annotations;
	}
}
