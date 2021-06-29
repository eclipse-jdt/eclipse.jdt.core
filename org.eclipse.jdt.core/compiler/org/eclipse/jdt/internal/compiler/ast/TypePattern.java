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
import org.eclipse.jdt.internal.compiler.lookup.AnyPatternBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.PatternBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypePatternBinding;

public class TypePattern extends AbstractTypePattern {

	public TypePattern(LocalDeclaration local) {
		this.local = local;
	}

	@Override
	public PatternKind kind() {
		return PatternKind.TYPE_PATTERN;
	}

	@Override
	public String getKindName() {
		return TypeConstants.TYPE_PATTERN_STRING;
	}

	@Override
	public boolean isTotalForType(TypeBinding type) {
		if (this.resolvedType == null || type == null)
			return false;
		return type.erasure().isSubtypeOf(this.resolvedType.erasure(), false);
	}
	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		if (this.resolvedPattern == null) {
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
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if (this.local != null) {
			LocalVariableBinding localBinding = this.local.binding;
			codeStream.checkcast(localBinding.type);
			this.local.generateCode(currentScope, codeStream);
			codeStream.store(localBinding, false);
			localBinding.recordInitializationStartPC(codeStream.position);
		}
	}
	/*
	 * A type pattern, p, declaring a pattern variable x of type T, that is total for U,
	 * is resolved to an any pattern that declares x of type T;
	 * otherwise it is resolved to p.
	 */
	@Override
	public PatternBinding resolveAtType(BlockScope scope, TypeBinding u) {
		if (this.resolvedPattern == null) {
			this.resolvedPattern = new TypePatternBinding(this.local.binding);
		}
		return this.isTotalForType(u) ? new AnyPatternBinding(this.local.binding) : this.resolvedPattern;
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
		this.local.type.printExpression(0, output);
		output.append(' ');
		return output.append(this.local.name);
	}
}
