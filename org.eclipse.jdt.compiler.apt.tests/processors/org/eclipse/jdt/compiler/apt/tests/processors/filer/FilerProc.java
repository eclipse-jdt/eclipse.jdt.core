/*******************************************************************************
 * Copyright (c) 2007, 2011 BEA Systems, Inc.
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
 *    philippe.marschall@netcetera.ch - Regression test for 338370
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.filer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.Filer;
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
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.eclipse.jdt.compiler.apt.tests.annotations.GenResource;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that reads the GenResource annotation and produces the specified Java type
 */
@SupportedAnnotationTypes({ "org.eclipse.jdt.compiler.apt.tests.annotations.GenResource" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({})
public class FilerProc extends BaseProcessor {

	private ProcessingEnvironment _processingEnv;
	private Messager _messager;
	private Filer _filer;

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
		_filer = _processingEnv.getFiler();
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
		if (roundEnv.processingOver()) {
			this.triggerException();
		}
		return true;
	}

	/**
	 * Perform a round of processing
	 */
	private void round(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement genClassAnno = annotations.iterator().next();
		Set<? extends Element> annotatedEls = roundEnv.getElementsAnnotatedWith(genClassAnno);
		for (Element e : annotatedEls) {
			GenResource genClassMirror = e.getAnnotation(GenResource.class);
			generateType(genClassMirror, e);
		}
	}

	private void generateType(GenResource genResourceMirror, Element annotatedEl) {
		// Collect and validate the parameters of the annotation
		String pkg = null;
		String relativeName = null;
		String stringContent = null;
		byte[] binaryContent = null;
		try {
			pkg = genResourceMirror.pkg();
			relativeName = genResourceMirror.relativeName();
			stringContent = genResourceMirror.stringContent();
			binaryContent = genResourceMirror.binaryContent();
		} catch (Exception e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to read @GenResource annotation" + e.getLocalizedMessage(), annotatedEl);
			return;
		}
		if (relativeName.length() == 0) {
			// User hasn't specified relativeName yet
			_messager.printMessage(Diagnostic.Kind.WARNING, "The relativeName attribute is missing", annotatedEl);
			return;
		}

		FileObject fo = null;
		try {
			fo = _filer.createResource(StandardLocation.SOURCE_OUTPUT, pkg, relativeName, annotatedEl);
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.WARNING,
					"Unable to open resource file for pkg " + pkg + ", relativeName " +
					relativeName + ": " + e.getLocalizedMessage(), annotatedEl);
			return;
		}
		if (null == fo) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Filer.createResource() returned null", annotatedEl);
			return;
		}
		if (stringContent.isEmpty()) {
			// Binary content.  Open an OutputStream.
			OutputStream os = null;
			try {
				os = fo.openOutputStream();
				os.write(binaryContent);
			}
			catch (Exception e) {
				_messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
				return;
			}
			finally {
				try {
					os.close();
				} catch (IOException e) {
					_messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
				}
			}
		}
		else {
			// String content.  Open a Writer.
			Writer w = null;
			try {
				w = fo.openWriter();
				w.write(stringContent);
			}
			catch (Exception e) {
				_messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
				return;
			}
			finally {
				try {
					w.close();
				} catch (IOException e) {
					_messager.printMessage(Kind.ERROR, e.getLocalizedMessage(), annotatedEl);
				}
			}
		}
	}

	private void triggerException() {
		Messager messenger = this.processingEnv.getMessager();
		try {
			_filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "not-existing.txt");
			reportError("failed");
		} catch (FileNotFoundException e) {
			reportSuccess();
			messenger.printMessage(Diagnostic.Kind.NOTE, "FileNotFoundException");
		} catch (IOException e) {
			reportSuccess();
			messenger.printMessage(Diagnostic.Kind.NOTE, e.getClass().getName());
		}
	}
}
