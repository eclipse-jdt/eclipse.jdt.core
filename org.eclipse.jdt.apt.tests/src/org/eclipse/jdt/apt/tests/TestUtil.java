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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class TestUtil
{

	/**
	 * creates the annotation jar.  
	 * @return the java.io.File of the jar that was created.
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
		FileFilter filter = new PackageFileFilter(
				ANNOTATIONS_PKG, getPluginClassesDir());
		Map<File, FileFilter> files = Collections.singletonMap(
				new File(getPluginClassesDir()), filter);
		zip( classesJarPath, files );
		addLibraryEntry( project, new Path(classesJarPath), null /*srcAttachmentPath*/, 
			null /*srcAttachmentPathRoot*/, true );
		return new File(classesJarPath);
	}
	
	/**
	 * Creates an annotation jar containing annotations and processors
	 * from the "external.annotations" package, and adds it to the project.
	 * Classes will be found under [project]/binext, and manifest will be
	 * drawn from [project]/srcext/META-INF.  
	 * This jar is meant to represent an annotation jar file not 
	 * wrapped within a plugin.  Note that adding a jar to a project makes
	 * its classes visible to the compiler but does NOT automatically cause 
	 * its annotation processors to be loaded.
	 * @return the java.io.File of the jar that was created.
	 */
	public static File createAndAddExternalAnnotationJar( 
			IJavaProject project  )
		throws IOException, JavaModelException
	{
		IPath projectPath = getProjectPath( project );
		File jarFile = new File( projectPath.toFile(), "ClassesExt.jar" );
		String classesJarPath = jarFile.getAbsolutePath();
		FileFilter classFilter = new PackageFileFilter(
				EXTANNOTATIONS_PKG, getPluginExtClassesDir());
		FileFilter manifestFilter = new PackageFileFilter(
				"META-INF", getPluginExtSrcDir());
		Map<File, FileFilter> files = new HashMap<File, FileFilter>(2);
		files.put(new File( getPluginExtClassesDir() ), classFilter);
		files.put(new File( getPluginExtSrcDir() ), manifestFilter);
		zip( classesJarPath, files );
		addLibraryEntry( project, new Path(classesJarPath), null /*srcAttachmentPath*/, 
			null /*srcAttachmentPathRoot*/, true );
		return new File(classesJarPath);
	}
	
	public static IPath getProjectPath( IJavaProject project )
	{
		return project.getResource().getLocation();
	}
	
	public static String getPluginClassesDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( "/bin" ) )
			.getAbsolutePath();
	}

	public static String getPluginExtClassesDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( "/binext" ) )
			.getAbsolutePath();
	}

	public static String getPluginExtSrcDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( "/srcext" ) )
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

	/**
	 * Create a zip file and add contents.
	 * @param zipPath the zip file
	 * @param input a map of root directories and corresponding filters.  Each
	 * root directory will be searched, and any files that pass the filter will
	 * be added to the zip file.
	 * @throws IOException
	 */
	public static void zip(String zipPath, Map<File, FileFilter> input)
		throws IOException
	{
		ZipOutputStream zip = null;
		try
		{
			zip = new ZipOutputStream( new FileOutputStream( zipPath ) );
			// +1 for last slash
			for (Map.Entry<File, FileFilter> e : input.entrySet()) {
				zip( zip, e.getKey(), e.getKey().getPath().length() + 1, e.getValue() );
			}
		}
		finally
		{
			if( zip != null )
			{
				zip.close();
			}
		}
	}
	
	private static void zip(ZipOutputStream zip, File dir, int rootPathLength,
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
						zip( zip, file, rootPathLength, filter );
					}
					else
					{
						String path = file.getPath();
						path = path.substring( rootPathLength );
						ZipEntry entry = new ZipEntry( path.replace( '\\', '/' ) );
						zip.putNextEntry( entry );
						zip.write( getBytesFromFile( file ) );
						zip.closeEntry();
					}
				}
			}
		}
	}
	
	private static byte[] getBytesFromFile( File f )
		throws IOException
	{
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		byte[] rtrn = new byte[0]; 
		try
		{
			fis = new FileInputStream( f );
			baos = new ByteArrayOutputStream();
			int b;
			while ( ( b = fis.read() ) != -1)
				baos.write( b );
			rtrn = baos.toByteArray();
		}
		finally
		{
			if ( fis != null ) fis.close();
			if ( baos != null ) baos.close();
		}
		return rtrn;
	
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
	
	

	public static void addLibraryEntry(IJavaProject project, IPath path, IPath srcAttachmentPath, IPath srcAttachmentPathRoot, boolean exported) throws JavaModelException{
		IClasspathEntry[] entries = project.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 1, length);
		entries[0] = JavaCore.newLibraryEntry(
			path, 
			srcAttachmentPath, 
			srcAttachmentPathRoot, 
			exported);
		project.setRawClasspath(entries, null);
	}
	
	
	private static class PackageFileFilter implements FileFilter {
		private final String[] _packageParts;
		private final Path _binDir;
		
		/**
		 * Select only those files under a certain package.
		 * @param packageSubset a partial package name, such as 
		 * "org.eclipse.jdt.apt.tests.annotations".
		 * @param binDir the absolute path of the directory 
		 * in which the compiled classes are to be found.
		 */
		public PackageFileFilter(String packageSubset, String binDir) {
			_packageParts = packageSubset.split("\\.");
			_binDir = new Path(binDir);
		}
		
		public boolean accept(File pathname)
		{
			IPath f = new Path( pathname.getAbsolutePath() );

			int nsegments = f.matchingFirstSegments( _binDir );
			boolean ok = true;
			int min = Math.min( f.segmentCount() - nsegments,
					_packageParts.length );
			for( int i = nsegments, j = 0; j < min; i++, j++ )
			{
				if( !f.segment( i ).equals( _packageParts[j] ) )
				{
					ok = false;
					break;
				}
			}
			return ok;
		}
	}
	
	public static final String ANNOTATIONS_PKG = 
		"org.eclipse.jdt.apt.tests.annotations";

	public static final String EXTANNOTATIONS_PKG = 
		"org.eclipse.jdt.apt.tests.external.annotations";

}
