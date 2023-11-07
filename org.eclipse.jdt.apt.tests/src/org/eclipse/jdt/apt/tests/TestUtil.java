/*******************************************************************************
 * Copyright (c) 2000, 2010 BEA Systems, Inc, IBM Corporation, and others
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.tests.util.ZipEntryStorageException;

public class TestUtil
{

	private static final String BIN_EXT = "/bin-ext"; //$NON-NLS-1$
	private static final String BIN_ANNOTATIONS = "/bin-annotations"; //$NON-NLS-1$

	private static File ANNO_JAR = null;

	/**
	 * Returns the annotation jar, creating it if it hasn't already been created.
	 * @return the java.io.File of the jar that was created.
	 */
	public static File createAndAddAnnotationJar( IJavaProject project )
		throws IOException, JavaModelException
	{
		if (ANNO_JAR == null) {
			// The jar file will be created in the state location, e.g., .metadata/
			IPath statePath = AptPlugin.getPlugin().getStateLocation();
			IPath jarPath = statePath.append("org.eclipse.jdt.apt.tests.TestUtil.jar");
			ANNO_JAR = new File(jarPath.toOSString());
			String classesJarPath = ANNO_JAR.getAbsolutePath();

			if (null != getFileInPlugin( AptTestsPlugin.getDefault(), new Path(BIN_ANNOTATIONS) )) {
				// We're in a dev environment, where we jar up the classes from the plugin project
				FileFilter filter = new PackageFileFilter(
						ANNOTATIONS_PKG, getAnnotationsClassesDir());
				Map<File, FileFilter> files = Collections.singletonMap(
						new File(getAnnotationsClassesDir()), filter);
				zip( classesJarPath, files );
			}
			else {
				// We're in a releng environment, where we copy the already-built jar
				File aptJarFile = getFileInPlugin( AptTestsPlugin.getDefault(), new Path("/apt.jar"));
				if(null == aptJarFile) {
					throw new FileNotFoundException("Could not find apt.jar file in org.eclipse.jdt.apt.tests plugin");
				}
				moveFile(aptJarFile, classesJarPath);
			}

			ANNO_JAR.deleteOnExit();
		}

		addLibraryEntry( project, new Path(ANNO_JAR.getAbsolutePath()), null /*srcAttachmentPath*/,
			null /*srcAttachmentPathRoot*/, true );

		return ANNO_JAR;
	}

	/**
	 * Looks for the apt.jar that is defined in the build.properties
	 * and available when the plugin is built deployed.
	 * (currently when the plugin is built using releng the /bin directory classes are not available)
	 *
	 * else it creates an annotation jar containing annotations and processors
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
		// create temporary file
		File jarFile = File.createTempFile("org.eclipse.jdt.apt.tests.TestUtil", ".jar");  //$NON-NLS-1$//$NON-NLS-2$
		String classesJarPath = jarFile.getAbsolutePath();

		File extBinDir = getFileInPlugin( AptTestsPlugin.getDefault(), new Path(BIN_EXT));
		if(null != extBinDir) {

			//create zip file in temp file location
			FileFilter classFilter = new PackageFileFilter(
					EXTANNOTATIONS_PKG, getPluginExtClassesDir());
			FileFilter manifestFilter = new PackageFileFilter(
					"META-INF", getPluginExtSrcDir()); //$NON-NLS-1$
			Map<File, FileFilter> files = new HashMap<File, FileFilter>(2);
			files.put(new File( getPluginExtClassesDir() ), classFilter);
			files.put(new File( getPluginExtSrcDir() ), manifestFilter);
			zip( classesJarPath, files );

		} else {

			File extJarFile = getFileInPlugin( AptTestsPlugin.getDefault(), new Path("/aptext.jar"));
			if(null != extJarFile) {

				// move extapt.jar to classesJarPath file
				moveFile(extJarFile, classesJarPath);

			} else {

				throw new FileNotFoundException("Could not find aptext.jar file in org.eclipse.jdt.apt.tests plugin");
			}

		}

		if(project != null) {
			addLibraryEntry( project, new Path(classesJarPath), null /*srcAttachmentPath*/,
					null /*srcAttachmentPathRoot*/, true );
		}

		// This file will be locked until GC takes care of unloading the
		// annotation processor classes, so we can't delete it ourselves.
		jarFile.deleteOnExit();
		return jarFile;

	}

	/**
	 * In automated tests, newly created resources are often locked by the
	 * Java Indexer and cannot be deleted right away.  The methods in
	 * org.eclipse.jdt.core.tests.util.Util work around this by catching
	 * and retrying until success.  This is a convenience method to fill a
	 * hole in the Util API.
	 * @return an IStatus that describes if the deletion was successful
	 */
	public static IStatus deleteFile(IPath path) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return Util.delete(file);
	}

	/**
     * Set the autobuild to the value of the parameter and
     * return the old one.  This is a workaround for a synchronization
     * problem: thread A creates a project, thus spawning thread B to
     * do an autobuild.  Thread A goes on to configure the project's
     * classpath; at the same time, thread B calls APT, which configures
     * the project's classpath.  Access to the classpath is not
     * synchronized, so there's a race for which thread's modification
     * wins.  We work around this by disabling autobuild.
     *
     * @param state the value to be set for autobuilding.
     * @return the old value of the autobuild state
     */
    public static boolean enableAutoBuild(boolean state) throws CoreException {
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc= workspace.getDescription();
        boolean isAutoBuilding= desc.isAutoBuilding();
        if (isAutoBuilding != state) {
            desc.setAutoBuilding(state);
            workspace.setDescription(desc);
            waitForBuildEvents();
        }
        return isAutoBuilding;
    }

	public static void waitForBuildEvents() {
		try {
			Thread.sleep(50);
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (OperationCanceledException | InterruptedException e) {
			// ignore
		}
	}

	public static IPath getProjectPath( IJavaProject project )
	{
		return project.getResource().getLocation();
	}

	public static String getAnnotationsClassesDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( BIN_ANNOTATIONS ) ) //$NON-NLS-1$
			.getAbsolutePath();
	}

	public static String getPluginExtClassesDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( BIN_EXT ) ) //$NON-NLS-1$
			.getAbsolutePath();
	}

	public static String getPluginExtSrcDir()
	{
		return getFileInPlugin( AptTestsPlugin.getDefault(), new Path( "/src-ext" ) ) //$NON-NLS-1$
			.getAbsolutePath();
	}

	/**
	 *
	 * @param plugin The Plugin to get file from
	 * @param path The path to the file in the Plugin
	 * @return File object if found, null otherwise
	 */
	public static java.io.File getFileInPlugin(Plugin plugin, IPath path)
	{
		try
		{
			URL installURL = plugin.getBundle().getEntry( path.toString() );
			if(null == installURL)
				return null; // File Not found

			URL localURL = FileLocator.toFileURL( installURL );
			return new java.io.File( localURL.getFile() );
		}
		catch( IOException e )
		{
			return null;
		}
	}

	/**
	 * Could use File.renameTo(File) but it's platform dependant.
	 *
	 * @param from - The file to move
	 * @param path - The path to move it to
	 */
	public static void moveFile(File from , String toPath)
		throws FileNotFoundException, IOException {

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try
		{
			fis = new FileInputStream( from );
			fos = new FileOutputStream(new File(toPath));
			int b;
			while ( ( b = fis.read() ) != -1)
				fos.write( b );
		}
		finally
		{
			if ( fis != null ) fis.close();
			if ( fos != null ) fos.close();
		}
	}

	/**
	 * Create a zip file and add contents.
	 * @param zipPath the zip file
	 * @param input a map of root directories and corresponding filters.  Each
	 * root directory will be searched, and any files that pass the filter will
	 * be added to the zip file.
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
	private static File getZipEntryFile(File destDir, ZipEntry e, String canonicalDestDirPath) throws IOException {
		  String result = e.getName();
		  File destfile = new File(destDir, result);
		  String canonicalDestFile = destfile.getCanonicalPath();
		  if (!canonicalDestFile.startsWith(canonicalDestDirPath + File.separator)) {
			  throw new ZipEntryStorageException("Entry is outside of the target dir: " + e.getName());
		  }
		  return destfile;
	}

	public static void unzip (File srcZip, File destDir) throws IOException {
		ZipFile zf = new ZipFile(srcZip);
		String canonicalDestDirPath = destDir.getCanonicalPath();
		for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
			ZipEntry entry = entries.nextElement();
			File dest = getZipEntryFile(destDir, entry, canonicalDestDirPath);
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
		zf.close(); // Will do
	}

	public static void unzip (ZipInputStream srcZip, File destDir) throws IOException {
		ZipEntry entry;
		String canonicalDestDirPath = destDir.getCanonicalPath();
		while ((entry = srcZip.getNextEntry()) != null) {
			File dest = getZipEntryFile(destDir, entry, canonicalDestDirPath);
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
		IClasspathEntry newPathEntry = JavaCore.newLibraryEntry(
				path,
				srcAttachmentPath,
				srcAttachmentPathRoot,
				exported);
		for(int i = 0; i < length; i++) {
			//check for duplicates (Causes JavaModelException) - return if path already exists
			if(newPathEntry.equals(entries[i]))
				return;
		}
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length + 1], 1, length);
		entries[0] = newPathEntry;
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
			_packageParts = packageSubset.split("\\."); //$NON-NLS-1$
			_binDir = new Path(binDir);
		}

		@Override
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
		"org.eclipse.jdt.apt.tests.annotations"; //$NON-NLS-1$

	public static final String EXTANNOTATIONS_PKG =
		"org.eclipse.jdt.apt.tests.external.annotations"; //$NON-NLS-1$

}
