package org.eclipse.jdt.core.tests.model;

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
import java.util.HashMap;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

import junit.framework.*;

/**
 * DO NOT RELEASE AS PART OF REGRESSION TEST - if failing, can cause testing hang
 */
public class ThreadSafetyTests extends ModifyingResourceTests {

	public class Semaphore {
		private int status = 0;
		public Semaphore() { // mutex by default
			this(1);
		}
		public Semaphore(int status) {
			this.status = status;
		}
		public synchronized void acquire() {
			while (status <= 0){
				try {
					wait();
				} catch(InterruptedException e){
				}
			}
			status--;
		}
		public synchronized void release() {
			if (++status > 0) notifyAll();
		}
	}

public ThreadSafetyTests(String name) {
	super(name);
}

public static Test suite() {

	if (false){
		TestSuite suite = new Suite(ThreadSafetyTests.class.getName());
		suite.addTest(new ClasspathTests("testDeadlock01"));
		return suite;
	}
	return new Suite(ThreadSafetyTests.class);	
}
/**
 * 32905 - deadlocked if activating initializer while some concurrent action is populating the JavaModel
 */
public void testDeadlock01() throws CoreException {

	System.out.println("Test deadlock scenario");
	try {
		ContainerInitializer.setInitializer(new ClasspathInitializerTests.DefaultContainerInitializer(new String[] {"P", ""}){
			public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
				System.out.println(Thread.currentThread() + "initializer has started: attempting to acquire workspace lock");
				super.initialize(containerPath, project);
				System.out.println(Thread.currentThread() + "initializer has finished");
			}

		});
		final IJavaProject project = this.createJavaProject(
				"P", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
		
		// simulate state on startup (flush containers, and discard their previous values)
		project.getJavaModel().close();
		JavaModelManager.PreviousSessionContainers = new HashMap(5);
		JavaModelManager.Containers = new HashMap(5);
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)project);

		// use a thread to hold the lock, so as to recreate potential deadlock situation
		final Semaphore permissionToPopulateModel = new Semaphore(0); // first acquisition will wait
		final Semaphore permissionToModifyResource = new Semaphore(0); // first acquisition to wait
		final Semaphore hasCompleted = new Semaphore(0); 
		
		// trigger some delta notification in different thread
		Thread performJavaOperationInsideWorkspaceLock = new Thread(new Runnable(){
				public void run() {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor)	throws CoreException {
								System.out.println(Thread.currentThread() + " got workspace lock");
								permissionToPopulateModel.release();
								permissionToModifyResource.acquire(); // ensure the java model lock is taken
								ThreadSafetyTests.this.createFile("/P/X.java", "public class X {}");
								System.out.println(Thread.currentThread() + " created file X.java");
							}
						}, null);
					} catch (CoreException e) {
					}
					hasCompleted.release();
					System.out.println(Thread.currentThread() +" ResourceModification DONE");
				}
			},"ModifyResource");
		performJavaOperationInsideWorkspaceLock.setDaemon(true);
		performJavaOperationInsideWorkspaceLock.start();

		Thread attemptPopulateTheJavaModel = new Thread(new Runnable(){
				public void run() {
					try {
						//synchronized(JavaModelManager.getJavaModelManager()) {
							permissionToPopulateModel.acquire(); // ensure workspace lock is taken already
							System.out.println(Thread.currentThread() + " about to take Java model lock");
							// needs the JavaModel lock to populate the project
							project.getChildren(); // trigger classpath initializer activation (requires workspace lock)
							permissionToModifyResource.release();
							System.out.println(Thread.currentThread() + " done populating the model");
						//}
					} catch (JavaModelException e) {
					}
					hasCompleted.release();
					System.out.println(Thread.currentThread() +" Populate JavaModel DONE");
				}
			},"PopulateModel");
		attemptPopulateTheJavaModel.setDaemon(true);
		attemptPopulateTheJavaModel.start();

		hasCompleted.acquire(); // ensure both actions did complete
		hasCompleted.acquire();
		System.out.println("SUCCESS - no deadlock encountered");
	} finally {
		// cleanup  
		this.deleteProject("P");
	}
}
}
