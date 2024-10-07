/*******************************************************************************
 * Copyright (c) 2014, 2024 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 377883 - NPE on open Call Hierarchy
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.osgi.service.environment.Constants;

// The size of JavaSearchBugsTests.java is very big, Hence continuing here.
@SuppressWarnings({"rawtypes", "unchecked"})
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

		@Override
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
			this.matches.add(searchMatch);
		}
	}

	@Override
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
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib325418.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p325418M/Missing.java",
					"package p325418M;\n" +
					"public class Missing{}\n" },
					p.getProject().getLocation().append("lib325418M.jar").toOSString(), CompilerOptions.getFirstSupportedJavaVersion());

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
							CompilerOptions.getFirstSupportedJavaVersion());
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
	 * bug123836: [1.5][search] for references to overriding method with bound type variable is not polymorphic
	 * test Search for references to an overridden method with bound variables should yield.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=123836"
	 */
	public void testBug123836a() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
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
	// Search for a non-overridden method with same name as which could have been overridden should
	// not have results
	public void testBug123836b() throws CoreException {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
	public void testBug123836c_missingImport() throws CoreException {
		// original version with missing import, will now give POTENTIAL_MATCH
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
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
								"Test.java void Test.calc(Property, Property<? extends Serializable>) [compute(null)] POTENTIAL_MATCH");
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/Test.java",
				"import java.io.Serializable;\n" +
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
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
			project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
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
	 * bug297825: [search] Rename refactoring doesn't update enclosing type
	 * test Search for references for enclosing type's subclass should return a match.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297825"
	 */
	public void testBug297825a() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/b297825");
			createFile("/P/src/b297825/_Foo.java",
					"package b297825;\n" +
					"public class _Foo {\n" +
					"	public static class Bar {\n" +
					"	}\n" +
					"}"
					);
			createFile("/P/src/b297825/Foo.java",
					"package b297825;\n" +
					"public class Foo extends _Foo {\n" +
					"}\n"
					);
			createFile("/P/src/b297825/Main.java",
					"package b297825;\n" +
					"public class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"		new Foo.Bar();\n" +
					"	}\n" +
					"}"
					);
			waitUntilIndexesReady();
			IType type = getCompilationUnit("/P/src/b297825/Foo.java").getType("Foo");
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search(type, REFERENCES, scope, this.resultCollector);

			assertSearchResults("src/b297825/Main.java void b297825.Main.main(String[]) [Foo] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	/**
	 * bug297825: [search] Rename refactoring doesn't update enclosing type
	 * test Verify there is no AIOOB when searching for references for a type.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297825"
	 */
	public void testBug297825b() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFile("/P/src/Foo.java",
					"class _Foo {\n" +
					"	public static class Bar {\n" +
					"	}\n" +
					"}" +
					"public class Foo extends _Foo {\n" +
					"	public static class FooBar {\n" +
					"	}\n" +
					"}" +
					"class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"		new Foo.Bar();\n" +
					"		new Foo.FooBar();\n" +
					"	}\n" +
					"}"
					);
			waitUntilIndexesReady();
			IType type = getCompilationUnit("/P/src/Foo.java").getType("Foo");
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search(type, REFERENCES, scope, this.resultCollector);

			assertSearchResults("src/Foo.java void Main.main(String[]) [Foo] EXACT_MATCH\n" +
					"src/Foo.java void Main.main(String[]) [Foo] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	/**
	 * bug297825: [search] Rename refactoring doesn't update enclosing type
	 * test Search for references for the top level type Foo should report no match. "new _Foo.Bar.Foo()" refers to a different type.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297825"
	 */
	public void testBug297825c() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/b297825");
			createFile("/P/src/b297825/_Foo.java",
					"package b297825;\n" +
					"public class _Foo {\n" +
					"	public static class Bar {\n" +
					"		public static class Foo {\n" +
					"		}\n" +
					"	}\n" +
					"}"
					);
			createFile("/P/src/b297825/Foo.java",
					"package b297825;\n" +
					"public class Foo extends _Foo {\n" +
					"}"
					);
			createFile("/P/src/b297825/Main.java",
					"package b297825;\n" +
					"class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"		new _Foo.Bar.Foo();\n" +
					"	}\n" +
					"}"
					);
			waitUntilIndexesReady();
			IType type = getCompilationUnit("/P/src/b297825/Foo.java").getType("Foo");
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search(type, REFERENCES, scope, this.resultCollector);

			assertSearchResults("");
		} finally {
			deleteProject("P");
		}
	}
	/**
	 * bug297825: [search] Rename refactoring doesn't update enclosing type
	 * test Search for references for enclosing type's subclass should return a match. The inner type is parameterized.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=297825"
	 */
	public void testBug297825d() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/b297825");
			createFile("/P/src/b297825/_Foo.java",
					"package b297825;\n" +
					"public class _Foo {\n" +
					"	public static class Bar<T> {\n" +
					"	}\n" +
					"}"
					);
			createFile("/P/src/b297825/Foo.java",
					"package b297825;\n" +
					"public class Foo extends _Foo {\n" +
					"}\n"
					);
			createFile("/P/src/b297825/Main.java",
					"package b297825;\n" +
					"public class Main {\n" +
					"	public static void main(String[] args) {\n" +
					"		new Foo.Bar<String>();\n" +
					"	}\n" +
					"}"
					);
			waitUntilIndexesReady();
			IType type = getCompilationUnit("/P/src/b297825/Foo.java").getType("Foo");
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search(type, REFERENCES, scope, this.resultCollector);

			assertSearchResults("src/b297825/Main.java void b297825.Main.main(String[]) [Foo] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	/**
	 * bug342393: Anonymous class' occurrence count is incorrect when two methods in a class have the same name.
	 * test Search for Enumerators with anonymous types
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=342393"
	 */
	public void testBug342393() throws CoreException {
		try {
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
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
	 * bug376673: DBCS4.2 Can not rename the class names when DBCS (Surrogate e.g. U+20B9F) is in it
	 * test Search for DBCS type should report the match
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=376673"
	 */
	public void testBug376673a() throws CoreException {
		try {
			if ("macosx".equals(System.getProperty("osgi.os"))) {
				System.out.println("testBug376673* may fail on macosx");
				return;
			}
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			String content = "package pkg;\n" +
					"class \uD842\uDF9F1 {}\n";
			createFolder("/P/pkg");
			IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, StandardCharsets.UTF_8);
			file.setCharset("UTF-8", null);
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
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			String content = "package pkg;\n" +
					"class \uD842\uDF9F1 {" +
					"	public void \uD842\uDF9Fm() {}\n" +
					"}\n";
			createFolder("/P/pkg");
			IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, StandardCharsets.UTF_8);
			file.setCharset("UTF-8", null);
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
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			String content = "package pkg;\n" +
					"class \uD842\uDF9F1 {" +
					"	public \uD842\uDF9F1() {}\n" +
					"}\n";
			createFolder("/P/pkg");
			IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, StandardCharsets.UTF_8);
			file.setCharset("UTF-8", null);
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
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			String content = "package pkg;\n" +
					"class \uD842\uDF9F1 {" +
					"	public int \uD842\uDF9Ff;\n" +
					"}\n";
			createFolder("/P/pkg");
			IFile file = createFile("/P/pkg/\uD842\uDF9F1.java", content, StandardCharsets.UTF_8);
			file.setCharset("UTF-8", null);
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
		String os = Platform.getOS();
		if (!Platform.OS_WIN32.equals(os)) {
			// on Windows we have Windows-1252 as default, *nix should use UTF-8
			assertUTF8Encoding();
		}
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib376673.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
		IPath jarPath = p.getProject().getLocation().append("lib376673.jar");

		org.eclipse.jdt.core.tests.util.Util.createJar(
						new String[] {
						"p\uD842\uDF9F/i\uD842\uDF9F/Test.java",
						"package p\uD842\uDF9F.i\uD842\uDF9F;\n" +
						"public class Test{}\n" },
						jarPath.toOSString(),
						CompilerOptions.getFirstSupportedJavaVersion());
		refresh(p);
		waitForAutoBuild();
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
		search("Test", TYPE, DECLARATIONS, scope, this.resultCollector);

		try {
			if (this.resultCollector.count == 0) {
				System.out.println("Test " + getName() + " about to fail, listing extra debug info");
				System.out.println("LANG env variable: " + System.getenv("LANG"));
				System.out.println("Listing markers of test project");
				IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					System.out.println(marker.getAttribute(IMarker.SEVERITY) + ":" + marker.getAttribute(IMarker.MESSAGE));
				}
				System.out.println("Resolved classpath entries for test project:");
				for (IClasspathEntry e : p.getResolvedClasspath(false)) {
					System.out.println(e);
				}
				System.out.println("All classpath entries for test project:");
				for (IClasspathEntry e : p.getResolvedClasspath(true)) {
					System.out.println(e);
				}
				printJavaElements(p, System.out);
				String jarFilePath = jarPath.toOSString();
				printZipContents(new File(jarFilePath), System.out);
			}
		} catch (Throwable t) {
			System.out.println("Exception occurred while printing extra infos for test " + getName());
			t.printStackTrace(System.out);
		}

		assertSearchResults("lib376673.jar p\uD842\uDF9F.i\uD842\uDF9F.Test [No source] EXACT_MATCH");
	} finally {
		deleteProject("P");
	}
	}
	/**
	 * bug357547: [search] Search for method references is returning methods as overridden even if the superclass's method is only package-visible
	 * test Search for a non-overridden method because of package visibility should not be found
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
			project = createJavaProject("P", new String[] {""}, new String[] { "/P/lib357547.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p2/A.java",
					"package p2;\n" +
					"public class A{}\n" },
					project.getProject().getLocation().append("libStuff.jar").toOSString(), CompilerOptions.getFirstSupportedJavaVersion());

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
					CompilerOptions.getFirstSupportedJavaVersion());
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
			project = createJavaProject("P", new String[] {""}, new String[] { "/P/lib357547.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p2/A.java",
					"package p2;\n" +
					"public class A{}\n" },
					project.getProject().getLocation().append("libStuff.jar").toOSString(), CompilerOptions.getFirstSupportedJavaVersion());

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
					CompilerOptions.getFirstSupportedJavaVersion());
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
	 * bug375971: [search] Not finding method references with generics
	 * test TODO
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
	// search for constructor declarations in javadoc
	public void testBug381567a() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/b381567");
			createFile("/P/src/b381567/A.java",
					"package b381567;\n" +
							"/**\n" +
							"* {@link B#equals(java.lang.Object)}\n" +
							"*/\n" +
							"public class A {\n" +
							"	A() {}\n" +
							"}");
			createFile("/P/src/b381567/B.java",
					"package b381567;\n" +
							"public class B {\n" +
							"}");
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);
			SearchPattern pattern = SearchPattern.createPattern("*", CONSTRUCTOR, DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
			this.resultCollector.showInsideDoc();

			search(pattern, scope, this.resultCollector);

			assertSearchResults("src/b381567/A.java b381567.A() [A] EXACT_MATCH OUTSIDE_JAVADOC");
		} finally {
			deleteProject("P");
		}
	}
	// search for all constructor occurrences (declarations and references) in javadoc
	public void testBug381567b() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/b381567");
			createFile("/P/src/b381567/A.java",
					"package b381567;\n" +
							"class A {\n" +
							"	A(Exception ex) {}\n" +
							"	class B { \n" +
							"		/**\n" +
							"		 * Link {@link #A(Exception)} OK\n" +
							"		 */\n" +
							"		public B(String str) {}\n" +
							"	}\n" +
							"}"
					);
			waitUntilIndexesReady();
			IMethod[] methods = getCompilationUnit("/P/src/b381567/A.java")
					.getType("A").getMethods();
			assertEquals("Invalid number of methods", 1, methods.length);
			assertTrue(methods[0].isConstructor());
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p });
			this.resultCollector.showInsideDoc();

			search(methods[0], ALL_OCCURRENCES, scope);

			assertSearchResults("src/b381567/A.java b381567.A(Exception) [A] EXACT_MATCH OUTSIDE_JAVADOC\n"
					+ "src/b381567/A.java b381567.A$B(String) [A(Exception)] EXACT_MATCH INSIDE_JAVADOC");
		} finally {
			deleteProject("P");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=382778, Call hierarchy missing valid callers probably because java search marks exact matches as potential
	public void testBug382778() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/b382778");
			createFile("/P/src/b382778/Impl2.java",
					"package b382778;\n" +
							"public class Impl2 implements PublicInterface2 {\n" +
							"	private final String name;\n" +
							"	public Impl2(String name) {\n" +
							"		this.name = name;\n" +
							"	}\n" +
							"	public String getName() {\n" +
							"		return name;\n" +
							"	}\n" +
							"}\n"
					);
			createFile("/P/src/b382778/Main.java",
					"package b382778;\n" +
							"public class Main {\n" +
							"	public static void main(String[] args) {\n" +
							"		broken();\n" +
							"		ok();\n" +
							"	}\n" +
							"	private static void broken() {\n" +
							"		PublicInterface2 impl2 = new Impl2(\"Name Broken\");\n" +
							"		Static.printIt(impl2.getName());\n" +
							"	}\n" +
							"	private static void ok() {\n" +
							"		PublicInterface2 impl2 = new Impl2(\"Name OK\");\n" +
							"		String name = impl2.getName();\n" +
							"		Static.printIt(name);\n" +
							"	}\n" +
							"}\n"
					);
			createFile("/P/src/b382778/MainBroken.java",
					"package b382778;\n" +
							"public class MainBroken {\n" +
							"	public static void main(String[] args) {\n" +
							"		PublicInterface2 impl2 = new Impl2(\"Name Broken\");\n" +
							"		Static.printIt(impl2.getName());\n" +
							"	}\n" +
							"}\n"
					);
			createFile("/P/src/b382778/MainOK.java",
					"package b382778;\n" +
							"public class MainOK {\n" +
							"	public static void main(String[] args) {\n" +
							"		PublicInterface2 impl2 = new Impl2(\"Name OK\");\n" +
							"		String name = impl2.getName();\n" +
							"		Static.printIt(name);\n" +
							"	}\n" +
							"}\n"
					);
			createFile("/P/src/b382778/PublicInterface1.java",
					"package b382778;\n" +
							"public interface PublicInterface1 extends PackageInterface1Getters {\n" +
							"}\n" +
							"/* package */interface PackageInterface1Getters {\n" +
							"String getName();\n" +
							"}"
					);
			createFile("/P/src/b382778/PublicInterface2.java",
					"package b382778;\n" +
							"public interface PublicInterface2 extends PackageInterface2Getters {\n" +
							"}\n" +
							"/* package */interface PackageInterface2Getters extends PackageInterface1Getters {\n" +
							"}\n"
					);
			createFile("/P/src/b382778/Static.java",
					"package b382778;\n" +
							"public class Static {\n" +
							"public static void printIt(String it) {\n" +
							"System.out.println(it);\n" +
							"}\n" +
							"}"
					);
			waitUntilIndexesReady();
			ICompilationUnit unit = getCompilationUnit("/P/src/b382778/Static.java");
			IMethod method = unit.getType("Static").getMethod("printIt",
					new String[] { "QString;" });
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES);

			search(method, REFERENCES, scope, this.resultCollector);

			assertSearchResults("src/b382778/Main.java void b382778.Main.broken() [printIt(impl2.getName())] EXACT_MATCH\n"
					+ "src/b382778/Main.java void b382778.Main.ok() [printIt(name)] EXACT_MATCH\n"
					+ "src/b382778/MainBroken.java void b382778.MainBroken.main(String[]) [printIt(impl2.getName())] EXACT_MATCH\n"
					+ "src/b382778/MainOK.java void b382778.MainOK.main(String[]) [printIt(name)] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug383315a() throws CoreException {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
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
	public void testBug395348() throws CoreException {
		try {
			IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFile("/P/X.java",
					"public class X {\n"+
					"   static void f() {\n" +
					"	    new Y<C2>() {\n"+
					"           public int  compare(C2 o1) {\n" +
					"               return 0;\n" +
					"           }\n" +
					"       };\n"+
					"   }\n" +
					"}\n" +
					"interface Y<T> {\n" +
					"  public abstract int compare(T o1);\n" +
					"}\n" +
					"class C2 {}\n"
			);
			IMethod method = selectMethod(getCompilationUnit("/P/X.java"), "compare", 0);
			MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, EQUIVALENT_RULE|EXACT_RULE);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			search(pattern,  scope, this.resultCollector);
			assertSearchResults("X.java int void X.f():<anonymous>#1.compare(C2) [compare] EXACT_MATCH\n" +
								"X.java int Y.compare(T) [compare] EXACT_MATCH"); // an NPE was thrown without the fix
		} finally {
			deleteProject("P");
		}
	}

	public void testBug401272() throws CoreException, IOException {
		boolean indexState = isIndexDisabledForTest();
		// the strategy of this test was outlined in https://bugs.eclipse.org/bugs/show_bug.cgi?id=401272#c16
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" }, new String[] { "JCL18_LIB", "/P/libStuff.jar" }, "bin", CompilerOptions.getFirstSupportedJavaVersion());

			org.eclipse.jdt.core.tests.util.Util.createJar(
				new String[] {
					// this class must be our possibleMatch #401
					// it must be binary to trigger the ClassFileMatchLocator
					// the match must be impossible-due-to-mismatching-type-variables to trigger matchLocator.getMethodBinding(this.pattern);
					"p2/A.java",
					"package p2;\n" +
					"public class A<E> {\n" +
					"	public int test(E b) { return 1; }\n" +
					"	void bar() {\n" +
					"		test(null);\n" +
					"	}\n" +
					"}\n",
					// this class contains the method we search for, possibleMatch #402
					// (must be > 401 possibleMatches to trigger environment cleanup)
					"p2/B.java",
					"package p2;\n" +
					"public class B<T> {\n" +
					"	public int test(T t) {\n" +
					"		return 0;\n" +
					"	}\n" +
					"}\n"
				},
				p.getProject().getLocation().append("libStuff.jar").toOSString(), CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			createFolder("/P/src/pkg");

			this.indexDisabledForTest = true;
			// 400 matches, which populate MatchLocator.unitScope
			// all 400 matches are processed in one go of MatchLocator.locateMatches(JavaProject, PossibleMatch[], int, int)
			// next round will call nameEnvironment.cleanup() but reuse MatchLocator.unitScope ==> BOOM
			for (int i = 0; i < 400; i++) {
				createFile("/P/src/pkg/Bug"+i+".java",
						"package pkg;\n"+
						"public class Bug"+i+" {\n"+
						"	String[] test(p2.B<String> b) {\n" +
						"		return b.test(\"S\");\n" +
						"	}\n" +
						"}");
			}
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p },
					IJavaSearchScope.SOURCES|IJavaSearchScope.SYSTEM_LIBRARIES|IJavaSearchScope.APPLICATION_LIBRARIES);

			IMethod method = p.findType("p2.B").getMethods()[1];
			search(method, METHOD, ALL_OCCURRENCES, scope, this.resultCollector);

			assertSearchResults("libStuff.jar int p2.B.test(T) [No source] EXACT_MATCH"); // an NPE was thrown without the fix
		} finally {
			deleteProject("P");
			this.indexDisabledForTest = indexState;
		}
	}
	/**
	 * bug423409: [search] Search shows references to fields as potential matches
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=423409"
	 */
	public void testBug423409() throws CoreException, JavaModelException {
		try {
			createJavaProject("P", new String[] { "src" },
					new String[] {"JCL18_LIB"}, "bin");
			createFolder("/P/src/com/test");
			createFile("/P/src/com/test/Test2.java",
					"package com.test;\n" +
							"\n" +
							"class Test2 {\n" +
							"	public static final FI fi/*here*/ = null; // find references of 'fi' via Ctrl+Shift+G\n" +
							"	\n" +
							"	{\n" +
							"		fun1(fi);\n" +
							"	}\n" +
							"	\n" +
							"	private FI fun1(FI x) {\n" +
							"		System.out.println(fi);\n" +
							"		return fi;\n" +
							"	}\n" +
							"}\n" );
			createFile("/P/src/com/test/Test1.java",
					"package com.test;\n" +
							"\n" +
							"class Test1 {\n" +
							"	public static final FI fi = null;\n" +
							"}\n" );
			createFile("/P/src/com/test/T.java",
					"package com.test;\n" +
							"\n" +
							"interface FI {\n" +
							"	int foo(int x);\n" +
							"}\n");
			waitUntilIndexesReady();
			IType type = getCompilationUnit("/P/src/com/test/Test2.java").getType("Test2");
			IJavaElement field = type.getField("fi");
			search(field, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);

			assertSearchResults(
					"src/com/test/Test2.java com.test.Test2.{} [fi] EXACT_MATCH\n" +
					"src/com/test/Test2.java FI com.test.Test2.fun1(FI) [fi] EXACT_MATCH\n" +
					"src/com/test/Test2.java FI com.test.Test2.fun1(FI) [fi] EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}
	public void testBug381392() throws CoreException {
		IJavaProject egit = null;
		IJavaProject jgit = null;
		try	{
			jgit = createJavaProject("jgit", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("/jgit/base");
			createFile("/jgit/base/AbstractPlotRenderer.java",
					"package base;\n" +
					"public abstract class AbstractPlotRenderer<TLane extends PlotLane, TColor> {\n"+
					"	protected abstract TColor laneColor(TLane myLane);\n"+
					"\n"+
					"	@SuppressWarnings(\"unused\")\n"+
					"	protected void paintCommit(final PlotCommit<TLane> commit) {\n"+
					"		final TLane myLane = commit.getLane();\n"+
					"		final TColor myColor = laneColor(myLane);\n"+
					"	}\n"+
					"}\n");
			createFile("/jgit/base/PlotCommit.java",
					"package base;\n"+
					"public class PlotCommit<L extends PlotLane> {\n"+
					"	public L getLane() {\n"+
					"		return null;\n"+
					"	}\n"+
					"}");
			createFile("/jgit/base/PlotLane.java",
					"package base;\n"+
					"public class PlotLane {\n"+
					"}");
			egit = createJavaProject("egit", new String[] {""}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("/egit/bug");
			createFile("/egit/bug/SWTPlotLane.java",
					"package bug;\n" +
					"import base.PlotLane;\n" +
					"public class SWTPlotLane extends PlotLane {}");
			createFile("/egit/bug/SWTPlotRenderer.java",
					"package bug;\n" +
					"import base.AbstractPlotRenderer;\n" +
					"class SWTPlotRenderer extends AbstractPlotRenderer<SWTPlotLane, Integer> {\n" +
					"    @Override\n" +
					"    protected Integer laneColor(SWTPlotLane myLane) {\n" +
					"        return 1;\n" +
					"    }\n" +
					"}");
			addClasspathEntry(egit, JavaCore.newProjectEntry(jgit.getPath()));

			// search
			IType type  = getCompilationUnit("/egit/bug/SWTPlotRenderer.java").getType("SWTPlotRenderer");
			IMethod method = type.getMethods()[0];
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults("base/AbstractPlotRenderer.java void base.AbstractPlotRenderer.paintCommit(PlotCommit<TLane>) [laneColor(myLane)] EXACT_MATCH");
		} finally {
			if (jgit != null) deleteProject(jgit);
			if (egit != null) deleteProject(egit);
		}
	}
	public void testBug469965_0001() throws CoreException {
		try {

			IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] { "/P/lib469965.jar", "JCL18_LIB" }, "bin", "1.8");
			String libsource = "package f3;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		final Y y = new Y();\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"	}\n" +
			"	void goo() {\n" +
			"		final Y y = new Y();\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"	void bar() {}	\n" +
			"}\n";
			String jarFileName = "lib469965.jar";
			String srcZipName = "lib469965.src.zip";
			createLibrary(project, jarFileName, srcZipName, new String[] {"f3/X.java",libsource}, new String[0], CompilerOptions.getFirstSupportedJavaVersion());
			IFile srcZip=(IFile) project.getProject().findMember(srcZipName);
			IFile jar = (IFile) project.getProject().findMember(jarFileName);
			project.getPackageFragmentRoot(jar).attachSource(srcZip.getFullPath(), null, null);
			waitUntilIndexesReady();

			IType type = getClassFile("P", jarFileName, "f3", "Y.class").getType();
			IMethod method = type.getMethods()[1];
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES);
			search(method, REFERENCES, scope);
			assertSearchResults(
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			deleteProject("P");
		}
	}
	public void testBug469965_0002() throws CoreException {
		try {

			IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] { "/P/lib469965.jar", "JCL18_LIB" }, "bin", "1.8");
			String libsource = "package f3;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		final Y y = new Y();\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"	}\n" +
			"	void goo() {\n" +
			"		final Y y = new Y();\n" +
			"		new X() {\n" +
			"			void goo() {\n" +
			"		        new X() {\n" +
			"			        void goo() { y.bar();}\n" +
			"		        };\n" +
			"               y.bar();\n" +
			"           }\n" +
			"		};\n" +
			"		new X() {\n" +
			"			void goo() { y.bar();}\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"	void bar() {}	\n" +
			"}\n";
			String jarFileName = "lib469965.jar";
			String srcZipName = "lib469965.src.zip";
			createLibrary(project, jarFileName, srcZipName, new String[] {"f3/X.java",libsource}, new String[0], CompilerOptions.getFirstSupportedJavaVersion());
			IFile srcZip=(IFile) project.getProject().findMember(srcZipName);
			IFile jar = (IFile) project.getProject().findMember(jarFileName);
			project.getPackageFragmentRoot(jar).attachSource(srcZip.getFullPath(), null, null);
			waitUntilIndexesReady();

			IType type = getClassFile("P", jarFileName, "f3", "Y.class").getType();
			IMethod method = type.getMethods()[1];
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES);
			search(method, REFERENCES, scope);
			assertSearchResults(
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH\n" +
					"lib469965.jar void f3.<anonymous>.goo() EXACT_MATCH");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			deleteProject("P");
		}
	}

	public void testBug473921() throws Exception {
		try {
			IJavaProject p = this.createJavaProject(
				"P",
				new String[] {},
				new String[] { "/P/lib473921.jar", "JCL18_LIB" },
				new String[][] {{ "p/*" }, { }},
				new String[][] {{ "**/*" }, { }},
				null/*no project*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				null/*no exported project*/,
				"",
				null/*no source outputs*/,
				null/*no inclusion pattern*/,
				null/*no exclusion pattern*/,
				CompilerOptions.getFirstSupportedJavaVersion()
			);
			org.eclipse.jdt.core.tests.util.Util.createJar(
					new String[] {
							"p/Enclosing.java",
							"package p;\n" +
							"public class Enclosing { enum Nested { A, B; class Matryoshka { } } }\n",
							"classified/CEnclosing.java",
							"package classified;\n" +
							"public class CEnclosing { interface CNested { class CMatryoshka { } } }\n"
					},
					p.getProject().getLocation().append("lib473921.jar").toOSString(),
					CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p });
			class Collector extends TypeNameMatchRequestor {
				List<TypeNameMatch> matches = new ArrayList<>();
				@Override
				public void acceptTypeNameMatch(TypeNameMatch match) {
					this.matches.add(match);
				}
			}
			Collector collector = new Collector();
			new SearchEngine().searchAllTypeNames(
				null,
				new char[][] { "Nested".toCharArray(), "Enclosing".toCharArray(), "Matryoshka".toCharArray() },
				scope,
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
			assertEquals(3, collector.matches.size());
			assertEquals(IAccessRule.K_ACCESSIBLE, collector.matches.get(0).getAccessibility());
			assertEquals(IAccessRule.K_ACCESSIBLE, collector.matches.get(1).getAccessibility()); // bug 482309
			assertEquals(IAccessRule.K_ACCESSIBLE, collector.matches.get(2).getAccessibility()); // bug 482309 (double-nested type)

			collector = new Collector();
			new SearchEngine().searchAllTypeNames(
					null,
					new char[][] { "CNested".toCharArray(), "CEnclosing".toCharArray(), "CMatryoshka".toCharArray() },
					scope,
					collector,
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
					null);
			assertEquals(3, collector.matches.size());
			assertEquals(IAccessRule.K_NON_ACCESSIBLE, collector.matches.get(0).getAccessibility());
			assertEquals(IAccessRule.K_NON_ACCESSIBLE, collector.matches.get(1).getAccessibility()); // bug 482309
			assertEquals(IAccessRule.K_NON_ACCESSIBLE, collector.matches.get(2).getAccessibility()); // bug 482309 (double-nested type)
		} finally {
			deleteProject("P");
		}
	}
	public void testBug478042_0001() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()\n" +
					"/P/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()",
					requestor
			);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug478042_0002() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()\n" +
					"/P/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()",
					requestor
			);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug478042_0003() throws Exception {
		IJavaProject project = null;
		try {
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void foo() {}\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			class Collector extends MethodNameMatchRequestor {
				List<MethodNameMatch> matches = new ArrayList<>();
				@Override
				public void acceptMethodNameMatch(MethodNameMatch match) {
					this.matches.add(match);
				}
			}
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			Collector collector = new Collector();
			new SearchEngine().searchAllMethodNames(
					null, SearchPattern.R_EXACT_MATCH,
					null, SearchPattern.R_EXACT_MATCH,
					"AllMethodDeclarations01".toCharArray(), SearchPattern.R_EXACT_MATCH,
					"foo".toCharArray(), SearchPattern.R_PREFIX_MATCH,
					scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
			assertEquals(4, collector.matches.size());
		} finally {
			deleteProject("P");
		}
	}
	public void testBug478042_0004() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
					"package p478042;\n" +
					"class Y<T> {}\n" +
					"class X<T> {}\n" +
					"public class AllMethodDeclarations01 {\n" +
					"  public Y<X> foo01(Y<X> t) {}\n" +
					"  public int foo02(Object o) {return null;}\n" +
					"  public char foo03(Object o, String s) {return null;}\n" +
					"}");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
					"package p478042;\n" +
					"public class AllMethodDeclarations01b {\n" +
					"  public Integer fooInt() {return null;}\n" +
					"}");
			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java Y p478042.AllMethodDeclarations01.foo01(Y t)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()",
					requestor
			);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug478042_005() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib478042.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			createJar(new String[] {
				"p478042/AllMethodDeclarations02.java",
				"package p478042;\n" +
				"class X {}\n" +
				"class Y<T>{}\n" +
				"public class AllMethodDeclarations02 {\n" +
				"  public Y<X> foo01(Y<X> t) { return null;}\n" +
				"  public int foo02(Object o) {return 0;}\n" +
				"  public char foo03(Object o, String s) {return '0';}\n" +
				"}",
				"p478042/AllMethodDeclarations02b.java",
				"package p478042;\n" +
				"class AllMethodDeclarations02b {\n" +
				"  public void fooInt() {}\n" +
				"}"
			}, p.getProject().getLocation().append("lib478042.jar").toOSString(),
				new String[] { p.getProject().getLocation().append("lib478042.jar").toOSString() },
				CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES);
			searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/lib478042.jar|p478042/AllMethodDeclarations02.class char p478042.AllMethodDeclarations02.foo03(java.lang.Object o,java.lang.String s)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations02.class int p478042.AllMethodDeclarations02.foo02(java.lang.Object o)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations02.class p478042.Y p478042.AllMethodDeclarations02.foo01(p478042.Y t)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations02b.class void p478042.AllMethodDeclarations02b.fooInt()",
					requestor
			);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug478042_006() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib478042.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			createJar(new String[] {
				"p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"    public class Nested {\n" +
				"        public class Inner {\n" +
				"            public void foo01() {}\n" +
				"            public int foo02(Object o) {return 0;}\n" +
				"            public char foo03(Object o, String s) {return '0';}\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			}, p.getProject().getLocation().append("lib478042.jar").toOSString(),
				new String[] { p.getProject().getLocation().append("lib478042.jar").toOSString() },
				CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES);
			searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/lib478042.jar|p478042/AllMethodDeclarations01$Nested$Inner.class char p478042.Inner.foo03(java.lang.Object o,java.lang.String s)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations01$Nested$Inner.class int p478042.Inner.foo02(java.lang.Object o)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations01$Nested$Inner.class void p478042.Inner.foo01()",
					requestor
			);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug478042_007() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib478042.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			createJar(new String[] {
				"p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"    public class Nested {\n" +
				"        public class Inner {\n" +
				"            public void foo01() {}\n" +
				"            public int foo02(Object o) {return 0;}\n" +
				"            public char foo03(Object o, String s) {return '0';}\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			}, p.getProject().getLocation().append("lib478042.jar").toOSString(),
				new String[] { p.getProject().getLocation().append("lib478042.jar").toOSString() },
				CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES);
			searchAllMethodNames(
					"Inner", SearchPattern.R_EXACT_MATCH,
					"foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/lib478042.jar|p478042/AllMethodDeclarations01$Nested$Inner.class char p478042.Inner.foo03(java.lang.Object o,java.lang.String s)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations01$Nested$Inner.class int p478042.Inner.foo02(java.lang.Object o)\n" +
					"/P/lib478042.jar|p478042/AllMethodDeclarations01$Nested$Inner.class void p478042.Inner.foo01()",
					requestor
			);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug478042_008() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
					"package p478042;\n" +
					"public class AllMethodDeclarations01 {\n" +
					"    public class Nested {\n" +
					"        public class Inner {\n" +
					"            public void foo01() {}\n" +
					"            public int foo02(Object o) {return 0;}\n" +
					"            public char foo03(Object o, String s) {return '0';}\n" +
					"        }\n" +
					"    }\n" +
					"}");
			MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.Nested .Inner.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.Nested .Inner.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.Nested .Inner.foo01()",
					requestor
			);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			deleteProject(project);
		}
	}
	public void testBug483303_001() throws Exception {
		IJavaProject project = null;
		try {
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void m1(int i) {}\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			class Collector extends MethodNameMatchRequestor {
				List<MethodNameMatch> matches = new ArrayList<>();
				@Override
				public void acceptMethodNameMatch(MethodNameMatch match) {
					this.matches.add(match);
				}
			}
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			Collector collector = new Collector();
			new SearchEngine().searchAllMethodNames(
					null, SearchPattern.R_EXACT_MATCH,
					null, SearchPattern.R_EXACT_MATCH,
					"AllMethodDeclarations01".toCharArray(), SearchPattern.R_EXACT_MATCH,
					"m1".toCharArray(), SearchPattern.R_PREFIX_MATCH,
					scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
			assertEquals(1, collector.matches.size());
			IMethod method = collector.matches.get(0).getMethod();
			assertTrue(method.exists());
		} finally {
			deleteProject("P");
		}
	}
	public void testBug483650_0001() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()\n" +
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()",
					collector
			);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug483650_0002() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()\n" +
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()",
					collector
			);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug483650_0003() throws Exception {
		IJavaProject project = null;
		try {
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void foo() {}\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			class Collector extends MethodNameMatchRequestor {
				List<MethodNameMatch> matches = new ArrayList<>();
				@Override
				public void acceptMethodNameMatch(MethodNameMatch match) {
					this.matches.add(match);
				}
			}
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			Collector collector = new Collector();
			new SearchEngine().searchAllMethodNames(
					"*.AllMethodDeclarations01".toCharArray(), SearchPattern.R_PATTERN_MATCH,
					"foo".toCharArray(), SearchPattern.R_PREFIX_MATCH,
					scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
			assertEquals(4, collector.matches.size());
		} finally {
			deleteProject("P");
		}
	}
	public void testBug483650_0004() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
					"package p478042;\n" +
					"class Y<T> {}\n" +
					"class X<T> {}\n" +
					"public class AllMethodDeclarations01 {\n" +
					"  public Y<X> foo01(Y<X> t) {}\n" +
					"  public int foo02(Object o) {return null;}\n" +
					"  public char foo03(Object o, String s) {return null;}\n" +
					"}");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
					"package p478042;\n" +
					"public class AllMethodDeclarations01b {\n" +
					"  public Integer fooInt() {return null;}\n" +
					"}");
			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertSearchResults(
					"/P/src/p478042/AllMethodDeclarations01.java Y p478042.AllMethodDeclarations01.foo01(Y t)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)\n" +
					"/P/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)\n" +
					"/P/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()",
					collector
			);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug483650_005() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib478042.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			createJar(new String[] {
				"p478042/AllMethodDeclarations02.java",
				"package p478042;\n" +
				"class X {}\n" +
				"class Y<T>{}\n" +
				"public class AllMethodDeclarations02 {\n" +
				"  public Y<X> foo01(Y<X> t) { return null;}\n" +
				"  public int foo02(Object o) {return 0;}\n" +
				"  public char foo03(Object o, String s) {return '0';}\n" +
				"}",
				"p478042/AllMethodDeclarations02b.java",
				"package p478042;\n" +
				"class AllMethodDeclarations02b {\n" +
				"  public void fooInt() {}\n" +
				"}"
			}, p.getProject().getLocation().append("lib478042.jar").toOSString(),
				new String[] { p.getProject().getLocation().append("lib478042.jar").toOSString() },
				CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES);
			searchAllMethodNames("*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertSearchResults(
					"/P/lib478042.jar p478042.Y p478042.AllMethodDeclarations02.foo01(p478042.Y t)\n" +
					"/P/lib478042.jar void p478042.AllMethodDeclarations02b.fooInt()\n" +
					"/P/lib478042.jar int p478042.AllMethodDeclarations02.foo02(java.lang.Object o)\n" +
					"/P/lib478042.jar char p478042.AllMethodDeclarations02.foo03(java.lang.Object o,java.lang.String s)",
					collector
			);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug483650_006() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib478042.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			createJar(new String[] {
				"p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"    public class Nested {\n" +
				"        public class Inner {\n" +
				"            public void foo01() {}\n" +
				"            public int foo02(Object o) {return 0;}\n" +
				"            public char foo03(Object o, String s) {return '0';}\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			}, p.getProject().getLocation().append("lib478042.jar").toOSString(),
				new String[] { p.getProject().getLocation().append("lib478042.jar").toOSString() },
				CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES);
			searchAllMethodNames("*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertSearchResults(
					"/P/lib478042.jar void p478042.AllMethodDeclarations01.Nested.Inner.foo01()\n" +
					"/P/lib478042.jar int p478042.AllMethodDeclarations01.Nested.Inner.foo02(java.lang.Object o)\n" +
					"/P/lib478042.jar char p478042.AllMethodDeclarations01.Nested.Inner.foo03(java.lang.Object o,java.lang.String s)",
					collector
			);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug483650_007() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {}, new String[] { "/P/lib478042.jar", "JCL18_LIB" }, "", CompilerOptions.getFirstSupportedJavaVersion());
			createJar(new String[] {
				"p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"    public class Nested {\n" +
				"        public class Inner {\n" +
				"            public void foo01() {}\n" +
				"            public int foo02(Object o) {return 0;}\n" +
				"            public char foo03(Object o, String s) {return '0';}\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			}, p.getProject().getLocation().append("lib478042.jar").toOSString(),
				new String[] { p.getProject().getLocation().append("lib478042.jar").toOSString() },
				CompilerOptions.getFirstSupportedJavaVersion());
			refresh(p);

			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, IJavaSearchScope.SOURCES|IJavaSearchScope.APPLICATION_LIBRARIES);
			searchAllMethodNames(
					"*Inner", SearchPattern.R_PATTERN_MATCH,
					"foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertSearchResults(
					"/P/lib478042.jar void p478042.AllMethodDeclarations01.Nested.Inner.foo01()\n" +
					"/P/lib478042.jar int p478042.AllMethodDeclarations01.Nested.Inner.foo02(java.lang.Object o)\n" +
					"/P/lib478042.jar char p478042.AllMethodDeclarations01.Nested.Inner.foo03(java.lang.Object o,java.lang.String s)",
					collector
			);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug483650_008() throws Exception {
		IJavaProject project = null;
		try
		{
			// create the common project and create an interface
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
					"package p478042;\n" +
					"public class AllMethodDeclarations01 {\n" +
					"    public class Nested {\n" +
					"        public class Inner {\n" +
					"            public void foo01() {}\n" +
					"            public int foo02(Object o) {return 0;}\n" +
					"            public char foo03(Object o, String s) {return '0';}\n" +
					"        }\n" +
					"    }\n" +
					"}");
			MethodNameMatchCollector collector = new MethodNameMatchCollector() {
				@Override
				public String toString() {
					return toFullyQualifiedNamesString();
				}
			};
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			searchAllMethodNames("*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
			assertTrue(collector.matches.size() == 3);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			deleteProject(project);
		}
	}
	public void testBug483650_009() throws Exception {
		IJavaProject project = null;
		try {
			project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("P/src/p478042");
			createFile("/P/src/p478042/AllMethodDeclarations01.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01 {\n" +
				"  public void m1(int i) {}\n" +
				"  public void foo01() {}\n" +
				"  public int foo02(Object o) {return null;}\n" +
				"  public char foo03(Object o, String s) {return null;}\n" +
				"    }");
			createFile("/P/src/p478042/AllMethodDeclarations01b.java",
				"package p478042;\n" +
				"public class AllMethodDeclarations01b {\n" +
				"  public Integer fooInt() {return null;}\n" +
				"    }");
			class Collector extends MethodNameMatchRequestor {
				List<MethodNameMatch> matches = new ArrayList<>();
				@Override
				public void acceptMethodNameMatch(MethodNameMatch match) {
					this.matches.add(match);
				}
			}
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, IJavaSearchScope.SOURCES);
			Collector collector = new Collector();
			new SearchEngine().searchAllMethodNames(
					"*.AllMethodDeclarations01".toCharArray(), SearchPattern.R_PATTERN_MATCH,
					"m1".toCharArray(), SearchPattern.R_PREFIX_MATCH,
					scope, collector, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
			assertEquals(1, collector.matches.size());
			IMethod method = collector.matches.get(0).getMethod();
			assertTrue(method.exists());
		} finally {
			deleteProject("P");
		}
	}
	public void testBug489404() throws CoreException, IOException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" }, new String[] { "/P/p1p2.jar", "JCL18_LIB" }, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p1/MissingClass.java",
					"package p1;\n" +
					"public class MissingClass{}\n" },
					p.getProject().getLocation().append("p1.jar").toOSString(), CompilerOptions.getFirstSupportedJavaVersion());

			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p1/p2/BinaryWithField.java",
					"package p1.p2;\n" +
					"import p1.MissingClass;\n" +
					"public class BinaryWithField {\n" +
					"  public class Binary {\n" +
					"  }\n" +
					"  MissingClass f;\n" +
					"}\n",
					"p1/p2/BinaryWithMethod.java",
					"package p1.p2;\n" +
					"import p1.MissingClass;\n" +
					"public class BinaryWithMethod {\n" +
					"  public class Binary {\n" +
					"  }\n" +
					"  MissingClass m() {\n" +
					"    return null;\n" +
					"  }\n" +
					"}\n"},
					null,
					p.getProject().getLocation().append("p1p2.jar").toOSString(),
					new String[] { p.getProject().getLocation().append("p1.jar").toOSString() },
					CompilerOptions.getFirstSupportedJavaVersion());
			createFolder("/P/src/test");
			createFile("/P/src/test/A.java",
					"package test;\n" +
					"import p1.MissingClass;\n" +
					"public class A {\n" +
					"  MissingClass m() {\n" +
					"    return null;\n" +
					"  }\n" +
					"}\n");
			createFile("/P/src/test/TestSearchBug.java",
					"package test;\n" +
					"\n" +
					"public class TestSearchBug {\n" +
					"  public String lang;\n" +
					"}\n");
			refresh(p);
			waitUntilIndexesReady();
			IType type = getCompilationUnit("/P/src/test/TestSearchBug.java").getType("TestSearchBug");
			IField field = type.getField("lang");
			search(field, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults(
					"Unexpected search results!",
					"",
					this.resultCollector);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug491656_001() throws CoreException, IOException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" }, new String[] { "JCL18_LIB", "/P/lib491656_001.jar" }, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			String libsource = "package p2;\n" +
							"import java.util.HashMap;\n"+
							"import java.util.Map;\n"+
							"\n"+
							"public class MyLinkedHashMap<K,V>\n"+
							"    extends HashMap<K,V>\n"+
							"    implements Map<K,V>\n"+
							"{\n"+
							"    private transient Entry<K,V> header;\n"+
							"    public MyLinkedHashMap(int initialCapacity, float loadFactor) {\n"+
							"        super(initialCapacity, loadFactor);\n"+
							"    }\n"+
							"    private static class Entry<K,V>  {\n"+
							"        public Object hash;\n"+
							"        Entry<K,V> before, after;\n"+
							"\n"+
							"        Entry(int hash, K key, V value, HashMap.Entry<K,V> next) {}\n"+
							"        private void remove491656() {}\n"+
							"        private void remove491656Test() {}\n"+
							"\n"+
							"        private void addBefore491656(Entry<K,V> existingEntry) {}\n"+
							"        public void iamAPublicMethod491656(Entry<K,V> existingEntry) {}\n"+
							"\n"+
							"        void recordAccess(HashMap<K,V> m) {\n"+
							"            MyLinkedHashMap<K,V> lm = (MyLinkedHashMap<K,V>)m;\n"+
							"            remove491656();\n"+
							"            remove491656Test();\n"+
							"            addBefore491656(lm.header);\n"+
							"            iamAPublicMethod491656(lm.header);\n"+
							"        }\n"+
							"    }\n"+
							"}\n";
			String jarFileName = "lib491656_001.jar";
			String srcZipName = "lib491656_001.src.zip";
			createLibrary(p, jarFileName, srcZipName, new String[] {"p2/MyLinkedHashMap.java",libsource}, new String[0], CompilerOptions.getFirstSupportedJavaVersion());
			IFile srcZip=(IFile) p.getProject().findMember(srcZipName);
			IFile jar = (IFile) p.getProject().findMember(jarFileName);
			p.getPackageFragmentRoot(jar).attachSource(srcZip.getFullPath(), null, null);
			refresh(p);
			waitUntilIndexesReady();
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			IType enclosingType = p.findType("p2.MyLinkedHashMap");
			IType[] types = enclosingType.getTypes();
			IMethod[] methods = types[0].getMethods();
			IMethod method = methods[3];
			assertTrue("Wrong Method", method.toString().contains("addBefore491656"));
			SearchPattern pattern = SearchPattern.createPattern(method, REFERENCES);
			MatchLocator.setFocus(pattern, method);
			new SearchEngine(this.workingCopies).search(
				pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				getJavaSearchScope(),
				this.resultCollector,
				null
			);
			search(pattern, scope, this.resultCollector);
			assertSearchResults(
			"lib491656_001.jar void p2.MyLinkedHashMap$Entry.recordAccess(java.util.HashMap<K,V>) EXACT_MATCH");
		} finally {
			deleteProject("P");
		}
	}

	/**
	 * Test that a diamond in a type name doesn't result in an AIOOBE during a search.
	 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/825
	 */
	public void testOutOfBoundsIndexExceptionDuringSearchGh825() throws Exception {
		String projectName = "testGh825";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");
			String srcFolder = "/" + projectName + "/src/";
			String packageFolder = srcFolder + "test";
			createFolder(packageFolder);
			String snippet = String.join(System.lineSeparator(), new String[] {
					"package test;",
					"public class Test {",
					"    public interface TestInterface<T> {",
					"        void testMethod(T t);",
					"    }",
					"    TestInterface<String> SUP = new TestInterface<>() {",
					"        public void testMethod(String s) {}",
					"    };",
					"    static void shouldFail() {}",
					"}",
			});
			createFile(packageFolder + "/Test.java", snippet);

			String module = "module tester {}";
			createFile(srcFolder + "/module-info.java", module);

			String testFolder = "/" + projectName + "/test";
			createFolder(testFolder);
			IClasspathAttribute[] testAttrs = { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, Boolean.toString(true)) };
			addClasspathEntry(project, JavaCore.newSourceEntry(
					new Path(testFolder),
					null,
					null,
					new Path("/" + projectName + "/bin-test"),
					testAttrs));

			addLibrary(project,
					"libGh825.jar",
					"libGh825.src.zip",
					new String[] {
							"testpackage/TestClass.java",
							"package testpackage;\n" +
							"public class TestClass {}"
					},
					JavaCore.VERSION_11);

			buildAndExpectNoProblems(project);
			IType type = project.findType("test.Test");
			IMethod method = type.getMethod("shouldFail", new String[0]);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			// we expect no matches and no exceptions
			assertSearchResults("");
		} finally {
			deleteProject(projectName);
		}
	}
	public void testCallHierarchyAnonymousInnerTypeGh856() throws CoreException, IOException {
		try {
			IJavaProject p = createJavaProject("P", new String[] { "src" }, new String[] { "JCL11_LIB" }, "bin", "11");
			createFolder("/P/src/test");
			createFile("/P/src/test/X.java", String.join(System.lineSeparator(), new String[] {
					"package test;\n",
					"public class X {\n",
					"    public static interface Action1 {\n",
					"        public void doit();\n",
					"    }\n",
					"\n",
					"    public static interface Action2 {\n",
					"        public void doit();\n",
					"    }\n",
					"\n",
					"    public static void action1( Action1 action ) {\n",
					"        action.doit();\n",
					"    }\n",
					"\n",
					"    public static void action2( Action2 action ) {\n",
					"        action.doit();\n",
					"    }\n",
					"\n",
					"    public static void testMethod() {\n",
					"        action2( new Action2() {\n",
					"            public void doit() { // call hierarchy here\n",
					"            }\n",
					"        } );\n",
					"    }\n",
					"}\n",
			}));
			buildAndExpectNoProblems(p);
			IType type = p.findType("test.X");
			IMethod testMethod = type.getMethod("testMethod", new String[0]);
			IJavaElement[] children = testMethod.getChildren();
			assertEquals("Expected to find 1 anonymous type in method, instead found children " + Arrays.toString(children),
					1, children.length);
			IType anonymousType = (IType) children[0];
			IMethod method = anonymousType.getMethod("doit", new String[0]);
			search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults(
					"Unexpected search results!",
					"src/test/X.java void test.X.action2(Action2) [doit()] EXACT_MATCH",
					this.resultCollector);
		} finally {
			deleteProject("P");
		}
	}

	/**
	 * Test that an inner class constructor with first argument of the outer type
	 * doesn't result in no search matches.
	 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/401
	 */
	public void testInnerConstructorWithOuterTypeArgumentGh401() throws Exception {
		String projectName = "testGh401";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");
			String srcFolder = "/" + projectName + "/src/";
			String packageFolder = srcFolder + "test1";
			createFolder(packageFolder);
			String snippet1 =
					"""
					package test1;
					public final class Outer1 {
						public void staticConstructorCaller() {
							new StaticInner1(this);
						}
						public void constructorCaller() {
							new Inner1(this);
						}
						private static final class StaticInner1 {
							StaticInner1(Outer1 o) {}
						}
						private final class Inner1 {
							Inner1(Outer1 o) {}
						}
					}
					""";
			createFile(packageFolder + "/Outer1.java", snippet1);

			addLibrary(project,
					"libGh401.jar",
					"libGh401.src.zip",
					new String[] {
							"test2/Outer2.java",
							"""
							package test2;
							public final class Outer2 {
								public void staticConstructorCaller() {
									new StaticInner2(this);
								}
								public void constructorCaller() {
									new Inner2(this);
								}
								private static final class StaticInner2 {
									StaticInner2(Outer2 o) {}
								}
								private final class Inner2 {
									Inner2(Outer2 o) {}
								}
							}
							""",
					},
					JavaCore.VERSION_11);

			buildAndExpectNoProblems(project);

			String[] typesAndExpcetedMatches = {
				"test1.Outer1.StaticInner1",
				"src/test1/Outer1.java void test1.Outer1.staticConstructorCaller() [new StaticInner1(this)] EXACT_MATCH",
				"test1.Outer1.Inner1",
				"src/test1/Outer1.java void test1.Outer1.constructorCaller() [new Inner1(this)] EXACT_MATCH",
				"test2.Outer2.StaticInner2",
				"libGh401.jar void test2.Outer2.staticConstructorCaller() EXACT_MATCH",
				"test2.Outer2.Inner2",
				"libGh401.jar void test2.Outer2.constructorCaller() EXACT_MATCH",

			};

			for (int i = 0; i < typesAndExpcetedMatches.length; i += 2) {
				String type =          typesAndExpcetedMatches[i + 0];
				String expectedMatch = typesAndExpcetedMatches[i + 1];
				IType staticInnerType = project.findType(type);
				IMethod staticInnerConstructor = staticInnerType.getMethods()[0];
				search(staticInnerConstructor, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
				assertSearchResults(expectedMatch);
				this.resultCollector.clear();
			}
		} finally {
			deleteProject(projectName);
		}
	}

	/**
	 * For the following scenario, no call hierarchy is found:<br>
	 * A nested private class implements a public nested interface.
	 * The overridden method from the interface also has an argument of the interface type.
	 * No call hierarchy is found for the overridden method, when searching from within the nested class.
	 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/821
	 */
	public void testCallHierarchyWithNestedInterfacesGh821() throws Exception {
		String projectName = "TestCallHierarchyWithNestedInterfacesGh821";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] { "src" }, new String[] { "JCL18_LIB" }, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			String packageFolder = "/" + projectName + "/src/test";
			createFolder(packageFolder);
			String snippet1 =
					"""
					package test;
					public class TestNestedPrivateClass {
						public NestedInterface nm = new NC();
						public interface NestedInterface {
							public void foo(NestedInterface arg);
						}
						private final class NC implements NestedInterface {
							public void foo(NestedInterface arg) {}
						}
					}
					""";
			createFile(packageFolder + "/TestNestedPrivateClass.java", snippet1);
			String snippet2 =
					"""
					package test;
					public class TestReferencingClass {
						public static void bar() {
							TestNestedPrivateClass c = new TestNestedPrivateClass();
							TestNestedPrivateClass.NestedInterface nestedMember = c.nm;
							nestedMember.foo(nestedMember);
						}
					}
					""";
			createFile(packageFolder + "/TestReferencingClass.java", snippet2);
			buildAndExpectNoProblems(project);
			IType type = project.findType("test.TestNestedPrivateClass.NC");
			IMethod[] methods = type.getMethods();
			assertEquals("Expected one method in: " + Arrays.asList(methods), 1, methods.length);
			search(methods[0], REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults(
					"src/test/TestReferencingClass.java void test.TestReferencingClass.bar() [foo(nestedMember)] EXACT_MATCH");
		} finally {
			deleteProject(projectName);
		}
	}

	private static void printJavaElements(IJavaProject javaProject, PrintStream output) throws Exception {
		output.println("Printing Java elements of Java project: " + javaProject);
		List<IJavaElement> queue = new LinkedList<>();
		while (!queue.isEmpty()) {
			IJavaElement element = queue.remove(queue.size() - 1);
			output.println(element);
			output.print(element);
			if (element instanceof IParent) {
				IParent parent = (IParent) element;
				queue.addAll(Arrays.asList(parent.getChildren()));
			}
		}
	}

	private static void printZipContents(File jarFile, PrintStream output) throws Exception {
		if (jarFile.exists()) {
			output.println("Listing contents of jar file: " + jarFile);
			try (ZipFile zipFile = new ZipFile(jarFile)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry zipEntry = entries.nextElement();
					String zipEntryName = zipEntry.getName();
					output.println("Listing contents of zip entry: " + zipEntryName);
					InputStream zipEntryInputStream = zipFile.getInputStream(zipEntry);
					if (zipEntryName.endsWith(".class")) {
						byte[] classFileBytes = toByteArray(zipEntryInputStream);
						ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
						String classContents = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
						output.println(classContents);
					} else {
						@SuppressWarnings("resource") // the zip file resource-try will close the stream
						Scanner scanner = new Scanner(zipEntryInputStream);
						while (scanner.hasNextLine()) {
							output.println(scanner.nextLine());
						}
					}
				}
			}
		} else {
			output.println("File does not exist: " + jarFile);
		}
	}

	private static byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[8192];

		int nRead;
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		return buffer.toByteArray();
	}

	private void assertUTF8Encoding() {
		String fileEncodingVMProperty = System.getProperty("file.encoding");
		Charset defaultCharset = Charset.defaultCharset();
		String langEnvVariable = System.getenv("LANG");
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append(System.lineSeparator());
		errorMessage.append("Unexpected encoding during test, encoding parameters are:");
		errorMessage.append(System.lineSeparator());
		errorMessage.append("-Dfile.encoding=" + fileEncodingVMProperty);
		errorMessage.append(System.lineSeparator());
		errorMessage.append("Charset.defaultCharset()=" + defaultCharset);
		errorMessage.append(System.lineSeparator());
		errorMessage.append("LANG=" + langEnvVariable);

		assertEquals("unexpected default charset " + errorMessage, StandardCharsets.UTF_8, defaultCharset);
		if (Constants.OS_LINUX.equals(System.getProperty("osgi.os"))) {
			assertNotNull("expected LANG environment variable to be set" + errorMessage, langEnvVariable);
			String utf8LANGRegexp = ".*\\.(UTF-8|UTF8|utf-8|utf8)";
			Pattern utf8LANGPattern = Pattern.compile(utf8LANGRegexp);
			Matcher LANGMatcher = utf8LANGPattern.matcher(langEnvVariable);
			assertTrue("expected encoding set with LANG environment variable to match regexp: " + utf8LANGRegexp + ", LANG=" + langEnvVariable + errorMessage,
					LANGMatcher.matches());
		}
	}
}