/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class CompletionTests2 extends ModifyingResourceTests implements RelevanceConstants {

public CompletionTests2(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Completion");
}
public void tearDownSuite() throws Exception {
	deleteProject("Completion");
	
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

public static Test suite() {
	TestSuite suite = new Suite(CompletionTests2.class.getName());
	
	suite.addTest(new CompletionTests2("testBug29832"));
	suite.addTest(new CompletionTests2("testBug33560"));
	suite.addTest(new CompletionTests2("testAccessRestriction1"));
	suite.addTest(new CompletionTests2("testAccessRestriction2"));
	suite.addTest(new CompletionTests2("testAccessRestriction3"));
	suite.addTest(new CompletionTests2("testAccessRestriction4"));
	suite.addTest(new CompletionTests2("testAccessRestriction5"));
	suite.addTest(new CompletionTests2("testAccessRestriction6"));
	suite.addTest(new CompletionTests2("testAccessRestriction7"));
	suite.addTest(new CompletionTests2("testAccessRestriction8"));
	suite.addTest(new CompletionTests2("testAccessRestriction9"));
	suite.addTest(new CompletionTests2("testAccessRestriction10"));
	suite.addTest(new CompletionTests2("testAccessRestriction11"));
	suite.addTest(new CompletionTests2("testAccessRestriction12"));
	return suite;
}

File createFile(File parent, String name, String content) throws IOException {
	File file = new File(parent, name);
	FileOutputStream out = new FileOutputStream(file);
	out.write(content.getBytes());
	out.close();
	return file;
}
File createDirectory(File parent, String name) {
	File dir = new File(parent, name);
	dir.mkdirs();
	return dir;
}
/**
 * Test for bug 29832
 */
public void testBug29832() throws Exception {
	try {
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		IFile f = getFile("/Completion/lib.jar");
		IJavaProject p = this.createJavaProject(
			"P1",
			new String[]{},
			Util.getJavaClassLibs(),
			 "");
		this.createFile("/P1/lib.jar", f.getContents());
		this.addLibraryEntry(p, "/P1/lib.jar", true);
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			new String[]{"/P1"},
			"bin");
		this.createFile(
			"/P2/src/X.java",
			"public class X {\n"+
			"  ZZZ z;\n"+
			"}");
		
		// do completion
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "ZZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
			requestor.getResults());
		
		
		// delete P1
		p.getProject().delete(true, false, null);
		
		// create P1
		File dest = getWorkspaceRoot().getLocation().toFile();
		File pro = this.createDirectory(dest, "P1");
		
		this.createFile(pro, ".classpath", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"var\" path=\"JCL_LIB\" sourcepath=\"JCL_SRC\" rootpath=\"JCL_SRCROOT\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>");
			
		this.createFile(pro, ".project", 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<projectDescription>\n" +
			"	<name>org.eclipse.jdt.core</name>\n" +
			"	<comment></comment>\n" +
			"	<projects>\n" +
			"	</projects>\n" +
			"	<buildSpec>\n" +
			"		<buildCommand>\n" +
			"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
			"			<arguments>\n" +
			"			</arguments>\n" +
			"		</buildCommand>\n" +
			"	</buildSpec>\n" +
			"	<natures>\n" +
			"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
			"	</natures>\n" +
			"</projectDescription>");
		
		File src = this.createDirectory(pro, "src");
		
		File pz = this.createDirectory(src, "pz");
		
		this.createFile(pz, "ZZZ.java",
			"package pz;\n" +
			"public class ZZZ {\n" +
			"}");
		
		final IProject project = getWorkspaceRoot().getProject("P1");
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(populate, null);
		JavaCore.create(project);
		
		
		// do completion
		requestor = new CompletionTestsRequestor();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/**
 * Test for bug 33560
 */
public void testBug33560() throws Exception {
	try {
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		IFile f = getFile("/Completion/lib.jar");
		IJavaProject p = this.createJavaProject(
			"P1",
			new String[]{},
			Util.getJavaClassLibs(),
			 "");
		this.createFile("/P1/lib.jar", f.getContents());
		this.addLibraryEntry(p, "/P1/lib.jar", true);
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			new String[]{"/P1"},
			new boolean[]{true},
			"bin");
					
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			new String[]{"/P2"},
			"bin");
		this.createFile(
			"/P3/src/X.java",
			"public class X {\n"+
			"  ZZZ z;\n"+
			"}");
		
		// do completion
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("P3", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "ZZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
			requestor.getResults());
		
		
		// delete P1
		p.getProject().delete(true, false, null);
		
		// create P1
		File dest = getWorkspaceRoot().getLocation().toFile();
		File pro = this.createDirectory(dest, "P1");
		
		this.createFile(pro, ".classpath", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"var\" path=\"JCL_LIB\" sourcepath=\"JCL_SRC\" rootpath=\"JCL_SRCROOT\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>");
			
		this.createFile(pro, ".project", 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<projectDescription>\n" +
			"	<name>org.eclipse.jdt.core</name>\n" +
			"	<comment></comment>\n" +
			"	<projects>\n" +
			"	</projects>\n" +
			"	<buildSpec>\n" +
			"		<buildCommand>\n" +
			"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
			"			<arguments>\n" +
			"			</arguments>\n" +
			"		</buildCommand>\n" +
			"	</buildSpec>\n" +
			"	<natures>\n" +
			"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
			"	</natures>\n" +
			"</projectDescription>");
		
		File src = this.createDirectory(pro, "src");
		
		File pz = this.createDirectory(src, "pz");
		
		this.createFile(pz, "ZZZ.java",
			"package pz;\n" +
			"public class ZZZ {\n" +
			"}");
		
		final IProject project = getWorkspaceRoot().getProject("P1");
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(populate, null);
		JavaCore.create(project);
		
		
		// do completion
		requestor = new CompletionTestsRequestor();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
	}
}
public void testAccessRestriction1() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			new String[]{"/P1"},
			"bin");
		this.createFile(
			"/P2/src/YY.java",
			"public class YY {\n"+
			"  void foo() {\n"+
			"    XX\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}

public void testAccessRestriction2() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
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
			"    XX\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction3() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
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
			"    XX\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction4() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
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
			"    XX\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction5() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
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
			"    XX\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction6() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		this.createFolder("/P1/src/c");
		this.createFile(
				"/P1/src/c/XX3.java",
				"package c;\n"+
				"public class XX3 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"a/*"}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P2"},
			new String[][]{{}},
			new String[][]{{"b/*"}},
			new boolean[]{false},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFile(
			"/P3/src/YY.java",
			"public class YY {\n"+
			"  void foo() {\n"+
			"    XX\n"+
			"  }\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P3", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX3[TYPE_REF]{c.XX3, c, Lc.XX3;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction7() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1", "/P3"},
			new String[][]{{}, {}},
			new String[][]{{"a/*"}, {}},
			new boolean[]{false, false},
			"bin",
			null,
			null,
			null,
			"1.4");
		this.createFile(
				"/P2/src/YY.java",
				"public class YY {\n"+
				"  void foo() {\n"+
				"    XX\n"+
				"  }\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction8() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");

		this.createFolder("/P1/src/b");
		this.createFile(
				"/P1/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P3", "/P1"},
			new String[][]{{}, {}},
			new String[][]{{}, {"a/*"}},
			new boolean[]{false, false},
			"bin",
			null,
			null,
			null,
			"1.4");
		this.createFile(
				"/P2/src/YY.java",
				"public class YY {\n"+
				"  void foo() {\n"+
				"    XX\n"+
				"  }\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction9() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/p11");
		this.createFile(
				"/P1/src/p11/XX11.java",
				"package p11;\n"+
				"public class XX11 {\n"+
				"}");
		
		this.createFolder("/P1/src/p12");
		this.createFile(
				"/P1/src/p12/XX12.java",
				"package p12;\n"+
				"public class XX12 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1", "/P3"},
			new String[][]{{}, {}},
			new String[][]{{"p11/*"}, {"p31/*"}},
			new boolean[]{true, true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P2/src/p21");
		this.createFile(
				"/P2/src/p21/XX21.java",
				"package p21;\n"+
				"public class XX21 {\n"+
				"}");
		
		this.createFolder("/P2/src/p22");
		this.createFile(
				"/P2/src/p22/XX22.java",
				"package p22;\n"+
				"public class XX22 {\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"p12/*"}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P3/src/p31");
		this.createFile(
				"/P3/src/p31/XX31.java",
				"package p31;\n"+
				"public class XX31 {\n"+
				"}");
		
		this.createFolder("/P3/src/p32");
		this.createFile(
				"/P3/src/p32/XX32.java",
				"package p32;\n"+
				"public class XX32 {\n"+
				"}");
		
		// create PX
		this.createJavaProject(
				"PX",
				new String[]{"src"},
				Util.getJavaClassLibs(),
				null,
				null,
				new String[]{"/P2"},
				null,
				null,
				new boolean[]{false},
				"bin",
				null,
				null,
				null,
				"1.4");
		
		this.createFile(
				"/PX/src/X.java",
				"public class X {\n"+
				"  void foo() {\n"+
				"    XX\n"+
				"  }\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("PX", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX12[TYPE_REF]{p12.XX12, p12, Lp12.XX12;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{p21.XX21, p21, Lp21.XX21;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{p22.XX22, p22, Lp22.XX22;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{p32.XX32, p32, Lp32.XX32;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		this.deleteProject("PX");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction10() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/p11");
		this.createFile(
				"/P1/src/p11/XX11.java",
				"package p11;\n"+
				"public class XX11 {\n"+
				"}");
		
		this.createFolder("/P1/src/p12");
		this.createFile(
				"/P1/src/p12/XX12.java",
				"package p12;\n"+
				"public class XX12 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1", "/P3"},
			new String[][]{{}, {}},
			new String[][]{{"p11/*"}, {"p31/*"}},
			new boolean[]{true, true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P2/src/p21");
		this.createFile(
				"/P2/src/p21/XX21.java",
				"package p21;\n"+
				"public class XX21 {\n"+
				"}");
		
		this.createFolder("/P2/src/p22");
		this.createFile(
				"/P2/src/p22/XX22.java",
				"package p22;\n"+
				"public class XX22 {\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"p12/*"}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P3/src/p31");
		this.createFile(
				"/P3/src/p31/XX31.java",
				"package p31;\n"+
				"public class XX31 {\n"+
				"}");
		
		this.createFolder("/P3/src/p32");
		this.createFile(
				"/P3/src/p32/XX32.java",
				"package p32;\n"+
				"public class XX32 {\n"+
				"}");
		
		// create PX
		this.createJavaProject(
				"PX",
				new String[]{"src"},
				Util.getJavaClassLibs(),
				null,
				null,
				new String[]{"/P2"},
				null,
				null,
				new boolean[]{false},
				"bin",
				null,
				null,
				null,
				"1.4");
		
		this.createFile(
				"/PX/src/X.java",
				"public class X {\n"+
				"  void foo() {\n"+
				"    XX\n"+
				"  }\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("PX", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX11[TYPE_REF]{p11.XX11, p11, Lp11.XX11;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE) + "}\n" +
			"XX31[TYPE_REF]{p31.XX31, p31, Lp31.XX31;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE) + "}\n" +
			"XX12[TYPE_REF]{p12.XX12, p12, Lp12.XX12;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{p21.XX21, p21, Lp21.XX21;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{p22.XX22, p22, Lp22.XX22;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{p32.XX32, p32, Lp32.XX32;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		this.deleteProject("PX");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction11() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/x/y/z/p11");
		this.createFile(
				"/P1/src/x/y/z/p11/XX11.java",
				"package x.y.z.p11;\n"+
				"public class XX11 {\n"+
				"}");
		
		this.createFolder("/P1/src/x/y/z/p12");
		this.createFile(
				"/P1/src/x/y/z/p12/XX12.java",
				"package x.y.z.p12;\n"+
				"public class XX12 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P3", "/P1"},
			new String[][]{{}, {}},
			new String[][]{{"x/y/z/p31/*"}, {"x/y/z/p11/*"}},
			new boolean[]{true, true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P2/src/x/y/z/p21");
		this.createFile(
				"/P2/src/x/y/z/p21/XX21.java",
				"package x.y.z.p21;\n"+
				"public class XX21 {\n"+
				"}");
		
		this.createFolder("/P2/src/x/y/z/p22");
		this.createFile(
				"/P2/src/x/y/z/p22/XX22.java",
				"package x.y.z.p22;\n"+
				"public class XX22 {\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"x/y/z/p12/*"}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P3/src/x/y/z/p31");
		this.createFile(
				"/P3/src/x/y/z/p31/XX31.java",
				"package x.y.z.p31;\n"+
				"public class XX31 {\n"+
				"}");
		
		this.createFolder("/P3/src/x/y/z/p32");
		this.createFile(
				"/P3/src/x/y/z/p32/XX32.java",
				"package x.y.z.p32;\n"+
				"public class XX32 {\n"+
				"}");
		
		// create PX
		this.createJavaProject(
				"PX",
				new String[]{"src"},
				Util.getJavaClassLibs(),
				null,
				null,
				new String[]{"/P2"},
				null,
				null,
				new boolean[]{false},
				"bin",
				null,
				null,
				null,
				"1.4");
		
		this.createFile(
				"/PX/src/X.java",
				"public class X {\n"+
				"  void foo() {\n"+
				"    XX\n"+
				"  }\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("PX", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX11[TYPE_REF]{x.y.z.p11.XX11, x.y.z.p11, Lx.y.z.p11.XX11;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{x.y.z.p21.XX21, x.y.z.p21, Lx.y.z.p21.XX21;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{x.y.z.p22.XX22, x.y.z.p22, Lx.y.z.p22.XX22;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{x.y.z.p32.XX32, x.y.z.p32, Lx.y.z.p32.XX32;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		this.deleteProject("PX");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction12() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);
		
		// create variable
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
			null);

		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			 "bin");
		
		this.createFolder("/P1/src/p11");
		this.createFile(
				"/P1/src/p11/XX11.java",
				"package p11;\n"+
				"public class XX11 {\n"+
				"}");
		
		this.createFolder("/P1/src/p12");
		this.createFile(
				"/P1/src/p12/XX12.java",
				"package p12;\n"+
				"public class XX12 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P3", "/P1"},
			new String[][]{{}, {}},
			new String[][]{{"p31/*"}, {"p11/*"}},
			new boolean[]{true, true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P2/src/p21");
		this.createFile(
				"/P2/src/p21/XX21.java",
				"package p21;\n"+
				"public class XX21 {\n"+
				"}");
		
		this.createFolder("/P2/src/p22");
		this.createFile(
				"/P2/src/p22/XX22.java",
				"package p22;\n"+
				"public class XX22 {\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			Util.getJavaClassLibs(),
			null,
			null,
			new String[]{"/P1"},
			new String[][]{{}},
			new String[][]{{"p12/*"}},
			new boolean[]{true},
			"bin",
			null,
			null,
			null,
			"1.4");
		
		this.createFolder("/P3/src/p31");
		this.createFile(
				"/P3/src/p31/XX31.java",
				"package p31;\n"+
				"public class XX31 {\n"+
				"}");
		
		this.createFolder("/P3/src/p32");
		this.createFile(
				"/P3/src/p32/XX32.java",
				"package p32;\n"+
				"public class XX32 {\n"+
				"}");
		
		// create PX
		this.createJavaProject(
				"PX",
				new String[]{"src"},
				Util.getJavaClassLibs(),
				null,
				null,
				new String[]{"/P2"},
				null,
				null,
				new boolean[]{false},
				"bin",
				null,
				null,
				null,
				"1.4");
		
		this.createFile(
				"/PX/src/X.java",
				"public class X {\n"+
				"  void foo() {\n"+
				"    XX\n"+
				"  }\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("PX", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "XX";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"XX12[TYPE_REF]{p12.XX12, p12, Lp12.XX12;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE) + "}\n" +
			"XX31[TYPE_REF]{p31.XX31, p31, Lp31.XX31;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE) + "}\n" +
			"XX11[TYPE_REF]{p11.XX11, p11, Lp11.XX11;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{p21.XX21, p21, Lp21.XX21;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{p22.XX22, p22, Lp22.XX22;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{p32.XX32, p32, Lp32.XX32;, null, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		this.deleteProject("PX");
		JavaCore.setOptions(oldOptions);
	}
}
//public void testAccessRestrictionX() throws Exception {
//	Hashtable oldOptions = JavaCore.getOptions();
//	try {
//		Hashtable options = new Hashtable(oldOptions);
//		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
//		options.put(JavaCore.CODEASSIST_RESTRICTIONS_CHECK, JavaCore.DISABLED);
//		JavaCore.setOptions(options);
//		
//		// create variable
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);
//
//		// create P1
//		this.createJavaProject(
//			"P1",
//			new String[]{"src"},
//			Util.getJavaClassLibs(),
//			 "bin");
//		
//		this.createFolder("/P1/src/a");
//		this.createFile(
//				"/P1/src/a/XX1.java",
//				"package a;\n"+
//				"public class XX1 {\n"+
//				"  public void foo() {\n"+
//				"  }\n"+
//				"}");
//		
//		// create P2
//		this.createJavaProject(
//			"P2",
//			new String[]{"src"},
//			Util.getJavaClassLibs(),
//			null,
//			null,
//			new String[]{"/P1"},
//			new String[][]{{}},
//			new String[][]{{"a/*"}},
//			new boolean[]{false},
//			"bin",
//			null,
//			null,
//			null,
//			"1.4");
//		this.createFile(
//			"/P2/src/YY.java",
//			"public class YY {\n"+
//			"  void foo() {\n"+
//			"    a.XX1 x;\n"+
//			"    x.fo\n"+
//			"  }\n"+
//			"}");
//		
//		waitUntilIndexesReady();
//		
//		// do completion
//		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
//		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "YY.java");
//		
//		String str = cu.getSource();
//		String completeBehind = "x.fo";
//		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
//		cu.codeComplete(cursorLocation, requestor);
//		
//		assertResults(
//			"foo[METHOD_REF]{foo(), La.XX1;, ()V, foo, "+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC) + "}",
//			requestor.getResults());
//	} finally {
//		this.deleteProject("P1");
//		this.deleteProject("P2");
//		JavaCore.setOptions(oldOptions);
//	}
//}
}
