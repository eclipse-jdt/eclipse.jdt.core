/*******************************************************************************
 * Copyright (c) 2025 IBM Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MultiReleaseTests extends BuilderTests {

	public MultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(MultiReleaseTests.class);
	}

	public void testMultiReleaseCompile() throws JavaModelException, IOException {
		IPath projectPath = env.addProject("P", CompilerOptions.VERSION_1_8);
		env.removePackageFragmentRoot(projectPath, "");
		IPath defaultSrc = env.addPackageFragmentRoot(projectPath, "src");
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, org.eclipse.jdt.core.JavaCore.VERSION_9) };
		IPath release9Src = env.addPackageFragmentRoot(projectPath, "src9", extraAttributes);
		env.setOutputFolder(projectPath, "bin");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addClass(defaultSrc, "p", "MultiReleaseType",
				"""
				package p;
				public class MultiReleaseType {

				}
				"""
		);
		env.addClass(release9Src, "p", "MultiReleaseType",
				"""
				package p;
				public class MultiReleaseType {

				}
				"""
		);
		fullBuild();
		expectingNoProblems();
		IPath defaultReleaseClass = projectPath.append("bin/p/MultiReleaseType.class");
		IPath java9ReleaseClass = projectPath.append("bin/META-INF/versions/9/p/MultiReleaseType.class");
		expectingPresenceOf(defaultReleaseClass);
		assertEquals("Major version for release compilation is wrong", 53, getMajorVersionOfClass(java9ReleaseClass));
		assertEquals("Major version for default compilation is wrong", 52, getMajorVersionOfClass(defaultReleaseClass));
	}

	private int getMajorVersionOfClass(IPath clazz) throws IOException, FileNotFoundException {
		expectingPresenceOf(clazz);
		File classFile = env.getWorkspaceRootPath().append(clazz).toFile();
		assertNotNull(classFile);
		IClassFileReader reader;
		try (FileInputStream stream = new FileInputStream(classFile)) {
			reader = ToolFactory.createDefaultClassFileReader(stream, IClassFileReader.ALL);
		}
		return reader.getMajorVersion();
	}

}
