/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import java.util.Collections;
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
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * A jdt.core pre-process resource change listener that manages generated resources.
 * <p>
 *
 * Note that this is both a pre-build listener and a post-change listener,
 * because there is a bug in the resource change event notification in the platform:
 * sometimes they fail to send out deletion notifications for files in pre-build,
 * but they do send them out in post-change.
 */
public class GeneratedResourceChangeListener implements IResourceChangeListener
{
	// Synchronized collection, as post-change notifications could come in
	// simultaneously. Note that pre-build will not though, as it holds the
	// workspace lock
	private final Set<IResource> deletedResources =
		Collections.synchronizedSet(new HashSet<IResource>());

	public GeneratedResourceChangeListener(){}

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		if ( event.getType() == IResourceChangeEvent.PRE_CLOSE )
		{
			IProject p = (IProject)event.getResource();
			if( AptPlugin.DEBUG_GFM )
				AptPlugin.trace(
						"generated resource change listener got a pre-close event: project = " + p.getName()); //$NON-NLS-1$
			IJavaProject jp = JavaCore.create(p);
			AptPlugin.getAptProject(jp).projectClosed();
		}
		else if ( event.getType() == IResourceChangeEvent.PRE_DELETE )
		{
			// TODO:  need to update projectDeleted() to delete the generated_src folder
			// in an async thread.  The resource tree is locked here.
			IProject p = (IProject)event.getResource();
			if( AptPlugin.DEBUG_GFM )
				AptPlugin.trace(
						"generated resource change listener got a pre-delete event: project = " + p.getName()); //$NON-NLS-1$
			IJavaProject jp = JavaCore.create(p);
			AptPlugin.getAptProject(jp).projectDeleted();
			AptPlugin.deleteAptProject(jp);
		}
		else if ( event.getType() == IResourceChangeEvent.PRE_BUILD )
		{
			try
			{
				if( AptPlugin.DEBUG_GFM )
					AptPlugin.trace("generated resource change listener got a pre-build event"); //$NON-NLS-1$

				final PreBuildVisitor pbv = new PreBuildVisitor();

				// First we need to handle previously deleted resources (from the post-change event),
				// because we could not perform file i/o during that event
				for (IResource resource : deletedResources) {
					pbv.handleDeletion(resource);
				}

				event.getDelta().accept( pbv );
				addGeneratedSrcFolderTo(pbv.getProjectsThatNeedGenSrcFolder(), false);
				addGeneratedSrcFolderTo(pbv.getProjectsThatNeedGenTestSrcFolder(), true);

				// Now clear the set of deleted resources,
				// as we don't want to re-handle them
				deletedResources.clear();
			}
			catch ( CoreException ce )
			{
				AptPlugin.log(ce, "Error during pre-build resource change"); //$NON-NLS-1$
			}
		}
		else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			if( AptPlugin.DEBUG_GFM )
				AptPlugin.trace(
						"generated resource change listener got a post-change event"); //$NON-NLS-1$
			PostChangeVisitor pcv = new PostChangeVisitor();
			try {
				event.getDelta().accept(pcv);
			}
			catch (CoreException ce) {
				AptPlugin.log(ce, "Error during post-change resource event"); //$NON-NLS-1$
			}
		}
	}

	private void addGeneratedSrcFolderTo(final Set<IProject> projs, boolean isTestCode){

		for(IProject proj : projs ){
			final IJavaProject javaProj = JavaCore.create(proj);
			if(javaProj.getProject().isOpen() && AptConfig.isEnabled(javaProj)){
				final GeneratedSourceFolderManager gsfm = AptPlugin.getAptProject(javaProj).getGeneratedSourceFolderManager(isTestCode);
				gsfm.ensureFolderExists();
			}
		}

	}

	/**
	 * We need a post-change visitor, as there is a bug in the platform for
	 * resource change notification -- some items will be reported *only* in the post-change event,
	 * so we keep track of them here and handle them in the pre-build
	 */
	private class PostChangeVisitor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			if( delta.getKind() == IResourceDelta.REMOVED ){
				if (AptPlugin.DEBUG_GFM) {
					AptPlugin.trace("generated resource post-change listener adding to deletedResources:" +  //$NON-NLS-1$
							delta.getResource().getName());
				}
				deletedResources.add(delta.getResource());
			}

			return true;
		}

	}

	private class PreBuildVisitor implements IResourceDeltaVisitor
	{
		// projects that we need to add the generated source folder to.
		private final Set<IProject> _addGenFolderTo = new HashSet<>();
		// projects that we need to add the generated test source folder to.
		private final Set<IProject> _addGenTestFolderTo = new HashSet<>();
		// any projects that is closed or about to be deleted
		private final Set<IProject> _removedProjects = new HashSet<>();
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException
		{
			IResource r = delta.getResource();
			IProject project = r.getProject();

			if ( project == null )
				return true;

			if( delta.getKind() == IResourceDelta.REMOVED ){
				if (!deletedResources.contains(r)) {
					handleDeletion(r);
				}
			}
			else if( r instanceof IProject ){
				final IProject proj = (IProject)delta.getResource();
				if( canUpdate(proj) ){
					_addGenFolderTo.add(proj);
					_addGenTestFolderTo.add(proj);
				}
				else
					_removedProjects.add(proj);
			}

			return true;
		}

		private void handleDeletion(IResource resource) throws CoreException {
			if (AptPlugin.DEBUG_GFM) {
				AptPlugin.trace("handleDeletion: resource = " + resource.getName()); //$NON-NLS-1$
			}
			IProject project = resource.getProject();
			final IJavaProject javaProj = JavaCore.create(project);
			final AptProject aptProj = AptPlugin.getAptProject(javaProj);
			if( resource instanceof IFile ){
				final GeneratedFileManager gfm = aptProj.getGeneratedFileManager(false);
				IFile f = (IFile)resource;
				gfm.fileDeleted(f);
				aptProj.getGeneratedFileManager(true).fileDeleted(f);
			}
			else if( resource instanceof IFolder ){
				IFolder f = (IFolder) resource;
				final GeneratedSourceFolderManager gsfm = aptProj.getGeneratedSourceFolderManager(false);
				if ( gsfm.isGeneratedSourceFolder( f ) ){
					gsfm.folderDeleted();
					// all deletion occurs before any add (adding the generated source directory)
					if( !_removedProjects.contains(project) ){
						_addGenFolderTo.add(project);
					}
					// if the project is already closed or in the process of being
					// deleted, will ignore this deletion since we cannot correct
					// the classpath anyways.
				}
				final GeneratedSourceFolderManager testgsfm = aptProj.getGeneratedSourceFolderManager(true);
				if ( testgsfm.isGeneratedSourceFolder( f ) ){
					testgsfm.folderDeleted();
					// all deletion occurs before any add (adding the generated source directory)
					if( !_removedProjects.contains(project) ){
						_addGenTestFolderTo.add(project);
					}
					// if the project is already closed or in the process of being
					// deleted, will ignore this deletion since we cannot correct
					// the classpath anyways.
				}
			}
			else if( resource instanceof IProject ){
				_removedProjects.add((IProject)resource);
			}
		}

		Set<IProject> getProjectsThatNeedGenSrcFolder(){
			_addGenFolderTo.removeAll(_removedProjects);
			return _addGenFolderTo;
		}

		Set<IProject> getProjectsThatNeedGenTestSrcFolder(){
			_addGenTestFolderTo.removeAll(_removedProjects);
			return _addGenTestFolderTo;
		}

		private boolean canUpdate(IProject proj)
			throws CoreException
		{
			return proj.isOpen() && proj.exists() && proj.hasNature(JavaCore.NATURE_ID);
		}
	}
}
