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
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

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

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

public class JavacCompiler extends Compiler {
	JavacConfig compilerConfig;
	IProblemFactory problemFactory;

	Map<JavaFileObject, ICompilationUnit> fileObjectToCUMap = new HashMap<>();

	public JavacCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerConfiguration compilerConfig,
			ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, compilerConfig.compilerOptions(), requestor, problemFactory);
		this.compilerConfig = JavacConfig.createFrom(compilerConfig);
		this.problemFactory = problemFactory;
	}

	@Override
	public void compile(ICompilationUnit[] sourceUnits) {
		Map<ICompilationUnit, List<IProblem>> javacProblems = new HashMap<>();

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
		JavacTaskListener javacListener = new JavacTaskListener(this.compilerConfig, outputSourceMapping, this.problemFactory, this.fileObjectToCUMap);

		for (Entry<IContainer, List<ICompilationUnit>> outputSourceSet : outputSourceMapping.entrySet()) {

			Context javacContext = new Context();
			JavacProblemConverter problemConverter = new JavacProblemConverter(this.compilerConfig.compilerOptions(), javacContext);
			javacContext.put(DiagnosticListener.class, diagnostic -> {
				if (diagnostic.getSource() instanceof JavaFileObject fileObject) {
					JavacProblem javacProblem = problemConverter.createJavacProblem(diagnostic);
					if (javacProblem != null) {
						ICompilationUnit originalUnit = this.fileObjectToCUMap.get(fileObject);
						if (originalUnit == null) {
							return;
						}
						List<IProblem> previous = javacProblems.get(originalUnit);
						if (previous == null) {
							previous = new ArrayList<>();
							javacProblems.put(originalUnit, previous);
						}
						previous.add(javacProblem);
					}
				}
			});
			MultiTaskListener mtl = MultiTaskListener.instance(javacContext);
			mtl.add(javacListener);
			mtl.add(new TaskListener() {
				@Override
				public void finished(TaskEvent e) {
					if (e.getSourceFile() != null && fileObjectToCUMap.get(e.getSourceFile()) instanceof JCCompilationUnit u) {
						problemConverter.registerUnit(e.getSourceFile(), u);
					}
				}
			});

			// Configure Javac to generate the class files in a mapped temporary location
			var outputDir = JavacClassFile.getMappedTempOutput(outputSourceSet.getKey()).toFile();
			javacListener.setOutputDir(outputSourceSet.getKey());
			JavacUtils.configureJavacContext(javacContext, this.compilerConfig, javaProject, outputDir, true);
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
			javac.shouldStopPolicyIfError = CompileState.GENERATE;
			JavacFileManager fileManager = (JavacFileManager)javacContext.get(JavaFileManager.class);
			try {
				javac.compile(com.sun.tools.javac.util.List.from(outputSourceSet.getValue().stream()
						.filter(SourceFile.class::isInstance).map(SourceFile.class::cast).map(source -> {
							File unitFile;
							// path is relative to the workspace, make it absolute
							IResource asResource = javaProject.getProject().getParent()
									.findMember(new String(source.getFileName()));
							if (asResource != null) {
								unitFile = asResource.getLocation().toFile();
							} else {
								unitFile = new File(new String(source.getFileName()));
							}
							JavaFileObject jfo = fileManager.getJavaFileObject(unitFile.getAbsolutePath());
							fileObjectToCUMap.put(jfo, source);
							return jfo;
						}).toList()));
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
