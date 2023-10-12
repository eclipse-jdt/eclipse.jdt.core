/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;

public class Java21ElementTests extends AbstractJavaModelTests {

	private IJavaProject project;
	public Java21ElementTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(AbstractCompilerTest.F_21, Java21ElementTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.project = createJavaProject("Java21Elements", new String[] { "src" },
				new String[] { "JCL21_LIB" }, "bin", "21");
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject("Java21Elements");
		super.tearDown();
	}

	public void test001() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public interface Test {
					public static void main(String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test002() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					public static void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test003() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					public void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test004() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					protected void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test005() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test006() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					private void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test007() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					protected void main(String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test008() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					void main(String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test009() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					private void main(String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test010() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					protected void main(int args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test011() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					void main(int args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test012() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					private void main(int args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}
	
	public void test013() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					public static void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test014() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					public void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test015() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					protected void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test016() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test017() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					private void main() {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test018() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					protected void main(String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test019() throws Exception {
		this.project.open(null);
		String fileContent = """
				public class Test {
					void main(String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test020() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					protected void main(java.lang.String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test021() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					void main(java.lang.String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertTrue(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

	public void test022() throws Exception {
		this.project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.project.open(null);
		String fileContent = """
				public class Test {
					private void main(java.lang.String[] args) {
					}
				}
					""";
		createFile("/Java21Elements/src/Test.java", fileContent);

		ICompilationUnit unit = getCompilationUnit("/Java21Elements/src/Test.java");
		assertFalse(unit.getTypes()[0].getMethods()[0].isMainMethodCandidate());
	}

}
