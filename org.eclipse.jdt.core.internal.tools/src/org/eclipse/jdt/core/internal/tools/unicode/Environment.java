/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.internal.tools.unicode;

public abstract class Environment {
	/**
	 * Returns <code>true</code> if the given category is a valid one for the current environment, <code>false</code> otherwise.
	 * @param value the given category value
	 * @return <code>true</code> if the given category is a valid one for the current environment, <code>false</code> otherwise.
	 */
	public abstract boolean hasCategory(String value);

	/**
	 * Returns the resource file name for the given environment.
	 * @return the resource file name for the given environment.
	 */
	public abstract String getResourceFileName();
}
