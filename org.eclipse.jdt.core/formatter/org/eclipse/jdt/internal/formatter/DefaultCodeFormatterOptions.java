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
package org.eclipse.jdt.internal.formatter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.formatter.align.Alignment;

/**
 * This is still subject to changes before 3.0.
 * @since 3.0
 */

public class DefaultCodeFormatterOptions {
	public static final char DASH = ' ';//183
	public static final int DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final String DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final String DEFAULT_ARRAY_INITIALIZER_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_ARRAY_INITIALIZER_CONTINUATION_INDENTATION = 2; // 2 indentations
	public static final int DEFAULT_BINARY_EXPRESSION_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_BLANK_LINES_AFTER_IMPORTS = 0;
	public static final int DEFAULT_BLANK_LINES_AFTER_PACKAGE = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_FIELD = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_IMPORTS = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_METHOD = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_PACKAGE = 0;
	public static final String DEFAULT_BLOCK_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final boolean DEFAULT_COMPACT_ELSE_IF = true;
	public static final int DEFAULT_COMPACT_IF_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_INDENT_BY_ONE;
	public static final int DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT;
	public static final int DEFAULT_CONTINUATION_INDENTATION = 2; // 2 indentations
	// TODO remove before 3.0
	/**
	 * @deprecated
	 */
	public static final boolean DEFAULT_CONVERT_OLD_TO_NEW = true;
	public static final int DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final char DEFAULT_FILLING_SPACE = DASH;
	public static final boolean DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE = false;
	public static final boolean DEFAULT_INDENT_BLOCK_STATEMENTS = true;
	public static final boolean DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER = true;
	public static final boolean DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES = true;
	public static final boolean DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = true;
	public static final boolean DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS = false;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_BLOCK = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_BINARY_OPERATOR = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COLON_IN_ASSERT = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_POSTFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_PREFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_UNARY_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE = true; 
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BINARY_OPERATOR = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CATCH_EXPRESSION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_ASSERT = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CASE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_FIRST_ARGUMENT = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_FIRST_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_FOR_PAREN = true;	
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_IF_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_MESSAGE_SEND = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_PREFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SEMICOLON = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SWITCH_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_UNARY_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_WHILE_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER = false;	
	public static final boolean DEFAULT_INSERT_SPACE_IN_CATCH_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_FOR_PARENS = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_IF_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_SWITCH_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_WHILE_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_WITHIN_MESSAGE_SEND = false;
	public static final boolean DEFAULT_KEEP_ELSE_STATEMENT_ON_SAME_LINE = false;
	public static final boolean DEFAULT_KEEP_SIMPLE_IF_ON_ONE_LINE = false; 
	public static final boolean DEFAULT_KEEP_THEN_STATEMENT_ON_SAME_LINE = false;
	public static final int DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final String DEFAULT_METHOD_DECLARATION_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_MULTIPLE_FIELDS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;//$NON-NLS-1$
	public static final int DEFAULT_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY = 0;
	public static final int DEFAULT_NUMBER_OF_EMPTY_LINES_TO_PRESERVE = 0;
	
	public static final int DEFAULT_PAGE_WIDTH = 80;
	public static final boolean DEFAULT_PRESERVE_USER_LINEBREAKS = false;
	public static final boolean DEFAULT_PUT_EMPTY_STATEMENT_ON_NEW_LINE = false;
	public static final int DEFAULT_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final String DEFAULT_SWITCH_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_TAB_SIZE = 4;
	public static final String DEFAULT_TYPE_DECLARATION_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT = Alignment.M_NEXT_SHIFTED_SPLIT;
	public static final int DEFAULT_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT = Alignment.M_NEXT_SHIFTED_SPLIT;
	public static final int DEFAULT_TYPE_MEMBER_ALIGNMENT = Alignment.M_NO_ALIGNMENT;
	public static final boolean DEFAULT_USE_TAB = true; // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49081

	public static DefaultCodeFormatterOptions getDefaultSettings() {
		DefaultCodeFormatterOptions options = new DefaultCodeFormatterOptions();
		options.setDefaultSettings();
		return options;
	}
	
	public static DefaultCodeFormatterOptions getJavaConventionsSettings() {
		DefaultCodeFormatterOptions options = new DefaultCodeFormatterOptions();
		options.setJavaConventionsSettings();
		return options;
	}

	public int allocation_expression_arguments_alignment;
	public String anonymous_type_declaration_brace_position;
	public String array_initializer_brace_position;
	public int array_initializer_continuation_indentation;
	public int array_initializer_expressions_alignment;
	public int binary_expression_alignment;
	public int blank_lines_after_imports;
	public int blank_lines_after_package;
	public int blank_lines_before_field;
	public int blank_lines_before_first_class_body_declaration;
	public int blank_lines_before_imports;
	public int blank_lines_before_member_type;
	public int blank_lines_before_method;
	public int blank_lines_before_new_chunk;
	public int blank_lines_before_package;
	public String block_brace_position;
	public boolean compact_else_if;
	public int compact_if_alignment;
	public int conditional_expression_alignment;
	public int continuation_indentation;
	public int explicit_constructor_arguments_alignment;
	public char filling_space; 	// TODO remove when testing is over
	public boolean format_guardian_clause_on_one_line;
	public boolean indent_block_statements;
	public boolean indent_body_declarations_compare_to_type_header;
	public boolean indent_breaks_compare_to_cases;
	public boolean indent_switchstatements_compare_to_cases;
	public boolean indent_switchstatements_compare_to_switch;
	public int initial_indentation_level;
	public boolean insert_new_line_after_opening_brace_in_array_initializer;
	public boolean insert_new_line_before_closing_brace_in_array_initializer;
	public boolean insert_new_line_in_control_statements;
	public boolean insert_new_line_in_empty_anonymous_type_declaration;
	public boolean insert_new_line_in_empty_block;
	public boolean insert_new_line_in_empty_method_body;
	public boolean insert_new_line_in_empty_type_declaration;
	public boolean insert_space_after_assignment_operators;
	public boolean insert_space_after_binary_operator;
	public boolean insert_space_after_block_close_brace;
	public boolean insert_space_after_closing_paren_in_cast;
	public boolean insert_space_after_colon_in_assert;
	public boolean insert_space_after_colon_in_conditional;
	public boolean insert_space_after_colon_in_labeled_statement;
	public boolean insert_space_after_comma_in_allocation_expression;
	public boolean insert_space_after_comma_in_array_initializer;
	public boolean insert_space_after_comma_in_constructor_arguments;
	public boolean insert_space_after_comma_in_constructor_throws;
	public boolean insert_space_after_comma_in_explicitconstructorcall_arguments;
	public boolean insert_space_after_comma_in_for_increments;
	public boolean insert_space_after_comma_in_for_inits;
	public boolean insert_space_after_comma_in_messagesend_arguments;
	public boolean insert_space_after_comma_in_method_arguments;
	public boolean insert_space_after_comma_in_method_throws;
	public boolean insert_space_after_comma_in_multiple_field_declarations;
	public boolean insert_space_after_comma_in_multiple_local_declarations;
	public boolean insert_space_after_comma_in_superinterfaces;
	public boolean insert_space_after_open_paren_in_parenthesized_expression;
	public boolean insert_space_after_opening_paren_in_cast;
	public boolean insert_space_after_postfix_operator;
	public boolean insert_space_after_prefix_operator;
	public boolean insert_space_after_question_in_conditional;
	public boolean insert_space_after_semicolon_in_for;
	public boolean insert_space_after_unary_operator;
	public boolean insert_space_before_anonymous_type_open_brace;
	public boolean insert_space_before_assignment_operators;
	public boolean insert_space_before_binary_operator;
	public boolean insert_space_before_block_open_brace;
	public boolean insert_space_before_bracket_in_array_reference;
	public boolean insert_space_before_bracket_in_array_type_reference;
	public boolean insert_space_before_catch_expression;
	public boolean insert_space_before_closing_brace_in_array_initializer;
	public boolean insert_space_before_closing_paren;
	public boolean insert_space_before_closing_paren_in_cast;
	public boolean insert_space_before_closing_paren_in_parenthesized_expression;
	public boolean insert_space_before_colon_in_assert;
	public boolean insert_space_before_colon_in_case;
	public boolean insert_space_before_colon_in_conditional;
	public boolean insert_space_before_colon_in_default;
	public boolean insert_space_before_colon_in_labeled_statement;
	public boolean insert_space_before_comma_in_allocation_expression;
	public boolean insert_space_before_comma_in_array_initializer;
	public boolean insert_space_before_comma_in_constructor_arguments;
	public boolean insert_space_before_comma_in_constructor_throws;
	public boolean insert_space_before_comma_in_explicitconstructorcall_arguments;
	public boolean insert_space_before_comma_in_for_increments;
	public boolean insert_space_before_comma_in_for_inits;
	public boolean insert_space_before_comma_in_messagesend_arguments;
	public boolean insert_space_before_comma_in_method_arguments;
	public boolean insert_space_before_comma_in_method_throws;
	public boolean insert_space_before_comma_in_multiple_field_declarations;
	public boolean insert_space_before_comma_in_multiple_local_declarations;
	public boolean insert_space_before_comma_in_superinterfaces;
	public boolean insert_space_before_first_argument;
	public boolean insert_space_before_first_initializer;
	public boolean insert_space_before_for_paren;
	public boolean insert_space_before_if_condition;
	public boolean insert_space_before_message_send;
	public boolean insert_space_before_method_declaration_open_paren;
	public boolean insert_space_before_method_open_brace;
	public boolean insert_space_before_open_paren_in_parenthesized_expression;
	public boolean insert_space_before_opening_brace_in_array_initializer;
	public boolean insert_space_before_postfix_operator;
	public boolean insert_space_before_prefix_operator;
	public boolean insert_space_before_question_in_conditional;
	public boolean insert_space_before_semicolon;
	public boolean insert_space_before_switch_condition;
	public boolean insert_space_before_switch_open_brace;
	public boolean insert_space_before_synchronized_condition;
	public boolean insert_space_before_type_open_brace;
	public boolean insert_space_before_unary_operator;
	public boolean insert_space_before_while_condition;
	public boolean insert_space_between_brackets_in_array_reference;
	public boolean insert_space_between_brackets_in_array_type_reference;
	public boolean insert_space_between_empty_arguments;
	public boolean insert_space_between_empty_array_initializer;
	public boolean insert_space_in_catch_expression;
	public boolean insert_space_in_for_parens;
	public boolean insert_space_in_if_condition;
	public boolean insert_space_in_switch_condition;
	public boolean insert_space_in_synchronized_condition;
	public boolean insert_space_in_while_condition;
	public boolean insert_space_within_message_send;
	public boolean keep_else_statement_on_same_line;
	public boolean keep_simple_if_on_one_line;
	public boolean keep_then_statement_on_same_line;
	public String line_separator;
	public int message_send_arguments_alignment;
	public int message_send_selector_alignment;
	public int method_declaration_arguments_alignment;
	public String method_declaration_brace_position;
	public int method_throws_clause_alignment;
	public int multiple_fields_alignment;
	public int number_of_blank_lines_to_insert_at_beginning_of_method_body;
	public int number_of_empty_lines_to_preserve;
	public int page_width;
	public boolean preserve_user_linebreaks;
	public boolean put_empty_statement_on_new_line; 
	public int qualified_allocation_expression_arguments_alignment;
	public String switch_brace_position;
	public int tab_size;
	public String type_declaration_brace_position;
	public int type_declaration_superclass_alignment;
	public int type_declaration_superinterfaces_alignment;
	public int type_member_alignment;
	public boolean use_tab;
	
