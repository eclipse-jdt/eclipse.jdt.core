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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
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

	private static void init()
	{
		_initialized = true;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener( new ResourceChangedListener() );
	}
	
	public static GeneratedFileManager getGeneratedFileManager() 
	{
		if ( ! _initialized ) 
			init();
		return _generatedFileManagerInstance;
	}

	/**
	 * 
	 * @param parentFile
	 * @param typeName
	 * @param contents
	 * @param progressMonitor
	 * @param charsetName
	 * @return - the newly created IFile
	 * @throws CoreException
	 * @throws UnsupportedEncodingException
	 */
	public IFile generateFileDuringBuild(
			IFile parentFile,
			IProject project,
			String typeName, 
			String contents, 
			IProgressMonitor progressMonitor,
			String charsetName ) 
		throws CoreException, UnsupportedEncodingException
	{
		try
		{		

		// create folder for generated source files
		IFolder folder = project.getFolder( GENERATED_SOURCE_FOLDER_NAME );
		if (!folder.exists())
			folder.create(true, false, null);

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
		
		if ( !file.exists() )
		{
			file.create( is, true, progressMonitor );
		}
		else
		{
			makeReadOnly( file, false );
			file.setContents( is, true, true, progressMonitor );
		}
		
		file.setDerived( true );
		
		makeReadOnly( file, true );
		
		updateFileMaps( typeName, parentFile, file );
		return file;
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}
		
		return null;
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
	public ICompilationUnit generateFileDuringReconcile(
			ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor ) 
	{
		ICompilationUnit workingCopy = null;
		try 
		{
			//
			// get working copy (either from cache or create a new one)
			//
			workingCopy = getWorkingCopy( 
				parentCompilationUnit,  typeName, contents,  
				workingCopyOwner, problemRequestor,  progressMonitor);
			
			//
			//  Update working copy's buffer with the contents of the type 
			// 
			updateWorkingCopy( contents, workingCopy, workingCopyOwner, progressMonitor );
			
			return workingCopy;
		
		} 
		catch (JavaModelException jme) 
		{
			jme.printStackTrace();
		} 
		catch (CoreException ce) 
		{
			ce.printStackTrace();
		}
		return workingCopy;
	}

	
	public boolean isGeneratedFile( IFile f )
	{
		Set s = (Set)_derivedFile2Parents.get( f ); 
		if ( s == null || s.isEmpty() )
			return false;
		else
			return true;
	}
	
	public boolean isParentFile( IFile f )
	{
		Set s = (Set)_parent2DerivedFiles.get( f );
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
	public Set getGeneratedTypesForParent( IFile parent )
	{
		return (Set)_parent2TypeNames.get( parent );
	}
	
	/**
	 * 
	 * @param parent
	 * @return Set of IFile instances that are the files known to be generated
	 * by this parent
	 */
	public Set getGeneratedFilesForParent( IFile parent )
	{
		return (Set)_parent2DerivedFiles.get( parent );
	}
	
	public void discardGeneratedWorkingCopy( String typeName, ICompilationUnit parentCompilationUnit )
		throws JavaModelException
	{
		discardGeneratedWorkingCopy(  typeName,  parentCompilationUnit, true );
	}
	
	private void discardGeneratedWorkingCopy( String typeName, ICompilationUnit parentCompilationUnit, boolean deleteFromParent2TypeNames )
		throws JavaModelException
	{
		if ( deleteFromParent2TypeNames )
		{
			Set typeNames = (Set)_parent2TypeNames.get( parentCompilationUnit.getResource() );
			
			if ( typeNames == null ) throw new RuntimeException( "Unexpected null entry in _parent2TypeNames map.");
			if ( ! typeNames.contains( typeName )) throw new RuntimeException ("type names set didn't contain expected value");
			
			typeNames.remove( typeName );
		}
	
		Set parents = (Set) _typeName2Parents.get( typeName );

		// TODO:  change these to assertions
		if ( parents == null ) throw new RuntimeException( "parents == null and it shouldnt");
		if ( ! parents.contains( parentCompilationUnit )) throw new RuntimeException("parents set should contain parentCompilationUnit");
		parents.remove( parentCompilationUnit );
		
		if ( parents.size() == 0 )
		{
			ICompilationUnit cu = (ICompilationUnit)_typeName2WorkingCopy.get( typeName );

			if ( cu == null ) throw new RuntimeException( "compilation unit is null and it shouldn't be");
			
			_typeName2WorkingCopy.remove( typeName );
			cu.discardWorkingCopy();
		}
	}
	
	public void parentWorkingCopyDiscarded( ICompilationUnit parentCompilationUnit )
		throws JavaModelException
	{
		Set typeNames = (Set)_parent2TypeNames.get( parentCompilationUnit.getResource() );
		if ( typeNames == null || typeNames.size() == 0 )
			return;
		
		Iterator it = typeNames.iterator();
		while ( it.hasNext() )
		{
			String typeName = (String) it.next();
			it.remove();
			discardGeneratedWorkingCopy( typeName, parentCompilationUnit, false );
		}
	}
	
	public void parentFileDeleted( IFile parent, IProgressMonitor monitor ) 
		throws CoreException
	{
		Set derivedFiles = (Set) _parent2DerivedFiles.get( parent );
		
		Iterator it = derivedFiles.iterator(); 
		while ( it.hasNext() )
		{
			IFile generatedFile = (IFile) it.next();
			it.remove();
			deleteGeneratedFile( generatedFile, parent, monitor, false );
		}
	}

	public void deleteGeneratedFile(IFile fileToDelete, IFile parent, IProgressMonitor progressMonitor )
		throws CoreException
	{
		deleteGeneratedFile( fileToDelete, parent, progressMonitor, true );
	}
	
	private void deleteGeneratedFile(IFile fileToDelete, IFile parent, IProgressMonitor progressMonitor, boolean deleteFromParent2DerivedFiles ) 
		throws CoreException
	{
		// update _parents2DerivedFiles map
		if ( deleteFromParent2DerivedFiles )
		{
			Set derivedFiles = (Set)_parent2DerivedFiles.get( parent );

			// assertions
			if ( derivedFiles == null ) throw new RuntimeException( "derivedFiles is null and it shouldn't be");
			if ( ! derivedFiles.contains( fileToDelete )) throw new RuntimeException( "derivedFiles does not contain fileToDelete");
		
			derivedFiles.remove( fileToDelete );
		}
		
		// update _derivedFile2Parents map and delete file if it has no other parents
		Set parents = (Set) _derivedFile2Parents.get( fileToDelete );
		
		// assertions
		if( parents == null ) throw new RuntimeException( " parents is null and it shouldn't be" );
		if( ! parents.contains( parent )) throw new RuntimeException( "parents set does not contain parent" );
		
		parents.remove( parent );
		if ( parents.size() == 0 )
		{
			// MRK TODO - uncomment this!!!
			/*
			fileToDelete.delete( true, true, progressMonitor );
			*/
		}
	}

	public void generatedFileDeleted( IFile deletedFile,  IProgressMonitor progressMonitor )
	{
		Set parents = (Set) _derivedFile2Parents.get( deletedFile );
		if ( parents == null || parents.isEmpty() )
			return;
		
		String typeName = getTypeNameForDerivedFile( deletedFile );
		
		Iterator it = parents.iterator();
		while ( it.hasNext() )
		{
			IFile parent = (IFile) it.next();
			Set s = (Set)_parent2DerivedFiles.get( parent );
			s.remove( deletedFile );
			
			s = (Set)_parent2TypeNames.get( parent );
			s.remove( typeName );
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
	
	private ICompilationUnit getWorkingCopy(ICompilationUnit parentCompilationUnit, String typeName,
			String contents, WorkingCopyOwner workingCopyOwner,
			IProblemRequestor problemRequestor, IProgressMonitor progressMonitor)
		throws CoreException, JavaModelException
	{
		//
		//  check cache to see if we already have a working copy
		//
		ICompilationUnit workingCopy = (ICompilationUnit) _typeName2WorkingCopy.get( typeName );
		if ( workingCopy != null )
		{
			updateMaps( typeName, parentCompilationUnit, workingCopy );
			return workingCopy;
		}
		
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
		workingCopy = cu;
		
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
	
	private void updateWorkingCopy( 
			String contents, ICompilationUnit workingCopy, 
			WorkingCopyOwner workingCopyOwner, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		//
		// TODO - reuse existing char[] if there is one?
		//
		IBuffer b = workingCopy.getBuffer();
		char[] buf = new char[contents.length()];
		contents.getChars(0, contents.length(), buf, 0);
		b.setContents(buf);
		workingCopy.reconcile(AST.JLS3, true, workingCopyOwner,
				progressMonitor);
	}
	
	private void updateMaps( String typeName, ICompilationUnit parentCompilationUnit, ICompilationUnit workingCopy )
	{
		IFile parentFile = (IFile) parentCompilationUnit.getResource();
		IFile generatedFile = (IFile) workingCopy.getResource();
		updateFileMaps( typeName, parentFile, generatedFile );

		// type name -> set of parent compilation unit
		Set s = (Set)_typeName2Parents.get( typeName );
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
		Set s = (Set) _parent2TypeNames.get( parentFile );
		if ( s == null )
		{
			s = new HashSet();
			_parent2TypeNames.put( parentFile, s );
		}
		s.add( typeName );
		
		
		// add parent file -> set of derived files
		s = (Set)_parent2DerivedFiles.get( parentFile );
		if ( s == null )
		{
		 	s = new HashSet();
		 	_parent2DerivedFiles.put( parentFile, s );
		}
		s.add( generatedFile );


		// add derived file -> set of parent files
		s = (Set) _derivedFile2Parents.get( generatedFile );
		if ( s == null )
		{ 
			s = new HashSet();
			_derivedFile2Parents.put( generatedFile, s );
		}
		s.add( parentFile );
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
	
	
	/**
	 * map from IFile of parent file to Set <IFile>of derived files
	 */
	private Map _parent2DerivedFiles = new HashMap();

	/**
	 * map from IFile of dervied file to Set <IFile>of parent files
	 */
	private Map _derivedFile2Parents = new HashMap();

	/**
	 * map from ICompilationUnit of parent working copy to Set
	 * <String> of type names generated by that file
	 * 
	 * Map<ICompilationUnit, Set<String>>
	 */
	private Map _parent2TypeNames = new HashMap();

	/**
	 * map from typename of generated file to Set<ICompilationUnit>of parent 
	 * working copies
	 * 
	 * Map<String, Set<ICompilationUnit>>
	 */
	private Map _typeName2Parents = new HashMap();
	
	/**
	 * Map from type name to the working copy in memory of that type name
	 * 
	 * Map<String, ICompilationUnit>
	 */
	private Map _typeName2WorkingCopy = new HashMap();
	

	
	/**
	 * TODO:  need to deal with synchronization on this instance.  Also, may need to break this up per project basis...
	 */
	private static GeneratedFileManager _generatedFileManagerInstance = new GeneratedFileManager();
	
	private static boolean _initialized = false;
	
	private static final String GENERATED_SOURCE_FOLDER_NAME = "__generated_src";

}
