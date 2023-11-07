/*******************************************************************************
 * Copyright (c) 2005, 2016 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    het@google.com  - Bug 441790
 *******************************************************************************/
//TODO AnnotationMirror.ElementValues()
//TODO AnnotationMirror.getPosition()
//TODO AnnotationValue.getPosition()
//TODO AnnotationValue.toString()
//TODO Declaration
//TODO ExecutableDeclaration
//TODO TypeDeclaration
//TODO InterfaceDeclaration
//TODO MemberDeclaration
//TODO MethodDeclaration.getFormalTypeParameters()
//TODO PackageDeclaration
//TODO TypeParameterDeclaration

package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.util.SourcePosition;

public class MirrorDeclarationTestAnnotationProcessor extends BaseProcessor {

	public MirrorDeclarationTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process() {
		ProcessorTestStatus.setProcessorRan();
		try {
			Collection<TypeDeclaration> decls = _env.getSpecifiedTypeDeclarations();
			TypeDeclaration testClass = null;
			ClassDeclaration testClassDec = null;
			for(TypeDeclaration decl : decls) {
	            if(decl.toString().endsWith("DeclarationTestClass")) { //$NON-NLS-1$
	            	testClass = decl;
	            }
	            if(decl.toString().endsWith("ClassDec")) { //$NON-NLS-1$
	            	testClassDec = (ClassDeclaration)decl;
	            }
			}

			testAnnotationImplementations(testClass);
			testClassDeclaration(testClassDec);
			testEnumImplementations(testClass);
			testFieldDeclaration(testClassDec);
			testMethodDeclaration(testClassDec);
		}
		catch (Throwable t) {
			if (!ProcessorTestStatus.hasErrors()) {
				ProcessorTestStatus.failWithoutException(t.toString());
			}
			t.printStackTrace();
		}
	}


	/**
	 * Tests for:
	 * Annotation Mirror
	 * AnnotationTypeDeclaration
	 * AnnotationTypeElementDeclaration
	 * AnnotationValue
	 *
	 * @param testClass TypeDeclaration
	 */
	private void testAnnotationImplementations(TypeDeclaration testClass) {

		//AnnotationMirror tests
		Collection<AnnotationMirror> annoMirrors = testClass.getAnnotationMirrors();
		ProcessorTestStatus.assertEquals("Number of annotation mirrors", 1, annoMirrors.size());

        AnnotationMirror annoMirror = annoMirrors.iterator().next();
        ProcessorTestStatus.assertTrue("Annotation mirror contents", annoMirror.toString().startsWith("@org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationTestAnnotation"));

        AnnotationType annoType = annoMirror.getAnnotationType();
        ProcessorTestStatus.assertTrue("AnnotationType name", annoType.toString().endsWith("mirrortest.MirrorDeclarationTestAnnotation"));


        //AnnotationTypeDeclaration tests
        AnnotationTypeDeclaration annoTypeDecl = annoType.getDeclaration();
        ProcessorTestStatus.assertEquals("AnnotationTypeDeclaration same as AnnotationType", annoType, annoTypeDecl);


        //AnnotationTypeElementDeclaration and AnnotationValue tests
        Collection<AnnotationTypeElementDeclaration> elementDeclarations = annoTypeDecl.getMethods();
        ProcessorTestStatus.assertEquals("Number of methods on annotation", 2, elementDeclarations.size());

        AnnotationTypeElementDeclaration elementString = null;
        AnnotationTypeElementDeclaration elementInt = null;
        for(AnnotationTypeElementDeclaration ated : elementDeclarations) {
        	if(ated.toString().startsWith("S")) {
        		elementString = ated;
                SourcePosition posAted = ated.getPosition();
                ProcessorTestStatus.assertTrue("position should be null", posAted == null); // the anno is declared in binary - no AST.
        	}
        	if(ated.toString().startsWith("i"))
        		elementInt = ated;
        }
        ProcessorTestStatus.assertEquals("declaring type same as AnnotationTypeDeclaration", annoTypeDecl, elementString.getDeclaringType());
        ProcessorTestStatus.assertEquals("declaring type same as AnnotationTypeDeclaration", annoTypeDecl, elementInt.getDeclaringType());

        AnnotationValue valueString = elementString.getDefaultValue();
        SourcePosition posVS = valueString.getPosition();
        ProcessorTestStatus.assertTrue("position should be null", posVS == null); // the anno is declared in binary - no AST.
        AnnotationValue valueInt = elementInt.getDefaultValue();
        ProcessorTestStatus.assertEquals("", "bob", valueString.getValue());
        ProcessorTestStatus.assertEquals("", Integer.valueOf(3), valueInt.getValue());
        ProcessorTestStatus.assertEquals("", "\"bob\"", valueString.toString());
        ProcessorTestStatus.assertEquals("", "3", valueInt.toString());
	}

