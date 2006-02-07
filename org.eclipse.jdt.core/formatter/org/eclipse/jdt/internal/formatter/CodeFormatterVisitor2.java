/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.formatter.align.Alignment;
import org.eclipse.jdt.internal.formatter.align.Alignment2;
import org.eclipse.jdt.internal.formatter.align.AlignmentException;
import org.eclipse.text.edits.TextEdit;

/**
 * This class is responsible for formatting a valid java source code.
 * @since 3.2
 */
public class CodeFormatterVisitor2 extends ASTVisitor {
	public static boolean DEBUG = false;
	
	private static final int[] CLOSING_GENERICS_EXPECTEDTOKENS = new int[] {
		TerminalTokens.TokenNameRIGHT_SHIFT,
		TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT,
		TerminalTokens.TokenNameGREATER
	};
	private static final int[] NUMBER_LITERALS_EXPECTEDTOKENS = new int[] {
		TerminalTokens.TokenNameIntegerLiteral,
		TerminalTokens.TokenNameLongLiteral,
		TerminalTokens.TokenNameFloatingPointLiteral,
		TerminalTokens.TokenNameDoubleLiteral
	};
	/*
	 * Set of expected tokens type for a single type reference.
	 * This array needs to be SORTED.
	 */
	private static final int[] PRIMITIVE_TYPE_EXPECTEDTOKENS = new int[] {
		TerminalTokens.TokenNameboolean,
		TerminalTokens.TokenNamebyte,
		TerminalTokens.TokenNamechar,
		TerminalTokens.TokenNamedouble,
		TerminalTokens.TokenNamefloat,
		TerminalTokens.TokenNameint,
		TerminalTokens.TokenNamelong,
		TerminalTokens.TokenNameshort,
		TerminalTokens.TokenNamevoid
	};
	private Scanner localScanner;

	public DefaultCodeFormatterOptions preferences;
	
	public Scribe2 scribe;

