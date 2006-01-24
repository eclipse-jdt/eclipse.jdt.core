/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class AllocationExpression extends Expression implements InvocationSite {
		
	public TypeReference type;
	public Expression[] arguments;
	public MethodBinding binding;							// exact binding resulting from lookup
	protected MethodBinding codegenBinding;	// actual binding used for code generation (if no synthetic accessor)
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeReference[] typeArguments;	
	public TypeBinding[] genericTypeArguments;
	public FieldDeclaration enumConstant; // for enum constant initializations

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// check captured variables are initialized in current context (26134)
		checkCapturedLocalInitializationIfNecessary((ReferenceBinding)this.binding.declaringClass.erasure(), currentScope, flowInfo);

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
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		manageSyntheticAccessIfNecessary(currentScope, flowInfo);
		
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
			codeStream.ldc(String.valueOf(enumConstant.name));
			codeStream.generateInlinedValue(enumConstant.binding.id);
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
		generateArguments(binding, arguments, currentScope, codeStream);
		// handling innerclass instance allocation - outer local arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticOuterArgumentValues(
				currentScope,
				allocatedType,
				this);
		}
		// invoke constructor
		if (syntheticAccessor == null) {
			codeStream.invokespecial(this.codegenBinding);
		} else {
			// synthetic accessor got some extra arguments appended to its signature, which need values
			for (int i = 0,
				max = syntheticAccessor.parameters.length - this.codegenBinding.parameters.length;
				i < max;
				i++) {
				codeStream.aconst_null();
			}
			codeStream.invokespecial(syntheticAccessor);
		}
		codeStream.generateImplicitConversion(this.implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return this.genericTypeArguments;
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
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
		ReferenceBinding allocatedTypeErasure = (ReferenceBinding) binding.declaringClass.erasure();

		// perform some emulation work in case there is some and we are inside a local type only
		if (allocatedTypeErasure.isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (allocatedTypeErasure.isLocalType()) {
				((LocalTypeBinding) allocatedTypeErasure).addInnerEmulationDependent(currentScope, false);
				// request cascade of accesses
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(allocatedTypeErasure, false);
				// request cascade of accesses
			}
		}
		}
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {

		// if constructor from parameterized type got found, use the original constructor at codegen time
		this.codegenBinding = this.binding.original();

		if (this.codegenBinding.isPrivate()
			&& (currentScope.enclosingSourceType() != this.codegenBinding.declaringClass)) {

			if (currentScope.compilerOptions().isPrivateConstructorAccessChangingVisibility) {
				this.codegenBinding.tagForClearingPrivateModifier();
				// constructor will not be dumped as private, no emulation required thus
			} else {
				syntheticAccessor =
					((SourceTypeBinding) this.codegenBinding.declaringClass).addSyntheticMethod(this.codegenBinding, isSuperAccess());
				currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
			}
		}
		}
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (this.type != null) { // type null for enum constant initializations
			output.append("new "); //$NON-NLS-1$
		}
		if (typeArguments != null) {
			output.append('<');
			int max = typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeArguments[max].print(0, output);
			output.append('>');
		}
		if (type != null) { // type null for enum constant initializations
			type.printExpression(0, output); 
		}
		output.append('(');
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				arguments[i].printExpression(0, output);
			}
		}
		return output.append(')');
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		// Propagate the type checking to the arguments, and check if the constructor is defined.
		constant = Constant.NotAConstant;
		if (this.type == null) {
			// initialization of an enum constant
			this.resolvedType = scope.enclosingReceiverType();
		} else {
			this.resolvedType = this.type.resolveType(scope, true /* check bounds*/);
			checkParameterizedAllocation: {
				if (this.type instanceof ParameterizedQualifiedTypeReference) { // disallow new X<String>.Y<Integer>()
					ReferenceBinding currentType = (ReferenceBinding)this.resolvedType;
					if (currentType == null) return null;
					do {
						// isStatic() is answering true for toplevel types
						if ((currentType.modifiers & ClassFileConstants.AccStatic) != 0) break checkParameterizedAllocation;
						if (currentType.isRawType()) break checkParameterizedAllocation;
					} while ((currentType = currentType.enclosingType())!= null);
					ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference) this.type;
					for (int i = qRef.typeArguments.length - 2; i >= 0; i--) {
						if (qRef.typeArguments[i] != null) {
							scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, this.resolvedType);
							break;
						}
					}
				}
			}
		}
		// will check for null after args are resolved

		// resolve type arguments (for generic constructor call)
		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			boolean argHasError = false; // typeChecks all arguments
			this.genericTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				if ((this.genericTypeArguments[i] = this.typeArguments[i].resolveType(scope, true /* check bounds*/)) == null) {
					argHasError = true;
				}
			}
			if (argHasError) {
				return null;
			}
		}
		
		// buffering the arguments' types
		boolean argsContainCast = false;
		TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
		if (arguments != null) {
			boolean argHasError = false;
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				Expression argument = this.arguments[i];
				if (argument instanceof CastExpression) {
					argument.bits |= DisableUnnecessaryCastCheck; // will check later on
					argsContainCast = true;
				}
				if ((argumentTypes[i] = argument.resolveType(scope)) == null) {
					argHasError = true;
				}
			}
			if (argHasError) {
				if (this.resolvedType instanceof ReferenceBinding) {
					// record a best guess, for clients who need hint about possible contructor match
					TypeBinding[] pseudoArgs = new TypeBinding[length];
					for (int i = length; --i >= 0;)
						pseudoArgs[i] = argumentTypes[i] == null ? this.resolvedType : argumentTypes[i]; // replace args with errors with receiver
					this.binding = scope.findMethod((ReferenceBinding) this.resolvedType, TypeConstants.INIT, pseudoArgs, this);
				}
				return this.resolvedType;
			}
		}
		if (this.resolvedType == null)
			return null;

		// null type denotes fake allocation for enum constant inits
		if (this.type != null && !this.resolvedType.canBeInstantiated()) {
			scope.problemReporter().cannotInstantiate(type, this.resolvedType);
			return this.resolvedType;
		}
		ReferenceBinding allocationType = (ReferenceBinding) this.resolvedType;
		if (!(binding = scope.getConstructor(allocationType, argumentTypes, this)).isValidBinding()) {
			if (binding.declaringClass == null)
				binding.declaringClass = allocationType;
			scope.problemReporter().invalidConstructor(this, binding);
			return this.resolvedType;
		}
		if (isMethodUseDeprecated(binding, scope, true))
			scope.problemReporter().deprecatedMethod(binding, this);
		checkInvocationArguments(scope, null, allocationType, this.binding, this.arguments, argumentTypes, argsContainCast, this);

		return allocationType;
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

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.typeArguments != null) {
				for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
					this.typeArguments[i].traverse(visitor, scope);
				}
			}
			if (this.type != null) { // enum constant scenario
				this.type.traverse(visitor, scope);
			}
			if (this.arguments != null) {
				for (int i = 0, argumentsLength = this.arguments.length; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
