/*******************************************************************************
 * Copyright (c) 2026 Hélios GILLES and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Hélios GILLES - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;

/**
 * Tests for the class loaders that {@link AnnotationProcessorFactoryLoader} creates for the
 * factory path: jars are loaded from private copies, so that the original files are not locked.
 */
public class FactoryClassLoaderTests extends TestCase {

	private Path _dir; // holds the "original" factory path entries
	private final List<ClassLoader> _loaders = new ArrayList<>();

	public FactoryClassLoaderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FactoryClassLoaderTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = Files.createTempDirectory("FactoryClassLoaderTests"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		for (ClassLoader loader : _loaders) {
			((Closeable) loader).close();
		}
		_loaders.clear();
		try (Stream<Path> paths = Files.walk(_dir)) {
			paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
		}
		super.tearDown();
	}

	public void testJarIsLoadedFromPrivateCopy() throws Exception {
		File jar = createJar("processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(jar);

		File loaded = getLoadedJar(loader, "a.txt"); //$NON-NLS-1$
		assertFalse("The original jar must not be loaded", jar.getCanonicalFile().equals(loaded.getCanonicalFile())); //$NON-NLS-1$
		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$

		// On Windows this fails if the class loader holds the original jar
		Files.delete(jar.toPath());
		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testOriginalJarCanBeReplaced() throws Exception {
		File jar = createJar("processor.jar", null, "a.txt", "old content"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(jar);
		assertEquals("old content", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$

		// On Windows this fails if the class loader holds the original jar
		createJar("processor.jar", null, "a.txt", "new content"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// the existing class loader is not affected, a new one sees the new jar
		assertEquals("old content", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("new content", read(createClassLoader(jar), "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCloseDeletesCopies() throws Exception {
		File jar = createJar("processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(jar);
		File copy = getLoadedJar(loader, "a.txt"); //$NON-NLS-1$
		File copyDir = copy.getParentFile();
		// read through the class loader, so that it actually holds the copy open
		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(copy.isFile());

		((Closeable) loader).close();

		assertFalse("The copy should be deleted on close", copy.exists()); //$NON-NLS-1$
		assertFalse("The copy directory should be deleted on close", copyDir.exists()); //$NON-NLS-1$
		assertTrue("The original jar must be left alone", jar.isFile()); //$NON-NLS-1$
	}

	public void testJarsWithSameName() throws Exception {
		File jar1 = createJar("one/processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File jar2 = createJar("two/processor.jar", null, "b.txt", "content of b"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(jar1, jar2);

		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("content of b", read(loader, "b.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse(jar1.getCanonicalFile().equals(getLoadedJar(loader, "a.txt").getCanonicalFile())); //$NON-NLS-1$
		assertFalse(jar2.getCanonicalFile().equals(getLoadedJar(loader, "b.txt").getCanonicalFile())); //$NON-NLS-1$
	}

	public void testOrderIsPreserved() throws Exception {
		File jar1 = createJar("first.jar", null, "a.txt", "from first"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File jar2 = createJar("second.jar", null, "a.txt", "from second"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		assertEquals("from first", read(createClassLoader(jar1, jar2), "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("from second", read(createClassLoader(jar2, jar1), "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testFolderEntry() throws Exception {
		Path folder = Files.createDirectories(_dir.resolve("classes")); //$NON-NLS-1$
		Files.write(folder.resolve("c.txt"), "content of c".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
		File jar = createJar("processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(folder.toFile(), jar);

		assertEquals("content of c", read(loader, "c.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$

		((Closeable) loader).close();
		assertTrue("A folder entry must be left alone", Files.isRegularFile(folder.resolve("c.txt"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testMissingEntry() throws Exception {
		File missing = _dir.resolve("missing.jar").toFile(); //$NON-NLS-1$
		File jar = createJar("processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(missing, jar);

		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse(jar.getCanonicalFile().equals(getLoadedJar(loader, "a.txt").getCanonicalFile())); //$NON-NLS-1$
	}

	public void testFileThatIsNotAJar() throws Exception {
		File notAJar = _dir.resolve("broken.jar").toFile(); //$NON-NLS-1$
		Files.write(notAJar.toPath(), "this is not a jar".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$
		File jar = createJar("processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(notAJar, jar);

		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse(jar.getCanonicalFile().equals(getLoadedJar(loader, "a.txt").getCanonicalFile())); //$NON-NLS-1$
	}

	/**
	 * A jar may refer to other jars with a relative Class-Path in its manifest. They must
	 * still be found, although they are not on the factory path.
	 */
	public void testManifestClassPath() throws Exception {
		createJar("lib/dependency.jar", null, "dep.txt", "content of dep"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File jar = createJar("processor.jar", "lib/dependency.jar", "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		File other = createJar("other.jar", null, "b.txt", "content of b"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ClassLoader loader = createClassLoader(jar, other);

		assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("content of dep", read(loader, "dep.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		// the other jars are still loaded from a copy
		assertFalse(other.getCanonicalFile().equals(getLoadedJar(loader, "b.txt").getCanonicalFile())); //$NON-NLS-1$
	}

	/**
	 * The copy can be disabled, for processors that locate files relative to their own jar.
	 */
	public void testCopyCanBeDisabled() throws Exception {
		File jar = createJar("processor.jar", null, "a.txt", "content of a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		System.setProperty(AnnotationProcessorFactoryLoader.COPY_FACTORY_JARS_PROPERTY, "false"); //$NON-NLS-1$
		try {
			ClassLoader loader = createClassLoader(jar);
			assertEquals(jar.getCanonicalFile(), getLoadedJar(loader, "a.txt").getCanonicalFile()); //$NON-NLS-1$
			assertEquals("content of a", read(loader, "a.txt")); //$NON-NLS-1$ //$NON-NLS-2$

			((Closeable) loader).close();
			assertTrue("The original jar must be left alone", jar.isFile()); //$NON-NLS-1$
		} finally {
			System.clearProperty(AnnotationProcessorFactoryLoader.COPY_FACTORY_JARS_PROPERTY);
		}
		// and it is enabled by default
		assertFalse(jar.getCanonicalFile().equals(getLoadedJar(createClassLoader(jar), "a.txt").getCanonicalFile())); //$NON-NLS-1$
	}

	private ClassLoader createClassLoader(File... files) {
		ClassLoader loader = AnnotationProcessorFactoryLoader.createClassLoader(List.of(files), null);
		_loaders.add(loader);
		return loader;
	}

	/**
	 * Creates a jar below the test directory, replacing it if it exists.
	 * @param classPath the Class-Path of the manifest, or null for no manifest
	 */
	private File createJar(String path, String classPath, String entryName, String content) throws IOException {
		Path jar = _dir.resolve(path);
		Files.createDirectories(jar.getParent());
		try (OutputStream out = Files.newOutputStream(jar);
				JarOutputStream jarOut = classPath == null ? new JarOutputStream(out) : new JarOutputStream(out, manifest(classPath))) {
			jarOut.putNextEntry(new ZipEntry(entryName));
			jarOut.write(content.getBytes(StandardCharsets.UTF_8));
			jarOut.closeEntry();
		}
		return jar.toFile();
	}

	private static Manifest manifest(String classPath) {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); //$NON-NLS-1$
		manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classPath);
		return manifest;
	}

	private static String read(ClassLoader loader, String resource) throws IOException {
		URL url = loader.getResource(resource);
		assertNotNull("Resource not found: " + resource, url); //$NON-NLS-1$
		// no caching: a cached connection would keep the jar open after the class loader is closed
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		try (InputStream in = connection.getInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * @return the jar from which the class loader loads the given resource
	 */
	private static File getLoadedJar(ClassLoader loader, String resource) throws Exception {
		URL url = loader.getResource(resource);
		assertNotNull("Resource not found: " + resource, url); //$NON-NLS-1$
		assertEquals("jar", url.getProtocol()); //$NON-NLS-1$
		return new File(((JarURLConnection) url.openConnection()).getJarFileURL().toURI());
	}
}
