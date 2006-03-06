/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jdt.core.compiler.InvalidInputException;

public class ScannerHelper {
	public final static int Bit1 = 0x1;
	public final static int Bit2 = 0x2;
	public final static int Bit3 = 0x4;
	public final static int Bit4 = 0x8;
	public final static int Bit5 = 0x10;
	public final static int Bit6 = 0x20;
	public final static int Bit7 = 0x40;
	public final static int Bit8 = 0x80;
	public final static int Bit9 = 0x100;
	public final static int Bit10= 0x200;
	public final static int Bit11 = 0x400;
	public final static int Bit12 = 0x800;
	public final static int Bit13 = 0x1000;
	public final static int Bit14 = 0x2000;
	public final static int Bit15 = 0x4000;
	public final static int Bit16 = 0x8000;
	public final static int Bit17 = 0x10000;
	public final static int Bit18 = 0x20000; 
	public final static int Bit19 = 0x40000; 
	public final static int Bit20 = 0x80000; 
	public final static int Bit21 = 0x100000; 		
	public final static int Bit22 = 0x200000;
	public final static int Bit23 = 0x400000;
	public final static int Bit24 = 0x800000;
	public final static int Bit25 = 0x1000000;
	public final static int Bit26 = 0x2000000;
	public final static int Bit27 = 0x4000000;
	public final static int Bit28 = 0x8000000;
	public final static int Bit29 = 0x10000000;
	public final static int Bit30 = 0x20000000;
	public final static int Bit31 = 0x40000000;
	public final static int Bit32 = 0x80000000;
	public final static long Bit33 = 0x100000000L;
	public final static long Bit34 = 0x200000000L;
	public final static long Bit35 = 0x400000000L;
	public final static long Bit36 = 0x800000000L;
	public final static long Bit37 = 0x1000000000L;
	public final static long Bit38 = 0x2000000000L;
	public final static long Bit39 = 0x4000000000L;
	public final static long Bit40 = 0x8000000000L;
	public final static long Bit41 = 0x10000000000L;
	public final static long Bit42 = 0x20000000000L;
	public final static long Bit43 = 0x40000000000L;
	public final static long Bit44 = 0x80000000000L;
	public final static long Bit45 = 0x100000000000L;
	public final static long Bit46 = 0x200000000000L;
	public final static long Bit47 = 0x400000000000L;
	public final static long Bit48 = 0x800000000000L;
	public final static long Bit49 = 0x1000000000000L;
	public final static long Bit50 = 0x2000000000000L;
	public final static long Bit51 = 0x4000000000000L;
	public final static long Bit52 = 0x8000000000000L;
	public final static long Bit53 = 0x10000000000000L;
	public final static long Bit54 = 0x20000000000000L;
	public final static long Bit55 = 0x40000000000000L;
	public final static long Bit56 = 0x80000000000000L;
	public final static long Bit57 = 0x100000000000000L;
	public final static long Bit58 = 0x200000000000000L;
	public final static long Bit59 = 0x400000000000000L;
	public final static long Bit60 = 0x800000000000000L;
	public final static long Bit61 = 0x1000000000000000L;
	public final static long Bit62 = 0x2000000000000000L;
	public final static long Bit63 = 0x4000000000000000L;
	public final static long Bit64 = 0x8000000000000000L;
	public final static long[] Bits = { Bit1, Bit2, Bit3, Bit4, Bit5, Bit6,
			Bit7, Bit8, Bit9, Bit10, Bit11, Bit12, Bit13, Bit14, Bit15, Bit16,
			Bit17, Bit18, Bit19, Bit20, Bit21, Bit22, Bit23, Bit24, Bit25,
			Bit26, Bit27, Bit28, Bit29, Bit30, Bit31, Bit32, Bit33, Bit34,
			Bit35, Bit36, Bit37, Bit38, Bit39, Bit40, Bit41, Bit42, Bit43,
			Bit44, Bit45, Bit46, Bit47, Bit48, Bit49, Bit50, Bit51, Bit52,
			Bit53, Bit54, Bit55, Bit56, Bit57, Bit58, Bit59, Bit60, Bit61,
			Bit62, Bit63, Bit64,
	};

	private static final int START_INDEX = 0;
	private static final int PART_INDEX = 1;

	private static long[][][] Tables;

