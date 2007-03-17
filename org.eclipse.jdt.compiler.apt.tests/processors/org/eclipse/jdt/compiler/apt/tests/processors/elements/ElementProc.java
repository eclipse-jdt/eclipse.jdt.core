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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
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

	private TypeElement _elementIA;
	private TypeElement _elementAB;

	private TypeElement _elementA;

	private TypeElement _elementString;
	
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
		
		// Verify that we get the root elements we expect
		Set<String> expectedRootElementNames = new HashSet<String>(ROOT_ELEMENT_NAMES.length);
		for (String name : ROOT_ELEMENT_NAMES) {
			expectedRootElementNames.add(name);
		}
		Set<? extends Element> actualRootElements = roundEnv.getRootElements();
		if (null == actualRootElements) {
			reportError("getRootElements() returned null");
			return false;
		}
		for (Element e : actualRootElements) {
			String name = e.getSimpleName().toString();
			if (!expectedRootElementNames.remove(name)) {
				reportError("Missing root element " + name);
			}
		}
		if (!expectedRootElementNames.isEmpty()) {
			reportError("Found extra root elements including " + expectedRootElementNames.iterator().next());
			return false;
		}
		
		if (!collectElements()) {
			return false;
		}
		
		if (!examineABInterfaces()) {
			return false;
		}
		
		if (!examineDHierarchy()) {
			return false;
		}
		
		if (!examineAMethodsAndFields()) {
			return false;
		}
		
		if (!examinePBPackage()) {
			return false;
		}
		
		ElementProc.reportSuccess();
		return false;
	}
	
	/**
	 * Collect some elements that will be reused in various tests
	 * @return true if all tests passed
	 */
	private boolean collectElements() {
		_elementIA = _elementUtils.getTypeElement("targets.model.pa.IA");
		if (_elementIA == null) {
			reportError("element IA was not found");
			return false;
		}
		if (_elementIA.getKind() != ElementKind.INTERFACE) {
			reportError("IA claims to not be an interface");
			return false;
		}
		_elementA = _elementUtils.getTypeElement("targets.model.pa.A");
		if (_elementA == null) {
			reportError("element A was not found");
			return false;
		}
		if (_elementA.getKind() != ElementKind.CLASS) {
			reportError("A claims to not be a class");
			return false;
		}
		_elementAB = _elementUtils.getTypeElement("targets.model.pb.AB");
		if (_elementAB == null) {
			reportError("element AB was not found");
			return false;
		}
		if (_elementAB.getKind() != ElementKind.CLASS) {
			reportError("AB claims to not be a class");
			return false;
		}
		_elementString = _elementUtils.getTypeElement("java.lang.String");
		return true;
	}
	
	/**
	 * Examine the interfaces that AB implements
	 * @return true if all tests passed
	 */
	private boolean examineABInterfaces() {
		List<? extends TypeMirror> interfacesAB = _elementAB.getInterfaces();
		if (null == interfacesAB) {
			reportError("AB.getInterfaces() returned null");
			return false;
		}
		boolean foundIAinterface = false;
		for (TypeMirror type : interfacesAB) {
			Element decl = _typeUtils.asElement(type);
			if (null == decl) {
				reportError("One of AB's interfaces, " + type.toString() + ", produced null from Types.asElement()");
				return false;
			}
			if (_elementIA.equals(decl)) {
				foundIAinterface = true;
				break;
			}
		}
		if (!foundIAinterface) {
			reportError("AB does not have IA as an interface");
			return false;
		}
		return true;
	}

	/**
	 * Examine the hierarchy of element D
	 * @return true if all tests passed
	 */
	private boolean examineDHierarchy() {
		// What is D's superclass?
		TypeElement elementD = _elementUtils.getTypeElement("targets.model.pb.D");
		if (elementD == null) {
			reportError("element D was not found");
			return false;
		}
		TypeMirror supertypeD = elementD.getSuperclass();
		if (null == supertypeD) {
			reportError("element D's supertype was null");
			return false;
		}
		Element superclassD = _typeUtils.asElement(supertypeD);
		if (!_elementAB.equals(superclassD)) {
			reportError("element D's superclass did not equal element AB");
			return false;
		}

		return true;
	}
	
	/**
	 * Examine the methods of element A
	 * @return true if all tests passed
	 */
	private boolean examineAMethodsAndFields() {
		// METHODS
		List<? extends Element> enclosedA = _elementA.getEnclosedElements();
		if (enclosedA == null) {
			reportError("elementA.getEnclosedElements() returned null");
			return false;
		}
		List<ExecutableElement> methodsA = ElementFilter.methodsIn(enclosedA);
		ExecutableElement methodIAString = null;
		for (ExecutableElement method : methodsA) {
			Name methodName = method.getSimpleName();
			if ("methodIAString".equals(methodName.toString())) {
				methodIAString = method;
			}
		}
		if (null == methodIAString) {
			reportError("element A did not contain methodIAString()");
			return false;
		}
		if (methodIAString.getKind() != ElementKind.METHOD) {
			reportError("A.methodIAString is not an ElementKind.METHOD");
			return false;
		}
		Element enclosingMethodIAStringInA = methodIAString.getEnclosingElement();
		if (null == enclosingMethodIAStringInA || !_elementA.equals(enclosingMethodIAStringInA)) {
			reportError("Element enclosing A.methodIAString() is not A");
			return false;
		}
		
		// RETURN AND PARAMS
		TypeMirror returnType = methodIAString.getReturnType();
		if (!(returnType instanceof DeclaredType) || returnType.getKind() != TypeKind.DECLARED) {
			reportError("Return type of A.methodIAString() is not a declared type");
			return false;
		}
		if (!_elementString.equals(((DeclaredType)returnType).asElement())) {
			reportError("Return type of A.methodIAString() does not equal java.lang.String");
			return false;
		}
		/* TODO: this doesn't work, because VariableElement.getSimpleName returns type name rather than param name!
		List<? extends VariableElement> paramsMethodIAString = methodIAString.getParameters();
		VariableElement int1 = null;
		for (VariableElement param : paramsMethodIAString) {
			if ("int1".equals(param.getSimpleName().toString())) {
				int1 = param;
			}
		}
		if (null == int1) {
			reportError("A.methodIAString() does not report a parameter named int1");
			return false;
		}
		*/
		
		// FIELDS
		List<VariableElement> fieldsA = ElementFilter.fieldsIn(enclosedA);
		VariableElement fieldAint = null;
		for (VariableElement field : fieldsA) {
			Name fieldName = field.getSimpleName();
			if ("_fieldAint".equals(fieldName.toString())) {
				fieldAint = field;
			}
		}
		if (null == fieldAint) {
			reportError("element A did not contain _fieldAint");
			return false;
		}
		if (fieldAint.getKind() != ElementKind.FIELD) {
			reportError("A._fieldAint is not an ElementKind.FIELD");
			return false;
		}
		return true;
	}

	/**
	 * Check the PackageDeclaration of pb
	 * @return true if all tests passed
	 */
	private boolean examinePBPackage() {
		Element packagePB = _elementAB.getEnclosingElement();
		if (!(packagePB instanceof PackageElement) || packagePB.getKind() != ElementKind.PACKAGE) {
			reportError("element AB is not enclosed by a package");
			return false;
		}
		if (!("targets.model.pb".equals(((PackageElement)packagePB).getQualifiedName().toString()))) {
			reportError("The name of package pb is not targets.model.pb");
			return false;
		}
		return true;
	}
}
