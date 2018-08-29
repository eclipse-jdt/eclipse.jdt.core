/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.SourceVersion;

@SupportedAnnotationTypes("targets.AnnotationProcessorTests.Bug443769.Bug443769")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug443769Proc extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Elements elements = processingEnv.getElementUtils();
		for (TypeElement typeElement : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
				for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
					elements.getElementValuesWithDefaults(annotationMirror);
				}
			}
		}
		return true;
	}
}
