/*******************************************************************************
 * Copyright (c) 2024 GK Software SE, and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

import junit.framework.Test;

public class ExternalAnnotationsOwningTest extends ExternalAnnotations18Test {

	public ExternalAnnotationsOwningTest(String name) {
		super(name, "21", "JCL_21_LIB");
	}

	static {
		TESTS_NAMES = new String[] {"testOwningMethod"};
	}

	public static Test suite() {
		return buildModelTestSuite(ExternalAnnotationsOwningTest.class, BYTECODE_DECLARATION_ORDER);
	}

	@Override
	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePathBase()+"/ExternalAnnotationsOwning";
	}

	void myCreateJavaProject(String name, String projectCompliance, String projectJclLib) throws CoreException {
		this.project = createJavaProject(name, new String[]{"src"}, new String[]{projectJclLib}, null, null, "bin", null, null, null, projectCompliance);
		addLibraryEntry(this.project, this.ANNOTATION_LIB, false);
		Map<String, String> options = this.project.getOptions(true);
		options.put(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
		this.project.setOptions(options);

		IPackageFragmentRoot[] roots = this.project.getAllPackageFragmentRoots();
		int count = 0;
		for (int i = 0, max = roots.length; i < max; i++) {
			final IPackageFragmentRoot packageFragmentRoot = roots[i];
			switch(packageFragmentRoot.getKind()) {
				case IPackageFragmentRoot.K_SOURCE :
					count++;
					if (this.root == null) {
						this.root = packageFragmentRoot;
					}
			}
		}
		assertEquals("Wrong value", 1, count); //$NON-NLS-1$
		assertNotNull("Should not be null", this.root); //$NON-NLS-1$
	}

	public void testNotOwningMethod() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"""
					package libs;

					public interface Lib1 {
						AutoCloseable getResource();
					}
					"""
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"getResource\n" +
				" ()Ljava/lang/AutoCloseable;\n" +
				" ()L8java/lang/AutoCloseable;\n"); // NotOwning
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"""
					package tests;

					import libs.Lib1;

					public class Test1 {
						String test(Lib1 lib1) {
							AutoCloseable rc = lib1.getResource();
							return rc.toString();
						}
					}
					""",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.getJLSLatest(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

	public void testOwningParameter() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"""
					package libs;

					public interface Lib1 {
						void consume(AutoCloseable rc);
					}
					"""
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"consume\n" +
				" (Ljava/lang/AutoCloseable;)V\n" +
				" (L9java/lang/AutoCloseable;)V\n"); // Owning
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"""
					package tests;

					import libs.Lib1;

					public class Test1 {
						void test(Lib1 lib1) {
							AutoCloseable rc = new AutoCloseable() { public void close() {} };
							lib1.consume(rc);
						}
					}
					""",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.getJLSLatest(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

	public void testOwningField() throws Exception {
		myCreateJavaProject("TestLibs");
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/libs/Lib1.java",
				"""
					package libs;

					public class Lib1 {
						public static AutoCloseable rc;
					}
					"""
			}, null);
		createFileInProject("annots/libs", "Lib1.eea",
				"class libs/Lib1\n" +
				"\n" +
				"rc\n" +
				" Ljava/lang/AutoCloseable;\n" +
				" L9java/lang/AutoCloseable;\n"); // Owning
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit unit = fragment.createCompilationUnit("Test1.java",
				"""
					package tests;

					import libs.Lib1;

					public class Test1 {
						void test() {
							Lib1.rc = new AutoCloseable() { public void close() {} };
						}
					}
					""",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = unit.reconcile(AST.getJLSLatest(), true, null, new NullProgressMonitor());
		IProblem[] problems = reconciled.getProblems();
		assertNoProblems(problems);
	}

}
