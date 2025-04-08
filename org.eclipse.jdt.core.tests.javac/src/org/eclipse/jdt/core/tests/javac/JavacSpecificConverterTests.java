/*******************************************************************************
 * Copyright (c) 2025, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.javac;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

/**
 * Tests for the javac ast converter that aren't covered anywhere in the jdt.core ast conversion suite.
 */
public class JavacSpecificConverterTests {

	@Test
	public void testConvertSerialJavadocTag() throws Exception {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource("""
				public class HelloWorld {
				/**
				 * @serial
				 */
				int a;
				public static void main(String... args) {
					System.out.println("Hello, World!");
				}
			}
			""".toCharArray());
		ASTNode result = parser.createAST(new NullProgressMonitor());
		CompilationUnit cu = (CompilationUnit)result;
		TypeDeclaration typeDecl = (TypeDeclaration)cu.types().get(0);
		FieldDeclaration fieldDecl = (FieldDeclaration)typeDecl.bodyDeclarations().get(0);
		Javadoc javadoc = fieldDecl.getJavadoc();
		TagElement elt = (TagElement)javadoc.tags().get(0);
		assertEquals(elt.getTagName(), TagElement.TAG_SERIAL);
	}
}
