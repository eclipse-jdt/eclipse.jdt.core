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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IBufferFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * Wraps an IBufferFactory.
 * TODO: remove when removing IBufferFactory
 */
public class BufferFactoryWrapper extends WorkingCopyOwner {
	
	public BufferFactoryWrapper(IBufferFactory factory) {
		this.factory = factory;
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof BufferFactoryWrapper)) return false;
		BufferFactoryWrapper other = (BufferFactoryWrapper)obj;
		if (this.factory == null) return other.factory == null;
		return this.factory.equals(other.factory);
	}
	public int hashCode() {
		if (this.factory == null) return 0;
		return this.factory.hashCode();
	}
	public String toString() {
		return "FactoryWrapper for " + this.factory; //$NON-NLS-1$
	}
}
