/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CompletionWithMissingTypesTests2 extends ModifyingResourceTests implements RelevanceConstants {

public CompletionWithMissingTypesTests2(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (AbstractJavaModelCompletionTests.COMPLETION_PROJECT == null)  {
		AbstractJavaModelCompletionTests.COMPLETION_PROJECT = setUpJavaProject("Completion");
	} else {
		setUpProjectCompliance(AbstractJavaModelCompletionTests.COMPLETION_PROJECT, "1.4");
		this.currentProject = AbstractJavaModelCompletionTests.COMPLETION_PROJECT;
	}
	super.setUpSuite();
}
@Override
public void tearDownSuite() throws Exception {
	if (AbstractJavaModelCompletionTests.COMPLETION_SUITES == null) {
		deleteProject("Completion");
	} else {
		AbstractJavaModelCompletionTests.COMPLETION_SUITES.remove(getClass());
		if (AbstractJavaModelCompletionTests.COMPLETION_SUITES.size() == 0) {
			deleteProject("Completion");
			AbstractJavaModelCompletionTests.COMPLETION_SUITES = null;
		}
	}
	if (AbstractJavaModelCompletionTests.COMPLETION_SUITES == null) {
		AbstractJavaModelCompletionTests.COMPLETION_PROJECT = null;
	}
	super.tearDownSuite();
}

protected static void assertResults(String expected, String actual) {
	try {
		assertEquals(expected, actual);
	} catch(ComparisonFailure c) {
		System.out.println(actual);
		System.out.println();
		throw c;
	}
}
static {
//	TESTS_NAMES = new String[] { "testBug96950" };
}
public static Test suite() {
	return buildModelTestSuite(CompletionWithMissingTypesTests2.class);
}

File createFile(File parent, String name, String content) throws IOException {
	File file = new File(parent, name);
	FileOutputStream out = new FileOutputStream(file);
	try  {
		out.write(content.getBytes());
	} finally {
		out.close();
	}
	return file;
}
File createDirectory(File parent, String name) {
	File dir = new File(parent, name);
	dir.mkdirs();
	return dir;
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0001() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.DISABLED);
		options.put(JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		// create variable
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			 "bin");

		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX.java",
				"package a;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX.java",
				"package b;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");

		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"a/*"}},
			new boolean[]{false},
			"bin",
			null,
			null,
			null,
			"1.4");
		this.createFile(
			"/P2/src/YY.java",
			"public class YY {\n"+
			"  void foo() {\n"+
			"    XX x = null;\n"+
			"    x.fo\n"+
			"  }\n"+
			"}");

		waitUntilIndexesReady();

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");

		String str = cu.getSource();
		String completeBehind = "x.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("x.fo") + "x.".length();
		int end1 = start1 + "fo".length();
		int start2 = str.lastIndexOf("XX");
		int end2 = start2 + "XX".length();
		assertResults(
				"foo[METHOD_REF]{foo(), La.XX;, ()V, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   XX[TYPE_REF]{a.XX, a, La.XX;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
				"foo[METHOD_REF]{foo(), Lb.XX;, ()V, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   XX[TYPE_REF]{b.XX, b, Lb.XX;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0002() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		options.put(JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		// create variable
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			 "bin");

		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX.java",
				"package a;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX.java",
				"package b;\n"+
				"public class XX {\n"+
				"  void foo() {}\n"+
				"}");

		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"a/*"}},
			new boolean[]{false},
			"bin",
			null,
			null,
			null,
			"1.4");
		this.createFile(
			"/P2/src/YY.java",
			"public class YY {\n"+
			"  void foo() {\n"+
			"    XX x = null;\n"+
			"    x.fo\n"+
			"  }\n"+
			"}");

		waitUntilIndexesReady();

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");

		String str = cu.getSource();
		String completeBehind = "x.fo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("x.fo") + "x.".length();
		int end1 = start1 + "fo".length();
		int start2 = str.lastIndexOf("XX");
		int end2 = start2 + "XX".length();
		assertResults(
				"foo[METHOD_REF]{foo(), Lb.XX;, ()V, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   XX[TYPE_REF]{b.XX, b, Lb.XX;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
}
