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

public class AllocationExpression
	extends Expression
	implements InvocationSite {
		
	public TypeReference type;
	public Expression[] arguments;
	public MethodBinding binding;

	MethodBinding syntheticAccessor;

	public AllocationExpression() {
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// check captured variables are initialized in current context (26134)
		checkCapturedLocalInitializationIfNecessary(this.binding.declaringClass, currentScope, flowInfo);

		// process arguments
		if (arguments != null) {
			for (int i = 0, count = arguments.length; i < count; i++) {
				flowInfo =
					arguments[i]
						.analyseCode(currentScope, flowContext, flowInfo)
						.unconditionalInits();
			}
		}
		// record some dependency information for exception types
		ReferenceBinding[] thrownExceptions;
		if (((thrownExceptions = this.binding.thrownExceptions).length) != 0) {
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

	public void checkCapturedLocalInitializationIfNecessary(ReferenceBinding checkedType, BlockScope currentScope, FlowInfo flowInfo) {

		if (checkedType.isLocalType() 
				&& !checkedType.isAnonymousType()
				&& !currentScope.isDefinedInType(checkedType)) { // only check external allocations
			NestedTypeBinding nestedType = (NestedTypeBinding) checkedType;
			SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
			if (syntheticArguments != null) 
				for (int i = 0, count = syntheticArguments.length; i < count; i++){
					SyntheticArgumentBinding syntheticArgument = syntheticArguments[i];
					LocalVariableBinding targetLocal;
					if ((targetLocal = syntheticArgument.actualOuterLocalVariable) == null) continue;
					if (targetLocal.declaration != null && !flowInfo.isDefinitelyAssigned(targetLocal)){
						currentScope.problemReporter().uninitializedLocalVariable(targetLocal, this);
					}
				}
						
		}
	}
	
	public Expression enclosingInstance() {
		return null;
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
	}

	public boolean isSuperAccess() {

		return false;
	}

	public boolean isTypeAccess() {

		return true;
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
				((LocalTypeBinding) allocatedType).addInnerEmulationDependent(currentScope, false);
				// request cascade of accesses
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(allocatedType, false);
				// request cascade of accesses
			}
		}
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {

		if (binding.isPrivate()
			&& (currentScope.enclosingSourceType() != binding.declaringClass)) {

			if (currentScope
				.environment()
				.options
				.isPrivateConstructorAccessChangingVisibility) {
				binding.tagForClearingPrivateModifier();
				// constructor will not be dumped as private, no emulation required thus
			} else {
				syntheticAccessor =
					((SourceTypeBinding) binding.declaringClass).addSyntheticMethod(binding, isSuperAccess());
				currentScope.problemReporter().needToEmulateMethodAccess(binding, this);
			}
		}
	}

	public TypeBinding resolveType(BlockScope scope) {

		// Propagate the type checking to the arguments, and check if the constructor is defined.
		constant = NotAConstant;
		this.resolvedType = type.resolveType(scope);
		// will check for null after args are resolved

		// buffering the arguments' types
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			boolean argHasError = false;
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++)
				if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null)
					argHasError = true;
			if (argHasError)
				return this.resolvedType;
		}
		if (this.resolvedType == null)
			return null;

		if (!this.resolvedType.canBeInstantiated()) {
			scope.problemReporter().cannotInstantiate(type, this.resolvedType);
			return this.resolvedType;
		}
		ReferenceBinding allocatedType = (ReferenceBinding) this.resolvedType;
		if (!(binding = scope.getConstructor(allocatedType, argumentTypes, this))
			.isValidBinding()) {
			if (binding.declaringClass == null)
				binding.declaringClass = allocatedType;
			scope.problemReporter().invalidConstructor(this, binding);
			return this.resolvedType;
		}
		if (isMethodUseDeprecated(binding, scope))
			scope.problemReporter().deprecatedMethod(binding, this);

		if (arguments != null)
			for (int i = 0; i < arguments.length; i++)
				arguments[i].implicitWidening(binding.parameters[i], argumentTypes[i]);
		return allocatedType;
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}

	public void setDepth(int i) {
		// ignored
	}

	public void setFieldIndex(int i) {
		// ignored
	}

	public String toStringExpression() {

		String s = "new " + type.toString(0); //$NON-NLS-1$
		if (arguments == null)
			s = s + "()"; //$NON-NLS-1$
		else {
			s = s + "("; //$NON-NLS-1$
			for (int i = 0; i < arguments.length; i++) {
				s = s + arguments[i].toStringExpression();
				if (i == (arguments.length - 1))
					s = s + ")"; //$NON-NLS-1$
				else
					s = s + ", "; //$NON-NLS-1$
			}
		}
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			int argumentsLength;
			type.traverse(visitor, scope);
			if (arguments != null) {
				argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}