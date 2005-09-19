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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.AptPlugin;
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
	

	// Use a weak hash map to allow file managers to get GC'ed if a project
	// goes away
	private static final Map<IProject, GeneratedFileManager> MANAGERS_MAP = 
		new WeakHashMap<IProject, GeneratedFileManager>();
	
	/**
	 * Construction can only take place from within 
	 * the factory method, getGeneratedFileManager().
	 */
	private GeneratedFileManager(final IProject project) {
		_project = project;
		_javaProject = JavaCore.create( _project );
		
		// get generated source dir from config 
		// default value is set in org.eclipse.jdt.apt.core.internal.util.AptCorePreferenceInitializer
		_generatedSourceFolderName = AptConfig.getString( _javaProject, AptPreferenceConstants.APT_GENSRCDIR);
	}

	private static void init()
	{
		_initialized = true;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		// register resource-changed listener
		int mask = IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;
		workspace.addResourceChangeListener( new ResourceChangedListener(), mask );
		
		// register element-changed listener
		mask = ElementChangedEvent.POST_CHANGE;
		JavaCore.addElementChangedListener( new ElementChangedListener(), mask );
	}
	
	/**
	 *  Returns a list of the generated file managers for all projects.  
	 */
	public static synchronized List<GeneratedFileManager> getGeneratedFileManagers() {
		return new ArrayList(MANAGERS_MAP.values());
	}
	
	/**
	 *  returns a generated file manager instance for the specified project.  If one doesn't
	 *  already exist, then one will be created. 
	 */
	public static synchronized GeneratedFileManager getGeneratedFileManager(final IProject project) 
	{
		if ( project == null )
			return null;
		
		if ( ! _initialized ) 
			init();
		GeneratedFileManager gfm = MANAGERS_MAP.get(project);
		if (gfm != null)
			return gfm;

		gfm = new GeneratedFileManager(project);
		MANAGERS_MAP.put(project, gfm);
		return gfm;
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
			IProgressMonitor progressMonitor)
	throws CoreException
	{
		try{
			boolean updatededSourcePath = ensureGeneratedSourceFolder( progressMonitor );
			final IFolder genFolder = getGeneratedSourceFolder();
			IPackageFragmentRoot genFragRoot = null;
			IPackageFragmentRoot[] roots = _javaProject.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot root : roots) {
				if( genFolder.equals(root.getResource()) ){
					genFragRoot = root;
					break;
				}
			}
			if( genFragRoot == null ){
				throw new IllegalStateException("failed to locate package fragment root for " + genFolder.getName()); //$NON-NLS-1$
			}
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
			IPackageFragment pkgFrag = genFragRoot.createPackageFragment(pkgName, true, progressMonitor);
			if( pkgFrag == null ){
				IStatus status = AptPlugin.createStatus(
						new IllegalStateException("failed to locate package '" + pkgName + "'"),  //$NON-NLS-1$ //$NON-NLS-2$
						"Failure generating file");  //$NON-NLS-1$
				throw new CoreException(status);
			}			
			final String cuName = typeSimpleName + ".java"; //$NON-NLS-1$
			
			ICompilationUnit unit = pkgFrag.getCompilationUnit(cuName);
			boolean contentsDiffer = true;
			
			if( unit.exists() ){
				InputStream oldData = null;
				InputStream is = null;
				try {
					is = new ByteArrayInputStream( contents.getBytes() );
					oldData = new BufferedInputStream( ((IFile)unit.getResource() ).getContents());
					contentsDiffer = !compareStreams(oldData, is);
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
			
			if( contentsDiffer )
				unit = pkgFrag.createCompilationUnit(cuName, contents, true, progressMonitor);
			
			if( unit == null ) {
				IStatus status = AptPlugin.createStatus(new IllegalStateException("Unable to create unit for " + cuName), "Failure generating file"); //$NON-NLS-1$ //$NON-NLS-2$
				throw new CoreException(status);
			}
			else{
				final IFile file = (IFile)unit.getResource();
				file.setDerived( true );
				// We used to also make the file read-only. This is a bad idea,
				// as refactorings then fail in the future, which is worse
				// than allowing a user to modify a generated file.
				
				// during a batch build
				if( parentFile != null )
					addEntryToFileMaps( parentFile, file );
				return new FileGenerationResult(file, contentsDiffer, updatededSourcePath);
			}
			
		}
		catch(Exception e){
			AptPlugin.log(e, "failed to generate type " + typeName); //$NON-NLS-1$
		}
		IStatus status = AptPlugin.createStatus(new IllegalStateException("Failed to generate type " + typeName), "Failure generating file"); //$NON-NLS-1$ //$NON-NLS-2$
		throw new CoreException(status);
	}	
	
	/**
	 * Return true if the content of the streams is identical, 
	 * false if not.
	 */
	private static boolean compareStreams(InputStream is1, InputStream is2) {
		try {
			int b1 = is1.read();
	        while(b1 != -1) {
	            int b2 = is2.read();
	            if(b1 != b2) {
	                return false;
	            }
	            b1 = is1.read();
	        }

	        int b2 = is2.read();
	        if(-1 != b2) {
	            return false;
	        }
	        return true;
		}
		catch (IOException ioe) {
			return false;
		}
	}
		
	/**
	 * This function generates a type "in-memory" by creating or updating a working copy with the
	 * specified contents.   The generated-source folder must be configured correctly for this to 
	 * work. This method takes no locks, so it is safe to call when holding fine-grained resource 
	 * locks (e.g., during some reconcile paths).  Since this only works on an in-memory working 
	 * copy of the type, the IFile for the generated type may not exist on disk.  Likewise, the
	 * corresponding package directories of type-name may not exist on disk.   
	 * 
	 * TODO:  figure out how to create a working copy with a client-specified character se
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
	 * @see #ensureGeneratedSourceFolder(IProgressMonitor)
	 */
	public  FileGenerationResult generateFileDuringReconcile(
			ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor ) 
	{	
		
		if (!GENERATE_TYPE_DURING_RECONCILE)
			return null;
		
		// type-generation during reconcile only works if the generated source
		// folder is created and added to the project's source path. 
		if ( ! isGeneratedSourceFolderConfigured() )
			return null;
		
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
				result = new FileGenerationResult((IFile)workingCopy.getResource(), true, false);
			}
			else
			{

				//
				//  Update working copy's buffer with the contents of the type 
				// 
				boolean modified = updateWorkingCopy( contents, workingCopy, workingCopyOwner, progressMonitor );
				result = new FileGenerationResult((IFile)workingCopy.getResource(), modified, false);
			}
			
			return result;
		} 
		catch (JavaModelException jme) 
		{
			AptPlugin.log(jme, "Could not generate file for type: " + typeName); //$NON-NLS-1$
		} 
		return new FileGenerationResult((IFile)workingCopy.getResource(), true, false);
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
	 * @see #ensureGeneratedSourceFolder(IJavaProject, IProgressMonitor)
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
			IContainer parent = generatedFile.getParent();
			generatedFile.delete(true, true, progressMonitor);
			while( !genFolder.equals(parent) && parent != null ){
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
		throws JavaModelException
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
	
	/** 
	 * given an IFile, this will make sure that the intermediate folders
	 * are created. 
	 */
	private void createFoldersForFile( IFile f )
	    throws CoreException
	{
		String[] parts = f.getProjectRelativePath().segments();
		IContainer c = f.getProject();

		IFolder folder = c.getFolder( new Path( parts[0] ) );
		if ( !folder.exists() )
			folder.create( true, false, null );
		
		for ( int i = 1; i < parts.length - 1; i++ )
		{
			folder = folder.getFolder( parts[i] );
			if ( !folder.exists() )
				folder.create( true, false, null );
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
	 *  @param progressMonitor the progress monitor.  This can be null. 
	 *  
	 *  @return true if the generatedSourceFolder is added to the project's classpath entries,
	 *  false if it is not added to the project's classpath entries.  
	 *  
	 *  @see #getGeneratedSourceFolder()
	 *  @see #isGeneratedSourceFolderConfigured()
	 */
	public boolean ensureGeneratedSourceFolder( IProgressMonitor progressMonitor )
		throws CoreException
	{
		synchronized( this )
		{
			if ( _generatedSourceFolder != null )
				return false;
		}
		
		// don't take any locks in while creating the folder, since we are doing file-system operations
		IFolder srcFolder = getGeneratedSourceFolder();
		srcFolder.refreshLocal( IResource.DEPTH_INFINITE, progressMonitor );
		if (!srcFolder.exists()) {
			FileSystemUtil.mkdirs(srcFolder);
			// Since we've created it, mark it as derived
			srcFolder.setDerived(true);
		}
			
		//
		// make sure __generated_src dir is on the cp if not already
		//
		boolean addedToSourcePath = updateProjectClasspath( _javaProject, srcFolder, progressMonitor );
		
		synchronized ( this )
		{
			_generatedSourceFolder = srcFolder;
			return addedToSourcePath;
		}
	}
	
	/** 
	 * @return true if the generated source folder has been created and added to the project's source path, false otherwise
	 * 
	 * @see #getGeneratedSourceFolder()
	 * @see #getGeneratedSourceFolderName()
	 * @see #ensureGeneratedSourceFolder(IJavaProject, IProgressMonitor)
	 */
	public boolean isGeneratedSourceFolderConfigured()
	{
		// if _generatedSourceFolder is non-null, then it has been
		// created and added to the project's classpath
		synchronized( this )
		{
			return ( _generatedSourceFolder != null );
		}
	}
	
	
	/**
	 * This method will return the binary output location for the generated source folder.
	 * If the generated-source folder is not configured (i.e., not created or not added to
	 * the project's source path, then this method will return the default binary output
	 * location for the project. 
	 * 
	 * TODO - change this to return an IFolder
	 *
	 * @return the java.io.File corresponding to the binary output location for the
	 * generated source folder.  
	 * 
	 * @throws JavaModelException
	 * 
	 * @see #getGeneratedSourceFolder()
	 * @see #isGeneratedSourceFolderConfigured()
	 * @see #ensureGeneratedSourceFolder(IProgressMonitor)
	 */
	public java.io.File getGeneratedSourceFolderOutputLocation()
		 throws JavaModelException 
	{
		IPath outputRoot = null;
		IFolder f = getGeneratedSourceFolder();
		if ( f != null && f.exists() )
		{
			IClasspathEntry cpe = findProjectSourcePath( _javaProject, f );
			if ( cpe != null )
				outputRoot = cpe.getOutputLocation();
		}
		
		// no output root, so get project's default output location
		if ( outputRoot == null )
			outputRoot = _javaProject.getOutputLocation();

		// output location is relative to the workspace, we want to make it relative to project
		int segments = outputRoot.matchingFirstSegments( _javaProject.getPath() );
		outputRoot = outputRoot.removeFirstSegments( segments );
		
		// TODO - use getRawLocation() or getLocation()?  sometimes getRawLocation() returns null.  Investigate
		IPath projectRoot = _javaProject.getProject().getRawLocation();
		if ( projectRoot == null )
			projectRoot = _javaProject.getProject().getLocation();
		
		java.io.File file = projectRoot.toFile();
		file = new java.io.File( file, outputRoot.toFile().getPath() );
		return file;	
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
		IPackageFragmentRoot root = _javaProject.getPackageFragmentRoot(folder);
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
			if (!derivedFiles.contains(generatedFile))
				throw new RuntimeException(
					"derivedFiles does not contain fileToDelete"); //$NON-NLS-1$

			derivedFiles.remove(generatedFile);
		
			// update _derivedFile2Parents map
			Set<IFile> parents = _generatedFile2ParentFiles.get(generatedFile);

			// assertions
			if (parents == null)
				throw new RuntimeException(" parents is null and it shouldn't be"); //$NON-NLS-1$
			if (!parents.contains(parentFile))
				throw new RuntimeException("parents set does not contain parent"); //$NON-NLS-1$

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
	
	/**
	 * returns true if we updated the classpath, false otherwise
	 */
	private boolean updateProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		boolean found = false;
		IPath path = folder.getFullPath();
		for (int i = 0; i < cp.length; i++) 
		{
			if (cp[i].getPath().equals( path )) 
			{
				found = true;
				break;
			}
		}
		
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
					for ( int i = 0; i<members.length; i++ )
						members[i].delete( true, null );
				}
				catch ( CoreException ce )
				{
					AptPlugin.log(ce, "Could not delete generated files"); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * Inovked when a project has been deleted.  This will remove this generated file manager
	 * from the static map of projects->generated file managers, and this will flush any known
	 * in-memory state tracking generated files.  This will not delete any of the project's generated files
	 * from disk.  
	 */
	public void projectDeleted()
	{
		//
		// remove this project from the managers map.  Some other clients may still
		// have a reference to this, but that should be fine since the project is being
		// deleted.  We'll just empty out member fields rather than
		// setting them to null to avoid NPEs.
		//
		synchronized( this.getClass() )
		{
			MANAGERS_MAP.remove( _project );
		}
		
		// TODO:  eventually make this true.  Right now, the resource tree is locked 
		// when we get the project-deleted event, so we can't delete any files.
		projectClean( false );
	}
	
	/**
	 *  Invoked when the generated source folder has been deleted.  This will 
	 *  flush any in-memory state tracking generated files. 
	 */
	public void generatedSourceFolderDeleted()
	{
		// jdt-core will remove the generated source folder from the java 
		// project's classpath, so we'll just clean out our maps. 
		projectClean( false );
		synchronized( this )
		{
			_generatedSourceFolder = null;
		}
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
	 *  Will return an IFolder corresponding to the generated source folder name.  The result
	 *  IFolder may not exist and may not necessarily be on the java project's classpath. 
	 *  To ensure that the generated source folder is created and added to as source path
	 *  to the project, call ensureGeneratedSourceFolder().
	 *  
	 *   @see #ensureGeneratedSourceFolder(IJavaProject, IProgressMonitor)
	 *   @see #isGeneratedSourceFolderConfigured()
	 *   @see #getGeneratedSourceFolderName()
	 */
	public synchronized IFolder getGeneratedSourceFolder()
	{
		//
		// don't set _generatedSourceFolder in here, let that happen in 
		// ensureGeneratedSourceFolder. we use a non-null _generatedSourceFolder 
		// as an indicator that as an indicator that the folder has been created
		// and added to the project's source path.
		//
			
		if ( _generatedSourceFolder != null)
			return _generatedSourceFolder;
		else
			// OK to call getFolder while holding a lock.  getFolder() doesn't take any locks - Mike K.
			return _project.getFolder( _generatedSourceFolderName );
	}
	
	/**
	 * returns the name of the folder for generated source files.  The name is relative
	 * to the project root.
	 * 
	 * @see #getGeneratedSourceFolder()
	 * @see #ensureGeneratedSourceFolder(IJavaProject, IProgressMonitor)
	 * @see #isGeneratedSourceFolderConfigured()
	 */
	public synchronized String getGeneratedSourceFolderName() 
	{ 
		return _generatedSourceFolderName; 
	}

	
	/**
	 * Sets the name of the generated soruce folder.  The source folder will not be created 
	 * and will not be added to the project's source paths (i.e., after a call to
	 * setGeneratedSourceFolderName, isGeneratedSourceFolderConfigured() will return false.)  
	 * To properly have the new generated source folder configured, call #ensureGeneratedSourceFolder(). 
	 * 
	 * @param s The string name of the new generated source folder.  This should be relative 
	 * to the project root.  Absolute paths are not supported.  The specified string should be 
	 * a valid folder name for the file system, and should not be an existing source folder for the 
	 * project.  
	 * 
	 * @see #getGeneratedSourceFolder()
	 * @see #getGeneratedSourceFolderName()
	 * @see #ensureGeneratedSourceFolder(IProgressMonitor)
	 * @see #isGeneratedSourceFolderConfigured()
	 */
	public void setGeneratedSourceFolderName( String s ) 
	{
		synchronized( this )
		{
			// bail if they specify null, empty-string or don't change the name of the source folder
			if ( s == null || s.length() == 0 || s.equals( _generatedSourceFolderName ) )
				return;
		}
		
		projectClean( true );

		IFolder srcFolder;
		synchronized ( this )
		{
			_generatedSourceFolderName = s;
			// save _generatedSrcFolder off to avoid race conditions
			srcFolder = _generatedSourceFolder;
			_generatedSourceFolder = null;
		}
		
		// delete generatedSourceFolder
		if ( srcFolder != null )
		{
			try
			{
				removeFromProjectClasspath( _javaProject, srcFolder, null );
				if ( srcFolder.exists() )
					srcFolder.delete( true,false, null );
			}
			catch( CoreException ce )
			{
				AptPlugin.log( ce, "Error occurred deleting old generated src folder " + srcFolder.getName() ); //$NON-NLS-1$
			}
		}
	}
	
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
	
	private final IProject _project;
	
	private final IJavaProject _javaProject;
	
	
	private static boolean _initialized = false;
	
}
