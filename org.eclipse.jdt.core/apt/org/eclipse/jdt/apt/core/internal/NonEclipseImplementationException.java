/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

/**
 * Thrown when the eclipse compiler encountered a non-eclipse implementation of the mirror type system.
 */
public class NonEclipseImplementationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NonEclipseImplementationException(String msg)
    { super(msg); }

    public NonEclipseImplementationException(){}
}
