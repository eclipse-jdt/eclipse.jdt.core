/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.tests.model.ModifyingResourceTests;
import org.eclipse.jdt.core.tests.util.Util;

public class AbstractASTTests extends ModifyingResourceTests {

	public AbstractASTTests(String name) {
		super(name);
	}
	
	/*
	 * Removes the *start* and *end* markers from the given source
	 * and remembers the positions.
	 */
	public class MarkerInfo {
		String path;
		String source;
		int astStart, astEnd;
		
		public MarkerInfo(String source) {
			this(null, source);
		}
		public MarkerInfo(String path, String source) {
			this.path = path;
			this.source = source;
			String markerStart = "/*start*/";
			String markerEnd = "/*end*/";
			this.astStart = source.indexOf(markerStart); // start of AST inclusive
			this.source = new String(CharOperation.replace(this.source.toCharArray(), markerStart.toCharArray(), CharOperation.NO_CHAR));
			this.astEnd = this.source.indexOf(markerEnd); // end of AST exclusive
			this.source = new String(CharOperation.replace(this.source.toCharArray(), markerEnd.toCharArray(), CharOperation.NO_CHAR));	
		}
	}
	
	protected void assertASTNodeEquals(String expected, ASTNode node) {
		String actual = node.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 3) + ",");
		}
		assertEquals("Unexpected ast node", expected, actual);
	}
	
	protected void assertASTNodesEqual(String expected, List nodes) {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = nodes.iterator();
		while (iterator.hasNext()) {
			ASTNode node = (ASTNode) iterator.next();
			buffer.append(node);
			if (node instanceof CompilationUnit) {
				IProblem[] problems = ((CompilationUnit) node).getProblems();
				if (problems != null) {
					for (int i = 0, length = problems.length; i < length; i++) {
						IProblem problem = problems[i];
						buffer.append('\n');
						buffer.append(problem);
					}
				}
			}
			buffer.append('\n');
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.println(displayString(actual, 4) + ",");
		}
		assertEquals("Unexpected ast nodes", expected, actual);
	}
		
	protected void assertBindingKeyEquals(String expected, String actual) {
		assertBindingKeysEqual(expected, new String[] {actual});
	}

	protected void assertBindingKeysEqual(String expected, String[] actualKeys) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = actualKeys.length; i < length; i++) {
			if (i > 0) buffer.append('\n');
			buffer.append(actualKeys[i]);
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.print(displayString(actual, 4));
			System.out.println(',');
		}
		assertEquals(
			"Unexpected binding keys",
			expected,
			actual);
	}

	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, and returns the AST node that was delimited
	 * by "*start*" and "*end*".
	 */
	protected ASTNode buildAST(String contents, ICompilationUnit cu) throws JavaModelException {
		MarkerInfo markerInfo = new MarkerInfo(contents);
		contents = markerInfo.source;

		cu.getBuffer().setContents(contents);
		CompilationUnit unit = cu.reconcile(AST.JLS3, false, null, null);
		
		StringBuffer buffer = new StringBuffer();
		IProblem[] problems = unit.getProblems();
		for (int i = 0, length = problems.length; i < length; i++)
			Util.appendProblem(buffer, problems[i], contents.toCharArray(), i+1);
		if (buffer.length() > 0)
			System.err.println(buffer.toString());

		return findNode(unit, markerInfo);
	}
	
	protected ASTNode findNode(CompilationUnit unit, final MarkerInfo markerInfo) {
		class EndVisit extends RuntimeException {
			private static final long serialVersionUID = 1L;
		}
		class Visitor extends ASTVisitor {
			ASTNode found;
			public void preVisit(ASTNode node) {
				if (node instanceof CompilationUnit) return;
				if (node.getStartPosition() == markerInfo.astStart && node.getStartPosition() + node.getLength() == markerInfo.astEnd) {
					this.found = node;
					throw new EndVisit();
				}
			}
		}
		Visitor visitor = new Visitor();
		try {
			unit.accept(visitor);
		} catch (EndVisit e) {
			return visitor.found;
		}
		return null;
	}

}
