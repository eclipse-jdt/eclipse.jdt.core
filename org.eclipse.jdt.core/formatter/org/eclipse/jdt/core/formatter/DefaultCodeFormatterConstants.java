/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.formatter;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.align.Alignment;

/**
 * This is still subject to changes before 3.0.
 * @since 3.0
 */
public class DefaultCodeFormatterConstants {

	public static final String END_OF_LINE = "end_of_line";	//$NON-NLS-1$
	public static final String NEXT_LINE = "next_line"; //$NON-NLS-1$
	public static final String NEXT_LINE_SHIFTED = "next_line_shifted";	//$NON-NLS-1$
    public static final String NEXT_LINE_ON_WRAP = "next_line_on_wrap"; //$NON-NLS-1$

    public static final String FALSE = "false"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$

	public static final int WRAP_NO_SPLIT= 0;
	public static final int WRAP_COMPACT= 1;
	public static final int WRAP_COMPACT_FIRST_BREAK= 2;
	public static final int WRAP_ONE_PER_LINE= 3;
	public static final int WRAP_NEXT_SHIFTED= 4;
	public static final int WRAP_NEXT_PER_LINE= 5;
	
	public static final int INDENT_DEFAULT= 0;
	public static final int INDENT_ON_COLUMN = 1;
	public static final int INDENT_BY_ONE= 2;
	
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_arguments_in_allocation_expression";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_EXPLICIT_CONSTRUCTOR_CALL = JavaCore.PLUGIN_ID + ".formatter.alignment_for_arguments_in_explicit_constructor_call";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_arguments_in_method_invocation";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_QUALIFIED_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_arguments_in_qualified_allocation_expression";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_BINARY_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_binary_expression";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_COMPACT_IF = JavaCore.PLUGIN_ID + ".formatter.alignment_for_compact_if";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_conditional_expression";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.alignment_for_expressions_in_array_initializer";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_MULTIPLE_FIELDS = JavaCore.PLUGIN_ID + ".formatter.alignment_for_multiple_fields";//$NON-NLS-1$	
	public static final String FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_parameters_in_constructor_declaration";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_parameters_in_method_declaration";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_SELECTOR_IN_METHOD_INVOCATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_selector_in_method_invocation";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_SUPERCLASS_IN_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_superclass_in_type_declaration";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_superinterfaces_in_type_declaration";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_throws_clause_in_constructor_declaration";	 //$NON-NLS-1$
	public static final String FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.alignment_for_throws_clause_in_method_declaration";	 //$NON-NLS-1$
	
	public static final String FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS = JavaCore.PLUGIN_ID + ".align_type_members_on_columns";	 //$NON-NLS-1$
	
	public static final String FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_anonymous_type_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_array_initializer";	//$NON-NLS-1$
	public static final String FORMATTER_BRACE_POSITION_FOR_BLOCK = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_block";	//$NON-NLS-1$
	public static final String FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_constructor_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_method_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_type_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_BRACE_POSITION_FOR_SWITCH = JavaCore.PLUGIN_ID + ".formatter.brace_position_for_switch";	//$NON-NLS-1$

	public static final String FORMATTER_CONTINUATION_INDENTATION = JavaCore.PLUGIN_ID + ".formatter.continuation_indentation";	//$NON-NLS-1$
	public static final String FORMATTER_CONTINUATION_INDENTATION_FOR_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.continuation_indentation_for_array_initializer";	//$NON-NLS-1$

	public static final String FORMATTER_BLANK_LINES_AFTER_IMPORTS = JavaCore.PLUGIN_ID + ".formatter.blank_lines_after_imports";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_AFTER_PACKAGE = JavaCore.PLUGIN_ID + ".formatter.blank_lines_after_package";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_FIELD = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_field";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_first_class_body_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_IMPORTS = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_imports";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_member_type";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_METHOD = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_method";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_new_chunk";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BEFORE_PACKAGE = JavaCore.PLUGIN_ID + ".formatter.blank_lines_before_package";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_BETWEEN_TYPE_DECLARATIONS = JavaCore.PLUGIN_ID + ".formatter.blank_lines_between_type_declarations";	//$NON-NLS-1$
	public static final String FORMATTER_BLANK_LINES_AT_BEGINNING_OF_METHOD_BODY = JavaCore.PLUGIN_ID + ".formatter.number_of_blank_lines_at_beginning_of_method_body"; //$NON-NLS-1$

