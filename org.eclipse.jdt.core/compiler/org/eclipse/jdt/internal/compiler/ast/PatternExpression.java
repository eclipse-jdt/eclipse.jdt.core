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
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class PatternExpression extends Expression {

	public Pattern pattern;

	public PatternExpression(Pattern pattern) {
		this.pattern = pattern;
		this.sourceStart = pattern.sourceStart();
		this.sourceEnd = pattern.sourceEnd();
	}

	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		this.pattern.collectPatternVariablesToScope(variables, scope);
		addPatternVariablesWhenTrue(this.pattern.getPatternVariablesWhenTrue());
	}

	@Override
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
		return this.pattern.analyseCode(currentScope, flowContext, flowInfo);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		this.pattern.generateCode(currentScope, codeStream);
	}
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		// by default... subclasses should implement a better TB if required.
		return this.pattern != null ? this.pattern.resolveType(scope) : null;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		return this.printExpression(indent, output);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return this.pattern != null ? this.pattern.print(indent, output) : output;
	}
	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.pattern != null)
				this.pattern.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}