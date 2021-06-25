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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypePatternBinding;

public abstract class AbstractTypePattern extends Pattern {

	public LocalDeclaration local;

	public static AbstractTypePattern createPattern(LocalDeclaration local) {
		char[][] name = (local != null && local.type != null) ? local.type.getTypeName() : null;
		return name != null &&  CharOperation.toString(name).equals(TypeConstants.VAR_STRING) ?
				new AnyPattern(local)
				: new TypePattern(local);
	}

	// TODO: BUG 573940 to implement this method - THIS IS A PLACEHOLDER
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo.markAsDefinitelyAssigned(this.local.binding);
		return flowInfo;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalDeclaration[] getPatternVariables() {
		return new LocalDeclaration[] { this.local };
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resolve(BlockScope scope) {
		this.resolveType(scope);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null || this.local == null)
			return this.resolvedType;

		this.local.resolve(scope);
		if (this.local.binding != null) {
			this.resolvedType = this.local.binding.type;
			this.resolvedPattern = new TypePatternBinding(this.local.binding);
		}
		return this.resolvedType;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		return this.local != null ? this.local.printAsExpression(indent, output) : output;
	}

}
