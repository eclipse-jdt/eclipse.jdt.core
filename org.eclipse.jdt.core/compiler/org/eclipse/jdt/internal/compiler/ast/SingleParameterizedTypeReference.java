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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class SingleParameterizedTypeReference extends ArrayTypeReference {

	public TypeReference[] typeArguments;
	
	public SingleParameterizedTypeReference(char[] name, TypeReference[] typeArguments, int dim, long pos){
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

	public TypeBinding getTypeBinding(Scope scope) {
		if (this.resolvedType != null) return resolvedType;
		int paramCount = this.typeArguments.length;
		TypeBinding[] typeArgumentBindings = new TypeBinding[paramCount];
		for (int i = 0; i < paramCount; i++){
			typeArgumentBindings[i] = this.typeArguments[i].getTypeBinding(scope);
		}
		TypeBinding type = scope.getType(token);
		// per construction, can only be a ReferenceBinding
		ReferenceBinding rawType = (ReferenceBinding) type;
		this.resolvedType = scope.createParameterizedType(rawType, typeArgumentBindings);
		if (this.dimensions > 0){
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
		for (int i= 0 ; i < dimensions ; i++) {
			output.append("[]"); //$NON-NLS-1$
		}
		return output;
	}
}
