/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Exception thrown by a class file reader when encountering a error in decoding
 * information contained in a .class file.
 * 
 * @since 2.0
 */
public class ClassFormatException extends Exception {
	public static final int ERROR_MALFORMED_UTF8 = 1;
	public static final int ERROR_TRUNCATED_INPUT = 2;
	public static final int INVALID_CONSTANT_POOL_ENTRY = 3;
	public static final int TOO_MANY_BYTES = 4;
	public static final int INVALID_ARGUMENTS_FOR_INVOKEINTERFACE = 5;
	public static final int INVALID_BYTECODE = 6;
	
	/**
	 * Constructor for ClassFormatException.
	 */
	public ClassFormatException(int errorID) {
	}

	/**
	 * Constructor for ClassFormatException.
	 * @param message the message for the exception
	 */
	public ClassFormatException(String message) {
		super(message);
	}
}
