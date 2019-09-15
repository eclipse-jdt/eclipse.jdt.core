/*******************************************************************************
 * Copyright (c) 2014 Jesper Steen Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jesper Steen Moller - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.modeltester;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.apt.pluggable.tests.ModelTests;
import org.eclipse.jdt.apt.pluggable.tests.ProcessorTestStatus;
import org.eclipse.jdt.apt.pluggable.tests.annotations.LookAt;
import org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTest8Trigger;

/**
 * This processor tests features specific to JEP 118.
 * One processor can run many tests. The JUnit tests specify which test to run by passing its name in to the
 * ModelTest8Trigger annotation.
 *
 * Although this test processor only needs to be run with 1.8 JRE, we don't explicitly state the supported version.
 * The clients invoking this must ensure that this is invoked only with JRE 1.8 and above,
 * like it's done in {@link ModelTests#testMethodParameters}.
 *
 * @since 3.9
 */
@SupportedAnnotationTypes( { "org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTest8Trigger" })
@SupportedOptions( {})
public class ModelTester8Proc extends AbstractProcessor {
	public static final String TEST_METHOD_PARAMETERS_TYPE1_PKG = "p";
	public static final String TEST_METHOD_PARAMETERS_TYPE1_CLASS = "Bar";
	public static final String TEST_METHOD_PARAMETERS_TYPE1_SOURCE =
			"package p;\n" +
					"public class Bar {\n" +
					"    public void otherStuff(final double fun, String beans) { }\n" +
					"}";

	public static final String TEST_METHOD_PARAMETERS_TYPE2_PKG = "p";
	public static final String TEST_METHOD_PARAMETERS_TYPE2_CLASS = "MyEnum";
	public static final String TEST_METHOD_PARAMETERS_TYPE2_SOURCE =
			"package p;\n" +
					"\n" +
					"public enum MyEnum {\n" +
					"	ONE(1), TWO(2);\n" +
					"	\n" +
					"	private MyEnum(final int finalIntValue) { this.var = finalIntValue; }\n" +
					"	int var;\n" +
					"}\n";

	public static final String TEST_METHOD_PARAMETERS_TYPE3_PKG = "p";
	public static final String TEST_METHOD_PARAMETERS_TYPE3_CLASS = "Foo";
	public static final String TEST_METHOD_PARAMETERS_TYPE3_SOURCE =
		"package p;\n" +
		"import org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTest8Trigger;\n" +
		"import org.eclipse.jdt.apt.pluggable.tests.annotations.LookAt;\n" +
		"@ModelTest8Trigger(test = \"testMethodParameters\")" +
		"public class Foo {\n" +
		"    @LookAt\n" +
		"    public Bar doStuff(final int number, String textual) { return null; }\n" +
		"    @LookAt\n" +
		"    public MyEnum guess(final int isItOne) { return isItOne == 1 ? MyEnum.ONE : MyEnum.TWO; }\n" +
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