	/**
	 * Tests for:
	 * ClassDeclaration
	 * ConstructorDeclaration
	 */
	private void testClassDeclaration(ClassDeclaration testClassDec) {

		//ClassDeclaration tests
		Collection<ConstructorDeclaration> constructDecls = testClassDec.getConstructors();
		ProcessorTestStatus.assertEquals("Number of constructors", 2, constructDecls.size());

		ConstructorDeclaration constructNoArg = null;
		ConstructorDeclaration constructIntArg = null;
		for(ConstructorDeclaration construct : constructDecls) {
			if(construct.toString().endsWith("()"))
				constructNoArg = construct;
			if(construct.toString().endsWith("(int j)"))
				constructIntArg = construct;
		}
		ProcessorTestStatus.assertTrue("constructor with no args", constructNoArg != null);
		ProcessorTestStatus.assertTrue("constructor with one (int) arg", constructIntArg != null);

		Collection<MethodDeclaration> methodDecls = testClassDec.getMethods();
		ProcessorTestStatus.assertEquals("Number of methods", 5, methodDecls.size());

		ArrayList<AnnotationMirror> annotationMirrors = new ArrayList<AnnotationMirror>();
		for (MethodDeclaration methodDeclaration : methodDecls) {
			Collection<AnnotationMirror> mirrors = methodDeclaration.getAnnotationMirrors();
			annotationMirrors.addAll(mirrors);
		}
		ProcessorTestStatus.assertEquals("Wrong size for annotation mirrors", 3, annotationMirrors.size());

		MethodDeclaration methodDecl = methodDecls.iterator().next();
		ProcessorTestStatus.assertTrue("method declaration exists", methodDecl != null);

		ClassType superClass = testClassDec.getSuperclass();
		ProcessorTestStatus.assertEquals("Object is only super", "java.lang.Object", superClass.toString());
	}

