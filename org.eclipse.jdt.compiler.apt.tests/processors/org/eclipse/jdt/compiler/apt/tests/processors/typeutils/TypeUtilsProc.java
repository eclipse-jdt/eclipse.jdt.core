/*******************************************************************************
 * Copyright (c) 2007 - 2015 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Walter Harley - initial API and implementation
 *    IBM Corporation - Bug 382590
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.typeutils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;
import org.eclipse.jdt.internal.compiler.apt.model.DeclaredTypeImpl;

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

		try {
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

    		if (!examineTypesAsMemberOf()) {
    			return false;
    		}

    		if (!examineTypesAsMemberOfSubclass()) {
    		    return false;
    		}
		} catch (RuntimeException e) {
		    StringWriter sw = new StringWriter();
		    PrintWriter w = new PrintWriter(sw);
		    e.printStackTrace(w);
		    reportError(sw.toString());
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
		TypeMirror erasureOfInt = _typeUtils.erasure(intType);
		if (erasureOfInt != intType) {
			reportError("erasure of a primitive type is the type itself");
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
		String noTypeString = noType.toString();
		if (!("none".equals(noTypeString) || "<none>".equals(noTypeString))) {
			reportError("NoType has the wrong name: " + noTypeString);
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
		TypeMirror typeD = elementD.asType();
		if (!_typeUtils.isSubtype(typeD, typeAB)) {
			reportError("Type of D is not a subtype of type AB");
			return false;
		}
		ArrayType arrayOfTypeD = _typeUtils.getArrayType(typeD);
		ArrayType arrayOfTypeAB = _typeUtils.getArrayType(typeAB);
		if (!_typeUtils.isSubtype(arrayOfTypeD, arrayOfTypeAB)) {
			reportError("Array of type D is not a subtype of array of type AB");
			return false;
		}
		PrimitiveType typeInt = _typeUtils.getPrimitiveType(TypeKind.INT);
		ArrayType arrayOfInt = _typeUtils.getArrayType(typeInt);
		ArrayType arrayOfIntInt = _typeUtils.getArrayType(arrayOfInt);
		TypeElement objectTypeElement = _elementUtils.getTypeElement("java.lang.Object");
		TypeMirror javaLangObject = objectTypeElement.asType();
		if (!_typeUtils.isSubtype(arrayOfIntInt, javaLangObject)) {
			reportError("int[][] is not a subtype of Object");
			return false;
		}
		TypeElement stringTypeElement = _elementUtils.getTypeElement("java.lang.String");
		TypeMirror javaLangString = stringTypeElement.asType();
		ArrayType arrayOfString = _typeUtils.getArrayType(javaLangString);
		ArrayType arrayOfObject = _typeUtils.getArrayType(javaLangObject);
		ArrayType arrayOfObjectObject = _typeUtils.getArrayType(arrayOfObject);
		if (_typeUtils.isSubtype(arrayOfString, arrayOfObjectObject)) {
			reportError("String[] is a subtype of Object[][]");
			return false;
		}
		if (!_typeUtils.isSubtype(arrayOfString, arrayOfObject)) {
			reportError("String[] is not a subtype of Object[]");
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
		TypeElement mapDecl = _elementUtils.getTypeElement(Map.class.getName());
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
		TypeElement elementOuter = _elementUtils.getTypeElement("targets.model.pd.Outer");
		TypeElement elementInner = _elementUtils.getTypeElement("targets.model.pd.Outer.Inner");
		DeclaredType stringType = _typeUtils.getDeclaredType(stringDecl);
		DeclaredType numberType = _typeUtils.getDeclaredType(numberDecl);
		ArrayType numberArrayType = _typeUtils.getArrayType(numberType);

		// Outer<T1, T2> ---> Outer<String, Number[]>
		DeclaredType outerType = _typeUtils.getDeclaredType(elementOuter, stringType, numberArrayType);

		// Outer<T1, T2>.Inner<T2> ---> Outer<String, Number[]>.Inner<Number[]>
		DeclaredType decl = _typeUtils.getDeclaredType(outerType, elementInner, new TypeMirror[] { numberArrayType });

		List<? extends TypeMirror> args = decl.getTypeArguments();
		if (args.size() != 1) {
			reportError("Outer<String, Number[]>.Inner<Number[]> should have one argument but decl.getTypeArguments() returned " + args.size());
			return false;
		}
		if (!_typeUtils.isSameType(numberArrayType, args.get(0))) {
			reportError("First arg of Outer<String, Number[]>.Inner<Number[]> was expected to be Number[], but was: " + args.get(0));
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

	/**
	 * Test {@link Types#asMemberOf()}.
	 * @return true if tests passed
	 */
	private boolean examineTypesAsMemberOf() {
	    TypeElement containerElement = _elementUtils.getTypeElement("targets.model.pc.AsMemberOf");
	    DeclaredType longType = (DeclaredType)_elementUtils.getTypeElement("java.lang.Long").asType();
		DeclaredType container = _typeUtils.getDeclaredType(containerElement, longType);
		return innerTestAsMemberOf("examineTypesAsMemberOf", container, containerElement);
	}
	/**
	 * Test {@link Types#asMemberOf()} where the elements are members of a superclass of the
	 * declared container. See <a href="http://bugs.eclipse.org/382590">Bug 382590</a>. Note that
	 * the asMemberOf() javadoc says the element is viewed "as a member of, or otherwise directly
	 * contained by, a given type." It is not clear what "directly" means here, but javac 1.6
	 * supports this case, so we will too.
	 * @return true if tests passed
	 */
	private boolean examineTypesAsMemberOfSubclass() {
	    DeclaredType declaredContainer = (DeclaredType) _elementUtils.getTypeElement("targets.model.pc.AsMemberOfSub").asType();
	    TypeElement containerElement = _elementUtils.getTypeElement("targets.model.pc.AsMemberOf");
	    return innerTestAsMemberOf("examineTypesAsMemberOfSubclass", declaredContainer, containerElement);
	}

	/**
	 * @return true if tests passed
	 */
    private boolean innerTestAsMemberOf(String method, DeclaredType declaredContainer,
            TypeElement containerElement) {
        DeclaredType longType = (DeclaredType)_elementUtils.getTypeElement("java.lang.Long").asType();
        Map<String, Element> members = new HashMap<>();
	    for (Element element : containerElement.getEnclosedElements()) {
	        members.put(element.getSimpleName().toString(), element);
	    }
	    for (String m : new String[] {"f2", "m2"}) {
	        if (members.get(m) != null) {
	            reportError(method + ": Should not have found member named '" + m + "' in container " + declaredContainer);
	            return false;
	        }
	    }
	    for (String m : new String[] {"f", "m", "C", "D", "e"}) {
	        Element memberElement = members.get(m);
	        if (memberElement == null) {
	            reportError(method + ": Couldn't find member named '" + m + "' in container " + declaredContainer);
	            return false;
	        }
	        TypeMirror tm = _typeUtils.asMemberOf(declaredContainer, memberElement);

            if ("f".equals(m)) {
                // Long field
                if (!_typeUtils.isSameType(tm, longType)) {
                    reportError(method + ": member f should be of type Long, but was " + tm);
                    return false;
                }
            } else if ("m".equals(m)) {
                // void method returning Long
                if (tm.getKind() != TypeKind.EXECUTABLE) {
                    reportError(method + ": member m() should be executable but was " + tm.getKind());
                    return false;
                }
                ExecutableType etm = (ExecutableType)tm;
                if (!_typeUtils.isSameType(etm.getReturnType(), longType)) {
                    reportError(method + ": member m() should have Long return type, but found " + etm.getReturnType());
                    return false;
                }
            } else if ("C".equals(m)) {
	            // Not clear what to expect in the case of a static nested class. Outer parameterization doesn't
	            // apply to the nested class; and the method spec doesn't say what to do when the contained
	            // member is incompletely parameterized.
	            TypeElement c = _elementUtils.getTypeElement("targets.model.pc.AsMemberOf.C");
	            TypeMirror tmExpected = c.asType();

	            // In javac, we get A.C<T> whether C is directly contained or accessed via a subclass.
	            // In Eclipse, when accessing through a subclass, the binding to C is lazily created
	            // within ParameterizedTypeBinding.memberTypes(), so it gets created as a PTB itself,
	            // with no type arguments. Thus it is A<Long>.C instead of A.C<T>. This is probably a
	            // compiler bug, but given that asMemberOf() is documented to apply only to "directly
	            // contained" members it's probably not worth worrying about. So, we accept that case.
	            if (!_typeUtils.isSameType(tm, tmExpected) &&
	                    !(tm instanceof DeclaredTypeImpl &&
	                            "targets.model.pc.AsMemberOf<java.lang.Long>.C".equals(tm.toString()))) {
	                reportError(method + ": member C: Expected type " + tmExpected + " but found " + tm);
	                return false;
	            }
	        } else if ("D".equals(m)) {
	            // Expected type of D is AsMemberOf2<Long>.D
	            DeclaredType tmContainer = _typeUtils.getDeclaredType(containerElement, longType);
	            TypeElement d = _elementUtils.getTypeElement("targets.model.pc.AsMemberOf.D");
	            TypeMirror tmExpected = _typeUtils.getDeclaredType(tmContainer, d);
	            if (!_typeUtils.isSameType(tm, tmExpected)) {
	                reportError(method + ": member D: Expected type " + tmExpected +
	                        " but found " + tm);
	                return false;
	            }
    		} else if ("e".equals(m)) {
    		    // Expected type of e is A<Long>.E<Integer>
                DeclaredType tmContainer = _typeUtils.getDeclaredType(containerElement, longType);
                TypeElement eTypeElem = _elementUtils.getTypeElement("targets.model.pc.AsMemberOf.E");
                DeclaredType tmInt = (DeclaredType) _elementUtils.getTypeElement("java.lang.Integer").asType();
                TypeMirror tmExpected = _typeUtils.getDeclaredType(tmContainer, eTypeElem, tmInt);
                if (!_typeUtils.isSameType(tm, tmExpected)) {
                    reportError(method + ": member e: Expected type " + tmExpected +
                            " but found " + tm);
                    return false;
                }
    		}
	    }
	    return true;
    }

}
