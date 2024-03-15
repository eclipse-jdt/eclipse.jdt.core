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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

public class ASTParserRecoveryTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase {

	public ASTParserRecoveryTest(String name) {
		super(name);
	}

	public static ASTNode getAST(String source) {
		@SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setStatementsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		return parser.createAST(new NullProgressMonitor());
	}

	@Test
	public void testRecoverSwitchInAnonymousClassInMethod() {
		ASTNode root = getAST(
				"""
				public class Test {
					private static ILog test() {
						return new ILog() {
							@Override
							public void log(String status) {
								switch (status.length()) { // here
								case
								}
							}
						};
					}
				}
				""");
		assertTrue("root is compilationUnit", root instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) root;
		assertEquals("should have one type", 1, compilationUnit.types().size());
		TypeDeclaration typeDeclaration = (TypeDeclaration)compilationUnit.types().get(0);
		assertEquals("should have one method", 1, typeDeclaration.getMethods().length);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		assertEquals("method should have one statement", 1, methodDeclaration.getBody().statements().size());
		ExpressionStatement returnStatement = (ExpressionStatement)methodDeclaration.getBody().statements().get(0);
		assertTrue("return expression should be class instance creation", returnStatement.getExpression() instanceof ClassInstanceCreation);
		ClassInstanceCreation anonymous = (ClassInstanceCreation)returnStatement.getExpression();
		assertEquals("anonymous class has one body declaration", 1, anonymous.getAnonymousClassDeclaration().bodyDeclarations().size());
		MethodDeclaration logMethodDeclaration = (MethodDeclaration) anonymous.getAnonymousClassDeclaration().bodyDeclarations().get(0);
		assertEquals("anonymous class log method has one statement in it", 1, logMethodDeclaration.getBody().statements().size());
	}

}
