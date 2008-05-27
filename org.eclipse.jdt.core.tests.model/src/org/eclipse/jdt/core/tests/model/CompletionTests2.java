/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;

import junit.framework.*;

public class CompletionTests2 extends ModifyingResourceTests implements RelevanceConstants {
	
	public static class CompletionContainerInitializer implements ContainerInitializer.ITestInitializer {
		
		public static class DefaultContainer implements IClasspathContainer {
			char[][] libPaths;
			boolean[] areExported;
			String[] forbiddenReferences;
			public DefaultContainer(char[][] libPaths, boolean[] areExported, String[] forbiddenReferences) {
				this.libPaths = libPaths;
				this.areExported = areExported;
				this.forbiddenReferences = forbiddenReferences;
			}
			public IClasspathEntry[] getClasspathEntries() {
				int length = this.libPaths.length;
				IClasspathEntry[] entries = new IClasspathEntry[length];
				for (int j = 0; j < length; j++) {
				    IPath path = new Path(new String(this.libPaths[j]));
				    boolean isExported = this.areExported[j];
		
				    IAccessRule[] accessRules;
				    if(forbiddenReferences != null && forbiddenReferences[j]!= null && forbiddenReferences[j].length() != 0) {
					    StringTokenizer tokenizer = new StringTokenizer(forbiddenReferences[j], ";");
					    int count = tokenizer.countTokens();
					    accessRules = new IAccessRule[count];
					    String token = null;
					    for (int i = 0; i < count; i++) {
					    	token = tokenizer.nextToken();
							accessRules[i] = JavaCore.newAccessRule(new Path(token), IAccessRule.K_NON_ACCESSIBLE);
						}
					} else {
						accessRules = new IAccessRule[0];
					}
				    if (path.segmentCount() == 1) {
				        entries[j] = JavaCore.newProjectEntry(path, accessRules, true, new IClasspathAttribute[0], isExported);
				    } else {
						entries[j] = JavaCore.newLibraryEntry(path, null, null, accessRules, new IClasspathAttribute[0], isExported);
				    }
				}
				return entries;
			}
			public String getDescription() {
				return "Test container";
			}
			public int getKind() {
				return IClasspathContainer.K_APPLICATION;
			}
			public IPath getPath() {
				return new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
			}
		}
		
		Map containerValues;
		CoreException exception;
		
		public CompletionContainerInitializer(String projectName, String[] libPaths, boolean[] areExported) {
			this(projectName, libPaths, areExported, null);
		}
		public CompletionContainerInitializer(String projectName, String[] libPaths, boolean[] areExported, String[] forbiddenRefrences) {
			containerValues = new HashMap();
			
			int libPathsLength = libPaths.length;
			char[][] charLibPaths = new char[libPathsLength][];
			for (int i = 0; i < libPathsLength; i++) {
				charLibPaths[i] = libPaths[i].toCharArray();
			}
			containerValues.put(
				projectName, 
				newContainer(charLibPaths, areExported, forbiddenRefrences)
			);
		}
		protected DefaultContainer newContainer(final char[][] libPaths, final boolean[] areExperted, final String[] forbiddenRefrences) {
			return new DefaultContainer(libPaths, areExperted, forbiddenRefrences);
		}
		public boolean allowFailureContainer() {
			return true;
		}
		public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
			if (containerValues == null) return;
			try {
				JavaCore.setClasspathContainer(
					containerPath, 
					new IJavaProject[] {project},
					new IClasspathContainer[] {(IClasspathContainer)containerValues.get(project.getElementName())}, 
					null);
			} catch (CoreException e) {
				this.exception = e;
				throw e;
			}
		}
	}
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
static {
//	TESTS_NAMES = new String[] { "testBug96950" };
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests2.class);
}

