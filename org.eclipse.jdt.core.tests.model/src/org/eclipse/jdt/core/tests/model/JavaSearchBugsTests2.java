/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;

import junit.framework.Test;

// The size of JavaSearchBugsTests.java is very big, Hence continuing here.
public class JavaSearchBugsTests2 extends AbstractJavaSearchTests {

	public JavaSearchBugsTests2(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugsTests2.class);
	}

	class TestCollector extends JavaSearchResultCollector {
		public List matches = new ArrayList();

		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
			this.matches.add(searchMatch);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}

	/**
	 * Test that missing types in the class shouldn't impact the search of a type in an inner class
	 */
	public void testBug362633() throws CoreException, IOException {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib325418.jar", "JCL15_LIB" }, "", "1.5");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p325418M/Missing.java",
					"package p325418M;\n" + 
					"public class Missing{}\n" }, 
					p.getProject().getLocation().append("lib325418M.jar").toOSString(), "1.5");
			
			org.eclipse.jdt.core.tests.util.Util.createJar(
							new String[] {
							"p325418/Test.java",
							"package p325418;\n" +
							"public class Test{\n" +
							"	public void foo(p325418M.Missing a) {}\n" +
							"	public <T> T foo(int a) {\n" +
							 "		return new Inner<T>() {T  run() {  return null;  }}.run();\n" +
							 "	}\n" + "}\n",
							"p325418/Inner.java",
							"package p325418;\n" +
							"abstract class Inner <T> {\n" +
							"	 abstract T run();\n" + "}\n" },
							null,
							p.getProject().getLocation().append("lib325418.jar").toOSString(),
							new String[] { p.getProject().getLocation().append("lib325418M.jar").toOSString() },
							"1.5");
			refresh(p);
			int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES;
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
			search("Inner.run()", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
			assertSearchResults(
					"Unexpected search results!",
					"lib325418.jar T p325418.Inner.run() [No source] EXACT_MATCH\n" +
					"lib325418.jar T p325418.<anonymous>.run() [No source] EXACT_MATCH",
					this.resultCollector);
		} finally {
			deleteProject("P");
		}
	}
}
