/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

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
				    if(this.forbiddenReferences != null && this.forbiddenReferences[j]!= null && this.forbiddenReferences[j].length() != 0) {
					    StringTokenizer tokenizer = new StringTokenizer(this.forbiddenReferences[j], ";");
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
			this.containerValues = new HashMap();

			int libPathsLength = libPaths.length;
			char[][] charLibPaths = new char[libPathsLength][];
			for (int i = 0; i < libPathsLength; i++) {
				charLibPaths[i] = libPaths[i].toCharArray();
			}
			this.containerValues.put(
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
			if (this.containerValues == null) return;
			try {
				JavaCore.setClasspathContainer(
					containerPath,
					new IJavaProject[] {project},
					new IClasspathContainer[] {(IClasspathContainer)this.containerValues.get(project.getElementName())},
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
	if (AbstractJavaModelCompletionTests.COMPLETION_PROJECT == null)  {
		AbstractJavaModelCompletionTests.COMPLETION_PROJECT = setUpJavaProject("Completion");
	} else {
		setUpProjectCompliance(AbstractJavaModelCompletionTests.COMPLETION_PROJECT, "1.4");
		this.currentProject = AbstractJavaModelCompletionTests.COMPLETION_PROJECT;
	}
	super.setUpSuite();
}
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
		File pro = createDirectory(dest, "P1");

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

		File src = createDirectory(pro, "src");

		File pz = createDirectory(src, "pz");

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
		File pro = createDirectory(dest, "P1");

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

		File src = createDirectory(pro, "src");

		File pz = createDirectory(src, "pz");

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
public void testBug6930_01() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		createFolder("/P/src/p6930");
		
		this.workingCopies = new ICompilationUnit[3];
		
		this.workingCopies[1] = getWorkingCopy("/P/src/p6930/AllConstructors01.java",
			"package p6930;\n" +
			"public class AllConstructors01 {\n" +
			"  public AllConstructors01() {}\n" +
			"  public AllConstructors01(Object o) {}\n" +
			"  public AllConstructors01(int o) {}\n" +
			"  public AllConstructors01(Object o, String s) {}\n" +
			"}\n"
		);
		
		this.workingCopies[2] = getWorkingCopy("/P/src/p6930/AllConstructors01b.java",
			"package p6930;\n" +
			"public class AllConstructors01b {\n" +
			"}\n"
		);
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors01[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors01;, ()V, AllConstructors01, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors01[TYPE_REF]{p6930.AllConstructors01, p6930, Lp6930.AllConstructors01;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors01[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors01;, (I)V, AllConstructors01, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors01[TYPE_REF]{p6930.AllConstructors01, p6930, Lp6930.AllConstructors01;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors01[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors01;, (Ljava.lang.Object;)V, AllConstructors01, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors01[TYPE_REF]{p6930.AllConstructors01, p6930, Lp6930.AllConstructors01;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors01[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors01;, (Ljava.lang.Object;Ljava.lang.String;)V, AllConstructors01, (o, s), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors01[TYPE_REF]{p6930.AllConstructors01, p6930, Lp6930.AllConstructors01;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors01b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors01b;, ()V, AllConstructors01b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors01b[TYPE_REF]{p6930.AllConstructors01b, p6930, Lp6930.AllConstructors01b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_02() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createJar(new String[] {
			"p6930/AllConstructors02.java",
			"package p6930;\n" +
			"public class AllConstructors02 {\n" +
			"  public AllConstructors02() {}\n" +
			"  public AllConstructors02(Object o) {}\n" +
			"  public AllConstructors02(int o) {}\n" +
			"  public AllConstructors02(Object o, String s) {}\n" +
			"}",
			"p6930/AllConstructors02b.java",
			"package p6930;\n" +
			"public class AllConstructors02b {\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors02[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors02;, ()V, AllConstructors02, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors02[TYPE_REF]{p6930.AllConstructors02, p6930, Lp6930.AllConstructors02;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors02[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors02;, (I)V, AllConstructors02, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors02[TYPE_REF]{p6930.AllConstructors02, p6930, Lp6930.AllConstructors02;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors02[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors02;, (Ljava.lang.Object;)V, AllConstructors02, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors02[TYPE_REF]{p6930.AllConstructors02, p6930, Lp6930.AllConstructors02;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors02[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors02;, (Ljava.lang.Object;Ljava.lang.String;)V, AllConstructors02, (o, s), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors02[TYPE_REF]{p6930.AllConstructors02, p6930, Lp6930.AllConstructors02;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors02b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors02b;, ()V, AllConstructors02b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors02b[TYPE_REF]{p6930.AllConstructors02b, p6930, Lp6930.AllConstructors02b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_03() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors03.java",
				"package p6930;\n" +
				"public class AllConstructors03 {\n" +
				"  public AllConstructors03() {}\n" +
				"  public AllConstructors03(Object o) {}\n" +
				"  public AllConstructors03(int o) {}\n" +
				"  public AllConstructors03(Object o, String s) {}\n" +
				"}");
		
		createFile(
				"/P/src/p6930/AllConstructors03b.java",
				"package p6930;\n" +
				"public class AllConstructors03b {\n" +
				"}");
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors03[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors03;, ()V, AllConstructors03, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors03[TYPE_REF]{p6930.AllConstructors03, p6930, Lp6930.AllConstructors03;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors03[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors03;, (I)V, AllConstructors03, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors03[TYPE_REF]{p6930.AllConstructors03, p6930, Lp6930.AllConstructors03;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors03[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors03;, (Ljava.lang.Object;)V, AllConstructors03, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors03[TYPE_REF]{p6930.AllConstructors03, p6930, Lp6930.AllConstructors03;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors03[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors03;, (Ljava.lang.Object;Ljava.lang.String;)V, AllConstructors03, (o, s), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors03[TYPE_REF]{p6930.AllConstructors03, p6930, Lp6930.AllConstructors03;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors03b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors03b;, ()V, AllConstructors03b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors03b[TYPE_REF]{p6930.AllConstructors03b, p6930, Lp6930.AllConstructors03b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_04() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"class AllConstructors04a {\n" +
				"  public class AllConstructors0b {\n" +
				"  }\n" +
				"}\n" +
				"public class Test {\n" +
				"  public class AllConstructors04c {\n" +
				"    public class AllConstructors04d {\n" +
				"    }\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    class AllConstructors04e {\n" +
				"      class AllConstructors04f {\n" +
				"      }\n" +
				"    }\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/test/AllConstructors04g.java",
				"package test;"+
				"public class AllConstructors04g {\n" +
				"  public class AllConstructors0h {\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors04a[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors04a;, ()V, AllConstructors04a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors04a[TYPE_REF]{AllConstructors04a, test, Ltest.AllConstructors04a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors04c[CONSTRUCTOR_INVOCATION]{(), Ltest.Test$AllConstructors04c;, ()V, AllConstructors04c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   Test.AllConstructors04c[TYPE_REF]{AllConstructors04c, test, Ltest.Test$AllConstructors04c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors04e[CONSTRUCTOR_INVOCATION]{(), LAllConstructors04e;, ()V, AllConstructors04e, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors04e[TYPE_REF]{AllConstructors04e, null, LAllConstructors04e;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors04g[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors04g;, ()V, AllConstructors04g, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors04g[TYPE_REF]{AllConstructors04g, test, Ltest.AllConstructors04g;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_05() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"class AllConstructors05a {\n" +
				"  public static class AllConstructors0b {\n" +
				"  }\n" +
				"}\n" +
				"public class Test {\n" +
				"  public static class AllConstructors05c {\n" +
				"    public static class AllConstructors05d {\n" +
				"    }\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/test/AllConstructors05g.java",
				"package test;"+
				"public class AllConstructors05g {\n" +
				"  public static class AllConstructors0h {\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors05a[TYPE_REF]{AllConstructors05a, test, Ltest.AllConstructors05a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors05a[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors05a;, ()V, AllConstructors05a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors05a[TYPE_REF]{AllConstructors05a, test, Ltest.AllConstructors05a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors05c[CONSTRUCTOR_INVOCATION]{(), Ltest.Test$AllConstructors05c;, ()V, AllConstructors05c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   Test.AllConstructors05c[TYPE_REF]{AllConstructors05c, test, Ltest.Test$AllConstructors05c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors05g[TYPE_REF]{AllConstructors05g, test, Ltest.AllConstructors05g;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors05g[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors05g;, ()V, AllConstructors05g, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors05g[TYPE_REF]{AllConstructors05g, test, Ltest.AllConstructors05g;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"Test.AllConstructors05c[TYPE_REF]{AllConstructors05c, test, Ltest.Test$AllConstructors05c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_06() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors06a.java",
				"package p6930;\n" +
				"public class AllConstructors06a {\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors06b.java",
			"package p6930;\n" +
			"public class AllConstructors06b {\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors06a;\n"+
				"import p6930.AllConstructors06b;\n"+
				"import p6930.AllConstructors06c;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors06c.java",
				"package p6930;"+
				"public class AllConstructors06c {\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors06a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors06a;, ()V, AllConstructors06a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors06a[TYPE_REF]{AllConstructors06a, p6930, Lp6930.AllConstructors06a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors06b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors06b;, ()V, AllConstructors06b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors06b[TYPE_REF]{AllConstructors06b, p6930, Lp6930.AllConstructors06b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors06c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors06c;, ()V, AllConstructors06c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors06c[TYPE_REF]{AllConstructors06c, p6930, Lp6930.AllConstructors06c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());

	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_07() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors07a.java",
				"package p6930;\n" +
				"public class AllConstructors07a {\n" +
				"  public class AllConstructors07b {\n" +
				"  }\n" +
				"  public static class AllConstructors07c {\n" +
				"  }\n" +
				"}");
		
		createFile(
				"/P/src/p6930/AllConstructors07d.java",
				"package p6930;\n" +
				"public class AllConstructors07d {\n" +
				"  public class AllConstructors07e {\n" +
				"  }\n" +
				"  public static class AllConstructors07f {\n" +
				"  }\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.*;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors07a[TYPE_REF]{AllConstructors07a, p6930, Lp6930.AllConstructors07a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors07a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors07a;, ()V, AllConstructors07a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors07a[TYPE_REF]{AllConstructors07a, p6930, Lp6930.AllConstructors07a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors07d[TYPE_REF]{AllConstructors07d, p6930, Lp6930.AllConstructors07d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors07d[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors07d;, ()V, AllConstructors07d, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors07d[TYPE_REF]{AllConstructors07d, p6930, Lp6930.AllConstructors07d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_08() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors08a.java",
				"package p6930;\n" +
				"public class AllConstructors08a {\n" +
				"  public class AllConstructors08b {\n" +
				"  }\n" +
				"  public static class AllConstructors08c {\n" +
				"  }\n" +
				"}");
		
		createFile(
				"/P/src/p6930/AllConstructors08d.java",
				"package p6930;\n" +
				"public class AllConstructors08d {\n" +
				"  public class AllConstructors08e {\n" +
				"  }\n" +
				"  public static class AllConstructors08f {\n" +
				"  }\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors08a;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors08d[TYPE_REF]{p6930.AllConstructors08d, p6930, Lp6930.AllConstructors08d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors08d[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors08d;, ()V, AllConstructors08d, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors08d[TYPE_REF]{p6930.AllConstructors08d, p6930, Lp6930.AllConstructors08d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors08a[TYPE_REF]{AllConstructors08a, p6930, Lp6930.AllConstructors08a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors08a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors08a;, ()V, AllConstructors08a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors08a[TYPE_REF]{AllConstructors08a, p6930, Lp6930.AllConstructors08a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}

public void testBug6930_09() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors09a.java",
				"package p6930;\n" +
				"public class AllConstructors09a {\n" +
				"  public class AllConstructors09b {\n" +
				"  }\n" +
				"  public static class AllConstructors09c {\n" +
				"  }\n" +
				"}");
		
		createFile(
				"/P/src/p6930/AllConstructors09d.java",
				"package p6930;\n" +
				"public class AllConstructors09d {\n" +
				"  public class AllConstructors09e {\n" +
				"  }\n" +
				"  public static class AllConstructors09f {\n" +
				"    public static class AllConstructors09g {\n" +
				"    }\n" +
				"  }\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors09a.*;\n"+
				"import static p6930.AllConstructors09d.*;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors09a[TYPE_REF]{p6930.AllConstructors09a, p6930, Lp6930.AllConstructors09a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors09a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors09a;, ()V, AllConstructors09a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors09a[TYPE_REF]{p6930.AllConstructors09a, p6930, Lp6930.AllConstructors09a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors09d[TYPE_REF]{p6930.AllConstructors09d, p6930, Lp6930.AllConstructors09d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors09d[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors09d;, ()V, AllConstructors09d, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors09d[TYPE_REF]{p6930.AllConstructors09d, p6930, Lp6930.AllConstructors09d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors09d.AllConstructors09f[TYPE_REF]{AllConstructors09f, p6930, Lp6930.AllConstructors09d$AllConstructors09f;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors09f[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors09d$AllConstructors09f;, ()V, AllConstructors09f, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors09d.AllConstructors09f[TYPE_REF]{AllConstructors09f, p6930, Lp6930.AllConstructors09d$AllConstructors09f;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_10() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors10a.java",
				"package p6930;\n" +
				"public class AllConstructors10a {\n" +
				"  public class AllConstructors10b {\n" +
				"    public static class AllConstructors10bs {\n" +
				"    }\n" +
				"  }\n" +
				"  public static class AllConstructors10c {\n" +
				"    public static class AllConstructors10cs {\n" +
				"    }\n" +
				"  }\n" +
				"}");
		
		createFile(
				"/P/src/p6930/AllConstructors10d.java",
				"package p6930;\n" +
				"public class AllConstructors10d {\n" +
				"  public class AllConstructors10e {\n" +
				"    public static class AllConstructors10es {\n" +
				"    }\n" +
				"  }\n" +
				"  public static class AllConstructors10f {\n" +
				"    public static class AllConstructors10fs {\n" +
				"    }\n" +
				"  }\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors10a.AllConstructors10b;\n"+
				"import p6930.AllConstructors10a.AllConstructors10c;\n"+
				"import static p6930.AllConstructors10d.AllConstructors10e;\n"+
				"import static p6930.AllConstructors10d.AllConstructors10f;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors10a[TYPE_REF]{p6930.AllConstructors10a, p6930, Lp6930.AllConstructors10a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors10a;, ()V, AllConstructors10a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors10a[TYPE_REF]{p6930.AllConstructors10a, p6930, Lp6930.AllConstructors10a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10d[TYPE_REF]{p6930.AllConstructors10d, p6930, Lp6930.AllConstructors10d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10d[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors10d;, ()V, AllConstructors10d, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors10d[TYPE_REF]{p6930.AllConstructors10d, p6930, Lp6930.AllConstructors10d;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10a.AllConstructors10c[TYPE_REF]{AllConstructors10c, p6930, Lp6930.AllConstructors10a$AllConstructors10c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors10a$AllConstructors10c;, ()V, AllConstructors10c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors10a.AllConstructors10c[TYPE_REF]{AllConstructors10c, p6930, Lp6930.AllConstructors10a$AllConstructors10c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10d.AllConstructors10f[TYPE_REF]{AllConstructors10f, p6930, Lp6930.AllConstructors10d$AllConstructors10f;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors10f[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors10d$AllConstructors10f;, ()V, AllConstructors10f, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors10d.AllConstructors10f[TYPE_REF]{AllConstructors10f, p6930, Lp6930.AllConstructors10d$AllConstructors10f;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_11() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors11a.java",
				"package p6930;\n" +
				"public class AllConstructors11a {\n" +
				"}");

		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    p6930.AllConstructors11a a = new \n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "new ";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"Test[CONSTRUCTOR_INVOCATION]{(), Ltest.Test;, ()V, Test, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   Test[TYPE_REF]{Test, test, Ltest.Test;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors11a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors11a;, ()V, AllConstructors11a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors11a[TYPE_REF]{p6930.AllConstructors11a, p6930, Lp6930.AllConstructors11a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_12() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors12a.java",
				"package p6930;\n" +
				"public class AllConstructors12a {\n" +
				"  public static class AllConstructors12b {\n" +
				"  }\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    p6930.AllConstructors12a a = new \n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "new ";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"Test[CONSTRUCTOR_INVOCATION]{(), Ltest.Test;, ()V, Test, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   Test[TYPE_REF]{Test, test, Ltest.Test;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors12a[TYPE_REF]{p6930.AllConstructors12a, p6930, Lp6930.AllConstructors12a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors12a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors12a;, ()V, AllConstructors12a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors12a[TYPE_REF]{p6930.AllConstructors12a, p6930, Lp6930.AllConstructors12a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_13() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors13a.java",
				"package p6930;\n" +
				"public class AllConstructors13a {\n" +
				"}");

		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    p6930.AllConstructors13a a = new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "new AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors13a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors13a;, ()V, AllConstructors13a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors13a[TYPE_REF]{p6930.AllConstructors13a, p6930, Lp6930.AllConstructors13a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_14() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors14a.java",
				"package p6930;\n" +
				"public class AllConstructors14a {\n" +
				"  public static class AllConstructors14b {\n" +
				"  }\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    p6930.AllConstructors14a a = new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "new AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors14a[TYPE_REF]{p6930.AllConstructors14a, p6930, Lp6930.AllConstructors14a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors14a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors14a;, ()V, AllConstructors14a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors14a[TYPE_REF]{p6930.AllConstructors14a, p6930, Lp6930.AllConstructors14a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_15() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors15a.java",
				"package p6930;\n" +
				"public class AllConstructors15a<T> {\n" +
				"}");
		
		createJar(
				new String[] {
					"p6930/AllConstructors15b.java",
					"package p6930;\n" +
					"public class AllConstructors15b<T> {\n" +
					"}"
				},
				p.getProject().getLocation().append("lib6930.jar").toOSString(),
				new String[]{getExternalJCLPathString("1.5")},
				"1.5");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors15c.java",
				"package p6930;"+
				"public class AllConstructors15c<T> {\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors15a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors15a;, ()V, AllConstructors15a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors15a[TYPE_REF]{p6930.AllConstructors15a, p6930, Lp6930.AllConstructors15a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors15b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors15b;, ()V, AllConstructors15b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors15b[TYPE_REF]{p6930.AllConstructors15b, p6930, Lp6930.AllConstructors15b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors15c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors15c;, ()V, AllConstructors15c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors15c[TYPE_REF]{p6930.AllConstructors15c, p6930, Lp6930.AllConstructors15c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_16() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors16a.java",
				"package p6930;\n" +
				"public class AllConstructors16a{\n" +
				"  public <T> AllConstructors16a(){}\n" +
				"}");
		
		createJar(
				new String[] {
					"p6930/AllConstructors16b.java",
					"package p6930;\n" +
					"public class AllConstructors16b {\n" +
					"  public <T> AllConstructors16b(){}\n" +
					"}"
				},
				p.getProject().getLocation().append("lib6930.jar").toOSString(),
				new String[]{getExternalJCLPathString("1.5")},
				"1.5");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors16c.java",
				"package p6930;"+
				"public class AllConstructors16c {\n" +
				"  public <T> AllConstructors16c(){}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors16a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors16a;, ()V, AllConstructors16a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors16a[TYPE_REF]{p6930.AllConstructors16a, p6930, Lp6930.AllConstructors16a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors16b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors16b;, ()V, AllConstructors16b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors16b[TYPE_REF]{p6930.AllConstructors16b, p6930, Lp6930.AllConstructors16b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors16c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors16c;, ()V, AllConstructors16c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors16c[TYPE_REF]{p6930.AllConstructors16c, p6930, Lp6930.AllConstructors16c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_17() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors17a.java",
				"package p6930;\n" +
				"public class AllConstructors17a{\n" +
				"  public AllConstructors17a(java.util.Collection<Object> o){}\n" +
				"}");
		
		createJar(
				new String[] {
					"p6930/AllConstructors17b.java",
					"package p6930;\n" +
					"public class AllConstructors17b {\n" +
					"  public AllConstructors17b(java.util.Collection<Object> o){}\n" +
					"}"
				},
				p.getProject().getLocation().append("lib6930.jar").toOSString(),
				new String[]{getExternalJCLPathString("1.5")},
				"1.5");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors17c.java",
				"package p6930;"+
				"public class AllConstructors17c {\n" +
				"  public AllConstructors17c(java.util.Collection<Object> o){}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors17a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors17a;, (Ljava.util.Collection<Ljava.lang.Object;>;)V, AllConstructors17a, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors17a[TYPE_REF]{p6930.AllConstructors17a, p6930, Lp6930.AllConstructors17a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors17b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors17b;, (Ljava.util.Collection<Ljava.lang.Object;>;)V, AllConstructors17b, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors17b[TYPE_REF]{p6930.AllConstructors17b, p6930, Lp6930.AllConstructors17b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors17c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors17c;, (Ljava.util.Collection<Ljava.lang.Object;>;)V, AllConstructors17c, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors17c[TYPE_REF]{p6930.AllConstructors17c, p6930, Lp6930.AllConstructors17c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_18() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors18a.java",
				"package p6930;\n" +
				"public interface AllConstructors18a {\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors18b.java",
			"package p6930;\n" +
			"public interface AllConstructors18b {\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors18c.java",
				"package p6930;"+
				"public interface AllConstructors18c {\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors18a[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors18a;, ()V, AllConstructors18a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors18a[TYPE_REF]{p6930.AllConstructors18a, p6930, Lp6930.AllConstructors18a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors18b[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors18b;, ()V, AllConstructors18b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors18b[TYPE_REF]{p6930.AllConstructors18b, p6930, Lp6930.AllConstructors18b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors18c[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors18c;, ()V, AllConstructors18c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors18c[TYPE_REF]{p6930.AllConstructors18c, p6930, Lp6930.AllConstructors18c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_19() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors19a.java",
				"package p6930;\n" +
				"public interface AllConstructors19a {\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors19b.java",
			"package p6930;\n" +
			"public interface AllConstructors19b {\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors19a;\n"+
				"import p6930.AllConstructors19b;\n"+
				"import p6930.AllConstructors19c;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors19c.java",
				"package p6930;"+
				"public interface AllConstructors19c {\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors19a[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors19a;, ()V, AllConstructors19a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors19a[TYPE_REF]{AllConstructors19a, p6930, Lp6930.AllConstructors19a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors19b[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors19b;, ()V, AllConstructors19b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors19b[TYPE_REF]{AllConstructors19b, p6930, Lp6930.AllConstructors19b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors19c[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors19c;, ()V, AllConstructors19c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors19c[TYPE_REF]{AllConstructors19c, p6930, Lp6930.AllConstructors19c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_20() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors20a.java",
				"package p6930;\n" +
				"public enum AllConstructors20a {\n" +
				"	ZZZ;\n" +
				"}");
		
		createJar(new String[] {
				"p6930/AllConstructors20b.java",
				"package p6930;\n" +
				"public enum AllConstructors20b {\n" +
				"	ZZZ;\n" +
				"}"
			},
			p.getProject().getLocation().append("lib6930.jar").toOSString(),
			new String[]{getExternalJCLPathString("1.5")},
			"1.5");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors20c.java",
				"package p6930;"+
				"public enum AllConstructors20c {\n" +
				"	ZZZ;\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_21() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib6930.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors21a.java",
				"package p6930;\n" +
				"public enum AllConstructors21a {\n" +
				"	ZZZ;\n" +
				"}");
		
		createJar(
				new String[] {
					"p6930/AllConstructors21b.java",
					"package p6930;\n" +
					"public enum AllConstructors21b {\n" +
					"	ZZZ;\n" +
					"}"
				},
				p.getProject().getLocation().append("lib6930.jar").toOSString(),
				new String[]{getExternalJCLPathString("1.5")},
				"1.5");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors21a;\n"+
				"import p6930.AllConstructors21b;\n"+
				"import p6930.AllConstructors21c;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors21c.java",
				"package p6930;"+
				"public enum AllConstructors21c {\n" +
				"	ZZZ;\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_22() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors22a.java",
				"package p6930;\n" +
				"public class AllConstructors22a {\n" +
				"	private AllConstructors22a(){}\n" +
				"	public static class AllConstructorsInner{}\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors22b.java",
			"package p6930;\n" +
			"public class AllConstructors22b {\n" +
			"	private AllConstructors22b(){}\n" +
			"	public static class AllConstructorsInner{}\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors22c.java",
				"package p6930;"+
				"public class AllConstructors22c {\n" +
				"	private AllConstructors22c(){}\n" +
				"	public static class AllConstructorsInner{}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors22a[TYPE_REF]{p6930.AllConstructors22a, p6930, Lp6930.AllConstructors22a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors22b[TYPE_REF]{p6930.AllConstructors22b, p6930, Lp6930.AllConstructors22b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors22c[TYPE_REF]{p6930.AllConstructors22c, p6930, Lp6930.AllConstructors22c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_23() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors23a.java",
				"package p6930;\n" +
				"public class AllConstructors23a {\n" +
				"	private AllConstructors23a(){}\n" +
				"	public static class AllConstructorsInner{}\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors23b.java",
			"package p6930;\n" +
			"public class AllConstructors23b {\n" +
			"	private AllConstructors23b(){}\n" +
			"	public static class AllConstructorsInner{}\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors23a;\n"+
				"import p6930.AllConstructors23b;\n"+
				"import p6930.AllConstructors23c;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors23c.java",
				"package p6930;"+
				"public class AllConstructors23c {\n" +
				"	private AllConstructors23c(){}\n" +
				"	public static class AllConstructorsInner{}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors23a[TYPE_REF]{AllConstructors23a, p6930, Lp6930.AllConstructors23a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors23b[TYPE_REF]{AllConstructors23b, p6930, Lp6930.AllConstructors23b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors23c[TYPE_REF]{AllConstructors23c, p6930, Lp6930.AllConstructors23c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_24() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors24a.java",
				"package p6930;\n" +
				"public class AllConstructors24a {\n" +
				"	public AllConstructors24a(){}\n" +
				"	private static class AllConstructorsInner{}\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors24b.java",
			"package p6930;\n" +
			"public class AllConstructors24b {\n" +
			"	public AllConstructors24b(){}\n" +
			"	private static class AllConstructorsInner{}\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors24c.java",
				"package p6930;"+
				"public class AllConstructors24c {\n" +
				"	public AllConstructors24c(){}\n" +
				"	private static class AllConstructorsInner{}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors24a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors24a;, ()V, AllConstructors24a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors24a[TYPE_REF]{p6930.AllConstructors24a, p6930, Lp6930.AllConstructors24a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors24b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors24b;, ()V, AllConstructors24b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors24b[TYPE_REF]{p6930.AllConstructors24b, p6930, Lp6930.AllConstructors24b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors24c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors24c;, ()V, AllConstructors24c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors24c[TYPE_REF]{p6930.AllConstructors24c, p6930, Lp6930.AllConstructors24c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_25() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructors25a.java",
				"package p6930;\n" +
				"public class AllConstructors25a {\n" +
				"	public AllConstructors25a(){}\n" +
				"	private static class AllConstructorsInner{}\n" +
				"}");
		
		createJar(new String[] {
			"p6930/AllConstructors25b.java",
			"package p6930;\n" +
			"public class AllConstructors25b {\n" +
			"	public AllConstructors25b(){}\n" +
			"	private static class AllConstructorsInner{}\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"import p6930.AllConstructors25a;\n"+
				"import p6930.AllConstructors25b;\n"+
				"import p6930.AllConstructors25c;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors25c.java",
				"package p6930;"+
				"public class AllConstructors25c {\n" +
				"	public AllConstructors25c(){}\n" +
				"	private static class AllConstructorsInner{}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors25a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors25a;, ()V, AllConstructors25a, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors25a[TYPE_REF]{AllConstructors25a, p6930, Lp6930.AllConstructors25a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors25b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors25b;, ()V, AllConstructors25b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors25b[TYPE_REF]{AllConstructors25b, p6930, Lp6930.AllConstructors25b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors25c[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors25c;, ()V, AllConstructors25c, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors25c[TYPE_REF]{AllConstructors25c, p6930, Lp6930.AllConstructors25c;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_26() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo(p6930.AllConstructors26a var) {\n" +
				"    var.new AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors26a.java",
				"package p6930;"+
				"public class AllConstructors26a {\n" +
				"	public class AllConstructors26b {\n" +
				"	  public AllConstructors26b(int i) {}\n" +
				"	}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors26b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors26a$AllConstructors26b;, (I)V, AllConstructors26b, (i), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors26a.AllConstructors26b[TYPE_REF]{AllConstructors26b, p6930, Lp6930.AllConstructors26a$AllConstructors26b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_27() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new p6930.AllConstructors27a.AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors27a.java",
				"package p6930;"+
				"public class AllConstructors27a {\n" +
				"	public static class AllConstructors27b {\n" +
				"	  public AllConstructors27b(int i) {}\n" +
				"	}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors27b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors27a$AllConstructors27b;, (I)V, AllConstructors27b, (i), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors27a.AllConstructors27b[TYPE_REF]{AllConstructors27b, p6930, Lp6930.AllConstructors27a$AllConstructors27b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_28() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/p6930/Test.java",
				"package p6930;\n"+
				"class AllConstructors28a {\n" +
				"	public AllConstructors28a(int i) {}\n" +
				"}\n" +
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new p6930.AllConstructors\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p6930/AllConstructors28b.java",
				"package p6930;"+
				"public class AllConstructors28b {\n" +
				"	public AllConstructors28b(int i) {}\n" +
				"}");
		
		this.workingCopies[2] = getWorkingCopy(
				"/P/src/p6930b/AllConstructors28c.java",
				"package p6930b;"+
				"public class AllConstructors28c {\n" +
				"	public AllConstructors28c(int i) {}\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors28a[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors28a;, (I)V, AllConstructors28a, (i), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors28a[TYPE_REF]{AllConstructors28a, p6930, Lp6930.AllConstructors28a;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors28b[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors28b;, (I)V, AllConstructors28b, (i), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors28b[TYPE_REF]{AllConstructors28b, p6930, Lp6930.AllConstructors28b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_29() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib6930.jar"}, "bin");
		
		createJar(new String[] {
			"p6930/AllConstructors29.java",
			"package p6930;\n" +
			"public class AllConstructors29 {\n" +
			"  public AllConstructors29() {}\n" +
			"  public AllConstructors29(Object o) {}\n" +
			"  public AllConstructors29(Object o, String s) {}\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    p6930.AllConstructors29 var = new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors29[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors29;, ()V, AllConstructors29, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors29[TYPE_REF]{p6930.AllConstructors29, p6930, Lp6930.AllConstructors29;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors29[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors29;, (Ljava.lang.Object;)V, AllConstructors29, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors29[TYPE_REF]{p6930.AllConstructors29, p6930, Lp6930.AllConstructors29;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors29[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors29;, (Ljava.lang.Object;Ljava.lang.String;)V, AllConstructors29, (o, s), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors29[TYPE_REF]{p6930.AllConstructors29, p6930, Lp6930.AllConstructors29;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_30() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/AllConstructors30.java",
				"package test;"+
				"class AllConstructors30b {\n" +
				"  private class Innerb {}\n" +
				"}\n" +
				"public class AllConstructors30 {\n" +
				"  private class Inner {}\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors30[TYPE_REF]{AllConstructors30, test, Ltest.AllConstructors30;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors30[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors30;, ()V, AllConstructors30, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors30[TYPE_REF]{AllConstructors30, test, Ltest.AllConstructors30;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors30b[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors30b;, ()V, AllConstructors30b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors30b[TYPE_REF]{AllConstructors30b, test, Ltest.AllConstructors30b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_31() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/AllConstructors31.java",
				"package test;"+
				"class AllConstructors31b {\n" +
				"  private class Innerb {}\n" +
				"}\n" +
				"public class AllConstructors31 {\n" +
				"  public class Inner {\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    AllConstructors31.Inner var = new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors31[TYPE_REF]{AllConstructors31, test, Ltest.AllConstructors31;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors31[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors31;, ()V, AllConstructors31, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors31[TYPE_REF]{AllConstructors31, test, Ltest.AllConstructors31;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors31b[CONSTRUCTOR_INVOCATION]{(), Ltest.AllConstructors31b;, ()V, AllConstructors31b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors31b[TYPE_REF]{AllConstructors31b, test, Ltest.AllConstructors31b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_32() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  public class AllConstructors32b {\n" +
				"    private class Innerb {}\n" +
				"  }\n" +
				"  public class AllConstructors32 {\n" +
				"    public class Inner {}\n" +
				"    void foo() {\n" +
				"      new Test.AllConstructors\n" +
				"    }\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors32[CONSTRUCTOR_INVOCATION]{(), Ltest.Test$AllConstructors32;, ()V, AllConstructors32, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Test.AllConstructors32[TYPE_REF]{AllConstructors32, test, Ltest.Test$AllConstructors32;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors32b[CONSTRUCTOR_INVOCATION]{(), Ltest.Test$AllConstructors32b;, ()V, AllConstructors32b, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Test.AllConstructors32b[TYPE_REF]{AllConstructors32b, test, Ltest.Test$AllConstructors32b;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Test.AllConstructors32[TYPE_REF]{AllConstructors32, test, Ltest.Test$AllConstructors32;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_33() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		createFolder("/P/src/p6930");
		createFolder("/P/src/p6930_1");
		createFolder("/P/src/p6930_2");
		
		createFile(
				"/P/src/p6930/AllConstructors33.java",
				"package p6930;\n" +
				"import p6930_2.ParamType;\n" +
				"public class AllConstructors33 {\n" +
				"  public AllConstructors33(ParamType p11, ParamType p12) {}\n" +
				"  public AllConstructors33(p6930_1.ParamType p21, ParamType p22) {}\n" +
				"}");
		
		createFile(
				"/P/src/p6930_1/ParamType.java",
				"package p6930_1;\n" +
				"public class ParamType {\n" +
				"}");
		
		createFile(
				"/P/src/p6930_2/ParamType.java",
				"package p6930_2;\n" +
				"public class ParamType {\n" +
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors33[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors33;, (Lp6930_1.ParamType;Lp6930_2.ParamType;)V, AllConstructors33, (p21, p22), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors33[TYPE_REF]{p6930.AllConstructors33, p6930, Lp6930.AllConstructors33;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"AllConstructors33[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors33;, (Lp6930_2.ParamType;Lp6930_2.ParamType;)V, AllConstructors33, (p11, p12), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors33[TYPE_REF]{p6930.AllConstructors33, p6930, Lp6930.AllConstructors33;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_34() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		Map compileOptions = new HashMap();
		compileOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
	    
		String[] pathsAndContents =
			new String[] {
				"p6930/AllConstructors34.java",
				"package p6930;\n" +
				"public class AllConstructors34 {\n" +
				"  public AllConstructors34(Object o) {}\n" +
				"}"
			};
		createJar(
				pathsAndContents,
				p.getProject().getLocation().append("lib6930.jar").toOSString(),
				compileOptions);
		
		addLibraryEntry(p, "/P/lib6930.jar", null);
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors34[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors34;, (Ljava.lang.Object;)V, AllConstructors34, (arg0), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors34[TYPE_REF]{p6930.AllConstructors34, p6930, Lp6930.AllConstructors34;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
public void testBug6930_35() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		
		Map compileOptions = new HashMap();
		compileOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
	    
		String[] pathsAndContents =
			new String[] {
				"p6930/AllConstructors35.java",
				"package p6930;\n" +
				"public class AllConstructors35 {\n" +
				"  public AllConstructors35(Object o) {}\n" +
				"}"
			};
		createJar(
				pathsAndContents,
				p.getProject().getLocation().append("lib6930.jar").toOSString(),
				compileOptions);
		
		createSourceZip(
				pathsAndContents,
				p.getProject().getLocation().append("lib6930src.zip").toOSString());
		
		addLibraryEntry(p, "/P/lib6930.jar", "/P/lib6930src.zip");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors35[CONSTRUCTOR_INVOCATION]{(), Lp6930.AllConstructors35;, (Ljava.lang.Object;)V, AllConstructors35, (o), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors35[TYPE_REF]{p6930.AllConstructors35, p6930, Lp6930.AllConstructors35;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=276890
public void testBug276890_01() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB"}, "bin", "1.5");
		
		createFolder("/P/src/p276890");
		
		createFile(
				"/P/src/p276890/Stuff.java",
				"package p276890;\n" +
				"public class Stuff<E> {\n"+
				"  public Stuff(E e) {}\n"+
				"  public Stuff(Object o, Object o2) {}\n"+
				"  public Stuff(Stuff<E> ees) {}\n"+
				"  public Stuff() {}\n"+
				"}");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new Stuf\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "Stuf";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, ()V, Stuff, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (Ljava.lang.Object;Ljava.lang.Object;)V, Stuff, (o, o2), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (Lp276890.Stuff<TE;>;)V, Stuff, (ees), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (TE;)V, Stuff, (e), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=276890
public void testBug276890_02() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB"}, "bin", "1.5");
		
		createFolder("/P/src/p276890");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new Stuf\n" +
				"  }\n" +
				"}");
		
		this.workingCopies[1] = getWorkingCopy(
				"/P/src/p276890/Stuff.java",
				"package p276890;\n" +
				"public class Stuff<E> {\n"+
				"  public Stuff(E e) {}\n"+
				"  public Stuff(Object o, Object o2) {}\n"+
				"  public Stuff(Stuff<E> ees) {}\n"+
				"  public Stuff() {}\n"+
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "Stuf";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, ()V, Stuff, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (Ljava.lang.Object;Ljava.lang.Object;)V, Stuff, (o, o2), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (Lp276890.Stuff<TE;>;)V, Stuff, (ees), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (TE;)V, Stuff, (e), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=276890
public void testBug276890_03() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL15_LIB", "/P/lib276890.jar"}, "bin", "1.5");
		
		createFolder("/P/src/p276890");
		
		createJar(
				new String[] {
						"p276890/Stuff.java",
						"package p276890;\n" +
						"public class Stuff<E> {\n"+
						"  public Stuff(E e) {}\n"+
						"  public Stuff(Object o, Object o2) {}\n"+
						"  public Stuff(Stuff<E> ees) {}\n"+
						"  public Stuff() {}\n"+
						"}"
				},
				p.getProject().getLocation().append("lib276890.jar").toOSString(),
				new String[]{getExternalJCLPathString("1.5")},
				"1.5");
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;\n"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new Stuf\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "Stuf";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, ()V, Stuff, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (Ljava.lang.Object;Ljava.lang.Object;)V, Stuff, (o, o2), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (Lp276890.Stuff<TE;>;)V, Stuff, (ees), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"Stuff[CONSTRUCTOR_INVOCATION]{(), Lp276890.Stuff;, (TE;)V, Stuff, (e), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   Stuff[TYPE_REF]{p276890.Stuff, p276890, Lp276890.Stuff;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
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
public void testBug237469a() throws Exception {
	String externalJar1 = Util.getOutputDirectory() + File.separator + "bug237469a.jar"; //$NON-NLS-1$
	String externalJar2 = Util.getOutputDirectory() + File.separator + "bug237469b.jar"; //$NON-NLS-1$
	
	try {
		// create variable
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);
		
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test/IProject.java", //$NON-NLS-1$
					"package test;\n" + //$NON-NLS-1$
					"public class IProject {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		Util.createJar(
				new String[] {
					"test/IJavaProject.java", //$NON-NLS-1$
					"package test;\n" + //$NON-NLS-1$
					"import test.IProject;\n" + //$NON-NLS-1$
					"public class IJavaProject {\n" + //$NON-NLS-1$
					"	IProject project = null;\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1, externalJar2},
			 "bin");

		this.createFolder("/PS1/src/test");
		this.createFile(
				"/PS1/src/test/Y.java",
				"package test;\n"+
				"import test.IProject;\n"+
				"import test.IJavaProject;\n"+
				"public class Y {\n"+
				"  IProject project;\n"+
				"  IJavaProject javaProject;\n"+
				"}");

		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			new String[]{"/PS1"},
			"bin");

		this.createFolder("/PS2/src/test");
		this.createFile(
				"/PS2/src/test/X.java",
				"package test;\n"+
				"public class X extends test.Y {\n"+
				"  private Object initializer;\n"+
				"  public void foo() {\n"+
				"    initializer\n"+
				"  }\n"+
				"}");

		waitUntilIndexesReady();

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("PS2", "src", "test", "X.java");

		String str = cu.getSource();
		String completeBehind = "initializer";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);

		assertResults(
			"initializer[FIELD_REF]{initializer, Ltest.X;, Ljava.lang.Object;, initializer, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
	} finally {
		this.deleteProject("PS1");
		this.deleteProject("PS2");
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
	}
}
public void testBug237469b() throws Exception {
	String externalJar1 = Util.getOutputDirectory() + File.separator + "bug237469a.jar"; //$NON-NLS-1$
	String externalJar2 = Util.getOutputDirectory() + File.separator + "bug237469b.jar"; //$NON-NLS-1$
	
	try {
		// create variable
//		JavaCore.setClasspathVariables(
//			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
//			new IPath[] {getExternalJCLPath(), getExternalJCLSourcePath(), getExternalJCLRootSourcePath()},
//			null);
		
		
		// create external jar 1
		Util.createJar(
				new String[] {
					"test/IProject.java", //$NON-NLS-1$
					"package test;\n" + //$NON-NLS-1$
					"public class IProject {\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				new HashMap(),
				externalJar1);
		
		// create external jar 2
		Util.createJar(
				new String[] {
					"test/IJavaProject.java", //$NON-NLS-1$
					"package test;\n" + //$NON-NLS-1$
					"import test.IProject;\n" + //$NON-NLS-1$
					"public class IJavaProject {\n" + //$NON-NLS-1$
					"	IProject project = null;\n" + //$NON-NLS-1$
					"}" //$NON-NLS-1$
				},
				null,
				new HashMap(),
				new String[]{externalJar1},
				externalJar2);

		// create P1
		this.createJavaProject(
			"PS1",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar1, externalJar2},
			 "bin");

		this.createFolder("/PS1/src/test");
		this.createFile(
				"/PS1/src/test/Y.java",
				"package test;\n"+
				"import test.IProject;\n"+
				"import test.IJavaProject;\n"+
				"public class Y {\n"+
				"  IProject project;\n"+
				"  IJavaProject javaProject;\n"+
				"}");

		// create P2
		this.createJavaProject(
			"PS2",
			new String[]{"src"},
			new String[]{"JCL_LIB", externalJar2},
			new String[]{"/PS1"},
			"bin");

		this.createFolder("/PS2/src/test");
		this.createFile(
				"/PS2/src/test/X.java",
				"package test;\n"+
				"public class X extends test.Y {\n"+
				"  private X initializer;\n"+
				"  public void foo() {\n"+
				"    Object o; o.equals\n"+
				"  }\n"+
				"}");

		waitUntilIndexesReady();

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, false, false, false);
		requestor.setRequireExtendedContext(true);
		requestor.setComputeEnclosingElement(false);
		requestor.setComputeVisibleElements(true);
		requestor.setAssignableType("Ltest/X;");
		
		ICompilationUnit cu= getCompilationUnit("PS2", "src", "test", "X.java");

		String str = cu.getSource();
		String completeBehind = "equals";
		
		int tokenStart = str.lastIndexOf(completeBehind);
		int tokenEnd = tokenStart + completeBehind.length() - 1;
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
		
		assertResults(
				"completion offset="+(cursorLocation)+"\n" +
				"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
				"completion token=\"equals\"\n" +
				"completion token kind=TOKEN_KIND_NAME\n" +
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null\n"+
				"completion token location=UNKNOWN\n"+
				"visibleElements={\n" +
				"	initializer {key=Ltest/X;.initializer)Ltest/X;} [in X [in X.java [in test [in src [in PS2]]]]],\n" +
				"}",
				requestor.getContext());
	} finally {
		this.deleteProject("PS1");
		this.deleteProject("PS2");
		this.deleteExternalFile(externalJar1);
		this.deleteExternalFile(externalJar2);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=270113
public void testBug270113_01() throws Exception {
	Hashtable oldOptions = JavaCore.getOptions();
	
	try {
		Hashtable options = new Hashtable(oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/lib270113.jar"}, "bin");
		
		createJar(new String[] {
			"p270113/AllConstructors01.java",
			"package p270113;\n" +
			"public abstract class AllConstructors01 {\n" +
			"  protected AllConstructors01(int i) {}\n" +
			"}"
		}, p.getProject().getLocation().append("lib270113.jar").toOSString());
		
		refresh(p);
		
		waitUntilIndexesReady();
		
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new AllConstructors\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "AllConstructors";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"AllConstructors01[ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION]{(), Lp270113.AllConstructors01;, (I)V, AllConstructors01, (i), "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}\n" +
			"   AllConstructors01[TYPE_REF]{p270113.AllConstructors01, p270113, Lp270113.AllConstructors01;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
		
		JavaCore.setOptions(oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=281598
public void testBug281598() throws Exception {

	try {
		// Create project and jar
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/empty.jar"}, "bin");
		createFile("/P/empty.jar", "");
		refresh(p);
		waitUntilIndexesReady();

		// Create working copy
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    sys\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor() {
			long start = System.currentTimeMillis();

			public boolean isCanceled() {
	            long time = System.currentTimeMillis() - this.start;
	            return time > 1000; // cancel after 1 sec
            }
			
		};

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "sys";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    // no results expected, just verify that no cancel operation exception occurs...
	    assertResults("", requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug281598b() throws Exception {
	try {
		// Create project and jar
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB", "/P/empty.jar"}, "bin");
		createFile("/P/empty.jar", "");
		refresh(p);
		waitUntilIndexesReady();

		// Create working copy
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/P/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    new String\n" +
				"  }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor() {
			long start = System.currentTimeMillis();

			public boolean isCanceled() {
	            long time = System.currentTimeMillis() - this.start;
	            return time > 1000; // cancel after 1 sec
            }
			
		};

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "String";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"String[CONSTRUCTOR_INVOCATION]{(), Ljava.lang.String;, ()V, String, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_UNQUALIFIED+R_NON_RESTRICTED)+"}\n" +
			"   String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		deleteProject("P");
	}
}
public void testBug281598c() throws Exception {
	IndexManager indexManager = JavaModelManager.getIndexManager();
	try {
		// Create project
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[]{"JCL_LIB"}, "bin");
		waitUntilIndexesReady();
		
		// Disable indexing
		indexManager.disable();

		// Create compilation unit in which completion occurs
		String path = "/P/src/test/Test.java";
		String source = "package test;\n" +
			"public class Test {\n" +
			"	public void foo() {\n" +
			"		Strin\n" +
			"	}\n" +
			"}\n";
		createFolder("/P/src/test");
		createFile(path, source);
		refresh(p);

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, true, true);
		requestor.allowAllRequiredProposals();
		NullProgressMonitor monitor = new NullProgressMonitor() {
			long start = System.currentTimeMillis();

			public boolean isCanceled() {
	            long time = System.currentTimeMillis() - this.start;
	            return time > 1000; // cancel after 1 sec
            }
			
		};

	    String completeBehind = "Strin";
	    int cursorLocation = source.lastIndexOf(completeBehind) + completeBehind.length();
	    IType type = p.findType("test.Test");
	    type.getTypeRoot().codeComplete(cursorLocation, requestor, this.wcOwner, monitor);
	    
	    assertResults(
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED)+"}",
			requestor.getResults());
	} finally {
		indexManager.enable();
		deleteProject("P");
	}
}
}
