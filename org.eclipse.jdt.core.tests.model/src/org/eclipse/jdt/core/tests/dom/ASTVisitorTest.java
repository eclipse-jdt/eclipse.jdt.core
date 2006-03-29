/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

public class ASTVisitorTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase { 

	/** @deprecated using deprecated code */
	public static Test suite() {
		// TODO (frederic) use buildList + setAstLevel(init) instead...
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTVisitorTest.class.getName());
		
		Class c = ASTVisitorTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTVisitorTest(methods[i].getName(), AST.JLS2));
				suite.addTest(new ASTVisitorTest(methods[i].getName(), AST.JLS3));
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
	SimpleName N4;
	String N4S;
	Expression E1;
	String E1S;
	Expression E2;
	String E2S;
	Type T1;
	String T1S;
	Type T2;
	String T2S;
	ParameterizedType PT1;
	String PT1S;
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
	TagElement TAG1;
	String TAG1S;
	TextElement TEXT1;
	String TEXT1S;
	MemberRef MBREF1;
	String MBREF1S;
	MethodRef MTHREF1;
	String MTHREF1S;
	MethodRefParameter MPARM1;
	String MPARM1S;
	LineComment LC1;
	String LC1S;
	BlockComment BC1;
	String BC1S;
	AnonymousClassDeclaration ACD1;
	String ACD1S;
	TypeParameter TP1;
	String TP1S;
	TypeParameter TP2;
	String TP2S;
	MemberValuePair MVP1;
	String MVP1S;
	MemberValuePair MVP2;
	String MVP2S;
	Modifier MOD1;
	String MOD1S;
	Modifier MOD2;
	String MOD2S;
	Annotation ANO1;
	String ANO1S;
	Annotation ANO2;
	String ANO2S;
	EnumConstantDeclaration EC1;
	String EC1S;
	EnumConstantDeclaration EC2;
	String EC2S;
	
	final StringBuffer b = new StringBuffer();
	
	int API_LEVEL;

	public ASTVisitorTest(String name, int apiLevel) {
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
		
		ast = AST.newAST(this.API_LEVEL);
		N1 = ast.newSimpleName("N"); //$NON-NLS-1$
		N1S = "[(nSNNnS)]"; //$NON-NLS-1$
		N2 = ast.newSimpleName("M"); //$NON-NLS-1$
		N2S = "[(nSMMnS)]"; //$NON-NLS-1$
		N3 = ast.newSimpleName("O"); //$NON-NLS-1$
		N3S = "[(nSOOnS)]"; //$NON-NLS-1$
		N4 = ast.newSimpleName("P"); //$NON-NLS-1$
		N4S = "[(nSPPnS)]"; //$NON-NLS-1$
		E1 = ast.newSimpleName("X"); //$NON-NLS-1$
		E1S = "[(nSXXnS)]"; //$NON-NLS-1$
		E2 = ast.newSimpleName("Y"); //$NON-NLS-1$
		E2S = "[(nSYYnS)]"; //$NON-NLS-1$
		T1 = ast.newSimpleType(ast.newSimpleName("Z")); //$NON-NLS-1$
		T1S = "[(tS[(nSZZnS)]tS)]"; //$NON-NLS-1$
		T2 = ast.newSimpleType(ast.newSimpleName("X")); //$NON-NLS-1$
		T2S = "[(tS[(nSXXnS)]tS)]"; //$NON-NLS-1$
		S1 = ast.newContinueStatement();
		S1S = "[(sCNsCN)]"; //$NON-NLS-1$
		S2 = ast.newBreakStatement();
		S2S = "[(sBRsBR)]"; //$NON-NLS-1$
		B1 = ast.newBlock();
		B1S = "[(sBsB)]"; //$NON-NLS-1$
		V1 = ast.newSingleVariableDeclaration();
		V1.setType(ast.newPrimitiveType(PrimitiveType.INT));
		V1.setName(ast.newSimpleName("a")); //$NON-NLS-1$
		V1S = "[(VD[(tPintinttP)][(nSaanS)]VD)]"; //$NON-NLS-1$
		V2 = ast.newSingleVariableDeclaration();
		V2.setType(ast.newPrimitiveType(PrimitiveType.BYTE));
		V2.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		V2S = "[(VD[(tPbytebytetP)][(nSbbnS)]VD)]"; //$NON-NLS-1$
		W1 = ast.newVariableDeclarationFragment();
		W1.setName(ast.newSimpleName("a")); //$NON-NLS-1$
		W1S = "[(VS[(nSaanS)]VS)]"; //$NON-NLS-1$
		W2 = ast.newVariableDeclarationFragment();
		W2.setName(ast.newSimpleName("b")); //$NON-NLS-1$
		W2S = "[(VS[(nSbbnS)]VS)]"; //$NON-NLS-1$
		{
			VariableDeclarationFragment temp = ast.newVariableDeclarationFragment();
			temp.setName(ast.newSimpleName("f")); //$NON-NLS-1$
			FD1 = ast.newFieldDeclaration(temp);
			FD1.setType(ast.newPrimitiveType(PrimitiveType.INT));
			FD1S = "[(FD[(tPintinttP)][(VS[(nSffnS)]VS)]FD)]"; //$NON-NLS-1$
		}
		{
			VariableDeclarationFragment temp = ast.newVariableDeclarationFragment();
			temp.setName(ast.newSimpleName("g")); //$NON-NLS-1$
			FD2 = ast.newFieldDeclaration(temp);
			FD2.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
			FD2S = "[(FD[(tPcharchartP)][(VS[(nSggnS)]VS)]FD)]"; //$NON-NLS-1$
		}
		PD1 = ast.newPackageDeclaration();
		PD1.setName(ast.newSimpleName("p")); //$NON-NLS-1$
		PD1S = "[(PD[(nSppnS)]PD)]"; //$NON-NLS-1$
		ID1 = ast.newImportDeclaration();
		ID1.setName(ast.newSimpleName("i")); //$NON-NLS-1$
		ID1S = "[(ID[(nSiinS)]ID)]"; //$NON-NLS-1$
		ID2 = ast.newImportDeclaration();
		ID2.setName(ast.newSimpleName("j")); //$NON-NLS-1$
		ID2S = "[(ID[(nSjjnS)]ID)]"; //$NON-NLS-1$
		TD1 = ast.newTypeDeclaration();
		TD1.setName(ast.newSimpleName("c")); //$NON-NLS-1$
		TD1S = "[(TD[(nSccnS)]TD)]"; //$NON-NLS-1$
		TD2 = ast.newTypeDeclaration();
		TD2.setName(ast.newSimpleName("d")); //$NON-NLS-1$
		TD2S = "[(TD[(nSddnS)]TD)]"; //$NON-NLS-1$
		
		ACD1 = ast.newAnonymousClassDeclaration();
		ACD1S = "[(ACDACD)]"; //$NON-NLS-1$
		
		JD1 = ast.newJavadoc();
		JD1S = "[(JDJD)]"; //$NON-NLS-1$
		
		LC1 = ast.newLineComment();
		LC1S = "[(//*//)]"; //$NON-NLS-1$

		BC1 = ast.newBlockComment();
		BC1S = "[(/**/)]"; //$NON-NLS-1$
		
		TAG1 = ast.newTagElement();
		TAG1.setTagName("@foo"); //$NON-NLS-1$
		TAG1S = "[(TG@foo@fooTG)]";  //$NON-NLS-1$

		TEXT1 = ast.newTextElement();
		TEXT1.setText("foo"); //$NON-NLS-1$
		TEXT1S = "[(TXfoofooTX)]";  //$NON-NLS-1$

		MBREF1 = ast.newMemberRef();
		MBREF1.setName(ast.newSimpleName("p")); //$NON-NLS-1$
		MBREF1S = "[(MBREF[(nSppnS)]MBREF)]";  //$NON-NLS-1$

		MTHREF1 = ast.newMethodRef();
		MTHREF1.setName(ast.newSimpleName("p")); //$NON-NLS-1$
		MTHREF1S = "[(MTHREF[(nSppnS)]MTHREF)]";  //$NON-NLS-1$

		MPARM1 = ast.newMethodRefParameter();
		MPARM1.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
		MPARM1S = "[(MPARM[(tPcharchartP)]MPARM)]";  //$NON-NLS-1$

		if (ast.apiLevel() >= AST.JLS3) {
			PT1 = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("Z"))); //$NON-NLS-1$
			PT1S = "[(tM[(tS[(nSZZnS)]tS)]tM)]"; //$NON-NLS-1$

			TP1 = ast.newTypeParameter();
			TP1.setName(ast.newSimpleName("x")); //$NON-NLS-1$
			TP1S = "[(tTP[(nSxxnS)]tTP)]"; //$NON-NLS-1$

			TP2 = ast.newTypeParameter();
			TP2.setName(ast.newSimpleName("y")); //$NON-NLS-1$
			TP2S = "[(tTP[(nSyynS)]tTP)]"; //$NON-NLS-1$

			MVP1 = ast.newMemberValuePair();
			MVP1.setName(ast.newSimpleName("x")); //$NON-NLS-1$
			MVP1.setValue(ast.newSimpleName("y")); //$NON-NLS-1$
			MVP1S = "[(@MVP[(nSxxnS)][(nSyynS)]@MVP)]"; //$NON-NLS-1$
		
			MVP2 = ast.newMemberValuePair();
			MVP2.setName(ast.newSimpleName("a")); //$NON-NLS-1$
			MVP2.setValue(ast.newSimpleName("b")); //$NON-NLS-1$
			MVP2S = "[(@MVP[(nSaanS)][(nSbbnS)]@MVP)]"; //$NON-NLS-1$
		
			MOD1 = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			MOD1S = "[(MODpublicpublicMOD)]"; //$NON-NLS-1$
			MOD2 = ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
			MOD2S = "[(MODfinalfinalMOD)]"; //$NON-NLS-1$
		
			ANO1 = ast.newMarkerAnnotation();
			ANO1.setTypeName(ast.newSimpleName("a")); //$NON-NLS-1$
			ANO1S = "[(@MAN[(nSaanS)]@MAN)]"; //$NON-NLS-1$

			ANO2 = ast.newNormalAnnotation();
			ANO2.setTypeName(ast.newSimpleName("b")); //$NON-NLS-1$
			ANO2S = "[(@NAN[(nSbbnS)]@NAN)]"; //$NON-NLS-1$
		
			EC1 = ast.newEnumConstantDeclaration();
			EC1.setName(ast.newSimpleName("c")); //$NON-NLS-1$
			EC1S = "[(ECD[(nSccnS)]ECD)]"; //$NON-NLS-1$
		
			EC2 = ast.newEnumConstantDeclaration();
			EC2.setName(ast.newSimpleName("d")); //$NON-NLS-1$
			EC2S = "[(ECD[(nSddnS)]ECD)]"; //$NON-NLS-1$
		}

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
	
	class TestVisitor extends ASTVisitor {
		
		boolean visitTheKids = true;
		
		boolean visitDocTags;
		
		TestVisitor() {
			this(false);
		}
		
		TestVisitor(boolean visitDocTags) {
			super(visitDocTags);
			this.visitDocTags = visitDocTags;
		}
		
		public boolean isVisitingChildren() {
			return visitTheKids;
		}

		public void setVisitingChildren(boolean visitChildren) {
			visitTheKids = visitChildren;
		}

		// NAMES

		public boolean visit(SimpleName node) {
			b.append("(nS"); //$NON-NLS-1$
			b.append(node.getIdentifier());
			return isVisitingChildren();
		}
		public void endVisit(SimpleName node) {
			b.append(node.getIdentifier());
			b.append("nS)"); //$NON-NLS-1$
		}
		public boolean visit(QualifiedName node) {
			b.append("(nQ"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(QualifiedName node) {
			b.append("nQ)"); //$NON-NLS-1$
		}

		// TYPES

		public boolean visit(SimpleType node) {
			b.append("(tS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SimpleType node) {
			b.append("tS)"); //$NON-NLS-1$
		}
		public boolean visit(ArrayType node) {
			b.append("(tA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayType node) {
			b.append("tA)"); //$NON-NLS-1$
		}
		public boolean visit(PrimitiveType node) {
			b.append("(tP"); //$NON-NLS-1$
			b.append(node.getPrimitiveTypeCode().toString());
			return isVisitingChildren();
		}
		public void endVisit(PrimitiveType node) {
			b.append(node.getPrimitiveTypeCode().toString());
			b.append("tP)"); //$NON-NLS-1$
		}
		public boolean visit(ParameterizedType node) {
			b.append("(tM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ParameterizedType node) {
			b.append("tM)"); //$NON-NLS-1$
		}
		public boolean visit(QualifiedType node) {
			b.append("(tQ"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(QualifiedType node) {
			b.append("tQ)"); //$NON-NLS-1$
		}
		public boolean visit(WildcardType node) {
			b.append("(tW"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(WildcardType node) {
			b.append("tW)"); //$NON-NLS-1$
		}
		
		// EXPRESSIONS and STATEMENTS


		public boolean visit(ArrayAccess node) {
			b.append("(eAA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayAccess node) {
			b.append("eAA)"); //$NON-NLS-1$
		}

		public boolean visit(ArrayCreation node) {
			b.append("(eAC"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayCreation node) {
			b.append("eAC)"); //$NON-NLS-1$
		}

		public boolean visit(ArrayInitializer node) {
			b.append("(eAI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayInitializer node) {
			b.append("eAI)"); //$NON-NLS-1$
		}

		public boolean visit(AssertStatement node) {
			b.append("(sAS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AssertStatement node) {
			b.append("sAS)"); //$NON-NLS-1$
		}

		public boolean visit(Assignment node) {
			b.append("("); //$NON-NLS-1$
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(Assignment node) {
			b.append(node.getOperator().toString());
			b.append(")"); //$NON-NLS-1$
		}

		public boolean visit(Block node) {
			b.append("(sB"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(Block node) {
			b.append("sB)"); //$NON-NLS-1$
		}

		public boolean visit(BooleanLiteral node) {
			b.append("(eBL"); //$NON-NLS-1$
			b.append(node.booleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			return isVisitingChildren();
		}
		public void endVisit(BooleanLiteral node) {
			b.append(node.booleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			b.append("eBL)"); //$NON-NLS-1$
		}

		public boolean visit(BreakStatement node) {
			b.append("(sBR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(BreakStatement node) {
			b.append("sBR)"); //$NON-NLS-1$
		}

		public boolean visit(CastExpression node) {
			b.append("(eCS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CastExpression node) {
			b.append("eCS)"); //$NON-NLS-1$
		}

		public boolean visit(CatchClause node) {
			b.append("(cc"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CatchClause node) {
			b.append("cc)"); //$NON-NLS-1$
		}

		public boolean visit(CharacterLiteral node) {
			b.append("(eCL"); //$NON-NLS-1$
			b.append(node.getEscapedValue());
			return isVisitingChildren();
		}
		public void endVisit(CharacterLiteral node) {
			b.append(node.getEscapedValue());
			b.append("eCL)"); //$NON-NLS-1$
		}

		public boolean visit(ClassInstanceCreation node) {
			b.append("(eCI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ClassInstanceCreation node) {
			b.append("eCI)"); //$NON-NLS-1$
		}
		
		public boolean visit(AnonymousClassDeclaration node) {
			b.append("(ACD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AnonymousClassDeclaration node) {
			b.append("ACD)"); //$NON-NLS-1$
		}

		public boolean visit(CompilationUnit node) {
			b.append("(CU"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CompilationUnit node) {
			b.append("CU)"); //$NON-NLS-1$
		}

		public boolean visit(ConditionalExpression node) {
			b.append("(eCO"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ConditionalExpression node) {
			b.append("eCO)"); //$NON-NLS-1$
		}

		public boolean visit(ConstructorInvocation node) {
			b.append("(sCI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ConstructorInvocation node) {
			b.append("sCI)"); //$NON-NLS-1$
		}

		public boolean visit(ContinueStatement node) {
			b.append("(sCN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ContinueStatement node) {
			b.append("sCN)"); //$NON-NLS-1$
		}

		public boolean visit(DoStatement node) {
			b.append("(sDO"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(DoStatement node) {
			b.append("sDO)"); //$NON-NLS-1$
		}

		public boolean visit(EmptyStatement node) {
			b.append("(sEM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EmptyStatement node) {
			b.append("sEM)"); //$NON-NLS-1$
		}

		public boolean visit(EnhancedForStatement node) {
			b.append("(sEFR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EnhancedForStatement node) {
			b.append("sEFR)"); //$NON-NLS-1$
		}

		public boolean visit(EnumConstantDeclaration node) {
			b.append("(ECD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EnumConstantDeclaration node) {
			b.append("ECD)"); //$NON-NLS-1$
		}

		public boolean visit(EnumDeclaration node) {
			b.append("(ED"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EnumDeclaration node) {
			b.append("ED)"); //$NON-NLS-1$
		}

		public boolean visit(ExpressionStatement node) {
			b.append("(sEX"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ExpressionStatement node) {
			b.append("sEX)"); //$NON-NLS-1$
		}

		public boolean visit(FieldAccess node) {
			b.append("(eFA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(FieldAccess node) {
			b.append("eFA)"); //$NON-NLS-1$
		}

		public boolean visit(FieldDeclaration node) {
			b.append("(FD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(FieldDeclaration node) {
			b.append("FD)"); //$NON-NLS-1$
		}

		public boolean visit(ForStatement node) {
			b.append("(sFR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ForStatement node) {
			b.append("sFR)"); //$NON-NLS-1$
		}

		public boolean visit(IfStatement node) {
			b.append("(sIF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(IfStatement node) {
			b.append("sIF)"); //$NON-NLS-1$
		}

		public boolean visit(ImportDeclaration node) {
			b.append("(ID"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ImportDeclaration node) {
			b.append("ID)"); //$NON-NLS-1$
		}

		public boolean visit(InfixExpression node) {
			b.append("(eIN"); //$NON-NLS-1$
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(InfixExpression node) {
			b.append(node.getOperator().toString());
			b.append("eIN)"); //$NON-NLS-1$
		}

		public boolean visit(InstanceofExpression node) {
			b.append("(eIO"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(InstanceofExpression node) {
			b.append("eIO)"); //$NON-NLS-1$
		}

		public boolean visit(Initializer node) {
			b.append("(IN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(Initializer node) {
			b.append("IN)"); //$NON-NLS-1$
		}

		/**
		 * @deprecated (not really - just suppressing the warnings
		 * that come from testing Javadoc.getComment())
		 *
		 */
		public boolean visit(Javadoc node) {
			b.append("(JD"); //$NON-NLS-1$
			
			// verify that children of Javadoc nodes are visited only if requested
			if (visitDocTags) {
				assertTrue(super.visit(node) == true);
			} else {
				assertTrue(super.visit(node) == false);
			}
			return isVisitingChildren() && super.visit(node);
		}

		/**
		 * @deprecated (not really - just suppressing the warnings
		 * that come from testing Javadoc.getComment())
		 *
		 */
		public void endVisit(Javadoc node) {
			b.append("JD)"); //$NON-NLS-1$
		}

		public boolean visit(BlockComment node) {
			b.append("(/*"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(BlockComment node) {
			b.append("*/)"); //$NON-NLS-1$
		}

		public boolean visit(LineComment node) {
			b.append("(//"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(LineComment node) {
			b.append("//)"); //$NON-NLS-1$
		}

		public boolean visit(TagElement node) {
			b.append("(TG"); //$NON-NLS-1$
			b.append(node.getTagName());
			return isVisitingChildren();
		}
		public void endVisit(TagElement node) {
			b.append(node.getTagName());
			b.append("TG)"); //$NON-NLS-1$
		}

		public boolean visit(TextElement node) {
			b.append("(TX"); //$NON-NLS-1$
			b.append(node.getText());
			return isVisitingChildren();
		}
		public void endVisit(TextElement node) {
			b.append(node.getText());
			b.append("TX)"); //$NON-NLS-1$
		}

		public boolean visit(MemberRef node) {
			b.append("(MBREF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MemberRef node) {
			b.append("MBREF)"); //$NON-NLS-1$
		}

		public boolean visit(MethodRef node) {
			b.append("(MTHREF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodRef node) {
			b.append("MTHREF)"); //$NON-NLS-1$
		}

		public boolean visit(MethodRefParameter node) {
			b.append("(MPARM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodRefParameter node) {
			b.append("MPARM)"); //$NON-NLS-1$
		}

		public boolean visit(LabeledStatement node) {
			b.append("(sLA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(LabeledStatement node) {
			b.append("sLA)"); //$NON-NLS-1$
		}

		public boolean visit(MethodDeclaration node) {
			b.append("(MD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodDeclaration node) {
			b.append("MD)"); //$NON-NLS-1$
		}

		public boolean visit(MethodInvocation node) {
			b.append("(eMI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodInvocation node) {
			b.append("eMI)"); //$NON-NLS-1$
		}

		public boolean visit(NullLiteral node) {
			b.append("(eNL"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(NullLiteral node) {
			b.append("eNL)"); //$NON-NLS-1$
		}

		public boolean visit(NumberLiteral node) {
			b.append("(eNU"); //$NON-NLS-1$
			b.append(node.getToken());
			return isVisitingChildren();
		}
		public void endVisit(NumberLiteral node) {
			b.append(node.getToken());
			b.append("eNU)"); //$NON-NLS-1$
		}

		public boolean visit(PackageDeclaration node) {
			b.append("(PD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(PackageDeclaration node) {
			b.append("PD)"); //$NON-NLS-1$
		}

		public boolean visit(ParenthesizedExpression node) {
			b.append("(ePA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ParenthesizedExpression node) {
			b.append("ePA)"); //$NON-NLS-1$
		}

		public boolean visit(PostfixExpression node) {
			b.append("(ePO"); //$NON-NLS-1$
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(PostfixExpression node) {
			b.append(node.getOperator().toString());
			b.append("ePO)"); //$NON-NLS-1$
		}

		public boolean visit(PrefixExpression node) {
			b.append("(ePR"); //$NON-NLS-1$
			b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(PrefixExpression node) {
			b.append(node.getOperator().toString());
			b.append("ePR)"); //$NON-NLS-1$
		}

		public boolean visit(ReturnStatement node) {
			b.append("(sRT"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ReturnStatement node) {
			b.append("sRT)"); //$NON-NLS-1$
		}

		public boolean visit(SingleVariableDeclaration node) {
			b.append("(VD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SingleVariableDeclaration node) {
			b.append("VD)"); //$NON-NLS-1$
		}

		public boolean visit(StringLiteral node) {
			b.append("(eSL"); //$NON-NLS-1$
			b.append(node.getLiteralValue());
			return isVisitingChildren();
		}
		public void endVisit(StringLiteral node) {
			b.append(node.getLiteralValue());
			b.append("eSL)"); //$NON-NLS-1$
		}

		public boolean visit(SuperConstructorInvocation node) {
			b.append("(sSC"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperConstructorInvocation node) {
			b.append("sSC)"); //$NON-NLS-1$
		}

		public boolean visit(SuperFieldAccess node) {
			b.append("(eSF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperFieldAccess node) {
			b.append("eSF)"); //$NON-NLS-1$
		}

		public boolean visit(SuperMethodInvocation node) {
			b.append("(eSM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperMethodInvocation node) {
			b.append("eSM)"); //$NON-NLS-1$
		}

		public boolean visit(SwitchCase node) {
			b.append("(sSC"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SwitchCase node) {
			b.append("sSC)"); //$NON-NLS-1$
		}

		public boolean visit(SwitchStatement node) {
			b.append("(sSW"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SwitchStatement node) {
			b.append("sSW)"); //$NON-NLS-1$
		}

		public boolean visit(SynchronizedStatement node) {
			b.append("(sSY"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SynchronizedStatement node) {
			b.append("sSY)"); //$NON-NLS-1$
		}

		public boolean visit(ThisExpression node) {
			b.append("(eTH"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ThisExpression node) {
			b.append("eTH)"); //$NON-NLS-1$
		}

		public boolean visit(ThrowStatement node) {
			b.append("(sTR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ThrowStatement node) {
			b.append("sTR)"); //$NON-NLS-1$
		}

		public boolean visit(TryStatement node) {
			b.append("(sTY"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TryStatement node) {
			b.append("sTY)"); //$NON-NLS-1$
		}

		public boolean visit(TypeDeclaration node) {
			b.append("(TD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeDeclaration node) {
			b.append("TD)"); //$NON-NLS-1$
		}

		public boolean visit(TypeDeclarationStatement node) {
			b.append("(sTD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeDeclarationStatement node) {
			b.append("sTD)"); //$NON-NLS-1$
		}

		public boolean visit(TypeLiteral node) {
			b.append("(eTL"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeLiteral node) {
			b.append("eTL)"); //$NON-NLS-1$
		}

		public boolean visit(TypeParameter node) {
			b.append("(tTP"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeParameter node) {
			b.append("tTP)"); //$NON-NLS-1$
		}

		public boolean visit(VariableDeclarationExpression node) {
			b.append("(eVD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationExpression node) {
			b.append("eVD)"); //$NON-NLS-1$
		}

		public boolean visit(VariableDeclarationFragment node) {
			b.append("(VS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationFragment node) {
			b.append("VS)"); //$NON-NLS-1$
		}

		public boolean visit(VariableDeclarationStatement node) {
			b.append("(sVD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationStatement node) {
			b.append("sVD)"); //$NON-NLS-1$
		}

		public boolean visit(WhileStatement node) {
			b.append("(sWH"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(WhileStatement node) {
			b.append("sWH)"); //$NON-NLS-1$
		}

		public boolean visit(AnnotationTypeDeclaration node) {
			b.append("(@TD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AnnotationTypeDeclaration node) {
			b.append("@TD)"); //$NON-NLS-1$
		}

		public boolean visit(AnnotationTypeMemberDeclaration node) {
			b.append("(@MD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			b.append("@MD)"); //$NON-NLS-1$
		}

		public boolean visit(NormalAnnotation node) {
			b.append("(@NAN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(NormalAnnotation node) {
			b.append("@NAN)"); //$NON-NLS-1$
		}

		public boolean visit(MarkerAnnotation node) {
			b.append("(@MAN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MarkerAnnotation node) {
			b.append("@MAN)"); //$NON-NLS-1$
		}

		public boolean visit(SingleMemberAnnotation node) {
			b.append("(@SMAN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SingleMemberAnnotation node) {
			b.append("@SMAN)"); //$NON-NLS-1$
		}

		public boolean visit(MemberValuePair node) {
			b.append("(@MVP"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MemberValuePair node) {
			b.append("@MVP)"); //$NON-NLS-1$
		}

		public boolean visit(Modifier node) {
			b.append("(MOD"); //$NON-NLS-1$
			b.append(node.getKeyword().toString());
			return isVisitingChildren();
		}
		public void endVisit(Modifier node) {
			b.append(node.getKeyword().toString());
			b.append("MOD)"); //$NON-NLS-1$
		}

		public void preVisit(ASTNode node) {
			b.append("["); //$NON-NLS-1$
		}

		public void postVisit(ASTNode node) {
			b.append("]"); //$NON-NLS-1$
		}

	}	
	// NAMES
	public void testSimpleName() {
		Name x1 = ast.newName(new String[]{"Z"}); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(nSZZnS)]".equals(result)); //$NON-NLS-1$
	}

	public void testQualifiedName() {
		Name x1 = ast.newName(new String[]{"X", "Y"}); //$NON-NLS-1$ //$NON-NLS-2$
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(nQ[(nSXXnS)][(nSYYnS)]nQ)]".equals(result)); //$NON-NLS-1$
	}

	
	// TYPES
	public void testPrimitiveType() {
		Type x1 = ast.newPrimitiveType(PrimitiveType.CHAR);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(tPcharchartP)]".equals(result)); //$NON-NLS-1$
	}

	public void testSimpleType() {
		Type x1 = ast.newSimpleType(ast.newName(new String[]{"Z"})); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(tS[(nSZZnS)]tS)]".equals(result)); //$NON-NLS-1$
	}

	public void testArrayType() {
		Type x0 = ast.newPrimitiveType(PrimitiveType.CHAR);
		Type x1 = ast.newArrayType(x0);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(tA[(tPcharchartP)]tA)]".equals(result)); //$NON-NLS-1$
	}

	/** @deprecated using deprecated code */
	public void testParameterizedType() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		ParameterizedType x1 = ast.newParameterizedType(T1);
		x1.typeArguments().add(T2);
		x1.typeArguments().add(PT1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(tM"+T1S+T2S+PT1S+"tM)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testQualifiedType() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		QualifiedType x1 = ast.newQualifiedType(T1, N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(tQ"+T1S+N1S+"tQ)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testWildcardType() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		WildcardType x1 = ast.newWildcardType();
		x1.setBound(T1, true);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(tW"+T1S+"tW)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(eAA"+E1S+E2S+"eAA)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(eAC"+"[(tA"+T1S+"tA)]"+E1S+E2S+"[(eAIeAI)]eAC)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	public void testArrayInitializer() {
		ArrayInitializer x1 = ast.newArrayInitializer();
		x1.expressions().add(E1);
		x1.expressions().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eAI"+E1S+E2S+"eAI)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testAssertStatement() {
		AssertStatement x1 = ast.newAssertStatement();
		x1.setExpression(E1);
		x1.setMessage(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sAS"+E1S+E2S+"sAS)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testAssignment() {
		Assignment x1 = ast.newAssignment();
		x1.setLeftHandSide(E1);
		x1.setRightHandSide(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(="+E1S+E2S+"=)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testBlock() {
		Block x1 = ast.newBlock();
		x1.statements().add(S1);
		x1.statements().add(S2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sB"+S1S+S2S+"sB)]")); //$NON-NLS-1$ //$NON-NLS-2$

		// check that visiting children can be cut off
		v1.setVisitingChildren(false);
		b.setLength(0);
		x1.accept(v1);
		result = b.toString();
		assertTrue(result.equals("[(sBsB)]")); //$NON-NLS-1$
	}

	public void testBlockComment() {
		BlockComment x1 = ast.newBlockComment();
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(/**/)]".equals(result)); //$NON-NLS-1$
	}

	public void testBooleanLiteral() {
		BooleanLiteral x1 = ast.newBooleanLiteral(true);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eBLtruetrueeBL)]")); //$NON-NLS-1$
	}
	public void testBreakStatement() {
		BreakStatement x1 = ast.newBreakStatement();
		x1.setLabel(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sBR"+N1S+"sBR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCastExpression() {
		CastExpression x1 = ast.newCastExpression();
		x1.setType(T1);
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eCS"+T1S+E1S+"eCS)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCatchClause() {
		CatchClause x1 = ast.newCatchClause();
		x1.setException(V1);
		x1.setBody(B1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(cc"+V1S+B1S+"cc)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCharacterLiteral() {
		CharacterLiteral x1 = ast.newCharacterLiteral();
		x1.setCharValue('q');
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eCL'q''q'eCL)]")); //$NON-NLS-1$
	}
	/** @deprecated using deprecated code */
	public void testClassInstanceCreation() {
		ClassInstanceCreation x1 = ast.newClassInstanceCreation();
		x1.setExpression(E1);
		if (ast.apiLevel() == AST.JLS2) {
			x1.setName(N1);
		} else {
			x1.typeArguments().add(PT1);
			x1.setType(T1);
		}
		x1.setAnonymousClassDeclaration(ACD1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eCI"+E1S+N1S+ACD1S+"eCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eCI"+E1S+PT1S+T1S+ACD1S+"eCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testAnonymousClassDeclaration() {
		AnonymousClassDeclaration x1 = ast.newAnonymousClassDeclaration();
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ACD"+FD1S+FD2S+"ACD)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(CU"+PD1S+ID1S+ID2S+TD1S+TD2S+"CU)]")); //$NON-NLS-1$ //$NON-NLS-2$

		// check that visiting children can be cut off
		v1.setVisitingChildren(false);
		b.setLength(0);
		x1.accept(v1);
		result = b.toString();
		assertTrue(result.equals("[(CUCU)]")); //$NON-NLS-1$
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
		assertTrue(result.equals("[(eCO"+E1S+E2S+N1S+"eCO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testConstructorInvocation() {
		ConstructorInvocation x1 = ast.newConstructorInvocation();
		if (ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(PT1);
		}
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(sCI"+E1S+E2S+"sCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(sCI"+PT1S+E1S+E2S+"sCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testContinueStatement() {
		ContinueStatement x1 = ast.newContinueStatement();
		x1.setLabel(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sCN"+N1S+"sCN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testDoStatement() {
		DoStatement x1 = ast.newDoStatement();
		x1.setExpression(E1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sDO"+S1S+E1S+"sDO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testEmptyStatement() {
		EmptyStatement x1 = ast.newEmptyStatement();
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sEMsEM)]")); //$NON-NLS-1$
	}
	/** @deprecated Only to suppress warnings for refs to bodyDeclarations. */
	// TODO (jeem) - remove deprecation after 3.1 M4
	public void testEnumConstantDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		EnumConstantDeclaration x1 = ast.newEnumConstantDeclaration();
		x1.setJavadoc(JD1);
		x1.modifiers().add(MOD1);
		x1.modifiers().add(MOD2);
		x1.setName(N1);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		x1.setAnonymousClassDeclaration(ACD1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ECD"+JD1S+MOD1S+MOD2S+N1S+E1S+E2S+ACD1S+"ECD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testEnumDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		EnumDeclaration x1 = ast.newEnumDeclaration();
		x1.setJavadoc(JD1);
		x1.modifiers().add(MOD1);
		x1.modifiers().add(MOD2);
		x1.setName(N1);
		x1.superInterfaceTypes().add(T1);
		x1.superInterfaceTypes().add(T2);
		x1.enumConstants().add(EC1);
		x1.enumConstants().add(EC2);
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ED"+JD1S+MOD1S+MOD2S+N1S+T1S+T2S+EC1S+EC2S+FD1S+FD2S+"ED)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testExpressionStatement() {
		ExpressionStatement x1 = ast.newExpressionStatement(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sEX"+E1S+"sEX)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testFieldAccess() {
		FieldAccess x1 = ast.newFieldAccess();
		x1.setExpression(E1);
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eFA"+E1S+N1S+"eFA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testFieldDeclaration() {
		FieldDeclaration x1 = ast.newFieldDeclaration(W1);
		x1.setJavadoc(JD1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
		}
		x1.setType(T1);
		x1.fragments().add(W2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(FD"+JD1S+T1S+W1S+W2S+"FD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(FD"+JD1S+MOD1S+MOD2S+T1S+W1S+W2S+"FD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
		assertTrue(result.equals("[(sFR"+E1S+E2S+N1S+N2S+N3S+S1S+"sFR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testEnhancedForStatement() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		EnhancedForStatement x1 = ast.newEnhancedForStatement();
		x1.setParameter(V1);
		x1.setExpression(E1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sEFR"+V1S+E1S+S1S+"sEFR)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(sIF"+E1S+S1S+S2S+"sIF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testImportDeclaration() {
		ImportDeclaration x1 = ast.newImportDeclaration();
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ID"+N1S+"ID)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(eIN+"+E1S+E2S+N1S+N2S+"+eIN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testInstanceofExpression() {
		InstanceofExpression x1 = ast.newInstanceofExpression();
		x1.setLeftOperand(E1);
		x1.setRightOperand(T1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eIO"+E1S+T1S+"eIO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testInitializer() {
		Initializer x1 = ast.newInitializer();
		x1.setJavadoc(JD1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
		}
		x1.setBody(B1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(IN"+JD1S+B1S+"IN)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(IN"+JD1S+MOD1S+MOD2S+B1S+"IN)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * @deprecated (not really - just suppressing the warnings
	 * that come from testing Javadoc.getComment())
	 *
	 */
	public void testJavadoc() {
		Javadoc x1 = ast.newJavadoc();
		x1.tags().add(TAG1);
		
		// ASTVisitor() does not visit doc tags
		{
			TestVisitor v1 = new TestVisitor();
			b.setLength(0);
			x1.accept(v1);
			String result = b.toString();
			assertTrue(("[(JDJD)]").equals(result)); //$NON-NLS-1$
		}
		
		// ASTVisitor(false) does not visit doc tags
		{
			TestVisitor v1 = new TestVisitor(false);
			b.setLength(0);
			x1.accept(v1);
			String result = b.toString();
			assertTrue(("[(JDJD)]").equals(result)); //$NON-NLS-1$
		}
		
		// ASTVisitor(true) does visit doc tags
		{
			TestVisitor v1 = new TestVisitor(true);
			b.setLength(0);
			x1.accept(v1);
			String result = b.toString();
			assertTrue(("[(JD"+TAG1S+"JD)]").equals(result)); //$NON-NLS-1$
		}
	}

	public void testLabeledStatement() {
		LabeledStatement x1 = ast.newLabeledStatement();
		x1.setLabel(N1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sLA"+N1S+S1S+"sLA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testLineComment() {
		LineComment x1 = ast.newLineComment();
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(////)]".equals(result)); //$NON-NLS-1$
	}

	public void testMemberRef() {
		MemberRef x1 = ast.newMemberRef();
		x1.setQualifier(N1);
		x1.setName(N2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(MBREF"+N1S+N2S+"MBREF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testMethodDeclaration() {
		MethodDeclaration x1 = ast.newMethodDeclaration();
		x1.setJavadoc(JD1);
		if (ast.apiLevel() == AST.JLS2) {
			x1.setReturnType(T1);
		} else {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
			x1.typeParameters().add(TP1);
			x1.setReturnType2(T1);
		}
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
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(MD"+JD1S+T1S+N1S+V1S+V2S+N2S+N3S+B1S+"MD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(MD"+JD1S+MOD1S+MOD2S+TP1S+T1S+N1S+V1S+V2S+N2S+N3S+B1S+"MD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/** @deprecated using deprecated code */
	public void testMethodInvocation() {
		MethodInvocation x1 = ast.newMethodInvocation();
		x1.setExpression(N1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(PT1);
		}
		x1.setName(N2);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eMI"+N1S+N2S+E1S+E2S+"eMI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eMI"+N1S+PT1S+N2S+E1S+E2S+"eMI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testMethodRef() {
		MethodRef x1 = ast.newMethodRef();
		x1.setQualifier(N1);
		x1.setName(N2);
		x1.parameters().add(MPARM1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(MTHREF"+N1S+N2S+MPARM1S+"MTHREF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testMethodRefParameter() {
		MethodRefParameter x1 = ast.newMethodRefParameter();
		x1.setType(T1);
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(MPARM"+T1S+N1S+"MPARM)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testModifier() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		Modifier x1 = ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(MODprivateprivateMOD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testNormalAnnotation() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		NormalAnnotation x1 = ast.newNormalAnnotation();
		x1.setTypeName(N1);
		x1.values().add(MVP1);
		x1.values().add(MVP2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(@NAN"+N1S+MVP1S+MVP2S+"@NAN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testMemberValuePair() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		MemberValuePair x1 = ast.newMemberValuePair();
		x1.setName(N1);
		x1.setValue(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(@MVP"+N1S+E1S+"@MVP)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testMarkerAnnotation() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		MarkerAnnotation x1 = ast.newMarkerAnnotation();
		x1.setTypeName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(@MAN"+N1S+"@MAN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testSingleMemberAnnotation() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		SingleMemberAnnotation x1 = ast.newSingleMemberAnnotation();
		x1.setTypeName(N1);
		x1.setValue(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(@SMAN"+N1S+E1S+"@SMAN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testAnnotationTypeDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		AnnotationTypeDeclaration x1 = ast.newAnnotationTypeDeclaration();
		x1.setJavadoc(JD1);
		x1.modifiers().add(MOD1);
		x1.modifiers().add(MOD2);
		x1.setName(N1);
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(@TD"+JD1S+MOD1S+MOD2S+N1S+FD1S+FD2S+"@TD)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/** @deprecated using deprecated code */
	public void testAnnotationTypeMemberDeclaration() {
		if (ast.apiLevel() == AST.JLS2) {
			return;
		}
		AnnotationTypeMemberDeclaration x1 = ast.newAnnotationTypeMemberDeclaration();
		x1.setJavadoc(JD1);
		x1.modifiers().add(MOD1);
		x1.modifiers().add(MOD2);
		x1.setType(T1);
		x1.setName(N1);
		x1.setDefault(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(@MD"+JD1S+MOD1S+MOD2S+T1S+N1S+E1S+"@MD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testNullLiteral() {
		NullLiteral x1 = ast.newNullLiteral();
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eNLeNL)]")); //$NON-NLS-1$
	}
	public void testNumberLiteral() {
		NumberLiteral x1 = ast.newNumberLiteral("1.0"); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eNU1.01.0eNU)]")); //$NON-NLS-1$
	}
	/** @deprecated using deprecated code */
	public void testPackageDeclaration() {
		PackageDeclaration x1 = ast.newPackageDeclaration();
		if (ast.apiLevel() >= AST.JLS3) {
			x1.setJavadoc(JD1);
			x1.annotations().add(ANO1);
			x1.annotations().add(ANO2);
		}
		x1.setName(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(PD"+N1S+"PD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(PD"+JD1S+ANO1S+ANO2S+N1S+"PD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testParenthesizedExpression() {
		ParenthesizedExpression x1 = ast.newParenthesizedExpression();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ePA"+E1S+"ePA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testPostfixExpression() {
		PostfixExpression x1 = ast.newPostfixExpression();
		x1.setOperand(E1);
		x1.setOperator(PostfixExpression.Operator.INCREMENT);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ePO++"+E1S+"++ePO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testPrefixExpression() {
		PrefixExpression x1 = ast.newPrefixExpression();
		x1.setOperand(E1);
		x1.setOperator(PrefixExpression.Operator.INCREMENT);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(ePR++"+E1S+"++ePR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testReturnStatement() {
		ReturnStatement x1 = ast.newReturnStatement();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sRT"+E1S+"sRT)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testStringLiteral() {
		StringLiteral x1 = ast.newStringLiteral();
		x1.setLiteralValue("H"); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eSLHHeSL)]")); //$NON-NLS-1$
	}
	/** @deprecated using deprecated code */
	public void testSuperConstructorInvocation() {
		SuperConstructorInvocation x1 = ast.newSuperConstructorInvocation();
		x1.setExpression(N1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(PT1);
		}
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(sSC"+N1S+E1S+E2S+"sSC)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(sSC"+N1S+PT1S+E1S+E2S+"sSC)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testSuperFieldAccess() {
		SuperFieldAccess x1 = ast.newSuperFieldAccess();
		x1.setQualifier(N1);
		x1.setName(N2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eSF"+N1S+N2S+"eSF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testSuperMethodInvocation() {
		SuperMethodInvocation x1 = ast.newSuperMethodInvocation();
		x1.setQualifier(N1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(PT1);
		}
		x1.setName(N2);
		x1.arguments().add(E1);
		x1.arguments().add(E2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eSM"+N1S+N2S+E1S+E2S+"eSM)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eSM"+N1S+PT1S+N2S+E1S+E2S+"eSM)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testSwitchCase() {
		SwitchCase x1 = ast.newSwitchCase();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sSC"+E1S+"sSC)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(sSW"+E1S+S1S+S2S+"sSW)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testSynchronizedStatement() {
		SynchronizedStatement x1 = ast.newSynchronizedStatement();
		x1.setExpression(E1);
		x1.setBody(B1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sSY"+E1S+B1S+"sSY)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTagElement() {
		TagElement x1 = ast.newTagElement();
		x1.setTagName("x"); //$NON-NLS-1$
		x1.fragments().add(TAG1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(("[(TGx"+TAG1S+"xTG)]").equals(result)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTextElement() {
		TextElement x1 = ast.newTextElement();
		x1.setText("x"); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue("[(TXxxTX)]".equals(result)); //$NON-NLS-1$
	}

	public void testThisExpression() {
		ThisExpression x1 = ast.newThisExpression();
		x1.setQualifier(N1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eTH"+N1S+"eTH)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testThrowStatement() {
		ThrowStatement x1 = ast.newThrowStatement();
		x1.setExpression(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sTR"+E1S+"sTR)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		assertTrue(result.equals("[(sTY"+B1S+"[(cc"+V1S+"[(sBsB)]"+"cc)]"+"[(cc"+V2S+"[(sBsB)]"+"cc)]"+"[(sBsB)]"+"sTY)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	}
	/** @deprecated using deprecated code */
	public void testTypeDeclaration() {
		TypeDeclaration x1 = ast.newTypeDeclaration();
		x1.setJavadoc(JD1);
		x1.setName(N1);
		if (ast.apiLevel() == AST.JLS2) {
			x1.setSuperclass(N2);
			x1.superInterfaces().add(N3);
			x1.superInterfaces().add(N4);
		} else {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
			x1.typeParameters().add(TP1);
			x1.setSuperclassType(PT1);
			x1.superInterfaceTypes().add(T1);
			x1.superInterfaceTypes().add(T2); //$NON-NLS-1$
		}
		x1.bodyDeclarations().add(FD1);
		x1.bodyDeclarations().add(FD2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(TD"+JD1S+N1S+N2S+N3S+N4S+FD1S+FD2S+"TD)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			assertTrue(result.equals("[(TD"+JD1S+MOD1S+MOD2S+N1S+TP1S+PT1S+T1S+T2S+FD1S+FD2S+"TD)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	public void testTypeDeclarationStatement() {
		TypeDeclarationStatement x1 = ast.newTypeDeclarationStatement(TD1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sTD"+TD1S+"sTD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTypeLiteral() {
		TypeLiteral x1 = ast.newTypeLiteral();
		x1.setType(T1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(eTL"+T1S+"eTL)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testSingleVariableDeclaration() {
		SingleVariableDeclaration x1 = ast.newSingleVariableDeclaration();
		if (ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
		}
		x1.setType(T1);
		x1.setName(N1);
		x1.setInitializer(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(VD"+T1S+N1S+E1S+"VD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(VD"+MOD1S+MOD2S+T1S+N1S+E1S+"VD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testVariableDeclarationFragment() {
		VariableDeclarationFragment x1 = ast.newVariableDeclarationFragment();
		x1.setName(N1);
		x1.setInitializer(E1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(VS"+N1S+E1S+"VS)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testVariableDeclarationExpression() {
		VariableDeclarationExpression x1 = ast.newVariableDeclarationExpression(W1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
		}
		x1.setType(T1);
		x1.fragments().add(W2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eVD"+T1S+W1S+W2S+"eVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eVD"+MOD1S+MOD2S+T1S+W1S+W2S+"eVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/** @deprecated using deprecated code */
	public void testVariableDeclarationStatement() {
		VariableDeclarationStatement x1 = ast.newVariableDeclarationStatement(W1);
		if (ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(MOD1);
			x1.modifiers().add(MOD2);
		}
		x1.setType(T1);
		x1.fragments().add(W2);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		if (ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(sVD"+T1S+W1S+W2S+"sVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(sVD"+MOD1S+MOD2S+T1S+W1S+W2S+"sVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testWhileStatement() {
		WhileStatement x1 = ast.newWhileStatement();
		x1.setExpression(E1);
		x1.setBody(S1);
		TestVisitor v1 = new TestVisitor();
		b.setLength(0);
		x1.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[(sWH"+E1S+S1S+"sWH)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testPrePost() {
		SimpleName n1 = ast.newSimpleName("a"); //$NON-NLS-1$
		SimpleName n2 = ast.newSimpleName("b"); //$NON-NLS-1$
		QualifiedName q = ast.newQualifiedName(n1, n2);
		TestVisitor v1 = new TestVisitor() {
			public void preVisit(ASTNode node) {
				b.append("["); //$NON-NLS-1$
				switch (node.getNodeType()) {
					case ASTNode.QUALIFIED_NAME :
						b.append("q"); //$NON-NLS-1$
						break;
					case ASTNode.SIMPLE_NAME :
						b.append(((SimpleName) node).getIdentifier());
						break;
				}
			}

			public void postVisit(ASTNode node) {
				switch (node.getNodeType()) {
					case ASTNode.QUALIFIED_NAME :
						b.append("q"); //$NON-NLS-1$
						break;
					case ASTNode.SIMPLE_NAME :
						b.append(((SimpleName) node).getIdentifier());
						break;
				}
				b.append("]"); //$NON-NLS-1$
			}
		};

		b.setLength(0);
		q.accept(v1);
		String result = b.toString();
		assertTrue(result.equals("[q(nQ" + "[a(nSaanS)a]" + "[b(nSbbnS)b]" + "nQ)q]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}	
	public void testTraverseAndModify() {
		final TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
		typeDeclaration.setName(N1);
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName("M1")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(0, methodDeclaration);
		final MethodDeclaration methodDeclaration2 = ast.newMethodDeclaration();
		methodDeclaration2.setName(ast.newSimpleName("M2")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(1, methodDeclaration2);
		MethodDeclaration methodDeclaration3 = ast.newMethodDeclaration();
		methodDeclaration3.setName(ast.newSimpleName("M3")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(2, methodDeclaration3);
		// insert a new before the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(MethodDeclaration node) {
				if (node == methodDeclaration2) {
					MethodDeclaration methodDeclaration4 = ast.newMethodDeclaration();
					methodDeclaration4.setName(ast.newSimpleName("M4")); //$NON-NLS-1$
					typeDeclaration.bodyDeclarations().add(0, methodDeclaration4);
				}
				return super.visit(node);
			}			
		};
		b.setLength(0);
		typeDeclaration.accept(v1);
		assertEquals("wrong output", "[(TD[(nSNNnS)][(MD[(tPvoidvoidtP)][(nSM1M1nS)]MD)][(MD[(tPvoidvoidtP)][(nSM2M2nS)]MD)][(MD[(tPvoidvoidtP)][(nSM3M3nS)]MD)]TD)]", b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTraverseAndModify_2() {
		final TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
		typeDeclaration.setName(N1);
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		methodDeclaration.setName(ast.newSimpleName("M1")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(0, methodDeclaration);
		final MethodDeclaration methodDeclaration2 = ast.newMethodDeclaration();
		methodDeclaration2.setName(ast.newSimpleName("M2")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(1, methodDeclaration2);
		MethodDeclaration methodDeclaration3 = ast.newMethodDeclaration();
		methodDeclaration3.setName(ast.newSimpleName("M3")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(2, methodDeclaration3);
		// insert a new after the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(MethodDeclaration node) {
				if (node == methodDeclaration2) {
					MethodDeclaration methodDeclaration4 = ast.newMethodDeclaration();
					methodDeclaration4.setName(ast.newSimpleName("M4")); //$NON-NLS-1$
					typeDeclaration.bodyDeclarations().add(3, methodDeclaration4);
				}
				return super.visit(node);
			}			
		};
		b.setLength(0);
		typeDeclaration.accept(v1);
		assertEquals("wrong output", "[(TD[(nSNNnS)][(MD[(tPvoidvoidtP)][(nSM1M1nS)]MD)][(MD[(tPvoidvoidtP)][(nSM2M2nS)]MD)][(MD[(tPvoidvoidtP)][(nSM3M3nS)]MD)][(MD[(tPvoidvoidtP)][(nSM4M4nS)]MD)]TD)]", b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTraverseAndModify_3() {
		final InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setLeftOperand(ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(ast.newNumberLiteral("10")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// insert a new after the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(SimpleName node) {
				infixExpression.setRightOperand(ast.newNumberLiteral("22")); //$NON-NLS-1$
				return super.visit(node);
			}			
		};
		b.setLength(0);
		infixExpression.accept(v1);
		assertEquals("wrong output", "[(eIN+[(nSiinS)][(eNU2222eNU)]+eIN)]", b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTraverseAndModify_4() {
		final InfixExpression infixExpression = ast.newInfixExpression();
		infixExpression.setLeftOperand(ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(ast.newNumberLiteral("10")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// insert a new before the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			public boolean visit(NumberLiteral node) {
				infixExpression.setLeftOperand(ast.newSimpleName("j")); //$NON-NLS-1$
				return super.visit(node);
			}			
		};
		b.setLength(0);
		infixExpression.accept(v1);
		assertEquals("wrong output", "[(eIN+[(nSiinS)][(eNU1010eNU)]+eIN)]", b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
