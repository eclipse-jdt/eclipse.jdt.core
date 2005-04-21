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
package org.eclipse.jdt.core.dom;

/**
 * Internal class
 */
class ResolvedAnnotation implements IResolvedAnnotation 
{	
	static final ResolvedAnnotation[] NoAnnotations = new ResolvedAnnotation[0];
	private final org.eclipse.jdt.internal.compiler.lookup.IAnnotationInstance internalAnnotation;
	private final BindingResolver bindingResolver;
	
	ResolvedAnnotation(org.eclipse.jdt.internal.compiler.lookup.IAnnotationInstance anno, 
	  				   BindingResolver resolver )
	{
		internalAnnotation = anno;
		bindingResolver = resolver;
	}
	
	public ITypeBinding getAnnotationType() {
		final ITypeBinding binding = 
			this.bindingResolver.getTypeBinding(this.internalAnnotation.getAnnotationType());
		return binding.isAnnotation() ? binding : null;
	}
	
	public IResolvedMemberValuePair[] getDeclaredMemberValuePairs() {
		final org.eclipse.jdt.internal.compiler.lookup.IElementValuePair[] internalPair =
			this.internalAnnotation.getElementValuePairs();
		final int len = internalPair.length;
		IResolvedMemberValuePair[] pairs = ResolvedMemberValuePair.NoPair;
		for( int i=0; i<len; i++ ){
			pairs[i] = new ResolvedMemberValuePair(internalPair[i],this.bindingResolver);
		}
		return pairs;
	}
	
	public IResolvedMemberValuePair[] getAllMemberValuePairs() {
		// TODO missing implementation
		throw new RuntimeException("Not implemented yet"); //$NON-NLS-1$
	}
	
}
