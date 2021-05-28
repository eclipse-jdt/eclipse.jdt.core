/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation.
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

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class SealedTypeModelTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] {"test001"};
	}

	ProblemRequestor problemRequestor;
	public SealedTypeModelTests(String name) {
		super(name);
	}
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.problemRequestor =  new ProblemRequestor();
		this.wcOwner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return SealedTypeModelTests.this.problemRequestor;
			}
		};
	}
	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_17, SealedTypeModelTests.class);
	}
	protected IJavaProject createJavaProject(String projectName) throws CoreException {
		IJavaProject createJavaProject = super.createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "17");
		return createJavaProject;
	}
	// Check types with neither sealed nor non-sealed don't return those modifiers
	public void test001() throws Exception {
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "interface I {}\n" +
									"public class X implements I {}\n" +
									"interface Y extends I {}\n";

			createFile(	"/SealedTypes/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/SealedTypes/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 3, types.length);
			for (IType iType : types) {
				assertFalse("modifier should not contain sealed : " + iType.getElementName(), iType.isSealed());
			}
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	public void test002() throws Exception {
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "sealed interface I permits X, Y {}\n" +
									"public non-sealed class X implements I {}\n" +
									"non-sealed interface Y extends I {}\n";

			createFile(	"/SealedTypes/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/SealedTypes/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 3, types.length);
			for (IType iType : types) {
				if (iType.getElementName().equals("I")) {
					assertTrue("modifier should contain sealed", iType.isSealed());
				} else {
					assertFalse("modifier should not contain sealed : " + iType.getElementName(), iType.isSealed());
				}
			}
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test explicitly permitted sub types in Source Type
	public void test003() throws Exception {
		String[] permitted = new String[] {"X", "Y"};
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "sealed interface I permits X, Y {}\n" +
									"public non-sealed class X implements I {}\n" +
									"non-sealed interface Y extends I {}\n";

			createFile(	"/SealedTypes/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/SealedTypes/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 3, types.length);
			for (IType iType : types) {
				if (iType.getElementName().equals("I")) {
					assertTrue("modifier should contain sealed", iType.isSealed());
					String[] permittedSubtypeNames = iType.getPermittedSubtypeNames();
					assertEquals("incorrect permitted sub types", permitted.length, permittedSubtypeNames.length);
					for (int i = 0; i < permitted.length; i++) {
						assertEquals("incorrect permitted sub type", permitted[i], permittedSubtypeNames[i]);
					}
				}
			}
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test implicitly permitted sub types in Source Type
	public void test004() throws Exception {
		String[] permitted = new String[] {"X", "Y"};
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "sealed interface I {}\n" +
									"public non-sealed class X implements I {}\n" +
									"non-sealed interface Y extends I {}\n";

			createFile(	"/SealedTypes/src/X.java",	fileContent);
			ICompilationUnit unit = getCompilationUnit("/SealedTypes/src/X.java");
			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 3, types.length);
			for (IType iType : types) {
				if (iType.getElementName().equals("I")) {
					assertTrue("modifier should contain sealed", iType.isSealed());
					String[] permittedSubtypeNames = iType.getPermittedSubtypeNames();
					assertEquals("incorrect permitted sub types", permitted.length, permittedSubtypeNames.length);
					for (int i = 0; i < permitted.length; i++) {
						assertEquals("incorrect permitted sub type", permitted[i], permittedSubtypeNames[i]);
					}
				}
			}
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test explicitly permitted sub types in binary
	public void test005() throws Exception {
		String[] permitted = new String[] {"p.X", "p.Y"};
		try {
			String[] sources = {
					"p/X.java",
					"package p;\n;" +
					"sealed interface I permits X, Y {}\n" +
					"public non-sealed class X implements I {}\n" +
					"non-sealed interface Y extends I {}\n"
				};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "sealed.jar";
			Util.createJar(sources, jarPath, "17", true);

			IJavaProject project = createJavaProject("SealedTypes");
			addClasspathEntry(project, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, null, false));
			project.open(null);
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot root = null;
			for (IPackageFragmentRoot iRoot : roots) {
				if (iRoot.getRawClasspathEntry().getPath().toString().endsWith("sealed.jar")) {
					root = iRoot;
				}
			}
			assertNotNull("root should not be null", root);
			IPackageFragment packageFragment = root.getPackageFragment("p");
			assertNotNull("package is null", packageFragment);
			IOrdinaryClassFile classFile = packageFragment.getOrdinaryClassFile("I.class");
			assertNotNull("class is null", classFile);
			IType type = classFile.getType();
			assertNotNull("type is null", type);
			String[] permittedSubtypeNames = type.getPermittedSubtypeNames();
			assertEquals("incorrect permitted sub types", permitted.length, permittedSubtypeNames.length);
			for (int i = 0; i < permitted.length; i++) {
				assertEquals("incorrect permitted sub type", permitted[i], permittedSubtypeNames[i]);
			}
			assertTrue("modifier should contain sealed", type.isSealed());

			classFile = packageFragment.getOrdinaryClassFile("X.class");
			assertNotNull("class is null", classFile);
			type = classFile.getType();
			assertNotNull("type is null", type);
			permittedSubtypeNames = type.getPermittedSubtypeNames();
			assertEquals("incorrect permitted sub types", 0, permittedSubtypeNames.length);
			assertFalse("modifier should not contain sealed", type.isSealed());

			classFile = packageFragment.getOrdinaryClassFile("Y.class");
			assertNotNull("class is null", classFile);
			type = classFile.getType();
			assertNotNull("type is null", type);
			permittedSubtypeNames = type.getPermittedSubtypeNames();
			assertEquals("incorrect permitted sub types", 0, permittedSubtypeNames.length);
			assertFalse("modifier should not contain sealed", type.isSealed());
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test implicitly permitted sub types in binary
	public void test006() throws Exception {
		String[] permitted = new String[] {"p.X", "p.Y"};
		try {
			String[] sources = {
							"p/X.java",
							"package p;\n;" +
							"sealed interface I {}\n" +
							"public non-sealed class X implements I {}\n" +
							"non-sealed interface Y extends I {}\n"
			};
			String outputDirectory = Util.getOutputDirectory();

			String jarPath = outputDirectory + File.separator + "sealed.jar";
			Util.createJar(sources, jarPath, "17", true);

			IJavaProject project = createJavaProject("SealedTypes");
			addClasspathEntry(project, JavaCore.newLibraryEntry(new Path(jarPath), null, null, null, null, false));
			project.open(null);
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			IPackageFragmentRoot root = null;
			for (IPackageFragmentRoot iRoot : roots) {
				if (iRoot.getRawClasspathEntry().getPath().toString().endsWith("sealed.jar")) {
					root = iRoot;
				}
			}
			assertNotNull("root should not be null", root);
			IPackageFragment packageFragment = root.getPackageFragment("p");
			assertNotNull("package is null", packageFragment);
			IOrdinaryClassFile classFile = packageFragment.getOrdinaryClassFile("I.class");
			assertNotNull("class is null", classFile);
			IType type = classFile.getType();
			assertNotNull("type is null", type);
			String[] permittedSubtypeNames = type.getPermittedSubtypeNames();
			assertEquals("incorrect permitted sub types", permitted.length, permittedSubtypeNames.length);
			for (int i = 0; i < permitted.length; i++) {
				assertEquals("incorrect permitted sub type", permitted[i], permittedSubtypeNames[i]);
			}
			assertTrue("modifier should contain sealed", type.isSealed());

			classFile = packageFragment.getOrdinaryClassFile("X.class");
			assertNotNull("class is null", classFile);
			type = classFile.getType();
			assertNotNull("type is null", type);
			permittedSubtypeNames = type.getPermittedSubtypeNames();
			assertEquals("incorrect permitted sub types", 0, permittedSubtypeNames.length);
			assertFalse("modifier should not contain sealed", type.isSealed());

			classFile = packageFragment.getOrdinaryClassFile("Y.class");
			assertNotNull("class is null", classFile);
			type = classFile.getType();
			assertNotNull("type is null", type);
			permittedSubtypeNames = type.getPermittedSubtypeNames();
			assertEquals("incorrect permitted sub types", 0, permittedSubtypeNames.length);
			assertFalse("modifier should not contain sealed", type.isSealed());
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test sealed types for reconciler
	public void test007() throws Exception {
		String[] permitted = new String[] {"p.X"};
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "package p;\n" +
								  "sealed interface I permits p.X {}\n";
			createFolder("/SealedTypes/src/p");
			createFile("/SealedTypes/src/p/I.java", fileContent);
			fileContent =  "package p;\n" +
						   "public non-sealed class X implements p.I {}\n";
			createFile(	"/SealedTypes/src/p/X.java", fileContent);

			ICompilationUnit unit = getCompilationUnit("/SealedTypes/src/p/I.java");

			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = fileContent.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = unit.getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n",
					this.problemRequestor);

			IType[] types = unit.getTypes();
			assertEquals("Incorret no of types", 1, types.length);
			for (IType iType : types) {
				if (iType.getElementName().equals("I")) {
					assertTrue("modifier should contain sealed", iType.isSealed());
					String[] permittedSubtypeNames = iType.getPermittedSubtypeNames();
					assertEquals("incorrect permitted sub types", permitted.length, permittedSubtypeNames.length);
					for (int i = 0; i < permitted.length; i++) {
						assertEquals("incorrect permitted sub type", permitted[i], permittedSubtypeNames[i]);
					}
				}
			}
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test sealed types for reconciler
	public void test008() throws Exception {
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "package p;\n" +
								  "sealed interface I permits p.X {}\n";
			createFolder("/SealedTypes/src/p");
			createFile(	"/SealedTypes/src/p/I.java", fileContent);

			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = fileContent.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit("/SealedTypes/src/p/I.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /SealedTypes/src/p/I.java (at line 2)\n" +
					"	sealed interface I permits p.X {}\n" +
					"	                           ^^^\n" +
					"p.X cannot be resolved to a type\n" +
					"----------\n",
					this.problemRequestor);
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
	// Test sealed types for reconciler
	public void test009() throws Exception {
		try {
			IJavaProject project = createJavaProject("SealedTypes");
			project.open(null);
			String fileContent =  "package p;\n" +
					"sealed interface I permits p.X {}\n";
			createFolder("/SealedTypes/src/p");
			createFile(	"/SealedTypes/src/p/I.java", fileContent);
			fileContent =  "package p;\n" +
					"public non-sealed class X {}\n";
			createFile(	"/SealedTypes/src/p/X.java", fileContent);

			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = fileContent.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit("/SealedTypes/src/p/X.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
							"1. ERROR in /SealedTypes/src/p/X.java (at line 2)\n" +
							"	public non-sealed class X {}\n" +
							"	                        ^\n" +
							"A class X declared as non-sealed should have either a sealed direct superclass or a sealed direct superinterface\n" +
							"----------\n",
							this.problemRequestor);
		}
		finally {
			deleteProject("SealedTypes");
		}
	}
}
