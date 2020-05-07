/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;

public class RecordsElementTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] {"test001"};
	}

	public RecordsElementTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_14, RecordsElementTests.class);
	}
	protected IJavaProject createJavaProject(String projectName) throws CoreException {
		IJavaProject createJavaProject = super.createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "14");
		createJavaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		return createJavaProject;
	}
	// Test a simple class for record oriented attributes
	public void test001() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "@SuppressWarnings(\"preview\")\n" +
					"public class Point {\n" +
					"	public Point(int x1, int x2) {\n" +
					"		x1 = 10;\n" +
					"		x2 = 11;\n" +
					"	}\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());
			assertFalse("type should be a record", types[0].isRecord());
			IField[] recordComponents = types[0].getRecordComponents();
			assertNotNull("should not be null", recordComponents);
			assertEquals("Incorret no of components", 0, recordComponents.length);
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	// Test that with preview disabled, model doesn't see/create record elements
	public void test002() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
			project.open(null);
			String fileContent =  "@SuppressWarnings(\"preview\")\n" +
					"public class Point {\n" +
					"	public Point(int x1, int x2) {\n" +
					"		x1 = 10;\n" +
					"		x2 = 11;\n" +
					"	}\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	// Test a simple record and record components
	public void test003() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
				String fileContent =  "@SuppressWarnings(\"preview\")\n" +
						"public record Point(int x1, int x2) {\n" +
						"	public Point {\n" +
						"		this.x1 = 10;\n" +
						"		this.x2 = 11;\n" +
						"	}\n" +
						"}\n";
				createFile(	"/RecordsElement/src/X.java",	fileContent);
				ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
				IType[] types = unit.getTypes();
				assertEquals("Incorret no of types", 1, types.length);
				assertTrue("type should be a record", types[0].isRecord());
				assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());
				IField[] recordComponents = types[0].getRecordComponents();
				assertNotNull("should be null", recordComponents);
				assertEquals("Incorret no of components", 2, recordComponents.length);
				IField comp = recordComponents[0];
				assertEquals("type should be a record component", IJavaElement.FIELD, comp.getElementType());
				assertEquals("incorrect element name", "x1", comp.getElementName());
				comp = recordComponents[1];
				assertEquals("type should be a record component", IJavaElement.FIELD, comp.getElementType());
				assertEquals("incorrect element name", "x2", comp.getElementName());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
}
