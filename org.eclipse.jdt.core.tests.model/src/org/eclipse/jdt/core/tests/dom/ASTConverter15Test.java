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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public class ASTConverter15Test extends ConverterTestSetup {
	
	public ASTConverter15Test(String name) {
		super(name);
	}

	public static Test suite() {
		if (true) {
			return new Suite(ASTConverter15Test.class);
		}
		TestSuite suite = new Suite(ASTConverter15Test.class.getName());		
		suite.addTest(new ASTConverter15Test("test0004"));
		return suite;
	}
		
	public void test0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0001", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(AST.LEVEL_3_0, sourceUnit, false);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
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
//		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(AST.LEVEL_3_0, sourceUnit, false);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
	}
	
	public void test0003() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0003", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runConversion(AST.LEVEL_3_0, sourceUnit, false);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
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
		ASTNode result = runConversion(AST.LEVEL_3_0, sourceUnit, false);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
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
	
}

