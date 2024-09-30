/*******************************************************************************
 * Copyright (c) 2021 SSI Schaefer.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SSI Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

public class JavaSearchBug565512Test extends AbstractJavaSearchTests {

	public String getProjectName() {
		return "JavaSearchBug565512lib";
	}

	public JavaSearchBug565512Test(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBug565512Test.class, BYTECODE_DECLARATION_ORDER);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject(getProjectName(), "9");
		// Prevent ComparisonFailure on tearDown which checks that "Workspace options should be back to their default":
		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject(getProjectName());
		super.tearDownSuite();
		JAVA_PROJECT = null;
	}

	@Override
	IJavaSearchScope getJavaSearchScope() {
		IJavaElement[] elements = new IJavaProject[] { getJavaProject(getProjectName()) };
		boolean excludeTestCode = false;
		int includeMask = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES
				| IJavaSearchScope.REFERENCED_PROJECTS;
		return SearchEngine.createJavaSearchScope(excludeTestCode, elements, includeMask);
	}

	/** Bug 576433 - inner Type not found in .class file. */
	public void testBug576433() throws CoreException {
		search("lib565512.Class565512.InnerClass.InnerClass2", IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, getJavaSearchScope());
		assertSearchResults("lib/bug565512.jar lib565512.Class565512$InnerClass$InnerClass2 [No source]");
	}

	/** Bug 576580 - Search does not find any TYPE IMPLEMENTORS in .class files */
	public void test576580() throws CoreException {
		search("lib565512.Class565512", IJavaSearchConstants.TYPE, IJavaSearchConstants.IMPLEMENTORS, getJavaSearchScope());
		assertSearchResults(
				// XXX those anonymous names are wrong!
				// among other things they have multiple results with same name,
				// but at least they are found:
				"lib/bug565512.jar lib565512.<anonymous> [No source]\n" + // XXX
				"lib/bug565512.jar lib565512.<anonymous> [No source]\n" + // XXX
				"lib/bug565512.jar lib565512.<anonymous> [No source]\n" + // XXX
				"lib/bug565512.jar lib565512.<anonymous> [No source]\n" + // XXX
				"lib/bug565512.jar lib565512.Class565512$ExtendedClass [No source]\n" + //
				"lib/bug565512.jar lib565512.ExtendedClass565512$ExtendedClass [No source]\n" + //
				"lib/bug565512.jar lib565512.ExtendedClass565512 [No source]" //
		);
	}
}