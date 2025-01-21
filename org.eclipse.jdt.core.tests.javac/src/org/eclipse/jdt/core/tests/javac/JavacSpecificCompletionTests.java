/*******************************************************************************
 * Copyright (c) 2025, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.javac;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests.ProblemRequestor;
import org.eclipse.jdt.core.tests.model.CompletionTestsRequestor2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * These cases aren't handled properly upstream yet.
 */
public class JavacSpecificCompletionTests {

	private static IJavaProject COMPLETION_PROJECT = null;
	private static WorkingCopyOwner WC_OWNER = new WorkingCopyOwner() {
	};

	private ICompilationUnit[] workingCopies = null;

	private static IJavaProject importProject(String locationInBundle)
			throws URISyntaxException, IOException, CoreException {
		File file = new File(FileLocator
				.toFileURL(JavacSpecificCompletionTests.class.getResource("/" + locationInBundle + "/.project")).toURI());
		IPath dotProjectPath = Path.fromOSString(file.getAbsolutePath());
		IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().loadProjectDescription(dotProjectPath);
		projectDescription.setLocation(dotProjectPath.removeLastSegments(1));
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
		proj.create(projectDescription, null);
		proj.open(null);
		proj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		IJavaProject javaProject = JavaCore.create(proj);
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "21");
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "21");
		javaProject.setOption(JavaCore.COMPILER_RELEASE, "21");
		javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "21");
		javaProject.setOption(JavaCore.CODEASSIST_SUBWORD_MATCH, JavaCore.DISABLED);
		javaProject.open(new NullProgressMonitor());
		return javaProject;
	}

	private static void assertEquals(String expected, String actual) {
		if ((expected == null && expected == actual) || expected.equals(actual)) {
			return;
		}
		StringBuilder formatted = new StringBuilder();
		formatted.append("\n----------- Expected ------------\n"); //$NON-NLS-1$
		formatted.append(expected);
		formatted.append("\n------------ but was ------------\n"); //$NON-NLS-1$
		formatted.append(actual);
		formatted.append("\n---------------------- ----------\n"); //$NON-NLS-1$

		throw new junit.framework.ComparisonFailure(formatted.toString(), expected, actual);
	}

	private ICompilationUnit getWorkingCopy(String fileName, String contents) throws JavaModelException {
		try {
			ICompilationUnit cu = (ICompilationUnit) COMPLETION_PROJECT.findElement(Path.fromOSString(fileName));
			ICompilationUnit workingCopy = cu.getWorkingCopy(WC_OWNER, null);
			workingCopy.getBuffer().setContents(contents);
			IProblemRequestor problemRequestor = WC_OWNER.getProblemRequestor(workingCopy);
			if (problemRequestor instanceof ProblemRequestor) {
				((ProblemRequestor) problemRequestor).initialize(contents.toCharArray());
			}
			workingCopy.makeConsistent(null/* no progress monitor */);
			return workingCopy;
		} catch (JavaModelException e) {
			Assert.fail("unable to get working copy of file");
		}
		return null;
	}

	@BeforeClass
	public static void setupClass() throws URISyntaxException, IOException, CoreException {
		COMPLETION_PROJECT = importProject("projects/Completion");
	}

	@Before
	public void setup() {
	}

	@Test
	public void testScopePatternVariablesFromForExpressionInside() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				import java.util.List;
				public class HelloWorld {
					private static record MyRecord(String a, int b) {}
					public static void main(String... args) {
						List myList = List.of(new MyRecord("hello", 12), new Object());
						for (int i = 0; i < myList.size() && myList.get(i) instanceof MyRecord(String jello, int kilometer); i++) {
							System.out.println(jel);
						}
					}
				}
				""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.workingCopies[0].getSource();
		String completeBehind = "jel";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
			assertEquals(
					"jello[LOCAL_VARIABLE_REF]{jello, null, Ljava.lang.String;, jello, 82}",
					requestor.getResults());
		} catch (OperationCanceledException e) {
			Assert.assertTrue("Should not be cancelled", false);
		}
	}

	@Test
	public void testScopePatternVariablesFromForExpressionOutsideInvalid() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				import java.util.List;
				public class HelloWorld {
					private static record MyRecord(String a, int b) {}
					public static void main(String... args) {
						List myList = List.of(new MyRecord("hello", 12), new Object());
						for (int i = 0; i < myList.size() && myList.get(i) instanceof MyRecord(String jello, int kilometer); i++) {
						}
						System.out.println(jel);
					}
				}
				""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.workingCopies[0].getSource();
		String completeBehind = "jel";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
			assertEquals(
					"",
					requestor.getResults());
		} catch (OperationCanceledException e) {
			Assert.assertTrue("Should not be cancelled", false);
		}
	}

	@Test
	public void testScopePatternVariablesFromForExpressionOutsideValid() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				import java.util.List;
				public class HelloWorld {
					private static record MyRecord(String a, int b) {}
					public static void main(String... args) {
						List myList = List.of(new MyRecord("hello", 12), new Object());
						for (int i = 0; !(myList.get(i) instanceof MyRecord(String jello, int kilometer)); i++) {
						}
						System.out.println(jel);
					}
				}
				""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.workingCopies[0].getSource();
		String completeBehind = "jel";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
			assertEquals(
					"jello[LOCAL_VARIABLE_REF]{jello, null, Ljava.lang.String;, jello, 82}",
					requestor.getResults());
		} catch (OperationCanceledException e) {
			Assert.assertTrue("Should not be cancelled", false);
		}
	}

	@Test
	public void testScopePatternVariablesFromForExpressionOutsideWithOr() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				import java.util.List;
				public class HelloWorld {
					private static record MyRecord(String a, int b) {}
					public static void main(String... args) {
						List myList = List.of(new MyRecord("hello", 12), new Object());
						for (int i = 0; i < myList.size() || !(myList.get(i) instanceof MyRecord(String jello, int kilometer)); i++) {
						}
						System.out.println(jel);
					}
				}
				""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.workingCopies[0].getSource();
		String completeBehind = "jel";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
			assertEquals(
					"jello[LOCAL_VARIABLE_REF]{jello, null, Ljava.lang.String;, jello, 82}",
					requestor.getResults());
		} catch (OperationCanceledException e) {
			Assert.assertTrue("Should not be cancelled", false);
		}
	}

}
