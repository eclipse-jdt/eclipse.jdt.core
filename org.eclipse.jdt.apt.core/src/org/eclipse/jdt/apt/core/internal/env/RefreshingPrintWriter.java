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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.AptPlugin;

public class RefreshingPrintWriter extends PrintWriter {

	private final IPath _path;
	private final IProject _project;
	
	public RefreshingPrintWriter(final IPath path, final IProject project) throws FileNotFoundException {
		super(path.toFile());
		_path = path;
		_project = project;
	}
	
	public RefreshingPrintWriter(final IPath path, final IProject project, String charsetName) 
		throws FileNotFoundException, UnsupportedEncodingException 
	{
		super(path.toFile(), charsetName);
		_path = path;
		_project = project;
	}
	
	public void close() {
		super.close();
		try {
			_project.getFile(_path).refreshLocal(IResource.DEPTH_ZERO, null);
		}
		catch (CoreException ce) {
			AptPlugin.log(ce, "Could not close print writer for ifile: " + _path); //$NON-NLS-1$
		}
	}
}
