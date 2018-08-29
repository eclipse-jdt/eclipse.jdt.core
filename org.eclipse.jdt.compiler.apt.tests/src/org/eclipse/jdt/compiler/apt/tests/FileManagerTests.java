/*******************************************************************************
 * Copyright (c) 2007, 2015 BEA Systems, Inc. and others
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
 *    IBM Corporation - fixed a resource leak warning
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileManager;

import junit.framework.TestCase;

/**
 * Test the implementation of the Filer interface,
 * in more detail than BatchDispatchTests does.
 * @since 3.4
 */
public class FileManagerTests extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testFileManager() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpFolder, "src" + System.currentTimeMillis());
		dir.mkdirs();
		File inputFile = new File(dir, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write("public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		StandardJavaFileManager fileManager = null;
		try {
			fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
	
			List<File> fins = new ArrayList<File>();
			fins.add(dir);
	
			JavaFileManager.Location sourceLoc = javax.tools.StandardLocation.SOURCE_PATH;
			fileManager.setLocation(sourceLoc, fins);
	
			Set<JavaFileObject.Kind> fileTypes = new HashSet<JavaFileObject.Kind>();
			fileTypes.add(JavaFileObject.Kind.SOURCE);
	
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.list(sourceLoc, "", fileTypes, true);
	
			Iterator<? extends JavaFileObject> it = compilationUnits.iterator();
			StringBuilder builder = new StringBuilder();
			while (it.hasNext()) {
				JavaFileObject next = it.next();
				String name = next.getName();
				name = name.replace('\\', '/');
				int lastIndexOf = name.lastIndexOf('/');
				builder.append(name.substring(lastIndexOf + 1));
			}
			assertEquals("Wrong contents", "X.java", String.valueOf(builder));
			
			List<File> files = new ArrayList<File>();
			files.add(dir);
			try {
				fileManager.getJavaFileObjectsFromFiles(files);
				fail("IllegalArgumentException should be thrown but not");
			} catch(IllegalArgumentException iae) {
				// Do nothing
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileManager.close();
			} catch (IOException e) {
				//ignore the exception
			}
		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());
		assertTrue("delete failed", dir.delete());
	}
	// Test that JavaFileManager#inferBinaryName returns null for invalid file
	public void testInferBinaryName() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpFolder, "src" + System.currentTimeMillis());
		dir.mkdirs();
		File inputFile = new File(dir, "test.txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write("This is not a valid Java file");
			writer.flush();
			writer.close();
		} catch (IOException e) {
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
		try {
			StandardJavaFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
	
			List<File> fins = new ArrayList<File>();
			fins.add(dir);
			JavaFileManager.Location sourceLoc = javax.tools.StandardLocation.SOURCE_PATH;
			fileManager.setLocation(sourceLoc, fins);
	
			Set<JavaFileObject.Kind> fileTypes = new HashSet<JavaFileObject.Kind>();
			fileTypes.add(JavaFileObject.Kind.OTHER);

			Iterable<? extends JavaFileObject> compilationUnits = fileManager.list(sourceLoc, "", fileTypes, true);
			JavaFileObject invalid = null;
			for (JavaFileObject javaFileObject : compilationUnits) {
				invalid = javaFileObject;
				break;
			}
			String inferredName = fileManager.inferBinaryName(sourceLoc, invalid);
			fileManager.close();
			assertNull("Should return null for invalid file", inferredName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("delete failed", inputFile.delete());
		assertTrue("delete failed", dir.delete());
	}

	public void testBug460085() {
		if (isOnJRE9()) return;
		try {
			boolean found = false;
			EclipseFileManager fileManager = null;
			fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
			Iterable <? extends File> files = fileManager.getLocation(javax.tools.StandardLocation.PLATFORM_CLASS_PATH);
			Iterator<? extends File> iter = files.iterator();
			while (iter.hasNext()) {
				File f = iter.next();
				if ("rt.jar".equals(f.getName())) {
					found = true;
					break;
				}
			}
			fileManager.close();
			assertTrue("rt.jar not found", found);
		} catch (IOException e) {
			fail(e.getMessage());
		} 
	}
	private boolean isOnJRE9() {
		try {
			SourceVersion.valueOf("RELEASE_9");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}

	public void testBug466878_getResource_defaultPackage() throws Exception {
		EclipseFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
		List<File> classpath = new ArrayList<>();
		classpath.add(new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/classes"));
		classpath.add(new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/dependency.zip"));
		fileManager.setLocation(javax.tools.StandardLocation.CLASS_PATH, classpath);
		assertNotNull(fileManager.getFileForInput(javax.tools.StandardLocation.CLASS_PATH, "", "dirresource.txt"));
		assertNotNull(fileManager.getFileForInput(javax.tools.StandardLocation.CLASS_PATH, "", "jarresource.txt"));
		fileManager.close();
	}

	public void testBug514121_getClassloader_close() throws Exception {
		EclipseFileManager fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
		List<File> classpath = new ArrayList<>();
		classpath.add(new File(BatchTestUtils.getPluginDirectoryPath(), "resources/targets/filemanager/dependency.zip"));
		fileManager.setLocation(javax.tools.StandardLocation.ANNOTATION_PROCESSOR_PATH, classpath);
		URLClassLoader loader = (URLClassLoader) fileManager
				.getClassLoader(javax.tools.StandardLocation.ANNOTATION_PROCESSOR_PATH);
		assertNotNull(loader.findResource("jarresource.txt")); // sanity check
		fileManager.close();
		assertNull(loader.findResource("jarresource.txt")); // assert the classloader is closed
	}
}
