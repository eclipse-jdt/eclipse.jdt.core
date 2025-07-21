/*******************************************************************************
 * Copyright (c) 2013, 2021 IBM Corporation.
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

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.eclipse.jdt.compiler.apt.tests.annotations.*;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the java 8 specific elements and validates the lambda and
 * type annotated elements. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.Java8ElementProcessor to the command line.
 * @since 3.10
 */
@SupportedAnnotationTypes({"targets.model8.TypeAnnot",
							"org.eclipse.jdt.compiler.apt.tests.annotations.Type", "org.eclipse.jdt.compiler.apt.tests.annotations.Type1",
							"org.eclipse.jdt.compiler.apt.tests.annotations.Type$1",
	                       "org.eclipse.jdt.compiler.apt.tests.annotations.Foo", "org.eclipse.jdt.compiler.apt.tests.annotations.FooContainer",
	                       "org.eclipse.jdt.compiler.apt.tests.annotations.IFoo", "org.eclipse.jdt.compiler.apt.tests.annotations.IFooContainer",
	                       "org.eclipse.jdt.compiler.apt.tests.annotations.Goo", "org.eclipse.jdt.compiler.apt.tests.annotations.GooNonContainer",
	                       "org.eclipse.jdt.compiler.apt.tests.annotations.FooNonContainer", "targets.filer8.PackageAnnot",
	                       "java.lang.Deprecated"})

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Java8ElementProcessor extends BaseProcessor {

	private static final String[] ELEMENT_NAMES = new String[] {"targets.model8.X", "T", "U", "K", "V", "KK", "VV", "KKK", "VVV"};
	private static final String[] TYPE_PARAM_ELEMENTS_Z1 = new String[] {"KK", "VV"};
	private static final String[] TYPE_PARAM_ELEMENTS_Z2 = new String[] {"KKK", "VVV"};
	String simpleName = "filer8";
	String packageName = "targets.filer8";
	int roundNo = 0;
	boolean reportSuccessAlready = true;

	protected RoundEnvironment roundEnv = null;
	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}

		this.roundEnv = roundEnv;

		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		} else {
			try {
				if (!invokeTestMethods(options)) {
					testAll();
				}
				if (this.reportSuccessAlready) {
					super.reportSuccess();
				}
			} catch (AssertionFailedError e) {
				super.reportError(getExceptionStackTrace(e));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean invokeTestMethods(Map<String, String> options) throws Throwable {
		Method testMethod = null;
		Set<String> keys = options.keySet();
		boolean testsFound = false;
		for (String option : keys) {
			if (option.startsWith("test")) {
				try {
					testMethod = this.getClass().getDeclaredMethod(option, new Class[0]);
					if (testMethod != null) {
						testsFound = true;
						testMethod.invoke(this,  new Object[0]);
					}
				} catch (InvocationTargetException e) {
					throw e.getCause();
				} catch (Exception e) {
					super.reportError(getExceptionStackTrace(e));
				}
			}
		}
		return testsFound;
	}

	public void testAll() throws AssertionFailedError {
		testSE8Specifics();
		testLambdaSpecifics();
		testTypeAnnotations();
		testTypeAnnotations1();
		testTypeAnnotations2();
		testTypeAnnotations3();
		testTypeAnnotations4();
		testTypeAnnotations5();
		testTypeAnnotations6();
		testTypeAnnotations7();
		testTypeAnnotations8();
		testTypeAnnotations9();
		testTypeAnnotations10();
		testTypeAnnotations11();
		testTypeAnnotations12();
		testTypeAnnotations13();
		testTypeAnnotations14();
		testTypeAnnotations15();
		testTypeAnnotations16();
		testRepeatedAnnotations17();
		testRepeatedAnnotations18();
		testRepeatedAnnotations19();
		testRepeatedAnnotations20();
		testRepeatedAnnotations21();
		testRepeatedAnnotations22();
		testTypeAnnotations23();
		testRepeatedAnnotations24();
		testRepeatedAnnotations25();
		testTypeAnnotations26();
		testTypeAnnotations27();
		//testPackageAnnotations();
		testBug520540();
		testBug526288();
		testEnumConstArguments();
		testBug544288();
	}

	public void testLambdaSpecifics() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.LambdaTest");
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Type element for LambdaTest should not be null", annotatedType);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Type LambdaTest is not a functional interface", _elementUtils.isFunctionalInterface(annotatedType));
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method foo should not be null", method);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Method foo is not a default method", method.isDefault());
		Set<Modifier> modifiers = method.getModifiers();
		assertModifiers(modifiers, new String[]{});

		annotatedType = _elementUtils.getTypeElement("targets.model8.DefaultInterface");
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Type element for DefaultInterface should not be null", annotatedType);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Type DefaultInterface is not a functional interface", _elementUtils.isFunctionalInterface(annotatedType));

		method = null;
		members = _elementUtils.getAllMembers(annotatedType);
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("defaultMethod".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method defaultMethod() should not be null", method);
		assertTrue("Java8ElementProcessor#examineLambdaSpecifics: Method defaultMethod() should be a default method", method.isDefault());
		modifiers = method.getModifiers();
		assertModifiers(modifiers, new String[]{"public", "default"});

		method = null;
		members = _elementUtils.getAllMembers(annotatedType);
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("anotherDefault".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method anotherDefault() should not be null", method);
		assertTrue("Java8ElementProcessor#examineLambdaSpecifics: Method anotherDefault() should be a default method", method.isDefault());
		modifiers = method.getModifiers();
		assertModifiers(modifiers, new String[]{"public", "default"});


		method = null;
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("staticMethod".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method staticMethod() should not be null", method);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Method staticMethod() shoule not be a default method", method.isDefault());
		modifiers = method.getModifiers();
		assertModifiers(modifiers, new String[]{"public", "static"});

		annotatedType = _elementUtils.getTypeElement("targets.model8.FunctionalInterface");
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Type element for FunctionalInterface should not be null", annotatedType);
		assertTrue("Java8ElementProcessor#examineLambdaSpecifics: Type FunctionalInterface should be a functional interface", _elementUtils.isFunctionalInterface(annotatedType));

		method = null;
		members = _elementUtils.getAllMembers(annotatedType);
		for (ExecutableElement member : ElementFilter.methodsIn(members)) {
			if ("abstractMethod".equals(member.getSimpleName().toString())) {
				method = member;
				break;
			}
		}
		assertNotNull("Java8ElementProcessor#examineLambdaSpecifics: Element for method abstractMethod() should not be null", method);
		assertFalse("Java8ElementProcessor#examineLambdaSpecifics: Method abstractMethod() should not be a default method", method.isDefault());
	}

	public void testSE8Specifics() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		examineSE8AnnotationMethods("Java8ElementProcessor#examineSE8Specifics: ", annotatedType, "c");

		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		VariableElement field = null, field1 = null, field11 = null;
		ExecutableElement method2 = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			} else if ("_field".equals(member.getSimpleName().toString())) {
				field = (VariableElement) member;
			} else if ("noAnnotationHere".equals(member.getSimpleName().toString())) {
				method2 = (ExecutableElement) member;
			} else if ("_field1".equals(member.getSimpleName().toString())) {
				field1 = (VariableElement) member;
			} else if ("_field11".equals(member.getSimpleName().toString())) {
				field11 = (VariableElement) member;
			}

		}
		assertNotNull("Method should not be null", method);
		TypeMirror typeMirror = method.getReturnType();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for method foo should not be null", typeMirror);
		examineSE8AnnotationMethods("Java8ElementProcessor#examineSE8Specifics: ", typeMirror, "m");
		List<? extends AnnotationMirror> list = typeMirror.getAnnotationMirrors();
		assertEquals("Java8ElementProcessor#examineSE8Specifics: Incorrect no of annotation mirrors", 1, list.size());
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for field _field should not be null", field);
		typeMirror = field.asType();
		examineSE8AnnotationMethods("Java8ElementProcessor#examineSE8Specifics: ", typeMirror, "f");

		TypeMirror similar = typeMirror;
		typeMirror = field1.asType();
		assertFalse("Should be of same type", _typeUtils.isSameType(typeMirror, similar));
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f1)"});
		similar = field11.asType();
		assertTrue("Should be of same type", _typeUtils.isSameType(typeMirror, similar));

		typeMirror = method2.getReturnType();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for method noAnnotationHere should not be null", typeMirror);
		Type annot = typeMirror.getAnnotation(Type.class);
		assertNull("Annotation should not be present", annot);
		Type[] annots = typeMirror.getAnnotationsByType(Type.class);
		assertEquals("Annotation is not empty list", 0, annots.length);
	}

	public void testTypeAnnotations() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		TypeMirror superType = annotatedType.getSuperclass();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: super type not be null", superType);
		verifyAnnotations(superType, new String[]{"@Type(value=s)"});

		List<? extends TypeMirror> interfaces  = annotatedType.getInterfaces();
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: super interfaces list should not be null", interfaces);
		assertEquals("Java8ElementProcessor#examineSE8Specifics: incorrect no of super interfaces", 2, interfaces.size());
		superType = interfaces.get(0);
		verifyAnnotations(superType, new String[]{"@Type(value=i1)"});
		superType = interfaces.get(1);
		verifyAnnotations(superType, new String[]{"@Type(value=i2)"});
	}

	public void testTypeAnnotations1() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("bar".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		List<? extends VariableElement> params = method.getParameters();
		assertEquals("Incorrect no of params for method bar()", 2, params.size());
		VariableElement param = params.get(0);
		TypeMirror typeMirror = param.asType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p1)"});
		param = params.get(1);
		typeMirror = param.asType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p2)"});
	}

	public void testTypeAnnotations2() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		VariableElement field2 = null;
		VariableElement field3 = null;
		for (Element member : members) {
			if ("_field2".equals(member.getSimpleName().toString())) {
				field2 = (VariableElement) member;
			} else if ("_field3".equals(member.getSimpleName().toString())) {
				field3 = (VariableElement) member;
			}
		}

		//@Type("f") String @Type("f1") [] @Type("f2") [] _field2 @Type("f3") [], _field3 @Type("f4") [][] = null;
		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for field _field2 should not be null", field2);
		TypeMirror typeMirror = field2.asType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f3)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f1)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f2)"});

		assertNotNull("Java8ElementProcessor#examineSE8Specifics: Element for field _field3 should not be null", field3);
		typeMirror = field3.asType();
		// The second field binding doesn't seem to have the annotations. To be investigated
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f4)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f1)"});
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{"@Type(value=f2)"});
	}

	public void testTypeAnnotations3() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		// @Type("m") String @Type("m1") [] foo() @Type("m2") [] @Type("m3") [] {}
		assertNotNull("Method should not be null", method);
		TypeMirror typeMirror = method.getReturnType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=m2)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=m3)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=m1)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=m)"});
	}

	public void testTypeAnnotations4() {
		// void bar( @Type("p1") String [] a @Type("p2") [], @Type("p3") int @Type("p4") [] b [] @Type("p5") []) {}
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("bar".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		assertNotNull("Method should not be null", method);
		List<? extends VariableElement> params = method.getParameters();
		assertEquals("Incorrect no of params for method bar()", 2, params.size());
		VariableElement param = params.get(0);
		TypeMirror typeMirror = param.asType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p2)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();

		verifyAnnotations(typeMirror, new String[]{});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p1)"});

		param = params.get(1);
		typeMirror = param.asType();
		verifyAnnotations(typeMirror, new String[]{});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();

		verifyAnnotations(typeMirror, new String[]{"@Type(value=p5)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p4)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p3)"});

	}

	public void testTypeAnnotations5() {
		// void foo2() throws (@Type("e1") NullPointerException, (@Type("e2") ArrayIndexOutOfBoundsException {}
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("foo2".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		List<?extends TypeMirror> exceptions = method.getThrownTypes();
		assertEquals("Incorrect no of thrown exceptions", 2, exceptions.size());
		TypeMirror typeMirror = exceptions.get(0);
		verifyAnnotations(typeMirror, new String[]{"@Type(value=e1)"});
		typeMirror = exceptions.get(1);
		verifyAnnotations(typeMirror, new String[]{"@Type(value=e2)"});
	}

	public void testTypeAnnotations6() {
		// void bar2 (@Type("p1") String @Type("p2") [] @Type("p3") ... args) {}
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("bar2".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		List<? extends VariableElement> params = method.getParameters();
		assertEquals("Incorrect no of parameters", 1, params.size());
		TypeMirror typeMirror = params.get(0).asType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p2)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p3)"});
		assertEquals("Should be an array type", TypeKind.ARRAY, typeMirror.getKind());
		typeMirror = ((ArrayType) typeMirror).getComponentType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=p1)"});

	}

	public void testTypeAnnotations7() {
		// public class Z <@Type("tp1") K, @Type("tp2") V> {
		TypeElement typeZ = _elementUtils.getTypeElement("targets.model8.Z");
		TypeMirror typeMirror = typeZ.asType();
		List<? extends TypeParameterElement> typeParams = typeZ.getTypeParameters();
		assertEquals("Incorrect no of type params", 2, typeParams.size());
		TypeParameterElement typeParam = typeParams.get(0);
		verifyAnnotations(typeParam, new String[]{"@Type(value=tp1)"});
		typeMirror = typeParam.asType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=tp1)"});
		typeParam = typeParams.get(1);
		typeMirror = typeParam.asType();
		verifyAnnotations(typeParam, new String[]{"@Type(value=tp2)"});
		verifyAnnotations(typeMirror, new String[]{"@Type(value=tp2)"});

		// public <T> Z(@Type T t){}
		List<? extends Element> members = _elementUtils.getAllMembers(typeZ);
		for (ExecutableElement method : ElementFilter.constructorsIn(members)) {
			ExecutableType executabletype = (ExecutableType) method.asType();
			List<? extends TypeMirror> list = executabletype.getParameterTypes();
			List<? extends VariableElement> list1 = method.getParameters();
			for(int i = 0; i < list1.size(); i++) {
				VariableElement variableelement = list1.get(i);
				if (method.getSimpleName().toString().equals("<init>")) {
					assertEquals("Trouble!", list.get(i), variableelement.asType());
				}
			}
		}
	}

	public void testTypeAnnotations8() {
		TypeElement typeZ = _elementUtils.getTypeElement("targets.model8.Z");
		List<? extends Element> members = _elementUtils.getAllMembers(typeZ);
		ExecutableElement method = null;
		VariableElement field = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			} else if ("z1".equals(member.getSimpleName().toString())) {
				field = (VariableElement) member;
			}
		}

		// public <@Type("mp1") T, @Type("mp2") U> void foo() {}
		List<? extends TypeParameterElement> typeParams = method.getTypeParameters();
		assertEquals("Incorrect no of type params", 2, typeParams.size());
		TypeParameterElement typeParam = typeParams.get(0);
		verifyAnnotations(typeParam, new String[]{"@Type(value=mp1)"});
		verifyAnnotations(typeParam.asType(), new String[]{"@Type(value=mp1)"});
		typeParam = typeParams.get(1);
		verifyAnnotations(typeParam, new String[]{"@Type(value=mp2)"});
		verifyAnnotations(typeParam.asType(), new String[]{"@Type(value=mp2)"});
		//Z<@Type("ta1") String, @Type("ta2") Object> z1 = null;
		// APIs don't expose the type arguments on a TypeMirror
		TypeMirror typeMirror = field.asType();
		verifyAnnotations(typeMirror, new String[]{});
	}

	public void testTypeAnnotations9() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);

		VariableElement field2 = null;
		for (VariableElement member : ElementFilter.fieldsIn(members)) {
			if ("_field2".equals(member.getSimpleName().toString())) {
				field2 = member;
				break;
			}
		}
		TypeMirror typeMirror = field2.asType();
		Type$1 annot1 = typeMirror.getAnnotation(Type$1.class);
		assertNotNull("Annotation should not be null", annot1);
		Type.One annot2 = typeMirror.getAnnotation(Type.One.class);
		assertNotNull("Annotation should not be null", annot2);
	}

	public void testTypeAnnotations10() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		VariableElement field3 = null;
		for (Element member : members) {
			if ("_field3".equals(member.getSimpleName().toString())) {
				field3 = (VariableElement) member;
			}
		}
		verifyAnnotations(annotatedType, new String[]{"@Type(value=c)"});
		verifyAnnotations(annotatedType.asType(), new String[]{});
		verifyAnnotations(field3, new String[]{});
		verifyAnnotations(field3.asType(), new String[]{});
	}

	public void testTypeAnnotations11() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		VariableElement xy = null;
		for (Element member : members) {
			if ("xy".equals(member.getSimpleName().toString())) {
				xy = (VariableElement) member;
			}
		}
		verifyAnnotations(xy, new String[]{});
		verifyAnnotations(xy.asType(), new String[]{"@Type(value=xy)"});

		Set<String> expectedElementNames = Stream.of(ELEMENT_NAMES).collect(Collectors.toSet());
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Type.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);

		for (Element e : actualElments) {
			if (e instanceof TypeElement) {
				String name = ((TypeElement) e).getQualifiedName().toString();
				if (!expectedElementNames.remove(name)) {
					reportError("Missing root element " + name);
				}
			} else if (e instanceof TypeParameterElement) {
				String name = ((TypeParameterElement) e).getSimpleName().toString();
				if (!expectedElementNames.remove(name)) {
					reportError("Missing root element " + name);
				}
			}
		}
		assertTrue("Found unexpected extra elements", expectedElementNames.isEmpty());
	}

	private void tTypeAnnotations12(TypeElement annotatedType) {
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement bar2 = null;
		ExecutableElement constr = null;
		ExecutableElement constr2 = null;
		for (Element member : members) {
			if ("bar2".equals(member.getSimpleName().toString())) {
				bar2 = (ExecutableElement) member;
			} else if ("<init>".equals(member.getSimpleName().toString())) {
				if (((ExecutableElement) member).getParameters().isEmpty()) {
					constr = (ExecutableElement) member;
				} else {
					constr2 = (ExecutableElement) member;
				}
			}
		}
		TypeMirror typeMirror = bar2.getReceiverType();
		verifyAnnotations(typeMirror, new String[]{"@Type(value=receiver)"});
		ExecutableType type = (ExecutableType) bar2.asType();
		verifyAnnotations(type.getReceiverType(), new String[]{"@Type(value=receiver)"});

		verifyAnnotations(constr, new String[]{});
		type = (ExecutableType) constr.asType();
		verifyAnnotations(type, new String[]{});

		verifyAnnotations(constr2, new String[]{"@Type1(value=constr2)"});
		type = (ExecutableType) constr2.asType();
		verifyAnnotations(type, new String[]{});
	}

	public void testTypeAnnotations12() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		tTypeAnnotations12(annotatedType);
	}

	public void testTypeAnnotations12Binary() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model9.X");
		tTypeAnnotations12(annotatedType);
	}

	public void testTypeAnnotations13() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);

		VariableElement field = null;
		for (VariableElement member : ElementFilter.fieldsIn(members)) {
			if ("_i".equals(member.getSimpleName().toString())) {
				field = member;
				break;
			}
		}
		TypeMirror typeMirror = field.asType();
		verifyAnnotations(typeMirror, new String[]{});
	}

	public void testTypeAnnotations14() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.X");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement main = null;
		ExecutableElement constr = null;
		TypeElement XY = null;
		for (Element member : members) {
			if ("main".equals(member.getSimpleName().toString())) {
				main = (ExecutableElement) member;
			} else if ("<init>".equals(member.getSimpleName().toString())) {
				constr = (ExecutableElement) member;
			} else if ("XY".equals(member.getSimpleName().toString())) {
				XY = (TypeElement) member;
			}
		}
		TypeMirror typeMirror = main.getReceiverType();
		assertNotNull("TypeMirror should not be null", typeMirror);
		assertSame("Should be no type", TypeKind.NONE, typeMirror.getKind());
		ExecutableType type = (ExecutableType) main.asType();
		typeMirror = type.getReceiverType();
		assertNotNull("TypeMirror should not be null", typeMirror);
		assertSame("Should be no type", TypeKind.NONE, typeMirror.getKind());
		typeMirror = constr.getReceiverType();
		assertNotNull("TypeMirror should not be null", typeMirror);
		assertSame("Should be no type", TypeKind.NONE, typeMirror.getKind());

		Type[] annotations = typeMirror.getAnnotationsByType(Type.class);
		assertEquals("Annotations arrays should be empty", 0, annotations.length);

		type = (ExecutableType) constr.asType();
		typeMirror = type.getReceiverType();
		assertNotNull("TypeMirror should not be null", typeMirror);
		assertSame("Should be no type", TypeKind.NONE, typeMirror.getKind());

		members = _elementUtils.getAllMembers(XY);
		for (Element member : members) {
			if ("<init>".equals(member.getSimpleName().toString())) {
				constr = (ExecutableElement) member;
			}
		}
		typeMirror = constr.getReceiverType();
		assertNotNull("TypeMirror should not be null", typeMirror);
		assertNotSame("Should not be no type", TypeKind.NONE, typeMirror.getKind());
		verifyAnnotations(typeMirror, new String[]{"@Type(value=receiver)"});
		type = (ExecutableType) constr.asType();
		typeMirror = type.getReceiverType();
		assertNotNull("TypeMirror should not be null", typeMirror);
		verifyAnnotations(typeMirror, new String[]{"@Type(value=receiver)"});
		assertNotSame("Should not be no type", TypeKind.NONE, typeMirror.getKind());
	}

	public void testTypeAnnotations15() {
		Set<String> expectedElementNames = Stream.of(TYPE_PARAM_ELEMENTS_Z1).collect(Collectors.toSet());
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Type.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);

		for (Element e : actualElments) {
			if (e instanceof TypeParameterElement) {
				String name = ((TypeParameterElement) e).getSimpleName().toString();
				if (!expectedElementNames.remove(name)) {
					reportError("Missing root element " + name);
				}
			}
		}
		assertTrue("Found unexpected extra elements", expectedElementNames.isEmpty());
	}

	public void testTypeAnnotations16() {
		Set<String> expectedElementNames = Stream.of(TYPE_PARAM_ELEMENTS_Z2).collect(Collectors.toSet());
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Type.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);

		for (Element e : actualElments) {
			if (e instanceof TypeParameterElement) {
				String name = ((TypeParameterElement) e).getSimpleName().toString();
				if (!expectedElementNames.remove(name)) {
					reportError("Missing root element " + name);
				}
			}
		}
		assertTrue("Found unexpected extra elements", expectedElementNames.isEmpty());
	}

	public void testRepeatedAnnotations17() {
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Foo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 0);

		actualElments = roundEnv.getElementsAnnotatedWith(FooContainer.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 1);

		for (Element e : actualElments) {
			verifyAnnotations(e, new String[]{"@FooContainer(value=@org.eclipse.jdt.compiler.apt.tests.annotations.Foo,@org.eclipse.jdt.compiler.apt.tests.annotations.Foo)"},
					new String [] {"@FooContainer(value=[@org.eclipse.jdt.compiler.apt.tests.annotations.Foo, @org.eclipse.jdt.compiler.apt.tests.annotations.Foo])"});
			Annotation annot = e.getAnnotation(Foo.class);
			assertNull("Repeating annotation should not be seen through old API", annot);
			annot = e.getAnnotation(FooContainer.class);
			assertNotNull("Container missing", annot);
			Annotation [] annots = e.getAnnotationsByType(FooContainer.class);
			assertTrue("Should not be empty", annots.length == 1);
			annots = e.getAnnotationsByType(Foo.class);
			assertTrue("Should be 2", annots.length == 2);
			assertEquals("@Foo missing", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo()", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo",  annots[0].toString());
			assertEquals("@Foo missing", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo()", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo", annots[1].toString());
		}
	}

	public void testRepeatedAnnotations18() {
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Foo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 0);

		actualElments = roundEnv.getElementsAnnotatedWith(FooContainer.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 1);

		for (Element e : actualElments) {
			verifyAnnotations(e, new String[]{"@FooContainer(value=@org.eclipse.jdt.compiler.apt.tests.annotations.Foo,@org.eclipse.jdt.compiler.apt.tests.annotations.Foo)"},
					new String [] {"@FooContainer(value=[@org.eclipse.jdt.compiler.apt.tests.annotations.Foo, @org.eclipse.jdt.compiler.apt.tests.annotations.Foo])"});
			Annotation annot = e.getAnnotation(Foo.class);
			assertNull("Repeating annotation should not be seen through old API", annot);
			annot = e.getAnnotation(FooContainer.class);
			assertNotNull("Container missing", annot);
			Annotation [] annots = e.getAnnotationsByType(FooContainer.class);
			assertTrue("Should not be empty", annots.length == 1);
			annots = e.getAnnotationsByType(Foo.class);
			assertTrue("Should be 2", annots.length == 2);
			assertEquals("@Foo missing", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo()", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo",  annots[0].toString());
			assertEquals("@Foo missing", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo()", "@org.eclipse.jdt.compiler.apt.tests.annotations.Foo", annots[1].toString());
		}
	}
	public void testRepeatedAnnotations19() { // Goo is wrapped by GooNonContainer, but Goo is not repeatable.
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Goo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 0);

		actualElments = roundEnv.getElementsAnnotatedWith(GooNonContainer.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 1);

		for (Element e : actualElments) {
			verifyAnnotations(e, new String[]{"@GooNonContainer(value=@org.eclipse.jdt.compiler.apt.tests.annotations.Goo,@org.eclipse.jdt.compiler.apt.tests.annotations.Goo)"},
					new String [] {"@GooNonContainer(value=[@org.eclipse.jdt.compiler.apt.tests.annotations.Goo, @org.eclipse.jdt.compiler.apt.tests.annotations.Goo])"});
			Annotation annot = e.getAnnotation(Goo.class);
			assertNull("Repeating annotation should not be seen through old API", annot);
			annot = e.getAnnotation(GooNonContainer.class);
			assertNotNull("Container missing", annot);
			Annotation [] annots = e.getAnnotationsByType(GooNonContainer.class);
			assertTrue("Should not be empty", annots.length == 1);
			annots = e.getAnnotationsByType(Goo.class); // Goo should not be unwrapped from the container as Goo is not a repeatable annotation.
			assertTrue("Should be 0", annots.length == 0);
		}
	}
	public void testRepeatedAnnotations20() { // Both Foo and FooContainer occur.
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Foo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 1);

		actualElments = roundEnv.getElementsAnnotatedWith(FooContainer.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 1);

		for (Element e : actualElments) {
			verifyAnnotations(e, new String[]{"@FooContainer(value=@org.eclipse.jdt.compiler.apt.tests.annotations.Foo)", "@Foo()"},
					new String [] {"@FooContainer(value=[@org.eclipse.jdt.compiler.apt.tests.annotations.Foo])","@org.eclipse.jdt.compiler.apt.tests.annotations.Foo"});
			Annotation annot = e.getAnnotation(Foo.class);
			assertNotNull("Foo is not wrapped, so should be seen with old API", annot);
			annot = e.getAnnotation(FooContainer.class);
			assertNotNull("Container missing", annot);
			Annotation [] annots = e.getAnnotationsByType(FooContainer.class);
			assertTrue("Should not be empty", annots.length == 1);
			annots = e.getAnnotationsByType(Foo.class);
			assertTrue("Should be 2", annots.length == 2);
		}
	}

	public void testRepeatedAnnotations21() { // Foo is wrapped by a non-declared container
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Foo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 0);

		actualElments = roundEnv.getElementsAnnotatedWith(FooNonContainer.class);
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 1);

		for (Element e : actualElments) {
			verifyAnnotations(e, new String[]{"@FooNonContainer(value=@org.eclipse.jdt.compiler.apt.tests.annotations.Foo,@org.eclipse.jdt.compiler.apt.tests.annotations.Foo)"},
					new String [] {"@FooNonContainer(value=[@org.eclipse.jdt.compiler.apt.tests.annotations.Foo, @org.eclipse.jdt.compiler.apt.tests.annotations.Foo])"});
			Annotation annot = e.getAnnotation(Foo.class);
			assertNull("Foo should not be seen with old API", annot);
			annot = e.getAnnotation(FooNonContainer.class);
			assertNotNull("Container missing", annot);
			Annotation [] annots = e.getAnnotationsByType(FooNonContainer.class);
			assertTrue("Should not be empty", annots.length == 1);
			annots = e.getAnnotationsByType(Foo.class);
			assertTrue("Should be 0", annots.length == 0);
		}
	}

	public void testRepeatedAnnotations22() { // Repeating type annotations
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(Foo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 2);

		for (Element e : actualElments) {
			if (e instanceof VariableElement) {
				VariableElement field = (VariableElement) e;
				TypeMirror mirror = field.asType();
				verifyAnnotations(mirror, new String[]{"@TFooContainer(value=@org.eclipse.jdt.compiler.apt.tests.annotations.TFoo,@org.eclipse.jdt.compiler.apt.tests.annotations.TFoo)"},
					new String [] {"@TFooContainer(value=[@org.eclipse.jdt.compiler.apt.tests.annotations.TFoo, @org.eclipse.jdt.compiler.apt.tests.annotations.TFoo])"});
				Annotation annot = mirror.getAnnotation(TFoo.class);
				assertNull("TFoo should not be seen with old API", annot);
				annot = mirror.getAnnotation(TFooContainer.class);
				assertNotNull("Container missing", annot);
				Annotation [] annots = mirror.getAnnotationsByType(TFooContainer.class);
				assertTrue("Should not be empty", annots.length == 1);
				annots = mirror.getAnnotationsByType(TFoo.class);
				assertTrue("Should be 2", annots.length == 2);
			}
		}
	}

	public void testTypeAnnotations23() {
		Set<? extends Element> allElements = roundEnv.getRootElements();
		for (Element element : allElements) {
			List<? extends AnnotationMirror> list = _elementUtils.getAllAnnotationMirrors(element);
			List<? extends AnnotationMirror> list1 = element.getAnnotationMirrors();
			assertTrue("Annotations mirrors returned by getAllAnnotationMirrors() must contain directly declared annotation mirrors", list.containsAll(list1));
		}
	}

	public void testRepeatedAnnotations24() {
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(IFoo.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 3);
		for (Element e : actualElments) {
			if ("SubClass2".equals(e.getSimpleName().toString())) {
				IFoo annotation = e.getAnnotation(IFoo.class);
				assertTrue("Wrong annotation", annotation.value() == 5);
				IFooContainer container = e.getAnnotation(IFooContainer.class);
				assertTrue("Wrong annotation", container.value()[0].value() == 2);
				IFoo [] annotations = e.getAnnotationsByType(IFoo.class);
				assertTrue("Wrong count", annotations.length == 1);
				assertTrue("Wrong annotation", annotations[0].value() == 5);
				IFooContainer [] containers = e.getAnnotationsByType(IFooContainer.class);
				assertTrue("Wrong count", containers.length == 1);
				assertTrue("Wrong annotation", containers[0].value()[0].value() == 2);
			} else if ("SubClass".equals(e.getSimpleName().toString())) {
				IFoo annotation = e.getAnnotation(IFoo.class);
				assertTrue("Wrong annotation", annotation.value() == 1);
				IFooContainer container = e.getAnnotation(IFooContainer.class);
				assertTrue("Messed up", container.value().length == 2);
				assertTrue("Wrong annotation", container.value()[0].value() == 3);
				assertTrue("Wrong annotation", container.value()[1].value() == 4);
				IFoo [] annotations = e.getAnnotationsByType(IFoo.class);
				assertTrue("Wrong count", annotations.length == 2);
				assertTrue("Wrong annotation", annotations[0].value() == 3);
				assertTrue("Wrong annotation", annotations[1].value() == 4);
				IFooContainer [] containers = e.getAnnotationsByType(IFooContainer.class);
				assertTrue("Wrong count", containers.length == 1);
				assertTrue("Wrong annotation", containers[0].value()[0].value() == 3);
				assertTrue("Wrong annotation", containers[0].value()[1].value() == 4);
			} else if ("JEP120_6".equals(e.getSimpleName().toString())) {
				IFoo annotation = e.getAnnotation(IFoo.class);
				assertTrue("Wrong annotation", annotation.value() == 1);
				IFooContainer container = e.getAnnotation(IFooContainer.class);
				assertTrue("Messed up", container.value().length == 1);
				assertTrue("Wrong annotation", container.value()[0].value() == 2);
				IFoo [] annotations = e.getAnnotationsByType(IFoo.class);
				assertTrue("Wrong count", annotations.length == 2);
				assertTrue("Wrong annotation", annotations[0].value() == 1);
				assertTrue("Wrong annotation", annotations[1].value() == 2);
				IFooContainer [] containers = e.getAnnotationsByType(IFooContainer.class);
				assertTrue("Wrong count", containers.length == 1);
				assertTrue("Wrong annotation", containers[0].value()[0].value() == 2);
			}
		}
	}
	public void testRepeatedAnnotations25() {
		Set<? extends Element> actualElments = roundEnv.getElementsAnnotatedWith(IFooContainer.class); // discovery is always in terms of container
		assertNotNull("RoundEnvironment#getElementsAnnotatedWith returned null", actualElments);
		assertTrue("Found unexpected elements", actualElments.size() == 2);
		IFooContainer annotationOnSubclass = null, annotationOnJep7 = null;
		for (Element e : actualElments) {
			if ("SubClass3".equals(e.getSimpleName().toString())) {
				annotationOnSubclass = e.getAnnotation(IFooContainer.class);
			} else if ("JEP120_7".equals(e.getSimpleName().toString())) {
				annotationOnJep7 = e.getAnnotation(IFooContainer.class);
			}
		}
		assertTrue("Should be equals", annotationOnJep7.equals(annotationOnSubclass));
	}

	public void testTypeAnnotations26() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.Iface");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;

				List<? extends VariableElement> list = method.getParameters();
				VariableElement param = list.get(0);
				verifyAnnotations(param, new String[]{});
			}
		}
	}

	public void testTypeAnnotations27() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model8.a.Test");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		for (Element member : members) {
			if ("foo".equals(member.getSimpleName().toString())) {
				ExecutableElement method = (ExecutableElement) member;

				List<? extends TypeParameterElement> list = method.getTypeParameters();
				TypeParameterElement tParam = list.get(0);
				verifyAnnotations(tParam, new String[]{"@MarkerContainer(value=[@targets.model8.a.Marker, @targets.model8.a.Marker])"});
			}
		}

	}
	// Disabled for now. Javac includes the CLASS element of the package-info in the root element if there's one.
	// But ECJ includes the Package element.
	public void testPackageAnnotations() {
		if ( roundNo++ == 0) {
			this.reportSuccessAlready = false;
			try {
				createPackageBinary();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.setProperty(this.getClass().getName(), "Processor did not fully do the job");
		} else {
			this.reportSuccessAlready = true;
			PackageElement packageEl = null;
			for (Element element : roundEnv.getRootElements()) {
				if (element.getKind() == ElementKind.PACKAGE) {
					packageEl = (PackageElement) element;
				} else {
					System.out.println(element);
				}
			}
			assertNotNull("Package element should not be null", packageEl);
			assertEquals("Incorrect package name", simpleName, packageEl.getSimpleName().toString());
			assertEquals("Incorrect package name", packageName, packageEl.getQualifiedName().toString());
			assertFalse("Package should not be unnamed", packageEl.isUnnamed());
		}
	}
	public void testBug520540() {
		PackageElement packageElement = _elementUtils.getPackageElement("targets.bug520540");
		assertNotNull("package element should not be null", packageElement);
		List<? extends Element> enclosedElements = packageElement.getEnclosedElements();
		assertEquals("Incorrect no of elements", 5, enclosedElements.size());
		List<String> typeElements = new ArrayList<>();
		for (Element element : enclosedElements) {
			if (element instanceof TypeElement) {
				typeElements.add(((TypeElement) element).getQualifiedName().toString());
			}
		}
		String[] types = new String[] { "targets.bug520540.GenericType", "targets.bug520540.MyEnum",
				"targets.bug520540.TypeEx", "targets.bug520540.TypeA", "targets.bug520540.AnnotB" };
		for (String string : types) {
			typeElements.remove(string);
		}
		assertEquals("found incorrect types", 0, typeElements.size());
	}
	public void testBug526288() {
		PackageElement packageElement = _elementUtils.getPackageElement("targets.testBug526288");
		assertNotNull("package element should not be null", packageElement);
		assertNull("package should have no enclosing element", packageElement.getEnclosingElement());
	}
	public void testEnumConstArguments() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.bug521812.MyEnum");
		List<? extends Element> enclosedElements = annotatedType.getEnclosedElements();
		ExecutableElement constr = null;
		for (Element element : enclosedElements) {
			if (element.getSimpleName().toString().equals("<init>")) {
				constr = (ExecutableElement) element;
			}
		}
		assertNotNull("constructor should not be null", constr);
		List<? extends VariableElement> parameters = constr.getParameters();
		ExecutableType asType = (ExecutableType) constr.asType();
		List<? extends TypeMirror> parameterTypes = asType.getParameterTypes();
		assertEquals("param count and param type count should be same", parameters.size(), parameterTypes.size());
		for(int i = 0; i < parameters.size(); i++) {
			VariableElement param = parameters.get(i);
			TypeMirror asType2 = param.asType();
			assertEquals("Parameter type should be same", param.asType(), asType2);
		}
	}
	public void testBug531717() {
		NoType noType = _typeUtils.getNoType(TypeKind.NONE);
		TypeMirror erasure = _typeUtils.erasure(noType);
		assertSame("NoType should be same", noType, erasure);
		NullType nullType = _typeUtils.getNullType();
		erasure = _typeUtils.erasure(nullType);
		assertSame("NoType should be same", nullType, erasure);
	}
	public void testMethodAnnotation() {
		TypeElement annotatedType = _elementUtils.getTypeElement("targets.model9.Y");
		List<? extends Element> members = _elementUtils.getAllMembers(annotatedType);
		ExecutableElement method = null;
		for (Element member : members) {
			if ("m".equals(member.getSimpleName().toString())) {
				method = (ExecutableElement) member;
			}
		}
		assertNotNull("Method should not be null", method);
		verifyAnnotations(method, new String[]{"@Deprecated()"});
	}
	public void testBug544288() {
		TypeElement type = _elementUtils.getTypeElement("targets.bug544288.TestEntity");
		List<? extends Element> members = _elementUtils.getAllMembers(type);
		VariableElement field = null;
		for (Element member : members) {
			if ("fieldWithTypeUse".equals(member.getSimpleName().toString())) {
				field = (VariableElement) member;
			}
		}
		assertNotNull("Should not be null", field);
		TypeMirror asType = field.asType();
		Element asElement = _typeUtils.asElement(asType);
		verifyAnnotations(asElement, new String[]{"@Deprecated()"});
	}
	private void createPackageBinary() throws IOException {
		String path = packageName.replace('.', '/');
		ClassLoader loader = getClass().getClassLoader();
		try (InputStream in = loader.getResourceAsStream(path + "/package-info.class")) {
			Filer filer = processingEnv.getFiler();
			try (OutputStream out = filer.createClassFile(packageName + ".package-info").openOutputStream()) {
				if (in != null && out != null) {
					int c = in.read();
					while (c != -1) {
						out.write(c);
						c = in.read();
					}
				}
			}
		}
	}

	private String getExceptionStackTrace(Throwable t) {
		StringBuilder buf = new StringBuilder(t.getMessage());
		StackTraceElement[] traces = t.getStackTrace();
		for (int i = 0; i < traces.length; i++) {
			StackTraceElement trace = traces[i];
			buf.append("\n\tat " + trace);
			if (i == 12)
				break; // Don't dump all stacks
		}
		return buf.toString();
	}

	private void verifyAnnotations(AnnotatedConstruct construct, String[] annots) {
		List<? extends AnnotationMirror> annotations = construct.getAnnotationMirrors();
		assertEquals("Incorrect no of annotations", annots.length, annotations.size());
		for(int i = 0, length = annots.length; i < length; i++) {
			AnnotationMirror mirror = annotations.get(i);
			assertEquals("Invalid annotation value", annots[i], getAnnotationString(mirror));
		}
	}

	private void verifyAnnotations(AnnotatedConstruct construct, String[] annots, String [] alternateAnnots) {
		List<? extends AnnotationMirror> annotations = construct.getAnnotationMirrors();
		assertEquals("Incorrect no of annotations", annots.length, annotations.size());
		for(int i = 0, length = annots.length; i < length; i++) {
			AnnotationMirror mirror = annotations.get(i);
			assertEquals("Invalid annotation value", annots[i], alternateAnnots[i], getAnnotationString(mirror));
		}
	}

	private String getAnnotationString(AnnotationMirror annot) {
		DeclaredType annotType = annot.getAnnotationType();
		TypeElement type = (TypeElement) annotType.asElement();
		StringBuilder buf = new StringBuilder("@" + type.getSimpleName());
		Map<? extends ExecutableElement, ? extends AnnotationValue> values = annot.getElementValues();
		Set<? extends ExecutableElement> keys = values.keySet();
		buf.append('(');
		for (ExecutableElement executableElement : keys) { // @Marker3()
			buf.append(executableElement.getSimpleName());
			buf.append('=');
			AnnotationValue value = values.get(executableElement);
			buf.append(value.getValue());
		}
		buf.append(')');
		return buf.toString();
	}

	private <A extends Annotation> void examineSE8AnnotationMethods(String msg, AnnotatedConstruct construct,  String value) {
		Type annot = construct.getAnnotation(Type.class);
		assertNotNull(msg + "Annotation for element " + construct.toString() + " should not be null", annot);
		assertSame(msg + "Invalid annotation type" , Type.class, annot.annotationType());
		assertEquals(msg + "Invalid annotation value", value, annot.value());

		Type[] annots = construct.getAnnotationsByType(Type.class);
		assertEquals(msg + "Incorrect no of annotations", 1, annots.length);
		annot = annots[0];
		assertSame(msg + "Invalid annotation type" , Type.class, annots[0].annotationType());
		assertEquals(msg + "Invalid annotation value", value, annot.value());
	}

	@Override
	public void reportError(String msg) {
		throw new AssertionFailedError(msg);
	}

	public void assertModifiers(Set<Modifier> modifiers, String[] expected) {
		assertEquals("Incorrect no of modifiers", modifiers.size(), expected.length);
		Set<String> actual = new HashSet<>(expected.length);
		for (Modifier modifier : modifiers) {
			actual.add(modifier.toString());
		}
		for(int i = 0, length = expected.length; i < length; i++) {
			boolean result = actual.remove(expected[i]);
			if (!result) reportError("Modifier not present :" + expected[i]);
		}
		if (!actual.isEmpty()) {
			reportError("Unexpected modifiers present:" + actual.toString());
		}
	}
	public void assertTrue(String msg, boolean value) {
		if (!value) reportError(msg);
	}
	public void assertFalse(String msg, boolean value) {
		if (value) reportError(msg);
	}
	public void assertSame(String msg, Object obj1, Object obj2) {
		if (obj1 != obj2) {
			reportError(msg + ", should be " + obj1.toString() + " but " + obj2.toString());
		}
	}
	public void assertNotSame(String msg, Object obj1, Object obj2) {
		if (obj1 == obj2) {
			reportError(msg + ", " + obj1.toString() + " should not be same as " + obj2.toString());
		}
	}
	public void assertNotNull(String msg, Object obj) {
		if (obj == null) {
			reportError(msg);
		}
	}
	public void assertNull(String msg, Object obj) {
		if (obj != null) {
			reportError(msg);
		}
	}
    public void assertEquals(String message, Object expected, Object actual) {
        if (equalsRegardingNull(expected, actual)) {
            return;
        } else {
        	reportError(message + ", expected " + expected.toString() + " but was " + actual.toString());
        }
    }

    public void assertEquals(String message, Object expected, Object alternateExpected, Object actual) {
        if (equalsRegardingNull(expected, actual) || equalsRegardingNull(alternateExpected, actual)) {
            return;
        } else {
        	reportError(message + ", expected " + expected.toString() + " but was " + actual.toString());
        }
    }

    static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }
        return expected.equals(actual);
    }

	public void assertEquals(String msg, int expected, int actual) {
		if (expected != actual) {
			StringBuilder buf = new StringBuilder();
			buf.append(msg);
			buf.append(", expected " + expected + " but was " + actual);
			reportError(buf.toString());
		}
	}
	public void assertEquals(Object expected, Object actual) {
		if (expected != actual) {

		}
	}
	private static class AssertionFailedError extends Error {
		private static final long serialVersionUID = 1L;

		public AssertionFailedError(String msg) {
			super(msg);
		}
	}
}
