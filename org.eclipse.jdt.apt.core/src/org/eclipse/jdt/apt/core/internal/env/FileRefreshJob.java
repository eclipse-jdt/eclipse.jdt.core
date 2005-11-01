/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class FileRefreshJob extends Job {
	
	private final IFile _file;
	
	FileRefreshJob(final IFile file) {
		super(file.toString());
		_file = file;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			_file.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		catch (CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}

}
