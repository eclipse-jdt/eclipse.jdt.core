/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.test.performance.Dimension;

public class BuildBatchCompilerTest extends FullSourceWorkspaceTests {

	public BuildBatchCompilerTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		return buildSuite(BuildBatchCompilerTest.class);
	}
		
	public void testPerfBuildCompilerUsingBatchCompiler() throws IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath =  workspaceRoot.getProject(JavaCore.PLUGIN_ID).getLocation().toFile().getCanonicalPath();
		final String compilerPath = targetWorkspacePath + File.separator + "src"; //$NON-NLS-1$
		final String sources = targetWorkspacePath + File.separator + "compiler"; //$NON-NLS-1$
		final String bins = targetWorkspacePath + File.separator + "bin"; //$NON-NLS-1$
		final String logs = targetWorkspacePath + File.separator + "log.txt"; //$NON-NLS-1$

		// Note this test is not a finger print test, so we don't want to use tagAsGlobalSummary(...)
		tagAsSummary("Build jdt-core/compiler using batch compiler", Dimension.CPU_TIME);
		
		// Compile 10 times
		Main.compile(sources + " -1.4 -g -preserveAllLocals -nowarn -d " + bins + " -log " + logs); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < 10; i++) {
			startMeasuring();
			Main.compile(sources + " -1.4 -g -preserveAllLocals -nowarn -d " + bins + " -log " + logs); //$NON-NLS-1$ //$NON-NLS-2$
			stopMeasuring();
			cleanupDirectory(new File(bins));
		}
		commitMeasurements();
		assertPerformance();
		
		File logsFile = new File(logs);
		assertTrue("No log file", logsFile.exists());
		assertEquals("Has errors", 0, logsFile.length());
	}
	
	protected void cleanupDirectory(File directory) {
		if (!directory.isDirectory() || !directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete())
					System.out.println("Could not delete file " + file.getPath()); //$NON-NLS-1$
			}
		}
		if (!directory.delete())
			System.out.println("Could not delete directory " + directory.getPath()); //$NON-NLS-1$
	}
}
