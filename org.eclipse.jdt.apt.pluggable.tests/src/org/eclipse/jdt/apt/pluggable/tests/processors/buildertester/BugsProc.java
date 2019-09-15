/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation.
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
package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"org.eclipse.jdt.apt.pluggable.tests.annotations.Module"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BugsProc extends AbstractProcessor {

	private static final String[] ELEMENT_NAMES = new String[] {"targets.bug407841.ModuleCore", "targets.bug407841.ModuleLegacy"};
	private static Set<String> expectedElements = Stream.of(ELEMENT_NAMES).collect(Collectors.toSet());
	private static int _numRounds = 0;
	RoundEnvironment roundEnv = null;
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		_numRounds++;
		this.roundEnv = roundEnv;
		for (TypeElement element : annotations) {
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(element);
			for (Element e : elements) {
				expectedElements.remove(e.asType().toString());
			}
		}
		return true;
	}

	public static int getNumRounds() {
		return _numRounds;
	}

	public static int getUnprocessedElements() {
		return expectedElements.size();
	}
}
