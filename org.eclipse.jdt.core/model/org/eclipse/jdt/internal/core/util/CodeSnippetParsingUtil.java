/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Utility class to parse different code snippets
 */
public class CodeSnippetParsingUtil {

	public IProblem problems[];
	public int lineSeparatorPositions[];

	public ASTNode[] parseClassBodyDeclarations(char[] source, Map settings) {
		return parseClassBodyDeclarations(source, 0, source.length, settings);
	}
	
	public ASTNode[] parseClassBodyDeclarations(char[] source, int offset, int length, Map settings) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		final ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault()));
					
		Parser parser =
			new Parser(problemReporter, false);
		parser.javadocParser.checkJavadoc = false;

		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
		final CompilationUnitDeclaration compilationUnitDeclaration = new CompilationUnitDeclaration(problemReporter, compilationResult, source.length);
		ASTNode[] result = parser.parseClassBodyDeclarations(source, offset, length, compilationUnitDeclaration);
		
		this.problems = compilationResult.problems;
		this.lineSeparatorPositions = compilationResult.lineSeparatorPositions;
		return result;
	}

	public CompilationUnitDeclaration parseCompilationUnit(char[] source, Map settings) {
		
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault())),
			false);
		parser.javadocParser.checkJavadoc = false;
		
		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);
		CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit));
		
		if (compilationUnitDeclaration.ignoreMethodBodies) {
			compilationUnitDeclaration.ignoreFurtherInvestigation = true;
			// if initial diet parse did not work, no need to dig into method bodies.
			return compilationUnitDeclaration; 
		}
		
		//fill the methods bodies in order for the code to be generated
		//real parse of the method....
		parser.scanner.setSource(source);
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
		if (types != null) {
			for (int i = types.length; --i >= 0;) {
				types[i].parseMethod(parser, compilationUnitDeclaration);
			}
		}
		return compilationUnitDeclaration;
	}

	public Expression parseExpression(char[] source, Map settings) {
		return parseExpression(source, 0, source.length, settings);
	}
	
	public Expression parseExpression(char[] source, int offset, int length, Map settings) {
		
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		final ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault()));
					
		Parser parser = new Parser(problemReporter, false);
		parser.javadocParser.checkJavadoc = false;

		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
		Expression result = parser.parseExpression(source, offset, length, new CompilationUnitDeclaration(problemReporter, compilationResult, source.length));
		this.problems = compilationResult.problems;
		this.lineSeparatorPositions = compilationResult.lineSeparatorPositions;
		return result;
	}

	public ConstructorDeclaration parseStatements(char[] source, Map settings) {
		return parseStatements(source, 0, source.length, settings);
	}
	
	public ConstructorDeclaration parseStatements(char[] source, int offset, int length, Map settings) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		final ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault()));
		Parser parser = new Parser(problemReporter, false);
		parser.javadocParser.checkJavadoc = false;
		
		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		final CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration compilationUnitDeclaration = new CompilationUnitDeclaration(problemReporter, compilationResult, length);		

		ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);
		constructorDeclaration.sourceEnd  = -1;
		constructorDeclaration.declarationSourceEnd = offset + length - 1;
		constructorDeclaration.bodyStart = offset;
		constructorDeclaration.bodyEnd = offset + length - 1;
		
		parser.scanner.setSource(source);
		parser.scanner.resetTo(offset, offset + length);
		parser.parse(constructorDeclaration, compilationUnitDeclaration, true);
		
		this.problems = compilationResult.problems;
		this.lineSeparatorPositions = compilationResult.lineSeparatorPositions;
		return constructorDeclaration;
	}	
}
