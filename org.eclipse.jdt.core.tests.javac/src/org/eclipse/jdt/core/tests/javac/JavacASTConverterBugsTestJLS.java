/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.javac;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.dom.ASTConverterBugsTest;
import org.eclipse.jdt.core.tests.dom.ASTConverterBugsTestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite to verify that DOM/AST bugs are fixed.
 *
 * Note that only specific JLS8 tests are defined in this test suite, but when
 * running it, all superclass {@link ASTConverterBugsTest} tests will be run
 * as well.
 */
@SuppressWarnings("rawtypes")
public class JavacASTConverterBugsTestJLS extends ASTConverterBugsTestSetup {
	public JavacASTConverterBugsTestJLS(String name) {
	    super(name);
	    this.testLevel = AST.getJLSLatest();
	}

	public static Test suite() {
		TestSuite suite = new Suite(JavacASTConverterBugsTestJLS.class.getName());
		List tests = buildTestsList(JavacASTConverterBugsTestJLS.class, 1, 0/* do not sort*/);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	/**
	 */
	public void testMethodSuperCall() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
					"""
					public class A {
						public class Q {
							public Q() {
								super();
							}
							public void doSomething() {}
						}
						public class Q2 extends Q {
							public Q() {
								super();
							}
							public void doSomething() {
								super.doSomething();
							}
						}
					}"""
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			runConversion(this.testLevel, cuA, true, true, true);
		} finally {
			deleteProject("P");
		}
	}


	/**
	 */
	public void testPlusEquals() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
					"""
					public class A {
						public void foo() {
							int x = 3;
							x += 5;
						}
					}"""
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			runConversion(this.testLevel, cuA, true, true, true);
		} finally {
			deleteProject("P");
		}
	}


	/**
	 */
	public void testModuleTransitiveDependency() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0],
					null, null, null, null, null, true, null, "", null, null, null, "9", false);
			createFile("P/module-info.java",
					"""
					module name {
						requires transitive asdfhjkl;
					}
					"""
			);
			ICompilationUnit cuA = getCompilationUnit("P/module-info.java");
			runConversion(this.testLevel, cuA, true, true, true);
		} finally {
			deleteProject("P");
		}
	}

	public void testAnnotatedDoublyNestedArray() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0],
					null, null, null, null, null, true, null, "", null, null, null, "9", false);
			createFile("P/A.java",
					"""
					public class A {
						public static void main(String... args) {
							@NonNull String @NonNull[] @NonNull myCoolArray = new String[0][];
						}
					}
					"""
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			runConversion(this.testLevel, cuA, true, true, true);
		} finally {
			deleteProject("P");
		}
	}

	public void testGettingParameterInModel() throws Exception {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
				"""
				public class A {
					public static void main(String[] args) {
						System.out.println(args.length);
					}
				}
			"""
					);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			IType typeA = cuA.getType("A");
			assertEquals(1, typeA.getMethods().length);
			IMethod mainMethod = Stream.of(typeA.getMethods()).filter(method -> "main".equals(method.getElementName())).findFirst().get();
			ILocalVariable[] parameters = mainMethod.getParameters();
			assertEquals(1, parameters.length);
		} finally {
			deleteProject("P");
		}
	}
}
