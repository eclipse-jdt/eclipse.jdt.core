/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;



import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;


public class ImportRewrite18Test extends AbstractJavaModelTests {


	private static final Class THIS= ImportRewrite18Test.class;
	private static final String PROJECT = "ImportRewrite18TestProject";

	protected IPackageFragmentRoot sourceFolder;

	public ImportRewrite18Test(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}

	public static Test suite() {
		return allTests();
	}

	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject proj= createJavaProject(PROJECT, new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));

		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(1));


		this.sourceFolder = getPackageFragmentRoot(PROJECT, "src");

		waitUntilIndexesReady();
	}

	protected void tearDown() throws Exception {
		deleteProject(PROJECT);
		super.tearDown();
	}

	public void testBug417937a_since_8() throws Exception {
		String contents = "package pack1;\n" +
				"public class X{\n" +
				"	public void foo( pack2.pack3.@Marker B arg , A a) {}\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack1;\n" +
				"public class A{}\n";
		createFile("/" + PROJECT + "/src/pack1/A.java", contents);
		contents = "package pack2/pack3;\n" +
				"public class B {}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFolder("/" + PROJECT + "/src/pack2/pack3");
		createFile("/" + PROJECT + "/src/pack2/pack3/B.java", contents);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(getCompilationUnit("/" + PROJECT + "/src/pack1/A.java"));
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration type= (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration [] methods =  type.getMethods();
		MethodDeclaration method = methods[0];
		VariableDeclaration variable= (VariableDeclaration) method.parameters().get(0);
		IVariableBinding binding = variable.resolveBinding();
		ITypeBinding typeBinding = binding.getType();
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertEquals("@Marker B", actualType.toString());
		assertTrue(actualType.isSimpleType());
		apply(rewrite);
		String contentsA = "package pack1;\n" +
				"\n" +
				"import pack2.pack3.B;\n" +
				"\n" +
				"public class A{}\n";
		assertEqualStringIgnoreDelim(cu.getSource(), contentsA);
	}

	public void testBug417937b_since_8() throws Exception {
		String contents = "package pack1;\n" +
				"public class X{\n" +
				"	public void foo( pack2.pack3.@Marker B arg , A a) {}\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack1;\n" +
				"import pack3.pack4.B;\n" +
				"public class A{\n" +
				"	public void foo(B arg) {}\n" +
				"}\n";
		createFile("/" + PROJECT + "/src/pack1/A.java", contents);
		contents = "package pack2/pack3;\n" +
				"public class B {}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFolder("/" + PROJECT + "/src/pack2/pack3");
		createFile("/" + PROJECT + "/src/pack2/pack3/B.java", contents);
		contents = "package pack3/pack4;\n" +
				"public class B {}\n";
		createFolder("/" + PROJECT + "/src/pack3");
		createFolder("/" + PROJECT + "/src/pack3/pack4");
		createFile("/" + PROJECT + "/src/pack3/pack4/B.java", contents);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(getCompilationUnit("/" + PROJECT + "/src/pack1/A.java"));
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		astRoot = (CompilationUnit) parser.createAST(null);	
		TypeDeclaration type= (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration [] methods =  type.getMethods();
		MethodDeclaration method = methods[0];
		VariableDeclaration variable= (VariableDeclaration) method.parameters().get(0);
		IVariableBinding binding = variable.resolveBinding();
		ITypeBinding typeBinding = binding.getType();
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertEquals("pack2.pack3.@Marker B", actualType.toString());
		assertTrue(actualType.isNameQualifiedType());
		apply(rewrite);
		String contentsA = "package pack1;\n" +
				"import pack3.pack4.B;\n" +
				"public class A{\n" +
				"	public void foo(B arg) {}\n" +
				"}\n";
		assertEqualStringIgnoreDelim(cu.getSource(), contentsA);
	}

	private Type runTest417937candGetType(int i) throws Exception {
		String contents = "package pack1;\n" +
				"public class X{\n" +
				this.bug417937cTestInput[i][0] + "\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Annot1 {\n" +
				"	int value1() default 1;\n" +
				"	int value2();\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack1;\n" +
				"public class A{}\n";
		createFile("/" + PROJECT + "/src/pack1/A.java", contents);
		contents = "package pack2;\n" +
				"public class B1 {\n" +
				"	public class B2 {\n" +
				"		public class B3 {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFile("/" + PROJECT + "/src/pack2/B1.java", contents);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration [] methods =  typeDeclaration.getMethods();
		MethodDeclaration method = methods[0];

		VariableDeclaration variable= (VariableDeclaration) method.parameters().get(0);
		IVariableBinding variableBinding = variable.resolveBinding();
		ITypeBinding typeBinding = variableBinding.getType();
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		apply(rewrite);
		String contentsA = "package pack1;\n" +
				"\n" +
				"import " + this.bug417937cTestInput[i][1] + ";\n" +
				"\n" +
				"public class A{}\n";
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		assertEqualStringIgnoreDelim(cu.getSource(), contentsA);

		return actualType;
	}

	String[][] bug417937cTestInput = {
			{"public void foo000( pack2.@Marker B1.@Marker B2.@Marker B3 arg, A a) {}", "pack2.B1", "@Marker B1.@Marker B2.@Marker B3"},
			{"public void foo001( pack2.@Marker @Marker2 B1.@Marker B2.B3 arg, A a) {}", "pack2.B1", "@Marker @Marker2 B1.@Marker B2.B3"},
			{"public void foo002( pack2.B1.@Marker B2.B3 arg, @Marker int i, A a)", "pack2.B1.B2", "@Marker B2.B3"},
			{"public void foo003( pack2.B1.B2.@Marker B3 arg, A a) {}", "pack2.B1.B2.B3", "@Marker B3"},
			{"public void foo004( pack2.B1.B2.@Annot1(value2=2) B3 arg, A a) {}", "pack2.B1.B2.B3", "@Annot1(value2=2) B3"},
			{"public void foo005( pack2.B1.B2.@Annot1(value2=2,value1=0) B3 arg, A a) {}", "pack2.B1.B2.B3", "@Annot1(value2=2,value1=0) B3"},
			{"public void foo006( pack2.B1.B2.B3 arg, A a) {}", "pack2.B1.B2.B3", "B3"},
	};

	private Type bug417937c_runi_since_8(int i) throws Exception {
		Type actualType = runTest417937candGetType(i);
		assertEquals(this.bug417937cTestInput[i][2], actualType.toString());
		return actualType;
	}

	public void testBug417937c0_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(0);
		assertTrue(type.isQualifiedType());
	}
	public void testBug417937c1_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(1);
		assertTrue(type.isQualifiedType());
	}
	public void testBug417937c2_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(2);
		assertTrue(type.isQualifiedType());
	}
	public void testBug417937c3_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(3);
		assertTrue(type.isSimpleType());
	}
	public void testBug417937c4_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(4);
		assertTrue(type.isSimpleType());
	}
	public void testBug417937c5_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(5);
		assertTrue(type.isSimpleType());
	}
	public void testBug417937c6_since_8() throws Exception {
		Type type = bug417937c_runi_since_8(6);
		assertTrue(type.isSimpleType());
	}

	public void testBug417937d001_since_8() throws Exception {
		String contents = "package pack1;\n" +
				"public class X{\n" +
				"public void foo000( pack3.C1<pack2.B1>.C2<pack2.B1.B2>.C3<pack2.B1> arg, A a) {}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack1;\n" +
				"public class A{}\n";
		createFile("/" + PROJECT + "/src/pack1/A.java", contents);
		contents = "package pack2;\n" +
				"public class B1 {\n" +
				"	public class B2 {\n" +
				"		public class B3 {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFile("/" + PROJECT + "/src/pack2/B1.java", contents);
		contents = "package pack3;\n" +
				"public class C1 <T> {\n" +
				"	public class C2 <P>{\n" +
				"		public class C3 <Q> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n" + 
		createFolder("/" + PROJECT + "/src/pack3");
		createFile("/" + PROJECT + "/src/pack3/C1.java", contents);
		contents = "package pack4;\n" +
				"public class D1 {\n" +
				"	public class D2 <T>{\n" +
				"		public class D3 <S> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
		createFolder("/" + PROJECT + "/src/pack4");
		createFile("/" + PROJECT + "/src/pack4/D1.java", contents);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);	
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration [] methods =  typeDeclaration.getMethods();
		MethodDeclaration method = methods[0];

		VariableDeclaration variable= (VariableDeclaration) method.parameters().get(0);
		IVariableBinding variableBinding = variable.resolveBinding();
		ITypeBinding typeBinding = variableBinding.getType();
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		apply(rewrite);
		String contentsA = "package pack1;\n" +
				"\n" +
				"import pack2.B1;\n" +
				"import pack2.B1.B2;\n" +
				"import pack3.C1;\n" +
				"\n" +
				"public class A{}\n";
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		assertEqualStringIgnoreDelim(cu.getSource(), contentsA);
		assertEquals("C1<B1>.C2<B2>.C3<B1>", actualType.toString());
		assertTrue(actualType.isParameterizedType());
	}

	public void testBug417937d002_since_8() throws Exception {
		String contents = "package pack1;\n" +
				"public class X{\n" +
				"public void foo001( pack4.D1.D2<pack2.B1>.D3<pack2.B1> arg, A a) {}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack1;\n" +
				"public class A{}\n";
		createFile("/" + PROJECT + "/src/pack1/A.java", contents);
		contents = "package pack2;\n" +
				"public class B1 {\n" +
				"	public class B2 {\n" +
				"		public class B3 {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFile("/" + PROJECT + "/src/pack2/B1.java", contents);
		contents = "package pack3;\n" +
				"public class C1 <T> {\n" +
				"	public class C2 <P>{\n" +
				"		public class C3 <Q> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
		createFolder("/" + PROJECT + "/src/pack3");
		createFile("/" + PROJECT + "/src/pack3/C1.java", contents);
		contents = "package pack4;\n" +
				"public class D1 {\n" +
				"	public class D2 <T>{\n" +
				"		public class D3 <S> {\n" +
				"			\n" +
				"		}\n" +
				"	}\n" +
				"}\n" + 
		createFolder("/" + PROJECT + "/src/pack4");
		createFile("/" + PROJECT + "/src/pack4/D1.java", contents);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration [] methods =  typeDeclaration.getMethods();
		MethodDeclaration method = methods[0];

		VariableDeclaration variable= (VariableDeclaration) method.parameters().get(0);
		IVariableBinding variableBinding = variable.resolveBinding();
		ITypeBinding typeBinding = variableBinding.getType();
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		apply(rewrite);
		String contentsA = "package pack1;\n" +
				"\n" +
				"import pack2.B1;\n" +
				"import pack4.D1.D2;\n" +
				"\n" +
				"public class A{}\n";
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		assertEqualStringIgnoreDelim(cu.getSource(), contentsA);
		assertEquals("D2<B1>.D3<B1>", actualType.toString());
		assertTrue(actualType.isParameterizedType());
	}

	public void testBug417937e_since_8() throws Exception {
		String contents = "package pack1;\n" +
				"import pack2.B1;\n" +
				"public class X{\n" +
				"    public void foo001(B1<C1>.B2<C1>.@Annot(true) B3<C1> arg, A a) {}\n" +
				"}\n" +
				"class C1{}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Annot {\n	boolean value() default false;\n}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack1;\n" +
				"public class A{}\n";
		createFile("/" + PROJECT + "/src/pack1/A.java", contents);
		contents = "package pack2;\n" +
				"public class B1<T> {\n" +
				"	public class B2<P> {\n" +
				"		public class B3<Q> {\n" +
				"		}\n" +
				"	}\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFile("/" + PROJECT + "/src/pack2/B1.java", contents);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration [] methods =  typeDeclaration.getMethods();
		MethodDeclaration method = methods[0];

		VariableDeclaration variable= (VariableDeclaration) method.parameters().get(0);
		IVariableBinding variableBinding = variable.resolveBinding();
		ITypeBinding typeBinding = variableBinding.getType();
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/A.java");
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertTrue(actualType.isParameterizedType());
	}

	private void assertEqualStringIgnoreDelim(String actual, String expected) throws IOException {
		StringAsserts.assertEqualStringIgnoreDelim(actual, expected);
	}

	private ImportRewrite newImportsRewrite(ICompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) throws CoreException, BackingStoreException {
		ImportRewrite rewrite= ImportRewrite.create(cu, restoreExistingImports);
		rewrite.setImportOrder(order);
		rewrite.setOnDemandImportThreshold(normalThreshold);
		rewrite.setStaticOnDemandImportThreshold(staticThreshold);
		return rewrite;
	}

	protected ImportRewrite newImportsRewrite(CompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) {
		ImportRewrite rewrite= ImportRewrite.create(cu, restoreExistingImports);
		rewrite.setImportOrder(order);
		rewrite.setOnDemandImportThreshold(normalThreshold);
		rewrite.setStaticOnDemandImportThreshold(staticThreshold);
		return rewrite;
	}

	private void apply(ImportRewrite rewrite) throws CoreException, MalformedTreeException, BadLocationException {
		TextEdit edit= rewrite.rewriteImports(null);

		// not the efficient way!
		ICompilationUnit compilationUnit= rewrite.getCompilationUnit();
		Document document= new Document(compilationUnit.getSource());
		edit.apply(document);
		compilationUnit.getBuffer().setContents(document.get());
	}
}