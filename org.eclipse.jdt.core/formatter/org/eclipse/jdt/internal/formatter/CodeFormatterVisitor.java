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

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.formatter.align.Alignment;
import org.eclipse.jdt.internal.formatter.align.AlignmentException;

/**
 * This class is responsible for formatting a valid java source code.
 * @since 2.1
 */
// TODO add line splitting for all remaining expression and statements
// TODO find a way to handle complex boolean expression
// TODO Add the ICodeFormatter extension point once the UI has fixed their code
/*
   <extension
         id="org.eclipse.jdt.core.newformatter.codeformatter"
         name="org.eclipse.jdt.core.newformatter.codeformatter"
         point="org.eclipse.jdt.core.codeFormatter">
      <codeFormatter
            class="org.eclipse.jdt.internal.formatter.CodeFormatterVisitor">
      </codeFormatter>
   </extension>
*/
public class CodeFormatterVisitor extends AbstractSyntaxTreeVisitorAdapter {

	public static class MultiFieldDeclaration extends FieldDeclaration {
		
		FieldDeclaration[] declarations;
		
		MultiFieldDeclaration(FieldDeclaration[] declarations){
			this.declarations = declarations;
			this.modifiers = declarations[0].modifiers; 
		}
	}
	
	public static boolean DEBUG = false;
	private static final int NO_MODIFIERS = 0;
	/*
	 * Set of expected tokens type for a single type reference.
	 * This array needs to be SORTED.	 */
	private static final int[] SINGLETYPEREFERENCE_EXPECTEDTOKENS = new int[] {
		ITerminalSymbols.TokenNameIdentifier,
		ITerminalSymbols.TokenNameboolean,
		ITerminalSymbols.TokenNamebyte,
		ITerminalSymbols.TokenNamechar,
		ITerminalSymbols.TokenNamedouble,
		ITerminalSymbols.TokenNamefloat,
		ITerminalSymbols.TokenNameint,
		ITerminalSymbols.TokenNamelong,
		ITerminalSymbols.TokenNameshort,
		ITerminalSymbols.TokenNamevoid
	};
	public int lastLocalDeclarationSourceStart;
	private IScanner localScanner;
	public FormattingPreferences preferences;
	public Scribe scribe;
	private int chunkKind;
	
	/*
	 * TODO: See how to choose the formatter's options. The extension point is calling
	 * this constructor, but then there is no way to initialize the option used by the formatter.
	 */ 
	public CodeFormatterVisitor() {
		this(FormattingPreferences.getSunSetttings(), JavaCore.getOptions());
	}

	public CodeFormatterVisitor(FormattingPreferences preferences, Map settings) {
		if (settings != null) {
			String compiler_source = (String) settings.get(JavaCore.COMPILER_SOURCE);
			if (compiler_source == null) {
				this.localScanner = ToolFactory.createScanner(false, false, false, false);
			} else {
				this.localScanner = ToolFactory.createScanner(false, false, false, compiler_source);
			}
		} else {
			this.localScanner = ToolFactory.createScanner(false, false, false, false);
		}
		// TODO set the java core options when common preferences are changed
//		convertOldOptionsToPreferences(settings, preferences);
		this.preferences = preferences;
		this.scribe = new Scribe(this, settings);
	}

	public boolean checkChunkStart(int kind) {
		if (this.chunkKind != kind) {
			this.chunkKind = kind;
			return true;
		}
		return false;
	}
	
	private AstNode[] computeMergedMemberDeclarations(TypeDeclaration typeDeclaration){
		
		int fieldIndex = 0, fieldCount = (typeDeclaration.fields == null) ? 0 : typeDeclaration.fields.length;
		FieldDeclaration field = fieldCount == 0 ? null : typeDeclaration.fields[fieldIndex];
		int fieldStart = field == null ? Integer.MAX_VALUE : field.declarationSourceStart;

		int methodIndex = 0, methodCount = (typeDeclaration.methods == null) ? 0 : typeDeclaration.methods.length;
		AbstractMethodDeclaration method = methodCount == 0 ? null : typeDeclaration.methods[methodIndex];
		int methodStart = method == null ? Integer.MAX_VALUE : method.declarationSourceStart;

		int typeIndex = 0, typeCount = (typeDeclaration.memberTypes == null) ? 0 : typeDeclaration.memberTypes.length;
		MemberTypeDeclaration type = typeCount == 0 ? null : typeDeclaration.memberTypes[typeIndex];
		int typeStart = type == null ? Integer.MAX_VALUE : type.declarationSourceStart;
	
		final int memberLength = fieldCount+methodCount+typeCount;
		AstNode[] members = new AstNode[memberLength];
		if (memberLength != 0) {
			int index = 0;
			int previousFieldStart = -1;
			do {
				if (fieldStart < methodStart && fieldStart < typeStart) {
					// next member is a field
					if (fieldStart == previousFieldStart){ 
						AstNode previousMember = members[index - 1];
						if (previousMember instanceof MultiFieldDeclaration) {
							MultiFieldDeclaration multiField = (MultiFieldDeclaration) previousMember;
							int length = multiField.declarations.length;
							System.arraycopy(multiField.declarations, 0, multiField.declarations=new FieldDeclaration[length+1], 0, length);
							multiField.declarations[length] = (FieldDeclaration) field;
						} else {
							members[index - 1] = new MultiFieldDeclaration(new FieldDeclaration[]{ (FieldDeclaration)previousMember, field});
						}
					} else {
						members[index++] = field;					
					}
					previousFieldStart = fieldStart;
					if (++fieldIndex < fieldCount) { // find next field if any
						fieldStart = (field = typeDeclaration.fields[fieldIndex]).declarationSourceStart;
					} else {
						fieldStart = Integer.MAX_VALUE;
					}
				} else if (methodStart < fieldStart && methodStart < typeStart) {
					// next member is a method
					if (!method.isDefaultConstructor() && !method.isClinit()) {
						members[index++] = method;					
					}
					if (++methodIndex < methodCount) { // find next method if any
						methodStart = (method = typeDeclaration.methods[methodIndex]).declarationSourceStart;
					} else {
						methodStart = Integer.MAX_VALUE;
					}
				} else {
					// next member is a type
					members[index++] = type;
					if (++typeIndex < typeCount) { // find next type if any
						typeStart = (type = typeDeclaration.memberTypes[typeIndex]).declarationSourceStart;
					} else {
						typeStart = Integer.MAX_VALUE;
					}
				}
			} while ((fieldIndex < fieldCount) || (typeIndex < typeCount) || (methodIndex < methodCount));
			
			if (members.length != index) {
				System.arraycopy(members, 0, members=new AstNode[index], 0, index);
			}
		}
		return members;
	}

	void convertOldOptionsToPreferences(Map oldOptions, FormattingPreferences formattingPreferences) {
		if (oldOptions == null) {
			return;
		}
		Object[] entries = oldOptions.entrySet().toArray();
		
		for (int i = 0, max = entries.length; i < max; i++){
			Map.Entry entry = (Map.Entry)entries[i];
			if (!(entry.getKey() instanceof String)) continue;
			if (!(entry.getValue() instanceof String)) continue;
			String optionID = (String) entry.getKey();
			String optionValue = (String) entry.getValue();
			
			if(optionID.equals(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE)){
				if (optionValue.equals(JavaCore.INSERT)){
					formattingPreferences.anonymous_type_declaration_brace_position = FormattingPreferences.NEXT_LINE;
					formattingPreferences.type_declaration_brace_position = FormattingPreferences.NEXT_LINE;
					formattingPreferences.method_declaration_brace_position = FormattingPreferences.NEXT_LINE;
					formattingPreferences.block_brace_position = FormattingPreferences.NEXT_LINE;
					formattingPreferences.switch_brace_position = FormattingPreferences.NEXT_LINE;
				} else if (optionValue.equals(JavaCore.DO_NOT_INSERT)){
					formattingPreferences.anonymous_type_declaration_brace_position = FormattingPreferences.END_OF_LINE;
					formattingPreferences.type_declaration_brace_position = FormattingPreferences.END_OF_LINE;
					formattingPreferences.method_declaration_brace_position = FormattingPreferences.END_OF_LINE;
					formattingPreferences.block_brace_position = FormattingPreferences.END_OF_LINE;
					formattingPreferences.switch_brace_position = FormattingPreferences.END_OF_LINE;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_NEWLINE_CONTROL)) {
				if (optionValue.equals(JavaCore.INSERT)){
					formattingPreferences.insert_new_line_in_control_statements = true;
				} else if (optionValue.equals(JavaCore.DO_NOT_INSERT)){
					formattingPreferences.insert_new_line_in_control_statements = false;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_CLEAR_BLANK_LINES)) {
				if (optionValue.equals(JavaCore.CLEAR_ALL)){
					formattingPreferences.number_of_empty_lines_to_preserve = 0;
				} else if (optionValue.equals(JavaCore.PRESERVE_ONE)){
					formattingPreferences.number_of_empty_lines_to_preserve = 1;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_NEWLINE_ELSE_IF)){
				if (optionValue.equals(JavaCore.INSERT)){
					formattingPreferences.compact_else_if = false;
				} else if (optionValue.equals(JavaCore.DO_NOT_INSERT)){
					formattingPreferences.compact_else_if = true;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_NEWLINE_EMPTY_BLOCK)){
				if (optionValue.equals(JavaCore.INSERT)){
					formattingPreferences.insert_new_line_in_empty_anonymous_type_declaration = true;
					formattingPreferences.insert_new_line_in_empty_type_declaration = true;
					formattingPreferences.insert_new_line_in_empty_method_body = true;
					formattingPreferences.insert_new_line_in_empty_block = true;
				} else if (optionValue.equals(JavaCore.DO_NOT_INSERT)){
					formattingPreferences.insert_new_line_in_empty_anonymous_type_declaration = false;
					formattingPreferences.insert_new_line_in_empty_type_declaration = false;
					formattingPreferences.insert_new_line_in_empty_method_body = false;
					formattingPreferences.insert_new_line_in_empty_block = false;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_LINE_SPLIT)){
				try {
					int val = Integer.parseInt(optionValue);
					if (val >= 0) {
						formattingPreferences.page_width = val;
					}
				} catch(NumberFormatException e){
				}
			}
			if(optionID.equals(JavaCore.FORMATTER_COMPACT_ASSIGNMENT)){
				if (optionValue.equals(JavaCore.COMPACT)){
					formattingPreferences.insert_space_before_assignment_operators = false;
				} else if (optionValue.equals(JavaCore.NORMAL)){
					formattingPreferences.insert_space_before_assignment_operators = true;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_TAB_CHAR)){
				if (optionValue.equals(JavaCore.TAB)){
					formattingPreferences.use_tab = true;
				} else if (optionValue.equals(JavaCore.SPACE)){
					formattingPreferences.use_tab = false;
				}
				continue;
			}
			if(optionID.equals(JavaCore.FORMATTER_TAB_SIZE)){
				try {
					int val = Integer.parseInt(optionValue);
					if (val > 0) {
						formattingPreferences.tab_size = val;
					}
				} catch(NumberFormatException e){
				}
			}
			if(optionID.equals(JavaCore.FORMATTER_SPACE_CASTEXPRESSION)){
				if (optionValue.equals(JavaCore.INSERT)){
					formattingPreferences.insert_space_after_closing_paren_in_cast = true;
				} else if (optionValue.equals(JavaCore.DO_NOT_INSERT)){
					formattingPreferences.insert_space_after_closing_paren_in_cast = false;
				}
				continue;
			}		
		}		
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#acceptProblem(org.eclipse.jdt.core.compiler.IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		super.acceptProblem(problem);
	}

