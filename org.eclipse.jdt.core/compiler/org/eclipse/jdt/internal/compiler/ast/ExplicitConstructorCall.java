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

public class ExplicitConstructorCall extends Statement implements InvocationSite {
		
	public Expression[] arguments;
	public Expression qualification;
	public MethodBinding binding;							// exact binding resulting from lookup
	protected MethodBinding codegenBinding;	// actual binding used for code generation (if no synthetic accessor)
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public int accessMode;
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;
	
	public final static int ImplicitSuper = 1;
	public final static int Super = 2;
	public final static int This = 3;

	public VariableBinding[][] implicitArguments;
	boolean discardEnclosingInstance;
	
	// TODO Remove once DOMParser is activated
	public int typeArgumentsSourceStart;

	public ExplicitConstructorCall(int accessMode) {
		this.accessMode = accessMode;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// must verify that exceptions potentially thrown by this expression are caught in the method.

		try {
			((MethodScope) currentScope).isConstructorCall = true;

			// process enclosing instance
			if (qualification != null) {
				flowInfo =
					qualification
						.analyseCode(currentScope, flowContext, flowInfo)
						.unconditionalInits();
			}
			// process arguments
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					flowInfo =
						arguments[i]
							.analyseCode(currentScope, flowContext, flowInfo)
							.unconditionalInits();
				}
			}

