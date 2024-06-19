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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class JavacUtils {

	public static void configureJavacContext(Context context, Map<String, String> compilerOptions, IJavaProject javaProject) {
		configureJavacContext(context, compilerOptions, javaProject, null, null);
	}

	public static void configureJavacContext(Context context, JavacConfig compilerConfig,
	        IJavaProject javaProject, File output) {
		configureJavacContext(context, compilerConfig.compilerOptions().getMap(), javaProject, compilerConfig, output);
	}

	private static void configureJavacContext(Context context, Map<String, String> compilerOptions,
	        IJavaProject javaProject, JavacConfig compilerConfig, File output) {
		IClasspathEntry[] classpath = new IClasspathEntry[0];
		if (javaProject != null) {
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
		configureOptions(context, compilerOptions, addExports);
		// TODO populate more from compilerOptions and/or project settings
		if (context.get(JavaFileManager.class) == null) {
			JavacFileManager.preRegister(context);
		}
		if (javaProject instanceof JavaProject internal) {
			configurePaths(internal, context, compilerConfig, output);
		}
	}

	private static void configureOptions(Context context, Map<String, String> compilerOptions, String addExports) {
		Options options = Options.instance(context);
		options.put("allowStringFolding", Boolean.FALSE.toString());
		final Version complianceVersion;
		String compliance = compilerOptions.get(CompilerOptions.OPTION_Compliance);
		if (CompilerOptions.VERSION_1_8.equals(compliance)) {
			compliance = "8";
		}
		if (CompilerOptions.ENABLED.equals(compilerOptions.get(CompilerOptions.OPTION_Release))
			&& compliance != null && !compliance.isEmpty()) {
			complianceVersion = Version.parse(compliance);
			options.put(Option.RELEASE, compliance);
		} else {
			String source = compilerOptions.get(CompilerOptions.OPTION_Source);
			if (CompilerOptions.VERSION_1_8.equals(source)) {
				source = "8";
			}
			if (source != null && !source.isBlank()) {
				complianceVersion = Version.parse(source);
				if (complianceVersion.compareToIgnoreOptional(Version.parse("8")) < 0) {
					ILog.get().warn("Unsupported source level: " + source + ", using 8 instead");
					options.put(Option.SOURCE, "8");
				} else {
					options.put(Option.SOURCE, source);
				}
			} else {
				complianceVersion = Runtime.version();
			}
			String target = compilerOptions.get(CompilerOptions.OPTION_TargetPlatform);
			if (CompilerOptions.VERSION_1_8.equals(target)) {
				target = "8";
			}
			if (target != null && !target.isEmpty()) {
				Version version = Version.parse(target);
				if (version.compareToIgnoreOptional(Version.parse("8")) < 0) {
					ILog.get().warn("Unsupported target level: " + target + ", using 8 instead");
					options.put(Option.TARGET, "8");
				} else {
					options.put(Option.TARGET, target);
				}
			}
		}
		if (CompilerOptions.ENABLED.equals(compilerOptions.get(CompilerOptions.OPTION_EnablePreviews)) &&
			Runtime.version().feature() == complianceVersion.feature()) {
			options.put(Option.PREVIEW, Boolean.toString(true));
		}
		options.put(Option.XLINT, Boolean.TRUE.toString()); // TODO refine according to compilerOptions
		options.put(Option.XLINT_CUSTOM, "all"); // TODO refine according to compilerOptions
		if (addExports != null && !addExports.isBlank()) {
			options.put(Option.ADD_EXPORTS, addExports);
		}
	}

	private static void configurePaths(JavaProject javaProject, Context context, JavacConfig compilerConfig,
	        File output) {
		JavacFileManager fileManager = (JavacFileManager)context.get(JavaFileManager.class);
		try {
			if (output != null) {
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(output));
			} else if (compilerConfig != null && !compilerConfig.sourceOutputMapping().isEmpty()) {
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, compilerConfig.sourceOutputMapping().values().stream().distinct().toList());
			} else if (javaProject.getProject() != null) {
				IResource member = javaProject.getProject().getParent().findMember(javaProject.getOutputLocation());
				if( member != null ) {
					File f = member.getLocation().toFile();
					fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(f));
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
				fileManager.setLocation(StandardLocation.SOURCE_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE));
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
				fileManager.setLocation(StandardLocation.CLASS_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() != IClasspathEntry.CPE_SOURCE));
			}
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}

	private static <T> boolean isEmpty(List<T> list) {
		return list == null || list.isEmpty();
	}

	private static Collection<File> classpathEntriesToFiles(JavaProject project, Predicate<IClasspathEntry> select) {
		try {
			LinkedHashSet<File> res = new LinkedHashSet<>();
			Queue<IClasspathEntry> toProcess = new LinkedList<>();
			toProcess.addAll(Arrays.asList(project.resolveClasspath(project.getExpandedClasspath())));
			while (!toProcess.isEmpty()) {
				IClasspathEntry current = toProcess.poll();
				if (current.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IResource referencedResource = project.getProject().getParent().findMember(current.getPath());
					if (referencedResource instanceof IProject referencedProject) {
						JavaProject referencedJavaProject = (JavaProject) JavaCore.create(referencedProject);
						if (referencedJavaProject.exists()) {
							for (IClasspathEntry transitiveEntry : referencedJavaProject.resolveClasspath(referencedJavaProject.getExpandedClasspath()) ) {
								if (transitiveEntry.isExported() || transitiveEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
									toProcess.add(transitiveEntry);
								}
							}
						}
					}
				} else if (select.test(current)) {
					IPath path = current.getPath();
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
			}
			return res;
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return List.of();
		}
	}

}
