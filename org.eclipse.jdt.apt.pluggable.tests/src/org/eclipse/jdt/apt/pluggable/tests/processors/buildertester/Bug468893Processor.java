/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

@SupportedAnnotationTypes("targets.bug468893.Annotation")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug468893Processor extends AbstractProcessor {

	private static int count = 0;

	public Bug468893Processor() {
		count = 0;
	}
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			count++;
			processingEnv.getMessager().printMessage(Kind.WARNING, "Processing over...");
			try {
				FileObject resource = processingEnv.getFiler().createSourceFile("generated.TypeIndex");
				try (BufferedWriter w = new BufferedWriter(resource.openWriter())) {
					w.append("package generated;");
					w.newLine();
					w.append("public interface TypeIndex {");
					w.newLine();
					w.append("\tpublic static final String ANNOTATED = null;");
					w.newLine();
					w.append("}");
					w.newLine();
				}
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "Could not create output " + e.getMessage());
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	public static int count() {
		return count;
	}
}
