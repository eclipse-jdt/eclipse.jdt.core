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
package java.io;

public class UnsupportedEncodingException extends IOException {
	private static final long serialVersionUID = 1031403719398591519L;
	public UnsupportedEncodingException(String s) {
		super(s);
	}
	public UnsupportedEncodingException() {
	}
}
