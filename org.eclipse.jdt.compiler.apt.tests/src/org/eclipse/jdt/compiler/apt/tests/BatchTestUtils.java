/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;

import junit.framework.Assert;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

/**
 * Helper class to support compilation and results checking for tests running in batch mode.
 * @since 3.3
 */
public class BatchTestUtils {
	private static final String RESOURCES_DIR = "resources";
	// relative to plugin directory
	private static final String PROCESSOR_JAR_NAME = "lib/apttestprocessors.jar";
	private static String _processorJarPath;

	// locations to copy and generate files
	private static String _tmpFolder;

	private static JavaCompiler _eclipseCompiler;

	private static String _tmpSrcFolderName;
	private static File _tmpSrcDir;
	private static String _tmpBinFolderName;
	private static File _tmpBinDir;
	private static String _tmpGenFolderName;
	private static File _tmpGenDir;

	/**
	 * Create a class that contains an annotation that generates another class,
	 * and compile it.  Verify that generation and compilation succeeded.
	 */
	public static void compileOneClass(JavaCompiler compiler, List<String> options, File inputFile) {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		options.add("-d");
		options.add(_tmpBinFolderName);
		options.add("-s");
		options.add(_tmpGenFolderName);
		options.add("-cp");
		options.add(_tmpSrcFolderName + File.pathSeparator + _tmpGenFolderName + File.pathSeparator + _processorJarPath);
		options.add("-processorpath");
		options.add(_processorJarPath);
		options.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, manager, null, options, null, units);
		Boolean result = task.call();

