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
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class GuardedPattern extends Pattern {

	public Pattern primaryPattern;
	public Expression conditionalAndExpression;

	public GuardedPattern(Pattern primaryPattern, Expression conditionalAndExpression) {
		this.primaryPattern = primaryPattern;
		this.conditionalAndExpression = conditionalAndExpression;
		this.sourceStart = primaryPattern.sourceStart;
		this.sourceEnd = conditionalAndExpression.sourceEnd;
	}

	@Override
	public PatternKind kind() {
		return PatternKind.GUARDED_PATTERN;
	}

	@Override
	public String getKindName() {
		return TypeConstants.GUARDED_PATTERN_STRING;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractVariableDeclaration[] getPatternVariables() {
		return this.primaryPattern.getPatternVariables();
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// TODO Auto-generated method stub

	}
	@Override
	public void resolve(BlockScope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public StringBuffer printPattern(int indent, StringBuffer output) {
		this.primaryPattern.print(indent, output).append(" && "); //$NON-NLS-1$
		return this.conditionalAndExpression.print(indent, output);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.primaryPattern != null)
				this.primaryPattern.traverse(visitor, scope);
			if (this.conditionalAndExpression != null)
				this.conditionalAndExpression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

}