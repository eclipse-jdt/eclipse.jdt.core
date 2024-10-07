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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

public class JavaSearchSuperAfterStatementTests extends JavaSearchTests {
	public JavaSearchSuperAfterStatementTests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchSuperAfterStatementTests.class, BYTECODE_DECLARATION_ORDER);
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
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "23");
		JAVA_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		super.setUpSuite();
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

	public void test_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		String code = """
				class Y {
					public int v;
					Y(int v) {
						this.v = v;
					}
				}
				@SuppressWarnings("preview")
				public class X extends Y {
				    public X(int value) {
				        if (value <= 0)
				            throw new IllegalArgumentException("non-positive value");
				        super(value);
				        this.v = value;
				    }
				    public static void main(String[] args) {
						X x = new X(100);
						System.out.println(x.v);
					}
				}
				""";
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", code);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			search("Y", CONSTRUCTOR, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java X(int) [super(value);] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}


	}
	public void test_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		String code = """
				class Y {
					public int[] vArr;
					private F f1;
					private F f2;
					Y(F f1, F f2) {
						this.f1 = f1;
						this.f2 = f2;
					}
				}
				class F {}
				public class X extends Y {
					public int i;
					@SuppressWarnings("preview")
					public X(int i) {
				        var f = new F();
						super(f, f);
				        this.i = i;
				    }
				    public static void main(String[] args) {
						X x = new X(100);
						System.out.println(x.i);
						X x2 = new X(1);
						System.out.println(x2.i);
					}
				}
				""";
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", code);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			search("Y", CONSTRUCTOR, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java X(int) [super(f, f);] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void test_003() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		String code = """
				class Y {
					Y() {
					}
				}
				public class X extends Y {
					public int i;
					@SuppressWarnings("preview")
					public X(int i) {
					if(i >0)
						i = 10;
					super();
			        this.i = i;
				    }
				}
				""";
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java", code);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			search("Y", CONSTRUCTOR, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java X(int) [super();] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
}