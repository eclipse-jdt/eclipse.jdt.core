/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public interface BaseTypes {
	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, TypeConstants.INT, new char[] {'I'}); //$NON-NLS-1$
	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, TypeConstants.BYTE, new char[] {'B'}); //$NON-NLS-1$
	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, TypeConstants.SHORT, new char[] {'S'}); //$NON-NLS-1$
	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, TypeConstants.CHAR, new char[] {'C'}); //$NON-NLS-1$
	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, TypeConstants.LONG, new char[] {'J'}); //$NON-NLS-1$
	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, TypeConstants.FLOAT, new char[] {'F'}); //$NON-NLS-1$
	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, TypeConstants.DOUBLE, new char[] {'D'}); //$NON-NLS-1$
	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, TypeConstants.BOOLEAN, new char[] {'Z'}); //$NON-NLS-1$
	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, TypeConstants.NULL, new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$
	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, TypeConstants.VOID, new char[] {'V'}); //$NON-NLS-1$
}
