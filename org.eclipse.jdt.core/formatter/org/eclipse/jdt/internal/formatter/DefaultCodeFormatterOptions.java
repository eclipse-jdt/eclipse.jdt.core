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
	public static final char DASH = ' ';//183;
	public static final int DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final String DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_BINARY_EXPRESSION_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT;
	public static final int DEFAULT_BLANK_LINES_AFTER_IMPORTS = 0;
	public static final int DEFAULT_BLANK_LINES_AFTER_PACKAGE = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_FIELD = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_IMPORTS = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_METHOD = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_PACKAGE = 0;
	public static final String DEFAULT_BLOCK_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final boolean DEFAULT_COMPACT_ELSE_IF = true;
	public static final int DEFAULT_COMPACT_IF_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_INDENT_BY_ONE;
	public static final int DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT;
	public static final int DEFAULT_CONTINUATION_INDENTATION = 2; // 2 indentations
	public static final int DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final char DEFAULT_FILLING_SPACE = DASH;
	public static final boolean DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE = false;
	public static final boolean DEFAULT_INDENT_BLOCK_STATEMENTS = true;
	public static final boolean DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER = true;
	public static final boolean DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES = true;
	public static final boolean DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = true;
	public static final boolean DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = true;
	public static final int DEFAULT_INITIAL_INDENTATION_LEVEL = 0;
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
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION = false;
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
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION = false;
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
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION = false;
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
	public static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");	//$NON-NLS-1$
	public static final int DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final String DEFAULT_METHOD_DECLARATION_BRACE_POSITION = DefaultCodeFormatterConstants.END_OF_LINE;
	public static final int DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	public static final int DEFAULT_MULTIPLE_FIELDS_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT;//$NON-NLS-1$
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
	public static final boolean DEFAULT_USE_TAB = false;

	public int allocation_expression_arguments_alignment;
	public String anonymous_type_declaration_brace_position;
	public int array_initializer_expressions_alignment;
	public int binary_expression_alignment;
	public int blank_lines_after_imports;
	public int blank_lines_after_package;
	public int blank_lines_before_field;
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
	// TODO remove when testing is over
	public char filling_space;
	public boolean format_guardian_clause_on_one_line;
	public boolean indent_block_statements;
	public boolean indent_body_declarations_compare_to_type_header;
	public boolean indent_breaks_compare_to_cases;
	public boolean indent_switchstatements_compare_to_cases;
	public boolean indent_switchstatements_compare_to_switch;
	public int initial_indentation_level;
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
	public String line_delimiter;
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
	
	public DefaultCodeFormatterOptions() {
		this.use_tab = DEFAULT_USE_TAB;
		this.tab_size = DEFAULT_TAB_SIZE;
		this.page_width = DEFAULT_PAGE_WIDTH;
		this.insert_new_line_in_empty_block = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_BLOCK;
		this.blank_lines_before_package = DEFAULT_BLANK_LINES_BEFORE_PACKAGE;
		this.blank_lines_after_package = DEFAULT_BLANK_LINES_AFTER_PACKAGE;
		this.blank_lines_before_imports= DEFAULT_BLANK_LINES_BEFORE_PACKAGE;
		this.blank_lines_after_imports = DEFAULT_BLANK_LINES_AFTER_IMPORTS;
		this.initial_indentation_level = DEFAULT_INITIAL_INDENTATION_LEVEL;
		this.line_delimiter = DEFAULT_LINE_SEPARATOR;
		this.continuation_indentation = DEFAULT_CONTINUATION_INDENTATION;
		this.type_declaration_brace_position = DEFAULT_TYPE_DECLARATION_BRACE_POSITION;
		this.method_declaration_brace_position = DEFAULT_METHOD_DECLARATION_BRACE_POSITION;
		this.insert_space_before_method_declaration_open_paren = DEFAULT_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN;
		this.insert_space_before_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE;
		this.insert_space_before_method_open_brace = DEFAULT_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE;
		this.insert_space_between_empty_arguments = DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS;
		this.insert_space_before_first_argument = DEFAULT_INSERT_SPACE_BEFORE_FIRST_ARGUMENT;
		this.insert_space_before_closing_paren = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN;
		this.insert_space_before_assignment_operators = DEFAULT_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS;
		this.insert_space_after_assignment_operators = DEFAULT_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS;
		this.put_empty_statement_on_new_line = DEFAULT_PUT_EMPTY_STATEMENT_ON_NEW_LINE;
		this.insert_space_before_semicolon = DEFAULT_INSERT_SPACE_BEFORE_SEMICOLON;
		this.insert_space_within_message_send = DEFAULT_INSERT_SPACE_WITHIN_MESSAGE_SEND;
		this.insert_space_before_message_send = DEFAULT_INSERT_SPACE_BEFORE_MESSAGE_SEND;
		this.insert_space_before_first_initializer = DEFAULT_INSERT_SPACE_BEFORE_FIRST_INITIALIZER;
		this.insert_space_before_closing_brace_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER;
		this.block_brace_position = DEFAULT_BLOCK_BRACE_POSITION;
		this.insert_space_before_block_open_brace = DEFAULT_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE;
		this.insert_space_before_colon_in_case = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CASE;
		this.insert_space_after_opening_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST;
		this.insert_space_before_closing_paren_in_cast = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST;
		this.insert_space_before_colon_in_default = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT;
		this.insert_space_in_while_condition = DEFAULT_INSERT_SPACE_IN_WHILE_CONDITION;
		this.insert_space_in_if_condition = DEFAULT_INSERT_SPACE_IN_IF_CONDITION;
		this.compact_else_if = DEFAULT_COMPACT_ELSE_IF;
		this.insert_space_before_if_condition = DEFAULT_INSERT_SPACE_BEFORE_IF_CONDITION;		
		this.insert_space_before_for_paren = DEFAULT_INSERT_SPACE_BEFORE_FOR_PAREN;		
		this.insert_space_in_for_parens = DEFAULT_INSERT_SPACE_IN_FOR_PARENS;
		this.switch_brace_position = DEFAULT_SWITCH_BRACE_POSITION;
		this.insert_space_before_switch_open_brace = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE;
		this.insert_space_in_switch_condition = DEFAULT_INSERT_SPACE_IN_SWITCH_CONDITION;
		this.insert_space_before_switch_condition = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_CONDITION;
		this.insert_space_in_synchronized_condition = DEFAULT_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION;
		this.insert_space_before_synchronized_condition = DEFAULT_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION;
		this.insert_space_in_catch_expression = DEFAULT_INSERT_SPACE_IN_CATCH_EXPRESSION;
		this.insert_space_before_catch_expression = DEFAULT_INSERT_SPACE_BEFORE_CATCH_EXPRESSION;
		this.insert_space_before_while_condition = DEFAULT_INSERT_SPACE_BEFORE_WHILE_CONDITION;
		this.insert_new_line_in_control_statements = DEFAULT_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS;
		this.insert_space_before_binary_operator = DEFAULT_INSERT_SPACE_BEFORE_BINARY_OPERATOR;
		this.insert_space_after_binary_operator = DEFAULT_INSERT_SPACE_AFTER_BINARY_OPERATOR;
		this.insert_space_before_unary_operator = DEFAULT_INSERT_SPACE_BEFORE_UNARY_OPERATOR;
		this.insert_space_after_unary_operator = DEFAULT_INSERT_SPACE_AFTER_UNARY_OPERATOR;
		this.insert_space_before_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		this.insert_space_after_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		this.insert_space_before_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES;
		this.insert_space_after_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES;
		this.insert_space_before_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION;
		this.insert_space_after_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION;
		this.insert_space_before_comma_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER;
		this.insert_space_after_comma_in_array_initializer = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER;
		this.insert_space_before_colon_in_assert = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_ASSERT;
		this.insert_space_after_colon_in_assert = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_ASSERT;
		this.insert_space_before_question_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL;
		this.insert_space_after_question_in_conditional = DEFAULT_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL;
		this.insert_space_before_colon_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL;
		this.insert_space_after_colon_in_conditional = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL;
		this.insert_space_before_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		this.insert_space_after_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		this.insert_space_before_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS;
		this.insert_space_after_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS;
		this.insert_space_before_comma_in_for_increments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS;
		this.insert_space_after_comma_in_for_increments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS;
		this.insert_space_before_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		this.insert_space_after_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		this.insert_space_before_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT;
		this.insert_space_after_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT;
		this.insert_space_before_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS;
		this.insert_space_after_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS;
		this.insert_space_before_comma_in_method_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS;
		this.insert_space_after_comma_in_method_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS;
		this.insert_space_before_comma_in_method_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS;
		this.insert_space_after_comma_in_method_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS;
		this.insert_space_before_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		this.insert_space_after_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		this.insert_space_before_comma_in_for_inits = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS;
		this.insert_space_after_comma_in_for_inits = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS;
		this.insert_space_after_semicolon_in_for = DEFAULT_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR;
		this.insert_space_before_postfix_operator = DEFAULT_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR;
		this.insert_space_after_postfix_operator = DEFAULT_INSERT_SPACE_AFTER_POSTFIX_OPERATOR;
		this.insert_space_before_prefix_operator = DEFAULT_INSERT_SPACE_BEFORE_PREFIX_OPERATOR;
		this.insert_space_after_prefix_operator = DEFAULT_INSERT_SPACE_AFTER_PREFIX_OPERATOR;
		this.indent_switchstatements_compare_to_switch = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH;
		this.indent_switchstatements_compare_to_cases = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES;
		this.indent_breaks_compare_to_cases = DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES;
		this.anonymous_type_declaration_brace_position = DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION;
		this.insert_space_before_anonymous_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE;
		this.indent_body_declarations_compare_to_type_header = DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER;
		this.filling_space = DEFAULT_FILLING_SPACE;
		this.insert_space_after_closing_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST;
		this.number_of_blank_lines_to_insert_at_beginning_of_method_body = DEFAULT_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY;
		this.keep_simple_if_on_one_line = DEFAULT_KEEP_SIMPLE_IF_ON_ONE_LINE;
		this.format_guardian_clause_on_one_line = DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE;
		this.insert_space_before_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION;
		this.insert_space_after_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION;
		this.insert_space_before_closing_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION;
		this.keep_then_statement_on_same_line = DEFAULT_KEEP_THEN_STATEMENT_ON_SAME_LINE;
		this.blank_lines_before_new_chunk = DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK;
		this.blank_lines_before_field = DEFAULT_BLANK_LINES_BEFORE_FIELD;
		this.blank_lines_before_method = DEFAULT_BLANK_LINES_BEFORE_METHOD;
		this.blank_lines_before_member_type = DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE;
		this.insert_space_after_block_close_brace = DEFAULT_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE;
		this.keep_else_statement_on_same_line = DEFAULT_KEEP_ELSE_STATEMENT_ON_SAME_LINE;
		this.insert_space_before_bracket_in_array_type_reference = DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE;
		this.insert_space_between_brackets_in_array_type_reference = DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE;
		this.compact_if_alignment = DEFAULT_COMPACT_IF_ALIGNMENT;
		this.type_declaration_superclass_alignment = DEFAULT_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT;
		this.type_declaration_superinterfaces_alignment = DEFAULT_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT;
		this.method_declaration_arguments_alignment = DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT;
		this.message_send_arguments_alignment = DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT;
		this.message_send_selector_alignment = DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT;
		this.method_throws_clause_alignment = DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT;
		this.type_member_alignment = DEFAULT_TYPE_MEMBER_ALIGNMENT;
		this.allocation_expression_arguments_alignment = DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		this.qualified_allocation_expression_arguments_alignment = DEFAULT_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		this.array_initializer_expressions_alignment = DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT;
		this.explicit_constructor_arguments_alignment = DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT;
		this.conditional_expression_alignment = DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT;
		this.binary_expression_alignment = DEFAULT_BINARY_EXPRESSION_ALIGNMENT;
		this.insert_new_line_in_empty_method_body = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY;
		this.insert_new_line_in_empty_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION;
		this.insert_new_line_in_empty_anonymous_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION;
		this.number_of_empty_lines_to_preserve = DEFAULT_NUMBER_OF_EMPTY_LINES_TO_PRESERVE;
		this.preserve_user_linebreaks = DEFAULT_PRESERVE_USER_LINEBREAKS;
		this.insert_new_line_before_closing_brace_in_array_initializer = DEFAULT_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER;
		this.multiple_fields_alignment = DEFAULT_MULTIPLE_FIELDS_ALIGNMENT;
		this.insert_space_between_brackets_in_array_reference = DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE;
		this.insert_space_before_bracket_in_array_reference = DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE;
		this.indent_block_statements = DEFAULT_INDENT_BLOCK_STATEMENTS;
		this.insert_space_before_opening_brace_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER;
	}

	public DefaultCodeFormatterOptions(Map settings) {
		this();
		final Object lineDelimiterOptions = settings.get(DefaultCodeFormatterConstants.FORMATTER_LINE_SEPARATOR);
		if (lineDelimiterOptions != null) {
			this.line_delimiter = (String) lineDelimiterOptions;
		}
		final Object tabChar = settings.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		if (tabChar != null) {
			this.use_tab = JavaCore.TAB.equals(tabChar);
		}
		final Object tabSizeOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
		if (tabSizeOption != null) { 
			this.tab_size = Integer.parseInt((String)tabSizeOption);
		}
		final Object lineSplitOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT);
		if (lineSplitOption != null) { 
			this.page_width = Integer.parseInt((String)lineSplitOption);
		}
		final Object blankLinesBeforePackageOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE);
		if (blankLinesBeforePackageOption != null) { 
			this.blank_lines_before_package = Integer.parseInt((String)blankLinesBeforePackageOption);
		}
		final Object blankLinesAfterPackageOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE);
		if (blankLinesAfterPackageOption != null) { 
			this.blank_lines_after_package = Integer.parseInt((String)blankLinesAfterPackageOption);
		}
		final Object blankLinesBeforeImportsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_IMPORTS);
		if (blankLinesBeforeImportsOption != null) { 
			this.blank_lines_before_imports = Integer.parseInt((String)blankLinesBeforeImportsOption);
		}
		final Object blankLinesAfterImportsOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_IMPORTS);
		if (blankLinesAfterImportsOption != null) { 
			this.blank_lines_after_imports = Integer.parseInt((String)blankLinesAfterImportsOption);
		}
		final Object initialIndentationLevelOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INITIAL_INDENTATION_LEVEL);
		if (initialIndentationLevelOption != null) { 
			this.initial_indentation_level = Integer.parseInt((String)initialIndentationLevelOption);
		}
		final Object continuationIndentationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION);
		if (continuationIndentationOption != null) { 
			this.continuation_indentation = Integer.parseInt((String)continuationIndentationOption);
		}
		final Object typeDeclarationBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION);
		if (typeDeclarationBracePositionOption != null) { 
			this.type_declaration_brace_position = (String) typeDeclarationBracePositionOption;
		}
		final Object methodDeclarationBracePositionOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION);
		if (methodDeclarationBracePositionOption != null) { 
			this.method_declaration_brace_position = (String) methodDeclarationBracePositionOption;
		}		
		final Object insertSpaceBeforeMethodDeclarationOption = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN);
		if (insertSpaceBeforeMethodDeclarationOption != null) { 
			this.insert_space_before_method_declaration_open_paren = insertSpaceBeforeMethodDeclarationOption.equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE) != null) { 
			this.insert_space_before_type_open_brace = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE) != null) { 
			this.insert_space_before_method_open_brace = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS) != null) { 
			this.insert_space_between_empty_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_ARGUMENT) != null) { 
			this.insert_space_before_first_argument = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_ARGUMENT).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN) != null) { 
			this.insert_space_before_closing_paren = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS) != null) { 
			this.insert_space_after_assignment_operators = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS) != null) { 
			this.insert_space_before_assignment_operators = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS).equals(JavaCore.INSERT);
		}								
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE) != null) { 
			this.put_empty_statement_on_new_line = settings.get(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE).equals(JavaCore.INSERT);
		}								
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON) != null) { 
			this.insert_space_before_semicolon = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND) != null) { 
			this.insert_space_within_message_send = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_MESSAGE_SEND) != null) { 
			this.insert_space_before_message_send = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_MESSAGE_SEND).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_INITIALIZER) != null) { 
			this.insert_space_before_first_initializer = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_INITIALIZER).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER) != null) { 
			this.insert_space_before_closing_brace_in_array_initializer = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION) != null) { 
			this.block_brace_position = (String) settings.get(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE) != null) { 
			this.insert_space_before_block_open_brace = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE) != null) { 
			this.insert_space_before_colon_in_case = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST) != null) { 
			this.insert_space_after_opening_paren_in_cast = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST) != null) { 
			this.insert_space_before_closing_paren_in_cast = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT) != null) { 
			this.insert_space_before_colon_in_default = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_WHILE_CONDITION) != null) { 
			this.insert_space_in_while_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_WHILE_CONDITION).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_IF_CONDITION) != null) { 
			this.insert_space_in_if_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_IF_CONDITION).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_COMPACT_ELSE_IF) != null) { 
			this.compact_else_if = settings.get(DefaultCodeFormatterConstants.FORMATTER_COMPACT_ELSE_IF).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_IF_CONDITION) != null) { 
			this.insert_space_before_if_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_IF_CONDITION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FOR_PAREN) != null) { 
			this.insert_space_before_for_paren = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FOR_PAREN).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_FOR_PARENS) != null) { 
			this.insert_space_in_for_parens = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_FOR_PARENS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION) != null) { 
			this.switch_brace_position = (String) settings.get(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE) != null) { 
			this.insert_space_before_switch_open_brace = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SWITCH_CONDITION) != null) { 
			this.insert_space_in_switch_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SWITCH_CONDITION).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_CONDITION) != null) { 
			this.insert_space_before_switch_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_CONDITION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION) != null) { 
			this.insert_space_in_synchronized_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION).equals(JavaCore.INSERT);
		}	
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION) != null) { 
			this.insert_space_before_synchronized_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_CATCH_EXPRESSION) != null) { 
			this.insert_space_in_catch_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_CATCH_EXPRESSION).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CATCH_EXPRESSION) != null) { 
			this.insert_space_before_catch_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CATCH_EXPRESSION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_WHILE_CONDITION) != null) { 
			this.insert_space_before_while_condition = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_WHILE_CONDITION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS) != null) { 
			this.insert_new_line_in_control_statements = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR) != null) { 
			this.insert_space_before_binary_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR) != null) { 
			this.insert_space_after_binary_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR) != null) { 
			this.insert_space_before_unary_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR) != null) { 
			this.insert_space_after_unary_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS) != null) { 
			this.insert_space_before_comma_in_multiple_field_declarations = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS) != null) { 
			this.insert_space_after_comma_in_multiple_field_declarations = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES) != null) { 
			this.insert_space_before_comma_in_superinterfaces = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES) != null) { 
			this.insert_space_after_comma_in_superinterfaces = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION) != null) { 
			this.insert_space_before_comma_in_allocation_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION) != null) { 
			this.insert_space_after_comma_in_allocation_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER) != null) { 
			this.insert_space_before_comma_in_array_initializer = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER) != null) { 
			this.insert_space_after_comma_in_array_initializer = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT) != null) { 
			this.insert_space_before_colon_in_assert = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT) != null) { 
			this.insert_space_after_colon_in_assert = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL) != null) { 
			this.insert_space_before_question_in_conditional = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL) != null) { 
			this.insert_space_after_question_in_conditional = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL) != null) { 
			this.insert_space_before_colon_in_conditional = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL) != null) { 
			this.insert_space_after_colon_in_conditional = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_constructor_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_constructor_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS) != null) { 
			this.insert_space_before_comma_in_constructor_throws = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS) != null) { 
			this.insert_space_after_comma_in_constructor_throws = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS) != null) { 
			this.insert_space_before_comma_in_for_increments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS) != null) { 
			this.insert_space_after_comma_in_for_increments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_explicitconstructorcall_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_explicitconstructorcall_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT) != null) { 
			this.insert_space_before_colon_in_labeled_statement = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT) != null) { 
			this.insert_space_after_colon_in_labeled_statement = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_messagesend_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_messagesend_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_method_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_method_arguments = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS) != null) { 
			this.insert_space_before_comma_in_method_throws = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS) != null) { 
			this.insert_space_after_comma_in_method_throws = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS) != null) { 
			this.insert_space_before_comma_in_multiple_local_declarations = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS) != null) { 
			this.insert_space_after_comma_in_multiple_local_declarations = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS) != null) { 
			this.insert_space_before_comma_in_for_inits = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS) != null) { 
			this.insert_space_after_comma_in_for_inits = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR) != null) { 
			this.insert_space_after_semicolon_in_for = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR) != null) { 
			this.insert_space_before_postfix_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR) != null) { 
			this.insert_space_after_postfix_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR) != null) { 
			this.insert_space_before_prefix_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR) != null) { 
			this.insert_space_after_prefix_operator = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH) != null) { 
			this.indent_switchstatements_compare_to_switch = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES) != null) { 
			this.indent_switchstatements_compare_to_cases = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES) != null) { 
			this.indent_breaks_compare_to_cases = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES).equals(JavaCore.INSERT);
		}		
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION) != null) { 
			this.anonymous_type_declaration_brace_position = (String) settings.get(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE) != null) { 
			this.insert_space_before_anonymous_type_open_brace = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER) != null) { 
			this.indent_body_declarations_compare_to_type_header = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_FILLING_SPACE) != null) { 
			this.filling_space = ((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_FILLING_SPACE)).charAt(0);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST) != null) { 
			this.insert_space_after_closing_paren_in_cast = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY) != null) { 
			this.number_of_blank_lines_to_insert_at_beginning_of_method_body = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE) != null) { 
			this.keep_simple_if_on_one_line = settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE) != null) { 
			this.format_guardian_clause_on_one_line = settings.get(DefaultCodeFormatterConstants.FORMATTER_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION) != null) { 
			this.insert_space_before_open_paren_in_parenthesized_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION) != null) { 
			this.insert_space_after_open_paren_in_parenthesized_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION) != null) { 
			this.insert_space_before_closing_paren_in_parenthesized_expression = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE) != null) { 
			this.keep_then_statement_on_same_line = settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK) != null) { 
			this.blank_lines_before_new_chunk = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIELD) != null) { 
			this.blank_lines_before_field = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIELD));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD) != null) { 
			this.blank_lines_before_method = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE) != null) { 
			this.blank_lines_before_member_type = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE) != null) {
			this.insert_space_after_block_close_brace = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE) != null) {
			this.keep_else_statement_on_same_line = settings.get(DefaultCodeFormatterConstants.FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE) != null) {
			this.insert_space_before_bracket_in_array_type_reference = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE) != null) {
			this.insert_space_between_brackets_in_array_type_reference = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_COMPACT_IF_ALIGNMENT) != null) { 
			this.compact_if_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_COMPACT_IF_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT) != null) { 
			this.type_declaration_superclass_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT) != null) { 
			this.type_declaration_superinterfaces_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT) != null) { 
			this.method_declaration_arguments_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_ARGUMENTS_ALIGNMENT) != null) { 
			this.message_send_arguments_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_ARGUMENTS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_SELECTOR_ALIGNMENT) != null) { 
			this.message_send_selector_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_SELECTOR_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_THROWS_CLAUSE_ALIGNMENT) != null) { 
			this.method_throws_clause_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_METHOD_THROWS_CLAUSE_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_MEMBER_ALIGNMENT) != null) { 
			this.type_member_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_TYPE_MEMBER_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT) != null) { 
			this.allocation_expression_arguments_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT) != null) { 
			this.qualified_allocation_expression_arguments_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT) != null) { 
			this.array_initializer_expressions_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT) != null) { 
			this.explicit_constructor_arguments_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT) != null) { 
			this.conditional_expression_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_BINARY_EXPRESSION_ALIGNMENT) != null) { 
			this.binary_expression_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_BINARY_EXPRESSION_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY) != null) {
			this.insert_new_line_in_empty_method_body = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION) != null) {
			this.insert_new_line_in_empty_type_declaration = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION) != null) {
			this.insert_new_line_in_empty_anonymous_type_declaration = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK) != null) {
			this.insert_new_line_in_empty_block = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE) != null) { 
			this.number_of_empty_lines_to_preserve = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER) != null) {
			this.insert_new_line_before_closing_brace_in_array_initializer = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_MULTIPLE_FIELDS_ALIGNMENT) != null) { 
			this.multiple_fields_alignment = Integer.parseInt((String)settings.get(DefaultCodeFormatterConstants.FORMATTER_MULTIPLE_FIELDS_ALIGNMENT));
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE) != null) {
			this.insert_space_before_bracket_in_array_reference = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE) != null) {
			this.insert_space_between_brackets_in_array_reference = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BLOCK_STATEMENTS) != null) {
			this.indent_block_statements = settings.get(DefaultCodeFormatterConstants.FORMATTER_INDENT_BLOCK_STATEMENTS).equals(JavaCore.INSERT);
		}
		if (settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER) != null) {
			this.insert_space_before_opening_brace_in_array_initializer = settings.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER).equals(JavaCore.INSERT);
		}
	}
	
	private Integer getAlignment(int alignment) {
		return new Integer(alignment);
	}

	
	public Map getMap() {
		Map options = new HashMap();
		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SEPARATOR, this.line_delimiter);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, this.use_tab ? JavaCore.TAB: JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, Integer.toString(this.tab_size));
		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, Integer.toString(this.page_width));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE, Integer.toString(this.blank_lines_before_package));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE, Integer.toString(this.blank_lines_after_package));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_IMPORTS, Integer.toString(this.blank_lines_before_imports));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_IMPORTS, Integer.toString(this.blank_lines_after_imports));
		options.put(DefaultCodeFormatterConstants.FORMATTER_INITIAL_INDENTATION_LEVEL, Integer.toString(this.initial_indentation_level));
		options.put(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION, Integer.toString(this.continuation_indentation));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, this.type_declaration_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, this.method_declaration_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN, this.insert_space_before_method_declaration_open_paren ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE, this.insert_space_before_type_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE, this.insert_space_before_method_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS, this.insert_space_between_empty_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_ARGUMENT, this.insert_space_before_first_argument ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN, this.insert_space_before_closing_paren ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS, this.insert_space_after_assignment_operators ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS, this.insert_space_before_assignment_operators ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, this.put_empty_statement_on_new_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, this.insert_space_before_semicolon ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND, this.insert_space_within_message_send ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_MESSAGE_SEND, this.insert_space_before_message_send ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FIRST_INITIALIZER, this.insert_space_before_first_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, this.insert_space_before_closing_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, this.block_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE, this.insert_space_before_block_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE, this.insert_space_before_colon_in_case ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST, this.insert_space_after_opening_paren_in_cast ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST, this.insert_space_before_closing_paren_in_cast ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT, this.insert_space_before_colon_in_default ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_WHILE_CONDITION, this.insert_space_in_while_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_IF_CONDITION, this.insert_space_in_if_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMPACT_ELSE_IF, this.compact_else_if ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_IF_CONDITION, this.insert_space_before_if_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_FOR_PAREN, this.insert_space_before_for_paren ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_FOR_PARENS, this.insert_space_in_for_parens ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, switch_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE, this.insert_space_before_switch_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SWITCH_CONDITION, this.insert_space_in_switch_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SWITCH_CONDITION, this.insert_space_before_switch_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION, this.insert_space_in_synchronized_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION, this.insert_space_before_synchronized_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_IN_CATCH_EXPRESSION, this.insert_space_in_catch_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CATCH_EXPRESSION, this.insert_space_before_catch_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_WHILE_CONDITION, this.insert_space_before_while_condition ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, this.insert_new_line_in_control_statements ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR, this.insert_space_before_binary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR, this.insert_space_after_binary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR, this.insert_space_before_unary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR, this.insert_space_after_unary_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS, this.insert_space_before_comma_in_multiple_field_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS, this.insert_space_after_comma_in_multiple_field_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES, this.insert_space_before_comma_in_superinterfaces ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES, this.insert_space_after_comma_in_superinterfaces ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION, this.insert_space_before_comma_in_allocation_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION, this.insert_space_after_comma_in_allocation_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER, this.insert_space_before_comma_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, this.insert_space_after_comma_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT, this.insert_space_before_colon_in_assert ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT, this.insert_space_after_colon_in_assert ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL, this.insert_space_before_question_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL, this.insert_space_after_question_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL, this.insert_space_before_colon_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL, this.insert_space_after_colon_in_conditional ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS, this.insert_space_before_comma_in_constructor_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS, this.insert_space_after_comma_in_constructor_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS, this.insert_space_before_comma_in_constructor_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS, this.insert_space_after_comma_in_constructor_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS, this.insert_space_before_comma_in_for_increments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS, this.insert_space_after_comma_in_for_increments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS, this.insert_space_before_comma_in_explicitconstructorcall_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS, this.insert_space_after_comma_in_explicitconstructorcall_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT, this.insert_space_before_colon_in_labeled_statement ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT, this.insert_space_after_colon_in_labeled_statement ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS, this.insert_space_before_comma_in_messagesend_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS, this.insert_space_after_comma_in_messagesend_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS, this.insert_space_before_comma_in_method_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS, this.insert_space_after_comma_in_method_arguments ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS, this.insert_space_before_comma_in_method_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS, this.insert_space_after_comma_in_method_throws ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS, this.insert_space_before_comma_in_multiple_local_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS, this.insert_space_after_comma_in_multiple_local_declarations ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS, this.insert_space_before_comma_in_for_inits ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS, this.insert_space_after_comma_in_for_inits ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR, this.insert_space_after_semicolon_in_for ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR, this.insert_space_before_postfix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR, this.insert_space_after_postfix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR, this.insert_space_before_prefix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR, this.insert_space_after_prefix_operator ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, this.indent_switchstatements_compare_to_switch ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, this.indent_switchstatements_compare_to_cases ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES, this.indent_breaks_compare_to_cases ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, this.anonymous_type_declaration_brace_position);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE, this.insert_space_before_anonymous_type_open_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER, this.indent_body_declarations_compare_to_type_header ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_FILLING_SPACE, String.valueOf(this.filling_space));
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST, this.insert_space_after_closing_paren_in_cast ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY, Integer.toString(number_of_blank_lines_to_insert_at_beginning_of_method_body));
		options.put(DefaultCodeFormatterConstants.FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE, this.keep_simple_if_on_one_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE, this.format_guardian_clause_on_one_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION, this.insert_space_before_open_paren_in_parenthesized_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION, this.insert_space_after_open_paren_in_parenthesized_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION, this.insert_space_before_closing_paren_in_parenthesized_expression ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE, this.keep_then_statement_on_same_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK, Integer.toString(this.blank_lines_before_new_chunk));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIELD, Integer.toString(this.blank_lines_before_field));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, Integer.toString(this.blank_lines_before_method));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE, Integer.toString(this.blank_lines_before_member_type));
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE, this.insert_space_after_block_close_brace ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE, this.keep_else_statement_on_same_line ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE, this.insert_space_before_bracket_in_array_type_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE, this.insert_space_between_brackets_in_array_type_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_COMPACT_IF_ALIGNMENT, getAlignment(this.compact_if_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT, getAlignment(this.type_declaration_superclass_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT, getAlignment(this.type_declaration_superinterfaces_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT, getAlignment(this.method_declaration_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_ARGUMENTS_ALIGNMENT, getAlignment(this.message_send_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_MESSAGE_SEND_SELECTOR_ALIGNMENT, getAlignment(this.message_send_selector_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_THROWS_CLAUSE_ALIGNMENT, getAlignment(this.method_throws_clause_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_MEMBER_ALIGNMENT, getAlignment(this.type_member_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT, getAlignment(this.allocation_expression_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT, getAlignment(this.qualified_allocation_expression_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT, getAlignment(this.array_initializer_expressions_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT, getAlignment(this.explicit_constructor_arguments_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT, getAlignment(this.conditional_expression_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_BINARY_EXPRESSION_ALIGNMENT, getAlignment(this.binary_expression_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY, this.insert_new_line_in_empty_method_body ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION, this.insert_new_line_in_empty_type_declaration ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION, this.insert_new_line_in_empty_anonymous_type_declaration ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK, this.insert_new_line_in_empty_block ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, Integer.toString(this.number_of_empty_lines_to_preserve));
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, this.insert_new_line_before_closing_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_MULTIPLE_FIELDS_ALIGNMENT, getAlignment(this.multiple_fields_alignment));
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE, this.insert_space_before_bracket_in_array_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE, this.insert_space_between_brackets_in_array_reference ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BLOCK_STATEMENTS, this.indent_block_statements ? DefaultCodeFormatterConstants.TRUE : DefaultCodeFormatterConstants.FALSE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER, this.insert_space_before_opening_brace_in_array_initializer ? JavaCore.INSERT : JavaCore.DO_NOT_INSERT);
		return options;
	}
}
