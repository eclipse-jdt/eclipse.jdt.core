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

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * This class encapsulates the standard compiler options that can be
 * used to compile Java files. It provides methods to set and retrieve
 * various compiler options, including source paths, class paths,
 * output directories, annotation processing options, and other compiler
 * arguments.
 * <p>
 * Clients typically use this class when opting for an alternative compiler
 * like Javac to compile Java files.
 *
 * @since 3.38
 */
public final class CompilerConfiguration {
    private final List<IContainer> sourcepaths;
    private final List<IContainer> moduleSourcepaths;
    private final List<URI> classpaths;
    private final List<URI> modulepaths;
    private final List<URI> annotationProcessorPaths;
    private final List<IContainer> generatedSourcePaths;
    private final Map<IContainer, IContainer> sourceOutputMapping;
    private final CompilerOptions compilerOptions;

    /**
     *
     */
    public CompilerConfiguration(
            /**
             * List of file paths where the compiler can find source files.
             */
            List<IContainer> sourcepaths,
            /**
             * List of file paths where the compiler can find source files for modules.
             */
            List<IContainer> moduleSourcepaths,
            /**
             * List of file paths where the compiler can find user class files and annotation processors.
             */
            List<URI> classpaths,
            /**
             * List of file paths where the compiler can find modules.
             */
            List<URI> modulepaths,
            /**
             * Location to search for annotation processors.
             */
            List<URI> annotationProcessorPaths,
            /**
             * Locations to place generated source files.
             */
            List<IContainer> generatedSourcePaths,
            /**
             * The mapping of source files to output directories.
             */
            Map<IContainer, IContainer> sourceOutputMapping,
            /**
             * The compiler options used to control the compilation behavior.
             * See {@link org.eclipse.jdt.internal.compiler.impl.CompilerOptions} for a list of available options.
             */
            CompilerOptions compilerOptions) {
        this.sourcepaths = sourcepaths;
        this.moduleSourcepaths = moduleSourcepaths;
        this.classpaths = classpaths;
        this.modulepaths = modulepaths;
        this.annotationProcessorPaths = annotationProcessorPaths;
        this.generatedSourcePaths = generatedSourcePaths;
        this.sourceOutputMapping = sourceOutputMapping;
        this.compilerOptions = compilerOptions;
    }

    public List<IContainer> sourcepaths() {
        return sourcepaths;
    }

    public List<IContainer> moduleSourcepaths() {
        return moduleSourcepaths;
    }

    public List<URI> classpaths() {
        return classpaths;
    }

    public List<URI> modulepaths() {
        return modulepaths;
    }

    public List<URI> annotationProcessorPaths() {
        return annotationProcessorPaths;
    }

    public List<IContainer> generatedSourcePaths() {
        return generatedSourcePaths;
    }

    public Map<IContainer, IContainer> sourceOutputMapping() {
        return sourceOutputMapping;
    }

    public CompilerOptions compilerOptions() {
        return compilerOptions;
    }

    @java.lang.Override
    public boolean equals(java.lang.Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CompilerConfiguration) obj;
        return java.util.Objects.equals(this.sourcepaths, that.sourcepaths) &&
               java.util.Objects.equals(this.moduleSourcepaths, that.moduleSourcepaths) &&
               java.util.Objects.equals(this.classpaths, that.classpaths) &&
               java.util.Objects.equals(this.modulepaths, that.modulepaths) &&
               java.util.Objects.equals(this.annotationProcessorPaths, that.annotationProcessorPaths) &&
               java.util.Objects.equals(this.generatedSourcePaths, that.generatedSourcePaths) &&
               java.util.Objects.equals(this.sourceOutputMapping, that.sourceOutputMapping) &&
               java.util.Objects.equals(this.compilerOptions, that.compilerOptions);
    }

    @java.lang.Override
    public int hashCode() {
        return java.util.Objects.hash(sourcepaths, moduleSourcepaths, classpaths, modulepaths, annotationProcessorPaths, generatedSourcePaths, sourceOutputMapping, compilerOptions);
    }

    @java.lang.Override
    public String toString() {
        return "CompilerConfiguration[" +
               "sourcepaths=" + sourcepaths + ", " +
               "moduleSourcepaths=" + moduleSourcepaths + ", " +
               "classpaths=" + classpaths + ", " +
               "modulepaths=" + modulepaths + ", " +
               "annotationProcessorPaths=" + annotationProcessorPaths + ", " +
               "generatedSourcePaths=" + generatedSourcePaths + ", " +
               "sourceOutputMapping=" + sourceOutputMapping + ", " +
               "compilerOptions=" + compilerOptions + ']';
    }

}
