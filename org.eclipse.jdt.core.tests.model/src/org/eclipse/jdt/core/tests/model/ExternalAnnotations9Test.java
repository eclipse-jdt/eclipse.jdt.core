/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil;
import org.eclipse.jdt.core.util.ExternalAnnotationUtil.MergeStrategy;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ExternalAnnotations9Test extends ExternalAnnotations18Test {

	public ExternalAnnotations9Test(String name) {
		super(name, "9", "JCL19_LIB");
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] {"testBug522401"};
	}

	public static Test suite() {
		return buildModelTestSuite(ExternalAnnotations9Test.class, BYTECODE_DECLARATION_ORDER);
	}

	@Override
	public String getSourceWorkspacePath() {
		// we read individual projects from within this folder:
		return super.getSourceWorkspacePathBase()+"/ExternalAnnotations9";
	}

	protected boolean hasJRE19() {
		return ((AbstractCompilerTest.getPossibleComplianceLevels() & AbstractCompilerTest.F_9) != 0);
	}
	@Deprecated
	static int getJSL9() {
		return AST.JLS9;
	}

	/** Project with real JRE. */
	public void testBug522401() throws Exception {
		if (!hasJRE19()) {
			System.out.println("Skipping ExternalAnnotations9Test.testBug522401(), needs JRE9");
			return;
		}
		Hashtable options = JavaCore.getOptions();
		try {
			setupJavaProject("Test2");
			this.project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = this.project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			assertNoMarkers(markers);
		} finally {
			JavaCore.setOptions(options);
		}
	}

	public void testBug525712() throws Exception {
		myCreateJavaProject("TestLibs");
		String lib1Content =
				"package libs;\n" +
				"\n" +
				"public abstract class Lib1 {\n" +
				"	public abstract void take(X x);\n" +
				"}\n";
		addLibraryWithExternalAnnotations(this.project, "lib1.jar", "annots", new String[] {
				"/UnannotatedLib/module-info.java",
				"module testlib {\n" +
				"	exports libs;\n" +
				"}\n",
				"/UnannotatedLib/libs/Lib1.java",
				lib1Content,

				"/UnannotatedLib/libs/X.java",
				"package libs;\n" +
				"public abstract class X {\n" +
				"}\n",


			}, null);

		// type check sources:
		IPackageFragment fragment = this.project.getPackageFragmentRoots()[0].createPackageFragment("tests", true, null);
		ICompilationUnit cu = fragment.createCompilationUnit("Test1.java",
				"package tests;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"import libs.Lib1;\n" +
				"import libs.X;\n" +
				"\n" +
				"@NonNullByDefault\n" +
				"public abstract class Test1 extends Lib1 {\n" +
				"	public abstract void take(X x);\n" +
				"}\n",
				true, new NullProgressMonitor()).getWorkingCopy(new NullProgressMonitor());
		CompilationUnit reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertProblems(reconciled.getProblems(), new String[] {
				"Pb(916) Illegal redefinition of parameter x, inherited method from Lib1 does not constrain this parameter"
		}, new int[] { 8 });

		// acquire library AST:
		IType type = this.project.findType("libs.Lib1");
		ICompilationUnit libWorkingCopy = type.getClassFile().getWorkingCopy(this.wcOwner, null);
		ASTParser parser = ASTParser.newParser(getJSL9());
		parser.setSource(libWorkingCopy);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		libWorkingCopy.discardWorkingCopy();

		// find type binding:
		int start = lib1Content.indexOf("take");
		ASTNode name = NodeFinder.perform(unit, start, 0);
		assertTrue("should be simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		ASTNode method = name.getParent();
		IMethodBinding methodBinding = ((MethodDeclaration)method).resolveBinding();

		// find annotation file (not yet existing):
		IFile annotationFile = ExternalAnnotationUtil.getAnnotationFile(this.project, methodBinding.getDeclaringClass(), null);
		assertFalse("file should not exist", annotationFile.exists());
		assertEquals("file path", "/TestLibs/annots/libs/Lib1.eea", annotationFile.getFullPath().toString());

		// annotate:
		String originalSignature = ExternalAnnotationUtil.extractGenericSignature(methodBinding);
		ExternalAnnotationUtil.annotateMember("libs/Lib1", annotationFile,
				"take",
				originalSignature,
				"(L1libs/X;)V",
				MergeStrategy.OVERWRITE_ANNOTATIONS, null);
		assertTrue("file should exist", annotationFile.exists());

		// check that the error is resolved now:
		reconciled = cu.reconcile(getJSL9(), true, null, new NullProgressMonitor());
		assertNoProblems(reconciled.getProblems());
	}
}
