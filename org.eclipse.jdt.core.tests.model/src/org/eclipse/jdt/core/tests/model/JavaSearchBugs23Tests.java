/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

public class JavaSearchBugs23Tests extends AbstractJavaSearchTests {

	public JavaSearchBugs23Tests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs23Tests.class, BYTECODE_DECLARATION_ORDER);
	}

	class TestCollector extends JavaSearchResultCollector {
		@Override
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
		}
	}

	@Override
	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs23")});
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
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs23", "23");
		JAVA_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	}
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs23");
		super.tearDownSuite();
	}
	@Override
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}

	public void testModuleImport() throws CoreException, IOException {
			IModuleDescription module = JAVA_PROJECT.findModule("mod.one", this.wcOwner);
			search(module, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("""
					src/modimp/X.java [mod.one] EXACT_MATCH
					lib/mod.one.jar mod.one [No source] EXACT_MATCH""");
	}

	public void testModuleImportPatternReferences() throws CoreException, IOException {
			SearchPattern pattern = SearchPattern.createPattern("*od.*", IJavaSearchConstants.MODULE, REFERENCES, SearchPattern.R_EXACT_MATCH);
			search(pattern, getJavaSearchScope(), this.resultCollector);
			assertSearchResults("""
					src/modimp/X.java [mod.one] EXACT_MATCH""");
	}

	public void testModuleImportPrefixDeclaration() throws CoreException, IOException {
			SearchPattern pattern = SearchPattern.createPattern("mod", IJavaSearchConstants.MODULE, DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
			search(pattern, getJavaSearchScope(), this.resultCollector);
			assertSearchResults("""
					lib/mod.one.jar mod.one [No source] EXACT_MATCH""");
	}

}
