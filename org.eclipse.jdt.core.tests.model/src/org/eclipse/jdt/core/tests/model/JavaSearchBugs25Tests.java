/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

public class JavaSearchBugs25Tests extends AbstractJavaSearchTests {

	public JavaSearchBugs25Tests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs25Tests.class, BYTECODE_DECLARATION_ORDER);
	}

	class TestCollector extends JavaSearchResultCollector {
		@Override
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
		}
	}

	@Override
	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
	}
	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScope();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}
	@Override
	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		if (this.wcOwner == null) {
			this.wcOwner = new WorkingCopyOwner() {};
		}
		return getWorkingCopy(path, source, this.wcOwner);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs25", "17");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs25");
		super.tearDownSuite();
	}
	@Override
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}

	public void testRecordOverridenMethod_plain() throws CoreException {
		waitUntilIndexesReady();
		JAVA_PROJECT.setOption(JavaCore.COMPILER_RELEASE, JavaCore.DISABLED);
		IType namedType = JAVA_PROJECT.findType("test1.Person");
		assertTrue("class should exist in the model", namedType.exists());
		IMethod toString = namedType.getMethod("toString", new String[0]);
		assertNotNull("method was not found", toString);
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(getAllScopeElements(JAVA_PROJECT, false), false);
		SearchPattern pattern = SearchPattern.createPattern(toString, IJavaSearchConstants.REFERENCES,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
		search(pattern, scope, this.resultCollector);
	}

	public void testRecordOverridenMethod_release() throws CoreException {
		waitUntilIndexesReady();
		JAVA_PROJECT.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);
		IType namedType = JAVA_PROJECT.findType("test1.Person");
		assertTrue("class should exist in the model", namedType.exists());
		IMethod toString = namedType.getMethod("toString", new String[0]);
		assertNotNull("method was not found", toString);
		IJavaSearchScope scope= SearchEngine.createJavaSearchScope(getAllScopeElements(JAVA_PROJECT, false), false);
		SearchPattern pattern = SearchPattern.createPattern(toString, IJavaSearchConstants.REFERENCES,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
		search(pattern, scope, this.resultCollector);
	}

	private static IPackageFragmentRoot[] getAllScopeElements(IJavaProject project, boolean onlySourceRoots) throws JavaModelException {
		List<IPackageFragmentRoot> result= new ArrayList<>();
		for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
			if (!onlySourceRoots || root.getKind() == IPackageFragmentRoot.K_SOURCE)
				result.add(root);
		}
		return result.toArray(new IPackageFragmentRoot[result.size()]);
	}
}
