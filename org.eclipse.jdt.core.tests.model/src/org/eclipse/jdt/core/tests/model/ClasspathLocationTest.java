/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.core.builder.ClasspathJrt;
import org.eclipse.jdt.internal.core.builder.ClasspathJrtWithReleaseOption;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.junit.Test;

public class ClasspathLocationTest extends AbstractJavaModelTests {

	public ClasspathLocationTest(String name) {
		super(name);
	}

	public static junit.framework.Test suite() {
		return buildModelTestSuite(ClasspathLocationTest.class);
	}

	@Test
	public void testForJrtSystem() throws Exception {
		String javaHome = System.getProperty("java.home", null);
		assertNotNull("java.home is not defined", javaHome);
		File image = Paths.get(javaHome).toFile();
		assertTrue("java.home points to invalid path", image.isDirectory());
		String releaseVersion = JRTUtil.getJdkRelease(image);

		String jrt = "lib/" + org.eclipse.jdt.internal.compiler.util.JRTUtil.JRT_FS_JAR; //$NON-NLS-1$
		Path jrtPath = Paths.get(javaHome, jrt);
		int majorSegment = getMajorVersionSegment(releaseVersion);
		ClasspathJrt classpathJrt = ClasspathLocation.forJrtSystem(jrtPath.toString(), null, null, String.valueOf(majorSegment));
		assertEquals(ClasspathJrt.class, classpathJrt.getClass());

		int olderVersion = majorSegment - 1;

		classpathJrt = ClasspathLocation.forJrtSystem(jrtPath.toString(), null, null, String.valueOf(olderVersion));
		assertEquals(ClasspathJrtWithReleaseOption.class, classpathJrt.getClass());
	}

	private static int getMajorVersionSegment(String releaseVersion) {
		int dot = releaseVersion.indexOf('.');
		if (dot > 0) {
			return Integer.parseInt(releaseVersion.substring(0, dot));
		}
		return Integer.parseInt(releaseVersion);
	}
}
