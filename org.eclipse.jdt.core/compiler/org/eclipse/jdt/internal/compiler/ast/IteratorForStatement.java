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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class IteratorForStatement extends Statement {

	public Expression collection;
	public LocalDeclaration localDeclaration;

	public Statement action;

	// we always need a new scope.
	public BlockScope scope;

	public IteratorForStatement(
		LocalDeclaration localDeclaration,
		Expression collection,
		Statement action,
		int start,
		int end) {

		this.localDeclaration = localDeclaration;
		this.collection = collection;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.action = action;
		// remember useful empty statement
		if (action instanceof EmptyStatement) action.bits |= IsUsefulEmptyStatementMASK;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
		// TODO to be completed
		return flowInfo;
	}

	/**
	 * For statement code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// TODO to be completed
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("for ("); //$NON-NLS-1$
		this.localDeclaration.print(0, output); 
		output.append(" : ");//$NON-NLS-1$
		this.collection.print(0, output).append(") "); //$NON-NLS-1$
		//block
		if (action == null)
			output.append(';');
		else {
			output.append('\n');
			action.printStatement(tab + 1, output); //$NON-NLS-1$
		}
		return output;
	}

	public void resetStateForCodeGeneration() {
		// TODO to be completed
	}

	public void resolve(BlockScope upperScope) {
		// TODO to be completed
	}
	
	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.localDeclaration.traverse(visitor, scope);
			this.collection.traverse(visitor, scope);
			if (action != null) {
				action.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}
