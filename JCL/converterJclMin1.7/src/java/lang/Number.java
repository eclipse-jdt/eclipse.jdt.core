/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.lang;


public abstract class Number implements java.io.Serializable {
	private static final long serialVersionUID = 3166984097235214156L;
	public abstract int intValue();
	public abstract long longValue();
	public abstract float floatValue();
	public abstract double doubleValue();
	public byte byteValue() {
		return (byte) intValue();
	}
	public short shortValue() {
		return (short) intValue();
	}
}
