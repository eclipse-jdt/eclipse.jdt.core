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

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

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
		ReferenceBinding qualifiedType = null;
	    boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
		for (int i = 0, max = this.tokens.length; i < max; i++) {
		    ReferenceBinding currentType;
		    if (i == 0) {
		        // isolate first fragment
				while (this.typeArguments[i] == null) i++;
				try {
					this.resolvedType = scope.getType(this.tokens, i+1);
				} catch (AbortCompilation e) {
					e.updateContext(this, scope.referenceCompilationUnit().compilationResult);
					throw e;
				}
				if (!(this.resolvedType.isValidBinding())) {
					reportInvalidType(scope);
					return null;
				}
				currentType = (ReferenceBinding) this.resolvedType;
				if (currentType.isMemberType()) { // check raw enclosing type
				    ArrayList enclosingTypes = new ArrayList();
				    boolean hasGenericEnclosing = false;
				    for (ReferenceBinding enclosing = currentType.enclosingType(); enclosing != null; enclosing = enclosing.enclosingType()) {
				        enclosingTypes.add(enclosing);
				        if (enclosing.isGenericType()) hasGenericEnclosing = true;
				    }
				    if (hasGenericEnclosing) {
				        for (int j = enclosingTypes.size() - 1; j >= 0; j--) {
				            ReferenceBinding enclosing = (ReferenceBinding)enclosingTypes.get(j);
				            if (enclosing.isGenericType()) {
					            qualifiedType = scope.environment().createRawType(enclosing, qualifiedType); // raw type
				            }
				        }
				    }
				}
		    } else {
			    this.resolvedType = currentType = scope.getMemberType(this.tokens[i], (ReferenceBinding)qualifiedType.erasure());
				if (!(this.resolvedType.isValidBinding())) {
					reportInvalidType(scope);
					return null;
				}
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
				ParameterizedTypeBinding parameterizedType = scope.createParameterizedType(currentType, argTypes, qualifiedType);
				// check argument type compatibility
				for (int j = 0; j < argLength; j++) {
				    TypeBinding argType = argTypes[j];
				    if (!typeVariables[j].boundCheck(parameterizedType, argType)) {
						scope.problemReporter().typeMismatchError(argType, typeVariables[j], currentType, args[j]);
				    }
				}
				qualifiedType = parameterizedType;
		    } else {
// TODO (philippe)	if ((this.bits & ASTNode.IsSuperType) != 0)
				if (isClassScope)
					if (((ClassScope) scope).detectCycle(currentType, this, null))
						return null;

				if (currentType.isGenericType()) { // check raw type
   			    	qualifiedType = scope.environment().createRawType(currentType, qualifiedType); // raw type
   			    } else if (qualifiedType != null && (qualifiedType.isParameterizedType() || qualifiedType.isRawType())) {
   			    	qualifiedType = scope.createParameterizedType(currentType, null, qualifiedType);
   			    }
			}
		}
		this.resolvedType = qualifiedType;
		if (isTypeUseDeprecated(this.resolvedType, scope)) {
			reportDeprecatedType(scope);
		}		
		// array type ?
		if (this.dimensions > 0) {
			if (dimensions > 255) {
				scope.problemReporter().tooManyDimensions(this);
			}
			this.resolvedType = scope.createArrayType(qualifiedType, dimensions);
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
