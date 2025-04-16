/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

public class Java24ElementTests extends AbstractJavaModelTests {
	private IJavaProject project;
	public Java24ElementTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_24, Java24ElementTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.project = createJavaProject("Java24Elements", new String[] { "src" },
				new String[] { "JCL24_LIB" }, "bin", "24");
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("Java24Elements");
		super.tearDown();
	}

	public void test3200_a() throws Exception {
		this.project.open(null);
		String fileContent = """
				public enum Test {
					/**
					 * Javadoc always ok on the following Instance...
					 */
					BUG( // TODO ...but THIS COMMENT suppresses "MouseOver" on the next field.
					);

					/**
					 *
					 * This Javadoc is not shown on "MouseOver" if the above comment is present.<br>
					 * <br>
					 * Eclipse IDE for RCP and RAP Developers (includes Incubating components)<br>
					 * Version: 2024-09 (4.33.0)<br>
					 * Build id: 20240905-0614
					 */
					public final int bug = 0;
					/**
					 * ...but from here on its OK again...
					 */
					public final int ok  = 0;
				}
					""";
		createFile("/Java24Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java24Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getFields()[0].getSourceRange().getLength() == 138);
		assertTrue(unit.getTypes()[0].getFields()[0].getSourceRange().getOffset() == 20);//Enum  constant

		assertTrue(unit.getTypes()[0].getFields()[1].getSourceRange().getLength() == 272);
		assertTrue(unit.getTypes()[0].getFields()[1].getSourceRange().getOffset() == 162);//first field

		assertTrue(unit.getTypes()[0].getFields()[2].getSourceRange().getLength() == 75);
		assertTrue(unit.getTypes()[0].getFields()[2].getSourceRange().getOffset() == 436); // second field

	}

	public void test3200_b() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public enum Test {
					/// Javadoc always ok on the following Instance...
					BUG( // TODO ...but THIS COMMENT suppresses "MouseOver" on the next field.
					);

					/// This Javadoc is not shown on "MouseOver" if the above comment is present.
					///
					/// **Eclipse IDE for RCP and RAP Developers** (includes Incubating components)
					/// *Version*: 2024-09 (4.33.0)
					/// *Build id*: 20240905-0614
					public final int bug = 0;
					/// ...but from here on its OK again...
					public final int ok  = 0;
				}
					""";
		createFile("/Java24Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java24Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getFields()[0].getSourceRange().getLength() == 129);
		assertTrue(unit.getTypes()[0].getFields()[0].getSourceRange().getOffset() == 20);//Enum  constant

		assertTrue(unit.getTypes()[0].getFields()[1].getSourceRange().getLength() == 330);
		assertTrue(unit.getTypes()[0].getFields()[1].getSourceRange().getOffset() == 77);//first field

		assertTrue(unit.getTypes()[0].getFields()[2].getSourceRange().getLength() == 66);
		assertTrue(unit.getTypes()[0].getFields()[2].getSourceRange().getOffset() == 409); // second field

	}
}
