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

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
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
		//TESTS_NAMES = new String[] {"testBug123836"};
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
}
