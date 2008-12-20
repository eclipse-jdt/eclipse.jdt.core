/*******************************************************************************
 * Copyright (c) 2007, 2008 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.typeutils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
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
		
		if (!examineGetDeclaredType()) {
			return false;
		}
		
		if (!examineGetDeclaredTypeParameterized()) {
			return false;
		}

		if (!examineGetDeclaredTypeNested()) {
			return false;
		}
		
		if (!examineGetArrayTypeParameterized()) {
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
	
	/**
	 * Test the implementation of {@link javax.lang.model.util.Types#getDeclaredType()}
	 * @return true if tests passed
	 */
	private boolean examineGetDeclaredType() {
		TypeElement elementD = _elementUtils.getTypeElement("targets.model.pb.D");
		TypeElement elementAB = _elementUtils.getTypeElement("targets.model.pb.AB");
		TypeMirror typeAB = _typeUtils.getDeclaredType(elementAB);
		if (!(typeAB instanceof DeclaredType) || typeAB.getKind() != TypeKind.DECLARED) {
			reportError("Types.getDeclaredType(elementAB) returned bad value: " + typeAB);
			return false;
		}
		TypeMirror typeDSuper = elementD.getSuperclass();
		if (typeDSuper == null || !_typeUtils.isSameType(typeAB, typeDSuper)) {
			reportError("Type of AB and superclass of D are not same type");
			return false;
		}
		return true;
	}
	
	/**
	 * Test getDeclaredType() for parameterized types
	 * @return true if tests passed
	 */
	private boolean examineGetDeclaredTypeParameterized() {
		TypeElement stringDecl = _elementUtils.getTypeElement(String.class.getName());
		TypeElement mapDecl = _elementUtils.getTypeElement("java.util.Map");
		DeclaredType stringType = _typeUtils.getDeclaredType(stringDecl);
		ArrayType stringArrayType = _typeUtils.getArrayType(stringType);

		DeclaredType decl = _typeUtils.getDeclaredType(mapDecl, stringType, stringArrayType);
		List<? extends TypeMirror> args = decl.getTypeArguments();
		if (args.size() != 2) {
			reportError("Map<String, String[]> should have two arguments but decl.getTypeArguments() returned " + args.size());
			return false;
		}
		if (!_typeUtils.isSameType(stringType, args.get(0))) {
			reportError("First arg of Map<String, String[]> was expected to be String, but was: " + args.get(0));
			return false;
		}
		if (!_typeUtils.isSameType(stringArrayType, args.get(1))) {
			reportError("Second arg of Map<String, String[]> was expected to be String[], but was: " + args.get(1));
			return false;
		}
		return true;
	}

	/**
	 * Test getDeclaredType() for nested parameterized types (Outer&lt;Foo&gt;.Inner&lt;Bar&gt;).
	 * @return true if tests passed
	 */
	private boolean examineGetDeclaredTypeNested() {
		TypeElement stringDecl = _elementUtils.getTypeElement(String.class.getName());
		TypeElement numberDecl = _elementUtils.getTypeElement(Number.class.getName());
		TypeElement mapDecl = _elementUtils.getTypeElement("java.util.HashMap");
		TypeElement iterDecl = _elementUtils.getTypeElement("java.util.HashMap.HashIterator");
		DeclaredType stringType = _typeUtils.getDeclaredType(stringDecl);
		DeclaredType numberType = _typeUtils.getDeclaredType(numberDecl);
		ArrayType numberArrayType = _typeUtils.getArrayType(numberType);

		// HashMap<String, Number[]>
		DeclaredType outerType = _typeUtils.getDeclaredType(mapDecl, stringType, numberArrayType);
		
		// HashMap<String, Number[]>.HashIterator<Number[]>
		DeclaredType decl = _typeUtils.getDeclaredType(outerType, iterDecl, new TypeMirror[] { numberArrayType });
		
		List<? extends TypeMirror> args = decl.getTypeArguments();
		if (args.size() != 1) {
			reportError("Map<String, Number[]>.EntryIterator<Number[]> should have one argument but decl.getTypeArguments() returned " + args.size());
			return false;
		}
		if (!_typeUtils.isSameType(numberArrayType, args.get(0))) {
			reportError("First arg of Map<String, Number[]>.EntryIterator<Number[]> was expected to be Number[], but was: " + args.get(0));
			return false;
		}
		return true;
	}

	/**
	 * Test getArrayType() for a parameterized type
	 * @return true if tests passed
	 */
	private boolean examineGetArrayTypeParameterized() {
		TypeElement stringDecl = _elementUtils.getTypeElement(String.class.getName());
		TypeElement listDecl = _elementUtils.getTypeElement(List.class.getName());
		DeclaredType stringType = _typeUtils.getDeclaredType(stringDecl);
		
		// List<String>
		DeclaredType decl = _typeUtils.getDeclaredType(listDecl, stringType);
		
		// List<String>[]
		ArrayType listArray = _typeUtils.getArrayType(decl);
		
		TypeMirror leafType = listArray.getComponentType();
		if (!_typeUtils.isSameType(leafType, decl)) {
			reportError("Leaf type of List<String>[] should be List<String>, but was: " + leafType);
			return false;
		}
		
		return true;
	}

}