	public static final String FORMATTER_INDENT_BLOCK_STATEMENTS = JavaCore.PLUGIN_ID + ".formatter.indent_block_statements"; //$NON-NLS-1$
	public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER = JavaCore.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_type_header";	//$NON-NLS-1$
	public static final String FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES = JavaCore.PLUGIN_ID + ".formatter.indent_breaks_compare_to_cases";	//$NON-NLS-1$
	public static final String FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = JavaCore.PLUGIN_ID + ".formatter.indent_switchstatements_compare_to_cases";	//$NON-NLS-1$
	public static final String FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = JavaCore.PLUGIN_ID + ".formatter.indent_switchstatements_compare_to_switch";	//$NON-NLS-1$

	public static final String FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_after_opening_brace_in_array_initializer";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_before_closing_brace_in_array_initializer";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_in_control_statements";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_in_empty_anonymous_type_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_in_empty_block";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_in_empty_method_body";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_new_line_in_empty_type_declaration";	//$NON-NLS-1$

	
	public static final String FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_assignment_operator"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_binary_operator"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_closing_paren_in_cast"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_closing_brace_in_block"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_colon_in_assert"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_colon_in_conditional"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_colon_in_labeled_statement"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_allocation_expression"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_array_initializer"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_DECLARATION_PARAMETERS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_constructor_declaration_parameters"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_DECLARATION_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_constructor_declaration_throws"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICIT_CONSTRUCTOR_CALL_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_explicitconstructorcall_arguments"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_for_increments"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_for_inits"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_invocation_arguments"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_declaration_parameters"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_declaration_throws"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_multiple_field_declarations"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_multiple_local_declarations"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma__in_superinterfaces"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET_IN_ARRAY_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_bracket_in_array_allocation_expression";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET_IN_ARRAY_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_bracket_in_array_reference";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_brace_in_array_initializer";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_cast"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_catch"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_constructor_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_for"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_if"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_method_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_method_invocation"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_parenthesized_expression"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_switch"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SYNCHRONIZED = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_synchronized"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_while"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_postfix_operator"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_prefix_operator"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_question_in_conditional"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_semicolon_in_for"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_unary_operator"; //$NON-NLS-1$
	
	public static final String FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_assignment_operator";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_binary_operator";	//$NON-NLS-1$

	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_brace_in_array_initializer";		//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET_IN_ARRAY_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_bracket_in_array_allocation_expression";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET_IN_ARRAY_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_bracket_in_array_reference";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_cast";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_catch";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_constructor_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_for";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_if";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_method_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_method_invocation"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_parenthesized_expression"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_switch";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SYNCHRONIZED = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_synchronized";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_while";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_colon_in_assert";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_colon_in_case";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_colon_in_conditional";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_colon_in_default";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_colon_in_labeled_statement";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_allocation_expression";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_array_initializer";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_DECLARATION_PARAMETERS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_constructor_declaration_parameters";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_DECLARATION_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_constructor_declaration_throws";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICIT_CONSTRUCTOR_CALL_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_explicitconstructorcall_arguments";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_for_increments";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_for_inits";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_invocation_arguments";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_PARAMETERS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_declaration_parameters";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_declaration_throws";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_multiple_field_declarations";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_multiple_local_declarations";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma__in_superinterfaces";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ANONYMOUS_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_anonymous_type_declaration"; 	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_array_initializer"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_block";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_constructor_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_method_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_switch";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_type_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_bracket_in_array_allocation_expression";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_bracket_in_array_reference";//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_TYPE_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_bracket_in_array_type_reference";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_catch";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_constructor_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_for";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_if";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_method_invocation";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_method_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_switch";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SYNCHRONIZED = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_synchronized";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_parenthesized_expression"; //$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_while";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_postfix_operator";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_prefix_operator";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_question_in_conditional";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_semicolon";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON_IN_FOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_semicolon_in_for";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_unary_operator";	//$NON-NLS-1$

