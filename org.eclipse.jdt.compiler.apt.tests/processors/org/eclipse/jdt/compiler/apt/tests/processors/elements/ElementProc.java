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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the "model" target hierarchy and complains if it does
 * not find what it expects.  To enable this processor, add 
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc to the command line.
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ElementProc extends BaseProcessor {
	
	// The set of elements we expect getRootElements to return in package pa
	private static final String[] ROOT_ELEMENT_NAMES = new String[] {
		"targets.model.pa.AnnoZ", "targets.model.pa.A", "targets.model.pa.IA", "targets.model.pa.ExceptionA"};
	
	// Initialized in collectElements()
	private TypeElement _elementIA;
	private TypeElement _elementAB;
	private TypeElement _elementA;
	private TypeElement _elementD;
	private TypeElement _elementDChild;
	private TypeElement _elementAnnoZ;
	private TypeElement _elementString;

	// Initialized in examineDMethods()
	private ExecutableElement _methodDvoid;
	private TypeElement _elementDEnum;
	
	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			// We're not interested in the postprocessing round.
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}
		
		if (!collectElements()) {
			return false;
		}
		
		if (!examineRoundEnv(roundEnv)) {
			return false;
		}
		
		if (!examineABInterfaces()) {
			return false;
		}
		
		if (!examineABModifiers()) {
			return false;
		}
		
		if (!examineDHierarchy()) {
			return false;
		}
		
		if (!examineAMethodsAndFields()) {
			return false;
		}
		
		if (!examineAMethodThrowables()) {
			return false;
		}
		
		if (!examineDMethods()) {
			return false;
		}
		
		if (!examineDEnum()) {
			return false;
		}
		
		if (!examinePBPackage()) {
			return false;
		}
		
		if (!examineDAnnotations()) {
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
		_elementIA = _elementUtils.getTypeElement("targets.model.pa.IA");
		if (_elementIA == null) {
			reportError("element IA was not found");
			return false;
		}
		if (_elementIA.getKind() != ElementKind.INTERFACE) {
			reportError("IA claims to not be an interface");
			return false;
		}
		if (_elementIA.getNestingKind() != NestingKind.TOP_LEVEL) {
			reportError("NestingKind of element IA is not TOP_LEVEL");
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
		
		_elementAnnoZ = _elementUtils.getTypeElement("targets.model.pa.AnnoZ");
		if (_elementAnnoZ == null) {
			reportError("element AnnoZ was not found");
			return false;
		}
		if (_elementAnnoZ.getKind() != ElementKind.ANNOTATION_TYPE) {
			reportError("AnnoZ claims to not be an annotation type");
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
		
		_elementD = _elementUtils.getTypeElement("targets.model.pb.D");
		if (_elementD == null) {
			reportError("element D was not found");
			return false;
		}
		if (_elementD.getKind() != ElementKind.CLASS) {
			reportError("D claims to not be a class");
			return false;
		}
		
		_elementDChild = _elementUtils.getTypeElement("targets.model.pb.DChild");
		if (_elementDChild == null) {
			reportError("secondary element DChild was not found");
			return false;
		}
		if (_elementDChild.getKind() != ElementKind.CLASS) {
			reportError("DChild claims to not be a class");
			return false;
		}
		_elementString = _elementUtils.getTypeElement("java.lang.String");
		return true;
	}
	
	/**
	 * Check the methods on RoundEnvironment method
	 * @return true if all tests passed
	 */
	private boolean examineRoundEnv(RoundEnvironment roundEnv) {
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
			if (e instanceof TypeElement) {
				String name = ((TypeElement)e).getQualifiedName().toString();
				if (name.startsWith("targets.model.pa.") && !expectedRootElementNames.remove(name)) {
					reportError("Missing root element " + name);
					return false;
				}
			}
		}
		if (!expectedRootElementNames.isEmpty()) {
			reportError("Found extra root elements including " + expectedRootElementNames.iterator().next());
			return false;
		}
		
		// Verify that we get the annotations we expect
		Set<? extends Element> annotatedWithAnnoZ = roundEnv.getElementsAnnotatedWith(_elementAnnoZ);
		if (null == annotatedWithAnnoZ || !annotatedWithAnnoZ.contains(_elementD)) {
			reportError("Elements annotated with AnnoZ does not include D");
			return false;
		}
		
		// targets.model.pc.Deprecation contains @Deprecated annotations
		Set<? extends Element> annotatedWithDeprecated = roundEnv.getElementsAnnotatedWith(Deprecated.class);
		if (null == annotatedWithDeprecated) {
			reportError("getElementsAnnotatedWith(@Deprecated) returned null");
			return false;
		}
		boolean foundDeprecation = false;
		for (TypeElement deprecatedElement : ElementFilter.typesIn(annotatedWithDeprecated)) {
			if ("targets.model.pc.Deprecation".equals(deprecatedElement.getQualifiedName().toString())) {
				foundDeprecation = true;
				break;
			}
		}
		if (!foundDeprecation) {
			reportError("getElementsAnnotatedWith(@Deprecation) did not find targets.model.pc.Deprecation");
		}

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
	 * Examine the modifiers of AB's contents
	 * @return true if all tests passed
	 */
	private boolean examineABModifiers() {
		Map<String, Element> contents = new HashMap<String, Element>();
		for (Element enclosed : _elementAB.getEnclosedElements()) {
			contents.put(enclosed.getSimpleName().toString(), enclosed);
		}
		Element publicMethod = contents.get("methodIAString");
		Element protectedField = contents.get("_fieldListIA");
		Element privateClass = contents.get("E");
		if (null == publicMethod || null == protectedField || null == privateClass) {
			reportError("AB does not contain the expected enclosed elements");
			return false;
		}
		Set<Modifier> modifiers = publicMethod.getModifiers();
		if (!modifiers.contains(Modifier.PUBLIC) || modifiers.size() > 1) {
			reportError("AB.methodIAString() has unexpected modifiers");
			return false;
		}
		modifiers = protectedField.getModifiers();
		if (!modifiers.contains(Modifier.PROTECTED) || modifiers.size() > 1) {
			reportError("AB._fieldListIA() has unexpected modifiers");
			return false;
		}
		modifiers = privateClass.getModifiers();
		if (!modifiers.contains(Modifier.PRIVATE) || modifiers.size() > 1) {
			reportError("AB.E() has unexpected modifiers");
			return false;
		}
		return true;
	}

	/**
	 * Examine the hierarchy of element D
	 * @return true if all tests passed
	 */
	private boolean examineDHierarchy() {
		TypeMirror supertypeD = _elementD.getSuperclass();
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
	 * Examine the methods and fields of element A
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
		List<? extends VariableElement> paramsMethodIAString = methodIAString.getParameters();
		VariableElement int1 = null;
		for (VariableElement param : paramsMethodIAString) {
			int1 = param;
		}
		TypeMirror int1Type = int1.asType();
		if (null == int1Type || int1Type.getKind() != TypeKind.INT) {
			reportError("The first parameter of A.methodIAString() is not of int type");
			return false;
		}
		if (!("int1".equals(int1.getSimpleName().toString()))) {
			reportError("The first parameter of A.methodIAString() is not named int1");
			return false;
		}
		
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
	 * Examine the methods of A which have throws clauses
	 * @return true if all tests passed
	 */
	private boolean examineAMethodThrowables() {
		List<ExecutableElement> methodsA = ElementFilter.methodsIn(_elementA.getEnclosedElements());
		ExecutableElement methodIAString = null; // no throws clauses
		ExecutableElement methodThrows1 = null;
		ExecutableElement methodThrows2 = null;
		for (ExecutableElement method : methodsA) {
			String methodName = method.getSimpleName().toString();
			if ("methodIAString".equals(methodName)) {
				methodIAString = method;
			}
			if ("methodThrows1".equals(methodName)) {
				methodThrows1 = method;
			}
			else if ("methodThrows2".equals(methodName)) {
				methodThrows2 = method;
			}
		}
		if (null == methodIAString || null == methodThrows1 || null == methodThrows2) {
			reportError("element A did not contain methodIAString(), methodThrows1(), or methodThrows2()");
			return false;
		}
		List<? extends TypeMirror> thrownTypes0 = methodIAString.getThrownTypes();
		List<? extends TypeMirror> thrownTypes1 = methodThrows1.getThrownTypes();
		List<? extends TypeMirror> thrownTypes2 = methodThrows2.getThrownTypes();
		if (null == thrownTypes0 || null == thrownTypes1 || null == thrownTypes2) {
			reportError("getThrownTypes() on A.methodIAString(), methodThrows1(), or methodThrows2() returned null");
			return false;
		}
		if (!thrownTypes0.isEmpty()) {
			reportError("A.methodIAString unexpectedly reports having a throws clause");
			return false;
		}
		boolean foundEA = false;
		for (TypeMirror type : thrownTypes1) {
			Element element = _typeUtils.asElement(type);
			if ("ExceptionA".equals(element.getSimpleName().toString())) {
				foundEA = true;
			}
		}
		if (thrownTypes1.size() != 1 || !foundEA) {
			reportError("A.methodThrows1() reported unexpected throwables");
			return false;
		}
		foundEA = false;
		boolean foundUOE = false;
		for (TypeMirror type : thrownTypes2) {
			Element element = _typeUtils.asElement(type);
			if ("UnsupportedOperationException".equals(element.getSimpleName().toString())) {
				foundUOE = true;
			}
			else if ("ExceptionA".equals(element.getSimpleName().toString())) {
				foundEA = true;
			}
		}
		if (thrownTypes2.size() != 2 || !foundEA || !foundUOE) {
			reportError("A.methodThrows2() reported unexpected throwables");
			return false;
		}
		return true;
	}
	
	/**
	 * Examine the methods of D (which are interesting because of an enum param and void return)
	 * @return true if all tests passed
	 */
	private boolean examineDMethods() {
		List<ExecutableElement> methodsD = ElementFilter.methodsIn(_elementD.getEnclosedElements());
		_methodDvoid = null;
		for (ExecutableElement method : methodsD) {
			Name methodName = method.getSimpleName();
			if ("methodDvoid".equals(methodName.toString())) {
				_methodDvoid = method;
			}
		}
		if (null == _methodDvoid) {
			reportError("element D did not contain methodDvoid()");
			return false;
		}
		TypeMirror returnType = _methodDvoid.getReturnType();
		if (returnType.getKind() != TypeKind.VOID) {
			reportError("D.methodDvoid() return type was not void");
			return false;
		}
		List<? extends VariableElement> params = _methodDvoid.getParameters();
		if (null == params || params.isEmpty()) {
			reportError("D.methodDvoid() reports no parameters");
			return false;
		}
		VariableElement param1 = params.iterator().next();
		TypeMirror param1Type = param1.asType();
		if (null == param1Type || param1Type.getKind() != TypeKind.DECLARED) {
			reportError("First parameter of D.methodDvoid() is not a declared type");
			return false;
		}
		if (!"targets.model.pb.D.DEnum".equals(param1Type.toString())) {
			reportError("Type of first parameter of D.methodDvoid() is not DEnum");
			return false;
		}
		Element param1TypeElement = ((DeclaredType)param1Type).asElement();
		if (null == param1TypeElement || param1TypeElement.getKind() != ElementKind.ENUM || !(param1TypeElement instanceof TypeElement)) {
			reportError("Type of first parameter of D.methodDvoid() is not an enum");
			return false;
		}
		_elementDEnum = (TypeElement)param1TypeElement;
		return true;
	}

	/**
	 * Check the DEnum type declared inside element D
	 * @return true if all tests passed
	 */
	private boolean examineDEnum()
	{
		if (_elementDEnum.getNestingKind() != NestingKind.MEMBER) {
			reportError("Type DEnum is not NestingKind.MEMBER");
			return false;
		}
		Map<String, VariableElement> values = new LinkedHashMap<String, VariableElement>();
		for (VariableElement enclosedElement : ElementFilter.fieldsIn(_elementDEnum.getEnclosedElements())) {
			values.put(enclosedElement.getSimpleName().toString(), enclosedElement);
		}
		if (values.size() != 3) {
			reportError("DEnum should have three values, but instead has: " + values.size());
			return false;
		}
		Iterator<String> iter = values.keySet().iterator();
		if (!"DEnum1".equals(iter.next()) || !"DEnum2".equals(iter.next()) || !"DEnum3".equals(iter.next())) {
			reportError("DEnum does not have the expected values in the expected order");
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
	
	/**
	 * Read the annotations on element D (class and method)
	 * @return true if all tests passed
	 */
	private boolean examineDAnnotations() {
		// Examine annotation on class declaration
		List<? extends AnnotationMirror> annotsD = _elementD.getAnnotationMirrors();
		if (null == annotsD || annotsD.isEmpty()) {
			reportError("element D reports no annotations");
			return false;
		}
		for (AnnotationMirror annotD : annotsD) {
			DeclaredType annotDType = annotD.getAnnotationType();
			if (null == annotDType) {
				reportError("annotation mirror of AnnoZ on element D reports null type");
				return false;
			}
			Element annotDElem = annotDType.asElement();
			if (!(annotDElem instanceof TypeElement) || 
					!"targets.model.pa.AnnoZ".equals(((TypeElement)annotDElem).getQualifiedName().toString())) {
				reportError("annotation on element D is not TypeElement targets.model.pa.AnnoZ");
				return false;
			}
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotD.getElementValues();
			if (null == values || values.isEmpty()) {
				reportError("@AnnoZ on element D reports no values");
				return false;
			}
			boolean foundStringMethod = false;
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
				String methodName = entry.getKey().getSimpleName().toString();
				if ("annoZString".equals(methodName)) {
					foundStringMethod = true;
					Object value = entry.getValue().getValue();
					if (!"annoZOnD".equals(value)) {
						reportError("Value of annoZString param on element D is not \"annoZOnD\"");
						return false;
					}
				}
			}
			if (!foundStringMethod) {
				reportError("Failed to find method annoZString on @AnnoZ on element D");
				return false;
			}
			
			// Check Elements.getElementValuesWithDefaults()
			Map<? extends ExecutableElement, ? extends AnnotationValue> defaults = 
				_elementUtils.getElementValuesWithDefaults(annotD);
			if (null == defaults) {
				reportError("Element.getElementValuesWithDefaults(annotD) returned null");
				return false;
			}
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : defaults.entrySet()) {
				String methodName = entry.getKey().getSimpleName().toString();
				if ("annoZString".equals(methodName)) {
					foundStringMethod = true;
					Object value = entry.getValue().getValue();
					if (!"annoZOnD".equals(value)) {
						reportError("Explicit value of AnnoZ.annoZString is not \"annoZOnD\"");
						return false;
					}
				}
				else if ("annoZint".equals(methodName)) {
					foundStringMethod = true;
					Object value = entry.getValue().getValue();
					if (null == value || !value.equals(17)) {
						reportError("Default value of AnnoZ.annoZint() is not 17");
						return false;
					}
				}
			}
		}
		
		List<? extends AnnotationMirror> annotsMethodDvoid = _methodDvoid.getAnnotationMirrors();
		if (null == annotsMethodDvoid || annotsMethodDvoid.isEmpty()) {
			reportError("method D.methodDvoid() reports no annotations");
			return false;
		}
		for (AnnotationMirror annotMethodDvoid : annotsMethodDvoid) {
			DeclaredType annotDType = annotMethodDvoid.getAnnotationType();
			if (null == annotDType) {
				reportError("annotation mirror of AnnoZ on D.methodDvoid() reports null type");
				return false;
			}
			Element annotDElem = annotDType.asElement();
			if (!(annotDElem instanceof TypeElement) || 
					!"targets.model.pa.AnnoZ".equals(((TypeElement)annotDElem).getQualifiedName().toString())) {
				reportError("annotation on D.methodDvoid() is not TypeElement targets.model.pa.AnnoZ");
				return false;
			}
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotMethodDvoid.getElementValues();
			if (null == values || values.isEmpty()) {
				reportError("@AnnoZ on D.methodDvoid() reports no values");
				return false;
			}
			boolean foundIntMethod = false;
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
				String methodName = entry.getKey().getSimpleName().toString();
				if ("annoZint".equals(methodName)) {
					foundIntMethod = true;
					Object value = entry.getValue().getValue();
					if (!(value instanceof Integer) || (Integer)value != 31) {
						reportError("Value of annoZint param on D.methodDvoid() is not 31");
						return false;
					}
				}
			}
			if (!foundIntMethod) {
				reportError("Failed to find method annoZint on @AnnoZ on D.methodDvoid()");
				return false;
			}
		}
		
		return true;
	}
}
