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

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends ASTNode {

	public enum PatternKind {
		TYPE_PATTERN,
		GUARDED_PATTERN,
		NOT_A_PATTERN
	}

	public int parenthesisSourceStart;
	public int parenthesisSourceEnd;

	public abstract FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo);

	public abstract void generateCode(BlockScope currentScope, CodeStream codeStream);

	// TODO: Recheck whether we need this in Pattern
	public abstract void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired);

	public abstract void resolve(BlockScope scope);

	public abstract TypeBinding resolveType(BlockScope scope);

	public PatternKind kind() {
		return PatternKind.NOT_A_PATTERN;
	}
}