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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

// The size of JavaSearchBugsTests.java is very big, Hence continuing here.
public class JavaSearchBugsTests2 extends AbstractJavaSearchTests {

	public JavaSearchBugsTests2(String name) {
		super(name);
		this.endChar = "";
	}
	
	static {
		//TESTS_NAMES = new String[] {"testBug378390"};
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
	
	/**
	 * @bug 123836: [1.5][search] for references to overriding method with bound type variable is not polymorphic
	 * @test Search for references to an overridden method with bound variables should yield.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=123836"
	 */
	public void testBug123836a() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Sub.java", 
					"abstract class Sup<C> {\n" +
					"    protected void m(C classifier) {}\n"+
					"    public void use(C owner) { m (owner); }\n" +
					"}\n" +
					"public class Sub extends Sup<String>{\n" +
					"    @Override\n"+
					"    protected void m(String classifier) {}\n"+
					"}\n");
			IType type = getCompilationUnit("/P/Sub.java").getType("Sub");
			IMethod method = type.getMethod("m", new String[]{"QString;"});
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Sub.java void Sup.use(C) [m (owner)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// Search for a non-overriden method with same name as which could have been overriden should
	// not have results
	public void testBug123836b() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Sub.java", 
					"abstract class Sup<C> {\n" +
					"    protected void m(C classifier) {}\n"+
					"    public void use(C owner) { m (owner); }\n" +
					"}\n" +
					"public class Sub extends Sup<String>{\n" +
					"    @Override\n"+
					"    protected void m(String classifier) {}\n"+
					"    protected void m(Sub classifier) {}\n"+
					"}\n" );
			// search
			IType type = getCompilationUnit("/P/Sub.java").getType("Sub");
			IMethod method = type.getMethod("m", new String[]{"QSub;"});
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("");
			
		} finally {
			deleteProject(project);
		}
	}
	// another variant of the testcase
	public void testBug123836c() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"    void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"        prop.compute(null);\n"+
				"        p2.compute(null);\n"+
				"    }\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"    public abstract void compute(E e);\n"+
				"}\n"+
				"class StringProperty extends Property<String> {\n"+
				"    @Override public void compute(String e) {\n"+
				"        System.out.println(e);\n"+
				"    }");
			IType type = getCompilationUnit("/P/Test.java").getType("StringProperty");
			IMethod method = type.getMethod("compute", new String[]{"QString;"});
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// Test inner class
	public void testBug123836d() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"    void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"        prop.compute(null);\n"+
				"        p2.compute(null);\n"+
				"    }\n"+
				"	class StringProperty extends Property<String> {\n"+
				"    @Override public void compute(String e) {\n"+
				"        System.out.println(e);\n"+
				"    }\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"    public abstract void compute(E e);\n"+
				"}");
				
			IType type = getCompilationUnit("/P/Test.java").getType("Test").getType("StringProperty");
			IMethod method = type.getMethod("compute", new String[]{"QString;"});
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// Test local class
	public void testBug123836e() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"    void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"        prop.compute(null);\n"+
				"        p2.compute(null);\n"+
				"		class StringProperty extends Property<String> {\n"+
				"   		@Override public void compute(String e) {\n"+
				"        		System.out.println(e);\n"+
				"    		}\n"+
				"		}\n"+
				"    }\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"    public abstract void compute(E e);\n"+
				"}");
			IMethod method = selectMethod(getCompilationUnit("/P/Test.java"), "compute", 3);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// test inner class
	public void testBug123836f() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"    void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"        prop.compute(null);\n"+
				"        p2.compute(null);\n"+
				"		 new Property<String>() {\n"+
				"   		@Override public void compute(String e) {\n"+
				"        		System.out.println(e);\n"+
				"    		}\n"+
				"		};\n"+
				"    }\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"    public abstract void compute(E e);\n"+
				"}");
			IMethod method = selectMethod(getCompilationUnit("/P/Test.java"), "compute", 3);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// test in initializer block
	public void testBug123836g() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"	{\n" +
				"		new Property<String>() {\n" +
				"			@Override public void compute(String e) {}\n" +
				"		};\n"+
				"	 }\n"+
				"	void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"		prop.compute(null);\n"+
				"		p2.compute(null);\n"+
				"	}\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"	public abstract void compute(E e);\n"+
				"}");
			IMethod method = selectMethod(getCompilationUnit("/P/Test.java"), "compute", 1);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// test in static initializer
	public void testBug123836h() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"	static {\n" +
				"		new Property<String>() {\n" +
				"			@Override public void compute(String e) {}\n" +
				"		};\n"+
				"	 }\n"+
				"	void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"		prop.compute(null);\n"+
				"		p2.compute(null);\n"+
				"	}\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"	public abstract void compute(E e);\n"+
				"}");
			IMethod method = selectMethod(getCompilationUnit("/P/Test.java"), "compute", 1);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// test in static initializer
	public void testBug123836i() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"	Property <?>p = new Property<String>() {\n" +
				"			@Override public void compute(String e) {}\n" +
				"		};\n"+
				"	void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"		prop.compute(null);\n"+
				"		p2.compute(null);\n"+
				"	}\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"	public abstract void compute(E e);\n"+
				"}");
			IMethod method = selectMethod(getCompilationUnit("/P/Test.java"), "compute", 1);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	public void testBug123836j() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Test.java", 
				"class Test {\n"+
				"    void calc(Property prop, Property<? extends Serializable> p2) {\n"+
				"        prop.compute(null);\n"+
				"        p2.compute(null);\n"+
				"    }\n"+
				"}\n"+
				"abstract class Property<E> {\n"+
				"    public abstract void compute(E e);\n"+
				"}\n"+
				"class StringProperty extends Property<String> {\n"+
				"	@Override public void compute(String e) {\n"+
				"		 new Property<String>() {\n"+
				"			@Override public void compute(String e) {\n"+
				"				new Property<String>() {\n"+
				"					@Override public void compute(String e) {}\n"+
				"				};\n"+
				"			}\n"+
				"		};\n"+
				"	}\n"+
				"}");
			IMethod method = selectMethod(getCompilationUnit("/P/Test.java"), "compute", 6);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH\n" + 
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] EXACT_MATCH");
		} finally {
			deleteProject(project);
		}
	}
	// test search of name
	public void _testBug123836g1() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Sub.java", 
					"abstract class Sup<C> {\n" +
					"    protected void m(C classifier) {}\n"+
					"    public void use(C owner) { m (owner); }\n" +
					"}\n" +
					"public class Sub extends Sup<String>{\n" +
					"    @Override\n"+
					"    protected void m(String classifier) {}\n"+
					"    protected void m(Sub classifier) {}\n"+
					"}\n" );
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("Sub.m(String)", METHOD, REFERENCES, EXACT_RULE);
			search(pattern, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Sub.java void Sup.use(C) [m (owner)] EXACT_MATCH");
			
		} finally {
			deleteProject(project);
		}
	}
	// test search of name (negative)
	public void _testBug123836h1() throws CoreException {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");	
			createFile("/P/Sub.java", 
					"abstract class Sup<C> {\n" +
					"    protected void m(C classifier) {}\n"+
					"    public void use(C owner) { m (owner); }\n" +
					"}\n" +
					"public class Sub extends Sup<String>{\n" +
					"    @Override\n"+
					"    protected void m(String classifier) {}\n"+
					"    protected void m(Sub classifier) {}\n"+
					"}\n" );
			// search
			SearchPattern pattern = SearchPattern.createPattern("Sub.m(Sub)", METHOD, REFERENCES, EXACT_RULE);
			search(pattern, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("");
			
		} finally {
			deleteProject(project);
		}
	}

	/**
	 * @bug 342393: Anonymous class' occurrence count is incorrect when two methods in a class have the same name.
	 * @test Search for Enumerators with anonymous types
	 *
	 * @throws CoreException
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=342393"
	 */
	public void testBug342393() throws CoreException {
		try {
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "bin", "1.5");
			String content = "package b342393;\n" + "class Generic {\n"
					+ "enum A {\n" + "ONE {\n" + "A getSquare() {\n"
					+ "return ONE;\n" + "}\n" + "},\n" + "TWO {\n"
					+ "A getSquare() {\n" + "return TWO;\n" + "}\n" + "};\n"
					+ "abstract A getSquare();\n" + "}\n" + "}";
			createFolder("/P/b342393");
			createFile("/P/b342393/Generic.java", content);
			IJavaSearchScope scope = SearchEngine. createJavaSearchScope(
					new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			search("getSquare", METHOD, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
			assertSearchResults("b342393/Generic.java A b342393.Generic$A.ONE:<anonymous>#1.getSquare() [getSquare] EXACT_MATCH\n" + 
					"b342393/Generic.java A b342393.Generic$A.TWO:<anonymous>#1.getSquare() [getSquare] EXACT_MATCH\n" + 
					"b342393/Generic.java A b342393.Generic$A.getSquare() [getSquare] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	
	/**
	 * @bug 376673: DBCS4.2 Can not rename the class names when DBCS (Surrogate e.g. U+20B9F) is in it 
	 * @test Search for DBCS type should report the match
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=376673"
	 */
	public void testBug376673a() throws CoreException {
		try {
			if ("macosx".equals(System.getProperty("osgi.os"))) {
				System.out.println("testBug376673* may fail on macosx");
				return;
			}
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL17_LIB"}, "bin", "1.7");
			String content = "package pkg;\n" + 
					"class \uD842\uDF9F1 {}\n";
			createFolder("/P/pkg");
			try {
				IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, "UTF-8");
				file.setCharset("UTF-8", null);
			} catch (UnsupportedEncodingException e) {
				System.out.println("unsupported encoding");
			}
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine. createJavaSearchScope(
					new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			search("\uD842\uDF9F1", TYPE, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
			assertSearchResults("pkg/\uD842\uDF9F1.java pkg.\uD842\uDF9F1 [\uD842\uDF9F1] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	// Search for DBCS method should report the match
	public void testBug376673b() throws CoreException {
		try {
			if ("macosx".equals(System.getProperty("osgi.os"))) {
				return;
			}
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL17_LIB"}, "bin", "1.7");
			String content = "package pkg;\n" + 
					"class \uD842\uDF9F1 {" +
					"	public void \uD842\uDF9Fm() {}\n" +
					"}\n";
			createFolder("/P/pkg");
			try {
				IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, "UTF-8");
				file.setCharset("UTF-8", null);
			} catch (UnsupportedEncodingException e) {
				System.out.println("unsupported encoding");
			}
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine. createJavaSearchScope(
					new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			search("\uD842\uDF9Fm", METHOD, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
			assertSearchResults("pkg/\uD842\uDF9F1.java void pkg.\uD842\uDF9F1.\uD842\uDF9Fm() [\uD842\uDF9Fm] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	// Search for DBCS constructor should report the match
	public void testBug376673c() throws CoreException {
		try {
			if ("macosx".equals(System.getProperty("osgi.os"))) {
				return;
			}
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL17_LIB"}, "bin", "1.7");
			String content = "package pkg;\n" + 
					"class \uD842\uDF9F1 {" +
					"	public \uD842\uDF9F1() {}\n" +
					"}\n";
			createFolder("/P/pkg");
			try {
				IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, "UTF-8");
				file.setCharset("UTF-8", null);
			} catch (UnsupportedEncodingException e) {
				System.out.println("unsupported encoding");
			}
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine. createJavaSearchScope(
					new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			search("\uD842\uDF9F1", CONSTRUCTOR, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
			assertSearchResults("pkg/\uD842\uDF9F1.java pkg.\uD842\uDF9F1() [\uD842\uDF9F1] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	// Search for DBCS field should report the match
	public void testBug376673d() throws CoreException {
		try {
			if ("macosx".equals(System.getProperty("osgi.os"))) {
				return;
			}
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL17_LIB"}, "bin", "1.7");
			String content = "package pkg;\n" + 
					"class \uD842\uDF9F1 {" +
					"	public int \uD842\uDF9Ff;\n" +
					"}\n";
			createFolder("/P/pkg");
			try {
				IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, "UTF-8");
				file.setCharset("UTF-8", null);
			} catch (UnsupportedEncodingException e) {
				System.out.println("unsupported encoding");
			}
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine. createJavaSearchScope(
					new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			search("\uD842\uDF9Ff", FIELD, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
			assertSearchResults("pkg/\uD842\uDF9F1.java pkg.\uD842\uDF9F1.\uD842\uDF9Ff [\uD842\uDF9Ff] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	// Search for DBCS package name from a jar also should report the match
	public void testBug376673e() throws CoreException, IOException {
	try {
		if ("macosx".equals(System.getProperty("osgi.os"))) {
			return;
		}
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib376673.jar", "JCL17_LIB" }, "", "1.7");
		
		org.eclipse.jdt.core.tests.util.Util.createJar(
						new String[] {
						"p\uD842\uDF9F/i\uD842\uDF9F/Test.java",
						"package p\uD842\uDF9F.i\uD842\uDF9F;\n" +
						"public class Test{}\n" },
						p.getProject().getLocation().append("lib376673.jar").toOSString(),
						"1.7");
		refresh(p);
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
		search("Test", TYPE, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("lib376673.jar p\uD842\uDF9F.i\uD842\uDF9F.Test [No source] EXACT_MATCH");
	} finally {
		deleteProject("P");
	}
	}
	/**
	 * @bug 357547: [search] Search for method references is returning methods as overridden even if the superclass's method is only package-visible
	 * @test Search for a non-overridden method because of package visibility should not be found
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=357547"
	 */
	public void testBug357547a() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P");
			createFolder("/P/p1");
			createFile("/P/p1/B.java",
					"package p1;\n" +
					"import p2.*;\n" +
					"public class B extends A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			IType type = getCompilationUnit("/P/p1/B.java").getType("B");
			IMethod method = type.getMethod("k", new String[]{});
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Should not get any results", "", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	
	// search for the method name should also not return matches if not-overridden because of package-visible
	public void testBug357547b() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P");
			createFolder("/P/p1");
			createFile("/P/p1/B.java",
					"package p1;\n" +
					"import p2.*;\n" +
					"public class B extends A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("p*.B.k()", METHOD, REFERENCES , 0 );
			search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }), this.resultCollector);
			assertSearchResults("Should not get any results", "", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	
	// search for the method name should return the match if same package 
	public void testBug357547c() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P");
			createFolder("/P/p2");
			createFile("/P/p2/B.java",
					"package p2;\n" +
					"public class B extends A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("B.k()", METHOD, REFERENCES, EXACT_RULE);
			search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }), this.resultCollector);
			assertSearchResults("Wrong results", "p2/A.java long p2.A.m() [k()] EXACT_MATCH", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug357547d() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P");
			createFolder("/P/p1");
			createFile("/P/p1/B.java",
					"package p1;\n" +
					"import p2.*;\n" +
					"public class B extends A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A{ \n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			createFile("/P/p2/B.java",
					"package p2;\n" +
					"public class B {\n" +
					"}\n");
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("B.k()", METHOD, REFERENCES, EXACT_RULE);
			search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }), this.resultCollector);
			assertSearchResults("Should not get any results", "", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	// search for the method name should also not return matches if not-overridden because of package-visible
	// even if they are in jars
	public void testBug357547e() throws CoreException, IOException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] { "/P/lib357547.jar", "JCL15_LIB" }, "", "1.5");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p2/A.java",
					"package p2;\n" + 
					"public class A{}\n" }, 
					project.getProject().getLocation().append("libStuff.jar").toOSString(), "1.5");
			
			org.eclipse.jdt.core.tests.util.Util.createJar(
					new String[] {
						"p1/B.java",
						"package p1;\n"+
						"import p2.*;\n"+
						"public class B extends A {\n" +
						"long k(){\n" +
						"return 0;\n" +
						"}\n" + 
						"}\n"},
					null,
					project.getProject().getLocation().append("lib357547.jar").toOSString(),
					new String[] { project.getProject().getLocation().append("libStuff.jar").toOSString() },
					"1.5");
			refresh(project);
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("B.k()", METHOD, REFERENCES, EXACT_RULE);
			search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES), this.resultCollector);
			assertSearchResults("Wrong results", "", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	// search for the method name should also not return matches if not-overridden because of package-visible
	// even if they are in jars
	public void testBug357547f() throws CoreException, IOException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] { "/P/lib357547.jar", "JCL15_LIB" }, "", "1.5");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p2/A.java",
					"package p2;\n" + 
					"public class A{}\n" }, 
					project.getProject().getLocation().append("libStuff.jar").toOSString(), "1.5");
			
			org.eclipse.jdt.core.tests.util.Util.createJar(
					new String[] {
						"p2/B.java",
						"package p2;\n" +
						"import p2.*;\n" +
						"public class B extends A {\n" +
						"long k(){\n" +
						"return 0;\n" +
						"}\n" + 
						"}\n"},
					null,
					project.getProject().getLocation().append("lib357547.jar").toOSString(),
					new String[] { project.getProject().getLocation().append("libStuff.jar").toOSString() },
					"1.5");
			refresh(project);
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("B.k()", METHOD, REFERENCES, EXACT_RULE);
			search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES), this.resultCollector);
			assertSearchResults("Wrong results", "p2/A.java long p2.A.m() [k()] EXACT_MATCH", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	// search for declarations also should take care of default
	public void testBug357547g() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P");
			createFolder("/P/p1");
			createFile("/P/p1/B.java",
					"package p1;\n" +
					"import p2.*;\n" +
					"public class B extends A {\n" +
					"long k(int a){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFile("/P/p1/C.java",
					"package p1;\n" +
					"public class C extends B {\n" +
					"long k(int a){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A{ \n" +
					"long k(int a){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k(0);\n" +
			  		"}\n"+
					"}\n");
			createFile("/P/p2/B.java",
					"package p2;\n" +
					"public class B {\n" +
					"}\n");
			waitUntilIndexesReady();
			// search
			SearchPattern pattern = SearchPattern.createPattern("A.k(int)", METHOD, DECLARATIONS, EXACT_RULE);
			search(pattern, SearchEngine.createJavaSearchScope(new IJavaElement[] { project }), this.resultCollector);
			assertSearchResults("Wrong results", "p2/A.java long p2.A.k(int) [k] EXACT_MATCH", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug378390() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P");
			createFolder("/P/p1");
			createFile("/P/p1/B.java",
					"package p1;\n" +
					"import p2.*;\n" +
					"public class B extends A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
					"}\n");
			createFolder("/P/p2");
			createFile("/P/p2/A.java",
					"package p2;\n" +
					"public class A {\n" +
					"class B extends A {\n" +
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +	
			  		"}\n" +	
					"long k(){\n" +
					"return 0;\n" +
			  		"}\n" +
			  		"public long m(){\n"+
			  		"return new A().k();\n" +
			  		"}\n"+
					"}\n");
			IType type = getCompilationUnit("/P/p2/A.java").getType("A");
			type = type.getTypes()[0];
			IMethod method = type.getMethod("k", new String[]{});
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("Wrong results", "p2/A.java long p2.A.m() [k()] EXACT_MATCH", this.resultCollector);
		} finally {
			deleteProject(project);
		}
	}
	/**
	 * @bug 375971: [search] Not finding method references with generics
	 * @test TODO
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=375971"
	 */
	public void testBug375971a() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI <K, V>{\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI<K, V>{\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB extends ClassA<String, String, String>{\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/InterfaceI.java").getType("InterfaceI");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething() [addListener()] ERASURE_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971b() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI <K, V>{\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> {\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB extends ClassA<String, String, String> implements InterfaceI<String, String>{\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/InterfaceI.java").getType("InterfaceI");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething() [addListener()] ERASURE_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971c() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI <K, V>{\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> {\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB extends ClassA<String, String, String> implements InterfaceI<String, String>{\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/ClassA.java").getType("ClassA");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething() [addListener()] ERASURE_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971d() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
					"public interface InterfaceI <K, V>{\n"+
					" public void addListener();\n"+
					"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI<K, V>{\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB {\n"+
						" public void doSomething(ClassA a) {" +
						"   a.addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/InterfaceI.java").getType("InterfaceI");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething(ClassA) [addListener()] ERASURE_RAW_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971e() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI <K, V>{\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI<K, V> {\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB implements InterfaceI<String, String> {\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			createFile("/P/ClassC.java",
					"public class ClassC extends ClassA<Integer, String, String> {\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			ICompilationUnit unit = getCompilationUnit("/P/ClassB.java");
			IMethod method = selectMethod(unit, "addListener");
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething() [addListener()] EXACT_MATCH\n" + 
								"ClassC.java void ClassC.doSomething() [addListener()] ERASURE_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971f() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI {\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI{\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB extends ClassA<String, String, String>{\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/InterfaceI.java").getType("InterfaceI");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething() [addListener()] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971g() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI {\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI{\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB<K, V, B> extends ClassA<K, V, B>{\n"+
						" public void doSomething() {" +
						"   addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/InterfaceI.java").getType("InterfaceI");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething() [addListener()] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971h() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI<K,V> {\n"+
				" public void addListener();\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI<K, V>{\n"+
					" public void addListener() {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB<K, V, B> extends ClassA<K, V, B>{\n"+
						" public void doSomething(InterfaceI<String, String> i) {" +
						"   i.addListener();\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/ClassA.java").getType("ClassA");
			IMethod method = type.getMethod("addListener", new String[]{});
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething(InterfaceI<String,String>) [addListener()] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug375971i() throws CoreException {
		try {
			createJavaProject("P");
			createFile("/P/InterfaceI.java",
				"public interface InterfaceI<K,V> {\n"+
				" public void addListener(K k);\n"+
				"}\n");
			createFile("/P/ClassA.java",
				"public class ClassA <K, V, B> implements InterfaceI<K, V>{\n"+
					"public void addListener(K k) {\n" +
					"}\n" +
					"}\n");
			createFile("/P/ClassB.java",
					"public class ClassB<K, V, B> extends ClassA<K, V, B>{\n"+
						" public void doSomething(K k) {" +
						"   addListener(k);\n"+
						"}\n" +
						"}\n");
			waitUntilIndexesReady();
			// search
			IType type = getCompilationUnit("/P/InterfaceI.java").getType("InterfaceI");
			IMethod method = type.getMethods()[0];
			this.resultCollector.showRule();
			search(method, REFERENCES, ERASURE_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("ClassB.java void ClassB.doSomething(K) [addListener(k)] ERASURE_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug383315a() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "JCL15_LIB" }, "", "1.5");
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search("java.lang.Object.hashCode()", METHOD, ALL_OCCURRENCES, scope, this.resultCollector);

			assertSearchResults(""); // an NPE was thrown without the fix
		} finally {
			deleteProject("P");
		}
	}
	public void testBug383315b() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P");
			createFolder("/P/pkg");
			createFile("/P/pkg/A.java",
					"package pkg;\n"+
					"public class A {\n"+
					"	void a() {\n"+
					"	}\n"+
					"}");
			createFile("/P/pkg/B.java",
					"package pkg;\n"+
					"public class B extends A {\n"+
					"	void a() {\n"+
					"	}\n"+
					"}");
			createFile("/P/pkg/C.java",
					"package pkg;\n"+
					"public class C extends B {\n"+
					"	void a() {\n"+
					"	}\n"+
					"}");
			createFile("/P/pkg/D.java",
					"package pkg;\n"+
					"public class D extends C {\n"+
					"	void a() {\n"+
					"	}\n"+
					"	void d() {\n"+
					"		new A().a();\n"+
					"	}\n"+
					"}");
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search("C.a()", METHOD, REFERENCES, scope, this.resultCollector);

			assertSearchResults("pkg/D.java void pkg.D.d() [a()] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
}
