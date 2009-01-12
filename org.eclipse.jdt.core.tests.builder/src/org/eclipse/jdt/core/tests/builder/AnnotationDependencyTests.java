/*******************************************************************************
 * Copyright (c) 2009, Walter Harley and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Walter Harley (eclipse@cafewalter.com) - initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Tests to verify that annotation changes cause recompilation of dependent types.
 * See http://bugs.eclipse.org/149768 
 */
public class AnnotationDependencyTests extends BuilderTests {
	private IPath srcRoot = null;
	private IPath projectPath = null;
	
	public AnnotationDependencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(AnnotationDependencyTests.class);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		
		this.projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$
		env.addExternalJars(this.projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(this.projectPath,""); //$NON-NLS-1$

		this.srcRoot = env.addPackageFragmentRoot(this.projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(this.projectPath, "bin"); //$NON-NLS-1$
	}
	
	protected void tearDown() throws Exception {
		this.projectPath = null;
		this.srcRoot = null;
		
		super.tearDown();
	}
	
	private void addAnnotationType() {
		String annoCode = "package p1;\n"
			+ "@interface Anno {\n"
			+ "String value();\n"
			+ "}\n";
		env.addClass(this.srcRoot, "p1", "Anno", annoCode);
	}
	
	/**
	 * This test makes sure that changing an annotation on type A causes type B
	 * to be recompiled, if B references A.  See http://bugs.eclipse.org/149768
	 */
	public void testTypeAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {}";
		String a2Code = "package p1; " + "\n"
			+ "@Anno(\"A2\")" + "\n"
			+ "public class A {}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();
		
		fullBuild( this.projectPath );
		expectingNoProblems();
		
		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
		
		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	
	/**
	 * This test makes sure that changing an annotation on a field within type A 
	 * causes type B to be recompiled, if B references A.  
	 * See http://bugs.eclipse.org/149768
	 */
	public void testFieldAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A1\")" + "\n"
			+ "  protected int f;" + "\n"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A2\")" + "\n"
			+ "  protected int f;" + "\n"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();
		
		fullBuild( this.projectPath );
		expectingNoProblems();
		
		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
		
		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	
	/**
	 * This test makes sure that changing an annotation on a method within type A 
	 * causes type B to be recompiled, if B references A.  
	 * See http://bugs.eclipse.org/149768
	 */
	public void testMethodAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A1\")" + "\n"
			+ "  protected int f() { return 0; }" + "\n"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A2\")" + "\n"
			+ "  protected int f() { return 0; }" + "\n"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();
		
		fullBuild( this.projectPath );
		expectingNoProblems();
		
		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
		
		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.B" });
	}
	
	/**
	 * This test makes sure that changing an annotation on an inner type X within type A 
	 * causes type B to be recompiled, if B references A.  
	 * Note that B does not directly reference A.X, only A. 
	 * See http://bugs.eclipse.org/149768
	 */
	public void testInnerTypeAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A1\")" + "\n"
			+ "  public class X { }" + "\n"
			+ "}";
		String a2Code = "package p1; " + "\n"
			+ "public class A {" + "\n"
			+ "  @Anno(\"A2\")" + "\n"
			+ "  public class X { }" + "\n"
			+ "}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "  public A a;" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();
		
		fullBuild( this.projectPath );
		expectingNoProblems();
		
		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
		
		// verify that B was recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A", "p1.A$X", "p1.B" });
	}
	
	/**
	 * This test makes sure that changing an annotation on a type A
	 * does not cause type B to be recompiled, if B does not reference A.  
	 * See http://bugs.eclipse.org/149768
	 */
	public void testUnrelatedTypeAnnotationDependency() throws Exception
	{
		String a1Code = "package p1; " + "\n"
			+ "@Anno(\"A1\")" + "\n"
			+ "public class A {}";
		String a2Code = "package p1; " + "\n"
			+ "@Anno(\"A2\")" + "\n"
			+ "public class A {}";
		String bCode = "package p1; " + "\n"
			+ "public class B {" + "\n"
			+ "}";

		env.addClass( this.srcRoot, "p1", "A", a1Code );
		env.addClass( this.srcRoot, "p1", "B", bCode );
		addAnnotationType();
		
		fullBuild( this.projectPath );
		expectingNoProblems();
		
		// edit annotation in A
		env.addClass( this.srcRoot, "p1", "A", a2Code );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
		
		// verify that B was not recompiled
		expectingUniqueCompiledClasses(new String[] { "p1.A" });
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214948
	public void testPackageInfoDependency() throws Exception {
		String notypes = "@question.SimpleAnnotation(\"foo\") package notypes;";
		String question = "package question;";
		String deprecatedQuestion = "@Deprecated package question;";
		String SimpleAnnotation = "package question; " + "\n"
			+ "public @interface SimpleAnnotation { String value(); }";

		IPath notypesPath = env.addClass( this.srcRoot, "notypes", "package-info", notypes );
		env.addClass( this.srcRoot, "question", "package-info", question );
		env.addClass( this.srcRoot, "question", "SimpleAnnotation", SimpleAnnotation );

		fullBuild( this.projectPath );
		expectingNoProblems();

		env.addClass( this.srcRoot, "question", "package-info", deprecatedQuestion );
		incrementalBuild( this.projectPath );
		expectingOnlySpecificProblemFor(notypesPath, new Problem("", "The type SimpleAnnotation is deprecated", notypesPath, 1, 26, CategorizedProblem.CAT_DEPRECATION, IMarker.SEVERITY_WARNING)); //$NON-NLS-1$

		env.addClass( this.srcRoot, "question", "package-info", question );
		incrementalBuild( this.projectPath );
		expectingNoProblems();
	}	
}
