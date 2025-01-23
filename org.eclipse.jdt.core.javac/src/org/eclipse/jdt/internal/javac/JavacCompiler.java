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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

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
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.SourceFile;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.file.CacheFSInfo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Pair;
import com.sun.tools.javac.util.Context.Factory;
import com.sun.tools.javac.util.Context.Key;

public class JavacCompiler extends Compiler {
	public static final Key<Set<JavaFileObject>> FILES_WITH_ERRORS_KEY = new Key<>();
	JavacConfig compilerConfig;
	IProblemFactory problemFactory;

	Map<JavaFileObject, ICompilationUnit> fileObjectToCUMap = new HashMap<>();

	public JavacCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerConfiguration compilerConfig,
			ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, compilerConfig.compilerOptions(), requestor, problemFactory);
		this.compilerConfig = JavacConfig.createFrom(compilerConfig);
		this.problemFactory = problemFactory;
		// next is ugly workaround for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3370
		this.progress = new BuildNotifierCompilationProgress(requestor);
	}

	@Override
	public void compile(ICompilationUnit[] sourceUnits) {
		Map<ICompilationUnit, List<IProblem>> javacProblems = new HashMap<>();

		IJavaProject javaProject = Stream.of(sourceUnits).filter(SourceFile.class::isInstance).map(
		        SourceFile.class::cast).map(source -> source.resource).map(IResource::getProject).filter(
		                JavaProject::hasJavaNature).map(JavaCore::create).findFirst().orElse(null);

		var toCompile = Arrays.stream(sourceUnits)
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
			}).toList();

		JavacTaskListener javacListener = new JavacTaskListener(this, this.compilerConfig, this.problemFactory, this.fileObjectToCUMap);
		int unitIndex = 0;
		var tool = ToolProvider.getSystemJavaCompiler();
		Context javacContext = new Context();
		CacheFSInfo.preRegister(javacContext);
		ProceedOnErrorTransTypes.preRegister(javacContext);
		ProceedOnErrorGen.preRegister(javacContext);
		JavacProblemConverter problemConverter = new JavacProblemConverter(this.compilerConfig.compilerOptions(), javacContext);
		Set<JavaFileObject> sourceWithErrors = new HashSet<>();
		javacContext.put(FILES_WITH_ERRORS_KEY, sourceWithErrors);
		javacContext.put(DiagnosticListener.class, diagnostic -> {
			if (diagnostic.getSource() instanceof JavaFileObject fileObject) {
				if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
					sourceWithErrors.add(fileObject);
				}
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
		JavacUtils.configureJavacContext(javacContext, this.compilerConfig, javaProject, javacListener.tempDir.toFile(), true);
		// Javadoc problem are not reported by builder
		var javacOptions = Options.instance(javacContext);
		javacOptions.remove(Option.XDOCLINT.primaryName);
		javacOptions.remove(Option.XDOCLINT_CUSTOM.primaryName);
		javacContext.put(JavaCompiler.compilerKey, (Factory<JavaCompiler>)c -> new JavaCompiler(c) {
			boolean isInGeneration = false;

			@Override
			public void generate(Queue<Pair<Env<AttrContext>, JCClassDecl>> queue, Queue<JavaFileObject> results) {
				try {
					this.isInGeneration = true;
					super.generate(queue, results);
				} catch (AbortCompilation abort) {
					throw abort;
				} catch (Throwable ex) {
					ILog.get().error(ex.getMessage(), ex);
				} finally {
					this.isInGeneration = false;
				}
			}

			@Override
			protected void desugar(Env<AttrContext> env, Queue<Pair<Env<AttrContext>, JCClassDecl>> results) {
				try {
					super.desugar(env, results);
				} catch (AbortCompilation abort) {
					throw abort;
				} catch (Throwable ex) {
					ILog.get().error(ex.getMessage(), ex);
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
		});
		JavacFileManager fileManager = (JavacFileManager)javacContext.get(JavaFileManager.class);
		try {
			com.sun.tools.javac.util.List<JavaFileObject> sourceFiles = com.sun.tools.javac.util.List.from(toCompile.stream()
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
					}).toList());
			// Use a task to get proper initialization
			JavacTask task = ((JavacTool)tool).getTask(null, fileManager, null /* already added to context */, List.of(), List.of() /* already set */, sourceFiles, javacContext);
			task.generate();
		} catch (Throwable e) {
			// TODO fail
			ILog.get().error("compilation failed", e);
		}

		for (ICompilationUnit in : toCompile) {
			CompilationResult result = new CompilationResult(in, unitIndex, sourceUnits.length, Integer.MAX_VALUE);
			List<IProblem> problems = new ArrayList<>();
			if (javacListener.getResults().containsKey(in)) {
				result = javacListener.getResults().get(in);
				((JavacCompilationResult) result).migrateReferenceInfo();
				result.unitIndex = unitIndex;
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
			unitIndex++;
		}
	}

	@Override
	public void reportProgress(String taskDecription) {
		super.reportProgress(taskDecription);
	}
}
