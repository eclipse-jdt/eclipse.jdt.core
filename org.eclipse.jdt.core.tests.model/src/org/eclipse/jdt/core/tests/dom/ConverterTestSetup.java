/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;

public abstract class ConverterTestSetup extends AbstractJavaModelTests {

	protected AST ast;

	protected ConverterTestSetup(String name) {
		super(name);
	}

	protected static String getConverterJCLPath() {
		return AbstractJavaModelTests.EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMin.jar"; //$NON-NLS-1$
	}

	protected static String getConverterJCLSourcePath() {
		return AbstractJavaModelTests.EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMinsrc.zip"; //$NON-NLS-1$
	}

	protected static String getConverterJCLRootSourcePath() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Check locally for the required JCL files, jclMin.jar and jclMinsrc.zip.
	 * If not available, copy from the project resources.
	 */
	public void setupConverterJCL() throws IOException {
		String separator = java.io.File.separator;
		String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL"; //$NON-NLS-1$
		String localJCLPath =getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
		EXTERNAL_JAR_DIR_PATH = localJCLPath;
		java.io.File jclDir = new java.io.File(localJCLPath);
		java.io.File jclMin =
			new java.io.File(localJCLPath + separator + "converterJclMin.jar"); //$NON-NLS-1$
		java.io.File jclMinsrc = new java.io.File(localJCLPath + separator + "converterJclMinsrc.zip"); //$NON-NLS-1$
		if (!jclDir.exists()) {
			if (!jclDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + jclDir); //$NON-NLS-1$
			}
			//copy the two files to the JCL directory
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "converterJclMin.jar"); //$NON-NLS-1$
			copy(resourceJCLMin, jclMin);
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "converterJclMinsrc.zip"); //$NON-NLS-1$
			copy(resourceJCLMinsrc, jclMinsrc);
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing or less recent than the one in workspace
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "converterJclMin.jar"); //$NON-NLS-1$
			if (jclMin.lastModified() < resourceJCLMin.lastModified()) {
				copy(resourceJCLMin, jclMin);
			}
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "converterJclMinsrc.zip"); //$NON-NLS-1$
			if (jclMinsrc.lastModified() < resourceJCLMinsrc.lastModified()) {
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		}
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	public void tearDownSuite() throws Exception {
		ast = null;
		this.deleteProject("Converter"); //$NON-NLS-1$
		
		super.tearDown();
	}	

	/**
	 * Create project and set the jar placeholder.
	 */
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setupConverterJCL();
		ast = AST.newAST(AST.JLS2);
		setUpJavaProject("Converter"); //$NON-NLS-1$
		// ensure variables are set
		if (JavaCore.getClasspathVariable("ConverterJCL_LIB") == null) { //$NON-NLS-1$
			JavaCore.setClasspathVariables(
				new String[] {"CONVERTER_JCL_LIB", "CONVERTER_JCL_SRC", "CONVERTER_JCL_SRCROOT"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new Path[] {new Path(ConverterTestSetup.getConverterJCLPath()), new Path(ConverterTestSetup.getConverterJCLSourcePath()), new Path(ConverterTestSetup.getConverterJCLRootSourcePath())},
				null);
		}		
	}	

	public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(unit);
		parser.setResolveBindings(resolveBindings);
		return parser.createAST(null);
	}

	public ASTNode runConversion(ICompilationUnit unit, int position, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(unit);
		parser.setFocalPosition(position);
		parser.setResolveBindings(resolveBindings);
		return parser.createAST(null);
	}

	public ASTNode runConversion(IClassFile classFile, int position, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(classFile);
		parser.setFocalPosition(position);
		parser.setResolveBindings(resolveBindings);
		return parser.createAST(null);
	}
	
	public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		return parser.createAST(null);
	}

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map options) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		parser.setCompilerOptions(options);
		return parser.createAST(null);
	}	

	protected ASTNode getASTNodeToCompare(org.eclipse.jdt.core.dom.CompilationUnit unit) {
		ExpressionStatement statement = (ExpressionStatement) getASTNode(unit, 0, 0, 0);
		return (ASTNode) ((MethodInvocation) statement.getExpression()).arguments().get(0);
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex, int statementIndex) {
		BodyDeclaration bodyDeclaration = (BodyDeclaration)((TypeDeclaration)unit.types().get(typeIndex)).bodyDeclarations().get(bodyIndex);
		if (bodyDeclaration instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
			Block block = methodDeclaration.getBody();
			return (ASTNode) block.statements().get(statementIndex);
		} else if (bodyDeclaration instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclaration;
			return (ASTNode) typeDeclaration.bodyDeclarations().get(statementIndex);
		} else if (bodyDeclaration instanceof Initializer) {
			Initializer initializer = (Initializer) bodyDeclaration;
			Block block = initializer.getBody();
			return (ASTNode) block.statements().get(statementIndex);
		}
		return null;
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex) {
		return (ASTNode) ((TypeDeclaration)unit.types().get(typeIndex)).bodyDeclarations().get(bodyIndex);
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex) {
		return (ASTNode) unit.types().get(typeIndex);
	}
		
	protected void checkSourceRange(ASTNode node, String expectedContents, char[] source) {
		assertNotNull("The node is null", node); //$NON-NLS-1$
		assertTrue("The node(" + node.getClass() + ").getLength() == 0", node.getLength() != 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("The node.getStartPosition() == -1", node.getStartPosition() != -1); //$NON-NLS-1$
		int length = node.getLength();
		int start = node.getStartPosition();
		char[] actualContents = new char[length];
		System.arraycopy(source, start, actualContents, 0, length);
		String actualContentsString = new String(actualContents);
		assertSourceEquals("Unexpected source", expectedContents, actualContentsString);
	}
		
	protected boolean isMalformed(ASTNode node) {
		return (node.getFlags() & ASTNode.MALFORMED) != 0;
	}
	
	protected boolean isOriginal(ASTNode node) {
		return (node.getFlags() & ASTNode.ORIGINAL) != 0;
	}
}
