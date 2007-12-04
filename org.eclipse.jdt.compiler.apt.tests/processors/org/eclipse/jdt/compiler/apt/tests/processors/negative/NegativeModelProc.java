/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.negative;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * An annotation processor that investigates the model produced by code containing
 * semantic errors such as missing types. To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc to the
 * command line.
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions("org.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc")
public class NegativeModelProc extends AbstractProcessor
{

	private static final String CLASSNAME = NegativeModelProc.class.getName();

	/*
	 * Sometimes it's necessary to work on one test at a time, while
	 * ignoring failures in earlier tests. 
	 */
	private static final boolean testNegative1 = true;
	private static final boolean testNegative2 = true;
	private static final boolean testNegative3 = true;
	
	/**
	 * Report an error to the test case code.  
	 * This is not the same as reporting via Messager!  Use this if some API fails.
	 * @param value will be displayed in the test output, in the event of failure.
	 * Can be anything except "succeeded".
	 */
	public static void reportError(String value) {
		// Uncomment for processor debugging - don't report error
		// value = "succeeded";
		System.setProperty(CLASSNAME, value);
	}
	
	/**
	 * Report success to the test case code
	 */
	public static void reportSuccess() {
		System.setProperty(CLASSNAME, "succeeded");
	}

	private Elements _elementUtils;
	
	// Initialized in collectElements()
	private TypeElement _elementN1;
	private TypeElement _elementN2;
	private TypeElement _elementN3;

