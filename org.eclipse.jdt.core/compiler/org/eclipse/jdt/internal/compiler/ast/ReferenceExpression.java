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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ReferenceExpression extends FunctionalExpression implements InvocationSite {
	
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

	// Structured to report as many errors as possible in bail out situations.
	public TypeBinding resolveType(BlockScope scope) {
		super.resolveType(scope);

		if (isConstructorReference()) {
			this.lhs.bits |= ASTNode.IgnoreRawTypeCheck; // raw types in constructor references are to be treated as though <> were specified.
		}
		TypeBinding lhsType = this.lhs.resolveType(scope);
		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			boolean argHasError = scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5;
			this.resolvedTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				TypeReference typeReference = this.typeArguments[i];
				if ((this.resolvedTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
					argHasError = true;
				}
				if (argHasError && typeReference instanceof Wildcard) { // resolveType on wildcard always return null above, resolveTypeArgument is the real workhorse.
					scope.problemReporter().illegalUsageOfWildcard(typeReference);
				}
			}
			if (argHasError) {
				return this.resolvedType;
			}
		}
		
		if (lhsType == null || !lhsType.isValidBinding()) 
			return null;
		
		final TypeBinding[] descriptorParameters = this.descriptor != null ? this.descriptor.parameters : Binding.NO_PARAMETERS;
		final char[] selector = this.method.token;
		if (lhsType.isBaseType()) {
			scope.problemReporter().errorNoMethodFor(this.lhs, lhsType, selector, this.descriptor != null ? descriptorParameters : Binding.NO_TYPES);
			return null;
		}
		
		if (isConstructorReference() && !lhsType.canBeInstantiated()) {
			scope.problemReporter().cannotInstantiate(this.lhs, lhsType);
			return null;
		}
		
		/* 15.28: "It is a compile-time error if a method reference of the form super :: NonWildTypeArgumentsopt Identifier or of the form 
		   TypeName . super :: NonWildTypeArgumentsopt Identifier occurs in a static context.": This is nop since the primary when it resolves
		   itself will complain automatically.
		
		   15.28: "The immediately enclosing instance of an inner class instance (15.9.2) must be provided for a constructor reference by a lexically 
		   enclosing instance of this (8.1.3)", we will actually implement this check in code generation. Emulation path computation will fail if there
		   is no suitable enclosing instance. While this could be pulled up to here, leaving it to code generation is more consistent with Java 5,6,7 
		   modus operandi.
		*/
		
		// handle the special case of array construction first.
        final int parametersLength = descriptorParameters.length;
        if (isConstructorReference() && lhsType.isArrayType()) {
        	final TypeBinding leafComponentType = lhsType.leafComponentType();
			if (leafComponentType.isParameterizedType()) {
        		scope.problemReporter().illegalGenericArray(leafComponentType, this);
        		return null;
        	}
        	if (parametersLength != 1 || scope.parameterCompatibilityLevel(descriptorParameters[0], TypeBinding.INT) == Scope.NOT_COMPATIBLE) {
        		scope.problemReporter().invalidArrayConstructorReference(this, lhsType, descriptorParameters);
        		return null;
        	}
        	if (!lhsType.isCompatibleWith(this.descriptor.returnType)) {
        		scope.problemReporter().constructedArrayIncompatible(this, lhsType, this.descriptor.returnType);
        		return null;
        	}
        	return this.resolvedType; // No binding construction possible. Code generator will have to conjure up a rabbit.
        }
		
        this.receiverType = lhsType;
		
		this.haveReceiver = true;
		if (this.lhs instanceof NameReference) {
			if ((this.lhs.bits & ASTNode.RestrictiveFlagMASK) == Binding.TYPE) {
				this.haveReceiver = false;
				if (isMethodReference() && this.receiverType.isRawType())
					scope.problemReporter().rawTypeReference(this.lhs, this.receiverType);
			}
		} else if (this.lhs instanceof TypeReference) {
			this.haveReceiver = false;
		}

		/* For Reference expressions unlike other call sites, we always have a receiver _type_ since LHS of :: cannot be empty. 
		   LHS's resolved type == actual receiver type. All code below only when a valid descriptor is available.
		 */
        if (this.descriptor == null || !this.descriptor.isValidBinding())
        	return null;
        
        // 15.28.1
        final boolean isMethodReference = isMethodReference();
        MethodBinding someMethod = isMethodReference ? scope.getMethod(this.receiverType, selector, descriptorParameters, this) :
        											       scope.getConstructor((ReferenceBinding) this.receiverType, descriptorParameters, this);
        
        if (someMethod != null && someMethod.isValidBinding()) {
        	final boolean isStatic = someMethod.isStatic();
        	if (isStatic && (this.haveReceiver || this.receiverType instanceof ParameterizedTypeBinding)) {
    			scope.problemReporter().methodMustBeAccessedStatically(this, someMethod);
    			return null;
    		}
        	if (!this.haveReceiver) {
        		if (!isStatic && !someMethod.isConstructor()) {
        			scope.problemReporter().methodMustBeAccessedWithInstance(this, someMethod);
        			return null;
        		}
        	} 
        }
        
        MethodBinding anotherMethod = null;
        if (!this.haveReceiver && isMethodReference && parametersLength > 0) {
        	TypeBinding superType = descriptorParameters[0].findSuperTypeOriginatingFrom(this.receiverType);
        	if (superType != null) {
        		TypeBinding typeToSearch = this.receiverType.isRawType() ? superType : this.receiverType;
        		TypeBinding [] parameters = Binding.NO_PARAMETERS;
        		if (parametersLength > 1) {
        			parameters = new TypeBinding[parametersLength - 1];
        			System.arraycopy(descriptorParameters, 1, parameters, 0, parametersLength - 1);
        		}
        		anotherMethod = scope.getMethod(typeToSearch, selector, parameters, this);
        	}
        	if (anotherMethod != null && anotherMethod.isValidBinding() && anotherMethod.isStatic()) {
        		scope.problemReporter().methodMustBeAccessedStatically(this, anotherMethod);
        		return null;
        	}
        }
        
        if (someMethod != null && someMethod.isValidBinding() && anotherMethod != null && anotherMethod.isValidBinding()) {
        	scope.problemReporter().methodReferenceSwingsBothWays(this, anotherMethod, someMethod);
        	return null;
        }

        this.binding = someMethod != null && someMethod.isValidBinding() ? someMethod : anotherMethod != null && anotherMethod.isValidBinding() ? anotherMethod : null;
        if (this.binding == null) {
        	scope.problemReporter().danglingReference(this, this.receiverType, selector, descriptorParameters);
			return null;
        }
        
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=382350#c2, I.super::abstractMethod will be handled there.
        
        // OK, we have a compile time declaration, see if it passes muster.
        TypeBinding [] methodExceptions = this.binding.thrownExceptions;
        TypeBinding [] kosherExceptions = this.descriptor.thrownExceptions;
        boolean throwsTantrum = false;
        next: for (int i = 0, iMax = methodExceptions.length; i < iMax; i++) {
        	for (int j = 0, jMax = kosherExceptions.length; j < jMax; j++) {
        		if (methodExceptions[i].isCompatibleWith(kosherExceptions[j], scope))
        			continue next;
        	}
        	scope.problemReporter().unhandledException(methodExceptions[i], this);
        	throwsTantrum = true;
        }
        
        if (this.binding.isAbstract()) {
        	if (this.lhs instanceof SuperReference || this.lhs instanceof QualifiedSuperReference) {
        		scope.problemReporter().cannotReferToAbstractMethod(this, this.binding);
        		return null;
        	}
        }
        if (throwsTantrum)
        	return null;
        
        this.method.binding = this.binding;
 
        return this.resolvedType;
	}
	
	public final boolean isConstructorReference() {
		return CharOperation.equals(this.method.token,  ConstantPool.Init);
	}
	
	public final boolean isMethodReference() {
		return !CharOperation.equals(this.method.token,  ConstantPool.Init);
	}
	
	public TypeBinding[] genericTypeArguments() {
		return null;
	}

	public boolean isSuperAccess() {
		return false;
	}

	public boolean isTypeAccess() {
		return false;
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		return;
	}

	public void setDepth(int depth) {
		return;
	}

	public void setFieldIndex(int depth) {
		return;
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
		if (isConstructorReference())
			output.append("new"); //$NON-NLS-1$
		else 
			this.method.print(0, output);
		
		return output;
	}
		
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			
			this.lhs.traverse(visitor, blockScope);
			
			int length = this.typeArguments == null ? 0 : this.typeArguments.length;
			for (int i = 0; i < length; i++) {
				this.typeArguments[i].traverse(visitor, blockScope);
			}
			
			this.method.traverse(visitor, blockScope);

		}
		visitor.endVisit(this, blockScope);
	}
}