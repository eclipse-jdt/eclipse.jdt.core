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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class Argument extends LocalDeclaration {
	
	// prefix for setter method (to recognize special hiding argument)
	private final static char[] SET = "set".toCharArray(); //$NON-NLS-1$

	public Argument(char[] name, long posNom, TypeReference tr, int modifiers) {

		super(null, name, (int) (posNom >>> 32), (int) posNom);
		this.declarationSourceEnd = (int) posNom;
		this.modifiers = modifiers;
		type = tr;
		this.bits |= IsLocalDeclarationReachableMASK;
	}

	public void bind(MethodScope scope, TypeBinding typeBinding, boolean used) {

		if (this.type != null)
			this.type.resolvedType = typeBinding;
		// record the resolved type into the type reference
		int modifierFlag = this.modifiers;

		Binding existingVariable = scope.getBinding(name, BindingIds.VARIABLE, this);
		if (existingVariable != null && existingVariable.isValidBinding()){
			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				scope.problemReporter().redefineArgument(this);
				return;
			} else {
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
		}

		scope.addLocalVariable(
			this.binding =
				new LocalVariableBinding(this, typeBinding, modifierFlag, true));
		//true stand for argument instead of just local
		if (typeBinding != null && isTypeUseDeprecated(typeBinding, scope))
			scope.problemReporter().deprecatedType(typeBinding, this.type);
		this.binding.declaration = this;
		this.binding.useFlag = used ? LocalVariableBinding.USED : LocalVariableBinding.UNUSED;
	}

	public TypeBinding resolveForCatch(BlockScope scope) {

		// resolution on an argument of a catch clause
		// provide the scope with a side effect : insertion of a LOCAL
		// that represents the argument. The type must be from JavaThrowable

		TypeBinding tb = type.resolveTypeExpecting(scope, scope.getJavaLangThrowable());
		if (tb == null)
			return null;

		Binding existingVariable = scope.getBinding(name, BindingIds.VARIABLE, this);
		if (existingVariable != null && existingVariable.isValidBinding()){
			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				scope.problemReporter().redefineArgument(this);
				return null;
			} else {
				scope.problemReporter().localVariableHiding(this, existingVariable, false);
			}
		}

		binding = new LocalVariableBinding(this, tb, modifiers, false); // argument decl, but local var  (where isArgument = false)
		scope.addLocalVariable(binding);
		binding.constant = NotAConstant;
		return tb;
	}

	public String toString(int tab) {

		String s = ""; //$NON-NLS-1$
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		if (type == null) {
			s += "<no type> "; //$NON-NLS-1$
		} else {
			s += type.toString(tab) + " "; //$NON-NLS-1$
		}
		s += new String(name);
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			if (type != null)
				type.traverse(visitor, scope);
			if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
