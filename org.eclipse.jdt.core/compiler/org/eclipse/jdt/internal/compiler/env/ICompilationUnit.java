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
 * This interface denotes a compilation unit, providing its name and content.
 * 
 * <p>
 * Note: This internal interface has been implemented illegally by the
 * org.apache.jasper.glassfish bundle from Orbit, see
 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=500211">bug 500211</a>.
 * Avoid changing the API or supply default methods to avoid breaking the Eclipse Help system.
 * </p>
 */
public interface ICompilationUnit extends IDependent {
/**
 * Answer the contents of the compilation unit.
 *
 * In normal use, the contents are requested twice.
 * Once during the initial lite parsing step, then again for the
 * more detailed parsing step.
 * Implementors must never return null - return an empty char[] instead,
 * CharOperation.NO_CHAR being the candidate of choice.
 */
char[] getContents();
/**
 * Answer the name of the top level public type.
 * For example, {Hashtable}.
 */
char[] getMainTypeName();
/**
 * Answer the name of the package according to the directory structure
 * or null if package consistency checks should be ignored.
 * For example, {java, lang}.
 */
char[][] getPackageName();
/**
* Answer if optional problems should be ignored for this compilation unit.
* Implementors should return <code>false</code> if there is no preference.
*/
default boolean ignoreOptionalProblems() {
	return false;
}
/**
 * Returns the name of the module that this compilation unit belongs to.
 *
 * @return the name of the module as a char array.
 */
default char[] module() {
	return null;
}
}
