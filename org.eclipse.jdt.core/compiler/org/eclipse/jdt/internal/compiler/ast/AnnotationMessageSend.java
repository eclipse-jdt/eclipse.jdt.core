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

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;


public class AnnotationMessageSend extends MessageSend {

	public int tagSourceStart, tagSourceEnd;

	public AnnotationMessageSend(char[] name, long pos) {
		this.selector = name;
		this.nameSourcePosition = pos;
		this.sourceStart = (int) (nameSourcePosition >>> 32);
		this.sourceEnd = (int) nameSourcePosition;
		this.bits |= InsideAnnotation;
	}
	public AnnotationMessageSend(char[] name, long pos, AnnotationArgumentExpression[] arguments) {
		this(name, pos);
		this.arguments = arguments;
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
		// Answer the signature return type
		// Base type promotion

		constant = NotAConstant;
		if (this.receiver instanceof CastExpression) this.receiver.bits |= IgnoreNeedForCastCheckMASK; // will check later on
		if (this.receiver == null) {
			this.receiverType = scope.enclosingSourceType();
		}
		else {
			this.receiverType = receiver.resolveType(scope);
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
				if ((argumentTypes[i] = argument.resolveType(scope)) == null){
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
		if (this.receiverType == null) {
			return null;
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

	public TypeBinding resolveType(ClassScope classScope) {
		// Answer the signature return type
		// Base type promotion

		constant = NotAConstant;
		if (this.receiver instanceof CastExpression) this.receiver.bits |= IgnoreNeedForCastCheckMASK; // will check later on
		if (this.receiver == null) {
			this.receiverType = classScope.enclosingSourceType();
		}
		else if (this.receiver instanceof TypeReference) {
			TypeReference typeRef = (TypeReference) receiver;
			this.receiverType = typeRef.getTypeBinding(classScope);
			if (!this.receiverType.isValidBinding()) {
				classScope.problemReporter().invalidType(this.receiver, this.receiverType);
				return null;
			}
		}
		this.qualifyingType = this.receiverType;
			

		// will check for null after args are resolved
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			boolean argHasError = false; // typeChecks all arguments 
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++){
				Expression expr = arguments[i];
				if (expr instanceof AnnotationArgumentExpression) {
					AnnotationArgumentExpression argument = (AnnotationArgumentExpression) expr;
					if ((argumentTypes[i] = argument.resolveType(classScope)) == null){
						argHasError = true;
					}
				} else {
					classScope.problemReporter().annotationInvalidSeeReferenceArgs(expr.sourceStart, expr.sourceEnd);
					return null;
				}
			}
			if (argHasError) {
				if(receiverType instanceof ReferenceBinding) {
					// record any selector match, for clients who may still need hint about possible method match
					this.codegenBinding = this.binding = classScope.findMethod((ReferenceBinding)receiverType, selector, new TypeBinding[]{}, this);
				}			
				return null;
			}
		}
		if (this.receiverType == null) {
			return null;
		}

		// base type cannot receive any message
		if (this.receiverType.isBaseType()) {
			classScope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
			return null;
		}
		this.codegenBinding = this.binding = classScope.getMethod(this.receiverType, selector, argumentTypes, this); 
		if (!this.binding.isValidBinding()) {
			if (this.binding.declaringClass == null) {
				if (this.receiverType instanceof ReferenceBinding) {
					binding.declaringClass = (ReferenceBinding) this.receiverType;
				} else { 
					classScope.problemReporter().errorNoMethodFor(this, this.receiverType, argumentTypes);
					return null;
				}
			}
			classScope.problemReporter().invalidMethod(this, binding);
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
		if (isMethodUseDeprecated(binding, classScope)) {
			classScope.problemReporter().deprecatedMethod(binding, this);
		}

		return this.resolvedType = binding.returnType;
	}

	/**
	 * Redefine to capture annotation specific signatures
	 */
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
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
