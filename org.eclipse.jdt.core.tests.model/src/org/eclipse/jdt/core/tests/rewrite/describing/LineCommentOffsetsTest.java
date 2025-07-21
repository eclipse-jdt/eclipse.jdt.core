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
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.HashSet;
import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.dom.rewrite.LineCommentEndOffsets;

@SuppressWarnings({"rawtypes", "unchecked"})
public class LineCommentOffsetsTest extends ASTRewritingTest {

	public LineCommentOffsetsTest(String name) {
		super(name);
	}
	public LineCommentOffsetsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(LineCommentOffsetsTest.class);
	}

	public void testEmptyLineComments() throws Exception {

		StringBuilder buf= new StringBuilder();
		buf.append("\n");

		LineCommentEndOffsets offsets= new LineCommentEndOffsets(null);
		boolean res= offsets.isEndOfLineComment(0);
		assertFalse(res);
		res= offsets.remove(0);
		assertFalse(res);
	}

	public void testRemove_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		StringBuilder buf= new StringBuilder();
		buf.append("package test1;//comment Y\n");
		buf.append("public class E//comment Y\n");
		buf.append("{//comment Y\n");
		buf.append("}//comment Y");
		String contents= buf.toString();
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", contents, false, null);

		CompilationUnit astRoot= createAST(cu);

		LineCommentEndOffsets offsets= new LineCommentEndOffsets(astRoot.getCommentList());

		int p1= contents.indexOf('Y') + 1;
		int p2= contents.indexOf('Y', p1) + 1;
		int p3= contents.indexOf('Y', p2) + 1;
		int p4= contents.indexOf('Y', p3) + 1;

		assertFalse(offsets.isEndOfLineComment(0));
		assertTrue(offsets.isEndOfLineComment(p1));
		assertTrue(offsets.isEndOfLineComment(p2));
		assertTrue(offsets.isEndOfLineComment(p3));
		assertTrue(offsets.isEndOfLineComment(p4));

		boolean res= offsets.remove(p2);
		assertTrue(res);

		res= offsets.remove(p2);
		assertFalse(res);

		assertFalse(offsets.isEndOfLineComment(0));
		assertTrue(offsets.isEndOfLineComment(p1));
		assertFalse(offsets.isEndOfLineComment(p2));
		assertTrue(offsets.isEndOfLineComment(p3));
		assertTrue(offsets.isEndOfLineComment(p4));

		res= offsets.remove(p4);
		assertTrue(res);

		assertFalse(offsets.isEndOfLineComment(0));
		assertTrue(offsets.isEndOfLineComment(p1));
		assertFalse(offsets.isEndOfLineComment(p2));
		assertTrue(offsets.isEndOfLineComment(p3));
		assertFalse(offsets.isEndOfLineComment(p4));

		res= offsets.remove(p1);
		assertTrue(res);

		assertFalse(offsets.isEndOfLineComment(0));
		assertFalse(offsets.isEndOfLineComment(p1));
		assertFalse(offsets.isEndOfLineComment(p2));
		assertTrue(offsets.isEndOfLineComment(p3));
		assertFalse(offsets.isEndOfLineComment(p4));
	}



	public void testLineCommentEndOffsets() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);


		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/* comment */\n");
		buf.append("// comment Y\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment Y\n");
		buf.append("            i++;\n");
		buf.append("        }// comment// comment Y\n");
		buf.append("        return;\n");
		buf.append("    }\n");
		buf.append("} // comment Y");
		String content= buf.toString();

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", content, false, null);
		CompilationUnit astRoot= createAST(cu);

		LineCommentEndOffsets offsets= new LineCommentEndOffsets(astRoot.getCommentList());
		HashSet expectedOffsets= new HashSet();

		for (int i= 0; i < content.length(); i++) {
			char ch= content.charAt(i);
			if (ch == 'Y') {
				expectedOffsets.add(Integer.valueOf(i + 1));
			}
		}

		int count= 0;

		char[] charContent= content.toCharArray();
		for (int i= 0; i <= content.length() + 5; i++) {
			boolean expected= i > 0 && i <= content.length() && charContent[i - 1] == 'Y';
			boolean actual= offsets.isEndOfLineComment(i, charContent);
			assertEquals(expected, actual);

			actual= offsets.isEndOfLineComment(i);
			assertEquals(expected, actual);

			if (expected) {
				count++;
			}

		}
		assertEquals(4, count);
	}

	public void testLineCommentEndOffsetsMixedLineDelimiter() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/* comment */\r\n");
		buf.append("// comment Y\n");
		buf.append("public class E {\r\n");
		buf.append("    public void foo() {\n");
		buf.append("        while (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment Y\r\n");
		buf.append("            i++;\n");
		buf.append("        }// comment// comment Y\r");
		buf.append("        return;\n");
		buf.append("    }\r\n");
		buf.append("} // comment Y");
		String content= buf.toString();

		ICompilationUnit cu= pack1.createCompilationUnit("E.java", content, false, null);
		CompilationUnit astRoot= createAST(cu);

		LineCommentEndOffsets offsets= new LineCommentEndOffsets(astRoot.getCommentList());
		HashSet expectedOffsets= new HashSet();

		for (int i= 0; i < content.length(); i++) {
			char ch= content.charAt(i);
			if (ch == 'Y') {
				expectedOffsets.add(Integer.valueOf(i + 1));
			}
		}

		int count= 0;

		char[] charContent= content.toCharArray();
		for (int i= 0; i <= content.length() + 5; i++) {
			boolean expected= i > 0 && i <= content.length() && charContent[i - 1] == 'Y';
			boolean actual= offsets.isEndOfLineComment(i, charContent);
			assertEquals(expected, actual);
			if (expected) {
				count++;
			}

		}
		assertEquals(4, count);
	}

	public void testCommentInLists_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E implements A //comment\n");
		buf.append("{\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
		SimpleType newInterface= ast.newSimpleType(ast.newSimpleName("B"));
		listRewrite.insertLast(newInterface, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E implements A //comment\n");
		buf.append(", B\n");
		buf.append("{\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testCommentInType_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E //comment\n");
		buf.append("{\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
		SimpleType newInterface= ast.newSimpleType(ast.newSimpleName("B"));
		listRewrite.insertLast(newInterface, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E //comment\n");
		buf.append(" implements B\n");
		buf.append("{\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testBug103340_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E //implements List\n");
		buf.append("{\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
		TypeParameter newType= ast.newTypeParameter();
		newType.setName(ast.newSimpleName("X"));
		listRewrite.insertLast(newType, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E //implements List\n");
		buf.append("<X>\n");
		buf.append("{\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testBug95839_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    object.method(\n");
		buf.append("      param1, // text about param1\n");
		buf.append("      param2  // text about param2\n");
		buf.append("    );\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		ExpressionStatement statement= (ExpressionStatement) ((MethodDeclaration) type.bodyDeclarations().get(0)).getBody().statements().get(0);
		MethodInvocation inv= (MethodInvocation) statement.getExpression();

		ListRewrite listRewrite= rewrite.getListRewrite(inv, MethodInvocation.ARGUMENTS_PROPERTY);
		listRewrite.insertLast(ast.newSimpleName("param3"), null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    object.method(\n");
		buf.append("      param1, // text about param1\n");
		buf.append("      param2  // text about param2\n");
		buf.append(", param3\n");
		buf.append("    );\n");
		buf.append("  }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testBug114418_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    try {\n");
		buf.append("    } catch (IOException e) {\n");
		buf.append("    }\n");
		buf.append("    // comment\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		TryStatement statement= (TryStatement) ((MethodDeclaration) type.bodyDeclarations().get(0)).getBody().statements().get(0);

		ListRewrite listRewrite= rewrite.getListRewrite(statement, TryStatement.CATCH_CLAUSES_PROPERTY);
		CatchClause clause= ast.newCatchClause();
		SingleVariableDeclaration newSingleVariableDeclaration= ast.newSingleVariableDeclaration();
		newSingleVariableDeclaration.setName(ast.newSimpleName("e"));
		newSingleVariableDeclaration.setType(ast.newSimpleType(ast.newSimpleName("MyException")));
		clause.setException(newSingleVariableDeclaration);

		listRewrite.insertLast(clause, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    try {\n");
		buf.append("    } catch (IOException e) {\n");
		buf.append("    }\n");
		buf.append("    // comment\n");
		buf.append(" catch (MyException e) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testBug128818_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    if (true) {\n");
		buf.append("    } // comment\n");
		buf.append("    else\n");
		buf.append("      return;\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		IfStatement statement= (IfStatement) ((MethodDeclaration) type.bodyDeclarations().get(0)).getBody().statements().get(0);

		rewrite.set(statement, IfStatement.ELSE_STATEMENT_PROPERTY, ast.newBlock(), null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    if (true) {\n");
		buf.append("    } // comment\n");
		buf.append(" else {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testBug128422_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    if (i != 0 //I don't like 0\n");
		buf.append("                 && i != 10) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		IfStatement statement= (IfStatement) ((MethodDeclaration) type.bodyDeclarations().get(0)).getBody().statements().get(0);
		Expression expression= ((InfixExpression) statement.getExpression()).getLeftOperand();

		ParenthesizedExpression parenthesizedExpression= ast.newParenthesizedExpression();
		parenthesizedExpression.setExpression( (Expression) rewrite.createCopyTarget(expression));
		rewrite.replace(expression, parenthesizedExpression, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    if ((i != 0 //I don't like 0\n");
		buf.append(")\n");
		buf.append("                 && i != 10) {\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testBug128422b_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    foo(); //comment\n");
		buf.append("    foo();\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration method= (MethodDeclaration) type.bodyDeclarations().get(0);
		List statements= method.getBody().statements();
		ASTNode copy= rewrite.createCopyTarget((ASTNode) statements.get(0));

		Block newBlock= ast.newBlock();
		newBlock.statements().add(newStatement(ast));
		newBlock.statements().add(copy);
		newBlock.statements().add(newStatement(ast));

		rewrite.getListRewrite(method.getBody(), Block.STATEMENTS_PROPERTY).insertLast(newBlock, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("  void foo() {\n");
		buf.append("    foo(); //comment\n");
		buf.append("    foo();\n");
		buf.append("    {\n");
		buf.append("        bar();\n");
		buf.append("        foo(); //comment\n");
		buf.append("        bar();\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	private Statement newStatement(AST ast) {
		MethodInvocation inv= ast.newMethodInvocation();
		inv.setName(ast.newSimpleName("bar"));
		return ast.newExpressionStatement(inv);
	}

	public void testCommentAtEnd_since_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E \n");
		buf.append("{\n");
		buf.append("}//comment");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		ListRewrite listRewrite= rewrite.getListRewrite(astRoot, CompilationUnit.TYPES_PROPERTY);
		TypeDeclaration newType= ast.newTypeDeclaration();
		newType.setName(ast.newSimpleName("B"));
		listRewrite.insertLast(newType, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E \n");
		buf.append("{\n");
		buf.append("}//comment\n");
		buf.append("\n");
		buf.append("class B {\n");
		buf.append("}");
		assertEqualString(preview, buf.toString());
	}



}
