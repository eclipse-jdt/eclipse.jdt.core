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

package org.eclipse.jdt.apt.pluggable.tests.processors.message6;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.eclipse.jdt.apt.pluggable.tests.annotations.Message6;

/**
 * A processor that reads the Message6 annotation and sends output via the Messager API
 */
@SupportedAnnotationTypes({"org.eclipse.jdt.apt.pluggable.tests.annotations.Message6"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({})
public class Message6Proc extends AbstractProcessor {

	private ProcessingEnvironment _processingEnv;
	private Messager _messager;

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
		_messager = _processingEnv.getMessager();
	}
	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv)
	{
		if (!annotations.isEmpty()) {
			round(annotations, roundEnv);
		}
		return true;
	}

	/**
	 * Perform a round of processing
	 */
	private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement genClassAnno = annotations.iterator().next();
		Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(genClassAnno);
		for (Element annotatedEl : annotatedEls) {
			Message6 messageMirror = annotatedEl.getAnnotation(Message6.class);
			Diagnostic.Kind kind = Diagnostic.Kind.OTHER;
			String text = null;
			try {
				kind = messageMirror.value();
				text = messageMirror.text();
			} catch (Exception e) {
				// Do nothing: compiler will have put up a syntax error on the annotation already
				return;
			}
			if (kind != Diagnostic.Kind.OTHER) {
				_messager.printMessage(kind, text, annotatedEl);
			}
		}
	}

}
