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

import org.eclipse.jdt.core.dom.BindingResolver;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IResolvedMemberValuePair;

/**
 * Member value pair which compose of default values.
 */
class ResolvedDefaultValuePair implements IResolvedMemberValuePair
{	
	private final org.eclipse.jdt.internal.compiler.lookup.MethodBinding method;
	private final Object domValue;
	private final BindingResolver bindingResolver;
	
	ResolvedDefaultValuePair(org.eclipse.jdt.internal.compiler.lookup.AnnotationMethodBinding binding, final BindingResolver resolver )
	{	
		this.method = binding;
		this.domValue = ResolvedMemberValuePair.buildDOMValue(binding.getDefaultValue(), resolver);
		this.bindingResolver = resolver;
	}
	public String getName() {
		return new String( this.method.selector ); 
	}
	
	public IMethodBinding getMemberBinding() {
		return this.bindingResolver.getMethodBinding(this.method);
	}
	
	public Object getValue() {
		return this.domValue;
	}	
	
	public void toString(StringBuffer buffer) {
		buffer.append(getName());
		buffer.append(" = "); //$NON-NLS-1$		
		appendValue(getValue(), buffer);		
	}
	
	static void appendValue(Object value, StringBuffer buffer) {
		if( value instanceof Object[] ){
			appendValue((Object[])value, buffer);
		}
		else{
			if(value instanceof ITypeBinding ){
				buffer.append( ((ITypeBinding)value).getName() );
				buffer.append( ".class" ); //$NON-NLS-1$
			}
			else
				buffer.append(value);
		}
	}
	
	private static void appendValue(Object[] values, StringBuffer buffer) {		
		buffer.append('{');
		for( int i=0, len=values.length; i<len; i++ ){
			if( i != 0 )
				buffer.append(", "); //$NON-NLS-1$
			appendValue(values[i], buffer);
			
		}				
		buffer.append('}');
	}
	
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		toString(buffer);
		return buffer.toString();
	}
}
