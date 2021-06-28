/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *   IBM Corporation - modified to fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=185601
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import java.util.Objects;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Manage the generated source folder for an APT project.
 * Every AptProject has a GeneratedSourceFolderManager.  Depending on whether APT
 * is enabled for the project, there may or may not be an actual generated
 * source folder on disk; GeneratedSourceFolderManager is responsible for creating
 * and deleting this folder as needed whenever APT settings are changed.
 * <p>
 * The job of the GeneratedSourceFolderManager is to keep the following data
 * in agreement:
 * <ul>
 * <li>whether APT is enabled</li>
 * <li>the name of the generated source folder</li>
 * <li>the existence of the actual folder on disk</li>
 * <li>the presence of a classpath entry for the folder</li>
 * <li>problem markers indicating a disagreement in any of the above</li>
 * </ul>
 * We attempt to change the classpath entry and the folder on disk whenever
 * the enabled/disabled state or the folder name change.  These changes are
 * discovered via the preferenceChanged() method.
 * <p>
 * We attempt to update the classpath with an entry for the generated source
 * folder if APT is enabled and there is no entry for this folder present
 * on the classpath.
 * <p>
 * GeneratedSourceFolderManager is responsible only for the folder itself, not
 * its contents.  Contents are managed by @see GeneratedFileManager.
 *
 */
public class GeneratedSourceFolderManager {

	private final AptProject _aptProject;

	/**
	 * The folder where generated source files are placed.  This will be
	 * null if APT is disabled, or in any other error state (e.g., folder
	 * does not exist on disk; folder exists on disk but classpath entry
	 * does not exist).
	 * <p>
	 * In general, if we see that this member is null but the ENABLED
	 * preference is true, we will try to create the folder and add it to
	 * the classpath; if we see that this member is non-null but the
	 * ENABLED preference is false, we will try to delete this folder's
	 * contents and remove it from the classpath; and if we see that the
	 * ENABLED preference is true, but the GENSRCDIR folder name preference
	 * is different than the name of this folder, we will try to delete
	 * this folder's contents, remove it from the classpath, and create a
	 * new folder and add it to the classpath.  When we do this work depends
	 * on when we get notified of relevant changes and on what locks we are
	 * able to obtain.
	 */
	private volatile IFolder _generatedSourceFolder;

	private final boolean _isTestCode;
	
	/**
	 * Reflects whether apt is enabled as soon as it is enabled.
	 * Only write access this in the thread which receives preferenceChanged:
	 */
	private volatile boolean _aptEnabled;

	/**
	 * Should be constructed only by AptProject.  Other clients should call
	 * @see AptProject#getGeneratedSourceFolderManager(boolean) to get this object.
	 */
	public GeneratedSourceFolderManager(AptProject aptProject, boolean isTestCode)
	{
		_aptProject = aptProject;
		_isTestCode = isTestCode;
		final IJavaProject javaProject = aptProject.getJavaProject();

		// Set _generatedSourceFolder only if APT is enabled, the folder exists,
		// and the folder is on the classpath.
		// Otherwise leave it null, which will cause us to try to fix things later on.
		_aptEnabled = AptConfig.isEnabled(javaProject);
		if (_aptEnabled) {
			final IFolder folder = getFolder();
			if (folder.exists()) {
				if (isOnClasspath(folder)) {
					_generatedSourceFolder = folder;
				}
			}
		}
	}

	/**
	 * Add the folder to the classpath, unless it's already there.
	 * @param srcFolder the folder to add to the classpath.  Must not be null.
	 * @param specificOutputLocation
	 * @return true if, at the end of the routine, the folder is on the classpath.
	 */
	private boolean addToClasspath(IFolder srcFolder, IPath specificOutputLocation) {
		boolean onClasspath = false;
		try {
			ClasspathUtil.updateProjectClasspath( _aptProject.getJavaProject(), srcFolder, null, _isTestCode, specificOutputLocation );
			if(AptPlugin.DEBUG)
				AptPlugin.trace("Ensured classpath has an entry for " + srcFolder); //$NON-NLS-1$
			onClasspath = true;
		}
		catch (CoreException e) {
			e.printStackTrace();
			AptPlugin.log(e, "Failed to add classpath entry for generated source folder " + srcFolder.getName()); //$NON-NLS-1$
		}
		return onClasspath;
	}

