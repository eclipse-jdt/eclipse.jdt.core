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
}
