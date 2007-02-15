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
import java.io.PrintWriter;
import java.io.StringWriter;
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

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * Helper class to support compilation and results checking for tests running in batch mode.  
 * @since 3.3
 */
public class BatchTestUtils {
	// relative to plugin directory
	private static final String _processorJarName = "export\\apttestprocessors.jar";
	
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
		options.add(_tmpSrcFolderName + File.pathSeparator + _tmpGenFolderName + File.pathSeparator + _processorJarName);
		options.add("-processorpath");
		options.add(_processorJarName);
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
		
		File processorJar = new File(_processorJarName);
		Assert.assertTrue("Couldn't find processor jar at " + processorJar.getAbsolutePath(), processorJar.exists());
		
		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class);//, EclipseCompiler.class.getClassLoader());
		int compilerCounter = 0;
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			compilerCounter++;
			if (javaCompiler instanceof EclipseCompiler) {
				_eclipseCompiler = javaCompiler;
			}
	     }
		Assert.assertEquals("Only one compiler available", 1, compilerCounter);
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
