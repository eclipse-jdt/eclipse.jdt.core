/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *		Stephan Herrmann - Contribution for Bug 378024 - Ordering of comments between imports not preserved
 *		John Glassmyer <jogl@google.com> - import group sorting is broken - https://bugs.eclipse.org/430303
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ImportRewriteTest extends AbstractJavaModelTests {

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	private static final Class THIS= ImportRewriteTest.class;

	protected IPackageFragmentRoot sourceFolder;

	public ImportRewriteTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
//		System.err.println("Warning, only part of the ImportRewriteTest are being executed!");
//		Suite suite = new Suite(ImportRewriteTest.class.getName());
//		suite.addTest(new ImportRewriteTest("testRemoveImports1"));
//		return suite;
		return allTests();
	}

	@Override
	protected void setUp() throws Exception {
		this.indexDisabledForTest = false;
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

		// The tests in this class assume that the line separator is "\n".
		new ProjectScope(proj.getProject()).getNode(Platform.PI_RUNTIME).put(Platform.PREF_LINE_SEPARATOR, "\n");

		this.sourceFolder = getPackageFragmentRoot("P", "src");

		waitUntilIndexesReady();
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}

	/**
	 * Addresses https://bugs.eclipse.org/465566 ("Organize imports does not remove duplicated
	 * imports").
	 */
	public void testDuplicateImportOmittedWhenRestoreExistingImportsIsFalse() throws Exception {
		String str = """
			package pack1;
			
			import java.io.Serializable;
			import java.io.Serializable;
			
			public class Clazz {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 2, 2, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.Serializable");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.io.Serializable;
			
			public class Clazz {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that imports can be added for types from an unnamed (default) package, and that such
	 * imports are not reduced into an on-demand import. Imports of types from an unnamed package
	 * were legal in versions of Java prior to 1.4.
	 *
	 * Addresses https://bugs.eclipse.org/461863 ("addImport creates .ypename for unqualified type
	 * from default package").
	 */
	public void testAddImportsFromUnnamedPackage() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 2, 2, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.ArrayDeque");
		imports.addImport("java.util.ArrayList");
		imports.addImport("Bar");
		imports.addImport("Foo");

		apply(imports);

		String str = """
			package pack1;
			
			import java.util.*;
			
			import Bar;
			import Foo;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str);
	}

	/**
	 * Addresses https://bugs.eclipse.org/412929 ("Adding a type results in adding a package and
	 * later does not honor order").
	 */
	public void testImportGroupMatchingQualifiedName() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "#android.R.doFoo", "android.R", "java", "android" };

		ImportRewrite imports = newImportsRewrite(cu, order, 999, 999, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("android.R");
		imports.addImport("java.util.List");
		imports.addImport("android.Foo");
		imports.addStaticImport("android.R", "doFoo", false);

		apply(imports);

		String str = """
			package pack1;
			
			import static android.R.doFoo;
			
			import android.R;
			
			import java.util.List;
			
			import android.Foo;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str);
	}

	/**
	 * Expects that the comments from single imports are reassigned
	 * to a new on-demand import into which they are reduced.
	 */
	public void testReduceNewOnDemand() throws Exception {
		String str = """
			package pack1;
			
			import java.io.Serializable;
			
			// A floating leading
			
			// A leading
			/* A same-line leading */ import java.net.A; // A same-line trailing
			// A trailing
			
			// B floating leading
			
			// B leading
			/* B same-line leading */ import java.net.B; // B same-line trailing
			// B trailing
			
			// C floating leading
			
			// C leading
			/* C same-line leading */ import java.net.C; // C same-line trailing
			// C trailing
			
			import java.util.List;
			
			public class Clazz {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.Serializable");
		imports.addImport("java.net.A");
		imports.addImport("java.net.B");
		imports.addImport("java.net.C");
		imports.addImport("java.util.List");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.io.*;
			
			// A floating leading
			
			// A leading
			/* A same-line leading */
			// A same-line trailing
			// A trailing
			
			// B floating leading
			
			// B leading
			/* B same-line leading */
			// B same-line trailing
			// B trailing
			
			// C floating leading
			
			// C leading
			/* C same-line leading */
			// C same-line trailing
			// C trailing
			import java.net.*;
			import java.util.*;
			
			public class Clazz {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that the comments from single imports are reassigned
	 * to an existing on-demand import into which they are reduced,
	 * and that the on-demand import's own comments are preserved.
	 */
	public void testReduceExistingOnDemand() throws Exception {
		String str = """
			package pack1;
			
			import java.io.*;
			
			// on-demand floating
			
			// on-demand leading
			/* on-demand same-line leading */ import java.net.*; // on-demand same-line trailing
			// on-demand trailing
			
			// A floating leading
			
			// A leading
			/* A same-line leading */ import java.net.A; // A same-line trailing
			// A trailing
			
			// B floating leading
			
			// B leading
			/* B same-line leading */ import java.net.B; // B same-line trailing
			// B trailing
			
			// C floating leading
			
			// C leading
			/* C same-line leading */ import java.net.C; // C same-line trailing
			// C trailing
			
			import java.util.*;
			
			public class Clazz {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str);

		String[] order = new String[] { "java.io", "java", "java.util" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.Serializable");
		imports.addImport("java.net.A");
		imports.addImport("java.net.B");
		imports.addImport("java.net.C");
		imports.addImport("java.util.List");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.io.*;
			
			// A floating leading
			
			// A leading
			/* A same-line leading */
			// A same-line trailing
			// A trailing
			
			// B floating leading
			
			// B leading
			/* B same-line leading */
			// B same-line trailing
			// B trailing
			
			// C floating leading
			
			// C leading
			/* C same-line leading */
			// C same-line trailing
			// C trailing
			
			// on-demand floating
			
			// on-demand leading
			/* on-demand same-line leading */ import java.net.*; // on-demand same-line trailing
			// on-demand trailing
			
			import java.util.*;
			
			public class Clazz {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that comments from an expanded on-demand import are reassigned
	 * to a corresponding single import, and that comments of other single imports
	 * with the same container name are preserved.
	 */
	public void testExpandOnDemand() throws Exception {
		String str = """
			package pack1
			
			import com.example;
			
			/* on-demand floating */
			
			// on-demand leading
			/* on-demand same-line leading */ import java.util.*; // on-demand same-line trailing
			// on-demand trailing
			
			/* ArrayList floating */
			
			// ArrayList leading
			/* ArrayList same-line leading */ import java.util.ArrayList; // ArrayList same-line trailing
			// ArrayList trailing
			
			/* List floating */
			
			// List leading
			/* List same-line leading */ import java.util.List; // List same-line trailing
			// List trailing
			
			/* Map floating */
			
			// Map leading
			/* Map same-line leading */ import java.util.Map; // Map same-line trailing
			// Map trailing
			
			import java.net.Socket;
			
			public class C {}
			""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "com", "java.util", "java.net" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.util.ArrayList");
		importRewrite.addImport("java.util.Map");
		importRewrite.addImport("java.util.Set");
		importRewrite.addImport("java.net.Socket");

		apply(importRewrite);

		String str1 = """
			package pack1
			
			/* on-demand floating */
			
			// on-demand leading
			/* on-demand same-line leading */
			// on-demand same-line trailing
			// on-demand trailing
			
			/* ArrayList floating */
			
			// ArrayList leading
			/* ArrayList same-line leading */ import java.util.ArrayList; // ArrayList same-line trailing
			// ArrayList trailing
			
			/* Map floating */
			
			// Map leading
			/* Map same-line leading */ import java.util.Map; // Map same-line trailing
			// Map trailing
			import java.util.Set;
			
			import java.net.Socket;
			
			public class C {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that the comments of a removed import (other than an expanded on-demand import with
	 * a corresponding single import, or a reduced single import with a correponding on-demand
	 * import) are removed.
	 */
	public void testRemovedImportCommentsAreRemoved() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			/* Socket is a very useful class */
			import java.net.Socket; // Socket to 'em!
			/* Thank goodness Java has built-in networking libraries! */
			
			import java.util.ArrayList;
			
			public class C {}
			""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.ArrayList;
			
			public class C {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Addresses https://bugs.eclipse.org/318437 ("Organize Imports ignores Number of Imports needed
	 * for .*") and https://bugs.eclipse.org/359724 ("nested type imports not collapsed to wildcards
	 * ('*')").
	 */
	public void testOnDemandWithinType() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.Entry");

		apply(imports);

		String str = """
			package pack1;
			
			import java.util.Map.*;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str);
	}

	/**
	 * Expects that a comment embedded within an import declaration is preserved.
	 */
	public void testCommentWithinImportDeclaration() throws Exception {
		String str = """
			package pack1;
			
			import /* comment */ java.util.Map.*;
			
			public class C {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.*");

		apply(imports);

		String str1 = """
			package pack1;
			
			import /* comment */ java.util.Map.*;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Addresses https://bugs.eclipse.org/457051 ("comment is discarded when reducing imports to an
	 * on-demand import").
	 */
	public void testFloatingCommentPreservedWhenReducingOnDemandAbove() throws Exception {
		String str = """
			package pack1;
			
			import java.util.Queue;
			
			/* floating comment */
			
			import java.util.concurrent.BlockingDeque;
			
			public class C {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 2, 2, true);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.util.Formatter");

		apply(importRewrite);

		String str1 = """
			package pack1;
			
			import java.util.*;
			
			/* floating comment */
			
			import java.util.concurrent.BlockingDeque;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Addresses https://bugs.eclipse.org/457089 ("imports are improperly reordered in the presence
	 * of a floating comment").
	 */
	public void testFloatingCommentDoesntCauseImportsToMove() throws Exception {
		String str = """
			package pack1;
			
			import java.io.Serializable;
			
			/* floating comment */
			
			import java.util.List;
			
			import javax.sql.DataSource;
			
			public class C {}
			""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java", "javax" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.io.Serializable");
		importRewrite.addImport("java.util.List");
		importRewrite.addImport("javax.sql.DataSource");

		apply(importRewrite);

		String str1 = """
			package pack1;
			
			import java.io.Serializable;
			
			/* floating comment */
			
			import java.util.List;
			
			import javax.sql.DataSource;
			
			public class C {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImportIntoMatchAllImportGroup() throws Exception {
		String str = """
			package pack1;
			
			import java.util.ArrayList;
			
			public class C {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "", "java.net" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, true);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.net.Socket");

		apply(importRewrite);

		String str1 = """
			package pack1;
			
			import java.util.ArrayList;
			
			import java.net.Socket;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testCuInDefaultPackageWithNoExistingImports() throws Exception {
		String str = """
			public class C {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java", "java.net" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 999, 999, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addImport("java.net.Socket");
		importRewrite.addImport("java.util.ArrayList");

		apply(importRewrite);

		String str1 = """
			import java.util.ArrayList;
			
			import java.net.Socket;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Addresses https://bugs.eclipse.org/71761 ("ImportRewrite should let me add explicit import to
	 * existing on demand import").
	 */
	public void testNeedsExplicitImport() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java" };

		ImportRewriteContext needsExplicitImportContext = new ImportRewriteContext() {
			public int findInContext(String qualifier, String name, int kind) {
				return ImportRewriteContext.RES_NAME_UNKNOWN_NEEDS_EXPLICIT_IMPORT;
			}
		};

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);
		importRewrite.addStaticImport("java.util.Collections", "shuffle", false, needsExplicitImportContext);
		importRewrite.addStaticImport("java.util.Collections", "sort", false);
		importRewrite.addImport("java.util.List", needsExplicitImportContext);
		importRewrite.addImport("java.util.Map");

		apply(importRewrite);

		String str = """
			package pack1;
			
			import static java.util.Collections.*;
			import static java.util.Collections.shuffle;
			
			import java.util.*;
			import java.util.List;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str);
	}

	public void testOrganizeNoImportsWithOneLineDelim() throws Exception {
		String str = """
			package pack1;
			public class C {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);

		apply(importRewrite);

		String str1 = """
			package pack1;
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testOrganizeNoImportsWithTwoLineDelims() throws Exception {
		String str = """
			package pack1;
			
			public class C {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);

		apply(importRewrite);

		String str1 = """
			package pack1;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testOrganizeNoImportsWithJavadoc() throws Exception {
		String str = """
			package pack1;
			
			/**
			 * Best class ever.
			 */
			
			public class C {
			}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "C", str);

		String[] order = new String[] { "java" };

		ImportRewrite importRewrite = newImportsRewrite(cu, order, 1, 1, false);
		importRewrite.setUseContextToFilterImplicitImports(true);

		apply(importRewrite);

		String str1 = """
			package pack1;
			
			/**
			 * Best class ever.
			 */
			
			public class C {
			}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that imports are correctly placed after the end of a package declaration's multiline
	 * trailing comment.
	 */
	public void testPackageDeclarationTrailingComment() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1; /* pack1\s
			trailing\s
			comment */
			
			public class C {
			}
			""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");

		apply(imports);

		String str1 = """
			package pack1; /* pack1\s
			trailing\s
			comment */
			
			import java.util.ArrayList;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects correct placement of an import when package declaration, type declaration, and
	 * associated comments are all on one line.
	 */
	public void testAddImportWithPackageAndTypeOnSameLine() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1; /* pack1 trailing */  /** C leading */ public class C {}
			""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] { "java" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");

		apply(imports);

		String str1 = """
			package pack1; /* pack1 trailing */
			
			import java.util.ArrayList;
			
			/** C leading */ public class C {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that imports not matching defined import groups are placed together at the end.
	 *
	 * Addresses https://bugs.eclipse.org/430303 ("import group sorting is broken").
	 */
	public void testUnmatchedImports() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "java.net", "com.google" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.acme.BirdSeed");
		imports.addImport("com.acme.Dynamite");
		imports.addImport("com.google.Tgif");
		imports.addImport("java.net.Socket");
		imports.addImport("java.new.Bar");
		imports.addImport("org.linux.Kernel");

		apply(imports);

		String str = """
			package pack1;
			
			import java.net.Socket;
			
			import com.google.Tgif;
			
			import com.acme.BirdSeed;
			import com.acme.Dynamite;
			import java.new.Bar;
			import org.linux.Kernel;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str);
	}

	/**
	 * Expects that the order in which addImport is called does not affect the resulting order of
	 * import declarations.
	 *
	 * Addresses https://bugs.eclipse.org/430303 ("import group sorting is broken").
	 */
	public void testAddImportsInVaryingOrder() throws Exception {
		String[] order = new String[] { "h", "a" };

		List importsToAdd = new ArrayList();
		importsToAdd.add("a.ClassInA");
		importsToAdd.add("b.ClassInB");
		importsToAdd.add("c.ClassInC");
		importsToAdd.add("d.ClassInD");
		importsToAdd.add("e.ClassInE");
		importsToAdd.add("f.ClassInF");
		importsToAdd.add("g.ClassInG");
		importsToAdd.add("h.ClassInH");

		ICompilationUnit cu1 = createCompilationUnit("pack1", "C");
		ImportRewrite imports1 = newImportsRewrite(cu1, order, 99, 99, false);
		for (Iterator importsToAddIter = importsToAdd.iterator(); importsToAddIter.hasNext(); ) {
			imports1.addImport((String) importsToAddIter.next());
		}
		apply(imports1);
		String source1 = cu1.getSource();

		Collections.reverse(importsToAdd);

		ICompilationUnit cu2 = createCompilationUnit("pack1", "C");
		ImportRewrite imports2 = newImportsRewrite(cu2, order, 99, 99, false);
		for (Iterator importsToAddIter = importsToAdd.iterator(); importsToAddIter.hasNext(); ) {
			imports2.addImport((String) importsToAddIter.next());
		}
		apply(imports2);
		String source2 = cu2.getSource();

		// Reversing the order in which imports are added via addImport() should not affect the rewritten order.
		assertEqualString(source2, source1);
	}

	/**
	 * Expects that static imports not matching any defined import group end up above defined import
	 * groups and that non-static imports not matching any defined import group end up below defined
	 * import groups.
	 *
	 * Addresses https://bugs.eclipse.org/430303 ("import group sorting is broken").
	 */
	public void testStaticAndNonStaticUnmatchedImports() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		String[] order = new String[] { "#a", "h" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addStaticImport("a.ClassInA", "staticMethodInA", false);
		imports.addStaticImport("b.ClassInB", "staticMethodInB", false);
		imports.addImport("g.ClassInG");
		imports.addImport("h.ClassInH");

		apply(imports);

		String str = """
			package pack1;
			
			import static b.ClassInB.staticMethodInB;
			
			import static a.ClassInA.staticMethodInA;
			
			import h.ClassInH;
			
			import g.ClassInG;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str);
	}

	/**
	 * Expect that two duplicate on-demand imports and their comments survive a rewrite.
	 */
	public void testAddWithDuplicateOnDemandImports() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.lang.*;
			
			/* foo.bar.* 1 leading */
			/* foo.bar.* 1 same-line leading */ import foo.bar.*; // foo.bar.* 1 same-line trailing
			/* foo.bar.* 1 trailing */
			
			import pack1.*;
			
			/* foo.bar.* 2 leading */
			/* foo.bar.* 2 same-line leading */ import foo.bar.*; // foo.bar.* 2 same-line trailing
			/* foo.bar.* 2 trailing */
			
			public class C {}
			""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] { "java.lang", "foo", "pack1", "com" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.example.MyClass");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.lang.*;
			
			/* foo.bar.* 1 leading */
			/* foo.bar.* 1 same-line leading */ import foo.bar.*; // foo.bar.* 1 same-line trailing
			/* foo.bar.* 1 trailing */
			
			import pack1.*;
			
			import com.example.MyClass;
			
			/* foo.bar.* 2 leading */
			/* foo.bar.* 2 same-line leading */ import foo.bar.*; // foo.bar.* 2 same-line trailing
			/* foo.bar.* 2 trailing */
			
			public class C {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expect that two duplicate single imports and their comments survive a rewrite.
	 */
	public void testAddWithDuplicateSingleImports() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.lang.*;
			
			/* foo.Bar 1 leading */
			/* foo.Bar 1 same-line leading */ import foo.Bar; // foo.Bar 1 same-line trailing
			/* foo.Bar 1 trailing */
			
			import pack1.*;
			
			/* foo.Bar 2 leading */
			/* foo.Bar 2 same-line leading */ import foo.Bar; // foo.Bar 2 same-line trailing
			/* foo.Bar 2 trailing */
			
			public class C {}""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] { "java.lang", "foo", "pack1", "com" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.example.MyClass");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.lang.*;
			
			/* foo.Bar 1 leading */
			/* foo.Bar 1 same-line leading */ import foo.Bar; // foo.Bar 1 same-line trailing
			/* foo.Bar 1 trailing */
			
			import pack1.*;
			
			import com.example.MyClass;
			
			/* foo.Bar 2 leading */
			/* foo.Bar 2 same-line leading */ import foo.Bar; // foo.Bar 2 same-line trailing
			/* foo.Bar 2 trailing */
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testOtherDuplicateImportsNotDisturbed() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import pack1.SomeClass; // first import
			import java.util.ArrayList;
			
			import pack1.SomeClass; // second import
			import com.mycompany.Frobnigator;
			
			import pack1.SomeClass; // third import
			import org.eclipse.GreatIde;
			
			public class C {}""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] { "java", "pack1", "com", "org" };

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.mycompany.Foo");

		apply(imports);

		String str1 = """
			package pack1;
			
			import pack1.SomeClass; // first import
			import java.util.ArrayList;
			
			import pack1.SomeClass; // second import
			
			import com.mycompany.Foo;
			import com.mycompany.Frobnigator;
			
			import pack1.SomeClass; // third import
			import org.eclipse.GreatIde;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testDuplicateImportsDoNotCountTowardOnDemandThreshold() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import com.mycompany.Foo;
			import com.mycompany.Foo;
			
			public class C {}""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 3, 3, true);
		imports.addImport("com.mycompany.Bar");

		apply(imports);

		// Expect that the 3-import on-demand threshold has not been reached.
		String str1 = """
			package pack1;
			
			import com.mycompany.Bar;
			import com.mycompany.Foo;
			import com.mycompany.Foo;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that a conflict between identically named fields from two static on-demand imports
	 * is resolved with an explicit import of one of the fields.
	 *
	 * Addresses https://bugs.eclipse.org/360789 ("Organize imports changes static imports to .*
	 * even when that introduces compile errors").
	 */
	public void testOnDemandConflictBetweenStaticFields() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		// This test uses enum constants because the example in bug 360789 used enum constants,
		// but the behavior generalizes to static fields that are not enum constants.
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		String str = """
			package pack2;
			public enum Horizontal { LEFT, CENTER, RIGHT }
			""";
		pack2.createCompilationUnit("Horizontal.java", str, false, null);
		String str1 = """
			package pack2;
			public enum Vertical { TOP, CENTER, BOTTOM }
			""";
		pack2.createCompilationUnit("Vertical.java", str1, false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.addStaticImport("pack2.Horizontal", "CENTER", true);
		imports.addStaticImport("pack2.Vertical", "TOP", true);

		apply(imports);

		String str2 = """
			package pack1;
			
			import static pack2.Horizontal.CENTER;
			import static pack2.Vertical.*;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str2);
	}

	/**
	 * Expects that a conflict between a static on-demand import and a type on-demand import
	 * is resolved with an explicit import of one of the conflicting member types.
	 *
	 * Inspired by https://bugs.eclipse.org/360789
	 */
	public void testOnDemandConflictBetweenTypeAndNestedStaticType() throws Exception {
		ICompilationUnit cu = createCompilationUnit("pack1", "C");

		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		String str = """
			package pack2;
			public class ContainingType {
			    public static class TypeWithSameName {}
			    public static final int CONSTANT = 42;
			}
			""";
		pack2.createCompilationUnit("ContainingType.java", str, false, null);

		IPackageFragment pack3 = this.sourceFolder.createPackageFragment("pack3", false, null);
		String str1 = """
			package pack3;
			public class TypeWithSameName {}
			""";
		pack3.createCompilationUnit("TypeWithSameName.java", str1, false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 1, 1, false);
		imports.addStaticImport("pack2.ContainingType", "CONSTANT", true);
		imports.addImport("pack3.TypeWithSameName");

		apply(imports);

		String str2 = """
			package pack1;
			
			import static pack2.ContainingType.*;
			
			import pack3.TypeWithSameName;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str2);
	}

	public void testFloatingCommentWithBlankLine() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import com.mycompany.Bar;
			
			/*hello!
			
			this is a comment!*/
			
			import com.mycompany.Foo;
			
			public class C {}""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] {};

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.mycompany.Bar");
		imports.addImport("com.mycompany.Foo");

		apply(imports);

		String str1 = """
			package pack1;
			
			import com.mycompany.Bar;
			
			/*hello!
			
			this is a comment!*/
			
			import com.mycompany.Foo;
			
			public class C {}""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that an import rewrite with no effective changes produces an empty TextEdit.
	 */
	public void testNoEdits() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			// leading comment
			import com.mycompany.Foo;
			// trailing comment
			
			// leading comment
			import java.util.ArrayList;
			// trailing comment
			
			public class C {}""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] {"com", "java"};

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.mycompany.Foo");
		imports.addImport("java.util.ArrayList");

		TextEdit edit = imports.rewriteImports(null);

		assertEquals(0, ((MultiTextEdit) edit).getChildrenSize());
	}

	public void testAddImportWithCommentBetweenImportsAndType() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import com.mycompany.Bar;
			
			/* floating comment */
			
			// type comment
			public class C {}""";
		ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

		String[] order = new String[] {"com", "java"};

		ImportRewrite imports = newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("com.mycompany.Bar");
		imports.addImport("com.mycompany.Foo");

		apply(imports);

		String str1 = """
			package pack1;
			
			import com.mycompany.Bar;
			import com.mycompany.Foo;
			
			/* floating comment */
			
			// type comment
			public class C {}""";
		assertEqualString(str1, cu.getSource());
	}

	public void testRenameImportedClassWithImportedNestedClass() throws Exception {
		String str = """
			package pack1;
			
			import com.example.A;
			import com.example.A.ANested;
			import com.example.C;
			import com.example.C.CNested;
			import com.example.E;
			import com.example.E.ENested;
			
			public class Clazz {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str);

		String[] order = new String[] { "com" };

		ImportRewrite imports = newImportsRewrite(cu, order, 999, 999, true);
		imports.setUseContextToFilterImplicitImports(true);
		// Simulate renaming com.example.A to com.example.D.
		imports.removeImport("com.example.A");
		imports.removeImport("com.example.A.ANested");
		imports.addImport("com.example.D");
		imports.addImport("com.example.D.ANested");

		apply(imports);

		String str1 = """
			package pack1;
			
			import com.example.C;
			import com.example.C.CNested;
			import com.example.D;
			import com.example.D.ANested;
			import com.example.E;
			import com.example.E.ENested;
			
			public class Clazz {}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testConflictsBetweenOriginalOnDemands() throws Exception {
		// Create a type named "A" in each of two packages.
		createCompilationUnit("conflicting1", "A");
		createCompilationUnit("conflicting2", "A");

		// Create a static member named "doStuff" in each of two types.
		String str = """
			package statics;
			
			public class Statics1 {
			    public static void doStuff() {}
			}
			""";
		createCompilationUnit("statics", "Statics1", str);
		String str1 = """
			package statics;
			
			public class Statics2 {
			    public static void doStuff() {}
			}
			""";
		createCompilationUnit("statics", "Statics2", str1);

		// Import the types and static members ambiguously via conflicting on-demand imports.
		String str2 = """
			package pack1;
			
			import static statics.Statics1.*;
			import static statics.Statics2.*;
			
			import conflicting1.*;
			import conflicting2.*;
			
			class Clazz {}""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str2);

		ImportRewrite imports = newImportsRewrite(cu, new String[0], 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		// Add imports that surface the ambiguity between the existing on-demand imports.
		imports.addImport("conflicting1.A");
		imports.addStaticImport("statics.Statics1", "doStuff", false);

		apply(imports);

		String str3 = """
			package pack1;
			
			import static statics.Statics1.*;
			import static statics.Statics1.doStuff;
			import static statics.Statics2.*;
			
			import conflicting1.*;
			import conflicting1.A;
			import conflicting2.*;
			
			class Clazz {}""";
		assertEqualString(cu.getSource(), str3);
	}

	public void testRemoveImportsWithPackageDocComment() throws Exception {
		String str = """
			/** package doc comment */
			package pack1;
			
			import com.example.Foo;
			
			public class Clazz {}
			""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str);

		ImportRewrite rewrite = newImportsRewrite(cu, new String[] {}, 999, 999, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		rewrite.removeImport("com.example.Foo");
		apply(rewrite);

		String str1 = """
			/** package doc comment */
			package pack1;
			
			public class Clazz {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testImplicitImportFiltering() throws Exception {
		String[] order = new String[] {};

		ICompilationUnit cuWithFiltering = createCompilationUnit("pack1", "CuWithFiltering");

		ImportRewrite rewriteWithFiltering = newImportsRewrite(cuWithFiltering, order, 999, 999, true);
		rewriteWithFiltering.setUseContextToFilterImplicitImports(true);
		rewriteWithFiltering.setFilterImplicitImports(true);
		rewriteWithFiltering.addImport("java.lang.Integer");
		apply(rewriteWithFiltering);

		String str = """
			package pack1;
			
			public class CuWithFiltering {}""";
		assertEqualString(cuWithFiltering.getSource(), str);

		ICompilationUnit cuWithoutFiltering = createCompilationUnit("pack1", "CuWithoutFiltering");

		ImportRewrite rewriteWithoutFiltering = newImportsRewrite(cuWithoutFiltering, order, 999, 999, true);
		rewriteWithoutFiltering.setUseContextToFilterImplicitImports(true);
		rewriteWithoutFiltering.setFilterImplicitImports(false);
		rewriteWithoutFiltering.addImport("java.lang.Integer");
		apply(rewriteWithoutFiltering);

		String str1 = """
			package pack1;
			
			import java.lang.Integer;
			
			public class CuWithoutFiltering {}""";
		assertEqualString(cuWithoutFiltering.getSource(), str1);
	}

	/**
	 * Addresses https://bugs.eclipse.org/460484 ("ImportRewrite throws SIOOBE when trying to add
	 * import").
	 */
	public void testAddAdjacentImportWithCommonPrefixButLongerInitialSegment() throws Exception {
		String str = """
			package pack1;
			
			import a.FromA;
			import b.FromB;
			
			public class Clazz {}
			""";
		ICompilationUnit cu = createCompilationUnit("pack1", "Clazz", str);

		ImportRewrite rewrite = newImportsRewrite(cu, new String[] {}, 999, 999, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		// Expect that no exception is thrown when "ab" is compared with "a".
		rewrite.addImport("ab.FromAb");
		apply(rewrite);

		String str1 = """
			package pack1;
			
			import a.FromA;
			import ab.FromAb;
			import b.FromB;
			
			public class Clazz {}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	// https://bugs.eclipse.org/459320
	public void testAddImportToCuNotOnClasspath() throws Exception {
		String str = """
			package pack1;
			
			public class Clazz {}
			""";
		createFolder("/P/alt-src/pack1/");
		IFile clazz = createFile("/P/alt-src/pack1/Clazz.java", str);
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(clazz);
		cu.becomeWorkingCopy(null);

		try {
			ImportRewrite rewrite = newImportsRewrite(cu, new String[] {}, 999, 999, true);
			rewrite.setUseContextToFilterImplicitImports(true);
			rewrite.addImport("pack1.AnotherClass");
			apply(rewrite);

			assertEqualString(cu.getSource(), str);
		} finally {
			cu.discardWorkingCopy();
		}
	}

	public void testAddImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Map;
			import java.util.Set;
			import java.util.Vector;
			
			import pack.List;
			import pack.List2;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.net.Socket");
		imports.addImport("p.A");
		imports.addImport("com.something.Foo");

		apply(imports);

		// java.net.Socket gets added to the "java" import group
		// p.A gets added to the default match-all group at the end
		// com.something.Foo gets added to the "com" import group
		String str1 = """
			package pack1;
			
			import java.net.Socket;
			import java.util.Map;
			import java.util.Set;
			import java.util.Vector;
			
			import com.something.Foo;
			
			import pack.List;
			import pack.List2;
			
			import p.A;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImportsNoEmptyLines() throws Exception {

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(0));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java.util", "java.net", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.Set;
			import java.net.Socket;
			import p.A;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImportsMoreEmptyLines() throws Exception {

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(2));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java.util", "java.net", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.Set;
			
			
			import java.net.Socket;
			
			
			import p.A;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set;
			import java.util.Vector;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.x.Socket");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.x.Socket;
			
			import java.util.Set;
			import java.util.Vector;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}


	public void testAddImports3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set; // comment
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.Set; // comment
			import java.util.Vector;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports4() throws Exception {
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set; // comment
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.Set; // comment
			import java.util.Vector ;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports5() throws Exception {
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map");
		imports.addImport("java.util.Set");
		imports.addImport("java.util.Map.Entry");
		imports.addImport("java.util.Collections");

		apply(imports);

		// java.util.{Map,Set,Collections} are reduced to java.util.*
		// java.util.Map.Entry is reduced to java.util.Map.*
		String str1 = """
			package pack1;
			
			import java.util.* ;
			import java.util.Map.* ;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=306568
	public void testAddImports6() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        System.out.println("hello");
			    }
			}""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.Entry");

		apply(imports);

		// With on-demand threshold set to 1, java.util.Map.Entry is reduced to java.util.Map.*.
		String str1 = """
			package pack1;
			
			import java.util.*;
			import java.util.Map.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        System.out.println("hello");
			    }
			}""";
		assertEqualString(cu.getSource(), str1);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=309022
	public void testAddImports7() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.*;
			import java.util.Map.Entry;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.PrintWriter");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.io.*;
			
			import java.util.*;
			import java.util.Map.Entry;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImportsWithGroupsOfUnmatched1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "", "org", "#", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("org.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("java.util.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.Vector;
			
			import my.M;
			
			import org.x.Y;
			
			import static stat.X.CONST;
			
			import pack.P;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImportsWithGroupsOfUnmatched2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "org", "com", "pack", "#", "" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("org.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		String str1 = """
			package pack1;
			
			import org.Vector;
			
			import com.x.Y;
			
			import pack.P;
			
			import static stat.X.CONST;
			
			import my.M;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testRemoveImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set;
			import java.util.Vector;
			import java.util.Map;
			
			import pack.List;
			import pack.List2;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Set");
		imports.removeImport("pack.List");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.*;
			
			import pack.List2;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testRemoveImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Set;
			import java.util.Vector; // comment
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Vector");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.util.Set;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testRemoveImports3() throws Exception {
		IPackageFragment pack= this.sourceFolder.createPackageFragment("pack", false, null);
		String str = """
			package pack;
			
			public class A {
			    public class Inner {
			    }
			}
			""";
		pack.createCompilationUnit("A.java", str, false, null);

		IPackageFragment test1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str1 = """
			package test1;
			
			import pack.A;
			import pack.A.Inner;
			import pack.A.NotThere;
			import pack.B;
			import pack.B.Inner;
			import pack.B.NotThere;
			
			public class T {
			}
			""";
		ICompilationUnit cuT= test1.createCompilationUnit("T.java", str1, false, null);

		ASTParser parser= ASTParser.newParser(JLS3_INTERNAL);
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

		String str2 = """
			package test1;
			
			import pack.A;
			import pack.B;
			
			public class T {
			}
			""";
		assertEqualString(cuT.getSource(), str2);
	}

	public void testRemoveImportWithSyntaxError_bug494691() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.*;
			
			syntaxError
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		ImportRewrite imports= newImportsRewrite(cu, new String[0], 2, 2, true);
		imports.removeImport("java.util.*");

		apply(imports);

		String str1 = """
			package pack1;
			
			syntaxError
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports_bug23078() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import p.A.*;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { };

		ImportRewrite imports= newImportsRewrite(cu, order, 3, 3, true);
		imports.addImport("p.A");
		imports.addImport("p.Inner");
		imports.addImport("p.Inner.*");

		apply(imports);

		// Without having set useContextToFilterImplicitImports to true, we get pre-3.6 behavior,
		// which sorts imports by containing type and/or package before sorting by qualified name.
		String str1 = """
			package pack1;
			
			import p.A;
			import p.Inner;
			import p.A.*;
			import p.Inner.*;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports_bug23078_usingContext() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import p.A.*;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { };

		ImportRewrite imports= newImportsRewrite(cu, order, 3, 3, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("p.A");
		imports.addImport("p.Inner");
		imports.addImport("p.Inner.*");

		apply(imports);

		// Having set useContextToFilterImplicitImports to true, we get 3.6-and-later behavior,
		// which sorts imports by containing package and then by qualified name.
		String str1 = """
			package pack1;
			
			import p.A;
			import p.A.*;
			import p.Inner;
			import p.Inner.*;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports_bug25113() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.awt.Panel;
			
			import java.math.BigInteger;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java.awt", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.beans.Beans");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.awt.Panel;
			
			import java.beans.Beans;
			import java.math.BigInteger;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddImports_bug42637() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.lang.System;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.io.Exception");

		apply(imports);

		String str1 = """
			package pack1;
			
			import java.io.Exception;
			import java.lang.System;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Expects that, in the absence of a package declaration, comments preceding the first import
	 * declaration are treated as file header comments and left in place.
	 */
	public void testAddImports_bug121428() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			/** comment */
			import java.lang.System;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		String str1 = """
			/** comment */
			import java.io.Exception;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Test that the Inner class import comes in the right order (i.e. after the enclosing type's import) when re-organized
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358() throws Exception {

		String str = """
			package pack1;
			
			import pack2.A;
			import pack2.A.Inner;
			import pack2.B;
			
			public class C {
			}
			""";
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

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

		String str1 = """
			package pack1;
			
			import pack2.A;
			import pack2.A.Inner;
			import pack2.B;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	/**
	 * Test that a valid inner class import is not removed even when the container
	 * class is implicitly available. This tests the case where the classes are in
	 * different compilation units.
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358a() throws Exception {
		String str = """
			package com.pack1;
			
			import com.pack1.A;
			import com.pack1.A.Inner;
			import com.pack2.B;
			
			public class C {
			}
			""";
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("com.pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);
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

		String str1 = """
			package com.pack1;
			
			import com.pack1.A.Inner;
			import com.pack2.B;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}
	/**
	 * Test that the Inner type imports are not removed while organizing even though the
	 * containing class is implicitly available - for the case when both the classes are
	 * in the same compilation unit
	 *
	 * see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=235253"
	 */
	public void testBug235253() throws Exception {
		String str = """
			package bug;
			
			class Bug {
			public void addFile(File file) {}
				interface Proto{};
			}
			class Foo implements Proto{}""";
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("bug", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("Bug.java", str, false, null);
		String[] order= new String[] { "bug" , "java" };
		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("bug.Bug.Proto");
		imports.addImport("java.io.File");

		apply(imports);
		String str1 = """
			package bug;
			
			import bug.Bug.Proto;
			
			import java.io.File;
			
			class Bug {
			public void addFile(File file) {}
				interface Proto{};
			}
			class Foo implements Proto{}""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddStaticImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.lang.System;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		apply(imports);

		String str1 = """
			package pack1;
			
			import static java.lang.Math.max;
			import static java.lang.Math.min;
			
			import java.lang.System;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddStaticImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.lang.System;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("xx.MyConstants", "SIZE", true);
		imports.addStaticImport("xy.MyConstants", "*", true);
		imports.addImport("xy.MyConstants");

		apply(imports);

		String str1 = """
			package pack1;
			
			import static xx.MyConstants.SIZE;
			import static xy.MyConstants.*;
			
			import java.lang.System;
			
			import xy.MyConstants;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testAddStaticImports3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.lang.System;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

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

		String str1 = """
			package pack1;
			
			import static java.io.File.pathSeparator;
			import static java.io.File.separator;
			import static java.lang.Math.*;
			
			import java.lang.System;
			import java.util.ArrayList;
			import java.util.List;
			import java.util.Vector;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}


	private void createClassStub(String pack, String typeName, String typeKind) throws JavaModelException {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment(pack, false, null);
		StringBuilder buf= new StringBuilder();
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
		String content= """
			package test1;
			import java.util.*;
			import java.net.*;
			import java.io.*;
			public class A {
			    public void foo() {
			        IOException s;
			        URL[][] t;
			        List<SocketAddress> x;
			    }
			}
			""";
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		String content2= """
			package test1;
			public class B {
			}
			""";
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(JLS3_INTERNAL, false);
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

		String str = """
			package test1;
			
			import java.util.List;
			
			import java.io.IOException;
			
			import java.net.SocketAddress;
			import java.net.URL;
			
			public class B {
			}
			""";
		assertEqualStringIgnoreDelim(cu2.getSource(), str);
	}

	public void testImportStructureWithSignatures2() throws Exception {
		createClassStub("java.util", "Map<S, T>", "interface");
		createClassStub("java.util", "Set<S>", "interface");
		createClassStub("java.net", "SocketAddress", "class");
		createClassStub("java.net", "ServerSocket", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String content= """
			package test1;
			import java.util.*;
			import java.net.*;
			import java.io.*;
			public class A {
			    public void foo() {
			        Map<?, ? extends Set<? super ServerSocket>> z;
			    }
			}
			""";
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		String content2= """
			package test1;
			public class B {
			}
			""";
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(JLS3_INTERNAL, false);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("Map"), "Map".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}

		apply(importsRewrite);

		String str = """
			package test1;
			
			import java.util.Map;
			import java.util.Set;
			
			import java.net.ServerSocket;
			
			public class B {
			}
			""";
		assertEqualStringIgnoreDelim(cu2.getSource(), str);
	}


	public void testAddedRemovedImportsAPI() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			import java.util.Vector;
			
			public class C {
			    public final static int CONST= 9;
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

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

		String str1 = """
			package pack1;
			
			import static java.lang.Math.max;
			import static java.lang.Math.min;
			import static pack1.C.CONST;
			
			import java.util.Vector;
			
			public class C {
			    public final static int CONST= 9;
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testPackageInfo() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			
			package pack1;""";
		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", str, false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("foo.Bar");

		apply(imports);

		String str1 = """
			
			package pack1;
			
			import foo.Bar;
			""";
		assertEqualString(cu.getSource(), str1);
	}

	public void testBug252379() throws CoreException, BackingStoreException,
			MalformedTreeException, BadLocationException {


		ICompilationUnit[] units = new ICompilationUnit[3];

		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(
				"bug", false, null);

		String str = """
			package bug;
			
			enum CaseType {
				one;
				static CaseType[] all(){return null;}
			}
			""";
		units[0] = pack1.createCompilationUnit("CaseType.java", str, false, null);

		String str1 = """
			package bug;
			enum ShareLevel{all})
			""";
		units[1] = pack1.createCompilationUnit("ShareLevel.java", str1, false, null);

		String str2 = """
			package bug;
			class Bug {
			public ShareLevel createControl() {
			for (CaseType cat : all())
			cat.hashCode();
			ShareLevel temp = all;
			return temp;
			};
			}
			""";
		units[2] = pack1.createCompilationUnit("Bug.java", str2, false, null);

		ImportRewrite imports = newImportsRewrite(units[2], new String[] {}, 99, 99, false);
		imports.addStaticImport("bug.CaseType", "all", false);
		imports.addStaticImport("bug.ShareLevel", "all", true);

		apply(imports);

		String str3 = """
			package bug;
			
			import static bug.CaseType.all;
			import static bug.ShareLevel.all;
			
			class Bug {
			public ShareLevel createControl() {
			for (CaseType cat : all())
			cat.hashCode();
			ShareLevel temp = all;
			return temp;
			};
			}
			""";
		assertEqualString(units[2].getSource(), str3);
	}

	/**
	 * Expects that comments in a variety of positions around and between import declarations
	 * are preserved when restoreExistingImports is set to false.
	 */
	public void testAddImports_bug24804() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		String str = """
			package pack1;
			
			/** floating comment before first import */
			
			import java.util.ArrayList; // trailing same-line comment
			
			/** floating comment between imports*/
			
			/** preceding-line comment */
			import java.util.Collection;
			/** comment on line between imports */
			import java.util.Deque;
			
			public class C {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.util.ArrayList");
		imports.addImport("java.util.Collection");
		imports.addImport("java.util.Deque");

		apply(imports);

		String str1 = """
			package pack1;
			
			/** floating comment before first import */
			
			import java.util.ArrayList; // trailing same-line comment
			
			/** floating comment between imports*/
			
			/** preceding-line comment */
			import java.util.Collection;
			/** comment on line between imports */
			import java.util.Deque;
			
			public class C {
			}
			""";
		assertEqualString(cu.getSource(), str1);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			import java.util.*; // test
			import java.util.Map.Entry;
			//comment 2
			import java.util.Map.SomethingElse;
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.io.*;
			
			import java.util.*; // test
			import java.util.Map.Entry;
			//comment 2
			import java.util.Map.SomethingElse;
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_2() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			import java.util.*; // test
			import java.util.Map.Entry; // test2
			import java.util.Map.SomethingElse;
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.io.*;
			
			// comment 1
			import java.util.*; // test
			import java.util.Map.Entry; // test2
			import java.util.Map.SomethingElse;
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_3() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.util.Map.Entry; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.io.*;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.util.Map.Entry; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_3a() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.util.Map.Entry; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.io.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_4() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.util.Map.*; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.io.*;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.util.Map.*; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_4a() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.HashMap; // test1
			/* lead 2*/import java.util.Map.*; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.io.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_5() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 3*/
			// test3
			// commen 3
			import java.util.Map.*;
			
			/* lead 2*/import java.io.PrintWriter.*; // test2
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_5a() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			/* lead 1*/ import java.util.*; // test1
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        // java.util.Map.* takes the place of java.util.Map.SomethingElse,
        // and the latter's comments are reassigned to it.
        String str1 = """
			package pack1;
			
			// comment 1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			/* lead 1*/ import java.util.*; // test1
			/* lead 3*/
			// test3
			// commen 3
			import java.util.Map.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // added import should get folded into existing *, without touching comments
    public void testBug376930_5b() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			/* lead 1*/ import java.util.*; // test1
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map");

        apply(imports);

        String str1 = """
			package pack1;
			
			/* lead 1*/ import java.util.*; // test1
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_5c() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 1*/ import java.util.*; // test1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			/* lead 3*/ import java.util.Map.SomethingElse; // test3
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        // java.util.Map.* takes the place of java.util.Map.SomethingElse,
        // and the latter's comments are reassigned to it.
        String str1 = """
			package pack1;
			
			/* lead 3*/
			// test3
			// commen 3
			import java.util.Map.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // added import should get folded along with existing import into *, without deleting comments
    public void testBug376930_5d() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			/* lead 1*/ import java.util.Map; // test1
			// commen 3
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/* lead 2*/import java.io.PrintWriter.*; // test2
			
			/* lead 1*/
			// test1
			// commen 3
			import java.util.*;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			        PrintWriter pw;
			        System.out.println("hello");
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // separating comment should not prevent folding into *-import
    public void testBug376930_5e() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			import java.util.Map;
			/* comment leading Map.Entry */
			import java.util.Map.Entry;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "javax", "org", "com" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			import java.util.*;
			/* comment leading Map.Entry */
			import java.util.Map.Entry;
			
			public class C {
			    public static void main(String[] args) {
			        HashMap h;
			
			        Map.Entry e= null;
			        Entry e2= null;
			
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    public void testBug378024() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * keep me with List
			 *
			 */
			import java.awt.List;// test1
			/*
			 * keep me with Serializable
			 */
			import java.io.Serializable;// test2
			/*
			 * keep me with HashMap
			 */
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.awt", "java.io", "java.util" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * keep me with List
			 *
			 */
			import java.awt.List;// test1
			
			/*
			 * keep me with Serializable
			 */
			import java.io.Serializable;// test2
			
			/*
			 * keep me with HashMap
			 */
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    public void testBug378024b() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			import java.awt.List;// test1
			/*
			 * don't move me 2
			 */
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			// test1
			import java.awt.*;
			/*
			 * don't move me 2
			 */
			// test2
			import java.io.*;
			
			/*
			 * don't move me 3
			 */
			// test3
			// commen 3
			import java.util.*;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports.
    // comments in between stay where they are
    public void testBug378024c() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports.
    // comments in between stay where they are
    public void testBug378024c_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports, even if they get folded.
    // comments in between stay where they are
    public void testBug378024c_2() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			// test1
			import java.awt.*;
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			// test2
			/*
			 * don't move me 3
			 */
			import java.io.*;
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			// test3
			// commen 3
			import java.util.*;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // not adding an import should preserve its comments and put them at the end.
    public void testBug378024d() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			// test1
			import java.awt.*;
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			// test3
			// commen 3
			import java.util.*;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // adding a new import should not disturb comments and import should be added in its group
    public void testBug378024e() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			// test2
			/*
			 * don't move me 3
			 */
			import java.io.*;
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // removing an import should preserve its comments at the end, and adding a new import should not disturb
    // existing comments
    public void testBug378024e_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			import java.io.PrintWriter;
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // folding imports because of a newly added import should preserve comments
    public void testBug378024f() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			// test2
			/*
			 * don't move me 3
			 */
			import java.io.*;
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // folding imports because of a newly added import should preserve comments
    public void testBug378024f_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * keep me with List
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * keep me with Serializable
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * keep me with Serializable 2
			 */
			
			// lead 3
			import java.io.PrintWriter;// test3
			/*
			 * keep me with PrintWriter
			 */
			
			/*
			 * don't move me
			 */
			
			//lead 4
			import java.util.HashMap;// test4
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * keep me with List
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * keep me with Serializable
			 */
			
			// lead 2
			// test2
			/*
			 * keep me with Serializable 2
			 */
			// lead 3
			// test3
			/*
			 * keep me with PrintWriter
			 */
			import java.io.*;
			
			/*
			 * don't move me
			 */
			
			//lead 4
			import java.util.HashMap;// test4
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Re-ordering imports and converting them to *
    public void testBug378024g() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.awt", "java.util", "java.io", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			// test1
			import java.awt.*;
			
			//lead 3
			// test3
			// commen 3
			import java.util.*;
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			// test2
			/*
			 * don't move me 3
			 */
			/*
			 * don't move me 4
			 */
			import java.io.*;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        List l = new List();
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are removed in case the restoring of imports is enabled
    // This will test changes in org.eclipse.jdt.internal.core.dom.rewrite.ImportRewriteAnalyzer.removeImport(String, boolean)
    public void testBug378024h() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			
			// lead 1
			import java.awt.List;// test1
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.removeImport("java.awt.List");

        apply(imports);

        String str1 = """
			package pack1;
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are removed in case the restoring of imports is enabled
    public void testBug378024h_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			// lead 1
			import java.awt.List;// test1
			/* i am with List */
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.removeImport("java.awt.List");
        imports.addImport("java.util.List");

        apply(imports);

        String str1 = """
			package pack1;
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			// commen 3
			import java.util.List;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are unfolded.
    public void testBug378024i() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			// lead 1
			import java.awt.*;// test1
			/* i am with List */
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			import java.io.*;// test2
			/*
			 * don't move me 3
			 */
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			import java.util.*;// test3
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        HashMap e= null;
			        PrintWriter p= null;
			        List l= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");
        imports.addImport("java.util.Map");
        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			// lead 1
			// test1
			/* i am with List */
			import java.awt.List;
			
			/*
			 * don't move me 2
			 */
			
			// lead 2
			// test2
			/*
			 * don't move me 3
			 */
			import java.io.PrintWriter;
			import java.io.Serializable;
			
			/*
			 * don't move me 4
			 */
			
			//lead 3
			// test3
			// commen 3
			import java.util.HashMap;
			import java.util.Map;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        HashMap e= null;
			        PrintWriter p= null;
			        List l= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are folded but a member type import is present
    public void testBug378024j() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        String str = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			// lead 1
			import java.awt.List;// test1
			/* i am with List */
			
			/*
			 * don't move me 2
			 */
			
			//lead 3
			import java.util.HashMap;// test3
			/*
			 * don't move me 3
			 */
			
			/*keep me with Map.Entry*/
			import java.util.Map.Entry;// member type import
			/*keep me with Map.Entry 2*/
			
			/*
			 * don't move me 4
			 */
			
			// lead 2
			import java.io.Serializable;// test2
			// commen 3
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        Map e= null;
			    }
			}""";
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", str, false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.util.HashMap");
        imports.addImport("java.util.Map.Entry");
        imports.addImport("java.io.Serializable");

        apply(imports);

        String str1 = """
			package pack1;
			
			// comment 1
			/*
			 * don't move me 1
			 *
			 */
			// lead 1
			// test1
			/* i am with List */
			import java.awt.*;
			
			/*
			 * don't move me 4
			 */
			
			// lead 2
			// test2
			// commen 3
			import java.io.*;
			
			/*
			 * don't move me 2
			 */
			
			//lead 3
			// test3
			/*
			 * don't move me 3
			 */
			import java.util.*;
			/*keep me with Map.Entry*/
			// member type import
			/*keep me with Map.Entry 2*/
			import java.util.Map.*;
			
			public class C implements Serializable{
			    public static void main(String[] args) {
			        Map e= null;
			    }
			}""";
        assertEqualString(cu.getSource(), str1);
    }

	public void testBug430108_001() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String contents = """
			package pack1;
			public class X {
			}
			""";
		ICompilationUnit cu = pack1.createCompilationUnit("X.java", contents, false, null);

		ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		contents = """
			package pack2;
			public class X {
			}
			""";
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		cu = pack2.createCompilationUnit("X.java", contents, false, null);
		ImportRewrite rewrite = newImportsRewrite(cu, new String[0], 99, 99, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		Type actualType = rewrite.addImport(typeBinding, astRoot.getAST());
		assertEquals("pack1.X", actualType.toString());
	}

	public void testBug430108_002() throws Exception {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
		String contents = """
			package pack1;
			public class X {
			}
			""";
		ICompilationUnit cu = pack1.createCompilationUnit("X.java", contents, false, null);

		ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types().get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		contents = """
			package pack2;
			public class X {
			}
			""";
		IPackageFragment pack2 = this.sourceFolder.createPackageFragment("pack2", false, null);
		parser.setSource(pack2.createCompilationUnit("X.java", contents, false, null));
		CompilationUnit astRoot2 = (CompilationUnit) parser.createAST(null);
		ImportRewrite rewrite = ImportRewrite.create(astRoot2, true);
		rewrite.setUseContextToFilterImplicitImports(true);
		Type actualType = rewrite.addImport(typeBinding, astRoot2.getAST());
		assertEquals("pack1.X", actualType.toString());
	}

	private ICompilationUnit createCompilationUnit(String packageName, String className) throws JavaModelException {
		StringBuilder contents = new StringBuilder();
		contents.append("package " + packageName + ";\n");
		contents.append("\n");
		contents.append("public class " + className + " {}");
		return createCompilationUnit(packageName, className, contents.toString());
	}

	private ICompilationUnit createCompilationUnit(
			String packageName, String className, String contents) throws JavaModelException {
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(packageName, false, null);
		ICompilationUnit cu = pack1.createCompilationUnit(className + ".java", contents, /* force */ true, null);
		return cu;
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
