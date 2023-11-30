/*******************************************************************************
 * Copyright (c) 2010 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Walter Harley - initial API and implementation (based on FilerTester)
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.apt.pluggable.tests.ProcessorTestStatus;

/**
 * This processor claims <code>@InheritedTrigger</code>, which is meta-annotated
 * with <code>@Inherited</code>. It keeps track of what elements it is asked to
 * process, allowing the test method to verify that elements that inherit annotations
 * from their superclass are treated the same as if the annotations were present on
 * the element.
 *
 * @since 3.5
 */
@SupportedAnnotationTypes( { "org.eclipse.jdt.apt.pluggable.tests.annotations.InheritedTrigger" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( {})
public class InheritedAnnoProc extends AbstractProcessor {

	private static final List<String> processedElements = new ArrayList<>();

	public static List<String> getProcessedElements() {
		return Collections.unmodifiableList(processedElements);
	}
	public static void clearProcessedElements() {
		processedElements.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set,
	 *      javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		ProcessorTestStatus.setProcessorRan();
		if (!roundEnv.processingOver()) {
			for (Element el : roundEnv.getRootElements()) {
				processedElements.add(el.getSimpleName().toString());
			}
		}
		return true;
	}

}
