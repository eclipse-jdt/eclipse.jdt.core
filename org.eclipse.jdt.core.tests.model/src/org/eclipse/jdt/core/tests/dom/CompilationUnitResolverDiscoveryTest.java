/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.dom.ICompilationUnitResolver;

public class CompilationUnitResolverDiscoveryTest extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.getJLSLatest(), false);
	}

	public CompilationUnitResolverDiscoveryTest(String name) {
		super(name);
	}

	public static Test suite() {
		String javaVersion = System.getProperty("java.version");
		int index = -1;
		if ( (index = javaVersion.indexOf('-')) != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			if (javaVersion.length() > 3) {
				javaVersion = javaVersion.substring(0, 3);
			}
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
		}
		return buildModelTestSuite(CompilationUnitResolverDiscoveryTest.class);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testCompilationUnitResolverNoSysprop() throws JavaModelException {
		String SELECTED_SYSPROP = "ICompilationUnitResolver";
		String original = System.getProperty(SELECTED_SYSPROP);
		try {
			System.clearProperty(SELECTED_SYSPROP);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter9" , "src", "testBug497719_001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true, true);
			assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			ASTNode node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a compilation unit", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			IMethodBinding mb = methodDeclaration.resolveBinding();
			assertEquals(mb.getClass().getName(), "org.eclipse.jdt.core.dom.MethodBinding");
		} finally {
			if (original == null) {
				System.clearProperty(SELECTED_SYSPROP);
			} else {
				System.setProperty(SELECTED_SYSPROP, original);
			}
		}
	}

	public void testCompilationUnitResolverInvalidSysprop() throws JavaModelException {
		String SELECTED_SYSPROP = "ICompilationUnitResolver";
		String original = System.getProperty(SELECTED_SYSPROP);
		try {
			System.setProperty(SELECTED_SYSPROP, "doesNotExist");
			ICompilationUnit sourceUnit = getCompilationUnit("Converter9" , "src", "testBug497719_001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true, true);
			assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			ASTNode node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a compilation unit", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			IMethodBinding mb = methodDeclaration.resolveBinding();
			assertEquals(mb.getClass().getName(), "org.eclipse.jdt.core.dom.MethodBinding");
		} finally {
			if (original == null) {
				System.clearProperty(SELECTED_SYSPROP);
			} else {
				System.setProperty(SELECTED_SYSPROP, original);
			}
		}
	}

	public void testCompilationUnitResolverValidSysprop() throws JavaModelException {
		String SELECTED_SYSPROP = "ICompilationUnitResolver";
		String original = System.getProperty(SELECTED_SYSPROP);
		try {
			System.setProperty(SELECTED_SYSPROP, "org.eclipse.jdt.core.tests.model.resolver1");
			ICompilationUnit sourceUnit = getCompilationUnit("Converter9" , "src", "testBug497719_001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true, true);
			assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			PackageDeclaration pd = compilationUnit.getPackage();
			assertNotNull(pd);
			String pdName = pd.getName().toString();
			assertEquals(pdName, "compilationUnitResolverDiscoveryTest");
		} finally {
			if (original == null) {
				System.clearProperty(SELECTED_SYSPROP);
			} else {
				System.setProperty(SELECTED_SYSPROP, original);
			}
		}
	}


	/*
	 * Custom made interface that implements exactly to the test
	 */
	public static final class TEST_RESOLVER implements ICompilationUnitResolver {
		@Override
		public CompilationUnit toCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
				boolean initialNeedsToResolveBinding, IJavaProject project, List<Classpath> classpaths,
				int focalPosition, int apiLevel, Map<String, String> compilerOptions,
				WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags,
				IProgressMonitor monitor) {
			// Return a mostly-invalid hard-coded dom tree for the purpose of this test
			AST ast = AST.newAST(apiLevel, JavaCore.ENABLED.equals(compilerOptions.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)));
			CompilationUnit res = ast.newCompilationUnit();
			PackageDeclaration pack = ast.newPackageDeclaration();
			pack.setName(ast.newSimpleName("compilationUnitResolverDiscoveryTest"));
			res.setPackage(pack);
			return res;
		}

		@Override
		public void resolve(String[] sourceFilePaths, String[] encodings, String[] bindingKeys,
				FileASTRequestor requestor, int apiLevel, Map<String, String> compilerOptions,
				List<Classpath> classpathList, int flags, IProgressMonitor monitor) {
		}

		@Override
		public void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
				Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
			// irrelevant for test
		}

		@Override
		public void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
				Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
			// irrelevant for test
		}

		@Override
		public void resolve(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor,
				int apiLevel, Map<String, String> compilerOptions, IJavaProject project,
				WorkingCopyOwner workingCopyOwner, int flags, IProgressMonitor monitor) {
			// irrelevant for test
		}
	}
}
