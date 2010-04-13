/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;

public class ImportRewriteTest extends AbstractJavaModelTests {

	private static final Class THIS= ImportRewriteTest.class;

	protected IPackageFragmentRoot sourceFolder;

	public ImportRewriteTest(String name) {
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

		IJavaProject proj= createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));

		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(1));


		this.sourceFolder = getPackageFragmentRoot("P", "src");

		waitUntilIndexesReady();
	}

	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}


	public void testAddImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.net.Socket");
		imports.addImport("p.A");
		imports.addImport("com.something.Foo");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.net.Socket;\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import com.something.Foo;\n");
		buf.append("\n");
		buf.append("import p.A;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsNoEmptyLines() throws Exception {

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(0));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.util", "java.new", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.net.Socket;\n");
		buf.append("import p.A;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsMoreEmptyLines() throws Exception {

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(2));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.util", "java.new", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("import java.net.Socket;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("import p.A;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.x.Socket");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.x.Socket;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}


	public void testAddImports3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	
	public void testAddImports4() throws Exception {
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("import java.util.Vector ;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports5() throws Exception {
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map");
		imports.addImport("java.util.Set");
		imports.addImport("java.util.Map.Entry");
		imports.addImport("java.util.Collections");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.* ;\n");
		buf.append("import java.util.Map.Entry ;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=306568
	public void testAddImports6() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.Entry");

		apply(imports);

		buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"import java.util.Map.Entry;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		assertEqualString(cu.getSource(), buf.toString());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=309022
	public void testAddImports7() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"import java.util.Map.Entry;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        PrintWriter pw;\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.PrintWriter");

		apply(imports);

		buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.io.*;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"import java.util.Map.Entry;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        PrintWriter pw;\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsWithGroupsOfUnmatched1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "", "org", "#", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("org.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("java.util.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("import my.M;\n");
		buf.append("\n");
		buf.append("import org.x.Y;\n");
		buf.append("\n");
		buf.append("import static stat.X.CONST;\n");
		buf.append("\n");
		buf.append("import pack.P;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsWithGroupsOfUnmatched2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "org", "com", "pack", "#", "" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("org.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import org.Vector;\n");
		buf.append("\n");
		buf.append("import com.x.Y;\n");
		buf.append("\n");
		buf.append("import pack.P;\n");
		buf.append("\n");
		buf.append("import static stat.X.CONST;\n");
		buf.append("\n");
		buf.append("import my.M;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Set");
		imports.removeImport("pack.List");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector; // comment\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Vector");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports3() throws Exception {
		IPackageFragment pack= this.sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public class Inner {\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack.createCompilationUnit("A.java", buf.toString(), false, null);
		
		IPackageFragment test1= this.sourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import pack.A;\n");
		buf.append("import pack.A.Inner;\n");
		buf.append("import pack.A.NotThere;\n");
		buf.append("import pack.B;\n");
		buf.append("import pack.B.Inner;\n");
		buf.append("import pack.B.NotThere;\n");
		buf.append("\n");
		buf.append("public class T {\n");
		buf.append("}\n");
		ICompilationUnit cuT= test1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cuT);
		parser.setResolveBindings(true);
		CompilationUnit astRoot= (CompilationUnit) parser.createAST(null);
		
		ImportRewrite imports= newImportsRewrite(astRoot, new String[0], 99, 99, true);
		imports.setUseContextToFilterImplicitImports(true);
		
		imports.removeImport("pack.A.Inner");
		imports.removeImport("pack.A.NotThere");
		imports.removeImport("pack.B.Inner");
		imports.removeImport("pack.B.NotThere");
		
		apply(imports);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import pack.A;\n");
		buf.append("import pack.B;\n");
		buf.append("\n");
		buf.append("public class T {\n");
		buf.append("}\n");
		assertEqualString(cuT.getSource(), buf.toString());
	}

	public void testAddImports_bug23078() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import p.A.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("p.Inner");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import p.Inner;\n");
		buf.append("import p.A.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug25113() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.awt.Panel;\n");
		buf.append("\n");
		buf.append("import java.math.BigInteger;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.awt", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.applet.Applet");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.awt.Panel;\n");
		buf.append("\n");
		buf.append("import java.applet.Applet;\n");
		buf.append("import java.math.BigInteger;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug42637() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.Exception;\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug121428() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.io.Exception;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Test that the Inner class import comes in the right order (i.e. after the enclosing type's import) when re-organized
	 * 
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358() throws Exception {

		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack2.A;\n");
		buf.append("import pack2.A.Inner;\n");
		buf.append("import pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		// We need to actually make some state in the AST for the classes, to test that we can 
		// disambiguate between packages and inner classes (see the bug for details).
		IPackageFragment pack2= this.sourceFolder.createPackageFragment("pack2", false, null);
		ICompilationUnit aUnit= pack2.createCompilationUnit("A.java", "", false, null);
		ICompilationUnit bUnit= pack2.createCompilationUnit("B.java", "", false, null);
		bUnit.createType("class B {}", null, false, null);

		IType aType= aUnit.createType("class A {}", null, false, null);
		aType.createType("class Inner {}", null, false, null);
		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("pack2.A");
		imports.addImport("pack2.B");
		imports.addImport("pack2.A.Inner");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack2.A;\n");
		buf.append("import pack2.A.Inner;\n");
		buf.append("import pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Test that a valid inner class import is not removed even when the container
	 * class is implicitly available. This tests the case where the classes are in 
	 * different compilation units.
	 * 
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358a() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package com.pack1;\n");
		buf.append("\n");
		buf.append("import com.pack1.A;\n");
		buf.append("import com.pack1.A.Inner;\n");
		buf.append("import com.pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("com.pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		ICompilationUnit aUnit= pack1.createCompilationUnit("A.java", "", false, null);

		IPackageFragment pack2= this.sourceFolder.createPackageFragment("com.pack2", false, null);
		ICompilationUnit bUnit= pack2.createCompilationUnit("B.java", "", false, null);
		bUnit.createType("class B {}", null, false, null);
		IType aType= aUnit.createType("class A {}", null, false, null);
		aType.createType("class Inner {}", null, false, null);
		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(false);
		imports.addImport("com.pack1.A");
		imports.addImport("com.pack1.A.Inner");
		imports.addImport("com.pack2.B");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package com.pack1;\n");
		buf.append("\n");
		buf.append("import com.pack1.A.Inner;\n");
		buf.append("import com.pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	/**
	 * Test that the Inner type imports are not removed while organizing even though the 
	 * containing class is implicitly available - for the case when both the classes are 
	 * in the same compilation unit
	 * 
	 * see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=235253"
	 */
	public void testBug235253() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package bug;\n");
		buf.append("\n");
		buf.append("class Bug {\n");
		buf.append("public void addFile(File file) {}\n");
		buf.append("\tinterface Proto{};\n");
		buf.append("}\n");		
		buf.append("class Foo implements Proto{}");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("bug", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("Bug.java", buf.toString(), false, null);
		String[] order= new String[] { "bug" , "java" };
		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("bug.Bug.Proto");
		imports.addImport("java.io.File"); 
		
		apply(imports);
		buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("\n");
		buf.append("import bug.Bug.Proto;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("\n");
		buf.append("class Bug {\n");
		buf.append("public void addFile(File file) {}\n");
		buf.append("\tinterface Proto{};\n");
		buf.append("}\n");		
		buf.append("class Foo implements Proto{}");
		assertEqualString(cu.getSource(), buf.toString());
	}
		
	public void testAddStaticImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.lang.Math.max;\n");
		buf.append("import static java.lang.Math.min;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("xx.MyConstants", "SIZE", true);
		imports.addStaticImport("xy.MyConstants", "*", true);
		imports.addImport("xy.MyConstants");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static xx.MyConstants.SIZE;\n");
		buf.append("import static xy.MyConstants.*;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("import xy.MyConstants;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 3, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addStaticImport("java.lang.Math", "max", true);
		imports.addStaticImport("java.lang.Math", "abs", true);

		imports.addStaticImport("java.io.File", "pathSeparator", true);
		imports.addStaticImport("java.io.File", "separator", true);

		imports.addImport("java.util.List");
		imports.addImport("java.util.Vector");
		imports.addImport("java.util.ArrayList");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.io.File.pathSeparator;\n");
		buf.append("import static java.io.File.separator;\n");
		buf.append("import static java.lang.Math.*;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}


	private void createClassStub(String pack, String typeName, String typeKind) throws JavaModelException {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment(pack, false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package ").append(pack).append(";\n");
		buf.append("public ").append(typeKind).append(" ").append(typeName).append(" {\n");
		buf.append("}\n");
		String content= buf.toString();

		String name= typeName;
		int idx= typeName.indexOf('<');
		if (idx != -1) {
			name= typeName.substring(0, idx);
		}
		pack1.createCompilationUnit(name + ".java", content, false, null);
	}


	public void testImportStructureWithSignatures() throws Exception {
		createClassStub("java.io", "IOException", "class");
		createClassStub("java.net", "URL", "class");
		createClassStub("java.util", "List<E>", "interface");
		createClassStub("java.net", "SocketAddress", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.net.*;\n");
		buf.append("import java.io.*;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        IOException s;\n");
		buf.append("        URL[][] t;\n");
		buf.append("        List<SocketAddress> x;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String content= buf.toString();
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("}\n");
		String content2= buf.toString();
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(AST.JLS3);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("IOException"), "IOException".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("URL"), "URL".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("List"), "List".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		apply(importsRewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("\n");
		buf.append("import java.net.SocketAddress;\n");
		buf.append("import java.net.URL;\n");
		buf.append("\n");
		buf.append("public class B {\n");
		buf.append("}\n");

		assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
	}

	public void testImportStructureWithSignatures2() throws Exception {
		createClassStub("java.util", "Map<S, T>", "interface");
		createClassStub("java.util", "Set<S>", "interface");
		createClassStub("java.net", "SocketAddress", "class");
		createClassStub("java.net", "ServerSocket", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.net.*;\n");
		buf.append("import java.io.*;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Map<?, ? extends Set<? super ServerSocket>> z;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String content= buf.toString();
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("}\n");
		String content2= buf.toString();
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(AST.JLS3);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("Map"), "Map".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}

		apply(importsRewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Map;\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("import java.net.ServerSocket;\n");
		buf.append("\n");
		buf.append("public class B {\n");
		buf.append("}\n");

		assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
	}


	public void testAddedRemovedImportsAPI() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public final static int CONST= 9;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");

		assertAddedAndRemoved(imports,
				new String[] { "java.lang.Math" }, new String[] {},
				new String[] { "java.lang.Math.min" }, new String[] {}
		);

		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		assertAddedAndRemoved(imports,
				new String[] { "java.lang.Math" }, new String[] {},
				new String[] { "java.lang.Math.min", "java.lang.Math.max" }, new String[] {}
		);

		imports.removeImport("java.lang.Math");
		imports.removeImport("java.util.Vector");
		imports.removeStaticImport("java.lang.Math.dup");

		assertAddedAndRemoved(imports,
				new String[] { }, new String[] { "java.util.Vector"},
				new String[] { "java.lang.Math.min", "java.lang.Math.max" }, new String[] {}
		);

		imports.addImport("java.util.Vector");
		imports.addStaticImport("pack1.C", "CONST", true);

		assertAddedAndRemoved(imports,
				new String[] { }, new String[] { },
				new String[] { "java.lang.Math.min", "java.lang.Math.max", "pack1.C.CONST" }, new String[] {}
		);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.lang.Math.max;\n");
		buf.append("import static java.lang.Math.min;\n");
		buf.append("import static pack1.C.CONST;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public final static int CONST= 9;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testPackageInfo() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("\npackage pack1;");

		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("foo.Bar");

		apply(imports);

		buf= new StringBuffer();
		buf.append("\npackage pack1;\n");
		buf.append("import foo.Bar;\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testBug252379() throws CoreException, BackingStoreException,
			MalformedTreeException, BadLocationException {
		

		ICompilationUnit[] units = new ICompilationUnit[3];
		
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(
				"bug", false, null);

		StringBuffer buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("\n");
		buf.append("enum CaseType {\n");
		buf.append("\tone;\n");
		buf.append("\tstatic CaseType[] all(){return null;}\n");
		buf.append("}\n");
		
		units[0] = pack1.createCompilationUnit("CaseType.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("enum ShareLevel{all})\n");
		
		units[1] = pack1.createCompilationUnit("ShareLevel.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("class Bug {\n");
		buf.append("public ShareLevel createControl() {\n");
		buf.append("for (CaseType cat : all())\n");
		buf.append("cat.hashCode();\n");
		buf.append("ShareLevel temp = all;\n");
		buf.append("return temp;\n");
		buf.append("};\n");
		buf.append("}\n");
		units[2] = pack1.createCompilationUnit("Bug.java", buf.toString(), false, null);

		ImportRewrite imports = newImportsRewrite(units[2], new String[] {}, 99, 99, false);
		imports.addStaticImport("bug.CaseType", "all", false);
		imports.addStaticImport("bug.ShareLevel", "all", true);

		apply(imports);

		buf = new StringBuffer();
		buf.append("package bug;\n\n");
		buf.append("import static bug.CaseType.all;\n");
		buf.append("import static bug.ShareLevel.all;\n\n");
		buf.append("class Bug {\n");
		buf.append("public ShareLevel createControl() {\n");
		buf.append("for (CaseType cat : all())\n");
		buf.append("cat.hashCode();\n");
		buf.append("ShareLevel temp = all;\n");
		buf.append("return temp;\n");
		buf.append("};\n");
		buf.append("}\n");
		assertEqualString(units[2].getSource(), buf.toString());
	}

	private void assertAddedAndRemoved(ImportRewrite imports, String[] expectedAdded, String[] expectedRemoved, String[] expectedAddedStatic, String[] expectedRemovedStatic) {
		assertEqualStringsIgnoreOrder(imports.getAddedImports(), expectedAdded);
		assertEqualStringsIgnoreOrder(imports.getAddedStaticImports(), expectedAddedStatic);
		assertEqualStringsIgnoreOrder(imports.getRemovedImports(), expectedRemoved);
		assertEqualStringsIgnoreOrder(imports.getRemovedStaticImports(), expectedRemovedStatic);
	}

	private void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}

	private void assertEqualStringsIgnoreOrder(String[] actual, String[] expecteds) {
		StringAsserts.assertEqualStringsIgnoreOrder(actual, expecteds);
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
