/*******************************************************************************
 * Copyright (c) 2000, 2005 BEA Systems, Inc, IBM Corporation, and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;

public class TestUtil
{

	/**
	 * creates the annotation jar.  Returns the java.io.File of the jar that was created.
	 */
	public static File createAndAddAnnotationJar( IJavaProject project )
		throws IOException, JavaModelException
	{
		//
		//   add annotations jar as part of the project
		//
		IPath projectPath = getProjectPath( project );
		File jarFile = new File( projectPath.toFile(), "Classes.jar" );
		String classesJarPath = jarFile.getAbsolutePath();
		TestUtil.createAnnotationJar( classesJarPath );
		addLibraryEntry( project, new Path(classesJarPath), null /*srcAttachmentPath*/, 
			null /*srcAttachmentPathRoot*/, null /*accessibleFiles*/, null/*nonAccessibleFiles*/, true );
		return new File(classesJarPath);
	}
	
	public static void createAnnotationJar(String jarPath)
		throws IOException
	{
		//
		// This filter only accepts classes in the package ANNOTATIONS_PKG.
		// This way, we can jar up these files and have them available to the
		// project
		// so code can reference types in there.
		//
		FileFilter filter = new FileFilter()
		{
			public boolean accept(File pathname)
			{
				IPath f = new Path( pathname.getAbsolutePath() );

				int nsegments = f.matchingFirstSegments( new Path(
					getPluginClassesDir() ) );
				boolean ok = true;
				int min = Math.min( f.segmentCount() - nsegments,
					ANNOTATIONS_PKG_PARTS.length );
				for( int i = nsegments, j = 0; j < min; i++, j++ )
				{
					if( !f.segment( i ).equals( ANNOTATIONS_PKG_PARTS[j] ) )
					{
						ok = false;
						break;
					}
				}
				return ok;
			}
		};
		zip( new File( getPluginClassesDir() ), jarPath, filter );
	}

	public static IPath getProjectPath( IJavaProject project )
	{
		Workspace workspace = (Workspace)ResourcesPlugin.getWorkspace();
		FileSystemResourceManager fileSystemMgr = workspace.getFileSystemManager();
		IPath p = fileSystemMgr.locationFor( project.getResource() );
		return p;
	}
	
	
	public static String getPluginClassesDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( "/bin" ) )
			.getAbsolutePath();
	}

	public static java.io.File getFileInPlugin(Plugin plugin, IPath path)
	{
		try
		{
			URL installURL = plugin.getBundle().getEntry( path.toString() );
			URL localURL = Platform.asLocalURL( installURL );
			return new java.io.File( localURL.getFile() );
		}
		catch( IOException e )
		{
			return null;
		}
	}

	public static void zip(File rootDir, String zipPath, FileFilter filter)
		throws IOException
	{
		ZipOutputStream zip = null;
		try
		{
			zip = new ZipOutputStream( new FileOutputStream( zipPath ) );
			// +1 for last slash
			zip( rootDir, zip, rootDir.getPath().length() + 1, filter ); 
		}
		finally
		{
			if( zip != null )
			{
				zip.close();
			}
		}
	}

	private static void zip(File dir, ZipOutputStream zip, int rootPathLength,
		FileFilter filter) throws IOException
	{
		String[] list = dir.list();
		if( list != null )
		{
			for( int i = 0, length = list.length; i < length; i++ )
			{
				String name = list[i];
				File file = new File( dir, name );
				if( filter == null || filter.accept( file ) )
				{
					if( file.isDirectory() )
					{
						zip( file, zip, rootPathLength, filter );
					}
					else
					{
						String path = file.getPath();
						path = path.substring( rootPathLength );
						ZipEntry entry = new ZipEntry( path.replace( '\\', '/' ) );
						zip.putNextEntry( entry );
						zip.write( org.eclipse.jdt.internal.compiler.util.Util
							.getFileByteContent( file ) );
						zip.closeEntry();
					}
				}
			}
		}
	}
	
	public static void unzip (File srcZip, File destDir) throws IOException {
		ZipFile zf = new ZipFile(srcZip);
		for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			File dest = new File(destDir, name);
			if (entry.isDirectory()) {
				FileSystemUtil.mkdirs(dest);
			}
			else {
				File parent = dest.getParentFile();
				FileSystemUtil.mkdirs(parent);
				InputStream from = null;
	            OutputStream to = null;
	            try {
	                from = zf.getInputStream(entry);
	                to = new FileOutputStream(dest);
	                byte[] buffer = new byte[4096];
	                int bytesRead;
	                while ((bytesRead = from.read(buffer)) != -1) {
	                    to.write(buffer, 0, bytesRead);
	                }
	            }
	            finally {
	                if (from != null) try {from.close();} catch (IOException ioe){}
	                if (to != null) try {to.close();} catch (IOException ioe) {}
	            }
			}
		}
	}
	
	public static void unzip (ZipInputStream srcZip, File destDir) throws IOException {
		ZipEntry entry;
		while ((entry = srcZip.getNextEntry()) != null) {
			String name = entry.getName();
			File dest = new File(destDir, name);
			if (entry.isDirectory()) {
				FileSystemUtil.mkdirs(dest);
			}
			else {
				File parent = dest.getParentFile();
				FileSystemUtil.mkdirs(parent);
	            OutputStream to = null;
	            try {
	                to = new FileOutputStream(dest);
	                byte[] buffer = new byte[4096];
	                int bytesRead;
	                while ((bytesRead = srcZip.read(buffer)) != -1) {
	                    to.write(buffer, 0, bytesRead);
	                }
	            }
	            finally {
                    srcZip.closeEntry();
	                if (to != null) try {to.close();} catch (IOException ioe) {}
	            }
			}
		}
	}
	
	

	public static void addLibraryEntry(IJavaProject project, IPath path, IPath srcAttachmentPath, IPath srcAttachmentPathRoot, IPath[] accessibleFiles, IPath[] nonAccessibleFiles, boolean exported) throws JavaModelException{
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 1, length);
		entries[0] = JavaCore.newLibraryEntry(
			path, 
			srcAttachmentPath, 
			srcAttachmentPathRoot, 
			ClasspathEntry.getAccessRules(accessibleFiles, nonAccessibleFiles), 
			new IClasspathAttribute[0], 
			exported);
		project.setRawClasspath(entries, null);
	}
	
	
	public static final String		ANNOTATIONS_PKG			= "org.eclipse.jdt.apt.tests.annotations";

	public static final String[]	ANNOTATIONS_PKG_PARTS	= ANNOTATIONS_PKG
																.split( "\\." );

}
