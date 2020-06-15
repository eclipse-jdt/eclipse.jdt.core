/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* This is an implementation of an early-draft specification developed under the Java
* Community Process (JCP) and is made available for testing and evaluation purposes
* only. The code is not compatible with any specification of the JCP.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import junit.framework.Test;

public class ASTRewritingRecordDeclarationTest extends ASTRewritingTest {

	private static final String PROJECT = "P";

	public ASTRewritingRecordDeclarationTest(String name) {
		super(name, 14);
	}

	public ASTRewritingRecordDeclarationTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingRecordDeclarationTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS14 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void testRecord_001() throws Exception {
		if (this.apiLevel != 14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		MethodDeclaration methodDecl= findMethodDeclaration(record, "C");
		assertTrue("Not a compact constructor", methodDecl.isCompactConstructor());
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // rename first param & last throw statement
			List recordComponents = record.recordComponents();
			assertTrue("must be 0 parameters", recordComponents.size() == 0);
			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			newParam.setName(ast.newSimpleName("param1"));
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.RECORD_COMPONENTS_PROPERTY);
			listRewrite.insertFirst(newParam, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		//buf.append("public record C(int param1) {\n");
		buf.append("public record Cint param1() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");		assertEqualString(preview, buf.toString());

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	public void testRecord_002() throws Exception {
		if (this.apiLevel != 14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		MethodDeclaration methodDecl= findMethodDeclaration(record, "C");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // rename first param & last throw statement
			MethodDeclaration methodDecl1 = ast.newMethodDeclaration();
			methodDecl1.setConstructor(true);
			methodDecl1.setName(ast.newSimpleName("C"));
			methodDecl1.modifiers().addAll(ast.newModifiers( Modifier.PUBLIC));
			List parameters = methodDecl1.parameters();
			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			newParam.setName(ast.newSimpleName("param1"));
			parameters.add(newParam);
			Block body= ast.newBlock();
			methodDecl1.setBody(body);
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertLast(methodDecl1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n\n");
		buf.append("        public C(int param1) {\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("}\n");		assertEqualString(preview, buf.toString());

		assertEqualString(preview, buf.toString());

	}


}

