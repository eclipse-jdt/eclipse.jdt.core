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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ArrayTypeReference extends SingleTypeReference {
	public int dimensions;

	/**
	 * ArrayTypeReference constructor comment.
	 * @param source char[]
	 * @param dimensions int
	 * @param pos int
	 */
	public ArrayTypeReference(char[] source, int dimensions, long pos) {
		
		super(source, pos);
		this.dimensions = dimensions ;
	}
	
	public int dimensions() {
		
		return dimensions;
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
		return new char[][]{ CharOperation.concat(token, dimChars) };
	}	
	protected TypeBinding getTypeBinding(Scope scope) {
		
		if (this.resolvedType != null) return this.resolvedType;
		if (dimensions > 255) {
			scope.problemReporter().tooManyDimensions(this);
		}
		TypeBinding leafComponentType = scope.getType(token);
		return scope.createArrayType(leafComponentType, dimensions);
	
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
	
		super.printExpression(indent, output)  ;
		for (int i= 0 ; i < dimensions ; i++) {
			output.append("[]"); //$NON-NLS-1$
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
}
