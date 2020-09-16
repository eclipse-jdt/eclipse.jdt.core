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

public class Character {
	public Character(char c) {
	}
	public static char toLowerCase(char c) {
		return ' ';
	}
	
	public static int getNumericValue(char c) {
		return 0;
	}
	
    public static int digit(char ch, int radix) {
    	 return 0;
    }
    public static boolean isWhitespace(char c) {
    	return false;
    }
    public static boolean isJavaIdentifierStart(char c) {
    	return false;
    }
    public static boolean isJavaIdentifierPart(char c) {
    	return false;
    }
    public static boolean isDigit(char c) {
    	return false;
    }
}
