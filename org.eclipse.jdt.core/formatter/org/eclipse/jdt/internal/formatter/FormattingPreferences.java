/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
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

import org.eclipse.jdt.internal.formatter.align.Alignment;

public class FormattingPreferences {
	/**
	 * Preferences values
	 */
	public static final String TRUE = "true"; 											//$NON-NLS-1$
	public static final String FALSE = "false"; 										//$NON-NLS-1$
	public static final String END_OF_LINE = "end_of_line";						//$NON-NLS-1$
	public static final String NEXT_LINE = "next_line";							//$NON-NLS-1$
	public static final String NEXT_LINE_SHIFTED = "next_line_shifted";	//$NON-NLS-1$
	public static final char DASH = ' ';//183;
	
	/**
	 * Preferences keys	 */
	public static final String PAGE_WIDTH = "page_width";	//$NON-NLS-1$
	public static final String USE_TAB = "use_tab";	//$NON-NLS-1$
	public static final String TAB_SIZE = "tab_size";	//$NON-NLS-1$
	public static final String LINE_SEPARATOR = "line_delimiter";	//$NON-NLS-1$
	public static final String BLANK_LINES_BEFORE_PACKAGE = "blank_lines_before_package";	//$NON-NLS-1$
	public static final String BLANK_LINES_AFTER_PACKAGE = "blank_lines_after_package";	//$NON-NLS-1$
	public static final String BLANK_LINES_BEFORE_IMPORTS = "blank_lines_before_imports";	//$NON-NLS-1$
	public static final String BLANK_LINES_AFTER_IMPORTS = "blank_lines_after_imports";	//$NON-NLS-1$
	public static final String INITIAL_INDENTATION_LEVEL = "initial_indentation_level";	//$NON-NLS-1$
	public static final String CONTINUATION_INDENTATION = "continuation_indentation";	//$NON-NLS-1$
	public static final String TYPE_DECLARATION_BRACE_POSITION = "type_declaration_brace_position";	//$NON-NLS-1$
	public static final String METHOD_DECLARATION_BRACE_POSITION = "method_declaration_brace_position";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS = "insert_space_after_assignment_operators";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN = "insert_space_before_method_declaration_open_paren";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE = "insert_space_before_type_open_brace";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE = "insert_space_before_method_open_brace";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_FIRST_ARGUMENT = "insert_space_before_first_argument";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_CLOSING_PAREN = "insert_space_before_closing_paren";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS = "insert_space_before_assignment_operators";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_SEMICOLON = "insert_space_before_semicolon";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS = "inset_space_between_empty_arguments";	//$NON-NLS-1$
	public static final String PUT_EMPTY_STATEMENT_ON_NEW_LINE = "put_empty_statement_on_new_line";	//$NON-NLS-1$
	public static final String INSERT_SPACE_WITHIN_MESSAGE_SEND = "insert_space_within_message_send";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_MESSAGE_SEND = "insert_space_before_message_send";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_FIRST_INITIALIZER = "insert_space_before_first_initializer";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = "insert_space_before_closing_brace_in_array_initializer";		//$NON-NLS-1$
	public static final String INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER = "insert_space_between_empty_array_initializer";	//$NON-NLS-1$
	public static final String BLOCK_BRACE_POSITION = "block_brace_position";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE = "insert_space_before_block_open_brace";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COLON_IN_CASE = "insert_space_before_colon_in_case";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST = "insert_space_after_opening_paren_in_cast";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST = "insert_space_before_closing_paren_in_cast";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COLON_IN_DEFAULT = "insert_space_before_colon_in_default";	//$NON-NLS-1$
	public static final String INSERT_SPACE_IN_WHILE_CONDITION = "insert_space_in_while_condition";	//$NON-NLS-1$
	public static final String INSERT_SPACE_IN_IF_CONDITION = "insert_space_in_if_condition";	//$NON-NLS-1$
	public static final String COMPACT_ELSE_IF = "compact_else_if";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_IF_CONDITION = "insert_space_before_if_condition";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_FOR_PAREN = "insert_space_before_for_paren";	//$NON-NLS-1$
	public static final String INSERT_SPACE_IN_FOR_PARENS = "insert_space_in_for_parens";	//$NON-NLS-1$
	public static final String SWITCH_BRACE_POSITION = "switch_brace_position";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE = "insert_space_before_switch_open_brace";	//$NON-NLS-1$
	public static final String INSERT_SPACE_IN_SWITCH_CONDITION = "insert_space_in_switch_condition";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_SWITCH_CONDITION = "insert_space_before_switch_condition";	//$NON-NLS-1$
	public static final String INSERT_SPACE_IN_SYNCHRONIZED_CONDITION = "insert_space_in_synchronized_condition";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION = "insert_space_before_synchronized_condition";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_CATCH_EXPRESSION = "insert_space_before_catch_expression";	//$NON-NLS-1$
	public static final String INSERT_SPACE_IN_CATCH_EXPRESSION = "insert_space_in_catch_expression";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_WHILE_CONDITION = "insert_space_before_while_condition";	//$NON-NLS-1$
	public static final String INSERT_NEW_LINE_IN_CONTROL_STATEMENTS = "insert_new_line_in_control_statements";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_BINARY_OPERATOR = "insert_space_before_binary_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_BINARY_OPERATOR = "insert_space_after_binary_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_UNARY_OPERATOR = "insert_space_before_unary_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_UNARY_OPERATOR = "insert_space_after_unary_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = "insert_space_before_comma_in_multiple_field_declarations";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = "insert_space_after_comma_in_multiple_field_declarations";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES = "insert_space_before_comma__in_superinterfaces";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES = "insert_space_after_comma__in_superinterfaces";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION = "insert_space_before_comma_in_allocation_expression";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION = "insert_space_after_comma_in_allocation_expression";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER = "insert_space_before_comma_in_array_initializer";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER = "insert_space_after_comma_in_array_initializer";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COLON_IN_ASSERT = "insert_space_before_colon_in_assert";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COLON_IN_ASSERT = "insert_space_after_colon_in_assert";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL = "insert_space_before_question_in_conditional";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL = "insert_space_after_question_in_conditional";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL = "insert_space_before_colon_in_conditional";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL = "insert_space_after_colon_in_conditional";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS = "insert_space_before_comma_in_constructor_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS = "insert_space_after_comma_in_constructor_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS = "insert_space_before_comma_in_constructor_throws";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS = "insert_space_after_comma_in_constructor_throws";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS = "insert_space_before_comma_in_for_increments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS = "insert_space_after_comma_in_for_increments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = "insert_space_before_comma_in_explicitconstructorcall_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = "insert_space_after_comma_in_explicitconstructorcall_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT = "insert_space_before_colon_in_labeled_statement";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT = "insert_space_after_colon_in_labeled_statement";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS = "insert_space_before_comma_in_messagesend_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS = "insert_space_after_comma_in_messagesend_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS = "insert_space_before_comma_in_method_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS = "insert_space_after_comma_in_method_arguments";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS = "insert_space_before_comma_in_method_throws";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS = "insert_space_after_comma_in_method_throws";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = "insert_space_before_comma_in_multiple_local_declarations";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = "insert_space_after_comma_in_multiple_local_declarations";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS = "insert_space_before_comma_in_for_inits";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS = "insert_space_after_comma_in_for_inits";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_SEMICOLON_IN_FOR = "insert_space_after_semicolon_in_for";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_POSTFIX_OPERATOR = "insert_space_before_postfix_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_POSTFIX_OPERATOR = "insert_space_after_postfix_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_PREFIX_OPERATOR = "insert_space_before_prefix_operator";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_PREFIX_OPERATOR = "insert_space_after_prefix_operator";	//$NON-NLS-1$
	public static final String INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = "indent_switchstatements_compare_to_switch";	//$NON-NLS-1$
	public static final String INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = "indent_switchstatements_compare_to_cases";	//$NON-NLS-1$
	public static final String INDENT_BREAKS_COMPARE_TO_CASES = "indent_breaks_compare_to_cases";	//$NON-NLS-1$
	public static final String ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION = "anonymous_type_declaration_brace_position";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE = "insert_space_before_anonymous_type_open_brace"; 	//$NON-NLS-1$
	public static final String INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER = "indent_body_declarations_compare_to_type_header";	//$NON-NLS-1$
	public static final String FILLING_SPACE = "filling_space";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST = "insert_space_after_closing_paren_in_cast";	//$NON-NLS-1$
	public static final String NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY = "number_of_blank_lines_to_insert_at_beginning_of_method_body"; //$NON-NLS-1$
	public static final String KEEP_SIMPLE_IF_ON_ONE_LINE = "keep_imple_if_on_one_line"; //$NON-NLS-1$
	public static final String FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE = "format_guardian_clause_on_one_line";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION = "insert_space_before_open_paren_in_parenthesized_expression"; //$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION = "insert_space_after_open_paren_in_parenthesized_expression"; //$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION = "insert_space_before_closing_paren_in_parenthesized_expression"; //$NON-NLS-1$
	public static final String KEEP_THEN_STATEMENT_ON_SAME_LINE = "keep_then_statement_on_same_line";//$NON-NLS-1$
	public static final String KEEP_ELSE_STATEMENT_ON_SAME_LINE = "keep_else_statement_on_same_line"; //$NON-NLS-1$
	public static final String BLANK_LINES_BEFORE_NEW_CHUNK = "blank_lines_before_new_chunk";	//$NON-NLS-1$
	public static final String BLANK_LINES_BEFORE_FIELD = "blank_lines_before_field";	//$NON-NLS-1$
	public static final String BLANK_LINES_BEFORE_METHOD = "blank_lines_before_method";	//$NON-NLS-1$
	public static final String BLANK_LINES_BEFORE_MEMBER_TYPE = "blank_lines_before_member_type";	//$NON-NLS-1$
	public static final String INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE = "insert_space_after_block_close_brace";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE = "insert_space_before_bracket_in_array_type_reference";	//$NON-NLS-1$
	public static final String INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE = "insert_space_between_brackets_in_array_type_reference";	//$NON-NLS-1$
	public static final String COMPACT_IF_ALIGNMENT = "compact_if_alignment";	//$NON-NLS-1$
	public static final String TYPE_DECLARATION_SUPERCLASS_ALIGNMENT = "type_declaration_superclass_alignment";	//$NON-NLS-1$
	public static final String TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT = "type_declaration_superinterfaces_alignment";	//$NON-NLS-1$
	public static final String METHOD_DECLARATION_ARGUMENTS_ALIGNMENT = "method_declaration_arguments_alignment";	//$NON-NLS-1$
	public static final String MESSAGE_SEND_ARGUMENTS_ALIGNMENT = "message_send_arguments_alignment";	//$NON-NLS-1$
	public static final String MESSAGE_SEND_SELECTOR_ALIGNMENT = "message_send_selector_alignment";	//$NON-NLS-1$
	public static final String METHOD_THROWS_CLAUSE_ALIGNMENT = "method_throws_clause_alignment";	//$NON-NLS-1$
	public static final String TYPE_MEMBER_ALIGNMENT = "type_member_alignment";	//$NON-NLS-1$
	public static final String ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = "allocation_expression_arguments_alignment";	//$NON-NLS-1$
	public static final String QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = "qualified_allocation_expression_arguments_alignment";	//$NON-NLS-1$
	public static final String ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT = "array_initializer_expressions_alignment";	//$NON-NLS-1$
	public static final String EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT = "explicit_constructor_arguments_alignment";	//$NON-NLS-1$
	public static final String CONDITIONAL_EXPRESSION_ALIGNMENT = "conditional_expression_alignment";	//$NON-NLS-1$
	// TODO update the code formatter preview with these options
	public static final String BINARY_EXPRESSION_ALIGNMENT = "binary_expression_alignment";	//$NON-NLS-1$
	public static final String INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY = "insert_new_line_in_empty_method_body";	//$NON-NLS-1$
	public static final String INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION = "insert_new_line_in_empty_type_declaration";	//$NON-NLS-1$
	public static final String INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION = "insert_new_line_in_empty_anonymous_type_declaration";	//$NON-NLS-1$
	public static final String INSERT_NEW_LINE_IN_EMPTY_BLOCK = "insert_new_line_in_empty_block";	//$NON-NLS-1$
	public static final String NUMBER_OF_EMPTY_LINES_TO_PRESERVE = "number_of_empty_lines_to_preserve";	//$NON-NLS-1$

