package org.eclipse.jdt.internal.compiler.env;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/**
 * The name environment provides a callback API that the compiler
 * can use to look up types, compilation units, and packages in the
 * current environment.  The name environment is passed to the compiler
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

NameEnvironmentAnswer findType(char[][] compoundTypeName);
/**
 * Find a type named <typeName> in the package <packageName>.
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

NameEnvironmentAnswer findType(char[] typeName, char[][] packageName);
/**
 * Answer whether packageName is the name of a known subpackage inside
 * the package parentPackageName. A top level package is found relative to null.
 * The default package is always assumed to exist.
 *
 * For example:
 *      isPackage({{java}, {awt}}, {event});
 *      isPackage(null, {java});
 */

boolean isPackage(char[][] parentPackageName, char[] packageName);
}
