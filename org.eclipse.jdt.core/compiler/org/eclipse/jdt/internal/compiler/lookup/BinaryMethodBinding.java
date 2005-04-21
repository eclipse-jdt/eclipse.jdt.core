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

public class BinaryMethodBinding extends MethodBinding 
{		
	private static final IAnnotationInstance[][] NoExtendedModifiers = new IAnnotationInstance[0][];
	
	/**
	 * In the majority of the time, there will no annotations at all.
	 * We will try to optimized the storage by packing both
	 * method and parameter annotation into one field. 
	 * 
	 * If there are no annotations and no parameter annotations, 
	 * this will be a zero-length array. 
	 * If this is an array of size 1, then method annotations are intialized and there 
	 * may or may not be parameter annotations. 
	 * If there are ever any parameter annotations, this will be an array of size > 1. 
	 * </code>null</code> in the array means not initialized.
	 * If the field is <code>null</code> this means it is not initalized at all.	 
	 */
	private IAnnotationInstance[][] extendedModifiers = null;	
	
	public BinaryMethodBinding(int modifiers,
							   char[] selector, 
							   TypeBinding returnType, 
							   TypeBinding[] parameters, 
							   ReferenceBinding[] thrownExceptions, 
							   ReferenceBinding declaringClass,
							   IAnnotationInstance[] methodAnnotations,
							   IAnnotationInstance[][] parameterAnnotations)
	{
		super( modifiers, selector, returnType, parameters, thrownExceptions, declaringClass);
	}
	
	//constructor for creating binding representing constructor
	public BinaryMethodBinding(int modifiers,
					  		   TypeBinding[] parameters,
					  		   ReferenceBinding[] thrownExceptions,
					  		   ReferenceBinding declaringClass,
							   IAnnotationInstance[] methodAnnotations,
							   IAnnotationInstance[][] parameterAnnotations)
	{
		this(modifiers, TypeConstants.INIT, VoidBinding, 
			 parameters, thrownExceptions, declaringClass,
			 methodAnnotations, parameterAnnotations);
	}
	
	
	//special API used to change method declaring class for runtime visibility check
	public BinaryMethodBinding(BinaryMethodBinding initialMethodBinding, ReferenceBinding declaringClass) 
	{
		super(initialMethodBinding, declaringClass );
		this.extendedModifiers = initialMethodBinding.extendedModifiers;
	}
	
	void initExtendedModifiers(final IAnnotationInstance[] methodAnnotations,
							   final IAnnotationInstance[][] parameterAnnotations)
	{
		final int numMethodAnnos = methodAnnotations.length;
		final int numParams  = parameterAnnotations.length;		
		if( numMethodAnnos == 0 && numParams == 0 )
			this.extendedModifiers = NoExtendedModifiers;
		else{
			// not even going to create the slot if there are no parameters
			if( numParams == 0 )
				this.extendedModifiers = new IAnnotationInstance[][]{methodAnnotations};
			else{
				this.extendedModifiers = new IAnnotationInstance[numParams + 1][];
				this.extendedModifiers[0] = methodAnnotations;
				System.arraycopy(parameterAnnotations, 0, this.extendedModifiers, 1, numParams);
			}
		}
	}
	
	public IAnnotationInstance[] getAnnotations()
	{
		final int len = this.extendedModifiers.length;
		if( len == 0 ) return NoAnnotations;
		else
			return this.extendedModifiers[0];
	}
	
	public IAnnotationInstance[] getParameterAnnotations(final int index)
	{		
		final int numberOfParameters = this.parameters == null ? 0 : this.parameters.length;
		if( numberOfParameters == 0 || index < 0 || index >= numberOfParameters )
			throw new IllegalArgumentException("number of parameters = " + numberOfParameters + //$NON-NLS-1$ 
											   " index = " + index ); //$NON-NLS-1$	
		final int numberOfExtendedMods = this.extendedModifiers.length;
		// no annotations what so ever.		
		if( numberOfExtendedMods == 0 )
			return NoAnnotations;		
		else
			return this.extendedModifiers[index + 1];	
	}
	
	public Object getDefaultValue(){ return null; }
}
