/*******************************************************************************
 * Copyright (c) 2014, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;


@SuppressWarnings("rawtypes")
public class ImportRewrite25Test extends AbstractJavaModelTests {


	private static final Class THIS= ImportRewrite25Test.class;
	private static final String PROJECT = "ImportRewrite25TestProject";

	protected IPackageFragmentRoot sourceFolder;
	protected IJavaProject project;

	public ImportRewrite25Test(String name) {
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

		IJavaProject proj= createJavaProject(PROJECT, new String[] {"src"}, new String[] {"JCL_25_LIB"}, "bin", "25");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_25);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_25);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_25);
		proj.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		proj.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));

		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(1));

		setupExternalJCL("jclMin25");
		JavaCore.setClasspathVariables(
				new String[] {"CONVERTER_JCL_25_LIB", "CONVERTER_JCL_25_SRC", "CONVERTER_JCL_25_SRCROOT"},
				new IPath[] {getConverterJCLPath("25"), getConverterJCLSourcePath("25"), getConverterJCLRootSourcePath()},
				null);

		this.project= proj;
		this.sourceFolder = getPackageFragmentRoot(PROJECT, "src");

		waitUntilIndexesReady();
	}
	protected static int getJLS25() {
		return AST.JLS25;
	}
	@Override
	protected void tearDown() throws Exception {
		deleteProject(PROJECT);
		super.tearDown();
	}

	// From Java 24 onwards, we will keep the jclMin*jar and convertJclMin*jar one and same
	// The /JCL/build.xml has been updated to produce only jclMin*.jar
	private String jclMinName(String compliance) {
		long jdkLevel = CompilerOptions.versionToJdkLevel(compliance);
		return (jdkLevel >= ClassFileConstants.JDK24) ? "jclMin" : "converterJclMin";
	}
	protected IPath getConverterJCLPath() {
		return getConverterJCLPath(CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	}

	protected IPath getConverterJCLPath(String compliance) {
		String jarName = jclMinName(compliance);
		return new Path(getExternalPath() + jarName + compliance + ".jar"); //$NON-NLS-1$
	}

	protected IPath getConverterJCLSourcePath() {
		return getConverterJCLSourcePath(CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	}

	protected IPath getConverterJCLSourcePath(String compliance) {
		String jarName = jclMinName(compliance);
		return new Path(getExternalPath() + jarName + compliance + "src.zip"); //$NON-NLS-1$
	}

	protected IPath getConverterJCLRootSourcePath() {
		return new Path(""); //$NON-NLS-1$
	}

	public void testImportClassInJavaBase_since_25() throws Exception {
		String contents = """
			package pack1;
			import module java.base;
			public class X{
				List<String> a;
			}
			""";
		createFolder("/" + PROJECT + "/src/pack1");
		createFile("/" + PROJECT + "/src/pack1/X.java", contents);

		ASTParser parser = ASTParser.newParser(getJLS25());

		ICompilationUnit cu = getCompilationUnit("/" + PROJECT + "/src/pack1/X.java");
		parser.setSource(contents.toCharArray());
		parser.setProject(this.project);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setUnitName("X.java");
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		Method setTypeRoot= CompilationUnit.class.getDeclaredMethod("setTypeRoot", ITypeRoot.class);
		setTypeRoot.setAccessible(true);
		setTypeRoot.invoke(astRoot, cu);
		TypeDeclaration type= (TypeDeclaration) astRoot.types().get(0);
		FieldDeclaration [] fields =  type.getFields();
		FieldDeclaration field = fields[0];
		List<VariableDeclarationFragment> fragments= field.fragments();
		VariableDeclarationFragment fragment= fragments.get(0);
		IVariableBinding varBinding= fragment.resolveBinding();
		ITypeBinding typeBinding= varBinding.getType();
		ImportRewrite rewrite = newImportsRewrite((ICompilationUnit) astRoot.getJavaElement(), new String[0], 99, 99, true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertEquals("List<String>", actualType.toString());
		assertTrue(actualType.isParameterizedType());
		apply(rewrite);
		assertEqualStringIgnoreDelim(cu.getSource(), contents);
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
