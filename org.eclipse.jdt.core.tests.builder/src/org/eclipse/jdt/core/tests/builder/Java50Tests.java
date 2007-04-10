/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

public class Java50Tests extends BuilderTests {

	public Java50Tests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(Java50Tests.class);
	}

	public void testAnnotation() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"@q.Ann\n" +
			"public class Use {\n" +
			"}"
		);
		env.addClass(projectPath, "q", "Ann",
			"package q;\n" +
			"public @interface Ann {\n" +
			"}"
		); 

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Ann",
			"package q;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD)\n" +
			"public @interface Ann {\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			usePath,
			"Problem : The annotation @Ann is disallowed for this location [ resource : </Project/p/Use.java> range : <11,17> category : <40> severity : <2>]"
		);
	}

	public void testParameterizedMemberType() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath xPath = env.addClass(projectPath, "", "X",
			"class X<T> extends A<T> {}"
		);

		IPath aPath = env.addClass(projectPath, "", "A",
			"class A<T> extends B<B<T>.M> {}\n" + 
			"class B<T> extends Missing<T> {\n" + 
			"	class M{}\n" + 
			"}\n" + 
			"class Missing<T> {}"
		); 

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "A",
			"class A<T> extends B<B<T>.M> {}\n" + 
			"class B<T> extends Missing<T> {\n" + 
			"	class M{}\n" + 
			"}"
		); 

		incrementalBuild(projectPath);
		expectingProblemsFor(
			new IPath[] {aPath, xPath},
			"Problem : The hierarchy of the type A is inconsistent [ resource : </Project/A.java> range : <6,7> category : <40> severity : <2>]\n" + 
			"Problem : Missing cannot be resolved to a type [ resource : </Project/A.java> range : <51,58> category : <40> severity : <2>]\n" + 
			"Problem : The hierarchy of the type X is inconsistent [ resource : </Project/X.java> range : <6,7> category : <40> severity : <2>]"
		);

		env.addClass(projectPath, "", "X",
			"class X<T> extends A<T> {}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			new IPath[] {aPath, xPath},
			"Problem : The hierarchy of the type A is inconsistent [ resource : </Project/A.java> range : <6,7> category : <40> severity : <2>]\n" + 
			"Problem : Missing cannot be resolved to a type [ resource : </Project/A.java> range : <51,58> category : <40> severity : <2>]\n" + 
			"Problem : The hierarchy of the type X is inconsistent [ resource : </Project/X.java> range : <6,7> category : <40> severity : <2>]"
		);

		env.addClass(projectPath, "", "A",
			"class A<T> extends B<B<T>.M> {}\n" + 
			"class B<T> extends Missing<T> {\n" + 
			"	class M{}\n" + 
			"}"
		); 

		incrementalBuild(projectPath);
		expectingProblemsFor(
			new IPath[] {aPath, xPath},
			"Problem : The hierarchy of the type A is inconsistent [ resource : </Project/A.java> range : <6,7> category : <40> severity : <2>]\n" + 
			"Problem : Missing cannot be resolved to a type [ resource : </Project/A.java> range : <51,58> category : <40> severity : <2>]\n" + 
			"Problem : The hierarchy of the type X is inconsistent [ resource : </Project/X.java> range : <6,7> category : <40> severity : <2>]"
		);

		env.addClass(projectPath, "", "A",
			"class A<T> extends B<B<T>.M> {}\n" + 
			"class B<T> extends Missing<T> {\n" + 
			"	class M{}\n" + 
			"}\n" + 
			"class Missing<T> {}"
		); 

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testParameterizedType1() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"import java.util.ArrayList;\n" + 
			"import q.Other;\n" + 
			"public class Use {\n" + 
			"	public Use() {\n" + 
			"		new Other().foo(new ArrayList<String>());\n" + 
			"	}\n" + 
			"}"
		);
		env.addClass(projectPath, "q", "Other",
			"package q;\n" + 
			"import java.util.List;\n" + 
			"public class Other {\n" + 
			"	public void foo(List<String> ls) {}\n" + 
			"}"
		); 

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Other",
			"package q;\n" + 
			"import java.util.List;\n" + 
			"public class Other {\n" + 
			"	public void foo(List<Object> ls) {}\n" +
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			usePath,
			"Problem : The method foo(List<Object>) in the type Other is not applicable for the arguments (ArrayList<String>) [ resource : </Project/p/Use.java> range : <104,107> category : <50> severity : <2>]"
		);
	}
	
	public void testParameterizedType2() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5"); 
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "");

		IPath usePath = env.addClass(projectPath, "p", "Use",
			"package p;\n" +
			"import java.util.ArrayList;\n" + 
			"import q.Other;\n" + 
			"public class Use {\n" + 
			"	public Use() {\n" + 
			"		new Other().foo(new ArrayList<String>());\n" + 
			"	}\n" + 
			"}"
		);
		env.addClass(projectPath, "q", "Other",
			"package q;\n" + 
			"import java.util.List;\n" + 
			"public class Other {\n" + 
			"	public void foo(List<String> ls) {}\n" + 
			"}"
		); 

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "q", "Other",
			"package q;\n" + 
			"import java.util.List;\n" + 
			"public class Other {\n" + 
			"	public void foo(List<String> ls) throws Exception {}\n" + 
			"}"
		);

		incrementalBuild(projectPath);
		expectingProblemsFor(
			usePath,
			"Problem : Unhandled exception type Exception [ resource : </Project/p/Use.java> range : <92,132> category : <40> severity : <2>]"
		);
	}
	
}
