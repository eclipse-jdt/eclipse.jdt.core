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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.test.performance.Dimension;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FullSourceWorkspaceTests extends Tests {

	public FullSourceWorkspaceTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new TestSuite(FullSourceWorkspaceTests.class);
	}

	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		CodeSource javaCoreCodeSource = JavaCore.class.getProtectionDomain().getCodeSource();
		if (javaCoreCodeSource != null) {
			URL javaCoreUrl = javaCoreCodeSource.getLocation();
			String javaCorePath = javaCoreUrl.getFile();
			int index = javaCorePath.indexOf(JavaCore.PLUGIN_ID);
			if (index != -1) {
				String pluginsPath = javaCorePath.substring(0, index);
				File pluginsFile = new File(pluginsPath);
				String[] list = pluginsFile.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith( "org.eclipse.jdt.core.tests.performance");
					}
				});
				if (list != null && list.length > 0) {
					return pluginsPath + list[0];
				}
			}
		}
		return null;
	}
	
	private void setUpFullSourceWorkspace() throws IOException, CoreException {
		String fullSourceZipPath = getPluginDirectoryPath() + File.separator + "full-source-R3_0.zip";
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();
		
		Util.unzip(fullSourceZipPath, targetWorkspacePath);
		
		String jdkLib = Util.getJavaClassLib();
		JavaCore.setClasspathVariable("JRE_LIB", new Path(jdkLib), null);
		
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				File targetWorkspaceDir = new File(targetWorkspacePath);
				String[] projectNames = targetWorkspaceDir.list();
				for (int i = 0, length = projectNames.length; i < length; i++) {
					String projectName = projectNames[i];
					if (".metadata".equals(projectName)) continue;
					IProject project = workspaceRoot.getProject(projectName);
					project.create(monitor);
					project.open(monitor);
				}
			}
		}, null);
		
		// workaround bug 73253 Project references not set on project open 
		IJavaProject[] projects = JavaCore.create(workspaceRoot).getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			projects[i].setRawClasspath(projects[i].getRawClasspath(), null);
		}
	}
		
	public void testPerfFullBuild() throws IOException, CoreException {
		setUpFullSourceWorkspace();
		tagAsGlobalSummary("Full source workspace build", Dimension.CPU_TIME);
		startMeasuring();
		fullBuild();
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

}
