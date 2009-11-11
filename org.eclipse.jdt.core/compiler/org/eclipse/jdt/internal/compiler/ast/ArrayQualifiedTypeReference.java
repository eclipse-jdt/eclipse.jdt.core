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
			return this.resolvedType = scope.createArrayType(leafComponentType, this.dimensions);
		} catch (AbortCompilation e) {
			e.updateContext(this, scope.referenceCompilationUnit().compilationResult);
			throw e;
		} finally {
			env.missingClassFileLocation = null;
		}
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
		if (this.annotationsOnDimensions != null) {
			for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
				Annotation[] annotationsOnDimension = this.annotationsOnDimensions[i];
				resolveAnnotations((BlockScope) scope, annotationsOnDimension, null);
			}
		}
		return super.internalResolveType(scope);
	}
}
