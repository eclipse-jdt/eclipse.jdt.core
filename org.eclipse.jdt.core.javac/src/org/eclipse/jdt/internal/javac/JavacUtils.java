/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
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
import java.lang.Runtime.Version;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;

public class JavacUtils {

	public static void configureJavacContext(Context context, Map<String, String> compilerOptions, IJavaProject javaProject, boolean isTest) {
		configureJavacContext(context, compilerOptions, javaProject, null, null, isTest);
	}

	public static void configureJavacContext(Context context, JavacConfig compilerConfig,
	        IJavaProject javaProject, File output, boolean isTest) {
		configureJavacContext(context, compilerConfig.compilerOptions().getMap(), javaProject, compilerConfig, output, isTest);
	}

	private static void configureJavacContext(Context context, Map<String, String> compilerOptions,
	        IJavaProject javaProject, JavacConfig compilerConfig, File output, boolean isTest) {
		IClasspathEntry[] classpath = new IClasspathEntry[0];
		if (javaProject != null && javaProject.getProject() != null) {
			try {
				classpath = javaProject.getRawClasspath();
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		var addExports = Arrays.stream(classpath) //
				.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) //
				.map(IClasspathEntry::getExtraAttributes)
				.flatMap(Arrays::stream)
				.filter(attribute -> IClasspathAttribute.ADD_EXPORTS.equals(attribute.getName()))
				.map(IClasspathAttribute::getValue)
				.map(value -> value.split(":"))
				.flatMap(Arrays::stream)
				.collect(Collectors.joining("\0")); //$NON-NLS-1$ // \0 as expected by javac
		configureOptions(javaProject, context, compilerOptions, addExports);
		// TODO populate more from compilerOptions and/or project settings
		if (context.get(JavaFileManager.class) == null) {
			JavacFileManager.preRegister(context);
		}
		if (javaProject instanceof JavaProject internal) {
			configurePaths(internal, context, compilerConfig, output, isTest);
		}
	}

	private static void configureOptions(IJavaProject javaProject, Context context, Map<String, String> compilerOptions, String addExports) {
		boolean nineOrLater = false;
		Options options = Options.instance(context);
		options.put("allowStringFolding", Boolean.FALSE.toString());
		final Version complianceVersion;
		String compliance = compilerOptions.get(CompilerOptions.OPTION_Compliance);
		if (CompilerOptions.VERSION_1_8.equals(compliance)) {
			compliance = "8";
			nineOrLater = false;
		}
		if (CompilerOptions.ENABLED.equals(compilerOptions.get(CompilerOptions.OPTION_Release))
			&& compliance != null && !compliance.isEmpty()) {
			complianceVersion = Version.parse(compliance);
			options.put(Option.RELEASE, compliance);
			nineOrLater = complianceVersion.compareTo(Version.parse("9")) >= 0;
		} else {
			String source = compilerOptions.get(CompilerOptions.OPTION_Source);
			if (CompilerOptions.VERSION_1_8.equals(source)) {
				source = "8";
				nineOrLater = false;
			}
			if (source != null && !source.isBlank()) {
				complianceVersion = Version.parse(source);
				if (complianceVersion.compareToIgnoreOptional(Version.parse("8")) < 0) {
					ILog.get().warn("Unsupported source level: " + source + ", using 8 instead");
					options.put(Option.SOURCE, "8");
					nineOrLater = false;
				} else {
					options.put(Option.SOURCE, source);
					nineOrLater = complianceVersion.compareTo(Version.parse("9")) >= 0;
				}
			} else {
				complianceVersion = Runtime.version();
				nineOrLater = true;
			}
			String target = compilerOptions.get(CompilerOptions.OPTION_TargetPlatform);
			if (CompilerOptions.VERSION_1_8.equals(target)) {
				target = "8";
				nineOrLater = false;
			}
			if (target != null && !target.isEmpty()) {
				Version version = Version.parse(target);
				if (version.compareToIgnoreOptional(Version.parse("8")) < 0) {
					ILog.get().warn("Unsupported target level: " + target + ", using 8 instead");
					options.put(Option.TARGET, "8");
					nineOrLater = false;
				} else {
					if (Integer.parseInt(target) < Integer.parseInt(source)) {
						ILog.get().warn("javac requires the source version to be less than or equal to the target version. Targetting " + source + " instead");
						target = source;
					}
					options.put(Option.TARGET, target);
				}
			}
		}
		if (CompilerOptions.ENABLED.equals(compilerOptions.get(CompilerOptions.OPTION_EnablePreviews)) &&
			Runtime.version().feature() == complianceVersion.feature()) {
			options.put(Option.PREVIEW, Boolean.toString(true));
		}
		options.put(Option.XLINT, Boolean.toString(true));
		options.put(Option.XLINT_CUSTOM, "all");
		options.put(Option.XDOCLINT, Boolean.toString(true));
		options.put(Option.XDOCLINT_CUSTOM, "all");
		if (addExports != null && !addExports.isBlank()) {
			options.put(Option.ADD_EXPORTS, addExports);
		}
		if (JavaCore.ENABLED.equals(compilerOptions.get(JavaCore.COMPILER_DOC_COMMENT_SUPPORT))) {
			options.put(Option.XDOCLINT, Boolean.toString(true));
		}
		if (nineOrLater && !options.isSet(Option.RELEASE) && javaProject instanceof JavaProject javaProjectImpl) {
			try {
				for (IClasspathEntry entry : javaProject.getRawClasspath()) {
					if (entry.getPath() != null && entry.getPath().toString().startsWith("org.eclipse.jdt.launching.JRE_CONTAINER")) {
						for (IClasspathEntry resolved : javaProjectImpl.resolveClasspath(new IClasspathEntry[] { entry })) {
							options.put(Option.SYSTEM, resolved.getPath().toString());
						}
					}
				}
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}

		Map<String, String> processorOptions = ProcessorConfig.getProcessorOptions(javaProject);
		for (Entry<String, String> processorOption : processorOptions.entrySet()) {
			options.put("-A" + processorOption.getKey() + "=" + processorOption.getValue(), Boolean.toString(true));
		}

		addDebugInfos(compilerOptions, options);
	}

	private static void addDebugInfos(Map<String, String> compilerOptions, Options options) {
		boolean generateVars = CompilerOptions.GENERATE.equals(compilerOptions.get(CompilerOptions.OPTION_LocalVariableAttribute));
		boolean generateLines = CompilerOptions.GENERATE.equals(compilerOptions.get(CompilerOptions.OPTION_LineNumberAttribute));
		boolean generateSource = CompilerOptions.GENERATE.equals(compilerOptions.get(CompilerOptions.OPTION_SourceFileAttribute));
		if (generateVars && generateLines && generateSource) {
			options.put(Option.G, Boolean.toString(true));
		} else if (!generateVars && !generateLines && !generateSource) {
			options.put(Option.G_CUSTOM, Boolean.toString(true));
			options.put(Option.G_NONE, Boolean.toString(true));
		} else {
			options.put(Option.G_CUSTOM, Boolean.toString(true));
			if (generateVars) {
				options.put("-g:vars", Boolean.toString(true));
			}
			if (generateLines) {
				options.put("-g:lines", Boolean.toString(true));
			}
			if (generateSource) {
				options.put("-g:source", Boolean.toString(true));
			}
		}
	}

	private static void configurePaths(JavaProject javaProject, Context context, JavacConfig compilerConfig,
	        File output, boolean isTest) {
		var fileManager = (StandardJavaFileManager)context.get(JavaFileManager.class);
		try {
			if (compilerConfig != null && !isEmpty(compilerConfig.annotationProcessorPaths())) {
				fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH,
					compilerConfig.annotationProcessorPaths()
						.stream()
						.map(File::new)
						.toList());
			}
			if (compilerConfig != null && !isEmpty(compilerConfig.generatedSourcePaths())) {
				fileManager.setLocation(StandardLocation.SOURCE_OUTPUT,
					compilerConfig.generatedSourcePaths()
						.stream()
						.map(File::new)
						.map(file -> {
							if (!file.exists() && !file.mkdirs()) {
								ILog.get().warn("Failed to create generated source file directory: " + file);
							}
							return file;
						})
						.toList());
			}

			if (output != null) {
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(ensureDirExists(output)));
			} else if (compilerConfig != null && !compilerConfig.sourceOutputMapping().isEmpty()) {
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, compilerConfig.sourceOutputMapping().values().stream().distinct()
						.map(container -> ensureDirExists(JavacClassFile.getMappedTempOutput(container).toFile())).toList());
			} else if (javaProject.getProject() != null) {
				IResource member = javaProject.getProject().getParent().findMember(javaProject.getOutputLocation());
				if( member != null ) {
					File f = member.getLocation().toFile();
					fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(ensureDirExists(f)));
				}
			}

