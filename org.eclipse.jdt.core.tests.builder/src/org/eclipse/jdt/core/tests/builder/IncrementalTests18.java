/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class IncrementalTests18 extends BuilderTests {

	public IncrementalTests18(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(IncrementalTests18.class);
	}


	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423122, [1.8] Missing incremental build dependency from lambda expression to functional interface.
	public void test423122() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "I",
			"package p;	\n"+
			"public interface I { void foo(); }	\n"
		);
		env.addClass(root, "p", "X",
				"package p;	\n"+
				"public class X { I i = () -> {}; }	\n"
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "I",
				"package p;	\n"+
				"public interface I { }	\n"
			);
		incrementalBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : The target type of this expression must be a functional interface [ resource : </Project/src/p/X.java> range : <35,40> category : <40> severity : <2>]"
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423122, [1.8] Missing incremental build dependency from lambda expression to functional interface.
	public void test423122a() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "test1", "I",
				"package test1;\n" +
				"public interface I {\n" +
				"    int method(int a); // change argument type to Object\n" +
				"}\n"
		);
		env.addClass(root, "test1", "E",
				"package test1;\n" +
				"public class E {\n" +
				"    void take(I i) {\n" +
				"    }\n" +
				"}\n"
		);
		env.addClass(root, "test1", "Ref",
				"package test1;\n" +
				"public class Ref {\n" +
				"    void foo(E e) {\n" +
				"        e.take((x) -> x+2); // not recompiled when I#method changed\n" +
				"    }\n" +
				"}\n"
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "test1", "I",
				"package test1;\n" +
				"public interface I {\n" +
				"    int method(Object a); // change argument type to Object\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : The operator + is undefined for the argument type(s) Object, int [ resource : </Project/src/test1/Ref.java> range : <76,79> category : <60> severity : <2>]"
		);
		env.addClass(root, "test1", "I",
				"package test1;\n" +
				"public interface I {\n" +
				"    int method(int a); // change argument type back to int\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
}
