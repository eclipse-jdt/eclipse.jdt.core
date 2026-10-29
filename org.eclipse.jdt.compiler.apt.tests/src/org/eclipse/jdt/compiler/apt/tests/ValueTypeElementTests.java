/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
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

public class ValueTypeElementTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.ValueTypeElementProcessor";

	public void testValueType() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "28", "testValueType", null, true);
	}
	public void testValueTypeJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "28", "testValueType", null, true);
	}
	protected void internalTestWithBinary(JavaCompiler compiler, String processor, String compliance, String testMethod, String testClass, boolean processBinariesAgain) throws IOException {
		if (!canRunJava28()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "value");
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("targets/value/", targetFolder);
		} else {
			BatchTestUtils.copyResource("targets/value/" + testClass, targetFolder);
		}

		List<String> options = new ArrayList<>();
		options.add("--enable-preview");
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
		BatchTestUtils.compileTreeAndProcessBinaries(compiler, options, processor, targetFolder, null);
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean canRunJava28() {
		try {
			SourceVersion.valueOf("RELEASE_28");
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
