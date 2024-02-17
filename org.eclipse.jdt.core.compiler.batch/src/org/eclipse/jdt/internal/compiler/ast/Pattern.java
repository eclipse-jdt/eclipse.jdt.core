/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	/* package */ boolean isTotalTypeNode = false;

	private Pattern enclosingPattern;
	protected MethodBinding accessorMethod;

	public int index = -1; // index of this in enclosing record pattern, or -1 for top level patterns

	/**
	 * @return the enclosingPattern
	 */
	public Pattern getEnclosingPattern() {
		return this.enclosingPattern;
	}

	/**
	 * @param enclosingPattern the enclosingPattern to set
	 */
	public void setEnclosingPattern(RecordPattern enclosingPattern) {
		this.enclosingPattern = enclosingPattern;
	}

	public boolean isUnnamed() {
		return false;
	}

	/**
	 * Implement the rules in the spec under 14.11.1.1 Exhaustive Switch Blocks
	 *
	 * @return whether pattern covers the given type or not
	 */
	public boolean coversType(TypeBinding type) {
		return false;
	}

	public boolean isAlwaysTrue() {
		return true;
	}

	public abstract void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel);

	public TypeReference getType() {
		return null;
	}

	// 14.30.3 Properties of Patterns: A pattern p is said to be applicable at a type T if ...
	protected abstract boolean isApplicable(TypeBinding other, BlockScope scope);

	public abstract boolean dominates(Pattern p);

	@Override
	public StringBuilder print(int indent, StringBuilder output) {
		return this.printExpression(indent, output);
	}
}