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
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Binding denoting a method after type parameter substitutions got performed.
 * On parameterized type bindings, all methods got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these methods.
 */
public class ParameterizedMethodBinding extends MethodBinding {
    
    protected MethodBinding originalMethod;
    
    /**
     * Create method of parameterized type, substituting original parameters with type arguments.
     */
	public ParameterizedMethodBinding(ParameterizedTypeBinding parameterizedDeclaringClass, MethodBinding originalMethod) {
	
	    super(
	            originalMethod.modifiers, 
	            originalMethod.selector, 
	            parameterizedDeclaringClass.substitute(  originalMethod.returnType),
	            ReferenceBinding.substitute(parameterizedDeclaringClass, originalMethod.parameters),
	            ReferenceBinding.substitute(parameterizedDeclaringClass, originalMethod.thrownExceptions),
	            parameterizedDeclaringClass);
	    this.originalMethod = originalMethod;
	    this.typeVariables = originalMethod.typeVariables;
	}
	
    public ParameterizedMethodBinding() {
        // should only be used for subclass
    }

	/**
	 * Returns true if some parameters got substituted.
	 */
	public boolean hasSubstitutedParameters() {
	    return this.parameters != originalMethod.parameters;
	}
	
	/**
	 * Returns the original method (as opposed to parameterized instances)
	 */
	public MethodBinding original() {
	    return this.originalMethod.original();
	}	
}