			boolean sourcePathEnabled = false;
			if (compilerConfig != null && !isEmpty(compilerConfig.sourcepaths())) {
				fileManager.setLocation(StandardLocation.SOURCE_PATH,
					compilerConfig.sourcepaths()
						.stream()
						.map(File::new)
						.toList());
				sourcePathEnabled = true;
			}
			if (compilerConfig != null && !isEmpty(compilerConfig.moduleSourcepaths())) {
				fileManager.setLocation(StandardLocation.MODULE_SOURCE_PATH,
					compilerConfig.moduleSourcepaths()
						.stream()
						.map(File::new)
						.toList());
				sourcePathEnabled = true;
			}
			if (!sourcePathEnabled) {
				fileManager.setLocation(StandardLocation.SOURCE_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE  && (isTest || !entry.isTest())));
			}
			
			boolean classpathEnabled = false;
			if (compilerConfig != null && !isEmpty(compilerConfig.classpaths())) {
				fileManager.setLocation(StandardLocation.CLASS_PATH,
					compilerConfig.classpaths()
						.stream()
						.map(File::new)
						.toList());
				classpathEnabled = true;
			}
			
			if (compilerConfig != null && !isEmpty(compilerConfig.modulepaths())) {
				fileManager.setLocation(StandardLocation.MODULE_PATH,
					compilerConfig.modulepaths()
						.stream()
						.map(File::new)
						.toList());
				classpathEnabled = true;
			}
			if (!classpathEnabled) {
				Set<JavaProject> moduleProjects = Stream.of(javaProject.getExpandedClasspath())
						.filter(classpath -> classpath.getEntryKind() == IClasspathEntry.CPE_PROJECT)
						.map(classpath -> javaProject.getJavaModel().getJavaProject(classpath.getPath().lastSegment()))
						.filter(Objects::nonNull)
						.filter(classpathJavaProject -> {
							try {
								return classpathJavaProject.getModuleDescription() != null;
							} catch (JavaModelException e) {
								return false;
							}
						})
						.collect(Collectors.toSet());
				
				fileManager.setLocation(StandardLocation.CLASS_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() != IClasspathEntry.CPE_SOURCE && (isTest || !entry.isTest())));
				
				if (!moduleProjects.isEmpty()) {
					fileManager.setLocation(StandardLocation.MODULE_PATH, moduleProjects.stream()
							.map(project -> {
								try {
									IPath relativeOutputPath = project.getOutputLocation();
									IPath absPath = javaProject.getProject().getParent()
											.findMember(relativeOutputPath).getLocation();
									return absPath.toOSString();
								} catch (JavaModelException e) {
									return null;
								}
							})
							.filter(Objects::nonNull)
							.map(File::new)
							.toList());
				}
			}
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}

	public static <T> boolean isEmpty(List<T> list) {
		return list == null || list.isEmpty();
	}

	private static Collection<File> classpathEntriesToFiles(JavaProject project, Predicate<IClasspathEntry> select) {
		try {
			LinkedHashSet<File> res = new LinkedHashSet<>();
			Queue<IClasspathEntry> toProcess = new LinkedList<>();
			toProcess.addAll(Arrays.asList(project.resolveClasspath(project.getExpandedClasspath())));
			while (!toProcess.isEmpty()) {
				IClasspathEntry current = toProcess.poll();
				if (current.getEntryKind() == IClasspathEntry.CPE_PROJECT && select.test(current)) {
					IResource referencedResource = project.getProject().getParent().findMember(current.getPath());
					if (referencedResource instanceof IProject referencedProject) {
						JavaProject referencedJavaProject = (JavaProject) JavaCore.create(referencedProject);
						if (referencedJavaProject.exists()) {
							IModuleDescription moduleDescription = null;
							try {
								moduleDescription = referencedJavaProject.getModuleDescription();
							} catch (JavaModelException e) {
								// do nothing
							}
							if (moduleDescription == null) {
								IPath path = referencedJavaProject.getOutputLocation();
								addPath(referencedJavaProject, path, res);
								for (IClasspathEntry transitiveEntry : referencedJavaProject.resolveClasspath(referencedJavaProject.getExpandedClasspath()) ) {
									if (transitiveEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
										IPath outputLocation = transitiveEntry.getOutputLocation();
										if (outputLocation != null && select.test(transitiveEntry)) {
											addPath(referencedJavaProject, outputLocation, res);
										}
									} else if (transitiveEntry.isExported()) {
										toProcess.add(transitiveEntry);
									}
								}
							}
						}
					}
				} else if (select.test(current)) {
					IPath path = current.getPath();
					addPath(project, path, res);
				}
			}
			return res;
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return List.of();
		}
	}

		private static void addPath(JavaProject project, IPath path, LinkedHashSet<File> res) {
		File asFile = path.toFile();
		if (asFile.exists()) {
			res.add(asFile);
		} else {
			IResource asResource = project.getProject().getParent().findMember(path);
			if (asResource != null && asResource.exists()) {
				res.add(asResource.getLocation().toFile());
			}
		}
	}

	private static File ensureDirExists(File file) {
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public static boolean isTest(IJavaProject project, org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] units) {
		if (units == null || project == null || project.getResource() == null) {
			return false;
		}
		Set<IPath> testFolders = new HashSet<>();
		try {
			for (IClasspathEntry entry : project.getResolvedClasspath(false)) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.isTest()) {
					testFolders.add(entry.getPath());
				}
			}
			return Arrays.stream(units)
					.map(org.eclipse.jdt.internal.compiler.env.ICompilationUnit::getFileName)
					.map(String::new)
					.map(Path::new)
					.anyMatch(file -> testFolders.stream().anyMatch(folder -> folder.isPrefixOf(file)));
		}  catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return true;
		}
	}

	/// Removes non-relevant content (eg other method blocks) for given focal position
	public static void trimUnvisibleContent(JCCompilationUnit u, int focalPoint, Context context) {
		TreeMaker treeMaker = TreeMaker.instance(context);
		u.accept(new TreeScanner() {
			@Override
			public void visitMethodDef(JCMethodDecl decl) {
				if (decl.getBody() != null &&
					!decl.getBody().getStatements().isEmpty() &&
					!(decl.getStartPosition() <= focalPoint &&
					decl.getStartPosition() + TreeInfo.getEndPos(decl, u.endPositions) >= focalPoint)) {
					var throwNewRuntimeExceptionOutOfFocalPositionScope =
						treeMaker.Throw(
								treeMaker.NewClass(null, null,
										treeMaker.Ident(Names.instance(context).fromString(RuntimeException.class.getSimpleName())),
										com.sun.tools.javac.util.List.of(treeMaker.Literal("Out of focalPosition scope")), null)); //$NON-NLS-1$
					decl.body.stats = com.sun.tools.javac.util.List.of(throwNewRuntimeExceptionOutOfFocalPositionScope);
				}
			}
		});
	}
}
