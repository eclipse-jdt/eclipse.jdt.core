/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial test suite for AST API
 ******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public class ASTConverterTest2 extends ConverterTestSetup {
	
	public ASTConverterTest2(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterTest2.class.getName());		

		if (true) {
			Class c = ASTConverterTest2.class;
			Method[] methods = c.getMethods();
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterTest2(methods[i].getName()));
				}
			}
			return suite;
		}
		suite.addTest(new ASTConverterTest2("test0447"));			
		return suite;
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=22560
	 */
	public void test0401() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0401", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
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
		methodDeclaration.getReturnType();
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No typebinding", typeBinding); //$NON-NLS-1$
		assertEquals("wrong name", "int", typeBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23464
	 */
	public void test0402() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0402", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 0);
		assertEquals("Wrong number of errors", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		assertNotNull(node);
		assertTrue("Not a super method invocation", node.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION); //$NON-NLS-1$
		checkSourceRange(node, "new A().super();", source); //$NON-NLS-1$
	}	
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
	 */
	public void test0403() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0403", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 1);
		assertEquals("Wrong number of errors", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
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
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23597
	 */
	public void test0404() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0404", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertEquals("Wrong number of errors", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0405", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 1, 0, 1);
		assertEquals("Wrong number of errors", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0406", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0, 1);
		assertEquals("Wrong number of errors", 1, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
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
	}	

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23162
	 */
	public void test0407() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0407", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertEquals("Wrong number of errors", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
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
	}
	
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23162
	 */
	public void test0408() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0408", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertEquals("Wrong number of errors", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType();
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
			ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0409", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			ASTNode result = runConversion(sourceUnit, true);
			assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
			CompilationUnit compilationUnit = (CompilationUnit) result; 
			assertEquals("Wrong number of errors", 0, compilationUnit.getProblems().length); //$NON-NLS-1$
			BindingsCollectorVisitor bindingsCollectorVisitor = new BindingsCollectorVisitor();
			compilationUnit.accept(bindingsCollectorVisitor);
			assertEquals("wrong number", 3, bindingsCollectorVisitor.getUnresolvedNodesSet().size()); //$NON-NLS-1$
			Map bindingsMap = bindingsCollectorVisitor.getBindingsMap();
			assertEquals("wrong number", 187, bindingsMap.size()); //$NON-NLS-1$
			ASTNodesCollectorVisitor nodesCollector = new ASTNodesCollectorVisitor();
			compilationUnit.accept(nodesCollector);
			Set detachedNodes = nodesCollector.getDetachedAstNodes();
			int counter = 0;
			for (Iterator iterator = detachedNodes.iterator(); iterator.hasNext(); ) {
				ASTNode detachedNode = (ASTNode) iterator.next();
				counter++;
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0410", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertEquals("Wrong number of errors", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0411", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertEquals("Wrong number of errors", 0, ((CompilationUnit) result).getProblems().length); //$NON-NLS-1$
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0412", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0413", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 1, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List throwsException = methodDeclaration.thrownExceptions();
		assertEquals("wrong size", 2, throwsException.size()); //$NON-NLS-1$
		Name name = (Name) throwsException.get(0);
		IBinding binding = name.resolveBinding();
		assertNull("Got a binding", binding); //$NON-NLS-1$
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23734
	 */
	public void test0414() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0414", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$
		ASTNode node = getASTNode(unit, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No type binding", typeBinding); //$NON-NLS-1$
		ASTNode declaringNode = unit.findDeclaringNode(typeBinding);
		assertNull("Got a declaring node", declaringNode); //$NON-NLS-1$

		node = getASTNode(unit, 0, 1);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration2 = (MethodDeclaration) node;
		Type type2 = methodDeclaration2.getReturnType();
		ITypeBinding typeBinding2 = type2.resolveBinding();
		assertNotNull("No type binding", typeBinding2); //$NON-NLS-1$
		ASTNode declaringNode2 = unit.findDeclaringNode(typeBinding2);
		assertNotNull("No declaring node", declaringNode2); //$NON-NLS-1$

		ICompilationUnit sourceUnit2 = getCompilationUnit("Converter" , "", "test0414", "B.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		result = runConversion(sourceUnit2, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit2 = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit2.getProblems().length); //$NON-NLS-1$
		ASTNode declaringNode3 = unit2.findDeclaringNode(typeBinding);
		assertNull("Got a declaring node", declaringNode3); //$NON-NLS-1$
		
		ASTNode declaringNode4 = unit2.findDeclaringNode(typeBinding.getKey());
		assertNotNull("No declaring node", declaringNode4); //$NON-NLS-1$
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24268
	 */
	public void test0415() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0415", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		assertEquals("wrong toString()", "default : ", defaultCase.toString());
	}
	
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24324
	 */
	public void test0416() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0416", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0417", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0418", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0419", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0420", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0421", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0422", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0423", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0424", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0425", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0426", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0427", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24449
	 */
	public void test0428() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0428", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0429", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0430", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0, 0);
		assertTrue("Not a constructor invocation", node.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION);
		ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0431", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0432", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0433", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24501
	 */
	public void test0434() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0434", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0435", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0436", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		IBinding binding = name.resolveBinding();
		assertNull("Got a binding", binding);
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		SimpleName simpleName = qualifiedName.getName();
		assertEquals("wrong name", "CInner", simpleName.getIdentifier());
		IBinding binding2 = simpleName.resolveBinding();
		assertNull("Got a binding", binding2);
	}	

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24502
	 */
	public void test0437() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0437", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		assertNull("No binding", binding);
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24511
	 */
	public void test0438() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0438", "D.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0439", "E.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0440", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0441", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0442", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0443", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 2, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertNotNull("No body", methodDeclaration.getBody());
		assertNotNull("No binding", methodDeclaration.resolveBinding());
		assertTrue("Not an abstract method", Modifier.isAbstract(methodDeclaration.getModifiers())); 
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24623
	 */
	public void test0444() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0444", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 2, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0);
		assertEquals("Wrong type", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 2, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration)bodyDeclarations.get(0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		assertEquals("Wrong name", "foo", methodDeclaration.getName().getIdentifier());
		assertNull("Got a binding", methodDeclaration.resolveBinding());
		bodyDeclaration = (BodyDeclaration)bodyDeclarations.get(1);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		assertEquals("Wrong name", "foo", ((MethodDeclaration) bodyDeclaration).getName().getIdentifier());
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=24773
	 */
	public void test0445() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0445", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25018
	 */
	public void test0446() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0446", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 1, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25124
	 */
	public void test0447() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0447", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 4, unit.getProblems().length); //$NON-NLS-1$<
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25330
	 */
	public void test0448() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0448", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", node.getNodeType(), ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
		ITypeBinding returnTypeBinding = methodDeclaration.getReturnType().resolveBinding();
		assertNotNull("No return type binding", returnTypeBinding);
		Block block = methodDeclaration.getBody();
		assertNotNull("No method body", block);
		assertEquals("wrong size", 0, block.statements().size()); 
	}
	
	/**
	 * Check that the implicit super constructor call is not there
	 */
	public void test0449() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter" , "", "test0449", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(sourceUnit, true);
		assertTrue("not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT); //$NON-NLS-1$
		CompilationUnit unit = (CompilationUnit) result;
		assertEquals("Wrong number of errors", 0, unit.getProblems().length); //$NON-NLS-1$<
		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", node.getNodeType(), ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Not a constructor", methodDeclaration.isConstructor());
		Block block = methodDeclaration.getBody();
		assertNotNull("No method body", block);
		assertEquals("wrong size", 1, block.statements().size()); 
	}	
}

