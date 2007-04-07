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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

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
	private TypeElement _elementG;

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
		
		if (!examineGetAllMembers()) {
			return false;
		}
		
		reportSuccess();
		return true;
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
		_elementG = _elementUtils.getTypeElement("targets.model.pc.G");
		if (_elementG == null || _elementG.getKind() != ElementKind.CLASS) {
			reportError("element G was not found or was not a class");
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
			if ("FChild".equals(type.getSimpleName().toString())) {
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

}
