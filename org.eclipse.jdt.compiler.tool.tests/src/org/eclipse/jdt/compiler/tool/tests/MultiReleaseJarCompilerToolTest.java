/*******************************************************************************
 * Copyright (c) 2026 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vaclav Haisman - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import junit.framework.TestCase;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class MultiReleaseJarCompilerToolTest extends TestCase {
	private Path testDirectory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.testDirectory = Files.createTempDirectory("jdt-mrjar-manifest-test");
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			Util.delete(this.testDirectory.toFile());
		} finally {
			super.tearDown();
		}
	}

	public void testMissingMultiReleaseManifestHeaderMatchesJavac() throws Exception {
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		assertNotNull("No system Java compiler available", javac);

		Path jar = createJarWithoutMultiReleaseHeader(javac);
		Path source = this.testDirectory.resolve("consumer-src/X.java");
		Files.createDirectories(source.getParent());
		Files.writeString(source,
				"public class X {\n" +
				"    public static String value() {\n" +
				"        return dep.Value.TEXT;\n" +
				"    }\n" +
				"}\n");

		Path ecjOutput = this.testDirectory.resolve("ecj-output");
		compile(new EclipseCompiler(), source, ecjOutput, "9", jar, "ECJ");
		Path javacOutput = this.testDirectory.resolve("javac-output");
		compile(javac, source, javacOutput, "9", jar, "javac");

		String ecjValue = readValue(ecjOutput);
		String javacValue = readValue(javacOutput);
		assertEquals("Unexpected javac output", "root", javacValue);
		assertEquals(
				"Compiler outputs differ for a JAR without Multi-Release: true\n" +
				"javac output: " + javacValue + "\n" +
				"ECJ output: " + ecjValue,
				javacValue,
				ecjValue);
	}

	private Path createJarWithoutMultiReleaseHeader(JavaCompiler javac) throws IOException {
		Path rootSource = this.testDirectory.resolve("root-src/dep/Value.java");
		Files.createDirectories(rootSource.getParent());
		Files.writeString(rootSource,
				"package dep;\n" +
				"public class Value {\n" +
				"    public static final String TEXT = \"root\";\n" +
				"}\n");
		Path rootOutput = this.testDirectory.resolve("root-output");
		compile(javac, rootSource, rootOutput, "8", null, "javac");

		Path versionedSource = this.testDirectory.resolve("versioned-src/dep/Value.java");
		Files.createDirectories(versionedSource.getParent());
		Files.writeString(versionedSource,
				"package dep;\n" +
				"public class Value {\n" +
				"    public static final String TEXT = \"version-9\";\n" +
				"}\n");
		Path versionedOutput = this.testDirectory.resolve("versioned-output");
		compile(javac, versionedSource, versionedOutput, "9", null, "javac");

		Path jar = this.testDirectory.resolve("missing-multi-release-header.jar");
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
			addEntry(output, "dep/Value.class", rootOutput.resolve("dep/Value.class"));
			addEntry(output, "META-INF/versions/9/dep/Value.class", versionedOutput.resolve("dep/Value.class"));
		}
		return jar;
	}

	private void compile(JavaCompiler compiler, Path source, Path output, String release, Path classpath,
			String compilerName) throws IOException {
		Files.createDirectories(output);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
			List<String> options = new ArrayList<>();
			options.add("--release");
			options.add(release);
			options.add("-d");
			options.add(output.toString());
			if (classpath != null) {
				options.add("-classpath");
				options.add(classpath.toString());
			}
			Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(source.toFile());
			boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
			assertTrue(compilerName + " compilation failed:\n" + diagnosticsToString(diagnostics), success);
		}
	}

	private String diagnosticsToString(DiagnosticCollector<JavaFileObject> diagnostics) {
		StringWriter output = new StringWriter();
		try (PrintWriter writer = new PrintWriter(output)) {
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				writer.println(diagnostic);
			}
		}
		return output.toString();
	}

	private void addEntry(JarOutputStream output, String name, Path contents) throws IOException {
		output.putNextEntry(new JarEntry(name));
		Files.copy(contents, output);
		output.closeEntry();
	}

	private String readValue(Path output) throws Exception {
		try (URLClassLoader loader = new URLClassLoader(new URL[] { output.toUri().toURL() }, null)) {
			Class<?> testClass = loader.loadClass("X");
			Method method = testClass.getMethod("value");
			return (String) method.invoke(null);
		}
	}
}
