/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.lookup.*;


public class JavadocMessageSend extends MessageSend {

	public int tagSourceStart, tagSourceEnd;
	public boolean superAccess = false;

	public JavadocMessageSend(char[] name, long pos) {
		this.selector = name;
		this.nameSourcePosition = pos;
		this.sourceStart = (int) (nameSourcePosition >>> 32);
		this.sourceEnd = (int) nameSourcePosition;
		this.bits |= InsideJavadoc;
	}
	public JavadocMessageSend(char[] name, long pos, JavadocArgumentExpression[] arguments) {
		this(name, pos);
		this.arguments = arguments;
	}

	/*
	 * Resolves type on a Block or Class scope.
	 */
	private TypeBinding internalResolveType(Scope scope) {
		// Answer the signature return type
		// Base type promotion

		constant = NotAConstant;
		if (this.receiver instanceof CastExpression) this.receiver.bits |= IgnoreNeedForCastCheckMASK; // will check later on
		SourceTypeBinding sourceTypeBinding = scope.enclosingSourceType();
		if (this.receiver == null) {
			this.receiverType = sourceTypeBinding;
			this.receiver = new JavadocQualifiedTypeReference(sourceTypeBinding.compoundName, new long[sourceTypeBinding.compoundName.length], 0, 0);
		}
		else {
			if (scope.kind == Scope.CLASS_SCOPE) {
				this.receiverType = receiver.resolveType((ClassScope)scope);
			} else {
				this.receiverType = receiver.resolveType((BlockScope)scope);
			}
			if (this.receiverType == null) {
				return null;
			}
			this.superAccess = sourceTypeBinding.isCompatibleWith(this.receiverType);
		}
		this.qualifyingType = this.receiverType;

		// will check for null after args are resolved
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			boolean argHasError = false; // typeChecks all arguments 
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++){
				Expression argument = arguments[i];
				if (scope.kind == Scope.CLASS_SCOPE) {
					argumentTypes[i] = argument.resolveType((ClassScope)scope);
				} else {
					argumentTypes[i] = argument.resolveType((BlockScope)scope);
				}
				if (argumentTypes[i] == null) {
					argHasError = true;
				}
			}
			if (argHasError) {
				if(receiverType instanceof ReferenceBinding) {
					// record any selector match, for clients who may still need hint about possible method match
					this.codegenBinding = this.binding = scope.findMethod((ReferenceBinding)receiverType, selector, new TypeBinding[]{}, this);
				}			
				return null;
			}
		}

		// base type cannot receive any message
		if (this.receiverType.isBaseType()) {
			scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
			return null;
		}
		this.codegenBinding = this.binding = scope.getMethod(this.receiverType, selector, argumentTypes, this); 
		if (!binding.isValidBinding()) {
			if (binding.declaringClass == null) {
				if (this.receiverType instanceof ReferenceBinding) {
					binding.declaringClass = (ReferenceBinding) this.receiverType;
				} else { 
					scope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
					return null;
				}
			}
			scope.problemReporter().invalidMethod(this, binding);
			// record the closest match, for clients who may still need hint about possible method match
			if (binding instanceof ProblemMethodBinding){
				MethodBinding closestMatch = ((ProblemMethodBinding)binding).closestMatch;
				if (closestMatch != null) this.codegenBinding = this.binding = closestMatch;
			}
			return this.resolvedType = binding == null ? null : binding.returnType;
		}
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				arguments[i].implicitWidening(binding.parameters[i], argumentTypes[i]);
			}
		}
		if (isMethodUseDeprecated(binding, scope)) {
			scope.problemReporter().deprecatedMethod(binding, this);
		}

		return this.resolvedType = binding.returnType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return this.superAccess;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){
	
		if (receiver != null) {
			receiver.printExpression(0, output);
		}
		output.append('#').append(selector).append('(');
		if (arguments != null) {
			for (int i = 0; i < arguments.length ; i ++) {	
				if (i > 0) output.append(", "); //$NON-NLS-1$
				arguments[i].printExpression(0, output);
			}
		}
		return output.append(')');
	}

	public TypeBinding resolveType(BlockScope scope) {
		return internalResolveType(scope);
	}

	public TypeBinding resolveType(ClassScope scope) {
		return internalResolveType(scope);
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			if (receiver != null) {
				receiver.traverse(visitor, blockScope);
			}
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					arguments[i].traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}
