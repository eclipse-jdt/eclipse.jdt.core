/*******************************************************************************
 * Copyright (c) 2011, 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	 Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.tests.util.Util;

public class OwningAnnotationModelTests extends ReconcilerTests {

	String ANNOTATION_LIB;

	public static Test suite() {
		return buildModelTestSuite(OwningAnnotationModelTests.class);
	}

	public OwningAnnotationModelTests(String name) {
		super(name);
	}

	static {
//		TESTS_NAMES = new String[] { "testAnnotationNotRexeported_src" };
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// TODO: switch to bundle, once it updates BREE to 9 and contains module-info.java:
//		Bundle[] bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.2.0,3.0.0)");
//		File bundleFile = FileLocator.getBundleFile(bundles[0]);
//		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
		this.ANNOTATION_LIB = createAnnotation_2_2_jar(getExternalPath(), getExternalJCLPathString("9"));
	}

	public static String createAnnotation_2_2_jar(String dirName, String jcl9Path) throws IOException {
		// role our own annotation library as long as o.e.j.annotation is still at BREE 1.8:
		String jarFileName = dirName + "org.eclipse.jdt.annotation_2.2.0.jar";
		Util.createJar(new String[] {
				"module-info.java",
				"module org.eclipse.jdt.annotation {\n" +
				"	exports org.eclipse.jdt.annotation;\n" +
				"}\n",

				"org/eclipse/jdt/annotation/Owning.java",
				"package org.eclipse.jdt.annotation;\n" +
				"import java.lang.annotation.*;\n" +
				" \n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Documented\n" +
				"@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE })\n" +
				"public @interface Owning {\n" +
				"	// marker annotation with no members\n" +
				"}\n",

				"org/eclipse/jdt/annotation/NotOwning.java",
				"package org.eclipse.jdt.annotation;\n" +
				"import java.lang.annotation.*;\n" +
				" \n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@Documented\n" +
				"@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE })\n" +
				"public @interface NotOwning {\n" +
				"	// marker annotation with no members\n" +
				"}\n",
			},
			null,
			jarFileName,
			null,
			"23");
		return jarFileName;
	}

	private void assertErrorFromCompilationUnitResolver(IJavaProject p, ICompilationUnit unit, String expectedError) {
		ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
		parser.setProject(p);
		parser.setResolveBindings(true);
		parser.setSource(unit);
		CompilationUnit ast = (CompilationUnit) parser.createAST(null);
		assertNotNull("ast should not be null", ast);
		this.problemRequestor.reset();
		this.problemRequestor.beginReporting();
		IProblem[] problems = ast.getProblems();
		for (int i=0; i<problems.length; i++)
			this.problemRequestor.acceptProblem(problems[i]);
		assertProblems("Unexpected problems from CompilationUnitResolver", expectedError);
	}

	private void assertMarkersFromJavaBuilder(IJavaProject p, String expectedMarkers, String markerPath) throws CoreException {
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers", expectedMarkers, markers);
		if (markerPath != null)
			assertEquals("Unexpected marker path", markerPath, markers[0].getResource().getFullPath().toString());
	}

	public void testAnnotationNotRexeported_src() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p1 = createJavaProject("P1", new String[] {""}, new String[] {"JCL_23_LIB", this.ANNOTATION_LIB}, "bin", "23");
			p1.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);

			createFolder("/P1/annotated");
			String testNotOwningPath = "/P1/annotated/TestNotOwning.java";
			String testNotOwningSource = """
					package annotated;
					import java.util.List;
					import org.eclipse.jdt.annotation.NotOwning;
					import org.eclipse.jdt.annotation.Owning;

					public class TestNotOwning implements AutoCloseable {
						private List<AutoCloseable> toClose;

						@NotOwning
						public <T extends AutoCloseable> T register(Object dontCare, @Owning T closeable) throws Exception {
							closeable.close();
							return closeable;
						}

						public void close() throws Exception {
							for (AutoCloseable closeable : toClose) {
								closeable.close(); // Ignore error handling for this demonstration
							}
						}
					}
					""";
			createFile(testNotOwningPath, testNotOwningSource);

			this.problemRequestor.initialize(testNotOwningSource.toCharArray());
			ICompilationUnit unit = getCompilationUnit(testNotOwningPath).getWorkingCopy(this.wcOwner, null);
			assertNoProblem(unit.getBuffer().getCharacters(), unit);

			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);


			IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_23_LIB"}, "bin", "23");
			p2.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
			p2.setOption(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, JavaCore.WARNING);
			addClasspathEntry(p2, JavaCore.newProjectEntry(p1.getPath())); // no access to the annotation lib, since not re-exported

			createFolder("/P2/client");
			String testingAnnotatedPath = "/P2/client/TestingAnnotated.java";
			String testingAnnotatedSource = """
					package client;

					import annotated.TestNotOwning;
					import java.io.InputStream;

					public class TestingAnnotated {

						public static void client() throws Exception {
							try (TestNotOwning t = new TestNotOwning()) {
								AutoCloseable a = () -> {} ;
								AutoCloseable b = t.register(null, new InputStream());
								assert a != null;
								assert b != null;
							}
						}
					}
					""";
			createFile(testingAnnotatedPath, testingAnnotatedSource);

			// Challenge CompilationUnitProblemFinder:
			this.problemRequestor.initialize(testingAnnotatedSource.toCharArray());
			unit = getCompilationUnit(testingAnnotatedPath).getWorkingCopy(this.wcOwner, null);
			String expectedError =
					"""
					----------
					1. WARNING in /P2/client/TestingAnnotated.java (at line 10)
						AutoCloseable a = () -> {} ;
						              ^
					Resource leak: 'a' is never closed
					----------
					2. WARNING in /P2/client/TestingAnnotated.java (at line 11)
						AutoCloseable b = t.register(null, new InputStream());
						              ^
					Resource leak: 'b' is never closed
					----------
					3. WARNING in /P2/client/TestingAnnotated.java (at line 11)
						AutoCloseable b = t.register(null, new InputStream());
						                  ^^^^^^^^^^
					Method 'register' has an unresolved annotation 'NotOwning' that could be relevant for static analysis
					----------
					4. WARNING in /P2/client/TestingAnnotated.java (at line 11)
						AutoCloseable b = t.register(null, new InputStream());
						                                   ^^^^^^^^^^^^^^^^^
					Parameter 'closeable' of method 'register' has an unresolved annotation 'Owning' that could be relevant for static analysis
					----------
					5. WARNING in /P2/client/TestingAnnotated.java (at line 11)
						AutoCloseable b = t.register(null, new InputStream());
						                                   ^^^^^^^^^^^^^^^^^
					Mandatory close of resource '<unassigned Closeable value>' has not been shown
					----------
					""";
			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);

			// Challenge JavaBuilder (using class files from p1 we only see one problem):
			assertMarkersFromJavaBuilder(p2, "Resource leak: 'a' is never closed", "/P2/client/TestingAnnotated.java");

			assertErrorFromCompilationUnitResolver(p2, unit, expectedError);
		} finally {
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	public void testAnnotationNotRexeported_srcModular() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p1 = createJavaProject("P1", new String[] {""}, new String[] {"JCL_23_LIB"}, "bin", "23");
			addLibraryEntry(p1, new Path(this.ANNOTATION_LIB), null, null, null, null,
					new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")}, false);
			p1.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
			p1.setOption(JavaCore.COMPILER_SOURCE, "23");
			p1.setOption(JavaCore.COMPILER_COMPLIANCE, "23");

			createFile("/P1/module-info.java",
					"""
					module P1 {
						requires org.eclipse.jdt.annotation;
						exports annotated;
					}
					""");
			createFolder("/P1/annotated");
			String testNotOwningPath = "/P1/annotated/TestNotOwning.java";
			String testNotOwningSource = """
					package annotated;
					import java.util.List;
					import org.eclipse.jdt.annotation.NotOwning;
					import org.eclipse.jdt.annotation.Owning;

					public class TestNotOwning implements AutoCloseable {
						private List<AutoCloseable> toClose;

						@NotOwning
						public <T extends AutoCloseable> T register(@Owning T closeable) throws Exception {
							closeable.close();
							return closeable;
						}

						public void close() throws Exception {
							for (AutoCloseable closeable : toClose) {
								closeable.close(); // Ignore error handling for this demonstration
							}
						}
					}
					""";
			createFile(testNotOwningPath, testNotOwningSource);

			this.problemRequestor.initialize(testNotOwningSource.toCharArray());
			ICompilationUnit unit = getCompilationUnit(testNotOwningPath).getWorkingCopy(this.wcOwner, null);
			assertNoProblem(unit.getBuffer().getCharacters(), unit);

			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);


			IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_23_LIB"}, "bin", "23");
			p2.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
			p2.setOption(JavaCore.COMPILER_SOURCE, "23");
			p2.setOption(JavaCore.COMPILER_COMPLIANCE, "23");
			addClasspathEntry(p2,
					JavaCore.newProjectEntry(p1.getPath(),
							null,
							false,
							new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")},
							false) );
				// no access to the annotation lib, since not re-exported

			createFile("/P2/module-info.java",
					"""
					module P2 {
						requires P1;
					}
					""");

			createFolder("/P2/client");
			String testingAnnotatedPath = "/P2/client/TestingAnnotated.java";
			String testingAnnotatedSource = """
					package client;

					import annotated.TestNotOwning;

					public class TestingAnnotated {

						public static void client() throws Exception {
							try (TestNotOwning t = new TestNotOwning()) {
								AutoCloseable a = () -> {} ; // produces warning
								AutoCloseable b = t.register(() -> {} ); // produces no warning
								assert a != null;
								assert b != null;
							}
						}
					}
					""";
			createFile(testingAnnotatedPath, testingAnnotatedSource);

			// Challenge CompilationUnitProblemFinder:
			this.problemRequestor.initialize(testingAnnotatedSource.toCharArray());
			unit = getCompilationUnit(testingAnnotatedPath).getWorkingCopy(this.wcOwner, null);
			String expectedError =
					"""
					----------
					1. WARNING in /P2/client/TestingAnnotated.java (at line 9)
						AutoCloseable a = () -> {} ; // produces warning
						              ^
					Resource leak: 'a' is never closed
					----------
					2. WARNING in /P2/client/TestingAnnotated.java (at line 10)
						AutoCloseable b = t.register(() -> {} ); // produces no warning
						              ^
					Resource leak: 'b' is never closed
					----------
					3. WARNING in /P2/client/TestingAnnotated.java (at line 10)
						AutoCloseable b = t.register(() -> {} ); // produces no warning
						                  ^^^^^^^^^^
					Method 'register' has an unresolved annotation 'NotOwning' that could be relevant for static analysis
					----------
					""";
			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);

			// Challenge JavaBuilder (using class files from p1 we only see one problem):
			assertMarkersFromJavaBuilder(p2, "Resource leak: 'a' is never closed", "/P2/client/TestingAnnotated.java");

			assertErrorFromCompilationUnitResolver(p2, unit, expectedError);
		} finally {
			deleteProject("P1");
			deleteProject("P2");
		}
	}
	public void testAnnotationNotRexeported_bin() throws Exception {
		try {
			// using class TestNotOwning from a jar, all three strategies show only one problem:

			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL_23_LIB", this.ANNOTATION_LIB}, "bin", "23");
			p.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
			String jarAbsPath = p.getProject().getLocation()+"/annotated.jar";

			createJar(new String[] {
					"annotated/TestNotOwning.java",
					"""
					package annotated;
					import java.util.List;
					import org.eclipse.jdt.annotation.*;

					public class TestNotOwning implements AutoCloseable {
						private List<AutoCloseable> toClose;

						@NotOwning
						public <T extends AutoCloseable> T register(@Owning T closeable) throws Exception {
							closeable.close();
							return closeable;
						}

						public void close() throws Exception {
							for (AutoCloseable closeable : toClose) {
								closeable.close(); // Ignore error handling for this demonstration
							}
						}
					}
					"""
					},
					jarAbsPath,
					new String[] {this.ANNOTATION_LIB},
					"23");

			// no access to the annotation lib
			addClasspathEntry(p, JavaCore.newLibraryEntry(new Path(jarAbsPath), null, null));

			createFolder("/P/client");
			String testingAnnotatedPath = "/P/client/TestingAnnotated.java";
			String testingAnnotatedSource = """
					package client;

					import annotated.TestNotOwning;

					public class TestingAnnotated {

						public static void client() throws Exception {
							try (TestNotOwning t = new TestNotOwning()) {
								AutoCloseable a = () -> {} ; // produces warning
								AutoCloseable b = t.register(() -> {} ); // produces no warning
								assert a != null;
								assert b != null;
							}
						}
					}
					""";
			createFile(testingAnnotatedPath, testingAnnotatedSource);

			// Challenge CompilationUnitProblemFinder:
			this.problemRequestor.initialize(testingAnnotatedSource.toCharArray());
			ICompilationUnit unit = getCompilationUnit(testingAnnotatedPath).getWorkingCopy(this.wcOwner, null);
			String expectedError =
					"""
					----------
					1. WARNING in /P/client/TestingAnnotated.java (at line 9)
						AutoCloseable a = () -> {} ; // produces warning
						              ^
					Resource leak: 'a' is never closed
					----------
					""";
			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);

			assertMarkersFromJavaBuilder(p, "Resource leak: 'a' is never closed", "/P/client/TestingAnnotated.java");

			assertErrorFromCompilationUnitResolver(p, unit, expectedError);
		} finally {
			deleteProject("P");
		}
	}

	public void testAnnotationNotRexeported_src_inheritedField() throws CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p1 = createJavaProject("P1", new String[] {""}, new String[] {"JCL_23_LIB", this.ANNOTATION_LIB}, "bin", "23");
			p1.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);

			createFolder("/P1/annotated");
			String testNotOwningPath = "/P1/annotated/TestOwning.java";
			String testNotOwningSource = """
					package annotated;

					import org.eclipse.jdt.annotation.Owning;

					public class TestOwning implements AutoCloseable {
						protected @Owning AutoCloseable cached;

						public void close() throws Exception {
							cached.close(); // Ignore error handling for this demonstration
						}
					}
					""";
			createFile(testNotOwningPath, testNotOwningSource);

			this.problemRequestor.initialize(testNotOwningSource.toCharArray());
			ICompilationUnit unit = getCompilationUnit(testNotOwningPath).getWorkingCopy(this.wcOwner, null);
			assertNoProblem(unit.getBuffer().getCharacters(), unit);

			p1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p1.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);


			IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_23_LIB"}, "bin", "23");
			p2.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
			p2.setOption(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, JavaCore.WARNING);
			addClasspathEntry(p2, JavaCore.newProjectEntry(p1.getPath())); // no access to the annotation lib, since not re-exported

			createFolder("/P2/client");
			String testingAnnotatedPath = "/P2/client/TestingAnnotated.java";
			String testingAnnotatedSource = """
					package client;

					import annotated.TestOwning;
					import java.io.InputStream;

					public class TestingAnnotated extends TestOwning {

						public void client() throws Exception {
							this.cached = new InputStream();
						}
					}
					""";
			createFile(testingAnnotatedPath, testingAnnotatedSource);

			// Challenge CompilationUnitProblemFinder:
			this.problemRequestor.initialize(testingAnnotatedSource.toCharArray());
			unit = getCompilationUnit(testingAnnotatedPath).getWorkingCopy(this.wcOwner, null);
			String expectedError =
					"""
					----------
					1. WARNING in /P2/client/TestingAnnotated.java (at line 9)
						this.cached = new InputStream();
						^^^^^^^^^^^
					Field 'cached' has an unresolved annotation 'Owning' that could be relevant for static analysis
					----------
					2. WARNING in /P2/client/TestingAnnotated.java (at line 9)
						this.cached = new InputStream();
						              ^^^^^^^^^^^^^^^^^
					Mandatory close of resource '<unassigned Closeable value>' has not been shown
					----------
					""";
			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);

			// Challenge JavaBuilder (using binary types from p1 we have no problem):
			assertMarkersFromJavaBuilder(p2, "", null);

			assertErrorFromCompilationUnitResolver(p2, unit, expectedError);
		} finally {
			deleteProject("P1");
			deleteProject("P2");
		}
	}
}
