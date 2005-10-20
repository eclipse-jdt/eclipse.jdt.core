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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;

public class MixedModeTesting extends APTTestBase{
	
	private File _extJar; // external annotation jar	
	
	public MixedModeTesting(String name){
		super(name);
	}
	
	
	public static Test suite()
	{	
		return new TestSuite( MixedModeTesting.class );
	}
	
	public void setUp() throws Exception
	{
		super.setUp();
		IJavaProject javaProj = env.getJavaProject( getProjectName() );
		_extJar = TestUtil.createAndAddExternalAnnotationJar(javaProj);
				
		// This file will be locked until GC takes care of unloading the
		// annotation processor classes, so we can't delete it ourselves.
		_extJar.deleteOnExit();
		
		FactoryPath fp = (FactoryPath)AptConfig.getFactoryPath(javaProj);
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(_extJar);
		fp.addEntryToHead(fc, /*isEnabled=*/ true, /*runInBatchMode=*/ true);
		AptConfig.setFactoryPath(javaProj, fp);
	}
	
	/**
	 * Only one batch processor is involved 
	 */
	public void testSimpleBatchProcessing() throws Exception
	{	
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String codeA = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.external.annotations.batch.*;"
			+ "\n@Batch\n"
			+ "public class A {}\n";
		
		env.addClass( srcRoot, "p1", "A", codeA );
		
		String codeB = "package p1;\n"
			+ "\n@Deprecated\n"
			+ "public class B {}\n";
		
		env.addClass( srcRoot, "p1", "B", codeB );
		
		String codeC = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.external.annotations.batch.*;"
			+ "\n@Batch\n"
			+ "public class C {}\n";
		
		env.addClass( srcRoot, "p1", "C", codeC );

		fullBuild( project.getFullPath() );		
		expectingNoProblems();
		expectingMarkers(new String[]{"CompletedSuccessfully"});
	}
	
	public void testAPTRoundingInMixedMode() throws CoreException
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String codeX = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.annotations.aptrounding.*;"
			+ "\n@GenBean\n"
			+ "public class X {}\n";
		
		env.addClass( srcRoot, "p1", "X", codeX );
		
		String codeY = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.annotations.aptrounding.*;"
			+ "public class Y { @GenBean2 test.Bean _bean = null; }\n";
		
		env.addClass( srcRoot, "p1", "Y", codeY );
		
		String codeA = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.external.annotations.batch.*;"
			+ "\n@Batch\n"
			+ "public class A {}\n";
		
		env.addClass( srcRoot, "p1", "A", codeA );
		
		String codeB = "package p1;\n"
			+ "\n@Deprecated\n"
			+ "public class B {}\n";
		
		env.addClass( srcRoot, "p1", "B", codeB );
		
		String codeC = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.external.annotations.batch.*;"
			+ "\n@Batch\n"
			+ "public class C {}\n";
		
		env.addClass( srcRoot, "p1", "C", codeC );

		fullBuild( project.getFullPath() );
		expectingMarkers(new String[]{"CompletedSuccessfully"});
		
		expectingNoProblems();
		
		// Now run it again to verify that the classloader was successfully bounced
		fullBuild( project.getFullPath() );
		expectingMarkers(new String[]{"CompletedSuccessfully"});
		
		expectingNoProblems();
	}
}
