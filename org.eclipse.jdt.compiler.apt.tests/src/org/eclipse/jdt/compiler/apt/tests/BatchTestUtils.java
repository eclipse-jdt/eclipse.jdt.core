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
	// relative to plugin directory
	private static final String PROCESSOR_JAR_NAME = "lib/apttestprocessors.jar";
	private static String _processorJarPath;

	// locations to copy and generate files
	private static final String _tmpFolder = System.getProperty("java.io.tmpdir") + "eclipse-temp";

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
	public static void compileOneClass(JavaCompiler compiler, File inputFile, List<String> options) {
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
			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
	 		Assert.assertTrue("Compilation failed ", false);
		}
	}

	public static String getBinFolderName() {
		return _tmpBinFolderName;
	}

	public static JavaCompiler getEclipseCompiler() {
		return _eclipseCompiler;
	}

	public static String getGenFolderName() {
		return _tmpGenFolderName;
	}

	public static String getSrcFolderName() {
		return _tmpSrcFolderName;
	}

	/**
	 * Load Eclipse compiler and create temporary directories on disk
	 */
	public static void init()
	{
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
	public static boolean convertToIndependantLineDelimiter(File file) {
		return file.getName().endsWith(".java");
	}
	/**
	 * Copy file from src (path to the original file) to dest (path to the destination file).
	 */
	public static void copy(File src, File dest) throws IOException {
		// read source bytes
		byte[] srcBytes = read(src);

		if (convertToIndependantLineDelimiter(src)) {
			String contents = new String(srcBytes);
			contents = TestUtils.convertToIndependantLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}

		File parent = dest.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
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
	 * Copy file from src (path to the original file) to dest (path to the destination file).
	 */
	public static File copyResource(File src, File dest) {
		// read source bytes
		byte[] srcBytes = null;
		try {
			srcBytes = read(src);
		} catch (IOException e1) {
			return null;
		}

		if (convertToIndependantLineDelimiter(src)) {
			String contents = new String(srcBytes);
			contents = TestUtils.convertToIndependantLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}

		if (!dest.exists()) {
			dest.mkdirs();
		}
		// write bytes to dest
		FileOutputStream out = null;
		File result = new File(dest, src.getName());
		try {
			out = new FileOutputStream(result);
			out.write(srcBytes);
			out.flush();
		} catch(IOException e) {
			return null;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return result;
	}

	public static String setupProcessorJar(String processorJar, String tmpDir) throws IOException {
		String resourceDir = getPluginDirectoryPath();
		java.io.File destinationDir = new java.io.File(tmpDir);
		java.io.File libraryFile =
			new java.io.File(tmpDir, processorJar);
		if (!destinationDir.exists()) {
			if (!destinationDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + destinationDir);
			}
			//copy the two files to the JCL directory
			java.io.File libraryResource =
				new java.io.File(resourceDir, processorJar);
			copy(libraryResource, libraryFile);
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing or less recent than the one in workspace
			java.io.File libraryResource =
				new java.io.File(resourceDir, processorJar);
			if ((libraryFile.lastModified() < libraryResource.lastModified())
					|| (libraryFile.length() != libraryResource.length())) {
				copy(libraryResource, libraryFile);
			}
		}
		return libraryFile.getCanonicalPath();
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

	public static File copyResource(String resourcePath, File targetFolder) {
		File resDir = new File(getPluginDirectoryPath(), "resources");
		File resourceFile = new File(resDir, resourcePath);
		return copyResource(resourceFile, targetFolder);
	}
}
