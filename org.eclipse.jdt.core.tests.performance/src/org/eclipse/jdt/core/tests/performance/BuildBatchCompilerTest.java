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
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class BuildBatchCompilerTest extends Tests {

	public BuildBatchCompilerTest(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(BuildBatchCompilerTest.class);
	}

	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.performance").getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
			return new File(Platform.asLocalURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public void testBuildCompilerUsingBatchCompiler() throws IOException {
		String batchCompilerSource = getPluginDirectoryPath() + File.separator + "compiler-R3_0.zip"; //$NON-NLS-1$
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();
		final String compilerPath = targetWorkspacePath + File.separator + "compiler"; //$NON-NLS-1$
		final String sources = compilerPath + File.separator + "src"; //$NON-NLS-1$
		final String bins = compilerPath + File.separator + "bin"; //$NON-NLS-1$
		final String logs = compilerPath + File.separator + "log.txt"; //$NON-NLS-1$
		Util.unzip(batchCompilerSource, targetWorkspacePath);

		// Note this test is not a finger print test, so we don't want to use tagAsGlobalSummary(...)
		Main.compile(sources + " -1.4 -g -preserveAllLocals -enableJavadoc -nowarn -d " + bins + " -log " + logs); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < 10; i++) {
			startMeasuring();
			Main.compile(sources + " -1.4 -g -preserveAllLocals -enableJavadoc -nowarn -d " + bins + " -log " + logs); //$NON-NLS-1$ //$NON-NLS-2$
			stopMeasuring();
			cleanupDirectory(new File(bins));
		}
		commitMeasurements();
		assertPerformance();
		
		File logsFile = new File(logs);
		assertTrue("No log file", logsFile.exists());
		assertEquals("Has errors", 0, logsFile.length());
		cleanupDirectory(new File(compilerPath));
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
