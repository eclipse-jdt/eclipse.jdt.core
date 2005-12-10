/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal.generatedfile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.Messages;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;

/**
 * This class is used for managing generated files.  
 *   
 * There are four maps that are used.  Two are used to track the relationships 
 * between parent files & generated files ( 
 * <code>_parentFile2GeneratedFiles</code> & <code>_generatedFile2ParentFiles</code>). 
 * The other two maps are used to track cached working copies:  
 * <code>_generatedFile2WorkingCopy</code>  maps a generated file to its 
 * working copy, and  <code>_generatedWorkingCopy2OpenParentFiles</code>  
 * maps a working copy to any parent files that may be open.   
 * 
 * The file maps have entries added when a file is generated during a build.  
 * The file maps & working-copy maps haven entries added added when a file
 * is added during a reconcile.  There are various entry-points to keep the
 * maps up-to-date with respect to life-cycle events on the parent & generated files.
 * (e.g., parentFileDeleted(), ).
 *
 * SYNCHRONIZATION NOTES (IMPORTANT)
 * ---------------------------------
 * Synchronization around the GeneratedFileManager's maps uses the GeneratedFileManager
 * instance's monitor.   When acquiring this monitor, DO NOT PERFORM ANY OPERATIONS
 * THAT TAKE ANY OTHER LOCKS (e.g., java model operations, or file system operations like
 * creating or deleting a file or folder).  If you do this, then the code is subject to 
 * deadlock situations.  For example, a resource-changed listener may take a resource lock
 * and then call into the GeneratedFileManager for clean-up, where your code could reverse
 * the order in which the locks are taken.  This is bad, so be careful.   
 * 
 */
public class GeneratedFileManager {

	// disable type generation during reconcile. This can cause deadlock.
	// See radar bug #238684	
	public static final boolean GENERATE_TYPE_DURING_RECONCILE = false;
	
	/**
	 * map from IFile of parent file to Set <IFile>of generated files
	 */
	private Map<IFile, Set<IFile>> _parentFile2GeneratedFiles = new HashMap();

	/**
	 * map from IFile of generated file to Set <IFile>of parent files
	 */
	private Map<IFile, Set<IFile>> _generatedFile2ParentFiles = new HashMap();
	
	/**
	 * Map from a the working copy of a generated file to its *open* parents.  Note that
	 * the set of parent files are only those parent files that have an open editor.
	 * This set should be a subset for a correpsonding entry in the _generatedFile2Parents map.
	 */
	private Map<ICompilationUnit, Set<IFile>> _generatedWorkingCopy2OpenParentFiles = new HashMap();
	
	/**
	 * Map from type name to the working copy in memory of that type name
	 * 
	 * Map<String, ICompilationUnit>
	 */
	private Map<IFile, ICompilationUnit> _generatedFile2WorkingCopy = new HashMap();	

	/**
	 * The folder where generated source files are placed.  This will be null until
	 * the folder is actually created and the project's source path is updated to 
	 * include the folder. 
	 */
	private IFolder _generatedSourceFolder;
	
	private String _generatedSourceFolderName;
	
	private final AptProject _aptProject;
	
	// This is set when the build starts, and accessed during type generation. 
	private IPackageFragmentRoot _generatedPackageFragmentRoot;
	// This is initialized/reset when the build starts, and accessed during type generation.
	// It has the same life-cycle as _generatedPackageFragmentRoot.
	// This bit may be set to <code>true</code> during the first type generation to prevent any 
	// future type generation due to configuration problem.
	private boolean _skipTypeGeneration = false;
	// The name of the generated source folder when the _generatedPackageFragmenRoot is 
	// initialized. Used for problem reporting.
	private String _snapshotFolderName = null;
	