	public static final String FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_brackets_in_array_type_reference";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACES_IN_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_braces_in_array_initializer";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACKETS_IN_ARRAY_ALLOCATION_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_brackets_in_array_allocation_expression";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_CONSTRUCTOR_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_constructor_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_method_declaration";	//$NON-NLS-1$
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_arguments_in_method_invocation";	//$NON-NLS-1$

	public static final String FORMATTER_COMPACT_ELSE_IF = JavaCore.PLUGIN_ID + ".formatter.compact_else_if";	//$NON-NLS-1$
	public static final String FORMATTER_KEEP_GUARDIAN_CLAUSE_ON_ONE_LINE = JavaCore.PLUGIN_ID + ".formatter.format_guardian_clause_on_one_line";	//$NON-NLS-1$
	public static final String FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE = JavaCore.PLUGIN_ID + ".formatter.keep_else_statement_on_same_line"; //$NON-NLS-1$
	public static final String FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE = JavaCore.PLUGIN_ID + ".formatter.keep_imple_if_on_one_line"; //$NON-NLS-1$
	public static final String FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE = JavaCore.PLUGIN_ID + ".formatter.keep_then_statement_on_same_line";//$NON-NLS-1$


	public static final String FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE = JavaCore.PLUGIN_ID + ".formatter.number_of_empty_lines_to_preserve";	//$NON-NLS-1$
	public static final String FORMATTER_PRESERVE_USER_LINEBREAKS = JavaCore.PLUGIN_ID + ".formatter.preserve_user_linebreaks";//$NON-NLS-1$

	public static final String FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE = JavaCore.PLUGIN_ID + ".formatter.put_empty_statement_on_new_line";	//$NON-NLS-1$

	public static final String FORMATTER_REMOVE_UNNECESSARY_SEMICOLON = JavaCore.PLUGIN_ID + ".formatter.remove_unnecessary_semicolon";	//$NON-NLS-1$

	public static final String FORMATTER_LINE_SPLIT = JavaCore.PLUGIN_ID + ".formatter.lineSplit"; //$NON-NLS-1$
	public static final String FORMATTER_TAB_CHAR = JavaCore.PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$
	public static final String FORMATTER_TAB_SIZE = JavaCore.PLUGIN_ID + ".formatter.tabulation.size"; //$NON-NLS-1$

	/**
	 * <p>Returns the default settings.</p>
	 * 
	 * <p>This is subject to change before 3.0.</p>
	 * @since 3.0
	 */
	public static Map getDefaultSettings() {
		return DefaultCodeFormatterOptions.getDefaultSettings().getMap();
	}

	/**
	 * <p>Returns the settings according to the Java conventions.</p>
	 * 
	 * <p>This is subject to change before 3.0.</p>
	 * @since 3.0
	 */
	public static Map getJavaConventionsSettings() {
		return DefaultCodeFormatterOptions.getJavaConventionsSettings().getMap();
	}