File createFile(File parent, String name, String content) throws IOException {
	File file = new File(parent, name);
	FileOutputStream out = new FileOutputStream(file);
	try {
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
/**
 * Test for bug 29832
 */
public void testBug29832() throws Exception {
	try {
		// create variable
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);

		// create P1
		IFile f = getFile("/Completion/lib.jar");
		IJavaProject p = this.createJavaProject(
			"P1",
			new String[]{},
			new String[]{"JCL_LIB"},
			 "");
		this.createFile("/P1/lib.jar", f.getContents());
		this.addLibraryEntry(p, "/P1/lib.jar", true);
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			new String[]{"/P1"},
			"bin");
		this.createFile(
			"/P2/src/X.java",
			"public class X {\n"+
			"  ZZZ z;\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "ZZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
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
		
		waitUntilIndexesReady();
		
		// do completion
		requestor = new CompletionTestsRequestor();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
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
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);

		// create P1
		IFile f = getFile("/Completion/lib.jar");
		IJavaProject p = this.createJavaProject(
			"P1",
			new String[]{},
			new String[]{"JCL_LIB"},
			 "");
		this.createFile("/P1/lib.jar", f.getContents());
		this.addLibraryEntry(p, "/P1/lib.jar", true);
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			new String[]{"/P1"},
			new boolean[]{true},
			"bin");
					
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			new String[]{"/P2"},
			"bin");
		this.createFile(
			"/P3/src/X.java",
			"public class X {\n"+
			"  ZZZ z;\n"+
			"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
		ICompilationUnit cu= getCompilationUnit("P3", "src", "", "X.java");
		
		String str = cu.getSource();
		String completeBehind = "ZZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
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
		
		waitUntilIndexesReady();
		
		// do completion
		requestor = new CompletionTestsRequestor();
		cu.codeComplete(cursorLocation, requestor);

		assertEquals(
			"element:ZZZ    completion:pz.ZZZ    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
	}
}
public void testBug79288() throws Exception {
	try {
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
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			new String[]{"/P1"},
			"bin");
		
		this.createFolder("/P2/src/b");
		this.createFile(
				"/P2/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			new String[]{"/P2"},
			"bin");
		
		this.createFile(
				"/P3/src/YY.java",
				"public class YY {\n"+
				"  vois foo(){\n"+
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
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
	}
}
public void testBug91772() throws Exception {
	try {
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
				"/P1/src/a/XX1.java",
				"package a;\n"+
				"public class XX1 {\n"+
				"}");
		
		// create P2
		ContainerInitializer.setInitializer(new CompletionContainerInitializer("P2", new String[] {"/P1"}, new boolean[] {true}));
		String[] classLib = new String[]{"JCL_LIB"};
		int classLibLength = classLib.length;
		String[] lib = new String[classLibLength + 1];
		System.arraycopy(classLib, 0, lib, 0, classLibLength);
		lib[classLibLength] = "org.eclipse.jdt.core.tests.model.TEST_CONTAINER";
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			lib,
			"bin");
		
		this.createFolder("/P2/src/b");
		this.createFile(
				"/P2/src/b/XX2.java",
				"package b;\n"+
				"public class XX2 {\n"+
				"}");
		
		// create P3
		this.createJavaProject(
			"P3",
			new String[]{"src"},
			new String[]{"JCL_LIB"},
			new String[]{"/P2"},
			"bin");
		
		this.createFile(
				"/P3/src/YY.java",
				"public class YY {\n"+
				"  vois foo(){\n"+
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
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		
		
		// TODO the following code is not the correct way to remove the container
		// Cleanup caches
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		manager.containers = new HashMap(5);
		manager.variables = new HashMap(5);
	}
}
public void testBug93891() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
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
		ContainerInitializer.setInitializer(new CompletionContainerInitializer("P2", new String[] {"/P1"}, new boolean[] {true}, new String[]{"a/*"}));
		String[] classLib = new String[]{"JCL_LIB"};
		int classLibLength = classLib.length;
		String[] lib = new String[classLibLength + 1];
		System.arraycopy(classLib, 0, lib, 0, classLibLength);
		lib[classLibLength] = "org.eclipse.jdt.core.tests.model.TEST_CONTAINER";
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			lib,
			"bin");
		
		this.createFolder("/P2/src/b");
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
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		
		// TODO the following code is not the correct way to remove the container
		// Cleanup caches
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		manager.containers = new HashMap(5);
		manager.variables = new HashMap(5);
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction1() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE, JavaCore.IGNORE);
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
			new String[]{"JCL_LIB"},
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
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
 			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
			"XX3[TYPE_REF]{c.XX3, c, Lc.XX3;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
			"XX1[TYPE_REF]{a.XX1, a, La.XX1;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
				new String[]{"JCL_LIB"},
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
			"XX12[TYPE_REF]{p12.XX12, p12, Lp12.XX12;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{p21.XX21, p21, Lp21.XX21;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{p22.XX22, p22, Lp22.XX22;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{p32.XX32, p32, Lp32.XX32;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
				new String[]{"JCL_LIB"},
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
			"XX11[TYPE_REF]{p11.XX11, p11, Lp11.XX11;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE) + "}\n" +
			"XX31[TYPE_REF]{p31.XX31, p31, Lp31.XX31;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE) + "}\n" +
			"XX12[TYPE_REF]{p12.XX12, p12, Lp12.XX12;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{p21.XX21, p21, Lp21.XX21;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{p22.XX22, p22, Lp22.XX22;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{p32.XX32, p32, Lp32.XX32;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
				new String[]{"JCL_LIB"},
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
			"XX11[TYPE_REF]{x.y.z.p11.XX11, x.y.z.p11, Lx.y.z.p11.XX11;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{x.y.z.p21.XX21, x.y.z.p21, Lx.y.z.p21.XX21;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{x.y.z.p22.XX22, x.y.z.p22, Lx.y.z.p22.XX22;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{x.y.z.p32.XX32, x.y.z.p32, Lx.y.z.p32.XX32;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
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
			new String[]{"JCL_LIB"},
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
			new String[]{"JCL_LIB"},
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
				new String[]{"JCL_LIB"},
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
			"XX12[TYPE_REF]{p12.XX12, p12, Lp12.XX12;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE) + "}\n" +
			"XX31[TYPE_REF]{p31.XX31, p31, Lp31.XX31;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE) + "}\n" +
			"XX11[TYPE_REF]{p11.XX11, p11, Lp11.XX11;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX21[TYPE_REF]{p21.XX21, p21, Lp21.XX21;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX22[TYPE_REF]{p22.XX22, p22, Lp22.XX22;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"XX32[TYPE_REF]{p32.XX32, p32, Lp32.XX32;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		this.deleteProject("P3");
		this.deleteProject("PX");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction13() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.WARNING);
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
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		JavaCore.setOptions(oldOptions);
	}
}
public void testAccessRestriction14() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.WARNING);
		options.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		options.put(JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaCore.ENABLED);
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
			"XX2[TYPE_REF]{b.XX2, b, Lb.XX2;, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
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
//			new String[]{"JCL_LIB"},
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
//			new String[]{"JCL_LIB"},
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
//			"foo[METHOD_REF]{foo(), La.XX1;, ()V, foo, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC) + "}",
//			requestor.getResults());
//	} finally {
//		this.deleteProject("P1");
//		this.deleteProject("P2");
//		JavaCore.setOptions(oldOptions);
//	}
//}
public void testBug96950() throws Exception {
	try {
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
		this.createFile(
				"/P1/src/Taratata.java",
				"public class Taratata {\n"+
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
			new String[][]{{"**/*"}},
			new boolean[]{false},
			"bin",
			null,
			null,
			null,
			"1.4");
		this.createFile(
				"/P2/src/BreakRules.java",
				"public class BreakRules {\n"+
				"	Tara\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("P2", "src", "", "BreakRules.java");
		
		String str = cu.getSource();
		String completeBehind = "Tara";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
			"Tara[POTENTIAL_METHOD_DECLARATION]{Tara, LBreakRules;, ()V, Tara, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/**
 * @bug 162621: [model][delta] Validation errors do not clear after replacing jar file
 * @test Ensures that changing an internal jar and refreshing takes the change into account
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=162621"
 */
public void testChangeInternalJar() throws CoreException, IOException {
	String jarName = "b162621.jar";
	try {
		// Create jar file with a class with 2 methods doXXX
		String[] pathAndContents = new String[] {
			"pack/Util.java",
			"package pack;\n" + 
			"public class Util {\n" + 
			"    public void doit2A(int x, int y) { }\n" + 
			"    public void doit2B(int x) { }\n" + 
			"}\n"
		};
		addLibrary(jarName, "b162621_src.zip", pathAndContents, JavaCore.VERSION_1_4);

		// Wait a little bit to be sure file system is aware of zip file creation
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			// skip
		}

		// Create compilation unit in which completion occurs
		String path = "/Completion/src/test/Test.java";
		String source = "package test;\n" + 
			"import pack.*;\n" + 
			"public class Test {\n" + 
			"	public void foo() {\n" + 
			"		Util test = new Util();\n" + 
			"		test.doit2A(1, 2);\n" + 
			"	}\n" + 
			"}\n";
		createFolder("/Completion/src/test");
		createFile(path, source);

		// first completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit unit = getCompilationUnit(path);
		String completeBehind = "test.do";
		int cursorLocation = source.lastIndexOf(completeBehind) + completeBehind.length();
		unit.codeComplete(cursorLocation, requestor);
		assertResults(
			"doit2A[METHOD_REF]{doit2A, Lpack.Util;, (II)V, doit2A, "+(R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_NON_RESTRICTED + R_NON_STATIC) + "}\n" +
			"doit2B[METHOD_REF]{doit2B, Lpack.Util;, (I)V, doit2B, "+(R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_NON_RESTRICTED + R_NON_STATIC) + "}",
			requestor.getResults());

		// change class file to add a third doXXX method and refresh
		String projectLocation = this.currentProject.getProject().getLocation().toOSString();
		String jarPath = projectLocation + File.separator + jarName;
		createJar(new String[] {
			"pack/Util.java",
			"package pack;\n" + 
			"public class Util {\n" + 
			"    public void doit2A(int x, int y) { }\n" + 
			"    public void doit2B(int x) { }\n" + 
			"    public void doit2C(int x) { }\n" + 
			"}\n"
		}, jarPath);
		this.currentProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			// skip
		}

		// second completion
		requestor = new CompletionTestsRequestor2();
		unit.codeComplete(cursorLocation, requestor);
		assertResults(
			"doit2A[METHOD_REF]{doit2A, Lpack.Util;, (II)V, doit2A, "+(R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_NON_RESTRICTED + R_NON_STATIC) + "}\n" +
			"doit2B[METHOD_REF]{doit2B, Lpack.Util;, (I)V, doit2B, "+(R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_NON_RESTRICTED + R_NON_STATIC) + "}\n" +
			"doit2C[METHOD_REF]{doit2C, Lpack.Util;, (I)V, doit2C, "+(R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_NON_RESTRICTED + R_NON_STATIC) + "}",
			requestor.getResults());
	} finally {
		removeClasspathEntry(this.currentProject, new Path(jarName));
		deleteResource(new File(jarName));
	}
}
}
