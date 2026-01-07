/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation.
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

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import junit.framework.TestCase;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class ModuleImportTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.ModuleImportProcessor";

	public void test001() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "24", "test001", null, "java24", true, false);
	}
	public void test002() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "24", "test002", null, "java24_1", false, false);
	}
	public void test003() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "24", "test003", null, "java24_1", true, false);
	}
	public void test004() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "24", "test002", null, "java24", false, true);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void internalTest(JavaCompiler compiler, String processor, String compliance, String testMethod, String testClass, String resourceArea,
				boolean setModuleSourcePath, boolean compilationFails) throws IOException {
		if (!canRunJava24()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "mod_locations", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("mod_locations/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("mod_locations/" + resourceArea + "/" + testClass, targetFolder);
		}

		List<String> options = new ArrayList<>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		options.add("-processor");
		options.add(processor);
		// Javac 1.8 doesn't (yet?) support the -1.8 option
		if (compiler instanceof EclipseCompiler) {
			options.add("-" + compliance);
		} else {
			options.add("-source");
			options.add(compliance);
		}
		compileInModuleMode(compiler, options, processor, targetFolder, new DiagnosticListener() {
			@Override
			public void report(Diagnostic d) {
				if (d.getKind() == Diagnostic.Kind.ERROR) {
					System.err.println("Compilation error: " + d.getMessage(Locale.getDefault()));
				}
			}
		}, true, setModuleSourcePath, compilationFails);
	}
	public static void addModuleSourcePath(StandardJavaFileManager fileManager, String folder) throws IOException {
		File moduleLoc = new File(folder);
		File[] list = moduleLoc.listFiles(pathname -> pathname.isDirectory());
		for (File file : list) {
			String modName = file.getName();
			fileManager.setLocationForModule(
					StandardLocation.MODULE_SOURCE_PATH,
					modName,
					Collections.singleton(Paths.get(folder, modName)));
		}
	}
	public static void addModuleProcessorPathToCompiler(StandardJavaFileManager fileManager) throws IOException {
		File procLoc = new File(BatchTestUtils._jls8ProcessorJarPath);
		List<File> singletonList = Collections.singletonList(procLoc);
		fileManager.setLocation(StandardLocation.CLASS_PATH, singletonList);
		fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, singletonList);
	}
	public static void compileInModuleMode(JavaCompiler compiler, List<String> options, String processor,
			File targetFolder, DiagnosticListener<? super JavaFileObject> listener, 
			boolean multiModule, boolean setModuleSourcePath, boolean compilationFails) throws IOException {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		// create new list containing inputfile
		List<File> files = new ArrayList<>();
		BatchTestUtils.findFilesUnder(targetFolder, files);
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
		copyOptions.add(BatchTestUtils._tmpBinFolderName);
		copyOptions.add("-s");
		copyOptions.add(BatchTestUtils._tmpGenFolderName);
		addModuleProcessorPathToCompiler(manager);
		if (setModuleSourcePath)
			addModuleSourcePath(manager, targetFolder.getAbsolutePath());
		copyOptions.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, manager, listener, copyOptions, null, units);
		Boolean result = task.call();
		String errorOutput = stringWriter.getBuffer().toString();
		if (!result.booleanValue() || errorOutput.length() > 0) {
			if (!compilationFails) {
				System.err.println("Compilation failed: " + errorOutput);
				junit.framework.TestCase.assertTrue("Compilation failed : " + errorOutput, false);
			}
		} else {
			if (compilationFails) {
				junit.framework.TestCase.assertTrue("Compilation succeeded but was expected to fail", false);
			}
			junit.framework.TestCase.assertEquals("succeeded", System.getProperty(processor));
		}
	}
	public boolean canRunJava24() {
		try {
			SourceVersion.valueOf("RELEASE_24");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
