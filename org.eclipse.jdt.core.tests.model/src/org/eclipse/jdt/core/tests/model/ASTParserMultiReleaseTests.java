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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Tests that {@link ASTParser#createASTs(ICompilationUnit[], String[], ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)}
 * resolves bindings as seen from the release specific source folder the given compilation
 * units live in, honoring a release specific {@code module-info.java}.
 *
 * See https://github.com/eclipse-jdt/eclipse.jdt.core/pull/4534#discussion_r3660872811
 */
public class ASTParserMultiReleaseTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] { "testResolveWindowInRelease17" };
	}

	public ASTParserMultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTParserMultiReleaseTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject project = createJava9ProjectWithJREAttributes("ASTParserMR",
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

		createFolder("/ASTParserMR/src/p");
		createFolder("/ASTParserMR/src17/p");
		createFolder("/ASTParserMR/src21/p");

		// base module requires nothing
		createFile("/ASTParserMR/src/module-info.java", """
				module MRastparser {
				}
				""");
		createFile("/ASTParserMR/src/p/Test.java", """
				package p;
				public class Test {
					java.awt.Window w;
					org.w3c.dom.Element element;
				}
				""");

		// release 17 requires java.desktop (which transitively reads java.xml)
		createFile("/ASTParserMR/src17/module-info.java", """
				module MRastparser {
					requires java.desktop;
				}
				""");
		createFile("/ASTParserMR/src17/p/Test.java", """
				package p;
				public class Test {
					java.awt.Window w;
					org.w3c.dom.Element element;
				}
				""");

		// release 21 requires java.xml
		createFile("/ASTParserMR/src21/module-info.java", """
				module MRastparser {
					requires java.xml;
				}
				""");
		createFile("/ASTParserMR/src21/p/Test.java", """
				package p;
				public class Test {
					java.awt.Window w;
					org.w3c.dom.Element element;
				}
				""");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("ASTParserMR");
		super.tearDownSuite();
	}

	private ITypeBinding resolveFieldType(String unitPath, String fieldName) throws Exception {
		ICompilationUnit unit = getCompilationUnit(unitPath);
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setResolveBindings(true);
		parser.setProject(getJavaProject("ASTParserMR"));
		final ITypeBinding[] result = new ITypeBinding[1];
		parser.createASTs(new ICompilationUnit[] { unit }, new String[0], new ASTRequestor() {
			@Override
			public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
				TypeDeclaration type = (TypeDeclaration) ast.types().get(0);
				for (Object bodyDecl : type.bodyDeclarations()) {
					if (bodyDecl instanceof FieldDeclaration field) {
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
						if (fragment.getName().getIdentifier().equals(fieldName)) {
							result[0] = field.getType().resolveBinding();
						}
					}
				}
			}
		}, null);
		return result[0];
	}

	// org.w3c.dom.Element is reachable in src21 via 'requires java.xml': resolving its
	// field type must succeed with a non-recovered binding for exactly that type.
	public void testResolveElementInRelease21() throws Exception {
		ITypeBinding binding = resolveFieldType("/ASTParserMR/src21/p/Test.java", "element");
		assertNotNull("Field 'element' not resolved", binding);
		assertFalse("Binding should not be recovered/unresolved", binding.isRecovered());
		assertEquals("org.w3c.dom.Element", binding.getQualifiedName());
	}

	// java.awt.Window is reachable in src17 via 'requires java.desktop'.
	public void testResolveWindowInRelease17() throws Exception {
		ITypeBinding binding = resolveFieldType("/ASTParserMR/src17/p/Test.java", "w");
		assertNotNull("Field 'w' not resolved", binding);
		assertFalse("Binding should not be recovered/unresolved", binding.isRecovered());
		assertEquals("java.awt.Window", binding.getQualifiedName());
	}

	// java.desktop reads java.xml transitively, so org.w3c.dom.Element is reachable in src17 too.
	public void testResolveElementInRelease17() throws Exception {
		ITypeBinding binding = resolveFieldType("/ASTParserMR/src17/p/Test.java", "element");
		assertNotNull("Field 'element' not resolved", binding);
		assertFalse("Binding should not be recovered/unresolved", binding.isRecovered());
		assertEquals("org.w3c.dom.Element", binding.getQualifiedName());
	}

	// The base module-info requires neither java.desktop nor java.xml, so java.awt.Window
	// is not accessible and its field type must fail to resolve to a proper binding.
	public void testResolveWindowInBaseReleaseIsUnresolved() throws Exception {
		ITypeBinding binding = resolveFieldType("/ASTParserMR/src/p/Test.java", "w");
		assertTrue("Binding for inaccessible java.awt.Window should not resolve properly",
				binding == null || binding.isRecovered());
	}
}
