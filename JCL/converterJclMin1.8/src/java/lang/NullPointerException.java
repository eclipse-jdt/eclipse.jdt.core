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

public class NullPointerException extends RuntimeException {
	private static final long serialVersionUID = -945245241303869636L;

	/**
	 * Constructor for NullPointerException.
	 */
	public NullPointerException(String s) {
		super(s);
	}

	/**
	 * Constructor for NullPointerException.
	 */
	public NullPointerException() {
		super();
	}

}
