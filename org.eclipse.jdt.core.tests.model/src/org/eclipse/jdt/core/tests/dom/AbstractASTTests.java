/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.tests.model.ModifyingResourceTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractASTTests extends ModifyingResourceTests implements DefaultMarkedNodeLabelProviderOptions {

	/** @deprecated Using deprecated code */
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;
	public static final int astInternalJLS2() {
		return AST_INTERNAL_JLS2;
	}

	// TODO (frederic) use this field while converting instead of method argument
	protected int testLevel = AST_INTERNAL_JLS2;

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

		String markerStartStart;
		String markerStartEnd;
		String markerEndStart;
		String markerEndEnd;

		int[] astStarts, astEnds;

		public MarkerInfo(String source) {
			this(null, source);
		}
		public MarkerInfo(String path, String source) {
			this(path, source, "/*", "*/", "start", "end");
		}
		public MarkerInfo(
				String source,
				String markerBeginning,
				String markerEnding,
				String markerStart,
				String markerEnd) {
			this(null, source, markerBeginning, markerEnding, markerStart, markerEnd);
		}

		public MarkerInfo(
				String path,
				String source,
				String markerBeginning,
				String markerEnding,
				String markerStart,
				String markerEnd) {
			this.path = path;
			this.source = source;

			this.markerStartStart = markerBeginning + markerStart;
			this.markerStartEnd = markerEnding;
			this.markerEndStart = markerBeginning + markerEnd;
			this.markerEndEnd = markerEnding;

			int markerIndex = 1;
			while (source.indexOf(this.markerStartStart + markerIndex + this.markerStartEnd) != -1) {
				markerIndex++;
			}
			int astNumber = source.indexOf(this.markerStartStart + this.markerStartEnd) != -1 ? markerIndex : markerIndex-1;
			this.astStarts = new int[astNumber];
			this.astEnds = new int[astNumber];

			for (int i = 1; i < markerIndex; i++)
				setStartAndEnd(i);
			if (astNumber == markerIndex)
				setStartAndEnd(-1);
		}

		public int indexOfASTStart(int astStart) {
			return this.indexOfASTStart(astStart, 0);
		}

		public int indexOfASTStart(int astStart, int fromIndex) {
			for (int i = fromIndex, length = this.astStarts.length; i < length; i++)
				if (this.astStarts[i] == astStart)
					return i;
			return -1;
		}

		private void removeMarkerFromSource(String marker, int sourceIndex, int astNumber) {
			int markerLength = marker.length();
			this.source = this.source.substring(0, sourceIndex).concat(this.source.substring(sourceIndex + markerLength));
			// shift previously recorded positions
			for (int i = 0; i < astNumber; i++) {
				if (this.astStarts[i] > sourceIndex)
					this.astStarts[i] -= markerLength;
				if (this.astEnds[i] > sourceIndex)
					this.astEnds[i] -= markerLength;
			}
		}

		private void setStartAndEnd(int markerIndex) {
			String markerNumber;
			if (markerIndex == -1) {
				markerNumber = "";
				markerIndex = this.astStarts.length; // *start* is always last
			} else
				markerNumber = Integer.toString(markerIndex);

			String markerStart = this.markerStartStart + markerNumber + this.markerStartEnd;
			String markerEnd = this.markerEndStart + markerNumber + this.markerEndEnd;
			int astStart = this.source.indexOf(markerStart); // start of AST inclusive
			this.astStarts[markerIndex-1] = astStart;
			removeMarkerFromSource(markerStart, astStart, markerIndex-1);
			int astEnd = this.source.indexOf(markerEnd); // end of AST exclusive
			this.astEnds[markerIndex-1] = astEnd;
			removeMarkerFromSource(markerEnd, astEnd, markerIndex-1);
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

	public static class ASTResult {
		public String result; // marked ast output
		public String source; // source without marker
		public ASTResult(String result, String source) {
			this.result = result;
			this.source = source;
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
		assertBindingsEqual("Unexpected bindings", expected, actualBindings);
	}

	protected void assertBindingsEqual(String message, String expected, IBinding[] actualBindings) {
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
			System.out.print(displayString(actual, 3));
			System.out.println(',');
		}
		assertEquals(message, expected, actual);
	}

	protected void assertASTResult(
			String expected,
			ASTResult actual) throws JavaModelException {

		if (!expected.equals(actual.result)) {
			System.out.println();
			System.out.println(actual.source);
			System.out.println(Util.displayString(actual.result, 3));
		}

		assertEquals(expected, actual.result);
	}

	// TODO (frederic) replace all ASTParser creation with by calling this method
	protected ASTParser createASTParser() {
	    ASTParser parser = ASTParser.newParser(this.testLevel);
	    return parser;
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
				assertEquals("Unexpected errors", "", buffer.toString());
		}

		return findNode(unit, markerInfo);
	}

	protected ASTNode buildAST(ICompilationUnit cu) throws JavaModelException {
		return buildAST(null/*use existing contents*/, cu, true/*report errors*/);
	}

	protected ASTNode buildAST(String newContents, ICompilationUnit cu) throws JavaModelException {
		return buildAST(newContents, cu, true/*report errors*/);
	}

	protected ASTNode buildAST(MarkerInfo markerInfo, IClassFile classFile) throws JavaModelException {
		return buildAST(markerInfo, classFile, true/*report errors*/);
	}

	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, and returns the AST node that was delimited
	 * by "*start*" and "*end*".
	 */
	protected ASTNode buildAST(String newContents, ICompilationUnit cu, boolean reportErrors) throws JavaModelException {
		return buildAST(newContents, cu, reportErrors, false/*no statement recovery*/, false);
	}

	protected ASTNode buildAST(int JLSLevel, String newContents, ICompilationUnit cu, boolean reportErrors, boolean enableStatementRecovery, boolean bindingRecovery) throws JavaModelException {
		ASTNode[] nodes = buildASTs(JLSLevel, newContents, cu, reportErrors, enableStatementRecovery, bindingRecovery);
		if (nodes.length == 0) return null;
		return nodes[0];
	}
	protected ASTNode buildAST(String newContents, ICompilationUnit cu, boolean reportErrors, boolean enableStatementRecovery, boolean bindingRecovery) throws JavaModelException {
		ASTNode[] nodes = buildASTs(newContents, cu, reportErrors, enableStatementRecovery, bindingRecovery);
		if (nodes.length == 0) return null;
		return nodes[0];
	}

	protected ASTNode buildAST(String newContents, ICompilationUnit cu, boolean reportErrors, boolean enableStatementRecovery) throws JavaModelException {
		ASTNode[] nodes = buildASTs(newContents, cu, reportErrors, enableStatementRecovery, false);
		if (nodes.length == 0) return null;
		return nodes[0];
	}

	protected ASTNode buildAST(String newContents, ICompilationUnit cu, int flags) throws JavaModelException {
		ASTNode[] nodes = buildASTs(newContents, cu, flags);
		if (nodes.length == 0) return null;
		return nodes[0];
	}

	private ASTNode[] buildASTs(String newContents, ICompilationUnit cu, int flags) throws JavaModelException {
		MarkerInfo markerInfo;
		if (newContents == null) {
			markerInfo = new MarkerInfo(cu.getSource());
		} else {
			markerInfo = new MarkerInfo(newContents);
		}
		newContents = markerInfo.source;

		CompilationUnit unit;
		if (cu.isWorkingCopy()) {
			cu.getBuffer().setContents(newContents);
			unit = cu.reconcile(AST.JLS3, flags, null, null);
		} else {
			IBuffer buffer = cu.getBuffer();
			buffer.setContents(newContents);
			buffer.save(null, false);

			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(cu);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery((flags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);
			unit = (CompilationUnit) parser.createAST(null);
		}

		if ((flags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0) {
			StringBuffer buffer = new StringBuffer();
			IProblem[] problems = unit.getProblems();
			for (int i = 0, length = problems.length; i < length; i++)
				Util.appendProblem(buffer, problems[i], newContents.toCharArray(), i+1);
			if (buffer.length() > 0)
				assertEquals("Unexpected errors", "", buffer.toString());
		}

		ASTNode[] nodes = findNodes(unit, markerInfo);
		if (nodes.length == 0)
			return new ASTNode[] {unit};
		return nodes;
	}

	protected ASTNode[] buildASTs(String contents, ICompilationUnit cu) throws JavaModelException {
		return buildASTs(contents, cu, true);
	}

	/*
	 * Removes the marker comments "*start?*" and "*end?*" from the given new contents
	 * (where ? is either empty or a number), or use the current contents if the given new contents is null.
	 * Builds an AST from the resulting source.
	 * For each of the pairs, returns the AST node that was delimited by "*start?*" and "*end?*".
	 */
	protected ASTNode[] buildASTs(String newContents, ICompilationUnit cu, boolean reportErrors) throws JavaModelException {
		return buildASTs(newContents, cu, reportErrors, false, false);
	}
	protected ASTNode[] buildASTs(int JLSLevel, String newContents, ICompilationUnit cu, boolean reportErrors, boolean enableStatementRecovery, boolean bindingRecovery) throws JavaModelException {
		MarkerInfo markerInfo;
		if (newContents == null) {
			markerInfo = new MarkerInfo(cu.getSource());
		} else {
			markerInfo = new MarkerInfo(newContents);
		}
		newContents = markerInfo.source;

		CompilationUnit unit;
		if (cu.isWorkingCopy()) {
			cu.getBuffer().setContents(newContents);
			int flags = 0;
			if (reportErrors) flags |= ICompilationUnit.FORCE_PROBLEM_DETECTION;
			if (enableStatementRecovery) flags |= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
			if (bindingRecovery) flags |= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
			unit = cu.reconcile(JLSLevel, flags, null, null);
		} else {
			IBuffer buffer = cu.getBuffer();
			buffer.setContents(newContents);
			buffer.save(null, false);

			ASTParser parser = ASTParser.newParser(JLSLevel);
			parser.setSource(cu);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(enableStatementRecovery);
			parser.setBindingsRecovery(bindingRecovery);
			unit = (CompilationUnit) parser.createAST(null);
		}

		if (reportErrors) {
			StringBuffer buffer = new StringBuffer();
			IProblem[] problems = unit.getProblems();
			for (int i = 0, length = problems.length; i < length; i++)
				Util.appendProblem(buffer, problems[i], newContents.toCharArray(), i+1);
			if (buffer.length() > 0)
				assertEquals("Unexpected errors", "", buffer.toString());
		}

		ASTNode[] nodes = findNodes(unit, markerInfo);
		if (nodes.length == 0)
			return new ASTNode[] {unit};
		return nodes;
	}
	protected ASTNode[] buildASTs(String newContents, ICompilationUnit cu, boolean reportErrors, boolean enableStatementRecovery, boolean bindingRecovery) throws JavaModelException {
		String option = cu.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
		long jdkLevel = CompilerOptions.versionToJdkLevel(option);
		int JLSLevel = AST.JLS3;
		if (jdkLevel >= ClassFileConstants.JDK1_7) {
			JLSLevel = AST.JLS4;
		}
		return buildASTs(
				JLSLevel,
				newContents,
				cu,
				reportErrors,
				enableStatementRecovery,
				bindingRecovery);
	}

	protected ASTResult buildMarkedAST(
			String path,
			String content) throws JavaModelException {
		return
			this.buildMarkedAST(
					path,
					content,
					true, // enable statement recovery
					true, // report ast
					true, // report problems
					new MarkedASTFlattener.DefaultMarkedNodeLabelProvider(ALL_OPTIONS));
	}

	protected ASTResult buildMarkedAST(
			String path,
			String content,
			boolean enableStatementRecovery,
			boolean reportAST,
			boolean reportProblems,
			int options) throws JavaModelException {
		return
			this.buildMarkedAST(
					path,
					content,
					enableStatementRecovery,
					reportAST,
					reportProblems,
					new MarkedASTFlattener.DefaultMarkedNodeLabelProvider(options));
	}

	/*
	 * Removes the marker comments "[*?*]" from the given new contents
	 * (where ? is either empty or a number), or use the current contents if the given new contents is null.
	 * Builds an AST from the resulting source and information about marked nodes.
	 */
	protected ASTResult buildMarkedAST(
			String path,
			String content,
			boolean enableStatementRecovery,
			boolean reportAST,
			boolean reportProblems,
			MarkedASTFlattener.DefaultMarkedNodeLabelProvider labelProvider) throws JavaModelException {

		MarkerInfo markerInfo;
		markerInfo = new MarkerInfo(content, "[*", "*]", "", "");
		content = markerInfo.source;

		ICompilationUnit compilationUnit = getWorkingCopy(path, content, true);

		ASTParser parser = createASTParser();
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		parser.setWorkingCopyOwner(this.wcOwner);
		parser.setStatementsRecovery(enableStatementRecovery);
		CompilationUnit unit = (CompilationUnit)parser.createAST(null);

		MarkedASTFlattener flattener =
			new MarkedASTFlattener(
					reportAST,
					reportProblems,
					labelProvider);
		flattener.process(unit, markerInfo);

		ASTResult result = new ASTResult(flattener.getResult(), markerInfo.source);

		compilationUnit.discardWorkingCopy();

		return result;
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

	protected IVariableBinding[] createVariableBindings(String[] pathAndSources, String[] bindingKeys) throws JavaModelException {
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = createWorkingCopies(pathAndSources, owner);
		IBinding[] bindings = resolveBindings(bindingKeys, getJavaProject("P"), owner);
		int length = bindings.length;
		IVariableBinding[] result = new IVariableBinding[length];
		System.arraycopy(bindings, 0, result, 0, length);
		return result;
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
			ICompilationUnit workingCopy = getCompilationUnit(markerInfo.path).getWorkingCopy(owner, null);
			workingCopy.getBuffer().setContents(markerInfo.source);
			workingCopy.makeConsistent(null);
			copies[i] = workingCopy;
		}
		return copies;
	}

	protected ASTNode findNode(CompilationUnit unit, final MarkerInfo markerInfo) {
		ASTNode[] nodes = findNodes(unit, markerInfo);
		if (nodes.length == 0)
			return unit;
		return nodes[0];
	}

	protected ASTNode[] findNodes(CompilationUnit unit, final MarkerInfo markerInfo) {
		class Visitor extends ASTVisitor {
			ArrayList found = new ArrayList();
			public void preVisit(ASTNode node) {
				if (node instanceof CompilationUnit) return;
				int index = markerInfo.indexOfASTStart(node.getStartPosition());
				if (index != -1 && node.getStartPosition() + node.getLength() == markerInfo.astEnds[index]) {
					this.found.add(node);
					markerInfo.astStarts[index] = -1; // so that 2 nodes with the same start and end will not be found
				}
			}
		}
		Visitor visitor = new Visitor();
		unit.accept(visitor);
		int size = visitor.found.size();
		ASTNode[] result = new ASTNode[size];
		visitor.found.toArray(result);
		return result;
	}

	/**
	 * Create a new working copy owner using given problem requestor
	 * to report problem.
	 *
	 * @param problemRequestor The requestor used to report problems
	 * @return The created working copy owner
	 */
	protected WorkingCopyOwner newWorkingCopyOwner(final IProblemRequestor problemRequestor) {
		return new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return problemRequestor;
			}
		};
	}

	protected void resolveASTs(ICompilationUnit[] cus, String[] bindingKeys, ASTRequestor requestor, IJavaProject project, WorkingCopyOwner owner) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(project);
		parser.setWorkingCopyOwner(owner);
		parser.createASTs(cus, bindingKeys,  requestor, null);
	}

	protected IBinding resolveBinding(ASTNode node) {
		switch (node.getNodeType()) {
			case ASTNode.PACKAGE_DECLARATION:
				return ((PackageDeclaration) node).resolveBinding();
			case ASTNode.TYPE_DECLARATION:
				return ((TypeDeclaration) node).resolveBinding();
			case ASTNode.ANONYMOUS_CLASS_DECLARATION:
				return ((AnonymousClassDeclaration) node).resolveBinding();
			case ASTNode.TYPE_DECLARATION_STATEMENT:
				return ((TypeDeclarationStatement) node).resolveBinding();
			case ASTNode.METHOD_DECLARATION:
				return ((MethodDeclaration) node).resolveBinding();
			case ASTNode.METHOD_INVOCATION:
				return ((MethodInvocation) node).resolveMethodBinding();
			case ASTNode.TYPE_PARAMETER:
				return ((TypeParameter) node).resolveBinding();
			case ASTNode.PARAMETERIZED_TYPE:
				return ((ParameterizedType) node).resolveBinding();
			case ASTNode.WILDCARD_TYPE:
				return ((WildcardType) node).resolveBinding();
			case ASTNode.SIMPLE_NAME:
				return ((SimpleName) node).resolveBinding();
			case ASTNode.ARRAY_TYPE:
				return ((ArrayType) node).resolveBinding();
			case ASTNode.ASSIGNMENT:
				return ((Assignment) node).getRightHandSide().resolveTypeBinding();
			case ASTNode.SIMPLE_TYPE:
				return ((SimpleType) node).resolveBinding();
			case ASTNode.QUALIFIED_NAME:
				return ((QualifiedName) node).resolveBinding();
			case ASTNode.MARKER_ANNOTATION:
				return ((MarkerAnnotation) node).resolveAnnotationBinding();
			case ASTNode.NORMAL_ANNOTATION:
				return ((NormalAnnotation) node).resolveAnnotationBinding();
			case ASTNode.SINGLE_MEMBER_ANNOTATION:
				return ((SingleMemberAnnotation) node).resolveAnnotationBinding();
			case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
				return ((VariableDeclarationFragment) node).resolveBinding();
			default:
				throw new Error("Not yet implemented for this type of node: " + node);
		}
	}

	protected IBinding[] resolveBindings(String[] bindingKeys, IJavaProject project, WorkingCopyOwner owner) {
		BindingRequestor requestor = new BindingRequestor();
		resolveASTs(new ICompilationUnit[0], bindingKeys, requestor, project, owner);
		return requestor.getBindings(bindingKeys);
	}

	/*
	 * Resolve the bindings of the nodes marked with *start?* and *end?*.
	 */
	protected IBinding[] resolveBindings(String contents, ICompilationUnit cu) throws JavaModelException {
		return resolveBindings(contents, cu, true/*report errors*/);
	}

	/*
	 * Resolve the bindings of the nodes marked with *start?* and *end?*.
	 */
	protected IBinding[] resolveBindings(String contents, ICompilationUnit cu, boolean reportErrors) throws JavaModelException {
		ASTNode[] nodes = buildASTs(contents, cu, reportErrors);
		if (nodes == null) return null;
		int length = nodes.length;
		IBinding[] result = new IBinding[length];
		for (int i = 0; i < length; i++) {
			result[i] = resolveBinding(nodes[i]);
		}
		return result;
	}

	protected ASTNode buildAST(IClassFile classFile) throws JavaModelException {
		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("/*start*/") + "/*start*/".length()};
		markerInfo.astEnds = new int[] {source.indexOf("/*end*/")};
		ASTNode node = buildAST(markerInfo, classFile);
		return node;
	}


//	protected void tearDown() throws Exception {
//		discardWorkingCopies(this.workingCopies);
//		this.workingCopies = null;
//	}

}
