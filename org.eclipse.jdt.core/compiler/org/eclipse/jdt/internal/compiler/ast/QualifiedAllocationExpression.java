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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * Variation on allocation, where can optionally be specified any of:
 * - leading enclosing instance
 * - trailing anonymous type
 * - generic type arguments for generic constructor invocation
 */
public class QualifiedAllocationExpression extends AllocationExpression {
	
	//qualification may be on both side
	public Expression enclosingInstance;
	public TypeDeclaration anonymousType;
	public ReferenceBinding superTypeBinding;
	
	public QualifiedAllocationExpression() {
		// for subtypes
	}

	public QualifiedAllocationExpression(TypeDeclaration anonymousType) {
		this.anonymousType = anonymousType;
		anonymousType.allocation = this;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// analyse the enclosing instance
		if (this.enclosingInstance != null) {
			flowInfo = this.enclosingInstance.analyseCode(currentScope, flowContext, flowInfo);
		}
		
		// check captured variables are initialized in current context (26134)
		ReferenceBinding allocatedType = this.superTypeBinding == null ? this.binding.declaringClass : this.superTypeBinding;
		checkCapturedLocalInitializationIfNecessary(
			(ReferenceBinding) allocatedType.erasure(),
			currentScope, 
			flowInfo);
		
		// process arguments
		if (this.arguments != null) {
			for (int i = 0, count = this.arguments.length; i < count; i++) {
				flowInfo = this.arguments[i].analyseCode(currentScope, flowContext, flowInfo);
			}
		}

		// analyse the anonymous nested type
		if (this.anonymousType != null) {
			flowInfo = this.anonymousType.analyseCode(currentScope, flowContext, flowInfo);
		}

		// record some dependency information for exception types
		ReferenceBinding[] thrownExceptions;
		if (((thrownExceptions = this.binding.thrownExceptions).length) != 0) {
			// check exception handling
			flowContext.checkExceptionHandlers(
				thrownExceptions,
				this,
				flowInfo.unconditionalCopy(),
				currentScope);
		}
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		manageSyntheticAccessIfNecessary(currentScope, flowInfo);
		return flowInfo;
	}

