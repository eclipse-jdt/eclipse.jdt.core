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
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public abstract class AbstractTypePattern extends Pattern {

	public LocalDeclaration local;

	public static AbstractTypePattern createPattern(LocalDeclaration local) {
		char[][] name = (local != null && local.type != null) ? local.type.getTypeName() : null;
		return name != null &&  CharOperation.toString(name).equals(TypeConstants.VAR_STRING) ?
				new AnyPattern(local)
				: new TypePattern(local);
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractVariableDeclaration[] getPatternVariables() {
		return new LocalDeclaration[] { this.local };
	}


	@Override
	public void resolve(BlockScope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public StringBuffer printPattern(int indent, StringBuffer output) {
		return this.local != null ? this.local.printAsExpression(indent, output) : output;
	}

}
