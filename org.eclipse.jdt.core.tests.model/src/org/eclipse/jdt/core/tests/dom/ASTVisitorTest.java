/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;

@SuppressWarnings("rawtypes")
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
				suite.addTest(new ASTVisitorTest(methods[i].getName(), AST.JLS4));
				suite.addTest(new ASTVisitorTest(methods[i].getName(), getJLS8()));
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
	Type T3;
	String T3S;
	Type T4;
	String T4S;

	final StringBuffer b = new StringBuffer();

	int API_LEVEL;

	public ASTVisitorTest(String name, int apiLevel) {
		super(name);
		this.API_LEVEL = apiLevel;
	}

	public ASTVisitorTest(String name) {
		super(name.substring(0, name.indexOf(" - JLS")));
		name.indexOf(" - JLS");
		this.API_LEVEL = Integer.parseInt(name.substring(name.indexOf(" - JLS") + 6));
	}

	/** @deprecated using deprecated code */
	public String getName() {
		String name = super.getName() + " - JLS" + this.API_LEVEL;
		return name;
	}

	/**
	 * @deprecated (not really - just suppressing the warnings
	 * that come from testing Javadoc.getComment())
	 *
	 */
	protected void setUp() throws Exception {
		super.setUp();

		this.ast = AST.newAST(this.API_LEVEL, true);
		this.N1 = this.ast.newSimpleName("N"); //$NON-NLS-1$
		this.N1S = "[(nSNNnS)]"; //$NON-NLS-1$
		this.N2 = this.ast.newSimpleName("M"); //$NON-NLS-1$
		this.N2S = "[(nSMMnS)]"; //$NON-NLS-1$
		this.N3 = this.ast.newSimpleName("O"); //$NON-NLS-1$
		this.N3S = "[(nSOOnS)]"; //$NON-NLS-1$
		this.N4 = this.ast.newSimpleName("P"); //$NON-NLS-1$
		this.N4S = "[(nSPPnS)]"; //$NON-NLS-1$
		this.E1 = this.ast.newSimpleName("X"); //$NON-NLS-1$
		this.E1S = "[(nSXXnS)]"; //$NON-NLS-1$
		this.E2 = this.ast.newSimpleName("Y"); //$NON-NLS-1$
		this.E2S = "[(nSYYnS)]"; //$NON-NLS-1$
		this.T1 = this.ast.newSimpleType(this.ast.newSimpleName("Z")); //$NON-NLS-1$
		this.T1S = "[(tS[(nSZZnS)]tS)]"; //$NON-NLS-1$
		this.T2 = this.ast.newSimpleType(this.ast.newSimpleName("X")); //$NON-NLS-1$
		this.T2S = "[(tS[(nSXXnS)]tS)]"; //$NON-NLS-1$
		this.S1 = this.ast.newContinueStatement();
		this.S1S = "[(sCNsCN)]"; //$NON-NLS-1$
		this.S2 = this.ast.newBreakStatement();
		this.S2S = "[(sBRsBR)]"; //$NON-NLS-1$
		this.B1 = this.ast.newBlock();
		this.B1S = "[(sBsB)]"; //$NON-NLS-1$
		this.V1 = this.ast.newSingleVariableDeclaration();
		this.V1.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		this.V1.setName(this.ast.newSimpleName("a")); //$NON-NLS-1$
		this.V1S = "[(VD[(tPintinttP)][(nSaanS)]VD)]"; //$NON-NLS-1$
		this.V2 = this.ast.newSingleVariableDeclaration();
		this.V2.setType(this.ast.newPrimitiveType(PrimitiveType.BYTE));
		this.V2.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		this.V2S = "[(VD[(tPbytebytetP)][(nSbbnS)]VD)]"; //$NON-NLS-1$
		this.W1 = this.ast.newVariableDeclarationFragment();
		this.W1.setName(this.ast.newSimpleName("a")); //$NON-NLS-1$
		this.W1S = "[(VS[(nSaanS)]VS)]"; //$NON-NLS-1$
		this.W2 = this.ast.newVariableDeclarationFragment();
		this.W2.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		this.W2S = "[(VS[(nSbbnS)]VS)]"; //$NON-NLS-1$
		{
			VariableDeclarationFragment temp = this.ast.newVariableDeclarationFragment();
			temp.setName(this.ast.newSimpleName("f")); //$NON-NLS-1$
			this.FD1 = this.ast.newFieldDeclaration(temp);
			this.FD1.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
			this.FD1S = "[(FD[(tPintinttP)][(VS[(nSffnS)]VS)]FD)]"; //$NON-NLS-1$
		}
		{
			VariableDeclarationFragment temp = this.ast.newVariableDeclarationFragment();
			temp.setName(this.ast.newSimpleName("g")); //$NON-NLS-1$
			this.FD2 = this.ast.newFieldDeclaration(temp);
			this.FD2.setType(this.ast.newPrimitiveType(PrimitiveType.CHAR));
			this.FD2S = "[(FD[(tPcharchartP)][(VS[(nSggnS)]VS)]FD)]"; //$NON-NLS-1$
		}
		this.PD1 = this.ast.newPackageDeclaration();
		this.PD1.setName(this.ast.newSimpleName("p")); //$NON-NLS-1$
		this.PD1S = "[(PD[(nSppnS)]PD)]"; //$NON-NLS-1$
		this.ID1 = this.ast.newImportDeclaration();
		this.ID1.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		this.ID1S = "[(ID[(nSiinS)]ID)]"; //$NON-NLS-1$
		this.ID2 = this.ast.newImportDeclaration();
		this.ID2.setName(this.ast.newSimpleName("j")); //$NON-NLS-1$
		this.ID2S = "[(ID[(nSjjnS)]ID)]"; //$NON-NLS-1$
		this.TD1 = this.ast.newTypeDeclaration();
		this.TD1.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		this.TD1S = "[(TD[(nSccnS)]TD)]"; //$NON-NLS-1$
		this.TD2 = this.ast.newTypeDeclaration();
		this.TD2.setName(this.ast.newSimpleName("d")); //$NON-NLS-1$
		this.TD2S = "[(TD[(nSddnS)]TD)]"; //$NON-NLS-1$

		this.ACD1 = this.ast.newAnonymousClassDeclaration();
		this.ACD1S = "[(ACDACD)]"; //$NON-NLS-1$

		this.JD1 = this.ast.newJavadoc();
		this.JD1S = "[(JDJD)]"; //$NON-NLS-1$

		this.LC1 = this.ast.newLineComment();
		this.LC1S = "[(//*//)]"; //$NON-NLS-1$

		this.BC1 = this.ast.newBlockComment();
		this.BC1S = "[(/**/)]"; //$NON-NLS-1$

		this.TAG1 = this.ast.newTagElement();
		this.TAG1.setTagName("@foo"); //$NON-NLS-1$
		this.TAG1S = "[(TG@foo@fooTG)]";  //$NON-NLS-1$

		this.TEXT1 = this.ast.newTextElement();
		this.TEXT1.setText("foo"); //$NON-NLS-1$
		this.TEXT1S = "[(TXfoofooTX)]";  //$NON-NLS-1$

		this.MBREF1 = this.ast.newMemberRef();
		this.MBREF1.setName(this.ast.newSimpleName("p")); //$NON-NLS-1$
		this.MBREF1S = "[(MBREF[(nSppnS)]MBREF)]";  //$NON-NLS-1$

		this.MTHREF1 = this.ast.newMethodRef();
		this.MTHREF1.setName(this.ast.newSimpleName("p")); //$NON-NLS-1$
		this.MTHREF1S = "[(MTHREF[(nSppnS)]MTHREF)]";  //$NON-NLS-1$

		this.MPARM1 = this.ast.newMethodRefParameter();
		this.MPARM1.setType(this.ast.newPrimitiveType(PrimitiveType.CHAR));
		this.MPARM1S = "[(MPARM[(tPcharchartP)]MPARM)]";  //$NON-NLS-1$

		if (this.ast.apiLevel() >= AST.JLS3) {
			this.PT1 = this.ast.newParameterizedType(this.ast.newSimpleType(this.ast.newSimpleName("Z"))); //$NON-NLS-1$
			this.PT1S = "[(tM[(tS[(nSZZnS)]tS)]tM)]"; //$NON-NLS-1$

			this.TP1 = this.ast.newTypeParameter();
			this.TP1.setName(this.ast.newSimpleName("x")); //$NON-NLS-1$
			this.TP1S = "[(tTP[(nSxxnS)]tTP)]"; //$NON-NLS-1$

			this.TP2 = this.ast.newTypeParameter();
			this.TP2.setName(this.ast.newSimpleName("y")); //$NON-NLS-1$
			this.TP2S = "[(tTP[(nSyynS)]tTP)]"; //$NON-NLS-1$

			this.MVP1 = this.ast.newMemberValuePair();
			this.MVP1.setName(this.ast.newSimpleName("x")); //$NON-NLS-1$
			this.MVP1.setValue(this.ast.newSimpleName("y")); //$NON-NLS-1$
			this.MVP1S = "[(@MVP[(nSxxnS)][(nSyynS)]@MVP)]"; //$NON-NLS-1$

			this.MVP2 = this.ast.newMemberValuePair();
			this.MVP2.setName(this.ast.newSimpleName("a")); //$NON-NLS-1$
			this.MVP2.setValue(this.ast.newSimpleName("b")); //$NON-NLS-1$
			this.MVP2S = "[(@MVP[(nSaanS)][(nSbbnS)]@MVP)]"; //$NON-NLS-1$

			this.MOD1 = this.ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
			this.MOD1S = "[(MODpublicpublicMOD)]"; //$NON-NLS-1$
			this.MOD2 = this.ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
			this.MOD2S = "[(MODfinalfinalMOD)]"; //$NON-NLS-1$

			this.ANO1 = this.ast.newMarkerAnnotation();
			this.ANO1.setTypeName(this.ast.newSimpleName("a")); //$NON-NLS-1$
			this.ANO1S = "[(@MAN[(nSaanS)]@MAN)]"; //$NON-NLS-1$

			this.ANO2 = this.ast.newNormalAnnotation();
			this.ANO2.setTypeName(this.ast.newSimpleName("b")); //$NON-NLS-1$
			this.ANO2S = "[(@NAN[(nSbbnS)]@NAN)]"; //$NON-NLS-1$

			this.EC1 = this.ast.newEnumConstantDeclaration();
			this.EC1.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
			this.EC1S = "[(ECD[(nSccnS)]ECD)]"; //$NON-NLS-1$

			this.EC2 = this.ast.newEnumConstantDeclaration();
			this.EC2.setName(this.ast.newSimpleName("d")); //$NON-NLS-1$
			this.EC2S = "[(ECD[(nSddnS)]ECD)]"; //$NON-NLS-1$
		}
		if (this.ast.apiLevel() >= getJLS8()) {
			this.T3 = this.ast.newSimpleType(this.ast.newSimpleName("W")); //$NON-NLS-1$
			this.T3S = "[(tS[(nSWWnS)]tS)]"; //$NON-NLS-1$
			this.T4 = this.ast.newSimpleType(this.ast.newSimpleName("X")); //$NON-NLS-1$
			this.T4S = "[(tS[(nSXXnS)]tS)]"; //$NON-NLS-1$
		}

	}

	protected void tearDown() throws Exception {
		this.ast = null;
		super.tearDown();
	}
	/**
	 * @deprecated
	 */
	protected static int getJLS8() {
		return AST.JLS8;
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
			return this.visitTheKids;
		}

		public void setVisitingChildren(boolean visitChildren) {
			this.visitTheKids = visitChildren;
		}

		// NAMES

		public boolean visit(SimpleName node) {
			ASTVisitorTest.this.b.append("(nS"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getIdentifier());
			return isVisitingChildren();
		}
		public void endVisit(SimpleName node) {
			ASTVisitorTest.this.b.append(node.getIdentifier());
			ASTVisitorTest.this.b.append("nS)"); //$NON-NLS-1$
		}
		public boolean visit(QualifiedName node) {
			ASTVisitorTest.this.b.append("(nQ"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(QualifiedName node) {
			ASTVisitorTest.this.b.append("nQ)"); //$NON-NLS-1$
		}

		// TYPES

		public boolean visit(SimpleType node) {
			ASTVisitorTest.this.b.append("(tS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SimpleType node) {
			ASTVisitorTest.this.b.append("tS)"); //$NON-NLS-1$
		}
		public boolean visit(ArrayType node) {
			ASTVisitorTest.this.b.append("(tA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayType node) {
			ASTVisitorTest.this.b.append("tA)"); //$NON-NLS-1$
		}
		public boolean visit(PrimitiveType node) {
			ASTVisitorTest.this.b.append("(tP"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getPrimitiveTypeCode().toString());
			return isVisitingChildren();
		}
		public void endVisit(PrimitiveType node) {
			ASTVisitorTest.this.b.append(node.getPrimitiveTypeCode().toString());
			ASTVisitorTest.this.b.append("tP)"); //$NON-NLS-1$
		}
		public boolean visit(NameQualifiedType node) {
			ASTVisitorTest.this.b.append("(tPQ"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(NameQualifiedType node) {
			ASTVisitorTest.this.b.append("tPQ)"); //$NON-NLS-1$
		}
		public boolean visit(ParameterizedType node) {
			ASTVisitorTest.this.b.append("(tM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ParameterizedType node) {
			ASTVisitorTest.this.b.append("tM)"); //$NON-NLS-1$
		}
		public boolean visit(QualifiedType node) {
			ASTVisitorTest.this.b.append("(tQ"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(QualifiedType node) {
			ASTVisitorTest.this.b.append("tQ)"); //$NON-NLS-1$
		}
		public boolean visit(UnionType node) {
			ASTVisitorTest.this.b.append("(tU"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(UnionType node) {
			ASTVisitorTest.this.b.append("tU)"); //$NON-NLS-1$
		}
		public boolean visit(WildcardType node) {
			ASTVisitorTest.this.b.append("(tW"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(WildcardType node) {
			ASTVisitorTest.this.b.append("tW)"); //$NON-NLS-1$
		}

		// EXPRESSIONS and STATEMENTS


		public boolean visit(ArrayAccess node) {
			ASTVisitorTest.this.b.append("(eAA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayAccess node) {
			ASTVisitorTest.this.b.append("eAA)"); //$NON-NLS-1$
		}

		public boolean visit(ArrayCreation node) {
			ASTVisitorTest.this.b.append("(eAC"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayCreation node) {
			ASTVisitorTest.this.b.append("eAC)"); //$NON-NLS-1$
		}

		public boolean visit(ArrayInitializer node) {
			ASTVisitorTest.this.b.append("(eAI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ArrayInitializer node) {
			ASTVisitorTest.this.b.append("eAI)"); //$NON-NLS-1$
		}

		public boolean visit(AssertStatement node) {
			ASTVisitorTest.this.b.append("(sAS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AssertStatement node) {
			ASTVisitorTest.this.b.append("sAS)"); //$NON-NLS-1$
		}

		public boolean visit(Assignment node) {
			ASTVisitorTest.this.b.append("("); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(Assignment node) {
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			ASTVisitorTest.this.b.append(")"); //$NON-NLS-1$
		}

		public boolean visit(Block node) {
			ASTVisitorTest.this.b.append("(sB"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(Block node) {
			ASTVisitorTest.this.b.append("sB)"); //$NON-NLS-1$
		}

		public boolean visit(BooleanLiteral node) {
			ASTVisitorTest.this.b.append("(eBL"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.booleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			return isVisitingChildren();
		}
		public void endVisit(BooleanLiteral node) {
			ASTVisitorTest.this.b.append(node.booleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
			ASTVisitorTest.this.b.append("eBL)"); //$NON-NLS-1$
		}

		public boolean visit(BreakStatement node) {
			ASTVisitorTest.this.b.append("(sBR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(BreakStatement node) {
			ASTVisitorTest.this.b.append("sBR)"); //$NON-NLS-1$
		}

		public boolean visit(CastExpression node) {
			ASTVisitorTest.this.b.append("(eCS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CastExpression node) {
			ASTVisitorTest.this.b.append("eCS)"); //$NON-NLS-1$
		}

		public boolean visit(CatchClause node) {
			ASTVisitorTest.this.b.append("(cc"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CatchClause node) {
			ASTVisitorTest.this.b.append("cc)"); //$NON-NLS-1$
		}

		public boolean visit(CharacterLiteral node) {
			ASTVisitorTest.this.b.append("(eCL"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getEscapedValue());
			return isVisitingChildren();
		}
		public void endVisit(CharacterLiteral node) {
			ASTVisitorTest.this.b.append(node.getEscapedValue());
			ASTVisitorTest.this.b.append("eCL)"); //$NON-NLS-1$
		}

		public boolean visit(ClassInstanceCreation node) {
			ASTVisitorTest.this.b.append("(eCI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ClassInstanceCreation node) {
			ASTVisitorTest.this.b.append("eCI)"); //$NON-NLS-1$
		}

		public boolean visit(AnonymousClassDeclaration node) {
			ASTVisitorTest.this.b.append("(ACD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AnonymousClassDeclaration node) {
			ASTVisitorTest.this.b.append("ACD)"); //$NON-NLS-1$
		}

		public boolean visit(CompilationUnit node) {
			ASTVisitorTest.this.b.append("(CU"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CompilationUnit node) {
			ASTVisitorTest.this.b.append("CU)"); //$NON-NLS-1$
		}

		public boolean visit(ConditionalExpression node) {
			ASTVisitorTest.this.b.append("(eCO"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ConditionalExpression node) {
			ASTVisitorTest.this.b.append("eCO)"); //$NON-NLS-1$
		}

		public boolean visit(ConstructorInvocation node) {
			ASTVisitorTest.this.b.append("(sCI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ConstructorInvocation node) {
			ASTVisitorTest.this.b.append("sCI)"); //$NON-NLS-1$
		}

		public boolean visit(ContinueStatement node) {
			ASTVisitorTest.this.b.append("(sCN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ContinueStatement node) {
			ASTVisitorTest.this.b.append("sCN)"); //$NON-NLS-1$
		}

		public boolean visit(DoStatement node) {
			ASTVisitorTest.this.b.append("(sDO"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(DoStatement node) {
			ASTVisitorTest.this.b.append("sDO)"); //$NON-NLS-1$
		}

		public boolean visit(EmptyStatement node) {
			ASTVisitorTest.this.b.append("(sEM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EmptyStatement node) {
			ASTVisitorTest.this.b.append("sEM)"); //$NON-NLS-1$
		}

		public boolean visit(EnhancedForStatement node) {
			ASTVisitorTest.this.b.append("(sEFR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EnhancedForStatement node) {
			ASTVisitorTest.this.b.append("sEFR)"); //$NON-NLS-1$
		}

		public boolean visit(EnumConstantDeclaration node) {
			ASTVisitorTest.this.b.append("(ECD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EnumConstantDeclaration node) {
			ASTVisitorTest.this.b.append("ECD)"); //$NON-NLS-1$
		}

		public boolean visit(EnumDeclaration node) {
			ASTVisitorTest.this.b.append("(ED"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(EnumDeclaration node) {
			ASTVisitorTest.this.b.append("ED)"); //$NON-NLS-1$
		}

		public boolean visit(ExpressionStatement node) {
			ASTVisitorTest.this.b.append("(sEX"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ExpressionStatement node) {
			ASTVisitorTest.this.b.append("sEX)"); //$NON-NLS-1$
		}

		public boolean visit(FieldAccess node) {
			ASTVisitorTest.this.b.append("(eFA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(FieldAccess node) {
			ASTVisitorTest.this.b.append("eFA)"); //$NON-NLS-1$
		}

		public boolean visit(FieldDeclaration node) {
			ASTVisitorTest.this.b.append("(FD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(FieldDeclaration node) {
			ASTVisitorTest.this.b.append("FD)"); //$NON-NLS-1$
		}

		public boolean visit(ForStatement node) {
			ASTVisitorTest.this.b.append("(sFR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ForStatement node) {
			ASTVisitorTest.this.b.append("sFR)"); //$NON-NLS-1$
		}

		public boolean visit(IfStatement node) {
			ASTVisitorTest.this.b.append("(sIF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(IfStatement node) {
			ASTVisitorTest.this.b.append("sIF)"); //$NON-NLS-1$
		}

		public boolean visit(ImportDeclaration node) {
			ASTVisitorTest.this.b.append("(ID"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ImportDeclaration node) {
			ASTVisitorTest.this.b.append("ID)"); //$NON-NLS-1$
		}

		public boolean visit(InfixExpression node) {
			ASTVisitorTest.this.b.append("(eIN"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(InfixExpression node) {
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			ASTVisitorTest.this.b.append("eIN)"); //$NON-NLS-1$
		}

		public boolean visit(InstanceofExpression node) {
			ASTVisitorTest.this.b.append("(eIO"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(InstanceofExpression node) {
			ASTVisitorTest.this.b.append("eIO)"); //$NON-NLS-1$
		}

		public boolean visit(Initializer node) {
			ASTVisitorTest.this.b.append("(IN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(Initializer node) {
			ASTVisitorTest.this.b.append("IN)"); //$NON-NLS-1$
		}

		/**
		 * @deprecated (not really - just suppressing the warnings
		 * that come from testing Javadoc.getComment())
		 *
		 */
		public boolean visit(Javadoc node) {
			ASTVisitorTest.this.b.append("(JD"); //$NON-NLS-1$

			// verify that children of Javadoc nodes are visited only if requested
			if (this.visitDocTags) {
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
			ASTVisitorTest.this.b.append("JD)"); //$NON-NLS-1$
		}

		public boolean visit(BlockComment node) {
			ASTVisitorTest.this.b.append("(/*"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(BlockComment node) {
			ASTVisitorTest.this.b.append("*/)"); //$NON-NLS-1$
		}

		public boolean visit(CreationReference node) {
			ASTVisitorTest.this.b.append("(eCR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(CreationReference node) {
			ASTVisitorTest.this.b.append("eCR)"); //$NON-NLS-1$
		}

		public boolean visit(ExpressionMethodReference node) {
			ASTVisitorTest.this.b.append("(eEMR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ExpressionMethodReference node) {
			ASTVisitorTest.this.b.append("eEMR)"); //$NON-NLS-1$
		}

		public boolean visit(LineComment node) {
			ASTVisitorTest.this.b.append("(//"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(LineComment node) {
			ASTVisitorTest.this.b.append("//)"); //$NON-NLS-1$
		}

		public boolean visit(TagElement node) {
			ASTVisitorTest.this.b.append("(TG"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getTagName());
			return isVisitingChildren();
		}
		public void endVisit(TagElement node) {
			ASTVisitorTest.this.b.append(node.getTagName());
			ASTVisitorTest.this.b.append("TG)"); //$NON-NLS-1$
		}

		public boolean visit(TextElement node) {
			ASTVisitorTest.this.b.append("(TX"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getText());
			return isVisitingChildren();
		}
		public void endVisit(TextElement node) {
			ASTVisitorTest.this.b.append(node.getText());
			ASTVisitorTest.this.b.append("TX)"); //$NON-NLS-1$
		}

		public boolean visit(MemberRef node) {
			ASTVisitorTest.this.b.append("(MBREF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MemberRef node) {
			ASTVisitorTest.this.b.append("MBREF)"); //$NON-NLS-1$
		}

		public boolean visit(MethodRef node) {
			ASTVisitorTest.this.b.append("(MTHREF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodRef node) {
			ASTVisitorTest.this.b.append("MTHREF)"); //$NON-NLS-1$
		}

		public boolean visit(MethodRefParameter node) {
			ASTVisitorTest.this.b.append("(MPARM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodRefParameter node) {
			ASTVisitorTest.this.b.append("MPARM)"); //$NON-NLS-1$
		}

		public boolean visit(LabeledStatement node) {
			ASTVisitorTest.this.b.append("(sLA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(LabeledStatement node) {
			ASTVisitorTest.this.b.append("sLA)"); //$NON-NLS-1$
		}

		public boolean visit(MethodDeclaration node) {
			ASTVisitorTest.this.b.append("(MD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodDeclaration node) {
			ASTVisitorTest.this.b.append("MD)"); //$NON-NLS-1$
		}

		public boolean visit(MethodInvocation node) {
			ASTVisitorTest.this.b.append("(eMI"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MethodInvocation node) {
			ASTVisitorTest.this.b.append("eMI)"); //$NON-NLS-1$
		}

		public boolean visit(ModuleDeclaration node) {
			ASTVisitorTest.this.b.append("(MoD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ModuleDeclaration node) {
			ASTVisitorTest.this.b.append("MoD)"); //$NON-NLS-1$
		}

		public boolean visit(NullLiteral node) {
			ASTVisitorTest.this.b.append("(eNL"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(NullLiteral node) {
			ASTVisitorTest.this.b.append("eNL)"); //$NON-NLS-1$
		}

		public boolean visit(NumberLiteral node) {
			ASTVisitorTest.this.b.append("(eNU"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getToken());
			return isVisitingChildren();
		}
		public void endVisit(NumberLiteral node) {
			ASTVisitorTest.this.b.append(node.getToken());
			ASTVisitorTest.this.b.append("eNU)"); //$NON-NLS-1$
		}

		public boolean visit(PackageDeclaration node) {
			ASTVisitorTest.this.b.append("(PD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(PackageDeclaration node) {
			ASTVisitorTest.this.b.append("PD)"); //$NON-NLS-1$
		}

		public boolean visit(ParenthesizedExpression node) {
			ASTVisitorTest.this.b.append("(ePA"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ParenthesizedExpression node) {
			ASTVisitorTest.this.b.append("ePA)"); //$NON-NLS-1$
		}

		public boolean visit(PostfixExpression node) {
			ASTVisitorTest.this.b.append("(ePO"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(PostfixExpression node) {
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			ASTVisitorTest.this.b.append("ePO)"); //$NON-NLS-1$
		}

		public boolean visit(PrefixExpression node) {
			ASTVisitorTest.this.b.append("(ePR"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			return isVisitingChildren();
		}
		public void endVisit(PrefixExpression node) {
			ASTVisitorTest.this.b.append(node.getOperator().toString());
			ASTVisitorTest.this.b.append("ePR)"); //$NON-NLS-1$
		}

		public boolean visit(ReturnStatement node) {
			ASTVisitorTest.this.b.append("(sRT"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ReturnStatement node) {
			ASTVisitorTest.this.b.append("sRT)"); //$NON-NLS-1$
		}

		public boolean visit(SingleVariableDeclaration node) {
			ASTVisitorTest.this.b.append("(VD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SingleVariableDeclaration node) {
			ASTVisitorTest.this.b.append("VD)"); //$NON-NLS-1$
		}

		public boolean visit(StringLiteral node) {
			ASTVisitorTest.this.b.append("(eSL"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getLiteralValue());
			return isVisitingChildren();
		}
		public void endVisit(StringLiteral node) {
			ASTVisitorTest.this.b.append(node.getLiteralValue());
			ASTVisitorTest.this.b.append("eSL)"); //$NON-NLS-1$
		}

		public boolean visit(SuperConstructorInvocation node) {
			ASTVisitorTest.this.b.append("(sSC"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperConstructorInvocation node) {
			ASTVisitorTest.this.b.append("sSC)"); //$NON-NLS-1$
		}

		public boolean visit(SuperFieldAccess node) {
			ASTVisitorTest.this.b.append("(eSF"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperFieldAccess node) {
			ASTVisitorTest.this.b.append("eSF)"); //$NON-NLS-1$
		}

		public boolean visit(SuperMethodInvocation node) {
			ASTVisitorTest.this.b.append("(eSM"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperMethodInvocation node) {
			ASTVisitorTest.this.b.append("eSM)"); //$NON-NLS-1$
		}

		public boolean visit(SuperMethodReference node) {
			ASTVisitorTest.this.b.append("(eSMR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SuperMethodReference node) {
			ASTVisitorTest.this.b.append("eSMR)"); //$NON-NLS-1$
		}

		public boolean visit(SwitchCase node) {
			ASTVisitorTest.this.b.append("(sSC"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SwitchCase node) {
			ASTVisitorTest.this.b.append("sSC)"); //$NON-NLS-1$
		}

		public boolean visit(SwitchStatement node) {
			ASTVisitorTest.this.b.append("(sSW"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SwitchStatement node) {
			ASTVisitorTest.this.b.append("sSW)"); //$NON-NLS-1$
		}

		public boolean visit(SynchronizedStatement node) {
			ASTVisitorTest.this.b.append("(sSY"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SynchronizedStatement node) {
			ASTVisitorTest.this.b.append("sSY)"); //$NON-NLS-1$
		}

		public boolean visit(ThisExpression node) {
			ASTVisitorTest.this.b.append("(eTH"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ThisExpression node) {
			ASTVisitorTest.this.b.append("eTH)"); //$NON-NLS-1$
		}

		public boolean visit(ThrowStatement node) {
			ASTVisitorTest.this.b.append("(sTR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(ThrowStatement node) {
			ASTVisitorTest.this.b.append("sTR)"); //$NON-NLS-1$
		}

		public boolean visit(TryStatement node) {
			ASTVisitorTest.this.b.append("(sTY"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TryStatement node) {
			ASTVisitorTest.this.b.append("sTY)"); //$NON-NLS-1$
		}

		public boolean visit(TypeDeclaration node) {
			ASTVisitorTest.this.b.append("(TD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeDeclaration node) {
			ASTVisitorTest.this.b.append("TD)"); //$NON-NLS-1$
		}

		public boolean visit(TypeDeclarationStatement node) {
			ASTVisitorTest.this.b.append("(sTD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeDeclarationStatement node) {
			ASTVisitorTest.this.b.append("sTD)"); //$NON-NLS-1$
		}

		public boolean visit(TypeLiteral node) {
			ASTVisitorTest.this.b.append("(eTL"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeLiteral node) {
			ASTVisitorTest.this.b.append("eTL)"); //$NON-NLS-1$
		}

		public boolean visit(TypeMethodReference node) {
			ASTVisitorTest.this.b.append("(eTMR"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeMethodReference node) {
			ASTVisitorTest.this.b.append("eTMR)"); //$NON-NLS-1$
		}

		public boolean visit(TypeParameter node) {
			ASTVisitorTest.this.b.append("(tTP"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(TypeParameter node) {
			ASTVisitorTest.this.b.append("tTP)"); //$NON-NLS-1$
		}

		public boolean visit(VariableDeclarationExpression node) {
			ASTVisitorTest.this.b.append("(eVD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationExpression node) {
			ASTVisitorTest.this.b.append("eVD)"); //$NON-NLS-1$
		}

		public boolean visit(VariableDeclarationFragment node) {
			ASTVisitorTest.this.b.append("(VS"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationFragment node) {
			ASTVisitorTest.this.b.append("VS)"); //$NON-NLS-1$
		}

		public boolean visit(VariableDeclarationStatement node) {
			ASTVisitorTest.this.b.append("(sVD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(VariableDeclarationStatement node) {
			ASTVisitorTest.this.b.append("sVD)"); //$NON-NLS-1$
		}

		public boolean visit(WhileStatement node) {
			ASTVisitorTest.this.b.append("(sWH"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(WhileStatement node) {
			ASTVisitorTest.this.b.append("sWH)"); //$NON-NLS-1$
		}

		public boolean visit(AnnotationTypeDeclaration node) {
			ASTVisitorTest.this.b.append("(@TD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AnnotationTypeDeclaration node) {
			ASTVisitorTest.this.b.append("@TD)"); //$NON-NLS-1$
		}

		public boolean visit(AnnotationTypeMemberDeclaration node) {
			ASTVisitorTest.this.b.append("(@MD"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			ASTVisitorTest.this.b.append("@MD)"); //$NON-NLS-1$
		}

		public boolean visit(NormalAnnotation node) {
			ASTVisitorTest.this.b.append("(@NAN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(NormalAnnotation node) {
			ASTVisitorTest.this.b.append("@NAN)"); //$NON-NLS-1$
		}

		public boolean visit(MarkerAnnotation node) {
			ASTVisitorTest.this.b.append("(@MAN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MarkerAnnotation node) {
			ASTVisitorTest.this.b.append("@MAN)"); //$NON-NLS-1$
		}

		public boolean visit(SingleMemberAnnotation node) {
			ASTVisitorTest.this.b.append("(@SMAN"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(SingleMemberAnnotation node) {
			ASTVisitorTest.this.b.append("@SMAN)"); //$NON-NLS-1$
		}

		public boolean visit(MemberValuePair node) {
			ASTVisitorTest.this.b.append("(@MVP"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(MemberValuePair node) {
			ASTVisitorTest.this.b.append("@MVP)"); //$NON-NLS-1$
		}

		public boolean visit(Modifier node) {
			ASTVisitorTest.this.b.append("(MOD"); //$NON-NLS-1$
			ASTVisitorTest.this.b.append(node.getKeyword().toString());
			return isVisitingChildren();
		}
		public void endVisit(Modifier node) {
			ASTVisitorTest.this.b.append(node.getKeyword().toString());
			ASTVisitorTest.this.b.append("MOD)"); //$NON-NLS-1$
		}

		public boolean visit(Dimension node) {
			ASTVisitorTest.this.b.append("(@ED"); //$NON-NLS-1$
			return isVisitingChildren();
		}
		public void endVisit(Dimension node) {
			ASTVisitorTest.this.b.append("@ED)"); //$NON-NLS-1$
		}

		public void preVisit(ASTNode node) {
			ASTVisitorTest.this.b.append("["); //$NON-NLS-1$
		}

		public void postVisit(ASTNode node) {
			ASTVisitorTest.this.b.append("]"); //$NON-NLS-1$
		}

	}
	// NAMES
	public void testSimpleName() {
		Name x1 = this.ast.newName(new String[]{"Z"}); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(nSZZnS)]".equals(result)); //$NON-NLS-1$
	}

	public void testQualifiedName() {
		Name x1 = this.ast.newName(new String[]{"X", "Y"}); //$NON-NLS-1$ //$NON-NLS-2$
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(nQ[(nSXXnS)][(nSYYnS)]nQ)]".equals(result)); //$NON-NLS-1$
	}


	// TYPES
	public void testPrimitiveType() {
		Type x1 = this.ast.newPrimitiveType(PrimitiveType.CHAR);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(tPcharchartP)]".equals(result)); //$NON-NLS-1$
	}

	public void testSimpleType() {
		Type x1 = this.ast.newSimpleType(this.ast.newName(new String[]{"Z"})); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(tS[(nSZZnS)]tS)]".equals(result)); //$NON-NLS-1$
	}

	public void testArrayType() {
		Type x0 = this.ast.newPrimitiveType(PrimitiveType.CHAR);
		Type x1 = this.ast.newArrayType(x0);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		String expected = this.ast.apiLevel() < getJLS8() ? "[(tA[(tPcharchartP)]tA)]" : "[(tA[(tPcharchartP)][(@ED@ED)]tA)]";
		assertTrue(expected.equals(result)); //$NON-NLS-1$
	}

	/** @deprecated using deprecated code */
	public void testNameQualifiedType() {
		if (this.ast.apiLevel() < getJLS8()) {
			return;
		}
		QualifiedName q = this.ast.newQualifiedName(this.N2, this.N3);
		NameQualifiedType x1 = this.ast.newNameQualifiedType(q, this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(tPQ"+"[(nQ"+this.N2S+this.N3S+"nQ)]"+this.N1S+"tPQ)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testParameterizedType() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		ParameterizedType x1 = this.ast.newParameterizedType(this.T1);
		x1.typeArguments().add(this.T2);
		x1.typeArguments().add(this.PT1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(tM"+this.T1S+this.T2S+this.PT1S+"tM)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testQualifiedType() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		QualifiedType x1 = this.ast.newQualifiedType(this.T1, this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(tQ"+this.T1S+this.N1S+"tQ)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testWildcardType() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		WildcardType x1 = this.ast.newWildcardType();
		x1.setBound(this.T1, true);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(tW"+this.T1S+"tW)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testUnionType() {
		if (this.ast.apiLevel() <= AST.JLS4) {
			return;
		}
		UnionType x1 = this.ast.newUnionType();
		x1.types().add(this.T1);
		x1.types().add(this.T2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(tU"+this.T1S+this.T2S+"tU)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// EXPRESSIONS and STATEMENTS

	public void testArrayAccess() {
		ArrayAccess x1 = this.ast.newArrayAccess();
		x1.setArray(this.E1);
		x1.setIndex(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eAA"+this.E1S+this.E2S+"eAA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testArrayCreation() {
		ArrayCreation x1 = this.ast.newArrayCreation();
		x1.setType(this.ast.newArrayType(this.T1));
		x1.dimensions().add(this.E1);
		x1.dimensions().add(this.E2);
		x1.setInitializer(this.ast.newArrayInitializer());
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		String dim = this.ast.apiLevel() < getJLS8() ? "" : "[(@ED@ED)]";
		assertTrue(result.equals("[(eAC"+"[(tA"+this.T1S+ dim +"tA)]"+this.E1S+this.E2S+"[(eAIeAI)]eAC)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	public void testArrayInitializer() {
		ArrayInitializer x1 = this.ast.newArrayInitializer();
		x1.expressions().add(this.E1);
		x1.expressions().add(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eAI"+this.E1S+this.E2S+"eAI)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testAssertStatement() {
		AssertStatement x1 = this.ast.newAssertStatement();
		x1.setExpression(this.E1);
		x1.setMessage(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sAS"+this.E1S+this.E2S+"sAS)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testAssignment() {
		Assignment x1 = this.ast.newAssignment();
		x1.setLeftHandSide(this.E1);
		x1.setRightHandSide(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(="+this.E1S+this.E2S+"=)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testBlock() {
		Block x1 = this.ast.newBlock();
		x1.statements().add(this.S1);
		x1.statements().add(this.S2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sB"+this.S1S+this.S2S+"sB)]")); //$NON-NLS-1$ //$NON-NLS-2$

		// check that visiting children can be cut off
		v1.setVisitingChildren(false);
		this.b.setLength(0);
		x1.accept(v1);
		result = this.b.toString();
		assertTrue(result.equals("[(sBsB)]")); //$NON-NLS-1$
	}

	public void testBlockComment() {
		BlockComment x1 = this.ast.newBlockComment();
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(/**/)]".equals(result)); //$NON-NLS-1$
	}

	public void testBooleanLiteral() {
		BooleanLiteral x1 = this.ast.newBooleanLiteral(true);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eBLtruetrueeBL)]")); //$NON-NLS-1$
	}
	public void testBreakStatement() {
		BreakStatement x1 = this.ast.newBreakStatement();
		x1.setLabel(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sBR"+this.N1S+"sBR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCastExpression() {
		CastExpression x1 = this.ast.newCastExpression();
		x1.setType(this.T1);
		x1.setExpression(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eCS"+this.T1S+this.E1S+"eCS)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCatchClause() {
		CatchClause x1 = this.ast.newCatchClause();
		x1.setException(this.V1);
		x1.setBody(this.B1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(cc"+this.V1S+this.B1S+"cc)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCharacterLiteral() {
		CharacterLiteral x1 = this.ast.newCharacterLiteral();
		x1.setCharValue('q');
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eCL'q''q'eCL)]")); //$NON-NLS-1$
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eCI"+this.E1S+this.N1S+this.ACD1S+"eCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eCI"+this.E1S+this.PT1S+this.T1S+this.ACD1S+"eCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testAnonymousClassDeclaration() {
		AnonymousClassDeclaration x1 = this.ast.newAnonymousClassDeclaration();
		x1.bodyDeclarations().add(this.FD1);
		x1.bodyDeclarations().add(this.FD2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ACD"+this.FD1S+this.FD2S+"ACD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testCompilationUnit() {
		CompilationUnit x1 = this.ast.newCompilationUnit();
		x1.setPackage(this.PD1);
		x1.imports().add(this.ID1);
		x1.imports().add(this.ID2);
		x1.types().add(this.TD1);
		x1.types().add(this.TD2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(CU"+this.PD1S+this.ID1S+this.ID2S+this.TD1S+this.TD2S+"CU)]")); //$NON-NLS-1$ //$NON-NLS-2$

		// check that visiting children can be cut off
		v1.setVisitingChildren(false);
		this.b.setLength(0);
		x1.accept(v1);
		result = this.b.toString();
		assertTrue(result.equals("[(CUCU)]")); //$NON-NLS-1$
	}
	public void testConditionalExpression() {
		ConditionalExpression x1 = this.ast.newConditionalExpression();
		x1.setExpression(this.E1);
		x1.setThenExpression(this.E2);
		x1.setElseExpression(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eCO"+this.E1S+this.E2S+this.N1S+"eCO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testConstructorInvocation() {
		ConstructorInvocation x1 = this.ast.newConstructorInvocation();
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(sCI"+this.E1S+this.E2S+"sCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(sCI"+this.PT1S+this.E1S+this.E2S+"sCI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testContinueStatement() {
		ContinueStatement x1 = this.ast.newContinueStatement();
		x1.setLabel(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sCN"+this.N1S+"sCN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCreationReference() {
		if (this.ast.apiLevel() < getJLS8())
			return;
		CreationReference x1 = this.ast.newCreationReference();
		x1.setType(this.T1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eCR"+this.T1S+"eCR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testDoStatement() {
		DoStatement x1 = this.ast.newDoStatement();
		x1.setExpression(this.E1);
		x1.setBody(this.S1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sDO"+this.S1S+this.E1S+"sDO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testEmptyStatement() {
		EmptyStatement x1 = this.ast.newEmptyStatement();
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sEMsEM)]")); //$NON-NLS-1$
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ECD"+this.JD1S+this.MOD1S+this.MOD2S+this.N1S+this.E1S+this.E2S+this.ACD1S+"ECD)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ED"+this.JD1S+this.MOD1S+this.MOD2S+this.N1S+this.T1S+this.T2S+this.EC1S+this.EC2S+this.FD1S+this.FD2S+"ED)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testExpressionMethodReference() {
		if (this.ast.apiLevel() < getJLS8())
			return;
		ExpressionMethodReference x1 = this.ast.newExpressionMethodReference();
		x1.setExpression(this.E1);
		x1.setName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eEMR"+this.E1S+this.N1S+"eEMR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testExpressionStatement() {
		ExpressionStatement x1 = this.ast.newExpressionStatement(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sEX"+this.E1S+"sEX)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testExtraDimension() {
		if (this.ast.apiLevel() < getJLS8()) {
			return;
		}
		Dimension x1 = this.ast.newDimension();
		x1.annotations().add(this.ANO1);
		x1.annotations().add(this.ANO2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertEquals("[(@ED"+this.ANO1S+this.ANO2S+"@ED)]", result); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testFieldAccess() {
		FieldAccess x1 = this.ast.newFieldAccess();
		x1.setExpression(this.E1);
		x1.setName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eFA"+this.E1S+this.N1S+"eFA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testFieldDeclaration() {
		FieldDeclaration x1 = this.ast.newFieldDeclaration(this.W1);
		x1.setJavadoc(this.JD1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.fragments().add(this.W2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(FD"+this.JD1S+this.T1S+this.W1S+this.W2S+"FD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(FD"+this.JD1S+this.MOD1S+this.MOD2S+this.T1S+this.W1S+this.W2S+"FD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testForStatement() {
		ForStatement x1 = this.ast.newForStatement();
		x1.initializers().add(this.E1);
		x1.initializers().add(this.E2);
		x1.setExpression(this.N1);
		x1.updaters().add(this.N2);
		x1.updaters().add(this.N3);
		x1.setBody(this.S1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sFR"+this.E1S+this.E2S+this.N1S+this.N2S+this.N3S+this.S1S+"sFR)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sEFR"+this.V1S+this.E1S+this.S1S+"sEFR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testIfStatement() {
		IfStatement x1 = this.ast.newIfStatement();
		x1.setExpression(this.E1);
		x1.setThenStatement(this.S1);
		x1.setElseStatement(this.S2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sIF"+this.E1S+this.S1S+this.S2S+"sIF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testImportDeclaration() {
		ImportDeclaration x1 = this.ast.newImportDeclaration();
		x1.setName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ID"+this.N1S+"ID)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testInfixExpression() {
		InfixExpression x1 = this.ast.newInfixExpression();
		x1.setOperator(InfixExpression.Operator.PLUS);
		x1.setLeftOperand(this.E1);
		x1.setRightOperand(this.E2);
		x1.extendedOperands().add(this.N1);
		x1.extendedOperands().add(this.N2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eIN+"+this.E1S+this.E2S+this.N1S+this.N2S+"+eIN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testInstanceofExpression() {
		InstanceofExpression x1 = this.ast.newInstanceofExpression();
		x1.setLeftOperand(this.E1);
		x1.setRightOperand(this.T1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eIO"+this.E1S+this.T1S+"eIO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testInitializer() {
		Initializer x1 = this.ast.newInitializer();
		x1.setJavadoc(this.JD1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setBody(this.B1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(IN"+this.JD1S+this.B1S+"IN)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(IN"+this.JD1S+this.MOD1S+this.MOD2S+this.B1S+"IN)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @deprecated (not really - just suppressing the warnings
	 * that come from testing Javadoc.getComment())
	 *
	 */
	public void testJavadoc() {
		Javadoc x1 = this.ast.newJavadoc();
		x1.tags().add(this.TAG1);

		// ASTVisitor() does not visit doc tags
		{
			TestVisitor v1 = new TestVisitor();
			this.b.setLength(0);
			x1.accept(v1);
			String result = this.b.toString();
			assertTrue(("[(JDJD)]").equals(result)); //$NON-NLS-1$
		}

		// ASTVisitor(false) does not visit doc tags
		{
			TestVisitor v1 = new TestVisitor(false);
			this.b.setLength(0);
			x1.accept(v1);
			String result = this.b.toString();
			assertTrue(("[(JDJD)]").equals(result)); //$NON-NLS-1$
		}

		// ASTVisitor(true) does visit doc tags
		{
			TestVisitor v1 = new TestVisitor(true);
			this.b.setLength(0);
			x1.accept(v1);
			String result = this.b.toString();
			assertTrue(("[(JD"+this.TAG1S+"JD)]").equals(result)); //$NON-NLS-1$
		}
	}

	public void testLabeledStatement() {
		LabeledStatement x1 = this.ast.newLabeledStatement();
		x1.setLabel(this.N1);
		x1.setBody(this.S1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sLA"+this.N1S+this.S1S+"sLA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testLineComment() {
		LineComment x1 = this.ast.newLineComment();
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(////)]".equals(result)); //$NON-NLS-1$
	}

	public void testMemberRef() {
		MemberRef x1 = this.ast.newMemberRef();
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(MBREF"+this.N1S+this.N2S+"MBREF)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
			x1.setReturnType2(this.T1);
		}
		x1.setName(this.N1);
		x1.parameters().add(this.V1);
		x1.parameters().add(this.V2);
		if (this.ast.apiLevel() < getJLS8()) {
			x1.thrownExceptions().add(this.N2);
			x1.thrownExceptions().add(this.N3);
		} else {
			x1.thrownExceptionTypes().add(this.T3);
			x1.thrownExceptionTypes().add(this.T4);
		}
		x1.setBody(this.B1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertEquals("[(MD"+this.JD1S+this.T1S+this.N1S+this.V1S+this.V2S+this.N2S+this.N3S+this.B1S+"MD)]", result); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (this.ast.apiLevel() < getJLS8()) {
			assertEquals("[(MD"+this.JD1S+this.MOD1S+this.MOD2S+this.TP1S+this.T1S+this.N1S+this.V1S+this.V2S+this.N2S+this.N3S+this.B1S+"MD)]", result); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertEquals("[(MD"+this.JD1S+this.MOD1S+this.MOD2S+this.TP1S+this.T1S+this.N1S+this.V1S+this.V2S+this.T3S+this.T4S+this.B1S+"MD)]", result); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/** @deprecated using deprecated code */
	public void testMethodInvocation() {
		MethodInvocation x1 = this.ast.newMethodInvocation();
		x1.setExpression(this.N1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.setName(this.N2);
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eMI"+this.N1S+this.N2S+this.E1S+this.E2S+"eMI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eMI"+this.N1S+this.PT1S+this.N2S+this.E1S+this.E2S+"eMI)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void testMethodRef() {
		MethodRef x1 = this.ast.newMethodRef();
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		x1.parameters().add(this.MPARM1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(MTHREF"+this.N1S+this.N2S+this.MPARM1S+"MTHREF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testMethodRefParameter() {
		MethodRefParameter x1 = this.ast.newMethodRefParameter();
		x1.setType(this.T1);
		x1.setName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(MPARM"+this.T1S+this.N1S+"MPARM)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testModifier() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		Modifier x1 = this.ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(MODprivateprivateMOD)]")); //$NON-NLS-1$ //$NON-NLS-2$
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(@NAN"+this.N1S+this.MVP1S+this.MVP2S+"@NAN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testMemberValuePair() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		MemberValuePair x1 = this.ast.newMemberValuePair();
		x1.setName(this.N1);
		x1.setValue(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(@MVP"+this.N1S+this.E1S+"@MVP)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testMarkerAnnotation() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		MarkerAnnotation x1 = this.ast.newMarkerAnnotation();
		x1.setTypeName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(@MAN"+this.N1S+"@MAN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testSingleMemberAnnotation() {
		if (this.ast.apiLevel() == AST.JLS2) {
			return;
		}
		SingleMemberAnnotation x1 = this.ast.newSingleMemberAnnotation();
		x1.setTypeName(this.N1);
		x1.setValue(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(@SMAN"+this.N1S+this.E1S+"@SMAN)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(@TD"+this.JD1S+this.MOD1S+this.MOD2S+this.N1S+this.FD1S+this.FD2S+"@TD)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(@MD"+this.JD1S+this.MOD1S+this.MOD2S+this.T1S+this.N1S+this.E1S+"@MD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testNullLiteral() {
		NullLiteral x1 = this.ast.newNullLiteral();
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eNLeNL)]")); //$NON-NLS-1$
	}
	public void testNumberLiteral() {
		NumberLiteral x1 = this.ast.newNumberLiteral("1.0"); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eNU1.01.0eNU)]")); //$NON-NLS-1$
	}
	/** @deprecated using deprecated code */
	public void testPackageDeclaration() {
		PackageDeclaration x1 = this.ast.newPackageDeclaration();
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.setJavadoc(this.JD1);
			x1.annotations().add(this.ANO1);
			x1.annotations().add(this.ANO2);
		}
		x1.setName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(PD"+this.N1S+"PD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(PD"+this.JD1S+this.ANO1S+this.ANO2S+this.N1S+"PD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testParenthesizedExpression() {
		ParenthesizedExpression x1 = this.ast.newParenthesizedExpression();
		x1.setExpression(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ePA"+this.E1S+"ePA)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testPostfixExpression() {
		PostfixExpression x1 = this.ast.newPostfixExpression();
		x1.setOperand(this.E1);
		x1.setOperator(PostfixExpression.Operator.INCREMENT);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ePO++"+this.E1S+"++ePO)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testPrefixExpression() {
		PrefixExpression x1 = this.ast.newPrefixExpression();
		x1.setOperand(this.E1);
		x1.setOperator(PrefixExpression.Operator.INCREMENT);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(ePR++"+this.E1S+"++ePR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testReturnStatement() {
		ReturnStatement x1 = this.ast.newReturnStatement();
		x1.setExpression(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sRT"+this.E1S+"sRT)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testStringLiteral() {
		StringLiteral x1 = this.ast.newStringLiteral();
		x1.setLiteralValue("H"); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eSLHHeSL)]")); //$NON-NLS-1$
	}
	/** @deprecated using deprecated code */
	public void testSuperConstructorInvocation() {
		SuperConstructorInvocation x1 = this.ast.newSuperConstructorInvocation();
		x1.setExpression(this.N1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(sSC"+this.N1S+this.E1S+this.E2S+"sSC)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(sSC"+this.N1S+this.PT1S+this.E1S+this.E2S+"sSC)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testSuperFieldAccess() {
		SuperFieldAccess x1 = this.ast.newSuperFieldAccess();
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eSF"+this.N1S+this.N2S+"eSF)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testSuperMethodInvocation() {
		SuperMethodInvocation x1 = this.ast.newSuperMethodInvocation();
		x1.setQualifier(this.N1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.typeArguments().add(this.PT1);
		}
		x1.setName(this.N2);
		x1.arguments().add(this.E1);
		x1.arguments().add(this.E2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eSM"+this.N1S+this.N2S+this.E1S+this.E2S+"eSM)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eSM"+this.N1S+this.PT1S+this.N2S+this.E1S+this.E2S+"eSM)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testSuperMethodReference() {
		if (this.ast.apiLevel() < getJLS8()) {
			return;
		}
		SuperMethodReference x1 = this.ast.newSuperMethodReference();
		x1.setQualifier(this.N1);
		x1.setName(this.N2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eSMR"+this.N1S+this.N2S+"eSMR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	@SuppressWarnings("deprecation")
	public void testSwitchCase() {
		SwitchCase x1 = this.ast.newSwitchCase();
		x1.setExpression(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sSC"+this.E1S+"sSC)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testSwitchStatement() {
		SwitchStatement x1 = this.ast.newSwitchStatement();
		x1.setExpression(this.E1);
		x1.statements().add(this.S1);
		x1.statements().add(this.S2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sSW"+this.E1S+this.S1S+this.S2S+"sSW)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testSynchronizedStatement() {
		SynchronizedStatement x1 = this.ast.newSynchronizedStatement();
		x1.setExpression(this.E1);
		x1.setBody(this.B1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sSY"+this.E1S+this.B1S+"sSY)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTagElement() {
		TagElement x1 = this.ast.newTagElement();
		x1.setTagName("x"); //$NON-NLS-1$
		x1.fragments().add(this.TAG1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(("[(TGx"+this.TAG1S+"xTG)]").equals(result)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTextElement() {
		TextElement x1 = this.ast.newTextElement();
		x1.setText("x"); //$NON-NLS-1$
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue("[(TXxxTX)]".equals(result)); //$NON-NLS-1$
	}

	public void testThisExpression() {
		ThisExpression x1 = this.ast.newThisExpression();
		x1.setQualifier(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eTH"+this.N1S+"eTH)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testThrowStatement() {
		ThrowStatement x1 = this.ast.newThrowStatement();
		x1.setExpression(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sTR"+this.E1S+"sTR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testTryStatement() {
		TryStatement x1 = this.ast.newTryStatement();
		int level = this.ast.apiLevel();
		if (level >= AST.JLS4) {
			VariableDeclarationExpression vde1= this.ast.newVariableDeclarationExpression(this.W1);
			vde1.setType(this.T1);
			x1.resources().add(vde1);
			VariableDeclarationExpression vde2= this.ast.newVariableDeclarationExpression(this.W2);
			vde2.setType(this.T2);
			x1.resources().add(vde2);
		}
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
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertEquals("[(sTY"
				+(level >= AST.JLS4 ? "[(eVD"+this.T1S+this.W1S+"eVD)]"+"[(eVD"+this.T2S+this.W2S+"eVD)]" : "")
				+this.B1S+"[(cc"+this.V1S+"[(sBsB)]"+"cc)]"+"[(cc"+this.V2S+"[(sBsB)]"+"cc)]"+"[(sBsB)]"+"sTY)]", result); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
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
			x1.setSuperclassType(this.PT1);
			x1.superInterfaceTypes().add(this.T1);
			x1.superInterfaceTypes().add(this.T2); //$NON-NLS-1$
		}
		x1.bodyDeclarations().add(this.FD1);
		x1.bodyDeclarations().add(this.FD2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(TD"+this.JD1S+this.N1S+this.N2S+this.N3S+this.N4S+this.FD1S+this.FD2S+"TD)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			assertTrue(result.equals("[(TD"+this.JD1S+this.MOD1S+this.MOD2S+this.N1S+this.TP1S+this.PT1S+this.T1S+this.T2S+this.FD1S+this.FD2S+"TD)]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	public void testTypeDeclarationStatement() {
		TypeDeclarationStatement x1 = this.ast.newTypeDeclarationStatement(this.TD1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sTD"+this.TD1S+"sTD)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTypeLiteral() {
		TypeLiteral x1 = this.ast.newTypeLiteral();
		x1.setType(this.T1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eTL"+this.T1S+"eTL)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTypeMethodReference() {
		if (this.ast.apiLevel() < getJLS8())
			return;
		TypeMethodReference x1 = this.ast.newTypeMethodReference();
		x1.setType(this.T1);
		x1.setName(this.N1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(eTMR"+this.T1S+this.N1S+"eTMR)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** @deprecated using deprecated code */
	public void testSingleVariableDeclaration() {
		SingleVariableDeclaration x1 = this.ast.newSingleVariableDeclaration();
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.setName(this.N1);
		x1.setInitializer(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(VD"+this.T1S+this.N1S+this.E1S+"VD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(VD"+this.MOD1S+this.MOD2S+this.T1S+this.N1S+this.E1S+"VD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testVariableDeclarationFragment() {
		VariableDeclarationFragment x1 = this.ast.newVariableDeclarationFragment();
		x1.setName(this.N1);
		x1.setInitializer(this.E1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(VS"+this.N1S+this.E1S+"VS)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/** @deprecated using deprecated code */
	public void testVariableDeclarationExpression() {
		VariableDeclarationExpression x1 = this.ast.newVariableDeclarationExpression(this.W1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.fragments().add(this.W2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(eVD"+this.T1S+this.W1S+this.W2S+"eVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(eVD"+this.MOD1S+this.MOD2S+this.T1S+this.W1S+this.W2S+"eVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	/** @deprecated using deprecated code */
	public void testVariableDeclarationStatement() {
		VariableDeclarationStatement x1 = this.ast.newVariableDeclarationStatement(this.W1);
		if (this.ast.apiLevel() >= AST.JLS3) {
			x1.modifiers().add(this.MOD1);
			x1.modifiers().add(this.MOD2);
		}
		x1.setType(this.T1);
		x1.fragments().add(this.W2);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		if (this.ast.apiLevel() == AST.JLS2) {
			assertTrue(result.equals("[(sVD"+this.T1S+this.W1S+this.W2S+"sVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			assertTrue(result.equals("[(sVD"+this.MOD1S+this.MOD2S+this.T1S+this.W1S+this.W2S+"sVD)]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public void testWhileStatement() {
		WhileStatement x1 = this.ast.newWhileStatement();
		x1.setExpression(this.E1);
		x1.setBody(this.S1);
		TestVisitor v1 = new TestVisitor();
		this.b.setLength(0);
		x1.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[(sWH"+this.E1S+this.S1S+"sWH)]")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testPrePost() {
		SimpleName n1 = this.ast.newSimpleName("a"); //$NON-NLS-1$
		SimpleName n2 = this.ast.newSimpleName("b"); //$NON-NLS-1$
		QualifiedName q = this.ast.newQualifiedName(n1, n2);
		TestVisitor v1 = new TestVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				ASTVisitorTest.this.b.append("["); //$NON-NLS-1$
				switch (node.getNodeType()) {
					case ASTNode.QUALIFIED_NAME :
						ASTVisitorTest.this.b.append("q"); //$NON-NLS-1$
						break;
					case ASTNode.SIMPLE_NAME :
						ASTVisitorTest.this.b.append(((SimpleName) node).getIdentifier());
						break;
				}
			}

			@Override
			public void postVisit(ASTNode node) {
				switch (node.getNodeType()) {
					case ASTNode.QUALIFIED_NAME :
						ASTVisitorTest.this.b.append("q"); //$NON-NLS-1$
						break;
					case ASTNode.SIMPLE_NAME :
						ASTVisitorTest.this.b.append(((SimpleName) node).getIdentifier());
						break;
				}
				ASTVisitorTest.this.b.append("]"); //$NON-NLS-1$
			}
		};

		this.b.setLength(0);
		q.accept(v1);
		String result = this.b.toString();
		assertTrue(result.equals("[q(nQ" + "[a(nSaanS)a]" + "[b(nSbbnS)b]" + "nQ)q]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	public void testTraverseAndModify() {
		final TypeDeclaration typeDeclaration = this.ast.newTypeDeclaration();
		typeDeclaration.setName(this.N1);
		MethodDeclaration methodDeclaration = this.ast.newMethodDeclaration();
		methodDeclaration.setName(this.ast.newSimpleName("M1")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(0, methodDeclaration);
		final MethodDeclaration methodDeclaration2 = this.ast.newMethodDeclaration();
		methodDeclaration2.setName(this.ast.newSimpleName("M2")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(1, methodDeclaration2);
		MethodDeclaration methodDeclaration3 = this.ast.newMethodDeclaration();
		methodDeclaration3.setName(this.ast.newSimpleName("M3")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(2, methodDeclaration3);
		// insert a new before the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				if (node == methodDeclaration2) {
					MethodDeclaration methodDeclaration4 = ASTVisitorTest.this.ast.newMethodDeclaration();
					methodDeclaration4.setName(ASTVisitorTest.this.ast.newSimpleName("M4")); //$NON-NLS-1$
					typeDeclaration.bodyDeclarations().add(0, methodDeclaration4);
				}
				return super.visit(node);
			}
		};
		this.b.setLength(0);
		typeDeclaration.accept(v1);
		assertEquals("wrong output", "[(TD[(nSNNnS)][(MD[(tPvoidvoidtP)][(nSM1M1nS)]MD)][(MD[(tPvoidvoidtP)][(nSM2M2nS)]MD)][(MD[(tPvoidvoidtP)][(nSM3M3nS)]MD)]TD)]", this.b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testTraverseAndModify_2() {
		final TypeDeclaration typeDeclaration = this.ast.newTypeDeclaration();
		typeDeclaration.setName(this.N1);
		MethodDeclaration methodDeclaration = this.ast.newMethodDeclaration();
		methodDeclaration.setName(this.ast.newSimpleName("M1")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(0, methodDeclaration);
		final MethodDeclaration methodDeclaration2 = this.ast.newMethodDeclaration();
		methodDeclaration2.setName(this.ast.newSimpleName("M2")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(1, methodDeclaration2);
		MethodDeclaration methodDeclaration3 = this.ast.newMethodDeclaration();
		methodDeclaration3.setName(this.ast.newSimpleName("M3")); //$NON-NLS-1$
		typeDeclaration.bodyDeclarations().add(2, methodDeclaration3);
		// insert a new after the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				if (node == methodDeclaration2) {
					MethodDeclaration methodDeclaration4 = ASTVisitorTest.this.ast.newMethodDeclaration();
					methodDeclaration4.setName(ASTVisitorTest.this.ast.newSimpleName("M4")); //$NON-NLS-1$
					typeDeclaration.bodyDeclarations().add(3, methodDeclaration4);
				}
				return super.visit(node);
			}
		};
		this.b.setLength(0);
		typeDeclaration.accept(v1);
		assertEquals("wrong output", "[(TD[(nSNNnS)][(MD[(tPvoidvoidtP)][(nSM1M1nS)]MD)][(MD[(tPvoidvoidtP)][(nSM2M2nS)]MD)][(MD[(tPvoidvoidtP)][(nSM3M3nS)]MD)][(MD[(tPvoidvoidtP)][(nSM4M4nS)]MD)]TD)]", this.b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTraverseAndModify_3() {
		final InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newNumberLiteral("10")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// insert a new after the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				infixExpression.setRightOperand(ASTVisitorTest.this.ast.newNumberLiteral("22")); //$NON-NLS-1$
				return super.visit(node);
			}
		};
		this.b.setLength(0);
		infixExpression.accept(v1);
		assertEquals("wrong output", "[(eIN+[(nSiinS)][(eNU2222eNU)]+eIN)]", this.b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testTraverseAndModify_4() {
		final InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newNumberLiteral("10")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		// insert a new before the current node during a traverse
		TestVisitor v1 = new TestVisitor() {
			@Override
			public boolean visit(NumberLiteral node) {
				infixExpression.setLeftOperand(ASTVisitorTest.this.ast.newSimpleName("j")); //$NON-NLS-1$
				return super.visit(node);
			}
		};
		this.b.setLength(0);
		infixExpression.accept(v1);
		assertEquals("wrong output", "[(eIN+[(nSiinS)][(eNU1010eNU)]+eIN)]", this.b.toString()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
