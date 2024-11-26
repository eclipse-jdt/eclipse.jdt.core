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

import java.io.File;
import junit.framework.Test;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.osgi.framework.Bundle;

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
		Bundle[] bundles = Platform.getBundles("org.eclipse.jdt.annotation", "[2.0.0,3.0.0)");
		File bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
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
			addClasspathEntry(p2, JavaCore.newProjectEntry(p1.getPath())); // no access to the annotation lib, since not re-exported

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
					"----------\n" +
					"1. WARNING in /P2/client/TestingAnnotated.java (at line 9)\n" +
					"	AutoCloseable a = () -> {} ; // produces warning\n" +
					"	              ^\n" +
					"Resource leak: 'a' is never closed\n" +
					"----------\n";
			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);

			// Challenge JavaBuilder:
			p2.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			markers = p2.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Resource leak: 'a' is never closed",
					markers);
			assertEquals("Unexpected marker path", "/P2/client/TestingAnnotated.java", markers[0].getResource().getFullPath().toString());

			// Challenge CompilationUnitResolver:
			ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
			parser.setProject(p2);
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
		} finally {
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	public void testAnnotationNotRexeported_bin() throws Exception {
		try {
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL_23_LIB", this.ANNOTATION_LIB}, "bin", "23");
			p.setOption(JavaCore.COMPILER_ANNOTATION_RESOURCE_ANALYSIS, JavaCore.ENABLED);
			String jarAbsPath = p.getProject().getLocation()+"/annotated.jar";

			createJar(new String[] {
					"annotated/TestNotOwning.java",
					"""
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
					"""
					},
					jarAbsPath,
					new String[] {this.ANNOTATION_LIB},
					"23");

			// no access to the annotation lib, since not re-exported
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
					"----------\n" +
					"1. WARNING in /P/client/TestingAnnotated.java (at line 9)\n" +
					"	AutoCloseable a = () -> {} ; // produces warning\n" +
					"	              ^\n" +
					"Resource leak: 'a' is never closed\n" +
					"----------\n";
			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);

			// Challenge JavaBuilder:
			p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Resource leak: 'a' is never closed",
					markers);
			assertEquals("Unexpected marker path", "/P/client/TestingAnnotated.java", markers[0].getResource().getFullPath().toString());

			// Challenge CompilationUnitResolver:
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
		} finally {
			deleteProject("P");
		}
	}
}
