package org.eclipse.jdt.internal.eval;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;

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
		codeStream.recordPositionsFrom(pc, type);

		// handling innerclass instance allocation
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticArgumentValues(
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
				if (binding.parameters[i].isBaseType()) {
					codeStream.generateObjectWrapperForType(binding.parameters[i]);
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
	codeStream.recordPositionsFrom(pc, this);
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
	TypeBinding typeBinding = type.resolveType(scope); // will check for null after args are resolved

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
			return null;
	}
	if (typeBinding == null)
		return null;

	if (!typeBinding.canBeInstantiated()) {
		scope.problemReporter().cannotInstantiate(type, typeBinding);
		return null;
	}
	ReferenceBinding allocatedType = (ReferenceBinding) typeBinding;
	if (!(binding = scope.getConstructor(allocatedType, argumentTypes, this)).isValidBinding()) {
		if (binding instanceof ProblemMethodBinding
			&& ((ProblemMethodBinding) binding).problemId() == NotVisible) {
			if (this.evaluationContext.declaringTypeName != null) {
				delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
				if (delegateThis == null) {
					if (binding.declaringClass == null)
						binding.declaringClass = allocatedType;
					scope.problemReporter().invalidConstructor(this, binding);
					return null;
				}
			} else {
				if (binding.declaringClass == null)
					binding.declaringClass = allocatedType;
				scope.problemReporter().invalidConstructor(this, binding);
				return null;
			}
			CodeSnippetScope localScope = new CodeSnippetScope(scope);			
			MethodBinding privateBinding = localScope.getConstructor((ReferenceBinding)delegateThis.type, argumentTypes, this);
			if (!privateBinding.isValidBinding()) {
				if (binding.declaringClass == null)
					binding.declaringClass = allocatedType;
				scope.problemReporter().invalidConstructor(this, binding);
				return null;
			} else {
				binding = privateBinding;
			}				
		} else {
			if (binding.declaringClass == null)
				binding.declaringClass = allocatedType;
			scope.problemReporter().invalidConstructor(this, binding);
			return null;
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
