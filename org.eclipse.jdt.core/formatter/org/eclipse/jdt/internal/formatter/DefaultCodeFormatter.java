/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.CodeFormatter;
import org.eclipse.jdt.core.ICodeFormatter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class DefaultCodeFormatter extends CodeFormatter implements ICodeFormatter {
	
	private int[] positionsMapping;

	public static final boolean DEBUG = false;
	
	private CodeFormatterVisitor newCodeFormatter;
	private Map options;
	private FormattingPreferences preferences;

	private static AstNode[] parseClassBodyDeclarations(char[] source, Map settings) {
		
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		final ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault()));
					
		CodeFormatterParser parser =
			new CodeFormatterParser(
				problemReporter,
			false,
			compilerOptions.sourceLevel);

		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		return parser.parseClassBodyDeclarations(source, new CompilationUnitDeclaration(problemReporter, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit), source.length));
	}

	private static CompilationUnitDeclaration parseCompilationUnit(char[] source, Map settings) {
		
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
			false,
			compilerOptions.sourceLevel);
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

	private static Expression parseExpression(char[] source, Map settings) {
		
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		final ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault()));
					
		CodeFormatterParser parser =
			new CodeFormatterParser(
				problemReporter,
			false,
			compilerOptions.sourceLevel);

		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		return parser.parseExpression(source, new CompilationUnitDeclaration(problemReporter, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit), source.length));
	}

	private static ConstructorDeclaration parseStatements(char[] source, Map settings) {
		
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		final ProblemReporter problemReporter = new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault()));
		Parser parser =
			new Parser(
				problemReporter,
			false,
			compilerOptions.sourceLevel);
		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		final CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration compilationUnitDeclaration = new CompilationUnitDeclaration(problemReporter, compilationResult, source.length);		

		ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);
		constructorDeclaration.sourceEnd  = -1;
		constructorDeclaration.declarationSourceEnd = source.length + 1;
		
		parser.scanner.setSource(CharOperation.concat('{', source, '}'));
		parser.parse(constructorDeclaration, compilationUnitDeclaration);
		
		return constructorDeclaration;
	}
	
	public DefaultCodeFormatter() {
		this.preferences = FormattingPreferences.getSunSetttings();
		this.options = JavaCore.getOptions();
	}
	
	public DefaultCodeFormatter(FormattingPreferences preferences, Map options) {
		this.preferences = preferences;
		this.options = options;
	}

	/**
	 * @see CodeFormatter#format(String, int, int[], String, int)
	 */
	public String format(
			int kind,
			String source,
			int indentationLevel,
			int[] positions,
			String lineSeparator) {
				
		switch(kind) {
			case K_CLASS_BODY_DECLARATIONS :
				return formatClassBodyDeclarations(source, indentationLevel, positions, lineSeparator);
			case K_COMPILATION_UNIT :
				return formatCompilationUnit(source, indentationLevel, positions, lineSeparator);
			case K_EXPRESSION :
				return formatExpression(source, indentationLevel, positions, lineSeparator);
			case K_STATEMENTS :
				return formatStatements(source, indentationLevel, positions, lineSeparator);
		}
		this.positionsMapping = positions;
		return source;
	}
	
	public String format(
		String string,
		int start,
		int end,
		int indentationLevel,
		int[] positions,
		String lineSeparator) {
		// TODO: Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.core.ICodeFormatter#format(String, int, int[], String)
	 */
	public String format(
		String source,
		int indentationLevel,
		int[] positions,
		String lineSeparator) {
			return format(K_COMPILATION_UNIT, source, indentationLevel, positions, lineSeparator);
	}

	private String formatClassBodyDeclarations(String source, int indentationLevel, int[] positions, String lineSeparator) {
		AstNode[] bodyDeclarations = parseClassBodyDeclarations(source.toCharArray(), this.options);
		
		this.preferences.line_delimiter = lineSeparator;
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, this.options);
		
		String result = this.newCodeFormatter.format(source, positions, bodyDeclarations);
		if (positions != null) {
			System.arraycopy(this.newCodeFormatter.scribe.mappedPositions, 0, positions, 0, positions.length);
		}
		return result;
	}

	private String formatCompilationUnit(String source, int indentationLevel, int[] positions, String lineSeparator) {
		CompilationUnitDeclaration compilationUnitDeclaration = parseCompilationUnit(source.toCharArray(), this.options);
		
		this.preferences.line_delimiter = lineSeparator;
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, this.options);
		
		String result = this.newCodeFormatter.format(source, positions, compilationUnitDeclaration);
		if (positions != null) {
			System.arraycopy(this.newCodeFormatter.scribe.mappedPositions, 0, positions, 0, positions.length);
		}
		return result;
	}

	private String formatExpression(String source, int indentationLevel, int[] positions, String lineSeparator) {
		Expression expression = parseExpression(source.toCharArray(), this.options);
		
		this.preferences.line_delimiter = lineSeparator;
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, this.options);
		
		String result = this.newCodeFormatter.format(source, positions, expression);
		if (positions != null) {
			System.arraycopy(this.newCodeFormatter.scribe.mappedPositions, 0, positions, 0, positions.length);
		}
		return result;
	}

	private String formatStatements(String source, int indentationLevel, int[] positions, String lineSeparator) {
		ConstructorDeclaration constructorDeclaration = parseStatements(source.toCharArray(), this.options);
		
		this.preferences.line_delimiter = lineSeparator;
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, this.options);
		
		String result = this.newCodeFormatter.format(source, positions, constructorDeclaration);;
		if (positions != null) {
			System.arraycopy(this.newCodeFormatter.scribe.mappedPositions, 0, positions, 0, positions.length);
		}
		return result;
	}
	
	public String getDebugOutput() {
		return this.newCodeFormatter.scribe.toString();
	}
	
	public int[] getMappedPositions() {
		return this.positionsMapping;
	}
}