	/**
	 * Construction can only take place from within 
	 * the factory method, getGeneratedFileManager().
	 */
	public GeneratedFileManager(final AptProject aptProject) {
		_aptProject = aptProject;
		final IJavaProject javaProject = aptProject.getJavaProject();
		
		// register a preference listener so that we can watch for changes 
		// to the gen src dir at the project scope...
		IScopeContext projScope = new ProjectScope(aptProject.getJavaProject().getProject());
		IEclipsePreferences projPrefs = projScope.getNode(AptPlugin.PLUGIN_ID);
		IPreferenceChangeListener projListener = new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				
				final String newValue = (String)event.getNewValue();
				if (newValue == null) {
					// Null is used to indicate this preference has
					// been removed, as the project has been deleted.
					// We do nothing
					return;
				}
				final String oldValue = (String)event.getOldValue();
				if (newValue.equals(oldValue)) {
					// No-op -- same config
					return;
				}
				
				if (AptPreferenceConstants.APT_GENSRCDIR.equals(event.getKey())) {
					final boolean aptEnabled = AptConfig.isEnabled(_aptProject.getJavaProject());
					if( AptPlugin.DEBUG )
						AptPlugin.trace("configure generated source directory new value = " +  //$NON-NLS-1$
								newValue + 
								" old value = "  + oldValue + //$NON-NLS-1$
								" APT is enabled = " + aptEnabled); //$NON-NLS-1$
					// If APT is enabled, 
					// clean up the old cp entry, delete the old folder, 
					// create the new one and update the classpath.
					if( aptEnabled )
						configureGeneratedSourceFolder( newValue, oldValue );
					else
						setGeneratedSourceFolderName(newValue);
				}
				else if(AptPreferenceConstants.APT_ENABLED.equals(event.getKey()) ){
					if( AptPlugin.DEBUG ){
						AptPlugin.trace("Got preference change event for " + AptPreferenceConstants.APT_ENABLED ); //$NON-NLS-1$
					}
					
					// no-op;
					if(newValue.equals(oldValue)){
						return;
					}
					
					final boolean isEnabling = Boolean.parseBoolean(newValue);
					if( AptPlugin.DEBUG ){
						if( isEnabling )
							AptPlugin.trace("enabling APT for " + _aptProject.getJavaProject().getElementName()); //$NON-NLS-1$
						else
							AptPlugin.trace("diabling APT " + _aptProject.getJavaProject().getElementName()); //$NON-NLS-1$
					}
					if( isEnabling )
						configureGeneratedSourceFolder();
					else{
						final IFolder srcFolder = getGeneratedSourceFolder();
						projectClean(true);
						resetGeneratedSrcFolder(srcFolder, false);
					}
				}
			}
		};
		projPrefs.addPreferenceChangeListener(projListener);
		
		// ...and at the workspace scope.
		// Note we check all projects, even those that have project-specific
		// settings, when the workspace setting changes.  For projects with
		// project-specific settings, the value of the setting won't change 
		// so the request will be ignored.
		IScopeContext wkspScope = new InstanceScope();
		IEclipsePreferences wkspPrefs = wkspScope.getNode(AptPlugin.PLUGIN_ID);
		IPreferenceChangeListener wkspListener = new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				if (AptPreferenceConstants.APT_GENSRCDIR.equals(event.getKey())) {
					configureGeneratedSourceFolder( AptConfig.getGenSrcDir(javaProject), null );
				}
			}
		};
		wkspPrefs.addPreferenceChangeListener(wkspListener);
		
		// get generated source dir from config 
		// default value is set in org.eclipse.jdt.apt.core.internal.util.AptCorePreferenceInitializer
		_generatedSourceFolderName = AptConfig.getString( javaProject, AptPreferenceConstants.APT_GENSRCDIR);
		// properly initialize the GeneratedFileManager if project path is up-to-date and the generated 
		// source folder is there.
		final IFolder folder = javaProject.getProject().getFolder(_generatedSourceFolderName);
		if(folder.exists()){
			boolean uptodate = false;
			try{
				uptodate = isProjectClassPathUpToDate(javaProject, null, folder.getFullPath(), null);
			}catch(JavaModelException e){
				e.printStackTrace();
			}
			if( uptodate )
				_generatedSourceFolder = folder;
		}	
	}

	static
	{
		// register element-changed listener to clean up working copies
		int mask = ElementChangedEvent.POST_CHANGE;
		JavaCore.addElementChangedListener( new WorkingCopyCleanupListener(), mask );
	}
	
	/**
	 * Invoked when a file is generated during a build.  The generated file and intermediate 
	 * directories will be created if they don't  exist.  This method takes file-system locks, 
	 * and assumes that the calling method has at some point acquired a workspace-level 
	 * resource lock.
	 * 
	 * @param parentFile the parent of the type being generated
	 * @param typeName the dot-separated java type name of the type being generated
	 * @param contents the java code contents of the new type .
	 * @param progressMonitor a progres monitor.  This may be null.
	 * @param charsetName the character set to use when creating the new file.  This can be null 
	 * or the empty string, in which case the platform default encoding will be used.
	 *  
	 * @return - the newly created IFile along with whether it was modified
	 * 
	 * @throws CoreException
	 * @throws UnsupportedEncodingException
	 */
	public FileGenerationResult generateFileDuringBuild(
			IFile parentFile,
			String typeName, 
			String contents, 
			ProcessorEnvImpl env,
			IProgressMonitor progressMonitor)
	throws CoreException
	{
		if( _skipTypeGeneration ) return null;
		else if( _generatedPackageFragmentRoot == null ){			
			String message = Messages.bind(
					Messages.GeneratedFileManager_missing_classpath_entry, 
					new String[] {_snapshotFolderName});
			IMarker marker = _aptProject.getJavaProject().getProject().createMarker(AptPlugin.APT_CONFIG_PROBLEM_MARKER);
			marker.setAttributes(
					new String[] {
						IMarker.MESSAGE, 
						IMarker.SEVERITY
					},
					new Object[] {
						message,
						IMarker.SEVERITY_ERROR
					}
				);
			// disable any future type generation
			_skipTypeGeneration = true;
			return null;
		}
		
		try{
			
			if( typeName.indexOf('/') != -1 )
				typeName = typeName.replace('/', '.');
			int separatorIndex = typeName.lastIndexOf('.');			
			final String typeSimpleName;
			final String pkgName;
			if( separatorIndex == -1 ){
				pkgName = ""; //$NON-NLS-1$
				typeSimpleName = typeName;
			}
			else{
				pkgName = typeName.substring(0, separatorIndex);
				typeSimpleName = typeName.substring(separatorIndex + 1, typeName.length());
			}
			
			// NOTE: Do NOT ever create any type of resource (files, folders) through the
			// resource API. The resource change event will not go out until the build
			// is completed. Instead always go through the JavaModel. -theodora
			IFolder genSrcFolder = (IFolder)_generatedPackageFragmentRoot.getResource();
			final Set<IContainer> newFolders = getNewPackageFolders(pkgName, genSrcFolder);
			IPackageFragment pkgFrag = _generatedPackageFragmentRoot.createPackageFragment(pkgName, true, progressMonitor);
			if( pkgFrag == null ){
				final Exception e = new IllegalStateException("failed to locate package '" + pkgName + "'");  //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
				throw e;
			}			
			// mark all newly create folders as derived.			
			markNewFoldersAsDerived((IContainer)pkgFrag.getResource(), newFolders);
			
			final String cuName = typeSimpleName + ".java"; //$NON-NLS-1$
			
			ICompilationUnit unit = pkgFrag.getCompilationUnit(cuName);
			IFile file = (IFile)unit.getResource();
			boolean contentsDiffer = true;

			if (unit.exists()) {
				InputStream oldData = null;
				InputStream is = null;
				try {
					is = new ByteArrayInputStream( contents.getBytes() );
					oldData = new BufferedInputStream( ((IFile)unit.getResource()).getContents());
					contentsDiffer = !FileSystemUtil.compareStreams(oldData, is);
				}
				catch (CoreException ce) {
					// Do nothing. Assume the new content is different
				}
				finally {
					is.reset();
					if (oldData != null) {
						try {
							oldData.close();
						} 
						catch (IOException ioe) 
						{}
					}
				}
			}	
			
			if( contentsDiffer ){
				if( unit.exists() && unit.isOpen() ){
					// directly modify the content of the working copy
					// so that UI will pick up the change.
					IBuffer buffer = unit.getBuffer();
					if (buffer == null){
						throw new IllegalStateException("Unable to update unit for " + cuName); //$NON-NLS-1$
						
					}
					buffer.setContents(contents.toCharArray());
					buffer.save(progressMonitor, true);
				}
				else{
					ICompilationUnit newUnit = null;
					newUnit = pkgFrag.createCompilationUnit(cuName, contents, true,
							progressMonitor);
					if( newUnit == null ) {				
						throw new IllegalStateException("Unable to create unit for " + cuName); //$NON-NLS-1$
					}
					if( AptPlugin.DEBUG )
						AptPlugin.trace("generated " + typeName ); //$NON-NLS-1$
					newUnit.save(progressMonitor, true);
				}
			}
			
			// Mark the file as derived. Note that certain user actions may have
			// deleted this file before we get here, so if the file doesn't exist,
			// marking it derived throws a ResourceException
			if (file.exists()) {
				file.setDerived(true);
			}
			// We used to also make the file read-only. This is a bad idea,
			// as refactorings then fail in the future, which is worse
			// than allowing a user to modify a generated file.
			
			// during a batch build
			if( parentFile != null ){
				addEntryToFileMaps( parentFile, file );
			}
			return new FileGenerationResult(file, contentsDiffer);
		}
		catch(Throwable e){
			AptPlugin.log(e, "(2)failed to generate type " + typeName); //$NON-NLS-1$
			e.printStackTrace();
		}
		return null; // something failed. The catch block have already logged the error.
	}	
	
		
	/**
	 * This function generates a type "in-memory" by creating or updating a working copy with the
	 * specified contents.   The generated-source folder must be configured correctly for this to 
	 * work. This method takes no locks, so it is safe to call when holding fine-grained resource 
	 * locks (e.g., during some reconcile paths).  Since this only works on an in-memory working 
	 * copy of the type, the IFile for the generated type may not exist on disk.  Likewise, the
	 * corresponding package directories of type-name may not exist on disk.   
	 * 
	 * TODO:  figure out how to create a working copy with a client-specified character set
	 * 
	 * @param parentCompilationUnit - the parent compilation unit. 
	 * @param typeName - the dot-separated java type name for the new type
	 * @param contents - the contents of the new type
	 * @param workingCopyOwner - the working copy owner.  This may be null.  If null, parentCompilationUnit.getOwner() 
	 * will be used.  
	 * @param problemRequestor - this may be null. 
	 * @param progressMonitor - this may be null
	 * 
	 * @return The FileGenerationResult.  This will return null if the generated source folder
	 * is not configured.
	 *
	 */
	public  FileGenerationResult generateFileDuringReconcile(
			ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor ) 
	
	throws CoreException
	{	
		
		if (!GENERATE_TYPE_DURING_RECONCILE)
			return null;
		// We have disabled Reconcile-time type generated for a long time and
		// everything else has changed ever since. Don't expect the following 
		// code to work when we enable reconcile-time type generation again. 
		// -theodora
		// Work item
		// 1) make sure generated source directory and classpath is setup properly
		//    (i don't think it is today) -theodora
		
		ICompilationUnit workingCopy = null;
		FileGenerationResult result = null;
		IFile parentFile = (IFile)parentCompilationUnit.getResource();
		try 
		{
			//
			// get working copy (either from cache or create a new one)
			//
			workingCopy = getCachedWorkingCopy( parentFile, typeName );
			
			if ( workingCopyOwner == null )
				workingCopyOwner = parentCompilationUnit.getOwner();
			
			if ( workingCopy == null )
			{
				// create a new working copy
				workingCopy = createNewWorkingCopy(  
						parentFile,  typeName, contents,  
						workingCopyOwner, problemRequestor,  progressMonitor);
								
				workingCopy.reconcile(AST.JLS3, true, workingCopyOwner,
						progressMonitor);
				
				// TODO:  pass in correct flag for source-patch changed.  This is probably not going to matter.  Per 103183, we will either 
				// disable reconcile-time generation, or do it without any modifications, so we shouldn't have to worry about this.   
				result = new FileGenerationResult((IFile)workingCopy.getResource(), true);
			}
			else
			{

				//
				//  Update working copy's buffer with the contents of the type 
				// 
				boolean modified = updateWorkingCopy( contents, workingCopy, workingCopyOwner, progressMonitor );
				result = new FileGenerationResult((IFile)workingCopy.getResource(), modified);
			}
			
			return result;
		} 
		catch (JavaModelException jme) 
		{
			AptPlugin.log(jme, "Could not generate file for type: " + typeName); //$NON-NLS-1$
		} 
		return new FileGenerationResult((IFile)workingCopy.getResource(), true);
	}

	
	/**
	 *  returns true if the specified file is a generated file (i.e., it has one or more parent files)
	 *  
	 *  @param f the file in question
	 *  @return true
	 */
	public synchronized boolean isGeneratedFile( IFile f )
	{
		Set<IFile> s = _generatedFile2ParentFiles.get( f ); 
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	
	/**
	 *  returns true if the specified file is a parent  file (i.e., it has one or more generated files)  
	 *  
	 *  @param f - the file in question
	 *  @return true if the file is a parent, false otherwise
	 *  
	 *  @see #getGeneratedFilesForParent(IFile)
	 *  @see #isGeneratedFile(IFile)
	 */
	public synchronized boolean isParentFile( IFile f )
	{
		Set<IFile> s = _parentFile2GeneratedFiles.get( f );
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	

	/**
	 * returns true if the specified folder is the source folder used where
	 * generated files are placed. 
	 * 
	 * @param folder - the folder to determine if it is the generated source folder
	 * @return true if it is the generated source folder, false otherwise.  
	 * 
	 * @see #getGeneratedSourceFolder()
	 */
	public boolean isGeneratedSourceFolder( IFolder folder )
	{
		// use getGeneratedSourceFolder() here.  Bad things can happen if we try to 
		// create the generated source folder when this is invoked from a resource 
		// change listener
		if ( folder != null && folder.equals( getGeneratedSourceFolder() ) )
			return true;
		else
			return false;
	}
	
	
	/**
	 * @param parent - the parent file that you want to get generated files for
	 * @return Set of IFile instances that are the files known to be generated
	 * by this parent
	 * 
	 * @see #isParentFile(IFile)
	 * @see #isGeneratedFile(IFile)
	 */
	public synchronized Set<IFile> getGeneratedFilesForParent( IFile parent )
	{
		Set<IFile> s = _parentFile2GeneratedFiles.get( parent ); 
		if (s == null )
			s = Collections.emptySet();
		else
			// make a copy of the set to avoid any race conditions
			s = new HashSet<IFile>( s );
		return s;
	}
	
	

	/**
	 * 	Invoked whenever we potentially need to discard a generated working copy. 
	 *  Note that the generated working copy may not necessarily be discarded.  It 
	 *  will only be discarded if specified parent file is the only open parent file
	 *  for the specified Generated file.  If there are other parent open parent files, 
	 *  then the working copy for the generated file will remain open, but the link between
	 *  the generated file's working copy and its open parent file will be discarded. 
	 *  
	 * @param generatedFile - the generated file that we potentially want to discard
	 * @param parentFile - the parent file for the generated file
	 * @throws JavaModelException 
	 */
	public void discardGeneratedWorkingCopy( IFile generatedFile, IFile parentFile )
		throws JavaModelException
	{
		removeFromWorkingCopyMaps( generatedFile, parentFile );
	}

	/**
	 *  Invoked whenever a parent working copy has been discarded.
	 *  
	 *  @param parentFile.  The parent file whose working copy has been discarded
	 *  @throws JavaModelException if there is a problem discarding any working copies 
	 *  generated by the parent.
	 */
	public void parentWorkingCopyDiscarded( IFile parentFile )
		throws JavaModelException
	{
		Set<IFile> generatedFiles;
		synchronized( this )
		{
			generatedFiles = _parentFile2GeneratedFiles.get( parentFile );
			if ( generatedFiles == null || generatedFiles.size() == 0 )
				return;
		
			// make a copy to prevent race conditions
			generatedFiles = new HashSet<IFile>( generatedFiles );
		}
		
		for ( IFile generatedFile : generatedFiles )
			discardGeneratedWorkingCopy( generatedFile, parentFile );
	}
	
	/**
	 *  Invoked whenever a parent file has been deleted
	 */
	public void parentFileDeleted( IFile parent, IProgressMonitor monitor ) 
		throws CoreException
	{
		Set<IFile> generatedFiles;
		
		synchronized( this )
		{
			generatedFiles = _parentFile2GeneratedFiles.get( parent );
			// make a copy to avoid race conditions
			generatedFiles = new HashSet<IFile>( generatedFiles );
		}
			
		for ( IFile generatedFile : generatedFiles )
			deleteGeneratedFile( generatedFile, parent, monitor );
	}

	/**
	 *  Invoked whenever we need to delete a generated file (e.g., the parent file has been deleted,
	 *  or a parent stops generating a specific child).  Note that the generated file will only 
	 *  be deleted if the specified parent file is the only parent of the specified generated file. 
	 *  If there are other parents, then the generated file will not be deleted, but the link associating
	 *  the parent and the generated file will be removed (i.e., the the generated file will no longer consider
	 *  the parent file a "parent").
	 *  
	 */
	public boolean deleteGeneratedFile(IFile generatedFile, IFile parentFile, IProgressMonitor progressMonitor )
		throws CoreException
	{
		removeFromFileMaps( generatedFile, parentFile );
		
		boolean delete = false;

		synchronized ( this )
		{
			Set<IFile> parents = _generatedFile2ParentFiles.get( generatedFile );
		
			// this can be empty, but it shouldn't be null here unless parentFile was never a parent of generatedFile
			if ( parents == null ) throw new RuntimeException("unexpected null value for parents set for file " + generatedFile); //$NON-NLS-1$
		
			if (parents == null || parents.size() == 0) 
				delete = true;
		}
		
		if ( delete ){
			final IFolder genFolder = getGeneratedSourceFolder();
			assert genFolder != null : "Generated folder == null"; //$NON-NLS-1$
			IContainer parent = generatedFile.getParent();
			generatedFile.delete(true, true, progressMonitor);
			// not deleting the generated source folder and only 
			// delete generated folders containing the generated file.
			while( !genFolder.equals(parent) && parent != null && parent.isDerived() ){				
				final IResource[] members = parent.members();
				IContainer grandParent = parent.getParent();
				// last one turns the light off.
				if( members == null || members.length == 0 )
					parent.delete(true, progressMonitor);
				else
					break;
				parent = grandParent;
			}
		}
		
		return delete;
	}
	

	/**
	 *  Invoked whenever a previously-generated file is removed during reconcile.  We put an empty buffer in the contents 
	 *  of the working copy.  This effectively makes the type go away from the in-memory type system.  A subsequent
	 *  build is necessary to actually remove the file from disk, and to actually remove references in the
	 *  the generated file manager's state. 
	 *  
	 * @param generatedFile - the generated file whose working-copy buffer we want to be the empty string. 
	 * @param parentWorkingCopy - the parent working copy. 
	 * @param progressMonitor - a progress monitor
	 * 
	 * @return return true if the working-copy's buffer is set to the empty-string, false otherwise. 
	 * 
	 * @throws JavaModelException
	 */
	public boolean deleteGeneratedTypeInMemory(IFile generatedFile, ICompilationUnit parentWorkingCopy, IProgressMonitor progressMonitor )
		throws JavaModelException, CoreException
	{		
		if( !GENERATE_TYPE_DURING_RECONCILE )
			return false;
		// see if this is the only parent for this generated file
		boolean remove = false;
		IFile parentFile = (IFile) parentWorkingCopy.getResource();
		ICompilationUnit workingCopy = null;
		synchronized ( this )
		{
			// see if this generated file has any other parent files.  
			Set<IFile> parentFiles = _generatedFile2ParentFiles.get( generatedFile );

			assert( parentFiles != null && parentFiles.contains( parentFile ) ) : "Unexpected state in GeneratedFileManager"; //$NON-NLS-1$
			
			if ( parentFiles.size() == 1 && parentFiles.contains( parentFile ) )
			{
				workingCopy = _generatedFile2WorkingCopy.get( generatedFile );
				remove = true;
			}
			else
				remove = false;
		}

		if ( remove )
		{
			// we don't need to remove entries from any maps.  That will happen after 
			// the user saves & builds. 
			
			if ( workingCopy != null )
			{
				updateWorkingCopy( "", workingCopy, workingCopy.getOwner(), progressMonitor ); //$NON-NLS-1$
				return true;
			}
			else
			{
				// we don't have a cached working copy, so call generateWorkingCopyDuringReconcile and create an empty-stringed working copy
				// for the type that was generated during build.
				String typeName =  getTypeNameForDerivedFile( generatedFile );
				WorkingCopyOwner workingCopyOwner = parentWorkingCopy.getOwner();
				generateFileDuringReconcile( parentWorkingCopy, typeName, "", workingCopyOwner, null, progressMonitor ); //$NON-NLS-1$
			}
		}
		
		return remove;
	}
	
	/**
	 * Invoked whenever a generated file has been deleted.  This method will
	 * clean up any in-memory state about the previously generated file. 
	 * 
	 * @param generatedFile - the generated file that has been deleted
	 * @param progressMonitor - progress monitor.  this can be null. 
	 *
	 * @throws JavaModelException if there is an exception when discarding an open working copy for the generated file
	 */
	public void generatedFileDeleted( IFile generatedFile,  IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		Set<IFile> parentFiles;
		synchronized( this )
		{
			parentFiles = _generatedFile2ParentFiles.get( generatedFile );
			if ( parentFiles == null || parentFiles.isEmpty() )
				return;
			
			// make a copy to prevent race conditions
			parentFiles = new HashSet<IFile>( parentFiles );
		}
		
		for ( IFile parentFile : parentFiles )
		{
			removeFromWorkingCopyMaps( generatedFile, parentFile );
			removeFromFileMaps( generatedFile, parentFile );
		}
	}

	
	/**
	 * given file f, return the typename corresponding to the file.  This assumes
	 * that derived files use java naming rules (i.e., type "a.b.c" will be file 
	 * "a/b/c.java".
	 */
	private String getTypeNameForDerivedFile( IFile f )
	{
		IPath p = f.getFullPath();

		IProject project = f.getProject();
		IFolder folder = project.getFolder( getGeneratedSourceFolderName() );
		IPath generatedSourcePath = folder.getFullPath();
		
		int count = p.matchingFirstSegments( generatedSourcePath );	
		p = p.removeFirstSegments( count );
	
		String s = p.toPortableString();
		int idx = s.lastIndexOf( '.' );
		s = p.toPortableString().replace( '/', '.' );
		return s.substring( 0, idx );
	}
	
	/**
	 * Given a typename a.b.c, this will return the IFile for the 
	 * type name, where the IFile is in the GENERATED_SOURCE_FOLDER_NAME.
	 */
	private IFile getIFileForTypeName( String typeName )
	{
		// split the type name into its parts
		String[] parts = typeName.split( "\\."); //$NON-NLS-1$
		
		IFolder folder = getGeneratedSourceFolder();
		for ( int i = 0; i < parts.length - 1; i++ )
			folder = folder.getFolder( parts[i] );
		
		// the last part of the type name is the file name
		String fileName = parts[parts.length - 1] + ".java"; //$NON-NLS-1$		
		IFile file = folder.getFile( fileName );
		return file;
	}
	
	private void markNewFoldersAsDerived(IContainer folder, Set<IContainer> newFolders)
		throws CoreException
	{
		while(folder != null){
			if( newFolders.contains(folder) ){
				folder.setDerived(true);
			}
			folder = folder.getParent();
		}
	}
	
	private Set<IContainer> getNewPackageFolders(String pkgName, IFolder parent )
	{
		StringBuilder buffer = new StringBuilder();
		Set<IContainer> newFolders = new HashSet<IContainer>();
	    for( int i=0, len=pkgName.length(); i<len; i++ ){
	    	final char c = pkgName.charAt(i);
	    	if( c != '.')
	    		buffer.append(c);
	    	// create a folder when we see a dot or when we are at the end.
	    	if( c == '.' || i == len - 1){
	    		if( buffer.length() > 0 ){
	    			final IFolder folder = parent.getFolder(buffer.toString());
	    			if( !folder.exists()){
	    				newFolders.add(folder);
	    			}
	    			parent = folder;
	    			// reset the buffer
	    			buffer.setLength(0);
	    		}
	    	}
	    }
	    return newFolders;
	}
	
	/**
	 * Create all the folders corresponding to specified package name
	 * and mark all newly created ones as derived.
	 * @param pkgName dot-separated package name
	 * @param parent the parent folder of the folder to be created
	 * @throws CoreException when the folder creation fails.
	 */
	private void createFoldersForPackage(String pkgName, IFolder parent)
		throws CoreException
	{
	    StringBuilder buffer = new StringBuilder();
	    for( int i=0, len=pkgName.length(); i<len; i++ ){
	    	final char c = pkgName.charAt(i);
	    	if( c != '.')
	    		buffer.append(c);
	    	// create a folder when we see a dot or when we are at the end.
	    	if( c == '.' || i == len - 1){
	    		if( buffer.length() > 0 ){
	    			final IFolder folder = parent.getFolder(buffer.toString());
	    			if( !folder.exists()){
	    				folder.create(true, true, null);
	    				folder.setDerived(true);
	    			}
	    			parent = folder;
	    			// reset the buffer
	    			buffer.setLength(0);
	    		}
	    	}
	    }
	}
	
	/**
	 * Called at the start of build in order to cache our package fragment root
	 */
	public void compilationStarted() {

		try{
			// clear out any generated source folder config markers
			IMarker[] markers = _aptProject.getJavaProject().getProject().findMarkers(AptPlugin.APT_CONFIG_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( markers != null ){
				for( IMarker marker : markers )
					marker.delete();
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete configuration marker."); //$NON-NLS-1$
		}
		_skipTypeGeneration = false;
		createGeneratedSourceFolder();
		final IFolder genFolder;		
		synchronized(this){
			genFolder = getGeneratedSourceFolder();
			_snapshotFolderName = _generatedSourceFolderName;
		}
		try {
			_generatedPackageFragmentRoot = null;
			IPackageFragmentRoot[] roots = _aptProject.getJavaProject().getAllPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				final IResource resource = root.getResource();
				if( resource != null && resource.equals(genFolder)){
					_generatedPackageFragmentRoot = root;
					return;
				}
			}
		}
		catch (JavaModelException jme) {
			AptPlugin.log(jme, "Failure during start of compilation attempting to create generated source folder"); //$NON-NLS-1$
		}
		
	}
	
	/**
	 * Creates the generated source folder if it doesn't exist. 
	 * No changes to the classpath will be made.
	 */
	public void createGeneratedSourceFolder(){
		IFolder srcFolder = getGeneratedSourceFolder();
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
	 *  <em>
	 *  The only time that it is save to call this method is either we are explicitly fixing the 
	 *  classpath during a <code>ICompilationParitcipant.BROKEN_CLASSPATH_BUILD_FAILURE_EVENT</code> 
	 *  or during a resource change event. Since resource change event only occur before or after a build 
	 *  but never during one, the classpath will be updated at the correct time.
	 *  </em> 
	 *  
	 *  
	 *  @param progressMonitor the progress monitor.  This can be null. 
	 *  @return <code>true</code> iff the any resource or classpath has been modified. 
	 *  return <code>false</code> otherwise.
	 *  
	 *  @see #getGeneratedSourceFolder()
	 *  @see #isGeneratedSourceFolderConfigured()
	 */
	private boolean ensureGeneratedSourceFolder(){
		
		boolean reset = false;
		IFolder curSrcFolder = null;
	
		synchronized( this )
		{
			if( _generatedSourceFolder != null ){
				final IPath srcFolderPath = _generatedSourceFolder.getProjectRelativePath();
				
				if( !_generatedSourceFolderName.equals( srcFolderPath.toString()) ){
					reset = true;
					curSrcFolder = _generatedSourceFolder;
					_generatedSourceFolder = null;
				}
				else {					
					try{
						_generatedSourceFolder.refreshLocal( IResource.DEPTH_INFINITE, null );
					}
					catch(CoreException ce){
						AptPlugin.log(ce, "Failure during refreshLocal on " + srcFolderPath); //$NON-NLS-1$

					}
					
					// if the folder doesn't exists, there is no point examining the classpath
					boolean isConsistent = false;
					if (_generatedSourceFolder.exists()) {
						try {
							IJavaProject jp = _aptProject.getJavaProject();
							IClasspathEntry[] cp = jp.getRawClasspath();
							IPath path = _generatedSourceFolder.getFullPath();
							isConsistent = isProjectClassPathUpToDate(jp, cp, path, null);
						}
						catch (JavaModelException jme) {
							AptPlugin.log(jme, "Failure examining the classpath"); //$NON-NLS-1$
						}
					}
					
					if(isConsistent)
						return false;
				}
			}
	
		}
		IFolder srcFolder = null;
		try{
			if( reset ){
				// the generated source folder and the generated source folder name is not
				// lining up.
				removeFromProjectClasspath(_aptProject.getJavaProject(), curSrcFolder, null );
				if ( curSrcFolder.exists() ){
					if( AptPlugin.DEBUG )
						AptPlugin.trace("deleting gen src dir " + curSrcFolder.getName() ); //$NON-NLS-1$
					curSrcFolder.delete( true, false, null );
				}
			}
				
			// don't take any locks while creating the folder, since we are doing file-system operations
			srcFolder = getGeneratedSourceFolder();
			srcFolder.refreshLocal( IResource.DEPTH_INFINITE, null );
			if (!srcFolder.exists()) {
				FileSystemUtil.makeDerivedParentFolders(srcFolder);
			}
				
			//
			// make sure __generated_src dir is on the cp if not already
			//
			updateProjectClasspath( _aptProject.getJavaProject(), srcFolder, null );
			
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
	 * @see #getGeneratedSourceFolder()
	 * @see #isGeneratedSourceFolderConfigured()	
	 */
	public IPath getGeneratedSourceFolderOutputLocation()
		 throws JavaModelException 
	{
		IPath outputRootPath = null;
		IFolder generatedSourceFolder = getGeneratedSourceFolder();
		if ( generatedSourceFolder != null && generatedSourceFolder.exists() )
		{
			IClasspathEntry cpe = findProjectSourcePath( _aptProject.getJavaProject(), generatedSourceFolder );
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
	
	//
	//  check cache to see if we already have a working copy
	//
	private ICompilationUnit getCachedWorkingCopy( IFile parentFile, String typeName )
	{
		IFile derivedFile = getIFileForTypeName( typeName );
		ICompilationUnit workingCopy= null;
		
		synchronized( this )
		{
			workingCopy = _generatedFile2WorkingCopy.get( derivedFile );
		}
		
		if ( workingCopy != null )
			addEntryToWorkingCopyMaps( parentFile, workingCopy );

		return workingCopy;
	}
	
	private ICompilationUnit createNewWorkingCopy(IFile parentFile, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor)
		throws JavaModelException
	{
		IFolder folder = getGeneratedSourceFolder();
		
		// 
		//  figure out package part of type & file name
		//
		String pkgName;
		String fname;
		int idx = typeName.lastIndexOf( '.' );
		if ( idx > 0 )
		{
		    pkgName = typeName.substring( 0, idx );
		    fname = 
				typeName.substring(idx + 1, typeName.length()) + ".java"; //$NON-NLS-1$
		}
		else
		{
			pkgName = ""; //$NON-NLS-1$
			fname = typeName + ".java"; //$NON-NLS-1$
		}

		//
		//  create compilation unit
		//
		IPackageFragmentRoot root = _aptProject.getJavaProject().getPackageFragmentRoot(folder);
		IPackageFragment pkgFragment = 	root.getPackageFragment(pkgName );
		ICompilationUnit cu = pkgFragment.getCompilationUnit( fname );

		//
		// BecomeWorkingCopyOperation shouldn't take any resource locks to run, so we should be thread-safe here
		//
		cu.becomeWorkingCopy(problemRequestor, progressMonitor);
		ICompilationUnit workingCopy = cu;
		
		//
		//  update working copy
		//
		updateWorkingCopy( contents, workingCopy, workingCopyOwner, progressMonitor );

		
		//
		// update maps
		//
		addEntryToWorkingCopyMaps( parentFile, workingCopy );
		

		return workingCopy;	
	}
	
	/**
	 * Returns true if the file was modified
	 */
	private static boolean updateWorkingCopy( 
			String contents, ICompilationUnit workingCopy, 
			WorkingCopyOwner workingCopyOwner, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		IBuffer b = workingCopy.getBuffer();
		char[] oldBuf = b.getCharacters();
		// Diff the contents, and only set if they differ
		if (oldBuf.length == contents.length()) {
			boolean contentsMatch = true;
			for (int i=0; i<oldBuf.length; i++) {
				if (oldBuf[i] != contents.charAt(i)) {
					contentsMatch = false;
					break;
				}
			}
			if (contentsMatch) {
				// No change, no need to update buffer
				return false;
			}
		}
		
		b.setContents(contents);
		workingCopy.reconcile(AST.JLS3, true, workingCopyOwner,
				progressMonitor);
		return true;
	}
	
	private void addEntryToWorkingCopyMaps( IFile parentFile, ICompilationUnit workingCopy )
	{
		IFile generatedFile = (IFile) workingCopy.getResource();
		addEntryToFileMaps( parentFile, generatedFile );

		synchronized( this )
		{
			ICompilationUnit cu = _generatedFile2WorkingCopy.get( generatedFile );
			Set<IFile> parents = _generatedWorkingCopy2OpenParentFiles.get( workingCopy);
		
			if ( cu != null )
			{
				//assert( cu.equals( workingCopy ) ) : "unexpected different instances of working copy for the same type";
				if ( !cu.equals(workingCopy) ) throw new RuntimeException( "unexpected different instances of working copy for the same type" ); //$NON-NLS-1$
				if ( parents == null || parents.size() < 1 ) throw new RuntimeException( "Unexpected size of open-parents set.  Expected size >= 0"); //$NON-NLS-1$
			}
			else
			{
				_generatedFile2WorkingCopy.put( generatedFile, workingCopy );
			}
		
			if ( parents == null )
			{
				parents = new HashSet<IFile>();
				_generatedWorkingCopy2OpenParentFiles.put( workingCopy, parents );
			}
			parents.add( parentFile );
		}
	}
	
	private void addEntryToFileMaps( IFile parentFile, IFile generatedFile )
	{
		synchronized ( this )
		{
			// add parent file -> set of derived files
			Set<IFile> fileSet = _parentFile2GeneratedFiles.get( parentFile );
			if ( fileSet == null )
			{
				fileSet = new HashSet();
				_parentFile2GeneratedFiles.put( parentFile, fileSet );
			}
			fileSet.add( generatedFile );

			// add derived file -> set of parent files
			fileSet = _generatedFile2ParentFiles.get( generatedFile );
			if ( fileSet == null )
			{ 
				fileSet = new HashSet();
				_generatedFile2ParentFiles.put( generatedFile, fileSet );
			}
			fileSet.add( parentFile );
		}
	}
	
	private void removeFromFileMaps( IFile generatedFile, IFile parentFile ) 
	    throws JavaModelException 
	{
		boolean discardWorkingCopy;
		synchronized( this )
		{	
			discardWorkingCopy = _generatedFile2WorkingCopy.containsKey(generatedFile);
		}
		
		// don't want to hold a lock when we call discardGeneratedWorkingCopy...
		if ( discardWorkingCopy )
			discardGeneratedWorkingCopy(generatedFile, parentFile);

		synchronized( this )
		{
			Set<IFile> derivedFiles = _parentFile2GeneratedFiles.get(parentFile);

			// assertions
			if (derivedFiles == null)
				throw new RuntimeException(
					"derivedFiles is null and it shouldn't be"); //$NON-NLS-1$

			derivedFiles.remove(generatedFile);
		
			// update _derivedFile2Parents map
			Set<IFile> parents = _generatedFile2ParentFiles.get(generatedFile);

			// assertions
			if (parents == null)
				throw new RuntimeException(" parents is null and it shouldn't be"); //$NON-NLS-1$
			if (!parents.contains(parentFile))
				throw new RuntimeException("parents set does not contain parent. Parent: " + parentFile + ". Child: " + generatedFile); //$NON-NLS-1$ //$NON-NLS-2$

			parents.remove(parentFile);
		}
	}

	private void removeFromWorkingCopyMaps( IFile derivedFile, IFile parentFile )
		throws JavaModelException
	{
		ICompilationUnit workingCopy = null;
		boolean discard = false;
		
		synchronized( this )
		{
			workingCopy = _generatedFile2WorkingCopy.get( derivedFile );
			if ( workingCopy == null )
				return;
	
			Set<IFile> parents = _generatedWorkingCopy2OpenParentFiles.get( workingCopy );

			// TODO:  change these to assertions
			if ( parents == null ) throw new RuntimeException( "parents == null and it shouldnt"); //$NON-NLS-1$
			if ( ! parents.contains( parentFile )) throw new RuntimeException("parents set should contain parentCompilationUnit"); //$NON-NLS-1$
		
			// remove entry from parents _derivedWorkingCopy2OpenParentFiles
			parents.remove( parentFile );
	
			// and remove entry from _derivedFile2WorkingCopy
			if ( parents.size() == 0 )
			{
				_generatedFile2WorkingCopy.remove( derivedFile );
				discard = true;
			}
		}
	
		if ( discard )
			workingCopy.discardWorkingCopy();
	}

	private void clearWorkingCopyMaps()
	{
		// first discard all working copies

		Collection<ICompilationUnit> workingCopies;
		
		synchronized( this )
		{
			// make a copy to avoid race conditions
			workingCopies = new ArrayList<ICompilationUnit>( _generatedFile2WorkingCopy.values() );
		
			_generatedWorkingCopy2OpenParentFiles.clear();
			_generatedFile2WorkingCopy.clear();
		}
		
			
		for ( ICompilationUnit workingCopy : workingCopies )
		{
			try
			{
				workingCopy.discardWorkingCopy();
			}
			catch( JavaModelException jme )
			{
				AptPlugin.log(jme, "Could not discard working copy"); //$NON-NLS-1$
				// TODO:  deal with this
			}
		}
	}
	
	private void clearAllMaps() 
	{
		clearWorkingCopyMaps();
		
		synchronized( this )
		{
			// now clear file maps
			_parentFile2GeneratedFiles.clear();
			_generatedFile2ParentFiles.clear();
		}
	}
	
	/**
	 * Given a java project, this function will determine if the specified
	 * folder is a source folder of the java project. 
	 * 
	 * @param jp - the java project
	 * @param folder - the folder that you want to see if it is a classpath entry for the java project
	 * @return
	 * @throws JavaModelException
	 */
	public static IClasspathEntry findProjectSourcePath( IJavaProject jp, IFolder folder )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IClasspathEntry searchingFor = 
			JavaCore.newSourceEntry(folder.getFullPath());
		IPath searchingForPath = searchingFor.getPath();
		for (int i = 0; i < cp.length; i++) 
		{
			if (cp[i].getPath().equals( searchingForPath )) 
				return cp[i];
		}
		return null;
	}
	
	private static boolean isProjectClassPathUpToDate(		
			IJavaProject jp,
			IClasspathEntry[] cp,
			IPath path, 
			IProgressMonitor progressMonitor)
		throws JavaModelException
	{	
		if( cp == null )
			cp = jp.getRawClasspath();
		for (int i = 0; i < cp.length; i++) 
		{
			if (cp[i].getPath().equals( path )) 
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns true if we updated the classpath, false otherwise
	 */
	private static boolean updateProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IPath path = folder.getFullPath();
		boolean found = isProjectClassPathUpToDate(jp, cp, path, progressMonitor);
		
		if (!found) 
		{
			// update exclusion patterns
			ArrayList<IPath> exclusions = new ArrayList<IPath>();
			for ( int i = 0; i< cp.length; i++ )
			{
				if ( cp[i].getPath().isPrefixOf( path ) )
				{
					// exclusion patterns must be project-relative paths, and must end with a "/"
					IPath projectRelativePath = folder.getProjectRelativePath().addTrailingSeparator();
					
					// path is contained in an existing source path, so update existing paths's exclusion patterns				
					IPath[] oldExclusions = cp[i].getExclusionPatterns();

					// don't add if exclusion pattern already contains src dir
					boolean add = true;
					for ( int j = 0; j < oldExclusions.length; j++ )
						if ( oldExclusions[j].equals( projectRelativePath ) )
							add = false;
					
					if ( add )
					{
						IPath[] newExclusions;
						if ( cp[i].getExclusionPatterns() == null )
							newExclusions = new IPath[1];
						else
						{
							newExclusions = new IPath[ oldExclusions.length + 1 ];
							System.arraycopy( oldExclusions, 0, newExclusions, 0, oldExclusions.length );
						}
						newExclusions[ newExclusions.length - 1 ] = projectRelativePath;
						cp[i] = JavaCore.newSourceEntry(cp[i].getPath(), cp[i].getInclusionPatterns(), newExclusions, cp[i].getOutputLocation(), cp[i].getExtraAttributes());
					}
					
				}
				else if ( path.isPrefixOf( cp[i].getPath() ))
				{
					// new source path contains an existing source path, so add an exclusion pattern for it
					exclusions.add( cp[i].getPath().addTrailingSeparator() );
				}
			}
			
			IPath[] exclusionPatterns = exclusions.toArray( new IPath[exclusions.size()] );
			IClasspathEntry generatedSourceClasspathEntry = 
				JavaCore.newSourceEntry(folder.getFullPath(), exclusionPatterns );
			
			IClasspathEntry[] newCp = new IClasspathEntry[cp.length + 1];
			System.arraycopy(cp, 0, newCp, 0, cp.length);
			newCp[newCp.length - 1] = generatedSourceClasspathEntry;
			
			jp.setRawClasspath(newCp, progressMonitor );
		}

		// return true if we updated the project's classpath entries
		return !found;
	}

	/** 
	 * removes a classpath entry from the project 
	 */
	public static void removeFromProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
		throws JavaModelException
	{			
		IClasspathEntry[] cp = jp.getRawClasspath();
		IPath workspaceRelativePath = folder.getFullPath();
		boolean found = isProjectClassPathUpToDate(jp, cp, workspaceRelativePath, progressMonitor);
		
		if( found ){			
			IPath projectRelativePath = folder.getProjectRelativePath().addTrailingSeparator();
	
			// remove entries that are for the specified folder, account for 
			// multiple entries, and clean up any exclusion entries to the 
			// folder being removed.
			int j = 0;
			for ( int i=0; i<cp.length; i++ )
			{
				if (! cp[i].getPath().equals( workspaceRelativePath ) )
				{
				
					// see if we added the generated source dir as an exclusion pattern to some other entry
					IPath[] oldExclusions = cp[i].getExclusionPatterns();
					int m = 0;
					for ( int k = 0; k < oldExclusions.length; k++ )
					{
						if ( !oldExclusions[k].equals( projectRelativePath ) )
						{
							oldExclusions[m] = oldExclusions[k];
							m++;
						}
					}
					
					if ( oldExclusions.length == m )
					{
						// no exclusions changed, so we do't need to create a new entry
						cp[j] = cp[i];
					}
					else
					{
						// we've removed some exclusion, so create a new entry
						IPath[] newExclusions = new IPath[ m ];
						System.arraycopy( oldExclusions, 0, newExclusions, 0, m );
						cp[j] = JavaCore.newSourceEntry( cp[i].getPath(), cp[i].getInclusionPatterns(), newExclusions, cp[i].getOutputLocation(), cp[i].getExtraAttributes() );
					}
					
					j++;
				}
			}
			
			// now copy updated classpath entries into new array
			IClasspathEntry[] newCp = new IClasspathEntry[ j ];
			System.arraycopy( cp, 0, newCp, 0, j);
			jp.setRawClasspath( newCp, progressMonitor );
			
			if( AptPlugin.DEBUG ){
				AptPlugin.trace("removed " + workspaceRelativePath + " from classpath"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	/**
	 * invoked when a project is closed.  This will discard any open working-copies
	 * of generated files.
	 */
	public void projectClosed()
	{
		clearWorkingCopyMaps();
	}
	
	/**
	 * invoked whenever a project is cleaned.  This will remove any state kept about
	 * generated files for the given project.  If the deleteFiles flag is specified, 
	 * then the contents of the generated source folder will be deleted. 
	 *
	 * @param deleteFiles true if the contents of the generated source folder are to be
	 * deleted, false otherwise.
	 */
	
	public void projectClean( boolean deleteFiles )
	{
		clearAllMaps();
		
		// delete the generated source dir
		if ( deleteFiles )
		{
			IFolder f = getGeneratedSourceFolder();
			if ( f != null && f.exists() )
			{
				// delete the contents of the generated source folder, but don't delete
				// the generated source folder because that will cause a classpath change,
				// which will force the next build to be a full build.
				try
				{	
					IResource[] members = f.members();
					for ( int i = 0; i<members.length; i++ ){
						deleteDerivedResources(members[i]);
					}
				}
				catch ( CoreException ce )
				{
					AptPlugin.log(ce, "Could not delete generated files"); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * If the given resource is a folder, then recursively deleted all derived  
	 * files and folders contained within it. Delete the folder if it becomes empty
	 * and if itself is also a derived resource.
	 * If the given resource is a file, delete it iff it is a derived resource.
	 * The resource is left untouched if it is no a folder or a file.
	 * @param resource
	 * @return <code>true</code> iff the resource has been deleted.
	 * @throws CoreException
	 */
	private boolean deleteDerivedResources(final IResource resource)
		throws CoreException
	{		
		if( resource.getType() == IResource.FOLDER ){
			boolean deleteFolder = resource.isDerived();
			IResource[] members = ((IFolder)resource).members();
			for( int i=0, len=members.length; i<len; i++ ){	
				deleteFolder &= deleteDerivedResources(members[i]);
			}
			if( deleteFolder ){
				resource.delete(true, null);
				return true;
			}
			return false; 
		}
		else if( resource.getType() == IResource.FILE ){
			if( resource.isDerived() ){
				resource.delete(true, null);
				return true;
			}
			return false;
		}
		// will skip pass everything else.
		else
			return false;
	}
	
	/**
	 * Inovked when a project has been deleted.  This will remove this generated file manager
	 * from the static map of projects->generated file managers, and this will flush any known
	 * in-memory state tracking generated files.  This will not delete any of the project's generated files
	 * from disk.  
	 */
	public void projectDeleted()
	{
		projectClean( false );
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
		projectClean( false );
		
		IFolder srcFolder;
		synchronized(this){
			srcFolder = getGeneratedSourceFolder();
			_generatedSourceFolder = null;
		}
		if(AptPlugin.DEBUG)
			AptPlugin.trace("nulled out gen src dir " + srcFolder.getName() ); //$NON-NLS-1$
	}
	
	/**
	 *  This method should only be used for testing purposes to ensure
	 *  that maps contain entries when we expect them to.
	 */
	public synchronized boolean containsWorkingCopyMapEntriesForParent( IFile f )
	{
		Collection<Set<IFile>> parentSets = _generatedWorkingCopy2OpenParentFiles.values();
		if ( parentSets != null )
		{
			for( Set<IFile> s : parentSets )
			{
				if ( s.contains( f ) )
					return true;
			}
		}

		Set<IFile> generatedFiles = _parentFile2GeneratedFiles.get( f );
		if ( generatedFiles != null )
		{
			for ( IFile gf : generatedFiles )
			{
				ICompilationUnit cu = _generatedFile2WorkingCopy.get( gf );
				if ( cu != null )
				{
					Set<IFile> parents = _generatedWorkingCopy2OpenParentFiles.get( cu );
					if ( parents.contains( cu ) || parents.size() == 0 )
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @return get the generated source folder. May return null if
	 * creation has failed, the folder has been deleted or has not been created.
	 */
	public IFolder getGeneratedSourceFolder(){
		
		final String folderName;
		synchronized (this) {
			if( _generatedSourceFolder != null )
				return _generatedSourceFolder;
			folderName = getGeneratedSourceFolderName();
		}		
		if(folderName == null)
			return null;
		
		return _aptProject.getJavaProject().getProject().getFolder( folderName );
	}
	
	/**
	 * returns the name of the folder for generated source files.  The name is relative
	 * to the project root.
	 * 
	 * @see #getGeneratedSourceFolder()
	 * @see #isGeneratedSourceFolderConfigured()
	 */
	public synchronized String getGeneratedSourceFolderName() 
	{ 
		return _generatedSourceFolderName; 
	}
	
	public boolean isGeneratedSourceFolderConfigured(){
		return _generatedSourceFolder != null;
	}
	
	public void configureGeneratedSourceFolder(){
		final String folderName = _generatedSourceFolderName;
		if( AptPlugin.DEBUG ){
			AptPlugin.trace("configure genenerated source folder to be " + folderName ); //$NON-NLS-1$
		}
		configureGeneratedSourceFolder(folderName, null);
	}
	
	/**
	 * Simply set the name of the generated source folder. 
	 * <em>This should only be called when APT is disabled.</em>
	 * @param newName
	 */
	private void setGeneratedSourceFolderName(String newName){
		assert !AptConfig.isEnabled(_aptProject.getJavaProject()) :
			 "APT is enabled for " + _aptProject.getJavaProject().getElementName(); //$NON-NLS-1$
		if( newName == null || newName.length() == 0 )
			throw new IllegalStateException("[" + newName + "] not a valid name for generated source folder ");  //$NON-NLS-1$//$NON-NLS-2$
		synchronized (this) {
			_generatedSourceFolderName = newName;
		}
	}
	
	/**
	 * Sets the name of the generated soruce folder.  The source folder will not be created 
	 * and will not be added to the project's source paths (i.e., after a call to
	 * setGeneratedSourceFolderName, isGeneratedSourceFolderConfigured() will return false.)  
	 * To properly have the new generated source folder configured, call #ensureGeneratedSourceFolder(). 
	 * 
	 * @param newValue The string name of the new generated source folder.  This should be relative 
	 * to the project root.  Absolute paths are not supported.  The specified string should be 
	 * a valid folder name for the file system, and should not be an existing source folder for the 
	 * project.  
	 * 
	 * @see #getGeneratedSourceFolder()
	 * @see #getGeneratedSourceFolderName()
	 * @see #isGeneratedSourceFolderConfigured()
	 */
	private void configureGeneratedSourceFolder( String newValue, String oldValue ) 
	{
		
		// bail if they specify null, empty-string or don't change the name of the source folder
		if ( newValue == null || 
			 newValue.length() == 0 || 
			 newValue.equals(oldValue) )
			return;
		
		projectClean( true );

		IFolder srcFolder = null;
		synchronized ( this )
		{
			// We are not going to delete any directories or change the classpath
			// since this could happen during a build. 
			// see ensureGeneratedSourceFolder() 
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
	 * Cleanup the classpath and schedule a job to delete the generated source folder.
	 * @param recreate if <code>true</code> configure the generated source directory.
	 */
	private void resetGeneratedSrcFolder(final IFolder srcFolder, boolean recreate){
		// clean up the classpath first so that when we actually delete the 
		// generated source folder and won't cause a classpath error.
		if( srcFolder != null ){
			try{	
		  		removeFromProjectClasspath( _aptProject.getJavaProject(), srcFolder, null );		
			}catch(JavaModelException e){
				AptPlugin.log( e, "Error occurred deleting old generated src folder " + srcFolder.getName() ); //$NON-NLS-1$
			}
		}
		
		if( recreate )
			ensureGeneratedSourceFolder();
		
		if( srcFolder != null ){
			// schedule the deletion job.
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