	/**
	 * Preferences defaults value	 */	
	public static final int DEFAULT_PAGE_WIDTH = 80;
	public static final boolean DEFAULT_USE_TAB = true;
	public static final int DEFAULT_TAB_SIZE = 4;
	public static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");	//$NON-NLS-1$
	public static final int DEFAULT_BLANK_LINES_BEFORE_PACKAGE = 0;
	public static final int DEFAULT_BLANK_LINES_AFTER_PACKAGE = 1;
	public static final int DEFAULT_BLANK_LINES_BEFORE_IMPORTS = 1;
	public static final int DEFAULT_BLANK_LINES_AFTER_IMPORTS = 1;
	public static final int DEFAULT_INITIAL_INDENTATION_LEVEL = 0;
	public static final int DEFAULT_CONTINUATION_INDENTATION = 2; // 2 indentations
	public static final String DEFAULT_TYPE_DECLARATION_BRACE_POSITION = END_OF_LINE;
	public static final String DEFAULT_METHOD_DECLARATION_BRACE_POSITION = END_OF_LINE;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_FIRST_ARGUMENT = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS = true;
	public static final boolean DEFAULT_PUT_EMPTY_STATEMENT_ON_NEW_LINE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SEMICOLON = false;
	public static final boolean DEFAULT_INSERT_SPACE_WITHIN_MESSAGE_SEND = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_MESSAGE_SEND = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_FIRST_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER = false;	
	public static final String DEFAULT_BLOCK_BRACE_POSITION = END_OF_LINE;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CASE = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_WHILE_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_IN_IF_CONDITION = false;
	public static final boolean DEFAULT_COMPACT_ELSE_IF = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_IF_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_FOR_PAREN = true;	
	public static final boolean DEFAULT_INSERT_SPACE_IN_FOR_PARENS = false;
	public static final String DEFAULT_SWITCH_BRACE_POSITION = END_OF_LINE;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_IN_SWITCH_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SWITCH_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CATCH_EXPRESSION = true;
	public static final boolean DEFAULT_INSERT_SPACE_IN_CATCH_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_WHILE_CONDITION = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BINARY_OPERATOR = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_BINARY_OPERATOR = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_UNARY_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_UNARY_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_ASSERT = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COLON_IN_ASSERT = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS = true;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_POSTFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_PREFIX_OPERATOR = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_PREFIX_OPERATOR = false;
	public static final boolean DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = true;
	public static final boolean DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = true;
	public static final boolean DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES = true;
	public static final String DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION = END_OF_LINE;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE = false; 
	public static final boolean DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER = true;
	public static final char DEFAULT_FILLING_SPACE = DASH;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST = true;
	public static final int DEFAULT_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY = 0;
	public static final boolean DEFAULT_KEEP_SIMPLE_IF_ON_ONE_LINE = true; 
	public static final boolean DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION = false;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION = false;
	public static final boolean DEFAULT_KEEP_THEN_STATEMENT_ON_SAME_LINE = true;
	public static final boolean DEFAULT_KEEP_ELSE_STATEMENT_ON_SAME_LINE = true;
	public static final int DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK = 1;
	public static final int DEFAULT_BLANK_LINES_BEFORE_FIELD = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_METHOD = 0;
	public static final int DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE = 0;
	public static final boolean DEFAULT_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE = true;
	public static final boolean DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE = false;
	public static final boolean DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE = false;
	public static final int DEFAULT_COMPACT_IF_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_INDENT_BY_ONE;
	public static final int DEFAULT_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT = Alignment.M_NEXT_SHIFTED_SPLIT;
	public static final int DEFAULT_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT = Alignment.M_NEXT_SHIFTED_SPLIT;
	public static final int DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN;
	public static final int DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT;
	public static final int DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT;
	public static final int DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
	public static final int DEFAULT_TYPE_MEMBER_ALIGNMENT = Alignment.M_MULTICOLUMN;
	public static final int DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN;
	public static final int DEFAULT_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN;
	public static final int DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT = Alignment.M_COMPACT_SPLIT;
	public static final int DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN;
	public static final int DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT = Alignment.M_NEXT_PER_LINE_SPLIT;
	public static final int DEFAULT_BINARY_EXPRESSION_ALIGNMENT = Alignment.M_ONE_PER_LINE_SPLIT;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION = true;
	public static final boolean DEFAULT_INSERT_NEW_LINE_IN_EMPTY_BLOCK = true;
	public static final int DEFAULT_NUMBER_OF_EMPTY_LINES_TO_PRESERVE = 1;

