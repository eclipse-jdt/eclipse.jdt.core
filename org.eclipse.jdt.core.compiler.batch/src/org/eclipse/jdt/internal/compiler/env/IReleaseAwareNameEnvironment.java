/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;

public interface IReleaseAwareNameEnvironment extends IModuleAwareNameEnvironment {

	/**
	 * Marker used to be specified in cases where a release is passed or used as an argument but no specific release is
	 * used..
	 */
	static final int NO_RELEASE = -1;

	/**
	 * The first java release that supported multi release jars
	 */
	static final int FIRST_MULTI_RELEASE = 9;

	NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName, int release);

	NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName, int release);

	default NameEnvironmentAnswer findType(char[][] compoundTypeName, int release) {
		return findType(compoundTypeName, ModuleBinding.ANY, release);
	}

	default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, int release) {
		return findType(typeName, packageName, ModuleBinding.ANY, release);
	}

	@Override
	default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		return findType(typeName, packageName, NO_RELEASE);
	}

	@Override
	default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
		return findType(typeName, packageName, moduleName, NO_RELEASE);
	}

	@Override
	default NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
		return findType(compoundName, moduleName, NO_RELEASE);
	}
}
