/*******************************************************************************
 * Copyright (c) 2008, 2012, Walter Harley and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.modeltester;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.apt.pluggable.tests.ProcessorTestStatus;
import org.eclipse.jdt.apt.pluggable.tests.annotations.LookAt;
import org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTestTrigger;

/**
 * Testing annotation processors through JUnit in the IDE is complex, because each test requires
 * something different of the processor and all processors must coexist in the plugin registry, and
 * because the processor has very limited communication with the rest of the IDE. So, we make one
 * processor run many tests. The JUnit tests specify which test to run by passing its name in to the
 * ModelTestTrigger annotation. Test failures are reported via the Messager interface.
 *
 * @since 3.5
 */
@SupportedAnnotationTypes( { "org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTestTrigger" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( {})
public class ModelTesterProc extends AbstractProcessor {

	public static final String TEST_FIELD_TYPE_PKG = "p";
	public static final String TEST_FIELD_TYPE_CLASS = "Foo";
	public static final String TEST_FIELD_TYPE_SOURCE =
		"package p;\n" +
		"import org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTestTrigger;\n" +
		"import org.eclipse.jdt.apt.pluggable.tests.annotations.LookAt;\n" +
		"@ModelTestTrigger(test = \"testFieldType\")" +
		"public class Foo {\n" +
		"    @LookAt\n" +
		"    private int _fInt = 0;\n" +
		"    @LookAt\n" +
		"    private String _fString = \"\";\n" +
		"    @LookAt\n" +
		"    private Foo _fFoo = null;\n" +
		"}";

	public static final String TEST_METHOD_TYPE_PKG = "p";
	public static final String TEST_METHOD_TYPE_CLASS = "Foo";
	public static final String TEST_METHOD_TYPE_SOURCE =
		"package p;\n" +
		"import org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTestTrigger;\n" +
		"import org.eclipse.jdt.apt.pluggable.tests.annotations.LookAt;\n" +
		"@ModelTestTrigger(test = \"testMethodType\")" +
		"public class Foo {\n" +
		"    @LookAt\n" +
		"    private Foo self() { return this;}\n" +
		"}";

	@SuppressWarnings("unused")
	private ProcessingEnvironment _processingEnv;

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 *      javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		ProcessorTestStatus.setProcessorRan();
		if (!roundEnv.processingOver() && !annotations.isEmpty()) {
			round(annotations, roundEnv);
		}
		return true;
	}

	/**
	 * Perform a round of processing: for a given annotation instance, determine what test method it
	 * specifies, and invoke that method, passing in the annotated element.
	 */
	private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement modelTesterAnno = annotations.iterator().next();
		Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(modelTesterAnno);
		for (Element annotatedEl : annotatedEls) {
			ModelTestTrigger modelTesterMirror = annotatedEl.getAnnotation(ModelTestTrigger.class);
			String testMethodName = modelTesterMirror.test();
			String arg0 = modelTesterMirror.arg0();
			String arg1 = modelTesterMirror.arg1();
			if (null != testMethodName && testMethodName.length() > 0) {
				try {
					Method testMethod = ModelTesterProc.class.getMethod(testMethodName,
							RoundEnvironment.class, Element.class, String.class, String.class);
					testMethod.invoke(this, roundEnv, annotatedEl, arg0, arg1);
				} catch (Exception e) {
					Throwable t;
					t = (e instanceof InvocationTargetException) ? t = e.getCause() : e;
					t.printStackTrace();
					// IllegalStateException probably means test method called ProcessorTestStatus.fail()
					String msg = (t instanceof IllegalStateException) ?
							t.getMessage() :
							t.getClass().getSimpleName() + " invoking test method " +
							testMethodName + " - see console for details";
					ProcessorTestStatus.fail(msg);
				}
			}
		}
	}

	/**
	 * Check the types of some fields (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=248163)
	 * @see #TEST_FIELD_TYPE_SOURCE
	 */
	public void testFieldType(RoundEnvironment roundEnv, Element e, String arg0, String arg1)
			throws Exception
	{
		Map<String, VariableElement> fields = new HashMap<String, VariableElement>();
		Iterable<? extends Element> elements;
		// With this line uncommented, test would pass:
		// elements = e.getEnclosedElements();
		// With this line uncommented, test would fail:
		elements = roundEnv.getElementsAnnotatedWith(LookAt.class);
		for (VariableElement field : ElementFilter.fieldsIn(elements)) {
			fields.put(field.getSimpleName().toString(), field);
		}

		VariableElement fInt = fields.get("_fInt");
		if (fInt == null) {
			ProcessorTestStatus.fail("Field _fInt was not found");
		}
		if (fInt.getKind() != ElementKind.FIELD) {
			ProcessorTestStatus.fail("ElementKind of field _fInt was " + fInt.getKind() +
					", expected FIELD");
		}
		TypeMirror fIntType = fInt.asType();
		if (fIntType.getKind() != TypeKind.INT) {
			ProcessorTestStatus.fail("Field _fInt asType returned type kind of " + fIntType.getKind() +
					", expected INT");
		}

		VariableElement fString = fields.get("_fString");
		if (fString == null) {
			ProcessorTestStatus.fail("Field _fString was not found");
		}
		if (fString.getKind() != ElementKind.FIELD) {
			ProcessorTestStatus.fail("ElementKind of field _fString was " + fString.getKind() +
					", expected FIELD");
		}
		TypeMirror fStringType = fString.asType();
		if (fStringType.getKind() != TypeKind.DECLARED) {
			ProcessorTestStatus.fail("Field _fString asType returned type kind of " + fStringType.getKind() +
					", expected DECLARED");
		}
		VariableElement fFoo = fields.get("_fFoo");
		if (fFoo == null) {
			ProcessorTestStatus.fail("Field _fFoo was not found");
		}
		if (fFoo.getKind() != ElementKind.FIELD) {
			ProcessorTestStatus.fail("ElementKind of field _fFoo was " + fFoo.getKind() +
					", expected FIELD");
		}
		TypeMirror fFooType = fFoo.asType();
		if (fFooType.getKind() != TypeKind.DECLARED) {
			ProcessorTestStatus.fail("Field _fFoo asType returned type kind of " + fFooType.getKind() +
					", expected DECLARED");
		}
	}

	/**
	 * Check the types of some fields (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=248163)
	 * @see #TEST_METHOD_TYPE_SOURCE
	 */
	public void testMethodType(RoundEnvironment roundEnv, Element e, String arg0, String arg1)
			throws Exception
	{
		Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>();
		Iterable<? extends Element> elements;
		// With this line uncommented, test would pass:
		// elements = e.getEnclosedElements();
		// With this line uncommented, test would fail:
		elements = roundEnv.getElementsAnnotatedWith(LookAt.class);
		for (ExecutableElement method : ElementFilter.methodsIn(elements)) {
			methods.put(method.getSimpleName().toString(), method);
		}

		ExecutableElement mSelf = methods.get("self");
		if (mSelf == null) {
			ProcessorTestStatus.fail("Method self() was not found");
		}
		if (mSelf.getKind() != ElementKind.METHOD) {
			ProcessorTestStatus.fail("ElementKind of method self() was " + mSelf.getKind() +
					", expected METHOD");
		}
		TypeMirror mSelfType = mSelf.getReturnType();
		if (mSelfType.getKind() != TypeKind.DECLARED) {
			ProcessorTestStatus.fail("Method self() asType returned type kind of " + mSelfType.getKind() +
					", expected DECLARED");
		}
	}
}
