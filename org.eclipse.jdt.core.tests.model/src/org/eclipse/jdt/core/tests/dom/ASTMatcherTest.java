/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial test suite for AST API
 ******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Test suite for <code>ASTMatcher</code> and <code>ASTNode.subtreeMatch</code>.
 */
public class ASTMatcherTest extends TestCase { 

	public static Test suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTMatcherTest.class.getName());
		
		Class c = ASTMatcherTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) {
				suite.addTest(new ASTMatcherTest(methods[i].getName()));
			}
		}
		return suite;
	}	
	
	AST ast;
	SimpleName N1;
	String N1S;
	SimpleName N2;
	String N2S;
	SimpleName N3;
	String N3S;
	Expression E1;
	String E1S;
	Expression E2;
	String E2S;
	Type T1;
	String T1S;
	Statement S1;
	String S1S;
	Statement S2;
	Block B1;
	String B1S;
	String S2S;
	SingleVariableDeclaration V1;
	String V1S;
	SingleVariableDeclaration V2;
	String V2S;
	VariableDeclarationFragment W1;
	String W1S;
	VariableDeclarationFragment W2;
	String W2S;
	FieldDeclaration FD1;
	String FD1S;
	FieldDeclaration FD2;
	String FD2S;
	PackageDeclaration PD1;
	String PD1S;
	ImportDeclaration ID1;
	String ID1S;
	ImportDeclaration ID2;
	String ID2S;
	TypeDeclaration TD1;
	String TD1S;
	TypeDeclaration TD2;
	String TD2S;
	AnonymousClassDeclaration ACD1;
	String ACD1S;
	Javadoc JD1;
	String JD1S;
	Javadoc JD2;
	String JD2S;
	
	final StringBuffer b = new StringBuffer();
	
	public ASTMatcherTest(String name) {
		super(name);
	}
	
	protected void setUp() {
		ast = new AST();
		N1 = ast.newSimpleName("N");
		N1S = "(nSNNnS)";
		N2 = ast.newSimpleName("M");
		N2S = "(nSMMnS)";
		N3 = ast.newSimpleName("O");
		N3S = "(nSOOnS)";
		E1 = ast.newSimpleName("X");
		E1S = "(nSXXnS)";
		E2 = ast.newSimpleName("Y");
		E2S = "(nSYYnS)";
		T1 = ast.newSimpleType(ast.newSimpleName("Z"));
		T1S = "(tS(nSZZnS)tS)";
		S1 = ast.newContinueStatement();
		S1S = "(sCNsCN)";
		S2 = ast.newBreakStatement();
		S2S = "(sBRsBR)";
		B1 = ast.newBlock();
		B1S = "(sBsB)";
		V1 = ast.newSingleVariableDeclaration();
		V1.setType(ast.newPrimitiveType(PrimitiveType.INT));
		V1.setName(ast.newSimpleName("a"));
		V1S = "(VD(tPintinttP)(nSaanS)VD)";
		V2 = ast.newSingleVariableDeclaration();
		V2.setType(ast.newPrimitiveType(PrimitiveType.BYTE));
		V2.setName(ast.newSimpleName("b"));
		V2S = "(VD(tPbytebytetP)(nSbbnS)VD)";
		W1 = ast.newVariableDeclarationFragment();
		W1.setName(ast.newSimpleName("a"));
		W1S = "(VS(nSaanS)VS)";
		W2 = ast.newVariableDeclarationFragment();
		W2.setName(ast.newSimpleName("b"));
		W2S = "(VS(nSbbnS)VS)";
		{
			VariableDeclarationFragment temp = ast.newVariableDeclarationFragment();
			temp.setName(ast.newSimpleName("f"));
			FD1 = ast.newFieldDeclaration(temp);
			FD1.setType(ast.newPrimitiveType(PrimitiveType.INT));
			FD1S = "(FD(tPintinttP)(VS(nSffnS)VS)FD)";
		}
		{
			VariableDeclarationFragment temp = ast.newVariableDeclarationFragment();
			temp.setName(ast.newSimpleName("g"));
			FD2 = ast.newFieldDeclaration(temp);
			FD2.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
			FD2S = "(FD(tPcharchartP)(VS(nSggnS)VS)FD)";
		}
		PD1 = ast.newPackageDeclaration();
		PD1.setName(ast.newSimpleName("p"));
		PD1S = "(PD(nSppnS)PD)";
		ID1 = ast.newImportDeclaration();
		ID1.setName(ast.newSimpleName("i"));
		ID1S = "(ID(nSiinS)ID)";
		ID2 = ast.newImportDeclaration();
		ID2.setName(ast.newSimpleName("j"));
		ID2S = "(ID(nSjjnS)ID)";
		TD1 = ast.newTypeDeclaration();
		TD1.setName(ast.newSimpleName("c"));
		TD1S = "(TD(nSccnS)TD)";
		TD2 = ast.newTypeDeclaration();
		TD2.setName(ast.newSimpleName("d"));
		TD2S = "(TD(nSddnS)TD)";
		
		ACD1 = ast.newAnonymousClassDeclaration();
		ACD1S = "(ACDACD)";
		
		JD1 = ast.newJavadoc();
		JD1.setComment("/**X*/");
		JD1S = "(JD/**X*//**X*/JD)";
		JD2 = ast.newJavadoc();
		JD2.setComment("/**Y*/");
		JD2S = "(JD/**Y*//**Y*/JD)";

	}
	
	protected void tearDown() {
		ast = null;
	}
	
	
	/**
	 * An ASTMatcher that simply records the arguments it is passed,
	 * immediately returns a pre-ordained answer, and counts how many
	 * times it is called.
	 */
	class TestMatcher extends ASTMatcher {

		public Object receiver;
		public Object other;
		public boolean result;
		public boolean superMatch;
		public boolean superMatchResult;
		public int matchCalls = 0;

		TestMatcher() {
		}

		boolean standardBody(ASTNode receiver, Object other, boolean superMatchResult) {
			matchCalls++;
			this.receiver = receiver;
			this.other = other;
			this.superMatchResult = superMatchResult;
			if (superMatch) {
				return this.superMatchResult;
			} else {
				return this.result;
			}
		}

		public boolean match(AnonymousClassDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayAccess node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayCreation node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayInitializer node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ArrayType node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(AssertStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(Assignment node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(Block node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(BooleanLiteral node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(BreakStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(CastExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(CatchClause node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(CharacterLiteral node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ClassInstanceCreation node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(CompilationUnit node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ConditionalExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ConstructorInvocation node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ContinueStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(DoStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(EmptyStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ExpressionStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(FieldAccess node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(FieldDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ForStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(IfStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ImportDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(InfixExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(Initializer node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(Javadoc node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(LabeledStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(MethodDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(MethodInvocation node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(NullLiteral node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(NumberLiteral node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(PackageDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ParenthesizedExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(PostfixExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(PrefixExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(PrimitiveType node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(QualifiedName node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ReturnStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SimpleName node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SimpleType node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SingleVariableDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(StringLiteral node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SuperConstructorInvocation node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SuperFieldAccess node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SuperMethodInvocation node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SwitchCase node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SwitchStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(SynchronizedStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ThisExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(ThrowStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(TryStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeDeclaration node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeDeclarationStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(TypeLiteral node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(VariableDeclarationExpression node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(VariableDeclarationFragment node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(VariableDeclarationStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
		public boolean match(WhileStatement node, Object other) {
			return standardBody(node, other, superMatch ? super.match(node, other) : false);
		}
	}
	
	/**
	 * AST node visitor that counts the nodes visited.
	 */
	static class NodeCounter extends ASTVisitor {
		public int count = 0;

		/* (no javadoc for this method)
		 * Method declared on ASTVisitor.
		 */
		public void preVisit(ASTNode node) {
			count++;
		}

	}
	
	/**
	 * Returns the number of AST nodes in the given subtree.
	 * 
	 * @param node the root of the subtree
	 * @return the number of nodes (always positive)
	 */
	static int nodeCount(ASTNode node) {
		NodeCounter c = new NodeCounter();
		node.accept(c);
		return c.count;
	}		
	
	/**
	 * Checks that the ASTNode.subtreeMatch mechanism is working
	 * for a node of a given type. 
	 */
	void basicMatch(ASTNode node) {
		int count = nodeCount(node);
		
		// check that matcher was called with right arguments
		// and that matches succeed
		TestMatcher m1 = new TestMatcher();
		Object o1 = new Object();
		m1.result = true;
		boolean result = node.subtreeMatch(m1, o1);
		assertTrue(m1.matchCalls == 1);
		assertTrue(m1.receiver == node);
		assertTrue(m1.other == o1);
		assertTrue(result == true);
		
		// check that matcher was called with right arguments
		// and that non-matches fail
		m1 = new TestMatcher();
		o1 = new Object();
		m1.result = false;
		result = node.subtreeMatch(m1, o1);
		assertTrue(m1.matchCalls == 1);
		assertTrue(m1.receiver == node);
		assertTrue(m1.other == o1);
		assertTrue(result == false);
		
		// check that ASTMatcher default implementations delegate
		m1 = new TestMatcher();
		m1.superMatch = true;
		result = node.subtreeMatch(m1, node);
		assertTrue(m1.matchCalls == count);
		assertTrue(result == true);
		
	}

	// NAMES
	public void testSimpleName() {
		Name x1 = ast.newName(new String[]{"Z"});
		basicMatch(x1);
	}

	public void testQualifiedName() {
		Name x1 = ast.newName(new String[]{"X", "Y"});
		basicMatch(x1);
	}

	
	// TYPES
	public void testPrimitiveType() {
		Type x1 = ast.newPrimitiveType(PrimitiveType.CHAR);
		basicMatch(x1);
	}

	public void testSimpleType() {
		Type x1 = ast.newSimpleType(ast.newName(new String[]{"Z"}));
		basicMatch(x1);
	}

	public void testArrayType() {
		Type x0 = ast.newPrimitiveType(PrimitiveType.CHAR);
		Type x1 = ast.newArrayType(x0);
		basicMatch(x1);
	}

	// EXPRESSIONS and STATEMENTS

	public void testAnonymousClassDeclaration() {
		AnonymousClassDeclaration x1 = ast.newAnonymousClassDeclaration();
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		basicMatch(x1);
	}
	public void testArrayAccess() {
		ArrayAccess x1 = ast.newArrayAccess();
		x1.setArray(E1);
		x1.setIndex(E2);
		basicMatch(x1);
	}
	public void testArrayCreation() {
		ArrayCreation x1 = ast.newArrayCreation();
		x1.setType(ast.newArrayType(T1));
		x1.dimensions().add(E1);
		x1.dimensions().add(E2);
		x1.setInitializer(ast.newArrayInitializer());
		basicMatch(x1);
	}
	public void testArrayInitializer() {
		ArrayInitializer x1 = ast.newArrayInitializer();
		x1.expressions().add(E1);
		x1.expressions().add(E2);
		basicMatch(x1);
	}
	public void testAssertStatement() {
		AssertStatement x1 = ast.newAssertStatement();
		x1.setExpression(E1);
		x1.setMessage(E2);
		basicMatch(x1);
	}
	public void testAssignment() {
		Assignment x1 = ast.newAssignment();
		x1.setLeftHandSide(E1);
		x1.setRightHandSide(E2);
		basicMatch(x1);
	}
	public void testBlock() {
		Block x1 = ast.newBlock();
		x1.statements().add(S1);
		x1.statements().add(S2);
		basicMatch(x1);
	}
	public void testBooleanLiteral() {
		BooleanLiteral x1 = ast.newBooleanLiteral(true);
		basicMatch(x1);
	}
	public void testBreakStatement() {
		BreakStatement x1 = ast.newBreakStatement();
		x1.setLabel(N1);
		basicMatch(x1);
	}
	public void testCastExpression() {
		CastExpression x1 = ast.newCastExpression();
		x1.setType(T1);
		x1.setExpression(E1);
		basicMatch(x1);
	}
	public void testCatchClause() {
		CatchClause x1 = ast.newCatchClause();
		x1.setException(V1);
		x1.setBody(B1);
		basicMatch(x1);
	}
	public void testCharacterLiteral() {
		CharacterLiteral x1 = ast.newCharacterLiteral();
		x1.setCharValue('q');
		basicMatch(x1);
	}
	public void testClassInstanceCreation() {
		ClassInstanceCreation x1 = ast.newClassInstanceCreation();
		x1.setExpression(E1);
		x1.setName(N1);
		x1.setAnonymousClassDeclaration(ACD1);
		basicMatch(x1);
	}
	public void testCompilationUnit() {
		CompilationUnit x1 = ast.newCompilationUnit();
		x1.setPackage(PD1);
		x1.imports().add(ID1);
		x1.imports().add(ID2);
		x1.types().add(TD1);
		x1.types().add(TD2);
		basicMatch(x1);
	}
	public void testConditionalExpression() {
		ConditionalExpression x1 = ast.newConditionalExpression();
		x1.setExpression(E1);
		x1.setThenExpression(E2);
		x1.setElseExpression(N1);
		basicMatch(x1);
	}
	public void testConstructorInvocation() {
		ConstructorInvocation x1 = ast.newConstructorInvocation();
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		basicMatch(x1);
	}
	public void testContinueStatement() {
		ContinueStatement x1 = ast.newContinueStatement();
		x1.setLabel((SimpleName) N1);
		basicMatch(x1);
	}
	public void testDoStatement() {
		DoStatement x1 = ast.newDoStatement();
		x1.setExpression(E1);
		x1.setBody(S1);
		basicMatch(x1);
	}
	public void testEmptyStatement() {
		EmptyStatement x1 = ast.newEmptyStatement();
		basicMatch(x1);
	}
	public void testExpressionStatement() {
		ExpressionStatement x1 = ast.newExpressionStatement(E1);
		basicMatch(x1);
	}
	public void testFieldAccess() {
		FieldAccess x1 = ast.newFieldAccess();
		x1.setExpression(E1);
		x1.setName(N1);
		basicMatch(x1);
	}
	public void testFieldDeclaration() {
		FieldDeclaration x1 = ast.newFieldDeclaration(W1);
		x1.setJavadoc(JD1);
		x1.setType(T1);
		x1.fragments().add(W2);
		basicMatch(x1);
	}
	public void testForStatement() {
		ForStatement x1 = ast.newForStatement();
		x1.initializers().add(E1);
		x1.initializers().add(E2);
		x1.setExpression(N1);
		x1.updaters().add(N2);
		x1.updaters().add(N3);
		x1.setBody(S1);
		basicMatch(x1);
	}
	public void testIfStatement() {
		IfStatement x1 = ast.newIfStatement();
		x1.setExpression(E1);
		x1.setThenStatement(S1);
		x1.setElseStatement(S2);
		basicMatch(x1);
	}
	public void testImportDeclaration() {
		ImportDeclaration x1 = ast.newImportDeclaration();
		x1.setName(N1);
		basicMatch(x1);
	}
	public void testInfixExpression() {
		InfixExpression x1 = ast.newInfixExpression();
		x1.setOperator(InfixExpression.Operator.PLUS);
		x1.setLeftOperand(E1);
		x1.setRightOperand(E2);
		x1.extendedOperands().add(N1);
		x1.extendedOperands().add(N2);
		basicMatch(x1);
	}
	public void testInitializer() {
		Initializer x1 = ast.newInitializer();
		x1.setJavadoc(JD1);
		x1.setBody(B1);
		basicMatch(x1);
	}
	public void testJavadoc() {
		Javadoc x1 = ast.newJavadoc();
		x1.setComment("/**?*/");
		basicMatch(x1);
	}

	public void testLabeledStatement() {
		LabeledStatement x1 = ast.newLabeledStatement();
		x1.setLabel(N1);
		x1.setBody(S1);
		basicMatch(x1);
	}
	public void testMethodDeclaration() {
		MethodDeclaration x1 = ast.newMethodDeclaration();
		x1.setJavadoc(JD1);
		x1.setReturnType(T1);
		x1.setName(N1);
		x1.parameters().add(V1);
		x1.parameters().add(V2);
		x1.thrownExceptions().add(N2);
		x1.thrownExceptions().add(N3);
		x1.setBody(B1);
		basicMatch(x1);
	}
	public void testMethodInvocation() {
		MethodInvocation x1 = ast.newMethodInvocation();
		x1.setExpression(N1);
		x1.setName(N2);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		basicMatch(x1);
	}
	public void testNullLiteral() {
		NullLiteral x1 = ast.newNullLiteral();
		basicMatch(x1);
	}
	public void testNumberLiteral() {
		NumberLiteral x1 = ast.newNumberLiteral("1.0");
		basicMatch(x1);
	}
	public void testPackageDeclaration() {
		PackageDeclaration x1 = ast.newPackageDeclaration();
		basicMatch(x1);
	}
	public void testParenthesizedExpression() {
		ParenthesizedExpression x1 = ast.newParenthesizedExpression();
		basicMatch(x1);
	}
	public void testPostfixExpression() {
		PostfixExpression x1 = ast.newPostfixExpression();
		x1.setOperand(E1);
		x1.setOperator(PostfixExpression.Operator.INCREMENT);
		basicMatch(x1);
	}
	public void testPrefixExpression() {
		PrefixExpression x1 = ast.newPrefixExpression();
		x1.setOperand(E1);
		x1.setOperator(PrefixExpression.Operator.INCREMENT);
		basicMatch(x1);
	}
	public void testReturnStatement() {
		ReturnStatement x1 = ast.newReturnStatement();
		x1.setExpression(E1);
		basicMatch(x1);
	}
	public void testSingleVariableDeclaration() {
		SingleVariableDeclaration x1 = ast.newSingleVariableDeclaration();
		x1.setType(T1);
		x1.setName(N1);
		x1.setInitializer(E1);
		basicMatch(x1);
	}
	public void testStringLiteral() {
		StringLiteral x1 = ast.newStringLiteral();
		x1.setLiteralValue("H");
		basicMatch(x1);
	}
	public void testSuperConstructorInvocation() {
		SuperConstructorInvocation x1 = ast.newSuperConstructorInvocation();
		x1.setExpression(N1);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		basicMatch(x1);
	}
	public void testSuperFieldAccess() {
		SuperFieldAccess x1 = ast.newSuperFieldAccess();
		x1.setQualifier(N1);
		x1.setName(N2);
		basicMatch(x1);
	}
	public void testSuperMethodInvocation() {
		SuperMethodInvocation x1 = ast.newSuperMethodInvocation();
		x1.setQualifier(N1);
		x1.setName(N2);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		basicMatch(x1);
	}
	public void testSwitchCase() {
		SwitchCase x1 = ast.newSwitchCase();
		x1.setExpression(E1);
		basicMatch(x1);
	}
	public void testSwitchStatement() {
		SwitchStatement x1 = ast.newSwitchStatement();
		x1.setExpression(E1);
		x1.statements().add(S1);
		x1.statements().add(S2);
		basicMatch(x1);
	}
	public void testSynchronizedStatement() {
		SynchronizedStatement x1 = ast.newSynchronizedStatement();
		x1.setExpression(E1);
		x1.setBody(B1);
		basicMatch(x1);
	}
	public void testThisExpression() {
		ThisExpression x1 = ast.newThisExpression();
		x1.setQualifier(N1);
		basicMatch(x1);
	}
	public void testThrowStatement() {
		ThrowStatement x1 = ast.newThrowStatement();
		x1.setExpression(E1);
		basicMatch(x1);
	}
	public void testTryStatement() {
		TryStatement x1 = ast.newTryStatement();
		x1.setBody(B1);
		CatchClause c1 = ast.newCatchClause();
		c1.setException(V1);
		c1.setBody(ast.newBlock());
		x1.catchClauses().add(c1);
		CatchClause c2 = ast.newCatchClause();
		c2.setException(V2);
		c2.setBody(ast.newBlock());
		x1.catchClauses().add(c2);
		x1.setFinally(ast.newBlock());
		basicMatch(x1);
	}
	public void testTypeDeclaration() {
		TypeDeclaration x1 = ast.newTypeDeclaration();
		x1.setJavadoc(JD1);
		x1.setName(N1);
		x1.setSuperclass(N2);
		x1.superInterfaces().add(N3);
		x1.superInterfaces().add(ast.newSimpleName("J"));
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		basicMatch(x1);
	}
	public void testTypeDeclarationStatement() {
		TypeDeclarationStatement x1 = ast.newTypeDeclarationStatement(TD1);
		basicMatch(x1);
	}
	public void testTypeLiteral() {
		TypeLiteral x1 = ast.newTypeLiteral();
		x1.setType(T1);
		basicMatch(x1);
	}
	public void testVariableDeclarationFragment() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		x1.setName(N1);
		x1.setInitializer(E1);
		basicMatch(x1);
	}
	public void testVariableDeclarationExpression() {
		VariableDeclarationExpression x1 = ast.newVariableDeclarationExpression(W1);
		x1.setType(T1);
		x1.fragments().add(W2);
		basicMatch(x1);
	}
	public void testVariableDeclarationStatement() {
		VariableDeclarationStatement x1 = ast.newVariableDeclarationStatement(W1);
		x1.setType(T1);
		x1.fragments().add(W2);
		basicMatch(x1);
	}
	public void testWhileStatement() {
		WhileStatement x1 = ast.newWhileStatement();
		x1.setExpression(E1);
		x1.setBody(S1);
		basicMatch(x1);
	}
}