	/**
	 * Tests for:
	 * EnumConstantDeclaration
	 * EnumDeclaration
	 */
	private void testEnumImplementations(TypeDeclaration testClass) {
		//EnumDeclaration tests
		Collection<TypeDeclaration> nestedTypes = testClass.getNestedTypes();
		EnumDeclaration enumDecl = null;
		for(TypeDeclaration decl : nestedTypes) {
			if(decl.toString().endsWith("EnumDec"))
				enumDecl = (EnumDeclaration)decl;
		}
		ProcessorTestStatus.assertTrue("EnumDeclaration exists", enumDecl != null);

		Collection<EnumConstantDeclaration> enumConstDecls = enumDecl.getEnumConstants();
		ProcessorTestStatus.assertEquals("Number of enum constants", 2, enumConstDecls.size());
		EnumConstantDeclaration enumConstAardvark = null;
		EnumConstantDeclaration enumConstAnteater = null;
		for(EnumConstantDeclaration enumConst : enumConstDecls) {
			if(enumConst.toString().equals("aardvark"))
				enumConstAardvark = enumConst;
			if(enumConst.toString().equals("anteater"))
				enumConstAnteater = enumConst;
		}
		ProcessorTestStatus.assertTrue("enum constant \"aardvark\" exists", enumConstAardvark != null);
		ProcessorTestStatus.assertTrue("enum constant \"anteater\" exists", enumConstAnteater != null);

		//EnumConstantDeclaration tests
		EnumDeclaration declaringTypeAardvark = enumConstAardvark.getDeclaringType();
		EnumDeclaration declaringTypeAnteater = enumConstAnteater.getDeclaringType();
		ProcessorTestStatus.assertEquals("Declaring type is EnumDec", "mirrortestpackage.DeclarationTestClass.EnumDec", declaringTypeAardvark.toString());
		ProcessorTestStatus.assertEquals("Declaring type is EnumDec", "mirrortestpackage.DeclarationTestClass.EnumDec", declaringTypeAnteater.toString());

		//Modifier tests
		Modifier[] valuesArray = Modifier.values();
		int valuesArrayLength = valuesArray.length;
		ProcessorTestStatus.assertEquals("Modifier.values() array length", 11, valuesArrayLength);

		ProcessorTestStatus.assertEquals("Modifier.ABSTRACT", "abstract", Modifier.ABSTRACT.toString());
		ProcessorTestStatus.assertEquals("Modifier.FINAL", "final", Modifier.FINAL.toString());
		ProcessorTestStatus.assertEquals("Modifier.NATIVE", "native", Modifier.NATIVE.toString());
		ProcessorTestStatus.assertEquals("Modifier.PRIVATE", "private", Modifier.PRIVATE.toString());
		ProcessorTestStatus.assertEquals("Modifier.PROTECTED", "protected", Modifier.PROTECTED.toString());
		ProcessorTestStatus.assertEquals("Modifier.PUBLIC", "public", Modifier.PUBLIC.toString());
		ProcessorTestStatus.assertEquals("Modifier.STATIC", "static", Modifier.STATIC.toString());
		ProcessorTestStatus.assertEquals("Modifier.STRICTFP", "strictfp", Modifier.STRICTFP.toString());
		ProcessorTestStatus.assertEquals("Modifier.SYNCHRONIZED", "synchronized", Modifier.SYNCHRONIZED.toString());
		ProcessorTestStatus.assertEquals("Modifier.TRANSIENT", "transient", Modifier.TRANSIENT.toString());
		ProcessorTestStatus.assertEquals("Modifier.VOLATILE", "volatile", Modifier.VOLATILE.toString());
		ProcessorTestStatus.assertEquals("Modifier.valueOf(\"PUBLIC\")", Modifier.PUBLIC, Modifier.valueOf("PUBLIC"));
	}

