/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ArrayTypeReference extends SingleTypeReference {
	public int dimensions;
	public Annotation[][] annotationsOnDimensions; // jsr308 style type annotations on dimensions.
	public int originalSourceEnd;

	/**
	 * ArrayTypeReference constructor comment.
	 * @param source char[]
	 * @param dimensions int
	 * @param pos int
	 */
	public ArrayTypeReference(char[] source, int dimensions, long pos) {

		super(source, pos);
		this.originalSourceEnd = this.sourceEnd;
		this.dimensions = dimensions ;
		this.annotationsOnDimensions = null;
	}

	public ArrayTypeReference(char[] source, int dimensions, Annotation[][] annotationsOnDimensions, long pos) {
		this(source, dimensions, pos);
		this.annotationsOnDimensions = annotationsOnDimensions;
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
		return new char[][]{ CharOperation.concat(this.token, dimChars) };
	}
	protected TypeBinding getTypeBinding(Scope scope) {

		if (this.resolvedType != null) {
			return this.resolvedType;
		}
		if (this.dimensions > 255) {
			scope.problemReporter().tooManyDimensions(this);
		}
		TypeBinding leafComponentType = scope.getType(this.token);
		return scope.createArrayType(leafComponentType, this.dimensions);

	}

	public StringBuffer printExpression(int indent, StringBuffer output){

		super.printExpression(indent, output);
		if ((this.bits & IsVarArgs) != 0) {
			for (int i= 0 ; i < this.dimensions - 1; i++) {
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
					output.append(" "); //$NON-NLS-1$
					printAnnotations(this.annotationsOnDimensions[i], output);
				}
				output.append("[]"); //$NON-NLS-1$
			}
			if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[this.dimensions - 1] != null) {
				output.append(" "); //$NON-NLS-1$
				printAnnotations(this.annotationsOnDimensions[this.dimensions - 1], output);
			}
			output.append("..."); //$NON-NLS-1$
		} else {
			for (int i= 0 ; i < this.dimensions; i++) {
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
					output.append(" "); //$NON-NLS-1$
					printAnnotations(this.annotationsOnDimensions[i], output);
				}
				output.append("[]"); //$NON-NLS-1$
			}
		}
		return output;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {

		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
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
