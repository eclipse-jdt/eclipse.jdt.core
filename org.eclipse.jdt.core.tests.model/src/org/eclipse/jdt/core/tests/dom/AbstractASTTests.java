/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.tests.model.ModifyingResourceTests;
import org.eclipse.jdt.core.tests.util.Util;

public class AbstractASTTests extends ModifyingResourceTests {

//	ICompilationUnit[] workingCopies;

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
	
	public class BindingRequestor extends ASTRequestor {
		HashMap bindings = new HashMap();
		public void acceptBinding(String bindingKey, IBinding binding) {
			this.bindings.put(bindingKey, binding);
		}
		public IBinding[] getBindings(String[] bindingKeys) {
			int length = this.bindings.size();
			IBinding[] result = new IBinding[length];
			for (int i = 0; i < length; i++) {
				result[i] = (IBinding) this.bindings.get(bindingKeys[i]);
			}
			return result;
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

	protected void assertBindingEquals(String expected, IBinding binding) {
		assertBindingsEqual(expected, new IBinding[] {binding});
	}
	
	protected void assertBindingsEqual(String expected, IBinding[] actualBindings) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = actualBindings.length; i < length; i++) {
			if (i > 0) buffer.append('\n');
			if (actualBindings[i] == null)
				buffer.append("<null>");
			else
				buffer.append(actualBindings[i].getKey());
		}
		String actual = buffer.toString();
		if (!expected.equals(actual)) {
			System.out.print(displayString(actual, 4));
			System.out.println(',');
		}
		assertEquals(
			"Unexpected bindings",
			expected,
			actual);
	}

	/*
	 * Builds an AST from the info source (which is assumed to be the source attached to the given class file), 
	 * and returns the AST node that was delimited by the astStart and astEnd of the marker info.
	 */
	protected ASTNode buildAST(MarkerInfo markerInfo, IClassFile classFile, boolean reportErrors) throws JavaModelException {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(classFile);
		parser.setResolveBindings(true);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		
		if (reportErrors) {
			StringBuffer buffer = new StringBuffer();
			IProblem[] problems = unit.getProblems();
			for (int i = 0, length = problems.length; i < length; i++)
				Util.appendProblem(buffer, problems[i], markerInfo.source.toCharArray(), i+1);
			if (buffer.length() > 0)
				System.err.println(buffer.toString());
		}

		return findNode(unit, markerInfo);
	}

	protected ASTNode buildAST(MarkerInfo markerInfo, IClassFile classFile) throws JavaModelException {
		return buildAST(markerInfo, classFile, true);
	}

	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, and returns the AST node that was delimited
	 * by "*start*" and "*end*".
	 */
	protected ASTNode buildAST(String contents, ICompilationUnit cu, boolean reportErrors) throws JavaModelException {
		MarkerInfo markerInfo = new MarkerInfo(contents);
		contents = markerInfo.source;

		cu.getBuffer().setContents(contents);
		CompilationUnit unit = cu.reconcile(AST.JLS3, false, null, null);
		
		if (reportErrors) {
			StringBuffer buffer = new StringBuffer();
			IProblem[] problems = unit.getProblems();
			for (int i = 0, length = problems.length; i < length; i++)
				Util.appendProblem(buffer, problems[i], contents.toCharArray(), i+1);
			if (buffer.length() > 0)
				System.err.println(buffer.toString());
		}

		return findNode(unit, markerInfo);
	}

	protected ASTNode buildAST(String contents, ICompilationUnit cu) throws JavaModelException {
		return buildAST(contents, cu, true);
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

	protected IMethodBinding[] createMethodBindings(String[] pathAndSources, String[] bindingKeys) throws JavaModelException {
		return createMethodBindings(pathAndSources, bindingKeys, getJavaProject("P"));
	}

	protected IMethodBinding[] createMethodBindings(String[] pathAndSources, String[] bindingKeys, IJavaProject project) throws JavaModelException {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = createWorkingCopies(pathAndSources, owner);
		IBinding[] bindings = resolveBindings(bindingKeys, project, owner);
		int length = bindings.length;
		IMethodBinding[] result = new IMethodBinding[length];
		System.arraycopy(bindings, 0, result, 0, length);
		return result;
	}

	protected ITypeBinding[] createTypeBindings(String[] pathAndSources, String[] bindingKeys) throws JavaModelException {
		return createTypeBindings(pathAndSources, bindingKeys, getJavaProject("P"));
	}
	
	protected ITypeBinding[] createTypeBindings(String[] pathAndSources, String[] bindingKeys, IJavaProject project) throws JavaModelException {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = createWorkingCopies(pathAndSources, owner);
		IBinding[] bindings = resolveBindings(bindingKeys, project, owner);
		int length = bindings.length;
		ITypeBinding[] result = new ITypeBinding[length];
		System.arraycopy(bindings, 0, result, 0, length);
		return result;
	}

	protected ICompilationUnit[] createWorkingCopies(String[] pathAndSources, WorkingCopyOwner owner) throws JavaModelException {
		MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
		return createWorkingCopies(markerInfos, owner);
	}
	
	protected ICompilationUnit[] createWorkingCopies(MarkerInfo[] markerInfos, WorkingCopyOwner owner) throws JavaModelException {
		int length = markerInfos.length;
		ICompilationUnit[] copies = new ICompilationUnit[length];
		for (int i = 0; i < length; i++) {
			MarkerInfo markerInfo = markerInfos[i];
			ICompilationUnit workingCopy = getCompilationUnit(markerInfo.path).getWorkingCopy(owner, null, null);
			workingCopy.getBuffer().setContents(markerInfo.source);
			workingCopy.makeConsistent(null);
			copies[i] = workingCopy;
		}
		return copies;
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

	protected void resolveASTs(ICompilationUnit[] cus, String[] bindingKeys, ASTRequestor requestor, IJavaProject project, WorkingCopyOwner owner) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(project);
		parser.setWorkingCopyOwner(owner);
		parser.createASTs(cus, bindingKeys,  requestor, null);
	}
	
	protected IBinding[] resolveBindings(String[] bindingKeys, IJavaProject project, WorkingCopyOwner owner) {
		BindingRequestor requestor = new BindingRequestor();
		resolveASTs(new ICompilationUnit[0], bindingKeys, requestor, project, owner);
		return requestor.getBindings(bindingKeys);
	}
	
//	protected void tearDown() throws Exception {
//		discardWorkingCopies(this.workingCopies);
//		this.workingCopies = null;
//	}
	
}
