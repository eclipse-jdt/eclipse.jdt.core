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
import javax.lang.model.type.DeclaredType;
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

	// Set this to false in order to pass the tests that are not currently supported
	private boolean _testFailingCases = true;

	
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
		
		if (!checkAnnotations()) {
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
		// TODO: try collecting a nested or secondary type that extends a missing type
		return true;
	}
	
	/**
	 * Check the annotations in the model of targets.negative.pa.Negative1
	 * @return true if all tests passed
	 */
	private boolean checkAnnotations() {
		AnnotationMirror am3 = findAnnotation(_elementN1, "A3");
		if (_testFailingCases && null == am3) {
			reportError("Couldn't find annotation A3 on class Negative1");
			return false;
		}
		List<? extends Element> enclosedElements = _elementN1.getEnclosedElements();
		for (Element element : enclosedElements) {
			String name = element.getSimpleName().toString();
			if ("m1".equals(name)) {
				AnnotationMirror am4 = findAnnotation(element, "A4");
				if (_testFailingCases && null == am4) {
					reportError("Couldn't find annotation A4 on field Negative1.m1");
					return false;
				}
			}
			else if ("i1".equals(name)) {
				AnnotationMirror am5 = findAnnotation(element, "A5");
				if (_testFailingCases && null == am5) {
					reportError("Couldn't find annotation A5 on field Negative1.i1");
					return false;
				}
			}
			else if ("m2".equals(name)) {
				AnnotationMirror am8 = findAnnotation(element, "A8");
				if (_testFailingCases && null == am8) {
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
