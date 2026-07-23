/*******************************************************************************
 * Copyright (c) 2026 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Tests that reconciling (problem detection in working copies) of a multi-release
 * modular project resolves modules and types as seen from the release specific source
 * folder the reconciled unit lives in, honoring a per-release {@code module-info.java}.
 *
 * See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/4268
 */
public class ReconcilerModuleMultiReleaseTests extends ModifyingResourceTests {

	static {
//		TESTS_NAMES = new String[] { "testReconcileUsesReleaseModuleInfo21" };
	}

	public ReconcilerModuleMultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ReconcilerModuleMultiReleaseTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject project = createJava9ProjectWithJREAttributes("ReconcilerModuleMR",
				new String[] { "src", "src17", "src21" }, null, "21");
		IClasspathEntry[] classpath = project.getRawClasspath();
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (entry.getPath().toString().endsWith("src17")) {
					classpath[i] = JavaCore.newSourceEntry(entry.getPath(), null, null, null,
							new IClasspathAttribute[] {
									JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "17") });
				} else if (entry.getPath().toString().endsWith("src21")) {
					classpath[i] = JavaCore.newSourceEntry(entry.getPath(), null, null, null,
							new IClasspathAttribute[] {
									JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "21") });
				}
			}
		}
		project.setRawClasspath(classpath, new NullProgressMonitor());
		project.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);

		createFolder("/ReconcilerModuleMR/src/p");
		createFolder("/ReconcilerModuleMR/src17/p");
		createFolder("/ReconcilerModuleMR/src21/p");

		// base module requires nothing
		createFile("/ReconcilerModuleMR/src/module-info.java", """
				module MRmodular {
				}
				""");
		// release 17 requires java.desktop (which transitively reads java.xml)
		createFile("/ReconcilerModuleMR/src17/module-info.java", """
				module MRmodular {
					requires java.desktop;
				}
				""");
		// release 21 requires java.xml
		createFile("/ReconcilerModuleMR/src21/module-info.java", """
				module MRmodular {
					requires java.xml;
				}
				""");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("ReconcilerModuleMR");
		super.tearDownSuite();
	}

	private void assertReconcileProblems(String path, String source, String expectedProblems) throws Exception {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		WorkingCopyOwner owner = new WorkingCopyOwner() {
			@Override
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return problemRequestor;
			}
		};
		ICompilationUnit wc = getWorkingCopy(path, source, owner, problemRequestor);
		try {
			problemRequestor.initialize(source.toCharArray());
			wc.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, owner, null);
			assertProblems("Unexpected problems for " + path, expectedProblems, problemRequestor);
		} finally {
			wc.discardWorkingCopy();
		}
	}

	// java.xml is required in src21, so org.w3c.dom.Element must be accessible (no problem).
	public void testReconcileUsesReleaseModuleInfo21() throws Exception {
		assertReconcileProblems("/ReconcilerModuleMR/src21/p/Use.java", """
				package p;
				public class Use {
					org.w3c.dom.Element element;
				}
				""", "----------\n----------\n");
	}

	// java.desktop is required in src17 and reads java.xml transitively, so both are accessible.
	public void testReconcileUsesReleaseModuleInfo17() throws Exception {
		assertReconcileProblems("/ReconcilerModuleMR/src17/p/Use.java", """
				package p;
				public class Use {
					java.awt.Window window;
					org.w3c.dom.Element element;
				}
				""", "----------\n----------\n");
	}

	// the base module requires nothing, so org.w3c.dom.Element is not accessible here.
	public void testReconcileUsesBaseModuleInfo() throws Exception {
		assertReconcileProblems("/ReconcilerModuleMR/src/p/Use.java", """
				package p;
				public class Use {
					org.w3c.dom.Element element;
				}
				""",
				"----------\n" +
				"1. ERROR in /ReconcilerModuleMR/src/p/Use.java (at line 3)\n" +
				"	org.w3c.dom.Element element;\n" +
				"	^^^^^^^^^^^^^^^^^^^\n" +
				"The type org.w3c.dom.Element is not accessible\n" +
				"----------\n");
	}

	private ITypeBinding resolveFieldType(String path, String source) throws Exception {
		ICompilationUnit wc = getWorkingCopy(path, source);
		try {
			ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
			parser.setResolveBindings(true);
			parser.setSource(wc);
			org.eclipse.jdt.core.dom.CompilationUnit ast =
					(org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);
			TypeDeclaration type = (TypeDeclaration) ast.types().get(0);
			FieldDeclaration field = type.getFields()[0];
			return field.getType().resolveBinding();
		} finally {
			wc.discardWorkingCopy();
		}
	}

	// ASTParser.createAST (resolved DOM AST) must resolve org.w3c.dom.Element as seen from src21.
	public void testCreateASTUsesReleaseModuleInfo21() throws Exception {
		ITypeBinding binding = resolveFieldType("/ReconcilerModuleMR/src21/p/Use.java", """
				package p;
				public class Use {
					org.w3c.dom.Element element;
				}
				""");
		assertNotNull("Type binding should be resolved", binding);
		assertEquals("org.w3c.dom.Element", binding.getQualifiedName());
	}

	// java.desktop reads java.xml transitively, so the binding resolves in src17 as well.
	public void testCreateASTUsesReleaseModuleInfo17() throws Exception {
		ITypeBinding binding = resolveFieldType("/ReconcilerModuleMR/src17/p/Use.java", """
				package p;
				public class Use {
					org.w3c.dom.Element element;
				}
				""");
		assertNotNull("Type binding should be resolved", binding);
		assertEquals("org.w3c.dom.Element", binding.getQualifiedName());
	}
}
