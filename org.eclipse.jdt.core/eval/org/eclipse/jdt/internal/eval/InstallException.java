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
package org.eclipse.jdt.internal.eval;

/**
 * A <code>InstallException</code> is thrown when installing class files on a target has failed
 * for any reason.
 */
public class InstallException extends Exception {
/**
 * Constructs a <code>InstallException</code> with no detail  message.
 */
public InstallException() {
	super();
}
/**
 * Constructs a <code>InstallException</code> with the specified 
 * detail message. 
 *
 * @param   s   the detail message.
 */
public InstallException(String s) {
	super(s);
}
}
