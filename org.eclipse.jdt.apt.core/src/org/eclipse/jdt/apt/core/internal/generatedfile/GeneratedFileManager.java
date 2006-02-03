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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.Messages;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IBuffer;
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

	private final IJavaProject _jProject;
	
	private final GeneratedSourceFolderManager _gsfm;
	
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
	 * Clients should not instantiate this class; it is created only by @see AptProject .
	 */
	public GeneratedFileManager(final AptProject aptProject, final GeneratedSourceFolderManager gsfm) {
		_jProject = aptProject.getJavaProject();
		_gsfm = gsfm;
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
		// If the generated package fragment root wasn't set,
		// then our classpath is incorrect. Add a marker and return
		else if( _generatedPackageFragmentRoot == null ){			
			String message = Messages.bind(
					Messages.GeneratedFileManager_missing_classpath_entry, 
					new String[] {_snapshotFolderName});
			IMarker marker = _jProject.getProject().createMarker(AptPlugin.APT_CONFIG_PROBLEM_MARKER);
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
					if (oldData != null) {
						try {
							oldData.close();
						} 
						catch (IOException ioe) 
						{}
					}
					if (is != null) {
						try {
							is.close();
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
			
			// during a batch build, parentFile will be null.
			// Only keep track of ownership in iterative builds
			if( parentFile != null ) {
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
	private void discardGeneratedWorkingCopy( IFile generatedFile, IFile parentFile )
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
			final IFolder genFolder = _gsfm.getFolder();
			assert genFolder != null : "Generated folder == null"; //$NON-NLS-1$
			IContainer parent = generatedFile.getParent();
			try {
				generatedFile.delete(true, true, progressMonitor);
			}
			catch (CoreException ce) {
				// File was locked or read-only
				AptPlugin.logWarning(ce, "Failed to delete file: " + generatedFile); //$NON-NLS-1$
			}
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

		IFolder folder = _gsfm.getFolder();
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
		
		IFolder folder = _gsfm.getFolder();
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
	 * Called at the start of build in order to cache our package fragment root
	 */
	public void compilationStarted() {

		try{
			// clear out any generated source folder config markers
			IMarker[] markers = _jProject.getProject().findMarkers(AptPlugin.APT_CONFIG_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( markers != null ){
				for( IMarker marker : markers )
					marker.delete();
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete configuration marker."); //$NON-NLS-1$
		}
		_skipTypeGeneration = false;
		_gsfm.ensureFolderExists();
		final IFolder genFolder;		
		synchronized(this){
			genFolder = _gsfm.getFolder();
			_snapshotFolderName = genFolder.getProjectRelativePath().toString();
		}
		try {
			_generatedPackageFragmentRoot = null;
			IPackageFragmentRoot[] roots = _jProject.getAllPackageFragmentRoots();
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
		IFolder folder = _gsfm.getFolder();
		
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
		IPackageFragmentRoot root = _jProject.getPackageFragmentRoot(folder);
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
	
	public void addEntryToFileMaps( IFile parentFile, IFile generatedFile )
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

	public void clearWorkingCopyMaps()
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
	
	public void clearAllMaps() 
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
	
}
