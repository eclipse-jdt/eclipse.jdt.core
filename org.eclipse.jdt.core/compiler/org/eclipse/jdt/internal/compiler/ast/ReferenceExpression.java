/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ReferenceExpression extends FunctionalExpression {
	
	protected NameReference name;
	protected TypeReference type;
	protected Expression primary;
	
	protected TypeReference [] typeParameters;
	
	protected SingleNameReference method; // == null ? "::new" : "::method"
	
	public ReferenceExpression(NameReference name, TypeReference[] typeArguments, int sourceEnd) {
		this.name = name;
		this.typeParameters = typeArguments;
		this.method = null;
		this.sourceStart = name.sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public ReferenceExpression(NameReference name, TypeReference[] typeArguments, SingleNameReference method) {
		this.name = name;
		this.typeParameters = typeArguments;
		this.method = method;
		this.sourceStart = name.sourceStart;
		this.sourceEnd = method.sourceEnd;
	}

	public ReferenceExpression(Expression primary, TypeReference [] typeArguments, SingleNameReference method) {
		this.primary = primary;
		this.typeParameters = typeArguments;
		this.method = method;
		this.sourceStart = primary.sourceStart;
		this.sourceEnd = method.sourceEnd;
	}

	public ReferenceExpression(TypeReference type, TypeReference[] typeArguments, SingleNameReference method) {
		this.type = type;
		this.typeParameters = typeArguments;
		this.method = method;
		this.sourceStart = type.sourceStart;
		this.sourceEnd = method.sourceEnd;
	}

	public ReferenceExpression(TypeReference type, TypeReference[] typeArguments, int sourceEnd) {
		this.type = type;
		this.typeParameters = typeArguments;
		this.method = null;
		this.sourceStart = type.sourceStart;
		this.sourceEnd = sourceEnd;
	}
	
	public TypeBinding resolveType(BlockScope blockScope) {
		super.resolveType(blockScope);
		return this.functionalInterfaceType;
	}
	
	public StringBuffer printExpression(int tab, StringBuffer output) {
		
		if (this.type != null) {
			this.type.print(0, output);
		} else if (this.name != null) {
			this.name.print(0, output);
		} else {
			this.primary.print(0, output);
		}
		output.append("::"); //$NON-NLS-1$
		if (this.typeParameters != null) {
			output.append('<');
			int max = this.typeParameters.length - 1;
			for (int j = 0; j < max; j++) {
				this.typeParameters[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			this.typeParameters[max].print(0, output);
			output.append('>');
		}
		if (this.method == null) {
			output.append("new"); //$NON-NLS-1$	
		} else {
			this.method.print(0, output);
		}
		return output;
	}
	public boolean isConstructorReference() {
		return this.method == null;
	}
	public boolean isMethodReference() {
		return this.method != null;
	}
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {

			if (this.name != null)
				this.name.traverse(visitor, blockScope);

			if (this.type != null)
				this.type.traverse(visitor, blockScope);

			if (this.primary != null)
				this.primary.traverse(visitor, blockScope);

			int length = this.typeParameters == null ? 0 : this.typeParameters.length;
			for (int i = 0; i < length; i++) {
				this.typeParameters[i].traverse(visitor, blockScope);
			}

			if (this.method != null)
				this.method.traverse(visitor, blockScope);

		}
		visitor.endVisit(this, blockScope);
	}
}