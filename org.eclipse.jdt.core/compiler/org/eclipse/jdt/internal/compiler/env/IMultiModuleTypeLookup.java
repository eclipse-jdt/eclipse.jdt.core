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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Functional interface for looking up types in multiple modules
 * 
 */
public interface IMultiModuleTypeLookup extends ITypeLookup {
	default NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
		return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false, Optional.empty());
	}
	default public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly, Optional.empty());
	}
	default NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, char[] module) {
		return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false, Optional.of(Collections.singletonList(module)));
	}
	/**
	 * Find the class named typeName with binary file name qualifiedBinaryFileName in the package whose full name is qualifiedPackageName
	 * 
	 * @param typeName
	 * @param qualifiedPackageName
	 * @param qualifiedBinaryFileName
	 * @param asBinaryOnly Look for only binary files
	 * @param modules The modules to search in.
	 * 
	 * @return NameEnvironmentAnswer if found, otherwise null
	 * 
	 * @throws NullPointerException the collection is null or contains nulls
	 */
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, Optional<Collection<char[]>> modules);
}