	/**
	 * Tests for:
	 * FieldDeclaration
	 */
	private void testFieldDeclaration(ClassDeclaration testClassDec) {
		//FieldDeclaration tests
		Collection<FieldDeclaration> fieldDecls = testClassDec.getFields();
		ProcessorTestStatus.assertEquals("Number of fields", 4, fieldDecls.size());
		FieldDeclaration fieldI = null;
		FieldDeclaration fieldF = null;
		FieldDeclaration fieldS = null;
		FieldDeclaration fieldGC = null;
		for(FieldDeclaration field : fieldDecls) {
			if(field.toString().equals("i"))
				fieldI = field;
			if(field.toString().equals("f"))
				fieldF = field;
			if(field.toString().equals("s"))
				fieldS = field;
			if(field.toString().equals("gc"))
				fieldGC = field;
		}
		ProcessorTestStatus.assertTrue("Field i exists", fieldI != null);
		ProcessorTestStatus.assertEquals("Field i constant expression is 1", "1", fieldI.getConstantExpression());
		ProcessorTestStatus.assertEquals("Field i constant value is 1", "1", fieldI.getConstantValue().toString());
		ProcessorTestStatus.assertEquals("Field i type is int", "int", fieldI.getType().toString());
		ProcessorTestStatus.assertTrue("Field f exists", fieldF != null);
		ProcessorTestStatus.assertEquals("Field f constant expression is null", null, fieldF.getConstantExpression());
		ProcessorTestStatus.assertEquals("Field f constant value is null", null, fieldF.getConstantValue());
		ProcessorTestStatus.assertEquals("Field f type is float", "float", fieldF.getType().toString());
		ProcessorTestStatus.assertTrue("Field s exists", fieldS != null);
		ProcessorTestStatus.assertEquals("Field s constant expression is hello", "hello", fieldS.getConstantExpression());
		ProcessorTestStatus.assertEquals("Field s constant value is hello", "hello", fieldS.getConstantValue().toString());
		ProcessorTestStatus.assertEquals("Field s type is java.lang.String", "java.lang.String", fieldS.getType().toString());
		ProcessorTestStatus.assertTrue("Field gc exists", fieldGC != null);
		ProcessorTestStatus.assertEquals("Field gc constant expression is null", null, fieldGC.getConstantExpression());
		ProcessorTestStatus.assertEquals("Field gc constant value is null", null, fieldGC.getConstantValue());
		ProcessorTestStatus.assertEquals("Field gc type is java.util.GregorianCalendar", "java.util.GregorianCalendar", fieldGC.getType().toString());
	}

	/**
	 * Tests for:
	 * MethodDeclaration
	 * ParameterDeclaration
	 */
	private void testMethodDeclaration(ClassDeclaration testClassDec) {
		//Tests for MethodDeclaration
		Collection<MethodDeclaration> methodDecls = testClassDec.getMethods();
		MethodDeclaration methodDec = null;
		MethodDeclaration methodDecNoArg = null;
		for(MethodDeclaration method : methodDecls) {
			if(method.toString().endsWith("methodDec(int k, String... t)"))
				methodDec = method;
			if(method.toString().endsWith("methodDecNoArg()"))
				methodDecNoArg = method;
		}
		ProcessorTestStatus.assertTrue("Method methodDec exists", methodDec != null);

		Collection<ReferenceType> thrownTypes = methodDec.getThrownTypes();
		ProcessorTestStatus.assertEquals("Number of types thrown", 1, thrownTypes.size());

		ReferenceType thrownType = thrownTypes.iterator().next();
		ProcessorTestStatus.assertEquals("methodDec throws Exception", "java.lang.Exception", thrownType.toString());
		ProcessorTestStatus.assertTrue("methodDec is varargs", methodDec.isVarArgs());
		ProcessorTestStatus.assertTrue("Method methodDecNoArg exists", methodDecNoArg != null);
		ProcessorTestStatus.assertEquals("Number of types thrown", 0, methodDecNoArg.getThrownTypes().size());
		ProcessorTestStatus.assertTrue("methodDecNoArg is not varargs", !methodDecNoArg.isVarArgs());


		//Tests for ParameterDeclaration
		Collection<ParameterDeclaration> paramDecls = methodDec.getParameters();
		ParameterDeclaration paramDeclInt = null;
		ParameterDeclaration paramDeclString = null;
		for(ParameterDeclaration param : paramDecls) {
			if(param.toString().startsWith("int"))
				paramDeclInt = param;
			if(param.toString().startsWith("String..."))
				paramDeclString = param;
		}
		ProcessorTestStatus.assertTrue("int parameter exists", paramDeclInt != null);
		ProcessorTestStatus.assertEquals("Parameter type is int", "int", paramDeclInt.getType().toString());
		ProcessorTestStatus.assertTrue("String... parameter exists", paramDeclString != null);
		ProcessorTestStatus.assertEquals("Parameter type is String[]", "java.lang.String[]", paramDeclString.getType().toString());
		ProcessorTestStatus.assertEquals("Number of parameters in methodDecNoArg", 0, methodDecNoArg.getParameters().size());
	}
}