	/**
	 * Call this to create the folder and add it to the classpath, when APT is enabled
	 * (in which case the folder did not previously exist) or when the folder name is
	 * changed (in which case the old stuff must also be removed).
	 * <p>
	 * This method will take a resource lock if the generated source folder needs
	 * to be created on disk, and it will take a java model lock if the project's
	 * source paths need to be updated.  Care should be taken when calling this
	 * method to ensure that locking behavior is correct.
	 * <p>
 	 * This should only be called on an event thread, with no locks on the project
	 * or classpath.
	 */
	private void configure() {
		IFolder sourceFolder = _generatedSourceFolder;
		IFolder srcFolder = getFolderPreference();
		boolean same = Objects.equals(sourceFolder, srcFolder);
		assert(sourceFolder == null || same): "Should have already removed old folder by now: " + sourceFolder; //$NON-NLS-1$
		if (srcFolder == null) {
			IStatus status = AptPlugin.createStatus(null, "Could not create generated source folder (" + //$NON-NLS-1$
					AptConfig.getGenSrcDir(_aptProject.getJavaProject()) + ")"); //$NON-NLS-1$
			AptPlugin.log(status);
			return;
		}

		ensureFolderExists(srcFolder);
	}

	public void ensureFolderExists(IFolder srcFolder) {
		IPath specificOutputLocation;
		if (_isTestCode) {
			IClasspathEntry[] cp;
			try {
				cp = _aptProject.getJavaProject().getRawClasspath();
			} catch (JavaModelException e) {
				return;
			}
			specificOutputLocation = ClasspathUtil.findTestOutputLocation(cp);
			if (specificOutputLocation == null) {
				// not test source folder present
				return;
			}
		} else {
			specificOutputLocation = null;
		}
		// Ensure that the new folder exists on disk.
		if (createOnDisk(srcFolder)) {
			// Add it to the classpath.
			if (addToClasspath(srcFolder, specificOutputLocation)) {
				// Only if we get this far do we actually set _generatedSourceFolder.
				synchronized ( this ) {
					_generatedSourceFolder = srcFolder;
				}
			}
		}
	}


	/**
	 * Creates the generated source folder if necessary.  This should be called just
	 * before doing a build.
	 *
	 * Classpath will be updated with an entry for the generated source folder
	 * if it is not already added to the classpath. See bug 185601.
	 */
	public void ensureFolderExists(){
		// If APT is disabled, do nothing.
		if (!_aptEnabled) {
			return;
		}

		// In principle we could bail out here, if (_generatedSourceFolder != null).
		// However, this method is an opportunity to detect and fix problems such
		// as the folder getting deleted without generatedSourceFolderDeleted()
		// getting called (e.g., without user having done a refresh).
		IFolder srcFolder = getFolder();
		if (srcFolder == null) {
			IStatus status = AptPlugin.createStatus(null, "Could not create generated source folder (" + //$NON-NLS-1$
					AptConfig.getGenSrcDir(_aptProject.getJavaProject()) + ")"); //$NON-NLS-1$
			AptPlugin.log(status);
			return;
		}
		ensureFolderExists(srcFolder);
	}

