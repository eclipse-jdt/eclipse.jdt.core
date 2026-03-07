/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Given:
 * <pre>
 * project A
 *     src/
 *         a/A.java
 *             package a;
 *             public interface A {}
 *
 * project B
 *     src/
 *         b/I.java
 *             package b;
 *             public interface I {}
 *         b/B.java
 *             package b;
 *             public class B implements a.A, I {}
 *
 * project C
 *     src/
 *         c/C1.java
 *             package c;
 *             public class C1 {
 *                 public static b.B create() { ... }
 *                 public static void foo(b.I i) {}
 *             }
 *         c/C2.java
 *             package c;
 *             public class C2 {
 *                 public void c() { C1.foo(C1.create()); }
 *             }
 * </pre>
 * Expect compile error at: {@code C1.foo(C1.create());}
 * <pre>
 * The type a.A cannot be resolved. It is indirectly referenced from required type b.B
 * </pre>
 */
public class GH4747Test extends BuilderTests {

	private static final String SRC = "SRC";

	private static final String A = """
		    package a;
			public interface A {}
			""";
	private static final String I = """
		    package b;
			public interface I {}
			""";
	private static final String B = """
		    package b;
			public class B implements a.A, I {}
			""";
	private static final String C1_1 = """
			package c;
			public class C1 {
				public static b.B create() { return null; }
				public static void foo(b.I i) {}
			}
			""";
	private static final String C1_2 = """
			package c;
			public class C1 {
				public static b.B create() { return new b.B(); }
				public static void foo(b.I i) {}
			}
			""";
	private static final String C2 = """
			package c;
			public class C2 {
				public void c() { C1.foo(C1.create()); }
			}
			""";

	private static final String PROBLEMS = """
			The project was not built since its build path is incomplete. Cannot find the class file for a.A. Fix the build path then try building this project
			The type a.A cannot be resolved. It is indirectly referenced from required type b.B""";

	public GH4747Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(GH4747Test.class);
	}

	public void test1() throws Exception {
		IPath projectA = createProject("TestProjectA", "a");
		env.addClass(getPackagePath(projectA, "a"), "A", A);
		env.fullBuild(projectA);
		expectingNoProblemsFor(projectA);

		IPath projectB = createProject("TestProjectB", "b");
		env.addClass(getPackagePath(projectB, "b"), "I", I);
		env.addClass(getPackagePath(projectB, "b"), "B", B);
		env.addRequiredProject(projectB, projectA);
		env.fullBuild(projectB);
		expectingNoProblemsFor(projectB);

		IPath projectC = createProject("TestProjectC", "c");
		env.addClass(getPackagePath(projectC, "c"), "C1", C1_1);
		env.addClass(getPackagePath(projectC, "c"), "C2", C2);
		env.addRequiredProject(projectC, projectB);
		env.fullBuild(projectC);
		expectingParticipantProblems(projectC, PROBLEMS);
	}

	public void test2() throws Exception {
		IPath projectA = createProject("TestProjectA", "a");
		env.addClass(getPackagePath(projectA, "a"), "A", A);
		env.fullBuild(projectA);
		expectingNoProblemsFor(projectA);

		IPath projectB = createProject("TestProjectB", "b");
		env.addClass(getPackagePath(projectB, "b"), "I", I);
		env.addClass(getPackagePath(projectB, "b"), "B", B);
		env.addRequiredProject(projectB, projectA);
		env.fullBuild(projectB);
		expectingNoProblemsFor(projectB);

		IPath projectC = createProject("TestProjectC", "c");
		env.addClass(getPackagePath(projectC, "c"), "C1", C1_2);
		env.addClass(getPackagePath(projectC, "c"), "C2", C2);
		env.addRequiredProject(projectC, projectB);
		env.fullBuild(projectC);
		expectingParticipantProblems(projectC, PROBLEMS);
	}

	private IPath createProject(String projectName, String packageName) throws JavaModelException {
		IPath project = env.addProject(projectName);
		env.addExternalJars(project, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(project, "");
		IPath src = env.addPackageFragmentRoot(project, SRC);
		env.addPackage(src, packageName);
		env.addProject(env.getProject(project));
		return project;
	}

	private static IPath getPackagePath(IPath project, String packageName) {
		IPath root = env.getPackageFragmentRootPath(project, SRC);
		IPath path = env.getPackagePath(root, packageName);
		return path;
	}
}
