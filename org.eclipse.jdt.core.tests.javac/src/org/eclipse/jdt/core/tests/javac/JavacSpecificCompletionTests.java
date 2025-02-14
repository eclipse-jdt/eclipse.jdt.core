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

import static org.junit.Assert.assertTrue;

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
import org.eclipse.jdt.core.CompletionProposal;
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

	@Test
	public void testCompletionForEachElementInScope() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				import java.util.List;
				public class HelloWorld {
					public static void main(String... args) {
						List<String> myList = List.of("a", "b", "c");
						for (String element : myList) {
							System.out.println(elemen);
						}
					}
				}
				""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		// there are many types ending with "element" that are (correctly) suggested here
		requestor.setIgnored(CompletionProposal.TYPE_REF, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "elemen";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
			assertEquals(
					"element[LOCAL_VARIABLE_REF]{element, null, Ljava.lang.String;, element, 82}",
					requestor.getResults());
		} catch (OperationCanceledException e) {
			Assert.assertTrue("Should not be cancelled", false);
		}
	}

	@Test
	public void testSystemOutCompletion() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class HelloWorld {
					public static void main(String... args) {
						System.
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "System.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		assertTrue(requestor.getResults().contains("out[FIELD_REF]{out, Ljava.lang.System;, Ljava.io.PrintStream;, out, [78, 78], 51}"));
	}

	@Test
	public void testWhileCastedMethodRefCompletion() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class HelloWorld {
					public int foo() { return 12; }
					public static void main(String... args) {
						Object obj = null;
						while (obj instanceof HelloWorld) {
							obj.fo
						}
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "obj.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		assertTrue(requestor.getResults().contains("foo[METHOD_REF_WITH_CASTED_RECEIVER]{((HelloWorld)obj).foo(), LHelloWorld;, ()I, LHelloWorld;, foo, replace[164, 170], receiver[164, 167], 60}"));
	}

	@Test
	public void testForCastedMethodRefCompletion() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class HelloWorld {
					public int foo() { return 12; }
					public static void main(String... args) {
						Object obj = null;
						for (;obj instanceof HelloWorld;) {
							obj.fo
						}
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "obj.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		assertTrue(requestor.getResults().contains("foo[METHOD_REF_WITH_CASTED_RECEIVER]{((HelloWorld)obj).foo(), LHelloWorld;, ()I, LHelloWorld;, foo, replace[164, 170], receiver[164, 167], 60}"));
	}

	@Test
	public void testNegativeIfContainingReturnCastedMethodRefCompletion() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class HelloWorld {
					public int foo() { return 12; }
					public static void main(String... args) {
						Object obj = null;
						if (!(obj instanceof HelloWorld)) {
							String asdf = "";
							return;
						}
						obj.fo
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "obj.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		assertTrue(requestor.getResults().contains("foo[METHOD_REF_WITH_CASTED_RECEIVER]{((HelloWorld)obj).foo(), LHelloWorld;, ()I, LHelloWorld;, foo, replace[199, 205], receiver[199, 202], 60}"));
	}

	@Test
	public void testCastedFieldRefPreservesComments() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class DemoClass {
					int myField = 12;
					public void myMethod() {
						MyNested obj = new MyNested();
						if (obj.nestedField instanceof DemoClass) {
							obj /* obj.nestedField is definitely a DemoClass */ . nestedField . myF
						}
					}

					public static class MyNested {
						public Object nestedField = null;
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "myF";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		assertEquals("myField[FIELD_REF_WITH_CASTED_RECEIVER]{((DemoClass)obj /* obj.nestedField is definitely a DemoClass */ . nestedField) . myField, LDemoClass;, I, LHelloWorld~DemoClass;, myField, replace[152, 223], receiver[152, 219], 60}", requestor.getResults());
	}

	@Test
	public void testCompleteAnonymousClassField() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class DemoClass  {
					public void myMethod() {
						Object obj = new Object() {
							int myField = 4;
							void myMethod() {
								System.out.println(myF);
							}
						};
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "myF";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		assertEquals("myField[FIELD_REF]{myField, Ljava.lang.Object;, I, myField, [146, 149], 82}", requestor.getResults());
	}

}
