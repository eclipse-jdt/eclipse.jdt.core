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

package org.eclipse.jdt.internal.compiler;

import java.io.File;
import java.util.List;
import java.util.Map;

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
public record CompilerConfiguration(
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
		Map<String, String> compilerOptions) {
}
