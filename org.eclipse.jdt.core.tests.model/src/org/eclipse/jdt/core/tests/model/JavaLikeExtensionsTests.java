/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.Test;

public class JavaLikeExtensionsTests extends ModifyingResourceTests {

	public JavaLikeExtensionsTests(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "TypeParameterBug73884" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 13 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
	}
	public static Test suite() {
		return buildModelTestSuite(JavaLikeExtensionsTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		Util.resetJavaLikeExtensions();
	}

	/*
	 * Ensures that the known Java-like extensions are correct.
	 */
	public void testGetJavaLikeExtensions01() {
		assertSortedStringsEqual(
			"Unexpected file extensions",
			"bar\n" +
			"foo\n" +
			"java\n",
			JavaCore.getJavaLikeExtensions()
		);
	}

	/*
	 * Ensures that the known Java-like extensions are correct after a Java-like file extension is added.
	 */
	public void testGetJavaLikeExtensions02() throws CoreException {
		IContentType javaContentType = Platform.getContentTypeManager().getContentType(JavaCore.JAVA_SOURCE_CONTENT_TYPE);
		try {
			if (javaContentType != null)
				javaContentType.addFileSpec("abc", IContentType.FILE_EXTENSION_SPEC);
			assertSortedStringsEqual(
				"Unexpected file extensions",
				"abc\n" +
				"bar\n" +
				"foo\n" +
				"java\n",
				JavaCore.getJavaLikeExtensions()
			);
		} finally {
			if (javaContentType != null)
				javaContentType.removeFileSpec("abc", IContentType.FILE_EXTENSION_SPEC);
		}
	}

	/*
	 * Ensure that file.foo is a Java-like file name
	 */
	public void testIJavaLikeFileName01() {
		assertTrue("file.foo should be a Java-like file name", JavaCore.isJavaLikeFileName("file.foo"));
	}

	/*
	 * Ensure that file.java is a Java-like file name
	 */
	public void testIJavaLikeFileName02() {
		assertTrue("file.java should be a Java-like file name", JavaCore.isJavaLikeFileName("file.java"));
	}

	/*
	 * Ensure that file.other is not a Java-like file name
	 */
	public void testIJavaLikeFileName03() {
		assertFalse("file.other should not be a Java-like file name", JavaCore.isJavaLikeFileName("file.other"));
	}

	/*
	 * Ensure that file is not a Java-like file name
	 */
	public void testIJavaLikeFileName04() {
		assertFalse("file should not be a Java-like file name", JavaCore.isJavaLikeFileName("file"));
	}

	/*
	 * Ensure that removing the Java-like extension for file.foo returns foo
	 */
	public void testRemoveJavaLikeExtension01() {
		assertEquals("Unexpected file without Java-like extension", "file", JavaCore.removeJavaLikeExtension("file.foo"));
	}

	/*
	 * Ensure that removing the Java-like extension for file.java returns foo
	 */
	public void testRemoveJavaLikeExtension02() {
		assertEquals("Unexpected file without Java-like extension", "file", JavaCore.removeJavaLikeExtension("file.java"));
	}

	/*
	 * Ensure that removing the Java-like extension for file.other returns foo.other
	 */
	public void testRemoveJavaLikeExtension03() {
		assertEquals("Unexpected file without Java-like extension", "file.other", JavaCore.removeJavaLikeExtension("file.other"));
	}
}