	// Report failures on tests that are already known to be unsupported
	private boolean _reportFailingCases = true;

	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
	}

	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			// We're not interested in the postprocessing round.
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(CLASSNAME)) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}
		
		if (!collectElements()) {
			return false;
		}
		
		if (testNegative1 && !checkNegative1()) {
			return false;
		}
		
		if (testNegative2 && !checkNegative2()) {
			return false;
		}
		
		if (testNegative3 && !checkNegative3()) {
			return false;
		}
		
		reportSuccess();
		return false;
	}

	/**
	 * Collect some elements that will be reused in various tests
	 * @return true if all tests passed
	 */
	private boolean collectElements() {
		_elementN1 = _elementUtils.getTypeElement("targets.negative.pa.Negative1");
		if (null == _elementN1 || _elementN1.getKind() != ElementKind.CLASS) {
			reportError("Element Negative1 was not found or was not a class");
			return false;
		}
		_elementN2 = _elementUtils.getTypeElement("targets.negative.pa.Negative2");
		if (null == _elementN2 || _elementN2.getKind() != ElementKind.CLASS) {
			reportError("Element Negative2 was not found or was not a class");
			return false;
		}
		_elementN3 = _elementUtils.getTypeElement("targets.negative.pa.Negative3");
		if (null == _elementN3 || _elementN3.getKind() != ElementKind.CLASS) {
			reportError("Element Negative3 was not found or was not a class");
			return false;
		}
		// TODO: try collecting a nested or secondary type that extends a missing type
		return true;
	}
	
	/**
	 * Check the annotations in the model of targets.negative.pa.Negative1
	 * @return true if all tests passed
	 */
	private boolean checkNegative1() {
		AnnotationMirror am3 = findAnnotation(_elementN1, "A3");
		if (_reportFailingCases && null == am3) {
			reportError("Couldn't find annotation A3 on class Negative1");
			return false;
		}
		List<? extends Element> enclosedElements = _elementN1.getEnclosedElements();
		boolean foundM1 = false; // do we find an element of unresolved type?
		for (Element element : enclosedElements) {
			String name = element.getSimpleName().toString();
			if ("m1".equals(name)) {
				foundM1 = true;
				TypeKind tk = element.asType().getKind();
				if (tk != TypeKind.ERROR && tk != TypeKind.DECLARED) {
					reportError("Field Negative1.m1 has a type of unexpected kind " + tk);
					return false;
				}
				AnnotationMirror am4 = findAnnotation(element, "A4");
				if (_reportFailingCases && null == am4) {
					reportError("Couldn't find annotation A4 on field Negative1.m1");
					return false;
				}
			}
			else if ("i1".equals(name)) {
				AnnotationMirror am5 = findAnnotation(element, "A5");
				if (_reportFailingCases && null == am5) {
					reportError("Couldn't find annotation A5 on field Negative1.i1");
					return false;
				}
			}
			else if ("m2".equals(name)) {
				AnnotationMirror am8 = findAnnotation(element, "A8");
				if (_reportFailingCases && null == am8) {
					reportError("Couldn't find annotation A8 on field Negative1.m2");
					return false;
				}
			}
			else if ("s1".equals(name)) {
				AnnotationMirror am = findAnnotation(element, "Anno1");
				if (null == am) {
					reportError("Couldn't find annotation Anno on field Negative1.s1");
					return false;
				}
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = am.getElementValues();
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
					if ("value".equals(entry.getKey().getSimpleName().toString())) {
						if (!"spud".equals(entry.getValue().getValue())) {
							reportError("Unexpected value for Anno1 on Negative1.s1: " + entry.getValue().getValue());
							return false;
						}
					}
				}
			}
		}
		if (_reportFailingCases && !foundM1) {
			reportError("Couldn't find field Negative1.m1, presumably because its type is missing");
			return false;
		}
		return true;
	}
	
	/**
	 * Check the annotations in the model of targets.negative.pa.Negative2
	 * @return true if all tests passed
	 */
	private boolean checkNegative2() {
		List<? extends Element> enclosedElements = _elementN2.getEnclosedElements();
		for (Element element : enclosedElements) {
			String name = element.getSimpleName().toString();
			if ("m1".equals(name)) {
				AnnotationMirror am2 = findAnnotation(element, "Anno2");
				if (_reportFailingCases && null == am2) {
					reportError("Couldn't find annotation Anno2 on method Negative2.m1");
					return false;
				}
			}
			else if ("m2".equals(name)) {
				AnnotationMirror am1 = findAnnotation(element, "Anno1");
				if (_reportFailingCases && null == am1) {
					reportError("Couldn't find annotation Anno1 on method Negative2.m2");
					return false;
				}
				AnnotationMirror am3 = findAnnotation(element, "FakeAnno3");
				if (_reportFailingCases && null == am3) {
					reportError("Couldn't find annotation FakeAnno3 on method Negative2.m2");
					return false;
				}
			}
			else if ("m3".equals(name)) {
				AnnotationMirror am2 = findAnnotation(element, "Anno2");
				if (_reportFailingCases && null == am2) {
					reportError("Couldn't find annotation Anno2 on method Negative2.m3");
					return false;
				}
				AnnotationMirror am3 = findAnnotation(element, "FakeAnno3");
				if (_reportFailingCases && null == am3) {
					reportError("Couldn't find annotation FakeAnno3 on method Negative2.m3");
					return false;
				}
			}
			else if ("m4".equals(name)) {
				AnnotationMirror am4 = findAnnotation(element, "Anno4");
				if (_reportFailingCases && null == am4) {
					reportError("Couldn't find annotation Anno4 on method Negative2.m4");
					return false;
				}
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = am4.getElementValues();
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
					if ("value".equals(entry.getKey().getSimpleName().toString())) {
						String value = entry.getValue().getValue().toString();
						if (!"123".equals(value) && !"<error>".equals(value)) {
							reportError("Unexpected value for Anno4 on Negative1.s1: " + value);
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Check the model of targets.negative.pa.Negative3
	 * @return true if all tests passed
	 */
	private boolean checkNegative3() {
		List<? extends Element> enclosedElements = _elementN3.getEnclosedElements();
		for (Element element : enclosedElements) {
			String name = element.getSimpleName().toString();
			if ("foo".equals(name)) {
				ElementKind kind = element.getKind();
				if (_reportFailingCases && ElementKind.METHOD != kind) {
					reportError("Element 'foo' was expected to be a METHOD but was a " + kind);
					return false;
				}
				List<? extends VariableElement> params = ((ExecutableElement)element).getParameters();
				if (_reportFailingCases && (params == null || params.size() != 1)) {
					reportError("Expected method Negative3.foo() to have one param, but found " +
							(params == null ? 0 : params.size()));
					return false;
				}
				VariableElement param1 = params.iterator().next();
				TypeMirror param1Type = param1.asType();
				TypeKind tkind = param1Type.getKind();
				if (_reportFailingCases && TypeKind.ERROR != tkind && TypeKind.DECLARED != tkind) {
					reportError("Expected the TypeKind of Negative3.foo() param to be ERROR or DECLARED, but found " + tkind);
					return false;
				}
				// The behavior of TypeMirror.toString() is suggested, not required, by its javadoc. 
				// So, this is a test of whether we behave like javac, rather than whether we meet the spec.
				String pname = param1Type.toString();
				if (_reportFailingCases && !"M2.M3.M4".equals(pname)) {
					reportError("Expected toString() of the type of Negative3.foo() param to be M2.M3.M4, but found " + pname);
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Find a particular annotation on a specified element.
	 * @param el the annotated element
	 * @param name the simple name of the annotation
	 * @return a mirror for the annotation, or null if the annotation was not found.
	 */
	private AnnotationMirror findAnnotation(Element el, String name) {
		for (AnnotationMirror am : el.getAnnotationMirrors()) {
			DeclaredType annoType = am.getAnnotationType();
			if (null != annoType) {
				Element annoTypeElement = annoType.asElement();
				if (null != annoTypeElement) {
					if (name.equals(annoTypeElement.getSimpleName().toString())) {
						return am;
					}
				}
			}
		}
		return null;
	}
}
