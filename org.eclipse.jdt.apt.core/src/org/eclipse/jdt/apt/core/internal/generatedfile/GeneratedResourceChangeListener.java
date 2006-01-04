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

import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * A jdt.core pre-process resource change listener that manages generated resources.
 */
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
					AptPlugin.trace("---- generated resource change listener got a pre-build event"); //$NON-NLS-1$ //$NON-NLS-2$
				final PreBuildVisitor visitor = new PreBuildVisitor();
				event.getDelta().accept( visitor );
				addGeneratedSrcFolderTo(visitor.getProjectsThatNeedGenSrcFolder());
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
			AptPlugin.getAptProject(jp).projectClosed();
		}
		else if ( event.getType() == IResourceChangeEvent.PRE_DELETE )
		{
			// TODO:  need to update projectDeleted() to delete the generated_src folder
			// in an async thread.  The resource tree is locked here.
			IProject p = (IProject)event.getResource();
			IJavaProject jp = JavaCore.create(p);
			AptPlugin.getAptProject(jp).projectDeleted();
			AptPlugin.deleteAptProject(jp);
		}
	}
	
private void addGeneratedSrcFolderTo(final Set<IProject> projs ){
		
		for(IProject proj : projs ){
			final IJavaProject javaProj = JavaCore.create(proj);
			if(AptConfig.isEnabled(javaProj)){
				final GeneratedSourceFolderManager gsfm = AptPlugin.getAptProject(javaProj).getGeneratedSourceFolderManager();
				gsfm.createGeneratedSourceFolder();
			}	
		}

	}

	private class PreBuildVisitor implements IResourceDeltaVisitor
	{
		// projects that we need to add the generated source folder to.
		private final Set<IProject> _addGenFolderTo = new HashSet<IProject>();
		// any projects that is closed or about to be deleted
		private final Set<IProject> _removedProjects = new HashSet<IProject>();
		public boolean visit(IResourceDelta delta) throws CoreException 
		{
			IResource r = delta.getResource();
			IProject project = r.getProject();
			
			if ( project == null ) 
				return true;
			
			if( delta.getKind() == IResourceDelta.REMOVED ){
				final IJavaProject javaProj = JavaCore.create(project);
				final AptProject aptProj = AptPlugin.getAptProject(javaProj);
				if( r instanceof IFile ){
					final GeneratedFileManager gfm = aptProj.getGeneratedFileManager();
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
					final GeneratedSourceFolderManager gsfm = aptProj.getGeneratedSourceFolderManager();
					IFolder f = (IFolder) r;					
					if ( gsfm.isGeneratedSourceFolder( f ) ){
						gsfm.generatedSourceFolderDeleted();
						// all deletion occurs before any add (adding the generated source directory)
						if( !_removedProjects.contains(project) ){
							_addGenFolderTo.add(project);
						}
						// if the project is already closed or in the process of being
						// deleted, will ignore this deletion since we cannot correct 
						// the classpath anyways.
					}
				}
				else if( r instanceof IProject ){	
					_removedProjects.add((IProject)r);
				}
			}
			else if( r instanceof IProject ){
				final IProject proj = (IProject)delta.getResource();		
				if( canUpdate(proj) ){
					_addGenFolderTo.add(proj);
				}				
				else
					_removedProjects.add(proj);
			}

			return true;
		}	
		
		Set<IProject> getProjectsThatNeedGenSrcFolder(){
			_addGenFolderTo.removeAll(_removedProjects);
			return _addGenFolderTo;
		}
		
		private boolean canUpdate(IProject proj)
			throws CoreException
		{
			return proj.isOpen() && proj.exists() && proj.hasNature(JavaCore.NATURE_ID);
		}
	}
}