	public int page_width;
	public boolean use_tab;
	public int tab_size;
	public String line_delimiter;
	public int blank_lines_before_package;
	public int blank_lines_after_package;
	public int blank_lines_before_imports;
	public int blank_lines_after_imports;
	public int initial_indentation_level;
	public int continuation_indentation;
	public String type_declaration_brace_position;
	public String method_declaration_brace_position;
	public boolean insert_space_before_method_declaration_open_paren;
	public boolean insert_space_before_type_open_brace;
	public boolean insert_space_before_method_open_brace;
	public boolean insert_space_between_empty_arguments;
	public boolean insert_space_before_first_argument;
	public boolean insert_space_before_closing_paren;
	public boolean insert_space_after_assignment_operators;
	public boolean insert_space_before_assignment_operators;
	public boolean put_empty_statement_on_new_line; 
	public boolean insert_space_before_semicolon;
	public boolean insert_space_within_message_send;
	public boolean insert_space_before_message_send;
	public boolean insert_space_before_first_initializer;
	public boolean insert_space_before_closing_brace_in_array_initializer;
	public boolean insert_space_between_empty_array_initializer;
	public String block_brace_position;
	public boolean insert_space_before_block_open_brace;
	public boolean insert_space_before_colon_in_case;
	public boolean insert_space_before_colon_in_default;
	public boolean insert_space_after_opening_paren_in_cast;
	public boolean insert_space_before_closing_paren_in_cast;
	public boolean insert_space_in_while_condition;
	public boolean insert_space_in_if_condition;
	public boolean compact_else_if;
	public boolean insert_space_before_if_condition;
	public boolean insert_space_before_for_paren;
	public boolean insert_space_in_for_parens;
	public String switch_brace_position;
	public boolean insert_space_before_switch_open_brace;
	public boolean insert_space_before_switch_condition;
	public boolean insert_space_in_switch_condition;
	public boolean insert_space_before_synchronized_condition;
	public boolean insert_space_in_synchronized_condition;
	public boolean insert_space_before_catch_expression;
	public boolean insert_space_in_catch_expression;
	public boolean insert_space_before_while_condition;
	public boolean insert_new_line_in_control_statements;
	public boolean insert_space_before_binary_operator;
	public boolean insert_space_after_binary_operator;
	public boolean insert_space_before_unary_operator;
	public boolean insert_space_after_unary_operator;
	public boolean insert_space_before_comma_in_multiple_field_declarations;
	public boolean insert_space_after_comma_in_multiple_field_declarations;
	public boolean insert_space_before_comma_in_superinterfaces;
	public boolean insert_space_after_comma_in_superinterfaces;
	public boolean insert_space_before_comma_in_allocation_expression;
	public boolean insert_space_after_comma_in_allocation_expression;
	public boolean insert_space_before_comma_in_array_initializer;
	public boolean insert_space_after_comma_in_array_initializer;
	public boolean insert_space_before_colon_in_assert;
	public boolean insert_space_after_colon_in_assert;
	public boolean insert_space_before_question_in_conditional;
	public boolean insert_space_after_question_in_conditional;
	public boolean insert_space_before_colon_in_conditional;
	public boolean insert_space_after_colon_in_conditional;
	public boolean insert_space_before_comma_in_constructor_arguments;
	public boolean insert_space_after_comma_in_constructor_arguments;
	public boolean insert_space_before_comma_in_constructor_throws;
	public boolean insert_space_after_comma_in_constructor_throws;
	public boolean insert_space_before_comma_in_for_increments;
	public boolean insert_space_after_comma_in_for_increments;
	public boolean insert_space_before_comma_in_explicitconstructorcall_arguments;
	public boolean insert_space_after_comma_in_explicitconstructorcall_arguments;
	public boolean insert_space_before_colon_in_labeled_statement;
	public boolean insert_space_after_colon_in_labeled_statement;
	public boolean insert_space_before_comma_in_messagesend_arguments;
	public boolean insert_space_after_comma_in_messagesend_arguments;
	public boolean insert_space_before_comma_in_method_arguments;
	public boolean insert_space_after_comma_in_method_arguments;
	public boolean insert_space_before_comma_in_method_throws;
	public boolean insert_space_after_comma_in_method_throws;
	public boolean insert_space_before_comma_in_multiple_local_declarations;
	public boolean insert_space_after_comma_in_multiple_local_declarations;
	public boolean insert_space_before_comma_in_for_inits;
	public boolean insert_space_after_comma_in_for_inits;
	public boolean insert_space_after_semicolon_in_for;
	public boolean insert_space_before_postfix_operator;
	public boolean insert_space_after_postfix_operator;
	public boolean insert_space_before_prefix_operator;
	public boolean insert_space_after_prefix_operator;
	public boolean indent_switchstatements_compare_to_switch;
	public boolean indent_switchstatements_compare_to_cases;
	public boolean indent_breaks_compare_to_cases;
	public String anonymous_type_declaration_brace_position;
	public boolean insert_space_before_anonymous_type_open_brace;
	public boolean indent_body_declarations_compare_to_type_header;
	public char filling_space;
	public boolean insert_space_after_closing_paren_in_cast;
	public int number_of_blank_lines_to_insert_at_beginning_of_method_body;
	public boolean keep_simple_if_on_one_line;
	public boolean format_guardian_clause_on_one_line;
	public boolean insert_space_before_open_paren_in_parenthesized_expression;
	public boolean insert_space_after_open_paren_in_parenthesized_expression;
	public boolean insert_space_before_closing_paren_in_parenthesized_expression;
	public boolean keep_then_statement_on_same_line;
	public boolean keep_else_statement_on_same_line;
	public int blank_lines_before_new_chunk;
	public int blank_lines_before_field;
	public int blank_lines_before_method;
	public int blank_lines_before_member_type;
	public boolean insert_space_after_block_close_brace;
	public boolean insert_space_before_bracket_in_array_type_reference;
	public boolean insert_space_between_brackets_in_array_type_reference;
	public int compact_if_alignment;
	public int type_declaration_superclass_alignment;
	public int type_declaration_superinterfaces_alignment;
	public int method_declaration_arguments_alignment;
	public int message_send_arguments_alignment;
	public int message_send_selector_alignment;
	public int method_throws_clause_alignment;
	public int type_member_alignment;
	public int allocation_expression_arguments_alignment;
	public int qualified_allocation_expression_arguments_alignment;
	public int array_initializer_expressions_alignment;
	public int explicit_constructor_arguments_alignment;
	public int conditional_expression_alignment;
	public int binary_expression_alignment;
	public boolean insert_new_line_in_empty_method_body;
	public boolean insert_new_line_in_empty_type_declaration;
	public boolean insert_new_line_in_empty_anonymous_type_declaration;
	public boolean insert_new_line_in_empty_block;
	public int number_of_empty_lines_to_preserve;

	private FormattingPreferences() {
	}