	private boolean dumpBinaryExpression(
		BinaryExpression binaryExpression,
		int operator,
		BlockScope scope) {

		final int numberOfParens = (binaryExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;

		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(binaryExpression, numberOfParens);
		}	
		BinaryExpressionFragmentBuilder builder = buildFragments(binaryExpression, scope);
		final int fragmentsSize = builder.size();
		
		if (fragmentsSize > 1) {
			Alignment binaryExpressionAlignment = this.scribe.createAlignment("binaryExpressionAlignment", this.preferences.binary_expression_alignment, fragmentsSize, this.scribe.scanner.currentPosition); //$NON-NLS-1$
			this.scribe.enterAlignment(binaryExpressionAlignment);
			boolean ok = false;
			AstNode[] fragments = builder.fragments();
			int[] operators = builder.operators();
			do {
				try {
					for (int i = 0; i < fragmentsSize - 1; i++) {
						AstNode fragment = fragments[i];
						fragment.traverse(this, scope);
						this.scribe.alignFragment(binaryExpressionAlignment, i);
						this.scribe.printNextToken(operators[i], this.preferences.insert_space_before_binary_operator);
						if (this.preferences.insert_space_after_binary_operator) {
							this.scribe.space();
						}						
					}
					fragments[fragmentsSize - 1].traverse(this, scope);
					ok = true;
				} catch(AlignmentException e){
					this.scribe.redoAlignment(e);
				}
			} while (!ok);		
			this.scribe.exitAlignment(binaryExpressionAlignment, true);
		} else {
			binaryExpression.left.traverse(this, scope);
			this.scribe.printNextToken(operator, this.preferences.insert_space_before_binary_operator);
			if (this.preferences.insert_space_after_binary_operator) {
				this.scribe.space();
			}
			binaryExpression.right.traverse(this, scope);
		}	
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(binaryExpression, numberOfParens);
		}
		return false;
	}

	private BinaryExpressionFragmentBuilder buildFragments(BinaryExpression binaryExpression, BlockScope scope) {
		BinaryExpressionFragmentBuilder builder = new BinaryExpressionFragmentBuilder();
		
		switch((binaryExpression.bits & EqualExpression.OperatorMASK) >> EqualExpression.OperatorSHIFT) {
			case OperatorIds.AND_AND :
				binaryExpression.left.traverse(builder, scope);
				builder.operatorsList.add(new Integer(ITerminalSymbols.TokenNameAND_AND));
				binaryExpression.right.traverse(builder, scope);
				break;
			case OperatorIds.OR_OR :
				binaryExpression.left.traverse(builder, scope);
				builder.operatorsList.add(new Integer(ITerminalSymbols.TokenNameOR_OR));
				binaryExpression.right.traverse(builder, scope);
				break;
		}

		return builder;
	}
	
	private void formatEmptyStatement() {

		if (this.preferences.put_empty_statement_on_new_line) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		this.scribe.printTrailingCommentForEmptyStatement();
	}

	private void formatGuardClauseBlock(Block block, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACE, this.preferences.insert_space_before_block_open_brace);
		this.scribe.space();

		final Statement[] statements = block.statements;
		statements[0].traverse(this, scope);
		this.scribe.space();
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE, false, true);
		this.scribe.printTrailingComment();
	}	
	
	private void format(FieldDeclaration fieldDeclaration, IAbstractSyntaxTreeVisitor visitor, MethodScope scope, boolean isChunkStart) {
		
		int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
		if (newLineBeforeChunk > 0) {
			this.scribe.printNewLines(newLineBeforeChunk);
		}
		final int newLinesBeforeField = this.preferences.blank_lines_before_field;
		if (newLinesBeforeField > 0) {
			this.scribe.printNewLines(newLinesBeforeField);
		}
		Alignment fieldAlignment = this.scribe.getAlignment("typeMembers");	//$NON-NLS-1$

		if (fieldDeclaration.modifiers != NO_MODIFIERS) {
			this.scribe.printModifiers();
		}
		/*
		 * Field type
		 */
		this.scribe.alignFragment(fieldAlignment, 0);

		fieldDeclaration.type.traverse(this, scope);
		
		/*
		 * Field name
		 */
		this.scribe.alignFragment(fieldAlignment, 1);

		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true);

		/*
		 * Check for extra dimensions
		 */
		int extraDimensions = getExtraDimension();
		if (extraDimensions != 0) {
			 for (int i = 0; i < extraDimensions; i++) {
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			 }
		}
	
		/*
		 * Field initialization
		 */
		if (fieldDeclaration.initialization != null) {
			this.scribe.alignFragment(fieldAlignment, 2);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operators);
			if (this.preferences.insert_space_after_assignment_operators) {
				this.scribe.space();
			}
			fieldDeclaration.initialization.traverse(this, scope);
		}
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);

		if (fieldDeclaration.initialization == null) {
			this.scribe.alignFragment(fieldAlignment, 2);
		}

		if (fieldAlignment != null) {
			this.scribe.alignFragment(fieldAlignment, 3);
			this.scribe.printTrailingComment();
			this.scribe.exitAlignment(fieldAlignment, false);
		} else {
			this.scribe.space();
			this.scribe.printTrailingComment();
		}
	}
	
	private void format(MultiFieldDeclaration multiFieldDeclaration, IAbstractSyntaxTreeVisitor visitor, MethodScope scope, boolean isChunkStart) {

		int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
		if (newLineBeforeChunk > 0) {
			this.scribe.printNewLines(newLineBeforeChunk);
		}
		final int newLinesBeforeField = this.preferences.blank_lines_before_field;
		if (newLinesBeforeField > 0) {
			this.scribe.printNewLines(newLinesBeforeField);
		}
		Alignment fieldAlignment = this.scribe.getAlignment("typeMembers");	//$NON-NLS-1$

		if (multiFieldDeclaration.declarations[0].modifiers != NO_MODIFIERS) {
			this.scribe.printModifiers();
		}
		this.scribe.alignFragment(fieldAlignment, 0);

		multiFieldDeclaration.declarations[0].type.traverse(this, scope);

		for (int i = 0, length = multiFieldDeclaration.declarations.length; i < length; i++) {
			FieldDeclaration fieldDeclaration = multiFieldDeclaration.declarations[i];
			/*
			 * Field name
			 */
			if (i == 0) {
				this.scribe.alignFragment(fieldAlignment, 1);
			}
			this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true);
	
			/*
			 * Check for extra dimensions
			 */
			int extraDimensions = getExtraDimension();
			if (extraDimensions != 0) {
				 for (int index = 0; index < extraDimensions; index++) {
				 	this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
				 	this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
				 }
			}
		
			/*
			 * Field initialization
			 */
			if (fieldDeclaration.initialization != null) {
				if (i == 0) {
					this.scribe.alignFragment(fieldAlignment, 2);
				}
				this.scribe.printNextToken(ITerminalSymbols.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operators);
				if (this.preferences.insert_space_after_assignment_operators) {
					this.scribe.space();
				}
				fieldDeclaration.initialization.traverse(this, scope);
			}
			
			if (i != length - 1) {
				if (i == 0 && fieldDeclaration.initialization == null) {
					this.scribe.alignFragment(fieldAlignment, 2);
				}				
				this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_multiple_field_declarations);
				if (this.preferences.insert_space_after_comma_in_multiple_field_declarations) {
					this.scribe.space();
				}
			} else {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				
				this.scribe.alignFragment(fieldAlignment, 3);
				this.scribe.printTrailingComment();
			}
		}
		if (fieldAlignment != null) {
			this.scribe.exitAlignment(fieldAlignment, false);
		}
	}
	
	/**
	 * @see org.eclipse.jdt.core.ICodeFormatter#format(java.lang.String, int, int, java.lang.String)
	 * TODO handle positions mapping
	 */
	public String format(String string, int[] positions, AstNode[] nodes) {
		// reset the scribe
		this.scribe.reset(positions);
		
		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.scribe.scanner.setSource(compilationUnitSource);
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.scannerEndPosition = compilationUnitSource.length;
		this.scribe.scanner.resetTo(0, this.scribe.scannerEndPosition);
		if (nodes == null) {
			return string;
		}

		this.lastLocalDeclarationSourceStart = 0;
		try {
			if (nodes != null) {
				formatClassBodyDeclarations(nodes);
			}
		} catch(AbortFormatting e){
			return failedToFormat(compilationUnitSource);
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.toString();
	}

	private String failedToFormat(final char[] compilationUnitSource) {
		StringBuffer buffer = new StringBuffer(this.scribe.formattedSource());
		buffer.append(compilationUnitSource, this.scribe.scanner.getCurrentTokenEndPosition(), this.scribe.scannerEndPosition - this.scribe.scanner.getCurrentTokenEndPosition());
		System.out.println("COULD NOT FORMAT \n" + this.scribe.scanner); //$NON-NLS-1$
		if (DEBUG) {
			System.out.println(this.scribe);
		}
		return buffer.toString();
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeFormatter#format(java.lang.String, int, int, java.lang.String)
	 * TODO handle positions mapping
	 */
	public String format(String string, int[] positions, CompilationUnitDeclaration compilationUnitDeclaration) {
		// reset the scribe
		this.scribe.reset(positions);
		
		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.scribe.scanner.setSource(compilationUnitSource);
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.scannerEndPosition = compilationUnitSource.length;
		this.scribe.scanner.resetTo(0, this.scribe.scannerEndPosition);
		if (compilationUnitDeclaration == null || compilationUnitDeclaration.ignoreFurtherInvestigation) {
			return string;
		}

		this.lastLocalDeclarationSourceStart = 0;
		try {
			compilationUnitDeclaration.traverse(this, compilationUnitDeclaration.scope);
		} catch(AbortFormatting e){
			return failedToFormat(compilationUnitSource);
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.toString();
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeFormatter#format(java.lang.String, int, int, java.lang.String)
	 * TODO handle positions mapping
	 */
	public String format(String string, int[] positions, Expression expression) {
		// reset the scribe
		this.scribe.reset(positions);
		
		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.scribe.scanner.setSource(compilationUnitSource);
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.scannerEndPosition = compilationUnitSource.length;
		this.scribe.scanner.resetTo(0, this.scribe.scannerEndPosition);
		if (expression == null) {
			return string;
		}

		this.lastLocalDeclarationSourceStart = 0;
		try {
			expression.traverse(this, null);
		} catch(AbortFormatting e){
			return failedToFormat(compilationUnitSource);
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.toString();
	}

	/**
	 * @see org.eclipse.jdt.core.ICodeFormatter#format(java.lang.String, int, int, java.lang.String)
	 * TODO handle positions mapping
	 */
	public String format(String string, int[] positions, ConstructorDeclaration constructorDeclaration) {
		// reset the scribe
		this.scribe.reset(positions);
		
		long startTime = System.currentTimeMillis();

		final char[] compilationUnitSource = string.toCharArray();
		
		this.scribe.scanner.setSource(compilationUnitSource);
		this.localScanner.setSource(compilationUnitSource);
		this.scribe.scannerEndPosition = compilationUnitSource.length;
		this.scribe.scanner.resetTo(0, this.scribe.scannerEndPosition);
		if (constructorDeclaration == null) {
			return string;
		}

		this.lastLocalDeclarationSourceStart = 0;
		try {
			ExplicitConstructorCall explicitConstructorCall = constructorDeclaration.constructorCall;
			if (explicitConstructorCall != SuperReference.implicitSuperConstructorCall()) {
				explicitConstructorCall.traverse(this, null);
			}
			Statement[] statements = constructorDeclaration.statements;
			if (statements != null) {
				formatStatements(null, statements);
			}
		} catch(AbortFormatting e){
			return failedToFormat(compilationUnitSource);
		}
		if (DEBUG){
			System.out.println("Formatting time: " + (System.currentTimeMillis() - startTime));  //$NON-NLS-1$
		}
		return this.scribe.toString();
	}
	
	private void format(TypeDeclaration typeDeclaration){

		if (typeDeclaration.modifiers != NO_MODIFIERS) {
			this.scribe.printModifiers();
		} else {
			this.scribe.printComment(true);
		}
		/*
		 * Type name
		 */
		if (typeDeclaration.isInterface()) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameinterface, true); 
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameclass, true); 
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true); 

		/* 
		 * Superclass 
		 */
		final TypeReference superclass = typeDeclaration.superclass;
		if (superclass != null) {
			Alignment superclassAlignment =this.scribe.createAlignment(
					"superclass", //$NON-NLS-1$
					this.preferences.type_declaration_superclass_alignment,
					2,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(superclassAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(superclassAlignment, 0);
					this.scribe.printNextToken(ITerminalSymbols.TokenNameextends, true);
					this.scribe.alignFragment(superclassAlignment, 1);
					this.scribe.space();
					superclass.traverse(this, typeDeclaration.scope);
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
		final TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			
			int superInterfaceLength = superInterfaces.length;
			Alignment interfaceAlignment =this.scribe.createAlignment(
					"superInterfaces",//$NON-NLS-1$
					this.preferences.type_declaration_superinterfaces_alignment,
					superInterfaceLength+1,  // implements token is first fragment
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(interfaceAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(interfaceAlignment, 0);
					if (typeDeclaration.isInterface()) {
						this.scribe.printNextToken(ITerminalSymbols.TokenNameextends, true);
					} else  {
						this.scribe.printNextToken(ITerminalSymbols.TokenNameimplements, true);
					}
					for (int i = 0; i < superInterfaceLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_superinterfaces);
							this.scribe.alignFragment(interfaceAlignment, i+1);
							if (this.preferences.insert_space_after_comma_in_superinterfaces) {
								this.scribe.space();
							}
							superInterfaces[i].traverse(this, typeDeclaration.scope);
						} else {
							this.scribe.alignFragment(interfaceAlignment, i+1);
							this.scribe.space();
							superInterfaces[i].traverse(this, typeDeclaration.scope);
						}
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(interfaceAlignment, true);
		}

		/*
		 * Type body
		 */
		String class_declaration_brace = this.preferences.type_declaration_brace_position;

		formatTypeOpeningBrace(class_declaration_brace, this.preferences.insert_space_before_type_open_brace, typeDeclaration);
		
		if (this.preferences.indent_body_declarations_compare_to_type_header) {
			this.scribe.indent();
		}

		formatTypeMembers(typeDeclaration);

		this.scribe.printComment(true);
		
		if (this.preferences.indent_body_declarations_compare_to_type_header) {
			this.scribe.unIndent();
		}
		
		if (this.preferences.insert_new_line_in_empty_type_declaration) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (class_declaration_brace.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}
	}

	public void formatMethodArguments(
			AbstractMethodDeclaration methodDeclaration, 
			boolean spaceBeforeOpenParen, 
			boolean spaceBetweenEmptyArgument,
			boolean spaceBeforeClosingParen, 
			boolean spaceBeforeFirstArgument, 
			boolean spaceBeforeComma, 
			boolean spaceAfterComma) {
				
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, spaceBeforeOpenParen); 
		
		final Argument[] arguments = methodDeclaration.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			Alignment argumentsAlignment = this.scribe.createAlignment(
					"methodArguments",//$NON-NLS-1$
					this.preferences.method_declaration_arguments_alignment,
					argumentLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					if (spaceBeforeFirstArgument) {
						this.scribe.space();
					}
					for (int i = 0; i < argumentLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, spaceBeforeComma);
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && spaceAfterComma) {
							this.scribe.space();
						}
						arguments[i].traverse(this, methodDeclaration.scope);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
		
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, spaceBeforeClosingParen); 
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, spaceBetweenEmptyArgument); 
		}
	}

	private void format(
		MemberTypeDeclaration memberTypeDeclaration,
		ClassScope scope,
		boolean isChunkStart) {

		int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
		if (newLineBeforeChunk > 0) {
			this.scribe.printNewLines(newLineBeforeChunk);
		}
		final int newLinesBeforeMember = this.preferences.blank_lines_before_member_type;
		if (newLinesBeforeMember > 0) {
			this.scribe.printNewLines(newLinesBeforeMember);
		}
		memberTypeDeclaration.traverse(this, scope);
	}

	private void format(
		AbstractMethodDeclaration methodDeclaration,
		ClassScope scope,
		boolean isChunkStart) {

		final int newLineBeforeChunk = isChunkStart ? this.preferences.blank_lines_before_new_chunk : 0;
		if (newLineBeforeChunk > 0) {
			this.scribe.printNewLines(newLineBeforeChunk);
		}
		final int newLinesBeforeMethod = this.preferences.blank_lines_before_method;
		if (newLinesBeforeMethod > 0) {
			this.scribe.printNewLines(newLinesBeforeMethod);
		}
		methodDeclaration.traverse(this, scope);
	}

	public void formatMessageSend(
		MessageSend messageSend,
		BlockScope scope,
		Alignment messageAlignment) {

		if (messageAlignment != null) {
			this.scribe.alignFragment(messageAlignment, 0);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier); // selector
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_message_send);

		final Expression[] arguments = messageSend.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			Alignment argumentsAlignment = this.scribe.createAlignment(
					"messageArguments", //$NON-NLS-1$
					this.preferences.message_send_arguments_alignment,
					argumentLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					if (this.preferences.insert_space_within_message_send) {
						this.scribe.space();
					}
					for (int i = 0; i < argumentLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_messagesend_arguments);
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_messagesend_arguments) {
							this.scribe.space();
						}
						arguments[i].traverse(this, scope);
					}
					this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_within_message_send);
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_between_empty_arguments);
		}
	}

	private void formatOpeningBrace(String bracePosition, boolean insertSpaceBeforeBrace, boolean insertNewLine) {
	
		if (bracePosition.equals(FormattingPreferences.NEXT_LINE)) {
			this.scribe.printNewLine();
		} else if (bracePosition.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
			this.scribe.printNewLine();
			this.scribe.indent();
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACE, insertSpaceBeforeBrace);

		this.scribe.printTrailingComment(insertNewLine);
	}

	private void formatTypeOpeningBrace(String bracePosition, boolean insertSpaceBeforeBrace, TypeDeclaration typeDeclaration) {
		int fieldCount = (typeDeclaration.fields == null) ? 0 : typeDeclaration.fields.length;
		int methodCount = (typeDeclaration.methods == null) ? 0 : typeDeclaration.methods.length;
		int typeCount = (typeDeclaration.memberTypes == null) ? 0 : typeDeclaration.memberTypes.length;
	
		if (methodCount == 1 && typeDeclaration.methods[0].isDefaultConstructor()) {
			methodCount = 0;
		}
		final int memberLength = fieldCount+methodCount+typeCount;

		boolean insertNewLine = memberLength > 0;
		
		if (!insertNewLine) {
			if (typeDeclaration instanceof AnonymousLocalTypeDeclaration) {
				insertNewLine = this.preferences.insert_new_line_in_empty_anonymous_type_declaration;
			} else {
				insertNewLine = this.preferences.insert_new_line_in_empty_type_declaration;
			}
		}
	
		formatOpeningBrace(bracePosition, insertSpaceBeforeBrace, insertNewLine);		
	}

	public void formatStatements(BlockScope scope, final Statement[] statements) {
		int statementsLength = statements.length;
		for (int i = 0; i < statementsLength; i++) {
			final Statement statement = statements[i];
			statement.traverse(this, scope);
			if (statement instanceof Expression) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				if (i < (statementsLength - 1)) {
					/*
					 * We need to check that the next statement is a local declaration
					 */
					if (statements[i + 1] instanceof EmptyStatement) {
						this.scribe.printTrailingCommentForEmptyStatement();
					} else {
						this.scribe.printTrailingComment();
					}
				} else {
					this.scribe.printTrailingComment();
				}		
			} else if (statement instanceof LocalDeclaration) {
				LocalDeclaration currentLocal = (LocalDeclaration) statement;
				if (i < (statementsLength - 1)) {
					/* 
					 * We need to check that the next statement is a local declaration
					 */
					if (statements[i + 1] instanceof LocalDeclaration) {
						LocalDeclaration nextLocal = (LocalDeclaration) statements[i + 1];
						if (currentLocal.declarationSourceStart != nextLocal.declarationSourceStart) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
							this.scribe.printTrailingComment();
						}
					} else {
						this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
						this.scribe.printTrailingComment();
					}
				} else {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
					this.scribe.printTrailingComment();
				}
			} else if (i < (statementsLength - 1)) {
				/*
				 * We need to check that the next statement is a local declaration
				 */
				if (statements[i + 1] instanceof EmptyStatement) {
					this.scribe.printTrailingCommentForEmptyStatement();
				} else {
					this.scribe.printTrailingComment();
				}
			} else {
				this.scribe.printTrailingComment();
			}
		}
	}
	
	private void formatThrowsClause(
		AbstractMethodDeclaration methodDeclaration,
		boolean spaceBeforeComma,
		boolean spaceAfterComma) {
			
		final TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
		if (thrownExceptions != null) {
			int thrownExceptionsLength = thrownExceptions.length;
			Alignment throwsAlignment = this.scribe.createAlignment(
					"throws",//$NON-NLS-1$
					this.preferences.method_throws_clause_alignment,
					thrownExceptionsLength + 1, // throws is the first token
					this.scribe.scanner.currentPosition);
		
			this.scribe.enterAlignment(throwsAlignment);
			boolean ok = false;
			do {
				try {
					this.scribe.alignFragment(throwsAlignment, 0);
					this.scribe.printNextToken(ITerminalSymbols.TokenNamethrows, true); 
					this.scribe.space();
		
					for (int i = 0; i < thrownExceptionsLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, spaceBeforeComma);
							if (spaceAfterComma) {
								this.scribe.space();
							}
						}
						this.scribe.alignFragment(throwsAlignment, i + 1);
						thrownExceptions[i].traverse(this, methodDeclaration.scope);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(throwsAlignment, true);
		}
	}

	/*
	 * Merged traversal of member (types, fields, methods)
	 */
	private void formatTypeMembers(TypeDeclaration typeDeclaration) {
		final int FIELD = 1, METHOD = 2, TYPE = 3;
		
		Alignment memberAlignment = this.scribe.createAlignment("typeMembers", this.preferences.type_member_alignment, 4, this.scribe.scanner.currentPosition); //$NON-NLS-1$
		this.scribe.enterAlignment(memberAlignment);
		AstNode[] members = computeMergedMemberDeclarations(typeDeclaration);
		boolean isChunkStart = false;
		boolean ok = false;
		int startIndex = 0;
		do {
			try {
				for (int i = startIndex, max = members.length; i < max; i++) {
					AstNode member = members[i];
					if (member instanceof FieldDeclaration) {
						isChunkStart = memberAlignment.checkChunkStart(FIELD, i, this.scribe.scanner.currentPosition);
						if (member instanceof MultiFieldDeclaration){
							MultiFieldDeclaration multiField = (MultiFieldDeclaration) member;
							
							if (multiField.isStatic()) {
								format(multiField, this, typeDeclaration.staticInitializerScope, isChunkStart);
							} else {
								format(multiField, this, typeDeclaration.initializerScope, isChunkStart);
							}					
						} else if (member instanceof Initializer) {
							Initializer initializer = (Initializer) member;
							if (initializer.isStatic()) {
								initializer.traverse(this, typeDeclaration.staticInitializerScope);
							} else {
								initializer.traverse(this, typeDeclaration.initializerScope);
							}					
						} else {
							FieldDeclaration field = (FieldDeclaration) member;
							if (field.isStatic()) {
								format(field, this, typeDeclaration.staticInitializerScope, isChunkStart);
							} else {
								format(field, this, typeDeclaration.initializerScope, isChunkStart);
							}					
						}
					} else if (member instanceof AbstractMethodDeclaration) {
						isChunkStart = memberAlignment.checkChunkStart(METHOD, i, this.scribe.scanner.currentPosition);
						format((AbstractMethodDeclaration) member, typeDeclaration.scope, isChunkStart);
					} else {
						isChunkStart = memberAlignment.checkChunkStart(TYPE, i, this.scribe.scanner.currentPosition);
						format((MemberTypeDeclaration)member, typeDeclaration.scope, isChunkStart);
					}
					if (isSemiColon()) {
						this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
						this.scribe.printTrailingComment();
					}
				}
				ok = true;
			} catch(AlignmentException e){
				startIndex = memberAlignment.chunkStartIndex;
				this.scribe.redoAlignment(e);
			}
		} while (!ok);		
		this.scribe.exitAlignment(memberAlignment, true);
	}

	/*
	 * Merged traversal of member (types, fields, methods)
	 */
	private void formatClassBodyDeclarations(AstNode[] nodes) {
		final int FIELD = 1, METHOD = 2, TYPE = 3;
		
		Alignment memberAlignment = this.scribe.createAlignment("typeMembers", this.preferences.type_member_alignment, 4, this.scribe.scanner.currentPosition); //$NON-NLS-1$
		this.scribe.enterAlignment(memberAlignment);
		boolean isChunkStart = false;
		boolean ok = false;
		int startIndex = 0;
		do {
			try {
				for (int i = startIndex, max = nodes.length; i < max; i++) {
					AstNode member = nodes[i];
					if (member instanceof FieldDeclaration) {
						isChunkStart = memberAlignment.checkChunkStart(FIELD, i, this.scribe.scanner.currentPosition);
						if (member instanceof MultiFieldDeclaration){
							MultiFieldDeclaration multiField = (MultiFieldDeclaration) member;
							
							if (multiField.isStatic()) {
								format(multiField, this, null, isChunkStart);
							} else {
								format(multiField, this, null, isChunkStart);
							}					
						} else if (member instanceof Initializer) {
							Initializer initializer = (Initializer) member;
							if (initializer.isStatic()) {
								initializer.traverse(this, null);
							} else {
								initializer.traverse(this, null);
							}					
						} else {
							FieldDeclaration field = (FieldDeclaration) member;
							if (field.isStatic()) {
								format(field, this, null, isChunkStart);
							} else {
								format(field, this, null, isChunkStart);
							}					
						}
					} else if (member instanceof AbstractMethodDeclaration) {
						isChunkStart = memberAlignment.checkChunkStart(METHOD, i, this.scribe.scanner.currentPosition);
						format((AbstractMethodDeclaration) member, null, isChunkStart);
					} else {
						isChunkStart = memberAlignment.checkChunkStart(TYPE, i, this.scribe.scanner.currentPosition);
						format((MemberTypeDeclaration)member, null, isChunkStart);
					}
					if (isSemiColon()) {
						this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
						this.scribe.printTrailingComment();
					}
				}
				ok = true;
			} catch(AlignmentException e){
				startIndex = memberAlignment.chunkStartIndex;
				this.scribe.redoAlignment(e);
			}
		} while (!ok);		
		this.scribe.exitAlignment(memberAlignment, true);
	}

	private int getExtraDimension() {

		this.localScanner.resetTo(this.scribe.scanner.currentPosition, this.scribe.scannerEndPosition - 1);
		int dimensions = 0;
		try {
			int token;
			while ((token = this.localScanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				switch(token) {
					case ITerminalSymbols.TokenNameRBRACKET://166 
						dimensions++;
						break;
					case ITerminalSymbols.TokenNameIdentifier ://90						
					case ITerminalSymbols.TokenNameLBRACE ://90						
					case ITerminalSymbols.TokenNameLPAREN :
					case ITerminalSymbols.TokenNameCOMMA ://90
					case ITerminalSymbols.TokenNameEQUAL ://167
					case ITerminalSymbols.TokenNameSEMICOLON ://64
					case ITerminalSymbols.TokenNameRPAREN : //86
						return dimensions;
				}
			}
		} catch(InvalidInputException e) {
		}
		return dimensions;
	}

	private boolean isComma() {

		this.localScanner.resetTo(this.scribe.scanner.currentPosition, this.scribe.scannerEndPosition - 1);
		try {
			return this.localScanner.getNextToken() == ITerminalSymbols.TokenNameCOMMA;
		} catch(InvalidInputException e) {
		}
		return false;
	}

	private boolean isGuardClause(Block block) {
		return block.statements != null 
				&& block.statements.length == 1
				&& (block.statements[0] instanceof ReturnStatement
					|| block.statements[0] instanceof ThrowStatement);
	}	

	private boolean isMultipleLocalDeclaration(LocalDeclaration localDeclaration) {

		if (localDeclaration.declarationSourceStart == this.lastLocalDeclarationSourceStart) return true;
		this.lastLocalDeclarationSourceStart = localDeclaration.declarationSourceStart;
		return false;
	}

	private boolean isPartOfMultipleLocalDeclaration() {
		this.localScanner.resetTo(this.scribe.scanner.currentPosition, this.scribe.scannerEndPosition - 1);
		try {
			int token;
			while ((token = this.localScanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				switch(token) {
					case ITerminalSymbols.TokenNameCOMMA ://90
						return true;
					default:
						return false;
				}
			}
		} catch(InvalidInputException e) {
		}
		return false;
	}

	private boolean isSemiColon() {

		this.localScanner.resetTo(this.scribe.scanner.currentPosition, this.scribe.scannerEndPosition - 1);
		try {
			return this.localScanner.getNextToken() == ITerminalSymbols.TokenNameSEMICOLON;
		} catch(InvalidInputException e) {
		}
		return false;
	}
	
	private void manageClosingParenthesizedExpression(Expression expression, int numberOfParens) {
		for (int i = 0; i < numberOfParens; i++) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_parenthesized_expression);
		}
	}

	private void manageOpeningParenthesizedExpression(Expression expression, int numberOfParens) {
		for (int i = 0; i < numberOfParens; i++) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_open_paren_in_parenthesized_expression);
			if (this.preferences.insert_space_after_open_paren_in_parenthesized_expression) {
				this.scribe.space();
			}
		}
	}
			
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.AllocationExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		AllocationExpression allocationExpression,
		BlockScope scope) {
		// 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

		final int numberOfParens = (allocationExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(allocationExpression, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNamenew);
		this.scribe.space();
		allocationExpression.type.traverse(this, scope);
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_message_send);

		final Expression[] arguments = allocationExpression.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			Alignment argumentsAlignment =this.scribe.createAlignment(
					"allocation",//$NON-NLS-1$
					this.preferences.allocation_expression_arguments_alignment,
					argumentLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					if (this.preferences.insert_space_within_message_send) {
						this.scribe.space();
					}
					for (int i = 0; i < argumentLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_allocation_expression);
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_allocation_expression) {
							this.scribe.space();
						}
						arguments[i].traverse(this, scope);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_within_message_send); 
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_between_empty_arguments); 
		}
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(allocationExpression, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		AND_AND_Expression and_and_Expression,
		BlockScope scope) {
			
		return dumpBinaryExpression(and_and_Expression, ITerminalSymbols.TokenNameAND_AND, scope);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
		BlockScope scope) {
			
		/*
		 * Type body
		 */
		String anonymous_type_declaration_brace_position = this.preferences.anonymous_type_declaration_brace_position;
		formatTypeOpeningBrace(anonymous_type_declaration_brace_position, this.preferences.insert_space_before_anonymous_type_open_brace, anonymousTypeDeclaration);
		
		this.scribe.indent();

		formatTypeMembers(anonymousTypeDeclaration);
		
		this.scribe.printComment(true);
		this.scribe.unIndent();
		if (this.preferences.insert_new_line_in_empty_anonymous_type_declaration) {
			this.scribe.printNewLine();
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE);
		if (anonymous_type_declaration_brace_position.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Argument, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(Argument argument, BlockScope scope) {

		if (argument.modifiers != NO_MODIFIERS) {
			this.scribe.printModifiers();
			this.scribe.space();
		}

		/*
		 * Argument type 
		 */		
		if (argument.type != null) {
			argument.type.traverse(this, scope);
		}
		
		/*
		 * Print the argument name
		 */	
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true);


		/*
		 * Check for extra dimensions
		 */
		int extraDimensions = getExtraDimension();
		if (extraDimensions != 0) {
			 for (int i = 0; i < extraDimensions; i++) {
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			 }
		}
		
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		ArrayAllocationExpression arrayAllocationExpression,
		BlockScope scope) {

			final int numberOfParens = (arrayAllocationExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
			if (numberOfParens > 0) {
				manageOpeningParenthesizedExpression(arrayAllocationExpression, numberOfParens);
			}
			this.scribe.printNextToken(ITerminalSymbols.TokenNamenew);
			this.scribe.space();
			arrayAllocationExpression.type.traverse(this, scope);
			
			final Expression[] dimensions = arrayAllocationExpression.dimensions;
			int dimensionsLength = dimensions.length;
			for (int i = 0; i < dimensionsLength; i++) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
				if (dimensions[i] != null) {
					dimensions[i].traverse(this, scope);
				}
				this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			}
			final ArrayInitializer initializer = arrayAllocationExpression.initializer;
			if (initializer != null) {
				initializer.traverse(this, scope);
			}

			if (numberOfParens > 0) {
				manageClosingParenthesizedExpression(arrayAllocationExpression, numberOfParens);
			}
			return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayInitializer, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {

		final int numberOfParens = (arrayInitializer.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(arrayInitializer, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACE);
		
		final Expression[] expressions = arrayInitializer.expressions;
		if (expressions != null) {
			int expressionsLength = expressions.length;
			if (expressionsLength > 1) {
				Alignment expressionsAlignment =this.scribe.createAlignment(
						"expressions",//$NON-NLS-1$
						this.preferences.array_initializer_expressions_alignment,
						expressionsLength - 1,
						this.scribe.scanner.currentPosition);
				this.scribe.enterAlignment(expressionsAlignment);
				boolean ok = false;
				do {
					try {
						if (this.preferences.insert_space_before_first_initializer) {
							this.scribe.space();
						}
						expressions[0].traverse(this, scope);
						for (int i = 1; i < expressionsLength; i++) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_array_initializer);
							if (this.preferences.insert_space_after_comma_in_array_initializer) {
								this.scribe.space();
							}
							this.scribe.alignFragment(expressionsAlignment, i - 1);
							expressions[i].traverse(this, scope);
						}
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(expressionsAlignment, true);
			} else {
				// we don't need to use an alignment
				if (this.preferences.insert_space_before_first_initializer) {
					this.scribe.space();
				}
				expressions[0].traverse(this, scope);
				for (int i = 1; i < expressionsLength; i++) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_array_initializer);
					if (this.preferences.insert_space_after_comma_in_array_initializer) {
						this.scribe.space();
					}
					expressions[i].traverse(this, scope);
				}
			}
			if (isComma()) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_array_initializer);
			}
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE, this.preferences.insert_space_before_closing_brace_in_array_initializer, true); 
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE, this.preferences.insert_space_between_empty_array_initializer, true);
		}

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(arrayInitializer, numberOfParens);
		}
		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		BlockScope scope) {

			final int numberOfParens = (arrayQualifiedTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
			if (numberOfParens > 0) {
				manageOpeningParenthesizedExpression(arrayQualifiedTypeReference, numberOfParens);
			}
			this.scribe.printQualifiedReference(arrayQualifiedTypeReference.sourceEnd - 1);
			int dimensions = getExtraDimension();
			if (dimensions != 0) {
				for (int i = 0; i < dimensions; i++) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
					this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
				}
			}
			if (numberOfParens > 0) {
				manageClosingParenthesizedExpression(arrayQualifiedTypeReference, numberOfParens);
			}
			return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(
		ArrayQualifiedTypeReference arrayQualifiedTypeReference,
		ClassScope scope) {

			final int numberOfParens = (arrayQualifiedTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
			if (numberOfParens > 0) {
				manageOpeningParenthesizedExpression(arrayQualifiedTypeReference, numberOfParens);
			}
			this.scribe.printQualifiedReference(arrayQualifiedTypeReference.sourceEnd - 1);
			int dimensions = getExtraDimension();
			if (dimensions != 0) {
				for (int i = 0; i < dimensions; i++) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
					this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
				}
			}
			if (numberOfParens > 0) {
				manageClosingParenthesizedExpression(arrayQualifiedTypeReference, numberOfParens);
			}
			return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ArrayReference arrayReference, BlockScope scope) {

		final int numberOfParens = (arrayReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(arrayReference, numberOfParens);
		}
		arrayReference.receiver.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
		arrayReference.position.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(arrayReference, numberOfParens);
		}
		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		ArrayTypeReference arrayTypeReference,
		BlockScope scope) {

		final int numberOfParens = (arrayTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(arrayTypeReference, numberOfParens);
		}
		this.scribe.printNextToken(SINGLETYPEREFERENCE_EXPECTEDTOKENS, false, true);
		
		int dimensions = getExtraDimension();
		if (dimensions != 0) {
			if (this.preferences.insert_space_before_bracket_in_array_type_reference) {
				this.scribe.space();
			}
			for (int i = 0; i < dimensions; i++) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
				if (this.preferences.insert_space_between_brackets_in_array_type_reference) {
					this.scribe.space();
				}
				this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			}
		}
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(arrayTypeReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(
		ArrayTypeReference arrayTypeReference,
		ClassScope scope) {

		final int numberOfParens = (arrayTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) { 
			manageOpeningParenthesizedExpression(arrayTypeReference, numberOfParens);
		}
		this.scribe.printNextToken(SINGLETYPEREFERENCE_EXPECTEDTOKENS, false, true);
		int dimensions = getExtraDimension();
		if (dimensions != 0) {
			if (this.preferences.insert_space_before_bracket_in_array_type_reference) {
				this.scribe.space();
			}
			for (int i = 0; i < dimensions; i++) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
				if (this.preferences.insert_space_between_brackets_in_array_type_reference) {
					this.scribe.space();
				}
				this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			}
		}
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(arrayTypeReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.AssertStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(AssertStatement assertStatement, BlockScope scope) {
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameassert);
		this.scribe.space();
		assertStatement.assertExpression.traverse(this, scope);
		
		if (assertStatement.exceptionArgument != null) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameCOLON, this.preferences.insert_space_before_colon_in_assert);
			if (this.preferences.insert_space_after_colon_in_assert) {
				this.scribe.space();
			}
			assertStatement.exceptionArgument.traverse(this, scope);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);		
		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Assignment, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(Assignment assignment, BlockScope scope) {

		final int numberOfParens = (assignment.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(assignment, numberOfParens);
		}
		assignment.lhs.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operators);
		if (this.preferences.insert_space_after_assignment_operators) {
			this.scribe.space();
		}
		assignment.expression.traverse(this, scope);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(assignment, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.BinaryExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {

		switch((binaryExpression.bits & EqualExpression.OperatorMASK) >> EqualExpression.OperatorSHIFT) {
			case OperatorIds.AND :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameAND, scope);
			case OperatorIds.DIVIDE :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameDIVIDE, scope);
			case OperatorIds.GREATER :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameGREATER, scope);
			case OperatorIds.GREATER_EQUAL :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameGREATER_EQUAL, scope);
			case OperatorIds.LEFT_SHIFT :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameLEFT_SHIFT, scope);
			case OperatorIds.LESS :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameLESS, scope);
			case OperatorIds.LESS_EQUAL :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameLESS_EQUAL, scope);
			case OperatorIds.MINUS :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameMINUS, scope);
			case OperatorIds.MULTIPLY :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameMULTIPLY, scope);
			case OperatorIds.OR :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameOR, scope);
			case OperatorIds.PLUS :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNamePLUS, scope);
			case OperatorIds.REMAINDER :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameREMAINDER, scope);
			case OperatorIds.RIGHT_SHIFT :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameRIGHT_SHIFT, scope);
			case OperatorIds.UNSIGNED_RIGHT_SHIFT :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT, scope);
			case OperatorIds.XOR :
				return dumpBinaryExpression(binaryExpression, ITerminalSymbols.TokenNameXOR, scope);
			default:
				throw new IllegalStateException();
		}
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Block, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(Block block, BlockScope scope) {
	
		String block_brace_position = this.preferences.block_brace_position;
		formatOpeningBrace(block_brace_position, this.preferences.insert_space_before_block_open_brace, this.preferences.insert_new_line_in_empty_block);
		this.scribe.indent();
	
		final Statement[] statements = block.statements;
		if (statements != null) {
			this.scribe.printTrailingComment();
			formatStatements(scope, statements);
		} else if (this.preferences.insert_new_line_in_empty_block) {
			this.scribe.printNewLine();
		}
		this.scribe.printComment(true);

		this.scribe.unIndent();
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE);
	
		if (block_brace_position.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}		
		return false;	
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Break, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(BreakStatement breakStatement, BlockScope scope) {
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNamebreak);
		if (breakStatement.label != null) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Case, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(CaseStatement caseStatement, BlockScope scope) {		
		if (caseStatement.constantExpression == null) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNamedefault);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameCOLON, this.preferences.insert_space_before_colon_in_default);
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNamecase);
			this.scribe.space();
			caseStatement.constantExpression.traverse(this, scope);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameCOLON, this.preferences.insert_space_before_colon_in_case);
		}
		return false;
	}



	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.CastExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(CastExpression castExpression, BlockScope scope) {

		final int numberOfParens = (castExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(castExpression, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN);
		if (this.preferences.insert_space_after_opening_paren_in_cast) {
			this.scribe.space();
		}
		castExpression.type.traverse(this, scope);

		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_before_closing_paren_in_cast);
		if (this.preferences.insert_space_after_closing_paren_in_cast) {
			this.scribe.space();
		}
		castExpression.expression.traverse(this, scope);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(castExpression, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.CharLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(CharLiteral charLiteral, BlockScope scope) {

		final int numberOfParens = (charLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(charLiteral, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameCharacterLiteral);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(charLiteral, numberOfParens);
		}
		return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ClassLiteralAccess classLiteral, BlockScope scope) {

		final int numberOfParens = (classLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(classLiteral, numberOfParens);
		}
		classLiteral.type.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameclass);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(classLiteral, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Clinit, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(Clinit clinit, ClassScope scope) {

		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration, org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope)
	 */
	public boolean visit(
		CompilationUnitDeclaration compilationUnitDeclaration,
		CompilationUnitScope scope) {
		
		/* 
		 * Package declaration		 */
		if (compilationUnitDeclaration.currentPackage != null) {
			// OPTION
			// dump the package keyword
			int blankLinesBeforePackage = this.preferences.blank_lines_before_package;
			if (blankLinesBeforePackage > 0) {
				this.scribe.printNewLines(blankLinesBeforePackage - 1);
			}
			this.scribe.printComment(true);
			this.scribe.printNextToken(ITerminalSymbols.TokenNamepackage);
			this.scribe.space();
			this.scribe.printQualifiedReference(compilationUnitDeclaration.currentPackage.sourceEnd);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
			int blankLinesAfterPackage = this.preferences.blank_lines_after_package;
			if (blankLinesAfterPackage > 0) {
				this.scribe.printNewLines(blankLinesAfterPackage);
			}				
		} else {
			this.scribe.printComment(true);
		}
		
		/*
		 * Import statements		 */
		final ImportReference[] imports = compilationUnitDeclaration.imports;
		if (imports != null) {
			int blankLinesBeforeImports = this.preferences.blank_lines_before_imports;
			if (blankLinesBeforeImports > 0) {
				this.scribe.printNewLines(blankLinesBeforeImports);
			}
			int importLength = imports.length;
			for (int i = 0; i < importLength; i++) {
				imports[i].traverse(this, scope);
			}			
			
			int blankLinesAfterImports = this.preferences.blank_lines_after_imports;
			if (blankLinesAfterImports > 0) {
				this.scribe.printNewLines(blankLinesAfterImports);
			}
		}

		/*
		 * Type declarations		 */
		final TypeDeclaration[] types = compilationUnitDeclaration.types;
		if (types != null) {
			int typesLength = types.length;
			for (int i = 0; i < typesLength; i++) {
				types[i].traverse(this, scope);
			}
		}
		this.scribe.printComment(false);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.CompoundAssignment, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		CompoundAssignment compoundAssignment,
		BlockScope scope) {
			
		final int numberOfParens = (compoundAssignment.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(compoundAssignment, numberOfParens);
		}
		compoundAssignment.lhs.traverse(this, scope);
		
		/*
		 * Print the operator
		 */
		int operator;
		switch(compoundAssignment.operator) {
			case OperatorIds.PLUS :
				operator = ITerminalSymbols.TokenNamePLUS_EQUAL;
				break;
			case OperatorIds.MINUS :
				operator = ITerminalSymbols.TokenNameMINUS_EQUAL;
				break;
			case OperatorIds.MULTIPLY :
				operator = ITerminalSymbols.TokenNameMULTIPLY_EQUAL;
				break;
			case OperatorIds.DIVIDE :
				operator = ITerminalSymbols.TokenNameDIVIDE_EQUAL;
				break;
			case OperatorIds.AND :
				operator = ITerminalSymbols.TokenNameAND_EQUAL;
				break;
			case OperatorIds.OR :
				operator = ITerminalSymbols.TokenNameOR_EQUAL;
				break;
			case OperatorIds.XOR :
				operator = ITerminalSymbols.TokenNameXOR_EQUAL;
				break;
			case OperatorIds.REMAINDER :
				operator = ITerminalSymbols.TokenNameREMAINDER_EQUAL;
				break;
			case OperatorIds.LEFT_SHIFT :
				operator = ITerminalSymbols.TokenNameLEFT_SHIFT_EQUAL;
				break;
			case OperatorIds.RIGHT_SHIFT :
				operator = ITerminalSymbols.TokenNameRIGHT_SHIFT_EQUAL;
				break;
			default: // OperatorIds.UNSIGNED_RIGHT_SHIFT :
				operator = ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL;
		}
		
		this.scribe.printNextToken(operator, this.preferences.insert_space_before_assignment_operators);
		if (this.preferences.insert_space_after_assignment_operators) {
			this.scribe.space();
		}
		compoundAssignment.expression.traverse(this, scope);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(compoundAssignment, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ConditionalExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		ConditionalExpression conditionalExpression,
		BlockScope scope) {

		final int numberOfParens = (conditionalExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(conditionalExpression, numberOfParens);
		}
		conditionalExpression.condition.traverse(this, scope);

		Alignment conditionalExpressionAlignment =this.scribe.createAlignment(
				"conditionalExpression", //$NON-NLS-1$
				this.preferences.conditional_expression_alignment,
				2,
				this.scribe.scanner.currentPosition);

		this.scribe.enterAlignment(conditionalExpressionAlignment);
		boolean ok = false;
		do {
			try {
				this.scribe.alignFragment(conditionalExpressionAlignment, 0);
				this.scribe.printNextToken(ITerminalSymbols.TokenNameQUESTION, this.preferences.insert_space_before_question_in_conditional);

				if (this.preferences.insert_space_after_question_in_conditional) {
					this.scribe.space();
				}
				conditionalExpression.valueIfTrue.traverse(this, scope);

				this.scribe.alignFragment(conditionalExpressionAlignment, 1);
				this.scribe.printNextToken(ITerminalSymbols.TokenNameCOLON, this.preferences.insert_space_before_colon_in_conditional);

				if (this.preferences.insert_space_after_colon_in_conditional) {
					this.scribe.space();
				}
				conditionalExpression.valueIfFalse.traverse(this, scope);

				ok = true;
			} catch (AlignmentException e) {
				this.scribe.redoAlignment(e);
			}
		} while (!ok);
		this.scribe.exitAlignment(conditionalExpressionAlignment, true);
			
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(conditionalExpression, numberOfParens);
		}
		return false;	
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Continue, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ContinueStatement continueStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNamecontinue);
		if (continueStatement.label != null) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(
		ConstructorDeclaration constructorDeclaration,
		ClassScope scope) {
			
		if (constructorDeclaration.modifiers != NO_MODIFIERS) {
			this.scribe.printModifiers();
		} else {
			this.scribe.printComment(true);
		}			
		/*
		 * Print the method name
		 */	
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true); 

		formatMethodArguments(
			constructorDeclaration, 
			this.preferences.insert_space_before_method_declaration_open_paren,
			this.preferences.insert_space_between_empty_arguments,
			this.preferences.insert_space_before_closing_paren,
			this.preferences.insert_space_before_first_argument,
			this.preferences.insert_space_before_comma_in_constructor_arguments,
			this.preferences.insert_space_after_comma_in_constructor_arguments);

		formatThrowsClause(
				constructorDeclaration,
				this.preferences.insert_space_before_comma_in_constructor_throws,
				this.preferences.insert_space_after_comma_in_constructor_throws);

		if (!constructorDeclaration.isNative() && !constructorDeclaration.isAbstract()) {
			/*
			 * Method body
			 */
			String method_declaration_brace = this.preferences.method_declaration_brace_position;
			formatOpeningBrace(method_declaration_brace, this.preferences.insert_space_before_method_open_brace, this.preferences.insert_new_line_in_empty_method_body);
			this.scribe.indent();			
			final int numberOfBlankLinesAtBeginningOfMethodBody = this.preferences.number_of_blank_lines_to_insert_at_beginning_of_method_body;
			if (numberOfBlankLinesAtBeginningOfMethodBody > 0) {
				this.scribe.printNewLines(numberOfBlankLinesAtBeginningOfMethodBody);
			}
			if (constructorDeclaration.constructorCall != null && !constructorDeclaration.constructorCall.isImplicitSuper()) {
				constructorDeclaration.constructorCall.traverse(this, constructorDeclaration.scope);
				this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				this.scribe.printTrailingComment();
			}
			final Statement[] statements = constructorDeclaration.statements;
			if (statements != null) {
				formatStatements(constructorDeclaration.scope, statements);
			} else if (this.preferences.insert_new_line_in_empty_method_body) {
				this.scribe.printNewLine();
			}
			this.scribe.printComment(true);
			this.scribe.unIndent();
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE);
			this.scribe.printTrailingComment();
			if (method_declaration_brace.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
				this.scribe.unIndent();
			}
		} else {
			// no method body
			this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
		}
		return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.DoStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(DoStatement doStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNamedo);
		
		final Statement action = doStatement.action;
		if (action != null) {
			if (action instanceof Block) {
				action.traverse(this, scope);
			} else {
				this.scribe.printNewLine();
				this.scribe.indent();
				action.traverse(this, scope);
				this.scribe.unIndent();
			}
			if (action instanceof Expression) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				this.scribe.printTrailingComment();
			} else if (action instanceof Block && this.preferences.insert_new_line_in_control_statements) {
				this.scribe.printTrailingComment();
			}
		} else {
			this.scribe.indent();
			/*
			 * This is an empty statement
			 */
			formatEmptyStatement(); 
			this.scribe.unIndent();
		}
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNamewhile, this.preferences.insert_space_after_block_close_brace);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_while_condition);
		
		if (this.preferences.insert_space_in_while_condition) {
			this.scribe.space();
		}
		
		doStatement.condition.traverse(this, scope);
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_while_condition);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.DoubleLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {

		final int numberOfParens = (doubleLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(doubleLiteral, numberOfParens);
		}
		Constant constant = doubleLiteral.constant;
		if (constant != null && constant.doubleValue() < 0) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameMINUS);			
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameDoubleLiteral);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(doubleLiteral, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.EmptyStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(EmptyStatement statement, BlockScope scope) {

		formatEmptyStatement();
		return false;	
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.FalseLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {

		final int numberOfParens = (falseLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(falseLiteral, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNamefalse);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(falseLiteral, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.FieldReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(FieldReference fieldReference, BlockScope scope) {

		final int numberOfParens = (fieldReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(fieldReference, numberOfParens);
		}
		fieldReference.receiver.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(fieldReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ForStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ForStatement forStatement, BlockScope scope) {
	
		this.scribe.printNextToken(ITerminalSymbols.TokenNamefor);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_for_paren);
		
		if (this.preferences.insert_space_in_for_parens) {
			this.scribe.space();
		}
		final Statement[] initializations = forStatement.initializations;
		if (initializations != null) {
			int length = initializations.length;
			for (int i = 0; i < length; i++) {
				initializations[i].traverse(this, scope);
				if (i >= 0 && (i < length - 1) && !(initializations[i] instanceof LocalDeclaration)) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_for_inits);
					if (this.preferences.insert_space_after_comma_in_for_inits) {
						this.scribe.space();
					}
				}				
			}
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		if (this.preferences.insert_space_after_semicolon_in_for) {
			this.scribe.space();
		}
		final Expression condition = forStatement.condition;
		if (condition != null) {
			condition.traverse(this, scope);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		if (this.preferences.insert_space_after_semicolon_in_for) {
			this.scribe.space();
		}
		final Statement[] increments = forStatement.increments;
		if (increments != null) {
			for (int i = 0, length = increments.length; i < length; i++) {
				increments[i].traverse(this, scope);
				if (i != length - 1) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_for_increments);
					if (this.preferences.insert_space_after_comma_in_for_increments) {
						this.scribe.space();
					}
				}
			}
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_for_parens);
		
		final Statement action = forStatement.action;
		if (action != null) {
			if (action instanceof Block) {
				action.traverse(this, scope);
			} else {
				this.scribe.indent();
				this.scribe.printNewLine();
				action.traverse(this, scope);
				this.scribe.unIndent();
			}
			if (action instanceof Expression) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				this.scribe.printTrailingComment();
			} else if (action instanceof Block && this.preferences.insert_new_line_in_control_statements) {
				this.scribe.printTrailingComment();
			}
		} else {
			this.scribe.indent();
			/*
			 * This is an empty statement
			 */
			formatEmptyStatement(); 
			this.scribe.unIndent();
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.EqualExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(EqualExpression equalExpression, BlockScope scope) {

		if ((equalExpression.bits & EqualExpression.OperatorMASK) >> EqualExpression.OperatorSHIFT == OperatorIds.EQUAL_EQUAL) {
			return dumpBinaryExpression(equalExpression, ITerminalSymbols.TokenNameEQUAL_EQUAL, scope);
		} else {
			return dumpBinaryExpression(equalExpression, ITerminalSymbols.TokenNameNOT_EQUAL, scope);
		}			
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		ExplicitConstructorCall explicitConstructor,
		BlockScope scope) {

		if (explicitConstructor.isImplicitSuper()) {
			return false;
		}
		final Expression qualification = explicitConstructor.qualification;
		if (qualification != null) {
			qualification.traverse(this, scope);
		}

		if (explicitConstructor.isSuperAccess()) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNamesuper);
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNamethis);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_message_send);
		
		final Expression[] arguments = explicitConstructor.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			Alignment argumentsAlignment =this.scribe.createAlignment(
					"explicit_constructor_call",//$NON-NLS-1$
					this.preferences.explicit_constructor_arguments_alignment,
					argumentLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					if (this.preferences.insert_space_within_message_send) {
						this.scribe.space();
					}
					for (int i = 0; i < argumentLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_explicitconstructorcall_arguments);
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_explicitconstructorcall_arguments) {
							this.scribe.space();
						}
						arguments[i].traverse(this, scope);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_within_message_send); 
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_between_empty_arguments); 
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.FloatLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {

		final int numberOfParens = (floatLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(floatLiteral, numberOfParens);
		}
		Constant constant = floatLiteral.constant;
		if (constant != null && floatLiteral.constant.floatValue() < 0) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameMINUS);			
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameFloatingPointLiteral);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(floatLiteral, numberOfParens);
		}
		return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.IfStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(IfStatement ifStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNameif);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_if_condition);
		if (this.preferences.insert_space_in_if_condition) {
			this.scribe.space();
		}
		ifStatement.condition.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_if_condition);

		final Statement thenStatement = ifStatement.thenStatement;
		final Statement elseStatement = ifStatement.elseStatement;

		if (thenStatement != null) {
			if (thenStatement instanceof Block) {
				if (isGuardClause((Block)thenStatement) && elseStatement == null && this.preferences.format_guardian_clause_on_one_line) {
					/* 
					 * Need a specific formatting for guard clauses
					 * guard clauses are block with a single return or throw
					 * statement
					 */
					 formatGuardClauseBlock((Block) thenStatement, scope);
				} else {
					if (thenStatement instanceof Block) {
						thenStatement.traverse(this, scope);
					} else {
						this.scribe.printNewLine();
						this.scribe.indent();
						thenStatement.traverse(this, scope);
						this.scribe.unIndent();
					}
					if (elseStatement == null || this.preferences.insert_new_line_in_control_statements) {
						this.scribe.printTrailingComment();
					}
				}
			} else if (elseStatement == null && this.preferences.keep_simple_if_on_one_line) {
				Alignment compactIfAlignment = this.scribe.createAlignment(
					"compactIf", //$NON-NLS-1$
					this.preferences.compact_if_alignment,
					Alignment.R_OUTERMOST,
					1, 
					this.scribe.scanner.currentPosition);
				this.scribe.enterAlignment(compactIfAlignment);
				boolean ok = false;
				do {
					try {
						this.scribe.alignFragment(compactIfAlignment, 0);
						this.scribe.space();
						thenStatement.traverse(this, scope);
						if (thenStatement instanceof Expression) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
						}
						this.scribe.printTrailingComment();
						ok = true;
					} catch (AlignmentException e) {
						this.scribe.redoAlignment(e);
					}
				} while (!ok);
				this.scribe.exitAlignment(compactIfAlignment, true);				
			} else if (this.preferences.keep_then_statement_on_same_line) {
				this.scribe.space();
				thenStatement.traverse(this, scope);
				if (thenStatement instanceof Expression) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				}
				this.scribe.printTrailingComment();
			} else {
				this.scribe.printTrailingComment();
				this.scribe.indent();
				thenStatement.traverse(this, scope);
				if (thenStatement instanceof Expression) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				}
				this.scribe.printTrailingComment();
				this.scribe.unIndent();
			}
		}
		
		if (elseStatement != null) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameelse, true);
			if (elseStatement instanceof Block) {
				elseStatement.traverse(this, scope);
			} else if (elseStatement instanceof IfStatement) {
				if (!this.preferences.compact_else_if) {
					this.scribe.printTrailingComment();
					this.scribe.indent();
				}
				this.scribe.space();				
				elseStatement.traverse(this, scope);
				if (!this.preferences.compact_else_if) {
					this.scribe.printTrailingComment();
					this.scribe.unIndent();
				}
			} else if (this.preferences.keep_else_statement_on_same_line) {
				this.scribe.space();
				elseStatement.traverse(this, scope);
				if (elseStatement instanceof Expression) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				}
				this.scribe.printTrailingComment();
			} else {
				this.scribe.printTrailingComment();
				this.scribe.indent();
				elseStatement.traverse(this, scope);
				if (elseStatement instanceof Expression) {
					this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				}
				this.scribe.printTrailingComment();
				this.scribe.unIndent();
			}
		}
		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ImportReference, org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope)
	 */
	public boolean visit(
		ImportReference importRef,
		CompilationUnitScope scope) {
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameimport);
		this.scribe.space();
		if (importRef.onDemand) {
			this.scribe.printQualifiedReference(importRef.sourceEnd);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameMULTIPLY);			
			this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		} else {
			this.scribe.printQualifiedReference(importRef.sourceEnd);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		}
		this.scribe.printTrailingComment();
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Initializer, org.eclipse.jdt.internal.compiler.lookup.MethodScope)
	 */
	public boolean visit(Initializer initializer, MethodScope scope) {

		if (initializer.isStatic()) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNamestatic);
		}
		initializer.block.traverse(this, scope);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		InstanceOfExpression instanceOfExpression,
		BlockScope scope) {

		final int numberOfParens = (instanceOfExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(instanceOfExpression, numberOfParens);
		}
		instanceOfExpression.expression.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameinstanceof, true);
		this.scribe.space();
		instanceOfExpression.type.traverse(this, scope);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(instanceOfExpression, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter#visit(org.eclipse.jdt.internal.compiler.ast.IntLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(IntLiteral intLiteral, BlockScope scope) {

		final int numberOfParens = (intLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(intLiteral, numberOfParens);
		}
		Constant constant = intLiteral.constant;
		if (constant != null && constant.intValue() < 0) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameMINUS);			
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIntegerLiteral);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(intLiteral, numberOfParens);
		}
		return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.LabeledStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameCOLON, this.preferences.insert_space_before_colon_in_labeled_statement);
		if (this.preferences.insert_space_after_colon_in_labeled_statement) {
			this.scribe.space();
		}
		labeledStatement.statement.traverse(this, scope);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.LocalDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {

		if (!isMultipleLocalDeclaration(localDeclaration)) {
			if (localDeclaration.modifiers != NO_MODIFIERS) {
				this.scribe.printModifiers();
				this.scribe.space();
			}
	
			/*
			 * Argument type 
			 */		
			if (localDeclaration.type != null) {
				localDeclaration.type.traverse(this, scope);
			}
		}
		/*
		 * Print the argument name
		 */	
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true); 
		/*
		 * Check for extra dimensions
		 */
		int extraDimensions = getExtraDimension();
		if (extraDimensions != 0) {
			 for (int index = 0; index < extraDimensions; index++) {
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			 }
		}
	
		if (localDeclaration.initialization != null) {
			/*
			 * Print the method name
			 */	
			this.scribe.printNextToken(ITerminalSymbols.TokenNameEQUAL, this.preferences.insert_space_before_assignment_operators);
			if (this.preferences.insert_space_after_assignment_operators) {
				this.scribe.space();
			}			 
			localDeclaration.initialization.traverse(this, scope);
		}

		if (isPartOfMultipleLocalDeclaration()) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_multiple_local_declarations); 
			if (this.preferences.insert_space_after_comma_in_multiple_local_declarations) {
				this.scribe.space();
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		LocalTypeDeclaration localTypeDeclaration,
		BlockScope scope) {

			format(localTypeDeclaration);
			return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.LongLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(LongLiteral longLiteral, BlockScope scope) {

		final int numberOfParens = (longLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(longLiteral, numberOfParens);
		}
		Constant constant = longLiteral.constant;
		if (constant != null && constant.longValue() < 0) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameMINUS);			
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLongLiteral);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(longLiteral, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter#visit(org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope) {
		Alignment memberTypeAlignment = this.scribe.getAlignment("typeMembers");		//$NON-NLS-1$
		format(memberTypeDeclaration);
		this.scribe.exitAlignment(memberTypeAlignment, false);		
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.MessageSend, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(MessageSend messageSend, BlockScope scope) {

		final int numberOfParens = (messageSend.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(messageSend, numberOfParens);
		}
		Alignment messageAlignment = null;
		if (!messageSend.receiver.isImplicitThis()) {
			messageSend.receiver.traverse(this, scope);
			messageAlignment = this.scribe.createAlignment(
					"messageAlignment", //$NON-NLS-1$
					this.preferences.message_send_selector_alignment,
					1,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(messageAlignment);
			boolean ok = false;
			do {
				try {
					formatMessageSend(messageSend, scope, messageAlignment);
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(messageAlignment, true);
		} else {
			formatMessageSend(messageSend, scope, null);			
		}
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(messageSend, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.MethodDeclaration, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(
		MethodDeclaration methodDeclaration,
		ClassScope scope) {

		if (methodDeclaration.modifiers != NO_MODIFIERS) {
			this.scribe.printModifiers();
			this.scribe.space();
		}
		
		/*
		 * Print the method return type
		 */	
		final TypeReference returnType = methodDeclaration.returnType;
		final MethodScope methodDeclarationScope = methodDeclaration.scope;
		
		if (returnType != null) {
			returnType.traverse(this, methodDeclarationScope);
		}
		/*
		 * Print the method name		 */
		this.scribe.printNextToken(ITerminalSymbols.TokenNameIdentifier, true); 

		/*
		 * Check for extra dimensions
		 */
		int extraDimensions = getExtraDimension();
		if (extraDimensions != 0) {
			 for (int i = 0; i < extraDimensions; i++) {
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameLBRACKET);
			 	this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACKET);
			 }
		}

		formatMethodArguments(
			methodDeclaration, 
			this.preferences.insert_space_before_method_declaration_open_paren,
			this.preferences.insert_space_between_empty_arguments,
			this.preferences.insert_space_before_closing_paren,
			this.preferences.insert_space_before_first_argument,
			this.preferences.insert_space_before_comma_in_method_arguments,
			this.preferences.insert_space_after_comma_in_method_arguments);

		formatThrowsClause(
			methodDeclaration,
			this.preferences.insert_space_before_comma_in_method_throws,
			this.preferences.insert_space_after_comma_in_method_throws);

		if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract() && ((methodDeclaration.modifiers & CompilerModifiers.AccSemicolonBody) == 0)) {
			/*
			 * Method body
			 */
			String method_declaration_brace = this.preferences.method_declaration_brace_position;
			formatOpeningBrace(method_declaration_brace, this.preferences.insert_space_before_method_open_brace, this.preferences.insert_new_line_in_empty_method_body);
			this.scribe.indent();			
			final int numberOfBlankLinesAtBeginningOfMethodBody = this.preferences.number_of_blank_lines_to_insert_at_beginning_of_method_body;
			if (numberOfBlankLinesAtBeginningOfMethodBody > 0) {
				this.scribe.printNewLines(numberOfBlankLinesAtBeginningOfMethodBody);
			}
			final Statement[] statements = methodDeclaration.statements;
			if (statements != null) {
				formatStatements(methodDeclarationScope, statements);
			} else if (this.preferences.insert_new_line_in_empty_method_body) {
				this.scribe.printNewLine();
			}
			this.scribe.printComment(true);
			this.scribe.unIndent();
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE);
			this.scribe.printTrailingComment();
			if (method_declaration_brace.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
				this.scribe.unIndent();
			}
		} else {
			// no method body
			this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
			this.scribe.printTrailingComment();
		}
		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.NullLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(NullLiteral nullLiteral, BlockScope scope) {

		final int numberOfParens = (nullLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(nullLiteral, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNamenull);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(nullLiteral, numberOfParens);
		}
		return false;
	}


	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {

		return dumpBinaryExpression(or_or_Expression, ITerminalSymbols.TokenNameOR_OR, scope);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.PostfixExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		PostfixExpression postfixExpression,
		BlockScope scope) {

		final int numberOfParens = (postfixExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(postfixExpression, numberOfParens);
		}
		postfixExpression.lhs.traverse(this, scope);
		int operator = postfixExpression.operator == OperatorIds.PLUS 
			? ITerminalSymbols.TokenNamePLUS_PLUS : ITerminalSymbols.TokenNameMINUS_MINUS;
		this.scribe.printNextToken(operator, this.preferences.insert_space_before_postfix_operator);
		if (this.preferences.insert_space_after_postfix_operator) {
			this.scribe.space();
		}
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(postfixExpression, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.PrefixExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {

		final int numberOfParens = (prefixExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(prefixExpression, numberOfParens);
		}
		int operator = prefixExpression.operator == OperatorIds.PLUS 
			? ITerminalSymbols.TokenNamePLUS_PLUS : ITerminalSymbols.TokenNameMINUS_MINUS;
		this.scribe.printNextToken(operator, this.preferences.insert_space_before_prefix_operator);
		prefixExpression.lhs.traverse(this, scope);
		if (this.preferences.insert_space_after_prefix_operator) {
			this.scribe.space();
		}
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(prefixExpression, numberOfParens);
		}
		return false;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		QualifiedAllocationExpression qualifiedAllocationExpression,
		BlockScope scope) {
			
		final int numberOfParens = (qualifiedAllocationExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(qualifiedAllocationExpression, numberOfParens);
		}
		final Expression enclosingInstance = qualifiedAllocationExpression.enclosingInstance;
		if (enclosingInstance != null) {
			enclosingInstance.traverse(this, scope);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNamenew);
		this.scribe.space();
		qualifiedAllocationExpression.type.traverse(this, scope);
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_message_send);

		final Expression[] arguments = qualifiedAllocationExpression.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			Alignment argumentsAlignment =this.scribe.createAlignment(
					"allocation",//$NON-NLS-1$
					this.preferences.qualified_allocation_expression_arguments_alignment,
					argumentLength,
					this.scribe.scanner.currentPosition);
			this.scribe.enterAlignment(argumentsAlignment);
			boolean ok = false;
			do {
				try {
					if (this.preferences.insert_space_within_message_send) {
						this.scribe.space();
					}
					for (int i = 0; i < argumentLength; i++) {
						if (i > 0) {
							this.scribe.printNextToken(ITerminalSymbols.TokenNameCOMMA, this.preferences.insert_space_before_comma_in_allocation_expression);
						}
						this.scribe.alignFragment(argumentsAlignment, i);
						if (i > 0 && this.preferences.insert_space_after_comma_in_allocation_expression) {
							this.scribe.space();
						}
						arguments[i].traverse(this, scope);
					}
					ok = true;
				} catch (AlignmentException e) {
					this.scribe.redoAlignment(e);
				}
			} while (!ok);
			this.scribe.exitAlignment(argumentsAlignment, true);
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_within_message_send); 
		} else {
			this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_between_empty_arguments); 
		}
		final AnonymousLocalTypeDeclaration anonymousType = qualifiedAllocationExpression.anonymousType;
		if (anonymousType != null) {
			anonymousType.traverse(this, scope);
		}
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(qualifiedAllocationExpression, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		QualifiedNameReference qualifiedNameReference,
		BlockScope scope) {

		final int numberOfParens = (qualifiedNameReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(qualifiedNameReference, numberOfParens);
		}
		this.scribe.printQualifiedReference(qualifiedNameReference.sourceEnd);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(qualifiedNameReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		QualifiedSuperReference qualifiedSuperReference,
		BlockScope scope) {

		final int numberOfParens = (qualifiedSuperReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(qualifiedSuperReference, numberOfParens);
		}
		qualifiedSuperReference.qualification.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
		this.scribe.printNextToken(ITerminalSymbols.TokenNamesuper);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(qualifiedSuperReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		QualifiedThisReference qualifiedThisReference,
		BlockScope scope) {

		final int numberOfParens = (qualifiedThisReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(qualifiedThisReference, numberOfParens);
		}
		qualifiedThisReference.qualification.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameDOT);
		this.scribe.printNextToken(ITerminalSymbols.TokenNamethis);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(qualifiedThisReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		QualifiedTypeReference qualifiedTypeReference,
		BlockScope scope) {

		final int numberOfParens = (qualifiedTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(qualifiedTypeReference, numberOfParens);
		}
		this.scribe.printQualifiedReference(qualifiedTypeReference.sourceEnd + 1);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(qualifiedTypeReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(
		QualifiedTypeReference qualifiedTypeReference,
		ClassScope scope) {

			final int numberOfParens = (qualifiedTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
			if (numberOfParens > 0) {
				manageOpeningParenthesizedExpression(qualifiedTypeReference, numberOfParens);
			}
			this.scribe.printQualifiedReference(qualifiedTypeReference.sourceEnd + 1);
			
			if (numberOfParens > 0) {
				manageClosingParenthesizedExpression(qualifiedTypeReference, numberOfParens);
			}
			return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ReturnStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNamereturn);
		final Expression expression = returnStatement.expression;
		if (expression != null) {
			this.scribe.space();
			expression.traverse(this, scope);
		}
		/*
		 * Print the semi-colon
		 */	
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter#visit(org.eclipse.jdt.internal.compiler.ast.SingleNameReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(SingleNameReference singleNameReference, BlockScope scope) {

		final int numberOfParens = (singleNameReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(singleNameReference, numberOfParens);
		}
		this.scribe.printNextToken(SINGLETYPEREFERENCE_EXPECTEDTOKENS, false, true);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(singleNameReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.SingleTypeReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		SingleTypeReference singleTypeReference,
		BlockScope scope) {

		final int numberOfParens = (singleTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(singleTypeReference, numberOfParens);
		}
		this.scribe.printNextToken(SINGLETYPEREFERENCE_EXPECTEDTOKENS, false, true);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(singleTypeReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.SingleTypeReference, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(
		SingleTypeReference singleTypeReference,
		ClassScope scope) {

		final int numberOfParens = (singleTypeReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(singleTypeReference, numberOfParens);
		}
		this.scribe.printNextToken(SINGLETYPEREFERENCE_EXPECTEDTOKENS, false, true);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(singleTypeReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.StringLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {

		final int numberOfParens = (stringLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(stringLiteral, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNameStringLiteral);
		
		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(stringLiteral, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.SuperReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(SuperReference superReference, BlockScope scope) {

		final int numberOfParens = (superReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(superReference, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNamesuper);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(superReference, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.SwitchStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(SwitchStatement switchStatement, BlockScope scope) {
		this.scribe.printNextToken(ITerminalSymbols.TokenNameswitch);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_switch_condition);
		
		if (this.preferences.insert_space_in_switch_condition) {
			this.scribe.space();
		}
		
		switchStatement.testExpression.traverse(this, scope);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_switch_condition);
		/*
		 * Type body
		 */
		String switch_brace = this.preferences.switch_brace_position;
		formatOpeningBrace(switch_brace, this.preferences.insert_space_before_switch_open_brace, true);

		if (preferences.indent_switchstatements_compare_to_switch) {
			this.scribe.indent();
		}
		final Statement[] statements = switchStatement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			boolean wasABreak = false;
			for (int i = 0; i < statementsLength; i++) {
				final Statement statement = statements[i];
				if (i == 0) {
					// this is a case or a default statement
					statement.traverse(this, scope);
					this.scribe.printTrailingComment();
					if (this.preferences.indent_switchstatements_compare_to_cases) {
						this.scribe.indent();
					}
				} else if (statement instanceof CaseStatement) {
					if (wasABreak) {
						if (this.preferences.indent_breaks_compare_to_cases) {
							this.scribe.unIndent();
						}
					} else if (this.preferences.indent_switchstatements_compare_to_cases) {
						this.scribe.unIndent();
					}
					statement.traverse(this, scope);
					wasABreak = false;
					if (this.preferences.indent_switchstatements_compare_to_cases) {
						this.scribe.indent();
					}
				} else if (statement instanceof BreakStatement) {
					wasABreak = true;
					if (this.preferences.indent_switchstatements_compare_to_cases) {
						if (!this.preferences.indent_breaks_compare_to_cases) {
							this.scribe.unIndent();
						}
					} else if (this.preferences.indent_breaks_compare_to_cases) {
						this.scribe.indent();
					}
					statement.traverse(this, scope);
					if (i == statementsLength - 1 && this.preferences.indent_breaks_compare_to_cases) {
						this.scribe.unIndent();
					}
				} else {
					wasABreak = false;
					statement.traverse(this, scope);
					if (i == statementsLength - 1 && this.preferences.indent_switchstatements_compare_to_cases) {
						this.scribe.unIndent();
					}
				}
				if (statement instanceof Expression) {
					/*
					 * Print the semi-colon
					 */	
					this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
					if (i < (statementsLength - 1)) {
						/*
						 * We need to check that the next statement is a local declaration
						 */
						if (statements[i + 1] instanceof EmptyStatement) {
							this.scribe.printTrailingCommentForEmptyStatement();
						} else {
							this.scribe.printTrailingComment();
						}
					} else {
						this.scribe.printTrailingComment();
					}					
				} else if (statement instanceof LocalDeclaration) {
					LocalDeclaration currentLocal = (LocalDeclaration) statement;
					if (i < (statementsLength - 1)) {
						/* 
						 * We need to check that the next statement is a local declaration
						 */
						if (statements[i + 1] instanceof LocalDeclaration) {
							LocalDeclaration nextLocal = (LocalDeclaration) statements[i + 1];
							if (currentLocal.declarationSourceStart != nextLocal.declarationSourceStart) {
								/*
								 * Print the semi-colon
								 */	
								this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
								this.scribe.printTrailingComment();
							}
						} else {
							/*
							 * Print the semi-colon
							 */	
							this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
							this.scribe.printTrailingComment();
						}
					} else {
						/*
						 * Print the semi-colon
						 */	
						this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
						this.scribe.printTrailingComment();
					}
				} else if (i < (statementsLength - 1)) {
					/* 
					 * We need to check that the next statement is a local declaration
					 */
					if (statements[i + 1] instanceof EmptyStatement) {
						this.scribe.printTrailingCommentForEmptyStatement();
					} else {
						this.scribe.printTrailingComment();
					}
				} else {
					this.scribe.printTrailingComment();
				}
			}
		}		
		
		if (preferences.indent_switchstatements_compare_to_switch) {
			this.scribe.unIndent();
		}
		this.scribe.printNewLine();
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRBRACE);
		this.scribe.printTrailingComment();
		if (switch_brace.equals(FormattingPreferences.NEXT_LINE_SHIFTED)) {
			this.scribe.unIndent();
		}		
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(
		SynchronizedStatement synchronizedStatement,
		BlockScope scope) {
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNamesynchronized);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_synchronized_condition);
		
		if (this.preferences.insert_space_in_synchronized_condition) {
			this.scribe.space();
		}
		synchronizedStatement.expression.traverse(this, scope);
	
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_synchronized_condition);
		
		synchronizedStatement.block.traverse(this, scope);
		this.scribe.printTrailingComment();
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.TrueLiteral, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {

		final int numberOfParens = (trueLiteral.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(trueLiteral, numberOfParens);
		}
		this.scribe.printNextToken(ITerminalSymbols.TokenNametrue);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(trueLiteral, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope)
	 */
	public boolean visit(
		TypeDeclaration typeDeclaration,
		CompilationUnitScope scope) {

		format(typeDeclaration);
		return false;
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ThisReference, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ThisReference thisReference, BlockScope scope) {
		
		if (!thisReference.isImplicitThis()) {
			final int numberOfParens = (thisReference.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
			if (numberOfParens > 0) {
				manageOpeningParenthesizedExpression(thisReference, numberOfParens);
			}
			this.scribe.printNextToken(ITerminalSymbols.TokenNamethis);
			
			if (numberOfParens > 0) {
				manageClosingParenthesizedExpression(thisReference, numberOfParens);
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.ThrowStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(ThrowStatement throwStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNamethrow);
		this.scribe.space();
		throwStatement.exception.traverse(this, scope);
		/*
		 * Print the semi-colon
		 */	
		this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.TryStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(TryStatement tryStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNametry);
		tryStatement.tryBlock.traverse(this, scope);
		if (this.preferences.insert_new_line_in_control_statements) {
			this.scribe.printTrailingComment();
		}	
		if (tryStatement.catchArguments != null) {
			for (int i = 0, max = tryStatement.catchBlocks.length; i < max; i++) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNamecatch, this.preferences.insert_space_after_block_close_brace);
				this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_catch_expression);
				
				if (this.preferences.insert_space_in_catch_expression) {
					this.scribe.space();
				}
				
				tryStatement.catchArguments[i].traverse(this, scope);
			
				this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_catch_expression);
				
				tryStatement.catchBlocks[i].traverse(this, scope);
				if (this.preferences.insert_new_line_in_control_statements) {
					this.scribe.printTrailingComment();
				}	
			}
		}
		if (tryStatement.finallyBlock != null) {
			this.scribe.printNextToken(ITerminalSymbols.TokenNamefinally, this.preferences.insert_space_after_block_close_brace);
			tryStatement.finallyBlock.traverse(this, scope);
			if (this.preferences.insert_new_line_in_control_statements) {
				this.scribe.printTrailingComment();
			}	
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.UnaryExpression, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {

		final int numberOfParens = (unaryExpression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		if (numberOfParens > 0) {
			manageOpeningParenthesizedExpression(unaryExpression, numberOfParens);
		}

		/*
		 * Print the operator
		 */
		int operator;
		switch((unaryExpression.bits & OperatorExpression.OperatorMASK) >> OperatorExpression.OperatorSHIFT) {
			case OperatorIds.PLUS:
				operator = ITerminalSymbols.TokenNamePLUS;
				break;
			case OperatorIds.MINUS:
				operator = ITerminalSymbols.TokenNameMINUS;
				break;
			case OperatorIds.TWIDDLE:
				operator = ITerminalSymbols.TokenNameTWIDDLE;
				break;
			default:
				operator = ITerminalSymbols.TokenNameNOT;
		}

		if (operator == ITerminalSymbols.TokenNameMINUS) {
			this.scribe.printNextToken(operator, true);
		} else {
			this.scribe.printNextToken(operator, this.preferences.insert_space_before_unary_operator);
		}
		if (this.preferences.insert_space_after_unary_operator) {
			this.scribe.space();
		}
		unaryExpression.expression.traverse(this, scope);

		if (numberOfParens > 0) {
			manageClosingParenthesizedExpression(unaryExpression, numberOfParens);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor#visit(org.eclipse.jdt.internal.compiler.ast.WhileStatement, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(WhileStatement whileStatement, BlockScope scope) {

		this.scribe.printNextToken(ITerminalSymbols.TokenNamewhile);
		this.scribe.printNextToken(ITerminalSymbols.TokenNameLPAREN, this.preferences.insert_space_before_while_condition);
		
		if (this.preferences.insert_space_in_while_condition) {
			this.scribe.space();
		}
		whileStatement.condition.traverse(this, scope);
		
		this.scribe.printNextToken(ITerminalSymbols.TokenNameRPAREN, this.preferences.insert_space_in_while_condition);
		
		final Statement action = whileStatement.action;
		if (action != null) {
			if (action instanceof Block) {
				action.traverse(this, scope);
			} else {
				this.scribe.printNewLine();
				this.scribe.indent();
				action.traverse(this, scope);
				this.scribe.unIndent();
			}
			if (action instanceof Expression) {
				this.scribe.printNextToken(ITerminalSymbols.TokenNameSEMICOLON, this.preferences.insert_space_before_semicolon);
				this.scribe.printTrailingComment();
			} else if (action instanceof Block) {
				this.scribe.printTrailingComment();
			}		
		} else {
			this.scribe.indent();
			/*
			 * This is an empty statement
			 */
			formatEmptyStatement(); 
			this.scribe.unIndent();
		}

		return false;
	}
}
