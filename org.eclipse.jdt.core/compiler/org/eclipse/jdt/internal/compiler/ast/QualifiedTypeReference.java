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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

public class QualifiedTypeReference extends TypeReference {

	public char[][] tokens;
	public long[] sourcePositions;

	public QualifiedTypeReference(char[][] sources , long[] poss) {
		
		tokens = sources ;
		sourcePositions = poss ;
		sourceStart = (int) (sourcePositions[0]>>>32) ;
		sourceEnd = (int)(sourcePositions[sourcePositions.length-1] & 0x00000000FFFFFFFFL ) ;
	}
		
	public TypeReference copyDims(int dim){
		//return a type reference copy of me with some dimensions
		//warning : the new type ref has a null binding
		return new ArrayQualifiedTypeReference(tokens, dim, sourcePositions);
	}
	
	protected TypeBinding getTypeBinding(Scope scope) {
		
		if (this.resolvedType != null)
			return this.resolvedType;
		try {
			return scope.getType(this.tokens, this.tokens.length);
		} catch (AbortCompilation e) {
			e.updateContext(this, scope.referenceCompilationUnit().compilationResult);
			throw e;
		}
	}
	
	public char[][] getTypeName(){
	
		return tokens;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(tokens[i]);
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
