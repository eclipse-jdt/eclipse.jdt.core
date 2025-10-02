/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation.
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
import java.util.Locale;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import junit.framework.TestCase;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class Java22ElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.Java22ElementProcessor";

	public void testGetEnumConstantBody01Ecj() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "22", "testGetEnumConstantBody01", null, "java22", false);
	}
	public void testGetEnumConstantBody01Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "22", "testGetEnumConstantBody01", null, "java22", false);
	}
	public void testGetEnumConstantBody02Ecj() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "22", "testGetEnumConstantBody02", null, "java22", false);
	}
	public void testGetEnumConstantBody02Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "22", "testGetEnumConstantBody02", null, "java22", false);
	}
	public void testGetEnumConstantBody03Ecj() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "22", "testGetEnumConstantBody03", null, "java22", false);
	}
	public void testGetEnumConstantBody03Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "22", "testGetEnumConstantBody03", null, "java22", false);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void internalTestWithBinary(JavaCompiler compiler, String processor, String compliance, String testMethod, String testClass, String resourceArea,
				boolean processBinariesAgain) throws IOException {
		if (!canRunJava22()) {
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
		// Javac 1.8 doesn't (yet?) support the -1.8 option
		if (compiler instanceof EclipseCompiler) {
			options.add("-" + compliance);
		} else {
			options.add("-source");
			options.add(compliance);
		}
		BatchTestUtils.compileInModuleMode(compiler, options, processor, targetFolder, new DiagnosticListener() {
			@Override
			public void report(Diagnostic d) {
				if (d.getKind() == Diagnostic.Kind.ERROR) {
					System.out.println("Compilation error: " + d.getMessage(Locale.getDefault()));
				}
			}
		}, true, processBinariesAgain);
		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean canRunJava22() {
		try {
			SourceVersion.valueOf("RELEASE_22");
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