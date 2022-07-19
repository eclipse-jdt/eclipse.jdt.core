/*******************************************************************************
 *  Copyright (c) 2020 Simeon Andreev and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.indexer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.JavaRuntime;

import junit.framework.Test;

/**
 * Note: this is a manual test for reproducing race conditions with breakpoints,
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=566262#c3
 * <code>
1. Set breakpoints:
    1.1. DiskIndex [line: 624] - readAllDocumentNames() (at line after returning without doing anything)
    1.2. Index [line: 216] - save() (at line merging index)
    1.3. IndexManager [line: 829] - removeIndex(IPath) (at line removing index file)

2. Debug the test (see code above).

3. Observe concurrent suspend at Index.save() and IndexManager.removeIndex(IPath).
    3.1. Do step over for the IndexManager.removeIndex() suspend.
    3.2. Do resume for the Index.save() suspend.
    3.3. Do resume for the IndexManager.removeIndex() suspend.

4. Observe concurrent suspend at DiskIndex.readAllDocumentNames() and IndexManager.removeIndex(IPath).
    4.1. Do step over for the DiskIndex.readAllDocumentNames() suspend, until the line that reads DiskIndex.bufferEnd.
    4.2. Do step over for the IndexManager.removeIndex() suspend.
    4.3. Do step over for the DiskIndex.readAllDocumentNames() suspend, observe DisKIndex.bufferEnd is 2048.
    4.4. Do resume for the IndexManager.removeIndex() suspend.
    4.5. Do resume for the DiskIndex.readAllDocumentNames() suspend.

5. Observe another hit in Index.save(). Resume this.

6. Observe another hit in DiskIndex.readAllDocumentNames(), step through and observe DiskIndex.bufferEnd is -1, after being read from the stream.
   Resume.

7. Observe the logged exception:
    Exception in thread "Java indexing" java.lang.ArrayIndexOutOfBoundsException: Index 2048 out of bounds for length 2048
 * </code>
 *
 */
public class Bug566262Test extends AbstractJavaModelTests {

	public Bug566262Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(Bug566262Test.class);
	}

	/**
	 * Loops project creation and removal, to check if the JDT indexer will run into a {@link ArrayIndexOutOfBoundsException}.
	 */
	public void testBug566262() throws Exception {
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();

		startLogListening();
		try {
			for (int i = 0; i < 2; ++i) {
				String projectName = "Bug566262Test";
				IWorkspaceRunnable create = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						createTestProject(projectName);
					}
				};
				workspace.run(create, new NullProgressMonitor());
				IProject project = workspace.getRoot().getProject(projectName);

				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);

				Thread.sleep(500);
				project.delete(IResource.FORCE, new NullProgressMonitor());

				waitUntilIndexesReady();
			}
			assertLogEquals(""); // expect no logged errors
		} finally {
			stopLogListening();
		}
	}

	IProject createTestProject(String projectName) throws CoreException {
		String outputFolder = "bin";

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.create(description, new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		JavaProject javaProject = (JavaProject) JavaCore.create(project);

		IClasspathAttribute[] attributes = new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") };
		IClasspathEntry jrtEntry = JavaCore.newContainerEntry(JavaRuntime.newDefaultJREContainerPath().append(StandardVMType.ID_STANDARD_VM_TYPE).append("JavaSE-11"), null, attributes, true);
		IClasspathEntry sourcesEntry = JavaCore.newSourceEntry(project.getFile("src").getFullPath(), null, null, project.getFile(outputFolder).getFullPath());

		IClasspathEntry[] classpath = { jrtEntry, sourcesEntry };

		javaProject.writeFileEntries(classpath, project.getFullPath().append(outputFolder));
		return project;
	}
}