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

package org.eclipse.jdt.core.compiler;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * This class encapsulates the standard compiler options that can be
 * used to compile Java files. It provides methods to set and retrieve
 * various compiler options, including source paths, class paths,
 * output directories, annotation processing options, and other compiler
 * arguments.
 *
 * Clients typically use this class when opting for an alternative compiler
 * like Javac to compile Java files.
 *
 * @since 3.38
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CompilerConfiguration {
	List<String> sourcepaths;
	List<String> moduleSourcepaths;
	List<String> classpaths;
	List<String> modulepaths;
	Map<File, File> sourceOutputMapping;
	CompilerOptions options;
	// Location to search for annotation processors.
	List<String> annotationProcessorPaths;
	// Locations to place generated source files.
	List<String> generatedSourcePaths;

	/**
	 * Returns where to find user class files and annotation processors.
	 */
	public List<String> getClasspaths() {
		return this.classpaths;
	}

	/**
	 * Sets where to find user class files and annotation processors.
	 * @param classpaths the list of class paths
	 */
	public void setClasspaths(List<String> classpaths) {
		this.classpaths = classpaths;
	}

	/**
	 * Returns where to find modules.
	 */
	public List<String> getModulepaths() {
		return this.modulepaths;
	}

	/**
	 * Sets where to find modules.
	 * @param modulepaths the list of module paths
	 */
	public void setModulepaths(List<String> modulepaths) {
		this.modulepaths = modulepaths;
	}

	/**
	 * Returns the source code path to search for class or interface definitions.
	 */
	public List<String> getSourcepaths() {
		return this.sourcepaths;
	}

	/**
	 * Sets the source code path to search for class or interface definitions.
	 * @param sourcepaths the list of source paths
	 */
	public void setSourcepaths(List<String> sourcepaths) {
		this.sourcepaths = sourcepaths;
	}

	/**
	 * Returns where to find source files when compiling modules.
	 */
	public List<String> getModuleSourcepaths() {
		return this.moduleSourcepaths;
	}

	/**
	 * Sets where to find source files when compiling modules.
	 * @param moduleSourcepaths the list of module source paths
	 */
	public void setModuleSourcepaths(List<String> moduleSourcepaths) {
		this.moduleSourcepaths = moduleSourcepaths;
	}

	/**
	 * Returns the mapping of source files to output directories.
	 */
	public Map<File, File> getSourceOutputMapping() {
		return this.sourceOutputMapping;
	}

	/**
	 * Sets the mapping of source files to output directories.
	 * @param sourceOutputMapping the source output mapping
	 */
	public void setSourceOutputMapping(Map<File, File> sourceOutputMapping) {
		this.sourceOutputMapping = sourceOutputMapping;
	}

	/**
	 * Returns the compiler options to be used for compilation.
	 *
	 * @see {@link org.eclipse.jdt.internal.compiler.impl.CompilerOptions} for a list of available options.
	 */
	public CompilerOptions getOptions() {
		return this.options;
	}

	/**
	 * Sets the compiler options to be used for compilation.
	 * @param options the compiler options
	 * @see {@link org.eclipse.jdt.internal.compiler.impl.CompilerOptions} for a list of available options.
	 */
	public void setOptions(CompilerOptions options) {
		this.options = options;
	}

	/**
	 * Returns the locations to search for annotation processors.
	 */
	public List<String> getAnnotationProcessorPaths() {
		return this.annotationProcessorPaths;
	}

	/**
	 * Sets the locations to search for annotation processors.
	 * @param annotationProcessorPaths the list of annotation processor paths
	 */
	public void setAnnotationProcessorPaths(List<String> annotationProcessorPaths) {
		this.annotationProcessorPaths = annotationProcessorPaths;
	}

	/**
	 * Returns the locations to place generated source files.
	 */
	public List<String> getGeneratedSourcePaths() {
		return this.generatedSourcePaths;
	}

	/**
	 * Sets the locations to place generated source files.
	 * @param generatedSourcePaths the list of generated source paths
	 */
	public void setGeneratedSourcePaths(List<String> generatedSourcePaths) {
		this.generatedSourcePaths = generatedSourcePaths;
	}
}
