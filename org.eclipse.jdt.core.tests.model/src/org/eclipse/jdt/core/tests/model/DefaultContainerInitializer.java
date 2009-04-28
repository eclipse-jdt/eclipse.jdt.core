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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;

public class DefaultContainerInitializer implements ContainerInitializer.ITestInitializer {

	public static class DefaultContainer implements IClasspathContainer {
		char[][] libPaths;
		public DefaultContainer(char[][] libPaths) {
			this.libPaths = libPaths;
		}
		public IClasspathEntry[] getClasspathEntries() {
			int length = this.libPaths.length;
			IClasspathEntry[] entries = new IClasspathEntry[length];
			for (int j = 0; j < length; j++) {
			    IPath path = new Path(new String(this.libPaths[j]));
			    if (path.segmentCount() == 1 && path.getDevice() == null) {
			        entries[j] = JavaCore.newProjectEntry(path);
			    } else {
					entries[j] = JavaCore.newLibraryEntry(path, null, null);
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

	/*
	 * values is [<project name>, <lib path>[,<lib path>]* ]*
	 */
	public DefaultContainerInitializer(String[] values) {
		this.containerValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			final String projectName = values[i];
			final char[][] libPaths = CharOperation.splitOn(',', values[i+1].toCharArray());
			this.containerValues.put(
				projectName,
				newContainer(libPaths)
			);
		}
	}
	protected DefaultContainerInitializer.DefaultContainer newContainer(final char[][] libPaths) {
		return new DefaultContainer(libPaths);
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