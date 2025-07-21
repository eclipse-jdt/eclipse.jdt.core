/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
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

@SupportedAnnotationTypes("targets.AnnotationProcessorTests.Bug456986.Bug456986")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug456986Proc extends AbstractProcessor
{
	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Filer filer = processingEnv.getFiler();
		if (!annotations.isEmpty()) {
			try {
				JavaFileObject jfo = filer.createSourceFile("gen.anno.Annos");
				Writer writer = jfo.openWriter();
				writer.write("package gen.anno;\n");
				writer.write("import java.lang.annotation.ElementType;\n");
				writer.write("import java.lang.annotation.Retention;\n");
				writer.write("import java.lang.annotation.RetentionPolicy;\n");
				writer.write("import java.lang.annotation.Target;\n");
				writer.write("public final class Annos {\n");
				writer.write("  @Retention(RetentionPolicy.RUNTIME)\n");
				writer.write("  @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})\n");
				writer.write("  public @interface GenAnno {}\n");
				writer.write("\n");
				writer.write("  private Annos() {}\n");
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
