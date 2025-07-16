package org.eclipse.jdt.apt.pluggable.tests.processors.genclass6;
/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

//package org.eclipse.jdt.compiler.apt.tests.processors.bugs;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * A processor that should not be invoked. Reports an error if invoked.
 */
@SupportedAnnotationTypes({"java.lang.Deprecated"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug419769Proc extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> rootElements = roundEnv.getRootElements();

		for (Element element : rootElements) {
			if(element.getSimpleName().contentEquals("AnnotatedClass")){
				//Always create error
				processingEnv.getMessager().printMessage(Kind.ERROR, "Some Error Message.", element);

				Filer filer = processingEnv.getFiler();

				try {
					JavaFileObject file = filer.createSourceFile("gen.GeneratedClass");

					Writer writer = file.openWriter();

					writer.append("package gen;  public class GeneratedClass{}");
					writer.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				processingEnv.getMessager().printMessage(Kind.ERROR, "Yet another Error Message.", element);
			}

		}
		return false;
	}

}
