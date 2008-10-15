/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.core.SourceRange;

public class ASTNodeFinderTest extends ConverterTestSetup {

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS3);
	}

	public ASTNodeFinderTest(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 9 };
	}

	public static Test suite() {
		return buildModelTestSuite(ASTNodeFinderTest.class);
	}

	public void test0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		int index2 = CharOperation.indexOf('{', source, index + 1, source.length);
		ASTNode node = NodeFinder.perform(result, index, index2 - index + 1);
		NodeFinder nodeFinder = new NodeFinder(result, index, index2 - index + 1);
		assertTrue("Different node", nodeFinder.getCoveringNode() == node);
	}
	public void test0002() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index, className.length);
		NodeFinder nodeFinder = new NodeFinder(result, index, className.length);
		assertTrue("Different node", nodeFinder.getCoveredNode() == node);
	}
	public void test0003() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		int index2 = CharOperation.indexOf('{', source, index + 1, source.length);
		SourceRange range = new SourceRange(index, index2 - index + 1);
		ASTNode node = NodeFinder.perform(result, range);
		NodeFinder nodeFinder = new NodeFinder(result, index, index2 - index + 1);
		assertTrue("Different node", nodeFinder.getCoveringNode() == node);
	}
	public void test0004() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		SourceRange range = new SourceRange(index, className.length);
		ASTNode node = NodeFinder.perform(result, range);
		NodeFinder nodeFinder = new NodeFinder(result, index, className.length);
		assertTrue("Different node", nodeFinder.getCoveredNode() == node);
	}
	public void test0005() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		int index2 = CharOperation.indexOf('{', source, index + 1, source.length);
		ASTNode node = NodeFinder.perform(result, index, index2 - index + 1, sourceUnit);
		NodeFinder nodeFinder = new NodeFinder(result, index, index2 - index + 1);
		assertTrue("Different node", nodeFinder.getCoveringNode() == node);
	}
	public void test0006() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index, className.length, sourceUnit);
		NodeFinder nodeFinder = new NodeFinder(result, index, className.length);
		assertTrue("Different node", nodeFinder.getCoveredNode() == node);
	}
	public void test0007() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index - 1, 1);
		NodeFinder nodeFinder = new NodeFinder(result, index - 1, 1);
		assertNull("No covered node", nodeFinder.getCoveredNode());
		assertNotNull("Got a covering node", node);
	}
	public void test0008() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		SourceRange range = new SourceRange(index - 1, 1);
		ASTNode node = NodeFinder.perform(result, range);
		NodeFinder nodeFinder = new NodeFinder(result, index - 1, 1);
		assertNull("No covered node", nodeFinder.getCoveredNode());
		assertNotNull("Got a covering node", node);
	}
	public void test0009() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		char[] className = "Test".toCharArray();
		int index = CharOperation.indexOf(className, source, true);
		ASTNode node = NodeFinder.perform(result, index - 1, 1, sourceUnit);
		NodeFinder nodeFinder = new NodeFinder(result, index - 1, 1);
		assertNull("No covered node", nodeFinder.getCoveredNode());
		assertNull("No covering node", node);
	}
}
