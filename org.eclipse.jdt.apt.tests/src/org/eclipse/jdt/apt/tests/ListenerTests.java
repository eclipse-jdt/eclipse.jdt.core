/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.listener.ListenerProcessor;

public class ListenerTests extends APTTestBase
{

	public ListenerTests(String name) {
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(ListenerTests.class);
	}

	public void testListenerCalled() throws Exception
	{
		clearProcessorResult(ListenerProcessor.class);
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.listener.ListenerAnnotation;" + "\n" +
				"@ListenerAnnotation" + "\n" +
				"public class Test" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();
		checkProcessorResult(ListenerProcessor.class);
	}

}
