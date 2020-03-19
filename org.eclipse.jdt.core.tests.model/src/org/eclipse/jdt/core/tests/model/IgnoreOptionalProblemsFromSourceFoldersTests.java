/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class IgnoreOptionalProblemsFromSourceFoldersTests extends ModifyingResourceTests {
	private static final IClasspathAttribute ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE = JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true");

	public static Test suite() {
		return buildModelTestSuite(IgnoreOptionalProblemsFromSourceFoldersTests.class);
	}

	public IgnoreOptionalProblemsFromSourceFoldersTests(String name) {
		super(name);
	}

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	// ignore optional errors
	public void test001() throws CoreException {
		ICompilationUnit unit = null;
		try {
			IJavaProject project = createJavaProject("P", new String[] {}, new String[] { "JCL_LIB" }, "bin");
			project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

			IClasspathEntry[] originalCP = project.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
			newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			project.setRawClasspath(newCP, null);

			createFolder("/P/src/p");
			IFile file = createFile("/P/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"}");
			unit = (ICompilationUnit) JavaCore.create(file);

			ProblemRequestor problemRequestor = new ProblemRequestor();
			WorkingCopyOwner owner = newWorkingCopyOwner(problemRequestor);
			unit.getWorkingCopy(owner, null);
			assertProblems("Unexpected problems",
					"----------\n" +
					"----------\n",
					problemRequestor);
		} finally {
			if (unit != null) {
				unit.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	// two different source folders ignore only from one
	public void test002() throws CoreException {
		ICompilationUnit x = null;
		ICompilationUnit y = null;
		try {
			IJavaProject project = createJavaProject("P", new String[] {}, new String[] { "JCL_LIB" }, "bin");
			project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

			IClasspathEntry[] originalCP = project.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
			newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			newCP[originalCP.length + 1] = JavaCore.newSourceEntry(new Path("/P/src2"));
			project.setRawClasspath(newCP, null);

			createFolder("/P/src/p");
			IFile fileX = createFile("/P/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"}");
			x = (ICompilationUnit) JavaCore.create(fileX);
			createFolder("/P/src2/q");
			IFile fileY = createFile("/P/src2/q/Y.java",
					"package q;\n" +
					"public class Y {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"}");
			y = (ICompilationUnit) JavaCore.create(fileY);

			ProblemRequestor problemRequestorX = new ProblemRequestor();
			WorkingCopyOwner ownerX = newWorkingCopyOwner(problemRequestorX);
			x.getWorkingCopy(ownerX, null);
			assertProblems("Unexpected problems",
					"----------\n" +
					"----------\n",
					problemRequestorX);

			ProblemRequestor problemRequestorY = new ProblemRequestor();
			WorkingCopyOwner ownerY = newWorkingCopyOwner(problemRequestorY);
			y.getWorkingCopy(ownerY, null);
			assertProblems("Unexpected problems value",
					"----------\n" +
					"1. ERROR in /P/src2/q/Y.java\n" +
					"The value of the local variable i is not used\n" +
					"----------\n",
					problemRequestorY);
		} finally {
			if (x != null) {
				x.discardWorkingCopy();
			}
			if (y != null) {
				y.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	// two different source folders ignore from both
	public void test003() throws CoreException {
		ICompilationUnit x = null;
		ICompilationUnit y = null;
		try {
			IJavaProject project = createJavaProject("P", new String[] {}, new String[] { "JCL_LIB" }, "bin");
			project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

			IClasspathEntry[] originalCP = project.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
			newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			newCP[originalCP.length + 1] = JavaCore.newSourceEntry(new Path("/P/src2"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			project.setRawClasspath(newCP, null);

			createFolder("/P/src/p");
			IFile fileX = createFile("/P/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"}");
			x = (ICompilationUnit) JavaCore.create(fileX);
			createFolder("/P/src2/q");
			IFile fileY = createFile("/P/src2/q/Y.java",
					"package q;\n" +
					"public class Y {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"}");
			y = (ICompilationUnit) JavaCore.create(fileY);

			ProblemRequestor problemRequestorX = new ProblemRequestor();
			WorkingCopyOwner ownerX = newWorkingCopyOwner(problemRequestorX);
			x.getWorkingCopy(ownerX, null);
			assertProblems("Unexpected problems",
					"----------\n" +
					"----------\n",
					problemRequestorX);

			ProblemRequestor problemRequestorY = new ProblemRequestor();
			WorkingCopyOwner ownerY = newWorkingCopyOwner(problemRequestorY);
			y.getWorkingCopy(ownerY, null);
			assertProblems("Unexpected problems",
					"----------\n" +
					"----------\n",
					problemRequestorY);
		} finally {
			if (x != null) {
				x.discardWorkingCopy();
			}
			if (y != null) {
				y.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	// non-optional errors cannot be ignored
	public void test004() throws CoreException {
		ICompilationUnit unit = null;
		try {
			IJavaProject project = createJavaProject("P", new String[] {}, new String[] { "JCL_LIB" }, "bin");
			project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

			IClasspathEntry[] originalCP = project.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
			newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			project.setRawClasspath(newCP, null);

			createFolder("/P/src/p");
			IFile file = createFile("/P/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"	public void bar() {\n" +
					"		a++;\n" +
					"	}\n" +
					"}");
			unit = (ICompilationUnit) JavaCore.create(file);

			ProblemRequestor problemRequestor = new ProblemRequestor();
			WorkingCopyOwner owner = newWorkingCopyOwner(problemRequestor);
			unit.getWorkingCopy(owner, null);
			assertProblems("Unexpeted problems",
					"----------\n" +
					"1. ERROR in /P/src/p/X.java\n" +
					"a cannot be resolved to a variable\n" +
					"----------\n",
					problemRequestor);
		} finally {
			if (unit != null) {
				unit.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	// task tags cannot be ignored
	public void test005() throws CoreException {
		ICompilationUnit unit = null;
		try {
			IJavaProject project = createJavaProject("P", new String[] {}, new String[] { "JCL_LIB" }, "bin");
			project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);
			project.setOption(JavaCore.COMPILER_TASK_TAGS, "TODO");
			project.setOption(JavaCore.COMPILER_TASK_PRIORITIES, "NORMAL");

			IClasspathEntry[] originalCP = project.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
			newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			project.setRawClasspath(newCP, null);

			createFolder("/P/src/p");
			IFile file = createFile("/P/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"	public void bar() {\n" +
					"		// TODO nothing\n" +
					"	}\n" +
					"}");
			unit = (ICompilationUnit) JavaCore.create(file);

			ProblemRequestor problemRequestor = new ProblemRequestor();
			WorkingCopyOwner owner = newWorkingCopyOwner(problemRequestor);
			unit.getWorkingCopy(owner, null);
			assertProblems("Unexpeted problems",
					"----------\n" +
					"1. WARNING in /P/src/p/X.java\n" +
					"TODO nothing\n" +
					"----------\n",
					problemRequestor);
		} finally {
			if (unit != null) {
				unit.discardWorkingCopy();
			}
			deleteProject("P");
		}
	}

	/**
	 * createASTs() should not respect this option.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=372377
	 */
	public void test006() throws CoreException {
		try {
			IJavaProject project = createJavaProject("P", new String[] {}, new String[] { "JCL_LIB" }, "bin");
			project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.ERROR);

			IClasspathEntry[] originalCP = project.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
			newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
					new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
			project.setRawClasspath(newCP, null);

			createFolder("/P/src/p");
			IFile file = createFile("/P/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"	public void foo() {\n" +
					"		int i;\n" +
					"	}\n" +
					"}");
			ICompilationUnit unit = (ICompilationUnit) JavaCore.create(file);

			ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
			parser.setProject(project);
			parser.setSource(unit);
			parser.setResolveBindings(true);

			class Requestor extends ASTRequestor {
				CompilationUnit cuAST;
				public void acceptAST(ICompilationUnit source,
						CompilationUnit ast) {
					this.cuAST = ast;
				}
			}
			Requestor requestor = new Requestor();
			parser.createASTs(new ICompilationUnit[] {unit}, new String[0], requestor, null);
			IProblem[] problems = requestor.cuAST.getProblems();
			assertEquals("Should have 1 problem", 1, problems.length);
		} finally {
			deleteProject("P");
		}
	}
}
