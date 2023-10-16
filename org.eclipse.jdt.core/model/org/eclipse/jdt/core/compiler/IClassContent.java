/*******************************************************************************
 * Copyright (c) 2023 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.compiler;

import org.eclipse.core.runtime.CoreException;

/**
 * Encapsulates the produced class of the compile operation
 * @since 3.35
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IClassContent {

	/**
	 * Provides access to the raw class bytes of this class content.
	 *
	 * @see #setBytes(byte[]) for how to update the class bytes
	 * @return the class bytes of this class content or <code>null</code> if no content is avaiable
	 * @throws CoreException
	 *             if gathering the bytes failed
	 */
	byte[] getBytes() throws CoreException;

	/**
	 * Set the class bytes to the provided array
	 *
	 * @see #getBytes() for gathering the current class bytes
	 * @param classBytes
	 * @throws CoreException
	 */
	void setBytes(byte[] classBytes) throws CoreException;

	/**
	 * Describe the relative filename of the class that could be used to store the contetn for example in a jar file or on disk.
	 *
	 * @return the filename of the class as represented for example in a jar file
	 */
	String getFileName();
}
