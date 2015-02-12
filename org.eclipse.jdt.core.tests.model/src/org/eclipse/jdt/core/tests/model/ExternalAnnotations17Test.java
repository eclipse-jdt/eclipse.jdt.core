/*******************************************************************************
 * Copyright (c) 2015 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.osgi.framework.Bundle;

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
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	protected Bundle[] getAnnotationBundles() {
		return org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[1.1.0,2.0.0)");
	}
	
	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePathBase()+"/ExternalAnnotations17";
	}

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

	/** Reconcile an individual CU. */
	public void test1Reconcile() throws Exception {
		setupJavaProject("Test1");
		this.project.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/MyMap.java",
				MY_MAP_CONTENT
			}, null);
		IPackageFragment fragment = this.root.getPackageFragment("test1");
		ICompilationUnit unit = fragment.getCompilationUnit("Test1.java").getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems,
					new String[] {
						"Pb(910) Null type mismatch: required \'@NonNull Test1\' but the provided value is null",
						"Pb(910) Null type mismatch: required \'@NonNull Object\' but the provided value is null",
						"Pb(452) Potential null pointer access: The variable v may be null at this location"
					},	
					new int[]{ 7, 8, 9});
	}

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
		CompilationUnit reconciled = unit.reconcile(AST.JLS8, true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertProblems(problems, new String[] {
			"Pb(933) Null type mismatch: required '@NonNull String' but the provided value is specified as @Nullable",
		}, new int[] { 8 });
	}

}
