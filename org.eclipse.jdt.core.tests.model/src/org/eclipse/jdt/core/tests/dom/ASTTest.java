/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;

import junit.framework.Test;
import org.eclipse.jdt.core.dom.*;

// testing

public class ASTTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase { 
	
	class CheckPositionsMatcher extends ASTMatcher {
		
		public CheckPositionsMatcher() {
			// include doc tags
			super(true);
		}
	
		private void checkPositions(Object source, Object destination) {
			assertTrue(source instanceof ASTNode);
			assertTrue(destination instanceof ASTNode);
			int startPosition = ((ASTNode)source).getStartPosition();
			if (startPosition != -1) {
				assertTrue(startPosition == ((ASTNode)destination).getStartPosition());
			}
			int length = ((ASTNode)source).getLength();
			if (length != 0) {
				assertTrue(length == ((ASTNode)destination).getLength());
			}
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(AnnotationTypeDeclaration, Object)
		 * @since 3.0
		 */
		public boolean match(AnnotationTypeDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(AnnotationTypeMemberDeclaration, Object)
		 * @since 3.0
		 */
		public boolean match(AnnotationTypeMemberDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(AnonymousClassDeclaration, Object)
		 */
		public boolean match(AnonymousClassDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ArrayAccess, Object)
		 */
		public boolean match(ArrayAccess node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ArrayCreation, Object)
		 */
		public boolean match(ArrayCreation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ArrayInitializer, Object)
		 */
		public boolean match(ArrayInitializer node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ArrayType, Object)
		 */
		public boolean match(ArrayType node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(AssertStatement, Object)
		 */
		public boolean match(AssertStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(Assignment, Object)
		 */
		public boolean match(Assignment node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(Block, Object)
		 */
		public boolean match(Block node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(BlockComment, Object)
         * @since 3.0
		 */
		public boolean match(BlockComment node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(BooleanLiteral, Object)
		 */
		public boolean match(BooleanLiteral node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(BreakStatement, Object)
		 */
		public boolean match(BreakStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(CastExpression, Object)
		 */
		public boolean match(CastExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(CatchClause, Object)
		 */
		public boolean match(CatchClause node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(CharacterLiteral, Object)
		 */
		public boolean match(CharacterLiteral node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ClassInstanceCreation, Object)
		 */
		public boolean match(ClassInstanceCreation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(CompilationUnit, Object)
		 */
		public boolean match(CompilationUnit node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ConditionalExpression, Object)
		 */
		public boolean match(ConditionalExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ConstructorInvocation, Object)
		 */
		public boolean match(ConstructorInvocation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ContinueStatement, Object)
		 */
		public boolean match(ContinueStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(DoStatement, Object)
		 */
		public boolean match(DoStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(EmptyStatement, Object)
		 */
		public boolean match(EmptyStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
		
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(EnhancedForStatement, Object)
		 * @since 3.0
		 */
		public boolean match(EnhancedForStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
		
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(EnumConstantDeclaration, Object)
		 * @since 3.0
		 */
		public boolean match(EnumConstantDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
		
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ExpressionStatement, Object)
		 */
		public boolean match(ExpressionStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(FieldAccess, Object)
		 */
		public boolean match(FieldAccess node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(FieldDeclaration, Object)
		 */
		public boolean match(FieldDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ForStatement, Object)
		 */
		public boolean match(ForStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(IfStatement, Object)
		 */
		public boolean match(IfStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ImportDeclaration, Object)
		 */
		public boolean match(ImportDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(InfixExpression, Object)
		 */
		public boolean match(InfixExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(Initializer, Object)
		 */
		public boolean match(Initializer node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(InstanceofExpression, Object)
		 */
		public boolean match(InstanceofExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(Javadoc, Object)
		 */
		public boolean match(Javadoc node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(LabeledStatement, Object)
		 */
		public boolean match(LabeledStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(LineComment, Object)
         * @since 3.0
		 */
		public boolean match(LineComment node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MarkerAnnotation, Object)
		 * @since 3.0
		 */
		public boolean match(MarkerAnnotation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MemberRef, Object)
         * @since 3.0
		 */
		public boolean match(MemberRef node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MemberValuePair, Object)
		 * @since 3.0
		 */
		public boolean match(MemberValuePair node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MethodDeclaration, Object)
		 */
		public boolean match(MethodDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MethodInvocation, Object)
		 */
		public boolean match(MethodInvocation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MethodRef, Object)
         * @since 3.0
		 */
		public boolean match(MethodRef node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(MethodRefParameter, Object)
         * @since 3.0
		 */
		public boolean match(MethodRefParameter node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(Modifier, Object)
		 * @since 3.0
		 */
		public boolean match(Modifier node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(NormalAnnotation, Object)
		 * @since 3.0
		 */
		public boolean match(NormalAnnotation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(NullLiteral, Object)
		 */
		public boolean match(NullLiteral node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(NumberLiteral, Object)
		 */
		public boolean match(NumberLiteral node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(PackageDeclaration, Object)
		 */
		public boolean match(PackageDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ParameterizedType, Object)
		 * @since 3.0
		 */
		public boolean match(ParameterizedType node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ParenthesizedExpression, Object)
		 */
		public boolean match(ParenthesizedExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(PostfixExpression, Object)
		 */
		public boolean match(PostfixExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(PrefixExpression, Object)
		 */
		public boolean match(PrefixExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(PrimitiveType, Object)
		 */
		public boolean match(PrimitiveType node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(QualifiedName, Object)
		 */
		public boolean match(QualifiedName node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(QualifiedType, Object)
		 * @since 3.0
		 */
		public boolean match(QualifiedType node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ReturnStatement, Object)
		 */
		public boolean match(ReturnStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SimpleName, Object)
		 */
		public boolean match(SimpleName node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SimpleType, Object)
		 */
		public boolean match(SimpleType node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SingleMemberAnnotation, Object)
		 * @since 3.0
		 */
		public boolean match(SingleMemberAnnotation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SingleVariableDeclaration, Object)
		 */
		public boolean match(SingleVariableDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(StringLiteral, Object)
		 */
		public boolean match(StringLiteral node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SuperConstructorInvocation, Object)
		 */
		public boolean match(SuperConstructorInvocation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SuperFieldAccess, Object)
		 */
		public boolean match(SuperFieldAccess node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SuperMethodInvocation, Object)
		 */
		public boolean match(SuperMethodInvocation node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SwitchCase, Object)
		 */
		public boolean match(SwitchCase node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SwitchStatement, Object)
		 */
		public boolean match(SwitchStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(SynchronizedStatement, Object)
		 */
		public boolean match(SynchronizedStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TagElement, Object)
         * @since 3.0
		 */
		public boolean match(TagElement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TextElement, Object)
         * @since 3.0
		 */
		public boolean match(TextElement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ThisExpression, Object)
		 */
		public boolean match(ThisExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(ThrowStatement, Object)
		 */
		public boolean match(ThrowStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TryStatement, Object)
		 */
		public boolean match(TryStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TypeDeclaration, Object)
		 */
		public boolean match(TypeDeclaration node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TypeDeclarationStatement, Object)
		 */
		public boolean match(TypeDeclarationStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TypeLiteral, Object)
		 */
		public boolean match(TypeLiteral node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(TypeParameter, Object)
		 * @since 3.0
		 */
		public boolean match(TypeParameter node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(VariableDeclarationExpression, Object)
		 */
		public boolean match(VariableDeclarationExpression node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(VariableDeclarationFragment, Object)
		 */
		public boolean match(VariableDeclarationFragment node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(VariableDeclarationStatement, Object)
		 */
		public boolean match(VariableDeclarationStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(WhileStatement, Object)
		 */
		public boolean match(WhileStatement node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
	
		/**
		 * @see org.eclipse.jdt.core.dom.ASTMatcher#match(WildcardType, Object)
		 * @since 3.0
		 */
		public boolean match(WildcardType node, Object other) {
			checkPositions(node, other);
			return super.match(node, other);
		}
		
	}
	
	/** @deprecated using deprecated code */
	public static Test suite() {
		// TODO (frederic) use buildList + setAstLevel(init) instead...
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTTest.class.getName());
		
		Class c = ASTTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTTest(methods[i].getName(), AST.JLS2));
				suite.addTest(new ASTTest(methods[i].getName(), AST.JLS3));
			}
		}
		return suite;
	}	
	
	AST ast;
	int API_LEVEL;
	
	
	public ASTTest(String name, int apiLevel) {
		super(name);
		this.API_LEVEL = apiLevel;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		ast = AST.newAST(this.API_LEVEL);
	}
	
	protected void tearDown() throws Exception {
		ast = null;
		super.tearDown();
	}
	
	/** @deprecated using deprecated code */
	public String getName() {
		String name = super.getName();
		switch (this.API_LEVEL) {
			case AST.JLS2:
				name = "JLS2 - " + name;
				break;
			case AST.JLS3:
				name = "JLS3 - " + name; 
				break;
		}
		return name;
	}
	
	/**
	 * Snippets that show how to...
	 * @deprecated using deprecated code
	 */
	public void testExampleSnippets() {
		{
			AST localAst = AST.newAST(ast.apiLevel());
			CompilationUnit cu = localAst.newCompilationUnit();

			// package com.example;
			PackageDeclaration pd = localAst.newPackageDeclaration();
			pd.setName(localAst.newName(new String[]{"com", "example"})); //$NON-NLS-1$ //$NON-NLS-2$
			cu.setPackage(pd);
			assertTrue(pd.getRoot() == cu);

			// import java.io;*;
			ImportDeclaration im1 = localAst.newImportDeclaration();
			im1.setName(localAst.newName(new String[]{"java", "io"})); //$NON-NLS-1$ //$NON-NLS-2$
			im1.setOnDemand(true);
			cu.imports().add(im1);
			assertTrue(im1.getRoot() == cu);
			
			// import java.util.List;
			ImportDeclaration im2 = localAst.newImportDeclaration();
			im2.setName(localAst.newName(new String[]{"java", "util", "List"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			im2.setOnDemand(false);
			cu.imports().add(im2);
			assertTrue(im2.getRoot() == cu);
			
			// /** Spec. \n @deprecated Use {@link #foo() bar} instead. */public class MyClass {}
			TypeDeclaration td = localAst.newTypeDeclaration();
			if (ast.apiLevel() == AST.JLS2) {
				td.setModifiers(Modifier.PUBLIC);
			} else {
				td.modifiers().add(localAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			}
			td.setInterface(false); 
			td.setName(localAst.newSimpleName("MyClass")); //$NON-NLS-1$
			{ 
				Javadoc jd = localAst.newJavadoc();
				TagElement tg0 = localAst.newTagElement();
				jd.tags().add(tg0);
				TextElement tx1 = localAst.newTextElement();
				tx1.setText("Spec."); //$NON-NLS-1$
				tg0.fragments().add(tx1);
				TagElement tg1 = localAst.newTagElement();
				tg1.setTagName(TagElement.TAG_DEPRECATED);
				jd.tags().add(tg1);
				TextElement tx2 = localAst.newTextElement();
				tx2.setText("Use "); //$NON-NLS-1$
				tg1.fragments().add(tx2);
				TagElement tg2 = localAst.newTagElement();
				tg2.setTagName(TagElement.TAG_LINK);
				tg1.fragments().add(tg2);
				MethodRef mr1 = localAst.newMethodRef();
				mr1.setName(localAst.newSimpleName("foo"));
				tg2.fragments().add(mr1);
				TextElement tx3 = localAst.newTextElement();
				tx3.setText("bar"); //$NON-NLS-1$
				tg2.fragments().add(tx3);
				TextElement tx4 = localAst.newTextElement();
				tx2.setText(" instead."); //$NON-NLS-1$
				tg1.fragments().add(tx4);
			}
			
			cu.types().add(td);
			assertTrue(td.getRoot() == cu);
			
			// private static boolean DEBUG = true;
			VariableDeclarationFragment f1 = localAst.newVariableDeclarationFragment();
			f1.setName(localAst.newSimpleName("DEBUG")); //$NON-NLS-1$
			f1.setInitializer(localAst.newBooleanLiteral(true));
			FieldDeclaration fd = localAst.newFieldDeclaration(f1);
			fd.setType(localAst.newPrimitiveType(PrimitiveType.BOOLEAN));
			if (ast.apiLevel() == AST.JLS2) {
				fd.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
			} else {
				fd.modifiers().add(localAst.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
				fd.modifiers().add(localAst.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
			}
			td.bodyDeclarations().add(fd);
			assertTrue(fd.getRoot() == cu);
			
			// public static void main();
			MethodDeclaration md = localAst.newMethodDeclaration();
			if (ast.apiLevel() == AST.JLS2) {
				md.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
				md.setReturnType(localAst.newPrimitiveType(PrimitiveType.VOID));
			} else {
				md.modifiers().add(localAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
				md.modifiers().add(localAst.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
				md.setReturnType2(localAst.newPrimitiveType(PrimitiveType.VOID));
			}
			md.setConstructor(false);
			md.setName(localAst.newSimpleName("main")); //$NON-NLS-1$
			td.bodyDeclarations().add(md);
			assertTrue(md.getRoot() == cu);
			
			// String[] args
			SingleVariableDeclaration a1 = localAst.newSingleVariableDeclaration();
			a1.setType(localAst.newArrayType(
				localAst.newSimpleType(localAst.newSimpleName("String")))); //$NON-NLS-1$
			a1.setName(localAst.newSimpleName("args")); //$NON-NLS-1$
			md.parameters().add(a1);
			assertTrue(a1.getRoot() == cu);
			
			// {}
			Block b = localAst.newBlock();
			md.setBody(b);
			assertTrue(b.getRoot() == cu);

			// System.out.println("hello world");		
			MethodInvocation e = localAst.newMethodInvocation();
			e.setExpression(localAst.newName(new String[] {"System", "out"})); //$NON-NLS-1$ //$NON-NLS-2$
			e.setName(localAst.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral h = localAst.newStringLiteral();
			h.setLiteralValue("hello world"); //$NON-NLS-1$
			e.arguments().add(h);
			
			b.statements().add(localAst.newExpressionStatement(e));
			assertTrue(e.getRoot() == cu);
			assertTrue(h.getRoot() == cu);
			
			// new String[len];
			ArrayCreation ac1 = localAst.newArrayCreation();
			ac1.setType(
				localAst.newArrayType(
					localAst.newSimpleType(localAst.newSimpleName("String")))); //$NON-NLS-1$
			ac1.dimensions().add(localAst.newSimpleName("len")); //$NON-NLS-1$
			b.statements().add(localAst.newExpressionStatement(ac1));
			assertTrue(ac1.getRoot() == cu);

			// new double[7][24][];
			ArrayCreation ac2 = localAst.newArrayCreation();
			ac2.setType(
				localAst.newArrayType(
					localAst.newPrimitiveType(PrimitiveType.DOUBLE), 3));
			ac2.dimensions().add(localAst.newNumberLiteral("7")); //$NON-NLS-1$
			ac2.dimensions().add(localAst.newNumberLiteral("24")); //$NON-NLS-1$
			b.statements().add(localAst.newExpressionStatement(ac2));
			assertTrue(ac2.getRoot() == cu);

			// new int[] {1, 2};
			ArrayCreation ac3 = localAst.newArrayCreation();
			ac3.setType(
				localAst.newArrayType(
					localAst.newPrimitiveType(PrimitiveType.INT)));
			ArrayInitializer ai = localAst.newArrayInitializer();
			ac3.setInitializer(ai);
			ai.expressions().add(localAst.newNumberLiteral("1")); //$NON-NLS-1$
			ai.expressions().add(localAst.newNumberLiteral("2")); //$NON-NLS-1$
			b.statements().add(localAst.newExpressionStatement(ac3));
			assertTrue(ac3.getRoot() == cu);
			assertTrue(ai.getRoot() == cu);
			
			// new String(10);
			ClassInstanceCreation cr1 = localAst.newClassInstanceCreation();
			if (ast.apiLevel() == AST.JLS2) {
				cr1.setName(localAst.newSimpleName("String")); //$NON-NLS-1$
			} else {
				cr1.setType(localAst.newSimpleType(localAst.newSimpleName("String"))); //$NON-NLS-1$
			}
			cr1.arguments().add(localAst.newNumberLiteral("10"));		 //$NON-NLS-1$
			b.statements().add(localAst.newExpressionStatement(cr1));
			assertTrue(cr1.getRoot() == cu);

			// new Listener() {public void handleEvent() {} };
			ClassInstanceCreation cr2 = localAst.newClassInstanceCreation();
			AnonymousClassDeclaration ad1 = localAst.newAnonymousClassDeclaration();
			cr2.setAnonymousClassDeclaration(ad1);
			if (ast.apiLevel() == AST.JLS2) {
				cr2.setName(localAst.newSimpleName("Listener")); //$NON-NLS-1$
			} else {
				cr2.setType(localAst.newSimpleType(localAst.newSimpleName("Listener"))); //$NON-NLS-1$
			}
			MethodDeclaration md0 = localAst.newMethodDeclaration();
			if (ast.apiLevel() == AST.JLS2) {
				md0.setModifiers(Modifier.PUBLIC);
			} else {
				md0.modifiers().add(localAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			}
			md0.setName(localAst.newSimpleName("handleEvent")); //$NON-NLS-1$
			md0.setBody(localAst.newBlock());
			ad1.bodyDeclarations().add(md0);
			b.statements().add(localAst.newExpressionStatement(cr2));
			assertTrue(cr2.getRoot() == cu);
			assertTrue(md0.getRoot() == cu);
			assertTrue(ad1.getRoot() == cu);

		}
	}
	
	abstract class Property {
		
		/**
		 * Indicates whether this property is compulsory, in that every node
		 * must have a value at all times.
		 */
		private boolean compulsory;

		private Class nodeType;		
		private String propertyName;

		/**
		 * Creates a new property with the given name.
		 */
		Property(String propertyName, boolean compulsory, Class nodeType) {
			this.propertyName = propertyName;
			this.compulsory = compulsory;
			this.nodeType = nodeType;
		}
		
		/**
		 * Returns a sample node of a type suitable for storing
		 * in this property.
		 * 
		 * @param targetAST the target AST
		 * @param parented <code>true</code> if the sample should be
		 *    parented, and <code>false</code> if unparented
		 * @return a sample node
		 */
		public abstract ASTNode sample(AST targetAST, boolean parented);

		/**
		 * Returns examples of node of types unsuitable for storing
		 * in this property.
		 * <p>
		 * This implementation returns an empty list. Subclasses
		 * should reimplement to specify counter-examples.
		 * </p>
		 * 
		 * @param targetAST the target AST
		 * @return a list of counter-example nodes
		 */
		public ASTNode[] counterExamples(AST targetAST) {
			return new ASTNode[] {};
		}

		/**
		 * Returns a sample node of a type suitable for storing
		 * in this property. The sample embeds the node itself.
		 * <p>
		 * For instance, for an Expression-valued property of a given
		 * Statement, this method returns an Expression that embeds
		 * this Statement node (as a descendent).
		 * </p>
		 * <p>
		 * Returns <code>null</code> if such an embedding is impossible.
		 * For instance, for an Name-valued property of a given
		 * Statement, this method returns <code>null</code> because
		 * an Expression cannot be embedded in a Name.
		 * </p>
		 * <p>
		 * This implementation returns <code>null</code>. Subclasses
		 * should reimplement to specify an embedding.
		 * </p>
		 * 
		 * @return a sample node that embeds the given node,
		 *    and <code>null</code> if such an embedding is impossible
		 */
		public ASTNode wrap() {
			return null;
		}
		
		/**
		 * Undoes the effects of a previous <code>wrap</code>.
		 * <p>
		 * This implementation does nothing. Subclasses
		 * should reimplement if they reimplement <code>wrap</code>.
		 * </p>
		 */
		public void unwrap() {
		}
		
		/**
		 * Returns whether this property is compulsory, in that every node
		 * must have a value at all times.
		 * 
		 * @return <code>true</code> if the property is compulsory,
		 *    and <code>false</code> if the property may be null
		 */
		public final boolean isCompulsory() {
			return compulsory;
		}
		
		/**
		 * Returns the value of this property.
		 * <p>
		 * This implementation throws an unchecked exception. Subclasses 
		 * should reimplement.
		 * </p>
		 * 
		 * @return the property value, or <code>null</code> if no value
		 */
		public ASTNode get() {
			throw new RuntimeException("get not implemented"); //$NON-NLS-1$
		}
		
		/**
		 * Sets or clears the value of this property.
		 * <p>
		 * This implementation throws an unchecked exception. Subclasses 
		 * should reimplement.
		 * </p>
		 * 
		 * @param value the property value, or <code>null</code> if no value
		 */
		public void set(ASTNode value) {
			throw new RuntimeException("get(" + value + ") not implemented"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public String toString() {
			return "Property(" + this.propertyName + ", " + this.compulsory + ", " + this.nodeType + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	/**
	 * Exercises the given property of the given node.
	 * 
	 * @param node the node to test
	 * @param prop the property descriptor
	 */
	void genericPropertyTest(ASTNode node, Property prop) {
		
		ASTNode x1 = prop.sample(node.getAST(), false);
		prop.set(x1);
		assertTrue(prop.get() == x1);
		assertTrue(x1.getParent() == node);
		
		// check handling of null
		if (prop.isCompulsory()) {
			try {
				prop.set(null);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		} else {
			long previousCount = node.getAST().modificationCount();
			prop.set(null);
			assertTrue(prop.get() == null);
			assertTrue(node.getAST().modificationCount() > previousCount);
		}			

		// check that a child from a different AST is detected
		try {
			AST newAST = AST.newAST(node.getAST().apiLevel());
			prop.set(prop.sample(newAST, false));
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a child with a parent is detected
		try {
			ASTNode b1 = prop.sample(node.getAST(), true);
			prop.set(b1); // bogus: already has parent
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a cycle is detected
		assertTrue(node.getParent() == null);
		ASTNode s1 = null;
		try {
			s1 = prop.wrap();
			if (s1 != null) {
				prop.set(s1);  // bogus: creates a cycle
				assertTrue(false);
			}
		} catch (RuntimeException e) {
			// pass
		} finally {
			if (s1 != null) {
				prop.unwrap();
				assertTrue(node.getParent() == null);
			}
		}
		
		// check that a child of the wrong type is detected
		ASTNode b1[] = prop.counterExamples(node.getAST());
		for (int i = 0; i < b1.length; i++) {
			try {
				prop.set(b1[i]); // bogus: wrong type
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}

	}

	/**
	 * Exercises the given property of the given node.
	 * 
	 * @param node the node to test
	 * @param children the node to test
	 * @param prop the property descriptor
	 */
	void genericPropertyListTest(ASTNode node, List children, Property prop) {
		
		// wipe the slate clean
		children.clear();
		assertTrue(children.size() == 0);
		
		// add a child
		ASTNode x1 = prop.sample(node.getAST(), false);
		long previousCount = node.getAST().modificationCount();
		children.add(x1);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 1);
		assertTrue(children.get(0) == x1);
		assertTrue(x1.getParent() == node);
		
		// add a second child
		ASTNode x2 = prop.sample(node.getAST(), false);
		previousCount = node.getAST().modificationCount();
		children.add(x2);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 2);
		assertTrue(children.get(0) == x1);
		assertTrue(children.get(1) == x2);
		assertTrue(x1.getParent() == node);
		assertTrue(x2.getParent() == node);

		// remove the first child
		previousCount = node.getAST().modificationCount();
		children.remove(0);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 1);
		assertTrue(children.get(0) == x2);
		assertTrue(x1.getParent() == null);
		assertTrue(x2.getParent() == node);

		// remove the remaining child
		previousCount = node.getAST().modificationCount();
		children.remove(x2);
		assertTrue(node.getAST().modificationCount() > previousCount);
		assertTrue(children.size() == 0);
		assertTrue(x1.getParent() == null);
		assertTrue(x2.getParent() == null);

		// check that null is never allowed
		try {
			children.add(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a child from a different AST is detected
		try {
			AST newAST = AST.newAST(node.getAST().apiLevel());
			children.add(prop.sample(newAST, false));
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a child with a parent is detected
		try {
			ASTNode b1 = prop.sample(node.getAST(), true);
			children.add(b1); // bogus: already has parent
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that a cycle is detected
		assertTrue(node.getParent() == null);
		ASTNode s1 = null;
		try {
			s1 = prop.wrap();
			if (s1 != null) {
				children.add(s1);  // bogus: creates a cycle
				assertTrue(false);
			}
		} catch (RuntimeException e) {
			// pass
		} finally {
			if (s1 != null) {
				prop.unwrap();
				assertTrue(node.getParent() == null);
			}
		}
		
		// check that a child of the wrong type is detected
		ASTNode b1[] = prop.counterExamples(node.getAST());
		for (int i = 0; i < b1.length; i++) {
			try {
				children.add(b1[i]); // bogus: wrong type
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}
		
	}

	/** @deprecated using deprecated code */
	public void testAST() {
		
		assertTrue(AST.JLS2 == 2);
		assertTrue(AST.JLS3 == 3);
		
		AST a0 = new AST(); // deprecated, but still 2.0
		assertTrue(a0.apiLevel() == AST.JLS2);
		AST a1 = new AST(new HashMap()); // deprecated, but still 2.0
		assertTrue(a1.apiLevel() == AST.JLS2);
		AST a2 = AST.newAST(AST.JLS2);
		assertTrue(a2.apiLevel() == AST.JLS2);
		AST a3 = AST.newAST(AST.JLS3);
		assertTrue(a3.apiLevel() == AST.JLS3);

		// modification count is always non-negative
		assertTrue(ast.modificationCount() >= 0);
		
		// modification count increases for node creations
		long previousCount = ast.modificationCount();
		SimpleName x = ast.newSimpleName("first"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);

		// modification count does not increase for reading node attributes
		previousCount = ast.modificationCount();
		x.getIdentifier();
		x.getParent();
		x.getRoot();
		x.getAST();
		x.getFlags();
		x.getStartPosition();
		x.getLength();
		x.equals(x);
		assertTrue(ast.modificationCount() == previousCount);

		// modification count does not increase for reading or writing properties
		previousCount = ast.modificationCount();
		x.getProperty("any"); //$NON-NLS-1$
		x.setProperty("any", "value"); // N.B. //$NON-NLS-1$ //$NON-NLS-2$
		x.properties();
		assertTrue(ast.modificationCount() == previousCount);

		// modification count increases for changing node attributes
		previousCount = ast.modificationCount();
		x.setIdentifier("second"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		
		previousCount = ast.modificationCount();
		x.setFlags(0);
		assertTrue(ast.modificationCount() > previousCount);
		
		previousCount = ast.modificationCount();
		x.setSourceRange(-1,0);
		assertTrue(ast.modificationCount() > previousCount);
	}	
	
	public void testWellKnownBindings() {

		// well known bindings
		String[] wkbs = {
			"byte", "char", "short", "int", "long", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"boolean", "float", "double", "void", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"java.lang.Class", //$NON-NLS-1$
			"java.lang.Cloneable", //$NON-NLS-1$
			"java.lang.Error", //$NON-NLS-1$
			"java.lang.Exception", //$NON-NLS-1$
			"java.lang.Object", //$NON-NLS-1$
			"java.lang.RuntimeException", //$NON-NLS-1$
			"java.lang.String", //$NON-NLS-1$
			"java.lang.StringBuffer", //$NON-NLS-1$
			"java.lang.Throwable", //$NON-NLS-1$
			"java.io.Serializable", //$NON-NLS-1$
			"java.lang.Boolean", //$NON-NLS-1$
			"java.lang.Byte", //$NON-NLS-1$
			"java.lang.Character", //$NON-NLS-1$
			"java.lang.Double", //$NON-NLS-1$
			"java.lang.Float", //$NON-NLS-1$
			"java.lang.Integer", //$NON-NLS-1$
			"java.lang.Long", //$NON-NLS-1$
			"java.lang.Short", //$NON-NLS-1$
			"java.lang.Void", //$NON-NLS-1$
		};
		
		// no-so-well-known bindings
		String[] nwkbs = {
			"verylong", //$NON-NLS-1$
			"java.lang.Math", //$NON-NLS-1$
			"com.example.MyCode", //$NON-NLS-1$
		};
	
		// none of the well known bindings resolve in a plain AST		
		for (int i = 0; i<wkbs.length; i++) {
			assertTrue(ast.resolveWellKnownType(wkbs[i]) == null);
		}
	
		// none of the no so well known bindings resolve either		
		for (int i = 0; i<nwkbs.length; i++) {
			assertTrue(ast.resolveWellKnownType(nwkbs[i]) == null);
		}
	}
	
	public void testSimpleName() {
		long previousCount = ast.modificationCount();
		SimpleName x = ast.newSimpleName("foo"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("foo".equals(x.getIdentifier())); //$NON-NLS-1$
		assertTrue("foo".equals(x.getFullyQualifiedName())); //$NON-NLS-1$
		assertTrue(x.getNodeType() == ASTNode.SIMPLE_NAME);
		assertTrue(x.isDeclaration() == false);
		assertTrue(x.structuralPropertiesForType() == SimpleName.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		previousCount = ast.modificationCount();
		x.setIdentifier("bar"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("bar".equals(x.getIdentifier())); //$NON-NLS-1$
		assertTrue("bar".equals(x.getFullyQualifiedName())); //$NON-NLS-1$

		// check that property cannot be set to null
		try {
			x.setIdentifier(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		
		// check that property cannot be set to keyword or reserved work
		String[] reserved  = 
				new String[] {
						"true", "false", "null", // literals //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"abstract", "default", "if", "private", "this", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"boolean", "do", "implements", "protected", "throw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"break", "double", "import", "public", "throws", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"byte", "else", "instanceof", "return", "transient", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"case", "extends", "int", "short", "try", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"catch", "final", "interface", "static", "void", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"char", "finally", "long", "strictfp", "volatile", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"class", "float", "native", "super", "while", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						"const", "for", "new", "switch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"continue", "goto", "package", "synchronized"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		for (int i=0; i<reserved.length; i++) {
			try {
				x.setIdentifier(reserved[i]);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}
		
		// check that property cannot be set to keyword or reserved work
		String[] bogus  = 
				new String[] {
						"a b", "a ", " a", // literals //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"a-b", "a[]", "a<T>", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
						"", " ", "a.b"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<bogus.length; i++) {
			try {
				x.setIdentifier(bogus[i]);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}
		// check that "assert" is not considered a keyword
		// "assert" only became a keyword in J2SE 1.4 and we do *not* want to
		// preclude the AST API from being used to analyze pre-1.4 code
		x.setIdentifier("assert"); //$NON-NLS-1$
		
		// check that "enum" is not considered a keyword
		// "enum" only became a keyword in J2SE 1.5 and we do *not* want to
		// preclude the AST API from being used to analyze pre-1.5 code
		x.setIdentifier("enum"); //$NON-NLS-1$
		
		// check that isDeclaration works
		QualifiedName y = ast.newQualifiedName(ast.newSimpleName("a"), x); //$NON-NLS-1$
		assertTrue(x.isDeclaration() == false);
		y.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		assertTrue(x.isDeclaration() == false);

		TypeDeclaration td = ast.newTypeDeclaration();
		td.setName(x);
		assertTrue(x.isDeclaration() == true);
		td.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		assertTrue(x.isDeclaration() == false);
		
		MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(x);
		assertTrue(x.isDeclaration() == true);
		md.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		assertTrue(x.isDeclaration() == false);
		
		SingleVariableDeclaration vd = ast.newSingleVariableDeclaration();
		vd.setName(x);
		assertTrue(x.isDeclaration() == true);
		vd.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		assertTrue(x.isDeclaration() == false);
		
		VariableDeclarationFragment fd = ast.newVariableDeclarationFragment();
		fd.setName(x);
		assertTrue(x.isDeclaration() == true);
		fd.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		assertTrue(x.isDeclaration() == false);
		
		if (ast.apiLevel() >= AST.JLS3) {
			AnnotationTypeDeclaration atd = ast.newAnnotationTypeDeclaration();
			atd.setName(x);
			assertTrue(x.isDeclaration() == true);
			atd.setName(ast.newSimpleName("b")); //$NON-NLS-1$
			assertTrue(x.isDeclaration() == false);
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			AnnotationTypeMemberDeclaration atmd = ast.newAnnotationTypeMemberDeclaration();
			atmd.setName(x);
			assertTrue(x.isDeclaration() == true);
			atmd.setName(ast.newSimpleName("b")); //$NON-NLS-1$
			assertTrue(x.isDeclaration() == false);
		}
		
	}		

	public void testQualifiedName() {
		long previousCount = ast.modificationCount();
		final QualifiedName x = ast.newQualifiedName(
			ast.newSimpleName("q"), //$NON-NLS-1$
			ast.newSimpleName("i")); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getQualifier().getParent() == x);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == false);
		assertTrue(x.getNodeType() == ASTNode.QUALIFIED_NAME);
		assertTrue("q.i".equals(x.getFullyQualifiedName())); //$NON-NLS-1$
		assertTrue(x.structuralPropertiesForType() == QualifiedName.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Qualifier", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"), //$NON-NLS-1$
					targetAst.newSimpleName("b")); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				QualifiedName s1 = ast.newQualifiedName(x, ast.newSimpleName("z")); //$NON-NLS-1$
				return s1;
			}
			public void unwrap() {
				QualifiedName s1 = (QualifiedName) x.getParent();
				s1.setQualifier(ast.newSimpleName("z")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		// test fullyQualifiedName on nested names
		Name q0 = ast.newName(new String[] {"a", "bb", "ccc", "dddd", "eeeee", "ffffff"});
		assertTrue("a.bb.ccc.dddd.eeeee.ffffff".equals(q0.getFullyQualifiedName())); //$NON-NLS-1$

	}		

	public void testNameFactories() {
		long previousCount = ast.modificationCount();
		Name x = ast.newName("foo"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("foo".equals(x.getFullyQualifiedName())); //$NON-NLS-1$
		assertTrue(x.getNodeType() == ASTNode.SIMPLE_NAME);

		previousCount = ast.modificationCount();
		x = ast.newName("foo.bar"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("foo.bar".equals(x.getFullyQualifiedName())); //$NON-NLS-1$
		assertTrue(x.getNodeType() == ASTNode.QUALIFIED_NAME);
		QualifiedName q = (QualifiedName) x;
		assertTrue("bar".equals(q.getName().getFullyQualifiedName())); //$NON-NLS-1$
		assertTrue("foo".equals(q.getQualifier().getFullyQualifiedName())); //$NON-NLS-1$
		
		// check that simple and qualified names work
		String[] legal  = 
				new String[] {
						"a", "abcdef", "XYZZY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"a.b", "java.lang.Object", "a.b.c.d.e"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		for (int i=0; i<legal.length; i++) {
			try {
				x = ast.newName(legal[i]);
				assertTrue(legal[i].equals(x.getFullyQualifiedName()));
			} catch (RuntimeException e) {
				assertTrue(false);
			}
		}
		
		// check that property cannot be set to keyword or reserved work
		String[] bogus  = 
				new String[] {
						"", ".", ".a", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"a.", "a..b", "..a"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<bogus.length; i++) {
			try {
				x = ast.newName(bogus[i]);
				assertTrue(false);
			} catch (RuntimeException e) {
				// pass
			}
		}
	}		
	
	public void testNullLiteral() {
		long previousCount = ast.modificationCount();
		NullLiteral x = ast.newNullLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.NULL_LITERAL);
		assertTrue(x.structuralPropertiesForType() == NullLiteral.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

	}		

	public void testBooleanLiteral() {
		long previousCount = ast.modificationCount();
		BooleanLiteral x = ast.newBooleanLiteral(true);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.booleanValue() == true);
		assertTrue(x.getNodeType() == ASTNode.BOOLEAN_LITERAL);
		assertTrue(x.structuralPropertiesForType() == BooleanLiteral.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		previousCount = ast.modificationCount();
		x.setBooleanValue(false);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.booleanValue() == false);
		
		previousCount = ast.modificationCount();
		x.setBooleanValue(true);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.booleanValue() == true);
	}		
	
	public void testStringLiteral() {
		long previousCount = ast.modificationCount();
		// check 0-arg factory first
		StringLiteral x = ast.newStringLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("\"\"".equals(x.getEscapedValue())); //$NON-NLS-1$
		assertTrue("".equals(x.getLiteralValue())); //$NON-NLS-1$
		assertTrue(x.getNodeType() == ASTNode.STRING_LITERAL);
		assertTrue(x.structuralPropertiesForType() == StringLiteral.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		previousCount = ast.modificationCount();
		x.setEscapedValue("\"bye\""); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\"bye\"".equals(x.getEscapedValue())); //$NON-NLS-1$
		assertTrue("bye".equals(x.getLiteralValue())); //$NON-NLS-1$

		previousCount = ast.modificationCount();
		x.setLiteralValue("hi"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\"hi\"".equals(x.getEscapedValue())); //$NON-NLS-1$
		assertTrue("hi".equals(x.getLiteralValue())); //$NON-NLS-1$

		previousCount = ast.modificationCount();
		x.setLiteralValue("\\012\\015"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertEquals("different", "\"\\\\012\\\\015\"", x.getEscapedValue()); //$NON-NLS-1$
		assertTrue("\\012\\015".equals(x.getLiteralValue())); //$NON-NLS-1$

		previousCount = ast.modificationCount();
		x.setLiteralValue("\012\015"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\n\r".equals(x.getLiteralValue())); //$NON-NLS-1$
		assertEquals("different", "\"\\n\\r\"", x.getEscapedValue()); //$NON-NLS-1$

		// check that property cannot be set to null
		try {
			x.setEscapedValue(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check that property cannot be set to null
		try {
			x.setLiteralValue(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
	}		

	public void testStringLiteralUnicode() {
		AST localAst = AST.newAST(ast.apiLevel());
		StringLiteral literal = localAst.newStringLiteral();
		literal.setEscapedValue("\"hello\\u0026\\u0050worl\\u0064\""); //$NON-NLS-1$
		assertTrue(literal.getLiteralValue().equals("hello&Pworld")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setEscapedValue("\"hello\\nworld\""); //$NON-NLS-1$
		assertTrue(literal.getLiteralValue().equals("hello\nworld")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setLiteralValue("hello\nworld"); //$NON-NLS-1$
		assertTrue(literal.getLiteralValue().equals("hello\nworld")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setLiteralValue("\n"); //$NON-NLS-1$
		assertTrue(literal.getEscapedValue().equals("\"\\n\"")); //$NON-NLS-1$
		assertTrue(literal.getLiteralValue().equals("\n")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setEscapedValue("\"hello\\\"world\""); //$NON-NLS-1$
		assertTrue(literal.getLiteralValue().equals("hello\"world")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setLiteralValue("hello\\u0026world"); //$NON-NLS-1$
		assertTrue(literal.getLiteralValue().equals("hello\\u0026world")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setLiteralValue("hello\\u0026world"); //$NON-NLS-1$
		assertTrue(literal.getEscapedValue().equals("\"hello\\\\u0026world\"")); //$NON-NLS-1$
		
		localAst = AST.newAST(ast.apiLevel());
		literal = localAst.newStringLiteral();
		literal.setLiteralValue("\\u0001"); //$NON-NLS-1$
		assertTrue(literal.getEscapedValue().equals("\"\\\\u0001\"")); //$NON-NLS-1$
	}		
	
	public void testCharacterLiteral() {
		long previousCount = ast.modificationCount();
		CharacterLiteral x = ast.newCharacterLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getEscapedValue().startsWith("\'")); //$NON-NLS-1$
		assertTrue(x.getEscapedValue().endsWith("\'")); //$NON-NLS-1$
		assertTrue(x.getNodeType() == ASTNode.CHARACTER_LITERAL);
		assertTrue(x.structuralPropertiesForType() == CharacterLiteral.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		previousCount = ast.modificationCount();
		x.setEscapedValue("\'z\'"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue("\'z\'".equals(x.getEscapedValue())); //$NON-NLS-1$
		assertTrue(x.charValue() == 'z');

		try {
			x.setEscapedValue("\"z\""); //$NON-NLS-1$
			assertTrue(false);
		} catch(IllegalArgumentException e) {
			// pass
		}

		// test other factory method
		previousCount = ast.modificationCount();
		CharacterLiteral y = ast.newCharacterLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(y.getAST() == ast);
		assertTrue(y.getParent() == null);
		String v = y.getEscapedValue();
		assertTrue(v.length() >= 3 && v.charAt(0) == '\'' & v.charAt(v.length()-1 ) == '\'');

		// check that property cannot be set to null
		try {
			x.setEscapedValue(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		
		// test escaped characters
		// b, t, n, f, r, ", ', \, 0, 1, 2, 3, 4, 5, 6, or 7
		try {
			x.setEscapedValue("\'\\b\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\t\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\n\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\f\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\\"\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\'\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\\\\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\0\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\1\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\2\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\3\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\4\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\5\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\6\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\7\'"); //$NON-NLS-1$
			x.setEscapedValue("\'\\u0041\'"); //$NON-NLS-1$
			assertTrue(x.charValue() == 'A');
		} catch(IllegalArgumentException e) {
			assertTrue(false);
		}
		
		x.setCharValue('\u0041');
		assertTrue(x.getEscapedValue().equals("\'A\'")); //$NON-NLS-1$
		x.setCharValue('\t');
		assertTrue(x.getEscapedValue().equals("\'\\t\'")); //$NON-NLS-1$
		x.setEscapedValue("\'\\\\\'"); //$NON-NLS-1$
		assertTrue(x.getEscapedValue().equals("\'\\\\\'")); //$NON-NLS-1$
		assertTrue(x.charValue() == '\\');
		x.setEscapedValue("\'\\\'\'"); //$NON-NLS-1$
		assertTrue(x.getEscapedValue().equals("\'\\\'\'")); //$NON-NLS-1$
		assertTrue(x.charValue() == '\'');
		x.setCharValue('\'');
		assertTrue(x.getEscapedValue().equals("\'\\\'\'")); //$NON-NLS-1$
		assertTrue(x.charValue() == '\'');
		x.setCharValue('\\');
		assertTrue(x.getEscapedValue().equals("\'\\\\\'")); //$NON-NLS-1$
		assertTrue(x.charValue() == '\\');
		x.setCharValue('\b');
		assertTrue(x.getEscapedValue().equals("\'\\b\'")); //$NON-NLS-1$
		x.setCharValue('\n');
		assertTrue(x.getEscapedValue().equals("\'\\n\'")); //$NON-NLS-1$
		x.setCharValue('\f');
		assertTrue(x.getEscapedValue().equals("\'\\f\'")); //$NON-NLS-1$
		x.setCharValue('\r');
		assertTrue(x.getEscapedValue().equals("\'\\r\'")); //$NON-NLS-1$
		x.setCharValue('\"');
		assertTrue(x.getEscapedValue().equals("\'\\\"\'")); //$NON-NLS-1$
		x.setCharValue('\0');
		assertTrue(x.getEscapedValue().equals("\'\\0\'")); //$NON-NLS-1$
		x.setCharValue('\1');
		assertTrue(x.getEscapedValue().equals("\'\\1\'")); //$NON-NLS-1$
		x.setCharValue('\2');
		assertTrue(x.getEscapedValue().equals("\'\\2\'")); //$NON-NLS-1$
		x.setCharValue('\3');
		assertTrue(x.getEscapedValue().equals("\'\\3\'")); //$NON-NLS-1$
		x.setCharValue('\4');
		assertTrue(x.getEscapedValue().equals("\'\\4\'")); //$NON-NLS-1$
		x.setCharValue('\5');
		assertTrue(x.getEscapedValue().equals("\'\\5\'")); //$NON-NLS-1$
		x.setCharValue('\6');
		assertTrue(x.getEscapedValue().equals("\'\\6\'")); //$NON-NLS-1$
		x.setCharValue('\7');
		assertTrue(x.getEscapedValue().equals("\'\\7\'")); //$NON-NLS-1$
	}

	public void testNumberLiteral() {
		long previousCount = ast.modificationCount();
		NumberLiteral x = ast.newNumberLiteral("1234"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue("1234".equals(x.getToken())); //$NON-NLS-1$
		assertTrue(x.getNodeType() == ASTNode.NUMBER_LITERAL);
		assertTrue(x.structuralPropertiesForType() == NumberLiteral.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		// test other factory method
		previousCount = ast.modificationCount();
		NumberLiteral y = ast.newNumberLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(y.getAST() == ast);
		assertTrue(y.getParent() == null);
		assertTrue("0".equals(y.getToken())); //$NON-NLS-1$

		final String[] samples =
			{ "0", "1", "1234567890", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "0L", "1L", "1234567890L", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "0l", "1l", "1234567890l", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "077", "0177", "012345670", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "077L", "0177L", "012345670L", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "077l", "0177l", "012345670l", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "0x00", "0x1", "0x0123456789ABCDEF", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "0x00L", "0x1L", "0x0123456789ABCDEFL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "0x00l", "0x1l", "0x0123456789ABCDEFl", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  "1e1f", "2.f", ".3f", "0f", "3.14f", "6.022137e+23f", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			  "1e1", "2.", ".3", "0.0", "3.14", "1e-9d", "1e137", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			};
		for (int i = 0; i < samples.length; i++) {			
			previousCount = ast.modificationCount();
			x.setToken(samples[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(samples[i].equals(x.getToken()));
		}

		// check that property cannot be set to null
		try {
			x.setToken(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

	}		

	public void testSimpleType() {
		long previousCount = ast.modificationCount();
		final SimpleType x = ast.newSimpleType(ast.newSimpleName("String")); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(!x.isParameterizedType());
		assertTrue(!x.isQualifiedType());
		assertTrue(!x.isWildcardType());
		assertTrue(x.getNodeType() == ASTNode.SIMPLE_TYPE);
		assertTrue(x.structuralPropertiesForType() == SimpleType.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Name", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});
	}		
	
	public void testPrimitiveType() {
		long previousCount = ast.modificationCount();
		PrimitiveType x = ast.newPrimitiveType(PrimitiveType.INT);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getPrimitiveTypeCode().equals(PrimitiveType.INT));
		assertTrue(!x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(x.isPrimitiveType());
		assertTrue(!x.isParameterizedType());
		assertTrue(!x.isQualifiedType());
		assertTrue(!x.isWildcardType());
		assertTrue(x.getNodeType() == ASTNode.PRIMITIVE_TYPE);
		assertTrue(x.structuralPropertiesForType() == PrimitiveType.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// check the names of the primitive type codes
		assertTrue(PrimitiveType.BYTE.toString().equals("byte")); //$NON-NLS-1$
		assertTrue(PrimitiveType.INT.toString().equals("int")); //$NON-NLS-1$
		assertTrue(PrimitiveType.BOOLEAN.toString().equals("boolean")); //$NON-NLS-1$
		assertTrue(PrimitiveType.CHAR.toString().equals("char")); //$NON-NLS-1$
		assertTrue(PrimitiveType.SHORT.toString().equals("short")); //$NON-NLS-1$
		assertTrue(PrimitiveType.LONG.toString().equals("long")); //$NON-NLS-1$
		assertTrue(PrimitiveType.FLOAT.toString().equals("float")); //$NON-NLS-1$
		assertTrue(PrimitiveType.DOUBLE.toString().equals("double")); //$NON-NLS-1$
		assertTrue(PrimitiveType.VOID.toString().equals("void")); //$NON-NLS-1$

		
		PrimitiveType.Code[] known = {
			PrimitiveType.BOOLEAN,
			PrimitiveType.BYTE,
			PrimitiveType.CHAR,
			PrimitiveType.INT,
			PrimitiveType.SHORT,
			PrimitiveType.LONG,
			PrimitiveType.FLOAT,
			PrimitiveType.DOUBLE,
			PrimitiveType.VOID,
		};
		
		// check all primitive type codes are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all primitive type codes work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setPrimitiveTypeCode(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getPrimitiveTypeCode().equals(known[i]));
		}
		// ensure null does not work as a primitive type code
		try {
			x.setPrimitiveTypeCode(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toCode lookup of primitive type code by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(PrimitiveType.toCode(name).equals(known[i]));
		}
		assertTrue(PrimitiveType.toCode("not-a-type") == null); //$NON-NLS-1$
	}		
	
	public void testArrayType() {
		SimpleName x1 = ast.newSimpleName("String"); //$NON-NLS-1$
		SimpleType x2 = ast.newSimpleType(x1);
		long previousCount = ast.modificationCount();
		final ArrayType x = ast.newArrayType(x2);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getComponentType().getParent() == x);
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		assertTrue(!x.isSimpleType());
		assertTrue(x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(!x.isParameterizedType());
		assertTrue(!x.isQualifiedType());
		assertTrue(!x.isWildcardType());
		assertTrue(x.getNodeType() == ASTNode.ARRAY_TYPE);
		assertTrue(x.structuralPropertiesForType() == ArrayType.propertyDescriptors(ast.apiLevel()));

		assertTrue(x.getDimensions() == 1);
		assertTrue(x.getElementType() == x2);

		genericPropertyTest(x, new Property("ComponentType", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("a")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ArrayType result = ast.newArrayType(x);
				return result;
			}
			public void unwrap() {
				ArrayType a = (ArrayType) x.getParent();
				a.setComponentType(ast.newPrimitiveType(PrimitiveType.INT));
			}
			public ASTNode get() {
				return x.getComponentType();
			}
			public void set(ASTNode value) {
				x.setComponentType((Type) value);
			}
		});
		
		x.setComponentType(
			ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT), 4));
			
		assertTrue(x.getDimensions() == 5);
		assertTrue(x.getElementType().isPrimitiveType());
	}		

	/** @deprecated using deprecated code */
	public void testParameterizedType() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("String"))); //$NON-NLS-1$
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		Type t = ast.newSimpleType(ast.newSimpleName("String")); //$NON-NLS-1$
		final ParameterizedType x = ast.newParameterizedType(t); 
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType() == t);
		assertTrue(x.getType().getParent() == x);
		assertTrue(!x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(x.isParameterizedType());
		assertTrue(!x.isQualifiedType());
		assertTrue(!x.isWildcardType());
		assertTrue(x.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		assertTrue(x.typeArguments().size() == 0);
		assertTrue(x.structuralPropertiesForType() == ParameterizedType.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = 
				targetAst.newSimpleType(
						targetAst.newSimpleName("a")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParameterizedType s1 = ast.newParameterizedType(x); //$NON-NLS-1$
				return s1;
			}
			public void unwrap() {
				ParameterizedType s1 = (ParameterizedType) x.getParent();
				s1.setType(ast.newSimpleType(ast.newSimpleName("z"))); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
		genericPropertyListTest(x, x.typeArguments(),
		  new Property("Arguments", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				PrimitiveType result = targetAst.newPrimitiveType(PrimitiveType.INT);
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Type that embeds x
				ParameterizedType s1 = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("foo"))); //$NON-NLS-1$
				s1.typeArguments().add(x);
				return s1;
			}
			public void unwrap() {
				ParameterizedType s1 = (ParameterizedType) x.getParent();
				s1.typeArguments().remove(x);
			}
		});
	}		
	
	/** @deprecated using deprecated code */
	public void testQualifiedType() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newQualifiedType(
						ast.newSimpleType(ast.newSimpleName("q")), //$NON-NLS-1$
						ast.newSimpleName("i")); //$NON-NLS-1$
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final QualifiedType x = ast.newQualifiedType(
				ast.newSimpleType(ast.newSimpleName("q")), //$NON-NLS-1$
				ast.newSimpleName("i")); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getQualifier().getParent() == x);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == false);
		assertTrue(x.getNodeType() == ASTNode.QUALIFIED_TYPE);
		assertTrue(!x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(!x.isParameterizedType());
		assertTrue(x.isQualifiedType());
		assertTrue(!x.isWildcardType());
		assertTrue(x.structuralPropertiesForType() == QualifiedType.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Qualifier", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = 
				targetAst.newSimpleType(
						targetAst.newSimpleName("a")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				QualifiedType s1 = ast.newQualifiedType(x, ast.newSimpleName("z")); //$NON-NLS-1$
				return s1;
			}
			public void unwrap() {
				QualifiedType s1 = (QualifiedType) x.getParent();
				s1.setQualifier(ast.newSimpleType(ast.newSimpleName("z"))); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Type) value);
			}
		});
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}		

	/** @deprecated using deprecated code */
	public void testWildcardType() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newWildcardType();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final WildcardType x = ast.newWildcardType();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getBound() == null);
		assertTrue(x.isUpperBound() == true);
		assertTrue(x.getNodeType() == ASTNode.WILDCARD_TYPE);
		assertTrue(!x.isSimpleType());
		assertTrue(!x.isArrayType());
		assertTrue(!x.isPrimitiveType());
		assertTrue(!x.isParameterizedType());
		assertTrue(!x.isQualifiedType());
		assertTrue(x.isWildcardType());
		assertTrue(x.structuralPropertiesForType() == WildcardType.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		// make sure that isUpperBound works
		Type b = ast.newPrimitiveType(PrimitiveType.BYTE);
		x.setBound(b);
		x.setUpperBound(false);
		assertTrue(x.isUpperBound() == false);
		x.setUpperBound(true);
		assertTrue(x.isUpperBound() == true);
		x.setBound(null);
		x.setUpperBound(false);
		assertTrue(x.isUpperBound() == false);
		x.setUpperBound(true);
		assertTrue(x.isUpperBound() == true);
		
		// make sure that setBound(Type,boolean) works
		x.setBound(b, false);
		assertTrue(x.getBound() == b);
		assertTrue(x.isUpperBound() == false);
		x.setBound(null, true);
		assertTrue(x.getBound() == null);
		assertTrue(x.isUpperBound() == true);
		x.setBound(b, true);
		assertTrue(x.getBound() == b);
		assertTrue(x.isUpperBound() == true);
		x.setBound(null, false);
		assertTrue(x.getBound() == null);
		assertTrue(x.isUpperBound() == false);
		
		genericPropertyTest(x, new Property("Bound", false, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = 
				targetAst.newSimpleType(
						targetAst.newSimpleName("a")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				WildcardType s1 = ast.newWildcardType();
				s1.setBound(x);
				return s1;
			}
			public void unwrap() {
				WildcardType s1 = (WildcardType) x.getParent();
				s1.setBound(null);
			}
			public ASTNode get() {
				return x.getBound();
			}
			public void set(ASTNode value) {
				x.setBound((Type) value);
			}
		});
		
	}		

	public void testPackageDeclaration() {
		long previousCount = ast.modificationCount();
		final PackageDeclaration x = ast.newPackageDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.getJavadoc() == null);
			assertTrue(x.annotations().isEmpty());
		}
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.PACKAGE_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == PackageDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyTest(x, new Property("Javadoc", false, Javadoc.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Javadoc result = targetAst.newJavadoc();
					if (parented) {
						targetAst.newInitializer().setJavadoc(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getJavadoc();
				}
				public void set(ASTNode value) {
					x.setJavadoc((Javadoc) value);
				}
			});
			
			genericPropertyListTest(x, x.annotations(), new Property("Annotations", true, Annotation.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					MarkerAnnotation result = targetAst.newMarkerAnnotation();
					if (parented) {
						PackageDeclaration pd = targetAst.newPackageDeclaration();
						pd.annotations().add(result);
					}
					return result;
				}
			});
		}
		
		genericPropertyTest(x, new Property("Name", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});
	}		
	
	public void testImportDeclaration() {
		long previousCount = ast.modificationCount();
		final ImportDeclaration x = ast.newImportDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.isOnDemand() == false);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.isStatic() == false);
		}
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.IMPORT_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == ImportDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Name", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((Name) value);
			}
		});

		previousCount = ast.modificationCount();
		x.setOnDemand(false);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isOnDemand() == false);
		previousCount = ast.modificationCount();
		x.setOnDemand(true);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isOnDemand() == true);
		
		if (ast.apiLevel() >= AST.JLS3) {
			x.setStatic(true);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.isStatic() == true);
		}
	}
	
	public void testCompilationUnit() {
		long previousCount = ast.modificationCount();
		final CompilationUnit x = ast.newCompilationUnit();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getPackage() == null);
		assertTrue(x.imports().size() == 0);
		assertTrue(x.types().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.COMPILATION_UNIT);
		assertTrue(x.structuralPropertiesForType() == CompilationUnit.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tClientProperties(x);
		
		genericPropertyTest(x, new Property("Package", false, PackageDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				PackageDeclaration result = targetAst.newPackageDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.setPackage(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getPackage();
			}
			public void set(ASTNode value) {
				x.setPackage((PackageDeclaration) value);
			}
		});

		genericPropertyListTest(x, x.imports(), new Property("Imports", true, ImportDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				ImportDeclaration result = targetAst.newImportDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.imports().add(result);
				}
				return result;
			}
		});
		
		genericPropertyListTest(x, x.types(), new Property("Types", true, AbstractTypeDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.types().add(result);
				}
				return result;
			}
		});
		
		// check that TypeDeclarations in body are classified correctly
		TypeDeclaration t1 = ast.newTypeDeclaration();
		x.types().add(t1);
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == false);
		assertTrue(t1.isPackageMemberTypeDeclaration() == true);

	}		
	
	public void testCompilationUnitLineNumberTable() {
//		TO RUN THIS TEST YOU MUST TEMPORARILY MAKE PUBLIC
//		THE METHOD CompilationUnit.setLineEndTable
		
//		final CompilationUnit x = ast.newCompilationUnit();
//		
//		// table starts off empty
//		for (int i= -10; i < 10; i++) {
//			assertTrue(x.lineNumber(i) == 1);
//		}
//		
//		// supply a simple line table to test
//		String s = "AA\nBBB\nCC\nDDDD\nEEE";
//		assertTrue(s.length() == 18);  // cross check
//		int le[] = new int[5];
//		le[0] = s.indexOf('\n');
//		le[1] = s.indexOf('\n', le[0] + 1);
//		le[2] = s.indexOf('\n', le[1] + 1);
//		le[3] = s.indexOf('\n', le[2] + 1);
//		le[4] = s.length() - 1;
//		long previousCount = ast.modificationCount();
//		x.setLineEndTable(le);
//		assertTrue(ast.modificationCount() > previousCount);
//
//		assertTrue(x.lineNumber(0) == 1);
//		assertTrue(x.lineNumber(1) == 1);
//		assertTrue(x.lineNumber(2) == 1);
//		assertTrue(x.lineNumber(3) == 2);
//		assertTrue(x.lineNumber(4) == 2);
//		assertTrue(x.lineNumber(5) == 2);
//		assertTrue(x.lineNumber(6) == 2);
//		assertTrue(x.lineNumber(7) == 3);
//		assertTrue(x.lineNumber(8) == 3);
//		assertTrue(x.lineNumber(9) == 3);
//		assertTrue(x.lineNumber(10) == 4);
//		assertTrue(x.lineNumber(11) == 4);
//		assertTrue(x.lineNumber(12) == 4);
//		assertTrue(x.lineNumber(13) == 4);
//		assertTrue(x.lineNumber(14) == 4);
//		assertTrue(x.lineNumber(15) == 5);
//		assertTrue(x.lineNumber(16) == 5);
//		assertTrue(x.lineNumber(17) == 5);
//
//		assertTrue(x.lineNumber(18) == 1);
//		assertTrue(x.lineNumber(100) == 1);
//		assertTrue(x.lineNumber(1000000) == 1);
//		assertTrue(x.lineNumber(-1) == 1);
//		assertTrue(x.lineNumber(-100) == 1);
//		assertTrue(x.lineNumber(-1000000) == 1);
//				
//		// slam table back to none
//		x.setLineEndTable(new int[0]);
//		for (int i= -10; i < 10; i++) {
//			assertTrue(x.lineNumber(i) == 1);
//		}
	}		
	
	/** @deprecated using deprecated code */
	public void testTypeDeclaration() {
		long previousCount = ast.modificationCount();
		final TypeDeclaration x = ast.newTypeDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
			assertTrue(x.getSuperclass() == null);
			assertTrue(x.superInterfaces().size() == 0);
		} else {
			assertTrue(x.modifiers().size() == 0);
			assertTrue(x.typeParameters().size() == 0);
			assertTrue(x.getSuperclassType() == null);
			assertTrue(x.superInterfaceTypes().size() == 0);
		}
		assertTrue(x.isInterface() == false);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.bodyDeclarations().size()== 0);
		assertTrue(x.getNodeType() == ASTNode.TYPE_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == TypeDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		previousCount = ast.modificationCount();
		x.setInterface(true);	
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isInterface() == true);
		
		if (ast.apiLevel() == AST.JLS2) {
			int legal = Modifier.PUBLIC | Modifier.PROTECTED
				| Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC
				| Modifier.FINAL | Modifier.STRICTFP;
			previousCount = ast.modificationCount();
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
	
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}
		
		tJavadocComment(x);
		tModifiers(x);		
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeParameters(),
			  new Property("TypeParameters", true, TypeParameter.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					TypeParameter result = targetAst.newTypeParameter();
					if (parented) {
						targetAst.newMethodDeclaration().typeParameters().add(result);
					}
					return result;
				}
			});
		}
		
		if (ast.apiLevel() == AST.JLS2) {
			genericPropertyTest(x, new Property("Superclass", false, Name.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
					if (parented) {
						targetAst.newExpressionStatement(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getSuperclass();
				}
				public void set(ASTNode value) {
					x.setSuperclass((Name) value);
				}
			});
		}
		
		if (ast.apiLevel() == AST.JLS2) {
			genericPropertyListTest(x, x.superInterfaces(),
			  new Property("SuperInterfaces", true, Name.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
					if (parented) {
						targetAst.newExpressionStatement(result);
					}
					return result;
				}
			});
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyTest(x, new Property("SuperclassType", false, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleType result = targetAst.newSimpleType(targetAst.newSimpleName("foo")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getSuperclassType();
				}
				public void set(ASTNode value) {
					x.setSuperclassType((Type) value);
				}
			});
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.superInterfaceTypes(),
			  new Property("SuperInterfaceTypes", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleType result = targetAst.newSimpleType(targetAst.newSimpleName("foo")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
			});
		}
		

		genericPropertyListTest(x, x.bodyDeclarations(),
		  new Property("BodyDeclarations", true, BodyDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.types().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				TypeDeclaration s1 = x.getAST().newTypeDeclaration();
				s1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s1 = (TypeDeclaration) x.getParent();
				s1.bodyDeclarations().remove(x);
			}
		});
		
		// check special bodyDeclaration methods
		x.bodyDeclarations().clear();
		FieldDeclaration f1 = ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
		FieldDeclaration f2 = ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
		MethodDeclaration m1 = ast.newMethodDeclaration();
		MethodDeclaration m2 = ast.newMethodDeclaration();
		TypeDeclaration t1 = ast.newTypeDeclaration();
		TypeDeclaration t2 = ast.newTypeDeclaration();
		
		EnumConstantDeclaration c1 = null;
		EnumConstantDeclaration c2 = null;
		if (ast.apiLevel() >= AST.JLS3) {
			c1 = ast.newEnumConstantDeclaration();
			c2 = ast.newEnumConstantDeclaration();
			x.bodyDeclarations().add(c1);
			x.bodyDeclarations().add(c2);
		}

		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(f1);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(f2);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(t1);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(m1);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(m2);
		x.bodyDeclarations().add(ast.newInitializer());
		x.bodyDeclarations().add(t2);
		x.bodyDeclarations().add(ast.newInitializer());

		List fs = Arrays.asList(x.getFields());
		assertTrue(fs.size() == 2);
		assertTrue(fs.contains(f1));
		assertTrue(fs.contains(f2));
		
		List ms = Arrays.asList(x.getMethods());
		assertTrue(ms.size() == 2);
		assertTrue(ms.contains(m1));
		assertTrue(ms.contains(m2));

		List ts = Arrays.asList(x.getTypes());
		assertTrue(ts.size() == 2);
		assertTrue(ts.contains(t1));
		assertTrue(ts.contains(t2));
		
		// check that TypeDeclarations in body are classified correctly
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == true);
		assertTrue(t1.isPackageMemberTypeDeclaration() == false);
	
	}	
	
	/** @deprecated using deprecated code */
	public void testEnumDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newEnumDeclaration();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final EnumDeclaration x = ast.newEnumDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.modifiers().size() == 0);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.superInterfaceTypes().size() == 0);
		assertTrue(x.enumConstants().size()== 0);
		assertTrue(x.bodyDeclarations().size()== 0);
		assertTrue(x.getNodeType() == ASTNode.ENUM_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == EnumDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		previousCount = ast.modificationCount();
		
		tJavadocComment(x);
		tModifiers(x);		
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyListTest(x, x.superInterfaceTypes(),
		  new Property("SuperInterfaceTypes", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
		});

		genericPropertyListTest(x, x.enumConstants(),
				  new Property("EnumConstants", true, EnumConstantDeclaration.class) { //$NON-NLS-1$
					public ASTNode sample(AST targetAst, boolean parented) {
						EnumConstantDeclaration result = targetAst.newEnumConstantDeclaration();
						if (parented) {
							// use fact that EnumConstantDeclaration is also a BodyDeclaration
							TypeDeclaration d = targetAst.newTypeDeclaration();
							d.bodyDeclarations().add(result);
						}
						return result;
					}
					public ASTNode wrap() {
						EnumConstantDeclaration s1 = x.getAST().newEnumConstantDeclaration();
						AnonymousClassDeclaration anonymousClassDeclaration = x.getAST().newAnonymousClassDeclaration();
						s1.setAnonymousClassDeclaration(anonymousClassDeclaration);
						anonymousClassDeclaration.bodyDeclarations().add(x);
						return s1;
					}
					public void unwrap() {
						AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) x.getParent();
						if (anonymousClassDeclaration != null) {
							anonymousClassDeclaration.bodyDeclarations().remove(x);
						}
					}
				});
				
		genericPropertyListTest(x, x.bodyDeclarations(),
		  new Property("BodyDeclarations", true, BodyDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit cu = targetAst.newCompilationUnit();
					cu.types().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				TypeDeclaration s1 = x.getAST().newTypeDeclaration();
				s1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s1 = (TypeDeclaration) x.getParent();
				s1.bodyDeclarations().remove(x);
			}
		});
		
		// check special bodyDeclaration methods
		x.bodyDeclarations().clear();
		EnumConstantDeclaration c1 = ast.newEnumConstantDeclaration();
		EnumConstantDeclaration c2 = ast.newEnumConstantDeclaration();
		FieldDeclaration f1 = ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
		FieldDeclaration f2 = ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
		MethodDeclaration m1 = ast.newMethodDeclaration();
		MethodDeclaration m2 = ast.newMethodDeclaration();
		TypeDeclaration t1 = ast.newTypeDeclaration();
		TypeDeclaration t2 = ast.newTypeDeclaration();

		x.enumConstants().add(c1);
		x.enumConstants().add(c2);
		x.bodyDeclarations().add(f1);
		x.bodyDeclarations().add(f2);
		x.bodyDeclarations().add(m1);
		x.bodyDeclarations().add(m2);
		x.bodyDeclarations().add(t1);
		x.bodyDeclarations().add(t2);

		// check that TypeDeclarations in body are classified correctly
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == true);
		assertTrue(t1.isPackageMemberTypeDeclaration() == false);
	
	}	
	
	/** @deprecated using deprecated code */
	public void testEnumConstantDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newEnumConstantDeclaration();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final EnumConstantDeclaration x = ast.newEnumConstantDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.arguments().size()== 0);
		assertTrue(x.getAnonymousClassDeclaration() == null);
		assertTrue(x.modifiers().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.ENUM_CONSTANT_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == EnumConstantDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
			
		tJavadocComment(x);
		tModifiers(x);		
				
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
				
		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				AnonymousClassDeclaration s1 = x.getAST().newAnonymousClassDeclaration();
				s1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration s1 = (AnonymousClassDeclaration) x.getParent();
				s1.bodyDeclarations().remove(x);
			}
		});

		genericPropertyTest(x, new Property("AnonymousClassDeclaration", false, AnonymousClassDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				AnonymousClassDeclaration result = targetAst.newAnonymousClassDeclaration();
				if (parented) {
					targetAst.newClassInstanceCreation().setAnonymousClassDeclaration(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return AnonymousClassDeclaration that embeds x
				AnonymousClassDeclaration s0 = x.getAST().newAnonymousClassDeclaration();
				EnumDeclaration s1 = x.getAST().newEnumDeclaration();
				s0.bodyDeclarations().add(s1);
				s1.bodyDeclarations().add(x);
				return s0;
			}
			public void unwrap() {
				EnumDeclaration s1 = (EnumDeclaration) x.getParent();
				s1.bodyDeclarations().remove(x);
			}
			public ASTNode get() {
				return x.getAnonymousClassDeclaration();
			}
			public void set(ASTNode value) {
				x.setAnonymousClassDeclaration((AnonymousClassDeclaration) value);
			}
		});

		// check that TypeDeclarations in body are classified correctly
		x.setAnonymousClassDeclaration(null);
		AnonymousClassDeclaration w0 = ast.newAnonymousClassDeclaration();
		x.setAnonymousClassDeclaration(w0);
		TypeDeclaration w1 = ast.newTypeDeclaration();
		w0.bodyDeclarations().add(w1);

		assertTrue(w1.isLocalTypeDeclaration() == false);
		assertTrue(w1.isMemberTypeDeclaration() == true);
		assertTrue(w1.isPackageMemberTypeDeclaration() == false);
	
	}	
	
	/** @deprecated using deprecated code */
	public void testTypeParameter() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newTypeParameter();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final TypeParameter x = ast.newTypeParameter();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.TYPE_PARAMETER);
		assertTrue(x.typeBounds().size() == 0);
		assertTrue(x.structuralPropertiesForType() == TypeParameter.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		genericPropertyListTest(x, x.typeBounds(),
		  new Property("TypeBounds", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Type result = targetAst.newSimpleType(targetAst.newSimpleName("foo"));
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
		});
	}		
	
	/** @deprecated using deprecated code */
	public void testSingleVariableDeclaration() {
		long previousCount = ast.modificationCount();
		final SingleVariableDeclaration x = ast.newSingleVariableDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
		} else {
			assertTrue(x.modifiers().size() == 0);
			assertTrue(x.isVarargs() == false);
		}
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getExtraDimensions() == 0);
		assertTrue(x.getInitializer() == null);
		assertTrue(x.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == SingleVariableDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		if (ast.apiLevel() == AST.JLS2) {
			int legal = Modifier.PUBLIC | Modifier.PROTECTED
				| Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL
				| Modifier.TRANSIENT | Modifier.VOLATILE;
			previousCount = ast.modificationCount();
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
	
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}

		previousCount = ast.modificationCount();
		x.setExtraDimensions(1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 1);

		previousCount = ast.modificationCount();
		x.setExtraDimensions(0);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 0);

		if (ast.apiLevel() >= AST.JLS3) {
			previousCount = ast.modificationCount();
			x.setVarargs(true);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.isVarargs() == true);
	
			previousCount = ast.modificationCount();
			x.setVarargs(false);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.isVarargs() == false);
		}

		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.modifiers(), new Property("Modifiers", true, IExtendedModifier.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Modifier result = targetAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
					if (parented) {
						TypeDeclaration pd = targetAst.newTypeDeclaration();
						pd.modifiers().add(result);
					}
					return result;
				}
				public ASTNode wrap() {
					SingleMemberAnnotation s1 = x.getAST().newSingleMemberAnnotation();
					ClassInstanceCreation s2 = x.getAST().newClassInstanceCreation();
					AnonymousClassDeclaration s3 = x.getAST().newAnonymousClassDeclaration();
					MethodDeclaration s4 = x.getAST().newMethodDeclaration();
					SingleVariableDeclaration s5 = x.getAST().newSingleVariableDeclaration();
					s1.setValue(s2);
					s2.setAnonymousClassDeclaration(s3);
					s3.bodyDeclarations().add(s4);
					s4.parameters().add(s5);
					s5.modifiers().add(x);
					return s1;
				}
				public void unwrap() {
					SingleVariableDeclaration s5 = (SingleVariableDeclaration) x.getParent();
					s5.modifiers().remove(x);
				}
			});
			
			// check that getModifiers() tracks changes to modifiers()
			x.modifiers().clear();
			assertTrue(x.getModifiers() == Modifier.NONE);
			Modifier[] allMods = allModifiers();
			// one at a time
			for (int i = 0 ; i < allMods.length; i++) {
				x.modifiers().add(allMods[i]);
				assertTrue(x.getModifiers() == allMods[i].getKeyword().toFlagValue());
				x.modifiers().remove(allMods[i]);
				assertTrue(x.getModifiers() == Modifier.NONE);
			}
			// all at same time
			for (int i = 0 ; i < allMods.length; i++) {
				x.modifiers().add(allMods[i]);
			}
			int flags  = x.getModifiers();
			for (int i = 0 ; i < allMods.length; i++) {
				assertTrue((flags & allMods[i].getKeyword().toFlagValue()) != 0);
			}
		}

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
		
		genericPropertyTest(x, new Property("Initializer", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return an Expression that embeds x
				CatchClause s1 = ast.newCatchClause();
				s1.setException(x);
				return s1;
			}
			public void unwrap() {
				CatchClause s1 = (CatchClause) x.getParent();
				s1.setException(ast.newSingleVariableDeclaration());
			}
			public ASTNode get() {
				return x.getInitializer();
			}
			public void set(ASTNode value) {
				x.setInitializer((Expression) value);
			}
		});
	}
	
	public void testVariableDeclarationFragment() {
		long previousCount = ast.modificationCount();
		final VariableDeclarationFragment x = ast.newVariableDeclarationFragment();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getExtraDimensions() == 0);
		assertTrue(x.getInitializer() == null);
		assertTrue(x.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		assertTrue(x.structuralPropertiesForType() == 
			VariableDeclarationFragment.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		previousCount = ast.modificationCount();
		x.setExtraDimensions(1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 1);

		previousCount = ast.modificationCount();
		x.setExtraDimensions(0);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 0);
		
		// check that property cannot be set negative
		try {
			x.setExtraDimensions(-1);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Initializer", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return an Expression that embeds x
				VariableDeclarationExpression s1 =
					ast.newVariableDeclarationExpression(x);
				return s1;
			}
			public void unwrap() {
				VariableDeclarationExpression s1 = 
					(VariableDeclarationExpression) x.getParent();
				s1.fragments().remove(x);
			}
			public ASTNode get() {
				return x.getInitializer();
			}
			public void set(ASTNode value) {
				x.setInitializer((Expression) value);
			}
		});
	}
	
	/** @deprecated using deprecated code */
	public void testMethodDeclaration() {
		long previousCount = ast.modificationCount();
		final MethodDeclaration x = ast.newMethodDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
			assertTrue(x.getReturnType().getParent() == x);
			assertTrue(x.getReturnType().isPrimitiveType());
			assertTrue(((PrimitiveType) x.getReturnType()).getPrimitiveTypeCode() == PrimitiveType.VOID);
			try {
				x.typeParameters();
				assertTrue("Should have failed", false);
			} catch(UnsupportedOperationException e) {
				// pass
			}
			try {
				x.isVarargs();
				assertTrue("Should have failed", false);
			} catch(UnsupportedOperationException e) {
				// pass
			}
		} else {
			assertTrue(x.modifiers().size() == 0);
			assertTrue(x.typeParameters().size() == 0);
			assertTrue(x.getReturnType2().getParent() == x);
			assertTrue(x.getReturnType2().isPrimitiveType());
			assertTrue(((PrimitiveType) x.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID);
		}
		assertTrue(x.isConstructor() == false);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getExtraDimensions() == 0);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.parameters().size() == 0);
		assertTrue(x.thrownExceptions().size() == 0);
		assertTrue(x.getBody() == null);
		assertTrue(x.getNodeType() == ASTNode.METHOD_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == 
			MethodDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		previousCount = ast.modificationCount();
		x.setConstructor(true);	
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isConstructor() == true);
		assertTrue(x.getName().isDeclaration() == false);

		previousCount = ast.modificationCount();
		x.setConstructor(false);	
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.isConstructor() == false);
		
		if (ast.apiLevel() == AST.JLS2) {
			previousCount = ast.modificationCount();
			int legal = Modifier.PUBLIC | Modifier.PROTECTED
				| Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC 
				| Modifier.FINAL | Modifier.SYNCHRONIZED| Modifier.NATIVE
				| Modifier.STRICTFP;
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
	
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}

		previousCount = ast.modificationCount();
		x.setExtraDimensions(1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 1);

		previousCount = ast.modificationCount();
		x.setExtraDimensions(0);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getExtraDimensions() == 0);

		tJavadocComment(x);
		tModifiers(x);
						
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeParameters(),
			  new Property("TypeParameters", true, TypeParameter.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					TypeParameter result = targetAst.newTypeParameter();
					if (parented) {
						targetAst.newMethodDeclaration().typeParameters().add(result);
					}
					return result;
				}
			});
		}
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		if (ast.apiLevel() == AST.JLS2) {
			genericPropertyTest(x, new Property("ReturnType", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleType result = targetAst.newSimpleType(
						targetAst.newSimpleName("foo")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getReturnType();
				}
				public void set(ASTNode value) {
					x.setReturnType((Type) value);
				}
			});
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyTest(x, new Property("ReturnType2", false, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleType result = targetAst.newSimpleType(
						targetAst.newSimpleName("foo")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getReturnType2();
				}
				public void set(ASTNode value) {
					x.setReturnType2((Type) value);
				}
			});
		}
		
		genericPropertyListTest(x, x.parameters(),
		  new Property("Parameters", true, SingleVariableDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SingleVariableDeclaration result = targetAst.newSingleVariableDeclaration();
				if (parented) {
					targetAst.newCatchClause().setException(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a SingleVariableDeclaration that embeds x
				SingleVariableDeclaration s1 = ast.newSingleVariableDeclaration();
				ClassInstanceCreation s2 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s2.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s2);
				a1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration a1 = (AnonymousClassDeclaration) x.getParent();
				a1.bodyDeclarations().remove(x);
			}
		});
		
		genericPropertyListTest(x, x.thrownExceptions(),
		  new Property("ThrownExceptions", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
		});
		
		genericPropertyTest(x, new Property("Body", false, Block.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				TypeDeclaration s2 = ast.newTypeDeclaration();
				s1.statements().add(ast.newTypeDeclarationStatement(s2));
				s2.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s2 = (TypeDeclaration) x.getParent();
				s2.bodyDeclarations().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
		
		if (ast.apiLevel() >= AST.JLS3) {
			// check isVariableArity convenience method
			x.parameters().clear();
			assertTrue(!x.isVarargs()); // 0 params
			x.parameters().add(ast.newSingleVariableDeclaration());
			assertTrue(!x.isVarargs()); // 1 params
			SingleVariableDeclaration v = ast.newSingleVariableDeclaration();
			x.parameters().add(v);
			assertTrue(!x.isVarargs()); // 2 param fixed arity
			v.setVarargs(true);
			assertTrue(x.isVarargs()); // 2 param fixed arity
			x.parameters().add(ast.newSingleVariableDeclaration());
			assertTrue(!x.isVarargs()); // only last param counts
		}
		try {
			x.setExtraDimensions(-1);
			assertTrue("Should fail", false);
		} catch(IllegalArgumentException e) {
			// pass
		}
	}	
	
	/** @deprecated using deprecated code */
	public void testInitializer() {
		long previousCount = ast.modificationCount();
		final Initializer x = ast.newInitializer();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getJavadoc() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
		} else {
			assertTrue(x.modifiers().size() == 0);
		}
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody().statements().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.INITIALIZER);
		assertTrue(x.structuralPropertiesForType() == 
			Initializer.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		tJavadocComment(x);
		tModifiers(x);
				
		if (ast.apiLevel() == AST.JLS2) {
			int legal = Modifier.STATIC;
			previousCount = ast.modificationCount();
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
			
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}
		
		genericPropertyTest(x, new Property("Body", true, Block.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Block that embeds x
				Block s1 = ast.newBlock();
				TypeDeclaration s2 = ast.newTypeDeclaration();
				s1.statements().add(ast.newTypeDeclarationStatement(s2));
				s2.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				TypeDeclaration s2 = (TypeDeclaration) x.getParent();
				s2.bodyDeclarations().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}	

	/**
	 * @deprecated (not really - its just that Javadoc.get/setComment
	 * are deprecated, and this suppresses the extra warnings)
	 */
	public void testJavadoc() {
		long previousCount = ast.modificationCount();
		final Javadoc x = ast.newJavadoc();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getComment().startsWith("/**")); //$NON-NLS-1$
			assertTrue(x.getComment().endsWith("*/")); //$NON-NLS-1$
		}
		assertTrue(x.getNodeType() == ASTNode.JAVADOC);
		assertTrue(!x.isBlockComment());
		assertTrue(!x.isLineComment());
		assertTrue(x.isDocComment());
		assertTrue(x.tags().isEmpty());
		assertTrue(x.getAlternateRoot() == null);
		assertTrue(x.structuralPropertiesForType() == 
			Javadoc.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// check the constants
		assertTrue(TagElement.TAG_AUTHOR.equals("@author")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_DEPRECATED.equals("@deprecated")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_DOCROOT.equals("@docRoot")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_EXCEPTION.equals("@exception")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_INHERITDOC.equals("@inheritDoc")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_LINK.equals("@link")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_LINKPLAIN.equals("@linkplain")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_PARAM.equals("@param")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_RETURN.equals("@return")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_SEE.equals("@see")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_SERIAL.equals("@serial")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_SERIALDATA.equals("@serialData")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_SERIALFIELD.equals("@serialField")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_SINCE.equals("@since")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_THROWS.equals("@throws")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_VALUE.equals("@value")); //$NON-NLS-1$
		assertTrue(TagElement.TAG_VERSION.equals("@version")); //$NON-NLS-1$

		if (ast.apiLevel() == AST.JLS2) {
			final String[] samples =
				{ 
				  "/** Hello there */", //$NON-NLS-1$
				  "/**\n * Line 1\n * Line 2\n */", //$NON-NLS-1$
				  "/***/", //$NON-NLS-1$
				};
			for (int i = 0; i < samples.length; i++) {			
				previousCount = ast.modificationCount();
				x.setComment(samples[i]);
				assertTrue(ast.modificationCount() > previousCount);
				assertTrue(samples[i].equals(x.getComment()));
			}
	
			final String[] badSamples =
				{ 
				  null,
				  "", //$NON-NLS-1$
				  "/* */", //$NON-NLS-1$
				  "/**", //$NON-NLS-1$
				  "*/", //$NON-NLS-1$
				};
	
			// check that property cannot be set to clearly illegal things
			for (int i = 0; i < badSamples.length; i++) {			
				try {
					x.setComment(badSamples[i]);
					assertTrue(false);
				} catch (RuntimeException e) {
					// pass
				}
			}
		}
		
		tAlternateRoot(x);

		genericPropertyListTest(x, x.tags(),
		  new Property("Tags", true, TagElement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TagElement result = targetAst.newTagElement();
				if (parented) {
					Javadoc parent = targetAst.newJavadoc();
					parent.tags().add(result);
				}
				return result;
			}
			public ASTNode[] counterExamples(AST targetAst) {
				return new ASTNode[] {
					targetAst.newEmptyStatement(),
					targetAst.newCompilationUnit(),
					targetAst.newTypeDeclaration(),
					targetAst.newJavadoc(),
					targetAst.newTextElement(),
					targetAst.newMethodRef()
				};
			}
		});
	}		

	public void testBlockComment() {
		long previousCount = ast.modificationCount();
		final BlockComment x = ast.newBlockComment();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.BLOCK_COMMENT);
		assertTrue(x.isBlockComment());
		assertTrue(!x.isLineComment());
		assertTrue(!x.isDocComment());
		assertTrue(x.getAlternateRoot() == null);
		assertTrue(x.structuralPropertiesForType() == 
			BlockComment.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tAlternateRoot(x);
	}		

	public void testLineComment() {
		long previousCount = ast.modificationCount();
		final LineComment x = ast.newLineComment();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.LINE_COMMENT);
		assertTrue(!x.isBlockComment());
		assertTrue(x.isLineComment());
		assertTrue(!x.isDocComment());
		assertTrue(x.getAlternateRoot() == null);
		assertTrue(x.structuralPropertiesForType() == 
			LineComment.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tAlternateRoot(x);
	}		

	public void testTagElement() {
		long previousCount = ast.modificationCount();
		final TagElement x = ast.newTagElement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.TAG_ELEMENT);
		assertTrue(x.getTagName() == null);
		assertTrue(x.fragments().isEmpty());
		assertTrue(x.structuralPropertiesForType() == 
			TagElement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// tagName property
		previousCount = ast.modificationCount();
		String s1 = new String("hello"); //$NON-NLS-1$
		x.setTagName(s1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getTagName() == s1);
		previousCount = ast.modificationCount();
		String s2 = new String("bye"); //$NON-NLS-1$
		x.setTagName(s2);
		assertTrue(x.getTagName() == s2);
		assertTrue(ast.modificationCount() > previousCount);
		x.setTagName(null);
		assertTrue(x.getTagName() == null);
		assertTrue(ast.modificationCount() > previousCount);
		
		// check that fragments() can handle TagElement
		genericPropertyListTest(x, x.fragments(),
		  new Property("Fragments", true, TagElement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TagElement result = targetAst.newTagElement();
				if (parented) {
					Javadoc parent = targetAst.newJavadoc();
					parent.tags().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return TagElement that embeds x
				TagElement tagElement = ast.newTagElement();
				tagElement.fragments().add(x);
				return tagElement;
			}
			public void unwrap() {
				TagElement tagElement = (TagElement) x.getParent();
				tagElement.fragments().remove(x);
			}
			public ASTNode[] counterExamples(AST targetAst) {
				return new ASTNode[] {
					targetAst.newEmptyStatement(),
					targetAst.newCompilationUnit(),
					targetAst.newTypeDeclaration(),
					targetAst.newJavadoc(),
				};
			}
		});
		// check that fragments() can handle Name
		genericPropertyListTest(x, x.fragments(),
		  new Property("Fragments", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
		});
		// check that fragments() can handle TextElement
		genericPropertyListTest(x, x.fragments(),
		  new Property("Fragments", true, TextElement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TextElement result = targetAst.newTextElement();
				if (parented) {
					TagElement parent = targetAst.newTagElement();
					parent.fragments().add(result);
				}
				return result;
			}
		});
		// check that fragments() can handle MethodRef
		genericPropertyListTest(x, x.fragments(),
		  new Property("Fragments", true, MethodRef.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				MethodRef result = targetAst.newMethodRef();
				if (parented) {
					TagElement parent = targetAst.newTagElement();
					parent.fragments().add(result);
				}
				return result;
			}
		});
		// check that fragments() can handle MemberRef
		genericPropertyListTest(x, x.fragments(),
		  new Property("Fragments", true, MemberRef.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				MemberRef result = targetAst.newMemberRef();
				if (parented) {
					TagElement parent = targetAst.newTagElement();
					parent.fragments().add(result);
				}
				return result;
			}
		});
	}		

	public void testTextElement() {
		long previousCount = ast.modificationCount();
		final TextElement x = ast.newTextElement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.TEXT_ELEMENT);
		assertTrue(x.getText().length() == 0);
		assertTrue(x.structuralPropertiesForType() == 
			TextElement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// text property
		previousCount = ast.modificationCount();
		String s1 = new String("hello");
		x.setText(s1);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getText() == s1);
		previousCount = ast.modificationCount();
		String s2 = new String("");
		x.setText(s2);
		assertTrue(x.getText() == s2);
		assertTrue(ast.modificationCount() > previousCount);
		// check that property cannot be set to null
		previousCount = ast.modificationCount();
		try {
			x.setText(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		assertTrue(ast.modificationCount() == previousCount);
		// check that property cannot include */
		previousCount = ast.modificationCount();
		try {
			x.setText("this would be the */ end of it");  //$NON-NLS-1$
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		assertTrue(ast.modificationCount() == previousCount);
	}		

	public void testMemberRef() {
		long previousCount = ast.modificationCount();
		final MemberRef x = ast.newMemberRef();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.MEMBER_REF);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.structuralPropertiesForType() == 
			MemberRef.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Qualifier", false, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"), //$NON-NLS-1$
					targetAst.newSimpleName("b")); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}		
	
	public void testMethodRef() {
		long previousCount = ast.modificationCount();
		final MethodRef x = ast.newMethodRef();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.METHOD_REF);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.parameters().isEmpty());
		assertTrue(x.structuralPropertiesForType() == 
			MethodRef.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Qualifier", false, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"), //$NON-NLS-1$
					targetAst.newSimpleName("b")); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});

		genericPropertyListTest(x, x.parameters(),
		  new Property("Parameters", true, MethodRefParameter.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				MethodRefParameter result = targetAst.newMethodRefParameter();
				if (parented) {
					MethodRef parent = targetAst.newMethodRef();
					parent.parameters().add(result);
				}
				return result;
			}
		});
	}		
	
	public void testMethodRefParameter() {
		long previousCount = ast.modificationCount();
		final MethodRefParameter x = ast.newMethodRefParameter();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getNodeType() == ASTNode.METHOD_REF_PARAMETER);
		assertTrue(x.getType().getParent() == x);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.isVarargs() == false);
		}
		assertTrue(x.getName() == null);
		assertTrue(x.structuralPropertiesForType() == 
			MethodRefParameter.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		if (ast.apiLevel() >= AST.JLS3) {
			previousCount = ast.modificationCount();
			x.setVarargs(true);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.isVarargs() == true);
	
			previousCount = ast.modificationCount();
			x.setVarargs(false);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.isVarargs() == false);
		}

		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyTest(x, new Property("Name", false, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}		
	
	public void testBlock() {
		long previousCount = ast.modificationCount();
		final Block x = ast.newBlock();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.statements().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.BLOCK);
		assertTrue(x.structuralPropertiesForType() == 
			Block.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		tLeadingComment(x);

		genericPropertyListTest(x, x.statements(),
		  new Property("Statements", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
		});
	}	
	
	public void testMethodInvocation() {
		long previousCount = ast.modificationCount();
		final MethodInvocation x = ast.newMethodInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.typeArguments().isEmpty());
		}
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getExpression() == null);
		assertTrue(x.arguments().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.METHOD_INVOCATION);
		assertTrue(x.structuralPropertiesForType() == 
			MethodInvocation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		genericPropertyTest(x, new Property("Expression", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeArguments(),
			  new Property("TypeArguments", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Type result = targetAst.newSimpleType(targetAst.newSimpleName("X")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
			});
		}
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});

		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
		});
	}	
	
	public void testExpressionStatement() {
		long previousCount = ast.modificationCount();
		SimpleName x1 = ast.newSimpleName("foo"); //$NON-NLS-1$
		final ExpressionStatement x = ast.newExpressionStatement(x1);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.EXPRESSION_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			ExpressionStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

	}	
	
	/** @deprecated using deprecated code */
	public void testVariableDeclarationStatement() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		long previousCount = ast.modificationCount();
		final VariableDeclarationStatement x = 
			ast.newVariableDeclarationStatement(x1);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
		} else {
			assertTrue(x.modifiers().size() == 0);
		}
		assertTrue(x.getType() != null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.fragments().size() == 1);
		assertTrue(x.fragments().get(0) == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			VariableDeclarationStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		if (ast.apiLevel() == AST.JLS2) {
			int legal = Modifier.FINAL;
			previousCount = ast.modificationCount();
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
	
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.modifiers(), new Property("Modifiers", true, IExtendedModifier.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Modifier result = targetAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
					if (parented) {
						TypeDeclaration pd = targetAst.newTypeDeclaration();
						pd.modifiers().add(result);
					}
					return result;
				}
				public ASTNode wrap() {
					SingleMemberAnnotation s1 = x.getAST().newSingleMemberAnnotation();
					ClassInstanceCreation s2 = x.getAST().newClassInstanceCreation();
					AnonymousClassDeclaration s3 = x.getAST().newAnonymousClassDeclaration();
					MethodDeclaration s4 = x.getAST().newMethodDeclaration();
					Block s5 = x.getAST().newBlock();
					VariableDeclarationFragment s6 = x.getAST().newVariableDeclarationFragment();
					VariableDeclarationStatement s7 = x.getAST().newVariableDeclarationStatement(s6);
					s1.setValue(s2);
					s2.setAnonymousClassDeclaration(s3);
					s3.bodyDeclarations().add(s4);
					s4.setBody(s5);
					s5.statements().add(s7);
					s7.modifiers().add(x);
					return s1;
				}
				public void unwrap() {
					VariableDeclarationStatement s7 = (VariableDeclarationStatement) x.getParent();
					s7.modifiers().remove(x);
				}
			});
			
			// check that getModifiers() tracks changes to modifiers()
			x.modifiers().clear();
			assertTrue(x.getModifiers() == Modifier.NONE);
			Modifier[] allMods = allModifiers();
			// one at a time
			for (int i = 0 ; i < allMods.length; i++) {
				x.modifiers().add(allMods[i]);
				assertTrue(x.getModifiers() == allMods[i].getKeyword().toFlagValue());
				x.modifiers().remove(allMods[i]);
				assertTrue(x.getModifiers() == Modifier.NONE);
			}
			// all at same time
			for (int i = 0 ; i < allMods.length; i++) {
				x.modifiers().add(allMods[i]);
			}
			int flags  = x.getModifiers();
			for (int i = 0 ; i < allMods.length; i++) {
				assertTrue((flags & allMods[i].getKeyword().toFlagValue()) != 0);
			}
		}

		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyListTest(x, x.fragments(),
		  new Property("VariableSpecifiers", true, VariableDeclarationFragment.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				VariableDeclarationFragment result = targetAst.newVariableDeclarationFragment();
				if (parented) {
					targetAst.newVariableDeclarationExpression(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return VariableDeclarationFragment that embeds x
				VariableDeclarationFragment s1 = ast.newVariableDeclarationFragment();
				ClassInstanceCreation s0 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s0.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s0);
				Initializer s2 = ast.newInitializer();
				a1.bodyDeclarations().add(s2);
				s2.getBody().statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});
	}	
	
	/** @deprecated using deprecated code */
	public void testTypeDeclarationStatement() {
		AbstractTypeDeclaration x1 = ast.newTypeDeclaration();
		long previousCount = ast.modificationCount();
		final TypeDeclarationStatement x =
			ast.newTypeDeclarationStatement(x1);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getTypeDeclaration() == x1);
		} else {
			assertTrue(x.getDeclaration() == x1);
		}
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.TYPE_DECLARATION_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			TypeDeclarationStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		// check that TypeDeclaration inside is classified correctly
		assertTrue(x1.isLocalTypeDeclaration() == true);
		assertTrue(x1.isMemberTypeDeclaration() == false);
		assertTrue(x1.isPackageMemberTypeDeclaration() == false);
	
		tLeadingComment(x);

		if (ast.apiLevel() == AST.JLS2) {
			genericPropertyTest(x, new Property("TypeDeclaration", true, TypeDeclaration.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					TypeDeclaration result = targetAst.newTypeDeclaration();
					if (parented) {
						targetAst.newTypeDeclarationStatement(result);
					}
					return result;
				}
				public ASTNode wrap() {
					// return TypeDeclaration that embeds x
					TypeDeclaration s1 = ast.newTypeDeclaration();
					MethodDeclaration s2 = ast.newMethodDeclaration();
					s1.bodyDeclarations().add(s2);
					Block s3 = ast.newBlock();
					s2.setBody(s3);
					s3.statements().add(x);
					return s1;
				}
				public void unwrap() {
					Block s3 = (Block) x.getParent();
					s3.statements().remove(x);
				}
				public ASTNode get() {
					return x.getTypeDeclaration();
				}
				public void set(ASTNode value) {
					x.setTypeDeclaration((TypeDeclaration) value);
				}
			});
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyTest(x, new Property("Declaration", true, AbstractTypeDeclaration.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					AbstractTypeDeclaration result = targetAst.newTypeDeclaration();
					if (parented) {
						targetAst.newTypeDeclarationStatement(result);
					}
					return result;
				}
				public ASTNode wrap() {
					// return TypeDeclaration that embeds x
					TypeDeclaration s1 = ast.newTypeDeclaration();
					MethodDeclaration s2 = ast.newMethodDeclaration();
					s1.bodyDeclarations().add(s2);
					Block s3 = ast.newBlock();
					s2.setBody(s3);
					s3.statements().add(x);
					return s1;
				}
				public void unwrap() {
					Block s3 = (Block) x.getParent();
					s3.statements().remove(x);
				}
				public ASTNode get() {
					return x.getDeclaration();
				}
				public void set(ASTNode value) {
					x.setDeclaration((AbstractTypeDeclaration) value);
				}
			});
		}
	}
		
	/** @deprecated using deprecated code */
	public void testVariableDeclarationExpression() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		long previousCount = ast.modificationCount();
		final VariableDeclarationExpression x = 
			ast.newVariableDeclarationExpression(x1);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
		} else {
			assertTrue(x.modifiers().size() == 0);
		}
		assertTrue(x.getType() != null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.fragments().size() == 1);
		assertTrue(x.fragments().get(0) == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			VariableDeclarationExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		if (ast.apiLevel() == AST.JLS2) {
			int legal = Modifier.FINAL;
			previousCount = ast.modificationCount();
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
	
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.modifiers(), new Property("Modifiers", true, IExtendedModifier.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Modifier result = targetAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
					if (parented) {
						TypeDeclaration pd = targetAst.newTypeDeclaration();
						pd.modifiers().add(result);
					}
					return result;
				}
				public ASTNode wrap() {
					SingleMemberAnnotation s1 = x.getAST().newSingleMemberAnnotation();
					s1.setValue(x);
					return s1;
				}
				public void unwrap() {
					SingleMemberAnnotation s1 = (SingleMemberAnnotation) x.getParent();
					s1.setValue(x.getAST().newNullLiteral());
				}
			});

			// check that getModifiers() tracks changes to modifiers()
			x.modifiers().clear();
			assertTrue(x.getModifiers() == Modifier.NONE);
			Modifier[] allMods = allModifiers();
			// one at a time
			for (int i = 0 ; i < allMods.length; i++) {
				x.modifiers().add(allMods[i]);
				assertTrue(x.getModifiers() == allMods[i].getKeyword().toFlagValue());
				x.modifiers().remove(allMods[i]);
				assertTrue(x.getModifiers() == Modifier.NONE);
			}
			// all at same time
			for (int i = 0 ; i < allMods.length; i++) {
				x.modifiers().add(allMods[i]);
			}
			int flags  = x.getModifiers();
			for (int i = 0 ; i < allMods.length; i++) {
				assertTrue((flags & allMods[i].getKeyword().toFlagValue()) != 0);
			}
		}

		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyListTest(x, x.fragments(),
		  new Property("VariableSpecifiers", true, VariableDeclarationFragment.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				VariableDeclarationFragment result = targetAst.newVariableDeclarationFragment();
				if (parented) {
					targetAst.newVariableDeclarationExpression(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return VariableDeclarationFragment that embeds x
				VariableDeclarationFragment s1 = ast.newVariableDeclarationFragment();
				ClassInstanceCreation s0 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s0.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s0);
				ForStatement s2 = ast.newForStatement();
				s2.initializers().add(x);
				Initializer s3 = ast.newInitializer();
				a1.bodyDeclarations().add(s3);
				s3.getBody().statements().add(s2);
				return s1;
			}
			public void unwrap() {
				ForStatement s2 = (ForStatement) x.getParent();
				s2.initializers().remove(x);
			}
		});
	}	
	
	/** @deprecated using deprecated code */
	public void testFieldDeclaration() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		long previousCount = ast.modificationCount();
		final FieldDeclaration x = ast.newFieldDeclaration(x1);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getJavadoc() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getModifiers() == Modifier.NONE);
		} else {
			assertTrue(x.modifiers().size() == 0);
		}
		assertTrue(x.getType() != null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.fragments().size() == 1);
		assertTrue(x.fragments().get(0) == x1);
		assertTrue(x1.getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.FIELD_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == 
			FieldDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		if (ast.apiLevel() == AST.JLS2) {
			int legal = Modifier.PUBLIC | Modifier.PROTECTED
				| Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL
				| Modifier.TRANSIENT | Modifier.VOLATILE;
			previousCount = ast.modificationCount();
			x.setModifiers(legal);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == legal);
	
			previousCount = ast.modificationCount();
			x.setModifiers(Modifier.NONE);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}

		tJavadocComment(x);
		tModifiers(x);
						
		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});

		genericPropertyListTest(x, x.fragments(),
		  new Property("VariableSpecifiers", true, VariableDeclarationFragment.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				VariableDeclarationFragment result = targetAst.newVariableDeclarationFragment();
				if (parented) {
					targetAst.newVariableDeclarationStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return VariableDeclarationFragment that embeds x
				VariableDeclarationFragment s1 = ast.newVariableDeclarationFragment();
				ClassInstanceCreation s2 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s2.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s2);
				a1.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration a1 = (AnonymousClassDeclaration) x.getParent();
				a1.bodyDeclarations().remove(x);
			}
		});
	
	}
	