	private DefaultCodeFormatterOptions() {
		// cannot be instantiated
	}

	public DefaultCodeFormatterOptions(Map settings) {
		setDefaultSettings();
		if (settings == null) return;
		set(settings);
	}

	private String getAlignment(int alignment) {
		return Integer.toString(alignment);
	}

	public Map getMap() {
		Map options = new HashMap();
		options.put(DefaultCodeFormatterConstants.FORMATTER_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT, getAlignment(this.allocation_expression_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, this.anonymous_type_declaration_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, this.array_initializer_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, Integer.toString(this.array_initializer_continuation_indentation));
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT, getAlignment(this.array_initializer_expressions_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BINARY_EXPRESSION_ALIGNMENT, getAlignment(this.binary_expression_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_IMPORTS, Integer.toString(this.blank_lines_after_imports));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE, Integer.toString(this.blank_lines_after_package));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIELD, Integer.toString(this.blank_lines_before_field));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, Integer.toString(this.blank_lines_before_first_class_body_declaration));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_IMPORTS, Integer.toString(this.blank_lines_before_imports));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE, Integer.toString(this.blank_lines_before_member_type));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, Integer.toString(this.blank_lines_before_method));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK, Integer.toString(this.blank_lines_before_new_chunk));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE, Integer.toString(this.blank_lines_before_package));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, this.block_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMPACT_ELSE_IF, this.compact_else_if ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMPACT_IF_ALIGNMENT, getAlignment(this.compact_if_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT, getAlignment(this.conditional_expression_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION, Integer.toString(this.continuation_indentation));
		options.put(DefaultCodeFormatterConstants.FORMATTER_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT, getAlignment(this.explicit_constructor_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_FILLING_SPACE, String.valueOf(this.filling_space));
		options.put(DefaultCodeFormatterConstants.FORMATTER_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE, this.format_guardian_clause_on_one_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BLOCK_STATEMENTS, this.indent_block_statements ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER, this.indent_body_declarations_compare_to_type_header ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES, this.indent_breaks_compare_to_cases ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, this.indent_switchstatements_compare_to_cases ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, this.indent_switchstatements_compare_to_switch ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, this.insert_new_line_after_opening_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, this.insert_new_line_before_closing_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, this.insert_new_line_in_control_statements ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION, this.insert_new_line_in_empty_anonymous_type_declaration ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK, this.insert_new_line_in_empty_block ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY, this.insert_new_line_in_empty_method_body ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION, this.insert_new_line_in_empty_type_declaration ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS, this.insert_space_after_assignment_operators ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR, this.insert_space_after_binary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE, this.insert_space_after_block_close_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST, this.insert_space_after_closing_paren_in_cast ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT, this.insert_space_after_colon_in_assert ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL, this.insert_space_after_colon_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT, this.insert_space_after_colon_in_labeled_statement ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION, this.insert_space_after_comma_in_allocation_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, this.insert_space_after_comma_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS, this.insert_space_after_comma_in_constructor_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS, this.insert_space_after_comma_in_constructor_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS, this.insert_space_after_comma_in_explicitconstructorcall_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS, this.insert_space_after_comma_in_for_increments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS, this.insert_space_after_comma_in_for_inits ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS, this.insert_space_after_comma_in_messagesend_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS, this.insert_space_after_comma_in_method_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS, this.insert_space_after_comma_in_method_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS, this.insert_space_after_comma_in_multiple_field_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS, this.insert_space_after_comma_in_multiple_local_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES, this.insert_space_after_comma_in_superinterfaces ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION, this.insert_space_after_open_paren_in_parenthesized_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST, this.insert_space_after_opening_paren_in_cast ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR, this.insert_space_after_postfix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR, this.insert_space_after_prefix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL, this.insert_space_after_question_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR, this.insert_space_after_semicolon_in_for ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR, this.insert_space_after_unary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE, this.insert_space_before_anonymous_type_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS, this.insert_space_before_assignment_operators ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR, this.insert_space_before_binary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE, this.insert_space_before_block_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE, this.insert_space_before_bracket_in_array_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE, this.insert_space_before_bracket_in_array_type_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CATCH_EXPRESSION, this.insert_space_before_catch_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, this.insert_space_before_closing_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN, this.insert_space_before_closing_paren ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST, this.insert_space_before_closing_paren_in_cast ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION, this.insert_space_before_closing_paren_in_parenthesized_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT, this.insert_space_before_colon_in_assert ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE, this.insert_space_before_colon_in_case ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL, this.insert_space_before_colon_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT, this.insert_space_before_colon_in_default ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT, this.insert_space_before_colon_in_labeled_statement ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION, this.insert_space_before_comma_in_allocation_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER, this.insert_space_before_comma_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS, this.insert_space_before_comma_in_constructor_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS, this.insert_space_before_comma_in_constructor_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS, this.insert_space_before_comma_in_explicitconstructorcall_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS, this.insert_space_before_comma_in_for_increments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS, this.insert_space_before_comma_in_for_inits ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS, this.insert_space_before_comma_in_messagesend_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS, this.insert_space_before_comma_in_method_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS, this.insert_space_before_comma_in_method_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS, this.insert_space_before_comma_in_multiple_field_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS, this.insert_space_before_comma_in_multiple_local_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES, this.insert_space_before_comma_in_superinterfaces ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_ARGUMENT, this.insert_space_before_first_argument ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_INITIALIZER, this.insert_space_before_first_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FOR_PAREN, this.insert_space_before_for_paren ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_IF_CONDITION, this.insert_space_before_if_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_MESSAGE_SEND, this.insert_space_before_message_send ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN, this.insert_space_before_method_declaration_open_paren ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE, this.insert_space_before_method_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION, this.insert_space_before_open_paren_in_parenthesized_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER, this.insert_space_before_opening_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR, this.insert_space_before_postfix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR, this.insert_space_before_prefix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL, this.insert_space_before_question_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, this.insert_space_before_semicolon ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_CONDITION, this.insert_space_before_switch_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE, this.insert_space_before_switch_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION, this.insert_space_before_synchronized_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE, this.insert_space_before_type_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR, this.insert_space_before_unary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_WHILE_CONDITION, this.insert_space_before_while_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE, this.insert_space_between_brackets_in_array_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE, this.insert_space_between_brackets_in_array_type_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS, this.insert_space_between_empty_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER, this.insert_space_between_empty_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_CATCH_EXPRESSION, this.insert_space_in_catch_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_FOR_PARENS, this.insert_space_in_for_parens ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_IF_CONDITION, this.insert_space_in_if_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SWITCH_CONDITION, this.insert_space_in_switch_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION, this.insert_space_in_synchronized_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_WHILE_CONDITION, this.insert_space_in_while_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND, this.insert_space_within_message_send ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE, this.keep_else_statement_on_same_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE, this.keep_simple_if_on_one_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE, this.keep_then_statement_on_same_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, Integer.toString(this.page_width));
		options.put(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_ARGUMENTS_ALIGNMENT, getAlignment(this.message_send_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_SELECTOR_ALIGNMENT, getAlignment(this.message_send_selector_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT, getAlignment(this.method_declaration_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, this.method_declaration_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_THROWS_CLAUSE_ALIGNMENT, getAlignment(this.method_throws_clause_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_MULTIPLE_FIELDS_ALIGNMENT, getAlignment(this.multiple_fields_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY, Integer.toString(number_of_blank_lines_to_insert_at_beginning_of_method_body));
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, Integer.toString(this.number_of_empty_lines_to_preserve));
		options.put(DefaultCodeFormatterConstants.FORMATTER_PRESERVE_USER_LINEBREAKS, this.preserve_user_linebreaks ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, this.put_empty_statement_on_new_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT, getAlignment(this.qualified_allocation_expression_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, this.switch_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, this.use_tab ? JavaCore.TAB: JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, Integer.toString(this.tab_size));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, this.type_declaration_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT, getAlignment(this.type_declaration_superclass_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT, getAlignment(this.type_declaration_superinterfaces_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_MEMBER_ALIGNMENT, getAlignment(this.type_member_alignment));
		return options;
	}

	public void set(Map settings) {
		final Object allocationExpressionArgumentsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT);
		if (allocationExpressionArgumentsAlignmentOption != null) {
			this.allocation_expression_arguments_alignment = Integer.parseInt((String) allocationExpressionArgumentsAlignmentOption);
		}
		final Object anonymousTypeDeclarationBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION);
		if (anonymousTypeDeclarationBracePositionOption != null) {
			this.anonymous_type_declaration_brace_position = (String) anonymousTypeDeclarationBracePositionOption;
		}
		final Object arrayInitializerBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION);
		if (arrayInitializerBracePositionOption != null) {
			this.array_initializer_brace_position = (String) arrayInitializerBracePositionOption;
		}
		final Object arrayInitializerContinuationIndentationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION);
		if (arrayInitializerContinuationIndentationOption != null) {
			this.array_initializer_continuation_indentation = Integer.parseInt((String) arrayInitializerContinuationIndentationOption);
		}
		final Object arrayInitializerExpressionsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT);
		if (arrayInitializerExpressionsAlignmentOption != null) {
			this.array_initializer_expressions_alignment = Integer.parseInt((String) arrayInitializerExpressionsAlignmentOption);
		}
		final Object binaryExpressionAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BINARY_EXPRESSION_ALIGNMENT);
		if (binaryExpressionAlignmentOption != null) {
			this.binary_expression_alignment = Integer.parseInt((String) binaryExpressionAlignmentOption);
		}
		final Object blankLinesAfterImportsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_IMPORTS);
		if (blankLinesAfterImportsOption != null) {
			this.blank_lines_after_imports = Integer.parseInt((String) blankLinesAfterImportsOption);
		}
		final Object blankLinesAfterPackageOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE);
		if (blankLinesAfterPackageOption != null) {
			this.blank_lines_after_package = Integer.parseInt((String) blankLinesAfterPackageOption);
		}
		final Object blankLinesBeforeFieldOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIELD);
		if (blankLinesBeforeFieldOption != null) {
			this.blank_lines_before_field = Integer.parseInt((String) blankLinesBeforeFieldOption);
		}
		final Object blankLinesBeforeFirstClassBodyDeclarationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION);
		if (blankLinesBeforeFirstClassBodyDeclarationOption != null) {
			this.blank_lines_before_first_class_body_declaration = Integer.parseInt((String) blankLinesBeforeFirstClassBodyDeclarationOption);
		}
		final Object blankLinesBeforeImportsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_IMPORTS);
		if (blankLinesBeforeImportsOption != null) {
			this.blank_lines_before_imports = Integer.parseInt((String) blankLinesBeforeImportsOption);
		}
		final Object blankLinesBeforeMemberTypeOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE);
		if (blankLinesBeforeMemberTypeOption != null) {
			this.blank_lines_before_member_type = Integer.parseInt((String) blankLinesBeforeMemberTypeOption);
		}
		final Object blankLinesBeforeMethodOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD);
		if (blankLinesBeforeMethodOption != null) {
			this.blank_lines_before_method = Integer.parseInt((String) blankLinesBeforeMethodOption);
		}
		final Object blankLinesBeforeNewChunkOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK);
		if (blankLinesBeforeNewChunkOption != null) {
			this.blank_lines_before_new_chunk = Integer.parseInt((String) blankLinesBeforeNewChunkOption);
		}
		final Object blankLinesBeforePackageOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE);
		if (blankLinesBeforePackageOption != null) {
			this.blank_lines_before_package = Integer.parseInt((String) blankLinesBeforePackageOption);
		}
		final Object blockBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION);
		if (blockBracePositionOption != null) {
			this.block_brace_position = (String) blockBracePositionOption;
		}
		final Object compactElseIfOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_COMPACT_ELSE_IF);
		if (compactElseIfOption != null) {
			this.compact_else_if = DefaultCodeFormatterConstants.TRUE.equals(compactElseIfOption);
		}
		final Object compactIfAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_COMPACT_IF_ALIGNMENT);
		if (compactIfAlignmentOption != null) {
			this.compact_if_alignment = Integer.parseInt((String) compactIfAlignmentOption);
		}
		final Object conditionalExpressionAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT);
		if (conditionalExpressionAlignmentOption != null) {
			this.conditional_expression_alignment = Integer.parseInt((String) conditionalExpressionAlignmentOption);
		}
		final Object continuationIndentationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION);
		if (continuationIndentationOption != null) {
			this.continuation_indentation = Integer.parseInt((String) continuationIndentationOption);
		}
		final Object explicitConstructorArgumentsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT);
		if (explicitConstructorArgumentsAlignmentOption != null) {
			this.explicit_constructor_arguments_alignment = Integer.parseInt((String) explicitConstructorArgumentsAlignmentOption);
		}
		final Object fillingSpaceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_FILLING_SPACE);
		if (fillingSpaceOption != null) {
			String fillingSpaceOptionValue = (String) fillingSpaceOption;
			if (fillingSpaceOptionValue.length() >= 1) {
				this.filling_space = fillingSpaceOptionValue.charAt(0);
			}
		}
		final Object formatGuardianClauseOnOneLineOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE);
		if (formatGuardianClauseOnOneLineOption != null) {
			this.format_guardian_clause_on_one_line = DefaultCodeFormatterConstants.TRUE.equals(formatGuardianClauseOnOneLineOption);
		}
		final Object indentBlockStatementsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BLOCK_STATEMENTS);
		if (indentBlockStatementsOption != null) {
			this.indent_block_statements = DefaultCodeFormatterConstants.TRUE.equals(indentBlockStatementsOption);
		}
		final Object indentBodyDeclarationsCompareToTypeHeaderOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER);
		if (indentBodyDeclarationsCompareToTypeHeaderOption != null) {
			this.indent_body_declarations_compare_to_type_header = DefaultCodeFormatterConstants.TRUE.equals(indentBodyDeclarationsCompareToTypeHeaderOption);
		}
		final Object indentBreaksCompareToCasesOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES);
		if (indentBreaksCompareToCasesOption != null) {
			this.indent_breaks_compare_to_cases = DefaultCodeFormatterConstants.TRUE.equals(indentBreaksCompareToCasesOption);
		}
		final Object indentSwitchstatementsCompareToCasesOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES);
		if (indentSwitchstatementsCompareToCasesOption != null) {
			this.indent_switchstatements_compare_to_cases = DefaultCodeFormatterConstants.TRUE.equals(indentSwitchstatementsCompareToCasesOption);
		}
		final Object indentSwitchstatementsCompareToSwitchOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH);
		if (indentSwitchstatementsCompareToSwitchOption != null) {
			this.indent_switchstatements_compare_to_switch = DefaultCodeFormatterConstants.TRUE.equals(indentSwitchstatementsCompareToSwitchOption);
		}
		final Object insertNewLineAfterOpeningBraceInArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER);
		if (insertNewLineAfterOpeningBraceInArrayInitializerOption != null) {
			this.insert_new_line_after_opening_brace_in_array_initializer = JavaCore.INSERT.equals(insertNewLineAfterOpeningBraceInArrayInitializerOption);
		}
		final Object insertNewLineBeforeClosingBraceInArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER);
		if (insertNewLineBeforeClosingBraceInArrayInitializerOption != null) {
			this.insert_new_line_before_closing_brace_in_array_initializer = JavaCore.INSERT.equals(insertNewLineBeforeClosingBraceInArrayInitializerOption);
		}
		final Object insertNewLineInControlStatementsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS);
		if (insertNewLineInControlStatementsOption != null) {
			this.insert_new_line_in_control_statements = JavaCore.INSERT.equals(insertNewLineInControlStatementsOption);
		}
		final Object insertNewLineInEmptyAnonymousTypeDeclarationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION);
		if (insertNewLineInEmptyAnonymousTypeDeclarationOption != null) {
			this.insert_new_line_in_empty_anonymous_type_declaration = JavaCore.INSERT.equals(insertNewLineInEmptyAnonymousTypeDeclarationOption);
		}
		final Object insertNewLineInEmptyBlockOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK);
		if (insertNewLineInEmptyBlockOption != null) {
			this.insert_new_line_in_empty_block = JavaCore.INSERT.equals(insertNewLineInEmptyBlockOption);
		}
		final Object insertNewLineInEmptyMethodBodyOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY);
		if (insertNewLineInEmptyMethodBodyOption != null) {
			this.insert_new_line_in_empty_method_body = JavaCore.INSERT.equals(insertNewLineInEmptyMethodBodyOption);
		}
		final Object insertNewLineInEmptyTypeDeclarationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION);
		if (insertNewLineInEmptyTypeDeclarationOption != null) {
			this.insert_new_line_in_empty_type_declaration = JavaCore.INSERT.equals(insertNewLineInEmptyTypeDeclarationOption);
		}
		final Object insertSpaceAfterAssignmentOperatorsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS);
		if (insertSpaceAfterAssignmentOperatorsOption != null) {
			this.insert_space_after_assignment_operators = JavaCore.INSERT.equals(insertSpaceAfterAssignmentOperatorsOption);
		}
		final Object insertSpaceAfterBinaryOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR);
		if (insertSpaceAfterBinaryOperatorOption != null) {
			this.insert_space_after_binary_operator = JavaCore.INSERT.equals(insertSpaceAfterBinaryOperatorOption);
		}
		final Object insertSpaceAfterBlockCloseBraceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE);
		if (insertSpaceAfterBlockCloseBraceOption != null) {
			this.insert_space_after_block_close_brace = JavaCore.INSERT.equals(insertSpaceAfterBlockCloseBraceOption);
		}
		final Object insertSpaceAfterClosingParenInCastOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST);
		if (insertSpaceAfterClosingParenInCastOption != null) {
			this.insert_space_after_closing_paren_in_cast = JavaCore.INSERT.equals(insertSpaceAfterClosingParenInCastOption);
		}
		final Object insertSpaceAfterColonInAssertOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT);
		if (insertSpaceAfterColonInAssertOption != null) {
			this.insert_space_after_colon_in_assert = JavaCore.INSERT.equals(insertSpaceAfterColonInAssertOption);
		}
		final Object insertSpaceAfterColonInConditionalOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL);
		if (insertSpaceAfterColonInConditionalOption != null) {
			this.insert_space_after_colon_in_conditional = JavaCore.INSERT.equals(insertSpaceAfterColonInConditionalOption);
		}
		final Object insertSpaceAfterColonInLabeledStatementOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT);
		if (insertSpaceAfterColonInLabeledStatementOption != null) {
			this.insert_space_after_colon_in_labeled_statement = JavaCore.INSERT.equals(insertSpaceAfterColonInLabeledStatementOption);
		}
		final Object insertSpaceAfterCommaInAllocationExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION);
		if (insertSpaceAfterCommaInAllocationExpressionOption != null) {
			this.insert_space_after_comma_in_allocation_expression = JavaCore.INSERT.equals(insertSpaceAfterCommaInAllocationExpressionOption);
		}
		final Object insertSpaceAfterCommaInArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER);
		if (insertSpaceAfterCommaInArrayInitializerOption != null) {
			this.insert_space_after_comma_in_array_initializer = JavaCore.INSERT.equals(insertSpaceAfterCommaInArrayInitializerOption);
		}
		final Object insertSpaceAfterCommaInConstructorArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS);
		if (insertSpaceAfterCommaInConstructorArgumentsOption != null) {
			this.insert_space_after_comma_in_constructor_arguments = JavaCore.INSERT.equals(insertSpaceAfterCommaInConstructorArgumentsOption);
		}
		final Object insertSpaceAfterCommaInConstructorThrowsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS);
		if (insertSpaceAfterCommaInConstructorThrowsOption != null) {
			this.insert_space_after_comma_in_constructor_throws = JavaCore.INSERT.equals(insertSpaceAfterCommaInConstructorThrowsOption);
		}
		final Object insertSpaceAfterCommaInExplicitconstructorcallArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS);
		if (insertSpaceAfterCommaInExplicitconstructorcallArgumentsOption != null) {
			this.insert_space_after_comma_in_explicitconstructorcall_arguments = JavaCore.INSERT.equals(insertSpaceAfterCommaInExplicitconstructorcallArgumentsOption);
		}
		final Object insertSpaceAfterCommaInForIncrementsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS);
		if (insertSpaceAfterCommaInForIncrementsOption != null) {
			this.insert_space_after_comma_in_for_increments = JavaCore.INSERT.equals(insertSpaceAfterCommaInForIncrementsOption);
		}
		final Object insertSpaceAfterCommaInForInitsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS);
		if (insertSpaceAfterCommaInForInitsOption != null) {
			this.insert_space_after_comma_in_for_inits = JavaCore.INSERT.equals(insertSpaceAfterCommaInForInitsOption);
		}
		final Object insertSpaceAfterCommaInMessagesendArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS);
		if (insertSpaceAfterCommaInMessagesendArgumentsOption != null) {
			this.insert_space_after_comma_in_messagesend_arguments = JavaCore.INSERT.equals(insertSpaceAfterCommaInMessagesendArgumentsOption);
		}
		final Object insertSpaceAfterCommaInMethodArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS);
		if (insertSpaceAfterCommaInMethodArgumentsOption != null) {
			this.insert_space_after_comma_in_method_arguments = JavaCore.INSERT.equals(insertSpaceAfterCommaInMethodArgumentsOption);
		}
		final Object insertSpaceAfterCommaInMethodThrowsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS);
		if (insertSpaceAfterCommaInMethodThrowsOption != null) {
			this.insert_space_after_comma_in_method_throws = JavaCore.INSERT.equals(insertSpaceAfterCommaInMethodThrowsOption);
		}
		final Object insertSpaceAfterCommaInMultipleFieldDeclarationsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS);
		if (insertSpaceAfterCommaInMultipleFieldDeclarationsOption != null) {
			this.insert_space_after_comma_in_multiple_field_declarations = JavaCore.INSERT.equals(insertSpaceAfterCommaInMultipleFieldDeclarationsOption);
		}
		final Object insertSpaceAfterCommaInMultipleLocalDeclarationsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS);
		if (insertSpaceAfterCommaInMultipleLocalDeclarationsOption != null) {
			this.insert_space_after_comma_in_multiple_local_declarations = JavaCore.INSERT.equals(insertSpaceAfterCommaInMultipleLocalDeclarationsOption);
		}
		final Object insertSpaceAfterCommaInSuperinterfacesOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES);
		if (insertSpaceAfterCommaInSuperinterfacesOption != null) {
			this.insert_space_after_comma_in_superinterfaces = JavaCore.INSERT.equals(insertSpaceAfterCommaInSuperinterfacesOption);
		}
		final Object insertSpaceAfterOpenParenInParenthesizedExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION);
		if (insertSpaceAfterOpenParenInParenthesizedExpressionOption != null) {
			this.insert_space_after_open_paren_in_parenthesized_expression = JavaCore.INSERT.equals(insertSpaceAfterOpenParenInParenthesizedExpressionOption);
		}
		final Object insertSpaceAfterOpeningParenInCastOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST);
		if (insertSpaceAfterOpeningParenInCastOption != null) {
			this.insert_space_after_opening_paren_in_cast = JavaCore.INSERT.equals(insertSpaceAfterOpeningParenInCastOption);
		}
		final Object insertSpaceAfterPostfixOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR);
		if (insertSpaceAfterPostfixOperatorOption != null) {
			this.insert_space_after_postfix_operator = JavaCore.INSERT.equals(insertSpaceAfterPostfixOperatorOption);
		}
		final Object insertSpaceAfterPrefixOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR);
		if (insertSpaceAfterPrefixOperatorOption != null) {
			this.insert_space_after_prefix_operator = JavaCore.INSERT.equals(insertSpaceAfterPrefixOperatorOption);
		}
		final Object insertSpaceAfterQuestionInConditionalOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL);
		if (insertSpaceAfterQuestionInConditionalOption != null) {
			this.insert_space_after_question_in_conditional = JavaCore.INSERT.equals(insertSpaceAfterQuestionInConditionalOption);
		}
		final Object insertSpaceAfterSemicolonInForOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR);
		if (insertSpaceAfterSemicolonInForOption != null) {
			this.insert_space_after_semicolon_in_for = JavaCore.INSERT.equals(insertSpaceAfterSemicolonInForOption);
		}
		final Object insertSpaceAfterUnaryOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR);
		if (insertSpaceAfterUnaryOperatorOption != null) {
			this.insert_space_after_unary_operator = JavaCore.INSERT.equals(insertSpaceAfterUnaryOperatorOption);
		}
		final Object insertSpaceBeforeAnonymousTypeOpenBraceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE);
		if (insertSpaceBeforeAnonymousTypeOpenBraceOption != null) {
			this.insert_space_before_anonymous_type_open_brace = JavaCore.INSERT.equals(insertSpaceBeforeAnonymousTypeOpenBraceOption);
		}
		final Object insertSpaceBeforeAssignmentOperatorsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS);
		if (insertSpaceBeforeAssignmentOperatorsOption != null) {
			this.insert_space_before_assignment_operators = JavaCore.INSERT.equals(insertSpaceBeforeAssignmentOperatorsOption);
		}
		final Object insertSpaceBeforeBinaryOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR);
		if (insertSpaceBeforeBinaryOperatorOption != null) {
			this.insert_space_before_binary_operator = JavaCore.INSERT.equals(insertSpaceBeforeBinaryOperatorOption);
		}
		final Object insertSpaceBeforeBlockOpenBraceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE);
		if (insertSpaceBeforeBlockOpenBraceOption != null) {
			this.insert_space_before_block_open_brace = JavaCore.INSERT.equals(insertSpaceBeforeBlockOpenBraceOption);
		}
		final Object insertSpaceBeforeBracketInArrayReferenceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE);
		if (insertSpaceBeforeBracketInArrayReferenceOption != null) {
			this.insert_space_before_bracket_in_array_reference = JavaCore.INSERT.equals(insertSpaceBeforeBracketInArrayReferenceOption);
		}
		final Object insertSpaceBeforeBracketInArrayTypeReferenceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE);
		if (insertSpaceBeforeBracketInArrayTypeReferenceOption != null) {
			this.insert_space_before_bracket_in_array_type_reference = JavaCore.INSERT.equals(insertSpaceBeforeBracketInArrayTypeReferenceOption);
		}
		final Object insertSpaceBeforeCatchExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CATCH_EXPRESSION);
		if (insertSpaceBeforeCatchExpressionOption != null) {
			this.insert_space_before_catch_expression = JavaCore.INSERT.equals(insertSpaceBeforeCatchExpressionOption);
		}
		final Object insertSpaceBeforeClosingBraceInArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER);
		if (insertSpaceBeforeClosingBraceInArrayInitializerOption != null) {
			this.insert_space_before_closing_brace_in_array_initializer = JavaCore.INSERT.equals(insertSpaceBeforeClosingBraceInArrayInitializerOption);
		}
		final Object insertSpaceBeforeClosingParenOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN);
		if (insertSpaceBeforeClosingParenOption != null) {
			this.insert_space_before_closing_paren = JavaCore.INSERT.equals(insertSpaceBeforeClosingParenOption);
		}
		final Object insertSpaceBeforeClosingParenInCastOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST);
		if (insertSpaceBeforeClosingParenInCastOption != null) {
			this.insert_space_before_closing_paren_in_cast = JavaCore.INSERT.equals(insertSpaceBeforeClosingParenInCastOption);
		}
		final Object insertSpaceBeforeClosingParenInParenthesizedExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION);
		if (insertSpaceBeforeClosingParenInParenthesizedExpressionOption != null) {
			this.insert_space_before_closing_paren_in_parenthesized_expression = JavaCore.INSERT.equals(insertSpaceBeforeClosingParenInParenthesizedExpressionOption);
		}
		final Object insertSpaceBeforeColonInAssertOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT);
		if (insertSpaceBeforeColonInAssertOption != null) {
			this.insert_space_before_colon_in_assert = JavaCore.INSERT.equals(insertSpaceBeforeColonInAssertOption);
		}
		final Object insertSpaceBeforeColonInCaseOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE);
		if (insertSpaceBeforeColonInCaseOption != null) {
			this.insert_space_before_colon_in_case = JavaCore.INSERT.equals(insertSpaceBeforeColonInCaseOption);
		}
		final Object insertSpaceBeforeColonInConditionalOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL);
		if (insertSpaceBeforeColonInConditionalOption != null) {
			this.insert_space_before_colon_in_conditional = JavaCore.INSERT.equals(insertSpaceBeforeColonInConditionalOption);
		}
		final Object insertSpaceBeforeColonInDefaultOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT);
		if (insertSpaceBeforeColonInDefaultOption != null) {
			this.insert_space_before_colon_in_default = JavaCore.INSERT.equals(insertSpaceBeforeColonInDefaultOption);
		}
		final Object insertSpaceBeforeColonInLabeledStatementOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT);
		if (insertSpaceBeforeColonInLabeledStatementOption != null) {
			this.insert_space_before_colon_in_labeled_statement = JavaCore.INSERT.equals(insertSpaceBeforeColonInLabeledStatementOption);
		}
		final Object insertSpaceBeforeCommaInAllocationExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION);
		if (insertSpaceBeforeCommaInAllocationExpressionOption != null) {
			this.insert_space_before_comma_in_allocation_expression = JavaCore.INSERT.equals(insertSpaceBeforeCommaInAllocationExpressionOption);
		}
		final Object insertSpaceBeforeCommaInArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER);
		if (insertSpaceBeforeCommaInArrayInitializerOption != null) {
			this.insert_space_before_comma_in_array_initializer = JavaCore.INSERT.equals(insertSpaceBeforeCommaInArrayInitializerOption);
		}
		final Object insertSpaceBeforeCommaInConstructorArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS);
		if (insertSpaceBeforeCommaInConstructorArgumentsOption != null) {
			this.insert_space_before_comma_in_constructor_arguments = JavaCore.INSERT.equals(insertSpaceBeforeCommaInConstructorArgumentsOption);
		}
		final Object insertSpaceBeforeCommaInConstructorThrowsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS);
		if (insertSpaceBeforeCommaInConstructorThrowsOption != null) {
			this.insert_space_before_comma_in_constructor_throws = JavaCore.INSERT.equals(insertSpaceBeforeCommaInConstructorThrowsOption);
		}
		final Object insertSpaceBeforeCommaInExplicitconstructorcallArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS);
		if (insertSpaceBeforeCommaInExplicitconstructorcallArgumentsOption != null) {
			this.insert_space_before_comma_in_explicitconstructorcall_arguments = JavaCore.INSERT.equals(insertSpaceBeforeCommaInExplicitconstructorcallArgumentsOption);
		}
		final Object insertSpaceBeforeCommaInForIncrementsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS);
		if (insertSpaceBeforeCommaInForIncrementsOption != null) {
			this.insert_space_before_comma_in_for_increments = JavaCore.INSERT.equals(insertSpaceBeforeCommaInForIncrementsOption);
		}
		final Object insertSpaceBeforeCommaInForInitsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS);
		if (insertSpaceBeforeCommaInForInitsOption != null) {
			this.insert_space_before_comma_in_for_inits = JavaCore.INSERT.equals(insertSpaceBeforeCommaInForInitsOption);
		}
		final Object insertSpaceBeforeCommaInMessagesendArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS);
		if (insertSpaceBeforeCommaInMessagesendArgumentsOption != null) {
			this.insert_space_before_comma_in_messagesend_arguments = JavaCore.INSERT.equals(insertSpaceBeforeCommaInMessagesendArgumentsOption);
		}
		final Object insertSpaceBeforeCommaInMethodArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS);
		if (insertSpaceBeforeCommaInMethodArgumentsOption != null) {
			this.insert_space_before_comma_in_method_arguments = JavaCore.INSERT.equals(insertSpaceBeforeCommaInMethodArgumentsOption);
		}
		final Object insertSpaceBeforeCommaInMethodThrowsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS);
		if (insertSpaceBeforeCommaInMethodThrowsOption != null) {
			this.insert_space_before_comma_in_method_throws = JavaCore.INSERT.equals(insertSpaceBeforeCommaInMethodThrowsOption);
		}
		final Object insertSpaceBeforeCommaInMultipleFieldDeclarationsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS);
		if (insertSpaceBeforeCommaInMultipleFieldDeclarationsOption != null) {
			this.insert_space_before_comma_in_multiple_field_declarations = JavaCore.INSERT.equals(insertSpaceBeforeCommaInMultipleFieldDeclarationsOption);
		}
		final Object insertSpaceBeforeCommaInMultipleLocalDeclarationsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS);
		if (insertSpaceBeforeCommaInMultipleLocalDeclarationsOption != null) {
			this.insert_space_before_comma_in_multiple_local_declarations = JavaCore.INSERT.equals(insertSpaceBeforeCommaInMultipleLocalDeclarationsOption);
		}
		final Object insertSpaceBeforeCommaInSuperinterfacesOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES);
		if (insertSpaceBeforeCommaInSuperinterfacesOption != null) {
			this.insert_space_before_comma_in_superinterfaces = JavaCore.INSERT.equals(insertSpaceBeforeCommaInSuperinterfacesOption);
		}
		final Object insertSpaceBeforeFirstArgumentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_ARGUMENT);
		if (insertSpaceBeforeFirstArgumentOption != null) {
			this.insert_space_before_first_argument = JavaCore.INSERT.equals(insertSpaceBeforeFirstArgumentOption);
		}
		final Object insertSpaceBeforeFirstInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_INITIALIZER);
		if (insertSpaceBeforeFirstInitializerOption != null) {
			this.insert_space_before_first_initializer = JavaCore.INSERT.equals(insertSpaceBeforeFirstInitializerOption);
		}
		final Object insertSpaceBeforeForParenOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FOR_PAREN);
		if (insertSpaceBeforeForParenOption != null) {
			this.insert_space_before_for_paren = JavaCore.INSERT.equals(insertSpaceBeforeForParenOption);
		}
		final Object insertSpaceBeforeIfConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_IF_CONDITION);
		if (insertSpaceBeforeIfConditionOption != null) {
			this.insert_space_before_if_condition = JavaCore.INSERT.equals(insertSpaceBeforeIfConditionOption);
		}
		final Object insertSpaceBeforeMessageSendOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_MESSAGE_SEND);
		if (insertSpaceBeforeMessageSendOption != null) {
			this.insert_space_before_message_send = JavaCore.INSERT.equals(insertSpaceBeforeMessageSendOption);
		}
		final Object insertSpaceBeforeMethodDeclarationOpenParenOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN);
		if (insertSpaceBeforeMethodDeclarationOpenParenOption != null) {
			this.insert_space_before_method_declaration_open_paren = JavaCore.INSERT.equals(insertSpaceBeforeMethodDeclarationOpenParenOption);
		}
		final Object insertSpaceBeforeMethodOpenBraceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE);
		if (insertSpaceBeforeMethodOpenBraceOption != null) {
			this.insert_space_before_method_open_brace = JavaCore.INSERT.equals(insertSpaceBeforeMethodOpenBraceOption);
		}
		final Object insertSpaceBeforeOpenParenInParenthesizedExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION);
		if (insertSpaceBeforeOpenParenInParenthesizedExpressionOption != null) {
			this.insert_space_before_open_paren_in_parenthesized_expression = JavaCore.INSERT.equals(insertSpaceBeforeOpenParenInParenthesizedExpressionOption);
		}
		final Object insertSpaceBeforeOpeningBraceInArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER);
		if (insertSpaceBeforeOpeningBraceInArrayInitializerOption != null) {
			this.insert_space_before_opening_brace_in_array_initializer = JavaCore.INSERT.equals(insertSpaceBeforeOpeningBraceInArrayInitializerOption);
		}
		final Object insertSpaceBeforePostfixOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR);
		if (insertSpaceBeforePostfixOperatorOption != null) {
			this.insert_space_before_postfix_operator = JavaCore.INSERT.equals(insertSpaceBeforePostfixOperatorOption);
		}
		final Object insertSpaceBeforePrefixOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR);
		if (insertSpaceBeforePrefixOperatorOption != null) {
			this.insert_space_before_prefix_operator = JavaCore.INSERT.equals(insertSpaceBeforePrefixOperatorOption);
		}
		final Object insertSpaceBeforeQuestionInConditionalOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL);
		if (insertSpaceBeforeQuestionInConditionalOption != null) {
			this.insert_space_before_question_in_conditional = JavaCore.INSERT.equals(insertSpaceBeforeQuestionInConditionalOption);
		}
		final Object insertSpaceBeforeSemicolonOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON);
		if (insertSpaceBeforeSemicolonOption != null) {
			this.insert_space_before_semicolon = JavaCore.INSERT.equals(insertSpaceBeforeSemicolonOption);
		}
		final Object insertSpaceBeforeSwitchConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_CONDITION);
		if (insertSpaceBeforeSwitchConditionOption != null) {
			this.insert_space_before_switch_condition = JavaCore.INSERT.equals(insertSpaceBeforeSwitchConditionOption);
		}
		final Object insertSpaceBeforeSwitchOpenBraceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE);
		if (insertSpaceBeforeSwitchOpenBraceOption != null) {
			this.insert_space_before_switch_open_brace = JavaCore.INSERT.equals(insertSpaceBeforeSwitchOpenBraceOption);
		}
		final Object insertSpaceBeforeSynchronizedConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION);
		if (insertSpaceBeforeSynchronizedConditionOption != null) {
			this.insert_space_before_synchronized_condition = JavaCore.INSERT.equals(insertSpaceBeforeSynchronizedConditionOption);
		}
		final Object insertSpaceBeforeTypeOpenBraceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE);
		if (insertSpaceBeforeTypeOpenBraceOption != null) {
			this.insert_space_before_type_open_brace = JavaCore.INSERT.equals(insertSpaceBeforeTypeOpenBraceOption);
		}
		final Object insertSpaceBeforeUnaryOperatorOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR);
		if (insertSpaceBeforeUnaryOperatorOption != null) {
			this.insert_space_before_unary_operator = JavaCore.INSERT.equals(insertSpaceBeforeUnaryOperatorOption);
		}
		final Object insertSpaceBeforeWhileConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_WHILE_CONDITION);
		if (insertSpaceBeforeWhileConditionOption != null) {
			this.insert_space_before_while_condition = JavaCore.INSERT.equals(insertSpaceBeforeWhileConditionOption);
		}
		final Object insertSpaceBetweenBracketsInArrayReferenceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE);
		if (insertSpaceBetweenBracketsInArrayReferenceOption != null) {
			this.insert_space_between_brackets_in_array_reference = JavaCore.INSERT.equals(insertSpaceBetweenBracketsInArrayReferenceOption);
		}
		final Object insertSpaceBetweenBracketsInArrayTypeReferenceOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE);
		if (insertSpaceBetweenBracketsInArrayTypeReferenceOption != null) {
			this.insert_space_between_brackets_in_array_type_reference = JavaCore.INSERT.equals(insertSpaceBetweenBracketsInArrayTypeReferenceOption);
		}
		final Object insertSpaceBetweenEmptyArgumentsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS);
		if (insertSpaceBetweenEmptyArgumentsOption != null) {
			this.insert_space_between_empty_arguments = JavaCore.INSERT.equals(insertSpaceBetweenEmptyArgumentsOption);
		}
		final Object insertSpaceBetweenEmptyArrayInitializerOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER);
		if (insertSpaceBetweenEmptyArrayInitializerOption != null) {
			this.insert_space_between_empty_array_initializer = JavaCore.INSERT.equals(insertSpaceBetweenEmptyArrayInitializerOption);
		}
		final Object insertSpaceInCatchExpressionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_CATCH_EXPRESSION);
		if (insertSpaceInCatchExpressionOption != null) {
			this.insert_space_in_catch_expression = JavaCore.INSERT.equals(insertSpaceInCatchExpressionOption);
		}
		final Object insertSpaceInForParensOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_FOR_PARENS);
		if (insertSpaceInForParensOption != null) {
			this.insert_space_in_for_parens = JavaCore.INSERT.equals(insertSpaceInForParensOption);
		}
		final Object insertSpaceInIfConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_IF_CONDITION);
		if (insertSpaceInIfConditionOption != null) {
			this.insert_space_in_if_condition = JavaCore.INSERT.equals(insertSpaceInIfConditionOption);
		}
		final Object insertSpaceInSwitchConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SWITCH_CONDITION);
		if (insertSpaceInSwitchConditionOption != null) {
			this.insert_space_in_switch_condition = JavaCore.INSERT.equals(insertSpaceInSwitchConditionOption);
		}
		final Object insertSpaceInSynchronizedConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION);
		if (insertSpaceInSynchronizedConditionOption != null) {
			this.insert_space_in_synchronized_condition = JavaCore.INSERT.equals(insertSpaceInSynchronizedConditionOption);
		}
		final Object insertSpaceInWhileConditionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_WHILE_CONDITION);
		if (insertSpaceInWhileConditionOption != null) {
			this.insert_space_in_while_condition = JavaCore.INSERT.equals(insertSpaceInWhileConditionOption);
		}
		final Object insertSpaceWithinMessageSendOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND);
		if (insertSpaceWithinMessageSendOption != null) {
			this.insert_space_within_message_send = JavaCore.INSERT.equals(insertSpaceWithinMessageSendOption);
		}
		final Object keepElseStatementOnSameLineOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE);
		if (keepElseStatementOnSameLineOption != null) {
			this.keep_else_statement_on_same_line = DefaultCodeFormatterConstants.TRUE.equals(keepElseStatementOnSameLineOption);
		}
		final Object keepSimpleIfOnOneLineOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE);
		if (keepSimpleIfOnOneLineOption != null) {
			this.keep_simple_if_on_one_line = DefaultCodeFormatterConstants.TRUE.equals(keepSimpleIfOnOneLineOption);
		}
		final Object keepThenStatementOnSameLineOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE);
		if (keepThenStatementOnSameLineOption != null) {
			this.keep_then_statement_on_same_line = DefaultCodeFormatterConstants.TRUE.equals(keepThenStatementOnSameLineOption);
		}
		final Object messageSendArgumentsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_ARGUMENTS_ALIGNMENT);
		if (messageSendArgumentsAlignmentOption != null) {
			this.message_send_arguments_alignment = Integer.parseInt((String) messageSendArgumentsAlignmentOption);
		}
		final Object messageSendSelectorAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_SELECTOR_ALIGNMENT);
		if (messageSendSelectorAlignmentOption != null) {
			this.message_send_selector_alignment = Integer.parseInt((String) messageSendSelectorAlignmentOption);
		}
		final Object methodDeclarationArgumentsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT);
		if (methodDeclarationArgumentsAlignmentOption != null) {
			this.method_declaration_arguments_alignment = Integer.parseInt((String) methodDeclarationArgumentsAlignmentOption);
		}
		final Object methodDeclarationBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION);
		if (methodDeclarationBracePositionOption != null) {
			this.method_declaration_brace_position = (String) methodDeclarationBracePositionOption;
		}
		final Object methodThrowsClauseAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_THROWS_CLAUSE_ALIGNMENT);
		if (methodThrowsClauseAlignmentOption != null) {
			this.method_throws_clause_alignment = Integer.parseInt((String) methodThrowsClauseAlignmentOption);
		}
		final Object multipleFieldsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_MULTIPLE_FIELDS_ALIGNMENT);
		if (multipleFieldsAlignmentOption != null) {
			this.multiple_fields_alignment = Integer.parseInt((String) multipleFieldsAlignmentOption);
		}
		final Object numberOfBlankLinesToInsertAtBeginningOfMethodBodyOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY);
		if (numberOfBlankLinesToInsertAtBeginningOfMethodBodyOption != null) {
			this.number_of_blank_lines_to_insert_at_beginning_of_method_body = Integer.parseInt((String) numberOfBlankLinesToInsertAtBeginningOfMethodBodyOption);
		}
		final Object numberOfEmptyLinesToPreserveOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE);
		if (numberOfEmptyLinesToPreserveOption != null) {
			this.number_of_empty_lines_to_preserve = Integer.parseInt((String) numberOfEmptyLinesToPreserveOption);
		}
		final Object pageWidthOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT);
		if (pageWidthOption != null) {
			this.page_width = Integer.parseInt((String) pageWidthOption);
		}
		final Object preserveUserLinebreaksOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_PRESERVE_USER_LINEBREAKS);
		if (preserveUserLinebreaksOption != null) {
			this.preserve_user_linebreaks = DefaultCodeFormatterConstants.TRUE.equals(preserveUserLinebreaksOption);
		}
		final Object putEmptyStatementOnNewLineOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE);
		if (putEmptyStatementOnNewLineOption != null) {
			this.put_empty_statement_on_new_line = DefaultCodeFormatterConstants.TRUE.equals(putEmptyStatementOnNewLineOption);
		}
		final Object qualifiedAllocationExpressionArgumentsAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT);
		if (qualifiedAllocationExpressionArgumentsAlignmentOption != null) {
			this.qualified_allocation_expression_arguments_alignment = Integer.parseInt((String) qualifiedAllocationExpressionArgumentsAlignmentOption);
		}
		final Object switchBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION);
		if (switchBracePositionOption != null) {
			this.switch_brace_position = (String) switchBracePositionOption;
		}
		final Object tabSizeOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
		if (tabSizeOption != null) {
			this.tab_size = Integer.parseInt((String) tabSizeOption);
		}
		final Object typeDeclarationBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION);
		if (typeDeclarationBracePositionOption != null) {
			this.type_declaration_brace_position = (String) typeDeclarationBracePositionOption;
		}
		final Object typeDeclarationSuperclassAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT);
		if (typeDeclarationSuperclassAlignmentOption != null) {
			this.type_declaration_superclass_alignment = Integer.parseInt((String) typeDeclarationSuperclassAlignmentOption);
		}
		final Object typeDeclarationSuperinterfacesAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT);
		if (typeDeclarationSuperinterfacesAlignmentOption != null) {
			this.type_declaration_superinterfaces_alignment = Integer.parseInt((String) typeDeclarationSuperinterfacesAlignmentOption);
		}
		final Object typeMemberAlignmentOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_MEMBER_ALIGNMENT);
		if (typeMemberAlignmentOption != null) {
			this.type_member_alignment = Integer.parseInt((String) typeMemberAlignmentOption);
		}
		final Object useTabOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		if (useTabOption != null) {
			this.use_tab = JavaCore.TAB.equals(useTabOption);
		}
	}

	
	public void setDefaultSettings() {
		this.allocation_expression_arguments_alignment = DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		this.anonymous_type_declaration_brace_position = DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION;
		this.array_initializer_brace_position = DEFAULT_ARRAY_INITIALIZER_BRACE_POSITION;
		this.array_initializer_continuation_indentation = DEFAULT_ARRAY_INITIALIZER_CONTINUATION_INDENTATION;
		this.array_initializer_expressions_alignment = DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT;
		this.binary_expression_alignment = DEFAULT_BINARY_EXPRESSION_ALIGNMENT;
		this.blank_lines_after_imports = DEFAULT_BLANK_LINES_AFTER_IMPORTS;
		this.blank_lines_after_package = DEFAULT_BLANK_LINES_AFTER_PACKAGE;
		this.blank_lines_before_field = DEFAULT_BLANK_LINES_BEFORE_FIELD;
		this.blank_lines_before_first_class_body_declaration = DEFAULT_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION;
		this.blank_lines_before_imports = DEFAULT_BLANK_LINES_BEFORE_IMPORTS;
		this.blank_lines_before_member_type = DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE;
		this.blank_lines_before_method = DEFAULT_BLANK_LINES_BEFORE_METHOD;
		this.blank_lines_before_new_chunk = DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK;
		this.blank_lines_before_package = DEFAULT_BLANK_LINES_BEFORE_PACKAGE;
		this.block_brace_position = DEFAULT_BLOCK_BRACE_POSITION;
		this.compact_else_if = DEFAULT_COMPACT_ELSE_IF;
		this.compact_if_alignment = DEFAULT_COMPACT_IF_ALIGNMENT;
		this.conditional_expression_alignment = DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT;
		this.continuation_indentation = DEFAULT_CONTINUATION_INDENTATION;
		this.explicit_constructor_arguments_alignment = DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT;
		this.filling_space = DEFAULT_FILLING_SPACE;
		this.format_guardian_clause_on_one_line = DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE;
		this.indent_block_statements = DEFAULT_INDENT_BLOCK_STATEMENTS;
		this.indent_body_declarations_compare_to_type_header = DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER;
		this.indent_breaks_compare_to_cases = DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES;
		this.indent_switchstatements_compare_to_cases = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES;
		this.indent_switchstatements_compare_to_switch = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH;
		this.insert_new_line_after_opening_brace_in_array_initializer = DEFAULT_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER;
		this.insert_new_line_before_closing_brace_in_array_initializer = DEFAULT_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER;
		this.insert_new_line_in_control_statements = DEFAULT_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS;
		this.insert_new_line_in_empty_anonymous_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION;
		this.insert_new_line_in_empty_block = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_BLOCK;
		this.insert_new_line_in_empty_method_body = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY;
		this.insert_new_line_in_empty_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION;
		this.insert_space_after_assignment_operators = DEFAULT_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS;
		this.insert_space_after_binary_operator = DEFAULT_INSERT_SPACE_AFTER_BINARY_OPERATOR;
		this.insert_space_after_block_close_brace = DEFAULT_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE;
		this.insert_space_after_closing_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST;
		this.insert_space_after_colon_in_assert = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_ASSERT;
		this.insert_space_after_colon_in_conditional = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL;
		this.insert_space_after_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT;
		this.insert_space_after_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION;
		this.insert_space_after_comma_in_array_initializer = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER;
		this.insert_space_after_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		this.insert_space_after_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS;
		this.insert_space_after_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		this.insert_space_after_comma_in_for_increments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS;
		this.insert_space_after_comma_in_for_inits = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS;
		this.insert_space_after_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS;
		this.insert_space_after_comma_in_method_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS;
		this.insert_space_after_comma_in_method_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS;
		this.insert_space_after_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		this.insert_space_after_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		this.insert_space_after_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES;
		this.insert_space_after_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION;
		this.insert_space_after_opening_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST;
		this.insert_space_after_postfix_operator = DEFAULT_INSERT_SPACE_AFTER_POSTFIX_OPERATOR;
		this.insert_space_after_prefix_operator = DEFAULT_INSERT_SPACE_AFTER_PREFIX_OPERATOR;
		this.insert_space_after_question_in_conditional = DEFAULT_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL;
		this.insert_space_after_semicolon_in_for = DEFAULT_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR;
		this.insert_space_after_unary_operator = DEFAULT_INSERT_SPACE_AFTER_UNARY_OPERATOR;
		this.insert_space_before_anonymous_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE;
		this.insert_space_before_assignment_operators = DEFAULT_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS;
		this.insert_space_before_binary_operator = DEFAULT_INSERT_SPACE_BEFORE_BINARY_OPERATOR;
		this.insert_space_before_block_open_brace = DEFAULT_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE;
		this.insert_space_before_bracket_in_array_reference = DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE;
		this.insert_space_before_bracket_in_array_type_reference = DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE;
		this.insert_space_before_catch_expression = DEFAULT_INSERT_SPACE_BEFORE_CATCH_EXPRESSION;
		this.insert_space_before_closing_brace_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER;
		this.insert_space_before_closing_paren = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN;
		this.insert_space_before_closing_paren_in_cast = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST;
		this.insert_space_before_closing_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION;
		this.insert_space_before_colon_in_assert = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_ASSERT;
		this.insert_space_before_colon_in_case = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CASE;
		this.insert_space_before_colon_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL;
		this.insert_space_before_colon_in_default = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT;
		this.insert_space_before_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT;
		this.insert_space_before_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION;
		this.insert_space_before_comma_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER;
		this.insert_space_before_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		this.insert_space_before_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS;
		this.insert_space_before_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		this.insert_space_before_comma_in_for_increments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS;
		this.insert_space_before_comma_in_for_inits = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS;
		this.insert_space_before_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS;
		this.insert_space_before_comma_in_method_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS;
		this.insert_space_before_comma_in_method_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS;
		this.insert_space_before_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		this.insert_space_before_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		this.insert_space_before_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES;
		this.insert_space_before_first_argument = DEFAULT_INSERT_SPACE_BEFORE_FIRST_ARGUMENT;
		this.insert_space_before_first_initializer = DEFAULT_INSERT_SPACE_BEFORE_FIRST_INITIALIZER;
		this.insert_space_before_for_paren = DEFAULT_INSERT_SPACE_BEFORE_FOR_PAREN;
		this.insert_space_before_if_condition = DEFAULT_INSERT_SPACE_BEFORE_IF_CONDITION;
		this.insert_space_before_message_send = DEFAULT_INSERT_SPACE_BEFORE_MESSAGE_SEND;
		this.insert_space_before_method_declaration_open_paren = DEFAULT_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN;
		this.insert_space_before_method_open_brace = DEFAULT_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE;
		this.insert_space_before_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION;
		this.insert_space_before_opening_brace_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER;
		this.insert_space_before_postfix_operator = DEFAULT_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR;
		this.insert_space_before_prefix_operator = DEFAULT_INSERT_SPACE_BEFORE_PREFIX_OPERATOR;
		this.insert_space_before_question_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL;
		this.insert_space_before_semicolon = DEFAULT_INSERT_SPACE_BEFORE_SEMICOLON;
		this.insert_space_before_switch_condition = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_CONDITION;
		this.insert_space_before_switch_open_brace = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE;
		this.insert_space_before_synchronized_condition = DEFAULT_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION;
		this.insert_space_before_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE;
		this.insert_space_before_unary_operator = DEFAULT_INSERT_SPACE_BEFORE_UNARY_OPERATOR;
		this.insert_space_before_while_condition = DEFAULT_INSERT_SPACE_BEFORE_WHILE_CONDITION;
		this.insert_space_between_brackets_in_array_reference = DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE;
		this.insert_space_between_brackets_in_array_type_reference = DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE;
		this.insert_space_between_empty_arguments = DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS;
		this.insert_space_between_empty_array_initializer = DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER;
		this.insert_space_in_catch_expression = DEFAULT_INSERT_SPACE_IN_CATCH_EXPRESSION;
		this.insert_space_in_for_parens = DEFAULT_INSERT_SPACE_IN_FOR_PARENS;
		this.insert_space_in_if_condition = DEFAULT_INSERT_SPACE_IN_IF_CONDITION;
		this.insert_space_in_switch_condition = DEFAULT_INSERT_SPACE_IN_SWITCH_CONDITION;
		this.insert_space_in_synchronized_condition = DEFAULT_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION;
		this.insert_space_in_while_condition = DEFAULT_INSERT_SPACE_IN_WHILE_CONDITION;
		this.insert_space_within_message_send = DEFAULT_INSERT_SPACE_WITHIN_MESSAGE_SEND;
		this.keep_else_statement_on_same_line = DEFAULT_KEEP_ELSE_STATEMENT_ON_SAME_LINE;
		this.keep_simple_if_on_one_line = DEFAULT_KEEP_SIMPLE_IF_ON_ONE_LINE;
		this.keep_then_statement_on_same_line = DEFAULT_KEEP_THEN_STATEMENT_ON_SAME_LINE;
		this.message_send_arguments_alignment = DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT;
		this.message_send_selector_alignment = DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT;
		this.method_declaration_arguments_alignment = DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT;
		this.method_declaration_brace_position = DEFAULT_METHOD_DECLARATION_BRACE_POSITION;
		this.method_throws_clause_alignment = DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT;
		this.multiple_fields_alignment = DEFAULT_MULTIPLE_FIELDS_ALIGNMENT;
		this.number_of_blank_lines_to_insert_at_beginning_of_method_body = DEFAULT_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY;
		this.number_of_empty_lines_to_preserve = DEFAULT_NUMBER_OF_EMPTY_LINES_TO_PRESERVE;
		this.page_width = DEFAULT_PAGE_WIDTH;
		this.preserve_user_linebreaks = DEFAULT_PRESERVE_USER_LINEBREAKS;
		this.put_empty_statement_on_new_line = DEFAULT_PUT_EMPTY_STATEMENT_ON_NEW_LINE;
		this.qualified_allocation_expression_arguments_alignment = DEFAULT_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		this.switch_brace_position = DEFAULT_SWITCH_BRACE_POSITION;
		this.tab_size = DEFAULT_TAB_SIZE;
		this.type_declaration_brace_position = DEFAULT_TYPE_DECLARATION_BRACE_POSITION;
		this.type_declaration_superclass_alignment = DEFAULT_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT;
		this.type_declaration_superinterfaces_alignment = DEFAULT_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT;
		this.type_member_alignment = DEFAULT_TYPE_MEMBER_ALIGNMENT;
		this.use_tab = DEFAULT_USE_TAB;
	}
	public void setJavaConventionsSettings() {
		this.allocation_expression_arguments_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.anonymous_type_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		this.array_initializer_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		this.array_initializer_continuation_indentation = 2;
		this.array_initializer_expressions_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.binary_expression_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.blank_lines_after_imports = 1;
		this.blank_lines_after_package = 1;
		this.blank_lines_before_field = 1;
		this.blank_lines_before_first_class_body_declaration = 0;
		this.blank_lines_before_imports= 1;
		this.blank_lines_before_member_type = 1;
		this.blank_lines_before_method = 1;
		this.blank_lines_before_new_chunk = 1;
		this.blank_lines_before_package = 0;
		this.block_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		this.compact_else_if = true;
		this.compact_if_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.conditional_expression_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_NEXT_PER_LINE_SPLIT);
		this.continuation_indentation = 2;
		this.explicit_constructor_arguments_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.filling_space = ' ';
		this.format_guardian_clause_on_one_line = true;
		this.indent_block_statements = true;
		this.indent_body_declarations_compare_to_type_header = true;
		this.indent_breaks_compare_to_cases = true;
		this.indent_switchstatements_compare_to_cases = true;
		this.indent_switchstatements_compare_to_switch = false;
		this.insert_new_line_after_opening_brace_in_array_initializer = false;
		this.insert_new_line_before_closing_brace_in_array_initializer = false;
		this.insert_new_line_in_control_statements = false;
		this.insert_new_line_in_empty_anonymous_type_declaration = true;
		this.insert_new_line_in_empty_block = true;
		this.insert_new_line_in_empty_method_body = true;
		this.insert_new_line_in_empty_type_declaration = true;
		this.insert_space_after_assignment_operators = true;
		this.insert_space_after_binary_operator = true;
		this.insert_space_after_block_close_brace = true;
		this.insert_space_after_closing_paren_in_cast = true;
		this.insert_space_after_colon_in_assert = true;
		this.insert_space_after_colon_in_conditional = true;
		this.insert_space_after_colon_in_labeled_statement = true;
		this.insert_space_after_comma_in_allocation_expression = true;
		this.insert_space_after_comma_in_array_initializer = true;
		this.insert_space_after_comma_in_constructor_arguments = true;
		this.insert_space_after_comma_in_constructor_throws = true;
		this.insert_space_after_comma_in_explicitconstructorcall_arguments = true;
		this.insert_space_after_comma_in_for_increments = true;
		this.insert_space_after_comma_in_for_inits = true;
		this.insert_space_after_comma_in_messagesend_arguments = true;
		this.insert_space_after_comma_in_method_arguments = true;
		this.insert_space_after_comma_in_method_throws = true;
		this.insert_space_after_comma_in_multiple_field_declarations = true;
		this.insert_space_after_comma_in_multiple_local_declarations = true;
		this.insert_space_after_comma_in_superinterfaces = true;
		this.insert_space_after_open_paren_in_parenthesized_expression = false;
		this.insert_space_after_opening_paren_in_cast = false;
		this.insert_space_after_postfix_operator = false;
		this.insert_space_after_prefix_operator = false;
		this.insert_space_after_question_in_conditional = true;
		this.insert_space_after_semicolon_in_for = true;
		this.insert_space_after_unary_operator = false;
		this.insert_space_before_anonymous_type_open_brace = true;
		this.insert_space_before_assignment_operators = true;
		this.insert_space_before_binary_operator = true;
		this.insert_space_before_block_open_brace = true;
		this.insert_space_before_bracket_in_array_reference = false;
		this.insert_space_before_bracket_in_array_type_reference = false;
		this.insert_space_before_catch_expression = true;
		this.insert_space_before_closing_brace_in_array_initializer = false;
		this.insert_space_before_closing_paren = false;
		this.insert_space_before_closing_paren_in_cast = false;
		this.insert_space_before_closing_paren_in_parenthesized_expression = false;
		this.insert_space_before_colon_in_assert = true;
		this.insert_space_before_colon_in_case = false;
		this.insert_space_before_colon_in_conditional = true;
		this.insert_space_before_colon_in_default = false;
		this.insert_space_before_colon_in_labeled_statement = false;
		this.insert_space_before_comma_in_allocation_expression = false;
		this.insert_space_before_comma_in_array_initializer = false;
		this.insert_space_before_comma_in_constructor_arguments = false;
		this.insert_space_before_comma_in_constructor_throws = false;
		this.insert_space_before_comma_in_explicitconstructorcall_arguments = false;
		this.insert_space_before_comma_in_for_increments = false;
		this.insert_space_before_comma_in_for_inits = false;
		this.insert_space_before_comma_in_messagesend_arguments = false;
		this.insert_space_before_comma_in_method_arguments = false;
		this.insert_space_before_comma_in_method_throws = false;
		this.insert_space_before_comma_in_multiple_field_declarations = false;
		this.insert_space_before_comma_in_multiple_local_declarations = false;
		this.insert_space_before_comma_in_superinterfaces = false;
		this.insert_space_before_first_argument = false;
		this.insert_space_before_first_initializer = false;
		this.insert_space_before_for_paren = true;
		this.insert_space_before_if_condition = true;
		this.insert_space_before_message_send = false;
		this.insert_space_before_method_declaration_open_paren = false;
		this.insert_space_before_method_open_brace = true;
		this.insert_space_before_open_paren_in_parenthesized_expression = false;
		this.insert_space_before_opening_brace_in_array_initializer = false;
		this.insert_space_before_postfix_operator = false;
		this.insert_space_before_prefix_operator = false;
		this.insert_space_before_question_in_conditional = true;
		this.insert_space_before_semicolon = false;
		this.insert_space_before_switch_condition = true;
		this.insert_space_before_switch_open_brace = true;
		this.insert_space_before_synchronized_condition = true;
		this.insert_space_before_type_open_brace = true;
		this.insert_space_before_unary_operator = false;
		this.insert_space_before_while_condition = true;
		this.insert_space_between_brackets_in_array_reference = false;
		this.insert_space_between_brackets_in_array_type_reference = false;
		this.insert_space_between_empty_arguments = false;
		this.insert_space_between_empty_array_initializer = false;
		this.insert_space_in_catch_expression = false;
		this.insert_space_in_for_parens = false;
		this.insert_space_in_if_condition = false;
		this.insert_space_in_switch_condition = false;
		this.insert_space_in_synchronized_condition = false;
		this.insert_space_in_while_condition = false;
		this.insert_space_within_message_send = false;
		this.keep_else_statement_on_same_line = false;
		this.keep_simple_if_on_one_line = true;
		this.keep_then_statement_on_same_line = true;
		this.message_send_arguments_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.message_send_selector_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.method_declaration_arguments_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.method_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		this.method_throws_clause_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.multiple_fields_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.number_of_blank_lines_to_insert_at_beginning_of_method_body = 0;
		this.number_of_empty_lines_to_preserve = 1;
		this.page_width = 80;
		this.preserve_user_linebreaks = false;
		this.put_empty_statement_on_new_line = true;
		this.qualified_allocation_expression_arguments_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.switch_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		this.tab_size = 4;
		this.type_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		this.type_declaration_superclass_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.type_declaration_superinterfaces_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		this.type_member_alignment = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_NO_ALIGNMENT);
		this.use_tab = false;
	}
}
