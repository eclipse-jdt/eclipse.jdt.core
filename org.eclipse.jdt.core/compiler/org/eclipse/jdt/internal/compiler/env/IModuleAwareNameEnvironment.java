/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;

/**
 * A module aware name environment
 * FIXME(SHMOD): consider folding this interface into INameEnvironment and extend existing methods rather then adding new ones. 
 *
 */
public interface IModuleAwareNameEnvironment extends INameEnvironment {
	default NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		return findType(compoundTypeName, ModuleBinding.ANY);
	}
	default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		return findType(typeName, packageName, ModuleBinding.ANY);
	}
	default boolean isPackage(char[][] parentPackageName, char[] packageName) {
		return getModulesDeclaringPackage(parentPackageName, packageName, ModuleBinding.ANY) != null;
	}

	NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName);
	/** Answer a type identified by the given names. A {@code null} moduleName signals the unnamed module. */
	NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName);
	char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] name, char[] moduleName);

	/** Get the module with the given name, which must denote a named module. */
	IModule getModule(char[] moduleName);
	IModule[] getAllAutomaticModules();

	default void applyModuleUpdates(IUpdatableModule module, IUpdatableModule.UpdateKind kind) { /* default: do nothing */ }
}
