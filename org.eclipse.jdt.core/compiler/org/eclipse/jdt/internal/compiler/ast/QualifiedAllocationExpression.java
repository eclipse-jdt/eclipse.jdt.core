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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Variation on allocation, where can be specified an enclosing instance and an anonymous type
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
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// analyse the enclosing instance
		if (enclosingInstance != null) {
			flowInfo = enclosingInstance.analyseCode(currentScope, flowContext, flowInfo);
		}
		
		// check captured variables are initialized in current context (26134)
		checkCapturedLocalInitializationIfNecessary(
			this.superTypeBinding == null ? this.binding.declaringClass : this.superTypeBinding, 
			currentScope, 
			flowInfo);
		
		// process arguments
		if (arguments != null) {
			for (int i = 0, count = arguments.length; i < count; i++) {
				flowInfo = arguments[i].analyseCode(currentScope, flowContext, flowInfo);
			}
		}

		// analyse the anonymous nested type
		if (anonymousType != null) {
			flowInfo = anonymousType.analyseCode(currentScope, flowContext, flowInfo);
		}

		// record some dependency information for exception types
		ReferenceBinding[] thrownExceptions;
		if (((thrownExceptions = binding.thrownExceptions).length) != 0) {
			// check exception handling
			flowContext.checkExceptionHandlers(
				thrownExceptions,
				this,
				flowInfo,
				currentScope);
		}
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		manageSyntheticAccessIfNecessary(currentScope, flowInfo);
		return flowInfo;
	}

	public Expression enclosingInstance() {

		return enclosingInstance;
	}

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		ReferenceBinding allocatedType = binding.declaringClass;
		codeStream.new_(allocatedType);
		if (valueRequired) {
			codeStream.dup();
		}
		// better highlight for allocation: display the type individually
		codeStream.recordPositionsFrom(pc, type.sourceStart);

		// handling innerclass instance allocation - enclosing instance arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticEnclosingInstanceValues(
				currentScope,
				allocatedType,
				enclosingInstance(),
				this);
		}
		// generate the arguments for constructor
		if (arguments != null) {
			for (int i = 0, count = arguments.length; i < count; i++) {
				arguments[i].generateCode(currentScope, codeStream, true);
			}
		}
		// handling innerclass instance allocation - outer local arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticOuterArgumentValues(
				currentScope,
				allocatedType,
				this);
		}
		
		// invoke constructor
		if (syntheticAccessor == null) {
			codeStream.invokespecial(binding);
		} else {
			// synthetic accessor got some extra arguments appended to its signature, which need values
			for (int i = 0,
				max = syntheticAccessor.parameters.length - binding.parameters.length;
				i < max;
				i++) {
				codeStream.aconst_null();
			}
			codeStream.invokespecial(syntheticAccessor);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);

		if (anonymousType != null) {
			anonymousType.generateCode(currentScope, codeStream);
		}
	}
	
	public boolean isSuperAccess() {

		// necessary to lookup super constructor of anonymous type
		return anonymousType != null;
	}
	
	/* Inner emulation consists in either recording a dependency 
	 * link only, or performing one level of propagation.
	 *
	 * Dependency mechanism is used whenever dealing with source target
	 * types, since by the time we reach them, we might not yet know their
	 * exact need.
	 */
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return;
		ReferenceBinding allocatedType;

		// perform some emulation work in case there is some and we are inside a local type only
		if ((allocatedType = binding.declaringClass).isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (allocatedType.isLocalType()) {
				((LocalTypeBinding) allocatedType).addInnerEmulationDependent(currentScope, enclosingInstance != null);
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(allocatedType, enclosingInstance != null);
			}
		}
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (enclosingInstance != null)
			enclosingInstance.printExpression(0, output).append('.'); 
		super.printExpression(0, output);
		if (anonymousType != null) {
			anonymousType.print(indent, output);
		}
		return output;
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		// added for code assist...cannot occur with 'normal' code
		if (anonymousType == null && enclosingInstance == null) {
			return super.resolveType(scope);
		}

		// Propagate the type checking to the arguments, and checks if the constructor is defined.
		// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		// ClassInstanceCreationExpression ::= Name '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		// ==> by construction, when there is an enclosing instance the typename may NOT be qualified
		// ==> therefore by construction the type is always a SingleTypeReferenceType instead of being either 
		// sometime a SingleTypeReference and sometime a QualifedTypeReference

		constant = NotAConstant;
		TypeBinding enclosingInstanceType = null;
		TypeBinding receiverType = null;
		boolean hasError = false;
		boolean enclosingInstanceContainsCast = false;
		boolean argsContainCast = false;
		
		if (enclosingInstance != null) {
			if (enclosingInstance instanceof CastExpression) {
				enclosingInstance.bits |= IgnoreNeedForCastCheckMASK; // will check later on
				enclosingInstanceContainsCast = true;
			}
			if ((enclosingInstanceType = enclosingInstance.resolveType(scope)) == null){
				hasError = true;
			} else if (enclosingInstanceType.isBaseType() || enclosingInstanceType.isArrayType()) {
				scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
					enclosingInstanceType,
					enclosingInstance);
				hasError = true;
			} else {
				receiverType = ((SingleTypeReference) type).resolveTypeEnclosing(scope, (ReferenceBinding) enclosingInstanceType);
				if (receiverType != null && enclosingInstanceContainsCast) {
						CastExpression.checkNeedForEnclosingInstanceCast(scope, enclosingInstance, enclosingInstanceType, receiverType);
				}
			}
		} else {
			receiverType = type.resolveType(scope);
		}
		if (receiverType == null) {
			hasError = true;
		} else if (((ReferenceBinding) receiverType).isFinal() && this.anonymousType != null) {
			scope.problemReporter().anonymousClassCannotExtendFinalClass(type, receiverType);
			hasError = true;
		}

		// will check for null after args are resolved
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				Expression argument = this.arguments[i];
				if (argument instanceof CastExpression) {
					argument.bits |= IgnoreNeedForCastCheckMASK; // will check later on
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
			ReferenceBinding allocationType = (ReferenceBinding) receiverType;
			if (!receiverType.canBeInstantiated()) {
				scope.problemReporter().cannotInstantiate(type, receiverType);
				return this.resolvedType = receiverType;
			}
			if ((this.binding = scope.getConstructor(allocationType, argumentTypes, this)).isValidBinding()) {
				if (isMethodUseDeprecated(binding, scope)) {
					scope.problemReporter().deprecatedMethod(this.binding, this);
				}
				if (arguments != null) {
					for (int i = 0; i < arguments.length; i++) {
						arguments[i].implicitWidening(this.binding.parameters[i], argumentTypes[i]);
					}
					if (argsContainCast) {
						CastExpression.checkNeedForArgumentCasts(scope, null, allocationType, binding, this.arguments, argumentTypes, this);
					}
				}
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
			if (enclosingInstanceType.isCompatibleWith(expectedType)) {
				return receiverType;
			}
			scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
				this.enclosingInstance,
				enclosingInstanceType,
				expectedType);
			return this.resolvedType = receiverType;
		}

		// anonymous type scenario
		// an anonymous class inherits from java.lang.Object when declared "after" an interface
		this.superTypeBinding = receiverType.isInterface() ? scope.getJavaLangObject() : (ReferenceBinding) receiverType;
		// insert anonymous type in scope
		scope.addAnonymousType(this.anonymousType, (ReferenceBinding) receiverType);
		this.anonymousType.resolve(scope);		
		
		// find anonymous super constructor
		MethodBinding inheritedBinding = scope.getConstructor(this.superTypeBinding, argumentTypes, this);
		if (!inheritedBinding.isValidBinding()) {
			if (inheritedBinding.declaringClass == null) {
				inheritedBinding.declaringClass = this.superTypeBinding;
			}
			scope.problemReporter().invalidConstructor(this, inheritedBinding);
			return this.resolvedType = anonymousType.binding;
		}
		if (enclosingInstance != null) {
			ReferenceBinding targetEnclosing = inheritedBinding.declaringClass.enclosingType();
			if (targetEnclosing == null) {
				scope.problemReporter().unnecessaryEnclosingInstanceSpecification(enclosingInstance, (ReferenceBinding)receiverType);
				return this.resolvedType = anonymousType.binding;
			} else 	if (!enclosingInstanceType.isCompatibleWith(targetEnclosing)) {
				scope.problemReporter().typeMismatchErrorActualTypeExpectedType(enclosingInstance, enclosingInstanceType, targetEnclosing);
				return this.resolvedType = anonymousType.binding;
			}
		}

		// this promotion has to be done somewhere: here or inside the constructor of the
		// anonymous class. We do it here while the constructor of the inner is then easier.
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				arguments[i].implicitWidening(inheritedBinding.parameters[i], argumentTypes[i]);
			}
			if (argsContainCast) {
				CastExpression.checkNeedForArgumentCasts(scope, null, this.superTypeBinding, inheritedBinding, this.arguments, argumentTypes, this);
			}
		}
		// Update the anonymous inner class : superclass, interface  
		binding = anonymousType.createsInternalConstructorWithBinding(inheritedBinding);
		return this.resolvedType = anonymousType.binding; // 1.2 change
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (enclosingInstance != null)
				enclosingInstance.traverse(visitor, scope);
			type.traverse(visitor, scope);
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (anonymousType != null)
				anonymousType.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
