/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Test suite for <code>ASTMatcher</code> and <code>ASTNode.subtreeMatch</code>.
 */
public class ASTMatcherTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase {

	/** @deprecated using deprecated code */
	public static Test suite() {
		// TODO (frederic) use buildList + setAstLevel(init) instead...
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTMatcherTest.class.getName());

		Class c = ASTMatcherTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTMatcherTest(methods[i].getName(), AST.JLS2));
				suite.addTest(new ASTMatcherTest(methods[i].getName(), AST.JLS3));
			}
		}
		return suite;
	}

	AST ast;
	SimpleName N1;
	SimpleName N2;
	SimpleName N3;
	SimpleName N4;
	Expression E1;
	Expression E2;
	Type T1;
	String T1S;
	Type T2;
	String T2S;
	ParameterizedType PT1;
	String PT1S;
	Statement S1;
	Statement S2;
	Block B1;
	SingleVariableDeclaration V1;
	SingleVariableDeclaration V2;
	VariableDeclarationFragment W1;
	VariableDeclarationFragment W2;
	FieldDeclaration FD1;
	FieldDeclaration FD2;
	PackageDeclaration PD1;
	ImportDeclaration ID1;
	ImportDeclaration ID2;
	TypeDeclaration TD1;
	TypeDeclaration TD2;
	AnonymousClassDeclaration ACD1;
	Javadoc JD1;
	Javadoc JD2;
	String JD2S;
	TypeParameter TP1;
	String TP1S;
	TypeParameter TP2;
	String TP2S;
	TagElement TAG1;
	TagElement TAG2;
	TextElement TEXT1;
	MemberRef MBREF1;
	MethodRef MTHREF1;
	MethodRefParameter MPARM1;
	LineComment LC1;
	BlockComment BC1;
	MemberValuePair MVP1;
	MemberValuePair MVP2;
	MarkerAnnotation ANO1;
	SingleMemberAnnotation ANO2;
	Modifier MOD1;
	Modifier MOD2;
	EnumConstantDeclaration EC1;
	EnumConstantDeclaration EC2;

	final StringBuffer b = new StringBuffer();

	int API_LEVEL;

	public ASTMatcherTest(String name, int apiLevel) {
		super(name);
		this.API_LEVEL = apiLevel;
	}

	/**
	 * @deprecated (not really - just suppressing the warnings
	 * that come from testing Javadoc.getComment())
	 *
	 */
	protected void setUp() throws Exception {
		super.setUp();

		this.ast = AST.newAST(this.API_LEVEL);
		this.N1 = this.ast.newSimpleName("N"); //$NON-NLS-1$
		this.N2 = this.ast.newSimpleName("M"); //$NON-NLS-1$
		this.N3 = this.ast.newSimpleName("O"); //$NON-NLS-1$
		this.N4 = this.ast.newSimpleName("P"); //$NON-NLS-1$
		this.E1 = this.ast.newSimpleName("X"); //$NON-NLS-1$
		this.E2 = this.ast.newSimpleName("Y"); //$NON-NLS-1$
		this.T1 = this.ast.newSimpleType(this.ast.newSimpleName("Z")); //$NON-NLS-1$
		this.T1S = "(tS(nSZZnS)tS)"; //$NON-NLS-1$
		this.T2 = this.ast.newSimpleType(this.ast.newSimpleName("Y")); //$NON-NLS-1$
		this.T2S = "(tS(nSYYnS)tS)"; //$NON-NLS-1$
		this.S1 = this.ast.newContinueStatement();
		this.S2 = this.ast.newBreakStatement();
		this.B1 = this.ast.newBlock();
		this.V1 = this.ast.newSingleVariableDeclaration();
		this.V1.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		this.V1.setName(this.ast.newSimpleName("a")); //$NON-NLS-1$
		this.V2 = this.ast.newSingleVariableDeclaration();
		this.V2.setType(this.ast.newPrimitiveType(PrimitiveType.BYTE));
		this.V2.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		this.W1 = this.ast.newVariableDeclarationFragment();
		this.W1.setName(this.ast.newSimpleName("a")); //$NON-NLS-1$
		this.W2 = this.ast.newVariableDeclarationFragment();
		this.W2.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		{
			VariableDeclarationFragment temp = this.ast.newVariableDeclarationFragment();
			temp.setName(this.ast.newSimpleName("f")); //$NON-NLS-1$
			this.FD1 = this.ast.newFieldDeclaration(temp);
			this.FD1.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		}
		{
			VariableDeclarationFragment temp = this.ast.newVariableDeclarationFragment();
			temp.setName(this.ast.newSimpleName("g")); //$NON-NLS-1$
			this.FD2 = this.ast.newFieldDeclaration(temp);
			this.FD2.setType(this.ast.newPrimitiveType(PrimitiveType.CHAR));
		}
		this.PD1 = this.ast.newPackageDeclaration();
		this.PD1.setName(this.ast.newSimpleName("p")); //$NON-NLS-1$
		this.ID1 = this.ast.newImportDeclaration();
		this.ID1.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		this.ID2 = this.ast.newImportDeclaration();
		this.ID2.setName(this.ast.newSimpleName("j")); //$NON-NLS-1$
		this.TD1 = this.ast.newTypeDeclaration();
		this.TD1.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		this.TD2 = this.ast.newTypeDeclaration();
		this.TD2.setName(this.ast.newSimpleName("d")); //$NON-NLS-1$

		this.ACD1 = this.ast.newAnonymousClassDeclaration();

		this.JD1 = this.ast.newJavadoc();
		this.JD2 = this.ast.newJavadoc();
		if (this.ast.apiLevel() == AST.JLS2) {
			this.JD1.setComment("/**X*/"); //$NON-NLS-1$
			this.JD2.setComment("/**Y*/"); //$NON-NLS-1$
		}

		this.BC1 = this.ast.newBlockComment();

		this.TAG1 = this.ast.newTagElement();
		this.TAG1.setTagName("@foo"); //$NON-NLS-1$

		this.TAG2 = this.ast.newTagElement();
		this.TAG2.setTagName("@bar"); //$NON-NLS-1$

		this.TEXT1 = this.ast.newTextElement();
		this.TEXT1.setText("foo"); //$NON-NLS-1$

		this.MBREF1 = this.ast.newMemberRef();
		this.MBREF1.setName(this.ast.newSimpleName("p")); //$NON-NLS-1$

		this.MTHREF1 = this.ast.newMethodRef();
		this.MTHREF1.setName(this.ast.newSimpleName("p")); //$NON-NLS-1$

		this.MPARM1 = this.ast.newMethodRefParameter();
		this.MPARM1.setType(this.ast.newPrimitiveType(PrimitiveType.CHAR));

		if (this.ast.apiLevel() >= AST.JLS3) {
			this.PT1 = this.ast.newParameterizedType(this.ast.newSimpleType(this.ast.newSimpleName("Z"))); //$NON-NLS-1$
			this.PT1S = "[(tM[(tS[(nSZZnS)]tS)]tM)]"; //$NON-NLS-1$

			this.TP1 = this.ast.newTypeParameter();
			this.TP1.setName(this.ast.newSimpleName("x")); //$NON-NLS-1$
			this.TP1S = "[(tTP[(nSxxnS)]tTP)]"; //$NON-NLS-1$

			this.TP2 = this.ast.newTypeParameter();
			this.TP2.setName(this.ast.newSimpleName("y")); //$NON-NLS-1$
			this.TP2S = "[(tTP[(nSyynS)]tTP)]"; //$NON-NLS-1$
			this.LC1 = this.ast.newLineComment();

			this.MVP1 = this.ast.newMemberValuePair();
			this.MVP1.setName(this.ast.newSimpleName("x")); //$NON-NLS-1$
			this.MVP1.setValue(this.ast.newSimpleName("y")); //$NON-NLS-1$

			this.MVP2 = this.ast.newMemberValuePair();
			this.MVP2.setName(this.ast.newSimpleName("a")); //$NON-NLS-1$
			this.MVP2.setValue(this.ast.newSimpleName("b")); //$NON-NLS-1$

			this.ANO1 = this.ast.newMarkerAnnotation();
			this.ANO1.setTypeName(this.ast.newSimpleName("p")); //$NON-NLS-1$

			this.ANO2 = this.ast.newSingleMemberAnnotation();
			this.ANO2.setTypeName(this.ast.newSimpleName("q")); //$NON-NLS-1$
			this.ANO2.setValue(this.ast.newSimpleName("v")); //$NON-NLS-1$

			this.MOD1 = this.ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			this.MOD2 = this.ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);

			this.EC1 = this.ast.newEnumConstantDeclaration();
			this.EC1.setName(this.ast.newSimpleName("F")); //$NON-NLS-1$
			this.EC2 = this.ast.newEnumConstantDeclaration();
			this.EC2.setName(this.ast.newSimpleName("G")); //$NON-NLS-1$
		}

	}

	protected void tearDown() throws Exception {
		this.ast = null;
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
	 * An ASTMatcher that simply records the arguments it is passed,
	 * immediately returns a pre-ordained answer, and counts how many
	 * times it is called.
	 */
	class TestMatcher extends ASTMatcher {

		public Object receiverNode;
		public Object otherNode;
		public boolean result;
		public boolean superMatch;
		public boolean superMatchResult;
		public int matchCalls = 0;

		TestMatcher() {
			this(false);
		}

		TestMatcher(boolean visitDocTags) {
			super(visitDocTags);
		}

		boolean standardBody(ASTNode receiver, Object other, boolean matchResult) {
			this.matchCalls++;
			this.receiverNode = receiver;
			this.otherNode = other;
			this.superMatchResult = matchResult;
			if (this.superMatch) {
				return this.superMatchResult;
			}
			return this.result;
		}

		public boolean match(AnnotationTypeDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(AnnotationTypeMemberDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(AnonymousClassDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayAccess node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayCreation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayInitializer node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayType node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(AssertStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(Assignment node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(Block node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(BlockComment node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(BooleanLiteral node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(BreakStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(CastExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(CatchClause node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(CharacterLiteral node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ClassInstanceCreation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(CompilationUnit node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ConditionalExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ConstructorInvocation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ContinueStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(DoStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(EmptyStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(EnhancedForStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(EnumConstantDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(EnumDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ExpressionStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(FieldAccess node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(FieldDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ForStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(IfStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ImportDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(InfixExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(Initializer node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(Javadoc node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(LabeledStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(LineComment node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MarkerAnnotation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MemberRef node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MemberValuePair node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MethodDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MethodInvocation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MethodRef node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(MethodRefParameter node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(Modifier node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(NormalAnnotation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(NullLiteral node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(NumberLiteral node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(PackageDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ParameterizedType node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ParenthesizedExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(PostfixExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(PrefixExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(PrimitiveType node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(QualifiedName node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(QualifiedType node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ReturnStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SimpleName node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SimpleType node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SingleMemberAnnotation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SingleVariableDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(StringLiteral node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SuperConstructorInvocation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SuperFieldAccess node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SuperMethodInvocation node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SwitchCase node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SwitchStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(SynchronizedStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TagElement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TextElement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ThisExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(ThrowStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TryStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeDeclaration node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeDeclarationStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeLiteral node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeParameter node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(VariableDeclarationExpression node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(VariableDeclarationFragment node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(VariableDeclarationStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(WhileStatement node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
		public boolean match(WildcardType node, Object other) {
			return standardBody(node, other, this.superMatch ? super.match(node, other) : false);
		}
	}

	/**
	 * AST node visitor that counts the nodes visited.
	 */
	static class NodeCounter extends ASTVisitor {
		public NodeCounter(boolean visitDocTags) {
			super(visitDocTags);
		}

		public int count = 0;

		/* (no javadoc for this method)
		 * Method declared on ASTVisitor.
		 */
		public void preVisit(ASTNode node) {
			this.count++;
		}

	}

	/**
	 * Returns the number of AST nodes in the given subtree.
	 *
	 * @param node the root of the subtree
	 * @param visitDocTags true if doc tags should be visited
	 * @return the number of nodes (always positive)
	 */
	static int nodeCount(ASTNode node, boolean visitDocTags) {
		NodeCounter c = new NodeCounter(visitDocTags);
		node.accept(c);
		return c.count;
	}

	/**
	 * Checks that the ASTNode.subtreeMatch mechanism is working
	 * for a node of a given type.
	 */
	void basicMatch(ASTNode node) {
		TestMatcher[] m = {
			new TestMatcher(),
			new TestMatcher(true),
			new TestMatcher(false)};
		for (int i = 0; i < m.length; i++) {
			// check that matcher was called with right arguments
			// and that matches succeed
			TestMatcher m1 = m[i];
			Object o1 = new Object();
			m1.result = true;
			boolean result = node.subtreeMatch(m1, o1);
			assertTrue(m1.matchCalls == 1);
			assertTrue(m1.receiverNode == node);
			assertTrue(m1.otherNode == o1);
			assertTrue(result == true);
		}

		m = new TestMatcher[] {
							new TestMatcher(),
							new TestMatcher(true),
							new TestMatcher(false)};
		for (int i = 0; i < m.length; i++) {
			// check that matcher was called with right arguments
			// and that non-matches fail
			TestMatcher m1 = m[i];
			Object o1 = new Object();
			m1.result = false;
			boolean result = node.subtreeMatch(m1, o1);
			assertTrue(m1.matchCalls == 1);
			assertTrue(m1.receiverNode == node);
			assertTrue(m1.otherNode == o1);
			assertTrue(result == false);
		}

		// check that ASTMatcher() default implementations delegate
		{
			int count = nodeCount(node, false); // ignore doc tags
			TestMatcher m1 = new TestMatcher();
			m1.superMatch = true;
			boolean result = node.subtreeMatch(m1, node);
			assertTrue(m1.matchCalls == count);
			assertTrue(result == true);
		}

		// check that ASTMatcher(false) default implementations delegate
		{
			int count = nodeCount(node, false); // ignore doc tags
			TestMatcher m1 = new TestMatcher(false);
			m1.superMatch = true;
			boolean result = node.subtreeMatch(m1, node);
			assertTrue(m1.matchCalls == count);
			assertTrue(result == true);
		}

		// check that ASTMatcher(true) default implementations delegate
		{
			int count = nodeCount(node, true); // include doc tags
			TestMatcher m1 = new TestMatcher(true);
			m1.superMatch = true;
			boolean result = node.subtreeMatch(m1, node);
			assertTrue(m1.matchCalls == count);
			assertTrue(result == true);
		}

	}

	// NAMES
	public void testSimpleName() {
		Name x1 = this.ast.newName(new String[]{"Z"}); //$NON-NLS-1$
		basicMatch(x1);
	}

	public void testQualifiedName() {
		Name x1 = this.ast.newName(new String[]{"X", "Y"}); //$NON-NLS-1$ //$NON-NLS-2$
		basicMatch(x1);
	}


	// TYPES
	public void testPrimitiveType() {
		Type x1 = this.ast.newPrimitiveType(PrimitiveType.CHAR);
		basicMatch(x1);
	}

	public void testSimpleType() {
		Type x1 = this.ast.newSimpleType(this.N1);
		basicMatch(x1);
	}

	public void testArrayType() {
		Type x0 = this.ast.newPrimitiveType(PrimitiveType.CHAR);
		Type x1 = this.ast.newArrayType(x0);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testParameterizedType() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		ParameterizedType x1 = this.ast.newParameterizedType(this.ast.newSimpleType(this.ast.newSimpleName("X"))); //$NON-NLS-1$
		x1.typeArguments().add(this.T1);
		x1.typeArguments().add(this.T2);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testQualifiedType() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		Type x1 = this.ast.newQualifiedType(this.T1, this.N1);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testWildcardType() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		WildcardType x1 = this.ast.newWildcardType();
		x1.setBound(this.T1, true);
		basicMatch(x1);
	}

	// EXPRESSIONS and STATEMENTS

	public void testAnonymousClassDeclaration() {
		AnonymousClassDeclaration x1 = this.ast.newAnonymousClassDeclaration();
		x1.bodyDeclarations().add(this.FD1);
		x1.bodyDeclarations().add(this.FD2);
		basicMatch(x1);
	}
	public void testArrayAccess() {
		ArrayAccess x1 = this.ast.newArrayAccess();
		x1.setArray(this.E1);
		x1.setIndex(this.E2);
		basicMatch(x1);
	}
	public void testArrayCreation() {
		ArrayCreation x1 = this.ast.newArrayCreation();
		x1.setType(this.ast.newArrayType(this.T1));
		x1.dimensions().add(this.E1);
		x1.dimensions().add(this.E2);
		x1.setInitializer(this.ast.newArrayInitializer());
		basicMatch(x1);
	}
	public void testArrayInitializer() {
		ArrayInitializer x1 = this.ast.newArrayInitializer();
		x1.expressions().add(this.E1);
		x1.expressions().add(this.E2);
		basicMatch(x1);
	}
	public void testAssertStatement() {
		AssertStatement x1 = this.ast.newAssertStatement();
		x1.setExpression(this.E1);
		x1.setMessage(this.E2);
		basicMatch(x1);
	}
	public void testAssignment() {
		Assignment x1 = this.ast.newAssignment();
		x1.setLeftHandSide(this.E1);
		x1.setRightHandSide(this.E2);
		basicMatch(x1);
	}
	public void testBlock() {
		Block x1 = this.ast.newBlock();
		x1.statements().add(this.S1);
		x1.statements().add(this.S2);
		basicMatch(x1);
	}

	public void testBlockComment() {
		BlockComment x1 = this.ast.newBlockComment();
		basicMatch(x1);
	}

	public void testBooleanLiteral() {
		BooleanLiteral x1 = this.ast.newBooleanLiteral(true);
		basicMatch(x1);
	}
	public void testBreakStatement() {
		BreakStatement x1 = this.ast.newBreakStatement();
		x1.setLabel(this.N1);
		basicMatch(x1);
	}
	public void testCastExpression() {
		CastExpression x1 = this.ast.newCastExpression();
		x1.setType(this.T1);
		x1.setExpression(this.E1);
		basicMatch(x1);
	}
	public void testCatchClause() {
		CatchClause x1 = this.ast.newCatchClause();
		x1.setException(this.V1);
		x1.setBody(this.B1);
		basicMatch(x1);
	}
	public void testCharacterLiteral() {
		CharacterLiteral x1 = this.ast.newCharacterLiteral();
		x1.setCharValue('q');
		basicMatch(x1);
	}
	/** @deprecated using deprecated code */
	public void testClassInstanceCreation() {
		ClassInstanceCreation x1 = this.ast.newClassInstanceCreation();
		x1.setExpression(this.E1);
		if (this.ast.apiLevel() == AST.JLS2) {
			x1.setName(this.N1);
		} else {
			x1.typeArguments().add(this.PT1);
			x1.setType(this.T1);
		}
		x1.setAnonymousClassDeclaration(this.ACD1);
		basicMatch(x1);
	}
	public void testCompilationUnit() {
		CompilationUnit x1 = this.ast.newCompilationUnit();
		x1.setPackage(this.PD1);
		x1.imports().add(this.ID1);
		x1.imports().add(this.ID2);
		x1.types().add(this.TD1);
		x1.types().add(this.TD2);
		basicMatch(x1);
	}
	public void testConditionalExpression() {
		ConditionalExpression x1 = this.ast.newConditionalExpression();
		x1.setExpression(this.E1);
		x1.setThenExpression(this.E2);
		x1.setElseExpression(this.N1);
		basicMatch(x1);
	}
	public void testConstructorInvocation() {
		ConstructorInvocation x1 = this.ast.newConstructorInvocation();
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		basicMatch(x1);
	}
	public void testContinueStatement() {
		ContinueStatement x1 = this.ast.newContinueStatement();
		x1.setLabel(this.N1);
		basicMatch(x1);
	}
	public void testDoStatement() {
		DoStatement x1 = this.ast.newDoStatement();
		x1.setExpression(this.E1);
		x1.setBody(this.S1);
		basicMatch(x1);
	}
	public void testEmptyStatement() {
		EmptyStatement x1 = this.ast.newEmptyStatement();
		basicMatch(x1);
	}
	/** @deprecated using deprecated code */
	public void testEnhancedForStatement() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		EnhancedForStatement x1 = this.ast.newEnhancedForStatement();
		x1.setParameter(this.V1);
		x1.setExpression(this.E1);
		x1.setBody(this.S1);
		basicMatch(x1);
	}
	/** @deprecated Only to suppress warnings for refs to bodyDeclarations. */
	// TODO (jeem) - remove deprecation after 3.1 M4
	public void testEnumConstantDeclaration() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		EnumConstantDeclaration x1 = this.ast.newEnumConstantDeclaration();
		x1.setJavadoc(this.JD1);
		x1.modifiers().add(this.MOD1);
		x1.modifiers().add(this.MOD2);
		x1.setName(this.N1);
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		x1.setAnonymousClassDeclaration(this.ACD1);
		basicMatch(x1);
	}
	/** @deprecated using deprecated code */
	public void testEnumDeclaration() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		EnumDeclaration x1 = this.ast.newEnumDeclaration();
		x1.setJavadoc(this.JD1);
		x1.modifiers().add(this.MOD1);
		x1.modifiers().add(this.MOD2);
		x1.setName(this.N1);
		x1.superInterfaceTypes().add(this.T1);
		x1.superInterfaceTypes().add(this.T2);
		x1.enumConstants().add(this.EC1);
		x1.enumConstants().add(this.EC2);
		x1.bodyDeclarations().add(this.FD1);
		x1.bodyDeclarations().add(this.FD2);
		basicMatch(x1);
	}
	public void testExpressionStatement() {
		ExpressionStatement x1 = this.ast.newExpressionStatement(this.E1);
		basicMatch(x1);
	}
	public void testFieldAccess() {
		FieldAccess x1 = this.ast.newFieldAccess();
		x1.setExpression(this.E1);
		x1.setName(this.N1);
		basicMatch(x1);
	}
	public void testFieldDeclaration() {
		FieldDeclaration x1 = this.ast.newFieldDeclaration(this.W1);
		x1.setJavadoc(this.JD1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.fragments().add(this.W2);
		basicMatch(x1);
	}
	public void testForStatement() {
		ForStatement x1 = this.ast.newForStatement();
		x1.initializers().add(this.E1);
		x1.initializers().add(this.E2);
		x1.setExpression(this.N1);
		x1.updaters().add(this.N2);
		x1.updaters().add(this.N3);
		x1.setBody(this.S1);
		basicMatch(x1);
	}
	public void testIfStatement() {
		IfStatement x1 = this.ast.newIfStatement();
		x1.setExpression(this.E1);
		x1.setThenStatement(this.S1);
		x1.setElseStatement(this.S2);
		basicMatch(x1);
	}
	public void testImportDeclaration() {
		ImportDeclaration x1 = this.ast.newImportDeclaration();
		x1.setName(this.N1);
		basicMatch(x1);
	}
	public void testInfixExpression() {
		InfixExpression x1 = this.ast.newInfixExpression();
		x1.setOperator(InfixExpression.Operator.PLUS);
		x1.setLeftOperand(this.E1);
		x1.setRightOperand(this.E2);
		x1.extendedOperands().add(this.N1);
		x1.extendedOperands().add(this.N2);
		basicMatch(x1);
	}
	public void testInitializer() {
		Initializer x1 = this.ast.newInitializer();
		x1.setJavadoc(this.JD1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setBody(this.B1);
		basicMatch(x1);
	}
	/**
	 * @deprecated (not really - just suppressing the warnings
	 * that come from testing Javadoc.getComment())
	 *
	 */
	public void testJavadoc() {
		Javadoc x1 = this.ast.newJavadoc();
		if (this.ast.apiLevel() == AST.JLS2) {
			x1.setComment("/**?*/"); //$NON-NLS-1$
		}
		x1.tags().add(this.TAG1);
		x1.tags().add(this.TAG2);
		basicMatch(x1);
	}

	public void testLabeledStatement() {
		LabeledStatement x1 = this.ast.newLabeledStatement();
		x1.setLabel(this.N1);
		x1.setBody(this.S1);
		basicMatch(x1);
	}

	public void testLineComment() {
		LineComment x1 = this.ast.newLineComment();
		basicMatch(x1);
	}

	public void testMemberRef() {
		MemberRef x1 = this.ast.newMemberRef();
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testMethodDeclaration() {
		MethodDeclaration x1 = this.ast.newMethodDeclaration();
		x1.setJavadoc(this.JD1);
		if (this.ast.apiLevel() == AST.JLS2) {
			x1.setReturnType(this.T1);
		} else {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
			x1.typeParameters().add(this.TP1);
			x1.typeParameters().add(this.TP2);
			x1.setReturnType2(this.T1);
		}
		x1.setName(this.N1);
		x1.parameters().add(this.V1);
		x1.parameters().add(this.V2);
		x1.thrownExceptions().add(this.N2);
		x1.thrownExceptions().add(this.N3);
		x1.setBody(this.B1);
		basicMatch(x1);
	}
	public void testMethodInvocation() {
		MethodInvocation x1 = this.ast.newMethodInvocation();
		x1.setExpression(this.N1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.setName(this.N2);
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		basicMatch(x1);
	}

	public void testMethodRef() {
		MethodRef x1 = this.ast.newMethodRef();
		basicMatch(x1);
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		x1.parameters().add(this.MPARM1);
	}
	public void testMethodRefParameter() {
		MethodRefParameter x1 = this.ast.newMethodRefParameter();
		x1.setType(this.T1);
		x1.setName(this.N1);
		basicMatch(x1);
	}

	public void testNullLiteral() {
		NullLiteral x1 = this.ast.newNullLiteral();
		basicMatch(x1);
	}
	public void testNumberLiteral() {
		NumberLiteral x1 = this.ast.newNumberLiteral("1.0"); //$NON-NLS-1$
		basicMatch(x1);
	}
	public void testPackageDeclaration() {
		PackageDeclaration x1 = this.ast.newPackageDeclaration();
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.setJavadoc(this.JD1);
			x1.annotations().add(this.ANO1);
			x1.annotations().add(this.ANO2);
		}
		basicMatch(x1);
	}
	public void testParenthesizedExpression() {
		ParenthesizedExpression x1 = this.ast.newParenthesizedExpression();
		basicMatch(x1);
	}
	public void testPostfixExpression() {
		PostfixExpression x1 = this.ast.newPostfixExpression();
		x1.setOperand(this.E1);
		x1.setOperator(PostfixExpression.Operator.INCREMENT);
		basicMatch(x1);
	}
	public void testPrefixExpression() {
		PrefixExpression x1 = this.ast.newPrefixExpression();
		x1.setOperand(this.E1);
		x1.setOperator(PrefixExpression.Operator.INCREMENT);
		basicMatch(x1);
	}
	public void testReturnStatement() {
		ReturnStatement x1 = this.ast.newReturnStatement();
		x1.setExpression(this.E1);
		basicMatch(x1);
	}
	public void testSingleVariableDeclaration() {
		SingleVariableDeclaration x1 = this.ast.newSingleVariableDeclaration();
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.setName(this.N1);
		x1.setInitializer(this.E1);
		basicMatch(x1);
	}
	public void testStringLiteral() {
		StringLiteral x1 = this.ast.newStringLiteral();
		x1.setLiteralValue("H"); //$NON-NLS-1$
		basicMatch(x1);
	}
	public void testSuperConstructorInvocation() {
		SuperConstructorInvocation x1 = this.ast.newSuperConstructorInvocation();
		x1.setExpression(this.N1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		basicMatch(x1);
	}
	public void testSuperFieldAccess() {
		SuperFieldAccess x1 = this.ast.newSuperFieldAccess();
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		basicMatch(x1);
	}
	public void testSuperMethodInvocation() {
		SuperMethodInvocation x1 = this.ast.newSuperMethodInvocation();
		x1.setQualifier(this.N1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.setName(this.N2);
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		basicMatch(x1);
	}
	public void testSwitchCase() {
		SwitchCase x1 = this.ast.newSwitchCase();
		x1.setExpression(this.E1);
		basicMatch(x1);
	}
	public void testSwitchStatement() {
		SwitchStatement x1 = this.ast.newSwitchStatement();
		x1.setExpression(this.E1);
		x1.statements().add(this.S1);
		x1.statements().add(this.S2);
		basicMatch(x1);
	}
	public void testSynchronizedStatement() {
		SynchronizedStatement x1 = this.ast.newSynchronizedStatement();
		x1.setExpression(this.E1);
		x1.setBody(this.B1);
		basicMatch(x1);
	}

	public void testTagElement() {
		TagElement x1 = this.ast.newTagElement();
		x1.setTagName("@foo"); //$NON-NLS-1$
		x1.fragments().add(this.TAG1);
		x1.fragments().add(this.TEXT1);
		x1.fragments().add(this.N1);
		x1.fragments().add(this.MTHREF1);
		basicMatch(x1);
	}
	public void testTextElement() {
		TextElement x1 = this.ast.newTextElement();
		x1.setText("foo"); //$NON-NLS-1$
		basicMatch(x1);
	}

	public void testThisExpression() {
		ThisExpression x1 = this.ast.newThisExpression();
		x1.setQualifier(this.N1);
		basicMatch(x1);
	}
	public void testThrowStatement() {
		ThrowStatement x1 = this.ast.newThrowStatement();
		x1.setExpression(this.E1);
		basicMatch(x1);
	}
	public void testTryStatement() {
		TryStatement x1 = this.ast.newTryStatement();
		x1.setBody(this.B1);
		CatchClause c1 = this.ast.newCatchClause();
		c1.setException(this.V1);
		c1.setBody(this.ast.newBlock());
		x1.catchClauses().add(c1);
		CatchClause c2 = this.ast.newCatchClause();
		c2.setException(this.V2);
		c2.setBody(this.ast.newBlock());
		x1.catchClauses().add(c2);
		x1.setFinally(this.ast.newBlock());
		basicMatch(x1);
	}
	/** @deprecated using deprecated code */
	public void testTypeDeclaration() {
		TypeDeclaration x1 = this.ast.newTypeDeclaration();
		x1.setJavadoc(this.JD1);
		x1.setName(this.N1);
		if (this.ast.apiLevel() == AST.JLS2) {
			x1.setSuperclass(this.N2);
			x1.superInterfaces().add(this.N3);
			x1.superInterfaces().add(this.N4);
		} else {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
			x1.typeParameters().add(this.TP1);
			x1.typeParameters().add(this.TP2);
			x1.setSuperclassType(this.PT1);
			x1.superInterfaceTypes().add(this.T1);
			x1.superInterfaceTypes().add(this.T2);
		}
		x1.bodyDeclarations().add(this.FD1);
		x1.bodyDeclarations().add(this.FD2);
		basicMatch(x1);
	}
	public void testTypeDeclarationStatement() {
		TypeDeclarationStatement x1 = this.ast.newTypeDeclarationStatement(this.TD1);
		basicMatch(x1);
	}
	public void testTypeLiteral() {
		TypeLiteral x1 = this.ast.newTypeLiteral();
		x1.setType(this.T1);
		basicMatch(x1);
	}
	/** @deprecated using deprecated code */
	public void testTypeParameter() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		TypeParameter x1 = this.ast.newTypeParameter();
		x1.setName(this.N1);
		x1.typeBounds().add(this.T1);
		x1.typeBounds().add(this.T2);
		basicMatch(x1);
	}
	public void testVariableDeclarationFragment() {
		VariableDeclarationFragment x1 = this.ast.newVariableDeclarationFragment();
		x1.setName(this.N1);
		x1.setInitializer(this.E1);
		basicMatch(x1);
	}
	public void testVariableDeclarationExpression() {
		VariableDeclarationExpression x1 = this.ast.newVariableDeclarationExpression(this.W1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.fragments().add(this.W2);
		basicMatch(x1);
	}
	public void testVariableDeclarationStatement() {
		VariableDeclarationStatement x1 = this.ast.newVariableDeclarationStatement(this.W1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.fragments().add(this.W2);
		basicMatch(x1);
	}
	public void testWhileStatement() {
		WhileStatement x1 = this.ast.newWhileStatement();
		x1.setExpression(this.E1);
		x1.setBody(this.S1);
		basicMatch(x1);
	}

	// annotation-related
	/** @deprecated using deprecated code */
	public void testAnnotationTypeDeclaration() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		AnnotationTypeDeclaration x1 = this.ast.newAnnotationTypeDeclaration();
		x1.setJavadoc(this.JD1);
		x1.modifiers().add(this.MOD1);
		x1.modifiers().add(this.MOD2);
		x1.setName(this.N1);
		x1.bodyDeclarations().add(this.FD1);
		x1.bodyDeclarations().add(this.FD2);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testAnnotationTypeMemberDeclaration() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		AnnotationTypeMemberDeclaration x1 = this.ast.newAnnotationTypeMemberDeclaration();
		x1.setJavadoc(this.JD1);
		x1.modifiers().add(this.MOD1);
		x1.modifiers().add(this.MOD2);
		x1.setType(this.T1);
		x1.setName(this.N1);
		x1.setDefault(this.E1);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testNormalAnnotation() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		NormalAnnotation x1 = this.ast.newNormalAnnotation();
		x1.setTypeName(this.N1);
		x1.values().add(this.MVP1);
		x1.values().add(this.MVP2);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testMarkerAnnotation() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		MarkerAnnotation x1 = this.ast.newMarkerAnnotation();
		x1.setTypeName(this.N1);
		basicMatch(x1);
	}

	/** @deprecated using deprecated code */
	public void testSingleMemberAnnotation() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		SingleMemberAnnotation x1 = this.ast.newSingleMemberAnnotation();
		x1.setTypeName(this.N1);
		x1.setValue(this.E1);
		basicMatch(x1);
	}

}
