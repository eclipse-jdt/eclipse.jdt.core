/*******************************************************************************
* Copyright (c) 2020, 2022 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingRecordDeclarationTest extends ASTRewritingTest {

	public ASTRewritingRecordDeclarationTest(String name) {
		super(name, 16);
	}

	public ASTRewritingRecordDeclarationTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingRecordDeclarationTest.class, 16);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpProjectAbove16();
	}

	@SuppressWarnings("deprecation")
	private boolean checkAPILevel() {
		if (this.apiLevel < 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public void testRecord_001() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
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
		{ // add record component
			List recordComponents = record.recordComponents();
			assertTrue("must be 0 parameters", recordComponents.size() == 0);
			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			newParam.setName(ast.newSimpleName("param1"));
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.RECORD_COMPONENTS_PROPERTY);
			listRewrite.insertFirst(newParam, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int param1) {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testRecord_002() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
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
		{ // add new constructor with parameter
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

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n\n");
		buf.append("        public C(int param1) {\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_003() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int age) {\n");
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
		{ // rename first param & last throw statement
			MethodDeclaration methodDecl1 = ast.newMethodDeclaration();
			methodDecl1.setName(ast.newSimpleName("age"));
			methodDecl1.modifiers().addAll(ast.newModifiers( Modifier.PUBLIC));
			methodDecl1.setReturnType2(ast.newPrimitiveType(PrimitiveType.INT));
			Block body= ast.newBlock();
			ReturnStatement returnStatement = ast.newReturnStatement();
			returnStatement.setExpression(ast.newSimpleName("age"));
			body.statements().add(returnStatement);
			methodDecl1.setBody(body);
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertLast(methodDecl1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int age) {\n\n");
		buf.append("    public int age() {\n");
		buf.append("        return age;\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_004() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(){\n");
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

		ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
		SimpleType newInterface= ast.newSimpleType(ast.newSimpleName("A"));
		listRewrite.insertLast(newInterface, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() implements A{\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testRecord_005() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() implements A{\n");
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

		ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
		SimpleType newInterface= ast.newSimpleType(ast.newSimpleName("B"));
		listRewrite.insertLast(newInterface, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() implements A, B{\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("rawtypes")
	public void testRecord_006() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int param1){\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // remove record component
			List recordComponents = record.recordComponents();
			assertTrue("must be 1 parameters", recordComponents.size() == 1);
			rewrite.remove((ASTNode)recordComponents.get(0), null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(){\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("rawtypes")
	public void testRecord_007() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int param1){\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // rename record component
			List recordComponents = record.recordComponents();
			assertTrue("must be 1 parameters", recordComponents.size() == 1);
			SingleVariableDeclaration newParam= astRoot.getAST().newSingleVariableDeclaration();
			newParam.setType(astRoot.getAST().newPrimitiveType(PrimitiveType.INT));
			newParam.setName(astRoot.getAST().newSimpleName("param2"));
			rewrite.replace((SingleVariableDeclaration)recordComponents.get(0), newParam, null);

		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int param2){\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testRecord_008() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		{ // change record name
			rewrite.set(type, RecordDeclaration.NAME_PROPERTY, ast.newSimpleName("X"), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record X() {\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_009() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		{ // add java doc
			Javadoc javadoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.set(type, RecordDeclaration.JAVADOC_PROPERTY, javadoc, null);
		}

		{ // change modifier to private
			rewrite.remove((ASTNode) type.modifiers().get(0), null);
			ListRewrite listRewrite= rewrite.getListRewrite(type, RecordDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertFirst(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/**\n");
		buf.append(" * Hello\n");
		buf.append(" */\n");
		buf.append("private record C() {\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_010() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/** javadoc comment */\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());


		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		{ // remove java doc
			rewrite.remove(type.getJavadoc(), null);
		}

		{ // remove modifier
			rewrite.remove((ASTNode) type.modifiers().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("record C() {\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_011() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/** javadoc comment */\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		{ // replace java doc
			Javadoc javadoc= ast.newJavadoc();
			TextElement textElem= ast.newTextElement();
			textElem.setText("Hello");
			TagElement tagElement= ast.newTagElement();
			tagElement.fragments().add(textElem);
			javadoc.tags().add(tagElement);
			rewrite.replace(type.getJavadoc(), javadoc, null);
		}

		{ // add modifier
			ListRewrite listRewrite= rewrite.getListRewrite(type, RecordDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.insertLast(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("/**\n");
		buf.append(" * Hello\n");
		buf.append(" */\n");
		buf.append("public final record C() {\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_012() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		ListRewrite declarations= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		{ // add record in class
			RecordDeclaration record = ast.newRecordDeclaration();
			record.setName(ast.newSimpleName("X"));
			declarations.insertFirst(record, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("\n");
		buf.append("    record X() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("}\n");


		assertEqualString(preview, buf.toString());

	}

	public void testRecord_013() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		ListRewrite declarations= rewrite.getListRewrite(type, RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
		{ // add record in record
			RecordDeclaration record = ast.newRecordDeclaration();
			record.setName(ast.newSimpleName("X"));
			declarations.insertFirst(record, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("    record X() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("}\n");


		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings("rawtypes")
	public void testRecord_015() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("    record X() {\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		ListRewrite declarations= rewrite.getListRewrite(type, RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
		{ // remove record from record
			List types = (List)type.getStructuralProperty(RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
			declarations.remove((RecordDeclaration)types.get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}


	@SuppressWarnings("rawtypes")
	public void testRecord_0015() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int param1) {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // add record component
			List recordComponents = record.recordComponents();
			assertTrue("must be 1 parameter", recordComponents.size() == 1);
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.RECORD_COMPONENTS_PROPERTY);
			listRewrite.remove((SingleVariableDeclaration)recordComponents.get(0), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_016() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
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
		{ // replace compact constructor
			MethodDeclaration methodDecl1 = ast.newMethodDeclaration();
			methodDecl1.setName(ast.newSimpleName("C"));
			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			newParam.setName(ast.newSimpleName("param1"));
			methodDecl1.parameters().add(newParam);
			methodDecl1.setConstructor(Boolean.TRUE);
			methodDecl1.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			Block body= ast.newBlock();
			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral literal = ast.newStringLiteral();
			literal.setLiteralValue("Hello world");//$NON-NLS-1$
			methodInvocation.arguments().add(literal);
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			body.statements().add(expressionStatement);
			methodDecl1.setBody(body);
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.replace(methodDecl, methodDecl1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C(int param1) {\n");
		buf.append("            System.out.println(\"Hello world\");\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings("rawtypes")
	public void testRecord_0016() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() implements A{\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;

		{
			// Remove interface
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
			List interfaces = record.superInterfaceTypes();
			listRewrite.remove((Type)interfaces.get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(){\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	@SuppressWarnings("rawtypes")
	public void testRecord_0017() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() implements A{\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;

		{
			// change interface
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
			List interfaces = record.superInterfaceTypes();
			SimpleType newInterface= astRoot.getAST().newSimpleType(astRoot.getAST().newSimpleName("B"));
			listRewrite.replace((SimpleType)interfaces.get(0), newInterface, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() implements B{\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testRecord_0018() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
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
		{ // add var arg property for record component
			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			newParam.setName(ast.newSimpleName("param1"));
			newParam.setVarargs(Boolean.TRUE);
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.RECORD_COMPONENTS_PROPERTY);
			listRewrite.insertFirst(newParam, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int... param1) {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_0019() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int... param) {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // remove var arg property of record component
			rewrite.set((SingleVariableDeclaration) record.recordComponents().get(0), SingleVariableDeclaration.VARARGS_PROPERTY, Boolean.FALSE, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int param) {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_0020() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C(int... param) {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // remove record component with var arg
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.RECORD_COMPONENTS_PROPERTY);
			listRewrite.remove((SingleVariableDeclaration)record.recordComponents().get(0), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_0021() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
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
		{ // add parameter type
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.TYPE_PARAMETERS_PROPERTY);
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName("X"));
			listRewrite.insertFirst(typeParameter, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C<X>() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_0022() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C<X>() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // remove parameter type
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.TYPE_PARAMETERS_PROPERTY);
			listRewrite.remove((TypeParameter)record.typeParameters().get(0), null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C () {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_023() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C<X>() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast = astRoot.getAST();
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "C");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{ // change parameter type
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.TYPE_PARAMETERS_PROPERTY);
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName("Y"));
			listRewrite.replace((TypeParameter)record.typeParameters().get(0), typeParameter, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C<Y>() {\n");
		buf.append("		public C {\n");
		buf.append("			System.out.println(\"error\");\n");
		buf.append("		}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_024() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
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
		{ // add compact constructor
			MethodDeclaration methodDecl1 = ast.newMethodDeclaration();
			methodDecl1.setName(ast.newSimpleName("C"));
			methodDecl1.setConstructor(Boolean.TRUE);
			methodDecl1.setCompactConstructor(Boolean.TRUE);
			Block body= ast.newBlock();
			methodDecl1.setBody(body);
			methodDecl1.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
			ListRewrite listRewrite= rewrite.getListRewrite(record, RecordDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewrite.insertFirst(methodDecl1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record C() {\n");
		buf.append("\n");
		buf.append("    public C{}\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings("rawtypes")
	public void testRecord_025() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("import java.lang.annotation.Target;\n");
		buf.append("record X(@MyAnnot int lo) {\n");
		buf.append("	public int lo() {\n");
		buf.append("		return this.lo;\\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@Target({ElementType.FIELD})\n");
		buf.append("@interface MyAnnot {}");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "X");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		{
			List recordComponents = record.recordComponents();
			assertTrue("must be 1 parameter", recordComponents.size() == 1);
			SingleVariableDeclaration recordComp = (SingleVariableDeclaration)recordComponents.get(0);
			// remove annotation
			ListRewrite listRewrite= rewrite.getListRewrite(recordComp, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			listRewrite.remove((MarkerAnnotation)(recordComp.modifiers().get(0)), null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("import java.lang.annotation.Target;\n");
		buf.append("record X(int lo) {\n");
		buf.append("	public int lo() {\n");
		buf.append("		return this.lo;\\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@Target({ElementType.FIELD})\n");
		buf.append("@interface MyAnnot {}");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings("rawtypes")
	public void testRecord_026() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("import java.lang.annotation.Target;\n");
		buf.append("record X(@MyAnnot int lo) {\n");
		buf.append("	public int lo() {\n");
		buf.append("		return this.lo;\\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@Target({ElementType.FIELD})\n");
		buf.append("@interface MyAnnot {}");
		buf.append("@Target({ElementType.RECORD_COMPONENT})\n");
		buf.append("@interface MyAnnotNew {}");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "X");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		AST ast= astRoot.getAST();
		{
			List recordComponents = record.recordComponents();
			assertTrue("must be 1 parameter", recordComponents.size() == 1);
			SingleVariableDeclaration recordComp = (SingleVariableDeclaration)recordComponents.get(0);
			// modify annotation
			ListRewrite listRewrite= rewrite.getListRewrite(recordComp, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("MyAnnotNew"));
			listRewrite.replace((MarkerAnnotation)(recordComp.modifiers().get(0)), annot, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("import java.lang.annotation.Target;\n");
		buf.append("record X(@MyAnnotNew int lo) {\n");
		buf.append("	public int lo() {\n");
		buf.append("		return this.lo;\\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@Target({ElementType.FIELD})\n");
		buf.append("@interface MyAnnot {}");
		buf.append("@Target({ElementType.RECORD_COMPONENT})\n");
		buf.append("@interface MyAnnotNew {}");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings("rawtypes")
	public void testRecord_027() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("import java.lang.annotation.Target;\n");
		buf.append("record X(int lo) {\n");
		buf.append("	public int lo() {\n");
		buf.append("		return this.lo;\\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@Target({ElementType.RECORD_COMPONENT})\n");
		buf.append("@interface MyAnnot {}");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		AbstractTypeDeclaration type= findAbstractTypeDeclaration(astRoot, "X");
		assertTrue("Not a record", type instanceof RecordDeclaration);
		RecordDeclaration record = (RecordDeclaration)type;
		AST ast= astRoot.getAST();
		{
			List recordComponents = record.recordComponents();
			assertTrue("must be 1 parameter", recordComponents.size() == 1);
			SingleVariableDeclaration recordComp = (SingleVariableDeclaration)recordComponents.get(0);
			// add annotation
			ListRewrite listRewrite= rewrite.getListRewrite(recordComp, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			MarkerAnnotation annot= ast.newMarkerAnnotation();
			annot.setTypeName(ast.newSimpleName("MyAnnot"));
			listRewrite.insertFirst(annot, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("import java.lang.annotation.Target;\n");
		buf.append("record X(@MyAnnot int lo) {\n");
		buf.append("	public int lo() {\n");
		buf.append("		return this.lo;\\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@Target({ElementType.RECORD_COMPONENT})\n");
		buf.append("@interface MyAnnot {}");

		assertEqualString(preview, buf.toString());

	}


	public void testRecord_028() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Y.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "Y");
		ListRewrite declarations = rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		{
		    RecordDeclaration record = ast.newRecordDeclaration();
		    record.setName(ast.newSimpleName("X"));

		    VariableDeclarationFragment variableDeclarationFragment = ast.newVariableDeclarationFragment();
		    variableDeclarationFragment.setName(ast.newSimpleName("staticField"));

		    FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(variableDeclarationFragment);
		    fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
		    fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));

		    record.bodyDeclarations().add(fieldDeclaration);

		    declarations.insertFirst(record, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class Y {\n");
		buf.append("\n");
		buf.append("    record X() {\n");
		buf.append("        private static final int staticField;\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	public void testRecord_029_a() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		String code = """
					package test1;
					record Test(String name) {
					    public static Builder builder() {}
					    public static final class Builder {}
					}
				""";

		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", code, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		List <RecordDeclaration> methods= astRoot.types();
		MethodDeclaration methodDecl= findMethodDeclaration(methods.get(0), "builder");
		{
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String reWriteCode = """
					package test1;
					record Test(String name) {
					    public static final class Builder {}
					}
				""";

		assertEqualString(preview, reWriteCode);
	}

	public void testRecord_029_b() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		String code = """
					package test1;
					public class Test {
					    public static Builder builder() {}
					    public static final class Builder {}
					}
				""";

		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", code, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		List <TypeDeclaration> methods= astRoot.types();
		MethodDeclaration methodDecl= findMethodDeclaration(methods.get(0), "builder");
		{
			rewrite.remove(methodDecl, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String reWriteCode = """
					package test1;
					public class Test {
					    public static final class Builder {}
					}
				""";

		assertEqualString(preview, reWriteCode);
	}

	public void testRecord_029_c() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		String code = """
					package test1;
					record Test(String name) {
					    public static Builder builder() {}
					    public static final class Builder {}
					    public static Builder builderNew() {}
					    public static final class builderNew {
					    	builderNew(){
					    		System.out.println("Test");
					    	}
					    }
					}
				""";

		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", code, false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		List <RecordDeclaration> methods= astRoot.types();
		MethodDeclaration methodDecl= findMethodDeclaration(methods.get(0), "builder");
		MethodDeclaration methodDeclNew= findMethodDeclaration(methods.get(0), "builderNew");
		{
			rewrite.remove(methodDecl, null);
			rewrite.remove(methodDeclNew, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		String reWriteCode = """
					package test1;
					record Test(String name) {
					    public static final class Builder {}
					    public static final class builderNew {
					    	builderNew(){
					    		System.out.println("Test");
					    	}
					    }
					}
				""";

		assertEqualString(preview, reWriteCode);
	}

}

