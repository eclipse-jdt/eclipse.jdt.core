/*******************************************************************************
 * Copyright (c) 2002, 2003 International Business Machines Corp. and others.
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

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.ICodeFormatter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

// TODO (olivier) remove dependency onto ICodeFormatter
public class DefaultCodeFormatter extends CodeFormatter implements ICodeFormatter {

	public static final boolean DEBUG = false;

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
			new CodeFormatterParser(problemReporter, false);

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
		CodeFormatterParser parser =
			new CodeFormatterParser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault())),
			false);
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
			new CodeFormatterParser(problemReporter, false);

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
		CodeFormatterParser parser = new CodeFormatterParser(problemReporter, false);
		ICompilationUnit sourceUnit = 
			new CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);

		final CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration compilationUnitDeclaration = new CompilationUnitDeclaration(problemReporter, compilationResult, source.length);		

		ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration(compilationResult);
		constructorDeclaration.sourceEnd  = -1;
		constructorDeclaration.declarationSourceEnd = source.length - 1;
		constructorDeclaration.bodyStart = 0;
		constructorDeclaration.bodyEnd = source.length - 1;
		
		parser.scanner.setSource(source);
		parser.parse(constructorDeclaration, compilationUnitDeclaration);
		
		return constructorDeclaration;
	}
	
	private CodeFormatterVisitor newCodeFormatter;
	
	private DefaultCodeFormatterOptions preferences;
	private Map options;
	
	public DefaultCodeFormatter() {
		this(DefaultCodeFormatterOptions.getDefault(), null);
	}

	public DefaultCodeFormatter(Map options) {
		this(null, options);
	}
	
	public DefaultCodeFormatter(DefaultCodeFormatterOptions preferences) {
		this(preferences, null);
	}

	public DefaultCodeFormatter(DefaultCodeFormatterOptions preferences, Map options) {
		if (options == null) {
			options = JavaCore.getOptions();
		}
		this.options = options;
		if (options != null) {
			this.preferences = new DefaultCodeFormatterOptions(options);
			if (preferences != null) {
				this.preferences.set(preferences.getMap());
			}
		} else {
			this.preferences = DefaultCodeFormatterOptions.getDefault();
			if (preferences != null) {
				this.preferences.set(preferences.getMap());
			}
		}
	}

	/**
	 * @see CodeFormatter#format(int, String, int, int[], String, Map)
	 */
	public TextEdit format(
			int kind,
			String source,
			int offset,
			int length,
			int indentationLevel,
			String lineSeparator) {

		if (offset < 0 || length < 0 || length > source.length()) {
			throw new IllegalArgumentException();
		}
		switch(kind) {
			case K_CLASS_BODY_DECLARATIONS :
				return formatClassBodyDeclarations(source, indentationLevel, lineSeparator, offset, length);
			case K_COMPILATION_UNIT :
				return formatCompilationUnit(source, indentationLevel, lineSeparator, offset, length);
			case K_EXPRESSION :
				return formatExpression(source, indentationLevel, lineSeparator, offset, length);
			case K_STATEMENTS :
				return formatStatements(source, indentationLevel, lineSeparator, offset, length);
			case K_UNKNOWN :
				return probeFormatting(source, indentationLevel, lineSeparator, offset, length);
		}
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
		
		TextEdit textEdit = probeFormatting(source, indentationLevel, lineSeparator, 0, source.length());
		if (textEdit == null) {
			return source;
		} else {
			Document document = new Document(source);
			try {
				textEdit.apply(document, TextEdit.UPDATE_REGIONS);
			} catch (MalformedTreeException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			return document.get();
		}
	}

	private TextEdit formatClassBodyDeclarations(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		AstNode[] bodyDeclarations = parseClassBodyDeclarations(source.toCharArray(), this.options);
		
		if (bodyDeclarations == null) {
			// a problem occured while parsing the source
			return null;
		}
		return internalFormatClassBodyDeclarations(source, indentationLevel, lineSeparator, bodyDeclarations, offset, length);
	}

	private TextEdit formatCompilationUnit(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		CompilationUnitDeclaration compilationUnitDeclaration = parseCompilationUnit(source.toCharArray(), this.options);
		
		if (lineSeparator != null) {
			this.preferences.line_delimiter = lineSeparator;
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = createCodeFormatterVisitor(offset, length, source);
		
		return this.newCodeFormatter.format(source, compilationUnitDeclaration);
	}

	private TextEdit formatExpression(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		Expression expression = parseExpression(source.toCharArray(), this.options);
		
		if (expression == null) {
			// a problem occured while parsing the source
			return null;
		}
		return internalFormatExpression(source, indentationLevel, lineSeparator, expression, offset, length);
	}

	private TextEdit formatStatements(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		ConstructorDeclaration constructorDeclaration = parseStatements(source.toCharArray(), this.options);
		
		if (constructorDeclaration.statements == null) {
			// a problem occured while parsing the source
			return null;
		}
		return internalFormatStatements(source, indentationLevel, lineSeparator, constructorDeclaration, offset, length);
	}

	private TextEdit probeFormatting(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		Expression expression = parseExpression(source.toCharArray(), this.options);
		
		if (expression != null) {
			return internalFormatExpression(source, indentationLevel, lineSeparator, expression, offset, length);
		}

		ConstructorDeclaration constructorDeclaration = parseStatements(source.toCharArray(), this.options);
		
		if (constructorDeclaration.statements != null) {
			return internalFormatStatements(source, indentationLevel, lineSeparator, constructorDeclaration, offset, length);
		}
		
		AstNode[] bodyDeclarations = parseClassBodyDeclarations(source.toCharArray(), this.options);
		
		if (bodyDeclarations != null) {
			return internalFormatClassBodyDeclarations(source, indentationLevel, lineSeparator, bodyDeclarations, offset, length);
		}

		return formatCompilationUnit(source, indentationLevel, lineSeparator, offset, length);
	}

	public String getDebugOutput() {
		return this.newCodeFormatter.scribe.toString();
	}

	private TextEdit internalFormatClassBodyDeclarations(String source, int indentationLevel, String lineSeparator, AstNode[] bodyDeclarations, int offset, int length) {
		if (lineSeparator != null) {
			this.preferences.line_delimiter = lineSeparator;
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = createCodeFormatterVisitor(offset, length, source);
		
		return this.newCodeFormatter.format(source, bodyDeclarations);
	}

	private TextEdit internalFormatExpression(String source, int indentationLevel, String lineSeparator, Expression expression, int offset, int length) {
		if (lineSeparator != null) {
			this.preferences.line_delimiter = lineSeparator;
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = createCodeFormatterVisitor(offset, length, source);
		
		return this.newCodeFormatter.format(source, expression);
	}
	
	private CodeFormatterVisitor createCodeFormatterVisitor(int offset, int length, String source) {
		if (source.length() == length) {
			return new CodeFormatterVisitor(this.preferences, options, -1, length);
		} else {
			return new CodeFormatterVisitor(this.preferences, options, offset, length);
		}
	}

	private TextEdit internalFormatStatements(String source, int indentationLevel, String lineSeparator, ConstructorDeclaration constructorDeclaration, int offset, int length) {
		if (lineSeparator != null) {
			this.preferences.line_delimiter = lineSeparator;
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = createCodeFormatterVisitor(offset, length, source);
		
		return  this.newCodeFormatter.format(source, constructorDeclaration);
	}
}
