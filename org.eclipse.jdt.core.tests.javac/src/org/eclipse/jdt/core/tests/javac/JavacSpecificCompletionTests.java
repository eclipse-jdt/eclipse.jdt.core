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
		assertEquals("myField[FIELD_REF_WITH_CASTED_RECEIVER]{((DemoClass)obj /* obj.nestedField is definitely a DemoClass */ . nestedField) . myField, LDemoClass;, I, LDemoClass;, myField, replace[152, 223], receiver[152, 219], 60}", requestor.getResults());
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

	@Test
	public void testCompleteFirstParameter() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class HelloWorld  {
					static final int AAA = 1;
					static final int BBB = 2;
					public void myMethod() {
						invocable();
					}
					public void invocable(int a, int b) {
						System.out.println("sum: " + (a + b));
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "invocable(";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);

		// The "invocable" result here is questionable (completing it does nothing),
		// but it matches upstream behaviour
		// The results here don't match upstream behaviour;
		// upstream the results are filtered to those that match the expected type (int).
		// however, if you type a character, then close and reopen completion, it's no longer filtered this way.
		assertEquals("""
				finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, [119, 119], 44}
				invocable[METHOD_REF]{, LHelloWorld;, (II)V, invocable, [119, 119], 44}
				myMethod[METHOD_REF]{myMethod(), LHelloWorld;, ()V, myMethod, [119, 119], 44}
				notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, [119, 119], 44}
				notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, [119, 119], 44}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, [119, 119], 44}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, [119, 119], 44}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, [119, 119], 44}
				HelloWorld[TYPE_REF]{HelloWorld, , LHelloWorld;, null, [119, 119], 49}
				clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, [119, 119], 49}
				equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, [119, 119], 49}
				getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, [119, 119], 49}
				new[KEYWORD]{new, null, null, new, [109, 119], 49}
				null[KEYWORD]{null, null, null, null, [109, 119], 49}
				super[KEYWORD]{super, null, null, super, [109, 119], 49}
				this[KEYWORD]{this, null, null, this, [109, 119], 49}
				toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, [119, 119], 49}
				AAA[FIELD_REF]{AAA, LHelloWorld;, I, AAA, [119, 119], 79}
				BBB[FIELD_REF]{BBB, LHelloWorld;, I, BBB, [119, 119], 79}
				hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, [119, 119], 79}""",
				requestor.getResults());
	}

	@Test
	public void testCompleteEnumAlreadyQualified() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
				"""
				public class HelloWorld  {
					public enum MyEnum {
						ONE, TWO, THREE;
					}
					public void myMethod() {
						MyEnum val = MyEnum.
					}
				}
				""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "MyEnum.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		var expectedContent= "ONE[FIELD_REF]{ONE, LHelloWorld$MyEnum;, LHelloWorld$MyEnum;, ";
		Assert.assertTrue(requestor.getResults().contains(expectedContent));
	}

	@Test
	public void testCompleteBeforeDereference() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
			"""
			public class HelloWorld  {
				void m() {
				String s = "";
				s.
				m();
			}
			}
			""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		var expectedContent= "getChars[METHOD_REF]{getChars(), Ljava.lang.String;, (II[CI)V, ";
		Assert.assertTrue(requestor.getResults().contains(expectedContent));
	}

	@Test
	public void testCompleteYieldStatement() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("HelloWorld.java",
			"""
			public class HelloWorld  {
				public static enum Color { RED, GREEN, BLUE; }
				public static class NaturalGas {}
				public static class NaturalHistory {}
				void m() {
					Color c = null;
					int naturalize = 2;
					String naturalist = "sandiwch";
					int i = switch (c) {
						case BLUE: {
							if (1<2) {
								yield ;
							}
						}
						default -> 1;
					};
				}
			}
			""");

		// add the replace range to the results
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, true);

		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		IProgressMonitor monitor = new NullProgressMonitor();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, WC_OWNER, monitor);
		Assert.assertTrue(requestor.getReversedResults().startsWith("""
				hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, [301, 301], 82}
				naturalize[LOCAL_VARIABLE_REF]{naturalize, null, I, naturalize, [301, 301], 82}
				"""));
	}


}
