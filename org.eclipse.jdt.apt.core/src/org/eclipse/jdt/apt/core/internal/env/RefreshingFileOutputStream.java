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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class RefreshingFileOutputStream extends FileOutputStream {

	private final IPath _path;
	private final IProject _project;
	
	public RefreshingFileOutputStream(final IPath path, final IProject project) throws FileNotFoundException {
		super(project.getLocation().append(path).toFile());
		_path = path;
		_project = project;
	}
	
	public void close() throws IOException {
		super.close();
		new FileRefreshJob(_project.getFile(_path)).schedule();
	}

}
