/*******************************************************************************
 * Copyright (c) 2014, 2018 IBM Corporation and others.
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
 *     het@google.com - Bug 456986 - Bogus error when annotation processor generates annotation types.
 *                      Bug 415274 - Annotation processing throws a NPE in getElementsAnnotatedWith()
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import junit.framework.TestCase;

public class AnnotationProcessorTests extends TestCase {

	public final class DiagnosticReport<S> implements DiagnosticListener<S> {
		public int count;
		public StringBuilder buffer;
		private final List<Diagnostic<? extends S>> warnings = new ArrayList<>();
		DiagnosticReport() {
			this.count = 0;
			this.buffer = new StringBuilder();
		}
		@Override
		public void report(Diagnostic<? extends S> diagnostic) {
			if (diagnostic.getKind() ==  Diagnostic.Kind.WARNING) {
				warnings.add(diagnostic);
				count++;
				buffer.append(diagnostic.getMessage(Locale.getDefault()));
				buffer.append("\n");
			} else if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
				count++;
				buffer.append(diagnostic.getMessage(Locale.getDefault()));
				buffer.append("\n");
				System.out.println(buffer.toString());
			}
		}
		public Diagnostic<? extends S> getErrorAt(int index) {
			return warnings.get(index);
		}
		@Override
		public String toString() {
			return this.buffer.toString();
		}
		public void clear() {
			this.count = 0;
			this.buffer = new StringBuilder();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testBug443769() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug443769");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug443769", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug443769Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null);
		assertEquals(true, success);
	}

	public void testBug456986() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug456986");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug456986", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug456986Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null);
		assertEquals(true, success);
	}

	public void testBug540090() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug540090");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug540090", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug540090Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null);
		assertEquals(true, success);
	}

	public void testBug415274() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug415274");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug415274", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug415274Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null, false);
		assertEquals(true, success);
	}

	public void testBug463062() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug463062");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug463062", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug463062Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null, true);
		assertNull(System.getProperty(PROC));
	}
	public void testBug493837() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug493837");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug493837", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug493837Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		DiagnosticReport<JavaFileObject> diagnosticListener = new DiagnosticReport<>();
		BatchTestUtils.compileTree(compiler, options, targetFolder, false, diagnosticListener);
		assertNull(System.getProperty(PROC));
	}
	public void testBug340635() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug340635");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug340635", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug340635Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		DiagnosticReport<JavaFileObject> diagnosticListener = new DiagnosticReport<>();
		BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener, true);
		assertNull(System.getProperty(PROC));
		assertEquals("incorrect number of messages", 1, diagnosticListener.count);
		assertEquals("Erased type: classes.MyInterface - type arguments: \n", diagnosticListener.buffer.toString());
	}
	public void testBug471995() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug471995");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug471995", targetFolder);
		List<String> options = new ArrayList<>();
		options.add("-proc:only");
		BatchTestUtils.compileTree(compiler, options, targetFolder, null);
	}
	public void testBug317216() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug317216");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug317216", targetFolder);
		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug317216Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		DiagnosticReport<JavaFileObject> diagnosticListener = new DiagnosticReport<>();
		BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener, true);
		assertNull(System.getProperty(PROC));
		assertEquals("incorrect number of messages", 0, diagnosticListener.count);
		assertNull(System.getProperty(PROC));
	}

	public void testBug530665() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File srcFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug317216");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug530665", srcFolder);
		File binFolder = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "AnnotationProcessorTests", "bug530665");
		binFolder.mkdirs();
		File preexistsFile = new File(binFolder, "preexists.txt");
		assertTrue(preexistsFile.createNewFile());
		File nonexistsFile = new File(binFolder, "nonexists.txt");
		assertTrue(!nonexistsFile.exists());

		List<String> options = new ArrayList<>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug530665Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		DiagnosticReport<JavaFileObject> diagnosticListener = new DiagnosticReport<>();
		BatchTestUtils.compileTree(compiler, options, srcFolder, false, diagnosticListener);
		assertEquals("succeeded", System.getProperty(PROC));

		assertTrue(!preexistsFile.exists()); // deleted
		assertTrue(nonexistsFile.exists()); // rewritten
	}
}
