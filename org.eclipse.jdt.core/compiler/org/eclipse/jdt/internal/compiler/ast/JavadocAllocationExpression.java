/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class JavadocAllocationExpression extends AllocationExpression {

	public int tagSourceStart, tagSourceEnd;
	public int tagValue;
	public boolean superAccess = false;
	
	public JavadocAllocationExpression(int start, int end) {
		this.sourceStart = start;
		this.sourceEnd = end;
		this.bits |= InsideJavadoc;
	}
	public JavadocAllocationExpression(long pos) {
		this((int) (pos >>> 32), (int) pos);
	}

	private TypeBinding internalResolveType(Scope scope) {
	
		// Propagate the type checking to the arguments, and check if the constructor is defined.
		this.constant = Constant.NotAConstant;
		if (this.type == null) {
			this.resolvedType = scope.enclosingSourceType();
		} else if (scope.kind == Scope.CLASS_SCOPE) {
			this.resolvedType = this.type.resolveType((ClassScope)scope);
		} else {
			this.resolvedType = this.type.resolveType((BlockScope)scope, true /* check bounds*/);
		}
	
		// buffering the arguments' types
		TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
		boolean hasTypeVarArgs = false;
		if (this.arguments != null) {
			boolean argHasError = false;
			int length = this.arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				Expression argument = this.arguments[i];
				if (scope.kind == Scope.CLASS_SCOPE) {
					argumentTypes[i] = argument.resolveType((ClassScope)scope);
				} else {
					argumentTypes[i] = argument.resolveType((BlockScope)scope);
				}
				if (argumentTypes[i] == null) {
					argHasError = true;
				} else if (!hasTypeVarArgs) {
					hasTypeVarArgs = argumentTypes[i].isTypeVariable();
				}
			}
			if (argHasError) {
				return null;
			}
		}
	
		// check resolved type
		if (this.resolvedType == null) {
			return null;
		}
		this.resolvedType = scope.environment().convertToRawType(this.type.resolvedType);
		SourceTypeBinding enclosingType = scope.enclosingSourceType();
		this.superAccess = enclosingType==null ? false : enclosingType.isCompatibleWith(this.resolvedType);
	
		ReferenceBinding allocationType = (ReferenceBinding) this.resolvedType;
		this.binding = scope.getConstructor(allocationType, argumentTypes, this);
		if (!this.binding.isValidBinding()) {
			ReferenceBinding enclosingTypeBinding = allocationType;
			MethodBinding contructorBinding = this.binding;
			while (!contructorBinding.isValidBinding() && (enclosingTypeBinding.isMemberType() || enclosingTypeBinding.isLocalType())) {
				enclosingTypeBinding = enclosingTypeBinding.enclosingType();
				contructorBinding = scope.getConstructor(enclosingTypeBinding, argumentTypes, this);
			}
			if (contructorBinding.isValidBinding()) {
				this.binding = contructorBinding;
			}
		}
		if (!this.binding.isValidBinding()) {
			// First try to search a method instead
			MethodBinding methodBinding = scope.getMethod(this.resolvedType, this.resolvedType.sourceName(), argumentTypes, this);
			if (methodBinding.isValidBinding()) {
				this.binding = methodBinding;
			} else {
				if (this.binding.declaringClass == null) {
					this.binding.declaringClass = allocationType;
				}
				scope.problemReporter().javadocInvalidConstructor(this, this.binding, scope.getDeclarationModifiers());
			}
			return this.resolvedType;
		} else if (binding.isVarargs()) {
			int length = argumentTypes.length;
			if (!(binding.parameters.length == length && argumentTypes[length-1].isArrayType())) {
				MethodBinding problem = new ProblemMethodBinding(this.binding, this.binding.selector, argumentTypes, ProblemReasons.NotFound);
				scope.problemReporter().javadocInvalidConstructor(this, problem, scope.getDeclarationModifiers());
			}
		} else if (hasTypeVarArgs) {
			MethodBinding problem = new ProblemMethodBinding(this.binding, this.binding.selector, argumentTypes, ProblemReasons.NotFound);
			scope.problemReporter().javadocInvalidConstructor(this, problem, scope.getDeclarationModifiers());
		} else if (this.binding instanceof ParameterizedMethodBinding) {
			ParameterizedMethodBinding paramMethodBinding = (ParameterizedMethodBinding) this.binding;
			if (paramMethodBinding.hasSubstitutedParameters()) {
				int length = argumentTypes.length;
				for (int i=0; i<length; i++) {
					if (paramMethodBinding.parameters[i] != argumentTypes[i] &&
							paramMethodBinding.parameters[i].erasure() != argumentTypes[i].erasure()) {
						MethodBinding problem = new ProblemMethodBinding(this.binding, this.binding.selector, argumentTypes, ProblemReasons.NotFound);
						scope.problemReporter().javadocInvalidConstructor(this, problem, scope.getDeclarationModifiers());
						break;
					}
				}
			}
		}
		if (isMethodUseDeprecated(this.binding, scope, true)) {
			scope.problemReporter().javadocDeprecatedMethod(this.binding, this, scope.getDeclarationModifiers());
		}
		return allocationType;
	}

	public boolean isSuperAccess() {
		return this.superAccess;
	}

	public TypeBinding resolveType(BlockScope scope) {
		return internalResolveType(scope);
	}

	public TypeBinding resolveType(ClassScope scope) {
		return internalResolveType(scope);
	}
}