	public FormattingPreferences(Map map) {
		if (map.get(LINE_SEPARATOR) != null) {
			this.line_delimiter = (String) map.get(LINE_SEPARATOR);
		} else {
			this.line_delimiter = DEFAULT_LINE_SEPARATOR;			
		}
		if (map.get(USE_TAB) != null) { 
			this.use_tab = Boolean.valueOf((String)map.get(USE_TAB)).booleanValue();
		} else {
			this.use_tab = DEFAULT_USE_TAB;
		}
		if (map.get(TAB_SIZE) != null) { 
			this.tab_size = Integer.parseInt((String)map.get(TAB_SIZE));
		} else {
			this.tab_size = DEFAULT_TAB_SIZE;
		}
		if (map.get(PAGE_WIDTH) != null) { 
			this.page_width = Integer.parseInt((String)map.get(PAGE_WIDTH));
		} else {
			this.page_width = DEFAULT_PAGE_WIDTH;
		}
		if (map.get(BLANK_LINES_BEFORE_PACKAGE) != null) { 
			this.blank_lines_before_package = Integer.parseInt((String)map.get(BLANK_LINES_BEFORE_PACKAGE));
		} else {
			this.blank_lines_before_package = DEFAULT_BLANK_LINES_BEFORE_PACKAGE;
		}
		if (map.get(BLANK_LINES_AFTER_PACKAGE) != null) { 
			this.blank_lines_after_package = Integer.parseInt((String)map.get(BLANK_LINES_AFTER_PACKAGE));
		} else {
			this.blank_lines_after_package = DEFAULT_BLANK_LINES_AFTER_PACKAGE;
		}
		if (map.get(BLANK_LINES_BEFORE_IMPORTS) != null) { 
			this.blank_lines_before_imports = Integer.parseInt((String)map.get(BLANK_LINES_BEFORE_IMPORTS));
		} else {
			this.blank_lines_before_imports = DEFAULT_BLANK_LINES_BEFORE_IMPORTS;
		}
		if (map.get(BLANK_LINES_AFTER_IMPORTS) != null) { 
			this.blank_lines_after_imports = Integer.parseInt((String)map.get(BLANK_LINES_AFTER_IMPORTS));
		} else {
			this.blank_lines_after_imports = DEFAULT_BLANK_LINES_AFTER_IMPORTS;
		}
		if (map.get(INITIAL_INDENTATION_LEVEL) != null) { 
			this.initial_indentation_level = Integer.parseInt((String)map.get(INITIAL_INDENTATION_LEVEL));
		} else {
			this.initial_indentation_level = DEFAULT_INITIAL_INDENTATION_LEVEL;
		}
		if (map.get(CONTINUATION_INDENTATION) != null) { 
			this.continuation_indentation = Integer.parseInt((String)map.get(CONTINUATION_INDENTATION));
		} else {
			this.continuation_indentation = this.use_tab ? DEFAULT_CONTINUATION_INDENTATION : DEFAULT_CONTINUATION_INDENTATION * this.tab_size;
		}
		if (map.get(TYPE_DECLARATION_BRACE_POSITION) != null) { 
			this.type_declaration_brace_position = (String) map.get(TYPE_DECLARATION_BRACE_POSITION);
		} else {
			this.type_declaration_brace_position = DEFAULT_TYPE_DECLARATION_BRACE_POSITION;
		}
		if (map.get(METHOD_DECLARATION_BRACE_POSITION) != null) { 
			this.method_declaration_brace_position = (String) map.get(METHOD_DECLARATION_BRACE_POSITION);
		} else {
			this.method_declaration_brace_position = DEFAULT_METHOD_DECLARATION_BRACE_POSITION;
		}		
		if (map.get(INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN) != null) { 
			this.insert_space_before_method_declaration_open_paren = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN)).booleanValue();
		} else {
			this.insert_space_before_method_declaration_open_paren = DEFAULT_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN;
		}
		if (map.get(INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE) != null) { 
			this.insert_space_before_type_open_brace = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE)).booleanValue();
		} else {
			this.insert_space_before_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE;
		}
		if (map.get(INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE) != null) { 
			this.insert_space_before_method_open_brace = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE)).booleanValue();
		} else {
			this.insert_space_before_method_open_brace = DEFAULT_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE;
		}
		if (map.get(INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS) != null) { 
			this.insert_space_between_empty_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_between_empty_arguments = DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_FIRST_ARGUMENT) != null) { 
			this.insert_space_before_first_argument = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_FIRST_ARGUMENT)).booleanValue();
		} else {
			this.insert_space_before_first_argument = DEFAULT_INSERT_SPACE_BEFORE_FIRST_ARGUMENT;
		}
		if (map.get(INSERT_SPACE_BEFORE_CLOSING_PAREN) != null) { 
			this.insert_space_before_closing_paren = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_CLOSING_PAREN)).booleanValue();
		} else {
			this.insert_space_before_closing_paren = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN;
		}
		if (map.get(INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS) != null) { 
			this.insert_space_after_assignment_operators = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS)).booleanValue();
		} else {
			this.insert_space_after_assignment_operators = DEFAULT_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS;
		}
		if (map.get(INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS) != null) { 
			this.insert_space_before_assignment_operators = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS)).booleanValue();
		} else {
			this.insert_space_before_assignment_operators = DEFAULT_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS;
		}								
		if (map.get(PUT_EMPTY_STATEMENT_ON_NEW_LINE) != null) { 
			this.put_empty_statement_on_new_line = Boolean.valueOf((String)map.get(PUT_EMPTY_STATEMENT_ON_NEW_LINE)).booleanValue();
		} else {
			this.put_empty_statement_on_new_line = DEFAULT_PUT_EMPTY_STATEMENT_ON_NEW_LINE;
		}								
		if (map.get(INSERT_SPACE_BEFORE_SEMICOLON) != null) { 
			this.insert_space_before_semicolon = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_SEMICOLON)).booleanValue();
		} else {
			this.insert_space_before_semicolon = DEFAULT_INSERT_SPACE_BEFORE_SEMICOLON;
		}
		if (map.get(INSERT_SPACE_WITHIN_MESSAGE_SEND) != null) { 
			this.insert_space_within_message_send = Boolean.valueOf((String)map.get(INSERT_SPACE_WITHIN_MESSAGE_SEND)).booleanValue();
		} else {
			this.insert_space_within_message_send = DEFAULT_INSERT_SPACE_WITHIN_MESSAGE_SEND;
		}
		if (map.get(INSERT_SPACE_BEFORE_MESSAGE_SEND) != null) { 
			this.insert_space_before_message_send = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_MESSAGE_SEND)).booleanValue();
		} else {
			this.insert_space_before_message_send = DEFAULT_INSERT_SPACE_BEFORE_MESSAGE_SEND;
		}
		if (map.get(INSERT_SPACE_BEFORE_FIRST_INITIALIZER) != null) { 
			this.insert_space_before_first_initializer = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_FIRST_INITIALIZER)).booleanValue();
		} else {
			this.insert_space_before_first_initializer = DEFAULT_INSERT_SPACE_BEFORE_FIRST_INITIALIZER;
		}		
		if (map.get(INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER) != null) { 
			this.insert_space_before_closing_brace_in_array_initializer = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER)).booleanValue();
		} else {
			this.insert_space_before_closing_brace_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER;
		}		
		if (map.get(BLOCK_BRACE_POSITION) != null) { 
			this.block_brace_position = (String) map.get(BLOCK_BRACE_POSITION);
		} else {
			this.block_brace_position = DEFAULT_BLOCK_BRACE_POSITION;
		}
		if (map.get(INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE) != null) { 
			this.insert_space_before_block_open_brace = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE)).booleanValue();
		} else {
			this.insert_space_before_block_open_brace = DEFAULT_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE;
		}		
		if (map.get(INSERT_SPACE_BEFORE_COLON_IN_CASE) != null) { 
			this.insert_space_before_colon_in_case = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COLON_IN_CASE)).booleanValue();
		} else {
			this.insert_space_before_colon_in_case = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CASE;
		}		
		if (map.get(INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST) != null) { 
			this.insert_space_after_opening_paren_in_cast = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST)).booleanValue();
		} else {
			this.insert_space_after_opening_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST;
		}		
		if (map.get(INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST) != null) { 
			this.insert_space_before_closing_paren_in_cast = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST)).booleanValue();
		} else {
			this.insert_space_before_closing_paren_in_cast = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST;
		}		
		if (map.get(INSERT_SPACE_BEFORE_COLON_IN_DEFAULT) != null) { 
			this.insert_space_before_colon_in_default = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COLON_IN_DEFAULT)).booleanValue();
		} else {
			this.insert_space_before_colon_in_default = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT;
		}		
		if (map.get(INSERT_SPACE_IN_WHILE_CONDITION) != null) { 
			this.insert_space_in_while_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_IN_WHILE_CONDITION)).booleanValue();
		} else {
			this.insert_space_in_while_condition = DEFAULT_INSERT_SPACE_IN_WHILE_CONDITION;
		}		
		if (map.get(INSERT_SPACE_IN_IF_CONDITION) != null) { 
			this.insert_space_in_if_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_IN_IF_CONDITION)).booleanValue();
		} else {
			this.insert_space_in_if_condition = DEFAULT_INSERT_SPACE_IN_IF_CONDITION;
		}		
		if (map.get(COMPACT_ELSE_IF) != null) { 
			this.compact_else_if = Boolean.valueOf((String)map.get(COMPACT_ELSE_IF)).booleanValue();
		} else {
			this.compact_else_if = DEFAULT_COMPACT_ELSE_IF;
		}		
		if (map.get(INSERT_SPACE_BEFORE_IF_CONDITION) != null) { 
			this.insert_space_before_if_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_IF_CONDITION)).booleanValue();
		} else {
			this.insert_space_before_if_condition = DEFAULT_INSERT_SPACE_BEFORE_IF_CONDITION;
		}
		if (map.get(INSERT_SPACE_BEFORE_FOR_PAREN) != null) { 
			this.insert_space_before_for_paren = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_FOR_PAREN)).booleanValue();
		} else {
			this.insert_space_before_for_paren = DEFAULT_INSERT_SPACE_BEFORE_FOR_PAREN;
		}
		if (map.get(INSERT_SPACE_IN_FOR_PARENS) != null) { 
			this.insert_space_in_for_parens = Boolean.valueOf((String)map.get(INSERT_SPACE_IN_FOR_PARENS)).booleanValue();
		} else {
			this.insert_space_in_for_parens = DEFAULT_INSERT_SPACE_IN_FOR_PARENS;
		}
		if (map.get(SWITCH_BRACE_POSITION) != null) { 
			this.switch_brace_position = (String) map.get(SWITCH_BRACE_POSITION);
		} else {
			this.switch_brace_position = DEFAULT_SWITCH_BRACE_POSITION;
		}
		if (map.get(INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE) != null) { 
			this.insert_space_before_switch_open_brace = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE)).booleanValue();
		} else {
			this.insert_space_before_switch_open_brace = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE;
		}		
		if (map.get(INSERT_SPACE_IN_SWITCH_CONDITION) != null) { 
			this.insert_space_in_switch_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_IN_SWITCH_CONDITION)).booleanValue();
		} else {
			this.insert_space_in_switch_condition = DEFAULT_INSERT_SPACE_IN_SWITCH_CONDITION;
		}		
		if (map.get(INSERT_SPACE_BEFORE_SWITCH_CONDITION) != null) { 
			this.insert_space_before_switch_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_SWITCH_CONDITION)).booleanValue();
		} else {
			this.insert_space_before_switch_condition = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_CONDITION;
		}
		if (map.get(INSERT_SPACE_IN_SYNCHRONIZED_CONDITION) != null) { 
			this.insert_space_in_synchronized_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_IN_SYNCHRONIZED_CONDITION)).booleanValue();
		} else {
			this.insert_space_in_synchronized_condition = DEFAULT_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION;
		}		
		if (map.get(INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION) != null) { 
			this.insert_space_before_synchronized_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION)).booleanValue();
		} else {
			this.insert_space_before_synchronized_condition = DEFAULT_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION;
		}
		if (map.get(INSERT_SPACE_IN_CATCH_EXPRESSION) != null) { 
			this.insert_space_in_catch_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_IN_CATCH_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_in_catch_expression = DEFAULT_INSERT_SPACE_IN_CATCH_EXPRESSION;
		}		
		if (map.get(INSERT_SPACE_BEFORE_CATCH_EXPRESSION) != null) { 
			this.insert_space_before_catch_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_CATCH_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_before_catch_expression = DEFAULT_INSERT_SPACE_BEFORE_CATCH_EXPRESSION;
		}
		if (map.get(INSERT_SPACE_BEFORE_WHILE_CONDITION) != null) { 
			this.insert_space_before_while_condition = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_WHILE_CONDITION)).booleanValue();
		} else {
			this.insert_space_before_while_condition = DEFAULT_INSERT_SPACE_BEFORE_WHILE_CONDITION;
		}
		if (map.get(INSERT_NEW_LINE_IN_CONTROL_STATEMENTS) != null) { 
			this.insert_new_line_in_control_statements = Boolean.valueOf((String)map.get(INSERT_NEW_LINE_IN_CONTROL_STATEMENTS)).booleanValue();
		} else {
			this.insert_new_line_in_control_statements = DEFAULT_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_BINARY_OPERATOR) != null) { 
			this.insert_space_before_binary_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_BINARY_OPERATOR)).booleanValue();
		} else {
			this.insert_space_before_binary_operator = DEFAULT_INSERT_SPACE_BEFORE_BINARY_OPERATOR;
		}
		if (map.get(INSERT_SPACE_AFTER_BINARY_OPERATOR) != null) { 
			this.insert_space_after_binary_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_BINARY_OPERATOR)).booleanValue();
		} else {
			this.insert_space_after_binary_operator = DEFAULT_INSERT_SPACE_AFTER_BINARY_OPERATOR;
		}
		if (map.get(INSERT_SPACE_BEFORE_UNARY_OPERATOR) != null) { 
			this.insert_space_before_unary_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_UNARY_OPERATOR)).booleanValue();
		} else {
			this.insert_space_before_unary_operator = DEFAULT_INSERT_SPACE_BEFORE_UNARY_OPERATOR;
		}
		if (map.get(INSERT_SPACE_AFTER_UNARY_OPERATOR) != null) { 
			this.insert_space_after_unary_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_UNARY_OPERATOR)).booleanValue();
		} else {
			this.insert_space_after_unary_operator = DEFAULT_INSERT_SPACE_AFTER_UNARY_OPERATOR;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS) != null) { 
			this.insert_space_before_comma_in_multiple_field_declarations = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS) != null) { 
			this.insert_space_after_comma_in_multiple_field_declarations = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES) != null) { 
			this.insert_space_before_comma_in_superinterfaces = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES)).booleanValue();
		} else {
			this.insert_space_before_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES) != null) { 
			this.insert_space_after_comma_in_superinterfaces = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES)).booleanValue();
		} else {
			this.insert_space_after_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION) != null) { 
			this.insert_space_before_comma_in_allocation_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_before_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION) != null) { 
			this.insert_space_after_comma_in_allocation_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_after_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER) != null) { 
			this.insert_space_before_comma_in_array_initializer = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER)).booleanValue();
		} else {
			this.insert_space_before_comma_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER) != null) { 
			this.insert_space_after_comma_in_array_initializer = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER)).booleanValue();
		} else {
			this.insert_space_after_comma_in_array_initializer = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER;
		}
		if (map.get(INSERT_SPACE_BEFORE_COLON_IN_ASSERT) != null) { 
			this.insert_space_before_colon_in_assert = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COLON_IN_ASSERT)).booleanValue();
		} else {
			this.insert_space_before_colon_in_assert = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_ASSERT;
		}
		if (map.get(INSERT_SPACE_AFTER_COLON_IN_ASSERT) != null) { 
			this.insert_space_after_colon_in_assert = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COLON_IN_ASSERT)).booleanValue();
		} else {
			this.insert_space_after_colon_in_assert = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_ASSERT;
		}
		if (map.get(INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL) != null) { 
			this.insert_space_before_question_in_conditional = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL)).booleanValue();
		} else {
			this.insert_space_before_question_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL;
		}
		if (map.get(INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL) != null) { 
			this.insert_space_after_question_in_conditional = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL)).booleanValue();
		} else {
			this.insert_space_after_question_in_conditional = DEFAULT_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL;
		}
		if (map.get(INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL) != null) { 
			this.insert_space_before_colon_in_conditional = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL)).booleanValue();
		} else {
			this.insert_space_before_colon_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL;
		}
		if (map.get(INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL) != null) { 
			this.insert_space_after_colon_in_conditional = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL)).booleanValue();
		} else {
			this.insert_space_after_colon_in_conditional = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_constructor_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_constructor_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS) != null) { 
			this.insert_space_before_comma_in_constructor_throws = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS) != null) { 
			this.insert_space_after_comma_in_constructor_throws = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS) != null) { 
			this.insert_space_before_comma_in_for_increments = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_for_increments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS) != null) { 
			this.insert_space_after_comma_in_for_increments = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_for_increments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_explicitconstructorcall_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_explicitconstructorcall_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT) != null) { 
			this.insert_space_before_colon_in_labeled_statement = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT)).booleanValue();
		} else {
			this.insert_space_before_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT;
		}
		if (map.get(INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT) != null) { 
			this.insert_space_after_colon_in_labeled_statement = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT)).booleanValue();
		} else {
			this.insert_space_after_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_messagesend_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_messagesend_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS) != null) { 
			this.insert_space_before_comma_in_method_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_method_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS) != null) { 
			this.insert_space_after_comma_in_method_arguments = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_method_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS) != null) { 
			this.insert_space_before_comma_in_method_throws = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_method_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS) != null) { 
			this.insert_space_after_comma_in_method_throws = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_method_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS) != null) { 
			this.insert_space_before_comma_in_multiple_local_declarations = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS) != null) { 
			this.insert_space_after_comma_in_multiple_local_declarations = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		}
		if (map.get(INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS) != null) { 
			this.insert_space_before_comma_in_for_inits = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS)).booleanValue();
		} else {
			this.insert_space_before_comma_in_for_inits = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS;
		}
		if (map.get(INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS) != null) { 
			this.insert_space_after_comma_in_for_inits = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS)).booleanValue();
		} else {
			this.insert_space_after_comma_in_for_inits = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS;
		}
		if (map.get(INSERT_SPACE_AFTER_SEMICOLON_IN_FOR) != null) { 
			this.insert_space_after_semicolon_in_for = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_SEMICOLON_IN_FOR)).booleanValue();
		} else {
			this.insert_space_after_semicolon_in_for = DEFAULT_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR;
		}
		if (map.get(INSERT_SPACE_BEFORE_POSTFIX_OPERATOR) != null) { 
			this.insert_space_before_postfix_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_POSTFIX_OPERATOR)).booleanValue();
		} else {
			this.insert_space_before_postfix_operator = DEFAULT_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR;
		}
		if (map.get(INSERT_SPACE_AFTER_POSTFIX_OPERATOR) != null) { 
			this.insert_space_after_postfix_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_POSTFIX_OPERATOR)).booleanValue();
		} else {
			this.insert_space_after_postfix_operator = DEFAULT_INSERT_SPACE_AFTER_POSTFIX_OPERATOR;
		}
		if (map.get(INSERT_SPACE_BEFORE_PREFIX_OPERATOR) != null) { 
			this.insert_space_before_prefix_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_PREFIX_OPERATOR)).booleanValue();
		} else {
			this.insert_space_before_prefix_operator = DEFAULT_INSERT_SPACE_BEFORE_PREFIX_OPERATOR;
		}
		if (map.get(INSERT_SPACE_AFTER_PREFIX_OPERATOR) != null) { 
			this.insert_space_after_prefix_operator = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_PREFIX_OPERATOR)).booleanValue();
		} else {
			this.insert_space_after_prefix_operator = DEFAULT_INSERT_SPACE_AFTER_PREFIX_OPERATOR;
		}
		if (map.get(INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH) != null) { 
			this.indent_switchstatements_compare_to_switch = Boolean.valueOf((String)map.get(INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH)).booleanValue();
		} else {
			this.indent_switchstatements_compare_to_switch = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH;
		}
		if (map.get(INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES) != null) { 
			this.indent_switchstatements_compare_to_cases = Boolean.valueOf((String)map.get(INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES)).booleanValue();
		} else {
			this.indent_switchstatements_compare_to_cases = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES;
		}
		if (map.get(INDENT_BREAKS_COMPARE_TO_CASES) != null) { 
			this.indent_breaks_compare_to_cases = Boolean.valueOf((String)map.get(INDENT_BREAKS_COMPARE_TO_CASES)).booleanValue();
		} else {
			this.indent_breaks_compare_to_cases = DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES;
		}		
		if (map.get(ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION) != null) { 
			this.anonymous_type_declaration_brace_position = (String) map.get(ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION);
		} else {
			this.anonymous_type_declaration_brace_position = DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION;
		}
		if (map.get(INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE) != null) { 
			this.insert_space_before_anonymous_type_open_brace = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE)).booleanValue();
		} else {
			this.insert_space_before_anonymous_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE;
		}
		if (map.get(INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER) != null) { 
			this.indent_body_declarations_compare_to_type_header = Boolean.valueOf((String)map.get(INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER)).booleanValue();
		} else {
			this.indent_body_declarations_compare_to_type_header = DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER;
		}
		if (map.get(FILLING_SPACE) != null) { 
			this.filling_space = ((String)map.get(FILLING_SPACE)).charAt(0);
		} else {
			this.filling_space = DEFAULT_FILLING_SPACE;
		}
		if (map.get(INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST) != null) { 
			this.insert_space_after_closing_paren_in_cast = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST)).booleanValue();
		} else {
			this.insert_space_after_closing_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST;
		}
		if (map.get(NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY) != null) { 
			this.number_of_blank_lines_to_insert_at_beginning_of_method_body = Integer.parseInt((String)map.get(NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY));
		} else {
			this.number_of_blank_lines_to_insert_at_beginning_of_method_body = DEFAULT_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY;
		}
		if (map.get(KEEP_SIMPLE_IF_ON_ONE_LINE) != null) { 
			this.keep_simple_if_on_one_line = Boolean.valueOf((String)map.get(KEEP_SIMPLE_IF_ON_ONE_LINE)).booleanValue();
		} else {
			this.keep_simple_if_on_one_line = DEFAULT_KEEP_SIMPLE_IF_ON_ONE_LINE;
		}
		if (map.get(FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE) != null) { 
			this.format_guardian_clause_on_one_line = Boolean.valueOf((String)map.get(FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE)).booleanValue();
		} else {
			this.format_guardian_clause_on_one_line = DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE;
		}
		if (map.get(INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION) != null) { 
			this.insert_space_before_open_paren_in_parenthesized_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_before_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION;
		}
		if (map.get(INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION) != null) { 
			this.insert_space_after_open_paren_in_parenthesized_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_after_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION;
		}
		if (map.get(INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION) != null) { 
			this.insert_space_before_closing_paren_in_parenthesized_expression = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION)).booleanValue();
		} else {
			this.insert_space_before_closing_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION;
		}
		if (map.get(KEEP_THEN_STATEMENT_ON_SAME_LINE) != null) { 
			this.keep_then_statement_on_same_line = Boolean.valueOf((String)map.get(KEEP_THEN_STATEMENT_ON_SAME_LINE)).booleanValue();
		} else {
			this.keep_then_statement_on_same_line = DEFAULT_KEEP_THEN_STATEMENT_ON_SAME_LINE;
		}
		if (map.get(BLANK_LINES_BEFORE_NEW_CHUNK) != null) { 
			this.blank_lines_before_new_chunk = Integer.parseInt((String)map.get(BLANK_LINES_BEFORE_NEW_CHUNK));
		} else {
			this.blank_lines_before_new_chunk = DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK;
		}
		if (map.get(BLANK_LINES_BEFORE_FIELD) != null) { 
			this.blank_lines_before_field = Integer.parseInt((String)map.get(BLANK_LINES_BEFORE_FIELD));
		} else {
			this.blank_lines_before_field = DEFAULT_BLANK_LINES_BEFORE_FIELD;
		}
		if (map.get(BLANK_LINES_BEFORE_METHOD) != null) { 
			this.blank_lines_before_method = Integer.parseInt((String)map.get(BLANK_LINES_BEFORE_METHOD));
		} else {
			this.blank_lines_before_method = DEFAULT_BLANK_LINES_BEFORE_METHOD;
		}
		if (map.get(BLANK_LINES_BEFORE_MEMBER_TYPE) != null) { 
			this.blank_lines_before_member_type = Integer.parseInt((String)map.get(BLANK_LINES_BEFORE_MEMBER_TYPE));
		} else {
			this.blank_lines_before_member_type = DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE;
		}
		if (map.get(INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE) != null) {
			this.insert_space_after_block_close_brace = Boolean.valueOf((String)map.get(INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE)).booleanValue();
		} else {
			this.insert_space_after_block_close_brace = DEFAULT_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE;
		}
		if (map.get(KEEP_ELSE_STATEMENT_ON_SAME_LINE) != null) {
			this.keep_else_statement_on_same_line = Boolean.valueOf((String)map.get(KEEP_ELSE_STATEMENT_ON_SAME_LINE)).booleanValue();
		} else {
			this.keep_else_statement_on_same_line = DEFAULT_KEEP_ELSE_STATEMENT_ON_SAME_LINE;
		}
		if (map.get(INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE) != null) {
			this.insert_space_before_bracket_in_array_type_reference = Boolean.valueOf((String)map.get(INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE)).booleanValue();
		} else {
			this.insert_space_before_bracket_in_array_type_reference = DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE;
		}
		if (map.get(INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE) != null) {
			this.insert_space_between_brackets_in_array_type_reference = Boolean.valueOf((String)map.get(INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE)).booleanValue();
		} else {
			this.insert_space_between_brackets_in_array_type_reference = DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE;
		}
		if (map.get(COMPACT_IF_ALIGNMENT) != null) { 
			this.compact_if_alignment = Integer.parseInt((String)map.get(COMPACT_IF_ALIGNMENT));
		} else {
			this.compact_if_alignment = DEFAULT_COMPACT_IF_ALIGNMENT;
		}
		if (map.get(TYPE_DECLARATION_SUPERCLASS_ALIGNMENT) != null) { 
			this.type_declaration_superclass_alignment = Integer.parseInt((String)map.get(TYPE_DECLARATION_SUPERCLASS_ALIGNMENT));
		} else {
			this.type_declaration_superclass_alignment = DEFAULT_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT;
		}
		if (map.get(TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT) != null) { 
			this.type_declaration_superinterfaces_alignment = Integer.parseInt((String)map.get(TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT));
		} else {
			this.type_declaration_superinterfaces_alignment = DEFAULT_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT;
		}
		if (map.get(METHOD_DECLARATION_ARGUMENTS_ALIGNMENT) != null) { 
			this.method_declaration_arguments_alignment = Integer.parseInt((String)map.get(METHOD_DECLARATION_ARGUMENTS_ALIGNMENT));
		} else {
			this.method_declaration_arguments_alignment = DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT;
		}
		if (map.get(MESSAGE_SEND_ARGUMENTS_ALIGNMENT) != null) { 
			this.message_send_arguments_alignment = Integer.parseInt((String)map.get(MESSAGE_SEND_ARGUMENTS_ALIGNMENT));
		} else {
			this.message_send_arguments_alignment = DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT;
		}
		if (map.get(MESSAGE_SEND_SELECTOR_ALIGNMENT) != null) { 
			this.message_send_selector_alignment = Integer.parseInt((String)map.get(MESSAGE_SEND_SELECTOR_ALIGNMENT));
		} else {
			this.message_send_selector_alignment = DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT;
		}
		if (map.get(METHOD_THROWS_CLAUSE_ALIGNMENT) != null) { 
			this.method_throws_clause_alignment = Integer.parseInt((String)map.get(METHOD_THROWS_CLAUSE_ALIGNMENT));
		} else {
			this.method_throws_clause_alignment = DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT;
		}
		if (map.get(TYPE_MEMBER_ALIGNMENT) != null) { 
			this.type_member_alignment = Integer.parseInt((String)map.get(TYPE_MEMBER_ALIGNMENT));
		} else {
			this.type_member_alignment = DEFAULT_TYPE_MEMBER_ALIGNMENT;
		}
		if (map.get(ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT) != null) { 
			this.allocation_expression_arguments_alignment = Integer.parseInt((String)map.get(ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT));
		} else {
			this.allocation_expression_arguments_alignment = DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		}
		if (map.get(QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT) != null) { 
			this.qualified_allocation_expression_arguments_alignment = Integer.parseInt((String)map.get(QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT));
		} else {
			this.qualified_allocation_expression_arguments_alignment = DEFAULT_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		}
		if (map.get(ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT) != null) { 
			this.array_initializer_expressions_alignment = Integer.parseInt((String)map.get(ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT));
		} else {
			this.array_initializer_expressions_alignment = DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT;
		}
		if (map.get(EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT) != null) { 
			this.explicit_constructor_arguments_alignment = Integer.parseInt((String)map.get(EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT));
		} else {
			this.explicit_constructor_arguments_alignment = DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT;
		}
		if (map.get(CONDITIONAL_EXPRESSION_ALIGNMENT) != null) { 
			this.conditional_expression_alignment = Integer.parseInt((String)map.get(CONDITIONAL_EXPRESSION_ALIGNMENT));
		} else {
			this.conditional_expression_alignment = DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT;
		}
		if (map.get(BINARY_EXPRESSION_ALIGNMENT) != null) { 
			this.binary_expression_alignment = Integer.parseInt((String)map.get(BINARY_EXPRESSION_ALIGNMENT));
		} else {
			this.binary_expression_alignment = DEFAULT_BINARY_EXPRESSION_ALIGNMENT;
		}
		if (map.get(INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY) != null) {
			this.insert_new_line_in_empty_method_body = Boolean.valueOf((String)map.get(INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY)).booleanValue();
		} else {
			this.insert_new_line_in_empty_method_body = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY;
		}
		if (map.get(INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION) != null) {
			this.insert_new_line_in_empty_type_declaration = Boolean.valueOf((String)map.get(INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION)).booleanValue();
		} else {
			this.insert_new_line_in_empty_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION;
		}
		if (map.get(INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION) != null) {
			this.insert_new_line_in_empty_anonymous_type_declaration = Boolean.valueOf((String)map.get(INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION)).booleanValue();
		} else {
			this.insert_new_line_in_empty_anonymous_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION;
		}
		if (map.get(INSERT_NEW_LINE_IN_EMPTY_BLOCK) != null) {
			this.insert_new_line_in_empty_block = Boolean.valueOf((String)map.get(INSERT_NEW_LINE_IN_EMPTY_BLOCK)).booleanValue();
		} else {
			this.insert_new_line_in_empty_block = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_BLOCK;
		}
		if (map.get(NUMBER_OF_EMPTY_LINES_TO_PRESERVE) != null) { 
			this.number_of_empty_lines_to_preserve = Integer.parseInt((String)map.get(NUMBER_OF_EMPTY_LINES_TO_PRESERVE));
		} else {
			this.number_of_empty_lines_to_preserve = DEFAULT_NUMBER_OF_EMPTY_LINES_TO_PRESERVE;
		}
	}
		
	public static FormattingPreferences getDefault() {
		FormattingPreferences defaults = new FormattingPreferences();
		defaults.use_tab = DEFAULT_USE_TAB;
		defaults.tab_size = DEFAULT_TAB_SIZE;
		defaults.page_width = DEFAULT_PAGE_WIDTH;
		defaults.blank_lines_before_package = DEFAULT_BLANK_LINES_BEFORE_PACKAGE;
		defaults.blank_lines_after_package = DEFAULT_BLANK_LINES_AFTER_PACKAGE;
		defaults.blank_lines_before_imports= DEFAULT_BLANK_LINES_BEFORE_PACKAGE;
		defaults.blank_lines_after_imports = DEFAULT_BLANK_LINES_AFTER_IMPORTS;
		defaults.initial_indentation_level = DEFAULT_INITIAL_INDENTATION_LEVEL;
		defaults.line_delimiter = DEFAULT_LINE_SEPARATOR;
		defaults.continuation_indentation = DEFAULT_CONTINUATION_INDENTATION;
		defaults.type_declaration_brace_position = DEFAULT_TYPE_DECLARATION_BRACE_POSITION;
		defaults.method_declaration_brace_position = DEFAULT_METHOD_DECLARATION_BRACE_POSITION;
		defaults.insert_space_before_method_declaration_open_paren = DEFAULT_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN;
		defaults.insert_space_before_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE;
		defaults.insert_space_before_method_open_brace = DEFAULT_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE;
		defaults.insert_space_between_empty_arguments = DEFAULT_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS;
		defaults.insert_space_before_first_argument = DEFAULT_INSERT_SPACE_BEFORE_FIRST_ARGUMENT;
		defaults.insert_space_before_closing_paren = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN;
		defaults.insert_space_before_assignment_operators = DEFAULT_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS;
		defaults.insert_space_after_assignment_operators = DEFAULT_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS;
		defaults.put_empty_statement_on_new_line = DEFAULT_PUT_EMPTY_STATEMENT_ON_NEW_LINE;
		defaults.insert_space_before_semicolon = DEFAULT_INSERT_SPACE_BEFORE_SEMICOLON;
		defaults.insert_space_within_message_send = DEFAULT_INSERT_SPACE_WITHIN_MESSAGE_SEND;
		defaults.insert_space_before_message_send = DEFAULT_INSERT_SPACE_BEFORE_MESSAGE_SEND;
		defaults.insert_space_before_first_initializer = DEFAULT_INSERT_SPACE_BEFORE_FIRST_INITIALIZER;
		defaults.insert_space_before_closing_brace_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER;
		defaults.block_brace_position = DEFAULT_BLOCK_BRACE_POSITION;
		defaults.insert_space_before_block_open_brace = DEFAULT_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE;
		defaults.insert_space_before_colon_in_case = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CASE;
		defaults.insert_space_after_opening_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST;
		defaults.insert_space_before_closing_paren_in_cast = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST;
		defaults.insert_space_before_colon_in_default = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT;
		defaults.insert_space_in_while_condition = DEFAULT_INSERT_SPACE_IN_WHILE_CONDITION;
		defaults.insert_space_in_if_condition = DEFAULT_INSERT_SPACE_IN_IF_CONDITION;
		defaults.compact_else_if = DEFAULT_COMPACT_ELSE_IF;
		defaults.insert_space_before_if_condition = DEFAULT_INSERT_SPACE_BEFORE_IF_CONDITION;		
		defaults.insert_space_before_for_paren = DEFAULT_INSERT_SPACE_BEFORE_FOR_PAREN;		
		defaults.insert_space_in_for_parens = DEFAULT_INSERT_SPACE_IN_FOR_PARENS;
		defaults.switch_brace_position = DEFAULT_SWITCH_BRACE_POSITION;
		defaults.insert_space_before_switch_open_brace = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE;
		defaults.insert_space_in_switch_condition = DEFAULT_INSERT_SPACE_IN_SWITCH_CONDITION;
		defaults.insert_space_before_switch_condition = DEFAULT_INSERT_SPACE_BEFORE_SWITCH_CONDITION;
		defaults.insert_space_in_synchronized_condition = DEFAULT_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION;
		defaults.insert_space_before_synchronized_condition = DEFAULT_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION;
		defaults.insert_space_in_catch_expression = DEFAULT_INSERT_SPACE_IN_CATCH_EXPRESSION;
		defaults.insert_space_before_catch_expression = DEFAULT_INSERT_SPACE_BEFORE_CATCH_EXPRESSION;
		defaults.insert_space_before_while_condition = DEFAULT_INSERT_SPACE_BEFORE_WHILE_CONDITION;
		defaults.insert_new_line_in_control_statements = DEFAULT_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS;
		defaults.insert_space_before_binary_operator = DEFAULT_INSERT_SPACE_BEFORE_BINARY_OPERATOR;
		defaults.insert_space_after_binary_operator = DEFAULT_INSERT_SPACE_AFTER_BINARY_OPERATOR;
		defaults.insert_space_before_unary_operator = DEFAULT_INSERT_SPACE_BEFORE_UNARY_OPERATOR;
		defaults.insert_space_after_unary_operator = DEFAULT_INSERT_SPACE_AFTER_UNARY_OPERATOR;
		defaults.insert_space_before_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		defaults.insert_space_after_comma_in_multiple_field_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS;
		defaults.insert_space_before_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES;
		defaults.insert_space_after_comma_in_superinterfaces = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES;
		defaults.insert_space_before_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION;
		defaults.insert_space_after_comma_in_allocation_expression = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION;
		defaults.insert_space_before_comma_in_array_initializer = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER;
		defaults.insert_space_after_comma_in_array_initializer = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER;
		defaults.insert_space_before_colon_in_assert = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_ASSERT;
		defaults.insert_space_after_colon_in_assert = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_ASSERT;
		defaults.insert_space_before_question_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL;
		defaults.insert_space_after_question_in_conditional = DEFAULT_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL;
		defaults.insert_space_before_colon_in_conditional = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL;
		defaults.insert_space_after_colon_in_conditional = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL;
		defaults.insert_space_before_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		defaults.insert_space_after_comma_in_constructor_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS;
		defaults.insert_space_before_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS;
		defaults.insert_space_after_comma_in_constructor_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS;
		defaults.insert_space_before_comma_in_for_increments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS;
		defaults.insert_space_after_comma_in_for_increments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS;
		defaults.insert_space_before_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		defaults.insert_space_after_comma_in_explicitconstructorcall_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS;
		defaults.insert_space_before_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT;
		defaults.insert_space_after_colon_in_labeled_statement = DEFAULT_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT;
		defaults.insert_space_before_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS;
		defaults.insert_space_after_comma_in_messagesend_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS;
		defaults.insert_space_before_comma_in_method_arguments = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS;
		defaults.insert_space_after_comma_in_method_arguments = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS;
		defaults.insert_space_before_comma_in_method_throws = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS;
		defaults.insert_space_after_comma_in_method_throws = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS;
		defaults.insert_space_before_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		defaults.insert_space_after_comma_in_multiple_local_declarations = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS;
		defaults.insert_space_before_comma_in_for_inits = DEFAULT_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS;
		defaults.insert_space_after_comma_in_for_inits = DEFAULT_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS;
		defaults.insert_space_after_semicolon_in_for = DEFAULT_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR;
		defaults.insert_space_before_postfix_operator = DEFAULT_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR;
		defaults.insert_space_after_postfix_operator = DEFAULT_INSERT_SPACE_AFTER_POSTFIX_OPERATOR;
		defaults.insert_space_before_prefix_operator = DEFAULT_INSERT_SPACE_BEFORE_PREFIX_OPERATOR;
		defaults.insert_space_after_prefix_operator = DEFAULT_INSERT_SPACE_AFTER_PREFIX_OPERATOR;
		defaults.indent_switchstatements_compare_to_switch = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH;
		defaults.indent_switchstatements_compare_to_cases = DEFAULT_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES;
		defaults.indent_breaks_compare_to_cases = DEFAULT_INDENT_BREAKS_COMPARE_TO_CASES;
		defaults.anonymous_type_declaration_brace_position = DEFAULT_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION;
		defaults.insert_space_before_anonymous_type_open_brace = DEFAULT_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE;
		defaults.indent_body_declarations_compare_to_type_header = DEFAULT_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER;
		defaults.filling_space = DEFAULT_FILLING_SPACE;
		defaults.insert_space_after_closing_paren_in_cast = DEFAULT_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST;
		defaults.number_of_blank_lines_to_insert_at_beginning_of_method_body = DEFAULT_NUMBER_OF_BLANK_LINES_TO_INSERT_AT_BEGINNING_OF_METHOD_BODY;
		defaults.keep_simple_if_on_one_line = DEFAULT_KEEP_SIMPLE_IF_ON_ONE_LINE;
		defaults.format_guardian_clause_on_one_line = DEFAULT_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE;
		defaults.insert_space_before_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION;
		defaults.insert_space_after_open_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHIZED_EXPRESSION;
		defaults.insert_space_before_closing_paren_in_parenthesized_expression = DEFAULT_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHIZED_EXPRESSION;
		defaults.keep_then_statement_on_same_line = DEFAULT_KEEP_THEN_STATEMENT_ON_SAME_LINE;
		defaults.blank_lines_before_new_chunk = DEFAULT_BLANK_LINES_BEFORE_NEW_CHUNK;
		defaults.blank_lines_before_field = DEFAULT_BLANK_LINES_BEFORE_FIELD;
		defaults.blank_lines_before_method = DEFAULT_BLANK_LINES_BEFORE_METHOD;
		defaults.blank_lines_before_member_type = DEFAULT_BLANK_LINES_BEFORE_MEMBER_TYPE;
		defaults.insert_space_after_block_close_brace = DEFAULT_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE;
		defaults.keep_else_statement_on_same_line = DEFAULT_KEEP_ELSE_STATEMENT_ON_SAME_LINE;
		defaults.insert_space_before_bracket_in_array_type_reference = DEFAULT_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE;
		defaults.insert_space_between_brackets_in_array_type_reference = DEFAULT_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE;
		defaults.compact_if_alignment = DEFAULT_COMPACT_IF_ALIGNMENT;
		defaults.type_declaration_superclass_alignment = DEFAULT_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT;
		defaults.type_declaration_superinterfaces_alignment = DEFAULT_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT;
		defaults.method_declaration_arguments_alignment = DEFAULT_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT;
		defaults.message_send_arguments_alignment = DEFAULT_MESSAGE_SEND_ARGUMENTS_ALIGNMENT;
		defaults.message_send_selector_alignment = DEFAULT_MESSAGE_SEND_SELECTOR_ALIGNMENT;
		defaults.method_throws_clause_alignment = DEFAULT_METHOD_THROWS_CLAUSE_ALIGNMENT;
		defaults.type_member_alignment = DEFAULT_TYPE_MEMBER_ALIGNMENT;
		defaults.allocation_expression_arguments_alignment = DEFAULT_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		defaults.qualified_allocation_expression_arguments_alignment = DEFAULT_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT;
		defaults.array_initializer_expressions_alignment = DEFAULT_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT;
		defaults.explicit_constructor_arguments_alignment = DEFAULT_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT;
		defaults.conditional_expression_alignment = DEFAULT_CONDITIONAL_EXPRESSION_ALIGNMENT;
		defaults.binary_expression_alignment = DEFAULT_BINARY_EXPRESSION_ALIGNMENT;
		defaults.insert_new_line_in_empty_method_body = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY;
		defaults.insert_new_line_in_empty_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION;
		defaults.insert_new_line_in_empty_anonymous_type_declaration = DEFAULT_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION;
		defaults.number_of_empty_lines_to_preserve = DEFAULT_NUMBER_OF_EMPTY_LINES_TO_PRESERVE;
		return defaults;
	}

	public static FormattingPreferences getSunSetttings() {
		FormattingPreferences sunSettings = new FormattingPreferences();
		sunSettings.use_tab = false;
		sunSettings.tab_size = 4;
		sunSettings.page_width = 80;
		sunSettings.blank_lines_before_package = 0;
		sunSettings.blank_lines_after_package = 1;
		sunSettings.blank_lines_before_imports= 1;
		sunSettings.blank_lines_after_imports = 1;
		sunSettings.initial_indentation_level = 0;
		sunSettings.line_delimiter = DEFAULT_LINE_SEPARATOR;
		sunSettings.continuation_indentation = 2;
		sunSettings.type_declaration_brace_position = END_OF_LINE;
		sunSettings.method_declaration_brace_position = END_OF_LINE;
		sunSettings.insert_space_before_method_declaration_open_paren = false;
		sunSettings.insert_space_before_type_open_brace = true;
		sunSettings.insert_space_before_method_open_brace = true;
		sunSettings.insert_space_between_empty_arguments = false;
		sunSettings.insert_space_before_first_argument = false;
		sunSettings.insert_space_before_closing_paren = false;
		sunSettings.insert_space_before_assignment_operators = true;
		sunSettings.insert_space_after_assignment_operators = true;
		sunSettings.put_empty_statement_on_new_line = true;
		sunSettings.insert_space_before_semicolon = false;
		sunSettings.insert_space_within_message_send = false;
		sunSettings.insert_space_before_message_send = false;
		sunSettings.insert_space_before_first_initializer = false;
		sunSettings.insert_space_before_closing_brace_in_array_initializer = false;
		sunSettings.block_brace_position = END_OF_LINE;
		sunSettings.insert_space_before_block_open_brace = true;
		sunSettings.insert_space_before_colon_in_case = false;
		sunSettings.insert_space_after_opening_paren_in_cast = false;
		sunSettings.insert_space_before_closing_paren_in_cast = false;
		sunSettings.insert_space_before_colon_in_default = false;
		sunSettings.insert_space_in_while_condition = false;
		sunSettings.insert_space_in_if_condition = false;
		sunSettings.compact_else_if = true;
		sunSettings.insert_space_before_if_condition = true;
		sunSettings.insert_space_before_for_paren = true;		
		sunSettings.insert_space_in_for_parens = false;
		sunSettings.switch_brace_position = END_OF_LINE;
		sunSettings.insert_space_before_switch_open_brace = true;
		sunSettings.insert_space_in_switch_condition = false;
		sunSettings.insert_space_before_switch_condition = true;
		sunSettings.insert_space_in_synchronized_condition = false;
		sunSettings.insert_space_before_synchronized_condition = true;
		sunSettings.insert_space_in_catch_expression = false;
		sunSettings.insert_space_before_catch_expression = true;
		sunSettings.insert_space_before_while_condition = true;
		sunSettings.insert_new_line_in_control_statements = true;
		sunSettings.insert_space_before_binary_operator = true;
		sunSettings.insert_space_after_binary_operator = true;
		sunSettings.insert_space_before_unary_operator = false;
		sunSettings.insert_space_after_unary_operator = false;
		sunSettings.insert_space_before_comma_in_multiple_field_declarations = false;
		sunSettings.insert_space_after_comma_in_multiple_field_declarations = true;
		sunSettings.insert_space_before_comma_in_superinterfaces = false;
		sunSettings.insert_space_after_comma_in_superinterfaces = true;
		sunSettings.insert_space_before_comma_in_allocation_expression = false;
		sunSettings.insert_space_after_comma_in_allocation_expression = true;
		sunSettings.insert_space_before_comma_in_array_initializer = false;
		sunSettings.insert_space_after_comma_in_array_initializer = true;
		sunSettings.insert_space_before_colon_in_assert = true;
		sunSettings.insert_space_after_colon_in_assert = true;
		sunSettings.insert_space_before_question_in_conditional = true;
		sunSettings.insert_space_after_question_in_conditional = true;
		sunSettings.insert_space_before_colon_in_conditional = true;
		sunSettings.insert_space_after_colon_in_conditional = true;
		sunSettings.insert_space_before_comma_in_constructor_arguments = false;
		sunSettings.insert_space_after_comma_in_constructor_arguments = true;
		sunSettings.insert_space_before_comma_in_constructor_throws = false;
		sunSettings.insert_space_after_comma_in_constructor_throws = true;
		sunSettings.insert_space_before_comma_in_for_increments = false;
		sunSettings.insert_space_after_comma_in_for_increments = true;
		sunSettings.insert_space_before_comma_in_explicitconstructorcall_arguments = false;
		sunSettings.insert_space_after_comma_in_explicitconstructorcall_arguments = true;
		sunSettings.insert_space_before_colon_in_labeled_statement = false;
		sunSettings.insert_space_after_colon_in_labeled_statement = true;
		sunSettings.insert_space_before_comma_in_messagesend_arguments = false;
		sunSettings.insert_space_after_comma_in_messagesend_arguments = true;
		sunSettings.insert_space_before_comma_in_method_arguments = false;
		sunSettings.insert_space_after_comma_in_method_arguments = true;
		sunSettings.insert_space_before_comma_in_method_throws = false;
		sunSettings.insert_space_after_comma_in_method_throws = true;
		sunSettings.insert_space_before_comma_in_multiple_local_declarations = false;
		sunSettings.insert_space_after_comma_in_multiple_local_declarations = true;
		sunSettings.insert_space_before_comma_in_for_inits = false;
		sunSettings.insert_space_after_comma_in_for_inits = true;
		sunSettings.insert_space_after_semicolon_in_for = true;
		sunSettings.insert_space_before_postfix_operator = false;
		sunSettings.insert_space_after_postfix_operator = false;
		sunSettings.insert_space_before_prefix_operator = true;
		sunSettings.insert_space_after_prefix_operator = false;
		sunSettings.indent_switchstatements_compare_to_switch = false;
		sunSettings.indent_switchstatements_compare_to_cases = true;
		sunSettings.indent_breaks_compare_to_cases = true;
		sunSettings.anonymous_type_declaration_brace_position = END_OF_LINE;
		sunSettings.insert_space_before_anonymous_type_open_brace = true;
		sunSettings.indent_body_declarations_compare_to_type_header = true;
		sunSettings.filling_space = ' ';
		sunSettings.insert_space_after_closing_paren_in_cast = true;
		sunSettings.number_of_blank_lines_to_insert_at_beginning_of_method_body = 0;
		sunSettings.keep_simple_if_on_one_line = true;
		sunSettings.format_guardian_clause_on_one_line = true;
		sunSettings.insert_space_before_open_paren_in_parenthesized_expression = false;
		sunSettings.insert_space_after_open_paren_in_parenthesized_expression = false;
		sunSettings.insert_space_before_closing_paren_in_parenthesized_expression = false;
		sunSettings.keep_then_statement_on_same_line = true;
		sunSettings.blank_lines_before_new_chunk = 1;
		sunSettings.blank_lines_before_field = 1;
		sunSettings.blank_lines_before_method = 1;
		sunSettings.blank_lines_before_member_type = 1;
		sunSettings.insert_space_after_block_close_brace = true;
		sunSettings.keep_else_statement_on_same_line = false;
		sunSettings.insert_space_before_bracket_in_array_type_reference = false;
		sunSettings.insert_space_between_brackets_in_array_type_reference = false;
		sunSettings.compact_if_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.type_declaration_superclass_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.type_declaration_superinterfaces_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.method_declaration_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.message_send_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.message_send_selector_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.method_throws_clause_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		sunSettings.allocation_expression_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.qualified_allocation_expression_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.explicit_constructor_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		sunSettings.conditional_expression_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		sunSettings.binary_expression_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		sunSettings.insert_new_line_in_empty_method_body = true;
		sunSettings.insert_new_line_in_empty_type_declaration = true;
		sunSettings.insert_new_line_in_empty_anonymous_type_declaration = true;
		sunSettings.number_of_empty_lines_to_preserve = 1;
		return sunSettings;
	}
}
