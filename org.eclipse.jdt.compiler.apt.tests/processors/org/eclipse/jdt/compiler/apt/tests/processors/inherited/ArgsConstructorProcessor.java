/*******************************************************************************
 * Copyright (c) 2011, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.inherited;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import org.eclipse.jdt.compiler.apt.tests.annotations.ArgsConstructor;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

@SupportedAnnotationTypes("org.eclipse.jdt.compiler.apt.tests.annotations.ArgsConstructor")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ArgsConstructorProcessor extends BaseProcessor {
	private TypeElement _elementAC;

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment env) {

		for (TypeElement type : annotations) {
			processArgsConstructorClasses(env, type);
		}

		if (!collectElements()) {
			reportError("Failed");
			return false;
		}
		TypeMirror superclass = _elementAC.getSuperclass();
		if (TypeKind.DECLARED != superclass.getKind()) {
			reportError("Wrong type: should be a declared type");
			return false;
		}
		Element typeElement = _typeUtils.asElement(superclass);
		if (typeElement.getAnnotationMirrors().size() != 1) {
			reportError("Should contain an annotation");
			return false;
		}
		reportSuccess();
		return true;
	}

	/**
	 * Collect some elements that will be reused in various tests
	 * @return true if all tests passed
	 */
	private boolean collectElements() {
		_elementAC = _elementUtils.getTypeElement("targets.inherited.TestGenericChild");
		if (_elementAC == null) {
			reportError("TestGenericChild was not found");
			return false;
		}
		return true;
	}

	private void processArgsConstructorClasses(RoundEnvironment env,
			TypeElement type) {
		for (Element element : env.getElementsAnnotatedWith(type)) {
			processClass(element);
			processingEnv.getMessager().printMessage(Kind.NOTE,
					"Class " + element + " is processed");
		}
	}

	private void processClass(Element element) {

		String actionName = ArgsConstructor.class.getName();
		AnnotationValue action = null;
		for (AnnotationMirror am : processingEnv.getElementUtils()
				.getAllAnnotationMirrors(element)) {
			if (actionName.equals(am.getAnnotationType().toString())) {
				for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am
						.getElementValues().entrySet()) {
					if ("value".equals(entry.getKey().getSimpleName()
							.toString())) {
						action = entry.getValue();
						break;
					}

				}

			}
		}

		if (action == null) {
			processingEnv.getMessager()
					.printMessage(
							Kind.WARNING,
							"Class " + element
									+ " lacks a annotation with required args",
							element);
			return;
		}

		List<TypeMirror> mirrors = new ArrayList<TypeMirror>();
		for (Object val : (List<?>) action.getValue()) {
			AnnotationValue v = (AnnotationValue) val;
			TypeMirror m = (TypeMirror) v.getValue();
			mirrors.add(m);
		}

		if (!doesClassContainArgsConstructor(element, mirrors)) {
			String s = "";
			for (TypeMirror tm : mirrors) {
				if (!s.isEmpty()) {
					s += ",";
				}
				s += tm.toString();

			}
			processingEnv.getMessager().printMessage(
					Kind.ERROR,
					"Class " + element
							+ " lacks a public constructor with args: " + s,
					element);
		} else {
			processingEnv.getMessager().printMessage(Kind.NOTE,
					"Processed type: " + element);
		}
	}

	private boolean doesClassContainArgsConstructor(Element el,
			List<TypeMirror> annotTypes) {
		for (Element subelement : el.getEnclosedElements()) {
			if (subelement.getKind() == ElementKind.CONSTRUCTOR
					&& subelement.getModifiers().contains(Modifier.PUBLIC)) {
				TypeMirror mirror = subelement.asType();
				if (mirror.accept(argsVisitor, annotTypes))
					return true;
			}
		}
		return false;
	}
	/**
	 * @deprecated
	 */
	private final TypeVisitor<Boolean, List<TypeMirror>> argsVisitor = new SimpleTypeVisitor6<Boolean, List<TypeMirror>>() {
		@Override
		public Boolean visitExecutable(ExecutableType t,
				List<TypeMirror> annotatedTypes) {

			List<? extends TypeMirror> types = t.getParameterTypes();
			if (annotatedTypes.size() != types.size()) {
				return false;
			}
			Types tutil = processingEnv.getTypeUtils();

			for (int i = 0; i < types.size(); i++) {
				TypeMirror test = tutil.erasure(types.get(i));// because same
																// type bad
																// Map<String,String>
																// != Map
				TypeMirror expected = tutil.erasure(annotatedTypes.get(i));

				if (!tutil.isAssignable(expected, test)) {
					return false;
				}
			}
			return true;
		}
	};

}
