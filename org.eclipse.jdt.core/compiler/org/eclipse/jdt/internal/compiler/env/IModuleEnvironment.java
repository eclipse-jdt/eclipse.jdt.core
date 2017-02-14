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

/**
 * The module environment provides a callback API that the compiler
 * can use to look up types, compilation units, and packages in the 
 * context of one or modules. A lookup environment for a module can
 * be obtained from the IModulePathEntry that contributes the module
 * 
 * @see IModulePathEntry#getLookupEnvironmentFor(IModule)
 */
public interface IModuleEnvironment {
	static String MODULE_INFO = "module-info"; //$NON-NLS-1$
	static String MODULE_INFO_JAVA = "module-info.java"; //$NON-NLS-1$
	static String MODULE_INFO_CLASS = "module-info.class"; //$NON-NLS-1$

	/**
	 * A way to lookup types in a module
	 * 
	 * @return A TypeLookup that can look up types in a module
	 */
	ITypeLookup typeLookup();
	
	/**
	 * A way to lookup packages in a module
	 * 
	 * @return A PackageLookup that can look up packages in a module
	 */
	IPackageLookup packageLookup();

	/**
	  * Answer whether qualifiedName is the name of a known package in the
	  * module this environment represents
	  * The default package is always assumed to exist. 
	  * 
	  * @param qualifiedName
	 */
	default boolean isPackage(String qualifiedName) {
		return packageLookup().isPackage(qualifiedName);
	}
	
	/**
	 * Find class with the given type name and qualified package name from the module this environment represents
	 *
	 * @param typeName
	 * @param qualifiedPackageName
	 * @param qualifiedBinaryFileName
	 * @param asBinaryOnly
	 * 
	 * @return a NameEnvironmentAnswer or null if no such type exists
	 */
//	default NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
//		return typeLookup().findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly);
//	}
//	default NameEnvironmentAnswer findClass(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly, char[] mod) {
//		if (servesModule(mod))
//			return typeLookup().findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly);
//		return null;
//	}

}
