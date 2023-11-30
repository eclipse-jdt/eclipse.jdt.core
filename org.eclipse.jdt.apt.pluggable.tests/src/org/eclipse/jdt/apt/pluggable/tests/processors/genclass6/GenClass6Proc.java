/*******************************************************************************
 * Copyright (c) 2007, 2009 BEA Systems, Inc.
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

package org.eclipse.jdt.apt.pluggable.tests.processors.genclass6;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
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
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;

/**
 * A processor that reads the GenClass6 annotation and produces the specified Java type
 */
@SupportedAnnotationTypes({ "org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({})
public class GenClass6Proc extends AbstractProcessor {

	private ProcessingEnvironment _processingEnv;
	private Messager _messager;
	private Filer _filer;
	private Map<String, Element> _classesToSummarize; // map of generated name to element that produced it

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		_processingEnv = processingEnv;
		_filer = _processingEnv.getFiler();
		_messager = _processingEnv.getMessager();
		_classesToSummarize = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver() && !_classesToSummarize.isEmpty()) {
			summarize();
		}
		else if (!annotations.isEmpty()) {
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
		for (Element e : annotatedEls) {
			GenClass6 genClassMirror = e.getAnnotation(GenClass6.class);
			processType(genClassMirror, e);
		}
	}

	private void processType(GenClass6 genClassMirror, Element annotatedEl) {
		// Collect and validate the parameters of the annotation
		String pkg = null;
		String name = null;
		String method = null;
		boolean summary = false;
		int rounds = 1;
		String[] options = null;
		try {
			pkg = genClassMirror.pkg();
			name = genClassMirror.name();
			method = genClassMirror.method();
			summary = genClassMirror.summary();
			rounds = genClassMirror.rounds();
			options = genClassMirror.options();
		} catch (Exception e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to read @GenClass6 annotation" + e.getLocalizedMessage(), annotatedEl);
			return;
		}

		// Options allow the processor to expose certain error conditions.
		if (null != options) {
			Set<String> optionSet = new HashSet<>(Arrays.asList(options));
			// See https://bugs.eclipse.org/269934: calling getEnclosedElements forces resolution of referenced types
			if (optionSet.contains("forceElementResolution")) {
				annotatedEl.getEnclosedElements();
			}
		}

		if (name.length() == 0) {
			// User hasn't specified name yet
			_messager.printMessage(Diagnostic.Kind.WARNING, "The name attribute is missing", annotatedEl);
			return;
		}
		if (pkg == null) {
			pkg = "";
		}
		String qname = (pkg.length() > 0) ? pkg + '.' + name : name;
		if (method == null) {
			method = "";
		}

		// Get a writer
		JavaFileObject jfo = null;
		try {
			jfo = _filer.createSourceFile(qname, annotatedEl);
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to open file for class " + qname, annotatedEl);
			return;
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(jfo.openWriter());
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.WARNING, "Unable to get writer for file " + jfo.getName());
			return;
		}

		// Generate the class
		if (summary) {
			_classesToSummarize.put(qname, annotatedEl);
		}
		pw.println("// Generated by " + this.getClass().getName());
		pw.println("package " + pkg + ";");
		if (rounds > 1) {
			pw.println("import " + GenClass6.class.getCanonicalName() + ";");
			StringBuilder sb = new StringBuilder();
			sb.append("@GenClass6(");
			if (pkg.length() > 0) {
				sb.append("pkg = \"");
				sb.append(pkg);
				sb.append("\", ");
			}
			sb.append("name = \"");
			sb.append(name);
			sb.append("Gen\"");
			if (method.length() > 0) {
				sb.append(", method = \"");
				sb.append(method);
				sb.append("\"");
			}
			if (--rounds > 1) {
				sb.append(", rounds = ");
				sb.append(rounds);
			}
			if (summary) {
				sb.append(", summary = true");
			}
			sb.append(")");
			pw.println(sb.toString());
		}
		pw.println("public class " + name + "{");
		if (method != null && method.length() > 0) {
			pw.println("\tpublic String " + method + "() { return null; }");
		}
		pw.println("}");
		pw.close();
	}

	/**
	 * Generate the summary.txt file if requested
	 */
	protected void summarize() {
		PrintWriter pw = null;
		try {
			Element[] parents = new Element[_classesToSummarize.size()];
			parents = _classesToSummarize.values().toArray(parents);
			FileObject summaryFile = _filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "summary.txt", parents);
			pw = new PrintWriter(summaryFile.openWriter());
			for (String clazz : _classesToSummarize.keySet()) {
				pw.println(clazz);
			}
			pw.flush();
		} catch (IOException e) {
			_messager.printMessage(Diagnostic.Kind.ERROR, "Unable to create summary.txt: " + e.getLocalizedMessage());
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

}
