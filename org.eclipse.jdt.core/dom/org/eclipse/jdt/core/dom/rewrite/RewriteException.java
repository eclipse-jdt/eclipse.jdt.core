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
package org.eclipse.jdt.core.dom.rewrite;

/**
 * An exception representing a failure in the DOM AST rewriting
 *
 * @since 3.0
 */
public class RewriteException extends Exception {
	/**
     * Create a RewriteException with no message. 
     */
    public RewriteException() {
    	super();
    }
    
	/**
     * Create a RewriteException with a throwable.
     */
    public RewriteException(Throwable throwable) {
    	super(throwable);
    }

    /**
     * Create a RewriteException with a message. 
     */
    public RewriteException(String msg) {
    	super(msg);
    }
}
