/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.internal.dom.rewrite;

/**
 * An exception representing a failure in the DOM AST rewriting
 *
 * @since 3.0
 */
public class RewriteRuntimeException extends RuntimeException {
	/**
     * Create a RewriteRuntimeException with no message. 
     */
    public RewriteRuntimeException() {
    	super();
    }
    
	/**
     * Create a RewriteRuntimeException with a throwable.
     */
    public RewriteRuntimeException(Throwable throwable) {
    	super(throwable);
    }

    /**
     * Create a RewriteRuntimeException with a message. 
     */
    public RewriteRuntimeException(String msg) {
    	super(msg);
    }
}
