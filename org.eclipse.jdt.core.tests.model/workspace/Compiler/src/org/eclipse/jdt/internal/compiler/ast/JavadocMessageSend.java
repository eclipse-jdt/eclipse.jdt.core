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
import org.eclipse.jdt.internal.compiler.lookup.*;


public class JavadocMessageSend extends MessageSend {

	public int tagSourceStart, tagSourceEnd;
	public int tagValue;
	public boolean superAccess = false;

	public JavadocMessageSend(char[] name, long pos) {
		this.selector = name;
		this.nameSourcePosition = pos;
		this.sourceStart = (int) (this.nameSourcePosition >>> 32);
		this.sourceEnd = (int) this.nameSourcePosition;
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
		this.constant = NotAConstant;
		if (this.receiver == null) {
			this.receiverType = scope.enclosingSourceType();
		} else if (scope.kind == Scope.CLASS_SCOPE) {
			this.receiverType = this.receiver.resolveType((ClassScope) scope);
		} else {
			this.receiverType = this.receiver.resolveType((BlockScope) scope);
		}

		// will check for null after args are resolved
		TypeBinding[] argumentTypes = NoParameters;
		if (this.arguments != null) {
			boolean argHasError = false; // typeChecks all arguments 
			int length = this.arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++){
				Expression argument = this.arguments[i];
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
				return null;
			}
		}

		// check receiver type
		if (this.receiverType == null) {
			return null;
		}
		this.qualifyingType = this.receiverType;
		this.superAccess = scope.enclosingSourceType().isCompatibleWith(this.receiverType);

		// base type cannot receive any message
		if (this.receiverType.isBaseType()) {
			scope.problemReporter().javadocErrorNoMethodFor(this, this.receiverType, argumentTypes, scope.getDeclarationModifiers());
			return null;
		}
		this.binding = (this.receiver != null && this.receiver.isThis())
			? scope.getImplicitMethod(this.selector, argumentTypes, this)
			: scope.getMethod(this.receiverType, this.selector, argumentTypes, this);
		if (!this.binding.isValidBinding()) {
			// implicit lookup may discover issues due to static/constructor contexts. javadoc must be resilient
			switch (this.binding.problemId()) {
				case ProblemReasons.NonStaticReferenceInConstructorInvocation:
				case ProblemReasons.NonStaticReferenceInStaticContext:
				case ProblemReasons.InheritedNameHidesEnclosingName : 
					MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
					if (closestMatch != null) {
						this.binding = closestMatch; // ignore problem if can reach target method through it
					}
			}
		}
		if (!this.binding.isValidBinding()) {
			if (this.binding.declaringClass == null) {
				if (this.receiverType instanceof ReferenceBinding) {
					this.binding.declaringClass = (ReferenceBinding) this.receiverType;
				} else { 
					scope.problemReporter().javadocErrorNoMethodFor(this, this.receiverType, argumentTypes, scope.getDeclarationModifiers());
					return null;
				}
			}
			scope.problemReporter().javadocInvalidMethod(this, this.binding, scope.getDeclarationModifiers());
			// record the closest match, for clients who may still need hint about possible method match
			if (this.binding instanceof ProblemMethodBinding){
				MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
				if (closestMatch != null) this.binding = closestMatch;
			}
			return this.resolvedType = this.binding == null ? null : this.binding.returnType;
		}
		if (isMethodUseDeprecated(this.binding, scope)) {
			scope.problemReporter().javadocDeprecatedMethod(this.binding, this, scope.getDeclarationModifiers());
		}

		return this.resolvedType = this.binding.returnType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return this.superAccess;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){
	
		if (this.receiver != null) {
			this.receiver.printExpression(0, output);
		}
		output.append('#').append(this.selector).append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length ; i ++) {	
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].printExpression(0, output);
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
			if (this.receiver != null) {
				this.receiver.traverse(visitor, blockScope);
			}
			if (this.arguments != null) {
				int argumentsLength = this.arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}
