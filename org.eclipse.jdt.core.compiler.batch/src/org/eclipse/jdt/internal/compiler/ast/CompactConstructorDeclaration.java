/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class CompactConstructorDeclaration extends ConstructorDeclaration {

	public CompactConstructorDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}
	@Override
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		this.constructorCall = SuperReference.implicitSuperConstructorCall();
		parser.parse(this, unit, false);
	}
	@Override
	public void analyseCode(ClassScope classScope, InitializationFlowContext initializerFlowContext, FlowInfo flowInfo, int initialReachMode) {
		try {
			this.scope.isCompactConstructorScope = true;
			super.analyseCode(classScope, initializerFlowContext, flowInfo, initialReachMode);
		} finally {
			this.scope.isCompactConstructorScope = false;
		}
	}
	@Override
	protected void doFieldReachAnalysis(FlowInfo flowInfo, FieldBinding[] fields) {
		// do nothing
	}

	@Override
	public void resolve(ClassScope upperScope) {
		if (!upperScope.referenceContext.isRecord()) {
			upperScope.problemReporter().compactConstructorsOnlyInRecords(this);
			return;
		}
		super.resolve(upperScope);
	}
}