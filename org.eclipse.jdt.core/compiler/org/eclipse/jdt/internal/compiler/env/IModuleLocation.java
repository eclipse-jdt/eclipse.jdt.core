/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
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

/**
 * Represents a location within the INameEnvironment that may contain a module. A module
 * location could contain a module either in the form of module-info.java or module-info.class.
 */
public interface IModuleLocation {
	/**
	 * Find class with the given type name and qualified package name from the given module. Clients can also use
	 * servesModule() before invoking this for performance implications.
	 *
	 * @param typeName
	 * @param qualifiedPackageName
	 * @param qualifiedBinaryFileName
	 * @param mod
	 * @return a NameEnvironmentAnswer
	 */
	public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName, IModule mod);
	public NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, IModule mod);
	public boolean isPackage(String qualifiedPackageName);
	public IModule getModule(char[] moduleName);
	/**
	 * Tells the client whether or not this module location serves the given module.
	 * @param module
	 * @return whether the given module is found in this module location.
	 */
	public boolean servesModule(IModule module);
}
