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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.core.JavaElement;

/*
 * Tests that modify resources in the workspace.
 */
public class ModifyingResourceTests extends AbstractJavaModelTests {
	
public ModifyingResourceTests(String name) {
	super(name);
}
protected void assertDeltas(String message, String expected) {
	StringBuffer buffer = new StringBuffer();
	IJavaElementDelta[] deltas = this.deltaListener.deltas;
	for (int i=0, length= deltas.length; i<length; i++) {
		IJavaElementDelta[] projects = deltas[i].getAffectedChildren();
		for (int j=0, projectsLength=projects.length; j<projectsLength; j++) {
			buffer.append(projects[j]);
			if (j != projectsLength-1) {
				buffer.append("\n");
			}
		}
		IResourceDelta[] nonJavaProjects = deltas[i].getResourceDeltas();
		if (nonJavaProjects != null) {
			for (int j=0, nonJavaProjectsLength=nonJavaProjects.length; j<nonJavaProjectsLength; j++) {
				if (j == 0 && buffer.length() != 0) {
					buffer.append("\n");
				}
				buffer.append(nonJavaProjects[j]);
				if (j != nonJavaProjectsLength-1) {
					buffer.append("\n");
				}
			}
		}
		if (i != length-1) {
			buffer.append("\n\n");
		}
	}
	String actual = buffer.toString();
	if (!expected.equals(actual)){
	 	System.out.println(Util.displayString(actual, 3));
	}
	assertEquals(
		message,
		expected,
		actual);
}
/**
 * E.g. <code>
 * org.eclipse.jdt.tests.core.ModifyingResourceTests.generateClassFile(
 *   "A",
 *   "public class A {\n" +
 *   "}")
 */
public static void generateClassFile(String className, String javaSource) throws IOException {
	String cu = "d:/temp/" + className + ".java";
	FileOutputStream output = new FileOutputStream(cu);
	try {
		output.write(javaSource.getBytes());
	} finally {
		output.close();
	}
	Main.compile(cu + " -d d:/temp -classpath " + System.getProperty("java.home") + "/lib/rt.jar");
	FileInputStream input = new FileInputStream("d:/temp/" + className + ".class");
	try {
		System.out.println("{");
		byte[] buffer = new byte[80];
		int read = 0;
		while (read != -1) {
			read = input.read(buffer);
			if (read != -1) System.out.print("\t");
			for (int i = 0; i < read; i++) {
				System.out.print(buffer[i]);
				System.out.print(", ");	
			}
			if (read != -1) System.out.println();
		}
		System.out.print("}");
	} finally {
		input.close();
	}
}
	
protected IFile createFile(String path, byte[] content) throws CoreException {
	IFile file = this.getFile(path);
	InputStream input = new ByteArrayInputStream(content);
	file.create(input, true, null);
	return file;
}

protected IFile createFile(String path, String content) throws CoreException {
	IFile file = this.getFile(path);
	InputStream input = new ByteArrayInputStream(content.getBytes());
	file.create(input, true, null);
	return file;
}
protected IFolder createFolder(String path) throws CoreException {
	IFolder folder = this.getFolder(path);
	IContainer parent = folder.getParent();
	if (parent instanceof IFolder && !parent.exists()) {
		this.createFolder(parent.getFullPath().toString());
	} 
	folder.create(true, true, null);
	return folder;
}
protected void deleteFile(String filePath) throws CoreException {
	this.getFile(filePath).delete(true, null);
}
protected void deleteFolder(String folderPath) throws CoreException {
	this.getFolder(folderPath).delete(true, null);
}
protected IFile editFile(String path, String content) throws CoreException {
	IFile file = this.getFile(path);
	InputStream input = new ByteArrayInputStream(content.getBytes());
	file.setContents(input, IFile.FORCE, null);
	return file;
}
/* 
 * Expands (i.e. open) the given element and returns a toString() representation
 * of the tree.
 */
protected String expandAll(IJavaElement element) throws CoreException {
	StringBuffer buffer = new StringBuffer();
	this.expandAll(element, 0, buffer);
	return buffer.toString();
}
private void expandAll(IJavaElement element, int tab, StringBuffer buffer) throws CoreException {
	IJavaElement[] children = null;
	// force opening of element by getting its children
	if (element instanceof IParent) {
		IParent parent = (IParent)element;
		children = parent.getChildren();
	}
	((JavaElement)element).toStringInfo(tab, buffer);
	if (children != null) {
		for (int i = 0, length = children.length; i < length; i++) {
			buffer.append("\n");
			this.expandAll(children[i], tab+1, buffer);
		}
	}
}
protected void renameProject(String project, String newName) throws CoreException {
	this.getProject(project).move(new Path(newName), true, null);
}
protected ICompilationUnit getCompilationUnit(String path) {
	return (ICompilationUnit)JavaCore.create(this.getFile(path));
}
protected IClassFile getClassFile(String path) {
	return (IClassFile)JavaCore.create(this.getFile(path));
}
protected IFile getFile(String path) {
	return getWorkspaceRoot().getFile(new Path(path));
}
protected IFolder getFolder(String path) {
	return getWorkspaceRoot().getFolder(new Path(path));
}
protected IPackageFragment getPackage(String path) {
	if (path.indexOf('/', 1) != -1) { // if path as more than one segment
		IJavaElement element = JavaCore.create(this.getFolder(path));
		if (element instanceof IPackageFragmentRoot) {
			return ((IPackageFragmentRoot)element).getPackageFragment("");
		} else {
			return (IPackageFragment)element;
		}
	} else {
		IProject project = this.getProject(path);
		return JavaCore.create(project).getPackageFragmentRoot(project).getPackageFragment("");
	}
}
protected IPackageFragmentRoot getPackageFragmentRoot(String path) {
	if (path.indexOf('/', 1) != -1) { // if path as more than one segment
		if (path.endsWith(".jar")) {
			return  (IPackageFragmentRoot)JavaCore.create(this.getFile(path));
		} else {
			return (IPackageFragmentRoot)JavaCore.create(this.getFolder(path));
		}
	} else {
		IProject project = this.getProject(path);
		return JavaCore.create(project).getPackageFragmentRoot(project);
	}
}
protected String getSortedByProjectDeltas() {
	StringBuffer buffer = new StringBuffer();
	for (int i=0, length = this.deltaListener.deltas.length; i<length; i++) {
		IJavaElementDelta[] projects = this.deltaListener.deltas[i].getAffectedChildren();
		int projectsLength = projects.length;
		
		// sort by project
		IJavaElementDelta[] sorted = new IJavaElementDelta[projectsLength];
		System.arraycopy(projects, 0, sorted, 0, projectsLength);
		org.eclipse.jdt.internal.core.Util.sort(
			sorted, 
			new  org.eclipse.jdt.internal.core.Util.Comparer() {
				public int compare(Object a, Object b) {
					return a.toString().compareTo(b.toString());
				}
			});
		
		for (int j=0; j<projectsLength; j++) {
			buffer.append(sorted[j]);
			if (j != projectsLength-1) {
				buffer.append("\n");
			}
		}
		if (i != length-1) {
			buffer.append("\n\n");
		}
	}
	return buffer.toString();
}
protected void moveFile(String sourcePath, String destPath) throws CoreException {
	this.getFile(sourcePath).move(this.getFile(destPath).getFullPath(), false, null);
}
protected void moveFolder(String sourcePath, String destPath) throws CoreException {
	this.getFolder(sourcePath).move(this.getFolder(destPath).getFullPath(), false, null);
}
protected void swapFiles(String firstPath, String secondPath) throws CoreException {
	final IFile first = this.getFile(firstPath);
	final IFile second = this.getFile(secondPath);
	IWorkspaceRunnable runnable = new IWorkspaceRunnable(	) {
		public void run(IProgressMonitor monitor) throws CoreException {
			IPath tempPath = first.getParent().getFullPath().append("swappingFile.temp");
			first.move(tempPath, false, monitor);
			second.move(first.getFullPath(), false, monitor);
			getWorkspaceRoot().getFile(tempPath).move(second.getFullPath(), false, monitor);
		}
	};
	getWorkspace().run(runnable, null);
}
/*
 * Returns a new classpath from the given source folders and their respective exclusion patterns.
 * The given array as the following form:
 * [<source folder>, "<pattern>[|<pattern]*"]*
 * E.g. new String[] {
 *   "src1", "p/A.java",
 *   "src2", "*.txt|com.tests/**"
 * }
 */
protected IClasspathEntry[] createClasspath(String[] sourceFoldersAndExclusionPatterns) {
	int length = sourceFoldersAndExclusionPatterns.length;
	IClasspathEntry[] classpath = new IClasspathEntry[length/2];
	for (int i = 0; i < length; i+=2) {
		String src = sourceFoldersAndExclusionPatterns[i];
		String patterns = sourceFoldersAndExclusionPatterns[i+1];
		StringTokenizer tokenizer = new StringTokenizer(patterns, "|");
		int patternsCount =  tokenizer.countTokens();
		IPath[] patternPaths = new IPath[patternsCount];
		for (int j = 0; j < patternsCount; j++) {
			patternPaths[j] = new Path(tokenizer.nextToken());
		}
		classpath[i/2] = JavaCore.newSourceEntry(new Path(src), patternPaths); 
	}
	return classpath;
}

}
