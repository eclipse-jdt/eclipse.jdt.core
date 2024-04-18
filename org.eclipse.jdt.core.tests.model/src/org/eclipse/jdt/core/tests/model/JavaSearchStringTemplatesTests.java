/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

public class JavaSearchStringTemplatesTests extends JavaSearchTests {
	public JavaSearchStringTemplatesTests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchStringTemplatesTests.class, BYTECODE_DECLARATION_ORDER);
	}
	class TestCollector extends JavaSearchResultCollector {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
		}
	}
	class ReferenceCollector extends JavaSearchResultCollector {
		protected void writeLine() throws CoreException {
			super.writeLine();
			ReferenceMatch refMatch = (ReferenceMatch) this.match;
			IJavaElement localElement = refMatch.getLocalElement();
			if (localElement != null) {
				this.line.append("+[");
				if (localElement.getElementType() == IJavaElement.ANNOTATION) {
					this.line.append('@');
					this.line.append(localElement.getElementName());
					this.line.append(" on ");
					this.line.append(localElement.getParent().getElementName());
				} else {
					this.line.append(localElement.getElementName());
				}
				this.line.append(']');
			}
		}
	}

	class TypeReferenceCollector extends ReferenceCollector {
		protected void writeLine() throws CoreException {
			super.writeLine();
			TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
			IJavaElement[] others = typeRefMatch.getOtherElements();
			int length = others==null ? 0 : others.length;
			if (length > 0) {
				this.line.append("+[");
				for (int i=0; i<length; i++) {
					IJavaElement other = others[i];
					if (i>0) this.line.append(',');
					if (other.getElementType() == IJavaElement.ANNOTATION) {
						this.line.append('@');
						this.line.append(other.getElementName());
						this.line.append(" on ");
						this.line.append(other.getParent().getElementName());
					} else {
						this.line.append(other.getElementName());
					}
				}
				this.line.append(']');
			}
		}
	}

	protected IJavaProject setUpJavaProject(final String projectName, String compliance, boolean useFullJCL) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		IJavaProject setUpJavaProject = super.setUpJavaProject(projectName, compliance, useFullJCL);
		return setUpJavaProject;
	}

	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
	}

	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScope();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}

	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		if (this.wcOwner == null) {
			this.wcOwner = new WorkingCopyOwner() {};
		}
		return getWorkingCopy(path, source, this.wcOwner);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "22");
	}

	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs");
		super.tearDownSuite();
	}

	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}

	public void _test_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		String code = """
				public class X {
					void foo(Object o) {
						String name = "Eclipse";
						String os = "OS";
						String xyz = "xyz";
						String s = STR. \"Hello \\{name} \\{os} is your OS. \\{xyz} is xyz";
						String s1 = STR. \"Hello \\{name} \\{os} is your OS. \\{xyz} is xyz";
					}
				}
				""";
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", code);

			search("STR", FIELD, REFERENCES, EXACT_RULE);
			String SearchResults = """
					src/X.java void X.foo(Object) [STR] EXACT_MATCH
					src/X.java void X.foo(Object) [STR] EXACT_MATCH""";
			assertSearchResults(SearchResults);
	}

	public void _test_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		String code = """
				public class X {
					void fooxx(Object o) {
						String name = "Eclipse";
						String os = "OS";
						int xyz = 1;
						String s = STR. "Hello \\{name} \\{os} is your OS. \\{xyz} is xyz";
					}
				}
				""";
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", code);

			search("fooxx", METHOD, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.fooxx(Object) [fooxx] EXACT_MATCH");
	}

	public void test_003() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		String code = """
				public class X {
					String sam = "sasm";
					@SuppressWarnings("preview")
					void foo() {
						String name = "Eclipse";
						String oss = "OS";
						int xyz = 1;
						this.sam = "xxx";
						oss = name;
						String eclipse = this.sam;
						String s = STR. "Hello \\{name} \\{oss} is your OS. \\{xyz} is xyz. sam \\{this.sam}";
					}
				}
				""";
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", code);

			search("sam", FIELD, REFERENCES, EXACT_RULE);
			assertSearchResults(
					"""
					src/X.java void X.foo() [sam] EXACT_MATCH
					src/X.java void X.foo() [sam] EXACT_MATCH
					src/X.java void X.foo() [sam] EXACT_MATCH"""
					);
	}
}