		if (!result.booleanValue()) {
			String errorOutput = stringWriter.getBuffer().toString();
			System.err.println("Compilation failed: " + errorOutput);
	 		Assert.assertTrue("Compilation failed : " + errorOutput, false);
		}
	}

	public static void compileTree(JavaCompiler compiler, List<String> options, File targetFolder) {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		findFilesUnder(targetFolder, files);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		options.add("-d");
		options.add(_tmpBinFolderName);
		options.add("-s");
		options.add(_tmpGenFolderName);
		options.add("-cp");
		options.add(_tmpSrcFolderName + File.pathSeparator + _tmpGenFolderName + File.pathSeparator + _processorJarPath);
		options.add("-processorpath");
		options.add(_processorJarPath);
		options.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, manager, null, options, null, units);
		Boolean result = task.call();

		if (!result.booleanValue()) {
			String errorOutput = stringWriter.getBuffer().toString();
			System.err.println("Compilation failed: " + errorOutput);
	 		Assert.assertTrue("Compilation failed : " + errorOutput, false);
		}
	}
	
	/**
	 * Recursively collect all the files under some root.  Ignore directories named "CVS".
	 * Used when compiling multiple source files.
	 * @param files a List<File> to which all the files found will be added
	 * @return the set of Files under a root folder.
	 */
	public static void findFilesUnder(File rootFolder, List<File> files) {
		for (File child : rootFolder.listFiles()) {
			if ("CVS".equals(child.getName())) {
				continue;
			}
			if (child.isDirectory()) {
				findFilesUnder(child, files);
			}
			else {
				files.add(child);
			}
		}
	}

	/** @return the name of the folder where class files will be saved */
	public static String getBinFolderName() {
		return _tmpBinFolderName;
	}

	public static JavaCompiler getEclipseCompiler() {
		return _eclipseCompiler;
	}

	/** @return the name of the folder where generated files will be placed */
	public static String getGenFolderName() {
		return _tmpGenFolderName;
	}

	/** @return the name of the folder where source files will be found during compilation */
	public static String getSrcFolderName() {
		return _tmpSrcFolderName;
	}

	/**
	 * Load Eclipse compiler and create temporary directories on disk
	 */
	public static void init()
	{
		_tmpFolder = System.getProperty("java.io.tmpdir");
		if (_tmpFolder.endsWith(File.separator)) {
			_tmpFolder += "eclipse-temp";
		} else {
			_tmpFolder += (File.separator + "eclipse-temp");
		}
		_tmpBinFolderName = _tmpFolder + File.separator + "bin";
		_tmpBinDir = new File(_tmpBinFolderName);
		BatchTestUtils.deleteTree(_tmpBinDir); // remove existing contents
		_tmpBinDir.mkdirs();
		assert _tmpBinDir.exists() : "couldn't mkdirs " + _tmpBinFolderName;

		_tmpGenFolderName = _tmpFolder + File.separator + "gen-src";
		_tmpGenDir = new File(_tmpGenFolderName);
		BatchTestUtils.deleteTree(_tmpGenDir); // remove existing contents
		_tmpGenDir.mkdirs();
		assert _tmpGenDir.exists() : "couldn't mkdirs " + _tmpGenFolderName;

		_tmpSrcFolderName = _tmpFolder + File.separator + "src";
		_tmpSrcDir = new File(_tmpSrcFolderName);
		BatchTestUtils.deleteTree(_tmpSrcDir); // remove existing contents
		_tmpSrcDir.mkdirs();
		assert _tmpSrcDir.exists() : "couldn't mkdirs " + _tmpSrcFolderName;

		try {
			_processorJarPath = setupProcessorJar(PROCESSOR_JAR_NAME, _tmpFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull("No processor jar path set", _processorJarPath);
		File processorJar = new File(_processorJarPath);
		Assert.assertTrue("Couldn't find processor jar at " + processorJar.getAbsolutePath(), processorJar.exists());

		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class);//, EclipseCompiler.class.getClassLoader());
		Class<?> c = null;
		try {
			c = Class.forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler");
		} catch (ClassNotFoundException e) {
			// ignore
		}
		if (c == null) {
			Assert.assertTrue("Eclipse compiler is not available", false);
		}
		int compilerCounter = 0;
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			compilerCounter++;
			if (c.isInstance(javaCompiler)) {
				_eclipseCompiler = javaCompiler;
			}
		}
		Assert.assertEquals("Only one compiler available", 1, compilerCounter);
		Assert.assertNotNull("No Eclipse compiler found", _eclipseCompiler);
	}

	public static void tearDown() {
		new File(_processorJarPath).deleteOnExit();
		BatchTestUtils.deleteTree(new File(_tmpFolder));
	}
	protected static String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.compiler.apt.tests").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = null;
		try {
			stream = new java.io.FileInputStream(file);
			int bytesRead = 0;
			int lastReadSize = 0;
			while ((lastReadSize != -1) && (bytesRead != fileLength)) {
				lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
				bytesRead += lastReadSize;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return fileBytes;
	}
	
	/**
	 * @return true if this file's end-of-line delimiters should be replaced with
	 * a platform-independent value, e.g. for compilation.
	 */
	public static boolean shouldConvertToIndependentLineDelimiter(File file) {
		return file.getName().endsWith(".java");
	}
	
	/**
	 * Copy a file from one location to another, unless the destination file already exists and has
	 * the same timestamp and file size. Create the destination location if necessary. Convert line
	 * delimiters according to {@link #shouldConvertToIndependentLineDelimiter(File)}.
	 * 
	 * @param src
	 *            the full path to the resource location.
	 * @param destFolder
	 *            the full path to the destination location.
	 * @throws IOException
	 */
	public static void copyResource(File src, File dest) throws IOException {
		if (dest.exists() &&
				src.lastModified() < dest.lastModified() && 
				src.length() == dest.length()) 
		{
			return;
		}
		
		// read source bytes
		byte[] srcBytes = null;
		srcBytes = read(src);

		if (shouldConvertToIndependentLineDelimiter(src)) {
			String contents = new String(srcBytes);
			contents = TestUtils.convertToIndependentLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}

		File destFolder = dest.getParentFile();
		if (!destFolder.exists()) {
			if (!destFolder.mkdirs()) {
				throw new IOException("Unable to create directory " + destFolder);
			}
		}
		// write bytes to dest
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(dest);
			out.write(srcBytes);
			out.flush();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Copy a resource that is located under the <code>resources</code> folder of the plugin to a
	 * corresponding location under the specified target folder. Convert line delimiters according
	 * to {@link #shouldConvertToIndependentLineDelimiter(File)}.
	 * 
	 * @param resourcePath
	 *            the relative path under <code>[plugin-root]/resources</code> of the resource to
	 *            be copied
	 * @param targetFolder
	 *            the absolute path of the folder under which the resource will be copied. Folder
	 *            and subfolders will be created if necessary.
	 * @return a file representing the copied resource
	 * @throws IOException
	 */
	public static File copyResource(String resourcePath, File targetFolder) throws IOException {
		File resDir = new File(getPluginDirectoryPath(), RESOURCES_DIR);
		File resourceFile = new File(resDir, resourcePath);
		File targetFile = new File(targetFolder, resourcePath);
		copyResource(resourceFile, targetFile);
		return targetFile;
	}
	
	/**
	 * Copy all the files under the directory specified by src to the directory
	 * specified by dest.  The src and dest directories must exist; child directories
	 * under dest will be created as required.  Existing files in dest will be
	 * overwritten.  Newlines will be converted according to 
	 * {@link #shouldConvertToIndependentLineDelimiter(File)}.  Directories
	 * named "CVS" will be ignored.
	 * @param resourceFolderName the name of the source folder, relative to
	 * <code>[plugin-root]/resources</code>
	 * @param the absolute path of the destination folder
	 * @throws IOException 
	 */
	public static void copyResources(String resourceFolderName, File destFolder) throws IOException {
		File resDir = new File(getPluginDirectoryPath(), RESOURCES_DIR);
		File resourceFolder = new File(resDir, resourceFolderName);
		copyResources(resourceFolder, destFolder);
	}
	
	private static void copyResources(File resourceFolder, File destFolder) throws IOException {
		if (resourceFolder == null) {
			return;
		}
		// Copy all resources in this folder
		String[] children = resourceFolder.list();
		if (null == children) {
			return;
		}
		// if there are any children, (recursively) copy them
		for (String child : children) {
			if ("CVS".equals(child)) {
				continue;
			}
			File childRes = new File(resourceFolder, child);
			File childDest = new File(destFolder, child);
			if (childRes.isDirectory()) {
				copyResources(childRes, childDest);
			}
			else {
				copyResource(childRes, childDest);
			}
		}
	}
	
	public static String setupProcessorJar(String processorJar, String tmpDir) throws IOException {
		File libDir = new File(getPluginDirectoryPath());
		File libFile = new File(libDir, processorJar);
		File destinationDir = new File(tmpDir);
		File destinationFile = new File(destinationDir, processorJar);
		copyResource(libFile, destinationFile);
		return destinationFile.getCanonicalPath();
	}

	/**
	 * Recursively delete the contents of a directory, including any subdirectories.
	 * This is not optimized to handle very large or deep directory trees efficiently.
	 * @param f is either a normal file (which will be deleted) or a directory
	 * (which will be emptied and then deleted).
	 */
	public static void deleteTree(File f)
	{
		if (null == f) {
			return;
		}
		File[] children = f.listFiles();
		if (null != children) {
			// if f has any children, (recursively) delete them
			for (File child : children) {
				deleteTree(child);
			}
		}
		// At this point f is either a normal file or an empty directory
		f.delete();
	}

}
