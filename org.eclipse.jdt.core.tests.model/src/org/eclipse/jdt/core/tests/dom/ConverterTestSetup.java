package org.eclipse.jdt.core.tests.dom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;

abstract class ConverterTestSetup extends AbstractJavaModelTests {

	protected AST ast;

	protected ConverterTestSetup(String name) {
		super(name);
	}

	protected static String getConverterJCLPath() {
		return AbstractJavaModelTests.EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMin.jar";
	}

	protected static String getConverterJCLSourcePath() {
		return AbstractJavaModelTests.EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMinsrc.zip";
	}

	protected static String getConverterJCLRootSourcePath() {
		return "";
	}

	/**
	 * Check locally for the required JCL files, jclMin.jar and jclMinsrc.zip.
	 * If not available, copy from the project resources.
	 */
	public void setupConverterJCL() throws IOException {
		String separator = java.io.File.separator;
		String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL";
		String localJCLPath =getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
		EXTERNAL_JAR_DIR_PATH = localJCLPath;
		java.io.File jclDir = new java.io.File(localJCLPath);
		java.io.File jclMin =
			new java.io.File(localJCLPath + separator + "converterJclMin.jar");
		java.io.File jclMinsrc = new java.io.File(localJCLPath + separator + "converterJclMinsrc.zip");
		if (!jclDir.exists()) {
			if (!jclDir.mkdir()) {
				//mkdir failed
				throw new IOException("Could not create the directory " + jclDir);
			} else {
				//copy the two files to the JCL directory
				java.io.File resourceJCLMin =
					new java.io.File(resourceJCLDir + separator + "converterJclMin.jar");
				copy(resourceJCLMin, jclMin);
				java.io.File resourceJCLMinsrc =
					new java.io.File(resourceJCLDir + separator + "converterJclMinsrc.zip");
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		} else {
			//check that the two files, jclMin.jar and jclMinsrc.zip are present
			//copy either file that is missing
			if (!jclMin.exists()) {
				java.io.File resourceJCLMin =
					new java.io.File(resourceJCLDir + separator + "converterJclMin.jar");
				copy(resourceJCLMin, jclMin);
			}
			if (!jclMinsrc.exists()) {
				java.io.File resourceJCLMinsrc =
					new java.io.File(resourceJCLDir + separator + "converterJclMinsrc.zip");
				copy(resourceJCLMinsrc, jclMinsrc);
			}
		}
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	public void tearDownSuite() throws Exception {
		ast = null;
		this.deleteProject("Converter");
		
		super.tearDown();
	}	

	/**
	 * Create project and set the jar placeholder.
	 */
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setupConverterJCL();
		ast = new AST();
		setUpJavaProject("Converter");
		// ensure variables are set
		if (JavaCore.getClasspathVariable("ConverterJCL_LIB") == null) {
			JavaCore.setClasspathVariables(
				new String[] {"CONVERTER_JCL_LIB", "CONVERTER_JCL_SRC", "CONVERTER_JCL_SRCROOT"},
				new Path[] {new Path(ConverterTestSetup.getConverterJCLPath()), new Path(ConverterTestSetup.getConverterJCLSourcePath()), new Path(ConverterTestSetup.getConverterJCLRootSourcePath())},
				null);
		}		
	}	

	public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
		return AST.parseCompilationUnit(unit, resolveBindings);
	}

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
		return AST.parseCompilationUnit(source, unitName, project);
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
		}
		return null;
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex) {
		return (ASTNode) ((TypeDeclaration)unit.types().get(typeIndex)).bodyDeclarations().get(bodyIndex);
	}

	protected ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex) {
		return (ASTNode) (TypeDeclaration)unit.types().get(typeIndex);
	}
		
	protected void checkSourceRange(ASTNode node, String expectedContents, char[] source) {
		assertNotNull("The node is null", node);
		assertTrue("The node(" + node.getClass() + ").getLength() == 0", node.getLength() != 0);
		assertTrue("The node.getStartPosition() == -1", node.getStartPosition() != -1);
		int length = node.getLength();
		int start = node.getStartPosition();
		char[] actualContents = new char[length];
		System.arraycopy(source, start, actualContents, 0, length);
		String actualContentsString = new String(actualContents);
		if (containsLineSeparator(actualContentsString)) {
			assertArraysEquals(actualContentsString, expectedContents);
		} else {		
			assertTrue("The two strings are not equals\n---\nactualContents = >" + actualContentsString + "<\nexpectedContents = >" + expectedContents + "<\n----", expectedContents.equals(actualContentsString));
		}
	}
	
	private boolean containsLineSeparator(String s) {
		return s.indexOf("\n") != -1 ||  s.indexOf("\r") != -1;
	}
	
	private void assertArraysEquals(String actualContents, String expectedContents) {
		String[] actualContentsArray = createArrayOfString(actualContents);
		String[] expectedContentsArray = createArrayOfString(expectedContents);
		assertTrue("Different size", actualContentsArray.length == expectedContentsArray.length);
		for (int i = 0, max = expectedContentsArray.length; i < max; i++) {
			assertEquals("Different array parts", expectedContentsArray[i], actualContentsArray[i]);
		}
	}
	
	private String[] createArrayOfString(String s) {
		StringTokenizer tokenizer = new StringTokenizer(s, "\r\n");
		ArrayList arrayList = new ArrayList();
		while (tokenizer.hasMoreElements()) {
			String nextToken = tokenizer.nextToken();
			if (nextToken.length() != 0) {
				arrayList.add(nextToken);
			}
		}
		return (String[]) arrayList.toArray(new String[arrayList.size()]);
	}
	
	protected boolean isMalformed(ASTNode node) {
		return (node.getFlags() & ASTNode.MALFORMED) != 0;
	}
}
