/*******************************************************************************
 * Copyright (c) 2007, 2021 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
  *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fix for 342936
 *    het@google.com - Bug 415274 - Annotation processing throws a NPE in getElementsAnnotatedWith()
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

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
	private static final String JLS8_PROCESSOR_JAR_NAME = "lib/apttestprocessors8.jar";
	public static String _processorJarPath;
	public static String _jls8ProcessorJarPath;

	// locations to copy and generate files
	public static String _tmpFolder;

	private static JavaCompiler _eclipseCompiler;

	private static String _tmpSrcFolderName;
	private static File _tmpSrcDir;
	private static String _tmpBinFolderName;
	private static File _tmpBinDir;
	public static String _tmpGenFolderName;
	private static File _tmpGenDir;

	public static final class DiagnosticReport<S> implements DiagnosticListener<S> {
		public StringBuffer buffer;
		private final List<Diagnostic<? extends S>> diagnostics = new ArrayList<>();
		DiagnosticReport() {
			this.buffer = new StringBuffer();
		}
		public void report(Diagnostic<? extends S> diagnostic) {
			diagnostics.add(diagnostic);
			buffer.append(diagnostic.getMessage(Locale.getDefault()));
			buffer.append("\n");
		}
		public List<Diagnostic<? extends S>> get(Diagnostic.Kind first, Diagnostic.Kind... rest) {
			Set<Diagnostic.Kind> wanted = EnumSet.of(first, rest);
			return diagnostics.stream().filter(d -> wanted.contains(d.getKind())).collect(Collectors.toList());
		}
		public String toString() {
			return this.buffer.toString();
		}
	}

	/**
	 * Create a class that contains an annotation that generates another class,
	 * and compile it.  Verify that generation and compilation succeeded.
	 */
	public static DiagnosticReport<JavaFileObject> compileOneClass(JavaCompiler compiler, List<String> options, File inputFile) {
		return compileOneClass(compiler, options, inputFile, false);
	}
	public static DiagnosticReport<JavaFileObject> compileOneClass(JavaCompiler compiler, List<String> options, File inputFile, boolean useJLS8Processors) {
		DiagnosticReport<JavaFileObject> diagnostics = new DiagnosticReport<>();
		StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), Charset.defaultCharset());

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
		addProcessorPaths(options, useJLS8Processors, true);
		options.add("-XprintRounds");
		options.add("-XprintProcessorInfo");
		CompilationTask task = compiler.getTask(printWriter, manager, diagnostics, options, null, units);
		Boolean result = task.call();

		if (!result.booleanValue()) {
			String errorOutput = stringWriter.getBuffer().toString();
			System.err.println("Compilation failed: " + errorOutput);
	 		junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
		}
		try {
			manager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diagnostics;
	}

	public static void compileTree(JavaCompiler compiler, List<String> options, File targetFolder) {
		compileTree(compiler, options, targetFolder, false);
	}

	public static void compileTree(JavaCompiler compiler, List<String> options, File targetFolder,
			DiagnosticListener<? super JavaFileObject> listener) {
		compileTree(compiler, options, targetFolder, false, listener);
	}

	public static void compileTree(JavaCompiler compiler, List<String> options, File targetFolder, boolean useJLS8Processors) {
		compileTree(compiler, options, targetFolder, useJLS8Processors, null);
	}

	public static void compileInModuleMode(JavaCompiler compiler, List<String> options, String processor,
			File targetFolder, DiagnosticListener<? super JavaFileObject> listener, boolean multiModule) throws IOException {
		compileInModuleMode(compiler, options, processor, targetFolder, listener, multiModule, true);
	}
	public static void compileInModuleMode(JavaCompiler compiler, List<String> options, String processor,
			File targetFolder, DiagnosticListener<? super JavaFileObject> listener, boolean multiModule, boolean processBinariesAgain) throws IOException {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Iterable<? extends File> location = manager.getLocation(StandardLocation.CLASS_PATH);
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		findFilesUnder(targetFolder, files);
		files.sort(new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				int compareTo = f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
				return compareTo;
			}
		});
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		List<String> copyOptions = new ArrayList<>();
		copyOptions.addAll(options);
		copyOptions.add("-processor");
		copyOptions.add(processor);
		copyOptions.add("-A" + processor);
		copyOptions.add("-d");
		copyOptions.add(_tmpBinFolderName);
		copyOptions.add("-s");
		copyOptions.add(_tmpGenFolderName);
		addModuleProcessorPath(copyOptions, targetFolder.getAbsolutePath(), multiModule);
		copyOptions.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, manager, listener, copyOptions, null, units);
		Boolean result = task.call();

		if (!result.booleanValue()) {
			String errorOutput = stringWriter.getBuffer().toString();
			System.err.println("Compilation failed: " + errorOutput);
	 		junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
		} else {
			junit.framework.TestCase.assertEquals("succeeded", System.getProperty(processor));
		}
		if (!processBinariesAgain) {
			return;
		}
		List<String> classes = new ArrayList<>();
		try {
			manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
			System.clearProperty(processor);
			copyOptions = new ArrayList<>();
			copyOptions.addAll(options);
			copyOptions.add("-Abinary");
			copyOptions.add("-cp");
			copyOptions.add(_jls8ProcessorJarPath + File.pathSeparator + _tmpGenFolderName);
			copyOptions.add("--processor-module-path");
			copyOptions.add(_jls8ProcessorJarPath);
			copyOptions.add("--module-path");
			copyOptions.add(_tmpBinFolderName);
			classes.add("java.base/java.lang.Object"); // This is required to make sure BTB for Object is fully populated.
			findClassesUnderModules(Paths.get(_tmpBinFolderName), classes);
			manager.setLocation(StandardLocation.CLASS_PATH, location);
			task = compiler.getTask(printWriter, manager, listener, copyOptions, classes, null);
			result = task.call();
			if (!result.booleanValue()) {
				String errorOutput = stringWriter.getBuffer().toString();
				System.err.println("Compilation failed: " + errorOutput);
		 		junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
			} else {
				junit.framework.TestCase.assertEquals("succeeded", System.getProperty(processor));
			}
		} catch (IOException e) {
			// print the stack just in case.
			e.printStackTrace();
		}
		try {
			manager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void compileTree(JavaCompiler compiler, List<String> options,
			File targetFolder, boolean useJLS8Processors,
			DiagnosticListener<? super JavaFileObject> listener) {
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
		addProcessorPaths(options, useJLS8Processors, true);
		options.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, manager, listener, options, null, units);
		Boolean result = task.call();

		if (!result.booleanValue()) {
			String errorOutput = stringWriter.getBuffer().toString();
			System.err.println("Compilation failed: " + errorOutput);
	 		junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
		}
		try {
			manager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public interface InjectCustomOptions {
		public void execute(List<String> options);
	}
	/*
	 * First compiles the given files without processor, then processes them
	 * with the just compiled binaries.
	 */
	public static void compileTreeAndProcessBinaries(JavaCompiler compiler, List<String> options, String processor,
			File targetFolder, DiagnosticListener<? super JavaFileObject> listener) {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Iterable<? extends File> location = manager.getLocation(StandardLocation.CLASS_PATH);
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		findFilesUnder(targetFolder, files);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		List<String> copyOptions = new ArrayList<>();
		copyOptions.addAll(options);
		copyOptions.add("-d");
		copyOptions.add(_tmpBinFolderName);
		copyOptions.add("-s");
		copyOptions.add(_tmpGenFolderName);
		addProcessorPaths(copyOptions, true, true);
		copyOptions.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, manager, listener, copyOptions, null, units);
		Boolean result = task.call();

		if (!result.booleanValue()) {
			String errorOutput = stringWriter.getBuffer().toString();
			System.err.println("Compilation failed: " + errorOutput);
	 		junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
		}
		// Check the APT result for the source round already
		if (!"succeeded".equals(System.getProperty(processor))) {
			return;
		}
		List<String> classes = new ArrayList<>();
		try {
			System.clearProperty(processor);
			copyOptions = new ArrayList<>();
			copyOptions.addAll(options);
			copyOptions.add("-Abinary");
			copyOptions.add("-cp");
			copyOptions.add(_tmpBinFolderName + File.pathSeparator + _jls8ProcessorJarPath + File.pathSeparator + _tmpGenFolderName);
			copyOptions.add("-processorpath");
			copyOptions.add(_jls8ProcessorJarPath);
			classes.add("java.lang.Object"); // This is required to make sure BTB for Object is fully populated.
			findClassesUnder(Paths.get(_tmpBinFolderName), null, classes, null);
			manager.setLocation(StandardLocation.CLASS_PATH, location);
			task = compiler.getTask(printWriter, manager, listener, copyOptions, classes, null);
			result = task.call();
			if (!result.booleanValue()) {
				String errorOutput = stringWriter.getBuffer().toString();
				System.err.println("Compilation failed: " + errorOutput);
		 		junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
			}
		} catch (IOException e) {
			// print the stack just in case.
			e.printStackTrace();
		}
	}

	/**
	 * Compile the contents of a directory tree, collecting errors so that they can be
	 * compared with expected errors.
	 * @param compiler the system compiler or Eclipse compiler
	 * @param options will be passed to the compiler
	 * @param targetFolder the folder to compile
	 * @param errors a StringWriter into which compiler output will be written
	 * @return true if the compilation was successful
	 */
	public static boolean compileTreeWithErrors(
			JavaCompiler compiler,
			List<String> options,
			File targetFolder,
			DiagnosticListener<? super JavaFileObject> diagnosticListener) {
		return compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener, false, true);
	}
	public static boolean compileTreeWithErrors(
			JavaCompiler compiler,
			List<String> options,
			File targetFolder,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			boolean addProcessorsToClasspath) {
		return compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener, false, addProcessorsToClasspath);
	}
	public static boolean compileTreeWithErrors(
			JavaCompiler compiler,
			List<String> options,
			File targetFolder,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			boolean useJLS8Processors, 
			boolean addProcessorsToClasspath,
			InjectCustomOptions custom) {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		findFilesUnder(targetFolder, files);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);

		options.add("-d");
		options.add(_tmpBinFolderName);
		options.add("-s");
		options.add(_tmpGenFolderName);
		addProcessorPaths(options, useJLS8Processors, addProcessorsToClasspath);
		if (custom != null) custom.execute(options);
		// use writer to prevent System.out/err to be polluted with problems
		StringWriter writer = new StringWriter();
		CompilationTask task = compiler.getTask(writer, manager, diagnosticListener, options, null, units);
		Boolean result = task.call();
		try {
			manager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.booleanValue();
	}
	public static boolean compileTreeWithErrors(
			JavaCompiler compiler,
			List<String> options,
			File targetFolder,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			boolean useJLS8Processors, boolean addProcessorsToClasspath) {
			return compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener, useJLS8Processors, addProcessorsToClasspath, null);
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

	protected static void findClassesUnderModules(Path modulePath, List<String> classes) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(modulePath)) {
	        for (Path entry : stream) {
	        	if (Files.isDirectory(entry)) {
	        		findClassesUnder(entry, entry, classes, entry.getFileName().toString());
	        	}
	        }
		}
	}
	protected static void findClassesUnder(Path root, Path folder, List<String> classes, String moduleName) throws IOException {
		if (folder == null)
			folder = root;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
	        for (Path entry : stream) {
	            if (Files.isDirectory(entry)) {
	            	findClassesUnder(root, entry, classes, moduleName);
	            } else {
	            	String fileName = entry.getFileName().toString();
					if (fileName.endsWith(".class") && !fileName.startsWith("module-info")) {
						String className = root.relativize(entry).toString();
						className = className.substring(0, className.indexOf(".class"));
						className = className.replace(File.separatorChar, '.');
						if (moduleName != null) {
							className = moduleName + "/" + className;
						}
						classes.add(className);
	            	}
	            }
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

	public static String getResourceFolderName() {
		return RESOURCES_DIR;
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
			_jls8ProcessorJarPath = setupProcessorJar(JLS8_PROCESSOR_JAR_NAME, _tmpFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		junit.framework.TestCase.assertNotNull("No processor jar path set", _processorJarPath);
		File processorJar = new File(_processorJarPath);
		junit.framework.TestCase.assertTrue("Couldn't find processor jar at " + processorJar.getAbsolutePath(), processorJar.exists());

		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class, BatchTestUtils.class.getClassLoader());
		Class<?> c = null;
		try {
			c = Class.forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler");
		} catch (ClassNotFoundException e) {
			// ignore
		}
		if (c == null) {
			junit.framework.TestCase.assertTrue("Eclipse compiler is not available", false);
		}
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			if (c.isInstance(javaCompiler)) {
				_eclipseCompiler = javaCompiler;
			}
		}
		junit.framework.TestCase.assertNotNull("No Eclipse compiler found", _eclipseCompiler);
	}

	private static void addProcessorPaths(List<String> options, boolean useJLS8Processors, boolean addToNormalClasspath) {
		String path = useJLS8Processors ? _jls8ProcessorJarPath : _processorJarPath;
		if (addToNormalClasspath) {
			options.add("-cp");
			options.add(_tmpSrcFolderName + File.pathSeparator + _tmpGenFolderName + File.pathSeparator + path);
		}
		options.add("-processorpath");
		options.add(path);
	}
	private static void addModuleProcessorPath(List<String> options, String srcFolderName, boolean multiModule) {
		options.add("--processor-module-path");
		options.add(_jls8ProcessorJarPath);
		options.add("--module-path");
		options.add(_jls8ProcessorJarPath);
		if (multiModule) {
			options.add("--module-source-path");
			options.add(srcFolderName);
		}
	}

	public static void tearDown() {
		new File(_processorJarPath).deleteOnExit();
		new File(_jls8ProcessorJarPath).deleteOnExit();
		BatchTestUtils.deleteTree(new File(_tmpFolder));
	}
	protected static String getPluginDirectoryPath() {
		try {
			if (Platform.isRunning()) {
				URL platformURL = Platform.getBundle("org.eclipse.jdt.compiler.apt.tests").getEntry("/");
				return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
			}
			return new File(System.getProperty("user.dir")).getAbsolutePath();
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
		writeFile(dest, srcBytes);
	}

	public static void writeFile(File dest, byte[] srcBytes) throws IOException {

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

	/**
	 * Check the contents of a file.
	 * @return true if the contents of <code>genTextFile</code> exactly match <code>string</code>
	 */
	public static boolean fileContentsEqualText(File genTextFile, String input) {
		long length = genTextFile.length();
		if (length != input.length()) {
			return false;
		}
		char[] contents = new char[512];

		Reader r = null;
		try {
			r = new BufferedReader(new FileReader(genTextFile));
			int ofs = 0;
			while (length > 0) {
				int read = r.read(contents);
				String match = input.substring(ofs, ofs + read);
				if (!match.contentEquals(new String(contents, 0, read))) {
					return false;
				}
				ofs += read;
				length -= read;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
