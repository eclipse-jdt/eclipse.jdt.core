/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
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
 * discovered via the handlePreferenceChange() method. 
 * <p>
 * GeneratedSourceFolderManager is responsible only for the folder itself, not
 * its contents.  Contents are managed by @see GeneratedFileManager.
 *  
 */
public class GeneratedSourceFolderManager {
	
	private final AptProject _aptProject;

	/**
	 * The folder where generated source files are placed.  This will be null until
	 * the folder is actually created and the project's source path is updated to 
	 * include the folder.  It will also be null if there was an error creating the
	 * folder.
	 */
	private IFolder _generatedSourceFolder = null;
	
	/**
	 * The name of the generated source folder, relative to the project.  This
	 * will be identical to the value of the APT_GENSRCDIR preference, except when 
	 * the preference has changed and this object has not yet been informed.
	 */
	private String _generatedSourceFolderName = null;
	
	/**
	 * Should be constructed only by AptProject.  Other clients should call
	 * @see AptProject#getGeneratedSourceFolderManager() to get this object.
	 */
	public GeneratedSourceFolderManager(AptProject aptProject) 
	{
		_aptProject = aptProject;
		final IJavaProject javaProject = aptProject.getJavaProject();
		
		// get generated source dir from config 
		// default value is set in org.eclipse.jdt.apt.core.internal.util.AptCorePreferenceInitializer
		_generatedSourceFolderName = AptConfig.getString( javaProject, AptPreferenceConstants.APT_GENSRCDIR);
		// properly initialize the GeneratedFileManager if project path is up-to-date and the generated 
		// source folder is there.
		final IFolder folder = getFolder();
		if(folder.exists()){
			boolean uptodate = false;
			try{
				uptodate = ClasspathUtil.isProjectClassPathUpToDate(javaProject, null, folder.getFullPath(), null);
			}catch(JavaModelException e){
				e.printStackTrace();
			}
			if( uptodate )
				_generatedSourceFolder = folder;
		}	
	}
	
	/**
	 * Sets the name of the generated soruce folder.  The source folder will not be created 
	 * and will not be added to the project's source paths.  If there is an existing source
	 * folder, it will be deleted.
	 * To properly have the new generated source folder configured, call #ensureGeneratedSourceFolder(). 
	 * 
	 * @param newValue The string name of the new generated source folder.  This should be relative 
	 * to the project root.  Absolute paths are not supported.  The specified string should be 
	 * a valid folder name for the file system, and should not be an existing source folder for the 
	 * project.  
	 * 
	 * @see #getFolder()
	 * @see #getFolderName()
	 */
	private void configure( String newValue, String oldValue ) 
	{
		
		// bail if they specify null, empty-string or don't change the name of the source folder
		if ( newValue == null || 
			 newValue.length() == 0 || 
			 newValue.equals(oldValue) )
			return;
		
		_aptProject.projectClean( true );

		IFolder srcFolder = null;
		synchronized ( this )
		{
			_generatedSourceFolderName = newValue;
			srcFolder = _generatedSourceFolder;
		}
		
		// if the preference change occur before we actually
		// initialized the _generatedSourceFolder. 
		// This may happen when the pre-processor resource change event occurs after
		// the preference change event.
		if( oldValue != null && srcFolder == null ){
			srcFolder = _aptProject.getJavaProject().getProject().getFolder( oldValue );
		}
		
		resetGeneratedSrcFolder(srcFolder, true);		
	}
	
	/**
	 * Creates the generated source folder if it doesn't exist. 
	 * No changes to the classpath will be made.
	 */
	public void createGeneratedSourceFolder(){
		IFolder srcFolder = getFolder();
		// This most likely means the preference change event hasn't occured yet
		// and we don't know about the name of the generated source directory.
		if( srcFolder == null )
			return;
		try{
			srcFolder.refreshLocal( IResource.DEPTH_INFINITE, null );
			if (!srcFolder.exists()) {
				if( AptPlugin.DEBUG )
					AptPlugin.trace("creating " + srcFolder.getProjectRelativePath()); //$NON-NLS-1$
					
				FileSystemUtil.makeDerivedParentFolders(srcFolder);
			}
		}
		catch(CoreException ce){
			AptPlugin.log(ce, "Failure during refreshLocal on " + srcFolder.getProjectRelativePath()); //$NON-NLS-1$
		}
		synchronized (this) {
			_generatedSourceFolder = srcFolder;
		}
	}
	
