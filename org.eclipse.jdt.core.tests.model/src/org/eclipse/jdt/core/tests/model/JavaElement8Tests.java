/*******************************************************************************
 * Copyright (c) 2014, 2018 IBM Corporation and others.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.LambdaExpression;
import org.eclipse.jdt.internal.core.LambdaMethod;
import org.eclipse.jdt.internal.core.SourceMethod;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaElement8Tests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] {"testBug428178"};
	}

	public JavaElement8Tests(String name) {
		super(name);
		this.endChar = "";
	}
	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_1_8, JavaElement8Tests.class);
	}
	@Deprecated
	static int getJSL9() {
		return AST.JLS9;
	}
	public void testBug428178() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug428178", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
				String fileContent =  """
					package p;
					public interface Test {
						static void main(String[] args) {
							System.out.println("Hello");
						}
					}""";
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
			String fileContent =  """
				package p;
				public interface Test {
					static void main(String[] args) {
						System.out.println("Hello");
					}
				}""";
			addLibrary(project,
							"lib.jar",
							"src.zip", new
							String[] {"p/Test.java", fileContent},
							JavaCore.VERSION_1_8);
				IType type = getPackageFragmentRoot("Bug428178", "lib.jar").getPackageFragment("p").getOrdinaryClassFile("Test.class").getType();
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
			String fileContent =  """
				package p;
				public interface Test {
					static void main(String[] args) {
						I i = (x) -> {};
					}
				}
				interface I {
				  public void foo(int x);
				}""";
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
			String fileContent =  """
				package p;
				public interface Test {
					static void main(String[] args) {
						I i = (x) -> {};
					}
				}
				interface I {
				  public void foo(int x);
				}""";
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
					"""
				interface Supplier<T> {
				    T get();
				}
				interface Runnable {
				    public abstract void run();
				}
				public class X {
					public static void main(String[] args) {
						execute(() -> {
							executeInner(() -> {
							});
							return null;
						});
						System.out.println("done");
					}
					static <R> R execute(Supplier<R> supplier) {
						return null;
					}
					static void executeInner(Runnable callback) {
					}
				}
				""";
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
					"""
				interface Supplier<T> {
				    T get();
				}
				interface Runnable {
				    public abstract void run();
				}
				public class X {
					public static void main(String[] args) {
						execute(() -> {
				           executeOuter(() -> {
							    executeInner(() -> {
							    });
							    return null;
						    });
				       });
						System.out.println("done");
					}
					static <R> R execute(Supplier<R> supplier) {
						return null;
					}
					static void executeInner(Runnable callback) {
					}
					static void executeOuter(Runnable callback) {
					}
				}
				""";
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
					"""
				interface Supplier<T> {
				    T get();
				}
				interface Runnable {
				    public abstract void run();
				}
				public class X {
					public static void main(String[] args) {
						execute(() -> {
				           executeOuter(() -> {
							    executeInner(() -> {
							    });
						    });
						return null;
				       });
					}
					static <R> R execute(Supplier<R> supplier) {
						return null;
					}
					static void executeInner(Runnable callback) {
					}
					static void executeOuter(Runnable callback) {
					}
				}
				""";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);
			IType type = getCompilationUnit(fileName).getType("Runnable");
			ITypeHierarchy h = type.newTypeHierarchy(null);
			assertHierarchyEquals(
					"""
						Focus: Runnable [in X.java [in <default> [in src [in Bug429966]]]]
						Super types:
						Sub types:
						  <lambda #1> [in get() [in <lambda #1> [in main(String[]) [in X [in X.java [in <default> [in src [in Bug429966]]]]]]]]
						  <lambda #1> [in run() [in <lambda #1> [in get() [in <lambda #1> [in main(String[]) [in X [in X.java [in <default> [in src [in Bug429966]]]]]]]]]]
						""",
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
			String fileContent =  """
				package p;
				import java.util.List;
				public interface Test {
					static void main(String[] args) {
						I<String> i = (x) -> {};
					}
				}
				interface I<T> {
				  public void foo(List<T> x);
				}""";
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429910 [1.8][model] Superinterfaces of lambda element's IType are missing type arguments
	public void testBug429910a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug429910", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =  """
				package p;
				public interface MyFunction<T, R> {
					R apply(T t);
					default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {
						return (V v) -> apply(before.apply(v));\
					}\
				}""";
			createFolder("/Bug429910/src/p");
			createFile(	"/Bug429910/src/p/MyFunction.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/Bug429910/src/p/MyFunction.java");
			int start = fileContent.indexOf("v))");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect no of elements", 1, elements.length);
			assertEquals("Incorrect element type", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IMethod method = (IMethod) elements[0].getParent();
			assertTrue("Should be a lambda method",method.isLambdaMethod());
			assertEquals("Incorrect lambda method signature", "(TV;)TR;", method.getSignature());
			IJavaElement parent = method.getParent();
			assertTrue("Should be a lambda expression", (parent instanceof LambdaExpression));
			LambdaExpression lambda = (LambdaExpression) parent;
			String sigs = lambda.getSuperInterfaceTypeSignatures()[0];
			assertEquals("Incorrect super interface signature", "Lp.MyFunction<TV;TR;>;", sigs);
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
					"""
				interface MyFunction<T, R> {
				    R apply(T t);
				    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {
				        return (V v) -> apply(before.apply(v));
				    }
				}
				""";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);

			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("v");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertElementEquals("Wrong element", "v [in apply(V) [in <lambda #1> [in compose(MyFunction<? super V,? extends T>) [in MyFunction [in X.java [in <default> [in src [in Bug429966]]]]]]]]", elements[0]);
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
					"""
				interface MyFunction<T, R> {
				    R apply(T t);
				    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {
				        return v -> apply(before.apply(v));
				    }
				}
				""";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);

			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("v");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertElementEquals("Wrong element", "v [in apply(V) [in <lambda #1> [in compose(MyFunction<? super V,? extends T>) [in MyFunction [in X.java [in <default> [in src [in Bug429966]]]]]]]]", elements[0]);
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
					"""
						public interface IntPredicate {
						    boolean test(int value);
						    default IntPredicate and(IntPredicate other) {
						        return (value) -> test(value) && other.test(value);
						    }
						}
						"""
				};

				HashMap libraryOptions = new HashMap(project.getOptions(true));
				libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
				addLibrary(project, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);


			IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
			IPackageFragment packageFragment = packageFragmentRoots[2].getPackageFragment("");
			IClassFile classFile = packageFragment.getClassFile("IntPredicate.class");
			IJavaElement[] elements = classFile.codeSelect(128, 5);
			assertElementEquals("Wrong element", "value [in test(int) [in <lambda #1> [in and(IntPredicate) [in IntPredicate [in IntPredicate.class [in <default> [in Elements.jar [in Bug430033]]]]]]]]", elements[0]);
		}
		finally {
			if (project != null) {
				removeLibrary(project, jarName, srcName);
				deleteProject(projectName);
			}
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430141,  [1.8][hierarchy] Incorrect hierarchy with lambda elements missing
	public void test430141() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug430141", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =
							"""
				interface I {
					void doit();
				}
				interface J extends I {
				}
				public class X {
					public static void main(String[] args) {
						J j  = () -> { System.out.println("Lambda"); };
						j.doit();
					}
				}
				""";
			createFile(	"/Bug430141/src/X.java",	fileContent);
			IType type = getCompilationUnit("/Bug430141/src/X.java").getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);
			assertHierarchyEquals(
							"""
								Focus: I [in X.java [in <default> [in src [in Bug430141]]]]
								Super types:
								Sub types:
								  J [in X.java [in <default> [in src [in Bug430141]]]]
								    <lambda #1> [in main(String[]) [in X [in X.java [in <default> [in src [in Bug430141]]]]]]
								""",
					h);
		}
		finally {
			deleteProject("Bug430141");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430141,  [1.8][hierarchy] Incorrect hierarchy with lambda elements missing
	public void test430141a() throws Exception {
		try {
			IJavaProject project = createJavaProject("Bug430141", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =
							"""
				interface I {
					void doit();
				}
				interface J extends I {
				}
				public class X {
					public static void main(String[] args) {
						J j  = () -> { System.out.println("Lambda"); };
						j.doit();
					}
				}
				""";
			createFile(	"/Bug430141/src/X.java",	fileContent);
			IType type = getCompilationUnit("/Bug430141/src/X.java").getType("J");
			ITypeHierarchy h = type.newTypeHierarchy(null);
			assertHierarchyEquals(
					"""
						Focus: J [in X.java [in <default> [in src [in Bug430141]]]]
						Super types:
						  I [in X.java [in <default> [in src [in Bug430141]]]]
						Sub types:
						  <lambda #1> [in main(String[]) [in X [in X.java [in <default> [in src [in Bug430141]]]]]]
						""",
					h);
		}
		finally {
			deleteProject("Bug430141");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430136
	public void test430136() throws CoreException {
		String projectName = "([Bug430136])";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =
					"""
				interface MyFunction<T, R> {
				    R apply(T t);
				    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {
				        return v -> apply(before.apply(v));
				    }
				}
				""";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);

			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("v");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect java element", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IType lambda = (IType) elements[0].getParent().getParent();
			String mem = lambda.getHandleIdentifier();
			String expected = "=\\(\\[Bug430136\\])/src<{X.java[MyFunction~compose~QMyFunction\\<-QV;+QT;>;=)=\"LMyFunction\\<TV;TR;>;!148!174!151=&apply!1=\"TV;=\"v=\"TR;=\"LX\\~MyFunction\\<LX\\~MyFunction;:1TV;LX\\~MyFunction;:TR;>;.apply\\(TV;)TR;@v!148!148!148!148!Ljava\\/lang\\/Object;!0!true=)";
			assertEquals("Incorrect memento", expected, mem);
			IJavaElement result = JavaCore.create(expected);
			assertEquals("Incorrect element created", lambda, result);
		}
		finally {
			deleteProject(projectName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431716
	public void test431716() throws CoreException {
		String projectName = "Bug431716";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =
					"""
				public interface X<T> {
				    default void asIntStream() {
				    	mapToInt((long l) -> (int) l);\
				    }
					default void mapToInt(ToIntFunction<? super T> mapper) {}
				interface ToIntFunction<T> {
					int applyAsInt(T value);
				}
				""";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);

			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("l)");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect java element", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			String mem = elements[0].getHandleIdentifier();
			String expected = """
				=Bug431716/src<{X.java[X~asIntStream\
				=)="LX$ToIntFunction\\<TT;>;!71!89!81=&\
				applyAsInt!1="TT;="l="I="LX$ToIntFunction\\<LX;:TT;>;.\
				applyAsInt\\(TT;)I@l!72!77!77!77!Ljava\\/lang\\/Object;!0!true=&\
				@l!72!77!77!77!J!0!true""";
			assertEquals("Incorrect memento", expected, mem);

			IJavaElement parent = elements[0].getParent();
			mem = parent.getHandleIdentifier();
			expected = """
				=Bug431716/src<{X.java[X~asIntStream\
				=)="LX$ToIntFunction\\<TT;>;!71!89!81=&\
				applyAsInt!1="TT;="l="I="LX$ToIntFunction\\<LX;:TT;>;.\
				applyAsInt\\(TT;)I@l!72!77!77!77!Ljava\\/lang\\/Object;!0!true=&""";
			assertEquals("Incorrect memento", expected, mem);
			assertTrue("Parent should be LambdaMethod", parent instanceof LambdaMethod);

			parent = parent.getParent();
			mem = parent.getHandleIdentifier();
			expected = """
				=Bug431716/src<{X.java[X~asIntStream\
				=)="LX$ToIntFunction\\<TT;>;!71!89!81=&\
				applyAsInt!1="TT;="l="I="LX$ToIntFunction\\<LX;:TT;>;.\
				applyAsInt\\(TT;)I@l!72!77!77!77!Ljava\\/lang\\/Object;!0!true=)""";
			assertEquals("Incorrect memento", expected, mem);
			assertTrue("Grand-parent should be LambdaExpression", parent instanceof LambdaExpression);

			parent = parent.getParent();
			mem = parent.getHandleIdentifier();
			expected = "=Bug431716/src<{X.java[X~asIntStream";
			assertEquals("Incorrect memento", expected, mem);
			assertTrue("Great-grand-parent should be SourceMethod", parent instanceof SourceMethod);
		}
		finally {
			deleteProject(projectName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430195
	public void test430195() throws CoreException {
		String projectName = "Bug430195";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);
			String fileContent =
					"""
				interface MyFunction<T, R> {
				    R apply(T t);
				    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {
				        return v -> apply(before.apply(v));
				    }
				}
				""";
			String fileName = "/" + projectName + "/src/X.java";
			createFile(fileName, fileContent);

			ICompilationUnit unit = getCompilationUnit(fileName);
			int start = fileContent.indexOf("v");
			IJavaElement[] elements = unit.codeSelect(start, 1);
			assertEquals("Incorrect java element", IJavaElement.LOCAL_VARIABLE, elements[0].getElementType());
			IType lambda = (IType) elements[0].getParent().getParent();
			assertEquals("Incorrect qualified type name", "MyFunction$1", lambda.getTypeQualifiedName());
		}
		finally {
			deleteProject(projectName);
		}
	}

	public void testBug485080() throws Exception {
		String projectName = "Bug485080";
		try {
			IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.open(null);

			// create a .java file in a folder that's not on the build path:
			IFolder folder= project.getProject().getFolder("nosrc");
			folder.create(0, true, null);
			IFile file= folder.getFile("X.java");
			String content= """
				public class X {
					public <T> void meth(T s) {
					}
				}
				""";
			file.create(new ByteArrayInputStream(content.getBytes("UTF-8")), 0, null);

			// create a CU from that file:
			ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
			cu.becomeWorkingCopy(null);

			// create the binding for the CU's main type, and drill down to details:
			ASTParser parser= ASTParser.newParser(getJSL9());
			parser.setProject(project);
			IBinding[] bindings = parser.createBindings(new IJavaElement[] { cu.findPrimaryType() }, null);
			IMethodBinding methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[1];
			assertEquals("method name", "meth", methodBinding.getName());
			ITypeBinding typeParameter = methodBinding.getTypeParameters()[0];

			// fetch and inspect the corresponding java element:
			IJavaElement javaElement = typeParameter.getJavaElement();
			assertNotNull("java element", javaElement);
			assertEquals("element kind", IJavaElement.TYPE_PARAMETER, javaElement.getElementType());
			assertEquals("element name", "T", javaElement.getElementName());
		} finally {
			deleteProject(projectName);
		}
	}
}
