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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;

import junit.framework.Test;

/*
 * Test the creation/resolution of several ASTs at once.
 */
public class BatchASTCreationTests extends AbstractASTTests {
	
	public class TestASTRequestor extends ASTRequestor {
		
		public ArrayList asts = new ArrayList();
		public ICompilationUnit[][] workingCopyBatches;
		public int currentBatch = 0;
		
		public TestASTRequestor(ICompilationUnit[] workingCopies) {
			this.workingCopyBatches= new ICompilationUnit[][] {workingCopies};
		}

		public TestASTRequestor(ICompilationUnit[][] workingCopyBatches) {
			this.workingCopyBatches= workingCopyBatches;
		}

		public void acceptAST(ASTNode node) {
			this.asts.add(node);
		}

		public ICompilationUnit[] getSources() {
			if (this.currentBatch == this.workingCopyBatches.length)
				return null;
			return this.workingCopyBatches[this.currentBatch++];
		}
	}
	
	public WorkingCopyOwner owner = new WorkingCopyOwner() {};

	public BatchASTCreationTests(String name) {
		super(name);
	}

	public static Test suite() {
		if (false) {
			Suite suite = new Suite(BatchASTCreationTests.class.getName());
			suite.addTest(new BatchASTCreationTests("test006"));
			return suite;
		}
		return new Suite(BatchASTCreationTests.class);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.5");
	}
	
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}
	
	private void createASTs(TestASTRequestor requestor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.createASTs(requestor, null);
	}
	
	private MarkerInfo[] createMarkerInfos(String[] pathAndSources) {
		MarkerInfo[] markerInfos = new MarkerInfo[pathAndSources.length / 2];
		int index = 0;
		for (int i = 0, length = pathAndSources.length; i < length; i++) {
			String path = pathAndSources[i];
			String source = pathAndSources[++i];
			markerInfos[index++] = new MarkerInfo(path, source);
		}
		return markerInfos;
	}

	private ICompilationUnit[] createWorkingCopies(String[] pathAndSources) throws JavaModelException {
		MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
		return createWorkingCopies(markerInfos);
	}
	
	private ICompilationUnit[] createWorkingCopies(MarkerInfo[] markerInfos) throws JavaModelException {
		int length = markerInfos.length;
		ICompilationUnit[] workingCopies = new ICompilationUnit[length];
		for (int i = 0; i < length; i++) {
			MarkerInfo markerInfo = markerInfos[i];
			ICompilationUnit workingCopy = getCompilationUnit(markerInfo.path).getWorkingCopy(this.owner, null, null);
			workingCopy.getBuffer().setContents(markerInfo.source);
			workingCopy.makeConsistent(null);
			workingCopies[i] = workingCopy;
		}
		return workingCopies;
	}
	
	private void discardWorkingCopies(ICompilationUnit[] workingCopies) throws JavaModelException {
		if (workingCopies == null) return;
		for (int i = 0, length = workingCopies.length; i < length; i++)
			workingCopies[i].discardWorkingCopy();
	}
	
	private void discardWorkingCopies(ICompilationUnit[][] workingCopyBatches) throws JavaModelException {
		if (workingCopyBatches == null) return;
		for (int i = 0, length = workingCopyBatches.length; i < length; i++)
			discardWorkingCopies(workingCopyBatches[i]);
	}
	
	private void resolveASTs(TestASTRequestor requestor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(getJavaProject("P"));
		parser.setWorkingCopyOwner(this.owner);
		parser.createASTs(requestor, null);
	}
	
	/*
	 * Test the batch creation of 2 ASTs without resolving.
	 */
	public void test001() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor(workingCopies);
			createASTs(requestor);
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}
	
	/*
	 * Test the batch creation of 2 ASTs with resolving.
	 */
	public void test002() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends /*start*/Y/*end*/ {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"/*start*/public class Y {\n" +
				"}/*end*/",
			});
			workingCopies = createWorkingCopies(markerInfos);
			TestASTRequestor requestor = new TestASTRequestor(workingCopies);
			resolveASTs(requestor);
			
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
			
			// compare the bindings coming from the 2 ASTs
			Type superX = (Type) findNode((CompilationUnit) requestor.asts.get(0), markerInfos[0]);
			TypeDeclaration typeY = (TypeDeclaration) findNode((CompilationUnit) requestor.asts.get(1), markerInfos[1]);
			IBinding superXBinding = superX.resolveBinding();
			IBinding typeYBinding = typeY.resolveBinding();
			assertTrue("Super of X and Y should be the same", superXBinding == typeYBinding);
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/*
	 * Test the creation of 3 batches of ASTs without resolving.
	 */
	public void test003() throws CoreException {
		ICompilationUnit[][] workingCopyBatches = new ICompilationUnit[3][];
		try {
			workingCopyBatches[0] = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			workingCopyBatches[1] = createWorkingCopies(new String[] {});
			workingCopyBatches[2] = createWorkingCopies(new String[] {
				"/P/p1/Z.java",
				"package p1;\n" +
				"public class Z {\n" +
				"  Object foo() {\n" +
				"    return new X();\n" +
				"  }\n" +
				"}",
			});
			
			TestASTRequestor requestor = new TestASTRequestor(workingCopyBatches);
			createASTs(requestor);
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Z {\n" + 
				"  Object foo(){\n" + 
				"    return new X();\n" + 
				"  }\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
		} finally {
			discardWorkingCopies(workingCopyBatches);
		}
	}
	
	/*
	 * Test the creation of 3 batches of ASTs with resolving.
	 */
	public void test004() throws CoreException {
		ICompilationUnit[][] workingCopyBatches = new ICompilationUnit[3][];
		try {
			MarkerInfo[] firstMarkerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start*/public class X extends Y {\n" +
				"}/*end*/",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			workingCopyBatches[0] = createWorkingCopies(firstMarkerInfos);
			workingCopyBatches[1] = createWorkingCopies(new String[] {});
			MarkerInfo[] lastMarkerInfos = createMarkerInfos(new String[] {
				"/P/p1/Z.java",
				"package p1;\n" +
				"public class Z {\n" +
				"  Object foo() {\n" +
				"    return new /*start*/X/*end*/();\n" +
				"  }\n" +
				"}",
			});
			workingCopyBatches[2] = createWorkingCopies(lastMarkerInfos);
			
			TestASTRequestor requestor = new TestASTRequestor(workingCopyBatches);
			resolveASTs(requestor);
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Z {\n" + 
				"  Object foo(){\n" + 
				"    return new X();\n" + 
				"  }\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
			
			// compare the bindings coming from the first AST and the last AST
			TypeDeclaration typeX = (TypeDeclaration) findNode((CompilationUnit) requestor.asts.get(0), firstMarkerInfos[0]);
			Type newX = (Type) findNode((CompilationUnit) requestor.asts.get(2), lastMarkerInfos[0]);
			IBinding typeXBinding = typeX.resolveBinding();
			IBinding newXBinding = newX.resolveBinding();
			assertTrue("Declaration of X and new X() should have the same binding", typeXBinding == newXBinding);
		} finally {
			discardWorkingCopies(workingCopyBatches);
		}
	}
	
	/*
	 * Ensure that ASTs that are required by original source but were not asked for are not handled.
	 */
	public void _test005() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		ICompilationUnit[] otherWorkingCopies = null;
		try {
			workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
			});
			otherWorkingCopies = createWorkingCopies(new String[] {
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor(workingCopies);
			resolveASTs(requestor);
			
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
		} finally {
			discardWorkingCopies(workingCopies);
			discardWorkingCopies(otherWorkingCopies);
		}
	}
	
	/*
	 * Test the creation of 2 batches of ASTs (first batch refers to CU in second batch) with resolving.
	 */
	public void _test006() throws CoreException {
		ICompilationUnit[][] workingCopyBatches = new ICompilationUnit[2][];
		try {
			MarkerInfo[] firstMarkerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends /*start*/Y/*end*/ {\n" +
				"}",
			});
			workingCopyBatches[0] = createWorkingCopies(firstMarkerInfos);
			MarkerInfo[] secondMarkerInfos = createMarkerInfos(new String[] {
				"/P/p1/Y.java",
				"package p1;\n" +
				"/*start*/public class Y {\n" +
				"}/*end*/",
			});
			workingCopyBatches[1] = createWorkingCopies(secondMarkerInfos);
			
			TestASTRequestor requestor = new TestASTRequestor(workingCopyBatches);
			resolveASTs(requestor);
			assertASTNodesEqual(
				"package p1;\n" + 
				"public class X extends Y {\n" + 
				"}\n" + 
				"\n" + 
				"package p1;\n" + 
				"public class Y {\n" + 
				"}\n" + 
				"\n",
				requestor.asts
			);
			
			// compare the bindings coming from the first AST and the last AST
			Type superX = (Type) findNode((CompilationUnit) requestor.asts.get(0), firstMarkerInfos[0]);
			TypeDeclaration typeY = (TypeDeclaration) findNode((CompilationUnit) requestor.asts.get(1), secondMarkerInfos[0]);
			IBinding superXBinding = superX.resolveBinding();
			IBinding typeYBinding = typeY.resolveBinding();
			assertTrue("Super of X and Y should be the same", superXBinding == typeYBinding);
		} finally {
			discardWorkingCopies(workingCopyBatches);
		}
	}
	

}
