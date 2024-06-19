/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.CompilerConfiguration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public record JavacConfig(
	/**
	 * List of file paths where the compiler can find source files.
	 */
	List<String> sourcepaths,
	/**
	 * List of file paths where the compiler can find source files for modules.
	 */
	List<String> moduleSourcepaths,
	/**
	 * List of file paths where the compiler can find user class files and annotation processors.
	 */
	List<String> classpaths,
	/**
	 * List of file paths where the compiler can find modules.
	 */
	List<String> modulepaths,
	/**
	 * Location to search for annotation processors.
	 */
	List<String> annotationProcessorPaths,
	/**
	 * Locations to place generated source files.
	 */
	List<String> generatedSourcePaths,
	/**
	 * The mapping of source files to output directories.
	 */
	Map<File, File> sourceOutputMapping,
	/**
	 * The compiler options used to control the compilation behavior.
	 * See {@link org.eclipse.jdt.internal.compiler.impl.CompilerOptions} for a list of available options.
	 */
	CompilerOptions compilerOptions) {

	static JavacConfig createFrom(CompilerConfiguration config) {
		return new JavacConfig(
			config.sourcepaths().stream().map(IContainer::getRawLocation).filter(path -> path != null).map(IPath::toOSString).collect(Collectors.toList()),
			config.moduleSourcepaths().stream().map(IContainer::getRawLocation).filter(path -> path != null).map(IPath::toOSString).collect(Collectors.toList()),
			config.classpaths().stream().map(URI::getPath).collect(Collectors.toList()),
			config.modulepaths().stream().map(URI::getPath).collect(Collectors.toList()),
			config.annotationProcessorPaths().stream().map(URI::getPath).collect(Collectors.toList()),
			config.generatedSourcePaths().stream().map(IContainer::getRawLocation).filter(path -> path != null).map(IPath::toOSString).collect(Collectors.toList()),
			config.sourceOutputMapping().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getRawLocation().toFile(), e -> e.getValue().getRawLocation().toFile())),
			config.compilerOptions());
	}
}
