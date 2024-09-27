/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingImplicitTypeDeclarationTest extends ASTRewritingTest{

	public ASTRewritingImplicitTypeDeclarationTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingImplicitTypeDeclarationTest.class, 23);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS23 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_23);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_23);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_23);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}

	public void test001() throws Exception {
		AST ast = AST.newAST(AST.JLS23, true);
		// Create CompilationUnit
        CompilationUnit compilationUnit = ast.newCompilationUnit();

        ImplicitTypeDeclaration implicitTypeDeclaration = ast.newImplicitTypeDeclaration();

        Javadoc javaDoc= ast.newJavadoc();
		TextElement textElem= ast.newTextElement();
		textElem.setText("Hello");
		TagElement tagElement= ast.newTagElement();
		tagElement.fragments().add(textElem);
		javaDoc.tags().add(tagElement);
        implicitTypeDeclaration.setJavadoc(javaDoc);

        QualifiedName qualifiedName = ast.newQualifiedName(ast.newName("System"), ast.newSimpleName("out"));
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(qualifiedName);
        methodInvocation.setName(ast.newSimpleName("println"));

        StringLiteral literal = ast.newStringLiteral();
		literal.setLiteralValue("Eclipse");
        methodInvocation.arguments().add(literal);
        ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);

        Block block= ast.newBlock();
        block.statements().add(expressionStatement);
        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        methodDeclaration.setName(ast.newSimpleName("main"));
        methodDeclaration.setBody(block);
        implicitTypeDeclaration.bodyDeclarations().add(methodDeclaration);
     // Add Implicity Type class to compilation unit
        compilationUnit.types().add(implicitTypeDeclaration);

        StringBuilder buf = new StringBuilder();
        buf.append("/** \n");
        buf.append(" * Hello\n");
        buf.append(" */\n");
        buf.append("  void main(){\n");
        buf.append("    System.out.println(\"Eclipse\");\n");
        buf.append("  }\n");

        assertEqualString(compilationUnit.toString(), buf.toString());
	}

	//javaDoc
	public void test002() throws Exception {
		AST ast = AST.newAST(AST.JLS23, true);
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf= new StringBuilder();
		buf.append("/** \n");
        buf.append(" * Hello\n");
        buf.append(" */\n");
        buf.append("void main(){\n");
        buf.append("  System.out.println(\"Eclipse\");\n");
        buf.append("}\n");

        ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		ImplicitTypeDeclaration implicitTypeDeclaration= findImplicitDeclaration(astRoot, "");
		List<MethodDeclaration> methodDeclarationsList = implicitTypeDeclaration.bodyDeclarations();
		MethodDeclaration methodDeclaration = methodDeclarationsList.get(0);
		{

			Javadoc javaDoc =  methodDeclaration.getJavadoc();

			Javadoc newJavaDoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Eclipse");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			newJavaDoc.tags().add(tagElement);

			rewrite.replace(javaDoc, newJavaDoc, null);
		}

		String preview = evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();

		buf.append("/**\n");
        buf.append(" * Eclipse\n");
        buf.append(" */\n");
        buf.append("void main(){\n");
        buf.append("  System.out.println(\"Eclipse\");\n");
        buf.append("}\n");

        assertEqualString(preview, buf.toString());

        {
        	Javadoc javaDoc =  methodDeclaration.getJavadoc();
			Javadoc newJavaDoc = null;

			rewrite.replace(javaDoc, newJavaDoc, null);
        }

        preview = evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();

        buf.append("void main(){\n");
        buf.append("  System.out.println(\"Eclipse\");\n");
        buf.append("}\n");

        assertEqualString(preview, buf.toString());
	}

	//adding more MEthodDeclaration
	public void test003() throws Exception {
		AST ast = AST.newAST(AST.JLS23, true);
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf= new StringBuilder();
		buf.append("/** \n");
        buf.append(" * Hello\n");
        buf.append(" */\n");
        buf.append("void main(){\n");
        buf.append("  System.out.println(\"Eclipse\");\n");
        buf.append("}\n");

        ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		ImplicitTypeDeclaration implicitTypeDeclaration= findImplicitDeclaration(astRoot, "");
		{
			MethodInvocation methodInvocation = ast.newMethodInvocation();
			methodInvocation.setName(ast.newSimpleName("println"));

			StringLiteral literal = ast.newStringLiteral();
			literal.setLiteralValue("abc");

			QualifiedName qualifiedName = ast.newQualifiedName(ast.newName("System"), ast.newSimpleName("out"));

	        methodInvocation.setExpression(qualifiedName);
			methodInvocation.arguments().add(literal);

			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);

			Block block = ast.newBlock();
			block.statements().add(expressionStatement);

			MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
			methodDeclaration.setBody(block);
			methodDeclaration.setName(ast.newSimpleName("abc"));
			methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

			ListRewrite listRewrite= rewrite.getListRewrite(implicitTypeDeclaration, ImplicitTypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertAt(methodDeclaration, 1, null);
		}

		String preview = evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();

		buf.append("/** \n");
        buf.append(" * Hello\n");
        buf.append(" */\n");
        buf.append("void main(){\n");
        buf.append("  System.out.println(\"Eclipse\");\n");
        buf.append("}\n");
        buf.append("\n");
        buf.append("void abc() {\n");
        buf.append("    System.out.println(\"abc\");\n");
        buf.append("}\n");

        assertEqualString(preview, buf.toString());
	}
	public void test004() throws Exception {
		AST ast = AST.newAST(AST.JLS23, true);
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf= new StringBuilder();
		buf.append("/** \n");
        buf.append(" * Hello\n");
        buf.append(" */\n");
        buf.append("void main(){\n");
        buf.append("  System.out.println(\"main\");\n");
        buf.append("}\n");
        buf.append("void abc(){\n");
        buf.append("  System.out.println(\"abc\");\n");
        buf.append("}\n");

        ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		ImplicitTypeDeclaration implicitTypeDeclaration= findImplicitDeclaration(astRoot, "");
		List<ASTNode> bodyDeclaration = implicitTypeDeclaration.bodyDeclarations();
		System.out.println("sasi");
		{

			rewrite.remove(bodyDeclaration.get(1), null);//remove one method

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			methodInvocation.setName(ast.newSimpleName("println"));

			StringLiteral literal = ast.newStringLiteral();
			literal.setLiteralValue("xyz");

			QualifiedName qualifiedName = ast.newQualifiedName(ast.newName("System"), ast.newSimpleName("out"));

	        methodInvocation.setExpression(qualifiedName);
			methodInvocation.arguments().add(literal);

			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);

			Block block = ast.newBlock();
			block.statements().add(expressionStatement);

			MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
			methodDeclaration.setBody(block);
			methodDeclaration.setName(ast.newSimpleName("xyz"));
			methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

			ListRewrite listRewrite= rewrite.getListRewrite(implicitTypeDeclaration, ImplicitTypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertAt(methodDeclaration, 1, null);

			String preview = evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();

			buf.append("/** \n");
	        buf.append(" * Hello\n");
	        buf.append(" */\n");
	        buf.append("void main(){\n");
	        buf.append("  System.out.println(\"main\");\n");
	        buf.append("}\n");
	        buf.append("void xyz() {\n");
	        buf.append("    System.out.println(\"xyz\");\n");
	        buf.append("}\n");

	        assertEqualString(preview, buf.toString());

		}
	}

}