/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

/**
 * The name environment provides a callback API that the compiler
 * can use to look up types, compilation units, and packages in the
 * current environment.  The name environment is passed to the compiler
 * on creation.
 * 
 * This name environment can be canceled using the monitor passed as an argument to
 * {@link #setMonitor(IProgressMonitor)}.
 * 
 * @since 3.6
 */
public interface INameEnvironmentWithProgress extends INameEnvironment {

	/**
	 * Set the monitor for the given name environment. In order to be able to cancel this name environment calls,
	 * a non-null monitor should be given. 
	 * 
	 * @param monitor the given monitor
	 */
	void setMonitor(IProgressMonitor monitor);

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
	 *
	 * The flag <searchWithSecondaryTypes> can be used to switch on/off the search for secondary types.
	 * This is useful because the search for secondary types may by very expensive regarding the performance
	 * and in many cases it isn't necessary to search for secondary types.
	 *
	 * @param typeName type to find
	 * @param packageName package of the searched type
	 * @param searchWithSecondaryTypes flag to switch on/off the search for secondary types
	 * @return {@link NameEnvironmentAnswer}
	 */
	NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, boolean searchWithSecondaryTypes);
}