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
}
