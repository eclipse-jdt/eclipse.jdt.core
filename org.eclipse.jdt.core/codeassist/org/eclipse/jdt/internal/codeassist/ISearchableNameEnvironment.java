package org.eclipse.jdt.internal.codeassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * This interface defines the API that may be used to implement any
 * search-based tool (such as a CodeAssist, a Finder, ...)
 * It is mainly used to hide from the search tool the implementation
 * of the underlying environment and its constructions.
 */

import org.eclipse.jdt.internal.compiler.env.*;

public interface ISearchableNameEnvironment extends INameEnvironment {
/**
 * Find the packages that start with the given prefix.
 * A valid prefix is a qualified name separated by periods
 * (ex. com.ibm.com or java.util).
 * The packages found are passed to:
 *    ISearchRequestor.acceptPackage(char[][] packageName)
 */

void findPackages(char[] prefix, ISearchRequestor requestor);
/**
 * Find the top-level types (classes and interfaces) that are defined
 * in the current environment and whose name starts with the
 * given prefix. The prefix is a qualified name separated by periods
 * or a simple name (ex. java.util.V or V).
 *
 * The types found are passed to one of the following methods (if additional
 * information is known about the types):
 *    ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
 *    ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int modifiers)
 *    ISearchRequestor.acceptInterface(char[][] packageName, char[] typeName, int modifiers)
 *
 * This method can not be used to find member types... member
 * types are found relative to their enclosing type.
 */

void findTypes(char[] prefix, ISearchRequestor requestor);
}
