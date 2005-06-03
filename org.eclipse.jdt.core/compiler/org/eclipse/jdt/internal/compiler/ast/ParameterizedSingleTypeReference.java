/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
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
		this.originalSourceEnd = this.sourceEnd;
		this.typeArguments = typeArguments;
	}
	public void checkBounds(Scope scope) {
		if (this.resolvedType == null) return;

		if (this.resolvedType.leafComponentType() instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) this.resolvedType.leafComponentType();
			ReferenceBinding currentType = parameterizedType.type;
			TypeVariableBinding[] typeVariables = currentType.typeVariables();
			TypeBinding[] argTypes = parameterizedType.arguments;
			if (argTypes != null && typeVariables != null) { // may be null in error cases
				parameterizedType.boundCheck(scope, this.typeArguments);
			}
		}
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#copyDims(int)
	 */
	public TypeReference copyDims(int dim) {
		this.dimensions = dim;
		return this;
	}

	/**
	 * @return char[][]
	 */
	public char [][] getParameterizedTypeName(){
		StringBuffer buffer = new StringBuffer(5);
		buffer.append(this.token).append('<');
		for (int i = 0, length = this.typeArguments.length; i < length; i++) {
			if (i > 0) buffer.append(',');
			buffer.append(CharOperation.concatWith(this.typeArguments[i].getParameterizedTypeName(), '.'));
		}
		buffer.append('>');
		int nameLength = buffer.length();
		char[] name = new char[nameLength];
		buffer.getChars(0, nameLength, name, 0);
		int dim = this.dimensions;
		if (dim > 0) {
			char[] dimChars = new char[dim*2];
			for (int i = 0; i < dim; i++) {
				int index = i*2;
				dimChars[index] = '[';
				dimChars[index+1] = ']';
			}
			name = CharOperation.concat(name, dimChars);
		}		
		return new char[][]{ name };
	}	
	/**
     * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
     */
    protected TypeBinding getTypeBinding(Scope scope) {
        return null; // not supported here - combined with resolveType(...)
    }	

    /*
     * No need to check for reference to raw type per construction
     */
	private TypeBinding internalResolveType(Scope scope, ReferenceBinding enclosingType, boolean checkBounds) {

		// handle the error here
		this.constant = NotAConstant;
		if (this.didResolve) { // is a shared type reference which was already resolved
			if (this.resolvedType != null && !this.resolvedType.isValidBinding())
				return null; // already reported error
			return this.resolvedType;
		} 
	    this.didResolve = true;
		if (enclosingType == null) {
			this.resolvedType = scope.getType(token);
			if (!(this.resolvedType.isValidBinding())) {
				reportInvalidType(scope);
				return null;
			}
			enclosingType = this.resolvedType.enclosingType(); // if member type
			if (enclosingType != null) {
				ReferenceBinding currentType = (ReferenceBinding) this.resolvedType;
				if (currentType.isStatic() 
						|| (enclosingType.isGenericType() 
								&& enclosingType.outermostEnclosingType() != scope.outerMostClassScope().referenceContext.binding)) {
					enclosingType = (ReferenceBinding) scope.convertToRawType(enclosingType);
				}
			}
		} else { // resolving member type (relatively to enclosingType)
			this.resolvedType = scope.getMemberType(token, (ReferenceBinding)enclosingType.erasure());		    
			if (!this.resolvedType.isValidBinding()) {
				scope.problemReporter().invalidEnclosingType(this, this.resolvedType, enclosingType);
				return null;
			}
			if (isTypeUseDeprecated(this.resolvedType, scope))
				scope.problemReporter().deprecatedType(this.resolvedType, this);
		}

		// check generic and arity
	    boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
		ReferenceBinding currentType = (ReferenceBinding) this.resolvedType;
		int argLength = this.typeArguments.length;
		TypeBinding[] argTypes = new TypeBinding[argLength];
		boolean argHasError = false;
		for (int i = 0; i < argLength; i++) {
		    TypeReference typeArgument = this.typeArguments[i];
		    TypeBinding argType = isClassScope
				? typeArgument.resolveTypeArgument((ClassScope) scope, currentType, i)
				: typeArgument.resolveTypeArgument((BlockScope) scope, currentType, i);
		     if (argType == null) {
		         argHasError = true;
		     } else {
			    argTypes[i] = argType;
		     }
		}
		if (argHasError) return null;
		if (isClassScope)
			if (((ClassScope) scope).detectHierarchyCycle(currentType, this, argTypes))
				return null;

		TypeVariableBinding[] typeVariables = currentType.typeVariables();
		if (typeVariables == NoTypeVariables) { // check generic
			scope.problemReporter().nonGenericTypeCannotBeParameterized(this, currentType, argTypes);
			return null;
		} else if (argLength != typeVariables.length) { // check arity
			scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes);
			return null;
		} else if (!currentType.isStatic() && enclosingType != null && enclosingType.isRawType()){
			scope.problemReporter().rawMemberTypeCannotBeParameterized(
					this, scope.environment().createRawType((ReferenceBinding)currentType.erasure(), enclosingType), argTypes);
			return null;
		}

		// if generic type X<T> is referred to as parameterized X<T>, then answer itself
		boolean allEqual = true;
	    for (int i = 0; allEqual && i < argLength; i++)
			allEqual = typeVariables[i] == argTypes[i];
	    if (!allEqual) {
	    	ParameterizedTypeBinding parameterizedType = scope.createParameterizedType((ReferenceBinding)currentType.erasure(), argTypes, enclosingType);
			// check argument type compatibility
			if (checkBounds) // otherwise will do it in Scope.connectTypeVariables() or generic method resolution
				parameterizedType.boundCheck(scope, this.typeArguments);
	
			this.resolvedType = parameterizedType;
			if (isTypeUseDeprecated(this.resolvedType, scope))
				reportDeprecatedType(scope);
		}
		// array type ?
		if (this.dimensions > 0) {
			if (dimensions > 255)
				scope.problemReporter().tooManyDimensions(this);
			this.resolvedType = scope.createArrayType(this.resolvedType, dimensions);
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
		if ((this.bits & IsVarArgs) != 0) {
			for (int i= 0 ; i < dimensions - 1; i++) {
				output.append("[]"); //$NON-NLS-1$
			}
			output.append("..."); //$NON-NLS-1$
		} else {
			for (int i= 0 ; i < dimensions; i++) {
				output.append("[]"); //$NON-NLS-1$
			}
		}
		return output;
	}
	
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
	    return internalResolveType(scope, null, checkBounds);
	}	

	public TypeBinding resolveType(ClassScope scope) {
	    return internalResolveType(scope, null, false /*no bounds check in classScope*/);
	}	
	
	public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
	    return internalResolveType(scope, enclosingType, true/*check bounds*/);
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
