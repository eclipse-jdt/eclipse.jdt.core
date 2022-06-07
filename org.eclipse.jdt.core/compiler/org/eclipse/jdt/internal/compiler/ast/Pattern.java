/*******************************************************************************
 * Copyright (c) 2021, 2022 IBM Corporation and others.
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

import java.util.function.Supplier;

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	/* package */ boolean isTotalTypeNode = false;
	/* package */ static final char[] SecretPatternVariableName = "switchDispatchPattern".toCharArray(); //$NON-NLS-1$

	protected MethodBinding accessorMethod;

	public boolean isTotalForType(TypeBinding type) {
		return false;
	}
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding type) {
		return null;
	}
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		return resolveType(scope, true);
	}
	public TypeBinding resolveType(BlockScope scope, boolean isPatternVariable) {
		return null;
	}

	public TypeReference getType() {
		return null;
	}
	public abstract void resolveWithExpression(BlockScope scope, Expression expression);

	public void setTargetSupplier(Supplier<BranchLabel> targetSupplier) {
		// default implementation does nothing
	}

	public abstract boolean dominates(Pattern p);

	public Pattern primary() {
		return this;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		return this.printExpression(indent, output);
	}
}
