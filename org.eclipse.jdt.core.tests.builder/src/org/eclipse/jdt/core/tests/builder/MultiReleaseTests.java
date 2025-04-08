/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MultiReleaseTests extends BuilderTests {

	private static final int JAVA8 = 52;
	private static final int JAVA9 = 53;

	public MultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(MultiReleaseTests.class);
	}

	public void testMultiReleaseCompile() throws JavaModelException, IOException {
		IPath projectPath = whenSetupMRRpoject();
		fullBuild();
		expectingNoProblems();
		expectingMultiReleaseClasses(projectPath);
	}

	public void testMultiReleaseCompileWithMultipleFolders() throws JavaModelException, IOException {
		IPath projectPath = whenSetupMRRpoject();
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, org.eclipse.jdt.core.JavaCore.VERSION_9) };
		IPath release9Src2 = env.addPackageFragmentRoot(projectPath, "src9_2", extraAttributes);
		env.addClass(release9Src2, "p", "Release9Type",
				"""
				package p;
				public class Release9Type {

				}
				"""
		);
		fullBuild();
		expectingNoProblems();
		expectingMultiReleaseClasses(projectPath);
		assertJavaVersion(projectPath.append("bin/META-INF/versions/9/p/Release9Type.class"), JAVA9);
	}

	public void testMultiReleaseCompileWithConflict() throws JavaModelException, IOException {
		IPath projectPath = whenSetupMRRpoject();
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] {
				JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, org.eclipse.jdt.core.JavaCore.VERSION_9) };
		IPath release9Src2 = env.addPackageFragmentRoot(projectPath, "src9_2", extraAttributes);
		env.addClass(release9Src2, "p", "MultiReleaseType",
				"""
				package p;
				public class MultiReleaseType {

				}
				"""
		);
		fullBuild();
		expectingOnlySpecificProblemFor(release9Src2, new Problem("", "The type MultiReleaseType is already defined", release9Src2.append("p/MultiReleaseType.java"), 24, 40, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
	}

	private IPath whenSetupMRRpoject() throws JavaModelException {
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
		return projectPath;
	}

	private void expectingMultiReleaseClasses(IPath projectPath) throws IOException, FileNotFoundException {
		IPath defaultReleaseClass = projectPath.append("bin/p/MultiReleaseType.class");
		IPath java9ReleaseClass = projectPath.append("bin/META-INF/versions/9/p/MultiReleaseType.class");
		assertJavaVersion(defaultReleaseClass, JAVA8);
		assertJavaVersion(java9ReleaseClass, JAVA9);
	}

	private void assertJavaVersion(IPath clazz, int javaVersion) throws IOException, FileNotFoundException {
		assertEquals("Wrong java version for "+clazz, javaVersion, getMajorVersionOfClass(clazz));
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
