/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.util.ScannerUtil;

public class ScannerTests extends APTTestBase {

	public ScannerTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( ScannerTests.class );
	}

	public void testHasAnnotation() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.noop.NoOpAnnotation;" + "\n" +
				"@NoOpAnnotation" + "\n" +
				"public class Has" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Has", code);

		IFile file = project.getFile(P + "src" + P + "test" + P + "Has.java");

		assertTrue("Expected annotation instance", ScannerUtil.hasAnnotationInstance(file));
		assertTrue("Expected annotation instance", ScannerUtil.hasAnnotationInstance(code.toCharArray()));
	}

	public void testHasNoAnnotation() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"public class No" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "No", code);

		IFile file = project.getFile(P + "src" + P + "test" + P + "No.java");

		assertFalse("Expected no annotation instance", ScannerUtil.hasAnnotationInstance(file));
		assertFalse("Expected no annotation instance", ScannerUtil.hasAnnotationInstance(code.toCharArray()));
	}

	public void testHasAnnotationInComment() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"/**" + "\n" +
				" * @author Joe Bob" + "\n" +
				" */" + "\n" +
				"public class Comments" + "\n" +
				"{" + "\n" +
				"    // @Foo" + "\n" +
				"    /* @Bar */" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Comments", code);

		IFile file = project.getFile(P + "src" + P + "test" + P + "Comments.java");

		assertFalse("Expected no annotation instance", ScannerUtil.hasAnnotationInstance(file));
		assertFalse("Expected no annotation instance", ScannerUtil.hasAnnotationInstance(code.toCharArray()));
	}


	/*
	* Test currently disabled due to Bugzilla 140232
	* https://bugs.eclipse.org/bugs/show_bug.cgi?id=140232
	*/
	public void _testHasAnnotationDeclaration() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"public @interface AnnoDecl" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "AnnoDecl", code);

		IFile file = project.getFile(P + "src" + P + "test" + P + "AnnoDecl.java");

		assertFalse("Expected no annotation instance", ScannerUtil.hasAnnotationInstance(file));
		assertFalse("Expected no annotation instance", ScannerUtil.hasAnnotationInstance(code.toCharArray()));
	}

	private final String P = File.separator;

}
