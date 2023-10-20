/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.model.Semaphore.TimeOutException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.processing.IJob;

import junit.framework.Test;

/*
 * Test indexing support.
 */
public class SearchTests extends ModifyingResourceTests implements IJavaSearchConstants {
	/*
	 * Empty jar contents.
	 * Generated using the following code:

	 	String filePath = "d:\\temp\\empty.jar";
		new JarOutputStream(new FileOutputStream(filePath), new Manifest()).close();
		byte[] contents = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(filePath));
		System.out.print("{");
		for (int i = 0, length = contents.length; i < length; i++) {
			System.out.print(contents[i]);
			System.out.print(", ");
		}
		System.out.print("}");
	 */
	static final byte[] EMPTY_JAR = {80, 75, 3, 4, 20, 0, 8, 0, 8, 0, 106, -100, 116, 46, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 4, 0, 77, 69, 84, 65, 45, 73, 78, 70, 47, 77, 65, 78, 73, 70, 69, 83, 84, 46, 77, 70, -2, -54, 0, 0, -29, -27, 2, 0, 80, 75, 7, 8, -84, -123, -94, 20, 4, 0, 0, 0, 2, 0, 0, 0, 80, 75, 1, 2, 20, 0, 20, 0, 8, 0, 8, 0, 106, -100, 116, 46, -84, -123, -94, 20, 4, 0, 0, 0, 2, 0, 0, 0, 20, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 77, 69, 84, 65, 45, 73, 78, 70, 47, 77, 65, 78, 73, 70, 69, 83, 84, 46, 77, 70, -2, -54, 0, 0, 80, 75, 5, 6, 0, 0, 0, 0, 1, 0, 1, 0, 70, 0, 0, 0, 74, 0, 0, 0, 0, 0, };
	static class WaitUntilReadyMonitor implements IProgressMonitor {
		public Semaphore sem = new Semaphore();
		public void beginTask(String name, int totalWork) {
		}
		public void internalWorked(double work) {
		}
		public void done() {
		}
		public boolean isCanceled() {
			return false;
		}
		public void setCanceled(boolean value) {
		}
		public void setTaskName(String name) {
		}
		public void subTask(String name) {
			// concurrent job is signaling it is working
			this.sem.release();
		}
		public void worked(int work) {
		}
	}
	public static class SearchMethodNameRequestor extends MethodNameRequestor {
		List<String> results = new ArrayList<>();
		@Override
		public void acceptMethod(
				char[] methodName,
				int parameterCount,
				char[] declaringQualifier,
				char[] simpleTypeName,
				int typeModifiers,
				char[] packageName,
				char[] signature,
				char[][] parameterTypes,
				char[][] parameterNames,
				char[] returnType,
				int modifiers,
				String path,
				int methodIndex) {
			StringBuilder buffer = new StringBuilder();
			char c = '.';
			char[] noname = new String("<NONAME>").toCharArray();
			buffer.append(path);
			buffer.append(' ');
			buffer.append(returnType == null ? CharOperation.NO_CHAR: returnType);
			buffer.append(' ');
			checkAndAddtoBuffer(buffer, packageName, c);
			checkAndAddtoBuffer(buffer, declaringQualifier, c);
			checkAndAddtoBuffer(buffer, simpleTypeName == null ? noname : simpleTypeName, c);
			buffer.append(methodName);
			buffer.append('(');
			parameterTypes = signature == null ? parameterTypes : Signature.getParameterTypes(signature);

			for (int i = 0; i < parameterCount; i++) {

				if (parameterTypes != null) {
					char[] parameterType;
					if (parameterTypes.length != parameterCount) {
						System.out.println("Error");
					}
					if (signature != null) {
						parameterType = Signature.toCharArray(Signature.getTypeErasure(parameterTypes[i]));
						CharOperation.replace(parameterType, '/', '.');
					} else {
						parameterType = parameterTypes[i];
					}
					buffer.append(parameterType);
				} else {
					buffer.append('?'); // parameter type names are not stored in the indexes
					buffer.append('?');
					buffer.append('?');
				}
				buffer.append(' ');
				if (parameterNames != null) {
					buffer.append(parameterNames[i]);
				} else {
					buffer.append("arg"+i);
				}
				if (parameterCount > 1 && i < parameterCount - 1) buffer.append(',');
			}
			buffer.append(')');
			this.results.add(buffer.toString());
		}
		static void checkAndAddtoBuffer(StringBuilder buffer, char[] precond, char c) {
			if (precond == null || precond.length == 0) return;
			buffer.append(precond);
			buffer.append(c);
		}
		public String toString(){
			int length = this.results.size();
			String[] strings = new String[length];
			this.results.toArray(strings);
			org.eclipse.jdt.internal.core.util.Util.sort(strings);
			StringBuilder buffer = new StringBuilder(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
		public int size() {
			return this.results.size();
		}
	}
	public static class SearchTypeNameRequestor extends TypeNameRequestor {
		List<String> results = new ArrayList<>();
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
			char[] typeName =
				CharOperation.concat(
					CharOperation.concatWith(enclosingTypeNames, '$'),
					simpleTypeName,
					'$');
			this.results.add(new String(CharOperation.concat(packageName, typeName, '.')));
		}
		public String toString(){
			int length = this.results.size();
			String[] strings = new String[length];
			this.results.toArray(strings);
			org.eclipse.jdt.internal.core.util.Util.sort(strings);
			StringBuilder buffer = new StringBuilder(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
		public String unsortedString() {
			int length = this.results.size();
			String[] strings = new String[length];
			this.results.toArray(strings);
			StringBuilder buffer = new StringBuilder(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
		public int size() {
			return this.results.size();
		}
	}
	static class WaitingJob implements IJob {
		private static final int MAX_WAIT = 30000; // wait 30s max
		private final Semaphore startingSem = new Semaphore();
		private final Semaphore runningSem = new Semaphore();
		public boolean belongsTo(String jobFamily) {
			return false;
		}
		public void cancel() {
		}
		public void ensureReadyToRun() {
		}
		public boolean execute(IProgressMonitor progress) {
			this.startingSem.release();
			try {
				this.runningSem.acquire(MAX_WAIT);
			} catch (TimeOutException e) {
				e.printStackTrace();
			}
			return true;
		}
		public String getJobFamily() {
			return "SearchTests.Waiting";
		}
		public void suspend() throws Semaphore.TimeOutException, CoreException {
	 		runAndSuspend(null);
		}
		public void runAndSuspend(IWorkspaceRunnable runnable) throws Semaphore.TimeOutException, CoreException {
	 		IndexManager indexManager = JavaModelManager.getIndexManager();
	 		while(indexManager.isEnabled()) {
	 			indexManager.disable();
	 		}
			if (runnable != null) {
				runnable.run(null);
			}
			indexManager.request(this);
			while(!indexManager.isEnabled()) {
	 			indexManager.enable();
	 		}
			this.startingSem.acquire(30000); // wait for job to start (wait 30s max)
		}

		public void resume() {
			this.runningSem.release();
			JavaModelManager.getIndexManager().enable();
		}
	}
static {
	//TESTS_PREFIX = "testSearchPatternValidateMatchRule";
}
public static Test suite() {
	return buildModelTestSuite(SearchTests.class);
}


public SearchTests(String name) {
	super(name);
}
protected void assertAllTypes(int waitingPolicy, IProgressMonitor progressMonitor, String expected) throws JavaModelException {
	assertAllTypes("Unexpected all types", null/* no specific project*/, waitingPolicy, progressMonitor, expected);
}
protected void assertAllTypes(String expected) throws JavaModelException {
	assertAllTypes(WAIT_UNTIL_READY_TO_SEARCH, null/* no progress monitor*/, expected);
}
protected void assertAllTypes(String message, IJavaProject project, String expected) throws JavaModelException {
	assertAllTypes(message, project, WAIT_UNTIL_READY_TO_SEARCH, null/* no progress monitor*/, expected);
}
protected void assertAllTypes(String message, IJavaProject project, int waitingPolicy, IProgressMonitor progressMonitor, String expected) throws JavaModelException {
	IJavaSearchScope scope =
		project == null ?
			SearchEngine.createWorkspaceScope() :
			SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
	SearchEngine searchEngine = new SearchEngine();
	SearchTypeNameRequestor requestor = new SearchTypeNameRequestor();
	searchEngine.searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		scope,
		requestor,
		waitingPolicy,
		progressMonitor);
	String actual = requestor.toString();
	if (!expected.equals(actual)){
	 	System.out.println(Util.displayString(actual, 3));
	}
	assertEquals(
		message,
		expected,
		actual);
}
protected void assertPattern(String expected, SearchPattern actualPattern) {
	String actual = actualPattern == null ? null : actualPattern.toString();
	if (!expected.equals(actual)) {
		System.out.print(actual == null ? "null" : Util.displayString(actual, 2));
		System.out.println(",");
	}
	assertEquals(
		"Unexpected search pattern",
		expected,
		actual);
}
protected void assertValidMatchRule(String pattern, int rule) {
	assertValidMatchRule(pattern, rule, rule);
}
protected void assertValidMatchRule(String pattern, int rule, int expected) {
	int validated = SearchPattern.validateMatchRule(pattern, rule);
	String givenRule = BasicSearchEngine.getMatchRuleString(rule);
	String validatedRule = BasicSearchEngine.getMatchRuleString(validated);
	String expectedRule = BasicSearchEngine.getMatchRuleString(expected);
	if (!validatedRule.equals(expectedRule)) {
		System.out.println("Test "+getName());
		System.out.print("	assertValidMatchRule(\"");
		System.out.print(pattern);
		System.out.println("\",");
		System.out.print("		SearchPattern.");
		System.out.print(givenRule);
		System.out.println("\",");
		System.out.print("		SearchPattern.");
		System.out.print(validatedRule);
		System.out.println(");");
		assertEquals(pattern+"' does not match expected match rule!", expectedRule, validatedRule);
	}
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	createJavaProject("P");
	createFolder("/P/x/y/z");
	createFile(
		"/P/x/y/z/Foo.java",
		"package x.y,z;\n" +
		"import x.y.*;\n" +
		"import java.util.Vector;\n" +
		"public class Foo {\n" +
		"  int field;\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	createFile(
		"/P/x/y/z/I.java",
		"package x.y,z;\n" +
		"public interface I {\n" +
		"}"
	);
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("P");
	super.tearDownSuite();
}
/*
 * Ensure that changing the classpath in the middle of reindexing
 * a project causes another request to reindex.
 */
public void testChangeClasspath() throws CoreException, TimeOutException {
	boolean indexDisabled = isIndexDisabledForTest();
	if(indexDisabled) {
		enableIndexer();
	}
	WaitingJob job = new WaitingJob();
	try {
		// setup: suspend indexing and create a project (prj=src) with one cu
		job.runAndSuspend(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				JavaCore.run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor2) throws CoreException {
						createJavaProject("P1");
						createFile(
							"/P1/X.java",
							"public class X {\n" +
							"}"
						);
					}
				}, monitor);
			}
		});

