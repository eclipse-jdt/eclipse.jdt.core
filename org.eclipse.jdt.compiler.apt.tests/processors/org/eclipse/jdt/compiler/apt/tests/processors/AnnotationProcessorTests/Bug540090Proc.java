/*******************************************************************************
 * Copyright (c) 2018 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("targets.AnnotationProcessorTests.Bug540090.Bug540090")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug540090Proc extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Filer filer = processingEnv.getFiler();
		if (!annotations.isEmpty()) {
			try {
				JavaFileObject jfo = filer.createSourceFile("gen.GenClass");
				Writer writer = jfo.openWriter();
				writer.write("package gen;\n");
				writer.write("public class GenClass {\n");
				writer.write(" int x;");
				writer.write("}\n");
				writer.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
}
