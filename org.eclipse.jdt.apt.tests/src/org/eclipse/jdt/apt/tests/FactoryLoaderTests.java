/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.AnnotationProcessorFactoryLoader;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorTestCodeExample;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestCodeExample;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class FactoryLoaderTests extends APTTestBase {

	private File _extJar; // external annotation jar
	private IPath _extVarJar; // external annotation jar, as a classpath-var-relative path
	private IPath _projectPath; // initialized in setUp(), cleared in tearDown()

	private final static String TEMPJARDIR_CPVAR = "FACTORYLOADERTEST_TEMP"; //$NON-NLS-1$

	public FactoryLoaderTests(String name)
	{
		super( name );
	}

	public static Test suite() {
		return new TestSuite( FactoryLoaderTests.class );
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		_projectPath = env.getProject( getProjectName() ).getFullPath();
		_extJar = TestUtil.createAndAddExternalAnnotationJar(
				env.getJavaProject( _projectPath ));

		// Create a classpath variable for the same jar file, so we can
		// refer to it that way.
		File canonicalJar = _extJar.getCanonicalFile();
		IPath jarDir = new Path( canonicalJar.getParent() );
		String extJarName = canonicalJar.getName();
		IPath varPath = new Path( TEMPJARDIR_CPVAR );
		_extVarJar = varPath.append( extJarName );
		JavaCore.setClasspathVariable( TEMPJARDIR_CPVAR, jarDir, null );

		IPath srcRoot = getSourcePath();
		String code = LoaderTestCodeExample.CODE;
		env.addClass(srcRoot, LoaderTestCodeExample.CODE_PACKAGE, LoaderTestCodeExample.CODE_CLASS_NAME, code);

		code = ColorTestCodeExample.CODE;
		env.addClass(srcRoot, ColorTestCodeExample.CODE_PACKAGE, ColorTestCodeExample.CODE_CLASS_NAME, code);
	}

	public void testExternalJarLoader() throws Exception {
		LoaderTestAnnotationProcessor.clearLoaded();
		IProject project = env.getProject( getProjectName() );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());

		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);

		// add _extJar to the factory list as an external jar, and rebuild.
		ifp.addExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);

		// rebuild and verify that the processor was loaded
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());

		// Verify that we were able to run the ColorAnnotationProcessor successfully
		assertTrue(ColorAnnotationProcessor.wasSuccessful());

		// restore to the original
		ifp.removeExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);

		// rebuild and verify that the processor was not loaded.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());

		// add _extJar to the factory list as a class-path-relative jar, and rebuild.
		ifp.addVarJar(_extVarJar);
		AptConfig.setFactoryPath(jproj, ifp);

		// rebuild and verify that the processor was loaded
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());

		// restore to the original
		ifp.removeVarJar(_extVarJar);
		AptConfig.setFactoryPath(jproj, ifp);

		// rebuild and verify that the processor was not loaded.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
	}

	// Test what happens when the factory path contains a jar file that can't be found.
	public void testNonexistentEntry() throws Exception {
		LoaderTestAnnotationProcessor.clearLoaded();
		IProject project = env.getProject( getProjectName() );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());

		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);

		// add bogus entry to factory list, and rebuild.
		File bogusJar = new File("bogusJar.jar"); // assumed to not exist
		ifp.addExternalJar(bogusJar);

		// verify that a problem marker was added.
		AptConfig.setFactoryPath(jproj, ifp);
		fullBuild( project.getFullPath() );
		IMarker[] markers = getAllAPTMarkers(_projectPath);
		assertEquals(1, markers.length);
		assertEquals(AptPlugin.APT_LOADER_PROBLEM_MARKER, markers[0].getType());
		String message = markers[0].getAttribute(IMarker.MESSAGE, "");
		assertTrue(message.contains("bogusJar.jar"));

		// remove bogus entry, add _extJar to the factory list as an external jar, and rebuild.
		ifp.removeExternalJar(bogusJar);
		ifp.addExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);

		// rebuild and verify that the processor was loaded and the problems were removed.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());

		// Verify that we were able to run the ColorAnnotationProcessor successfully
		assertTrue(ColorAnnotationProcessor.wasSuccessful());

		// restore to the original
		AptConfig.setFactoryPath(jproj, ifp);
	}

	// Test that the jars of the factory path are not locked: they are loaded from a private
	// copy, which is deleted when the class loader is discarded.
	public void testExternalJarIsLoadedFromCopy() throws Exception {
		IProject project = env.getProject( getProjectName() );
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);
		ifp.addExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);
		// The change of the factory path discards the class loaders when the build events
		// are sent, which is done by a job: let it run now and not after the build.
		env.waitForAutoBuild();

		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());
		assertFalse("The jar should be loaded from a copy", findCopies(_extJar).isEmpty());

		// On Windows this fails if the jar is still held by the class loader
		File savedJar = new File(_extJar.getPath() + ".saved");
		Files.copy(_extJar.toPath(), savedJar.toPath());
		try {
			Files.delete(_extJar.toPath());
		} finally {
			Files.move(savedJar.toPath(), _extJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// discarding the class loaders deletes the copies
		AnnotationProcessorFactoryLoader.getLoader().resetAll();
		assertEquals("The copies should be deleted", Collections.emptyList(), findCopies(_extJar));

		// and the processor can be loaded again
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());
		assertFalse(findCopies(_extJar).isEmpty());

		// deleting the project deletes the copies too
		env.removeProject( project.getFullPath() );
		assertEquals("The copies should be deleted", Collections.emptyList(), findCopies(_extJar));
	}

	// Test that threads which load the factories of a project at the same time end up
	// with the same class loaders, and that the redundant ones are closed.
	public void testConcurrentLoadCreatesOneClassLoader() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		// a batch mode entry: its class loader is created by each load of the factories
		FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
		fp.addEntryToHead(FactoryPathUtil.newExtJarFactoryContainer(_extJar), true, true);
		AptConfig.setFactoryPath(jproj, fp);
		env.waitForAutoBuild();
		AnnotationProcessorFactoryLoader loader = AnnotationProcessorFactoryLoader.getLoader();
		loader.resetAll();
		assertEquals(Collections.emptyList(), findCopies(_extJar));

		int threadCount = 8;
		CyclicBarrier start = new CyclicBarrier(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		try {
			List<Future<Set<ClassLoader>>> results = new ArrayList<>();
			for (int i = 0; i < threadCount; i++) {
				results.add(executor.submit(() -> {
					start.await();
					Set<ClassLoader> classLoaders = new HashSet<>();
					for (AnnotationProcessorFactory factory : loader.getJava5FactoriesForProject(jproj)) {
						// ignore the factories that are contributed by plugins
						if (factory.getClass().getClassLoader() instanceof URLClassLoader classLoader) {
							classLoaders.add(classLoader);
						}
					}
					return classLoaders;
				}));
			}
			Set<ClassLoader> classLoaders = new HashSet<>();
			for (Future<Set<ClassLoader>> result : results) {
				classLoaders.addAll(result.get());
			}
			assertEquals("All threads should use the same class loader", 1, classLoaders.size());
		} finally {
			executor.shutdownNow();
		}
		assertEquals("The redundant class loaders should be closed", 1, findCopies(_extJar).size());

		loader.resetAll();
		assertEquals("The copies should be deleted", Collections.emptyList(), findCopies(_extJar));
	}

	// Test that the copies are deleted when the plugin stops
	public void testShutdownDeletesCopies() throws Exception {
		IProject project = env.getProject( getProjectName() );
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);
		ifp.addExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);
		env.waitForAutoBuild();

		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(findCopies(_extJar).isEmpty());

		AnnotationProcessorFactoryLoader.shutdown();
		assertEquals("The copies should be deleted", Collections.emptyList(), findCopies(_extJar));
	}

	/**
	 * @return the copies of the given jar that the factory loader created to load it
	 */
	private static List<java.nio.file.Path> findCopies(File jar) throws IOException {
		java.nio.file.Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
		List<java.nio.file.Path> copies = new ArrayList<>();
		try (DirectoryStream<java.nio.file.Path> copyDirs = Files.newDirectoryStream(tempDir, "jdt-apt-*")) {
			for (java.nio.file.Path copyDir : copyDirs) {
				try (DirectoryStream<java.nio.file.Path> files = Files.newDirectoryStream(copyDir, "*_" + jar.getName())) {
					files.forEach(copies::add);
				} catch (NoSuchFileException | NotDirectoryException e) {
					// deleted in the meantime, or not ours
				}
			}
		}
		return copies;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.builder.Tests#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		JavaCore.removeClasspathVariable( TEMPJARDIR_CPVAR, null );
		_extJar = null;
		_extVarJar = null;
		_projectPath = null;
		super.tearDown();
	}


}
