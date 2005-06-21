/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    
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

import java.util.Collection;

import com.sun.mirror.apt.AnnotationProcessor;
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

public class MirrorDeclarationTestAnnotationProcessor implements AnnotationProcessor {
	
	public static final String NO_ERRORS = "NO ERRORS";
	
	/** Used by the test harness to verify that no errors were encountered **/
	public static String ERROR = NO_ERRORS;
	
	private final AnnotationProcessorEnvironment env;
	
	public MirrorDeclarationTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		this.env = env;
	}

	public void process() {
		try {
			Collection<TypeDeclaration> decls = env.getSpecifiedTypeDeclarations();
			TypeDeclaration testClass = null;
			ClassDeclaration testClassDec = null;
			for(TypeDeclaration decl : decls) {
	            if(decl.toString().endsWith("DeclarationTestClass")) {
	            	testClass = decl;
	            }
	            if(decl.toString().endsWith("ClassDec")) {
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
			if (ERROR == null) {
				ERROR = t.toString();
			}
			t.printStackTrace();
		}
	}

	private void assertEquals(String reason, Object expected, Object actual) {
		if (expected == actual)
			return;
		if (expected != null && expected.equals(actual))
			return;
		fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}

	private void assertEquals(String reason, String expected, String actual) {
		if (expected == actual)
			return;
		if (expected != null && expected.equals(actual))
			return;
		fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}
	
	private void assertEquals(String reason, int expected, int actual) {
		if (expected == actual)
			return;
		fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}
	
	private void assertTrue(String reason, boolean expected) {
		if (!expected)
			fail(reason);
	}
	
	private void fail(final String reason) {
		ERROR = reason;
		throw new IllegalStateException("Failed during test: " + reason);
	}
	
	
	/**
	 * Tests for:
	 * Annotation Mirror
	 * AnnotationTypeDeclaration
	 * AnnotationTypeElementDeclaration
	 * AnnotationValue
	 * 
	 * @param testClass TypeDeclaration
	 * 
	 */
	private void testAnnotationImplementations(TypeDeclaration testClass) {
		
		//AnnotationMirror tests
		Collection<AnnotationMirror> annoMirrors = testClass.getAnnotationMirrors();
		assertEquals("Number of annotation mirrors", 1, annoMirrors.size());
		
        AnnotationMirror annoMirror = annoMirrors.iterator().next();
        assertTrue("Annotation mirror contents", annoMirror.toString().startsWith("@MirrorDeclarationTestAnnotation"));
        
        AnnotationType annoType = annoMirror.getAnnotationType();
        assertTrue("AnnotationType name", annoType.toString().endsWith("mirrortest.MirrorDeclarationTestAnnotation"));
        

        //AnnotationTypeDeclaration tests
        AnnotationTypeDeclaration annoTypeDecl = annoType.getDeclaration();
        assertEquals("AnnotationTypeDeclaration same as AnnotationType", annoType, annoTypeDecl);
        
        
        //AnnotationTypeElementDeclaration and AnnotationValue tests
        Collection<AnnotationTypeElementDeclaration> elementDeclarations = annoTypeDecl.getMethods();
        assertEquals("Number of methods on annotation", 2, elementDeclarations.size());

        AnnotationTypeElementDeclaration elementString = null;
        AnnotationTypeElementDeclaration elementInt = null;
        for(AnnotationTypeElementDeclaration ated : elementDeclarations) {
        	if(ated.toString().startsWith("S"))
        		elementString = ated;
        	if(ated.toString().startsWith("i"))
        		elementInt = ated;
        }
        assertEquals("declaring type same as AnnotationTypeDeclaration", annoTypeDecl, elementString.getDeclaringType());
        assertEquals("declaring type same as AnnotationTypeDeclaration", annoTypeDecl, elementInt.getDeclaringType());

        AnnotationValue valueString = elementString.getDefaultValue();
        AnnotationValue valueInt = elementInt.getDefaultValue();
        assertEquals("", new String("bob"), valueString.getValue());
        assertEquals("", new Integer(3), valueInt.getValue());
        //assertEquals("", new String("bob"), valueString.toString());
        //assertEquals("", new Integer(3), valueInt.toString());
	}
	
	/**
	 * Tests for:
	 * ClassDeclaration
	 * ConstructorDeclaration
	 * 
	 * @param testClass
	 */
	private void testClassDeclaration(ClassDeclaration testClassDec) {
			
		//ClassDeclaration tests
		Collection<ConstructorDeclaration> constructDecls = testClassDec.getConstructors();
		assertEquals("Number of constructors", 2, constructDecls.size());

		ConstructorDeclaration constructNoArg = null;
		ConstructorDeclaration constructIntArg = null;
		for(ConstructorDeclaration construct : constructDecls) {
			if(construct.toString().endsWith("()"))
				constructNoArg = construct;
			if(construct.toString().endsWith("(int j)"))
				constructIntArg = construct;
		}
		assertTrue("constructor with no args", constructNoArg != null);
		assertTrue("constructor with one (int) arg", constructIntArg != null);
		
		Collection<MethodDeclaration> methodDecls = testClassDec.getMethods();
		assertEquals("Number of methods", 2, methodDecls.size());

		MethodDeclaration methodDecl = null;
		methodDecl = methodDecls.iterator().next();
		assertTrue("method declaration exists", methodDecl != null);
		
		ClassType superClass = testClassDec.getSuperclass();
		assertEquals("Object is only super", "java.lang.Object", superClass.toString());	
	}
	
	/**
	 * Tests for:
	 * EnumConstantDeclaration
	 * EnumDeclaration 
	 * 
	 * @param testClass
	 */
	private void testEnumImplementations(TypeDeclaration testClass) {
		
		//EnumDeclaration tests
		Collection<TypeDeclaration> nestedTypes = testClass.getNestedTypes();
		EnumDeclaration enumDecl = null;
		for(TypeDeclaration decl : nestedTypes) {
			if(decl.toString().endsWith("EnumDec"))
				enumDecl = (EnumDeclaration)decl;
		}
		assertTrue("EnumDeclaration exists", enumDecl != null);

		Collection<EnumConstantDeclaration> enumConstDecls = enumDecl.getEnumConstants();
		assertEquals("Number of enum constants", 2, enumConstDecls.size());
		EnumConstantDeclaration enumConstAardvark = null;
		EnumConstantDeclaration enumConstAnteater = null;
		for(EnumConstantDeclaration enumConst : enumConstDecls) {
			if(enumConst.toString().equals("aardvark"))
				enumConstAardvark = enumConst;
			if(enumConst.toString().equals("anteater"))
				enumConstAnteater = enumConst;
		}
		assertTrue("enum constant \"aardvark\" exists", enumConstAardvark != null);
		assertTrue("enum constant \"anteater\" exists", enumConstAnteater != null);
		
		//EnumConstantDeclaration tests
		EnumDeclaration declaringTypeAardvark = enumConstAardvark.getDeclaringType();
		EnumDeclaration declaringTypeAnteater = enumConstAnteater.getDeclaringType();
		assertEquals("Declaring type is EnumDec", "mirrortestpackage.DeclarationTestClass.EnumDec", declaringTypeAardvark.toString());
		assertEquals("Declaring type is EnumDec", "mirrortestpackage.DeclarationTestClass.EnumDec", declaringTypeAnteater.toString());
		
		
		//Modifier tests
		Modifier[] valuesArray = Modifier.values();
		int valuesArrayLength = valuesArray.length;
		assertEquals("Modifier.values() array length", 11, valuesArrayLength);

		assertEquals("Modifier.ABSTRACT", "abstract", Modifier.ABSTRACT.toString());
		assertEquals("Modifier.FINAL", "final", Modifier.FINAL.toString());
		assertEquals("Modifier.NATIVE", "native", Modifier.NATIVE.toString());
		assertEquals("Modifier.PRIVATE", "private", Modifier.PRIVATE.toString());
		assertEquals("Modifier.PROTECTED", "protected", Modifier.PROTECTED.toString());
		assertEquals("Modifier.PUBLIC", "public", Modifier.PUBLIC.toString());
		assertEquals("Modifier.STATIC", "static", Modifier.STATIC.toString());
		assertEquals("Modifier.STRICTFP", "strictfp", Modifier.STRICTFP.toString());
		assertEquals("Modifier.SYNCHRONIZED", "synchronized", Modifier.SYNCHRONIZED.toString());
		assertEquals("Modifier.TRANSIENT", "transient", Modifier.TRANSIENT.toString());
		assertEquals("Modifier.VOLATILE", "volatile", Modifier.VOLATILE.toString());
		assertEquals("Modifier.valueOf(\"PUBLIC\")", Modifier.PUBLIC, Modifier.valueOf("PUBLIC"));
	}
	
	/**
	 * Tests for:
	 * FieldDeclaration
	 * 
	 * @param testClassDec
	 */
	private void testFieldDeclaration(ClassDeclaration testClassDec) {
		
		//FieldDeclaration tests
		Collection<FieldDeclaration> fieldDecls = testClassDec.getFields();
		assertEquals("Number of fields", 4, fieldDecls.size());
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
		assertTrue("Field i exists", fieldI != null);
		assertEquals("Field i constant expression is 1", "1", fieldI.getConstantExpression());
		assertEquals("Field i constant value is 1", "1", fieldI.getConstantValue().toString());
		assertEquals("Field i type is int", "int", fieldI.getType().toString());
		assertTrue("Field f exists", fieldF != null);
		assertEquals("Field f constant expression is null", null, fieldF.getConstantExpression());
		assertEquals("Field f constant value is null", null, fieldF.getConstantValue());
		assertEquals("Field f type is float", "float", fieldF.getType().toString());
		assertTrue("Field s exists", fieldS != null);
		assertEquals("Field s constant expression is hello", "hello", fieldS.getConstantExpression());
		assertEquals("Field s constant value is hello", "hello", fieldS.getConstantValue().toString());
		assertEquals("Field s type is java.lang.String", "java.lang.String", fieldS.getType().toString());
		assertTrue("Field gc exists", fieldGC != null);
		assertEquals("Field gc constant expression is null", null, fieldGC.getConstantExpression());
		assertEquals("Field gc constant value is null", null, fieldGC.getConstantValue());
		assertEquals("Field gc type is java.util.GregorianCalendar", "java.util.GregorianCalendar", fieldGC.getType().toString());
	}
	
	/**
	 * Tests for:
	 * MethodDeclaration
	 * ParameterDeclaration
	 * 
	 * @param testClassDec
	 */
	private void testMethodDeclaration(ClassDeclaration testClassDec) {
		
		//Tests for MethodDeclaration
		Collection<MethodDeclaration> methodDecls = testClassDec.getMethods();
		MethodDeclaration methodDec = null;
		MethodDeclaration methodDecNoArg = null;
		for(MethodDeclaration method : methodDecls) {
			if(method.toString().endsWith("methodDec(int k, String[] t)"))
				methodDec = method;
			if(method.toString().endsWith("methodDecNoArg()"))
				methodDecNoArg = method;
		}
		assertTrue("Method methodDec exists", methodDec != null);

		Collection<ReferenceType> thrownTypes = methodDec.getThrownTypes();
		assertEquals("Number of types thrown", 1, thrownTypes.size());
		
		ReferenceType thrownType = thrownTypes.iterator().next();
		assertEquals("methodDec throws Exception", "java.lang.Exception", thrownType.toString());
		assertTrue("methodDec is varargs", methodDec.isVarArgs());
		assertTrue("Method methodDecNoArg exists", methodDecNoArg != null);
		assertEquals("Number of types thrown", 0, methodDecNoArg.getThrownTypes().size());
		assertTrue("methodDecNoArg is not varargs", !methodDecNoArg.isVarArgs());

		
		//Tests for ParameterDeclaration
		Collection<ParameterDeclaration> paramDecls = methodDec.getParameters();
		ParameterDeclaration paramDeclInt = null;
		ParameterDeclaration paramDeclString = null;
		for(ParameterDeclaration param : paramDecls) {
			if(param.toString().startsWith("int"))
				paramDeclInt = param;
			if(param.toString().startsWith("String[]"))
				paramDeclString = param;
		}
		assertTrue("int parameter exists", paramDeclInt != null);
		assertEquals("Parameter type is int", "int", paramDeclInt.getType().toString());
		assertTrue("String[] parameter exists", paramDeclString != null);
		assertEquals("Parameter type is String[]", "java.lang.String[]", paramDeclString.getType().toString());
		assertEquals("Number of parameters in methodDecNoArg", 0, methodDecNoArg.getParameters().size());
	}
}
