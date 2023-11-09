/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
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

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		IJavaProject javaProj = env.getJavaProject( getProjectName() );
		_extJar = TestUtil.createAndAddExternalAnnotationJar(javaProj);

		FactoryPath fp = (FactoryPath)AptConfig.getFactoryPath(javaProj);
		FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(_extJar);
		fp.addEntryToHead(fc, /*isEnabled=*/ true, /*runInBatchMode=*/ true);
		AptConfig.setFactoryPath(javaProj, fp);
	}

	/**
	 * Only one batch processor is involved
	 * This test the processor environment and that it returns the correct
	 * set of declared types.
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

		// This one doesn't have annotations.
		String codeD = "package p1; public class D{}";
		env.addClass( srcRoot, "p1", "D", codeD );

		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingMarkers(new String[]{"CompletedSuccessfully"});
	}

	/**
	 * What this tests test.
	 * This makes sure the internal apt rounding occurs correctly in batch mode.
	 */
	public void testAPTRoundingInMixedMode0()
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

		// This one doesn't have annotations.
		String codeD = "package p1; public class D{}";
		env.addClass( srcRoot, "p1", "D", codeD );

		fullBuild( project.getFullPath() );
		expectingMarkers(new String[]{"CompletedSuccessfully", "Called 2 times."});

		expectingNoProblems();

		// Now run it again to verify that the classloader was successfully bounced
		fullBuild( project.getFullPath() );
		expectingMarkers(new String[]{"CompletedSuccessfully", "Called 2 times."});

		expectingNoProblems();
	}

	/*
	 * What this test tests.
	 * There should be a total of 3 rounds.
	 * -The first round starts because of the "BatchGen" annotations.
	 *  This round creates the gen.Class0 type
	 * -The second round starts because of a batch processor being dispatched in a previous round
	 * and a new type is generated.
	 *  This round creates the gen.Class1 type
	 * -The third round starts for the exact same reason as round 2.
	 *  This is a no-op round.
	 */

	public void testAPTRoundingInMixedMode1()
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String codeA = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.external.annotations.batch.*;"
			+ "\n import gen.*;"
			+ "\n@BatchGen\n"
			+ "public class A {"
			+ "   Class0 clazz0;\n"
			+ "   Class1 clazz1;\n"
			+ "}\n";

		env.addClass( srcRoot, "p1", "A", codeA );

		// drop something to possibily fire off an incremental build
		String codeB = "package p1;\n"
			+ "public class B {}\n";

		env.addClass( srcRoot, "p1", "B", codeB );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingMarkers(new String[]{"Called the third time."});
	}
}