	public static boolean getForceWrapping(Map options, String key) {
		Object option = options.get(key);
		if (option != null) {
			try {
				int existingValue = Integer.parseInt((String) option);
				return (existingValue & Alignment.M_FORCE) != 0;
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
		return false;
	}

	public static int getIndentStyle(Map options, String key) {
		Object option = options.get(key);
		if (option != null) {
			try {
				int existingValue = Integer.parseInt((String) option);
				if ((existingValue & Alignment.M_INDENT_BY_ONE) != 0) {
					return INDENT_BY_ONE;
				} else if ((existingValue & Alignment.M_INDENT_ON_COLUMN) != 0) {
					return INDENT_ON_COLUMN;
				} else {
					return INDENT_DEFAULT;
				}
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
		return INDENT_DEFAULT;
	}
	
	public static int getWrappingStyle(Map options, String key) {
		Object option = options.get(key);
		if (option != null) {
			try {
				int existingValue = Integer.parseInt((String) option) & Alignment.SPLIT_MASK;
				switch(existingValue) {
					case Alignment.M_COMPACT_SPLIT :
						return WRAP_COMPACT;
					case Alignment.M_COMPACT_FIRST_BREAK_SPLIT :
						return WRAP_COMPACT_FIRST_BREAK;
					case Alignment.M_NEXT_PER_LINE_SPLIT :
						return WRAP_NEXT_PER_LINE;
					case Alignment.M_NEXT_SHIFTED_SPLIT :
						return WRAP_NEXT_SHIFTED;
					case Alignment.M_NO_ALIGNMENT :
						return WRAP_NO_SPLIT;
					case Alignment.M_ONE_PER_LINE_SPLIT :
						return WRAP_ONE_PER_LINE;
				}
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
		return WRAP_NO_SPLIT;
	}
	
	public static void setIndentStyle(Map options, String key, int indentStyle) {
		Object option = options.get(key);
		if (option != null) {
			try {
				int existingValue = Integer.parseInt((String) options.get(key));
				// clear existing indent bits
				existingValue &= ~(Alignment.M_INDENT_BY_ONE | Alignment.M_INDENT_ON_COLUMN);
				switch(indentStyle) {
					case INDENT_BY_ONE :
						existingValue |= Alignment.M_INDENT_BY_ONE;
						break;
					case INDENT_ON_COLUMN :
						existingValue |= Alignment.M_INDENT_ON_COLUMN;
				}
				options.put(key, String.valueOf(existingValue));
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
	}
	
	public static void setForceWrapping(Map options, String key, boolean forceSplit) {
		Object option = options.get(key);
		if (option != null) {
			try {
				int existingValue = Integer.parseInt((String) option);
				// clear existing force bit
				existingValue &= ~Alignment.M_FORCE;
				if (forceSplit) {
					existingValue |= Alignment.M_FORCE;
				}
				options.put(key, String.valueOf(existingValue));
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
	}

	public static void setWrappingStyle(Map options, String key, int splitStyle) {
		Object option = options.get(key);
		if (option != null) {
			try {
				int existingValue = Integer.parseInt((String) option);
				// clear existing split bits
				existingValue &= ~(Alignment.SPLIT_MASK);
				switch(splitStyle) {
					case WRAP_COMPACT :
						existingValue |= Alignment.M_COMPACT_SPLIT;
						break;
					case WRAP_COMPACT_FIRST_BREAK :
						existingValue |= Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
						break;
					case WRAP_NEXT_PER_LINE :
						existingValue |= Alignment.M_NEXT_PER_LINE_SPLIT;
						break;
					case WRAP_NEXT_SHIFTED :
						existingValue |= Alignment.M_NEXT_SHIFTED_SPLIT;
						break;
					case WRAP_NO_SPLIT :
						existingValue = Alignment.M_NO_ALIGNMENT;
						break;
					case WRAP_ONE_PER_LINE :
						existingValue |= Alignment.M_ONE_PER_LINE_SPLIT;
						break;
				}
				options.put(key, String.valueOf(existingValue));
			} catch (NumberFormatException e) {
				// nothing to do
			}
		}
	}	
	
	public static String createAlignmentValue(boolean forceSplit, int wrapStyle, int indentStyle) {
		int alignmentValue = 0; 
		switch(wrapStyle) {
			case WRAP_COMPACT :
				alignmentValue |= Alignment.M_COMPACT_SPLIT;
				break;
			case WRAP_COMPACT_FIRST_BREAK :
				alignmentValue |= Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
				break;
			case WRAP_NEXT_PER_LINE :
				alignmentValue |= Alignment.M_NEXT_PER_LINE_SPLIT;
				break;
			case WRAP_NEXT_SHIFTED :
				alignmentValue |= Alignment.M_NEXT_SHIFTED_SPLIT;
				break;
			case WRAP_NO_SPLIT :
				alignmentValue = Alignment.M_NO_ALIGNMENT;
				break;
			case WRAP_ONE_PER_LINE :
				alignmentValue |= Alignment.M_ONE_PER_LINE_SPLIT;
				break;
		}		
		if (forceSplit) {
			alignmentValue |= Alignment.M_FORCE;
		}
		switch(indentStyle) {
			case INDENT_BY_ONE :
				alignmentValue |= Alignment.M_INDENT_BY_ONE;
				break;
			case INDENT_ON_COLUMN :
				alignmentValue |= Alignment.M_INDENT_ON_COLUMN;
		}
		return String.valueOf(alignmentValue);
	}
	/*
	 * All deprecated fields
	 */
	/*
	 * TODO remove prior to 3.0
	 */
	public static final String FORMATTER_FILLING_SPACE = JavaCore.PLUGIN_ID + ".formatter.filling_space";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_METHOD_DECLARATION_ARGUMENTS_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.method_declaration_arguments_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION instead
	 */
	public static final String FORMATTER_MESSAGE_SEND_ARGUMENTS_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.message_send_arguments_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_SELECTOR_IN_METHOD_INVOCATION instead
	 */
	public static final String FORMATTER_MESSAGE_SEND_SELECTOR_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.message_send_selector_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_QUALIFIED_ALLOCATION_EXPRESSION instead
	 */
	public static final String FORMATTER_QUALIFIED_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.qualified_allocation_expression_arguments_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_SUPERCLASS_IN_TYPE_DECLARATION instead
	 */
	public static final String FORMATTER_TYPE_DECLARATION_SUPERCLASS_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.type_declaration_superclass_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_TYPE_DECLARATION instead
	 */
	public static final String FORMATTER_TYPE_DECLARATION_SUPERINTERFACES_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.type_declaration_superinterfaces_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_METHOD_THROWS_CLAUSE_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.method_throws_clause_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION instead
	 */
	public static final String FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.conditional_expression_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ALLOCATION_EXPRESSION instead
	 */
	public static final String FORMATTER_ALLOCATION_EXPRESSION_ARGUMENTS_ALIGNMENT  = JavaCore.PLUGIN_ID + ".formatter.allocation_expression_arguments_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_COMPACT_IF instead
	 */
	public static final String FORMATTER_COMPACT_IF_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.compact_if_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER instead
	 */
	public static final String FORMATTER_ARRAY_INITIALIZER_EXPRESSIONS_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.array_initializer_expressions_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_BINARY_EXPRESSION instead
	 */
	public static final String FORMATTER_BINARY_EXPRESSION_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.binary_expression_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_EXPLICIT_CONSTRUCTOR_CALL instead
	 */
	public static final String FORMATTER_EXPLICIT_CONSTRUCTOR_ARGUMENTS_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.explicit_constructor_arguments_alignment";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION instead
	 */
	public static final String FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION = JavaCore.PLUGIN_ID + ".formatter.anonymous_type_declaration_brace_position";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER instead
	 */
	public static final String FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION = JavaCore.PLUGIN_ID + ".formatter.array_initializer_brace_position";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_BRACE_POSITION_FOR_BLOCK instead
	 */
	public static final String FORMATTER_BLOCK_BRACE_POSITION = JavaCore.PLUGIN_ID + ".formatter.block_brace_position";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_METHOD_DECLARATION_BRACE_POSITION = JavaCore.PLUGIN_ID + ".formatter.method_declaration_brace_position";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION instead
	 */
	public static final String FORMATTER_TYPE_DECLARATION_BRACE_POSITION = JavaCore.PLUGIN_ID + ".formatter.type_declaration_brace_position";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_BRACE_POSITION_FOR_SWITCH instead
	 */
	public static final String FORMATTER_SWITCH_BRACE_POSITION = JavaCore.PLUGIN_ID + ".formatter.switch_brace_position";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_DECLARATION_PARAMETERS insted
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_constructor_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MESSAGESEND_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_messagesend_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_open_paren_in_parenthesized_expression"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_MESSAGE_SEND = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_message_send"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_MESSAGE_SEND = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_message_send"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_DECLARATION_PARAMETERS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_constructor_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MESSAGESEND_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_messagesend_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_MESSAGE_SEND = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_message_send";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_FIRST_ARGUMENT = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_first_argument";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND = JavaCore.PLUGIN_ID + ".formatter.insert_space_within_message_send";	//$NON-NLS-1$
	/**
	 * @deprecated use FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_BLOCK_CLOSE_BRACE = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_block_close_brace";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ANONYMOUS_TYPE_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_ANONYMOUS_TYPE_OPEN_BRACE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_anonymous_type_open_brace"; 	//$NON-NLS-1$
	/**
	 * @deprecated use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_BLOCK_OPEN_BRACE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_block_open_brace";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CATCH_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_catch_expression";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CONSTRUCTOR_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CONSTRUCTOR_DECLARATION_OPEN_PAREN = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_constructor_declaration_open_paren";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_FIRST_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_first_initializer";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_FOR_PAREN = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_for_paren";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_IF_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_if_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_METHOD_DECLARATION_OPEN_PAREN = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_method_declaration_open_paren";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_METHOD_OPEN_BRACE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_method_open_brace";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPEN_PAREN_IN_PARENTHESIZED_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_open_paren_in_parenthesized_expression"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_SWITCH_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_switch_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_SWITCH_OPEN_BRACE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_switch_open_brace";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SYNCHRONIZED instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_SYNCHRONIZED_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_synchronized_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_TYPE_OPEN_BRACE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_type_open_brace";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_WHILE_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_while_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET_IN_ARRAY_REFERENCE and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET_IN_ARRAY_REFERENCE instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_brackets_in_array_reference";//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACES_IN_ARRAY_INITIALIZER instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_array_initializer";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION
	 */
	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_MESSAGESEND_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_between_empty_messagesend_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH instead
	 */
	public static final String FORMATTER_INSERT_SPACE_IN_CATCH_EXPRESSION = JavaCore.PLUGIN_ID + ".formatter.insert_space_in_catch_expression";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR instead
	 */
	public static final String FORMATTER_INSERT_SPACE_IN_FOR_PARENS = JavaCore.PLUGIN_ID + ".formatter.insert_space_in_for_parens";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF instead
	 */
	public static final String FORMATTER_INSERT_SPACE_IN_IF_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_in_if_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH instead
	 */
	public static final String FORMATTER_INSERT_SPACE_IN_SWITCH_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_in_switch_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SYNCHRONIZED and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SYNCHRONIZED instead
	 */
	public static final String FORMATTER_INSERT_SPACE_IN_SYNCHRONIZED_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_in_synchronized_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE and FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE instead
	 */
	public static final String FORMATTER_INSERT_SPACE_IN_WHILE_CONDITION = JavaCore.PLUGIN_ID + ".formatter.insert_space_in_while_condition";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_REFERENCE instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_bracket_in_array_reference";//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_TYPE_REFERENCE instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_BRACKET_IN_ARRAY_TYPE_REFERENCE = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_bracket_in_array_type_reference";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATORS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_assignment_operators";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATORS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_assignment_operators"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_KEEP_GUARDIAN_CLAUSE_ON_ONE_LINE instead
	 */
	public static final String FORMATTER_FORMAT_GUARDIAN_CLAUSE_ON_ONE_LINE = JavaCore.PLUGIN_ID + ".formatter.format_guardian_clause_on_one_line";	//$NON-NLS-1$

	
	/**
	 * @deprecated Use FORMATTER_CONTINUATION_INDENTATION_FOR_ARRAY_INITIALIZER instead
	 */
	public static final String FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION = JavaCore.PLUGIN_ID + ".formatter.array_initializer_continuation_indentation";	//$NON-NLS-1$
	
	/**
	 * @deprecated Use FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS instead
	 */
	public static final String FORMATTER_TYPE_MEMBER_ALIGNMENT = JavaCore.PLUGIN_ID + ".formatter.type_member_alignment";	 //$NON-NLS-1$
	/** 
	 * <table BORDER COLS=4 WIDTH="100%" >
	 * <tr><td>#fragment1A</td>            <td>#fragment2A</td>       <td>#fragment3A</td>  <td>#very-long-fragment4A</td></tr>
	 * <tr><td>#fragment1B</td>            <td>#long-fragment2B</td>  <td>#fragment3B</td>  <td>#fragment4B</td></tr>
	 * <tr><td>#very-long-fragment1C</td>  <td>#fragment2C</td>       <td>#fragment3C</td>  <td>#fragment4C</td></tr>
	 * </table>
	 * @deprecated Removed
	 */
	public static final String FORMATTER_MULTICOLUMN = "256"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_throws"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_throws";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_PARAMETERS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CONSTRUCTOR_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CONSTRUCTOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_constructor";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_CONSTRUCTOR_DECLARATION instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_CONSTRUCTOR = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_constructor";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICIT_CONSTRUCTOR_CALL_ARGUMENTS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_explicitconstructorcall_arguments"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICIT_CONSTRUCTOR_CALL_ARGUMENTS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICITCONSTRUCTORCALL_ARGUMENTS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_explicitconstructorcall_arguments";	//$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_DECLARATION_THROWS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_after_comma_in_constructor_throws"; //$NON-NLS-1$
	/**
	 * @deprecated Use FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_DECLARATION_THROWS instead
	 */
	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_THROWS = JavaCore.PLUGIN_ID + ".formatter.insert_space_before_comma_in_constructor_throws";	//$NON-NLS-1$
	/**
	 * if bit set, then alignment will be non-optional (default is optional)
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_ALIGNMENT_FORCE = "1";//$NON-NLS-1$

	/** foobar(<ul>
	 * <li>    #fragment1, #fragment2,  </li>
	 * <li>     #fragment5, #fragment4, </li>
	 * </ul>
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_COMPACT_FIRST_BREAK_SPLIT = "32";//$NON-NLS-1$
	/** foobar(#fragment1, #fragment2, <ul>
	 *  <li>    #fragment3, #fragment4 </li>
	 * </ul>
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_COMPACT_SPLIT = "16";//$NON-NLS-1$
	/**
	 * if bit set, broken fragments will be indented one level below current (not using continuation indentation)
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_INDENT_BY_ONE = "4";//$NON-NLS-1$
	/**
	 * if bit set, broken fragments will be aligned on current location column (default is to break at current indentation level)
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_INDENT_ON_COLUMN = "2";//$NON-NLS-1$
	/** foobar(#fragment1, <ul>
	 * <li>      #fragment2,  </li>
	 * <li>      #fragment3 </li>
	 * <li>      #fragment4,  </li>
	 * </ul>
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_NEXT_PER_LINE_SPLIT = "80"; //$NON-NLS-1$
	/** 
	 * foobar(<ul>
	 * <li>     #fragment1,  </li>
	 * <li>        #fragment2,  </li>
	 * <li>        #fragment3 </li>
	 * <li>        #fragment4,  </li>
	 * </ul>
	 * @deprecated Use the API method to set alignments
	 */ 
	public static final String FORMATTER_NEXT_SHIFTED_SPLIT = "64";//$NON-NLS-1$

	/**
	 * Use to disable line wrapping/splitting
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_NO_ALIGNMENT = "0";//$NON-NLS-1$

	/** foobar(<ul>
	 * <li>     #fragment1,  </li>
	 * <li>     #fragment2,  </li>
	 * <li>     #fragment3 </li>
	 * <li>     #fragment4,  </li>
	 * </ul>
	 * @deprecated Use the API method to set alignments
	 */
	public static final String FORMATTER_ONE_PER_LINE_SPLIT = "48";//$NON-NLS-1$

}
