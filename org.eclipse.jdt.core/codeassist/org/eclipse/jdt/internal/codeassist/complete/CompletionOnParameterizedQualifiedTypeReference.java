/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a type reference containing the completion identifier as part
 * of a parameterized qualified name.
 * e.g.
 *
 *	class X extends Y<Z>.W[cursor]
 *
 *	---> class X extends <CompleteOnType:Y<Z>.W>
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class CompletionOnParameterizedQualifiedTypeReference extends ParameterizedQualifiedTypeReference {
	public static final int TYPE = 0;
	public static final int CLASS = 1;
	public static final int INTERFACE = 2;
	public static final int EXCEPTION = 3;
	
	private int kind = TYPE;
	public char[] completionIdentifier;
	/**
	 * @param tokens
	 * @param typeArguments
	 * @param positions
	 */
	public CompletionOnParameterizedQualifiedTypeReference(char[][] tokens,	TypeReference[][] typeArguments, char[] completionIdentifier, long[] positions) {
		this(tokens, typeArguments, completionIdentifier, positions, TYPE);
	}
	
	/**
	 * @param tokens
	 * @param typeArguments
	 * @param positions
	 * @param kind
	 */
	public CompletionOnParameterizedQualifiedTypeReference(char[][] tokens,	TypeReference[][] typeArguments, char[] completionIdentifier, long[] positions, int kind) {
		super(tokens, typeArguments, 0, positions);
		this.completionIdentifier = completionIdentifier;
		this.kind = kind;
	}
	
	public boolean isClass(){
		return this.kind == CLASS;
	}
	
	public boolean isInterface(){
		return this.kind == INTERFACE;
	}
	
	public boolean isException(){
		return this.kind == EXCEPTION;
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		super.resolveType(scope);
		throw new CompletionNodeFound(this, this.resolvedType, scope);
	}
	
	public TypeBinding resolveType(ClassScope scope) {
		super.resolveType(scope);
		throw new CompletionNodeFound(this, this.resolvedType, scope);
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		switch (this.kind) {
			case CLASS :
				output.append("<CompleteOnClass:");//$NON-NLS-1$
				break;
			case INTERFACE :
				output.append("<CompleteOnInterface:");//$NON-NLS-1$
				break;
			case EXCEPTION :
				output.append("<CompleteOnException:");//$NON-NLS-1$
				break;
			default :
				output.append("<CompleteOnType:");//$NON-NLS-1$
				break;
		}
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
		output.append('.').append(completionIdentifier).append('>'); 
		return output;
	}	
}
