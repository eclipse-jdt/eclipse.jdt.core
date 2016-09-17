/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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

import java.util.Optional;

public interface IMultiModulePackageLookup extends IPackageLookup {

	/**
	 * Answers whether qualifiedPackageName is the name of a known package in the module named module
	 * The unnamed package is always assumed to exist
	 * 
	 * @param qualifiedName
	 * 
	 * @throws NullPointerException if module is null
	 */
	default boolean isPackage(String qualifiedName) {
		return isPackage(qualifiedName, Optional.empty());
	}
	/**
	 * Answers whether qualifiedPackageName is the name of a known package in the module named module
	 * The unnamed package is always assumed to exist
	 * 
	 * @param qualifiedName
	 * @param module
	 * 
	 * @throws NullPointerException if module is null
	 */
	default boolean isPackage(String qualifiedName, char[] module) {
		return isPackage(qualifiedName, Optional.of(module));
	}
	/**
	 * Answers whether qualifiedPackageName is the name of a known package in the module named moduleName
	 * The unnamed package is always assumed to exist
	 * 
	 * @param qualifiedName
	 * @param moduleName
	 */
	boolean isPackage(String qualifiedName, Optional<char[]> moduleName);
}