			ReferenceBinding[] thrownExceptions;
			if ((thrownExceptions = binding.thrownExceptions) != NoExceptions) {
				// check exceptions
				flowContext.checkExceptionHandlers(
					thrownExceptions,
					(accessMode == ImplicitSuper)
						? (ASTNode) currentScope.methodScope().referenceContext
						: (ASTNode) this,
					flowInfo,
					currentScope);
			}
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
			manageSyntheticAccessIfNecessary(currentScope, flowInfo);
			return flowInfo;
		} finally {
			((MethodScope) currentScope).isConstructorCall = false;
		}
	}

	/**
	 * Constructor call code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		try {
			((MethodScope) currentScope).isConstructorCall = true;

			int pc = codeStream.position;
			codeStream.aload_0();

			// handling innerclass constructor invocation
			ReferenceBinding targetType = this.codegenBinding.declaringClass;
			// handling innerclass instance allocation - enclosing instance arguments
			if (targetType.isNestedType()) {
				codeStream.generateSyntheticEnclosingInstanceValues(
					currentScope,
					targetType,
					discardEnclosingInstance ? null : qualification,
					this);
			}
			// regular code gen
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					arguments[i].generateCode(currentScope, codeStream, true);
				}
			}
			// handling innerclass instance allocation - outer local arguments
			if (targetType.isNestedType()) {
				codeStream.generateSyntheticOuterArgumentValues(
					currentScope,
					targetType,
					this);
			}
			if (syntheticAccessor != null) {
				// synthetic accessor got some extra arguments appended to its signature, which need values
				for (int i = 0,
					max = syntheticAccessor.parameters.length - this.codegenBinding.parameters.length;
					i < max;
					i++) {
					codeStream.aconst_null();
				}
				codeStream.invokespecial(syntheticAccessor);
			} else {
				codeStream.invokespecial(this.codegenBinding);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			((MethodScope) currentScope).isConstructorCall = false;
		}
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return this.genericTypeArguments;
	}
	public boolean isImplicitSuper() {
		//return true if I'm of these compiler added statement super();

		return (accessMode == ImplicitSuper);
	}

	public boolean isSuperAccess() {

		return accessMode != This;
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
	void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		ReferenceBinding superType;

		if (!flowInfo.isReachable()) return;
		// perform some emulation work in case there is some and we are inside a local type only
		if ((superType = binding.declaringClass).isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (superType.isLocalType()) {
				((LocalTypeBinding) superType).addInnerEmulationDependent(currentScope, qualification != null);
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(superType, qualification != null);
			}
		}
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if (!flowInfo.isReachable()) return;
		// if constructor from parameterized type got found, use the original constructor at codegen time
		this.codegenBinding = this.binding.original();
		
		// perform some emulation work in case there is some and we are inside a local type only
		if (binding.isPrivate() && accessMode != This) {

			if (currentScope.environment().options.isPrivateConstructorAccessChangingVisibility) {
				this.codegenBinding.tagForClearingPrivateModifier();
				// constructor will not be dumped as private, no emulation required thus
			} else {
				syntheticAccessor =
					((SourceTypeBinding) this.codegenBinding.declaringClass).addSyntheticMethod(this.codegenBinding, isSuperAccess());
				currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
			}
		}
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output);
		if (qualification != null) qualification.printExpression(0, output).append('.');
		if (typeArguments != null) {
			output.append('<');//$NON-NLS-1$
			int max = typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeArguments[max].print(0, output);
			output.append('>');
		}		
		if (accessMode == This) {
			output.append("this("); //$NON-NLS-1$
		} else {
			output.append("super("); //$NON-NLS-1$
		}
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				arguments[i].printExpression(0, output);
			}
		}
		return output.append(");"); //$NON-NLS-1$
	}
	
	public void resolve(BlockScope scope) {
		// the return type should be void for a constructor.
		// the test is made into getConstructor

		// mark the fact that we are in a constructor call.....
		// unmark at all returns
		MethodScope methodScope = scope.methodScope();
		try {
			AbstractMethodDeclaration methodDeclaration = methodScope.referenceMethod();
			if (methodDeclaration == null 
					|| !methodDeclaration.isConstructor()
					|| ((ConstructorDeclaration) methodDeclaration).constructorCall != this) {
				scope.problemReporter().invalidExplicitConstructorCall(this);
				return;
			}
			methodScope.isConstructorCall = true;
			ReferenceBinding receiverType = scope.enclosingSourceType();
			if (accessMode != This)
				receiverType = receiverType.superclass();

			if (receiverType == null) {
				return;
			}

			// qualification should be from the type of the enclosingType
			if (qualification != null) {
				if (accessMode != Super) {
					scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
						qualification,
						receiverType);
				}
				ReferenceBinding enclosingType = receiverType.enclosingType();
				if (enclosingType == null) {
					scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
						qualification,
						receiverType);
					discardEnclosingInstance = true;
				} else {
					TypeBinding qTb = qualification.resolveTypeExpecting(scope, enclosingType);
					qualification.computeConversion(scope, qTb, qTb);
				}
			}
			// resolve type arguments (for generic constructor call)
			if (this.typeArguments != null) {
				int length = this.typeArguments.length;
				boolean argHasError = false; // typeChecks all arguments
				this.genericTypeArguments = new TypeBinding[length];
				for (int i = 0; i < length; i++) {
					if ((this.genericTypeArguments[i] = this.typeArguments[i].resolveType(scope)) == null) {
						argHasError = true;
					}
				}
				if (argHasError) {
					return;
				}
			}			
	
			// arguments buffering for the method lookup
			TypeBinding[] argumentTypes = NoParameters;
			boolean argsContainCast = false;
			if (arguments != null) {
				boolean argHasError = false; // typeChecks all arguments
				int length = arguments.length;
				argumentTypes = new TypeBinding[length];
				for (int i = 0; i < length; i++) {
					Expression argument = this.arguments[i];
					if (argument instanceof CastExpression) {
						argument.bits |= IgnoreNeedForCastCheckMASK; // will check later on
						argsContainCast = true;
					}
					if ((argumentTypes[i] = argument.resolveType(scope)) == null) {
						argHasError = true;
					}
				}
				if (argHasError) {
					return;
				}
			}
			if ((binding = scope.getConstructor(receiverType, argumentTypes, this)).isValidBinding()) {
				if (isMethodUseDeprecated(binding, scope))
					scope.problemReporter().deprecatedMethod(binding, this);
				if (this.arguments != null)
					checkInvocationArguments(scope, null, receiverType, binding, this.arguments, argumentTypes, argsContainCast, this);
				if (binding.isPrivate()) {
					binding.original().modifiers |= AccPrivateUsed;
				}				
			} else {
				if (binding.declaringClass == null)
					binding.declaringClass = receiverType;
				scope.problemReporter().invalidConstructor(this, binding);
			}
		} finally {
			methodScope.isConstructorCall = false;
		}
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}

	public void setDepth(int depth) {
		// ignore for here
	}

	public void setFieldIndex(int depth) {
		// ignore for here
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (this.qualification != null) {
				this.qualification.traverse(visitor, scope);
			}
			if (this.typeArguments != null) {
				for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
					this.typeArguments[i].traverse(visitor, scope);
				}			
			}
			if (this.arguments != null) {
				for (int i = 0, argumentLength = this.arguments.length; i < argumentLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
