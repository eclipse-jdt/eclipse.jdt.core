/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class QualifiedParameterizedTypeReference extends ArrayQualifiedTypeReference {

	public TypeReference[][] typeArguments;
	private boolean didResolve = false;

	/**
	 * @param tokens
	 * @param dim
	 * @param positions
	 */
	public QualifiedParameterizedTypeReference(char[][] tokens, TypeReference[][] typeArguments, int dim, long[] positions) {
	    
		super(tokens, dim, positions);
		this.typeArguments = typeArguments;
	}
	public TypeReference copyDims(int dim){
		//return a type reference copy of me with some dimensions
		//warning : the new type ref has a null binding
		this.dimensions = dim;
		return this;
	}	
	
	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
     */
    protected TypeBinding getTypeBinding(Scope scope) {
        return null; // not supported here - combined with resolveType(...)
    }
    
	public StringBuffer printExpression(int indent, StringBuffer output) {
		int length = tokens.length;
		for (int i = 0; i < length - 1; i++) {
			output.append(tokens[i]);
			TypeReference[] typeArgument = typeArguments[i];
			if (typeArgument != null) {
				output.append('<');//$NON-NLS-1$
				int max = typeArgument.length - 1;
				for (int j = 0; j < max; j++) {
					typeArgument[j].print(0, output);
					output.append(", ");//$NON-NLS-1$
				}
				typeArgument[max].print(0, output);
				output.append('>');
			}
			output.append('.');
		}
		output.append(tokens[length - 1]);
		TypeReference[] typeArgument = typeArguments[length - 1];
		if (typeArgument != null) {
			output.append('<');//$NON-NLS-1$
			int max = typeArgument.length - 1;
			for (int j = 0; j < max; j++) {
				typeArgument[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeArgument[max].print(0, output);
			output.append('>');
		}
		for (int i= 0 ; i < dimensions ; i++) {
			output.append("[]"); //$NON-NLS-1$
		}
		return output;
	}	
	
	public TypeBinding resolveType(BlockScope scope) {
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
		for (int i = 0, max = this.tokens.length; i < max; i++) {
		    if (i == 0) {
		        // isolate first fragment
				while (this.typeArguments[i] == null) i++;
				TypeBinding type = scope.getType(this.tokens, i+1);
				if (!(type.isValidBinding())) {
					reportInvalidType(scope);
					return null;
				}
				currentType = (ReferenceBinding) type;
		    } else {
			    currentType = scope.getMemberType(this.tokens[i], currentType);
		    }
		    // check generic and arity
			TypeVariableBinding[] typeVariables = currentType.typeVariables();
		    TypeReference[] args = this.typeArguments[i];
			int argLength = args.length;
			TypeBinding[] argTypes = new TypeBinding[argLength];
			for (int j = 0; j < argLength; j++) {
			    argTypes[j] = args[j].resolveType(scope);
			}
			if (typeVariables == NoTypeVariables) { // check generic
					scope.problemReporter().nonGenericTypeCannotBeParameterized(this, currentType, argTypes);
					return null;
			} else if (argLength != typeVariables.length) { // check arity
					scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes);
					return null;
			}			
			// check argument type compatibility
			boolean argHasError = false;
			for (int j = 0; j < argLength; j++) {
			    TypeBinding argType = argTypes[j];
			    if (argType == null) {
			        argHasError = true;
			    } else if (!typeVariables[j].boundCheck(argType)) {
			        argHasError = true;
					scope.problemReporter().typeMismatchError(argType, typeVariables[j], currentType, args[j]);
			    }
			}
			if (argHasError) return null;
			currentType = scope.createParameterizedType(currentType, argTypes);
		}
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
}
