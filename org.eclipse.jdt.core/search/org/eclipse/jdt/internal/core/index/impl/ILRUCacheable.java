package org.eclipse.jdt.internal.core.index.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Types implementing this interface can occupy a variable amount of space
 * in an LRUCache.  Cached items that do not implement this interface are
 * considered to occupy one unit of space.
 *
 * @see LRUCache
 */
public interface ILRUCacheable {
	/**
	 * Returns the space the receiver consumes in an LRU Cache.  The default space
	 * value is 1.
	 *
	 * @return int Amount of cache space taken by the receiver
	 */
	public int getCacheFootprint();
}
