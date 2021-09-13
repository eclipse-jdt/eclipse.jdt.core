/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.io.IOException;
import java.util.List;

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
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;

import junit.framework.Test;


@SuppressWarnings("rawtypes")
public class ImportRewrite_RecordTest extends AbstractJavaModelTests {


	private static final Class THIS= ImportRewrite_RecordTest.class;
	private static final String PROJECT = "ImportRewrite14TestProject";

	protected IPackageFragmentRoot sourceFolder;


	public ImportRewrite_RecordTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return allTests();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject proj= createJavaProject(PROJECT, new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "15");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_16);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));

		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(1));


		this.sourceFolder = getPackageFragmentRoot(PROJECT, "src");

		waitUntilIndexesReady();
	}

	@SuppressWarnings("deprecation")
	protected static int getJLS16() {
		return AST.JLS16;
	}
	@Override
	protected void tearDown() throws Exception {
		deleteProject(PROJECT);
		super.tearDown();
	}

	/*
	 * typeBinding shows "int value" rather than "@MyAnnotation int value"
	 */
	public void test001() throws Exception {
		String contents = "package pack1;\n" +
				"import pack2.MyAnnotation;\n" +
				"public record X(@MyAnnotation int value){\n" +
				"}\n";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);
		contents = "package pack2;\n" +
				"public @interface MyAnnotation{}\n";
		createFolder("/" + PROJECT + "/src/pack2");
		createFile("/" + PROJECT + "/src/pack2/MyAnnotation.java", contents);

		ASTParser parser = ASTParser.newParser(getJLS16());
		parser.setSource(getCompilationUnit("/" + PROJECT + "/src/pack2/MyAnnotation.java"));
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		astRoot = (CompilationUnit) parser.createAST(null);
		RecordDeclaration record= (RecordDeclaration) astRoot.types().get(0);
		List<SingleVariableDeclaration> recordComponents = record.recordComponents();
		SingleVariableDeclaration recordComponent = recordComponents.get(0);
		assertEquals("Record component type is int" , "int", recordComponent.getType().toString());
		IVariableBinding binding = recordComponent.resolveBinding();

		ITypeBinding typeBinding = binding.getType();
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertEquals("int", actualType.toString());
		assertTrue(actualType.isPrimitiveType());
		apply(rewrite);
		String contentsA = "package pack1;\n" +
				"import pack2.MyAnnotation;\n" +
				"public record X(@MyAnnotation int value){\n"+
				"}\n";
		assertEqualStringIgnoreDelim(cu.getSource(), contentsA);
	}

	/*
	 * Import should not be added in the default package
	 */
	public void testBug563375_2() throws Exception {
		String contents = ""+
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		var i_S = i_s();\n" +
			"		System.out.println(i_S.i + i_S.s);\n" +
			"		}\n" +
			"	\n" +
			"	static record I_S(int i, String s) {}\n" +
			"	\n" +
			"	private static I_S i_s() {\n" +
			"		return new I_S(1, \"abc\");\n" +
			"	}\n" +
			"}";
		createFile("/" + PROJECT + "/src/X.java", contents);

		ICompilationUnit cu= getCompilationUnit("/" + PROJECT + "/src/X.java");
		ASTParser parser = ASTParser.newParser(getJLS16());
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);


		TypeDeclaration clazz= (TypeDeclaration) astRoot.types().get(0);
		ITypeBinding binding= clazz.resolveBinding();
		ITypeBinding recBinding= binding.getDeclaredTypes()[0];
		assertNotNull(recBinding);
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		cu = getCompilationUnit("/" + PROJECT + "/src/X.java");
		rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		String actualType = rewrite.addImport(recBinding);
		assertEquals("X.I_S", actualType);
		apply(rewrite);
		assertEquals(0, cu.getImports().length);
	}

	protected void assertEqualStringIgnoreDelim(String actual, String expected) throws IOException {
		StringAsserts.assertEqualStringIgnoreDelim(actual, expected);
	}

	protected ImportRewrite newImportsRewrite(ICompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) throws CoreException, BackingStoreException {
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

	protected void apply(ImportRewrite rewrite) throws CoreException, MalformedTreeException, BadLocationException {
		TextEdit edit= rewrite.rewriteImports(null);

		// not the efficient way!
		ICompilationUnit compilationUnit= rewrite.getCompilationUnit();
		Document document= new Document(compilationUnit.getSource());
		edit.apply(document);
		compilationUnit.getBuffer().setContents(document.get());
	}

}
