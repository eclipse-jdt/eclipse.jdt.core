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

import java.io.File;
import junit.framework.Test;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;

public class RecordsElementTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] {"test001"};
	}

	public RecordsElementTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_17, RecordsElementTests.class);
	}
	protected IJavaProject createJavaProject(String projectName) throws CoreException {
		IJavaProject createJavaProject = super.createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "17");
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
				IField[] fields = types[0].getFields();
				assertEquals("Incorret no of fields", 0, fields.length);
				IField[] recordComponents = types[0].getRecordComponents();
				assertNotNull("should be null", recordComponents);
				assertEquals("Incorret no of components", 2, recordComponents.length);
				IField comp = recordComponents[0];
				assertEquals("type should be a record component", IJavaElement.FIELD, comp.getElementType());
				assertEquals("incorrect element name", "x1", comp.getElementName());
				comp = recordComponents[1];
				assertEquals("type should be a record component", IJavaElement.FIELD, comp.getElementType());
				assertEquals("incorrect element name", "x2", comp.getElementName());
				IMethod[] methods = types[0].getMethods();
				assertNotNull("should not be null", methods);
				assertEquals("Incorret no of methods", 1, methods.length);
				IMethod iMethod = methods[0];
				assertEquals("type should be a record component", IJavaElement.METHOD, iMethod.getElementType());
				assertEquals("incorrect element name", "Point", iMethod.getElementName());
				String[] parameterNames = iMethod.getParameterNames();
				assertEquals("parameters not matching", 2, parameterNames.length);
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	public void test004() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "@SuppressWarnings(\"preview\")\n" +
					"public record Point(int x1, int x2) {\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertTrue("type should be a record", types[0].isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());

			IField[] fields = types[0].getRecordComponents();
			assertEquals("Incorret no of fields", 2, fields.length);
			for (IField field : fields) {
				assertTrue("Should be record component: " + field, field.isRecordComponent());
				IField sameField = types[0].getRecordComponent(field.getElementName());
				assertTrue("Should be record component: " + sameField, sameField.isRecordComponent());
			}

			IMethod[] methods = types[0].getMethods();
			assertNotNull("should not be null", methods);
			assertEquals("Incorret no of elements", 0, methods.length);
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	// Test record with compact canonical constructor
	public void test005() throws Exception {
		try {
			this.workingCopies = new ICompilationUnit[1];
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "public record Point(int x1, int x2) {\n" +
					"	public Point {\n" +
					"		x1 = 1;\n" +
					"	}\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertTrue("type should be a record", types[0].isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());
			IMethod[] methods = types[0].getMethods();
			assertNotNull("should not be null", methods);
			assertEquals("Incorret no of elements", 1, methods.length);
			IMethod constructor = methods[0];
			assertTrue("should be a constructor", constructor.isConstructor());
			//assertTrue("should be a canonical constructor", constructor.isCanonicalConstructor());
			assertEquals("incorrect number of parameters", 2, constructor.getNumberOfParameters());
			String[] parameterNames = constructor.getParameterNames();
			assertEquals("incorrect numer of names", 2, parameterNames.length);
			assertEquals("incorrect parameter names", "x1", parameterNames[0]);
			assertEquals("incorrect parameter names", "x2", parameterNames[1]);

			this.workingCopies[0] = getWorkingCopy("/RecordsElement/src/X.java", fileContent);
			// Test code select
			String str = this.workingCopies[0].getSource();
			String selection = "x1";
			int start = str.lastIndexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("Incorret no of types", 1, elements.length);
			IJavaElement element = elements[0];
			assertEquals("type should be a record", IJavaElement.LOCAL_VARIABLE, element.getElementType());
			element = element.getParent();
			assertNotNull("should not be null", element);
			// unlike constructors whose parameters are explicitly declared,
			// in case of compact constructors, the element is attached as a child of
			// the field that represents the record component.
			assertEquals("should be a method", IJavaElement.FIELD, element.getElementType());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	//Test record with canonical constructor
	public void test006() throws Exception {
		try {
			this.workingCopies = new ICompilationUnit[1];
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "public record Point(int x1, int x2) {\n" +
					"	public Point(int x1, int x2) {\n" +
					"		this.x1 = x1;\n" +
					"		this.x2 = x2;\n" +
					"	}\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertTrue("type should be a record", types[0].isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());

			IField[] fields = types[0].getRecordComponents();
			assertEquals("Incorret no of fields", 2, fields.length);
			for (IField field : fields) {
				assertTrue("Should be record component: " + field, field.isRecordComponent());
				IField sameField = types[0].getRecordComponent(field.getElementName());
				assertTrue("Should be record component: " + sameField, sameField.isRecordComponent());
			}
			IMethod[] methods = types[0].getMethods();
			assertNotNull("should not be null", methods);
			assertEquals("Incorret no of elements", 1, methods.length);
			IMethod constructor = methods[0];
			assertTrue("should be a constructor", constructor.isConstructor());
//			assertTrue("should be a canonical constructor", constructor.isCanonicalConstructor());
			assertEquals("incorrect number of parameters", 2, constructor.getNumberOfParameters());
			String[] parameterNames = constructor.getParameterNames();
			assertEquals("incorrect numer of names", 2, parameterNames.length);
			assertEquals("incorrect parameter names", "x1", parameterNames[0]);
			assertEquals("incorrect parameter names", "x2", parameterNames[1]);

			this.workingCopies[0] = getWorkingCopy("/RecordsElement/src/X.java", fileContent);
			// Test code select
			String str = this.workingCopies[0].getSource();
			String selection = "x1";
			int start = str.lastIndexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("Incorret no of types", 1, elements.length);
			IJavaElement element = elements[0];
			assertEquals("type should be a record", IJavaElement.LOCAL_VARIABLE, element.getElementType());
			element = element.getParent();
			assertNotNull("should not be null", element);
			assertEquals("type should be a method", IJavaElement.METHOD, element.getElementType());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	// Test things from a binary
	public void test007() throws Exception {
		try {
			String[] sources = {
					"p/Point.java",
					"package p;\n;" +
					"public record Point(int x1, int x2) {\n" +
					"	public Point(int x1, int x2) {\n" +
					"		this.x1 = x1;\n" +
					"		this.x2 = x2;\n" +
					"	}\n" +
					"	public Point(int x1, int x2, int x3) {\n" +
					"		this(x1, x2);\n" +
					"	}\n" +
					"	public Point(int x1, float f2) {\n" +
					"		this(0, 0);\n" +
					"	}\n" +
					"}\n"
				};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "records.jar";
			Util.createJar(sources, jarPath, "17", false);

			IJavaProject project = createJavaProject("RecordsElement");
			addClasspathEntry(project, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, null, false));
			project.open(null);
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot root = null;
			for (IPackageFragmentRoot iRoot : roots) {
				if (iRoot.getRawClasspathEntry().getPath().toString().endsWith("records.jar")) {
					root = iRoot;
				}
			}
			assertNotNull("root should not be null", root);
			IPackageFragment packageFragment = root.getPackageFragment("p");
			assertNotNull("package is null", packageFragment);
			IOrdinaryClassFile classFile = packageFragment.getOrdinaryClassFile("Point.class");
			assertNotNull("class is null", classFile);
			IType type = classFile.getType();
			assertNotNull("type is null", type);
			assertTrue("should be a record", type.isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, type.getElementType());

			IField[] fields = type.getRecordComponents();
			assertEquals("Incorret no of fields", 2, fields.length);
			for (IField field : fields) {
				assertTrue("Should be record component: " + field, field.isRecordComponent());
				IField sameField = type.getRecordComponent(field.getElementName());
				assertTrue("Should be record component: " + sameField, sameField.isRecordComponent());
			}

			IMethod[] methods = type.getMethods();
			assertNotNull("should not be null", methods);
			assertEquals("Incorret no of elements", 8, methods.length); // Point(),  Point(), x1(), x2(), toString(), hashCode(), equals()
			IMethod constructor = methods[0];
			assertTrue("should be a constructor", constructor.isConstructor());
//			assertTrue("should be a canonical constructor", constructor.isCanonicalConstructor());
			assertEquals("incorrect number of parameters", 2, constructor.getNumberOfParameters());
			String[] parameterNames = constructor.getParameterNames();
			assertEquals("incorrect numer of names", 2, parameterNames.length);
			assertEquals("incorrect parameter names", "x1", parameterNames[0]);
			assertEquals("incorrect parameter names", "x2", parameterNames[1]);

			constructor = methods[1];
			assertTrue("should be a constructor", constructor.isConstructor());
//			assertFalse("should not be a canonical constructor", constructor.isCanonicalConstructor());
			assertEquals("incorrect number of parameters", 3, constructor.getNumberOfParameters());

			constructor = methods[2];
			assertTrue("should be a constructor", constructor.isConstructor());
//			assertFalse("should not be a canonical constructor", constructor.isCanonicalConstructor());
			assertEquals("incorrect number of parameters", 2, constructor.getNumberOfParameters());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	// Test things from a binary
	public void test008() throws Exception {
		try {
			String[] sources = {
					"p/Point.java",
					"package p;\n;" +
							"public record Point(int x1, int x2) {\n" +
							"	public Point {\n" +
							"		x1 = 1;\n" +
							"		x2 = 2;\n" +
							"	}\n" +
							"}\n"
			};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "records.jar";
			Util.createJar(sources, jarPath, "17", false);

			IJavaProject project = createJavaProject("RecordsElement");
			addClasspathEntry(project, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, null, false));
			project.open(null);
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot root = null;
			for (IPackageFragmentRoot iRoot : roots) {
				if (iRoot.getRawClasspathEntry().getPath().toString().endsWith("records.jar")) {
					root = iRoot;
				}
			}
			assertNotNull("root should not be null", root);
			IPackageFragment packageFragment = root.getPackageFragment("p");
			assertNotNull("package is null", packageFragment);
			IOrdinaryClassFile classFile = packageFragment.getOrdinaryClassFile("Point.class");
			assertNotNull("class is null", classFile);
			IType type = classFile.getType();
			assertNotNull("type is null", type);
			assertTrue("should be a record", type.isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, type.getElementType());
			IMethod[] methods = type.getMethods();
			assertNotNull("should not be null", methods);
			assertEquals("Incorret no of elements", 6, methods.length);
			IMethod constructor = methods[0];
			assertTrue("should be a constructor", constructor.isConstructor());
//			assertTrue("should be a canonical constructor", constructor.isCanonicalConstructor());
			assertEquals("incorrect number of parameters", 2, constructor.getNumberOfParameters());
			String[] parameterNames = constructor.getParameterNames();
			assertEquals("incorrect numer of names", 2, parameterNames.length);
			assertEquals("incorrect parameter names", "x1", parameterNames[0]);
			assertEquals("incorrect parameter names", "x2", parameterNames[1]);
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	public void testBug566860_1() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "@SuppressWarnings(\"preview\")\n" +
					"public record Point(int /* comment1 */ x1, int /* comment2 */ x2) {\n" +
					"	public void foo() {}\n" +
					"	static int field;\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertTrue("type should be a record", types[0].isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());
			IMethod[] methods = types[0].getMethods();
			assertEquals("Incorret no of methods", 1, methods.length);
			IMethod m = methods[0];
			ISourceRange sourceRange = m.getSourceRange();
			String methodString = "public void foo() {}";
			int o = fileContent.indexOf(methodString);
			int l = methodString.length();
			assertEquals("Unexpected offset", o, sourceRange.getOffset());
			assertEquals("Unexpected length", l, sourceRange.getLength());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	public void testBug566860_2() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "@SuppressWarnings(\"preview\")\n" +
					"public record Point(int /* comment */ x1) {\n" +
					"	static int field;\n" +
					"	public void foo() {}\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertTrue("type should be a record", types[0].isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());
			IField[] fields = types[0].getFields();
			assertEquals("Incorret no of methods", 1, fields.length);
			IField m = fields[0];
			ISourceRange sourceRange = m.getSourceRange();
			String methodString = "static int field;";
			int o = fileContent.indexOf(methodString);
			int l = methodString.length();
			assertEquals("Unexpected offset", o, sourceRange.getOffset());
			assertEquals("Unexpected length", l, sourceRange.getLength());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
	public void testBug566860_3() throws Exception {
		try {
			IJavaProject project = createJavaProject("RecordsElement");
			project.open(null);
			String fileContent =  "@SuppressWarnings(\"preview\")\n" +
					"public record Point(int /* comment */ x1) {\n" +
					"	/** javadoc */ static int field;\n" +
					"	public void foo() {}\n" +
					"}\n";
			createFile(	"/RecordsElement/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/RecordsElement/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			assertTrue("type should be a record", types[0].isRecord());
			assertEquals("type should be a record", IJavaElement.TYPE, types[0].getElementType());
			IField[] fields = types[0].getFields();
			assertEquals("Incorret no of methods", 1, fields.length);
			IField m = fields[0];
			ISourceRange sourceRange = m.getSourceRange();
			String methodString = "/** javadoc */ static int field;";
			int o = fileContent.indexOf(methodString);
			int l = methodString.length();
			assertEquals("Unexpected offset", o, sourceRange.getOffset());
			assertEquals("Unexpected length", l, sourceRange.getLength());
		}
		finally {
			deleteProject("RecordsElement");
		}
	}
}
