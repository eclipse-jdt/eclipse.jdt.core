/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * A processor that explores the "model" target hierarchy and complains if it does
 * not find what it expects.  To enable this processor, add 
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc to the command line.
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ElementProc extends AbstractProcessor {
	
	private static final String CLASSNAME = ElementProc.class.getName();
	
	// The set of elements we expect getRootElements to return
	private static final String[] ROOT_ELEMENT_NAMES = new String[] {"AnnoZ", "A", "IA", "AB", "AC", "D", "IB", "IC"};
	
	/**
	 * Report an error to the test case code
	 * @param value
	 */
	public static void reportError(String value) {
		// Debugging - don't report error
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
	private Types _typeUtils;
	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_elementUtils = processingEnv.getElementUtils();
		_typeUtils = processingEnv.getTypeUtils();
	}

	// Always return false from this processor, because it supports "*".
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
		
		// Verify that we get the root elements we expect
		Set<String> expectedRootElementNames = new HashSet<String>(ROOT_ELEMENT_NAMES.length);
		for (String name : ROOT_ELEMENT_NAMES) {
			expectedRootElementNames.add(name);
		}
		Set<? extends Element> actualRootElements = roundEnv.getRootElements();
		if (null == actualRootElements) {
			ElementProc.reportError("getRootElements() returned null");
			return false;
		}
		for (Element e : actualRootElements) {
			String name = e.getSimpleName().toString();
			if (!expectedRootElementNames.remove(name)) {
				ElementProc.reportError("Missing root element " + name);
			}
		}
		if (!expectedRootElementNames.isEmpty()) {
			ElementProc.reportError("Found extra root elements including " + expectedRootElementNames.iterator().next());
			return false;
		}
		
		// Check some basic attributes
		TypeElement elementIA = _elementUtils.getTypeElement("targets.model.pa.IA");
		if (elementIA == null) {
			ElementProc.reportError("element IA was not found");
			return false;
		}
		TypeElement elementAB = _elementUtils.getTypeElement("targets.model.pb.AB");
		if (elementAB == null) {
			ElementProc.reportError("element AB was not found");
			return false;
		}
		if (elementIA.getKind() != ElementKind.INTERFACE) {
			ElementProc.reportError("IA claims to not be an interface");
			return false;
		}
		if (elementAB.getKind() != ElementKind.CLASS) {
			ElementProc.reportError("AB claims to not be a class");
			return false;
		}
		
		// Can we look at what interfaces AB implements?
		List<? extends TypeMirror> ABinterfaces = elementAB.getInterfaces();
		if (null == ABinterfaces) {
			ElementProc.reportError("AB.getInterfaces() returned null");
			return false;
		}
		boolean foundIAinterface = false;
		for (TypeMirror type : ABinterfaces) {
			Element decl = _typeUtils.asElement(type);
			if (null == decl) {
				ElementProc.reportError("One of AB's interfaces, " + type.toString() + ", produced null from Types.asElement()");
				return false;
			}
			if (elementIA.equals(decl)) {
				foundIAinterface = true;
				break;
			}
		}
		if (!foundIAinterface) {
			ElementProc.reportError("AB does not have IA as an interface");
			return false;
		}
		
		ElementProc.reportSuccess();
		return false;
	}

}
