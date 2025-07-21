/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that should not be invoked. Reports an error if invoked.
 */
@SupportedAnnotationTypes({"targets.AnnotationProcessorTests.bug317216.Gen"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug317216Proc extends BaseProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement typeElement : annotations) {
			if (typeElement.getQualifiedName().toString().equals("targets.AnnotationProcessorTests.bug317216.Gen")) {
					for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
						if (element.getKind() == ElementKind.CLASS) {
							printEntrySet((TypeElement) element);
						}
					}
				}
			}
		return true;
	}

	public void printEntrySet(TypeElement element) {
		ProcessingEnvironment pe = processingEnv;
		for (ExecutableElement method : ElementFilter.methodsIn(pe.getElementUtils().getAllMembers(element))) {
			if (!method.toString().contains("getFoo")) {
				continue;
			}
			TypeElement element2 = (TypeElement) pe.getTypeUtils().asElement(method.getReturnType());
			// element2 == java.util.Map
			for (ExecutableElement method2 : ElementFilter.methodsIn(pe.getElementUtils().getAllMembers(element2))) {
				if (method2.getSimpleName().toString().contains("entrySet")) {
					TypeMirror s = method2.getReturnType();
					DeclaredType st = (DeclaredType) s;

					TypeMirror e = st.getTypeArguments().get(0);
					DeclaredType de = (DeclaredType) e;
					TypeMirror[] eargs = new TypeMirror[2];
					eargs[0] = de.getTypeArguments().get(0);
					eargs[1] = de.getTypeArguments().get(1);
					TypeMirror e2 = pe.getTypeUtils().getDeclaredType((TypeElement) de.asElement(), eargs);
					// e2.toString breaks without fix
					e2.toString();
					TypeMirror[] sargs = new TypeMirror[1];
					sargs[0] = e2;

					s = pe.getTypeUtils().getDeclaredType((TypeElement) st.asElement(), sargs);
					s.toString();
				}
			}
		}
	}

}
