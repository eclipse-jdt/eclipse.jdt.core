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
 *	   Stephan Herrmann - Contribution for
 *							bug 402028 - [1.8][compiler] null analysis for reference expressions 
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class ReferenceExpression extends FunctionalExpression implements InvocationSite {
	
	public Expression lhs;
	public TypeReference [] typeArguments;
	public SingleNameReference method;
	
	private TypeBinding receiverType;
	private boolean haveReceiver;
	public TypeBinding[] resolvedTypeArguments;
	private boolean typeArgumentsHaveErrors;
	
	public ReferenceExpression(CompilationResult compilationResult, Expression lhs, TypeReference [] typeArguments, SingleNameReference method, int sourceEnd) {
		super(compilationResult);
		this.lhs = lhs;
		this.typeArguments = typeArguments;
		this.method = method;
		this.sourceStart = lhs.sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// static methods with receiver value never get here
		if (this.haveReceiver) {
			this.lhs.checkNPE(currentScope, flowContext, flowInfo);
			this.lhs.analyseCode(currentScope, flowContext, flowInfo, true);
		}
		return flowInfo;
	}

	public TypeBinding resolveType(BlockScope scope) {
		
		final CompilerOptions compilerOptions = scope.compilerOptions();
		TypeBinding lhsType;
    	if (this.constant != Constant.NotAConstant) {
    		this.constant = Constant.NotAConstant;
    		this.enclosingScope = scope;
    		if (isConstructorReference())
    			this.lhs.bits |= ASTNode.IgnoreRawTypeCheck; // raw types in constructor references are to be treated as though <> were specified.

    		lhsType = this.lhs.resolveType(scope);
    		if (this.typeArguments != null) {
    			int length = this.typeArguments.length;
    			this.typeArgumentsHaveErrors = compilerOptions.sourceLevel < ClassFileConstants.JDK1_5;
    			this.resolvedTypeArguments = new TypeBinding[length];
    			for (int i = 0; i < length; i++) {
    				TypeReference typeReference = this.typeArguments[i];
    				if ((this.resolvedTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
    					this.typeArgumentsHaveErrors = true;
    				}
    				if (this.typeArgumentsHaveErrors && typeReference instanceof Wildcard) { // resolveType on wildcard always return null above, resolveTypeArgument is the real workhorse.
    					scope.problemReporter().illegalUsageOfWildcard(typeReference);
    				}
    			}
    			if (this.typeArgumentsHaveErrors)
    				return this.resolvedType;
    		}
    	} else {
    		if (this.typeArgumentsHaveErrors)
				return this.resolvedType;
    		lhsType = this.lhs.resolvedType;
    	}

    	if (this.expectedType == null && this.expressionContext == INVOCATION_CONTEXT) {
			return new PolyTypeBinding(this);
		}
		super.resolveType(scope);
		
    	if (lhsType == null || !lhsType.isValidBinding()) 
			return null;
		
		final TypeBinding[] descriptorParameters = this.descriptor != null ? this.descriptor.parameters : Binding.NO_PARAMETERS;
		final char[] selector = this.method.token;
		if (lhsType.isBaseType()) {
			scope.problemReporter().errorNoMethodFor(this.lhs, lhsType, selector, descriptorParameters);
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
        	if (isStatic && (this.haveReceiver || this.receiverType.isParameterizedType())) {
    			scope.problemReporter().methodMustBeAccessedStatically(this, someMethod);
    			return null;
    		}
        	if (!this.haveReceiver) {
        		if (!isStatic && !someMethod.isConstructor()) {
        			scope.problemReporter().methodMustBeAccessedWithInstance(this, someMethod);
        			return null;
        		}
        	} 
        } else {
        	if (this.lhs instanceof NameReference && !this.haveReceiver && isMethodReference() && this.receiverType.isRawType()) {
        		if ((this.lhs.bits & ASTNode.IgnoreRawTypeCheck) == 0 && compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
        			scope.problemReporter().rawTypeReference(this.lhs, this.receiverType);
        		}
        	}
        }
        
        MethodBinding anotherMethod = null;
        if (!this.haveReceiver && isMethodReference && parametersLength > 0) {
        	final TypeBinding potentialReceiver = descriptorParameters[0];
        	if (potentialReceiver.isCompatibleWith(this.receiverType, scope)) {
        		TypeBinding typeToSearch = this.receiverType;
        		if (this.receiverType.isRawType()) {
        			TypeBinding superType = potentialReceiver.findSuperTypeOriginatingFrom(this.receiverType);
        			if (superType != null)
        				typeToSearch = superType;
        		}
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

        this.method.binding = this.binding = someMethod != null && someMethod.isValidBinding() ? someMethod : 
        											anotherMethod != null && anotherMethod.isValidBinding() ? anotherMethod : null;

        if (this.binding == null) {
        	char [] visibleName = isConstructorReference() ? this.receiverType.sourceName() : selector;
        	scope.problemReporter().danglingReference(this, this.receiverType, visibleName, descriptorParameters);
			return null;
        }
        
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=382350#c2, I.super::abstractMethod will be handled there.

        if (this.binding.isAbstract() && this.lhs.isSuper())
        	scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, this.binding);
        
        if (this.binding.isStatic() && this.binding.declaringClass != this.receiverType)
			scope.problemReporter().indirectAccessToStaticMethod(this, this.binding);
    
    	if (isMethodUseDeprecated(this.binding, scope, true))
    		scope.problemReporter().deprecatedMethod(this.binding, this);

    	if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES)
    		scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.resolvedTypeArguments, this.typeArguments);
    	
    	if ((this.binding.tagBits & TagBits.HasMissingType) != 0)
    		scope.problemReporter().missingTypeInMethod(this, this.binding);
    	

        // OK, we have a compile time declaration, see if it passes muster.
        TypeBinding [] methodExceptions = this.binding.thrownExceptions;
        TypeBinding [] kosherExceptions = this.descriptor.thrownExceptions;
        next: for (int i = 0, iMax = methodExceptions.length; i < iMax; i++) {
        	for (int j = 0, jMax = kosherExceptions.length; j < jMax; j++) {
        		if (methodExceptions[i].isCompatibleWith(kosherExceptions[j], scope))
        			continue next;
        	}
        	scope.problemReporter().unhandledException(methodExceptions[i], this);
        }
        
    	if (checkInvocationArguments(scope, null, this.receiverType, this.binding, null, descriptorParameters, false, this))
    		this.bits |= ASTNode.Unchecked;

    	if (this.descriptor.returnType.id != TypeIds.T_void) {
    		// from 1.5 source level on, array#clone() returns the array type (but binding still shows Object)
    		TypeBinding returnType = null;
    		if (this.binding == scope.environment().arrayClone || this.binding.isConstructor()) {
    			returnType = this.receiverType;
    		} else {
    			if ((this.bits & ASTNode.Unchecked) != 0 && this.resolvedTypeArguments == null) {
    				returnType = this.binding.returnType;
    				if (returnType != null) {
    					returnType = scope.environment().convertToRawType(returnType.erasure(), true);
    				}
    			} else {
    				returnType = this.binding.returnType;
    				if (returnType != null) {
    					returnType = returnType.capture(scope, this.sourceEnd);
    				}
    			}
    		}
    		if (!returnType.isCompatibleWith(this.descriptor.returnType, scope) && !isBoxingCompatible(returnType, this.descriptor.returnType, this, scope)) {
    			scope.problemReporter().incompatibleReturnType(this, this.binding, this.descriptor.returnType);
    			this.method.binding = this.binding = null;
    		}
    	}

    	return this.resolvedType; // Phew !
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

	public boolean isCompatibleWith(TypeBinding left, Scope scope) {
		// 15.28.1
		final MethodBinding sam = left.getSingleAbstractMethod(scope);
		if (sam == null || !sam.isValidBinding())
			return false;
		boolean isCompatible;
		setExpectedType(left);
		IErrorHandlingPolicy oldPolicy = this.enclosingScope.problemReporter().switchErrorHandlingPolicy(silentErrorHandlingPolicy);
		try {
			this.method.binding = this.binding = null;
			resolveType(this.enclosingScope);
		} finally {
			this.enclosingScope.problemReporter().switchErrorHandlingPolicy(oldPolicy);
			isCompatible = this.binding != null && this.binding.isValidBinding();
			this.method.binding = this.binding = null;
		}
		return isCompatible;
	}
}