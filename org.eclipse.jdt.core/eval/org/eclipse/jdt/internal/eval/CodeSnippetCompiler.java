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
package org.eclipse.jdt.internal.eval;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * A compiler that compiles code snippets. 
 */
public class CodeSnippetCompiler extends Compiler {
	/**
	 * Creates a new code snippet compiler initialized with a code snippet parser.
	 */
	public CodeSnippetCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory,
		EvaluationContext evaluationContext,
		int codeSnippetStart,
		int codeSnippetEnd) {
		super(environment, policy, settings, requestor, problemFactory);
		this.parser =
			new CodeSnippetParser(
				problemReporter,
				evaluationContext,
				this.options.parseLiteralExpressionsAsConstants,
				this.options.sourceLevel >= CompilerOptions.JDK1_4,
				codeSnippetStart,
				codeSnippetEnd);
		this.parseThreshold = 1;
		// fully parse only the code snippet compilation unit
	}
}
