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
package org.eclipse.jdt.internal.core.util;

/**
 * An interface useful for debugging.  
 * Implementors know how to dump their internal state in a human-readable way
 * to a Dumper.
 *
 * @see Dumper
 */
public interface IDumpable {
	/**
	 * Dumps the internal state of this object in a human-readable way.
	 */
	public void dump(Dumper dumper);
}
