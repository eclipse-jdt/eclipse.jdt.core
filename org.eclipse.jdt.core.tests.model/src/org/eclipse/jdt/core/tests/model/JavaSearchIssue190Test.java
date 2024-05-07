/*******************************************************************************
 * Copyright (c) 2022 Christopher Gerking and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christopher Gerking - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

import junit.framework.Test;

public class JavaSearchIssue190Test extends AbstractJavaSearchTests {

	public String getProjectName() {
		return "JavaSearchIssue190";
	}

	public JavaSearchIssue190Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchIssue190Test.class, BYTECODE_DECLARATION_ORDER);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		createExternalFolder("TestContainer/p");

		String sourceWorkspacePath = getSourceWorkspacePath();
		IWorkspace targetWorkspace = ResourcesPlugin.getWorkspace();
		File targetWorkspaceLocation = new File(targetWorkspace.getRoot().getLocation().toOSString());

		File javaFileSrc = new File(sourceWorkspacePath, "JavaSearchMultipleProjects2/lib/p/Y.java");
		File javaFileDst = new File(targetWorkspaceLocation.getParentFile().getCanonicalFile(), "TestContainer/p/Y.java");
		copy(javaFileSrc, javaFileDst);

		File classFileSrc = new File(sourceWorkspacePath, "JavaSearchMultipleProjects2/lib/p/Y.class");
		File classFileDst = new File(targetWorkspaceLocation.getParentFile().getCanonicalFile(), "TestContainer/p/Y.class");
		copy(classFileSrc, classFileDst);

		JAVA_PROJECT = setUpJavaProject(getProjectName(), "11");

		JavaCore.setOptions(JavaCore.getDefaultOptions());
	}

	@Override
	IJavaSearchScope getJavaSearchScope() {
		IJavaElement[] elements = new IJavaElement[] { getJavaProject(getProjectName()) };
		return SearchEngine.createJavaSearchScope(elements, true);
	}

	public void testIssue190() throws CoreException, IOException {
		List<String> searchResults = new ArrayList<>();

		SearchRequestor searchRequestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				Object element = match.getElement();

				if (element instanceof IType) {
					String fqn = ((IType) element).getFullyQualifiedName();
					searchResults.add(fqn);
				}
			}
		};

		search("Y", IJavaSearchConstants.CLASS,
				IJavaSearchConstants.DECLARATIONS, getJavaSearchScope(), searchRequestor);

		assertTrue("Searched class not found in external classpath container", searchResults.contains("p.Y"));
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject(getProjectName());
		deleteExternalResource("TestContainer");
		super.tearDownSuite();
		JAVA_PROJECT = null;
	}
}
