/*******************************************************************************
 * Copyright (c) 2003 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Vector;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.JavaWorkspaceScope;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.processing.IJob;

import junit.framework.Test;

/*
 * Test indexing support.
 */
public class SearchTests extends ModifyingResourceTests implements IJavaSearchConstants {
	class WaitingJob implements IJob {
		boolean isRunning;
		boolean isResumed;
		public boolean belongsTo(String jobFamily) {
			return false;
		}
		public void cancel() {
		}
		public boolean execute(IProgressMonitor progress) {
			startJob();
			suspend();
			return true;
		}
		public boolean isReadyToRun() {
			return true;
		}
		public synchronized void resume() {
			this.isResumed = true;
			notifyAll();
		}
		public synchronized void startJob() {
			this.isRunning = true;
			notifyAll();
		}
		public synchronized void suspend() {
			if (this.isResumed) return;
			try {
				wait(60000); // wait 1 minute max
			} catch (InterruptedException e) {
			}
		}
		public synchronized void waitForJobToStart() {
			if (this.isRunning) return;
			try {
				wait(60000); // wait 1 minute max
			} catch (InterruptedException e) {
			}
		}
	}
	class TypeNameRequestor implements ITypeNameRequestor {
		Vector results = new Vector();
		public void acceptClass(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			acceptType(packageName, simpleTypeName, enclosingTypeNames);
		}
		public void acceptInterface(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			acceptType(packageName, simpleTypeName, enclosingTypeNames);
		}
		private void acceptType(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames) {
			char[] typeName = 
				CharOperation.concat(
					CharOperation.concatWith(enclosingTypeNames, '$'), 
					simpleTypeName,
					'$');
			results.addElement(new String(CharOperation.concat(packageName, typeName, '.')));
		}
		public String toString(){
			int length = results.size();
			String[] strings = new String[length];
			results.toArray(strings);
			org.eclipse.jdt.internal.core.Util.sort(strings);
			StringBuffer buffer = new StringBuffer(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
	}

public SearchTests(String name) {
	super(name);
}
public static Test suite() {
	return new Suite(SearchTests.class);
}
protected void assertAllTypes(String message, String expected) throws JavaModelException {
	SearchEngine searchEngine = new SearchEngine();
	TypeNameRequestor requestor = new TypeNameRequestor();
	searchEngine.searchAllTypeNames(
		ResourcesPlugin.getWorkspace(),
		null,
		null,
		PATTERN_MATCH,
		CASE_INSENSITIVE,
		TYPE,
		new JavaWorkspaceScope(), 
		requestor,
		WAIT_UNTIL_READY_TO_SEARCH,
		null);
	String actual = requestor.toString();
	if (!expected.equals(actual)){
	 	System.out.println(Util.displayString(actual, 3));
	}
	assertEquals(
		message,
		expected,
		actual);
}
protected void assertAllTypes(String expected) throws JavaModelException {
	assertAllTypes("Unexpected all types", expected);
}

/*
 * Ensure that changing the classpath in the middle of reindexing
 * a project causes another request to reindex.
 */
public void testChangeClasspath() throws CoreException {
	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
	WaitingJob job = new WaitingJob();
	try {
		// setup: suspend indexing and create a project (prj=src) with one cu
		indexManager.disable();
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createJavaProject("P");
				createFile(
					"/P/X.java",
					"public class X {\n" +
					"}"
				);
			}
		}, null);
		
		// add waiting job and wait for it to be executed
		indexManager.request(job);
		indexManager.enable();
		job.waitForJobToStart();
		
		// remove source folder from classpath
		getJavaProject("P").setRawClasspath(
			new IClasspathEntry[0], 
			null);
			
		// resume waiting job
		job.resume();
		
		assertAllTypes(
			"Unexpected all types after removing source folder",
			""
		);
	} finally {
		job.resume();
		deleteProject("P");
		indexManager.enable();
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
				IJavaProject project = createJavaProject("P");
				project.setRawClasspath(
					createClasspath(new String[] {"/P/src1", "src2/", "/P/src1/src2", ""}), 
					new Path("/P/bin"),
					null);
				createFolder("/P/src1/src2");
				createFile(
					"/P/src1/src2/X.java",
					"public class X {\n" +
					"}"
				);
			}
		}, null);
		assertAllTypes(
			"Unexpected all types after setup",
			"X"
		);
		
		// remove outer folder from classpath
		getJavaProject("P").setRawClasspath(
			createClasspath(new String[] {"/P/src1/src2", ""}), 
			null);
		assertAllTypes(
			"Unexpected all types after removing outer folder",
			"X"
		);
		
	} finally {
		deleteProject("P");
	}
}
}
