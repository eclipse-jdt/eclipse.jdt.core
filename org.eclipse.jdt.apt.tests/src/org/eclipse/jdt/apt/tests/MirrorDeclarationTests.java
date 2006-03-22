/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationCodeExample;

public class MirrorDeclarationTests extends APTTestBase {

	public MirrorDeclarationTests(final String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(MirrorDeclarationTests.class);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String code = MirrorDeclarationCodeExample.CODE;
		env.addClass(srcRoot, MirrorDeclarationCodeExample.CODE_PACKAGE, MirrorDeclarationCodeExample.CODE_CLASS_NAME, code);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}
	
	public void testMirrorDeclaration() throws Exception
	{
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	public void testFieldConstant() throws Exception 
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String codeTrigger =
			"package test;\n" +
			"public @interface Trigger{}";
		
		env.addClass(srcRoot, "test", "Trigger", codeTrigger);
		
		String codeEntryPoint = "package test;\n" +
								"@Trigger\n" +
								"public class EntryPoint {\n" +
								"    ClassWithNestedAnnotation nestedAnno;\n}";
		
		env.addClass(srcRoot, "test", "EntryPoint", codeEntryPoint);

		String codeClassWithNestedAnnotation = 
			"package test; \n" +
			"public class ClassWithNestedAnnotation {\n" +
			"	public final int FOUR = 4; \n " +
			"}";
		
		env.addClass(srcRoot, "test", "ClassWithNestedAnnotation", codeClassWithNestedAnnotation);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}
	
	// TODO: Disabled due to Bugzilla 124388 -theodora
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124388
	public void DISABLED_testDefault() throws Exception
	{	
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String codeTrigger =
			"package test;\n" +
			"public @interface Trigger{}";
		
		env.addClass(srcRoot, "test", "Trigger", codeTrigger);
		
		String codeEntryPoint = "package test;\n" +
								"@Trigger\n" +
								"public class EntryPoint {\n" +
								"    ClassWithNestedAnnotation nestedAnno;\n}";
		
		env.addClass(srcRoot, "test", "EntryPoint", codeEntryPoint);	
		
		String codeClassWithNestedAnnotation = 
			"package test; \n" +
			"public class ClassWithNestedAnnotation {\n" +
			"	public @interface NestedAnnotation{\n" +
			"		public enum Character{ \n" +
			"			Winnie, Tiger, Piglet, Eore; \n" +
			"		}\n"+
			"		Character value() default Character.Eore; \n" +
			"	}\n" +
			"}";
		
		env.addClass(srcRoot, "test", "ClassWithNestedAnnotation", codeClassWithNestedAnnotation);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}
	
	/**
	 * Test AST based mirror implementation and binding based mirror implementation.
	 * Specifically,
	 *   (i) method declaration with unresolvable return type.
	 *  (ii) constructor declaration with unresolvable parameter
	 * (iii) field declaration with unresolvable type.
	 * 
	 * This test focus on declarations from file in context.
	 * 
	 * @throws Exception
	 */
	public void testUnresolvableDeclarations0()
		throws Exception 
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String declAnno =
			"package test;\n" +
			"public @interface DeclarationAnno{}";
		
		env.addClass(srcRoot, "test", "DeclarationAnno", declAnno);
		
		String codeFoo = 
			"package test;\n" +
			"@DeclarationAnno\n" +
			"public class Foo {\n" +
			"    int field0;\n " +
			"    UnknownType field1;\n " +
			"    public Foo(UnknownType type){} \n" +
			"    public void voidMethod(){} \n " +
			"    public UnknownType getType(){}\n " +
			"    public class Inner{} \n" +
			"}";
		
		final IPath fooPath = env.addClass(srcRoot, "test", "Foo", codeFoo);
		fullBuild( project.getFullPath() );
		
		expectingOnlySpecificProblemsFor(fooPath, new ExpectedProblem[]{
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath)}
		);
	}
	
	/**
	 * Test AST based mirror implementation and binding based mirror implementation.
	 * Specifically,
	 *   (i) method declaration with unresolvable return type.
	 *  (ii) constructor declaration with unresolvable parameter
	 * (iii) field declaration with unresolvable type.
	 * 
	 * This test focus on declarations from file outside of processor
	 * environment context.
	 * 
	 * @throws Exception
	 */
	public void testUnresolvableDeclarations1()
		throws Exception 
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String declAnno =
			"package test;\n" +
			"public @interface DeclarationAnno{}";
		
		env.addClass(srcRoot, "test", "DeclarationAnno", declAnno);		
		
		String codeBar = 
			"package test;\n" +
			"@DeclarationAnno\n" +
			"public class Bar {}";		
		env.addClass(srcRoot, "test", "Bar", codeBar);
		
		String codeFoo = 
			"package test;\n" +
			"@DeclarationAnno\n" +
			"public class Foo {\n" +
			"    int field0;\n " +
			"    UnknownType field1;\n " +
			"    public Foo(UnknownType type){} \n" +
			"    public void voidMethod(){} \n " +
			"    public UnknownType getType(){}\n " +
			"    public class Inner{} \n" +
			"}";
		
		final IPath fooPath = env.addClass(srcRoot, "test", "Foo", codeFoo);
		
		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemsFor(fooPath, new ExpectedProblem[]{
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath)}
		);
	}
}
