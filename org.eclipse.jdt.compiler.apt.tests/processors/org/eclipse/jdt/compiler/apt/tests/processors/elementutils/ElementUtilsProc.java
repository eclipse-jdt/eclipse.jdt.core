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
			reportError("getAllAnnotationMirrors(_elementH) returned null");
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
					reportError("unexpected value for annotation AnnoY");
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
						reportError("unexpected value for annotation AnnoX");
						return false;
					}
				}
				else {
					reportError("getAllAnnotationMirrors(_elementH) returned a mirror with no value()");
					return false;
				}
			}
		}
		if (0 != foundF || 1 != foundG || 1 != foundH) {
			reportError("getAllAnnotationMirrors() found wrong number of annotations on H");
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
			reportError("getAllMembers(_elementG) returned null");
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
			reportError("getAllMembers(_elementG) did not include method hashCode()");
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
			reportError("getAllMembers(_elementG) did not include class FChild");
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
				reportError("getAllMembers(_elementG) included the private inherited field _fieldT1_private");
				return false;
			}
		}
		if (!foundFProtectedField) {
			reportError("getAllMembers(_elementG) did not return the protected inherited field _fieldT1_protected");
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
				reportError("getAllMembers(_elementG) returned a constructor for an element other than G");
				return false;
			}
		}
		if (!foundGConstructor) {
			reportError("getAllMembers(_elementG) did not include G's constructor");
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
					reportError("getAllMembers(_elementG) included an overridden version of method_T1()");
					return false;
				}
			}
		}
		if (!foundGMethodT1) {
			reportError("getAllMembers(_elementG) did not include G's method_T1(String)");
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
			reportError("Couldn't find targets.model.pc.Deprecation");
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
			reportError("Could not find methods Deprecation.deprecatedMethod() or Deprecation.nonDeprecatedMethod()");
			return false;
		}
		if (_elementUtils.isDeprecated(methodNotDeprecated)) {
			reportError("ElementUtils.isDeprecated(Deprecation.nonDeprecatedMethod()) is true");
			return false;
		}
		if (!_elementUtils.isDeprecated(methodDeprecated)) {
			reportError("ElementUtils.isDeprecated(Deprecation.deprecatedMethod()) is false");
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
			reportError("Could not find methods Deprecation.deprecatedClass() or Deprecation.nonDeprecatedClass()");
			return false;
		}
		if (null == interfaceDeprecated || null == interfaceNotDeprecated) {
			reportError("Could not find methods Deprecation.deprecatedInterface() or Deprecation.nonDeprecatedInterface()");
			return false;
		}
		if (_elementUtils.isDeprecated(classNotDeprecated)) {
			reportError("ElementUtils.isDeprecated(Deprecation.nonDeprecatedClass()) is true");
			return false;
		}
		if (!_elementUtils.isDeprecated(classDeprecated)) {
			reportError("ElementUtils.isDeprecated(Deprecation.deprecatedClass()) is false");
			return false;
		}
		if (_elementUtils.isDeprecated(interfaceNotDeprecated)) {
			reportError("ElementUtils.isDeprecated(Deprecation.nonDeprecatedInterface()) is true");
			return false;
		}
		if (!_elementUtils.isDeprecated(interfaceDeprecated)) {
			reportError("ElementUtils.isDeprecated(Deprecation.deprecatedInterface()) is false");
			return false;
		}
		
		TypeElement deprecatedInnerClass = _elementUtils.getTypeElement("targets.model.pc.Deprecation.deprecatedClass");
		if (null == deprecatedInnerClass) {
			reportError("Couldn't find class Deprecation.deprecatedClass");
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
			reportError("getBinaryName(F) should be " + refNameF + ", was: " + bnameF);
			return false;
		}
		String bnameFChild = _elementUtils.getBinaryName(_elementFChild).toString();
		if (!refBNameFChild.equals(bnameFChild)) {
			reportError("getBinaryName(F) should be " + refBNameFChild + ", was: " + bnameF);
			return false;
		}
		String bnameFEnum = _elementUtils.getBinaryName(_elementFEnum).toString();
		if (!refBNameFEnum.equals(bnameFEnum)) {
			reportError("getBinaryName(F) should be " + refBNameFEnum + ", was: " + bnameF);
			return false;
		}
		return true;
	}
}
