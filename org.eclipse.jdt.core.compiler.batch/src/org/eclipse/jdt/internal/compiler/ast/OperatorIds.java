/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

public interface OperatorIds {
	public static final int AND_AND = 0;
	public static final int OR_OR = 1;
	public static final int AND = 2;
	public static final int OR = 3;
	public static final int LESS = 4;
	public static final int LESS_EQUAL = 5;
	public static final int GREATER = 6;
	public static final int GREATER_EQUAL = 7;
	public static final int XOR = 8;
	public static final int DIVIDE = 9;
	public static final int LEFT_SHIFT = 10;
	public static final int NOT = 11;
	public static final int TWIDDLE = 12;
	public static final int MINUS = 13;
	public static final int PLUS = 14;
	public static final int MULTIPLY = 15;
	public static final int REMAINDER = 16;
	public static final int RIGHT_SHIFT = 17;
	public static final int EQUAL_EQUAL = 18;
	public static final int UNSIGNED_RIGHT_SHIFT= 19;
	public static final int NumberOfTables = 20;

	public static final int QUESTIONCOLON = 21;

	public static final int NOT_EQUAL = 22;
	public static final int EQUAL = 23;
	public static final int INSTANCEOF = 24;
	public static final int PLUS_PLUS = 25;
	public static final int MINUS_MINUS = 26;
}