	public Expression enclosingInstance() {

		return this.enclosingInstance;
	}

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		ReferenceBinding allocatedType = this.codegenBinding.declaringClass;
		codeStream.new_(allocatedType);
		if (valueRequired) {
			codeStream.dup();
		}
		// better highlight for allocation: display the type individually
		if (this.type != null) { // null for enum constant body
			codeStream.recordPositionsFrom(pc, this.type.sourceStart);
		} else {
			// push enum constant name and ordinal
			codeStream.ldc(String.valueOf(this.enumConstant.name));
			codeStream.generateInlinedValue(this.enumConstant.binding.id);
		}
		// handling innerclass instance allocation - enclosing instance arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticEnclosingInstanceValues(
				currentScope,
				allocatedType,
				enclosingInstance(),
				this);
		}
		// generate the arguments for constructor
		generateArguments(this.binding, this.arguments, currentScope, codeStream);
		// handling innerclass instance allocation - outer local arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticOuterArgumentValues(
				currentScope,
				allocatedType,
				this);
		}
		
		// invoke constructor
		if (this.syntheticAccessor == null) {
			codeStream.invokespecial(this.codegenBinding);
		} else {
			// synthetic accessor got some extra arguments appended to its signature, which need values
			for (int i = 0,
				max = this.syntheticAccessor.parameters.length - this.codegenBinding.parameters.length;
				i < max;
				i++) {
				codeStream.aconst_null();
			}
			codeStream.invokespecial(this.syntheticAccessor);
		}
		codeStream.generateImplicitConversion(this.implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);

		if (this.anonymousType != null) {
			this.anonymousType.generateCode(currentScope, codeStream);
		}
	}
	
	public boolean isSuperAccess() {

		// necessary to lookup super constructor of anonymous type
		return this.anonymousType != null;
	}
	
	/* Inner emulation consists in either recording a dependency 
	 * link only, or performing one level of propagation.
	 *
	 * Dependency mechanism is used whenever dealing with source target
	 * types, since by the time we reach them, we might not yet know their
	 * exact need.
	 */
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
		ReferenceBinding allocatedTypeErasure = (ReferenceBinding) this.binding.declaringClass.erasure();

		// perform some extra emulation work in case there is some and we are inside a local type only
		if (allocatedTypeErasure.isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (allocatedTypeErasure.isLocalType()) {
				((LocalTypeBinding) allocatedTypeErasure).addInnerEmulationDependent(currentScope, this.enclosingInstance != null);
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(allocatedTypeErasure, this.enclosingInstance != null);
			}
		}
		}
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (this.enclosingInstance != null)
			this.enclosingInstance.printExpression(0, output).append('.'); 
		super.printExpression(0, output);
		if (this.anonymousType != null) {
			this.anonymousType.print(indent, output);
		}
		return output;
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		// added for code assist...cannot occur with 'normal' code
		if (this.anonymousType == null && this.enclosingInstance == null) {
			return super.resolveType(scope);
		}

		// Propagate the type checking to the arguments, and checks if the constructor is defined.
		// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		// ClassInstanceCreationExpression ::= Name '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		
		this.constant = Constant.NotAConstant;
		TypeBinding enclosingInstanceType = null;
		TypeBinding receiverType = null;
		boolean hasError = false;
		boolean enclosingInstanceContainsCast = false;
		boolean argsContainCast = false;
		
		if (this.enclosingInstance != null) {
			if (this.enclosingInstance instanceof CastExpression) {
				this.enclosingInstance.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
				enclosingInstanceContainsCast = true;
			}
			if ((enclosingInstanceType = this.enclosingInstance.resolveType(scope)) == null){
				hasError = true;
			} else if (enclosingInstanceType.isBaseType() || enclosingInstanceType.isArrayType()) {
				scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
					enclosingInstanceType,
					this.enclosingInstance);
				hasError = true;
			} else if (this.type instanceof QualifiedTypeReference) {
				scope.problemReporter().illegalUsageOfQualifiedTypeReference((QualifiedTypeReference)this.type);
				hasError = true;
			} else {
				receiverType = ((SingleTypeReference) this.type).resolveTypeEnclosing(scope, (ReferenceBinding) enclosingInstanceType);
				if (receiverType != null && enclosingInstanceContainsCast) {
					CastExpression.checkNeedForEnclosingInstanceCast(scope, this.enclosingInstance, enclosingInstanceType, receiverType);
				}
			}
		} else {
			if (this.type == null) {
				// initialization of an enum constant
				receiverType = scope.enclosingSourceType();
			} else {
				receiverType = this.type.resolveType(scope, true /* check bounds*/);
				checkParameterizedAllocation: {
					if (receiverType == null) break checkParameterizedAllocation;
					if (this.type instanceof ParameterizedQualifiedTypeReference) { // disallow new X<String>.Y<Integer>()
						ReferenceBinding currentType = (ReferenceBinding)receiverType;
						do {
							// isStatic() is answering true for toplevel types
							if ((currentType.modifiers & ClassFileConstants.AccStatic) != 0) break checkParameterizedAllocation;
							if (currentType.isRawType()) break checkParameterizedAllocation;
						} while ((currentType = currentType.enclosingType())!= null);
						ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference) this.type;
						for (int i = qRef.typeArguments.length - 2; i >= 0; i--) {
							if (qRef.typeArguments[i] != null) {
								scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, receiverType);
								break;
							}
						}
					}
				}				
			}			
		}
		if (receiverType == null) {
			hasError = true;
		} else if (((ReferenceBinding) receiverType).isFinal()) {
			if (this.anonymousType != null) {
				if (!receiverType.isEnum()) {
					scope.problemReporter().anonymousClassCannotExtendFinalClass(this.type, receiverType);
					hasError = true;
				}
			} else if (!receiverType.canBeInstantiated()) {
				scope.problemReporter().cannotInstantiate(this.type, receiverType);
				return this.resolvedType = receiverType;
			}
		}
		// resolve type arguments (for generic constructor call)
		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			this.genericTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				TypeReference typeReference = this.typeArguments[i];				
				TypeBinding argType = typeReference.resolveType(scope, true /* check bounds*/);
				if (argType == null) {
					if (typeReference instanceof Wildcard) {
						scope.problemReporter().illegalUsageOfWildcard(typeReference);
					}
					return null; // error already reported
				}
				this.genericTypeArguments[i] = argType;
			}
		}
		
		// will check for null after args are resolved
		TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
		if (this.arguments != null) {
			int length = this.arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				Expression argument = this.arguments[i];
				if (argument instanceof CastExpression) {
					argument.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
					argsContainCast = true;
				}
				if ((argumentTypes[i] = argument.resolveType(scope)) == null){
					hasError = true;
				}
			}
		}
		// limit of fault-tolerance
		if (hasError) return this.resolvedType = receiverType;
		if (this.anonymousType == null) {
			// qualified allocation with no anonymous type
			if (!receiverType.canBeInstantiated()) {
				scope.problemReporter().cannotInstantiate(this.type, receiverType);
				return this.resolvedType = receiverType;
			}
			ReferenceBinding allocationType = (ReferenceBinding) receiverType;
			if ((this.binding = scope.getConstructor(allocationType, argumentTypes, this)).isValidBinding()) {
				if (isMethodUseDeprecated(this.binding, scope, true)) {
					scope.problemReporter().deprecatedMethod(this.binding, this);
				}
				checkInvocationArguments(scope, null, allocationType, this.binding, this.arguments, argumentTypes, argsContainCast, this);
			} else {
				if (this.binding.declaringClass == null) {
					this.binding.declaringClass = allocationType;
				}
				scope.problemReporter().invalidConstructor(this, this.binding);
				return this.resolvedType = receiverType;
			}

			// The enclosing instance must be compatible with the innermost enclosing type
			ReferenceBinding expectedType = this.binding.declaringClass.enclosingType();
			if (expectedType != enclosingInstanceType) // must call before computeConversion() and typeMismatchError()
				scope.compilationUnitScope().recordTypeConversion(expectedType, enclosingInstanceType);
			if (enclosingInstanceType.isCompatibleWith(expectedType) || scope.isBoxingCompatibleWith(enclosingInstanceType, expectedType)) {
				this.enclosingInstance.computeConversion(scope, expectedType, enclosingInstanceType);
				return this.resolvedType = receiverType;
			}
			scope.problemReporter().typeMismatchError(enclosingInstanceType, expectedType, this.enclosingInstance);
			return this.resolvedType = receiverType;
		}

		if (receiverType.isTypeVariable()) {
			receiverType = new ProblemReferenceBinding(receiverType.sourceName(), (ReferenceBinding)receiverType, ProblemReasons.IllegalSuperTypeVariable);
			scope.problemReporter().invalidType(this, receiverType);
			return null;
		} else if (this.type != null && receiverType.isEnum()) { // tolerate enum constant body
			scope.problemReporter().cannotInstantiate(this.type, receiverType);
			return this.resolvedType = receiverType;
		}
		// anonymous type scenario
		// an anonymous class inherits from java.lang.Object when declared "after" an interface
		this.superTypeBinding = receiverType.isInterface() ? scope.getJavaLangObject() : (ReferenceBinding) receiverType;
		// insert anonymous type in scope
		scope.addAnonymousType(this.anonymousType, (ReferenceBinding) receiverType);
		this.anonymousType.resolve(scope);		
		if (this.superTypeBinding.erasure().id == TypeIds.T_JavaLangEnum) {
			scope.problemReporter().cannotExtendEnum(this.anonymousType.binding, this.type, this.superTypeBinding);
		}
		
		if ((receiverType.tagBits & TagBits.HasDirectWildcard) != 0) {
			scope.problemReporter().superTypeCannotUseWildcard(this.anonymousType.binding, this.type, receiverType);
		}		
		// find anonymous super constructor
		MethodBinding inheritedBinding = scope.getConstructor(this.superTypeBinding, argumentTypes, this);
		if (!inheritedBinding.isValidBinding()) {
			if (inheritedBinding.declaringClass == null) {
				inheritedBinding.declaringClass = this.superTypeBinding;
			}
			scope.problemReporter().invalidConstructor(this, inheritedBinding);
			return this.resolvedType = this.anonymousType.binding;
		}
		if (this.enclosingInstance != null) {
			ReferenceBinding targetEnclosing = inheritedBinding.declaringClass.enclosingType();
			if (targetEnclosing == null) {
				scope.problemReporter().unnecessaryEnclosingInstanceSpecification(this.enclosingInstance, (ReferenceBinding)receiverType);
				return this.resolvedType = this.anonymousType.binding;
			} else if (!enclosingInstanceType.isCompatibleWith(targetEnclosing) && !scope.isBoxingCompatibleWith(enclosingInstanceType, targetEnclosing)) {
				scope.problemReporter().typeMismatchError(enclosingInstanceType, targetEnclosing, this.enclosingInstance);
				return this.resolvedType = this.anonymousType.binding;
			}
			this.enclosingInstance.computeConversion(scope, targetEnclosing, enclosingInstanceType);
		}
		if (this.arguments != null)
			checkInvocationArguments(scope, null, this.superTypeBinding, inheritedBinding, this.arguments, argumentTypes, argsContainCast, this);

		// Update the anonymous inner class : superclass, interface  
		this.binding = this.anonymousType.createDefaultConstructorWithBinding(inheritedBinding);
		return this.resolvedType = this.anonymousType.binding; // 1.2 change
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.enclosingInstance != null)
				this.enclosingInstance.traverse(visitor, scope);
			if (this.typeArguments != null) {
				for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
					this.typeArguments[i].traverse(visitor, scope);
				}					
			}
			if (this.type != null) // case of enum constant
				this.type.traverse(visitor, scope);
			if (this.arguments != null) {
				int argumentsLength = this.arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
			if (this.anonymousType != null)
				this.anonymousType.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
