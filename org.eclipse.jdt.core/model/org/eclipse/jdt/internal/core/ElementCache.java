package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.LRUCache;

/**
 * An LRU cache of <code>JavaElements</code>.
 */
public class ElementCache extends OverflowingLRUCache {
	/**
	 * Constructs a new element cache of the given size.
	 */
	public ElementCache(int size) {
		super(size);
	}

	/**
	 * Constructs a new element cache of the given size.
	 */
	public ElementCache(int size, int overflow) {
		super(size, overflow);
	}

	/**
	 * Returns true if the element is successfully closed and
	 * removed from the cache, otherwise false.
	 *
	 * <p>NOTE: this triggers an external removal of this element
	 * by closing the element.
	 */
	protected boolean close(LRUCacheEntry entry) {
		IOpenable element = (IOpenable) entry._fKey;
		synchronized (element) {
			try {
				if (element.hasUnsavedChanges()) {
					return false;
				} else {
					element.close();
					return true;
				}
			} catch (JavaModelException npe) {
				return false;
			}
		}
	}

	/**
	 * Returns a new instance of the reciever.
	 */
	protected LRUCache newInstance(int size, int overflow) {
		return new ElementCache(size, overflow);
	}

}
