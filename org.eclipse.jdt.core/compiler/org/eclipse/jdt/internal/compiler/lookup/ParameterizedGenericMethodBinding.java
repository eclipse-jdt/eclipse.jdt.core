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

import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Binding denoting a generic method after type parameter substitutions got performed.
 * On parameterized type bindings, all methods got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these methods.
 */
public class ParameterizedGenericMethodBinding extends ParameterizedMethodBinding implements Substitution {

    private TypeBinding[] typeArguments; 
    
    /**
     * Create method of parameterized type, substituting original parameters with type arguments.
     */
	public ParameterizedGenericMethodBinding(MethodBinding originalMethod, TypeBinding[] typeArguments) {

		this.modifiers = originalMethod.modifiers;
		this.selector = originalMethod.selector;
		this.declaringClass = originalMethod.declaringClass;
	    this.typeVariables = NoTypeVariables;
	    this.typeArguments = typeArguments;
	    this.originalMethod = originalMethod;
	    this.parameters = ReferenceBinding.substitute(this, originalMethod.parameters);
	    this.thrownExceptions = ReferenceBinding.substitute(this, originalMethod.thrownExceptions);
	    this.returnType = this.substitute(originalMethod.returnType);
	}
	
    /**
     * Returns a type, where original type was substituted using the receiver
     * parameterized method.
     */
    public TypeBinding substitute(TypeBinding originalType) {
        
        if ((originalType.tagBits & TagBits.HasTypeVariable) != 0) {
    	    if (originalType.isTypeVariable()) {
    	        TypeVariableBinding originalVariable = (TypeVariableBinding) originalType;
    	        TypeVariableBinding[] variables = this.originalMethod.typeVariables;
    	        int length = variables.length;
    	        // check this variable can be substituted given parameterized type
       		        if (originalVariable.rank < length && variables[originalVariable.rank] == originalVariable) {
    					return this.typeArguments[originalVariable.rank];
       		        }
    	    } else if (originalType.isParameterizedType()) {
    	        ParameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding) originalType;
    	        TypeBinding[] originalArguments = originalParameterizedType.arguments;
    	        TypeBinding[] substitutedArguments = ReferenceBinding.substitute(this, originalArguments);
    	        if (substitutedArguments != originalArguments) {
    	            return originalParameterizedType.environment.createParameterizedType(
    	                    originalParameterizedType.type, substitutedArguments, originalParameterizedType.enclosingType);
        	    } 
    	    } else if (originalType.isArrayType()) {
    			TypeBinding originalLeafComponentType = originalType.leafComponentType();
    			TypeBinding substitutedLeafComponentType = substitute(originalLeafComponentType);
    			if (substitutedLeafComponentType != originalLeafComponentType) {
    			    return ((ArrayBinding)originalType).environment().createArrayType(substitutedLeafComponentType, originalType.dimensions());
    			}
			} else if (originalType.isWildcard()) {
		        WildcardBinding wildcard = (WildcardBinding) originalType;
		        if (wildcard.kind != Wildcard.UNBOUND) {
			        TypeBinding originalBound = wildcard.bound;
			        TypeBinding substitutedBound = substitute(originalBound);
			        if (substitutedBound != originalBound) {
		        		return wildcard.environment.createWildcard(substitutedBound, wildcard.kind);
			        }
		        }
    	    }
        }
        return originalType;
    }

    
}
