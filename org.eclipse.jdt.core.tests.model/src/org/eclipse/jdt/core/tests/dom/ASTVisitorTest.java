/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
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

public class ASTVisitorTest extends TestCase { 

	public static Test suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTVisitorTest.class.getName());
		
		Class c = ASTVisitorTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) {
				suite.addTest(new ASTVisitorTest(methods[i].getName()));
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
	Javadoc JD1;
	String JD1S;
	Javadoc JD2;
	String JD2S;
	AnonymousClassDeclaration ACD1;
	String ACD1S;
	
	final StringBuffer b = new StringBuffer();
	
	public ASTVisitorTest(String name) {
		super(name);
	}
	
	protected void setUp() {
		ast = new AST();
		N1 = ast.newSimpleName("N");
		N1S = "[(nSNNnS)]";
		N2 = ast.newSimpleName("M");
		N2S = "[(nSMMnS)]";
		N3 = ast.newSimpleName("O");
		N3S = "[(nSOOnS)]";
		E1 = ast.newSimpleName("X");
		E1S = "[(nSXXnS)]";
		E2 = ast.newSimpleName("Y");
		E2S = "[(nSYYnS)]";
		T1 = ast.newSimpleType(ast.newSimpleName("Z"));
		T1S = "[(tS[(nSZZnS)]tS)]";
		S1 = ast.newContinueStatement();
		S1S = "[(sCNsCN)]";
		S2 = ast.newBreakStatement();
		S2S = "[(sBRsBR)]";
		B1 = ast.newBlock();
		B1S = "[(sBsB)]";
		V1 = ast.newSingleVariableDeclaration();
		V1.setType(ast.newPrimitiveType(PrimitiveType.INT));
		V1.setName(ast.newSimpleName("a"));
		V1S = "[(VD[(tPintinttP)][(nSaanS)]VD)]";
		V2 = ast.newSingleVariableDeclaration();
		V2.setType(ast.newPrimitiveType(PrimitiveType.BYTE));
		V2.setName(ast.newSimpleName("b"));
		V2S = "[(VD[(tPbytebytetP)][(nSbbnS)]VD)]";
		W1 = ast.newVariableDeclarationFragment();
		W1.setName(ast.newSimpleName("a"));
		W1S = "[(VS[(nSaanS)]VS)]";
		W2 = ast.newVariableDeclarationFragment();
		W2.setName(ast.newSimpleName("b"));
		W2S = "[(VS[(nSbbnS)]VS)]";
		{
			VariableDeclarationFragment temp = ast.newVariableDeclarationFragment();
			temp.setName(ast.newSimpleName("f"));
			FD1 = ast.newFieldDeclaration(temp);
			FD1.setType(ast.newPrimitiveType(PrimitiveType.INT));
			FD1S = "[(FD[(tPintinttP)][(VS[(nSffnS)]VS)]FD)]";
		}
		{
			VariableDeclarationFragment temp = ast.newVariableDeclarationFragment();
			temp.setName(ast.newSimpleName("g"));
			FD2 = ast.newFieldDeclaration(temp);
			FD2.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
			FD2S = "[(FD[(tPcharchartP)][(VS[(nSggnS)]VS)]FD)]";
		}
		PD1 = ast.newPackageDeclaration();
		PD1.setName(ast.newSimpleName("p"));
		PD1S = "[(PD[(nSppnS)]PD)]";
		ID1 = ast.newImportDeclaration();
		ID1.setName(ast.newSimpleName("i"));
		ID1S = "[(ID[(nSiinS)]ID)]";
		ID2 = ast.newImportDeclaration();
		ID2.setName(ast.newSimpleName("j"));
		ID2S = "[(ID[(nSjjnS)]ID)]";
		TD1 = ast.newTypeDeclaration();
		TD1.setName(ast.newSimpleName("c"));
		TD1S = "[(TD[(nSccnS)]TD)]";
		TD2 = ast.newTypeDeclaration();
		TD2.setName(ast.newSimpleName("d"));
		TD2S = "[(TD[(nSddnS)]TD)]";
		
		ACD1 = ast.newAnonymousClassDeclaration();
		ACD1S = "[(ACDACD)]";
		
		JD1 = ast.newJavadoc();
		JD1.setComment("/**X*/");
		JD1S = "[(JD/**X*//**X*/JD)]";
		JD2 = ast.newJavadoc();
		JD2.setComment("/**Y*/");
		JD2S = "[(JD/**Y*//**Y*/JD)]";

	}
	
	protected void tearDown() {
		ast = null;
	}
	
	class TestVisitor extends ASTVisitor {
		
		boolean visitTheKids = true;
		
		public boolean isVisitingChildren() {
			return visitTheKids;
		}

		public void setVisitingChildren(boolean visitChildren) {
			visitTheKids = visitChildren;
		}

		// NAMES

		public boolean visit(SimpleName node) {
			b.append("(nS");
			b.append(node.getIdentifier());
			return isVisitingChildren();
		}
		public void endVisit(SimpleName node) {
			b.append(node.getIdentifier());
			b.append("nS)");
		}
		public boolean visit(QualifiedName node) {
			b.append("(nQ");
			return isVisitingChildren();
		}
		public void endVisit(QualifiedName node) {
			b.append("nQ)");
		}

		// TYPES

		public boolean visit(SimpleType node) {
			b.append("(tS");
			return isVisitingChildren();
		}
		public void endVisit(SimpleType node) {
			b.append("tS)");
		}
		public boolean visit(ArrayType node) {
			b.append("(tA");
			return isVisitingChildren();
		}
		public void endVisit(ArrayType node) {
			b.append("tA)");
		}
		public boolean visit(PrimitiveType node) {
			b.append("(tP");
			b.append(node.getPrimitiveTypeCode().toString());
			return isVisitingChildren();
		}
		public void endVisit(PrimitiveType node) {
			b.append(node.getPrimitiveTypeCode().toString());
			b.append("tP)");
		}

		// EXPRESSIONS and STATEMENTS


		public boolean visit(ArrayAccess node) {
			b.append("(eAA");
			return isVisitingChildren();
		}
		public void endVisit(ArrayAccess node) {
			b.append("eAA)");
		}

		public boolean visit(ArrayCreation node) {
			b.append("(eAC");
			return isVisitingChildren();
		}
		public void endVisit(ArrayCreation node) {
			b.append("eAC)");
		}

		public boolean visit(ArrayInitializer node) {
			b.append("(eAI");
			return isVisitingChildren();
		}
		public void endVisit(ArrayInitializer node) {
			b.append("eAI)");
		}

		public boolean visit(AssertStatement node) {
			b.append("(sAS");
			return isVisitingChildren();
		}
		public void endVisit(AssertStatement node) {
			b.append("sAS)");
		}

		public boolean visit(Assignment node) {
			b.append("(");
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(Assignment node) {
			b.append(node.getOperator().toString());
			b.append(")");
		}

		public boolean visit(Block node) {
			b.append("(sB");
			return isVisitingChildren();
		}
		public void endVisit(Block node) {
			b.append("sB)");
		}

		public boolean visit(BooleanLiteral node) {
			b.append("(eBL");
			b.append(node.booleanValue() ? "true" : "false");
			return isVisitingChildren();
		}
		public void endVisit(BooleanLiteral node) {
			b.append(node.booleanValue() ? "true" : "false");
			b.append("eBL)");
		}

		public boolean visit(BreakStatement node) {
			b.append("(sBR");
			return isVisitingChildren();
		}
		public void endVisit(BreakStatement node) {
			b.append("sBR)");
		}

		public boolean visit(CastExpression node) {
			b.append("(eCS");
			return isVisitingChildren();
		}
		public void endVisit(CastExpression node) {
			b.append("eCS)");
		}

		public boolean visit(CatchClause node) {
			b.append("(cc");
			return isVisitingChildren();
		}
		public void endVisit(CatchClause node) {
			b.append("cc)");
		}

		public boolean visit(CharacterLiteral node) {
			b.append("(eCL");
			b.append(node.getEscapedValue());
			return isVisitingChildren();
		}
		public void endVisit(CharacterLiteral node) {
			b.append(node.getEscapedValue());
			b.append("eCL)");
		}

		public boolean visit(ClassInstanceCreation node) {
			b.append("(eCI");
			return isVisitingChildren();
		}
		public void endVisit(ClassInstanceCreation node) {
			b.append("eCI)");
		}

		public boolean visit(AnonymousClassDeclaration node) {
			b.append("(ACD");
			return isVisitingChildren();
		}
		public void endVisit(AnonymousClassDeclaration node) {
			b.append("ACD)");
		}

		public boolean visit(CompilationUnit node) {
			b.append("(CU");
			return isVisitingChildren();
		}
		public void endVisit(CompilationUnit node) {
			b.append("CU)");
		}

		public boolean visit(ConditionalExpression node) {
			b.append("(eCO");
			return isVisitingChildren();
		}
		public void endVisit(ConditionalExpression node) {
			b.append("eCO)");
		}

		public boolean visit(ConstructorInvocation node) {
			b.append("(sCI");
			return isVisitingChildren();
		}
		public void endVisit(ConstructorInvocation node) {
			b.append("sCI)");
		}

		public boolean visit(ContinueStatement node) {
			b.append("(sCN");
			return isVisitingChildren();
		}
		public void endVisit(ContinueStatement node) {
			b.append("sCN)");
		}

		public boolean visit(DoStatement node) {
			b.append("(sDO");
			return isVisitingChildren();
		}
		public void endVisit(DoStatement node) {
			b.append("sDO)");
		}

		public boolean visit(EmptyStatement node) {
			b.append("(sEM");
			return isVisitingChildren();
		}
		public void endVisit(EmptyStatement node) {
			b.append("sEM)");
		}

		public boolean visit(ExpressionStatement node) {
			b.append("(sEX");
			return isVisitingChildren();
		}
		public void endVisit(ExpressionStatement node) {
			b.append("sEX)");
		}

		public boolean visit(FieldAccess node) {
			b.append("(eFA");
			return isVisitingChildren();
		}
		public void endVisit(FieldAccess node) {
			b.append("eFA)");
		}

		public boolean visit(FieldDeclaration node) {
			b.append("(FD");
			return isVisitingChildren();
		}
		public void endVisit(FieldDeclaration node) {
			b.append("FD)");
		}

		public boolean visit(ForStatement node) {
			b.append("(sFR");
			return isVisitingChildren();
		}
		public void endVisit(ForStatement node) {
			b.append("sFR)");
		}

		public boolean visit(IfStatement node) {
			b.append("(sIF");
			return isVisitingChildren();
		}
		public void endVisit(IfStatement node) {
			b.append("sIF)");
		}

		public boolean visit(ImportDeclaration node) {
			b.append("(ID");
			return isVisitingChildren();
		}
		public void endVisit(ImportDeclaration node) {
			b.append("ID)");
		}

		public boolean visit(InfixExpression node) {
			b.append("(eIN");
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(InfixExpression node) {
			b.append(node.getOperator().toString());
			b.append("eIN)");
		}

		public boolean visit(InstanceofExpression node) {
			b.append("(eIO");
			return isVisitingChildren();
		}
		public void endVisit(InstanceofExpression node) {
			b.append("eIO)");
		}

		public boolean visit(Initializer node) {
			b.append("(IN");
			return isVisitingChildren();
		}
		public void endVisit(Initializer node) {
			b.append("IN)");
		}

		public boolean visit(Javadoc node) {
			b.append("(JD");
			b.append(node.getComment());
			return isVisitingChildren();
		}
		public void endVisit(Javadoc node) {
			b.append(node.getComment());
			b.append("JD)");
		}

		public boolean visit(LabeledStatement node) {
			b.append("(sLA");
			return isVisitingChildren();
		}
		public void endVisit(LabeledStatement node) {
			b.append("sLA)");
		}

		public boolean visit(MethodDeclaration node) {
			b.append("(MD");
			return isVisitingChildren();
		}
		public void endVisit(MethodDeclaration node) {
			b.append("MD)");
		}

		public boolean visit(MethodInvocation node) {
			b.append("(eMI");
			return isVisitingChildren();
		}
		public void endVisit(MethodInvocation node) {
			b.append("eMI)");
		}

		public boolean visit(NullLiteral node) {
			b.append("(eNL");
			return isVisitingChildren();
		}
		public void endVisit(NullLiteral node) {
			b.append("eNL)");
		}

		public boolean visit(NumberLiteral node) {
			b.append("(eNU");
			b.append(node.getToken());
			return isVisitingChildren();
		}
		public void endVisit(NumberLiteral node) {
			b.append(node.getToken());
			b.append("eNU)");
		}

		public boolean visit(PackageDeclaration node) {
			b.append("(PD");
			return isVisitingChildren();
		}
		public void endVisit(PackageDeclaration node) {
			b.append("PD)");
		}

		public boolean visit(ParenthesizedExpression node) {
			b.append("(ePA");
			return isVisitingChildren();
		}
		public void endVisit(ParenthesizedExpression node) {
			b.append("ePA)");
		}

		public boolean visit(PostfixExpression node) {
			b.append("(ePO");
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(PostfixExpression node) {
			b.append(node.getOperator().toString());
			b.append("ePO)");
		}

		public boolean visit(PrefixExpression node) {
			b.append("(ePR");
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(PrefixExpression node) {
			b.append(node.getOperator().toString());
			b.append("ePR)");
		}

		public boolean visit(ReturnStatement node) {
			b.append("(sRT");
			return isVisitingChildren();
		}
		public void endVisit(ReturnStatement node) {
			b.append("sRT)");
		}

		public boolean visit(SingleVariableDeclaration node) {
			b.append("(VD");
			return isVisitingChildren();
		}
		public void endVisit(SingleVariableDeclaration node) {
			b.append("VD)");
		}

		public boolean visit(StringLiteral node) {
			b.append("(eSL");
			b.append(node.getLiteralValue());
			return isVisitingChildren();
		}
		public void endVisit(StringLiteral node) {
			b.append(node.getLiteralValue());
			b.append("eSL)");
		}

		public boolean visit(SuperConstructorInvocation node) {
			b.append("(sSC");
			return isVisitingChildren();
		}
		public void endVisit(SuperConstructorInvocation node) {
			b.append("sSC)");
		}

		public boolean visit(SuperFieldAccess node) {
			b.append("(eSF");
			return isVisitingChildren();
		}
		public void endVisit(SuperFieldAccess node) {
			b.append("eSF)");
		}

		public boolean visit(SuperMethodInvocation node) {
			b.append("(eSM");
			return isVisitingChildren();
		}
		public void endVisit(SuperMethodInvocation node) {
			b.append("eSM)");
		}

		public boolean visit(SwitchCase node) {
			b.append("(sSC");
			return isVisitingChildren();
		}
		public void endVisit(SwitchCase node) {
			b.append("sSC)");
		}

		public boolean visit(SwitchStatement node) {
			b.append("(sSW");
			return isVisitingChildren();
		}
		public void endVisit(SwitchStatement node) {
			b.append("sSW)");
		}

		public boolean visit(SynchronizedStatement node) {
			b.append("(sSY");
			return isVisitingChildren();
		}
		public void endVisit(SynchronizedStatement node) {
			b.append("sSY)");
		}

		public boolean visit(ThisExpression node) {
			b.append("(eTH");
			return isVisitingChildren();
		}
		public void endVisit(ThisExpression node) {
			b.append("eTH)");
		}

		public boolean visit(ThrowStatement node) {
			b.append("(sTR");
			return isVisitingChildren();
		}
		public void endVisit(ThrowStatement node) {
			b.append("sTR)");
		}

		public boolean visit(TryStatement node) {
			b.append("(sTY");
			return isVisitingChildren();
		}
		public void endVisit(TryStatement node) {
			b.append("sTY)");
		}

		public boolean visit(TypeDeclaration node) {
			b.append("(TD");
			return isVisitingChildren();
		}
		public void endVisit(TypeDeclaration node) {
			b.append("TD)");
		}

		public boolean visit(TypeDeclarationStatement node) {
			b.append("(sTD");
			return isVisitingChildren();
		}
		public void endVisit(TypeDeclarationStatement node) {
			b.append("sTD)");
		}

		public boolean visit(TypeLiteral node) {
			b.append("(eTL");
			return isVisitingChildren();
		}
		public void endVisit(TypeLiteral node) {
			b.append("eTL)");
		}

		public boolean visit(VariableDeclarationExpression node) {
			b.append("(eVD");
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationExpression node) {
			b.append("eVD)");
		}

		public boolean visit(VariableDeclarationFragment node) {
			b.append("(VS");
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationFragment node) {
			b.append("VS)");
		}

		public boolean visit(VariableDeclarationStatement node) {
			b.append("(sVD");
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationStatement node) {
			b.append("sVD)");
		}

		public boolean visit(WhileStatement node) {
			b.append("(sWH");
			return isVisitingChildren();
		}
		public void endVisit(WhileStatement node) {
			b.append("sWH)");
		}
		
		public void preVisit(ASTNode node) {
			b.append("[");
		}

		public void postVisit(ASTNode node) {
			b.append("]");
		}

	}	
	// NAMES
	public void testSimpleName() {
		Name x1 = ast.newName(new String[]{"Z"});
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(nSZZnS)]".equals(result));
	}

	public void testQualifiedName() {
		Name x1 = ast.newName(new String[]{"X", "Y"});
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(nQ[(nSXXnS)][(nSYYnS)]nQ)]".equals(result));
	}

	
	// TYPES
	public void testPrimitiveType() {
		Type x1 = ast.newPrimitiveType(PrimitiveType.CHAR);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(tPcharchartP)]".equals(result));
	}

	public void testSimpleType() {
		Type x1 = ast.newSimpleType(ast.newName(new String[]{"Z"}));
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(tS[(nSZZnS)]tS)]".equals(b.toString()));
	}

	public void testArrayType() {
		Type x0 = ast.newPrimitiveType(PrimitiveType.CHAR);
		Type x1 = ast.newArrayType(x0);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(tA[(tPcharchartP)]tA)]".equals(result));
	}

	// EXPRESSIONS and STATEMENTS

	public void testArrayAccess() {
		ArrayAccess x1 = ast.newArrayAccess();
		x1.setArray(E1);
		x1.setIndex(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eAA"+E1S+E2S+"eAA)]"));
	}
	
	public void testArrayCreation() {
		ArrayCreation x1 = ast.newArrayCreation();
		x1.setType(ast.newArrayType(T1));
		x1.dimensions().add(E1);
		x1.dimensions().add(E2);
		x1.setInitializer(ast.newArrayInitializer());
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eAC"+"[(tA"+T1S+"tA)]"+E1S+E2S+"[(eAIeAI)]eAC)]"));
	}
	public void testArrayInitializer() {
		ArrayInitializer x1 = ast.newArrayInitializer();
		x1.expressions().add(E1);
		x1.expressions().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eAI"+E1S+E2S+"eAI)]"));
	}
	public void testAssertStatement() {
		AssertStatement x1 = ast.newAssertStatement();
		x1.setExpression(E1);
		x1.setMessage(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sAS"+E1S+E2S+"sAS)]"));
	}
	public void testAssignment() {
		Assignment x1 = ast.newAssignment();
		x1.setLeftHandSide(E1);
		x1.setRightHandSide(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(="+E1S+E2S+"=)]"));
	}
	public void testBlock() {
		Block x1 = ast.newBlock();
		x1.statements().add(S1);
		x1.statements().add(S2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sB"+S1S+S2S+"sB)]"));

		// check that visiting children can be cut off
		v1.setVisitingChildren(false);
		b.setLength(0);
		x1.accept(v1);
		result = b.toString();
		assertTrue(result.equals("[(sBsB)]"));
	}
	public void testBooleanLiteral() {
		BooleanLiteral x1 = ast.newBooleanLiteral(true);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eBLtruetrueeBL)]"));
	}
	public void testBreakStatement() {
		BreakStatement x1 = ast.newBreakStatement();
		x1.setLabel(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sBR"+N1S+"sBR)]"));
	}
	public void testCastExpression() {
		CastExpression x1 = ast.newCastExpression();
		x1.setType(T1);
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eCS"+T1S+E1S+"eCS)]"));
	}
	public void testCatchClause() {
		CatchClause x1 = ast.newCatchClause();
		x1.setException(V1);
		x1.setBody(B1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(cc"+V1S+B1S+"cc)]"));
	}
	public void testCharacterLiteral() {
		CharacterLiteral x1 = ast.newCharacterLiteral();
		x1.setCharValue('q');
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eCL'q''q'eCL)]"));
	}
	public void testClassInstanceCreation() {
		ClassInstanceCreation x1 = ast.newClassInstanceCreation();
		x1.setExpression(E1);
		x1.setName(N1);
		x1.setAnonymousClassDeclaration(ACD1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eCI"+E1S+N1S+ACD1S+"eCI)]"));
	}
	public void testAnonymousClassDeclaration() {
		AnonymousClassDeclaration x1 = ast.newAnonymousClassDeclaration();
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ACD"+FD1S+FD2S+"ACD)]"));
	}
	public void testCompilationUnit() {
		CompilationUnit x1 = ast.newCompilationUnit();
		x1.setPackage(PD1);
		x1.imports().add(ID1);
		x1.imports().add(ID2);
		x1.types().add(TD1);
		x1.types().add(TD2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(CU"+PD1S+ID1S+ID2S+TD1S+TD2S+"CU)]"));

		// check that visiting children can be cut off
		v1.setVisitingChildren(false);
		b.setLength(0);
		x1.accept(v1);
		result = b.toString();
		assertTrue(result.equals("[(CUCU)]"));
	}
	public void testConditionalExpression() {
		ConditionalExpression x1 = ast.newConditionalExpression();
		x1.setExpression(E1);
		x1.setThenExpression(E2);
		x1.setElseExpression(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eCO"+E1S+E2S+N1S+"eCO)]"));
	}
	public void testConstructorInvocation() {
		ConstructorInvocation x1 = ast.newConstructorInvocation();
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sCI"+E1S+E2S+"sCI)]"));
	}
	public void testContinueStatement() {
		ContinueStatement x1 = ast.newContinueStatement();
		x1.setLabel((SimpleName) N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sCN"+N1S+"sCN)]"));
	}
	public void testDoStatement() {
		DoStatement x1 = ast.newDoStatement();
		x1.setExpression(E1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sDO"+E1S+S1S+"sDO)]"));
	}
	public void testEmptyStatement() {
		EmptyStatement x1 = ast.newEmptyStatement();
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sEMsEM)]"));
	}
	public void testExpressionStatement() {
		ExpressionStatement x1 = ast.newExpressionStatement(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sEX"+E1S+"sEX)]"));
	}
	public void testFieldAccess() {
		FieldAccess x1 = ast.newFieldAccess();
		x1.setExpression(E1);
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eFA"+E1S+N1S+"eFA)]"));
	}
	public void testFieldDeclaration() {
		FieldDeclaration x1 = ast.newFieldDeclaration(W1);
		x1.setJavadoc(JD1);
		x1.setType(T1);
		x1.fragments().add(W2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(FD"+JD1S+T1S+W1S+W2S+"FD)]"));
	}
	public void testForStatement() {
		ForStatement x1 = ast.newForStatement();
		x1.initializers().add(E1);
		x1.initializers().add(E2);
		x1.setExpression(N1);
		x1.updaters().add(N2);
		x1.updaters().add(N3);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sFR"+E1S+E2S+N1S+N2S+N3S+S1S+"sFR)]"));
	}
	public void testIfStatement() {
		IfStatement x1 = ast.newIfStatement();
		x1.setExpression(E1);
		x1.setThenStatement(S1);
		x1.setElseStatement(S2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sIF"+E1S+S1S+S2S+"sIF)]"));
	}
	public void testImportDeclaration() {
		ImportDeclaration x1 = ast.newImportDeclaration();
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ID"+N1S+"ID)]"));
	}
	public void testInfixExpression() {
		InfixExpression x1 = ast.newInfixExpression();
		x1.setOperator(InfixExpression.Operator.PLUS);
		x1.setLeftOperand(E1);
		x1.setRightOperand(E2);
		x1.extendedOperands().add(N1);
		x1.extendedOperands().add(N2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eIN+"+E1S+E2S+N1S+N2S+"+eIN)]"));
	}
	public void testInstanceofExpression() {
		InstanceofExpression x1 = ast.newInstanceofExpression();
		x1.setLeftOperand(E1);
		x1.setRightOperand(T1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eIO"+E1S+T1S+"eIO)]"));
	}
	public void testInitializer() {
		Initializer x1 = ast.newInitializer();
		x1.setJavadoc(JD1);
		x1.setBody(B1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(IN"+JD1S+B1S+"IN)]"));
	}
	public void testJavadoc() {
		Javadoc x1 = ast.newJavadoc();
		x1.setComment("/**?*/");
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(JD/**?*//**?*/JD)]".equals(result));
	}

	public void testLabeledStatement() {
		LabeledStatement x1 = ast.newLabeledStatement();
		x1.setLabel(N1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sLA"+N1S+S1S+"sLA)]"));
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
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(MD"+JD1S+T1S+N1S+V1S+V2S+N2S+N3S+B1S+"MD)]"));
	}
	public void testMethodInvocation() {
		MethodInvocation x1 = ast.newMethodInvocation();
		x1.setExpression(N1);
		x1.setName(N2);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eMI"+N1S+N2S+E1S+E2S+"eMI)]"));
	}
	public void testNullLiteral() {
		NullLiteral x1 = ast.newNullLiteral();
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eNLeNL)]"));
	}
	public void testNumberLiteral() {
		NumberLiteral x1 = ast.newNumberLiteral("1.0");
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eNU1.01.0eNU)]"));
	}
	public void testPackageDeclaration() {
		PackageDeclaration x1 = ast.newPackageDeclaration();
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(PD"+N1S+"PD)]"));
	}
	public void testParenthesizedExpression() {
		ParenthesizedExpression x1 = ast.newParenthesizedExpression();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ePA"+E1S+"ePA)]"));
	}
	public void testPostfixExpression() {
		PostfixExpression x1 = ast.newPostfixExpression();
		x1.setOperand(E1);
		x1.setOperator(PostfixExpression.Operator.INCREMENT);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ePO++"+E1S+"++ePO)]"));
	}
	public void testPrefixExpression() {
		PrefixExpression x1 = ast.newPrefixExpression();
		x1.setOperand(E1);
		x1.setOperator(PrefixExpression.Operator.INCREMENT);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ePR++"+E1S+"++ePR)]"));
	}
	public void testReturnStatement() {
		ReturnStatement x1 = ast.newReturnStatement();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sRT"+E1S+"sRT)]"));
	}
	public void testStringLiteral() {
		StringLiteral x1 = ast.newStringLiteral();
		x1.setLiteralValue("H");
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eSLHHeSL)]"));
	}
	public void testSuperConstructorInvocation() {
		SuperConstructorInvocation x1 = ast.newSuperConstructorInvocation();
		x1.setExpression(N1);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sSC"+N1S+E1S+E2S+"sSC)]"));
	}
	public void testSuperFieldAccess() {
		SuperFieldAccess x1 = ast.newSuperFieldAccess();
		x1.setQualifier(N1);
		x1.setName(N2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eSF"+N1S+N2S+"eSF)]"));
	}
	public void testSuperMethodInvocation() {
		SuperMethodInvocation x1 = ast.newSuperMethodInvocation();
		x1.setQualifier(N1);
		x1.setName(N2);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eSM"+N1S+N2S+E1S+E2S+"eSM)]"));
	}
	public void testSwitchCase() {
		SwitchCase x1 = ast.newSwitchCase();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sSC"+E1S+"sSC)]"));
	}
	public void testSwitchStatement() {
		SwitchStatement x1 = ast.newSwitchStatement();
		x1.setExpression(E1);
		x1.statements().add(S1);
		x1.statements().add(S2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sSW"+E1S+S1S+S2S+"sSW)]"));
	}
	public void testSynchronizedStatement() {
		SynchronizedStatement x1 = ast.newSynchronizedStatement();
		x1.setExpression(E1);
		x1.setBody(B1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sSY"+E1S+B1S+"sSY)]"));
	}
	public void testThisExpression() {
		ThisExpression x1 = ast.newThisExpression();
		x1.setQualifier(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eTH"+N1S+"eTH)]"));
	}
	public void testThrowStatement() {
		ThrowStatement x1 = ast.newThrowStatement();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sTR"+E1S+"sTR)]"));
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
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sTY"+B1S+"[(cc"+V1S+"[(sBsB)]"+"cc)]"+"[(cc"+V2S+"[(sBsB)]"+"cc)]"+"[(sBsB)]"+"sTY)]"));
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
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(TD"+JD1S+N1S+N2S+N3S+"[(nSJJnS)]"+FD1S+FD2S+"TD)]"));
	}
	public void testTypeDeclarationStatement() {
		TypeDeclarationStatement x1 = ast.newTypeDeclarationStatement(TD1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sTD"+TD1S+"sTD)]"));
	}
	public void testTypeLiteral() {
		TypeLiteral x1 = ast.newTypeLiteral();
		x1.setType(T1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eTL"+T1S+"eTL)]"));
	}
	public void testVariableDeclaration() {
		SingleVariableDeclaration x1 = ast.newSingleVariableDeclaration();
		x1.setType(T1);
		x1.setName(N1);
		x1.setInitializer(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(VD"+T1S+N1S+E1S+"VD)]"));
	}
	public void testVariableSpecifier() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		x1.setName(N1);
		x1.setInitializer(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(VS"+N1S+E1S+"VS)]"));
	}
	public void testVariableDeclarationExpression() {
		VariableDeclarationExpression x1 = ast.newVariableDeclarationExpression(W1);
		x1.setType(T1);
		x1.fragments().add(W2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eVD"+T1S+W1S+W2S+"eVD)]"));
	}
	public void testVariableDeclarationStatement() {
		VariableDeclarationStatement x1 = ast.newVariableDeclarationStatement(W1);
		x1.setType(T1);
		x1.fragments().add(W2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sVD"+T1S+W1S+W2S+"sVD)]"));
	}
	public void testWhileStatement() {
		WhileStatement x1 = ast.newWhileStatement();
		x1.setExpression(E1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sWH"+E1S+S1S+"sWH)]"));
	}

	public void testPrePost() {
		SimpleName n1 = ast.newSimpleName("a");
		SimpleName n2 = ast.newSimpleName("b");
		QualifiedName q = ast.newQualifiedName(n1, n2);
		TestVisitor v1 = new TestVisitor() {
			public void preVisit(ASTNode node) {
				b.append("[");
				switch (node.getNodeType()) {
					case ASTNode.QUALIFIED_NAME :
						b.append("q");
						break;
					case ASTNode.SIMPLE_NAME :
						b.append(((SimpleName) node).getIdentifier());
						break;
				}
			}

			public void postVisit(ASTNode node) {
				switch (node.getNodeType()) {
					case ASTNode.QUALIFIED_NAME :
						b.append("q");
						break;
					case ASTNode.SIMPLE_NAME :
						b.append(((SimpleName) node).getIdentifier());
						break;
				}
				b.append("]");
			}
		};

		b.setLength(0);
		q.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[q(nQ" + "[a(nSaanS)a]" + "[b(nSbbnS)b]" + "nQ)q]"));
	}	
	public void testTraverseAndModify() {
		final TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
		typeDeclaration.setName(N1);
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName("M1"));
		typeDeclaration.bodyDeclarations().add(0, methodDeclaration);
		final MethodDeclaration methodDeclaration2 = ast.newMethodDeclaration();
		methodDeclaration2.setName(ast.newSimpleName("M2"));
		typeDeclaration.bodyDeclarations().add(1, methodDeclaration2);
		MethodDeclaration methodDeclaration3 = ast.newMethodDeclaration();
		methodDeclaration3.setName(ast.newSimpleName("M3"));
		typeDeclaration.bodyDeclarations().add(2, methodDeclaration3);
		// insert a new before the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(MethodDeclaration node) {
				if (node == methodDeclaration2) {
					MethodDeclaration methodDeclaration4 = ast.newMethodDeclaration();
					methodDeclaration4.setName(ast.newSimpleName("M4"));
					typeDeclaration.bodyDeclarations().add(0, methodDeclaration4);
				}
				return super.visit(node);
			}			
		};
		b.setLength(0);
		typeDeclaration.accept(v1);
		assertEquals("wrong output", "[(TD[(nSNNnS)][(MD[(tPvoidvoidtP)][(nSM1M1nS)]MD)][(MD[(tPvoidvoidtP)][(nSM2M2nS)]MD)][(MD[(tPvoidvoidtP)][(nSM3M3nS)]MD)]TD)]", b.toString());
	}

	public void testTraverseAndModify_2() {
		final TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
		typeDeclaration.setName(N1);
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName("M1"));
		typeDeclaration.bodyDeclarations().add(0, methodDeclaration);
		final MethodDeclaration methodDeclaration2 = ast.newMethodDeclaration();
		methodDeclaration2.setName(ast.newSimpleName("M2"));
		typeDeclaration.bodyDeclarations().add(1, methodDeclaration2);
		MethodDeclaration methodDeclaration3 = ast.newMethodDeclaration();
		methodDeclaration3.setName(ast.newSimpleName("M3"));
		typeDeclaration.bodyDeclarations().add(2, methodDeclaration3);
		// insert a new after the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(MethodDeclaration node) {
				if (node == methodDeclaration2) {
					MethodDeclaration methodDeclaration4 = ast.newMethodDeclaration();
					methodDeclaration4.setName(ast.newSimpleName("M4"));
					typeDeclaration.bodyDeclarations().add(3, methodDeclaration4);
				}
				return super.visit(node);
			}			
		};
		b.setLength(0);
		typeDeclaration.accept(v1);
		assertEquals("wrong output", "[(TD[(nSNNnS)][(MD[(tPvoidvoidtP)][(nSM1M1nS)]MD)][(MD[(tPvoidvoidtP)][(nSM2M2nS)]MD)][(MD[(tPvoidvoidtP)][(nSM3M3nS)]MD)][(MD[(tPvoidvoidtP)][(nSM4M4nS)]MD)]TD)]", b.toString());
	}
	public void testTraverseAndModify_3() {
		final InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setLeftOperand(ast.newSimpleName("i"));
		infixExpression.setRightOperand(ast.newNumberLiteral("10"));
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// insert a new after the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(SimpleName node) {
				infixExpression.setRightOperand(ast.newNumberLiteral("22"));
				return super.visit(node);
			}			
		};
		b.setLength(0);
		infixExpression.accept(v1);
		assertEquals("wrong output", "[(eIN+[(nSiinS)][(eNU2222eNU)]+eIN)]", b.toString());
	}
	public void testTraverseAndModify_4() {
		final InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setLeftOperand(ast.newSimpleName("i"));
		infixExpression.setRightOperand(ast.newNumberLiteral("10"));
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// insert a new before the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(NumberLiteral node) {
				infixExpression.setLeftOperand(ast.newSimpleName("j"));
				return super.visit(node);
			}			
		};
		b.setLength(0);
		infixExpression.accept(v1);
		assertEquals("wrong output", "[(eIN+[(nSiinS)][(eNU1010eNU)]+eIN)]", b.toString());
	}
}
