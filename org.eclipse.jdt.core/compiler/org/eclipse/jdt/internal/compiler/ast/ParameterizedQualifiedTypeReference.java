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

/**
 * Syntactic representation of a reference to a generic type.
 * Note that it might also have a dimension.
 */
public class ParameterizedQualifiedTypeReference extends ArrayQualifiedTypeReference {

	public TypeReference[] typeArguments;

	/**
	 * @param tokens
	 * @param dim
	 * @param positions
	 */
	public ParameterizedQualifiedTypeReference(char[][] tokens, TypeReference[] typeArguments, int dim, long[] positions) {
		super(tokens, dim, positions);
		this.typeArguments = typeArguments;
	}
}
