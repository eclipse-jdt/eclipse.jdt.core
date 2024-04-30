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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilerConfiguration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class JavacUtils {

	public static void configureJavacContext(Context context, Map<String, String> compilerOptions, IJavaProject javaProject) {
		configureJavacContext(context, compilerOptions, javaProject, null);
	}

	public static void configureJavacContext(Context context, CompilerConfiguration compilerConfig, IJavaProject javaProject) {
		configureJavacContext(context, compilerConfig.getOptions().getMap(), javaProject, compilerConfig);
	}

	private static void configureJavacContext(Context context, Map<String, String> compilerOptions, IJavaProject javaProject, CompilerConfiguration compilerConfig) {
		configureOptions(context, compilerOptions);
		// TODO populate more from compilerOptions and/or project settings
		if (context.get(JavaFileManager.class) == null) {
			JavacFileManager.preRegister(context);
		}
		if (javaProject instanceof JavaProject internal) {
			configurePaths(internal, context, compilerConfig);
		}
	}

	private static void configureOptions(Context context, Map<String, String> compilerOptions) {
		Options options = Options.instance(context);
		options.put(Option.XLINT, Boolean.TRUE.toString()); // TODO refine according to compilerOptions
		options.put("allowStringFolding", Boolean.FALSE.toString());
		if (CompilerOptions.ENABLED.equals(compilerOptions.get(CompilerOptions.OPTION_EnablePreviews))) {
			options.put(Option.PREVIEW, Boolean.toString(true));
		}
		String release = compilerOptions.get(CompilerOptions.OPTION_Release);
		String compliance = compilerOptions.get(CompilerOptions.OPTION_Compliance);
		if (CompilerOptions.ENABLED.equals(release) && compliance != null && !compliance.isEmpty()) {
			options.put(Option.RELEASE, compliance);
		} else {
			String source = compilerOptions.get(CompilerOptions.OPTION_Source);
			if (source != null && !source.isEmpty()) {
				if (source.indexOf("1.") != -1 && source.indexOf("1.8") == -1 || source.indexOf(".") == -1 && Integer.parseInt(source) < 8) {
					ILog.get().warn("Unsupported source level: " + source + ", using 1.8 instead");
					options.put(Option.SOURCE, "1.8");
				} else {
					options.put(Option.SOURCE, source);
				}
			}
			String target = compilerOptions.get(CompilerOptions.OPTION_TargetPlatform);
			if (target != null && !target.isEmpty()) {
				if (target.indexOf("1.") != -1 && target.indexOf("1.8") == -1 || target.indexOf(".") == -1 && Integer.parseInt(target) < 8) {
					ILog.get().warn("Unsupported target level: " + target + ", using 1.8 instead");
					options.put(Option.TARGET, "1.8");
				} else {
					options.put(Option.TARGET, target);
				}
			}
		}
		options.put(Option.XLINT_CUSTOM, "all"); // TODO refine according to compilerOptions
	}

	private static void configurePaths(JavaProject javaProject, Context context, CompilerConfiguration compilerConfig) {
		JavacFileManager fileManager = (JavacFileManager)context.get(JavaFileManager.class);
		try {
			if (compilerConfig != null && !compilerConfig.getSourceOutputMapping().isEmpty()) {
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, compilerConfig.getSourceOutputMapping().values().stream().distinct().toList());
			} else if (javaProject.getProject() != null) {
				IResource member = javaProject.getProject().getParent().findMember(javaProject.getOutputLocation());
				if( member != null ) {
					File f = member.getLocation().toFile();
					fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(f));
				}
			}

			boolean sourcePathEnabled = false;
			if (compilerConfig != null && !isEmpty(compilerConfig.getSourcepaths())) {
				fileManager.setLocation(StandardLocation.SOURCE_PATH,
					compilerConfig.getSourcepaths()
						.stream()
						.map(File::new)
						.toList());
				sourcePathEnabled = true;
			}
			if (compilerConfig != null && !isEmpty(compilerConfig.getModuleSourcepaths())) {
				fileManager.setLocation(StandardLocation.MODULE_SOURCE_PATH,
					compilerConfig.getModuleSourcepaths()
						.stream()
						.map(File::new)
						.toList());
				sourcePathEnabled = true;
			}
			if (!sourcePathEnabled) {
				fileManager.setLocation(StandardLocation.SOURCE_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE));
			}

			boolean classpathEnabled = false;
			if (compilerConfig != null && !isEmpty(compilerConfig.getClasspaths())) {
				fileManager.setLocation(StandardLocation.CLASS_PATH,
					compilerConfig.getClasspaths()
						.stream()
						.map(File::new)
						.toList());
				classpathEnabled = true;
			}
			if (compilerConfig != null && !isEmpty(compilerConfig.getModulepaths())) {
				fileManager.setLocation(StandardLocation.MODULE_PATH,
					compilerConfig.getModulepaths()
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

	private static List<File> classpathEntriesToFiles(JavaProject project, Predicate<IClasspathEntry> select) {
		try {
			IClasspathEntry[] selected = Arrays.stream(project.getRawClasspath())
				.filter(select)
				.toArray(IClasspathEntry[]::new);
			return Arrays.stream(project.resolveClasspath(selected))
				.map(IClasspathEntry::getPath)
				.map(path -> {
					File asFile = path.toFile();
					if (asFile.exists()) {
						return asFile;
					}
					IResource asResource = project.getProject().getParent().findMember(path);
					if (asResource != null) {
						return asResource.getLocation().toFile();
					}
					return null;
				}).filter(Objects::nonNull)
				.filter(File::exists)
				.toList();
		} catch (JavaModelException ex) {
			ILog.get().error(ex.getMessage(), ex);
			return List.of();
		}
	}

}
