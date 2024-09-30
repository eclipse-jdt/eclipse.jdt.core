/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     het@google.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("targets.AnnotationProcessorTests.Bug415274.Bug415274")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug415274Proc extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(Bug415274Anno.class)) {
			System.out.println(element);
		}
		return true;
	}
}