	@Override
    public SourceVersion getSupportedSourceVersion() {
    	return SourceVersion.latestSupported();
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
			ModelTest8Trigger modelTesterMirror = annotatedEl.getAnnotation(ModelTest8Trigger.class);
			String testMethodName = modelTesterMirror.test();
			String arg0 = modelTesterMirror.arg0();
			String arg1 = modelTesterMirror.arg1();
			if (null != testMethodName && testMethodName.length() > 0) {
				try {
					Method testMethod = ModelTester8Proc.class.getMethod(testMethodName,
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
	 * Check the types of some methods (check that the annotation processing uses the parsed MethodParameters
	 * attribute from class files according to JEP 118)
	 * @see #TEST_METHOD_PARAMETERS_TYPE1_SOURCE
	 * @see #TEST_METHOD_PARAMETERS_TYPE2_SOURCE
	 * @see #TEST_METHOD_PARAMETERS_TYPE3_SOURCE
	 */
	public void testMethodParameters(RoundEnvironment roundEnv, Element e, String arg0, String arg1) throws Exception {
		Map<String, ExecutableElement> methods = new HashMap<String, ExecutableElement>();
		Iterable<? extends Element> elements;

		elements = roundEnv.getElementsAnnotatedWith(LookAt.class);
		for (ExecutableElement method : ElementFilter.methodsIn(elements)) {
			methods.put(method.getSimpleName().toString(), method);
		}

		// Examine the easy case, the Foo.doStuff method
		ExecutableElement mDoStuff = methods.get("doStuff");
		if (mDoStuff == null) {
			ProcessorTestStatus.fail("Method doStuff() was not found");
		}
		if (mDoStuff.getKind() != ElementKind.METHOD) {
			ProcessorTestStatus.fail("ElementKind of method doStuff() was " + mDoStuff.getKind() +
					", expected METHOD");
		}
		// Examine parameters
		List<? extends VariableElement> parameters = mDoStuff.getParameters();
		if (parameters.size() != 2) {
			ProcessorTestStatus.fail("Expected two parameters for doStuff()");
		}
		ProcessorTestStatus.assertEquals("Wrong name", "number", parameters.get(0).getSimpleName().toString());
		ProcessorTestStatus.assertEquals("Wrong name", "textual", parameters.get(1).getSimpleName().toString());

		///////////////////////////////////////////////////////////////////////////////////

		// Cool, now check 'p.Bar.otherStuff' which is also the return type of doStuff
		TypeMirror returnType = mDoStuff.getReturnType();
		if (returnType.getKind() != TypeKind.DECLARED)
			ProcessorTestStatus.fail("TypeKind of method doStuff()'s return type " + returnType.getKind() +
					", expected DECLARED");

		DeclaredType barType = (DeclaredType) returnType;
		TypeElement bar = (TypeElement) barType.asElement();

		for (Element method : bar.getEnclosedElements()) {
			if (method.getKind() == ElementKind.METHOD)
				methods.put(method.getSimpleName().toString(), (ExecutableElement)method);
		}

		ExecutableElement mOtherStuff = methods.get("otherStuff");
		if (mOtherStuff == null) {
			ProcessorTestStatus.fail("Method otherStuff() was not found");
		}
		if (mOtherStuff.getKind() != ElementKind.METHOD) {
			ProcessorTestStatus.fail("ElementKind of method otherStuff() was " + mOtherStuff.getKind() +
					", expected METHOD");
		}
		// Examine parameters
		List<? extends VariableElement> otherParameters = mOtherStuff.getParameters();
		if (otherParameters.size() != 2) {
			ProcessorTestStatus.fail("Expected two parameters for otherStuff()");
		}
		ProcessorTestStatus.assertEquals("Wrong name", "fun", otherParameters.get(0).getSimpleName().toString());
		ProcessorTestStatus.assertEquals("Wrong name", "beans", otherParameters.get(1).getSimpleName().toString());

		///////////////////////////////////////////////////////////////////////////////////

		// Examine the enum as returned by Foo.guess method
		ExecutableElement mGuess = methods.get("guess");
		if (mGuess == null) {
			ProcessorTestStatus.fail("Method guess() was not found");
		}
		if (mGuess.getKind() != ElementKind.METHOD) {
			ProcessorTestStatus.fail("ElementKind of method doStuff() was " + mGuess.getKind() +
					", expected METHOD");
		}

		// Cool, now check 'p.Bar.otherStuff' which is also the return type of doStuff
		TypeMirror guessReturnType = mGuess.getReturnType();
		if (guessReturnType.getKind() != TypeKind.DECLARED)
			ProcessorTestStatus.fail("TypeKind of method guess()'s return type " + guessReturnType.getKind() +
					", expected DECLARED");

		DeclaredType myEnumType = (DeclaredType) guessReturnType;
		TypeElement myEnumClass = (TypeElement) myEnumType.asElement();

		List<ExecutableElement> ctors = new LinkedList<ExecutableElement>();
		for (Element method : myEnumClass.getEnclosedElements()) {
			if (method.getKind() == ElementKind.CONSTRUCTOR) {
				ctors.add((ExecutableElement)method);
			}
		}

		ProcessorTestStatus.assertEquals("Bad # of constructors for MyEnum", 1, ctors.size());
		// Examine parameters
		List<? extends VariableElement> ctorParameters = ctors.get(0).getParameters();
		ProcessorTestStatus.assertEquals("Bad # of parameters for MyEnum ctor", 1, ctorParameters.size());
		ProcessorTestStatus.assertEquals("Wrong name", "finalIntValue", ctorParameters.get(0).getSimpleName().toString());
	}
}
