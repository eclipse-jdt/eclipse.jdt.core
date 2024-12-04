/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.javac;

import java.io.File;
import java.nio.file.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.junit.Test;

public class CompilerTests extends AbstractJavaModelTests {

	public CompilerTests() {
		super(CompilerTests.class.getName());
	}

	@Test
	public void testProceedOnErrors() throws CoreException {
		IJavaProject javaProject = createJavaProject("A", new String[]{"src"}, "bin");
		createFile("A/src/A.java", """
			public class A {
				void syntaxError() {
					System.err.println("Correct statement pre-error");
					syntaxError
				}
				void resolutionError() {
					System.err.println("Correct statement pre-error");
					this.o = 1;
					System.err.println("Correct statement post-error");
				}
			""");
		javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		IFile classFile = javaProject.getProject().getFolder("bin").getFile("A.class");
		assertTrue(classFile.exists());
	}

	@Test
	public void testSwitchExpression() throws Exception {
		IJavaProject javaProject = createJava21Project("A");
		createFile("A/src/SwitchExpr.java", """
			import java.time.Month;
			public class SwitchExpr {
				public static void main(String[] args) {
					Month opt = Month.JANUARY;
					int n = switch (opt) {
						case JANUARY -> 1;
						case FEBRUARY -> 2;
						default -> 3;
					};
					System.err.println(n);
				}
			}
			""");
		javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		IFile classFile = javaProject.getProject().getParent().getFolder(javaProject.getOutputLocation()).getFile("SwitchExpr.class");
		assertTrue(classFile.exists());
		//
		File tmpOutput = File.createTempFile("output", "txt");
		new ProcessBuilder().directory(classFile.getParent().getLocation().toFile())
			.command(ProcessHandle.current().info().command().orElse(""), "SwitchExpr")
			.redirectError(tmpOutput)
			.start()
			.waitFor();
		var lines = Files.readAllLines(tmpOutput.toPath());
		tmpOutput.delete();
		assertEquals("1", lines.get(0));
	}
}
