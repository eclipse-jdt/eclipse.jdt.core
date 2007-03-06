/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public abstract class TypeReference extends Expression {

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}

// allows us to trap completion & selection nodes
public void aboutToResolve(Scope scope) {
	// default implementation: do nothing
}
/*
 * Answer a base type reference (can be an array of base type).
 */
public static final TypeReference baseTypeReference(int baseType, int dim) {
	
	if (dim == 0) {
		switch (baseType) {
			case (TypeIds.T_void) :
				return new SingleTypeReference(TypeBinding.VOID.simpleName, 0);
			case (TypeIds.T_boolean) :
				return new SingleTypeReference(TypeBinding.BOOLEAN.simpleName, 0);
			case (TypeIds.T_char) :
				return new SingleTypeReference(TypeBinding.CHAR.simpleName, 0);
			case (TypeIds.T_float) :
				return new SingleTypeReference(TypeBinding.FLOAT.simpleName, 0);
			case (TypeIds.T_double) :
				return new SingleTypeReference(TypeBinding.DOUBLE.simpleName, 0);
			case (TypeIds.T_byte) :
				return new SingleTypeReference(TypeBinding.BYTE.simpleName, 0);
			case (TypeIds.T_short) :
				return new SingleTypeReference(TypeBinding.SHORT.simpleName, 0);
			case (TypeIds.T_int) :
				return new SingleTypeReference(TypeBinding.INT.simpleName, 0);
			default : //T_long	
				return new SingleTypeReference(TypeBinding.LONG.simpleName, 0);
		}
	}
	switch (baseType) {
		case (TypeIds.T_void) :
			return new ArrayTypeReference(TypeBinding.VOID.simpleName, dim, 0);
		case (TypeIds.T_boolean) :
			return new ArrayTypeReference(TypeBinding.BOOLEAN.simpleName, dim, 0);
		case (TypeIds.T_char) :
			return new ArrayTypeReference(TypeBinding.CHAR.simpleName, dim, 0);
		case (TypeIds.T_float) :
			return new ArrayTypeReference(TypeBinding.FLOAT.simpleName, dim, 0);
		case (TypeIds.T_double) :
			return new ArrayTypeReference(TypeBinding.DOUBLE.simpleName, dim, 0);
		case (TypeIds.T_byte) :
			return new ArrayTypeReference(TypeBinding.BYTE.simpleName, dim, 0);
		case (TypeIds.T_short) :
			return new ArrayTypeReference(TypeBinding.SHORT.simpleName, dim, 0);
		case (TypeIds.T_int) :
			return new ArrayTypeReference(TypeBinding.INT.simpleName, dim, 0);
		default : //T_long	
			return new ArrayTypeReference(TypeBinding.LONG.simpleName, dim, 0);
	}
}
public void checkBounds(Scope scope) {
	// only parameterized type references have bounds
}
public abstract TypeReference copyDims(int dim);
public int dimensions() {
	return 0;
}

public abstract char[] getLastToken();

/**
 * @return char[][]
 * TODO (jerome) should merge back into #getTypeName()
 */
public char [][] getParameterizedTypeName(){
	return getTypeName();
}
protected abstract TypeBinding getTypeBinding(Scope scope);
/**
 * @return char[][]
 */
public abstract char [][] getTypeName() ;
public boolean isTypeReference() {
	return true;
}
public TypeBinding resolveSuperType(ClassScope scope) {
	// assumes the implementation of resolveType(ClassScope) will call back to detect cycles
	if (resolveType(scope) == null) return null;

	if (this.resolvedType.isTypeVariable()) {
		this.resolvedType = new ProblemReferenceBinding(getTypeName(), (ReferenceBinding) this.resolvedType, ProblemReasons.IllegalSuperTypeVariable);
		reportInvalidType(scope);
		return null;
	}
	return this.resolvedType;
}

public final TypeBinding resolveType(BlockScope blockScope) {
	return resolveType(blockScope, true /* checkbounds if any */);
}

public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
	// handle the error here
	this.constant = Constant.NotAConstant;
	if (this.resolvedType != null) // is a shared type reference which was already resolved
		return this.resolvedType.isValidBinding() ? this.resolvedType : null; // already reported error

	TypeBinding type = this.resolvedType = getTypeBinding(scope);
	if (type == null)
		return null; // detected cycle while resolving hierarchy	
	if (!type.isValidBinding()) {
		reportInvalidType(scope);
		return null;
	}
	if (type.isArrayType() && ((ArrayBinding) type).leafComponentType == TypeBinding.VOID) {
		scope.problemReporter().cannotAllocateVoidArray(this);
		return null;
	}

	if (isTypeUseDeprecated(type, scope))
		reportDeprecatedType(type, scope);
	
	type = scope.environment().convertToRawType(type);
	if (type.leafComponentType().isRawType() 
			&& (this.bits & ASTNode.IgnoreRawTypeCheck) == 0 
			&& scope.compilerOptions().getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {	
		scope.problemReporter().rawTypeReference(this, type);
	}			
	return this.resolvedType = type;
}
public TypeBinding resolveType(ClassScope scope) {
	// handle the error here
	this.constant = Constant.NotAConstant;
	if (this.resolvedType != null) // is a shared type reference which was already resolved
		return this.resolvedType.isValidBinding() ? this.resolvedType : null; // already reported error

	TypeBinding type = this.resolvedType = getTypeBinding(scope);
	if (type == null)
		return null; // detected cycle while resolving hierarchy	
	if (!type.isValidBinding()) {
		reportInvalidType(scope);
		return null;
	}
	if (type.isArrayType() && ((ArrayBinding) type).leafComponentType == TypeBinding.VOID) {
		scope.problemReporter().cannotAllocateVoidArray(this);
		return null;
	}	
	if (isTypeUseDeprecated(type, scope))
		reportDeprecatedType(type, scope);
	
	type = scope.environment().convertToRawType(type);
	if (type.leafComponentType().isRawType() 
			&& (this.bits & ASTNode.IgnoreRawTypeCheck) == 0 
			&& scope.compilerOptions().getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
		scope.problemReporter().rawTypeReference(this, type);
	}			
	return this.resolvedType = type;	
}

public TypeBinding resolveTypeArgument(BlockScope blockScope, ReferenceBinding genericType, int rank) {
    return resolveType(blockScope, true /* check bounds*/);
}

public TypeBinding resolveTypeArgument(ClassScope classScope, ReferenceBinding genericType, int rank) {
    return resolveType(classScope);
}
	
protected void reportInvalidType(Scope scope) {
	scope.problemReporter().invalidType(this, this.resolvedType);
}
protected void reportDeprecatedType(TypeBinding type, Scope scope) {
	scope.problemReporter().deprecatedType(type, this);
}
public abstract void traverse(ASTVisitor visitor, BlockScope scope);
public abstract void traverse(ASTVisitor visitor, ClassScope scope);
}