	/**
	 * Create a folder on disk, unless it already exists.
	 * <p>
	 * This method will frequently be called on multiple threads simultaneously
	 * (e.g., build thread and UI thread).
	 * @param srcFolder the folder to create.  Must not be null.
	 * @return true if, at the end of the routine, the folder exists on disk.
	 */
	private boolean createOnDisk(IFolder srcFolder) {
		boolean exists = false;
		try {
			// don't take any locks while creating the folder, since we are doing file-system operations
			srcFolder.refreshLocal( IResource.DEPTH_INFINITE, null );
			if (!srcFolder.exists()) {
				FileSystemUtil.makeDerivedParentFolders(srcFolder);
				if(AptPlugin.DEBUG)
					AptPlugin.trace("Created folder " + srcFolder + " on disk"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			exists = true;
		}
		catch (CoreException e) {
			e.printStackTrace();
			AptPlugin.log(e, "Failed to ensure existence of generated source folder " + srcFolder.getName()); //$NON-NLS-1$
		}
		return exists;
	}

	/**
	 * Call this method when the APT_ENABLED preference has changed.
	 *
	 * Configure the generated source folder according to whether APT is enabled
	 * or disabled.  If enabled, the folder will be created and a classpath entry
	 * will be added.  If disabled, the folder and classpath entry will be removed.
	 * <p>
	 * This should only be called on an event thread, with no locks on the project
	 * or classpath.
	 */
	public void enabledPreferenceChanged()
	{
		final boolean enable = AptConfig.isEnabled(_aptProject.getJavaProject());
		// Short-circuit if nothing changed.
		if (enable == _aptEnabled) {
			if( AptPlugin.DEBUG ) {
				AptPlugin.trace("enabledChanged() doing nothing; state is already " + enable); //$NON-NLS-1$
			}
			// no change in state
			return;
		}
		enabledPreferenceChangedTo(enable);
	}

	private void enabledPreferenceChangedTo(final boolean enable) {
		_aptEnabled = enable;

		if ( AptPlugin.DEBUG ) {
			AptPlugin.trace("enabledChanged() changing state to " + enable +  //$NON-NLS-1$
					" for " + _aptProject.getJavaProject().getElementName()); //$NON-NLS-1$
		}
		if( enable ) {
			configure();
		}
		else {
			removeFolder(false);
		}
	}

	/**
	 * Respond to a change in the name of the generated source folder.
	 * If APT is enabled, remove the old folder and classpath entry and
	 * create new ones.
	 * <p>
 	 * This should only be called on an event thread, with no locks on the project
	 * or classpath.
	 */
	public void folderNamePreferenceChanged()
	{
		// if APT is disabled, we don't need to do anything
		if (!_aptEnabled) {
			return;
		}

		// if name didn't change, we don't need to do anything
		if (_generatedSourceFolder != null && _generatedSourceFolder.equals(getFolderPreference())) {
			if( AptPlugin.DEBUG ) {
				AptPlugin.trace("folderNameChanged() doing nothing; name is already " +  //$NON-NLS-1$
						_generatedSourceFolder.getProjectRelativePath());
			}
			return;
		}

		removeFolder(true);
		configure();
	}

	/**
	 *  Invoked when the generated source folder has been deleted.  This will
	 *  flush any in-memory state tracking generated files, and cause the
	 *  generated source folder to be recreated the next time we build.
	 *
	 *  Note: this should only be called within a resource change event to ensure that the classpath
	 *  is correct during any build. Resource change event never occurs during a build.
	 */
	public void folderDeleted()
	{
		_aptProject.projectClean( false, !_isTestCode, _isTestCode);

		IFolder srcFolder;
		synchronized(this){
			srcFolder = _generatedSourceFolder;
			_generatedSourceFolder = null;
		}
		if(AptPlugin.DEBUG)
			AptPlugin.trace("set _generatedSourceFolder to null; was " + srcFolder ); //$NON-NLS-1$
	}

	/**
	 * This method will return the binary output location for the generated source folder.
	 * If the generated-source folder is not configured (i.e., not created or not added to
	 * the project's source path, then this method will return the default binary output
	 * location for the project.
	 *
	 * @return the IPath corresponding to the binary output location for the
	 * generated source folder. This is relative to the project.
	 *
	 * @throws JavaModelException
	 *
	 * @see #getFolder()
	 */
	public IPath getBinaryOutputLocation()
		 throws JavaModelException
	{
		IPath outputRootPath = null;
		IFolder generatedSourceFolder = getFolder();
		if ( generatedSourceFolder != null && generatedSourceFolder.exists() )
		{
			IClasspathEntry cpe = ClasspathUtil.findProjectSourcePath( _aptProject.getJavaProject(), generatedSourceFolder );
			if ( cpe != null )
				outputRootPath = cpe.getOutputLocation();
		}

		// no output root, so get project's default output location
		if ( outputRootPath == null )
			outputRootPath = _aptProject.getJavaProject().getOutputLocation();

		// output location is relative to the workspace, we want to make it relative to project
		int segments = outputRootPath.matchingFirstSegments( _aptProject.getJavaProject().getPath() );
		outputRootPath = outputRootPath.removeFirstSegments( segments );

		return outputRootPath;
	}

	/**
	 * Get the current generated source folder; or if it is null, return
	 * an IFolder corresponding to the current generated source folder name.
	 * This is a handle-only operation and does not have anything to do with
	 * whether the folder exists on disk.
	 * @throws IllegalArgumentException if the name is invalid (e.g., "..").
	 */
	public IFolder getFolder(){

		synchronized (this) {
			if( _generatedSourceFolder != null )
				return _generatedSourceFolder;
		}

		return getFolderPreference();
	}

	/**
	 * Get an IFolder that corresponds to the folder name preference.
	 * This has nothing to do with whether APT is enabled or disabled,
	 * nothing to do with whether the folder exists on disk; it's just
	 * a handle corresponding to a name.
	 * @return null if the IFolder could not be created, which probably
	 * means that the name is something illegal like "..".
	 */
	private IFolder getFolderPreference() {
		final String folderName = _isTestCode ? AptConfig.getGenTestSrcDir(_aptProject.getJavaProject()) : AptConfig.getGenSrcDir(_aptProject.getJavaProject());
		IFolder folder = null;
		try {
			folder = _aptProject.getJavaProject().getProject().getFolder( folderName );
		}
		catch (IllegalArgumentException e) {
			// In the event that the folderName is invalid, just return null.
		}
		return folder;
	}

	/**
	 * returns true if the specified folder is the source folder used where
	 * generated files are placed.
	 *
	 * @param folder - the folder to determine if it is the generated source folder
	 * @return true if it is the generated source folder, false otherwise.
	 *
	 * @see #getFolder()
	 */
	public boolean isGeneratedSourceFolder( IFolder folder )
	{
		return folder != null && folder.equals( getFolder() );
	}

	private boolean isOnClasspath(IFolder srcFolder) {
		boolean found = false;
		try {
			if (ClasspathUtil.doesClasspathContainEntry(
					_aptProject.getJavaProject(), null, srcFolder.getFullPath(), null)) {
				found = true;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return found;
	}

	/**
	 * Remove a folder from disk and from the classpath.
	 * @param srcFolder
	 */
	private void removeFolder(boolean waitForWorkspaceEvents) {
		final IFolder srcFolder = _generatedSourceFolder;
		if (srcFolder == null) {
			return;
		}

		// Clear out the generated file maps
		_aptProject.projectClean(false, !_isTestCode, _isTestCode);

		// clean up the classpath first so that when we actually delete the
		// generated source folder we won't cause a classpath error.
		try {
			if (srcFolder.isDerived()) {
				ClasspathUtil.removeFromProjectClasspath( _aptProject.getJavaProject(), srcFolder, null );
			}
		} catch (JavaModelException e) {
			AptPlugin.log( e, "Failed to remove classpath entry for old generated src folder " + srcFolder.getName() ); //$NON-NLS-1$
		}

		final IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
	        @Override
			public void run(IProgressMonitor monitor)
	        {
            	try {
            		IResource parent = srcFolder.getParent();
            		boolean deleted = FileSystemUtil.deleteDerivedResources(srcFolder);

            		// We also want to delete our parent folder(s) if they are derived and empty
            		if (deleted) {
            			while (parent.isDerived() && parent.getType() == IResource.FOLDER) {
            				IFolder parentFolder = (IFolder)parent;
            				if (parentFolder.members().length == 0) {
            					parent = parentFolder.getParent();
            					FileSystemUtil.deleteDerivedResources(parentFolder);
            				}
            				else {
            					break;
            				}
            			}
            		}

            	} catch(CoreException e) {
            		AptPlugin.log(e, "failed to delete old generated source folder " + srcFolder.getName() ); //$NON-NLS-1$
            	} catch(OperationCanceledException cancel) {
            		AptPlugin.log(cancel, "deletion of generated source folder got cancelled"); //$NON-NLS-1$
            	}
	        }
	    };
	    IWorkspace ws = ResourcesPlugin.getWorkspace();
	    try{
	    	ws.run(runnable, ws.getRoot(), IWorkspace.AVOID_UPDATE, null);
	    }catch(CoreException e){
			AptPlugin.log(e, "Runnable for deleting old generated source folder " + srcFolder.getName() + " failed."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	    if(waitForWorkspaceEvents) {
			try {
				Thread.sleep(50);
				// wait for workspace events *after* delete task is done
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
			} catch (OperationCanceledException | InterruptedException e) {
				// ignore
			}
		}
	    synchronized ( this ) {
	    	if(srcFolder.equals(_generatedSourceFolder)) {
				_generatedSourceFolder = null;
			}
	    }
	}

	/**
	 * Check whether the proposed name is permitted.
	 * @param folderName can be anything, including null.
	 * @return true if attempting to set the generated source folder to
	 * <code>dirString</code> is likely to succeed.
	 */
	public static boolean validate(final IJavaProject jproj, final String folderName) {
		boolean succeeded = false;
		try {
			if (jproj != null) {
				// If we have a specific project, we can just ask.
				IFolder folder = null;
				folder = jproj.getProject().getFolder( folderName );
				succeeded = (folder != null);
			}
			else {
				// We're being asked about the default, so no specific project;
				// here we have to guess.  The code that will later fail if we
				// get it wrong is IProject.getFolder(String).  So we use some
				// heuristics.
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IPath state = AptPlugin.getPlugin().getStateLocation();
				IPath proposed = new Path(folderName);
				IPath combined = state.append(proposed);
				if (combined.segmentCount() <= state.segmentCount()) {
					// proposed folder depth is too shallow
					return false;
				}
				IFolder folder = root.getFolder(combined);
				succeeded = (folder != null);
			}
		}
		catch (IllegalArgumentException e) {
			return false;
		}
		return succeeded;
	}

	public boolean isTestCode() {
		return _isTestCode;
	}
}
