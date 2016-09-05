/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 * The name environment provides a callback API that the compiler
 * can use to look up types, compilation units, and packages in the
 * current environment. The name environment is passed to the compiler
 * on creation.
 * 
 * <p>
 * Note: This internal interface has been implemented illegally by the
 * org.apache.jasper.glassfish bundle from Orbit, see
 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=500211">bug 500211</a>.
 * Avoid changing the API or supply default methods to avoid breaking the Eclipse Help system.
 * </p>
 */
public interface INameEnvironment {
/**
 * Find a type with the given compound name.
 * Answer the binary form of the type if it is known to be consistent.
 * Otherwise, answer the compilation unit which defines the type
 * or null if the type does not exist.
 * Types in the default package are specified as {{typeName}}.
 *
 * It is unknown whether the package containing the type actually exists.
 *
 * NOTE: This method can be used to find a member type using its
 * internal name A$B, but the source file for A is answered if the binary
 * file is inconsistent.
 * 
 * NOTE: Implementers must reimplement this method!
 */
default NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] client) {
	return findType(compoundTypeName);
}
/**
 * @deprecated don't override this method any more, but override {@link #findType(char[][], char[])}
 */
@Deprecated
default NameEnvironmentAnswer findType(char[][] compoundTypeName) {
	return findType(compoundTypeName, null /*JRTUtil.JAVA_BASE_CHAR*/);
}

/**
 * Find a type named <typeName> in the package <packageName> if it is readable
 * to the given module.
 * Answer the binary form of the type if it is known to be consistent.
 * Otherwise, answer the compilation unit which defines the type
 * or null if the type does not exist.
 * The default package is indicated by char[0][].
 *
 * It is known that the package containing the type exists.
 *
 * NOTE: This method can be used to find a member type using its
 * internal name A$B, but the source file for A is answered if the binary
 * file is inconsistent.
 * 
 * NOTE: Implementers must reimplement this method!
 */
default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] client) {
	return findType(typeName, packageName);
}
/**
 * @deprecated don't override this method any more, but override {@link #findType(char[], char[][], char[])}
 */
@Deprecated
default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	return findType(typeName, packageName, null /*JRTUtil.JAVA_BASE_CHAR*/);
}

/**
 * Answer whether packageName is the name of a known subpackage inside
 * the package parentPackageName if it is readable
 * to the given module. A top level package is found relative to null.
 * The default package is always assumed to exist.
 *
 * For example:
 *      isPackage({{java}, {awt}}, {event});
 *      isPackage(null, {java});
 * 
 * NOTE: Implementers must reimplement this method!
 */

default boolean isPackage(char[][] parentPackageName, char[] packageName, char[] client) {
	return isPackage(parentPackageName, packageName);
}
/**
 * @deprecated don't override this method any more, but override {@link #isPackage(char[][], char[], char[])}
 */
@Deprecated
default boolean isPackage(char[][] parentPackageName, char[] packageName) {
	return isPackage(parentPackageName, packageName, null /*JRTUtil.JAVA_BASE_CHAR*/);
}

/**
 * Accepts (and preserves if necessary) the given module and the corresponding
 * module location. This helps the name environment to later whether or not
 * a particular module location should be queried for types in a specific module.
 *
 * NOTE: Implementers must reimplement this method!
 * 
 * @param module
 * @param location
 */
default public void acceptModule(IModule module, IModuleLocation location) {
	// empty default implementation for compatibility
}
/**
 * NOTE: Implementers must reimplement this method!
 */
default public boolean isPackageVisible(char[] pack, char[] source, char[] client) { return true; }
/**
 * NOTE: Implementers must reimplement this method!
 */
default public IModule getModule(char[] name) {
	return null;
}
/**
 * NOTE: Implementers must reimplement this method!
 */
default public IModule getModule(IModuleLocation location) {
	return null;
}

/**
 * This method cleans the environment. It is responsible for releasing the memory
 * and freeing resources. Passed that point, the name environment is no longer usable.
 *
 * A name environment can have a long life cycle, therefore it is the responsibility of
 * the code which created it to decide when it is a good time to clean it up.
 */
void cleanup();

}
