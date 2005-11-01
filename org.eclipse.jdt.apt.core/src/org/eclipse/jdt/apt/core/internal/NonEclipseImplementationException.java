/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
