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
package org.eclipse.jdt.internal.core.dom.rewrite;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.TryStatement;

import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore.CopySourceInfo;

/**
 *
 */
public final class NodeInfoStore {	
	private AST fAst;
	
	private Map fPlaceholderNodes;
	private Set fCollapsedNodes;

	public NodeInfoStore(AST ast) {
		super();
		fAst= ast;
		fPlaceholderNodes= null;
		fCollapsedNodes= null;
	}

	/**
	 * Marks a node as a placehoder for a plain string content. The type of the node should correspond to the
	 * code's code content.
	 * @param placeholder The placeholder node that acts for the string content.
	 * @param code The string content.
	 */
	public final void markAsStringPlaceholder(ASTNode placeholder, String code) {
		StringPlaceholderData data= new StringPlaceholderData();
		data.code= code;
		setPlaceholderData(placeholder, data);
	}
	
	/**
	 * Marks a node as a copy or move target. The copy target represents a copied node at the target (copied) site.
	 * @param target The node at the target site. Can be a placeholder node but also the source node itself.
	 * @param copySource The info at the source site.
	 */
	public final void markAsCopyTarget(ASTNode target, CopySourceInfo copySource) {
		CopyPlaceholderData data= new CopyPlaceholderData();
		data.copySource= copySource;
		setPlaceholderData(target, data);
	}
	
