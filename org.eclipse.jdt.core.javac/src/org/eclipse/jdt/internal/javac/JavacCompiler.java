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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
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
				JavacProblem javacProblem = JavacProblemConverter.createJavacProblem(diagnostic, javacContext);
				List<IProblem> previous = javacProblems.get(fileObject.getOriginalUnit());
				if (previous == null) {
					previous = new ArrayList<>();
					javacProblems.put(fileObject.getOriginalUnit(), previous);
				}
				previous.add(javacProblem);
			}
		});
		IJavaProject javaProject = Stream.of(sourceUnits).filter(SourceFile.class::isInstance).map(
		        SourceFile.class::cast).map(source -> source.resource).map(IResource::getProject).filter(
		                JavaProject::hasJavaNature).map(JavaCore::create).findFirst().orElse(null);
		
		Map<File, List<ICompilationUnit>> outputSourceMapping = groupByOutput(sourceUnits);
		
		for (Entry<File, List<ICompilationUnit>> outputSourceSet : outputSourceMapping.entrySet()) {
			var outputFile = outputSourceSet.getKey();
			JavacUtils.configureJavacContext(javacContext, this.compilerConfig, javaProject, outputFile);
			JavaCompiler javac = JavaCompiler.instance(javacContext);
			try {
				javac.compile(com.sun.tools.javac.util.List.from(
				        outputSourceSet.getValue().stream().filter(SourceFile.class::isInstance).map(
				                SourceFile.class::cast).map(
				                        source -> new JavacFileObject(source, null, source.resource.getLocationURI(),
				                                Kind.SOURCE, Charset.defaultCharset())).map(
				                                        JavaFileObject.class::cast).toList()));
			} catch (Throwable e) {
				// TODO fail
			}
			for (int i = 0; i < sourceUnits.length; i++) {
				ICompilationUnit in = sourceUnits[i];
				CompilationResult result = new CompilationResult(in, i, sourceUnits.length, Integer.MAX_VALUE);
				if (javacProblems.containsKey(in)) {
					JavacProblem[] problems = javacProblems.get(in).toArray(new JavacProblem[0]);
					result.problems = problems; // JavaBuilder is responsible
					                            // for converting the problems
					                            // to IMarkers
					result.problemCount = problems.length;
				}
				this.requestor.acceptResult(result);
			}
		}
	}
	
	/**
	 * @return grouped files where for each unique output folder, the mapped
	 *         list of source folders
	 */
	private Map<File, List<ICompilationUnit>> groupByOutput(ICompilationUnit[] sourceUnits) {
		Map<Path, ICompilationUnit> pathsToUnits = new HashMap<>();
		for (ICompilationUnit unit : sourceUnits) {
			if (unit instanceof SourceFile sf) {
				pathsToUnits.put(sf.resource.getLocation().toFile().toPath(), unit);
			}
		}
		
		Map<File, List<ICompilationUnit>> groupResult = new HashMap<>();
		this.compilerConfig.getSourceOutputMapping().entrySet().forEach(entry -> {
			groupResult.compute(entry.getValue(), (key, exising) -> {
				final List<ICompilationUnit> result;
				if (exising == null) {
					result = new ArrayList<>();
				} else {
					result = exising;
				}
				pathsToUnits.entrySet().stream().filter(
				        e -> e.getKey().startsWith(entry.getKey().toPath())).findFirst().ifPresent(
				        e -> result.add(e.getValue()));
				return result;
			});
		});
		return groupResult;
	}
}