	public void testAssignment() {
		long previousCount = ast.modificationCount();
		final Assignment x = ast.newAssignment();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getOperator() == Assignment.Operator.ASSIGN);
		assertTrue(x.getLeftHandSide().getParent() == x);
		assertTrue(x.getRightHandSide().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.ASSIGNMENT);
		assertTrue(x.structuralPropertiesForType() == 
			Assignment.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		previousCount = ast.modificationCount();
		x.setOperator(Assignment.Operator.PLUS_ASSIGN);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getOperator() == Assignment.Operator.PLUS_ASSIGN);
		assertTrue(Assignment.Operator.PLUS_ASSIGN 
			!= Assignment.Operator.ASSIGN);

		// check the names of the primitive type codes
		assertTrue(Assignment.Operator.ASSIGN.toString().equals("=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.PLUS_ASSIGN.toString().equals("+=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.MINUS_ASSIGN.toString().equals("-=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.TIMES_ASSIGN.toString().equals("*=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.DIVIDE_ASSIGN.toString().equals("/=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.REMAINDER_ASSIGN.toString().equals("%=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.LEFT_SHIFT_ASSIGN.toString().equals("<<=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN.toString().equals(">>=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN.toString().equals(">>>=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.BIT_AND_ASSIGN.toString().equals("&=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.BIT_OR_ASSIGN.toString().equals("|=")); //$NON-NLS-1$
		assertTrue(Assignment.Operator.BIT_XOR_ASSIGN.toString().equals("^=")); //$NON-NLS-1$
		
		Assignment.Operator[] known = {
			Assignment.Operator.ASSIGN,
			Assignment.Operator.PLUS_ASSIGN,
			Assignment.Operator.MINUS_ASSIGN,
			Assignment.Operator.TIMES_ASSIGN,
			Assignment.Operator.DIVIDE_ASSIGN,
			Assignment.Operator.REMAINDER_ASSIGN,
			Assignment.Operator.LEFT_SHIFT_ASSIGN,
			Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN,
			Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN,
			Assignment.Operator.BIT_AND_ASSIGN,
			Assignment.Operator.BIT_OR_ASSIGN,
			Assignment.Operator.BIT_XOR_ASSIGN,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as a primitive type code
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operators by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(Assignment.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(Assignment.Operator.toOperator("not-an-op") == null); //$NON-NLS-1$

		genericPropertyTest(x, new Property("LeftHandSide", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getLeftHandSide();
			}
			public void set(ASTNode value) {
				x.setLeftHandSide((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("RightHandSide", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getRightHandSide();
			}
			public void set(ASTNode value) {
				x.setRightHandSide((Expression) value);
			}
		});
	}	

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testBreakStatement() {
		long previousCount = ast.modificationCount();
		final BreakStatement x = ast.newBreakStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLabel() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.BREAK_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			BreakStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Label", false, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getLabel();
			}
			public void set(ASTNode value) {
				x.setLabel((SimpleName) value);
			}
		});
	}	
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testContinueStatement() {
		long previousCount = ast.modificationCount();
		final ContinueStatement x = ast.newContinueStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLabel() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.CONTINUE_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			ContinueStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Label", false, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getLabel();
			}
			public void set(ASTNode value) {
				x.setLabel((SimpleName) value);
			}
		});
	}	
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testIfStatement() {
		long previousCount = ast.modificationCount();
		final IfStatement x = ast.newIfStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getThenStatement().getParent() == x);
		assertTrue(x.getThenStatement() instanceof Block);
		assertTrue(((Block) x.getThenStatement()).statements().isEmpty());
		assertTrue(x.getElseStatement() == null);
		assertTrue(x.getNodeType() == ASTNode.IF_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			IfStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("ThenStatement", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getThenStatement();
			}
			public void set(ASTNode value) {
				x.setThenStatement((Statement) value);
			}
		});
		
		genericPropertyTest(x, new Property("ElseStatement", false, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getElseStatement();
			}
			public void set(ASTNode value) {
				x.setElseStatement((Statement) value);
			}
		});
	}	
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testWhileStatement() {
		long previousCount = ast.modificationCount();
		final WhileStatement x = ast.newWhileStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.WHILE_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			WhileStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}	
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testDoStatement() {
		long previousCount = ast.modificationCount();
		final DoStatement x = ast.newDoStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.DO_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			DoStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}	
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testTryStatement() {
		long previousCount = ast.modificationCount();
		final TryStatement x = ast.newTryStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getBody().getParent() == x);
		assertTrue((x.getBody()).statements().isEmpty());
		assertTrue(x.getFinally() == null);
		assertTrue(x.catchClauses().size() == 0);
		assertTrue(x.getNodeType() == ASTNode.TRY_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			TryStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tLeadingComment(x);

		genericPropertyTest(x, new Property("Body", true, Block.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});

		genericPropertyListTest(x, x.catchClauses(),
		  new Property("CatchClauses", true, CatchClause.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				CatchClause result = targetAst.newCatchClause();
				if (parented) {
					TryStatement s1 = targetAst.newTryStatement();
					s1.catchClauses().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return CatchClause that embeds x
				CatchClause s1 = ast.newCatchClause();
				Block s2 = ast.newBlock();
				s1.setBody(s2);
				s2.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
		});

		genericPropertyTest(x, new Property("Finally", false, Block.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getFinally();
			}
			public void set(ASTNode value) {
				x.setFinally((Block) value);
			}
		});
	}	

	public void testCatchClause() {
		long previousCount = ast.modificationCount();
		final CatchClause x = ast.newCatchClause();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody().statements().isEmpty());
		assertTrue(x.getException().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.CATCH_CLAUSE);
		assertTrue(x.structuralPropertiesForType() == 
			CatchClause.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Exception", true, SingleVariableDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SingleVariableDeclaration result = targetAst.newSingleVariableDeclaration();
				if (parented) {
					targetAst.newCatchClause().setException(result);
					
				}
				return result;
			}
			public ASTNode wrap() {
				// return SingleVariableDeclaration that embeds x
				SingleVariableDeclaration s1 = ast.newSingleVariableDeclaration();
				ClassInstanceCreation s2 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s2.setAnonymousClassDeclaration(a1);
				s1.setInitializer(s2);
				MethodDeclaration s3 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s3);
				Block s4 = ast.newBlock();
				s3.setBody(s4);
				TryStatement s5 = ast.newTryStatement();
				s4.statements().add(s5);
				s5.catchClauses().add(x);
				return s1;
			}
			public void unwrap() {
				TryStatement s5 = (TryStatement) x.getParent();
				s5.catchClauses().remove(x);
			}
			public ASTNode get() {
				return x.getException();
			}
			public void set(ASTNode value) {
				x.setException((SingleVariableDeclaration) value);
			}
		});

		genericPropertyTest(x, new Property("Body", true, Block.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				TryStatement s2 = ast.newTryStatement();
				s1.statements().add(s2);
				s2.catchClauses().add(x);
				return s1;
			}
			public void unwrap() {
				TryStatement s2 = (TryStatement) x.getParent();
				s2.catchClauses().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testEmptyStatement() {
		long previousCount = ast.modificationCount();
		final EmptyStatement x = ast.newEmptyStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.EMPTY_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			EmptyStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		tLeadingComment(x);
	}
	
	/**
	 * Exercise the leadingComment property.
	 * 
	 * @param x the statement to test
	 * @deprecated (Uses get/setLeadingComment() which is deprecated)
	 */
	void tLeadingComment(Statement x) {
		
		// check that null is allowed
		long previousCount = ast.modificationCount();
		x.setLeadingComment(null);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == null);
		
		// check that regular comment is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("/* X */"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "/* X */"); //$NON-NLS-1$
		
		// check that regular comment with line breaks is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("/* X\n *Y\n */"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "/* X\n *Y\n */"); //$NON-NLS-1$
		
		// check that end-of-line comment is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("// X\n"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "// X\n"); //$NON-NLS-1$

		// check that end-of-line comment without a line break at the end is allowed
		previousCount = ast.modificationCount();
		x.setLeadingComment("// X"); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getLeadingComment() == "// X"); //$NON-NLS-1$
		
		// check that end-of-line comment with embedded end of line 
		// not allowed
		try {
			x.setLeadingComment("// X\n extra"); //$NON-NLS-1$
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}
		
	}
		
	/**
	 * Exercise the javadoc property.
	 * 
	 * @param x the body declaration to test
	 */
	void tJavadocComment(final BodyDeclaration x) {
		genericPropertyTest(x, new Property("Javadoc", false, Javadoc.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Javadoc result = targetAst.newJavadoc();
				if (parented) {
					targetAst.newInitializer().setJavadoc(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getJavadoc();
			}
			public void set(ASTNode value) {
				x.setJavadoc((Javadoc) value);
			}
		});
	}

	/**
	 * Returns a list of all the different Modifier nodes.
	 */
	Modifier[] allModifiers() {
		Modifier[] allMods = {
				ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD),
				ast.newModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD)
		};
		return allMods;
	}
	
	/**
	 * Exercise the modifiers property.
	 * 
	 * @param x the body declaration to test
	 * @deprecated using deprecated code
	 */
	void tModifiers(final BodyDeclaration x) {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		genericPropertyListTest(x, x.modifiers(), new Property("Modifiers", true, IExtendedModifier.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Modifier result = targetAst.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
				if (parented) {
					TypeDeclaration pd = targetAst.newTypeDeclaration();
					pd.modifiers().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				SingleMemberAnnotation s1 = x.getAST().newSingleMemberAnnotation();
				ClassInstanceCreation s2 = x.getAST().newClassInstanceCreation();
				AnonymousClassDeclaration s3 = x.getAST().newAnonymousClassDeclaration();
				MethodDeclaration s4 = x.getAST().newMethodDeclaration();
				s1.setValue(s2);
				s2.setAnonymousClassDeclaration(s3);
				s3.bodyDeclarations().add(s4);
				s4.modifiers().add(x);
				return s1;
			}
			public void unwrap() {
				MethodDeclaration s4 = (MethodDeclaration) x.getParent();
				s4.modifiers().remove(x);
			}
		});
		
		// check that getModifiers() tracks changes to modifiers()
		x.modifiers().clear();
		assertTrue(x.getModifiers() == Modifier.NONE);
		Modifier[] allMods = allModifiers();
		// one at a time
		for (int i = 0 ; i < allMods.length; i++) {
			x.modifiers().add(allMods[i]);
			assertTrue(x.getModifiers() == allMods[i].getKeyword().toFlagValue());
			x.modifiers().remove(allMods[i]);
			assertTrue(x.getModifiers() == Modifier.NONE);
		}
		// all at same time
		for (int i = 0 ; i < allMods.length; i++) {
			x.modifiers().add(allMods[i]);
		}
		int flags  = x.getModifiers();
		for (int i = 0 ; i < allMods.length; i++) {
			assertTrue((flags & allMods[i].getKeyword().toFlagValue()) != 0);
		}
	}

	/**
	 * Exercise the alternateRoot property of a Comment.
	 * 
	 * @param x the comment to test
     * @since 3.0
	 */
	void tAlternateRoot(final Comment x) {
		CompilationUnit cu = ast.newCompilationUnit();
		long previousCount = ast.modificationCount();
		x.setAlternateRoot(cu);
		assertTrue(ast.modificationCount() > previousCount);
		assertTrue(x.getAlternateRoot() == cu);
		previousCount = ast.modificationCount();
		x.setAlternateRoot(null);
		assertTrue(x.getAlternateRoot() == null);
		assertTrue(ast.modificationCount() > previousCount);
	}

	/**
	 * Exercise the client properties of a node.
	 * 
	 * @param x the node to test
	 */
	void tClientProperties(ASTNode x) {
		
		long previousCount = ast.modificationCount();
		
		// a node initially has no properties
		assertTrue(x.properties().size() == 0);
		assertTrue(x.getProperty("1") == null); //$NON-NLS-1$

		// clearing an unset property does not add it to list of known ones
		x.setProperty("1", null); //$NON-NLS-1$
		assertTrue(x.getProperty("1") == null); //$NON-NLS-1$
		assertTrue(x.properties().size() == 0);

		// setting an unset property does add it to the list of known ones
		x.setProperty("1", "a1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("1") == "a1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().size() == 1);
		Map.Entry[] m = (Map.Entry[]) x.properties().entrySet().toArray(new Map.Entry[1]);
		assertTrue(m[0].getKey() == "1"); //$NON-NLS-1$
		assertTrue(m[0].getValue() == "a1"); //$NON-NLS-1$

		// setting an already set property just changes its value
		x.setProperty("1", "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("1") == "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().size() == 1);
		m = (Map.Entry[]) x.properties().entrySet().toArray(new Map.Entry[1]);
		assertTrue(m[0].getKey() == "1"); //$NON-NLS-1$
		assertTrue(m[0].getValue() == "a2"); //$NON-NLS-1$

		// clearing a set property removes it from list of known ones
		x.setProperty("1", null); //$NON-NLS-1$
		assertTrue(x.getProperty("1") == null); //$NON-NLS-1$
		assertTrue(x.properties().size() == 0);
		
		
		// ========= test 2 and 3 properties
		x.setProperty("1", "a1"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("2", "b1"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("3", "c1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("1") == "a1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("2") == "b1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("3") == "c1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().size() == 3);
		assertTrue(x.properties().get("1") == "a1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().get("2") == "b1"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().get("3") == "c1"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("1", "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("2", "b2"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("3", "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("1") == "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("2") == "b2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("3") == "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().size() == 3);
		assertTrue(x.properties().get("1") == "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().get("2") == "b2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().get("3") == "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("2", null); //$NON-NLS-1$
		assertTrue(x.getProperty("1") == "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.getProperty("2") == null); //$NON-NLS-1$
		assertTrue(x.getProperty("3") == "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().size() == 2);
		assertTrue(x.properties().get("1") == "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().get("2") == null); //$NON-NLS-1$
		assertTrue(x.properties().get("3") == "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setProperty("1", null); //$NON-NLS-1$
		assertTrue(x.getProperty("1") == null); //$NON-NLS-1$
		assertTrue(x.getProperty("2") == null); //$NON-NLS-1$
		assertTrue(x.getProperty("3") == "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(x.properties().size() == 1);
		assertTrue(x.properties().get("1") == null); //$NON-NLS-1$
		assertTrue(x.properties().get("2") == null); //$NON-NLS-1$
		assertTrue(x.properties().get("3") == "c2"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// none of this is considered to have affected the AST
		assertTrue(ast.modificationCount() == previousCount);
	}
	
	public void testReturnStatement() {
		long previousCount = ast.modificationCount();
		final ReturnStatement x = ast.newReturnStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == null);
		assertTrue(x.getNodeType() == ASTNode.RETURN_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			ReturnStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testThrowStatement() {
		long previousCount = ast.modificationCount();
		final ThrowStatement x = ast.newThrowStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.THROW_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			ThrowStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testAssertStatement() {
		long previousCount = ast.modificationCount();
		final AssertStatement x = ast.newAssertStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getMessage() == null);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.ASSERT_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			AssertStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Message", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getMessage();
			}
			public void set(ASTNode value) {
				x.setMessage((Expression) value);
			}
		});
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testSwitchStatement() {
		long previousCount = ast.modificationCount();
		final SwitchStatement x = ast.newSwitchStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.SWITCH_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			SwitchStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyListTest(x, x.statements(),
		  new Property("Statements", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
		});
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testSwitchCase() {
		long previousCount = ast.modificationCount();
		final SwitchCase x = ast.newSwitchCase();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(!x.isDefault());	
		assertTrue(x.getNodeType() == ASTNode.SWITCH_CASE);
		assertTrue(x.structuralPropertiesForType() == 
			SwitchCase.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Expression", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				SwitchStatement s4 = ast.newSwitchStatement();
				s3.statements().add(s4);
				s4.statements().add(x);
				return s1;
			}
			public void unwrap() {
				SwitchStatement s4 = (SwitchStatement) x.getParent();
				s4.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testSynchronizedStatement() {
		long previousCount = ast.modificationCount();
		final SynchronizedStatement x = ast.newSynchronizedStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.SYNCHRONIZED_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			SynchronizedStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Block.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Block that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Block) value);
			}
		});
	}
	
	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testLabeledStatement() {
		long previousCount = ast.modificationCount();
		final LabeledStatement x = ast.newLabeledStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLabel().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.LABELED_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			LabeledStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Label", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getLabel();
			}
			public void set(ASTNode value) {
				x.setLabel((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Body", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}
	
	/**
	 * Walks the given AST and assigns properly-nested (but otherwise totally bogus)
	 * source ranges to all nodes.
	 */
	void assignSourceRanges(ASTNode target) {
		final StringBuffer buffer = new StringBuffer();
		final List stack = new ArrayList();
		// pretend that every construct begins with "(" and ends with ")"
		class PositionAssigner extends ASTVisitor {
			PositionAssigner() {
				// visit Javadoc.tags();
				super(true);
			}
			public void preVisit(ASTNode node) {
				int start = buffer.length();
				buffer.append("(");
				// push start position - popped by postVisit for same node
				stack.add(new Integer(start));
			}
			public void postVisit(ASTNode node) {
				// pop start position placed there by preVisit
				int start = ((Integer) stack.remove(stack.size() - 1)).intValue();
				buffer.append(")");
				int length = buffer.length() - start;
				node.setSourceRange(start, length);
			}
		}
		target.accept(new PositionAssigner());
	}
	
	public void testClone() {
		ASTNode x = SampleASTs.oneOfEach(ast);
		assignSourceRanges(x);
		assertTrue(x.subtreeMatch(new CheckPositionsMatcher(), x));
		
		// same AST clone
		ASTNode y = ASTNode.copySubtree(ast, x);
		assertTrue(x.subtreeMatch(new CheckPositionsMatcher(), y));
		assertTrue(y.subtreeMatch(new CheckPositionsMatcher(), x));
		
		// different AST clone
		AST newAST = AST.newAST(ast.apiLevel());
		ASTNode z = ASTNode.copySubtree(newAST, x);
		assertTrue(x.subtreeMatch(new CheckPositionsMatcher(), z));
		assertTrue(z.subtreeMatch(new CheckPositionsMatcher(), x));
	}

	public void testNullResolve() {
		ASTNode x = SampleASTs.oneOfEach(ast);
		
		ASTVisitor v = new ASTVisitor(true) {
			// NAMES

			public boolean visit(SimpleName node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(QualifiedName node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}

			// TYPES
	
			public boolean visit(SimpleType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(ArrayType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(ParameterizedType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(PrimitiveType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(QualifiedType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(WildcardType node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			
			// EXPRESSIONS
	
			public boolean visit(Assignment node) {
				assertTrue(node.resolveTypeBinding() == null);
				return true;
			}
	
			public boolean visit(ClassInstanceCreation node) {
				assertTrue(node.resolveConstructorBinding() == null);
				return true;
			}
	
			public boolean visit(ConstructorInvocation node) {
				assertTrue(node.resolveConstructorBinding() == null);
				return true;
			}
	
			public boolean visit(SuperConstructorInvocation node) {
				assertTrue(node.resolveConstructorBinding() == null);
				return true;
			}
	
			// MAJOR DECLARATIONS
	
			public boolean visit(PackageDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(ImportDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(MethodDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(TypeDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(TypeDeclarationStatement node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(SingleVariableDeclaration node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(VariableDeclarationFragment node) {
				assertTrue(node.resolveBinding() == null);
				return true;
			}
			public boolean visit(EnumConstantDeclaration node) {
				assertTrue(node.resolveVariable() == null);
				return true;
			}
		};
		
		x.accept(v);
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testForStatement() {
		long previousCount = ast.modificationCount();
		final ForStatement x = ast.newForStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.initializers().isEmpty());
		assertTrue(x.getExpression() == null);
		assertTrue(x.updaters().isEmpty());
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.FOR_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			ForStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);
		
		genericPropertyListTest(x, x.initializers(),
		  new Property("Initializers", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});

		genericPropertyTest(x, new Property("Expression", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyListTest(x, x.updaters(),
		  new Property("Updaters", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});

		genericPropertyTest(x, new Property("Body", true, Statement.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				Block result = targetAst.newBlock();
				if (parented) {
					Block b2 = targetAst.newBlock();
					b2.statements().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return a Statement that embeds x
				Block s1 = ast.newBlock();
				s1.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s2 = (Block) x.getParent();
				s2.statements().remove(x);
			}
			public ASTNode get() {
				return x.getBody();
			}
			public void set(ASTNode value) {
				x.setBody((Statement) value);
			}
		});
	}

	/**
	 * @deprecated (Uses getLeadingComment() which is deprecated)
	 */
	public void testEnhancedForStatement() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newEnhancedForStatement();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final EnhancedForStatement x = ast.newEnhancedForStatement();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getParameter() != null);
		assertTrue(x.getParameter().getParent() == x);
		assertTrue(x.getExpression() != null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getBody().getParent() == x);
		assertTrue(x.getBody() instanceof Block);
		assertTrue(((Block) x.getBody()).statements().isEmpty());
		assertTrue(x.getLeadingComment() == null);
		assertTrue(x.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT);
		assertTrue(x.structuralPropertiesForType() == 
			EnhancedForStatement.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		tLeadingComment(x);
		
		genericPropertyTest(x, new Property("Parameter", true, SingleVariableDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SingleVariableDeclaration result = targetAst.newSingleVariableDeclaration();
				if (parented) {
					CatchClause parent = targetAst.newCatchClause();
					parent.setException(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getParameter();
			}
			public void set(ASTNode value) {
				x.setParameter((SingleVariableDeclaration) value);
			}
		});
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST target, boolean parented) {
				Expression result = target.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					target.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
 			}
 		});
	}

	public void testConstructorInvocation() {
		long previousCount = ast.modificationCount();
		final ConstructorInvocation x = ast.newConstructorInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.typeArguments().isEmpty());
		}
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION);
		assertTrue(x.structuralPropertiesForType() == 
			ConstructorInvocation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeArguments(),
			  new Property("TypeArguments", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Type result = targetAst.newSimpleType(targetAst.newSimpleName("X")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
			});
		}
		
		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});
	}

	public void testSuperConstructorInvocation() {
		long previousCount = ast.modificationCount();
		final SuperConstructorInvocation x = ast.newSuperConstructorInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == null);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.typeArguments().isEmpty());
		}
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION);
		assertTrue(x.structuralPropertiesForType() == 
			SuperConstructorInvocation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Expression", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeArguments(),
			  new Property("TypeArguments", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Type result = targetAst.newSimpleType(targetAst.newSimpleName("X")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
			});
		}
		
		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = ast.newClassInstanceCreation();
				AnonymousClassDeclaration a1 = ast.newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(a1);
				MethodDeclaration s2 = ast.newMethodDeclaration();
				a1.bodyDeclarations().add(s2);
				Block s3 = ast.newBlock();
				s2.setBody(s3);
				s3.statements().add(x);
				return s1;
			}
			public void unwrap() {
				Block s3 = (Block) x.getParent();
				s3.statements().remove(x);
			}
		});
	}

	public void testThisExpression() {
		long previousCount = ast.modificationCount();
		final ThisExpression x = ast.newThisExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.getNodeType() == ASTNode.THIS_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			ThisExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Qualifier", false, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"), //$NON-NLS-1$
					targetAst.newSimpleName("b")); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});
	}

	public void testFieldAccess() {
		long previousCount = ast.modificationCount();
		final FieldAccess x = ast.newFieldAccess();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.FIELD_ACCESS);
		assertTrue(x.structuralPropertiesForType() == 
			FieldAccess.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}


	public void testSuperFieldAccess() {
		long previousCount = ast.modificationCount();
		final SuperFieldAccess x = ast.newSuperFieldAccess();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.getNodeType() == ASTNode.SUPER_FIELD_ACCESS);
		assertTrue(x.structuralPropertiesForType() == 
			SuperFieldAccess.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Qualifier", false, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"), //$NON-NLS-1$
					targetAst.newSimpleName("b")); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
	}

	public void testSuperMethodInvocation() {
		long previousCount = ast.modificationCount();
		final SuperMethodInvocation x = ast.newSuperMethodInvocation();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		if (ast.apiLevel() >= AST.JLS3) {
			assertTrue(x.typeArguments().isEmpty());
		}
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getQualifier() == null);
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION);
		assertTrue(x.structuralPropertiesForType() == 
			SuperMethodInvocation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Qualifier", false, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				QualifiedName result = targetAst.newQualifiedName(
					targetAst.newSimpleName("a"), //$NON-NLS-1$
					targetAst.newSimpleName("b")); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getQualifier();
			}
			public void set(ASTNode value) {
				x.setQualifier((Name) value);
			}
		});

		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeArguments(),
			  new Property("TypeArguments", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Type result = targetAst.newSimpleType(targetAst.newSimpleName("X")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
			});
		}
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});

		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
		});
	}

	public void testTypeLiteral() {
		long previousCount = ast.modificationCount();
		final TypeLiteral x = ast.newTypeLiteral();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.TYPE_LITERAL);
		assertTrue(x.structuralPropertiesForType() == 
			TypeLiteral.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("a")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
	}

	public void testCastExpression() {
		long previousCount = ast.modificationCount();
		final CastExpression x = ast.newCastExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.CAST_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			CastExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("a")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}

	public void testPrefixExpression() {
		long previousCount = ast.modificationCount();
		final PrefixExpression x = ast.newPrefixExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getOperand().getParent() == x);
		assertTrue(x.getOperator() != null);
		assertTrue(x.getNodeType() == ASTNode.PREFIX_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			PrefixExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// Operator property - mandatory typesafe enumeration
		// check the names of the operators
		assertTrue(PrefixExpression.Operator.INCREMENT.toString().equals("++")); //$NON-NLS-1$
		assertTrue(PrefixExpression.Operator.DECREMENT.toString().equals("--")); //$NON-NLS-1$
		assertTrue(PrefixExpression.Operator.PLUS.toString().equals("+")); //$NON-NLS-1$
		assertTrue(PrefixExpression.Operator.MINUS.toString().equals("-")); //$NON-NLS-1$
		assertTrue(PrefixExpression.Operator.COMPLEMENT.toString().equals("~")); //$NON-NLS-1$
		assertTrue(PrefixExpression.Operator.NOT.toString().equals("!")); //$NON-NLS-1$
		
		PrefixExpression.Operator[] known = {
			PrefixExpression.Operator.INCREMENT,
			PrefixExpression.Operator.DECREMENT,
			PrefixExpression.Operator.PLUS,
			PrefixExpression.Operator.MINUS,
			PrefixExpression.Operator.COMPLEMENT,
			PrefixExpression.Operator.NOT,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operator by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(PrefixExpression.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(PrefixExpression.Operator.toOperator("huh") == null); //$NON-NLS-1$

		genericPropertyTest(x, new Property("Operand", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getOperand();
			}
			public void set(ASTNode value) {
				x.setOperand((Expression) value);
			}
		});
	}

	public void testPostfixExpression() {
		long previousCount = ast.modificationCount();
		final PostfixExpression x = ast.newPostfixExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getOperand().getParent() == x);
		assertTrue(x.getOperator() != null);
		assertTrue(x.getNodeType() == ASTNode.POSTFIX_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			PostfixExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// Operator property - mandatory typesafe enumeration
		// check the names of the operators
		assertTrue(PostfixExpression.Operator.INCREMENT.toString().equals("++")); //$NON-NLS-1$
		assertTrue(PostfixExpression.Operator.DECREMENT.toString().equals("--")); //$NON-NLS-1$
		
		PostfixExpression.Operator[] known = {
			PostfixExpression.Operator.INCREMENT,
			PostfixExpression.Operator.DECREMENT,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operator by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(PostfixExpression.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(PostfixExpression.Operator.toOperator("huh") == null); //$NON-NLS-1$

		genericPropertyTest(x, new Property("Operand", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getOperand();
			}
			public void set(ASTNode value) {
				x.setOperand((Expression) value);
			}
		});
	}

	public void testInfixExpression() {
		long previousCount = ast.modificationCount();
		final InfixExpression x = ast.newInfixExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeftOperand().getParent() == x);
		assertTrue(x.getOperator() != null);
		assertTrue(x.getRightOperand().getParent() == x);
		assertTrue(x.extendedOperands().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.INFIX_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			InfixExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// Operator property - mandatory typesafe enumeration
		// check the names of the operators
		assertTrue(InfixExpression.Operator.TIMES.toString().equals("*")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.DIVIDE.toString().equals("/")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.REMAINDER.toString().equals("%")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.PLUS.toString().equals("+")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.MINUS.toString().equals("-")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.LEFT_SHIFT.toString().equals("<<")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.RIGHT_SHIFT_SIGNED.toString().equals(">>")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED.toString().equals(">>>")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.LESS.toString().equals("<")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.GREATER.toString().equals(">")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.LESS_EQUALS.toString().equals("<=")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.GREATER_EQUALS.toString().equals(">=")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.EQUALS.toString().equals("==")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.NOT_EQUALS.toString().equals("!=")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.XOR.toString().equals("^")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.OR.toString().equals("|")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.AND.toString().equals("&")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.CONDITIONAL_OR.toString().equals("||")); //$NON-NLS-1$
		assertTrue(InfixExpression.Operator.CONDITIONAL_AND.toString().equals("&&")); //$NON-NLS-1$
		
		InfixExpression.Operator[] known = {
				InfixExpression.Operator.TIMES,
				InfixExpression.Operator.DIVIDE,
				InfixExpression.Operator.REMAINDER,
				InfixExpression.Operator.PLUS,
				InfixExpression.Operator.MINUS,
				InfixExpression.Operator.LEFT_SHIFT,
				InfixExpression.Operator.RIGHT_SHIFT_SIGNED,
				InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED,
				InfixExpression.Operator.LESS,
				InfixExpression.Operator.GREATER,
				InfixExpression.Operator.LESS_EQUALS,
				InfixExpression.Operator.GREATER_EQUALS,
				InfixExpression.Operator.EQUALS,
				InfixExpression.Operator.NOT_EQUALS,
				InfixExpression.Operator.XOR,
				InfixExpression.Operator.OR,
				InfixExpression.Operator.AND,
				InfixExpression.Operator.CONDITIONAL_OR,
				InfixExpression.Operator.CONDITIONAL_AND,
		};
		
		// check all operators are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all operators work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setOperator(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getOperator().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setOperator(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toOperator lookup of operator by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(InfixExpression.Operator.toOperator(name).equals(known[i]));
		}
		assertTrue(InfixExpression.Operator.toOperator("huh") == null); //$NON-NLS-1$

		genericPropertyTest(x, new Property("LeftOperand", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getLeftOperand();
			}
			public void set(ASTNode value) {
				x.setLeftOperand((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("RightOperand", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getRightOperand();
			}
			public void set(ASTNode value) {
				x.setRightOperand((Expression) value);
			}
		});

		genericPropertyListTest(x, x.extendedOperands(),
		  new Property("ExtendedOperands", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
		});
	}

	public void testInstanceofExpression() {
		long previousCount = ast.modificationCount();
		final InstanceofExpression x = ast.newInstanceofExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getLeftOperand().getParent() == x);
		assertTrue(x.getRightOperand().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			InstanceofExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
						
		genericPropertyTest(x, new Property("LeftOperand", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getLeftOperand();
			}
			public void set(ASTNode value) {
				x.setLeftOperand((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("RightOperand", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Type result = localAst.newSimpleType(localAst.newSimpleName("Object")); //$NON-NLS-1$
				if (parented) {
					localAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getRightOperand();
			}
			public void set(ASTNode value) {
				x.setRightOperand((Type) value);
			}
		});
	}

	public void testConditionalExpression() {
		long previousCount = ast.modificationCount();
		final ConditionalExpression x = ast.newConditionalExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getThenExpression().getParent() == x);
		assertTrue(x.getElseExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			ConditionalExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("ThenExpression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getThenExpression();
			}
			public void set(ASTNode value) {
				x.setThenExpression((Expression) value);
			}
		});
		
		genericPropertyTest(x, new Property("ElseExpression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getElseExpression();
			}
			public void set(ASTNode value) {
				x.setElseExpression((Expression) value);
			}
		});
	}

	public void testArrayAccess() {
		long previousCount = ast.modificationCount();
		final ArrayAccess x = ast.newArrayAccess();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getArray().getParent() == x);
		assertTrue(x.getIndex().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.ARRAY_ACCESS);
		assertTrue(x.structuralPropertiesForType() == 
			ArrayAccess.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Array", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getArray();
			}
			public void set(ASTNode value) {
				x.setArray((Expression) value);
			}
		});

		genericPropertyTest(x, new Property("Index", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getIndex();
			}
			public void set(ASTNode value) {
				x.setIndex((Expression) value);
			}
		});
	}

	public void testArrayInitializer() {
		long previousCount = ast.modificationCount();
		final ArrayInitializer x = ast.newArrayInitializer();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.expressions().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.ARRAY_INITIALIZER);
		assertTrue(x.structuralPropertiesForType() == 
			ArrayInitializer.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyListTest(x, x.expressions(),
		  new Property("Expressions", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
		});
	}

	/** @deprecated using deprecated code */
	public void testClassInstanceCreation() {
		long previousCount = ast.modificationCount();
		final ClassInstanceCreation x = ast.newClassInstanceCreation();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression() == null);
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(x.getName().getParent() == x);
		} else {
			assertTrue(x.typeArguments().isEmpty());
			assertTrue(x.getType().getParent() == x);
		}
		assertTrue(x.arguments().isEmpty());
		assertTrue(x.getAnonymousClassDeclaration() == null);
		assertTrue(x.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION);
		assertTrue(x.structuralPropertiesForType() == 
			ClassInstanceCreation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Expression", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});

		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyListTest(x, x.typeArguments(),
			  new Property("TypeArguments", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					Type result = targetAst.newSimpleType(targetAst.newSimpleName("X")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
			});
		}
		
		if (ast.apiLevel() == AST.JLS2) {
			genericPropertyTest(x, new Property("Name", true, Name.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
					if (parented) {
						targetAst.newExpressionStatement(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getName();
				}
				public void set(ASTNode value) {
					x.setName((Name) value);
				}
			});
		}
		
		if (ast.apiLevel() >= AST.JLS3) {
			genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
				public ASTNode sample(AST targetAst, boolean parented) {
					SimpleType result = targetAst.newSimpleType(targetAst.newSimpleName("foo")); //$NON-NLS-1$
					if (parented) {
						targetAst.newArrayType(result);
					}
					return result;
				}
				public ASTNode get() {
					return x.getType();
				}
				public void set(ASTNode value) {
					x.setType((Type) value);
				}
			});
		}
		
		genericPropertyListTest(x, x.arguments(),
		  new Property("Arguments", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
		});
		
		genericPropertyTest(x, new Property("AnonymousClassDeclaration", false, AnonymousClassDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				AnonymousClassDeclaration result = targetAst.newAnonymousClassDeclaration();
				if (parented) {
					targetAst.newClassInstanceCreation().setAnonymousClassDeclaration(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return AnonymousClassDeclaration that embeds x
				AnonymousClassDeclaration s0 = x.getAST().newAnonymousClassDeclaration();
				VariableDeclarationFragment s1 = x.getAST().newVariableDeclarationFragment();
				FieldDeclaration s2 = x.getAST().newFieldDeclaration(s1);
				s0.bodyDeclarations().add(s2);
				s1.setInitializer(x);
				return s0;
			}
			public void unwrap() {
				VariableDeclarationFragment s1 = (VariableDeclarationFragment) x.getParent();
				s1.setInitializer(null);
			}
			public ASTNode get() {
				return x.getAnonymousClassDeclaration();
			}
			public void set(ASTNode value) {
				x.setAnonymousClassDeclaration((AnonymousClassDeclaration) value);
			}
		});

	}

	public void testAnonymousClassDeclaration() {
		long previousCount = ast.modificationCount();
		final AnonymousClassDeclaration x = ast.newAnonymousClassDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.bodyDeclarations().isEmpty());
		assertTrue(x.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == 
			AnonymousClassDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyListTest(x, x.bodyDeclarations(),
		  new Property("BodyDeclarations", true, BodyDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				TypeDeclaration result = targetAst.newTypeDeclaration();
				if (parented) {
					CompilationUnit compilationUnit = targetAst.newCompilationUnit();
					compilationUnit.types().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return BodyDeclaration that embeds x
				VariableDeclarationFragment s0 = x.getAST().newVariableDeclarationFragment();
				FieldDeclaration s1 = x.getAST().newFieldDeclaration(s0);
				ClassInstanceCreation s2= x.getAST().newClassInstanceCreation(); 
				s0.setInitializer(s2);
				s2.setAnonymousClassDeclaration(x);
				return s1;
			}
			public void unwrap() {
				ClassInstanceCreation s2 = (ClassInstanceCreation) x.getParent();
				s2.setAnonymousClassDeclaration(null);
			}
		});
		
		// check that TypeDeclarations in body are classified correctly
		TypeDeclaration t1 = ast.newTypeDeclaration();
		x.bodyDeclarations().add(t1);
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == true);
		assertTrue(t1.isPackageMemberTypeDeclaration() == false);
	}

	public void testArrayCreation() {
		long previousCount = ast.modificationCount();
		final ArrayCreation x = ast.newArrayCreation();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.dimensions().isEmpty());
		assertTrue(x.getInitializer() == null);
		assertTrue(x.getNodeType() == ASTNode.ARRAY_CREATION);
		assertTrue(x.structuralPropertiesForType() == 
			ArrayCreation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		genericPropertyTest(x, new Property("Type", true, ArrayType.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				ArrayType result = targetAst.newArrayType(
					targetAst.newSimpleType(targetAst.newSimpleName("a"))); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((ArrayType) value);
			}
		});
		
		genericPropertyListTest(x, x.dimensions(),
		  new Property("Dimensions", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("x")); //$NON-NLS-1$
			}
		});

		genericPropertyTest(x, new Property("Initializer", false, ArrayInitializer.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				ArrayInitializer result = targetAst.newArrayInitializer();
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return ArrayInitializer that embeds x
				ArrayInitializer s1 = ast.newArrayInitializer();
				s1.expressions().add(x);
				return s1;
			}
			public void unwrap() {
				ArrayInitializer s1 = (ArrayInitializer) x.getParent();
				s1.expressions().remove(x);
			}
			public ASTNode get() {
				return x.getInitializer();
			}
			public void set(ASTNode value) {
				x.setInitializer((ArrayInitializer) value);
			}
		});
	}

	public void testParenthesizedExpression() {
		long previousCount = ast.modificationCount();
		final ParenthesizedExpression x = ast.newParenthesizedExpression();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getExpression().getParent() == x);
		assertTrue(x.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION);
		assertTrue(x.structuralPropertiesForType() == 
			ParenthesizedExpression.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Expression", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				ParenthesizedExpression s1 = ast.newParenthesizedExpression();
				s1.setExpression(x);
				return s1;
			}
			public void unwrap() {
				ParenthesizedExpression s1 = (ParenthesizedExpression) x.getParent();
				s1.setExpression(ast.newSimpleName("fie")); //$NON-NLS-1$
			}
			public ASTNode get() {
				return x.getExpression();
			}
			public void set(ASTNode value) {
				x.setExpression((Expression) value);
			}
		});
	}
	
	/** @deprecated using deprecated code */
	public void testAnnotationTypeDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newAnnotationTypeDeclaration();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final AnnotationTypeDeclaration x = ast.newAnnotationTypeDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.modifiers().size() == 0);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.bodyDeclarations().size()== 0);
		assertTrue(x.getNodeType() == ASTNode.ANNOTATION_TYPE_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == 
			AnnotationTypeDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		previousCount = ast.modificationCount();
		
		tJavadocComment(x);
		tModifiers(x);		
		
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyListTest(x, x.bodyDeclarations(),
		  new Property("BodyDeclarations", true, BodyDeclaration.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				AnnotationTypeMemberDeclaration result = targetAst.newAnnotationTypeMemberDeclaration();
				if (parented) {
					AnnotationTypeDeclaration atd = targetAst.newAnnotationTypeDeclaration();
					atd.bodyDeclarations().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return AnnotationTypeMemberDeclaration that embeds x
				AnnotationTypeMemberDeclaration s1 = x.getAST().newAnnotationTypeMemberDeclaration();
				ClassInstanceCreation s2 = x.getAST().newClassInstanceCreation();
				AnonymousClassDeclaration s3 = x.getAST().newAnonymousClassDeclaration();
				s1.setDefault(s2);
				s2.setAnonymousClassDeclaration(s3);
				s3.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration s3 = (AnonymousClassDeclaration) x.getParent();
				s3.bodyDeclarations().remove(x);
			}
		});
		
		// check that TypeDeclarations in body are classified correctly
		assertTrue(x.isLocalTypeDeclaration() == false);
		assertTrue(x.isMemberTypeDeclaration() == false);
		assertTrue(x.isPackageMemberTypeDeclaration() == false);
	
		// check special bodyDeclaration methods
		TypeDeclaration t0 = ast.newTypeDeclaration();
		AnnotationTypeDeclaration t1 = ast.newAnnotationTypeDeclaration();
		t0.bodyDeclarations().add(t1);
		assertTrue(t1.isLocalTypeDeclaration() == false);
		assertTrue(t1.isMemberTypeDeclaration() == true);
		assertTrue(t1.isPackageMemberTypeDeclaration() == false);
		
		CompilationUnit t2 = ast.newCompilationUnit();
		AnnotationTypeDeclaration t3 = ast.newAnnotationTypeDeclaration();
		t2.types().add(t3);
		assertTrue(t3.isLocalTypeDeclaration() == false);
		assertTrue(t3.isMemberTypeDeclaration() == false);
		assertTrue(t3.isPackageMemberTypeDeclaration() == true);
	}

	/** @deprecated using deprecated code */
	public void testAnnotationTypeMemberDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newAnnotationTypeMemberDeclaration();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final AnnotationTypeMemberDeclaration x = ast.newAnnotationTypeMemberDeclaration();
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.modifiers().size() == 0);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == true);
		assertTrue(x.getType().getParent() == x);
		assertTrue(x.getJavadoc() == null);
		assertTrue(x.getDefault() == null);
		assertTrue(x.getNodeType() == ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION);
		assertTrue(x.structuralPropertiesForType() == 
			AnnotationTypeMemberDeclaration.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
	
		tJavadocComment(x);
		tModifiers(x);
						
		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Type", true, Type.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleType result = targetAst.newSimpleType(
					targetAst.newSimpleName("foo")); //$NON-NLS-1$
				if (parented) {
					targetAst.newArrayType(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getType();
			}
			public void set(ASTNode value) {
				x.setType((Type) value);
			}
		});
		
		genericPropertyTest(x, new Property("Default", false, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = x.getAST().newClassInstanceCreation();
				AnonymousClassDeclaration s2 = x.getAST().newAnonymousClassDeclaration();
				s1.setAnonymousClassDeclaration(s2);
				s2.bodyDeclarations().add(x);
				return s1;
			}
			public void unwrap() {
				AnonymousClassDeclaration s2 = (AnonymousClassDeclaration) x.getParent();
				s2.bodyDeclarations().remove(x);
			}
			public ASTNode get() {
				return x.getDefault();
			}
			public void set(ASTNode value) {
				x.setDefault((Expression) value);
			}
		});
	}

	/** @deprecated using deprecated code */
	public void testNormalAnnotation() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newNormalAnnotation();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final NormalAnnotation x = ast.newNormalAnnotation(); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getTypeName().getParent() == x);
		assertTrue(x.values().size() == 0);
		assertTrue(x.isAnnotation());
		assertTrue(!x.isModifier());
		assertTrue(!x.isMarkerAnnotation());
		assertTrue(x.isNormalAnnotation());
		assertTrue(!x.isSingleMemberAnnotation());
		assertTrue(x.getNodeType() == ASTNode.NORMAL_ANNOTATION);
		assertTrue(x.structuralPropertiesForType() == 
			NormalAnnotation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		tAnnotationName(x);

		genericPropertyListTest(x, x.values(), new Property("Values", true, MemberValuePair.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				MemberValuePair result = targetAst.newMemberValuePair();
				if (parented) {
					NormalAnnotation ann = targetAst.newNormalAnnotation();
					ann.values().add(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return MemberValuePair that embeds x
				MemberValuePair s1 = x.getAST().newMemberValuePair();
				ClassInstanceCreation s2 = x.getAST().newClassInstanceCreation();
				AnonymousClassDeclaration s3 = x.getAST().newAnonymousClassDeclaration();
				MethodDeclaration s4 = x.getAST().newMethodDeclaration();
				s1.setValue(s2);
				s2.setAnonymousClassDeclaration(s3);
				s3.bodyDeclarations().add(s4);
				s4.modifiers().add(x);
				return s1;
			}
			public void unwrap() {
				MethodDeclaration s4 = (MethodDeclaration) x.getParent();
				s4.modifiers().remove(x);
			}
		});
	}
		
	/** @deprecated using deprecated code */
	public void testMarkerAnnotation() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newMarkerAnnotation();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final MarkerAnnotation x = ast.newMarkerAnnotation(); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getTypeName().getParent() == x);
		assertTrue(x.isAnnotation());
		assertTrue(!x.isModifier());
		assertTrue(x.isMarkerAnnotation());
		assertTrue(!x.isNormalAnnotation());
		assertTrue(!x.isSingleMemberAnnotation());
		assertTrue(x.getNodeType() == ASTNode.MARKER_ANNOTATION);
		assertTrue(x.structuralPropertiesForType() == 
			MarkerAnnotation.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		tAnnotationName(x);
	}

	/** @deprecated using deprecated code */
	public void testSingleMemberAnnotation() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newSingleMemberAnnotation();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final SingleMemberAnnotation x = ast.newSingleMemberAnnotation(); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getTypeName().getParent() == x);
		assertTrue(x.isAnnotation());
		assertTrue(!x.isModifier());
		assertTrue(!x.isMarkerAnnotation());
		assertTrue(!x.isNormalAnnotation());
		assertTrue(x.isSingleMemberAnnotation());
		assertTrue(x.getNodeType() == ASTNode.SINGLE_MEMBER_ANNOTATION);
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		tAnnotationName(x);

		genericPropertyTest(x, new Property("Value", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = x.getAST().newClassInstanceCreation();
				AnonymousClassDeclaration s2 = x.getAST().newAnonymousClassDeclaration();
				MethodDeclaration s3 = x.getAST().newMethodDeclaration();
				s1.setAnonymousClassDeclaration(s2);
				s2.bodyDeclarations().add(s3);
				s3.modifiers().add(x);
				return s1;
			}
			public void unwrap() {
				MethodDeclaration s3 = (MethodDeclaration) x.getParent();
				s3.modifiers().remove(x);
			}
			public ASTNode get() {
				return x.getValue();
			}
			public void set(ASTNode value) {
				x.setValue((Expression) value);
			}
		});
	}

	/** @deprecated using deprecated code */
	public void testMemberValuePair() {
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newMemberValuePair();
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			return;
		}
		long previousCount = ast.modificationCount();
		final MemberValuePair x = ast.newMemberValuePair(); //$NON-NLS-1$
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getName().getParent() == x);
		assertTrue(x.getName().isDeclaration() == false);
		assertTrue(x.getNodeType() == ASTNode.MEMBER_VALUE_PAIR);
		assertTrue(x.structuralPropertiesForType() == 
			MemberValuePair.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);

		genericPropertyTest(x, new Property("Name", true, SimpleName.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getName();
			}
			public void set(ASTNode value) {
				x.setName((SimpleName) value);
			}
		});
		
		genericPropertyTest(x, new Property("Value", true, Expression.class) { //$NON-NLS-1$
			public ASTNode sample(AST localAst, boolean parented) {
				Expression result = localAst.newSimpleName("foo"); //$NON-NLS-1$
				if (parented) {
					localAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode wrap() {
				// return Expression that embeds x
				ClassInstanceCreation s1 = x.getAST().newClassInstanceCreation();
				AnonymousClassDeclaration s2 = x.getAST().newAnonymousClassDeclaration();
				MethodDeclaration s3 = x.getAST().newMethodDeclaration();
				NormalAnnotation s4 = x.getAST().newNormalAnnotation();
				s1.setAnonymousClassDeclaration(s2);
				s2.bodyDeclarations().add(s3);
				s3.modifiers().add(s4);
				s4.values().add(x);
				return s1;
			}
			public void unwrap() {
				NormalAnnotation s4 = (NormalAnnotation) x.getParent();
				s4.values().remove(x);
			}
			public ASTNode get() {
				return x.getValue();
			}
			public void set(ASTNode value) {
				x.setValue((Expression) value);
			}
		});
	}
	
	/**
	 * Exercise the typeName property of an Annotation.
	 * 
	 * @param x the annotation to test
     * @since 3.0
	 */
	public void tAnnotationName(final Annotation x) {
		genericPropertyTest(x, new Property("TypeName", true, Name.class) { //$NON-NLS-1$
			public ASTNode sample(AST targetAst, boolean parented) {
				SimpleName result = targetAst.newSimpleName("a"); //$NON-NLS-1$
				if (parented) {
					targetAst.newExpressionStatement(result);
				}
				return result;
			}
			public ASTNode get() {
				return x.getTypeName();
			}
			public void set(ASTNode value) {
				x.setTypeName((Name) value);
			}
		});
	}

	/** @deprecated using deprecated code */
	public void testModifiers() {
		
		// check all modifiers match their JVM spec values
		assertTrue(Modifier.ABSTRACT == 0x0400);
		assertTrue(Modifier.FINAL == 0x0010);
		assertTrue(Modifier.NATIVE == 0x0100);
		assertTrue(Modifier.NONE == 0x0000);
		assertTrue(Modifier.PRIVATE == 0x0002);
		assertTrue(Modifier.PROTECTED == 0x0004);
		assertTrue(Modifier.PUBLIC == 0x0001);
		assertTrue(Modifier.STATIC == 0x0008);
		assertTrue(Modifier.STRICTFP == 0x0800);
		assertTrue(Modifier.SYNCHRONIZED == 0x0020);
		assertTrue(Modifier.TRANSIENT == 0x0080);
		assertTrue(Modifier.VOLATILE == 0x0040);
		
		// check that all
		final int[] mods =
			{
				Modifier.ABSTRACT,
				Modifier.FINAL,
				Modifier.NATIVE,
				Modifier.PRIVATE,
				Modifier.PROTECTED,
				Modifier.PUBLIC,
				Modifier.STATIC,
				Modifier.STRICTFP,
				Modifier.SYNCHRONIZED,
				Modifier.TRANSIENT,
				Modifier.VOLATILE,
				};
				
		for (int i=0; i< mods.length; i++) {
			int m = mods[i];
			assertTrue(Modifier.isAbstract(m) == (m == Modifier.ABSTRACT));
			assertTrue(Modifier.isFinal(m) == (m == Modifier.FINAL));
			assertTrue(Modifier.isNative(m) == (m == Modifier.NATIVE));
			assertTrue(Modifier.isPrivate(m) == (m == Modifier.PRIVATE));
			assertTrue(Modifier.isProtected(m) == (m == Modifier.PROTECTED));
			assertTrue(Modifier.isPublic(m) == (m == Modifier.PUBLIC));
			assertTrue(Modifier.isStatic(m) == (m == Modifier.STATIC));
			assertTrue(Modifier.isStrictfp(m) == (m == Modifier.STRICTFP));
			assertTrue(Modifier.isSynchronized(m) == (m == Modifier.SYNCHRONIZED));
			assertTrue(Modifier.isTransient(m) == (m == Modifier.TRANSIENT));
			assertTrue(Modifier.isVolatile(m) == (m == Modifier.VOLATILE));
		}
		
		if (ast.apiLevel() == AST.JLS2) {
			// node type introduced in 3.0 API
			try {
				ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			try {
				ast.newModifiers(Modifier.NONE);
				assertTrue(false);
			} catch (UnsupportedOperationException e) {
				// pass
			}
			// skip rest of tests
			return;
		}
		
		// JLS3 only
		long previousCount = ast.modificationCount();
		final Modifier x = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
		assertTrue(ast.modificationCount() > previousCount);
		previousCount = ast.modificationCount();
		assertTrue(x.getAST() == ast);
		assertTrue(x.getParent() == null);
		assertTrue(x.getKeyword() == Modifier.ModifierKeyword.PUBLIC_KEYWORD);
		assertTrue(x.getNodeType() == ASTNode.MODIFIER);
		assertTrue(x.structuralPropertiesForType() == 
			Modifier.propertyDescriptors(ast.apiLevel()));
		// make sure that reading did not change modification count
		assertTrue(ast.modificationCount() == previousCount);
		
		// ModifierKeyword property - mandatory typesafe enumeration
		// check the names of the modifiers
		assertTrue(Modifier.ModifierKeyword.PUBLIC_KEYWORD.toString().equals("public")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.PROTECTED_KEYWORD.toString().equals("protected")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.PRIVATE_KEYWORD.toString().equals("private")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.STATIC_KEYWORD.toString().equals("static")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.ABSTRACT_KEYWORD.toString().equals("abstract")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.FINAL_KEYWORD.toString().equals("final")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD.toString().equals("synchronized")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.NATIVE_KEYWORD.toString().equals("native")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.TRANSIENT_KEYWORD.toString().equals("transient")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.VOLATILE_KEYWORD.toString().equals("volatile")); //$NON-NLS-1$
		assertTrue(Modifier.ModifierKeyword.STRICTFP_KEYWORD.toString().equals("strictfp")); //$NON-NLS-1$

		final Modifier.ModifierKeyword[] known = {
			Modifier.ModifierKeyword.PUBLIC_KEYWORD,
			Modifier.ModifierKeyword.PROTECTED_KEYWORD,
			Modifier.ModifierKeyword.PRIVATE_KEYWORD,
			Modifier.ModifierKeyword.STATIC_KEYWORD,
			Modifier.ModifierKeyword.ABSTRACT_KEYWORD,
			Modifier.ModifierKeyword.FINAL_KEYWORD,
			Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD,
			Modifier.ModifierKeyword.NATIVE_KEYWORD,
			Modifier.ModifierKeyword.TRANSIENT_KEYWORD,
			Modifier.ModifierKeyword.VOLATILE_KEYWORD,
			Modifier.ModifierKeyword.STRICTFP_KEYWORD,
		};
		
		// check all modifiers are distinct
		for (int i = 0; i < known.length; i++) {
			for (int j = 0; j < known.length; j++) {
				assertTrue(i == j || !known[i].equals(known[j]));
			}
		}

		// check all modifiers work
		for (int i = 0; i < known.length; i++) {
			previousCount = ast.modificationCount();
			x.setKeyword(known[i]);
			assertTrue(ast.modificationCount() > previousCount);
			assertTrue(x.getKeyword().equals(known[i]));
		}
		// ensure null does not work as an operator
		try {
			x.setKeyword(null);
			assertTrue(false);
		} catch (RuntimeException e) {
			// pass
		}

		// check toKeyword lookup of modifier by name
		for (int i = 0; i < known.length; i++) {
			String name = known[i].toString();
			assertTrue(Modifier.ModifierKeyword.toKeyword(name).equals(known[i]));
		}
		assertTrue(Modifier.ModifierKeyword.toKeyword("huh") == null); //$NON-NLS-1$

		// check AST.newModifiers(flags)
		for (int i = 0; i < mods.length; i++) {
			int m = mods[i];
			List result = ast.newModifiers(m);
			assertEquals(1, result.size());
			Modifier modNode = (Modifier) result.get(0);
			assertEquals(m, modNode.getKeyword().toFlagValue());
		}
		
		// check AST.newModifiers ordering
		final Modifier.ModifierKeyword[] expectedOrder = {
				Modifier.ModifierKeyword.PUBLIC_KEYWORD,
				Modifier.ModifierKeyword.PROTECTED_KEYWORD,
				Modifier.ModifierKeyword.PRIVATE_KEYWORD,
				Modifier.ModifierKeyword.ABSTRACT_KEYWORD,
				Modifier.ModifierKeyword.STATIC_KEYWORD,
				Modifier.ModifierKeyword.FINAL_KEYWORD,
				Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD,
				Modifier.ModifierKeyword.NATIVE_KEYWORD,
				Modifier.ModifierKeyword.STRICTFP_KEYWORD,
				Modifier.ModifierKeyword.TRANSIENT_KEYWORD,
				Modifier.ModifierKeyword.VOLATILE_KEYWORD,
			};
		int all = 0;
		for (int i = 0; i < mods.length; i++) {
			all |= mods[i];
		}
		List result = ast.newModifiers(all);
		assertEquals(expectedOrder.length, result.size());
		for (int i = 0; i< expectedOrder.length; i++) {
			final Modifier modifier = ((Modifier) result.get(i));
			assertEquals(expectedOrder[i], modifier.getKeyword());
			if (modifier.isAbstract()) {
				assertEquals(Modifier.ModifierKeyword.ABSTRACT_KEYWORD, modifier.getKeyword());
			} else if (modifier.isFinal()) {
				assertEquals(Modifier.ModifierKeyword.FINAL_KEYWORD, modifier.getKeyword());
			} else if (modifier.isNative()) {
				assertEquals(Modifier.ModifierKeyword.NATIVE_KEYWORD, modifier.getKeyword());
			} else if (modifier.isPrivate()) {
				assertEquals(Modifier.ModifierKeyword.PRIVATE_KEYWORD, modifier.getKeyword());
			} else if (modifier.isProtected()) {
				assertEquals(Modifier.ModifierKeyword.PROTECTED_KEYWORD, modifier.getKeyword());
			} else if (modifier.isPublic()) {
				assertEquals(Modifier.ModifierKeyword.PUBLIC_KEYWORD, modifier.getKeyword());
			} else if (modifier.isStatic()) {
				assertEquals(Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getKeyword());
			} else if (modifier.isStrictfp()) {
				assertEquals(Modifier.ModifierKeyword.STRICTFP_KEYWORD, modifier.getKeyword());
			} else if (modifier.isSynchronized()) {
				assertEquals(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD, modifier.getKeyword());
			} else if (modifier.isTransient()) {
				assertEquals(Modifier.ModifierKeyword.TRANSIENT_KEYWORD, modifier.getKeyword());
			} else {
				assertEquals(Modifier.ModifierKeyword.VOLATILE_KEYWORD, modifier.getKeyword());
			} 
		}
	}
		
	public void testSubtreeBytes() {
		ASTNode x = SampleASTs.oneOfEach(ast);
		final int subtreeBytes = x.subtreeBytes();
		assertTrue(subtreeBytes > 0);
	}
	
	public void testNodeTypeConstants() {
		// it would be a breaking API change to change the numeric values of
		// public static final ints
		assertTrue(ASTNode.ANONYMOUS_CLASS_DECLARATION == 1);
		assertTrue(ASTNode.ARRAY_ACCESS == 2);
		assertTrue(ASTNode.ARRAY_CREATION == 3);
		assertTrue(ASTNode.ARRAY_INITIALIZER == 4);
		assertTrue(ASTNode.ARRAY_TYPE == 5);
		assertTrue(ASTNode.ASSERT_STATEMENT == 6);
		assertTrue(ASTNode.ASSIGNMENT == 7);
		assertTrue(ASTNode.BLOCK == 8);
		assertTrue(ASTNode.BOOLEAN_LITERAL == 9);
		assertTrue(ASTNode.BREAK_STATEMENT == 10);
		assertTrue(ASTNode.CAST_EXPRESSION == 11);
		assertTrue(ASTNode.CATCH_CLAUSE == 12);
		assertTrue(ASTNode.CHARACTER_LITERAL == 13);
		assertTrue(ASTNode.CLASS_INSTANCE_CREATION == 14);
		assertTrue(ASTNode.COMPILATION_UNIT == 15);
		assertTrue(ASTNode.CONDITIONAL_EXPRESSION == 16);
		assertTrue(ASTNode.CONSTRUCTOR_INVOCATION == 17);
		assertTrue(ASTNode.CONTINUE_STATEMENT == 18);
		assertTrue(ASTNode.DO_STATEMENT == 19);
		assertTrue(ASTNode.EMPTY_STATEMENT == 20);
		assertTrue(ASTNode.EXPRESSION_STATEMENT == 21);
		assertTrue(ASTNode.FIELD_ACCESS == 22);
		assertTrue(ASTNode.FIELD_DECLARATION == 23);
		assertTrue(ASTNode.FOR_STATEMENT == 24);
		assertTrue(ASTNode.IF_STATEMENT == 25);
		assertTrue(ASTNode.IMPORT_DECLARATION == 26);
		assertTrue(ASTNode.INFIX_EXPRESSION == 27);
		assertTrue(ASTNode.INITIALIZER == 28);
		assertTrue(ASTNode.JAVADOC == 29);
		assertTrue(ASTNode.LABELED_STATEMENT == 30);
		assertTrue(ASTNode.METHOD_DECLARATION == 31);
		assertTrue(ASTNode.METHOD_INVOCATION == 32);
		assertTrue(ASTNode.NULL_LITERAL == 33);
		assertTrue(ASTNode.NUMBER_LITERAL == 34);
		assertTrue(ASTNode.PACKAGE_DECLARATION == 35);
		assertTrue(ASTNode.PARENTHESIZED_EXPRESSION == 36);
		assertTrue(ASTNode.POSTFIX_EXPRESSION == 37);
		assertTrue(ASTNode.PREFIX_EXPRESSION == 38);
		assertTrue(ASTNode.PRIMITIVE_TYPE == 39);
		assertTrue(ASTNode.QUALIFIED_NAME == 40);
		assertTrue(ASTNode.RETURN_STATEMENT == 41);
		assertTrue(ASTNode.SIMPLE_NAME == 42);
		assertTrue(ASTNode.SIMPLE_TYPE == 43);
		assertTrue(ASTNode.SINGLE_VARIABLE_DECLARATION == 44);
		assertTrue(ASTNode.STRING_LITERAL == 45);
		assertTrue(ASTNode.SUPER_CONSTRUCTOR_INVOCATION == 46);
		assertTrue(ASTNode.SUPER_FIELD_ACCESS == 47);
		assertTrue(ASTNode.SUPER_METHOD_INVOCATION == 48);
		assertTrue(ASTNode.SWITCH_CASE == 49);
		assertTrue(ASTNode.SWITCH_STATEMENT == 50);
		assertTrue(ASTNode.SYNCHRONIZED_STATEMENT == 51);
		assertTrue(ASTNode.THIS_EXPRESSION == 52);
		assertTrue(ASTNode.THROW_STATEMENT == 53);
		assertTrue(ASTNode.TRY_STATEMENT == 54);
		assertTrue(ASTNode.TYPE_DECLARATION == 55);
		assertTrue(ASTNode.TYPE_DECLARATION_STATEMENT == 56);
		assertTrue(ASTNode.TYPE_LITERAL == 57);
		assertTrue(ASTNode.VARIABLE_DECLARATION_EXPRESSION == 58);
		assertTrue(ASTNode.VARIABLE_DECLARATION_FRAGMENT == 59);
		assertTrue(ASTNode.VARIABLE_DECLARATION_STATEMENT == 60);
		assertTrue(ASTNode.WHILE_STATEMENT == 61);
		assertTrue(ASTNode.INSTANCEOF_EXPRESSION == 62);
		assertTrue(ASTNode.LINE_COMMENT == 63);
		assertTrue(ASTNode.BLOCK_COMMENT == 64);
		assertTrue(ASTNode.TAG_ELEMENT == 65);
		assertTrue(ASTNode.TEXT_ELEMENT == 66);
		assertTrue(ASTNode.MEMBER_REF == 67);
		assertTrue(ASTNode.METHOD_REF == 68);
		assertTrue(ASTNode.METHOD_REF_PARAMETER == 69);
		assertTrue(ASTNode.ENHANCED_FOR_STATEMENT == 70);
		assertTrue(ASTNode.ENUM_DECLARATION == 71);
		assertTrue(ASTNode.ENUM_CONSTANT_DECLARATION == 72);
		assertTrue(ASTNode.TYPE_PARAMETER == 73);
		assertTrue(ASTNode.PARAMETERIZED_TYPE == 74);
		assertTrue(ASTNode.QUALIFIED_TYPE == 75);
		assertTrue(ASTNode.WILDCARD_TYPE == 76);
		assertTrue(ASTNode.NORMAL_ANNOTATION == 77);
		assertTrue(ASTNode.MARKER_ANNOTATION == 78);
		assertTrue(ASTNode.SINGLE_MEMBER_ANNOTATION == 79);
		assertTrue(ASTNode.MEMBER_VALUE_PAIR == 80);
		assertTrue(ASTNode.ANNOTATION_TYPE_DECLARATION == 81);
		assertTrue(ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION == 82);
		assertTrue(ASTNode.MODIFIER == 83);
		
		// ensure that all constants are distinct, positive, and small
		// (this may seem paranoid, but this test did uncover a stupid bug!)
		int[] all= {
	      	  ASTNode.ANNOTATION_TYPE_DECLARATION,
			  ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION,
              ASTNode.ANONYMOUS_CLASS_DECLARATION,
              ASTNode.ARRAY_ACCESS,
              ASTNode.ARRAY_CREATION,
              ASTNode.ARRAY_INITIALIZER,
              ASTNode.ARRAY_TYPE,
              ASTNode.ASSERT_STATEMENT,
              ASTNode.ASSIGNMENT,
              ASTNode.BLOCK,
        	  ASTNode.BLOCK_COMMENT,
              ASTNode.BOOLEAN_LITERAL,
              ASTNode.BREAK_STATEMENT,
              ASTNode.CAST_EXPRESSION,
              ASTNode.CATCH_CLAUSE,
              ASTNode.CHARACTER_LITERAL,
              ASTNode.CLASS_INSTANCE_CREATION,
              ASTNode.COMPILATION_UNIT,
              ASTNode.CONDITIONAL_EXPRESSION,
              ASTNode.CONSTRUCTOR_INVOCATION,
              ASTNode.CONTINUE_STATEMENT,
              ASTNode.DO_STATEMENT,
              ASTNode.EMPTY_STATEMENT,
              ASTNode.ENHANCED_FOR_STATEMENT,
              ASTNode.ENUM_CONSTANT_DECLARATION,
              ASTNode.ENUM_DECLARATION,
              ASTNode.EXPRESSION_STATEMENT,
              ASTNode.FIELD_ACCESS,
              ASTNode.FIELD_DECLARATION,
              ASTNode.FOR_STATEMENT,
              ASTNode.IF_STATEMENT,
              ASTNode.IMPORT_DECLARATION,
              ASTNode.INFIX_EXPRESSION,
              ASTNode.INSTANCEOF_EXPRESSION,
              ASTNode.INITIALIZER,
              ASTNode.JAVADOC,
              ASTNode.LABELED_STATEMENT,
        	  ASTNode.LINE_COMMENT,
      		  ASTNode.MARKER_ANNOTATION,
        	  ASTNode.MEMBER_REF,
      		  ASTNode.MEMBER_VALUE_PAIR,
              ASTNode.METHOD_DECLARATION,
              ASTNode.METHOD_INVOCATION,
        	  ASTNode.METHOD_REF,
        	  ASTNode.METHOD_REF_PARAMETER,
      		  ASTNode.MODIFIER,              
			  ASTNode.NORMAL_ANNOTATION,
              ASTNode.NULL_LITERAL,
              ASTNode.NUMBER_LITERAL,
              ASTNode.PACKAGE_DECLARATION,
              ASTNode.PARAMETERIZED_TYPE,
              ASTNode.PARENTHESIZED_EXPRESSION,
              ASTNode.POSTFIX_EXPRESSION,
              ASTNode.PREFIX_EXPRESSION,
              ASTNode.PRIMITIVE_TYPE,
              ASTNode.QUALIFIED_NAME,
              ASTNode.QUALIFIED_TYPE,
              ASTNode.RETURN_STATEMENT,
              ASTNode.SIMPLE_NAME,
              ASTNode.SIMPLE_TYPE,
      		  ASTNode.SINGLE_MEMBER_ANNOTATION,
              ASTNode.SINGLE_VARIABLE_DECLARATION,
              ASTNode.STRING_LITERAL,
              ASTNode.SUPER_CONSTRUCTOR_INVOCATION,
              ASTNode.SUPER_FIELD_ACCESS,
              ASTNode.SUPER_METHOD_INVOCATION,
              ASTNode.SWITCH_CASE,
              ASTNode.SWITCH_STATEMENT,
              ASTNode.SYNCHRONIZED_STATEMENT,
        	  ASTNode.TAG_ELEMENT,
        	  ASTNode.TEXT_ELEMENT,
              ASTNode.THIS_EXPRESSION,
              ASTNode.THROW_STATEMENT,
              ASTNode.TRY_STATEMENT,
              ASTNode.TYPE_DECLARATION,
              ASTNode.TYPE_DECLARATION_STATEMENT,
              ASTNode.TYPE_LITERAL,
              ASTNode.TYPE_PARAMETER,
              ASTNode.VARIABLE_DECLARATION_EXPRESSION,
              ASTNode.VARIABLE_DECLARATION_FRAGMENT,
              ASTNode.VARIABLE_DECLARATION_STATEMENT,
              ASTNode.WHILE_STATEMENT,
              ASTNode.WILDCARD_TYPE,
		};
		int MIN = 1;
		int MAX = 100;
		Set s = new HashSet();
		for (int i=0; i<all.length; i++) {
			assertTrue(MIN <= all[i] && all[i] <= MAX);
			s.add(new Integer(all[i]));
		}
		assertTrue(s.size() == all.length);
		// ensure that Integers really do compare properly with equals
		assertTrue(new Integer(1).equals(new Integer(1)));
	}
}

