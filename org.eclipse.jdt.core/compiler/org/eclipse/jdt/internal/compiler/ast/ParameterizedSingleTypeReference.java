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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class ParameterizedSingleTypeReference extends ArrayTypeReference {

	public TypeReference[] typeArguments;
	private boolean didResolve = false;
	
	public ParameterizedSingleTypeReference(char[] name, TypeReference[] typeArguments, int dim, long pos){
		super(name, dim, pos);
		this.typeArguments = typeArguments;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#copyDims(int)
	 */
	public TypeReference copyDims(int dim) {
		this.dimensions = dim;
		return this;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
     */
    protected TypeBinding getTypeBinding(Scope scope) {
        return null; // not supported here - combined with resolveType(...)
    }	

	private TypeBinding internalResolveType(Scope scope) {
	    
	    boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
	    
		// handle the error here
		this.constant = NotAConstant;
		if (this.didResolve) { // is a shared type reference which was already resolved
			if (this.resolvedType != null && !this.resolvedType.isValidBinding()) {
				return null; // already reported error
			}
			return this.resolvedType;
		} 
	    this.didResolve = true;
		ReferenceBinding currentType = null;
		this.resolvedType = scope.getType(token);
		if (!(this.resolvedType.isValidBinding())) {
			reportInvalidType(scope);
			return null;
		}
		currentType = (ReferenceBinding) this.resolvedType;
	    // check generic and arity
		TypeVariableBinding[] typeVariables = currentType.typeVariables();
		int argLength = this.typeArguments.length;
		TypeBinding[] argTypes = new TypeBinding[argLength];
		boolean argHasError = false;
		for (int j = 0; j < argLength; j++) {
		    TypeBinding argType;
		    if (isClassScope) {
		        	argType = this.typeArguments[j].resolveType((ClassScope)scope);
		    } else {
		        	argType = this.typeArguments[j].resolveType((BlockScope)scope);
		    }
		     if (argType == null) {
		         argHasError = true;
		     } else {
			    argTypes[j] = argType;
		     }
		}
		if (argHasError) return null;
		if (typeVariables == NoTypeVariables) { // check generic
				scope.problemReporter().nonGenericTypeCannotBeParameterized(this, currentType, argTypes);
				return null;
		} else if (argLength != typeVariables.length) { // check arity
				scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes);
				return null;
		}			
		// check argument type compatibility
		for (int j = 0; j < argLength; j++) {
		    TypeBinding argType = argTypes[j];
			if (!typeVariables[j].boundCheck(argType)) {
		        argHasError = true;
				scope.problemReporter().typeMismatchError(argType, typeVariables[j], currentType, this.typeArguments[j]);
		    }
		}
		if (argHasError) return null;
		currentType = scope.createParameterizedType(currentType, argTypes);
		this.resolvedType = currentType;
		if (isTypeUseDeprecated(this.resolvedType, scope)) {
			reportDeprecatedType(scope);
		}		
		// array type ?
		if (this.dimensions > 0) {
			if (dimensions > 255) {
				scope.problemReporter().tooManyDimensions(this);
			}
			this.resolvedType = scope.createArray(currentType, dimensions);
		}
		return this.resolvedType;
	}	
	
	public StringBuffer printExpression(int indent, StringBuffer output){
		output.append(token);
		output.append("<"); //$NON-NLS-1$
		int max = typeArguments.length - 1;
		for (int i= 0; i < max; i++) {
			typeArguments[i].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		typeArguments[max].print(0, output);
		output.append(">"); //$NON-NLS-1$
		for (int i= 0 ; i < dimensions ; i++) {
			output.append("[]"); //$NON-NLS-1$
		}
		return output;
	}
	
	public TypeBinding resolveType(BlockScope scope) {
	    return internalResolveType(scope);
	}	

	public TypeBinding resolveType(ClassScope scope) {
	    return internalResolveType(scope);
	}	
}
