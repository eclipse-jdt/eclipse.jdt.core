/*******************************************************************************
 * Copyright (c) 2011, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for bug 186342 - [compiler][null] Using annotations for null checking
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTConverter15JLS4Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getJLS4(), false);
	}

	public ASTConverter15JLS4Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 350 };
//		TESTS_RANGE = new int[] { 325, -1 };
//		TESTS_NAMES = new String[] {"testBug348024"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter15JLS4Test.class);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	private Type componentType(ArrayType array) {
		return array.getElementType();
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=234609 BindingKey#toSignature() fails with key from createWilcardTypeBindingKey(..)
	public void test234609() throws JavaModelException {

		String newContents = 	"package p;\n" +
		"import java.util.HashMap;\n" +
		"public class X {\n" +
		"  /*start*/HashMap<? extends Integer,? super String>/*end*/ s;" +
		"}";

		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);

		Type node = (Type) buildAST(
				newContents,
				this.workingCopy);

		ITypeBinding bindingFromAST = node.resolveBinding();
		String recoveredBindingKey = bindingFromAST.getKey();

		String genericTypeKey = BindingKey.createTypeBindingKey("java.util.HashMap");
		String [] wildcardKeys = new String [] { BindingKey.createWildcardTypeBindingKey(genericTypeKey, Signature.C_EXTENDS, BindingKey.createTypeBindingKey("java.lang.Integer"), 0),
				BindingKey.createWildcardTypeBindingKey(genericTypeKey, Signature.C_SUPER, BindingKey.createTypeBindingKey("java.lang.String"), 1)
		};

		String composedBindingKey = BindingKey.createParameterizedTypeBindingKey(genericTypeKey, wildcardKeys);

		if (!composedBindingKey.equals(recoveredBindingKey))
			fail("Composed binding key differs from Recovered binding key");


		this.workingCopy.discardWorkingCopy();
		this.workingCopy = null;

		ITypeBinding [] bindingFromKey = createTypeBindings(
						new String[] {
							"/Converter15/src/p/X.java",
							newContents
						},
						new String[] {
							composedBindingKey
						},
						getJavaProject("Converter15")
					);

		if (bindingFromKey.length != 1)
			fail("Problem in going from key to binding\n");
		if (!composedBindingKey.equals(bindingFromKey[0].getKey()))
			fail ("Binding key mismatch");
		String signature = new BindingKey(composedBindingKey).toSignature();
		if (!signature.equals("Ljava.util.HashMap<+Ljava.lang.Integer;-Ljava.lang.String;>;"))
			fail("Bad signature");

		assertTrue("Equals", bindingFromKey[0].isEqualTo(bindingFromAST));

		// check existence of getGenericType() API.
		ITypeBinding gType = bindingFromAST.getTypeArguments()[0].getGenericTypeOfWildcardType();
		if (gType == null)
			fail("Missing generic type");
		if (!gType.getKey().equals("Ljava/util/HashMap<TK;TV;>;"))
			fail("getKey() API is broken");

		// test for getRank API.
		if (bindingFromAST.getTypeArguments()[0].getRank() != 0)
			fail ("Wrong rank");

		if (bindingFromAST.getTypeArguments()[1].getRank() != 1)
			fail ("Wrong rank");
	}

	// Similar test as above - variation in wildcard type being unbounded.
	public void test234609b() throws JavaModelException {

		String newContents = 	"package p;\n" +
		"import java.util.ArrayList;\n" +
		"public class X {\n" +
		"  /*start*/ArrayList<?>/*end*/ s;" +
		"}";

		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);

		Type node = (Type) buildAST(
				newContents,
				this.workingCopy);

		ITypeBinding bindingFromAST = node.resolveBinding();
		String recoveredBindingKey = bindingFromAST.getKey();

		String genericTypeKey = BindingKey.createTypeBindingKey("java.util.ArrayList");
		String [] wildcardKeys = new String [] { BindingKey.createWildcardTypeBindingKey(genericTypeKey, Signature.C_STAR, null, 0) };

		String composedBindingKey = BindingKey.createParameterizedTypeBindingKey(genericTypeKey, wildcardKeys);

		if (!composedBindingKey.equals(recoveredBindingKey))
			fail("Composed binding key differs from Recovered binding key");


		this.workingCopy.discardWorkingCopy();
		this.workingCopy = null;

		ITypeBinding [] bindingFromKey = createTypeBindings(
						new String[] {
							"/Converter15/src/p/X.java",
							newContents
						},
						new String[] {
							composedBindingKey
						},
						getJavaProject("Converter15")
					);

		if (bindingFromKey.length != 1)
			fail("Problem in going from key to binding\n");
		if (!composedBindingKey.equals(bindingFromKey[0].getKey()))
			fail ("Binding key mismatch");
		String signature = new BindingKey(composedBindingKey).toSignature();
		if (!signature.equals("Ljava.util.ArrayList<*>;"))
			fail("Bad signature");
		assertTrue("Equals", bindingFromKey[0].isEqualTo(bindingFromAST));
	}

	public void test0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}

	public void test0003() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0003", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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

	/** @deprecated using deprecated code */
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
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		checkSourceRange(typeParameter, "A extends Object & java.io.Serializable & Comparable<?>", source);
		List typeBounds = typeParameter.typeBounds();
		assertEquals("Wrong size", 3, typeBounds.size());
		Type typeBound = (Type) typeBounds.get(0);
		checkSourceRange(typeBound, "Object", source);
		typeBound = (Type) typeBounds.get(1);
		checkSourceRange(typeBound, "java.io.Serializable", source);
		typeBound = (Type) typeBounds.get(2);
		checkSourceRange(typeBound, "Comparable<?>", source);
	}

	public void test0016() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0016", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedProblems = "";
		assertProblemsSize(compilationUnit, 0, expectedProblems);
		ASTNode node = getASTNode(compilationUnit, 0, 5);
		assertEquals("Wrong first character", '<', source[node.getStartPosition()]);
	}

	public void test0017() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0017", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0, 0);
		assertTrue("Not a variable declaration statement", node.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1,  fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initializer  = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		ITypeBinding binding = initializer.resolveTypeBinding();
		assertNotNull("No binding", binding);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedProblems ="";
		assertProblemsSize(compilationUnit, 0, expectedProblems);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		ITypeBinding typeBinding2 = enumDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding2);
		List modifiers = enumDeclaration.modifiers();
		assertEquals("Wrong number of modifiers", 1, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not a modifier", extendedModifier instanceof Modifier);
		Modifier modifier = (Modifier) extendedModifier;
		checkSourceRange(modifier, "public", source);
		assertEquals("wrong name", "X", enumDeclaration.getName().getIdentifier());
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("wrong size", 4, enumConstants.size());
		List bodyDeclarations = enumDeclaration.bodyDeclarations();
		assertEquals("wrong size", 2, bodyDeclarations.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		IMethodBinding methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration.getName(), "PLUS", source);
		checkSourceRange(enumConstantDeclaration, "PLUS {\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x + y; }\n" +
				"    }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());
		AnonymousClassDeclaration anonymousClassDeclaration = enumConstantDeclaration.getAnonymousClassDeclaration();
		assertNotNull("No anonymous class", anonymousClassDeclaration);
		checkSourceRange(anonymousClassDeclaration, "{\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x + y; }\n" +
				"    }", source);
		ITypeBinding typeBinding = anonymousClassDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a enum type", typeBinding.isEnum());
		bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "@Override\n        double eval(double x, double y) { return x + y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());

		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration.getName(), "MINUS", source);
		checkSourceRange(enumConstantDeclaration, "MINUS {\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x - y; }\n" +
				"    }", source);
		anonymousClassDeclaration = enumConstantDeclaration.getAnonymousClassDeclaration();
		typeBinding = anonymousClassDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a enum type", typeBinding.isEnum());
		assertNotNull("No anonymous class", anonymousClassDeclaration);
		checkSourceRange(anonymousClassDeclaration, "{\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x - y; }\n" +
				"    }", source);
		bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "@Override\n        double eval(double x, double y) { return x - y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());

		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(2);
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration.getName(), "TIMES", source);
		checkSourceRange(enumConstantDeclaration, "TIMES {\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x * y; }\n" +
				"    }", source);
		anonymousClassDeclaration = enumConstantDeclaration.getAnonymousClassDeclaration();
		assertNotNull("No anonymous class", anonymousClassDeclaration);
		checkSourceRange(anonymousClassDeclaration, "{\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x * y; }\n" +
				"    }", source);
		typeBinding = anonymousClassDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a enum type", typeBinding.isEnum());
		bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "@Override\n        double eval(double x, double y) { return x * y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());

		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(3);
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration.getName(), "DIVIDED_BY", source);
		checkSourceRange(enumConstantDeclaration, "DIVIDED_BY {\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x / y; }\n" +
				"    }", source);
		anonymousClassDeclaration = enumConstantDeclaration.getAnonymousClassDeclaration();
		assertNotNull("No anonymous class", anonymousClassDeclaration);
		checkSourceRange(anonymousClassDeclaration, "{\n" +
				"        @Override\n" +
				"        double eval(double x, double y) { return x / y; }\n" +
				"    }", source);
		typeBinding = anonymousClassDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a enum type", typeBinding.isEnum());
		bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("wrong size", 1, bodyDeclarations.size());
		bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		checkSourceRange(methodDeclaration.getName(), "eval", source);
		checkSourceRange(methodDeclaration, "@Override\n        double eval(double x, double y) { return x / y; }", source);
		assertEquals("wrong size", 0, enumConstantDeclaration.arguments().size());
	}

	public void test0027() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0027", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		IMethodBinding methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
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
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
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
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
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
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
				"            System.out.println(op.eval(x, y));\n" +
				"	}", source);
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		checkSourceRange(methodDeclaration.getName(), "bar", source);
		checkSourceRange(methodDeclaration, "abstract double bar(double x, double y);", source);
	}

	public void test0029() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0029", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not a constructor invocation", ASTNode.CONSTRUCTOR_INVOCATION, node.getNodeType());
		checkSourceRange(node, "<T>this();", source);
	}

	public void test0031() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0031", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}

	public void test0032() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0032", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
	}

	public void test0034() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0034", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		assertTrue("Not from source", typeBinding.isFromSource());
		assertEquals("Wrong key", "Ltest0037/X;:TT;", typeBinding.getKey());
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
		assertTrue("Not from source", typeBinding.isFromSource());
		assertEquals("Wrong key", "Ltest0037/X;:TU;", typeBinding.getKey());
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		assertTrue("Not from source", typeBinding.isFromSource());
		assertEquals("Wrong isWildcardType", false, typeBinding.isWildcardType());
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertEquals("Wrong name", "X<T>", typeBinding2.getName());
		assertEquals("Wrong isArray", false, typeBinding2.isArray());
		assertEquals("Wrong isAnnotation", false, typeBinding2.isAnnotation());
		assertEquals("Wrong isAnonymous", false, typeBinding2.isAnonymous());
		assertEquals("Wrong isClass", true, typeBinding2.isClass());
		assertEquals("Wrong isEnum", false, typeBinding2.isEnum());
		assertEquals("Wrong isInterface", false, typeBinding2.isInterface());
		assertEquals("Wrong isGenericType", false, typeBinding2.isGenericType());
		assertEquals("Wrong isLocal", false, typeBinding2.isLocal());
		assertEquals("Wrong isMember", false, typeBinding2.isMember());
		assertEquals("Wrong isNested", false, typeBinding2.isNested());
		assertEquals("Wrong isNullType", false, typeBinding2.isNullType());
		assertEquals("Wrong isParameterizedType", true, typeBinding2.isParameterizedType());
		assertEquals("Wrong isPrimitive", false, typeBinding2.isPrimitive());
		assertEquals("Wrong isRawType", false, typeBinding2.isRawType());
		assertEquals("Wrong isTopLevel", true, typeBinding2.isTopLevel());
		assertEquals("Wrong isUpperbound", false, typeBinding2.isUpperbound());
		assertEquals("Wrong isTypeVariable", false, typeBinding2.isTypeVariable());
		assertEquals("Wrong isWildcardType", false, typeBinding2.isWildcardType());
		typeParameters = typeBinding2.getTypeParameters();
		assertEquals("Wrong size", 0, typeParameters.length);
	}

	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72233
	 */
	public void test0039() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0039", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		assertFalse("Wrong isConstructor", methodBinding.isConstructor());
		assertFalse("Wrong isDefaultConstructor", methodBinding.isDefaultConstructor());
		assertFalse("Wrong isDeprecated", methodBinding.isDeprecated());
		assertTrue("Wrong isGenericMethod", methodBinding.isGenericMethod());
		assertFalse("Wrong isParameterizedMethod", methodBinding.isParameterizedMethod());
		assertFalse("Wrong isRawMethod", methodBinding.isRawMethod());
		assertFalse("Wrong isSynthetic", methodBinding.isSynthetic());
		assertFalse("Wrong isVarargs", methodBinding.isVarargs());
		ITypeBinding[] typeParametersBindings = methodBinding.getTypeParameters();
		assertNotNull("No type parameters", typeParametersBindings);
		assertEquals("Wrong size", 1, typeParametersBindings.length);
		ITypeBinding typeBinding = typeParametersBindings[0];
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertTrue("Not from source", typeBinding.isFromSource());
		assertEquals("Wrong fully qualified name", "Z", typeBinding.getQualifiedName());
	}

	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72891
	 */
	public void test0045() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0045", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		IMethodBinding genericMethod = methodBinding.getMethodDeclaration();
		assertNotNull("No generic method", genericMethod);
		assertFalse("Not a parameterized method", genericMethod.isParameterizedMethod());
	}

	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=72889
	 */
	public void test0046() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0046", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		IMethodBinding methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration, "GREEN(0, 1)", source);
		checkSourceRange(enumConstantDeclaration.getName(), "GREEN", source);
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration.getName(), "RED", source);
		checkSourceRange(enumConstantDeclaration, "RED()", source);
	}

	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=73561
	 */
	public void test0049() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0049", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		IMethodBinding methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration, "GREEN(0, 1)", source);
		checkSourceRange(enumConstantDeclaration.getName(), "GREEN", source);
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		checkSourceRange(enumConstantDeclaration.getName(), "RED", source);
		checkSourceRange(enumConstantDeclaration, "RED", source);
	}

	/**
	 * Ellipsis
	 */
	public void test0050() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0050", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		checkSourceRange(componentType(arrayType), "String", source);
		assertEquals("Wrong extra dimensions", 1, singleVariableDeclaration.getExtraDimensions());
	}

	/**
	 * Ellipsis
	 */
	public void test0051() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0051", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		checkSourceRange(componentType(arrayType), "String", source);
		assertEquals("Wrong extra dimensions", 0, singleVariableDeclaration.getExtraDimensions());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76103
	 */
	public void test0052() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0052", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not an annotation type declaration", ASTNode.ANNOTATION_TYPE_DECLARATION, node.getNodeType());
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) node;
		assertNotNull("No javadoc", annotationTypeDeclaration.getJavadoc());
	}

	public void test0056() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0056", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, false, false);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
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
			this.workingCopy,
			false);
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
	}

	/*
	 * Ensures that the type parameters of a generic type are included in its binding key.
	 * (regression test for 77808 [1.5][dom] type bindings for raw List and List<E> have same key)
	 */
	public void test0061() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"/*start*/public class X<T> {\n" +
			"}/*end*/",
			this.workingCopy);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertBindingKeyEquals(
			"Lp/X<TT;>;",
			binding.getKey());
	}

	/*
	 * Ensures that the type arguments of a parameterized type are included in its binding key.
	 */
	public void test0062() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"public class X<T> {\n" +
			"  /*start*/X<Class>/*end*/ f;\n" +
			"}",
			this.workingCopy,
			false);
		IBinding binding = ((Type) node).resolveBinding();
		assertBindingKeyEquals(
			"Lp/X<Ljava/lang/Class<>;>;",
			binding.getKey());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0063() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0063", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		assertEquals("Wrong qualified name", "java.util.List<? extends test0063.X<?>>", typeBinding.getQualifiedName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0064() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0064", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		assertEquals("Wrong qualified name", "java.util.List<? extends test0064.X<?,?>>", typeBinding.getQualifiedName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0065() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0065", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ITypeBinding genericType = typeBinding.getTypeDeclaration();
		assertEquals("Wrong qualified name", "test0065.X", genericType.getQualifiedName());
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong node", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		typeBinding = expression.resolveTypeBinding();
		assertTrue("Not parameterized", typeBinding.isParameterizedType());
		assertEquals("Wrong qualified name", "test0065.X<java.lang.String,java.util.List<?>>", typeBinding.getQualifiedName());
		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Wrong node", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration declaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = declaration.getType();
		typeBinding = type.resolveBinding();
		assertEquals("Wrong qualified name", "java.util.List<? extends test0065.X<?,?>>", typeBinding.getQualifiedName());
	}

	/*
	 * Ensures that a raw type doesn't include the type parameters in its binding key.
	 * (regression test for 77808 [1.5][dom] type bindings for raw List and List<E> have same key)
	 */
	public void test0066() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"public class X<T> {\n" +
			"  /*start*/X/*end*/ field;" +
			"}",
			this.workingCopy,
			false);
		IBinding binding = ((Type) node).resolveBinding();
		assertBindingKeyEquals(
			"Lp/X<>;",
			binding.getKey());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78649
	 */
	public void test0067() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0067", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
	public void test0069() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0069", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ITypeBinding typeBinding = qualifiedType.resolveBinding();
        assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 1", "test0069.Outer<java.lang.String>.Inner<java.lang.Integer>", typeBinding.getQualifiedName());
		SimpleName simpleName = qualifiedType.getName();
        IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", simpleName.resolveBinding());
        assertEquals("Wrong name 2", "test0069.Outer<java.lang.String>.Inner<java.lang.Integer>", typeBinding.getQualifiedName());
		Type type3 = qualifiedType.getQualifier();
		assertTrue("Not a parameterized type", type3.isParameterizedType());
		ParameterizedType parameterizedType2 = (ParameterizedType) type3;
        typeBinding = parameterizedType2.resolveBinding();
		assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 3", "test0069.Outer<java.lang.String>", typeBinding.getQualifiedName());
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
		binding = simpleName2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.PACKAGE, binding.getKind());
		SimpleName simpleName3 = qualifiedName.getName();
		assertNotNull("No binding", simpleName3.resolveBinding());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
	 */
	public void test0070() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0070", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ITypeBinding typeBinding = qualifiedType.resolveBinding();
        assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 1", "test0070.Outer<java.lang.String>.Inner<java.lang.Number>", typeBinding.getQualifiedName());
		SimpleName simpleName = qualifiedType.getName();
        typeBinding = simpleName.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 2", "test0070.Outer<java.lang.String>.Inner<java.lang.Number>", typeBinding.getQualifiedName());
		Type type3 = qualifiedType.getQualifier();
		assertTrue("Not a parameterized type", type3.isParameterizedType());
		ParameterizedType parameterizedType2 = (ParameterizedType) type3;
        typeBinding = parameterizedType2.resolveBinding();
		assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 3", "test0070.Outer<java.lang.String>", typeBinding.getQualifiedName());
		Type type4 = parameterizedType2.getType();
		assertTrue("Not a simple type", type4.isSimpleType());
		SimpleType simpleType = (SimpleType) type4;
		typeBinding = simpleType.resolveBinding();
		assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 3", "test0070.Outer<java.lang.String>", typeBinding.getQualifiedName());
		Name name = simpleType.getName();
		assertTrue("Not a simpleName", name.isSimpleName());
		SimpleName simpleName2 = (SimpleName) name;
		typeBinding = simpleName2.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
        assertEquals("Wrong name 3", "test0070.Outer", typeBinding.getQualifiedName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78930
	 */
	public void test0071() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0071", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15", "src", "test0072", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS4(), sourceUnit, true);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
	public void test0074() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0074", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
        assertEquals("wrong name", "java.util.List<java.lang.String>", binding2.getQualifiedName());
		Name name2 = qualifiedName.getQualifier();
		assertTrue("Not a qualified name", name2.isQualifiedName());
		QualifiedName qualifiedName2 = (QualifiedName) name2;
		IBinding binding3 = qualifiedName2.resolveBinding();
		assertNotNull("No binding", binding3);
		assertEquals("wrong kind", IBinding.PACKAGE, binding3.getKind());
        assertEquals("wrong name2", "java.util", binding3.getName());
        simpleName = qualifiedName2.getName();
        binding3 = simpleName.resolveBinding();
        assertNotNull("No binding", binding3);
        assertEquals("wrong kind", IBinding.PACKAGE, binding3.getKind());
        assertEquals("wrong name2", "java.util", binding3.getName());
        name2 = qualifiedName2.getQualifier();
        assertTrue("Not a simple name", name2.isSimpleName());
        simpleName = (SimpleName) name2;
        binding3 = simpleName.resolveBinding();
        assertNotNull("No binding", binding3);
        assertEquals("wrong kind", IBinding.PACKAGE, binding3.getKind());
        assertEquals("wrong name2", "java", binding3.getName());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79271
	 */
	public void test0075() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"import java.util.ArrayList;\n" +
			"public class X {\n" +
			"  /*start*/ArrayList<Integer>/*end*/ field;" +
			"}",
			this.workingCopy);
		ITypeBinding binding = ((Type) node).resolveBinding();
		ITypeBinding genericType = binding.getTypeDeclaration();
		assertFalse("Equals", binding.isEqualTo(genericType));
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79362
	 */
	public void test0076() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0076", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput = "Type mismatch: cannot convert from Map[] to Map<String,Double>[][]";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "Map<String, Double>[][]", source);
		assertEquals("wrong type", ASTNode.ARRAY_TYPE, type.getNodeType());
		ArrayType arrayType = (ArrayType) type;
		type = componentType(arrayType);
		checkSourceRange(type, "Map<String, Double>", source);
		assertEquals("wrong type", ASTNode.PARAMETERIZED_TYPE, type.getNodeType());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79362
	 */
	public void test0077() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0077", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		char[] source = sourceUnit.getSource().toCharArray();
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput = "Type mismatch: cannot convert from Map[] to Map<String,Double>[][]";
		assertProblemsSize(compilationUnit, 1, expectedOutput);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong type", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		checkSourceRange(type, "java.util.Map<String, Double>[][]", source);
		assertEquals("wrong type", ASTNode.ARRAY_TYPE, type.getNodeType());
		ArrayType arrayType = (ArrayType) type;
		type = componentType(arrayType);
		checkSourceRange(type, "java.util.Map<String, Double>", source);
		assertEquals("wrong type", ASTNode.PARAMETERIZED_TYPE, type.getNodeType());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0078() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"public class X<T> {\n" +
			"  String foo(int i) { return /*start*/Integer.toString(i)/*end*/;}" +
			"}",
			this.workingCopy);
		IMethodBinding methodBinding = ((MethodInvocation) node).resolveMethodBinding();
		assertFalse("Is a raw method", methodBinding.isRawMethod());
		assertFalse("Is a parameterized method", methodBinding.isParameterizedMethod());
		assertFalse("Is a generic method", methodBinding.isGenericMethod());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0079() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
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
			this.workingCopy);
		IMethodBinding methodBinding = ((MethodDeclaration) node).resolveBinding();
		assertFalse("Is a raw method", methodBinding.isRawMethod());
		assertFalse("Is a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a generic method", methodBinding.isGenericMethod());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0080() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
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
			this.workingCopy);
		IMethodBinding methodBinding = ((MethodInvocation) node).resolveMethodBinding();
		assertFalse("Is a raw method", methodBinding.isRawMethod());
		assertTrue("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertFalse("Is a generic method", methodBinding.isGenericMethod());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79460
	 */
	public void test0081() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0081", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput =
			"Type safety: The method foo(Object) belongs to the raw type Y. References to generic type Y<T> should be parameterized";
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
		assertFalse("Doesn't override itself", methodBinding.overrides(methodBinding));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78183
	 */
	public void test0082() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0082", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		String expectedOutput =	"";
		assertProblemsSize(compilationUnit, 0, expectedOutput);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
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
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
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
			this.workingCopy);
		TypeLiteral typeLiteral = (TypeLiteral) node;
		ITypeBinding typeBinding = typeLiteral.resolveTypeBinding();
		assertEquals("Wrong name", "java.lang.Class<p.X>", typeBinding.getQualifiedName());
		assertEquals("Wrong name", "Class<X>", typeBinding.getName());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79775
	 */
	public void test0087() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		buildAST(
			"package p;\n" +
			"public class X<T1> {\n" +
			"	public <M1> X() {\n" +
			"	}\n" +
			"	class Y<T2> {\n" +
			"		public <M2> Y() {\n" +
			"		}\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		new <Object>X<Object>().new <Object>Y<Object>();\n" +
			"	}\n" +
			"}\n",
			this.workingCopy);
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=79690
	 */
	public void test0088() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0088", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong type", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Wrong name", "E", typeBinding.getName());
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertTrue("Not from source", typeBinding.isFromSource());
		ASTNode node2 = compilationUnit.findDeclaringNode(typeBinding);
		assertNotNull("No declaring node", node2);
		ASTNode node3 = compilationUnit.findDeclaringNode(typeBinding.getKey());
		assertNotNull("No declaring node", node3);
		assertTrue("Nodes don't match", node2.subtreeMatch(new ASTMatcher(), node3));
		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Wrong type", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List typeParameters = methodDeclaration.typeParameters();
		assertEquals("Wrong size", 1, typeParameters.size());
		TypeParameter typeParameter = (TypeParameter) typeParameters.get(0);
		assertTrue("Nodes don't match", typeParameter.subtreeMatch(new ASTMatcher(), node3));
		assertTrue("Nodes don't match", typeParameter.subtreeMatch(new ASTMatcher(), node2));
	}

	/*
	 * Ensures that a parameterized method binding (with a wildcard parameter) doesn't throw a NPE when computing its binding key.
	 * (regression test for 79967 NPE in WildcardBinding.signature with Mark Occurrences in Collections.class)
	 */
	public void test0089() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"public class X<T> {\n" +
			"  void foo() {\n" +
			"  }\n" +
			"  void bar(X<?> x) {\n" +
			"    /*start*/x.foo()/*end*/;\n"+
			"  }\n" +
			"}",
			this.workingCopy);
		IBinding binding = ((MethodInvocation) node).resolveMethodBinding();
		assertBindingKeyEquals(
			"Lp/X<!Lp/X;{0}*75;>;.foo()V",
			binding.getKey());
	}

	/*
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=80021
	 */
	public void test0090() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode result = buildAST(
			"package p;\n" +
			"public class X {\n" +
			"	public void foo() {}\n" +
			"	public void bar(X x, int f) {\n" +
			"		x.foo();\n" +
			"	}\n" +
			"}",
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, result.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		compilationUnit.accept(new ASTVisitor() {
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
			 */
			public boolean visit(SingleVariableDeclaration node) {
				IVariableBinding binding = node.resolveBinding();
				assertNotNull("No binding", binding);
				IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
			 */
			public boolean visit(VariableDeclarationFragment node) {
				IVariableBinding binding = node.resolveBinding();
				assertNotNull("No binding", binding);
				IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldAccess)
			 */
			public boolean visit(FieldAccess node) {
				IVariableBinding binding = node.resolveFieldBinding();
				assertNotNull("No binding", binding);
				IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.SuperFieldAccess)
			 */
			public boolean visit(SuperFieldAccess node) {
				IVariableBinding binding = node.resolveFieldBinding();
				assertNotNull("No binding", binding);
				IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				return false;
			}
		});
	}

	/*
	 * Check bindings for annotation type declaration
	 */
	public void test0091() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"@interface X {\n" +
			"	int id() default 0;\n" +
			"}",
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not an annotation type declaration", ASTNode.ANNOTATION_TYPE_DECLARATION, node.getNodeType());
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) node;
		ITypeBinding binding = annotationTypeDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertTrue("Not an annotation", binding.isAnnotation());
		assertEquals("Wrong name", "X", binding.getName());
		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not an annotation type member declaration", ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION, node.getNodeType());
		AnnotationTypeMemberDeclaration memberDeclaration = (AnnotationTypeMemberDeclaration) node;
		IMethodBinding methodBinding = memberDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		assertEquals("Wrong name", "id", methodBinding.getName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80960
	 */
	public void test0092() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"import java.util.*;\n" +
			"public class X {\n" +
			"  public enum Rank { DEUCE, THREE, FOUR, FIVE, SIX,\n" +
			"    SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }\n" +
			"\n" +
			"  //public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }\n" +
			"  public enum Suit{\n" +
			"\n" +
			"  private X(int rank, int suit) {  \n" +
			"  }\n" +
			"  \n" +
			"  private static final List<X> protoDeck = new ArrayList<X>();\n" +
			"  \n" +
			"  public static ArrayList<X> newDeck() {\n" +
			"      return new ArrayList<X>(protoDeck); // Return copy of prototype deck\n" +
			"  }\n" +
			"}",
			this.workingCopy,
			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81023
	 */
	public void test0093() throws JavaModelException {
		String contents =
			"public class Test {\n" +
			"    public <U> Test(U u) {\n" +
			"    }\n" +
			"\n" +
			"    void bar() {\n" +
			"        new <String> Test(null) {};\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/Test.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		checkSourceRange(expression, "new <String> Test(null) {}", contents.toCharArray());
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		IJavaElement element = typeBinding.getJavaElement();
		assertNotNull("No java element", element);
	}


	public void test0094() throws JavaModelException {
		String contents =
			"import java.lang.annotation.Target;\n" +
			"import java.lang.annotation.Retention;\n" +
			"\n" +
			"@Retention(RetentionPolicy.SOURCE)\n" +
			"@Target(ElementType.METHOD)\n" +
			"@interface ThrowAwayMethod {\n" +
			"\n" +
			"	/**\n" +
			"	 * Comment for <code>test</code>\n" +
			"	 */\n" +
			"	protected final Test test;\n" +
			"\n" +
			"	/**\n" +
			"	 * @param test\n" +
			"	 */\n" +
			"	ThrowAwayMethod(Test test) {\n" +
			"		this.test= test;\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/ThrowAwayMethod.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy,
			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
	}

	/*
	 * Ensures that resolving a generic method with a non existing parameter type doesn't throw a NPE when computing its binding key.
	 * (regression test for 81134 [dom] [5.0] NPE when creating AST
	 */
	public void test0095() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"public class X {\n" +
			"   /*start*/<T> void foo(NonExisting arg) {\n" +
			"   }/*end*/\n" +
			"}",
			this.workingCopy,
			false,
			false,
			true);
		IMethodBinding methodBinding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", methodBinding);
		assertEquals("LX;.foo<T:Ljava/lang/Object;>(LNonExisting;)V", methodBinding.getKey());
		assertFalse("Method should not be flagged as recovered", methodBinding.isRecovered());
		assertTrue("Method argument type should be flagged as recovered", methodBinding.getParameterTypes()[0].isRecovered());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82140
	 */
	public void test0096() throws JavaModelException {
		String contents =
			"public @interface An1 {\n" +
			"	String value();\n" +
			"	String item() default \"Hello\";\n" +
			"\n" +
			"}\n" +
			"\n" +
			"@An1(value=\"X\") class A {\n" +
			"	\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/An1.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		assertEquals("Wrong name", "A", typeDeclaration.getName().getIdentifier());
		List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier instanceof Annotation);
		checkSourceRange((Annotation) modifier, "@An1(value=\"X\")", contents.toCharArray());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82140
	 */
	public void test0097() throws JavaModelException {
		String contents =
			"@interface An1 {}\n" +
			"@interface An2 {}\n" +
			"@interface An3 {}\n" +
			"@An2 class X {\n" +
			"	@An1 Object o;\n" +
			"	@An3 void foo() {\n" +
			"		\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 3);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier instanceof Annotation);
		checkSourceRange((Annotation) modifier, "@An2", contents.toCharArray());

		node = getASTNode(compilationUnit, 3, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		modifiers = fieldDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier instanceof Annotation);
		checkSourceRange((Annotation) modifier, "@An1", contents.toCharArray());

		node = getASTNode(compilationUnit, 3, 1);
		assertEquals("Not a field declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier instanceof Annotation);
		checkSourceRange((Annotation) modifier, "@An3", contents.toCharArray());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82140
	 */
	public void test0098() throws JavaModelException {
		String contents =
			"class X {\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 0, modifiers.size());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82141
	 */
	public void test0099() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	@Override @Annot(value=\"Hello\") public String toString() {\n" +
			"		return super.toString();\n" +
			"	}\n" +
			"	@Annot(\"Hello\") void bar() {\n" +
			"	}\n" +
			"	@interface Annot {\n" +
			"		String value();\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong size", 3, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Wrong type", modifier instanceof Annotation);
		Annotation annotation = (Annotation) modifier;
		ITypeBinding binding = annotation.resolveTypeBinding();
		assertNotNull("No binding", binding);

		modifier = (IExtendedModifier) modifiers.get(1);
		assertTrue("Wrong type", modifier instanceof Annotation);
		annotation = (Annotation) modifier;
		binding = annotation.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		List values = normalAnnotation.values();
		assertEquals("wrong size", 1, values.size());
		MemberValuePair valuePair = (MemberValuePair) values.get(0);
		SimpleName name = valuePair.getName();
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding", binding2);
		ITypeBinding typeBinding = name.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);

		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Wrong type", modifier instanceof Annotation);
		annotation = (Annotation) modifier;
		binding = annotation.resolveTypeBinding();
		assertNotNull("No binding", binding);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82216
	 */
	public void test0100() throws JavaModelException {
		String contents =
			"public enum E {\n" +
			"	A, B, C;\n" +
			"	public static final E D = B;\n" +
			"	public static final String F = \"Hello\";\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/E.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("wrong size", 3, enumConstants.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		IMethodBinding methodBinding = enumConstantDeclaration.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertTrue("Not a constructor", methodBinding.isConstructor());
		IVariableBinding variableBinding = enumConstantDeclaration.resolveVariable();
		assertNotNull("no binding", variableBinding);
		assertNull("is constant", variableBinding.getConstantValue());
		assertTrue("Not an enum constant", variableBinding.isEnumConstant());

		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		assertEquals("wrong name", "D", fragment.getName().getIdentifier());
		variableBinding = fragment.resolveBinding();
		assertNotNull("no binding", variableBinding);
		assertFalse("An enum constant", variableBinding.isEnumConstant());

		node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		assertEquals("wrong name", "F", fragment.getName().getIdentifier());
		variableBinding = fragment.resolveBinding();
		assertNotNull("no binding", variableBinding);
		assertNotNull("is constant", variableBinding.getConstantValue());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68823
	 */
	public void test0101() throws JavaModelException {
		String contents =
			"public class X{\n" +
			"	public void foo() {\n" +
			"		assert (true): (\"hello\");\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents.toCharArray());
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		String expectedOutput = "Dead code";
		assertProblemsSize(compilationUnit, 1, expectedOutput);

		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an assert statement", ASTNode.ASSERT_STATEMENT, node.getNodeType());
		AssertStatement assertStatement = (AssertStatement) node;
		final char[] source = contents.toCharArray();
		checkSourceRange(assertStatement.getExpression(), "(true)", source);
		checkSourceRange(assertStatement.getMessage(), "(\"hello\")", source);
		checkSourceRange(assertStatement, "assert (true): (\"hello\");", source);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82670
	 */
	public void test0102() throws JavaModelException {
		String contents =
			"import java.util.HashMap;\n" +
			"\n" +
			"public class X {\n" +
			"    Object o= new HashMap<?, ?>[0];\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		checkSourceRange(fragment, "o= new HashMap<?, ?>[0]", contents.toCharArray());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82985
	 */
	public void test0103() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0103", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List imports = compilationUnit.imports();
		assertEquals("Wrong size", 2, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		IBinding binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());
		Name name = importDeclaration.getName();
		binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		SimpleName simpleName = qualifiedName.getName();
		binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.VARIABLE, binding.getKind());

		Name name2 = qualifiedName.getQualifier();
		binding = name2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());

		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name2.getNodeType());
		qualifiedName = (QualifiedName) name2;
		simpleName = qualifiedName.getName();
		binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());

		Name name3 = qualifiedName.getQualifier();
		binding = name3.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.PACKAGE, binding.getKind());

		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name3.getNodeType());

		importDeclaration = (ImportDeclaration) imports.get(1);
		binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertFalse("Not a single name import", importDeclaration.isOnDemand());
		name = importDeclaration.getName();
		binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.METHOD, binding.getKind());

		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		qualifiedName = (QualifiedName) name;
		simpleName = qualifiedName.getName();
		binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.METHOD, binding.getKind());

		name2 = qualifiedName.getQualifier();
		binding = name2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name2.getNodeType());
		qualifiedName = (QualifiedName) name2;
		simpleName = qualifiedName.getName();
		binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.TYPE, binding.getKind());

		name2 = qualifiedName.getQualifier();
		binding = name2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.PACKAGE, binding.getKind());
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name2.getNodeType());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82985
	 */
	public void test0104() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0104", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List imports = compilationUnit.imports();
		assertEquals("Wrong size", 1, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		IBinding binding = importDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		int kind = binding.getKind();
		assertTrue("Wrong type", kind == IBinding.VARIABLE || kind == IBinding.METHOD);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83011
	 */
	public void test0105() throws JavaModelException {
		String contents =
			"@interface Ann {}\n" +
			"\n" +
			"@Ann public class X {}\n";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not a marker annotation", extendedModifier instanceof MarkerAnnotation);
		MarkerAnnotation markerAnnotation = (MarkerAnnotation) extendedModifier;
		ITypeBinding binding = markerAnnotation.resolveTypeBinding();
		assertNotNull("No binding", binding);
		Name name = markerAnnotation.getTypeName();
		binding = name.resolveTypeBinding();
		assertNotNull("No binding", binding);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83011
	 */
	public void test0106() throws JavaModelException {
		String contents =
			"package p;\n" +
			"@interface Ann {}\n" +
			"\n" +
			"@p.Ann public class X {}\n";
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not a marker annotation", extendedModifier instanceof MarkerAnnotation);
		MarkerAnnotation markerAnnotation = (MarkerAnnotation) extendedModifier;
		ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		Name name = markerAnnotation.getTypeName();
		typeBinding = name.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		IBinding binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong kind of binding", IBinding.TYPE, binding.getKind());
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		SimpleName simpleName = qualifiedName.getName();
		binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		name = qualifiedName.getQualifier();
		binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong kind of binding", IBinding.PACKAGE, binding.getKind());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83013
	 */
	public void test0107() throws JavaModelException {
		String contents =
			"@interface A {\n" +
			"    String value() default \"\";\n" +
			"}\n" +
			"@interface Main {\n" +
			"   A child() default @A(\"Void\");\n" +
			"}\n" +
			"@Main(child=@A(\"\")) @A class X {}\n";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 2);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		checkSourceRange(node, "@Main(child=@A(\"\")) @A class X {}", contents.toCharArray());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83228
	 */
	public void test0108() throws JavaModelException {
		String contents =
			"class X<E> {\n" +
			"    enum Numbers {\n" +
			"        ONE {\n" +
			"            Numbers getSquare() {\n" +
			"                return ONE;\n" +
			"            }\n" +
			"        };\n" +
			"        abstract Numbers getSquare();\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;

		List bodyDeclarations = enumDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclarations.get(0);
		Type returnType = methodDeclaration.getReturnType2();
		ITypeBinding typeBinding = returnType.resolveBinding();

		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("Wrong size", 1, enumConstants.size());
		EnumConstantDeclaration constantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		AnonymousClassDeclaration anonymousClassDeclaration = constantDeclaration.getAnonymousClassDeclaration();
		assertNotNull("No anonymous", anonymousClassDeclaration);
		bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Type type = methodDeclaration.getReturnType2();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		SimpleName simpleName = (SimpleName) name;
		ITypeBinding typeBinding2 = simpleName.resolveTypeBinding();

		assertTrue("Not equals", typeBinding.isEqualTo(typeBinding2));
		assertTrue("Not identical", typeBinding == typeBinding2);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=883297
	 */
	public void test0109() throws JavaModelException {
		String contents =
			"@Annot(value=\"Hello\", count=-1)\n" +
			"@interface Annot {\n" +
			"    String value();\n" +
			"    int count();\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/Annot.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) node;
		ITypeBinding typeBinding = annotationTypeDeclaration.resolveBinding();
		assertNotNull("No type binding", typeBinding);
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertEquals("Wrong size", 2, methods.length);
	}

	/*
	 * Ensures that the type declaration of a top level type binding is correct.
	 */
	public void test0110() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		TypeDeclaration type = (TypeDeclaration) buildAST(
			"/*start*/public class X {\n" +
			"}/*end*/",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getTypeDeclaration();
		assertBindingEquals(
			"LX;",
			binding);
	}

	/*
	 * Ensures that the type declaration of a generic type binding is correct.
	 */
	public void test0111() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		TypeDeclaration type = (TypeDeclaration) buildAST(
			"/*start*/public class X<E> {\n" +
			"}/*end*/",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getTypeDeclaration();
		assertBindingEquals(
			"LX<TE;>;",
			binding);
	}

	/*
	 * Ensures that the type declaration of a parameterized type binding is correct.
	 */
	public void test0112() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  /*start*/X<String>/*end*/ field;\n" +
			"}",
			this.workingCopy,
			false);
		ITypeBinding binding = type.resolveBinding().getTypeDeclaration();
		assertBindingEquals(
			"LX<TE;>;",
			binding);
	}

	/*
	 * Ensures that the type declaration of a raw type binding is correct.
	 */
	public void test0113() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  /*start*/X/*end*/ field;\n" +
			"}",
			this.workingCopy,
			false);
		ITypeBinding binding = type.resolveBinding().getTypeDeclaration();
		assertBindingEquals(
			"LX<TE;>;",
			binding);
	}

	/*
	 * Ensures that the type declaration of a wildcard type binding is correct.
	 */
	public void test0114() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  X</*start*/? extends String/*end*/> field;\n" +
			"}",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getTypeDeclaration();
		assertBindingEquals(
			"LX;{0}+Ljava/lang/String;",
			binding);
	}

	/*
	 * Ensures that the type declaration of a type variable binding is correct.
	 */
	public void test0115() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		TypeParameter type = (TypeParameter) buildAST(
			"public class X</*start*/E/*end*/> {\n" +
			"}",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getTypeDeclaration();
		assertBindingEquals(
			"LX;:TE;",
			binding);
	}

	/*
	 * Ensures that the erasure of a top level type binding is correct.
	 */
	public void test0116() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		TypeDeclaration type = (TypeDeclaration) buildAST(
			"/*start*/public class X {\n" +
			"}/*end*/",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getErasure();
		assertBindingEquals(
			"LX;",
			binding);
	}

	/*
	 * Ensures that the erasure of a generic type binding is correct.
	 */
	public void test0117() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		TypeDeclaration type = (TypeDeclaration) buildAST(
			"/*start*/public class X<E> {\n" +
			"}/*end*/",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getErasure();
		assertBindingEquals(
			"LX<TE;>;",
			binding);
	}

	/*
	 * Ensures that the erasure of a parameterized type binding is correct.
	 */
	public void test0118() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  /*start*/X<String>/*end*/ field;\n" +
			"}",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getErasure();
		assertBindingEquals(
			"LX<TE;>;",
			binding);
	}

	/*
	 * Ensures that the erasure of a raw type binding is correct.
	 */
	public void test0119() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  /*start*/X/*end*/ field;\n" +
			"}",
			this.workingCopy,
			false);
		ITypeBinding binding = type.resolveBinding().getErasure();
		assertBindingEquals(
			"LX<TE;>;",
			binding);
	}

	/*
	 * Ensures that the erasure of a wildcard type binding is correct.
	 */
	public void test0120() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  X</*start*/? extends String/*end*/> field;\n" +
			"}",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getErasure();
		assertBindingEquals(
			"Ljava/lang/String;",
			binding);
	}

	/*
	 * Ensures that the erasure of a type variable binding is correct.
	 */
	public void test0121() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		TypeParameter type = (TypeParameter) buildAST(
			"public class X</*start*/E/*end*/> {\n" +
			"}",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding().getErasure();
		assertBindingEquals(
			"Ljava/lang/Object;",
			binding);
	}

	/*
	 * Ensures that the declaration of a non generic method binding is correct.
	 */
	public void test0122() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		MethodDeclaration method = (MethodDeclaration) buildAST(
			"public class X {\n" +
			"  /*start*/void foo() {\n" +
			"  }/*end*/\n" +
			"}",
			this.workingCopy);
		IMethodBinding binding = method.resolveBinding().getMethodDeclaration();
		assertBindingEquals(
			"LX;.foo()V",
			binding);
	}

	/*
	 * Ensures that the declaration of a generic method binding is correct.
	 */
	public void test0123() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		MethodDeclaration method = (MethodDeclaration) buildAST(
			"public class X {\n" +
			"  /*start*/<E> void foo() {\n" +
			"  }/*end*/\n" +
			"}",
			this.workingCopy);
		IMethodBinding binding = method.resolveBinding().getMethodDeclaration();
		assertBindingEquals(
			"LX;.foo<E:Ljava/lang/Object;>()V",
			binding);
	}

	/*
	 * Ensures that the declaration of a parameterized method binding is correct.
	 */
	public void test0124() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		MethodInvocation method = (MethodInvocation) buildAST(
			"public class X {\n" +
			"  <E> void foo() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    /*start*/this.<String>foo()/*end*/;\n" +
			"  }\n" +
			"}",
			this.workingCopy);
		IMethodBinding binding = method.resolveMethodBinding().getMethodDeclaration();
		assertBindingEquals(
			"LX;.foo<E:Ljava/lang/Object;>()V",
			binding);
	}

	/*
	 * Ensures that the declaration of a raw method binding is correct.
	 */
	public void test0125() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		MethodInvocation method = (MethodInvocation) buildAST(
			"public class X {\n" +
			"  <E> void foo() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    /*start*/this.foo()/*end*/;\n" +
			"  }\n" +
			"}",
			this.workingCopy);
		IMethodBinding binding = method.resolveMethodBinding().getMethodDeclaration();
		assertBindingEquals(
			"LX;.foo<E:Ljava/lang/Object;>()V",
			binding);
	}

	/*
	 * Ensures that the key for a parameterized type binding with an extends wildcard bounded to a type variable
	 * is correct.
	 */
	public void test0126() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"public class X<E> {\n" +
			"  /*start*/Class<? extends E>/*end*/ field;\n" +
			"}",
			this.workingCopy);
		ITypeBinding binding = type.resolveBinding();
		assertBindingEquals(
			"Ljava/lang/Class<Ljava/lang/Class;{0}+LX;:TE;>;",
			binding);
	}

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83817
    public void test0127() throws CoreException {
        this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
        ASTNode node = buildAST(
            "class X<T> {\n" +
            "    public void method(Number num) {}\n" +
            "}\n" +
            "\n" +
            "class Z {\n" +
            "	void test() {\n" +
            "		new X<String>().method(0);\n" +
            "		new X<Integer>().method(1);\n" +
            "	}\n" +
            "}",
            this.workingCopy);
        assertNotNull("No node", node);
        assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
        CompilationUnit compilationUnit = (CompilationUnit) node;
        assertProblemsSize(compilationUnit, 0);
        node = getASTNode(compilationUnit, 1, 0, 0);
        assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
        ExpressionStatement statement = (ExpressionStatement) node;
        Expression expression = statement.getExpression();
        assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
        MethodInvocation methodInvocation = (MethodInvocation) expression;
        IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
        node = getASTNode(compilationUnit, 1, 0, 1);
        assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
        statement = (ExpressionStatement) node;
        expression = statement.getExpression();
        assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
        methodInvocation = (MethodInvocation) expression;
        IMethodBinding methodBinding2 = methodInvocation.resolveMethodBinding();
        assertFalse("Keys are equals", methodBinding.getKey().equals(methodBinding2.getKey()));
        assertFalse("bindings are equals", methodBinding.isEqualTo(methodBinding2));
    }

   // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84064
    public void test0128() throws CoreException {
        this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
        final String contents =
        	"class X {\n" +
            "	static X x;\n" +
            "\n" +
            "	static class G extends E {\n" +
            "		public G() {\n" +
            "			x.<String> super();\n" +
            "		}\n" +
            "	}\n" +
            "\n" +
            "	class E {\n" +
            "		public <T> E() {\n" +
            "		}\n" +
            "	}\n" +
            "}";
        final char[] source = contents.toCharArray();
        ASTNode node = buildAST(
            contents,
            this.workingCopy);
        assertNotNull("No node", node);
        assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
        CompilationUnit compilationUnit = (CompilationUnit) node;
        assertProblemsSize(compilationUnit, 0);
        node = getASTNode(compilationUnit, 0, 1, 0);
        assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        assertTrue("Not a constructor", methodDeclaration.isConstructor());
        Block body = methodDeclaration.getBody();
        assertNotNull("No body", body);
        List statements = body.statements();
        assertEquals("Wrong size", 1, statements.size());
        Statement statement = (Statement) statements.get(0);
        assertEquals("Not a super constructor invocation", ASTNode.SUPER_CONSTRUCTOR_INVOCATION, statement.getNodeType());
        SuperConstructorInvocation constructorInvocation = (SuperConstructorInvocation) statement;
        checkSourceRange(constructorInvocation, "x.<String> super();", source);
    }

   // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84064
    public void test0129() throws CoreException {
        this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
        final String contents =
        	"class X {\n" +
        	"	static X x;\n" +
        	"	static class G extends E {\n" +
        	"		public <T> G() {\n" +
        	"			x.<String> this();\n" +
        	"		}\n" +
        	"	}\n" +
        	"	static class E {\n" +
        	"		public <T> E() {\n" +
        	"		}\n" +
        	"	}\n" +
        	"}";
        final char[] source = contents.toCharArray();
        ASTNode node = buildAST(
            contents,
            this.workingCopy,
            false);
        assertNotNull("No node", node);
        assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
        CompilationUnit compilationUnit = (CompilationUnit) node;
        String expectedProblem = "Illegal enclosing instance specification for type X.G";
        assertProblemsSize(compilationUnit, 1, expectedProblem);
        node = getASTNode(compilationUnit, 0, 1, 0);
        assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        assertTrue("Not a constructor", methodDeclaration.isConstructor());
        Block body = methodDeclaration.getBody();
        assertNotNull("No body", body);
        List statements = body.statements();
        assertEquals("Wrong size", 1, statements.size());
        Statement statement = (Statement) statements.get(0);
        assertEquals("Not a constructor invocation", ASTNode.CONSTRUCTOR_INVOCATION, statement.getNodeType());
        ConstructorInvocation constructorInvocation = (ConstructorInvocation) statement;
        checkSourceRange(constructorInvocation, "x.<String> this();", source, true/*expectMalformed*/);
        assertTrue("Node is not malformed", isMalformed(constructorInvocation));
    }

   // https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
    public void test0130() throws CoreException {
        this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
        final String contents =
        	"class Outer<A> {\n" +
        	"	class Inner {\n" +
        	"		class InnerInner<C> {\n" +
        	"		}\n" +
        	"	}\n" +
        	"}\n" +
        	"\n" +
        	"public class X {\n" +
        	"	void foo() {\n" +
        	"		Outer<String>.Inner.InnerInner<Integer> in = new Outer<String>().new Inner(). new InnerInner<Integer>();\n" +
        	"	}\n" +
        	"}";
        ASTNode node = buildAST(
            contents,
            this.workingCopy);
        assertNotNull("No node", node);
        assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
        CompilationUnit compilationUnit = (CompilationUnit) node;
        assertProblemsSize(compilationUnit, 0);
        node = getASTNode(compilationUnit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		ITypeBinding typeBinding = parameterizedType.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 1", "Outer<java.lang.String>.Inner.InnerInner<java.lang.Integer>", typeBinding.getQualifiedName());
		type = parameterizedType.getType();
		assertTrue("Not a qualified type", type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		typeBinding = qualifiedType.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 2", "Outer<java.lang.String>.Inner.InnerInner<java.lang.Integer>", typeBinding.getQualifiedName());
		SimpleName simpleName = qualifiedType.getName();
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 3", "Outer<java.lang.String>.Inner.InnerInner<java.lang.Integer>", typeBinding.getQualifiedName());
		type = qualifiedType.getQualifier();
		assertTrue("Not a qualified type", type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		typeBinding = qualifiedType.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 4", "Outer<java.lang.String>.Inner", typeBinding.getQualifiedName());
		simpleName = qualifiedType.getName();
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 5", "Outer.Inner", typeBinding.getQualifiedName());
		type = qualifiedType.getQualifier();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		parameterizedType = (ParameterizedType) type;
		typeBinding = parameterizedType.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 6", "Outer<java.lang.String>", typeBinding.getQualifiedName());
		type = parameterizedType.getType();
		assertTrue("Not a simple type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		typeBinding = simpleType.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 7", "Outer<java.lang.String>", typeBinding.getQualifiedName());
		Name name = simpleType.getName();
		assertTrue("Not a simple name", name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong qualified name 8", "Outer", typeBinding.getQualifiedName());
   }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84140
    public void test0131() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
			"	public void bar(String... args){\n" +
			"	}\n" +
			"}";
     	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
	   	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    	MethodDeclaration methodDeclaration = (MethodDeclaration) node;
    	List parameters = methodDeclaration.parameters();
    	assertEquals("Wrong size", 1, parameters.size());
    	SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
    	assertTrue("Not a var args", singleVariableDeclaration.isVarargs());
	   	Type type = singleVariableDeclaration.getType();
    	checkSourceRange(type, "String", contents);
     	assertTrue("Not a simple type", type.isSimpleType());
    	checkSourceRange(type, "String", contents);
    	ITypeBinding typeBinding = type.resolveBinding();
    	assertNotNull("No binding", typeBinding);
    	assertFalse("An array", typeBinding.isArray());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		ITypeBinding parameterType = parameterTypes[0];
    	assertTrue("Not an array binding", parameterType.isArray());
    	assertTrue("Not equals", parameterType.getComponentType() == parameterType.getElementType());
    	assertEquals("wrong dimension", 1, parameterType.getDimensions());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84140
    public void test0132() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
    		"	public void bar(String[]... args[]){\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 1, "Extended dimensions are illegal for a variable argument");
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    	MethodDeclaration methodDeclaration = (MethodDeclaration) node;
    	List parameters = methodDeclaration.parameters();
    	assertEquals("Wrong size", 1, parameters.size());
    	SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
    	assertTrue("Not a var args", singleVariableDeclaration.isVarargs());
		assertTrue("Not a malformed node", isMalformed(singleVariableDeclaration));
    	Type type = singleVariableDeclaration.getType();
    	checkSourceRange(type, "String[]", contents);
    	assertTrue("Not an array type", type.isArrayType());
    	ITypeBinding typeBinding = type.resolveBinding();
    	assertNotNull("No binding", typeBinding);
    	assertTrue("Not an array", typeBinding.isArray());
    	assertEquals("wrong dimensions", 1, typeBinding.getDimensions());
    	ArrayType arrayType = (ArrayType) type;
    	assertEquals("Wrong dimension", 1, arrayType.getDimensions());
    	type = componentType(arrayType);
    	assertTrue("Not a simple type", type.isSimpleType());
    	checkSourceRange(type, "String", contents);
    	assertEquals("Wrong extra dimension", 1, singleVariableDeclaration.getExtraDimensions());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		ITypeBinding parameterType = parameterTypes[0];
    	assertTrue("Not an array binding", parameterType.isArray());
       	assertEquals("wrong dimension", 3, parameterType.getDimensions());
       	ITypeBinding componentType = parameterType.getComponentType();
       	assertEquals("wrong dimension", 2, componentType.getDimensions());
       	assertTrue("Not equal", parameterType.getElementType() == componentType.getElementType());
       	ITypeBinding componentType2 = componentType.getComponentType();
       	assertEquals("wrong dimension", 1, componentType2.getDimensions());
       	assertTrue("Not equal", parameterType.getElementType() == componentType2.getElementType());
     }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84181
    public void test0133() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"import java.util.Vector;\n" +
    		"\n" +
    		"public class X {\n" +
    		"  void k() {\n" +
    		"    Vector v2 = /*start*/new Vector()/*end*/;\n" +
    		"    Vector v3 = new Vector();\n" +
    		"\n" +
    		"    v3.add(\"fff\");\n" +
    		"    v2.add(v3);\n" +
    		"   }\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a class instance creation unit", ASTNode.CLASS_INSTANCE_CREATION, node.getNodeType());
    	ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
    	ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
    	assertEquals("wrong qualified name", "java.util.Vector", typeBinding.getQualifiedName());
    	assertTrue("Not a raw type", typeBinding.isRawType());
    	assertFalse("From source", typeBinding.isFromSource());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84181
    public void test0134() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"import java.util.Vector;\n" +
    		"\n" +
    		"public class X {\n" +
    		"  void k() {\n" +
    		"    Vector v2 = /*start*/new Vector<String>()/*end*/;\n" +
    		"\n" +
    		"    v2.add(\"\");\n" +
    		"   }\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a class instance creation unit", ASTNode.CLASS_INSTANCE_CREATION, node.getNodeType());
    	ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
    	ITypeBinding typeBinding = classInstanceCreation.resolveTypeBinding();
    	assertEquals("wrong qualified name", "java.util.Vector<java.lang.String>", typeBinding.getQualifiedName());
    	assertTrue("Not a parameterized type", typeBinding.isParameterizedType());
    	assertFalse("From source", typeBinding.isFromSource());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
    public void test0135() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"class X {\n" +
    		"	public static X instance= new X();\n" +
    		"\n" +
    		"	int s;\n" +
    		"\n" +
    		"	int f() {\n" +
    		"		System.out.println(X.instance.s + 1);\n" +
    		"		return 1;\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
			contents,
			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	compilationUnit.accept(new ASTVisitor() {
    		public boolean visit(QualifiedName qualifiedName) {
    			ITypeBinding typeBinding = qualifiedName.resolveTypeBinding();
    			assertNotNull("No binding", typeBinding);
    			return true;
    		}
    	});
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=78934
    public void test0136() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"class X {\n" +
    		"	public static X instance= new X();\n" +
    		"	public X instance2 = new X();\n" +
    		"	int s;\n" +
    		"	int f() {\n" +
    		"		System.out.println(X.instance.instance2.s + 1);\n" +
    		"		return 1;\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
			contents,
			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	compilationUnit.accept(new ASTVisitor() {
    		public boolean visit(QualifiedName qualifiedName) {
    			ITypeBinding typeBinding = qualifiedName.resolveTypeBinding();
    			assertNotNull("No binding", typeBinding);
    			return true;
    		}
    	});
    }

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=79696
	 */
	public void test0137() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0137", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong node", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		type = parameterizedType.getType();
		assertTrue("Not a parameterized type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		name = qualifiedName.getQualifier();
		assertTrue("Not a simple name", name.isSimpleName());
		ITypeBinding typeBinding = name.resolveTypeBinding();
		assertEquals("Wrong name", "test0137.Source", typeBinding.getQualifiedName());
	}

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=81544
	public void test0138() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
    		"class X {\n" +
    		"	java.util.List<URL> method(java.util.List<URL> list) {\n" +
    		"		java.util.List<URL> url= new java.util.List<URL>();\n" +
    		"		return url;\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
			contents,
			this.workingCopy,
			false,
			false,
			true);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 5,
    			"URL cannot be resolved to a type\n" +
    			"URL cannot be resolved to a type\n" +
    			"URL cannot be resolved to a type\n" +
    			"Cannot instantiate the type List<URL>\n" +
    			"URL cannot be resolved to a type");
    	compilationUnit.accept(new ASTVisitor() {
    		public boolean visit(ParameterizedType type) {
    			checkSourceRange(type, "java.util.List<URL>", contents);
    			ITypeBinding typeBinding = type.resolveBinding();
    			assertNotNull("No binding", typeBinding);
    			return true;
    		}
    	});
    }

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84358
	 */
	public void test0139() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0139", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false, true);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 1, "The type test0139a.C is not visible");
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Wrong node", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		Type type = statement.getType();
		assertTrue("Not a parameterized type", type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		type = parameterizedType.getType();
		assertTrue("Not a parameterized type", type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		Name name = simpleType.getName();
		assertTrue("Not a qualified name", name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		IBinding binding = qualifiedName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertEquals("wrong qualified name", "test0139a.C", typeBinding.getQualifiedName());
		SimpleName simpleName = qualifiedName.getName();
		binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.TYPE, binding.getKind());
		typeBinding = (ITypeBinding) binding;
		assertEquals("wrong qualified name", "test0139a.C", typeBinding.getQualifiedName());
		name = qualifiedName.getQualifier();
		assertEquals("Not a simpleName", ASTNode.SIMPLE_NAME, name.getNodeType());
		binding = name.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("wrong type", IBinding.PACKAGE, binding.getKind());
		IPackageBinding packageBinding = (IPackageBinding) binding;
		assertEquals("wrong name", "test0139a", packageBinding.getName());
		assertEquals("Wrong modifier", Modifier.NONE, packageBinding.getModifiers());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85115
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85215
	 */
	public void test0140() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0140", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertNotNull("No node", node);
		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List modifiers = enumDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Wrong type", modifier instanceof MarkerAnnotation);
		MarkerAnnotation markerAnnotation = (MarkerAnnotation) modifier;
		ITypeBinding typeBinding = markerAnnotation.resolveTypeBinding();
		assertTrue("Not an annotation", typeBinding.isAnnotation());
		assertTrue("Not a top level type", typeBinding.isTopLevel());

		sourceUnit = getCompilationUnit("Converter15" , "src", "test0140", "Annot.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		result = runJLS4Conversion(sourceUnit, true, false);
		assertNotNull(result);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertNotNull("No node", node);
		assertEquals("Not an annotation declaration", ASTNode.ANNOTATION_TYPE_DECLARATION, node.getNodeType());
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) node;
		modifiers = annotationTypeDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		typeBinding = annotationTypeDeclaration.resolveBinding();
		int modifierValue = typeBinding.getModifiers();
		assertEquals("Type is not public", Modifier.PUBLIC, modifierValue);
	}

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83100
	public void test0141() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
    		"public class X<T> {\n" +
    		"	int x;\n" +
 			"	public static void main(String[] args) {\n" +
			"		System.out.println(new X<String>().x);\n" +
			"	}\n" +
    		"}";
    	ASTNode node = buildAST(
			contents,
			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertFalse("Not a parameter", variableBinding.isParameter());
		node = getASTNode(compilationUnit, 0, 1, 0);
    	assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
    	assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		Expression expression2 = (Expression) arguments.get(0);
    	assertEquals("Not a field access", ASTNode.FIELD_ACCESS, expression2.getNodeType());
		FieldAccess fieldAccess = (FieldAccess) expression2;
		IVariableBinding variableBinding2 = fieldAccess.resolveFieldBinding();
		assertFalse("Not a parameter", variableBinding2.isParameter());
		assertFalse("Bindings are not equals", variableBinding.isEqualTo(variableBinding2));
		IVariableBinding variableBinding3 = variableBinding2.getVariableDeclaration();
		assertTrue("Bindings are equals", variableBinding.isEqualTo(variableBinding3));
		node = compilationUnit.findDeclaringNode(variableBinding2);
		assertNotNull("No declaring node", node);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83100
	public void test0142() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
    		"public class X<T> {\n" +
 			"	public static void main(String[] args) {\n" +
   			"		int x = 0;\n" +
 			"		System.out.println(x);\n" +
			"	}\n" +
    		"}";
    	ASTNode node = buildAST(
			contents,
			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		assertEquals("Wrong name", "x", fragment.getName().getIdentifier());
		IVariableBinding variableBinding = fragment.resolveBinding();
		IVariableBinding variableBinding2 = variableBinding.getVariableDeclaration();
		assertFalse("Not a parameter", variableBinding.isParameter());
		assertFalse("Not a parameter", variableBinding2.isParameter());
		assertTrue("Bindings are equals", variableBinding.isEqualTo(variableBinding2));
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84140
    public void test0143() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
    		"	public void bar(String[]... args){\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    	MethodDeclaration methodDeclaration = (MethodDeclaration) node;
    	List parameters = methodDeclaration.parameters();
    	assertEquals("Wrong size", 1, parameters.size());
    	SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
    	assertTrue("Not a var args", singleVariableDeclaration.isVarargs());
    	Type type = singleVariableDeclaration.getType();
    	checkSourceRange(type, "String[]", contents);
    	assertTrue("Not an array type", type.isArrayType());
    	ITypeBinding typeBinding = type.resolveBinding();
    	assertNotNull("No binding", typeBinding);
    	assertTrue("Not an array", typeBinding.isArray());
    	assertEquals("wrong dimensions", 1, typeBinding.getDimensions());
    	ArrayType arrayType = (ArrayType) type;
    	assertEquals("Wrong dimension", 1, arrayType.getDimensions());
    	type = componentType(arrayType);
    	assertTrue("Not a simple type", type.isSimpleType());
    	checkSourceRange(type, "String", contents);
    	assertEquals("Wrong extra dimension", 0, singleVariableDeclaration.getExtraDimensions());
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		ITypeBinding parameterType = parameterTypes[0];
    	assertTrue("Not an array binding", parameterType.isArray());
    	assertEquals("wrong dimension", 2, parameterType.getDimensions());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=87171
    public void test0144() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X<T> {\n" +
    		"	void foo(T t) {\n" +
    		"		System.out.println(t);\n" +
    		"	}\n" +
    		"}\n" +
    		"\n" +
    		"class Use {\n" +
    		"	public static void main(String[] args) {\n" +
    		"		X<String> i= new X<String>();\n" +
    		"		i.foo(\"Eclipse\");\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 1, 0, 1);
    	assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
    	assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		node = compilationUnit.findDeclaringNode(methodBinding);
		assertNotNull("No declaring node", node);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=87350
    public void test0145() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public enum X {\n" +
    		"    RED, GREEN(), BLUE(17), PINK(1) {/*anon*};\n" +
    		"    Color() {}\n" +
    		"    Color(int i) {}\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
		String expectedErrors = "The constructor X(int) is undefined\n" +
			"The constructor X(int) is undefined\n" +
			"Unexpected end of comment";
    	assertProblemsSize(compilationUnit, 3, expectedErrors);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=87481
    public void test0146() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"import java.util.Iterator;\n" +
    		"public class X {\n" +
    		"    void doit() {\n" +
    		"			Iterator iter= (Iterator) null;\n" +
    		"			System.out.println(iter);\n" +
    		"    }\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	String expectedProblems = "";
    	assertProblemsSize(compilationUnit, 0, expectedProblems);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertNotNull("No initializer", expression);
		assertEquals("Not a cast expression", ASTNode.CAST_EXPRESSION, expression.getNodeType());
		CastExpression castExpression = (CastExpression) expression;
		Type type = castExpression.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Wrong type", "java.util.Iterator", typeBinding.getQualifiedName());
		assertTrue("Not a raw type", typeBinding.isRawType());
		assertFalse("Is a generic type", typeBinding.isGenericType());
		assertFalse("Is a parameterized type", typeBinding.isParameterizedType());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=87498
    public void test0147() throws CoreException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0147", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=87350
    public void test0148() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public enum X {\n" +
    		"    RED, GREEN(), BLUE(17), PINK(1) {/*anon*};\n" +
    		"    Color() {}\n" +
    		"    Color(int i) {}\n";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
		String expectedErrors = "The constructor X(int) is undefined\n" +
			"The constructor X(int) is undefined\n" +
			"Unexpected end of comment";
    	assertProblemsSize(compilationUnit, 3, expectedErrors);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88252
    public void test0149() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		" interface Jpf {\n" +
    		" 	@interface Action {\n" +
    		" 		ValidatableProperty[] validatableProperties();\n" +
    		" 	}\n" +
    		" 	\n" +
    		" 	@interface ValidatableProperty {\n" +
    		" 		String propertyName();\n" +
    		" 		 ValidationLocaleRules[] localeRules();\n" +
    		" 	}\n" +
    		" 	\n" +
    		" 	@interface ValidationLocaleRules {\n" +
    		" 		  ValidateMinLength validateMinLength();\n" +
    		" 	}\n" +
    		" 	\n" +
    		" 	@interface ValidateMinLength {\n" +
    		" 		String chars();\n" +
    		" 	}\n" +
    		"}\n" +
    		" \n" +
    		" public class X {\n" +
    		" \n" +
    		" @Jpf.Action(\n" +
    		"      validatableProperties={@Jpf.ValidatableProperty(propertyName=\"fooField\",\n" +
    		"        localeRules={@Jpf.ValidationLocaleRules(\n" +
    		"            validateMinLength=@Jpf.ValidateMinLength(chars=\"12\")\n" +
    		"        )}\n" +
    		"      )}\n" +
    		"    )\n" +
    		"    public String actionForValidationRuleTest()    {\n" +
    		"        return null;\n" +
    		"    }\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1, 0);
   		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
  		assertTrue("Not a normal annotation", modifier instanceof NormalAnnotation);
		NormalAnnotation annotation = (NormalAnnotation) modifier;
		List values = annotation.values();
		assertEquals("wrong size", 1, values.size());
		MemberValuePair memberValuePair = (MemberValuePair) values.get(0);
		Expression expression = memberValuePair.getValue();
   		assertEquals("Not an array initializer", ASTNode.ARRAY_INITIALIZER, expression.getNodeType());
		ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
		List expressions = arrayInitializer.expressions();
		assertEquals("wrong size", 1, expressions.size());
		Expression expression2 = (Expression) expressions.get(0);
  		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, expression2.getNodeType());
		NormalAnnotation annotation2 = (NormalAnnotation) expression2;
		values = annotation2.values();
		assertEquals("wrong size", 2, values.size());
		MemberValuePair memberValuePair2 = (MemberValuePair) values.get(1);
		Expression expression3 = memberValuePair2.getValue();
   		assertEquals("Not an array initializer", ASTNode.ARRAY_INITIALIZER, expression3.getNodeType());
		arrayInitializer = (ArrayInitializer) expression3;
		expressions = arrayInitializer.expressions();
		assertEquals("wrong size", 1, expressions.size());
		Expression expression4 = (Expression) expressions.get(0);
   		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, expression4.getNodeType());
		NormalAnnotation annotation3 = (NormalAnnotation) expression4;
		values = annotation3.values();
		assertEquals("wrong size", 1, values.size());
		MemberValuePair memberValuePair3 = (MemberValuePair) values.get(0);
		Expression expression5 = memberValuePair3.getValue();
   		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, expression5.getNodeType());
		NormalAnnotation annotation4 = (NormalAnnotation) expression5;
		checkSourceRange(annotation4, "@Jpf.ValidateMinLength(chars=\"12\")", contents);
		checkSourceRange(memberValuePair3, "validateMinLength=@Jpf.ValidateMinLength(chars=\"12\")", contents);
   }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88224
    public void test0150() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
    		"	void foo() {\n" +
    		"		class Local {\n" +
    		"			static enum E {\n" +
    		"				C, B;\n" +
    		"			}\n" +
    		"		}\n" +
    		"	}\n" +
    		"	void bar() {\n" +
    		"	}\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
        final String expectedErrors = "The member enum E can only be defined inside a top-level class or interface or in a static context";
    	assertProblemsSize(compilationUnit, 1, expectedErrors);
		node = getASTNode(compilationUnit, 0, 0, 0);
   		assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) node;
		AbstractTypeDeclaration typeDeclaration = typeDeclarationStatement.getDeclaration();
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
   		assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, bodyDeclaration.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) bodyDeclaration;
		List enumConstants = enumDeclaration.enumConstants();
		assertEquals("Wrong size", 2, enumConstants.size());
		EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(0);
		checkSourceRange(enumConstantDeclaration, "C", contents);
		enumConstantDeclaration = (EnumConstantDeclaration) enumConstants.get(1);
		checkSourceRange(enumConstantDeclaration, "B", contents);
   }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88548
    public void _2551_test0151() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
	   		"public enum X {\n" +
    		"	RED, GREEN(), BLUE(17);\n" +
    		"	X() {}\n" +
    		"	X(int i) {}\n" +
    		"	public static void main(String[] args) {\n" +
    		"		for (X x : X.values()) {\n" +
    		"			switch(x) {\n" +
    		"				case RED :\n" +
    		"					System.out.println(\"ROUGE\");\n" +
    		"					break;\n" +
    		"				case GREEN :\n" +
    		"					System.out.println(\"VERT\");\n" +
    		"					break;\n" +
    		"				case BLUE :\n" +
    		"					System.out.println(\"BLEU\");\n" +
    		"					break;\n" +
    		"			}\n" +
    		"		}\n" +
    		"   }\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
    	assertEquals("Not an enum declaration", ASTNode.ENUM_DECLARATION, node.getNodeType());
		EnumDeclaration enumDeclaration = (EnumDeclaration) node;
		List bodyDeclarations = enumDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 3, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(2);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertNotNull("No body", block);
		List statements = block.statements();
		assertEquals("Wrong size", 1, statements.size());
		Statement statement = (Statement) statements.get(0);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, statement.getNodeType());
		EnhancedForStatement forStatement = (EnhancedForStatement) statement;
		Statement statement2 = forStatement.getBody();
    	assertEquals("Not a block", ASTNode.BLOCK, statement2.getNodeType());
		Block block2 = (Block) statement2;
		statements = block2.statements();
		assertEquals("Wrong size", 1, statements.size());
		statement = (Statement) statements.get(0);
    	assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, statement.getNodeType());
		SwitchStatement switchStatement = (SwitchStatement) statement;
		statements = switchStatement.statements();
		assertEquals("Wrong size", 9, statements.size());
		statement = (Statement) statements.get(0);
    	assertEquals("Not a switch case statement", ASTNode.SWITCH_CASE, statement.getNodeType());
		SwitchCase switchCase = (SwitchCase) statement;
		@SuppressWarnings("deprecation")
		Expression expression = switchCase.getExpression();
		assertNull("Got a constant", expression.resolveConstantExpressionValue());
   }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88548
    @SuppressWarnings("deprecation")
	public void test0152() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
	   		"public class X {\n" +
	   		"	public static final int CONST1 = 1;\n" +
	   		"	public static final int CONST2 = 2;\n" +
    		"	public static void main(String[] args) {\n" +
    		"		int[] intTab = new int[] {2, 3};\n" +
    		"		for (int i : intTab) {\n" +
    		"			switch(i) {\n" +
    		"				case CONST1 :\n" +
    		"					System.out.println(\"1\");\n" +
    		"					break;\n" +
    		"				case CONST2 :\n" +
    		"					System.out.println(\"2\");\n" +
    		"					break;\n" +
    		"				default :\n" +
    		"					System.out.println(\"default\");\n" +
    		"					break;\n" +
    		"			}\n" +
    		"		}\n" +
    		"   }\n" +
    		"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
    	assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 3, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(2);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertNotNull("No body", block);
		List statements = block.statements();
		assertEquals("Wrong size", 2, statements.size());
		Statement statement = (Statement) statements.get(1);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, statement.getNodeType());
		EnhancedForStatement forStatement = (EnhancedForStatement) statement;
		Statement statement2 = forStatement.getBody();
    	assertEquals("Not a block", ASTNode.BLOCK, statement2.getNodeType());
		Block block2 = (Block) statement2;
		statements = block2.statements();
		assertEquals("Wrong size", 1, statements.size());
		statement = (Statement) statements.get(0);
    	assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, statement.getNodeType());
		SwitchStatement switchStatement = (SwitchStatement) statement;
		statements = switchStatement.statements();
		assertEquals("Wrong size", 9, statements.size());
		statement = (Statement) statements.get(0);
    	assertEquals("Not a switch case statement", ASTNode.SWITCH_CASE, statement.getNodeType());
		SwitchCase switchCase = (SwitchCase) statement;
		Expression expression = switchCase.getExpression();
		Object constant = expression.resolveConstantExpressionValue();
		assertNotNull("No constant", constant);
		assertEquals("Wrong value", "1", String.valueOf(constant));
   }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88841
    public void test0153() throws CoreException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0153", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0154() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(/*start*/1/*end*/);\n" +
				"	}\n" +
				"	public static void test(Integer i) {}\n" +
				"}";
    	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, node.getNodeType());
		NumberLiteral literal = (NumberLiteral) node;
		assertTrue("Not boxed", literal.resolveBoxing());
		assertFalse("Is unboxed", literal.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0155() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static int bar() {return 1;}\n" +
				"	public static void main(String[] s) {\n" +
				"		test(/*start*/bar()/*end*/);\n" +
				"	}\n" +
				"	public static void test(Integer i) {}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, node.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) node;
		assertTrue("Not boxed", methodInvocation.resolveBoxing());
		assertFalse("Is unboxed", methodInvocation.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0156() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(/*start*/new Integer(1)/*end*/);\n" +
				"	}\n" +
				"	public static void test(int i) {}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, node.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
		assertFalse("Is boxed", classInstanceCreation.resolveBoxing());
		assertTrue("Not unboxed", classInstanceCreation.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88548
    public void test0157() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(/*start*/null/*end*/);\n" +
				"	}\n" +
				"	public static void test(Object o) {}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a null literal", ASTNode.NULL_LITERAL, node.getNodeType());
		NullLiteral nullLiteral = (NullLiteral) node;
		assertNull("Got a constant", nullLiteral.resolveConstantExpressionValue());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88548
    public void test0158() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	private static final String CONST = \"Hello World\";\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.println(/*start*/CONST/*end*/);\n" +
				"	}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, node.getNodeType());
		SimpleName name = (SimpleName) node;
		assertNotNull("No constant", name.resolveConstantExpressionValue());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0159() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		test(/*start*/new Integer(1)/*end*/);\n" +
				"	}\n" +
				"	public static void test(Integer i) {}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, node.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
		assertFalse("Is boxed", classInstanceCreation.resolveBoxing());
		assertFalse("Is unboxed", classInstanceCreation.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0160() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		Y.test(1, new Integer(2), -3);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static void test(int ... i) {}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
    	assertEquals("Not method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 3, arguments.size());
		Expression argument = (Expression) arguments.get(0);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertFalse("Is unboxed", argument.resolveUnboxing());
		argument = (Expression) arguments.get(1);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertTrue("Not unboxed", argument.resolveUnboxing());
		argument = (Expression) arguments.get(2);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertFalse("Is unboxed", argument.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0161() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		new Y().test(new Integer(1), 1);\n" +
				"		new Y().test(1, new Integer(1));\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	void test(Integer i, int j) { System.out.print(1); }\n" +
				"	void test(int i, Integer j) { System.out.print(2); }\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement expressionStatement = (ExpressionStatement) node;
		Expression expression = expressionStatement.getExpression();
    	assertEquals("Not method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 2, arguments.size());
		Expression argument = (Expression) arguments.get(0);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertFalse("Is unboxed", argument.resolveUnboxing());
		argument = (Expression) arguments.get(1);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertFalse("Is unboxed", argument.resolveUnboxing());
		getASTNode(compilationUnit, 0, 0, 1);
    	assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		expressionStatement = (ExpressionStatement) node;
		expression = expressionStatement.getExpression();
    	assertEquals("Not method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		methodInvocation = (MethodInvocation) expression;
		arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 2, arguments.size());
		argument = (Expression) arguments.get(0);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertFalse("Is unboxed", argument.resolveUnboxing());
		argument = (Expression) arguments.get(1);
		assertFalse("Is boxed", argument.resolveBoxing());
		assertFalse("Is unboxed", argument.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87173
    public void test0162() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		int i = Y.test();\n" +
				"		System.out.print(i);\n" +
				"	}\n" +
				"}\n" +
				"class Y {\n" +
				"	public static Byte test() { return new Byte((byte) 1); }\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertFalse("Is boxed", expression.resolveBoxing());
		assertTrue("Not unboxed", expression.resolveUnboxing());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86580
    public void test0163() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"public class X<T>{\n" +
				"  void f(T t){}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertTrue("Not from source", typeBinding.isFromSource());
		final ITypeBinding declaringClass = typeBinding.getDeclaringClass();
		assertNotNull("No declaring class", declaringClass);
		assertTrue("Not a generic class", declaringClass.isGenericType());
		assertEquals("Wrong name", "X", declaringClass.getName());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86580
    public void test0164() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"class X {\n" +
				"  <U> void foo(U u) {}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("Wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		Type type = singleVariableDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertTrue("Not from source", typeBinding.isFromSource());
		final IMethodBinding methodBinding = typeBinding.getDeclaringMethod();
		assertNotNull("No declaring method", methodBinding);
		assertEquals("Wrong name", "foo", methodBinding.getName());
		assertTrue("Not a generic method", methodBinding.isGenericMethod());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86580
    public void test0165() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"class X {\n" +
				"   <U> void foo(U u) {\n" +
				"		class C {}\n" +
				"	}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
		TypeDeclarationStatement statement = (TypeDeclarationStatement) node;
		AbstractTypeDeclaration typeDeclaration = statement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a local type", typeBinding.isLocal());
		ITypeBinding declaringClass = typeBinding.getDeclaringClass();
		assertNotNull("No declaring class", declaringClass);
		IMethodBinding declaringMethod = typeBinding.getDeclaringMethod();
		assertNotNull("No declaring method", declaringMethod);
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86580
    public void test0166() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"class X {\n" +
				"   {\n" +
				"		class C {}\n" +
				"	}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
    	assertNotNull("No node", node);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not a type declaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
		TypeDeclarationStatement statement = (TypeDeclarationStatement) node;
		AbstractTypeDeclaration typeDeclaration = statement.getDeclaration();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a local type", typeBinding.isLocal());
		ITypeBinding declaringClass = typeBinding.getDeclaringClass();
		assertNotNull("No declaring class", declaringClass);
		IMethodBinding declaringMethod = typeBinding.getDeclaringMethod();
		assertNull("No declaring method", declaringMethod);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88841
    public void test0167() throws CoreException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0167", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 1, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 4, parameters.size());
		SingleVariableDeclaration param = (SingleVariableDeclaration)parameters.get(3);
		Type t = param.getType();
		String typeName = ((SimpleType)t).getName().getFullyQualifiedName();

		IType[] types = sourceUnit.getTypes();
		assertEquals("wrong size", 2, types.length);
		IType mainType = types[1];
		String[][] typeMatches = mainType.resolveType( typeName );
		assertNotNull(typeMatches);
		assertEquals("wrong size", 1, typeMatches.length);
		String[] typesNames = typeMatches[0];
		assertEquals("wrong size", 2, typesNames.length);
		assertEquals("Wrong part 1", "java.lang", typesNames[0]);
		assertEquals("Wrong part 2", "Object", typesNames[1]);
    }

	public void test0168() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		final String contents =
				"import java.util.List;\n" +
				"public class X {\n" +
				"	void f() {\n" +
				"		List<?> list = null;\n" +
				"		System.out.println(list);\n" +
				"    }\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List arguments = methodInvocation.arguments();
		assertEquals("Wrong size", 1, arguments.size());
		Expression argument = (Expression) arguments.get(0);
		ITypeBinding typeBinding = argument.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
		assertTrue("Not a parameterized binding", typeBinding.isParameterizedType());
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		assertEquals("Wrong size", 1, typeArguments.length);
		final ITypeBinding typeBinding2 = typeArguments[0];
		assertTrue("Not a capture binding", typeBinding2.isCapture());
		assertTrue("Not from source", typeBinding2.isFromSource());
		assertNotNull("No wildcard", typeBinding2.getWildcard());
	}

	public void test0169() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
				"public class X {\n" +
				"    static class BB<T, S> { }\n" +
				"    static class BD<T> extends BB<T, T> { }\n" +
				"    void f() {\n" +
				"        BB<? extends Number, ? super Integer> bb = null;\n" +
				"        Object o = (BD<Number>) bb;\n" +
				"    }\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 1, "Type safety: Unchecked cast from X.BB<capture#1-of ? extends Number,capture#2-of ? super Integer> to X.BD<Number>");
		node = getASTNode(compilationUnit, 0, 2, 1);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a cast expression", ASTNode.CAST_EXPRESSION, expression.getNodeType());
		CastExpression castExpression = (CastExpression) expression;
		Expression expression2 = castExpression.getExpression();
		ITypeBinding typeBinding = expression2.resolveTypeBinding();
		assertTrue("Not a parameterized type", typeBinding.isParameterizedType());
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		assertEquals("Wrong size", 2, typeArguments.length);
		final ITypeBinding typeBinding2 = typeArguments[0];
		assertTrue("Not a capture binding", typeBinding2.isCapture());
		ITypeBinding wildcardBinding = typeBinding2.getWildcard();
		assertNotNull("No wildcard binding", wildcardBinding);
		assertTrue("Not from source", typeBinding2.isFromSource());
		assertTrue("Not a wildcard", wildcardBinding.isWildcardType());
	}

	public void test0170() throws CoreException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
				"public class X {\n" +
				"    static class BB<T, S> { }\n" +
				"    static class BD<T> extends BB<T, T> { }\n" +
				"    static BB<? extends Number, ? super Integer> bb = null;\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(/*start*/X.bb/*end*/);\n" +
				"    }\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertNotNull("No node", node);
	   	assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, node.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) node;
		ITypeBinding typeBinding = qualifiedName.resolveTypeBinding();
		assertTrue("Not a parameterized type", typeBinding.isParameterizedType());
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		assertEquals("Wrong size", 2, typeArguments.length);
		final ITypeBinding typeBinding2 = typeArguments[0];
		assertTrue("Not a capture binding", typeBinding2.isCapture());
		ITypeBinding wildcardBinding = typeBinding2.getWildcard();
		assertNotNull("No wildcard binding", wildcardBinding);
		assertTrue("Not from source", typeBinding2.isFromSource());
		assertTrue("Not a wildcard", wildcardBinding.isWildcardType());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92361
	 */
	public void test0171() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"public class X {\n" +
				"\n" +
				"    java.util.List<? extends Runnable> list;\n" +
				"    Object o= /*start*/list/*end*/;\n" +
				"}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, node.getNodeType());
		ITypeBinding type = ((SimpleName)node).resolveTypeBinding();
		assertNull("Unexpected element", type.getTypeArguments()[0].getJavaElement());
	}

	/*
	 * Ensures that 2 different capture bindings are not "isEqualTo(...)".
	 * (regression test for bug 92888 ITypeBinding#isEqualTo(..) is wrong for capture bindings)
	 */
	public void test0172() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X<T> {\n" +
			"  private static X<? super Number> num() {\n" +
			"		return null;\n" +
			"	}\n" +
			"  void add(T t) {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Number n= null;\n" +
			"    /*start1*/num().add(null)/*end1*/;\n" +
			"    /*start2*/num().add(n)/*end2*/;\n" +
			"  }\n" +
			"}\n";
	   	IBinding[] bindings = resolveBindings(contents, this.workingCopy);
	   	assertTrue("2 different capture bindings should not be equals", !bindings[0].isEqualTo(bindings[1]));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93093
	 */
	public void test0173() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"import java.util.Vector;\n" +
				"\n" +
				"@SuppressWarnings(\"null\")\n" +
				"public class X {\n" +
				"	void test1() {\n" +
				"		Vector<? extends Number[]> v = null;\n" +
				"		 /*start*/v.get(0)/*end*/;\n" +
				"	}\n" +
				"}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
		ITypeBinding type = ((Expression)node).resolveTypeBinding();
		assertTrue("Should be one bound", type.getTypeBounds().length == 1);
		assertEquals("Invalid bound", "[Ljava.lang.Number;", type.getTypeBounds()[0].getBinaryName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92982
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=88202
	 */
	public void test0174() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"import java.util.*;\n" +
				"\n" +
				"public class X {\n" +
				"	void test1() {\n" +
				"		List<? extends Collection> l = null;\n" +
				"		 /*start*/l.get(0)/*end*/;\n" +
				"	}\n" +
				"}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		ITypeBinding type = ((Expression)node).resolveTypeBinding();
		assertTrue("Should be one bound", type.getTypeBounds().length == 1);
		assertEquals("Invalid bound", "java.util.Collection", type.getTypeBounds()[0].getBinaryName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=88202
	 */
	public void test0175() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"import java.util.*;\n" +
				"\n" +
				"@SuppressWarnings(\"null\")\n" +
				"public class X {\n" +
				"	void test1() {\n" +
				"		List<?> l = null;\n" +
				"		 /*start*/l.get(0)/*end*/;\n" +
				"	}\n" +
				"}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
		ITypeBinding type = ((Expression)node).resolveTypeBinding();
		assertTrue("Should be no bound", type.getTypeBounds().length == 0);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92982
	 */
	public void test0176() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
				"import java.util.*;\n" +
				"\n" +
				"public class X<T extends Collection> {\n" +
				"	void test1() {\n" +
				"		List<T> l = null;\n" +
				"		 /*start*/l.get(0)/*end*/;\n" +
				"	}\n" +
				"}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		ITypeBinding type = ((Expression)node).resolveTypeBinding();
		assertTrue("Should be one bound", type.getTypeBounds().length == 1);
		assertEquals("Invalid bound", "java.util.Collection", type.getTypeBounds()[0].getBinaryName());
	}

	/*
	 * Ensure that the declaring class of a capture binding is correct
	 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=93275)
	 */
    public void test0177() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"public class X<T> {\n" +
				"    Object foo(X<?> list) {\n" +
				"       return /*start*/list.get()/*end*/;\n" +
				"    }\n" +
				"    T get() {\n" +
				"    	return null;\n" +
				"    }\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
		MethodInvocation methodInvocation = (MethodInvocation) node;
		ITypeBinding capture = methodInvocation.resolveTypeBinding();
		ITypeBinding declaringClass = capture.getDeclaringClass();
		assertBindingEquals("LX<TT;>;", declaringClass);
    }

   	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93075
	 */
    public void test0178() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"import java.util.Vector;\n" +
				"\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		Vector< ? super java.util.Collection<? super java.lang.Number> > lhs= null;		\n" +
				"	}\n" +
				"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
	   	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
	   	CompilationUnit unit = (CompilationUnit) node;
	   	node = getASTNode(unit, 0, 0, 0);
	   	assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
	   	VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
	   	Type type = statement.getType();
	   	checkSourceRange(type, "Vector< ? super java.util.Collection<? super java.lang.Number> >", contents);
	   	assertEquals("Not a parameterized type", ASTNode.PARAMETERIZED_TYPE, type.getNodeType());
	   	ParameterizedType parameterizedType = (ParameterizedType) type;
	   	List typeArguments = parameterizedType.typeArguments();
	   	assertEquals("Wrong size", 1, typeArguments.size());
	   	Type typeArgument = (Type) typeArguments.get(0);
	   	assertEquals("Not a wildcard type", ASTNode.WILDCARD_TYPE, typeArgument.getNodeType());
	   	WildcardType wildcardType = (WildcardType) typeArgument;
	   	checkSourceRange(wildcardType, "? super java.util.Collection<? super java.lang.Number>", contents);
	   	Type bound = wildcardType.getBound();
	   	assertEquals("Not a parameterized type", ASTNode.PARAMETERIZED_TYPE, bound.getNodeType());
	   	ParameterizedType parameterizedType2 = (ParameterizedType) bound;
	   	checkSourceRange(bound, "java.util.Collection<? super java.lang.Number>", contents);
	   	typeArguments = parameterizedType2.typeArguments();
	   	assertEquals("Wrong size", 1, typeArguments.size());
	   	typeArgument = (Type) typeArguments.get(0);
	   	assertEquals("Not a wildcard type", ASTNode.WILDCARD_TYPE, typeArgument.getNodeType());
	   	wildcardType = (WildcardType) typeArgument;
	   	checkSourceRange(wildcardType, "? super java.lang.Number", contents);
    }

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93075
	 */
    public void test0179() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"@interface Test {}\n" +
				"public enum X\n" +
				"{\n" +
				"     /*start*/@Test HISTORY/*end*/\n" +
				"}";
	   	ASTNode node = buildAST(
			contents,
    		this.workingCopy);
	   	assertEquals("Not an enum constant declaration", ASTNode.ENUM_CONSTANT_DECLARATION, node.getNodeType());
		EnumConstantDeclaration constantDeclaration = (EnumConstantDeclaration) node;
		List modifiers = constantDeclaration.modifiers();
		assertEquals("Wrong size", 1, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
	   	assertTrue("Not a marker annotation", modifier instanceof MarkerAnnotation);
    }

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92360
	 */
    public void test0180() throws CoreException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
   		String contents =
				"import java.util.List;\n" +
				"public class X {\n" +
				"    List</*start*/? extends Runnable/*end*/> list;\n" +
				"}";
	   	ASTNode node = buildAST(
			contents,
    		this.workingCopy);
	   	assertEquals("Not a wildcard type", ASTNode.WILDCARD_TYPE, node.getNodeType());
		WildcardType wildcardType = (WildcardType) node;
		ITypeBinding typeBinding = wildcardType.resolveBinding();
		assertTrue("Not a wildcard type", typeBinding.isWildcardType());
		assertFalse("Not an class", typeBinding.isClass());
    }

	/*
	 * Ensures that 2 different parameterized type bindings are not "isEqualTo(...)".
	 * (regression test for bug 93408 ITypeBinding#isEqualTo(..) does not resolve type variables)
	 */
	public void test0181() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X<E> {\n" +
			"	/*start1*/Y<E>/*end1*/ y;\n" +
			"	static class Other<E> {\n" +
			"		/*start2*/Y<E>/*end2*/ y;\n" +
			"	}\n" +
			"}\n" +
			"class Y<E> {\n" +
			"}";
	   	IBinding[] bindings = resolveBindings(contents, this.workingCopy);
	   	assertTrue("2 different parameterized type bindings should not be equals", !bindings[0].isEqualTo(bindings[1]));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=95911
	 */
	public void test0182() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"import java.util.Map;\n" +
			"\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		Map<String, Number> map= new Map<String, Number>() {\n" +
			"		};\n" +
			"	}\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	node = getASTNode(compilationUnit, 0, 0, 0);
    	assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
    	VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
    	List fragments = statement.fragments();
    	assertEquals("Wrong size", 1, fragments.size());
    	VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
    	String expectedSource = "map= new Map<String, Number>() {\n" +
			"		}";
    	checkSourceRange(fragment, expectedSource, contents);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=95911
	 */
	public void test0183() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"import java.util.Map;\n" +
			"\n" +
			"public class X {\n" +
			"	Map<String, Number> map= new Map<String, Number>() {\n" +
			"	};\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
    	FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
    	List fragments = fieldDeclaration.fragments();
    	assertEquals("Wrong size", 1, fragments.size());
    	VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
    	String expectedSource = "map= new Map<String, Number>() {\n" +
			"	}";
    	checkSourceRange(fragment, expectedSource, contents);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=97841
	 */
	public void test0184() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"public class X {\n" +
			"	java.util.Map<String, Number> map= new java.util.Map<String, Number>() {\n" +
			"	};\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
    	FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
    	List fragments = fieldDeclaration.fragments();
    	assertEquals("Wrong size", 1, fragments.size());
    	VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
    	Expression initializer = fragment.getInitializer();
    	assertNotNull("No initializer", initializer);
    	ITypeBinding binding = initializer.resolveTypeBinding();
    	assertNotNull("No binding", binding);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98086
	 */
	public void test0185() throws JavaModelException {
		final ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0185", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals("Wrong setting", JavaCore.WARNING, sourceUnit.getJavaProject().getOption(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, true));
		final ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		final CompilationUnit compilationUnit = (CompilationUnit) result;
	   	assertProblemsSize(compilationUnit, 1, "At least one of the problems in category 'rawtypes' is not analysed due to a compiler option being ignored");
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98086
	 */
	public void test0186() throws JavaModelException {
		final ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0186", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals("Wrong setting", JavaCore.WARNING, sourceUnit.getJavaProject().getOption(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, true));
		final ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		final CompilationUnit compilationUnit = (CompilationUnit) result;
	   	assertProblemsSize(compilationUnit, 1, "Type safety: The expression of type ArrayList needs unchecked conversion to conform to List<String>");
	}

	/*
	 * Ensures that the binding key of a parameterized type can be computed when it contains a reference to a type variable.
	 * (regression test for bug 98259 NPE computing ITypeBinding#getKey())
	 */
	public void test0187() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"public class X {\n" +
			"	<T> /*start*/Y<T>/*end*/ foo() {\n" +
			"      return null;" +
			"	};\n" +
			"}\n" +
			"class Y<E> {\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	ParameterizedType type = (ParameterizedType) node;
    	assertBindingEquals(
    		"LX~Y<LX;:1TT;>;",
    		type.resolveBinding());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98473
	 */
	public void test0188() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	class Counter<T, /*start*/L extends List<T>/*end*/> {\n" +
			"		private L _attribute;\n" +
			"	}\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a type parameter", ASTNode.TYPE_PARAMETER, node.getNodeType());
    	ITypeBinding typeBinding = ((TypeParameter) node).resolveBinding();
    	assertNotNull("No binding", typeBinding);
    	assertFalse("Cannot be top level", typeBinding.isTopLevel());
    	assertFalse("A class", typeBinding.isClass());
    	assertFalse("An interface", typeBinding.isInterface());
    	assertTrue("Not a type variable", typeBinding.isTypeVariable());
		assertTrue("Not from source", typeBinding.isFromSource());
	}

	public void test0189() throws CoreException, IOException {
		try {
			IJavaProject project = createJavaProject("P1", new String[] {""}, new String[] {"CONVERTER_JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
			addLibrary(project, "lib.jar", "src.zip", new String[] {
				"/P1/p/I1.java",
				"package p;\n" +
				"public class I1<E> {\n" +
				"}",
				"/P1/p/I2.java",
				"package p;\n" +
				"public interface I2<K, V> {\n" +
				"	interface I3<K,V> {}\n" +
				"	I1<I2.I3<K, V>> foo();\n" +
				"}",
				"/P1/p/X.java",
				"package p;\n" +
				"public class X<K,V>  implements I2<K,V> {\n" +
				"	public I1<I2.I3<K,V>> foo() {\n" +
				"		return null;\n" +
				"	}	\n" +
				"}"
			}, CompilerOptions.getFirstSupportedJavaVersion());
			this.workingCopy = getWorkingCopy("/P1/p1/Y.java", true/*resolve*/);
			ASTNode node = buildAST(
				"package p1;\n" +
				"import p.*;\n" +
				"public abstract class Y implements I2 {\n" +
				"	public I1 foo() {\n" +
				"		return /*start*/bar().foo()/*end*/;\n" +
				"	}\n" +
				"	private X bar() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
				this.workingCopy,
				false);
			MethodInvocation method = (MethodInvocation) node;
			IMethodBinding methodBinding = method.resolveMethodBinding();
			assertBindingEquals(
				"Lp/X;.foo()Lp/I1<Lp/I2$I3<TK;TV;>;>;",
				methodBinding.getMethodDeclaration());
		} finally {
			deleteProject("P1");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99355
	public void test0190() throws CoreException, IOException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	final String contents =
			"class Container<T> {\n" +
			"	private final T m_t;\n" +
			"\n" +
			"	public Container(T t) {\n" +
			"		m_t = t;\n" +
			"	}\n" +
			"\n" +
			"	T get() {\n" +
			"		return m_t;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class GenericContainer {\n" +
			"	private final Container<?> m_c;\n" +
			"\n" +
			"	public GenericContainer(Container<?> c) {\n" +
			"		m_c = c;\n" +
			"	}\n" +
			"\n" +
			"	public Container<?> getC() {\n" +
			"		return m_c;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"	GenericContainer createContainer() {\n" +
			"		final Container<String> innerContainer = new Container<String>(\"hello\");\n" +
			"		final Container<Container<String>> outerContainer = new Container<Container<String>>(\n" +
			"				innerContainer);\n" +
			"		return new GenericContainer(outerContainer);\n" +
			"	}\n" +
			"\n" +
			"	void method() {\n" +
			"		final GenericContainer createContainer = createContainer();\n" +
			"		/*start*/@SuppressWarnings(\"unchecked\")\n" +
			"		final Container<Container<String>> c = (Container<Container<String>>) createContainer.getC();/*end*/\n" +
			"		final Container<String> container = c.get();\n" +
			"		final String string = container.get();\n" +
			"	}\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
    	VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
    	List modifiers = statement.modifiers();
    	assertEquals("Wrong size", 2, modifiers.size());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99510
	public void test0191() throws CoreException, IOException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0191", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS4(), sourceUnit, true);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Type safety: Unchecked cast from Collection<capture#1-of ? extends Number> to Vector<Object>");
		node = getASTNode(unit, 0, 0, 0);
		assertNotNull("No node", node);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression initializer = fragment.getInitializer();
		assertNotNull("No initializer", initializer);
		assertEquals("Not a cast expression", ASTNode.CAST_EXPRESSION, initializer.getNodeType());
		CastExpression castExpression = (CastExpression) initializer;
		Type type = castExpression.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
		Expression expression = castExpression.getExpression();
		ITypeBinding typeBinding2 = expression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding2);
		assertTrue("Not cast compatible", typeBinding2.isCastCompatible(typeBinding));
	}

	// Wrong ParameterizedTypeBinding yields null type declaration result
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100584
	public void test0192() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X<E> {\n" +
			"	public static class InnerClass {\n" +
			"		static class InnerInnerClass {\n" +
			"			/*start*/X.WrongInnerClass/*end*/.InnerInnerClass m;\n" +
			"		}\n" +
			"	}\n" +
			"}";
	   	IBinding[] bindings = resolveBindings(contents, this.workingCopy, false/*don't report errors*/);
	   	if (bindings[0] != null) {
	   		// should not get here if patch 100584 applied
		   	try {
		   		bindings[0].toString();
		   		fail("should get an exception if bug 100584 present");
		   		// which means that the code would now return a non null,
		   		// erroneous binding, yet able to respond to toString
		   	} catch (Throwable t) {/* absorb quietly */}
	   	}
	   	assertTrue("should yield a null, not a malformed binding",
	   			bindings[0] == null);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=104492
	public void test0193() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        byte[] b1 = new byte[0];\n" +
			"        byte[] b2 = new byte[0];\n" +
			"        for (byte[] bs : new byte[][] { b1, b2 }) {}\n" +
			"    }\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit unit = (CompilationUnit) node;
    	node = getASTNode(unit, 0, 0, 2);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, node.getNodeType());
    	EnhancedForStatement forStatement = (EnhancedForStatement) node;
    	SingleVariableDeclaration singleVariableDeclaration = forStatement.getParameter();
    	assertEquals("Should be 0", 0, singleVariableDeclaration.getExtraDimensions());
    	Type type = singleVariableDeclaration.getType();
    	assertEquals("Not an array type", ASTNode.ARRAY_TYPE, type.getNodeType());
    	ArrayType arrayType = (ArrayType) type;
    	assertEquals("Should be 1", 1, arrayType.getDimensions());
    }

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=104492
	public void test0194() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        byte[] b1 = new byte[0];\n" +
			"        byte[] b2 = new byte[0];\n" +
			"        for (byte[] bs/*comment*/ [ /*comment*/ ]: new byte[][][] { new byte[][] { b1, b2 }}) {}\n" +
			"    }\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit unit = (CompilationUnit) node;
    	node = getASTNode(unit, 0, 0, 2);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, node.getNodeType());
    	EnhancedForStatement forStatement = (EnhancedForStatement) node;
    	SingleVariableDeclaration singleVariableDeclaration = forStatement.getParameter();
    	assertEquals("Should be 1", 1, singleVariableDeclaration.getExtraDimensions());
    	Type type = singleVariableDeclaration.getType();
    	assertEquals("Not an array type", ASTNode.ARRAY_TYPE, type.getNodeType());
    	ArrayType arrayType = (ArrayType) type;
    	assertEquals("Should be 1", 1, arrayType.getDimensions());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106834
	public void test0195() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X {\n" +
			"	<S extends Number, T> void take(S e, T f) {}\n" +
			"	<S extends Number, T> void take(T e, S f) {}\n" +
			"	<S extends Number, T extends S> void take(T e, S f) {}\n" +
			"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit unit = (CompilationUnit) node;
    	node = getASTNode(unit, 0, 0);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    	MethodDeclaration methodDeclaration = (MethodDeclaration) node;
    	IMethodBinding methodBinding = methodDeclaration.resolveBinding();

    	node = getASTNode(unit, 0, 1);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    	MethodDeclaration methodDeclaration2 = (MethodDeclaration) node;
    	IMethodBinding methodBinding2 = methodDeclaration2.resolveBinding();

    	node = getASTNode(unit, 0, 2);
    	assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    	MethodDeclaration methodDeclaration3 = (MethodDeclaration) node;
    	IMethodBinding methodBinding3 = methodDeclaration3.resolveBinding();

    	assertFalse("Bindings are equals", methodBinding.isEqualTo(methodBinding2));
    	assertFalse("Bindings are equals", methodBinding2.isEqualTo(methodBinding));
    	assertFalse("Bindings are equals", methodBinding3.isEqualTo(methodBinding));
    	assertFalse("Bindings are equals", methodBinding3.isEqualTo(methodBinding2));
    	assertFalse("Bindings are equals", methodBinding2.isEqualTo(methodBinding3));
    	assertFalse("Bindings are equals", methodBinding.isEqualTo(methodBinding3));
    	assertTrue("Bindings are not equals", methodBinding3.isEqualTo(methodBinding3));
    	assertTrue("Bindings are not equals", methodBinding2.isEqualTo(methodBinding2));
    	assertTrue("Bindings are not equals", methodBinding.isEqualTo(methodBinding));
    }

	/*
	 * Ensures that the signature of and IBinding representing a local type ends with the local type's simple name.
	 * (regression test for bug 104879 BindingKey#internalToSignature() returns invalid signature for local type
	 */
	public void test0196() throws JavaModelException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo() {\n" +
			"    /*start*/class Y {\n" +
			"    }/*end*/\n" +
			"  }\n" +
			"}",
			this.workingCopy);
		IBinding binding = ((TypeDeclarationStatement) node).resolveBinding();
		assertNotNull("No binding", binding);

		String key = binding.getKey();
		String signature = new BindingKey(key).toSignature();
		String simpleName = Signature.getSimpleName(Signature.toString(signature));
		assertEquals("Unexpected simple name", "Y", simpleName);
	}


	/*
	 * Ensures that creating an AST with binding resolution where there is a problem in a binary
	 * doesn't throw an NPE
	 * (regression test for bug 100606 NPE during reconcile)
	 */
	public void test0197() throws CoreException {
		try {
			createJavaProject("P", new String[] {"src" }, new String[] {"CONVERTER_JCL18_LIB", "/P/lib"}, "bin", CompilerOptions.getFirstSupportedJavaVersion());
			IFolder folder = createFolder("/P/lib");
			String classesPath = folder.getLocation().toOSString();
			Map options = new HashMap();
			String[] pathsAndContents = new String[] {
				"p/Bin.java",
				"package p;\n" +
				"public class Bin {\n" +
				"}",
				"p/BinSub.java",
				"package p;\n" +
				"public class BinSub extends Bin {\n" +
				"}",
			};
			Util.compile(pathsAndContents, options, classesPath);
			folder.refreshLocal(IResource.DEPTH_INFINITE, null);
//			folder.getFolder("p").getFile("Bin.class").delete(false, null);
			Util.delete(folder.getFolder("p").getFile("Bin.class"));
	    	this.workingCopy = getWorkingCopy("/P/src/X.java", true/*resolve*/);
	    	String contents =
				"public class X {\n" +
				"	void bar() throws p.BinSub {\n" +
				"	}\n" +
				"	</*start*/T/*end*/> void foo() {\n" +
				"	}\n" +
				"}";
		   	IBinding[] bindings = resolveBindings(contents, this.workingCopy, false/*don't report errors*/);
		   	assertBindingsEqual(
		   		"LX;.foo<T:Ljava/lang/Object;>()V:TT;",
		   		bindings);
		} finally {
			deleteProject("P");
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=110773
	 */
	public void test0198() throws CoreException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			"public class X<E> {\n" +
			"    class B { }\n" +
			"    {\n" +
			"        X<String>.B b;\n" +
			"    }\n" +
			"}",
			this.workingCopy,
			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 1);
    	assertEquals("Not a initializer", ASTNode.INITIALIZER, node.getNodeType());
    	Initializer initializer = (Initializer) node;
    	Block block = initializer.getBody();
    	assertNotNull("No block", block);
    	List statements = block.statements();
    	assertEquals("Wrong size", 1, statements.size());
    	Statement statement = (Statement) statements.get(0);
    	assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, statement.getNodeType());
    	VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
    	Type type = variableDeclarationStatement.getType();
    	ITypeBinding typeBinding = type.resolveBinding();
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
    	TypeDeclaration typeDeclaration = (TypeDeclaration) node;
    	ITypeBinding typeBinding2 = typeDeclaration.resolveBinding();
    	assertTrue("Not a member type", typeDeclaration.isMemberTypeDeclaration());
    	assertFalse("Binding should not be equals", typeBinding.isEqualTo(typeBinding2));
    	assertFalse("Binding should not be equals", typeBinding2.isEqualTo(typeBinding));
    	ITypeBinding typeBinding3 = typeBinding.getTypeDeclaration();
    	assertFalse("Binding should not be equals", typeBinding.isEqualTo(typeBinding3));
    	assertFalse("Binding should not be equals", typeBinding3.isEqualTo(typeBinding));
    }

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=110657
	 */
	public void test0199() throws CoreException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		final String source = "public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        byte[] b1 = new byte[0];\n" +
			"        byte[] b2 = new byte[0];\n" +
			"        for (byte[] bs : new byte[][] { b1, b2 }) {\n" +
			"			System.out.println(bs);\n" +
			"        }\n" +
			"    }\n" +
			"}";
		ASTNode node = buildAST(
			source,
			this.workingCopy,
			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0, 2);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, node.getNodeType());
    	EnhancedForStatement forStatement = (EnhancedForStatement) node;
    	final SingleVariableDeclaration parameter = forStatement.getParameter();
    	final Type type = parameter.getType();
    	checkSourceRange(type, "byte[]", source);
    	checkSourceRange(parameter, "byte[] bs", source);
    	assertTrue("not an array type", type.isArrayType());
    	ArrayType arrayType = (ArrayType) type;
    	Type elementType = arrayType.getElementType();
    	assertTrue("not a primitive type", elementType.isPrimitiveType());
    	checkSourceRange(elementType, "byte", source);
    }
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=110657
	 */
	public void test0200() throws CoreException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		final String source = "public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        byte[] b1 = new byte[0];\n" +
			"        byte[] b2 = new byte[0];\n" +
			"        for (final byte[] bs : new byte[][] { b1, b2 }) {\n" +
			"			System.out.println(bs);\n" +
			"        }\n" +
			"    }\n" +
			"}";
		ASTNode node = buildAST(
			source,
			this.workingCopy,
			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0, 2);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, node.getNodeType());
    	EnhancedForStatement forStatement = (EnhancedForStatement) node;
    	final SingleVariableDeclaration parameter = forStatement.getParameter();
    	final Type type = parameter.getType();
    	checkSourceRange(type, "byte[]", source);
    	checkSourceRange(parameter, "final byte[] bs", source);
    }

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=110657
	 */
	public void test0201() throws CoreException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		final String source = "public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        byte[] b1 = new byte[0];\n" +
			"        byte[] b2 = new byte[0];\n" +
			"        for (final byte bs[] : new byte[][] { b1, b2 }) {\n" +
			"			System.out.println(bs);\n" +
			"        }\n" +
			"    }\n" +
			"}";
		ASTNode node = buildAST(
			source,
			this.workingCopy,
			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0, 2);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, node.getNodeType());
    	EnhancedForStatement forStatement = (EnhancedForStatement) node;
    	final SingleVariableDeclaration parameter = forStatement.getParameter();
    	final Type type = parameter.getType();
    	assertEquals("Wrong extended dimension", 1, parameter.getExtraDimensions());
    	checkSourceRange(type, "byte", source);
    	checkSourceRange(parameter, "final byte bs[]", source);
    	assertTrue("not a primitive type", type.isPrimitiveType());
    }

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=110657
	 */
	public void test0202() throws CoreException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		final String source = "public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        byte[] b1 = new byte[0];\n" +
			"        byte[] b2 = new byte[0];\n" +
			"        for (@Ann final byte bs[] : new byte[][] { b1, b2 }) {\n" +
			"			System.out.println(bs);\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"@interface Ann {}";
		ASTNode node = buildAST(
			source,
			this.workingCopy,
			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0, 2);
    	assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, node.getNodeType());
    	EnhancedForStatement forStatement = (EnhancedForStatement) node;
    	final SingleVariableDeclaration parameter = forStatement.getParameter();
    	final Type type = parameter.getType();
    	assertEquals("Wrong extended dimension", 1, parameter.getExtraDimensions());
    	checkSourceRange(type, "byte", source);
    	checkSourceRange(parameter, "@Ann final byte bs[]", source);
    	assertTrue("not a primitive type", type.isPrimitiveType());
    	List modifiers = parameter.modifiers();
    	assertEquals("Wrong size", 2, modifiers.size());
    	final ASTNode modifier1 = ((ASTNode) modifiers.get(0));
		assertEquals("Not an annotation", ASTNode.MARKER_ANNOTATION, modifier1.getNodeType());
    	final ASTNode modifier2 = ((ASTNode) modifiers.get(1));
		assertEquals("Not a modifier", ASTNode.MODIFIER, modifier2.getNodeType());
		checkSourceRange(modifier1, "@Ann", source);
		checkSourceRange(modifier2, "final", source);
    }
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80472
	 */
	public void test0203() throws CoreException {
	   	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		final String source = "class X<T> {\n" +
				"        X<T> list= this;\n" +
				"        X<? super T> list2= this;\n" +
				"}";
		ASTNode node = buildAST(
			source,
			this.workingCopy,
			false);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    	CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 0);
    	node = getASTNode(compilationUnit, 0, 0);
    	assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
    	FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
    	List fragments = fieldDeclaration.fragments();
    	assertEquals("Wrong size", 1, fragments.size());
    	VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
    	Expression initializer = fragment.getInitializer();
    	ITypeBinding typeBinding = initializer.resolveTypeBinding();
    	assertTrue("Not a parameterized binding", typeBinding.isParameterizedType());

    	node = getASTNode(compilationUnit, 0, 1);
    	assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
    	fieldDeclaration = (FieldDeclaration) node;
    	fragments = fieldDeclaration.fragments();
    	assertEquals("Wrong size", 1, fragments.size());
    	fragment = (VariableDeclarationFragment) fragments.get(0);
    	initializer = fragment.getInitializer();
    	typeBinding = initializer.resolveTypeBinding();
    	assertTrue("Not a parameterized binding", typeBinding.isParameterizedType());
	}

	/*
	 * Ensures that the key of static member of generic enclosing type is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=83064)
	 */
	public void test0204() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X<T> {\n" +
    		"	static class Y {\n" +
    		"		/*start*/Y/*end*/ y;\n" +
    		"	}\n" +
    		"}";
	   	IBinding[] bindings = resolveBindings(contents, this.workingCopy);
	   	assertBindingsEqual(
	   		"LX$Y;", // static member is not raw
	   		bindings);
	}

	/*
	 * Ensures that the key of non-static member with a generic enclosing type is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=83064)
	 */
	public void test0204b() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X<T> {\n" +
    		"	class Y {\n" +
    		"		/*start*/Y/*end*/ y;\n" +
    		"	}\n" +
    		"}";
	   	IBinding[] bindings = resolveBindings(contents, this.workingCopy);
	   	assertBindingsEqual(
	   		"LX<LX;:TT;>.Y;", // non-static member is generic
	   		bindings);
	}

	/*
	 * Ensures that the key of non-static member with a raw enclosing type is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=83064)
	 */
	public void test0204c() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X<T> {\n" +
    		"	class Y {\n" +
    		"	}\n" +
    		"	static X./*start*/Y/*end*/ y;\n" +
    		"}";
    	IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, true);
    	try {
    		javaProject.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		   	IBinding[] bindings = resolveBindings(contents, this.workingCopy);
		   	assertBindingsEqual(
		   		"LX<>.Y;", // non-static member with raw enclosing
		   		bindings);
    	} finally {
    		javaProject.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, old);
    	}
	}

	/*
	 * Ensures that the declaration method binding and the reference method bindings are the same
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=83064)
	 */
	public void test0205() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X<E> {\n" +
    		"    @I(12)\n" +
    		"    @interface I {\n" +
    		"        @I(/*start1*/value/*end1*/=13)\n" +
    		"        int /*start2*/value/*end2*/();\n" +
    		"    }\n" +
    		"}";
	   	IBinding[] bindings = resolveBindings(contents, this.workingCopy);
	   	assertTrue("Bindings should be the same", bindings[0] == bindings[1]); // generic outer is irrelevant because @interface is implicitly static
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=120263
	 */
	public void test0206() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
    		"        public @interface Annot {\n" +
    		"        }\n" +
    		"        @Annot(newAttrib= {1, 2})\n" +
    		"        public void foo() {\n" +
    		"        }\n" +
    		"}";
    	ASTNode node = buildAST(
    			contents,
    			this.workingCopy,
    			false);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
    	assertProblemsSize(compilationUnit, 1, "The attribute newAttrib is undefined for the annotation type X.Annot");
    	node = getASTNode(compilationUnit, 0, 1);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List modifiers = methodDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not a normal annotation", extendedModifier instanceof NormalAnnotation);
		NormalAnnotation annotation = (NormalAnnotation) extendedModifier;
		List values = annotation.values();
		assertEquals("Wrong size", 1, values.size());
		MemberValuePair memberValuePair = (MemberValuePair) values.get(0);
		Expression value = memberValuePair.getValue();
		assertEquals("Not an array initializer", ASTNode.ARRAY_INITIALIZER, value.getNodeType());
		ArrayInitializer arrayInitializer = (ArrayInitializer) value;
		ITypeBinding typeBinding = arrayInitializer.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=124716
	 */
	public void test0207() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"    void m() {\n" +
			"        new Object() {};\n" +
			"    }\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		Expression expression = ((ExpressionStatement) node).getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("Should not be null", binding);
		IAnnotationBinding[] annotations = binding.getAnnotations();
		assertNotNull("Should not be null", annotations);
		assertEquals("Should be empty", 0, annotations.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=125807
	 */
	public void test0208() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"/*start*/@Override(x= 1)/*end*/\n" +
			"public class X { }";
		NormalAnnotation normalAnnotation = (NormalAnnotation) buildAST(
				contents,
				this.workingCopy,
				false,
				true,
				false);
		IAnnotationBinding annotationBinding = normalAnnotation.resolveAnnotationBinding();
		IMemberValuePairBinding[] pairs = annotationBinding.getDeclaredMemberValuePairs();
		assertEquals("Wrong size", 0, pairs.length);
		List values = normalAnnotation.values();
		assertEquals("Wrong size", 1, values.size());
		MemberValuePair pair = (MemberValuePair) values.get(0);
		assertNotNull("no value", pair.getValue());
	}

	public void test0209() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test/V.java", true/*resolve*/);
		String contents =
			"package test;\n" +
			"import pack.*;\n" +
			"public class V {\n" +
			"	void bar() {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		@A3(\n" +
			"			annot = @A2(\n" +
			"				annot = @A1(value = E.CV, list = new E[] { E.CAV, E.CAV}, clazz = E.class),\n" +
			"				value = E.CV,\n" +
			"				list = new E[] { E.CAV, E.CAV},\n" +
			"				clazz = E.class),\n" +
			"			value = E.CV,\n" +
			"			list = new E[] { E.CAV, E.CAV},\n" +
			"			clazz = E.class)\n" +
			"		int x = 0;\n" +
			"		System.out.println(x);\n" +
			"		System.out.println(x + 1);\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		String problems =
			"The value for annotation attribute A1.list must be an array initializer\n" +
			"The value for annotation attribute A2.list must be an array initializer\n" +
			"The value for annotation attribute A3.list must be an array initializer";
		assertProblemsSize(compilationUnit, 3, problems);
		List imports = compilationUnit.imports();
		assertEquals("wrong size", 1, imports.size());
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.get(0);
		Name name = importDeclaration.getName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		SimpleName simpleName = (SimpleName) name;
		IBinding binding = simpleName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong type", IBinding.PACKAGE, binding.getKind());
	}
	public void test0210() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", false);
		String contents =
			"public class X {\n" +
			"	void foo(Object r) {\n" +
			"		if (r instanceof Future<?>) {\n" +
			"			System.out.println(\"TRUE\");\n" +
			"		} else {\n" +
			"			System.out.println(\"FALSE\");\n" +
			"		}\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertNotNull("No node", node);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
		IfStatement ifStatement = (IfStatement) node;
		Expression expression = ifStatement.getExpression();
		checkSourceRange(expression, "r instanceof Future<?>", contents);
		assertEquals("Not an instanceof expression", ASTNode.INSTANCEOF_EXPRESSION, expression.getNodeType());
		InstanceofExpression instanceofExpression = (InstanceofExpression) expression;
		Type type = instanceofExpression.getRightOperand();
		checkSourceRange(type, "Future<?>", contents);
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=129096
	 */
	public void test0211() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	void foo(java.util.List<?> tab[]) {\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy,
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
		checkSourceRange(variableDeclaration, "java.util.List<?> tab[]", contents);
		checkSourceRange(variableDeclaration.getType(), "java.util.List<?>", contents);
		checkSourceRange(variableDeclaration.getName(), "tab", contents);
		assertEquals("wrong number of extra dimensions", 1, variableDeclaration.getExtraDimensions());
	}
	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=129096
	 */
	public void test0212() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	void foo(java.util.List<?> tab[][]) {\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy,
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
		checkSourceRange(variableDeclaration, "java.util.List<?> tab[][]", contents);
		checkSourceRange(variableDeclaration.getType(), "java.util.List<?>", contents);
		checkSourceRange(variableDeclaration.getName(), "tab", contents);
		assertEquals("wrong number of extra dimensions", 2, variableDeclaration.getExtraDimensions());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=130528
	 */
	public void test0213() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"    int test(String[] strings) {\n" +
			"        return strings.length;\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy,
			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement returnStatement = (ReturnStatement) node;
		Expression expression = returnStatement.getExpression();
		assertNotNull("No expression", expression);
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, expression.getNodeType());
		QualifiedName name = (QualifiedName) expression;
		SimpleName simpleName = name.getName();
		checkSourceRange(simpleName, "length", contents);
		IBinding binding = simpleName.resolveBinding();
		assertEquals("Not a field", IBinding.VARIABLE, binding.getKind());
		IVariableBinding variableBinding = (IVariableBinding) binding;
		assertEquals("No annotations", 0, variableBinding.getAnnotations().length);
	}

	/*
	 * Check unique instance of generic method bindings
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=104293
	 */
	public void test0214() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X {\n" +
			"	\n" +
			"	<T extends A> T foo(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().bar();\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		B b1 = foo(new B());\n" +
			"		B b2 = foo(new B());\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class A {}\n" +
			"class B extends A {}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 2, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 2, 1);
		assertEquals("Not a compilation unit", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		statement = (VariableDeclarationStatement) node;
		fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment= (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding2 = invocation.resolveMethodBinding();

		assertTrue("Not identical", methodBinding == methodBinding2);
	}

	/*
	 * Check unique instance of generic method bindings
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=104293
	 */
	public void test0215() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"public class X {\n" +
			"	static <T> T identity(T t) { return t; }\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"aaa\";\n" +
			"		identity(s);\n" +
			"		identity(s);\n" +
			"		identity(s);\n" +
			"\n" +
			"		Object o = new Object();\n" +
			"		identity(o);\n" +
			"		identity(o);\n" +
			"		identity(o);\n" +
			"\n" +
			"		Throwable t = null;\n" +
			"		identity(t);\n" +
			"		identity(t);\n" +
			"		identity(t);\n" +
			"\n" +
			"		Exception e = null;\n" +
			"		identity(e);\n" +
			"		identity(e);\n" +
			"		identity(e);\n" +
			"\n" +
			"		NullPointerException npe = null;\n" +
			"		identity(npe);\n" +
			"		identity(npe);\n" +
			"		identity(npe);\n" +
			"\n" +
			"		Cloneable c = null;\n" +
			"		identity(c);\n" +
			"		identity(c);\n" +
			"		identity(c);\n" +
			"	}\n" +
			"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1, 1);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 2);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding2 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 3);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding3 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 5);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding4 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 6);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding5 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 9);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding6 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 10);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding7 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 11);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding8 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 13);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding9 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 14);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding10 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 15);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding11 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 17);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding12 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 18);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding13 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 19);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding14 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 21);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding15 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 22);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding16 = invocation.resolveMethodBinding();

		node = getASTNode(unit, 0, 1, 23);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding17 = invocation.resolveMethodBinding();

		assertTrue("method bindings are not equals", methodBinding == methodBinding2);
		assertTrue("method bindings are not equals", methodBinding2 == methodBinding3);
		assertTrue("method bindings are not equals", methodBinding4 == methodBinding5);
		assertTrue("method bindings are not equals", methodBinding6 == methodBinding7);
		assertTrue("method bindings are not equals", methodBinding7 == methodBinding8);
		assertTrue("method bindings are not equals", methodBinding9 == methodBinding10);
		assertTrue("method bindings are not equals", methodBinding9 == methodBinding11);
		assertTrue("method bindings are not equals", methodBinding12 == methodBinding13);
		assertTrue("method bindings are not equals", methodBinding14 == methodBinding13);
		assertTrue("method bindings are not equals", methodBinding15 == methodBinding16);
		assertTrue("method bindings are not equals", methodBinding17 == methodBinding16);
	}

	/*
	 * Check unique instance of generic method bindings
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=104293
	 */
	public void test0216() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"class Y<T> {\n" +
			"	<T> Class foo(T t) {\n" +
			"		return t.getClass();\n" +
			"	}\n" +
			"}\n" +
			"public class X { \n" +
			"	 \n" +
			"	public static void main(String[] args) { \n" +
			"		Class c = new Y().foo(null);\n" +
			"		Class c2 = new Y().foo(null);\n" +
			"	} \n" +
			"}\n" +
			"";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedOutput =
			"Type safety: The method foo(Object) belongs to the raw type Y. References to generic type Y<T> should be parameterized\n" +
			"Type safety: The method foo(Object) belongs to the raw type Y. References to generic type Y<T> should be parameterized";
		assertProblemsSize(unit, 2, expectedOutput);
		node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		assertTrue("Not a raw method", methodBinding.isRawMethod());

		node = getASTNode(unit, 1, 0, 1);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		statement = (VariableDeclarationStatement) node;
		fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment= (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding2 = invocation.resolveMethodBinding();
		assertTrue("Not a raw method", methodBinding2.isRawMethod());

		assertTrue("Method bindings are not identical", methodBinding == methodBinding2);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=110799
	 */
	public void test0217() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"class Generic<E> {\n" +
			"}\n" +
			"public class X {\n" +
			"    Generic raw;\n" +
			"    java.util.Collection rawCollection;\n" +
			"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedOutput =	"";
		assertProblemsSize(unit, 0, expectedOutput);
		node = getASTNode(unit, 1, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertTrue("isRaw", typeBinding.isRawType());

		node = getASTNode(unit, 1, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		fieldDeclaration = (FieldDeclaration) node;
		type = fieldDeclaration.getType();
		ITypeBinding typeBinding2 = type.resolveBinding();
		assertTrue("isRaw", typeBinding2.isRawType());

		ITypeBinding[] typeParameters = typeBinding.getTypeParameters();
		assertEquals("Wrong size", 0, typeParameters.length);

		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		assertEquals("Wrong size", 0, typeArguments.length);

		typeParameters = typeBinding2.getTypeParameters();
		assertEquals("Wrong size", 0, typeParameters.length);

		typeArguments = typeBinding2.getTypeArguments();
		assertEquals("Wrong size", 0, typeArguments.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=140318
	 */
	public void test0218() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @category fo\n" +
			"	 */\n" +
			"	@Test private int fXoo;\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Test cannot be resolved to a type");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration declaration = (FieldDeclaration) node;
		List modifiers = declaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		assertEquals("Not a marker annotation", ASTNode.MARKER_ANNOTATION, ((ASTNode) modifiers.get(0)).getNodeType());
		MarkerAnnotation annotation = (MarkerAnnotation) modifiers.get(0);
		Name name = annotation.getTypeName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		ITypeBinding binding = name.resolveTypeBinding();
		assertNotNull("No binding", binding);
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding", binding2);
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		assertNotNull("No binding", annotationBinding);
		assertEquals("LX;.fXoo)I@LTest;", annotationBinding.getKey());
		assertTrue("Annotation should not flagged as recovered", annotationBinding.isRecovered());
		assertTrue("Annotation type should be flagged as recovered", annotationBinding.getAnnotationType().isRecovered());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=140318
	 */
	public void test0219() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @category fo\n" +
			"	 */\n" +
			"	@Test private int fXoo;\n" +
			"}\n" +
			"class Test {}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Test is not an annotation type");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration declaration = (FieldDeclaration) node;
		List modifiers = declaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		assertEquals("Not a marker annotation", ASTNode.MARKER_ANNOTATION, ((ASTNode) modifiers.get(0)).getNodeType());
		MarkerAnnotation annotation = (MarkerAnnotation) modifiers.get(0);
		Name name = annotation.getTypeName();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, name.getNodeType());
		ITypeBinding binding = name.resolveTypeBinding();
		assertNotNull("No binding", binding);
		IBinding binding2 = name.resolveBinding();
		assertNotNull("No binding", binding2);
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		assertNull("Got a binding", annotationBinding);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=142793
	 * updated for https://bugs.eclipse.org/bugs/show_bug.cgi?id=143001
	 */
	public void test0220() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
			"        void bar(String[] c) {\n" +
			"                for(String s: c) {\n" +
			"                        try {\n" +
			"                        }\n" +
			"                }\n" +
			"        }\n" +
			"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false,
    			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Syntax error, insert \"Finally\" to complete BlockStatements");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block body = methodDeclaration.getBody();
		assertNotNull("No body", body);
		List statements = body.statements();
		assertEquals("Wrong size", 1, statements.size());
		assertTrue("Recovered", !isRecovered(body));
		assertFalse("Malformed", isMalformed(body));

		Statement statement = (Statement)statements.get(0);
		assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, statement.getNodeType());
		EnhancedForStatement enhancedForStatement = (EnhancedForStatement) statement;
		Statement forBody = enhancedForStatement.getBody();
		assertNotNull("No body", forBody);
		assertEquals("Not a block", ASTNode.BLOCK, forBody.getNodeType());

		statements = ((Block)forBody).statements();
		assertEquals("Wrong size", 1, statements.size());
		statement = (Statement)statements.get(0);
		assertEquals("Not an try statement", ASTNode.TRY_STATEMENT, statement.getNodeType());
		TryStatement tryStatement = (TryStatement) statement;
		Block finallyBlock = tryStatement.getFinally();
		assertNotNull("No finally block", finallyBlock);
		assertTrue("Not recovered", isRecovered(finallyBlock));


	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=147875
	 */
	public void _2551_test0221() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"import p1.p2.MyEnum;\n" +
    		"public class X {\n" +
			"	MyEnum foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false,
    			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=148797
	 */
	public void test0222() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
    		"   public void a() {\n" +
    		"      Object a = null;\n" +
    		"      for (Object o : a.getClass()()) {\n" +
    		"      }\n" +
    		"   }\n" +
    		"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false,
    			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 2, "Syntax error on token \")\", invalid Name\n" +
				"Syntax error, insert \")\" to complete EnhancedForStatementHeader");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block body = methodDeclaration.getBody();
		assertNotNull("No body", body);
		List statements = body.statements();
		assertEquals("Wrong size", 2, statements.size());
		Statement statement = (Statement) statements.get(1);
		assertEquals("Not an enhanced for statement", ASTNode.ENHANCED_FOR_STATEMENT, statement.getNodeType());
		EnhancedForStatement forStatement = (EnhancedForStatement) statement;
		Expression expression = forStatement.getExpression();
		assertNotNull("No expression", expression);
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=153303
	 */
	public void test0223() throws JavaModelException {
			this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"    @Zork\n" +
			"    public void foo( ) {\n" +
			"    }\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Zork cannot be resolved to a type");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = methodBinding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		assertNotNull("No binding", annotations[0]);
		assertEquals("LX;.foo()V@LZork;", annotations[0].getKey());
		assertTrue("Annotation should be flagged as recovered", annotations[0].isRecovered());
		assertTrue("Annotation type should be flagged as recovered", annotations[0].getAnnotationType().isRecovered());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=153303
	 */
	public void test0224() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"@Zork\n" +
    		"public class X {\n" +
    		"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Zork cannot be resolved to a type");
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		assertNotNull("No binding", annotations[0]);
		assertEquals("LX;@LZork;", annotations[0].getKey());
		assertTrue("Annotation should be flagged as recovered", annotations[0].isRecovered());
		assertTrue("Annotation type should be flagged as recovered", annotations[0].getAnnotationType().isRecovered());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=153303
	 */
	public void test0225() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"public class X {\n" +
    		"    public void foo(@Zork String s) {\n" +
    		"    }\n" +
    		"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false,
    			false,
    			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Zork cannot be resolved to a type");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 1, parameters.size());
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		IVariableBinding variableBinding = singleVariableDeclaration.resolveBinding();
		IAnnotationBinding[] bindings = variableBinding.getAnnotations();
		assertEquals("Wrong size", 1, bindings.length);
		assertNotNull("No binding", bindings[0]);
		assertEquals("@LZork;", bindings[0].getKey());
		assertTrue("Annotation should be flagged as recovered", bindings[0].isRecovered());
		assertTrue("Annotation type should be flagged as recovered", bindings[0].getAnnotationType().isRecovered());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=153303
	 */
	public void test0226() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/p/package-info.java", true/*resolve*/);
    	String contents =
    		"@Zork package p;";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Zork cannot be resolved to a type");
		PackageDeclaration packageDeclaration = unit.getPackage();
		IPackageBinding packageBinding = packageDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = packageBinding.getAnnotations();
		assertEquals("Wrong size", 0, annotations.length);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=155115
	public void test0227() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"import anno.Anno;\n" +
    		"import binary.B;\n" +
    		"import intf.IFoo;\n" +
    		"\n" +
    		"public class X extends B {\n" +
    		"	@Anno(clz=IFoo.IBar.class)\n" +
    			// the annotation we chase up is not this one, but the one
    			// carried by B#f
    		"	public void f() {}\n" +
    		"   IFoo.IBar m;\n" +
    		"}";
    	class TestASTRequestor extends ASTRequestor {
    		public ArrayList asts = new ArrayList();
    		public void acceptAST(ICompilationUnit source, CompilationUnit compilationUnit) {
    			this.asts.add(compilationUnit);
    		}
    		public void acceptBinding(String bindingKey, IBinding binding) {
    		}
    	}
    	this.workingCopy.getBuffer().setContents(contents);
    	this.workingCopy.save(null, true);
    	TestASTRequestor requestor = new TestASTRequestor();
    	resolveASTs(new ICompilationUnit[] { this.workingCopy } , new String[0], requestor, getJavaProject("Converter15"), null);
    	ArrayList asts = requestor.asts;
		assertEquals("Wrong size", 1, asts.size());
		CompilationUnit compilationUnit = (CompilationUnit) asts.get(0);
		assertNotNull("No compilation unit", compilationUnit);
		List types = compilationUnit.types();
		assertEquals("Wrong size", 1, types.size());
		AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) types.get(0);
		assertEquals("Wrong type", ASTNode.TYPE_DECLARATION, abstractTypeDeclaration.getNodeType());
		TypeDeclaration declaration = (TypeDeclaration) abstractTypeDeclaration;
		Type superclass = declaration.getSuperclassType();
		assertNotNull("No superclass", superclass);
		ITypeBinding typeBinding = superclass.resolveBinding();
		assertNotNull("No binding", typeBinding);
		IMethodBinding[] methods = typeBinding.getDeclaredMethods();
		assertNotNull("No methods", methods);
		assertEquals("Wrong size", 2, methods.length);
		IMethodBinding methodBinding = null;
		for(int i = 0; i < 2; i++) {
			methodBinding = methods[i];
			if (methodBinding.getName().equals("f")) {
				break;
			}
		}
		assertEquals("Wrong name", "f", methodBinding.getName());
		IAnnotationBinding[] annotationBindings = methodBinding.getAnnotations();
		assertNotNull("No annotations", annotationBindings);
		assertEquals("Wrong size", 1, annotationBindings.length);
		IAnnotationBinding annotationBinding = annotationBindings[0];
		IMemberValuePairBinding[] pairs = annotationBinding.getAllMemberValuePairs();
		assertNotNull("no pairs", pairs);
		assertEquals("Wrong size", 1, pairs.length);
		IMemberValuePairBinding memberValuePairBinding = pairs[0];
		assertEquals("Wrong kind", IBinding.MEMBER_VALUE_PAIR, memberValuePairBinding.getKind());
		Object value = memberValuePairBinding.getValue();
		assertNotNull("No value", value);
		assertTrue("Not a type binding", value instanceof ITypeBinding);
		assertEquals("Wrong qualified name", "intf.IFoo.IBar",
				((ITypeBinding) value).getQualifiedName());
		IVariableBinding[] fields =
			declaration.resolveBinding().getDeclaredFields();
		assertTrue("Bad field definition", fields != null && fields.length == 1);
		assertEquals("Type binding mismatch", value, fields[0].getType());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=157403
	 */
	public void test0228() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
    		"@interface Ann {\n" +
    		"  int foo();\n" +
    		"}\n" +
    		"@Ann(foo = bar())\n" +
    		"public class X {\n" +
    		"	public static int bar() {\n" +
    		" 		return 0;\n" +
    		"	}\n" +
    		"}";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "The method bar() is undefined for the type X");
		List types = unit.types();
		assertEquals("wrong size", 2, types.size());
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) types.get(1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, typeDeclaration.getNodeType());
		TypeDeclaration declaration = (TypeDeclaration) typeDeclaration;
		List modifiers = declaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("not an annotation", modifier.isAnnotation());
		Annotation annotation = (Annotation) modifier;
		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		List values = normalAnnotation.values();
		assertEquals("wrong size", 1, values.size());
		MemberValuePair pair = (MemberValuePair) values.get(0);
		IBinding binding = pair.getName().resolveBinding();
		assertNotNull("No binding", binding);
		binding = pair.getValue().resolveTypeBinding();
		assertNull("Got a binding", binding);
		binding = pair.resolveMemberValuePairBinding();
		assertNotNull("No binding", binding);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=160089
	 */
	public void test0229() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
    	String contents =
     		"import java.util.List;\n" +
     		"import java.util.Collection;\n" +
     		"public class X {\n" +
     		"	public static List<String> bar;\n" +
     		"   @SuppressWarnings(\"rawtypes\")\n" +
     		"	public static Collection bar2;\n" +
    		"}";
    	this.workingCopy.getBuffer().setContents(contents);
    	this.workingCopy.save(null, true);
    	final ASTNode[] asts = new ASTNode[1];
       	final IBinding[] bindings = new IBinding[1];
       	final String key = BindingKey.createParameterizedTypeBindingKey(
       	     "Ljava/util/Collection<TE;>;", new String[] {});
    	resolveASTs(
			new ICompilationUnit[] {
				this.workingCopy
			},
			new String[] {
				key
			},
			new ASTRequestor() {
                public void acceptAST(ICompilationUnit source, CompilationUnit localAst) {
                	asts[0] = localAst;
                }
                public void acceptBinding(String bindingKey, IBinding binding) {
                	if (key.equals(bindingKey)) {
                		bindings[0] = binding;
                 	}
                }
			},
			getJavaProject("Converter15"),
			null);
    	ASTNode node = asts[0];
    	assertNotNull("Should not be null", node);
    	assertNotNull("Should not be null", bindings[0]);
    	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "At least one of the problems in category 'rawtypes' is not analysed due to a compiler option being ignored");
		node = getASTNode(unit, 0, 0);
    	assertEquals("Not a compilation unit", ASTNode.FIELD_DECLARATION, node.getNodeType());
    	FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
    	Type type = fieldDeclaration.getType();
    	ITypeBinding typeBinding = type.resolveBinding();
		node = getASTNode(unit, 0, 1);
    	assertEquals("Not a compilation unit", ASTNode.FIELD_DECLARATION, node.getNodeType());
    	fieldDeclaration = (FieldDeclaration) node;
    	type = fieldDeclaration.getType();
    	ITypeBinding typeBinding2 = type.resolveBinding();
    	final ITypeBinding collectionTypeBinding = (ITypeBinding) bindings[0];
    	assertTrue("Not a raw type", collectionTypeBinding.isRawType());
    	assertTrue("Not assignement compatible", typeBinding.isAssignmentCompatible(typeBinding2));
    	assertTrue("Not assignement compatible", typeBinding.isAssignmentCompatible(collectionTypeBinding));
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156352
	 */
	public void test0230() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0230", "Test3.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType type = sourceUnit.getType("Test3");//$NON-NLS-1$

		assertNotNull("Should not be null", type);
		ASTParser parser= ASTParser.newParser(getJLS4());
		parser.setProject(type.getJavaProject());
		IBinding[] bindings= parser.createBindings(new IJavaElement[] { type }, null);
		if (bindings.length == 1 && bindings[0] instanceof ITypeBinding) {
			ITypeBinding typeBinding= (ITypeBinding) bindings[0];
			StringBuilder buffer = new StringBuilder();
			while (typeBinding != null) {
				buffer.append(typeBinding.getAnnotations().length);
				typeBinding= typeBinding.getSuperclass();
			}
			// while "020" is the right outcome, this test did oscillate between this and "000"
			// due to changes in processing order (at what point in time are annotations resolved?)
			assertEquals("Wrong number of annotations", "020", String.valueOf(buffer));
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156352
	 */
	public void test0231() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0231", "Test3.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType type = sourceUnit.getType("Test3");//$NON-NLS-1$

		assertNotNull("Should not be null", type);
		ASTParser parser= ASTParser.newParser(getJLS4());
		parser.setSource(sourceUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		List types = unit.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		StringBuilder buffer = new StringBuilder();
		while (typeBinding != null) {
			buffer.append(typeBinding.getAnnotations().length);
			typeBinding= typeBinding.getSuperclass();
		}
		assertEquals("Wrong number of annotations", "020", String.valueOf(buffer));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=167958
	 */
	public void test0232() throws JavaModelException {
		/*
			package test0232;
			import static java.lang.annotation.ElementType.*;
			import static java.lang.annotation.RetentionPolicy.*;
			import java.lang.annotation.Retention;
			import java.lang.annotation.Target;

			@Target(TYPE)
			@Retention(RUNTIME)
			@interface Annot {
			}
			package test0232;
			@Annot
			public class X {
			}
		 */
		String contents =
			"import test0232.X;\n" +
			"public class A {\n" +
			"    X test() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/A.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy,
			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration declaration = (MethodDeclaration) node;
		Type type = declaration.getReturnType2();
		ITypeBinding typeBinding = type.resolveBinding();
		assertTrue("Not a binary type binding", !typeBinding.isFromSource());
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertNotNull("No annotations", annotations);
		assertEquals("Wrong size", 1, annotations.length);
		IAnnotationBinding annotationBinding = annotations[0];
		assertEquals("Wrong name", "Annot", annotationBinding.getName());
		ITypeBinding binding = annotationBinding.getAnnotationType();
		assertEquals("Wrong name", "test0232.Annot", binding.getQualifiedName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=167958
	 */
	public void test0233() throws JavaModelException {
		/*
			package test0233;

			import static java.lang.annotation.ElementType.*;
			import static java.lang.annotation.RetentionPolicy.*;

			import java.lang.annotation.Retention;
			import java.lang.annotation.Target;

			@Target(TYPE)
			@Retention(CLASS)
			@interface Annot {
				String message() default "";
			}

			package test0233;

			@Annot(message="Hello, World!")
			public class X {
			}
		 */
		String contents =
			"import test0233.X;\n" +
			"public class A {\n" +
			"    X test() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/A.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy,
			true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration declaration = (MethodDeclaration) node;
		Type type = declaration.getReturnType2();
		ITypeBinding typeBinding = type.resolveBinding();
		assertTrue("Not a binary type binding", !typeBinding.isFromSource());
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertNotNull("No annotations", annotations);
		assertEquals("Wrong size", 1, annotations.length);
		IAnnotationBinding annotationBinding = annotations[0];
		assertEquals("Wrong name", "Annot", annotationBinding.getName());
		ITypeBinding binding = annotationBinding.getAnnotationType();
		assertEquals("Wrong name", "test0233.Annot", binding.getQualifiedName());
		IMemberValuePairBinding[] pairs = annotationBinding.getAllMemberValuePairs();
		assertEquals("Wrong number", 1, pairs.length);
		assertEquals("Wrong key", "message", pairs[0].getName());
		assertEquals("Wrong value", "Hello, World!", pairs[0].getValue());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=169744
	 */
	public void test0234() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"class B {\n" +
			"	<T> int m() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"}\n" +
			"public class X<T> extends B {\n" +
			"	int i = super.<T> m();\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		checkSourceRange(expression, "super.<T> m()", contents);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=172633
	 */
	public void test0235() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0235/X.java", true/*resolve*/);
		String contents =
			"package test0235;\n" +
			"public class X implements I {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedProblems = "The hierarchy of the type X is inconsistent\n" +
		"The type test0235.Zork cannot be resolved. It is indirectly referenced from required type test0235.I";
		assertProblemsSize(unit, 2, expectedProblems);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		assertNotNull("No interfaces", interfaces);
		assertEquals("Wrong size", 1, interfaces.length);
		assertNotNull("Should not be null", interfaces[0]);
		ITypeBinding typeBinding2 = interfaces[0];
		interfaces = typeBinding2.getInterfaces();
		assertNotNull("No interfaces", interfaces);
		assertEquals("Wrong size", 1, interfaces.length);
		assertNotNull("No binding", interfaces[0]);
		assertEquals("Ltest0235/Zork;", interfaces[0].getKey());
		assertFalse("I should not be flagged as recovered", typeBinding2.isRecovered());
		assertTrue("Zork should be flagged as recovered", interfaces[0].isRecovered());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=172633
	 */
	public void test0236() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X implements Runnable, Zork {\n" +
			"	public void run() {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedProblems = "Zork cannot be resolved to a type";
		assertProblemsSize(unit, 1, expectedProblems);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		ITypeBinding[] interfaces = typeBinding.getInterfaces();
		assertNotNull("No interfaces", interfaces);
		assertEquals("Wrong size", 2, interfaces.length);
		assertEquals("Ljava/lang/Runnable;", interfaces[0].getKey());
		assertFalse("Runnable should not be flagged as recovered", interfaces[0].isRecovered());
		assertEquals("LZork;", interfaces[1].getKey());
		assertTrue("Zork should be flagged as recovered", interfaces[1].isRecovered());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173338
	 */
	public void test0237() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0237/X.java", true/*resolve*/);
		String contents =
			"package test0237;\n" +
			"public class X {\n" +
			"	Zork foo() {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedProblems = "Zork cannot be resolved to a type";
		assertProblemsSize(unit, 1, expectedProblems);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		IMethodBinding[] methodBindings = typeBinding.getDeclaredMethods();
		assertNotNull("No method bindings", methodBindings);
		assertEquals("wrong size", 2, methodBindings.length);
		assertEquals("Ltest0237/X;.()V", methodBindings[0].getKey());
		assertEquals("Ltest0237/X;.foo()LZork;", methodBindings[1].getKey());
		assertFalse("#foo() should not be flagged as recovered", methodBindings[1].isRecovered());
		assertTrue("Zork should be flagged as recovered", methodBindings[1].getReturnType().isRecovered());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173338
	 */
	public void test0238() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0238/X.java", true/*resolve*/);
		String contents =
			"package test0238;\n" +
			"public class X extends A {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		typeBinding = typeBinding.getSuperclass();
		IMethodBinding[] methodBindings = typeBinding.getDeclaredMethods();
		assertNotNull("No method bindings", methodBindings);
		assertEquals("wrong size", 1, methodBindings.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173338
	 */
	public void test0238_2() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0238/X.java", true/*resolve*/);
		String contents =
			"package test0238;\n" +
			"public class X extends A {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		typeBinding = typeBinding.getSuperclass();
		IMethodBinding[] methodBindings = typeBinding.getDeclaredMethods();
		assertNotNull("No method bindings", methodBindings);
		assertEquals("wrong size", 2, methodBindings.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173338
	 */
	public void test0239() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0239/X.java", true/*resolve*/);
		String contents =
			"package test0239;\n" +
			"public class X extends A {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		typeBinding = typeBinding.getSuperclass();
		IVariableBinding[] variableBindings = typeBinding.getDeclaredFields();
		assertNotNull("No variable bindings", variableBindings);
		assertEquals("wrong size", 0, variableBindings.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=173338
	 */
	public void test0239_2() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0239/X.java", true/*resolve*/);
		String contents =
			"package test0239;\n" +
			"public class X extends A {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				true,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		typeBinding = typeBinding.getSuperclass();
		IVariableBinding[] variableBindings = typeBinding.getDeclaredFields();
		assertNotNull("No variable bindings", variableBindings);
		assertEquals("wrong size", 1, variableBindings.length);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107001
	 */
	public void test0240() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X<T> {}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", typeBinding);
		ITypeBinding[] typeParameters = typeBinding.getTypeParameters();
		assertEquals("Wrong size", 1, typeParameters.length);
		ITypeBinding typeParameter = typeParameters[0];
		assertTrue("Not a type variable", typeParameter.isTypeVariable());
		assertEquals("Wrong binary name", "X$T", typeParameter.getBinaryName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107001
	 */
	public void test0241() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	<T> void foo() {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		ITypeBinding[] typeParameters = methodBinding.getTypeParameters();
		assertEquals("Wrong size", 1, typeParameters.length);
		ITypeBinding typeParameter = typeParameters[0];
		assertTrue("Not a type variable", typeParameter.isTypeVariable());
		assertEquals("Wrong binary name", "X$()V$T", typeParameter.getBinaryName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107001
	 */
	public void test0242() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	<T> X() {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		ITypeBinding[] typeParameters = methodBinding.getTypeParameters();
		assertEquals("Wrong size", 1, typeParameters.length);
		ITypeBinding typeParameter = typeParameters[0];
		assertTrue("Not a type variable", typeParameter.isTypeVariable());
		assertEquals("Wrong binary name", "X$()V$T", typeParameter.getBinaryName());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107001
	 */
	public void test0243() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/*resolve*/);
		String contents =
			"package p;\n" +
			"public class X<U,V> {\n" +
			"	<T> X(Integer i) {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		ITypeBinding[] typeParameters = methodBinding.getTypeParameters();
		assertEquals("Wrong size", 1, typeParameters.length);
		ITypeBinding typeParameter = typeParameters[0];
		assertTrue("Not a type variable", typeParameter.isTypeVariable());
		assertEquals("Wrong binary name", "p.X$(Ljava/lang/Integer;)V$T", typeParameter.getBinaryName());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173849
	public void test0244() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/xy/X.java", true/*resolve*/);
		String contents =
			"package xy;\n" +
			"\n" +
			"public class X {\n" +
			"	protected class Inner {\n" +
			"	}\n" +
			"\n" +
			"	Inner[] i;\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
		IJavaElement element = typeBinding.getJavaElement();
		assertNotNull("No element", element);
		assertTrue("Doesn't exist", element.exists());
		assertEquals("Wrong handle identifier", "=Converter15/src<xy{X.java[X[Inner", element.getHandleIdentifier());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173849
	public void test0245() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/xy/X.java", true/*resolve*/);
		String contents =
			"package xy;\n" +
			"\n" +
			"public class X {\n" +
			"	protected class Inner {\n" +
			"	}\n" +
			"\n" +
			"	Inner i;\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
		IJavaElement element = typeBinding.getJavaElement();
		assertNotNull("No element", element);
		assertTrue("Doesn't exist", element.exists());
		assertEquals("Wrong handle identifier", "=Converter15/src<xy{X.java[X[Inner", element.getHandleIdentifier());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173849
	public void test0246() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/xy/X.java", true/*resolve*/);
		String contents =
			"package xy;\n" +
			"\n" +
			"public class X {\n" +
			"	protected class Inner {\n" +
			"	}\n" +
			"\n" +
			"	Inner[][] i;\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		Type type = fieldDeclaration.getType();
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
		IJavaElement element = typeBinding.getJavaElement();
		assertNotNull("No element", element);
		assertTrue("Doesn't exist", element.exists());
		assertEquals("Wrong handle identifier", "=Converter15/src<xy{X.java[X[Inner", element.getHandleIdentifier());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=156352
	 */
	public void test0247() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0247", "EclipseCompiler.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(getJLS4(), sourceUnit, true, true);
		assertNotNull("Not a compilation unit", result);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0248() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public <T> void find(T a, List<T> b) {\n" +
			"		}\n" +
			"	public void foo() {\n" +
			"		find(x, Arrays.asList(\"a\"));\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedError = "x cannot be resolved to a variable";
		assertProblemsSize(unit, 1, expectedError);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174436
	public void test0249() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.Collections;\n" +
			"import java.util.Map;\n" +
			"\n" +
			"public class X {\n" +
			"	void caller() {\n" +
			"		Map<String, String> explicitEmptyMap = Collections.<String, String> emptyMap();\n" +
			"		method(explicitEmptyMap);\n" +
			"		Map<String, String> emptyMap = Collections.emptyMap();\n" +
			"		method(emptyMap);\n" +
			"	}\n" +
			"\n" +
			"	void method(Map<String, String> map) {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement= (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());

		node = getASTNode(unit, 0, 0, 2);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		statement= (VariableDeclarationStatement) node;
		fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		methodInvocation = (MethodInvocation) expression;
		assertTrue("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174436
	public void test0250() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.Map;\n" +
			"\n" +
			"class A {\n" +
			"	public <K,V> Map<K,V> foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"public class X extends A {\n" +
			"	void caller() {\n" +
			"		Map<String, String> explicitEmptyMap = super.<String, String> foo();\n" +
			"		method(explicitEmptyMap);\n" +
			"		Map<String, String> emptyMap = super.foo();\n" +
			"		method(emptyMap);\n" +
			"	}\n" +
			"\n" +
			"	void method(Map<String, String> map) {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement= (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a super method invocation", ASTNode.SUPER_METHOD_INVOCATION, expression.getNodeType());
		SuperMethodInvocation methodInvocation = (SuperMethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());

		node = getASTNode(unit, 1, 0, 2);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		statement= (VariableDeclarationStatement) node;
		fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a super method invocation", ASTNode.SUPER_METHOD_INVOCATION, expression.getNodeType());
		methodInvocation = (SuperMethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174436
	public void test0251() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0251", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS4(), sourceUnit, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement= (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());

		node = getASTNode(unit, 0, 0, 2);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		statement= (VariableDeclarationStatement) node;
		fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		methodInvocation = (MethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=174436
	public void test0252() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0252", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode node = runConversion(getJLS4(), sourceUnit, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement= (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a super method invocation", ASTNode.SUPER_METHOD_INVOCATION, expression.getNodeType());
		SuperMethodInvocation methodInvocation = (SuperMethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());

		node = getASTNode(unit, 1, 0, 2);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		statement= (VariableDeclarationStatement) node;
		fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		expression = fragment.getInitializer();
		assertEquals("Not a super method invocation", ASTNode.SUPER_METHOD_INVOCATION, expression.getNodeType());
		methodInvocation = (SuperMethodInvocation) expression;
		assertFalse("Wrong value", methodInvocation.isResolvedTypeInferredFromExpectedType());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0253() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	protected Object foo() {\n" +
			"		List<String> c = null;\n" +
			"		return c;\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedError = "List cannot be resolved to a type";
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
		assertFalse("A recovered binding", variableBinding.isRecovered());
		ITypeBinding typeBinding = variableBinding.getType();
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a recovered binding", typeBinding.isRecovered());
		assertEquals("Wrong name", "List<String>", typeBinding.getName());
		assertEquals("Wrong dimension", 0, typeBinding.getDimensions());
		assertEquals("LList<Ljava/lang/String;>;", typeBinding.getKey());
	}

	/**
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=149567
	 */
	public void test0254() throws JavaModelException {
		String contents =
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	protected Object foo() {\n" +
			"		List<String> c = null;\n" +
			"		return c;\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		assertTrue("No binding recovery", unit.getAST().hasBindingsRecovery());
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("No fragments", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertFalse("A recovered binding", variableBinding.isRecovered());
		ITypeBinding typeBinding = variableBinding.getType();
		assertNotNull("No binding", typeBinding);
		assertFalse("A recovered binding", typeBinding.isRecovered());
		assertEquals("Wrong name", "List<String>", typeBinding.getName());
		assertEquals("Wrong dimension", 0, typeBinding.getDimensions());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=130001
	 */
	public void test0255() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				ICompilationUnit.FORCE_PROBLEM_DETECTION | ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertTrue("No statement recovery", unit.getAST().hasStatementsRecovery());
		assertTrue("No binding recovery", unit.getAST().hasBindingsRecovery());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=130001
	 */
	public void test0256() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				ICompilationUnit.ENABLE_STATEMENTS_RECOVERY);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertTrue("No statement recovery", unit.getAST().hasStatementsRecovery());
		assertFalse("Has binding recovery", unit.getAST().hasBindingsRecovery());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=130001
	 */
	public void test0257() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				0);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertFalse("Has statement recovery", unit.getAST().hasStatementsRecovery());
		assertFalse("Has binding recovery", unit.getAST().hasBindingsRecovery());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=179042
	 */
	public void test0258() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@interface Annot {\n" +
			"	public int id() default 0;\n" +
			"}\n" +
			"@Annot(id=4)\n" +
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				0);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1);
		assertEquals("Not a type declaration unit", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier.isAnnotation());
		Annotation annotation = (Annotation) modifier;
		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		IAnnotationBinding annotationBinding = normalAnnotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 0, annotationBinding.getAnnotations().length);
		IJavaElement javaElement = annotationBinding.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Wrong kind", IBinding.ANNOTATION, annotationBinding.getKind());
		assertEquals("Unexpected key", "LX;@LX~Annot;", annotationBinding.getKey());
		assertEquals("Wrong modifier", Modifier.NONE, annotationBinding.getModifiers());
		assertFalse("A deprecated annotation", annotationBinding.isDeprecated());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=179042
	 */
	public void test0259() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@Deprecated\n" +
			"@interface Annot {\n" +
			"	public int id() default 0;\n" +
			"}\n" +
			"@Annot(id=4)\n" +
			"public class X {\n" +
			"}\n" +
			"@Annot(id=4) class Y {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				0);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1);
		assertEquals("Not a type declaration unit", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier.isAnnotation());
		Annotation annotation = (Annotation) modifier;
		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		IAnnotationBinding annotationBinding = normalAnnotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 0, annotationBinding.getAnnotations().length);
		IJavaElement javaElement = annotationBinding.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Wrong kind", IBinding.ANNOTATION, annotationBinding.getKind());
		assertEquals("Unexpected key", "LX;@LX~Annot;", annotationBinding.getKey());
		assertEquals("Wrong modifier", Modifier.NONE, annotationBinding.getModifiers());
		assertTrue("Not a deprecated annotation", annotationBinding.isDeprecated());
		IMemberValuePairBinding[] allMemberValuePairs = annotationBinding.getAllMemberValuePairs();
		assertEquals("Wrong size", 1, allMemberValuePairs.length);
		assertFalse("Not a recovered binding", annotationBinding.isRecovered());
		assertFalse("Not a synthetic binding", annotationBinding.isSynthetic());

		node = getASTNode(unit, 2);
		assertEquals("Not a type declaration unit", ASTNode.TYPE_DECLARATION, node.getNodeType());
		typeDeclaration = (TypeDeclaration) node;
		modifiers = typeDeclaration.modifiers();
		assertEquals("wrong size", 1, modifiers.size());
		modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier.isAnnotation());
		annotation = (Annotation) modifier;
		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		normalAnnotation = (NormalAnnotation) annotation;
		IAnnotationBinding annotationBinding2 = normalAnnotation.resolveAnnotationBinding();

		assertTrue("Should be equal", annotationBinding2.isEqualTo(annotationBinding));
		assertTrue("Should be equal", annotationBinding.isEqualTo(annotationBinding2));
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=179042
	 */
	public void test0260() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@interface Annot {\n" +
			"	public int id() default 0;\n" +
			"	public String name() default \"\";\n" +
			"}\n" +
			"@Annot(id=4)\n" +
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				0);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1);
		assertEquals("Not a type declaration unit", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier.isAnnotation());
		Annotation annotation = (Annotation) modifier;
		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		IAnnotationBinding annotationBinding = normalAnnotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 0, annotationBinding.getAnnotations().length);
		IJavaElement javaElement = annotationBinding.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Wrong kind", IBinding.ANNOTATION, annotationBinding.getKind());
		assertEquals("Unexpected key", "LX;@LX~Annot;", annotationBinding.getKey());
		assertEquals("Wrong modifier", Modifier.NONE, annotationBinding.getModifiers());
		assertFalse("Not a deprecated annotation", annotationBinding.isDeprecated());
		IMemberValuePairBinding[] declaredMemberValuePairs = annotationBinding.getDeclaredMemberValuePairs();
		assertEquals("Wrong size", 1, declaredMemberValuePairs.length);
		IMemberValuePairBinding[] allMemberValuePairs = annotationBinding.getAllMemberValuePairs();
		assertEquals("Wrong size", 2, allMemberValuePairs.length);
		assertFalse("Not a recovered binding", annotationBinding.isRecovered());
		assertFalse("Not a synthetic binding", annotationBinding.isSynthetic());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=179065
	 */
	public void test0261() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@interface Annot {\n" +
			"	public boolean booleanValue() default true;\n" +
			"\n" +
			"	public byte byteValue() default 0;\n" +
			"\n" +
			"	public char charValue() default \' \';\n" +
			"\n" +
			"	public double doubleValue() default 0.0;\n" +
			"\n" +
			"	public float floatValue() default 0.0f;\n" +
			"\n" +
			"	public int intValue() default 1;\n" +
			"\n" +
			"	public long longValue() default Long.MAX_VALUE;\n" +
			"\n" +
			"	public short shortValue() default 127;\n" +
			"\n" +
			"	public String stringValue() default \"\";\n" +
			"\n" +
			"	public E enumValue() default E.A;\n" +
			"\n" +
			"	public Class classValue() default String.class;\n" +
			"\n" +
			"	@Deprecated public Ann annotationValue() default @Ann();\n" +
			"\n" +
			"	public boolean[] booleanArrayValue() default { true, false };\n" +
			"\n" +
			"	public byte[] byteArrayValue() default { 0, 1 };\n" +
			"\n" +
			"	public char[] charArrayValue() default { \'#\' };\n" +
			"\n" +
			"	@Deprecated public double[] doubleArrayValue() default { 2.0 };\n" +
			"\n" +
			"	public float[] floatArrayValue() default { 1.0f };\n" +
			"\n" +
			"	public int[] intArrayValue() default { 0, 1 };\n" +
			"\n" +
			"	public long[] longArrayValue() default { Long.MIN_VALUE };\n" +
			"\n" +
			"	public short[] shortArrayValue() default { 127 };\n" +
			"\n" +
			"	public String[] stringArrayValue() default { \"Hello\", \"World\" };\n" +
			"\n" +
			"	public E[] enumArrayValue() default { E.A, E.B };\n" +
			"\n" +
			"	public Class[] classArrayValue() default { Object.class, Annot.class };\n" +
			"\n" +
			"	public Ann[] annotationArrayValue() default {};\n" +
			"}\n" +
			"\n" +
			"enum E {\n" +
			"	A, B, C, D\n" +
			"}\n" +
			"\n" +
			"@interface Ann {}\n" +
			"\n" +
			"@Annot(\n" +
			"	booleanValue = true,\n" +
			"	byteValue = (byte) 1,\n" +
			"	charValue = \' \',\n" +
			"	doubleValue = 4.0,\n" +
			"	floatValue = 3.0f,\n" +
			"	intValue = 1,\n" +
			"	longValue = 65535L,\n" +
			"	shortValue = (short) 128,\n" +
			"	stringValue = \"SUCCESS\",\n" +
			"	enumValue = E.B,\n" +
			"	classValue = Object.class,\n" +
			"	annotationValue = @Ann())\n" +
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				0);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedProblems = "";
		assertProblemsSize(unit, 0, expectedProblems);
		node = getASTNode(unit, 3);
		assertEquals("Not a type declaration unit", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier.isAnnotation());
		Annotation annotation = (Annotation) modifier;
		assertEquals("Not a normal annotation", ASTNode.NORMAL_ANNOTATION, annotation.getNodeType());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		IAnnotationBinding annotationBinding = normalAnnotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 0, annotationBinding.getAnnotations().length);
		IJavaElement javaElement = annotationBinding.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Wrong kind", IBinding.ANNOTATION, annotationBinding.getKind());
		assertEquals("Unexpected key", "LX;@LX~Annot;", annotationBinding.getKey());
		assertEquals("Wrong modifier", Modifier.NONE, annotationBinding.getModifiers());
		assertFalse("Not a deprecated annotation", annotationBinding.isDeprecated());
		IMemberValuePairBinding[] declaredMemberValuePairs = annotationBinding.getDeclaredMemberValuePairs();
		assertEquals("Wrong size", 12, declaredMemberValuePairs.length);

		IMemberValuePairBinding pairBinding = declaredMemberValuePairs[0];
		assertEquals("Wrong name", "booleanValue", pairBinding.getName());
		Object value = pairBinding.getValue();
		assertTrue("Not a Boolean", value instanceof Boolean);
		assertEquals("Wrong value", Boolean.TRUE, value);
		assertTrue("Not the default value", pairBinding.isDefault());
		IMethodBinding methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "booleanValue", methodBinding.getName());
		Object defaultValue = methodBinding.getDefaultValue();
		assertTrue("Different values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[1];
		assertEquals("Wrong name", "byteValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a Byte", value instanceof Byte);
		assertEquals("Wrong value", Byte.valueOf((byte) 1), value);
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "byteValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[2];
		assertEquals("Wrong name", "charValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a Character", value instanceof Character);
		assertEquals("Wrong value",Character.valueOf(' '), value);
		assertTrue("Not the default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "charValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertTrue("Different values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[3];
		assertEquals("Wrong name", "doubleValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a Double", value instanceof Double);
		assertEquals("Wrong value", Double.valueOf(4.0), value);
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "doubleValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[4];
		assertEquals("Wrong name", "floatValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a Float", value instanceof Float);
		assertEquals("Wrong value", Float.valueOf(3.0f), value);
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "floatValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[5];
		assertEquals("Wrong name", "intValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not an Integer", value instanceof Integer);
		assertEquals("Wrong value", Integer.valueOf(1), value);
		assertTrue("Not the default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "intValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertTrue("Different values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[6];
		assertEquals("Wrong name", "longValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a Long", value instanceof Long);
		assertEquals("Wrong value", Long.valueOf(65535L), value);
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "longValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[7];
		assertEquals("Wrong name", "shortValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a Short", value instanceof Short);
		assertEquals("Wrong value", Short.valueOf((short) 128), value);
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "shortValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[8];
		assertEquals("Wrong name", "stringValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not a String", value instanceof String);
		assertEquals("Wrong value", "SUCCESS", value);
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "stringValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[9];
		assertEquals("Wrong name", "enumValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not an IVariableBinding", value instanceof IVariableBinding);
		IVariableBinding variableBinding = (IVariableBinding) value;
		assertEquals("Wrong value", "B", variableBinding.getName());
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "enumValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[10];
		assertEquals("Wrong name", "classValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not an ITypeBinding", value instanceof ITypeBinding);
		ITypeBinding typeBinding = (ITypeBinding) value;
		assertEquals("Wrong value", "java.lang.Object", typeBinding.getQualifiedName());
		assertFalse("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "classValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertFalse("Same values", value.equals(defaultValue));
		assertFalse("Is deprecated", pairBinding.isDeprecated());

		pairBinding = declaredMemberValuePairs[11];
		assertEquals("Wrong name", "annotationValue", pairBinding.getName());
		value = pairBinding.getValue();
		assertTrue("Not an IAnnotationBinding", value instanceof IAnnotationBinding);
		IAnnotationBinding annotationBinding2 = (IAnnotationBinding) value;
		assertEquals("Wrong value", "Ann", annotationBinding2.getName());
		assertTrue("The default value", pairBinding.isDefault());
		methodBinding = pairBinding.getMethodBinding();
		assertNotNull("No method binding", methodBinding);
		assertEquals("Wrong name", "annotationValue", methodBinding.getName());
		defaultValue = methodBinding.getDefaultValue();
		assertTrue("not a IBinding", defaultValue instanceof IBinding);
		assertTrue("Same values", annotationBinding2.isEqualTo((IBinding) defaultValue));
		assertTrue("Not deprecated", pairBinding.isDeprecated());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=166963
	 */
	public void test0262() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	public X(String s) {\n" +
			"	}\n" +
			"	public X() {\n" +
			"		String s = \"\";\n" +
			"		System.out.println();\n" +
			"		this(zork);\n" +
			"		Zork.this.this();\n" +
			"		<Zork>this(s);\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				0);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedErrors = "Constructor call must be the first statement in a constructor\n" +
		"zork cannot be resolved to a variable\n" +
		"Constructor call must be the first statement in a constructor\n" +
		"Zork cannot be resolved to a type\n" +
		"Zork cannot be resolved to a type\n" +
		"Constructor call must be the first statement in a constructor";
		assertProblemsSize(unit, 6, expectedErrors);
		node = getASTNode(unit, 0, 1, 4);
		assertEquals("Not a constructor invocation", ASTNode.CONSTRUCTOR_INVOCATION, node.getNodeType());
		ConstructorInvocation constructorInvocation = (ConstructorInvocation) node;
		assertNull("Got a binding", constructorInvocation.resolveConstructorBinding());
		List arguments = constructorInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size());
		Expression expression = (Expression) arguments.get(0);
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No binding", typeBinding);
		assertEquals("Wrong type", "java.lang.String", typeBinding.getQualifiedName());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=183468
	 */
	public void test0263() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@interface Annot {\n" +
			"	int[] array();\n" +
			"}\n" +
			"@Annot(array=1)\n" +
			"public class X {\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 2, modifiers.size());
		IExtendedModifier modifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", modifier.isAnnotation());
		Annotation annotation = (Annotation) modifier;
		assertTrue("Not a normal annotation", annotation.isNormalAnnotation());
		NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
		List values = normalAnnotation.values();
		assertEquals("Wrong size", 1, values.size());
		MemberValuePair pair = (MemberValuePair) values.get(0);
		IMemberValuePairBinding memberValuePairBinding = pair.resolveMemberValuePairBinding();
		assertFalse("Is default value", memberValuePairBinding.isDefault());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0264() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	private <T> T find(T a, List<T> b) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public void foo1() {\n" +
			"		// T x;\n" +
			"		find(x, Arrays.asList(\"a\")); // closestMatch: #find(String,List<String>)\n" +
			"		find(x, 0); // closestMatch: #find(Object,List<Object>)\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedErrors =
			"x cannot be resolved to a variable\n" +
			"x cannot be resolved to a variable";
		assertProblemsSize(unit, 2, expectedErrors);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		node = getASTNode(unit, 0, 1, 1);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		invocation = (MethodInvocation) expression;
		methodBinding = invocation.resolveMethodBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a raw method", methodBinding.isRawMethod());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0265() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	<T> X(T a, List<T> b) {\n" +
			"	}\n" +
			"\n" +
			"	public void foo1() {\n" +
			"		// T x;\n" +
			"		new X(x, Arrays.asList(\"a\")); // closestMatch:#X(String,List<String>)\n" +
			"		new X(x, 0); // closestMatch: #X(Object,List<Object>)\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedErrors =
			"x cannot be resolved to a variable\n" +
			"x cannot be resolved to a variable";
		assertProblemsSize(unit, 2, expectedErrors);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		node = getASTNode(unit, 0, 1, 1);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		classInstanceCreation = (ClassInstanceCreation) expression;
		methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a raw method", methodBinding.isRawMethod());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0266() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	class M {\n" +
			"		<T> M(T a, List<T> b) {\n" +
			"		}\n" +
			"	}\n" +
			"	public void foo1() {\n" +
			"		// T x;\n" +
			"		this.new M(x, Arrays.asList(\"a\")); // closestMatch: #X(String,List<String>)\n" +
			"		this.new M(x, 0); // closestMatch: #X(Object,List<Object>)\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedErrors =
			"x cannot be resolved to a variable\n" +
			"x cannot be resolved to a variable";
		assertProblemsSize(unit, 2, expectedErrors);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		node = getASTNode(unit, 0, 1, 1);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		classInstanceCreation = (ClassInstanceCreation) expression;
		methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a raw method", methodBinding.isRawMethod());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0267() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	class M {\n" +
			"		<T> M(T a, List<T> b) {\n" +
			"		}\n" +
			"	}\n" +
			"	public void foo1() {\n" +
			"		// T x;\n" +
			"		this.new M(x, Arrays.asList(\"a\")) {\n" +
			"		}; // closestMatch:#X(String,List<String>)\n" +
			"		this.new M(x, 0) {\n" +
			"		}; // closestMatch: #X(Object,List<Object>)\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedErrors =
			"x cannot be resolved to a variable\n" +
			"x cannot be resolved to a variable";
		assertProblemsSize(unit, 2, expectedErrors);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		node = getASTNode(unit, 0, 1, 1);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		statement = (ExpressionStatement) node;
		expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		classInstanceCreation = (ClassInstanceCreation) expression;
		methodBinding = classInstanceCreation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a raw method", methodBinding.isRawMethod());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0268() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.*;\n" +
			"\n" +
			"class Super {\n" +
			"	<T> Super(T a, List<T> b) {\n" +
			"	}\n" +
			"}\n" +
			"public class X extends Super {\n" +
			"	public X() {\n" +
			"		// T x;\n" +
			"		super(x, Arrays.asList(\"a\")); // closestMatch:#X(String,List<String>)\n" +
			"	}\n" +
			"	public X(boolean b) {\n" +
			"		// T x;\n" +
			"		super(x, 0); // closestMatch: #X(Object,List<Object>)\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedErrors =
			"x cannot be resolved to a variable\n" +
			"x cannot be resolved to a variable";
		assertProblemsSize(unit, 2, expectedErrors);
		node = getASTNode(unit, 1, 0, 0);
		assertEquals("Not a super constructor invocation", ASTNode.SUPER_CONSTRUCTOR_INVOCATION, node.getNodeType());
		SuperConstructorInvocation invocation = (SuperConstructorInvocation) node;
		IMethodBinding methodBinding = invocation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		node = getASTNode(unit, 1, 1, 0);
		assertEquals("Not a expression statement", ASTNode.SUPER_CONSTRUCTOR_INVOCATION, node.getNodeType());
		invocation = (SuperConstructorInvocation) node;
		methodBinding = invocation.resolveConstructorBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a raw method", methodBinding.isRawMethod());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=175409
	public void test0269() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.*;\n" +
			"\n" +
			"public class X {\n" +
			"	<T extends Comparable<T>> void find(T a, String[] b, List<T> c) {\n" +
			"	}\n" +
			"	void foo(String[] s) {\n" +
			"		find(x, Arrays.asList(\"a\"), s);\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		String expectedError = "x cannot be resolved to a variable";
		assertProblemsSize(unit, 1, expectedError);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a expression statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation invocation = (MethodInvocation) expression;
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		assertNotNull("No binding", methodBinding);
		assertFalse("Not a parameterized method", methodBinding.isParameterizedMethod());
		assertTrue("Not a parameterized method", methodBinding.isRawMethod());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180966
	public void _test0270() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/foo/X.java", true/*resolve*/);
		String contents =
			"package foo;\n" +
			"\n" +
			"class GenericBase<T> {\n" +
			"        public void someMethod() {}\n" +
			"}\n" +
			"public class X extends GenericBase<String> {\n" +
			"        @Override\n" +
			"        public void someMethod() {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration declaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = declaration.resolveBinding();
		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		declaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = declaration.resolveBinding();
		assertTrue("Doesn't override", methodBinding2.overrides(methodBinding));
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=180966
	public void _test0271() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/foo/X.java", true/*resolve*/);
		String contents =
			"package foo;\n" +
			"\n" +
			"class GenericBase<T> {\n" +
			"        public void someMethod() {}\n" +
			"}\n" +
			"public class X extends GenericBase<String> {\n" +
			"        @Override\n" +
			"        public void someMethod() {}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration declaration = (MethodDeclaration) node;
		IMethodBinding methodBinding = declaration.resolveBinding();
		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		declaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = declaration.resolveBinding();
		IMethodBinding[] declaredMethods = methodBinding.getDeclaringClass().getSuperclass().getDeclaredMethods();
		IMethodBinding methodBinding3 = null;
		loop: for (int i = 0, max = declaredMethods.length; i < max; i++) {
			if (declaredMethods[i].getName().equals(methodBinding.getName())) {
				methodBinding3 = declaredMethods[i];
				break loop;
			}
		}
		assertNotNull("Super method not found", methodBinding3);
		assertTrue("Should be the same", methodBinding3.isEqualTo(methodBinding2));
		assertTrue("Doesn't override", methodBinding.overrides(methodBinding3));
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186189
	public void test0272() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"	<T> T foo(T t) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	Object bar() {\n" +
			"		return new Object() {\n" +
			"			void bar(List<?> l) {\n" +
			"				foo(l.get(0));\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String args[]) {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1, 0);
		assertEquals("Not a return statement", ASTNode.RETURN_STATEMENT, node.getNodeType());
		ReturnStatement statement = (ReturnStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a class instance creation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		AnonymousClassDeclaration anonymousClassDeclaration = classInstanceCreation.getAnonymousClassDeclaration();
		List bodyDeclarations = anonymousClassDeclaration.bodyDeclarations();
		assertEquals("Wrong size", 1, bodyDeclarations.size());
		BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarations.get(0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, bodyDeclaration.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block body = methodDeclaration.getBody();
		assertNotNull("No block", body);
		List statements = body.statements();
		assertEquals("Wrong size", 1, statements.size());
		Statement statement2 = (Statement) statements.get(0);
		assertEquals("Not an expression statement", ASTNode.EXPRESSION_STATEMENT, statement2.getNodeType());
		Expression expression2 = ((ExpressionStatement) statement2).getExpression();
		assertEquals("Not a method invocation", ASTNode.METHOD_INVOCATION, expression2.getNodeType());
		ITypeBinding typeBinding = expression2.resolveTypeBinding();
		assertTrue("Not a capture", typeBinding.isCapture());
		assertNull("No binary type", typeBinding.getBinaryName());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185129
	public void test0273() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"import test0273.B;\n" +
			"import test0273.A;\n" +
			"public class X {\n" +
			"	Object foo() {\n" +
			"		return new B(new A());\n" +
			"	}\n" +
			"	void bar(String s) {\n" +
			"		System.out.println(s);\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191908
	public void test0274() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	@Deprecated\n" +
			"	public static int x= 5, y= 10;\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 2, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding binding = fragment.resolveBinding();
		assertTrue("Not deprecated", binding.isDeprecated());
		fragment = (VariableDeclarationFragment) fragments.get(1);
		binding = fragment.resolveBinding();
		assertTrue("Not deprecated", binding.isDeprecated());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191908
	public void test0275() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	public void foo() {\n" +
			"		@Deprecated\n" +
			"		int x= 5, y= 10;\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 2, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding binding = fragment.resolveBinding();
		IAnnotationBinding[] annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		fragment = (VariableDeclarationFragment) fragments.get(1);
		binding = fragment.resolveBinding();
		annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192774
	//Test ability to distinguish AST nodes of multiple similar annotations.
	public void test0276() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@interface Annot {\n" +
			"  public int value();\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"  @Annot(1) String foo1() { return null; }\n" +
			"  @Annot(1) String foo2() { return null; }\n" +
			"}";
		this.workingCopy.getBuffer().setContents(contents);

		class CompilationUnitRequestor extends ASTRequestor {
			public void acceptAST(ICompilationUnit source, CompilationUnit node) {
				MethodDeclaration methodDeclaration = (MethodDeclaration)getASTNode(node, 1, 0);
				IMethodBinding methodBinding = methodDeclaration.resolveBinding();
				IAnnotationBinding annoBinding = methodBinding.getAnnotations()[0];
				ASTNode annoNode = node.findDeclaringNode(annoBinding);
				int position1 = annoNode.getStartPosition();

				methodDeclaration = (MethodDeclaration)getASTNode(node, 1, 1);
				methodBinding = methodDeclaration.resolveBinding();
				IAnnotationBinding annoBinding2 = methodBinding.getAnnotations()[0];
				annoNode = node.findDeclaringNode(annoBinding2);
				int position2 = annoNode.getStartPosition();
				assertTrue("Anno 2 position <= anno 1 position", position2 > position1);
			}
		}

		CompilationUnitRequestor requestor = new CompilationUnitRequestor();
		ASTParser parser = ASTParser.newParser(getJLS4());
		parser.setResolveBindings(true);
		parser.setProject(getJavaProject("Converter15"));
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.createASTs(new ICompilationUnit[]{this.workingCopy}, new String[0], requestor, null);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191908
	public void test0277() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	public static void method() {\n" +
			"	}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	public static void method() {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding1 = methodDeclaration.resolveBinding();

		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();

		assertFalse("Overrides", methodBinding2.overrides(methodBinding1));
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191908
	public void test0278() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	public void method() {\n" +
			"	}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	public static void method() {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "This static method cannot hide the instance method from X");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding1 = methodDeclaration.resolveBinding();

		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();

		assertFalse("Overrides", methodBinding2.overrides(methodBinding1));
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191908
	public void test0279() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	public static void method() {\n" +
			"	}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	public void method() {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "This instance method cannot override the static method from X");
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding1 = methodDeclaration.resolveBinding();

		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();

		assertFalse("Overrides", methodBinding2.overrides(methodBinding1));
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191908
	public void test0280() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"public class X {\n" +
			"	public void method() {\n" +
			"	}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	@Override\n" +
			"	public void method() {\n" +
			"	}\n" +
			"}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding1 = methodDeclaration.resolveBinding();

		node = getASTNode(unit, 1, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		methodDeclaration = (MethodDeclaration) node;
		IMethodBinding methodBinding2 = methodDeclaration.resolveBinding();

		assertTrue("Doesn't overrides", methodBinding2.overrides(methodBinding1));
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=198085
	public void test0281() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		String contents =
			"@Invalid\n" +
			"@Deprecated\n" +
			"public class X {}";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false,
				false,
				true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Invalid cannot be resolved to a type");
		node = getASTNode(unit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals("wrong size", 2, annotations.length);
		assertEquals("LX;@LInvalid;", annotations[0].getKey());
		assertTrue("Annotation should be flagged as recovered", annotations[0].isRecovered());
		assertTrue("Annotation type should be flagged as recovered", annotations[0].getAnnotationType().isRecovered());
		assertEquals("LX;@Ljava/lang/Deprecated;", annotations[1].getKey());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=190622
	public void test0282() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public @interface Moo {\n" +
			"		Class<?> value();\n" +
			"	}\n" +
			"	@Moo(Bar.Baz.class)\n" +
			"	public static class Bar {\n" +
			"		public static class Baz {\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		final List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 3, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", extendedModifier instanceof SingleMemberAnnotation);
		SingleMemberAnnotation annotation = (SingleMemberAnnotation) extendedModifier;
		final Expression value = annotation.getValue();
		assertEquals("Not a type literal", ASTNode.TYPE_LITERAL, value.getNodeType());
		TypeLiteral typeLiteral = (TypeLiteral) value;
		final Type type = typeLiteral.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		final Name name = simpleType.getName();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		final IBinding binding = qualifiedName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong value", "Bar.Baz", qualifiedName.getFullyQualifiedName());
		final SimpleName simpleName = qualifiedName.getName();
		final IBinding binding2 = simpleName.resolveBinding();
		assertNotNull("No binding2", binding2);
		assertFalse("Not a recovered binding", binding2.isRecovered());
		final Name qualifier = qualifiedName.getQualifier();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, qualifier.getNodeType());
		SimpleName simpleName2 = (SimpleName) qualifier;
		final IBinding binding3 = simpleName2.resolveBinding();
		assertNotNull("No binding3", binding3);
		assertFalse("Not a recovered binding", binding3.isRecovered());
		final IJavaElement javaElement = binding3.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Not a type", IJavaElement.TYPE, javaElement.getElementType());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201104
	public void test0283() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public @interface Moo {\n" +
			"		Class<?> value();\n" +
			"	}\n" +
			"	@Moo(Bar2.Baz.class)\n" +
			"	public static class Bar {\n" +
			"		public static class Baz {\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Bar2 cannot be resolved to a type");
		node = getASTNode(unit, 0, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		final List modifiers = typeDeclaration.modifiers();
		assertEquals("Wrong size", 3, modifiers.size());
		IExtendedModifier extendedModifier = (IExtendedModifier) modifiers.get(0);
		assertTrue("Not an annotation", extendedModifier instanceof SingleMemberAnnotation);
		SingleMemberAnnotation annotation = (SingleMemberAnnotation) extendedModifier;
		final Expression value = annotation.getValue();
		assertEquals("Not a type literal", ASTNode.TYPE_LITERAL, value.getNodeType());
		TypeLiteral typeLiteral = (TypeLiteral) value;
		final Type type = typeLiteral.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		SimpleType simpleType = (SimpleType) type;
		final Name name = simpleType.getName();
		assertEquals("Not a qualified name", ASTNode.QUALIFIED_NAME, name.getNodeType());
		QualifiedName qualifiedName = (QualifiedName) name;
		final IBinding binding = qualifiedName.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong value", "Bar2.Baz", qualifiedName.getFullyQualifiedName());
		final Name qualifier = qualifiedName.getQualifier();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, qualifier.getNodeType());
		SimpleName simpleName2 = (SimpleName) qualifier;
		final IBinding binding3 = simpleName2.resolveBinding();
		assertNotNull("No binding3", binding3);
		assertTrue("Not a recovered binding", binding3.isRecovered());
		final IJavaElement javaElement = binding3.getJavaElement();
		assertNotNull("No java element", javaElement);
		assertEquals("Not a compilation unit", IJavaElement.TYPE, javaElement.getElementType());
		assertNotNull("No parent", javaElement.getParent());
	}


	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=203342
	 */
	public void test0284() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public static final double VAR = 0x0.0000000000001P-1022;\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
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
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "0x0.0000000000001P-1022", contents);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=223488
	 */
	public void test0285() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X.java", true/* resolve */);
		PackageDeclaration packageDeclaration = (PackageDeclaration) buildAST(
			"/*start*/package p;/*end*/\n" +
			"public class X {\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/);
		IPackageBinding packageBinding = packageDeclaration.resolveBinding();
		try {
			startLogListening();
			packageBinding.getAnnotations();
			assertLogEquals("");
		} finally {
			stopLogListening();
		}
	}

	/**
	 * bug187430: Unresolved types surfacing through DOM AST for annotation default values
	 * test That the qualified name of the default value does not contain any '$' character
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=187430"
	 */
	public void testBug187430() throws JavaModelException {
    	this.workingCopy = getWorkingCopy("/Converter15/src/b187430/Test.java", true/*resolve*/);
    	String contents =
    		"package b187430;\n" +
    		"@C\n" +
    		"public class Test {}\n";
	   	ASTNode node = buildAST(
				contents,
    			this.workingCopy,
    			false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0, "");
		List types = unit.types();
		assertEquals("Wrong size", 1, types.size());
		AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) types.get(0);
		assertEquals("Wrong type", ASTNode.TYPE_DECLARATION, abstractTypeDeclaration.getNodeType());
		TypeDeclaration declaration = (TypeDeclaration) abstractTypeDeclaration;
		ITypeBinding typeBinding = declaration.resolveBinding();
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		IMemberValuePairBinding[] allMemberValuePairs = annotations[0].getAllMemberValuePairs();
		assertEquals("Expected 'intval' and 'classval' member pair values", 2, allMemberValuePairs.length);
		IMethodBinding methodBinding = allMemberValuePairs[0].getMethodBinding();
		Object defaultValue = methodBinding.getDefaultValue();
		ITypeBinding iTypeBinding = (ITypeBinding) defaultValue;
		assertEquals("Unexpected default value", "b187430.A.B", iTypeBinding.getQualifiedName());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228691
	 */
	public void test0286() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0286/X.java", true/*resolve*/);
		String contents =
			"package test0286;\n" +
			"public class X {\n" +
			"	int i;\n" +
			"	Integer integer;\n" +
			"}\n";
		ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		ASTNode node2 = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node2.getNodeType());
		FieldDeclaration declaration = (FieldDeclaration) node2;
		ITypeBinding typeBinding = declaration.getType().resolveBinding();
		node2 = getASTNode(unit, 0, 1);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node2.getNodeType());
		declaration = (FieldDeclaration) node2;
		ITypeBinding typeBinding2 = declaration.getType().resolveBinding();
		assertEquals("Wrong type", "int", typeBinding.getName());
		assertEquals("Wrong type", "Integer", typeBinding2.getName());
		assertTrue("Not assignmentCompatible: Integer -> int", typeBinding2.isAssignmentCompatible(typeBinding));
		assertTrue("Not assignmentCompatible: int -> Integer", typeBinding.isAssignmentCompatible(typeBinding2));
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0287() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0287/A.java", true/*resolve*/);
		MemberValuePair pair = (MemberValuePair) buildAST(
			"package test0287;\n" +
			"@ABC (/*start*/name1=\"\"/*end*/)\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IMemberValuePairBinding resolveMemberValuePairBinding = pair.resolveMemberValuePairBinding();
		assertNull("Got a binding", resolveMemberValuePairBinding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0288() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0288/A.java", true/*resolve*/);
		MemberValuePair pair = (MemberValuePair) buildAST(
			"package test0288;\n" +
			"@ABC (/*start*/name1=\"\"/*end*/)\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		IMemberValuePairBinding resolveMemberValuePairBinding = pair.resolveMemberValuePairBinding();
		assertNull("Got a binding", resolveMemberValuePairBinding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0289() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0289/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0289;\n" +
			"/*start*/@ABC (name1=\"\")/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertNull("No binding", resolveAnnotationBinding);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0290() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0290/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0290;\n" +
			"/*start*/@ABC (name1=\"\")/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertTrue("Not recovered", resolveAnnotationBinding.isRecovered());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0291() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0291/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0291;\n" +
			"/*start*/@ABC (name1=\"\")/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 0, resolveAnnotationBinding.getAllMemberValuePairs().length);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0292() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0292/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0292;\n" +
			"@interface ABC {\n" +
			"	String name1() default \"\";\n" +
			"}\n" +
			"/*start*/@ABC(name1=\"\", id=0)/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertFalse("Recovered", resolveAnnotationBinding.isRecovered());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0293() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0293/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0293;\n" +
			"@interface ABC {\n" +
			"	String name1() default \"\";\n" +
			"}\n" +
			"/*start*/@ABC(name1=\"\", id=0)/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 1, resolveAnnotationBinding.getAllMemberValuePairs().length);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0294() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0294/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0294;\n" +
			"@interface ABC {\n" +
			"	String name1() default \"\";\n" +
			"}\n" +
			"/*start*/@ABC(name1=\"\", id=0)/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 1, resolveAnnotationBinding.getDeclaredMemberValuePairs().length);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0295() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0295/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0295;\n" +
			"@interface ABC {\n" +
			"	String name1() default \"\";\n" +
			"}\n" +
			"/*start*/@ABC(id=0)/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 1, resolveAnnotationBinding.getAllMemberValuePairs().length);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0296() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0296/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0296;\n" +
			"@interface ABC {\n" +
			"	String name1() default \"\";\n" +
			"}\n" +
			"/*start*/@ABC(id=0)/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 1, resolveAnnotationBinding.getAllMemberValuePairs().length);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228651
	 */
	public void test0297() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0297/A.java", true/*resolve*/);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
			"package test0297;\n" +
			"@interface ABC {\n" +
			"	String name1() default \"\";\n" +
			"}\n" +
			"/*start*/@ABC(name1=\"\", id=0)/*end*/\n" +
			"public class A {}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		IAnnotationBinding resolveAnnotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("Wrong size", 1, resolveAnnotationBinding.getDeclaredMemberValuePairs().length);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0298() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0298/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test0298;\n" +
			"import java.util.List;\n" +
			"public interface X {\n" +
			"	/*start*/List<IEntity>/*end*/ foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		ITypeBinding binding = type.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0299() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0299/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test0299;\n" +
			"public interface X {\n" +
			"	/*start*/List<IEntity>/*end*/ foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		ITypeBinding binding = type.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0300() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0300/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test0300;\n" +
			"public interface X {\n" +
			"	/*start*/ArrayList<IEntity>/*end*/ foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		ITypeBinding binding = type.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0301() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0301/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test03018;\n" +
			"import java.util.List;\n" +
			"public interface X {\n" +
			"	/*start*/List<IEntity>/*end*/ foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0302() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0302/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test0302;\n" +
			"public interface X {\n" +
			"	/*start*/List<IEntity>/*end*/ foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0303() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0303/X.java", true/*resolve*/);
		ParameterizedType type = (ParameterizedType) buildAST(
			"package test0303;\n" +
			"public interface X {\n" +
			"	/*start*/ArrayList<IEntity>/*end*/ foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0304() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0304/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test0304;\n" +
			"public interface X {\n" +
			"	ArrayList</*start*/IEntity/*end*/> foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0305() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0305/X.java", true/*resolve*/);
		Type type = (Type) buildAST(
			"package test0305;\n" +
			"public interface X {\n" +
			"	ArrayList</*start*/IEntity/*end*/> foo();\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		ITypeBinding binding = type.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0306() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0306/X.java", true/*resolve*/);
		VariableDeclarationStatement statement= (VariableDeclarationStatement) buildAST(
			"package test0306;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		/*start*/ArrayList<IEntity> list;/*end*/\n" +
			"	}\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		List fragments = statement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding binding = fragment.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0307() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0307/X.java", true/*resolve*/);
		VariableDeclarationStatement statement= (VariableDeclarationStatement) buildAST(
			"package test0307;\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		/*start*/ArrayList<IEntity> list;/*end*/\n" +
			"	}\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		List fragments = statement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding binding = fragment.resolveBinding();
		assertNotNull("No binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0308() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0308/X.java", true/*resolve*/);
		MethodDeclaration declaration= (MethodDeclaration) buildAST(
			"package test0308;\n" +
			"public class X {\n" +
			"	/*start*/ArrayList<IEntity> foo() {\n" +
			"		 return null;\n" +
			"	}/*end*/\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		IMethodBinding binding = declaration.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0309() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0309/X.java", true/*resolve*/);
		MethodDeclaration declaration= (MethodDeclaration) buildAST(
			"package test0309;\n" +
			"public class X {\n" +
			"	/*start*/ArrayList<IEntity> foo() {\n" +
			"		 return null;\n" +
			"	}/*end*/\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IMethodBinding binding = declaration.resolveBinding();
		assertNotNull("No binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0310() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0310/X.java", true/*resolve*/);
		MethodDeclaration declaration= (MethodDeclaration) buildAST(
			"package test0310;\n" +
			"public class X {\n" +
			"	/*start*/void foo(ArrayList<IEntity> list) {\n" +
			"	}/*end*/\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			false);
		IMethodBinding binding = declaration.resolveBinding();
		assertNull("Got a binding", binding);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=230127
	 */
	public void test0311() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0311/X.java", true/*resolve*/);
		MethodDeclaration declaration= (MethodDeclaration) buildAST(
			"package test0311;\n" +
			"public class X {\n" +
			"	/*start*/void foo(ArrayList<IEntity> list) {\n" +
			"	}/*end*/\n" +
			"}",
			this.workingCopy,
			false/*don't report errors*/,
			true,
			true);
		IMethodBinding binding = declaration.resolveBinding();
		assertNotNull("No binding", binding);
	}

	/*
	 * Ensures that requesting a type binding with a non-existing parameterized type doesn't throw an OutOfMemoryError
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=233625 )
	 */
	public void test0312() throws JavaModelException {
		String[] bindingKeys =  new String[] {"Ljava/util/Map<Ljava/lang/Class<Ljava/lang/Class;*>;Ljava/util/List<LUnknown;>;>;"};
		BindingRequestor requestor = new BindingRequestor();
		resolveASTs(new ICompilationUnit[] {} , bindingKeys, requestor, getJavaProject("Converter15"), null);
		assertBindingsEqual(
				"<null>",
				requestor.getBindings(bindingKeys));
	}

	/*
	 * Ensures that requesting a method binding with a non-existing parameterized type doesn't throw an OutOfMemoryError
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=233625 )
	 */
	public void test0313() throws JavaModelException {
		String[] bindingKeys =  new String[] {"Ljava/util/Collections;.emptyMap<K:Ljava/lang/Object;V:Ljava/lang/Object;>()Ljava/util/Map<TK;TV;>;%<Ljava/lang/Class<Ljava/lang/Class;*>;Ljava/util/List<LUnknown;>;>"};
		BindingRequestor requestor = new BindingRequestor();
		resolveASTs(new ICompilationUnit[] {} , bindingKeys, requestor, getJavaProject("Converter15"), null);
		assertBindingsEqual(
				"<null>",
				requestor.getBindings(bindingKeys));
	}

	/*
	 * Ensures that requesting a type binding with a non-existing parameterized type returns null
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=233625 )
	 */
	public void test0314() throws JavaModelException {
		String[] bindingKeys =  new String[] {"Ljava/util/List<LZork;>.Map<Ljava/lang/Object;Ljava/lang/Number;>;"};
		BindingRequestor requestor = new BindingRequestor();
		resolveASTs(new ICompilationUnit[] {} , bindingKeys, requestor, getJavaProject("Converter15"), null);
		assertBindingsEqual(
				"<null>",
				requestor.getBindings(bindingKeys));
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=120082
	 */
	public void test0315() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/pack1/E.java", true/*resolve*/);
		ASTNode node = buildAST(
				"package pack1;\n" +
				"public class E<X> {\n" +
				"	public static <T> E<T> bar(T t) {\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	public void foo(E<?> e) {\n" +
				"		/*start*/bar(e)/*end*/;\n" +
				"	}\n" +
				"}",
				this.workingCopy);
		IBinding binding = ((MethodInvocation) node).resolveTypeBinding();
		assertBindingKeyEquals(
				"Lpack1/E<Lpack1/E<!Lpack1/E;{0}*122;>;>;",
				binding.getKey());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=239439
	 */
	public void test0316() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0316/X.java", true/*resolve*/);
		ClassInstanceCreation expression = (ClassInstanceCreation) buildAST(
				"package test0316;\n" +
				"class AbstractClass {\n" +
				"	XXList<Class> statements = null;\n" +
				"}\n" +
				"import java.util.ArrayList;\n" +
				"public class X extends AbstractClass {\n" +
				"	public List<Class> compute() {\n" +
				"		statements = /*start*/new ArrayList<Class>()/*end*/;\n" +
				"		return statements;\n" +
				"	}\n" +
				"}",
				this.workingCopy,
				false,
				true,
				true);
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=239439
	 */
	public void test0317() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0317/X.java", true/*resolve*/);
		ClassInstanceCreation expression = (ClassInstanceCreation) buildAST(
				"package test0317;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	XXList<Class> statements = null;\n" +
				"	public List<Class> compute() {\n" +
				"		statements = /*start*/new ArrayList<Class>()/*end*/;\n" +
				"		return statements;\n" +
				"	}\n" +
				"}",
				this.workingCopy,
				false,
				true,
				true);
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertNotNull("No type binding", typeBinding);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=218500
	 */
	public void test0318() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0317/X.java", true/*resolve*/);
		SimpleType type = (SimpleType) buildAST(
				"class X {\n" +
				"	{\n" +
				"		abstract class B<T> {\n" +
				"			abstract class A {}\n" +
				"			public void foo() {\n" +
				"				new /*start*/A/*end*/() {};\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				this.workingCopy,
				false,
				true,
				true);
		ITypeBinding typeBinding = type.getName().resolveTypeBinding();
		assertEquals("Not an empty name", 0, typeBinding.getQualifiedName().length());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=212034
	 */
	public void test0319() throws JavaModelException {
		String contents =
			"package test0319;\n" +
			"public class Test {\n" +
			"	/*start*/@Deprecated\n" +
			"	@Invalid\n" +
			"	public void foo() {}/*end*/" +
			"}\n";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0319/Test.java",
				contents,
				true/*resolve*/
			);
		MethodDeclaration methodDeclaration = (MethodDeclaration) buildAST(contents, this.workingCopy, false, false, false);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = methodBinding.getAnnotations();
		assertEquals("Got more than one annotation binding", 1, annotations.length);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=212034
	 */
	public void test0320() throws JavaModelException {
		String contents =
			"package test0320;\n" +
			"public class Test {\n" +
			"	/*start*/@Deprecated\n" +
			"	@Invalid\n" +
			"	public int i;/*end*/" +
			"}\n";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0320/Test.java",
				contents,
				true/*resolve*/
			);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) buildAST(contents, this.workingCopy, false, false, false);
		List fragments = fieldDeclaration.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		IAnnotationBinding[] annotations = variableBinding.getAnnotations();
		assertEquals("Got more than one annotation binding", 1, annotations.length);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=103643
	 */
	public void _test0321() throws JavaModelException {
		String contents =
			"package test0321;\n" +
			"import java.util.*;\n" +
			"class X {\n" +
			"	<T extends Collection<? extends Number>> T getLonger(T t1, T t2) {\n" +
			"		return t1.size() > t2.size() ? t1 : t2;\n" +
			"	}\n" +
			"	void m(HashSet<? extends Double> list, ArrayList<? extends Integer> set) {\n" +
			"		/*start*/getLonger(list, set)/*end*/;\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0321/X.java",
				contents,
				true/*resolve*/
			);
		MethodInvocation invocation = (MethodInvocation) buildAST(contents, this.workingCopy, true, true, true);
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		System.out.println(methodBinding.getReturnType().getQualifiedName());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=242933
	 */
	public void test0322() throws JavaModelException {
		String contents =
			"package test0322;\n" +
			"@interface Range {\n" +
			"	long min() default -9223372036854775808L;\n" +
			"	long max() default 9223372036854775807L;\n" +
			"	String message() default \"\";\n" +
			"}\n" +
			"public class X {\n" +
			"	private int id;\n" +
			"	/*start*/@Range(max=9999999999999999)/*end*/\n" +
			"	public long getId() {\n" +
			"		return id;\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0322/X.java",
				contents,
				true/*resolve*/
			);
		NormalAnnotation annotation = (NormalAnnotation) buildAST(contents, this.workingCopy, false, true, true);
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		IMemberValuePairBinding[] memberValuePairBindings = annotationBinding.getDeclaredMemberValuePairs();
		IMemberValuePairBinding pairBinding = memberValuePairBindings[0];
		assertNull("Got a value", pairBinding.getValue());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=245563
	 */
	public void test0323() throws JavaModelException {
		String contents =
			"class X {\n" +
			"	{\n" +
			"		for(Object obj:\n" +
			"			new Object[]{\n" +
			"				new Object(){\n" +
			"					int field=method(\n" +
			"					});\n" +
			"				}\n" +
			"			});\n" +
			"	}\n" +
			"	int method(int...args){\n" +
			"		return args.length;\n" +
			"	}\n" +
			"}\n" +
			"";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0322/X.java",
				contents,
				true/*resolve*/
			);
		assertNotNull("No node", buildAST(contents, this.workingCopy, false, true, true));
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=270367
	public void test0324() throws JavaModelException {
		String contents = "package test0324;\n"
			+ "public class X {\n"
			+ "  public void someMethod() {\n"
			+ "     int i = /*start*/(new Integer(getId())).intValue()/*end*/;\n"
			+ "  }\n"
			+ "  public String getId() {\n"
			+ "     return null;\n"
			+ "  }\n"
			+ "}";
		this.workingCopy = getWorkingCopy("/Converter15/src/test0324/X.java", contents, true/*resolve*/
		);
		MethodInvocation methodCall = (MethodInvocation) buildAST(contents, this.workingCopy, false, true, true);
		ParenthesizedExpression intValueReceiver = (ParenthesizedExpression) methodCall.getExpression();
		ParenthesizedExpression newParenthesizedExpression = (ParenthesizedExpression) ASTNode.copySubtree(
				intValueReceiver.getAST(), intValueReceiver);
		replaceNodeInParent(methodCall, newParenthesizedExpression);

		// copied node
		ClassInstanceCreation constructorCall = (ClassInstanceCreation) newParenthesizedExpression.getExpression();
		constructorCall.resolveTypeBinding();
		IMethodBinding constructorBinding = constructorCall.resolveConstructorBinding();
		assertNull("Not null constructor binding", constructorBinding);

		// original node
		constructorCall = (ClassInstanceCreation) intValueReceiver.getExpression();
		constructorCall.resolveTypeBinding(); // This should not throw a NPE
		constructorBinding = constructorCall.resolveConstructorBinding();
		assertNotNull("Null constructor binding", constructorBinding);
	}

	// Utility method to replace "node" by "replacement"
	private void replaceNodeInParent(Expression node, Expression replacement) {
		StructuralPropertyDescriptor loc = node.getLocationInParent();
		if (loc.isChildProperty()) {
			node.getParent().setStructuralProperty(loc, replacement);
		}
		else {
			List l = (List) node.getParent().getStructuralProperty(loc);
			for (int i = 0; i < l.size(); i++) {
				if (node.equals(l.get(i))) {
					l.set(i, replacement);
					break;
				}
			}
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0325() throws JavaModelException {
		String contents =
			"package test0325;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0325/Y.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getJavaProject().findType("test0325.X").getAnnotations();
		assertAnnotationsEqual("@test0325.SecondaryTables({@test0325.SecondaryTable(name=\"FOO\"), @test0325.SecondaryTable(name=\"BAR\")})\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0326() throws JavaModelException {
		String contents =
			"package test0326;\n" +
			"@SecondaryTables({@SecondaryTable(name=\"FOO\"), @SecondaryTable(name=\"BAR\")})\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0326/X.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getType("X").getAnnotations();
		assertAnnotationsEqual("@SecondaryTables({@SecondaryTable(name=\"FOO\"), @SecondaryTable(name=\"BAR\")})\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0327() throws JavaModelException {
		String contents =
			"package test0327;\n" +
			"@SecondaryTables({@SecondaryTable(name=\"FOO\"), @SecondaryTable(name=\"BAR\")})\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0327/X.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getType("X").getAnnotations();
		assertAnnotationsEqual("@SecondaryTables({@SecondaryTable(name=\"FOO\"), @SecondaryTable(name=\"BAR\")})\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0328() throws JavaModelException {
		String contents =
			"package test0328;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0328/Y.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getJavaProject().findType("test0328.X").getAnnotations();
		assertAnnotationsEqual("@test0328.JoinTable(name=\"EMP_PROJ\", joinColumns={@test0328.JoinColumn(name=\"EMP_ID\", referencedColumnName=\"EMP_ID\")}, inverseJoinColumns={@test0328.JoinColumn(name=\"PROJ_ID\", referencedColumnName=\"PROJ_ID\")})\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0329() throws JavaModelException {
		String contents =
			"package test0329;\n" +
			"@JoinTable(\n" +
			"	name=\"EMP_PROJ\",\n" +
			"	joinColumns = {\n" +
			"			@JoinColumn(name = \"EMP_ID\", referencedColumnName = \"EMP_ID\")\n" +
			"	},\n" +
			"	inverseJoinColumns = {\n" +
			"			@JoinColumn(name = \"PROJ_ID\", referencedColumnName = \"PROJ_ID\")\n" +
			"	}\n" +
			")\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0329/X.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getType("X").getAnnotations();
		assertAnnotationsEqual("@JoinTable(name=\"EMP_PROJ\", joinColumns={@JoinColumn(name=\"EMP_ID\", referencedColumnName=\"EMP_ID\")}, inverseJoinColumns={@JoinColumn(name=\"PROJ_ID\", referencedColumnName=\"PROJ_ID\")})\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0330() throws JavaModelException {
		String contents =
			"package test0330;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0330/Y.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getJavaProject().findType("test0330.X").getAnnotations();
		assertAnnotationsEqual("@test0330.JoinTable(name=\"EMP_PROJ\", joinColumns=@test0330.JoinColumn(name=\"EMP_ID\", referencedColumnName=\"EMP_ID\"), inverseJoinColumns=@test0330.JoinColumn(name=\"PROJ_ID\", referencedColumnName=\"PROJ_ID\"))\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0331() throws JavaModelException {
		String contents =
			"package test0331;\n" +
			"@JoinTable(\n" +
			"	name=\"EMP_PROJ\",\n" +
			"	joinColumns = @JoinColumn(name = \"EMP_ID\", referencedColumnName = \"EMP_ID\"),\n" +
			"	inverseJoinColumns = @JoinColumn(name = \"PROJ_ID\", referencedColumnName = \"PROJ_ID\")\n" +
			")\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0331/X.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getType("X").getAnnotations();
		assertAnnotationsEqual("@JoinTable(name=\"EMP_PROJ\", joinColumns=@JoinColumn(name=\"EMP_ID\", referencedColumnName=\"EMP_ID\"), inverseJoinColumns=@JoinColumn(name=\"PROJ_ID\", referencedColumnName=\"PROJ_ID\"))\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0332() throws JavaModelException {
		String contents =
			"package test0332;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0332/Y.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getJavaProject().findType("test0332.X").getAnnotations();
		assertAnnotationsEqual("@test0332.JoinTable(name=\"EMP_PROJ\", joinColumns=@test0332.JoinColumn(name=\"EMP_ID\", referencedColumnClass=java.lang.Object.class), inverseJoinColumns=@test0332.JoinColumn(name=\"PROJ_ID\", referencedColumnClass=java.lang.Class.class), getLocalClass=java.lang.String.class)\n", annotations);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=271561
	 */
	public void test0333() throws JavaModelException {
		String contents =
			"package test0333;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0333/Y.java",
				contents,
				true/*resolve*/
			);
		IAnnotation[] annotations = this.workingCopy.getJavaProject().findType("test0333.X").getAnnotations();
		assertAnnotationsEqual("@test0333.JoinTable(name=\"EMP_PROJ\", joinColumns=@test0333.JoinColumn(name=\"EMP_ID\", referencedColumnClass=java.lang.Class.class), inverseJoinColumns=@test0333.JoinColumn(name=\"PROJ_ID\", referencedColumnClass=java.lang.Class.class), getLocalClass=java.lang.String.class)\n", annotations);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286407
	public void test0334() throws CoreException, IOException {
		String contents =
			"package test0334;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0334/Y.java",
				contents,
				true/*resolve*/
			);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String typeName = "test0334.MyAnnotation";
		class BindingRequestor extends ASTRequestor {
			ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
					this._result = (ITypeBinding) binding;
			}
		}
		String[] keys = new String[] {
			BindingKey.createTypeBindingKey(typeName)
		};
		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(getJLS4());
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		// this doesn't really do a parse; it's a type lookup
		parser.createASTs(new ICompilationUnit[] {}, keys, requestor, null);
		ITypeBinding typeBinding = requestor._result;
		assertFalse("Is from source", typeBinding.isFromSource());
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals("Wrong number", 1, annotations.length);
		IMemberValuePairBinding[] allMemberValuePairs = annotations[0].getAllMemberValuePairs();
		assertEquals("Wrong number", 1, allMemberValuePairs.length);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286407
	public void test0335() throws CoreException, IOException {
		String contents =
			"package test0335;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0335/Y.java",
				contents,
				true/*resolve*/
			);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String typeName = "test0335.MyAnnotation";
		class BindingRequestor extends ASTRequestor {
			ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
					this._result = (ITypeBinding) binding;
			}
		}
		String[] keys = new String[] {
			BindingKey.createTypeBindingKey(typeName)
		};
		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(getJLS4());
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		// this doesn't really do a parse; it's a type lookup
		parser.createASTs(new ICompilationUnit[] {}, keys, requestor, null);
		ITypeBinding typeBinding = requestor._result;
		assertFalse("Is from source", typeBinding.isFromSource());
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals("Wrong number", 1, annotations.length);
		IMemberValuePairBinding[] allMemberValuePairs = annotations[0].getAllMemberValuePairs();
		assertEquals("Wrong number", 1, allMemberValuePairs.length);
		IMemberValuePairBinding memberValuePair = allMemberValuePairs[0];
		IVariableBinding variableBinding = (IVariableBinding) memberValuePair.getValue();
		assertEquals("Wrong field", "RUNTIME", variableBinding.getName());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=286407
	public void test0336() throws CoreException, IOException {
		String contents =
			"package test0336;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter15/src/test0336/Y.java",
				contents,
				true/*resolve*/
			);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String typeName = "test0336.MyAnnotation";
		class BindingRequestor extends ASTRequestor {
			ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
					this._result = (ITypeBinding) binding;
			}
		}
		String[] keys = new String[] {
			BindingKey.createTypeBindingKey(typeName)
		};
		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(getJLS4());
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		// this doesn't really do a parse; it's a type lookup
		parser.createASTs(new ICompilationUnit[] {}, keys, requestor, null);
		ITypeBinding typeBinding = requestor._result;
		assertFalse("Is from source", typeBinding.isFromSource());
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals("Wrong number", 1, annotations.length);
		IMemberValuePairBinding[] allMemberValuePairs = annotations[0].getAllMemberValuePairs();
		assertEquals("Wrong number", 1, allMemberValuePairs.length);
		IMemberValuePairBinding memberValuePair = allMemberValuePairs[0];
		IVariableBinding variableBinding = (IVariableBinding) memberValuePair.getValue();
		assertEquals("Wrong field", "CLASS", variableBinding.getName());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=287701
	 */
	public void test0337() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"        void m() {\n" +
			"                int x= 1      ;\n" +
			"                int y= - 1  , z=0   ;\n" +
			"                // Assignment nodes too long:\n" +
			"                int a= x = 2      ;\n" +
			"                System.out.print(    x=1     );\n" +
			"                java.util.Arrays.asList(    x = 1    /*bla*/  , x= 2\n" +
			"                        // comment      \n" +
			"                );\n" +
			"        }\n" +
			"}\n" +
			"";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		ASTNode node = getASTNode(unit, 0, 0, 2);
		checkSourceRange(node, "int a= x = 2      ;", contents);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) node).fragments().get(0);
		checkSourceRange(fragment, "a= x = 2", contents);
		node = getASTNode(unit, 0, 0, 3);
		Expression expression = (Expression) ((MethodInvocation) ((ExpressionStatement) node).getExpression()).arguments().get(0);
		checkSourceRange(expression, "x=1", contents);
		node = getASTNode(unit, 0, 0, 4);
		List arguments = ((MethodInvocation) ((ExpressionStatement) node).getExpression()).arguments();
		ASTNode node2 = (ASTNode) arguments.get(0);
		checkSourceRange(node2, "x = 1", contents);
		checkSourceRange((ASTNode) arguments.get(1), "x= 2", contents);
		int extendedLength = unit.getExtendedLength(node2);
		int extendedStartPosition = unit.getExtendedStartPosition(node2);
		checkSourceRange(extendedStartPosition, extendedLength, "x = 1    /*bla*/", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=290877
	 */
	public void test0338() throws JavaModelException {
		String contents =
			"/**\n" +
			" * The first enum value for my enum.\n" +
			" *\n" +
			" * @enum myEnum\n" +
			" */\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		TypeDeclaration node = (TypeDeclaration) getASTNode(unit, 0);
		Javadoc javadoc = node.getJavadoc();
		List tags = javadoc.tags();
		assertEquals("Wrong size", "@enum", ((TagElement) tags.get(1)).getTagName());
		checkSourceRange((TagElement) tags.get(1), "@enum myEnum", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=290877
	 */
	public void test0339() throws JavaModelException {
		String contents =
			"/**\n" +
			" * Use const as a tag element name.\n" +
			" *\n" +
			" * @const new constant\n" +
			" */\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		TypeDeclaration node = (TypeDeclaration) getASTNode(unit, 0);
		Javadoc javadoc = node.getJavadoc();
		List tags = javadoc.tags();
		assertEquals("Wrong size", "@const", ((TagElement) tags.get(1)).getTagName());
		checkSourceRange((TagElement) tags.get(1), "@const new constant", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=290877
	 */
	public void test0340() throws JavaModelException {
		String contents =
			"/**\n" +
			" * Use the goto as a tag element name.\n" +
			" *\n" +
			" * @goto new position\n" +
			" */\n" +
			"public class X {}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		TypeDeclaration node = (TypeDeclaration) getASTNode(unit, 0);
		Javadoc javadoc = node.getJavadoc();
		List tags = javadoc.tags();
		assertEquals("Wrong size", "@goto", ((TagElement) tags.get(1)).getTagName());
		checkSourceRange((TagElement) tags.get(1), "@goto new position", contents);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=300734
	public void test341() throws JavaModelException {
		String contents =
			"public class Bug300734 {\n" +
			"	public void foo(String x) {\n" +
			"		x.getClass();\n" +
			"       x.getClass();\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/Bug300734.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			false,
			true);
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
		IMethodBinding methodBinding1 = ((MethodInvocation) ((ExpressionStatement) methodDeclaration.getBody().statements().get(0)).getExpression()).resolveMethodBinding();
		IMethodBinding methodBinding2 = ((MethodInvocation) ((ExpressionStatement) methodDeclaration.getBody().statements().get(1)).getExpression()).resolveMethodBinding();
		assertTrue("Bindings differ", methodBinding1 == methodBinding2);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=304122
	public void test342() throws JavaModelException {
		String contents =
			"@Deprecated\n" +
			"public class X<T> {\n" +
			"	X<String> field;\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(unit, 0);
		ITypeBinding binding = typeDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) getASTNode(unit, 0, 0);
		binding = fieldDeclaration.getType().resolveBinding();
		annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=304122
	public void test343() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	@Deprecated\n" +
			"	<T> Object foo(T t) {\n" +
			"		return t;\n" +
			"	}\n" +
			"	public static Object bar() {\n" +
			"		return new X().<String>foo(\"Hello\");\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
		IMethodBinding binding = methodDeclaration.resolveBinding();
		IAnnotationBinding[] annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 1);
		ReturnStatement statement = (ReturnStatement) methodDeclaration.getBody().statements().get(0);
		MethodInvocation expression = (MethodInvocation) statement.getExpression();
		binding = expression.resolveMethodBinding();
		annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		binding = binding.getMethodDeclaration();
		annotations = binding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=223225
	public void test344() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"    private @interface Strings {\n" +
			"        String[] value() default \"default element\";\n" +
			"    }\n" +
			"    private @interface Annot {\n" +
			"        String[] value();\n" +
			"    }\n" +
			"    private @interface Annot2 {\n" +
			"        String value();\n" +
			"    }\n" +
			"    private @interface Annot3 {\n" +
			"        Class<?> value();\n" +
			"    }\n" +
			"    @Strings\n" +
			"    public void marker() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Strings(\"single element\")\n" +
			"    public void single() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Strings(value = \"single element\")\n" +
			"    public void singleValue() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Strings({\"single element\"})\n" +
			"    public void singleArray() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Strings(value = {\"single element\"})\n" +
			"    public void singleArrayValue() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Strings({\"one\", \"two\", \"three\"})\n" +
			"    public void multi() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Strings(value = {\"one\", \"two\", \"three\"})\n" +
			"    public void multiValue() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Annot(\"test\")\n" +
			"    public void singleValue2() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"    @Annot2(\"test\")\n" +
			"    @Annot3(Object.class)\n" +
			"    public void singleValue3() {\n" +
			"        // nothing\n" +
			"    }\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit= (CompilationUnit) buildAST(
			contents,
			this.workingCopy,
			true,
			true,
			true);
		class MyVisitor extends ASTVisitor {
			List memberPairBindings = new ArrayList();
			private boolean checkAnnotationBinding(Annotation annotation) {
				final IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
				final IMemberValuePairBinding[] allMemberValuePairs = annotationBinding.getAllMemberValuePairs();
				assertEquals("Wrong size", 1, allMemberValuePairs.length);
				IMemberValuePairBinding memberValuePairBinding = allMemberValuePairs[0];
				final Object value = memberValuePairBinding.getValue();
				if ("Strings".equals(annotationBinding.getName())) {
					assertTrue("Not an array", value.getClass().isArray());
				}
				this.memberPairBindings.add(memberValuePairBinding);
				return false;
			}
			public boolean visit(MarkerAnnotation node) {
				return checkAnnotationBinding(node);
			}
			public boolean visit(SingleMemberAnnotation node) {
				return checkAnnotationBinding(node);
			}
			public boolean visit(NormalAnnotation node) {
				return checkAnnotationBinding(node);
			}
			public List allMemberValuePairs() {
				return this.memberPairBindings;
			}
		}
		MyVisitor visitor = new MyVisitor();
		unit.accept(visitor);
		List allMemberValuePairsBindings = visitor.allMemberValuePairs();
		AnnotationTypeDeclaration annotationTypeDeclaration = (AnnotationTypeDeclaration) ((AbstractTypeDeclaration) unit.types().get(0)).bodyDeclarations().get(0);
		AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration = (AnnotationTypeMemberDeclaration) annotationTypeDeclaration.bodyDeclarations().get(0);
		IMethodBinding binding = annotationTypeMemberDeclaration.resolveBinding();
		Object defaultValue = binding.getDefaultValue();
		assertTrue("Not an array", !defaultValue.getClass().isArray());
		unit = (CompilationUnit) buildAST(
				contents,
				this.workingCopy,
				true,
				true,
				true);
		visitor = new MyVisitor();
		unit.accept(visitor);
		List allMemberValuePairsBindings2 = visitor.allMemberValuePairs();
		final int size = allMemberValuePairsBindings.size();
		assertEquals("Wrong size", 10, size);
		assertEquals("Wrong size", 10, allMemberValuePairsBindings2.size());
		StringBuilder buffer = new StringBuilder();
		StringBuilder buffer2 = new StringBuilder();
		for (int i = 0; i < size; i++) {
			final IMemberValuePairBinding firstMemberValuePairBinding = (IMemberValuePairBinding) allMemberValuePairsBindings.get(i);
			final IMemberValuePairBinding secondMemberValuePairBinding = (IMemberValuePairBinding) allMemberValuePairsBindings2.get(i);
			final boolean isEqualTo = firstMemberValuePairBinding.isEqualTo(secondMemberValuePairBinding);
			assertTrue("not equals: " + i, isEqualTo);
			buffer.append(firstMemberValuePairBinding);
			buffer2.append(secondMemberValuePairBinding);
		}
		assertTrue("Different output", buffer.toString().equals(buffer2.toString()));
		annotationTypeDeclaration = (AnnotationTypeDeclaration) ((AbstractTypeDeclaration) unit.types().get(0)).bodyDeclarations().get(1);
		annotationTypeMemberDeclaration = (AnnotationTypeMemberDeclaration) annotationTypeDeclaration.bodyDeclarations().get(0);
		binding = annotationTypeMemberDeclaration.resolveBinding();
		defaultValue = binding.getDefaultValue();
		assertNull("Got a default value", defaultValue);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=327931
	 */
	public void test0345() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0345/X.java", true/*resolve*/);
		String contents =
				"package test0345;\n" +
				"public class X extends A {\n" +
				"	/*start*/@Test(groups = NAME)/*end*/ int i;\n" +
				"}";
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
				contents,
				this.workingCopy);
		assertNotNull("No annotation", annotation);
		List values = annotation.values();
		MemberValuePair pair = (MemberValuePair) values.get(0);
		SimpleName value = (SimpleName) pair.getValue();
		String constantValue = (String) value.resolveConstantExpressionValue();
		assertEquals("Wrong constant value", "a", constantValue);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=327931
	 */
	public void test0346() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0346/X.java", true/*resolve*/);
		String contents =
				"package test0346;\n" +
				"public class X extends A {\n" +
				"	/*start*/@Test(groups = NAME)/*end*/ int i;\n" +
				"}";
		NormalAnnotation annotation = (NormalAnnotation) buildAST(
				contents,
				this.workingCopy);
		assertNotNull("No annotation", annotation);
		List values = annotation.values();
		MemberValuePair pair = (MemberValuePair) values.get(0);
		SimpleName value = (SimpleName) pair.getValue();
		String constantValue = (String) value.resolveConstantExpressionValue();
		assertEquals("Wrong constant value", "a", constantValue);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=333360
	 */
	public void test0347() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/test0347/X.java", true/*resolve*/);
		String contents =
				"package test0347;\n" +
				"public class X implements One</*start*/Outer<Integer>.Inner<Double>[]/*end*/> {\n" +
				"}\n" +
				"interface One<T> {}\n" +
				"class Outer<T> {\n" +
				"	public class Inner<S> {}\n" +
				"}";
		ArrayType type = (ArrayType) buildAST(
				contents,
				this.workingCopy);
		assertNotNull("No annotation", type);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0347.Outer<java.lang.Integer>.Inner<java.lang.Double>[]", binding.getQualifiedName());
		Type componentType = componentType(type);
		binding = componentType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0347.Outer<java.lang.Integer>.Inner<java.lang.Double>", binding.getQualifiedName());
		assertTrue("Not parameterized", componentType.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) componentType;
		Type type2 = parameterizedType.getType();
		assertTrue("Not qualified", type2.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type2;
		binding = qualifiedType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0347.Outer<java.lang.Integer>.Inner<java.lang.Double>", binding.getQualifiedName());
		Type qualifier = qualifiedType.getQualifier();
		assertTrue("Not parameterized", qualifier.isParameterizedType());
		binding = qualifier.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0347.Outer<java.lang.Integer>", binding.getQualifiedName());
		parameterizedType = (ParameterizedType) qualifier;
		type2 = parameterizedType.getType();
		assertTrue("Not simple type", type2.isSimpleType());
		binding = type2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0347.Outer<java.lang.Integer>", binding.getQualifiedName());
	}
	// issues with annotation default values
	public void _test0348() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "test0348", "AnnotatedInterfaceWithStringDefault.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		IType type = sourceUnit.getType("AnnotatedInterfaceWithStringDefault");//$NON-NLS-1$
		//ICompilationUnit sourceUnit2 = getCompilationUnit("Converter15" , "src", "test0348", "TestAnnotationWithStringDefault.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		//IType type2 = sourceUnit2.getType("TestAnnotationWithStringDefault");//$NON-NLS-1$

		assertNotNull("Should not be null", type);
		ASTParser parser= ASTParser.newParser(getJLS4());
		parser.setProject(type.getJavaProject());
		IBinding[] bindings= parser.createBindings(new IJavaElement[] { type }, null);
		if (bindings.length == 1 && bindings[0] instanceof ITypeBinding) {
			ITypeBinding typeBinding= (ITypeBinding) bindings[0];
			IAnnotationBinding[] annotations = typeBinding.getAnnotations();
			for (int i = 0, max = annotations.length; i < max; i++) {
				IAnnotationBinding annotation = annotations[i];
				IMemberValuePairBinding[] allMemberValuePairs = annotation.getAllMemberValuePairs();
				for (int j = 0, max2 = allMemberValuePairs.length; j < max2; j++) {
					IMemberValuePairBinding memberValuePair = allMemberValuePairs[j];
					Object defaultValue = memberValuePair.getValue();
					System.out.println(defaultValue);
					assertNotNull("no default value", defaultValue);
				}
			}
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=334119
	 * Ensures that dollar in a type name is not confused as the starting of member type
	 */
	public void test0348a() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X$Y.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"/*start*/public class X$Y {\n" +
			"}/*end*/",
			this.workingCopy,
			false);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertBindingKeyEquals(
				"Lp/X$Y;",	// should not be Lp/X$Y-X$Y;
			binding.getKey());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=334119
	 * Ensures that dollar in a type name is not confused as the starting of member type
	 */
	public void test0348b() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter15/src/p/X$.java", true/*resolve*/);
		ASTNode node = buildAST(
			"package p;\n" +
			"/*start*/public class X$ {\n" +
			"}/*end*/",
			this.workingCopy,
			false);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertBindingKeyEquals(
				"Lp/X$;",	// should not be Lp/X$~X$;
			binding.getKey());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=339864
	 */
	public void test0349() throws JavaModelException {
		String contents =
			"import java.util.*;\n" +
			"public class X {\n" +
			"	public static Object foo() {\n" +
			"		List<String> l = new ArrayList<>();\n" +
			"		return l;\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit unit = (CompilationUnit) buildAST(
			getJLS3(),
			contents,
			this.workingCopy,
			false,
			true,
			true);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) getASTNode(unit, 0, 0, 0);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) ((VariableDeclarationFragment) statement.fragments().get(0)).getInitializer();
		assertTrue("Should be malformed", isMalformed(classInstanceCreation.getType()));
	}
	/*
	 * 3.7 maintenance - Fixed bug 348024: Empty AST for class with static inner class in a package with package-info.java
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=348024
	 */
	public void testBug348024() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter15" , "src", "testBug348024", "TestClass.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runJLS4Conversion(sourceUnit, true, true);
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Wrong number of types", 1, types.size());
		assertEquals("Wrong number of body declarations", 3, ((TypeDeclaration) types.get(0)).bodyDeclarations().size());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=420458
	 */
	public void testBug420458() throws JavaModelException {
		String contents =
				"/**\n" +
				" * Hello\n" +
				" * @see #foo(Object[][][])\n" +
				" **/\n" +
				"public class X {}\n";
		this.workingCopy = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
		CompilationUnit compilationUnit = (CompilationUnit) buildAST(
			getJLS4(),
			contents,
			this.workingCopy,
			false,
			true,
			true);
		ASTNode node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		Javadoc javaDoc = ((TypeDeclaration) node).getJavadoc();
		TagElement tagElement = (TagElement) javaDoc.tags().get(1);
		MethodRef methodRef = (MethodRef) tagElement.fragments().get(0);
		MethodRefParameter parameter = (MethodRefParameter) methodRef.parameters().get(0);
		ArrayType arrayType = (ArrayType) parameter.getType();
		checkSourceRange(arrayType, "Object[][][]", contents);
		checkSourceRange(arrayType.getElementType(), "Object", contents);
		assertTrue(arrayType.getDimensions() == 3);
		checkSourceRange(componentType(arrayType), "Object", contents);
	}
}
