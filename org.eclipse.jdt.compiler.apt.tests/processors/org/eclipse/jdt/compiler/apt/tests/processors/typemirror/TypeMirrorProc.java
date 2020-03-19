/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.typemirror;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that explores the "model" target hierarchy with an emphasis
 * on exploring the TypeMirror APIs.  To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.typemirror.TypeMirrorProc
 * to the command line.
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TypeMirrorProc extends BaseProcessor
{
	// Initialized in collectElements()
	private TypeElement _elementAC;
	private TypeElement _elementF;
	private TypeElement _elementFChild;
	private TypeMirror _typeString;
	private TypeMirror _typeNumber;

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

		if (!examineGetEnclosingType()) {
			return false;
		}

		if (!examineGetTypeArguments()) {
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
		_elementAC = _elementUtils.getTypeElement("targets.model.pb.AC");
		if (_elementAC == null || _elementAC.getKind() != ElementKind.CLASS) {
			reportError("element AC was not found or was not a class");
			return false;
		}

		TypeElement e = _elementUtils.getTypeElement("java.lang.String");
		_typeString = e.asType();
		e = _elementUtils.getTypeElement("java.lang.Number");
		_typeNumber = e.asType();
		return true;
	}

	/**
	 * Examine the DeclaredType.getEnclosingType() implementation
	 * @return true if all tests passed
	 */
	private boolean examineGetEnclosingType() {
		TypeMirror outer = _elementF.asType();
		if (!(outer instanceof DeclaredType)) {
			reportError("F.asType() did not return a DeclaredType");
			return false;
		}
		TypeMirror inner = _elementFChild.asType();
		if (!(outer instanceof DeclaredType)) {
			reportError("F.FChild.asType() did not return a DeclaredType");
			return false;
		}
		TypeMirror innerParent = ((DeclaredType)inner).getEnclosingType();
		if (!_typeUtils.isSameType(outer, innerParent)) {
			reportError("Enclosing type of FChild (" + innerParent + ") is not F (" + outer + ")");
			return false;
		}
		return true;
	}

	/**
	 * Examine the DeclaredType.getTypeArguments() implementation
	 * @return true if all tests passed
	 */
	private boolean examineGetTypeArguments() {
		VariableElement fieldMapStringNumber = null;
		VariableElement fieldRawList = null;
		for (VariableElement field : ElementFilter.fieldsIn(_elementAC.getEnclosedElements())) {
			String name = field.getSimpleName().toString();
			if ("_fieldMapStringNumber".equals(name)) {
				fieldMapStringNumber = field;
			}
			else if ("_fieldRawList".equals(name)) {
				fieldRawList = field;
			}
		}
		if (null == fieldMapStringNumber || fieldMapStringNumber.getKind() != ElementKind.FIELD) {
			reportError("Unable to find field AC._fieldMapStringNumber");
			return false;
		}
		if (null == fieldRawList || fieldRawList.getKind() != ElementKind.FIELD) {
			reportError("Unable to find field AC._fieldRawList");
			return false;
		}
		TypeMirror typeMap = fieldMapStringNumber.asType();
		if (typeMap == null || typeMap.getKind() != TypeKind.DECLARED) {
			reportError("Field AC._fieldMapStringNumber was not found or had wrong type kind");
			return false;
		}
		TypeMirror typeRawList = fieldRawList.asType();
		if (typeRawList == null || typeRawList.getKind() != TypeKind.DECLARED) {
			reportError("Field AC._fieldRawList was not found or had wrong type kind");
			return false;
		}
		List<? extends TypeMirror> args = ((DeclaredType)typeMap).getTypeArguments();
		if (args == null || args.size() != 2) {
			reportError("AC._fieldMapStringNumber.asType().getTypeArguments() returned wrong number of args: " + args);
			return false;
		}
		Iterator<? extends TypeMirror> argsIterator = args.iterator();
		if (!_typeUtils.isSameType(_typeString, argsIterator.next()) ||
				!_typeUtils.isSameType(_typeNumber, argsIterator.next())) {
			reportError("AC._fieldMapStringNumber.asType().getTypeArguments() returned wrong args: " + args);
			return false;
		}
		args = ((DeclaredType)typeRawList).getTypeArguments();
		if (args == null || args.size() != 0) {
			reportError("AC._fieldRawList.asType().getTypeArguments() returned wrong number of args: " + args);
			return false;
		}

		return true;
	}
}