		// remove source folder from classpath
		IJavaProject project = getJavaProject("P1");
		project.setRawClasspath(
			new IClasspathEntry[0],
			null);

		// resume waiting job
		job.resume();

		assertAllTypes(
			"Unexpected all types after removing source folder",
			project,
			""
		);
	} finally {
		job.resume();
		deleteProject("P1");
		if(indexDisabled) {
			disableIndexer();
		}
	}
}
/*
 * Ensure that removing a project source folder and adding another source folder removes the existing cus from the index
 * (regression test for bug 73356 Index not updated after adding a source folder
 */
public void testChangeClasspath2() throws CoreException {
	try {
		final IJavaProject project = createJavaProject("P1", new String[] {""}, "bin");
		createFile(
			"/P1/X.java",
			"public class X {}"
		);
		assertAllTypes(
			"Unexpected types before changing the classpath",
			null, // workspace search
			"X\n" +
			"java.io.Serializable\n" +
			"java.lang.Class\n" +
			"java.lang.CloneNotSupportedException\n" +
			"java.lang.Error\n" +
			"java.lang.Exception\n" +
			"java.lang.IllegalMonitorStateException\n" +
			"java.lang.InterruptedException\n" +
			"java.lang.Object\n" +
			"java.lang.RuntimeException\n" +
			"java.lang.String\n" +
			"java.lang.Throwable\n" +
			"x.y.Foo\n" +
			"x.y.I"
		);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createFolder("/P1/src");
				project.setRawClasspath(createClasspath(new String[] {"/P1/src"}, false, false), null);
			}
		}, null);
		assertAllTypes(
			"Unexpected types after changing the classpath",
			null, // workspace search
			"java.io.Serializable\n" +
			"java.lang.Class\n" +
			"java.lang.CloneNotSupportedException\n" +
			"java.lang.Error\n" +
			"java.lang.Exception\n" +
			"java.lang.IllegalMonitorStateException\n" +
			"java.lang.InterruptedException\n" +
			"java.lang.Object\n" +
			"java.lang.RuntimeException\n" +
			"java.lang.String\n" +
			"java.lang.Throwable\n" +
			"x.y.Foo\n" +
			"x.y.I"
		);
	} finally {
		deleteProject("P1");
	}
}
/*
 * Ensure that performing a concurrent job while indexing a jar doesn't use the old index.
 * (regression test for bug 35306 Index update request can be incorrectly handled)
 */
