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
 * JSR 175 Annotation instances that came from binary
 */
public class BinaryAnnotation implements IAnnotationInstance 
{
    // At creation the type may not be fully resolved.
	// The type will become fully resolved when this annotation is requested.
	ReferenceBinding typeBinding;
	IElementValuePair[] pairs;	
	private final LookupEnvironment env;
	
	BinaryAnnotation(ReferenceBinding binding, LookupEnvironment env)
	{ 
		this.typeBinding = binding;
		this.pairs = null;
		this.env = env;		
	}
	
	public ReferenceBinding getAnnotationType()
	{
		// annotation type are never parameterized
		return BinaryTypeBinding.resolveType(this.typeBinding, this.env, false);
	}
	
	public IElementValuePair[] getElementValuePairs(){ return this.pairs; }
}
