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

public class CompilerConfiguration {
	List<String> sourcepaths;
	List<String> moduleSourcepaths;
	List<String> classpaths;
	List<String> modulepaths;
	Map<File, File> sourceOutputMapping;
	CompilerOptions options;

	public List<String> getClasspaths() {
		return this.classpaths;
	}

	public void setClasspaths(List<String> classpaths) {
		this.classpaths = classpaths;
	}

	public List<String> getModulepaths() {
		return this.modulepaths;
	}

	public void setModulepaths(List<String> modulepaths) {
		this.modulepaths = modulepaths;
	}

	public List<String> getSourcepaths() {
		return this.sourcepaths;
	}

	public void setSourcepaths(List<String> sourcepaths) {
		this.sourcepaths = sourcepaths;
	}

	public List<String> getModuleSourcepaths() {
		return this.moduleSourcepaths;
	}

	public void setModuleSourcepaths(List<String> moduleSourcepaths) {
		this.moduleSourcepaths = moduleSourcepaths;
	}

	public Map<File, File> getSourceOutputMapping() {
		return this.sourceOutputMapping;
	}

	public void setSourceOutputMapping(Map<File, File> sourceOutputMapping) {
		this.sourceOutputMapping = sourceOutputMapping;
	}

	public CompilerOptions getOptions() {
		return this.options;
	}

	public void setOptions(CompilerOptions options) {
		this.options = options;
	}
}
