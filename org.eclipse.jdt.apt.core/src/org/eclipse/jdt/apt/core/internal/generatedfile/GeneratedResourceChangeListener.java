/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class GeneratedResourceChangeListener implements IResourceChangeListener 
{
	public GeneratedResourceChangeListener(){}
	
	public void resourceChanged(IResourceChangeEvent event) 
	{
		if ( event.getType() == IResourceChangeEvent.PRE_BUILD )
		{
			try
			{ 
				if( AptPlugin.DEBUG )
					AptPlugin.trace("[ thread= " + Thread.currentThread().getName() + " ] ---- got a pre-build event"); //$NON-NLS-1$ //$NON-NLS-2$
				final PreBuildVisitor visitor = new PreBuildVisitor();
				event.getDelta().accept( visitor );
			}
			catch ( CoreException ce )
			{
				AptPlugin.log(ce, "Error during resource change for " + event); //$NON-NLS-1$
				// TODO:  handle exception here.
			}
		}
		else if ( event.getType() == IResourceChangeEvent.PRE_CLOSE )
		{
			IProject p = (IProject)event.getResource();
			IJavaProject jp = JavaCore.create(p);
			GeneratedFileManager gfm = AptPlugin.getAptProject(jp).getGeneratedFileManager();
			gfm.projectClosed();
		}
		else if ( event.getType() == IResourceChangeEvent.PRE_DELETE )
		{
			// TODO:  need to update projectDeleted() to delete the generated_src folder
			// in an async thread.  The resource tree is locked here.
			IProject p = (IProject)event.getResource();
			IJavaProject jp = JavaCore.create(p);
			GeneratedFileManager gfm = AptPlugin.getAptProject(jp).getGeneratedFileManager();
			gfm.projectDeleted();
			AptPlugin.deleteAptProject(jp);
		}
	}

	public class PreBuildVisitor implements IResourceDeltaVisitor
	{
		public boolean visit(IResourceDelta delta) throws CoreException 
		{
			IResource r = delta.getResource();
			IProject project = r.getProject();		
			
			if ( project == null ) 
				return true;
			
			if( delta.getKind() == IResourceDelta.REMOVED ){
				IJavaProject jp = JavaCore.create(project);
				GeneratedFileManager gfm = AptPlugin.getAptProject(jp).getGeneratedFileManager();
				if( r instanceof IFile ){
					IFile f = (IFile)r;
					if ( gfm.isParentFile( f ) )
					{
						gfm.parentFileDeleted( (IFile) r, null /* progress monitor */ );
					}
					else if ( gfm.isGeneratedFile( f ) )
					{
						gfm.generatedFileDeleted( f, null /*progress monitor */ );
					}
				}
				else if( r instanceof IFolder ){			
					IFolder f = (IFolder) r;
					if ( gfm.isGeneratedSourceFolder( f ) ){
						gfm.generatedSourceFolderDeleted();
					}
				}
			}
			return true;
		}
	}
}
