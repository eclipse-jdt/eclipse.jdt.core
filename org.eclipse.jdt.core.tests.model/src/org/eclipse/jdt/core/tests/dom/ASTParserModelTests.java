/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc. and others.
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
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.tests.model.ModifyingResourceTests;

import junit.framework.Test;

public class ASTParserModelTests extends ModifyingResourceTests {

	public ASTParserModelTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTParserModelTests.class);
	}

	public void testStackOverflowInEmptiedModuleDeclarationParsing() throws JavaModelException, CoreException {
		try {
			IJavaProject project1 = createJavaProject("ASTParserModelTests", new String[] { "src" },
					new String[] { "CONVERTER_JCL9_LIB" }, "bin", "9");
			project1.open(null);
			addClasspathEntry(project1, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.MODULE_PATH")));
			String content = """
			module first {
				requires transitive static second.third;
				exports pack1.X11 to org.eclipse.jdt;
			}
			""";
			createFile("/ASTParserModelTests/src/module-info.java", content);
			ICompilationUnit workingCopy = getCompilationUnit("/ASTParserModelTests/src/module-info.java");
			workingCopy.getBuffer().setContents("");

			ASTParser astParser = ASTParser.newParser(AST.getJLSLatest());
			astParser.setSource(workingCopy);
			astParser.setResolveBindings(true);
			astParser.setStatementsRecovery(true);
			ASTNode astNode = astParser.createAST(new NullProgressMonitor());
			assertEquals("", astNode.toString());
		} finally {
			deleteProject("ASTParserModelTests");
		}
	}

}
