/*******************************************************************************
 * Copyright (c) 2015, 2022 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil.MergeStrategy;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.osgi.framework.Bundle;

import junit.framework.Test;

public class ExternalAnnotations17Test extends ExternalAnnotations18Test {


	public ExternalAnnotations17Test(String name) {
		super(name, "1.7", "JCL17_LIB");
	}

// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_PREFIX = "testLibsWithTypeParameters";
//		TESTS_NAMES = new String[] {"testLibsWithFields"};
//		TESTS_NUMBERS = new int[] { 23, 28, 38 };
//		TESTS_RANGE = new int[] { 21, 38 };
	}
	public static Test suite() {
		return buildModelTestSuite(ExternalAnnotations17Test.class, BYTECODE_DECLARATION_ORDER);
	}

	/**
	 * @deprecated
	 */
	static int getJLS8() {
		return AST.JLS8;
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	@Override
	protected Bundle[] getAnnotationBundles() {
		return org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[1.1.0,2.0.0)");
	}

	@Override
	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePathBase()+"/ExternalAnnotations17";
	}

	@Override
	public void test1FullBuild() throws Exception {
		setupJavaProject("Test1");
		this.project.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyMap.java",
				MY_MAP_CONTENT
			}, null);
		this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		sortMarkers(markers);
		assertMarkers("Unexpected markers",
				"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
				"Null type mismatch: required \'@NonNull Test1\' but the provided value is null\n" +
				"Potential null pointer access: The variable v may be null at this location",
				markers);
	}

	/** Perform full build, annotations are found relative to a variable. */
	@Override
	public void test1FullBuildWithVariable() throws Exception {
		setupJavaProject("Test1");
		this.project.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		JavaCore.setClasspathVariable("MY_PRJ_ROOT", this.project.getProject().getLocation(), null);
		try {
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "MY_PRJ_ROOT/annots", new String[] {
					"/UnannotatedLib/libs/MyMap.java",
					MY_MAP_CONTENT
				}, null);
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			sortMarkers(markers);
			assertMarkers("Unexpected markers",
					"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
					"Null type mismatch: required \'@NonNull Test1\' but the provided value is null\n" +
					"Potential null pointer access: The variable v may be null at this location",
					markers);
		} finally {
			JavaCore.removeClasspathVariable("MY_PRJ_ROOT", null);
		}
	}

	public void test1Full_ProjectRoot() throws Exception {
		// cf. testLibsWithFields but with annotations at the project root:
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "/TestLibs", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	String one = \"1\";\n" +
				"	String none = null;\n" +
				"}\n"
			}, null);
		createFileInProject("libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"one\n" +
				" Ljava/lang/String;\n" +
				" L1java/lang/String;\n" +
				"\n" +
				"none\n" +
				" Ljava/lang/String;\n" +
				" L0java/lang/String;\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0() {\n" +
				"		return Lib1.none;\n" +
				"	}\n" +
				"	@NonNull String test1() {\n" +
				"		return Lib1.one;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(933) Null type mismatch: required '@NonNull String' but the provided value is specified as @Nullable",
		}, new int[] { 8 });
	}

	public void testTwoProjects_a() throws Exception {
		IJavaProject prj2 = null;
		try {
			//  create project #1:
			myCreateJavaProject("TestLibs1");
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "/TestLibs1", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					"package libs;\n" +
					"\n" +
					"public class Lib1 {\n" +
					"	public static String one = \"1\";\n" +  // not final!
					"	public static String none = null;\n" +
					"}\n"
				}, null);
			createFileInProject("libs", "Lib1.eea",
					"class libs/Lib1\n" +
					"\n" +
					"one\n" +
					" Ljava/lang/String;\n" +
					" L1java/lang/String;\n" +
					"\n" +
					"none\n" +
					" Ljava/lang/String;\n" +
					" L0java/lang/String;\n" +
					"\n");
			IPackageFragment fragment1 = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);

			//  create project #2 (referring to the same lib, but without external annotations):
			prj2 = createJavaProject("TestLibs2", new String[]{"src"}, new String[]{this.jclLib, "/TestLibs1/lib1.jar"}, null, null, "bin", null, null, null, this.compliance);
			addLibraryEntry(prj2, this.ANNOTATION_LIB, false);
			Map<String,String> options = prj2.getOptions(true);
			options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			prj2.setOptions(options);
			IPackageFragment fragment2 = prj2.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);

			// test project #1:
			ICompilationUnit unit = fragment1.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull String test0() {\n" +
					"		return Lib1.none;\n" +
					"	}\n" +
					"	@NonNull String test1() {\n" +
					"		return Lib1.one;\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
				"Pb(933) Null type mismatch: required '@NonNull String' but the provided value is specified as @Nullable",
			}, new int[] { 8 });

			// test project #2:
			unit = fragment2.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull String test0() {\n" +
					"		return Lib1.none;\n" +
					"	}\n" +
					"	@NonNull String test1() {\n" +
					"		return Lib1.one;\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
					"Pb(912) Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
				}, new int[] { 8, 11 });
		} finally {
			if (prj2 != null)
				prj2.getProject().delete(true, true, null);
		}
	}

	// same as above but swapped order
	public void testTwoProjects_b() throws Exception {
		IJavaProject prj2 = null;
		try {
			//  create project #1:
			myCreateJavaProject("TestLibs1");
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "/TestLibs1", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					"package libs;\n" +
					"\n" +
					"public class Lib1 {\n" +
					"	public static String one = \"1\";\n" + // not final!
					"	public static String none = null;\n" +
					"}\n"
				}, null);
			createFileInProject("libs", "Lib1.eea",
					"class libs/Lib1\n" +
					"\n" +
					"one\n" +
					" Ljava/lang/String;\n" +
					" L1java/lang/String;\n" +
					"\n" +
					"none\n" +
					" Ljava/lang/String;\n" +
					" L0java/lang/String;\n" +
					"\n");
			IPackageFragment fragment1 = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);

			//  create project #2 (referring to the same lib, but without external annotations):
			prj2 = createJavaProject("TestLibs2", new String[]{"src"}, new String[]{this.jclLib, "/TestLibs1/lib1.jar"}, null, null, "bin", null, null, null, this.compliance);
			addLibraryEntry(prj2, this.ANNOTATION_LIB, false);
			Map<String,String> options = prj2.getOptions(true);
			options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			prj2.setOptions(options);
			IPackageFragment fragment2 = prj2.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);

			// test project #2:
			ICompilationUnit unit = fragment2.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull String test0() {\n" +
					"		return Lib1.none;\n" +
					"	}\n" +
					"	@NonNull String test1() {\n" +
					"		return Lib1.one;\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
					"Pb(912) Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
				}, new int[] { 8, 11 });


			// test project #1:
			unit = fragment1.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull String test0() {\n" +
					"		return Lib1.none;\n" +
					"	}\n" +
					"	@NonNull String test1() {\n" +
					"		return Lib1.one;\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
				"Pb(933) Null type mismatch: required '@NonNull String' but the provided value is specified as @Nullable",
			}, new int[] { 8 });
		} finally {
			if (prj2 != null)
				prj2.getProject().delete(true, true, null);
		}
	}

	/** Reconcile an individual CU. */
	@Override
	public void test1Reconcile() throws Exception {
		setupJavaProject("Test1");
		this.project.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyMap.java",
				MY_MAP_CONTENT
			}, null);
		IPackageFragment fragment = this.root.getPackageFragment("test1");
		ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems,
					new String[] {
						"Pb(910) Null type mismatch: required \'@NonNull Test1\' but the provided value is null",
						"Pb(910) Null type mismatch: required \'@NonNull Object\' but the provided value is null",
						"Pb(452) Potential null pointer access: The variable v may be null at this location"
					},
					new int[]{ 7, 8, 9});
	}

	@Override
	public void testLibsWithFields() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	String one = \"1\";\n" +
				"	String none = null;\n" +
				"}\n"
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"one\n" +
				" Ljava/lang/String;\n" +
				" L1java/lang/String;\n" +
				"\n" +
				"none\n" +
				" Ljava/lang/String;\n" +
				" L0java/lang/String;\n" +
				"\n");
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0() {\n" +
				"		return Lib1.none;\n" +
				"	}\n" +
				"	@NonNull String test1() {\n" +
				"		return Lib1.one;\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(933) Null type mismatch: required '@NonNull String' but the provided value is specified as @Nullable",
		}, new int[] { 8 });
	}

	// ===== Full round trip: detect problem - annotated - detect problem change =====

	public void testAnnotateFieldOfNested() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public interface Lib1 {\n" +
				"	public static class Nested {\n" +
				"		public static String one = \"1\";\n" +
				"	}\n" +
				"}\n"
			}, null);

		// acquire source AST:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		String test1Content = "package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull String test0() {\n" +
				"		return Lib1.Nested.one;\n" +
				"	}\n" +
				"}\n";
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java", test1Content,
						true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		IProblem[] problems = unit.getProblems();
		assertProblems(problems, new String[] {
				"Pb(912) Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'",
		}, new int[] { 8 });

		// find type binding:
		int start = test1Content.indexOf("one");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		IVariableBinding fieldBinding = (IVariableBinding) ((SimpleName)name).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, fieldBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1$Nested.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericTypeSignature(fieldBinding.getVariableDeclaration().getType());
		ExternalAnnotationUtil.annotateMember("libs/Lib1$Nested", annotationFile,
				"one",
				originalSignature,
				"L1java/lang/String;",
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is gone:
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}


	@Override
	public void testAnnotateFieldWithParameterizedType() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"package libs;\n" +
				"\n" +
				"public class Lib1<T> {\n" +
				"	public Lib1<T> one;\n" +
				"}\n"
			}, null);

		// acquire source AST:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		String test1Content = "package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
				"		return stringLib.one;\n" +
				"	}\n" +
				"}\n";
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java", test1Content,
						true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		IProblem[] problems = unit.getProblems();
		assertProblems(problems, new String[] {
				"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
		}, new int[] { 8 });

		// find type binding:
		int start = test1Content.indexOf("one");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		IVariableBinding fieldBinding = (IVariableBinding) ((SimpleName)name).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, fieldBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericTypeSignature(fieldBinding.getVariableDeclaration().getType());
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"one",
				originalSignature,
				"L0libs/Lib1<TT;>;",
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is even worse now:
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(933) Null type mismatch: required '@NonNull Lib1<String>' but the provided value is specified as @Nullable",
		}, new int[] { 8 });
	}

	public void testAnnotateMethodReturn() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"public interface Lib1<T> {\n" +
				"	public Lib1<T> getLib();\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content
			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"import libs.Lib1;\n" +
				"\n" +
				"public class Test1 {\n" +
				"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
				"		return stringLib.getLib();\n" +
				"	}\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
				"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
		}, new int[] { 8 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find method binding:
		int start = lib1Content.indexOf("getLib");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"getLib",
				originalSignature,
				"()L1libs/Lib1<TT;>;",
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error has gone:
		reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}

	// ==== error scenarii: ====

	// annotation file is empty
	public void testBogusAnnotationFile1() throws Exception {
		LogListener listener = new LogListener();
		try {
			Platform.addLogListener(listener);

			myCreateJavaProject("TestLibs");
			String lib1Content =
					"package libs;\n" +
					"\n" +
					"public interface Lib1<T> {\n" +
					"	public Lib1<T> getLib();\n" +
					"}\n";
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					lib1Content
				}, null);
			createFileInProject("annots/libs", "Lib1.eea",
					"");

			// type check sources:
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
			ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
					"		return stringLib.getLib();\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
			}, new int[] { 8 });

			assertEquals("number of log entries", 1, listener.loggedStatus.size());
			final Throwable exception = listener.loggedStatus.get(0).getException();
			assertEquals("logged message", "missing class header in annotation file for libs/Lib1", exception.getMessage());
		} finally {
			Platform.removeLogListener(listener);
		}
	}

	// wrong class header
	public void testBogusAnnotationFile2() throws Exception {
		LogListener listener = new LogListener();
		try {
			Platform.addLogListener(listener);

			myCreateJavaProject("TestLibs");
			String lib1Content =
					"package libs;\n" +
					"\n" +
					"public interface Lib1<T> {\n" +
					"	public Lib1<T> getLib();\n" +
					"}\n";
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					lib1Content
				}, null);
			createFileInProject("annots/libs", "Lib1.eea",
					"type Lib1\n");

			// type check sources:
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
			ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
					"		return stringLib.getLib();\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
			}, new int[] { 8 });

			assertEquals("number of log entries", 1, listener.loggedStatus.size());
			final Throwable exception = listener.loggedStatus.get(0).getException();
			assertEquals("logged message", "missing class header in annotation file for libs/Lib1", exception.getMessage());
		} finally {
			Platform.removeLogListener(listener);
		}
	}
	// wrong selector
	public void testBogusAnnotationFile3() throws Exception {
		LogListener listener = new LogListener();
		try {
			Platform.addLogListener(listener);

			myCreateJavaProject("TestLibs");
			String lib1Content =
					"package libs;\n" +
					"\n" +
					"public interface Lib1<T> {\n" +
					"	public Lib1<T> getLib();\n" +
					"}\n";
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					lib1Content
				}, null);
			createFileInProject("annots/libs", "Lib1.eea",
					"class libs/Lib1\n" +
					"\n" +
					"()V\n" +
					"foo\n");

			// type check sources:
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
			ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
					"		return stringLib.getLib();\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
			}, new int[] { 8 });

			assertEquals("number of log entries", 1, listener.loggedStatus.size());
			final Throwable exception = listener.loggedStatus.get(0).getException();
			assertEquals("logged message", "Illegal selector in external annotation file for libs/Lib1 at line 3: \"()V\"", exception.getMessage());
		} finally {
			Platform.removeLogListener(listener);
		}
	}
	// invalid raw signature
	public void testBogusAnnotationFile4() throws Exception {
		LogListener listener = new LogListener();
		try {
			Platform.addLogListener(listener);

			myCreateJavaProject("TestLibs");
			String lib1Content =
					"package libs;\n" +
					"\n" +
					"public interface Lib1<T> {\n" +
					"	public Lib1<T> getLib();\n" +
					"}\n";
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					lib1Content
				}, null);
			createFileInProject("annots/libs", "Lib1.eea",
					"class libs/Lib1\n" +
					"\n" +
					"foo\n" +
					" wrong()\n");

			// type check sources:
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
			ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
					"		return stringLib.getLib();\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
			}, new int[] { 8 });

			assertEquals("number of log entries", 1, listener.loggedStatus.size());
			final Throwable exception = listener.loggedStatus.get(0).getException();
			assertEquals("logged message", "Illegal format in external annotation file for libs/Lib1 at line 4: invalid signature \"wrong()\"", exception.getMessage());
		} finally {
			Platform.removeLogListener(listener);
		}
	}
	// invalid annotated signature
	public void testBogusAnnotationFile5() throws Exception {
		LogListener listener = new LogListener();
		try {
			Platform.addLogListener(listener);

			myCreateJavaProject("TestLibs");
			String lib1Content =
					"package libs;\n" +
					"\n" +
					"public interface Lib1<T> {\n" +
					"	public Lib1<T> getLib();\n" +
					"}\n";
			addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
					"/UnannotatedLib/libs/Lib1.java",
					lib1Content
				}, null);
			createFileInProject("annots/libs", "Lib1.eea",
					"class libs/Lib1\n" +
					"\n" +
					"foo\n" +
					" ()V\n" +
					" wrong()\n");

			// type check sources:
			IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
			ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
					"package tests;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"import libs.Lib1;\n" +
					"\n" +
					"public class Test1 {\n" +
					"	@NonNull Lib1<String> test0(Lib1<String> stringLib) {\n" +
					"		return stringLib.getLib();\n" +
					"	}\n" +
					"}\n",
					true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
			CompilationUnit reconciled = cu.reconcile(getJLS8(), true, null, new NullProgressMonitor());
			IProblem[] problems = reconciled.getProblems();
			assertProblems(problems, new String[] {
					"Pb(912) Null type safety: The expression of type 'Lib1<String>' needs unchecked conversion to conform to '@NonNull Lib1<String>'",
			}, new int[] { 8 });

			assertEquals("number of log entries", 1, listener.loggedStatus.size());
			final Throwable exception = listener.loggedStatus.get(0).getException();
			assertEquals("logged message", "Illegal format in external annotation file for libs/Lib1 at line 5: invalid signature \"wrong()\"", exception.getMessage());
		} finally {
			Platform.removeLogListener(listener);
		}
	}

	public void testBug565246() throws Exception {
		myCreateJavaProject("TestForloop");
		this.project.setOption(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
		this.project.setOption(CompilerOptions.OPTION_SimulateOperandStack, CompilerOptions.DISABLED);

		// std API missing from jclMin:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("java.lang", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Iterable.java",
				"package java.lang;\n" +
				"public interface Iterable<T> {\n" +
				"	@org.eclipse.jdt.annotation.NonNull java.util.Iterator<T> iterator();\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());

		createFileInProject("annots/java/util", "Iterator.eea",
				"class java/util/Iterator\n" +
				"\n" +
				"next\n" +
				" ()TE;\n" +
				" ()T1E;\n" + // this @NonNull should be respected by analysis of ForeachStatement
				"\n");
		addEeaToVariableEntry("JCL17_LIB", "annots");

		fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		unit = fragment.createCompilationUnit("B.java",
				"package tests;\n" +
				"\n" +
				"import java.util.Iterator;\n" +
				"\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public class B<E> extends A<E> {\n" +
				"\n" +
				"	public void barKOWithForLoop(I<? extends E> c) {\n" +
				"		for (E e : c) {\n" +
				"			foo(e); //<-- WRONG: Null type safety: The expression of type 'E' needs unchecked conversion to conform to '@NonNull E'\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	public void barOKWithWhileIteratorLoop(I<? extends E> c) {\n" +
				"		Iterator<? extends E> it = c.iterator();\n" +
				"		while (it.hasNext()) {\n" +
				"			E e = it.next(); // <-- OK\n" +
				"			foo(e);\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	public void foo(E e) { }\n" +
				"}\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"abstract class A<E> implements I<E> {\n" +
				"\n" +
				"	@Nullable public E e;\n" +
				"\n" +
				"	public Iterator<E> iterator() {\n" +
				"		return new Iterator<E>() {\n" +
				"			public boolean hasNext() {\n" +
				"				return false;\n" +
				"			}\n" +
				"			public E next() {\n" +
				"				E e = A.this.e;\n" +
				"				assert e != null;\n" +
				"				return e;\n" +
				"			}\n" +
				"			public void remove() {}\n" +
				"		};\n" +
				"	}\n" +
				"\n" +
				"	public void foo(E e) {\n" +
				"		throw new RuntimeException();\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"interface I<E> extends Iterable<E> {\n" +
				"	public Iterator<E> iterator();\n" +
				"	public void foo(E e);\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(getJLS8(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}
}
