/*******************************************************************************
 * Copyright (c) 2023, Red Hat, Inc. and others.
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaProject;

import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class JavacUtils {

	public static void configureJavacContext(Context context, Map<String, String> compilerOptions, IJavaProject javaProject) {
		Options options = Options.instance(context);
		options.put(Option.XLINT, Boolean.TRUE.toString()); // TODO refine according to compilerOptions
		if (Boolean.parseBoolean(compilerOptions.get(CompilerOptions.OPTION_EnablePreviews))) {
			options.put(Option.PREVIEW, Boolean.toString(true));
		}
		String release = compilerOptions.get(CompilerOptions.OPTION_Release);
		if (release != null) {
			options.put(Option.RELEASE, release);
		}
		options.put(Option.XLINT_CUSTOM, "all"); // TODO refine according to compilerOptions
		// TODO populate more from compilerOptions and/or project settings
		JavacFileManager.preRegister(context);
		if (javaProject instanceof JavaProject internal) {
			configurePaths(internal, context);
		}
		Todo.instance(context); // initialize early
		com.sun.tools.javac.main.JavaCompiler javac = new com.sun.tools.javac.main.JavaCompiler(context);
		javac.keepComments = true;
		javac.genEndPos = true;
		javac.lineDebugInfo = true;
	}

	private static void configurePaths(JavaProject javaProject, Context context) {
		JavacFileManager fileManager = (JavacFileManager)context.get(JavaFileManager.class);
		try {
			IResource member = javaProject.getProject().getParent().findMember(javaProject.getOutputLocation());
			if( member != null ) {
				File f = member.getLocation().toFile();
				fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(f));
			}
			fileManager.setLocation(StandardLocation.SOURCE_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE));
			fileManager.setLocation(StandardLocation.CLASS_PATH, classpathEntriesToFiles(javaProject, entry -> entry.getEntryKind() != IClasspathEntry.CPE_SOURCE));
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
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
