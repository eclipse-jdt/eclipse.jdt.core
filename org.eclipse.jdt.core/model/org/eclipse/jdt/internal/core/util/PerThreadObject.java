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

import java.util.Hashtable;

/**
 * Implementation of data structure remembering an Object value by thread. Its purpose is to ease
 * writing multi-threaded algorithms by providing a per thread data structure.
 */
public class PerThreadObject {

	private Hashtable internalMap = new Hashtable(3); // synchronized map
	
	/**
	 * Answer the current object value for this thread
	 */
	public Object getCurrent(){
		return this.internalMap.get(Thread.currentThread());
	}
	
	/**
	 * Set the object value for this current thread - setting to null is equivalent to removing it
	 */
	public void setCurrent(Object current){
		if (current == null){
			this.internalMap.remove(Thread.currentThread());			
		} else {
			this.internalMap.put(Thread.currentThread(), current);
		}
	}
}

