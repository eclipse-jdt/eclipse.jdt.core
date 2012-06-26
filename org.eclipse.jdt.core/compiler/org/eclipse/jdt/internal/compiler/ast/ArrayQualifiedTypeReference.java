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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

public class ArrayQualifiedTypeReference extends QualifiedTypeReference {
	int dimensions;
	Annotation[][] annotationsOnDimensions;  // jsr308 style type annotations on dimensions

	public ArrayQualifiedTypeReference(char[][] sources , int dim, long[] poss) {

		super( sources , poss);
		this.dimensions = dim ;
		this.annotationsOnDimensions = null; 
	}

	public ArrayQualifiedTypeReference(char[][] sources, int dim, Annotation[][] annotationsOnDimensions, long[] poss) {
		this(sources, dim, poss);
		this.annotationsOnDimensions = annotationsOnDimensions;
		this.bits |= ASTNode.HasTypeAnnotations;
	}

	public int dimensions() {

		return this.dimensions;
	}
	
	public Annotation[][] getAnnotationsOnDimensions() {
		return this.annotationsOnDimensions;
	}

	/**
	 * @return char[][]
	 */
	public char [][] getParameterizedTypeName(){
		int dim = this.dimensions;
		char[] dimChars = new char[dim*2];
		for (int i = 0; i < dim; i++) {
			int index = i*2;
			dimChars[index] = '[';
			dimChars[index+1] = ']';
		}
		int length = this.tokens.length;
		char[][] qParamName = new char[length][];
		System.arraycopy(this.tokens, 0, qParamName, 0, length-1);
		qParamName[length-1] = CharOperation.concat(this.tokens[length-1], dimChars);
		return qParamName;
	}

	protected TypeBinding getTypeBinding(Scope scope) {

		if (this.resolvedType != null)
			return this.resolvedType;
		if (this.dimensions > 255) {
			scope.problemReporter().tooManyDimensions(this);
		}
		LookupEnvironment env = scope.environment();
		try {
			env.missingClassFileLocation = this;
			TypeBinding leafComponentType = super.getTypeBinding(scope);
			if (leafComponentType != null) {
				return this.resolvedType = scope.createArrayType(leafComponentType, this.dimensions);
			}
			return null;
		} catch (AbortCompilation e) {
			e.updateContext(this, scope.referenceCompilationUnit().compilationResult);
			throw e;
		} finally {
			env.missingClassFileLocation = null;
		}
	}

	protected TypeBinding internalResolveType(Scope scope) {
		TypeBinding internalResolveType = super.internalResolveType(scope);
		if (this.annotationsOnDimensions != null) {
			switch(scope.kind) {
				case Scope.BLOCK_SCOPE :
				case Scope.METHOD_SCOPE :
					for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
						Annotation[] annotationsOnDimension = this.annotationsOnDimensions[i];
						resolveAnnotations((BlockScope) scope, annotationsOnDimension, new Annotation.TypeUseBinding(Binding.TYPE_USE));
					}
					break;
			}
		}
		return internalResolveType;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){

		super.printExpression(indent, output);
		if ((this.bits & IsVarArgs) != 0) {
			for (int i= 0 ; i < this.dimensions - 1; i++) {
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
					output.append(' ');
					printAnnotations(this.annotationsOnDimensions[i], output);
					output.append(' ');
				}
				output.append("[]"); //$NON-NLS-1$
			}
			if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[this.dimensions - 1] != null) {
				output.append(' ');
				printAnnotations(this.annotationsOnDimensions[this.dimensions - 1], output);
				output.append(' ');
			}
			output.append("..."); //$NON-NLS-1$
		} else {
			for (int i= 0 ; i < this.dimensions; i++) {
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
					output.append(" "); //$NON-NLS-1$
					printAnnotations(this.annotationsOnDimensions[i], output);
					output.append(" "); //$NON-NLS-1$
				}
				output.append("[]"); //$NON-NLS-1$
			}
		}
		return output;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (this.annotationsOnDimensions != null) {
				for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
					Annotation[] annotations2 = this.annotationsOnDimensions[i];
					for (int j = 0, max2 = annotations2.length; j < max2; j++) {
						Annotation annotation = annotations2[j];
						annotation.traverse(visitor, scope);
					}
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (this.annotationsOnDimensions != null) {
				for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
					Annotation[] annotations2 = this.annotationsOnDimensions[i];
					for (int j = 0, max2 = annotations2.length; j < max2; j++) {
						Annotation annotation = annotations2[j];
						annotation.traverse(visitor, scope);
					}
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	protected void resolveAnnotations(BlockScope scope) {
		super.resolveAnnotations(scope);
		if (this.annotationsOnDimensions != null) {
			for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
				Annotation[] annotationsOnDimension = this.annotationsOnDimensions[i];
				resolveAnnotations(scope, annotationsOnDimension, new Annotation.TypeUseBinding(Binding.TYPE_USE));
			}
		}
	}
}
