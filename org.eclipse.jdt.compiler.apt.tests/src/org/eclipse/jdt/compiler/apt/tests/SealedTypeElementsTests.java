/*******************************************************************************
 * Copyright (c) 2020, 2022 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import junit.framework.TestCase;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class SealedTypeElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.SealedTypeElementProcessor";

	public void test001() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test001", null, "sealed");
	}
	public void test001Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test001", null, "sealed");
	}
	public void test002() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test002", null, "sealed");
	}
	public void test002Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test002", null, "sealed");
	}
	public void test003() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test003", null, "sealed");
	}
	public void test003Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test003", null, "sealed");
	}
	public void test004() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test004", null, "sealed");
	}
	public void test004Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test004", null, "sealed");
	}
	public void test005Src() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test005Src", null, "sealed");
	}
	public void test005SrcJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test005Src", null, "sealed");
	}
	public void test005Binary() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test005Binary", null, "sealed");
	}
	public void test005BinaryJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test005Binary", null, "sealed");
	}
	public void test006() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test006", null, "sealed");
	}
	public void test006Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "17", "test006", null, "sealed");
	}

	protected void internalTest(JavaCompiler compiler, String processor, String compliance,
			String testMethod, String testClass, String resourceArea) throws IOException {
		if (!canRunJava17()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "mod_locations", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("mod_locations/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("mod_locations/" + resourceArea + "/" + testClass, targetFolder);
		}

		List<String> options = new ArrayList<>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		options.add("-processor");
		options.add(processor);
		if (compiler instanceof EclipseCompiler) {
			options.add("-" + compliance);
		} else {
			options.add("-source");
			options.add(compliance);
		}
		BatchTestUtils.compileInModuleMode(compiler, options, processor, targetFolder, null, true, true);
		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean canRunJava17() {
		try {
			SourceVersion.valueOf("RELEASE_17");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}