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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
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
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;

public class JavacCompiler extends Compiler {
	JavacConfig compilerConfig;
	IProblemFactory problemFactory;

	public JavacCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerConfiguration compilerConfig,
			ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, compilerConfig.compilerOptions(), requestor, problemFactory);
		this.compilerConfig = JavacConfig.createFrom(compilerConfig);
		this.problemFactory = problemFactory;
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

		Map<IContainer, List<ICompilationUnit>> outputSourceMapping = Arrays.stream(sourceUnits)
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
		JavacTaskListener javacListener = new JavacTaskListener(this.compilerConfig, outputSourceMapping, this.problemFactory);
		MultiTaskListener mtl = MultiTaskListener.instance(javacContext);
		mtl.add(javacListener);

		for (Entry<IContainer, List<ICompilationUnit>> outputSourceSet : outputSourceMapping.entrySet()) {
			// Configure Javac to generate the class files in a mapped temporary location
			var outputDir = JavacClassFile.getMappedTempOutput(outputSourceSet.getKey()).toFile();
			JavacUtils.configureJavacContext(javacContext, this.compilerConfig, javaProject, outputDir, true);
			JavaCompiler javac = TolerantJavaCompiler.configureCompilerInstance(javacContext);
			javac.shouldStopPolicyIfError = CompileState.GENERATE;
			try {
				javac.compile(com.sun.tools.javac.util.List.from(outputSourceSet.getValue().stream()
						.filter(SourceFile.class::isInstance).map(SourceFile.class::cast).map(source -> {
							File unitFile;
							if (javaProject != null) {
								// path is relative to the workspace, make it absolute
								IResource asResource = javaProject.getProject().getParent()
										.findMember(new String(source.getFileName()));
								if (asResource != null) {
									unitFile = asResource.getLocation().toFile();
								} else {
									unitFile = new File(new String(source.getFileName()));
								}
							} else {
								unitFile = new File(new String(source.getFileName()));
							}
							return new JavacFileObject(source, null, unitFile.toURI(), Kind.SOURCE,
									Charset.defaultCharset());
						}).map(JavaFileObject.class::cast).toList()));
			} catch (Throwable e) {
				// TODO fail
				ILog.get().error("compilation failed", e);
			}
			for (int i = 0; i < sourceUnits.length; i++) {
				ICompilationUnit in = sourceUnits[i];
				CompilationResult result = new CompilationResult(in, i, sourceUnits.length, Integer.MAX_VALUE);
				List<IProblem> problems = new ArrayList<>();
				if (javacListener.getResults().containsKey(in)) {
					result = javacListener.getResults().get(in);
					((JavacCompilationResult) result).migrateReferenceInfo();
					result.unitIndex = i;
					result.totalUnitsKnown = sourceUnits.length;
					List<CategorizedProblem> additionalProblems = ((JavacCompilationResult) result).getAdditionalProblems();
					if (additionalProblems != null && !additionalProblems.isEmpty()) {
						problems.addAll(additionalProblems);
					}
				}

				if (javacProblems.containsKey(in)) {
					problems.addAll(javacProblems.get(in));
				}
				// JavaBuilder is responsible for converting the problems to IMarkers
				result.problems = problems.toArray(new CategorizedProblem[0]);
				result.problemCount = problems.size();
				this.requestor.acceptResult(result);
				if (result.compiledTypes != null) {
					for (Object type : result.compiledTypes.values()) {
						if (type instanceof JavacClassFile classFile) {
							// Delete the temporary class file generated by Javac
							classFile.deleteTempClassFile();
							/**
							 * Javac does not generate class files for files with errors.
							 * However, we return 0 bytes to the CompilationResult to
							 * prevent NPE when the ImageBuilder writes failed class files.
							 * These 0-byte class files are empty and meaningless, which
							 * can confuse subsequent compilations since they are included
							 * in the classpath. Therefore, they should be deleted after
							 * compilation.
							 */
							if (classFile.getBytes().length == 0) {
								classFile.deleteExpectedClassFile();
							}
						}
					}
				}
			}
		}
	}

	private IContainer computeOutputDirectory(ICompilationUnit unit) {
		if (unit instanceof SourceFile sf) {
			IContainer sourceDirectory = sf.resource.getParent();
			while (sourceDirectory != null) {
				IContainer mappedOutput = this.compilerConfig.sourceOutputMapping().get(sourceDirectory);
				if (mappedOutput != null) {
					return mappedOutput;
				}
				sourceDirectory = sourceDirectory.getParent();
			}
		}
		return null;
	}
}
