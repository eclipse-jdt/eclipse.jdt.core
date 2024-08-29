/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.FileModificationValidationContext;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.team.core.RepositoryProvider;

/**
 * Repository provider that can be configured to be pessimistic.
 */
public class TestPessimisticProvider extends RepositoryProvider {

	public static final String NATURE_ID = "org.eclipse.jdt.core.tests.model.pessimisticnature";

	public static boolean markWritableOnEdit;
	public static boolean markWritableOnSave;

	public void configureProject() {
	}

	public String getID() {
		return NATURE_ID;
	}

	public void deconfigure() {
	}

	public FileModificationValidator getFileModificationValidator2() {
		return new FileModificationValidator() {
			public IStatus validateEdit(final IFile[] files, FileModificationValidationContext context) {
				if (TestPessimisticProvider.markWritableOnEdit) {
					try {
						ResourcesPlugin.getWorkspace().run(
							new IWorkspaceRunnable() {
								public void run(IProgressMonitor monitor)	{
									for (int i = 0, length = files.length; i < length; i++) {
										try {
											setReadOnly(files[i], false);
										} catch (CoreException e) {
											e.printStackTrace();
										}
									}
								}
							},
							null);
					} catch (CoreException e) {
						e.printStackTrace();
						return e.getStatus();
					}
				}
				return Status.OK_STATUS;
			}

			public IStatus validateSave(IFile file) {
				if (TestPessimisticProvider.markWritableOnSave) {
					try {
						setReadOnly(file, false);
					} catch (CoreException e) {
						e.printStackTrace();
						return e.getStatus();
					}
				}
				return Status.OK_STATUS;
			}

			void setReadOnly(IResource resource, boolean readOnly) throws CoreException {
				if (Util.isReadOnlySupported()) {
					ResourceAttributes resourceAttributes = resource.getResourceAttributes();
					if (resourceAttributes != null) {
						resourceAttributes.setReadOnly(readOnly);
						resource.setResourceAttributes(resourceAttributes);
					}
				}
			}
		};
	}

}
