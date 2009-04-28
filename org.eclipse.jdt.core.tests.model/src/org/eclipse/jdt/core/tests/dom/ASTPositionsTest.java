/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTPositionsTest extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS3);
	}

	public ASTPositionsTest(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 182, 183 };
//		TESTS_NAMES = new String[] {"test0177"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTPositionsTest.class);
	}

	private void sanityCheck(final String contents, CompilationUnit compilationUnit) {
		for (int i = 0, max = contents.length(); i < max; i++) {
    		final int lineNumber = compilationUnit.getLineNumber(i);
    		assertTrue("Wrong value for char at " + i, lineNumber >= 1);
    		final int columnNumber = compilationUnit.getColumnNumber(i);
    		assertTrue("Wrong value for char at " + i, columnNumber >= 0);
    		final int position = compilationUnit.getPosition(lineNumber, columnNumber);
    		assertTrue("Wrong value for char at i", position >= 0);
    		if (position == 0) {
    			assertEquals("Only true for first character", 0, i);
    		}
			assertEquals("Wrong char", contents.charAt(i), contents.charAt(position));
    	}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void test001() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"import java.util.Map;\r\n" +
			"public class X {\r\n" +
			"	Map<String, Number> map= null;\r\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertEquals("Wrong char", 'X', contents.charAt(compilationUnit.getPosition(2, 13)));
    	assertEquals("Wrong char", 'i', contents.charAt(compilationUnit.getPosition(1, 0)));
    	assertEquals("Wrong position", -1, compilationUnit.getPosition(1, -1));
    	assertEquals("Wrong position", -1, compilationUnit.getPosition(-1, 0));
    	assertEquals("Wrong position", -1, compilationUnit.getPosition(5, 0));
    	assertEquals("Wrong position", -1, compilationUnit.getPosition(4, 1));
    	assertEquals("Wrong char", '}', contents.charAt(compilationUnit.getPosition(4, 0)));
    	assertEquals("Wrong char", '\r', contents.charAt(compilationUnit.getPosition(1, 21)));

    	sanityCheck(contents, compilationUnit);
	}

	public void test002() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"import java.util.Map;\n" +
			"public class X {\n" +
			"	Map<String, Number> map= null;\n" +
			"}\n";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	sanityCheck(contents, compilationUnit);
	}

	public void test003() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"import java.util.Map;\r" +
			"public class X {\r" +
			"	Map<String, Number> map= null;\r" +
			"}\r";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	sanityCheck(contents, compilationUnit);
	}

	public void test004() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"package pack1;\npublic class X {}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
       	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
       	CompilationUnit compilationUnit = (CompilationUnit) node;
       	sanityCheck(contents, compilationUnit);
		assertEquals(1, compilationUnit.getLineNumber(0));
	}

	public void test005() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"package pack1;public class X {}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
       	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
       	CompilationUnit compilationUnit = (CompilationUnit) node;
		assertEquals(1, compilationUnit.getLineNumber(0));
       	sanityCheck(contents, compilationUnit);
	}


}
