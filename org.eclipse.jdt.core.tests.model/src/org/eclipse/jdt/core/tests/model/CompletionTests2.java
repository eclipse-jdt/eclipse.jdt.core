/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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


public static Test suite() {
	TestSuite suite = new Suite(CompletionTests2.class.getName());
	
	suite.addTest(new CompletionTests2("testBug29832"));
	
	return suite;
}

private File createFile(File parent, String name, String content) throws IOException {
	File file = new File(parent, name);
	FileOutputStream out = new FileOutputStream(file);
	out.write(content.getBytes());
	out.close();
	return file;
}
private File createDirectory(File parent, String name) throws IOException {
	File dir = new File(parent, name);
	dir.mkdirs();
	return dir;
}
private void addLibraryEntry(IJavaProject project, String path, boolean exported) throws JavaModelException {
	this.addLibraryEntry(project, new Path(path), null, null, exported);
} 
private void addLibraryEntry(IJavaProject project, String path, String srcAttachmentPath, String srcAttachmentPathRoot, boolean exported) throws JavaModelException{
	this.addLibraryEntry(
		project,
		new Path(path),
		srcAttachmentPath == null ? null : new Path(srcAttachmentPath),
		srcAttachmentPathRoot == null ? null : new Path(srcAttachmentPathRoot),
		exported
	);
}
private void addLibraryEntry(IJavaProject project, IPath path, IPath srcAttachmentPath, IPath srcAttachmentPathRoot, boolean exported) throws JavaModelException{
	IClasspathEntry[] entries = project.getRawClasspath();
	int length = entries.length;
	System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 1, length);
	entries[0] = JavaCore.newLibraryEntry(path, srcAttachmentPath, srcAttachmentPathRoot, true);
	project.setRawClasspath(entries, null);
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
			new String[]{Util.getJavaClassLib()},
			 "");
		this.createFile("/P1/lib.jar", f.getContents());
		this.addLibraryEntry(p, "/P1/lib.jar", true);
		
		// create P2
		this.createJavaProject(
			"P2",
			new String[]{"src"},
			new String[]{Util.getJavaClassLib()},
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
			"element:ZZZ    completion:pz.ZZZ    relevance:18",
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
			"element:ZZZ    completion:pz.ZZZ    relevance:18",
			requestor.getResults());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}

}
