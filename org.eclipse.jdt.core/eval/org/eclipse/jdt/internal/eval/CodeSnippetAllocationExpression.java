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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CodeSnippetAllocationExpression extends AllocationExpression implements ProblemReasons, EvaluationConstants {
	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetAllocationExpression constructor comment.
 */
public CodeSnippetAllocationExpression(EvaluationContext evaluationContext) {
	this.evaluationContext = evaluationContext;
}
public void generateCode(
	BlockScope currentScope, 
	CodeStream codeStream, 
	boolean valueRequired) {

	int pc = codeStream.position;
	ReferenceBinding allocatedType = binding.declaringClass;

	if (binding.canBeSeenBy(allocatedType, this, currentScope)) {
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
		codeStream.invokespecial(binding);
	} else {
		// private emulation using reflect
		((CodeSnippetCodeStream) codeStream).generateEmulationForConstructor(currentScope, binding);
		// generate arguments
		if (arguments != null) {
			int argsLength = arguments.length;
			codeStream.generateInlinedValue(argsLength);
			codeStream.newArray(currentScope, new ArrayBinding(currentScope.getType(TypeBinding.JAVA_LANG_OBJECT), 1));
			codeStream.dup();
			for (int i = 0; i < argsLength; i++) {
				codeStream.generateInlinedValue(i);
				arguments[i].generateCode(currentScope, codeStream, true);
				TypeBinding parameterBinding = binding.parameters[i];
				if (parameterBinding.isBaseType() && parameterBinding != NullBinding) {
					((CodeSnippetCodeStream)codeStream).generateObjectWrapperForType(binding.parameters[i]);
				}
				codeStream.aastore();
				if (i < argsLength - 1) {
					codeStream.dup();
				}	
			}
		} else {
			codeStream.generateInlinedValue(0);
			codeStream.newArray(currentScope, new ArrayBinding(currentScope.getType(TypeBinding.JAVA_LANG_OBJECT), 1));			
		}
		((CodeSnippetCodeStream) codeStream).invokeJavaLangReflectConstructorNewInstance();
		codeStream.checkcast(allocatedType);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
/* Inner emulation consists in either recording a dependency 
 * link only, or performing one level of propagation.
 *
 * Dependency mechanism is used whenever dealing with source target
 * types, since by the time we reach them, we might not yet know their
 * exact need.
 */
public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {
	// not supported yet
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {
}
public TypeBinding resolveType(BlockScope scope) {
	// Propagate the type checking to the arguments, and check if the constructor is defined.
	constant = NotAConstant;
	this.resolvedType = type.resolveType(scope); // will check for null after args are resolved

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
	if (!(binding = scope.getConstructor(allocatedType, argumentTypes, this)).isValidBinding()) {
		if (binding instanceof ProblemMethodBinding
			&& ((ProblemMethodBinding) binding).problemId() == NotVisible) {
			if (this.evaluationContext.declaringTypeName != null) {
				delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
				if (delegateThis == null) {
					if (binding.declaringClass == null)
						binding.declaringClass = allocatedType;
					scope.problemReporter().invalidConstructor(this, binding);
					return this.resolvedType;
				}
			} else {
				if (binding.declaringClass == null)
					binding.declaringClass = allocatedType;
				scope.problemReporter().invalidConstructor(this, binding);
				return this.resolvedType;
			}
			CodeSnippetScope localScope = new CodeSnippetScope(scope);			
			MethodBinding privateBinding = localScope.getConstructor((ReferenceBinding)delegateThis.type, argumentTypes, this);
			if (!privateBinding.isValidBinding()) {
				if (binding.declaringClass == null)
					binding.declaringClass = allocatedType;
				scope.problemReporter().invalidConstructor(this, binding);
				return this.resolvedType;
			} else {
				binding = privateBinding;
			}				
		} else {
			if (binding.declaringClass == null)
				binding.declaringClass = allocatedType;
			scope.problemReporter().invalidConstructor(this, binding);
			return this.resolvedType;
		}
	}
	if (isMethodUseDeprecated(binding, scope))
		scope.problemReporter().deprecatedMethod(binding, this);

	if (arguments != null)
		for (int i = 0; i < arguments.length; i++)
			arguments[i].implicitWidening(binding.parameters[i], argumentTypes[i]);
	return allocatedType;
}
}
