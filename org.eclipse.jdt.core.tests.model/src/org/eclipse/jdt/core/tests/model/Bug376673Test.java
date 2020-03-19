/*******************************************************************************
 * Copyright (c) 2018 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;

import junit.framework.Test;

public class Bug376673Test extends ModifyingResourceTests {

	public Bug376673Test(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(Bug376673Test.class);
	}

	/**
     * An extra test for bug 413114 in the context of bug 376673.
     * We want to know whether we can compile with when using a class from a jar as created in
     * {@link org.eclipse.jdt.core.tests.model.JavaSearchBugsTests2#testBug376673e()}.
	 */
	public void testBug376673() throws Exception {
		try {
			if ("macosx".equals(System.getProperty("osgi.os"))) {
				return;
			}
			IJavaProject p = createJavaProject("P", new String[] { "src" }, new String[] { "/P/lib376673.jar", "JCL17_LIB" }, "bin", "1.7");

			org.eclipse.jdt.core.tests.util.Util.createJar(
					new String[] { "p\uD842\uDF9F/i\uD842\uDF9F/Test.java",
							"package p\uD842\uDF9F.i\uD842\uDF9F;\n" + "public class Test{}\n" },
					p.getProject().getLocation().append("lib376673.jar").toOSString(), "1.7");

			createFolder("/P/src/pkg");
			String[] classFileContent = new String[] {
					"package pkg;",
					"class UseJarClass {",
					"	public p\uD842\uDF9F.i\uD842\uDF9F.Test test;",
					"}",
			};
			IFile file = createFile("/P/src/pkg/UseJarClass.java", String.join(System.lineSeparator(), classFileContent), "UTF-8");
			file.setCharset("UTF-8", null);
			refresh(p);
			waitForAutoBuild();
			waitUntilIndexesReady();
			IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
			List<String> errors = new ArrayList<>();
			StringBuilder markersToString = new StringBuilder();
			for (IMarker marker : markers) {

				Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
				String message = (String) marker.getAttribute(IMarker.MESSAGE);
				if (severity.intValue() == IMarker.SEVERITY_ERROR) {
					errors.add(message);
				}

				markersToString.append("Marker with severity: ");
				markersToString.append(severity);
				markersToString.append(", and message: ");
				markersToString.append(message);
				markersToString.append(", at location: ");
				markersToString.append(marker.getAttribute(IMarker.LOCATION));
				markersToString.append(", at line: ");
				markersToString.append(marker.getAttribute(IMarker.LINE_NUMBER));
			}
			assertEquals("expected no markers on test project, all markers are:" + System.lineSeparator() + markersToString,
					Collections.emptyList(), errors);
		} finally {
			deleteProject("P");
		}
	}
}
