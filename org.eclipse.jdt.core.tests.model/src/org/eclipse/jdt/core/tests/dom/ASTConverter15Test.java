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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class ASTConverter15Test extends ConverterTestSetup {
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS3);
	}
	
	public ASTConverter15Test(String name) {
		super(name);
	}

	public static Test suite() {
		if (true) {
			return new Suite(ASTConverter15Test.class);
		}
		TestSuite suite = new Suite(ASTConverter15Test.class.getName());
		suite.addTest(new ASTConverter15Test("test0086"));
		return suite;
	}
		
	public void test0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Wrong number of types", 1, types.size());
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) types.get(0);
		assertEquals("wrong type", ASTNode.TYPE_DECLARATION, typeDeclaration.getNodeType());
		TypeDeclaration typeDeclaration2 = (TypeDeclaration) typeDeclaration;
		List modifiers = typeDeclaration2.modifiers();
		assertEquals("Wrong number of modifiers", 1, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(0);
		checkSourceRange(modifier, "public", source);
		
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		modifiers = fieldDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 3, modifiers.size());
		modifier = (Modifier) modifiers.get(0);
		checkSourceRange(modifier, "public", source);
		modifier = (Modifier) modifiers.get(1);
		checkSourceRange(modifier, "static", source);
		modifier = (Modifier) modifiers.get(2);
		checkSourceRange(modifier, "final", source);
		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		modifier = (Modifier) modifiers.get(0);
		checkSourceRange(modifier, "private", source);
		modifier = (Modifier) modifiers.get(1);
		checkSourceRange(modifier, "static", source);
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong number of parameters", 1, parameters.size());
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		modifiers = variableDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 1, modifiers.size());
		modifier = (Modifier) modifiers.get(0);
		checkSourceRange(modifier, "final", source);
		
		node = getASTNode(compilationUnit, 0, 2);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		modifier = (Modifier) modifiers.get(0);
		checkSourceRange(modifier, "public", source);
		modifier = (Modifier) modifiers.get(1);
		checkSourceRange(modifier, "static", source);
	}
	
	public void test0002() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0002", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	public void test0003() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0003", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Wrong number of types", 3, types.size());
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) types.get(2);
		assertEquals("wrong type", ASTNode.TYPE_DECLARATION, typeDeclaration.getNodeType());
		TypeDeclaration typeDeclaration2 = (TypeDeclaration) typeDeclaration;
		List modifiers = typeDeclaration2.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		ASTNode modifier = (ASTNode) modifiers.get(0);
		checkSourceRange(modifier, "@Author(@Name(first=\"Joe\", last=\"Hacker\"))", source);
		assertEquals("wrong type", ASTNode.SINGLE_MEMBER_ANNOTATION, modifier.getNodeType());
		SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
		checkSourceRange(annotation.getTypeName(), "Author", source);
		Expression value = annotation.getValue();
		assertEquals("wrong type", ASTNode.NORMAL_ANNOTATION, value.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) value;
		checkSourceRange(normalAnnotation.getTypeName(), "Name", source);
		List values = normalAnnotation.values();
		assertEquals("wrong size", 2, values.size());
		MemberValuePair memberValuePair = (MemberValuePair) values.get(0);
		checkSourceRange(memberValuePair, "first=\"Joe\"", source);
		checkSourceRange(memberValuePair.getName(), "first", source);
		checkSourceRange(memberValuePair.getValue(), "\"Joe\"", source);
		memberValuePair = (MemberValuePair) values.get(1);
		checkSourceRange(memberValuePair, "last=\"Hacker\"", source);		
		checkSourceRange(memberValuePair.getName(), "last", source);
		checkSourceRange(memberValuePair.getValue(), "\"Hacker\"", source);
		modifier = (ASTNode) modifiers.get(1);
		checkSourceRange(modifier, "public", source);
	}
	
	public void test0004() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0004", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Wrong number of types", 3, types.size());
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) types.get(2);
		assertEquals("wrong type", ASTNode.TYPE_DECLARATION, typeDeclaration.getNodeType());
		TypeDeclaration typeDeclaration2 = (TypeDeclaration) typeDeclaration;
		List modifiers = typeDeclaration2.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		ASTNode modifier = (ASTNode) modifiers.get(1);
		checkSourceRange(modifier, "@Author(@Name(first=\"Joe\", last=\"Hacker\"))", source);
		assertEquals("wrong type", ASTNode.SINGLE_MEMBER_ANNOTATION, modifier.getNodeType());
		SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
		checkSourceRange(annotation.getTypeName(), "Author", source);
		Expression value = annotation.getValue();
		assertEquals("wrong type", ASTNode.NORMAL_ANNOTATION, value.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) value;
		checkSourceRange(normalAnnotation.getTypeName(), "Name", source);
		List values = normalAnnotation.values();
		assertEquals("wrong size", 2, values.size());
		MemberValuePair memberValuePair = (MemberValuePair) values.get(0);
		checkSourceRange(memberValuePair, "first=\"Joe\"", source);
		checkSourceRange(memberValuePair.getName(), "first", source);
		checkSourceRange(memberValuePair.getValue(), "\"Joe\"", source);
		memberValuePair = (MemberValuePair) values.get(1);
		checkSourceRange(memberValuePair, "last=\"Hacker\"", source);		
		checkSourceRange(memberValuePair.getName(), "last", source);
		checkSourceRange(memberValuePair.getValue(), "\"Hacker\"", source);
		modifier = (ASTNode) modifiers.get(0);
		checkSourceRange(modifier, "public", source);
	}
	
	public void test0005() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0005", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Wrong number of types", 4, types.size());
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) types.get(3);
		assertEquals("wrong type", ASTNode.TYPE_DECLARATION, typeDeclaration.getNodeType());
		TypeDeclaration typeDeclaration2 = (TypeDeclaration) typeDeclaration;
		List modifiers = typeDeclaration2.modifiers();
		assertEquals("Wrong number of modifiers", 3, modifiers.size());
		ASTNode modifier = (ASTNode) modifiers.get(0);
		checkSourceRange(modifier, "@Retention", source);
		assertEquals("wrong type", ASTNode.MARKER_ANNOTATION, modifier.getNodeType());
		MarkerAnnotation markerAnnotation = (MarkerAnnotation) modifier;
		checkSourceRange(markerAnnotation.getTypeName(), "Retention", source);
		modifier = (ASTNode) modifiers.get(2);
		checkSourceRange(modifier, "@Author(@Name(first=\"Joe\", last=\"Hacker\", age=32))", source);
		assertEquals("wrong type", ASTNode.SINGLE_MEMBER_ANNOTATION, modifier.getNodeType());
		SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
		checkSourceRange(annotation.getTypeName(), "Author", source);
		Expression value = annotation.getValue();
		assertEquals("wrong type", ASTNode.NORMAL_ANNOTATION, value.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) value;
		checkSourceRange(normalAnnotation.getTypeName(), "Name", source);
		List values = normalAnnotation.values();
		assertEquals("wrong size", 3, values.size());
		MemberValuePair memberValuePair = (MemberValuePair) values.get(0);
		checkSourceRange(memberValuePair, "first=\"Joe\"", source);
		checkSourceRange(memberValuePair.getName(), "first", source);
		checkSourceRange(memberValuePair.getValue(), "\"Joe\"", source);
		memberValuePair = (MemberValuePair) values.get(1);
		checkSourceRange(memberValuePair, "last=\"Hacker\"", source);		
		checkSourceRange(memberValuePair.getName(), "last", source);
		checkSourceRange(memberValuePair.getValue(), "\"Hacker\"", source);
		memberValuePair = (MemberValuePair) values.get(2);
		checkSourceRange(memberValuePair, "age=32", source);		
		checkSourceRange(memberValuePair.getName(), "age", source);
		checkSourceRange(memberValuePair.getValue(), "32", source);
		modifier = (ASTNode) modifiers.get(1);
		checkSourceRange(modifier, "public", source);
		
		typeDeclaration = (AbstractTypeDeclaration) types.get(0);
		assertEquals("wrong type", ASTNode.ANNOTATION_TYPE_DECLARATION, typeDeclaration.getNodeType());
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) typeDeclaration;
		List bodyDeclarations = annotationTypeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 3, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("wrong type", ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION, bodyDeclaration.getNodeType());
		AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration = (AnnotationTypeMemberDeclaration) bodyDeclaration;
		IMethodBinding methodBinding = annotationTypeMemberDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		checkSourceRange(annotationTypeMemberDeclaration, "String first() default \"Joe\";", source);
		Expression expression = annotationTypeMemberDeclaration.getDefault();
		checkSourceRange(expression, "\"Joe\"", source);
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(2);
		assertEquals("wrong type", ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION, bodyDeclaration.getNodeType());
		annotationTypeMemberDeclaration = (AnnotationTypeMemberDeclaration) bodyDeclaration;
		checkSourceRange(annotationTypeMemberDeclaration, "int age();", source);
		expression = annotationTypeMemberDeclaration.getDefault();
		assertNull("Got a default", expression);
	}
	
	public void test0006() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0006", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		final String expectedOutput = "Package annotations must be in file package-info.java";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		assertNotNull("No package declaration", packageDeclaration);
		checkSourceRange(packageDeclaration, "@Retention package test0006;", source);
		List annotations = packageDeclaration.annotations();
		assertEquals("Wrong size", 1, annotations.size());
		Annotation annotation = (Annotation) annotations.get(0);
		checkSourceRange(annotation, "@Retention", source);
		assertEquals("Not a marker annotation", annotation.getNodeType(), ASTNode.MARKER_ANNOTATION);
		MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
		checkSourceRange(markerAnnotation.getTypeName(), "Retention", source);
	}
	
	public void test0007() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0007", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List imports = compilationUnit.imports();
		assertEquals("Wrong size", 2, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		checkSourceRange(importDeclaration, "import java.util.*;", source);
		assertFalse("is static", importDeclaration.isStatic());
		importDeclaration = (ImportDeclaration) imports.get(1);
		checkSourceRange(importDeclaration, "import static java.io.File.*;", source);
		assertTrue("not static", importDeclaration.isStatic());
	}
	
	public void test0008() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0008", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(AST.JLS2, sourceUnit, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List imports = compilationUnit.imports();
		assertEquals("Wrong size", 2, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(1);
		assertTrue("Not malformed", isMalformed(importDeclaration));
	}
	
	public void test0009() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0009", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertTrue("Not a foreach statement", node.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT);
		EnhancedForStatement enhancedForStatement = (EnhancedForStatement) node;
		checkSourceRange(enhancedForStatement, "for (String s : args) {System.out.println(s);}", source);
		SingleVariableDeclaration singleVariableDeclaration = enhancedForStatement.getParameter();
		checkSourceRange(singleVariableDeclaration, "String s", source);
		Type type = singleVariableDeclaration.getType();
		checkSourceRange(type, "String", source);
		SimpleName simpleName = singleVariableDeclaration.getName();
		assertEquals("Wrong name", "s", simpleName.getIdentifier());
		checkSourceRange(simpleName, "s", source);
		Expression expression = enhancedForStatement.getExpression();
		checkSourceRange(expression, "args", source);
		Statement body = enhancedForStatement.getBody();
		checkSourceRange(body, "{System.out.println(s);}", source);
	}
	
	public void test0010() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0010", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertTrue("Not a foreach statement", node.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT);
		EnhancedForStatement enhancedForStatement = (EnhancedForStatement) node;
		checkSourceRange(enhancedForStatement, "for (@Foo final String s : args) {System.out.println(s);}", source);
		SingleVariableDeclaration singleVariableDeclaration = enhancedForStatement.getParameter();
		checkSourceRange(singleVariableDeclaration, "@Foo final String s", source);
		SimpleName simpleName = singleVariableDeclaration.getName();
		List modifiers = singleVariableDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		checkSourceRange((ASTNode) modifier, "@Foo", source);
		modifier = (IExtendedModifier) modifiers.get(1);
		checkSourceRange((ASTNode) modifier, "final", source);
		Type type = singleVariableDeclaration.getType();
		checkSourceRange(type, "String", source);
		assertEquals("Wrong name", "s", simpleName.getIdentifier());
		checkSourceRange(simpleName, "s", source);
		Expression expression = enhancedForStatement.getExpression();
		checkSourceRange(expression, "args", source);
		Statement body = enhancedForStatement.getBody();
		checkSourceRange(body, "{System.out.println(s);}", source);
	}
	
	public void test0011() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0011", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertTrue("Not a foreach statement", node.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT);
		EnhancedForStatement enhancedForStatement = (EnhancedForStatement) node;
		checkSourceRange(enhancedForStatement, "for (@Foo final String s[] : args) {System.out.println(s);}", source);
		SingleVariableDeclaration singleVariableDeclaration = enhancedForStatement.getParameter();
		checkSourceRange(singleVariableDeclaration, "@Foo final String s[]", source);
		SimpleName simpleName = singleVariableDeclaration.getName();
		List modifiers = singleVariableDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		checkSourceRange((ASTNode) modifier, "@Foo", source);
		modifier = (IExtendedModifier) modifiers.get(1);
		checkSourceRange((ASTNode) modifier, "final", source);
		assertEquals("Wrong dimension", 1, singleVariableDeclaration.getExtraDimensions());
		Type type = singleVariableDeclaration.getType();
		checkSourceRange(type, "String", source);
		assertEquals("Wrong name", "s", simpleName.getIdentifier());
		checkSourceRange(simpleName, "s", source);
		Expression expression = enhancedForStatement.getExpression();
		checkSourceRange(expression, "args", source);
		Statement body = enhancedForStatement.getBody();
		checkSourceRange(body, "{System.out.println(s);}", source);
	}
	
	public void test0012() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0012", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 1, parameters.size());
		SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
		checkSourceRange(parameter, "@Foo final String[][]... args", source);
		List modifiers = parameter.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		ASTNode modifier = (ASTNode) modifiers.get(0);
		checkSourceRange(modifier, "@Foo", source);
		modifier = (ASTNode) modifiers.get(1);
		checkSourceRange(modifier, "final", source);
		assertEquals("Wrong name", "args", parameter.getName().getIdentifier());
		assertTrue("Not a variable argument", parameter.isVarargs());
	}

	public void test0013() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0013", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		SimpleName name = typeDeclaration.getName();
		assertEquals("Wrong name", "Convertible", name.getIdentifier());
		checkSourceRange(name, "Convertible", source);
		List typeParameters = typeDeclaration.typeParameters();
		assertEquals("Wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		checkSourceRange(typeParameter, "T", source);
		checkSourceRange(typeParameter.getName(), "T", source);
		node = getASTNode(compilationUnit, 1);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
		typeDeclaration = (TypeDeclaration) node;
		name = typeDeclaration.getName();
		assertEquals("Wrong name", "X", name.getIdentifier());
		checkSourceRange(name, "X", source);
		typeParameters = typeDeclaration.typeParameters();
		assertEquals("Wrong size", 2, typeParameters.size());
		typeParameter = (TypeParameter) typeParameters.get(0);
		checkSourceRange(typeParameter.getName(), "A", source);
		checkSourceRange(typeParameter, "A extends Convertible<B>", source);
		typeParameter = (TypeParameter) typeParameters.get(1);
		checkSourceRange(typeParameter.getName(), "B", source);
		checkSourceRange(typeParameter, "B extends Convertible<A>", source);
		List typeBounds = typeParameter.typeBounds();
		assertEquals("Wrong size", 1, typeBounds.size());
		Type typeBound = (Type) typeBounds.get(0);
		checkSourceRange(typeBound, "Convertible<A>", source);
		assertEquals("wrong type", ASTNode.PARAMETERIZED_TYPE, typeBound.getNodeType());
		ParameterizedType parameterizedType = (ParameterizedType) typeBound;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "A", source);
	}
	
	public void test0014() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0014", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		SimpleName name = typeDeclaration.getName();
		assertEquals("Wrong name", "X", name.getIdentifier());
		checkSourceRange(name, "X", source);
		List typeParameters = typeDeclaration.typeParameters();
		assertEquals("Wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		checkSourceRange(typeParameter.getName(), "A", source);
		checkSourceRange(typeParameter, "A extends Convertible<Convertible<A>>", source);
		List typeBounds = typeParameter.typeBounds();
		assertEquals("Wrong size", 1, typeBounds.size());
		Type typeBound = (Type) typeBounds.get(0);
		checkSourceRange(typeBound, "Convertible<Convertible<A>>", source);
		assertEquals("wrong type", ASTNode.PARAMETERIZED_TYPE, typeBound.getNodeType());
		ParameterizedType parameterizedType = (ParameterizedType) typeBound;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "Convertible<A>", source);
		assertEquals("wrong type", ASTNode.PARAMETERIZED_TYPE, typeArgument.getNodeType());
		parameterizedType = (ParameterizedType) typeArgument;
		typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "A", source);
	}
	
	public void test0015() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0015", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION);
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		SimpleName name = typeDeclaration.getName();
		assertEquals("Wrong name", "X", name.getIdentifier());
		checkSourceRange(name, "X", source);
		List typeParameters = typeDeclaration.typeParameters();
		assertEquals("Wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		checkSourceRange(typeParameter.getName(), "A", source);
		checkSourceRange(typeParameter, "A extends Object & java.io.Serializable & Comparable", source);
		List typeBounds = typeParameter.typeBounds();
		assertEquals("Wrong size", 3, typeBounds.size());
		Type typeBound = (Type) typeBounds.get(0);
		checkSourceRange(typeBound, "Object", source);	
		typeBound = (Type) typeBounds.get(1);
		checkSourceRange(typeBound, "java.io.Serializable", source);
		typeBound = (Type) typeBounds.get(2);
		checkSourceRange(typeBound, "Comparable", source);		
	}

	public void test0016() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0016", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 5);
		assertEquals("Wrong first character", '<', source[node.getStartPosition()]);
	}
	
	public void test0017() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0017", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertTrue("Not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "Integer", source);
		Type innerType = parameterizedType.getType();
		assertTrue("Not a qualified type", innerType.getNodeType() == ASTNode.QUALIFIED_TYPE);
		QualifiedType qualifiedType = (QualifiedType) innerType;
		checkSourceRange(qualifiedType.getName(), "B", source);
		Type qualifier = qualifiedType.getQualifier();
		checkSourceRange(qualifier, "test0017.A<String>", source);
		assertTrue("Not a parameterized type", qualifier.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType2 = (ParameterizedType) qualifier;
		typeArguments = parameterizedType2.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "String", source);
		innerType = parameterizedType2.getType();
		assertTrue("Not a simple type", innerType.getNodeType() == ASTNode.SIMPLE_TYPE);
		SimpleType simpleType = (SimpleType) innerType;
		checkSourceRange(simpleType, "test0017.A", source);
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.getNodeType() == ASTNode.QUALIFIED_NAME);
		QualifiedName qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName.getQualifier(), "test0017", source);
		checkSourceRange(qualifiedName.getName(), "A", source);		
	}
	
	public void test0018() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0018", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertTrue("Not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "Float", source);
		Type innerType = parameterizedType.getType();
		assertTrue("Not a qualified type", innerType.getNodeType() == ASTNode.QUALIFIED_TYPE);
		QualifiedType qualifiedType = (QualifiedType) innerType;
		checkSourceRange(qualifiedType.getName(), "C", source);
		Type qualifier = qualifiedType.getQualifier();
		checkSourceRange(qualifier, "test0018.A<String>.B", source);
		assertTrue("Not a qualified type", qualifier.getNodeType() == ASTNode.QUALIFIED_TYPE);
		qualifiedType = (QualifiedType) qualifier;
		checkSourceRange(qualifiedType.getName(), "B", source);
		qualifier = qualifiedType.getQualifier();
		checkSourceRange(qualifier, "test0018.A<String>", source);
		assertTrue("Not a parameterized type", qualifier.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType2 = (ParameterizedType) qualifier;
		typeArguments = parameterizedType2.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "String", source);
		innerType = parameterizedType2.getType();
		assertTrue("Not a simple type", innerType.getNodeType() == ASTNode.SIMPLE_TYPE);
		SimpleType simpleType = (SimpleType) innerType;
		checkSourceRange(simpleType, "test0018.A", source);
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.getNodeType() == ASTNode.QUALIFIED_NAME);
		QualifiedName qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName.getQualifier(), "test0018", source);
		checkSourceRange(qualifiedName.getName(), "A", source);		
	}
	
	public void test0019() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0019", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertTrue("Not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a qualified type", type.getNodeType() == ASTNode.QUALIFIED_TYPE);
		QualifiedType qualifiedType = (QualifiedType) type;
		checkSourceRange(qualifiedType.getName(), "C", source);
		Type qualifier = qualifiedType.getQualifier();
		checkSourceRange(qualifier, "test0019.A<String>.B<Integer>", source);
		assertTrue("Not a parameterized type", qualifier.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType = (ParameterizedType) qualifier;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "Integer", source);
		Type innerType = parameterizedType.getType();
		assertTrue("Not a qualified type", innerType.getNodeType() == ASTNode.QUALIFIED_TYPE);
		qualifiedType = (QualifiedType) innerType;
		checkSourceRange(qualifiedType.getName(), "B", source);
		qualifier = qualifiedType.getQualifier();
		checkSourceRange(qualifier, "test0019.A<String>", source);
		assertTrue("Not a parameterized type", qualifier.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType2 = (ParameterizedType) qualifier;
		typeArguments = parameterizedType2.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "String", source);
		innerType = parameterizedType2.getType();
		assertTrue("Not a simple type", innerType.getNodeType() == ASTNode.SIMPLE_TYPE);
		SimpleType simpleType = (SimpleType) innerType;
		checkSourceRange(simpleType, "test0019.A", source);
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.getNodeType() == ASTNode.QUALIFIED_NAME);
		QualifiedName qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName.getQualifier(), "test0019", source);
		checkSourceRange(qualifiedName.getName(), "A", source);		
	}
	
	public void test0020() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0020", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration declaration = (MethodDeclaration) node;
		List parameters = declaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		assertTrue("Not a parameterized type", type.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "?", source);
	}
	
	public void test0021() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0021", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration declaration = (MethodDeclaration) node;
		List parameters = declaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		assertTrue("Not a parameterized type", type.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "? extends E", source);
		assertTrue("Not a wildcard type", typeArgument.getNodeType() == ASTNode.WILDCARD_TYPE);
		WildcardType wildcardType = (WildcardType) typeArgument;
		Type bound = wildcardType.getBound();
		checkSourceRange(bound, "E", source);
		assertTrue("Not an upper bound", wildcardType.isUpperBound());
	}
	
	public void test0022() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0022", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration declaration = (MethodDeclaration) node;
		List parameters = declaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		assertTrue("Not a parameterized type", type.getNodeType() == ASTNode.PARAMETERIZED_TYPE);
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "? super E", source);
		assertTrue("Not a wildcard type", typeArgument.getNodeType() == ASTNode.WILDCARD_TYPE);
		WildcardType wildcardType = (WildcardType) typeArgument;
		Type bound = wildcardType.getBound();
		checkSourceRange(bound, "E", source);
		assertFalse("Is an upper bound", wildcardType.isUpperBound());
	}

	public void test0023() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0023", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 5);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertEquals("Wrong name", "zip", methodDeclaration.getName().getIdentifier());
		List typeParameters = methodDeclaration.typeParameters();
		assertNotNull("No type parameters", typeParameters);
		assertEquals("Wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		checkSourceRange(typeParameter, "T", source);
	}
	
	public void test0024() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0024", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) node;
		List fragments = declarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		List typeArguments = classInstanceCreation.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		Type type = (Type) typeArguments.get(0);
		checkSourceRange(type, "String", source);
	}
	
	public void test0025() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0025", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not a constructor invocation", ASTNode.CONSTRUCTOR_INVOCATION, node.getNodeType());
		ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
		List typeArguments = constructorInvocation.typeArguments();
		assertEquals("wrong size", 1, typeArguments.size());
		Type type = (Type) typeArguments.get(0);
		checkSourceRange(type, "E", source);
	}
	
	public void test0026() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0026", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List modifiers = enumDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 2, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not a modifier", extendedModifier instanceof Modifier);
		Modifier modifier = (Modifier) extendedModifier;
		checkSourceRange(modifier, "public", source);
		extendedModifier = (IExtendedModifier) modifiers.get(1);
		assertTrue("Not a modifier", extendedModifier instanceof Modifier);
		modifier = (Modifier) extendedModifier;
		checkSourceRange(modifier, "abstract", source);
		assertEquals("wrong name", "X", enumDeclaration.getName().getIdentifier());
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("wrong size", 4, enumConstants.size());
		List bodyDeclarations = enumDeclaration.bodyDeclarations();
		assertEquals("wrong size", 2, bodyDeclarations.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		checkSourceRange(enumConstantDeclaration.getName(), "PLUS", source);
		checkSourceRange(enumConstantDeclaration, "PLUS {\n" + 
				"        double eval(double x, double y) { return x + y; }\n" + 
				"    }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());		
		bodyDeclarations = enumConstantDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "double eval(double x, double y) { return x + y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());		
		
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		checkSourceRange(enumConstantDeclaration.getName(), "MINUS", source);
		checkSourceRange(enumConstantDeclaration, "MINUS {\n" + 
				"        double eval(double x, double y) { return x - y; }\n" + 
				"    }", source);
		bodyDeclarations = enumConstantDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "double eval(double x, double y) { return x - y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());		

		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(2);
		checkSourceRange(enumConstantDeclaration.getName(), "TIMES", source);
		checkSourceRange(enumConstantDeclaration, "TIMES {\n" + 
				"        double eval(double x, double y) { return x * y; }\n" + 
				"    }", source);
		bodyDeclarations = enumConstantDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "double eval(double x, double y) { return x * y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());		

		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(3);
		checkSourceRange(enumConstantDeclaration.getName(), "DIVIDED_BY", source);
		checkSourceRange(enumConstantDeclaration, "DIVIDED_BY {\n" + 
				"        double eval(double x, double y) { return x / y; }\n" + 
				"    }", source);
		bodyDeclarations = enumConstantDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "double eval(double x, double y) { return x / y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());		
	}
	
	public void test0027() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0027", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		char[] source = sourceUnit.getSource().toCharArray();
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List modifiers = enumDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 1, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not a modifier", extendedModifier instanceof Modifier);
		Modifier modifier = (Modifier) extendedModifier;
		checkSourceRange(modifier, "public", source);
		assertEquals("wrong name", "X", enumDeclaration.getName().getIdentifier());
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("wrong size", 4, enumConstants.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		checkSourceRange(enumConstantDeclaration.getName(), "PENNY", source);
		checkSourceRange(enumConstantDeclaration, "PENNY(1)", source);
		List arguments = enumConstantDeclaration.arguments();
		assertEquals("wrong size", 1, arguments.size());		
		Expression argument = (Expression) arguments.get(0);
		checkSourceRange(argument, "1", source);
		assertEquals("not an number literal", ASTNode.NUMBER_LITERAL, argument.getNodeType());
		IVariableBinding binding = enumConstantDeclaration.resolveVariable();
		assertNotNull("No binding", binding);
		assertEquals("Wrong name", "PENNY", binding.getName());
		ASTNode node2 = compilationUnit.findDeclaringNode(binding);
		assertTrue("Different node", node2 == enumConstantDeclaration);
		
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		checkSourceRange(enumConstantDeclaration.getName(), "NICKEL", source);
		checkSourceRange(enumConstantDeclaration, "NICKEL(5)", source);
		arguments = enumConstantDeclaration.arguments();
		assertEquals("wrong size", 1, arguments.size());		
		argument = (Expression) arguments.get(0);
		checkSourceRange(argument, "5", source);
		assertEquals("not an number literal", ASTNode.NUMBER_LITERAL, argument.getNodeType());
		binding = enumConstantDeclaration.resolveVariable();
		assertNotNull("No binding", binding);
		assertEquals("Wrong name", "NICKEL", binding.getName());
		
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(2);
		checkSourceRange(enumConstantDeclaration.getName(), "DIME", source);
		checkSourceRange(enumConstantDeclaration, "DIME(10)", source);
		arguments = enumConstantDeclaration.arguments();
		assertEquals("wrong size", 1, arguments.size());		
		argument = (Expression) arguments.get(0);
		checkSourceRange(argument, "10", source);
		assertEquals("not an number literal", ASTNode.NUMBER_LITERAL, argument.getNodeType());
		binding = enumConstantDeclaration.resolveVariable();
		assertNotNull("No binding", binding);
		assertEquals("Wrong name", "DIME", binding.getName());

	
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(3);
		checkSourceRange(enumConstantDeclaration.getName(), "QUARTER", source);
		checkSourceRange(enumConstantDeclaration, "QUARTER(25)", source);
		arguments = enumConstantDeclaration.arguments();
		assertEquals("wrong size", 1, arguments.size());		
		argument = (Expression) arguments.get(0);
		checkSourceRange(argument, "25", source);
		assertEquals("not an number literal", ASTNode.NUMBER_LITERAL, argument.getNodeType());
		binding = enumConstantDeclaration.resolveVariable();
		assertNotNull("No binding", binding);
		assertEquals("Wrong name", "QUARTER", binding.getName());
	}
	
	public void test0028() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0028", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		checkSourceRange(methodDeclaration.getName(), "foo", source);
		checkSourceRange(methodDeclaration, "void foo(String[] args) {\n" + 
				"    	if (args.length < 2) {\n" + 
				"    		System.out.println(\"Usage: X <double> <double>\");\n" + 
				"    		return;\n" + 
				"    	}\n" + 
				"        double x = Double.parseDouble(args[0]);\n" + 
				"        double y = Double.parseDouble(args[1]);\n" + 
				"\n" + 
				"        for (X op : X.values())\n" + 
				"            System.out.println(x + \" \" + op + \" \" + y + \" = \" + op.eval(x, y));\n" + 
				"	}", source);
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		checkSourceRange(methodDeclaration.getName(), "bar", source);
		checkSourceRange(methodDeclaration, "abstract double bar(double x, double y);", source);		
	}
	
	public void test0029() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0029", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=67790
	 */
	public void test0030() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0030", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not a constructor invocation", ASTNode.CONSTRUCTOR_INVOCATION, node.getNodeType());
		checkSourceRange(node, "<T>this();", source);		
	}
	
	public void test0031() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0031", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	public void test0032() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0032", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List superInterfaces = typeDeclaration.superInterfaceTypes();
		assertEquals("wrong size", 1, superInterfaces.size());
		Type type = (Type) superInterfaces.get(0);
		assertEquals("wrong type", ASTNode.PARAMETERIZED_TYPE, type.getNodeType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		Type type2 = parameterizedType.getType();
		checkSourceRange(type2, "C", source);
	}
	
	public void test0033() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0033", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}

	public void test0034() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0034", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=70292
	 */
	public void test0035() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0035", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=67790
	 */
	public void test0036() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0036", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		checkSourceRange(expressionStatement, "this.<T>foo();", source);		
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		checkSourceRange(methodInvocation, "this.<T>foo()", source);
		List typeArguments = methodInvocation.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=68838
	 */
	public void test0037() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0037", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List typeParameters = typeDeclaration.typeParameters();
		assertEquals("Wrong size", 2, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		IBinding binding = typeParameter.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertEquals("Wrong name", "T", typeBinding.getName());
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertEquals("Wrong key", "Ltest0037/X<TT;TU;>;:TT;", typeBinding.getKey());
		SimpleName simpleName = typeParameter.getName();
		assertEquals("Wrong name", "T", simpleName.getIdentifier());
		IBinding binding2 = simpleName.resolveBinding();
		assertNotNull("No binding", binding2);
		assertEquals("Wrong type", IBinding.TYPE, binding2.getKind());
		ITypeBinding typeBinding2 = (ITypeBinding) binding2;
		assertEquals("Wrong name", "T", typeBinding2.getName());
		ITypeBinding typeBinding3 = simpleName.resolveTypeBinding();
		assertNotNull("No binding", typeBinding3);
		assertEquals("Wrong type", IBinding.TYPE, typeBinding3.getKind());
		assertEquals("Wrong name", "T", typeBinding3.getName());
		
		typeParameter = (TypeParameter) typeParameters.get(1);
		binding = typeParameter.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		typeBinding = (ITypeBinding) binding;
		assertEquals("Wrong name", "U", typeBinding.getName());
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertEquals("Wrong key", "Ltest0037/X<TT;TU;>;:TU;", typeBinding.getKey());
		simpleName = typeParameter.getName();
		assertEquals("Wrong name", "U", simpleName.getIdentifier());
		binding2 = simpleName.resolveBinding();
		assertNotNull("No binding", binding2);
		assertEquals("Wrong type", IBinding.TYPE, binding2.getKind());
		typeBinding2 = (ITypeBinding) binding2;
		assertEquals("Wrong name", "U", typeBinding2.getName());
		typeBinding3 = simpleName.resolveTypeBinding();
		assertNotNull("No binding", typeBinding3);
		assertEquals("Wrong type", IBinding.TYPE, typeBinding3.getKind());
		assertEquals("Wrong name", "U", typeBinding3.getName());
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=69066
	 */
	public void test0038() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0038", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type typeArgument = (Type) typeArguments.get(0);
		checkSourceRange(typeArgument, "T", source);
		ITypeBinding typeBinding = typeArgument.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong name", "T", typeBinding.getName());
		ITypeBinding[] typeParameters = typeBinding.getTypeParameters();
		assertEquals("Wrong size", 0, typeParameters.length);
		assertEquals("Wrong isArray", false, typeBinding.isArray());
		assertEquals("Wrong isAnnotation", false, typeBinding.isAnnotation());
		assertEquals("Wrong isAnonymous", false, typeBinding.isAnonymous());
		assertEquals("Wrong isClass", false, typeBinding.isClass());
		assertEquals("Wrong isEnum", false, typeBinding.isEnum());
		assertEquals("Wrong isInterface", false, typeBinding.isInterface());
		assertEquals("Wrong isGenericType", false, typeBinding.isGenericType());
		assertEquals("Wrong isLocal", false, typeBinding.isLocal());
		assertEquals("Wrong isMember", false, typeBinding.isMember());
		assertEquals("Wrong isNested", false, typeBinding.isNested());
		assertEquals("Wrong isNullType", false, typeBinding.isNullType());
		assertEquals("Wrong isParameterizedType", false, typeBinding.isParameterizedType());
		assertEquals("Wrong isPrimitive", false, typeBinding.isPrimitive());
		assertEquals("Wrong isRawType", false, typeBinding.isRawType());
		assertEquals("Wrong isTopLevel", false, typeBinding.isTopLevel());
		assertEquals("Wrong isUpperbound", false, typeBinding.isUpperbound());
		assertEquals("Wrong isTypeVariable", true, typeBinding.isTypeVariable());
		assertEquals("Wrong isWildcardType", false, typeBinding.isWildcardType());
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertEquals("Wrong name", "X", typeBinding2.getName());
		assertEquals("Wrong isArray", false, typeBinding2.isArray());
		assertEquals("Wrong isAnnotation", false, typeBinding2.isAnnotation());
		assertEquals("Wrong isAnonymous", false, typeBinding2.isAnonymous());
		assertEquals("Wrong isClass", true, typeBinding2.isClass());
		assertEquals("Wrong isEnum", false, typeBinding2.isEnum());
		assertEquals("Wrong isInterface", false, typeBinding2.isInterface());
		assertEquals("Wrong isGenericType", true, typeBinding2.isGenericType());
		assertEquals("Wrong isLocal", false, typeBinding2.isLocal());
		assertEquals("Wrong isMember", false, typeBinding2.isMember());
		assertEquals("Wrong isNested", false, typeBinding2.isNested());
		assertEquals("Wrong isNullType", false, typeBinding2.isNullType());
		assertEquals("Wrong isParameterizedType", false, typeBinding2.isParameterizedType());
		assertEquals("Wrong isPrimitive", false, typeBinding2.isPrimitive());
		assertEquals("Wrong isRawType", false, typeBinding2.isRawType());
		assertEquals("Wrong isTopLevel", true, typeBinding2.isTopLevel());
		assertEquals("Wrong isUpperbound", false, typeBinding2.isUpperbound());
		assertEquals("Wrong isTypeVariable", false, typeBinding2.isTypeVariable());
		assertEquals("Wrong isWildcardType", false, typeBinding2.isWildcardType());
		typeParameters = typeBinding2.getTypeParameters();
		assertEquals("Wrong size", 1, typeParameters.length);
		assertEquals("Wrong name", "T", typeParameters[0].getName());
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72233
	 */
	public void test0039() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0039", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72248
	 */
	public void test0040() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0040", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List typeParameters = methodDeclaration.typeParameters();
		assertEquals("wrong size", 1, typeParameters.size());
		TypeParameter parameter = (TypeParameter) typeParameters.get(0);
		IBinding binding = parameter.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("wrong key", "Ltest0040/X;.foo<T:Ljava/lang/Object;>()TT;:TT;", binding.getKey());
		Type returnType = methodDeclaration.getReturnType2();
		IBinding binding2 = returnType.resolveBinding();
		assertNotNull("No binding", binding2);
		assertEquals("wrong type", IBinding.TYPE, binding2.getKind());
		assertEquals("wrong key", "Ltest0040/X;.foo<T:Ljava/lang/Object;>()TT;:TT;", binding2.getKey());		
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72477
	 */
	public void test0041() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0041", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, false, true); // TODO (olivier) active binding when annotations are supported
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=73048
	 */
	public void test0042() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0042", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List typeParameters = methodDeclaration.typeParameters();
		assertEquals("wrong size", 1, typeParameters.size());
		TypeParameter parameter = (TypeParameter) typeParameters.get(0);
		IBinding binding = parameter.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("wrong key", "Ltest0042/X;.foo<T:Ljava/lang/Object;>()[TT;:TT;", binding.getKey());
		Type returnType = methodDeclaration.getReturnType2();
		IBinding binding2 = returnType.resolveBinding();
		assertNotNull("No binding", binding2);
		assertEquals("wrong type", IBinding.TYPE, binding2.getKind());
		assertEquals("wrong key", "[Ltest0042/X;.foo<T:Ljava/lang/Object;>()[TT;:TT;", binding2.getKey());		
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72882
	 */
	public void test0043() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0043", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List typeParameters = typeDeclaration.typeParameters();
		assertEquals("Wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		IBinding binding = typeParameter.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertEquals("Wrong qualified name", "T", typeBinding.getQualifiedName());
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72891
	 */
	public void test0044() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0044", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List typeParameters = methodDeclaration.typeParameters();
		assertEquals("wrong size", 1, typeParameters.size());
		TypeParameter parameter = (TypeParameter) typeParameters.get(0);
		IBinding binding = parameter.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("wrong key", "Ltest0044/X;.foo<Z:Ljava/lang/Object;>(TZ;)V:TZ;", binding.getKey());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("no binding", methodBinding);
		assertEquals("Wrong isConstructor", false, methodBinding.isConstructor());
		assertEquals("Wrong isDefaultConstructor", false, methodBinding.isDefaultConstructor());
		assertEquals("Wrong isDeprecated", false, methodBinding.isDeprecated());
		assertEquals("Wrong isGenericMethod", true, methodBinding.isGenericMethod());
		assertEquals("Wrong isParameterizedMethod", false, methodBinding.isParameterizedMethod());
		assertEquals("Wrong isRawMethod", false, methodBinding.isRawMethod());
		assertEquals("Wrong isSynthetic", false, methodBinding.isSynthetic());
		assertEquals("Wrong isVarargs", false, methodBinding.isVarargs());
		ITypeBinding[] typeParametersBindings = methodBinding.getTypeParameters();
		assertNotNull("No type parameters", typeParametersBindings);
		assertEquals("Wrong size", 1, typeParametersBindings.length);
		ITypeBinding typeBinding = typeParametersBindings[0];
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertEquals("Wrong fully qualified name", "Z", typeBinding.getQualifiedName());
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72891
	 */
	public void test0045() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0045", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 1);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
		assertEquals("Not a expression statement", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		assertTrue("Not parameterized", methodBinding.isParameterizedMethod());
		ITypeBinding[] typeArguments = methodBinding.getTypeArguments();
		assertNotNull("No type arguments", typeArguments);
		assertEquals("Wrong size", 1, typeArguments.length);
		assertEquals("Wrong qualified name", "java.lang.String", typeArguments[0].getQualifiedName());
		IMethodBinding erasure = methodBinding.getErasure();
		assertNotNull("No erasure", erasure);
		assertFalse("Not a parameterized method", erasure.isParameterizedMethod());
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72889
	 */
	public void test0046() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0046", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		Type superclassType = typeDeclaration.getSuperclassType();
		ITypeBinding typeBinding = superclassType.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		String key1 = typeBinding.getKey();
		node = getASTNode(compilationUnit, 1, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		typeBinding = type.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		String key2 = typeBinding.getKey();
		assertFalse("Same keys", key1.equals(key2));
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72859
	 */
	public void test0047() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0047", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=73561
	 */
	public void test0048() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0048", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("wrong size", 2, enumConstants.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		checkSourceRange(enumConstantDeclaration, "GREEN(0, 1)", source);
		checkSourceRange(enumConstantDeclaration.getName(), "GREEN", source);
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		checkSourceRange(enumConstantDeclaration.getName(), "RED", source);
		checkSourceRange(enumConstantDeclaration, "RED()", source);
	}
	
	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=73561
	 */
	public void test0049() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0049", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("wrong size", 2, enumConstants.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		checkSourceRange(enumConstantDeclaration, "GREEN(0, 1)", source);
		checkSourceRange(enumConstantDeclaration.getName(), "GREEN", source);
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		checkSourceRange(enumConstantDeclaration.getName(), "RED", source);
		checkSourceRange(enumConstantDeclaration, "RED", source);
	}
	
	/**
	 * Ellipsis
	 */
	public void test0050() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0050", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		final String expectedOutput = "Extended dimensions are illegal for a variable argument";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		assertTrue("Not a varargs", singleVariableDeclaration.isVarargs());
		final Type type = singleVariableDeclaration.getType();
		checkSourceRange(type, "String[]", source);
		assertTrue("not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		checkSourceRange(arrayType.getComponentType(), "String", source);
		assertEquals("Wrong extra dimensions", 1, singleVariableDeclaration.getExtraDimensions());
	}
	
	/**
	 * Ellipsis
	 */
	public void test0051() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0051", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertTrue("Not a varargs", methodBinding.isVarargs());
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		assertTrue("Not a varargs", singleVariableDeclaration.isVarargs());
		final Type type = singleVariableDeclaration.getType();
		checkSourceRange(type, "String[]", source);
		assertTrue("not an array type", type.isArrayType());
		ArrayType arrayType = (ArrayType) type;
		checkSourceRange(arrayType.getComponentType(), "String", source);
		assertEquals("Wrong extra dimensions", 0, singleVariableDeclaration.getExtraDimensions());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76103
	 */
	public void test0052() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0052", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76100
	 */
	public void test0053() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0053", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an annotation type declaration", ASTNode.ANNOTATION_TYPE_DECLARATION, node.getNodeType());
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) node;
		assertNotNull("No javadoc", annotationTypeDeclaration.getJavadoc());
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76100
	 */
	public void test0054() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0054", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an annotation type declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		assertNotNull("No javadoc", enumDeclaration.getJavadoc());
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76100
	 */
	public void test0055() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0055", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an annotation type declaration", ASTNode.ANNOTATION_TYPE_DECLARATION, node.getNodeType());
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) node;
		assertNotNull("No javadoc", annotationTypeDeclaration.getJavadoc());
	}
	
	/**
	 *
	 */
	public void test0056() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0056", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		final String expectedOutput = "Zork1 cannot be resolved to a type";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77175
	 */
	public void test0057() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0057", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		ITypeBinding typeBinding = enumDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not an enum type", typeBinding.isEnum());
		assertTrue("Not a top level type", typeBinding.isTopLevel());
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77249
	 */
	public void test0058() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0058", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, false, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertTrue("Not public type declaration", Modifier.isPublic(typeDeclaration.getModifiers()));
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77772
	 */
	public void test0059() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0059", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/*
	 * Ensures that the type parameters of a method are included in its binding key.
	 * (regression test for 73970 [1.5][dom] overloaded parameterized methods have same method binding key)
	 */
	public void test0060() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"/*start*/public class X {\n" +
				"  <T> void foo(T t) {\n" +
				"  }\n" +
				"  <T extends X> void foo(T t) {\n" +
				"  }\n" +
				"  <T extends Class> void foo(T t) {\n" +
				"  }\n" +
				"  <T extends Exception & Runnable> void foo(T t) {\n" +
				"  }\n" +
				"}/*end*/",
				workingCopy);
			MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
			int length = methods.length;
			String[] keys = new String[length];
			for (int i = 0; i < length; i++)
				keys[i] = methods[i].resolveBinding().getKey();
			assertBindingKeysEqual(
				"Lp/X;.foo<T:Ljava/lang/Object;>(TT;)V\n" + 
				"Lp/X;.foo<T:Lp/X;>(TT;)V\n" + 
				"Lp/X;.foo<T:Ljava/lang/Class;>(TT;)V\n" + 
				"Lp/X;.foo<T:Ljava/lang/Exception;:Ljava/lang/Runnable;>(TT;)V",
				keys);
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that the type parameters of a generic type are included in its binding key.
	 * (regression test for 77808 [1.5][dom] type bindings for raw List and List<E> have same key)
	 */
	public void test0061() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"/*start*/public class X<T> {\n" +
				"}/*end*/",
				workingCopy);
			IBinding binding = ((TypeDeclaration) node).resolveBinding();
			assertBindingKeyEquals(
				"Lp/X<TT;>;",
				binding.getKey());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that the type arguments of a parameterized type are included in its binding key.
	 */
	public void test0062() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X<T> {\n" +
				"  /*start*/X<Class>/*end*/ f;\n" +
				"}",
				workingCopy);
			IBinding binding = ((Type) node).resolveBinding();
			assertBindingKeyEquals(
				"Lp/X<Ljava/lang/Class;>;",
				binding.getKey());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0063() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0063", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Wrong node", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name", "test0063.X", typeBinding.getQualifiedName());
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong node", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		typeBinding = expression.resolveTypeBinding();
		assertTrue("Not parameterized", typeBinding.isParameterizedType());
		assertEquals("Wrong qualified name", "test0063.X<java.lang.String>", typeBinding.getQualifiedName());		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Wrong node", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration declaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = declaration.getType();
		typeBinding = type.resolveBinding();
		assertEquals("Wrong qualified name", "java.util.List<? extends test0063.X>", typeBinding.getQualifiedName());				
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0064() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0064", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Wrong node", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name", "test0064.X", typeBinding.getQualifiedName());
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong node", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		typeBinding = expression.resolveTypeBinding();
		assertTrue("Not parameterized", typeBinding.isParameterizedType());
		assertEquals("Wrong qualified name", "test0064.X<java.lang.String,java.lang.Integer>", typeBinding.getQualifiedName());		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Wrong node", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration declaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = declaration.getType();
		typeBinding = type.resolveBinding();
		assertEquals("Wrong qualified name", "java.util.List<? extends test0064.X>", typeBinding.getQualifiedName());				
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0065() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0065", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Wrong node", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name", "test0065.X", typeBinding.getQualifiedName());
		ITypeBinding erasure = typeBinding.getErasure();
		assertEquals("Wrong qualified name", "test0065.X", erasure.getQualifiedName());
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong node", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		typeBinding = expression.resolveTypeBinding();
		assertTrue("Not parameterized", typeBinding.isParameterizedType());
		assertEquals("Wrong qualified name", "test0065.X<java.lang.String,java.util.List>", typeBinding.getQualifiedName());		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Wrong node", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration declaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = declaration.getType();
		typeBinding = type.resolveBinding();
		assertEquals("Wrong qualified name", "java.util.List<? extends test0065.X>", typeBinding.getQualifiedName());				
	}
	
	/*
	 * Ensures that a raw type doesn't include the type parameters in its binding key.
	 * (regression test for 77808 [1.5][dom] type bindings for raw List and List<E> have same key)
	 */
	public void test0066() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X<T> {\n" +
				"  /*start*/X/*end*/ field;" +
				"}",
				workingCopy);
			IBinding binding = ((Type) node).resolveBinding();
			assertBindingKeyEquals(
				"Lp/X;",
				binding.getKey());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78649
	 */
	public void test0067() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0067", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong node", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type type2 = (Type) typeArguments.get(0);
		assertTrue("Not a wildcard type", type2.isWildcardType());
		WildcardType wildcardType = (WildcardType) type2;
		assertTrue("Not an upperbound type", wildcardType.isUpperBound());
		ITypeBinding typeBinding = wildcardType.resolveBinding();
		assertTrue("Not an upperbound type binding", typeBinding.isUpperbound());
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78649
	 */
	public void test0068() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0068", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong node", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		List typeArguments = parameterizedType.typeArguments();
		assertEquals("Wrong size", 1, typeArguments.size());
		Type type2 = (Type) typeArguments.get(0);
		assertTrue("Not a wildcard type", type2.isWildcardType());
		WildcardType wildcardType = (WildcardType) type2;
		assertFalse("An upperbound type", wildcardType.isUpperBound());
		ITypeBinding typeBinding = wildcardType.resolveBinding();
		assertFalse("An upperbound type binding", typeBinding.isUpperbound());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
	 */
	public void _test0069() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0069", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		assertNotNull("No binding", parameterizedType.resolveBinding());
		Type type2 = parameterizedType.getType();
		assertTrue("Not a qualified type", type2.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type2;
		assertNotNull("No binding", qualifiedType.resolveBinding());
		SimpleName simpleName = qualifiedType.getName();
		assertNotNull("No binding", simpleName.resolveBinding());
		Type type3 = qualifiedType.getQualifier();
		assertTrue("Not a parameterized type", type3.isParameterizedType());
		ParameterizedType parameterizedType2 = (ParameterizedType) type3;
		assertNotNull("No binding", parameterizedType2.resolveBinding());
		Type type4 = parameterizedType2.getType();
		assertTrue("Not a simple type", type4.isSimpleType());
		SimpleType simpleType = (SimpleType) type4;
		assertNotNull("No binding", simpleType.resolveBinding());
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		assertNotNull("No binding", qualifiedName.resolveBinding());
		Name name2 = qualifiedName.getQualifier();
		assertTrue("Not a simpleName", name2.isSimpleName());
		SimpleName simpleName2 = (SimpleName) name2;
		IBinding binding = simpleName2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.PACKAGE, binding.getKind());
		SimpleName simpleName3 = qualifiedName.getName();
		assertNotNull("No binding", simpleName3.resolveBinding());
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
	 */
	public void _test0070() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0070", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78930
	 */
	public void test0071() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0071", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List typeParameters = methodDeclaration.typeParameters();
		assertEquals("wrong size", 1, typeParameters.size());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] typeBindings = methodBinding.getTypeParameters();
		assertEquals("wrong size", 1, typeBindings.length);
		ITypeBinding typeBinding = typeBindings[0];
		IJavaElement javaElement = typeBinding.getJavaElement();
		assertNotNull("No java element", javaElement);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77645
	 */
	public void test0072() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter", "src", "test0072", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(AST.JLS3, sourceUnit, true);
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
									assertEquals("Wrong method", "bar3", methodName);
								} else if (variableBindingName.equals("var12")) {
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
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77806
	 */
	public void test0073() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0073", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType2();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		assertNotNull("No binding", type.resolveBinding());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		ITypeBinding binding = parameterizedType.resolveBinding();
		assertNotNull("No binding", binding);
		Type type2 = parameterizedType.getType();
		assertTrue("Not a simple type", type2.isSimpleType());
		ITypeBinding binding2 = type2.resolveBinding();
		assertNotNull("No binding", binding2);
		SimpleType simpleType = (SimpleType) type2;
		Name name = simpleType.getName();
		assertTrue("Not a simpleName", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		ITypeBinding binding3 = simpleName.resolveTypeBinding();
		assertNotNull("No binding", binding3);
		assertTrue("Different binding", binding3.isEqualTo(binding));
		assertTrue("Different binding", binding2.isEqualTo(binding));
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
	 */
	public void _test0074() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0074", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type type = methodDeclaration.getReturnType2();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		assertNotNull("No binding", type.resolveBinding());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		Type type2 = parameterizedType.getType();
		assertTrue("Not a simple type", type2.isSimpleType());
		final ITypeBinding binding = type2.resolveBinding();
		assertNotNull("No binding", binding);
		SimpleType simpleType = (SimpleType) type2;
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		SimpleName simpleName = qualifiedName.getName();
		ITypeBinding binding2 = simpleName.resolveTypeBinding();
		assertNotNull("No binding", binding2);
		assertTrue("Different binding", binding2.isEqualTo(binding));
		Name name2 = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", name2.isQualifiedName());
		QualifiedName qualifiedName2 = (QualifiedName) name2;
		ITypeBinding binding3 = qualifiedName2.resolveTypeBinding();
		assertNotNull("No binding", binding3);
		assertEquals("wrong kind", IBinding.PACKAGE, binding3.getKind());
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79271
	 */
	public void test0075() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"import java.util.ArrayList;\n" +
				"public class X {\n" +
				"  /*start*/ArrayList<Integer>/*end*/ field;" +
				"}",
				workingCopy);
			ITypeBinding binding = ((Type) node).resolveBinding();
			ITypeBinding erasure = binding.getErasure();
			assertFalse("Equals", binding.isEqualTo(erasure));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79362
	 */
	public void test0076() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0076", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput = "The type Map is not generic; it cannot be parameterized with arguments <String, Double>";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Map<String, Double>[][]", source);
		assertEquals("wrong type", ASTNode.ARRAY_TYPE, type.getNodeType());
		ArrayType arrayType = (ArrayType) type;
		type = arrayType.getComponentType();
		checkSourceRange(type, "Map<String, Double>[]", source);
		assertEquals("wrong type", ASTNode.ARRAY_TYPE, type.getNodeType());
		arrayType = (ArrayType) type;
		type = arrayType.getComponentType();
		checkSourceRange(type, "Map<String, Double>", source);
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79362
	 */
	public void test0077() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0077", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput = "The type Map is not generic; it cannot be parameterized with arguments <String, Double>";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "java.util.Map<String, Double>[][]", source);
		assertEquals("wrong type", ASTNode.ARRAY_TYPE, type.getNodeType());
		ArrayType arrayType = (ArrayType) type;
		type = arrayType.getComponentType();
		checkSourceRange(type, "java.util.Map<String, Double>[]", source);
		assertEquals("wrong type", ASTNode.ARRAY_TYPE, type.getNodeType());
		arrayType = (ArrayType) type;
		type = arrayType.getComponentType();
		checkSourceRange(type, "java.util.Map<String, Double>", source);
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0078() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X<T> {\n" +
				"  String foo(int i) { return /*start*/Integer.toString(i)/*end*/;}" +
				"}",
				workingCopy);
			IMethodBinding methodBinding = ((MethodInvocation) node).resolveMethodBinding();
			assertFalse("Is a raw method", methodBinding.isRawMethod());
			assertFalse("Is a parameterized method", methodBinding.isParameterizedMethod());
			assertFalse("Is a generic method", methodBinding.isGenericMethod());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0079() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X {\n" + 
				"	\n" + 
				"	/*start*/<T extends A> T foo(T t) {\n" + 
				"		return t;\n" + 
				"	}/*end*/\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().bar();\n" + 
				"	}\n" + 
				"	void bar() {\n" + 
				"		B b = foo(new B());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class A {}\n" + 
				"class B extends A {}\n",
				workingCopy);
			IMethodBinding methodBinding = ((MethodDeclaration) node).resolveBinding();
			assertFalse("Is a raw method", methodBinding.isRawMethod());
			assertFalse("Is a parameterized method", methodBinding.isParameterizedMethod());
			assertTrue("Not a generic method", methodBinding.isGenericMethod());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}	
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0080() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X {\n" + 
				"	\n" + 
				"	<T extends A> T foo(T t) {\n" + 
				"		return t;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new X().bar();\n" + 
				"	}\n" + 
				"	void bar() {\n" + 
				"		B b = /*start*/foo(new B())/*end*/;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class A {}\n" + 
				"class B extends A {}\n",
				workingCopy);
			IMethodBinding methodBinding = ((MethodInvocation) node).resolveMethodBinding();
			assertFalse("Is a raw method", methodBinding.isRawMethod());
			assertTrue("Not a parameterized method", methodBinding.isParameterizedMethod());
			assertFalse("Is a generic method", methodBinding.isGenericMethod());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}	
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0081() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0081", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput = "Type safety: The method foo(Object) belongs to the raw type Y. References to generic type Y<T> should be parameterized";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertEquals("Not a method declaration", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not an method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		assertEquals("Wrong name", "foo", methodBinding.getName());
		assertTrue("Not a raw method", methodBinding.isRawMethod());
		assertFalse("Is a parameterized method", methodBinding.isParameterizedMethod());
		assertFalse("Is a generic method", methodBinding.isGenericMethod());
	}
	
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0082() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0082", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong name", "Gen", typeBinding.getName());
		assertEquals("Wrong name", "test0082.Gen", typeBinding.getQualifiedName());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("Not a generic type", typeBinding.isGenericType());
		assertTrue("Not a top level", typeBinding.isTopLevel());
		
		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a member type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		typeDeclaration = (TypeDeclaration) node;
		typeBinding = typeDeclaration.resolveBinding();
		assertEquals("Wrong name", "Inn", typeBinding.getName());
		assertEquals("Wrong name", "test0082.Gen.Inn", typeBinding.getQualifiedName());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("Not a member", typeBinding.isMember());
		assertTrue("Not a nested class", typeBinding.isNested());
		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		typeBinding = fieldDeclaration.getType().resolveBinding();
		assertEquals("Wrong name", "Gen<String>", typeBinding.getName());
		assertEquals("Wrong name", "test0082.Gen<java.lang.String>", typeBinding.getQualifiedName());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("Not a parameterized type", typeBinding.isParameterizedType());
		assertTrue("Not a toplevel", typeBinding.isTopLevel());
		
		node = getASTNode(compilationUnit, 0, 2);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		typeBinding = fieldDeclaration.getType().resolveBinding();
		assertEquals("Wrong name", "Inn", typeBinding.getName());
		assertEquals("Wrong name", "test0082.Gen<java.lang.String>.Inn", typeBinding.getQualifiedName());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("Not a member", typeBinding.isMember());
		assertTrue("Not a nested class", typeBinding.isNested());
		assertFalse("Is parameterized", typeBinding.isParameterizedType());

		node = getASTNode(compilationUnit, 0, 3);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		typeBinding = fieldDeclaration.getType().resolveBinding();
		assertEquals("Wrong name", "Gen", typeBinding.getName());
		assertEquals("Wrong name", "test0082.Gen", typeBinding.getQualifiedName());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("Not a raw type", typeBinding.isRawType());
		assertTrue("Not a toplevel", typeBinding.isTopLevel());
		
		node = getASTNode(compilationUnit, 0, 4);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		typeBinding = fieldDeclaration.getType().resolveBinding();
		assertEquals("Wrong name", "Inn", typeBinding.getName());
		assertEquals("Wrong name", "test0082.Gen.Inn", typeBinding.getQualifiedName());
		assertTrue("Not a class", typeBinding.isClass());
		assertTrue("Not a member", typeBinding.isMember());
		assertTrue("Not a nested type", typeBinding.isNested());
		assertFalse("Is parameterized", typeBinding.isParameterizedType());
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79544
	 */
	public void test0083() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0083", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		ITypeBinding typeBinding = fieldDeclaration.getType().resolveBinding();
		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		ITypeBinding typeBinding2 = fieldDeclaration.getType().resolveBinding();

		node = getASTNode(compilationUnit, 0, 2);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		ITypeBinding typeBinding3 = fieldDeclaration.getType().resolveBinding();

		node = getASTNode(compilationUnit, 0, 3);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		ITypeBinding typeBinding4 = fieldDeclaration.getType().resolveBinding();
		
		assertFalse("Binding are equals", typeBinding.isEqualTo(typeBinding2));
		assertFalse("Binding are equals", typeBinding.isEqualTo(typeBinding3));
		assertFalse("Binding are equals", typeBinding.isEqualTo(typeBinding4));
		assertFalse("Binding are equals", typeBinding2.isEqualTo(typeBinding3));
		assertFalse("Binding are equals", typeBinding2.isEqualTo(typeBinding4));
		assertFalse("Binding are equals", typeBinding3.isEqualTo(typeBinding4));
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79612
	 */
	public void test0084() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0084", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		ITypeBinding typeBinding = fieldDeclaration.getType().resolveBinding();
		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		ITypeBinding typeBinding2 = fieldDeclaration.getType().resolveBinding();

		assertFalse("Binding are equals", typeBinding.isEqualTo(typeBinding2));
	}
	
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79609
	 */
	public void test0085() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0085", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS3Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List typeParameters = methodDeclaration.typeParameters();
		assertEquals("wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		IBinding binding = typeParameter.resolveBinding();
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		typeParameters = methodDeclaration.typeParameters();
		assertEquals("wrong size", 1, typeParameters.size());
		typeParameter = (TypeParameter) typeParameters.get(0);
		binding = typeParameter.resolveBinding();
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding2 = (ITypeBinding) binding;

		assertFalse("Binding are equals", typeBinding.isEqualTo(typeBinding2));
	}
	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79512
	 */
	public void test0086() throws JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p;\n" +
				"public class X {\n" + 
				"	\n" + 
				"public Object foo() {\n" +
				"		return /*start*/X.class/*end*/;\n" +
				"	}" + 
				"}\n" + 
				"\n" + 
				"class A {}\n" + 
				"class B extends A {}\n",
				workingCopy);
			TypeLiteral typeLiteral = (TypeLiteral) node;
			ITypeBinding typeBinding = typeLiteral.resolveTypeBinding();
			assertEquals("Wrong name", "java.lang.Class<p.X>", typeBinding.getQualifiedName());
			assertEquals("Wrong name", "Class<X>", typeBinding.getName());
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}	
}