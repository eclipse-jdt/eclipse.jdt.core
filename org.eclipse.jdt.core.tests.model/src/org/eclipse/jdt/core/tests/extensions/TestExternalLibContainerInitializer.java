/*******************************************************************************
 * Copyright (c) 2010 Stephan Herrmann.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - inconsistent initialization of classpath container backed by external class folder, see https://bugs.eclipse.org/320618
 *******************************************************************************/
package org.eclipse.jdt.core.tests.extensions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class TestExternalLibContainerInitializer extends ClasspathContainerInitializer {

	IClasspathEntry[] entries;

	public void initialize(final IPath containerName, IJavaProject project)
			throws CoreException {
		IPath ws = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		final IPath containerPath = ws.append("../TestContainer/");
		IClasspathContainer container = new IClasspathContainer() {

			public IPath getPath() {
				return containerName;
			}

			public int getKind() {
				return IClasspathContainer.K_APPLICATION;
			}

			public String getDescription() {
				return "Test Container";
			}

			public IClasspathEntry[] getClasspathEntries() {
				if (TestExternalLibContainerInitializer.this.entries == null) {
					TestExternalLibContainerInitializer.this.entries = new IClasspathEntry[] {
							JavaCore.newLibraryEntry(containerPath, null, null)
						};
				}
				return TestExternalLibContainerInitializer.this.entries;
			}
		};
		JavaCore.setClasspathContainer(containerName, new IJavaProject[]{ project}, new IClasspathContainer[] { container }, null);
	}

}
