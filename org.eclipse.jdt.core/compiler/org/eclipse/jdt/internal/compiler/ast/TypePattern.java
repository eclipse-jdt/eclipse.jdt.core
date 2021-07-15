/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class TypePattern extends Pattern {

	public LocalDeclaration local;

	public TypePattern(LocalDeclaration local) {
		this.local = local;
	}

	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		if (this.resolvedType == null) {
			this.resolveType(scope);
		}
		if (this.local != null && this.local.binding != null) {
			if (this.patternVarsWhenTrue == null) {
				this.patternVarsWhenTrue = new LocalVariableBinding[1];
				this.patternVarsWhenTrue[0] = this.local.binding;
			} else {
				this.addPatternVariablesWhenTrue(new LocalVariableBinding[] {this.local.binding});
			}
		}
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo.markAsDefinitelyAssigned(this.local.binding);
		return flowInfo;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if (this.local != null) {
			LocalVariableBinding localBinding = this.local.binding;
			codeStream.checkcast(localBinding.type);
			this.local.generateCode(currentScope, codeStream);
			codeStream.store(localBinding, false);
			localBinding.recordInitializationStartPC(codeStream.position);
		}
	}

	@Override
	public LocalDeclaration getPatternVariableIntroduced() {
		return this.local;
	}

	@Override
	public void resolve(BlockScope scope) {
		this.resolveType(scope);
	}

	@Override
	public boolean isAnyPattern() {
		// Not sufficient enough, but will do for the time being.
		return this.local.type != null &&  (this.local.type.bits & IsVarArgs) != 0;
	}
	/*
	 * A type pattern, p, declaring a pattern variable x of type T, that is total for U,
	 * is resolved to an any pattern that declares x of type T;
	 * otherwise it is resolved to p.
	 */
	@Override
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding u) {
		if (this.resolvedType == null) {
			this.resolvedType = this.local.binding.type;
		}
		return this.resolvedType;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null || this.local == null)
			return this.resolvedType;

		this.local.modifiers |= ExtraCompilerModifiers.AccPatternVariable;
		this.local.resolve(scope, true);
		if (this.local.binding != null) {
			this.local.binding.modifiers |= ExtraCompilerModifiers.AccPatternVariable;
			this.local.binding.useFlag = LocalVariableBinding.USED;
			this.resolvedType = this.local.binding.type;
		}
		return this.resolvedType;
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.local != null)
				this.local.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return this.local != null ? this.local.printAsExpression(indent, output) : output;
	}
}
