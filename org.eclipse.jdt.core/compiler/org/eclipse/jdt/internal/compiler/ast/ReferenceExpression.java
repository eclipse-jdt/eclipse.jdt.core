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
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

public class ReferenceExpression extends NullLiteral { // For the time being.
	
	protected NameReference name;
	protected TypeReference type;
	protected Expression primary;
	
	protected TypeReference [] typeArguments;
	
	protected SingleNameReference method; // == null ? "::new" : "::method"
	
	public ReferenceExpression(NameReference name, TypeReference[] typeArguments, int sourceEnd) {
		super(name.sourceStart, sourceEnd);
		this.name = name;
		this.typeArguments = typeArguments;
		this.method = null;
	}

	public ReferenceExpression(NameReference name, TypeReference[] typeArguments, SingleNameReference method) {
		super(name.sourceStart, method.sourceEnd);
		this.name = name;
		this.typeArguments = typeArguments;
		this.method = method;
	}

	public ReferenceExpression(Expression primary, TypeReference [] typeArguments, SingleNameReference method) {
		super(primary.sourceStart, method.sourceEnd);
		this.primary = primary;
		this.typeArguments = typeArguments;
		this.method = method;
	}

	public ReferenceExpression(TypeReference type, TypeReference[] typeArguments, SingleNameReference method) {
		super(type.sourceStart, method.sourceEnd);
		this.type = type;
		this.typeArguments = typeArguments;
		this.method = method;
	}

	public ReferenceExpression(TypeReference type, TypeReference[] typeArguments, int sourceEnd) {
		super(type.sourceStart, sourceEnd);
		this.type = type;
		this.typeArguments = typeArguments;
		this.method = null;
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
		if (this.typeArguments != null) {
			output.append('<');
			int max = this.typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				this.typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			this.typeArguments[max].print(0, output);
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
}