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
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnParameterizedQualifiedTypeReference extends ParameterizedQualifiedTypeReference {
	public SelectionOnParameterizedQualifiedTypeReference(char[][] previousIdentifiers, char[] selectionIdentifier, TypeReference[][] typeArguments, long[] positions) {
		super(
			CharOperation.arrayConcat(previousIdentifiers, selectionIdentifier),
			typeArguments,
			0,
			positions);
		int length =  this.typeArguments.length;
		System.arraycopy(this.typeArguments, 0, this.typeArguments = new TypeReference[length + 1][], 0, length);
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		super.resolveType(scope);
		throw new SelectionNodeFound(this.resolvedType);
	}
	
	public TypeBinding resolveType(ClassScope scope) {
		super.resolveType(scope);
		throw new SelectionNodeFound(this.resolvedType);
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("<SelectOnType:");//$NON-NLS-1$
		int length = tokens.length - 1;
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
		output.append('.').append(tokens[length]).append('>'); 
		return output;
	}
}
