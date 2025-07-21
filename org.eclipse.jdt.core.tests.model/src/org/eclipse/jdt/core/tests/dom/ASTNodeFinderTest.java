/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;

public class ASTNodeFinderTest extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getJLS3(), false);
	}

	public ASTNodeFinderTest(String name) {
		super(name);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	static {
//		TESTS_NUMBERS = new int[] { 9 };
	}

	public static Test suite() {
		return buildModelTestSuite(ASTNodeFinderTest.class);
	}

	public void test0001() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		int index2 = CharOperation.indexOf('{', source, index + 1, source.length);
		ASTNode node = NodeFinder.perform(result, index, index2 - index + 1);
		NodeFinder nodeFinder = new NodeFinder(result, index, index2 - index + 1);
		assertTrue("Different node", nodeFinder.getCoveringNode() == node);
	}
	public void test0002() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index, className.length);
		NodeFinder nodeFinder = new NodeFinder(result, index, className.length);
		assertTrue("Different node", nodeFinder.getCoveredNode() == node);
	}
	public void test0003() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		int index2 = CharOperation.indexOf('{', source, index + 1, source.length);
		SourceRange range = new SourceRange(index, index2 - index + 1);
		ASTNode node = NodeFinder.perform(result, range);
		NodeFinder nodeFinder = new NodeFinder(result, index, index2 - index + 1);
		assertTrue("Different node", nodeFinder.getCoveringNode() == node);
	}
	public void test0004() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		SourceRange range = new SourceRange(index, className.length);
		ASTNode node = NodeFinder.perform(result, range);
		NodeFinder nodeFinder = new NodeFinder(result, index, className.length);
		assertTrue("Different node", nodeFinder.getCoveredNode() == node);
	}
	public void test0005() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		int index2 = CharOperation.indexOf('{', source, index + 1, source.length);
		ASTNode node = NodeFinder.perform(result, index, index2 - index + 1, this.workingCopy);
		NodeFinder nodeFinder = new NodeFinder(result, index, index2 - index + 1);
		assertTrue("Different node", nodeFinder.getCoveringNode() == node);
	}
	public void test0006() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index, className.length, this.workingCopy);
		NodeFinder nodeFinder = new NodeFinder(result, index, className.length);
		assertTrue("Different node", nodeFinder.getCoveredNode() == node);
	}
	public void test0007() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index - 1, 1);
		NodeFinder nodeFinder = new NodeFinder(result, index - 1, 1);
		assertNull("No covered node", nodeFinder.getCoveredNode());
		assertNotNull("Got a covering node", node);
	}
	public void test0008() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		SourceRange range = new SourceRange(index - 1, 1);
		ASTNode node = NodeFinder.perform(result, range);
		NodeFinder nodeFinder = new NodeFinder(result, index - 1, 1);
		assertNull("No covered node", nodeFinder.getCoveredNode());
		assertNotNull("Got a covering node", node);
	}
	public void test0009() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/NodeFinder/src/test0001/Test.java", false);
		String contents =
			"package test0001;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"Hello\" + \" world\");\n" +
			"	}\n" +
			"}";
		ASTNode result = buildAST(
				contents,
				this.workingCopy);
		char[] source = contents.toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index - 1, 1, this.workingCopy);
		NodeFinder nodeFinder = new NodeFinder(result, index - 1, 1);
		assertNull("No covered node", nodeFinder.getCoveredNode());
		assertNull("No covering node", node);
	}
}
