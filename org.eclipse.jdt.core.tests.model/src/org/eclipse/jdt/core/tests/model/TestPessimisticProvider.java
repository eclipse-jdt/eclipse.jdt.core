/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;

/**
 * Repository provider that can be configured to be pessimistic.
 */
public class TestPessimisticProvider extends RepositoryProvider implements IFileModificationValidator {
	private static TestPessimisticProvider soleInstance;
	
	public static final String NATURE_ID = "org.eclipse.jdt.core.tests.model.pessimisticnature";
	
	public static boolean markWritableOnEdit;
	public static boolean markWritableOnSave;

	public TestPessimisticProvider() {
		soleInstance = this;
	}
	
	public void configureProject() {
	}

	public String getID() {
		return NATURE_ID;
	}

	public void deconfigure() {
	}
	
	/*
	 * @see IRepositoryProvider#getFileModificationValidator()
	 */
	public IFileModificationValidator getFileModificationValidator() {
		return soleInstance;
	}
	
	public IStatus validateEdit(final IFile[] files, Object context) {
		if (markWritableOnEdit) {
			try {
				ResourcesPlugin.getWorkspace().run(
					new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor)	{
							for (int i = 0, length = files.length; i < length; i++) {
								files[i].setReadOnly(false);
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
		if (markWritableOnSave) {
			file.setReadOnly(false);
		}
		return Status.OK_STATUS;
	}
}
