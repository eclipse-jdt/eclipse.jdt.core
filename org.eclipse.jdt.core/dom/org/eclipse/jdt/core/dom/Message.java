/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Error message used to report potential errors found during the AST parsing
 * or name resolution. Instances of this class are immutable.
 *
 * @since 2.0
 */
public class Message {
	
	/**
	 * The message.
	 */
	private String message;
	
	/**
	 * The character index into the original source string, or -1 if none.
	 */
	private int sourcePosition;
	
	/**
	 * Creates a message.
	 * 
	 * @param message the localized message reported by the compiler
	 * @param sourcePosition the 0-based character index into the 
	 *    original source file, or <code>-1</code> if no source position
	 *    information is to be recorded for this message
	 */
	public Message(String message, int sourcePosition) {
		if (message == null) {
			throw new IllegalArgumentException();
		}
		if (sourcePosition < -1) {
			throw new IllegalArgumentException();
		}
		this.message = message;
		this.sourcePosition = sourcePosition;
	}
	
	/**
	 * Returns the localized message.
	 * 
	 * @return the localized message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Returns the character index into the original source file.
	 * 
	 * @return the 0-based character index, or <code>-1</code>
	 *    if no source position information is recorded for this
	 *    message
	 */
	public int getSourcePosition() {
		return sourcePosition;
	}
}
