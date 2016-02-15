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
 */

NameEnvironmentAnswer findType(char[][] compoundTypeName, char[] client);
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
 */

NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] client);
/**
 * Answer whether packageName is the name of a known subpackage inside
 * the package parentPackageName if it is readable
 * to the given module. A top level package is found relative to null.
 * The default package is always assumed to exist.
 *
 * For example:
 *      isPackage({{java}, {awt}}, {event});
 *      isPackage(null, {java});
 */

boolean isPackage(char[][] parentPackageName, char[] packageName, char[] client);

/**
 * Accepts (and preserves if necessary) the given module and the corresponding
 * module location. This helps the name environment to later whether or not
 * a particular module location should be queried for types in a specific module.
 *
 * @param module
 * @param location
 */
public void acceptModule(IModule module, IModuleLocation location);
public boolean isPackageVisible(char[] pack, char[] source, char[] client);
public IModule getModule(char[] name);
public IModule getModule(IModuleLocation location);

/**
 * This method cleans the environment. It is responsible for releasing the memory
 * and freeing resources. Passed that point, the name environment is no longer usable.
 *
 * A name environment can have a long life cycle, therefore it is the responsibility of
 * the code which created it to decide when it is a good time to clean it up.
 */
void cleanup();

}