public void testConcurrentJob() throws CoreException, InterruptedException, IOException, TimeOutException {
	WaitingJob job = new WaitingJob();
	try {
		// setup: suspend indexing and create a project with one empty jar on its classpath
		job.runAndSuspend(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				JavaCore.run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor2) throws CoreException {
						createJavaProject("P1", new String[] {}, new String[] {"/P1/jclMin.jar"}, "bin");
						createFile("/P1/jclMin.jar", EMPTY_JAR);
					}
				}, monitor);
			}
		});

		final IJavaProject project = getJavaProject("P1");

		// start concurrent job
		final boolean[] success = new boolean[1];
		final WaitUntilReadyMonitor monitor = new WaitUntilReadyMonitor();
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					assertAllTypes(
						"Unexpected all types",
						project,
						WAIT_UNTIL_READY_TO_SEARCH,
						monitor,
						"java.io.Serializable\n" +
						"java.lang.Class\n" +
						"java.lang.CloneNotSupportedException\n" +
						"java.lang.Error\n" +
						"java.lang.Exception\n" +
						"java.lang.IllegalMonitorStateException\n" +
						"java.lang.InterruptedException\n" +
						"java.lang.Object\n" +
						"java.lang.RuntimeException\n" +
						"java.lang.String\n" +
						"java.lang.Throwable"
					);
				} catch (JavaModelException e) {
					e.printStackTrace();
					return;
				}
				success[0] = true;
			}
		};
		thread.setDaemon(true);
		thread.start();

		// wait for concurrent job to start
		monitor.sem.acquire(30000); // wait 30s max

		// change jar contents
		getFile("/P1/jclMin.jar").setContents(new FileInputStream(getExternalJCLPathString()), IResource.NONE, null);
			// setContents closes the stream

		// resume waiting job
		job.resume();

		// wait for concurrent job to finish
		thread.join(10000); // 10s max

		assertTrue("Failed to get all types", success[0]);

	} finally {
		job.resume();
		deleteProject("P1");
	}
}
/*
 * Ensures that passing a null progress monitor with a CANCEL_IF_NOT_READY_TO_SEARCH
 * waiting policy doesn't throw a NullPointerException but an OperationCanceledException.
 * (regression test for bug 33571 SearchEngine.searchAllTypeNames: NPE when passing null as progress monitor)
 */
 public void testNullProgressMonitor() throws CoreException, TimeOutException {
	WaitingJob job = new WaitingJob();
 	try {
 		job.suspend();

		// query all type names with a null progress monitor
		boolean operationCanceled = false;
		try {
			assertAllTypes(
				CANCEL_IF_NOT_READY_TO_SEARCH,
				null, // null progress monitor
				"Should not get any type"
			);
		} catch (OperationCanceledException e) {
			operationCanceled = true;
		}
		assertTrue("Should throw an OperationCanceledException", operationCanceled);
 	} finally {
 		job.resume();
 	}
 }
 /*
  * Ensures that types are found if the project is a lib folder
  * (regression test for bug 83822 Classes at root of project not found in Open Type dialog)
  */
 public void testProjectLib() throws CoreException {
 	try {
 		IJavaProject javaProject = createJavaProject("P1", new String[0], new String[] {"/P1"}, "bin");
 		createClassFile("/P1", "X.class", "public class X {}");
 		IProject project = javaProject.getProject();
 		project.close(null);
 		waitUntilIndexesReady();
 		project.open(null);
 		assertAllTypes(
 			"Unexpected types in P1",
 			javaProject,
 			"X"
 		);
 	} finally {
 		deleteProject("P1");
 	}
 }
