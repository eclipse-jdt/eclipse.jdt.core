/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.readannotation.CodeExample;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

public class ReadAnnotationTests extends Tests 
{
	private int counter = 0;
	private String projectName = null;
	public ReadAnnotationTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( ReadAnnotationTests.class );
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getUniqueProjectName(){
		projectName = ReadAnnotationTests.class.getName() + "Project" + counter; //$NON-NLS-1$
		counter ++;
		return projectName;
	}
	

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public IPath getBinaryPath(){
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "binary" ); //$NON-NLS-1$
		IPath lib = srcFolder.getFullPath();
		return lib;
	}
	
	public IPath getOutputPath(){
		IProject project = env.getProject( getProjectName() );
		IFolder binFolder = project.getFolder( "bin" ); //$NON-NLS-1$
		IPath bin = binFolder.getFullPath();
		return bin;
	}
	
	private void addAllSources()
	{
		addQuestionSources();
		addTriggerSource();
	}
	
	private void addQuestionSources()
	{
		IPath srcRoot = getSourcePath();
		// SimpleAnnotation.java
		env.addClass( 
				srcRoot, 
				CodeExample.PACKAGE_QUESTION, 
				CodeExample.SIMPLE_ANNOTATION_CLASS,
				CodeExample.SIMPLE_ANNOTATION_CODE );
		
		// RTVisibleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION, 
				CodeExample.RTVISIBLE_CLASS,
				CodeExample.RTVISIBLE_ANNOTATION_CODE);
		
		// RTInvisibleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION, 
				CodeExample.RTINVISIBLE_CLASS,
				CodeExample.RTINVISIBLE_ANNOTATION_CODE);
		
		// package-info.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION, 
				CodeExample.PACKAGE_INFO_CLASS,
				CodeExample.PACKAGE_INFO_CODE);
		
		// Color.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION, 
				CodeExample.COLOR_CLASS,
				CodeExample.COLOR_CODE);
		
		// AnnotationTest.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION, 
				CodeExample.ANNOTATION_TEST_CLASS,
				CodeExample.ANNOTATION_TEST_CODE);
	}
	
	private void addTriggerSource()
	{
		IPath srcRoot = getSourcePath();
		// MyMarkerAnnotation.java
		env.addClass(srcRoot,
				CodeExample.PACKAGE_TRIGGER,
				CodeExample.MYMARKERANNOTATION_CLASS,
				CodeExample.MYMARKERANNOTATION_CODE);
		
		// Trigger.java
		env.addClass(srcRoot,
				CodeExample.PACKAGE_TRIGGER,
				CodeExample.TRIGGER_CLASS,
				CodeExample.TRIGGER_CODE);
	}
	
	private IProject setupTest() throws Exception
	{				
		ProcessorTestStatus.reset();
		// project will be deleted by super-class's tearDown() method
		IPath projectPath = env.addProject( getUniqueProjectName(), "1.5" ); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$ 
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$

		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		return env.getProject(getProjectName());
	}
	
	/**
	 * Set up all the source files for testing.
	 * Runs the AnnotationReaderProcessor, which contains
	 * the actual testing.
	 */

	public void test0() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		addAllSources();	
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Set up the jar file for testing.
	 * Runs the AnnotationReaderProcessor, which contains
	 * the actual testing.
	 */
	public void test1() throws Exception 
	{	
		IProject project = setupTest();
		final File jar = 
			TestUtil.getFileInPlugin(AptTestsPlugin.getDefault(), 
									 new Path("/src/org/eclipse/jdt/apt/tests/annotations/readannotation/lib/question.jar")); //$NON-NLS-1$
		final String path = jar.getAbsolutePath();
		env.addExternalJar(project.getFullPath(), path);
				
		addTriggerSource();
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
}
