/*******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Subclass of the parser used to parse different code snippets
 */
public class CodeFormatterParser extends Parser {

	public CodeFormatterParser(
			ProblemReporter problemReporter,
			boolean optimizeStringLiterals) {
		super(problemReporter, optimizeStringLiterals);
		this.javadocParser.checkJavadoc = false;
	}

	public Expression parseExpression(char[] source, CompilationUnitDeclaration unit) {
	
		initialize();
		goForExpression();
		nestedMethod[nestedType]++;
	
		referenceContext = unit;
		compilationUnit = unit;
	
		scanner.setSource(source);
		scanner.resetTo(0, source.length-1);
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;
		} finally {
			nestedMethod[nestedType]--;
		}
	
		if (lastAct == ERROR_ACTION) {
			return null;
		}
	
		return expressionStack[expressionPtr];
	}
	
	public ASTNode[] parseClassBodyDeclarations(char[] source, CompilationUnitDeclaration unit) {
		/* automaton initialization */
		initialize();
		goForClassBodyDeclarations();
		/* scanner initialization */
		scanner.setSource(source);
		scanner.resetTo(0, source.length - 1);

		/* type declaration should be parsed as member type declaration */	
		nestedType = 1;

		/* unit creation */
		referenceContext = unit;
		compilationUnit = unit;

		/* run automaton */
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;
		}
	
		if (lastAct == ERROR_ACTION) {
			return null;
		}
		int length;
		if ((length = astLengthStack[astLengthPtr--]) != 0) {
			ASTNode[] result = new ASTNode[length];
			astPtr -= length;
			System.arraycopy(astStack, astPtr + 1, result, 0, length);
			return result;
		} else {
			return null;
		}
	}	
}
