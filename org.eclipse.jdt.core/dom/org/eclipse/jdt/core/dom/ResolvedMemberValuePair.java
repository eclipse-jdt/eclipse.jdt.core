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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * Internal class.
 */
class ResolvedMemberValuePair implements IResolvedMemberValuePair
{	
	static final ResolvedMemberValuePair[] NoPair = new ResolvedMemberValuePair[0]; 
	private static final Object NoValue = new Object();
	private final org.eclipse.jdt.internal.compiler.lookup.IElementValuePair internalPair;
	private Object value = null; 
	private final BindingResolver bindingResolver;
	
	ResolvedMemberValuePair(final org.eclipse.jdt.internal.compiler.lookup.IElementValuePair pair, 
					 BindingResolver resolver)
	{	
		this.internalPair = pair;
		this.bindingResolver = resolver;		
	}
	
	private void init()
	{
		final org.eclipse.jdt.internal.compiler.lookup.TypeBinding type = this.internalPair.getType();
		this.value =  buildDOMValue(this.internalPair.getValue(), type, this.bindingResolver);
		if( this.value == null )
			this.value = NoValue;
	}
	
	static Object buildDOMValue(final Object internalObject, 
			  				    org.eclipse.jdt.internal.compiler.lookup.TypeBinding type,
			  				    BindingResolver resolver)
	{
		if( internalObject == null || type == null ) return null;
		switch(type.id)
		{
		case TypeIds.T_boolean:
			return new Boolean( ((Constant)internalObject).booleanValue() );			
		case TypeIds.T_byte:
			return new Byte( ((Constant)internalObject).byteValue() );			
		case TypeIds.T_char:
			return new Character( ((Constant)internalObject).charValue() );
		case TypeIds.T_double:
			return new Double( ((Constant)internalObject).doubleValue() );			
		case TypeIds.T_float:
			return new Float( ((Constant)internalObject).floatValue() );
		case TypeIds.T_int:
			return new Integer( ((Constant)internalObject).intValue() );			
		case TypeIds.T_long:
			return new Long( ((Constant)internalObject).longValue() );			
		case TypeIds.T_short:
			return new Short( ((Constant)internalObject).shortValue() );
		case TypeIds.T_JavaLangString:
			return internalObject;
		case TypeIds.T_JavaLangClass:
			return resolver.getTypeBinding((org.eclipse.jdt.internal.compiler.lookup.TypeBinding)internalObject);
		}	
		
		if( type.isAnnotationType() ){
			return new ResolvedAnnotation(
					(org.eclipse.jdt.internal.compiler.lookup.IAnnotationInstance)internalObject, 
					resolver);
		}
		else if( type.isEnum() ){
			return resolver.getVariableBinding((org.eclipse.jdt.internal.compiler.lookup.FieldBinding)internalObject);
		}
		else if( type.isArrayType() ){
			final Object[] iElements = (Object[])internalObject;
			final int len = iElements.length;
			Object[] values = null;
			if( len > 0){
				final org.eclipse.jdt.internal.compiler.lookup.TypeBinding elementType =
					((org.eclipse.jdt.internal.compiler.lookup.ArrayBinding)type).leafComponentType;
				values = new Object[len];
				for( int i=0; i<len; i++ ){
					values[i] = buildDOMValue(iElements[i], elementType, resolver);
				}
			}
		}
		throw new IllegalStateException(); // should never get here.		
	}
	
	public String getName() {
		final char[] membername = this.internalPair.getMemberName();
		return membername == null ? null : new String(membername);
	}
	
	public IMethodBinding getMemberBinding() {
		return this.bindingResolver.getMethodBinding(this.internalPair.getMethodBinding());
	}
	
	public Object getValue() {
		if( value == null )
			init();
		return value == NoValue ? null : this.value;
	}	
}
