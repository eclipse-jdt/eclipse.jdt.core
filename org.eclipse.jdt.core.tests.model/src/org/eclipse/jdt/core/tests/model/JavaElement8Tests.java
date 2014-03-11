/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.LambdaExpression;

public class JavaElement8Tests extends AbstractJavaModelTests { 

	static {
//		TESTS_NAMES = new String[] {"testBug428178"};
	}

	public JavaElement8Tests(String name) {
		super(name);
		this.endChar = "";
	}
	public static Test suite() {
		if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS!=null || TESTS_RANGE !=null) {
			return buildModelTestSuite(JavaElement8Tests.class);
		}
		TestSuite suite = new Suite(JavaElement8Tests.class.getName());
		suite.addTest(new JavaElement8Tests("testBug428178"));
		suite.addTest(new JavaElement8Tests("testBug428178a"));
		suite.addTest(new JavaElement8Tests("testBug429641"));
		suite.addTest(new JavaElement8Tests("testBug429641a"));
		suite.addTest(new JavaElement8Tests("test429948"));
		suite.addTest(new JavaElement8Tests("test429948a"));
		suite.addTest(new JavaElement8Tests("test429966"));
		suite.addTest(new JavaElement8Tests("testBug429910"));
		suite.addTest(new JavaElement8Tests("test430026"));
		suite.addTest(new JavaElement8Tests("test430026a"));
		suite.addTest(new JavaElement8Tests("test430033"));
		return suite;
	}
	public void testBug428178() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
				String fileContent =  "package p;\n" +
						 "public interface Test {\n" +
						 "	static void main(String[] args) {\n" +
						 "		System.out.println(\"Hello\");\n" +
						 "	}\n" +
						 "}";
				createFolder("/Bug428178/src/p");
				createFile(	"/Bug428178/src/p/Test.java",	fileContent);

				ICompilationUnit unit = getCompilationUnit("/Bug428178/src/p/Test.java");
				IMethod method = unit.getTypes()[0].getMethods()[0];
				assertNotNull("Method should not be null", method);
				assertTrue("Should be a main method", method.isMainMethod());
		}
		finally {
			deleteProject("Bug428178");
		}
	}
	public void testBug428178a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		System.out.println(\"Hello\");\n" +
					 "	}\n" +
					 "}";
			addLibrary(project, 
							"lib.jar", 
							"src.zip", new 
							String[] {"p/Test.java", fileContent},
							JavaCore.VERSION_1_8);
				IType type = getPackageFragmentRoot("Bug428178", "lib.jar").getPackageFragment("p").getClassFile("Test.class").getType();
				IMethod method = type.getMethods()[0];
				assertNotNull("Method should not be null", method);
				assertTrue("Should be a main method", method.isMainMethod());
		}
		finally {
			deleteProject("Bug428178");
		}
	}
	public void testBug429641() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429641", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		I i = (x) -> {};\n" +
					 "	}\n" +
					 "}\n" + 
					 "interface I {\n" + 
					 "  public void foo(int x);\n" +
					 "}";
			createFolder("/Bug429641/src/p");
			createFile(	"/Bug429641/src/p/Test.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/Bug429641/src/p/Test.java");
			int start = fileContent.indexOf("x) ->");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IMethod method = (IMethod) elements[0].getParent();
			assertTrue("Should be a lambda method", method.isLambdaMethod());
		}
		finally {
			deleteProject("Bug429641");
		}
	}
	public void testBug429641a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429641", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		I i = (x) -> {};\n" +
					 "	}\n" +
					 "}\n" + 
					 "interface I {\n" + 
					 "  public void foo(int x);\n" +
					 "}";
			createFolder("/Bug429641/src/p");
			createFile(	"/Bug429641/src/p/Test.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/Bug429641/src/p/Test.java");
			int start = fileContent.lastIndexOf("x");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IMethod method = (IMethod) elements[0].getParent();
			assertTrue("Should not be a lambda method", !method.isLambdaMethod());
		}
		finally {
			deleteProject("Bug429641");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429948, Unhandled event loop exception is thrown when a lambda expression is nested
	public void test429948() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429948", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent = 
					"interface Supplier<T> {\n" +
					"    T get();\n" +
					"}\n" +
					"interface Runnable {\n" +
					"    public abstract void run();\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		execute(() -> {\n" +
					"			executeInner(() -> {\n" +
					"			});\n" +
					"			return null;\n" +
					"		});\n" +
					"		System.out.println(\"done\");\n" +
					"	}\n" +
					"	static <R> R execute(Supplier<R> supplier) {\n" +
					"		return null;\n" +
					"	}\n" +
					"	static void executeInner(Runnable callback) {\n" +
					"	}\n" +
					"}\n";
			createFile(	"/Bug429948/src/X.java",	fileContent);
			IType type = getCompilationUnit("/Bug429948/src/X.java").getType("X");
			ITypeHierarchy h = type.newSupertypeHierarchy(null);
			assertHierarchyEquals(
					"Focus: X [in X.java [in <default> [in src [in Bug429948]]]]\n" + 
					"Super types:\n" + 
					"  Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclMin1.8.jar]]]\n" + 
					"Sub types:\n",
					h);
		}
		finally {
			deleteProject("Bug429948");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429948, Unhandled event loop exception is thrown when a lambda expression is nested
	public void test429948a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429948", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent = 
					"interface Supplier<T> {\n" +
					"    T get();\n" +
					"}\n" +
					"interface Runnable {\n" +
					"    public abstract void run();\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		execute(() -> {\n" +
					"           executeOuter(() -> {\n" +
					"			    executeInner(() -> {\n" +
					"			    });\n" +
					"			    return null;\n" +
					"		    });\n" +
					"       });\n" +
					"		System.out.println(\"done\");\n" +
					"	}\n" +
					"	static <R> R execute(Supplier<R> supplier) {\n" +
					"		return null;\n" +
					"	}\n" +
					"	static void executeInner(Runnable callback) {\n" +
					"	}\n" +
					"	static void executeOuter(Runnable callback) {\n" +
					"	}\n" +
					"}\n";
			createFile(	"/Bug429948/src/X.java",	fileContent);
			IType type = getCompilationUnit("/Bug429948/src/X.java").getType("X");
			ITypeHierarchy h = type.newSupertypeHierarchy(null);
			assertHierarchyEquals(
					"Focus: X [in X.java [in <default> [in src [in Bug429948]]]]\n" + 
					"Super types:\n" + 
					"  Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclMin1.8.jar]]]\n" + 
					"Sub types:\n",
					h);
		}
		finally {
			deleteProject("Bug429948");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429966, [1.8] CUD#functionalExpressions may record lambda copies in nested lambda situations
	public void test429966() throws CoreException {
		String projectName = "Bug429966";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent = 
					"interface Supplier<T> {\n" +
					"    T get();\n" +
					"}\n" +
					"interface Runnable {\n" +
					"    public abstract void run();\n" +
					"}\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		execute(() -> {\n" +
					"           executeOuter(() -> {\n" +
					"			    executeInner(() -> {\n" +
					"			    });\n" +
					"		    });\n" +
					"		return null;\n" +
					"       });\n" +
					"	}\n" +
					"	static <R> R execute(Supplier<R> supplier) {\n" +
					"		return null;\n" +
					"	}\n" +
					"	static void executeInner(Runnable callback) {\n" +
					"	}\n" +
					"	static void executeOuter(Runnable callback) {\n" +
					"	}\n" +
					"}\n";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);
			IType type = getCompilationUnit(fileName).getType("Runnable");
			ITypeHierarchy h = type.newTypeHierarchy(null);
			assertHierarchyEquals(
					"Focus: Runnable [in X.java [in <default> [in src [in Bug429966]]]]\n" + 
					"Super types:\n" + 
					"Sub types:\n" + 
					"  Lambda(Runnable) [in get() [in Lambda(Supplier) [in main(String[]) [in X [in X.java [in <default> [in src [in Bug429966]]]]]]]]\n" + 
					"  Lambda(Runnable) [in run() [in Lambda(Runnable) [in get() [in Lambda(Supplier) [in main(String[]) [in X [in X.java [in <default> [in src [in Bug429966]]]]]]]]]]\n",
					h);
		}
		finally {
			deleteProject(projectName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429910
	public void testBug429910() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429910", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  "package p;\n" +
					 "import java.util.List;\n" +
					 "public interface Test {\n" +
					 "	static void main(String[] args) {\n" +
					 "		I<String> i = (x) -> {};\n" +
					 "	}\n" +
					 "}\n" + 
					 "interface I<T> {\n" + 
					 "  public void foo(List<T> x);\n" +
					 "}";
			createFolder("/Bug429910/src/p");
			createFile(	"/Bug429910/src/p/Test.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/Bug429910/src/p/Test.java");
			int start = fileContent.indexOf("x) ->");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IMethod method = (IMethod) elements[0].getParent();
			assertTrue("Should be a lambda method",method.isLambdaMethod());
			IJavaElement parent = method.getParent();
			assertTrue("Should be a lambda expression", (parent instanceof LambdaExpression));
			LambdaExpression lambda = (LambdaExpression) parent;
			String sigs = lambda.getSuperInterfaceTypeSignatures()[0];
			assertEquals("Incorrect super interface signature", "Lp.I<Ljava.lang.String;>;", sigs);
		}
		finally {
			deleteProject("Bug429910");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430026,  [1.8] Lambda parameter has wrong parent if it declares its type
	public void test430026() throws CoreException {
		String projectName = "Bug429966";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent = 
					"interface MyFunction<T, R> {\n" +
					"    R apply(T t);\n" +
					"    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {\n" +
					"        return (V v) -> apply(before.apply(v));\n" +
					"    }\n" +
					"}\n";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);
			
			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("v");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertElementEquals("Wrong element", "v [in apply(V) [in Lambda(MyFunction) [in compose(MyFunction<? super V,? extends T>) [in MyFunction [in X.java [in <default> [in src [in Bug429966]]]]]]]]", elements[0]);
		}
		finally {
			deleteProject(projectName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430026,  [1.8] Lambda parameter has wrong parent if it declares its type
	public void test430026a() throws CoreException {
		String projectName = "Bug429966";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent = 
					"interface MyFunction<T, R> {\n" +
					"    R apply(T t);\n" +
					"    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {\n" +
					"        return v -> apply(before.apply(v));\n" +
					"    }\n" +
					"}\n";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);
			
			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("v");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertElementEquals("Wrong element", "v [in apply(V) [in Lambda(MyFunction) [in compose(MyFunction<? super V,? extends T>) [in MyFunction [in X.java [in <default> [in src [in Bug429966]]]]]]]]", elements[0]);
		}
		finally {
			deleteProject(projectName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430033, [1.8][model] Lambda elements missing in ancestry for binary elements 
	public void test430033() throws CoreException, IOException {
		
		String jarName = "Elements.jar";
		String srcName = "Elements_src.zip";
		String projectName = "Bug430033";
		IJavaProject project = null;
		try {
			project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
		
			String[] pathAndContents = new String[] {
					"IntPredicate.java",
					"public interface IntPredicate {\n" +
					"    boolean test(int value);\n" +
					"    default IntPredicate and(IntPredicate other) {\n" +
					"        return (value) -> test(value) && other.test(value);\n" +
					"    }\n" +
					"}\n"
				};
			
				HashMap libraryOptions = new HashMap(project.getOptions(true));
				libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
				addLibrary(project, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);
				
				
			IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
			IPackageFragment packageFragment = packageFragmentRoots[2].getPackageFragment("");
			IClassFile classFile = packageFragment.getClassFile("IntPredicate.class");
			IJavaElement[] elements = classFile.codeSelect(128, 5);
			assertElementEquals("Wrong element", "value [in test(int) [in Lambda(IntPredicate) [in and(IntPredicate) [in IntPredicate [in IntPredicate.class [in <default> [in Elements.jar [in Bug430033]]]]]]]]", elements[0]);
		}
		finally {
			if (project != null) {
				removeLibrary(project, jarName, srcName);
				deleteProject(projectName);
			}
		}
	}
}
