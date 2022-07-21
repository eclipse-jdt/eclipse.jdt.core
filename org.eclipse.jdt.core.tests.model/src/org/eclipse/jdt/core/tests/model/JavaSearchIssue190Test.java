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

import static org.junit.Assume.assumeNotNull;

import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
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
		return buildModelTestSuite(JavaSearchIssue190Test.class,
				BYTECODE_DECLARATION_ORDER);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject(getProjectName(), "11");
		// Prevent ComparisonFailure on tearDown which checks that "Workspace
		// options should be back to their default":
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
		IJavaElement[] elements = new IJavaElement[]{
				getJavaProject(getProjectName())};
		return SearchEngine.createJavaSearchScope(elements, true);
	}

	public void testIssue190() throws CoreException {
		Documented documented = Nullable.class.getAnnotation(Documented.class);
		assumeNotNull(documented);

		IProject project = getJavaProject(getProjectName()).getProject();
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		IMarker[] problems = project.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);

		StringBuilder builder = new StringBuilder();

		for (IMarker problem : problems) {
			int severity = problem.getAttribute(IMarker.SEVERITY,
					IMarker.SEVERITY_INFO);
			String message = problem.getAttribute(IMarker.MESSAGE).toString();

			if (severity != IMarker.SEVERITY_ERROR) {
				builder.append(message);
				builder.append("\n");
			}
		}

		assertTrue(builder.toString(), builder.length() == 0);

		List<String> annotatedClasses = new ArrayList<String>();

		SearchRequestor searchRequestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				Object element = match.getElement();

				if (element instanceof IType) {
					String fqn = ((IType) element).getFullyQualifiedName();
					annotatedClasses.add(fqn);
				}
			}
		};

		search(Documented.class.getCanonicalName(),
				IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				getJavaSearchScope(), searchRequestor);

		assertTrue("Annotation not found",
				annotatedClasses.contains(Nullable.class.getName()));
	}
}
