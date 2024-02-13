/*******************************************************************************
 * Copyright (c) 2017, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.compiler.tool.tests.AbstractCompilerToolTest.CompilerInvocationDiagnosticListener;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import junit.framework.TestCase;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CompilerToolJava9Tests extends TestCase {
	private static final boolean DEBUG = false;
	private static final String RESOURCES_DIR = "resources";
	private JavaCompiler[] compilers;
	private String[] compilerNames;
	private boolean isJREBelow9;
	private boolean isJREBelow12;
	private static String _tmpFolder;
	private static String _tmpSrcFolderName;
	private static File _tmpSrcDir;
	private static String _tmpBinFolderName;
	private static File _tmpBinDir;
	public static String _tmpGenFolderName;
	private static File _tmpGenDir;

	private static String modules_directory;
	public CompilerToolJava9Tests(String name) {
		super(name);
	}
	@Override
	protected void setUp() throws Exception {
		this.isJREBelow9 = SourceVersion.latest().compareTo(SourceVersion.RELEASE_8) <= 0;
		if (isJREBelow9)
			return;
		isJREBelow12 = SourceVersion.latest().compareTo(SourceVersion.RELEASE_11) <= 0;
		this.compilers = new JavaCompiler[2];
		this.compilerNames = new String[2];
		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class, EclipseCompiler.class.getClassLoader());
		for (JavaCompiler compiler : javaCompilerLoader) {
			if (compiler instanceof EclipseCompiler) {
				this.compilers[1] = compiler;
				this.compilerNames[1] = "Eclipse Compiler";
			}
		}
		this.compilerNames[0] = "System compiler";
		this.compilers[0] = ToolProvider.getSystemJavaCompiler();
		assertNotNull("System compiler unavailable", this.compilers[0]);
		assertNotNull("Eclipse compiler unavailable", this.compilers[1]);
		initializeLocations();
	}
	@Override
	protected void tearDown() throws Exception {
		if (isJREBelow9) {
			return;
		}
		deleteTree(new File(_tmpFolder));
		super.tearDown();
	}
	protected void initializeLocations() throws IOException {
		Path tempDirectory = Files.createTempDirectory("eclipse-temp");
		_tmpFolder = tempDirectory.toString();
		_tmpBinFolderName = _tmpFolder + File.separator + "bin";
		_tmpBinDir = new File(_tmpBinFolderName);
		deleteTree(_tmpBinDir); // remove existing contents
		Files.createDirectories(_tmpBinDir.toPath());
		assertTrue("couldn't mkdirs " + _tmpBinFolderName, _tmpBinDir.exists());

		_tmpGenFolderName = _tmpFolder + File.separator + "gen-src";
		_tmpGenDir = new File(_tmpGenFolderName);
		deleteTree(_tmpGenDir); // remove existing contents
		Files.createDirectories(_tmpGenDir.toPath());
		assertTrue("couldn't mkdirs " + _tmpGenFolderName, _tmpGenDir.exists());

		_tmpSrcFolderName = _tmpFolder + File.separator + "src";
		_tmpSrcDir = new File(_tmpSrcFolderName);
		deleteTree(_tmpSrcDir); // remove existing contents
		Files.createDirectories(_tmpSrcDir.toPath());
		assertTrue("couldn't mkdirs " + _tmpSrcFolderName, _tmpSrcDir.exists());

		modules_directory = getPluginDirectoryPath() + File.separator + RESOURCES_DIR + File.separator + "module_locations";

		Path moduleInfo = Paths.get(modules_directory, "source", "SimpleModules", "module.one", "module-info.java");
		assertTrue("File should exist: " + moduleInfo, Files.isReadable(moduleInfo));
		moduleInfo = Paths.get(modules_directory, "source", "SimpleModules", "module.two", "module-info.java");
		assertTrue("File should exist: " + moduleInfo, Files.isReadable(moduleInfo));
	}
	public void testGetLocationForModule1() {
		if (this.isJREBelow9) return;
		for(int i = 0; i < 2; i++) {
			String cName = this.compilerNames[i];
			JavaCompiler compiler = this.compilers[i];
			StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
			try {
	            Location location = manager.getLocationForModule(StandardLocation.SYSTEM_MODULES, "java.base");
	            assertNotNull(cName + ": Location should not be null", location);
	        } catch (UnsupportedOperationException ex) {
	            fail(cName + ": Should support getLocationForModule()");
	        }
			catch (IOException e) {
	        	 fail(cName + ": Should support getLocationForModule()");
			}
		}

	}
	public void testSupportedCompilerVersions() throws IOException {
		if (this.isJREBelow9) return;
		for(int i = 0; i < 2; i++) {
			JavaCompiler compiler = this.compilers[i];
			Set<SourceVersion> sourceVersions = compiler.getSourceVersions();
			SourceVersion[] values = SourceVersion.values();
			for (SourceVersion sourceVersion : values) {
				// Javac doesn't appear to have < 3 versions in the supported list, but we do anyway
				if (!(compiler instanceof EclipseCompiler) &&
						sourceVersion.compareTo(SourceVersion.RELEASE_3) <= 0) {
					continue;
				}

				assertTrue("source version " + sourceVersion + " should be supported"
						+ "by compiler " + compiler.getClass().getName(), sourceVersions.contains(sourceVersion));
			}
			//	specifically test the last known - 9
			Object obj = SourceVersion.valueOf("RELEASE_9");
			assertTrue("source version 9 should be supported", sourceVersions.contains(obj));
		}
	}
	public void testGetLocationForModule2() throws IOException {
		if (this.isJREBelow9) return;
		for(int i = 0; i < 2; i++) {
			String cName = this.compilerNames[i];
			JavaCompiler compiler = this.compilers[i];
			if (!(compiler instanceof EclipseCompiler))
				continue;
			StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
			Path path = Paths.get(modules_directory + File.separator + "source" + File.separator + "SimpleModules");
			manager.setLocationFromPaths(StandardLocation.MODULE_SOURCE_PATH, Arrays.asList(path));
			try {
				JavaFileManager.Location location = manager.getLocationForModule(StandardLocation.MODULE_SOURCE_PATH, "module.two");
				Path moduleInfo = path.resolve("module.two" + File.separator + "module-info.java");
				assertTrue("File should exist: " + moduleInfo, Files.isReadable(moduleInfo));
				assertNotNull(cName + ": module path location should not be null for path " + path, location);
			} catch (UnsupportedOperationException ex) {
				fail(cName + ":Should support getLocationForModule()");
			}
		}
	}
	// Incomplete tests - fails both with Javac and ECJ
	public void testGetLocationForModule3() throws IOException {
		if (this.isJREBelow9) return;
		for(int i = 0; i < 2; i++) {
			String cName = this.compilerNames[i];
			JavaCompiler compiler = this.compilers[i];
			if (!(compiler instanceof EclipseCompiler))
				continue;
			StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
			Path path = Paths.get(modules_directory + File.separator + "source" + File.separator + "SimpleModules");
			manager.setLocationFromPaths(StandardLocation.MODULE_SOURCE_PATH, Arrays.asList(path));
			try {
				JavaFileManager.Location location = manager.getLocationForModule(StandardLocation.MODULE_SOURCE_PATH, "module.one");
				Path moduleInfo = path.resolve("module.one" + File.separator + "module-info.java");
				assertTrue("File should exist: " + moduleInfo, Files.isReadable(moduleInfo));
				assertNotNull(cName + ": module path location should not be null for path " + path, location);
			} catch (UnsupportedOperationException ex) {
				fail(cName + ":Should support getLocationForModule()");
			}
		}
	}
	public ForwardingJavaFileManager<JavaFileManager> getFileManager(JavaFileManager manager) {
		return new ForwardingJavaFileManager<>(manager) {
			@Override
			public FileObject getFileForInput(Location location, String packageName, String relativeName)
					throws IOException {
				if (DEBUG) {
					System.out.println("Create file for input : " + packageName + " " + relativeName + " in location " + location);
				}
				return super.getFileForInput(location, packageName, relativeName);
			}
			@Override
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
					throws IOException {
				if (DEBUG) {
					System.out.println("Create java file for input : " + className + " in location " + location);
				}
				return super.getJavaFileForInput(location, className, kind);
			}
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {

				if (DEBUG) {
					System.out.println("Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
				}
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				if (DEBUG) {
					System.out.println(javaFileForOutput.toUri());
				}
				return javaFileForOutput;
			}
		};
	}
	public void testOptionRelease1() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Consumer<JavaFileManager> cons = manager -> {
			String tmpFolder = _tmpFolder;
			File inputFile = new File(tmpFolder, "X.java");
			try (Writer writer = new BufferedWriter(new FileWriter(inputFile))) {
				writer.write(
					"package p;\n" +
					"public class X {}");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			List<File> files = new ArrayList<>();
			files.add(inputFile);
			Iterable<? extends JavaFileObject> units = standardManager.getJavaFileObjectsFromFiles(files);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			List<String> options = new ArrayList<>();
			options.add("-d");
			options.add(tmpFolder);
			options.add("--release");
			options.add("8");
			ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
			PrintWriter err = new PrintWriter(errBuffer);
			CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err) {
				@Override
				public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
					JavaFileObject source = diagnostic.getSource();
					assertNotNull("No source", source);
					super.report(diagnostic);
				}
			};
	 		CompilationTask task = compiler.getTask(printWriter, getFileManager(manager), listener, options, null, units);
	 		// check the classpath location
	 		assertTrue("Has no location CLASS_OUPUT", getFileManager(manager).hasLocation(StandardLocation.CLASS_OUTPUT));
			Boolean result = task.call();
			printWriter.flush();
			printWriter.close();
	 		if (!result.booleanValue()) {
	 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
	 	 		assertTrue("Compilation failed ", false);
	 		}
	 		ClassFileReader reader = null;
	 		try {
				reader = ClassFileReader.read(new File(tmpFolder, "p/X.class"), true);
			} catch (ClassFormatException e) {
				assertTrue("Should not happen", false);
			} catch (IOException e) {
				assertTrue("Should not happen", false);
			}
			assertNotNull("No reader", reader);
			// This needs fix. This test case by design will produce different output every compiler version.
	 		assertEquals("Wrong value", ClassFileConstants.JDK1_8, reader.getVersion());
			// check that the .class file exist for X
			assertTrue("delete failed", inputFile.delete());
		};
		cons.accept(standardManager);
		cons.accept(getFileManager(standardManager));
	}
	public void testOptionRelease2() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Consumer<JavaFileManager> cons = manager -> {
			String tmpFolder = _tmpFolder;
			File inputFile = new File(tmpFolder, "X.java");
			try (Writer writer = new BufferedWriter(new FileWriter(inputFile))){
				writer.write(
					"package p;\n" +
					"public class X {}");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// create new list containing input file
			List<File> files = new ArrayList<>();
			files.add(inputFile);
			Iterable<? extends JavaFileObject> units = standardManager.getJavaFileObjectsFromFiles(files);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			List<String> options = new ArrayList<>();
			options.add("-d");
			options.add(tmpFolder);
			options.add("--release");
			options.add("8");
			ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
			PrintWriter err = new PrintWriter(errBuffer);
			CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err) {
				@Override
				public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
					JavaFileObject source = diagnostic.getSource();
					assertNotNull("No source", source);
					super.report(diagnostic);
				}
			};
	 		CompilationTask task = compiler.getTask(printWriter, getFileManager(manager), listener, options, null, units);
	 		// check the classpath location
	 		assertTrue("Has no location CLASS_OUPUT", getFileManager(manager).hasLocation(StandardLocation.CLASS_OUTPUT));
			Boolean result = task.call();
			printWriter.flush();
			printWriter.close();
	 		if (!result.booleanValue()) {
	 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
	 	 		assertTrue("Compilation failed ", false);
	 		}
	 		ClassFileReader reader = null;
	 		try {
				reader = ClassFileReader.read(new File(tmpFolder, "p/X.class"), true);
			} catch (ClassFormatException e) {
				assertTrue("Should not happen", false);
			} catch (IOException e) {
				assertTrue("Should not happen", false);
			}
			assertNotNull("No reader", reader);
			// This needs fix. This test case by design will produce different output every compiler version.
	 		assertEquals("Wrong value", ClassFileConstants.JDK1_8, reader.getVersion());
			// check that the .class file exist for X
			assertTrue("delete failed", inputFile.delete());
		};
		cons.accept(standardManager);
		cons.accept(getFileManager(standardManager));
	}
	public void testOptionRelease3() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Consumer<JavaFileManager> cons = manager -> {
			String tmpFolder = _tmpFolder;
			File inputFile = new File(tmpFolder, "X.java");
			try (Writer writer = new BufferedWriter(new FileWriter(inputFile))){
				writer.write(
					"package p;\n" +
					"public class X {}");
			} catch (IOException e) {
				e.printStackTrace();
			}
			

			// create new list containing input file
			List<File> files = new ArrayList<>();
			files.add(inputFile);
			Iterable<? extends JavaFileObject> units = standardManager.getJavaFileObjectsFromFiles(files);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			List<String> options = new ArrayList<>();
			options.add("-d");
			options.add(tmpFolder);
			options.add("--release");
			options.add("8");
			options.add("-source");
			options.add("7");
			ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
			PrintWriter err = new PrintWriter(errBuffer);
			CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err);
			try {
				compiler.getTask(printWriter, getFileManager(manager), listener, options, null, units);
				fail("compilation didn't fail as expected");
			} catch(IllegalArgumentException iae) {
				assertEquals("option -source is not supported when --release is used", iae.getMessage());
			}
			// Now with standard file manager
			try {
				compiler.getTask(printWriter, manager, listener, options, null, units);
				fail("compilation didn't fail as expected");
			} catch(IllegalArgumentException iae) {
				assertEquals("option -source is not supported when --release is used", iae.getMessage());
			}
		};
		cons.accept(standardManager);
		cons.accept(getFileManager(standardManager));
	}
	public void testOptionRelease4() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Consumer<JavaFileManager> cons = manager -> {
			String tmpFolder = _tmpFolder;
			File inputFile = new File(tmpFolder, "X.java");
			try (Writer writer = new BufferedWriter(new FileWriter(inputFile))) {
				writer.write(
					"package p;\n" +
					"import java.nio.Buffer;\n" +
					"import java.nio.ByteBuffer;\n" +
					"public class X {\n" +
					"  public Buffer buf() {\n" +
					"    ByteBuffer buffer = ByteBuffer.allocate(10);\n" +
					"    return buffer.flip();\n" +
					"  }\n" +
					"}\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			// create new list containing input file
			List<File> files = new ArrayList<>();
			files.add(inputFile);
			Iterable<? extends JavaFileObject> units = standardManager.getJavaFileObjectsFromFiles(files);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);

			List<String> options = new ArrayList<>();
			options.add("-d");
			options.add(tmpFolder);
			options.add("--release");
			options.add("8");
			ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
			PrintWriter err = new PrintWriter(errBuffer);
			CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err) {
				@Override
				public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
					JavaFileObject source = diagnostic.getSource();
					assertNotNull("No source", source);
					super.report(diagnostic);
				}
			};
			
	 		CompilationTask task = compiler.getTask(printWriter, manager, listener, options, null, units);
	 		// check the classpath location
			Boolean result = task.call();
			printWriter.flush();
			printWriter.close();
	 		if (!result.booleanValue()) {
	 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
	 	 		assertTrue("Compilation failed ", false);
	 		}
	 		ClassFileReader reader = null;
	 		try {
				reader = ClassFileReader.read(new File(tmpFolder, "p/X.class"), false);
			} catch (ClassFormatException e) {
				assertTrue("Should not happen", false);
			} catch (IOException e) {
				assertTrue("Should not happen", false);
			}
			assertNotNull("No reader", reader);
			// This needs fix. This test case by design will produce different output every compiler version.
	 		assertEquals("Wrong value", ClassFileConstants.JDK1_8, reader.getVersion());
	 		// check that the correct call was generated: must return a Buffer, not a ByteBuffer
	 		boolean found = false;
	 		int[] offsets = reader.getConstantPoolOffsets();
	 		for (int i = 0; i < offsets.length; i++) {
	 			int tag = reader.u1At(offsets[i]);
	 			if (tag == ClassFileConstants.MethodRefTag || tag ==  ClassFileConstants.InterfaceMethodRefTag) {
	 				char[] name = extractName(offsets, reader, i);
	 				char[] className = extractClassName(offsets, reader, i);
	 				String fullName = new String(className) + '.' + new String(name);
	 				char[] typeName = extractType(offsets, reader, i);
	 				if ("java/nio/ByteBuffer.flip".equals(fullName)) {
	 					found = true;
	 					assertEquals(fullName + "()Ljava/nio/Buffer;", fullName + new String(typeName));
	 					break;
	 				}
	 			}
	 		}
	 		assertTrue("No call to ByteBuffer.flip()", found);
			// check that the .class file exist for X
			assertTrue("delete failed", inputFile.delete());
		};
		cons.accept(standardManager);
		cons.accept(getFileManager(standardManager));
	}
	public void testOptionRelease5() throws IOException {
		if (this.isJREBelow12) return;
		JavaCompiler compiler = this.compilers[1];
		StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Consumer<JavaFileManager> cons = manager -> {
			String tmpFolder = _tmpFolder;
			File inputFile = new File(tmpFolder, "X.java");
			try (Writer writer = new BufferedWriter(new FileWriter(inputFile))){
				writer.write(
						"public class X { \n" +
						"	public Module getModule(String name) {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// create new list containing input file
			List<File> files = new ArrayList<>();
			files.add(inputFile);
			Iterable<? extends JavaFileObject> units = standardManager.getJavaFileObjectsFromFiles(files);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			List<String> options = new ArrayList<>();
			options.add("-d");
			options.add(tmpFolder);
			options.add("--release");
			options.add("8");
			ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
			PrintWriter err = new PrintWriter(errBuffer);
			CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err);
			CompilationTask task = compiler.getTask(printWriter, getFileManager(manager), listener, options, null, units);
			Boolean result = task.call();
			printWriter.flush();
			printWriter.close();
			if (result.booleanValue()) {
				System.err.println("Compilation did not fail as expected: " + stringWriter.getBuffer().toString());
				assertTrue("Compilation did not fail as expected", false);
			}
		};
		cons.accept(standardManager);
		cons.accept(getFileManager(standardManager));
	}
	public void testOptionRelease6() throws IOException {
		if (this.isJREBelow12) return;
		JavaCompiler compiler = this.compilers[1];
		StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		Consumer<JavaFileManager> cons = manager -> {
			String tmpFolder = _tmpFolder;
			File inputFile = new File(tmpFolder, "X.java");
			try (Writer writer = new BufferedWriter(new FileWriter(inputFile))){
				writer.write(
						"public class X { \n" +
						"	public Module getModule(String name) {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// create new list containing input file
			List<File> files = new ArrayList<>();
			files.add(inputFile);
			Iterable<? extends JavaFileObject> units = standardManager.getJavaFileObjectsFromFiles(files);
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			List<String> options = new ArrayList<>();
			options.add("-d");
			options.add(tmpFolder);
			options.add("--release");
			options.add("10");
			ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
			PrintWriter err = new PrintWriter(errBuffer);
			CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err);
			CompilationTask task = compiler.getTask(printWriter, getFileManager(manager), listener, options, null, units);
			Boolean result = task.call();
			printWriter.flush();
			printWriter.close();
			if (!result.booleanValue()) {
				System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
				assertTrue("Compilation failed", false);
			}
		};
		cons.accept(standardManager);
		cons.accept(getFileManager(standardManager));
	}
	private char[] extractClassName(int[] constantPoolOffsets, ClassFileReader reader, int index) {
		// the entry at i has to be a field ref or a method/interface method ref.
		int class_index = reader.u2At(constantPoolOffsets[index] + 1);
		int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[class_index] + 1)];
		return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
	}
	private char[] extractName(int[] constantPoolOffsets, ClassFileReader reader, int index) {
		int nameAndTypeIndex = reader.u2At(constantPoolOffsets[index] + 3);
		int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[nameAndTypeIndex] + 1)];
		return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
	}
	private char[] extractType(int[] constantPoolOffsets, ClassFileReader reader, int index) {
		int constantPoolIndex = reader.u2At(constantPoolOffsets[index] + 3);
		int utf8Offset = constantPoolOffsets[reader.u2At(constantPoolOffsets[constantPoolIndex] + 3)];
		return reader.utf8At(utf8Offset + 3, reader.u2At(utf8Offset + 1));
	}
	public void testClassOutputLocationForModule_1() throws IOException {
		if (this.isJREBelow9) return;
		for(int i = 0; i < 2; i++) {
			String cName = this.compilerNames[i];
			JavaCompiler compiler = this.compilers[i];
			if (!(compiler instanceof EclipseCompiler))
				continue;
			StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
			Path path = Paths.get(modules_directory + File.separator + "source" + File.separator + "SimpleModules");
			manager.setLocationFromPaths(StandardLocation.MODULE_SOURCE_PATH, Arrays.asList(path));
			manager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(_tmpBinDir));
			try {
				JavaFileManager.Location location = manager.getLocationForModule(StandardLocation.CLASS_OUTPUT, "module.two");
				assertNotNull(cName + ":module path location should not be null", location);
				assertTrue("should be output location", location.isOutputLocation());
			} catch (UnsupportedOperationException ex) {
				fail(cName + ":Should support getLocationForModule()");
			}
		}
	}
	public void testClassOutputLocationForModule_2() throws IOException {
		if (this.isJREBelow9) return;
		for(int i = 0; i < 2; i++) {
			String cName = this.compilerNames[i];
			JavaCompiler compiler = this.compilers[i];
			if (!(compiler instanceof EclipseCompiler))
				continue;
			StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
			Path path = Paths.get(modules_directory + File.separator + "source" + File.separator + "SimpleModules" + File.separator + "module.one");
			manager.setLocationForModule(StandardLocation.MODULE_SOURCE_PATH, "module.one", Arrays.asList(path));
			path = Paths.get(modules_directory + File.separator + "source" + File.separator + "SimpleModules" + File.separator + "module.two");
			manager.setLocationForModule(StandardLocation.MODULE_SOURCE_PATH, "module.two", Arrays.asList(path));
			manager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(_tmpBinDir));
			try {
				JavaFileManager.Location location = manager.getLocationForModule(StandardLocation.CLASS_OUTPUT, "module.one");
				assertTrue("should be output location", location.isOutputLocation());
				assertNotNull(cName + ":module path location should not be null", location);
				Iterable<? extends Path> locationAsPaths = manager.getLocationAsPaths(location);
				int count = 0;
				boolean found = false;
				for (Path path2 : locationAsPaths) {
					if (path2.endsWith("module.one")) {
						found = true;
					}
					count++;
				}
				assertEquals("incorrect no of output locations", 1, count);
				assertTrue("output location for module.two not found", found);
				location = manager.getLocationForModule(StandardLocation.CLASS_OUTPUT, "module.two");
				assertTrue("should be output location", location.isOutputLocation());
				assertNotNull(cName + ":module path location should not be null", location);
				locationAsPaths = manager.getLocationAsPaths(location);
				count = 0;
				found = false;
				for (Path path2 : locationAsPaths) {
					if (path2.endsWith("module.two")) {
						found = true;
					}
					count++;
				}
				assertEquals("incorrect no of output locations", 1, count);
				assertTrue("output location for module.two not found", found);

			} catch (UnsupportedOperationException ex) {
				fail(cName + ":Should support getLocationForModule()");
			}
		}
	}
	// Test that non-module folders inside module-path are not considered 
	// to be automatic modules.
	public void testBug565748() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		String tmpFolder = _tmpFolder;
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing input file
		List<File> files = new ArrayList<>();
		files.add(new File(modules_directory + File.separator + "source" + File.separator + "SimpleModules" +
										File.separator + "module.one" + File.separator + "module-info.java"));
		files.add(new File(modules_directory + File.separator + "source" + File.separator + "SimpleModules" +
				File.separator + "module.two" + File.separator + "module-info.java"));
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		List<String> options = new ArrayList<>();
		options.add("-d");
		options.add(tmpFolder);
		options.add("--module-source-path");
		options.add(modules_directory + File.separator + "source" + File.separator + "SimpleModules");
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintWriter err = new PrintWriter(errBuffer);
		CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err) {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				JavaFileObject source = diagnostic.getSource();
				assertNotNull("No source", source);
				super.report(diagnostic);
			}
		};
 		CompilationTask task = compiler.getTask(printWriter, manager, listener, options, null, units);
 		// check the classpath location
		Boolean result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed unexpectedly: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
 
 		// Try using the same outut as module-path and compile the new module (mod.test)
		manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing input file
		files = new ArrayList<>();
		files.add(new File(modules_directory + File.separator + "bug565748" + File.separator + 
									"mod.test" + File.separator + "module-info.java"));
		units = manager.getJavaFileObjectsFromFiles(files);
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);

		options = new ArrayList<>();
		options.add("-d");
		options.add(tmpFolder);
		options.add("--module-path");
		options.add(tmpFolder);
		options.add("--module-source-path");
		options.add(modules_directory + File.separator + "bug565748");
		errBuffer = new ByteArrayOutputStream();
		err = new PrintWriter(errBuffer);
		listener = new CompilerInvocationDiagnosticListener(err) {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				JavaFileObject source = diagnostic.getSource();
				assertNotNull("No source", source);
				super.report(diagnostic);
			}
		};
 		task = compiler.getTask(printWriter, manager, listener, options, null, units);
		result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed unexpectedly: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
 
 		// Delete the module-info.class from the previously compiled modules
 		// and try compiling the same module mod.test
 		File file = new File(tmpFolder + File.separator + "mod.test" + File.separator + "module-info.class");
 		file.delete();
 		file = new File(tmpFolder + File.separator + "mod.test");
 		file.delete();
 		file = new File(tmpFolder + File.separator + "module.one" + File.separator + "module-info.class");
 		file.delete();
 		file = new File(tmpFolder + File.separator + "module.two" + File.separator + "module-info.class");
 		file.delete();
 		manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

 		// create new list containing input file
 		files = new ArrayList<>();
 		files.add(new File(modules_directory + File.separator + "bug565748" + File.separator + 
 				"mod.test" + File.separator + "module-info.java"));
 		units = manager.getJavaFileObjectsFromFiles(files);
 		stringWriter = new StringWriter();
 		printWriter = new PrintWriter(stringWriter);

 		options = new ArrayList<>();
 		options.add("-d");
 		options.add(tmpFolder);
 		options.add("--module-path");
 		options.add(tmpFolder);
 		options.add("--module-source-path");
 		options.add(modules_directory + File.separator + "bug565748");
 		errBuffer = new ByteArrayOutputStream();
 		err = new PrintWriter(errBuffer);
 		listener = new CompilerInvocationDiagnosticListener(err) {
 			@Override
 			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
 				JavaFileObject source = diagnostic.getSource();
 				assertNotNull("No source", source);
 				super.report(diagnostic);
 			}
 		};
 		task = compiler.getTask(printWriter, manager, listener, options, null, units);
 		result = task.call();
 		printWriter.flush();
 		printWriter.close();
 		if (result.booleanValue()) {
 			System.err.println("Compilation should fail: " + stringWriter.getBuffer().toString());
 			assertTrue("Compilation did not fail ", false);
 		}
	}
	public void testBug566749() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		String tmpFolder = _tmpFolder;
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing input file
		List<File> files = new ArrayList<>();
		files.add(new File(modules_directory + File.separator + "bug566749" + File.separator + 
 				"mod.test" + File.separator + "module-info.java"));
		
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		List<String> options = new ArrayList<>();
		options = new ArrayList<>();
 		options.add("-d");
 		options.add(tmpFolder);
 		options.add("--module-path");
 		options.add(tmpFolder);
 		options.add("--module-source-path");
 		options.add(modules_directory + File.separator + "bug566749");
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintWriter err = new PrintWriter(errBuffer);
		CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err) {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				super.report(diagnostic);
			}
		};
 		CompilationTask task = compiler.getTask(printWriter, manager, listener, options, null, units);
 		// check the classpath location
		Boolean result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (result.booleanValue()) {
 			System.err.println("Compilation did not fail as expected: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation did not fail as expected ", false);
 		}
	}
	public void testBug574097() throws IOException {
		if (this.isJREBelow9) return;
		JavaCompiler compiler = this.compilers[1];
		String tmpFolder = _tmpFolder;
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		// create new list containing input file
		List<File> files = new ArrayList<>();
		files.add(new File(modules_directory + File.separator + "bug574097" + File.separator + 
 				"mod.one" + File.separator + "module-info.java"));
		files.add(new File(modules_directory + File.separator + "bug574097" + File.separator + 
 				"mod.one" + File.separator + "abc" + File.separator + "A.java"));
		
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);

		List<String> options = new ArrayList<>();
		options = new ArrayList<>();
 		options.add("-d");
 		options.add(tmpFolder);
 		options.add("--module-path");
 		options.add(tmpFolder);
 		options.add("--module-source-path");
 		options.add(modules_directory + File.separator + "bug574097");
 		options.add("--processor-module-path");
 		options.add(getPluginDirectoryPath() + File.separator + RESOURCES_DIR + File.separator + "bug574097.jar");
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintWriter err = new PrintWriter(errBuffer);
		CompilerInvocationDiagnosticListener listener = new CompilerInvocationDiagnosticListener(err) {
			@Override
			public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
				super.report(diagnostic);
			}
		};
 		CompilationTask task = compiler.getTask(printWriter, manager, listener, options, null, units);
 		// check the classpath location
		Boolean result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (result.booleanValue()) {
 			System.err.println("Compilation did not fail as expected: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation did not fail as expected ", false);
 		}
	}
	public void testBug569833() throws IOException {
		if (this.isJREBelow9) return;
		// Class before module descriptor in the source list
		CompilerBuilder b = new CompilerBuilder()
				.file(new File(modules_directory, "source/SimpleModules/module.one/pkg1/Cls1.java"))
				.file(new File(modules_directory, "source/SimpleModules/module.one/module-info.java"));
		b.compile();
		assertFalse(b.listener().hasDiagnostic());
	}
	public void testGetJavaFileObjects() {
		if (this.isJREBelow9) return;
	}
	public void testGetJavaFileObjects2() {
		if (this.isJREBelow9) return;
	}
	public void testSetLocationAsPaths() {
		if (this.isJREBelow9) return;
	}
	public void testContains() {
		if (this.isJREBelow9) return;
	}
	public void testGetServiceLoader() {
		if (this.isJREBelow9) return;
	}
	public void testInferModuleName() {
		if (this.isJREBelow9) return;
	}
	public void testListLocationsForModules() {
		if (this.isJREBelow9) return;
	}
	public void testAsPath() {
		if (this.isJREBelow9) return;
	}

	/*-- Code for testing bug 533830 --*/

	public void testBug533830_1() throws IOException {
		if (this.isJREBelow9) return;

		File src = createClassSource(
			"package p;\n"
		+	"public class X {}"
		);

		CompilerBuilder b = new CompilerBuilder()
			.option("--release", "8")
			.option("-source", "7")
			.file(src)
			;
		try {
			b.compile();
			fail("compilation didn't fail as expected");
		} catch(IllegalArgumentException iae) {
			assertEquals("option -source is not supported when --release is used", iae.getMessage());
		}

		//-- We must now also have a diagnostic
		assertTrue("The diagnostic listener did not receive an error for the illegal option", b.listener().hasDiagnostic("option -source is not supported when --release is used"));
	}

	/**
	 * Helps with building a compiler invocation, handling the common parts of testing.
	 */
	private final class CompilerBuilder {
		private final JavaCompiler compiler;

		private final List<String> options = new ArrayList<>();

		private final List<File> files = new ArrayList<>();

		private final StringWriter errorWriter = new StringWriter();

		private DiagListener listener;

		CompilerBuilder() {
			compiler = compilers[1];
		}

		public void compile() {
			String tmpFolder = _tmpFolder;
			StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

			Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
			PrintWriter errWriter = new PrintWriter(errorWriter);

			if(! options.contains("-d")) {
				option("-d", tmpFolder);
			}

			listener = new DiagListener(errWriter);

			ForwardingJavaFileManager<StandardJavaFileManager> forwardingManager = createFileManager(manager);
			compiler.getTask(errWriter, forwardingManager, listener, options, null, units).call();
		}

		public CompilerBuilder option(String... s) {
			options.addAll(Arrays.asList(s));
			return this;
		}

		public CompilerBuilder file(File f) {
			files.add(f);
			return this;
		}

		public DiagListener listener() {
			if(null == listener)
				throw new IllegalStateException("Call compile() before using the listener");
			return listener;
		}

		public ForwardingJavaFileManager<StandardJavaFileManager> createFileManager(StandardJavaFileManager manager) {
			ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<>(manager) {
				@Override
				public FileObject getFileForInput(Location location, String packageName, String relativeName)
						throws IOException {
					if (DEBUG) {
						System.out.println("Create file for input : " + packageName + " " + relativeName + " in location " + location);
					}
					return super.getFileForInput(location, packageName, relativeName);
				}
				@Override
				public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
						throws IOException {
					if (DEBUG) {
						System.out.println("Create java file for input : " + className + " in location " + location);
					}
					return super.getJavaFileForInput(location, className, kind);
				}
				@Override
				public JavaFileObject getJavaFileForOutput(Location location,
						String className,
						Kind kind,
						FileObject sibling) throws IOException {

					if (DEBUG) {
						System.out.println("Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
					}
					JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
					if (DEBUG) {
						System.out.println(javaFileForOutput.toUri());
					}
					return javaFileForOutput;
				}
			};
			return forwardingJavaFileManager;
		}
	}

	static private final class DiagListener extends CompilerInvocationDiagnosticListener {
		private final List<Diagnostic<? extends JavaFileObject>> errorList = new ArrayList<>();

		DiagListener(PrintWriter err) {
			super(err);
		}

		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			errorList.add(diagnostic);
			super.report(diagnostic);
		}

		public boolean hasDiagnostic() {
			return !errorList.isEmpty();
		}

		public boolean hasDiagnostic(String match) {
			for(Diagnostic<? extends JavaFileObject> d: errorList) {
				String msg = d.getMessage(Locale.US).toLowerCase();
				if(msg.contains(match.toLowerCase()))
					return true;
			}
			return false;
		}
	}

	private File createClassSource(String source) throws IOException {
		String tmpFolder = _tmpFolder;
		File inputFile = new File(tmpFolder, "X.java");
		try (Writer writer = new FileWriter(inputFile)){
			writer.write(source);
			return inputFile;
		}
	}

	/*-- end code for bug 533830 --*/

	/**
	 * Recursively delete the contents of a directory, including any subdirectories.
	 * This is not optimized to handle very large or deep directory trees efficiently.
	 * @param f is either a normal file (which will be deleted) or a directory
	 * (which will be emptied and then deleted).
	 */
	public static void deleteTree(File f) throws IOException
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
		Files.deleteIfExists(f.toPath());
	}
	/**
	 * Copy a file from one location to another, unless the destination file already exists and has
	 * the same timestamp and file size. Create the destination location if necessary. Convert line
	 * delimiters according to {@link #shouldConvertToIndependentLineDelimiter(File)}.
	 *
	 * @param src
	 *            the full path to the resource location.
	 * @param dest
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
			contents = convertToIndependentLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}
		writeFile(dest, srcBytes);
	}

	public static void writeFile(File dest, byte[] srcBytes) throws IOException {
		File destFolder = dest.getParentFile();
		Files.createDirectories(destFolder.toPath());
		// write bytes to dest
		Files.write(dest.toPath(), srcBytes);
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
	 * @param destFolder the absolute path of the destination folder
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
	protected static String getPluginDirectoryPath() throws IOException {
		if (Platform.isRunning()) {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.compiler.tool.tests").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		}
		return new File(System.getProperty("user.dir")).getAbsolutePath();
	}
	/**
	 * @return true if this file's end-of-line delimiters should be replaced with
	 * a platform-independent value, e.g. for compilation.
	 */
	public static boolean shouldConvertToIndependentLineDelimiter(File file) {
		return file.getName().endsWith(".java");
	}
	public static byte[] read(java.io.File file) throws java.io.IOException {
		return Files.readAllBytes(file.toPath());
	}

	public static String convertToIndependentLineDelimiter(String source) {
		if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
		StringBuilder buffer = new StringBuilder();
		for (int i = 0, length = source.length(); i < length; i++) {
			char car = source.charAt(i);
			if (car == '\r') {
				buffer.append('\n');
				if (i < length-1 && source.charAt(i+1) == '\n') {
					i++; // skip \n after \r
				}
			} else {
				buffer.append(car);
			}
		}
		return buffer.toString();
	}
}
