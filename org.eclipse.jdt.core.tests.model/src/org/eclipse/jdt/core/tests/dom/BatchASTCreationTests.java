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
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

/*
 * Test the creation/resolution of several ASTs at once.
 */
public class BatchASTCreationTests extends AbstractASTTests {
	
	public class TestASTRequestor extends ASTRequestor {
		public ArrayList asts = new ArrayList();
		public void acceptAST(CompilationUnit ast, ICompilationUnit source) {
			this.asts.add(ast);
		}
		public void acceptBinding(IBinding binding, String bindingKey) {
		}
	}
	
	public WorkingCopyOwner owner = new WorkingCopyOwner() {};

	public BatchASTCreationTests(String name) {
		super(name);
	}

	public static Test suite() {
		if (false) {
			Suite suite = new Suite(BatchASTCreationTests.class.getName());
			suite.addTest(new BatchASTCreationTests("test009"));
			return suite;
		}
		return new Suite(BatchASTCreationTests.class);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
	}
	
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}
	
	/*
	 * Resolves the given cus as a batch. 
	 * While resolving, for the ASTNode that is marked, ensures that its binding key is the given one.
	 * On the cuNumber acceptAST(...), retrieve the binding from the given binding key and return it.
	 */
	private void assertBindingFound(final MarkerInfo[] markerInfos, final String expectedKey) throws JavaModelException {
		ICompilationUnit[] workingCopies;
		workingCopies = createWorkingCopies(markerInfos);
		class Requestor extends TestASTRequestor {
			String bindingKey;
			int index = -1;
			String foundKey;
			public void acceptAST(CompilationUnit cu, ICompilationUnit source) {
				super.acceptAST(cu, source);
				ASTNode node = findNode(cu, markerInfos[++this.index]);
				if (node != null) {
					IBinding binding = null;
					if (node instanceof PackageDeclaration) {
						binding = ((PackageDeclaration) node).resolveBinding();
					} else if (node instanceof TypeDeclaration) {
						binding = ((TypeDeclaration) node).resolveBinding();
					} else if (node instanceof AnonymousClassDeclaration) {
						binding = ((AnonymousClassDeclaration) node).resolveBinding();
					}
					this.bindingKey = binding == null ? null : binding.getKey();
				}
			}
			public void acceptBinding(IBinding binding, String key) {
				super.acceptBinding(binding, key);
				this.foundKey = binding.getKey();
			}
		};
		Requestor requestor = new Requestor();
		resolveASTs(workingCopies, new String[] {expectedKey}, requestor);
		
		if (!expectedKey.equals(requestor.bindingKey))
			System.out.println(Util.displayString(expectedKey, 3));
		assertEquals("Unexpected binding for marked node", expectedKey, requestor.bindingKey);
		
		if (!expectedKey.equals(requestor.foundKey)) {
			System.out.println(Util.displayString(requestor.foundKey, 3));
		}
		assertEquals("Unexpected binding found by acceptBinding", expectedKey, requestor.foundKey);
	}

	private void createASTs(ICompilationUnit[] cus, TestASTRequestor requestor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.createASTs(cus, new String[] {}, requestor, null);
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
	
	private void resolveASTs(ICompilationUnit[] cus, TestASTRequestor requestor) {
		resolveASTs(cus, new String[0], requestor);
	}
	
	private void resolveASTs(ICompilationUnit[] cus, String[] bindingKeys, TestASTRequestor requestor) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(getJavaProject("P"));
		parser.setWorkingCopyOwner(this.owner);
		parser.createASTs(cus, bindingKeys,  requestor, null);
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
			TestASTRequestor requestor = new TestASTRequestor();
			createASTs(workingCopies, requestor);
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
			TestASTRequestor requestor = new TestASTRequestor();
			resolveASTs(workingCopies, requestor);
			
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
	 * Ensure that ASTs that are required by original source but were not asked for are not handled.
	 */
	public void test003() throws CoreException {
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
			TestASTRequestor requestor = new TestASTRequestor();
			resolveASTs(workingCopies, requestor);
			
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
	 * Ensure that a package binding can be retrieved using its key.
	 */
	public void test004() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"/*start*/package p1;/*end*/\n" +
				"public class X {\n" +
				"}",
			});
			assertBindingFound(markerInfos, "p1");
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/*
	 * Ensure that a type binding can be retrieved using its key.
	 */
	public void test005() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start*/public class X extends Y {\n" +
				"}/*end*/",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			assertBindingFound(markerInfos, "p1/X");
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/*
	 * Ensure that a type binding can be retrieved using its key.
	 */
	public void test006() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"/*start*/public class Y {\n" +
				"}/*end*/",
			});
			assertBindingFound(markerInfos, "p1/Y");
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}
	
	/*
	 * Ensure that a member type binding can be retrieved using its key.
	 */
	public void test007() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  /*start*/class Member {\n" +
				"  }/*end*/" +
				"}",
			});
			assertBindingFound(markerInfos, "p1/X$Member");
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/*
	 * Ensure that a member type binding can be retrieved using its key.
	 */
	public void test008() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member1 {\n" +
				"    /*start*/class Member2 {\n" +
				"    }/*end*/" +
				"  }\n" +
				"}",
			});
			assertBindingFound(markerInfos, "p1/X$Member1$Member2");
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}
	/*
	 * Ensure that an anonymous type binding can be retrieved using its key.
	 */
	public void test009() throws CoreException {
		ICompilationUnit[] workingCopies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    new X() /*start*/{\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			});
			assertBindingFound(markerInfos, "p1/X$1");
		} finally {
			discardWorkingCopies(workingCopies);
		}
	}
}
