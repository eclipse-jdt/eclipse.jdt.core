/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Variation on allocation, where can be specified an enclosing instance and an anonymous type
 */
public class QualifiedAllocationExpression extends AllocationExpression {
	
	//qualification may be on both side
	public Expression enclosingInstance;
	public AnonymousLocalTypeDeclaration anonymousType;
	public ReferenceBinding superTypeBinding;
	
	public QualifiedAllocationExpression() {
	}

	public QualifiedAllocationExpression(AnonymousLocalTypeDeclaration anonymousType) {
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
		manageEnclosingInstanceAccessIfNecessary(currentScope);
		manageSyntheticAccessIfNecessary(currentScope);
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
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {

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
		if (anonymousType == null) { //----------------no anonymous class------------------------	
			if ((enclosingInstanceType = enclosingInstance.resolveType(scope)) == null){
				hasError = true;
			} else if (enclosingInstanceType.isBaseType() || enclosingInstanceType.isArrayType()) {
				scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
					enclosingInstanceType,
					enclosingInstance);
			} else if ((this.resolvedType = receiverType = ((SingleTypeReference) type).resolveTypeEnclosing(
							scope,
							(ReferenceBinding) enclosingInstanceType)) == null) {
				hasError = true;
			}
			// will check for null after args are resolved
			TypeBinding[] argumentTypes = NoParameters;
			if (arguments != null) {
				int length = arguments.length;
				argumentTypes = new TypeBinding[length];
				for (int i = 0; i < length; i++)
					if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null){
						hasError = true;
					}
			}
			// limit of fault-tolerance
			if (hasError) return receiverType;

			if (!receiverType.canBeInstantiated()) {
				scope.problemReporter().cannotInstantiate(type, receiverType);
				return receiverType;
			}
			if ((this.binding = scope.getConstructor((ReferenceBinding) receiverType, argumentTypes, this))
					.isValidBinding()) {
				if (isMethodUseDeprecated(binding, scope))
					scope.problemReporter().deprecatedMethod(this.binding, this);

				if (arguments != null)
					for (int i = 0; i < arguments.length; i++)
						arguments[i].implicitWidening(this.binding.parameters[i], argumentTypes[i]);
			} else {
				if (this.binding.declaringClass == null)
					this.binding.declaringClass = (ReferenceBinding) receiverType;
				scope.problemReporter().invalidConstructor(this, this.binding);
				return receiverType;
			}

			// The enclosing instance must be compatible with the innermost enclosing type
			ReferenceBinding expectedType = this.binding.declaringClass.enclosingType();
			if (enclosingInstanceType.isCompatibleWith(expectedType))
				return receiverType;
			scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
				this.enclosingInstance,
				enclosingInstanceType,
				expectedType);
			return receiverType;
		}

		//--------------there is an anonymous type declaration-----------------
		if (this.enclosingInstance != null) {
			if ((enclosingInstanceType = this.enclosingInstance.resolveType(scope)) == null) {
				hasError = true;
			} else if (enclosingInstanceType.isBaseType() || enclosingInstanceType.isArrayType()) {
				scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
					enclosingInstanceType,
					this.enclosingInstance);
				hasError = true;
			} else {
				receiverType = ((SingleTypeReference) type).resolveTypeEnclosing(
										scope,
										(ReferenceBinding) enclosingInstanceType);				
			}
		} else {
			receiverType = type.resolveType(scope);
		}
		if (receiverType == null) {
			hasError = true;
		} else if (((ReferenceBinding) receiverType).isFinal()) {
			scope.problemReporter().anonymousClassCannotExtendFinalClass(type, receiverType);
			hasError = true;
		}
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++)
				if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null) {
					hasError = true;
				}
		}
		// limit of fault-tolerance
		if (hasError) {
				return receiverType;
		}

		// an anonymous class inherits from java.lang.Object when declared "after" an interface
		this.superTypeBinding =
			receiverType.isInterface() ? scope.getJavaLangObject() : (ReferenceBinding) receiverType;
		MethodBinding inheritedBinding =
			scope.getConstructor(this.superTypeBinding, argumentTypes, this);
		if (!inheritedBinding.isValidBinding()) {
			if (inheritedBinding.declaringClass == null)
				inheritedBinding.declaringClass = this.superTypeBinding;
			scope.problemReporter().invalidConstructor(this, inheritedBinding);
			return null;
		}
		if (enclosingInstance != null) {
			if (!enclosingInstanceType.isCompatibleWith(inheritedBinding.declaringClass.enclosingType())) {
				scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
					enclosingInstance,
					enclosingInstanceType,
					inheritedBinding.declaringClass.enclosingType());
				return null;
			}
		}

		// this promotion has to be done somewhere: here or inside the constructor of the
		// anonymous class. We do it here while the constructor of the inner is then easier.
		if (arguments != null)
			for (int i = 0; i < arguments.length; i++)
				arguments[i].implicitWidening(inheritedBinding.parameters[i], argumentTypes[i]);

		// Update the anonymous inner class : superclass, interface  
		scope.addAnonymousType(anonymousType, (ReferenceBinding) receiverType);
		anonymousType.resolve(scope);
		binding = anonymousType.createsInternalConstructorWithBinding(inheritedBinding);
		return anonymousType.binding; // 1.2 change
	}
	
	public String toStringExpression() {
		return this.toStringExpression(0);
	}

	public String toStringExpression(int tab) {

		String s = ""; //$NON-NLS-1$
		if (enclosingInstance != null)
			s += enclosingInstance.toString() + "."; //$NON-NLS-1$
		s += super.toStringExpression();
		if (anonymousType != null) {
			s += anonymousType.toString(tab);
		} //allows to restart just after the } one line under ....
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

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