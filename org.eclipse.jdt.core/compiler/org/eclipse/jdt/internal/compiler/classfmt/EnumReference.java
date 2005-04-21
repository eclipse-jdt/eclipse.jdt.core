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

import org.eclipse.jdt.internal.compiler.env.IEnumConstantReference;

/**
 * Represents a reference to the enum constant in a class file. 
 */
public class EnumReference implements IEnumConstantReference
{
	/** type name of the enum type */
	private final char[] typeName;
	/** name of the enum constant */
	private final char[] constName;
	
	EnumReference(final char[] typeName, char[] constName)
	{
		this.typeName = typeName;
		this.constName = constName;
	}
	
	public char[] getTypeName(){ return this.typeName; }
	public char[] getEnumConstantName(){ return this.constName; }
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.typeName);
		buffer.append('.');
		buffer.append(this.constName);
		return buffer.toString();
	}
}
