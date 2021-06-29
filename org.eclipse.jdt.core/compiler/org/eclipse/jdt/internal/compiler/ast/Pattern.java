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

import java.util.function.Supplier;

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.PatternBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	public enum PatternKind {
		ANY_PATTERN,
		GUARDED_PATTERN,
		TYPE_PATTERN,
		NOT_A_PATTERN,
	}

	public int parenthesisSourceStart;
	public int parenthesisSourceEnd;

	PatternBinding resolvedPattern;

	public abstract LocalDeclaration[] getPatternVariables();

	public abstract String getKindName(); // convenience method.

	public abstract boolean isTotalForType(TypeBinding type);

	public abstract PatternKind kind();

	public abstract PatternBinding resolveAtType(BlockScope scope, TypeBinding type);

	public void setTargetSupplier(Supplier<BranchLabel> targetSupplier) {
		// default implementation does nothing
	}
}