	public final static int MAX_OBVIOUS = 128;
	public final static int[] OBVIOUS_IDENT_CHAR_NATURES = new int[MAX_OBVIOUS];
	public final static int C_UPPER_LETTER = Bit6;
	public final static int C_LOWER_LETTER = Bit5;
	public final static int C_IDENT_PART = Bit4;
	public final static int C_DIGIT = Bit3;
	public final static int C_SEPARATOR = Bit2;
	public final static int C_SPACE = Bit1;
	static {
		for (int i = '0'; i <= '9'; i++) 
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_DIGIT;
		
		for (int i = 'a'; i <= 'z'; i++) 
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_LOWER_LETTER;
		for (int i = 'A'; i <= 'Z'; i++) 
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_UPPER_LETTER;
		OBVIOUS_IDENT_CHAR_NATURES['_'] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES['$'] = C_IDENT_PART;
		
		OBVIOUS_IDENT_CHAR_NATURES[10] = C_SPACE; // \ u000a: LINE FEED
		OBVIOUS_IDENT_CHAR_NATURES[12] = C_SPACE; // \ u000c: FORM FEED
		OBVIOUS_IDENT_CHAR_NATURES[13] = C_SPACE; //  \ u000d: CARRIAGE RETURN
		OBVIOUS_IDENT_CHAR_NATURES[32] = C_SPACE; //  \ u0020: SPACE
		OBVIOUS_IDENT_CHAR_NATURES[ 9] = C_SPACE; // \ u0009: HORIZONTAL TABULATION
		
		OBVIOUS_IDENT_CHAR_NATURES['.'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[':'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[';'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[','] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['['] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[']'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['('] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[')'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['{'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['}'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['+'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['-'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['*'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['/'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['='] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['&'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['|'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['?'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['<'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['>'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['!'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['%'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['^'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['~'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['"'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['\''] = C_SEPARATOR;
	}
	
static {
	Tables = new long[2][][];
	Tables[START_INDEX] = new long[2][];
	Tables[PART_INDEX] = new long[3][];
	try {
		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("start1.rsc")); //$NON-NLS-1$
		long[] readValues = new long[1024];
		for (int i = 0; i < 1024; i++) {
			readValues[i] = inputStream.readLong();
		}
		inputStream.close();
		Tables[START_INDEX][0] = readValues;
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	try {
		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("start2.rsc")); //$NON-NLS-1$
		long[] readValues = new long[1024];
		for (int i = 0; i < 1024; i++) {
			readValues[i] = inputStream.readLong();
		}
		inputStream.close();
		Tables[START_INDEX][1] = readValues;
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	try {
		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("part1.rsc")); //$NON-NLS-1$
		long[] readValues = new long[1024];
		for (int i = 0; i < 1024; i++) {
			readValues[i] = inputStream.readLong();
		}
		inputStream.close();
		Tables[PART_INDEX][0] = readValues;
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	try {
		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("part2.rsc")); //$NON-NLS-1$
		long[] readValues = new long[1024];
		for (int i = 0; i < 1024; i++) {
			readValues[i] = inputStream.readLong();
		}
		inputStream.close();
		Tables[PART_INDEX][1] = readValues;
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	try {
		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("part14.rsc")); //$NON-NLS-1$
		long[] readValues = new long[1024];
		for (int i = 0; i < 1024; i++) {
			readValues[i] = inputStream.readLong();
		}
		inputStream.close();
		Tables[PART_INDEX][2] = readValues;
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

private final static boolean isBitSet(long[] values, int i) {
	try {
		return (values[i / 64] & Bits[i % 64]) != 0;
	} catch (NullPointerException e) {
		return false;
	}
}

public static boolean isJavaIdentifierPart(char high, char low) {
	int codePoint = toCodePoint(high, low);
	switch((codePoint & 0x1F0000) >> 16) {
		case 0 :
			return Character.isJavaIdentifierPart((char) codePoint);
		case 1 :
			return isBitSet(Tables[PART_INDEX][0], codePoint & 0xFFFF);
		case 2 :
			return isBitSet(Tables[PART_INDEX][1], codePoint & 0xFFFF);
		case 14 :
			return isBitSet(Tables[PART_INDEX][2], codePoint & 0xFFFF);
	}
	return false;
}

public static boolean isJavaIdentifierStart(char high, char low) {
	int codePoint = toCodePoint(high, low);
	switch((codePoint & 0x1F0000) >> 16) {
		case 0 :
			return Character.isJavaIdentifierStart((char) codePoint);
		case 1 :
			return isBitSet(Tables[START_INDEX][0], codePoint & 0xFFFF);
		case 2 :
			return isBitSet(Tables[START_INDEX][1], codePoint & 0xFFFF);
	}
	return false;
}

private static int toCodePoint(char high, char low) {	
	return (high - Scanner.HIGH_SURROGATE_MIN_VALUE) * 0x400 + (low - Scanner.LOW_SURROGATE_MIN_VALUE) + 0x10000;
}
public static boolean isDigit(char c) throws InvalidInputException {
	if(c < ScannerHelper.MAX_OBVIOUS) {
		return ScannerHelper.C_DIGIT == ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c];
	}
	if (Character.isDigit(c)) {
		throw new InvalidInputException(Scanner.INVALID_DIGIT);
	} else {
		return false;
	}
}
public static boolean isWhitespace(char c) {
	return c < ScannerHelper.MAX_OBVIOUS && C_SPACE == ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c];
}
}
