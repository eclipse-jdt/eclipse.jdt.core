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
package org.eclipse.jdt.internal.compiler.lookup;

public interface BaseTypes {
	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, TypeConstants.INT, new char[] {'I'});
	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, TypeConstants.BYTE, new char[] {'B'});
	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, TypeConstants.SHORT, new char[] {'S'});
	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, TypeConstants.CHAR, new char[] {'C'});
	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, TypeConstants.LONG, new char[] {'J'});
	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, TypeConstants.FLOAT, new char[] {'F'});
	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, TypeConstants.DOUBLE, new char[] {'D'});
	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, TypeConstants.BOOLEAN, new char[] {'Z'});
	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, TypeConstants.NULL, new char[] {'N'}); //N stands for null even if it is never internally used
	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, TypeConstants.VOID, new char[] {'V'});
}
