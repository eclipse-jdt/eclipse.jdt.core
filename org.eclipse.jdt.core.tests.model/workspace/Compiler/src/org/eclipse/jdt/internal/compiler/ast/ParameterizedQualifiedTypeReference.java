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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class ParameterizedQualifiedTypeReference extends ArrayQualifiedTypeReference {

	public TypeReference[][] typeArguments;
	private boolean didResolve = false;

	/**
	 * @param tokens
	 * @param dim
	 * @param positions
	 */
	public ParameterizedQualifiedTypeReference(char[][] tokens, TypeReference[][] typeArguments, int dim, long[] positions) {
	    
		super(tokens, dim, positions);
		this.typeArguments = typeArguments;
	}
	public void checkBounds(Scope scope) {
		if (this.resolvedType == null) return;

		checkBounds(
			(ReferenceBinding) this.resolvedType.leafComponentType(),
			scope,
			this.typeArguments.length - 1);
	}
	public void checkBounds(ReferenceBinding type, Scope scope, int index) {
		if (type.enclosingType() != null)
			checkBounds(type.enclosingType(), scope, index - 1);

		if (type.isParameterizedType()) {
			ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) type;
			ReferenceBinding currentType = parameterizedType.type;
			TypeVariableBinding[] typeVariables = currentType.typeVariables();
			TypeBinding[] argTypes = parameterizedType.arguments;
			if (argTypes != null && typeVariables != null) { // argTypes may be null in error cases
				for (int i = 0, argLength = typeVariables.length; i < argLength; i++)
				    if (!typeVariables[i].boundCheck(parameterizedType, argTypes[i]))
						scope.problemReporter().typeMismatchError(argTypes[i], typeVariables[i], currentType, this.typeArguments[index][i]);
			}
		}
	}
	public TypeReference copyDims(int dim){
		//return a type reference copy of me with some dimensions
		//warning : the new type ref has a null binding
		this.dimensions = dim;
		return this;
	}	
	
	/**
	 * @return char[][]
	 */
	public char [][] getParameterizedTypeName(){
		int length = this.tokens.length;
		char[][] qParamName = new char[length][];
		for (int i = 0; i < length; i++) {
			TypeReference[] arguments = this.typeArguments[i];
			if (arguments == null) {
				qParamName[i] = this.tokens[i];
			} else {
				StringBuffer buffer = new StringBuffer(5);
				buffer.append(this.tokens[i]);
				buffer.append('<');
				for (int j = 0, argLength =arguments.length; j < argLength; j++) {
					if (j > 0) buffer.append(',');
					buffer.append(CharOperation.concatWith(arguments[j].getParameterizedTypeName(), '.'));
				}
				buffer.append('>');
				int nameLength = buffer.length();
				qParamName[i] = new char[nameLength];
				buffer.getChars(0, nameLength, qParamName[i], 0);		
			}
		}
		int dim = this.dimensions;
		if (dim > 0) {
			char[] dimChars = new char[dim*2];
			for (int i = 0; i < dim; i++) {
				int index = i*2;
				dimChars[index] = '[';
				dimChars[index+1] = ']';
			}
			qParamName[length-1] = CharOperation.concat(qParamName[length-1], dimChars);
		}
		return qParamName;
	}	
	
	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
     */
    protected TypeBinding getTypeBinding(Scope scope) {
        return null; // not supported here - combined with resolveType(...)
    }
    
    /*
     * No need to check for reference to raw type per construction
     */
	private TypeBinding internalResolveType(Scope scope) {

		// handle the error here
		this.constant = NotAConstant;
		if (this.didResolve) { // is a shared type reference which was already resolved
			if (this.resolvedType != null && !this.resolvedType.isValidBinding())
				return null; // already reported error
			return this.resolvedType;
		} 
	    this.didResolve = true;
	    Binding binding = scope.getPackage(this.tokens);
	    if (binding != null && !binding.isValidBinding()) {
	    	this.resolvedType = (ReferenceBinding) binding;
			reportInvalidType(scope);
			return null;
		}

	    PackageBinding packageBinding = binding == null ? null : (PackageBinding) binding;
	    boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
		boolean typeIsConsistent = true;
		ReferenceBinding qualifiedType = null;
	    for (int i = packageBinding == null ? 0 : packageBinding.compoundName.length, max = this.tokens.length; i < max; i++) {
			findNextTypeBinding(i, scope, packageBinding);
			if (!(this.resolvedType.isValidBinding())) {
				reportInvalidType(scope);
				return null;
			}
			ReferenceBinding currentType = (ReferenceBinding) this.resolvedType;
			if (typeIsConsistent && currentType.isStatic() && qualifiedType != null && (qualifiedType.isParameterizedType() || qualifiedType.isGenericType())) {
				scope.problemReporter().staticMemberOfParameterizedType(this, scope.createParameterizedType(currentType, null, qualifiedType));
				typeIsConsistent = false;
			}			
			// check generic and arity
		    TypeReference[] args = this.typeArguments[i];
		    if (args != null) {
				int argLength = args.length;
				TypeBinding[] argTypes = new TypeBinding[argLength];
				boolean argHasError = false;
				for (int j = 0; j < argLength; j++) {
				    TypeReference arg = args[j];
				    TypeBinding argType = isClassScope
						? arg.resolveTypeArgument((ClassScope) scope, currentType, j)
						: arg.resolveTypeArgument((BlockScope) scope, currentType, j);
					if (argType == null) {
						argHasError = true;
					} else {
						argTypes[j] = argType;
					}			    
				}
				if (argHasError) return null;
// TODO (philippe)	if ((this.bits & ASTNode.IsSuperType) != 0)
				if (isClassScope)
					if (((ClassScope) scope).detectCycle(currentType, this, argTypes))
						return null;

			    TypeVariableBinding[] typeVariables = currentType.typeVariables();
				if (typeVariables == NoTypeVariables) { // check generic
					scope.problemReporter().nonGenericTypeCannotBeParameterized(this, currentType, argTypes);
					return null;
				} else if (argLength != typeVariables.length) { // check arity
					scope.problemReporter().incorrectArityForParameterizedType(this, currentType, argTypes);
					return null;
				}
				// check parameterizing non-static member type of raw type
				if (typeIsConsistent && !currentType.isStatic() && qualifiedType != null && qualifiedType.isRawType()) {
					scope.problemReporter().rawMemberTypeCannotBeParameterized(
							this, scope.environment().createRawType(currentType, qualifiedType), argTypes);
					typeIsConsistent = false;				
				}
				ParameterizedTypeBinding parameterizedType = scope.createParameterizedType(currentType, argTypes, qualifiedType);
				// check argument type compatibility now if not a class scope
				if (!isClassScope) // otherwise will do it in Scope.connectTypeVariables()
					for (int j = 0; j < argLength; j++)
					    if (!typeVariables[j].boundCheck(parameterizedType, argTypes[j]))
							scope.problemReporter().typeMismatchError(argTypes[j], typeVariables[j], currentType, args[j]);
				qualifiedType = parameterizedType;
		    } else {
// TODO (philippe)	if ((this.bits & ASTNode.IsSuperType) != 0)
				if (isClassScope)
					if (((ClassScope) scope).detectCycle(currentType, this, null))
						return null;
				if (currentType.isGenericType()) {
	   			    if (typeIsConsistent && qualifiedType != null && qualifiedType.isParameterizedType()) {
						scope.problemReporter().parameterizedMemberTypeMissingArguments(this, scope.createParameterizedType(currentType, null, qualifiedType));
						typeIsConsistent = false;
					}
	   			    qualifiedType = scope.environment().createRawType(currentType, qualifiedType); // raw type
				} else {
					qualifiedType = (qualifiedType != null && qualifiedType.isParameterizedType())
													? scope.createParameterizedType(currentType, null, qualifiedType)
													: currentType;
				}
			}
		}
		this.resolvedType = qualifiedType;
		if (isTypeUseDeprecated(this.resolvedType, scope))
			reportDeprecatedType(scope);
		// array type ?
		if (this.dimensions > 0) {
			if (dimensions > 255)
				scope.problemReporter().tooManyDimensions(this);
			this.resolvedType = scope.createArrayType(this.resolvedType, dimensions);
		}
		return this.resolvedType;
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
	    return internalResolveType(scope);
	}	
	public TypeBinding resolveType(ClassScope scope) {
	    return internalResolveType(scope);
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				if (this.typeArguments[i] != null) {
					for (int j = 0, max2 = this.typeArguments[i].length; j < max2; j++) {
						this.typeArguments[i][j].traverse(visitor, scope);
					}
				}
			}
		}
		visitor.endVisit(this, scope);
	}
	
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				if (this.typeArguments[i] != null) {
					for (int j = 0, max2 = this.typeArguments[i].length; j < max2; j++) {
						this.typeArguments[i][j].traverse(visitor, scope);
					}
				}
			}
		}
		visitor.endVisit(this, scope);
	}

}
