/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
 *                          Bug 384687 - [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ReferenceExpression extends FunctionalExpression {
	
	public Expression lhs;
	public TypeReference [] typeArguments;
	public SingleNameReference method; // == null ? "::new" : "::method"
	
	private TypeBinding receiverType;
	private boolean haveReceiver;
	public TypeBinding[] resolvedTypeArguments;
	
	public ReferenceExpression(Expression lhs, TypeReference [] typeArguments, SingleNameReference method, int sourceEnd) {
		this.lhs = lhs;
		this.typeArguments = typeArguments;
		this.method = method;
		this.sourceStart = lhs.sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public TypeBinding resolveType(BlockScope blockScope) {
		super.resolveType(blockScope);

		this.receiverType = this.lhs.resolveType(blockScope);
		this.haveReceiver = true;
		if (this.lhs instanceof NameReference) {
			if ((this.lhs.bits & ASTNode.RestrictiveFlagMASK) == Binding.TYPE) {
				this.haveReceiver = false;
				if (this.receiverType.isRawType())
					blockScope.problemReporter().rawTypeReference(this.lhs, this.receiverType);
			}
		} else if (this.lhs instanceof TypeReference) {
			this.haveReceiver = false;
		}

		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			boolean argHasError = blockScope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5;
			this.resolvedTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				TypeReference typeReference = this.typeArguments[i];
				if ((this.resolvedTypeArguments[i] = typeReference.resolveType(blockScope, true /* check bounds*/)) == null) {
					argHasError = true;
				}
				if (argHasError && typeReference instanceof Wildcard) {
					blockScope.problemReporter().illegalUsageOfWildcard(typeReference);
				}
			}
		}
		
		if (this.receiverType == null || !this.receiverType.isValidBinding()) 
			return this.resolvedType;
		
		if (this.receiverType.isBaseType()) {
			blockScope.problemReporter().errorNoMethodFor(this.lhs, this.receiverType, this.method.token, this.descriptor != null ? this.descriptor.parameters : Binding.NO_TYPES);
			return null;
		}
		
		if (isConstructorReference() && !this.receiverType.canBeInstantiated()) {
			blockScope.problemReporter().cannotInstantiate(this.lhs, this.receiverType);
			return this.resolvedType;
		}


		return this.resolvedType;
	}
	
	public StringBuffer printExpression(int tab, StringBuffer output) {
		
		this.lhs.print(0, output);
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
	
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			
			this.lhs.traverse(visitor, blockScope);
			
			int length = this.typeArguments == null ? 0 : this.typeArguments.length;
			for (int i = 0; i < length; i++) {
				this.typeArguments[i].traverse(visitor, blockScope);
			}
			
			if (this.method != null)
				this.method.traverse(visitor, blockScope);

		}
		visitor.endVisit(this, blockScope);
	}
}