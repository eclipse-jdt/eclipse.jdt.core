/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
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
 * Message type. This is used to report potential errors found during the AST resolution or parsing.
 * It contains the localized error message reported by the compiler and the source positions.
 * The instances from this class are immutable.
 *
 * @since 2.0
 */
public class Message {
	private String message;
	private int sourcePosition;
	
	/**
	 * This constructor is used to initialize the message with its relevant information.
	 * @param message the localized message reported by the compiler
	 * @param sourcePosition the position in the source where this error should be reported
	 * @since 2.0
	 */
	public Message(String message, int sourcePosition) {
		this.message = message;
		this.sourcePosition = sourcePosition;
	}
	
	/**
	 * Return the localized message of this object.
	 * @return String
	 * @since 2.0
	 */
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * Return the source position of this object.
	 * @return int
	 * @since 2.0
	 */
	public int getSourcePosition() {
		return this.sourcePosition;
	}
}
