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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Argument extends LocalDeclaration {
	
	// prefix for setter method (to recognize special hiding argument)
	private final static char[] SET = "set".toCharArray(); //$NON-NLS-1$
	public boolean isVarArgs;
	
	public Argument(char[] name, long posNom, TypeReference tr, int modifiers, boolean isVarArgs) {

		super(name, (int) (posNom >>> 32), (int) posNom);
		this.declarationSourceEnd = (int) posNom;
		this.modifiers = modifiers;
		type = tr;
		this.bits |= IsLocalDeclarationReachableMASK;
		this.isVarArgs = isVarArgs;
	}

	public void bind(MethodScope scope, TypeBinding typeBinding, boolean used) {

		if (this.type != null)
			this.type.resolvedType = typeBinding; // TODO (philippe) no longer necessary as when binding got resolved, it was recorded already (SourceTypeBinding#resolveTypesFor(MethodBinding))
		// record the resolved type into the type reference
		int modifierFlag = this.modifiers;

		Binding existingVariable = scope.getBinding(name, BindingIds.VARIABLE, this, false /*do not resolve hidden field*/);
		if (existingVariable != null && existingVariable.isValidBinding()){
			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				scope.problemReporter().redefineArgument(this);
				return;
			}
			boolean isSpecialArgument = false;
			if (existingVariable instanceof FieldBinding) {
				if (scope.isInsideConstructor()) {
					isSpecialArgument = true; // constructor argument
				} else {
					AbstractMethodDeclaration methodDecl = scope.referenceMethod();
					if (methodDecl != null && CharOperation.prefixEquals(SET, methodDecl.selector)) {
						isSpecialArgument = true; // setter argument
					}
				}
			}
			scope.problemReporter().localVariableHiding(this, existingVariable, isSpecialArgument);
		}

		scope.addLocalVariable(
			this.binding =
				new LocalVariableBinding(this, typeBinding, modifierFlag, true));
		//true stand for argument instead of just local
		this.binding.declaration = this;
		this.binding.useFlag = used ? LocalVariableBinding.USED : LocalVariableBinding.UNUSED;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output);
		printModifiers(this.modifiers, output);
		if (type == null) {
			output.append("<no type> "); //$NON-NLS-1$
		} else {
			type.print(0, output).append(' '); 
		}
		return output.append(this.name);
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		return print(indent, output).append(';');
	}	

	public TypeBinding resolveForCatch(BlockScope scope) {

		// resolution on an argument of a catch clause
		// provide the scope with a side effect : insertion of a LOCAL
		// that represents the argument. The type must be from JavaThrowable

		TypeBinding exceptionType = this.type.resolveType(scope);
		if (exceptionType == null) return null;
		if (exceptionType.isGenericType() || exceptionType.isParameterizedType()) {
			scope.problemReporter().invalidParameterizedExceptionType(exceptionType, this);
			return null;
		}
		if (exceptionType.isTypeVariable()) {
			scope.problemReporter().invalidTypeVariableAsException(exceptionType, this);
			return null;
		}		
		TypeBinding throwable = scope.getJavaLangThrowable();
		if (!exceptionType.isCompatibleWith(throwable)) {
			scope.problemReporter().typeMismatchError(exceptionType, throwable, this);
			return null;
		}
		
		Binding existingVariable = scope.getBinding(name, BindingIds.VARIABLE, this, false /*do not resolve hidden field*/);
		if (existingVariable != null && existingVariable.isValidBinding()){
			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				scope.problemReporter().redefineArgument(this);
				return null;
			}
			scope.problemReporter().localVariableHiding(this, existingVariable, false);
		}

		binding = new LocalVariableBinding(this, exceptionType, modifiers, false); // argument decl, but local var  (where isArgument = false)
		scope.addLocalVariable(binding);
		binding.setConstant(NotAConstant);
		return exceptionType;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			if (type != null)
				type.traverse(visitor, scope);
			if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
