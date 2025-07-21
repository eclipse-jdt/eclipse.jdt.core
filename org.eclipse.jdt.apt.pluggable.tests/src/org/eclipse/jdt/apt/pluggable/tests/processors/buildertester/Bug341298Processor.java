/*******************************************************************************
 * Copyright (c) 2017 Fabian Steeg and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabian Steeg <steeg@hbz-nrw.de> - Pass automatically provided options to Java 6 processors - https://bugs.eclipse.org/341298
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests.processors.buildertester;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

@SupportedAnnotationTypes("test341298.Annotation")
public class Bug341298Processor extends AbstractProcessor {

	private static boolean success = false;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment rndEnv) {
		TypeElement typeElement = processingEnv.getElementUtils().getTypeElement("test341298.Annotation");
		if (!rndEnv.getElementsAnnotatedWith(typeElement).isEmpty()) {
			try {
				success = compile(writeSourceFile(), processingEnv.getOptions());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private JavaFileObject writeSourceFile() throws IOException {
		JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile("test341298.Test");
		PrintWriter pw = new PrintWriter(sourceFile.openOutputStream());
		pw.println("package test341298;");
		pw.println("import org.eclipse.jdt.apt.pluggable.tests.annotations.*;"); // classpath dependency
		pw.write("class Test { Annotated annotated() { return null; } }"); // sourcepath dependency
		pw.close();
		return sourceFile;
	}

	private Boolean compile(JavaFileObject sourceFile, Map<String, String> opts) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		List<String> options = opts.entrySet().stream().filter(e -> !e.getKey().equals("phase"))
				.flatMap(e -> Arrays.asList("-" + e.getKey(), e.getValue()).stream()).collect(toList());

		Iterable<? extends JavaFileObject> objects = fileManager
				.getJavaFileObjectsFromFiles(Arrays.asList(new File(sourceFile.toUri())));

		Boolean success = compiler.getTask(null, fileManager, null, options, null, objects).call();
		fileManager.close();
		return success;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	public static boolean success() {
		return success;
	}
}
