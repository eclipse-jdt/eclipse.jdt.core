package org.eclipse.jdt.internal.codeassist;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.codeassist.*;

/**
 * This is the internal requestor passed to the searchable name environment
 * so as to process the multiple search results as they are discovered.
 *
 * It is used to allow the code assist engine to add some more information
 * to the raw name environment results before answering them to the UI.
 */
public interface ISearchRequestor {
/**
 * One result of the search consists of a new class.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
public void acceptClass(char[] packageName, char[] typeName, int modifiers);
/**
 * One result of the search consists of a new interface.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.I".
 *    The default package is represented by an empty array.
 */
public void acceptInterface(char[] packageName, char[] typeName, int modifiers);
/**
 * One result of the search consists of a new package.
 *
 * NOTE - All package names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    The default package is represented by an empty array.
 */
public void acceptPackage(char[] packageName);
/**
 * One result of the search consists of a new type.
 *
 * NOTE - All package and type names are presented in their readable form:
 *    Package names are in the form "a.b.c".
 *    Nested type names are in the qualified form "A.M".
 *    The default package is represented by an empty array.
 */
public void acceptType(char[] packageName, char[] typeName);
}
