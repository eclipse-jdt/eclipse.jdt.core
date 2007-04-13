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

package org.eclipse.jdt.compiler.apt.tests.processors.typeutils;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that exercises the methods on the Elements utility.  To enable this processor, add 
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.typeutils.TypeUtilsProc to the command line.
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TypeUtilsProc extends BaseProcessor
{

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
		
		if (!examinePrimitives()) {
			return false;
		}
		
		if (!examineNoType()) {
			return false;
		}
		
		reportSuccess();
		return false;
	}
	
	/**
	 * Test the implementation of primitive types and the getPrimitiveType() method
	 * @return true if tests passed
	 */
	private boolean examinePrimitives() {
		TypeElement integerElement = _elementUtils.getTypeElement("java.lang.Integer");
		if (null == integerElement) {
			reportError("Failed to get element java.lang.Integer");
			return false;
		}
		TypeMirror integerType = integerElement.asType();
		if (null == integerType) {
			reportError("Failed to get element java.lang.Integer as a type");
			return false;
		}
		TypeMirror intType = _typeUtils.getPrimitiveType(TypeKind.INT);
		if (null == intType || intType.getKind() != TypeKind.INT) {
			reportError("Failed to get primitive type INT");
			return false;
		}
		TypeMirror floatType = _typeUtils.getPrimitiveType(TypeKind.FLOAT);
		if (null == floatType || floatType.getKind() != TypeKind.FLOAT) {
			reportError("Failed to get primitive type FLOAT");
			return false;
		}
		if (!intType.equals(_typeUtils.unboxedType(integerType))) {
			reportError("unboxedType(java.lang.Integer) is not primitive int");
			return false;
		}
		if (!_typeUtils.isAssignable(intType, floatType)) {
			reportError("isAssignable(int, float) should be true");
			return false;
		}
		if (_typeUtils.isAssignable(floatType, intType)) {
			reportError("isAssignable(float, int) should be false");
			return false;
		}
		
		// TYPE IDENTITY
		if (_typeUtils.isSameType(intType, floatType)) {
			reportError("Primitive type int is reported to be same as float");
			return false;
		}
		if (!_typeUtils.isSameType(floatType, floatType)) {
			reportError("Primitive type float is reported to not be same as itself");
			return false;
		}
		
		// SUBTYPES
		if (!_typeUtils.isSubtype(intType, intType)) {
			reportError("Primitive type int is not a subtype of itself");
			return false;
		}
		if (!_typeUtils.isSubtype(intType, floatType)) {
			reportError("Primitive type int is not a subtype of float");
			return false;
		}
		if (_typeUtils.isSubtype(floatType, intType)) {
			reportError("Primitive type float is a subtype of int");
			return false;
		}
		
		// BOXING
		if (!_typeUtils.isAssignable(intType, integerType)) {
			reportError("isAssignable(int, java.lang.Integer) should be true");
			return false;
		}
		if (!_typeUtils.isAssignable(integerType, intType)) {
			reportError("isAssignable(java.lang.Integer, int) should be true");
			return false;
		}
		if (!_typeUtils.isAssignable(integerType, floatType)) {
			reportError("isAssignable(java.lang.Integer, float) should be true");
			return false;
		}
		if (_typeUtils.isAssignable(floatType, integerType)) {
			reportError("isAssignable(float, java.lang.Integer) should be false");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Test the implementation of NoType and the getNoType() method
	 * @return true if tests passed
	 */
	private boolean examineNoType() {
		NoType noType = _typeUtils.getNoType(TypeKind.NONE);
		if (null == noType || noType.getKind() != TypeKind.NONE) {
			reportError("getNoType() didn't return a TypeKind.NONE type");
			return false;
		}
		if (!"<none>".equals(noType.toString())) {
			reportError("NoType has the wrong name: " + noType.toString());
			return false;
		}
		return true;
	}
		
}
