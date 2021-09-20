/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.tool.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.Test;

import junit.framework.TestCase;

public class InMemoryCompilationTest extends TestCase {
	@Test
	public void testInMemoryCompilationStaticMethod()
			throws ReflectiveOperationException, IllegalArgumentException, SecurityException {
		String absClassName = "my.pkg.MyClass";
		String sourceCode = "package my.pkg;" + //
				"public class MyClass {" + //
				"    public static String getText() {" + //
				"        return \"Hello world\";" + //
				"    }" + //
				"}";

		Class<?> compiledClass = compile(absClassName, sourceCode);
		Method method = compiledClass.getMethod("getText");
		Object actual = method.invoke(null);
		assertEquals("Hello world", actual);
	}

	@Test
	public void testInMemoryCompilationInheritedMethod()
			throws ReflectiveOperationException, IllegalArgumentException, SecurityException {
		String absClassName = "my.pkg.MyClass";
		String sourceCode = "package my.pkg;" + //
				"public class MyClass implements java.util.function.Supplier<String> {" + //
				"    public String get() {" + //
				"        return \"Hello world\";" + //
				"    }" + //
				"}";

		Class<?> compiledClass = compile(absClassName, sourceCode);
		@SuppressWarnings("unchecked")
		Supplier<String> instance = (Supplier<String>) compiledClass.getDeclaredConstructor().newInstance();
		String actual = instance.get();
		assertEquals("Hello world", actual);
	}

	@Test
	public void testBug574449()
			throws ReflectiveOperationException, IllegalArgumentException, SecurityException {
		testInMemoryCompilationStaticMethod();
		File file = new File("/my/pkg/");
		assertEquals(false, file.isDirectory());
	}

	private Class<?> compile(String absClassName, String sourceCode) throws ClassNotFoundException {
		InMemoryJavaSourceFileObject sourceFileObject = new InMemoryJavaSourceFileObject(absClassName, sourceCode);
		List<InMemoryJavaSourceFileObject> sources = Arrays.asList(sourceFileObject);

		JavaCompiler compiler = new EclipseCompiler();
		DiagnosticListener<JavaFileObject> diagnosticListener = new StdErrDiagnosticListener();
		JavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticListener, Locale.US,
				StandardCharsets.UTF_8);
		InMemoryClassLoader classLoader = new InMemoryClassLoader();
		InMemoryJavaFileManager inMemoryfileManager = new InMemoryJavaFileManager(standardFileManager, sources,
				classLoader);

		Writer stdErrWriter = new PrintWriter(System.err);
		List<String> options = new ArrayList<>();
		options.add("-source");
		options.add("1.8");
		options.add("-target");
		options.add("1.8");
		CompilationTask task = compiler.getTask(stdErrWriter, inMemoryfileManager, diagnosticListener, options, null,
				sources);

		Boolean result = task.call();
		assertTrue(result);

		return classLoader.loadClass(absClassName);
	}

	private static class InMemoryJavaSourceFileObject extends SimpleJavaFileObject {
		private final String sourceCode;
		private final String absClassName;

		public InMemoryJavaSourceFileObject(String absClassName, String sourceCode) {
			super(URI.create("memory:///" + absClassName.replace('.', '/') + ".java"), Kind.SOURCE);
			this.sourceCode = sourceCode;
			this.absClassName = absClassName;
		}

		public String getAbsClassName() {
			return absClassName;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return sourceCode;
		}

		@Override
		public InputStream openInputStream() throws IOException {
			return new ByteArrayInputStream(sourceCode.getBytes(StandardCharsets.UTF_8));
		}
	}

	private static class InMemoryJavaClassFileObject extends SimpleJavaFileObject {
		private ByteArrayOutputStream byteCode;

		public InMemoryJavaClassFileObject(String absClassName) {
			super(URI.create("memory:///" + absClassName.replace('.', '/') + ".class"), Kind.CLASS);
		}

		@Override
		public OutputStream openOutputStream() throws IOException {
			byteCode = new ByteArrayOutputStream();
			return byteCode;
		}

		public byte[] getByteCode() {
			return byteCode.toByteArray();
		}
	}

	private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
		private final List<InMemoryJavaSourceFileObject> sources;
		private final InMemoryClassLoader classLoader;

		protected InMemoryJavaFileManager(JavaFileManager fileManager, List<InMemoryJavaSourceFileObject> sources,
				InMemoryClassLoader classLoader) {
			super(fileManager);
			this.sources = sources;
			this.classLoader = classLoader;
		}

		public boolean hasLocation(Location location) {
			if (location == StandardLocation.SOURCE_PATH) {
				return true;
			}
			return super.hasLocation(location);
		}

		public boolean contains(Location location, FileObject fo) throws IOException {
			if (location == StandardLocation.SOURCE_PATH) {
				return sources.contains(fo);
			}
			return super.contains(location, fo);
		}

		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
				throws IOException {
			List<JavaFileObject> result = new ArrayList<>();
			if (location == StandardLocation.SOURCE_PATH && kinds.contains(Kind.SOURCE)) {
				result.addAll(sources);
			}
			if (super.hasLocation(location)) {
				Iterable<JavaFileObject> superResult = super.list(location, packageName, kinds, recurse);
				for (JavaFileObject fileObject : superResult) {
					result.add(fileObject);
				}
			}
			return result;
		}

		@Override
		public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
			if (location == StandardLocation.SOURCE_PATH && kind == Kind.SOURCE) {
				for (InMemoryJavaSourceFileObject source : sources) {
					if (source.getAbsClassName().equals(className)) {
						return source;
					}
				}
			}
			if (super.hasLocation(location)) {
				return super.getJavaFileForInput(location, className, kind);
			}
			return null;
		}

		public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind,
				FileObject sibling) throws IOException {
			InMemoryJavaClassFileObject fileObject = new InMemoryJavaClassFileObject(name);
			classLoader.add(name, fileObject);
			return fileObject;
		}
	}

	private static class InMemoryClassLoader extends ClassLoader {
		public final Map<String, InMemoryJavaClassFileObject> classes = new HashMap<>();

		public void add(String name, InMemoryJavaClassFileObject fileObject) {
			classes.put(name.replace('/', '.'), fileObject);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			InMemoryJavaClassFileObject fileObject = classes.get(name);
			if (fileObject != null) {
				byte[] byteCode = fileObject.getByteCode();
				return defineClass(name, byteCode, 0, byteCode.length);
			}
			return super.findClass(name);
		}
	}

	private static class StdErrDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			System.err.println(diagnostic.getMessage(null));
		}
	}

}