	public CodeFormatterVisitor2(DefaultCodeFormatterOptions preferences, Map settings, int offset, int length, CompilationUnit unit) {
		if (settings != null) {
			Object assertModeSetting = settings.get(JavaCore.COMPILER_SOURCE);
			long sourceLevel = ClassFileConstants.JDK1_3;
			if (JavaCore.VERSION_1_4.equals(assertModeSetting)) {
				sourceLevel = ClassFileConstants.JDK1_4;
			} else if (JavaCore.VERSION_1_5.equals(assertModeSetting)) {
				sourceLevel = ClassFileConstants.JDK1_5;
			}		
			this.localScanner = new Scanner(true, false, false/*nls*/, sourceLevel/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		} else {
			this.localScanner = new Scanner(true, false, false/*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		}
		
		this.preferences = preferences;
		this.scribe = new Scribe2(this, settings, offset, length, unit);
	}

	private boolean commentStartsBlock(int start, int end) {
		this.localScanner.resetTo(start, end);
		try {
			if (this.localScanner.getNextToken() ==  TerminalTokens.TokenNameLBRACE) {
				switch(this.localScanner.getNextToken()) {
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
					case TerminalTokens.TokenNameCOMMENT_LINE :
						return true;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return false;
	}
	

	public static int extractInfixExpressionOperator(InfixExpression node) {
		final InfixExpression.Operator infixOperator = node.getOperator();
		if (infixOperator == InfixExpression.Operator.AND) {
			return TerminalTokens.TokenNameAND;
		} else if (infixOperator == InfixExpression.Operator.CONDITIONAL_AND) {
			return TerminalTokens.TokenNameAND_AND;
		} else if (infixOperator == InfixExpression.Operator.CONDITIONAL_OR) {
			return TerminalTokens.TokenNameOR_OR;
		} else if (infixOperator == InfixExpression.Operator.DIVIDE) {
			return TerminalTokens.TokenNameDIVIDE;
		} else if (infixOperator == InfixExpression.Operator.EQUALS) {
			return TerminalTokens.TokenNameEQUAL_EQUAL;
		} else if (infixOperator == InfixExpression.Operator.GREATER) {
			return TerminalTokens.TokenNameGREATER;
		} else if (infixOperator == InfixExpression.Operator.GREATER_EQUALS) {
			return TerminalTokens.TokenNameGREATER_EQUAL;
		} else if (infixOperator == InfixExpression.Operator.LEFT_SHIFT) {
			return TerminalTokens.TokenNameLEFT_SHIFT;
		} else if (infixOperator == InfixExpression.Operator.LESS) {
			return TerminalTokens.TokenNameLESS;
		} else if (infixOperator == InfixExpression.Operator.LESS_EQUALS) {
			return TerminalTokens.TokenNameLESS_EQUAL;
		} else if (infixOperator == InfixExpression.Operator.MINUS) {
			return TerminalTokens.TokenNameMINUS;
		} else if (infixOperator == InfixExpression.Operator.NOT_EQUALS) {
			return TerminalTokens.TokenNameNOT_EQUAL;
		} else if (infixOperator == InfixExpression.Operator.OR) {
			return TerminalTokens.TokenNameOR;
		} else if (infixOperator == InfixExpression.Operator.PLUS) {
			return TerminalTokens.TokenNamePLUS;
		} else if (infixOperator == InfixExpression.Operator.REMAINDER) {
			return TerminalTokens.TokenNameREMAINDER;
		} else if (infixOperator == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
			return TerminalTokens.TokenNameRIGHT_SHIFT;
		} else if (infixOperator == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
			return TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT;
		} else if (infixOperator == InfixExpression.Operator.TIMES) {
			return TerminalTokens.TokenNameMULTIPLY;
		} else {
			return TerminalTokens.TokenNameXOR;
		}
	}

	private final TextEdit failedToFormat() {
		if (DEBUG) {
			System.out.println("COULD NOT FORMAT \n" + this.scribe.scanner); //$NON-NLS-1$
			System.out.println(this.scribe);
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.core.formatter.CodeFormatter#format(int, String, int, int, int, String)
	 */
	public TextEdit format(String string, AbstractTypeDeclaration typeDeclaration) {
		// reset the scribe
		this.scribe.reset();
		
		long startTime = System.currentTimeMillis();
	
		final char[] compilationUnitSource = string.toCharArray();
		
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.initializeScanner(compilationUnitSource);
	
		try {
			this.scribe.lastNumberOfNewLines = 1;
			formatTypeMembers(typeDeclaration.bodyDeclarations(), false);
		} catch(AbortFormatting e){
			return failedToFormat();
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.getRootEdit();
	}

	/**
	 * @see org.eclipse.jdt.core.formatter.CodeFormatter#format(int, String, int, int, int, String)
	 */
	public TextEdit format(String string, Block block) {
		// reset the scribe
		this.scribe.reset();
		
		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.initializeScanner(compilationUnitSource);

		if ((block.getFlags() & ASTNode.MALFORMED) != 0) {
			return failedToFormat();
		}

		try {
			formatStatements(block.statements(), false);
			if (hasComments()) {
				this.scribe.printNewLine();
			}
			this.scribe.printComment();
		} catch(AbortFormatting e){
			return failedToFormat();
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.getRootEdit();
	}

	/**
	 * @see org.eclipse.jdt.core.formatter.CodeFormatter#format(int, String, int, int, int, String)
	 */
	public TextEdit format(String string, CompilationUnit compilationUnit) {
		// reset the scribe
		this.scribe.reset();
		
		if ((compilationUnit.getFlags() & ASTNode.MALFORMED) != 0) {
			return failedToFormat();
		}

		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.initializeScanner(compilationUnitSource);

		try {
			compilationUnit.accept(this);
		} catch(AbortFormatting e){
			return failedToFormat();
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.getRootEdit();
	}

	/**
	 * @see org.eclipse.jdt.core.formatter.CodeFormatter#format(int, String, int, int, int, String)
	 */
	public TextEdit format(String string, Expression expression) {
		// reset the scribe
		this.scribe.reset();
		
		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.initializeScanner(compilationUnitSource);

		if ((expression.getFlags() & ASTNode.MALFORMED) != 0) {
			return failedToFormat();
		}
		try {
			expression.accept(this);
			this.scribe.printComment();
		} catch(AbortFormatting e){
			return failedToFormat();
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.getRootEdit();
	}

    private void format(
			AbstractTypeDeclaration memberTypeDeclaration,
			boolean isChunkStart,
			boolean isFirstClassBodyDeclaration) {

			if (isFirstClassBodyDeclaration) {
				int newLinesBeforeFirstClassBodyDeclaration = this.preferences.blank_lines_before_first_class_body_declaration;
				if (newLinesBeforeFirstClassBodyDeclaration > 0) {
					this.scribe.printEmptyLines(newLinesBeforeFirstClassBodyDeclaration);
				}
			} else {
				int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
				if (newLineBeforeChunk > 0) {
					this.scribe.printEmptyLines(newLineBeforeChunk);
				}
				final int newLinesBeforeMember = this.preferences.blank_lines_before_member_type;
				if (newLinesBeforeMember > 0) {
					this.scribe.printEmptyLines(newLinesBeforeMember);
				}
			}
			memberTypeDeclaration.accept(this);
		}

	private void format(FieldDeclaration fieldDeclaration, boolean isChunkStart, boolean isFirstClassBodyDeclaration) {
		if (isFirstClassBodyDeclaration) {
			int newLinesBeforeFirstClassBodyDeclaration = this.preferences.blank_lines_before_first_class_body_declaration;
			if (newLinesBeforeFirstClassBodyDeclaration > 0) {
				this.scribe.printEmptyLines(newLinesBeforeFirstClassBodyDeclaration);
			}
		} else {
			int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
			if (newLineBeforeChunk > 0) {
				this.scribe.printEmptyLines(newLineBeforeChunk);
			}
			final int newLinesBeforeField = this.preferences.blank_lines_before_field;
			if (newLinesBeforeField > 0) {
				this.scribe.printEmptyLines(newLinesBeforeField);
			}
		}
		Alignment2 memberAlignment = this.scribe.getMemberAlignment();
	
        this.scribe.printComment();
		final List modifiers = fieldDeclaration.modifiers();
		if (modifiers.size() != 0) {
			this.scribe.printModifiers(modifiers, this);
			this.scribe.space();
		}

		fieldDeclaration.getType().accept(this);
		
		List fragments = fieldDeclaration.fragments();
		final int fragmentsLength = fragments.size();
		if (fragmentsLength > 1) {
			// multiple field declaration
			Alignment2 multiFieldDeclarationsAlignment =this.scribe.createAlignment(
					"multiple_field",//$NON-NLS-1$
					this.preferences.alignment_for_multiple_fields,
					fragmentsLength - 1,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(multiFieldDeclarationsAlignment);
		
			boolean ok = false;
			do {
				try {
					for (int i = 0; i < fragmentsLength; i++) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(i);
						/*
						 * Field name
						 */
						if (i == 0) {
							this.scribe.alignFragment(memberAlignment, 0);
							this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
						} else {
							this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, false);
						}
				
						/*
						 * Check for extra dimensions
						 */
						final int extraDimensions = fragment.getExtraDimensions();
						if (extraDimensions != 0) {
							 for (int index = 0; index < extraDimensions; index++) {
							 	this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
							 	this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
							 }
						}
					
						/*
						 * Field initialization
						 */
						final Expression initialization = fragment.getInitializer();
						if (initialization != null) {
							if (i == 0) {
								this.scribe.alignFragment(memberAlignment, 1);
							}
							this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
							if (this.preferences.insert_space_after_assignment_operator) {
								this.scribe.space();
							}
							Alignment2 assignmentAlignment = this.scribe.createAlignment("fieldDeclarationAssignmentAlignment", this.preferences.alignment_for_assignment, 1, this.scribe.scanner.currentPosition); //$NON-NLS-1$
							this.scribe.enterAlignment(assignmentAlignment);
							boolean ok2 = false;
							do {
								try {
									this.scribe.alignFragment(assignmentAlignment, 0);
									initialization.accept(this);
									ok2 = true;
								} catch(AlignmentException e){
									this.scribe.redoAlignment(e);
								}
							} while (!ok2);		
							this.scribe.exitAlignment(assignmentAlignment, true);			
						}
						
						if (i != fragmentsLength - 1) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_multiple_field_declarations);
							this.scribe.printTrailingComment();
							this.scribe.alignFragment(multiFieldDeclarationsAlignment, i);

							if (this.preferences.insert_space_after_comma_in_multiple_field_declarations) {
								this.scribe.space();
							}
						} else {
							this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
							this.scribe.alignFragment(memberAlignment, 2);
							this.scribe.printTrailingComment();
						}
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(multiFieldDeclarationsAlignment, true);				
		} else {
			// single field declaration
			this.scribe.alignFragment(memberAlignment, 0);
			this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			final int extraDimensions = fragment.getExtraDimensions();
			if (extraDimensions != 0) {
				for (int i = 0; i < extraDimensions; i++) {
					this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
					this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
				}
			}
			final Expression initialization = fragment.getInitializer();
			if (initialization != null) {
				this.scribe.alignFragment(memberAlignment, 1);
				this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
				if (this.preferences.insert_space_after_assignment_operator) {
					this.scribe.space();
				}
				Alignment2 assignmentAlignment = this.scribe.createAlignment("fieldDeclarationAssignmentAlignment", this.preferences.alignment_for_assignment, 1, this.scribe.scanner.currentPosition); //$NON-NLS-1$
				this.scribe.enterAlignment(assignmentAlignment);
				boolean ok = false;
				do {
					try {
						this.scribe.alignFragment(assignmentAlignment, 0);
						initialization.accept(this);
						ok = true;
					} catch(AlignmentException e){
						this.scribe.redoAlignment(e);
					}
				} while (!ok);		
				this.scribe.exitAlignment(assignmentAlignment, true);			
			}
			
			this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);

			if (memberAlignment != null) {
				this.scribe.alignFragment(memberAlignment, 2);
				this.scribe.printTrailingComment();
			} else {
				this.scribe.space();
				this.scribe.printTrailingComment();
			}
		}
	}
	private void format(
			BodyDeclaration bodyDeclaration,
			boolean isChunkStart,
			boolean isFirstClassBodyDeclaration) {

		if (isFirstClassBodyDeclaration) {
			int newLinesBeforeFirstClassBodyDeclaration = this.preferences.blank_lines_before_first_class_body_declaration;
			if (newLinesBeforeFirstClassBodyDeclaration > 0) {
				this.scribe.printEmptyLines(newLinesBeforeFirstClassBodyDeclaration);
			}
		} else {
			final int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
			if (newLineBeforeChunk > 0) {
				this.scribe.printEmptyLines(newLineBeforeChunk);
			}
		}
		final int newLinesBeforeMethod = this.preferences.blank_lines_before_method;
		if (newLinesBeforeMethod > 0 && !isFirstClassBodyDeclaration) {
			this.scribe.printEmptyLines(newLinesBeforeMethod);
		} else if (this.scribe.line != 0 || this.scribe.column != 1) {
			this.scribe.printNewLine();
		}
		bodyDeclaration.accept(this);
	}

	private void formatAction(final int line, final Statement action, boolean insertLineForSingleStatement) {
		if (action != null) {
			switch(action.getNodeType()) {
				case ASTNode.BLOCK :
	                formatLeftCurlyBrace(line, this.preferences.brace_position_for_block);
					action.accept(this);
					break;
				case ASTNode.EMPTY_STATEMENT :
					this.scribe.indent();
					action.accept(this);
					this.scribe.unIndent();
					break;
				default :
					this.scribe.printNewLine();
					this.scribe.indent();
					action.accept(this);
					this.scribe.unIndent();
					if (insertLineForSingleStatement) {
						this.scribe.printNewLine();
					}
			}
		} else {
			// empty statement
			this.scribe.indent();
			action.accept(this);
			this.scribe.unIndent();
		}
	}

	private void formatBlock(Block block, String block_brace_position, boolean insertSpaceBeforeOpeningBrace) {
		formatOpeningBrace(block_brace_position, insertSpaceBeforeOpeningBrace);
		final List statements = block.statements();
		final int statementsLength = statements.size();
		if (statementsLength != 0) {
			this.scribe.printNewLine();
			if (this.preferences.indent_statements_compare_to_block) {
				this.scribe.indent();
			}
			formatStatements(statements, true);
			this.scribe.printComment();
	
			if (this.preferences.indent_statements_compare_to_block) {
				this.scribe.unIndent();
			}
		} else {
			if (this.preferences.insert_new_line_in_empty_block) {
				this.scribe.printNewLine();
			}
			if (this.preferences.indent_statements_compare_to_block) {
				this.scribe.indent();
			}
			this.scribe.printComment();
	
			if (this.preferences.indent_statements_compare_to_block) {
				this.scribe.unIndent();
			}
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(block_brace_position)) {
			this.scribe.unIndent();
		}
	}

	private void formatEmptyTypeDeclaration(boolean isFirst) {
		boolean hasSemiColon = isNextToken(TerminalTokens.TokenNameSEMICOLON);
		while(isNextToken(TerminalTokens.TokenNameSEMICOLON)) {
			this.scribe.printComment();
			this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
		}
		if (hasSemiColon && isFirst) {
			this.scribe.printNewLine();
		}
	}


	private void formatLeftCurlyBrace(final int line, final String bracePosition) {
        /*
         * deal with (quite unexpected) comments right before lcurly
         */
        this.scribe.printComment();
        if (DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP.equals(bracePosition)
                && (this.scribe.line > line || this.scribe.column >= this.preferences.page_width)) 
        {
            this.scribe.printNewLine();
        }
    }

	private void formatLocalDeclaration(VariableDeclarationExpression declarationExpression, boolean insertSpaceBeforeComma, boolean insertSpaceAfterComma) {
		final List modifiers = declarationExpression.modifiers();
		if (modifiers.size() != 0) {
			this.scribe.printModifiers(modifiers, this);
			this.scribe.space();
		}

		declarationExpression.getType().accept(this);
		
		formatVariableDeclarationFragments(declarationExpression.fragments(), insertSpaceBeforeComma, insertSpaceAfterComma);
	}

	private void formatVariableDeclarationFragments(final List fragments, boolean insertSpaceBeforeComma, boolean insertSpaceAfterComma) {
		final int fragmentsLength = fragments.size();
		if (fragmentsLength > 1) {
			// multiple field declaration
			Alignment2 multiFieldDeclarationsAlignment =this.scribe.createAlignment(
					"multiple_field",//$NON-NLS-1$
					this.preferences.alignment_for_multiple_fields,
					fragmentsLength - 1,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(multiFieldDeclarationsAlignment);
		
			boolean ok = false;
			do {
				try {
					for (int i = 0; i < fragmentsLength; i++) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(i);
						/*
						 * Field name
						 */
						if (i == 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
						} else {
							this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, false);
						}
				
						/*
						 * Check for extra dimensions
						 */
						final int extraDimensions = fragment.getExtraDimensions();
						if (extraDimensions != 0) {
							 for (int index = 0; index < extraDimensions; index++) {
							 	this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
							 	this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
							 }
						}
					
						/*
						 * Field initialization
						 */
						final Expression initialization = fragment.getInitializer();
						if (initialization != null) {
							this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
							if (this.preferences.insert_space_after_assignment_operator) {
								this.scribe.space();
							}
							Alignment2 assignmentAlignment = this.scribe.createAlignment("fieldDeclarationAssignmentAlignment", this.preferences.alignment_for_assignment, 1, this.scribe.scanner.currentPosition); //$NON-NLS-1$
							this.scribe.enterAlignment(assignmentAlignment);
							boolean ok2 = false;
							do {
								try {
									this.scribe.alignFragment(assignmentAlignment, 0);
									initialization.accept(this);
									ok2 = true;
								} catch(AlignmentException e){
									this.scribe.redoAlignment(e);
								}
							} while (!ok2);		
							this.scribe.exitAlignment(assignmentAlignment, true);			
						}
						
						if (i != fragmentsLength - 1) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, insertSpaceBeforeComma);
							this.scribe.printTrailingComment();
							this.scribe.alignFragment(multiFieldDeclarationsAlignment, i);

							if (insertSpaceAfterComma) {
								this.scribe.space();
							}
						}
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(multiFieldDeclarationsAlignment, true);				
		} else {
			// single field declaration
			this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			final int extraDimensions = fragment.getExtraDimensions();
			if (extraDimensions != 0) {
				for (int i = 0; i < extraDimensions; i++) {
					this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
					this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
				}
			}
			final Expression initialization = fragment.getInitializer();
			if (initialization != null) {
				this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
				if (this.preferences.insert_space_after_assignment_operator) {
					this.scribe.space();
				}
				Alignment2 assignmentAlignment = this.scribe.createAlignment("localDeclarationAssignmentAlignment", this.preferences.alignment_for_assignment, 1, this.scribe.scanner.currentPosition); //$NON-NLS-1$
				this.scribe.enterAlignment(assignmentAlignment);
				boolean ok = false;
				do {
					try {
						this.scribe.alignFragment(assignmentAlignment, 0);
						initialization.accept(this);
						ok = true;
					} catch(AlignmentException e){
						this.scribe.redoAlignment(e);
					}
				} while (!ok);		
				this.scribe.exitAlignment(assignmentAlignment, true);			
			}
		}
	}

	private void formatOpeningBrace(String bracePosition, boolean insertSpaceBeforeBrace) {
		if (DefaultCodeFormatterConstants.NEXT_LINE.equals(bracePosition)) {
			this.scribe.printNewLine();
		} else if (DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED.equals(bracePosition)) {
			this.scribe.printNewLine();
			this.scribe.indent();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameLBRACE, insertSpaceBeforeBrace);

		this.scribe.printTrailingComment();
	}
	
	private void formatStatements(final List statements, boolean insertNewLineAfterLastStatement) {
		final int statementsLength = statements.size();
		if (statementsLength > 1) {
			Statement previousStatement = (Statement) statements.get(0);
			previousStatement.accept(this);
			final int previousStatementNodeType = previousStatement.getNodeType();
			for (int i = 1; i < statementsLength - 1; i++) {
				final Statement statement = (Statement) statements.get(i);
				final int statementNodeType = statement.getNodeType();
				if ((previousStatementNodeType == ASTNode.EMPTY_STATEMENT
						&& statementNodeType != ASTNode.EMPTY_STATEMENT)
					|| (previousStatementNodeType != ASTNode.EMPTY_STATEMENT
						&& statementNodeType != ASTNode.EMPTY_STATEMENT)) {
					this.scribe.printNewLine();
				}
				statement.accept(this);
				previousStatement = statement;
			}
			final Statement statement = ((Statement) statements.get(statementsLength - 1));
			final int statementNodeType = statement.getNodeType();
			if ((previousStatementNodeType == ASTNode.EMPTY_STATEMENT
					&& statementNodeType != ASTNode.EMPTY_STATEMENT)
				|| (previousStatementNodeType != ASTNode.EMPTY_STATEMENT
					&& statementNodeType != ASTNode.EMPTY_STATEMENT)) {
				this.scribe.printNewLine();
			}
			statement.accept(this);
		} else {
			((Statement) statements.get(0)).accept(this);
		}
		if (insertNewLineAfterLastStatement) {
			this.scribe.printNewLine();
		}
	}

	private void formatTypeMembers(List bodyDeclarations, boolean insertLineAfterLastMember) {
		Alignment2 memberAlignment = this.scribe.createMemberAlignment("typeMembers", this.preferences.align_type_members_on_columns ? Alignment.M_MULTICOLUMN : Alignment.M_NO_ALIGNMENT, 3, this.scribe.scanner.currentPosition); //$NON-NLS-1$
		this.scribe.enterMemberAlignment(memberAlignment);
		boolean isChunkStart = false;
		boolean ok = false;
		int startIndex = 0;
		do {
			try {
				for (int i = startIndex, max = bodyDeclarations.size(); i < max; i++) {
					BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(i);
					switch(bodyDeclaration.getNodeType()) {
						case ASTNode.FIELD_DECLARATION :
							isChunkStart = memberAlignment.checkChunkStart(Alignment.CHUNK_FIELD, i, this.scribe.scanner.currentPosition);
							FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
							format(fieldDeclaration, isChunkStart, i == 0);
							break;
						case ASTNode.INITIALIZER :
							isChunkStart = memberAlignment.checkChunkStart(Alignment.CHUNK_FIELD, i, this.scribe.scanner.currentPosition);
							int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
							if (newLineBeforeChunk > 0 && i != 0) {
								this.scribe.printEmptyLines(newLineBeforeChunk);
							} else if (i == 0) {
								int newLinesBeforeFirstClassBodyDeclaration = this.preferences.blank_lines_before_first_class_body_declaration;
								if (newLinesBeforeFirstClassBodyDeclaration > 0) {
									this.scribe.printEmptyLines(newLinesBeforeFirstClassBodyDeclaration);
								}
							}
							bodyDeclaration.accept(this);			
							break;
						case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
						case ASTNode.METHOD_DECLARATION :
							isChunkStart = memberAlignment.checkChunkStart(Alignment.CHUNK_METHOD, i, this.scribe.scanner.currentPosition);
							format(bodyDeclaration, isChunkStart, i == 0);
							break;
						case ASTNode.TYPE_DECLARATION :
						case ASTNode.ENUM_DECLARATION :
						case ASTNode.ANNOTATION_TYPE_DECLARATION :
							isChunkStart = memberAlignment.checkChunkStart(Alignment.CHUNK_TYPE, i, this.scribe.scanner.currentPosition);
							format((AbstractTypeDeclaration)bodyDeclaration, isChunkStart, i == 0);
					}
					if (isNextToken(TerminalTokens.TokenNameSEMICOLON)) {
						this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
						this.scribe.printTrailingComment();
					}
					if (i < max - 1 || insertLineAfterLastMember) {
						this.scribe.printNewLine();
					}					
					// realign to the proper value
					if (this.scribe.memberAlignment != null) {
						// select the last alignment
						this.scribe.indentationLevel = this.scribe.memberAlignment.originalIndentationLevel;
					}
				}
				ok = true;
			} catch(AlignmentException e){
				startIndex = memberAlignment.chunkStartIndex;
				this.scribe.redoMemberAlignment(e);
			}
		} while (!ok);
		this.scribe.printComment();
		this.scribe.exitMemberAlignment(memberAlignment);
	}

	private void formatTypeOpeningBrace(String bracePosition, boolean insertSpaceBeforeBrace, boolean insertNewLine, ASTNode node) {
		formatOpeningBrace(bracePosition, insertSpaceBeforeBrace);
		if (!insertNewLine) {
			switch(node.getNodeType()) {
				case ASTNode.ENUM_DECLARATION :
					insertNewLine = this.preferences.insert_new_line_in_empty_enum_declaration;
					break;
				case ASTNode.ENUM_CONSTANT_DECLARATION :
					insertNewLine = this.preferences.insert_new_line_in_empty_enum_constant;
					break;
				case ASTNode.ANONYMOUS_CLASS_DECLARATION :
					insertNewLine = this.preferences.insert_new_line_in_empty_anonymous_type_declaration;
					break;
				default:
					insertNewLine = this.preferences.insert_new_line_in_empty_type_declaration;
			}
		}
		if (insertNewLine) {
			this.scribe.printNewLine();
		}
	}


	private boolean hasComments() {

		this.localScanner.resetTo(this.scribe.scanner.startPosition, this.scribe.scannerEndPosition - 1);
		try {
			switch(this.localScanner.getNextToken()) {
				case TerminalTokens.TokenNameCOMMENT_BLOCK :
				case TerminalTokens.TokenNameCOMMENT_JAVADOC :
				case TerminalTokens.TokenNameCOMMENT_LINE :
					return true;
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return false;
	}
	
	private boolean isClosingGenericToken() {
		this.localScanner.resetTo(this.scribe.scanner.currentPosition, this.scribe.scannerEndPosition - 1);
		try {
			int token = this.localScanner.getNextToken();
			loop: while(true) {
				switch(token) {
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
					case TerminalTokens.TokenNameCOMMENT_LINE :
						token = this.localScanner.getNextToken();
						continue loop;
					default:
						break loop;
				}
			}
			switch(token) {
				case TerminalTokens.TokenNameGREATER :
				case TerminalTokens.TokenNameRIGHT_SHIFT :
				case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT :
					return true;
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return false;
	}

	private boolean isGuardClause(Block block, List statements) {
		if (commentStartsBlock(block.getStartPosition(), block.getStartPosition() + block.getLength() - 1)) return false;
		final int statementsLength = statements.size();
		if (statementsLength != 1) return false;
		switch(((Statement) statements.get(0)).getNodeType()) {
			case ASTNode.RETURN_STATEMENT :
			case ASTNode.THROW_STATEMENT :
				return true;
		}
		return false;
	}

	private boolean isNextToken(int tokenName) {
		this.localScanner.resetTo(this.scribe.scanner.currentPosition, this.scribe.scannerEndPosition - 1);
		try {
			int token = this.localScanner.getNextToken();
			loop: while(true) {
				switch(token) {
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
					case TerminalTokens.TokenNameCOMMENT_LINE :
						token = this.localScanner.getNextToken();
						continue loop;
					default:
						break loop;
				}
			}
			return  token == tokenName;
		} catch(InvalidInputException e) {
			// ignore
		}
		return false;
	}

	public boolean visit(AnnotationTypeDeclaration node) {
        this.scribe.printComment();
        final int line = this.scribe.line; 
        
        final List modifiers = node.modifiers();
        if (modifiers.size() != 0) {
        	this.scribe.printModifiers(modifiers, this);
        	this.scribe.space();
        }
        this.scribe.printNextToken(TerminalTokens.TokenNameAT, this.preferences.insert_space_before_at_in_annotation_type_declaration);
		this.scribe.printNextToken(TerminalTokens.TokenNameinterface, this.preferences.insert_space_after_at_in_annotation_type_declaration); 
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true); 

		String class_declaration_brace;
		boolean space_before_opening_brace;
		class_declaration_brace = this.preferences.brace_position_for_annotation_type_declaration;
		space_before_opening_brace =  this.preferences.insert_space_before_opening_brace_in_annotation_type_declaration;

		formatLeftCurlyBrace(line, class_declaration_brace);
		final List bodyDeclarations = node.bodyDeclarations();
		formatTypeOpeningBrace(class_declaration_brace, space_before_opening_brace, bodyDeclarations.size() != 0, node);
		
		boolean indent_body_declarations_compare_to_header = this.preferences.indent_body_declarations_compare_to_annotation_declaration_header;
		if (indent_body_declarations_compare_to_header) {
			this.scribe.indent();
		}
		
		formatTypeMembers(bodyDeclarations, true);
		
		if (indent_body_declarations_compare_to_header) {
			this.scribe.unIndent();
		}
		
		if (this.preferences.insert_new_line_in_empty_annotation_declaration) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (class_declaration_brace.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}
		if (hasComments()) {
			this.scribe.printNewLine();
		}
		return false;
	}

	public boolean visit(AnnotationTypeMemberDeclaration node) {        
        /*
         * Print comments to get proper line number
         */
        this.scribe.printComment();
        List modifiers = node.modifiers();
        if (modifiers.size() != 0) {
        	this.scribe.printModifiers(modifiers, this);
    		this.scribe.space();
        }
		/*
		 * Print the method return type
		 */
        node.getType().accept(this);
		/*
		 * Print the method name
		 */
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true); 
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_annotation_type_member_declaration); 
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_annotation_type_member_declaration); 

		Expression defaultValue = node.getDefault();
		if (defaultValue != null) {
			this.scribe.printNextToken(TerminalTokens.TokenNamedefault, true);
			this.scribe.space();
			defaultValue.accept(this);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(AnonymousClassDeclaration node) {
		/*
		 * Type body
		 */
		String anonymous_type_declaration_brace_position = this.preferences.brace_position_for_anonymous_type_declaration;
		
		final List bodyDeclarations = node.bodyDeclarations();
		formatTypeOpeningBrace(anonymous_type_declaration_brace_position, this.preferences.insert_space_before_opening_brace_in_anonymous_type_declaration, bodyDeclarations.size() != 0, node);
		
		this.scribe.indent();

		formatTypeMembers(bodyDeclarations, true);

		this.scribe.unIndent();
		if (this.preferences.insert_new_line_in_empty_anonymous_type_declaration) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
		if (anonymous_type_declaration_brace_position.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}
		return false;
	}

	public boolean visit(ArrayAccess node) {
		node.getArray().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET, this.preferences.insert_space_before_opening_bracket_in_array_reference);
		if (this.preferences.insert_space_after_opening_bracket_in_array_reference) {
			this.scribe.space();
		}
		node.getIndex().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET, this.preferences.insert_space_before_closing_bracket_in_array_reference);
		return false;
	}

	public boolean visit(ArrayCreation node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamenew);
		this.scribe.space();
		final ArrayType type = node.getType();
		final List dimensions = node.dimensions();
		final int dimensionsLength = dimensions.size();

		final int arrayTypeDimensions = type.getDimensions();
		type.getElementType().accept(this);
		if (dimensionsLength != 0) {
			for (int i = 0; i < dimensionsLength; i++) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET, this.preferences.insert_space_before_opening_bracket_in_array_allocation_expression);
				Expression dimension = (Expression) dimensions.get(i);
				if (dimension != null) {
					if (this.preferences.insert_space_after_opening_bracket_in_array_allocation_expression) {
						this.scribe.space();
					}
					dimension.accept(this);
					this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET, this.preferences.insert_space_before_closing_bracket_in_array_allocation_expression);
				} else {
					this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET, this.preferences.insert_space_between_empty_brackets_in_array_allocation_expression);
				}
			}
			for (int i = 0, max = arrayTypeDimensions - dimensionsLength; i < max; i++) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET, this.preferences.insert_space_before_opening_bracket_in_array_allocation_expression);
				this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET, this.preferences.insert_space_between_empty_brackets_in_array_allocation_expression);
			}
		} else {
			for (int i = 0; i < arrayTypeDimensions; i++) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET, this.preferences.insert_space_before_opening_bracket_in_array_allocation_expression);
				this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET, this.preferences.insert_space_between_empty_brackets_in_array_allocation_expression);
			}
		}
		
		final ArrayInitializer initializer = node.getInitializer();
		if (initializer != null) {
			initializer.accept(this);
		}

		return false;
	}

	public boolean visit(ArrayInitializer node) {
		final List expressions = node.expressions();
		final int expressionsLength = expressions.size();
		if (expressionsLength != 0) {
			final String array_initializer_brace_position = this.preferences.brace_position_for_array_initializer;
			formatOpeningBrace(array_initializer_brace_position, this.preferences.insert_space_before_opening_brace_in_array_initializer);
		
			final boolean insert_new_line_after_opening_brace = this.preferences.insert_new_line_after_opening_brace_in_array_initializer;
			if (expressionsLength > 1) {
				if (insert_new_line_after_opening_brace) {
					this.scribe.printNewLine();
				}
				Alignment2 arrayInitializerAlignment = this.scribe.createAlignment(
						"array_initializer",//$NON-NLS-1$
						this.preferences.alignment_for_expressions_in_array_initializer,
						Alignment.R_OUTERMOST,
						expressionsLength,
						this.scribe.scanner.currentPosition,
						this.preferences.continuation_indentation_for_array_initializer,
						true);
				
				if (insert_new_line_after_opening_brace) {
				    arrayInitializerAlignment.fragmentIndentations[0] = arrayInitializerAlignment.breakIndentationLevel;
				}
				
				this.scribe.enterAlignment(arrayInitializerAlignment);
				boolean ok = false;
				do {
					try {
						this.scribe.alignFragment(arrayInitializerAlignment, 0);
						if (this.preferences.insert_space_after_opening_brace_in_array_initializer) {
							this.scribe.space();
						}
						((Expression) expressions.get(0)).accept(this);
						for (int i = 1; i < expressionsLength; i++) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_array_initializer);
							this.scribe.printTrailingComment();
							this.scribe.alignFragment(arrayInitializerAlignment, i);
							if (this.preferences.insert_space_after_comma_in_array_initializer) {
								this.scribe.space();
							}
							((Expression) expressions.get(i)).accept(this);
							if (i == expressionsLength - 1) {
								if (isNextToken(TerminalTokens.TokenNameCOMMA)) {
									this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_array_initializer);
									this.scribe.printTrailingComment();
								}
							}
						}
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(arrayInitializerAlignment, true);
			} else {
				if (insert_new_line_after_opening_brace) {
					this.scribe.printNewLine();
					this.scribe.indent();
				}
				// we don't need to use an alignment
				if (this.preferences.insert_space_after_opening_brace_in_array_initializer) {
					this.scribe.space();
				} else {
					this.scribe.needSpace = false;
				}
				((Expression) expressions.get(0)).accept(this);
				if (isNextToken(TerminalTokens.TokenNameCOMMA)) {
					this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_array_initializer);
					this.scribe.printTrailingComment();
				}
				if (insert_new_line_after_opening_brace) {
					this.scribe.unIndent();
				}
			}
			if (this.preferences.insert_new_line_before_closing_brace_in_array_initializer) {
				this.scribe.printNewLine();
			} else if (this.preferences.insert_space_before_closing_brace_in_array_initializer) {
				this.scribe.space();
			}
			this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE, false); 
			if (array_initializer_brace_position.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
				this.scribe.unIndent();
			}	
		} else {
			boolean keepEmptyArrayInitializerOnTheSameLine = this.preferences.keep_empty_array_initializer_on_one_line;
			String array_initializer_brace_position = this.preferences.brace_position_for_array_initializer;
			if (keepEmptyArrayInitializerOnTheSameLine) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLBRACE, this.preferences.insert_space_before_opening_brace_in_array_initializer);
				this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE, this.preferences.insert_space_between_empty_braces_in_array_initializer); 
			} else {
				formatOpeningBrace(array_initializer_brace_position, this.preferences.insert_space_before_opening_brace_in_array_initializer);
				this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE, false); 
				if (array_initializer_brace_position.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
					this.scribe.unIndent();
				}
			}
		}
		return false;
	}

	public boolean visit(ArrayType node) {
		node.getComponentType().accept(this);
		if (this.preferences.insert_space_before_opening_bracket_in_array_type_reference) {
			this.scribe.space();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
		if (this.preferences.insert_space_between_brackets_in_array_type_reference) {
			this.scribe.space();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
		return false;
	}

	public boolean visit(AssertStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameassert);
		this.scribe.space();
		node.getExpression().accept(this);
		
		Expression message = node.getMessage();
		if (message != null) {
			this.scribe.printNextToken(TerminalTokens.TokenNameCOLON, this.preferences.insert_space_before_colon_in_assert);
			if (this.preferences.insert_space_after_colon_in_assert) {
				this.scribe.space();
			}
			message.accept(this);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);		
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		Assignment.Operator operator = node.getOperator();
		if (operator == Assignment.Operator.ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.MINUS_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameMINUS_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.PLUS_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNamePLUS_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.TIMES_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameMULTIPLY_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.DIVIDE_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameDIVIDE_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.REMAINDER_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameREMAINDER_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.LEFT_SHIFT_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameLEFT_SHIFT_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameRIGHT_SHIFT_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.BIT_AND_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameAND_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else if (operator == Assignment.Operator.BIT_OR_ASSIGN) {
			this.scribe.printNextToken(TerminalTokens.TokenNameOR_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameXOR_EQUAL, this.preferences.insert_space_before_assignment_operator);
		} 
		if (this.preferences.insert_space_after_assignment_operator) {
			this.scribe.space();
		}

		Alignment2 assignmentAlignment = this.scribe.createAlignment("assignmentAlignment", this.preferences.alignment_for_assignment, 1, this.scribe.scanner.currentPosition); //$NON-NLS-1$
		this.scribe.enterAlignment(assignmentAlignment);
		boolean ok = false;
		do {
			try {
				this.scribe.alignFragment(assignmentAlignment, 0);
				node.getRightHandSide().accept(this);
				ok = true;
			} catch(AlignmentException e){
				this.scribe.redoAlignment(e);
			}
		} while (!ok);		
		this.scribe.exitAlignment(assignmentAlignment, true);
		return false;
	}

	public boolean visit(Block node) {
		formatBlock(node, this.preferences.brace_position_for_block, this.preferences.insert_space_before_opening_brace_in_block);	
		return false;
	}

	public boolean visit(BooleanLiteral node) {
		if (node.booleanValue()) {
			this.scribe.printNextToken(TerminalTokens.TokenNametrue);
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNamefalse);
		}
		return false;
	}

	public boolean visit(BreakStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamebreak);
		if (node.getLabel() != null) {
			this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(CastExpression node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN);
		if (this.preferences.insert_space_after_opening_paren_in_cast) {
			this.scribe.space();
		}
		node.getType().accept(this);

		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_cast);
		if (this.preferences.insert_space_after_closing_paren_in_cast) {
			this.scribe.space();
		}
		node.getExpression().accept(this);
		return false;
	}

	public boolean visit(CharacterLiteral node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameCharacterLiteral);
		return false;
	}

	public boolean visit(ClassInstanceCreation node) {
		Expression expression = node.getExpression();
		if (expression != null) {
			expression.accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNamenew);
		final List typeArguments = node.typeArguments();
		final int length = typeArguments.size();
		if (length != 0) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_type_arguments); 
				if (this.preferences.insert_space_after_opening_angle_bracket_in_type_arguments) {
					this.scribe.space();
				}
				for (int i = 0; i < length - 1; i++) {
					((Type) typeArguments.get(i)).accept(this);
					this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_type_arguments);
					if (this.preferences.insert_space_after_comma_in_type_arguments) {
						this.scribe.space();
					}				
				}
				((Type) typeArguments.get(length - 1)).accept(this);
				if (isClosingGenericToken()) {
					this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_type_arguments); 
				}
				if (this.preferences.insert_space_after_closing_angle_bracket_in_type_arguments) {
					this.scribe.space();
				}
		} else {
			this.scribe.space();
		}

		node.getType().accept(this);
		
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_method_invocation);

		final List arguments = node.arguments();
		final int argumentsLength = arguments.size();
		
		if (argumentsLength != 0) {
			if (this.preferences.insert_space_after_opening_paren_in_method_invocation) {
				this.scribe.space();
			}			
			Alignment2 argumentsAlignment =this.scribe.createAlignment(
					"allocation",//$NON-NLS-1$
					this.preferences.alignment_for_arguments_in_allocation_expression,
					argumentsLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					for (int i = 0; i < argumentsLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_allocation_expression);
							this.scribe.printTrailingComment();
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_allocation_expression) {
							this.scribe.space();
						}
						((Expression) arguments.get(i)).accept(this);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_method_invocation); 
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_method_invocation); 
		}
		final AnonymousClassDeclaration classDeclaration = node.getAnonymousClassDeclaration();
		if (classDeclaration != null) {
			classDeclaration.accept(this);
		}
		return false;
	}

	public boolean visit(CompilationUnit node) {
		// fake new line to handle empty lines before package declaration or import declarations
		this.scribe.lastNumberOfNewLines = 1;
		/* 
		 * Package declaration
		 */
		final PackageDeclaration packageDeclaration = node.getPackage();
		final boolean hasPackage = packageDeclaration != null;
		if (hasPackage) {
			if (hasComments()) {
				this.scribe.printComment();
			}
			int blankLinesBeforePackage = this.preferences.blank_lines_before_package;
			if (blankLinesBeforePackage > 0) {
				this.scribe.printEmptyLines(blankLinesBeforePackage);
			}

			final List annotations = packageDeclaration.annotations();
			if (annotations.size() != 0) {
				this.scribe.printModifiers(annotations, this);
				this.scribe.space();
			}
			// dump the package keyword
			this.scribe.printNextToken(TerminalTokens.TokenNamepackage);
			this.scribe.space();
			packageDeclaration.getName().accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
			int blankLinesAfterPackage = this.preferences.blank_lines_after_package;
			if (blankLinesAfterPackage > 0) {
				this.scribe.printEmptyLines(blankLinesAfterPackage);
			} else {
				this.scribe.printNewLine();
			}			
		} else {
			this.scribe.printComment();
		}
		
		/*
		 * Import statements
		 */
		final List imports = node.imports();
		final int importsLength = imports.size();
		if (importsLength != 0) {
			if (hasPackage) {
				int blankLinesBeforeImports = this.preferences.blank_lines_before_imports;
				if (blankLinesBeforeImports > 0) {
					this.scribe.printEmptyLines(blankLinesBeforeImports);
				}
			}
			for (int i = 0; i < importsLength; i++) {
				((ImportDeclaration) imports.get(i)).accept(this);
			}			
			
			int blankLinesAfterImports = this.preferences.blank_lines_after_imports;
			if (blankLinesAfterImports > 0) {
				this.scribe.printEmptyLines(blankLinesAfterImports);
			}
		}

		formatEmptyTypeDeclaration(true);
		
		int blankLineBetweenTypeDeclarations = this.preferences.blank_lines_between_type_declarations;
		/*
		 * Type declarations
		 */
		final List types = node.types();
		final int typesLength = types.size();
		if (typesLength != 0) {
			for (int i = 0; i < typesLength - 1; i++) {
				((AbstractTypeDeclaration) types.get(i)).accept(this);
				formatEmptyTypeDeclaration(false);
				if (blankLineBetweenTypeDeclarations != 0) {
					this.scribe.printEmptyLines(blankLineBetweenTypeDeclarations);
				} else {
					this.scribe.printNewLine();
				}
			}
			((AbstractTypeDeclaration) types.get(typesLength - 1)).accept(this);
		}
		this.scribe.printEndOfCompilationUnit();
		return false;
	}

	public boolean visit(ConditionalExpression node) {
		node.getExpression().accept(this);
    
    	Alignment2 conditionalExpressionAlignment =this.scribe.createAlignment(
    			"conditionalExpression", //$NON-NLS-1$
    			this.preferences.alignment_for_conditional_expression,
    			2,
    			this.scribe.scanner.currentPosition);
    
    	this.scribe.enterAlignment(conditionalExpressionAlignment);
    	boolean ok = false;
    	do {
    		try {
    			this.scribe.alignFragment(conditionalExpressionAlignment, 0);
    			this.scribe.printNextToken(TerminalTokens.TokenNameQUESTION, this.preferences.insert_space_before_question_in_conditional);
    
    			if (this.preferences.insert_space_after_question_in_conditional) {
    				this.scribe.space();
    			}
    			node.getThenExpression().accept(this);
    			this.scribe.printTrailingComment();
    			this.scribe.alignFragment(conditionalExpressionAlignment, 1);
    			this.scribe.printNextToken(TerminalTokens.TokenNameCOLON, this.preferences.insert_space_before_colon_in_conditional);
    
    			if (this.preferences.insert_space_after_colon_in_conditional) {
    				this.scribe.space();
    			}
    			node.getElseExpression().accept(this);
    
    			ok = true;
    		} catch (AlignmentException e) {
    			this.scribe.redoAlignment(e);
    		}
    	} while (!ok);
    	this.scribe.exitAlignment(conditionalExpressionAlignment, true);
    	return false;	
    }

	public boolean visit(ConstructorInvocation node) {
		final List typeArguments = node.typeArguments();
		final int typeArgumentsLength = typeArguments.size();
		if (typeArgumentsLength != 0) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_type_arguments); 
				if (this.preferences.insert_space_after_opening_angle_bracket_in_type_arguments) {
					this.scribe.space();
				}
				for (int i = 0; i < typeArgumentsLength - 1; i++) {
					((Type) typeArguments.get(i)).accept(this);
					this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_type_arguments);
					if (this.preferences.insert_space_after_comma_in_type_arguments) {
						this.scribe.space();
					}				
				}
				((Type) typeArguments.get(typeArgumentsLength - 1)).accept(this);
				if (isClosingGenericToken()) {
					this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_type_arguments); 
				}
				if (this.preferences.insert_space_after_closing_angle_bracket_in_type_arguments) {
					this.scribe.space();
				}
		}
		
		this.scribe.printNextToken(TerminalTokens.TokenNamethis);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_method_invocation);
		
		final List arguments = node.arguments();
		final int argumentsLength = arguments.size();
		if (argumentsLength != 0) {
			if (this.preferences.insert_space_after_opening_paren_in_method_invocation) {
				this.scribe.space();
			}
			Alignment2 argumentsAlignment =this.scribe.createAlignment(
					"explicit_constructor_call",//$NON-NLS-1$
					this.preferences.alignment_for_arguments_in_explicit_constructor_call,
					argumentsLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					for (int i = 0; i < argumentsLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_explicit_constructor_call_arguments);
							this.scribe.printTrailingComment();
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_explicit_constructor_call_arguments) {
							this.scribe.space();
						}
						((Expression) arguments.get(i)).accept(this);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_method_invocation); 
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_method_invocation); 
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(ContinueStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamecontinue);
		if (node.getLabel() != null) {
			this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(DoStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamedo);
		final int line = this.scribe.line;
		
		final Statement action = node.getBody();
		if (action != null) {
			switch(action.getNodeType()) {
				case ASTNode.BLOCK :
	                formatLeftCurlyBrace(line, this.preferences.brace_position_for_block);
					action.accept(this);
					break;
				case ASTNode.EMPTY_STATEMENT :
					action.accept(this);
					break;
				default :
					this.scribe.printNewLine();
					this.scribe.indent();
					action.accept(this);
					this.scribe.unIndent();
					this.scribe.printNewLine();
			}
		} else {
			action.accept(this);
		}

		if (this.preferences.insert_new_line_before_while_in_do_statement) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNamewhile, this.preferences.insert_space_after_closing_brace_in_block);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_while);
		
		if (this.preferences.insert_space_after_opening_paren_in_while) {
			this.scribe.space();
		}
		
		node.getExpression().accept(this);
		
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_while);
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(EmptyStatement node) {
		if (this.preferences.put_empty_statement_on_new_line) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(EnhancedForStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamefor);
	    final int line = this.scribe.line;
	    this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_for);
		
		if (this.preferences.insert_space_after_opening_paren_in_for) {
			this.scribe.space();
		}
		node.getParameter().accept(this);

		this.scribe.printNextToken(TerminalTokens.TokenNameCOLON, this.preferences.insert_space_before_colon_in_for);
		if (this.preferences.insert_space_after_colon_in_for) {
			this.scribe.space();
		}
		node.getExpression().accept(this);

		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_for);
		
		formatAction(line, node.getBody(), false);
		return false;
	}

	public boolean visit(EnumConstantDeclaration node) {
        this.scribe.printComment();
        final int line = this.scribe.line; 
        
        final List modifiers = node.modifiers();
        if (modifiers.size() != 0) {
            this.scribe.printModifiers(modifiers, this);
            this.scribe.space();
        }
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		if (isNextToken(TerminalTokens.TokenNameLPAREN)) {
			this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_enum_constant);
			final List arguments = node.arguments();
			final int argumentsLength = arguments.size();
			if (argumentsLength != 0) {
				Alignment2 argumentsAlignment = this.scribe.createAlignment(
						"enumConstantArguments",//$NON-NLS-1$
						this.preferences.alignment_for_arguments_in_enum_constant,
						argumentsLength,
						this.scribe.scanner.currentPosition);
				this.scribe.enterAlignment(argumentsAlignment);
				boolean ok = false;
				do {
					try {
						if (this.preferences.insert_space_after_opening_paren_in_enum_constant) {
							this.scribe.space();
						}
						for (int i = 0; i < argumentsLength; i++) {
							if (i > 0) {
								this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_enum_constant_arguments);
								this.scribe.printTrailingComment();
							}
							this.scribe.alignFragment(argumentsAlignment, i);
							if (i > 0 && this.preferences.insert_space_after_comma_in_enum_constant_arguments) {
								this.scribe.space();
							}
							((Expression) arguments.get(i)).accept(this);
						}
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(argumentsAlignment, true);
			
				this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_enum_constant); 
			} else {
				this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_enum_constant); 
			}
		}

		final AnonymousClassDeclaration anonymousClassDeclaration = node.getAnonymousClassDeclaration();
		if (anonymousClassDeclaration != null) {
			final List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
			String enum_constant_brace = this.preferences.brace_position_for_enum_constant;
			
	        formatLeftCurlyBrace(line, enum_constant_brace);
			formatTypeOpeningBrace(enum_constant_brace, this.preferences.insert_space_before_opening_brace_in_enum_constant, bodyDeclarations.size() != 0, node);
			
			if (this.preferences.indent_body_declarations_compare_to_enum_constant_header) {
				this.scribe.indent();
			}

			formatTypeMembers(bodyDeclarations, true);

			if (this.preferences.indent_body_declarations_compare_to_enum_constant_header) {
				this.scribe.unIndent();
			}
			
			if (this.preferences.insert_new_line_in_empty_enum_constant) {
				this.scribe.printNewLine();
			}
			this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
			if (enum_constant_brace.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
				this.scribe.unIndent();
			}
			if (hasComments()) {
				this.scribe.printNewLine();
			}
		}
		return false;
	}
	
    
	public boolean visit(EnumDeclaration node) {
        /*
         * Print comments to get proper line number
         */
        this.scribe.printComment();
        final int line = this.scribe.line; 
        
        final List modifiers = node.modifiers();
        if (modifiers.size() != 0) {
        	this.scribe.printModifiers(modifiers, this);
        	this.scribe.space();
        }
        	
		this.scribe.printNextToken(TerminalTokens.TokenNameenum, true); 

		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true); 

		/* 
		 * Super Interfaces 
		 */
		final List superInterfaces = node.superInterfaceTypes();
		final int superInterfacesLength = superInterfaces.size();
		if (superInterfacesLength != 0) {
			Alignment2 interfaceAlignment =this.scribe.createAlignment(
					"superInterfaces",//$NON-NLS-1$
					this.preferences.alignment_for_superinterfaces_in_enum_declaration,
					superInterfacesLength+1,  // implements token is first fragment
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(interfaceAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(interfaceAlignment, 0);
					this.scribe.printNextToken(TerminalTokens.TokenNameimplements, true);
					for (int i = 0; i < superInterfacesLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_superinterfaces);
							this.scribe.printTrailingComment();
							this.scribe.alignFragment(interfaceAlignment, i+1);
							if (this.preferences.insert_space_after_comma_in_superinterfaces) {
								this.scribe.space();
							}
							((Type) superInterfaces.get(i)).accept(this);
						} else {
							this.scribe.alignFragment(interfaceAlignment, i+1);
							this.scribe.space();
							((Type) superInterfaces.get(i)).accept(this);
						}
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(interfaceAlignment, true);
		}
		
		final String bracePosition = this.preferences.brace_position_for_enum_declaration;

		final List enumConstants = node.enumConstants();
		final int enumConstantsLength = enumConstants.size();

		formatLeftCurlyBrace(line, bracePosition);
		formatTypeOpeningBrace(bracePosition, this.preferences.insert_space_before_opening_brace_in_enum_declaration, (enumConstantsLength + node.bodyDeclarations().size()) != 0, node);
		
		final boolean indent_body_declarations_compare_to_header = this.preferences.indent_body_declarations_compare_to_enum_declaration_header;
		if (indent_body_declarations_compare_to_header) {
			this.scribe.indent();
		}
		
		if (enumConstantsLength != 0) {
			if (enumConstantsLength > 1) {
				Alignment2 enumConstantsAlignment = this.scribe.createAlignment(
						"enumConstants",//$NON-NLS-1$
						this.preferences.alignment_for_enum_constants,
						enumConstantsLength,
						this.scribe.scanner.currentPosition,
						0, // we don't want to indent enum constants when splitting to a new line
						false);
				this.scribe.enterAlignment(enumConstantsAlignment);
				boolean ok = false;
				do {
					try {
						for (int i = 0; i < enumConstantsLength; i++) {
							this.scribe.alignFragment(enumConstantsAlignment, i);
							final EnumConstantDeclaration enumConstantDeclaration = ((EnumConstantDeclaration) enumConstants.get(i));
							enumConstantDeclaration.accept(this);
							if (isNextToken(TerminalTokens.TokenNameCOMMA)) {
								this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_enum_declarations);
								if (this.preferences.insert_space_after_comma_in_enum_declarations) {
									this.scribe.space();
								}
								this.scribe.printTrailingComment();
								if (enumConstantDeclaration.getAnonymousClassDeclaration() != null) {
									this.scribe.printNewLine();
								}
							}
						}
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(enumConstantsAlignment, true);
			} else {
				final EnumConstantDeclaration enumConstantDeclaration = ((EnumConstantDeclaration) enumConstants.get(0));
				enumConstantDeclaration.accept(this);
				if (isNextToken(TerminalTokens.TokenNameCOMMA)) {
					this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_enum_declarations);
					if (this.preferences.insert_space_after_comma_in_enum_declarations) {
						this.scribe.space();
					}
					this.scribe.printTrailingComment();
					if (enumConstantDeclaration.getAnonymousClassDeclaration() != null) {
						this.scribe.printNewLine();
					}
				}
			}
		}
		if (isNextToken(TerminalTokens.TokenNameSEMICOLON)) {
			this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
		}
		if (enumConstantsLength != 0) {
			this.scribe.printNewLine();	
		}

		formatTypeMembers(node.bodyDeclarations(), true);
		
		if (indent_body_declarations_compare_to_header) {
			this.scribe.unIndent();
		}
		
		if (this.preferences.insert_new_line_in_empty_enum_declaration) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (bracePosition.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}
		if (hasComments()) {
			this.scribe.printNewLine();
		}
		return false;
	}	

	public boolean visit(ExpressionStatement node) {
		node.getExpression().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(FieldAccess node) {
		node.getExpression().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		return false;
	}

	public boolean visit(ForStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamefor);
	    final int line = this.scribe.line;
	    this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_for);
		
		if (this.preferences.insert_space_after_opening_paren_in_for) {
			this.scribe.space();
		}
		final List initializers = node.initializers();
		final int initializersLength = initializers.size();
		if (initializersLength != 0) {
			for (int i = 0; i < initializersLength; i++) {
				Expression initializer = (Expression) initializers.get(i);
				switch(initializer.getNodeType()) {
					case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
						formatLocalDeclaration((VariableDeclarationExpression) initializer, this.preferences.insert_space_before_comma_in_for_inits, this.preferences.insert_space_after_comma_in_for_inits);
						break;
					default:
						initializer.accept(this);
						if (i >= 0 && (i < initializersLength - 1)) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_for_inits);
							if (this.preferences.insert_space_after_comma_in_for_inits) {
								this.scribe.space();
							}
							this.scribe.printTrailingComment();
						}
				}
			}
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon_in_for);
		final Expression condition = node.getExpression();
		if (condition != null) {
			if (this.preferences.insert_space_after_semicolon_in_for) {
				this.scribe.space();
			}
			condition.accept(this);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon_in_for);
		final List updaters = node.updaters();
		final int updatersLength = updaters.size();
		if (updatersLength != 0) {
			if (this.preferences.insert_space_after_semicolon_in_for) {
				this.scribe.space();
			}
			for (int i = 0; i < updatersLength; i++) {
				((Expression) updaters.get(i)).accept(this);
				if (i != updatersLength - 1) {
					this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_for_increments);
					if (this.preferences.insert_space_after_comma_in_for_increments) {
						this.scribe.space();
					}
					this.scribe.printTrailingComment();
				}
			}
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_for);
		
		formatAction(line, node.getBody(), false);
		return false;
	}

	public boolean visit(IfStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameif);
        final int line = this.scribe.line;
        this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_if);
		if (this.preferences.insert_space_after_opening_paren_in_if) {
			this.scribe.space();
		}
		node.getExpression().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_if);

		final Statement thenStatement = node.getThenStatement();
		final Statement elseStatement = node.getElseStatement();

		boolean thenStatementIsBlock = false;
		if (thenStatement != null) {
			if (thenStatement instanceof Block) {
				final Block block = (Block) thenStatement;
				thenStatementIsBlock = true;
				final List statements = block.statements();
				if (isGuardClause(block, statements) && elseStatement == null && this.preferences.keep_guardian_clause_on_one_line) {
					/* 
					 * Need a specific formatting for guard clauses
					 * guard clauses are block with a single return or throw
					 * statement
					 */
					this.scribe.printNextToken(TerminalTokens.TokenNameLBRACE, this.preferences.insert_space_before_opening_brace_in_block);
					this.scribe.space();
					((Statement) statements.get(0)).accept(this);
					this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE, true);
					this.scribe.printTrailingComment();
				} else {
                    formatLeftCurlyBrace(line, this.preferences.brace_position_for_block);
					thenStatement.accept(this);
					if (elseStatement != null && (this.preferences.insert_new_line_before_else_in_if_statement)) {
						this.scribe.printNewLine();
					}
				}
			} else if (elseStatement == null && this.preferences.keep_simple_if_on_one_line) {
				Alignment2 compactIfAlignment = this.scribe.createAlignment(
						"compactIf", //$NON-NLS-1$
						this.preferences.alignment_for_compact_if,
						Alignment.R_OUTERMOST,
						1,
						this.scribe.scanner.currentPosition,
						1,
						false);
				this.scribe.enterAlignment(compactIfAlignment);
				boolean ok = false;
				do {
					try {
						this.scribe.alignFragment(compactIfAlignment, 0);
						this.scribe.space();
						thenStatement.accept(this);
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(compactIfAlignment, true);				
			} else if (this.preferences.keep_then_statement_on_same_line) {
				this.scribe.space();
				thenStatement.accept(this);
				if (elseStatement != null) {
					this.scribe.printNewLine();
				}
			} else {
				this.scribe.printTrailingComment();
				this.scribe.printNewLine();
				this.scribe.indent();
				thenStatement.accept(this);
				if (elseStatement != null) {
					this.scribe.printNewLine();
				}
				this.scribe.unIndent();
			}
		}
		
		if (elseStatement != null) {
			if (thenStatementIsBlock) {
				this.scribe.printNextToken(TerminalTokens.TokenNameelse, this.preferences.insert_space_after_closing_brace_in_block);
			} else {
				this.scribe.printNextToken(TerminalTokens.TokenNameelse, true);
			}
			if (elseStatement instanceof Block) {
				elseStatement.accept(this);
			} else if (elseStatement instanceof IfStatement) {
				if (!this.preferences.compact_else_if) {
					this.scribe.printNewLine();
					this.scribe.indent();
				}
				this.scribe.space();				
				elseStatement.accept(this);
				if (!this.preferences.compact_else_if) {
					this.scribe.unIndent();
				}
			} else if (this.preferences.keep_else_statement_on_same_line) {
				this.scribe.space();
				elseStatement.accept(this);
			} else {
				this.scribe.printNewLine();
				this.scribe.indent();
				elseStatement.accept(this);
				this.scribe.unIndent();
			}
		}
		return false;
	}

	public boolean visit(ImportDeclaration node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameimport);
		this.scribe.space();
		if (node.isStatic()) {
			this.scribe.printNextToken(TerminalTokens.TokenNamestatic);
			this.scribe.space();
		}
		node.getName().accept(this);
		if (node.isOnDemand()) {
			this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
			this.scribe.printNextToken(TerminalTokens.TokenNameMULTIPLY);			
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		this.scribe.printNewLine();
		return false;
	}

	public boolean visit(InfixExpression node) {
		// active line wrapping
		final InfixExpressionWrappingBuilder builder = new InfixExpressionWrappingBuilder();
		node.accept(builder);
		final int fragmentsSize = builder.getFragmentsCounter();
		this.scribe.printComment();
		Alignment2 binaryExpressionAlignment = this.scribe.createAlignment("binaryExpressionAlignment", this.preferences.alignment_for_binary_expression, Alignment.R_OUTERMOST, fragmentsSize, this.scribe.scanner.currentPosition); //$NON-NLS-1$
		this.scribe.enterAlignment(binaryExpressionAlignment);
		boolean ok = false;
		List fragments = builder.fragments;
		int[] operators = builder.getOperators();
/*		do {
			try {
				final boolean alignAfterOperator = false;
				if (alignAfterOperator) {
					for (int i = 0; i < fragmentsSize - 1; i++) {
						this.scribe.alignFragment(binaryExpressionAlignment, i);
						((Expression) fragments.get(i)).accept(this);
						this.scribe.printTrailingComment();
						if (this.scribe.lastNumberOfNewLines == 1) {
							// a new line has been inserted by printTrailingComment()
							this.scribe.indentationLevel = binaryExpressionAlignment.breakIndentationLevel;
						}
						this.scribe.printNextToken(operators[i], this.preferences.insert_space_before_binary_operator);
						if (operators[i] == TerminalTokens.TokenNameMINUS && isNextToken(TerminalTokens.TokenNameMINUS)) {
							// the next character is a minus (unary operator)
							this.scribe.space();
						}
						if (this.preferences.insert_space_after_binary_operator) {
							this.scribe.space();
						}
					}
					this.scribe.alignFragment(binaryExpressionAlignment, fragmentsSize - 1);
					((Expression) fragments.get(fragmentsSize - 1)).accept(this);
					this.scribe.printTrailingComment();
				} else {
					this.scribe.alignFragment(binaryExpressionAlignment, 0);
					((Expression) fragments.get(0)).accept(this);
					this.scribe.printTrailingComment();
					if (this.scribe.lastNumberOfNewLines == 1) {
						if (binaryExpressionAlignment.couldBreak() && binaryExpressionAlignment.wasSplit) {
							binaryExpressionAlignment.performFragmentEffect();
						}
					}
					for (int i = 1; i < fragmentsSize - 1; i++) {
						this.scribe.alignFragment(binaryExpressionAlignment, i);
						this.scribe.printNextToken(operators[i - 1], this.preferences.insert_space_before_binary_operator);
						if (operators[i] == TerminalTokens.TokenNameMINUS && isNextToken(TerminalTokens.TokenNameMINUS)) {
							// the next character is a minus (unary operator)
							this.scribe.space();
						}
						if (this.preferences.insert_space_after_binary_operator) {
							this.scribe.space();
						}
						((Expression) fragments.get(i)).accept(this);
						this.scribe.printTrailingComment();
						if (this.scribe.lastNumberOfNewLines == 1) {
							if (binaryExpressionAlignment.couldBreak() && binaryExpressionAlignment.wasSplit) {
								binaryExpressionAlignment.performFragmentEffect();
							}
						}
					}
					this.scribe.alignFragment(binaryExpressionAlignment, fragmentsSize - 1);
					this.scribe.printNextToken(operators[fragmentsSize - 2], this.preferences.insert_space_before_binary_operator);
					if (operators[fragmentsSize - 2] == TerminalTokens.TokenNameMINUS && isNextToken(TerminalTokens.TokenNameMINUS)) {
						// the next character is a minus (unary operator)
						this.scribe.space();
					}
					if (this.preferences.insert_space_after_binary_operator) {
						this.scribe.space();
					}
					((Expression) fragments.get(fragmentsSize - 1)).accept(this);
					this.scribe.printTrailingComment();
					if (this.scribe.lastNumberOfNewLines == 1) {
						if (binaryExpressionAlignment.couldBreak() && binaryExpressionAlignment.wasSplit) {
							binaryExpressionAlignment.performFragmentEffect();
						}
					}
				}
				ok = true;
			} catch(AlignmentException e){
				this.scribe.redoAlignment(e);
			}
		} while (!ok);
		this.scribe.exitAlignment(binaryExpressionAlignment, true);*/
		do {
			try {
				for (int i = 0; i < fragmentsSize - 1; i++) {
					((Expression) fragments.get(i)).accept(this);
					this.scribe.printTrailingComment();
					if (this.scribe.lastNumberOfNewLines == 1) {
						if (binaryExpressionAlignment.couldBreak() && binaryExpressionAlignment.wasSplit) {
							binaryExpressionAlignment.performFragmentEffect();
						}
					}
					this.scribe.alignFragment(binaryExpressionAlignment, i);
					this.scribe.printNextToken(operators[i], this.preferences.insert_space_before_binary_operator);
					this.scribe.printTrailingComment();
					if (this.scribe.lastNumberOfNewLines == 1) {
						if (binaryExpressionAlignment.couldBreak() && binaryExpressionAlignment.wasSplit) {
							binaryExpressionAlignment.performFragmentEffect();
						}
					}
					if ( this.preferences.insert_space_after_binary_operator
							|| (operators[i] == TerminalTokens.TokenNameMINUS && isNextToken(TerminalTokens.TokenNameMINUS))) {
						// the next character is a minus (unary operator) or the preference is set to true
						this.scribe.space();
					}
				}
				((Expression) fragments.get(fragmentsSize - 1)).accept(this);
				this.scribe.printTrailingComment();
				if (this.scribe.lastNumberOfNewLines == 1) {
					if (binaryExpressionAlignment.couldBreak() && binaryExpressionAlignment.wasSplit) {
						binaryExpressionAlignment.performFragmentEffect();
					}
				}
				ok = true;
			} catch(AlignmentException e){
				this.scribe.redoAlignment(e);
			}
		} while (!ok);		
		this.scribe.exitAlignment(binaryExpressionAlignment, true);		
/*			leftOperand.accept(this);
			final int operator = extractInfixExpressionOperator(node);
			this.scribe.printNextToken(operator, this.preferences.insert_space_before_binary_operator);
			if ( this.preferences.insert_space_after_binary_operator
					|| (operator == TerminalTokens.TokenNameMINUS && isNextToken(TerminalTokens.TokenNameMINUS))) {
				// the next character is a minus (unary operator) or the preference is set to true
				this.scribe.space();
			}
			rightOperand.accept(this);*/
		return false;
	}

	public boolean visit(Initializer node) {
		final List modifiers = node.modifiers();
		if (modifiers.size() != 0) {
			this.scribe.printModifiers(modifiers, this);
			this.scribe.space();
		}
		node.getBody().accept(this);
		return false;
	}

	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameinstanceof, true);
		this.scribe.space();
		node.getRightOperand().accept(this);
		return false;
	}

	public boolean visit(LabeledStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		this.scribe.printNextToken(TerminalTokens.TokenNameCOLON, this.preferences.insert_space_before_colon_in_labeled_statement);
		if (this.preferences.insert_space_after_colon_in_labeled_statement) {
			this.scribe.space();
		}
		node.getBody().accept(this);
		return false;
	}
	
	public boolean visit(MarkerAnnotation node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameAT);
		if (this.preferences.insert_space_after_at_in_annotation) {
			this.scribe.space();
		}
		node.getTypeName().accept(this);
		return false;
	}

	public boolean visit(MemberValuePair node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
		if (this.preferences.insert_space_after_assignment_operator) {
			this.scribe.space();
		}
		node.getValue().accept(this);	
		return false;
	}

	public boolean visit(MethodDeclaration node) {
		if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
			this.scribe.printComment();
			this.scribe.scanner.resetTo(node.getStartPosition() + node.getLength(), this.scribe.scannerEndPosition);
			this.scribe.printTrailingComment();
			return false;
		}
        /*
         * Print comments to get proper line number
         */
        this.scribe.printComment();
        final int line = this.scribe.line;
        
        final List modifiers = node.modifiers();
        final int modifiersLength = modifiers.size();
        if (modifiersLength != 0) {
        	this.scribe.printModifiers(modifiers, this);
        	this.scribe.space();
        }
		
        final List typeParameters = node.typeParameters();
        final int typeParametersLength = typeParameters.size();
		if (typeParametersLength != 0) {
			this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_type_parameters); 
			if (this.preferences.insert_space_after_opening_angle_bracket_in_type_parameters) {
				this.scribe.space();
			}
			for (int i = 0; i < typeParametersLength - 1; i++) {
				((TypeParameter) typeParameters.get(i)).accept(this);
				this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_type_parameters);
				if (this.preferences.insert_space_after_comma_in_type_parameters) {
					this.scribe.space();
				}				
			}
			((TypeParameter) typeParameters.get(typeParametersLength - 1)).accept(this);
			if (isClosingGenericToken()) {
				this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_type_parameters); 
			}
			if (this.preferences.insert_space_after_closing_angle_bracket_in_type_parameters) {
				this.scribe.space();
			}
		}
		
		final Type returnType = node.getReturnType2();
		if (returnType != null) {
			returnType.accept(this);
		}
		/*
		 * Print the method name
		 */
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true); 

		boolean spaceBeforeParen = this.preferences.insert_space_before_opening_paren_in_method_declaration;
		if (node.isConstructor()) {
			spaceBeforeParen = this.preferences.insert_space_before_opening_paren_in_constructor_declaration;
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, spaceBeforeParen); 
		
		final List parameters = node.parameters();
		final int parametersLength = parameters.size();
		if (parametersLength != 0) {
			if (this.preferences.insert_space_after_opening_paren_in_method_declaration) {
				this.scribe.space();
			}
			Alignment2 parametersAlignment = this.scribe.createAlignment(
					"methodParameters",//$NON-NLS-1$
					this.preferences.alignment_for_parameters_in_method_declaration,
					parametersLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(parametersAlignment);
			boolean ok = false;
			do {
				try {
					for (int i = 0; i < parametersLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_method_declaration_parameters);
							this.scribe.printTrailingComment();
						}
						this.scribe.alignFragment(parametersAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_method_declaration_parameters) {
							this.scribe.space();
						}
						((SingleVariableDeclaration) parameters.get(i)).accept(this);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(parametersAlignment, true);
		
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_method_declaration); 
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_method_declaration); 
		}
		/*
		 * Check for extra dimensions
		 */
		final int extraDimensions = node.getExtraDimensions();
		if (extraDimensions != 0) {
			 for (int i = 0; i < extraDimensions; i++) {
			 	this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
			 	this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
			 }
		}
				
		final List thrownExceptions = node.thrownExceptions();
		final int thrownExceptionsLength = thrownExceptions.size();
		if (thrownExceptionsLength != 0) {
			Alignment2 throwsAlignment = this.scribe.createAlignment(
					"throws",//$NON-NLS-1$
					node.isConstructor()
						? this.preferences.alignment_for_throws_clause_in_constructor_declaration
						: this.preferences.alignment_for_throws_clause_in_method_declaration,
					thrownExceptionsLength, // throws is the first token
					this.scribe.scanner.currentPosition);
		
			this.scribe.enterAlignment(throwsAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(throwsAlignment, 0);
					this.scribe.printNextToken(TerminalTokens.TokenNamethrows, true); 
		
					for (int i = 0; i < thrownExceptionsLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_method_declaration_throws);
							this.scribe.printTrailingComment();
							this.scribe.alignFragment(throwsAlignment, i);
							if (this.preferences.insert_space_after_comma_in_method_declaration_throws) {
								this.scribe.space();
							}
						} else {
							this.scribe.space();
						}
						((Name) thrownExceptions.get(i)).accept(this);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(throwsAlignment, true);
		}

		final Block body = node.getBody();
		if (body != null) {
			/*
			 * Method body
			 */
			String method_declaration_brace = this.preferences.brace_position_for_method_declaration;
            formatLeftCurlyBrace(line, method_declaration_brace);
			formatOpeningBrace(method_declaration_brace, this.preferences.insert_space_before_opening_brace_in_method_declaration);
			final int numberOfBlankLinesAtBeginningOfMethodBody = this.preferences.blank_lines_at_beginning_of_method_body;
			if (numberOfBlankLinesAtBeginningOfMethodBody > 0) {
				this.scribe.printEmptyLines(numberOfBlankLinesAtBeginningOfMethodBody);
			}
			final List statements = body.statements();
			final int statementsLength = statements.size();
			if (statementsLength != 0) {
				this.scribe.printNewLine();
				if (this.preferences.indent_statements_compare_to_body) {
					this.scribe.indent();
				}
				formatStatements(statements, true);
				this.scribe.printComment();
				if (this.preferences.indent_statements_compare_to_body) {
					this.scribe.unIndent();
				}
			} else if (this.preferences.insert_new_line_in_empty_method_body) {
				this.scribe.printNewLine();
				if (this.preferences.indent_statements_compare_to_body) {
					this.scribe.indent();
				}
				this.scribe.printComment();
				if (this.preferences.indent_statements_compare_to_body) {
					this.scribe.unIndent();
				}
			}
			this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
			this.scribe.printTrailingComment();
			if (method_declaration_brace.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
				this.scribe.unIndent();
			}
		} else {
			// no method body
			this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
		}
		return false;
	}

	public boolean visit(MethodInvocation node) {
		MethodInvocationFragmentBuilder builder = new MethodInvocationFragmentBuilder();
		node.accept(builder);
		
		final List fragments = builder.fragments();
		final int fragmentsLength = fragments.size();
		if (fragmentsLength >= 3) {
			// manage cascading method invocations
			// check the first fragment
			final Expression firstFragment = (Expression) fragments.get(0);
			switch(firstFragment.getNodeType()) {
				case ASTNode.METHOD_INVOCATION :
					formatSingleMethodInvocation((MethodInvocation) firstFragment);
					break;
				default:
					firstFragment.accept(this);
			}
			Alignment2 cascadingMessageSendAlignment =
				this.scribe.createAlignment(
					"cascadingMessageSendAlignment", //$NON-NLS-1$
					this.preferences.alignment_for_selector_in_method_invocation,
					Alignment.R_INNERMOST,
					fragmentsLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(cascadingMessageSendAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(cascadingMessageSendAlignment, 0);
					this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
					for (int i = 1; i < fragmentsLength - 1; i++) {
						MethodInvocation  currentMethodInvocation = (MethodInvocation) fragments.get(i);
						formatSingleMethodInvocation(currentMethodInvocation);
						this.scribe.alignFragment(cascadingMessageSendAlignment, i);
						this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
					}
					MethodInvocation  currentMethodInvocation = (MethodInvocation) fragments.get(fragmentsLength - 1);
					formatSingleMethodInvocation(currentMethodInvocation);
					ok = true;
				} catch(AlignmentException e){
					this.scribe.redoAlignment(e);
				}
			} while (!ok);		
			this.scribe.exitAlignment(cascadingMessageSendAlignment, true);
		} else {
			Expression expression = node.getExpression();
			if (expression != null) {
				expression.accept(this);
				this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
			}
			formatSingleMethodInvocation(node);			
		}
		return false;
	}

	private void formatSingleMethodInvocation(MethodInvocation node) {
		final List typeArguments = node.typeArguments();
		final int typeArgumentsLength = typeArguments.size();
		if (typeArgumentsLength != 0) {
			this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_type_arguments); 
			if (this.preferences.insert_space_after_opening_angle_bracket_in_type_arguments) {
				this.scribe.space();
			}
			for (int i = 0; i < typeArgumentsLength - 1; i++) {
				((Type) typeArguments.get(i)).accept(this);
				this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_type_arguments);
				if (this.preferences.insert_space_after_comma_in_type_arguments) {
					this.scribe.space();
				}				
			}
			((Type) typeArguments.get(typeArgumentsLength - 1)).accept(this);
			if (isClosingGenericToken()) {
				this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_type_arguments); 
			}
			if (this.preferences.insert_space_after_closing_angle_bracket_in_type_arguments) {
				this.scribe.space();
			}
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier); // selector
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_method_invocation);

		final List arguments = node.arguments();
		final int argumentsLength = arguments.size();
		if (argumentsLength != 0) {
			if (this.preferences.insert_space_after_opening_paren_in_method_invocation) {
				this.scribe.space();
			}
			if (argumentsLength > 1) {
				Alignment2 argumentsAlignment = this.scribe.createAlignment(
						"messageArguments", //$NON-NLS-1$
						this.preferences.alignment_for_arguments_in_method_invocation,
						argumentsLength,
						this.scribe.scanner.currentPosition);
				this.scribe.enterAlignment(argumentsAlignment);
				boolean ok = false;
				do {
					try {
						for (int i = 0; i < argumentsLength; i++) {
							if (i > 0) {
								this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_method_invocation_arguments);
								this.scribe.printTrailingComment();
							}
							this.scribe.alignFragment(argumentsAlignment, i);
							if (i > 0 && this.preferences.insert_space_after_comma_in_method_invocation_arguments) {
								this.scribe.space();
							}
							((Expression) arguments.get(i)).accept(this);
						}
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(argumentsAlignment, true);
			} else {
				for (int i = 0; i < argumentsLength; i++) {
					if (i > 0) {
						this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_method_invocation_arguments);
						this.scribe.printTrailingComment();
					}
					if (i > 0 && this.preferences.insert_space_after_comma_in_method_invocation_arguments) {
						this.scribe.space();
					}
					((Expression) arguments.get(i)).accept(this);
				}
			}
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_method_invocation); 
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_method_invocation);
		}
	}
	
	public boolean visit(NormalAnnotation node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameAT);
		if (this.preferences.insert_space_after_at_in_annotation) {
			this.scribe.space();
		}
		node.getTypeName().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_annotation);
		if (this.preferences.insert_space_after_opening_paren_in_annotation) {
			this.scribe.space();
		}
		final List memberValuePairs = node.values();
		final int memberValuePairsLength = memberValuePairs.size();
		if (memberValuePairs.size() != 0) {
			for (int i = 0; i < memberValuePairsLength - 1; i++) {
				((MemberValuePair) memberValuePairs.get(i)).accept(this);
				this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_annotation);
				if (this.preferences.insert_space_after_comma_in_annotation) {
					this.scribe.space();
				}
			}
			((MemberValuePair) memberValuePairs.get(memberValuePairsLength - 1)).accept(this);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_annotation);
		return false;
	}

	public boolean visit(NullLiteral node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamenull);
		return false;
	}

	public boolean visit(NumberLiteral node) {
		if (isNextToken(TerminalTokens.TokenNameMINUS)) {
			this.scribe.printNextToken(TerminalTokens.TokenNameMINUS, this.preferences.insert_space_before_unary_operator);
			if (this.preferences.insert_space_after_unary_operator) {
				this.scribe.space();
			}
		}
		this.scribe.printNextToken(NUMBER_LITERALS_EXPECTEDTOKENS); 
		return false;
	}

	public boolean visit(ParameterizedType node) {
		node.getType().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_parameterized_type_reference);
		if (this.preferences.insert_space_after_opening_angle_bracket_in_parameterized_type_reference) {
			this.scribe.space();
		}
		final List typeArguments = node.typeArguments();
		final int typeArgumentsLength = typeArguments.size();
		for (int i = 0; i < typeArgumentsLength - 1; i++) {
			((Type) typeArguments.get(i)).accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_parameterized_type_reference);
			if (this.preferences.insert_space_after_comma_in_parameterized_type_reference) {
				this.scribe.space();
			}			
		}
		((Type) typeArguments.get(typeArgumentsLength - 1)).accept(this);
		if (isClosingGenericToken()) {
			this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_parameterized_type_reference);
		}
		return false;
	}

	public boolean visit(ParenthesizedExpression node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_parenthesized_expression);
		if (this.preferences.insert_space_after_opening_paren_in_parenthesized_expression) {
			this.scribe.space();
		}
		node.getExpression().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_parenthesized_expression);
		return false;
	}

	public boolean visit(PostfixExpression node) {
		node.getOperand().accept(this);
		int operator = node.getOperator() == PostfixExpression.Operator.INCREMENT
			? TerminalTokens.TokenNamePLUS_PLUS : TerminalTokens.TokenNameMINUS_MINUS;
		this.scribe.printNextToken(operator, this.preferences.insert_space_before_postfix_operator);
		if (this.preferences.insert_space_after_postfix_operator) {
			this.scribe.space();
		}
		return false;
	}

	public boolean visit(PrefixExpression node) {
		int operator;
		boolean insertSpaceBeforeOperator;
		boolean insertSpaceAfterOperator;
		final Operator operator2 = node.getOperator();
		if (operator2 == PrefixExpression.Operator.INCREMENT) {
			operator = TerminalTokens.TokenNamePLUS_PLUS;
			insertSpaceBeforeOperator = this.preferences.insert_space_before_prefix_operator;
			insertSpaceAfterOperator = this.preferences.insert_space_after_prefix_operator;
		} else if (operator2 == PrefixExpression.Operator.DECREMENT) {
			operator = TerminalTokens.TokenNameMINUS_MINUS;
			insertSpaceBeforeOperator = this.preferences.insert_space_before_prefix_operator;
			insertSpaceAfterOperator = this.preferences.insert_space_after_prefix_operator;
		} else if (operator2 == PrefixExpression.Operator.COMPLEMENT) {
			operator = TerminalTokens.TokenNameTWIDDLE;
			insertSpaceBeforeOperator = this.preferences.insert_space_before_unary_operator;
			insertSpaceAfterOperator = this.preferences.insert_space_after_unary_operator;
		} else if (operator2 == PrefixExpression.Operator.MINUS) {
			operator = TerminalTokens.TokenNameMINUS;
			insertSpaceBeforeOperator = this.preferences.insert_space_before_unary_operator;
			insertSpaceAfterOperator = this.preferences.insert_space_after_unary_operator;
		} else if (operator2 == PrefixExpression.Operator.NOT) {
			operator = TerminalTokens.TokenNameNOT;
			insertSpaceBeforeOperator = this.preferences.insert_space_before_unary_operator;
			insertSpaceAfterOperator = this.preferences.insert_space_after_unary_operator;
		} else {
			operator = TerminalTokens.TokenNamePLUS;
			insertSpaceBeforeOperator = this.preferences.insert_space_before_unary_operator;
			insertSpaceAfterOperator = this.preferences.insert_space_after_unary_operator;
		}

		this.scribe.printNextToken(operator, insertSpaceBeforeOperator);
		if (insertSpaceAfterOperator) {
			this.scribe.space();
		}
		node.getOperand().accept(this);
		return false;
	}

	public boolean visit(PrimitiveType node) {
		this.scribe.printNextToken(PRIMITIVE_TYPE_EXPECTEDTOKENS);
		return false;
	}

	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		node.getName().accept(this);
		return false;
	}

	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		node.getName().accept(this);
		return false;
	}

	public boolean visit(ReturnStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamereturn);
		final Expression expression = node.getExpression();
		if (expression != null) {
			switch(expression.getNodeType()) {
				case ASTNode.PARENTHESIZED_EXPRESSION :
					if (this.preferences.insert_space_before_parenthesized_expression_in_return) {
						this.scribe.space();
					}
					break;
				default:
					this.scribe.space();
			}
			expression.accept(this);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(SimpleName node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		return false;
	}

	public boolean visit(SimpleType node) {
		node.getName().accept(this);
		return false;
	}

	public boolean visit(SingleMemberAnnotation node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameAT);
		if (this.preferences.insert_space_after_at_in_annotation) {
			this.scribe.space();
		}
		node.getTypeName().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_annotation);
		if (this.preferences.insert_space_after_opening_paren_in_annotation) {
			this.scribe.space();
		}
		node.getValue().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_annotation);
		return false;
	}

	public boolean visit(SingleVariableDeclaration node) {
		final List modifiers = node.modifiers();
		if (modifiers.size() != 0) {
			this.scribe.printModifiers(modifiers, this);
			this.scribe.space();
		}

		node.getType().accept(this);
		
		if (node.isVarargs()) {
			this.scribe.printNextToken(TerminalTokens.TokenNameELLIPSIS, this.preferences.insert_space_before_ellipsis);
			if (this.preferences.insert_space_after_ellipsis) {
				this.scribe.space();
			}
			this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, false);
		} else {
			/*
			 * Print the argument name
			 */	
			this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true);
		}
		
		final int extraDimensions = node.getExtraDimensions();
		if (extraDimensions != 0) {
			for (int i = 0; i < extraDimensions; i++) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLBRACKET);
				this.scribe.printNextToken(TerminalTokens.TokenNameRBRACKET);
			}
		}
		final Expression initialization = node.getInitializer();
		if (initialization != null) {
			this.scribe.printNextToken(TerminalTokens.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operator);
			if (this.preferences.insert_space_after_assignment_operator) {
				this.scribe.space();
			}
			Alignment2 assignmentAlignment = this.scribe.createAlignment("localDeclarationAssignmentAlignment", this.preferences.alignment_for_assignment, 1, this.scribe.scanner.currentPosition); //$NON-NLS-1$
			this.scribe.enterAlignment(assignmentAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(assignmentAlignment, 0);
					initialization.accept(this);
					ok = true;
				} catch(AlignmentException e){
					this.scribe.redoAlignment(e);
				}
			} while (!ok);		
			this.scribe.exitAlignment(assignmentAlignment, true);			
		}
		return false;
	}

	public boolean visit(StringLiteral node) {
		this.scribe.checkNLSTag(node.getStartPosition());
		this.scribe.printNextToken(TerminalTokens.TokenNameStringLiteral);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(SuperConstructorInvocation node) {
		final Expression qualification = node.getExpression();
		if (qualification != null) {
			qualification.accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		}
		final List typeArguments = node.typeArguments();
		final int typeArgumentsLength = typeArguments.size();
		if (typeArgumentsLength != 0) {
				this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_type_arguments); 
				if (this.preferences.insert_space_after_opening_angle_bracket_in_type_arguments) {
					this.scribe.space();
				}
				for (int i = 0; i < typeArgumentsLength - 1; i++) {
					((Type) typeArguments.get(i)).accept(this);
					this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_type_arguments);
					if (this.preferences.insert_space_after_comma_in_type_arguments) {
						this.scribe.space();
					}				
				}
				((Type) typeArguments.get(typeArgumentsLength - 1)).accept(this);
				if (isClosingGenericToken()) {
					this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_type_arguments); 
				}
				if (this.preferences.insert_space_after_closing_angle_bracket_in_type_arguments) {
					this.scribe.space();
				}
		}
		
		this.scribe.printNextToken(TerminalTokens.TokenNamesuper);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_method_invocation);
		
		final List arguments = node.arguments();
		final int argumentsLength = arguments.size();
		if (argumentsLength != 0) {
			if (this.preferences.insert_space_after_opening_paren_in_method_invocation) {
				this.scribe.space();
			}
			Alignment2 argumentsAlignment =this.scribe.createAlignment(
					"explicit_constructor_call",//$NON-NLS-1$
					this.preferences.alignment_for_arguments_in_explicit_constructor_call,
					argumentsLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					for (int i = 0; i < argumentsLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_explicit_constructor_call_arguments);
							this.scribe.printTrailingComment();
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_explicit_constructor_call_arguments) {
							this.scribe.space();
						}
						((Expression) arguments.get(i)).accept(this);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_method_invocation); 
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_method_invocation); 
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(SuperFieldAccess node) {
		final Name qualifier = node.getQualifier();
		if (qualifier != null) {
			qualifier.accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNamesuper);
		this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		return false;
	}

	public boolean visit(SuperMethodInvocation node) {
		final Name qualifier = node.getQualifier();
		if (qualifier != null) {
			qualifier.accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNamesuper);
		this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_method_invocation);

		final List arguments = node.arguments();
		final int argumentsLength = arguments.size();
		if (argumentsLength != 0) {
			if (this.preferences.insert_space_after_opening_paren_in_method_invocation) {
				this.scribe.space();
			}
			if (argumentsLength > 1) {
				Alignment2 argumentsAlignment = this.scribe.createAlignment(
						"messageArguments", //$NON-NLS-1$
						this.preferences.alignment_for_arguments_in_method_invocation,
						argumentsLength,
						this.scribe.scanner.currentPosition);
				this.scribe.enterAlignment(argumentsAlignment);
				boolean ok = false;
				do {
					try {
						for (int i = 0; i < argumentsLength; i++) {
							if (i > 0) {
								this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_method_invocation_arguments);
								this.scribe.printTrailingComment();
							}
							this.scribe.alignFragment(argumentsAlignment, i);
							if (i > 0 && this.preferences.insert_space_after_comma_in_method_invocation_arguments) {
								this.scribe.space();
							}
							((Expression) arguments.get(i)).accept(this);
						}
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(argumentsAlignment, true);
			} else {
				for (int i = 0; i < argumentsLength; i++) {
					if (i > 0) {
						this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_method_invocation_arguments);
						this.scribe.printTrailingComment();
					}
					if (i > 0 && this.preferences.insert_space_after_comma_in_method_invocation_arguments) {
						this.scribe.space();
					}
					((Expression) arguments.get(i)).accept(this);
				}
			}
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_method_invocation); 
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_between_empty_parens_in_method_invocation);
		}			
		return false;
	}

	public boolean visit(SwitchCase node) {
		Expression constant = node.getExpression();
		if (constant == null) {
			this.scribe.printNextToken(TerminalTokens.TokenNamedefault);
			this.scribe.printNextToken(TerminalTokens.TokenNameCOLON, this.preferences.insert_space_before_colon_in_default);
		} else {
			this.scribe.printNextToken(TerminalTokens.TokenNamecase);
			this.scribe.space();
			constant.accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameCOLON, this.preferences.insert_space_before_colon_in_case);
		}
		return false;
	}

	public boolean visit(SwitchStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameswitch);
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_switch);
		
		if (this.preferences.insert_space_after_opening_paren_in_switch) {
			this.scribe.space();
		}
		
		node.getExpression().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_switch);
		/*
		 * Type body
		 */
		String switch_brace = this.preferences.brace_position_for_switch;
		formatOpeningBrace(switch_brace, this.preferences.insert_space_before_opening_brace_in_switch);
		this.scribe.printNewLine();

		if (this.preferences.indent_switchstatements_compare_to_switch) {
			this.scribe.indent();
		}
		final List statements = node.statements();
		final int statementsLength = statements.size();
		boolean wasACase = false;
		boolean wasAStatement = false;
		if (statementsLength != 0) {
			for (int i = 0; i < statementsLength; i++) {
				final Statement statement = (Statement) statements.get(i);
				if (statement instanceof SwitchCase) {
					if (wasACase) {
						this.scribe.printNewLine();
					}
					if ((wasACase && this.preferences.indent_switchstatements_compare_to_cases) 
						|| (wasAStatement && this.preferences.indent_switchstatements_compare_to_cases)) {
						this.scribe.unIndent();
					}
					statement.accept(this);
					this.scribe.printTrailingComment();
					wasACase = true;
					wasAStatement = false;
					if (this.preferences.indent_switchstatements_compare_to_cases) {
						this.scribe.indent();
					}
				} else if (statement instanceof BreakStatement) {
					if (this.preferences.indent_breaks_compare_to_cases) {
						if (wasAStatement && !this.preferences.indent_switchstatements_compare_to_cases) {
							this.scribe.indent();
						}
					} else {
						if (wasAStatement) {
							if (this.preferences.indent_switchstatements_compare_to_cases) {
								this.scribe.unIndent();
							}
						}
						if (wasACase && this.preferences.indent_switchstatements_compare_to_cases) {
							this.scribe.unIndent();
						}
					}
					if (wasACase) {
						this.scribe.printNewLine();
					}
					statement.accept(this);
					if (this.preferences.indent_breaks_compare_to_cases) {
						this.scribe.unIndent();
					}
					wasACase = false;
					wasAStatement = false;
				} else if (statement instanceof Block) {
					String bracePosition;
					if (wasACase) {
						if (this.preferences.indent_switchstatements_compare_to_cases) {
							this.scribe.unIndent();
						}
						bracePosition =	this.preferences.brace_position_for_block_in_case;
						formatBlock((Block) statement, bracePosition, this.preferences.insert_space_after_colon_in_case);
						if (this.preferences.indent_switchstatements_compare_to_cases) {
							this.scribe.indent();
						}
					} else {
						bracePosition =	this.preferences.brace_position_for_block;
						formatBlock((Block) statement, bracePosition, this.preferences.insert_space_before_opening_brace_in_block);
					}
					wasAStatement = true;
					wasACase = false;
				} else {
					this.scribe.printNewLine();
					statement.accept(this);
					wasAStatement = true;
					wasACase = false;
				}
				if (!wasACase) {
					this.scribe.printNewLine();
				}
				this.scribe.printComment();
			}
		}		
		
		if ((wasACase || wasAStatement) && this.preferences.indent_switchstatements_compare_to_cases) {
			this.scribe.unIndent();
		}
		if (this.preferences.indent_switchstatements_compare_to_switch) {
			this.scribe.unIndent();
		}
		this.scribe.printNewLine();
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (switch_brace.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}		
		return false;
	}

	public boolean visit(SynchronizedStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamesynchronized);

		final int line = this.scribe.line;

		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_synchronized);
		
		if (this.preferences.insert_space_after_opening_paren_in_synchronized) {
			this.scribe.space();
		}
		node.getExpression().accept(this);
	
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_synchronized);
		
		formatLeftCurlyBrace(line, this.preferences.brace_position_for_block);
		node.getBody().accept(this);
		return false;
	}

	public boolean visit(ThisExpression node) {
		final Name qualifier = node.getQualifier();
		if (qualifier != null) {
			qualifier.accept(this);
			this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		}
		this.scribe.printNextToken(TerminalTokens.TokenNamethis);
		return false;
	}

	public boolean visit(ThrowStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamethrow);
		this.scribe.space();
		node.getExpression().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(TryStatement node) {

		this.scribe.printNextToken(TerminalTokens.TokenNametry);
		node.getBody().accept(this);
		final List catchClauses = node.catchClauses();
		final int catchClausesLength = catchClauses.size();
		if (catchClausesLength != 0) {
			for (int i = 0; i < catchClausesLength; i++) {
				if (this.preferences.insert_new_line_before_catch_in_try_statement) {
					this.scribe.printNewLine();
				}	
				this.scribe.printNextToken(TerminalTokens.TokenNamecatch, this.preferences.insert_space_after_closing_brace_in_block);
				final int line = this.scribe.line;
				this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_catch);
				
				if (this.preferences.insert_space_after_opening_paren_in_catch) {
					this.scribe.space();
				}
				
				final CatchClause catchClause = ((CatchClause) catchClauses.get(i));
				catchClause.getException().accept(this);
			
				this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_catch);
				
				formatLeftCurlyBrace(line, this.preferences.brace_position_for_block);
				catchClause.getBody().accept(this);
			}
		}
		final Block finallyBlock = node.getFinally();
		if (finallyBlock != null) {
			if (this.preferences.insert_new_line_before_finally_in_try_statement) {
				this.scribe.printNewLine();
			}	
			this.scribe.printNextToken(TerminalTokens.TokenNamefinally, this.preferences.insert_space_after_closing_brace_in_block);
			finallyBlock.accept(this);
		}
		return false;
	}

	public boolean visit(TypeDeclaration node) {
        /*
         * Print comments to get proper line number
         */
        this.scribe.printComment();
        final int line = this.scribe.line; 
        
        final List modifiers = node.modifiers();
        if (modifiers.size() != 0) {
            this.scribe.printModifiers(modifiers, this);
            this.scribe.space();
        }
        final boolean isInterface = node.isInterface();
		if (isInterface) {
			this.scribe.printNextToken(TerminalTokens.TokenNameinterface, true);
        } else {
			this.scribe.printNextToken(TerminalTokens.TokenNameclass, true);
        }

        this.scribe.printNextToken(TerminalTokens.TokenNameIdentifier, true); 

        final List typeParameters = node.typeParameters();
        final int typeParametersLength = typeParameters.size();
		if (typeParametersLength != 0) {
			this.scribe.printNextToken(TerminalTokens.TokenNameLESS, this.preferences.insert_space_before_opening_angle_bracket_in_type_parameters); 
			if (this.preferences.insert_space_after_opening_angle_bracket_in_type_parameters) {
				this.scribe.space();
			}
			for (int i = 0; i < typeParametersLength - 1; i++) {
				((TypeParameter) typeParameters.get(i)).accept(this);
				this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_type_parameters);
				if (this.preferences.insert_space_after_comma_in_type_parameters) {
					this.scribe.space();
				}				
			}
			((TypeParameter) typeParameters.get(typeParametersLength - 1)).accept(this);
			if (isClosingGenericToken()) {
				this.scribe.printNextToken(CLOSING_GENERICS_EXPECTEDTOKENS, this.preferences.insert_space_before_closing_angle_bracket_in_type_parameters); 
			}
			if (this.preferences.insert_space_after_closing_angle_bracket_in_type_parameters) {
				this.scribe.space();
			}
		}
		/* 
		 * Superclass 
		 */
		final Type superclass = node.getSuperclassType();
		if (superclass != null) {
			Alignment2 superclassAlignment =this.scribe.createAlignment(
					"superclass", //$NON-NLS-1$
					this.preferences.alignment_for_superclass_in_type_declaration,
					2,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(superclassAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(superclassAlignment, 0);
					this.scribe.printNextToken(TerminalTokens.TokenNameextends, true);
					this.scribe.alignFragment(superclassAlignment, 1);
					this.scribe.space();
					superclass.accept(this);
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(superclassAlignment, true); 
		}

		/* 
		 * Super Interfaces 
		 */
		final List superInterfaces = node.superInterfaceTypes();
		final int superInterfacesLength = superInterfaces.size();
		if (superInterfacesLength != 0) {
			Alignment2 interfaceAlignment = this.scribe.createAlignment(
					"superInterfaces",//$NON-NLS-1$
					this.preferences.alignment_for_superinterfaces_in_type_declaration,
					superInterfacesLength+1,  // implements token is first fragment
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(interfaceAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(interfaceAlignment, 0);
					if (isInterface) {
						this.scribe.printNextToken(TerminalTokens.TokenNameextends, true);
					} else {
						this.scribe.printNextToken(TerminalTokens.TokenNameimplements, true);
					}
					this.scribe.alignFragment(interfaceAlignment, 1);
					this.scribe.space();
					((Type) superInterfaces.get(0)).accept(this);
					for (int i = 1; i < superInterfacesLength; i++) {
						this.scribe.printNextToken(TerminalTokens.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_superinterfaces);
						this.scribe.printTrailingComment();
						this.scribe.alignFragment(interfaceAlignment, i+1);
						if (this.preferences.insert_space_after_comma_in_superinterfaces) {
							this.scribe.space();
						}
						((Type) superInterfaces.get(i)).accept(this);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(interfaceAlignment, true);
		}

		final String class_declaration_brace = this.preferences.brace_position_for_type_declaration;
		final boolean space_before_opening_brace = this.preferences.insert_space_before_opening_brace_in_type_declaration;

		formatLeftCurlyBrace(line, class_declaration_brace);
		final List bodyDeclarations = node.bodyDeclarations();
		formatTypeOpeningBrace(class_declaration_brace, space_before_opening_brace, bodyDeclarations.size() != 0, node);
		
		final boolean indent_body_declarations_compare_to_header = this.preferences.indent_body_declarations_compare_to_type_header;

		if (indent_body_declarations_compare_to_header) {
			this.scribe.indent();
		}
		
		formatTypeMembers(bodyDeclarations, true);
		
		if (indent_body_declarations_compare_to_header) {
			this.scribe.unIndent();
		}

		if (this.preferences.insert_new_line_in_empty_type_declaration) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(TerminalTokens.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (class_declaration_brace.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}
		if (hasComments()) {
			this.scribe.printNewLine();
		}
		return false;
	}

	public boolean visit(TypeDeclarationStatement node) {
		node.getDeclaration().accept(this);
		return false;
	}

	public boolean visit(TypeLiteral node) {
		node.getType().accept(this);
		this.scribe.printNextToken(TerminalTokens.TokenNameDOT);
		this.scribe.printNextToken(TerminalTokens.TokenNameclass);
		return false;
	}

	public boolean visit(TypeParameter node) {
		node.getName().accept(this);
		final List bounds = node.typeBounds();
		final int boundsLength = bounds.size();
		if (boundsLength != 0) {
			this.scribe.printNextToken(TerminalTokens.TokenNameextends, true);
			this.scribe.space();
			((Type) bounds.get(0)).accept(this);
			if (boundsLength > 1) {
				this.scribe.printNextToken(TerminalTokens.TokenNameAND, this.preferences.insert_space_before_and_in_type_parameter);
				if (this.preferences.insert_space_after_and_in_type_parameter) {
					this.scribe.space();
				}
				for (int i = 1; i < boundsLength - 1; i++) {
					((Type) bounds.get(i)).accept(this);
					this.scribe.printNextToken(TerminalTokens.TokenNameAND, this.preferences.insert_space_before_and_in_type_parameter);
					if (this.preferences.insert_space_after_and_in_type_parameter) {
						this.scribe.space();
					}
				}
				((Type) bounds.get(boundsLength - 1)).accept(this);
			}
		}
		return false;
	}

	public boolean visit(VariableDeclarationExpression node) {
		formatLocalDeclaration(node, this.preferences.insert_space_before_comma_in_multiple_local_declarations, this.preferences.insert_space_after_comma_in_multiple_local_declarations);
		return false;
	}

	public boolean visit(VariableDeclarationStatement node) {
		final List modifiers = node.modifiers();
		if (modifiers.size() != 0) {
			this.scribe.printModifiers(modifiers, this);
			this.scribe.space();
		}

		node.getType().accept(this);
		
		formatVariableDeclarationFragments(node.fragments(), this.preferences.insert_space_before_comma_in_multiple_local_declarations, this.preferences.insert_space_after_comma_in_multiple_local_declarations);
		this.scribe.printNextToken(TerminalTokens.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingComment();
		return false;
	}

	public boolean visit(WhileStatement node) {
		this.scribe.printNextToken(TerminalTokens.TokenNamewhile);
		final int line = this.scribe.line;
		this.scribe.printNextToken(TerminalTokens.TokenNameLPAREN, this.preferences.insert_space_before_opening_paren_in_while);
		
		if (this.preferences.insert_space_after_opening_paren_in_while) {
			this.scribe.space();
		}
		node.getExpression().accept(this);
		
		this.scribe.printNextToken(TerminalTokens.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_while);
		
		formatAction(line, node.getBody(),false);
		return false;
	}

	public boolean visit(WildcardType node) {
		this.scribe.printNextToken(TerminalTokens.TokenNameQUESTION, this.preferences.insert_space_before_question_in_wilcard);
		final Type bound = node.getBound();
		if (bound != null) {
			if (node.isUpperBound()) {
				this.scribe.printNextToken(TerminalTokens.TokenNameextends, true);				
			} else {
				this.scribe.printNextToken(TerminalTokens.TokenNamesuper, true);				
			}
			this.scribe.space();
			bound.accept(this);
		} else if (this.preferences.insert_space_after_question_in_wilcard) {
			this.scribe.space();
		}
		return false;
	}
	
}
