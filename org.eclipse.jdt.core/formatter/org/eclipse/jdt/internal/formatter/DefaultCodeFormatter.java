/*******************************************************************************
 * Copyright (c) 2000, 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.Map;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.text.edits.TextEdit;

public class DefaultCodeFormatter extends CodeFormatter {

	public static final boolean DEBUG = false;
	
	private CodeFormatterVisitor newCodeFormatter;
	private Map options;
	
	private DefaultCodeFormatterOptions preferences;
	
	public DefaultCodeFormatter() {
		this(new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings()), null);
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
			this.preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
			if (preferences != null) {
				this.preferences.set(preferences.getMap());
			}
		}
	}

	public DefaultCodeFormatter(Map options) {
		this(null, options);
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
	
	private TextEdit formatClassBodyDeclarations(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		ASTNode[] bodyDeclarations = CodeSnippetParsingUtil.parseClassBodyDeclarations(source.toCharArray(), this.options);
		
		if (bodyDeclarations == null) {
			// a problem occured while parsing the source
			return null;
		}
		return internalFormatClassBodyDeclarations(source, indentationLevel, lineSeparator, bodyDeclarations, offset, length);
	}

	private TextEdit formatCompilationUnit(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		CompilationUnitDeclaration compilationUnitDeclaration = CodeSnippetParsingUtil.parseCompilationUnit(source.toCharArray(), this.options);
		
		if (lineSeparator != null) {
			this.preferences.line_separator = lineSeparator;
		} else {
			this.preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, options, offset, length);
		
		return this.newCodeFormatter.format(source, compilationUnitDeclaration);
	}

	private TextEdit formatExpression(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		Expression expression = CodeSnippetParsingUtil.parseExpression(source.toCharArray(), this.options);
		
		if (expression == null) {
			// a problem occured while parsing the source
			return null;
		}
		return internalFormatExpression(source, indentationLevel, lineSeparator, expression, offset, length);
	}

	private TextEdit formatStatements(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		ConstructorDeclaration constructorDeclaration = CodeSnippetParsingUtil.parseStatements(source.toCharArray(), this.options);
		
		if (constructorDeclaration.statements == null) {
			// a problem occured while parsing the source
			return null;
		}
		return internalFormatStatements(source, indentationLevel, lineSeparator, constructorDeclaration, offset, length);
	}

	public String getDebugOutput() {
		return this.newCodeFormatter.scribe.toString();
	}

	private TextEdit internalFormatClassBodyDeclarations(String source, int indentationLevel, String lineSeparator, ASTNode[] bodyDeclarations, int offset, int length) {
		if (lineSeparator != null) {
			this.preferences.line_separator = lineSeparator;
		} else {
			this.preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, options, offset, length);
		
		return this.newCodeFormatter.format(source, bodyDeclarations);
	}

	private TextEdit internalFormatExpression(String source, int indentationLevel, String lineSeparator, Expression expression, int offset, int length) {
		if (lineSeparator != null) {
			this.preferences.line_separator = lineSeparator;
		} else {
			this.preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, options, offset, length);
		
		return this.newCodeFormatter.format(source, expression);
	}
	
	private TextEdit internalFormatStatements(String source, int indentationLevel, String lineSeparator, ConstructorDeclaration constructorDeclaration, int offset, int length) {
		if (lineSeparator != null) {
			this.preferences.line_separator = lineSeparator;
		} else {
			this.preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}
		this.preferences.initial_indentation_level = indentationLevel;

		this.newCodeFormatter = new CodeFormatterVisitor(this.preferences, options, offset, length);
		
		return  this.newCodeFormatter.format(source, constructorDeclaration);
	}

	private TextEdit probeFormatting(String source, int indentationLevel, String lineSeparator, int offset, int length) {
		Expression expression = CodeSnippetParsingUtil.parseExpression(source.toCharArray(), this.options);
		
		if (expression != null) {
			return internalFormatExpression(source, indentationLevel, lineSeparator, expression, offset, length);
		}

		ConstructorDeclaration constructorDeclaration = CodeSnippetParsingUtil.parseStatements(source.toCharArray(), this.options);
		
		if (constructorDeclaration.statements != null) {
			return internalFormatStatements(source, indentationLevel, lineSeparator, constructorDeclaration, offset, length);
		}
		
		ASTNode[] bodyDeclarations = CodeSnippetParsingUtil.parseClassBodyDeclarations(source.toCharArray(), this.options);
		
		if (bodyDeclarations != null) {
			return internalFormatClassBodyDeclarations(source, indentationLevel, lineSeparator, bodyDeclarations, offset, length);
		}

		return formatCompilationUnit(source, indentationLevel, lineSeparator, offset, length);
	}
}
