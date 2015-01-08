/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     het@google.com - Bug 456986 - Bogus error when annotation processor generates annotation types.
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;

public class AnnotationProcessorTests extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testBug443769() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug443769");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug443769", targetFolder);
		List<String> options = new ArrayList<String>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug443769Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null);
		assertEquals(true, success);
	}

	public void testBug456986() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "AnnotationProcessorTests", "bug456986");
		BatchTestUtils.copyResources("targets/AnnotationProcessorTests/bug456986", targetFolder);
		List<String> options = new ArrayList<String>();
		final String PROC = "org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests.Bug456986Proc";
		options.add("-processorpath");
		options.add(" ");
		options.add("-processor");
		options.add(PROC);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null);
		assertEquals(true, success);
	}
}