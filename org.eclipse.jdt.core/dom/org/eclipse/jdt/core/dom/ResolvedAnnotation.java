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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Internal class
 */
class ResolvedAnnotation implements IResolvedAnnotation 
{	
	static final ResolvedAnnotation[] NoAnnotations = new ResolvedAnnotation[0];
	private final org.eclipse.jdt.internal.compiler.lookup.IAnnotationInstance internalAnnotation;
	private final BindingResolver bindingResolver;
	
	/**	 
	 * @param anno
	 * @param resolver
	 */
	ResolvedAnnotation(org.eclipse.jdt.internal.compiler.lookup.IAnnotationInstance anno, 
	  				   BindingResolver resolver )
	{
		internalAnnotation = anno;
		if( internalAnnotation == null )
			throw new IllegalStateException();
		bindingResolver = resolver;
	}
	
	public ITypeBinding getAnnotationType() {
		final ITypeBinding binding = 
			this.bindingResolver.getTypeBinding(this.internalAnnotation.getAnnotationType());
		if( binding == null || !binding.isAnnotation() ) 
			return null;
		else
			return binding;		 
	}
	
	public IResolvedMemberValuePair[] getDeclaredMemberValuePairs() {
		final org.eclipse.jdt.internal.compiler.lookup.IElementValuePair[] internalPair =
			this.internalAnnotation.getElementValuePairs();
		final int len = internalPair.length;		
		IResolvedMemberValuePair[] pairs = ResolvedMemberValuePair.NoPair;
		if( len > 0 )
		{
			pairs = new ResolvedMemberValuePair[len];
			for( int i=0; i<len; i++ ){
				pairs[i] = new ResolvedMemberValuePair(internalPair[i],this.bindingResolver);
			}
		}
		return pairs;
	}
	
	public IResolvedMemberValuePair[] getAllMemberValuePairs() {
		final ReferenceBinding typeBinding = this.internalAnnotation.getAnnotationType();
		IResolvedMemberValuePair[] pairs = ResolvedMemberValuePair.NoPair;
		final org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] methods = typeBinding.methods();
		final int len = methods == null ? 0 : methods.length;
		if(typeBinding != null && len > 0 ){
			final org.eclipse.jdt.internal.compiler.lookup.IElementValuePair[] internalPair = 
				this.internalAnnotation.getElementValuePairs();
			final int declaredLen = internalPair == null ? 0 : internalPair.length;
			final Object[] names = declaredLen < len ? new Object[declaredLen] : null;
			for( int i=0; i<declaredLen; i++ ){
				pairs[i] = new ResolvedMemberValuePair(internalPair[i], this.bindingResolver);
				names[i] = internalPair[i].getMemberName();
			}
			if( declaredLen < len ){
				final MemberComparator comparator = new MemberComparator();
				Arrays.sort(names, comparator);
				int pIndex = declaredLen;
				for( int i=0; i<len; i++, pIndex++ ){
					final char[] selector = methods[i].selector;
					final int index = Arrays.binarySearch(names, selector, comparator);
					if( index < 0 )
						pairs[pIndex] = new ResolvedDefaultValuePair((AnnotationMethodBinding)methods[i], this.bindingResolver);
				}				
			}			
		}
		return pairs;
	}
	
	public String toString()
	{
		final ITypeBinding annoType = getAnnotationType();	
	    
		final StringBuffer buffer = new StringBuffer();		
		buffer.append('@');
		if(annoType != null)
			buffer.append(annoType.getName());
		buffer.append('(');
		final IResolvedMemberValuePair[] pairs = getDeclaredMemberValuePairs();		
		for( int i = 0, len = pairs.length; i<len; i++ ){
			if( i != 0 )
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(pairs[i].toString());
		}
		buffer.append(')');
		return buffer.toString();
		
	}
	
	private static class MemberComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1) 
		{
			// throws ClassCastException if either one of the arguments is not 
			// a char[]
			final char[] c0 = (char[])arg0;
			final char[] c1 = (char[])arg1;
			return CharOperation.compareWith(c0, c1);
		}
	}
}
