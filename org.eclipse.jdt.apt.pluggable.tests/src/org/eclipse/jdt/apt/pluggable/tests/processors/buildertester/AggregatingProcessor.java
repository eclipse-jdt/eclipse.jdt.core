/*******************************************************************************
 * Copyright (c) 2026 Bas Gooren and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("test.Aggregate")
public class AggregatingProcessor extends AbstractProcessor {

	public static final String AGGREGATING_OPTION = "org.gradle.annotation.processing.aggregating";

	private static boolean enabled;
	private static int processingRounds;

	public static void setEnabled(boolean enabled) {
		AggregatingProcessor.enabled = enabled;
	}

	public static int getProcessingRounds() {
		return processingRounds;
	}

	public static void resetProcessingRounds() {
		processingRounds = 0;
	}

	@Override
	public Set<String> getSupportedOptions() {
		return enabled ? Set.of(AGGREGATING_OPTION) : Set.of();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver() || annotations.isEmpty()) {
			return false;
		}
		processingRounds++;

		TypeElement annotation = annotations.iterator().next();
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
		String names = elements.stream() //
				.map(element -> ((TypeElement) element).getQualifiedName().toString()) //
				.sorted(Comparator.naturalOrder()) //
				.map(name -> "\t\t\"" + name + "\"") //
				.collect(Collectors.joining(",\n"));
		try {
			var source = processingEnv.getFiler().createSourceFile("generated.AggregatedTypes",
					elements.toArray(Element[]::new));
			try (var writer = source.openWriter()) {
				writer.write("package generated;\n\n");
				writer.write("public class AggregatedTypes {\n");
				writer.write("\tpublic static final String[] TYPES = {\n");
				writer.write(names);
				writer.write("\n\t};\n");
				writer.write("}\n");
			}
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Could not generate aggregate output: " + e.getMessage());
		}
		return false;
	}
}