	/**
	 * Creates a placeholder node of the given type. <code>null</code> if the type is not supported
	 * @param nodeType Type of the node to create. Use the type constants in {@link NodeInfoStore}.
	 * @return Returns a place holder node.
	 */
	public final ASTNode newPlaceholderNode(int nodeType) {
		AST ast= fAst;
		switch (nodeType) {
			case ASTNode.ANNOTATION_TYPE_DECLARATION :
				return ast.newAnnotationTypeDeclaration();
			case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
				return ast.newAnnotationTypeMemberDeclaration();
			case ASTNode.ANONYMOUS_CLASS_DECLARATION :
				return ast.newAnonymousClassDeclaration();
			case ASTNode.ARRAY_ACCESS :
				return ast.newArrayAccess();
			case ASTNode.ARRAY_CREATION :
				return ast.newArrayCreation();
			case ASTNode.ARRAY_INITIALIZER :
				return ast.newArrayInitializer();
			case ASTNode.ARRAY_TYPE :
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT));
			case ASTNode.ASSERT_STATEMENT :
				return ast.newAssertStatement();
			case ASTNode.ASSIGNMENT :
				return ast.newAssignment();
			case ASTNode.BLOCK :
				return ast.newBlock();
			case ASTNode.BLOCK_COMMENT :
				return ast.newBlockComment();
			case ASTNode.BOOLEAN_LITERAL :
				return ast.newBooleanLiteral(false);
			case ASTNode.BREAK_STATEMENT :
				return ast.newBreakStatement();
			case ASTNode.CAST_EXPRESSION :
				return ast.newCastExpression();
			case ASTNode.CATCH_CLAUSE :
				return ast.newCatchClause();
			case ASTNode.CHARACTER_LITERAL :
				return ast.newCharacterLiteral();
			case ASTNode.CLASS_INSTANCE_CREATION :
				return ast.newClassInstanceCreation();
			case ASTNode.COMPILATION_UNIT :
				return ast.newCompilationUnit();
			case ASTNode.CONDITIONAL_EXPRESSION :
				return ast.newConditionalExpression();
			case ASTNode.CONSTRUCTOR_INVOCATION :
				return ast.newConstructorInvocation();
			case ASTNode.CONTINUE_STATEMENT :
				return ast.newContinueStatement();
			case ASTNode.DO_STATEMENT :
				return ast.newDoStatement();
			case ASTNode.EMPTY_STATEMENT :
				return ast.newEmptyStatement();
			case ASTNode.ENHANCED_FOR_STATEMENT :
				return ast.newEnhancedForStatement();
			case ASTNode.ENUM_CONSTANT_DECLARATION :
				return ast.newEnumConstantDeclaration();
			case ASTNode.ENUM_DECLARATION :
				return ast.newEnumDeclaration();
			case ASTNode.EXPRESSION_STATEMENT :
				return ast.newExpressionStatement(ast.newMethodInvocation());
			case ASTNode.FIELD_ACCESS :
				return ast.newFieldAccess();
			case ASTNode.FIELD_DECLARATION :
				return ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
			case ASTNode.FOR_STATEMENT :
				return ast.newForStatement();
			case ASTNode.IF_STATEMENT :
				return ast.newIfStatement();
			case ASTNode.IMPORT_DECLARATION :
				return ast.newImportDeclaration();
			case ASTNode.INFIX_EXPRESSION :
				return ast.newInfixExpression();
			case ASTNode.INITIALIZER :
				return ast.newInitializer();
			case ASTNode.INSTANCEOF_EXPRESSION :
				return ast.newInstanceofExpression();
			case ASTNode.JAVADOC :
				return ast.newJavadoc();
			case ASTNode.LABELED_STATEMENT :
				return ast.newLabeledStatement();
			case ASTNode.LINE_COMMENT :
				return ast.newLineComment();
			case ASTNode.MARKER_ANNOTATION :
				return ast.newMarkerAnnotation();
			case ASTNode.MEMBER_REF :
				return ast.newMemberRef();
			case ASTNode.MEMBER_VALUE_PAIR :
				return ast.newMemberValuePair();
			case ASTNode.METHOD_DECLARATION :
				return ast.newMethodDeclaration();
			case ASTNode.METHOD_INVOCATION :
				return ast.newMethodInvocation();
			case ASTNode.METHOD_REF :
				return ast.newMethodRef();
			case ASTNode.METHOD_REF_PARAMETER :
				return ast.newMethodRefParameter();
			case ASTNode.MODIFIER :
				return ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
			case ASTNode.NORMAL_ANNOTATION :
				return ast.newNormalAnnotation();
			case ASTNode.NULL_LITERAL :
				return ast.newNullLiteral();
			case ASTNode.NUMBER_LITERAL :
				return ast.newNumberLiteral();
			case ASTNode.PACKAGE_DECLARATION :
				return ast.newPackageDeclaration();
			case ASTNode.PARAMETERIZED_TYPE :
				return ast.newParameterizedType(ast.newSimpleName("id")); //$NON-NLS-1$
			case ASTNode.PARENTHESIZED_EXPRESSION :
				return ast.newParenthesizedExpression();
			case ASTNode.POSTFIX_EXPRESSION :
				return ast.newPostfixExpression();
			case ASTNode.PREFIX_EXPRESSION :
				return ast.newPrefixExpression();
			case ASTNode.PRIMITIVE_TYPE :
				return ast.newPrimitiveType(PrimitiveType.INT);
			case ASTNode.QUALIFIED_NAME :
				return ast.newQualifiedName(ast.newSimpleName("id"), ast.newSimpleName("id"));  //$NON-NLS-1$//$NON-NLS-2$
			case ASTNode.QUALIFIED_TYPE :
				return ast.newQualifiedType(ast.newSimpleType(ast.newSimpleName("id")), ast.newSimpleName("id")); //$NON-NLS-1$ //$NON-NLS-2$
			case ASTNode.RETURN_STATEMENT :
				return ast.newReturnStatement();
			case ASTNode.SIMPLE_NAME :
				return ast.newSimpleName("id"); //$NON-NLS-1$
			case ASTNode.SIMPLE_TYPE :
				return ast.newSimpleType(ast.newSimpleName("id")); //$NON-NLS-1$
			case ASTNode.SINGLE_MEMBER_ANNOTATION :
				return ast.newSingleMemberAnnotation();
			case ASTNode.SINGLE_VARIABLE_DECLARATION :
				return ast.newSingleVariableDeclaration();
			case ASTNode.STRING_LITERAL :
				return ast.newStringLiteral();
			case ASTNode.SUPER_CONSTRUCTOR_INVOCATION :
				return ast.newSuperConstructorInvocation();
			case ASTNode.SUPER_FIELD_ACCESS :
				return ast.newFieldAccess();
			case ASTNode.SUPER_METHOD_INVOCATION :
				return ast.newSuperMethodInvocation();
			case ASTNode.SWITCH_CASE:
				return ast.newSwitchCase();
			case ASTNode.SWITCH_STATEMENT :
				return ast.newSwitchStatement();
			case ASTNode.SYNCHRONIZED_STATEMENT :
				return ast.newSynchronizedStatement();
			case ASTNode.TAG_ELEMENT :
				return ast.newTagElement();
			case ASTNode.TEXT_ELEMENT :
				return ast.newTextElement();
			case ASTNode.THIS_EXPRESSION :
				return ast.newThisExpression();
			case ASTNode.THROW_STATEMENT :
				return ast.newThrowStatement();
			case ASTNode.TRY_STATEMENT :
				TryStatement tryStatement= ast.newTryStatement();
				tryStatement.setFinally(ast.newBlock()); // have to set at least a finally clock to be legal code
				return tryStatement;
			case ASTNode.TYPE_DECLARATION :
				return ast.newTypeDeclaration();
			case ASTNode.TYPE_DECLARATION_STATEMENT :
				return ast.newTypeDeclarationStatement(ast.newTypeDeclaration());
			case ASTNode.TYPE_LITERAL :
				return ast.newTypeLiteral();
			case ASTNode.TYPE_PARAMETER :
				return ast.newTypeParameter();
			case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
				return ast.newVariableDeclarationExpression(ast.newVariableDeclarationFragment());
			case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
				return ast.newVariableDeclarationFragment();
			case ASTNode.VARIABLE_DECLARATION_STATEMENT :
				return ast.newVariableDeclarationStatement(ast.newVariableDeclarationFragment());
			case ASTNode.WHILE_STATEMENT :
				return ast.newWhileStatement();
			case ASTNode.WILDCARD_TYPE :
				return ast.newWildcardType();
		}
		throw new IllegalArgumentException();
	}
	
	
	// collapsed nodes: in source: use one node that represents many; to be used as
	// copy/move source or to replace at once.
	// in the target: one block node that is not flattened.
	
	public Block createCollapsePlaceholder() {
		Block placeHolder= fAst.newBlock();
		if (fCollapsedNodes == null) {
			fCollapsedNodes= new HashSet();
		}
		fCollapsedNodes.add(placeHolder);
		return placeHolder;
	}
	
	public boolean isCollapsed(ASTNode node) {
		if (fCollapsedNodes != null) {
			return fCollapsedNodes.contains(node);
		}
		return false;	
	}
	
	public Object getPlaceholderData(ASTNode node) {
		if (fPlaceholderNodes != null) {
			return fPlaceholderNodes.get(node);
		}
		return null;	
	}
	
	private void setPlaceholderData(ASTNode node, PlaceholderData data) {
		if (fPlaceholderNodes == null) {
			fPlaceholderNodes= new IdentityHashMap();
		}
		fPlaceholderNodes.put(node, data);		
	}
	
	private static class PlaceholderData {
		// base class
	}
			
	protected static final class CopyPlaceholderData extends PlaceholderData {
		public CopySourceInfo copySource;
		public String toString() {
			return "[placeholder " + copySource +"]";  //$NON-NLS-1$//$NON-NLS-2$
		}
	}	
	
	protected static final class StringPlaceholderData extends PlaceholderData {
		public String code;
		public String toString() {
			return "[placeholder string: " + code +"]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * 
	 */
	public void clear() {
		fPlaceholderNodes= null;
		fCollapsedNodes= null;
	}
}
