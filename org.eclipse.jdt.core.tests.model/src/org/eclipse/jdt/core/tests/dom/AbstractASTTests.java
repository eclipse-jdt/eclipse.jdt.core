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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
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
	
	protected MarkerInfo[] createMarkerInfos(String[] pathAndSources) {
		MarkerInfo[] markerInfos = new MarkerInfo[pathAndSources.length / 2];
		int index = 0;
		for (int i = 0, length = pathAndSources.length; i < length; i++) {
			String path = pathAndSources[i];
			String source = pathAndSources[++i];
			markerInfos[index++] = new MarkerInfo(path, source);
		}
		return markerInfos;
	}

	protected ICompilationUnit[] createWorkingCopies(String[] pathAndSources, WorkingCopyOwner owner) throws JavaModelException {
		MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
		return createWorkingCopies(markerInfos, owner);
	}
	
	protected ICompilationUnit[] createWorkingCopies(MarkerInfo[] markerInfos, WorkingCopyOwner owner) throws JavaModelException {
		int length = markerInfos.length;
		ICompilationUnit[] workingCopies = new ICompilationUnit[length];
		for (int i = 0; i < length; i++) {
			MarkerInfo markerInfo = markerInfos[i];
			ICompilationUnit workingCopy = getCompilationUnit(markerInfo.path).getWorkingCopy(owner, null, null);
			workingCopy.getBuffer().setContents(markerInfo.source);
			workingCopy.makeConsistent(null);
			workingCopies[i] = workingCopy;
		}
		return workingCopies;
	}
	
	protected ASTNode findNode(CompilationUnit unit, final MarkerInfo markerInfo) {
		class EndVisit extends RuntimeException {
			private static final long serialVersionUID = 6009335074727417445L;
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
		return unit;
	}

	protected void resolveASTs(ICompilationUnit[] cus, String[] bindingKeys, ASTRequestor requestor, WorkingCopyOwner owner) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(getJavaProject("P"));
		parser.setWorkingCopyOwner(owner);
		parser.createASTs(cus, bindingKeys,  requestor, null);
	}
	
	protected IBinding[] resolveBindings(String[] bindingKeys, WorkingCopyOwner owner) {
		class BindingRequestor extends ASTRequestor {
			HashMap bindings = new HashMap();
			public void acceptBinding(String bindingKey, IBinding binding) {
				this.bindings.put(bindingKey, binding);
			}
		}
		BindingRequestor requestor = new BindingRequestor();
		resolveASTs(new ICompilationUnit[0], bindingKeys, requestor, owner);
		int length = requestor.bindings.size();
		IBinding[] result = new IBinding[length];
		for (int i = 0; i < length; i++) {
			result[i] = (IBinding) requestor.bindings.get(bindingKeys[i]);
		}
		return result;
	}
	
}
