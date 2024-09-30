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

import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("targets.bug387956.Generate")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug387956Processor extends AbstractProcessor {

	RoundEnvironment roundEnv = null;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
			for (Element annotatedElement : annotatedElements) {
				Filer filer = processingEnv.getFiler();
				String generatedClassSimpleName = "Generated" + annotatedElement.getSimpleName().toString();
				try {
					JavaFileObject file = filer.createSourceFile("generated/" + generatedClassSimpleName, annotatedElement);
					file.openWriter() //
							.append("package generated;\n" //
									+ "\n" //
									+ "public class " + generatedClassSimpleName + " {\n" //
									+ "\n" //
									+ "}\n") //
							.close();
				} catch (IOException e) {
					Messager messager = processingEnv.getMessager();
					messager.printMessage(Diagnostic.Kind.ERROR, "IOException: " + e);
				}
			}
		}
		return true;
	}
}
