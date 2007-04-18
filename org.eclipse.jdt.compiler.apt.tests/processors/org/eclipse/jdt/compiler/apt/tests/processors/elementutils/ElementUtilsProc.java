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

package org.eclipse.jdt.compiler.apt.tests.processors.elementutils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that exercises the methods on the Elements utility.  To enable this processor, add 
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.elementutils.ElementUtilsProc to the command line.
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ElementUtilsProc extends BaseProcessor
{
	// Initialized in collectElements()
	private TypeElement _elementF;
	private TypeElement _elementFChild;
	private TypeElement _elementFEnum;
	private TypeElement _elementG;
	private TypeElement _elementH;
	private TypeElement _elementJ;
	private TypeElement _elementAnnoX;
	private ExecutableElement _annoXValue;
	private TypeElement _elementAnnoY;
	private ExecutableElement _annoYValue;

	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
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
		
		if (!examineGetAllAnnotations()) {
			return false;
		}
		
		if (!examineGetAllMembers()) {
			return false;
		}
		
		if (!examineIsDeprecated()) {
			return false;
		}
		
		if (!examineBinaryName()) {
			return false;
		}
		
		if (!examineGetDocComment()) {
			return false;
		}
		
		if (!examineHidesField()) {
			return false;
		}
		
		if (!examineHidesClass()) {
			return false;
		}
		
		if (!examineHidesMethod()) {
			return false;
		}
		
		// TODO: temporarily disabled
		if (false && !examineOverrides()) {
			return false;
		}
		
		reportSuccess();
		return false;
	}

	/**
	 * Collect some elements that will be reused in various tests
	 * @return true if successful
	 */
	private boolean collectElements()
	{
		_elementF = _elementUtils.getTypeElement("targets.model.pc.F");
		if (_elementF == null || _elementF.getKind() != ElementKind.CLASS) {
			reportError("element F was not found or was not a class");
			return false;
		}
		_elementFChild = _elementUtils.getTypeElement("targets.model.pc.F.FChild");
		if (_elementFChild == null || _elementFChild.getKind() != ElementKind.CLASS) {
			reportError("element FChild was not found or was not a class");
			return false;
		}
		_elementFEnum = _elementUtils.getTypeElement("targets.model.pc.F.FEnum");
		if (_elementFEnum == null || _elementFEnum.getKind() != ElementKind.ENUM) {
			reportError("enum F.FEnum was not found or was not an enum");
			return false;
		}
		_elementG = _elementUtils.getTypeElement("targets.model.pc.G");
		if (_elementG == null || _elementG.getKind() != ElementKind.CLASS) {
			reportError("element G was not found or was not a class");
			return false;
		}
		_elementH = _elementUtils.getTypeElement("targets.model.pc.H");
		if (_elementH == null || _elementH.getKind() != ElementKind.CLASS) {
			reportError("element H was not found or was not a class");
			return false;
		}
		_elementJ = _elementUtils.getTypeElement("targets.model.pc.J");
		if (_elementJ == null || _elementJ.getKind() != ElementKind.CLASS) {
			reportError("element J was not found or was not a class");
			return false;
		}
		
		_elementAnnoX = _elementUtils.getTypeElement("targets.model.pc.AnnoX");
		if (null == _elementAnnoX || _elementAnnoX.getKind() != ElementKind.ANNOTATION_TYPE) {
			reportError("annotation type annoX was not found or was not an annotation");
			return false;
		}
		for (ExecutableElement method : ElementFilter.methodsIn(_elementAnnoX.getEnclosedElements())) {
			if ("value".equals(method.getSimpleName().toString())) {
				_annoXValue = method;
			}
		}
		if (null == _annoXValue) {
			reportError("Could not find value() method in annotation type AnnoX");
			return false;
		}
		
		_elementAnnoY = _elementUtils.getTypeElement("targets.model.pc.AnnoY");
		if (null == _elementAnnoY || _elementAnnoY.getKind() != ElementKind.ANNOTATION_TYPE) {
			reportError("annotation type annoY was not found or was not an annotation");
			return false;
		}
		for (ExecutableElement method : ElementFilter.methodsIn(_elementAnnoY.getEnclosedElements())) {
			if ("value".equals(method.getSimpleName().toString())) {
				_annoYValue = method;
			}
		}
		if (null == _annoYValue) {
			reportError("Could not find value() method in annotation type AnnoY");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Test the {@link Elements#getAllAnnotationMirrors()} method
	 * @return true if all tests passed
	 */
	private boolean examineGetAllAnnotations()
	{
		List<? extends AnnotationMirror> annotationsH = _elementUtils.getAllAnnotationMirrors(_elementH);
		if (null == annotationsH) {
			reportError("examineGetAllAnnotations: getAllAnnotationMirrors(_elementH) returned null");
			return false;
		}
		// H has AnnoY("on H"), G has AnnoX("on G"), and F has hidden AnnoY("on F").
		int foundF = 0;
		int foundG = 0;
		int foundH = 0;
		for (AnnotationMirror anno : annotationsH) {
			Map<? extends ExecutableElement, ? extends AnnotationValue> values = anno.getElementValues();
			AnnotationValue valueY = values.get(_annoYValue);
			if (null != valueY) {
				if ("on F".equals(valueY.getValue())) {
					foundF++;
				}
				else if ("on H".equals(valueY.getValue())) {
					foundH++;
				}
				else {
					reportError("examineGetAllAnnotations: unexpected value for annotation AnnoY");
					return false;
				}
			}
			else {
				AnnotationValue valueX = values.get(_annoXValue);
				if (null != valueX) {
					if ("on G".equals(valueX.getValue())) {
						foundG++;
					}
					else {
						reportError("examineGetAllAnnotations: unexpected value for annotation AnnoX");
						return false;
					}
				}
				else {
					reportError("examineGetAllAnnotations: getAllAnnotationMirrors(_elementH) returned a mirror with no value()");
					return false;
				}
			}
		}
		if (0 != foundF || 1 != foundG || 1 != foundH) {
			reportError("examineGetAllAnnotations: getAllAnnotationMirrors() found wrong number of annotations on H");
			return false;
		}
		return true;
	}

	/**
	 * Test the {@link Elements#getAllMembers()} method
	 * @return true if all tests passed
	 */
	private boolean examineGetAllMembers()
	{
		List<? extends Element> members = _elementUtils.getAllMembers(_elementG);
		if (null == members) {
			reportError("examineGetAllMembers: getAllMembers(_elementG) returned null");
			return false;
		}
		
		// G member list should contain Object methods, e.g., hashCode()
		boolean foundHashCode = false;
		for (ExecutableElement method : ElementFilter.methodsIn(members)) {
			if ("hashCode".equals(method.getSimpleName().toString())) {
				foundHashCode = true;
				break;
			}
		}
		if (!foundHashCode) {
			reportError("examineGetAllMembers: getAllMembers(_elementG) did not include method hashCode()");
			return false;
		}
		
		// G member list should contain F's nested FChild class
		boolean foundFChild = false;
		for (TypeElement type : ElementFilter.typesIn(members)) {
			if (type.equals(_elementFChild)) {
				foundFChild = true;
				break;
			}
		}
		if (!foundFChild) {
			reportError("examineGetAllMembers: getAllMembers(_elementG) did not include class FChild");
			return false;
		}
		
		// G member list should contain F's _fieldT1_protected
		// G member list should not contain F's _fieldT1_private, because it is hidden
		boolean foundFProtectedField = false;
		for (VariableElement field : ElementFilter.fieldsIn(members)) {
			if ("_fieldT1_protected".equals(field.getSimpleName().toString())) {
				foundFProtectedField = true;
			}
			else if ("_fieldT1_private".equals(field.getSimpleName().toString())) {
				reportError("examineGetAllMembers: getAllMembers(_elementG) included the private inherited field _fieldT1_private");
				return false;
			}
		}
		if (!foundFProtectedField) {
			reportError("examineGetAllMembers: getAllMembers(_elementG) did not return the protected inherited field _fieldT1_protected");
			return false;
		}
		
		// G member list should contain G() constructor
		// G member list should not contain F() constructor
		boolean foundGConstructor = false;
		for (ExecutableElement method : ElementFilter.constructorsIn(members)) {
			Element enclosing = method.getEnclosingElement();
			if (_elementG.equals(enclosing)) {
				foundGConstructor = true;
			}
			else {
				reportError("examineGetAllMembers: getAllMembers(_elementG) returned a constructor for an element other than G");
				return false;
			}
		}
		if (!foundGConstructor) {
			reportError("examineGetAllMembers: getAllMembers(_elementG) did not include G's constructor");
			return false;
		}

		// G member list should contain G's method_T1(String)
		// G member list should not contain F's method_T1(T1), because it is overridden by G
		boolean foundGMethodT1 = false;
		for (ExecutableElement method : ElementFilter.methodsIn(members)) {
			Element enclosing = method.getEnclosingElement();
			if ("method_T1".equals(method.getSimpleName().toString())) {
				if (_elementG.equals(enclosing)) {
					foundGMethodT1 = true;
				}
				else {
					reportError("examineGetAllMembers: getAllMembers(_elementG) included an overridden version of method_T1()");
					return false;
				}
			}
		}
		if (!foundGMethodT1) {
			reportError("examineGetAllMembers: getAllMembers(_elementG) did not include G's method_T1(String)");
			return false;
		}
		return true;
	}

	/**
	 * Test the {@link Elements#isDeprecated()} method
	 * @return true if all tests passed
	 */
	private boolean examineIsDeprecated()
	{
		Element _deprecatedElem = _elementUtils.getTypeElement("targets.model.pc.Deprecation");
		if (null == _deprecatedElem) {
			reportError("examineIsDeprecated: Couldn't find targets.model.pc.Deprecation");
			return false;
		}
		ExecutableElement methodDeprecated = null;
		ExecutableElement methodNotDeprecated = null;
		for (ExecutableElement method : ElementFilter.methodsIn(_deprecatedElem.getEnclosedElements())) {
			if ("deprecatedMethod".equals(method.getSimpleName().toString())) {
				methodDeprecated = method;
			}
			else if ("nonDeprecatedMethod".equals(method.getSimpleName().toString())) {
				methodNotDeprecated = method;
			}
		}
		if (null == methodDeprecated || null == methodNotDeprecated) {
			reportError("examineIsDeprecated: Could not find methods Deprecation.deprecatedMethod() or Deprecation.nonDeprecatedMethod()");
			return false;
		}
		if (_elementUtils.isDeprecated(methodNotDeprecated)) {
			reportError("examineIsDeprecated: ElementUtils.isDeprecated(Deprecation.nonDeprecatedMethod()) is true");
			return false;
		}
		if (!_elementUtils.isDeprecated(methodDeprecated)) {
			reportError("examineIsDeprecated: ElementUtils.isDeprecated(Deprecation.deprecatedMethod()) is false");
			return false;
		}
		TypeElement classDeprecated = null;
		TypeElement classNotDeprecated = null;
		TypeElement interfaceDeprecated = null;
		TypeElement interfaceNotDeprecated = null;
		for (TypeElement type : ElementFilter.typesIn(_deprecatedElem.getEnclosedElements())) {
			if ("deprecatedClass".equals(type.getSimpleName().toString())) {
				classDeprecated = type;
			}
			else if ("nonDeprecatedClass".equals(type.getSimpleName().toString())) {
				classNotDeprecated = type;
			}
			else if ("deprecatedInterface".equals(type.getSimpleName().toString())) {
				interfaceDeprecated = type;
			}
			else if ("nonDeprecatedInterface".equals(type.getSimpleName().toString())) {
				interfaceNotDeprecated = type;
			}
		}
		if (null == classDeprecated || null == classNotDeprecated) {
			reportError("examineIsDeprecated: Could not find methods Deprecation.deprecatedClass() or Deprecation.nonDeprecatedClass()");
			return false;
		}
		if (null == interfaceDeprecated || null == interfaceNotDeprecated) {
			reportError("examineIsDeprecated: Could not find methods Deprecation.deprecatedInterface() or Deprecation.nonDeprecatedInterface()");
			return false;
		}
		if (_elementUtils.isDeprecated(classNotDeprecated)) {
			reportError("examineIsDeprecated: ElementUtils.isDeprecated(Deprecation.nonDeprecatedClass()) is true");
			return false;
		}
		if (!_elementUtils.isDeprecated(classDeprecated)) {
			reportError("examineIsDeprecated: ElementUtils.isDeprecated(Deprecation.deprecatedClass()) is false");
			return false;
		}
		if (_elementUtils.isDeprecated(interfaceNotDeprecated)) {
			reportError("examineIsDeprecated: ElementUtils.isDeprecated(Deprecation.nonDeprecatedInterface()) is true");
			return false;
		}
		if (!_elementUtils.isDeprecated(interfaceDeprecated)) {
			reportError("examineIsDeprecated: ElementUtils.isDeprecated(Deprecation.deprecatedInterface()) is false");
			return false;
		}
		
		TypeElement deprecatedInnerClass = _elementUtils.getTypeElement("targets.model.pc.Deprecation.deprecatedClass");
		if (null == deprecatedInnerClass) {
			reportError("examineIsDeprecated: Couldn't find class Deprecation.deprecatedClass");
			return false;
		}

		return true;
	}

	/**
	 * Test the {@link Elements#getBinaryName(TypeElement)} method
	 * @return true if all tests passed
	 */
	private boolean examineBinaryName() {
		final String refNameF = "targets.model.pc.F";
		final String refBNameFChild = "targets.model.pc.F$FChild";
		final String refBNameFEnum = "targets.model.pc.F$FEnum";
		String bnameF = _elementUtils.getBinaryName(_elementF).toString();
		if (!refNameF.equals(bnameF)) {
			reportError("examineBinaryName: getBinaryName(F) should be " + refNameF + ", was: " + bnameF);
			return false;
		}
		String bnameFChild = _elementUtils.getBinaryName(_elementFChild).toString();
		if (!refBNameFChild.equals(bnameFChild)) {
			reportError("examineBinaryName: getBinaryName(F) should be " + refBNameFChild + ", was: " + bnameF);
			return false;
		}
		String bnameFEnum = _elementUtils.getBinaryName(_elementFEnum).toString();
		if (!refBNameFEnum.equals(bnameFEnum)) {
			reportError("examineBinaryName: getBinaryName(F) should be " + refBNameFEnum + ", was: " + bnameF);
			return false;
		}
		return true;
	}
	
	/**
	 * Test the {@link Elements#getDocComment(TypeElement)} method
	 * @return true if all tests passed
	 */
	private boolean examineGetDocComment() {
		// Javadoc for element F and its enclosed elements - map of element simple name to javadoc
		Map<String, String> nameToDoc = new HashMap<String, String>();
		nameToDoc.put("F", " Javadoc on element F\n @param <T1> a type parameter\n");
		nameToDoc.put("FChild", " Javadoc on nested element FChild\n");
		nameToDoc.put("FEnum", " Javadoc on nested enum FEnum\n Two lines long\n");
		nameToDoc.put("FChildI", " Javadoc on nested interface FChildI\n");
		nameToDoc.put("_fieldT1_protected", "Javadoc on field _fieldT1_protected, inline format ");
		nameToDoc.put("fieldInt", null);
		nameToDoc.put("method_T1", " Javadoc on F.method_T1\n");
		nameToDoc.put("method_String", null);

		
		String actual = _elementUtils.getDocComment(_elementF);
		String expected = nameToDoc.get("F");
		if (!expected.equals(actual)) {
			reportError("examineGetDocComment: Unexpected result from getDocComment(F): " + actual);
			return false;
		}
		for (Element e : _elementF.getEnclosedElements()) {
			String name = e.getSimpleName().toString();
			if (nameToDoc.containsKey(name)) {
				actual = _elementUtils.getDocComment(e);
				expected = nameToDoc.get(name);
				if (expected == null && actual != null) {
					reportError("examineGetDocComment: Expected getDocComment(" + name + ") to return null, but got " + actual);
					return false;
				}
				else if (expected != null) {
					if (!expected.equals(actual)) {
						reportError("examineGetDocComment: Unexpected result from getDocComment(" + name + "): " + actual);
						return false;
					}
				}
				
			}
		}
		
		return true;
	}
	
	/**
	 * Test the {@link Elements#hides(Element, Element)} method for fields
	 * @return true if all tests passed
	 */
	private boolean examineHidesField() {
		VariableElement fieldIntJ = null;
		VariableElement fieldIntH = null;
		VariableElement fieldIntG = null;
		VariableElement fieldIntF = null;
		ExecutableElement methodFieldIntJ = null;
		for (VariableElement field : ElementFilter.fieldsIn(_elementF.getEnclosedElements())) {
			if ("fieldInt".equals(field.getSimpleName().toString())) {
				fieldIntF = field;
				break;
			}
		}
		for (VariableElement field : ElementFilter.fieldsIn(_elementG.getEnclosedElements())) {
			if ("fieldInt".equals(field.getSimpleName().toString())) {
				fieldIntG = field;
				break;
			}
		}
		for (VariableElement field : ElementFilter.fieldsIn(_elementH.getEnclosedElements())) {
			if ("fieldInt".equals(field.getSimpleName().toString())) {
				fieldIntH = field;
				break;
			}
		}
		for (VariableElement field : ElementFilter.fieldsIn(_elementJ.getEnclosedElements())) {
			if ("fieldInt".equals(field.getSimpleName().toString())) {
				fieldIntJ = field;
				break;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(_elementJ.getEnclosedElements())) {
			if ("fieldInt".equals(method.getSimpleName().toString())) {
				methodFieldIntJ = method;
				break;
			}
		}
		if (null == fieldIntJ || null == fieldIntH || null == fieldIntG || null == fieldIntF) {
			reportError("examineHidesField: Failed to find field \"fieldInt\" in either F, G, H, or J");
			return false;
		}
		if (null == methodFieldIntJ) {
			reportError("examineHidesField: Failed to find method \"fieldInt()\" in J");
			return false;
		}
		// Should hide:
		if (!_elementUtils.hides(fieldIntH, fieldIntF)) {
			reportError("examineHidesField: H.fieldInt should hide F.fieldInt");
			return false;
		}
		// Should not hide:
		if (_elementUtils.hides(fieldIntF, fieldIntF)) {
			reportError("examineHidesField: F.fieldInt should not hide itself");
			return false;
		}
		if (_elementUtils.hides(fieldIntF, fieldIntG)) {
			reportError("examineHidesField: F.fieldInt should not hide G.fieldInt");
			return false;
		}
		if (!_elementUtils.hides(fieldIntG, fieldIntF)) {
			reportError("examineHidesField: G.fieldInt should hide F.fieldInt");
			return false;
		}
		if (_elementUtils.hides(fieldIntJ, fieldIntG)) {
			reportError("examineHidesField: J.fieldInt should not hide G.fieldInt");
			return false;
		}
		if (_elementUtils.hides(fieldIntJ, methodFieldIntJ)) {
			reportError("examineHidesField: field J.fieldInt should not hide method J.fieldInt()");
			return false;
		}
		return true;
	}
	
	/**
	 * Test the {@link Elements#hides(Element, Element)} method for nested classes
	 * @return true if all tests passed
	 */
	private boolean examineHidesClass() {
		TypeElement elementFChildOnF = null;
		TypeElement elementFChildOnH = null;
		TypeElement elementFOnJ = null;
		TypeElement elementFChildOnJ = null;
		TypeElement elementIFChildOnIF = null;
		TypeElement elementIFChildOnH = null;
		TypeElement elementIF = _elementUtils.getTypeElement("targets.model.pc.IF");
		for (TypeElement element : ElementFilter.typesIn(elementIF.getEnclosedElements())) {
			String name = element.getSimpleName().toString();
			if ("IFChild".equals(name)) {
				elementIFChildOnIF = element;
				break;
			}
		}
		for (TypeElement element : ElementFilter.typesIn(_elementF.getEnclosedElements())) {
			String name = element.getSimpleName().toString();
			if ("FChild".equals(name)) {
				elementFChildOnF = element;
				break;
			}
		}
		for (TypeElement element : ElementFilter.typesIn(_elementH.getEnclosedElements())) {
			String name = element.getSimpleName().toString();
			if ("FChild".equals(name)) {
				elementFChildOnH = element;
			}
			else if ("IFChild".equals(name)) {
				elementIFChildOnH = element;
			}
		}
		for (TypeElement element : ElementFilter.typesIn(_elementJ.getEnclosedElements())) {
			String name = element.getSimpleName().toString();
			if ("FChild".equals(name)) {
				elementFChildOnJ = element;
			}
			else if ("F".equals(name)) {
				elementFOnJ = element;
			}
		}
		Element elementFPackage = _elementF.getEnclosingElement();
		
		// Should hide:
		if (!_elementUtils.hides(elementFChildOnH, elementFChildOnF)) {
			reportError("examineHidesClass: H.FChild should hide F.FChild");
			return false;
		}
		if (!_elementUtils.hides(elementIFChildOnH, elementIFChildOnIF)) {
			reportError("examineHidesClass: H.IFChild should hide IF.IFChild");
			return false;
		}
		// Should not hide:
		if (_elementUtils.hides(elementFChildOnF, elementFChildOnF)) {
			reportError("examineHidesClass: F.FChild should not hide itself");
			return false;
		}
		if (_elementUtils.hides(elementIFChildOnH, elementFChildOnF)) {
			reportError("examineHidesClass: H.IFChild should not hide F.FChild");
			return false;
		}
		if (_elementUtils.hides(elementFChildOnF, elementFChildOnH)) {
			reportError("examineHidesClass: F.FChild should not hide H.FChild");
			return false;
		}
		if (_elementUtils.hides(elementFChildOnJ, elementFChildOnF)) {
			reportError("examineHidesClass: J.FChild should not hide F.FChild");
			return false;
		}
		if (_elementUtils.hides(_elementF, elementFOnJ)) {
			reportError("examineHidesClass: J.F should not hide F");
			return false;
		}
		if (_elementUtils.hides(_elementF, elementFPackage) || _elementUtils.hides(elementFPackage, _elementF)) {
			reportError("examineHidesClass: F should not hide its enclosing package, and vice versa");
			return false;
		}
		return true;
	}

	/**
	 * Test the {@link Elements#hides(Element, Element)} method for methods
	 * @return true if all tests passed
	 */
	private boolean examineHidesMethod() {
		ExecutableElement methodStaticOnF = null;
		ExecutableElement methodStatic2OnF = null;
		ExecutableElement methodT1OnF = null;
		ExecutableElement methodStaticOnG = null;
		ExecutableElement methodT1OnG = null;
		ExecutableElement methodStaticOnH = null;
		ExecutableElement methodStaticIntOnH = null;
		ExecutableElement methodStaticOnJ = null;
		for (ExecutableElement method : ElementFilter.methodsIn(_elementF.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("staticMethod".equals(name)) {
				methodStaticOnF = method;
			}
			else if ("staticMethod2".equals(name)) {
				methodStatic2OnF = method;
			}
			else if ("method_T1".equals(name)) {
				methodT1OnF = method;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(_elementG.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("staticMethod".equals(name)) {
				methodStaticOnG = method;
			}
			else if ("method_T1".equals(name)) {
				methodT1OnG = method;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(_elementH.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("staticMethod".equals(name)) {
				if (method.getParameters().isEmpty()) {
					methodStaticOnH = method;
				}
				else {
					methodStaticIntOnH = method;
				}
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(_elementJ.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("staticMethod".equals(name)) {
				methodStaticOnJ = method;
				break;
			}
		}
		if (methodStaticOnF == null || methodStatic2OnF == null || methodT1OnF == null) {
			reportError("examineHidesMethod: Failed to find an expected method on F");
			return false;
		}
		if (methodStaticOnG == null || methodT1OnG == null) {
			reportError("examineHidesMethod: Failed to find an expected method on G");
			return false;
		}
		if (methodStaticOnH == null || methodStaticIntOnH == null) {
			reportError("examineHidesMethod: Failed to find an expected method on H");
			return false;
		}
		if (methodStaticOnJ == null) {
			reportError("examineHidesMethod: Failed to find an expected method on J");
			return false;
		}
		
		// The should-hide cases
		if (!_elementUtils.hides(methodStaticOnH, methodStaticOnG)) {
			reportError("examineHidesMethod: H.staticMethod() should hide G.staticMethod()");
			return false;
		}
		
		// The should-not-hide cases
		if (_elementUtils.hides(methodStaticOnG, methodStaticOnG)) {
			reportError("examineHidesMethod: G.staticMethod() should not hide itself");
			return false;
		}
		if (_elementUtils.hides(methodStaticOnG, methodStaticOnF)) {
			reportError("examineHidesMethod: G.staticMethod() should not hide (private) F.staticMethod()");
			return false;
		}
		if (_elementUtils.hides(methodStaticOnG, methodStaticOnH)) {
			reportError("examineHidesMethod: G.staticMethod() should not hide H.staticMethod()");
			return false;
		}
		if (_elementUtils.hides(methodStaticOnG, methodStatic2OnF)) {
			reportError("examineHidesMethod: G.staticMethod() should not hide F.staticMethod2()");
			return false;
		}
		if (_elementUtils.hides(methodStaticOnJ, methodStaticOnG)) {
			reportError("examineHidesMethod: J.staticMethod() should not hide G.staticMethod()");
			return false;
		}
		if (_elementUtils.hides(methodStaticIntOnH, methodStaticOnG)) {
			reportError("examineHidesMethod: H.staticMethod(int) should not hide G.staticMethod()");
			return false;
		}
		if (_elementUtils.hides(methodT1OnG, methodT1OnF)) {
			reportError("examineHidesMethod: G.methodT1() should not hide F.methodT1(), because they aren't static (JLS 8.4.8.2)");
			return false;
		}
		return true;
	}
	
	/**
	 * Test the {@link Elements#overrides(ExecutableElement, ExecutableElement, TypeElement)} implementation
	 * @return true if all tests passed
	 */
	private boolean examineOverrides() {
		TypeElement typeA = _elementUtils.getTypeElement("targets.model.pc.Overriding.A");
		TypeElement typeB = _elementUtils.getTypeElement("targets.model.pc.Overriding.B");
		TypeElement typeC = _elementUtils.getTypeElement("targets.model.pc.Overriding.C");
		TypeElement typeD = _elementUtils.getTypeElement("targets.model.pc.Overriding.D");
		if (typeA == null || typeB == null || typeC == null || typeD == null) {
			reportError("Unable to find types in targets.model.pc.Overriding");
			return false;
		}
		ExecutableElement methodAF = null;
		ExecutableElement methodAG = null;
		ExecutableElement methodAH = null;
		ExecutableElement methodAJ = null;
		ExecutableElement methodBF = null;
		ExecutableElement methodBG = null;
		ExecutableElement methodBH = null;
		ExecutableElement methodCH = null;
		ExecutableElement methodDF = null;
		ExecutableElement methodDG = null;
		ExecutableElement methodDJ = null;
		for (ExecutableElement method : ElementFilter.methodsIn(typeA.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("f".equals(name)) {
				methodAF = method;
			}
			else if ("g".equals(name)) {
				methodAG = method;
			}
			else if ("h".equals(name)) {
				methodAH = method;
			}
			else if ("j".equals(name)) {
				methodAJ = method;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(typeB.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("f".equals(name)) {
				methodBF = method;
			}
			else if ("g".equals(name)) {
				methodBG = method;
			}
			else if ("h".equals(name)) {
				methodBH = method;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(typeC.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("h".equals(name)) {
				methodCH = method;
				break;
			}
		}
		for (ExecutableElement method : ElementFilter.methodsIn(typeD.getEnclosedElements())) {
			String name = method.getSimpleName().toString();
			if ("f".equals(name)) {
				methodDF = method;
			}
			else if ("g".equals(name)) {
				methodDG = method;
			}
			else if ("j".equals(name)) {
				methodDJ = method;
			}
		}
		if (null == methodAF || null == methodAG || null == methodAH || null == methodAJ ||
				null == methodBF || null == methodBG || null == methodBH ||
				null == methodCH ||
				null == methodDF || null == methodDG || null == methodDJ) {
			reportError("examineOverrides: could not find some methods in targets.model.pc.Overrides");
			return false;
		}
		
		// Should override:
		if (!_elementUtils.overrides(methodAF, methodBF, typeC)) {
			reportError("examineOverrides: A.f() should override B.f() in the context of C");
			return false;
		}
		if (!_elementUtils.overrides(methodCH, methodAH, typeC)) {
			reportError("examineOverrides: C.h() should override A.h() in the context of C");
			return false;
		}
		if (!_elementUtils.overrides(methodCH, methodAH, typeD)) {
			reportError("examineOverrides: C.h() should override A.h() in the context of D");
			return false;
		}
		if (!_elementUtils.overrides(methodDF, methodBF, typeD)) {
			reportError("examineOverrides: D.f() should override B.f() in the context of D");
			return false;
		}
		if (!_elementUtils.overrides(methodDG, methodBG, typeD)) {
			reportError("examineOverrides: D.g() should override B.g() in the context of D");
			return false;
		}
		if (!_elementUtils.overrides(methodDJ, methodAJ, typeD)) {
			reportError("examineOverrides: D.j() should override A.j() in the context of D");
			return false;
		}
		if (!_elementUtils.overrides(methodAH, methodBH, typeC)) {
			reportError("examineOverrides: A.h() should override B.h() in the context of C (even though C.h does too)");
			return false;
		}
		
		// Should not override:
		if (_elementUtils.overrides(methodAF, methodAF, typeA)) {
			reportError("examineOverrides: A.f() should not override itself in the context of A");
			return false;
		}
		if (_elementUtils.overrides(methodAF, methodAF, typeC)) {
			reportError("examineOverrides: A.f() should not override itself in the context of C");
			return false;
		}
		if (_elementUtils.overrides(methodAF, methodBF, typeA)) {
			reportError("examineOverrides: A.f() should not override B.f() in the context of A");
			return false;
		}
		if (_elementUtils.overrides(methodAG, methodBG, typeC)) {
			reportError("examineOverrides: private A.g() should not override B.g() in the context of C");
			return false;
		}
		if (_elementUtils.overrides(methodDG, methodAG, typeD)) {
			reportError("examineOverrides: D.g() should not override private A.g() in the context of D");
			return false;
		}
		if (_elementUtils.overrides(methodDJ, methodAJ, typeC)) {
// javac implementation fails this test
//			reportError("examineOverrides: D.j() should not override A.j() in the context of C");
//			return false;
		}
		if (_elementUtils.overrides(methodDF, methodAF, typeC)) {
// javac implementation fails this test
//			reportError("examineOverrides: D.f() should not override A.f() in the context of C");
//			return false;
		}
		if (_elementUtils.overrides(methodDF, methodBF, typeC)) {
// javac implementation fails this test
//			reportError("examineOverrides: D.f() should not override B.f() in the context of C");
//			return false;
		}
		
		return true;
	}

}
