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

	protected TypeBinding findNextTypeBinding(int tokenIndex, Scope scope, PackageBinding packageBinding) {
		try {
		    if (this.resolvedType == null) {
				this.resolvedType = scope.getType(this.tokens[tokenIndex], packageBinding);
		    } else {
			    this.resolvedType = scope.getMemberType(this.tokens[tokenIndex], (ReferenceBinding) this.resolvedType);
				if (this.resolvedType instanceof ProblemReferenceBinding) {
					ProblemReferenceBinding problemBinding = (ProblemReferenceBinding) this.resolvedType;
					this.resolvedType = new ProblemReferenceBinding(
						org.eclipse.jdt.core.compiler.CharOperation.subarray(this.tokens, 0, tokenIndex + 1),
						problemBinding.original,
						this.resolvedType.problemId());
				}
			}
		    return this.resolvedType;
		} catch (AbortCompilation e) {
			e.updateContext(this, scope.referenceCompilationUnit().compilationResult);
			throw e;
		}
	}

	protected TypeBinding getTypeBinding(Scope scope) {
		
		if (this.resolvedType != null)
			return this.resolvedType;

		Binding binding = scope.getPackage(this.tokens);
		if (binding != null && !binding.isValidBinding())
			return (ReferenceBinding) binding; // not found

	    PackageBinding packageBinding = binding == null ? null : (PackageBinding) binding;
	    boolean isClassScope = scope.kind == Scope.CLASS_SCOPE;
	    ReferenceBinding qualifiedType = null;
		for (int i = packageBinding == null ? 0 : packageBinding.compoundName.length, max = this.tokens.length; i < max; i++) {
			findNextTypeBinding(i, scope, packageBinding);
			if (!this.resolvedType.isValidBinding())
				return this.resolvedType;
			
			if (isClassScope)
				if (((ClassScope) scope).detectCycle(this.resolvedType, this, null)) // must connect hierarchy to find inherited member types
					return null;
			ReferenceBinding currentType = (ReferenceBinding) this.resolvedType;
			if (currentType.isGenericType()) {
				qualifiedType = scope.environment().createRawType(currentType, qualifiedType);
			} else {
				qualifiedType = (qualifiedType != null && (qualifiedType.isRawType() || qualifiedType.isParameterizedType()))
										? scope.createParameterizedType(currentType, null, qualifiedType)
										: currentType;
			}
		}
		this.resolvedType = qualifiedType;
		return this.resolvedType;
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
