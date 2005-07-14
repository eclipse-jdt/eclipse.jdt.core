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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.AptPlugin;
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
	
	private final IProject _project;
	
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
	
	public static synchronized List<GeneratedFileManager> getGeneratedFileManagers() {
		return new ArrayList(MANAGERS_MAP.values());
	}
	
	public static synchronized GeneratedFileManager getGeneratedFileManager(final IProject project) 
	{
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
	 * Return the file and a flag indicating if the content was modified.
	 * 
	 * @param parentFile
	 * @param typeName
	 * @param contents
	 * @param progressMonitor
	 * @param charsetName
	 * @return - the newly created IFile along with whether it was modified
	 * @throws CoreException
	 * @throws UnsupportedEncodingException
	 */
	public FileGenerationResult generateFileDuringBuild(
			IFile parentFile,
			IJavaProject javaProject,
			String typeName, 
			String contents, 
			IProgressMonitor progressMonitor,
			String charsetName ) 
		throws CoreException, UnsupportedEncodingException
	{
		try
		{		
			IProject project = javaProject.getProject();

			IFolder folder = ensureGeneratedSourceFolder( javaProject, progressMonitor );
			
			IFile file = getIFileForTypeName( typeName, javaProject, true, progressMonitor );

			
			byte[] bytes;
			if ( charsetName == null || charsetName == "" )
				bytes = contents.getBytes();
			else
				bytes = contents.getBytes( charsetName );
			InputStream is = new ByteArrayInputStream( bytes );
			
			boolean contentsDiffer = true;
			
			if ( !file.exists() )
			{
				file.create( is, true, progressMonitor );
			}
			else
			{
				// Check if the content has changed
				InputStream oldData = null;
				try {
					oldData = new BufferedInputStream(file.getContents());
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
				if (contentsDiffer) {
					makeReadOnly( file, false );
					file.setContents( is, true, true, progressMonitor );
				}
			}
			
			file.setDerived( true );
			
			makeReadOnly( file, true );
			
			addEntryToFileMaps( parentFile, file );
			return new FileGenerationResult(file, contentsDiffer);
		}
		catch ( Throwable t )
		{
			AptPlugin.log(t, "Could not generate file for type: " + typeName);
		}
		
		return null;
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
	 * TODO:  figure out how to create a working copy with a client-specified character set
	 * 
	 * 
	 * @param parentCompilationUnit
	 * @param typeName
	 * @param contents
	 * @param workingCopyOwner
	 * @param problemRequestor
	 * @param progressMonitor
	 * @return
	 */
	public  FileGenerationResult generateFileDuringReconcile(
			ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor ) 
	{
		// BUGZILLA 103183 - reconcile-path disabled until type-generation in reconcile is turned on
		if ( true )
			return null;
		
		
		ICompilationUnit workingCopy = null;
		FileGenerationResult result = null;
		try 
		{
			//
			// get working copy (either from cache or create a new one)
			//
			workingCopy = getCachedWorkingCopy( parentCompilationUnit, typeName );
			
			if ( workingCopy == null )
			{
				// create a new working copy
				workingCopy = createNewWorkingCopy(  
						parentCompilationUnit,  typeName, contents,  
						workingCopyOwner, problemRequestor,  progressMonitor);
				workingCopy.reconcile(AST.JLS3, true, workingCopyOwner,
						progressMonitor);
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
			AptPlugin.log(jme, "Could not generate file for type: " + typeName);
		} 
		catch (CoreException ce) 
		{
			AptPlugin.log(ce, "Could not generate file for type: " + typeName);
		}
		return new FileGenerationResult((IFile)workingCopy.getResource(), true);
	}

	
	public synchronized boolean isGeneratedFile( IFile f )
	{
		Set<IFile> s = _generatedFile2ParentFiles.get( f ); 
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	
	public synchronized boolean isParentFile( IFile f )
	{
		Set<IFile> s = _parentFile2GeneratedFiles.get( f );
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	

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
	 * 
	 * @param parent - the parent file that you want to get generated files for
	 * @return Set of IFile instances that are the files known to be generated
	 * by this parent
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
	 *  Invoked whenever we need to discard a generated working copy
	 */
	public void discardGeneratedWorkingCopy( IFile derivedFile, IFile parentFile )
		throws JavaModelException
	{
		removeFromWorkingCopyMaps( derivedFile, parentFile );
	}

	/**
	 *  Invoked whenever a parent working copy has been discarded
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
	 *  or a parent stops generating a specific child)
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
			if ( parents == null ) throw new RuntimeException("unexpected null value for parents set for file " + generatedFile);
		
			if (parents == null || parents.size() == 0) 
				delete = true;
		}
		
		if ( delete )
			generatedFile.delete(true, true, progressMonitor);
		
		return delete;
	}
	
	public void generatedFileDeleted( IFile generatedFile,  IProgressMonitor progressMonitor )
		throws JavaModelException, CoreException
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
		IFolder folder = project.getFolder( GENERATED_SOURCE_FOLDER_NAME );
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
	 * @param typeName
	 * @return
	 */
	private IFile getIFileForTypeName( String typeName, IJavaProject javaProject, boolean create, IProgressMonitor progressMonitor)
	    throws CoreException
	{
		// split the type name into its parts
		String[] parts = typeName.split( "\\.");
		
		IFolder folder;
		if ( create )
			folder = ensureGeneratedSourceFolder( javaProject, progressMonitor );
		else
			folder = getGeneratedSourceFolder();
		
		//  create folders for the package parts
		int i = 0;
		for ( ;i < parts.length - 1; i++ )
		{
			folder = folder.getFolder( parts[i] );
			if ( create && !folder.exists() )
				folder.create( true, false, null );
		}
	
		String fileName = parts[i] + ".java";		
		IFile file = folder.getFile( fileName );
		return file;
	}
	
	/**
	 *  Creates the generated source folder if it doesn't exist, and adds it as a source path
	 *  to the project.  To access the generated source folder, but not have it be created
	 *  or added as a source path, use getGeneratedSourceFolder()
	 *  
	 *  @see #getGeneratedSourceFolder()
	 */
	private IFolder ensureGeneratedSourceFolder( IJavaProject javaProject, IProgressMonitor progressMonitor )
		throws CoreException
	{
		// don't take any locks in this method, since we are doing file-system operations
		IFolder srcFolder = getGeneratedSourceFolder();
		srcFolder.refreshLocal( IResource.DEPTH_INFINITE, progressMonitor );
		if (!srcFolder.exists())
			srcFolder.create(true, false, progressMonitor );
			
		//
		// make sure __generated_src dir is on the cp if not already
		//
		updateProjectClasspath( javaProject, _generatedSourceFolder, progressMonitor );
		
		return srcFolder;
	}
	
	/**
	 *  Will return an IFolder corresponding to the generated source folder name.  The result
	 *  IFolder may not exist and may not necessarily be on the java project's classpath. 
	 *  To ensure that the generated source folder is created and added to as source path
	 *  to the project, call ensureGeneratedSourceFolder().
	 *  
	 *   @see #ensureGeneratedSourceFolder(IJavaProject, IProgressMonitor)
	 */
	public synchronized IFolder getGeneratedSourceFolder()
	{
		if ( _generatedSourceFolder == null)
		{
			// OK to call getFolder while holding a lock.  getFolder() doesn't take any locks - Mike K.
			_generatedSourceFolder = _project.getFolder( GENERATED_SOURCE_FOLDER_NAME );
		}

		return _generatedSourceFolder;
	}
	
	
	// TODO - change this to return an IFolder
	public java.io.File getGeneratedOutputFile( IJavaProject jp )
		 throws JavaModelException, CoreException
	{
		IPath outputRoot = null;
		IFolder f = getGeneratedSourceFolder();
		if ( f != null && f.exists() )
		{
			IClasspathEntry cpe = findProjectSourcePath( jp, f, null );
			if ( cpe != null )
				outputRoot = cpe.getOutputLocation();
		}
		
		// no output root, so get project's default output location
		if ( outputRoot == null )
			outputRoot = jp.getOutputLocation();

		// output location is relative to the workspace, we want to make it relative to project
		int segments = outputRoot.matchingFirstSegments( jp.getPath() );
		outputRoot = outputRoot.removeFirstSegments( segments );
		
		// TODO - use getRawLocation() or getLocation()?  sometimes getRawLocation() returns null.  Investigate
		IPath projectRoot = jp.getProject().getRawLocation();
		if ( projectRoot == null )
			projectRoot = jp.getProject().getLocation();
		
		java.io.File file = projectRoot.toFile();
		file = new java.io.File( file, outputRoot.toFile().getPath() );
		return file;	
	}
	
	//
	//  check cache to see if we already have a working copy
	//
	private ICompilationUnit getCachedWorkingCopy( ICompilationUnit parentCompilationUnit, String typeName )
		throws CoreException
	{
		IFile derivedFile = getIFileForTypeName( typeName, parentCompilationUnit.getJavaProject(), false, null /*progressMonitor*/ );
		ICompilationUnit workingCopy= null;
		
		synchronized( this )
		{
			workingCopy = (ICompilationUnit) _generatedFile2WorkingCopy.get( derivedFile );
		}
		
		if ( workingCopy != null )
			addEntryToWorkingCopyMaps( parentCompilationUnit, workingCopy );

		return workingCopy;
	}
	
	private ICompilationUnit createNewWorkingCopy(ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor)
		throws CoreException, JavaModelException
	{	
		IProject project = parentCompilationUnit.getResource().getProject();
		IJavaProject jp = parentCompilationUnit.getJavaProject();

		//
		// create folder for generated source files
		//
		IFolder folder = ensureGeneratedSourceFolder( jp, progressMonitor );

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
				typeName.substring(idx + 1, typeName.length()) + ".java";
		}
		else
		{
			pkgName = "";
			fname = typeName + ".java";
		}

		//
		//  create compilation unit
		//
		IPackageFragmentRoot root = jp.getPackageFragmentRoot(folder);
		IPackageFragment pkgFragment = 
			root.createPackageFragment( pkgName, true, null );
		
		ICompilationUnit cu = pkgFragment.getCompilationUnit( fname );
		if ( cu == null || ! cu.getResource().exists() )
		{
		    cu = pkgFragment.createCompilationUnit(
			    fname, contents, true, progressMonitor );
		}
		else
		{
			makeReadOnly( cu, false );
		}

		
		//
		//  TODO:  can we call getWorkingCopy here?
		//
		cu.becomeWorkingCopy(problemRequestor, progressMonitor);
		ICompilationUnit workingCopy = cu;
		
		//
		// update maps
		//
		addEntryToWorkingCopyMaps( parentCompilationUnit, workingCopy );
		
		// we save this here since the resource has to exist on disk
		workingCopy.commitWorkingCopy( true, progressMonitor );
		
		//
		// make the file derived so that it is not checked into source control.
		//
		makeDerived( workingCopy );
		
		//
		// make working copy read-only
		//
		makeReadOnly( workingCopy, true );


		return workingCopy;
		
	}

	private void makeReadOnly( ICompilationUnit cu, boolean readOnly )
		throws CoreException
	{
		IResource r = cu.getResource();
		makeReadOnly( r, readOnly );
	}
	
	/**
	 *  make the compilation unit read-only
	 */
	private void makeReadOnly( IResource r, boolean readOnly )
		throws CoreException
	{
		if ( r.exists() )
		{
			ResourceAttributes ra = r.getResourceAttributes();
			if (ra == null)
				ra = new ResourceAttributes();
			ra.setReadOnly( readOnly );
			r.setResourceAttributes(ra);
		}
	}
	
	private void makeDerived( ICompilationUnit cu )
		throws CoreException
	{
		IResource r = cu.getResource();
		if ( r.exists() )
			r.setDerived( true );

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
	
	private void addEntryToWorkingCopyMaps( ICompilationUnit parentCompilationUnit, ICompilationUnit workingCopy )
	{
		IFile parentFile = (IFile) parentCompilationUnit.getResource();
		IFile generatedFile = (IFile) workingCopy.getResource();
		addEntryToFileMaps( parentFile, generatedFile );

		synchronized( this )
		{
			ICompilationUnit cu = (ICompilationUnit)_generatedFile2WorkingCopy.get( generatedFile );
			Set<IFile> parents = _generatedWorkingCopy2OpenParentFiles.get( workingCopy);
		
			if ( cu != null )
			{
				//assert( cu.equals( workingCopy ) ) : "unexpected different instances of working copy for the same type";
				if ( !cu.equals(workingCopy) ) throw new RuntimeException( "unexpected different instances of working copy for the same type" );
				if ( parents == null || parents.size() < 1 ) throw new RuntimeException( "Unexpected size of open-parents set.  Expected size >= 0");
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
	    throws CoreException 
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
					"derivedFiles is null and it shouldn't be");
			if (!derivedFiles.contains(generatedFile))
				throw new RuntimeException(
					"derivedFiles does not contain fileToDelete");

			derivedFiles.remove(generatedFile);
		
			// update _derivedFile2Parents map
			Set<IFile> parents = _generatedFile2ParentFiles.get(generatedFile);

			// assertions
			if (parents == null)
				throw new RuntimeException(" parents is null and it shouldn't be");
			if (!parents.contains(parentFile))
				throw new RuntimeException("parents set does not contain parent");

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
			if ( parents == null ) throw new RuntimeException( "parents == null and it shouldnt");
			if ( ! parents.contains( parentFile )) throw new RuntimeException("parents set should contain parentCompilationUnit");
		
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
				AptPlugin.log(jme, "Could not discard working copy");
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
	
	private IClasspathEntry findProjectSourcePath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IClasspathEntry searchingFor = 
			JavaCore.newSourceEntry(folder.getFullPath());
		IPath searchingForPath = searchingFor.getPath();
		boolean found = false;
		for (int i = 0; i < cp.length; i++) 
		{
			if (cp[i].getPath().equals( searchingForPath )) 
			{
				return cp[i];
			}
		}
		return null;
	}
	
	private void updateProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
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
	}
	
	private void removeFromProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
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
	
	public void projectClosed()
	{
		clearWorkingCopyMaps();
	}
	
	public void projectClean( boolean deleteFiles )
	{
		clearAllMaps();
		
		// delete the generated source dir
		if ( deleteFiles )
		{
			IFolder f = _project.getFolder( GENERATED_SOURCE_FOLDER_NAME );
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
					AptPlugin.log(ce, "Could not delete generated files");
				}
			}
		}
	}
	
	/**
	 * Inovked when a project has been deleted
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
	 *  Invoked when the generated source folder has been deleted.
	 */
	public void generatedSourceFolderDeleted()
		throws CoreException
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
	 * The folder where generated source files are placed
	 */
	private IFolder _generatedSourceFolder;
	
	private static boolean _initialized = false;
	
	private static final String GENERATED_SOURCE_FOLDER_NAME = "__generated_src";

}
