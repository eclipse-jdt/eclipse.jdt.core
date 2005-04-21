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
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IClassReference;

public class ClassReference implements IClassReference
{
	private final char[] className;
	ClassReference(final char[] className)
	{	
		this.className = className;
	}
	
	public char[] getTypeName(){ return this.className; }
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.className);
		buffer.append(".class"); //$NON-NLS-1$
		return buffer.toString();
	}
}