	/**
	 *  Creates the generated source folder if it doesn't exist, and adds it as a source path
	 *  to the project.  To access the generated source folder, but not have it be created
	 *  or added as a source path, use getGeneratedSourceFolder().  Note that this method 
	 *  will take a resource lock if the generated source folder needs to be created on disk, 
	 *  and it will take a java model lock if the project's source paths need to be updated.
	 *  Care should be taken when calling this method to ensure that locking behavior is correct.    
	 *  
	 *  @return <code>true</code> iff the any resource or classpath has been modified. 
	 *  return <code>false</code> otherwise.
	 *  
	 *  @see #getFolder()
	 *  @see #isGeneratedSourceFolderConfigured()
	 */
	private boolean ensureGeneratedSourceFolder(){
		
		boolean reset = false;
		IFolder curSrcFolder = null;
	
		// Determine current state of affairs, with respect to
		// folder, folder name, and classpath.
		synchronized( this )
		{
			if( _generatedSourceFolder != null ){
				final IPath srcFolderPath = _generatedSourceFolder.getProjectRelativePath();
				
				if( !_generatedSourceFolderName.equals( srcFolderPath.toString()) ){
					// Folder name has been changed!  Save the current folder so we can clear it out later.
					reset = true;
					curSrcFolder = _generatedSourceFolder;
					_generatedSourceFolder = null;
				}
				else {
					// Folder name and folder are in sync.  Check that folder is on classpath.
					
					// If the folder doesn't exist on disk, there is no point examining the classpath.
					try{
						_generatedSourceFolder.refreshLocal( IResource.DEPTH_INFINITE, null );
					}
					catch(CoreException ce){
						AptPlugin.log(ce, "Failure during refreshLocal on " + srcFolderPath); //$NON-NLS-1$
					}
					if (!_generatedSourceFolder.exists()) {
						return false;
					}
					
					try {
						IJavaProject jp = _aptProject.getJavaProject();
						IClasspathEntry[] cp = jp.getRawClasspath();
						IPath path = _generatedSourceFolder.getFullPath();
						if (ClasspathUtil.isProjectClassPathUpToDate(jp, cp, path, null)) {
							return false;
						}
					}
					catch (JavaModelException jme) {
						AptPlugin.log(jme, "Failure examining the classpath"); //$NON-NLS-1$
					}
				}
			}
		}
		
		IFolder srcFolder = null;
		try{
			if( reset ){
				// Folder name was out of sync with folder.  Delete the old folder and classpath entry.
				ClasspathUtil.removeFromProjectClasspath(_aptProject.getJavaProject(), curSrcFolder, null );
				if ( curSrcFolder.exists() ){
					if( AptPlugin.DEBUG )
						AptPlugin.trace("deleting gen src dir " + curSrcFolder.getName() ); //$NON-NLS-1$
					curSrcFolder.delete( true, false, null );
				}
			}
				
			// don't take any locks while creating the folder, since we are doing file-system operations
			srcFolder = getFolder();
			srcFolder.refreshLocal( IResource.DEPTH_INFINITE, null );
			if (!srcFolder.exists()) {
				FileSystemUtil.makeDerivedParentFolders(srcFolder);
			}
				
			//
			// make sure __generated_src dir is on the cp if not already
			//
			ClasspathUtil.updateProjectClasspath( _aptProject.getJavaProject(), srcFolder, null );
			
			if(AptPlugin.DEBUG)
				AptPlugin.trace("Added directory " + srcFolder.getName() + " and updated classpath" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(CoreException e){						
			e.printStackTrace();
			AptPlugin.log(e, "Failed to create generated source directory"); //$NON-NLS-1$
			return false;
		}
		
		synchronized ( this )
		{
			_generatedSourceFolder = srcFolder;
			return true;
		}
	}
	
	
	/**
	 *  Invoked when the generated source folder has been deleted.  This will 
	 *  flush any in-memory state tracking generated files and clean up the project classpath.
	 *  
	 *  Note: this should only be called within a resource change event to ensure that the classpath
	 *  is correct during any build. Resource change event never occurs during a build.
	 */
	public void generatedSourceFolderDeleted()
	{
		_aptProject.projectClean( false );
		
		IFolder srcFolder;
		synchronized(this){
			srcFolder = getFolder();
			_generatedSourceFolder = null;
		}
		if(AptPlugin.DEBUG)
			AptPlugin.trace("nulled out gen src dir " + srcFolder.getName() ); //$NON-NLS-1$
	}

	/**
	 * @return get the generated source folder. May return null if
	 * creation has failed, the folder has been deleted or has not been created.
	 */
	public IFolder getFolder(){
		
		final String folderName;
		synchronized (this) {
			if( _generatedSourceFolder != null )
				return _generatedSourceFolder;
			folderName = getFolderName();
		}		
		if(folderName == null)
			return null;
		
		return _aptProject.getJavaProject().getProject().getFolder( folderName );
	}
	
	/**
	 * returns the name of the folder for generated source files.  The name is relative
	 * to the project root.
	 * 
	 * @see #getFolder()
	 * @see #isGeneratedSourceFolderConfigured()
	 */
	public synchronized String getFolderName() 
	{ 
		return _generatedSourceFolderName; 
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
	 * @see #isGeneratedSourceFolderConfigured()	
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
	 * Configure the generated source folder according to whether APT is enabled
	 * or disabled.  If enabled, the folder will be created and a classpath entry
	 * will be added.  If disabled, the folder and classpath entry will be removed.
	 * <p>
	 * This should only be called on an event thread, with no locks on the project
	 * or classpath.
	 * @param enable
	 */
	public void setEnabled(boolean enable)
	{
		if( AptPlugin.DEBUG ){
			if( enable )
				AptPlugin.trace("enabling APT for " + _aptProject.getJavaProject().getElementName()); //$NON-NLS-1$
			else
				AptPlugin.trace("disabling APT for " + _aptProject.getJavaProject().getElementName()); //$NON-NLS-1$
		}
		if( enable ) {
			final String folderName = _generatedSourceFolderName;
			if( AptPlugin.DEBUG ){
				AptPlugin.trace("configure generated source folder to be " + folderName ); //$NON-NLS-1$
			}
			configure(folderName, null);
		} else{
			final IFolder srcFolder = getFolder();
			_aptProject.projectClean(true);
			resetGeneratedSrcFolder(srcFolder, false);
		}
	}
	
	/**
	 * Configure the name of the generated source folder.  If APT is enabled,
	 * remove the old folder and classpath entry and create new ones.  If
	 * disabled, simply record the new name.
	 * <p>
 	 * This should only be called on an event thread, with no locks on the project
	 * or classpath.
	 * TODO: why does this need to know the old name?  Didn't we get it in the constructor?
	 * @param oldName can be null if the old value is not known.
	 * @param newName
	 */
	public void changeFolderName(String oldName, String newName)
	{
		if (newName == null) {
			// Null is used to indicate this preference has
			// been removed, as the project has been deleted.
			// We do nothing
			return;
		}
		if (newName.equals(oldName)) {
			// No-op -- same config
			return;
		}
		
		final boolean aptEnabled = AptConfig.isEnabled(_aptProject.getJavaProject());
		if( AptPlugin.DEBUG )
			AptPlugin.trace("configure generated source directory new value = " +  //$NON-NLS-1$
					newName + 
					" old value = "  + oldName + //$NON-NLS-1$
					" APT is enabled = " + aptEnabled); //$NON-NLS-1$
		if( aptEnabled )
			// If APT is enabled, 
			// clean up the old cp entry, delete the old folder, 
			// create the new one and update the classpath.
			configure( newName, oldName );
		else
			// If APT is not enabled, the folder should not exist
			// and there should be no entry on the classpath.
			synchronized (this) {
				_generatedSourceFolderName = newName;
			}
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
	
	/**
	 * Cleanup the classpath and schedule a job to delete the generated source folder.
	 * @param recreate if <code>true</code> configure the generated source directory.
	 */
	private void resetGeneratedSrcFolder(final IFolder srcFolder, boolean recreate){
		// clean up the classpath first so that when we actually delete the 
		// generated source folder and won't cause a classpath error.
		if( srcFolder != null ){
			try{	
				ClasspathUtil.removeFromProjectClasspath( _aptProject.getJavaProject(), srcFolder, null );		
			}catch(JavaModelException e){
				AptPlugin.log( e, "Error occurred deleting old generated src folder " + srcFolder.getName() ); //$NON-NLS-1$
			}
		}
		
		if( recreate )
			ensureGeneratedSourceFolder();
		
		// delete the generated source folder as well as
		// all of its derived ancestors that are containers only to the 
		// generated source folder
		if( srcFolder != null ){
			IFolder folderToDelete = srcFolder;		
			for( IContainer c = srcFolder.getParent(); 
			 	 c != null && (c instanceof IFolder); 
			 	 c = c.getParent() ){
				
				try{
					// members can't be empty, there has to be at least 1.
					// will only delete the parent if it contains only the 
					// folder that we want to delete.
					if( c.isDerived() && c.members().length == 1 ){
						folderToDelete = (IFolder)c;
					}
					else
						break;
				}catch(CoreException e){
					AptPlugin.log(e, "failure while accessing member of " + c.getName() ); //$NON-NLS-1$
					break;
				}
			}
			removeFolder(folderToDelete);
		}
	}

	/**
	 * Remove the specified folder from disk.
	 * @param srcFolder
	 */
	private void removeFolder(final IFolder srcFolder) {
		if( srcFolder != null ){
			final IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
	            public void run(IProgressMonitor monitor)
	            {		
	            	if( srcFolder != null ){
		            	try{
		            		srcFolder.delete(true, false, null);
		            	}catch(CoreException e){
		            		AptPlugin.log(e, "failed to delete old generated source folder " + srcFolder.getName() ); //$NON-NLS-1$
		            	}catch(OperationCanceledException cancel){
		            		AptPlugin.log(cancel, "deletion of generated source folder got cancelled"); //$NON-NLS-1$
		            	}
	            	}
	            };
	        };
	        IWorkspace ws = _aptProject.getJavaProject().getProject().getWorkspace();
	        try{
	        	ws.run(runnable, ws.getRoot(), IWorkspace.AVOID_UPDATE, null);
	        }catch(CoreException e){
	    		AptPlugin.log(e, "Runnable for deleting old generated source folder " + srcFolder.getName() + " failed."); //$NON-NLS-1$ //$NON-NLS-2$
	    	}
		}
	}
	
}