/*
 * Ensure that removing the outer folder from the classpath doesn't remove cus in inner folder
 * from index
 * (regression test for bug 32607 Removing outer folder removes nested folder's cus from index)
 */
public void testRemoveOuterFolder() throws CoreException {
	try {
		// setup: one cu in a nested source folder
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IJavaProject project = createJavaProject("P1");
				project.setRawClasspath(
					createClasspath(new String[] {"/P1/src1", "src2/", "/P1/src1/src2", ""}, false/*no inclusion*/, true/*exclusion*/),
					new Path("/P1/bin"),
					null);
				createFolder("/P1/src1/src2");
				createFile(
					"/P1/src1/src2/X.java",
					"public class X {\n" +
					"}"
				);
			}
		}, null);
		IJavaProject project = getJavaProject("P1");
		assertAllTypes(
			"Unexpected all types after setup",
			project,
			"X"
		);

		// remove outer folder from classpath
		project.setRawClasspath(
			createClasspath(new String[] {"/P1/src1/src2", ""}, false/*no inclusion*/, true/*exclusion*/),
			null);
		assertAllTypes(
			"Unexpected all types after removing outer folder",
			project,
			"X"
		);

	} finally {
		deleteProject("P1");
	}
}
/**
 * Test pattern creation
 */
public void testSearchPatternCreation01() {

	SearchPattern searchPattern = createPattern(
			"main(*)",
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"MethodReferencePattern: main(*), pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation02() {

	SearchPattern searchPattern = createPattern(
			"main(*) void",
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"MethodReferencePattern: main(*) --> void, pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation03() {

	SearchPattern searchPattern = createPattern(
			"main(String*) void",
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"MethodReferencePattern: main(String*) --> void, pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation04() {

	SearchPattern searchPattern = createPattern(
			"main(*[])",
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"MethodReferencePattern: main(*[]), pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation05() {

	SearchPattern searchPattern = createPattern(
			"java.lang.*.main ",
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"MethodReferencePattern: java.lang.*.main(...), pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation06() {

	SearchPattern searchPattern = createPattern(
			"java.lang.* ",
			IJavaSearchConstants.CONSTRUCTOR,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"ConstructorReferencePattern: java.lang.*(...), pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation07() {

	SearchPattern searchPattern = createPattern(
			"X(*,*)",
			IJavaSearchConstants.CONSTRUCTOR,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"ConstructorReferencePattern: X(*, *), pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation08() {

	SearchPattern searchPattern = createPattern(
			"main(String*,*) void",
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			true); // case sensitive

	assertPattern(
		"MethodReferencePattern: main(String*, *) --> void, pattern match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation10() {

	SearchPattern searchPattern = createPattern(
			"x.y.z.Bar.field Foo",
			IJavaSearchConstants.FIELD,
			IJavaSearchConstants.DECLARATIONS,
			true); // case sensitive

	assertPattern(
		"FieldDeclarationPattern: x.y.z.Bar.field --> Foo, exact match, case sensitive, generic full match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation12() {
	IField field = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getField("field");
	SearchPattern searchPattern = createPattern(
			field,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"FieldReferencePattern: x.y.z.Foo.field --> int, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation13() {
	IField field = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getField("field");
	SearchPattern searchPattern = createPattern(
			field,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"FieldDeclarationPattern: x.y.z.Foo.field --> int, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation14() {
	IField field = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getField("field");
	SearchPattern searchPattern = createPattern(
			field,
			IJavaSearchConstants.ALL_OCCURRENCES);

	assertPattern(
		"FieldCombinedPattern: x.y.z.Foo.field --> int, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation15() {
	IImportDeclaration importDecl = getCompilationUnit("/P/x/y/z/Foo.java").getImport("x.y.*");
	SearchPattern searchPattern = createPattern(
			importDecl,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"PackageReferencePattern: <x.y>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation16() {
	IMethod method = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getMethod("bar", new String[] {});
	SearchPattern searchPattern = createPattern(
			method,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"MethodDeclarationPattern: x.y.z.Foo.bar() --> void, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation17() {
	IMethod method = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getMethod("bar", new String[] {});
	SearchPattern searchPattern = createPattern(
			method,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"MethodReferencePattern: x.y.z.Foo.bar() --> void, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation18() {
	IMethod method = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getMethod("bar", new String[] {});
	SearchPattern searchPattern = createPattern(
			method,
			IJavaSearchConstants.ALL_OCCURRENCES);

	assertPattern(
		"MethodCombinedPattern: x.y.z.Foo.bar() --> void, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation19() {
	IType type = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo");
	SearchPattern searchPattern = createPattern(
			type,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"TypeDeclarationPattern: pkg<x.y.z>, enclosing<>, type<Foo>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation20() {
	IType type = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo");
	SearchPattern searchPattern = createPattern(
			type,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"TypeReferencePattern: qualification<x.y.z>, type<Foo>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation21() {
	IType type = getCompilationUnit("/P/x/y/z/I.java").getType("I");
	SearchPattern searchPattern = createPattern(
			type,
			IJavaSearchConstants.IMPLEMENTORS);

	assertPattern(
		"SuperInterfaceReferencePattern: <I>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation22() {
	IType type = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo");
	SearchPattern searchPattern = createPattern(
			type,
			IJavaSearchConstants.ALL_OCCURRENCES);

	assertPattern(
		"TypeDeclarationPattern: pkg<x.y.z>, enclosing<>, type<Foo>, exact match, case sensitive, generic erasure match, fine grain: none\n" +
		"| TypeReferencePattern: qualification<x.y.z>, type<Foo>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation23() {
	IPackageDeclaration pkg = getCompilationUnit("/P/x/y/z/Foo.java").getPackageDeclaration("x.y.z");
	SearchPattern searchPattern = createPattern(
			pkg,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"PackageReferencePattern: <x.y.z>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation24() {
	IPackageFragment pkg = getPackage("/P/x/y/z");
	SearchPattern searchPattern = createPattern(
			pkg,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"PackageReferencePattern: <x.y.z>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation25() {
	IImportDeclaration importDecl = getCompilationUnit("/P/x/y/z/Foo.java").getImport("java.util.Vector");
	SearchPattern searchPattern = createPattern(
			importDecl,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"TypeReferencePattern: qualification<java.util>, type<Vector>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation26() {
	IPackageFragment pkg = getPackage("/P/x/y/z");
	SearchPattern searchPattern = createPattern(
			pkg,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"PackageDeclarationPattern: <x.y.z>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation27() {
	IPackageDeclaration pkg = getCompilationUnit("/P/x/y/z/Foo.java").getPackageDeclaration("x.y.z");
	SearchPattern searchPattern = createPattern(
			pkg,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"PackageDeclarationPattern: <x.y.z>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation28() {
	IImportDeclaration importDecl = getCompilationUnit("/P/x/y/z/Foo.java").getImport("x.y.*");
	SearchPattern searchPattern = createPattern(
			importDecl,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"PackageDeclarationPattern: <x.y>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation29() {
	IPackageFragment pkg = getPackage("/P/x/y/z");
	SearchPattern searchPattern = createPattern(
			pkg,
			IJavaSearchConstants.ALL_OCCURRENCES);

	assertPattern(
		"PackageDeclarationPattern: <x.y.z>, exact match, case sensitive, generic erasure match, fine grain: none\n" +
		"| PackageReferencePattern: <x.y.z>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test LocalVarDeclarationPattern creation
 */
public void testSearchPatternCreation30() {
	ILocalVariable localVar = new LocalVariable((JavaElement)getCompilationUnit("/P/X.java").getType("X").getMethod("foo", new String[0]),  "var", 1, 2, 3, 4, "Z", null, 0, false);
	SearchPattern searchPattern = createPattern(
			localVar,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"LocalVarDeclarationPattern: var [in foo() [in X [in X.java [in <default> [in <project root> [in P]]]]]], exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test LocalVarReferencePattern creation
 */
public void testSearchPatternCreation31() {
	ILocalVariable localVar = new LocalVariable((JavaElement)getCompilationUnit("/P/X.java").getType("X").getMethod("foo", new String[0]),  "var", 1, 2, 3, 4, "Z", null, 0, false);
	SearchPattern searchPattern = createPattern(
			localVar,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"LocalVarReferencePattern: var [in foo() [in X [in X.java [in <default> [in <project root> [in P]]]]]], exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test LocalVarCombinedPattern creation
 */
public void testSearchPatternCreation32() {
	ILocalVariable localVar = new LocalVariable((JavaElement)getCompilationUnit("/P/X.java").getType("X").getMethod("foo", new String[0]),  "var", 1, 2, 3, 4, "Z", null, 0, false);
	SearchPattern searchPattern = createPattern(
			localVar,
			IJavaSearchConstants.ALL_OCCURRENCES);

	assertPattern(
		"LocalVarCombinedPattern: var [in foo() [in X [in X.java [in <default> [in <project root> [in P]]]]]], exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test TypeDeclarationPattern creation
 */
public void testSearchPatternCreation33() {
	IType localType = getCompilationUnit("/P/X.java").getType("X").getMethod("foo", new String[0]).getType("Y", 2);
	SearchPattern searchPattern = createPattern(
			localType,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"TypeDeclarationPattern: pkg<>, enclosing<X.*>, type<Y>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test TypeReferencePattern creation
 */
public void testSearchPatternCreation34() {
	IType localType = getCompilationUnit("/P/X.java").getType("X").getMethod("foo", new String[0]).getType("Y", 3);
	SearchPattern searchPattern = createPattern(
			localType,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"TypeReferencePattern: qualification<X.*>, type<Y>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test TypeDeclarationPattern creation
 */
public void testSearchPatternCreation35() {
	IType localType = getCompilationUnit("/P/X.java").getType("X").getInitializer(1).getType("Y", 2);
	SearchPattern searchPattern = createPattern(
			localType,
			IJavaSearchConstants.DECLARATIONS);

	assertPattern(
		"TypeDeclarationPattern: pkg<>, enclosing<X.*>, type<Y>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Test TypeReferencePattern creation
 */
public void testSearchPatternCreation36() {
	IType localType = getCompilationUnit("/P/X.java").getType("X").getInitializer(2).getType("Y", 3);
	SearchPattern searchPattern = createPattern(
			localType,
			IJavaSearchConstants.REFERENCES);

	assertPattern(
		"TypeReferencePattern: qualification<X.*>, type<Y>, exact match, case sensitive, generic erasure match, fine grain: none",
		searchPattern);
}

/**
 * Fine grain patterns
 */
public void testSearchPatternCreation37() {
	int allFlags = 0xFFFFFFF0;
	SearchPattern searchPattern = createPattern("*", TYPE, allFlags, true);
	assertPattern(
		"TypeReferencePattern: qualification<*>, type<*>, pattern match, case sensitive, generic full match, " +
		"fine grain: FIELD_DECLARATION_TYPE_REFERENCE | " +
		"LOCAL_VARIABLE_DECLARATION_TYPE_REFERENCE | " +
		"PARAMETER_DECLARATION_TYPE_REFERENCE | " +
		"SUPERTYPE_TYPE_REFERENCE | " +
		"THROWS_CLAUSE_TYPE_REFERENCE | " +
		"CAST_TYPE_REFERENCE | " +
		"CATCH_TYPE_REFERENCE | " +
		"CLASS_INSTANCE_CREATION_TYPE_REFERENCE | " +
		"RETURN_TYPE_REFERENCE | " +
		"IMPORT_DECLARATION_TYPE_REFERENCE | " +
		"ANNOTATION_TYPE_REFERENCE | " +
		"TYPE_ARGUMENT_TYPE_REFERENCE | " +
		"TYPE_VARIABLE_BOUND_TYPE_REFERENCE | " +
		"WILDCARD_BOUND_TYPE_REFERENCE | " +
		" | " + // unused slots
		" | " +
		" | " +
		" | " +
		"SUPER_REFERENCE | " +
		"QUALIFIED_REFERENCE | " +
		"THIS_REFERENCE | " +
		"IMPLICIT_THIS_REFERENCE | " +
		"METHOD_REFERENCE_EXPRESSION | " +
		"PERMITTYPE_TYPE_REFERENCE | " + // unused slots
		" | ",
		searchPattern);
}

/**
 * Test pattern  validation
 */
public void testSearchPatternValidMatchRule01() {
	assertValidMatchRule("foo",
		SearchPattern.R_EXACT_MATCH,
		SearchPattern.R_EXACT_MATCH);
}
public void testSearchPatternValidMatchRule02() {
	assertValidMatchRule("foo",
		SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_PREFIX_MATCH);
}
public void testSearchPatternValidMatchRule03() {
	assertValidMatchRule("foo",
		SearchPattern.R_PATTERN_MATCH,
		SearchPattern.R_EXACT_MATCH);
}
public void testSearchPatternValidMatchRule04() {
	assertValidMatchRule("foo",
		SearchPattern.R_PATTERN_MATCH | SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_PREFIX_MATCH);
}
public void testSearchPatternValidMatchRule05() {
	assertValidMatchRule("foo",
		SearchPattern.R_CAMELCASE_MATCH,
		SearchPattern.R_PREFIX_MATCH);
}
public void testSearchPatternValidMatchRule06() {
	assertValidMatchRule("foo",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH,
		SearchPattern.R_EXACT_MATCH);
}
public void testSearchPatternValidMatchRule10() {
	assertValidMatchRule("CP*P",
		SearchPattern.R_EXACT_MATCH,
		SearchPattern.R_PATTERN_MATCH);
}
public void testSearchPatternValidMatchRule11() {
	assertValidMatchRule("CP*P",
		SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_PATTERN_MATCH);
}
public void testSearchPatternValidMatchRule12() {
	assertValidMatchRule("CP*P",
		SearchPattern.R_PATTERN_MATCH,
		SearchPattern.R_PATTERN_MATCH);
}
public void testSearchPatternValidMatchRule13() {
	assertValidMatchRule("CP*P",
		SearchPattern.R_PATTERN_MATCH | SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_PATTERN_MATCH);
}
public void testSearchPatternValidMatchRule14() {
	assertValidMatchRule("CP*P",
		SearchPattern.R_CAMELCASE_MATCH,
		SearchPattern.R_PATTERN_MATCH);
}
public void testSearchPatternValidMatchRule15() {
	assertValidMatchRule("CP*P",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH,
		SearchPattern.R_PATTERN_MATCH);
}
public void testSearchPatternValidMatchRule20() {
	assertValidMatchRule("NPE",
		SearchPattern.R_CAMELCASE_MATCH);
}
public void testSearchPatternValidMatchRule21() {
	assertValidMatchRule("NPE",
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE,
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
}
public void testSearchPatternValidMatchRule22() {
	assertValidMatchRule("nPE",
		SearchPattern.R_CAMELCASE_MATCH);
}
public void testSearchPatternValidMatchRule23() {
	assertValidMatchRule("NuPoEx",
		SearchPattern.R_CAMELCASE_MATCH);
}
public void testSearchPatternValidMatchRule24() {
	assertValidMatchRule("oF",
		SearchPattern.R_CAMELCASE_MATCH);
}
public void testSearchPatternValidMatchRule30() {
	assertValidMatchRule("NPE",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
}
public void testSearchPatternValidMatchRule31() {
	assertValidMatchRule("NPE",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
}
public void testSearchPatternValidMatchRule32() {
	assertValidMatchRule("NPE",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
}
public void testSearchPatternValidMatchRule33() {
	assertValidMatchRule("NPE",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE,
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
}
public void testSearchPatternValidMatchRule34() {
	assertValidMatchRule("nPE",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
}
public void testSearchPatternValidMatchRule35() {
	assertValidMatchRule("NuPoEx",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
}
public void testSearchPatternValidMatchRule36() {
	assertValidMatchRule("oF",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
}
public void testSearchPatternValidMatchRule40() {
	assertValidMatchRule("Nu/Po/Ex",
		SearchPattern.R_CAMELCASE_MATCH,
		SearchPattern.R_PREFIX_MATCH);
}
public void testSearchPatternValidMatchRule41() {
	assertValidMatchRule("Nu.Po.Ex",
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_PREFIX_MATCH);
}
public void testSearchPatternValidMatchRule42() {
	assertValidMatchRule("hashMap",
		SearchPattern.R_CAMELCASE_MATCH);
}
public void testSearchPatternValidMatchRule43() {
	assertValidMatchRule("Hashmap",
		SearchPattern.R_CAMELCASE_MATCH,
		SearchPattern.R_PREFIX_MATCH);
}
public void testSearchPatternValidMatchRule44() {
	assertValidMatchRule("Nu/Po/Ex",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH,
		SearchPattern.R_EXACT_MATCH);
}
public void testSearchPatternValidMatchRule45() {
	assertValidMatchRule("Nu.Po.Ex",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH,
		SearchPattern.R_EXACT_MATCH);
}
public void testSearchPatternValidMatchRule46() {
	assertValidMatchRule("hashMap",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
}
public void testSearchPatternValidMatchRule47() {
	assertValidMatchRule("Hashmap",
		SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH,
		SearchPattern.R_EXACT_MATCH);
}
}
