/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Date;

public class VerboseElementCache extends ElementCache {

	private Object beingAdded;
	private String name;

	public VerboseElementCache(int size, String name) {
		super(size);
		this.name = name;
	}

	protected boolean makeSpace(int space) {
		if (this.beingAdded != null && !isSpaceAvailable(space, fSpaceLimit)) {
			System.out.println(Thread.currentThread() + " " + new Date(System.currentTimeMillis()).toString()); //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " MAKING SPACE IN " + toStringFillingRation(this.name)); //$NON-NLS-1$
			System.out.println(Thread.currentThread() + " WHILE OPENING "+ ((JavaElement) this.beingAdded).toStringWithAncestors());  //$NON-NLS-1$
			System.out.println();
		}
		return super.makeSpace(space);
	}

	public Object put(Object key, Object value) {
		try {
			if (this.beingAdded == null)
				this.beingAdded = key;
			return super.put(key, value);
		} finally {
			if (key.equals(this.beingAdded))
				this.beingAdded = null;
		}
	}

}
