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
import java.util.Iterator;
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
import org.eclipse.jdt.internal.core.JavaProject;


/**
 * Class for managing generated files
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
		int mask = IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE;
		workspace.addResourceChangeListener( new ResourceChangedListener(), mask );
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
	public synchronized FileGenerationResult generateFileDuringBuild(
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
			// create folder for generated source files
			IFolder folder = project.getFolder( GENERATED_SOURCE_FOLDER_NAME );
			if (!folder.exists())
				folder.create(true, false, null);

			//
			// make sure __generated_src dir is on the cp if not already
			//
			updateProjectClasspath( (JavaProject)javaProject, folder, progressMonitor );
			
			// split the type name into its parts
			String[] parts = typeName.split( "\\.");
	
			//  create folders for the package parts
			int i = 0;
			for ( ;i < parts.length - 1; i++ )
			{
				folder = folder.getFolder( parts[i] );
				if ( !folder.exists() )
					folder.create( true, false, null );
			}
			
			String fileName = parts[i] + ".java";		
			IFile file = folder.getFile( fileName );
	
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
			
			updateFileMaps( typeName, parentFile, file );
			return new FileGenerationResult(file, contentsDiffer);
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
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
	public synchronized FileGenerationResult generateFileDuringReconcile(
			ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor ) 
	{
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
			jme.printStackTrace();
		} 
		catch (CoreException ce) 
		{
			ce.printStackTrace();
		}
		return new FileGenerationResult((IFile)workingCopy.getResource(), true);
	}

	
	public synchronized boolean isGeneratedFile( IFile f )
	{
		Set<IFile> s = _derivedFile2Parents.get( f ); 
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	
	public synchronized boolean isParentFile( IFile f )
	{
		Set<IFile> s = _parent2DerivedFiles.get( f );
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	
	
	/**
	 * @param parent
	 * @return set of Strings which are the type names known to be generated 
	 * by the specified parent.
	 */
	public synchronized Set<String> getGeneratedTypesForParent( IFile parent )
	{
		Set<String> s = _parent2TypeNames.get( parent );
		if ( s == null )
			s = Collections.emptySet();
		return s;
	}
	
	/**
	 * 
	 * @param parent
	 * @return Set of IFile instances that are the files known to be generated
	 * by this parent
	 */
	public synchronized Set<IFile> getGeneratedFilesForParent( IFile parent )
	{
		Set<IFile> s = _parent2DerivedFiles.get( parent ); 
		if (s == null )
			s = Collections.emptySet();
		return s;
	}
	
	public synchronized void discardGeneratedWorkingCopy( String typeName, ICompilationUnit parentCompilationUnit )
		throws JavaModelException
	{
		discardGeneratedWorkingCopy(  typeName,  parentCompilationUnit, true );
	}
	
	private void discardGeneratedWorkingCopy( String typeName, ICompilationUnit parentCompilationUnit, boolean deleteFromParent2TypeNames )
		throws JavaModelException
	{
		if ( deleteFromParent2TypeNames )
		{
			Set<String> typeNames = _parent2TypeNames.get( parentCompilationUnit.getResource() );
			
			if ( typeNames == null ) throw new RuntimeException( "Unexpected null entry in _parent2TypeNames map.");
			if ( ! typeNames.contains( typeName )) throw new RuntimeException ("type names set didn't contain expected value");
			
			typeNames.remove( typeName );
		}
	
		Set<ICompilationUnit> parents = _typeName2Parents.get( typeName );

		// TODO:  change these to assertions
		if ( parents == null ) throw new RuntimeException( "parents == null and it shouldnt");
		if ( ! parents.contains( parentCompilationUnit )) throw new RuntimeException("parents set should contain parentCompilationUnit");
		parents.remove( parentCompilationUnit );
		
		if ( parents.size() == 0 )
		{
			ICompilationUnit cu = _typeName2WorkingCopy.get( typeName );

			if ( cu == null ) throw new RuntimeException( "compilation unit is null and it shouldn't be");
			
			_typeName2WorkingCopy.remove( typeName );
			cu.discardWorkingCopy();
		}
	}
	
	public synchronized void parentWorkingCopyDiscarded( ICompilationUnit parentCompilationUnit )
		throws JavaModelException
	{
		Set<String> typeNames = _parent2TypeNames.get( parentCompilationUnit.getResource() );
		if ( typeNames == null || typeNames.size() == 0 )
			return;
		
		Iterator<String> it = typeNames.iterator();
		while ( it.hasNext() )
		{
			String typeName = it.next();
			it.remove();
			discardGeneratedWorkingCopy( typeName, parentCompilationUnit, false );
		}
	}
	
	public synchronized void parentFileDeleted( IFile parent, IProgressMonitor monitor ) 
		throws CoreException
	{
		Set<IFile> derivedFiles = _parent2DerivedFiles.get( parent );
		
		Iterator<IFile> it = derivedFiles.iterator(); 
		while ( it.hasNext() )
		{
			IFile generatedFile = it.next();
			it.remove();
			deleteGeneratedFile( generatedFile, parent, monitor, false );
		}
	}

	public synchronized boolean deleteGeneratedFile(IFile fileToDelete, IFile parent, IProgressMonitor progressMonitor )
		throws CoreException
	{
		return deleteGeneratedFile( fileToDelete, parent, progressMonitor, true );
	}
	
	private boolean deleteGeneratedFile(IFile fileToDelete, IFile parent, IProgressMonitor progressMonitor, boolean deleteFromParent2DerivedFiles ) 
		throws CoreException
	{
		// update _parents2DerivedFiles map
		if ( deleteFromParent2DerivedFiles )
		{
			Set<IFile> derivedFiles = _parent2DerivedFiles.get( parent );

			// assertions
			if ( derivedFiles == null ) throw new RuntimeException( "derivedFiles is null and it shouldn't be");
			if ( ! derivedFiles.contains( fileToDelete )) throw new RuntimeException( "derivedFiles does not contain fileToDelete");
		
			derivedFiles.remove( fileToDelete );
		}
		
		// update _derivedFile2Parents map and delete file if it has no other parents
		Set<IFile> parents = _derivedFile2Parents.get( fileToDelete );
		
		// assertions
		if( parents == null ) throw new RuntimeException( " parents is null and it shouldn't be" );
		if( ! parents.contains( parent )) throw new RuntimeException( "parents set does not contain parent" );
		
		parents.remove( parent );
		
		boolean deleted = false;
		if ( parents.size() == 0 )
		{
			fileToDelete.delete( true, true, progressMonitor );
			deleted = true;
		}
		return deleted;
	}

	public synchronized void generatedFileDeleted( IFile deletedFile,  IProgressMonitor progressMonitor )
	{
		Set<IFile> parents = _derivedFile2Parents.get( deletedFile );
		if ( parents == null || parents.isEmpty() )
			return;
		
		String typeName = getTypeNameForDerivedFile( deletedFile );
		
		Iterator<IFile> it = parents.iterator();
		while ( it.hasNext() )
		{
			IFile parent = it.next();
			Set<IFile> s = _parent2DerivedFiles.get( parent );
			s.remove( deletedFile );
			
			Set<String> types = _parent2TypeNames.get( parent );
			types.remove( typeName );
		}
		
		_derivedFile2Parents.remove( deletedFile );
		
		_typeName2Parents.remove( typeName );
		
		_typeName2WorkingCopy.remove( typeName );
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
	
	//
	//  check cache to see if we already have a working copy
	//
	private ICompilationUnit getCachedWorkingCopy( ICompilationUnit parentCompilationUnit, String typeName )
	{
		ICompilationUnit workingCopy = (ICompilationUnit) _typeName2WorkingCopy.get( typeName );
		if ( workingCopy != null )
			updateMaps( typeName, parentCompilationUnit, workingCopy );

		return workingCopy;
	}
	
	private ICompilationUnit createNewWorkingCopy(ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor)
		throws CoreException, JavaModelException
	{	
		IProject project = parentCompilationUnit.getResource().getProject();
		JavaProject jp = (JavaProject) parentCompilationUnit.getJavaProject();

		//
		// create folder for generated source files
		//
		IFolder folder = project.getFolder( GENERATED_SOURCE_FOLDER_NAME );
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		if (!folder.exists())
			folder.create(true, true, null);
		
		//
		// make sure __generated_src dir is on the cp if not already
		//
		updateProjectClasspath( jp, folder, progressMonitor );

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
		updateMaps( typeName, parentCompilationUnit, workingCopy );
		
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
	
	private void updateMaps( String typeName, ICompilationUnit parentCompilationUnit, ICompilationUnit workingCopy )
	{
		IFile parentFile = (IFile) parentCompilationUnit.getResource();
		IFile generatedFile = (IFile) workingCopy.getResource();
		updateFileMaps( typeName, parentFile, generatedFile );

		// type name -> set of parent compilation unit
		Set<ICompilationUnit> s = _typeName2Parents.get( typeName );
		if ( s == null )
		{
			s = new HashSet();
			_typeName2Parents.put( typeName, s );
		}
		s.add( parentCompilationUnit );
		
		// type name -> working copy
		ICompilationUnit cu = (ICompilationUnit)_typeName2WorkingCopy.get( typeName );
		if ( cu != null )
		{
			//assert( cu.equals( workingCopy ) ) : "unexpected different instances of working copy for the same type";
			if ( !cu.equals(workingCopy) ) throw new RuntimeException( "unexpected different instances of working copy for the same type" );
		}
		else
			_typeName2WorkingCopy.put( typeName, workingCopy );
	}
	
	private void updateFileMaps( String typeName, IFile parentFile, IFile generatedFile )
	{
		// parent IFile -> set of generated type name
		Set<String> stringSet = _parent2TypeNames.get( parentFile );
		if ( stringSet == null )
		{
			stringSet = new HashSet<String>();
			_parent2TypeNames.put( parentFile, stringSet );
		}
		stringSet.add( typeName );
		
		
		// add parent file -> set of derived files
		Set<IFile> fileSet = _parent2DerivedFiles.get( parentFile );
		if ( fileSet == null )
		{
			fileSet = new HashSet();
		 	_parent2DerivedFiles.put( parentFile, fileSet );
		}
		fileSet.add( generatedFile );


		// add derived file -> set of parent files
		fileSet = _derivedFile2Parents.get( generatedFile );
		if ( fileSet == null )
		{ 
			fileSet = new HashSet();
			_derivedFile2Parents.put( generatedFile, fileSet );
		}
		fileSet.add( parentFile );
	}
	
	private void updateProjectClasspath( JavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IClasspathEntry generatedSourceClasspathEntry = 
			JavaCore.newSourceEntry(folder.getFullPath());
		boolean found = false;
		for (int i = 0; i < cp.length; i++) 
		{
			if (cp[i].equals(generatedSourceClasspathEntry)) 
			{
				found = true;
				break;
			}
		}
		if (!found) 
		{
			IClasspathEntry[] newCp = new IClasspathEntry[cp.length + 1];
			System.arraycopy(cp, 0, newCp, 0, cp.length);
			newCp[newCp.length - 1] = generatedSourceClasspathEntry;
			jp.setRawClasspath(newCp, progressMonitor );
		}
	}
	
	public synchronized void projectClosed()
	{
		// discard all working copies
		Collection<ICompilationUnit> workingCopies = _typeName2WorkingCopy.values();
		for ( ICompilationUnit wc : workingCopies )
		{
			try 
			{
				wc.discardWorkingCopy();
			}
			catch ( JavaModelException jme )
			{
				jme.printStackTrace();
			}
		}

		// clear out the working copy maps
		_typeName2Parents.clear();
		_typeName2WorkingCopy.clear();
	}
	
	public synchronized void projectClean( boolean deleteFiles )
	{
		projectClosed();
		
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
					ce.printStackTrace();
				}
			}
		}
	
		// clear out all the file maps
		_parent2DerivedFiles.clear();
		_derivedFile2Parents.clear();
		_parent2TypeNames.clear();
	}
	
	public synchronized void projectDeleted()
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
	 * map from IFile of parent file to Set <IFile>of derived files
	 */
	private Map<IFile, Set<IFile>> _parent2DerivedFiles = new HashMap();

	/**
	 * map from IFile of dervied file to Set <IFile>of parent files
	 */
	private Map<IFile, Set<IFile>> _derivedFile2Parents = new HashMap();

	/**
	 * map from IFile of parent working copy to Set
	 * <String> of type names generated by that file
	 * 
	 * Map<IFile, Set<String>>
	 */
	private Map<IFile, Set<String>> _parent2TypeNames = new HashMap();

	/**
	 * map from typename of generated file to Set<ICompilationUnit>of parent 
	 * working copies
	 * 
	 * Map<String, Set<ICompilationUnit>>
	 */
	private Map<String, Set<ICompilationUnit>> _typeName2Parents = new HashMap();
	
	/**
	 * Map from type name to the working copy in memory of that type name
	 * 
	 * Map<String, ICompilationUnit>
	 */
	private Map<String, ICompilationUnit> _typeName2WorkingCopy = new HashMap();	

	
	private static boolean _initialized = false;
	
	private static final String GENERATED_SOURCE_FOLDER_NAME = "__generated_src";

}
