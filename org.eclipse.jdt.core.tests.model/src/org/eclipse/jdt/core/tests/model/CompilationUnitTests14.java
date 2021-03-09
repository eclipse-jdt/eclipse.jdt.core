/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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

import javax.lang.model.SourceVersion;

import org.eclipse.jdt.core.*;

public class CompilationUnitTests14 extends ModifyingResourceTests {
	ICompilationUnit cu;
	ICompilationUnit workingCopy;
	IJavaProject testProject;

public CompilationUnitTests14(String name) {
	super(name);
}

public void setUpSuite() throws Exception {
	super.setUpSuite();

	final String compliance = "16"; //$NON-NLS-1$
	this.testProject = createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString(compliance)}, "bin", compliance); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/X.java",
		"\n\n" + 	// package now includes comment (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=93880)
						// => need some empty line at beginning to be able to have cu without any other element (see testGetElementAt())
		"/* some comment */" +
		"package p;\n" +
		"import p2.*;\n" +
		"import p3.Z;\n" +
		"public class X implements Runnable {\n" +
		"  public int f1;\n" +
		"  /** @deprecated\n */" +
		"  protected Object f2;\n" +
		"  private X f3;\n" +
		"  java.lang.String f4;\n" +
		"  int f5, f6, f7;\n" +
		"  @Deprecated\n" +
		"  int f8;\n" +
		"  public class Inner {\n" +
		"    class InnerInner {\n" +
		"    }\n" +
		"  }\n" +
		"  public void foo(Y y) throws IOException {\n" +
		"  }\n" +
		"  protected static Object bar() {\n" +
		"  }\n" +
		"  /** @deprecated\n */" +
		"  private int fred() {\n" +
		"  }\n" +
		"  @Deprecated\n" +
		"  private void fred2() {\n" +
		"  }\n" +
		"  void testIsVarArgs(String s, Object ... args) {\n" +
		"  }\n" +
		"  X(String... s) {\n" +
		"  }\n" +
		"  native void foo2();\n" +
		"  volatile void foo3() {}\n" +
		"  strictfp void foo4() {}\n" +
		"}\n" +
		"/** @deprecated\n */" +
		"interface I {\n" +
		"  int run();\n" +
		"}\n" +
		"interface I2<E> {\n" +
		"}\n" +
		"@Deprecated\n" +
		"interface I3 {\n" +
		"}\n" +
		"class Y<E> implements I2<E> {\n" +
		"}\n" +
		"enum Colors {\n" +
		"  BLUE, WHITE, RED;\n" +
		"}\n" +
		"@interface /*c*/ Annot {\n" +
		"  String field();\n" +
		"}\n" +
		"record Record() {}"
	);
	this.testProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	this.cu = getCompilationUnit("/P/src/p/X.java");
}



// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_PREFIX = "testGetChildren";
//	TESTS_NAMES = new String[] { "testDefaultFlag1" };
//	TESTS_NUMBERS = new int[] { 13 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(CompilationUnitTests14.class);
}
protected void tearDown() throws Exception {
	if (this.workingCopy != null)
		this.workingCopy.discardWorkingCopy();
	super.tearDown();
}
public void tearDownSuite() throws Exception {
	this.deleteProject("P");
	super.tearDownSuite();
}

/**
 * Ensures that correct number of types with the correct names and modifiers
 * exist in a compilation unit.
 */
public void testGetTypes() throws JavaModelException {
	if (canRunJava16()) {
		IType[] types = this.cu.getTypes();
		String[] typeNames = new String[] {"X", "I", "I2", "I3", "Y", "Colors", "Annot", "Record"};
		String[] flags = new String[] {"public", "", "", "", "", "", "", ""};
		boolean[] isClass = new boolean[] {true, false, false, false, true, false, false, false};
		boolean[] isInterface = new boolean[] {false, true, true, true, false, false, true, false};
		boolean[] isAnnotation = new boolean[] {false, false, false, false, false, false, true, false};
		boolean[] isEnum = new boolean[] {false, false, false, false, false, true, false, false};
		boolean[] isRecord = new boolean[] {false, false, false, false, false, false, false, true};
		String[] superclassName = new String[] {null, null, null, null, null, null, null, "java.lang.Record"};
		String[] superclassType = new String[] {null, null, null, null, null, null, null, "Qjava.lang.Record;"};
		String[][] superInterfaceNames = new String[][] {
				new String[] {"Runnable"}, new String[0], new String[0], new String[0], new String[] {"I2<E>"}, new String[0], new String[0], new String[0]
		};
		String[][] superInterfaceTypes = new String[][] {
				new String[] {"QRunnable;"}, new String[0], new String[0], new String[0], new String[] {"QI2<QE;>;"}, new String[0], new String[0], new String[0]
		};
		String[][] formalTypeParameters = new String[][] {
			new String[0], new String[0], new String[] {"E"}, new String[0], new String[] {"E"}, new String[0], new String[0], new String[0]
		};

		assertEquals("Wrong number of types returned", typeNames.length, types.length);
		for (int i = 0; i < types.length; i++) {
			assertEquals("Incorrect name for the " + i + " type", typeNames[i], types[i].getElementName());
			String mod= Flags.toString(types[i].getFlags());
			assertEquals("Unexpected modifier for " + types[i].getElementName(), flags[i], mod);
			assertTrue("Type does not exist " + types[i], types[i].exists());
			assertEquals("Incorrect isClass for the " + i + " type", isClass[i], types[i].isClass());
			assertEquals("Incorrect isInterface for the " + i + " type", isInterface[i], types[i].isInterface());
			assertEquals("Incorrect isAnnotation for the " + i + " type", isAnnotation[i], types[i].isAnnotation());
			assertEquals("Incorrect isEnum for the " + i + " type", isEnum[i], types[i].isEnum());
			assertEquals("Incorrect isRecord for the " + i + " type", isRecord[i], types[i].isRecord());
			assertEquals("Incorrect superclassName for the " + i + " type", superclassName[i], types[i].getSuperclassName());
			assertEquals("Incorrect superclassType for the " + i + " type", superclassType[i], types[i].getSuperclassTypeSignature());
			assertEquals("Incorrect superInterfaceNames for the " + i + " type", superInterfaceNames[i].length, types[i].getSuperInterfaceNames().length);
			assertEquals("Incorrect superInterfaceTypes for the " + i + " type", superInterfaceTypes[i].length, types[i].getSuperInterfaceTypeSignatures().length);
			assertEquals("Incorrect formalTypeParameters for the " + i + " type", formalTypeParameters[i].length, types[i].getTypeParameters().length);
		}
	}
}

public boolean canRunJava16() {
	try {
		SourceVersion.valueOf("RELEASE_16");
	} catch(IllegalArgumentException iae) {
		return false;
	}
	return true;
}

}
