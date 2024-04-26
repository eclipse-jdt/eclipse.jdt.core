/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CompilerConfiguration;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.SourceFile;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;

public class JavacCompiler extends Compiler {
	CompilerConfiguration compilerConfig;

	public JavacCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerConfiguration compilerConfig,
			ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, compilerConfig.getOptions(), requestor, problemFactory);
		this.compilerConfig = compilerConfig;
	}

	@Override
	public void compile(ICompilationUnit[] sourceUnits) {
		Context javacContext = new Context();
		Map<ICompilationUnit, List<IProblem>> javacProblems = new HashMap<>();
		javacContext.put(DiagnosticListener.class, diagnostic -> {
			if (diagnostic.getSource() instanceof JavacFileObject fileObject) {
				JavacProblem javacProblem = JavacProblemConverter.createJavacProblem(diagnostic);
				List<IProblem> previous = javacProblems.get(fileObject.getOriginalUnit());
				if (previous == null) {
					previous = new ArrayList<>();
					javacProblems.put(fileObject.getOriginalUnit(), previous);
				}
				previous.add(javacProblem);
			}
		});
		JavacUtils.configureJavacContext(javacContext, this.compilerConfig, Stream.of(sourceUnits)
			.filter(SourceFile.class::isInstance)
			.map(SourceFile.class::cast)
			.map(source -> source.resource)
			.map(IResource::getProject)
			.filter(JavaProject::hasJavaNature)
			.map(JavaCore::create)
			.findFirst()
			.orElse(null));
		JavaCompiler javac = JavaCompiler.instance(javacContext);
		try {
			javac.compile(com.sun.tools.javac.util.List.from(Stream.of(sourceUnits)
				.filter(SourceFile.class::isInstance)
				.map(SourceFile.class::cast)
				.map(source -> new JavacFileObject(source, null, source.resource.getLocationURI(), Kind.SOURCE, Charset.defaultCharset()))
				.map(JavaFileObject.class::cast)
				.toList()));
		} catch (Throwable e) {
			// TODO fail
		}
		for (int i = 0; i < sourceUnits.length; i++) {
			ICompilationUnit in = sourceUnits[i];
			CompilationResult result = new CompilationResult(in, i, sourceUnits.length, Integer.MAX_VALUE);
			if (javacProblems.containsKey(in)) {
				JavacProblem[] problems = javacProblems.get(in).toArray(new JavacProblem[0]);
				result.problems = problems; // JavaBuilder is responsible for converting the problems to IMarkers
				result.problemCount = problems.length;
			}
			this.requestor.acceptResult(result);
		}
	}
}
