/**********************************************************************
Copyright (c) 2002, 2003 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core.compiler;

/**
 * Exception thrown by a scanner when encountering lexical errors.
 */
public class InvalidInputException extends Exception {

	/**
	 * Creates a new exception with no detail message.
	 */
	public InvalidInputException() {
		super();
	}
	/**
	 * Creates a new exception with the given detail message.
	 * @param message the detail message
	 */
	public InvalidInputException(String message) {
		super(message);
	}
}
