/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

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

public class JavaSearchBugs13Tests extends AbstractJavaSearchTests {

	static {
		//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		//		TESTS_NUMBERS = new int[] { 19 };
		//		TESTS_RANGE = new int[] { 1, -1 };
		//		TESTS_NAMES = new String[] {"testBug542559_001"};
	}

	public JavaSearchBugs13Tests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs13Tests.class, BYTECODE_DECLARATION_ORDER);
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
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "13");
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

	// all preview related Java 13 switch expression tests deleted
	// enabled preview and moved to JavaSearchBugs14SwitchExpressionTests
    // Not quite: testBug549413_017 && testBug549413_018 were lingering around despite being moved and got deleted separately.

	// add non-preview stuff involving yield field and method
	public void testBug549413_019() throws CoreException {
		//old style switch case without preview search for yield field.
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"+
						" static int  yield;\n"+
						" public static int   yield() { \n"+
						"	  return 7; \n"+
						" } \n"+
						"  public static void main(String[] args) { \n"+
						"     int week = 1;	\n"+
						"    switch (week) { \n"+
						"      case 1:       \n"+
						"     	 yield = 88; \n"+
						"    	 break; \n"+
						"     case 2:  \n"+
						"   	 yield = yield();\n"+
						"   	 break; \n"+
						"    default:  \n"+
						"  	 yield = 88; \n"+
						"     break; \n" +
						" } \n" +
						" System.out.println(yield); \n"+
						"	}\n"+
						"}\n"
				);

		try {
			search("yield", FIELD, REFERENCES);
			assertSearchResults("src/X.java void X.main(String[]) [yield] EXACT_MATCH\n" +
					"src/X.java void X.main(String[]) [yield] EXACT_MATCH\n" +
					"src/X.java void X.main(String[]) [yield] EXACT_MATCH\n" +
					"src/X.java void X.main(String[]) [yield] EXACT_MATCH");
		} finally {
		}
	}

	public void testBug549413_020() throws CoreException {
		//old style switch case without preview search for yield method.
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"+
						" static int  yield;\n"+
						" public static int   yield() { \n"+
						"	  return 7; \n"+
						" } \n"+
						"  public static void main(String[] args) { \n"+
						"     int week = 1;	\n"+
						"    switch (week) { \n"+
						"      case 1:       \n"+
						"     	 yield = 88; \n"+
						"    	 break; \n"+
						"     case 2:  \n"+
						"   	 yield = yield();\n"+
						"   	 break; \n"+
						"    default:  \n"+
						"  	 yield = 88; \n"+
						"     break; \n" +
						" } \n" +
						" System.out.println(yield); \n"+
						"	}\n"+
						"}\n"
				);

		try {
			search("yield", METHOD, REFERENCES);
			assertSearchResults("src/X.java void X.main(String[]) [yield()] EXACT_MATCH");
		} finally {
		}
	}
}
