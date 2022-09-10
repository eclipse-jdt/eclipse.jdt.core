/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
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
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.tests.model.CancelCounter;
import org.eclipse.jdt.core.tests.model.Canceler;
import org.eclipse.jdt.core.tests.model.ReconcilerTests;
import org.eclipse.jdt.core.tests.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTConverterTestAST3_2 extends ConverterTestSetup {

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getJLS3(), false);
	}

	public ASTConverterTestAST3_2(String name) {
		super(name);
	}

	static {
//		TESTS_NAMES = new String[] {"test0602"};
//		TESTS_RANGE = new int[] { 713, -1 };
//		TESTS_NUMBERS =  new int[] { 721 };
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverterTestAST3_2.class);
	}
	/**
	 * Internal access method to MethodDeclaration#thrownExceptions() for avoiding deprecated warnings.
	 * @deprecated
	 */
	private static List internalThrownExceptions(MethodDeclaration methodDeclaration) {
		return methodDeclaration.thrownExceptions();
	}

	/**
	 * @deprecated
	 */
	private Type componentType(ArrayType array) {
		return array.getComponentType();
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=22560
	 */
	public void test0401() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0401", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not a return statement", statement.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) statement;
		Expression expression = returnStatement.getExpression();
		assertNotNull("there is no expression", expression); //$NON-NLS-1$
		// call the default initialization
		methodDeclaration.getReturnType2();
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding); //$NON-NLS-1$
		assertEquals("wrong name", "int", typeBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23464
	 */
	public void test0402() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0402", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 0);
		assertEquals("Wrong number of problems", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not a super method invocation", node.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION); //$NON-NLS-1$
		checkSourceRange(node, "new A().super();", source); //$NON-NLS-1$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
	 */
	public void test0403() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0403", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 1);
		assertEquals("Wrong number of problems", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("wrong type", binding.getKind() == IBinding.VARIABLE); //$NON-NLS-1$
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "test", variableBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("no binding2", binding2); //$NON-NLS-1$
		assertTrue("Wrong type", binding2.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding = (IMethodBinding) binding2;
		assertEquals("Wrong name", "clone", methodBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		IMethodBinding methodBinding2 = methodInvocation.resolveMethodBinding();
		assertNotNull("No method binding2", methodBinding2);
		assertTrue("Wrong binding", methodBinding == methodBinding2);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
	 */
	public void test0404() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0404", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertEquals("Wrong number of problems", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("wrong type", binding.getKind() == IBinding.VARIABLE); //$NON-NLS-1$
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "a", variableBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("no binding2", binding2); //$NON-NLS-1$
		assertTrue("Wrong type", binding2.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding = (IMethodBinding) binding2;
		assertEquals("Wrong name", "clone", methodBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
	 */
	public void test0405() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0405", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 1);
		assertEquals("Wrong number of problems", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("wrong type", binding.getKind() == IBinding.VARIABLE); //$NON-NLS-1$
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "a", variableBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("no binding2", binding2); //$NON-NLS-1$
		assertTrue("Wrong type", binding2.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding = (IMethodBinding) binding2;
		assertEquals("Wrong name", "clone", methodBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
	 */
	public void test0406() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0406", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertEquals("Wrong number of problems", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("wrong type", binding.getKind() == IBinding.VARIABLE); //$NON-NLS-1$
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "a", variableBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "foo", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("no binding2", binding2); //$NON-NLS-1$
		assertTrue("Wrong type", binding2.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding = (IMethodBinding) binding2;
		assertEquals("Wrong name", "foo", methodBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull("Got a declaring node in the unit", unit.findDeclaringNode(methodBinding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23162
	 */
	public void test0407() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0407", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Wrong number of problems", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		CompilationUnit unit = (CompilationUnit) result;
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		SimpleName simpleName = methodDeclaration.getName();
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("Not a method binding", binding.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding = (IMethodBinding) binding;
		assertEquals("wrong name", "foo", methodBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		methodDeclaration.setName(methodDeclaration.getAST().newSimpleName("foo2")); //$NON-NLS-1$
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();
		assertNotNull("No methodbinding2", methodBinding2); //$NON-NLS-1$
		assertEquals("wrong name", "foo", methodBinding2.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		simpleName = methodDeclaration.getName();
		IBinding binding2 = simpleName.resolveBinding();
		assertNull("Got a binding2", binding2); //$NON-NLS-1$

		ASTNode astNode = unit.findDeclaringNode(methodBinding);
		assertNotNull("No declaring node", astNode);
		assertEquals("wrong declaring node", methodDeclaration, astNode);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23162
	 */
	public void test0408() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0408", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Wrong number of problems", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType2();
		assertTrue("Not a simple type", type.isSimpleType()); //$NON-NLS-1$
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.isQualifiedName()); //$NON-NLS-1$
		QualifiedName qualifiedName = (QualifiedName) name;
		name = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", name.isQualifiedName()); //$NON-NLS-1$
		qualifiedName = (QualifiedName) name;
		name = qualifiedName.getQualifier();
		assertTrue("Not a simple name", name.isSimpleName()); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) name;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("Not a package binding", binding.getKind() == IBinding.PACKAGE); //$NON-NLS-1$
		assertEquals("Wrong name", "java", binding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23162
	 */
	public void test0409() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		try {
			newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			JavaCore.setOptions(newOptions);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0409", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(getJLS3(), sourceUnit, true);
			assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
			CompilationUnit compilationUnit = (CompilationUnit) result;
			assertProblemsSize(compilationUnit, 0);
			BindingsCollectorVisitor bindingsCollectorVisitor = new BindingsCollectorVisitor();
			compilationUnit.accept(bindingsCollectorVisitor);
			assertEquals("wrong number", 3, bindingsCollectorVisitor.getUnresolvedNodesSet().size()); //$NON-NLS-1$
			Map bindingsMap = bindingsCollectorVisitor.getBindingsMap();
			assertEquals("wrong number", 211, bindingsMap.size()); //$NON-NLS-1$
			ASTNodesCollectorVisitor nodesCollector = new ASTNodesCollectorVisitor();
			compilationUnit.accept(nodesCollector);
			Set detachedNodes = nodesCollector.getDetachedAstNodes();
			for (Iterator iterator = detachedNodes.iterator(); iterator.hasNext(); ) {
				ASTNode detachedNode = (ASTNode) iterator.next();
				IBinding binding = (IBinding) bindingsMap.get(detachedNode);
				assertNotNull(binding);
				switch(detachedNode.getNodeType()) {
					case ASTNode.ARRAY_ACCESS :
					case ASTNode.ARRAY_CREATION :
					case ASTNode.ARRAY_INITIALIZER :
					case ASTNode.ASSIGNMENT :
					case ASTNode.BOOLEAN_LITERAL :
					case ASTNode.CAST_EXPRESSION :
					case ASTNode.CHARACTER_LITERAL :
					case ASTNode.CLASS_INSTANCE_CREATION :
					case ASTNode.CONDITIONAL_EXPRESSION :
					case ASTNode.FIELD_ACCESS :
					case ASTNode.INFIX_EXPRESSION :
					case ASTNode.INSTANCEOF_EXPRESSION :
					case ASTNode.METHOD_INVOCATION :
					case ASTNode.NULL_LITERAL :
					case ASTNode.NUMBER_LITERAL :
					case ASTNode.POSTFIX_EXPRESSION :
					case ASTNode.PREFIX_EXPRESSION :
					case ASTNode.THIS_EXPRESSION :
					case ASTNode.TYPE_LITERAL :
					case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
						ITypeBinding typeBinding = ((Expression) detachedNode).resolveTypeBinding();
						if (!binding.equals(typeBinding)) {
							System.out.println(detachedNode);
						}
						assertTrue("binding not equals", binding.equals(typeBinding)); //$NON-NLS-1$
						break;
					case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
						assertTrue("binding not equals", binding.equals(((VariableDeclarationFragment) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
					case ASTNode.ANONYMOUS_CLASS_DECLARATION :
						assertTrue("binding not equals", binding.equals(((AnonymousClassDeclaration) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
					case ASTNode.QUALIFIED_NAME :
					case ASTNode.SIMPLE_NAME :
						IBinding newBinding = ((Name) detachedNode).resolveBinding();
						assertTrue("binding not equals", binding.equals(newBinding)); //$NON-NLS-1$
						break;
					case ASTNode.ARRAY_TYPE :
					case ASTNode.SIMPLE_TYPE :
					case ASTNode.PRIMITIVE_TYPE :
						assertTrue("binding not equals", binding.equals(((Type) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
					case ASTNode.CONSTRUCTOR_INVOCATION :
						assertTrue("binding not equals", binding.equals(((ConstructorInvocation) detachedNode).resolveConstructorBinding())); //$NON-NLS-1$
						break;
					case ASTNode.IMPORT_DECLARATION :
						assertTrue("binding not equals", binding.equals(((ImportDeclaration) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
					case ASTNode.METHOD_DECLARATION :
						assertTrue("binding not equals", binding.equals(((MethodDeclaration) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
					case ASTNode.PACKAGE_DECLARATION :
						assertTrue("binding not equals", binding.equals(((PackageDeclaration) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
					case ASTNode.TYPE_DECLARATION :
						assertTrue("binding not equals", binding.equals(((TypeDeclaration) detachedNode).resolveBinding())); //$NON-NLS-1$
						break;
				}
			}
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/**
	 * Test for message on jdt-core-dev
	 */
	public void test0410() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0410", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Wrong number of problems", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull(node);
		assertTrue("Not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		Expression expression = ((ReturnStatement) node).getExpression();
		assertTrue("Not an infix expression", expression.getNodeType() == ASTNode.INFIX_EXPRESSION); //$NON-NLS-1$
		InfixExpression infixExpression = (InfixExpression) expression;
		List extendedOperands = infixExpression.extendedOperands();
		assertEquals("wrong size", 3, extendedOperands.size()); //$NON-NLS-1$
	}

	/**
	 * Test for message on jdt-core-dev
	 */
	public void test0411() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0411", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Wrong number of problems", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertNotNull(node);
		assertTrue("Not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		Expression expression = ((ReturnStatement) node).getExpression();
		assertTrue("Not an infix expression", expression.getNodeType() == ASTNode.INFIX_EXPRESSION); //$NON-NLS-1$
		InfixExpression infixExpression = (InfixExpression) expression;
		List extendedOperands = infixExpression.extendedOperands();
		assertEquals("wrong size", 0, extendedOperands.size()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23901
	 */
	public void test0412() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0412", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0);
		assertNotNull(node);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertTrue("Not an interface", typeDeclaration.isInterface()); //$NON-NLS-1$
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding); //$NON-NLS-1$
		assertNotNull("No declaring node", unit.findDeclaringNode(typeBinding)); //$NON-NLS-1$
		Name name = typeDeclaration.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		ASTNode declaringNode = unit.findDeclaringNode(binding);
		assertNotNull("No declaring node", declaringNode); //$NON-NLS-1$
		assertEquals("Wrong node", typeDeclaration, declaringNode); //$NON-NLS-1$
		typeBinding = name.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding); //$NON-NLS-1$
		declaringNode = unit.findDeclaringNode(typeBinding);
		assertNotNull("No declaring node", declaringNode); //$NON-NLS-1$
		assertEquals("Wrong node", typeDeclaration, declaringNode); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=20881
	 */
	public void test0413() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0413", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, false, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List throwsException = internalThrownExceptions(methodDeclaration);
		assertEquals("wrong size", 2, throwsException.size()); //$NON-NLS-1$
		Name name = (Name) throwsException.get(0);
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertEquals("LIOException;", binding.getKey());
		assertTrue("Binding should be marked as recovered", binding.isRecovered());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23734
	 */
	public void test0414() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0414", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType2();
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No type binding", typeBinding); //$NON-NLS-1$
		ASTNode declaringNode = unit.findDeclaringNode(typeBinding);
		assertNull("Got a declaring node", declaringNode); //$NON-NLS-1$

		node = getASTNode(unit, 0, 1);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration2 = (MethodDeclaration) node;
		Type type2 = methodDeclaration2.getReturnType2();
		ITypeBinding typeBinding2 = type2.resolveBinding();
		assertNotNull("No type binding", typeBinding2); //$NON-NLS-1$
		ASTNode declaringNode2 = unit.findDeclaringNode(typeBinding2);
		assertNotNull("No declaring node", declaringNode2); //$NON-NLS-1$

		ICompilationUnit sourceUnit2 = getCompilationUnit("Converter" , "src", "test0414", "B.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		result = runConversion(getJLS3(), sourceUnit2, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit2 = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit2.getProblems().length); //$NON-NLS-1$
		ASTNode declaringNode3 = unit2.findDeclaringNode(typeBinding);
		assertNull("Got a declaring node", declaringNode3); //$NON-NLS-1$

		ASTNode declaringNode4 = unit2.findDeclaringNode(typeBinding.getKey());
		assertNotNull("No declaring node", declaringNode4); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24268
	 */
	public void test0415() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0415", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a switch statement", node.getNodeType() == ASTNode.SWITCH_STATEMENT); //$NON-NLS-1$
		SwitchStatement switchStatement = (SwitchStatement) node;
		List statements = switchStatement.statements();
		assertEquals("wrong size", statements.size(), 5); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(3);
		assertTrue("not a switch case (default)", statement.getNodeType() == ASTNode.SWITCH_CASE); //$NON-NLS-1$
		SwitchCase defaultCase = (SwitchCase) statement;
		assertTrue("not a default case", defaultCase.isDefault());
		checkSourceRange(defaultCase, "default:", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24324
	 */
	public void test0416() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0416", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", fragments.size(), 1);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression init = fragment.getInitializer();
		assertTrue("not a qualified name", init.getNodeType() == ASTNode.QUALIFIED_NAME); //$NON-NLS-1$
		QualifiedName qualifiedName = (QualifiedName) init;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("Wrong name", "CONST", simpleName.getIdentifier());
		IBinding binding = simpleName.resolveBinding();
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong modifier", variableBinding.getModifiers(), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
		ASTNode declaringNode = unit.findDeclaringNode(variableBinding);
		assertNotNull("No declaring node", declaringNode);
		assertTrue("not a variable declaration fragment", declaringNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) declaringNode;
		FieldDeclaration fieldDeclaration = (FieldDeclaration) variableDeclarationFragment.getParent();
		assertEquals("Wrong modifier", fieldDeclaration.getModifiers(), Modifier.NONE);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24347
	 */
	public void test0417() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0417", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("not a simple type", type.getNodeType() == ASTNode.SIMPLE_TYPE); //$NON-NLS-1$
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		Name qualifier = qualifiedName.getQualifier();
		assertTrue("Not a simple name", qualifier.isSimpleName());
		IBinding binding = qualifier.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24406
	 */
	public void test0418() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0418", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 1, 0);
		assertNotNull("No node", node);
		assertTrue("not an expression statement ", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("not an method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Name name = methodInvocation.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24449
	 */
	public void test0419() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0419", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", node.getNodeType(), ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", expression.getNodeType(), ASTNode.ASSIGNMENT);
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a name", expression2.getNodeType(), ASTNode.SIMPLE_NAME);
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding = simpleName.resolveBinding();
		assertNull(binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24453
	 */
	public void test0420() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0420", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", expression.getNodeType(), ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getRightOperand();
		assertEquals("Not a parenthesized expression", expression2.getNodeType(), ASTNode.PARENTHESIZED_EXPRESSION);
		checkSourceRange(expression2, "(2 + 3)", source);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression2;
		Expression expression3 = parenthesizedExpression.getExpression();
		checkSourceRange(expression3, "2 + 3", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24453
	 */
	public void test0421() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0421", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", expression.getNodeType(), ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) expression;
		checkSourceRange(infixExpression, "(1 + 2) + 3", source);
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Not a parenthesized expression", expression2.getNodeType(), ASTNode.PARENTHESIZED_EXPRESSION);
		checkSourceRange(expression2, "(1 + 2)", source);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression2;
		Expression expression3 = parenthesizedExpression.getExpression();
		checkSourceRange(expression3, "1 + 2", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24453
	 */
	public void test0422() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0422", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", expression.getNodeType(), ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) expression;
		checkSourceRange(infixExpression, "( 1 + 2 ) + 3", source);
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Not a parenthesized expression", expression2.getNodeType(), ASTNode.PARENTHESIZED_EXPRESSION);
		checkSourceRange(expression2, "( 1 + 2 )", source);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression2;
		Expression expression3 = parenthesizedExpression.getExpression();
		checkSourceRange(expression3, "1 + 2", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24453
	 */
	public void test0423() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0423", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", expression.getNodeType(), ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getRightOperand();
		assertEquals("Not a parenthesized expression", expression2.getNodeType(), ASTNode.PARENTHESIZED_EXPRESSION);
		checkSourceRange(expression2, "( 2 + 3 )", source);
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression2;
		Expression expression3 = parenthesizedExpression.getExpression();
		checkSourceRange(expression3, "2 + 3", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24453
	 */
	public void test0424() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0424", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", expression.getNodeType(), ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) expression;
		assertEquals("Wrong size", 1, infixExpression.extendedOperands().size());
		Expression expression2 = (Expression) infixExpression.extendedOperands().get(0);
		checkSourceRange(expression2, "( 2 + 3 )", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24453
	 */
	public void test0425() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0425", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", expression.getNodeType(), ASTNode.INFIX_EXPRESSION);
		InfixExpression infixExpression = (InfixExpression) expression;
		assertEquals("Wrong size", 0, infixExpression.extendedOperands().size());
		Expression expression2 = infixExpression.getRightOperand();
		assertTrue("not an infix expression", expression2.getNodeType() == ASTNode.INFIX_EXPRESSION); //$NON-NLS-1$
		InfixExpression infixExpression2 = (InfixExpression) expression2;
		Expression expression3 = infixExpression2.getRightOperand();
		assertTrue("not a parenthesized expression", expression3.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION); //$NON-NLS-1$
		checkSourceRange(expression3, "( 2 + 3 )", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24449
	 */
	public void test0426() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0426", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, false, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("not a simple type", type.getNodeType() == ASTNode.SIMPLE_TYPE);
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertNotNull("No name", name);
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24449
	 */
	public void test0427() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0427", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", node.getNodeType(), ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", expression.getNodeType(), ASTNode.ASSIGNMENT);
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a super field access", expression2.getNodeType(), ASTNode.SUPER_FIELD_ACCESS);
		SuperFieldAccess superFieldAccess = (SuperFieldAccess) expression2;
		Name name = superFieldAccess.getName();
		assertNotNull("No name", name);
		IBinding binding = name.resolveBinding();
		assertNull("Got a binding", binding);
		assertNull("Got a binding", superFieldAccess.resolveFieldBinding());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24449
	 */
	public void test0428() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0428", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", node.getNodeType(), ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", expression.getNodeType(), ASTNode.ASSIGNMENT);
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a qualified name", expression2.getNodeType(), ASTNode.QUALIFIED_NAME);
		QualifiedName name = (QualifiedName) expression2;
		SimpleName simpleName = name.getName();
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding2", binding2);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24449
	 */
	public void test0429() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0429", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", node.getNodeType(), ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", expression.getNodeType(), ASTNode.ASSIGNMENT);
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a qualified name", expression2.getNodeType(), ASTNode.QUALIFIED_NAME);
		QualifiedName name = (QualifiedName) expression2;
		SimpleName simpleName = name.getName();
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding2", binding2);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24499
	 */
	public void test0430() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0430", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertTrue("Not a constructor invocation", node.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION);
		ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
		checkSourceRange(constructorInvocation, "this(coo2());", source);
		List arguments = constructorInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		Expression expression = (Expression) arguments.get(0);
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName simpleName = methodInvocation.getName();
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24500
	 */
	public void test0431() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0431", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertTrue("Not a constructor invocation", node.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION);
		ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
		List arguments = constructorInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		Expression expression = (Expression) arguments.get(0);
		assertTrue("Not a simple name", expression.getNodeType() == ASTNode.SIMPLE_NAME);
		SimpleName simpleName = (SimpleName) expression;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24501
	 */
	public void test0432() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0432", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", ASTNode.ASSIGNMENT, expression.getNodeType());
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression2.getNodeType());
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24501
	 */
	public void test0433() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0433", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", ASTNode.ASSIGNMENT, expression.getNodeType());
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a super field access", ASTNode.SUPER_FIELD_ACCESS, expression2.getNodeType());
		SuperFieldAccess superFieldAccess = (SuperFieldAccess) expression2;
		SimpleName simpleName = superFieldAccess.getName();
		assertEquals("wrong name", "fCoo", simpleName.getIdentifier());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong binding", IBinding.VARIABLE, binding.getKind());
		IVariableBinding variableBinding = superFieldAccess.resolveFieldBinding();
		assertTrue("Different binding", binding == variableBinding);
		ASTNode astNode = unit.findDeclaringNode(variableBinding);
		assertTrue("Wrong type", astNode.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION || astNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT || astNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24501
	 */
	public void test0434() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0434", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", ASTNode.ASSIGNMENT, expression.getNodeType());
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression2.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) expression2;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("wrong name", "fCoo", simpleName.getIdentifier());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24501
	 */
	public void test0435() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0435", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an assignment", ASTNode.ASSIGNMENT, expression.getNodeType());
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getLeftHandSide();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression2.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) expression2;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("wrong name", "fCoo", simpleName.getIdentifier());
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24502
	 */
	public void test0436() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0436", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, false, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertProblemsSize(unit, 1, "The type A.CInner is not visible"); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("wrong name", "CInner", simpleName.getIdentifier());
		IBinding binding2 = simpleName.resolveBinding();
		assertNotNull("No binding", binding2);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24502
	 */
	public void test0437() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0437", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, false, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertProblemsSize(unit, 1, "The type CInner is not visible"); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		SimpleName simpleName = (SimpleName) name;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24511
	 */
	public void test0438() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0438", "D.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, false, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		List imports = unit.imports();
		assertEquals("Wrong size", 1, imports.size()); //$NON-NLS-1$<
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		IBinding binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24502
	 */
	public void test0439() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0439", "E.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, false, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24622
	 */
	public void test0440() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0440", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		// 2 * 3 + "" + (true)
		InfixExpression infixExpression = (InfixExpression) expression;
		checkSourceRange(infixExpression, "2 * 3 + \"\" + (true)", source);
		Expression leftOperand = infixExpression.getLeftOperand();
		checkSourceRange(leftOperand, "2 * 3 + \"\"", source);
		checkSourceRange(infixExpression.getRightOperand(), "(true)", source);
		assertEquals("wrong operator", infixExpression.getOperator(), InfixExpression.Operator.PLUS);
		assertEquals("wrong type", ASTNode.INFIX_EXPRESSION, leftOperand.getNodeType());
		infixExpression = (InfixExpression) leftOperand;
		checkSourceRange(infixExpression, "2 * 3 + \"\"", source);
		leftOperand = infixExpression.getLeftOperand();
		checkSourceRange(leftOperand, "2 * 3", source);
		checkSourceRange(infixExpression.getRightOperand(), "\"\"", source);
		assertEquals("wrong operator", infixExpression.getOperator(), InfixExpression.Operator.PLUS);
		assertEquals("wrong type", ASTNode.INFIX_EXPRESSION, leftOperand.getNodeType());
		infixExpression = (InfixExpression) leftOperand;
		checkSourceRange(infixExpression, "2 * 3", source);
		leftOperand = infixExpression.getLeftOperand();
		checkSourceRange(leftOperand, "2", source);
		checkSourceRange(infixExpression.getRightOperand(), "3", source);
		assertEquals("wrong operator", infixExpression.getOperator(), InfixExpression.Operator.TIMES);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24622
	 */
	public void test0441() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0441", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		checkSourceRange(infixExpression, "(2 + 2) * 3 * 1", source);
		Expression leftOperand = infixExpression.getLeftOperand();
		checkSourceRange(leftOperand, "(2 + 2)", source);
		checkSourceRange(infixExpression.getRightOperand(), "3", source);
		List extendedOperands = infixExpression.extendedOperands();
		assertEquals("wrong size", 1, extendedOperands.size());
		checkSourceRange((Expression) extendedOperands.get(0), "1", source);
		assertEquals("wrong operator", InfixExpression.Operator.TIMES, infixExpression.getOperator());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24622
	 */
	public void test0442() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0442", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		checkSourceRange(infixExpression, "2 + (2 * 3) + 1", source);
		Expression leftOperand = infixExpression.getLeftOperand();
		checkSourceRange(leftOperand, "2", source);
		Expression rightOperand = infixExpression.getRightOperand();
		checkSourceRange(rightOperand, "(2 * 3)", source);
		assertEquals("wrong type", ASTNode.PARENTHESIZED_EXPRESSION, rightOperand.getNodeType());
		List extendedOperands = infixExpression.extendedOperands();
		assertEquals("wrong size", 1, extendedOperands.size());
		checkSourceRange((Expression) extendedOperands.get(0), "1", source);
		assertEquals("wrong operator", InfixExpression.Operator.PLUS, infixExpression.getOperator());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24623
	 */
	public void test0443() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0443", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 3, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertNotNull("No body", methodDeclaration.getBody());
		assertNotNull("No binding", methodDeclaration.resolveBinding());
		assertTrue("Not an abstract method", Modifier.isAbstract(methodDeclaration.getModifiers()));
		List modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		Modifier modifier1 = (Modifier) modifiers.get(0);
		assertTrue("Not a public modifier", modifier1.isPublic());
		Modifier modifier2 = (Modifier) modifiers.get(1);
		assertTrue("Not an abstract modifier", modifier2.isAbstract());
		assertTrue("Not malformed", isMalformed(methodDeclaration));
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24623
	 */
	public void test0444() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0444", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 2, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0);
		assertEquals("Wrong type", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 2, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration)bodyDeclarations.get(0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		assertEquals("Wrong name", "foo", methodDeclaration.getName().getIdentifier());
		bodyDeclaration = (BodyDeclaration)bodyDeclarations.get(1);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		assertEquals("Wrong name", "foo", ((MethodDeclaration) bodyDeclaration).getName().getIdentifier());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24773
	 */
	public void test0445() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0445", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25018
	 */
	public void test0446() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0446", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 2, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25124
	 */
	public void test0447() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0447", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 3, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25330
	 */
	public void test0448() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0448", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", node.getNodeType(), ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
		assertNull("No return type", methodDeclaration.getReturnType2());
		Block block = methodDeclaration.getBody();
		assertNotNull("No method body", block);
		assertEquals("wrong size", 0, block.statements().size());
	}

	/**
	 * Check that the implicit super constructor call is not there
	 */
	public void test0449() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0449", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", node.getNodeType(), ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
		Block block = methodDeclaration.getBody();
		assertNotNull("No method body", block);
		assertEquals("wrong size", 1, block.statements().size());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26452
	 */
	public void test0450() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0450", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", node.getNodeType(), ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("not a class", typeBinding.isClass());
		assertTrue("not a toplevel type", typeBinding.isTopLevel());
		assertTrue("a local type", !typeBinding.isLocal());
		assertTrue("an anonymous type", !typeBinding.isAnonymous());
		assertTrue("a member type", !typeBinding.isMember());
		assertTrue("a nested type", !typeBinding.isNested());
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not an expression statement", node.getNodeType(), ASTNode.EXPRESSION_STATEMENT);
		Expression expression = ((ExpressionStatement) node).getExpression();
		assertEquals("Not a class instance creation", expression.getNodeType(), ASTNode.CLASS_INSTANCE_CREATION);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		typeBinding = anonymousClassDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("not a class", typeBinding.isClass());
		assertTrue("a toplevel type", !typeBinding.isTopLevel());
		assertTrue("not a local type", typeBinding.isLocal());
		assertTrue("not an anonymous type", typeBinding.isAnonymous());
		assertTrue("a member type", !typeBinding.isMember());
		assertTrue("not a nested type", typeBinding.isNested());
		ASTNode astNode = unit.findDeclaringNode(typeBinding);
		assertEquals("Wrong type", ASTNode.ANONYMOUS_CLASS_DECLARATION, astNode.getNodeType());
		assertNotNull("Didn't get a key", typeBinding.getKey());
		astNode = unit.findDeclaringNode(typeBinding.getKey());
		assertNotNull("Didn't get a declaring node", astNode);

		ITypeBinding typeBinding3 = classInstanceCreation.resolveTypeBinding();
		assertEquals("wrong binding", typeBinding, typeBinding3);

		List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size", 2, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("not a type declaration", bodyDeclaration.getNodeType() == ASTNode.TYPE_DECLARATION);
		typeDeclaration = (TypeDeclaration) bodyDeclaration;

		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(1);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertNotNull("No body", block);
		List statements = block.statements();
		assertEquals("wrong size", 2, statements.size());
		Statement statement = (Statement) statements.get(1);
		assertEquals("Not a variable declaration statement", statement.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
		Type type = variableDeclarationStatement.getType();
		assertNotNull("No type", type);

		ITypeBinding typeBinding2 = type.resolveBinding();
		typeBinding = typeDeclaration.resolveBinding();
		assertTrue("not equals", typeBinding == typeBinding2);
		assertNotNull("No type binding", typeBinding);
		assertTrue("not a class", typeBinding.isClass());
		assertTrue("a toplevel type", !typeBinding.isTopLevel());
		assertTrue("an anonymous type", !typeBinding.isAnonymous());
		assertTrue("not a member type", typeBinding.isMember());
		assertTrue("not a nested type", typeBinding.isNested());
		assertTrue("a local type", !typeBinding.isLocal());

		bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("not a type declaration", bodyDeclaration.getNodeType() == ASTNode.TYPE_DECLARATION);
		typeDeclaration = (TypeDeclaration) bodyDeclaration;
		typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("not a class", typeBinding.isClass());
		assertTrue("a toplevel type", !typeBinding.isTopLevel());
		assertTrue("an anonymous type", !typeBinding.isAnonymous());
		assertTrue("not a member type", typeBinding.isMember());
		assertTrue("not a nested type", typeBinding.isNested());
		assertTrue("a local type", !typeBinding.isLocal());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24916
	 */
	public void test0451() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0451", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 2, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType2();
		checkSourceRange(type, "int", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=27204
	 */
	public void test0452() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "", "NO_WORKING.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		SimpleName name = methodDeclaration.getName();
		assertEquals("wrong line number", 3, compilationUnit.getLineNumber(name.getStartPosition()));
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=27173
	 */
	public void test0453() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0453", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0,0);
		assertNotNull("No node", node);
		assertTrue("not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertTrue("not a super method invocation", expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION); //$NON-NLS-1$
		SuperMethodInvocation methodInvocation = (SuperMethodInvocation) expression;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong binding", "toString", methodBinding.getName());
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28296
	 */
	public void test0454() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0454", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0,1);
		assertNotNull("No node", node);
		assertTrue("not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("not a cast expression", expression.getNodeType() == ASTNode.CAST_EXPRESSION); //$NON-NLS-1$
		checkSourceRange(expression, "(int) (3.14f * a)", source);
		CastExpression castExpression = (CastExpression) expression;
		checkSourceRange(castExpression.getType(), "int", source);
		Expression expression2 = castExpression.getExpression();
		checkSourceRange(expression2, "(3.14f * a)", source);
		assertTrue("not a parenthesized expression", expression2.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION); //$NON-NLS-1$
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28682
	 */
	public void test0455() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0455", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a for statement", node.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) node; // first for loop
		String expectedSource = "for (int i = 0; i < 10; i++)  // for 1\n" +
			"	        for (int j = 0; j < 10; j++)  // for 2\n" +
			"	            if (true) { }";
		checkSourceRange(forStatement, expectedSource, source);
		Statement body = forStatement.getBody();
		expectedSource = "for (int j = 0; j < 10; j++)  // for 2\n" +
			"	            if (true) { }";
		checkSourceRange(body, expectedSource, source);
		assertTrue("not a for statement", body.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement2 = (ForStatement) body;
		body = forStatement2.getBody();
		expectedSource = "if (true) { }";
		checkSourceRange(body, expectedSource, source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28682
	 */
	public void test0456() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0456", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a for statement", node.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) node; // first for loop
		String expectedSource = "for (int x= 10; x < 20; x++)\n" +
			"			main();";
		checkSourceRange(forStatement, expectedSource, source);
		Statement body = forStatement.getBody();
		expectedSource = "main();";
		checkSourceRange(body, expectedSource, source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28682
	 */
	public void test0457() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0457", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a for statement", node.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) node; // first for loop
		String expectedSource = "for (int i= 10; i < 10; i++)/*[*/\n"+
			"			for (int z= 10; z < 10; z++)\n" +
			"				foo();";
		checkSourceRange(forStatement, expectedSource, source);
		Statement body = forStatement.getBody();
		expectedSource = "for (int z= 10; z < 10; z++)\n" +
			"				foo();";
		checkSourceRange(body, expectedSource, source);
		assertTrue("not a for statement", body.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement2 = (ForStatement) body;
		body = forStatement2.getBody();
		expectedSource = "foo();";
		checkSourceRange(body, expectedSource, source);
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28682
	 */
	public void test0458() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0458", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a for statement", node.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) node; // first for loop
		String expectedSource = "for (int i= 10; i < 10; i++)/*[*/\n"+
			"			for (int z= 10; z < 10; z++)\n" +
			"				;";
		checkSourceRange(forStatement, expectedSource, source);
		Statement body = forStatement.getBody();
		expectedSource = "for (int z= 10; z < 10; z++)\n" +
			"				;";
		checkSourceRange(body, expectedSource, source);
		assertTrue("not a for statement", body.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement2 = (ForStatement) body;
		body = forStatement2.getBody();
		expectedSource = ";";
		checkSourceRange(body, expectedSource, source);
		assertTrue("not an empty statement", body.getNodeType() == ASTNode.EMPTY_STATEMENT); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28682
	 */
	public void test0459() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0459", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a for statement", node.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) node; // first for loop
		String expectedSource = "for (int i= 10; i < 10; i++)/*[*/\n"+
			"			for (int z= 10; z < 10; z++)\n" +
			"				{    }";
		checkSourceRange(forStatement, expectedSource, source);
		Statement body = forStatement.getBody();
		expectedSource = "for (int z= 10; z < 10; z++)\n" +
			"				{    }";
		checkSourceRange(body, expectedSource, source);
		assertTrue("not a for statement", body.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement2 = (ForStatement) body;
		body = forStatement2.getBody();
		expectedSource = "{    }";
		checkSourceRange(body, expectedSource, source);
		assertTrue("not a block", body.getNodeType() == ASTNode.BLOCK); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28869
	 */
	public void test0460() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0460", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertTrue("Has error", compilationUnit.getProblems().length == 0); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("Malformed", !isMalformed(node));
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=28824
	 */
	public void test0461() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0461", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertTrue("Has error", compilationUnit.getProblems().length == 0); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("Malformed", !isMalformed(node));
		assertTrue("not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("not an assignment", expression.getNodeType() == ASTNode.ASSIGNMENT); //$NON-NLS-1$
		Assignment assignment = (Assignment) expression;
		checkSourceRange(assignment, "z= foo().y.toList()", source);
		Expression expression2 = assignment.getRightHandSide();
		checkSourceRange(expression2, "foo().y.toList()", source);
		assertTrue("not a method invocation", expression2.getNodeType() == ASTNode.METHOD_INVOCATION);
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		checkSourceRange(expression3, "foo().y", source);
		checkSourceRange(methodInvocation.getName(), "toList", source);
		assertTrue("not a field access", expression3.getNodeType() == ASTNode.FIELD_ACCESS);
		FieldAccess fieldAccess = (FieldAccess) expression3;
		checkSourceRange(fieldAccess.getName(), "y", source);
		Expression expression4 = fieldAccess.getExpression();
		checkSourceRange(expression4, "foo()", source);
		assertTrue("not a method invocation", expression4.getNodeType() == ASTNode.METHOD_INVOCATION);
		MethodInvocation methodInvocation2 = (MethodInvocation) expression4;
		checkSourceRange(methodInvocation2.getName(), "foo", source);
		assertNull("no null", methodInvocation2.getExpression());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=32338
	 */
	public void test0462() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "", "Test462.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertTrue("Has error", compilationUnit.getProblems().length == 0); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0);
		assertNotNull("No node", node);
		assertTrue("not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("Wrong name", "Test462", typeDeclaration.getName().getIdentifier());
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "Test462", typeBinding.getQualifiedName());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33450
	 */
	public void test0463() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0463", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertTrue("not a string literal", expression.getNodeType() == ASTNode.STRING_LITERAL); //$NON-NLS-1$
		StringLiteral stringLiteral = (StringLiteral) expression;
		checkSourceRange(stringLiteral, "\"\\012\\015\\u0061\"", source);
		assertEquals("wrong value", "\012\015a", stringLiteral.getLiteralValue());
		assertEquals("wrong value", "\"\\012\\015\\u0061\"", stringLiteral.getEscapedValue());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33039
	 */
	public void test0464() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0464", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("No error", 1, compilationUnit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No node", node);
		assertTrue("not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertTrue("not a null literal", expression.getNodeType() == ASTNode.NULL_LITERAL); //$NON-NLS-1$
		NullLiteral nullLiteral = (NullLiteral) expression;
		ITypeBinding typeBinding = nullLiteral.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertFalse("A primitive type", typeBinding.isPrimitive());
		assertTrue("Null type", typeBinding.isNullType());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33831
	 */
	public void test0465() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0465", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No node", node);
		assertTrue("not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertTrue("not a field access", expression.getNodeType() == ASTNode.FIELD_ACCESS); //$NON-NLS-1$
		FieldAccess fieldAccess = (FieldAccess) expression;
		Name name = fieldAccess.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "i", variableBinding.getName());
		assertEquals("Wrong type", "int", variableBinding.getType().getName());
		IVariableBinding variableBinding2 = fieldAccess.resolveFieldBinding();
		assertTrue("different binding", variableBinding == variableBinding2);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33949
	 */
	public void test0466() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		try {
			newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			JavaCore.setOptions(newOptions);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0466", "Assert.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(getJLS3(), sourceUnit, true);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			char[] source = sourceUnit.getSource().toCharArray();
			ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
			checkSourceRange(node, "assert ref != null : message;", source);
			assertTrue("not an assert statement", node.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
			AssertStatement statement = (AssertStatement) node;
			checkSourceRange(statement.getExpression(), "ref != null", source);
			checkSourceRange(statement.getMessage(), "message", source);
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33949
	 */
	public void test0467() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		try {
			newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			JavaCore.setOptions(newOptions);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0467", "Assert.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(getJLS3(), sourceUnit, true);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			char[] source = sourceUnit.getSource().toCharArray();
			ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
			checkSourceRange(node, "assert ref != null : message\\u003B", source);
			assertTrue("not an assert statement", node.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
			AssertStatement statement = (AssertStatement) node;
			checkSourceRange(statement.getExpression(), "ref != null", source);
			checkSourceRange(statement.getMessage(), "message", source);

			node = getASTNode(compilationUnit, 0, 0, 1);
			checkSourceRange(node, "assert ref != null\\u003B", source);
			assertTrue("not an assert statement", node.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
			statement = (AssertStatement) node;
			checkSourceRange(statement.getExpression(), "ref != null", source);
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=36772
	 */
	public void test0468() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0468", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No node", node);
		assertTrue("not a return statement", node.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertTrue("not a field access", expression.getNodeType() == ASTNode.FIELD_ACCESS); //$NON-NLS-1$
		FieldAccess fieldAccess = (FieldAccess) expression;
		Name name = fieldAccess.getName();
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "i", variableBinding.getName());
		assertEquals("Wrong type", "int", variableBinding.getType().getName());
		IVariableBinding variableBinding2 = fieldAccess.resolveFieldBinding();
		assertTrue("different binding", variableBinding == variableBinding2);

		node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("No node", node);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);

		ASTNode foundNode = compilationUnit.findDeclaringNode(variableBinding);
		assertNotNull("No found node", foundNode);
		assertEquals("wrong node", fragment, foundNode);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=36895
	 */
	public void test0469() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "codeManipulation", "bug.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 2, 0);
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No node", node);
		assertTrue("not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		ASTNode parent = node.getParent();
		assertNotNull(parent);
		assertTrue("not a block", parent.getNodeType() == ASTNode.BLOCK); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=37381
	 */
	public void test0470() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0470", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No node", node);
		assertTrue("not a for statement", node.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) node;
		List initializers = forStatement.initializers();
		assertEquals("wrong size", 1, initializers.size());
		Expression initializer = (Expression) initializers.get(0);
		assertTrue("not a variable declaration expression", initializer.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION); //$NON-NLS-1$
		VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) initializer;
		List fragments = variableDeclarationExpression.fragments();
		assertEquals("wrong size", 2, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		checkSourceRange(fragment, "i= 0", source);
		fragment = (VariableDeclarationFragment) fragments.get(1);
		checkSourceRange(fragment, "j= goo(3)", source);
		checkSourceRange(variableDeclarationExpression, "int i= 0, j= goo(3)", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=38447
	 */
	public void test0471() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0471", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No error", 1, compilationUnit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Is a constructor", !methodDeclaration.isConstructor());
		checkSourceRange(methodDeclaration, "private void foo(){", source, true/*expectMalformed*/);
		node = getASTNode(compilationUnit, 0, 1);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		assertTrue("Is a constructor", !methodDeclaration.isConstructor());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=38447
	 */
	public void test0472() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "junit.textui", "ResultPrinter.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No error", 2, compilationUnit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 2);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=38732
	 */
	public void test0473() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		try {
			newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			newOptions.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
			JavaCore.setOptions(newOptions);

			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0473", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			char[] source = sourceUnit.getSource().toCharArray();
			ASTNode result = runConversion(getJLS3(), sourceUnit, true);
			CompilationUnit compilationUnit = (CompilationUnit) result;
			assertEquals("No error", 2, compilationUnit.getProblems().length); //$NON-NLS-1$
			ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
			assertNotNull("No node", node);
			assertTrue("not an assert statement", node.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
			AssertStatement assertStatement = (AssertStatement) node;
			checkSourceRange(assertStatement, "assert(true);", source);
			Expression expression = assertStatement.getExpression();
			checkSourceRange(expression, "(true)", source);
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=39259
	 */
	public void test0474() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0474", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertNotNull("No node", node);
		assertEquals("Not a while statement", node.getNodeType(), ASTNode.WHILE_STATEMENT);
		WhileStatement whileStatement = (WhileStatement) node;
		Statement statement = whileStatement.getBody();
		assertEquals("Not a while statement", statement.getNodeType(), ASTNode.WHILE_STATEMENT);
		WhileStatement whileStatement2 = (WhileStatement) statement;
		String expectedSource =
			"while(b())\n" +
			"				foo();";
		checkSourceRange(whileStatement2, expectedSource, source);
		Statement statement2 = whileStatement2.getBody();
		checkSourceRange(statement2, "foo();", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=39259
	 */
	public void test0475() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0475", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertNotNull("No node", node);
		assertEquals("Not an if statement", node.getNodeType(), ASTNode.IF_STATEMENT);
		IfStatement statement = (IfStatement) node;
		Statement statement2 = statement.getThenStatement();
		assertEquals("Not an if statement", statement2.getNodeType(), ASTNode.IF_STATEMENT);
		IfStatement statement3 = (IfStatement) statement2;
		String expectedSource =
			"if(b())\n" +
			"				foo();";
		checkSourceRange(statement3, expectedSource, source);
		Statement statement4 = statement3.getThenStatement();
		checkSourceRange(statement4, "foo();", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=39259
	 */
	public void test0476() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0476", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No error", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertNotNull("No node", node);
		assertEquals("Not a for statement", node.getNodeType(), ASTNode.FOR_STATEMENT);
		ForStatement statement = (ForStatement) node;
		Statement statement2 = statement.getBody();
		assertEquals("Not a for statement", statement2.getNodeType(), ASTNode.FOR_STATEMENT);
		ForStatement statement3 = (ForStatement) statement2;
		String expectedSource =
			"for(;b();)\n" +
			"				foo();";
		checkSourceRange(statement3, expectedSource, source);
		Statement statement4 = statement3.getBody();
		checkSourceRange(statement4, "foo();", source);
	}


	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=39327
	 */
	public void test0477() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0477", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("No error", 1, compilationUnit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertNotNull("No node", node);

		checkSourceRange(node, "this(undef());", source);
		assertEquals("Not a constructor invocation", node.getNodeType(), ASTNode.CONSTRUCTOR_INVOCATION);
		ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
		List arguments = constructorInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		IMethodBinding binding = constructorInvocation.resolveConstructorBinding();
		assertNotNull("No binding", binding);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0478() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0478", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 2, types.length);
		IType type = types[1];
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull(methodBinding);
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 2, statements.size());
		ASTNode node2 = (ASTNode) statements.get(1);
		assertNotNull(node2);
		assertTrue("Not an expression statement", node2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("wrong type", binding.getKind() == IBinding.VARIABLE); //$NON-NLS-1$
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "a", variableBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("no binding2", binding2); //$NON-NLS-1$
		assertTrue("Wrong type", binding2.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding2 = (IMethodBinding) binding2;
		assertEquals("Wrong name", "clone", methodBinding2.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0479() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0479", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 2, types.length);
		IType type = types[1];
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 2, statements.size());
		ASTNode node2 = (ASTNode) statements.get(1);
		assertNotNull(node2);
		assertTrue("Not an expression statement", node2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNull("No binding", binding); //$NON-NLS-1$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0480() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0480", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0481() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0481", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node2;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("Not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
		assertNotNull(typeBinding);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0482() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0482", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IType[] memberTypes = type.getTypes();
		assertNotNull(memberTypes);
		assertEquals("wrong size", 1, memberTypes.length);
		IType memberType = memberTypes[0];
		IMethod[] methods = memberType.getMethods();
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node2;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("Not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
		assertNotNull(typeBinding);
		assertTrue(typeBinding.isAnonymous());
		assertEquals("Wrong name", "", typeBinding.getName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0483() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0483", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "A", methodDeclaration.getName().getIdentifier());
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
		IBinding binding = methodDeclaration.getName().resolveBinding();
		assertNotNull(binding);
		assertEquals("Wrong type", IBinding.METHOD, binding.getKind());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node2;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("Not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
		assertNotNull(typeBinding);
		assertTrue(typeBinding.isAnonymous());
		assertEquals("Wrong name", "", typeBinding.getName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0484() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0482", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IType[] memberTypes = type.getTypes();
		assertNotNull(memberTypes);
		assertEquals("wrong size", 1, memberTypes.length);
		IType memberType = memberTypes[0];
		ISourceRange sourceRange = memberType.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("wrong name", "B", typeDeclaration.getName().getIdentifier());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0485() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0482", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IType[] memberTypes = type.getTypes();
		assertNotNull(memberTypes);
		assertEquals("wrong size", 1, memberTypes.length);
		IType memberType = memberTypes[0];
		ISourceRange sourceRange = memberType.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("wrong name", "B", typeDeclaration.getName().getIdentifier());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0486() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0486", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 2, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0487() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0487", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 3, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$

		ASTNode node = getASTNode((CompilationUnit) result, 0, 5);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 4);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Wrong name", "field", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Wrong name", "i", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		Initializer initializer = (Initializer) node;
		assertEquals("Not static", Modifier.NONE, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 3);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		initializer = (Initializer) node;
		assertEquals("Not static", Modifier.STATIC, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 6);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=40474
	 */
	public void test0488() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0488", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IInitializer[] initializers = type.getInitializers();
		assertEquals("wrong size", 2, initializers.length);
		IInitializer init = initializers[1];
		ISourceRange sourceRange = init.getSourceRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$

		ASTNode node = getASTNode((CompilationUnit) result, 0, 5);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 4);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Wrong name", "field", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Wrong name", "i", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		Initializer initializer = (Initializer) node;
		assertEquals("Not static", Modifier.NONE, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 3);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		initializer = (Initializer) node;
		assertEquals("Not static", Modifier.STATIC, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 6);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40804
	 */
	public void test0489() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0489", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 3, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertNotNull("No type binding", typeDeclaration.resolveBinding());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40804
	 */
	public void test0490() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0490", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42647
	 */
	public void test0491() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		try {
			newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			JavaCore.setOptions(newOptions);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0491", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			char[] source = sourceUnit.getSource().toCharArray();
			ASTNode result = runConversion(getJLS3(), sourceUnit, true);
			assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
			CompilationUnit unit = (CompilationUnit) result;
			assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
			ASTNode node = getASTNode(unit, 0, 0, 0);
			assertTrue("not an assert statement", node.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
			AssertStatement assertStatement = (AssertStatement) node;
			Expression expression = assertStatement.getExpression();
			assertTrue("not a parenthesized expression", expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION); //$NON-NLS-1$
			checkSourceRange(expression, "(loginName != null)", source);
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42647
	 */
	public void test0492() throws JavaModelException {
		Hashtable options = JavaCore.getOptions();
		Hashtable newOptions = JavaCore.getOptions();
		try {
			newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
			JavaCore.setOptions(newOptions);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0492", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			char[] source = sourceUnit.getSource().toCharArray();
			ASTNode result = runConversion(getJLS3(), sourceUnit, true);
			assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
			CompilationUnit unit = (CompilationUnit) result;
			assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
			ASTNode node = getASTNode(unit, 0, 0, 0);
			assertTrue("not an assert statement", node.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
			AssertStatement assertStatement = (AssertStatement) node;
			Expression expression = assertStatement.getExpression();
			checkSourceRange(expression, "loginName != null", source);
		} finally {
			JavaCore.setOptions(options);
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42839
	 */
	public void test0493() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0493", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Class[][]", source);
		assertTrue("not an array type", type.isArrayType()); //$NON-NLS-1$
		ArrayType arrayType = (ArrayType) type;
		Type componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("is an array type", !componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42839
	 */
	public void test0494() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0494", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Class[][][]", source);
		assertTrue("not an array type", type.isArrayType()); //$NON-NLS-1$
		ArrayType arrayType = (ArrayType) type;
		Type componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[][]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("is an array type", !componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42839
	 */
	public void test0495() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0495", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Class[][]", source);
		assertTrue("not an array type", type.isArrayType()); //$NON-NLS-1$
		ArrayType arrayType = (ArrayType) type;
		Type componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("is an array type", !componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class", source);
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		assertEquals("wrong extra dimension", 1, fragment.getExtraDimensions());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42839
	 */
	public void test0496() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0496", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Class[][][][]", source);
		assertTrue("not an array type", type.isArrayType()); //$NON-NLS-1$
		ArrayType arrayType = (ArrayType) type;
		Type componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[][][]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[][]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("not an array type", componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class[]", source);
		arrayType = (ArrayType) componentType;
		componentType = componentType(arrayType);
		assertTrue("is an array type", !componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class", source);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=42839
	 */
	public void test0497() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0497", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Class[]", source);
		assertTrue("not an array type", type.isArrayType()); //$NON-NLS-1$
		ArrayType arrayType = (ArrayType) type;
		Type componentType = componentType(arrayType);
		assertTrue("is an array type", !componentType.isArrayType()); //$NON-NLS-1$
		checkSourceRange(componentType, "Class", source);
	}

	/**
	 */
	public void test0498() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0498", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=45199
	 */
	public void test0499() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0499", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 1);
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		Expression expression = ((ExpressionStatement) node).getExpression();
		assertTrue("Not an assignment", expression.getNodeType() == ASTNode.ASSIGNMENT); //$NON-NLS-1$
		Assignment assignment = (Assignment) expression;
		Expression expression2 = assignment.getRightHandSide();
		assertTrue("Not an infix expression", expression2.getNodeType() == ASTNode.INFIX_EXPRESSION); //$NON-NLS-1$
		InfixExpression infixExpression = (InfixExpression) expression2;
		Expression expression3 = infixExpression.getLeftOperand();
		assertTrue("Not a simple name", expression3.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		ITypeBinding binding = expression3.resolveTypeBinding();
		assertNotNull("No binding", binding);
		Expression expression4 = assignment.getLeftHandSide();
		assertTrue("Not a simple name", expression4.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		ITypeBinding binding2 = expression4.resolveTypeBinding();
		assertNotNull("No binding", binding2);
		assertTrue("Should be the same", binding == binding2);
	}

	/**
	 * Test for bug 45436 fix.
	 * When this bug happened, the first assertion was false (2 problems found).
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45436">bug 45436</a>
	 * @throws JavaModelException
	 */
	public void test0500() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0500", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IJavaProject project = sourceUnit.getJavaProject();
		Map originalOptions = project.getOptions(false);
		try {
			project.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.ERROR);
			project.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.ERROR);
			project.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.ERROR);
			CompilationUnit result = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);
			IProblem[] problems= result.getProblems();
			assertTrue(problems.length == 1);
			assertEquals("Invalid warning", "Javadoc: Missing tag for parameter a", problems[0].getMessage());
		} finally {
			project.setOptions(originalOptions);
		}
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46012
	 */
	public void test0501() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0501", "JavaEditor.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertNotNull(result);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502a() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'i' in initializer
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)getASTNode(unit, 0, 0, 0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		IVariableBinding localBinding = fragment.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A;#0#i", localBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502b() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'j' in 'then' block in initializer
		IfStatement ifStatement = (IfStatement) getASTNode(unit, 0, 0, 1);
		Block block = (Block)ifStatement.getThenStatement();
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) block.statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		IVariableBinding localBinding = fragment.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A;#0#0#j", localBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502c() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'i' in 'foo()'
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)getASTNode(unit, 0, 1, 0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		IVariableBinding localBinding = fragment.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A;.foo()V#i", localBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502d() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'j' in 'then' block in 'foo()'
		IfStatement ifStatement = (IfStatement) getASTNode(unit, 0, 1, 1);
		Block block = (Block)ifStatement.getThenStatement();
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) block.statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		IVariableBinding localBinding = fragment.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A;.foo()V#0#j", localBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502e() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'j' in 'else' block in 'foo()'
		IfStatement ifStatement = (IfStatement) getASTNode(unit, 0, 1, 1);
		Block block = (Block)ifStatement.getElseStatement();
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) block.statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		IVariableBinding localBinding = fragment.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A;.foo()V#1#j", localBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502f() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// first 'new Object(){...}' in 'foo()'
		ExpressionStatement expressionStatement = (ExpressionStatement) getASTNode(unit, 0, 1, 2);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		ITypeBinding typeBinding = anonymousClassDeclaration.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A$182;", typeBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502g() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'B' in 'foo()'
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) getASTNode(unit, 0, 1, 3);
		AbstractTypeDeclaration typeDeclaration = typeDeclarationStatement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A$206$B;", typeBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502h() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// second 'new Object(){...}' in 'foo()'
		ExpressionStatement expressionStatement = (ExpressionStatement) getASTNode(unit, 0, 1, 4);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		ITypeBinding typeBinding = anonymousClassDeclaration.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A$255;", typeBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502i() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'field' in 'B' in 'foo()'
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) getASTNode(unit, 0, 1, 3);
		AbstractTypeDeclaration abstractTypeDeclaration = typeDeclarationStatement.getDeclaration();
		assertEquals("Wrong type", abstractTypeDeclaration.getNodeType(), ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) abstractTypeDeclaration;
		FieldDeclaration fieldDeclaration = typeDeclaration.getFields()[0];
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		IVariableBinding fieldBinding = fragment.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A$206$B;.field)I", fieldBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46013
	 */
	public void test0502j() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0502", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// 'bar()' in 'B' in 'foo()'
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) getASTNode(unit, 0, 1, 3);
		AbstractTypeDeclaration abstractTypeDeclaration = typeDeclarationStatement.getDeclaration();
		assertEquals("Wrong type", abstractTypeDeclaration.getNodeType(), ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) abstractTypeDeclaration;
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertEquals("Unexpected key", "Ltest0502/A$206$B;.bar()V", methodBinding.getKey()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503a() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// top level type A
		TypeDeclaration type = (TypeDeclaration)getASTNode(unit, 0);
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503b() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// member type B in A
		TypeDeclaration type = (TypeDeclaration)getASTNode(unit, 0, 0);
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$B", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503c() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// local type E in foo() in A
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) getASTNode(unit, 0, 1, 0);
		AbstractTypeDeclaration typeDeclaration = typeDeclarationStatement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$1$E", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503d() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// anonymous type new Object() {...} in foo() in A
		ExpressionStatement expressionStatement = (ExpressionStatement) getASTNode(unit, 0, 1, 1);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		ITypeBinding typeBinding = anonymousClassDeclaration.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$2", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503e() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// type F in anonymous type new Object() {...} in foo() in A
		ExpressionStatement expressionStatement = (ExpressionStatement) getASTNode(unit, 0, 1, 1);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		TypeDeclaration type = (TypeDeclaration) anonymousClassDeclaration.bodyDeclarations().get(0);
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$2$F", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503f() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// local type C in bar() in B in A
		MethodDeclaration method = (MethodDeclaration) getASTNode(unit, 0, 0, 0);
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) method.getBody().statements().get(0);
		AbstractTypeDeclaration typeDeclaration = typeDeclarationStatement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$1$C", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503g() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// anonymous type new Object() {...} in bar() in B in A
		MethodDeclaration method = (MethodDeclaration) getASTNode(unit, 0, 0, 0);
		ExpressionStatement expressionStatement = (ExpressionStatement) method.getBody().statements().get(1);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		ITypeBinding typeBinding = anonymousClassDeclaration.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$1", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503h() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// type D in anonymous type new Object() {...} in bar() in B in A
		MethodDeclaration method = (MethodDeclaration) getASTNode(unit, 0, 0, 0);
		ExpressionStatement expressionStatement = (ExpressionStatement) method.getBody().statements().get(1);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		TypeDeclaration type = (TypeDeclaration) anonymousClassDeclaration.bodyDeclarations().get(0);
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Unexpected binary name", "test0503.A$1$D", typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=46057
	 */
	public void test0503i() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0503", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit unit = (CompilationUnit)runConversion(getJLS3(), sourceUnit, true);

		// unreachable type G in foo() in A
		IfStatement ifStatement = (IfStatement) getASTNode(unit, 0, 1, 2);
		Block block = (Block)ifStatement.getThenStatement();
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) block.statements().get(0);
		AbstractTypeDeclaration typeDeclaration = typeDeclarationStatement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Unexpected binary name", null, typeBinding.getBinaryName()); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=47396
	 */
	public void test0504() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0504", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0);
		assertNotNull(node);
		assertTrue("Not a constructor declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration declaration = (MethodDeclaration) node;
		assertTrue("A constructor", !declaration.isConstructor());
		checkSourceRange(declaration, "public method(final int parameter);", source, true/*expectMalformed*/);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=47396
	 */
	public void test0505() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0505", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0);
		assertNotNull(node);
		assertTrue("Not a constructor declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration declaration = (MethodDeclaration) node;
		assertTrue("A constructor", !declaration.isConstructor());
		checkSourceRange(declaration, "public method(final int parameter) {     }", source, true/*expectMalformed*/);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=46699
	 */
	public void test0506() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0506", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong number of problems", 0, (unit).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		assertTrue("Not a class instance creation", expressionStatement.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertFalse("is synthetic", binding.isSynthetic());
		assertTrue("is default constructor", binding.isDefaultConstructor());
		assertNull("Has a declaring node", unit.findDeclaringNode(binding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=46699
	 */
	public void test0507() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0507", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong number of problems", 0, (unit).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		assertTrue("Not a class instance creation", expressionStatement.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertFalse("is synthetic", binding.isSynthetic());
		assertTrue("is default constructor", binding.isDefaultConstructor());
		assertNull("Has a declaring node", unit.findDeclaringNode(binding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=46699
	 */
	public void test0508() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0508", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 1, 0);
		assertEquals("Wrong number of problems", 0, (unit).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		assertTrue("Not a class instance creation", expressionStatement.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertFalse("is synthetic", binding.isSynthetic());
		assertTrue("not a default constructor", !binding.isDefaultConstructor());
		assertNotNull("Has no declaring node", unit.findDeclaringNode(binding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=46699
	 */
	public void test0509() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0509", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong number of problems", 0, (unit).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		assertTrue("Not a class instance creation", expressionStatement.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertFalse("is synthetic", binding.isSynthetic());
		assertTrue("not a default constructor", !binding.isDefaultConstructor());
		assertNull("Has a declaring node", unit.findDeclaringNode(binding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=46699
	 */
	public void test0510() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0510", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong number of problems", 0, (unit).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		assertTrue("Not a class instance creation", expressionStatement.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertFalse("is synthetic", binding.isSynthetic());
		assertFalse("is default constructor", binding.isDefaultConstructor());
		assertNull("Has a declaring node", unit.findDeclaringNode(binding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=46699
	 */
	public void test0511() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0511", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong number of problems", 0, (unit).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		assertTrue("Not a class instance creation", expressionStatement.getExpression().getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expressionStatement.getExpression();
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertFalse("is synthetic", binding.isSynthetic());
		assertFalse("is default constructor", binding.isDefaultConstructor());
		assertNull("Has a declaring node", unit.findDeclaringNode(binding));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=47326
	 */
	public void test0512() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0512", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Wrong number of problems", 2, unit.getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration declaration = (MethodDeclaration) node;
		assertTrue("Not a constructor", declaration.isConstructor());
		checkSourceRange(declaration, "public A();", source, true/*expectMalformed*/);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=49429
	 */
	public void test0513() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0513", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
	}


	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48502
	 */
	public void test0514() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0514", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=49204
	 */
	public void test0515() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0515", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a if statement", node.getNodeType() == ASTNode.IF_STATEMENT);
		IfStatement ifStatement = (IfStatement) node;
		assertTrue("not an empty statement", ifStatement.getThenStatement().getNodeType() == ASTNode.EMPTY_STATEMENT);
		checkSourceRange(ifStatement.getThenStatement(), ";", source);
		Statement statement = ifStatement.getElseStatement();
		assertTrue("not a if statement", statement.getNodeType() == ASTNode.IF_STATEMENT);
		ifStatement = (IfStatement) statement;
		assertTrue("not an empty statement", ifStatement.getThenStatement().getNodeType() == ASTNode.EMPTY_STATEMENT);
		checkSourceRange(ifStatement.getThenStatement(), ";", source);
		Statement statement2 = ifStatement.getElseStatement();
		assertTrue("not an empty statement", statement2.getNodeType() == ASTNode.EMPTY_STATEMENT);
		checkSourceRange(statement2, ";", source);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 */
	public void test0516() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0516", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration declaration = (MethodDeclaration) node;
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		parser.setSourceRange(declaration.getStartPosition(), declaration.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a type declaration", result2.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) result2;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue(declaration.subtreeMatch(new ASTMatcher(), bodyDeclaration));
		ASTNode root = bodyDeclaration.getRoot();
		assertNotNull("No root", root);
		assertTrue("not a compilation unit", root.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) root;
		assertEquals("wrong problem size", 0, compilationUnit.getProblems().length);
		assertNotNull("No comments", compilationUnit.getCommentList());
		assertEquals("Wrong size", 3, compilationUnit.getCommentList().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 */
	public void test0517() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0517", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No comments", unit.getCommentList());
		assertEquals("Wrong size", 3, unit.getCommentList().size());
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration declaration = (FieldDeclaration) node;
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		parser.setSourceRange(declaration.getStartPosition(), declaration.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a type declaration", result2.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) result2;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue(declaration.subtreeMatch(new ASTMatcher(), bodyDeclaration));
		ASTNode root = bodyDeclaration.getRoot();
		assertNotNull("No root", root);
		assertTrue("not a compilation unit", root.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) root;
		assertEquals("wrong problem size", 0, compilationUnit.getProblems().length);
		assertNotNull("No comments", compilationUnit.getCommentList());
		assertEquals("Wrong size", 2, compilationUnit.getCommentList().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 */
	public void test0518() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0518", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not an initializer", node.getNodeType() == ASTNode.INITIALIZER);
		Initializer declaration = (Initializer) node;
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		parser.setSourceRange(declaration.getStartPosition(), declaration.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a type declaration", result2.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) result2;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue(declaration.subtreeMatch(new ASTMatcher(), bodyDeclaration));
		ASTNode root = bodyDeclaration.getRoot();
		assertNotNull("No root", root);
		assertTrue("not a compilation unit", root.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) root;
		assertEquals("wrong problem size", 0, compilationUnit.getProblems().length);
		assertNotNull("No comments", compilationUnit.getCommentList());
		assertEquals("Wrong size", 3, compilationUnit.getCommentList().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 */
	public void test0519() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0519", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No comments", unit.getCommentList());
		assertEquals("Wrong size", 2, unit.getCommentList().size());
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		ASTNode statement = node;
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(source);
		parser.setSourceRange(statement.getStartPosition(), statement.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a block", result2.getNodeType() == ASTNode.BLOCK);
		Block block = (Block) result2;
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size());
		Statement statement2 = (Statement) statements.get(0);
		assertTrue(statement.subtreeMatch(new ASTMatcher(), statement2));
		ASTNode root = statement2.getRoot();
		assertNotNull("No root", root);
		assertTrue("not a compilation unit", root.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) root;
		assertEquals("wrong problem size", 0, compilationUnit.getProblems().length);
		assertNotNull("No comments", compilationUnit.getCommentList());
		assertEquals("Wrong size", 1, compilationUnit.getCommentList().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 */
	public void test0520() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0520", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		assertNotNull("No comments", unit.getCommentList());
		assertEquals("Wrong size", 2, unit.getCommentList().size());
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a block", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(source);
		parser.setSourceRange(expression.getStartPosition(), expression.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a method invocation", result2.getNodeType() == ASTNode.METHOD_INVOCATION);
		assertTrue(expression.subtreeMatch(new ASTMatcher(), result2));
		ASTNode root = result2.getRoot();
		assertNotNull("No root", root);
		assertTrue("not a compilation unit", root.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) root;
		assertEquals("wrong problem size", 0, compilationUnit.getProblems().length);
		assertNotNull("No comments", compilationUnit.getCommentList());
		assertEquals("Wrong size", 1, compilationUnit.getCommentList().size());
	}
	/**
	 * Ensure an OperationCanceledException is correcly thrown when progress monitor is canceled
	 * @deprecated using deprecated code
	 */
	public void test0521() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0521", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// count the number of time isCanceled() is called when converting this source unit
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		CancelCounter counter = new CancelCounter();
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(sourceUnit);
		parser.setResolveBindings(true);
		parser.setWorkingCopyOwner(owner);
		parser.createAST(counter);

		// throw an OperatonCanceledException at each point isCanceled() is called
		for (int i = 0; i < counter.count; i++) {
			boolean gotException = false;
			try {
				parser = ASTParser.newParser(AST.JLS2);
				parser.setSource(sourceUnit);
				parser.setResolveBindings(true);
				parser.setWorkingCopyOwner(owner);
				parser.createAST(new Canceler(i));
			} catch (OperationCanceledException e) {
				gotException = true;
			}
			assertTrue("Should get an OperationCanceledException (" + i + ")", gotException);
		}

		// last should not throw an OperationCanceledException
		parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(sourceUnit);
		parser.setResolveBindings(true);
		parser.setWorkingCopyOwner(owner);
		parser.createAST(new Canceler(counter.count));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0522() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0522", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		assertNotNull(type);
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 2, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull(methodBinding);
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 2, statements.size());
		ASTNode node2 = (ASTNode) statements.get(1);
		assertNotNull(node2);
		assertTrue("Not an expression statement", node2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNotNull("No binding", binding); //$NON-NLS-1$
		assertTrue("wrong type", binding.getKind() == IBinding.VARIABLE); //$NON-NLS-1$
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("Wrong name", "a", variableBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
		IBinding binding2 = simpleName2.resolveBinding();
		assertNotNull("no binding2", binding2); //$NON-NLS-1$
		assertTrue("Wrong type", binding2.getKind() == IBinding.METHOD); //$NON-NLS-1$
		IMethodBinding methodBinding2 = (IMethodBinding) binding2;
		assertEquals("Wrong name", "clone", methodBinding2.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0523() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0523", "Test.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		assertNotNull(type);
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 2, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 2, statements.size());
		ASTNode node2 = (ASTNode) statements.get(1);
		assertNotNull(node2);
		assertTrue("Not an expression statement", node2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) node2;
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertTrue("Not a simple name", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		IBinding binding  = simpleName.resolveBinding();
		assertNull("No binding", binding); //$NON-NLS-1$
		SimpleName simpleName2 = methodInvocation.getName();
		assertEquals("Wrong name", "clone", simpleName2.getIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0524() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0524", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		assertNotNull(type);
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 2, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0525() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0525", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		assertNotNull(type);
		IMethod[] methods = type.getMethods();
		assertNotNull(methods);
		assertEquals("wrong size", 2, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node2;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("Not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
		assertNotNull(typeBinding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0526() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0526", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		assertNotNull(type);
		IType[] memberTypes = type.getTypes();
		assertNotNull(memberTypes);
		assertEquals("wrong size", 1, memberTypes.length);
		IType memberType = memberTypes[0];
		IMethod[] methods = memberType.getMethods();
		assertEquals("wrong size", 2, methods.length);
		IMethod method = methods[1];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "test", methodDeclaration.getName().getIdentifier());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node2;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("Not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
		assertNotNull(typeBinding);
		assertTrue(typeBinding.isAnonymous());
		assertEquals("Wrong name", "", typeBinding.getName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0527() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0527", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		assertNotNull(type);
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("wrong name", "A", methodDeclaration.getName().getIdentifier());
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
		IBinding binding = methodDeclaration.getName().resolveBinding();
		assertNotNull(binding);
		assertEquals("Wrong type", IBinding.METHOD, binding.getKind());
		List statements = ((MethodDeclaration) node).getBody().statements();
		assertEquals("wrong size", 1, statements.size());
		ASTNode node2 = (ASTNode) statements.get(0);
		assertNotNull(node2);
		assertTrue("Not an variable declaration statement", node2.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node2;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertTrue("Not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
		assertNotNull(typeBinding);
		assertTrue(typeBinding.isAnonymous());
		assertEquals("Wrong name", "", typeBinding.getName());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0528() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0528", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		IType[] memberTypes = type.getTypes();
		assertNotNull(memberTypes);
		assertEquals("wrong size", 1, memberTypes.length);
		IType memberType = memberTypes[0];
		ISourceRange sourceRange = memberType.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("wrong name", "B", typeDeclaration.getName().getIdentifier());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0529() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0529", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		IType[] memberTypes = type.getTypes();
		assertNotNull(memberTypes);
		assertEquals("wrong size", 1, memberTypes.length);
		IType memberType = memberTypes[0];
		ISourceRange sourceRange = memberType.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("wrong name", "B", typeDeclaration.getName().getIdentifier());
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertTrue("Not a method declaration", bodyDeclaration.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0530() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0530", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 3, methods.length);
		IMethod method = methods[2];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0531() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0531", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		IType type = classFile.getType();
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 5, methods.length);
		IMethod method = methods[3];
		ISourceRange sourceRange = method.getSourceRange();
		ASTNode result = runConversion(getJLS3(), classFile, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$

		ASTNode node = getASTNode((CompilationUnit) result, 0, 5);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 4);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Wrong name", "field", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Wrong name", "i", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		Initializer initializer = (Initializer) node;
		assertEquals("Not static", Modifier.NONE, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 3);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		initializer = (Initializer) node;
		assertEquals("Not static", Modifier.STATIC, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 6);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48292
	 */
	public void test0532() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0488", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IInitializer[] initializers = type.getInitializers();
		assertEquals("wrong size", 2, initializers.length);
		IInitializer init = initializers[1];
		ISourceRange sourceRange = init.getSourceRange();
		int position = sourceRange.getOffset() + sourceRange.getLength() / 2;

		IOrdinaryClassFile classFile = getClassFile("Converter" , "bins", "test0532", "A.class"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertNotNull(classFile);
		assertNotNull(classFile.getSource());
		type = classFile.getType();
		initializers = type.getInitializers();
		assertEquals("wrong size", 0, initializers.length);
		ASTNode result = runConversion(getJLS3(), classFile, position, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$

		ASTNode node = getASTNode((CompilationUnit) result, 0, 5);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 4);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 0);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Wrong name", "field", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 1);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Wrong name", "i", fragment.getName().getIdentifier());
		assertNotNull("No initializer", expression);

		node = getASTNode((CompilationUnit) result, 0, 2);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		Initializer initializer = (Initializer) node;
		assertEquals("Not static", Modifier.NONE, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 3);
		assertTrue("Not an initializer", node.getNodeType() == ASTNode.INITIALIZER); //$NON-NLS-1$
		initializer = (Initializer) node;
		assertEquals("Not static", Modifier.STATIC, initializer.getModifiers());
		block = initializer.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());

		node = getASTNode((CompilationUnit) result, 0, 6);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		methodDeclaration = (MethodDeclaration) node;
		block = methodDeclaration.getBody();
		statements = block.statements();
		assertEquals("Wrong size", 0, statements.size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 * @deprecated using deprecated code
	 */
	public void test0533() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0533", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration declaration = (MethodDeclaration) node;
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		parser.setSourceRange(declaration.getStartPosition(), declaration.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a compilation unit", result2.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result2;
		assertEquals("wrong problem size", 1, compilationUnit.getProblems().length);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 * @deprecated using deprecated code
	 */
	public void test0534() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0534", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration declaration = (FieldDeclaration) node;
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		parser.setSourceRange(declaration.getStartPosition(), declaration.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a compilation unit", result2.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result2;
		assertEquals("wrong problem size", 1, compilationUnit.getProblems().length);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=48489
	 * @deprecated using deprecated code
	 */
	public void test0535() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0535", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("No node", node);
		assertTrue("not an initializer", node.getNodeType() == ASTNode.INITIALIZER);
		Initializer declaration = (Initializer) node;
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		parser.setSourceRange(declaration.getStartPosition(), declaration.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a compilation unit", result2.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result2;
		assertEquals("wrong problem size", 1, compilationUnit.getProblems().length);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=47396
	 */
	public void test0536() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0536", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertNotNull("No compilation unit", result);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=51089
	 */
	public void test0537a() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0537", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertNotNull("No compilation unit", result);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=51089
	 */
	public void test0537b() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0537", "B.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertNotNull("No compilation unit", result);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=51089
	 */
	public void test0537c() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0537", "C.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertNotNull("No compilation unit", result);
	}
	/**
	 * Ensures that an AST can be created during reconcile.
	 * @deprecated using deprecated code
	 */
	public void test0538a() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			sourceUnit.becomeWorkingCopy(null, null);
			sourceUnit.getBuffer().setContents(
				"package test0538;\n" +
				"public class A {\n" +
				"  int i;\n" +
				"}"
			);
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, false, null, null);
			assertNotNull("No level 2 compilation unit", unit);
			assertEquals("Compilation unit has wrong AST level (2)", AST.JLS2, unit.getAST().apiLevel());
			// TODO improve test for getJLS3()
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/*
	 * Ensures that no AST is created during reconcile if not requested.
	 */
	public void test0538b() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			sourceUnit.becomeWorkingCopy(null);
			sourceUnit.getBuffer().setContents(
				"package test0538;\n" +
				"public class A {\n" +
				"  int i;\n" +
				"}"
			);
			CompilationUnit unit = sourceUnit.reconcile(0, false, null, null);
			assertNull("Unexpected compilation unit", unit);
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * Ensures that no AST is created during reconcile if consistent.
	 * @deprecated using deprecated code
	 */
	public void test0538c() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			sourceUnit.becomeWorkingCopy(null, null);
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, false, null, null);
			assertNull("Unexpected compilation unit", unit);
			// TODO improve test for getJLS3()
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * Ensures that bindings are created during reconcile if the problem requestor is active.
	 * @deprecated using deprecated code
	 */
	public void test0538d() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor();
			sourceUnit.becomeWorkingCopy(pbRequestor, null);
			sourceUnit.getBuffer().setContents(
				"package test0538;\n" +
				"public class A {\n" +
				"  Object field;\n" +
				"}"
			);
			// TODO improve test for getJLS3()
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, false, null, null);
			ASTNode node = getASTNode(unit, 0, 0);
			assertNotNull("No node", node);
			assertTrue("Not original", isOriginal(node));
			assertTrue("not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
			FieldDeclaration declaration = (FieldDeclaration) node;
			Type type = declaration.getType();
			ITypeBinding typeBinding = type.resolveBinding();
			assertNotNull("No type binding", typeBinding);
			assertEquals("Wrong name", "Object", typeBinding.getName());
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * Ensures that bindings are created during reconcile if force problem detection is turned on.
	 * @deprecated using deprecated code
	 */
	public void test0538e() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor();
			sourceUnit.becomeWorkingCopy(pbRequestor, null);
			// TODO improve test for getJLS3()
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, true/*force pb detection*/, null, null);
			ASTNode node = getASTNode(unit, 0);
			assertNotNull("No node", node);
			assertTrue("not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
			TypeDeclaration declaration = (TypeDeclaration) node;
			ITypeBinding typeBinding = declaration.resolveBinding();
			assertNotNull("No type binding", typeBinding);
			assertEquals("Wrong name", "A", typeBinding.getName());
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * Ensures that bindings are created during reconcile if force problem detection is turned on.
	 * Case of a unit containing an anonymous type.
	 * (regression test for bug 55102 NPE when using ICU.reconcile(GET_AST_TRUE, ...))
	 * @deprecated using deprecated code
	 */
	public void test0538f() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor();
			sourceUnit.becomeWorkingCopy(pbRequestor, null);
			sourceUnit.getBuffer().setContents(
				"package test0538;\n" +
				"public class A {\n" +
				"  void foo() {\n" +
				"    new Object() {\n" +
				"      void bar() {\n" +
				"      }\n" +
				"    };\n" +
				"  }\n" +
				"}"
			);
			// TODO improve test for getJLS3()
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, true/*force pb detection*/, null, null);
			ASTNode node = getASTNode(unit, 0);
			assertNotNull("No node", node);
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * Ensures that bindings are created during reconcile if force problem detection is turned on.
	 * Case of a unit containing an anonymous type.
	 * (regression test for bug 55102 NPE when using ICU.reconcile(GET_AST_TRUE, ...))
	 * @deprecated using deprecated code
	 */
	public void test0538g() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor();
			sourceUnit.becomeWorkingCopy(pbRequestor, null);
			sourceUnit.getBuffer().setContents(
				"package test0538;\n" +
				"public class A {\n" +
				"  void foo() {\n" +
				"    new Object() {\n" +
				"      void bar() {\n" +
				"      }\n" +
				"    };\n" +
				"  }\n" +
				"}"
			);
			sourceUnit.reconcile(ICompilationUnit.NO_AST, false/* don't force pb detection*/, null, null);
			// TODO improve test for getJLS3()
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, true/*force pb detection*/, null, null);
			ASTNode node = getASTNode(unit, 0);
			assertNotNull("No node", node);
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * Ensures that asking for well known type doesn't throw a NPE if the problem requestor is not active.
	 * (regression test for bug 64750 NPE in Java AST Creation - editing some random file)
	 * @deprecated using deprecated code
	 */
	public void test0538h() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0538", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		try {
			ReconcilerTests.ProblemRequestor pbRequestor = new ReconcilerTests.ProblemRequestor() {
				@Override
                public boolean isActive() {
                    return false;
                }
			};
			sourceUnit.becomeWorkingCopy(pbRequestor, null);
			sourceUnit.getBuffer().setContents(
				"package test0538;\n" +
				"public class A {\n" +
				"  Object field;\n" +
				"}"
			);
			// TODO improve test for getJLS3()
			CompilationUnit unit = sourceUnit.reconcile(AST.JLS2, false, null, null);
			assertEquals("Unexpected well known type", null, unit.getAST().resolveWellKnownType("void"));
		} finally {
			sourceUnit.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=53477
	 */
	public void test0539() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0539", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 1, 0);
		assertNotNull("No node", node);
		assertTrue("not an expression statement", node.getNodeType() == ASTNode.EXPRESSION_STATEMENT);
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertTrue("not a class instance creation", expression.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		checkSourceRange(classInstanceCreation, "new A(){}.new Inner(){/*x*/}", source);
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		Expression expression2 = classInstanceCreation.getExpression();
		assertTrue("not a class instance creation", expression2.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION);
		ClassInstanceCreation classInstanceCreation2 = (ClassInstanceCreation) expression2;
		AnonymousClassDeclaration anonymousClassDeclaration2 = classInstanceCreation2.getAnonymousClassDeclaration();
		assertNotNull("No anonymous class declaration", anonymousClassDeclaration2);
		checkSourceRange(anonymousClassDeclaration2, "{}", source);
		assertNotNull("No anonymous class declaration", anonymousClassDeclaration);
		checkSourceRange(anonymousClassDeclaration, "{/*x*/}", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=54431
	 */
	public void test0540() {
		char[] source =
				("package test0540;\n" +  //$NON-NLS-1$
				"\n" +  //$NON-NLS-1$
				"class Test {\n" +  //$NON-NLS-1$
				"	public void foo(int arg) {\n" +//$NON-NLS-1$
				"		assert true;\n" +//$NON-NLS-1$
				"	}\n" +  //$NON-NLS-1$
				"}").toCharArray(); //$NON-NLS-1$
		IJavaProject project = getJavaProject("Converter"); //$NON-NLS-1$
		Map options = project.getOptions(true);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		ASTNode result = runConversion(getJLS3(), source, "Test.java", project, options, true); //$NON-NLS-1$
		assertNotNull("No compilation unit", result); //$NON-NLS-1$
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertEquals("Problems found", 0, compilationUnit.getProblems().length);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("not a TypeDeclaration", node instanceof TypeDeclaration); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding); //$NON-NLS-1$
		assertEquals("Wrong name", "Test", typeBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Wrong package", "test0540", typeBinding.getPackage().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Not an interface", typeBinding.isClass()); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=56697
	 */
	public void test0541() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0541", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		class Change14FieldAccessASTVisitor extends ASTVisitor {
			int counter;
			Change14FieldAccessASTVisitor() {
				this.counter = 0;
			}
			public void endVisit(QualifiedName qualifiedName) {
				IBinding i_binding = qualifiedName.getQualifier().resolveBinding();
				ITypeBinding type_binding = qualifiedName.getQualifier().resolveTypeBinding();
				if (i_binding == null || type_binding == null) {
					this.counter++;
				}
			}
		}
		Change14FieldAccessASTVisitor visitor = new Change14FieldAccessASTVisitor();
		unit.accept(visitor);
		assertEquals("Missing binding", 0, visitor.counter);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55004
	 */
	public void test0542() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0542", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "STRING_FIELD", variableBinding.getName());
		Object constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", "Hello world!", constantValue);
		Expression initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "\"Hello world!\"", source);

		node = getASTNode(unit, 0, 1);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "BOOLEAN_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Boolean.TRUE, constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "true", source);

		node = getASTNode(unit, 0, 2);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "BYTE_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Byte.valueOf((byte)1), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "1", source);

		node = getASTNode(unit, 0, 3);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "CHAR_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Character.valueOf('{'), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "\'{\'", source);

		node = getASTNode(unit, 0, 4);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "DOUBLE_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Double.valueOf("3.1415"), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "3.1415", source);

		node = getASTNode(unit, 0, 5);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "FLOAT_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Float.valueOf("3.14159f"), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "3.14159f", source);

		node = getASTNode(unit, 0, 6);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "INT_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Integer.valueOf("7fffffff", 16), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "Integer.MAX_VALUE", source);

		node = getASTNode(unit, 0, 7);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "LONG_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value", Long.valueOf("34"), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "34L", source);

		node = getASTNode(unit, 0, 8);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "SHORT_FIELD", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant", constantValue);
		assertEquals("Wrong value",Short.valueOf("130"), constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "130", source);

		node = getASTNode(unit, 0, 9);
		assertTrue("not a field declaration", node instanceof FieldDeclaration); //$NON-NLS-1$
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertEquals("Wrong name", "int_field", variableBinding.getName());
		constantValue = variableBinding.getConstantValue();
		assertNull("Got a constant", constantValue);
		initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		checkSourceRange(initializer, "Integer.MAX_VALUE", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=58436
	 */
	public void test0543() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0543", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		unit.accept(new GetKeyVisitor());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51500
	 */
	public void test0544() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0544", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Not an abstract method", (methodDeclaration.getModifiers() & Modifier.ABSTRACT) != 0);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not an abstract method binding", (methodBinding.getModifiers() & Modifier.ABSTRACT) != 0);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59843
	 */
	public void test0545() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0545", "First.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong key", "Ltest0545/First$Test;", typeBinding.getKey());

		sourceUnit = getCompilationUnit("Converter", "src", "test0545", "Second.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		result = runConversion(getJLS3(), sourceUnit, true);
		unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		node = getASTNode(unit, 0, 0);
		assertEquals("not a method declaration", ASTNode.TYPE_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		typeDeclaration = (TypeDeclaration) node;
		typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong key", "Ltest0545/Second$Test;", typeBinding.getKey());

		sourceUnit = getCompilationUnit("Converter", "src", "test0545", "Third.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		result = runConversion(getJLS3(), sourceUnit, true);
		unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		node = getASTNode(unit, 0, 0);
		assertEquals("not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		typeDeclaration = (TypeDeclaration) node;
		typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong key", "Ltest0545/Third$Test;", typeBinding.getKey());


		sourceUnit = getCompilationUnit("Converter", "src", "test0545", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		result = runConversion(getJLS3(), sourceUnit, true);
		unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		node = getASTNode(unit, 0);
		assertEquals("not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		typeDeclaration = (TypeDeclaration) node;
		typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong key", "Ltest0545/Test;", typeBinding.getKey());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59848
	 */
	public void test0546() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0546", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		final CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("not a variable declaration", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType()); //$NON-NLS-1$
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		ITypeBinding typeBinding = variableBinding.getType();
		assertTrue("An anonymous type binding", !typeBinding.isAnonymous());
		Expression initializer = fragment.getInitializer();
		assertEquals("not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, initializer.getNodeType()); //$NON-NLS-1$
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		ITypeBinding typeBinding2 = anonymousClassDeclaration.resolveBinding();
		assertTrue("Not an anonymous type binding", typeBinding2.isAnonymous());
		ITypeBinding typeBinding3 = classInstanceCreation.resolveTypeBinding();
		assertTrue("Not an anonymous type binding", typeBinding3.isAnonymous());
		node = getASTNode(unit, 1, 0, 1);
		assertEquals("not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType()); //$NON-NLS-1$
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType()); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		assertEquals("not a simple name", ASTNode.SIMPLE_NAME, expression2.getNodeType()); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		ITypeBinding typeBinding4 = simpleName.resolveTypeBinding();
		assertTrue("An anonymous type binding", !typeBinding4.isAnonymous());
		Type type = classInstanceCreation.getType();
		IBinding binding = type.resolveBinding();
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding5 = (ITypeBinding) binding;
		assertTrue("An anonymous type binding", !typeBinding5.isAnonymous());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60078
	 */
	public void test0547() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0547", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType()); //$NON-NLS-1$
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node;
		AbstractTypeDeclaration typeDeclaration = typeDeclarationStatement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong key", "Ltest0547/A$74$Local;", typeBinding.getKey());

		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("wrong size", 3, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("not a type declaration statement", ASTNode.TYPE_DECLARATION, bodyDeclaration.getNodeType()); //$NON-NLS-1$
		TypeDeclaration typeDeclaration2 = (TypeDeclaration) bodyDeclaration;

		typeBinding = typeDeclaration2.resolveBinding();
		assertEquals("Wrong key", "Ltest0547/A$100$LocalMember;", typeBinding.getKey());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60581
	 */
	public void test0548() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0548", "PaletteStackEditPart.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48502
	 */
	public void test0549() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0549", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48502
	 */
	public void test0550() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0550", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=60848
	 */
	public void test0551() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0551", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 1, problems.length); //$NON-NLS-1$
		IProblem problem = problems[0];
		assertEquals("wrong end position", source.length - 1, problem.getSourceEnd());
	}

	public void test0552() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0552", "Test.java");
		char[] source = sourceUnit.getSource().toCharArray();
		CompilationUnit result = (CompilationUnit) runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Got errors", 0, result.getProblems().length);
		TypeDeclaration declaration = (TypeDeclaration) result.types().get(0);
		Block body = declaration.getMethods()[0].getBody();
		ExpressionStatement expr = (ExpressionStatement) body.statements().get(0);
		MethodInvocation invocation = (MethodInvocation) expr.getExpression();
		InfixExpression node = (InfixExpression) invocation.arguments().get(0);
		ITypeBinding typeBinding = node.resolveTypeBinding();
		assertEquals("wrong type", "java.lang.String", typeBinding.getQualifiedName());
		checkSourceRange(node, "\"a\" + \"a\" + \"a\"", source);
		List extendedOperands = node.extendedOperands();
		assertEquals("Wrong size", 1, extendedOperands.size());
		Expression leftOperand = node.getLeftOperand();
		checkSourceRange(leftOperand, "\"a\"", source);
		typeBinding = leftOperand.resolveTypeBinding();
		assertEquals("wrong type", "java.lang.String", typeBinding.getQualifiedName());
		Expression rightOperand = node.getRightOperand();
		checkSourceRange(rightOperand, "\"a\"", source);
		typeBinding = rightOperand.resolveTypeBinding();
		assertEquals("wrong type", "java.lang.String", typeBinding.getQualifiedName());
		Expression expression = (Expression) extendedOperands.get(0);
		checkSourceRange(expression, "\"a\"", source);
		typeBinding = expression.resolveTypeBinding();
		assertEquals("wrong type", "java.lang.String", typeBinding.getQualifiedName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=61946
	 */
	public void test0553() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0553", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		Object constantValue = variableBinding.getConstantValue();
		assertNull("Got a constant value", constantValue);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=61946
	 */
	public void test0554() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0554", "B.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		checkSourceRange(expression2, "A", source);
		ITypeBinding typeBinding = expression2.resolveTypeBinding();
		assertEquals("wrong type", "test0554.A", typeBinding.getQualifiedName());
		IVariableBinding[] fields = typeBinding.getDeclaredFields();
		assertEquals("Wrong size", 1, fields.length);
		IVariableBinding variableBinding = fields[0];
		Object constantValue = variableBinding.getConstantValue();
		assertNotNull("Missing constant", constantValue);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=61946
	 */
	public void test0555() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0555", "B.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) expression;
		Name name = qualifiedName.getQualifier();
		checkSourceRange(name, "A", source);
		ITypeBinding typeBinding = name.resolveTypeBinding();
		assertEquals("wrong type", "test0555.A", typeBinding.getQualifiedName());
		IVariableBinding[] fields = typeBinding.getDeclaredFields();
		assertEquals("Wrong size", 1, fields.length);
		IVariableBinding variableBinding = fields[0];
		Object constantValue = variableBinding.getConstantValue();
		assertNotNull("No constant value", constantValue);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62463
	 */
	public void test0556() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0556", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		checkSourceRange(expression2, "(aa.bar())", source);
		SimpleName simpleName = methodInvocation.getName();
		checkSourceRange(simpleName, "size", source);
		checkSourceRange(expression, "(aa.bar()).size()", source);
		checkSourceRange(expressionStatement, "(aa.bar()).size();", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62463
	 */
	public void test0557() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0557", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not an method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		Expression expression2 = methodInvocation.getExpression();
		checkSourceRange(expression2, "(aa.bar())", source);
		SimpleName simpleName = methodInvocation.getName();
		checkSourceRange(simpleName, "get", source);
		checkSourceRange(expression, "(aa.bar()).get(0)", source);
		checkSourceRange(expressionStatement, "(aa.bar()).get(0);", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65090
	 * @deprecated using deprecated code
	 */
	public void test0558() {
		String src = "\tSystem.out.println(\"Hello\");\n\tSystem.out.println(\"World\");\n";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Wrong type", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		assertNotNull("No statements", statements);
		assertEquals("Wrong size", 2, statements.size());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0559() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0559", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		assertEquals("Wrong type", ASTNode.STRING_LITERAL, expression4.getNodeType());
		checkSourceRange(expression4, "\" \"", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0560() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0560", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		assertEquals("Wrong type", ASTNode.STRING_LITERAL, expression4.getNodeType());
		checkSourceRange(expression4, "\" \"", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0561() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0561", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		assertEquals("Wrong type", ASTNode.STRING_LITERAL, expression4.getNodeType());
		checkSourceRange(expression4, "\" \"", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0562() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0562", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		assertEquals("Wrong type", ASTNode.STRING_LITERAL, expression4.getNodeType());
		checkSourceRange(expression4, "\" \"", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0563() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0563", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		checkSourceRange(expression4, "new String()", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0564() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0564", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		checkSourceRange(expression4, "new String()", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65562
	 */
	public void test0565() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0565", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Wrong type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		InfixExpression infixExpression = (InfixExpression) expression;
		Expression expression2 = infixExpression.getLeftOperand();
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression2;
		Expression expression3 = methodInvocation.getExpression();
		assertEquals("Wrong type", ASTNode.PARENTHESIZED_EXPRESSION, expression3.getNodeType());
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression3;
		Expression expression4 = parenthesizedExpression.getExpression();
		checkSourceRange(expression4, "(/**/ String /**/) new String()", source);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=69349
	 */
	public void test0566() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0566", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		assertEquals("Wrong character", '}', source[node.getStartPosition() + node.getLength() - 1]);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=69349
	 */
	public void test0567() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0567", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		assertEquals("Wrong character", '}', source[node.getStartPosition() + node.getLength() - 1]);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=70398
	 */
	public void test0568() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0568", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=70398
	 */
	public void test0570() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0570", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
	}

	/**
	 * No binding when there is no unit name set
	 */
	public void test0571() throws JavaModelException {
		ASTParser parser = ASTParser.newParser(getJLS3());
		String source = "public class A {public boolean foo() {}}";
		parser.setSource(source.toCharArray());
		parser.setProject(getJavaProject("Converter"));
		// no unit name parser.setUnitName("A");
		parser.setResolveBindings(true);
		ASTNode node = parser.createAST(null);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
	}

	/**
	 * No binding when there is no unit name set
	 */
	public void test0572() throws JavaModelException {
		ASTParser parser = ASTParser.newParser(getJLS3());
		String source = "public class A {public boolean foo() {}}";
		parser.setSource(source.toCharArray());
		parser.setProject(getJavaProject("Converter"));
		parser.setUnitName("A.java");
		parser.setResolveBindings(true);
		ASTNode node = parser.createAST(null);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 1, problems.length); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77968
	 */
	public void test0573() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0573", "Z.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String source = sourceUnit.getSource();
		int pos = source.indexOf("his.ba");
		ASTNode result = runConversion(getJLS3(), sourceUnit, pos, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("Missing node", node);
		assertEquals("Wrong type", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertNotNull("Missing node", expression);
		assertEquals("Wrong type", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 1, problems.length); //$NON-NLS-1$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78735
	 */
	public void test0574() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0574", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull("Missing node", node);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		node = getASTNode(unit, 0, 1, 0);
		assertNotNull("Missing node", node);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding2 = fragment.resolveBinding();
		assertFalse("are Equals", variableBinding.isEqualTo(variableBinding2));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78735
	 */
	public void test0575() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0575", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		final IProblem[] problems = unit.getProblems();
		assertEquals("Wrong number of problems", 0, problems.length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 2);
		assertNotNull("Missing node", node);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		node = getASTNode(unit, 0, 1, 1);
		assertNotNull("Missing node", node);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();
		assertFalse("are Equals", methodBinding.isEqualTo(methodBinding2));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78740
	 * @deprecated marked deprecated to suppress JDOM-related deprecation warnings
	 */
	public void test0576() throws JavaModelException {
		org.eclipse.jdt.core.jdom.DOMFactory factory = new org.eclipse.jdt.core.jdom.DOMFactory();
		org.eclipse.jdt.core.jdom.IDOMCompilationUnit domCompilationUnit = factory.createCompilationUnit(
				"package x; /** @model */ interface X  {}", "NAME");
		org.eclipse.jdt.core.jdom.IDOMType domType = (org.eclipse.jdt.core.jdom.IDOMType) domCompilationUnit.getFirstChild().getNextNode();
		assertTrue("Not an interface", Flags.isInterface(domType.getFlags()));
		domType.getComment();
		assertTrue("Not an interface", Flags.isInterface(domType.getFlags()));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77645
	 */
	public void test0578() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0578", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertProblemsSize(unit, 0);
		unit.accept(new ASTVisitor() {
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
			 */
			public boolean visit(SingleVariableDeclaration node) {
				IVariableBinding binding = node.resolveBinding();
				assertNotNull("No method", binding.getDeclaringMethod());
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
			 */
			public boolean visit(VariableDeclarationFragment node) {
				IVariableBinding binding = node.resolveBinding();
				ASTNode parent = node.getParent();
				if (parent != null && binding != null) {
					final IMethodBinding declaringMethod = binding.getDeclaringMethod();
					final String variableBindingName = binding.getName();
					switch(parent.getNodeType()) {
						case ASTNode.FIELD_DECLARATION :
							assertNull("Got a method", declaringMethod);
							break;
						default :
							if (variableBindingName.equals("var1")
									|| variableBindingName.equals("var2")) {
								assertNull("Got a method", declaringMethod);
							} else {
								assertNotNull("No method", declaringMethod);
								String methodName = declaringMethod.getName();
								if (variableBindingName.equals("var4")) {
									assertEquals("Wrong method", "foo", methodName);
								} else if (variableBindingName.equals("var5")) {
									assertEquals("Wrong method", "foo2", methodName);
								} else if (variableBindingName.equals("var7")) {
									assertEquals("Wrong method", "foo3", methodName);
								} else if (variableBindingName.equals("var8")) {
									assertEquals("Wrong method", "X", methodName);
								} else if (variableBindingName.equals("var9")) {
									assertEquals("Wrong method", "bar3", methodName);
								} else if (variableBindingName.equals("var10")) {
									assertEquals("Wrong method", "bar3", methodName);
								} else if (variableBindingName.equals("var11")) {
									assertEquals("Wrong method", "X", methodName);
								}
							}
					}
				}
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldAccess)
			 */
			public boolean visit(FieldAccess node) {
				IVariableBinding binding = node.resolveFieldBinding();
				assertNull("No method", binding.getDeclaringMethod());
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SuperFieldAccess)
			 */
			public boolean visit(SuperFieldAccess node) {
				IVariableBinding binding = node.resolveFieldBinding();
				assertNull("No method", binding.getDeclaringMethod());
				return false;
			}
		});
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77984
	 * @deprecated
	 */
	public void test0579() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0579", "ParserTask.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = AST.parseCompilationUnit(sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("Wrong number of body declarations", 3, typeDeclaration.bodyDeclarations().size());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=79953
	 */
	public void test0580() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/p/X.java", true/*resolve*/);
			String source = "package p;\n" +
			"public class X {\n" +
			"	d String[][]tab;\n" +
			"}";
			ASTNode node = buildAST(
				source,
				workingCopy,
				false);
			assertEquals("wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			checkSourceRange(fieldDeclaration, "d String[][]", source.toCharArray(), true/*expectMalformed*/);
			Type type = fieldDeclaration.getType();
			assertTrue("Not a simple type", type.isSimpleType());
			List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			assertEquals("Wrong extended dimensions", 2, fragment.getExtraDimensions());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80041
	 */
	public void test0581() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X {\n" +
				"    void m(Object obj) {}\n" +
				"    void foo(Object obj) {}\n" +
				"}",
				workingCopy);
			assertEquals("wrong type", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List parameters = methodDeclaration.parameters();
			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
			IVariableBinding variableBinding = variableDeclaration.resolveBinding();
			node = getASTNode(compilationUnit, 0, 1);
			assertEquals("wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
			methodDeclaration = (MethodDeclaration) node;
			parameters = methodDeclaration.parameters();
			variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
			IVariableBinding variableBinding2 = variableDeclaration.resolveBinding();
			assertFalse("Bindings are equal", variableBinding.isEqualTo(variableBinding2));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80338
	 */
	// TODO (jerome) remove this test as it has nothing to do on ASTConverterTest and it is a dup of ExistenceTests#testMethodWithInvalidParameter()
	public void test0582() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0582", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IMethod[] methods = type.getMethods();
		assertEquals("wrong size", 1, methods.length);
		IMethod method = methods[0];
		assertEquals("wrong number", 1, method.getNumberOfParameters());
		assertEquals("wrong signature", "([[I)[[[[[I", method.getSignature());
		assertEquals("wrong return type", "[[[[[I", method.getReturnType());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0583() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\000\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\000', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0584() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\u0020\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", 32, literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0585() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\b\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\b', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0586() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\t\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\t', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0587() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\n\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\n', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0588() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\f\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\f', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0589() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\r\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\r', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0590() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\\"\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\"', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0591() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\'\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\'', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0592() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\\\\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\\', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0593() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\077\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a character literal", ASTNode.CHARACTER_LITERAL, expression.getNodeType());
			CharacterLiteral literal = (CharacterLiteral) expression;
			assertEquals("Wrong character", '\077', literal.charValue());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82616
	 */
	public void test0594() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private char nextChar() {\n" +
				"		return \'\\777\';\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 1, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			assertTrue("not malformed", isMalformed(methodDeclaration));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82985
	 */
	public void test0595() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0595", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List imports = compilationUnit.imports();
		assertEquals("Wrong size", 1, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		IBinding binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83098
	 */
	public void test0596() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void m(String[] args) {\n" +
				"		for (int i= 0; i < args.length; i++) {\n" +
				"			String string= args[i];\n" +
				"		}\n" +
				"		for (int i= 0; i < args.length; i++) {\n" +
				"			String string= args[i];\n" +
				"		}\n" +
				"	}\n" +
				"}\n";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertEquals("Got problems", 0, compilationUnit.getProblems().length);
			node = getASTNode(compilationUnit, 0, 0, 0);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, node.getNodeType());
			ForStatement forStatement = (ForStatement) node;
			Statement action = forStatement.getBody();
			assertEquals("Not a block", ASTNode.BLOCK, action.getNodeType());
			Block block = (Block) action;
			List statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());
			Statement statement = (Statement) statements.get(0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement.getNodeType());
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
			List fragments = variableDeclarationStatement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);

			node = getASTNode(compilationUnit, 0, 0, 1);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, node.getNodeType());
			forStatement = (ForStatement) node;
			action = forStatement.getBody();
			assertEquals("Not a block", ASTNode.BLOCK, action.getNodeType());
			block = (Block) action;
			statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());
			statement = (Statement) statements.get(0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement.getNodeType());
			variableDeclarationStatement = (VariableDeclarationStatement) statement;
			fragments = variableDeclarationStatement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding2 = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding2);

			assertFalse("Bindings are equals", variableBinding.isEqualTo(variableBinding2));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83210
	 */
	public void test0597() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0597", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		assertEquals("Not an instanceof expression", ASTNode.INSTANCEOF_EXPRESSION, expression.getNodeType());
		InstanceofExpression instanceofExpression = (InstanceofExpression) expression;
		Type type = instanceofExpression.getRightOperand();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		SimpleName simpleName = (SimpleName) name;
		ITypeBinding typeBinding = simpleName.resolveTypeBinding();

		List imports = compilationUnit.imports();
		assertEquals("Wrong size", 2, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		name = importDeclaration.getName();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		simpleName = qualifiedName.getName();
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();

		assertTrue("not identical", typeBinding == typeBinding2);
		assertTrue("not identical", typeBinding.isEqualTo(typeBinding2));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84778
	 */
	public void test0598() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	int m(int i) {\n" +
				"		return /*start*/1 + 2 + ++i/*end*/;\n" +
				"	}\n" +
				"}\n";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, node.getNodeType());
			assertEquals("Wrong debug string", "1 + 2 + ++i", node.toString());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=86541
	 */
	public void test0599() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0599", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		String expectedResult =
			"The hierarchy of the type X is inconsistent\n" +
			"The type test0599.Zork2 cannot be resolved. It is indirectly referenced from required type test0599.Zork";
		assertProblemsSize(compilationUnit, 2, expectedResult);
		compilationUnit.accept(new ASTVisitor() {
			public void endVisit(MethodDeclaration methodDeclaration) {
				Block body = methodDeclaration.getBody();
				assertNotNull("No body", body);
				assertTrue("No statements", body.statements().size() != 0);
			}
		});
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=87777
	 */
	public void test0600() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0600", "Try.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding().getMethodDeclaration();

		sourceUnit = getCompilationUnit("Converter" , "src", "test0600", "C.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding().getMethodDeclaration();

		assertTrue("Not equals", methodBinding.isEqualTo(methodBinding2));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=89014
	 */
	public void test0601() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	Runnable one= new Runnable(){\n" +
				"		public void run() {\n" +
				"		}\n" +
				"	};\n" +
				"	Runnable two= new Runnable(){\n" +
				"		public void run() {\n" +
				"		}\n" +
				"	};\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
			AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
			assertNotNull("No anonymous", anonymousClassDeclaration);
			List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
			assertEquals("Wrong size", 1, bodyDeclarations.size());
			BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
			IMethodBinding methodBinding = methodDeclaration.resolveBinding();

			node = getASTNode(compilationUnit, 0, 1);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			fieldDeclaration = (FieldDeclaration) node;
			fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			expression = fragment.getInitializer();
			assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
			classInstanceCreation = (ClassInstanceCreation) expression;
			anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
			assertNotNull("No anonymous", anonymousClassDeclaration);
			bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
			assertEquals("Wrong size", 1, bodyDeclarations.size());
			bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
			methodDeclaration = (MethodDeclaration) bodyDeclaration;
			IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();

			assertFalse("Bindings are equals", methodBinding.isEqualTo(methodBinding2));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=70526
	 */
	public void test0602() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0602", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(StringLiteral stringLiteral) {
				assertTrue("Wrong start position", stringLiteral.getStartPosition() != 0);
				assertTrue("Wrong length", stringLiteral.getLength() != -1);
				return false;
			}
		});
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91098
	 */
	public void test0603() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0603", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		assertProblemsSize(compilationUnit, 0);
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(SimpleType type) {
				assertFalse("start cannot be -1", type.getStartPosition() == -1);
				assertFalse("length cannot be 0", type.getLength() == 0);
				return false;
			}
			public boolean visit(ArrayType type) {
				assertFalse("start cannot be -1", type.getStartPosition() == -1);
				assertFalse("length cannot be 0", type.getLength() == 0);
				return true;
			}
		});
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91098
	 */
	public void test0604() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0604", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		assertProblemsSize(compilationUnit, 0);
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(SimpleType type) {
				assertFalse("start cannot be -1", type.getStartPosition() == -1);
				assertFalse("length cannot be 0", type.getLength() == 0);
				return false;
			}
			public boolean visit(ArrayType type) {
				assertFalse("start cannot be -1", type.getStartPosition() == -1);
				assertFalse("length cannot be 0", type.getLength() == 0);
				return true;
			}
		});
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92059
	 * check resolvedType binding from variable ref (of array type)
	 */
	public void test0605() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0605", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("Not a variable declaration", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement varDecl = (VariableDeclarationStatement) node;
		List fragments = varDecl.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression.getNodeType());
		IBinding arraylength = ((QualifiedName)expression).resolveBinding();
		IJavaElement element = arraylength.getJavaElement();
		assertNull("Shouldn't be binding for arraylength", element);
		Name name = ((QualifiedName) expression).getQualifier();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		ITypeBinding binding = name.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertTrue("No array", binding.isArray());

		node = getASTNode(compilationUnit, 0, 0, 4);
		assertEquals("Not a variable declaration", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		varDecl = (VariableDeclarationStatement) node;
		fragments = varDecl.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression.getNodeType());
		name = ((QualifiedName) expression).getQualifier();
		assertEquals("Not a simple name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		name = ((QualifiedName) name).getName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		ITypeBinding binding2 = name.resolveTypeBinding();
		assertNotNull("No binding", binding2);
		assertTrue("No array", binding2.isArray());

		assertEquals("Not same binding", binding, binding2);

		node = getASTNode(compilationUnit, 0, 0, 2);
		assertEquals("Not a variable declaration", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		varDecl = (VariableDeclarationStatement) node;
		fragments = varDecl.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a qualified name", ASTNode.FIELD_ACCESS, expression.getNodeType());
		expression = ((FieldAccess) expression).getExpression();
		assertEquals("Not a simple name", ASTNode.FIELD_ACCESS, expression.getNodeType());
		name = ((FieldAccess) expression).getName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		ITypeBinding binding3 = name.resolveTypeBinding();
		assertNotNull("No binding", binding3);
		assertTrue("No array", binding3.isArray());

		assertEquals("Not same binding", binding, binding3);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=89014
	 */
	public void test0606() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int i = 0;\n" +
				"    i += 1;\n" +
				"    String s = \"\";\n" +
				"    s += \"hello world\";\n" +
				"    System.out.println(i+s);\n" +
				"  }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0, 1);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			Expression expression = ((ExpressionStatement) node).getExpression();
			assertEquals("Not an assignment", ASTNode.ASSIGNMENT, expression.getNodeType());
			Assignment assignment = (Assignment) expression;
			assertEquals("Wrong operator", Assignment.Operator.PLUS_ASSIGN, assignment.getOperator());
			ITypeBinding typeBinding = assignment.resolveTypeBinding();
			assertNotNull("No binding", typeBinding);
			assertEquals("Wrong type", "int", typeBinding.getQualifiedName());

			node = getASTNode(compilationUnit, 0, 0, 3);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			expression = ((ExpressionStatement) node).getExpression();
			assertEquals("Not an assignment", ASTNode.ASSIGNMENT, expression.getNodeType());
			assignment = (Assignment) expression;
			assertEquals("Wrong operator", Assignment.Operator.PLUS_ASSIGN, assignment.getOperator());
			typeBinding = assignment.resolveTypeBinding();
			assertNotNull("No binding", typeBinding);
			assertEquals("Wrong type", "java.lang.String", typeBinding.getQualifiedName());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=95262
	 */
	public void test0607() throws JavaModelException {
		final char[] source = "private static Category[] values = new Category[]{v1, v2, v3};".toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertNotNull("cannot be null", root);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=96698
	 */
	public void test0608() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    for (int /*start*/i = 0/*end*/; i < args.length; i++) {\n" +
				"		System.out.println(args[i]);\n" +
				"	 }\n" +
				"  }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a variable declaration fragment", ASTNode.VARIABLE_DECLARATION_FRAGMENT, node.getNodeType());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			IJavaElement javaElement = variableBinding.getJavaElement();
			assertNotNull("No java element", javaElement);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=96698
	 */
	public void test0609() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    int /*start*/i = 0/*end*/;\n" +
				"	 System.out.println(i);\n" +
				"  }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a variable declaration fragment", ASTNode.VARIABLE_DECLARATION_FRAGMENT, node.getNodeType());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			IJavaElement javaElement = variableBinding.getJavaElement();
			assertNotNull("No java element", javaElement);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=98088
	 */
	public void test0610() throws JavaModelException {
		final ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0610", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		final ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertProblemsSize(unit, 1, "The type Test is deprecated");
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=96698
	 */
	public void test0611() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"abstract class SearchPattern {\n" +
				"}\n" +
				"class InternalSearchPattern extends SearchPattern {\n" +
				"	boolean mustResolve;\n" +
				"}\n" +
				"public class X {\n" +
				"	public static final int POSSIBLE_MATCH = 0;\n" +
				"	public static final int ACCURATE_MATCH = 1;\n" +
				"	\n" +
				"	public void foo(SearchPattern pattern) {\n" +
				"		int declarationLevel = ((InternalSearchPattern) pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;\n" +
				"		System.out.println(declarationLevel);\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 2, 2, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			checkSourceRange(expression, "((InternalSearchPattern) pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	public void test0612() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    void foo(boolean[]value) {\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List parameters = methodDeclaration.parameters();
			assertEquals("Wrong size", 1, parameters.size());
			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
			checkSourceRange(variableDeclaration, "boolean[]value", contents);
			Type type = variableDeclaration.getType();
			checkSourceRange(type, "boolean[]", contents);
			assertTrue("Not an array type", type.isArrayType());
			ArrayType arrayType = (ArrayType) type;
			Type componentType = componentType(arrayType);
			assertTrue("Not a primitive type", componentType.isPrimitiveType());
			PrimitiveType primitiveType = (PrimitiveType) componentType;
			assertEquals("Not boolean", PrimitiveType.BOOLEAN, primitiveType.getPrimitiveTypeCode());
			checkSourceRange(primitiveType, "boolean", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	public void test0613() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo(boolean b) {\n" +
				"		Zork z = null;\n" +
				"		if (b) {\n" +
				"			System.out.println(z);\n" +
				"		}\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Zork cannot be resolved to a type");
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding binding = fragment.resolveBinding();
			assertNotNull("No binding", binding);
			assertEquals("LX;.foo(Z)V#z", binding.getKey());

			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
			IfStatement ifStatement = (IfStatement) node;
			Statement statement2 = ifStatement.getThenStatement();
			assertEquals("Not a block", ASTNode.BLOCK, statement2.getNodeType());
			Block block = (Block) statement2;
			List statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());

			Statement statement3 = (Statement) statements.get(0);
			assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, statement3.getNodeType());
			ExpressionStatement expressionStatement = (ExpressionStatement) statement3;
			Expression expression = expressionStatement.getExpression();
			assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			List arguments = methodInvocation.arguments();
			assertEquals("Wrong size", 1, arguments.size());
			Expression expression2 = (Expression) arguments.get(0);
			ITypeBinding typeBinding = expression2.resolveTypeBinding();
			assertNotNull("No binding", typeBinding);
			assertEquals("LX;.foo(Z)V#z", binding.getKey());
			assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression2.getNodeType());
			SimpleName simpleName = (SimpleName) expression2;
			IBinding binding2 = simpleName.resolveBinding();
			assertNotNull("Got a binding", binding2);
			assertEquals("LX;.foo(Z)V#z", binding2.getKey());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=105192
	public void test0614() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
		    String contents =
		    	"class T { void m() { for (i=0, j=0; i<10; i++, j++) ; }}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", false/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			final String expectedOutput = "class T {\n" +
					"  void m(){\n" +
					"    for (i=0, j=0; i < 10; i++, j++)     ;\n" +
					"  }\n" +
					"}\n";
			assertEquals("Wrong output", Util.convertToIndependantLineDelimiter(expectedOutput), Util.convertToIndependantLineDelimiter(node.toString()));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100041
	 */
	public void test0615() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"class X {\n" +
				"	static Object object;\n" +
				"	static void foo() {\n" +
				"		/**\n" +
				"		 * javadoc comment.\n" +
				"		 */\n" +
				"		if (object instanceof String) {\n" +
				"			final String clr = null;\n" +
				"		}\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 1, 0);
			assertNotNull("No node", node);
			assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
			IfStatement ifStatement = (IfStatement) node;
			String expectedSource = "if (object instanceof String) {\n" +
			"			final String clr = null;\n" +
			"		}";
			checkSourceRange(ifStatement, expectedSource, contents);
			Statement statement = ifStatement.getThenStatement();
			assertNotNull("No then statement", statement);
			assertEquals("not a block", ASTNode.BLOCK, statement.getNodeType());
			Block block = (Block) statement;
			expectedSource = "{\n" +
			"			final String clr = null;\n" +
			"		}";
			checkSourceRange(block, expectedSource, contents);
			List statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());
			Statement statement2 = (Statement) statements.get(0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement2.getNodeType());
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement2;
			checkSourceRange(variableDeclarationStatement, "final String clr = null;", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=109333
	 */
	public void test0616() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"class X {\n" +
				"	boolean val = true && false && true && false && true;\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents.toCharArray());
			ASTNode node = runConversion(getJLS3(), workingCopy, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Dead code");
			node = getASTNode(unit, 0, 0);
			assertNotNull("No node", node);
			assertEquals("Not a field declaration ", ASTNode.FIELD_DECLARATION, node.getNodeType());
			final FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			final List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			final Expression initializer = fragment.getInitializer();
			assertNotNull("No initializer", initializer);
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, initializer.getNodeType());
			InfixExpression infixExpression = (InfixExpression) initializer;
			final List extendedOperands = infixExpression.extendedOperands();
			assertEquals("Wrong size", 3, extendedOperands.size());
			assertEquals("Wrong operator", InfixExpression.Operator.CONDITIONAL_AND, infixExpression.getOperator());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=109333
	 */
	public void test0617() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"class X {\n" +
				"	boolean val = true || false || true || false || true;\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents.toCharArray());
			ASTNode node = runConversion(getJLS3(), workingCopy, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedOutput = "Dead code";
			assertProblemsSize(unit, 1, expectedOutput);
			node = getASTNode(unit, 0, 0);
			assertNotNull("No node", node);
			assertEquals("Not a field declaration ", ASTNode.FIELD_DECLARATION, node.getNodeType());
			final FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			final List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			final Expression initializer = fragment.getInitializer();
			assertNotNull("No initializer", initializer);
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, initializer.getNodeType());
			InfixExpression infixExpression = (InfixExpression) initializer;
			final List extendedOperands = infixExpression.extendedOperands();
			assertEquals("Wrong size", 3, extendedOperands.size());
			assertEquals("Wrong operator", InfixExpression.Operator.CONDITIONAL_OR, infixExpression.getOperator());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=109535
	 */
	public void test0618() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	String f = \"\" + \"\" - 1;\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", false/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertNotNull("No node", node);
			assertEquals("Not a field declaration ", ASTNode.FIELD_DECLARATION, node.getNodeType());
			final FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			final List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			final Expression initializer = fragment.getInitializer();
			assertNotNull("No initializer", initializer);
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, initializer.getNodeType());
			InfixExpression infixExpression = (InfixExpression) initializer;
			List extendedOperands = infixExpression.extendedOperands();
			assertEquals("Wrong size", 0, extendedOperands.size());
			assertEquals("Wrong operator", InfixExpression.Operator.MINUS, infixExpression.getOperator());
			Expression leftOperand = infixExpression.getLeftOperand();
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, leftOperand.getNodeType());
			InfixExpression infixExpression2 = (InfixExpression) leftOperand;
			extendedOperands = infixExpression.extendedOperands();
			assertEquals("Wrong size", 0, extendedOperands.size());
			assertEquals("Wrong operator", InfixExpression.Operator.PLUS, infixExpression2.getOperator());
			assertEquals("Not a string literal", ASTNode.STRING_LITERAL, infixExpression2.getLeftOperand().getNodeType());
			assertEquals("Not a string literal", ASTNode.STRING_LITERAL, infixExpression2.getRightOperand().getNodeType());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=109646
	 */
	public void test0619() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0619", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assertProblemsSize(unit, 0);
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		ASTNode statement = node;
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(source);
		parser.setSourceRange(statement.getStartPosition(), statement.getLength());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result2 = parser.createAST(null);
		assertNotNull("No node", result2);
		assertTrue("not a block", result2.getNodeType() == ASTNode.BLOCK);
		Block block = (Block) result2;
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size());
		Statement statement2 = (Statement) statements.get(0);
		assertEquals("Statement is not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement2.getNodeType());
		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) statement2;
		assertEquals("Wrong number of fragments", 4, declarationStatement.fragments().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=109940
	 */
	public void test0620() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println((int) \'\\0\');\n" +
				"		System.out.println((int) \'\\1\');\n" +
				"		System.out.println((int) \'\\2\');\n" +
				"		System.out.println((int) \'\\3\');\n" +
				"		System.out.println((int) \'\\4\');\n" +
				"		System.out.println((int) \'\\5\');\n" +
				"		System.out.println((int) \'\\6\');\n" +
				"		System.out.println((int) \'\\7\');\n" +
				"		System.out.println((int) \'\\077\');\n" +
				"		System.out.println((int) \'\\55\');\n" +
				"		System.out.println((int) \'\\77\');\n" +
				"		System.out.println((int) \'\\377\');\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", false/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			unit.accept(new ASTVisitor() {
				public boolean visit(CharacterLiteral characterLiteral) {
					try {
						final String escapedValue = characterLiteral.getEscapedValue();
						final char charValue = characterLiteral.charValue();
						if (escapedValue.equals("\'\\0\'")) {
							assertEquals("Wrong value", 0, charValue);
						} else if (escapedValue.equals("\'\\1\'")) {
							assertEquals("Wrong value", 1, charValue);
						} else if (escapedValue.equals("\'\\2\'")) {
							assertEquals("Wrong value", 2, charValue);
						} else if (escapedValue.equals("\'\\3\'")) {
							assertEquals("Wrong value", 3, charValue);
						} else if (escapedValue.equals("\'\\4\'")) {
							assertEquals("Wrong value", 4, charValue);
						} else if (escapedValue.equals("\'\\5\'")) {
							assertEquals("Wrong value", 5, charValue);
						} else if (escapedValue.equals("\'\\6\'")) {
							assertEquals("Wrong value", 6, charValue);
						} else if (escapedValue.equals("\'\\7\'")) {
							assertEquals("Wrong value", 7, charValue);
						} else if (escapedValue.equals("\'\\077\'")) {
							assertEquals("Wrong value", 63, charValue);
						} else if (escapedValue.equals("\'\\55\'")) {
							assertEquals("Wrong value", 45, charValue);
						} else if (escapedValue.equals("\'\\77\'")) {
							assertEquals("Wrong value", 63, charValue);
						} else if (escapedValue.equals("\'\\377\'")) {
							assertEquals("Wrong value", 255, charValue);
						} else {
							assertTrue("Should not get there", false);
						}
					} catch(IllegalArgumentException e) {
						assertTrue("Should not happen", false);
					}
					return false;
				}
			});
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=109963
	 */
	public void test0621() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	public void foo(int y) {\n" +
				"		switch (y) {\n" +
				"			case 1:\n" +
				"				int i,j;\n" +
				"		}\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", false/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertNotNull("No node", node);
			assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, node.getNodeType());
			SwitchStatement switchStatement = (SwitchStatement) node;
			List statements = switchStatement.statements();
			assertEquals("Wrong size", 2, statements.size());
			Statement statement = (Statement) statements.get(1);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement.getNodeType());
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
			assertEquals("Wrong size", 2, variableDeclarationStatement.fragments().size());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=92866
	 */
	public void test0622() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println((int) \'\\0\');\n" +
				"		System.out.println((int) \'\\00\');\n" +
				"		System.out.println((int) \'\\000\');\n" +
				"		System.out.println((int) \'\\40\');\n" +
				"		System.out.println((int) \'\\040\');\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", false/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			unit.accept(new ASTVisitor() {
				public boolean visit(CharacterLiteral characterLiteral) {
					try {
						characterLiteral.charValue();
					} catch(IllegalArgumentException e) {
						assertTrue("Should not happen", false);
					}
					return false;
				}
			});
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=116573
	 */
	public void test0623() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"class X {\n" +
				"        X(boolean x, String y, String z) {}\n" +
				"        X(int x, String y) {}\n" +
				"        X(String x) {\n" +
				"                this(first, second);\n" +
				"        }\n" +
				"        void test() {\n" +
				"                new X(first, second);\n" +
				"        }\n" +
				"        class Z extends X {\n" +
				"                public Z() {\n" +
				"                        super(first, second);\n" +
				"                }\n" +
				"        }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedOutput =
				"first cannot be resolved to a variable\n" +
				"second cannot be resolved to a variable\n" +
				"first cannot be resolved to a variable\n" +
				"second cannot be resolved to a variable\n" +
				"first cannot be resolved to a variable\n" +
				"second cannot be resolved to a variable";
			assertProblemsSize(unit, 6, expectedOutput);
			unit.accept(new ASTVisitor() {
				public boolean visit(ConstructorInvocation constructorInvocation) {
					assertNotNull("No binding", constructorInvocation.resolveConstructorBinding());
					return false;
				}
				public boolean visit(ClassInstanceCreation classInstanceCreation) {
					assertNotNull("No binding", classInstanceCreation.resolveConstructorBinding());
					return false;
				}
			});
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=118876
	 */
	public void test0624() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X extend {}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedOutput =
				"Syntax error on token \"extend\", delete this token";
			assertProblemsSize(unit, 1, expectedOutput);
			unit.accept(new ASTVisitor() {
				public boolean visit(TypeDeclaration typeDeclaration) {
					assertTrue("Should be malformed", isMalformed(typeDeclaration));
					return false;
				}
				public boolean visit(CompilationUnit compilationUnit) {
					assertFalse("Should not be malformed", isMalformed(compilationUnit));
					return true;
				}
			});
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=118897
	 */
	public void test0625() {
		char[] source =
				("package test0305;\n" +  //$NON-NLS-1$
				"\n" +  //$NON-NLS-1$
				"class Test {\n" +  //$NON-NLS-1$
				"	public void foo(int arg) {}\n" +  //$NON-NLS-1$
				"}").toCharArray(); //$NON-NLS-1$
		IJavaProject project = getJavaProject("Converter"); //$NON-NLS-1$
		ASTNode result = runConversion(getJLS3(), source, "Test.java", project); //$NON-NLS-1$
		assertNotNull("No compilation unit", result); //$NON-NLS-1$
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("not a TypeDeclaration", node instanceof TypeDeclaration); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNull("Got a type binding", typeBinding); //$NON-NLS-1$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=118897
	 */
	public void test0626() {
		char[] source =
				("package java.lang;\n" +  //$NON-NLS-1$
				"\n" +  //$NON-NLS-1$
				"class Object {\n" +  //$NON-NLS-1$
				"	public void foo(int arg) {}\n" +  //$NON-NLS-1$
				"}").toCharArray(); //$NON-NLS-1$
		IJavaProject project = getJavaProject("Converter"); //$NON-NLS-1$
		ASTNode result = runConversion(getJLS3(), source, "Object.java", project); //$NON-NLS-1$
		assertNotNull("No compilation unit", result); //$NON-NLS-1$
		assertTrue("result is not a compilation unit", result instanceof CompilationUnit); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("not a TypeDeclaration", node instanceof TypeDeclaration); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNull("Got a type binding", typeBinding); //$NON-NLS-1$
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=116833
	 */
	public void test0627() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    void m() {\n" +
				"        error();\n" +
				"        new Cloneable() {\n" +
				"            void xx() {}\n" +
				"        };\n" +
				"        new Cloneable() {\n" +
				"            void xx() {}\n" +
				"        };\n" + 				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedOutput =
				"The method error() is undefined for the type X";
			assertProblemsSize(unit, 1, expectedOutput);
			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			Expression expression = ((ExpressionStatement) node).getExpression();
			assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
			AnonymousClassDeclaration anonymousClassDeclaration = ((ClassInstanceCreation) expression).getAnonymousClassDeclaration();
			assertNotNull("No anonymous class declaration", anonymousClassDeclaration);
			List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
			assertEquals("Wrong size", 1, bodyDeclarations.size());
			BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
			IMethodBinding methodBinding = methodDeclaration.resolveBinding();

			node = getASTNode(unit, 0, 0, 2);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			expression = ((ExpressionStatement) node).getExpression();
			assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
			anonymousClassDeclaration = ((ClassInstanceCreation) expression).getAnonymousClassDeclaration();
			assertNotNull("No anonymous class declaration", anonymousClassDeclaration);
			bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
			assertEquals("Wrong size", 1, bodyDeclarations.size());
			bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
			methodDeclaration = (MethodDeclaration) bodyDeclaration;
			IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();

			assertFalse("Should not be equal", methodBinding.isEqualTo(methodBinding2));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=125270
	 */
	public void test0628() throws JavaModelException {
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_EXPRESSION);
		String source = "{\"red\", \"yellow\"}";
		parser.setSource(source.toCharArray());
		parser.setSourceRange(0, source.length());
		parser.setCompilerOptions(JavaCore.getOptions());
		ASTNode result = parser.createAST(null);
		assertNotNull("No node", result);
		assertEquals("not an array initializer", ASTNode.ARRAY_INITIALIZER, result.getNodeType());
		ArrayInitializer arrayInitializer = (ArrayInitializer) result;
		List expressions = arrayInitializer.expressions();
		assertEquals("Wrong size", 2, expressions.size());
		assertEquals("Wrong type", ASTNode.STRING_LITERAL, ((Expression) expressions.get(0)).getNodeType());
		assertEquals("Wrong type", ASTNode.STRING_LITERAL, ((Expression) expressions.get(1)).getNodeType());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=126598
	 */
	public void test0629() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0629", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput =
			"Syntax error on token \",\", invalid Expression";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a compilation unit", ASTNode.FIELD_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		checkSourceRange(fragment, "s =  {\"\",,,}", source);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=126598
	 */
	public void test0630() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0630", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput =
			"Syntax error on token \",\", invalid Expression";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=126598
	 */
	public void test0631() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0631", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType()); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput =
			"Syntax error, insert \"}\" to complete ArrayInitializer\n" +
			"Syntax error, insert \";\" to complete FieldDeclaration\n" +
			"Syntax error, insert \"}\" to complete ClassBody";
		assertProblemsSize(compilationUnit, 3, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a compilation unit", ASTNode.FIELD_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		checkSourceRange(fragment, "s =  {\"\",,,", source, true/*expectMalformed*/);
		assertTrue("Not initializer", fragment.getInitializer() == null);
		assertTrue("Not a malformed node", isMalformed(fragment));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128539
	 */
	public void test0632() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void m(int state) {\n" +
				"		switch (state) {\n" +
				"			case 4:\n" +
				"				double M0,M1;\n" +
				"		}\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, node.getNodeType());
			SwitchStatement statement = (SwitchStatement) node;
			List statements = statement.statements();
			assertEquals("wrong size", 2, statements.size());
			assertEquals("Not a switch case", ASTNode.SWITCH_CASE, ((ASTNode) statements.get(0)).getNodeType());
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, ((ASTNode) statements.get(1)).getNodeType());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128539
	 */
	public void test0633() {
		String src = "switch (state) {case 4:double M0,M1;}";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Wrong type", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		assertNotNull("No statements", statements);
		assertEquals("Wrong size", 1, statements.size());
		final ASTNode node = (ASTNode) statements.get(0);
		assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, node.getNodeType());
		SwitchStatement statement = (SwitchStatement) node;
		statements = statement.statements();
		assertEquals("wrong size", 2, statements.size());
		assertEquals("Not a switch case", ASTNode.SWITCH_CASE, ((ASTNode) statements.get(0)).getNodeType());
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, ((ASTNode) statements.get(1)).getNodeType());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128823
	 */
	public void test0634() throws JavaModelException {
		try {
			String src =
				"public class X {\n" +
					"	void foo() {\n" +
					"		int i1 i1;\n" +
					"		int i2 i2;\n" +
					"		int i3 i3;\n" +
					"		int i4 i4;\n" +
	 				"		int i5 i5;\n" +
					"		int i6 i6;\n" +
	 				"		int i7 i7;\n" +
					"		int i8 i8;\n" +
					"		int i9 i9;\n" +
					"		int i10 i10;\n" +
					"		int i11 i11;\n" +
					"		\n" +
					"		for for ;;){}\n" +
					"	}\n" +
					"}";

			char[] source = src.toCharArray();
			ASTParser parser = ASTParser.newParser(getJLS3());
			parser.setKind (ASTParser.K_COMPILATION_UNIT);
			parser.setSource (source);
			parser.setStatementsRecovery(true);
			ASTNode result = parser.createAST (null);
			assertNotNull("no result", result);
		} catch (ArrayIndexOutOfBoundsException e) {
			assertTrue("ArrayIndexOutOfBoundsException", false);
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128960
	 */
	public void test0635() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo(Object tab[]) {\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration declaration = (MethodDeclaration) node;
			List parameters = declaration.parameters();
			assertEquals("wrong number", 1, parameters.size());
			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
			checkSourceRange(variableDeclaration, "Object tab[]", contents);
			checkSourceRange(variableDeclaration.getType(), "Object", contents);
			checkSourceRange(variableDeclaration.getName(), "tab", contents);
			assertEquals("wrong number of extra dimensions", 1, variableDeclaration.getExtraDimensions());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128960
	 */
	public void test0636() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo(java.lang.Object tab[]) {\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration declaration = (MethodDeclaration) node;
			List parameters = declaration.parameters();
			assertEquals("wrong number", 1, parameters.size());
			SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
			checkSourceRange(variableDeclaration, "java.lang.Object tab[]", contents);
			checkSourceRange(variableDeclaration.getType(), "java.lang.Object", contents);
			checkSourceRange(variableDeclaration.getName(), "tab", contents);
			assertEquals("wrong number of extra dimensions", 1, variableDeclaration.getExtraDimensions());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128961
	 */
	public void test0637() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo() {\n" +
				"		for( int i = (1); ; ) {\n" +
				"       }\n" +
				"   }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, node.getNodeType());
			ForStatement forStatement = (ForStatement) node;
			List inits = forStatement.initializers();
			assertEquals("Wrong size", 1, inits.size());
			Expression expression = (Expression) inits.get(0);
			assertEquals("Not a variable declaration expression", ASTNode.VARIABLE_DECLARATION_EXPRESSION, expression.getNodeType());
			VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) expression;
			List fragments = declarationExpression.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression initializer = fragment.getInitializer();
			checkSourceRange(initializer, "(1)", contents);
			checkSourceRange(fragment, "i = (1)", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128961
	 */
	public void test0638() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	int i = (1);\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression initializer = fragment.getInitializer();
			checkSourceRange(initializer, "(1)", contents);
			checkSourceRange(fragment, "i = (1)", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=128961
	 */
	public void test0639() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo() {\n" +
				"		for( int i = (1), j = 0; ; ) {\n" +
				"       }\n" +
				"   }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, node.getNodeType());
			ForStatement forStatement = (ForStatement) node;
			List inits = forStatement.initializers();
			assertEquals("Wrong size", 1, inits.size());
			Expression expression = (Expression) inits.get(0);
			assertEquals("Not a variable declaration expression", ASTNode.VARIABLE_DECLARATION_EXPRESSION, expression.getNodeType());
			VariableDeclarationExpression declarationExpression = (VariableDeclarationExpression) expression;
			List fragments = declarationExpression.fragments();
			assertEquals("Wrong size", 2, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression initializer = fragment.getInitializer();
			checkSourceRange(initializer, "(1)", contents);
			checkSourceRange(fragment, "i = (1)", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/*
	 * Ensures that 2 type bindings (one from .class file, the other from attache source) are "isEqualTo(...)".
	 * (regression test for bug 130317 ASTParser with IOrdinaryClassFile as source creates type bindings that are not isEqualTo(..) binary bindings)
	 */
	public void test0641() throws JavaModelException {
		// Integer from attached source
		IOrdinaryClassFile classFile = getClassFile("Converter", getConverterJCLPath().toOSString(), "java.lang", "Integer.class");
		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("public")};
		markerInfo.astEnds = new int[] {source.lastIndexOf('}') + 1};
		ASTNode node = buildAST(markerInfo, classFile);
		IBinding bindingFromAttachedSource = ((TypeDeclaration) node).resolveBinding();

		ICompilationUnit workingCopy = null;
		try {
    		workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
	    	String contents =
				"public class X {\n" +
				"	/*start*/Integer/*end*/ field;\n" +
				"}";
		   	IBinding[] bindings = resolveBindings(contents, workingCopy);
		   	assertTrue("2 type bindings should be equals", bindingFromAttachedSource.isEqualTo(bindings[0]));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=129330
	 */
	public void test0642() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.awt.Point;\n" +
				"public class X {\n" +
				"	public void foo(Point p, int[] a) {\n" +
				"	   p.x;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Syntax error, insert \"VariableDeclarators\" to complete LocalVariableDeclaration");
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			assertTrue("Not recovered", isRecovered(node));
			final Expression expression = ((ExpressionStatement) node).getExpression();
			assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression.getNodeType());
			assertTrue("Not recovered", isRecovered(expression));
			checkSourceRange(expression, "p.x", contents);
			checkSourceRange(node, "p.x;", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=129330
	 */
	public void test0643() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.awt.Point;\n" +
				"public class X {\n" +
				"	public void foo(Point p, int[] a) {\n" +
				"	   a[0];\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Syntax error, insert \"AssignmentOperator Expression\" to complete Expression");
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			assertTrue("Not recovered", isRecovered(node));
			final Expression expression = ((ExpressionStatement) node).getExpression();
			assertEquals("Not an array access", ASTNode.ARRAY_ACCESS, expression.getNodeType());
			assertTrue("Not recovered", isRecovered(expression));
			checkSourceRange(expression, "a[0]", contents);
			checkSourceRange(node, "a[0];", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=129330
	 */
	public void test0644() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	public void foo() {\n" +
				"	   int x =;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Syntax error on token \"=\", Expression expected after this token");
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a vaviable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			assertNull("No initializer", expression);
			assertTrue("Not recovered", isRecovered(fragment));
			checkSourceRange(fragment, "x =", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=135997
	 */
	public void test0645() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	{\n" +
				"	   new Object();\n" +
				"	   Object.equ;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertEquals(
					"Wrong problem size",
					2,
					unit.getProblems().length);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a field declaration statement", ASTNode.INITIALIZER, node.getNodeType());
			Initializer initializer = (Initializer) node;
			checkSourceRange(
					initializer,
					"{\n" +
					"	   new Object();\n" +
					"	   Object.equ;\n" +
					"	}",
					contents);
			Block block = initializer.getBody();
			checkSourceRange(
					block,
					"{\n" +
					"	   new Object();\n" +
					"	   Object.equ;\n" +
					"	}",
					contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=136972
	 */
	public void test0646() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"        static {\n" +
				"                class A\n" +
				"                Object o = new Object(){\n" +
				"                        void test(){\n" +
				"                        }\n" +
				"                };\n" +
				"        }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=141043
	 */
	public void test0647() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    public void run(int i) {\n" +
				"    }\n" +
				"    public void foo() {\n" +
				"        new Runnable() {\n" +
				"            public void run() {\n" +
				"                run(1);    \n" +
				"            }\n" +
				"        };\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "The method run() in the type new Runnable(){} is not applicable for the arguments (int)");
			node = getASTNode(unit, 0, 1, 0);
			assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
			Expression expression = ((ExpressionStatement) node).getExpression();
			ITypeBinding typeBinding = expression.resolveTypeBinding();
			IMethodBinding[] methodBindings = typeBinding.getDeclaredMethods();
			assertEquals("Wrong size", 2, methodBindings.length);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=147877
	 */
	public void test0648() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    public void foo(int[] a) {\n" +
				"        int i = a[0];\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a vaviable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			assertNotNull("No initializer", expression);
			checkSourceRange(expression, "a[0]", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=147877
	 */
	public void test0649() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    public void foo(int[] a) {\n" +
				"        int i = a[0\\u005D;\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a vaviable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			assertNotNull("No initializer", expression);
			checkSourceRange(expression, "a[0\\u005D", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=147877
	 */
	public void test0650() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    public void foo(int[] a) {\n" +
				"        int[] i = new int[0];\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a vaviable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			assertNotNull("No initializer", expression);
			checkSourceRange(expression, "new int[0]", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=147877
	 */
	public void test0651() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"    public void foo(int[] a) {\n" +
				"        int[] i = new int[0\\u005D;\n" +
				"    }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a vaviable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			Expression expression = fragment.getInitializer();
			assertNotNull("No initializer", expression);
			checkSourceRange(expression, "new int[0\\u005D", contents);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * @bug 149126: IllegalArgumentException in ASTConverter
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=149126"
	 */
	public void test0652() throws CoreException {
		ASTResult result = this.buildMarkedAST(
				"/Converter/src/TestCharset.java",
				"import java.nio.ByteBuffer;\n" +
				"import java.nio.CharBuffer;\n" +
				"import java.nio.charset.Charset;\n" +
				"import java.nio.charset.CharsetDecoder;\n" +
				"import java.nio.charset.CharsetEncoder;\n" +
				"import java.nio.charset.CoderResult;\n" +
				"public class TestCharset extends Charset {\n" +
				"        public CharsetDecoder newDecoder() {\n" +
				"                return new CharsetDecoder(this, 2.0, 2.0) {\n" +
				"                        CharsetDecoder(CharSet\n" +
				"                        protected CoderResult decodeLoop(ByteBuffer in,\n" +
				"CharBuffer out) {\n" +
				"                                return null;\n" +
				"                        }\n" +
				"                };;\n" +
				"        }\n" +
				"        public CharsetEncoder newEncoder() {\n" +
				"                return null;\n" +
				"        }\n" +
				"}");

		assertASTResult(
				"===== AST =====\n" +
				"import java.nio.ByteBuffer;\n" +
				"import java.nio.CharBuffer;\n" +
				"import java.nio.charset.Charset;\n" +
				"import java.nio.charset.CharsetDecoder;\n" +
				"import java.nio.charset.CharsetEncoder;\n" +
				"import java.nio.charset.CoderResult;\n" +
				"public class TestCharset extends Charset {\n" +
				"  public CharsetDecoder newDecoder(){\n" +
				"    return new CharsetDecoder(this,2.0,2.0){\n" +
				"      void CharsetDecoder();\n" +
				"      protected CoderResult decodeLoop(      ByteBuffer in,      CharBuffer out){\n" +
				"        return null;\n" +
				"      }\n" +
				"    }\n" +
				";\n" +
				"    ;\n" +
				"  }\n" +
				"  public CharsetEncoder newEncoder(){\n" +
				"    return null;\n" +
				"  }\n" +
				"}\n" +
				"\n" +
				"===== Details =====\n" +
				"===== Problems =====\n" +
				"1. ERROR in /Converter/src/TestCharset.java (at line 1)\n" +
				"	import java.nio.ByteBuffer;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.nio cannot be resolved\n" +
				"2. ERROR in /Converter/src/TestCharset.java (at line 2)\n" +
				"	import java.nio.CharBuffer;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.nio cannot be resolved\n" +
				"3. ERROR in /Converter/src/TestCharset.java (at line 3)\n" +
				"	import java.nio.charset.Charset;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.nio cannot be resolved\n" +
				"4. ERROR in /Converter/src/TestCharset.java (at line 4)\n" +
				"	import java.nio.charset.CharsetDecoder;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.nio cannot be resolved\n" +
				"5. ERROR in /Converter/src/TestCharset.java (at line 5)\n" +
				"	import java.nio.charset.CharsetEncoder;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.nio cannot be resolved\n" +
				"6. ERROR in /Converter/src/TestCharset.java (at line 6)\n" +
				"	import java.nio.charset.CoderResult;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.nio cannot be resolved\n" +
				"7. ERROR in /Converter/src/TestCharset.java (at line 7)\n" +
				"	public class TestCharset extends Charset {\n" +
				"	                                 ^^^^^^^\n" +
				"Charset cannot be resolved to a type\n" +
				"8. ERROR in /Converter/src/TestCharset.java (at line 8)\n" +
				"	public CharsetDecoder newDecoder() {\n" +
				"	       ^^^^^^^^^^^^^^\n" +
				"CharsetDecoder cannot be resolved to a type\n" +
				"9. ERROR in /Converter/src/TestCharset.java (at line 9)\n" +
				"	return new CharsetDecoder(this, 2.0, 2.0) {\n" +
				"	           ^^^^^^^^^^^^^^\n" +
				"CharsetDecoder cannot be resolved to a type\n" +
				"10. ERROR in /Converter/src/TestCharset.java (at line 10)\n" +
				"	CharsetDecoder(CharSet\n" +
				"	^^^^^^^^^^^^^^^\n" +
				"Return type for the method is missing\n" +
				"11. ERROR in /Converter/src/TestCharset.java (at line 10)\n" +
				"	CharsetDecoder(CharSet\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, insert \"... VariableDeclaratorId\" to complete FormalParameter\n" +
				"12. ERROR in /Converter/src/TestCharset.java (at line 10)\n" +
				"	CharsetDecoder(CharSet\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, insert \")\" to complete ConstructorDeclaration\n" +
				"13. ERROR in /Converter/src/TestCharset.java (at line 10)\n" +
				"	CharsetDecoder(CharSet\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, insert \";\" to complete ClassBodyDeclarations\n" +
				"14. ERROR in /Converter/src/TestCharset.java (at line 11)\n" +
				"	protected CoderResult decodeLoop(ByteBuffer in,\n" +
				"	          ^^^^^^^^^^^\n" +
				"CoderResult cannot be resolved to a type\n" +
				"15. ERROR in /Converter/src/TestCharset.java (at line 11)\n" +
				"	protected CoderResult decodeLoop(ByteBuffer in,\n" +
				"	                                 ^^^^^^^^^^\n" +
				"ByteBuffer cannot be resolved to a type\n" +
				"16. ERROR in /Converter/src/TestCharset.java (at line 12)\n" +
				"	CharBuffer out) {\n" +
				"	^^^^^^^^^^\n" +
				"CharBuffer cannot be resolved to a type\n" +
				"17. ERROR in /Converter/src/TestCharset.java (at line 17)\n" +
				"	public CharsetEncoder newEncoder() {\n" +
				"	       ^^^^^^^^^^^^^^\n" +
				"CharsetEncoder cannot be resolved to a type\n",
				result);
		}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=150409
	 */
	public void test0653() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0653", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType());
		CompilationUnit unit = (CompilationUnit) result;

		ASTNode node = getASTNode(unit, 0, 2, 0);
		assertEquals("Not a method invocation", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		MethodInvocation invocation = (MethodInvocation) statement.getExpression();
		List arguments = invocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		Expression argument = (Expression) arguments.get(0);
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, argument.getNodeType());
		invocation = (MethodInvocation) argument;
		Expression expression = invocation.getExpression();
		assertEquals("Not a method invocation", ASTNode.FIELD_ACCESS, expression.getNodeType());
		FieldAccess fieldAccess = (FieldAccess) expression;
		IVariableBinding variableBinding = fieldAccess.resolveFieldBinding();
		assertNotNull("No variable binding", variableBinding);
		IMethodBinding resolveMethodBinding = invocation.resolveMethodBinding();
		assertNotNull("No binding", resolveMethodBinding);
	}


	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=148224
	 */
	public void test0654() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	int i;\n" +
				"	public void foo(int[] a) {\n" +
				"	}\n" +
				"	String s;\n" +
				"	public String[][] bar() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration declaration = (FieldDeclaration) node;
			Type type = declaration.getType();
			ITypeBinding typeBinding = type.resolveBinding();
			assertTrue("Not a primitive type", typeBinding.isPrimitive());
			assertEquals("Not int", "int", typeBinding.getName());
			try {
				typeBinding.createArrayType(-1);
				assertTrue("Should throw an exception", false);
			} catch(IllegalArgumentException exception) {
				// ignore
			}
			try {
				typeBinding.createArrayType(0);
				assertTrue("Should throw an exception", false);
			} catch(IllegalArgumentException exception) {
				// ignore
			}
			try {
				typeBinding.createArrayType(256);
				assertTrue("Should throw an exception", false);
			} catch(IllegalArgumentException exception) {
				// ignore
			}
			ITypeBinding binding = typeBinding.createArrayType(2);
			assertEquals("Wrong dimensions", 2, binding.getDimensions());
			assertTrue("Not an array type binding", binding.isArray());
			ITypeBinding componentType = binding.getComponentType();
			assertTrue("Not an array type binding", componentType.isArray());
			assertEquals("Wrong dimensions", 1, componentType.getDimensions());
			componentType = componentType.getComponentType();
			assertFalse("An array type binding", componentType.isArray());
			assertEquals("Wrong dimensions", 0, componentType.getDimensions());

			binding = typeBinding.createArrayType(1);
			node = getASTNode(unit, 0, 1);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List parameters = methodDeclaration.parameters();
			assertEquals("Wrong size", 1, parameters.size());
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
			Type type2 = parameter.getType();
			ITypeBinding typeBinding2 = type2.resolveBinding();
			assertNotNull("No binding", typeBinding2);
			assertTrue("Not an array binding", typeBinding2.isArray());
			assertEquals("Wrong dimension", 1, typeBinding2.getDimensions());
			assertEquals("Wrong type", "int", typeBinding2.getElementType().getName());
			assertTrue("Should be equals", binding == typeBinding2);

			binding = typeBinding2.createArrayType(3);
			assertTrue("Not an array binding", binding.isArray());
			assertEquals("Wrong dimension", 4, binding.getDimensions());

			node = getASTNode(unit, 0, 2);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			declaration = (FieldDeclaration) node;
			type = declaration.getType();
			typeBinding = type.resolveBinding();
			assertTrue("A primitive type", !typeBinding.isPrimitive());
			assertEquals("Not String", "String", typeBinding.getName());

			binding = typeBinding.createArrayType(1);
			node = getASTNode(unit, 0, 3);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			methodDeclaration = (MethodDeclaration) node;
			type = methodDeclaration.getReturnType2();
			assertNotNull("No return type", type);
			typeBinding2 = type.resolveBinding();
			assertNotNull("No binding", typeBinding2);
			assertTrue("Not an array binding", typeBinding2.isArray());
			assertEquals("Wrong dimension", 2, typeBinding2.getDimensions());
			assertEquals("Wrong type", "String", typeBinding2.getElementType().getName());
			typeBinding2 = typeBinding2.getComponentType();
			assertTrue("Not an array binding", typeBinding2.isArray());
			assertEquals("Wrong dimension", 1, typeBinding2.getDimensions());
			assertTrue("Should be equals", binding == typeBinding2);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=157570
	 */
	public void test0655() {
		String src = "public static void m1()\n" +
				"    {\n" +
				"        int a;\n" +
				"        int b;\n" +
				"    }\n" +
				"\n" +
				"    public static void m2()\n" +
				"    {\n" +
				"        int c;\n" +
				"        int d;\n" +
				"    }";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Not a block", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		for (Iterator iterator = statements.iterator(); iterator.hasNext(); ) {
			Statement statement = (Statement) iterator.next();
			if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
				// only the expression statements are recovered. The others are considered as valid blocks.
				assertTrue(isRecovered(statement));
			}
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=157570
	 */
	public void test0656() {
		String src = "public static void m1()\n" +
				"    {\n" +
				"        int a;\n" +
				"        int b;\n" +
				"    }\n" +
				"\n" +
				"    public static void m2()\n" +
				"    {\n" +
				"        int c;\n" +
				"        int d;\n" +
				"    }";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(false);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Not a block", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		assertEquals("Should be empty", 0, statements.size());
	}

	// http://dev.eclipse.org/bugs/show_bug.cgi?id=160198
	public void test0657() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0657", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ICompilationUnit sourceUnit2 = getCompilationUnit("Converter" , "src", "test0657", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		class TestASTRequestor extends ASTRequestor {
			public ArrayList asts = new ArrayList();
			public void acceptAST(ICompilationUnit source, CompilationUnit compilationUnit) {
				this.asts.add(compilationUnit);
			}
			public void acceptBinding(String bindingKey, IBinding binding) {
			}
		}
		TestASTRequestor requestor = new TestASTRequestor();
		resolveASTs(
			new ICompilationUnit[] {sourceUnit, sourceUnit2},
			new String[0],
			requestor,
			getJavaProject("Converter"),
			sourceUnit.getOwner()
		);
		ArrayList arrayList = requestor.asts;
		assertEquals("Wrong size", 2, arrayList.size());
		int problemsCount = 0;
		for (int i = 0, max = arrayList.size(); i < max; i++) {
			Object current = arrayList.get(i);
			assertTrue("not a compilation unit", current instanceof CompilationUnit);
			CompilationUnit unit = (CompilationUnit) current;
			IProblem[] problems = unit.getProblems();
			problemsCount += problems.length;
		}
		assertEquals("wrong size", 1, problemsCount);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=157570
	 */
	public void _test0658() {
		String src = "public static void m1()\n" +
				"    {\n" +
				"        int a;\n" +
				"        int b;\n" +
				"    }\n" +
				"\n" +
				"    public static void m2()\n" +
				"    {\n" +
				"        int c;\n" +
				"        int d;\n" +
				"    }";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Not a block", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		assertEquals("Should be empty", 4, statements.size());
		assertTrue("Not recovered", isRecovered(block));
		ASTNode root = block.getRoot();
		assertNotNull("No root", root);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, root.getNodeType());
		CompilationUnit unit = (CompilationUnit) root;
		String errors =
				"Syntax error on token(s), misplaced construct(s)\n" +
				"Syntax error, insert \";\" to complete BlockStatements\n" +
				"Syntax error on token(s), misplaced construct(s)\n" +
				"Syntax error, insert \";\" to complete Statement";

		assertProblemsSize(unit, 4, errors);
	}

	public void test0659() throws CoreException, JavaModelException {
		IJavaProject javaProject = createJavaProject("P659", new String[] { "src" }, new String[0], "bin");
		try {
			ASTParser parser = ASTParser.newParser(getJLS3());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			String source ="package java.lang;\n" +
					"public class Object {\n" +
					"        public String toString() {\n" +
					"                return \"\";\n" +
					"        }\n" +
					"}";
			parser.setSource(source.toCharArray());
			parser.setProject(javaProject);
			parser.setResolveBindings(true);
			parser.setUnitName("Object.java");
			ASTNode result = parser.createAST (null);
			assertNotNull("no result", result);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType());
			CompilationUnit unit = (CompilationUnit) result;
			ASTNode node = getASTNode(unit, 0, 0);
			assertNotNull("No node", node);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration declaration = (MethodDeclaration) node;
			Block block = declaration.getBody();
			assertNotNull("no block", block);
			List statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());
			ReturnStatement returnStatement = (ReturnStatement) statements.get(0);
			Expression expression = returnStatement.getExpression();
			assertNotNull("No expression", expression);
			ITypeBinding binding = expression.resolveTypeBinding();
			assertNotNull("No binding", binding);
			assertEquals("LString;", binding.getKey());
		} finally {
			deleteProject("P659");
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=144858
	 */
	public void test0660() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo() {\n" +
				"		int x = 0;\n" +
				"		String x = \"\"; //$NON-NLS-1$\n" +
				"		x.toString();\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Duplicate local variable x");
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List  fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "x", variableBinding.getName());
			// (PR 149590)
			String key = variableBinding.getKey();

			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			statement = (VariableDeclarationStatement) node;
			fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "x", variableBinding.getName());
			// (PR 149590)
			String key2 = variableBinding.getKey();
			assertFalse("Keys should not be equals", key2.equals(key));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=144858
	 */
	public void test0661() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"        public static void main(String[] args) {\n" +
				"                int x = 2;\n" +
				"                try {\n" +
				"\n" +
				"                } catch(NullPointerException x) {\n" +
				"                } catch(Exception e) {\n" +
				"                }\n" +
				"        }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 1, "Duplicate parameter x");
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List  fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "x", variableBinding.getName());
			// (PR 149590)
			String key = variableBinding.getKey();

			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
			TryStatement statement2 = (TryStatement) node;
			List catchClauses = statement2.catchClauses();
			assertEquals("Wrong size", 2, catchClauses.size());
			CatchClause catchClause = (CatchClause) catchClauses.get(0);
			SingleVariableDeclaration variableDeclaration = catchClause.getException();
			variableBinding = variableDeclaration.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "x", variableBinding.getName());
			// (PR 149590)
			String key2 = variableBinding.getKey();
			assertFalse("Keys should not be equals", key2.equals(key));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=144858
	 */
	public void test0662() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"        public static void main(String[] args) {\n" +
				"                int x = x = 0;\n" +
				"                if (true) {\n" +
				"                        int x = x = 1;\n" +
				"                }\n" +
				"        }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedLog =
				"The assignment to variable x has no effect\n" +
				"Duplicate local variable x\n" +
				"The assignment to variable x has no effect";
			assertProblemsSize(unit, 3, expectedLog);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List  fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "x", variableBinding.getName());
			// (PR 149590)
			String key = variableBinding.getKey();

			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
			IfStatement ifStatement = (IfStatement) node;
			Block block = (Block) ifStatement.getThenStatement();
			List statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());
			Statement statement2 = (Statement) statements.get(0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement2.getNodeType());
			statement = (VariableDeclarationStatement) statement2;
			fragments = statement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "x", variableBinding.getName());

			// (PR 149590)
			String key2 = variableBinding.getKey();
			assertFalse("Keys should not be equals", key2.equals(key));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=144858
	 */
	public void test0663() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"        public static void main(String[] args) {\n" +
				"                for (int i = 0; i < 10; i++) {\n" +
				"                        for (int i = 0; i < 5; i++)  {\n" +
				"                                // do something\n" +
				"                        }\n" +
				"                }\n" +
				"        }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedLog = "Duplicate local variable i";
			assertProblemsSize(unit, 1, expectedLog);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, node.getNodeType());
			ForStatement statement = (ForStatement) node;
			List initializers = statement.initializers();
			assertEquals("Wrong size", 1, initializers.size());
			Expression expression = (Expression) initializers.get(0);
			assertEquals("Not a variable declaration expression", ASTNode.VARIABLE_DECLARATION_EXPRESSION, expression.getNodeType());
			VariableDeclarationExpression expression2 = (VariableDeclarationExpression) expression;
			List fragments = expression2.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "i", variableBinding.getName());
			// (PR 149590)
			String key = variableBinding.getKey();

			Block block = (Block) statement.getBody();
			List statements = block.statements();
			assertEquals("Wrong size", 1, statements.size());
			Statement statement2 = (Statement) statements.get(0);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, statement2.getNodeType());
			statement = (ForStatement) statement2;
			initializers = statement.initializers();
			assertEquals("Wrong size", 1, initializers.size());
			expression = (Expression) initializers.get(0);
			assertEquals("Not a variable declaration expression", ASTNode.VARIABLE_DECLARATION_EXPRESSION, expression.getNodeType());
			expression2 = (VariableDeclarationExpression) expression;
			fragments = expression2.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "i", variableBinding.getName());

			// (PR 149590)
			String key2 = variableBinding.getKey();
			assertFalse("Keys should not be equals", key2.equals(key));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=165662
	 */
	public void test0664() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"  void foo() {\n" +
				"     class Local {\n" +
				"        void foo() {}\n" +
				"     }\n" +
				"     {\n" +
				"        class Local {\n" +
				"                Local(int i) {\n" +
				"                        this.init(i);\n" +
				"                }\n" +
				"				 void init(int i) {}\n" +
				"        }\n" +
				"        Local l = new Local(0);\n" +
				"     }\n" +
				"  }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedLog = "Duplicate nested type Local";
			assertProblemsSize(unit, 1, expectedLog);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			TypeDeclarationStatement statement = (TypeDeclarationStatement) node;
			ITypeBinding typeBinding = statement.resolveBinding();
			assertNotNull("No binding", typeBinding);
			String key = typeBinding.getKey();
			assertNotNull("No key", key);

			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not a block", ASTNode.BLOCK, node.getNodeType());
			Block block = (Block) node;
			List statements = block.statements();
			assertEquals("wrong size", 2, statements.size());
			Statement statement2 = (Statement) statements.get(0);
			assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, statement2.getNodeType());
			statement = (TypeDeclarationStatement) statement2;
			typeBinding = statement.resolveBinding();
			assertNotNull("No binding", typeBinding);
			String key2 = typeBinding.getKey();
			assertNotNull("No key2", key2);
			assertFalse("Keys should not be equals", key.equals(key2));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=165662
	 */
	public void test0665() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"        void foo() {\n" +
				"                class Local {\n" +
				"                        void foo() {\n" +
				"                        }\n" +
				"                }\n" +
				"                {\n" +
				"                        class Local {\n" +
				"                               Local(int i) {\n" +
				"                                       this.init(i);\n" +
				"                                       this.bar();\n" +
				"                               }\n" +
				"				 				void init(int i) {}\n" +
				"                        		void bar() {\n" +
				"                        		}\n" +
				"                        }\n" +
				"                        Local l = new Local(0);\n" +
				"                }\n" +
				"                Local l = new Local();\n" +
				"                l.foo();\n" +
				"        }\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedLog = "Duplicate nested type Local";
			assertProblemsSize(unit, 1, expectedLog);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			TypeDeclarationStatement statement = (TypeDeclarationStatement) node;
			ITypeBinding typeBinding = statement.resolveBinding();
			assertNotNull("No binding", typeBinding);
			String key = typeBinding.getKey();
			assertNotNull("No key", key);

			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not a block", ASTNode.BLOCK, node.getNodeType());
			Block block = (Block) node;
			List statements = block.statements();
			assertEquals("wrong size", 2, statements.size());
			Statement statement2 = (Statement) statements.get(0);
			assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, statement2.getNodeType());
			statement = (TypeDeclarationStatement) statement2;
			typeBinding = statement.resolveBinding();
			assertNotNull("No binding", typeBinding);
			String key2 = typeBinding.getKey();
			assertNotNull("No key2", key2);
			assertFalse("Keys should not be equals", key.equals(key2));

			Statement statement3 = (Statement) statements.get(1);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement3.getNodeType());
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement3;
			List  fragments = variableDeclarationStatement.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("Wrong name", "l", variableBinding.getName());
			Expression expression = fragment.getInitializer();
			ITypeBinding typeBinding2 = expression.resolveTypeBinding();
			assertNotNull("No type binding2", typeBinding2);

			AbstractTypeDeclaration declaration = statement.getDeclaration();
			List bodyDeclarations = declaration.bodyDeclarations();
			assertEquals("Wrong size", 3, bodyDeclarations.size());
			BodyDeclaration declaration2 = (BodyDeclaration) bodyDeclarations.get(0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, declaration2.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) declaration2;
			assertTrue("not a constructor", methodDeclaration.isConstructor());
			block = methodDeclaration.getBody();
			statements = block.statements();
			assertEquals("Wrong size", 2, statements.size());
			statement3 = (Statement) statements.get(1);
			assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, statement3.getNodeType());
			expression = ((ExpressionStatement) statement3).getExpression();
			assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
			MethodInvocation invocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = invocation.resolveMethodBinding();
			assertNotNull(methodBinding);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0666() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	protected String foo() {\n" +
				"		List c = new ArrayList();\n" +
				"		c.add(null);\n" +
				"		return c;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError =
				"List cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("No fragments", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertEquals("LX;.foo()Ljava/lang/String;#c", variableBinding.getKey());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0666_2() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	protected String foo() {\n" +
				"		List c = new ArrayList();\n" +
				"		c.add(null);\n" +
				"		return c;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				true,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError =
				"List cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("No fragments", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNull("Got a binding", variableBinding);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0667() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	List foo() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError = "List cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration declaration = (MethodDeclaration) node;
			IMethodBinding binding = declaration.resolveBinding();
			assertNull("Got a binding", binding);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0667_2() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	List foo() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy,
				false,
				false,
				true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError = "List cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration declaration = (MethodDeclaration) node;
			IMethodBinding binding = declaration.resolveBinding();
			assertNotNull("No binding", binding);
			assertEquals("LX;.foo()LList;", binding.getKey());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=174298
	 */
	public void test0668() throws JavaModelException {
		assertEquals("Wrong name property", "org.eclipse.jdt.core.dom.SimpleName", AnnotationTypeDeclaration.NAME_PROPERTY.getChildType().getName());
		assertEquals("Wrong name property", "org.eclipse.jdt.core.dom.SimpleName", EnumDeclaration.NAME_PROPERTY.getChildType().getName());
		assertEquals("Wrong name property", "org.eclipse.jdt.core.dom.SimpleName", TypeDeclaration.NAME_PROPERTY.getChildType().getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=176057
	 */
	public void test0669() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0669", "UIPerformChangeOperation.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS3(), sourceUnit, true, true);
		assertNotNull(result);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0670() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	protected String foo() {\n" +
				"		List c = new ArrayList();\n" +
				"		c.add(null);\n" +
				"		return c;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents);
			ASTNode node = runConversion(getJLS3(), workingCopy, true, true, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError =
				"List cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("No fragments", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertFalse("Not a recovered binding", variableBinding.isRecovered());
			ITypeBinding typeBinding = variableBinding.getType();
			assertNotNull("No binding", typeBinding);
			assertTrue("Not a recovered binding", typeBinding.isRecovered());
			assertEquals("Wrong name", "List", typeBinding.getName());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0671() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	protected String foo() {\n" +
				"		List[] c[] = new ArrayList();\n" +
				"		c.add(null);\n" +
				"		return c;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents);
			ASTNode node = runConversion(getJLS3(), workingCopy, true, true, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError =
				"List cannot be resolved to a type"	;
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("No fragments", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertFalse("Unexpected recovered binding", variableBinding.isRecovered());
			ITypeBinding typeBinding = variableBinding.getType();
			assertNotNull("No binding", typeBinding);
			assertTrue("Not a recovered binding", typeBinding.isRecovered());
			assertEquals("Wrong name", "List[][]", typeBinding.getName());
			assertEquals("Wrong dimension", 2, typeBinding.getDimensions());
			ITypeBinding componentType = typeBinding.getComponentType();
			assertNotNull("No binding", componentType);
			assertTrue("Not a recovered binding", componentType.isRecovered());
			assertEquals("Wrong name", "List[]", componentType.getName());
			componentType = componentType.getComponentType();
			assertNotNull("No binding", componentType);
			assertTrue("Not a recovered binding", componentType.isRecovered());
			assertEquals("Wrong name", "List", componentType.getName());
			ITypeBinding elementType = typeBinding.getElementType();
			assertNotNull("No binding", elementType);
			assertTrue("Not a recovered binding", elementType.isRecovered());
			assertEquals("Wrong name", "List", elementType.getName());

			typeBinding = statement.getType().resolveBinding();
			assertNotNull("No binding", typeBinding);
			assertTrue("Not a recovered binding", typeBinding.isRecovered());
			assertEquals("Wrong name", "List[]", typeBinding.getName());
			assertEquals("Wrong dimension", 1, typeBinding.getDimensions());
			componentType = typeBinding.getComponentType();
			assertEquals("Wrong name", "List", componentType.getName());
			assertNotNull("No binding", componentType);
			assertTrue("Not a recovered binding", componentType.isRecovered());
			elementType = typeBinding.getElementType();
			assertNotNull("No binding", elementType);
			assertTrue("Not a recovered binding", elementType.isRecovered());
			assertEquals("Wrong name", "List", elementType.getName());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0672() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"import java.util.ArrayList;\n" +
				"\n" +
				"public class X {\n" +
				"	protected String foo() {\n" +
				"		List[][] c = new ArrayList();\n" +
				"		c.add(null);\n" +
				"		return c;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents);
			ASTNode node = runConversion(getJLS3(), workingCopy, true, true, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError =
				"List cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			assertTrue("No binding recovery", unit.getAST().hasBindingsRecovery());
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("No fragments", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			IVariableBinding variableBinding = fragment.resolveBinding();
			assertNotNull("No binding", variableBinding);
			assertFalse("Unexpected recovered binding", variableBinding.isRecovered());
			ITypeBinding typeBinding = variableBinding.getType();
			assertNotNull("No binding", typeBinding);
			assertTrue("Not a recovered binding", typeBinding.isRecovered());
			assertEquals("Wrong name", "List[][]", typeBinding.getName());
			assertEquals("Wrong dimension", 2, typeBinding.getDimensions());
			ITypeBinding componentType = typeBinding.getComponentType();
			assertNotNull("No binding", componentType);
			assertTrue("Not a recovered binding", componentType.isRecovered());
			assertEquals("Wrong name", "List[]", componentType.getName());
			ITypeBinding elementType = typeBinding.getElementType();
			assertNotNull("No binding", elementType);
			assertTrue("Not a recovered binding", elementType.isRecovered());
			assertEquals("Wrong name", "List", elementType.getName());

			typeBinding = statement.getType().resolveBinding();
			assertNotNull("No binding", typeBinding);
			assertTrue("Not a recovered binding", typeBinding.isRecovered());
			assertEquals("Wrong name", "List[][]", typeBinding.getName());
			assertEquals("Wrong dimension", 2, typeBinding.getDimensions());
			componentType = typeBinding.getComponentType();
			assertEquals("Wrong name", "List[]", componentType.getName());
			assertNotNull("No binding", componentType);
			assertTrue("Not a recovered binding", componentType.isRecovered());
			elementType = typeBinding.getElementType();
			assertNotNull("No binding", elementType);
			assertEquals("Wrong name", "List", elementType.getName());
			assertTrue("Not a recovered binding", elementType.isRecovered());

			IJavaElement javaElement = elementType.getJavaElement();
			assertNotNull("No java element", javaElement);
			assertTrue("Javalement exists", !javaElement.exists());
			IPackageBinding packageBinding = elementType.getPackage();
			assertNotNull("No package", packageBinding);
			assertTrue("Not the default package", packageBinding.isUnnamed());
			ITypeBinding arrayBinding = elementType.createArrayType(2);
			assertNotNull("No array binding", arrayBinding);
			assertEquals("Wrong dimensions", 2, arrayBinding.getDimensions());
			ITypeBinding elementType2 = arrayBinding.getElementType();
			assertNotNull("No element type", elementType2);
			assertNotNull("No key", elementType.getKey());
			assertTrue("Not equals", elementType2.isEqualTo(elementType));

			node = getASTNode(unit, 0);
			assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
			TypeDeclaration typeDeclaration = (TypeDeclaration) node;
			ITypeBinding typeBinding2 = typeDeclaration.resolveBinding();
			ITypeBinding javaLangObject = typeBinding2.getSuperclass();
			assertEquals("Not java.lang.Object", "java.lang.Object", javaLangObject.getQualifiedName());
			assertTrue("Not isCastCompatible", elementType.isCastCompatible(javaLangObject));
			assertTrue("Not isCastCompatible", elementType.isCastCompatible(elementType2));

			assertTrue("Not isSubTypeCompatible", elementType.isSubTypeCompatible(javaLangObject));
			assertTrue("Not isSubTypeCompatible", elementType.isSubTypeCompatible(elementType2));

			assertTrue("Not isAssignmentCompatible", elementType.isAssignmentCompatible(javaLangObject));
			assertTrue("Not isAssignmentCompatible", elementType.isAssignmentCompatible(elementType2));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=180905
	 */
	public void test0673() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	B foo() {\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents);
			ASTNode node = runConversion(getJLS3(), workingCopy, true, true, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertTrue("no binding recovery", unit.getAST().hasBindingsRecovery());
			assertTrue("no statement recovery", unit.getAST().hasStatementsRecovery());
			assertTrue("no binding resolution", unit.getAST().hasResolvedBindings());
			String expectedError = "B cannot be resolved to a type";
			assertProblemsSize(unit, 1, expectedError);
			assertTrue("No binding recovery", unit.getAST().hasBindingsRecovery());
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			Type type = methodDeclaration.getReturnType2();
			assertNotNull("No type", type);
			ITypeBinding typeBinding = type.resolveBinding();
			assertNotNull("No type binding", typeBinding);
			assertTrue("Not a recovered binding", typeBinding.isRecovered());
			IJavaElement javaElement = typeBinding.getJavaElement();
			assertNotNull("No java element", javaElement);
			assertEquals("Wrong java element type", IJavaElement.TYPE, javaElement.getElementType());
			assertTrue("Javalement exists", !javaElement.exists());
			IPackageBinding packageBinding = typeBinding.getPackage();
			assertNotNull("No package", packageBinding);
			assertTrue("Not the default package", packageBinding.isUnnamed());
			ITypeBinding arrayBinding = typeBinding.createArrayType(2);
			assertNotNull("No array binding", arrayBinding);
			assertEquals("Wrong dimensions", 2, arrayBinding.getDimensions());
			ITypeBinding elementType = arrayBinding.getElementType();
			assertNotNull("No element type", elementType);
			assertNotNull("No key", typeBinding.getKey());
			assertTrue("Not equals", elementType.isEqualTo(typeBinding));

			node = getASTNode(unit, 0);
			assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
			TypeDeclaration typeDeclaration = (TypeDeclaration) node;
			ITypeBinding typeBinding2 = typeDeclaration.resolveBinding();
			ITypeBinding javaLangObject = typeBinding2.getSuperclass();
			assertEquals("Not java.lang.Object", "java.lang.Object", javaLangObject.getQualifiedName());
			assertTrue("Not isCastCompatible", typeBinding.isCastCompatible(javaLangObject));
			assertTrue("Not isCastCompatible", typeBinding.isCastCompatible(elementType));

			assertTrue("Not isSubTypeCompatible", typeBinding.isSubTypeCompatible(javaLangObject));
			assertTrue("Not isSubTypeCompatible", typeBinding.isSubTypeCompatible(elementType));

			assertTrue("Not isAssignmentCompatible", typeBinding.isAssignmentCompatible(javaLangObject));
			assertTrue("Not isAssignmentCompatible", typeBinding.isAssignmentCompatible(elementType));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=180524
	 */
	public void test0674() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	Object foo() {\n" +
				"		return new Object() {/*anon*/};\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents);
			ASTNode node = runConversion(getJLS3(), workingCopy, true, true, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
			ITypeBinding binding = classInstanceCreation.resolveTypeBinding();
			assertTrue("not an anonymous type", binding.isAnonymous());
			try {
				assertNotNull(binding.createArrayType(2));
			} catch (IllegalArgumentException e) {
				assertTrue("Should not be rejected", false);
			}
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=166963
	 */
	public void test0675() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	public X(String label) {}\n" +
				"	public X() {\n" +
				"		String s= \"foo\";\n" +
				"		System.out.println(s);\n" +
				"		this(s);\n" +
				"		System.out.println(s);\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents);
			ASTNode node = runConversion(getJLS3(), workingCopy, true, true, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedError = "Constructor call must be the first statement in a constructor";
			assertProblemsSize(unit, 1, expectedError);
			node = getASTNode(unit, 0, 1, 2);
			assertEquals("Not a constructor invocation", ASTNode.CONSTRUCTOR_INVOCATION, node.getNodeType());
			ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
			assertNull("Got a binding", constructorInvocation.resolveConstructorBinding());
			List arguments = constructorInvocation.arguments();
			assertEquals("wrong size", 1, arguments.size());
			Expression expression = (Expression) arguments.get(0);
			ITypeBinding typeBinding = expression.resolveTypeBinding();
			assertNotNull("No binding", typeBinding);
			assertEquals("Wrong type", "java.lang.String", typeBinding.getQualifiedName());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=180905
	 */
	public void test0676() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0676", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertTrue("no binding recovery", unit.getAST().hasBindingsRecovery());
		assertTrue("no statement recovery", unit.getAST().hasStatementsRecovery());
		assertTrue("no binding resolution", unit.getAST().hasResolvedBindings());
		String expectedError = "B cannot be resolved to a type";
		assertProblemsSize(unit, 1, expectedError);
		assertTrue("No binding recovery", unit.getAST().hasBindingsRecovery());
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType2();
		assertNotNull("No type", type);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not a recovered binding", typeBinding.isRecovered());
		IJavaElement javaElement = typeBinding.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Wrong java element type", IJavaElement.TYPE, javaElement.getElementType());
		assertTrue("Java element exists", !javaElement.exists());
		IPackageBinding packageBinding = typeBinding.getPackage();
		assertNotNull("No package", packageBinding);
		assertNotNull("No java element for package", packageBinding.getJavaElement());
		assertEquals("Not the package test0676", "test0676", packageBinding.getName());
		ITypeBinding arrayBinding = typeBinding.createArrayType(2);
		assertNotNull("No array binding", arrayBinding);
		assertEquals("Wrong dimensions", 2, arrayBinding.getDimensions());
		ITypeBinding elementType = arrayBinding.getElementType();
		assertNotNull("No element type", elementType);
		assertNotNull("No key", typeBinding.getKey());
		assertTrue("Not equals", elementType.isEqualTo(typeBinding));

		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding2 = typeDeclaration.resolveBinding();
		ITypeBinding javaLangObject = typeBinding2.getSuperclass();
		assertEquals("Not java.lang.Object", "java.lang.Object", javaLangObject.getQualifiedName());
		assertTrue("Not isCastCompatible", typeBinding.isCastCompatible(javaLangObject));
		assertTrue("Not isCastCompatible", typeBinding.isCastCompatible(elementType));

		assertTrue("Not isSubTypeCompatible", typeBinding.isSubTypeCompatible(javaLangObject));
		assertTrue("Not isSubTypeCompatible", typeBinding.isSubTypeCompatible(elementType));

		assertTrue("Not isAssignmentCompatible", typeBinding.isAssignmentCompatible(javaLangObject));
		assertTrue("Not isAssignmentCompatible", typeBinding.isAssignmentCompatible(elementType));
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=185306
	 */
	public void test0677() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0677", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType()); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0);
		assertProblemsSize(unit, 0);
		assertNotNull(node);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		ITypeBinding superclass = typeBinding.getSuperclass();
		assertNotNull("No super class", superclass);
		IMethodBinding[] methods = superclass.getDeclaredMethods();
		assertNotNull("No methods", methods);
		assertTrue("Empty", methods.length != 0);
		IVariableBinding[] fields = superclass.getDeclaredFields();
		assertNotNull("No fields", fields);
		assertTrue("Empty", fields.length != 0);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=193979
	 */
	public void test0678() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	public String foo() {\n" +
				"		return((true ? \"\" : (\"Hello\" + \" World\") + \"!\"));\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			workingCopy.getBuffer().setContents(contents.toCharArray());
			ASTNode node = runConversion(getJLS3(), workingCopy, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			String expectedOutput = "Dead code";
			assertProblemsSize(unit, 1, expectedOutput);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement returnStatement = (ReturnStatement) node;
			Expression expression = returnStatement.getExpression();
			assertNotNull("No expression", expression);
			assertEquals("Not a parenthesized expression", ASTNode.PARENTHESIZED_EXPRESSION, expression.getNodeType());
			expression = ((ParenthesizedExpression) expression).getExpression();
			assertEquals("Not a parenthesized expression", ASTNode.PARENTHESIZED_EXPRESSION, expression.getNodeType());
			expression = ((ParenthesizedExpression) expression).getExpression();
			assertEquals("Not a conditional expression", ASTNode.CONDITIONAL_EXPRESSION, expression.getNodeType());
			ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
			final Expression elseExpression = conditionalExpression.getElseExpression();
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, elseExpression.getNodeType());
			InfixExpression infixExpression = (InfixExpression) elseExpression;
			List extendedOperands = infixExpression.extendedOperands();
			assertEquals("wrong size", 0, extendedOperands.size());
			Expression leftOperand = infixExpression.getLeftOperand();
			assertEquals("Not a parenthesized expression", ASTNode.PARENTHESIZED_EXPRESSION, leftOperand.getNodeType());
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) leftOperand;
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, parenthesizedExpression.getExpression().getNodeType());
			Expression rightOperand = infixExpression.getRightOperand();
			assertEquals("Not a string literal", ASTNode.STRING_LITERAL, rightOperand.getNodeType());
			StringLiteral stringLiteral = (StringLiteral) rightOperand;
			assertEquals("wrong value", "!", stringLiteral.getLiteralValue());
			infixExpression = (InfixExpression) parenthesizedExpression.getExpression();
			leftOperand = infixExpression.getLeftOperand();
			assertEquals("Not a string literal", ASTNode.STRING_LITERAL, leftOperand.getNodeType());
			stringLiteral = (StringLiteral) leftOperand;
			assertEquals("wrong value", "Hello", stringLiteral.getLiteralValue());
			rightOperand = infixExpression.getRightOperand();
			assertEquals("Not a string literal", ASTNode.STRING_LITERAL, rightOperand.getNodeType());
			stringLiteral = (StringLiteral) rightOperand;
			assertEquals("wrong value", " World", stringLiteral.getLiteralValue());
			assertEquals("Wrong size", 0, infixExpression.extendedOperands().size());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=169745
	 */
	public void test0679() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	int i = 1 - 2 + 3 + 4 * 5;\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			final List fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 1, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			final Expression initializer = fragment.getInitializer();
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, initializer.getNodeType());
			InfixExpression infixExpression = (InfixExpression) initializer;
			final Expression rightOperand = infixExpression.getRightOperand();
			InfixExpression rightOperand2 = (InfixExpression) rightOperand;
			assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, rightOperand2.getRightOperand().getNodeType());
			NumberLiteral literal = (NumberLiteral) rightOperand2.getRightOperand();
			assertEquals("Wrong value", "5", literal.getToken());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=196249
	 */
	public void test0680() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0680", "SAMPLE_UTF8.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("Not a compilation unit", node.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertNotNull(node);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		final List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=196354
	 */
	public void test0681() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "Sample", "Sample.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("Not a compilation unit", node.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		final PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		final IPackageBinding packageBinding = packageDeclaration.resolveBinding();
		assertNotNull("No binding", packageBinding);
		assertEquals("Wrong name", "Sample", packageBinding.getName());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=196514
	 */
	public void test0682() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0682", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true, true);
		assertTrue("Not a compilation unit", node.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(
				unit,
				2,
				"Variable must provide either dimension expressions or an array initializer\n" +
				"Syntax error on token \"String\", [ expected after this token");
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		node = expressionStatement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, node.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) node;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		node = (ASTNode)arguments.get(0);
		assertEquals("Not an array creation", ASTNode.ARRAY_CREATION, node.getNodeType());
		ArrayCreation arrayCreation = (ArrayCreation) node;
		ArrayType arrayType = arrayCreation.getType();
		checkSourceRange(arrayType, "String]", sourceUnit.getSource());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=196514
	 */
	public void test0683() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0683", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true, true);
		assertTrue("Not a compilation unit", node.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		node = expressionStatement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, node.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) node;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		node = (ASTNode)arguments.get(0);
		assertEquals("Not an array creation", ASTNode.ARRAY_CREATION, node.getNodeType());
		ArrayCreation arrayCreation = (ArrayCreation) node;
		ArrayType arrayType = arrayCreation.getType();
		checkSourceRange(arrayType, "String[0][b[10]][]", sourceUnit.getSource());
		node = componentType(arrayType);
		assertEquals("Not an array type", ASTNode.ARRAY_TYPE, node.getNodeType());
		arrayType = (ArrayType)node;
		checkSourceRange(arrayType, "String[0][b[10]]", sourceUnit.getSource());
		node = componentType(arrayType);
		assertEquals("Not an array type", ASTNode.ARRAY_TYPE, node.getNodeType());
		arrayType = (ArrayType)node;
		checkSourceRange(arrayType, "String[0]", sourceUnit.getSource());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=201929
	 */
	public void test0684() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0684", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true, true);
		assertTrue("Not a compilation unit", node.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		final AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		final List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, ((BodyDeclaration) bodyDeclarations.get(0)).getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclarations.get(0);
		final ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		final String qualifiedName = typeBinding.getQualifiedName();
		assertEquals("wrong qualified name", "", qualifiedName);
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=201929
	 */
	public void test0685() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0685", "C.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS3(), sourceUnit, true, true);
		assertTrue("Not a compilation unit", node.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		Expression expression = ((ExpressionStatement) node).getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		final AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		final List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, ((BodyDeclaration) bodyDeclarations.get(0)).getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclarations.get(0);
		final ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		final String qualifiedName = typeBinding.getQualifiedName();
		assertEquals("wrong qualified name", "", qualifiedName);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203579
	 */
	public void test0686() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	void foo() {\n" +
				"		int   a  ,   b  ;\n" +
				"		for (int  i  ,  j  ;;) {}\n" +
				"	}\n" +
				"	int   n  ,   m  ;\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a variable statement declaration", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
			VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
			List fragments = statement.fragments();
			assertEquals("Wrong size", 2, fragments.size());
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
			checkSourceRange(fragment, "a", contents);
			fragment = (VariableDeclarationFragment) fragments.get(1);
			checkSourceRange(fragment, "b", contents);
			node = getASTNode(unit, 0, 0, 1);
			assertEquals("Not a for statement", ASTNode.FOR_STATEMENT, node.getNodeType());
			ForStatement forStatement = (ForStatement) node;
			final List initializers = forStatement.initializers();
			assertEquals("Wrong size", 1, initializers.size());
			VariableDeclarationExpression expression = (VariableDeclarationExpression) initializers.get(0);
			fragments = expression.fragments();
			assertEquals("Wrong size", 2, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			assertFalse("Not a malformed node", isMalformed(fragment));
			checkSourceRange(fragment, "i", contents);
			fragment = (VariableDeclarationFragment) fragments.get(1);
			checkSourceRange(fragment, "j", contents);
			assertFalse("Not a malformed node", isMalformed(fragment));

			node = getASTNode(unit, 0, 1);
			assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			fragments = fieldDeclaration.fragments();
			assertEquals("Wrong size", 2, fragments.size());
			fragment = (VariableDeclarationFragment) fragments.get(0);
			checkSourceRange(fragment, "n", contents);
			assertFalse("Not a malformed node", isMalformed(fragment));
			fragment = (VariableDeclarationFragment) fragments.get(1);
			checkSourceRange(fragment, "m", contents);
			assertFalse("Not a malformed node", isMalformed(fragment));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=207754
	 */
	public void test0687() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	protected String foo(String string) {\n" +
				"		return (\"\" + string + \"\") + (\"\");\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			assertProblemsSize(unit, 0);
			node = getASTNode(unit, 0, 0, 0);
			assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
			ReturnStatement statement = (ReturnStatement) node;
			Expression expression = statement.getExpression();
			checkSourceRange(expression, "(\"\" + string + \"\") + (\"\")", contents);
			assertEquals("Not an infix expression", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
			InfixExpression infixExpression = (InfixExpression) expression;
			Expression leftOperand = infixExpression.getLeftOperand();
			checkSourceRange(leftOperand, "(\"\" + string + \"\")", contents);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=215858
	 */
	public void test0688() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0688", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType[] types = sourceUnit.getTypes();
		assertNotNull(types);
		assertEquals("wrong size", 1, types.length);
		IType type = types[0];
		IField field = type.getField("i");
		assertNotNull("No field", field);
		ISourceRange sourceRange = field.getNameRange();
		ASTNode result = runConversion(getJLS3(), sourceUnit, sourceRange.getOffset() + sourceRange.getLength() / 2, false);
		assertNotNull(result);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=240815
	 */
	public void test0689() throws JavaModelException {
		IJavaProject project = getJavaProject("Converter");
		if (project == null) {
			return;
		}
		// replace JCL_LIB with JCL15_LIB, and JCL_SRC with JCL15_SRC
		IClasspathEntry[] classpath = project.getRawClasspath();
		try {
			ArrayList newClasspathEntries = new ArrayList();
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];
				if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE) {
					newClasspathEntries.add(entry);
				}
			}
			IClasspathEntry[] newClasspath = new IClasspathEntry[newClasspathEntries.size()];
			newClasspathEntries.toArray(newClasspath);
			project.setRawClasspath(newClasspath, null);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0689", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(getJLS3(), sourceUnit, true, true);
			assertNotNull(result);
			ITypeBinding typeBinding = result.getAST().resolveWellKnownType("java.lang.Boolean");
			assertNull("Should be null", typeBinding);
		} finally {
			project.setRawClasspath(classpath, null);
		}
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=240815
	 */
	public void test0690() throws JavaModelException {
		IJavaProject project = getJavaProject("Converter");
		if (project == null) {
			return;
		}
		// replace JCL_LIB with JCL15_LIB, and JCL_SRC with JCL15_SRC
		IClasspathEntry[] classpath = project.getRawClasspath();
		try {
			ArrayList newClasspathEntries = new ArrayList();
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];
				if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE) {
					newClasspathEntries.add(entry);
				}
			}
			IClasspathEntry[] newClasspath = new IClasspathEntry[newClasspathEntries.size()];
			newClasspathEntries.toArray(newClasspath);
			project.setRawClasspath(newClasspath, null);
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0690", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(getJLS3(), sourceUnit, true, true, true);
			assertNotNull(result);
			ITypeBinding typeBinding = result.getAST().resolveWellKnownType("java.lang.Boolean");
			assertNull("Should be null", typeBinding);
		} finally {
			project.setRawClasspath(classpath, null);
		}
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=242292
	 */
	public void test0691() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "test0691", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType type = unit.getType("X");
		IMethod method = type.getMethod("foo", new String[0]);

		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource(unit);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		parser.setCompilerOptions(options);
		ISourceRange range = method.getSourceRange();
		parser.setSourceRange(range.getOffset(), range.getLength());
		ASTNode node = parser.createAST(null);
		assertNotNull("No node", node);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=242961
	 */
	public void test0692() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"/*start*/public class X {\n" +
				"	int k;\n" +
				"	Zork z;\n" +
				"}/*end*/";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			TypeDeclaration typeDeclaration = (TypeDeclaration) buildAST(
				contents,
				workingCopy,
				false,
				false,
				false);
			ITypeBinding binding = typeDeclaration.resolveBinding();
			IVariableBinding[] declaredFields = binding.getDeclaredFields();
			assertEquals("Wrong size", 1, declaredFields.length);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=243715
	 */
	public void test0693() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "test0693", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ICompilationUnit unit2 = getCompilationUnit("Converter" , "src", "test0693", "Y.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		ASTParser parser = ASTParser.newParser(getJLS3());
		ASTRequestor requestor = new ASTRequestor() {};
		ICompilationUnit[] cus = new ICompilationUnit[2];
		cus[0] = unit;
		cus[1] = unit2;

		try {
			// the following line will throw exception but seemingly shouldn't
			parser.createASTs(cus, new String[0], requestor, null);
		} catch(Exception e) {
			e.printStackTrace();
			assertFalse("Should not get there", true);
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=243715
	 */
	public void test0694() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "test0694", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ICompilationUnit unit2 = getCompilationUnit("Converter" , "src", "test0694", "Y.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		ASTParser parser = ASTParser.newParser(getJLS3());
		ASTRequestor requestor = new ASTRequestor() {};
		ICompilationUnit[] cus = new ICompilationUnit[2];
		cus[0] = unit;
		cus[1] = unit2;

		try {
			// the following line will throw exception but seemingly shouldn't
			parser.createASTs(cus, new String[0], requestor, null);
		} catch(Exception e) {
			e.printStackTrace();
			assertFalse("Should not get there", true);
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=245348
	 */
	public void test0695() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "test0695", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit.getSource().toCharArray());
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
		parser.setCompilerOptions(options);

		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		ASTVisitor visitor = new ASTVisitor() {
			public boolean visit(EnumDeclaration node) {
				assertFalse("Should not be there", true);
				return false;
			}
		};
		astRoot.accept(visitor);
		assertEquals("No problem found", 1, astRoot.getProblems().length);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=245348
	 */
	public void test0696() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "test0696", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit.getSource().toCharArray());
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
		parser.setCompilerOptions(options);

		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		ASTVisitor visitor = new ASTVisitor() {
			public boolean visit(AnnotationTypeDeclaration node) {
				assertFalse("Should not be there", true);
				return false;
			}
		};
		astRoot.accept(visitor);
		assertEquals("No problem found", 1, astRoot.getProblems().length);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248246
	 */
	public void test0697() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	/*start*/private void foo() {\n" +
				"		Object o = new new Object() {};\n" +
				"	}/*end*/\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			MethodDeclaration methodDeclaration = (MethodDeclaration) buildAST(
				contents,
				workingCopy,
				false,
				false,
				true);
			Block body = methodDeclaration.getBody();
			assertEquals("Should contain 1 statement", 1, body.statements().size());
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248246
	 */
	public void test0698a() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private void foo() {\n" +
				"		Object o = new /*start*/new Object() {}/*end*/;\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			ExpressionStatement statement = (ExpressionStatement) buildAST(
				contents,
				workingCopy,
				false,
				true,
				true);
			String expectedContents = "new Object() {}";
			checkSourceRange(statement, expectedContents, new MarkerInfo(contents).source);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248246
	 */
	public void test0698b() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			String contents =
				"public class X {\n" +
				"	private void foo() {\n" +
				"		/*start*/Object o = new /*end*/new Object() {};\n" +
				"	}\n" +
				"}";
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			VariableDeclarationStatement statement = (VariableDeclarationStatement) buildAST(
				contents,
				workingCopy,
				false,
				true,
				true);
			String expectedContents = "Object o = new ";
			checkSourceRange(statement, expectedContents, new MarkerInfo(contents).source);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264443
	public void test0699() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/example/Test.java", true/*resolve*/);
			String contents =
				"package example;\n" +
				"public class Test {\n" +
				"	public void test() throws Throwable {\n" +
				"		B /*start*/b = new B()/*end*/;\n" +
				"	}\n" +
				"}";

			VariableDeclarationFragment fragment = (VariableDeclarationFragment) buildAST(contents, workingCopy, false, true, true);
			IVariableBinding variableBinding = fragment.resolveBinding();
			final String key = variableBinding.getKey();
			ASTParser parser = ASTParser.newParser(getJLS3());
			parser.setProject(workingCopy.getJavaProject());
			parser.setResolveBindings(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			parser.createASTs(
					new ICompilationUnit[] { workingCopy },
					new String[] { key },
					new ASTRequestor() {
						public void acceptBinding(String bindingKey,
								IBinding binding) {
							assertEquals("Wrong key", key, bindingKey);
							assertTrue("Not a variable binding", binding.getKind() == IBinding.VARIABLE);
						}

						public void acceptAST(ICompilationUnit source,
								CompilationUnit astCompilationUnit) {
						}
					}, null);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264443
	public void test0700() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/example/Test.java", true/*resolve*/);
			String contents =
				"package example;\n" +
				"import java.io.IOException;\n" +
				"public class Test {\n" +
				"	public void test() throws IOException, RuntimeException {\n" +
				"		B /*start*/b = new B()/*end*/;\n" +
				"	}\n" +
				"}";

			VariableDeclarationFragment fragment = (VariableDeclarationFragment) buildAST(contents, workingCopy, false, true, true);
			IVariableBinding variableBinding = fragment.resolveBinding();
			final String key = variableBinding.getKey();
			ASTParser parser = ASTParser.newParser(getJLS3());
			parser.setProject(workingCopy.getJavaProject());
			parser.setResolveBindings(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			parser.createASTs(
					new ICompilationUnit[] { workingCopy },
					new String[] { key },
					new ASTRequestor() {
						public void acceptBinding(String bindingKey,
								IBinding binding) {
							assertEquals("Wrong key", key, bindingKey);
							assertTrue("Not a variable binding", binding.getKind() == IBinding.VARIABLE);
						}

						public void acceptAST(ICompilationUnit source,
								CompilationUnit astCompilationUnit) {
						}
					}, null);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=264443
	//no thrown exceptions
	public void test0701() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/example/Test.java", true/*resolve*/);
			String contents =
				"package example;\n" +
				"import java.io.IOException;\n" +
				"public class Test {\n" +
				"	public void test() {\n" +
				"		B /*start*/b = new B()/*end*/;\n" +
				"	}\n" +
				"}";

			VariableDeclarationFragment fragment = (VariableDeclarationFragment) buildAST(contents, workingCopy, false, true, true);
			IVariableBinding variableBinding = fragment.resolveBinding();
			final String key = variableBinding.getKey();
			ASTParser parser = ASTParser.newParser(getJLS3());
			parser.setProject(workingCopy.getJavaProject());
			parser.setResolveBindings(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			parser.createASTs(
					new ICompilationUnit[] { workingCopy },
					new String[] { key },
					new ASTRequestor() {
						public void acceptBinding(String bindingKey,
								IBinding binding) {
							assertEquals("Wrong key", key, bindingKey);
							assertTrue("Not a variable binding", binding.getKind() == IBinding.VARIABLE);
						}
						public void acceptAST(ICompilationUnit source,
								CompilationUnit astCompilationUnit) {
						}
					}, null);
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0702() throws JavaModelException {
		final char[] source = ("void foo() {\n" +
				"	Integer I = new ${cursor}\n" +
				"}").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration declaration = (MethodDeclaration) bodyDeclaration;
		// check if there is a body with one statement in it
		assertEquals("No statement found", 1, declaration.getBody().statements().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0703() throws JavaModelException {
		final char[] source = ("public class Try {\n" +
				"	void foo() {\n" +
				"		Integer I = new ${cursor}\n" +
				"	}\n" +
				"}").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a compilation declaration", ASTNode.COMPILATION_UNIT, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) ((CompilationUnit) root).types().get(0);
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration declaration = (MethodDeclaration) bodyDeclaration;
		// check if there is a body with one statement in it
		assertEquals("No statement found", 1, declaration.getBody().statements().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0704() throws JavaModelException {
		final char[] source = ("{\n" +
				"	Integer I = new ${cursor}\n" +
				"}").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not an initializer", ASTNode.INITIALIZER, bodyDeclaration.getNodeType());
		Initializer initializer = (Initializer) bodyDeclaration;
		// check if there is a body with one statement in it
		assertEquals("No statement found", 1, initializer.getBody().statements().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0705() throws JavaModelException {
		final char[] source = ("{\n" +
				"	Integer I = new ${cursor}\n" +
				"}\n" +
				"{\n" +
				"	Integer I = new ${cursor}\n" +
				"}").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 2, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not an initializer", ASTNode.INITIALIZER, bodyDeclaration.getNodeType());
		Initializer initializer = (Initializer) bodyDeclaration;
		// check if there is a body with one statement in it
		assertEquals("No statement found", 1, initializer.getBody().statements().size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(1);
		assertEquals("Not an initializer", ASTNode.INITIALIZER, bodyDeclaration.getNodeType());
		initializer = (Initializer) bodyDeclaration;
		// check if there is a body with one statement in it
		assertEquals("No statement found", 1, initializer.getBody().statements().size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0706() throws JavaModelException {
		final char[] source = ("public class Try {\n" +
				"	Integer i = new Integer() {\n" +
				"		Integer I = new ${cursor}\n" +
				"	};\"\n" +
				"}").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a compilation declaration", ASTNode.COMPILATION_UNIT, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) ((CompilationUnit) root).types().get(0);
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0707() throws JavaModelException {
		final char[] source = ("Integer i = new Integer() {\n" +
				"	Integer I = new ${cursor}\n" +
				"};").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		List bodyDeclarations = ((TypeDeclaration) root).bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=270148
	 */
	public void test0708() throws JavaModelException {
		final char[] source = ("System.out.println()\nint i;\n").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a block", ASTNode.BLOCK, root.getNodeType());
		Block block = (Block) root;
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=270367
	public void test0709() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			String contents =
				"public class X {\n" +
				"	public Integer test() {\n" +
				"		return (new Integer(getId()));\n" +
				"	}\n" +
				"	public String getId() {\n" +
				"		return \"1\";\n" +
				"	}\n" +
				"}";

			ASTNode node = buildAST(
					contents,
					workingCopy);
			node.accept(new ASTVisitor() {
				public boolean visit(ParenthesizedExpression parenthesizedExpression) {
					assertNotNull(parenthesizedExpression.resolveTypeBinding());
					return true;
				}
			});
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=280079
	 */
	public void test0710() throws JavaModelException {
		final char[] source = (";").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 0, bodyDeclarations.size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=280079
	 */
	public void test0711() throws JavaModelException {
		final char[] source = (";void foo() {}").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=280079
	 */
	public void test0712() throws JavaModelException {
		final char[] source = (";void foo() {};").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=280063
	 */
	public void test0713() throws JavaModelException {
		final char[] source = ("  class MyCommand extends CompoundCommand\n" +
				"  {\n" +
				"    public void execute()\n" +
				"    {\n" +
				"      // ...\n" +
				"      appendAndExecute(new AddCommand(...));\n" +
				"      if (condition) appendAndExecute(new AddCommand(...));\n" +
				"    }\n" +
				"  }").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(false);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.COMPILATION_UNIT, root.getNodeType());
		CompilationUnit unit = (CompilationUnit) root;
		assertTrue("No problem reported", unit.getProblems().length != 0);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=280063
	 */
	public void test0714() throws JavaModelException {
		final char[] source = ("class MyCommand extends CommandBase\n" +
				"  {\n" +
				"    protected Command subcommand;\n" +
				"\n" +
				"    //...\n" +
				"\n" +
				"    public void execute()\n" +
				"    {\n" +
				"      // ...\n" +
				"      Compound subcommands = new CompoundCommand();\n" +
				"      subcommands.appendAndExecute(new AddCommand(...));\n" +
				"      if (condition) subcommands.appendAndExecute(new AddCommand(...));\n" +
				"      subcommand = subcommands.unwrap();\n" +
				"    }\n" +
				"\n" +
				"    public void undo()\n" +
				"    {\n" +
				"      // ...\n" +
				"      subcommand.undo();\n" +
				"    }\n" +
				"\n" +
				"    public void redo()\n" +
				"    {\n" +
				"      // ...\n" +
				"      subcommand.redo();\n" +
				"    }\n" +
				"\n" +
				"    public void dispose()\n" +
				"    {\n" +
				"      // ...\n" +
				"      if (subcommand != null)\n" +
				"     {\n" +
				"        subcommand.dispose();\n" +
				"      }\n" +
				"    }\n" +
				"  }").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(false);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.COMPILATION_UNIT, root.getNodeType());
		CompilationUnit unit = (CompilationUnit) root;
		assertTrue("No problem reported", unit.getProblems().length != 0);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=288211
	 */
	public void test0715() throws JavaModelException {
		final char[] source = ("System.out.println()\nint i;\n").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setStatementsRecovery(true);
		parser.setIgnoreMethodBodies(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a block", ASTNode.BLOCK, root.getNodeType());
		Block block = (Block) root;
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=288211
	 */
	public void test0716() {
		String src = "switch (state) {case 4:double M0,M1;}";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setIgnoreMethodBodies(true);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Wrong type", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		assertNotNull("No statements", statements);
		assertEquals("Wrong size", 1, statements.size());
		final ASTNode node = (ASTNode) statements.get(0);
		assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, node.getNodeType());
		SwitchStatement statement = (SwitchStatement) node;
		statements = statement.statements();
		assertEquals("wrong size", 2, statements.size());
		assertEquals("Not a switch case", ASTNode.SWITCH_CASE, ((ASTNode) statements.get(0)).getNodeType());
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, ((ASTNode) statements.get(1)).getNodeType());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=288211
	 */
	public void test0717() throws JavaModelException {
		final char[] source = ("  class MyCommand extends CompoundCommand\n" +
				"  {\n" +
				"    public void execute()\n" +
				"    {\n" +
				"      // ...\n" +
				"      appendAndExecute(new AddCommand());\n" +
				"      if (condition) appendAndExecute(new AddCommand());\n" +
				"    }\n" +
				"  }").toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setStatementsRecovery(false);
		parser.setIgnoreMethodBodies(true);
		parser.setSource(source);
		ASTNode root = parser.createAST(null);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, root.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) root;
		typeDeclaration.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				Block body = node.getBody();
				assertNotNull(body);
				assertTrue(body.statements().size() == 0);
				return true;
			}
		});
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=296629
	public void test0718() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			String contents =
				"public class X {\n" +
				"	public void v() {\n" +
				"		class Test2 {}\n" +
				"		Test2 t = get();\n" +
				"		t.toString();\n" +
				"	}\n" +
				"	public Object get() {return null;}\n" +
				"}";

			CompilationUnit unit = (CompilationUnit) buildAST(
					contents,
					workingCopy,
					false);
			VariableDeclarationStatement statement = (VariableDeclarationStatement) getASTNode(unit, 0, 0, 1);
			ITypeBinding typeBinding = statement.getType().resolveBinding();
			ITypeBinding typeBinding2 = ((VariableDeclarationFragment) statement.fragments().get(0)).getInitializer().resolveTypeBinding();
			assertTrue("Is not cast compatible", typeBinding.isCastCompatible(typeBinding2));
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=258905
	public void test0719() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter/src/X.java", true/*resolve*/);
			String contents =
				"public class X {}";

			CompilationUnit unit = (CompilationUnit) buildAST(
					contents,
					workingCopy,
					true);
			final AST currentAst = unit.getAST();
			// well known bindings
			String[] wkbs = {
				"byte", "char", "short", "int", "long", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"boolean", "float", "double", "void", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"java.lang.AssertionError", //$NON-NLS-1$
				"java.lang.Class", //$NON-NLS-1$
				"java.lang.Cloneable", //$NON-NLS-1$
				"java.lang.Error", //$NON-NLS-1$
				"java.lang.Exception", //$NON-NLS-1$
				"java.lang.Object", //$NON-NLS-1$
				"java.lang.RuntimeException", //$NON-NLS-1$
				"java.lang.String", //$NON-NLS-1$
				"java.lang.StringBuffer", //$NON-NLS-1$
				"java.lang.Throwable", //$NON-NLS-1$
				"java.io.Serializable", //$NON-NLS-1$
				"java.lang.Boolean", //$NON-NLS-1$
				"java.lang.Byte", //$NON-NLS-1$
				"java.lang.Character", //$NON-NLS-1$
				"java.lang.Double", //$NON-NLS-1$
				"java.lang.Float", //$NON-NLS-1$
				"java.lang.Integer", //$NON-NLS-1$
				"java.lang.Long", //$NON-NLS-1$
				"java.lang.Short", //$NON-NLS-1$
				"java.lang.Void", //$NON-NLS-1$
			};

			// no-so-well-known bindings
			String[] nwkbs = {
				"verylong", //$NON-NLS-1$
				"java.lang.Math", //$NON-NLS-1$
				"com.example.MyCode", //$NON-NLS-1$
			};

			// all of the well known bindings resolve
			for (int i = 0; i<wkbs.length; i++) {
				assertNotNull("No binding for " + wkbs[i], currentAst.resolveWellKnownType(wkbs[i]));
			}

			// none of the no so well known bindings resolve
			for (int i = 0; i<nwkbs.length; i++) {
				assertNull("Binding for " + nwkbs[i], currentAst.resolveWellKnownType(nwkbs[i]));
			}
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=47396
	 */
	public void test0720() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0720", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of problems", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0);
		assertNotNull(node);
		assertTrue("Not a constructor declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration declaration = (MethodDeclaration) node;
		assertTrue("A constructor", !declaration.isConstructor());
		checkSourceRange(declaration, "public void method(final int parameter) {     }", source, true/*expectMalformed*/);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=342455
	 */
	public void test0721() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "src", "test0721", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		ASTNode node = getASTNode(unit, 0);
		TypeDeclaration declaration = (TypeDeclaration) node;
		Javadoc javadoc = declaration.getJavadoc();
		List tags = javadoc.tags();
		TagElement element = (TagElement) tags.get(0);
		List fragments = element.fragments();
		element = (TagElement) fragments.get(0);
		assertEquals("wrong tag name", "@code", element.getTagName());
		checkSourceRange((TextElement) element.fragments().get(0), " stars*", source);

		element = (TagElement) fragments.get(2);
		assertEquals("wrong tag name", "@literal", element.getTagName());
		checkSourceRange((TextElement) element.fragments().get(0), " stars****", source);

		element = (TagElement) fragments.get(4);
		assertEquals("wrong tag name", "@code", element.getTagName());
		checkSourceRange((TextElement) element.fragments().get(0), " space* ", source);

		element = (TagElement) fragments.get(5);
		assertEquals("wrong tag name", "@code", element.getTagName());
		checkSourceRange((TextElement) element.fragments().get(0), " stars* ", source);

		element = (TagElement) fragments.get(7);
		assertEquals("wrong tag name", "@literal", element.getTagName());
		checkSourceRange((TextElement) element.fragments().get(0), " stars**** ", source);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361938
	public void test0722() throws JavaModelException {
		String source = "public class X {\n" +
						"  X(){\n" +
						"    try {\n" +
						"    }\n" +
						"  finally {\n" +
						"    }\n" +
						"  }\n" +
						"}\n";

		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		CompilationUnit resultCompilationUnit = (CompilationUnit) parser.createAST(null);
		Object o = resultCompilationUnit.types().get(0);
		assertEquals(o.toString(), source);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=455986
	 */
	public void test0723() {
		String src = "super();";
		char[] source = src.toCharArray();
		ASTParser parser = ASTParser.newParser(getJLS3());
		parser.setKind (ASTParser.K_STATEMENTS);
		parser.setIgnoreMethodBodies(true);
		parser.setSource (source);
		ASTNode result = parser.createAST (null);
		assertNotNull("no result", result);
		assertEquals("Wrong type", ASTNode.BLOCK, result.getNodeType());
		Block block = (Block) result;
		List statements = block.statements();
		assertNotNull("No statements", statements);
		assertEquals("Wrong size", 1, statements.size());
		final ASTNode node = (ASTNode) statements.get(0);
		assertEquals("Not a super constructor call", ASTNode.SUPER_CONSTRUCTOR_INVOCATION, node.getNodeType());
		SuperConstructorInvocation statement = (SuperConstructorInvocation) node;
		checkSourceRange(statement, "super();", source);
	}
}
