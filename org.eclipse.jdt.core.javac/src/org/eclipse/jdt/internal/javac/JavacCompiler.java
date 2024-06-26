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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.CompilerConfiguration;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.SourceFile;

import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

public class JavacCompiler extends Compiler {
	JavacConfig compilerConfig;

	public JavacCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerConfiguration compilerConfig,
			ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, compilerConfig.compilerOptions(), requestor, problemFactory);
		this.compilerConfig = JavacConfig.createFrom(compilerConfig);
	}

	@Override
	public void compile(ICompilationUnit[] sourceUnits) {
		Context javacContext = new Context();
		Map<ICompilationUnit, List<IProblem>> javacProblems = new HashMap<>();
		JavacProblemConverter problemConverter = new JavacProblemConverter(this.compilerConfig.compilerOptions(), javacContext);
		javacContext.put(DiagnosticListener.class, diagnostic -> {
			if (diagnostic.getSource() instanceof JavacFileObject fileObject) {
				JavacProblem javacProblem = problemConverter.createJavacProblem(diagnostic);
				if (javacProblem != null) {
					List<IProblem> previous = javacProblems.get(fileObject.getOriginalUnit());
					if (previous == null) {
						previous = new ArrayList<>();
						javacProblems.put(fileObject.getOriginalUnit(), previous);
					}
					previous.add(javacProblem);
				}
			}
		});
		IJavaProject javaProject = Stream.of(sourceUnits).filter(SourceFile.class::isInstance).map(
		        SourceFile.class::cast).map(source -> source.resource).map(IResource::getProject).filter(
		                JavaProject::hasJavaNature).map(JavaCore::create).findFirst().orElse(null);
		
		Map<File, List<ICompilationUnit>> outputSourceMapping = Arrays.stream(sourceUnits)
			.filter(unit -> {
				/**
				 * Exclude the generated sources from the original source path to
				 * prevent conflicts with Javac's annotation processing.
				 *
				 * If the generated sources are already included in the input
				 * source list, Javac won't be able to regenerate those sources
				 * through annotation processing.
				 */
				if (unit instanceof SourceFile sf) {
					File sourceFile = sf.resource.getLocation().toFile();
					if (this.compilerConfig != null && !JavacUtils.isEmpty(this.compilerConfig.generatedSourcePaths())) {
						return !this.compilerConfig.generatedSourcePaths().stream()
							.anyMatch(path -> sourceFile.toPath().startsWith(Path.of(path)));
					}
				}
				return true;
			})
			.collect(Collectors.groupingBy(this::computeOutputDirectory));

		// Register listener to intercept intermediate results from Javac task.
		JavacTaskListener resultListener = new JavacTaskListener(this.compilerConfig, outputSourceMapping);
		MultiTaskListener mtl = MultiTaskListener.instance(javacContext);
		mtl.add(resultListener);

		for (Entry<File, List<ICompilationUnit>> outputSourceSet : outputSourceMapping.entrySet()) {
			var outputFile = outputSourceSet.getKey();
			JavacUtils.configureJavacContext(javacContext, this.compilerConfig, javaProject, outputFile);
			JavaCompiler javac = new JavaCompiler(javacContext) {
				boolean isInGeneration = false;

				@Override
				protected boolean shouldStop(CompileState cs) {
					// Never stop
					return false;
				}

				@Override
				public void generate(Queue<Pair<Env<AttrContext>, JCClassDecl>> queue, Queue<JavaFileObject> results) {
					try {
						this.isInGeneration = true;
						super.generate(queue, results);
					} catch (Throwable ex) {
						// TODO error handling
					} finally {
						this.isInGeneration = false;
					}
				}

				@Override
				protected void desugar(Env<AttrContext> env, Queue<Pair<Env<AttrContext>, JCClassDecl>> results) {
					try {
						super.desugar(env, results);
					} catch (Throwable ex) {
						// TODO error handling
					}
				}

				@Override
				public int errorCount() {
					// See JavaCompiler.genCode(Env<AttrContext> env, JCClassDecl cdef),
					// it stops writeClass if errorCount is not zero.
					// Force it to return 0 if we are in generation phase, and keeping
					// generating class files for those files without errors.
					return this.isInGeneration ? 0 : super.errorCount();
				}
			};
			javacContext.put(JavaCompiler.compilerKey, javac);
			javac.shouldStopPolicyIfError = CompileState.GENERATE;
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
	
	private File computeOutputDirectory(ICompilationUnit unit) {
		if (unit instanceof SourceFile sf) {
			File sourceFile = sf.resource.getLocation().toFile();
			File sourceDirectory = sourceFile.getParentFile();
			while (sourceDirectory != null) {
				File mappedOutput = this.compilerConfig.sourceOutputMapping().get(sourceDirectory);
				if (mappedOutput != null) {
					return mappedOutput;
				}
				sourceDirectory = sourceDirectory.getParentFile();
			}
		}
		return null;
	}
}
