/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial test suite for AST API
 ******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;
import org.eclipse.jdt.core.jdom.IDOMMethod;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.jdom.IDOMType;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;

public class ASTConverterTest extends AbstractJavaModelTests {
	
	AST ast;

	public ASTConverterTest(String name) {
		super(name);
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
				new Path[] {new Path(getConverterJCLPath()), new Path(getConverterJCLSourcePath()), new Path(getConverterJCLRootSourcePath())},
				null);
		}		
	}
	
	/**
	 * Returns the root directory that contains the resources for these tests.
	 */
	protected static String getRootDirectoryName() {
		CodeSource javaCoreCodeSource = JavaCore.class.getProtectionDomain().getCodeSource();
		if (javaCoreCodeSource != null) {
			URL javaCoreUrl = javaCoreCodeSource.getLocation();
			String javaCorePath = javaCoreUrl.getFile();
			int index = javaCorePath.indexOf(JavaCore.PLUGIN_ID);
			if (index != -1) {
				String pluginsPath = javaCorePath.substring(0, index);
				return pluginsPath + "org.eclipse.jdt.core.tests" + java.io.File.separator + "Eclipse Java Tests Model";
			}
		}
		return null;
	}	
	
	/**
	 * Check locally for the required JCL files, jclMin.jar and jclMinsrc.zip.
	 * If not available, copy from the project resources.
	 */
	public void setupConverterJCL() throws IOException {
		String separator = java.io.File.separator;
		String resourceJCLDir = getRootDirectoryName() + separator + "JCL";
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

	protected static String getConverterJCLPath() {
		return EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMin.jar";
	}
	
	protected static String getConverterJCLSourcePath() {
		return EXTERNAL_JAR_DIR_PATH + File.separator + "converterJclMinsrc.zip";
	}
	
	protected static String getConverterJCLRootSourcePath() {
		return "";
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	public void tearDownSuite() throws Exception {
		ast = null;
		this.deleteProject("Converter");
		
		super.tearDown();
	}	

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterTest.class.getName());		

		Class c = ASTConverterTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) {
				suite.addTest(new ASTConverterTest(methods[i].getName()));
			}
		}
		return suite;
	}
		
	public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
		return this.ast.parseCompilationUnit(unit, resolveBindings);
	}

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
		return this.ast.parseCompilationUnit(source, unitName, project);
	}
	
	public void test0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0001", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		
		// check that we have the right tree
		CompilationUnit unit = this.ast.newCompilationUnit();
		PackageDeclaration packageDeclaration = this.ast.newPackageDeclaration();
		packageDeclaration.setName(this.ast.newSimpleName("test0001"));//$NON-NLS-1$
		unit.setPackage(packageDeclaration);
		ImportDeclaration importDeclaration = this.ast.newImportDeclaration();
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newSimpleName("java"),//$NON-NLS-1$
				this.ast.newSimpleName("util"));//$NON-NLS-1$
		importDeclaration.setName(name);
		importDeclaration.setOnDemand(true);
		unit.imports().add(importDeclaration);
		TypeDeclaration type = this.ast.newTypeDeclaration();
		type.setInterface(false);
		type.setModifiers(Modifier.PUBLIC);
		type.setName(this.ast.newSimpleName("Test"));//$NON-NLS-1$
		MethodDeclaration methodDeclaration = this.ast.newMethodDeclaration();
		methodDeclaration.setConstructor(false);
		methodDeclaration.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
		methodDeclaration.setName(this.ast.newSimpleName("main"));//$NON-NLS-1$
		methodDeclaration.setReturnType(this.ast.newPrimitiveType(PrimitiveType.VOID));
		SingleVariableDeclaration variableDeclaration = this.ast.newSingleVariableDeclaration();
		variableDeclaration.setModifiers(Modifier.NONE);
		variableDeclaration.setType(this.ast.newArrayType(this.ast.newSimpleType(this.ast.newSimpleName("String"))));//$NON-NLS-1$
		variableDeclaration.setName(this.ast.newSimpleName("args"));//$NON-NLS-1$
		methodDeclaration.parameters().add(variableDeclaration);
		org.eclipse.jdt.core.dom.Block block = this.ast.newBlock();
		MethodInvocation methodInvocation = this.ast.newMethodInvocation();
		name = 
			this.ast.newQualifiedName(
				this.ast.newSimpleName("System"),//$NON-NLS-1$
				this.ast.newSimpleName("out"));//$NON-NLS-1$
		methodInvocation.setExpression(name);
		methodInvocation.setName(this.ast.newSimpleName("println")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral literal = this.ast.newStringLiteral();
		literal.setLiteralValue("Hello");//$NON-NLS-1$
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newStringLiteral();
		literal.setLiteralValue(" world");//$NON-NLS-1$
		infixExpression.setRightOperand(literal);//$NON-NLS-1$
		methodInvocation.arguments().add(infixExpression);
		ExpressionStatement expressionStatement = this.ast.newExpressionStatement(methodInvocation);
		block.statements().add(expressionStatement);
		methodDeclaration.setBody(block);
		type.bodyDeclarations().add(methodDeclaration);
		unit.types().add(type);
		assertTrue("Both AST trees should be identical", result.subtreeMatch(new ASTMatcher(), unit));//$NON-NLS-1$
		checkSourceRange(result, new String(source), source);
	}
	
	/**
	 * Test allocation expression: new Object() ==> ClassInstanceCreation
	 */
	public void test0002() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0002", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		classInstanceCreation.setName(this.ast.newSimpleName("Object")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", classInstanceCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new Object()", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new java.lang.Object() ==> ClassInstanceCreation
	 */
	public void test0003() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0003", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newQualifiedName(
					this.ast.newSimpleName("java"), //$NON-NLS-1$
					this.ast.newSimpleName("lang")), //$NON-NLS-1$
				this.ast.newSimpleName("Object"));//$NON-NLS-1$
		classInstanceCreation.setName(name);
		assertTrue("Both AST trees should be identical", classInstanceCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new java.lang.Object()", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new java.lang.Exception("ERROR") ==> ClassInstanceCreation
	 */
	public void test0004() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0004", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newQualifiedName(
					this.ast.newSimpleName("java"), //$NON-NLS-1$
					this.ast.newSimpleName("lang")), //$NON-NLS-1$
				this.ast.newSimpleName("Exception"));//$NON-NLS-1$
		classInstanceCreation.setName(name);
		StringLiteral literal = this.ast.newStringLiteral();
		literal.setLiteralValue("ERROR");
		classInstanceCreation.arguments().add(literal);
		assertTrue("Both AST trees should be identical", classInstanceCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new java.lang.Exception(\"ERROR\")", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new java.lang.Object() {} ==> ClassInstanceCreation
	 */
	public void test0005() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0005", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newQualifiedName(
					this.ast.newSimpleName("java"), //$NON-NLS-1$
					this.ast.newSimpleName("lang")), //$NON-NLS-1$
				this.ast.newSimpleName("Object"));//$NON-NLS-1$
		classInstanceCreation.setName(name);
		AnonymousClassDeclaration anonymousClassDeclaration = this.ast.newAnonymousClassDeclaration();
		classInstanceCreation.setAnonymousClassDeclaration(anonymousClassDeclaration);
		assertTrue("Both AST trees should be identical", classInstanceCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new java.lang.Object() {}", source); //$NON-NLS-1$
	}
	
				
	/**
	 * Test allocation expression: new java.lang.Runnable() { public void run() {}} ==> ClassInstanceCreation
	 */
	public void test0006() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0006", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newQualifiedName(
					this.ast.newSimpleName("java"), //$NON-NLS-1$
					this.ast.newSimpleName("lang")), //$NON-NLS-1$
				this.ast.newSimpleName("Runnable"));//$NON-NLS-1$
		classInstanceCreation.setName(name);
		MethodDeclaration methodDeclaration = this.ast.newMethodDeclaration();
		methodDeclaration.setBody(this.ast.newBlock());
		methodDeclaration.setConstructor(false);
		methodDeclaration.setModifiers(Modifier.PUBLIC);
		methodDeclaration.setName(this.ast.newSimpleName("run"));//$NON-NLS-1$
		methodDeclaration.setReturnType(this.ast.newPrimitiveType(PrimitiveType.VOID));
		AnonymousClassDeclaration anonymousClassDeclaration = this.ast.newAnonymousClassDeclaration();
		anonymousClassDeclaration.bodyDeclarations().add(methodDeclaration);
		classInstanceCreation.setAnonymousClassDeclaration(anonymousClassDeclaration);
		assertTrue("Both AST trees should be identical", classInstanceCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new java.lang.Runnable() { public void run() {}}", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new Test().new D() ==> ClassInstanceCreation
	 */
	public void test0007() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0007", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		ASTNode expression = (ASTNode) ((MethodInvocation) expressionStatement.getExpression()).arguments().get(0);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		classInstanceCreation.setName(this.ast.newSimpleName("D")); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreationExpression = this.ast.newClassInstanceCreation();
		classInstanceCreationExpression.setName(this.ast.newSimpleName("Test")); //$NON-NLS-1$
		classInstanceCreation.setExpression(classInstanceCreationExpression);
		assertTrue("Both AST trees should be identical", classInstanceCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new Test().new D()", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new int[] {1, 2, 3, 4} ==> ArrayCreation
	 */
	public void test0008() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0008", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ArrayCreation arrayCreation = this.ast.newArrayCreation();
		arrayCreation.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 1));
		ArrayInitializer arrayInitializer = this.ast.newArrayInitializer();
		arrayInitializer.expressions().add(this.ast.newNumberLiteral("1"));//$NON-NLS-1$
		arrayInitializer.expressions().add(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		arrayInitializer.expressions().add(this.ast.newNumberLiteral("3"));//$NON-NLS-1$
		arrayInitializer.expressions().add(this.ast.newNumberLiteral("4"));//$NON-NLS-1$
		arrayCreation.setInitializer(arrayInitializer);
		assertTrue("Both AST trees should be identical", arrayCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new int[] {1, 2, 3, 4}", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new int[][] {{1}, {2}} ==> ArrayCreation
	 */
	public void test0009() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0009", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ArrayCreation arrayCreation = this.ast.newArrayCreation();
		arrayCreation.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 2));
		ArrayInitializer arrayInitializer = this.ast.newArrayInitializer();
		ArrayInitializer innerArrayInitializer = this.ast.newArrayInitializer();
		innerArrayInitializer.expressions().add(this.ast.newNumberLiteral("1"));//$NON-NLS-1$
		arrayInitializer.expressions().add(innerArrayInitializer);
		innerArrayInitializer = this.ast.newArrayInitializer();
		innerArrayInitializer.expressions().add(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		arrayInitializer.expressions().add(innerArrayInitializer);
		arrayCreation.setInitializer(arrayInitializer);
		assertTrue("Both AST trees should be identical", arrayCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new int[][] {{1}, {2}}", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new int[3] ==> ArrayCreation
	 */
	public void test0010() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0010", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ArrayCreation arrayCreation = this.ast.newArrayCreation();
		arrayCreation.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 1));
		arrayCreation.dimensions().add(this.ast.newNumberLiteral("3")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", arrayCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new int[3]", source); //$NON-NLS-1$
	}

	/**
	 * Test allocation expression: new int[3][] ==> ArrayCreation
	 */
	public void test0011() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0011", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ArrayCreation arrayCreation = this.ast.newArrayCreation();
		arrayCreation.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 2));
		arrayCreation.dimensions().add(this.ast.newNumberLiteral("3")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", arrayCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new int[3][]", source); //$NON-NLS-1$
	}
		
	/**
	 * Test allocation expression: new int[][] {{},{}} ==> ArrayCreation
	 */
	public void test0012() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0012", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		ArrayCreation arrayCreation = this.ast.newArrayCreation();
		arrayCreation.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 2));
		ArrayInitializer arrayInitializer = this.ast.newArrayInitializer();
		ArrayInitializer innerArrayInitializer = this.ast.newArrayInitializer();
		arrayInitializer.expressions().add(innerArrayInitializer);
		innerArrayInitializer = this.ast.newArrayInitializer();
		arrayInitializer.expressions().add(innerArrayInitializer);
		arrayCreation.setInitializer(arrayInitializer);
		assertTrue("Both AST trees should be identical", arrayCreation.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "new int[][] {{}, {}}", source); //$NON-NLS-1$
	}

	/**
	 * int i; ==> SingleVariableDeclaration
	 */
	public void test0013() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0013", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * int i = 0; ==> SingleVariableDeclaration
	 */
	public void test0014() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0014", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i = 0;", source); //$NON-NLS-1$
	}

	/**
	 * i = 1; ==> ExpressionStatement(Assignment)
	 */
	public void test0015() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0015", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("1")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i = 1;", source); //$NON-NLS-1$
	}

	/**
	 * i += 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0016() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0016", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i += 2;", source); //$NON-NLS-1$
	}

	/**
	 * i -= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0017() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0017", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.MINUS_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i -= 2;", source); //$NON-NLS-1$
	}
	
	/**
	 * i *= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0018() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0018", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.TIMES_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i *= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i /= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0019() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0019", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.DIVIDE_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i /= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i &= 2 ==> ExpressionStatement(Assignment)
	 */
	public void test0020() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0020", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.BIT_AND_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i &= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i |= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0021() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0021", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.BIT_OR_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i |= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i ^= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0022() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0022", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.BIT_XOR_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i ^= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i %= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0023() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0023", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.REMAINDER_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i %= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i <<= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0024() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0024", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.LEFT_SHIFT_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i <<= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i >>= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0025() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0025", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i >>= 2;", source); //$NON-NLS-1$
	}

	/**
	 * i >>>= 2; ==> ExpressionStatement(Assignment)
	 */
	public void test0026() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0026", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Assignment assignment = this.ast.newAssignment();
		assignment.setLeftHandSide(this.ast.newSimpleName("i")); //$NON-NLS-1$
		assignment.setRightHandSide(this.ast.newNumberLiteral("2")); //$NON-NLS-1$
		assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN);
		ExpressionStatement statement = this.ast.newExpressionStatement(assignment);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i >>>= 2;", source); //$NON-NLS-1$
	}

	/**
	 * --i; ==> ExpressionStatement(PrefixExpression)
	 */
	public void test0027() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0027", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		prefixExpression.setOperator(PrefixExpression.Operator.DECREMENT);//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(prefixExpression);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "--i;", source); //$NON-NLS-1$
	}

	/**
	 * --i; ==> ExpressionStatement(PrefixExpression)
	 */
	public void test0028() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0028", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(prefixExpression);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "++i;", source); //$NON-NLS-1$
	}
	
	/**
	 * i--; ==> ExpressionStatement(PostfixExpression)
	 */
	public void test0029() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0029", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.DECREMENT);//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(postfixExpression);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i--;", source); //$NON-NLS-1$
	}

	/**
	 * i++; ==> ExpressionStatement(PostfixExpression)
	 */
	public void test0030() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0030", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(postfixExpression);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "i++;", source); //$NON-NLS-1$
	}

	/**
	 * (String) o; ==> ExpressionStatement(CastExpression)
	 */
	public void test0031() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0031", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("s")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("o"));//$NON-NLS-1$
		castExpression.setType(this.ast.newSimpleType(this.ast.newSimpleName("String")));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newSimpleType(this.ast.newSimpleName("String")));//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "String s = (String) o;", source); //$NON-NLS-1$
	}						

	/**
	 * (int) d; ==> ExpressionStatement(CastExpression)
	 */
	public void test0032() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0032", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("d"));//$NON-NLS-1$
		castExpression.setType(this.ast.newPrimitiveType(PrimitiveType.INT));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i = (int) d;", source); //$NON-NLS-1$
	}	
	
	/**
	 * (float) d; ==> ExpressionStatement(CastExpression)
	 */
	public void test0033() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0033", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("f")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("d"));//$NON-NLS-1$
		castExpression.setType(this.ast.newPrimitiveType(PrimitiveType.FLOAT));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.FLOAT));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "float f = (float) d;", source); //$NON-NLS-1$
	}	

	/**
	 * (byte) d; ==> ExpressionStatement(CastExpression)
	 */
	public void test0034() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0034", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("d"));//$NON-NLS-1$
		castExpression.setType(this.ast.newPrimitiveType(PrimitiveType.BYTE));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BYTE));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "byte b = (byte) d;", source); //$NON-NLS-1$
	}	

	/**
	 * (short) d; ==> ExpressionStatement(CastExpression)
	 */
	public void test0035() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0035", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("s")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("d"));//$NON-NLS-1$
		castExpression.setType(this.ast.newPrimitiveType(PrimitiveType.SHORT));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.SHORT));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "short s = (short) d;", source); //$NON-NLS-1$
	}

	/**
	 * (long) d; ==> ExpressionStatement(CastExpression)
	 */
	public void test0036() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0036", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("l")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("d"));//$NON-NLS-1$
		castExpression.setType(this.ast.newPrimitiveType(PrimitiveType.LONG));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.LONG));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "long l = (long) d;", source); //$NON-NLS-1$
	}

	/**
	 * (char) i; ==> ExpressionStatement(CastExpression)
	 */
	public void test0037() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0037", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setExpression(this.ast.newSimpleName("i"));//$NON-NLS-1$
		castExpression.setType(this.ast.newPrimitiveType(PrimitiveType.CHAR));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(castExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.CHAR));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "char c = (char) i;", source); //$NON-NLS-1$
	}	

	/**
	 * int.class; ==> ExpressionStatement(TypeLiteral)
	 */
	public void test0038() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0038", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		TypeLiteral typeLiteral = this.ast.newTypeLiteral();
		typeLiteral.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		variableDeclarationFragment.setInitializer(typeLiteral);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newSimpleType(this.ast.newSimpleName("Class")));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(((VariableDeclarationFragment)((VariableDeclarationStatement)node).fragments().get(0)).getInitializer(), "int.class", source); //$NON-NLS-1$
	}	

	/**
	 * void.class; ==> ExpressionStatement(TypeLiteral)
	 */
	public void test0039() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0039", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		TypeLiteral typeLiteral = this.ast.newTypeLiteral();
		typeLiteral.setType(this.ast.newPrimitiveType(PrimitiveType.VOID));
		variableDeclarationFragment.setInitializer(typeLiteral);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newSimpleType(this.ast.newSimpleName("Class")));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(((VariableDeclarationFragment)((VariableDeclarationStatement)node).fragments().get(0)).getInitializer(), "void.class", source); //$NON-NLS-1$
	}	

	/**
	 * double.class; ==> ExpressionStatement(TypeLiteral)
	 */
	public void test0040() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0040", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		TypeLiteral typeLiteral = this.ast.newTypeLiteral();
		typeLiteral.setType(this.ast.newPrimitiveType(PrimitiveType.DOUBLE));
		variableDeclarationFragment.setInitializer(typeLiteral);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newSimpleType(this.ast.newSimpleName("Class")));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(((VariableDeclarationFragment)((VariableDeclarationStatement)node).fragments().get(0)).getInitializer(), "double.class", source); //$NON-NLS-1$
	}	

	/**
	 * long.class; ==> ExpressionStatement(TypeLiteral)
	 */
	public void test0041() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0041", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("c")); //$NON-NLS-1$
		TypeLiteral typeLiteral = this.ast.newTypeLiteral();
		typeLiteral.setType(this.ast.newPrimitiveType(PrimitiveType.LONG));
		variableDeclarationFragment.setInitializer(typeLiteral);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newSimpleType(this.ast.newSimpleName("Class")));//$NON-NLS-1$

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(((VariableDeclarationFragment)((VariableDeclarationStatement)node).fragments().get(0)).getInitializer(), "long.class", source); //$NON-NLS-1$
	}	
		
	/**
	 * false ==> BooleanLiteral
	 */
	public void test0042() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0042", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		BooleanLiteral literal = this.ast.newBooleanLiteral(false);
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "false", source); //$NON-NLS-1$
	}

	/**
	 * true ==> BooleanLiteral
	 */
	public void test0043() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0043", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		BooleanLiteral literal = this.ast.newBooleanLiteral(true);
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "true", source); //$NON-NLS-1$
	}

	/**
	 * null ==> NullLiteral
	 */
	public void test0044() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0044", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NullLiteral literal = this.ast.newNullLiteral();
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "null", source); //$NON-NLS-1$
	}
		
	/**
	 * CharLiteral ==> CharacterLiteral
	 */
	public void test0045() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0045", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		CharacterLiteral literal = this.ast.newCharacterLiteral();
		literal.setEscapedValue("'c'");
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "'c'", source); //$NON-NLS-1$
	}

	/**
	 * DoubleLiteral ==> NumberLiteral
	 */
	public void test0046() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0046", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("1.00001");//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "1.00001", source); //$NON-NLS-1$
	}

	/**
	 * FloatLiteral ==> NumberLiteral
	 */
	public void test0047() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0047", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("1.00001f");//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "1.00001f", source); //$NON-NLS-1$
	}

	/**
	 * IntLiteral ==> NumberLiteral
	 */
	public void test0048() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0048", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("30000");//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "30000", source); //$NON-NLS-1$
	}

	/**
	 * IntLiteralMinValue ==> NumberLiteral
	 */
	public void test0049() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0049", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("-2147483648");//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "-2147483648", source); //$NON-NLS-1$
	}

	/**
	 * LongLiteral ==> NumberLiteral
	 */
	public void test0050() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0050", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("2147483648L");//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "2147483648L", source); //$NON-NLS-1$
	}

	/**
	 * LongLiteral ==> NumberLiteral (negative value)
	 */
	public void test0051() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0051", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("2147483648L");//$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperand(literal);
		prefixExpression.setOperator(PrefixExpression.Operator.MINUS);
		assertTrue("Both AST trees should be identical", prefixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "-2147483648L", source); //$NON-NLS-1$
	}

	/**
	 * LongLiteralMinValue ==> NumberLiteral
	 */
	public void test0052() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0052", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral("-9223372036854775808L");//$NON-NLS-1$
		assertTrue("Both AST trees should be identical", literal.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "-9223372036854775808L", source); //$NON-NLS-1$
	}

	/**
	 * ExtendedStringLiteral ==> StringLiteral
	 */
	public void test0053() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0053", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		/*
		StringLiteral literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("Hello World");*/
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral literal = this.ast.newStringLiteral();
		literal.setLiteralValue("Hello");//$NON-NLS-1$
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newStringLiteral();
		literal.setLiteralValue(" World");//$NON-NLS-1$
		infixExpression.setRightOperand(literal);//$NON-NLS-1$
		
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "\"Hello\" + \" World\"", source); //$NON-NLS-1$
	}

	/**
	 * AND_AND_Expression ==> InfixExpression
	 */
	public void test0054() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0054", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b3")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b3 = b && b2;", source); //$NON-NLS-1$
	}	

	/**
	 * OR_OR_Expression ==> InfixExpression
	 */
	public void test0055() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0055", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b3")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b3 = b || b2;", source); //$NON-NLS-1$
	}	

	/**
	 * EqualExpression ==> InfixExpression
	 */
	public void test0056() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0056", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b3")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.EQUALS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b3 = b == b2;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (+) ==> InfixExpression
	 */
	public void test0057() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0057", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i + j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (-) ==> InfixExpression
	 */
	public void test0058() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0058", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.MINUS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i - j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (*) ==> InfixExpression
	 */
	public void test0059() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0059", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.TIMES);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i * j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (/) ==> InfixExpression
	 */
	public void test0060() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0060", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.DIVIDE);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i / j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (%) ==> InfixExpression
	 */
	public void test0061() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0061", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.REMAINDER);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i % j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (^) ==> InfixExpression
	 */
	public void test0062() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0062", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.XOR);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i ^ j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (&) ==> InfixExpression
	 */
	public void test0063() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0063", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.AND);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i & j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (|) ==> InfixExpression
	 */
	public void test0064() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0064", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("j")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.OR);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = i | j;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (<) ==> InfixExpression
	 */
	public void test0065() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0065", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b1")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.LESS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b2 = b < b1;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (<=) ==> InfixExpression
	 */
	public void test0066() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0066", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b1")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.LESS_EQUALS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b2 = b <= b1;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (>) ==> InfixExpression
	 */
	public void test0067() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0067", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b1")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.GREATER);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b2 = b > b1;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (>=) ==> InfixExpression
	 */
	public void test0068() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0068", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b1")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.GREATER_EQUALS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b2 = b >= b1;", source); //$NON-NLS-1$
	}	

	/**
	 * BinaryExpression (!=) ==> InfixExpression
	 */
	public void test0069() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0069", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b2")); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("b")); //$NON-NLS-1$
		infixExpression.setRightOperand(this.ast.newSimpleName("b1")); //$NON-NLS-1$
		infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
		variableDeclarationFragment.setInitializer(infixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b2 = b != b1;", source); //$NON-NLS-1$
	}	

	/**
	 * InstanceofExpression ==> InfixExpression
	 */
	public void test0070() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0070", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		InstanceofExpression instanceOfExpression = this.ast.newInstanceofExpression();
		instanceOfExpression.setLeftOperand(this.ast.newSimpleName("o"));//$NON-NLS-1$ 
		SimpleType simpleType = this.ast.newSimpleType(this.ast.newSimpleName("Integer"));//$NON-NLS-1$
		instanceOfExpression.setRightOperand(simpleType); 
		variableDeclarationFragment.setInitializer(instanceOfExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b = o instanceof Integer;", source); //$NON-NLS-1$
	}	

	/**
	 * InstanceofExpression ==> InfixExpression
	 */
	public void test0071() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0071", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		InstanceofExpression instanceOfExpression = this.ast.newInstanceofExpression();
		instanceOfExpression.setLeftOperand(this.ast.newSimpleName("o")); //$NON-NLS-1$
		QualifiedName name =
			this.ast.newQualifiedName(
				this.ast.newQualifiedName(
					this.ast.newSimpleName("java"), //$NON-NLS-1$
					this.ast.newSimpleName("lang")), //$NON-NLS-1$
				this.ast.newSimpleName("Integer")); //$NON-NLS-1$
		Type type = ast.newSimpleType(name);
		instanceOfExpression.setRightOperand(type);
		variableDeclarationFragment.setInitializer(instanceOfExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b = o instanceof java.lang.Integer;", source); //$NON-NLS-1$
	}	

	/**
	 * UnaryExpression (!) ==> PrefixExpression
	 */
	public void test0072() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0072", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b1")); //$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperator(PrefixExpression.Operator.NOT);
		prefixExpression.setOperand(this.ast.newSimpleName("b"));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(prefixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b1 = !b;", source); //$NON-NLS-1$
	}	

	/**
	 * UnaryExpression (~) ==> PrefixExpression
	 */
	public void test0073() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0073", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("n")); //$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperator(PrefixExpression.Operator.COMPLEMENT);
		prefixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(prefixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int n = ~i;", source); //$NON-NLS-1$
	}	

	/**
	 * UnaryExpression (+) ==> PrefixExpression
	 */
	public void test0074() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0074", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperator(PrefixExpression.Operator.PLUS);
		prefixExpression.setOperand(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(prefixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i = +2;", source); //$NON-NLS-1$
	}	

	/**
	 * UnaryExpression (-) ==> PrefixExpression
	 */
	public void test0075() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0075", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperator(PrefixExpression.Operator.MINUS);
		prefixExpression.setOperand(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(prefixExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));


		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i = -2;", source); //$NON-NLS-1$
	}	

	/**
	 * ConditionalExpression ==> ConditionalExpression
	 */
	public void test0076() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0076", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("b")); //$NON-NLS-1$
		ConditionalExpression conditionalExpression = this.ast.newConditionalExpression();
		InfixExpression condition = this.ast.newInfixExpression();
		condition.setLeftOperand(this.ast.newSimpleName("args")); //$NON-NLS-1$
		condition.setRightOperand(this.ast.newNullLiteral()); //$NON-NLS-1$
		condition.setOperator(InfixExpression.Operator.NOT_EQUALS);
		conditionalExpression.setExpression(condition);
		conditionalExpression.setThenExpression(this.ast.newBooleanLiteral(true));
		conditionalExpression.setElseExpression(this.ast.newBooleanLiteral(false));
		variableDeclarationFragment.setInitializer(conditionalExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.BOOLEAN));
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "boolean b = args != null ? true : false;", source); //$NON-NLS-1$
	}	

	/**
	 * ConditionalExpression ==> ConditionalExpression
	 */
	public void test0077() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0077", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		ConditionalExpression conditionalExpression = this.ast.newConditionalExpression();
		conditionalExpression.setExpression(this.ast.newBooleanLiteral(true));
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newSimpleName("args"), //$NON-NLS-1$
				this.ast.newSimpleName("length")); //$NON-NLS-1$
		conditionalExpression.setThenExpression(name);
		conditionalExpression.setElseExpression(this.ast.newNumberLiteral("0"));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(conditionalExpression);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setModifiers(Modifier.NONE);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i = true ? args.length: 0;", source); //$NON-NLS-1$
	}	

	/**
	 * MessageSend ==> SuperMethodInvocation
	 */
	public void test0078() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0078", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		SuperMethodInvocation superMethodInvocation = this.ast.newSuperMethodInvocation();
		superMethodInvocation.setName(this.ast.newSimpleName("bar")); //$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(superMethodInvocation);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "super.bar();", source); //$NON-NLS-1$
	}	

	/**
	 * MessageSend ==> SuperMethodInvocation
	 */
	public void test0079() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0079", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		SuperMethodInvocation superMethodInvocation = this.ast.newSuperMethodInvocation();
		superMethodInvocation.setName(this.ast.newSimpleName("bar")); //$NON-NLS-1$
		superMethodInvocation.arguments().add(this.ast.newNumberLiteral("4"));//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(superMethodInvocation);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "super.bar(4);", source); //$NON-NLS-1$
	}	
	
	/**
	 * MessageSend ==> MethodInvocation
	 */
	public void test0080() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0080", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		MethodInvocation methodInvocation = this.ast.newMethodInvocation();
		methodInvocation.setName(this.ast.newSimpleName("bar")); //$NON-NLS-1$
		methodInvocation.arguments().add(this.ast.newNumberLiteral("4"));//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(methodInvocation);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "bar(4);", source); //$NON-NLS-1$
	}
	
	/**
	 * MessageSend ==> MethodInvocation
	 */
	public void test0081() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0081", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		MethodInvocation methodInvocation = this.ast.newMethodInvocation();
		methodInvocation.setName(this.ast.newSimpleName("bar")); //$NON-NLS-1$
		methodInvocation.setExpression(this.ast.newThisExpression());
		methodInvocation.arguments().add(this.ast.newNumberLiteral("4"));//$NON-NLS-1$
		ExpressionStatement statement = this.ast.newExpressionStatement(methodInvocation);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "this.bar(4);", source); //$NON-NLS-1$
	}
	
	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0082() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0082", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (;;);", source); //$NON-NLS-1$
	}
	
	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0083() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0083", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$
		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		forStatement.initializers().add(variableDeclarationExpression);
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(postfixExpression);
		forStatement.setBody(this.ast.newBlock());
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i"));
		infixExpression.setOperator(InfixExpression.Operator.LESS);
		infixExpression.setRightOperand(this.ast.newNumberLiteral("10"));
		forStatement.setExpression(infixExpression);
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (int i = 0; i < 10; i++) {}", source); //$NON-NLS-1$
	}
	
	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0084() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0084", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$

		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		
		forStatement.initializers().add(variableDeclarationExpression);
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(postfixExpression);
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i"));
		infixExpression.setOperator(InfixExpression.Operator.LESS);
		infixExpression.setRightOperand(this.ast.newNumberLiteral("10"));
		forStatement.setExpression(infixExpression);
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (int i = 0; i < 10; i++);", source); //$NON-NLS-1$
	}

	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0085() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0085", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$

		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		
		forStatement.initializers().add(variableDeclarationExpression);
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(postfixExpression);
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (int i = 0;; i++);", source); //$NON-NLS-1$
	}

	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0086() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0086", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(postfixExpression);
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i"));
		infixExpression.setOperator(InfixExpression.Operator.LESS);
		infixExpression.setRightOperand(this.ast.newNumberLiteral("10"));
		forStatement.setExpression(infixExpression);
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (; i < 10; i++);", source); //$NON-NLS-1$
	}

	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0087() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0087", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(postfixExpression);
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (;;i++);", source); //$NON-NLS-1$
	}

	/**
	 * LocalDeclaration ==> VariableDeclarationStatement
	 */
	public void test0088() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0088", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$

		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		statement.setModifiers(Modifier.NONE);

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * LocalDeclaration ==> VariableDeclarationStatement
	 */
	public void test0089() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0089", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$

		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("s")); //$NON-NLS-1$

		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newQualifiedName(
					this.ast.newSimpleName("java"),//$NON-NLS-1$
					this.ast.newSimpleName("lang")//$NON-NLS-1$
				),
				this.ast.newSimpleName("String") //$NON-NLS-1$
			);
		statement.setType(this.ast.newSimpleType(name));
		statement.setModifiers(Modifier.NONE);

		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "java.lang.String s;", source); //$NON-NLS-1$
	}

	/**
	 * LocalDeclaration ==> VariableDeclarationStatement
	 */
	public void test0090() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0090", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
	
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		ArrayInitializer initializer = this.ast.newArrayInitializer();
		initializer.expressions().add(this.ast.newNumberLiteral("1"));//$NON-NLS-1$
		initializer.expressions().add(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		variableDeclarationFragment.setInitializer(initializer);
		variableDeclarationFragment.setName(this.ast.newSimpleName("tab")); //$NON-NLS-1$

		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		statement.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 1));
		statement.setModifiers(Modifier.NONE);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int[] tab = {1, 2};", source); //$NON-NLS-1$
	}
	
	/**
	 * Argument ==> VariableDeclarationStatement
	 */
	public void test0091() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0091", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		MethodDeclaration method = (MethodDeclaration)((TypeDeclaration) ((CompilationUnit) result).types().get(0)).bodyDeclarations().get(0);
		SingleVariableDeclaration node = (SingleVariableDeclaration) method.parameters().get(0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		SingleVariableDeclaration variableDeclaration = this.ast.newSingleVariableDeclaration();
		variableDeclaration.setModifiers(Modifier.NONE);
		variableDeclaration.setType(this.ast.newSimpleType(this.ast.newSimpleName("String")));//$NON-NLS-1$
		variableDeclaration.setName(this.ast.newSimpleName("s")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", variableDeclaration.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "String s", source); //$NON-NLS-1$
	}

	/**
	 * Argument ==> VariableDeclarationStatement
	 */
	public void test0092() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0092", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		MethodDeclaration method = (MethodDeclaration)((TypeDeclaration) ((CompilationUnit) result).types().get(0)).bodyDeclarations().get(0);
		SingleVariableDeclaration node = (SingleVariableDeclaration) method.parameters().get(0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		SingleVariableDeclaration variableDeclaration = this.ast.newSingleVariableDeclaration();
		variableDeclaration.setModifiers(Modifier.FINAL);
		variableDeclaration.setType(this.ast.newSimpleType(this.ast.newSimpleName("String")));//$NON-NLS-1$
		variableDeclaration.setName(this.ast.newSimpleName("s")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", variableDeclaration.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "final String s", source); //$NON-NLS-1$
	}

	/**
	 * Break ==> BreakStatement
	 */
	public void test0093() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0093", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		ForStatement forStatement = (ForStatement) node;
		BreakStatement statement = (BreakStatement) ((Block) forStatement.getBody()).statements().get(0);
		assertNotNull("Expression should not be null", statement); //$NON-NLS-1$
		BreakStatement breakStatement = this.ast.newBreakStatement();
		assertTrue("Both AST trees should be identical", breakStatement.subtreeMatch(new ASTMatcher(), statement));		//$NON-NLS-1$
		checkSourceRange(statement, "break", source); //$NON-NLS-1$
	}

	/**
	 * Continue ==> ContinueStatement
	 */
	public void test0094() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0094", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		ForStatement forStatement = (ForStatement) node;
		ContinueStatement statement = (ContinueStatement) ((Block) forStatement.getBody()).statements().get(0);
		assertNotNull("Expression should not be null", statement); //$NON-NLS-1$
		ContinueStatement continueStatement = this.ast.newContinueStatement();
		assertTrue("Both AST trees should be identical", continueStatement.subtreeMatch(new ASTMatcher(), statement));		//$NON-NLS-1$
		checkSourceRange(statement, "continue", source); //$NON-NLS-1$
	}
	
	/**
	 * Continue with Label ==> ContinueStatement
	 */
	public void test0095() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0095", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		LabeledStatement labeledStatement = (LabeledStatement) getASTNode((CompilationUnit) result, 0, 0, 0);
		ForStatement forStatement = (ForStatement) labeledStatement.getBody();
		ContinueStatement statement = (ContinueStatement) ((Block) forStatement.getBody()).statements().get(0);
		assertNotNull("Expression should not be null", statement); //$NON-NLS-1$
		ContinueStatement continueStatement = this.ast.newContinueStatement();
		continueStatement.setLabel(this.ast.newSimpleName("label"));
		assertTrue("Both AST trees should be identical", continueStatement.subtreeMatch(new ASTMatcher(), statement));		//$NON-NLS-1$
		checkSourceRange(statement, "continue label", source); //$NON-NLS-1$
	}
	
	/**
	 * Break + label  ==> BreakStatement
	 */
	public void test0096() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0096", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		LabeledStatement labeledStatement = (LabeledStatement) getASTNode((CompilationUnit) result, 0, 0, 0);
		ForStatement forStatement = (ForStatement) labeledStatement.getBody();
		BreakStatement statement = (BreakStatement) ((Block) forStatement.getBody()).statements().get(0);
		assertNotNull("Expression should not be null", statement); //$NON-NLS-1$
		BreakStatement breakStatement = this.ast.newBreakStatement();
		breakStatement.setLabel(this.ast.newSimpleName("label")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", breakStatement.subtreeMatch(new ASTMatcher(), statement));		//$NON-NLS-1$
		checkSourceRange(statement, "break label", source); //$NON-NLS-1$
	}

	/**
	 * SwitchStatement ==> SwitchStatement
	 */
	public void test0097() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0097", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		SwitchStatement switchStatement = this.ast.newSwitchStatement();
		switchStatement.setExpression(this.ast.newSimpleName("i"));//$NON-NLS-1$
		SwitchCase _case = this.ast.newSwitchCase();
		_case.setExpression(this.ast.newNumberLiteral("1"));//$NON-NLS-1$
		switchStatement.statements().add(_case);
		switchStatement.statements().add(this.ast.newBreakStatement());
		_case = this.ast.newSwitchCase();
		_case.setExpression(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		switchStatement.statements().add(_case);
		MethodInvocation methodInvocation = this.ast.newMethodInvocation();
		QualifiedName name = 
			this.ast.newQualifiedName(
				this.ast.newSimpleName("System"),//$NON-NLS-1$
				this.ast.newSimpleName("out"));//$NON-NLS-1$
		methodInvocation.setExpression(name);
		methodInvocation.setName(this.ast.newSimpleName("println")); //$NON-NLS-1$
		methodInvocation.arguments().add(this.ast.newNumberLiteral("2"));//$NON-NLS-1$
		ExpressionStatement expressionStatement = this.ast.newExpressionStatement(methodInvocation);
		switchStatement.statements().add(expressionStatement);
		switchStatement.statements().add(this.ast.newBreakStatement());
		_case = this.ast.newSwitchCase();
		_case.setExpression(null);
		switchStatement.statements().add(_case);
		methodInvocation = this.ast.newMethodInvocation();
		name = 
			this.ast.newQualifiedName(
				this.ast.newSimpleName("System"),//$NON-NLS-1$
				this.ast.newSimpleName("out"));//$NON-NLS-1$
		methodInvocation.setExpression(name);
		methodInvocation.setName(this.ast.newSimpleName("println")); //$NON-NLS-1$
		StringLiteral literal = this.ast.newStringLiteral();
		literal.setLiteralValue("default");	//$NON-NLS-1$
		methodInvocation.arguments().add(literal);
		expressionStatement = this.ast.newExpressionStatement(methodInvocation);
		switchStatement.statements().add(expressionStatement);
		assertTrue("Both AST trees should be identical", switchStatement.subtreeMatch(new ASTMatcher(), node));	//$NON-NLS-1$
		String expectedSource = "switch(i) {\r\n" +//$NON-NLS-1$
			 "			case 1: \r\n" +//$NON-NLS-1$
			 "              break;\r\n" +//$NON-NLS-1$
			 "			case 2:\r\n" +//$NON-NLS-1$
			 "				System.out.println(2);\r\n" +//$NON-NLS-1$
			 "              break;\r\n" +//$NON-NLS-1$
			 "          default:\r\n" +//$NON-NLS-1$
			 "				System.out.println(\"default\");\r\n" +//$NON-NLS-1$
			 "		}";
		checkSourceRange(node, expectedSource, source);
		SwitchStatement switchStatement2 = (SwitchStatement) node;
		List statements = switchStatement2.statements();
		assertEquals("wrong size", 7, statements.size());
		Statement stmt = (Statement) statements.get(5);
		assertTrue("Not a case statement", stmt instanceof SwitchCase);
		SwitchCase switchCase = (SwitchCase) stmt;
		assertTrue("Not the default case", switchCase.isDefault());
	}

	/**
	 * EmptyStatement ==> EmptyStatement
	 */
	public void test0098() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0098", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		EmptyStatement emptyStatement = this.ast.newEmptyStatement();
		assertTrue("Both AST trees should be identical", emptyStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, ";", source); //$NON-NLS-1$
	}

	/**
	 * DoStatement ==> DoStatement
	 */
	public void test0099() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0099", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		DoStatement doStatement = this.ast.newDoStatement();
		Block block = this.ast.newBlock();
		block.statements().add(this.ast.newEmptyStatement());
		doStatement.setBody(block);
		doStatement.setExpression(this.ast.newBooleanLiteral(true));
		assertTrue("Both AST trees should be identical", doStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "do {;\r\n" +//$NON-NLS-1$
			 "		} while(true);";//$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * WhileStatement ==> WhileStatement
	 */
	public void test0100() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0100", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		WhileStatement whileStatement = this.ast.newWhileStatement();
		whileStatement.setExpression(this.ast.newBooleanLiteral(true));
		assertTrue("Both AST trees should be identical", whileStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "while(true)", source);//$NON-NLS-1$
	}

	/**
	 * WhileStatement ==> WhileStatement
	 */
	public void test0101() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0101", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		WhileStatement whileStatement = this.ast.newWhileStatement();
		whileStatement.setExpression(this.ast.newBooleanLiteral(true));
		whileStatement.setBody(this.ast.newBlock());
		assertTrue("Both AST trees should be identical", whileStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "while(true) {}", source);//$NON-NLS-1$
	}
	
	/**
	 * ExtendedStringLiteral ==> StringLiteral
	 */
	public void test0102() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0102", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("Hello");
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue(" World");
		infixExpression.setRightOperand(literal);		
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("!");
		infixExpression.extendedOperands().add(literal);
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "\"Hello\" + \" World\" + \"!\"", source);//$NON-NLS-1$
	}
	
	/**
	 * ExtendedStringLiteral ==> StringLiteral
	 */
	public void test0103() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0103", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("Hello");
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue(" World");
		infixExpression.setRightOperand(literal);		
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("!");
		infixExpression.extendedOperands().add(literal);
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("!");
		infixExpression.extendedOperands().add(literal);
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "\"Hello\" + \" World\" + \"!\" + \"!\"", source);//$NON-NLS-1$
	}

	/**
	 * ExtendedStringLiteral ==> StringLiteral
	 */
	public void test0104() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0104", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("Hello");
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue(" World");
		infixExpression.setRightOperand(literal);		
		literal = this.ast.newStringLiteral();//$NON-NLS-1$
		literal.setLiteralValue("!");
		infixExpression.extendedOperands().add(literal);
		NumberLiteral numberLiteral = this.ast.newNumberLiteral();//$NON-NLS-1$
		numberLiteral.setToken("4");
		infixExpression.extendedOperands().add(numberLiteral);
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "\"Hello\" + \" World\" + \"!\" + 4", source);//$NON-NLS-1$
	}

	/**
	 * NumberLiteral ==> InfixExpression
	 */
	public void test0105() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0105", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		NumberLiteral literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("5");
		infixExpression.setRightOperand(literal);		
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("6");
		infixExpression.extendedOperands().add(literal);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression.extendedOperands().add(literal);
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "4 + 5 + 6 + 4", source);//$NON-NLS-1$
	}
	
	/**
	 * NumberLiteral ==> InfixExpression
	 */
	public void test0106() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0106", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.MINUS);
		NumberLiteral literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("5");
		infixExpression.setRightOperand(literal);		
		
		InfixExpression infixExpression2 = this.ast.newInfixExpression();
		infixExpression2.setOperator(InfixExpression.Operator.PLUS);
		infixExpression2.setLeftOperand(infixExpression);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("6");
		infixExpression2.setRightOperand(literal);		
		
		InfixExpression infixExpression3 = this.ast.newInfixExpression();
		infixExpression3.setOperator(InfixExpression.Operator.PLUS);
		infixExpression3.setLeftOperand(infixExpression2);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression3.setRightOperand(literal);		
		
		assertTrue("Both AST trees should be identical", infixExpression3.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "4 - 5 + 6 + 4", source);//$NON-NLS-1$
	}

	/**
	 * NumberLiteral ==> InfixExpression
	 */
	public void test0107() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0107", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.MINUS);
		NumberLiteral literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression.setLeftOperand(literal);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("5");
		infixExpression.setRightOperand(literal);		
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("6");
		infixExpression.extendedOperands().add(literal);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression.extendedOperands().add(literal);
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "4 - 5 - 6 - 4", source);//$NON-NLS-1$
	}

	/**
	 * NumberLiteral ==> InfixExpression
	 */
	public void test0108() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0108", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		StringLiteral stringLiteral = this.ast.newStringLiteral();//$NON-NLS-1$
		stringLiteral.setLiteralValue("4");
		infixExpression.setLeftOperand(stringLiteral);
		NumberLiteral literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("5");
		infixExpression.setRightOperand(literal);		
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("6");
		infixExpression.extendedOperands().add(literal);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression.extendedOperands().add(literal);
		assertTrue("Both AST trees should be identical", infixExpression.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "\"4\" + 5 + 6 + 4", source);//$NON-NLS-1$
	}
	
	/**
	 * NumberLiteral ==> InfixExpression
	 */
	public void test0109() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0109", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setOperator(InfixExpression.Operator.MINUS);
		StringLiteral stringLiteral = this.ast.newStringLiteral();//$NON-NLS-1$
		stringLiteral.setLiteralValue("4");
		infixExpression.setLeftOperand(stringLiteral);
		NumberLiteral literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("5");
		infixExpression.setRightOperand(literal);		
		
		InfixExpression infixExpression2 = this.ast.newInfixExpression();
		infixExpression2.setOperator(InfixExpression.Operator.PLUS);
		infixExpression2.setLeftOperand(infixExpression);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("6");
		infixExpression2.setRightOperand(literal);		
		
		InfixExpression infixExpression3 = this.ast.newInfixExpression();
		infixExpression3.setOperator(InfixExpression.Operator.PLUS);
		infixExpression3.setLeftOperand(infixExpression2);
		literal = this.ast.newNumberLiteral();//$NON-NLS-1$
		literal.setToken("4");
		infixExpression3.setRightOperand(literal);		
		
		assertTrue("Both AST trees should be identical", infixExpression3.subtreeMatch(new ASTMatcher(), expression));		//$NON-NLS-1$
		checkSourceRange(expression, "\"4\" - 5 + 6 + 4", source);//$NON-NLS-1$
	}

	/**
	 * ReturnStatement ==> ReturnStatement
	 */
	public void test0110() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0110", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ReturnStatement returnStatement = this.ast.newReturnStatement();
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("2");//$NON-NLS-1$
		returnStatement.setExpression(literal);
		assertTrue("Both AST trees should be identical", returnStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "return 2;", source);//$NON-NLS-1$
	}

	/**
	 * ReturnStatement ==> ReturnStatement
	 */
	public void test0111() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0111", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ReturnStatement returnStatement = this.ast.newReturnStatement();
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("2");//$NON-NLS-1$
		returnStatement.setExpression(literal);
		assertTrue("Both AST trees should be identical", returnStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "return 2\\u003B", source);//$NON-NLS-1$
	}
	
	/**
	 * SynchronizedStatement ==> SynchronizedStatement
	 */
	public void test0112() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0112", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		SynchronizedStatement synchronizedStatement = this.ast.newSynchronizedStatement();
		synchronizedStatement.setExpression(this.ast.newThisExpression());
		synchronizedStatement.setBody(this.ast.newBlock());
		assertTrue("Both AST trees should be identical", synchronizedStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "synchronized(this) {\r\n" +//$NON-NLS-1$
			 "		}"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * TryStatement ==> TryStatement
	 */
	public void test0113() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0113", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		TryStatement tryStatement = this.ast.newTryStatement();
		tryStatement.setBody(this.ast.newBlock());
		tryStatement.setFinally(this.ast.newBlock());
		CatchClause catchBlock = this.ast.newCatchClause();
		catchBlock.setBody(this.ast.newBlock());
		SingleVariableDeclaration exceptionVariable = this.ast.newSingleVariableDeclaration();
		exceptionVariable.setModifiers(Modifier.NONE);
		exceptionVariable.setName(this.ast.newSimpleName("e"));//$NON-NLS-1$
		exceptionVariable.setType(this.ast.newSimpleType(this.ast.newSimpleName("Exception")));//$NON-NLS-1$
		catchBlock.setException(exceptionVariable);
		tryStatement.catchClauses().add(catchBlock);
		assertTrue("Both AST trees should be identical", tryStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "try {\r\n" +//$NON-NLS-1$
			 "		} catch(Exception e) {\r\n" +//$NON-NLS-1$
			 "		} finally {\r\n" +//$NON-NLS-1$
			 "		}"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * TryStatement ==> TryStatement
	 */
	public void test0114() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0114", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		TryStatement tryStatement = this.ast.newTryStatement();
		tryStatement.setBody(this.ast.newBlock());
		CatchClause catchBlock = this.ast.newCatchClause();
		catchBlock.setBody(this.ast.newBlock());
		SingleVariableDeclaration exceptionVariable = this.ast.newSingleVariableDeclaration();
		exceptionVariable.setModifiers(Modifier.NONE);
		exceptionVariable.setName(this.ast.newSimpleName("e"));//$NON-NLS-1$
		exceptionVariable.setType(this.ast.newSimpleType(this.ast.newSimpleName("Exception")));//$NON-NLS-1$
		catchBlock.setException(exceptionVariable);
		tryStatement.catchClauses().add(catchBlock);
		assertTrue("Both AST trees should be identical", tryStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "try {\r\n" +//$NON-NLS-1$
			 "		} catch(Exception e) {\r\n" +//$NON-NLS-1$
			 "		}"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * TryStatement ==> TryStatement
	 */
	public void test0115() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0115", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		TryStatement tryStatement = this.ast.newTryStatement();
		Block block = this.ast.newBlock();
		ReturnStatement returnStatement = this.ast.newReturnStatement();
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("2");//$NON-NLS-1$
		returnStatement.setExpression(literal);
		block.statements().add(returnStatement);
		tryStatement.setBody(block);
		CatchClause catchBlock = this.ast.newCatchClause();
		catchBlock.setBody(this.ast.newBlock());
		SingleVariableDeclaration exceptionVariable = this.ast.newSingleVariableDeclaration();
		exceptionVariable.setModifiers(Modifier.NONE);
		exceptionVariable.setName(this.ast.newSimpleName("e"));//$NON-NLS-1$
		exceptionVariable.setType(this.ast.newSimpleType(this.ast.newSimpleName("Exception")));//$NON-NLS-1$
		catchBlock.setException(exceptionVariable);
		tryStatement.catchClauses().add(catchBlock);
		assertTrue("Both AST trees should be identical", tryStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "try {\r\n" +//$NON-NLS-1$
			 "			return 2;\r\n" +//$NON-NLS-1$
			 "		} catch(Exception e) {\r\n" +//$NON-NLS-1$
			 "		}"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}
		
	/**
	 * ThrowStatement ==> ThrowStatement
	 */
	public void test0116() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0116", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ThrowStatement throwStatement = this.ast.newThrowStatement();
		throwStatement.setExpression(this.ast.newSimpleName("e")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", throwStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "throw e   \\u003B", source);//$NON-NLS-1$
	}

	/**
	 * ThrowStatement ==> ThrowStatement
	 */
	public void test0117() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0117", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ThrowStatement throwStatement = this.ast.newThrowStatement();
		throwStatement.setExpression(this.ast.newSimpleName("e")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", throwStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "throw e /* comment in the middle of a throw */  \\u003B", source);//$NON-NLS-1$
	}

	/**
	 * ThrowStatement ==> ThrowStatement
	 */
	public void test0118() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0118", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ThrowStatement throwStatement = this.ast.newThrowStatement();
		throwStatement.setExpression(this.ast.newSimpleName("e")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", throwStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "throw e /* comment in the middle of a throw */  \\u003B", source);//$NON-NLS-1$
	}

	/**
	 * IfStatement ==> IfStatement
	 */
	public void test0119() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0119", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setExpression(this.ast.newBooleanLiteral(true));
		ifStatement.setThenStatement(this.ast.newEmptyStatement());
		assertTrue("Both AST trees should be identical", ifStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "if (true)\\u003B", source);//$NON-NLS-1$
	}

	/**
	 * IfStatement ==> IfStatement
	 */
	public void test0120() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0120", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setExpression(this.ast.newBooleanLiteral(true));
		ifStatement.setThenStatement(this.ast.newEmptyStatement());
		ifStatement.setElseStatement(this.ast.newEmptyStatement());
		assertTrue("Both AST trees should be identical", ifStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "if (true)\\u003B\r\n" +//$NON-NLS-1$
			 "\t\telse ;"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * IfStatement ==> IfStatement
	 */
	public void test0121() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0121", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setExpression(this.ast.newBooleanLiteral(true));
		ifStatement.setThenStatement(this.ast.newBlock());
		ifStatement.setElseStatement(this.ast.newEmptyStatement());
		assertTrue("Both AST trees should be identical", ifStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "if (true) {}\r\n" +//$NON-NLS-1$
			 "		else ;"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * IfStatement ==> IfStatement
	 */
	public void test0122() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0122", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setExpression(this.ast.newBooleanLiteral(true));
		ReturnStatement returnStatement = this.ast.newReturnStatement();
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("2");//$NON-NLS-1$
		returnStatement.setExpression(literal);
		ifStatement.setThenStatement(returnStatement);
		assertTrue("Both AST trees should be identical", ifStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "if (true) return 2\\u003B", source);//$NON-NLS-1$
	}

	/**
	 * IfStatement ==> IfStatement
	 */
	public void test0123() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0123", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setExpression(this.ast.newBooleanLiteral(true));
		ReturnStatement returnStatement = this.ast.newReturnStatement();
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("2");//$NON-NLS-1$
		returnStatement.setExpression(literal);
		ifStatement.setThenStatement(returnStatement);
		returnStatement = this.ast.newReturnStatement();
		literal = this.ast.newNumberLiteral();
		literal.setToken("3");//$NON-NLS-1$
		returnStatement.setExpression(literal);		
		ifStatement.setElseStatement(returnStatement);
		assertTrue("Both AST trees should be identical", ifStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		String expectedSource = "if (true) return 2;\r\n" +//$NON-NLS-1$
			 "		else return 3;"; //$NON-NLS-1$
		checkSourceRange(node, expectedSource, source);
	}

	/**
	 * Multiple local declaration => VariabledeclarationStatement
	 */
	public void test0124() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0124", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("x"));//$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("10");//$NON-NLS-1$
		fragment.setInitializer(literal);
		fragment.setExtraDimensions(0);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("z"));//$NON-NLS-1$
		fragment.setInitializer(this.ast.newNullLiteral());
		fragment.setExtraDimensions(1);
		statement.fragments().add(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("i"));//$NON-NLS-1$
		fragment.setExtraDimensions(0);
		statement.fragments().add(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("j"));//$NON-NLS-1$
		fragment.setExtraDimensions(2);
		statement.fragments().add(fragment);
		statement.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		statement.setModifiers(Modifier.NONE);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		VariableDeclarationFragment[] fragments = (VariableDeclarationFragment[])((VariableDeclarationStatement) node).fragments().toArray(new VariableDeclarationFragment[4]);
		assertTrue("fragments.length != 4", fragments.length == 4);
		checkSourceRange(fragments[0], "x= 10", source);//$NON-NLS-1$
		checkSourceRange(fragments[1], "z[] = null", source);//$NON-NLS-1$
		checkSourceRange(fragments[2], "i", source);//$NON-NLS-1$
		checkSourceRange(fragments[3], "j[][]", source);//$NON-NLS-1$
		checkSourceRange(node, "int x= 10, z[] = null, i, j[][];", source);//$NON-NLS-1$
	}

	/**
	 * Multiple local declaration => VariabledeclarationStatement
	 */
	public void test0125() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0125", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("x"));//$NON-NLS-1$
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("10");//$NON-NLS-1$
		fragment.setInitializer(literal);
		fragment.setExtraDimensions(0);
		VariableDeclarationStatement statement = this.ast.newVariableDeclarationStatement(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("z"));//$NON-NLS-1$
		fragment.setInitializer(this.ast.newNullLiteral());
		fragment.setExtraDimensions(1);
		statement.fragments().add(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("i"));//$NON-NLS-1$
		fragment.setExtraDimensions(0);
		statement.fragments().add(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("j"));//$NON-NLS-1$
		fragment.setExtraDimensions(2);
		statement.fragments().add(fragment);
		statement.setType(this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT), 1));
		statement.setModifiers(Modifier.NONE);
		assertTrue("Both AST trees should be identical", statement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int[] x= 10, z[] = null, i, j[][];", source);
		VariableDeclarationFragment[] fragments = (VariableDeclarationFragment[])((VariableDeclarationStatement) node).fragments().toArray(new VariableDeclarationFragment[4]);
		assertTrue("fragments.length != 4", fragments.length == 4);
		checkSourceRange(fragments[0], "x= 10", source);//$NON-NLS-1$
		checkSourceRange(fragments[1], "z[] = null", source);//$NON-NLS-1$
		checkSourceRange(fragments[2], "i", source);//$NON-NLS-1$
		checkSourceRange(fragments[3], "j[][]", source);//$NON-NLS-1$
	}

	/**
	 * ForStatement
	 */
	public void test0126() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0126", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("tab")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNullLiteral());//$NON-NLS-1$
		variableDeclarationFragment.setExtraDimensions(1);
		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newArrayType(this.ast.newSimpleType(this.ast.newSimpleName("String")), 1));//$NON-NLS-1$
		forStatement.initializers().add(variableDeclarationExpression);
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);
		forStatement.updaters().add(prefixExpression);
		forStatement.setBody(this.ast.newBlock());
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (String[] tab[] = null;; ++i) {}", source); //$NON-NLS-1$
		checkSourceRange((ASTNode) ((ForStatement) node).updaters().get(0), "++i", source); //$NON-NLS-1$
		checkSourceRange((ASTNode) ((ForStatement) node).initializers().get(0), "String[] tab[] = null", source); //$NON-NLS-1$
	}

	/**
	 * ForStatement
	 */
	public void test0127() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0127", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("tab")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNullLiteral());//$NON-NLS-1$
		variableDeclarationFragment.setExtraDimensions(1);
		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newSimpleType(this.ast.newSimpleName("String")));//$NON-NLS-1$
		forStatement.initializers().add(variableDeclarationExpression);
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		prefixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);
		forStatement.updaters().add(prefixExpression);
		forStatement.setBody(this.ast.newBlock());
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (String tab[] = null;; ++i) {}", source); //$NON-NLS-1$
		checkSourceRange((ASTNode) ((ForStatement) node).updaters().get(0), "++i", source); //$NON-NLS-1$
		checkSourceRange((ASTNode) ((ForStatement) node).initializers().get(0), "String tab[] = null", source); //$NON-NLS-1$
	}

	/**
	 * ForStatement
	 */
	public void test0128() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0128", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		variableDeclarationFragment.setName(this.ast.newSimpleName("tab")); //$NON-NLS-1$
		variableDeclarationFragment.setInitializer(this.ast.newNullLiteral());//$NON-NLS-1$
		variableDeclarationFragment.setExtraDimensions(1);
		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newSimpleType(this.ast.newSimpleName("String")));//$NON-NLS-1$
		forStatement.initializers().add(variableDeclarationExpression);
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		postfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(postfixExpression);
		forStatement.setBody(this.ast.newBlock());
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (String tab[] = null;; i++/**/) {}", source); //$NON-NLS-1$
		checkSourceRange((ASTNode) ((ForStatement) node).updaters().get(0), "i++", source); //$NON-NLS-1$
		checkSourceRange((ASTNode) ((ForStatement) node).initializers().get(0), "String tab[] = null", source); //$NON-NLS-1$
	}

	/**
	 * FieldDeclaration
	 */
	public void test0129() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0129", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		VariableDeclarationFragment frag = (VariableDeclarationFragment) ((FieldDeclaration) node).fragments().get(0);
		assertTrue("Not a declaration", frag.getName().isDeclaration());
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("i"));
		fragment.setExtraDimensions(0);
		FieldDeclaration fieldDeclaration = this.ast.newFieldDeclaration(fragment);
		fieldDeclaration.setModifiers(Modifier.NONE);
		fieldDeclaration.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		assertTrue("Both AST trees should be identical", fieldDeclaration.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * FieldDeclaration
	 */
	public void test0130() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0130", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("x"));
		NumberLiteral literal = this.ast.newNumberLiteral();
		literal.setToken("10");
		fragment.setInitializer(literal);
		fragment.setExtraDimensions(0);
		FieldDeclaration fieldDeclaration = this.ast.newFieldDeclaration(fragment);
		fieldDeclaration.setModifiers(Modifier.PUBLIC);
		fieldDeclaration.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("y"));//$NON-NLS-1$
		fragment.setExtraDimensions(1);
		fragment.setInitializer(this.ast.newNullLiteral());
		fieldDeclaration.fragments().add(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("i"));//$NON-NLS-1$
		fragment.setExtraDimensions(0);
		fieldDeclaration.fragments().add(fragment);
		fragment = this.ast.newVariableDeclarationFragment();
		fragment.setName(this.ast.newSimpleName("j"));//$NON-NLS-1$
		fragment.setExtraDimensions(2);
		fieldDeclaration.fragments().add(fragment);
		assertTrue("Both AST trees should be identical", fieldDeclaration.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "public int x= 10, y[] = null, i, j[][];", source); //$NON-NLS-1$
		VariableDeclarationFragment[] fragments = (VariableDeclarationFragment[])((FieldDeclaration) node).fragments().toArray(new VariableDeclarationFragment[4]);
		assertTrue("fragments.length != 4", fragments.length == 4);
		checkSourceRange(fragments[0], "x= 10", source);//$NON-NLS-1$
		checkSourceRange(fragments[1], "y[] = null", source);//$NON-NLS-1$
		checkSourceRange(fragments[2], "i", source);//$NON-NLS-1$
		checkSourceRange(fragments[3], "j[][]", source);//$NON-NLS-1$
	}

	/**
	 * Argument with final modifier
	 */
	public void test0131() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0131", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		assertTrue("Not a declaration", ((MethodDeclaration) node).getName().isDeclaration());
		List parameters = ((MethodDeclaration) node).parameters();
		assertTrue("Parameters.length != 1", parameters.size() == 1);		//$NON-NLS-1$
		SingleVariableDeclaration arg = (SingleVariableDeclaration) ((MethodDeclaration) node).parameters().get(0);
		SingleVariableDeclaration singleVariableDeclaration = this.ast.newSingleVariableDeclaration();
		singleVariableDeclaration.setModifiers(Modifier.FINAL);
		singleVariableDeclaration.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		singleVariableDeclaration.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		assertTrue("Both AST trees should be identical", singleVariableDeclaration.subtreeMatch(new ASTMatcher(), arg));		//$NON-NLS-1$
		checkSourceRange(node, "void foo(final int i) {}", source); //$NON-NLS-1$
		checkSourceRange(arg, "final int i", source);
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test0132() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0132", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		Javadoc actualJavadoc = ((MethodDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n  */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		checkSourceRange(node, "/** JavaDoc Comment\r\n  */\r\n  void foo(final int i) {}", source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n  */", source);
	}
	
	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test0133() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0133", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		Javadoc actualJavadoc = ((MethodDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "void foo(final int i) {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test0134() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0134", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		Javadoc actualJavadoc = ((MethodDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "void foo(final int i) {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for FieldDeclaration
	 */
	public void test0135() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0135", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		Javadoc actualJavadoc = ((FieldDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n  */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		checkSourceRange(node, "/** JavaDoc Comment\r\n  */\r\n  int i;", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for FieldDeclaration
	 */
	public void test0136() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0136", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		Javadoc actualJavadoc = ((FieldDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for FieldDeclaration
	 */
	public void test0137() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0137", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		Javadoc actualJavadoc = ((FieldDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for TypeDeclaration
	 */
	public void test0138() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0138", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		String expectedContents = "public class Test {\r\n" +//$NON-NLS-1$
			"  int i;\r\n"  +//$NON-NLS-1$
			"}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for TypeDeclaration
	 */
	public void test0139() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0139", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		String expectedContents = "public class Test {\r\n" +//$NON-NLS-1$
			"  int i;\r\n"  +//$NON-NLS-1$
			"}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for TypeDeclaration
	 */
	public void test0140() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0140", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 " */\r\n" + //$NON-NLS-1$
			"public class Test {\r\n" +//$NON-NLS-1$
			"  int i;\r\n"  +//$NON-NLS-1$
			"}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n */", source);
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0141() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0141", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n	 */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 "	 */\r\n" + //$NON-NLS-1$
			 "  class B {}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n	 */", source);
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0142() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0142", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "class B {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0143() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0143", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "public static class B {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0144() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0144", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "public static class B {}", source); //$NON-NLS-1$
	}

	/**
	 * Checking initializers
	 */
	public void test0145() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0145", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		checkSourceRange(node, "{}", source); //$NON-NLS-1$
	}

	/**
	 * Checking initializers
	 */
	public void test0146() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0146", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		checkSourceRange(node, "static {}", source); //$NON-NLS-1$
	}

	/**
	 * Checking initializers
	 */
	public void test0147() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0147", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Javadoc actualJavadoc = ((Initializer) node).getJavadoc();
		assertNotNull("Javadoc comment should no be null", actualJavadoc); //$NON-NLS-1$
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n	 */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 "	 */\r\n" + //$NON-NLS-1$
			 "  static {}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n	 */", source);
		
	}

	/**
	 * Checking initializers
	 */
	public void test0148() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0148", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Javadoc actualJavadoc = ((Initializer) node).getJavadoc();
		assertNotNull("Javadoc comment should not be null", actualJavadoc); //$NON-NLS-1$
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n	 */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 "	 */\r\n" + //$NON-NLS-1$
			 "  {}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n	 */", source);
		
	}

	/**
	 * Checking initializers
	 */
	public void test0149() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0149", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Javadoc actualJavadoc = ((Initializer) node).getJavadoc();
		assertNull("Javadoc comment should be null", actualJavadoc); //$NON-NLS-1$
		checkSourceRange(node, "{}", source); //$NON-NLS-1$
	}

	/**
	 * Checking syntax error
	 */
	public void test0150() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0150", "Test.java");
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		assertTrue("The compilation unit is malformed", !isMalformed(unit));
		assertTrue("The package declaration is malformed", !isMalformed(unit.getPackage()));
		List imports = unit.imports();
		assertTrue("The imports list size is not one", imports.size() == 1);
		assertTrue("The first import is not malformed", isMalformed((ASTNode) imports.get(0)));
	}

	/**
	 * Checking syntax error
	 */
	public void test0151() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0151", "Test.java");
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The compilation unit is malformed", !isMalformed(result));
	}

	/**
	 * Checking syntax error
	 */
	public void test0152() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0152", "Test.java");
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The compilation unit is malformed", !isMalformed(result));
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The type is malformed", !isMalformed(node));
		node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The field is not malformed", isMalformed(node));
	}

	/**
	 * Checking syntax error
	 */
	public void test0153() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0153", "Test.java");
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The compilation unit is malformed", !isMalformed(result));
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The method is not malformed", isMalformed(node));
	}

	/**
	 * Checking binding of package declaration
	 */
	public void test0154() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0154", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		IBinding binding = compilationUnit.getPackage().getName().resolveBinding();
		assertNotNull("The package binding is null", binding); //$NON-NLS-1$
		assertTrue("The binding is not a package binding", binding instanceof IPackageBinding); //$NON-NLS-1$
		IPackageBinding packageBinding = (IPackageBinding) binding;
		assertEquals("The package name is incorrect", "test0154", packageBinding.getName());
		IBinding binding2 = compilationUnit.getPackage().getName().resolveBinding();
		assertTrue("The package binding is not canonical", binding == binding2); //$NON-NLS-1$
	}

	/**
	 * Checking arguments positions
	 */
	public void test0155() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0155", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);  //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("The result is not a method declaration", node instanceof MethodDeclaration);  //$NON-NLS-1$
		MethodDeclaration methodDecl = (MethodDeclaration) node;
		List parameters = methodDecl.parameters();
		assertTrue("The parameters size is different from 2", parameters.size() == 2);  //$NON-NLS-1$
		Object parameter = parameters.get(0);
		assertTrue("The parameter is not a SingleVariableDeclaration", parameter instanceof SingleVariableDeclaration);  //$NON-NLS-1$
		checkSourceRange((ASTNode) parameter, "int i", source);
		parameter = parameters.get(1);
		assertTrue("The parameter is not a SingleVariableDeclaration", parameter instanceof SingleVariableDeclaration);  //$NON-NLS-1$
		checkSourceRange((ASTNode) parameter, "final boolean b", source);
	}
	
	/**
	 * Checking arguments positions
	 */
	public void test0156() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0156", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);  //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("The result is not a method declaration", node instanceof MethodDeclaration);  //$NON-NLS-1$
		MethodDeclaration methodDecl = (MethodDeclaration) node;
		List parameters = methodDecl.parameters();
		assertTrue("The parameters size is different from 1", parameters.size() == 1);  //$NON-NLS-1$
		Object parameter = parameters.get(0);
		assertTrue("The parameter is not a SingleVariableDeclaration", parameter instanceof SingleVariableDeclaration);  //$NON-NLS-1$
		checkSourceRange((ASTNode) parameter, "int i", source);
		Block block = methodDecl.getBody();
		List statements = block.statements();
		assertTrue("The statements size is different from 2", statements.size() == 2);  //$NON-NLS-1$
		ASTNode statement = (ASTNode) statements.get(0);
		assertTrue("The statements[0] is a postfixExpression statement", statement instanceof ExpressionStatement);  //$NON-NLS-1$
	}

	/**
	 * Check canonic binding for fields
	 */
	public void test0157() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "", "Test0157.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Type binding is null", typeBinding);
		assertTrue("The type binding is canonical", typeBinding == typeDeclaration.resolveBinding());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertTrue("The body declaration list is empty", bodyDeclarations.size() != 0);
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("This is not a field", bodyDeclaration instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
		List variableFragments = fieldDeclaration.fragments();
		assertTrue("The fragment list is empty", variableFragments.size() != 0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableFragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("the field binding is null", variableBinding);
		assertTrue("The field binding is not canonical", variableBinding == fragment.resolveBinding());
		typeBinding = variableBinding.getType();
		assertTrue("The type is not an array type", typeBinding.isArray());
		assertTrue("The type binding for the field is not canonical", typeBinding == variableBinding.getType());
		SimpleName name = fragment.getName();
		assertTrue("is a declaration", name.isDeclaration());
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.VARIABLE, binding.getKind());
		assertTrue("not a field", ((IVariableBinding) binding).isField());
	}

	/**
	 * Check canonic bindings for fields
	 */
	public void test0158() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "", "Test0158.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Type binding is null", typeBinding);
		assertTrue("The type binding is canonical", typeBinding == typeDeclaration.resolveBinding());
		SimpleName simpleName = typeDeclaration.getName();
		assertTrue("is a declaration", simpleName.isDeclaration());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("wrong name", simpleName.getIdentifier(), binding.getName());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertTrue("The body declaration list is empty", bodyDeclarations.size() != 0);
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("This is not a field", bodyDeclaration instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
		List variableFragments = fieldDeclaration.fragments();
		assertTrue("The fragment list is empty", variableFragments.size() != 0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableFragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("the field binding is null", variableBinding);
		assertTrue("The field binding is not canonical", variableBinding == fragment.resolveBinding());
		ITypeBinding typeBinding2 = variableBinding.getType();
		assertTrue("The type is not an array type", typeBinding2.isArray());
		assertTrue("The type binding for the field is not canonical", typeBinding2 == variableBinding.getType());
		assertTrue("The type binding for the field is not canonical with the declaration type binding", typeBinding == typeBinding2.getElementType());
	}
	
	/**
	 * Define an anonymous type
	 */
	public void test0159() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0159", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
	}
	
	/**
	 * Check bindings for multiple field declarations
	 */
	public void test0160() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0160", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Type binding is null", typeBinding);
		assertTrue("The type binding is canonical", typeBinding == typeDeclaration.resolveBinding());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertTrue("The body declaration list is empty", bodyDeclarations.size() != 0);
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("This is not a field", bodyDeclaration instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
		List variableFragments = fieldDeclaration.fragments();
		assertTrue("The fragment list size is not 2", variableFragments.size() == 2);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableFragments.get(0);
		IVariableBinding variableBinding1 = fragment.resolveBinding();
		assertNotNull("the field binding is null", variableBinding1);
		assertTrue("The field binding is not canonical", variableBinding1 == fragment.resolveBinding());
		ITypeBinding type1 = variableBinding1.getType();
		assertNotNull("The type is null", type1);
		assertTrue("The field type is canonical", type1 == variableBinding1.getType());
		assertTrue("The type is not an array type",type1.isArray());
		assertTrue("The type dimension is 1", type1.getDimensions() == 1);
		fragment = (VariableDeclarationFragment) variableFragments.get(1);
		IVariableBinding variableBinding2 = fragment.resolveBinding();
		assertNotNull("the field binding is null", variableBinding2);
		assertTrue("The field binding is not canonical", variableBinding2 == fragment.resolveBinding());
		ITypeBinding type2 = variableBinding2.getType();
		type2 = variableBinding2.getType();
		assertNotNull("The type is null", type2);
		assertTrue("The field type is canonical", type2 == variableBinding2.getType());
		assertTrue("The type is not an array type",type2.isArray());
		assertTrue("The type dimension is 2", type2.getDimensions() == 2);
		assertTrue("Element type is canonical", type1.getElementType() == type2.getElementType());
		assertTrue("type1.id < type2.id", variableBinding1.getVariableId() < variableBinding2.getVariableId());
				
	}
	
	/**
	 * Check ITypeBinding APIs:
	 *  - getModifiers()
	 *  - getElementType() when it is not an array type
	 *  - getDimensions() when it is not an array type
	 *  - getDeclaringClass()
	 *  - getDeclaringName()
	 *  - getName()
	 *  - isNested()
	 *  - isAnonymous()
	 *  - isLocal()
	 *  - isMember()
	 *  - isArray()
	 *  - getDeclaredMethods() => returns binding for default constructor
	 *  - isPrimitive()
	 *  - isTopLevel()
	 *  - getSuperclass()
	 */
	public void test0161() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0161", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("The type binding should not be null", typeBinding);
		assertEquals("The modifier is PUBLIC", Modifier.PUBLIC, typeBinding.getModifiers());
		assertNull("There is no element type", typeBinding.getElementType());
		assertEquals("There is no dimension", 0, typeBinding.getDimensions());
		assertNull("This is not a member type", typeBinding.getDeclaringClass());
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("Contains the default constructor", 1, methods.length);
		assertEquals("The name is not Test", "Test", typeBinding.getName());
		assertTrue("An anonymous class", !typeBinding.isAnonymous());
		assertTrue("A local class", !typeBinding.isLocal());
		assertTrue("A nested class", !typeBinding.isNested());
		assertTrue("A member class", !typeBinding.isMember());
		assertTrue("An array", !typeBinding.isArray());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("An interface", !typeBinding.isInterface());
		assertTrue("Not from source", typeBinding.isFromSource());
		assertTrue("Is nested", typeBinding.isTopLevel());
		assertTrue("A primitive type", !typeBinding.isPrimitive());
		ITypeBinding superclass = typeBinding.getSuperclass();
		assertNotNull("No superclass", superclass);
		assertTrue("From source", !superclass.isFromSource());
		ITypeBinding supersuperclass = superclass.getSuperclass();
		assertNull("No superclass for java.lang.Object", supersuperclass);
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		assertNotNull("No interfaces", interfaces);
		assertEquals("More then one super interface", 1, interfaces.length);
		assertTrue("is not an interface", interfaces[0].isInterface());
		assertTrue("From source", !interfaces[0].isFromSource());
		assertEquals("Has fields", 0, typeBinding.getDeclaredFields().length);
	}

	/**
	 * Check ITypeBinding APIs:
	 *  - getModifiers()
	 *  - getElementType() when it is not an array type
	 *  - getDimensions() when it is not an array type
	 *  - getDeclaringClass()
	 *  - getDeclaringName()
	 *  - getName()
	 *  - isNested()
	 *  - isAnonymous()
	 *  - isLocal()
	 *  - isMember()
	 *  - isArray()
	 *  - getDeclaredMethods() => returns binding for default constructor
	 *  - isPrimitive()
	 *  - isTopLevel()
	 *  - getSuperclass()
	 */
	public void test0162() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0162", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("The type binding should not be null", typeBinding);
		assertEquals("The modifier is PUBLIC", Modifier.PUBLIC, typeBinding.getModifiers());
		assertNull("There is no element type", typeBinding.getElementType());
		assertEquals("There is no dimension", 0, typeBinding.getDimensions());
		assertNull("This is not a member type", typeBinding.getDeclaringClass());
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("Contains no methos", 0, methods.length);
		assertEquals("The name is not Test", "Test", typeBinding.getName());
		assertTrue("An anonymous class", !typeBinding.isAnonymous());
		assertTrue("A local class", !typeBinding.isLocal());
		assertTrue("A nested class", !typeBinding.isNested());
		assertTrue("A member class", !typeBinding.isMember());
		assertTrue("An array", !typeBinding.isArray());
		assertTrue("A class", !typeBinding.isClass());
		assertTrue("Not an interface", typeBinding.isInterface());
		assertTrue("Not from source", typeBinding.isFromSource());
		assertTrue("Is nested", typeBinding.isTopLevel());
		assertTrue("A primitive type", !typeBinding.isPrimitive());
		ITypeBinding superclass = typeBinding.getSuperclass();
		assertNull("No superclass", superclass);
		assertEquals("Has fields", 0, typeBinding.getDeclaredFields().length);
	}

	/**
	 * Test binding for anonymous declaration: new java.lang.Object() {}
	 */
	public void test0163() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0163", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode expression = getASTNodeToCompare((CompilationUnit) result);
		assertNotNull("Expression should not be null", expression); //$NON-NLS-1$
		assertTrue("Not an anonymous type declaration", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation anonymousClass = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = anonymousClass.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not an anonymous class", typeBinding.isAnonymous());
		assertEquals("The modifier is not default", Modifier.NONE, typeBinding.getModifiers());
		assertNull("There is no element type", typeBinding.getElementType());
		assertEquals("There is no dimension", 0, typeBinding.getDimensions());
		assertNotNull("This is a member type", typeBinding.getDeclaringClass());
		assertEquals("The name is not empty", "", typeBinding.getName());
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("Contains the default constructor", 1, methods.length);
		assertTrue("Not a local class", typeBinding.isLocal());
		assertTrue("Not a nested class", typeBinding.isNested());
		assertTrue("A member class", !typeBinding.isMember());
		assertTrue("An array", !typeBinding.isArray());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("An interface", !typeBinding.isInterface());
		assertTrue("Not from source", typeBinding.isFromSource());
		assertTrue("Is a top level", !typeBinding.isTopLevel());
		assertTrue("A primitive type", !typeBinding.isPrimitive());
		ITypeBinding superclass = typeBinding.getSuperclass();
		assertNotNull("No superclass", superclass);
		assertEquals("Has fields", 0, typeBinding.getDeclaredFields().length);
	}
	
	/**
	 * Test binding for member type declaration
	 */
	public void test0164() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0164", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("Not an type declaration", node instanceof TypeDeclaration);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("An anonymous class", !typeBinding.isAnonymous());
		assertEquals("The modifier is not default", Modifier.PRIVATE, typeBinding.getModifiers());
		assertNull("There is no element type", typeBinding.getElementType());
		assertEquals("There is no dimension", 0, typeBinding.getDimensions());
		assertNotNull("This is not a member type", typeBinding.getDeclaringClass());
		assertEquals("The name is not 'B'", "B", typeBinding.getName());
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("Contains the default constructor", 1, methods.length);
		assertTrue("A local class", !typeBinding.isLocal());
		assertTrue("Not a nested class", typeBinding.isNested());
		assertTrue("Not a member class", typeBinding.isMember());
		assertTrue("An array", !typeBinding.isArray());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("An interface", !typeBinding.isInterface());
		assertTrue("Not from source", typeBinding.isFromSource());
		assertTrue("Is a top level", !typeBinding.isTopLevel());
		assertTrue("A primitive type", !typeBinding.isPrimitive());
		ITypeBinding superclass = typeBinding.getSuperclass();
		assertNotNull("No superclass", superclass);
		assertEquals("Has fields", 0, typeBinding.getDeclaredFields().length);
	}
	
	/**
	 * Test binding for local type declaration
	 */
	public void test0165() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0165", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("Not an type declaration", node instanceof TypeDeclarationStatement);
		TypeDeclarationStatement statement = (TypeDeclarationStatement) node;
		TypeDeclaration typeDeclaration = statement.getTypeDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("An anonymous class", !typeBinding.isAnonymous());
		assertEquals("The modifier is not default", Modifier.NONE, typeBinding.getModifiers());
		assertNull("There is no element type", typeBinding.getElementType());
		assertEquals("There is no dimension", 0, typeBinding.getDimensions());
		assertNotNull("This is not a member type", typeBinding.getDeclaringClass());
		assertEquals("The name is not 'C'", "C", typeBinding.getName());
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("Contains the default constructor", 1, methods.length);
		assertTrue("Not a local class", typeBinding.isLocal());
		assertTrue("Not a nested class", typeBinding.isNested());
		assertTrue("A member class", !typeBinding.isMember());
		assertTrue("An array", !typeBinding.isArray());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("An interface", !typeBinding.isInterface());
		assertTrue("Not from source", typeBinding.isFromSource());
		assertTrue("Is a top level", !typeBinding.isTopLevel());
		assertTrue("A primitive type", !typeBinding.isPrimitive());
		ITypeBinding superclass = typeBinding.getSuperclass();
		assertNotNull("No superclass", superclass);
		assertEquals("Has fields", 0, typeBinding.getDeclaredFields().length);
	}

	/**
	 * Multiple local declaration => VariabledeclarationStatement
	 */
	public void test0166() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0166", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("Fragment list is not 4 ", fragments.size() == 4);
		VariableDeclarationFragment fragment1 = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding binding1 = fragment1.resolveBinding();
		assertNotNull("Binding is null", binding1);
		assertEquals("wrong name for binding1", "x", binding1.getName());
		assertEquals("wrong modifier for binding1", 0, binding1.getModifiers());
		assertTrue("a field", !binding1.isField());
		assertNull("declaring class is not null", binding1.getDeclaringClass());
		ITypeBinding typeBinding1 = binding1.getType();
		assertNotNull("typeBinding1 is null", typeBinding1);
		assertTrue("typeBinding1 is not a primitive type", typeBinding1.isPrimitive());
		assertTrue("typeBinding1 is not canonical", typeBinding1 == binding1.getType());
		VariableDeclarationFragment fragment2 = (VariableDeclarationFragment) fragments.get(1);
		IVariableBinding binding2 = fragment2.resolveBinding();
		assertNotNull("Binding is null", binding2);
		assertEquals("wrong name for binding2", "z", binding2.getName());
		assertEquals("wrong modifier for binding2", 0, binding2.getModifiers());
		assertTrue("a field", !binding2.isField());
		assertNull("declaring class is not null", binding2.getDeclaringClass());
		ITypeBinding typeBinding2 = binding2.getType();
		assertNotNull("typeBinding2 is null", typeBinding2);
		assertTrue("typeBinding2 is not an array type", typeBinding2.isArray());
		assertTrue("typeBinding2 is not canonical", typeBinding2 == binding2.getType());
		assertTrue("primitive type is not canonical", typeBinding1 == typeBinding2.getElementType());
		assertEquals("dimension is 1", 1, typeBinding2.getDimensions());
		assertEquals("it is not int[]", "int[]", typeBinding2.getName());		
		VariableDeclarationFragment fragment3 = (VariableDeclarationFragment) fragments.get(2);
		IVariableBinding binding3 = fragment3.resolveBinding();
		assertNotNull("Binding is null", binding3);
		assertEquals("wrong name for binding3", "i", binding3.getName());
		assertEquals("wrong modifier for binding3", 0, binding3.getModifiers());
		assertTrue("a field", !binding3.isField());
		assertNull("declaring class is not null", binding3.getDeclaringClass());
		ITypeBinding typeBinding3 = binding3.getType();
		assertNotNull("typeBinding3 is null", typeBinding3);
		assertTrue("typeBinding3 is not an primitive type", typeBinding3.isPrimitive());
		assertTrue("typeBinding3 is not canonical", typeBinding3 == binding3.getType());
		assertTrue("primitive type is not canonical", typeBinding1 == typeBinding3);
		assertEquals("dimension is 0", 0, typeBinding3.getDimensions());
		assertEquals("it is not the primitive type int", "int", typeBinding3.getName());
		VariableDeclarationFragment fragment4 = (VariableDeclarationFragment) fragments.get(3);
		IVariableBinding binding4 = fragment4.resolveBinding();
		assertNotNull("Binding is null", binding4);
		assertEquals("wrong name for binding4", "j", binding4.getName());
		assertEquals("wrong modifier for binding4", 0, binding4.getModifiers());
		assertTrue("a field", !binding4.isField());
		assertNull("declaring class is not null", binding4.getDeclaringClass());
		ITypeBinding typeBinding4 = binding4.getType();
		assertNotNull("typeBinding4 is null", typeBinding4);
		assertTrue("typeBinding4 is not an array type", typeBinding4.isArray());
		assertTrue("typeBinding4 is not canonical", typeBinding4 == binding4.getType());
		assertTrue("primitive type is not canonical", typeBinding1 == typeBinding4.getElementType());
		assertEquals("dimension is 2", 2, typeBinding4.getDimensions());
		assertEquals("it is not int[][]", "int[][]", typeBinding4.getName());
		assertTrue("ids in the wrong order", binding1.getVariableId() < binding2.getVariableId());
		assertTrue("ids in the wrong order", binding2.getVariableId() < binding3.getVariableId());
		assertTrue("ids in the wrong order", binding3.getVariableId() < binding4.getVariableId());
	}
	
	/**
	 * Check source position for new Test[1+2].length.
	 */
	public void test0167() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0167", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("Instance of VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("fragment list size is not 1", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = fragment.getInitializer();
		assertNotNull("No initialization", initialization);
		assertTrue("Not a FieldAccess", initialization instanceof FieldAccess);
		checkSourceRange(initialization, "new Test[1+2].length", source); //$NON-NLS-1$
	}
	
	/**
	 * Check package binding: test0168.test
	 */
	public void test0168() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0168.test1", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Binding not null", typeBinding);
		IPackageBinding packageBinding = typeBinding.getPackage();
		assertNotNull("No package binding", packageBinding);
		assertEquals("wrong name", "test0168.test1", packageBinding.getName());
		String[] components = packageBinding.getNameComponents();
		assertNotNull("no components", components);
		assertTrue("components size != 2", components.length == 2);
		assertEquals("wrong component name", "test0168", components[0]);
		assertEquals("wrong component name", "test1", components[1]);
		assertEquals("wrong type", IPackageBinding.PACKAGE, packageBinding.getKind());
		assertTrue("Unnamed package", !packageBinding.isUnnamed());
		assertTrue("Package binding is not canonical", packageBinding == typeBinding.getPackage());
	}
	
	/**
	 * Check package binding: test0169
	 */
	public void test0169() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0169", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Binding not null", typeBinding);
		IPackageBinding packageBinding = typeBinding.getPackage();
		assertNotNull("No package binding", packageBinding);
		assertEquals("wrong name", "test0169", packageBinding.getName());
		String[] components = packageBinding.getNameComponents();
		assertNotNull("no components", components);
		assertTrue("components size != 1", components.length == 1);
		assertEquals("wrong component name", "test0169", components[0]);
		assertEquals("wrong type", IPackageBinding.PACKAGE, packageBinding.getKind());
		assertTrue("Unnamed package", !packageBinding.isUnnamed());
		assertTrue("Package binding is not canonical", packageBinding == typeBinding.getPackage());
	}
	
	/**
	 * Check package binding: test0170
	 */
	public void test0170() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "", "Test0170.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Binding not null", typeBinding);
		IPackageBinding packageBinding = typeBinding.getPackage();
		assertNotNull("No package binding", packageBinding);
		assertEquals("wrong name", "UNNAMED", packageBinding.getName());
		String[] components = packageBinding.getNameComponents();
		assertNotNull("no components", components);
		assertTrue("components size != 0", components.length == 0);
		assertEquals("wrong type", IPackageBinding.PACKAGE, packageBinding.getKind());
		assertTrue("Not an unnamed package", packageBinding.isUnnamed());
		assertTrue("Package binding is not canonical", packageBinding == typeBinding.getPackage());
	}

	/**
	 * Check package binding: test0171
	 */
	public void test0171() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0171", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() == 2);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Binding not null", typeBinding);
		IPackageBinding packageBinding = typeBinding.getPackage();
		assertNotNull("No package binding", packageBinding);
		assertEquals("wrong name", "test0171", packageBinding.getName());
		String[] components = packageBinding.getNameComponents();
		assertNotNull("no components", components);
		assertTrue("components size != 1", components.length == 1);
		assertEquals("wrong component name", "test0171", components[0]);
		assertEquals("wrong type", IPackageBinding.PACKAGE, packageBinding.getKind());
		assertTrue("Unnamed package", !packageBinding.isUnnamed());
		assertTrue("Package binding is not canonical", packageBinding == typeBinding.getPackage());
		
		typeDeclaration = (TypeDeclaration) types.get(1);
		typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Binding not null", typeBinding);
		IPackageBinding packageBinding2 = typeBinding.getPackage();
		assertNotNull("No package binding", packageBinding);
		assertTrue("Package binding is not canonical", packageBinding == packageBinding2);
	}

	/**
	 * Check method binding
	 */
	public void test0172() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0172", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		List types = compilationUnit.types();
		assertTrue("The types list is empty", types.size() != 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("Binding not null", typeBinding);
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("methods.length != 4", 4, methods.length);
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("body declaration size != 3", 3, bodyDeclarations.size());
		MethodDeclaration method1 = (MethodDeclaration) bodyDeclarations.get(0);
		IMethodBinding methodBinding1 = method1.resolveBinding();
		assertNotNull("No method binding for foo", methodBinding1);
		SimpleName simpleName = method1.getName();
		assertTrue("not a declaration", simpleName.isDeclaration());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong name", binding.getName(), simpleName.getIdentifier());
		assertTrue("Canonical method binding", methodBinding1 == methods[1]);
		assertTrue("declaring class is canonical", typeBinding == methodBinding1.getDeclaringClass());
		ITypeBinding[] exceptionTypes = methodBinding1.getExceptionTypes();
		assertNotNull("No exception types", exceptionTypes);
		assertEquals("One exception", 1, exceptionTypes.length);
		assertEquals("wrong name for exception", "IOException", exceptionTypes[0].getName());
		assertEquals("wrong modifier", Modifier.NONE, methodBinding1.getModifiers());
		assertEquals("wrong name for method", "foo", methodBinding1.getName());
		ITypeBinding[] parameters = methodBinding1.getParameterTypes();
		assertNotNull("No parameters", parameters);
		assertEquals("wrong size", 1, parameters.length);
		assertEquals("wrong type", "int[]", parameters[0].getName());
		assertEquals("wrong return type", "void", methodBinding1.getReturnType().getName());
		assertTrue("A constructor", !methodBinding1.isConstructor());
		
		MethodDeclaration method2 = (MethodDeclaration) bodyDeclarations.get(1);
		IMethodBinding methodBinding2 = method2.resolveBinding();
		assertNotNull("No method binding for main", methodBinding2);
		assertTrue("Canonical method binding", methodBinding2 == methods[2]);
		assertTrue("declaring class is canonical", typeBinding == methodBinding2.getDeclaringClass());
		ITypeBinding[] exceptionTypes2 = methodBinding2.getExceptionTypes();
		assertNotNull("No exception types", exceptionTypes2);
		assertEquals("No exception", 0, exceptionTypes2.length);
		assertEquals("wrong modifier", Modifier.PUBLIC | Modifier.STATIC, methodBinding2.getModifiers());
		assertEquals("wrong name for method", "main", methodBinding2.getName());
		ITypeBinding[] parameters2 = methodBinding2.getParameterTypes();
		assertNotNull("No parameters", parameters2);
		assertEquals("wrong size", 1, parameters2.length);
		assertEquals("wrong type for parameter2[0]", "String[]", parameters2[0].getName());
		assertEquals("wrong return type", "void", methodBinding2.getReturnType().getName());
		assertTrue("A constructor", !methodBinding2.isConstructor());
		
		MethodDeclaration method3 = (MethodDeclaration) bodyDeclarations.get(2);
		IMethodBinding methodBinding3 = method3.resolveBinding();
		assertNotNull("No method binding for main", methodBinding3);
		assertTrue("Canonical method binding", methodBinding3 == methods[3]);
		assertTrue("declaring class is canonical", typeBinding == methodBinding3.getDeclaringClass());
		ITypeBinding[] exceptionTypes3 = methodBinding3.getExceptionTypes();
		assertNotNull("No exception types", exceptionTypes3);
		assertEquals("No exception", 1, exceptionTypes3.length);
		assertEquals("wrong modifier", Modifier.PRIVATE, methodBinding3.getModifiers());
		assertEquals("wrong name for method", "bar", methodBinding3.getName());
		ITypeBinding[] parameters3 = methodBinding3.getParameterTypes();
		assertNotNull("No parameters", parameters3);
		assertEquals("wrong size", 1, parameters3.length);
		assertEquals("wrong type", "String", parameters3[0].getName());
		assertEquals("wrong return type", "String", methodBinding3.getReturnType().getName());
		assertTrue("A constructor", !methodBinding3.isConstructor());
		assertTrue("The binding is not canonical", parameters3[0] == methodBinding3.getReturnType());
	}
	
	/**
	 * i++; IVariableBinding
	 */
	public void test0173() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0173", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("Not an expressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression ex = expressionStatement.getExpression();
		assertTrue("Not a postfixexpression", ex instanceof PostfixExpression);
		PostfixExpression postfixExpression = (PostfixExpression) ex;
		Expression expr = postfixExpression.getOperand();
		assertTrue("Not a simpleName", expr instanceof SimpleName);
		SimpleName name = (SimpleName) expr;
		assertTrue("a declaration", !name.isDeclaration());
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);

		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertTrue(variableBinding == binding);
	}

	/**
	 * i++; IVariableBinding (field)
	 */
	public void test0174() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0174", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("Not an expressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression ex = expressionStatement.getExpression();
		assertTrue("Not a postfixexpression", ex instanceof PostfixExpression);
		PostfixExpression postfixExpression = (PostfixExpression) ex;
		Expression expr = postfixExpression.getOperand();
		assertTrue("Not a simpleName", expr instanceof SimpleName);
		SimpleName name = (SimpleName) expr;
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);

		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("FieldDeclaration", node2 instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node2;
		List fragments = fieldDeclaration.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertTrue(variableBinding == binding);
	}
	
	/**
	 * int i = 0; Test IntBinding for the field declaration and the 0 literal
	 */
	public void test0175() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0175", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node2;
		List fragments = fieldDeclaration.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		ITypeBinding typeBinding = fragment.getInitializer().resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not a primitive type", typeBinding.isPrimitive());
		assertEquals("Not int", "int", typeBinding.getName());
		assertTrue(variableBinding.getType() == typeBinding);
	}
	
	/**
	 * ThisReference
	 */
	public void test0176() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0176", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("Return statement", node2 instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node2;
		assertTrue("Not a field access", returnStatement.getExpression() instanceof FieldAccess);
		FieldAccess fieldAccess = (FieldAccess) returnStatement.getExpression();
		ITypeBinding typeBinding = fieldAccess.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not a primitive type", typeBinding.isPrimitive());
		assertEquals("Not int", "int", typeBinding.getName());
		Expression expr = fieldAccess.getExpression();
		assertTrue("Not a this expression", expr instanceof ThisExpression);
		ThisExpression thisExpression = (ThisExpression) expr;
		ITypeBinding typeBinding2 = thisExpression.resolveTypeBinding();
		assertNotNull("No type binding2", typeBinding2);
		assertEquals("Not Test", "Test", typeBinding2.getName());
	}	

	/**
	 * i++; IVariableBinding
	 */
	public void test0177() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0177", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 1, 1);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("Not an expressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression ex = expressionStatement.getExpression();
		assertTrue("Not a postfixexpression", ex instanceof PostfixExpression);
		PostfixExpression postfixExpression = (PostfixExpression) ex;
		Expression expr = postfixExpression.getOperand();
		assertTrue("Not a simpleName", expr instanceof SimpleName);
		SimpleName name = (SimpleName) expr;
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);

		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertEquals("return type is not int", "int", variableBinding.getType().getName());
		assertTrue(variableBinding == binding);
	}

	/**
	 * SuperReference
	 */
	public void test0178() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0178", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 1, 0, 0);
		assertTrue("Return statement", node2 instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node2;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a field access", expr instanceof SuperFieldAccess);
		SuperFieldAccess fieldAccess = (SuperFieldAccess) expr;
		ITypeBinding typeBinding = fieldAccess.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not a primitive type", typeBinding.isPrimitive());
		assertEquals("Not int", "int", typeBinding.getName());
	}	
	
	/**
	 * Allocation expression
	 */
	public void test0179() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0179", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		Expression initialization = fragment.getInitializer();
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue(variableBinding.getType() == typeBinding);
	}	

	/**
	 * Allocation expression
	 */
	public void test0180() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0180", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		Expression initialization = fragment.getInitializer();
		assertTrue("No an array creation", initialization instanceof ArrayCreation);
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not an array", typeBinding.isArray());
		assertTrue(variableBinding.getType() == typeBinding);
	}	

	/**
	 * Allocation expression
	 */
	public void test0181() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0181", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		Expression initialization = fragment.getInitializer();
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not an array", typeBinding.isArray());
		assertTrue(variableBinding.getType() == typeBinding);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0182() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0182", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("IfStatement", node2 instanceof IfStatement);
		IfStatement ifStatement = (IfStatement) node2;
		Expression expr = ifStatement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i < 10", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0183() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0183", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("IfStatement", node2 instanceof IfStatement);
		IfStatement ifStatement = (IfStatement) node2;
		Expression expr = ifStatement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i < 10 && i < 20", source);
	}	
	
	/**
	 * BinaryExpression
	 */
	public void test0184() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0184", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("IfStatement", node2 instanceof IfStatement);
		IfStatement ifStatement = (IfStatement) node2;
		Expression expr = ifStatement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i < 10 || i < 20", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0185() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0185", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("IfStatement", node2 instanceof IfStatement);
		IfStatement ifStatement = (IfStatement) node2;
		Expression expr = ifStatement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i == 10", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0186() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0186", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("IfStatement", node2 instanceof IfStatement);
		IfStatement ifStatement = (IfStatement) node2;
		Expression expr = ifStatement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "o == o", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0187() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0187", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("IfStatement", node2 instanceof WhileStatement);
		WhileStatement whileStatement = (WhileStatement) node2;
		Expression expr = whileStatement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i <= 10", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0188() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0188", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 2);
		assertTrue("DoStatement", node2 instanceof DoStatement);
		DoStatement statement = (DoStatement) node2;
		Expression expr = statement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i <= 10", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0189() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0189", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("ForStatement", node2 instanceof ForStatement);
		ForStatement statement = (ForStatement) node2;
		Expression expr = statement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "i < 10", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0190() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0190", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 2, 1);
		assertTrue("IfStatement", node2 instanceof IfStatement);
		IfStatement statement = (IfStatement) node2;
		Expression expr = statement.getExpression();
		assertNotNull("No condition", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "scanner.x < selection.start && selection.start < scanner.y", source);
	}	

	/**
	 * BinaryExpression
	 */
	public void test0191() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0191", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression ex = expressionStatement.getExpression();
		assertTrue("Assignment", ex instanceof Assignment);
		Assignment statement = (Assignment) ex;
		Expression rightExpr = statement.getRightHandSide();
		assertTrue("Not an infix expression", rightExpr instanceof InfixExpression);
		InfixExpression infixExpression = (InfixExpression) rightExpr;
		Expression expr = infixExpression.getRightOperand();
		assertNotNull("No right hand side expression", expr);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "boolean", typeBinding.getName());
		checkSourceRange(expr, "2 < 20", source);
	}	

	/**
	 * Initializer
	 */
	public void test0192() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0192", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		Expression initialization = fragment.getInitializer();
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue(variableBinding.getType() == typeBinding);
		checkSourceRange(initialization, "0", source);
	}	

	/**
	 * Initializer
	 */
	public void test0193() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0193", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		Expression initialization = fragment.getInitializer();
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue(variableBinding.getType() == typeBinding);
		checkSourceRange(initialization, "new Inner()", source);
		assertEquals("Wrong type", "Inner", typeBinding.getName());
	}	

	/**
	 * Initializer
	 */
	public void test0194() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0194", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		Expression initialization = fragment.getInitializer();
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue(variableBinding.getType() == typeBinding);
		checkSourceRange(initialization, "new Inner[10]", source);
		assertTrue("Not an array", typeBinding.isArray());
		assertEquals("Wrong type", "Inner[]", typeBinding.getName());
	}	

	/**
	 * Initializer
	 */
	public void test0195() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0195", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 1, 0, 1);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression ex = expressionStatement.getExpression();
		assertTrue("MethodInvocation", ex instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) ex;
		checkSourceRange(methodInvocation, "a.useFile(/*]*/a.getFile()/*[*/)", source);
		List list = methodInvocation.arguments();
		assertTrue("Parameter list not empty", list.size() == 1);
		Expression parameter = (Expression) list.get(0);
		assertTrue("Not a method invocation", parameter instanceof MethodInvocation);
		ITypeBinding typeBinding = parameter.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not a boolean", "File", typeBinding.getName());
		checkSourceRange(parameter, "a.getFile()", source);
	}	

	/**
	 * Initializer
	 */
	public void test0196() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0196", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 2);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression ex = expressionStatement.getExpression();
		assertTrue("Assignment", ex instanceof Assignment);
		Assignment statement = (Assignment) ex;
		Expression rightExpr = statement.getRightHandSide();
		assertTrue("Not an instanceof expression", rightExpr instanceof InstanceofExpression);
		ITypeBinding typeBinding = rightExpr.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("wrong type", "boolean", typeBinding.getName());
		checkSourceRange(rightExpr, "inner instanceof Inner", source);
	}	

	/**
	 * Initializer
	 */
	public void test0197() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0197", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 1, 0, 1);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression ex = expressionStatement.getExpression();
		assertTrue("MethodInvocation", ex instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) ex;
		checkSourceRange(methodInvocation, "a.getFile()/*[*/.getName()", source);
		Expression receiver = methodInvocation.getExpression();
		assertTrue("Not a method invocation", receiver instanceof MethodInvocation);
		MethodInvocation methodInvocation2 = (MethodInvocation) receiver;
		ITypeBinding typeBinding = methodInvocation2.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "File", typeBinding.getName());
		checkSourceRange(methodInvocation2, "a.getFile()", source);
	}	

	/**
	 * Initializer
	 */
	public void test0198() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0198", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("ReturnStatement", node2 instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node2;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not an infixExpression", expr instanceof InfixExpression);
		InfixExpression infixExpression = (InfixExpression) expr;
		Expression left = infixExpression.getLeftOperand();
		assertTrue("Not an InfixExpression", left instanceof InfixExpression);
		InfixExpression infixExpression2 = (InfixExpression) left;
		Expression right = infixExpression2.getRightOperand();
		assertTrue("Not an InfixExpression", right instanceof InfixExpression);
		InfixExpression infixExpression3 = (InfixExpression) right;
		assertEquals("A multiplication", InfixExpression.Operator.TIMES, infixExpression3.getOperator());
		ITypeBinding typeBinding = infixExpression3.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not int", "int", typeBinding.getName());
		checkSourceRange(infixExpression3, "20 * 30", source);
	}	

	/**
	 * Initializer
	 */
	public void test0199() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0199", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = fragment.getInitializer();
		assertTrue("Not an infixExpression", initialization instanceof InfixExpression);
		InfixExpression infixExpression = (InfixExpression) initialization;
		Expression left = infixExpression.getLeftOperand();
		assertTrue("Not an InfixExpression", left instanceof InfixExpression);
		InfixExpression infixExpression2 = (InfixExpression) left;
		Expression right = infixExpression2.getRightOperand();
		assertTrue("Not an InfixExpression", right instanceof InfixExpression);
		InfixExpression infixExpression3 = (InfixExpression) right;
		assertEquals("A multiplication", InfixExpression.Operator.TIMES, infixExpression3.getOperator());
		ITypeBinding typeBinding = infixExpression3.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not int", "int", typeBinding.getName());
		checkSourceRange(infixExpression3, "10 * 30", source);
	}	

	/**
	 * Initializer
	 */
	public void test0200() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0200", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 1, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertTrue("No fragment", fragments.size() == 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = fragment.getInitializer();
		assertTrue("Not an infixExpression", initialization instanceof FieldAccess);
		FieldAccess fieldAccess = (FieldAccess) initialization;
		Expression receiver = fieldAccess.getExpression();
		assertTrue("ArrayCreation", receiver instanceof ArrayCreation);
		ArrayCreation arrayCreation = (ArrayCreation) receiver;
		List dimensions = arrayCreation.dimensions();
		assertEquals("Wrong dimension", 1, dimensions.size());
		Expression dim = (Expression) dimensions.get(0);
		assertTrue("InfixExpression", dim instanceof InfixExpression);
		InfixExpression infixExpression = (InfixExpression) dim;
		ITypeBinding typeBinding = infixExpression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Not int", "int", typeBinding.getName());
		checkSourceRange(infixExpression, "1 + 2", source);
	}	

	/**
	 * Position inside for statement: PR 3300
	 */
	public void test0201() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0201", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("ForStatement", node2 instanceof ForStatement);
		ForStatement forStatement = (ForStatement) node2;
		List initializers = forStatement.initializers();
		assertTrue("wrong size", initializers.size() == 1);
		Expression init = (Expression) initializers.get(0);
		checkSourceRange(init, "int i= 0", source);
	}
	
	/**
	 * PR 7386
	 */
	public void test0202() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0202", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("FieldDeclaration", node2 instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node2;
		checkSourceRange(fieldDeclaration, "int f= (2);", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = fragment.getInitializer();
		assertTrue("Not a parenthesized expression", initialization instanceof ParenthesizedExpression);
		checkSourceRange(initialization, "(2)", source);
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("no binding", typeBinding);
		assertEquals("not int", "int", typeBinding.getName());
	}		

	/**
	 * PR 7386
	 */
	public void test0203() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0203", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("FieldDeclaration", node2 instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node2;
		checkSourceRange(fieldDeclaration, "int f= (2);", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = fragment.getInitializer();
		assertTrue("Not a parenthesized expression", initialization instanceof ParenthesizedExpression);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) initialization;
		checkSourceRange(parenthesizedExpression, "(2)", source);
		Expression expr = parenthesizedExpression.getExpression();
		checkSourceRange(expr, "2", source);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("no binding", typeBinding);
		assertEquals("not int", "int", typeBinding.getName());
		assertTrue("type binding is canonical", typeBinding == parenthesizedExpression.resolveTypeBinding());
	}		

	/**
	 * PR 7386
	 */
	public void test0204() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0204", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("FieldDeclaration", node2 instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node2;
		checkSourceRange(fieldDeclaration, "int f= ((2));", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = fragment.getInitializer();
		assertTrue("Not a parenthesized expression", initialization instanceof ParenthesizedExpression);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) initialization;
		checkSourceRange(parenthesizedExpression, "((2))", source);
		Expression expr = parenthesizedExpression.getExpression();
		assertTrue("Not a parenthesized expression", expr instanceof ParenthesizedExpression);
		ParenthesizedExpression parenthesizedExpression2 = (ParenthesizedExpression) expr;
		checkSourceRange(parenthesizedExpression2, "(2)", source);
		expr = parenthesizedExpression2.getExpression();
		checkSourceRange(expr, "2", source);
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("no binding", typeBinding);
		assertEquals("not int", "int", typeBinding.getName());
		typeBinding = parenthesizedExpression.resolveTypeBinding();
		assertNotNull("no binding", typeBinding);
		assertEquals("not int", "int", typeBinding.getName());
		assertTrue("type binding is canonical", typeBinding == parenthesizedExpression2.resolveTypeBinding());
	}		


	/**
	 * Local class end position when trailing comment
	 */
	public void test0205() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0205", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("TypeDeclarationStatement", node2 instanceof TypeDeclarationStatement);
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node2;
		TypeDeclaration typeDeclaration = typeDeclarationStatement.getTypeDeclaration();
		assertEquals("wrong name", "AA", typeDeclaration.getName().getIdentifier());
		checkSourceRange(typeDeclaration, "class AA extends Test {}", source);
	}		

	/**
	 * QualifiedName
	 */
	public void test0206() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0206", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 2, 0);
		assertTrue("ReturnStatement", node2 instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node2;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a qualifiedName", expr instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) expr;
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Not an int (typeBinding)", "int", typeBinding.getName());
		checkSourceRange(qualifiedName, "field.field.field.field.i", source);
		assertTrue("Not a simple name", qualifiedName.getName().isSimpleName());
		SimpleName simpleName = qualifiedName.getName();
		assertTrue("a declaration", !simpleName.isDeclaration());
		checkSourceRange(simpleName, "i", source);
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();
		assertNotNull("No typebinding2", typeBinding2);
		assertEquals("Not an int (typeBinding2)", "int", typeBinding2.getName());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("VariableBinding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Not Test", "Test", variableBinding.getDeclaringClass().getName());
		assertEquals("Not default", Modifier.PUBLIC, variableBinding.getModifiers());
		Name qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", qualifierName.isQualifiedName());
		checkSourceRange(qualifierName, "field.field.field.field", source);
		ITypeBinding typeBinding5 = qualifierName.resolveTypeBinding();
		assertNotNull("No binding5", typeBinding5);
		assertEquals("Not Test", "Test", typeBinding5.getName());

		qualifiedName = (QualifiedName) qualifierName;
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "field", source);
		ITypeBinding typeBinding6 = simpleName.resolveTypeBinding();
		assertNotNull("No binding6", typeBinding6);
		assertEquals("Not Test", "Test", typeBinding6.getName());
		
		qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", qualifierName.isQualifiedName());
		checkSourceRange(qualifierName, "field.field.field", source);
		ITypeBinding typeBinding7 = qualifierName.resolveTypeBinding();
		assertNotNull("No binding7", typeBinding7);
		assertEquals("Not Test", "Test", typeBinding7.getName());
		
		qualifiedName = (QualifiedName) qualifierName;
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "field", source);
		qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", qualifierName.isQualifiedName());
		checkSourceRange(qualifierName, "field.field", source);
		ITypeBinding typeBinding3 = qualifierName.resolveTypeBinding();
		assertNotNull("No binding3", typeBinding3);
		assertEquals("Not Test", "Test", typeBinding3.getName());
		qualifiedName = (QualifiedName) qualifierName;
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "field", source);
		qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a simple name", qualifierName.isSimpleName());
		assertTrue("a declaration", !((SimpleName)qualifierName).isDeclaration());
		checkSourceRange(qualifierName, "field", source);
		ITypeBinding typeBinding4 = qualifierName.resolveTypeBinding();
		assertNotNull("No binding4", typeBinding4);
		assertEquals("Not Test", "Test", typeBinding4.getName());
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test0207() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0207", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		Javadoc actualJavadoc = ((MethodDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n  */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		checkSourceRange(node, "/** JavaDoc Comment\r\n  */\r\n  void foo(final int i) {}", source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n  */", source);
	}
	
	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test0208() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0208", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		Javadoc actualJavadoc = ((MethodDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "void foo(final int i) {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test0209() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0209", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a MethodDeclaration", node instanceof MethodDeclaration);
		Javadoc actualJavadoc = ((MethodDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "void foo(final int i) {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for FieldDeclaration
	 */
	public void test0210() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0210", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		Javadoc actualJavadoc = ((FieldDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n  */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		checkSourceRange(node, "/** JavaDoc Comment\r\n  */\r\n  int i;", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for FieldDeclaration
	 */
	public void test0211() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0211", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		Javadoc actualJavadoc = ((FieldDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for FieldDeclaration
	 */
	public void test0212() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0212", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a FieldDeclaration", node instanceof FieldDeclaration);
		Javadoc actualJavadoc = ((FieldDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "int i;", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for TypeDeclaration
	 */
	public void test0213() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0213", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		String expectedContents = "public class Test {\r\n" +//$NON-NLS-1$
			"  int i;\r\n"  +//$NON-NLS-1$
			"}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for TypeDeclaration
	 */
	public void test0214() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0214", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		String expectedContents = "public class Test {\r\n" +//$NON-NLS-1$
			"  int i;\r\n"  +//$NON-NLS-1$
			"}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for TypeDeclaration
	 */
	public void test0215() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0215", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 " */\r\n" + //$NON-NLS-1$
			"public class Test {\r\n" +//$NON-NLS-1$
			"  int i;\r\n"  +//$NON-NLS-1$
			"}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n */", source);
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0216() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0216", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n	 */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 "	 */\r\n" + //$NON-NLS-1$
			 "  class B {}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n	 */", source);
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0217() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0217", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "class B {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0218() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0218", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "public static class B {}", source); //$NON-NLS-1$
	}

	/**
	 * Check javadoc for MemberTypeDeclaration
	 */
	public void test0219() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0219", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		assertTrue("The node is not a TypeDeclaration", node instanceof TypeDeclaration);
		Javadoc actualJavadoc = ((TypeDeclaration) node).getJavadoc();
		assertTrue("Javadoc must be null", actualJavadoc == null);//$NON-NLS-1$
		checkSourceRange(node, "public static class B {}", source); //$NON-NLS-1$
	}

	/**
	 * Checking initializers
	 */
	public void test0220() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0220", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		checkSourceRange(node, "{}", source); //$NON-NLS-1$
	}

	/**
	 * Checking initializers
	 */
	public void test0221() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0221", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		checkSourceRange(node, "static {}", source); //$NON-NLS-1$
	}

	/**
	 * Checking initializers
	 */
	public void test0222() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0222", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Javadoc actualJavadoc = ((Initializer) node).getJavadoc();
		assertNotNull("Javadoc comment should no be null", actualJavadoc); //$NON-NLS-1$
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n	 */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 "	 */\r\n" + //$NON-NLS-1$
			 "  static {}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n	 */", source);
		
	}

	/**
	 * Checking initializers
	 */
	public void test0223() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0223", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Javadoc actualJavadoc = ((Initializer) node).getJavadoc();
		assertNotNull("Javadoc comment should not be null", actualJavadoc); //$NON-NLS-1$
		Javadoc javadoc = this.ast.newJavadoc();
		javadoc.setComment("/** JavaDoc Comment\r\n	 */");//$NON-NLS-1$*/
		assertTrue("Both AST trees should be identical", javadoc.subtreeMatch(new ASTMatcher(), actualJavadoc));//$NON-NLS-1$
		String expectedContents = 
			 "/** JavaDoc Comment\r\n" + //$NON-NLS-1$
			 "	 */\r\n" + //$NON-NLS-1$
			 "  {}";//$NON-NLS-1$
		checkSourceRange(node, expectedContents, source); //$NON-NLS-1$
		checkSourceRange(actualJavadoc, "/** JavaDoc Comment\r\n	 */", source);
		
	}

	/**
	 * Checking initializers
	 */
	public void test0224() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0224", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		Javadoc actualJavadoc = ((Initializer) node).getJavadoc();
		assertNull("Javadoc comment should be null", actualJavadoc); //$NON-NLS-1$
		checkSourceRange(node, "{}", source); //$NON-NLS-1$
	}

	/**
	 * Continue ==> ContinueStatement
	 */
	public void test0225() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0225", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		LabeledStatement labeledStatement = (LabeledStatement) getASTNode((CompilationUnit) result, 0, 0, 0);
		checkSourceRange(labeledStatement.getLabel(), "label", source);
		ForStatement forStatement = (ForStatement) labeledStatement.getBody();
		ContinueStatement statement = (ContinueStatement) ((Block) forStatement.getBody()).statements().get(0);
		assertNotNull("Expression should not be null", statement); //$NON-NLS-1$
		ContinueStatement continueStatement = this.ast.newContinueStatement();
		continueStatement.setLabel(this.ast.newSimpleName("label"));
		assertTrue("Both AST trees should be identical", continueStatement.subtreeMatch(new ASTMatcher(), statement));		//$NON-NLS-1$
		checkSourceRange(statement, "continue label", source); //$NON-NLS-1$
		checkSourceRange(statement.getLabel(), "label", source);
	}
		
	/**
	 * Break + label  ==> BreakStatement
	 */
	public void test0226() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0226", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		LabeledStatement labeledStatement = (LabeledStatement) getASTNode((CompilationUnit) result, 0, 0, 0);
		checkSourceRange(labeledStatement.getLabel(), "label", source);
		ForStatement forStatement = (ForStatement) labeledStatement.getBody();
		BreakStatement statement = (BreakStatement) ((Block) forStatement.getBody()).statements().get(0);
		assertNotNull("Expression should not be null", statement); //$NON-NLS-1$
		BreakStatement breakStatement = this.ast.newBreakStatement();
		breakStatement.setLabel(this.ast.newSimpleName("label")); //$NON-NLS-1$
		assertTrue("Both AST trees should be identical", breakStatement.subtreeMatch(new ASTMatcher(), statement));		//$NON-NLS-1$
		checkSourceRange(statement, "break label", source); //$NON-NLS-1$
		checkSourceRange(statement.getLabel(), "label", source);
	}

	/**
	 * QualifiedName
	 */
	public void test0227() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0227", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 3, 2, 0);
		assertTrue("ReturnStatement", node2 instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node2;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a qualifiedName", expr instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) expr;
		ITypeBinding typeBinding = expr.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Not an long (typeBinding)", "long", typeBinding.getName());
		checkSourceRange(qualifiedName, "field.fB.fA.j", source);

		SimpleName simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "j", source);
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();
		assertEquals("Not an long (typeBinding2)", "long", typeBinding2.getName());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("VariableBinding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Not A", "A", variableBinding.getDeclaringClass().getName());
		assertEquals("Not default", Modifier.NONE, variableBinding.getModifiers());
		assertEquals("wrong name", "j", variableBinding.getName());

		Name qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", qualifierName.isQualifiedName());
		checkSourceRange(qualifierName, "field.fB.fA", source);
		qualifiedName = (QualifiedName) qualifierName;
		ITypeBinding typeBinding3 = qualifiedName.resolveTypeBinding();
		assertNotNull("No type binding3", typeBinding3);
		assertEquals("Not an A", "A", typeBinding3.getName());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "fA", source);
		ITypeBinding typeBinding4 = simpleName.resolveTypeBinding();
		assertNotNull("No typeBinding4", typeBinding4);
		assertEquals("Not an A", "A", typeBinding4.getName());
		IBinding binding2 = qualifiedName.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertTrue("VariableBinding", binding2 instanceof IVariableBinding);
		IVariableBinding variableBinding2 = (IVariableBinding) binding2;
		assertEquals("Not B", "B", variableBinding2.getDeclaringClass().getName());
		assertEquals("Not default", Modifier.NONE, variableBinding2.getModifiers());
		assertEquals("wrong name", "fA", variableBinding2.getName());
		
		qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", qualifierName.isQualifiedName());
		checkSourceRange(qualifierName, "field.fB", source);
		qualifiedName = (QualifiedName) qualifierName;
		ITypeBinding typeBinding5 = qualifiedName.resolveTypeBinding();
		assertNotNull("No typeBinding5", typeBinding5);
		assertEquals("Not a B", "B", typeBinding5.getName());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "fB", source);
		ITypeBinding typeBinding6 = simpleName.resolveTypeBinding();
		assertNotNull("No typebinding6", typeBinding6);
		assertEquals("not a B", "B", typeBinding6.getName());
		IBinding binding3 = qualifiedName.resolveBinding();
		assertNotNull("No binding2", binding3);
		assertTrue("VariableBinding", binding3 instanceof IVariableBinding);
		IVariableBinding variableBinding3 = (IVariableBinding) binding3;
		assertEquals("Not C", "C", variableBinding3.getDeclaringClass().getName());
		assertEquals("Not default", Modifier.NONE, variableBinding3.getModifiers());
		assertEquals("wrong name", "fB", variableBinding3.getName());
		
		qualifierName = qualifiedName.getQualifier();
		assertTrue("Not a simple name", qualifierName.isSimpleName());
		checkSourceRange(qualifierName, "field", source);
		simpleName = (SimpleName) qualifierName;
		ITypeBinding typeBinding7 = simpleName.resolveTypeBinding();
		assertNotNull("No typeBinding7", typeBinding7);
		assertEquals("Not a C", "C", typeBinding7.getName());
		IBinding binding4 = simpleName.resolveBinding();
		assertNotNull("No binding4", binding4);
		assertTrue("VariableBinding", binding4 instanceof IVariableBinding);
		IVariableBinding variableBinding4 = (IVariableBinding) binding4;
		assertEquals("Not Test", "Test", variableBinding4.getDeclaringClass().getName());
		assertEquals("Not public", Modifier.PUBLIC, variableBinding4.getModifiers());
		assertEquals("wrong name", "field", variableBinding4.getName());
		assertEquals("wrong return type", "C", variableBinding4.getType().getName());
	}

	/**
	 * QualifiedName as TypeReference
	 */
	public void test0228() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0228", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("ReturnStatement", node2 instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node2;
		Expression expr = returnStatement.getExpression();
		checkSourceRange(expr, "test0228.Test.foo()", source);
		assertTrue("MethodInvocation", expr instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		Expression qualifier = methodInvocation.getExpression();
		assertNotNull("no qualifier", qualifier);
		assertTrue("QualifiedName", qualifier instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) qualifier;
		checkSourceRange(qualifiedName, "test0228.Test", source);
		ITypeBinding typeBinding = qualifiedName.resolveTypeBinding();
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong type", "Test", typeBinding.getName());
		IBinding binding = qualifiedName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Not a type", IBinding.TYPE, binding.getKind());
		
	}

	/**
	 * MethodInvocation
	 */
	public void test0229() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0229", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expr = expressionStatement.getExpression();
		assertTrue("MethodInvocation", expr instanceof MethodInvocation);
		checkSourceRange(expr, "System.err.println()", source);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		Expression qualifier = methodInvocation.getExpression();
		assertTrue("QualifiedName", qualifier instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) qualifier;
		ITypeBinding typeBinding = qualifier.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "PrintStream", typeBinding.getName());
		IBinding binding = qualifiedName.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("VariableBinding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("wrong name", "err", variableBinding.getName());
		SimpleName methodName = methodInvocation.getName();
		IBinding binding2 = methodName.resolveBinding();
		assertNotNull("No binding2", binding2);
	}
	
	/**
	 * MethodInvocation
	 */
	public void test0230() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0230", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expr = expressionStatement.getExpression();
		assertTrue("MethodInvocation", expr instanceof MethodInvocation);
		checkSourceRange(expr, "err.println()", source);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		Expression qualifier = methodInvocation.getExpression();
		assertTrue("SimpleName", qualifier instanceof SimpleName);
		SimpleName name = (SimpleName) qualifier;
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong name", "err", binding.getName());
		ITypeBinding typeBinding = name.resolveTypeBinding();
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wron type name", "PrintStream", typeBinding.getName());
	}
	
	/**
	 * MethodInvocation
	 */
	public void test0231() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0231", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("ExpressionStatement", node2 instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expr = expressionStatement.getExpression();
		assertTrue("MethodInvocation", expr instanceof MethodInvocation);
		checkSourceRange(expr, "System.err.println()", source);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		Expression qualifier = methodInvocation.getExpression();
		assertTrue("QualifiedName", qualifier instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) qualifier;
		ITypeBinding typeBinding = qualifier.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "PrintStream", typeBinding.getName());
		IBinding binding = qualifiedName.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("VariableBinding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("wrong name", "err", variableBinding.getName());
		SimpleName methodName = methodInvocation.getName();
		IBinding binding2 = methodName.resolveBinding();
		assertNotNull("No binding2", binding2);
		Name name = qualifiedName.getQualifier();
		assertTrue("SimpleName", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();
		assertNotNull("No typeBinding2", typeBinding2);
		assertEquals("wrong type name", "System", typeBinding2.getName());
	}

	/**
	 * MethodInvocation
	 */
	public void test0232() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0232", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node2 = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("VariableDeclarationStatement", node2 instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node2;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initialization = variableDeclarationFragment.getInitializer();
		ITypeBinding typeBinding = initialization.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not a primitive type", typeBinding.isPrimitive());
		assertEquals("wrong name", "int", typeBinding.getName());
		assertTrue("QualifiedName", initialization instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) initialization;
		SimpleName simpleName = qualifiedName.getName();
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding2);
		assertTrue("Not a primitive type", typeBinding2.isPrimitive());
		assertEquals("wrong name", "int", typeBinding2.getName());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("IVariableBinding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertNull("No declaring class", variableBinding.getDeclaringClass());
	}
	
	/**
	 * Checking that only syntax errors are reported for the MALFORMED tag
	 */
	public void test0233() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0233", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("Expression should not be null", result); //$NON-NLS-1$
		assertTrue("The compilation unit is malformed", !isMalformed(result));
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("The fiels is not malformed", !isMalformed(node));
		assertEquals("No problem found", 1, unit.getMessages().length);
	}

	/**
	 * Checking that null is returned for a resolveBinding if the type is unknown
	 */
	public void test0234() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0234", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("The fiels is not malformed", !isMalformed(node));
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("No problem found", 1, unit.getMessages().length);
		assertTrue("FieldDeclaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNull("binding not null", variableBinding);
	}

	/**
	 * Checking that null is returned for a resolveBinding if the type is unknown
	 */
	public void test0235() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0235", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("The fiels is not malformed", !isMalformed(node));
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("problems found", 0, unit.getMessages().length);
		assertTrue("FieldDeclaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
	}

	/**
	 * Test the removal of a IField inside a CU that has an initializer
	 */
	public void test0236() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0236", "Test.java");
		IType type = sourceUnit.getType("Test");
		assertNotNull("No type", type);
		IField field = type.getField("i");
		assertNotNull("No field", field);
		field.delete(true, null);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=9452
	 */
	public void test0237() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "junit.framework", "TestCase.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
	}
		
	/**
	 * Check ThisExpression
	 */
	public void test0238() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0238", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a type declaration statement", node instanceof TypeDeclarationStatement);
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node;
		TypeDeclaration typeDecl = typeDeclarationStatement.getTypeDeclaration();
		Object o = typeDecl.bodyDeclarations().get(0);
		assertTrue("Not a method", o instanceof MethodDeclaration);
		MethodDeclaration methodDecl = (MethodDeclaration) o;
		Block block = methodDecl.getBody();
		List statements = block.statements();
		assertEquals("Not 1", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		assertTrue("Not a return statement", stmt instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) stmt;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a method invocation", expr instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		checkSourceRange(methodInvocation, "Test.this.bar()", source);
		Expression qualifier = methodInvocation.getExpression();
		assertTrue("Not a ThisExpression", qualifier instanceof ThisExpression);
		ThisExpression thisExpression = (ThisExpression) qualifier;
		Name name = thisExpression.getQualifier();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong name", "Test", binding.getName());
	}

	/**
	 * Check ThisExpression
	 */
	public void test0239() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0239", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 0);
		assertTrue("Not a type declaration statement", node instanceof TypeDeclarationStatement);
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node;
		TypeDeclaration typeDecl = typeDeclarationStatement.getTypeDeclaration();
		Object o = typeDecl.bodyDeclarations().get(0);
		assertTrue("Not a method", o instanceof MethodDeclaration);
		MethodDeclaration methodDecl = (MethodDeclaration) o;
		Block block = methodDecl.getBody();
		List statements = block.statements();
		assertEquals("Not 1", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		assertTrue("Not a return statement", stmt instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) stmt;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a SuperMethodInvocation", expr instanceof SuperMethodInvocation);
		SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expr;
		Name name = superMethodInvocation.getQualifier();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("A type binding", binding instanceof ITypeBinding);
		assertEquals("Not Test", "Test", binding.getName());
		Name methodName = superMethodInvocation.getName();
		IBinding binding2 = methodName.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertTrue("No an IMethodBinding", binding2 instanceof IMethodBinding);
		IMethodBinding methodBinding = (IMethodBinding) binding2;
		assertEquals("Not bar", "bar", methodBinding.getName());
		assertEquals("Not T", "T", methodBinding.getDeclaringClass().getName());
	}
	
	/**
	 * Check FieldAccess
	 */
	public void test0240() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0240", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a type declaration statement", node instanceof TypeDeclarationStatement);
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node;
		TypeDeclaration typeDecl = typeDeclarationStatement.getTypeDeclaration();
		Object o = typeDecl.bodyDeclarations().get(0);
		assertTrue("Not a method", o instanceof MethodDeclaration);
		MethodDeclaration methodDecl = (MethodDeclaration) o;
		Block block = methodDecl.getBody();
		List statements = block.statements();
		assertEquals("Not 1", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		assertTrue("Not a return statement", stmt instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) stmt;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a field access", expr instanceof FieldAccess);
		FieldAccess fieldAccess = (FieldAccess) expr;
		Expression qualifier = fieldAccess.getExpression();
		assertTrue("Not a ThisExpression", qualifier instanceof ThisExpression);
		ThisExpression thisExpression = (ThisExpression) qualifier;
		Name name = thisExpression.getQualifier();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Not Test", "Test", binding.getName());
		Name fieldName = fieldAccess.getName();
		IBinding binding2 = fieldName.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertEquals("Wrong name", "f", binding2.getName());
		assertEquals("Wrong modifier", Modifier.PUBLIC, binding2.getModifiers());
		ITypeBinding typeBinding = fieldName.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Not int", "int", typeBinding.getName());
	}

	/**
	 * Check order of body declarations
	 */
	public void test0241() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0241", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assertTrue("Not a type declaration", node instanceof TypeDeclaration);
		assertTrue("Not a declaration", ((TypeDeclaration) node).getName().isDeclaration());
		assertEquals("Wrong size", 11, ((TypeDeclaration)node).bodyDeclarations().size());
		node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a field declaration", node instanceof FieldDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a MethodDeclaration", node instanceof MethodDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not a Type declaration", node instanceof TypeDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 3);
		assertTrue("Not a Type declaration", node instanceof TypeDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 4);
		assertTrue("Not a MethodDeclaration", node instanceof MethodDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 5);
		assertTrue("Not a field declaration", node instanceof FieldDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 6);
		assertTrue("Not a MethodDeclaration", node instanceof MethodDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 7);
		assertTrue("Not a field declaration", node instanceof FieldDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 8);
		assertTrue("Not a field declaration", node instanceof FieldDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 9);
		assertTrue("Not a MethodDeclaration", node instanceof MethodDeclaration);
		node = getASTNode((CompilationUnit) result, 0, 10);
		assertTrue("Not a Type declaration", node instanceof TypeDeclaration);
	}

	/**
	 * Check ThisExpression
	 */
	public void test0242() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0242", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 0);
		assertTrue("Not a type declaration statement", node instanceof TypeDeclarationStatement);
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node;
		TypeDeclaration typeDecl = typeDeclarationStatement.getTypeDeclaration();
		Object o = typeDecl.bodyDeclarations().get(0);
		assertTrue("Not a method", o instanceof MethodDeclaration);
		MethodDeclaration methodDecl = (MethodDeclaration) o;
		Block block = methodDecl.getBody();
		List statements = block.statements();
		assertEquals("Not 1", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		assertTrue("Not a return statement", stmt instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) stmt;
		Expression expr = returnStatement.getExpression();
		assertTrue("Not a SuperFieldAccess", expr instanceof SuperFieldAccess);
		SuperFieldAccess superFieldAccess = (SuperFieldAccess) expr;
		Name name = superFieldAccess.getQualifier();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("A type binding", binding instanceof ITypeBinding);
		assertEquals("Not Test", "Test", binding.getName());
		Name fieldName = superFieldAccess.getName();
		IBinding binding2 = fieldName.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertTrue("No an IVariableBinding", binding2 instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding2;
		assertEquals("Not f", "f", variableBinding.getName());
		assertEquals("Not T", "T", variableBinding.getDeclaringClass().getName());
		ITypeBinding typeBinding2 = fieldName.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding2);
		assertEquals("Not int", "int", typeBinding2.getName());
	}

	/**
	 * Check catch clause positions:
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10570
	 */
	public void test0243() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0243", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a try statement", node instanceof TryStatement);
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		assertEquals("wrong size", 1, catchClauses.size());
		CatchClause catchClause = (CatchClause) catchClauses.get(0);
		checkSourceRange(catchClause, "catch (Exception e){m();}", source);
	}

	/**
	 * Check catch clause positions:
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10570
	 */
	public void test0244() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0244", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a try statement", node instanceof TryStatement);
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		assertEquals("wrong size", 2, catchClauses.size());
		CatchClause catchClause = (CatchClause) catchClauses.get(0);
		checkSourceRange(catchClause, "catch (RuntimeException e){m();}", source);
		catchClause = (CatchClause) catchClauses.get(1);
		checkSourceRange(catchClause, "catch(Exception e) {}", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=10587
	 */
	public void test0245() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0245", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expr = returnStatement.getExpression();
		assertTrue("not a name", expr instanceof Name);
		Name name = (Name) expr;
		IBinding binding = name.resolveBinding();
		assertTrue("Not a variable binding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Not i", "i", variableBinding.getName());
		assertEquals("Not int", "int", variableBinding.getType().getName());
		ASTNode declaringNode = unit.findDeclaringNode(variableBinding);
		assertNotNull("No declaring node", declaringNode);
		assertTrue("Not a VariableDeclarationFragment", declaringNode instanceof VariableDeclarationFragment);
	}
	
	/**
	 * Test binding resolution for import declaration
	 */
	public void test0246() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0246", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		List imports = unit.imports();
		assertEquals("wrong imports size", 2, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		assertTrue("Not on demand", importDeclaration.isOnDemand());
		checkSourceRange(importDeclaration, "import java.util.*;", source);
		IBinding binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.PACKAGE, binding.getKind());
		assertEquals("Wrong name", "java.util", binding.getName());
		importDeclaration = (ImportDeclaration) imports.get(1);
		assertTrue("On demand", !importDeclaration.isOnDemand());
		checkSourceRange(importDeclaration, "import java.io.IOException;", source);
		binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("Wrong name", "IOException", binding.getName());
	}

	/**
	 * Test binding resolution for import declaration
	 */
	public void test0247() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0247", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		PackageDeclaration packageDeclaration = unit.getPackage();
		checkSourceRange(packageDeclaration, "package test0247;", source);
		IPackageBinding binding = packageDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.PACKAGE, binding.getKind());
		assertEquals("Wrong name", "test0247", binding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10592
	 */
	public void test0248() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0248", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);		
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Name name = singleVariableDeclaration.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("Not a variable binding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "i", variableBinding.getName());
		assertEquals("Wrong type", "int", variableBinding.getType().getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10592
	 */
	public void test0249() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0249", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 2, 1);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not an assignment", expression instanceof Assignment);
		Assignment assignment = (Assignment) expression;
		Expression leftHandSide = assignment.getLeftHandSide();
		assertTrue("Not a qualified name", leftHandSide instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) leftHandSide;
		Name simpleName = qualifiedName.getName();
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("no binding", binding);
		assertTrue("Not a IVariableBinding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "k", variableBinding.getName());
		assertEquals("Wrong modifier", Modifier.STATIC, variableBinding.getModifiers());
		assertEquals("Wrong type", "int", variableBinding.getType().getName());
		assertEquals("Wrong declaring class name", "j", variableBinding.getDeclaringClass().getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10592
	 */
	public void test0250() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0250", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);		
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 2, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Name name = singleVariableDeclaration.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("Not a variable binding", binding instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "i", variableBinding.getName());
		assertEquals("Wrong type", "int", variableBinding.getType().getName());
	}
		
	/**
	 * Check qualified name resolution for static fields
	 */
	public void test0251() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0251", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		checkSourceRange(methodInvocation, "java.lang.System.out.println()", source);
		Expression qualifier = methodInvocation.getExpression();
		assertTrue("Not a qualified name", qualifier instanceof QualifiedName);
		checkSourceRange(qualifier, "java.lang.System.out", source);
		QualifiedName qualifiedName = (QualifiedName) qualifier;
		Name typeName = qualifiedName.getQualifier();
		assertTrue("Not a QualifiedName", typeName instanceof QualifiedName);
		QualifiedName qualifiedTypeName = (QualifiedName) typeName;
		IBinding binding = qualifiedTypeName.getName().resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong name", "System", binding.getName());
		binding = qualifiedTypeName.getQualifier().resolveBinding();
		assertNotNull("No binding2", binding);
		assertEquals("Wrong type binding", IBinding.PACKAGE, binding.getKind());
	}
		
	/**
	 * Check binding for anonymous class
	 */
	public void test0252() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0252", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("Not a classinstancecreation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No methodBinding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		assertTrue("Not an anonymous class", methodBinding.getDeclaringClass().isAnonymous());
		assertEquals("Not an anonymous class of java.lang.Object", "Object", methodBinding.getDeclaringClass().getSuperclass().getName());
		assertEquals("Not an anonymous class of java.lang.Object", "java.lang", methodBinding.getDeclaringClass().getSuperclass().getPackage().getName());
	}

	/**
	 * Check binding for allocation expression
	 */
	public void test0253() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0253", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("Not a classinstancecreation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No methodBinding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		assertEquals("Wrong size", 1, methodBinding.getParameterTypes().length);
		assertEquals("Wrong type", "String", methodBinding.getParameterTypes()[0].getName());
	}

	/**
	 * Check binding for allocation expression
	 */
	public void test0254() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0254", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 1, 0);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("Not a class instance creation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", "C", binding.getDeclaringClass().getName());
	}


	/**
	 * Check binding for allocation expression
	 */
	public void test0255() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0255", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a MethodInvocation", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size());
		Expression expression2 = (Expression) arguments.get(0);
		assertTrue("Not a CastExpression", expression2 instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression2;
		Type type = castExpression.getType();
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("Not an array type", binding.isArray());
	}

	/**
	 * Check binding for allocation expression
	 */
	public void test0256() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0256", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a MethodInvocation", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size());
		Expression expression2 = (Expression) arguments.get(0);
		assertTrue("Not a CastExpression", expression2 instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression2;
		Type type = castExpression.getType();
		assertTrue("Not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("Not a class", binding.isClass());
		Name name = simpleType.getName();
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertEquals("Wrong type", "Object", binding2.getName());
	}

	/**
	 * Check binding for allocation expression
	 */
	public void test0257() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0257", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a MethodInvocation", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size());
		Expression expression2 = (Expression) arguments.get(0);
		assertTrue("Not a CastExpression", expression2 instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression2;
		Type type = castExpression.getType();
		assertTrue("Not a primitive type", type.isPrimitiveType());
		PrimitiveType primitiveType = (PrimitiveType) type;
		assertEquals("Not int", PrimitiveType.INT, primitiveType.getPrimitiveTypeCode());
	}

	/**
	 * Check binding for allocation expression
	 */
	public void test0258() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0258", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a MethodInvocation", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size());
		Expression expression2 = (Expression) arguments.get(0);
		assertTrue("Not a CastExpression", expression2 instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression2;
		Type type = castExpression.getType();
		assertTrue("Not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("Not a class", binding.isClass());
		Name name = simpleType.getName();
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertEquals("Wrong type", "Object", binding2.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10663
	 */
	public void test0259() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0259", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10592
	 */
	public void test0260() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0260", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);		
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 2, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		IBinding binding = singleVariableDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		Name name = singleVariableDeclaration.getName();
		assertTrue("Not a simple name", name instanceof SimpleName);
		SimpleName simpleName = (SimpleName) name;
		assertEquals("Wrong name", "i", simpleName.getIdentifier());
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding", binding2);
		assertTrue("binding == binding2", binding == binding2);
		assertTrue("Not a variable binding", binding2 instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) binding2;
		assertEquals("Wrong name", "i", variableBinding.getName());
		assertEquals("Wrong type", "int", variableBinding.getType().getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10679
	 */
	public void test0261() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0261", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("Wrong size", 1, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		ITypeBinding binding = expression.resolveTypeBinding();
		assertNull("got a binding", binding);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10676
	 */
	public void test0262() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0262", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expr = expressionStatement.getExpression();
		assertTrue("Not a MethodInvocation", expr instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong argument list size", 1, arguments.size());
		Expression expr2 = (Expression) arguments.get(0);
		assertTrue("Not a class instance creation", expr2 instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expr2;
		arguments = classInstanceCreation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		Expression expression2 = (Expression) arguments.get(0);
		assertTrue("Not a string literal", expression2 instanceof StringLiteral);
		StringLiteral stringLiteral = (StringLiteral) expression2;
		ITypeBinding typeBinding = stringLiteral.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "String", typeBinding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10700
	 */
	public void test0263() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0263", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expr = expressionStatement.getExpression();
		assertTrue("Not a MethodInvocation", expr instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expr;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong argument list size", 1, arguments.size());
		Expression expr2 = (Expression) arguments.get(0);
		assertTrue("Not a simple name", expr2 instanceof SimpleName);
		SimpleName simpleName = (SimpleName) expr2;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10699
	 */
	public void test0264() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0264", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("Wrong fragment size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a classinstancecreation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		assertNotNull("No anonymousclassdeclaration", anonymousClassDeclaration);
		String expectedSourceRange = 
			"{\r\n"+ 
			"			void m(int k){\r\n"+
			"				k= i;\r\n"+
			"			}\r\n"+
			"		}";
		checkSourceRange(anonymousClassDeclaration, expectedSourceRange, source);
		List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		assertEquals("Wrong name", "m", methodDeclaration.getName().getIdentifier());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10698
	 */
	public void test0265() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0265", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
	 */
	public void test0266() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0266", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "Inner\\u005b]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		Type type2 = arrayType.getElementType();
		assertTrue("Not a simple type", type2.isSimpleType());
		SimpleType simpleType = (SimpleType) type2;
		checkSourceRange(simpleType, "Inner", source);
		Name name = simpleType.getName();
		assertTrue("not a simple name", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "Inner", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
	 */
	public void test0267() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0267", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "Inner[]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		Type type2 = arrayType.getElementType();
		assertTrue("Not a simple type", type2.isSimpleType());
		SimpleType simpleType = (SimpleType) type2;
		checkSourceRange(simpleType, "Inner", source);
		Name name = simpleType.getName();
		assertTrue("not a simple name", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "Inner", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
	 */
	public void test0268() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0268", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "test0268.Test.Inner[]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		Type type2 = arrayType.getElementType();
		assertTrue("Not a simple type", type2.isSimpleType());
		SimpleType simpleType = (SimpleType) type2;
		checkSourceRange(simpleType, "test0268.Test.Inner", source);
		Name name = simpleType.getName();
		assertTrue("not a qualified name", name.isQualifiedName());
		checkSourceRange(name, "test0268.Test.Inner", source);
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
	 */
	public void test0269() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0269", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "test0269.Test.Inner[/**/]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		Type type2 = arrayType.getElementType();
		assertTrue("Not a simple type", type2.isSimpleType());
		SimpleType simpleType = (SimpleType) type2;
		checkSourceRange(simpleType, "test0269.Test.Inner", source);
		Name name = simpleType.getName();
		assertTrue("not a qualified name", name.isQualifiedName());
		checkSourceRange(name, "test0269.Test.Inner", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
	 */
	public void test0270() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0270", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "test0270.Test.Inner", source);
		assertTrue("Not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("not a qualified name", name.isQualifiedName());
		checkSourceRange(name, "test0270.Test.Inner", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
	 */
	public void test0271() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0271", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "test0271.Test.Inner[]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		Type type2 = arrayType.getElementType();
		assertTrue("Not a simple type", type2.isSimpleType());
		SimpleType simpleType = (SimpleType) type2;
		checkSourceRange(simpleType, "test0271.Test.Inner", source);
		Name name = simpleType.getName();
		assertTrue("not a qualified name", name.isQualifiedName());
		checkSourceRange(name, "test0271.Test.Inner", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10843
	 */
	public void test0272() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0272", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a For statement", node instanceof ForStatement);
		ForStatement forStatement = (ForStatement) node;
		checkSourceRange(forStatement, "for (int i= 0; i < 10; i++) foo();", source);
		Statement action = forStatement.getBody();
		checkSourceRange(action, "foo();", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10843
	 */
	public void test0273() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0273", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a For statement", node instanceof ForStatement);
		ForStatement forStatement = (ForStatement) node;
		checkSourceRange(forStatement, "for (int i= 0; i < 10; i++) { foo(); }", source);
		Statement action = forStatement.getBody();
		checkSourceRange(action, "{ foo(); }", source);
		assertTrue("Not a block", action instanceof Block);
		Block block = (Block) action;
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		checkSourceRange(stmt, "foo();", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10843
	 */
	public void test0274() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0274", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not a While statement", node instanceof WhileStatement);
		WhileStatement whileStatement = (WhileStatement) node;
		checkSourceRange(whileStatement, "while (i < 10) { foo(i++); }", source);
		Statement action = whileStatement.getBody();
		checkSourceRange(action, "{ foo(i++); }", source);
		assertTrue("Not a block", action instanceof Block);
		Block block = (Block) action;
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		checkSourceRange(stmt, "foo(i++);", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10843
	 */
	public void test0275() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0275", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not a While statement", node instanceof WhileStatement);
		WhileStatement whileStatement = (WhileStatement) node;
		checkSourceRange(whileStatement, "while (i < 10) foo(i++);", source);
		Statement action = whileStatement.getBody();
		checkSourceRange(action, "foo(i++);", source);
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10798
	 */
	public void test0276() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0276", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		String expectedSource = 
			"public void foo() {\r\n" +
			"		foo();\r\n" +
			"	}";
		checkSourceRange(methodDeclaration, expectedSource, source);
		expectedSource = 
			"{\r\n" +
			"		foo();\r\n" +
			"	}";		
		checkSourceRange(methodDeclaration.getBody(), expectedSource, source);
	}
		
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10798
	 */
	public void test0277() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0277", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		String expectedSource = 
			"public void foo() {\r\n" +
			"	}";
		checkSourceRange(methodDeclaration, expectedSource, source);
		expectedSource = 
			"{\r\n" +
			"	}";		
		checkSourceRange(methodDeclaration.getBody(), expectedSource, source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10861
	 */
	public void test0278() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0278", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "Class c = java.lang.String.class;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a type literal", expression instanceof TypeLiteral);
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "Class", typeBinding.getName());
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10861
	 */
	public void test0279() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0279", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0,0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		checkSourceRange(variableDeclarationStatement, "Class c = java.lang.String.class;", source);
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a type literal", expression instanceof TypeLiteral);
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "Class", typeBinding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10865
	 * Check well known types
	 */
	public void test0280() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0280", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		AST ast = result.getAST();
		ITypeBinding typeBinding = ast.resolveWellKnownType("boolean");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "boolean", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("char");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "char", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("byte");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "byte", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("short");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "short", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("int");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "int", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("long");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "long", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("float");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "float", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("double");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "double", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("void");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "void", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.Object");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "Object", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.String");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "String", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.StringBuffer");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "StringBuffer", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.Throwable");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "Throwable", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.Exception");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "Exception", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.RuntimeException");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "RuntimeException", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.Error");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "Error", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.Class");
		assertNotNull("No typeBinding", typeBinding);
		assertEquals("Wrong name", "Class", typeBinding.getName());
		typeBinding = ast.resolveWellKnownType("java.lang.Runnable");
		assertNull("typeBinding not null", typeBinding);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0281() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0281", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "Object o= /*]*/new Object()/*[*/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "new Object()", source);
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0282() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0282", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "boolean b = /*]*/true/*[*/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "true", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0283() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0283", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "char c = /*]*/'c'/*[*/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "'c'", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0284() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0284", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "Object o = /*]*/null/*[*/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "null", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0285() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0285", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "Object o = /*]*/Object.class/*[*/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "Object.class", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0286() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0286", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "int i = /**/(2)/**/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "(2)", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0287() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0287", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "String[] tab = /**/new String[3]/**/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "new String[3]", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0288() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0288", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "String[] tab = /**/{ }/**/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "{ }", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0289() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0289", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "String s = /**/tab1[0]/**/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "tab1[0]", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 */
	public void test0290() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0290", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "Object o = /*]*/new java.lang.Object()/*[*/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "new java.lang.Object()", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10898
	 */
	public void test0291() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0291", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("no errors", 1, unit.getMessages().length);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10913
	 */
	public void test0292() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0292", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("Not a qualifiedName", expression instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) expression;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("Wrong name", "x", simpleName.getIdentifier());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("NO binding", binding);
		assertTrue("Not a variable binding", binding instanceof IVariableBinding);
		assertEquals("wrong name", "x", binding.getName());
		Name name = qualifiedName.getQualifier();
		assertTrue("Not a simpleName", name instanceof SimpleName);
		SimpleName simpleName2 = (SimpleName) name;
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertTrue("Not a type binding", binding2 instanceof ITypeBinding);
		assertEquals("Wrong name", "Test", binding2.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10933
 	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10935
	 */
	public void test0293() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0293", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a class instance creation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		assertNotNull("No body", anonymousClassDeclaration);
		String expectedSource = 
				"{\r\n" +
				"			public void run() {\r\n" +
				"				/*]*/foo();/*[*/\r\n" +
				"			}\r\n" +
				"		}";
		checkSourceRange(anonymousClassDeclaration, expectedSource, source);
		expectedSource =
				"run= new Runnable() {\r\n" +
				"			public void run() {\r\n" +
				"				/*]*/foo();/*[*/\r\n" +
				"			}\r\n" +
				"		}";
		checkSourceRange(variableDeclarationFragment, expectedSource, source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10984
	 */
	public void test0294() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0294", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		String expectedSource = 
				"public void fails() {\r\n" +
				"		foo()\r\n" +
				"	}";
		checkSourceRange(methodDeclaration, expectedSource, source);
		Block block = methodDeclaration.getBody();
		expectedSource = 
				"{\r\n" +
				"		foo()\r\n" +
				"	}";
		checkSourceRange(block, expectedSource, source);	
		node = getASTNode(compilationUnit, 0, 1);	
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10986
	 */
	public void test0295() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0295", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("Wrong size", 2, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("not a method invocation", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		ITypeBinding typeBinding = methodInvocation.resolveTypeBinding();
		assertNull("type binding is not null", typeBinding);
	}


	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10984
	 */
	public void test0296() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0296", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		String expectedSource = 
				"public void fails() {\r\n" +
				"		foo()\r\n" +
				"	}";
		checkSourceRange(methodDeclaration, expectedSource, source);
		Block block = methodDeclaration.getBody();
		expectedSource = 
				"{\r\n" +
				"		foo()\r\n" +
				"	}";
		checkSourceRange(block, expectedSource, source);	
		node = getASTNode(compilationUnit, 0, 1);	
		assertTrue("Not a method declaration", node instanceof MethodDeclaration);
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11037
	 */
	public void test0297() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0297", "Test.java");
		runConversion(sourceUnit, false);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10984
	 */
	public void test0298() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0298", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a ReturnStatement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		checkSourceRange(expression, "a().length != 3", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11104
	 */
	public void test0299() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0299", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "int i = (/**/2/**/);", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a parenthesized expression", expression instanceof ParenthesizedExpression);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
		Expression expression2 = parenthesizedExpression.getExpression();
		checkSourceRange(expression2, "2", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11104
	 */
	public void test0300() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0300", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "boolean b = /**/true/**/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "true", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10874
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11104
	 */
	public void test0301() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0301", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a Field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		checkSourceRange(fieldDeclaration, "Object o = /**/null/**/;", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		checkSourceRange(expression, "null", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11106
	 */
	public void test0302() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0302", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a DoStatement", node instanceof DoStatement);
		DoStatement doStatement = (DoStatement) node;
		String expectedSource = 
				"do\r\n" +  
				"			foo();\r\n" + 
				"		while(1 < 10);";
		checkSourceRange(doStatement, expectedSource, source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11129
	 */
	public void test0303() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0303", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression2 = expressionStatement.getExpression();
		assertTrue("Not an Assignement", expression2 instanceof Assignment);
		Assignment assignment = (Assignment) expression2;
		Expression expression = assignment.getRightHandSide();
		assertTrue("Not a CastExpression", expression instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression;
		ITypeBinding typeBinding = castExpression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "char", typeBinding.getName());
		Type type = castExpression.getType();
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertNotNull("No binding2", typeBinding2);
		assertEquals("Wrong name", "char", typeBinding2.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11151
	 */
	public void test0304() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0304", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("not a method declaration", node instanceof MethodDeclaration);
		checkSourceRange(node, "public void foo(int arg);", source);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		assertNull("Has a body", block);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11125
	 */
	public void test0305() throws JavaModelException {
		char[] source = 
				("package test0304;\r\n" + 
				"\r\n" + 
				"class Test {\r\n" + 
				"	public void foo(int arg) {}\r\n" + 
				"}").toCharArray();
		IJavaProject project = getJavaProject("Converter");
		ASTNode result = runConversion(source, "Test.java", project);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("not a TypeDeclaration", node instanceof TypeDeclaration);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "Test", typeBinding.getName());
		assertEquals("Wrong package", "test0304", typeBinding.getPackage().getName());
		assertTrue("Not an interface", typeBinding.isClass());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11125
	 */
	public void test0306() throws JavaModelException {
		char[] source = 
				("package java.lang;\r\n" + 
				"\r\n" + 
				"class Object {\r\n" + 
				"	public void foo(int arg) {}\r\n" + 
				"}").toCharArray();
		IJavaProject project = getJavaProject("Converter");
		ASTNode result = runConversion(source, "Object.java", project);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("not a TypeDeclaration", node instanceof TypeDeclaration);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "Object", typeBinding.getName());
		assertEquals("Wrong package", "java.lang", typeBinding.getPackage().getName());
		assertTrue("Not an interface", typeBinding.isClass());
		assertEquals("Wrong size", 2, typeBinding.getDeclaredMethods().length);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11371
	 */
	public void test0307() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0307", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("not a method declaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		assertNotNull("No body", block);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size());
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not a super constructor invocation", statement instanceof SuperConstructorInvocation);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11371
	 */
	public void test0308() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0308", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("not a method declaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		assertNotNull("No body", block);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size());
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not a super constructor invocation", statement instanceof SuperConstructorInvocation);
		SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation) statement;
		IMethodBinding methodBinding = superConstructorInvocation.resolveConstructorBinding();
		assertNotNull("No methodBinding", methodBinding);
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();
		assertNotNull("No methodBinding2", methodBinding2);
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11380
	 */
	public void test0309() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0309", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a conditional expression", expression instanceof ConditionalExpression);
		ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
		ITypeBinding typeBinding = conditionalExpression.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("wrong name", "int", typeBinding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11380
	 */
	public void test0310() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0310", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("not a FieldDeclaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a qualified name", expression instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) expression;
		Name qualifier = qualifiedName.getQualifier();
		IBinding binding = qualifier.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong name", "I", binding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11638
	 */
	public void test0311() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0311", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("not a class instance creation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		assertNotNull("No body", anonymousClassDeclaration);
		List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size for body declarations", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertNotNull("no body", block);
		List statements = block.statements();
		assertEquals("Wrong size for statements", 1, statements.size());
		Statement statement = (Statement) statements.get(0);
		assertTrue("not a variable declaration statement", statement instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement2 = (VariableDeclarationStatement) statement;
		List fragments2 = variableDeclarationStatement2.fragments();
		assertEquals("wrong size for fragments2", 1, fragments2.size());
		VariableDeclarationFragment variableDeclarationFragment2 = (VariableDeclarationFragment) fragments2.get(0);
		Expression expression2 = variableDeclarationFragment2.getInitializer();
		assertTrue("Not a name", expression2 instanceof Name);
		Name name = (Name) expression2;
		checkSourceRange(name, "j", source);
		IBinding binding = name.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		assertNotNull("No declaring node", declaringNode);
		checkSourceRange(declaringNode, "int j", source);
		assertTrue("Not a single variable declaration", declaringNode instanceof SingleVariableDeclaration);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11638
	 * There is a error in this source. A is unresolved. Then there is no
	 * declaring node.
	 */
	public void test0312() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0312", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("not a class instance creation", expression instanceof ClassInstanceCreation);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		assertNotNull("No body", anonymousClassDeclaration);
		List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size for body declarations", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertNotNull("no body", block);
		List statements = block.statements();
		assertEquals("Wrong size for statements", 1, statements.size());
		Statement statement = (Statement) statements.get(0);
		assertTrue("not a variable declaration statement", statement instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement2 = (VariableDeclarationStatement) statement;
		List fragments2 = variableDeclarationStatement2.fragments();
		assertEquals("wrong size for fragments2", 1, fragments2.size());
		VariableDeclarationFragment variableDeclarationFragment2 = (VariableDeclarationFragment) fragments2.get(0);
		Expression expression2 = variableDeclarationFragment2.getInitializer();
		assertTrue("Not a name", expression2 instanceof Name);
		Name name = (Name) expression2;
		checkSourceRange(name, "j", source);
		IBinding binding = name.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		assertNull("No declaring node is available", declaringNode);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11659
	 */
	public void test0313() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0313", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not an InfixExpression", expression instanceof InfixExpression);
		InfixExpression infixExpression = (InfixExpression) expression;
		checkSourceRange(infixExpression, "i+j", source);
		Expression expression2 = infixExpression.getLeftOperand();
		checkSourceRange(expression2, "i", source);
		assertTrue("Not a name", expression2 instanceof Name);
		Name name = (Name) expression2;
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		ASTNode astNode = compilationUnit.findDeclaringNode(binding);
		assertNotNull("No declaring node", astNode);
		checkSourceRange(astNode, "int i", source);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=12326
	 */
	public void test0314() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0314", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		assertNotNull("No result", result);
		assertTrue("Not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("Wrong line number", 1, compilationUnit.lineNumber(0));
		// ensure that last character is on the last line
		assertEquals("Wrong line number", 3, compilationUnit.lineNumber(source.length - 1));
		// source.length is beyond the size of the compilation unit source
		assertEquals("Wrong line number", 1, compilationUnit.lineNumber(source.length));
	}
		
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=12326
	 */
	public void test0315() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0315", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a Return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("Not an instanceof expression", expression instanceof InstanceofExpression);
		InstanceofExpression instanceOfExpression = (InstanceofExpression) expression;
		Type rightOperand = instanceOfExpression.getRightOperand();
		assertTrue("Not a simpleType", rightOperand instanceof SimpleType);
		SimpleType simpleType = (SimpleType) rightOperand;
		Name n = simpleType.getName();
		assertTrue("Not a qualified name", n instanceof QualifiedName);
		QualifiedName name = (QualifiedName) n;
		checkSourceRange(name, "java.io.Serializable", source);
		ITypeBinding typeBinding = name.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("Wrong name", "Serializable", typeBinding.getName());
		Name qualifier = name.getQualifier();
		assertTrue("Not a qualified name", qualifier instanceof QualifiedName);
		ITypeBinding typeBinding2 = qualifier.resolveTypeBinding();
		assertNull("typebinding2 is not null", typeBinding2);
		IBinding binding = qualifier.resolveBinding();
		assertNotNull("no binding", binding);
		assertEquals("Wrong type", IBinding.PACKAGE, binding.getKind());
		IPackageBinding pBinding = (IPackageBinding) binding;
		assertEquals("Wrong name", "java.io", pBinding.getName());
	}
		
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=12454
	 */
	public void test0316() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "", "Hello.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No result", result);
		assertTrue("Not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("Wrong size", 2, compilationUnit.getMessages().length);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=12781
	 */
	public void test0317() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0317", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a return statement", node instanceof ReturnStatement);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("not an instanceof expression", expression instanceof InstanceofExpression);
		InstanceofExpression instanceOfExpression = (InstanceofExpression) expression;
		Expression left = instanceOfExpression.getLeftOperand();
		assertTrue("Not a Name", left instanceof Name);
		Name name = (Name) left;
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong name", "x", binding.getName());
		ITypeBinding typeBinding = name.resolveTypeBinding();
		assertNotNull("No typebinding", typeBinding);
		assertEquals("wrong type", "Object", typeBinding.getName());
		Type right = instanceOfExpression.getRightOperand();
		assertTrue("Not a simpleType", right instanceof SimpleType);
		SimpleType simpleType = (SimpleType) right;
		name = simpleType.getName();
		assertTrue("Not a simpleName", name instanceof SimpleName);
		SimpleName simpleName = (SimpleName) name;
		IBinding binding2 = simpleName.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertEquals("Wrong name", "Vector", binding2.getName());
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();
		assertNotNull("No typeBinding2", typeBinding2);
		assertEquals("Wrong name", "Vector", typeBinding2.getName());
	}
					
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
	 */
	public void test0318() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0318", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("No error", 1, unit.getMessages().length);
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertTrue("Not a variable declaration statement", node instanceof VariableDeclarationStatement);
		assertTrue("Not malformed", isMalformed(node));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0319() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0319", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not an array creation", expression instanceof ArrayCreation);
		ArrayCreation arrayCreation = (ArrayCreation) expression;
		ITypeBinding typeBinding = arrayCreation.resolveTypeBinding();
		assertNotNull("no type binding", typeBinding);
		assertEquals("wrong name", "Object[]", typeBinding.getName());
		ArrayType arrayType = arrayCreation.getType();
		ITypeBinding typeBinding2 = arrayType.resolveBinding();
		assertNotNull("no type binding2", typeBinding2);
		assertEquals("wrong name", "Object[]", typeBinding2.getName());
		Type type = arrayType.getElementType();
		assertTrue("Not a simple type", type instanceof SimpleType);
		SimpleType simpleType = (SimpleType) type;
		ITypeBinding typeBinding3 = simpleType.resolveBinding();
		assertNotNull("no type binding3", typeBinding3);
		assertEquals("wrong name", "Object", typeBinding3.getName());
	}
			
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0320() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0320", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "int[]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding typeBinding = arrayType.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		Type elementType = arrayType.getElementType();
		assertTrue("Not a simple type", elementType.isPrimitiveType());
		ITypeBinding typeBinding2 = elementType.resolveBinding();
		assertNotNull("No type binding2", typeBinding2);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0321() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0321", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding typeBinding = arrayType.resolveBinding();
		checkSourceRange(type, "java.lang.Object[][]", source);
		assertNotNull("No type binding", typeBinding);
		Type elementType = arrayType.getComponentType();
		ITypeBinding typeBinding2 = elementType.resolveBinding();
		assertNotNull("No type binding2", typeBinding2);
		assertEquals("wrong dimension", 1, typeBinding2.getDimensions());
		assertEquals("wrong name", "Object[]", typeBinding2.getName());		
		assertTrue("Not an array type", elementType.isArrayType());
		Type elementType2 = ((ArrayType) elementType).getComponentType();
		assertTrue("Not a simple type", elementType2.isSimpleType());
		ITypeBinding typeBinding3 = elementType2.resolveBinding();
		assertNotNull("No type binding3", typeBinding3);
		assertEquals("wrong dimension", 0, typeBinding3.getDimensions());
		assertEquals("wrong name", "Object", typeBinding3.getName());		
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13231
	 */
	public void test0322() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0322", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a FieldDeclaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a null literal", expression instanceof NullLiteral);
		NullLiteral nullLiteral = (NullLiteral) expression;
		ITypeBinding typeBinding = nullLiteral.resolveTypeBinding();
		assertNotNull("no type binding", typeBinding);
		assertTrue("Not the null type", typeBinding.isNullType());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14198
	 */
	public void test0323() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0323", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression2 = expressionStatement.getExpression();
		assertTrue("Not an Assignement", expression2 instanceof Assignment);
		Assignment assignment = (Assignment) expression2;
		Expression expression = assignment.getRightHandSide();
		assertTrue("Not a CastExpression", expression instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression;
		ITypeBinding typeBinding = castExpression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "Object", typeBinding.getName());
		Type type = castExpression.getType();
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertNotNull("No binding2", typeBinding2);
		assertEquals("Wrong name", "Object", typeBinding2.getName());
	}					

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14198
	 */
	public void test0324() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0324", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression2 = expressionStatement.getExpression();
		assertTrue("Not an Assignement", expression2 instanceof Assignment);
		Assignment assignment = (Assignment) expression2;
		Expression expression = assignment.getRightHandSide();
		assertTrue("Not a CastExpression", expression instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression;
		ITypeBinding typeBinding = castExpression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "Object[]", typeBinding.getName());
		Type type = castExpression.getType();
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertNotNull("No binding2", typeBinding2);
		assertEquals("Wrong name", "Object[]", typeBinding2.getName());
	}					

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14198
	 */
	public void test0325() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0325", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 1);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression2 = expressionStatement.getExpression();
		assertTrue("Not an Assignement", expression2 instanceof Assignment);
		Assignment assignment = (Assignment) expression2;
		Expression expression = assignment.getRightHandSide();
		assertTrue("Not a CastExpression", expression instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression;
		ITypeBinding typeBinding = castExpression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "int[]", typeBinding.getName());
		Type type = castExpression.getType();
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertNotNull("No binding2", typeBinding2);
		assertEquals("Wrong name", "int[]", typeBinding2.getName());
	}					

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14217
	 */
	public void test0326() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0326", "A.java");
		ASTNode result = runConversion(sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertTrue("Not an ExpressionStatement", node instanceof ExpressionStatement);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		checkSourceRange(expressionStatement.getExpression(), "a().f= a()", source);
		checkSourceRange(expressionStatement, "a().f= a();", source);
	}					

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14198
	 */
	public void test0327() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0327", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not an VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a CastExpression", expression instanceof CastExpression);
		CastExpression castExpression = (CastExpression) expression;
		ITypeBinding typeBinding = castExpression.resolveTypeBinding();
		assertNull("typeBinding is not null", typeBinding);
		Type type = castExpression.getType();
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertNull("typeBinding2 is not null", typeBinding2);
	}					

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0328() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0328", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "java.lang.Object[]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding typeBinding = arrayType.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("wrong name", "Object[]", typeBinding.getName());
		Type elementType = arrayType.getElementType();
		assertTrue("Not a simple type", elementType.isSimpleType());
		ITypeBinding typeBinding2 = elementType.resolveBinding();
		assertNotNull("No type binding2", typeBinding2);
		assertEquals("wrong name", "Object", typeBinding2.getName());
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a array creation", expression instanceof ArrayCreation);
		ITypeBinding typeBinding3 = expression.resolveTypeBinding();
		assertNotNull("No typeBinding3", typeBinding3);
		assertEquals("wrong name", "Object[]", typeBinding3.getName());
		ArrayCreation arrayCreation = (ArrayCreation) expression;
		ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
		assertNotNull("not array initializer", arrayInitializer);
		ITypeBinding typeBinding4 = arrayInitializer.resolveTypeBinding();
		assertNotNull("No typeBinding4", typeBinding3);
		assertEquals("wrong name", "Object[]", typeBinding4.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0329() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0329", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "java.lang.Object[]", source);
		assertTrue("Not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding typeBinding = arrayType.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("wrong name", "Object[]", typeBinding.getName());
		Type elementType = arrayType.getElementType();
		assertTrue("Not a simple type", elementType.isSimpleType());
		ITypeBinding typeBinding2 = elementType.resolveBinding();
		assertNotNull("No type binding2", typeBinding2);
		assertEquals("wrong name", "Object", typeBinding2.getName());
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a array creation", expression instanceof ArrayCreation);
		ITypeBinding typeBinding3 = expression.resolveTypeBinding();
		assertNotNull("No typeBinding3", typeBinding3);
		assertEquals("wrong name", "Object[]", typeBinding3.getName());
		ArrayCreation arrayCreation = (ArrayCreation) expression;
		ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
		assertNotNull("not array initializer", arrayInitializer);
		ITypeBinding typeBinding4 = arrayInitializer.resolveTypeBinding();
		assertNotNull("No typeBinding4", typeBinding3);
		assertEquals("wrong name", "Object[]", typeBinding4.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14313
	 */
	public void test0330() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0330", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("wrong size", 2, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("no type binding", typeBinding);
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("wrong size", 1, methods.length);
		assertTrue("not a constructor", methods[0].isConstructor());
		assertTrue("wrong name", !methods[0].getName().equals("foo"));
		node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a methodDeclaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNull("method binding not null", methodBinding);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT);
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		ITypeBinding typeBinding2 = expression.resolveTypeBinding();
		assertNotNull("no type binding2", typeBinding2);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14322
	 */
	public void test0331() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0331", "Test.java");
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not an VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not a QualifiedName", expression instanceof QualifiedName);
		QualifiedName qualifiedName = (QualifiedName) expression;
		IBinding binding = qualifiedName.getName().resolveBinding();
		assertNotNull("no binding", binding);
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertTrue("Not a field", variableBinding.isField());
		assertNull("Got a declaring class", variableBinding.getDeclaringClass());
		assertEquals("wrong name", "length", variableBinding.getName());
	}					

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14403
	 */
	public void test0332() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0332", "LocalSelectionTransfer.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0333() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0333", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not an array creation", expression instanceof ArrayCreation);
		ArrayCreation arrayCreation = (ArrayCreation) expression;
		ITypeBinding typeBinding = arrayCreation.resolveTypeBinding();
		assertNotNull("no type binding", typeBinding);
		assertEquals("wrong name", "Object[][]", typeBinding.getName());
		ArrayType arrayType = arrayCreation.getType();
		ITypeBinding typeBinding2 = arrayType.resolveBinding();
		assertNotNull("no type binding2", typeBinding2);
		assertEquals("wrong name", "Object[][]", typeBinding2.getName());
		Type type = arrayType.getElementType();
		assertTrue("Not a simple type", type instanceof SimpleType);
		SimpleType simpleType = (SimpleType) type;
		ITypeBinding typeBinding3 = simpleType.resolveBinding();
		assertNotNull("no type binding3", typeBinding3);
		assertEquals("wrong name", "Object", typeBinding3.getName());
		type = arrayType.getComponentType();
		assertTrue("Not an array type", type instanceof ArrayType);
		ArrayType arrayType2 = (ArrayType) type;
		ITypeBinding typeBinding4 = arrayType2.resolveBinding();
		assertNotNull("no type binding4", typeBinding4);
		assertEquals("wrong name", "Object[]", typeBinding4.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13807
	 */
	public void test0334() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0334", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a VariableDeclarationStatement", node instanceof VariableDeclarationStatement);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not an array creation", expression instanceof ArrayCreation);
		ArrayCreation arrayCreation = (ArrayCreation) expression;
		ITypeBinding typeBinding = arrayCreation.resolveTypeBinding();
		assertNotNull("no type binding", typeBinding);
		assertEquals("wrong name", "Object[][][]", typeBinding.getName());
		ArrayType arrayType = arrayCreation.getType();
		checkSourceRange(arrayType, "Object[10][][]", source);
		ITypeBinding typeBinding2 = arrayType.resolveBinding();
		assertNotNull("no type binding2", typeBinding2);
		assertEquals("wrong name", "Object[][][]", typeBinding2.getName());
		Type type = arrayType.getElementType();
		assertTrue("Not a simple type", type instanceof SimpleType);
		SimpleType simpleType = (SimpleType) type;
		checkSourceRange(simpleType, "Object", source);
		ITypeBinding typeBinding3 = simpleType.resolveBinding();
		assertNotNull("no type binding3", typeBinding3);
		assertEquals("wrong name", "Object", typeBinding3.getName());
		type = arrayType.getComponentType();
		assertTrue("Not an array type", type instanceof ArrayType);
		ArrayType arrayType2 = (ArrayType) type;
		checkSourceRange(arrayType2, "Object[10][]", source);
		ITypeBinding typeBinding4 = arrayType2.resolveBinding();
		assertNotNull("no type binding4", typeBinding4);
		assertEquals("wrong name", "Object[][]", typeBinding4.getName());
		type = arrayType2.getComponentType();
		assertTrue("Not an array type", type instanceof ArrayType);
		ArrayType arrayType3 = (ArrayType) type;
		ITypeBinding typeBinding5 = arrayType3.resolveBinding();
		assertNotNull("no type binding5", typeBinding5);
		assertEquals("wrong name", "Object[]", typeBinding5.getName());
		checkSourceRange(arrayType3, "Object[10]", source);
	}
	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14526
	 */
	public void test0335() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0335", "ExceptionTestCaseTest.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		assertNotNull("not null", node);
		assertTrue("not a type declaration", node instanceof TypeDeclaration);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		Name name = typeDeclaration.getSuperclass();
		assertNotNull("no super class", name);
		assertTrue("not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		name = qualifiedName.getQualifier();
		assertTrue("not a qualified name", name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		name = qualifiedName.getQualifier();
		assertTrue("not a simple name", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("no binding", binding);
		assertEquals("wrong type", IBinding.PACKAGE, binding.getKind());
		assertEquals("wrong name", "junit", binding.getName());
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14526
	 */
	public void test0336() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0336", "SorterTest.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		assertNotNull("not null", node);
		assertTrue("not a type declaration", node instanceof TypeDeclaration);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List superInterfaces = typeDeclaration.superInterfaces();
		assertEquals("wrong size", 1, superInterfaces.size());
		Name name = (Name) superInterfaces.get(0);
		assertTrue("not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		name = qualifiedName.getQualifier();
		assertTrue("not a simple name", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("no binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("wrong name", "Sorter", binding.getName());
	}	
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14602
	 */
	public void test0337() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0337", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		assertNotNull("not null", node);
		assertTrue("not a field declaration", node instanceof FieldDeclaration);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		checkSourceRange(variableDeclarationFragment, "message= Test.m(\"s\", new String[]{\"g\"})", source);
		checkSourceRange(fieldDeclaration, "String message= Test.m(\"s\", new String[]{\"g\"});", source);
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14852
	 */
	public void test0338() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0338", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		assertNotNull("not null", node);
		assertTrue("not a MethodDeclaration", node instanceof MethodDeclaration);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List thrownExceptions = methodDeclaration.thrownExceptions();
		assertEquals("Wrong size", 1, thrownExceptions.size());
		Name name = (Name) thrownExceptions.get(0);
		IBinding binding = name.resolveBinding();
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("wrong name", "IOException", binding.getName());
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=15061
	 */
	public void test0339() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0339", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No errors found", 2, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("not null", node);
		assertTrue("not a Type declaration", node instanceof TypeDeclaration);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclarations.get(0);
		checkSourceRange(methodDeclaration, "int doQuery(boolean x);", source);
		node = getASTNode(compilationUnit, 0, 1);
		assertNotNull("not null", node);
		assertTrue("not a MethodDeclaration", node instanceof MethodDeclaration);
		String expectedSource = 
			"public void setX(boolean x) {\r\n" + 
			" 		{\r\n" + 
			"		z\r\n" + 
			"	}\r\n" + 
			"}";
		checkSourceRange(node, expectedSource, source);
		int methodEndPosition = node.getStartPosition() + node.getLength();
		node = getASTNode(compilationUnit, 0);
		assertNotNull("not null", node);
		assertTrue("not a TypeDeclaration", node instanceof TypeDeclaration);
		int typeEndPosition = node.getStartPosition() + node.getLength();
		assertEquals("different positions", methodEndPosition, typeEndPosition);
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=14852
	 */
	public void test0340() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "p3", "B.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not an method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertNotNull("No receiver", expression2);
		ITypeBinding binding = expression2.resolveTypeBinding();
		assertNotNull("No type binding", binding);
		assertEquals("wrong name", "A", binding.getName());
		assertEquals("wrong name", "p2", binding.getPackage().getName());
		assertTrue("Not a qualified name", expression2.getNodeType() == ASTNode.QUALIFIED_NAME);
		QualifiedName qualifiedName = (QualifiedName) expression2;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("wrong name", "A", simpleName.getIdentifier());
		ITypeBinding typeBinding = simpleName.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertEquals("wrong name", "A", typeBinding.getName());
		assertEquals("wrong name", "p2", typeBinding.getPackage().getName());
		Name name = qualifiedName.getQualifier();
		assertTrue("Not a simple name", name.getNodeType() == ASTNode.SIMPLE_NAME);
		SimpleName simpleName2 = (SimpleName) name;
		assertEquals("wrong name", "p2", simpleName2.getIdentifier());
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("No binding", binding2);
		assertEquals("wrong type", IBinding.PACKAGE, binding2.getKind());
		assertEquals("wrong name", "p2", binding2.getName());
		node = getASTNode(compilationUnit, 0, 1, 0);
		assertNotNull("not null", node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement2 = (ExpressionStatement) node;
		Expression expression3 = expressionStatement2.getExpression();
		assertTrue("Not an method invocation", expression3.getNodeType() == ASTNode.METHOD_INVOCATION);
		MethodInvocation methodInvocation2 = (MethodInvocation) expression3;
		Expression expression4 = methodInvocation2.getExpression();
		assertNotNull("No receiver", expression4);
		ITypeBinding binding3 = expression4.resolveTypeBinding();
		assertNotNull("No type binding", binding3);
		assertEquals("wrong name", "A", binding3.getName());
		assertEquals("wrong name", "p1", binding3.getPackage().getName());
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=15804
	 */
	public void test0341() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0341", "A.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertNotNull("not null", node);
		assertTrue("Not an if statement", node.getNodeType() == ASTNode.IF_STATEMENT);
		String expectedSource = 
				"if (field != null) {\r\n" +
				"			throw new IOException();\r\n" +
				"		} else if (field == null) {\r\n" +
				"			throw new MalformedURLException();\r\n" +
				"		} else if (field == null) {\r\n" +
				"			throw new InterruptedIOException();\r\n" +
				"		} else {\r\n" +
				"			throw new UnsupportedEncodingException();\r\n" +
				"		}";
		checkSourceRange(node, expectedSource, source);
		IfStatement ifStatement = (IfStatement) node;
		Statement thenStatement = ifStatement.getThenStatement();
		expectedSource = 
				"{\r\n" +
				"			throw new IOException();\r\n" +
				"		}";
		checkSourceRange(thenStatement, expectedSource, source);
		Statement elseStatement = ifStatement.getElseStatement();
		expectedSource = 
				"if (field == null) {\r\n" +
				"			throw new MalformedURLException();\r\n" +
				"		} else if (field == null) {\r\n" +
				"			throw new InterruptedIOException();\r\n" +
				"		} else {\r\n" +
				"			throw new UnsupportedEncodingException();\r\n" +
				"		}";
		checkSourceRange(elseStatement, expectedSource, source);
		assertTrue("Not a if statement", elseStatement.getNodeType() == ASTNode.IF_STATEMENT);
		ifStatement = (IfStatement) elseStatement;
		thenStatement = ifStatement.getThenStatement();
		expectedSource = 
				"{\r\n" +
				"			throw new MalformedURLException();\r\n" +
				"		}";
		checkSourceRange(thenStatement, expectedSource, source);
		elseStatement = ifStatement.getElseStatement();
		expectedSource = 
				"if (field == null) {\r\n" +
				"			throw new InterruptedIOException();\r\n" +
				"		} else {\r\n" +
				"			throw new UnsupportedEncodingException();\r\n" +
				"		}";
		checkSourceRange(elseStatement, expectedSource, source);
		assertTrue("Not a if statement", elseStatement.getNodeType() == ASTNode.IF_STATEMENT);
		ifStatement = (IfStatement) elseStatement;
		thenStatement = ifStatement.getThenStatement();
		expectedSource = 
				"{\r\n" +
				"			throw new InterruptedIOException();\r\n" +
				"		}";
		checkSourceRange(thenStatement, expectedSource, source);
		elseStatement = ifStatement.getElseStatement();
		expectedSource = 
				"{\r\n" +
				"			throw new UnsupportedEncodingException();\r\n" +
				"		}";
		checkSourceRange(elseStatement, expectedSource, source);
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=15657
	 */
	public void test0342() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0342", "Test.java");
		IDOMCompilationUnit dcompUnit = new DOMFactory().createCompilationUnit(sourceUnit.getSource(), sourceUnit.getElementName());
		assertNotNull("dcompUnit is null", dcompUnit);

		// searching class 
		IDOMType classNode = null;
		Enumeration children = dcompUnit.getChildren();
		assertNotNull("dcompUnit has no children", children);
		
		while (children.hasMoreElements()) {
			IDOMNode child = (IDOMNode) children.nextElement();
			if (child.getNodeType() == IDOMNode.TYPE) {
				classNode = (IDOMType) child;
				break;
			}
		}
		assertNotNull("classNode is null", classNode);

		// searching for methods
		children = classNode.getChildren();

		assertNotNull("classNode has no children", children);

		while (children.hasMoreElements()) {
			IDOMNode child = (IDOMNode) children.nextElement();
			if (child.getNodeType() == IDOMNode.METHOD) {
				IDOMMethod childMethod = (IDOMMethod) child;

				// returnType is always null;
				String returnType = childMethod.getReturnType();
				if (childMethod.isConstructor()) {
					assertNull(returnType);
				} else {
					assertNotNull("no return type", returnType);
				}
			}
		}
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=16051
	 */
	public void test0343() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0343", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 1, 1);
		assertNotNull("not null", node);
		assertTrue("Not an if statement", node.getNodeType() == ASTNode.IF_STATEMENT);
		String expectedSource = 
				"if (flag)\r\n" +
				"			i= 10;";
		checkSourceRange(node, expectedSource, source);
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=16132
	 */
	public void test0344() throws JavaModelException {
		Preferences preferences = null;
		String pb_assert = null;
		String compiler_source = null;
		String compiler_compliance = null;
		try {
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0344", "Test.java");
			char[] source = sourceUnit.getSource().toCharArray();
			preferences = JavaCore.getPlugin().getPluginPreferences();
			pb_assert = preferences.getString(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER);
			compiler_source = preferences.getString(JavaCore.COMPILER_SOURCE);
			compiler_compliance = preferences.getString(JavaCore.COMPILER_COMPLIANCE);
			
			preferences.setValue(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR); 
			preferences.setValue(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			preferences.setValue(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4); 
			
			ASTNode result = runConversion(sourceUnit, true);
			assertNotNull("No compilation unit", result);
			assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			assertEquals("errors found", 0, compilationUnit.getMessages().length);
		} finally {
			if (preferences != null) {
				preferences.setValue(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, pb_assert); 
				preferences.setValue(JavaCore.COMPILER_SOURCE, compiler_source); 
				preferences.setValue(JavaCore.COMPILER_COMPLIANCE, compiler_compliance); 
			}
		}
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=17922
	 */
	public void test0345() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0345", "A.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not an ArrayCreation", expression.getNodeType() == ASTNode.ARRAY_CREATION);
		ArrayCreation arrayCreation = (ArrayCreation) expression;
		ArrayType arrayType = arrayCreation.getType();
		IBinding binding2 = arrayType.resolveBinding();
		assertNotNull("no binding2", binding2);
		assertEquals("not a type", binding2.getKind(), IBinding.TYPE);
		ITypeBinding typeBinding2 = (ITypeBinding) binding2;
		assertTrue("Not an array type binding2", typeBinding2.isArray());
		Type type = arrayType.getElementType();
		assertTrue("Not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("QualifiedName", name.getNodeType() == ASTNode.QUALIFIED_NAME);
		SimpleName simpleName = ((QualifiedName) name).getName();
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("no binding", binding);
		assertEquals("not a type", binding.getKind(), IBinding.TYPE);
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertTrue("An array type binding", !typeBinding.isArray());
		Type type2 = fieldDeclaration.getType();
		assertTrue("Not a array type", type2.isArrayType());
		ArrayType arrayType2 = (ArrayType) type2;
		Type type3 = arrayType2.getElementType();
		assertTrue("Not a simple type", type3.isSimpleType());
		SimpleType simpleType2 = (SimpleType) type3;
		Name name2 = simpleType2.getName();
		assertTrue("Not a qualified name", name2.getNodeType() == ASTNode.QUALIFIED_NAME);
		SimpleName simpleName2 = ((QualifiedName) name2).getName();
		IBinding binding3 = simpleName2.resolveBinding();
		assertNotNull("no binding", binding3);
		assertEquals("not a type", binding3.getKind(), IBinding.TYPE);
		ITypeBinding typeBinding3 = (ITypeBinding) binding3;
		assertTrue("An array type binding", !typeBinding3.isArray());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18138
	 */
	public void test0346() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0346", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an variable declaration", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "Vector", source);
		assertTrue("not an array type", !type.isArrayType());
		assertTrue("Not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("Not a simpleName", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertTrue("An array", !typeBinding.isArray());
		assertEquals("Wrong name", "Vector", binding.getName());
		ITypeBinding typeBinding2 = simpleType.resolveBinding();
		assertNotNull("No binding", typeBinding2);
		assertEquals("Wrong type", IBinding.TYPE, typeBinding2.getKind());
		assertTrue("An array", !typeBinding2.isArray());
		assertEquals("Wrong name", "Vector", typeBinding2.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18138
	 */
	public void test0347() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0347", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an variable declaration", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "Vector[]", source);
		assertTrue("not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding binding = arrayType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertTrue("Not an array type", binding.isArray());
		assertEquals("Wrong name", "Vector[]", binding.getName());
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18138
	 */
	public void test0348() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0348", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an variable declaration", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "Vector[][]", source);
		assertTrue("not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding binding = arrayType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertTrue("Not an array type", binding.isArray());
		assertEquals("Wrong name", "Vector[][]", binding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18138
	 */
	public void test0349() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0349", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Vector[][]", source);
		assertTrue("not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		ITypeBinding binding = arrayType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertTrue("Not an array type", binding.isArray());
		assertEquals("Wrong name", "Vector[][]", binding.getName());
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18138
	 */
	public void test0350() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0350", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Vector", source);
		assertTrue("not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		ITypeBinding binding = simpleType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertTrue("An array type", binding.isClass());
		assertEquals("Wrong name", "Vector", binding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18169
	 */
	public void test0351() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0351", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 2, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		checkSourceRange(singleVariableDeclaration, "int a", source);
		singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		checkSourceRange(singleVariableDeclaration, "int[] b", source);
		node = getASTNode(compilationUnit, 0, 1);
		assertNotNull("not null", node);
		assertTrue("Not an method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		methodDeclaration = (MethodDeclaration) node;
		parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 2, parameters.size());
		singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		checkSourceRange(singleVariableDeclaration, "int a", source);
		singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		checkSourceRange(singleVariableDeclaration, "int b[]", source);			
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18169
	 */
	public void test0352() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0352", "Test2.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 2, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		checkSourceRange(singleVariableDeclaration, "final int a", source);
		singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		checkSourceRange(singleVariableDeclaration, "final int[] b", source);
		node = getASTNode(compilationUnit, 0, 1);
		assertNotNull("not null", node);
		assertTrue("Not an method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		methodDeclaration = (MethodDeclaration) node;
		parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 2, parameters.size());
		singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		checkSourceRange(singleVariableDeclaration, "final int a", source);
		singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		checkSourceRange(singleVariableDeclaration, "final int b[]", source);			
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=18042
	 */
	public void test0353() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0353", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("not null", node);
		assertTrue("Not an variable declaration", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		checkSourceRange(type, "InputStream", source);
		assertTrue("not a simple type", type.isSimpleType());
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertTrue("Not a class", binding.isClass());
		assertEquals("Wrong name", "InputStream", binding.getName());
		assertEquals("Wrong package", "java.io", binding.getPackage().getName());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding", binding2);
		assertEquals("Wrong type", IBinding.TYPE, binding2.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding2;
		assertTrue("Not a class", typeBinding.isClass());
		assertEquals("Wrong name", "InputStream", typeBinding.getName());
		assertEquals("Wrong package", "java.io", typeBinding.getPackage().getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=19851
	 */
	public void test0354() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0354", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 3, compilationUnit.getMessages().length);
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=20520
	 */
	public void test0355() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0355", "Foo.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 0, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull(node);
		assertTrue("Not an if statement", node.getNodeType() == ASTNode.IF_STATEMENT);
		IfStatement ifStatement = (IfStatement) node;
		Expression condition = ifStatement.getExpression();
		assertTrue("Not an infixExpression", condition.getNodeType() == ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) condition;
		Expression expression = infixExpression.getLeftOperand();
		assertTrue("Not a method invocation expression", expression.getNodeType() == ASTNode.METHOD_INVOCATION);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a parenthesis expression", expression2.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=20865
	 */
	public void test0356() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0356", "X.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertNotNull("No compilation unit", result);
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("errors found", 2, compilationUnit.getMessages().length);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull(node);
		assertTrue("Not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		Type type = variableDeclarationStatement.getType();
		ITypeBinding binding = type.resolveBinding();
		assertNull(binding);
	}
	/**
	 * ForStatement ==> ForStatement
	 */
	public void test0357() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0357", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, false);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull("Expression should not be null", node); //$NON-NLS-1$
		ForStatement forStatement = this.ast.newForStatement();

		VariableDeclarationFragment iFragment = this.ast.newVariableDeclarationFragment();
		iFragment.setName(this.ast.newSimpleName("i")); //$NON-NLS-1$
		iFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$
		VariableDeclarationFragment jFragment = this.ast.newVariableDeclarationFragment();
		jFragment.setName(this.ast.newSimpleName("j")); //$NON-NLS-1$
		jFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$
		VariableDeclarationFragment kFragment = this.ast.newVariableDeclarationFragment();
		kFragment.setName(this.ast.newSimpleName("k")); //$NON-NLS-1$
		kFragment.setInitializer(this.ast.newNumberLiteral("0"));//$NON-NLS-1$

		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(iFragment);
		variableDeclarationExpression.setModifiers(Modifier.NONE);
		variableDeclarationExpression.setType(this.ast.newPrimitiveType(PrimitiveType.INT));
		variableDeclarationExpression.fragments().add(jFragment);
		variableDeclarationExpression.fragments().add(kFragment);
		forStatement.initializers().add(variableDeclarationExpression);

		PostfixExpression iPostfixExpression = this.ast.newPostfixExpression();
		iPostfixExpression.setOperand(this.ast.newSimpleName("i"));//$NON-NLS-1$
		iPostfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(iPostfixExpression);
		
		PostfixExpression jPostfixExpression = this.ast.newPostfixExpression();
		jPostfixExpression.setOperand(this.ast.newSimpleName("j"));//$NON-NLS-1$
		jPostfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(jPostfixExpression);

		PostfixExpression kPostfixExpression = this.ast.newPostfixExpression();
		kPostfixExpression.setOperand(this.ast.newSimpleName("k"));//$NON-NLS-1$
		kPostfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
		forStatement.updaters().add(kPostfixExpression);

		forStatement.setBody(this.ast.newBlock());
		
		InfixExpression infixExpression = this.ast.newInfixExpression();
		infixExpression.setLeftOperand(this.ast.newSimpleName("i"));
		infixExpression.setOperator(InfixExpression.Operator.LESS);
		infixExpression.setRightOperand(this.ast.newNumberLiteral("10"));
		forStatement.setExpression(infixExpression);
		
		assertTrue("Both AST trees should be identical", forStatement.subtreeMatch(new ASTMatcher(), node));		//$NON-NLS-1$
		checkSourceRange(node, "for (int i=0, j=0, k=0; i<10 ; i++, j++, k++) {}", source); //$NON-NLS-1$
	}
	
	private ASTNode getASTNodeToCompare(org.eclipse.jdt.core.dom.CompilationUnit unit) {
		ExpressionStatement statement = (ExpressionStatement) getASTNode(unit, 0, 0, 0);
		return (ASTNode) ((MethodInvocation) statement.getExpression()).arguments().get(0);
	}

	private ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex, int statementIndex) {
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

	private ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex, int bodyIndex) {
		return (ASTNode) ((TypeDeclaration)unit.types().get(typeIndex)).bodyDeclarations().get(bodyIndex);
	}

	private ASTNode getASTNode(org.eclipse.jdt.core.dom.CompilationUnit unit, int typeIndex) {
		return (ASTNode) (TypeDeclaration)unit.types().get(typeIndex);
	}
		
	private void checkSourceRange(ASTNode node, String expectedContents, char[] source) {
		assertNotNull("The node is null", node);
		assertTrue("The node(" + node.getClass() + ").getLength() == 0", node.getLength() != 0);
		assertTrue("The node.getStartPosition() == -1", node.getStartPosition() != -1);
		int length = node.getLength();
		int start = node.getStartPosition();
		char[] actualContents = new char[length];
		System.arraycopy(source, start, actualContents, 0, length);
		String actualContentsString = new String(actualContents);		
		assertTrue("The two strings are not equals\n---\nactualContents = >" + actualContentsString + "<\nexpectedContents = >" + expectedContents + "<\n----", expectedContents.equals(actualContentsString));
	}
	
	private boolean isMalformed(ASTNode node) {
		return (node.getFlags() & ASTNode.MALFORMED) != 0;
	}
}

